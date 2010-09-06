package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GotoFrameTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGotoFrame extends Action {
    public int frame;

    public ActionGotoFrame(SWFInputStream sis) throws IOException {
        super(0x81, 2);
        frame = sis.readUI16();
    }

    @Override
    public String toString() {
        return "GotoFrame " + frame;
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI16(frame);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionGotoFrame(FlasmLexer lexer) throws IOException, ParseException {
        super(0x81, 0);
        frame = (int) lexLong(lexer);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        output.add(new GotoFrameTreeItem(this, frame));
    }
}
