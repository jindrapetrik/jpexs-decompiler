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

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ConstantPoolAwareAction;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.SkipNextAction;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
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
public class ActionPushSetMember extends Action implements ConstantPoolAwareAction, SkipNextAction {

    public Object memberName = null;

    public Object value = null;

    public List<String> constantPool = null;

    public ActionPushSetMember(FlasmLexer lexer, List<String> constantPool, String charset) throws IOException, ActionParseException {
        super(0xA3, 0, charset);

        memberName = ActionPush.parseValue(lexer, constantPool, false, 0);

        ASMParsedSymbol symb = lexer.lex();
        boolean valueExpected = false;
        if (symb.type == ASMParsedSymbol.TYPE_COMMA) {
            valueExpected = true;
        } else {
            lexer.pushback(symb);
        }

        value = ActionPush.parseValue(lexer, constantPool, valueExpected, 1);
    }

    public ActionPushSetMember(int actionLength, SWFInputStream sis, String charset) throws IOException {
        super(0xA3, actionLength, Utf8Helper.charsetName);
        DumpInfo di = sis.dumpInfo;
        sis = sis.getLimitedStream(actionLength);
        sis.dumpInfo = di;
        memberName = ActionPush.readSingleValue(sis);
        value = ActionPush.readSingleValue(sis);
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(1)) {
            return false;
        }

        ActionPush push = new ActionPush(new Object[]{memberName, value}, charset);
        push.setConstantPool(constantPool);
        push.execute(lda);
        return new ActionSetMember().execute(lda);
    }

    @Override
    public String toString() {
        return "CC_PushSetMember " + ActionPush.valueToString(memberName, constantPool) + ", " + ActionPush.valueToString(value, constantPool);
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        ActionPush.getValueContentBytes(memberName, sos);
        ActionPush.getValueContentBytes(value, sos);
    }

    @Override
    protected int getContentBytesLength() {
        return ActionPush.getValueContentBytesLength(memberName, charset)
                + ActionPush.getValueContentBytesLength(value, charset);
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        DirectValueActionItem memberNameDirectVal = new DirectValueActionItem(this, lineStartAction, 0, memberName, constantPool);
        DirectValueActionItem valueDirectVal = new DirectValueActionItem(this, lineStartAction, 0, value, constantPool);
        GraphTargetItem object = stack.pop();

        stack.finishBlock(output);
        stack.allowSwap(output);
        stack.moveToStack(output);

        ActionSetMember.handleSetMember(false, valueDirectVal, memberNameDirectVal, object, this, usedDeobfuscations, uninitializedClassTraits, secondPassData, insideDoInitAction, lineStartAction, stack, output, regNames, variables, functions, staticOperation, path);
    }

    @Override
    public void setConstantPool(List<String> constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public List<String> getConstantPool() {
        return this.constantPool;
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 1;
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return 0;
    }
}
