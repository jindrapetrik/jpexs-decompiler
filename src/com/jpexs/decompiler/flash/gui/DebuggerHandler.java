/*
 *  Copyright (C) 2010-2026 JPEXS
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
import com.jpexs.debugger.flash.Debugger;
import com.jpexs.debugger.flash.DebuggerConnection;
import com.jpexs.debugger.flash.Variable;
import com.jpexs.decompiler.flash.SWF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * @author JPEXS
 */
public class DebuggerHandler implements DebugConnectionListener {

    private List<DebuggerSession> sessions = Collections.synchronizedList(new ArrayList<>());

    private int selectedSessionId = -1;

    private final Object selectionLock = new Object();

    private boolean terminating = false;

    private Map<SWF, Map<String, Set<Integer>>> allBreakPoints = new WeakHashMap<>();

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

    public static interface ConnectionListener {

        public void connected(DebuggerSession session);

        public void disconnected(DebuggerSession session);
    }

    public static interface TraceListener {

        public void trace(DebuggerSession session, String... val);
    }

    public static interface ErrorListener {

        public void errorException(DebuggerSession session, String message, Variable thrownVar);
    }

    public static interface FrameChangeListener {

        public void frameChanged(DebuggerSession session);
    }

    public static interface BreakListener {

        public void breakAt(DebuggerSession session, String scriptName, int line, int classIndex, int traitIndex, int methodIndex);

        public void doContinue(DebuggerSession session);
    }

    public static interface SessionSelectionListener {

        public void sessionSelected(DebuggerSession newSession, int oldSessionId);
    }

    private final List<DebuggerHandler.SessionSelectionListener> selectionListeners = new ArrayList<>();

    private final List<DebuggerHandler.BreakListener> breakListeners = new ArrayList<>();

    private final List<DebuggerHandler.TraceListener> traceListeners = new ArrayList<>();

    private final List<DebuggerHandler.ErrorListener> errorListeners = new ArrayList<>();

    private final List<DebuggerHandler.FrameChangeListener> frameChangeListeners = new ArrayList<>();

    private final List<DebuggerHandler.ConnectionListener> connectionListeners = new ArrayList<>();

    public void addSelectionListener(DebuggerHandler.SessionSelectionListener l) {
        selectionListeners.add(l);
    }

    public void removeSelectionListener(DebuggerHandler.SessionSelectionListener l) {
        selectionListeners.remove(l);
    }

    public void addBreakListener(DebuggerHandler.BreakListener l) {
        breakListeners.add(l);
    }

    public void removeBreakListener(DebuggerHandler.BreakListener l) {
        breakListeners.remove(l);
    }

    public void addFrameChangeListener(DebuggerHandler.FrameChangeListener l) {
        frameChangeListeners.add(l);
    }

    public void removeFrameChangeListener(DebuggerHandler.FrameChangeListener l) {
        frameChangeListeners.remove(l);
    }

    public void addTraceListener(DebuggerHandler.TraceListener l) {
        traceListeners.add(l);
    }

    public void removeTraceListener(DebuggerHandler.TraceListener l) {
        traceListeners.remove(l);
    }

    public void addErrorListener(DebuggerHandler.ErrorListener l) {
        errorListeners.add(l);
    }

    public void removeErrorListener(DebuggerHandler.ErrorListener l) {
        errorListeners.remove(l);
    }

    public void addConnectionListener(DebuggerHandler.ConnectionListener l) {
        connectionListeners.add(l);
    }

    public void removeConnectionListener(DebuggerHandler.ConnectionListener l) {
        connectionListeners.remove(l);
    }

    public List<BreakListener> getBreakListeners() {
        return new ArrayList<>(breakListeners);
    }

    public List<ErrorListener> getErrorListeners() {
        return new ArrayList<>(errorListeners);
    }

    public List<ConnectionListener> getConnectionListeners() {
        return new ArrayList<>(connectionListeners);
    }

    public List<FrameChangeListener> getFrameChangeListeners() {
        return new ArrayList<>(frameChangeListeners);
    }

    public List<TraceListener> getTraceListeners() {
        return new ArrayList<>(traceListeners);
    }

