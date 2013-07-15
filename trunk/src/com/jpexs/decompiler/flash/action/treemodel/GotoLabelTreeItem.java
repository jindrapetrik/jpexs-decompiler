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

import com.jpexs.decompiler.flash.action.swf3.ActionGoToLabel;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.List;

public class GotoLabelTreeItem extends TreeItem {

    public String label;

    public GotoLabelTreeItem(GraphSourceItem instruction, String label) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.label = label;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("gotoAndStop(\"") + Helper.escapeString(label) + hilight("\")");
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, new ActionGoToLabel(label));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
