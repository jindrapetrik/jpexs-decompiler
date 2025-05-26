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
package com.jpexs.decompiler.flash.abc.avm2.fastavm2;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import java.util.Iterator;

/**
 * Iterator for FastAVM2List
 *
 * @author JPEXS
 */
public final class FastAVM2ListIterator implements Iterator<AVM2InstructionItem> {

    /**
     * Current item
     */
    private AVM2InstructionItem item;

    /**
     * List
     */
    private final FastAVM2List list;

    /**
     * If the iterator has been started
     */
    private boolean started = false;

    /**
     * Constructs a new iterator.
     *
     * @param list List
     */
    FastAVM2ListIterator(FastAVM2List list) {
        item = list.first();
        this.list = list;
    }

    /**
     * Constructs a new iterator.
     *
     * @param list List
     * @param index Index
     */
    FastAVM2ListIterator(FastAVM2List list, int index) {
        item = list.first();
        this.list = list;
        for (int i = 0; i < index; i++) {
            if (!hasNext()) {
                throw new Error("Invalid index");
            }

            next();
        }
    }

    /**
     * Checks if there is a next item.
     *
     * @return True if there is a next item
     */
    @Override
    public boolean hasNext() {
        return item != null && (!started || item != list.first());
    }

    /**
     * Gets the next item.
     *
     * @return Next item
     */
    @Override
    public AVM2InstructionItem next() {
        AVM2InstructionItem result = item;
        item = item.next;
        started = true;
        /*if (!list.contains(result)) {
         throw new Error();
         }*/

        return result;
    }

    /**
     * Gets the previous item.
     *
     * @return Previous item
     */
    public AVM2InstructionItem prev() {
        item = item.prev;
        if (item == list.first()) {
            started = false;
        }

        /*if (!list.contains(item)) {
         throw new Error();
         }*/
        return item;
    }

    /**
     * Sets the current item.
     *
     * @param item Item
     */
    public void setCurrent(AVM2InstructionItem item) {
        this.item = item;
        if (item == list.first()) {
            started = false;
        }
    }

    /**
     * Removes the current item.
     */
    @Override
    public void remove() {
        item = list.removeItem(item.prev);
    }

    /**
     * Adds an item.
     *
     * @param ins Instruction
     */
    public void add(AVM2Instruction ins) {
        item = list.insertItemAfter(item.prev, ins).next;
    }

    /**
     * Adds an item.
     *
     * @param insItem Instruction item
     */
    public void add(AVM2InstructionItem insItem) {
        item = list.insertItemAfter(item.prev, insItem).next;
    }

    /**
     * Adds an item before the current item.
     *
     * @param insItem Instruction item
     */
    public void addBefore(AVM2InstructionItem insItem) {
        list.insertItemBefore(item.prev, insItem);
    }

    /**
     * Get the item at the specified index.
     *
     * @param index Index
     * @return Item
     */
    public AVM2InstructionItem peek(int index) {
        AVM2InstructionItem item = this.item;
        for (int i = 0; i < index; i++) {
            if (item == null) {
                break;
            }

            item = item.next;
        }

        return item;
    }
}
