/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.swf3;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.FSCommandTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetURLTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.LoadMovieNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.UnLoadMovieNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.UnLoadMovieTreeItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionGetURL extends Action {

    public String urlString;
    public String targetString;

    public ActionGetURL(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x83, actionLength);
        //byte data[] = sis.readBytes(actionLength);
        //sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        urlString = sis.readString();
        targetString = sis.readString();
    }

    public ActionGetURL(FlasmLexer lexer) throws IOException, ParseException {
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
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        return "GetUrl \"" + Helper.escapeString(urlString) + "\" \"" + Helper.escapeString(targetString) + "\"";
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        String fsCommandPrefix = "FSCommand:";
        if (urlString.startsWith(fsCommandPrefix) && targetString.equals("")) {
            String command = urlString.substring(fsCommandPrefix.length());
            output.add(new FSCommandTreeItem(this, command));
            return;
        }
        String levelPrefix = "_level";
        if (targetString.startsWith(levelPrefix)) {
            try {
                int num = Integer.valueOf(targetString.substring(levelPrefix.length()));
                if (urlString.equals("")) {
                    output.add(new UnLoadMovieNumTreeItem(this, num));
                } else {
                    DirectValueTreeItem urlStringDi = new DirectValueTreeItem(null, 0, urlString, new ArrayList<String>());
                    output.add(new LoadMovieNumTreeItem(this, urlStringDi, num, 1/*GET*/));
                }
                return;
            } catch (NumberFormatException nfe) {
            }

        }

        if (urlString.equals("")) {
            DirectValueTreeItem targetStringDi = new DirectValueTreeItem(null, 0, targetString, new ArrayList<String>());
            output.add(new UnLoadMovieTreeItem(this, targetStringDi));
        } else {
            output.add(new GetURLTreeItem(this, urlString, targetString));
        }

    }
}
