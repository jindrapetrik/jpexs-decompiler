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

import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;

/**
 *
 * @author JPEXS
 */
public class LoadVariablesTreeItem extends TreeItem {

    private GraphTargetItem urlString;
    private GraphTargetItem targetString;
    private int method;

    public LoadVariablesTreeItem(GraphSourceItem instruction, GraphTargetItem urlString, GraphTargetItem targetString, int method) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
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
        return hilight("loadVariables(") + urlString.toString(constants) + hilight(",") + targetString.toString(constants) + hilight(methodStr + ")");
    }
}
