/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.debugger.flash.messages.in.InBreakAt;
import com.jpexs.debugger.flash.messages.in.InBreakAtExt;
import com.jpexs.debugger.flash.messages.in.InBreakReason;
import com.jpexs.debugger.flash.messages.in.InCallFunction;
import com.jpexs.debugger.flash.messages.in.InConstantPool;
import com.jpexs.debugger.flash.messages.in.InContinue;
import com.jpexs.debugger.flash.messages.in.InErrorException;
import com.jpexs.debugger.flash.messages.in.InFrame;
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
import com.jpexs.debugger.flash.messages.out.OutPlay;
import com.jpexs.debugger.flash.messages.out.OutProcessedTag;
import com.jpexs.debugger.flash.messages.out.OutRewind;
import com.jpexs.debugger.flash.messages.out.OutSwfInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author JPEXS
 */
public class DebuggerHandler implements DebugConnectionListener {

    private boolean connected = false;

    private DebuggerCommands commands = null;

    //private List<InSwfInfo.SwfInfo> swfs = new ArrayList<>();
    private boolean paused = true;

    private Map<Integer, String> modulePaths = new HashMap<>();

    private Map<Integer, Integer> moduleToSwfIndex = new HashMap<>();

    //Marks swfIndices that are fully loaded - at least one break was on it (including onloaded break)
    private Set<Integer> swfIndicesCommitted = new HashSet<>();

    private Map<Integer, String> swfIndicesNewToSwfHash = new HashMap<>();

    private Map<String, Integer> scriptToModule = new HashMap<>();

    private Map<Integer, Integer> moduleToTraitIndex = new HashMap<>();

    private Map<Integer, Integer> moduleToClassIndex = new HashMap<>();

    private Map<Integer, Integer> moduleToMethodIndex = new HashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> toAddBPointMap = new WeakHashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> confirmedPointMap = new WeakHashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> invalidBreakPointMap = new WeakHashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> toRemoveBPointMap = new WeakHashMap<>();

    private int breakIp = -1;

    private String breakScriptName = null;

    private List<String> stackScriptNames = new ArrayList<>();

    private List<Integer> stackLines = new ArrayList<>();

    private List<SWF> debuggedSwfs = new ArrayList<>();

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

    public void setMainDebuggedSwf(SWF debuggedSwf) {
        debuggedSwfs.clear();
        //debuggedSwfs.add(debuggedSwf);
    }

    public List<SWF> getDebuggedSwfs() {
        return debuggedSwfs;
    }

    public int getBreakIp() {
        if (!isPaused()) {
            return -1;
        }
        return breakIp;
    }

    public int getDepth() {
        return depth;
    }

    public String getBreakScriptName() {
        if (!isPaused()) {
            return "-";
        }
        return breakScriptName;
    }

    public synchronized List<String> getStackScripts() {
        if (!isPaused()) {
            return new ArrayList<>();
        }
        return stackScriptNames;
    }

    public synchronized List<Integer> getStackLines() {
        if (!isPaused()) {
            return new ArrayList<>();
        }
        return stackLines;
    }

