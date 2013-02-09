package com.jpexs.asdec.abc.avm2.flowgraph;

import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ForException extends RuntimeException{
   public List<TreeItem> output;
   public List<TreeItem> finalOutput;
   public GraphPart continuePart;
           
   public ForException(List<TreeItem> output,List<TreeItem> finalOutput, GraphPart continuePart) {
      this.output=output;
      this.finalOutput=finalOutput;
      this.continuePart=continuePart;
   }
   
}
