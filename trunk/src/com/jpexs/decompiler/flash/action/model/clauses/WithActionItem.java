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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class WithActionItem extends ActionItem {

    public GraphTargetItem scope;
    public List<GraphTargetItem> items;

    public WithActionItem(Action instruction, GraphTargetItem scope, List<GraphTargetItem> items) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithActionItem(Action instruction, ActionItem scope) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = new ArrayList<>();
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        String ret;
        List<Object> localData = new ArrayList<>();
        localData.add(constants);
        ret = hilight("with(", highlight) + scope.toString(highlight, localData) + hilight(")", highlight) + "\r\n" + hilight("{", highlight) + "\r\n";
        for (GraphTargetItem ti : items) {
            ret += ti.toString(highlight, localData) + "\r\n";
        }
        ret += hilight("}", highlight);
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> data = generator.generate(localData, items);
        List<Action> dataA = new ArrayList<>();
        for (GraphSourceItem s : data) {
            if (s instanceof Action) {
                dataA.add((Action) s);
            }
        }
        int codeLen = Action.actionsToBytes(dataA, false, SWF.DEFAULT_VERSION).length;
        return toSourceMerge(localData, generator, scope, new ActionWith(codeLen), data);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
