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
package com.jpexs.decompiler.flash.action.swf3;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.DisplayObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GotoFrame2ActionItem;
import com.jpexs.decompiler.flash.action.model.GotoFrameActionItem;
import com.jpexs.decompiler.flash.action.model.PlayActionItem;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class ActionPlay extends Action {

    public ActionPlay() {
        super(0x06, 0);
    }

    @Override
    public String toString() {
        return "Play";
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        ((DisplayObject) lda.target).play();
        return true;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        if (!output.isEmpty() && (output.get(output.size() - 1) instanceof GotoFrameActionItem)) {
            GotoFrameActionItem gta = (GotoFrameActionItem) output.remove(output.size() - 1);
            output.add(new GotoFrame2ActionItem(this, lineStartAction, new DirectValueActionItem(gta.frame + 1), false, true, 0));
        } else {
            output.add(new PlayActionItem(this, lineStartAction));
        }
    }
}
