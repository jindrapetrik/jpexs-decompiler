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
import com.jpexs.debugger.flash.SWD;
import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.messages.in.InAskBreakpoints;
import com.jpexs.debugger.flash.messages.in.InBreakAt;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InBreakReason;
import com.jpexs.debugger.flash.messages.in.InContinue;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.debugger.flash.messages.in.InGetSwd;
import com.jpexs.debugger.flash.messages.in.InGetSwf;
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InProcessTag;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSetBreakpoint;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.debugger.flash.messages.in.InTrace;
import com.jpexs.debugger.flash.messages.in.InVersion;
import com.jpexs.debugger.flash.messages.out.OutGetBreakReason;
import com.jpexs.debugger.flash.messages.out.OutGetSwd;
import com.jpexs.debugger.flash.messages.out.OutGetSwf;
import com.jpexs.debugger.flash.messages.out.OutProcessedTag;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.CancellableWorker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class DebuggerHandler implements DebugConnectionListener {

    private boolean connected = false;
    private DebuggerCommands commands = null;
    private List<InSwfInfo.SwfInfo> swfs = new ArrayList<>();
    private boolean paused = true;
    private Map<Integer, String> modulePaths = new HashMap<>();
    private Map<String, Integer> classToModule = new HashMap<>();

    private Map<String, Set<Integer>> toAddBPointMap = new HashMap<>();
    private Map<String, Set<Integer>> confirmedPointMap = new HashMap<>();
    private Map<String, Set<Integer>> invalidBreakPointMap = new HashMap<>();
    private Map<String, Set<Integer>> toRemoveBPointMap = new HashMap<>();

    private int breakIp = -1;
    private String breakScriptName = null;

    public int getBreakIp() {
        return breakIp;
    }

    public String getBreakScriptName() {
        return breakScriptName;
    }

    public synchronized void removeBreakPoint(String scriptName, int line) {
        if (isBreakpointInvalid(scriptName, line)) {
            invalidBreakPointMap.get(scriptName).remove(line);
            if (invalidBreakPointMap.get(scriptName).isEmpty()) {
                invalidBreakPointMap.remove(scriptName);
            }
            return;
        }
        if (isBreakpointToAdd(scriptName, line)) {
            toAddBPointMap.get(scriptName).remove(line);
            if (toAddBPointMap.get(scriptName).isEmpty()) {
                toAddBPointMap.remove(scriptName);
            }
        } else if (isBreakpointConfirmed(scriptName, line)) {
            if (!toRemoveBPointMap.containsKey(scriptName)) {
                toRemoveBPointMap.put(scriptName, new TreeSet<>());
            }
            toRemoveBPointMap.get(scriptName).add(line);
        }
        try {
            sendBreakPoints(false);
        } catch (IOException ex) {
            //ignore
        }
    }

    public synchronized Set<Integer> getBreakPoints(String scriptName) {
        Set<Integer> lines = new TreeSet<>();
        if (confirmedPointMap.containsKey(scriptName)) {
            lines.addAll(confirmedPointMap.get(scriptName));
        }
        if (toAddBPointMap.containsKey(scriptName)) {
            lines.addAll(toAddBPointMap.get(scriptName));
        }
        return lines;
    }

    public synchronized void clearBreakPoints() {
        for (String scriptName : confirmedPointMap.keySet()) {
            if (!toAddBPointMap.containsKey(scriptName)) {
                toAddBPointMap.put(scriptName, new TreeSet<>());
            }
            toAddBPointMap.get(scriptName).addAll(confirmedPointMap.get(scriptName));
        }
        confirmedPointMap.clear();
        invalidBreakPointMap.clear();
    }

    public synchronized Map<String, Set<Integer>> getAllBreakPoints(boolean validOnly) {
        Map<String, Set<Integer>> ret = new HashMap<>();
        for (String scriptName : confirmedPointMap.keySet()) {
            Set<Integer> lines = new TreeSet<>();
            lines.addAll(confirmedPointMap.get(scriptName));
            ret.put(scriptName, lines);
        }
        for (String scriptName : toAddBPointMap.keySet()) {
            if (!ret.containsKey(scriptName)) {
                ret.put(scriptName, new TreeSet<>());
            }
            ret.get(scriptName).addAll(toAddBPointMap.get(scriptName));
        }
        if (!validOnly) {
            for (String scriptName : invalidBreakPointMap.keySet()) {
                if (!ret.containsKey(scriptName)) {
                    ret.put(scriptName, new TreeSet<>());
                }
                ret.get(scriptName).addAll(invalidBreakPointMap.get(scriptName));
            }
        }
        return ret;
    }

    public boolean addBreakPoint(String scriptName, int line) {
        synchronized (this) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "adding bp " + scriptName + ":" + line);
            if (isBreakpointToRemove(scriptName, line)) {
                toRemoveBPointMap.get(scriptName).remove(line);
                if (toRemoveBPointMap.get(scriptName).isEmpty()) {
                    toRemoveBPointMap.remove(scriptName);
                }
            }

            if (isBreakpointConfirmed(scriptName, line)) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp " + scriptName + ":" + line + " already confirmed");
                return true;
            }
            if (isBreakpointInvalid(scriptName, line)) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp " + scriptName + ":" + line + " already invalid");
                return false;
            }
            if (!toAddBPointMap.containsKey(scriptName)) {
                toAddBPointMap.put(scriptName, new TreeSet<>());
            }
            toAddBPointMap.get(scriptName).add(line);
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp " + scriptName + ":" + line + " added to todo");
        }
        try {
            sendBreakPoints(false);
        } catch (IOException ex) {
            //ignored
        }

        return true;
    }

    public synchronized boolean isBreakpointConfirmed(String scriptName, int line) {
        return confirmedPointMap.containsKey(scriptName) && confirmedPointMap.get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointToAdd(String scriptName, int line) {
        return toAddBPointMap.containsKey(scriptName) && toAddBPointMap.get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointToRemove(String scriptName, int line) {
        return toRemoveBPointMap.containsKey(scriptName) && toRemoveBPointMap.get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointInvalid(String scriptName, int line) {
        return invalidBreakPointMap.containsKey(scriptName) && invalidBreakPointMap.get(scriptName).contains(line);
    }

    private synchronized void markBreakPointInvalid(String scriptName, int line) {
        if (!invalidBreakPointMap.containsKey(scriptName)) {
            invalidBreakPointMap.put(scriptName, new TreeSet<>());
        }
        invalidBreakPointMap.get(scriptName).add(line);
    }

    private InFrame frame;

    private InBreakAtExt breakInfo;
    private InBreakReason breakReason;

    private final List<BreakListener> breakListeners = new ArrayList<>();

    private final List<TraceListener> traceListeners = new ArrayList<>();

    private final List<ConnectionListener> clisteners = new ArrayList<>();

    public String moduleToString(int file) {
        if (!modulePaths.containsKey(file)) {
            return "unknown";
        }
        return modulePaths.get(file);
    }

    public synchronized InBreakAtExt getBreakInfo() {
        if (!paused) {
            return null;
        }
        return breakInfo;
    }

    public synchronized InBreakReason getBreakReason() {
        if (!paused) {
            return null;
        }
        return breakReason;
    }

    public static interface ConnectionListener {

        public void connected();

        public void disconnected();

    }

    public static interface TraceListener {

        public void trace(String... val);

    }

    public static interface BreakListener {

        public void breakAt(String scriptName, int line);

        public void doContinue();

    }

    public void addBreakListener(BreakListener l) {
        breakListeners.add(l);
    }

    public void addTraceListener(TraceListener l) {
        traceListeners.add(l);
    }

    public void removeTraceListener(TraceListener l) {
        traceListeners.remove(l);
    }

    public void removeBreakListener(BreakListener l) {
        breakListeners.remove(l);
    }

    public void addConnectionListener(ConnectionListener l) {
        clisteners.add(l);
    }

    public void removeConnectionListener(ConnectionListener l) {
        clisteners.remove(l);
    }

    public synchronized InFrame getFrame() {
        if (!paused) {
            return null;
        }
        return frame;
    }

    public synchronized int moduleIdOf(String pack) {
        if (classToModule.containsKey(pack)) {
            return classToModule.get(pack);
        }
        return -1;
    }

    public boolean isPaused() {
        if (!isConnected()) {
            return false;
        }
        synchronized (this) {
            return paused;
        }
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
        synchronized (this) {
            for (String scriptName : confirmedPointMap.keySet()) {
                if (!toAddBPointMap.containsKey(scriptName)) {
                    toAddBPointMap.put(scriptName, new TreeSet<>());
                }
                toAddBPointMap.get(scriptName).addAll(confirmedPointMap.get(scriptName));
            }
            confirmedPointMap.clear();
        }
        for (ConnectionListener l : clisteners) {
            l.disconnected();
        }
        /*for (BreakListener l : breakListeners) {
         l.breakAt();
         }*/
    }

    public synchronized boolean isConnected() {
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
    public void failedListen(IOException ex) {
        View.execInEventDispatchLater(new Runnable() {

            @Override
            public void run() {
                disconnect();
                Main.stopRun();
                Main.stopWork();
                View.showMessageDialog(Main.getMainFrame().getPanel(), AppStrings.translate("error.debug.listen").replace("%port%", "" + Debugger.DEBUG_PORT));
                Main.getMainFrame().getPanel().updateMenu();
            }
        });

    }

    @Override
    public void connected(DebuggerConnection con) {
        clearBreakPoints();

        Main.startWork(AppStrings.translate("work.debugging"), null);

        synchronized (this) {
            paused = false;
        }

        Main.getMainFrame().getPanel().updateMenu();

        enlog(DebuggerConnection.class);
        enlog(DebuggerCommands.class);
        enlog(DebuggerHandler.class);
        try {
            con.getMessage(InVersion.class);
        } catch (IOException ex) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Respon to InProcessTag with OutProcessedTag
        con.addMessageListener(new DebugMessageListener<InProcessTag>() {
            @Override
            public void message(InProcessTag message) {
                try {
                    con.writeMessage(new OutProcessedTag(con));
                } catch (IOException ex) {
                    //disconnect();
                    //ignore
                }
            }
        });

        Map<Integer, String> moduleNames = new HashMap<>();

        try {

            int numScript = con.getMessage(InNumScript.class).num;
            for (int i = 0; i < numScript; i++) {
                InScript sc = con.getMessage(InScript.class);
                moduleNames.put(sc.module, sc.name);
            }

            modulePaths = new HashMap<>();
            classToModule = new HashMap<>();
            //Pattern patMainFrame = Pattern.compile("^Actions for Scene ([0-9]+): Frame ([0-9]+) of Layer Name .*$");
            //Pattern patSymbol = Pattern.compile("^Actions for Symbol ([0-9]+): Frame ([0-9]+) of Layer Name .*$");
            //Pattern patAS2 = Pattern.compile("^([^:]+): .*\\.as$");
            Pattern patAS3 = Pattern.compile("^(.*);(.*);(.*)\\.as$");
            for (int file : moduleNames.keySet()) {
                String name = moduleNames.get(file);
                String[] parts = name.split(";");

                Matcher m;
                /*if ((m = patMainFrame.matcher(name)).matches()) {
                 name = "\\frame_" + m.group(2) + "\\DoAction";
                 } else if ((m = patSymbol.matcher(name)).matches()) {
                 name = "\\DefineSprite(" + m.group(1) + ")\\frame_" + m.group(2) + "\\DoAction";
                 } else if ((m = patAS2.matcher(name)).matches()) {
                 name = "\\_Packages\\" + m.group(1).replace(".", "\\");
                 } else*/
                if ((m = patAS3.matcher(name)).matches()) {
                    String clsName = m.group(3);
                    String pkg = m.group(2).replace("\\", ".");
                    name = DottedChain.parse(pkg).add(clsName).toString();
                }
                modulePaths.put(file, name);
                classToModule.put(name, file);
            }

            //con.getMessage(InSetBreakpoint.class);
            commands = new DebuggerCommands(con);

            commands.stopWarning();
            commands.setStopOnFault();
            commands.setEnumerateOverride();
            commands.setNotifyFailure();
            commands.setInvokeSetters();
            commands.setSwfLoadNotify();
            commands.setGetterTimeout(1500);
            commands.setSetterTimeout(5000);
            /*
             //TODO:
             con.wideLines = commands.getOption("wide_line_player", "false").equals("true");
             if (con.wideLines) {
             commands.setOption("wide_line_debugger", "on");
             }*/
            commands.squelch(true);

            swfs = commands.getSwfInfo(1);
            con.sendMessage(new OutGetSwf(con, 0), InGetSwf.class);
            InGetSwd iswd = con.sendMessage(new OutGetSwd(con, 0), InGetSwd.class);

            boolean isAS3 = (Main.getMainFrame().getPanel().getCurrentSwf().isAS3());

            InSetBreakpoint isb = con.getMessage(InSetBreakpoint.class);
            synchronized (this) {
                for (int i = 0; i < isb.files.size(); i++) {
                    String sname = moduleNames.get(isb.files.get(i));
                    if (!confirmedPointMap.containsKey(sname)) {
                        confirmedPointMap.put(sname, new TreeSet<>());
                    }
                    if (toAddBPointMap.containsKey(sname)) {
                        toAddBPointMap.get(sname).remove(isb.lines.get(i));
                        if (toAddBPointMap.get(sname).isEmpty()) {
                            toAddBPointMap.remove(sname);
                        }
                    }
                    confirmedPointMap.get(sname).add(isb.lines.get(i));
                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} submitted successfully", new Object[]{sname, isb.lines.get(i)});
                }
            }

            synchronized (this) {
                connected = true;
            }
            con.addMessageListener(new DebugMessageListener<InAskBreakpoints>() {

                @Override
                public void message(InAskBreakpoints message) {

                }
            });
            con.addMessageListener(new DebugMessageListener<InContinue>() {
                @Override
                public void message(InContinue msg) {
                    synchronized (DebuggerHandler.this) {
                        paused = false;
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "continued");
                    }
                    for (BreakListener bl : breakListeners) {
                        bl.doContinue();
                    }
                }
            });
            con.addMessageListener(new DebugMessageListener<InBreakAt>() {

                @Override
                public void message(InBreakAt message) {
                    synchronized (DebuggerHandler.this) {
                        paused = true;
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "paused");
                    }

                    String newBreakScriptName = "unknown";
                    if (modulePaths.containsKey(message.file)) {
                        newBreakScriptName = modulePaths.get(message.file);
                    } else {
                        Logger.getLogger(DebuggerCommands.class.getName()).log(Level.SEVERE, "Invalid file: " + message.file);
                        return;
                    }

                    try {
                        breakInfo = con.getMessage(InBreakAtExt.class);
                        breakReason = con.sendMessage(new OutGetBreakReason(con), InBreakReason.class);

                        final String[] reasonNames = new String[]{"unknown", "breakpoint", "watch", "fault", "stopRequest", "step", "halt", "scriptLoaded"};
                        String reason = breakReason.reason < reasonNames.length ? reasonNames[breakReason.reason] : reasonNames[0];

                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "break at {0}:{1}, reason: {2}", new Object[]{newBreakScriptName, message.line, reason});

                        sendBreakPoints(false);
                        synchronized (DebuggerHandler.this) {
                            breakScriptName = newBreakScriptName;
                            breakIp = message.line;
                        }

                        if (breakReason.reason == InBreakReason.REASON_SCRIPT_LOADED) {
                            if (!Configuration.debugHalt.get()) {
                                commands.sendContinue();
                                return;
                            }
                            Main.startWork(AppStrings.translate("work.halted"), null);
                        } else {
                            Main.startWork(AppStrings.translate("work.breakat") + newBreakScriptName + ":" + message.line + " " + AppStrings.translate("debug.break.reason." + reason), null);
                        }
                        frame = commands.getFrame(0);

                        for (BreakListener l : breakListeners) {
                            l.breakAt(newBreakScriptName, message.line);
                        }

                    } catch (IOException ex) {
                        //ignore
                    }

                }
            });

            for (ConnectionListener l : clisteners) {
                l.connected();
            }

            con.addMessageListener(new DebugMessageListener<InTrace>() {

                @Override
                public void message(InTrace tr) {
                    for (TraceListener l : traceListeners) {
                        l.trace(tr.text);
                    }
                }
            });

            if (!isAS3) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINER, "End of connect - sending continue");
                commands.sendContinue();
            }

        } catch (IOException ex) {

            synchronized (this) {
                connected = false;
            }
        }
    }

    private void sendBreakPoints(boolean force) throws IOException {
        if (!force && !isPaused()) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "not sending bps, not paused");
            return;
        }
        synchronized (this) {
            for (String scriptName : toRemoveBPointMap.keySet()) {
                int file = moduleIdOf(scriptName);
                if (file > -1) {
                    for (int line : toRemoveBPointMap.get(scriptName)) {
                        if (isBreakpointConfirmed(scriptName, line)) {
                            commands.removeBreakPoint(file, line);
                            confirmedPointMap.get(scriptName).remove(line);
                            if (confirmedPointMap.get(scriptName).isEmpty()) {
                                confirmedPointMap.remove(scriptName);
                            }
                        }
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} removed", new Object[]{scriptName, line});
                    }
                }
            }
            toRemoveBPointMap.clear();

            for (String scriptName : toAddBPointMap.keySet()) {
                int file = moduleIdOf(scriptName);
                if (file > -1) {
                    for (int line : toAddBPointMap.get(scriptName)) {
                        if (commands.addBreakPoint(file, line)) {
                            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} submitted successfully", new Object[]{scriptName, line});
                            if (!confirmedPointMap.containsKey(scriptName)) {
                                confirmedPointMap.put(scriptName, new TreeSet<>());
                            }
                            confirmedPointMap.get(scriptName).add(line);
                        } else {
                            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} unable to submit", new Object[]{scriptName, line});
                            markBreakPointInvalid(scriptName, line);
                        }
                    }
                }
            }
            toAddBPointMap.clear();
        }
        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "sending bps finished");

    }
}
