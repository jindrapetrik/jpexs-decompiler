package com.jpexs.decompiler.flash.graph;

import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public interface GraphSourceItem {

   public void translate(List localData, Stack<GraphTargetItem> stack, List<GraphTargetItem> output);

   public boolean isJump();

   public boolean isBranch();

   public boolean isExit();

   public long getOffset();

   public List<Integer> getBranches(GraphSource code);
}
