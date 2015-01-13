/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.model.SetTargetActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ActionSetTarget extends Action {

    public String targetName;

    public ActionSetTarget(String targetName) {
        super(0x8B, 0);
        this.targetName = targetName;
    }

    public ActionSetTarget(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8B, actionLength);
        //byte[] data = sis.readBytes(actionLength);
        //sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        targetName = sis.readString("targetName");
    }

    @Override
    public String toString() {
        return "SetTarget \"" + Helper.escapeString(targetName) + "\"";
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(targetName);
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionSetTarget(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x8B, -1);
        targetName = lexString(lexer);
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        output.add(new SetTargetActionItem(this, targetName));
    }
}
