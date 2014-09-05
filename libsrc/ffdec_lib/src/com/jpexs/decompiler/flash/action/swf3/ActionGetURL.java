/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommandActionItem;
import com.jpexs.decompiler.flash.action.model.GetURLActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActionGetURL extends Action {

    public String urlString;
    public String targetString;

    public ActionGetURL(String urlString, String targetString) {
        super(0x83, 0);
        this.urlString = urlString;
        this.targetString = targetString;
    }

    public ActionGetURL(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x83, actionLength);
        //byte[] data = sis.readBytes(actionLength);
        //sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        urlString = sis.readString("urlString");
        targetString = sis.readString("targetString");
    }

    public ActionGetURL(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x83, 0);
        urlString = lexString(lexer);
        targetString = lexString(lexer);
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(urlString);
            sos.writeString(targetString);
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        return "GetUrl \"" + Helper.escapeString(urlString) + "\" \"" + Helper.escapeString(targetString) + "\"";
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        String fsCommandPrefix = "FSCommand:";
        if (urlString.startsWith(fsCommandPrefix) && targetString.isEmpty()) {
            String command = urlString.substring(fsCommandPrefix.length());
            output.add(new FSCommandActionItem(this, new DirectValueActionItem(command)));
            return;
        }
        String levelPrefix = "_level";
        if (targetString.startsWith(levelPrefix)) {
            try {
                int num = Integer.valueOf(targetString.substring(levelPrefix.length()));
                if (urlString.isEmpty()) {
                    output.add(new UnLoadMovieNumActionItem(this, new DirectValueActionItem((Long) (long) (int) num)));
                } else {
                    DirectValueActionItem urlStringDi = new DirectValueActionItem(null, 0, urlString, new ArrayList<String>());
                    output.add(new LoadMovieNumActionItem(this, urlStringDi, new DirectValueActionItem((Long) (long) (int) num), 1/*GET*/));
                }
                return;
            } catch (NumberFormatException nfe) {
            }

        }

        if (urlString.isEmpty()) {
            DirectValueActionItem targetStringDi = new DirectValueActionItem(null, 0, targetString, new ArrayList<String>());
            output.add(new UnLoadMovieActionItem(this, targetStringDi));
        } else {
            output.add(new GetURLActionItem(this, urlString, targetString));
        }

    }
}
