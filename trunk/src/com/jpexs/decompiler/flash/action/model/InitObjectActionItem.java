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

import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import java.util.ArrayList;
import java.util.List;

public class InitObjectActionItem extends ActionItem {

    public List<GraphTargetItem> names;
    public List<GraphTargetItem> values;

    public InitObjectActionItem(GraphSourceItem instruction, List<GraphTargetItem> names, List<GraphTargetItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
        this.names = names;
    }

    @Override
    public String toString(ConstantPool constants) {
        String objStr = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                objStr += hilight(",");
            }
            String valueStr = values.get(i).toString(constants);
            if (values.get(i) instanceof TernarOpItem) { //Ternar operator contains ":"
                valueStr = "(" + valueStr + ")";
            }
            objStr += names.get(i).toStringNoQuotes(constants) + hilight(":") + valueStr; //AS1/2 do not allow quotes in name here
        }
        return hilight("{") + objStr + hilight("}");
    }

    @Override
    public List<com.jpexs.decompiler.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.graph.GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem name : names) {
            ret.addAll(name.getNeededSources());
        }
        for (GraphTargetItem value : values) {
            ret.addAll(value.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (int i = values.size() - 1; i >= 0; i--) {
            ret.addAll(names.get(i).toSource(localData, generator));
            ret.addAll(values.get(i).toSource(localData, generator));
        }
        ret.add(new ActionPush((Long) (long) values.size()));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
