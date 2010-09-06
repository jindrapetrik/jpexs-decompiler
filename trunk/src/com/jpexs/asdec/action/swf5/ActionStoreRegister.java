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