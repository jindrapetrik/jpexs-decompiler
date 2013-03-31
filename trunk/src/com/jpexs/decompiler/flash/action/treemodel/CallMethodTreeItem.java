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

import com.jpexs.decompiler.flash.action.swf4.Undefined;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.List;

public class CallMethodTreeItem extends TreeItem {

    public GraphTargetItem methodName;
    public GraphTargetItem scriptObject;
    public List<GraphTargetItem> arguments;

    public CallMethodTreeItem(GraphSourceItem instruction, GraphTargetItem scriptObject, GraphTargetItem methodName, List<GraphTargetItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.methodName = methodName;
        this.arguments = arguments;
        this.scriptObject = scriptObject;
    }

    @Override
    public String toString(ConstantPool constants) {
        String paramStr = "";
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) {
                paramStr += hilight(",");
            }
            paramStr += arguments.get(t).toStringNL(constants);
        }
        boolean blankMethod = false;
        if (methodName instanceof DirectValueTreeItem) {
            if (((DirectValueTreeItem) methodName).value instanceof Undefined) {
                blankMethod = true;
            }
            if (((DirectValueTreeItem) methodName).value instanceof String) {
                if (((DirectValueTreeItem) methodName).value.equals("")) {
                    blankMethod = true;
                }
            }
        }
        if (blankMethod) {
            return scriptObject.toString(constants) + hilight("(") + paramStr + hilight(")");
        }
        return scriptObject.toString(constants) + hilight(".") + stripQuotes(methodName) + hilight("(") + paramStr + hilight(")");
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(methodName.getNeededSources());
        ret.addAll(scriptObject.getNeededSources());
        for (GraphTargetItem ti : arguments) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }
}
