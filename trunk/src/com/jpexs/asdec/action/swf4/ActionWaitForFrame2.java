package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.WaitForFrame2TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionWaitForFrame2 extends Action {
    public int skipCount;

    public ActionWaitForFrame2(SWFInputStream sis) throws IOException {
        super(0x8D, 1);
        skipCount = sis.readUI8();
    }

    public ActionWaitForFrame2(FlasmLexer lexer) throws IOException, ParseException {
        super(0x8D, 1);
        skipCount = (int) lexLong(lexer);
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI8(skipCount);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        return "WaitForFrame2 " + skipCount;
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem frame = stack.pop();
        output.add(new WaitForFrame2TreeItem(this, frame, skipCount));
    }
}