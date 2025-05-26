/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List of actions.
 *
 * @author JPEXS
 */
public class ActionList extends ArrayList<Action> {

    /**
     * Deobfuscation mode
     */
    public int deobfuscationMode;

    /**
     * File data
     */
    public byte[] fileData;

    /**
     * Charset - SWF version 5 or lower does not have UTF-8 charset
     */
    private String charset;

    /**
     * Constructs a new action list with the specified charset.
     *
     * @param charset Charset
     */
    public ActionList(String charset) {
        this.charset = charset;
    }

    /**
     * Gets the charset.
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Constructs a new action list with the specified actions and charset.
     *
     * @param actions Actions
     * @param charset Charset
     */
    public ActionList(Collection<Action> actions, String charset) {
        super(actions);
        this.charset = charset;
    }

    /**
     * Sets actions.
     *
     * @param list Actions
     */
    public void setActions(List<Action> list) {
        clear();
        addAll(list);
    }

    /**
     * Removes actions.
     *
     * @param actionsToRemove Actions to remove
     */
    public void removeActions(List<Action> actionsToRemove) {
        ActionListReader.removeActions(this, actionsToRemove, true);
    }

    /**
     * Removes action.
     *
     * @param index Index
     */
    public void removeAction(int index) {
        ActionListReader.removeAction(this, index, true);
    }

    /**
     * Removes action.
     *
     * @param index Index
     * @param count Count
     */
    public void removeAction(int index, int count) {
        if (size() <= index + count - 1) {
            // Can't remove count elements, only size - index is available
            count = size() - index;
        }

        for (int i = 0; i < count; i++) {
            ActionListReader.removeAction(this, index, true);
        }
    }

    /**
     * Adds action.
     *
     * @param index Index
     * @param action Action
     */
    public void addAction(int index, Action action) {
        ActionListReader.addAction(this, index, action, false, false);
    }

    /**
     * Adds actions.
     *
     * @param index Index
     * @param actions Actions
     */
    public void addActions(int index, List<Action> actions) {
        ActionListReader.addActions(this, index, actions);
    }

    /**
     * Fixes action list.
     */
    public void fixActionList() {
        ActionListReader.fixActionList(this, null);
    }

    /**
     * Gets container last actions.
     *
     * @param action Action
     * @return Container last actions
     */
    public List<Action> getContainerLastActions(Action action) {
        return ActionListReader.getContainerLastActions(this, action);
    }

