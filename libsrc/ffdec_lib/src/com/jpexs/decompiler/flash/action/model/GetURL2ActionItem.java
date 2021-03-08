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
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
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
public class GetURL2ActionItem extends ActionItem {

    public GraphTargetItem urlString;

    public GraphTargetItem targetString;

    public int sendVarsMethod;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(urlString);
        visitor.visit(targetString);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String methodStr = "";
        if (sendVarsMethod == 1) {
            methodStr = ",\"GET\"";
        }
        if (sendVarsMethod == 2) {
            methodStr = ",\"POST\"";
        }

        writer.append("getURL");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(");
        urlString.toString(writer, localData);
        writer.append(",");
        targetString.toString(writer, localData);
        return writer.append(methodStr).append(")");
    }

    public GetURL2ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem urlString, GraphTargetItem targetString, int method) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
        this.sendVarsMethod = method;
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(urlString.getNeededSources());
        ret.addAll(targetString.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, urlString, targetString, new ActionGetURL2(sendVarsMethod, false, false));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.urlString);
        hash = 79 * hash + Objects.hashCode(this.targetString);
        hash = 79 * hash + this.sendVarsMethod;
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
        final GetURL2ActionItem other = (GetURL2ActionItem) obj;
        if (this.sendVarsMethod != other.sendVarsMethod) {
            return false;
        }
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
        final GetURL2ActionItem other = (GetURL2ActionItem) obj;
        if (this.sendVarsMethod != other.sendVarsMethod) {
            return false;
        }
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
