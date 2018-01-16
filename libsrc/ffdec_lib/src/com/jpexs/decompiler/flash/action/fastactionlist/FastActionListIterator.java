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
package com.jpexs.decompiler.flash.action.fastactionlist;

import com.jpexs.decompiler.flash.action.Action;
import java.util.Iterator;

/**
 *
 * @author JPEXS
 */
public final class FastActionListIterator implements Iterator<ActionItem> {

    private ActionItem item;

    private final FastActionList list;

    private boolean started = false;

    FastActionListIterator(FastActionList list) {
        item = list.first();
        this.list = list;
    }

    FastActionListIterator(FastActionList list, int index) {
        item = list.first();
        this.list = list;
        for (int i = 0; i < index; i++) {
            if (!hasNext()) {
                throw new Error("Invalid index");
            }

            next();
        }
    }

    @Override
    public boolean hasNext() {
        return item != null && (!started || item != list.first());
    }

    @Override
    public ActionItem next() {
        ActionItem result = item;
        item = item.next;
        started = true;
        /*if (!list.contains(result)) {
         throw new Error();
         }*/

        return result;
    }

    public ActionItem prev() {
        item = item.prev;
        if (item == list.first()) {
            started = false;
        }

        /*if (!list.contains(item)) {
         throw new Error();
         }*/
        return item;
    }

    public void setCurrent(ActionItem item) {
        this.item = item;
        if (item == list.first()) {
            started = false;
        }
    }

    @Override
    public void remove() {
        item = list.removeItem(item.prev);
    }

    public void add(Action action) {
        item = list.insertItemAfter(item.prev, action).next;
    }

    public void add(ActionItem actionItem) {
        item = list.insertItemAfter(item.prev, actionItem).next;
    }

    public void addBefore(ActionItem actionItem) {
        list.insertItemBefore(item.prev, actionItem);
    }

    public ActionItem peek(int index) {
        ActionItem item = this.item;
        for (int i = 0; i < index; i++) {
            if (item == null) {
                break;
            }

            item = item.next;
        }

        return item;
    }
}
