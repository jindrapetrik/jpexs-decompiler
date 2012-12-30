/*
 *  Copyright (C) 2011 JPEXS
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
import com.jpexs.asdec.abc.types.traits.Traits;
import com.jpexs.asdec.tags.DoABCTag;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstVarNameMultinameUsage extends ConstVarMultinameUsage {

   public ConstVarNameMultinameUsage(int multinameIndex, int classIndex, int traitIndex, boolean isStatic, Traits traits, int parentTraitIndex) {
      super(multinameIndex, classIndex, traitIndex, isStatic, traits, parentTraitIndex);
   }

   @Override
   public String toString(List<DoABCTag> abcTags,ABC abc) {
      return super.toString(abcTags,abc) + " name";
   }
}
