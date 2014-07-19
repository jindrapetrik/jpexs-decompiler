/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.GotoFrame2ActionItem;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionGotoFrame2 extends Action {

    boolean sceneBiasFlag;
    boolean playFlag;
    public int sceneBias;
    int reserved;

    public ActionGotoFrame2(boolean playFlag, boolean sceneBiasFlag, int sceneBias) {
        super(0x9F, 0);
        this.sceneBiasFlag = sceneBiasFlag;
        this.playFlag = playFlag;
        this.sceneBias = sceneBias;
    }

    public ActionGotoFrame2(int actionLength, SWFInputStream sis) throws IOException {
        super(0x9F, actionLength);
        reserved = (int) sis.readUB(6, "reserved");
        sceneBiasFlag = sis.readUB(1, "sceneBiasFlag") == 1;
        playFlag = sis.readUB(1, "playFlag") == 1;
        if (sceneBiasFlag) {
            sceneBias = sis.readUI16("sceneBias");
        }
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUB(6, reserved);
            sos.writeUB(1, sceneBiasFlag ? 1 : 0);
            sos.writeUB(1, playFlag ? 1 : 0);
            if (sceneBiasFlag) {
                sos.writeUI16(sceneBias);
            }
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        return "GotoFrame2 " + sceneBiasFlag + " " + playFlag + " " + (sceneBiasFlag ? " " + sceneBias : "");
    }

    public ActionGotoFrame2(FlasmLexer lexer) throws IOException, ParseException {
        super(0x9F, -1);
        sceneBiasFlag = lexBoolean(lexer);
        playFlag = lexBoolean(lexer);
        if (sceneBiasFlag) {
            sceneBias = (int) lexLong(lexer);
        }
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        GraphTargetItem frame = stack.pop();
        output.add(new GotoFrame2ActionItem(this, frame, sceneBiasFlag, playFlag, sceneBias));
    }
}
