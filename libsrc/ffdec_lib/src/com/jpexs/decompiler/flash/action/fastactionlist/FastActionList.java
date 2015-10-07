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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class FastActionList implements Collection<ActionItem> {

    private int size;

    private ActionItem firstAction;

    private final Map<Action, ActionItem> actionItemMap;

    private final Set<ActionItem> actionItemSet;

    public FastActionList(ActionList actions) {
        actionItemMap = new HashMap<>(actions.size());
        actionItemSet = new HashSet<>(actions.size());
        for (Action action : actions) {
            insertItemAfter(null, action);
        }

        size = actions.size();
        getContainerLastActions(actions, actionItemMap);
        getJumps(actions, actionItemMap);
    }

    final ActionItem insertItemAfter(ActionItem item, Action action) {
        ActionItem newItem = new ActionItem(action);
        return insertItemAfter(item, newItem);
    }

    final ActionItem insertItemAfter(ActionItem item, ActionItem newItem) {
        if (item == null && firstAction == null) {
            firstAction = newItem;
            newItem.next = newItem;
            newItem.prev = newItem;
        } else {
            if (item == null) {
                // insert to the end
                item = firstAction.prev;
            }

            ActionItem oldNext = item.next;
            newItem.prev = item;
            newItem.next = oldNext;
            item.next = newItem;
            oldNext.prev = newItem;
        }

        size++;
        actionItemMap.put(newItem.action, newItem);
        actionItemSet.add(newItem);
        return newItem;
    }

    ActionItem removeItem(ActionItem item) {
        ActionItem next = null;
        if (item == firstAction) {
            if (item.next == item) {
                // there is only 1 item
                firstAction = null;
            } else {
                next = item.next;
                firstAction = next;
                next.prev = item.prev;
                item.prev.next = next;
            }
        } else {
            next = item.next;
            item.prev.next = next;
            next.prev = item.prev;
        }

        size--;
        actionItemMap.remove(item.action);
        actionItemSet.remove(item);
        return next;
    }

    private void getContainerLastActions(ActionList actions, Map<Action, ActionItem> actionItemMap) {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof GraphSourceItemContainer) {
                item.containerLastActions = getContainerLastActions(actions, action, actionItemMap);
            }

            item = item.next;
        } while (item != firstAction);
    }

    private List<ActionItem> getContainerLastActions(ActionList actions, Action action, Map<Action, ActionItem> actionItemMap) {
        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
        List<Long> sizes = container.getContainerSizes();
        long endAddress = action.getAddress() + container.getHeaderSize();
        List<ActionItem> lasts = new ArrayList<>(sizes.size());
        for (long size : sizes) {
            endAddress += size;
            long lastActionAddress = getNearAddress(actions, endAddress - 1, false);
            Action lastAction = null;
            if (lastActionAddress != -1) {
                lastAction = actions.getByAddress(lastActionAddress);
            }

            if (lastAction != null) {
                lasts.add(actionItemMap.get(lastAction));
            } else {
                lasts.add(null);
            }
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

    private void getJumps(ActionList actions, Map<Action, ActionItem> actionItemMap) {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            long target = -1;
            if (action instanceof ActionIf) {
                target = ((ActionIf) action).getTargetAddress();
            } else if (action instanceof ActionJump) {
                target = ((ActionJump) action).getTargetAddress();
            } else if (action instanceof ActionStore) {
                ActionStore aStore = (ActionStore) action;
                int storeSize = aStore.getStoreSize();
                // skip storeSize + 1 actions (+1 is the current action)
                Action targetAction = action;
                for (int i = 0; i <= storeSize; i++) {
                    long address = targetAction.getAddress() + targetAction.getTotalActionLength();
                    targetAction = actions.getByAddress(address);
                    if (targetAction == null) {
                        break;
                    }
                }

                item.jumpTarget = actionItemMap.get(targetAction);
            }
            if (target >= 0) {
                Action targetAction = actions.getByAddress(target);
                item.jumpTarget = actionItemMap.get(targetAction);
            }

            item = item.next;
        } while (item != firstAction);
    }

    private void updateActionAddressesAndLengths() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        long address = item.action.getAddress();
        do {
            Action action = item.action;
            action.setAddress(address);
            action.updateLength();
            address += action.getTotalActionLength();
            item = item.next;
        } while (item != firstAction);
    }

    private void updateJumps() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        long endAddress = item.prev.action.getAddress();
        do {
            Action action = item.action;
            if (action instanceof ActionIf) {
                ActionIf aIf = (ActionIf) action;
                Action target = item.jumpTarget == null ? null : item.jumpTarget.action;
                long offset;
                if (target != null) {
                    offset = target.getAddress() - action.getAddress() - action.getTotalActionLength();
                } else {
                    offset = endAddress - action.getAddress() - action.getTotalActionLength();
                }
                aIf.setJumpOffset((int) offset);
            } else if (action instanceof ActionJump) {
                ActionJump aJump = (ActionJump) action;
                Action target = item.jumpTarget == null ? null : item.jumpTarget.action;
                long offset;
                if (target != null) {
                    offset = target.getAddress() - action.getAddress() - action.getTotalActionLength();
                } else {
                    offset = endAddress - action.getAddress() - action.getTotalActionLength();
                }
                aJump.setJumpOffset((int) offset);
            }

            item = item.next;
        } while (item != firstAction);
    }

    private void updateActionStores(ActionList actionList) {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionStore) {
                ActionStore aStore = (ActionStore) action;
                Action nextActionAfterStore = item.jumpTarget == null ? null : item.jumpTarget.action;
                Action a1 = action;
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

            item = item.next;
        } while (item != firstAction);
    }

    private void updateContainerSizes() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                List<ActionItem> lastActions = item.containerLastActions;
                long startAddress = action.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    Action lastAction = lastActions.get(j).action;
                    int length = (int) (lastAction.getAddress() + lastAction.getTotalActionLength() - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }

            item = item.next;
        } while (item != firstAction);
    }

    public void expandPushes() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                if (push.values.size() > 1) {
                    for (int i = 1; i < push.values.size(); i++) {
                        Object value = push.values.get(i);
                        ActionPush newPush = new ActionPush(value);
                        newPush.constantPool = push.constantPool;
                        insertItemAfter(item, newPush);
                        item = item.next;
                    }

                    Object obj = push.values.get(0);
                    push.values.clear();
                    push.values.add(obj);
                }
            }

            item = item.next;
        } while (item != firstAction);
    }

    public void removeZeroJumps() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionJump) {
                if (item.jumpTarget == item.next && item.jumpTarget != firstAction) {
                    item = removeItem(item);
                    continue;
                }
            }

            item = item.next;
        } while (item != firstAction);
    }

    public void removeUnreachableActions() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        updateReachableFlags(null, null);

        do {
            if (item.reachable == 0) {
                item = removeItem(item);
                continue;
            }

            item = item.next;
        } while (item != firstAction);
    }

    private void clearReachableFlags() {
        ActionItem item = firstAction;
        if (item == null) {
            return;
        }

        do {
            item.reachable = 0;
            item = item.next;
        } while (item != firstAction);
    }

    private void updateReachableFlags(ActionItem jump, ActionItem jumpTarget) {
        if (firstAction == null) {
            return;
        }

        clearReachableFlags();

        firstAction.reachable = 1;
        boolean modified = true;
        while (modified) {
            modified = false;
            int i = 0;
            FastActionListIterator iterator = iterator();
            while (iterator.hasNext()) {
                ActionItem item = iterator.next();
                Action action = item.action;
                if (item.reachable == 1) {
                    item.reachable = 2;
                    modified = true;

                    if (item == jump) {
                        if (jumpTarget.reachable == 0) {
                            jumpTarget.reachable = 1;
                        }

                        continue;
                    }

                    if (!action.isExit() && !(action instanceof ActionJump) && item.next != null) {
                        if (item.next.reachable == 0) {
                            item.next.reachable = 1;
                        }
                    }

                    ActionItem target = item.jumpTarget;
                    if (target != null) {
                        if (target.reachable == 0) {
                            target.reachable = 1;
                        }
                    }

                    if (action instanceof GraphSourceItemContainer) {
                        for (ActionItem lastActionItem : item.containerLastActions) {
                            if (lastActionItem != null && lastActionItem.next != null && lastActionItem.next.reachable == 0) {
                                lastActionItem.next.reachable = 1;
                            }
                        }
                    }
                }
            }
        }
    }

    public ActionList updateActions() {
        List<Action> resultList = new ArrayList<>(size);
        ActionItem item = firstAction;
        if (item == null) {
            return new ActionList(resultList);
        }

        do {
            resultList.add(item.action);
            item = item.next;
        } while (item != firstAction);

        ActionList result = new ActionList(resultList);
        updateActionAddressesAndLengths();
        updateJumps();
        updateActionStores(result);
        updateContainerSizes();
        return result;
    }

    public ActionList toActionList() {
        return updateActions();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof ActionItem) {
            return actionItemSet.contains(o);
        } else if (o instanceof Action) {
            return actionItemMap.containsKey((Action) o);
        }

        return false;
    }

    @Override
    public FastActionListIterator iterator() {
        return new FastActionListIterator(firstAction, this);
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];

        ActionItem item = firstAction;
        if (item == null) {
            return result;
        }

        int i = 0;
        do {
            result[i] = item.action;
            item = item.next;
            i++;
        } while (item != firstAction);
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length != size) {
            a = (T[]) new ActionItem[size];
        }

        ActionItem item = firstAction;
        if (item == null) {
            return a;
        }

        int i = 0;
        do {
            a[i] = (T) item;
            item = item.next;
            i++;
        } while (item != firstAction);
        return null;
    }

    @Override
    public boolean add(ActionItem e) {
        insertItemAfter(null, e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        ActionItem item = null;
        if (o instanceof ActionItem) {
            item = (ActionItem) o;
        } else if (o instanceof Action) {
            item = actionItemMap.get(o);
        }

        if (item == null) {
            return false;
        }

        removeItem(item);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object c1 : c) {
            if (!contains(c1)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends ActionItem> c) {
        for (ActionItem c1 : c) {
            insertItemAfter(null, c1);
        }

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object c1 : c) {
            result |= remove(c1);
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        ActionItem item = firstAction;
        if (item == null) {
            return false;
        }

        boolean modified = false;
        do {
            if (!c.contains(item)) {
                item = removeItem(item);
                modified = true;
                continue;
            }

            item = item.next;
        } while (item != firstAction);
        return modified;
    }

    @Override
    public void clear() {
        firstAction = null;
        size = 0;
    }
}
