/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
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
 *
 * @author JPEXS
 */
public class FastAVM2List implements Collection<AVM2InstructionItem> {

    private int size;

    private AVM2InstructionItem firstItem;

    private final Map<AVM2Instruction, AVM2InstructionItem> actionItemMap;

    private final Set<AVM2InstructionItem> actionItemSet;

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
//        getContainerLastActions(avm2code, actionItemMap);
        getJumps(avm2code, actionItemMap);
    }

    public final AVM2InstructionItem insertItemBefore(AVM2InstructionItem item, AVM2Instruction action) {
        AVM2InstructionItem newItem = new AVM2InstructionItem(action);
        return insertItemBefore(item, newItem);
    }

    public final AVM2InstructionItem insertItemAfter(AVM2InstructionItem item, AVM2Instruction action) {
        AVM2InstructionItem newItem = new AVM2InstructionItem(action);
        return insertItemAfter(item, newItem);
    }

    public final AVM2InstructionItem insertItemBefore(AVM2InstructionItem item, AVM2InstructionItem newItem) {
        insertItemAfter(item.prev, newItem);
        if (item == firstItem) {
            firstItem = newItem;
        }

        return newItem;
    }

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

    public void removeItem(int index, int count) {
        FastAVM2ListIterator iterator = new FastAVM2ListIterator(this, index);
        for (int i = 0; i < count; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    public AVM2InstructionItem get(int index) {
        FastAVM2ListIterator iterator = new FastAVM2ListIterator(this, index);
        return iterator.next();
    }

    public void replaceJumpTargets(AVM2InstructionItem target, AVM2InstructionItem newTarget) {
        if (target.jumpsHere != null) {
            for (AVM2InstructionItem item : new ArrayList<>(target.jumpsHere)) {
                item.setJumpTarget(newTarget);
            }
        }
    }

//    private void getContainerLastActions(AVM2Code actions, Map<AVM2Instruction, AVM2InstructionItem> actionItemMap) {
//        AVM2InstructionItem item = firstItem;
//        if (item == null) {
//            return;
//        }
//
//        do {
//            AVM2Instruction action = item.ins;
//            if (action instanceof GraphSourceItemContainer) {
//                item.setContainerLastInstructions(getContainerLastActions(actions, action, actionItemMap));
//            }
//
//            item = item.next;
//        } while (item != firstItem);
//    }
//    private List<AVM2InstructionItem> getContainerLastActions(AVM2Code actions, AVM2Instruction action, Map<AVM2Instruction, AVM2InstructionItem> actionItemMap) {
//        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
//        List<Long> sizes = container.getContainerSizes();
//        long endAddress = action.getAddress() + container.getHeaderSize();
//        List<AVM2InstructionItem> lasts = new ArrayList<>(sizes.size());
//        for (long size : sizes) {
//            endAddress += size;
//            long lastActionAddress = getNearAddress(actions.code, endAddress - 1, false);
//            AVM2Instruction lastAction = null;
//            if (lastActionAddress != -1) {
//                lastAction = actions.getByAddress(lastActionAddress);
//            }
//
//            if (lastAction != null) {
//                lasts.add(actionItemMap.get(lastAction));
//            } else {
//                lasts.add(null);
//            }
//        }
//        return lasts;
//    }
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

    public AVM2InstructionItem getContainer(AVM2InstructionItem item) {
        while (!(item.ins instanceof GraphSourceItemContainer) && item != firstItem) {
            item = item.prev;
        }

        if (item.ins instanceof GraphSourceItemContainer) {
            return item;
        }

        return null;
    }

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

    public AVM2InstructionItem first() {
        return firstItem;
    }

    public AVM2InstructionItem last() {
        return firstItem == null ? null : firstItem.prev;
    }

    public void toMethodBody(MethodBody body) {
        updateActions(body);
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
        if (o instanceof AVM2InstructionItem) {
            return actionItemSet.contains(o);
        } else if (o instanceof AVM2Instruction) {
            return actionItemMap.containsKey((AVM2Instruction) o);
        }

        return false;
    }

    @Override
    public FastAVM2ListIterator iterator() {
        return new FastAVM2ListIterator(this);
    }

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

    @Override
    public boolean add(AVM2InstructionItem e) {
        insertItemAfter(null, e);
        return true;
    }

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
    public boolean addAll(Collection<? extends AVM2InstructionItem> c) {
        for (AVM2InstructionItem c1 : c) {
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

    @Override
    public void clear() {
        firstItem = null;
        size = 0;
    }
}
