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
package com.jpexs.asdec.abc.avm2.instructions.stack;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.WithTreeItem;
import com.jpexs.asdec.abc.types.MethodBody;
import com.jpexs.asdec.abc.types.MethodInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class PopScopeIns extends InstructionDefinition {

   public PopScopeIns() {
      super(0x1d, "popscope", new int[]{});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      lda.scopeStack.pop();
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      TreeItem scope = (TreeItem) scopeStack.pop();
      for (int i = output.size() - 1; i >= 0; i--) {
         if (output.get(i) instanceof WithTreeItem) {
            WithTreeItem wti = (WithTreeItem) output.get(i);
            if (wti.scope == scope) {
               wti.items = new ArrayList<TreeItem>();
               for (int k = i + 1; k < output.size(); k++) {
                  //output.subList(i+1, output.size());
                  wti.items.add(output.get(k));
               }
               while (output.size() > i + 1) {
                  output.remove(i + 1);
               }
               /*int count=output.size()-1-(i+1);
                for(int c=0;c<count;c++){
                output.remove(i+1);
                }*/
               break;
            }
         }
      }
   }

   @Override
   public int getScopeStackDelta(AVM2Instruction ins, ABC abc) {
      return -1;
   }
}
