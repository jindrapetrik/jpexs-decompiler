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
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jindra
 */
public class DebuggerHandler implements DebugConnectionListener {

    @Override
    public void connected(DebuggerConnection con) {

        Level level = Level.FINER;

        Logger rootLog = Logger.getLogger(Debugger.class.getName());
        rootLog.setLevel(level);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(level);
        rootLog.addHandler(ch);
        //rootLog.getHandlers()[0].setLevel(level);

        final DebuggerCommands dc = new DebuggerCommands(con);
        dc.stopWarning();
        dc.setStopOnFault();
        dc.setEnumerateOverride();
        dc.setNotifyFailure();
        dc.setInvokeSetters();
        dc.setSwfLoadNotify();
        dc.setGetterTimeout(1500);
        dc.setSetterTimeout(5000);
        dc.squelch(true);
        List<InSwfInfo.SwfInfo> swfs = dc.getSwfInfo(1);
        int numScript = con.getMessage(InNumScript.class).num;
        final Map<Integer, String> moduleNames = new HashMap<>();
        for (int i = 0; i < numScript; i++) {
            InScript sc = con.getMessage(InScript.class);
            System.out.println("" + sc.module + ":" + sc.name);
            moduleNames.put(sc.module, sc.name);
        }

        final Map<Integer, ClassPath> modulePaths = new HashMap<>();

        for (int mname : moduleNames.keySet()) {
            String name = moduleNames.get(mname);
            String[] parts = name.split(";");

            if (parts.length == 3) {
                String clsName = parts[2].replace(".as", "");
                String pkg = parts[1];
                modulePaths.put(mname, new ClassPath(DottedChain.parse(pkg), clsName));
            }
        }

        con.getMessage(InAskBreakpoints.class);
        //dc.addBreakPoint(15, 14);
        dc.addBreakPoint(9, 26);
        con.addMessageListener(new DebugMessageListener<InBreakAt>() {

            @Override
            public void message(InBreakAt message) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.WARNING, "break at {0}:{1}", new Object[]{moduleNames.get(message.file), message.line});
                String cls = modulePaths.get(message.file).toString();
                Main.getMainFrame().getPanel().debuggerBreakAt(Main.getMainFrame().getPanel().getCurrentSwf(), cls, message.line);
                //dc.sendContinue();
            }
        });
        dc.sendContinue();
    }
}
