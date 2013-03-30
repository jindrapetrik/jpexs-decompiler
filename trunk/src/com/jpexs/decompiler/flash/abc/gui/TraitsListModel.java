/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.gui;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

public class TraitsListModel implements ListModel {

    private List<TraitsListItem> items;
    private List<DoABCTag> abcTags;
    private ABC abc;
    private int classIndex;

    public void setSorted(boolean sorted) {
        if (sorted) {
            Collections.sort(items, new Comparator<TraitsListItem>() {
                @Override
                public int compare(TraitsListItem o1, TraitsListItem o2) {
                    return o1.toStringName().compareTo(o2.toStringName());
                }
            });
        } else {
            reset();
        }
    }

    private void reset() {
        items = new ArrayList<TraitsListItem>();
        for (int t = 0; t < abc.class_info[classIndex].static_traits.traits.length; t++) {
            items.add(new TraitsListItem(TraitsListItem.Type.getTypeForTrait(abc.class_info[classIndex].static_traits.traits[t]), t, true, abcTags, abc, classIndex));
        }
        for (int t = 0; t < abc.instance_info[classIndex].instance_traits.traits.length; t++) {
            items.add(new TraitsListItem(TraitsListItem.Type.getTypeForTrait(abc.instance_info[classIndex].instance_traits.traits[t]), t, false, abcTags, abc, classIndex));
        }
        items.add(new TraitsListItem(TraitsListItem.Type.INITIALIZER, 0, false, abcTags, abc, classIndex));
        items.add(new TraitsListItem(TraitsListItem.Type.INITIALIZER, 0, true, abcTags, abc, classIndex));
    }

    public TraitsListModel(List<DoABCTag> abcTags, ABC abc, int classIndex, boolean sorted) {
        this.abcTags = abcTags;
        this.abc = abc;
        this.classIndex = classIndex;
        reset();
        if (sorted) {
            setSorted(true);
        }
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public Object getElementAt(int index) {
        return items.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }
}
