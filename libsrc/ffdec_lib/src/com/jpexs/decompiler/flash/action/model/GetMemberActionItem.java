/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionGetMember;
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GetMemberActionItem extends ActionItem {

    public GraphTargetItem object;

    public final GraphTargetItem memberName;

    public boolean printObfuscatedMemberName = false;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visit(memberName);
    }

    public GetMemberActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem memberName) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.memberName = memberName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        if ((memberName instanceof DirectValueActionItem) && printObfuscatedMemberName) {
            writer.append(".");
            StringBuilder sb = new StringBuilder();
            StringBuilderTextWriter sbw = new StringBuilderTextWriter(new CodeFormatting(), sb);
            stripQuotes(memberName, localData, sbw);
            writer.append(IdentifiersDeobfuscation.printIdentifier(false, sb.toString()));
            return writer;
        }
        if (((!(memberName instanceof DirectValueActionItem)) || (!((DirectValueActionItem) memberName).isString()) || (!printObfuscatedMemberName && !IdentifiersDeobfuscation.isValidName(false, ((DirectValueActionItem) memberName).toStringNoQuotes(localData))))) {
            writer.append("[");
            memberName.toString(writer, localData);
            return writer.append("]");
        }
        writer.append(".");
        return stripQuotes(memberName, localData, writer);
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
        hash = 47 * hash + Objects.hashCode(object);
        hash = 47 * hash + Objects.hashCode(memberName);
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
        if (!Objects.equals(object, other.object)) {
            return false;
        }
        if (!Objects.equals(memberName, other.memberName)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetMemberActionItem other = (GetMemberActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(object, other.object)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(memberName, other.memberName)) {
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
