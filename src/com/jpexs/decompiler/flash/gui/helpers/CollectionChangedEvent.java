/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.helpers;

/**
 *
 * @author JPEXS
 */
public class CollectionChangedEvent<E> {

    private final CollectionChangedAction action;

    private E oldItem = null;

    private int oldIndex = -1;

    private E newItem = null;

    private int newIndex = -1;

    public CollectionChangedEvent(CollectionChangedAction action) {
        this(action, null, -1);
    }

    public CollectionChangedEvent(CollectionChangedAction action, E item, int index) {
        this.action = action;
        switch (action) {
            case ADD:
                newItem = item;
                newIndex = index;
                break;
            case REMOVE:
                oldItem = item;
                oldIndex = index;
                break;
        }
    }

    public CollectionChangedAction getAction() {
        return action;
    }

    public E getOldItem() {
        return oldItem;
    }

    public int getOldIndex() {
        return oldIndex;
    }

    public E getNewItem() {
        return newItem;
    }

    public int getNewIndex() {
        return newIndex;
    }
}
