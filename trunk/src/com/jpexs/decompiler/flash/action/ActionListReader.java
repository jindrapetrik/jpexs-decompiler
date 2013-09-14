/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.ReReadableInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for reading data from SWF file
 *
 * @author JPEXS
 */
public class ActionListReader {

    private static final Logger log = Logger.getLogger(SWFInputStream.class.getName());

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners
     * @param address
     * @param ip
     * @param rri
     * @param version
     * @param containerSWFOffset
     * @param endip
     * @param path
     * @return List of actions
     * @throws IOException
     */
    public static List<Action> readActionList(List<DisassemblyListener> listeners, long containerSWFOffset, ReReadableInputStream rri, int version, int ip, int endIp, String path) throws IOException {
        boolean deobfuscate = Configuration.getConfig("autoDeobfuscate", true);

        ConstantPool cpool = new ConstantPool();

        SWFInputStream sis = new SWFInputStream(rri, version);

        // List of the actions. N. item contains the action which starts in offset N.
        List<Action> actionMap = new ArrayList<>();
        List<Long> nextOffsets = new ArrayList<>();
        Action entryAction = readActionListAtPos(listeners, containerSWFOffset, cpool,
                sis, rri,
                actionMap, nextOffsets,
                ip, ip, endIp, version, path, false, new ArrayList<Long>());

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actionMap, containerLastActions);

        List<Action> actions = new ArrayList<>();

        // jump to the entry action when it is diffrent from the first action in the map
        int index = getNextNotNullIndex(actionMap, 0);
        if (index != -1 && entryAction != actionMap.get(index)) {
            ActionJump jump = new ActionJump(0);
            int size = getTotalActionLength(jump);
            jump.setJumpOffset((int) (entryAction.getAddress() - size));
            actions.add(jump);
        }

        // remove nulls
        index = getNextNotNullIndex(actionMap, index);
        while (index > -1) {
            Action action = actionMap.get(index);
            long nextOffset = nextOffsets.get(index);
            int nextIndex = getNextNotNullIndex(actionMap, index + 1);
            actions.add(action);
            if (nextIndex != -1 && nextOffset != nextIndex) {
                if (!action.isExit() && !(action instanceof ActionJump)) {
                    ActionJump jump = new ActionJump(0);
                    jump.setAddress(action.getAddress(), version);
                    int size = getTotalActionLength(jump);
                    jump.setJumpOffset((int) (nextOffset - action.getAddress() - size));
                    actions.add(jump);
                }
            }
            index = nextIndex;
        }

