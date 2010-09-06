package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.parser.ParsedSymbol;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionConstantPool extends Action {
    public List<String> constantPool = new ArrayList<String>();

    public ActionConstantPool(int actionLength, SWFInputStream sis) throws IOException {
        super(0x88, actionLength);
        int count = sis.readUI16();
        for (int i = 0; i < count; i++) {
            constantPool.add(sis.readString());
        }
    }

    public ActionConstantPool(FlasmLexer lexer) throws IOException, ParseException {
        super(0x88, 0);
        while (true) {
            ParsedSymbol symb = lexer.yylex();
            if (symb.type == ParsedSymbol.TYPE_STRING) {
                constantPool.add((String) symb.value);
            } else {
                lexer.yypushback(lexer.yylength());
                break;
            }
        }
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            sos.writeUI16(constantPool.size());
            for (String s : constantPool) {
                sos.writeString(s);
            }
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < constantPool.size(); i++) {
            if (i > 0) ret += " ";
            ret += "\"" + Helper.escapeString(constantPool.get(i)) + "\"";
        }
        return "ConstantPool " + ret;
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer,String> regNames) {
        constants.constants = constantPool;
    }
}