    public InGetVariable getVariable(long parentId, String varName, boolean children, boolean useGetter) {
        try {
            return commands.getVariable(parentId, varName, useGetter, children);
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

    public synchronized void removeBreakPoint(SWF swf, String scriptName, int line) {
        if (isBreakpointInvalid(swf, scriptName, line)) {
            invalidBreakPointMap.get(swf).get(scriptName).remove(line);
            if (invalidBreakPointMap.get(swf).get(scriptName).isEmpty()) {
                invalidBreakPointMap.get(swf).remove(scriptName);
            }
            return;
        }
        if (isBreakpointToAdd(swf, scriptName, line)) {
            toAddBPointMap.get(swf).get(scriptName).remove(line);
            if (toAddBPointMap.get(swf).get(scriptName).isEmpty()) {
                toAddBPointMap.get(swf).remove(scriptName);
            }
        } else if (isBreakpointConfirmed(swf, scriptName, line)) {
            if (!toRemoveBPointMap.containsKey(swf)) {
                toRemoveBPointMap.put(swf, new HashMap<>());
            }
            if (!toRemoveBPointMap.get(swf).containsKey(scriptName)) {
                toRemoveBPointMap.get(swf).put(scriptName, new TreeSet<>());
            }
            toRemoveBPointMap.get(swf).get(scriptName).add(line);
        }
        try {
            sendBreakPoints();
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

    public synchronized Set<Integer> getBreakPoints(SWF swf, String scriptName, boolean onlyValid) {
        Set<Integer> lines = new TreeSet<>();
        if (confirmedPointMap.containsKey(swf) && confirmedPointMap.get(swf).containsKey(scriptName)) {
            lines.addAll(confirmedPointMap.get(swf).get(scriptName));
        }
        if (toAddBPointMap.containsKey(swf) && toAddBPointMap.get(swf).containsKey(scriptName)) {
            lines.addAll(toAddBPointMap.get(swf).get(scriptName));
        }
        if (!onlyValid && invalidBreakPointMap.containsKey(swf) && invalidBreakPointMap.get(swf).containsKey(scriptName)) {
            lines.addAll(invalidBreakPointMap.get(swf).get(scriptName));
        }
        return lines;
    }

    public synchronized void clearBreakPoints(SWF swf) {
        Map<String, Set<Integer>> breakpoints = getAllBreakPoints(swf, false);
        for (String scriptName : breakpoints.keySet()) {
            for (int line : breakpoints.get(scriptName)) {
                removeBreakPoint(swf, scriptName, line);
            }
        }
    }

    public synchronized void makeBreakPointsUnconfirmed(SWF swf) {
        if (confirmedPointMap.containsKey(swf)) {
            for (String scriptName : confirmedPointMap.get(swf).keySet()) {
                if (!toAddBPointMap.containsKey(swf)) {
                    toAddBPointMap.put(swf, new HashMap<>());
                }
                if (!toAddBPointMap.get(swf).containsKey(scriptName)) {
                    toAddBPointMap.get(swf).put(scriptName, new TreeSet<>());
                }
                toAddBPointMap.get(swf).get(scriptName).addAll(confirmedPointMap.get(swf).get(scriptName));
            }
            confirmedPointMap.get(swf).clear();
        }
        if (invalidBreakPointMap.containsKey(swf)) {
            for (String scriptName : invalidBreakPointMap.get(swf).keySet()) {
                if (!toAddBPointMap.containsKey(swf)) {
                    toAddBPointMap.put(swf, new HashMap<>());
                }
                if (!toAddBPointMap.get(swf).containsKey(scriptName)) {
                    toAddBPointMap.get(swf).put(scriptName, new TreeSet<>());
                }
                toAddBPointMap.get(swf).get(scriptName).addAll(invalidBreakPointMap.get(swf).get(scriptName));
            }

            invalidBreakPointMap.get(swf).clear();
        }

    }

    public synchronized Map<String, Set<Integer>> getAllBreakPoints(SWF swf, boolean validOnly) {
        Map<String, Set<Integer>> ret = new HashMap<>();
        if (confirmedPointMap.containsKey(swf)) {
            for (String scriptName : confirmedPointMap.get(swf).keySet()) {
                Set<Integer> lines = new TreeSet<>();
                lines.addAll(confirmedPointMap.get(swf).get(scriptName));
                ret.put(scriptName, lines);
            }
        }
        if (toAddBPointMap.containsKey(swf)) {
            for (String scriptName : toAddBPointMap.get(swf).keySet()) {
                if (!ret.containsKey(scriptName)) {
                    ret.put(scriptName, new TreeSet<>());
                }
                ret.get(scriptName).addAll(toAddBPointMap.get(swf).get(scriptName));
            }
        }
        if (!validOnly) {
            if (invalidBreakPointMap.containsKey(swf)) {
                for (String scriptName : invalidBreakPointMap.get(swf).keySet()) {
                    if (!ret.containsKey(scriptName)) {
                        ret.put(scriptName, new TreeSet<>());
                    }
                    ret.get(scriptName).addAll(invalidBreakPointMap.get(swf).get(scriptName));
                }
            }
        }
        return ret;
    }

    public boolean addBreakPoint(SWF swf, String scriptName, int line) {
        synchronized (this) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "adding bp {0}:{1}", new Object[]{scriptName, line});
            if (isBreakpointToRemove(swf, scriptName, line)) {
                toRemoveBPointMap.get(swf).get(scriptName).remove(line);
                if (toRemoveBPointMap.get(swf).get(scriptName).isEmpty()) {
                    toRemoveBPointMap.get(swf).remove(scriptName);
                }
            }

            if (isBreakpointConfirmed(swf, scriptName, line)) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp {0}:{1} already confirmed", new Object[]{scriptName, line});
                return true;

            }
            if (isBreakpointInvalid(swf, scriptName, line)) {
                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp {0}:{1} already invalid", new Object[]{scriptName, line});
                return false;
            }
            if (!toAddBPointMap.containsKey(swf)) {
                toAddBPointMap.put(swf, new HashMap<>());
            }
            if (!toAddBPointMap.get(swf).containsKey(scriptName)) {
                toAddBPointMap.get(swf).put(scriptName, new TreeSet<>());
            }
            toAddBPointMap.get(swf).get(scriptName).add(line);
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "bp {0}:{1} added to todo", new Object[]{scriptName, line});
        }
        try {
            sendBreakPoints();
        } catch (IOException ex) {
            //ignored
        }

        return true;
    }

