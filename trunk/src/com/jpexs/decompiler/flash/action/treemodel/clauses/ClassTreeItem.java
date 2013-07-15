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
package com.jpexs.decompiler.flash.action.treemodel.clauses;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptSourceGenerator;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.TreeItem;
import com.jpexs.decompiler.flash.graph.Block;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import java.util.ArrayList;
import java.util.List;

public class ClassTreeItem extends TreeItem implements Block {

    public List<GraphTargetItem> functions;
    public List<GraphTargetItem> staticFunctions;
    public GraphTargetItem extendsOp;
    public List<GraphTargetItem> implementsOp;
    public GraphTargetItem className;
    public GraphTargetItem constructor;
    public List<MyEntry<GraphTargetItem, GraphTargetItem>> vars;
    public List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        ret.add(functions);
        ret.add(staticFunctions);
        return ret;
    }

    public ClassTreeItem(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, GraphTargetItem constructor, List<GraphTargetItem> functions, List<MyEntry<GraphTargetItem, GraphTargetItem>> vars, List<GraphTargetItem> staticFunctions, List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars) {
        super(null, NOPRECEDENCE);
        this.className = className;
        this.functions = functions;
        this.vars = vars;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.staticFunctions = staticFunctions;
        this.staticVars = staticVars;
        this.constructor = constructor;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret;
        ret = hilight("class ") + className.toStringNoQuotes(Helper.toList(constants));
        if (extendsOp != null) {
            ret += hilight(" extends ") + extendsOp.toStringNoQuotes(Helper.toList(constants));
        }
        if (!implementsOp.isEmpty()) {
            ret += hilight(" implements ");
            boolean first = true;
            for (GraphTargetItem t : implementsOp) {
                if (!first) {
                    ret += ", ";
                }
                first = false;
                ret += Action.getWithoutGlobal(t).toString(constants);
            }
        }
        ret += "\r\n{\r\n";
        for (GraphTargetItem f : functions) {
            ret += f.toString(constants) + "\r\n";
        }
        for (GraphTargetItem f : staticFunctions) {
            ret += "static " + f.toString(constants) + "\r\n";
        }
        for (MyEntry<GraphTargetItem, GraphTargetItem> item : vars) {
            ret += "var " + item.key.toStringNoQuotes(constants) + " = " + item.value.toString(constants) + ";\r\n";
        }
        for (MyEntry<GraphTargetItem, GraphTargetItem> item : staticVars) {
            ret += "static var " + item.key.toStringNoQuotes(constants) + " = " + item.value.toString(constants) + ";\r\n";
        }
        ret += "}\r\n";
        return ret;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionScriptSourceGenerator asGenerator = (ActionScriptSourceGenerator) generator;
        @SuppressWarnings("unchecked")
        List<Object> localData2 = (List<Object>) Helper.deepCopy(localData);
        asGenerator.setInMethod(localData2, true);
        ret.addAll(asGenerator.generateTraits(localData2, false, className, extendsOp, implementsOp, constructor, functions, vars, staticFunctions, staticVars));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
