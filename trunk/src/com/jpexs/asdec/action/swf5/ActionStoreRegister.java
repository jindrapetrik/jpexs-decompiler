/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.swf4.RegisterNumber;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionStoreRegister extends Action {
    public int registerNumber;

    public ActionStoreRegister(SWFInputStream sis) throws IOException {
        super(0x87, 1);
        registerNumber = sis.readUI8();
    }

    public ActionStoreRegister(FlasmLexer lexer) throws IOException, ParseException {
        super(0x87, 1);
        registerNumber = (int) lexLong(lexer);
    }

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
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem item = stack.peek();
        RegisterNumber rn=new RegisterNumber(registerNumber);
        if(regNames.containsKey(registerNumber)){
            rn.name=regNames.get(registerNumber);
        }
        output.add(new StoreRegisterTreeItem(this, rn, item));
    }
}
