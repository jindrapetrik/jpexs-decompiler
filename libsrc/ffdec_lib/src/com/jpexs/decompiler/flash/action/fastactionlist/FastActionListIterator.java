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
import java.util.Iterator;

/**
 *
 * @author JPEXS
 */
public class FastActionListIterator implements Iterator<ActionItem> {

    private ActionItem item;

    private final FastActionList list;

    private int index;

    FastActionListIterator(ActionItem firstAction, FastActionList list) {
        item = firstAction;
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return index < list.size();
    }

    @Override
    public ActionItem next() {
        ActionItem result = item;
        index++;
        item = item.next;
        return result;
    }

    @Override
    public void remove() {
        item = list.removeItem(item.prev);
        index--;
    }

    public void add(Action action) {
        item = list.insertItemAfter(item.prev, action).next;
        index++;
    }

    public void add(ActionItem actionItem) {
        item = list.insertItemAfter(item.prev, actionItem).next;
        index++;
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
