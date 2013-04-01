package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public interface GraphSourceItemContainer {

    public byte[] getHeaderBytes();

    public List<GraphSourceItem> getItems(List<GraphSourceItem> parent);

    public int getDataLength();
    
    public long getEndAddress();
    
    public void setEndAddress(long address);
    
    public void translateContainer(List<GraphTargetItem> content,Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions);
}
