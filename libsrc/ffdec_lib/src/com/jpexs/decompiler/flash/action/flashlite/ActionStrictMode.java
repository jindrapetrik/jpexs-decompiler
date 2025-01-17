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
package com.jpexs.decompiler.flash.action.flashlite;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.StrictModeActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StrictMode action - set strict mode.
 *
 * @author JPEXS
 */
public class ActionStrictMode extends Action {

    /**
     * Mode
     */
    public int mode;

    /**
     * Constructor
     * @param mode Mode
     */
    public ActionStrictMode(int mode) {
        super(0x89, 1, Utf8Helper.charsetName);
        this.mode = mode;
    }       
    
    /**
     * Constructor
     * @param actionLength Action length
     * @param sis SWF input stream
     * @throws IOException On I/O error
     */
    public ActionStrictMode(int actionLength, SWFInputStream sis) throws IOException {
        super(0x89, actionLength, sis.getCharset());
        mode = sis.readUI8("mode");
    }

    /**
     * Constructor
     * @param lexer Flasm lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionStrictMode(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x89, 1, charset);
        mode = (int) lexLong(lexer);
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI8(mode);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 1;
    }

    @Override
    public String toString() {
        return "StrictMode " + mode;
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        return true; //TODO?
    }

    @Override
    public void translate(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartItem, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        if (mode != 1) {
            return;
        }
        output.add(new StrictModeActionItem(this, lineStartItem, mode));
    }
}
