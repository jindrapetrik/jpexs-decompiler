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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItemPos;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionStoreRegister extends Action {

    public int registerNumber;

    public ActionStoreRegister(int registerNumber) {
        super(0x87, 1);
        this.registerNumber = registerNumber;
    }

    public ActionStoreRegister(SWFInputStream sis) throws IOException {
        super(0x87, 1);
        registerNumber = sis.readUI8();
    }

    public ActionStoreRegister(FlasmLexer lexer) throws IOException, ParseException {
        super(0x87, 0);
        registerNumber = (int) lexLong(lexer);
    }

    @Override
    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI8(registerNumber);
            sos.close();
        } catch (IOException e) {
        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        return "StoreRegister " + registerNumber;
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        GraphTargetItem item = stack.peek();
        RegisterNumber rn = new RegisterNumber(registerNumber);
        if (regNames.containsKey(registerNumber)) {
            rn.name = regNames.get(registerNumber);
        }
        item.moreSrc.add(new GraphSourceItemPos(this, 0));
        boolean define = !variables.containsKey("__register" + registerNumber);
        variables.put("__register" + registerNumber, item);
        output.add(new StoreRegisterTreeItem(this, rn, item, define));
    }
}
