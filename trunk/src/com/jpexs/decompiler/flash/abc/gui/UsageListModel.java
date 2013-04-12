/*
 *  Copyright (C) 2011-2013 JPEXS
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
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author JPEXS
 */
public class UsageListModel extends DefaultListModel {

    private ABC abc;
    private List<ABCContainerTag> abcTags;

    public UsageListModel(List<ABCContainerTag> abcTags, ABC abc) {
        this.abc = abc;
        this.abcTags = abcTags;
    }

    @Override
    public Object get(int index) {
        return ((MultinameUsage) super.get(index)).toString(abcTags, abc);
    }

    @Override
    public Object getElementAt(int index) {
        return ((MultinameUsage) super.getElementAt(index)).toString(abcTags, abc);
    }

    public MultinameUsage getUsage(int index) {
        return ((MultinameUsage) super.getElementAt(index));
    }
}
