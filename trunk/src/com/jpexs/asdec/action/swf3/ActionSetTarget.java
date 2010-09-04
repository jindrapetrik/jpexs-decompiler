package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.SetTargetTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionSetTarget extends Action {
    public String targetName;

    public ActionSetTarget(int actionLength, SWFInputStream sis) throws IOException {
        super(0x8B, actionLength);
        targetName = sis.readString();
    }

    @Override
    public String toString() {
        return "SetTarget \"" + Helper.escapeString(targetName) + "\"";
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeString(targetName);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionSetTarget(FlasmLexer lexer) throws IOException, ParseException {
        super(0x8B, 0);
        targetName = lexString(lexer);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new SetTargetTreeItem(this, targetName));
    }
}
