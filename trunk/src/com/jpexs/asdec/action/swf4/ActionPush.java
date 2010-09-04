package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.parser.ParsedSymbol;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.DirectValueTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionPush extends Action {
    public List<Object> values;

    public List<String> constantPool;


    public ActionPush(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x96, actionLength);
        int type;
        values = new ArrayList<Object>();
        sis = new SWFInputStream(new ByteArrayInputStream(sis.readBytes(actionLength)), version);
        while ((type = sis.readUI8()) > -1)
            switch (type) {
                case 0:
                    values.add(sis.readString());
                    break;
                case 1:
                    values.add(sis.readFLOAT());
                    break;
                case 2:
                    values.add(new Null());
                    break;
                case 3:
                    values.add(new Undefined());
                    break;
                case 4:
                    values.add(new RegisterNumber(sis.readUI8()));
                    break;
                case 5:
                    values.add(new Boolean(sis.readUI8() == 1));
                    break;
                case 6:
                    values.add(sis.readDOUBLE());
                    break;
                case 7:
                    values.add((Long) sis.readUI32());
                    break;
                case 8:
                    values.add(new ConstantIndex(sis.readUI8()));
                    break;
                case 9:
                    values.add(new ConstantIndex(sis.readUI16()));
                    break;
            }
    }

    public byte[] getBytes(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(baos, version);
        try {
            for (Object o : values) {
                if (o instanceof String) {
                    sos.writeUI8(0);
                    sos.writeString((String) o);
                }
                if (o instanceof Float) {
                    sos.writeUI8(1);
                    sos.writeFLOAT((Float) o);
                }
                if (o instanceof Null) {
                    sos.writeUI8(2);
                }
                if (o instanceof Undefined) {
                    sos.writeUI8(3);
                }
                if (o instanceof RegisterNumber) {
                    sos.writeUI8(4);
                    sos.writeUI8(((RegisterNumber) o).number);
                }
                if (o instanceof Boolean) {
                    sos.writeUI8(5);
                    sos.writeUI8((Boolean) o ? 1 : 0);
                }
                if (o instanceof Double) {
                    sos.writeUI8(6);
                    sos.writeDOUBLE((Double) o);
                }
                if (o instanceof Long) {
                    sos.writeUI8(7);
                    sos.writeUI32((Long) o);
                }
                if (o instanceof ConstantIndex) {
                    int cIndex = ((ConstantIndex) o).index;
                    if (cIndex < 256) {
                        sos.writeUI8(8);
                        sos.writeUI8(cIndex);
                    } else {
                        sos.writeUI8(9);
                        sos.writeUI16(cIndex);
                    }
                }
            }
            sos.close();
        } catch (IOException e) {

        }
        return surroundWithAction(baos.toByteArray(), version);
    }

    public ActionPush(FlasmLexer lexer, List<String> constantPool) throws IOException, ParseException {
        super(0x96, 0);
        this.constantPool = constantPool;
        values = new ArrayList<Object>();
        int count = 0;
        loop:
        while (true) {
            ParsedSymbol symb = lexer.yylex();
            switch (symb.type) {
                case ParsedSymbol.TYPE_STRING:
                    count++;
                    if (constantPool.contains(symb.value)) {
                        values.add(new ConstantIndex(constantPool.indexOf(symb.value), constantPool));
                    } else {
                        values.add(symb.value);
                    }
                    break;
                case ParsedSymbol.TYPE_FLOAT:
                case ParsedSymbol.TYPE_NULL:
                case ParsedSymbol.TYPE_UNDEFINED:
                case ParsedSymbol.TYPE_REGISTER:
                case ParsedSymbol.TYPE_BOOLEAN:
                case ParsedSymbol.TYPE_INTEGER:
                case ParsedSymbol.TYPE_CONSTANT:
                    count++;
                    values.add(symb.value);
                    break;
                case ParsedSymbol.TYPE_EOL:
                case ParsedSymbol.TYPE_EOF:
                    if (count == 0) {
                        throw new ParseException("Arguments expected", lexer.yyline());
                    } else {
                        break loop;
                    }
                default:
                    throw new ParseException("Arguments expected", lexer.yyline());


            }
        }
    }


    @Override
    public String toString() {
        String ret = "Push ";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) ret += " ";
            if (values.get(i) instanceof ConstantIndex) {
                ((ConstantIndex) values.get(i)).constantPool = constantPool;
                ret += ((ConstantIndex) values.get(i)).toString();
            } else if (values.get(i) instanceof String) {
                ret += "\"" + Helper.escapeString((String) values.get(i)) + "\"";
            } else if (values.get(i) instanceof Long) {
                long l = (Long) values.get(i);
                l = l & 0xffffffffL;
                ret += l;
            } else {
                ret += values.get(i).toString();
            }
        }
        return ret;
    }

    @Override
    public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output) {
        for (Object o : values) {
            if (o instanceof ConstantIndex) {
                o = constants.constants.get(((ConstantIndex) o).index);
            }
            stack.push(new DirectValueTreeItem(this, o, constants));
        }
    }


}
