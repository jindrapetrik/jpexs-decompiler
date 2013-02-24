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
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ConstructTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.EscapeXAttrTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.EscapeXElemTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.FindPropertyTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.GetLexTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.GetPropertyTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.StringTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.TreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.XMLTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.AddTreeItem;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ConstructIns extends InstructionDefinition {

   public ConstructIns() {
      super(0x42, "construct", new int[]{AVM2Code.DAT_ARG_COUNT});
   }

   @Override
   public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
      /*int argCount = (int) ((Long) arguments.get(0)).longValue();
       List passArguments = new ArrayList();
       for (int i = argCount - 1; i >= 0; i--) {
       passArguments.set(i, lda.operandStack.pop());
       }
       Object obj = lda.operandStack.pop();*/
      throw new RuntimeException("Cannot call constructor");
      //call construct property of obj
      //push new instance
   }

   public static boolean walkXML(TreeItem item, List<TreeItem> list) {
      boolean ret = true;
      if (item instanceof StringTreeItem) {
         list.add(item);
      } else if (item instanceof AddTreeItem) {
         ret = ret && walkXML(((AddTreeItem) item).leftSide, list);
         ret = ret && walkXML(((AddTreeItem) item).rightSide, list);
      } else if ((item instanceof EscapeXElemTreeItem) || (item instanceof EscapeXAttrTreeItem)) {
         list.add(item);
      } else {
         return false;
      }
      return ret;
   }

   @Override
   public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> stack, java.util.Stack<GraphTargetItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<GraphTargetItem> output, com.jpexs.decompiler.flash.abc.types.MethodBody body, com.jpexs.decompiler.flash.abc.ABC abc, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      int argCount = ins.operands[0];
      List<TreeItem> args = new ArrayList<TreeItem>();
      for (int a = 0; a < argCount; a++) {
         args.add(0, (TreeItem) stack.pop());
      }
      TreeItem obj = (TreeItem) stack.pop();

      FullMultinameTreeItem xmlMult = null;
      boolean isXML = false;
      if (obj instanceof GetPropertyTreeItem) {
         GetPropertyTreeItem gpt = (GetPropertyTreeItem) obj;
         if (gpt.object instanceof FindPropertyTreeItem) {
            FindPropertyTreeItem fpt = (FindPropertyTreeItem) gpt.object;
            xmlMult = fpt.propertyName;
            isXML = xmlMult.isXML(constants, localRegNames, fullyQualifiedNames) && xmlMult.isXML(constants, localRegNames, fullyQualifiedNames);
         }
      }
      if (obj instanceof GetLexTreeItem) {
         GetLexTreeItem glt = (GetLexTreeItem) obj;
         isXML = glt.propertyName.getName(constants, fullyQualifiedNames).equals("XML");
      }

      if (isXML) {
         if (args.size() == 1) {
            TreeItem arg = args.get(0);
            List<TreeItem> xmlLines = new ArrayList<TreeItem>();
            if (walkXML(arg, xmlLines)) {
               stack.push(new XMLTreeItem(ins, xmlLines));
               return;
            }
         }
      }

      stack.push(new ConstructTreeItem(ins, obj, args));
   }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -ins.operands[0] - 1 + 1;
   }
}
