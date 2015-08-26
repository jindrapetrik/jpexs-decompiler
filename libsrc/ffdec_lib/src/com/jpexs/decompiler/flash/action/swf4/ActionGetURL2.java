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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
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
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionGetURL2 extends Action {

    public int sendVarsMethod;

    public static final int GET = 1;

    public static final int POST = 2;

    public boolean loadTargetFlag;

    public boolean loadVariablesFlag;

    @Reserved
    public int reserved;

    public ActionGetURL2(int sendVarsMethod, boolean loadVariablesFlag, boolean loadTargetFlag) {
        super(0x9A, 1);
        this.loadTargetFlag = loadTargetFlag;
        this.loadVariablesFlag = loadVariablesFlag;
        this.sendVarsMethod = sendVarsMethod;
    }

    public ActionGetURL2(int actionLength, SWFInputStream sis) throws IOException {
        super(0x9A, actionLength);
        loadVariablesFlag = sis.readUB(1, "loadVariablesFlag") == 1;
        loadTargetFlag = sis.readUB(1, "loadTargetFlag") == 1;
        reserved = (int) sis.readUB(4, "reserved");
        sendVarsMethod = (int) sis.readUB(2, "sendVarsMethod"); //This is first in documentation, which is WRONG!
    }

    @Override
    public String toString() {
        return "GetURL2 " + loadVariablesFlag + " " + loadTargetFlag + " " + sendVarsMethod;
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

    public ActionGetURL2(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x9A, -1);
        loadVariablesFlag = lexBoolean(lexer);
        loadTargetFlag = lexBoolean(lexer);
        sendVarsMethod = (int) lexLong(lexer);
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
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
                    }
                }
            }
        }
        if (loadVariablesFlag) {
            if (num != null) {
                output.add(new LoadVariablesNumActionItem(this, urlString, new DirectValueActionItem(null, 0, (Long) (long) (int) num, new ArrayList<>()), sendVarsMethod));
            } else {
                output.add(new LoadVariablesActionItem(this, urlString, targetString, sendVarsMethod));
            }
        } else if (loadTargetFlag) {
            if ((urlString instanceof DirectValueActionItem) && (urlString.getResult().equals(""))) {
                output.add(new UnLoadMovieActionItem(this, targetString));
            } else {
                output.add(new LoadMovieActionItem(this, urlString, targetString, sendVarsMethod));
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
                    output.add(new UnLoadMovieNumActionItem(this, new DirectValueActionItem(null, 0, (Long) (long) (int) num, new ArrayList<>())));
                } else if (urlStr != null && urlStr.startsWith(printPrefix)) {
                    output.add(new PrintNumActionItem(this, new DirectValueActionItem((Long) (long) (int) num),
                            new DirectValueActionItem(urlStr.substring(printPrefix.length()))));
                } else if (urlStr != null && urlStr.startsWith(printAsBitmapPrefix)) {
                    output.add(new PrintAsBitmapNumActionItem(this, new DirectValueActionItem((Long) (long) (int) num), new DirectValueActionItem(urlStr.substring(printAsBitmapPrefix.length()))));
                } else {
                    output.add(new LoadMovieNumActionItem(this, urlString, new DirectValueActionItem(null, 0, (Long) (long) (int) num, new ArrayList<>()), sendVarsMethod));
                }
            } else {
                if (urlStr != null && urlStr.startsWith(printPrefix)) {
                    output.add(new PrintActionItem(this, targetString, new DirectValueActionItem(urlStr.substring(printPrefix.length()))));
                } else if (urlStr != null && urlStr.startsWith(printAsBitmapPrefix)) {
                    output.add(new PrintAsBitmapActionItem(this, targetString, new DirectValueActionItem(urlStr.substring(printAsBitmapPrefix.length()))));
                } else {
                    output.add(new GetURL2ActionItem(this, urlString, targetString, sendVarsMethod));
                }
            }
        }
    }

    @Override
    public int getStackPopCount(BaseLocalData localData, TranslateStack stack) {
        return 2;
    }
}
