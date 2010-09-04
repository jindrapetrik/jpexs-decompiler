package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.WaitForFrameTreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionWaitForFrame extends Action {
    public int frame;
    public int skipCount;

    public ActionWaitForFrame(SWFInputStream sis) throws IOException {
        super(0x8A, 3);
        frame = sis.readUI16();
        skipCount = sis.readUI8();
    }

    @Override
    public String toString() {
        return "WaitForFrame " + frame + " " + skipCount;
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI16(frame);
            sos.writeUI8(skipCount);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionWaitForFrame(FlasmLexer lexer) throws IOException, ParseException {
        super(0x8A, 0);
        frame = (int) lexLong(lexer);
        skipCount = (int) lexLong(lexer);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new WaitForFrameTreeItem(this, frame, skipCount));
    }
}
