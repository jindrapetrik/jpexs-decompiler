package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GotoLabelTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGoToLabel extends Action {
    public String label;

    public ActionGoToLabel(int actionLength, SWFInputStream sis) throws IOException {
        super(0x8C, actionLength);
        label = sis.readString();
    }

    @Override
    public String toString() {
        return "GoToLabel \"" + Helper.escapeString(label) + "\"";
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(label);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionGoToLabel(FlasmLexer lexer) throws IOException, ParseException {
        super(0x8C, 0);
        label = lexString(lexer);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        output.add(new GotoLabelTreeItem(this, label));
    }
}
