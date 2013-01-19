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


package com.jpexs.asdec.abc.types.traits;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.helpers.Highlighting;
import com.jpexs.asdec.tags.DoABCTag;
import java.util.List;

public class Traits {

   public Trait traits[] = new Trait[0];

   @Override
   public String toString() {
      String s = "";
      for (int t = 0; t < traits.length; t++) {
         if (t > 0) {
            s += "\r\n";
         }
         s += traits[t].toString();
      }
      return s;
   }

   public String toString(ABC abc, List<String> fullyQualifiedNames) {
      String s = "";
      for (int t = 0; t < traits.length; t++) {
         if (t > 0) {
            s += "\r\n";
         }
         s += traits[t].toString(abc, fullyQualifiedNames);
      }
      return s;
   }

   public String convert(List<DoABCTag> abcTags, ABC abc, boolean isStatic, boolean pcode, boolean makePackages, int classIndex, boolean highlighting, List<String> fullyQualifiedNames) {
      String s = "";
      for (int t = 0; t < traits.length; t++) {
         if (t > 0) {
            s += "\r\n\r\n";
         }
         String plus;
         if (makePackages) {
            plus = traits[t].convertPackaged(abcTags, abc, isStatic, pcode, classIndex, highlighting, fullyQualifiedNames);
         } else {
            plus = traits[t].convert(abcTags, abc, isStatic, pcode, classIndex, highlighting, fullyQualifiedNames);
         }
         if (highlighting) {
            int h = t;
            if (classIndex != -1) {
               if (!isStatic) {
                  h = h + abc.class_info[classIndex].static_traits.traits.length;
               }
            }
            if (traits[t] instanceof TraitClass) {
               plus = Highlighting.hilighClass(plus, ((TraitClass) traits[t]).class_info);
            } else {
               plus = Highlighting.hilighTrait(plus, h);
            }
         }
         s += plus;
      }
      return s;
   }
}
