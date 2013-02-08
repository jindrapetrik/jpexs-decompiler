package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NextValueTreeItem extends TreeItem {

   TreeItem index;
   TreeItem obj;
   public NextValueTreeItem(AVM2Instruction instruction,TreeItem index,TreeItem obj) {
      super(instruction, NOPRECEDENCE);
      this.index=index;
      this.obj=obj;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return "nextValue("+index.toString(constants, localRegNames, fullyQualifiedNames) +","+obj.toString(constants, localRegNames, fullyQualifiedNames)+")";
   }
   
}