    /**
     * Gets references for action.
     *
     * @param target Target
     * @return References
     */
    public Iterator<Action> getReferencesFor(final Action target) {
        return new Iterator<Action>() {
            private final Iterator<Action> iterator = ActionList.this.iterator();

            private Action action = getNext();

            @Override
            public boolean hasNext() {
                return action != null;
            }

            @Override
            public Action next() {
                Action a = action;
                action = getNext();
                return a;
            }

            private Action getNext() {
                while (iterator.hasNext()) {
                    Action a = iterator.next();
                    if (a instanceof ActionJump) {
                        ActionJump aJump = (ActionJump) a;
                        long ref = aJump.getTargetAddress();
                        if (target.getAddress() == ref) {
                            return aJump;
                        }
                    } else if (a instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) a;
                        long ref = aIf.getTargetAddress();
                        if (target.getAddress() == ref) {
                            return aIf;
                        }
                    } else if (a instanceof ActionStore) {
                        ActionStore aStore = (ActionStore) a;
                        int storeSize = aStore.getStoreSize();
                        int idx = indexOf(a);
                        int idx2 = indexOf(target);
                        if (idx != -1 && idx2 == idx + storeSize) {
                            return a;
                        }
                    } else if (a instanceof GraphSourceItemContainer) {
                        GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                        long ref = a.getAddress() + a.getTotalActionLength();
                        for (Long size : container.getContainerSizes()) {
                            ref += size;
                            if (target.getAddress() == ref) {
                                return a;
                            }
                        }
                    }
                }
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Gets constant pools.
     *
     * @return Constant pools
     */
    public Iterable<ActionConstantPool> getConstantPools() {
        return () -> new Iterator<ActionConstantPool>() {
            private final Iterator<Action> iterator = ActionList.this.iterator();

            private ActionConstantPool action = getNext();

            @Override
            public boolean hasNext() {
                return action != null;
            }

            @Override
            public ActionConstantPool next() {
                ActionConstantPool a = action;
                action = getNext();
                return a;
            }

            private ActionConstantPool getNext() {
                while (iterator.hasNext()) {
                    Action a = iterator.next();
                    if (a instanceof ActionConstantPool) {
                        return (ActionConstantPool) a;
                    }
                }

                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Gets pushes.
     *
     * @return Pushes
     */
    public Iterable<ActionPush> getPushes() {
        return () -> new Iterator<ActionPush>() {
            private final Iterator<Action> iterator = ActionList.this.iterator();

            private ActionPush action = getNext();

            @Override
            public boolean hasNext() {
                return action != null;
            }

            @Override
            public ActionPush next() {
                ActionPush a = action;
                action = getNext();
                return a;
            }

            private ActionPush getNext() {
                while (iterator.hasNext()) {
                    Action a = iterator.next();
                    if (a instanceof ActionPush) {
                        return (ActionPush) a;
                    }
                }

                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Gets constant pool index reference count.
     *
     * @param index Index
     * @return Constant pool index reference count
     */
    public int getConstantPoolIndexReferenceCount(int index) {
        int count = 0;
        for (Action action : this) {
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                for (Object value : push.values) {
                    if (value instanceof ConstantIndex) {
                        ConstantIndex constantIndex = (ConstantIndex) value;
                        if (constantIndex.index == index) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    /**
     * Gets inline constant pool string.
     *
     * @param index Index
     * @param str String
     */
    public void inlineConstantPoolString(int index, String str) {
        for (ActionPush push : getPushes()) {
            for (int i = 0; i < push.values.size(); i++) {
                Object value = push.values.get(i);
                if (value instanceof ConstantIndex) {
                    ConstantIndex constantIndex = (ConstantIndex) value;
                    if (constantIndex.index == index) {
                        push.values.set(i, str);
                    }
                }
            }
        }
    }

    /**
     * Removes non-referenced constant pool items.
     */
    public void removeNonReferencedConstantPoolItems() {
        int maxSize = 0;
        for (ActionConstantPool constantPool : getConstantPools()) {
            maxSize = Math.max(maxSize, constantPool.constantPool.size());
        }

        boolean[] used = new boolean[maxSize];

        for (ActionPush push : getPushes()) {
            for (int i = 0; i < push.values.size(); i++) {
                Object value = push.values.get(i);
                if (value instanceof ConstantIndex) {
                    ConstantIndex constantIndex = (ConstantIndex) value;
                    int index = constantIndex.index;
                    if (index >= 0 && index < maxSize) {
                        used[index] = true;
                    }
                }
            }
        }

        int newIdx = 0;
        for (int i = 0; i < maxSize; i++) {
            if (used[i]) {
                if (i != newIdx) {
                    for (ActionPush push : getPushes()) {
                        for (int j = 0; j < push.values.size(); j++) {
                            Object value = push.values.get(j);
                            if (value instanceof ConstantIndex) {
                                ConstantIndex constantIndex = (ConstantIndex) value;
                                if (constantIndex.index == i) {
                                    constantIndex.index = newIdx;
                                }
                            }
                        }
                    }
                }

                newIdx++;
            } else {
                for (ActionConstantPool constantPool : getConstantPools()) {
                    if (constantPool.constantPool.size() > newIdx) {
                        constantPool.constantPool.remove(newIdx);
                    }
                }
            }
        }
    }

    /**
     * Removes nops.
     */
    public void removeNops() {
        for (int i = 0; i < size(); i++) {
            if (get(i) instanceof ActionNop) {
                removeAction(i);
            }
        }
    }

    /**
     * Gets action by address.
     *
     * @param address Address
     * @return Action
     */
    public Action getByAddress(long address) {
        int idx = getIndexByAddress(address);
        return idx == -1 ? null : get(idx);
    }

    /**
     * Gets index by action.
     *
     * @param action Action
     * @return Index
     */
    public int getIndexByAction(Action action) {
        return getIndexByAddress(action.getAddress());
    }

    /**
     * Gets index by address.
     *
     * @param address Address
     * @return Index
     */
    public int getIndexByAddress(long address) {
        int min = 0;
        int max = size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = get(mid).getAddress();
            if (midValue == address) {
                return mid;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return -1;
    }

    /**
     * Gets container.
     *
     * @param idx Index
     * @return Container
     */
    public GraphSourceItemContainer getContainer(int idx) {
        Action action = get(idx);
        int i = idx - 1;
        while (i >= 0) {
            Action a = get(i);
            if (a instanceof GraphSourceItemContainer) {
                List<Action> lastActions = getContainerLastActions(a);
                Action lastAction = lastActions.get(lastActions.size() - 1);
                if (lastAction.getAddress() >= action.getAddress()) {
                    return (GraphSourceItemContainer) a;
                }
            }

            i--;
        }

        return null;
    }

    /**
     * Gets container end index.
     *
     * @param idx Index
     * @return Container end index
     */
    public int getContainerEndIndex(int idx) {
        Action action = get(idx);
        int i = idx - 1;
        while (i >= 0) {
            Action a = get(i);
            if (a instanceof GraphSourceItemContainer) {
                List<Action> lastActions = getContainerLastActions(a);
                Action lastAction = lastActions.get(lastActions.size() - 1);
                if (lastAction.getAddress() >= action.getAddress()) {
                    return getIndexByAddress(lastAction.getAddress());
                }
            }

            i--;
        }

        return -1;
    }

    /**
     * Gets unreachable actions.
     *
     * @return Unreachable actions
     */
    public List<Action> getUnreachableActions() {
        int[] isReachable = getUnreachableActionsMap(-1, 0);
        List<Action> unreachableActions = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            if (isReachable[i] == 0) {
                unreachableActions.add(get(i));
            }
        }

        if (unreachableActions.isEmpty()) {
            unreachableActions = null;
        }

        return unreachableActions;
    }

    /**
     * Gets unreachable actions.
     *
     * @param jumpIndex Jump index
     * @param jumpTargetIndex Jump target index
     * @return Unreachable actions
     */
    public List<Action> getUnreachableActions(int jumpIndex, int jumpTargetIndex) {
        int[] isReachable = getUnreachableActionsMap(jumpIndex, jumpTargetIndex);
        isReachable[jumpIndex] = 0;
        List<Action> unreachableActions = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            if (isReachable[i] == 0) {
                unreachableActions.add(get(i));
            }
        }

        if (unreachableActions.isEmpty()) {
            unreachableActions = null;
        }

        return unreachableActions;
    }

    /**
     * Gets unreachable actions map.
     *
     * @param jumpIndex Jump index
     * @param jumpTargetIndex Jump target index
     * @return Unreachable actions array - ip to reachable
     */
    private int[] getUnreachableActionsMap(int jumpIndex, int jumpTargetIndex) {
        int size = size();

        // one item for each action. 1 means reachable, 2 means reachable and processed
        int[] isReachable = new int[size];
        isReachable[0] = 1;
        boolean modified = true;
        while (modified) {
            modified = false;
            for (int i = 0; i < size; i++) {
                Action action = get(i);
                if (isReachable[i] == 1) {
                    isReachable[i] = 2;
                    modified = true;

                    if (i == jumpIndex) {
                        if (isReachable[jumpTargetIndex] == 0) {
                            isReachable[jumpTargetIndex] = 1;
                        }

                        continue;
                    }

                    if (!action.isExit() && !(action instanceof ActionJump) && i != size - 1) {
                        if (isReachable[i + 1] == 0) {
                            isReachable[i + 1] = 1;
                        }
                    }

                    if (action instanceof ActionJump) {
                        ActionJump aJump = (ActionJump) action;
                        long ref = aJump.getTargetAddress();
                        int targetIndex = getIndexByAddress(ref);
                        if (targetIndex != -1 && isReachable[targetIndex] == 0) {
                            isReachable[targetIndex] = 1;
                        }
                    } else if (action instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) action;
                        long ref = aIf.getTargetAddress();
                        int targetIndex = getIndexByAddress(ref);
                        if (targetIndex != -1 && isReachable[targetIndex] == 0) {
                            isReachable[targetIndex] = 1;
                        }
                    } else if (action instanceof ActionStore) {
                        ActionStore aStore = (ActionStore) action;
                        int storeSize = aStore.getStoreSize();
                        if (size > i + storeSize) {
                            int targetIndex = i + storeSize;
                            if (isReachable[targetIndex] == 0) {
                                isReachable[targetIndex] = 1;
                            }
                        }
                    } else if (action instanceof GraphSourceItemContainer) {
                        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                        long ref = action.getAddress() + action.getTotalActionLength();
                        for (Long containerSize : container.getContainerSizes()) {
                            ref += containerSize;
                            int targetIndex = getIndexByAddress(ref);
                            if (targetIndex != -1 && isReachable[targetIndex] == 0) {
                                isReachable[targetIndex] = 1;
                            }
                        }
                    }
                }
            }
        }

        return isReachable;
    }

    /**
     * Combines pushes.
     */
    public void combinePushes() {
        for (int i = 0; i < size() - 1; i++) {
            Action action = get(i);
            Action action2 = get(i + 1);
            if (action instanceof ActionPush && action2 instanceof ActionPush) {
                if (!getReferencesFor(action2).hasNext()) {
                    ActionPush push = (ActionPush) action;
                    ActionPush push2 = (ActionPush) action2;
                    if (!(push.constantPool != null && push2.constantPool != null && push.constantPool != push2.constantPool)) {
                        ActionPush newPush = new ActionPush(0, charset);
                        newPush.constantPool = push.constantPool == null ? push2.constantPool : push.constantPool;
                        newPush.values.clear();
                        newPush.values.addAll(push.values);
                        newPush.values.addAll(push2.values);
                        addAction(i + 1, newPush);
                        removeAction(i + 2);
                        removeAction(i);
                        i--;
                    }
                }
            }
        }
    }

    /**
     * Expands pushes.
     */
    public void expandPushes() {
        for (int i = 0; i < size(); i++) {
            Action action = get(i);
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                if (push.values.size() > 1) {
                    int j = 0;
                    for (Object value : push.values) {
                        j++;
                        ActionPush newPush = new ActionPush(value, charset);
                        newPush.constantPool = push.constantPool;
                        addAction(i + j, newPush);
                    }

                    removeAction(i);
                    i += j - 1;
                }
            }
        }
    }

    /**
     * Saves to file.
     *
     * @param fileName File name
     */
    public void saveToFile(String fileName) {
        File file = new File(fileName);
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            Action.actionsToString(new ArrayList<>(), 0, this, SWF.DEFAULT_VERSION, ScriptExportMode.PCODE, writer);
        } catch (IOException ex) {
            Logger.getLogger(ActionList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Converts to string.
     *
     * @return String
     */
    @Override
    public String toString() {
        HighlightedTextWriter writer = new HighlightedTextWriter(new CodeFormatting(), false);
        Action.actionsToString(new ArrayList<>(), 0, this, SWF.DEFAULT_VERSION, ScriptExportMode.PCODE, writer);
        writer.finishHilights();
        return writer.toString();
    }
}
