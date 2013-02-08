
package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class HasNextTreeItem extends TreeItem {
   public TreeItem object;
   public TreeItem collection;
   public HasNextTreeItem(AVM2Instruction instruction, TreeItem object, TreeItem collection) {
      super(instruction, NOPRECEDENCE);
      this.object=object;
      this.collection=collection;
   }

   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return collection.toString(constants, localRegNames, fullyQualifiedNames)+" hasNext "+object.toString(constants, localRegNames, fullyQualifiedNames);
   }
    
}
