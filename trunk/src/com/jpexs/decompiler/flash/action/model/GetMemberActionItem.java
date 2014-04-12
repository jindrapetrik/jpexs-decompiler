/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class GetMemberActionItem extends ActionItem {

    public GraphTargetItem object;
    public GraphTargetItem memberName;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        return ret;
    }

    public GetMemberActionItem(GraphSourceItem instruction, GraphTargetItem object, GraphTargetItem memberName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.memberName = memberName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        if ((memberName instanceof DirectValueActionItem) && (((DirectValueActionItem) memberName).value instanceof String)) {
            String memNameStr = (String) ((DirectValueActionItem) memberName).value;
            if (!Action.isReservedWord(memNameStr)) {
                writer.append(".");
                return stripQuotes(memberName, localData, writer);
            }
        }
        writer.append("[");
        memberName.toString(writer, localData);
        return writer.append("]");

    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(object.getNeededSources());
        ret.addAll(memberName.getNeededSources());
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 47 * hash + (this.memberName != null ? this.memberName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetMemberActionItem other = (GetMemberActionItem) obj;
        if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
            return false;
        }
        if (this.memberName != other.memberName && (this.memberName == null || !this.memberName.equals(other.memberName))) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, object, memberName, new ActionGetMember());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
