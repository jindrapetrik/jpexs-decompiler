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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.DisplayObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.GotoFrameActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class ActionGotoFrame extends Action {

    public int frame;

    public ActionGotoFrame(int frame) {
        super(0x81, 2);
        this.frame = frame;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        ((DisplayObject) lda.target).gotoFrame(frame);
        return true;
    }

    public ActionGotoFrame(int actionLength, SWFInputStream sis) throws IOException {
        super(0x81, actionLength);
        frame = sis.readUI16("frame");
    }

    @Override
    public String toString() {
        return "GotoFrame " + frame;
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI16(frame);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 2;
    }

    public ActionGotoFrame(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x81, 0);
        frame = (int) lexLong(lexer);
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        output.add(new GotoFrameActionItem(this, lineStartAction, frame));
    }
}
