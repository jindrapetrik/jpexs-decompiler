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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.LocalDataArea;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommandActionItem;
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
import com.jpexs.decompiler.flash.action.model.operations.StringAddActionItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     *
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
     *
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
     *
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
    public void translate(Set<String> usedDeobfuscations, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, GraphSourceItem lineStartAction, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem targetString = stack.pop();
        GraphTargetItem urlString = stack.pop();
        GraphTargetItem num = null;
        if (targetString.isCompileTime()) {
            Object res = targetString.getResult();
            if (res instanceof String) {
                String tarStr = (String) res;
                String levelPrefix = "_level";
                if (tarStr.startsWith(levelPrefix)) {
                    try {
                        num = new DirectValueActionItem(Long.valueOf(tarStr.substring(levelPrefix.length())));
                    } catch (NumberFormatException nfe) {
                        //ignored
                    }
                }
            }
        }
        if (num == null) {
            if (targetString instanceof StringAddActionItem) {
                StringAddActionItem sa = (StringAddActionItem) targetString;
                if (sa.leftSide.isCompileTime()) {
                    Object res = sa.leftSide.getResult();
                    if (res instanceof String) {
                        String tarStr = (String) res;
                        String levelPrefix = "_level";
                        if (tarStr.equals(levelPrefix)) {
                            num = sa.rightSide;
                        }
                    }
                }
            }
        }

        if (loadVariablesFlag) {
            if (num != null) {
                output.add(new LoadVariablesNumActionItem(this, lineStartAction, urlString, num, sendVarsMethod));
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
            final String printPrefix = "print:#";
            final String printAsBitmapPrefix = "printasbitmap:#";
            final String fscommandPrefix = "FSCommand:";
            GraphTargetItem printType = null;
            boolean doPrint = false;
            boolean doPrintAsBitmap = false;
            boolean doFSCommand = false;
            boolean doUnload = false;

            if (urlString.isCompileTime() && (urlString.getResult() instanceof String)) {
                String urlStr = (String) urlString.getResult();
                if ("".equals(urlStr)) {
                    doUnload = true;
                } else if (urlStr.startsWith(printPrefix)) {
                    printType = new DirectValueActionItem(urlStr.substring(printPrefix.length()));
                    doPrint = true;
                } else if (urlStr.startsWith(printAsBitmapPrefix)) {
                    printType = new DirectValueActionItem(urlStr.substring(printAsBitmapPrefix.length()));
                    doPrintAsBitmap = true;
                } else if (urlStr.startsWith(fscommandPrefix)) {
                    urlString = new DirectValueActionItem(urlStr.substring(fscommandPrefix.length()));
                    doFSCommand = true;
                } else if (urlStr.equals("print:")) {
                    printType = new DirectValueActionItem("bmovie");
                    doPrint = true;
                } else if (urlStr.equals("printasbitmap:")) {
                    printType = new DirectValueActionItem("bmovie");
                    doPrintAsBitmap = true;
                }
            } else if (urlString instanceof StringAddActionItem) {
                StringAddActionItem sa = (StringAddActionItem) urlString;
                if (sa.leftSide.isCompileTime()) {
                    Object res = sa.leftSide.getResult();
                    if (res instanceof String) {
                        String urlStr = (String) res;
                        switch (urlStr) {
                            case printPrefix:
                                printType = sa.rightSide;
                                doPrint = true;
                                urlString = null;
                                break;
                            case printAsBitmapPrefix:
                                printType = sa.rightSide;
                                doPrintAsBitmap = true;
                                urlString = null;
                                break;
                            case fscommandPrefix:
                                urlString = sa.rightSide;
                                doFSCommand = true;
                                break;
                        }
                    }
                }
            }

            if (num != null) {
                if (doUnload) {
                    output.add(new UnLoadMovieNumActionItem(this, lineStartAction, num));
                } else if (doPrint) {
                    output.add(new PrintNumActionItem(this, lineStartAction, num, printType));
                } else if (doPrintAsBitmap) {
                    output.add(new PrintAsBitmapNumActionItem(this, lineStartAction, num, printType));
                } else {
                    output.add(new LoadMovieNumActionItem(this, lineStartAction, urlString, num, sendVarsMethod));
                }
            } else if (doPrint) {
                output.add(new PrintActionItem(this, lineStartAction, targetString, printType));
            } else if (doPrintAsBitmap) {
                output.add(new PrintAsBitmapActionItem(this, lineStartAction, targetString, printType));
            } else if (doFSCommand) {
                output.add(new FSCommandActionItem(this, lineStartAction, urlString, targetString));
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
