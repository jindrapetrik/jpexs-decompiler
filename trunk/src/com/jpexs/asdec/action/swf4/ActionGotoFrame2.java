package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GotoFrame2TreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGotoFrame2 extends Action {
    boolean sceneBiasFlag;
    boolean playFlag;
    public int sceneBias;

    public ActionGotoFrame2(int actionLength, SWFInputStream sis) throws IOException {
        super(0x9F, actionLength);
        sis.readUB(6); //reserved
        sceneBiasFlag = sis.readUB(1) == 1;
        playFlag = sis.readUB(1) == 1;
        if (sceneBiasFlag) {
            sceneBias = sis.readUI16();
        }
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUB(6, 0);
            sos.writeUB(1, sceneBiasFlag ? 1 : 0);
            sos.writeUB(1, playFlag ? 1 : 0);
            if (sceneBiasFlag) {
                sos.writeUI16(sceneBias);
            }
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }


    @Override
    public String toString() {
        return "GotoFrame2 " + sceneBiasFlag + " " + playFlag + " " + (sceneBiasFlag ? " " + sceneBias : "");
    }

    public ActionGotoFrame2(FlasmLexer lexer) throws IOException, ParseException {
        super(0x9F, 0);
        sceneBiasFlag = lexBoolean(lexer);
        playFlag = lexBoolean(lexer);
        if (sceneBiasFlag) {
            sceneBias = (int) lexLong(lexer);
        }
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        TreeItem frame = stack.pop();
        output.add(new GotoFrame2TreeItem(this, frame, sceneBiasFlag, playFlag, sceneBias));
    }
}
