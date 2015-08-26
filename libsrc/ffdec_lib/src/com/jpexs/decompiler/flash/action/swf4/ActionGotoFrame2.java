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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.GotoFrame2ActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ActionGotoFrame2 extends Action {

    boolean sceneBiasFlag;

    boolean playFlag;

    public int sceneBias;

    @Reserved
    int reserved;

    public ActionGotoFrame2(boolean playFlag, boolean sceneBiasFlag, int sceneBias) {
        super(0x9F, 0);
        this.sceneBiasFlag = sceneBiasFlag;
        this.playFlag = playFlag;
        this.sceneBias = sceneBias;
    }

    public ActionGotoFrame2(int actionLength, SWFInputStream sis) throws IOException {
        super(0x9F, actionLength);
        reserved = (int) sis.readUB(6, "reserved");
        sceneBiasFlag = sis.readUB(1, "sceneBiasFlag") == 1;
        playFlag = sis.readUB(1, "playFlag") == 1;
        if (sceneBiasFlag) {
            sceneBias = sis.readUI16("sceneBias");
        }
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUB(6, reserved);
        sos.writeUB(1, sceneBiasFlag ? 1 : 0);
        sos.writeUB(1, playFlag ? 1 : 0);
        if (sceneBiasFlag) {
            sos.writeUI16(sceneBias);
        }
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        int res = 1;
        if (sceneBiasFlag) {
            res += 2;
        }

        return res;
    }

    @Override
    public String toString() {
        return "GotoFrame2 " + sceneBiasFlag + " " + playFlag + " " + (sceneBiasFlag ? " " + sceneBias : "");
    }

    public ActionGotoFrame2(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x9F, -1);
        sceneBiasFlag = lexBoolean(lexer);
        playFlag = lexBoolean(lexer);
        if (sceneBiasFlag) {
            sceneBias = (int) lexLong(lexer);
        }
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem frame = stack.pop();
        output.add(new GotoFrame2ActionItem(this, frame, sceneBiasFlag, playFlag, sceneBias));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
