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
package com.jpexs.decompiler.flash.abc.avm2.fastavm2;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fast AVM2 instruction list.
 *
 * @author JPEXS
 */
public class FastAVM2List implements Collection<AVM2InstructionItem> {

    /**
     * Size of the list
     */
    private int size;

    /**
     * First item
     */
    private AVM2InstructionItem firstItem;

    /**
     * Map of instructions to items
     */
    private final Map<AVM2Instruction, AVM2InstructionItem> actionItemMap;

    /**
     * Set of items
     */
    private final Set<AVM2InstructionItem> actionItemSet;

    /**
     * Constructs a new FastAVM2List from a method body
     *
     * @param body Method body
     */
    public FastAVM2List(MethodBody body) {
        // exceptions todo
        AVM2Code avm2code = body.getCode();
        List<AVM2Instruction> code = avm2code.code;
        actionItemMap = new HashMap<>(code.size());
        actionItemSet = new HashSet<>(code.size());
        for (AVM2Instruction action : code) {
            insertItemAfter(null, action);
        }

        size = code.size();
        getJumps(avm2code, actionItemMap);
    }

    /**
     * Inserts item before another item
     *
     * @param item Item to insert before
     * @param action Action to insert
     * @return New item
     */
    public final AVM2InstructionItem insertItemBefore(AVM2InstructionItem item, AVM2Instruction action) {
        AVM2InstructionItem newItem = new AVM2InstructionItem(action);
        return insertItemBefore(item, newItem);
    }

    /**
     * Inserts item before another item
     *
     * @param item Item to insert before
     * @param newItem New item
     * @return New item
     */
    public final AVM2InstructionItem insertItemBefore(AVM2InstructionItem item, AVM2InstructionItem newItem) {
        insertItemAfter(item.prev, newItem);
        if (item == firstItem) {
            firstItem = newItem;
        }

        return newItem;
    }

    /**
     * Inserts item after another item
     *
     * @param item Item to insert after
     * @param action Action to insert
     * @return New item
     */
    public final AVM2InstructionItem insertItemAfter(AVM2InstructionItem item, AVM2Instruction action) {
        AVM2InstructionItem newItem = new AVM2InstructionItem(action);
        return insertItemAfter(item, newItem);
    }

    /**
     * Inserts item after another item
     *
     * @param item Item to insert after
     * @param newItem New item
     * @return New item
     */
    public final AVM2InstructionItem insertItemAfter(AVM2InstructionItem item, AVM2InstructionItem newItem) {
        if (item == null && firstItem == null) {
            firstItem = newItem;
            newItem.next = newItem;
            newItem.prev = newItem;
        } else {
            if (item == null) {
                // insert to the end
                item = firstItem.prev;
            }

            AVM2InstructionItem oldNext = item.next;
            newItem.prev = item;
            newItem.next = oldNext;
            item.next = newItem;
            oldNext.prev = newItem;
        }

        size++;
        actionItemMap.put(newItem.ins, newItem);
        actionItemSet.add(newItem);
        return newItem;
    }

    /**
     * Removes item
     *
     * @param item Item to remove
     * @return Next item
     */
    public AVM2InstructionItem removeItem(AVM2InstructionItem item) {
        AVM2InstructionItem next = null;
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
        actionItemMap.remove(item.ins);
        actionItemSet.remove(item);

        item.removeJumpTarget();
        item.removeContainerLastInstructions();

        if (item.jumpsHere != null) {
            for (AVM2InstructionItem item1 : new ArrayList<>(item.jumpsHere)) {
                item1.setJumpTarget(item.next);
            }
        }

        if (item.lastInsOf != null) {
            for (AVM2InstructionItem item1 : new ArrayList<>(item.lastInsOf)) {
                item1.replaceContainerLastInstruction(item, item.prev);
            }
        }

        return next;
    }

