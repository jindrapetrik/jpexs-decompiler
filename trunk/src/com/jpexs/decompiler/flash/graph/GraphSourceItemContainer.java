package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.action.parser.FlasmLexer;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public interface GraphSourceItemContainer {

    public long getHeaderSize();

    public List<Long> getContainerSizes();

    public String getASMSourceBetween(int pos);

    public boolean parseDivision(int pos, long addr, FlasmLexer lexer);

    public HashMap<Integer, String> getRegNames();
    
    public void translateContainer(List<List<GraphTargetItem>> contents, Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions);
}
