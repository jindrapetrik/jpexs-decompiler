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
package com.jpexs.decompiler.flash.action.swf1;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.DisplayObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.GotoFrameActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GotoFrame action - Jumps to a frame in the current timeline.
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class ActionGotoFrame extends Action {

    /**
     * Frame number
     */
    public int frame;

    /**
     * Constructor
     * @param frame Frame number
     * @param charset Charset
     */
    public ActionGotoFrame(int frame, String charset) {
        super(0x81, 2, charset);
        this.frame = frame;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        ((DisplayObject) lda.target).gotoFrame(frame);
        return true;
    }

    /**
     * Constructor
     *
     * @param actionLength Length of action
     * @param sis SWF input stream
     * @throws IOException On I/O error
     */
    public ActionGotoFrame(int actionLength, SWFInputStream sis) throws IOException {
        super(0x81, actionLength, sis.getCharset());
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

    /**
     * Constructor
     * @param lexer Flasm lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionGotoFrame(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x81, 0, charset);
        frame = (int) lexLong(lexer);
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        output.add(new GotoFrameActionItem(this, lineStartAction, frame));
    }
}
