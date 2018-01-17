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
package com.jpexs.decompiler.flash.gui.proxy;

import com.jpexs.proxy.Replacement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * List mode for list with SWF urls
 *
 * @author JPEXS
 */
public class SWFListModel implements ListModel<Replacement> {

    private final List<ListDataListener> listeners = new ArrayList<>();

    private final List<Replacement> replacements;

    /**
     * Constructor
     *
     * @param replacements List of replacements
     */
    public SWFListModel(List<Replacement> replacements) {
        this.replacements = replacements;
    }

    /**
     * Removes replacement with specified index from the list
     *
     * @param index Index of replacement to remove
     * @return Removed replacement
     */
    public Replacement removeURL(int index) {
        if (index == -1) {
            return null;
        }
        if (index < replacements.size()) {
            Replacement r = replacements.remove(index);
            for (ListDataListener l : listeners) {
                l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
            }
            return r;
        }
        return null;
    }

    /**
     * Called when data in a replacement changed
     *
     * @param index Index of which SWF changed
     */
    public void dataChanged(int index) {
        if (index == -1) {
            return;
        }
        for (ListDataListener l : listeners) {
            l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index));
        }
    }

    /**
     * Returns index of specified replacement
     *
     * @param replacement Replacement
     * @return Index of -1 if not found
     */
    public int indexOf(Replacement replacement) {
        for (int i = 0; i < replacements.size(); i++) {
            if (replacements.get(i) == replacement) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Clears url list
     */
    public void clear() {
        int size = replacements.size();
        if (size == 0) {
            return;
        }
        replacements.clear();
        for (ListDataListener l : listeners) {
            l.intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size - 1));
        }
    }

    /**
     * Test whether the list contains url
     *
     * @param url URL to test
     * @return True when contains
     */
    public boolean contains(String url) {
        for (Replacement r : replacements) {
            if (r.matches(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds url to the list
     *
     * @param replacement URL to add
     */
    public void addURL(Replacement replacement) {
        int sizeBefore = replacements.size();
        replacements.add(replacement);
        for (ListDataListener l : listeners) {
            l.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, sizeBefore, sizeBefore));
        }
    }

    /**
     * Returns size of the list
     *
     * @return Size of the list
     */
    @Override
    public int getSize() {
        return replacements.size();
    }

    /**
     * Returns element on specified index
     *
     * @param index Index of element
     * @return Element on index
     */
    @Override
    public Replacement getElementAt(int index) {
        return replacements.get(index);
    }

    /**
     * Adds add list data listener
     *
     * @param l list data listener
     */
    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    /**
     * Adds remove list data listener
     *
     * @param l list data listener
     */
    @Override
    public void removeListDataListener(ListDataListener l) {
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }
}
