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
package com.jpexs.asdec.abc.usages;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.abc.types.traits.Traits;
import com.jpexs.asdec.tags.DoABCTag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class ConstVarMultinameUsage extends TraitMultinameUsage {

   public ConstVarMultinameUsage(int multinameIndex, int classIndex, int traitIndex, boolean isStatic, Traits traits, int parentTraitIndex) {
      super(multinameIndex, classIndex, traitIndex, isStatic, traits, parentTraitIndex);
   }

   @Override
   public String toString(List<DoABCTag> abcTags, ABC abc) {
      return super.toString(abcTags, abc) + " "
              + (parentTraitIndex > -1
              ? (isStatic
              ? (((TraitMethodGetterSetter) abc.class_info[classIndex].static_traits.traits[parentTraitIndex]).convertHeader("", abcTags, abc, isStatic, false, classIndex, false, new ArrayList<String>()))
              : (((TraitMethodGetterSetter) abc.instance_info[classIndex].instance_traits.traits[parentTraitIndex]).convertHeader("", abcTags, abc, isStatic, false, classIndex, false, new ArrayList<String>())))
              : "")
              + ((TraitSlotConst) traits.traits[traitIndex]).convertHeader("", abcTags, abc, isStatic, false, classIndex, false, new ArrayList<String>());
   }

   public int getTraitIndex() {
      return traitIndex;
   }

   public boolean isStatic() {
      return isStatic;
   }
}
