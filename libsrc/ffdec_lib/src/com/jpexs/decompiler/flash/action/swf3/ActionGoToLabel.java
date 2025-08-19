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
package com.jpexs.decompiler.flash.action.swf3;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.DisplayObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.GotoLabelActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GoToLabel action - Jumps to a label in the current timeline.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class ActionGoToLabel extends Action {

    /**
     * Label
     */
    public String label;

    /**
     * Constructor
     * @param label Label
     * @param charset Charset
     */
    public ActionGoToLabel(String label, String charset) {
        super(0x8C, 0, charset);
        this.label = label;
    }

    /**
     * Constructor
     * @param actionLength Length of action
     * @param sis SWF input stream
     * @param version SWF version
     * @throws IOException On I/O error
     */
    public ActionGoToLabel(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8C, actionLength, sis.getCharset());
        //byte[] data = sis.readBytes(actionLength);
        //sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        label = sis.readString("label");
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        ((DisplayObject) lda.target).gotoLabel(label);
        return true;
    }

    @Override
    public String toString() {
        return "GoToLabel \"" + Helper.escapeActionScriptString(label) + "\"";
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeString(label);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return Utf8Helper.getBytesLength(label, charset) + 1;
    }

    /**
     * Constructor
     * @param lexer Flasm lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionGoToLabel(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x8C, -1, charset);
        label = lexString(lexer);
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        output.add(new GotoLabelActionItem(this, lineStartAction, label));
    }
}
