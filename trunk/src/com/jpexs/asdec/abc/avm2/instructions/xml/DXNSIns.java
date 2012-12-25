/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.abc.avm2.instructions.xml;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import java.util.List;

public class DXNSIns extends InstructionDefinition {

   public DXNSIns() {
      super(0x06, "dxns", new int[]{AVM2Code.DAT_STRING_INDEX});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      int strIndex = (int) ((Long) arguments.get(0)).longValue();
      String s = constants.constant_string[strIndex];
      System.out.println("Set default XML space " + s);

   }
}
