package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CommentTreeItem extends TreeItem {
   public String comment;
   public CommentTreeItem(AVM2Instruction instruction,String comment) {
      super(instruction, NOPRECEDENCE);
      this.comment=comment;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
     return "//"+comment;
   }
   
}
