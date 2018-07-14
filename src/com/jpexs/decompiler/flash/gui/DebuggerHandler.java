/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.debugger.flash.DebugConnectionListener;
import com.jpexs.debugger.flash.DebugMessageListener;
import com.jpexs.debugger.flash.Debugger;
import com.jpexs.debugger.flash.DebuggerCommands;
import com.jpexs.debugger.flash.DebuggerConnection;
import com.jpexs.debugger.flash.Variable;
import com.jpexs.debugger.flash.VariableFlags;
import com.jpexs.debugger.flash.VariableType;
import com.jpexs.debugger.flash.messages.in.InAskBreakpoints;
import com.jpexs.debugger.flash.messages.in.InBreakAt;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InBreakReason;
import com.jpexs.debugger.flash.messages.in.InCallFunction;
import com.jpexs.debugger.flash.messages.in.InConstantPool;
import com.jpexs.debugger.flash.messages.in.InContinue;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.debugger.flash.messages.in.InGetSwd;
import com.jpexs.debugger.flash.messages.in.InGetSwf;
import com.jpexs.debugger.flash.messages.in.InGetVariable;
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InProcessTag;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSetBreakpoint;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.debugger.flash.messages.in.InTrace;
import com.jpexs.debugger.flash.messages.in.InVersion;
import com.jpexs.debugger.flash.messages.out.OutAddWatch2;
import com.jpexs.debugger.flash.messages.out.OutGetBreakReason;
import com.jpexs.debugger.flash.messages.out.OutGetSwd;
import com.jpexs.debugger.flash.messages.out.OutGetSwf;
import com.jpexs.debugger.flash.messages.out.OutPlay;
import com.jpexs.debugger.flash.messages.out.OutProcessedTag;
import com.jpexs.debugger.flash.messages.out.OutRewind;
import com.jpexs.debugger.flash.messages.out.OutSwfInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
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

    private Map<Integer, Integer> moduleToSwfIndex = new HashMap<>();

    private Map<String, Integer> scriptToModule = new HashMap<>();

    private Map<Integer, Integer> moduleToTraitIndex = new HashMap<>();

    private Map<Integer, Integer> moduleToClassIndex = new HashMap<>();

    private Map<Integer, Integer> moduleToMethodIndex = new HashMap<>();

    private Map<String, Set<Integer>> toAddBPointMap = new HashMap<>();

    private Map<String, Set<Integer>> confirmedPointMap = new HashMap<>();

    private Map<String, Set<Integer>> invalidBreakPointMap = new HashMap<>();

    private Map<String, Set<Integer>> toRemoveBPointMap = new HashMap<>();

    private int breakIp = -1;

    private String breakScriptName = null;

    public static class ActionScriptException extends Exception {

        private String errorClass;

        public ActionScriptException(String errorClass, String message) {
            super(message);
            this.errorClass = errorClass;
        }

        public ActionScriptException(String errorClsMessage) {
            this(errorClsMessage.substring(0, errorClsMessage.indexOf(": ")), errorClsMessage.substring(errorClsMessage.indexOf(": ") + 2));
        }

        public String getErrorClass() {
            return errorClass;
        }
    }

    public int getBreakIp() {
        if (!isPaused()) {
            return -1;
        }
        return breakIp;
    }

    public String getBreakScriptName() {
        if (!isPaused()) {
            return "-";
        }
        return breakScriptName;
    }

    public InGetVariable getVariable(long parentId, String varName, boolean children) {
        try {
            return commands.getVariable(parentId, varName, true, children);
        } catch (IOException ex) {
            return null;
        }
    }

    public void setVariable(long parentId, String varName, int valueType, Object value) {
        try {
            String svalue = "";
            switch (valueType) {
                case VariableType.STRING:
                    svalue = "" + value;
                    break;
                case VariableType.NUMBER:
                    svalue = "" + value;
                    break;
                case VariableType.BOOLEAN:
                    svalue = ((Boolean) value) ? "true" : "false";
                    break;
                case VariableType.UNDEFINED:
                    svalue = "undefined";
                    break;
                case VariableType.NULL:
                    svalue = "undefined";
                    break;
            }
            commands.setVariable(parentId, varName, valueType, svalue);
        } catch (IOException ex) {
            //ignore
        }
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

    private int watchTag = 1;

    public synchronized com.jpexs.debugger.flash.DebuggerCommands.Watch addWatch(Variable v, long v_id, boolean watchRead, boolean watchWrite) {
        int tag = watchTag++;
        try {
            return commands.addWatch(v_id, v.name, (watchRead ? OutAddWatch2.FLAG_READ : 0) | (watchWrite ? OutAddWatch2.FLAG_WRITE : 0), tag);
        } catch (IOException ex) {
            return null;
        }
    }

    public synchronized Set<Integer> getBreakPoints(String scriptName, boolean onlyValid) {
        Set<Integer> lines = new TreeSet<>();
        if (confirmedPointMap.containsKey(scriptName)) {
            lines.addAll(confirmedPointMap.get(scriptName));
        }
        if (toAddBPointMap.containsKey(scriptName)) {
            lines.addAll(toAddBPointMap.get(scriptName));
        }
        if (!onlyValid && invalidBreakPointMap.containsKey(scriptName)) {
            lines.addAll(invalidBreakPointMap.get(scriptName));
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
        for (String scriptName : invalidBreakPointMap.keySet()) {
            if (!toAddBPointMap.containsKey(scriptName)) {
                toAddBPointMap.put(scriptName, new TreeSet<>());
            }
            toAddBPointMap.get(scriptName).addAll(invalidBreakPointMap.get(scriptName));
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
            Logger.getLogger(DebuggerHandler.class
                    .getName()).log(Level.FINE, "adding bp " + scriptName + ":" + line);
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
            Logger
                    .getLogger(DebuggerHandler.class
                            .getName()).log(Level.FINE, "bp " + scriptName + ":" + line + " added to todo");
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

    private InConstantPool pool;

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

        public void breakAt(String scriptName, int line, int classIndex, int traitIndex, int methodIndex);

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

    public synchronized void refreshFrame() {
        if (!paused) {
            return;
        }
        try {
            frame = commands.getFrame(0);
            pool = commands.getConstantPool(0);
        } catch (IOException ex) {
            //ignore
        }
    }

    public synchronized InFrame getFrame() {
        if (!paused) {
            return null;
        }
        return frame;
    }

    public synchronized int moduleIdOf(String pack) {
        if (scriptToModule.containsKey(pack)) {
            return scriptToModule.get(pack);
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
        pool = null;
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
            for (String scriptName : invalidBreakPointMap.keySet()) {
                if (!toAddBPointMap.containsKey(scriptName)) {
                    toAddBPointMap.put(scriptName, new TreeSet<>());
                }
                toAddBPointMap.get(scriptName).addAll(invalidBreakPointMap.get(scriptName));
            }
            invalidBreakPointMap.clear();
        }
        for (ConnectionListener l : clisteners) {
            l.disconnected();
        }
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
        View.execInEventDispatch(new Runnable() {
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

        //enlog(DebuggerConnection.class);
        //enlog(DebuggerCommands.class);
        //enlog(DebuggerHandler.class);
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

        swfs.clear();

        Map<Integer, String> moduleNames = new HashMap<>();

        final Pattern patAS3 = Pattern.compile("^(.*);(.*);(.*)\\.as$");
        final Pattern patAS3PCode = Pattern.compile("^#PCODE abc:([0-9]+),script:([0-9]+),class:(-?[0-9]+),trait:(-?[0-9]+),method:([0-9]+),body:([0-9]+);(.*)$");

        try {

            con.addMessageListener(new DebugMessageListener<InNumScript>() {
                @Override
                public void message(InNumScript t) {
                    con.dropMessage(t);
                }
            });

            modulePaths = new HashMap<>();
            scriptToModule = new HashMap<>();

            con.addMessageListener(new DebugMessageListener<InScript>() {
                @Override
                public void message(InScript sc) {
                    moduleNames.put(sc.module, sc.name);
                    moduleToSwfIndex.put(sc.module, sc.swfIndex);
                    int file = sc.module;
                    String name = sc.name;
                    String[] parts = name.split(";");

                    Matcher m;
                    if ((m = patAS3.matcher(name)).matches()) {
                        String clsNameWithSuffix = m.group(3);
                        String pkg = m.group(2).replace("\\", ".");
                        m = patAS3PCode.matcher(name);

                        if (m.matches()) {
                            moduleToClassIndex.put(file, Integer.parseInt(m.group(3)));
                            moduleToTraitIndex.put(file, Integer.parseInt(m.group(4)));
                            moduleToMethodIndex.put(file, Integer.parseInt(m.group(5)));
                            name = DottedChain.parseWithSuffix(pkg).addWithSuffix(clsNameWithSuffix).toString();
                            name = "#PCODE abc:" + m.group(1) + ",body:" + m.group(6) + ";" + name;
                        } else {
                            name = DottedChain.parseWithSuffix(pkg).addWithSuffix(clsNameWithSuffix).toString();
                        }
                    }
                    modulePaths.put(file, name);
                    scriptToModule.put(name, file);
                    con.dropMessage(sc);
                }
            });

            /*int numScript = con.getMessage(InNumScript.class).num;
             for (int i = 0; i < numScript; i++) {
             InScript sc = con.getMessage(InScript.class);
             moduleNames.put(sc.module, sc.name);
             }*/
            //Pattern patMainFrame = Pattern.compile("^Actions for Scene ([0-9]+): Frame ([0-9]+) of Layer Name .*$");
            //Pattern patSymbol = Pattern.compile("^Actions for Symbol ([0-9]+): Frame ([0-9]+) of Layer Name .*$");
            //Pattern patAS2 = Pattern.compile("^([^:]+): .*\\.as$");
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

            boolean isAS3 = (Main.getMainFrame().getPanel().getCurrentSwf().isAS3());
            con.isAS3 = isAS3;

            //Widelines - only AS3, it hangs in AS1/2 and SWD does not support UI32 lines
            if (isAS3) {
                con.wideLines = commands.getOption("wide_line_player", "false").equals("true");
                if (con.wideLines) {
                    commands.setOption("wide_line_debugger", "on");
                }
            }
            commands.squelch(true);

            con.writeMessage(new OutSwfInfo(con, 0));
            con.addMessageListener(new DebugMessageListener<InSwfInfo>() {
                @Override
                public void message(InSwfInfo t) {
                    for (InSwfInfo.SwfInfo s : t.swfInfos) {
                        swfs.add(s);
                        View.execInEventDispatch(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    con.sendMessageWithTimeout(new OutGetSwf(con, (int) s.index), InGetSwf.class);
                                    con.sendMessageWithTimeout(new OutGetSwd(con, (int) s.index), InGetSwd.class);
                                } catch (IOException ex) {
                                    //ignore
                                }
                            }
                        });
                    }
                    con.dropMessage(t);
                }
            });

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

                    try {
                        breakInfo = con.getMessage(InBreakAtExt.class, DebuggerConnection.PREF_RESPONSE_TIMEOUT);
                        breakReason = con.sendMessageWithTimeout(new OutGetBreakReason(con), InBreakReason.class);

                        String newBreakScriptName = "unknown";
                        if (modulePaths.containsKey(message.file)) {
                            newBreakScriptName = modulePaths.get(message.file);

                        } else if (breakReason.reason != InBreakReason.REASON_SCRIPT_LOADED) {
                            Logger.getLogger(DebuggerCommands.class.getName()).log(Level.SEVERE, "Invalid file: {0}", message.file);
                            return;
                        }

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
                        pool = commands.getConstantPool(0);

                        for (BreakListener l : breakListeners) {
                            l.breakAt(newBreakScriptName, message.line,
                                    moduleToClassIndex.containsKey(message.file) ? moduleToClassIndex.get(message.file) : -1,
                                    moduleToTraitIndex.containsKey(message.file) ? moduleToTraitIndex.get(message.file) : -1,
                                    moduleToMethodIndex.containsKey(message.file) ? moduleToMethodIndex.get(message.file) : -1
                            );
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
                con.writeMessage(new OutRewind(con));
                con.writeMessage(new OutPlay(con));
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
                } else {
                    for (int line : toAddBPointMap.get(scriptName)) {
                        markBreakPointInvalid(scriptName, line);
                    }
                }
            }
            toAddBPointMap.clear();

        }
        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "sending bps finished");

    }

    public synchronized InConstantPool getConstantPool() {
        if (!paused) {
            return null;
        }
        return pool;
    }

    public synchronized InCallFunction callMethod(Variable object, String methodName, List<Object> args) throws ActionScriptException {
        return callFunction(false, methodName, object, args);
    }

    public synchronized InCallFunction callMethod(String object, String methodName, List<Object> args) throws ActionScriptException {
        InGetVariable igv = getVariable(0, object, false);
        return callMethod(igv.parent, methodName, args);
    }

    private static String typeAsStr(Object value) {
        if (value == null) {
            return "null";
        }
        if ((value instanceof Long) || (value instanceof Integer)) {
            return "int";
        }
        if (value instanceof Number) {
            return "Number";
        }
        if (value instanceof String) {
            return "String";
        }
        if (value instanceof Variable) {
            Variable v = (Variable) value;
            return v.getTypeAsStr();
        }
        return "String";
    }

    private static String valueAsStr(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Double) {
            double doubleValue = ((Double) value).doubleValue();
            long longValue = (long) doubleValue;
            if (doubleValue == longValue) {
                return Long.toString(longValue);
            }
        }

        if (value instanceof Variable) {
            Variable v = (Variable) value;
            return valueAsStr(v.value);
        }
        return "" + value;
    }

    public synchronized InCallFunction callFunction(boolean isConstructor, String funcName, Variable thisValue, List<Object> args) throws ActionScriptException {
        List<String> argTypes = new ArrayList<>();
        List<String> argValues = new ArrayList<>();
        for (Object value : args) {
            argTypes.add(typeAsStr(value));
            argValues.add(valueAsStr(value));
        }
        String thisType = typeAsStr(thisValue);
        String thisValueStr = valueAsStr(thisValue);
        try {
            InCallFunction icf = commands.callFunction(isConstructor, funcName, thisType, thisValueStr, argTypes, argValues);
            if (!icf.variables.isEmpty()) {
                if ((icf.variables.get(0).flags & VariableFlags.IS_EXCEPTION) > 0) {
                    throw new ActionScriptException("" + icf.variables.get(0).value);
                }
            }
            return icf;
        } catch (IOException e) {

        }
        return null;
    }
}
