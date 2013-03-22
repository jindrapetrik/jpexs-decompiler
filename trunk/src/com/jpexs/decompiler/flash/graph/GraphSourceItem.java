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

   public boolean ignoredLoops();

   public List<Integer> getBranches(GraphSource code);

   public boolean isIgnored();

   public void setIgnored(boolean ignored);
}
