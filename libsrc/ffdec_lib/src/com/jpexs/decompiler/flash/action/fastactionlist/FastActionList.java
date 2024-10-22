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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.special.ActionUnknown;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.helpers.CancellableWorker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fast action list implementation.
 *
 * @author JPEXS
 */
public class FastActionList implements Collection<ActionItem> {

    /**
     * Number of actions in the list.
     */
    private int size;

    /**
     * First action item in the list.
     */
    private ActionItem firstItem;

    /**
     * Map of actions to action items.
     */
    private final Map<Action, ActionItem> actionItemMap;

    /**
     * Set of action items.
     */
    private final Set<ActionItem> actionItemSet;

    /**
     * Charset of the actions.
     */
    private String charset;

    /**
     * Constructs a new fast action list.
     *
     * @param actions Action list
     */
    public FastActionList(ActionList actions) {
        actionItemMap = new HashMap<>(actions.size());
        actionItemSet = new HashSet<>(actions.size());
        for (Action action : actions) {
            insertItemAfter(null, action);
        }

        size = actions.size();
        getContainerLastActions(actions, actionItemMap);
        getJumps(actions, actionItemMap);
        charset = actions.getCharset();
    }

    /**
     * Gets the charset of the actions.
     *
     * @return Charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Inserts an action before the specified item.
     *
     * @param item Item
     * @param action Action
     * @return New action item
     */
    public final ActionItem insertItemBefore(ActionItem item, Action action) {
        ActionItem newItem = new ActionItem(action);
        return insertItemBefore(item, newItem);
    }

    /**
     * Inserts an action before the specified item.
     *
     * @param item Item
     * @param newItem New action item
     * @return New action item
     */
    public final ActionItem insertItemBefore(ActionItem item, ActionItem newItem) {
        insertItemAfter(item.prev, newItem);
        if (item == firstItem) {
            firstItem = newItem;
        }

        return newItem;
    }

    /**
     * Inserts an action after the specified item.
     *
     * @param item Item
     * @param action Action
     * @return New action item
     */
    public final ActionItem insertItemAfter(ActionItem item, Action action) {
        ActionItem newItem = new ActionItem(action);
        return insertItemAfter(item, newItem);
    }

