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
package com.jpexs.decompiler.flash.abc.usages;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ClassNameMultinameUsage extends InsideClassMultinameUsage {

    public ClassNameMultinameUsage(int multinameIndex, int classIndex) {
        super(multinameIndex, classIndex);
    }

    @Override
    public String toString(List<ABCContainerTag> abcTags, ABC abc) {
        return "class " + abc.constants.getMultiname(abc.instance_info[classIndex].name_index).getNameWithNamespace(abc.constants);
    }
}
