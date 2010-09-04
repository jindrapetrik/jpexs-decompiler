package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetURL2TreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGetURL2 extends Action {
    public int sendVarsMethod;
    public static final int GET = 1;
    public static final int POST = 2;
    public boolean loadTargetFlag;
    public boolean loadVariablesFlag;

    public ActionGetURL2(SWFInputStream sis) throws IOException {
        super(0x9A, 1);
        sendVarsMethod = (int) sis.readUB(2);
        sis.readUB(4); //reserved
        loadTargetFlag = sis.readUB(1) == 1;
        loadVariablesFlag = sis.readUB(1) == 1;
    }

    @Override
    public String toString() {
        return "GetURL2 " + sendVarsMethod + " " + loadTargetFlag + " " + loadVariablesFlag;
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUB(2, sendVarsMethod);
            sos.writeUB(4, 0);
            sos.writeUB(1, loadTargetFlag ? 1 : 0);
            sos.writeUB(1, loadVariablesFlag ? 1 : 0);
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionGetURL2(FlasmLexer lexer) throws IOException, ParseException {
        super(0x9A, 0);
        sendVarsMethod = (int) lexLong(lexer);
        loadTargetFlag = lexBoolean(lexer);
        loadVariablesFlag = lexBoolean(lexer);
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        TreeItem targetString = stack.pop();
        TreeItem urlString = stack.pop();
        output.add(new GetURL2TreeItem(this, urlString, targetString, sendVarsMethod, loadTargetFlag, loadTargetFlag));
    }
}
