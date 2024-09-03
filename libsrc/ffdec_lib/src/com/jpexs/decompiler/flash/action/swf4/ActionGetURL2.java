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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetURL2ActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesNumActionItem;
import com.jpexs.decompiler.flash.action.model.PrintActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapNumActionItem;
import com.jpexs.decompiler.flash.action.model.PrintNumActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GetURL2 action - Gets a URL, stack-based.
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class ActionGetURL2 extends Action {

    /**
     * Send variables method
     */
    public int sendVarsMethod;

    /**
     * GET method
     */
    public static final int GET = 1;

    /**
     * POST method
     */
    public static final int POST = 2;

    /**
     * Load target flag
     */
    public boolean loadTargetFlag;

    /**
     * Load variables flag
     */
    public boolean loadVariablesFlag;

    /**
     * Reserved
     */
    @Reserved
    public int reserved;

    /**
     * Constructor
     * @param sendVarsMethod Send variables method
     * @param loadVariablesFlag Load variables flag
     * @param loadTargetFlag Load target flag
     * @param charset Charset
     */
    public ActionGetURL2(int sendVarsMethod, boolean loadVariablesFlag, boolean loadTargetFlag, String charset) {
        super(0x9A, 1, charset);
        this.loadTargetFlag = loadTargetFlag;
        this.loadVariablesFlag = loadVariablesFlag;
        this.sendVarsMethod = sendVarsMethod;
    }

    /**
     * Constructor
     * @param actionLength Action length
     * @param sis SWF input stream
     * @param charset Charset
     * @throws IOException Error reading data
     */
    public ActionGetURL2(int actionLength, SWFInputStream sis, String charset) throws IOException {
        super(0x9A, actionLength, charset);
        loadVariablesFlag = sis.readUB(1, "loadVariablesFlag") == 1;
        loadTargetFlag = sis.readUB(1, "loadTargetFlag") == 1;
        reserved = (int) sis.readUB(4, "reserved");
        sendVarsMethod = (int) sis.readUB(2, "sendVarsMethod"); //This is first in documentation, which is WRONG!
    }

    @Override
    public String toString() {
        return "GetURL2 " + loadVariablesFlag + ", " + loadTargetFlag + ", " + sendVarsMethod;
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUB(1, loadVariablesFlag ? 1 : 0);
        sos.writeUB(1, loadTargetFlag ? 1 : 0);
        sos.writeUB(4, reserved);
        sos.writeUB(2, sendVarsMethod);
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

    /**
     * Constructor
     * @param lexer Lexer
     * @param charset Charset
     * @throws IOException On I/O error
     * @throws ActionParseException On error parsing action
     */
    public ActionGetURL2(FlasmLexer lexer, String charset) throws IOException, ActionParseException {
        super(0x9A, -1, charset);

        ASMParsedSymbol symb = lexer.lex();
        boolean sendVarsMethodLast = false;
        if (symb.type == ASMParsedSymbol.TYPE_BOOLEAN) { //backwards compatibility. In 19.1.0 up to 20.0.0 sendVarsMethod is first
            sendVarsMethodLast = true;
        }
        lexer.pushback(symb);
        if (!sendVarsMethodLast) {
            sendVarsMethod = (int) lexLong(lexer);
            lexOptionalComma(lexer);
        }
        loadVariablesFlag = lexBoolean(lexer);
        lexOptionalComma(lexer);
        loadTargetFlag = lexBoolean(lexer);
        if (sendVarsMethodLast) {
            lexOptionalComma(lexer);
            sendVarsMethod = (int) lexLong(lexer);
        }
    }

    @Override
    public boolean execute(LocalDataArea lda) {
        if (!lda.stackHasMinSize(2)) {
            return false;
        }

        String target = EcmaScript.toString(lda.pop());
        String urlString = EcmaScript.toString(lda.pop());

        //TODO: Execute - Connection
        return true;
    }

    @Override
    public void translate(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem targetString = stack.pop();
        GraphTargetItem urlString = stack.pop();
        Integer num = null;
        if (targetString.isCompileTime()) {
            Object res = targetString.getResult();
            if (res instanceof String) {
                String tarStr = (String) res;
                String levelPrefix = "_level";
                if (tarStr.startsWith(levelPrefix)) {
                    try {
                        num = Integer.valueOf(tarStr.substring(levelPrefix.length()));
                    } catch (NumberFormatException nfe) {
                        //ignored
                    }
                }
            }
        }
        if (loadVariablesFlag) {
            if (num != null) {
                output.add(new LoadVariablesNumActionItem(this, lineStartAction, urlString, new DirectValueActionItem(null, null, 0, (Long) (long) (int) num, new ArrayList<>()), sendVarsMethod));
            } else {
                output.add(new LoadVariablesActionItem(this, lineStartAction, urlString, targetString, sendVarsMethod));
            }
        } else if (loadTargetFlag) {
            if ((urlString instanceof DirectValueActionItem) && ("".equals(urlString.getResult()))) {
                output.add(new UnLoadMovieActionItem(this, lineStartAction, targetString));
            } else {
                output.add(new LoadMovieActionItem(this, lineStartAction, urlString, targetString, sendVarsMethod));
            }
        } else {
            String printPrefix = "print:#";
            String printAsBitmapPrefix = "printasbitmap:#";
            String urlStr = null;
            if (urlString.isCompileTime() && (urlString.getResult() instanceof String)) {
                urlStr = (String) urlString.getResult();
            }

            if (num != null) {
                if ("".equals(urlStr)) {
                    output.add(new UnLoadMovieNumActionItem(this, lineStartAction, new DirectValueActionItem(null, null, 0, (Long) (long) (int) num, new ArrayList<>())));
                } else if (urlStr != null && urlStr.startsWith(printPrefix)) {
                    output.add(new PrintNumActionItem(this, lineStartAction, new DirectValueActionItem((Long) (long) (int) num),
                            new DirectValueActionItem(urlStr.substring(printPrefix.length()))));
                } else if (urlStr != null && urlStr.startsWith(printAsBitmapPrefix)) {
                    output.add(new PrintAsBitmapNumActionItem(this, lineStartAction, new DirectValueActionItem((Long) (long) (int) num), new DirectValueActionItem(urlStr.substring(printAsBitmapPrefix.length()))));
                } else {
                    output.add(new LoadMovieNumActionItem(this, lineStartAction, urlString, new DirectValueActionItem(null, null, 0, (Long) (long) (int) num, new ArrayList<>()), sendVarsMethod));
                }
            } else if (urlStr != null && urlStr.startsWith(printPrefix)) {
                output.add(new PrintActionItem(this, lineStartAction, targetString, new DirectValueActionItem(urlStr.substring(printPrefix.length()))));
            } else if (urlStr != null && urlStr.startsWith(printAsBitmapPrefix)) {
                output.add(new PrintAsBitmapActionItem(this, lineStartAction, targetString, new DirectValueActionItem(urlStr.substring(printAsBitmapPrefix.length()))));
            } else {
                output.add(new GetURL2ActionItem(this, lineStartAction, urlString, targetString, sendVarsMethod));
            }
        }
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
