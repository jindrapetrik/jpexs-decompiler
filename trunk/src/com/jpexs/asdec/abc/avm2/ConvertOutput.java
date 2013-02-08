package com.jpexs.asdec.abc.avm2;

import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class ConvertOutput {

      public Stack<TreeItem> stack;
      public List<TreeItem> output;

      public ConvertOutput(Stack<TreeItem> stack, List<TreeItem> output) {
         this.stack = stack;
         this.output = output;
      }
   }
