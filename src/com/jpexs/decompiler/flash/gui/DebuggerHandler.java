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
import com.jpexs.debugger.flash.messages.in.InAskBreakpoints;
import com.jpexs.debugger.flash.messages.in.InBreakAt;
import com.jpexs.debugger.flash.messages.in.InContinue;
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSetBreakpoint;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.graph.DottedChain;
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
        connected = false;
        if (commands != null) {
            commands.disconnect();
        }
        commands = null;
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

    @Override
    public void connected(DebuggerConnection con) {

        synchronized (DebuggerHandler.this) {
            paused = true;
        }

        Main.getMainFrame().getPanel().updateMenu();

        Level level = Level.FINER;

        Logger rootLog = Logger.getLogger(Debugger.class.getName());
        rootLog.setLevel(level);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(level);
        rootLog.addHandler(ch);
        //rootLog.getHandlers()[0].setLevel(level);

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
                    Main.getMainFrame().getPanel().updateMenu();
                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "break at {0}:{1}", new Object[]{moduleNames.get(message.file), message.line});
                    if (!modulePaths.containsKey(message.file)) {
                        return;
                    }
                    String cls = modulePaths.get(message.file).toString();
                    Main.getMainFrame().getPanel().debuggerBreakAt(Main.getMainFrame().getPanel().getCurrentSwf(), cls, message.line);
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
            Main.getMainFrame().getPanel().refreshBreakPoints();
            connected = true;
        } catch (IOException ex) {
            connected = false;
        }
    }
}
