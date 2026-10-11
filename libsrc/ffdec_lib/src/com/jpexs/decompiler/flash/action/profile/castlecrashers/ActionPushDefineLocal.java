/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.profile.castlecrashers;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ConstantPoolAwareAction;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.SkipNextAction;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.GetVersionActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ActionPushDefineLocal extends Action implements ConstantPoolAwareAction, SkipNextAction {

    public Object name = null;

    public Object value;

    public List<String> constantPool = null;

    public ActionPushDefineLocal(FlasmLexer lexer, List<String> constantPool, String charset) throws IOException, ActionParseException {
        super(0xA4, 0, charset);
        ASMParsedSymbol symb = lexer.lex();
        if (symb.type != ASMParsedSymbol.TYPE_STRING) {
            throw new ActionParseException("String expected", lexer.yyline());
        }
        name = symb.value;
        symb = lexer.lex();
        boolean valueExpected = false;
        if (symb.type == ASMParsedSymbol.TYPE_COMMA) {
            valueExpected = true;
        } else {
            lexer.pushback(symb);
        }
        value = ActionPush.parseValue(lexer, constantPool, valueExpected, 1);
    }

    public ActionPushDefineLocal(int actionLength, SWFInputStream sis, String charset) throws IOException {
        super(0xA4, actionLength, Utf8Helper.charsetName);
        DumpInfo di = sis.dumpInfo;
        sis = sis.getLimitedStream(actionLength);
        sis.dumpInfo = di;

        int type = sis.readUI8("type");
        switch (type) {
            case 0:
                name = sis.readString("name");
                break;
            case 8:
                name = new ConstantIndex(sis.readUI8("nameIndex"));
                break;
            case 9:
                name = new ConstantIndex(sis.readUI16("nameIndex"));
                break;
            default:
                throw new IllegalArgumentException("Unknown name type: " + type);
        }

        value = ActionPush.readSingleValue(sis);
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        ActionPush push = new ActionPush(new Object[]{name, value}, charset);
        push.setConstantPool(constantPool);
        push.execute(lda);
        return new ActionDefineLocal().execute(lda);
    }

    @Override
    public String toString() {
        String ret = "";
        if (name instanceof ConstantIndex) {
            ret = ((ConstantIndex) name).toString(constantPool, Configuration.resolveConstants.get());
        } else if (name instanceof String) {
            ret = "\"" + Helper.escapeActionScriptString((String) name) + "\"";
        }
        return "CC_PushDefineLocal " + ret + ", " + ActionPush.valueToString(value, constantPool);
    }

    @Override
    protected int getContentBytesLength() {
        return ActionPush.getValueContentBytesLength(name, charset)
                + ActionPush.getValueContentBytesLength(value, charset);
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        if (name instanceof ConstantIndex) {
            ConstantIndex constantIndex = (ConstantIndex) name;
            if (constantIndex.index <= 255) {
                sos.writeUI8(1);
                sos.writeUI8(constantIndex.index);
            } else {
                sos.writeUI8(2);
                sos.writeUI16(constantIndex.index);
            }
        } else if (name instanceof String) {
            sos.writeUI8(0);
            sos.writeString((String) name);
        }

        ActionPush.getValueContentBytes(value, sos);
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        DirectValueActionItem valueDirectVal = new DirectValueActionItem(this, lineStartAction, 0, value, constantPool);
        DirectValueActionItem nameDirectVal = new DirectValueActionItem(this, lineStartAction, 0, name, constantPool);
        
        String nameStr;
        if (name instanceof DirectValueActionItem) {
            nameStr = nameDirectVal.toStringNoQuotes(LocalData.empty);
        } else {
            nameStr = EcmaScript.toString(nameDirectVal.getResult());
        }
        variables.put(nameStr, valueDirectVal);
        stack.addToOutput(new DefineLocalActionItem(this, lineStartAction, nameDirectVal, valueDirectVal));
    }

    @Override
    public void setConstantPool(List<String> constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public List<String> getConstantPool() {
        return this.constantPool;
    }
}
