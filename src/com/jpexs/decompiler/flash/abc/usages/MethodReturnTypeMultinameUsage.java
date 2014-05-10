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
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MethodReturnTypeMultinameUsage extends MethodMultinameUsage {

    public MethodReturnTypeMultinameUsage(int multinameIndex, int classIndex, int traitIndex, boolean isStatic, boolean isInitializer, Traits traits, int parentTraitIndex) {
        super(multinameIndex, classIndex, traitIndex, isStatic, isInitializer, traits, parentTraitIndex);
    }

    @Override
    public String toString(List<ABCContainerTag> abcTags, ABC abc) throws InterruptedException {
        return super.toString(abcTags, abc) + " return type";
    }
}
