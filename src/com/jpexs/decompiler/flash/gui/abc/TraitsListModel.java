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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author JPEXS
 */
public final class TraitsListModel implements ListModel<Object> {

    private List<TraitsListItem> items;

    private final ABC abc;

    private final int classIndex;

    private final int scriptIndex;

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
        items = new ArrayList<>();
        if (classIndex > -1) {
            List<Trait> traits = abc.class_info.get(classIndex).static_traits.traits;
            for (int t = 0; t < traits.size(); t++) {
                Trait trait = traits.get(t);
                if (trait.isVisible(true, abc)) {
                    items.add(new TraitsListItem(TraitType.getTypeForTrait(trait), t, true, abc, classIndex, scriptIndex));
                }
            }

            traits = abc.instance_info.get(classIndex).instance_traits.traits;
            for (int t = 0; t < traits.size(); t++) {
                Trait trait = traits.get(t);
                if (trait.isVisible(false, abc)) {
                    items.add(new TraitsListItem(TraitType.getTypeForTrait(trait), t, false, abc, classIndex, scriptIndex));
                }
            }

            items.add(new TraitsListItem(TraitType.INITIALIZER, 0, false, abc, classIndex, scriptIndex));
            items.add(new TraitsListItem(TraitType.INITIALIZER, 0, true, abc, classIndex, scriptIndex));
        }

        if (Configuration.enableScriptInitializerDisplay.get()) {
            items.add(new TraitsListItem(TraitType.SCRIPT_INITIALIZER, 0, true, abc, classIndex, scriptIndex));
        }
    }

    public TraitsListModel(ABC abc, int classIndex, int scriptIndex, boolean sorted) {
        this.abc = abc;
        this.classIndex = classIndex;
        this.scriptIndex = scriptIndex;
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
