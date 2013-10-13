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

import com.jpexs.decompiler.flash.action.swf5.ActionTypeOf;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class TypeOfActionItem extends ActionItem {

    public TypeOfActionItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("typeof(", writer);
        value.toString(writer, localData);
        return hilight(")", writer);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public Object getResult() {
        Object res = value.getResult();
        EcmaType type = EcmaScript.type(res);
        switch (type) {
            case STRING:
                return "string";
            case BOOLEAN:
                return "boolean";
            case NUMBER:
                return "number";
            case OBJECT:
                return "object";
            case UNDEFINED:
                return "undefined";
            case NULL:
                return "null";
        }
        //TODO: function,movieclip
        return "object";
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, value, new ActionTypeOf());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