    @Override
    public void failedListen(IOException ex) {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                //disconnect();
                Main.stopRun();
                Main.stopWork();
                ViewMessages.showMessageDialog(Main.getMainFrame().getPanel(), AppStrings.translate("error.debug.listen").replace("%port%", "" + Debugger.DEBUG_PORT));
                Main.getMainFrame().getPanel().updateMenu();
            }
        });
    }

    @Override
    public void connected(DebuggerConnection con) {
                
        Map<SWF, Map<String, Set<Integer>>> breakpoints = new LinkedHashMap<>();
        synchronized (this) {
            if (terminating) {
                return;
            }                        
            Set<SWF> swfs = new LinkedHashSet<>(allBreakPoints.keySet());
            for (SWF swf : swfs) {
                breakpoints.put(swf, new LinkedHashMap<>());
                Set<String> scriptNames = new LinkedHashSet<>(allBreakPoints.get(swf).keySet());
                for (String scriptName : scriptNames) {
                    breakpoints.get(swf).put(scriptName, new TreeSet<>());
                    for (int line : allBreakPoints.get(swf).get(scriptName)) {
                        breakpoints.get(swf).get(scriptName).add(line);
                    }
                }
            }
        }
        
        DebuggerSession session = new DebuggerSession(this, con, breakpoints);
        sessions.add(session);
        if (session.isConnected()) {
            for (DebuggerHandler.ConnectionListener l : getConnectionListeners()) {
                l.connected(session);
            }
        }
    }

    public void sessionInited(DebuggerSession session) {

    }

    public boolean isAnySessionConnected() {
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public int getNumberOfPausedSessions() {
        int count = 0;
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.isConnected() && session.isPaused()) {
                count++;
            }
        }
        return count;
    }

    public void terminateAllSessions() {
        synchronized (this) {
            if (terminating) {
                return;
            }
            terminating = true;
        }
        for (DebuggerSession session : sessions) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        synchronized (this) {
            terminating = false;
        }
    }

    public synchronized Map<String, Set<Integer>> getAllSessionsBreakPoints(SWF swf) {
        if (!allBreakPoints.containsKey(swf)) {
            return new LinkedHashMap<>();
        }

        return Collections.unmodifiableMap(allBreakPoints.get(swf));
    }

    public synchronized Set<Integer> getAllSessionsScriptBreakPoints(SWF swf, String scriptName) {
        if (!allBreakPoints.containsKey(swf)) {
            return new TreeSet<>();
        }
        if (!allBreakPoints.get(swf).containsKey(scriptName)) {
            return new TreeSet<>();
        }

        return Collections.unmodifiableSet(allBreakPoints.get(swf).get(scriptName));
    }

    public boolean addBreakPoint(SWF swf, String scriptName, int line) {
        if (!allBreakPoints.containsKey(swf)) {
            allBreakPoints.put(swf, new LinkedHashMap<>());
        }
        if (!allBreakPoints.get(swf).containsKey(scriptName)) {
            allBreakPoints.get(swf).put(scriptName, new TreeSet<>());
        }
        allBreakPoints.get(swf).get(scriptName).add(line);

        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            session.addBreakPoint(swf, scriptName, line);
        }
        return true;
    }

    public void removeBreakPoint(SWF swf, String scriptName, int line) {
        synchronized (this) {                    
            if (!allBreakPoints.containsKey(swf)) {
                return;
            }
            if (!allBreakPoints.get(swf).containsKey(scriptName)) {
                return;
            }
            allBreakPoints.get(swf).get(scriptName).remove(line);        
        }

        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            session.removeBreakPoint(swf, scriptName, line);
        }
    }

    public void clearBreakPoints(SWF swf) {
        allBreakPoints.remove(swf);
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            session.clearBreakPoints(swf);
        }
    }

    public DebuggerSession getSessionContainingSwf(SWF swf) {
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.containsSwf(swf)) {
                return session;
            }
        }
        return null;
    }

    public SWF getAnyDebuggedSwf() {
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.isConnected() && !session.getDebuggedSwfs().isEmpty()) {
                return session.getDebuggedSwfs().values().iterator().next();
            }
        }
        return null;
    }

    public Map<Integer, DebuggerSession> getActiveSessions() {
        Map<Integer, DebuggerSession> ret = new LinkedHashMap<>();
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.isConnected()) {
                ret.put(session.getId(), session);
            }
        }
        return ret;
    }

    public DebuggerSession getSessionById(int id) {
        List<DebuggerSession> currentSessions = new ArrayList<>(sessions);
        for (DebuggerSession session : currentSessions) {
            if (session.isConnected() && session.getId() == id) {
                return session;
            }
        }
        return null;
    }

    public DebuggerSession getSelectedSession() {
        int oldSelection;
        int newSelection;
        DebuggerSession ret = null;
        synchronized (selectionLock) {
            oldSelection = selectedSessionId;
            newSelection = oldSelection;
            Map<Integer, DebuggerSession> activeSessions = getActiveSessions();
            if (activeSessions.isEmpty()) {
                selectedSessionId = -1;
                newSelection = -1;
            } else {

                if (selectedSessionId == -1) {
                    DebuggerSession session = activeSessions.values().iterator().next();
                    selectedSessionId = newSelection = session.getId();                   
                }

                ret = getSessionById(selectedSessionId);
                if (ret == null) {
                    ret = activeSessions.values().iterator().next();
                    selectedSessionId = ret.getId();
                    newSelection = selectedSessionId;
                }
            }
        }
        
        if (oldSelection != newSelection) {
            for (SessionSelectionListener l : selectionListeners) {
                l.sessionSelected(ret, oldSelection);                
            }
        }
        
        return ret;

    }

    public boolean setSelectedSessionId(int id) {
        DebuggerSession session = getSessionById(id);
        if (session == null) {
            return false;
        }
        int oldSelection;
        int newSelection;
        synchronized (selectionLock) {
            oldSelection = selectedSessionId;
            selectedSessionId = id;          
            newSelection = id;
        }
        
        if (oldSelection != newSelection) {
            for (SessionSelectionListener l : selectionListeners) {
                l.sessionSelected(session, oldSelection);                
            }
        }
        return true;
    }
}
