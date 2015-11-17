/*
 * Copyright (C) 2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.debugger.flash.DebugConnectionListener;
import com.jpexs.debugger.flash.DebugMessageListener;
import com.jpexs.debugger.flash.Debugger;
import com.jpexs.debugger.flash.DebuggerCommands;
import com.jpexs.debugger.flash.DebuggerConnection;
import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.messages.in.InAskBreakpoints;
import com.jpexs.debugger.flash.messages.in.InBreakAt;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InBreakReason;
import com.jpexs.debugger.flash.messages.in.InContinue;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSetBreakpoint;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.debugger.flash.messages.in.InTrace;
import com.jpexs.debugger.flash.messages.in.InVersion;
import com.jpexs.debugger.flash.messages.out.OutGetBreakReason;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DebuggerHandler implements DebugConnectionListener {

    private boolean connected = false;
    private DebuggerCommands commands = null;
    private List<InSwfInfo.SwfInfo> swfs = new ArrayList<>();
    private boolean paused = true;
    private Map<Integer, ClassPath> modulePaths = new HashMap<>();
    private Map<ClassPath, Integer> classToModule = new HashMap<>();

    private InFrame frame;

    private InBreakAtExt breakInfo;
    private InBreakReason breakReason;

    private final List<VariableChangedListener> varListeners = new ArrayList<>();

    private final List<TraceListener> traceListeners = new ArrayList<>();

    private final List<ConnectionListener> clisteners = new ArrayList<>();

    public void notSuspended() {
        frame = null;
        breakInfo = null;
        breakReason = null;
        for (VariableChangedListener l : varListeners) {
            l.variablesChanged();
        }
    }

    public String moduleToString(int file) {
        if (!modulePaths.containsKey(file)) {
            return "unknown";
        }
        return modulePaths.get(file).toString();
    }

    public InBreakAtExt getBreakInfo() {
        return breakInfo;
    }

    public InBreakReason getBreakReason() {
        return breakReason;
    }

    public static interface ConnectionListener {

        public void connected();

        public void disconnected();

    }

    public static interface TraceListener {

        public void trace(String... val);

    }

    public static interface VariableChangedListener {

        public void variablesChanged();

    }

    public void addVariableChangedListener(VariableChangedListener l) {
        varListeners.add(l);
    }

    public void addTraceListener(TraceListener l) {
        traceListeners.add(l);
    }

    public void removeTraceListener(TraceListener l) {
        traceListeners.remove(l);
    }

    public void removeVariableChangedListener(VariableChangedListener l) {
        varListeners.remove(l);
    }

    public void addConnectionListener(ConnectionListener l) {
        clisteners.add(l);
    }

    public void removeConnectionListener(ConnectionListener l) {
        clisteners.remove(l);
    }

    public InFrame getFrame() {
        return frame;
    }

    public int moduleIdOf(ScriptPack pack) {
        if (classToModule.containsKey(pack.getClassPath())) {
            return classToModule.get(pack.getClassPath());
        }
        return -1;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public List<InSwfInfo.SwfInfo> getSwfs() {
        return swfs;
    }

    public void disconnect() {
        frame = null;
        breakInfo = null;
        breakReason = null;
        connected = false;
        if (commands != null) {
            commands.disconnect();
        }
        commands = null;
        for (ConnectionListener l : clisteners) {
            l.disconnected();
        }
        for (VariableChangedListener l : varListeners) {
            l.variablesChanged();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public DebuggerCommands getCommands() throws IOException {
        if (!isConnected() || commands == null) {
            throw new IOException("Not connected");
        }
        return commands;
    }

    private static void enlog(Class<?> cls) {
        Level level = Level.FINEST;

        Logger mylog = Logger.getLogger(cls.getName());
        mylog.setLevel(level);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(level);
        mylog.addHandler(ch);
    }

    @Override
    public void connected(DebuggerConnection con) {

        synchronized (DebuggerHandler.this) {
            paused = true;
        }

        Main.getMainFrame().getPanel().updateMenu();

        //enlog(DebuggerConnection.class);
        //enlog(DebuggerCommands.class);
        try {
            //rootLog.getHandlers()[0].setLevel(level);
            con.getMessage(InVersion.class);
        } catch (IOException ex) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        commands = new DebuggerCommands(con);
        try {
            commands.stopWarning();
            commands.setStopOnFault();
            commands.setEnumerateOverride();
            commands.setNotifyFailure();
            commands.setInvokeSetters();
            commands.setSwfLoadNotify();
            commands.setGetterTimeout(1500);
            commands.setSetterTimeout(5000);
            commands.squelch(true);
            swfs = commands.getSwfInfo(1);

            Map<Integer, String> moduleNames = new HashMap<>();

            modulePaths = new HashMap<>();
            classToModule = new HashMap<>();

            int numScript = con.getMessage(InNumScript.class).num;
            for (int i = 0; i < numScript; i++) {
                InScript sc = con.getMessage(InScript.class);
                moduleNames.put(sc.module, sc.name);
            }

            for (int mname : moduleNames.keySet()) {
                String name = moduleNames.get(mname);
                String[] parts = name.split(";");

                if (parts.length == 3) {
                    String clsName = parts[2].replace(".as", "");
                    String pkg = parts[1].replace("/", "\\").replace("\\", ".");
                    ClassPath cp = new ClassPath(DottedChain.parse(pkg), clsName);
                    modulePaths.put(mname, cp);
                    classToModule.put(cp, mname);
                }
            }

            con.getMessage(InSetBreakpoint.class);
            con.getMessage(InAskBreakpoints.class);
            con.addMessageListener(new DebugMessageListener<InContinue>() {

                @Override
                public void message(InContinue msg) {
                    synchronized (DebuggerHandler.this) {
                        paused = false;
                    }
                    Main.getMainFrame().getPanel().updateMenu();
                }
            });
            con.addMessageListener(new DebugMessageListener<InBreakAt>() {

                @Override
                public void message(InBreakAt message) {
                    synchronized (DebuggerHandler.this) {
                        paused = true;
                    }
                    View.execInEventDispatchLater(new Runnable() {

                        @Override
                        public void run() {

                            Main.getMainFrame().getPanel().updateMenu();
                            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "break at {0}:{1}", new Object[]{moduleNames.get(message.file), message.line});
                            if (!modulePaths.containsKey(message.file)) {
                                return;
                            }

                            ClassPath cls = modulePaths.get(message.file);
                            Main.startWork(AppStrings.translate("work.breakat") + cls + ":" + message.line, null);

                            try {
                                breakInfo = con.getMessage(InBreakAtExt.class);
                                breakReason = con.sendMessage(new OutGetBreakReason(con), InBreakReason.class);
                                frame = commands.getFrame(0);

                                for (VariableChangedListener l : varListeners) {
                                    l.variablesChanged();
                                }

                            } catch (IOException ex) {
                                //ignore
                            }
                            Main.breakAt(cls, message.line);
                        }
                    }
                    );
                    //dc.sendContinue();
                }
            });
            //commands.sendContinue();
            List<ScriptPack> packs = Main.getMainFrame().getPanel().getCurrentSwf().getAS3Packs();
            for (ScriptPack sp : packs) {
                ClassPath cp = sp.getClassPath();
                if (classToModule.containsKey(cp)) {
                    int file = classToModule.get(cp);
                    Set<Integer> bpts = new TreeSet<>(Main.getPackBreakPoints(sp));
                    for (int line : bpts) {
                        if (!commands.addBreakPoint(file, line)) {
                            Main.markBreakPointInvalid(sp, line);
                        }
                    }
                }
            }

            Main.getMainFrame()
                    .getPanel().refreshBreakPoints();
            connected = true;

            for (ConnectionListener l : clisteners) {
                l.connected();
            }

            if (Configuration.debugHalt.get()) {
                Main.startWork(AppStrings.translate("work.halted"), null);
            } else {
                commands.sendContinue();
            }

            new CancellableWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        while (isConnected()) {
                            InTrace tr = con.getMessage(InTrace.class);
                            for (TraceListener l : traceListeners) {
                                l.trace(tr.text);
                            }
                        }
                    } catch (IOException ex) {
                        //ignore
                    }
                    return null;
                }
            }.execute();

        } catch (IOException ex) {
            connected = false;
        }
    }
}
