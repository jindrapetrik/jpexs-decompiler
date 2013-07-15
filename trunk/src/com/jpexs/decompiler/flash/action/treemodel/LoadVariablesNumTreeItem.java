/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.parser.script.ActionScriptSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.action.treemodel.operations.AddTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class LoadVariablesNumTreeItem extends TreeItem {

    private GraphTargetItem urlString;
    private GraphTargetItem num;
    private int method;

    public LoadVariablesNumTreeItem(GraphSourceItem instruction, GraphTargetItem urlString, GraphTargetItem num, int method) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.num = num;
        this.method = method;
    }

    @Override
    public String toString(ConstantPool constants) {
        String methodStr = "";
        if (method == 1) {
            methodStr = ",\"GET\"";
        }
        if (method == 2) {
            methodStr = ",\"POST\"";
        }
        return hilight("loadVariablesNum(") + urlString.toString(constants) + hilight(",") + num + hilight(methodStr + ")");
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        ActionScriptSourceGenerator asGenerator = (ActionScriptSourceGenerator) generator;
        return toSourceMerge(localData, generator, urlString, new AddTreeItem(src, asGenerator.pushConstTargetItem("_level"), num, true), new ActionGetURL2(method, false, true));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
