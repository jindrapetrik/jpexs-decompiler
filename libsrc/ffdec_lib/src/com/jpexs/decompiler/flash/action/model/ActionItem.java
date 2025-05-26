/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for ActionScript 1/2 high-level model items.
 *
 * @author JPEXS
 */
public abstract class ActionItem extends GraphTargetItem implements Serializable {

    /**
     * Constructor.
     */
    public ActionItem() {
        super(ActionGraphTargetDialect.INSTANCE, null, null, NOPRECEDENCE);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param precedence Precedence
     */
    public ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence) {
        this(instruction, lineStartIns, precedence, null);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param precedence Precedence
     * @param value Value
     */
    public ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence, GraphTargetItem value) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, precedence, value);
    }

    /**
     * Chech if item is empty string.
     * @param target Target
     * @return True if item is empty string
     */
    protected boolean isEmptyString(GraphTargetItem target) {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {

                if (((DirectValueActionItem) target).value.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Strip quotes.
     * @param target Target
     * @param localData Local data
     * @param writer Writer
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    protected GraphTextWriter stripQuotes(GraphTargetItem target, LocalData localData, GraphTextWriter writer) throws InterruptedException {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {
                DirectValueActionItem dv = (DirectValueActionItem) target;

                //dv.toStringNoQuotes(writer, localData);
                return writer.append((String) dv.value);                
            }
        }
        if (target == null) {
            return writer;
        } else {
            return target.toString(writer, localData);
        }
    }

    /**
     * Converts item to source including call.
     * @param localData Local data
     * @param gen Generator
     * @param list List
     * @return List of source items
     * @throws CompilationException On compilation error
     */
    protected List<GraphSourceItem> toSourceCall(SourceGeneratorLocalData localData, SourceGenerator gen, List<GraphTargetItem> list) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) gen;
        String charset = asGenerator.getCharset();

        List<GraphSourceItem> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ret.addAll(0, list.get(i).toSource(localData, gen));
        }
        ret.add(new ActionPush((Long) (long) list.size(), charset));
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = toSource(localData, generator);
        if (hasReturnValue()) {
            ret.add(new ActionPop());
        }
        return ret;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}