    public synchronized boolean isBreakpointConfirmed(SWF swf, String scriptName, int line) {
        return confirmedPointMap.containsKey(swf) && confirmedPointMap.get(swf).containsKey(scriptName) && confirmedPointMap.get(swf).get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointToAdd(SWF swf, String scriptName, int line) {
        return toAddBPointMap.containsKey(swf) && toAddBPointMap.get(swf).containsKey(scriptName) && toAddBPointMap.get(swf).get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointToRemove(SWF swf, String scriptName, int line) {
        return toRemoveBPointMap.containsKey(swf) && toRemoveBPointMap.get(swf).containsKey(scriptName) && toRemoveBPointMap.get(swf).get(scriptName).contains(line);
    }

    public synchronized boolean isBreakpointInvalid(SWF swf, String scriptName, int line) {
        return invalidBreakPointMap.containsKey(swf) && invalidBreakPointMap.get(swf).containsKey(scriptName) && invalidBreakPointMap.get(swf).get(scriptName).contains(line);
    }

    private synchronized void markBreakPointInvalid(SWF swf, String scriptName, int line) {
        if (!invalidBreakPointMap.containsKey(swf)) {
            invalidBreakPointMap.put(swf, new HashMap<>());
        }
        if (!invalidBreakPointMap.get(swf).containsKey(scriptName)) {
            invalidBreakPointMap.get(swf).put(scriptName, new TreeSet<>());
        }
        invalidBreakPointMap.get(swf).get(scriptName).add(line);
    }

    private synchronized void markBreakPointConfirmed(SWF swf, String scriptName, int line) {
        if (!confirmedPointMap.containsKey(swf)) {
            confirmedPointMap.put(swf, new HashMap<>());
        }
        if (!confirmedPointMap.get(swf).containsKey(scriptName)) {
            confirmedPointMap.get(swf).put(scriptName, new TreeSet<>());
        }
        confirmedPointMap.get(swf).get(scriptName).add(line);
    }

    private synchronized void markBreakPointValid(SWF swf, String scriptName, int line) {
        if (!invalidBreakPointMap.containsKey(swf)) {
            return;
        }
        if (!invalidBreakPointMap.get(swf).containsKey(scriptName)) {
            return;
        }
        invalidBreakPointMap.get(swf).get(scriptName).remove(line);
        if (invalidBreakPointMap.get(swf).get(scriptName).isEmpty()) {
            invalidBreakPointMap.get(swf).remove(scriptName);
        }
        if (invalidBreakPointMap.get(swf).isEmpty()) {
            invalidBreakPointMap.remove(swf);
        }
    }

    private InFrame frame;

    private int depth;

    private InConstantPool pool;

    private InBreakAtExt breakInfo;

    private InBreakReason breakReason;

    private final List<BreakListener> breakListeners = new CopyOnWriteArrayList<>();

    private final List<TraceListener> traceListeners = new ArrayList<>();

    private final List<ErrorListener> errorListeners = new ArrayList<>();

    private final List<FrameChangeListener> frameChangeListeners = new ArrayList<>();

    private final List<ConnectionListener> clisteners = new ArrayList<>();

    public void setDepth(int depth) {
        this.depth = depth;
        refreshFrame();
    }

    public String moduleToString(int file) {
        if (!modulePaths.containsKey(file)) {
            return "unknown";
        }
        return modulePaths.get(file);
    }

    public Integer moduleToMethodIndex(int file) {
        return moduleToMethodIndex.get(file);
    }

    public Integer moduleToClassIndex(int file) {
        return moduleToClassIndex.get(file);
    }

    public Integer moduleToTraitIndex(int file) {
        return moduleToTraitIndex.get(file);
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

    public static interface ErrorListener {

        public void errorException(String message, Variable thrownVar);
    }

    public static interface FrameChangeListener {

        public void frameChanged();
    }

    public static interface BreakListener {

        public void breakAt(String scriptName, int line, int classIndex, int traitIndex, int methodIndex);

        public void doContinue();
    }

    public void addBreakListener(BreakListener l) {
        breakListeners.add(l);
    }

    public void addFrameChangeListener(FrameChangeListener l) {
        frameChangeListeners.add(l);
    }

    public void removeFrameChangeListener(FrameChangeListener l) {
        frameChangeListeners.remove(l);
    }

    public void addTraceListener(TraceListener l) {
        traceListeners.add(l);
    }

    public void removeTraceListener(TraceListener l) {
        traceListeners.remove(l);
    }

    public void addErrorListener(ErrorListener l) {
        errorListeners.add(l);
    }

    public void removeErrorListener(ErrorListener l) {
        errorListeners.remove(l);
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
            frame = commands.getFrame(depth);
            pool = commands.getConstantPool(0);
        } catch (IOException ex) {
            //ignore
        }
        for (FrameChangeListener l : frameChangeListeners) {
            l.frameChanged();
        }
    }

    public synchronized InFrame getFrame() {
        if (!paused) {
            return null;
        }
        return frame;
    }

    public synchronized int moduleIdOf(String packWithHash) {
        if (!scriptToModule.containsKey(packWithHash)) {
            return -1;
        }
        return scriptToModule.get(packWithHash);
    }

    public boolean isPaused() {
        if (!isConnected()) {
            return false;
        }
        synchronized (this) {
            return paused;
        }
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
            for (SWF debuggedSwf : debuggedSwfs) {
                if (confirmedPointMap.containsKey(debuggedSwf)) {
                    for (String scriptName : confirmedPointMap.get(debuggedSwf).keySet()) {
                        if (!toAddBPointMap.containsKey(debuggedSwf)) {
                            toAddBPointMap.put(debuggedSwf, new HashMap<>());
                        }
                        if (!toAddBPointMap.get(debuggedSwf).containsKey(scriptName)) {
                            toAddBPointMap.get(debuggedSwf).put(scriptName, new TreeSet<>());
                        }
                        toAddBPointMap.get(debuggedSwf).get(scriptName).addAll(confirmedPointMap.get(debuggedSwf).get(scriptName));
                    }
                    confirmedPointMap.get(debuggedSwf).clear();
                }

                if (invalidBreakPointMap.containsKey(debuggedSwf)) {
                    for (String scriptName : invalidBreakPointMap.get(debuggedSwf).keySet()) {
                        if (!toAddBPointMap.containsKey(debuggedSwf)) {
                            toAddBPointMap.put(debuggedSwf, new HashMap<>());
                        }
                        if (!toAddBPointMap.get(debuggedSwf).containsKey(scriptName)) {
                            toAddBPointMap.get(debuggedSwf).put(scriptName, new TreeSet<>());
                        }
                        toAddBPointMap.get(debuggedSwf).get(scriptName).addAll(invalidBreakPointMap.get(debuggedSwf).get(scriptName));
                    }
                    invalidBreakPointMap.get(debuggedSwf).clear();
                }
            }
        }
        for (ConnectionListener l : clisteners) {
            l.disconnected();
        }
        debuggedSwfs.clear();
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

    @Override
    public void failedListen(IOException ex) {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                disconnect();
                Main.stopRun();
                Main.stopWork();
                ViewMessages.showMessageDialog(Main.getMainFrame().getPanel(), AppStrings.translate("error.debug.listen").replace("%port%", "" + Debugger.DEBUG_PORT));
                Main.getMainFrame().getPanel().updateMenu();
            }
        });

    }

    @Override
    public void connected(DebuggerConnection con) {
        /*for (SWF debuggedSwf : debuggedSwfs) {
            makeBreakPointsUnconfirmed(debuggedSwf);
        }*/

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

        //Respond to InProcessTag with OutProcessedTag
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

        swfIndicesCommitted.clear();
        swfIndicesNewToSwfHash.clear();

        Map<Integer, String> moduleNames = new HashMap<>();

        final Pattern patAS3 = Pattern.compile("^(.*);(.*);(.*)\\.as$");
        final Pattern patAS3PCode = Pattern.compile("^(?<hash>[0-9a-z_]+):#PCODE abc:(?<abc>[0-9]+),script:(?<script>[0-9]+),class:(?<class>-?[0-9]+),trait:(?<trait>-?[0-9]+),method:(?<method>[0-9]+),body:(?<body>[0-9]+);(.*)$");

        boolean isAS3 = Main.getRunningSWF().isAS3();
        try {

            con.addMessageListener(new DebugMessageListener<InErrorException>() {
                @Override
                public void message(InErrorException t) {
                    for (ErrorListener l : errorListeners) {
                        l.errorException(t.exceptionMessage, t.thrownVar);
                    }
                    con.dropMessage(t);
                }
            });

            con.addMessageListener(new DebugMessageListener<InNumScript>() {
                @Override
                public void message(InNumScript t) {
                    con.dropMessage(t);
                }
            });

            modulePaths.clear();
            scriptToModule.clear();
            moduleNames.clear();
            moduleToClassIndex.clear();
            moduleToTraitIndex.clear();
            moduleToMethodIndex.clear();
            moduleToSwfIndex.clear();

            con.addMessageListener(new DebugMessageListener<InScript>() {
                @Override
                public void message(InScript sc) {
                    synchronized (DebuggerHandler.this) {
                        moduleNames.put(sc.module, sc.name);
                        int file = sc.module;
                        String name = sc.name;
                        int swfIndex = sc.swfIndex;

                        name = name.replaceAll("\\[(invalid_utf8=[0-9]+)\\]", "{$1}");

                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "Received script added - index {0} name: {1}", new Object[]{file, name});

                        String swfHash = "main";
                        Matcher m;
                        if ((m = patAS3.matcher(name)).matches()) {
                            String clsNameWithSuffix = m.group(3).replace("{{semicolon}}", ";");
                            String pkg = m.group(2).replace("{{semicolon}}", ";").replace("\\", ".");
                            m = patAS3PCode.matcher(name);
                            if (m.matches()) {
                                moduleToClassIndex.put(file, Integer.parseInt(m.group("class")));
                                moduleToTraitIndex.put(file, Integer.parseInt(m.group("trait")));
                                moduleToMethodIndex.put(file, Integer.parseInt(m.group("method")));
                                name = DottedChain.parseWithSuffix(pkg).addWithSuffix(clsNameWithSuffix).toString();
                                swfHash = m.group("hash");
                                name = swfHash + ":" + "#PCODE abc:" + m.group("abc") + ",body:" + m.group("body") + ";" + name;
                            } else {
                                if (pkg.contains(":")) {
                                    swfHash = pkg.substring(0, pkg.indexOf(":"));
                                    pkg = pkg.substring(pkg.indexOf(":") + 1);
                                }

                                name = swfHash + ":" + DottedChain.parseWithSuffix(pkg).addWithSuffix(clsNameWithSuffix).toPrintableString(con.isAS3);
                            }
                        } else {
                            if (name.contains(":")) {
                                swfHash = name.substring(0, name.indexOf(":"));
                            }
                        }
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "Script added - index {0} name: {1}", new Object[]{file, name});
                        modulePaths.put(file, name);
                        scriptToModule.put(name, file);
                        moduleToSwfIndex.put(file, swfIndex);
                        if (swfIndex > debuggedSwfs.size() - 1) {
                            SWF swf = Main.getSwfByHash(swfHash);
                            if (swf == null) {
                                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.WARNING, "SWF with hash {0} not found", swfHash);
                            } else {
                                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "adding {0} to debugSwfs", swfIndex);
                                swfIndicesCommitted.add(swfIndex);
                                debuggedSwfs.add(swf);
                            }
                        } else {
                            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "Swf index already committed");
                        }
                    }
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
                    /*for (InSwfInfo.SwfInfo s : t.swfInfos) {
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
                    }*/
                    con.dropMessage(t);
                }
            });

            InSetBreakpoint isb = con.getMessage(InSetBreakpoint.class);
            synchronized (this) {
                for (int i = 0; i < isb.files.size(); i++) {
                    String sname = moduleNames.get(isb.files.get(i));
                    SWF debuggedSwf = debuggedSwfs.get(moduleToSwfIndex.get(isb.files.get(i)));
                    if (!confirmedPointMap.containsKey(debuggedSwf)) {
                        confirmedPointMap.put(debuggedSwf, new HashMap<>());
                    }
                    if (!confirmedPointMap.get(debuggedSwf).containsKey(sname)) {
                        confirmedPointMap.get(debuggedSwf).put(sname, new TreeSet<>());
                    }
                    if (toAddBPointMap.containsKey(debuggedSwf)) {
                        if (toAddBPointMap.get(debuggedSwf).containsKey(sname)) {
                            toAddBPointMap.get(debuggedSwf).get(sname).remove(isb.lines.get(i));
                            if (toAddBPointMap.get(debuggedSwf).get(sname).isEmpty()) {
                                toAddBPointMap.get(debuggedSwf).remove(sname);
                            }
                        }
                    }
                    confirmedPointMap.get(debuggedSwf).get(sname).add(isb.lines.get(i));
                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} submitted successfully", new Object[]{sname, isb.lines.get(i)});
                }
            }

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
                        //ignore single InSetBreakpoint, otherwise setting breakpoints later won't work
                        con.getMessage(InSetBreakpoint.class, DebuggerConnection.PREF_RESPONSE_TIMEOUT);

                        breakInfo = con.getMessage(InBreakAtExt.class, DebuggerConnection.PREF_RESPONSE_TIMEOUT);
                        breakReason = con.sendMessageWithTimeout(new OutGetBreakReason(con), InBreakReason.class);

                        int reasonInt = breakReason == null ? 0 : breakReason.reason;

                        String newBreakScriptName = "unknown";
                        String userBreakScriptName = "unknown";
                        if (modulePaths.containsKey(message.file)) {
                            newBreakScriptName = modulePaths.get(message.file);
                            userBreakScriptName = newBreakScriptName;
                            if (newBreakScriptName.contains(":")) {
                                String swfHash = newBreakScriptName.substring(0, newBreakScriptName.indexOf(":"));
                                SWF swf = Main.getSwfByHash(swfHash);
                                userBreakScriptName = swf.toString() + ": " + newBreakScriptName.substring(newBreakScriptName.indexOf(":") + 1);
                            }
                        } else if (reasonInt != InBreakReason.REASON_SCRIPT_LOADED) {
                            Logger.getLogger(DebuggerCommands.class.getName()).log(Level.SEVERE, "Invalid file: {0}", message.file);
                            //return;
                        }

                        final String[] reasonNames = new String[]{"unknown", "breakpoint", "watch", "fault", "stopRequest", "step", "halt", "scriptLoaded"};
                        String reason = reasonInt < reasonNames.length ? reasonNames[reasonInt] : reasonNames[0];

                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "break at {0}:{1}, reason: {2}", new Object[]{newBreakScriptName, message.line, reason});

                        try {
                            sendBreakPoints();
                        } catch (IOException ex) {
                            //ignore
                            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINE, "send breakpoints exception: " + ex.getMessage());
                        }

                        synchronized (DebuggerHandler.this) {
                            breakScriptName = newBreakScriptName;
                            breakIp = message.line;

                            if (breakInfo != null) {
                                List<String> files = new ArrayList<>();
                                for (int i = 0; i < breakInfo.files.size(); i++) {
                                    files.add(Main.getDebugHandler().moduleToString(breakInfo.files.get(i)));
                                }
                                stackScriptNames = files;
                                List<Integer> lines = new ArrayList<>(breakInfo.lines);
                                stackLines = lines;
                            }
                        }

                        if (reasonInt == InBreakReason.REASON_SCRIPT_LOADED) {
                            if (!Configuration.debugHalt.get()) {
                                commands.sendContinue();
                                return;
                            }
                            Main.startWork(AppStrings.translate("work.halted"), null);
                        } else {
                            Main.startWork(AppStrings.translate("work.breakat") + userBreakScriptName + ":" + message.line + " " + AppStrings.translate("debug.break.reason." + reason), null);
                        }
                        depth = 0;
                        refreshFrame();
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

            synchronized (this) {
                connected = true;
            }
            /*if (!isAS3) {
                try {
                    commands.getConnection().writeMessage(new OutRewind(commands.getConnection()));
                } catch (IOException ex) {
                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/

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

    private void sendBreakPoints() throws IOException {
        if (!isConnected()) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "not sending bps, not connected");
            return;
        }
        synchronized (this) {
            Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "sending breakpoints of {0} swfs", debuggedSwfs.size());
            for (int swfIndex = 0; swfIndex < debuggedSwfs.size(); swfIndex++) {
                if (!swfIndicesCommitted.contains(swfIndex)) {
                    continue;
                }
                SWF debuggedSwf = debuggedSwfs.get(swfIndex);
                String hash = Main.getSwfHash(debuggedSwf);

                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "sending breakpoints of ", hash);

                if (toRemoveBPointMap.containsKey(debuggedSwf)) {
                    for (String scriptName : toRemoveBPointMap.get(debuggedSwf).keySet()) {
                        if (scriptName.startsWith("#PCODE") != Main.isDebugPCode()) {
                            continue;
                        }
                        int file = moduleIdOf(hash + ":" + scriptName);
                        if (file > -1) {
                            for (int line : toRemoveBPointMap.get(debuggedSwf).get(scriptName)) {
                                if (isBreakpointConfirmed(debuggedSwf, scriptName, line)) {
                                    commands.removeBreakPoint(file, line);
                                    confirmedPointMap.get(debuggedSwf).get(scriptName).remove(line);
                                    if (confirmedPointMap.get(debuggedSwf).get(scriptName).isEmpty()) {
                                        confirmedPointMap.get(debuggedSwf).remove(scriptName);
                                    }
                                }
                                Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} removed", new Object[]{scriptName, line});
                            }
                        }
                    }
                    toRemoveBPointMap.get(debuggedSwf).clear();
                    if (toRemoveBPointMap.get(debuggedSwf).isEmpty()) {
                        toRemoveBPointMap.remove(debuggedSwf);
                    }
                }

                if (toAddBPointMap.containsKey(debuggedSwf)) {
                    Set<String> toAddScripts = new HashSet<>(toAddBPointMap.get(debuggedSwf).keySet());
                    for (String scriptName : toAddScripts) {
                        if (scriptName.startsWith("#PCODE") != Main.isDebugPCode()) {
                            continue;
                        }
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "searching for module of {0}:{1}", new Object[]{hash, scriptName});
                        int file = moduleIdOf(hash + ":" + scriptName);
                        Logger.getLogger(DebuggerHandler.class.getName()).log(Level.FINEST, "module = {0}", file);
                        if (file > -1) {
                            Set<Integer> lines = new HashSet<>(toAddBPointMap.get(debuggedSwf).get(scriptName));
                            for (int line : lines) {
                                if (commands.addBreakPoint(file, line)) {
                                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} submitted successfully", new Object[]{scriptName, line});
                                    markBreakPointConfirmed(debuggedSwf, scriptName, line);
                                    markBreakPointValid(debuggedSwf, scriptName, line);
                                    toAddBPointMap.get(debuggedSwf).get(scriptName).remove(line);
                                    if (toAddBPointMap.get(debuggedSwf).get(scriptName).isEmpty()) {
                                        toAddBPointMap.get(debuggedSwf).remove(scriptName);
                                    }
                                    if (toAddBPointMap.get(debuggedSwf).isEmpty()) {
                                        toAddBPointMap.remove(debuggedSwf);
                                    }
                                } else {
                                    Logger.getLogger(DebuggerHandler.class.getName()).log(Level.INFO, "Breakpoint {0}:{1} unable to submit", new Object[]{scriptName, line});
                                    markBreakPointInvalid(debuggedSwf, scriptName, line);
                                }
                            }
                        } else {
                            for (int line : toAddBPointMap.get(debuggedSwf).get(scriptName)) {
                                markBreakPointInvalid(debuggedSwf, scriptName, line);
                            }
                        }
                    }
                }
            }
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
        InGetVariable igv = getVariable(0, object, false, false);
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
            //ignored
        }
        return null;
    }
}
