/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ReturnActionItem extends ActionItem implements ExitItem {

    public ReturnActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY, value);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("return ");
        return value.toString(writer, localData);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        List<GraphSourceItem> ret = new ArrayList<>();
        int forinlevel = asGenerator.getForInLevel(localData);
        for (int i = 0; i < forinlevel; i++) { //Must POP all remaining values from enumerations (for..in)
            List<Action> forinret = new ArrayList<>();
            forinret.add(new ActionPush(Null.INSTANCE));
            forinret.add(new ActionEquals2());
            forinret.add(new ActionNot());
            ActionIf aforinif = new ActionIf(0);
            forinret.add(aforinif);
            aforinif.setJumpOffset(-Action.actionsToBytes(forinret, false, SWF.DEFAULT_VERSION).length);
            ret.addAll(forinret);
        }
        if (value == null) {
            ret.add(new ActionPush(Undefined.INSTANCE));
        } else {
            ret.addAll(value.toSource(localData, generator));
        }
        ret.add(new ActionReturn());
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
