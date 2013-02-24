package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class ActionGraphSource extends GraphSource {

   private List<Action> actions;
   public int version;
   private HashMap<Integer, String> registerNames;

   public ActionGraphSource(List<Action> actions, int version, HashMap<Integer, String> registerNames) {
      this.actions = actions;
      this.version = version;
      this.registerNames = registerNames;
   }

   @Override
   public int size() {
      return actions.size();
   }

   @Override
   public GraphSourceItem get(int pos) {
      return actions.get(pos);
   }

   @Override
   public boolean isEmpty() {
      return actions.isEmpty();
   }

   @Override
   public List<GraphTargetItem> translatePart(List localData, Stack<GraphTargetItem> stack, int start, int end) {
      return (Action.actionsPartToTree(registerNames, stack, actions, start, end, version));
   }

   @Override
   public int adr2pos(long adr) {
      return Action.adr2ip(actions, adr, version);
   }

   @Override
   public long pos2adr(int pos) {
      return Action.ip2adr(actions, pos, version);
   }
}
