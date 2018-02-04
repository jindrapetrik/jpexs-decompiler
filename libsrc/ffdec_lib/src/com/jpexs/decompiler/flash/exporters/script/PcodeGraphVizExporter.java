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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionGraphSource;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class PcodeGraphVizExporter {

    private final String BLOCK_STYLE = "shape=\"box\"";

    private String getBlockName(ActionList list, GraphPart part) {
        long address = 0;
        if (!list.isEmpty()) {
            if (part.start >= list.size()) {
                address = list.get(list.size() - 1).getAddress() + list.get(list.size() - 1).getBytesLength();
            } else {
                address = list.get(part.start).getAddress();
            }
        }

        return "loc" + Helper.formatAddress(address);
    }

    private boolean isEndOfScript(ActionList list, GraphPart part) {
        if (part.start >= list.size()) {
            return true;
        }
        return false;
    }

    public void export(ASMSource src, GraphTextWriter writer) throws InterruptedException {
        ActionList alist = src.getActions();
        ActionGraph gr = new ActionGraph("", false, alist, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION);
        List<GraphPart> allBlocks = new ArrayList<>();
        List<GraphPart> heads = gr.makeGraph(new ActionGraphSource("", false, alist, SWF.DEFAULT_VERSION, new HashMap<>(), new HashMap<>(), new HashMap<>()), allBlocks, new ArrayList<>());
        writer.append("digraph pcode {\r\n");
        Set<Long> knownAddresses = Action.getActionsAllRefs(alist);
        int h = 0;
        for (GraphPart head : heads) {
            String headName = "start";
            if (heads.size() > 1) {
                h++;
                headName = "start" + h;
            }
            writer.append(headName + " [shape=\"circle\"]\r\n");
            writer.append(headName + ":s -> " + getBlockName(alist, head) + ":n;\r\n");
        }
        for (int i = 0; i < allBlocks.size(); i++) {

            GraphPart part = allBlocks.get(i);
            StringBuilder blkCode = new StringBuilder();
            for (int j = part.start; j <= part.end; j++) {
                if (j < alist.size()) {
                    if (knownAddresses.contains(alist.get(j).getAddress())) {
                        blkCode.append("loc" + Helper.formatAddress(alist.get(j).getAddress())).append(":\\l");
                    }
                    blkCode.append(alist.get(j).getASMSource(alist, knownAddresses, ScriptExportMode.PCODE)).append("\r\n");
                }
            }
            String labelStr = blkCode.toString();
            labelStr = labelStr.replace("\"", "\\\"");
            labelStr = labelStr.replace("\r\n", "\\l");
            String partBlockName = getBlockName(alist, part);
            String blkStyle = BLOCK_STYLE;
            if (isEndOfScript(alist, part)) {
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
                String nextBlockName = getBlockName(alist, next);
                writer.append(partBlockName + orientation + " -> " + nextBlockName + ":n;\r\n");
            }
        }
        writer.append("}\r\n");
    }
}
