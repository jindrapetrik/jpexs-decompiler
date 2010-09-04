package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetURLTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGetURL extends Action {
    public String urlString;
    public String targetString;

    public ActionGetURL(int actionLength, SWFInputStream sis) throws IOException {
        super(0x83, actionLength);
        urlString = sis.readString();
        targetString = sis.readString();
    }

    public ActionGetURL(FlasmLexer lexer) throws IOException, ParseException {
        super(0x83, 0);
        urlString = lexString(lexer);
        targetString = lexString(lexer);
    }

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
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        output.add(new GetURLTreeItem(this, urlString, targetString));
    }
}
