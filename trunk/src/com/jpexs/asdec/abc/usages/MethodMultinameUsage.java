/*
 *  Copyright (C) 2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.usages;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;

/**
 *
 * @author JPEXS
 */
public abstract class MethodMultinameUsage extends InsideClassMultinameUsage {

   public int traitIndex ;
   public boolean isStatic;
   public boolean isInitializer;
   public MethodMultinameUsage(int multinameIndex,int classIndex,int traitIndex,boolean isStatic,boolean isInitializer)
   {
      super(multinameIndex,classIndex);
      this.traitIndex=traitIndex;
      this.isStatic=isStatic;
      this.isInitializer=isInitializer;
   }

   public boolean isInitializer() {
      return isInitializer;
   }



   @Override
   public String toString(ABC abc) {
      return super.toString(abc)+" "+(
              isInitializer?
                 (isStatic?
                    "class initializer":
                    "instance initializer")
                 :
         (isStatic?
         ((TraitMethodGetterSetter)abc.class_info[classIndex].static_traits.traits[traitIndex]).convert(abc.constants, abc.method_info, abc,true)
              :
         ((TraitMethodGetterSetter)abc.instance_info[classIndex].instance_traits.traits[traitIndex]).convert(abc.constants, abc.method_info, abc,false)
         ));
   }

   public int getTraitIndex() {
      return traitIndex;
   }

   public boolean isStatic() {
      return isStatic;
   }

   


}
