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

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InterfaceActionItem extends ActionItem {

    public GraphTargetItem name;
    public List<GraphTargetItem> superInterfaces;

    public InterfaceActionItem(GraphTargetItem name, List<GraphTargetItem> superInterfaces) {
        super(null, NOPRECEDENCE);
        this.name = name;
        this.superInterfaces = superInterfaces;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("interface ", writer);
        name.toStringNoQuotes(writer, localData);
        boolean first = true;
        if (!superInterfaces.isEmpty()) {
            hilight(" extends ", writer);
        }
        for (GraphTargetItem ti : superInterfaces) {
            if (!first) {
                hilight(", ", writer);
            }
            first = false;
            Action.getWithoutGlobal(ti).toStringNoQuotes(writer, localData);
        }
        writer.appendNewLine();
        hilight("{", writer).appendNewLine();
        return hilight("}", writer).appendNewLine();
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        ret.addAll(asGenerator.generateTraits(localData, true, name, null, superInterfaces, null, null, null, null, null));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
