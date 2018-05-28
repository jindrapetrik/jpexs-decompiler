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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionScriptFunction;
import com.jpexs.decompiler.flash.action.ActionScriptObject;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
@SWFVersion(from = 7)
public class ActionDefineFunction2 extends Action implements GraphSourceItemContainer {

    public String functionName;

    public String replacedFunctionName;

    public List<String> paramNames = new ArrayList<>();

    public List<String> replacedParamNames;

    public List<Integer> paramRegisters = new ArrayList<>();

    public boolean preloadParentFlag;

    public boolean preloadRootFlag;

    public boolean suppressSuperFlag;

    public boolean preloadSuperFlag;

    public boolean suppressArgumentsFlag;

    public boolean preloadArgumentsFlag;

    public boolean suppressThisFlag;

    public boolean preloadThisFlag;

    @Reserved
    public int reserved;

    public boolean preloadGlobalFlag;

    public int registerCount;

    public int codeSize;

    private int version;

    public List<String> constantPool;

    @Override
    public boolean execute(LocalDataArea lda) {
        ActionScriptFunction f = new ActionScriptFunction(fileOffset, codeSize, functionName, paramNames, getRegNames());
        lda.push(f);
        lda.functions.add(f);
        ((ActionScriptObject) lda.target).setMember(functionName, f);
        return true;
    }

    public ActionDefineFunction2(String functionName, boolean preloadParentFlag, boolean preloadRootFlag, boolean suppressSuperFlag, boolean preloadSuperFlag, boolean suppressArgumentsFlag, boolean preloadArgumentsFlag, boolean suppressThisFlag, boolean preloadThisFlag, boolean preloadGlobalFlag, int registerCount, int codeSize, int version, List<String> paramNames, List<Integer> paramRegisters) {
        super(0x8E, 0);
        this.functionName = functionName;
        this.preloadParentFlag = preloadParentFlag;
        this.preloadRootFlag = preloadRootFlag;
        this.suppressSuperFlag = suppressSuperFlag;
        this.preloadSuperFlag = preloadSuperFlag;
        this.suppressArgumentsFlag = suppressArgumentsFlag;
        this.preloadArgumentsFlag = preloadArgumentsFlag;
        this.suppressThisFlag = suppressThisFlag;
        this.preloadThisFlag = preloadThisFlag;
        this.preloadGlobalFlag = preloadGlobalFlag;
        this.registerCount = registerCount;
        this.codeSize = codeSize;
        this.version = version;
        this.paramNames = paramNames;
        this.paramRegisters = paramRegisters;
    }

