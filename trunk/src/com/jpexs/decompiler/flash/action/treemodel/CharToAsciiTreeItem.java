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

import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class CharToAsciiTreeItem extends TreeItem {

    private GraphTargetItem value;

    public CharToAsciiTreeItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("ord(") + value.toString(constants) + hilight(")");
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime() {
        if (value instanceof DirectValueTreeItem) {
            DirectValueTreeItem dv = (DirectValueTreeItem) value;
            if (dv.value instanceof String) {
                String s = (String) dv.value;
                if (s.length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object getResult() {
        Object res = value.getResult();
        String s = res.toString();
        if (s.length() > 0) {
            char c = s.charAt(0);
            return (int) c;
        }
        return 0;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, value, new ActionCharToAscii());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