        // Map for storing the targers of the "jump" actions
        // "jump" action can be ActionIf, ActionJump and any ActionStore
        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);

        long endAddress = updateAddresses(actions, ip, version);
        updateJumps(actions, jumps, containerLastActions, endAddress, version);
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);
        updateActionLengths(actions, version);

        // add end action
        Action lastAction = actions.get(actions.size() - 1);
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd();
            aEnd.setAddress(endAddress, version);
            actions.add(aEnd);
            endAddress += getTotalActionLength(aEnd);
        }

        if (deobfuscate) {
            return deobfuscateActionList(listeners, containerSWFOffset, actions, version, 0, path);
        }

        return actions;
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners
     * @param address
     * @param ip
     * @param rri
     * @param version
     * @param containerSWFOffset
     * @param endip
     * @param path
     * @return List of actions
     * @throws IOException
     */
    public static List<Action> deobfuscateActionList(List<DisassemblyListener> listeners, long containerSWFOffset, List<Action> actions, int version, int ip, String path) throws IOException {
        byte[] data = Action.actionsToBytes(actions, true, version);
        ReReadableInputStream rri = new ReReadableInputStream(new ByteArrayInputStream(data));
        int endIp = data.length;

        List<Action> retdups = new ArrayList<>();
        for (int i = 0; i <= endIp; i++) {
            Action a = new ActionNop();
            a.setAddress(i, version);
            retdups.add(a);
        }
        ConstantPool cpool = new ConstantPool();

        Stack<GraphTargetItem> stack = new Stack<>();

        List<Object> localData = Helper.toList(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());

        SWFInputStream sis = new SWFInputStream(rri, version);
        deobfustaceActionListAtPosRecursive(listeners, new ArrayList<GraphTargetItem>(), new HashMap<Long, List<GraphSourceItemContainer>>(), containerSWFOffset, localData, stack, cpool, sis, rri, ip, retdups, ip, endIp, path, new HashMap<Integer, Integer>(), false, new HashMap<Integer, HashMap<String, GraphTargetItem>>(), version);

        if (!retdups.isEmpty()) {
            for (int i = 0; i < ip; i++) {
                retdups.remove(0);
            }
        }
        List<Action> ret = new ArrayList<>();
        Action last = null;
        for (Action a : retdups) {
            if (a != last) {
                ret.add(a);
            }
            last = a;
        }
        for (int i = 0; i < retdups.size(); i++) {
            Action a = retdups.get(i);
            if (a instanceof ActionEnd) {
                if (i < retdups.size() - 1) {
                    ActionJump jmp = new ActionJump(0);
                    jmp.setJumpOffset(retdups.size() - i - jmp.getBytes(version).length);
                    a.replaceWith = jmp;
                }
            }
        }

        if (Configuration.getConfig("removeNops", true)) {
            ret = Action.removeNops(0, ret, version, 0, path);
        }
        List<Action> reta = new ArrayList<>();
        for (Object o : ret) {
            if (o instanceof Action) {
                reta.add((Action) o);
            }
        }
        return reta;
    }

    private static int getPrevNotNullIndex(List<Action> actionMap, int startIndex) {
        startIndex = Math.min(startIndex, actionMap.size() - 1);
        for (int i = startIndex; i >= 0; i--) {
            if (actionMap.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

    private static int getNextNotNullIndex(List<Action> actionMap, int startIndex) {
        for (int i = startIndex; i < actionMap.size(); i++) {
            if (actionMap.get(i) != null) {
                return i;
            }
        }
        return -1;
    }

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

    private static void getJumps(List<Action> actions, Map<Action, Action> jumps) {
        Map<Long, Action> actionMap = actionListToMap(actions);
        for (Action a : actions) {
            long target = -1;
            if (a instanceof ActionIf) {
                ActionIf aIf = (ActionIf) a;
                target = aIf.getAddress() + getTotalActionLength(a) + aIf.getJumpOffset();
            } else if (a instanceof ActionJump) {
                ActionJump aJump = (ActionJump) a;
                target = aJump.getAddress() + getTotalActionLength(a) + aJump.getJumpOffset();
            } else if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                int storeSize = aStore.getStoreSize();
                // skip storeSize + 1 actions (+1 is the current action)
                Action targetAction = a;
                for (int i = 0; i <= storeSize; i++) {
                    long address = targetAction.getAddress() + getTotalActionLength(targetAction);
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

    private static void getContainerLastActions(List<Action> actionMap, Map<Action, List<Action>> lastActions) {
        for (int i = 0; i < actionMap.size(); i++) {
            Action a = actionMap.get(i);
            if (a == null) {
                continue;
            }

            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                List<Long> sizes = container.getContainerSizes();
                long endAddress = a.getAddress() + container.getHeaderSize();
                List<Action> lasts = new ArrayList<>(sizes.size());
                for (long size : sizes) {
                    endAddress += size;
                    int lastActionIndex = getPrevNotNullIndex(actionMap, (int) (endAddress - 1));
                    Action lastAction = null;
                    if (lastActionIndex != -1) {
                        lastAction = actionMap.get(lastActionIndex);
                    }
                    lasts.add(lastAction);
                }
                lastActions.put(a, lasts);
            }
        }
    }

    private static long updateAddresses(List<Action> actions, long address, int version) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            a.setAddress(address, version);
            int length = a.getBytes(version).length;
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                // placeholder for jump action
                length = getTotalActionLength(new ActionJump(0));
            }
            address += length;
        }
        return address;
    }

    private static void updateActionLengths(List<Action> actions, int version) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            int length = a.getBytes(version).length;
            a.actionLength = length - 1 - ((a.actionCode >= 0x80) ? 2 : 0);
        }
    }

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
                    long address = a1.getAddress() + getTotalActionLength(a1);
                    a1 = actionMap.get(address);
                    if (a1 == null || a1 == nextActionAfterStore) {
                        break;
                    }
                    actions.remove(a1);
                    store.add(a1);
                }
                aStore.setStore(store);
            }
        }
    }

    private static void updateContainerSizes(List<Action> actions, Map<Action, List<Action>> containerLastActions) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                List<Action> lastActions = containerLastActions.get(a);
                long startAddress = a.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    Action lastAction = lastActions.get(j);
                    int length = (int) (lastAction.getAddress() + getTotalActionLength(lastAction) - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }
        }
    }

    private static void replaceJumpTargets(Map<Action, Action> jumps, Action oldTarget, Action newTarget) {
        for (Action a : jumps.keySet()) {
            if (jumps.get(a) == oldTarget) {
                jumps.put(a, newTarget);
            }
        }
    }

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

    private static void updateJumps(List<Action> actions, Map<Action, Action> jumps, Map<Action, List<Action>> containerLastActions, long endAddress, int version) {
        if (actions.isEmpty()) {
            return;
        }

        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                ActionJump aJump = new ActionJump(0);
                aJump.setJumpOffset((int) (endAddress - a.getAddress() - getTotalActionLength(aJump)));
                aJump.setAddress(a.getAddress(), version);
                replaceJumpTargets(jumps, a, aJump);
                replaceContainerLastActions(containerLastActions, a, aJump);
                a = aJump;
                actions.set(i, a);
            } else if (a instanceof ActionIf) {
                ActionIf aIf = (ActionIf) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - getTotalActionLength(a);
                } else {
                    offset = endAddress - a.getAddress() - getTotalActionLength(a);
                }
                aIf.setJumpOffset((int) offset);
            } else if (a instanceof ActionJump) {
                ActionJump aJump = (ActionJump) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - getTotalActionLength(a);
                } else {
                    offset = endAddress - a.getAddress() - getTotalActionLength(a);
                }
                aJump.setJumpOffset((int) offset);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Action readActionListAtPos(List<DisassemblyListener> listeners, long containerSWFOffset, ConstantPool cpool,
            SWFInputStream sis, ReReadableInputStream rri,
            List<Action> actions, List<Long> nextOffsets,
            long ip, long startIp, long endIp, int version, String path, boolean indeterminate, List<Long> visitedContainers) throws IOException {

        Action entryAction = null;

        if (visitedContainers.contains(ip)) {
            return null;
        }
        visitedContainers.add(ip);

        Queue<Long> jumpQueue = new LinkedList<>();
        jumpQueue.add(ip);
        while (!jumpQueue.isEmpty()) {
            ip = jumpQueue.remove();
            while ((endIp == -1) || (endIp > ip)) {
                rri.setPos((int) ip);

                Action a;
                if ((a = sis.readAction(rri, cpool)) == null) {
                    break;
                }

                int actionLengthWithHeader = getTotalActionLength(a);

                // unknown action, replace with jump
                if (a instanceof ActionNop) {
                    ActionJump aJump = new ActionJump(0);
                    int jumpLength = getTotalActionLength(aJump);
                    aJump.setAddress(a.getAddress(), version);
                    aJump.setJumpOffset(actionLengthWithHeader - jumpLength);
                    a = aJump;
                    actionLengthWithHeader = getTotalActionLength(a);
                }

                if (entryAction == null) {
                    entryAction = a;
                }

                ensureCapacity(actions, nextOffsets, ip);

                Action existingAction = actions.get((int) ip);
                if (existingAction != null) {
                    break;
                }

                actions.set((int) ip, a);
                nextOffsets.set((int) ip, ip + actionLengthWithHeader);

                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).progress("Reading", rri.getCount(), rri.length());
                }

                a.containerSWFOffset = containerSWFOffset;
                a.setAddress(ip, version, false);

                if (a instanceof ActionPush && cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                } else if (a instanceof ActionConstantPool) {
                    if (cpool == null) {
                        cpool = new ConstantPool();
                    }
                    cpool.setNew(((ActionConstantPool) a).constantPool);
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
                            //long endIp2 = ip + actionLengthWithHeader + size;
                            readActionListAtPos(listeners, containerSWFOffset, cpool,
                                    sis, rri,
                                    actions, nextOffsets,
                                    ip2, startIp, endIp, version, newPath, indeterminate, visitedContainers);
                            actionLengthWithHeader += size;
                        }
                    }
                }

                ip = ip + actionLengthWithHeader;

                if (a.isExit()) {
                    break;
                }
            }
        }
        return entryAction;
    }

    private static int getTotalActionLength(Action action) {
        return action.actionLength + 1 + ((action.actionCode >= 0x80) ? 2 : 0);
    }

    private static void ensureCapacity(List<Action> actions, List<Long> nextOffsets, long index) {
        while (actions.size() <= index) {
            actions.add(null);
            nextOffsets.add(-1L);
        }
    }

    @SuppressWarnings("unchecked")
    private static void deobfustaceActionListAtPosRecursive(List<DisassemblyListener> listeners, List<GraphTargetItem> output, HashMap<Long, List<GraphSourceItemContainer>> containers, long containerSWFOffset, List<Object> localData, Stack<GraphTargetItem> stack, ConstantPool cpool, SWFInputStream sis, ReReadableInputStream rri, int ip, List<Action> ret, int startIp, int endip, String path, Map<Integer, Integer> visited, boolean indeterminate, Map<Integer, HashMap<String, GraphTargetItem>> decisionStates, int version) throws IOException {
        boolean debugMode = false;
        boolean decideBranch = false;

        boolean deobfuscate = Configuration.getConfig("autoDeobfuscate", true);
        rri.setPos(ip);
        Action a;
        long filePos = rri.getPos();
        Scanner sc = new Scanner(System.in, "utf-8");
        int prevIp = ip;
        loopip:
        while (((endip == -1) || (endip > ip)) && (a = sis.readAction(rri, cpool)) != null) {
            if (!visited.containsKey(ip)) {
                visited.put(ip, 0);
            }
            int curVisited = visited.get(ip);
            curVisited++;
            visited.put(ip, curVisited);
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progress("Deobfuscating", rri.getCount(), rri.length());
            }
            if ((ip < ret.size()) && (!(ret.get(ip) instanceof ActionNop))) {
                a = ret.get(ip);
            }
            a.containerSWFOffset = containerSWFOffset;
            a.setAddress(prevIp, version, false);
            int info = a.actionLength + 1 + ((a.actionCode >= 0x80) ? 2 : 0);

            if (a instanceof ActionPush) {
                if (cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                }
            }

            if (debugMode) {
                String atos = a.getASMSource(new ArrayList<GraphSourceItem>(), new ArrayList<Long>(), cpool.constants, version, false);
                if (a instanceof GraphSourceItemContainer) {
                    atos = a.toString();
                }
                System.err.println("readActionListAtPos ip: " + (ip - startIp) + " (0x" + Helper.formatAddress(ip - startIp) + ") " + " action(len " + a.actionLength + "): " + atos + (a.isIgnored() ? " (ignored)" : "") + " stack:" + Helper.stackToString(stack, Helper.toList(cpool)) + " " + Helper.byteArrToString(a.getBytes(version)));
                @SuppressWarnings("unchecked")
                HashMap<String, GraphTargetItem> vars = (HashMap<String, GraphTargetItem>) localData.get(1);
                System.err.print("variables: ");
                for (Map.Entry<String, GraphTargetItem> v : vars.entrySet()) {
                    System.err.print("'" + v + "' = " + v.getValue().toString(false, cpool) + ", ");
                }
                System.err.println();
                String add = "";
                if (a instanceof ActionIf) {
                    add = " change: " + ((ActionIf) a).getJumpOffset();
                }
                if (a instanceof ActionJump) {
                    add = " change: " + ((ActionJump) a).getJumpOffset();
                }
                System.err.println(add);
            }
            long newFilePos = rri.getPos();
            long actionLen = newFilePos - filePos;

            int newip = -1;

            if (a instanceof ActionConstantPool) {
                if (cpool == null) {
                    cpool = new ConstantPool();
                }
                cpool.setNew(((ActionConstantPool) a).constantPool);
            }
            ActionIf aif = null;
            boolean goaif = false;
            if (!a.isIgnored()) {
                String varname = null;
                if (a instanceof StoreTypeAction) {
                    StoreTypeAction sta = (StoreTypeAction) a;
                    varname = sta.getVariableName(stack, cpool);
                }

                try {
                    if (a instanceof ActionIf) {
                        aif = (ActionIf) a;

                        GraphTargetItem top = null;
                        if (deobfuscate) {
                            top = stack.pop();
                        }
                        int nip = (int) rri.getPos() + aif.getJumpOffset();

                        if (decideBranch) {
                            System.out.print("newip " + nip + ", ");
                            System.out.print("Action: jump(j),ignore(i),compute(c)?");
                            String next = sc.next();
                            if (next.equals("j")) {
                                newip = (int) rri.getPos() + aif.getJumpOffset();
                                rri.setPos(newip);

                            } else if (next.equals("i")) {
                            } else if (next.equals("c")) {
                                goaif = true;
                            }
                        } else if (deobfuscate && top.isCompileTime() && (!top.hasSideEffect())) {
                            ((ActionIf) a).compileTime = true;
                            if (debugMode) {
                                System.err.print("is compiletime -> ");
                            }
                            if (EcmaScript.toBoolean(top.getResult())) {
                                newip = (int) rri.getPos() + aif.getJumpOffset();
                                aif.jumpUsed = true;
                                if (aif.ignoreUsed) {
                                    aif.compileTime = false;
                                }
                                if (debugMode) {
                                    System.err.println("jump");
                                }
                            } else {
                                aif.ignoreUsed = true;
                                if (aif.jumpUsed) {
                                    aif.compileTime = false;
                                }
                                if (debugMode) {
                                    System.err.println("ignore");
                                }
                            }
                        } else {
                            if (debugMode) {
                                System.err.println("goaif");
                            }
                            goaif = true;
                        }
                    } else if (a instanceof ActionJump) {
                        newip = (int) rri.getPos() + ((ActionJump) a).getJumpOffset();
                    } else if (!(a instanceof GraphSourceItemContainer)) {
                        if (deobfuscate) {
                            //return in for..in,   TODO:Handle this better way
                            if (((a instanceof ActionEquals) || (a instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueActionItem)) {
                                stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<String>()));
                            }
                            if ((a instanceof ActionStoreRegister) && stack.isEmpty()) {
                                stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<String>()));
                            }
                            a.translate(localData, stack, output, Graph.SOP_USE_STATIC/*Graph.SOP_SKIP_STATIC*/, path);
                        }
                    }
                } catch (RuntimeException ex) {
                    log.log(Level.SEVERE, "Disassembly exception", ex);
                    break;
                }

                HashMap<String, GraphTargetItem> vars = (HashMap<String, GraphTargetItem>) localData.get(1);
                if (varname != null) {
                    GraphTargetItem varval = vars.get(varname);
                    if (varval != null && varval.isCompileTime() && indeterminate) {
                        vars.put(varname, new NotCompileTimeItem(null, varval));
                    }
                }
            }
            int nopos = -1;
            for (int i = 0; i < actionLen; i++) {
                if (a instanceof ActionNop) {
                    int prevPos = (int) a.getAddress();
                    a = new ActionNop();
                    a.setAddress(prevPos, version);
                    nopos++;
                    if (nopos > 0) {
                        a.setAddress(a.getAddress() + 1, version);
                    }

                }
                ret.set(ip + i, a);
            }

            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                if (a instanceof Action) {
                    long endAddr = a.getAddress() + cnt.getHeaderSize();
                    String cntName = cnt.getName();
                    List<List<GraphTargetItem>> output2s = new ArrayList<>();
                    for (long size : cnt.getContainerSizes()) {
                        if (size == 0) {
                            output2s.add(new ArrayList<GraphTargetItem>());
                            continue;
                        }
                        List<Object> localData2;
                        List<GraphTargetItem> output2 = new ArrayList<>();
                        if ((cnt instanceof ActionDefineFunction) || (cnt instanceof ActionDefineFunction2)) {
                            localData2 = Helper.toList(new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>());
                        } else {
                            localData2 = localData;
                        }
                        deobfustaceActionListAtPosRecursive(listeners, output2, containers, containerSWFOffset, localData2, new Stack<GraphTargetItem>(), cpool, sis, rri, (int) endAddr, ret, startIp, (int) (endAddr + size), path + (cntName == null ? "" : "/" + cntName), visited, indeterminate, decisionStates, version);
                        output2s.add(output2);
                        endAddr += size;
                    }
                    if (deobfuscate) {
                        cnt.translateContainer(output2s, stack, output, (HashMap<Integer, String>) localData.get(0), (HashMap<String, GraphTargetItem>) localData.get(1), (HashMap<String, GraphTargetItem>) localData.get(2));
                    }
                    ip = (int) endAddr;
                    prevIp = ip;
                    rri.setPos(ip);
                    filePos = rri.getPos();
                    continue;
                }
            }

            if (a instanceof ActionEnd) {
                break;
            }
            if (newip > -1) {
                ip = newip;
            } else {
                ip = ip + info;
            }
            rri.setPos(ip);
            filePos = rri.getPos();
            if (goaif) {
                aif.ignoreUsed = true;
                aif.jumpUsed = true;
                indeterminate = true;

                HashMap<String, GraphTargetItem> vars = (HashMap<String, GraphTargetItem>) localData.get(1);
                boolean stateChanged = false;
                if (decisionStates.containsKey(ip)) {
                    HashMap<String, GraphTargetItem> oldstate = decisionStates.get(ip);
                    if (oldstate.size() != vars.size()) {
                        stateChanged = true;
                    } else {
                        for (String k : vars.keySet()) {
                            if (!oldstate.containsKey(k)) {
                                stateChanged = true;
                                break;
                            }
                            if (!vars.get(k).isCompileTime() && oldstate.get(k).isCompileTime()) {
                                stateChanged = true;
                                break;
                            }
                        }
                    }
                }
                HashMap<String, GraphTargetItem> curstate = new HashMap<>();
                curstate.putAll(vars);
                decisionStates.put(ip, curstate);

                if ((!stateChanged) && curVisited > 1) {
                    List<Integer> branches = new ArrayList<>();
                    branches.add((int) rri.getPos() + aif.getJumpOffset());
                    branches.add((int) rri.getPos());
                    for (int br : branches) {
                        int visc = 0;
                        if (visited.containsKey(br)) {
                            visc = visited.get(br);
                        }
                        if (visc == 0) {//<curVisited){
                            ip = br;
                            prevIp = ip;
                            rri.setPos(br);
                            filePos = rri.getPos();
                            continue loopip;
                        }
                    }
                    break loopip;
                }

                int oldPos = (int) rri.getPos();
                @SuppressWarnings("unchecked")
                Stack<GraphTargetItem> substack = (Stack<GraphTargetItem>) stack.clone();
                deobfustaceActionListAtPosRecursive(listeners, output, containers, containerSWFOffset, prepareLocalBranch(localData), substack, cpool, sis, rri, (int) rri.getPos() + aif.getJumpOffset(), ret, startIp, endip, path, visited, indeterminate, decisionStates, version);
                rri.setPos(oldPos);
            }
            prevIp = ip;
            if (a.isExit()) {
                break;
            }
        }
        for (DisassemblyListener listener : listeners) {
            listener.progress("Deobfuscating", rri.getCount(), rri.length());
        }
    }

    private static List<Object> prepareLocalBranch(List<Object> localData) {
        @SuppressWarnings("unchecked")
        HashMap<Integer, String> regNames = (HashMap<Integer, String>) localData.get(0);
        @SuppressWarnings("unchecked")
        HashMap<String, GraphTargetItem> variables = (HashMap<String, GraphTargetItem>) localData.get(1);
        @SuppressWarnings("unchecked")
        HashMap<String, GraphTargetItem> functions = (HashMap<String, GraphTargetItem>) localData.get(2);
        List<Object> ret = new ArrayList<>();
        ret.add(new HashMap<>(regNames));
        ret.add(new HashMap<>(variables));
        ret.add(new HashMap<>(functions));
        return ret;
    }
}
