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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ActionWaitForFrame2 extends Action implements ActionStore {

    public int skipCount;

    List<Action> skipped;

    public ActionWaitForFrame2(int skipCount) {
        super(0x8D, 1);
        this.skipCount = skipCount;
        skipped = new ArrayList<>();
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

    public ActionWaitForFrame2(int actionLength, SWFInputStream sis) throws IOException {
        super(0x8D, actionLength);
        skipCount = sis.readUI8("skipCount");
        skipped = new ArrayList<>();
        /*for (int i = 0; i < skipCount; i++) {
         Action a = sis.readAction(cpool);
         if (a instanceof ActionEnd) {
         skipCount = i;
         break;
         }
         if (a == null) {
         skipCount = i;
         break;
         }
         skipped.add(a);
         }
         boolean deobfuscate = Configuration.getConfig("autoDeobfuscate", true);
         if (deobfuscate) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         for (int i = 0; i < skipCount; i++) {
         baos.write(skipped.get(i).getBytes(sis.getVersion()));
         }
         baos.write(new ActionEnd().getBytes(sis.getVersion()));
         SWFInputStream sis2 = new SWFInputStream(new ByteArrayInputStream(baos.toByteArray()), sis.getVersion());
         skipped = sis2.readActionList(new ArrayList<DisassemblyListener>(), 0, "");
         if (!skipped.isEmpty()) {
         if (skipped.get(skipped.size() - 1) instanceof ActionEnd) {
         skipped.remove(skipped.size() - 1);
         }
         }
         skipCount = skipped.size();
         }*/
    }

    public ActionWaitForFrame2(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x8D, -1);
        skipCount = (int) lexLong(lexer);
        skipped = new ArrayList<>();
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI8(skipCount);
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
        return "WaitForFrame2";
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode) {
        String ret = "WaitForFrame2 " + skipCount;
        /*for (int i = 0; i < skipped.size(); i++) {
         if (skipped.get(i) instanceof ActionEnd) {
         break;
         }
         ret += "\r\n" + skipped.get(i).getASMSource(container, knownAddreses, version, exportMode);
         }*/
        return ret;
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) throws InterruptedException {
        GraphTargetItem frame = stack.pop();
        List<GraphTargetItem> body = ActionGraph.translateViaGraph(regNames, variables, functions, skipped, SWF.DEFAULT_VERSION, staticOperation, path);
        output.add(new IfFrameLoadedActionItem(frame, body, this));
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }
}
