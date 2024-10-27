/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Load movie.
 *
 * @author JPEXS
 */
public class LoadMovieActionItem extends ActionItem {

    /**
     * URL string
     */
    private final GraphTargetItem urlString;

    /**
     * Target string
     */
    private final GraphTargetItem targetString;

    /**
     * Method
     */
    private final int method;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(urlString);
        visitor.visit(targetString);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param urlString URL string
     * @param targetString Target string
     * @param method Method
     */
    public LoadMovieActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem urlString, GraphTargetItem targetString, int method) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.urlString = urlString;
        this.targetString = targetString;
        this.method = method;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String methodStr = "";
        if (method == 1) {
            methodStr = ",\"GET\"";
        }
        if (method == 2) {
            methodStr = ",\"POST\"";
        }
        writer.append("loadMovie");
        writer.spaceBeforeCallParenthesis(2);
        writer.append("(");
        urlString.toString(writer, localData);
        writer.append(",");
        targetString.toString(writer, localData);
        return writer.append(methodStr).append(")");
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        return toSourceMerge(localData, generator, urlString, targetString, new ActionGetURL2(method, false, true, charset), needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.urlString);
        hash = 97 * hash + Objects.hashCode(this.targetString);
        hash = 97 * hash + this.method;
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
        final LoadMovieActionItem other = (LoadMovieActionItem) obj;
        if (this.method != other.method) {
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
        final LoadMovieActionItem other = (LoadMovieActionItem) obj;
        if (this.method != other.method) {
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
