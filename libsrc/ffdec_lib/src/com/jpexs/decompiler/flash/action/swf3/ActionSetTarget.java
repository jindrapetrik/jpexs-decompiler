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
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.SetTargetActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class ActionSetTarget extends Action {

    public String targetName;

    public ActionSetTarget(String targetName) {
        super(0x8B, 0);
        this.targetName = targetName;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (targetName.equals("/")) {
            lda.target = lda.stage;
            return true;
        }

        lda.target = lda.stage.getMember(targetName);
        return true;
    }

    public ActionSetTarget(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8B, actionLength);
        //byte[] data = sis.readBytes(actionLength);
        //sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        targetName = sis.readString("targetName");
    }

    @Override
    public String toString() {
        return "SetTarget \"" + Helper.escapeActionScriptString(targetName) + "\"";
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeString(targetName);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return Utf8Helper.getBytesLength(targetName) + 1;
    }

    public ActionSetTarget(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x8B, -1);
        targetName = lexString(lexer);
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        output.add(new SetTargetActionItem(this, lineStartAction, targetName));
    }
}
