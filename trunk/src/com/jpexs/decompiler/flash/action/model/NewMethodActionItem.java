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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.action.swf5.ActionNewMethod;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class NewMethodActionItem extends ActionItem {

    public GraphTargetItem methodName;
    public GraphTargetItem scriptObject;
    public List<GraphTargetItem> arguments;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(scriptObject);
        ret.addAll(arguments);
        return ret;
    }

    public NewMethodActionItem(GraphSourceItem instruction, GraphTargetItem scriptObject, GraphTargetItem methodName, List<GraphTargetItem> arguments) {
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
                paramStr += ",";
            }
            paramStr += arguments.get(t).toString(constants);
        }
        boolean blankMethod = false;
        String methodNameStr = "";
        if (methodName instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) methodName).value instanceof Undefined) {
                blankMethod = true;
            } else if (((DirectValueActionItem) methodName).value instanceof String) {
                if (((DirectValueActionItem) methodName).value.equals("")) {
                    blankMethod = true;
                }
                methodNameStr = ((DirectValueActionItem) methodName).toStringNoQuotes(constants);
            } else {
                methodNameStr = methodName.toString(constants);
            }
        } else {
            methodNameStr = methodName.toString(constants);
        }
        if (blankMethod) {
            return scriptObject.toString(constants) + "(" + paramStr + ")";
        }
        return hilight("new ") + scriptObject.toString(constants) + hilight(".") + methodNameStr + hilight("(") + paramStr + hilight(")");
    }

    @Override
    public List<com.jpexs.decompiler.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(methodName.getNeededSources());
        ret.addAll(scriptObject.getNeededSources());
        for (GraphTargetItem ti : arguments) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), scriptObject, methodName, new ActionNewMethod());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
