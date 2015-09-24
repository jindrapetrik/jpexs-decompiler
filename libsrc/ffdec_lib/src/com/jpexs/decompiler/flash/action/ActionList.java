/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
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
 *
 * @author JPEXS
 */
public class ActionList extends ArrayList<Action> {

    public ActionList() {
    }

    public ActionList(Collection<Action> actions) {
        super(actions);
    }

    public void setActions(List<Action> list) {
        clear();
        addAll(list);
    }

    public void removeAction(int index) {
        ActionListReader.removeAction(this, index, SWF.DEFAULT_VERSION, true);
    }

    public void removeActions(List<Action> actionsToRemove) {
        ActionListReader.removeActions(this, actionsToRemove, SWF.DEFAULT_VERSION, true);
    }

    public void removeAction(int index, int count) {
        if (size() <= index + count - 1) {
            // Can't remove count elements, only size - index is available
            count = size() - index;
        }

        for (int i = 0; i < count; i++) {
            ActionListReader.removeAction(this, index, SWF.DEFAULT_VERSION, true);
        }
    }

    public void addAction(int index, Action action) {
        ActionListReader.addAction(this, index, action, SWF.DEFAULT_VERSION, false, false);
    }

    public void addActions(int index, List<Action> actions) {
        ActionListReader.addActions(this, index, actions, SWF.DEFAULT_VERSION);
    }

    public void fixActionList() {
        ActionListReader.fixActionList(this, null, SWF.DEFAULT_VERSION);
    }

    public List<Action> getContainerLastActions(Action action) {
        return ActionListReader.getContainerLastActions(this, action);
    }

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
                        long ref = aJump.getAddress() + aJump.getTotalActionLength() + aJump.getJumpOffset();
                        if (target.getAddress() == ref) {
                            return aJump;
                        }
                    } else if (a instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) a;
                        long ref = aIf.getAddress() + aIf.getTotalActionLength() + aIf.getJumpOffset();
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

    public void removeNops() {
        for (int i = 0; i < size(); i++) {
            if (get(i) instanceof ActionNop) {
                removeAction(i);
            }
        }
    }

    public Action getByAddress(long address) {
        int idx = getIndexByAddress(address);
        return idx == -1 ? null : get(idx);
    }

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

    public void saveToFile(String fileName) {
        File file = new File(fileName);
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            Action.actionsToString(new ArrayList<>(), 0, this, SWF.DEFAULT_VERSION, ScriptExportMode.PCODE, writer);
        } catch (IOException ex) {
            Logger.getLogger(ActionList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return Action.actionsToString(new ArrayList<>(), 0, this, SWF.DEFAULT_VERSION, ScriptExportMode.PCODE);
    }

    public String toSource() {
        try {
            return Action.actionsToSource(null, this, "");
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
