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
package com.jpexs.decompiler.flash.action.swf3;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.SecondPassException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WaitForFrame action - Waits for a frame to be loaded before continuing.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class ActionWaitForFrame extends Action implements ActionStore {

    /**
     * Frame number
     */
    public int frame;

    /**
     * Skip count
     */
    public int skipCount;

    /**
     * Skipped actions
     */
    public List<Action> skipped;

    @Override
    public boolean execute(LocalDataArea lda) {
        //it's already loaded. TODO: check real loaded?
        return true;
    }

    /**
     * Constructor
     * @param actionLength Length of action
     * @param sis SWF input stream
     * @throws IOException On I/O error
     */
    public ActionWaitForFrame(int actionLength, SWFInputStream sis) throws IOException {
        super(0x8A, actionLength, sis.getCharset());
        frame = sis.readUI16("frame");
        skipCount = sis.readUI8("skipCount");
        skipped = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "WaitForFrame";
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode) {
        String ret = "WaitForFrame " + frame + ", " + skipCount;
        return ret;
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI16(frame);
        sos.writeUI8(skipCount);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 3;
    }

    /**
     * Constructor
     * @param lexer Flasm lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionWaitForFrame(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x8A, -1, charset);
        frame = (int) lexLong(lexer);
        lexOptionalComma(lexer);
        skipCount = (int) lexLong(lexer);
        skipped = new ArrayList<>();
    }

    @Override
    public void translate(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) throws InterruptedException {
        GraphTargetItem frameTi = new DirectValueActionItem(null, null, 0, (Long) ((long) frame), new ArrayList<>());
        List<GraphTargetItem> body;
        HashMap<String, GraphTargetItem> variablesBackup = new LinkedHashMap<>(variables);
        HashMap<String, GraphTargetItem> functionsBackup = new LinkedHashMap<>(functions);

        try {
            body = ActionGraph.translateViaGraph(uninitializedClassTraits, null, insideDoInitAction, true, regNames, variables, functions, skipped, SWF.DEFAULT_VERSION, staticOperation, path, getCharset());
        } catch (SecondPassException spe) {
            variables.clear();
            variables.putAll(variablesBackup);
            functions.clear();
            functions.putAll(functionsBackup);
            body = ActionGraph.translateViaGraph(uninitializedClassTraits, spe.getData(), insideDoInitAction, true, regNames, variables, functions, skipped, SWF.DEFAULT_VERSION, staticOperation, path, getCharset());
        }
        output.add(new IfFrameLoadedActionItem(frameTi, body, this, lineStartAction));
    }

    @Override
    public int getStoreSize() {
        return skipCount;
    }

    @Override
    public void setStore(List<Action> store) {
        skipped = store;
        skipCount = store.size();
    }
}
