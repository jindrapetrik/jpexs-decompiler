package com.jpexs.decompiler.flash.graph;

import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ContinueItem extends GraphTargetItem {

   public long loopId;

   public ContinueItem(GraphSourceItem src, long loopId) {
      super(src, NOPRECEDENCE);
      this.loopId = loopId;
   }

   @Override
   public String toString(List localData) {
      return hilight("continue") + " " + "loop" + loopId;
   }
}
