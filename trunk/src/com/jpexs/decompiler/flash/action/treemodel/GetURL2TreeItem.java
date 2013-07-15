/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class GetURL2TreeItem extends TreeItem {

    public GraphTargetItem urlString;
    public GraphTargetItem targetString;
    public int sendVarsMethod;

    @Override
    public String toString(ConstantPool constants) {
        String methodStr = "";
        if (sendVarsMethod == 1) {
            methodStr = ",\"GET\"";
        }
        if (sendVarsMethod == 2) {
            methodStr = ",\"POST\"";
        }

        return hilight("getURL(") + urlString.toString(constants) + hilight(",") + targetString.toString(constants) + hilight(methodStr + ")");
    }

    public GetURL2TreeItem(GraphSourceItem instruction, GraphTargetItem urlString, GraphTargetItem targetString, int method) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
        this.sendVarsMethod = method;
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(urlString.getNeededSources());
        ret.addAll(targetString.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, urlString, targetString, new ActionGetURL2(sendVarsMethod, false, false));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
