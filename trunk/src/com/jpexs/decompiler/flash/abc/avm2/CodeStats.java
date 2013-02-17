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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.abc.ABC;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CodeStats {

   public int maxstack = 0;
   public int maxscope = 0;
   public int maxlocal = 0;
   public boolean has_set_dxns = false;
   public boolean has_activation = false;
   public InstructionStats instructionStats[];

   public String toString(ABC abc, List<String> fullyQualifiedNames) {
      String ret = "Stats: maxstack=" + maxstack + ", maxscope=" + maxscope + ", maxlocal=" + maxlocal + "\r\n";
      int i = 0;
      int ms = 0;
      for (InstructionStats stats : instructionStats) {
         int deltastack = stats.ins.definition.getStackDelta(stats.ins, abc);
         if (stats.stackpos > ms) {
            ms = stats.stackpos;
         }
         ret += "" + i + ":" + stats.stackpos + (deltastack >= 0 ? "+" + deltastack : deltastack) + "," + stats.scopepos + "    " + stats.ins.toString(abc.constants, fullyQualifiedNames) + "\r\n";
         i++;
      }
      return ret;
   }

   public CodeStats(AVM2Code code) {
      instructionStats = new InstructionStats[code.code.size()];
      for (int i = 0; i < code.code.size(); i++) {
         instructionStats[i] = new InstructionStats(code.code.get(i));
      }
   }
}