    /**
     * Removes item.
     *
     * @param index Index of item to remove
     * @param count Number of items to remove
     */
    public void removeItem(int index, int count) {
        FastAVM2ListIterator iterator = new FastAVM2ListIterator(this, index);
        for (int i = 0; i < count; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Gets item at index
     *
     * @param index Index
     * @return Item
     */
    public AVM2InstructionItem get(int index) {
        FastAVM2ListIterator iterator = new FastAVM2ListIterator(this, index);
        return iterator.next();
    }

    /**
     * Replace jump targets.
     *
     * @param target Target
     * @param newTarget New target
     */
    public void replaceJumpTargets(AVM2InstructionItem target, AVM2InstructionItem newTarget) {
        if (target.jumpsHere != null) {
            for (AVM2InstructionItem item : new ArrayList<>(target.jumpsHere)) {
                item.setJumpTarget(newTarget);
            }
        }
    }

    /**
     * Gets nerby address.
     *
     * @param instructions Instructions
     * @param address Address
     * @param next Next
     * @return Nearby address
     */
    private long getNearAddress(List<AVM2Instruction> instructions, long address, boolean next) {
        int min = 0;
        int max = instructions.size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = instructions.get(mid).getAddress();
            if (midValue == address) {
                return address;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return next
                ? (min < instructions.size() ? instructions.get(min).getAddress() : -1)
                : (max >= 0 ? instructions.get(max).getAddress() : -1);
    }

    /**
     * Gets jumps.
     *
     * @param actions Actions
     * @param actionItemMap Action item map
     */
    private void getJumps(AVM2Code actions, Map<AVM2Instruction, AVM2InstructionItem> actionItemMap) {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            AVM2Instruction action = item.ins;
            long target = -1;
            if (action.definition instanceof IfTypeIns) {
                target = action.getTargetAddress();
            } else if (action.definition instanceof LookupSwitchIns) {
                // todo
            }
            if (target >= 0) {
                AVM2Instruction targetAction = actions.adr2ins(target);
                item.setJumpTarget(actionItemMap.get(targetAction));
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates action addresses and lengths.
     */
    private void updateActionAddressesAndLengths() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        long offset = item.ins.getAddress();
        do {
            AVM2Instruction action = item.ins;
            action.setAddress(offset);
            offset += action.getBytesLength();
            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates jumps.
     */
    private void updateJumps() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        long endAddress = item.prev.ins.getAddress();
        do {
            AVM2Instruction action = item.ins;
            if (action.definition instanceof IfTypeIns) {
                AVM2Instruction target = item.getJumpTargetInstruction();
                long offset;
                if (target != null) {
                    offset = target.getAddress() - action.getAddress() - action.getBytesLength();
                } else {
                    offset = endAddress - action.getAddress() - action.getBytesLength();
                }
                action.setTargetOffset((int) offset);
            } else if (action.definition instanceof LookupSwitchIns) {
                // todo
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates container sizes.
     */
    private void updateContainerSizes() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            AVM2Instruction action = item.ins;
            if (action instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                List<AVM2InstructionItem> lastActions = item.getContainerLastInstructions();
                long startAddress = action.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    AVM2Instruction lastAction = lastActions.get(j).ins;
                    int length = (int) (lastAction.getAddress() + lastAction.getBytesLength() - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Gets container.
     *
     * @param item Item
     * @return Container
     */
    public AVM2InstructionItem getContainer(AVM2InstructionItem item) {
        while (!(item.ins instanceof GraphSourceItemContainer) && item != firstItem) {
            item = item.prev;
        }

        if (item.ins instanceof GraphSourceItemContainer) {
            return item;
        }

        return null;
    }

    /**
     * Removes zero jumps.
     */
    public void removeZeroJumps() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            AVM2Instruction ins = item.ins;
            if (ins.definition instanceof JumpIns) {
                if (item.getJumpTarget() == item.next && item.getJumpTarget() != firstItem) {
                    item = removeItem(item);
                    continue;
                }
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Removes unreachable actions.
     */
    public void removeUnreachableActions() {
        AVM2InstructionItem item = firstItem;
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
     * Removes included actions.
     */
    public void removeIncludedActions() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            if (!item.excluded) {
                item = removeItem(item);
                continue;
            }

            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Gets unreachable action count.
     *
     * @param jump Jump
     * @param jumpTarget Jump target
     * @return Unreachable action count
     */
    public int getUnreachableActionCount(AVM2InstructionItem jump, AVM2InstructionItem jumpTarget) {
        AVM2InstructionItem item = firstItem;
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
     * Clears reachable flags.
     */
    private void clearReachableFlags() {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            item.reachable = 0;
            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Sets excluded flags.
     *
     * @param value Value
     */
    public void setExcludedFlags(boolean value) {
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return;
        }

        do {
            item.excluded = value;
            item = item.next;
        } while (item != firstItem);
    }

    /**
     * Updates reachable flags.
     *
     * @param jump Jump
     * @param jumpTarget Jump target
     */
    private void updateReachableFlags(AVM2InstructionItem jump, AVM2InstructionItem jumpTarget) {
        if (firstItem == null) {
            return;
        }

        clearReachableFlags();

        firstItem.reachable = 1;
        AVM2InstructionItem firstItem2 = firstItem;
        boolean modified = true;
        while (modified) {
            modified = false;
            AVM2InstructionItem item = firstItem2;
            do {
                AVM2InstructionItem next = item.next;
                //AVM2InstructionItem alternativeNext = null;
                AVM2Instruction action = item.ins;
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

                        if (!action.isExit() && !(action.definition instanceof JumpIns)) {
                            if (next.reachable == 0) {
                                next.reachable = 1;
                            }
                        }

                        if (action instanceof GraphSourceItemContainer) {
                            for (AVM2InstructionItem lastActionItem : item.getContainerLastInstructions()) {
                                if (lastActionItem != null && lastActionItem.next != null && lastActionItem.next.reachable == 0) {
                                    lastActionItem.next.reachable = 1;
                                    //alternativeNext = lastActionItem.next;
                                }
                            }
                        }

                        AVM2InstructionItem target = item.getJumpTarget();
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
     * Updates actions.
     *
     * @param body Method body
     */
    public void updateActions(MethodBody body) {
        AVM2Code result = new AVM2Code(size);
        AVM2InstructionItem item = firstItem;
        if (item == null) {
            body.setCode(result);
            body.exceptions = new ABCException[0];
            return;
        }

        List<AVM2Instruction> resultList = result.code;
        do {
            resultList.add(item.ins);
            item = item.next;
        } while (item != firstItem);

        updateActionAddressesAndLengths();
        updateJumps();
        updateContainerSizes();
    }

    /**
     * Gets first item.
     *
     * @return First item
     */
    public AVM2InstructionItem first() {
        return firstItem;
    }

    /**
     * Gets last item.
     *
     * @return Last item
     */
    public AVM2InstructionItem last() {
        return firstItem == null ? null : firstItem.prev;
    }

    /**
     * Converts to method body.
     *
     * @param body Method body
     */
    public void toMethodBody(MethodBody body) {
        updateActions(body);
    }

    /**
     * Gets size.
     *
     * @return Size
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Checks if empty.
     *
     * @return Whether empty
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if contains.
     *
     * @param o element whose presence in this collection is to be tested
     * @return Whether contains
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof AVM2InstructionItem) {
            return actionItemSet.contains(o);
        } else if (o instanceof AVM2Instruction) {
            return actionItemMap.containsKey((AVM2Instruction) o);
        }

        return false;
    }

    /**
     * Gets iterator.
     *
     * @return Iterator
     */
    @Override
    public FastAVM2ListIterator iterator() {
        return new FastAVM2ListIterator(this);
    }

    /**
     * Converts to array.
     *
     * @return Array
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];

        AVM2InstructionItem item = firstItem;
        if (item == null) {
            return result;
        }

        int i = 0;
        do {
            result[i] = item.ins;
            item = item.next;
            i++;
        } while (item != firstItem);
        return null;
    }

    /**
     * Converts to array.
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
            a = (T[]) new AVM2InstructionItem[size];
        }

        AVM2InstructionItem item = firstItem;
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
     * Adds an element to this collection.
     *
     * @param e element whose presence in this collection is to be ensured
     * @return Whether added
     */
    @Override
    public boolean add(AVM2InstructionItem e) {
        insertItemAfter(null, e);
        return true;
    }

    /**
     * Removes a single instance of the specified element from this collection,
     *
     * @param o element to be removed from this collection, if present
     * @return Whether removed
     */
    @Override
    public boolean remove(Object o) {
        AVM2InstructionItem item = null;
        if (o instanceof AVM2InstructionItem) {
            item = (AVM2InstructionItem) o;
        } else if (o instanceof AVM2Instruction) {
            item = actionItemMap.get((AVM2Instruction) o);
        }

        if (item == null) {
            return false;
        }

        removeItem(item);
        return true;
    }

    /**
     * Checks whether this collection contains all of the elements in the
     * specified collection.
     *
     * @param c collection to be checked for containment in this collection
     * @return Whether contains all
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
     * Adds all of the elements in the specified collection to this collection.
     *
     * @param c collection containing elements to be added to this collection
     * @return Whether added all
     */
    @Override
    public boolean addAll(Collection<? extends AVM2InstructionItem> c) {
        for (AVM2InstructionItem c1 : c) {
            insertItemAfter(null, c1);
        }

        return true;
    }

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this
     * collection
     * @return Whether removed all
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
     * Retains only the elements in this collection that are contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this collection
     * @return Whether retained all
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        AVM2InstructionItem item = firstItem;
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
     * Clears this collection.
     */
    @Override
    public void clear() {
        firstItem = null;
        size = 0;
    }
}
