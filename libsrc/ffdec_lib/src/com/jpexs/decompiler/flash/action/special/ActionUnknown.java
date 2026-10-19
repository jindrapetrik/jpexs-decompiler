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
package com.jpexs.decompiler.flash.action.special;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unknown action.
 *
 * @author JPEXS
 */
public class ActionUnknown extends Action {

    /**
     * Raw action data.
     */
    public ByteArrayRange unknownData;

    /**
     * Constructor.
     * @param actionCode Action code
     * @param actionLength Action length
     * @param charset Charset
     */
    public ActionUnknown(int actionCode, int actionLength, String charset) {
        super(actionCode, actionLength, charset);
        unknownData = new ByteArrayRange(new byte[actionLength]);
    }

    /**
     * Constructor.
     * @param actionCode Action code
     * @param actionLength Action length
     * @param sis SWF input stream
     * @param charset Charset
     * @throws IOException On I/O error
     */
    public ActionUnknown(int actionCode, int actionLength, SWFInputStream sis, String charset) throws IOException {
        super(actionCode, actionLength, charset);
        unknownData = sis.readByteRangeEx(actionLength, "unknownData");
    }

    /**
     * Constructor from p-code.
     * @param actionCode Action code
     * @param lexer Lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On invalid data
     */
    public ActionUnknown(int actionCode, FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(actionCode, 0, charset);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        ASMParsedSymbol symbol = lexer.lex();
        while (symbol.type == ASMParsedSymbol.TYPE_INTEGER) {
            long value = (Long) symbol.value;
            if (actionCode < 0x80) {
                throw new ActionParseException("Short actions cannot have data", lexer.yyline());
            }
            if (value < 0 || value > 0xff) {
                throw new ActionParseException("Byte value expected", lexer.yyline());
            }
            data.write((int) value);
            symbol = lexer.lex();
            if (symbol.type == ASMParsedSymbol.TYPE_COMMA) {
                symbol = lexer.lex();
                if (symbol.type != ASMParsedSymbol.TYPE_INTEGER) {
                    throw new ActionParseException("Byte value expected", lexer.yyline());
                }
            }
        }
        lexer.pushback(symbol);
        unknownData = new ByteArrayRange(data.toByteArray());
        updateLength();
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.write(unknownData);
    }

    @Override
    protected int getContentBytesLength() {
        return unknownData.getLength();
    }

    @Override
    public String toString() {
        byte[] data = unknownData.getRangeData();

        StringBuilder sb = new StringBuilder();
        sb.append("Unknown_").append(Helper.byteToHex((byte) getActionCode()));
        for (int i = 0; i < data.length; i++) {
            sb.append(i == 0 ? " " : ", ");
            sb.append("0x");
            sb.append(Helper.byteToHex(data[i]));
        }
        return sb.toString();
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        return true;
    }

    @Override
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
    }
}
