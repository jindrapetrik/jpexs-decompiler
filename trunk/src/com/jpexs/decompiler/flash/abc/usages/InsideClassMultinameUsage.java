/*
 *  Copyright (C) 2010-2014 JPEXS
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
public abstract class InsideClassMultinameUsage extends MultinameUsage {

    public int multinameIndex;
    public int classIndex;

    public InsideClassMultinameUsage(int multinameIndex, int classIndex) {
        this.multinameIndex = multinameIndex;
        this.classIndex = classIndex;
    }

    @Override
    public String toString(List<ABCContainerTag> abcTags, ABC abc) throws InterruptedException {
        return "class " + abc.constants.getMultiname(abc.instance_info[classIndex].name_index).getNameWithNamespace(abc.constants);
    }

    public int getMultinameIndex() {
        return multinameIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }
}
