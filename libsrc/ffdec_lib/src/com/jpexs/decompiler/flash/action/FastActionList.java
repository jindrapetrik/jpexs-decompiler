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

import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class FastActionList {

    private LinkedList<Action> actions;

    private Map<Action, List<Action>> containerLastActions;

    private Map<Action, Action> jumps;

    public FastActionList(ActionList actions) {
        this.actions = new LinkedList<>(actions);

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(this.actions, actions, containerLastActions);
        this.containerLastActions = containerLastActions;

        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);
        this.jumps = jumps;
    }

    private void getContainerLastActions(LinkedList<Action> linkedActions, ActionList actions, Map<Action, List<Action>> lastActions) {
        for (Action a : linkedActions) {
            if (a instanceof GraphSourceItemContainer) {
                lastActions.put(a, getContainerLastActions(actions, a));
            }
        }
    }

    private List<Action> getContainerLastActions(ActionList actions, Action action) {
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

    private long getNearAddress(ActionList actions, long address, boolean next) {
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

    private static void getJumps(ActionList actions, Map<Action, Action> jumps) {
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
                    targetAction = actions.getByAddress(address);
                    if (targetAction == null) {
                        break;
                    }
                }
                jumps.put(a, targetAction);
            }
            if (target >= 0) {
                Action targetAction = actions.getByAddress(target);
                jumps.put(a, targetAction);
            }
        }
    }

    private void updateJumps(Map<Action, Action> jumps) {
        if (actions.isEmpty()) {
            return;
        }

        long endAddress = actions.getLast().getAddress();
        for (Action a : actions) {
            if (a instanceof ActionIf) {
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

    private void updateActionStores(ActionList actionList, Map<Action, Action> jumps) {
        for (Action a : actions) {
            if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                Action nextActionAfterStore = jumps.get(a);
                Action a1 = a;
                List<Action> store = new ArrayList<>();
                while (true) {
                    long address = a1.getAddress() + a1.getTotalActionLength();
                    a1 = actionList.getByAddress(address);
                    if (a1 == null || a1 == nextActionAfterStore) {
                        break;
                    }
                    store.add(a1);
                }
                aStore.setStore(store);
            }
        }
    }

    private void updateContainerSizes(Map<Action, List<Action>> containerLastActions) {
        for (Action a : actions) {
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

    public void expandPushes() {
        ListIterator<Action> iterator = actions.listIterator();
        while (iterator.hasNext()) {
            Action action = iterator.next();
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                if (push.values.size() > 1) {
                    for (int i = 1; i < push.values.size(); i++) {
                        Object value = push.values.get(i);
                        ActionPush newPush = new ActionPush(value);
                        newPush.constantPool = push.constantPool;
                        iterator.add(newPush);
                    }

                    Object obj = push.values.get(0);
                    push.values.clear();
                    push.values.add(obj);
                }
            }
        }
    }

    private void updateActionAddressesAndLengths() {
        long address = actions.get(0).getAddress();
        for (Action action : actions) {
            action.setAddress(address);
            action.updateLength();
            address += action.getTotalActionLength();
        }
    }

    public ActionList toActionList() {
        ActionList result = new ActionList(actions);
        updateActionAddressesAndLengths();
        updateJumps(jumps);
        updateActionStores(result, jumps);
        updateContainerSizes(containerLastActions);
        return result;
    }
}