    public ActionDefineFunction2(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x8E, actionLength);
        this.version = version;
        functionName = sis.readString("functionName");
        int numParams = sis.readUI16("numParams");
        registerCount = sis.readUI8("registerCount");
        preloadParentFlag = sis.readUB(1, "preloadParentFlag") == 1;
        preloadRootFlag = sis.readUB(1, "preloadRootFlag") == 1;
        suppressSuperFlag = sis.readUB(1, "suppressSuperFlag") == 1;
        preloadSuperFlag = sis.readUB(1, "preloadSuperFlag") == 1;
        suppressArgumentsFlag = sis.readUB(1, "suppressArgumentsFlag") == 1;
        preloadArgumentsFlag = sis.readUB(1, "preloadArgumentsFlag") == 1;
        suppressThisFlag = sis.readUB(1, "suppressThisFlag") == 1;
        preloadThisFlag = sis.readUB(1, "preloadThisFlag") == 1;
        reserved = (int) sis.readUB(7, "reserved");
        preloadGlobalFlag = sis.readUB(1, "preloadGlobalFlag") == 1;
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add(sis.readUI8("paramRegister"));
            paramNames.add(sis.readString("paramName"));
        }
        codeSize = sis.readUI16("codeSize");
    }

    public ActionDefineFunction2(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x8E, -1);
        functionName = lexString(lexer);
        int numParams = (int) lexLong(lexer);
        registerCount = (int) lexLong(lexer);
        preloadParentFlag = lexBoolean(lexer);
        preloadRootFlag = lexBoolean(lexer);
        suppressSuperFlag = lexBoolean(lexer);
        preloadSuperFlag = lexBoolean(lexer);
        suppressArgumentsFlag = lexBoolean(lexer);
        preloadArgumentsFlag = lexBoolean(lexer);
        suppressThisFlag = lexBoolean(lexer);
        preloadThisFlag = lexBoolean(lexer);
        preloadGlobalFlag = lexBoolean(lexer);
        for (int i = 0; i < numParams; i++) {
            paramRegisters.add((int) lexLong(lexer));
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
        sos.writeUI8(registerCount);
        sos.writeUB(1, preloadParentFlag ? 1 : 0);
        sos.writeUB(1, preloadRootFlag ? 1 : 0);
        sos.writeUB(1, suppressSuperFlag ? 1 : 0);
        sos.writeUB(1, preloadSuperFlag ? 1 : 0);
        sos.writeUB(1, suppressArgumentsFlag ? 1 : 0);
        sos.writeUB(1, preloadArgumentsFlag ? 1 : 0);
        sos.writeUB(1, suppressThisFlag ? 1 : 0);
        sos.writeUB(1, preloadThisFlag ? 1 : 0);
        sos.writeUB(7, reserved);
        sos.writeUB(1, preloadGlobalFlag ? 1 : 0);
        for (int i = 0; i < paramNames.size(); i++) {
            sos.writeUI8(paramRegisters.get(i));
            sos.writeString(paramNames.get(i));
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
        int res = Utf8Helper.getBytesLength(functionName) + 8;
        for (int i = 0; i < paramNames.size(); i++) {
            res += Utf8Helper.getBytesLength(paramNames.get(i)) + 2;
        }

        return res;
    }

    @Override
    public GraphTextWriter getASMSourceReplaced(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode, GraphTextWriter writer) {
        List<String> oldParamNames = paramNames;
        if (replacedParamNames != null) {
            paramNames = replacedParamNames;
        }
        String oldFunctionName = functionName;
        if (replacedFunctionName != null) {
            functionName = replacedFunctionName;
        }
        String ret = getASMSource(container, knownAddreses, exportMode);
        paramNames = oldParamNames;
        functionName = oldFunctionName;
        writer.appendNoHilight(ret);
        return writer;

    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode) {
        StringBuilder paramStr = new StringBuilder();
        for (int i = 0; i < paramNames.size(); i++) {
            paramStr.append(paramRegisters.get(i)).append(" \"").append(Helper.escapeActionScriptString(paramNames.get(i))).append("\" ");
        }

        return ("DefineFunction2 \"" + Helper.escapeActionScriptString(functionName) + "\" " + paramRegisters.size() + " " + registerCount
                + " " + preloadParentFlag
                + " " + preloadRootFlag
                + " " + suppressSuperFlag
                + " " + preloadSuperFlag
                + " " + suppressArgumentsFlag
                + " " + preloadArgumentsFlag
                + " " + suppressThisFlag
                + " " + preloadThisFlag
                + " " + preloadGlobalFlag).trim() + " " + paramStr + " {" + (codeSize == 0 ? "\r\n}" : "");// + "\r\n" + Action.actionsToString(getAddress() + getHeaderLength(), getItems(container), knownAddreses, constantPool, version, hex, getFileAddress() + hdrSize) + "}";
    }

    @Override
    public String toString() {
        return "DefineFunction2";
    }

    public int getFirstRegister() {
        int pos = 1;
        if (preloadThisFlag) {
            pos++;
        }
        if (preloadArgumentsFlag) {
            pos++;
        }
        if (preloadSuperFlag) {
            pos++;
        }
        if (preloadRootFlag) {
            pos++;
        }
        if (preloadParentFlag) {
            pos++;
        }
        if (preloadGlobalFlag) {
            pos++;
        }
        return pos;
    }

    @Override
    public void translate(boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        HashMap<Integer, String> funcRegNames = new HashMap<>();
        for (int f = 0; f < paramNames.size(); f++) {
            int reg = paramRegisters.get(f);
            if (reg != 0) {
                funcRegNames.put(reg, paramNames.get(f));
            }
        }
        int pos = 1;
        if (preloadThisFlag) {
            funcRegNames.put(pos, "this");
            pos++;
        }
        if (preloadArgumentsFlag) {
            funcRegNames.put(pos, "arguments");
            pos++;
        }
        if (preloadSuperFlag) {
            funcRegNames.put(pos, "super");
            pos++;
        }
        if (preloadRootFlag) {
            funcRegNames.put(pos, "_root");
            pos++;
        }
        if (preloadParentFlag) {
            funcRegNames.put(pos, "_parent");
            pos++;
        }
        if (preloadGlobalFlag) {
            funcRegNames.put(pos, "_global");
            pos++;
        }
        return funcRegNames;
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
        FunctionActionItem fti = new FunctionActionItem(this, lineStartItem, functionName, paramNames, getRegNames(), content.get(0), constantPool, getFirstRegister(), new ArrayList<>(), funcList);
        functions.put(functionName, fti);
        stack.push(fti);
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
    public boolean parseDivision(long size, FlasmLexer lexer) {
        codeSize = (int) (size - getHeaderSize());
        return false;
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
