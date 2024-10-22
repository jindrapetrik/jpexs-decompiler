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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionScriptFunction;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DefineFunction action - Defines a function.
 *
 * @author JPEXS
 */
@SWFVersion(from = 5)
public class ActionDefineFunction extends Action implements GraphSourceItemContainer {

    /**
     * Function name
     */
    public String functionName;

    /**
     * Replaced function name
     */
    public String replacedFunctionName;

    /**
     * Parameter names
     */
    public List<String> paramNames = new ArrayList<>();

    /**
     * Replaced parameter names
     */
    public List<String> replacedParamNames;

    /**
     * Code size
     */
    public int codeSize;

    /**
     * Version
     */
    private int version;

    /**
     * Constant pool
     */
    public List<String> constantPool;

    @Override
    public boolean execute(LocalDataArea lda) {
        ActionScriptFunction f = new ActionScriptFunction(fileOffset, codeSize, functionName, paramNames, getRegNames());
        lda.push(f);
        lda.functions.add(f);
        ((ActionScriptObject) lda.target).setMember(functionName, f);
        return true;
    }

    /**
     * Constructor.
     * @param functionName Function name
     * @param paramNames Parameter names
     * @param codeSize Code size
     * @param version Version
     * @param charset Charset
     */
    public ActionDefineFunction(String functionName, List<String> paramNames, int codeSize, int version, String charset) {
        super(0x9B, 0, charset);
        this.functionName = functionName;
        this.codeSize = codeSize;
        this.version = version;
        this.paramNames = paramNames;
    }

    /**
     * Constructor.
     * @param actionLength Action length
     * @param sis SWF input stream
     * @param version Version
     * @throws IOException On I/O error
     */
    public ActionDefineFunction(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x9B, actionLength, sis.getCharset());
        this.version = version;
        functionName = sis.readString("functionName");
        int numParams = sis.readUI16("numParams");
        for (int i = 0; i < numParams; i++) {
            paramNames.add(sis.readString("paramName"));
        }
        codeSize = sis.readUI16("codeSize");
    }

    /**
     * Constructor.
     * @param lexer Flasm lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On action parse error
     */
    public ActionDefineFunction(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x9B, -1, charset);
        functionName = lexString(lexer);
        lexOptionalComma(lexer);
        int numParams = (int) lexLong(lexer);
        for (int i = 0; i < numParams; i++) {
            lexOptionalComma(lexer);
            paramNames.add(lexString(lexer));
        }
        lexBlockOpen(lexer);
    }

    @Override
    public long getHeaderSize() {
        return getBytesLength();
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeString(functionName);
        sos.writeUI16(paramNames.size());
        for (String s : paramNames) {
            sos.writeString(s);
        }
        sos.writeUI16(codeSize);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        int res = Utf8Helper.getBytesLength(functionName) + 5;
        for (String s : paramNames) {
            res += Utf8Helper.getBytesLength(s) + 1;
        }

        return res;
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode) {
        StringBuilder paramStr = new StringBuilder();
        for (int i = 0; i < paramNames.size(); i++) {
            paramStr.append(", ");
            paramStr.append("\"").append(Helper.escapeActionScriptString(paramNames.get(i))).append("\" ");
        }

        return "DefineFunction \"" + Helper.escapeActionScriptString(functionName) + "\", " + paramNames.size() + paramStr + " {" + (codeSize == 0 ? "\r\n}" : "");
    }

    @Override
    public GraphTextWriter getASMSourceReplaced(ActionList container, Set<Long> knownAddresses, ScriptExportMode exportMode, GraphTextWriter writer) {
        List<String> oldParamNames = paramNames;
        if (replacedParamNames != null) {
            paramNames = replacedParamNames;
        }
        String oldFunctionName = functionName;
        if (replacedFunctionName != null) {
            functionName = replacedFunctionName;
        }
        String ret = getASMSource(container, knownAddresses, exportMode);
        paramNames = oldParamNames;
        functionName = oldFunctionName;
        writer.appendNoHilight(ret);
        return writer;

    }

    @Override
    public void translate(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        return new HashMap<>();
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> content, GraphSourceItem lineStartItem, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        List<FunctionActionItem> funcList = new ArrayList<>();
        for (String key : functions.keySet()) {
            GraphTargetItem val = functions.get(key);
            if (val instanceof FunctionActionItem) {
                funcList.add((FunctionActionItem) val);
            }
        }
        FunctionActionItem fti = new FunctionActionItem(this, lineStartItem, functionName, paramNames, getRegNames(), content.get(0), constantPool, 1, new ArrayList<>(), funcList, false /*actually unknown*/);
        //ActionGraph.translateViaGraph(regNames, variables, functions, code, version)
        stack.push(fti);
        functions.put(functionName, fti);
    }

    @Override
    public String toString() {
        return "DefineFunction";
    }

    @Override
    public boolean parseDivision(long size, FlasmLexer lexer) {
        codeSize = (int) (size - getHeaderSize());
        return false;
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<>();
        ret.add((Long) (long) codeSize);
        return ret;
    }

    @Override
    public void setContainerSize(int index, long size) {
        if (index == 0) {
            codeSize = (int) size;
        } else {
            throw new IllegalArgumentException("Index must be 0.");
        }
    }

    @Override
    public String getASMSourceBetween(int pos) {
        return "";
    }

    @Override
    public String getName() {
        return "function";
    }
}