    /**
     * Inserts an action after the specified item.
     *
     * @param item Item
     * @param newItem New action item
     * @return New action item
     */
    public final ActionItem insertItemAfter(ActionItem item, ActionItem newItem) {
        if (item == null && firstItem == null) {
            firstItem = newItem;
            newItem.next = newItem;
            newItem.prev = newItem;
        } else {
            if (item == null) {
                // insert to the end
                item = firstItem.prev;
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

    /**
     * Removes the specified item.
     *
     * @param item Item
     * @return Next item
     */
    public ActionItem removeItem(ActionItem item) {
        ActionItem next = null;
        if (item == firstItem) {
            if (item.next == item) {
                // there is only 1 item
                firstItem = null;
            } else {
                next = item.next;
                firstItem = next;
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

        item.removeJumpTarget();
        item.removeContainerLastActions();

        if (item.jumpsHere != null) {
            for (ActionItem item1 : new ArrayList<>(item.jumpsHere)) {
                item1.setJumpTarget(item.next);
            }
        }

        if (item.lastActionOf != null) {
            for (ActionItem item1 : new ArrayList<>(item.lastActionOf)) {
                item1.replaceContainerLastAction(item, item.prev);
            }
        }

        return next;
    }

    /**
     * Removes the item(s) at the specified index.
     *
     * @param index Index
     * @param count Count
     */
    public void removeItem(int index, int count) {
        FastActionListIterator iterator = new FastActionListIterator(this, index);
        for (int i = 0; i < count; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Gets the item at the specified index.
     *
     * @param index Index
     * @return Item
     */
    public ActionItem get(int index) {
        FastActionListIterator iterator = new FastActionListIterator(this, index);
        return iterator.next();
    }

    /**
     * Replaces the jump targets.
     *
     * @param target Target
     * @param newTarget New target
     */
    public void replaceJumpTargets(ActionItem target, ActionItem newTarget) {
        if (target.jumpsHere != null) {
            for (ActionItem item : new ArrayList<>(target.jumpsHere)) {
                item.setJumpTarget(newTarget);
            }
        }
    }

    /**
     * Gets the last actions.
     *
     * @param actions Action list
     * @param actionItemMap Action item map
     */
    private void getContainerLastActions(ActionList actions, Map<Action, ActionItem> actionItemMap) {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof GraphSourceItemContainer) {
                item.setContainerLastActions(getContainerLastActions(actions, action, actionItemMap));
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Gets the last actions of a container.
     *
     * @param actions Action list
     * @param action Action
     * @param actionItemMap Action item map
     * @return Last actions
     */
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

    /**
     * Gets the nearest address.
     *
     * @param actions Action list
     * @param address Address
     * @param next Next
     * @return Address
     */
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

    /**
     * Gets the jumps.
     *
     * @param actions Action list
     * @param actionItemMap Action item map
     */
    private void getJumps(ActionList actions, Map<Action, ActionItem> actionItemMap) {
        ActionItem item = firstItem;
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

                item.setJumpTarget(actionItemMap.get(targetAction));
            }
            if (target >= 0) {
                Action targetAction = actions.getByAddress(target);
                item.setJumpTarget(actionItemMap.get(targetAction));
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates the action addresses and lengths.
     */
    private void updateActionAddressesAndLengths() {
        ActionItem item = firstItem;
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
        } while (item != firstItem);
    }

    /**
     * Updates the jumps.
     */
    private void updateJumps() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        long endAddress = item.prev.action.getAddress();
        do {
            Action action = item.action;
            if (action instanceof ActionIf) {
                ActionIf aIf = (ActionIf) action;
                Action target = item.getJumpTargetAction();
                long offset;
                if (target != null) {
                    offset = target.getAddress() - action.getAddress() - action.getTotalActionLength();
                } else {
                    offset = endAddress - action.getAddress() - action.getTotalActionLength();
                }
                aIf.setJumpOffset((int) offset);
            } else if (action instanceof ActionJump) {
                ActionJump aJump = (ActionJump) action;
                Action target = item.getJumpTargetAction();
                long offset;
                if (target != null) {
                    offset = target.getAddress() - action.getAddress() - action.getTotalActionLength();
                } else {
                    offset = endAddress - action.getAddress() - action.getTotalActionLength();
                }
                aJump.setJumpOffset((int) offset);
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates the action stores.
     */
    private void updateActionStores() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionStore) {
                ActionStore aStore = (ActionStore) action;
                Action nextActionAfterStore = item.getJumpTargetAction();
                ActionItem item1 = item;
                List<Action> store = new ArrayList<>();
                while (true) {
                    item1 = item1.next;
                    if (item1 == firstItem || item1.action == nextActionAfterStore) {
                        break;
                    }

                    store.add(item1.action);
                }

                aStore.setStore(store);
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates the container sizes.
     */
    private void updateContainerSizes() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                List<ActionItem> lastActions = item.getContainerLastActions();
                long startAddress = action.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    Action lastAction = lastActions.get(j).action;
                    int length = (int) (lastAction.getAddress() + lastAction.getTotalActionLength() - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Gets the container.
     *
     * @param item Item
     * @return Container
     */
    public ActionItem getContainer(ActionItem item) {
        while (!(item.action instanceof GraphSourceItemContainer) && item != firstItem) {
            item = item.prev;
        }

        if (item.action instanceof GraphSourceItemContainer) {
            return item;
        }

        return null;
    }

    /**
     * Expands the pushes.
     */
    public void expandPushes() {
        ActionItem item = firstItem;
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
                        ActionPush newPush = new ActionPush(value, charset);
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
        } while (item != firstItem);
    }

    /**
     * Removes the unknown actions.
     */
    public void removeUnknownActions() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionUnknown) {
                item = removeItem(item);
                continue;
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Removes the zero jumps.
     */
    public void removeZeroJumps() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            Action action = item.action;
            if (action instanceof ActionJump) {
                if (item.getJumpTarget() == item.next && item.getJumpTarget() != firstItem) {
                    item = removeItem(item);
                    continue;
                }
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Removes the unreachable actions.
     */
    public void removeUnreachableActions() throws InterruptedException {
        ActionItem item = firstItem;
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
        } while (item != firstItem);
    }

    /**
     * Removes the included actions.
     */
    public void removeIncludedActions() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        while (true) {
            if (!item.excluded) {
                item = removeItem(item);
                continue;
            }

            item = item.next;
            if (item == firstItem) {
                break;
            }
        }
    }

    /**
     * Gets the unreachable action count.
     *
     * @param jump Jump
     * @param jumpTarget Jump target
     * @return Unreachable action count
     */
    public int getUnreachableActionCount(ActionItem jump, ActionItem jumpTarget) throws InterruptedException {
        ActionItem item = firstItem;
        if (item == null) {
            return 0;
        }

        updateReachableFlags(jump, jumpTarget);
        jump.reachable = 0;

        int count = 0;
        do {
            if (item.reachable == 0) {
                count++;
            }

            item = item.next;
        } while (item != firstItem);

        return count;
    }

    /**
     * Clears the reachable flags.
     */
    private void clearReachableFlags() {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            item.reachable = 0;
            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Sets the excluded flags.
     *
     * @param value Value
     */
    public void setExcludedFlags(boolean value) {
        ActionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            item.excluded = value;
            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates the reachable flags.
     *
     * @param jump Jump
     * @param jumpTarget Jump target
     */
    private void updateReachableFlags(ActionItem jump, ActionItem jumpTarget) throws InterruptedException {
        if (firstItem == null) {
            return;
        }

        clearReachableFlags();

        firstItem.reachable = 1;
        ActionItem firstItem2 = firstItem;
        boolean modified = true;
        while (modified) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            modified = false;
            ActionItem item = firstItem2;
            do {                
                ActionItem next = item.next;
                //ActionItem alternativeNext = null;
                Action action = item.action;
                if (item.reachable == 1) {
                    item.reachable = 2;
                    modified = true;

                    if (item == firstItem2) {
                        firstItem2 = next;
                    }

                    if (item == jump) {
                        if (jumpTarget.reachable == 0) {
                            jumpTarget.reachable = 1;
                            //alternativeNext = jumpTarget;
                        }
                    } else {

                        if (!action.isExit() && !(action instanceof ActionJump)) {
                            if (next.reachable == 0) {
                                next.reachable = 1;
                            }
                        }

                        if (action instanceof GraphSourceItemContainer) {
                            for (ActionItem lastActionItem : item.getContainerLastActions()) {
                                if (lastActionItem != null && lastActionItem.next != null && lastActionItem.next.reachable == 0) {
                                    lastActionItem.next.reachable = 1;
                                    //alternativeNext = lastActionItem.next;
                                }
                            }
                        }

                        ActionItem target = item.getJumpTarget();
                        if (target != null) {
                            if (target.reachable == 0) {
                                target.reachable = 1;
                                //alternativeNext = target;
                            }
                        }
                    }
                }

                //item = alternativeNext == null || next.reachable == 1 ? next : alternativeNext;
                item = next;
            } while (item != firstItem);
        }
    }

    /**
     * Updates the actions.
     *
     * @return Action list
     */
    public ActionList updateActions() {
        List<Action> resultList = new ArrayList<>(size);
        ActionItem item = firstItem;
        if (item == null) {
            return new ActionList(resultList, charset);
        }

        do {
            resultList.add(item.action);
            item = item.next;
        } while (item != firstItem);

        ActionList result = new ActionList(resultList, charset);
        updateActionAddressesAndLengths();
        updateJumps();
        updateActionStores();
        updateContainerSizes();
        return result;
    }

    /**
     * Gets the first action.
     *
     * @return First action
     */
    public ActionItem first() {
        return firstItem;
    }

    /**
     * Gets the last action.
     *
     * @return Last action
     */
    public ActionItem last() {
        return firstItem == null ? null : firstItem.prev;
    }

    /**
     * Converts list to action list.
     *
     * @return Action list
     */
    public ActionList toActionList() {
        return updateActions();
    }

    /**
     * Gets the size of the list.
     *
     * @return Size
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checks if the list is empty.
     *
     * @return True if the list is empty, otherwise false
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if the list contains the specified element.
     *
     * @param o element whose presence in this collection is to be tested
     * @return True if the list contains the specified element, otherwise false
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof ActionItem) {
            return actionItemSet.contains(o);
        } else if (o instanceof Action) {
            return actionItemMap.containsKey((Action) o);
        }

        return false;
    }

    /**
     * Gets the iterator.
     *
     * @return Iterator
     */
    @Override
    public FastActionListIterator iterator() {
        return new FastActionListIterator(this);
    }

    /**
     * Converts the list to an array.
     *
     * @return Array
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];

        ActionItem item = firstItem;
        if (item == null) {
            return result;
        }

        int i = 0;
        do {
            result[i] = item.action;
            item = item.next;
            i++;
        } while (item != firstItem);
        return null;
    }

    /**
     * Converts the list to an array.
     *
     * @param the array into which the elements of this collection are to be
     * stored, if it is big enough; otherwise, a new array of the same runtime
     * type is allocated for this purpose.
     * @param <T> Type
     * @return Array
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length != size) {
            a = (T[]) new ActionItem[size];
        }

        ActionItem item = firstItem;
        if (item == null) {
            return a;
        }

        int i = 0;
        do {
            a[i] = (T) item;
            item = item.next;
            i++;
        } while (item != firstItem);
        return null;
    }

    /**
     * Adds an element to the list.
     *
     * @param e element whose presence in this collection is to be ensured
     * @return True
     */
    @Override
    public boolean add(ActionItem e) {
        insertItemAfter(null, e);
        return true;
    }

    /**
     * Removes the specified element from the list.
     *
     * @param o element to be removed from this collection, if present
     * @return True if the list contained the specified element, otherwise false
     */
    @Override
    public boolean remove(Object o) {
        ActionItem item = null;
        if (o instanceof ActionItem) {
            item = (ActionItem) o;
        } else if (o instanceof Action) {
            item = actionItemMap.get((Action) o);
        }

        if (item == null) {
            return false;
        }

        removeItem(item);
        return true;
    }

    /**
     * Checks if the list contains all elements of the specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return True if the list contains all elements of the specified
     * collection, otherwise false
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object c1 : c) {
            if (!contains(c1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds all elements of the specified collection to the list.
     *
     * @param c collection containing elements to be added to this collection
     * @return True
     */
    @Override
    public boolean addAll(Collection<? extends ActionItem> c) {
        for (ActionItem c1 : c) {
            insertItemAfter(null, c1);
        }

        return true;
    }

    /**
     * Removes all elements of the specified collection from the list.
     *
     * @param c collection containing elements to be removed from this
     * collection
     * @return True if the list contained all elements of the specified
     * collection, otherwise false
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object c1 : c) {
            result |= remove(c1);
        }

        return result;
    }

    /**
     * Retains only the elements in the list that are contained in the specified
     * collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return True if the list changed as a result of the call, otherwise false
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        ActionItem item = firstItem;
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
        } while (item != firstItem);
        return modified;
    }

    /**
     * Clears the list.
     */
    @Override
    public void clear() {
        firstItem = null;
        size = 0;
    }
}
