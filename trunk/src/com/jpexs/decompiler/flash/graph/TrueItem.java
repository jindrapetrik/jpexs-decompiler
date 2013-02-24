package com.jpexs.decompiler.flash.graph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TrueItem extends GraphTargetItem {

   public TrueItem(GraphSourceItem src) {
      super(src, PRECEDENCE_PRIMARY);
   }

   @Override
   public String toString(List localData) {
      return "true";
   }
}
