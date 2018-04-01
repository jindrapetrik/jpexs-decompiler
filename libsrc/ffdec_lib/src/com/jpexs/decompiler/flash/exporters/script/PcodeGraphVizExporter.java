/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.script;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.Action;
import static com.jpexs.decompiler.flash.action.Action.adr2ip;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.model.CommentItem;
import com.jpexs.graphs.graphviz.dot.parser.DotId;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author JPEXS
 */
public class PcodeGraphVizExporter {

    private final String BLOCK_STYLE = "shape=\"box\"";

    private static final int INS_LEN_LIMIT = 50;
    private static final String ELIPSIS = "...";

    private String getBlockName(GraphSource list, GraphPart part) {
        return "loc" + Helper.formatAddress(list.pos2adr(part.start, true));
    }

    private boolean isEndOfScript(GraphSource list, GraphPart part) {
        if (part.start >= list.size()) {
            return true;
        }
        return false;
    }

    private static void populateParts(GraphPart part, Set<GraphPart> allParts) {
        if (allParts.contains(part)) {
            return;
        }
        allParts.add(part);
        for (GraphPart p : part.nextParts) {
            populateParts(p, allParts);
        }
    }

    public void exportAs12(ASMSource src, GraphTextWriter writer) throws InterruptedException {
        ActionList alist = src.getActions();
        ActionGraph gr = new ActionGraph("", false, alist, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION);
        export(gr, writer);
    }

    public void exportAs3(ABC abc, MethodBody body, GraphTextWriter writer) throws InterruptedException {
        AVM2Graph gr = new AVM2Graph(body.getCode(), abc, body, false, -1, -1, new HashMap<>(), new ScopeStack(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), body.getCode().visitCode(body));
        export(gr, writer);
    }

    private void exportGraph(Graph graph, GraphTextWriter writer) throws InterruptedException {
        graph.init(null);
        GraphSource graphSource = graph.getGraphCode();
        Set<GraphPart> allBlocks = new HashSet<>();
        List<GraphPart> heads = graph.heads;
        for (GraphPart h : heads) {
            populateParts(h, allBlocks);
        }

        Set<Long> knownAddresses = graphSource.getImportantAddresses();
        int h = 0;
        for (GraphPart head : heads) {
            String headName = "start";
            if (heads.size() > 1) {
                h++;
                headName = "start" + h;
            }
            writer.append(headName + " [shape=\"circle\"]\r\n");
            writer.append(headName + ":s -> " + getBlockName(graphSource, head) + ":n;\r\n");
        }
        for (GraphPart part : allBlocks) {
            StringBuilder blkCodeBuilder = new StringBuilder();
            for (int j = part.start; j <= part.end; j++) {
                if (j < graphSource.size()) {
                    if (knownAddresses.contains(graphSource.get(j).getAddress())) {
                        blkCodeBuilder.append("loc").append(Helper.formatAddress(graphSource.get(j).getAddress())).append(":\r\n");
                    }
                    String insStr = graphSource.insToString(j);
                    if (insStr.length() > INS_LEN_LIMIT) {
                        insStr = insStr.substring(0, INS_LEN_LIMIT - ELIPSIS.length()) + ELIPSIS;
                    }
                    blkCodeBuilder.append(insStr).append("\r\n");
                }
            }
            String labelStr = blkCodeBuilder.toString();
            labelStr = labelStr.replace("\"", "\\\"");
            labelStr = labelStr.replace("\r\n", "\\l");
            String partBlockName = getBlockName(graphSource, part);
            String blkStyle = BLOCK_STYLE;
            if (isEndOfScript(graphSource, part)) {
                blkStyle = "shape=\"circle\"";
                labelStr = "FINISH";
            }
            writer.append(partBlockName + " [" + blkStyle + " label=\"" + labelStr + "\"];\r\n");
            for (int n = 0; n < part.nextParts.size(); n++) {
                GraphPart next = part.nextParts.get(n);
                String orientation = ":s";
                if (part.nextParts.size() == 2 && n == 0) {
                    orientation = "";
                }
                if (part.nextParts.size() == 2 && n == 1) {
                    orientation = "";
                }
                String nextBlockName = getBlockName(graphSource, next);
                writer.append(partBlockName + orientation + " -> " + nextBlockName + ":n;\r\n");
            }
        }
    }

    public void export(Graph graph, GraphTextWriter writer) throws InterruptedException {
        writer.append("digraph pcode {\r\n");
        exportGraph(graph, writer);
        int pos = 0;
        Map<String, Graph> subgraphs = graph.getSubGraphs();
        for (String name : subgraphs.keySet()) {
            writer.append("subgraph cluster_" + pos + " {");
            writer.append("label=" + new DotId(name, false) + ";\r\n");
            pos++;
            exportGraph(subgraphs.get(name), writer);
            writer.append("}");
        }
        writer.append("}\r\n");
    }
}
