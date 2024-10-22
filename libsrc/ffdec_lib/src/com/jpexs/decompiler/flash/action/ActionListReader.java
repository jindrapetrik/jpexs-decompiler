/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.deobfuscation.ActionDeobfuscator;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.special.ActionDeobfuscateJump;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.special.ActionUnknown;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.stat.Statistics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for reading Action data from SWF file.
 *
 * @author JPEXS
 */
public class ActionListReader {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(ActionListReader.class.getName());

    /**
     * Constructor.
     */
    public ActionListReader() {
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners List of listeners
     * @param sis SWF input stream
     * @param version SWF version
     * @param ip Start IP
     * @param endIp End IP
     * @param path Path
     * @param deobfuscationMode Deobfuscation mode
     * @return List of actions
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     * @throws TimeoutException On timeout
     */
    public static ActionList readActionListTimeout(final List<DisassemblyListener> listeners, final SWFInputStream sis, final int version, final int ip, final int endIp, final String path, final int deobfuscationMode) throws IOException, InterruptedException, TimeoutException {
        try {
            ActionList actions = CancellableWorker.call("script.readActionList", new Callable<ActionList>() {

                @Override
                public ActionList call() throws IOException, InterruptedException {
                    return readActionList(listeners, sis, version, ip, endIp, path, deobfuscationMode);
                }
            }, Configuration.decompilationTimeoutSingleMethod.get(), TimeUnit.SECONDS);

            return actions;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            } else if (cause instanceof InterruptedException) {
                throw (IOException) cause;
            } else {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return new ActionList(sis.getCharset());
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners List of listeners
     * @param sis SWF input stream
     * @param version SWF version
     * @param ip Start IP
     * @param endIp End IP
     * @param path Path
     * @param deobfuscationMode Deobfuscation mode
     * @return List of actions
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public static ActionList readActionList(List<DisassemblyListener> listeners, SWFInputStream sis, int version, int ip, int endIp, String path, int deobfuscationMode) throws IOException, InterruptedException {
        // Map of the actions. Use TreeMap to sort the keys in ascending order
        // actionMap and nextOffsets should contain exactly the same keys
        Map<Long, Action> actionMap = new TreeMap<>();
        Map<Long, Long> nextOffsets = new HashMap<>();
        Action entryAction = readActionListAtPos(listeners, null,
                sis, actionMap, nextOffsets,
                ip, 0, endIp, path, false, new ArrayList<>(),
                sis.getCharset());

        if (actionMap.isEmpty()) {
            return new ActionList(sis.getCharset());
        }

        List<Long> addresses = new ArrayList<>(actionMap.keySet());

        // add end action
        Action lastAction = actionMap.get(addresses.get(addresses.size() - 1));
        long endAddress;
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd(sis.getCharset());
            aEnd.setAddress(nextOffsets.get(lastAction.getAddress()));
            endAddress = aEnd.getAddress();
            actionMap.put(aEnd.getAddress(), aEnd);
            nextOffsets.put(endAddress, endAddress + 1);
        }

        ActionList actions = fixActionList(new ActionList(actionMap.values(), sis.getCharset()), nextOffsets);

        // jump to the entry action when it is different from the first action in the map
        if (entryAction != actions.get(0)) {
            ActionJump jump = new ActionDeobfuscateJump(0, sis.getCharset());
            actions.addAction(0, jump);
            jump.setJumpOffset((int) (entryAction.getAddress() - jump.getTotalActionLength()));
        }

        if (SWFDecompilerPlugin.fireActionListParsed(actions, sis.getSwf())) {
            actions = fixActionList(actions, null);
        }

        if (deobfuscationMode == 1) {
            try {
                try (Statistics s = new Statistics("ActionDeobfuscator")) {
                    new ActionDeobfuscator().actionListParsed(actions, sis.getSwf());
                }
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable ex) {
                // keep orignal (not deobfuscated) actions
                logger.log(Level.SEVERE, "Deobfuscation failed in: " + path, ex);
            }
        }

        /*System.err.println("=======================");
        int p = 0;
        for (Action a : actions) {
            System.err.println("loc" + Helper.formatAddress(a.getAddress()) + " (" + p + "): " + a.getASMSource(actions, new HashSet<Long>(), ScriptExportMode.PCODE));
            p++;
        }*/
        //TODO: This cleaner needs to be executed only before actual decompilation, not when disassembly only
        try {
            new ActionDefineFunctionPushRegistersCleaner().actionListParsed(actions, sis.getSwf());
        } catch (ThreadDeath | InterruptedException ex) {
            throw ex;
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Cleaning push registers in ActionDefineFunction failed: " + path, ex);
        }

        return actions;
    }

    /**
     * Fixes action list.
     *
     * @param actions Action list
     * @param nextOffsets Next offsets
     * @return Fixed action list
     */
    public static ActionList fixActionList(ActionList actions, Map<Long, Long> nextOffsets) {
        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        ActionList ret = new ActionList(actions.getCharset());
        ret.fileData = actions.fileData;

        if (nextOffsets != null) {
            int index = 0;
            while (index != -1 && index < actions.size()) {
                Action action = actions.get(index);
                ret.add(action);
                index++;
                if (index < actions.size()) {
                    long nextAddress = nextOffsets.get(action.getAddress());
                    if (actions.get(index).getAddress() != nextAddress) {
                        if (!action.isExit() && !(action instanceof ActionJump)) {
                            ActionJump jump = new ActionDeobfuscateJump(0, actions.getCharset());
                            jump.setAddress(action.getAddress());
                            int size = jump.getTotalActionLength();
                            jump.setJumpOffset((int) (nextAddress - action.getAddress() - size));
                            ret.add(jump);
                        }
                    }
                }
            }
        } else {
            ret.addAll(actions);
        }

        // Map for storing the targers of the "jump" actions
        // "jump" action can be ActionIf, ActionJump and any ActionStore
        Map<Action, Action> jumps = new HashMap<>();
        getJumps(ret, jumps);

        updateActionLengths(ret);
        updateAddresses(ret, 0, actions.getCharset());
        long endAddress = ret.get(ret.size() - 1).getAddress();

        updateJumps(ret, jumps, containerLastActions, endAddress, actions.getCharset());
        updateActionStores(ret, jumps);
        updateContainerSizes(ret, containerLastActions);

        return ret;
    }

    /**
     * Gets original actions.
     *
     * @param sis SWF input stream
     * @param startIp Start IP
     * @param endIp End IP
     * @return List of actions
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public static List<Action> getOriginalActions(SWFInputStream sis, int startIp, int endIp) throws IOException, InterruptedException {
        // Map of the actions. Use TreeMap to sort the keys in ascending order
        Map<Long, Action> actionMap = new TreeMap<>();
        Map<Long, Long> nextOffsets = new HashMap<>();
        readActionListAtPos(new ArrayList<>(), null,
                sis, actionMap, nextOffsets,
                startIp, startIp, endIp + 1, "", false, new ArrayList<>(),
                sis.getCharset());

        return new ArrayList<>(actionMap.values());
    }

    /**
     * Gets near address.
     *
     * @param actions Action list
     * @param address Address
     * @param next Next
     * @return Near address
     */
    private static long getNearAddress(ActionList actions, long address, boolean next) {
        int min = 0;
        int max = actions.size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = actions.get(mid).getAddress();
            if (midValue == address) {
                return address;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return next
                ? (min < actions.size() ? actions.get(min).getAddress() : -1)
                : (max >= 0 ? actions.get(max).getAddress() : -1);
    }

    /**
     * Converts list of actions to map of address to action.
     *
     * @param actions List of actions
     * @return Map of address to action
     */
    private static Map<Long, Action> actionListToMap(List<Action> actions) {
        Map<Long, Action> map = new HashMap<>(actions.size());
        for (Action a : actions) {
            long address = a.getAddress();
            // There are multiple actions in the same address (2nd action is a jump for obfuscated code)
            // So this check is required
            if (!map.containsKey(address)) {
                map.put(a.getAddress(), a);
            }
        }
        return map;
    }

    /**
     * Gets jumps.
     *
     * @param actions List of actions
     * @param jumps Jumps - map of jumpAction to targetAction
     */
    private static void getJumps(List<Action> actions, Map<Action, Action> jumps) {
        Map<Long, Action> actionMap = actionListToMap(actions);
        for (Action a : actions) {
            long target = -1;
            if (a instanceof ActionIf) {
                target = ((ActionIf) a).getTargetAddress();
            } else if (a instanceof ActionJump) {
                target = ((ActionJump) a).getTargetAddress();
            } else if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                int storeSize = aStore.getStoreSize();
                // skip storeSize + 1 actions (+1 is the current action)
                Action targetAction = a;
                for (int i = 0; i <= storeSize; i++) {
                    long address = targetAction.getAddress() + targetAction.getTotalActionLength();
                    targetAction = actionMap.get(address);
                    if (targetAction == null) {
                        break;
                    }
                }
                jumps.put(a, targetAction);
            }
            if (target >= 0) {
                Action targetAction = actionMap.get(target);
                jumps.put(a, targetAction);
            }
        }
    }

    /**
     * Gets container last actions.
     *
     * @param actions List of actions
     * @param action Action
     * @return List of last actions
     */
    public static List<Action> getContainerLastActions(ActionList actions, Action action) {
        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
        List<Long> sizes = container.getContainerSizes();
        long endAddress = action.getAddress() + container.getHeaderSize();
        List<Action> lasts = new ArrayList<>(sizes.size());
        for (long size : sizes) {
            endAddress += size;
            long lastActionAddress = getNearAddress(actions, endAddress - 1, false);
            Action lastAction = null;
            if (lastActionAddress != -1) {
                lastAction = actions.getByAddress(lastActionAddress);
            }
            lasts.add(lastAction);
        }
        return lasts;
    }

    /**
     * Gets container last actions.
     *
     * @param actions List of actions
     * @param lastActions Last actions
     */
    private static void getContainerLastActions(ActionList actions, Map<Action, List<Action>> lastActions) {
        for (Action a : actions) {
            if (a instanceof GraphSourceItemContainer) {
                lastActions.put(a, getContainerLastActions(actions, a));
            }
        }
    }

    /**
     * Updates addresses.
     *
     * @param actions List of actions
     * @param address Address
     * @param charset Charset
     * @return End address
     */
    private static long updateAddresses(List<Action> actions, long address, String charset) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            a.setAddress(address);
            int length = a.getTotalActionLength();
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                // placeholder for jump action
                length = new ActionDeobfuscateJump(0, charset).getTotalActionLength();
            }
            address += length;
        }
        return address;
    }

    /**
     * Updates action lengths.
     *
     * @param actions List of actions
     */
    private static void updateActionLengths(List<Action> actions) {
        for (int i = 0; i < actions.size(); i++) {
            actions.get(i).updateLength();
        }
    }

    /**
     * Updates action stores.
     *
     * @param actions List of actions
     * @param jumps Jumps - map of jumpAction to targetAction
     */
    private static void updateActionStores(List<Action> actions, Map<Action, Action> jumps) {
        Map<Long, Action> actionMap = actionListToMap(actions);
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                Action nextActionAfterStore = jumps.get(a);
                Action a1 = a;
                List<Action> store = new ArrayList<>();
                while (true) {
                    long address = a1.getAddress() + a1.getTotalActionLength();
                    a1 = actionMap.get(address);
                    if (a1 == null || a1 == nextActionAfterStore) {
                        break;
                    }
                    store.add(a1);
                }
                aStore.setStore(store);
            }
        }
    }

    /**
     * Updates container sizes.
     *
     * @param actions List of actions
     * @param containerLastActions Container last actions
     */
    private static void updateContainerSizes(List<Action> actions, Map<Action, List<Action>> containerLastActions) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                List<Action> lastActions = containerLastActions.get(a);
                long startAddress = a.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    Action lastAction = lastActions.get(j);
                    int length = (int) (lastAction.getAddress() + lastAction.getTotalActionLength() - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }
        }
    }

    /**
     * Replaces jump targets.
     *
     * @param jumps Jumps - map of jumpAction to targetAction
     * @param oldTarget Old target
     * @param newTarget New target
     */
    private static void replaceJumpTargets(Map<Action, Action> jumps, Action oldTarget, Action newTarget) {
        for (Action a : jumps.keySet()) {
            if (jumps.get(a) == oldTarget) {
                jumps.put(a, newTarget);
            }
        }
    }

    /**
     * Replaces container last actions.
     *
     * @param containerLastActions Container last actions
     * @param oldTarget Old target
     * @param newTarget New target
     */
    private static void replaceContainerLastActions(Map<Action, List<Action>> containerLastActions, Action oldTarget, Action newTarget) {
        for (Action a : containerLastActions.keySet()) {
            List<Action> targets = containerLastActions.get(a);
            for (int i = 0; i < targets.size(); i++) {
                if (targets.get(i) == oldTarget) {
                    targets.set(i, newTarget);
                }
            }
        }
    }

    /**
     * Updates jumps.
     *
     * @param actions List of actions
     * @param jumps Jumps - map of jumpAction to targetAction
     * @param containerLastActions Container last actions
     * @param endAddress End address
     * @param charset Charset
     */
    private static void updateJumps(List<Action> actions, Map<Action, Action> jumps, Map<Action, List<Action>> containerLastActions, long endAddress, String charset) {
        if (actions.isEmpty()) {
            return;
        }

        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                ActionJump aJump = new ActionDeobfuscateJump(0, charset);
                aJump.setJumpOffset((int) (endAddress - a.getAddress() - aJump.getTotalActionLength()));
                aJump.setAddress(a.getAddress());
                replaceJumpTargets(jumps, a, aJump);
                replaceContainerLastActions(containerLastActions, a, aJump);
                a = aJump;
                actions.set(i, a);
            } else if (a instanceof ActionIf) {
                ActionIf aIf = (ActionIf) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - a.getTotalActionLength();
                } else {
                    offset = endAddress - a.getAddress() - a.getTotalActionLength();
                }
                aIf.setJumpOffset((int) offset);
            } else if (a instanceof ActionJump) {
                ActionJump aJump = (ActionJump) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - a.getTotalActionLength();
                } else {
                    offset = endAddress - a.getAddress() - a.getTotalActionLength();
                }
                aJump.setJumpOffset((int) offset);
            }
        }
    }

    /**
     * Removes an action from the action list, and updates all references This
     * method will keep the inner actions of the container when you remove the
     * container.
     *
     * @param actions List of actions
     * @param index Index
     * @param removeWhenLast Remove when last
     * @return True if action was removed, false otherwise
     */
    public static boolean removeAction(ActionList actions, int index, boolean removeWhenLast) {

        if (index < 0 || actions.size() <= index) {
            return false;
        }

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        long endAddress = lastAction.getAddress() + lastAction.getTotalActionLength();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);

        Action prevAction = index > 0 ? actions.get(index - 1) : null;
        Action nextAction = index + 1 < actions.size() ? actions.get(index + 1) : null;
        Action actionToRemove = actions.get(index);
        for (Action a : containerLastActions.keySet()) {
            List<Action> lastActions = containerLastActions.get(a);
            for (int i = 0; i < lastActions.size(); i++) {
                if (lastActions.get(i) == actionToRemove) {
                    if (!removeWhenLast) {
                        return false;
                    }
                    lastActions.set(i, prevAction);
                }
            }
        }
        for (Action a : jumps.keySet()) {
            Action targetAction = jumps.get(a);
            if (targetAction == actionToRemove) {
                jumps.put(a, nextAction);
            }
        }
        if (containerLastActions.containsKey(actionToRemove)) {
            containerLastActions.remove(actionToRemove);
        }
        if (jumps.containsKey(actionToRemove)) {
            jumps.remove(actionToRemove);
        }

        actions.remove(index);

        updateActionLengths(actions);
        updateAddresses(actions, startIp, actions.getCharset());
        updateJumps(actions, jumps, containerLastActions, endAddress, actions.getCharset());
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Removes multiple actions from the action list, and updates all references
     * This method will keep the inner actions of the container when you remove
     * the container.
     *
     * @param actions List of actions
     * @param actionsToRemove Actions to remove
     * @param removeWhenLast Remove when last
     * @return True if actions were removed, false otherwise
     */
    public static boolean removeActions(ActionList actions, List<Action> actionsToRemove, boolean removeWhenLast) {

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        long endAddress = lastAction.getAddress() + lastAction.getTotalActionLength();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);

        for (Action actionToRemove : actionsToRemove) {
            int index = actions.getIndexByAction(actionToRemove);
            Action prevAction = index > 0 ? actions.get(index - 1) : null;
            Action nextAction = index + 1 < actions.size() ? actions.get(index + 1) : null;
            for (Action a : containerLastActions.keySet()) {
                List<Action> lastActions = containerLastActions.get(a);
                for (int i = 0; i < lastActions.size(); i++) {
                    if (lastActions.get(i) == actionToRemove) {
                        if (!removeWhenLast) {
                            return false;
                        }
                        lastActions.set(i, prevAction);
                    }
                }
            }

            for (Action a : jumps.keySet()) {
                Action targetAction = jumps.get(a);
                if (targetAction == actionToRemove) {
                    jumps.put(a, nextAction);
                }
            }
            if (containerLastActions.containsKey(actionToRemove)) {
                containerLastActions.remove(actionToRemove);
            }
            if (jumps.containsKey(actionToRemove)) {
                jumps.remove(actionToRemove);
            }

            actions.remove(index);
        }

        updateActionLengths(actions);
        updateAddresses(actions, startIp, actions.getCharset());
        updateJumps(actions, jumps, containerLastActions, endAddress, actions.getCharset());
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Adds an action to the action list to the specified location, and updates
     * all references.
     *
     * @param actions List of actions
     * @param index Index
     * @param action Action
     * @param addToContainer Add to container
     * @param replaceJump Replace jump
     * @return True if action was added, false otherwise
     */
    public static boolean addAction(ActionList actions, int index, Action action,
            boolean addToContainer, boolean replaceJump) {

        if (index < 0 || actions.size() < index) {
            return false;
        }

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd(actions.getCharset());
            aEnd.setAddress(lastAction.getAddress() + lastAction.getTotalActionLength());
            actions.add(aEnd);
            lastAction = aEnd;
        }

        long endAddress = lastAction.getAddress();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        List<Action> tempActions = new ArrayList<>(actions);
        tempActions.add(action);
        getJumps(tempActions, jumps);

        Action prevAction = actions.get(index);
        if (addToContainer) {
            for (Action a : containerLastActions.keySet()) {
                List<Action> lastActions = containerLastActions.get(a);
                for (int i = 0; i < lastActions.size(); i++) {
                    if (lastActions.get(i) == prevAction) {
                        lastActions.set(i, action);
                    }
                }
            }
        }

        if (replaceJump) {
            for (Action a : jumps.keySet()) {
                Action targetAction = jumps.get(a);
                if (targetAction == prevAction) {
                    jumps.put(a, action);
                }
            }
        }

        actions.add(index, action);

        updateActionLengths(actions);
        updateAddresses(actions, startIp, actions.getCharset());
        updateJumps(actions, jumps, containerLastActions, endAddress, actions.getCharset());
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Adds an action to the action list to the specified location, and updates
     * all references.
     *
     * @param actions List of actions
     * @param index Index
     * @param newActions New actions
     * @return True if actions were added, false otherwise
     */
    public static boolean addActions(ActionList actions, int index, List<Action> newActions) {

        if (index < 0 || actions.size() < index) {
            return false;
        }

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd(actions.getCharset());
            aEnd.setAddress(lastAction.getAddress() + lastAction.getTotalActionLength());
            actions.add(aEnd);
            lastAction = aEnd;
        }

        long endAddress = lastAction.getAddress();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        List<Action> tempActions = new ArrayList<>(actions);
        tempActions.addAll(newActions);
        getJumps(tempActions, jumps);

        actions.addAll(index, newActions);

        updateActionLengths(actions);
        updateAddresses(actions, startIp, actions.getCharset());
        updateJumps(actions, jumps, containerLastActions, endAddress, actions.getCharset());
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Reads action list at the specified position.
     *
     * @param listeners List of listeners
     * @param cpool Constant pool
     * @param sis SWF input stream
     * @param actions Actions
     * @param nextOffsets Next offsets
     * @param ip IP
     * @param startIp Start IP
     * @param endIp End IP
     * @param path Path
     * @param indeterminate Indeterminate
     * @param visitedContainers Visited containers
     * @param charset Charset
     * @return Action
     * @throws IOException On I/O error
     */
    private static Action readActionListAtPos(List<DisassemblyListener> listeners, ConstantPool cpool,
            SWFInputStream sis, Map<Long, Action> actions, Map<Long, Long> nextOffsets,
            long ip, long startIp, long endIp, String path, boolean indeterminate, List<Long> visitedContainers, String charset) throws IOException {

        Action entryAction = null;

        if (visitedContainers.contains(ip)) {
            return null;
        }
        visitedContainers.add(ip);

        Queue<Long> jumpQueue = new LinkedList<>();
        jumpQueue.add(ip);
        while (!jumpQueue.isEmpty()) {
            ip = jumpQueue.remove();
            if (ip < startIp) {
                continue;
            }

            while (endIp == -1 || endIp > ip) {
                sis.seek((int) ip);

                Action a;
                if ((a = sis.readAction()) == null) {
                    break;
                }
                a.fileOffset = ip;

                int actionLengthWithHeader = a.getTotalActionLength();

                // unknown action, replace with jump
                if (a instanceof ActionUnknown && a.getActionCode() >= 0x80) {
                    ActionJump aJump = new ActionDeobfuscateJump(0, charset);
                    int jumpLength = aJump.getTotalActionLength();
                    aJump.setAddress(a.getAddress());
                    //FIXME! This offset can be larger than SI16 value!
                    aJump.setJumpOffset(actionLengthWithHeader - jumpLength);
                    a = aJump;
                    actionLengthWithHeader = a.getTotalActionLength();
                }

                if (entryAction == null) {
                    entryAction = a;
                }

                Action existingAction = actions.get(ip);
                if (existingAction != null) {
                    break;
                }

                actions.put(ip, a);
                nextOffsets.put(ip, ip + actionLengthWithHeader);

                long pos = sis.getPos();
                long length = pos + sis.available();
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).progressReading(pos, length);
                }

                a.setAddress(ip);

                if (a instanceof ActionPush && cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                } else if (a instanceof ActionConstantPool) {
                    cpool = new ConstantPool(((ActionConstantPool) a).constantPool);
                } else if (a instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) a;
                    long nIp = ip + actionLengthWithHeader + aIf.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                } else if (a instanceof ActionJump) {
                    ActionJump aJump = (ActionJump) a;
                    long nIp = ip + actionLengthWithHeader + aJump.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                    break;
                } else if (a instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                    String cntName = cnt.getName();
                    String newPath = path + (cntName == null ? "" : "/" + cntName);
                    for (long size : cnt.getContainerSizes()) {
                        if (size != 0) {
                            long ip2 = ip + actionLengthWithHeader;
                            long endIp2 = ip + actionLengthWithHeader + size;
                            readActionListAtPos(listeners, cpool,
                                    sis, actions, nextOffsets,
                                    ip2, startIp, endIp2, newPath, indeterminate, visitedContainers,
                                    charset);
                            actionLengthWithHeader += size;
                        }
                    }
                }

                ip += actionLengthWithHeader;

                if (a.isExit()) {
                    break;
                }
            }
        }
        return entryAction;
    }

    /**
     * Fixes constant pools.
     *
     * @param listeners List of listeners
     * @param actions Actions
     * @return True if constant pools were fixed, false otherwise
     */
    public static boolean fixConstantPools(List<DisassemblyListener> listeners, ActionList actions) {
        Action lastAction = actions.get(actions.size() - 1);
        int endIp = (int) lastAction.getAddress();
        List<Action> actionMap = new ArrayList<>(endIp);
        for (int i = 0; i <= endIp; i++) {
            actionMap.add(null);
        }
        for (Action a : actions) {
            actionMap.set((int) a.getAddress(), a);
        }

        try {
            int startIp = (int) actions.get(0).getAddress();
            return fixConstantPools(listeners, new ConstantPool(), actionMap, new TreeMap<>(), startIp, startIp, endIp, null, true, new ArrayList<>());
        } catch (IOException ex) {
            // ignore
        }

        return false;
    }

    /**
     * Fixes constant pools.
     *
     * @param listeners List of listeners
     * @param cpool Constant pool
     * @param actions Actions
     * @param actionMap Action map
     * @param ip IP
     * @param startIp Start IP
     * @param endIp End IP
     * @param path Path
     * @param indeterminate Indeterminate
     * @param visitedContainers Visited containers
     * @return True if constant pools were fixed, false otherwise
     * @throws IOException On I/O error
     */
    private static boolean fixConstantPools(List<DisassemblyListener> listeners, ConstantPool cpool,
            List<Action> actions, Map<Integer, Action> actionMap,
            int ip, int startIp, int endIp, String path, boolean indeterminate, List<Integer> visitedContainers) throws IOException {

        if (visitedContainers.contains(ip)) {
            return false;
        }
        visitedContainers.add(ip);

        Queue<Integer> jumpQueue = new LinkedList<>();
        jumpQueue.add(ip);
        boolean ret = false;
        while (!jumpQueue.isEmpty()) {
            ip = jumpQueue.remove();
            if (ip < startIp) {
                continue;
            }

            while (endIp == -1 || endIp > ip) {
                Action a;
                if ((a = actions.get(ip)) == null) {
                    break;
                }

                int actionLengthWithHeader = a.getTotalActionLength();

                Action existingAction = actionMap.get(ip);
                if (existingAction != null) {
                    break;
                }

                actionMap.put(ip, a);

                if (listeners != null) {
                    for (int i = 0; i < listeners.size(); i++) {
                        listeners.get(i).progressReading(ip, actions.size());
                    }
                }

                if (a.getAddress() != ip) {
                    a.setAddress(ip);
                    ret = true;
                }

                if (a instanceof ActionPush && cpool != null) {
                    ActionPush push = (ActionPush) a;
                    if (push.constantPool != cpool.constants) {
                        push.constantPool = cpool.constants.isEmpty() ? null : cpool.constants;
                        if (push.constantPool != null) {
                            ret = true;
                        }
                    }
                } else if (a instanceof ActionConstantPool) {
                    cpool = new ConstantPool(((ActionConstantPool) a).constantPool);
                } else if (a instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) a;
                    int nIp = ip + actionLengthWithHeader + aIf.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                } else if (a instanceof ActionJump) {
                    ActionJump aJump = (ActionJump) a;
                    int nIp = ip + actionLengthWithHeader + aJump.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                    break;
                } else if (a instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                    String cntName = cnt.getName();
                    String newPath = path + (cntName == null ? "" : "/" + cntName);
                    for (long size : cnt.getContainerSizes()) {
                        if (size != 0) {
                            int ip2 = ip + actionLengthWithHeader;
                            int endIp2 = ip + actionLengthWithHeader + (int) size;
                            ret |= fixConstantPools(listeners, cpool, actions, actionMap, ip2, startIp, endIp2, newPath, indeterminate, visitedContainers);
                            actionLengthWithHeader += size;
                        }
                    }
                }

                ip += actionLengthWithHeader;

                if (a.isExit()) {
                    break;
                }
            }
        }

        return ret;
    }
}
