package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionSecondPassData extends SecondPassData {
    List<List<GraphPart>> switchParts = new ArrayList<>();
    List<List<GraphPart>> switchOnFalseParts = new ArrayList<>();
    List<List<GraphTargetItem>> switchCaseExpressions = new ArrayList<>();
}
