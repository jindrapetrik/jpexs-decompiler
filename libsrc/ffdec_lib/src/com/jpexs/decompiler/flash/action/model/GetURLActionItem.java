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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf3.ActionGetURL;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GetURLActionItem extends ActionItem {

    public String urlString;

    public String targetString;

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("getUrl");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(\"");
        writer.append(Helper.escapeActionScriptString(urlString));
        writer.append("\", \"");
        writer.append(Helper.escapeActionScriptString(targetString));
        return writer.append("\")");
    }

    public GetURLActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, String urlString, String targetString) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, new ActionGetURL(urlString, targetString));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.urlString);
        hash = 41 * hash + Objects.hashCode(this.targetString);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetURLActionItem other = (GetURLActionItem) obj;
        if (!Objects.equals(this.urlString, other.urlString)) {
            return false;
        }
        if (!Objects.equals(this.targetString, other.targetString)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetURLActionItem other = (GetURLActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.urlString, other.urlString)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.targetString, other.targetString)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}
