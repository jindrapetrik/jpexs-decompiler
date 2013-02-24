package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConvertOutput;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class AVM2GraphSource extends GraphSource {

   private AVM2Code code;
   boolean isStatic;
   int classIndex;
   HashMap<Integer, GraphTargetItem> localRegs;
   Stack<GraphTargetItem> scopeStack;
   ABC abc;
   MethodBody body;
   HashMap<Integer, String> localRegNames;
   List<String> fullyQualifiedNames;

   public AVM2GraphSource(AVM2Code code, boolean isStatic, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> scopeStack, ABC abc, MethodBody body, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
      this.code = code;
      this.isStatic = isStatic;
      this.classIndex = classIndex;
      this.localRegs = localRegs;
      this.scopeStack = scopeStack;
      this.abc = abc;
      this.body = body;
      this.localRegNames = localRegNames;
      this.fullyQualifiedNames = fullyQualifiedNames;
   }

   @Override
   public int size() {
      return code.code.size();
   }

   @Override
   public GraphSourceItem get(int pos) {
      return code.code.get(pos);
   }

   @Override
   public boolean isEmpty() {
      return code.code.isEmpty();
   }

   @Override
   public List<GraphTargetItem> translatePart(List localData, Stack<GraphTargetItem> stack, int start, int end) {
      List<GraphTargetItem> ret = new ArrayList<GraphTargetItem>();
      ConvertOutput co = code.toSourceOutput(false, isStatic, classIndex, localRegs, stack, scopeStack, abc, abc.constants, abc.method_info, body, start, end, localRegNames, fullyQualifiedNames, new boolean[size()]);
      ret.addAll(co.output);
      return ret;
   }

   @Override
   public int adr2pos(long adr) {
      return code.adr2pos(adr);
   }

   @Override
   public long pos2adr(int pos) {
      return code.pos2adr(pos);
   }
}
