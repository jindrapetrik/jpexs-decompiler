/*
 * Copyright (C) 2026 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.debugger.flash.DebugMessageListener;
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
import com.jpexs.debugger.flash.messages.in.InExit;
import com.jpexs.debugger.flash.messages.in.InFrame;
import com.jpexs.debugger.flash.messages.in.InGetSwf;
import com.jpexs.debugger.flash.messages.in.InGetVariable;
import com.jpexs.debugger.flash.messages.in.InNumScript;
import com.jpexs.debugger.flash.messages.in.InPlaceObject;
import com.jpexs.debugger.flash.messages.in.InProcessTag;
import com.jpexs.debugger.flash.messages.in.InScript;
import com.jpexs.debugger.flash.messages.in.InSetBreakpoint;
import com.jpexs.debugger.flash.messages.in.InSwfInfo;
import com.jpexs.debugger.flash.messages.in.InTrace;
import com.jpexs.debugger.flash.messages.in.InVersion;
import com.jpexs.debugger.flash.messages.out.OutAddWatch2;
import com.jpexs.debugger.flash.messages.out.OutGetBreakReason;
import com.jpexs.debugger.flash.messages.out.OutGetSwf;
import com.jpexs.debugger.flash.messages.out.OutPlay;
import com.jpexs.debugger.flash.messages.out.OutProcessedTag;
import com.jpexs.debugger.flash.messages.out.OutRewind;
import com.jpexs.debugger.flash.messages.out.OutSwfInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class DebuggerSession {

    private boolean connected = false;

    private DebuggerCommands commands = null;

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
    private Map<SWF, Map<String, Set<Integer>>> toRemoveBPointMap = new WeakHashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> confirmedPointMap = new WeakHashMap<>();

    private Map<SWF, Map<String, Set<Integer>>> invalidBreakPointMap = new WeakHashMap<>();

    private int breakIp = -1;

    private String breakScriptName = null;

    private List<String> stackScriptNames = new ArrayList<>();

    private List<Integer> stackLines = new ArrayList<>();

    private Map<Integer, SWF> debuggedSwfs = new LinkedHashMap<>();

    private Map<String, Long> placedObjects = new LinkedHashMap<>();

    private InFrame frame;

    private int depth;

    private InConstantPool pool;

    private InBreakAtExt breakInfo;

    private InBreakReason breakReason;

    private DebuggerHandler handler;

    private InSetBreakpoint inSetBreakpoint;

    private int id;
    
    private String title = "";

    
    private List<Thread> getSwfThreadList = Collections.synchronizedList(new ArrayList<>());

    public DebuggerSession(DebuggerHandler handler, DebuggerConnection con, Map<SWF, Map<String, Set<Integer>>> breakpoints) {
        id = con.getId();        

        toAddBPointMap = breakpoints;
        this.handler = handler;
        Main.startWork(AppStrings.translate("work.debugging"), null);

        synchronized (this) {
            paused = false;
        }

        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                Main.getMainFrame().getPanel().updateMenu();
            }            
        });
        
        

        //enlog(DebuggerConnection.class);
        //enlog(DebuggerCommands.class);
        //enlog(DebuggerHandler.class);
        try {
            con.getMessage(InVersion.class);
        } catch (IOException ex) {
            Logger.getLogger(DebuggerSession.class.getName()).log(Level.SEVERE, "session" + id + ": cannot get version", ex);
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

        //boolean isAS3 = Main.getRunningSWF().isAS3();
        try {

            con.addMessageListener(new DebugMessageListener<InErrorException>() {
                @Override
                public void message(InErrorException t) {
                    for (DebuggerHandler.ErrorListener l : handler.getErrorListeners()) {
                        l.errorException(DebuggerSession.this, t.exceptionMessage, t.thrownVar);
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

            placedObjects.clear();

            con.addMessageListener(new DebugMessageListener<InScript>() {
                @Override
                public void message(InScript sc) {
                    synchronized (DebuggerSession.this) {
                        moduleNames.put(sc.module, sc.name);
                        int file = sc.module;
                        String name = sc.name;
                        int swfIndex = sc.swfIndex;

                        name = name.replaceAll("\\[(invalid_utf8=[0-9]+)\\]", "{$1}");

                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received script added - index {1} name: {2}", new Object[]{id, file, name});

                        String swfHash = "unknown";
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

                                name = swfHash + ":" + DottedChain.parseWithSuffix(pkg).addWithSuffix(clsNameWithSuffix).toPrintableString(new LinkedHashSet<>(), Main.getRunningSWF()/*???*/, con.isAS3);
                            }
                        } else {
                            if (name.contains(":")) {
                                swfHash = name.substring(0, name.indexOf(":"));
                            }
                        }
                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Script added - index {1} name: {2}", new Object[]{id, file, name});
                        modulePaths.put(file, name);
                        scriptToModule.put(name, file);
                        moduleToSwfIndex.put(file, swfIndex);
                        swfIndicesCommitted.add(swfIndex);
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

            commands.squelch(true);

            con.addMessageListener(new DebugMessageListener<InExit>() {
                @Override
                public void message(InExit t) {
                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Handling inExit", id);
                    //con.dropMessage(t);
                    synchronized (DebuggerSession.this) {
                        paused = false;
                        connected = false;
                    }
                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Calling disconnected", id);
                    disconnected();
                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Disconnected called", id);
                }
            });

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
                    //View.execInEventDispatch(
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            boolean modulesEmptyBefore = debuggedSwfs.isEmpty();
                            try {
                                for (InSwfInfo.SwfInfo s : t.swfInfos) {
                                    try {
                                        InGetSwf inGetSwf = con.sendMessage(new OutGetSwf(con, (int) s.index), InGetSwf.class);
                                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received SWF", id);
                                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received SWF.URL = {1}", new Object[]{id, s.url});
                                        if (inGetSwf == null) {
                                            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Cannot read SWF", id);
                                            continue;
                                        }                                        
                                        String sha256 = Helper.byteArrayToHex(MessageDigest.getInstance("SHA-256").digest(inGetSwf.swfData));
                                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received SWF hash = {1}", new Object[]{id, sha256});
                                            
                                        String originalHash = Main.getHashFromMetadataFromSwfBytes(inGetSwf.swfData);
                                        if (originalHash != null) {
                                            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received SWF original hash = {1}", new Object[]{id, originalHash});
                                            sha256 = originalHash;
                                        }
                                        
                                        SWF debuggedSwf = Main.findOpenedSwfByHash(sha256);
                                        if (debuggedSwf == null) {
                                            con.disconnect();
                                            synchronized (DebuggerSession.this) {
                                                paused = false;
                                                connected = false;
                                            }
                                            disconnected();
                                            return;
                                        } else {
                                            debuggedSwfs.put((int) s.index, debuggedSwf);
                                            if (debuggedSwfs.size() == 1) { //it's the first one
                                                con.isAS3 = debuggedSwf.isAS3();
                                            }
                                        }
                                    } catch (IOException | NoSuchAlgorithmException ex) {
                                        //ignore

                                        con.disconnect();
                                        synchronized (DebuggerSession.this) {
                                            paused = false;
                                            connected = false;
                                        }
                                        disconnected();
                                        return;
                                    }
                                }

                                try {
                                    
                                    Set<String> hashes = new LinkedHashSet<>();
                                    
                                    for (int file : modulePaths.keySet()) {
                                        String path = modulePaths.get(file);
                                        if (!path.contains(":")) {
                                            //This is probably SWF file instrumented by another software                                            
                                            throw new IOException("Missing hash");
                                        }
                                        String hash = path.substring(0, path.indexOf(":"));
                                        hashes.add(hash);
                                    }
                                    for (String hash : hashes) {                                        
                                        if (Main.findOpenedSwfByHash(hash) == null) {
                                            //This is probably SWF file instrumented by another software
                                            throw new IOException("SWF with hash " + hash + " not found");
                                        }
                                    }                                                                       
                                    
                                    if (!debuggedSwfs.isEmpty() && modulesEmptyBefore) {
                                        if (title.isEmpty()) {
                                            title = debuggedSwfs.values().iterator().next().toString();
                                        }
                                        if (con.isAS3) {
                                            //Widelines - only AS3, it hangs in AS1/2 and SWD does not support UI32 lines          
                                            con.wideLines = commands.getOption("wide_line_player", "false").equals("true");
                                            if (con.wideLines) {
                                                commands.setOption("wide_line_debugger", "on");
                                            }
                                        } else {
                                            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINER, "session{0}: End of connect - sending continue", id);
                                            con.writeMessage(new OutRewind(con));
                                            con.writeMessage(new OutPlay(con));
                                            commands.sendContinue();
                                        }
                                    }

                                    synchronized (DebuggerSession.this) {
                                        for (int i = 0; i < inSetBreakpoint.files.size(); i++) {
                                            String sname = moduleNames.get(inSetBreakpoint.files.get(i));
                                            int swfIndex = moduleToSwfIndex.get(inSetBreakpoint.files.get(i));
                                            SWF debuggedSwf = debuggedSwfs.get(swfIndex);
                                            if (!confirmedPointMap.containsKey(debuggedSwf)) {
                                                confirmedPointMap.put(debuggedSwf, new HashMap<>());
                                            }
                                            if (!confirmedPointMap.get(debuggedSwf).containsKey(sname)) {
                                                confirmedPointMap.get(debuggedSwf).put(sname, new TreeSet<>());
                                            }
                                            if (toAddBPointMap.containsKey(debuggedSwf)) {
                                                if (toAddBPointMap.get(debuggedSwf).containsKey(sname)) {
                                                    toAddBPointMap.get(debuggedSwf).get(sname).remove(inSetBreakpoint.lines.get(i));
                                                    if (toAddBPointMap.get(debuggedSwf).get(sname).isEmpty()) {
                                                        toAddBPointMap.get(debuggedSwf).remove(sname);
                                                    }
                                                }
                                            }
                                            confirmedPointMap.get(debuggedSwf).get(sname).add(inSetBreakpoint.lines.get(i));
                                            Logger.getLogger(DebuggerSession.class.getName()).log(Level.INFO, "session{0}: Breakpoint {1}:{2} submitted successfully", new Object[]{id, sname, inSetBreakpoint.lines.get(i)});
                                        }
                                    }
                                } catch (IOException ex) {
                                    con.disconnect();
                                    synchronized (DebuggerSession.this) {
                                        paused = false;
                                        connected = false;
                                    }
                                    disconnected();
                                }
                            } finally {
                                getSwfThreadList.remove(Thread.currentThread());
                            }
                        }
                    };
                    Thread thread = new Thread(r, "Debugger session " + this.toString() + " - Get SWF");
                    thread.start();
                    getSwfThreadList.add(thread);
                }
            });

            con.addMessageListener(new DebugMessageListener<InPlaceObject>() {
                @Override
                public void message(InPlaceObject t) {
                    placedObjects.put(t.path, t.objId);
                    con.dropMessage(t);
                }
            });

            inSetBreakpoint = con.getMessage(InSetBreakpoint.class);

            con.addMessageListener(new DebugMessageListener<InContinue>() {
                @Override
                public void message(InContinue msg) {
                    synchronized (DebuggerSession.this) {
                        paused = false;
                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: continued", id);
                    }                    
                    for (DebuggerHandler.BreakListener bl : handler.getBreakListeners()) {
                        bl.doContinue(DebuggerSession.this);
                    }                                        
                }
            });

            con.addMessageListener(new DebugMessageListener<InBreakAt>() {
                @Override
                public void message(InBreakAt message) {
                    synchronized (DebuggerSession.this) {
                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: In break at", id);
                    }
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: In break at start", id);
                                    
                            synchronized (DebuggerSession.this) {
                                if (!connected) {
                                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Received break while not connected", id);
                                    return;
                                }
                                
                                
                                paused = true;
                                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: paused", id);
                            }

                            try {
                                //ignore single InSetBreakpoint, otherwise setting breakpoints later won't work
                                con.getMessage(InSetBreakpoint.class, DebuggerConnection.PREF_RESPONSE_TIMEOUT);

                                breakInfo = con.getMessage(InBreakAtExt.class, DebuggerConnection.PREF_RESPONSE_TIMEOUT);
                                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: Getting break reason...", id);
                                breakReason = con.sendMessageWithTimeout(new OutGetBreakReason(con), InBreakReason.class);

                                int reasonInt = breakReason == null ? 0 : breakReason.reason;

                                String newBreakScriptName = "unknown";
                                String userBreakScriptName = "unknown";
                                String userSwfName = "unknown";
                                if (moduleToSwfIndex.containsKey(message.file)) {
                                    SWF swf = debuggedSwfs.get(moduleToSwfIndex.get(message.file));
                                    userSwfName = swf.toString();
                                } else if (!debuggedSwfs.isEmpty()) {
                                    userSwfName = debuggedSwfs.get(debuggedSwfs.size() - 1).toString();
                                }

                                if (modulePaths.containsKey(message.file)) {
                                    newBreakScriptName = modulePaths.get(message.file);
                                    userBreakScriptName = newBreakScriptName;
                                    if (newBreakScriptName.contains(":")) {
                                        userBreakScriptName = userSwfName + ": " + newBreakScriptName.substring(newBreakScriptName.indexOf(":") + 1);
                                    }
                                } else if (reasonInt != InBreakReason.REASON_SCRIPT_LOADED) {
                                    Logger.getLogger(DebuggerCommands.class.getName()).log(Level.SEVERE, "session{0}: Invalid file: {1}", new Object[]{id, message.file});
                                    //return;
                                }

                                final String[] reasonNames = new String[]{"unknown", "breakpoint", "watch", "fault", "stopRequest", "step", "halt", "scriptLoaded"};
                                String reason = reasonInt < reasonNames.length ? reasonNames[reasonInt] : reasonNames[0];

                                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: break at {1}:{2}, reason: {3}", new Object[]{id, newBreakScriptName, message.line, reason});

                                try {
                                    sendBreakPoints();
                                } catch (IOException ex) {
                                    //ignore
                                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: send breakpoints exception: {1}", new Object[] {id, ex.getMessage()});
                                }

                                synchronized (DebuggerSession.this) {
                                    breakScriptName = newBreakScriptName;
                                    breakIp = message.line;

                                    if (breakInfo != null) {
                                        List<String> files = new ArrayList<>();
                                        for (int i = 0; i < breakInfo.files.size(); i++) {
                                            files.add(DebuggerSession.this.moduleToString(breakInfo.files.get(i)));
                                        }
                                        stackScriptNames = files;
                                        List<Integer> lines = new ArrayList<>(breakInfo.lines);
                                        stackLines = lines;
                                    }
                                }

                                if (reasonInt == InBreakReason.REASON_SCRIPT_LOADED) {
                                    if (!Configuration.debugHalt.get()) {
                                        try {
                                            commands.sendContinue();
                                        } catch (IOException iex) {
                                            //ignore
                                        }
                                        return;
                                    }
                                    Main.startWork(AppStrings.translate("debug.session").replace("%id%", "" + id) + " - " + AppStrings.translate("work.halted.with").replace("%file%", userSwfName), null, true);
                                } else {
                                    Main.startWork(AppStrings.translate("debug.session").replace("%id%", "" + id) + " - " + AppStrings.translate("work.breakat") + userBreakScriptName + ":" + message.line + " " + AppStrings.translate("debug.break.reason." + reason), null, true);
                                }
                                depth = 0;
                                refreshFrame();
                                
                                //If there is single one left paused, switch to it
                                if (Main.getDebugHandler().getNumberOfPausedSessions() == 1) {
                                    Main.getDebugHandler().setSelectedSessionId(getId());
                                }
                                
                                for (DebuggerHandler.BreakListener l : handler.getBreakListeners()) {
                                    l.breakAt(DebuggerSession.this, newBreakScriptName, message.line,
                                            moduleToClassIndex.containsKey(message.file) ? moduleToClassIndex.get(message.file) : -1,
                                            moduleToTraitIndex.containsKey(message.file) ? moduleToTraitIndex.get(message.file) : -1,
                                            moduleToMethodIndex.containsKey(message.file) ? moduleToMethodIndex.get(message.file) : -1
                                    );
                                }
                            } catch (IOException iex) {
                                //ignore ??
                            }
                        }
                    };

                    if (getSwfThreadList.isEmpty()) {
                        r.run();
                    } else {
                        //Wait till all SWFs are loaded
                        int delay = 500; //check each 500ms
                        Timer tim = new Timer();
                        tim.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                TimerTask that = this;
                                if (getSwfThreadList.isEmpty()) {
                                    r.run();
                                } else {
                                    Timer tim = new Timer();
                                    tim.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            that.run();
                                        }
                                    }, delay);
                                }
                            }
                        }, delay);
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
                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/           

            con.addMessageListener(new DebugMessageListener<InTrace>() {
                @Override
                public synchronized void message(InTrace tr) {
                    for (DebuggerHandler.TraceListener l : handler.getTraceListeners()) {
                        l.trace(DebuggerSession.this, tr.text);
                    }
                }
            });

            /*            if (!isAS3) {
                
            }*/
        } catch (IOException ex) {
            synchronized (this) {
                paused = false;
                connected = false;
            }
            disconnected();
        }
    }

    public int getId() {
        return id;
    }
    
    

    public boolean containsSwf(SWF swf) {
        return debuggedSwfs.containsValue(swf);
    }

    public void setMainDebuggedSwf(SWF debuggedSwf) {
        debuggedSwfs.clear();
        //debuggedSwfs.add(debuggedSwf);
    }

    public Map<Integer, SWF> getDebuggedSwfs() {
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
            return commands.getVariableWithTimeout(parentId, varName, useGetter, children, 100);
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

    public void removeBreakPoint(SWF swf, String scriptName, int line) {
        synchronized (this) {                    
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
            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: adding bp {1}:{2}", new Object[]{id, scriptName, line});
            if (isBreakpointToRemove(swf, scriptName, line)) {
                toRemoveBPointMap.get(swf).get(scriptName).remove(line);
                if (toRemoveBPointMap.get(swf).get(scriptName).isEmpty()) {
                    toRemoveBPointMap.get(swf).remove(scriptName);
                }
            }

            if (isBreakpointConfirmed(swf, scriptName, line)) {
                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: bp {1}:{2} already confirmed", new Object[]{id, scriptName, line});
                return true;

            }
            if (isBreakpointInvalid(swf, scriptName, line)) {
                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: bp {1}:{2} already invalid", new Object[]{id, scriptName, line});
                return false;
            }
            if (!toAddBPointMap.containsKey(swf)) {
                toAddBPointMap.put(swf, new HashMap<>());
            }
            if (!toAddBPointMap.get(swf).containsKey(scriptName)) {
                toAddBPointMap.get(swf).put(scriptName, new TreeSet<>());
            }
            toAddBPointMap.get(swf).get(scriptName).add(line);
            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINE, "session{0}: bp {1}:{2} added to todo", new Object[]{id, scriptName, line});
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

    public void refreshFrame() {
        synchronized (this) {                    
            if (!paused) {
                return;
            }
            try {
                frame = commands.getFrameWithTimeout(depth, 100);
                pool = commands.getConstantPoolWithTimeout(0, 100);
            } catch (IOException ex) {
                //ignore
            }
        }
        for (DebuggerHandler.FrameChangeListener l : handler.getFrameChangeListeners()) {
            l.frameChanged(DebuggerSession.this);
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

    private void disconnected() {
        frame = null;
        pool = null;
        breakInfo = null;
        breakReason = null;
        connected = false;
        commands = null;
        synchronized (this) {
            for (SWF debuggedSwf : debuggedSwfs.values()) {
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
        
        //If there is single one left paused, switch to it
        if (Main.getDebugHandler().getNumberOfPausedSessions() == 1) {
            for (DebuggerSession session : Main.getDebugHandler().getActiveSessions().values()) { 
                if (session.isPaused()) {
                    Main.getDebugHandler().setSelectedSessionId(session.getId());
                    break;
                }
            }
        }
        
        for (DebuggerHandler.ConnectionListener l : handler.getConnectionListeners()) {
            l.disconnected(DebuggerSession.this);
        }
        debuggedSwfs.clear();
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

        List<Thread> threads = new ArrayList<>(getSwfThreadList);
        for (Thread t : threads) {
            t.interrupt();
        }
        disconnected();
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

    private void sendBreakPoints() throws IOException {
        if (!isConnected()) {
            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: not sending bps, not connected", id);
            return;
        }
        synchronized (this) {
            Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: sending breakpoints of {1} swfs", new Object[]{id, debuggedSwfs.size()});
            for (int swfIndex = 0; swfIndex < debuggedSwfs.size(); swfIndex++) {
                if (!swfIndicesCommitted.contains(swfIndex)) {
                    continue;
                }
                SWF debuggedSwf = debuggedSwfs.get(swfIndex);
                String hash = Main.getSwfHash(debuggedSwf); //??

                Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: sending breakpoints of {1}", new Object[]{id, hash});

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
                                Logger.getLogger(DebuggerSession.class.getName()).log(Level.INFO, "session{0}: Breakpoint {1}:{2} removed", new Object[]{id, scriptName, line});
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
                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: searching for module of {1}:{2}", new Object[]{id, hash, scriptName});
                        int file = moduleIdOf(hash + ":" + scriptName);
                        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: module = {1}", new Object[]{id, file});
                        if (file > -1) {
                            Set<Integer> lines = new HashSet<>(toAddBPointMap.get(debuggedSwf).get(scriptName));
                            for (int line : lines) {
                                if (commands.addBreakPoint(file, line)) {
                                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.INFO, "session{0}: Breakpoint {1}:{2} submitted successfully", new Object[]{id, scriptName, line});
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
                                    Logger.getLogger(DebuggerSession.class.getName()).log(Level.INFO, "session{0}: Breakpoint {1}:{2} unable to submit", new Object[]{id, scriptName, line});
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
        Logger.getLogger(DebuggerSession.class.getName()).log(Level.FINEST, "session{0}: sending bps finished", id);

    }

    public synchronized InConstantPool getConstantPool() {
        if (!paused) {
            return null;
        }
        return pool;
    }

    public synchronized InCallFunction callMethod(Variable object, String methodName, List<Object> args) throws DebuggerHandler.ActionScriptException {
        return callFunction(false, methodName, object, args);
    }

    public synchronized InCallFunction callMethod(String object, String methodName, List<Object> args) throws DebuggerHandler.ActionScriptException {
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

    public synchronized InCallFunction callFunction(boolean isConstructor, String funcName, Variable thisValue, List<Object> args) throws DebuggerHandler.ActionScriptException {
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
                    throw new DebuggerHandler.ActionScriptException("" + icf.variables.get(0).value);
                }
            }
            return icf;
        } catch (IOException e) {
            //ignored
        }
        return null;
    }

    public Map<String, Long> getPlacedObjects() {
        return new LinkedHashMap<>(placedObjects);
    }

    @Override
    public String toString() {
        return "[s:" + id + "]";
    }

    public String getTitle() {
        return title;
    }        
}
