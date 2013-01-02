/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jindra
 */
public class DefaultXMLNamespace extends TreeItem{

   private TreeItem ns;
   public DefaultXMLNamespace(AVM2Instruction instruction,TreeItem ns) {
      super(instruction, NOPRECEDENCE);
      this.ns=ns;
   }

   
   
   @Override
   public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      return hilight("default xml namespace = ")+ns.toString(constants, localRegNames, fullyQualifiedNames);
   }
   
}
