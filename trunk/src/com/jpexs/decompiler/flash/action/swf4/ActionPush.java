/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionPush extends Action {

    public List<Object> values;
    public List<Object> replacement;
    public List<String> constantPool;
    public List<Integer> ignoredParts = new ArrayList<Integer>();

    public ActionPush(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x96, actionLength);
        int type;
        values = new ArrayList<Object>();
        sis = new SWFInputStream(new ByteArrayInputStream(sis.readBytes(actionLength)), version);
        while ((type = sis.readUI8()) > -1) {
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
                    int b = sis.readUI8();
                    if (b == 0) {
                        values.add((Boolean) false);
                    } else {
                        values.add((Boolean) true);
                    }

                    break;
                case 6:
                    values.add(sis.readDOUBLE());
                    break;
                case 7:
                    long el = sis.readSI32();
                    values.add((Long) el);
                    break;
                case 8:
                    values.add(new ConstantIndex(sis.readUI8()));
                    break;
                case 9:
                    values.add(new ConstantIndex(sis.readUI16()));
                    break;
            }
        }
    }

    @Override
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
                    sos.writeSI32((Long) o);
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

    public ActionPush(Object... values) throws IOException, ParseException {
        super(0x96, 0);
        this.values = new ArrayList<Object>();
        for (Object o : values) {
            this.values.add(o);
        }
    }

    public ActionPush(FlasmLexer lexer, List<String> constantPool) throws IOException, ParseException {
        super(0x96, 0);
        this.constantPool = constantPool;
        values = new ArrayList<Object>();
        int count = 0;
        loop:
        while (true) {
            ASMParsedSymbol symb = lexer.yylex();
            switch (symb.type) {
                case ASMParsedSymbol.TYPE_STRING:
                    count++;
                    if (constantPool.contains((String) symb.value)) {
                        values.add(new ConstantIndex(constantPool.indexOf(symb.value), constantPool));
                    } else {
                        values.add(symb.value);
                    }
                    break;
                case ASMParsedSymbol.TYPE_FLOAT:
                case ASMParsedSymbol.TYPE_NULL:
                case ASMParsedSymbol.TYPE_UNDEFINED:
                case ASMParsedSymbol.TYPE_REGISTER:
                case ASMParsedSymbol.TYPE_BOOLEAN:
                case ASMParsedSymbol.TYPE_INTEGER:
                case ASMParsedSymbol.TYPE_CONSTANT:
                    count++;
                    values.add(symb.value);
                    break;
                case ASMParsedSymbol.TYPE_EOL:
                case ASMParsedSymbol.TYPE_EOF:
                    if (count == 0) {
                        throw new ParseException("Arguments expected", lexer.yyline());
                    } else {
                        break loop;
                    }
                case ASMParsedSymbol.TYPE_COMMENT:
                    break;
                default:
                    throw new ParseException("Arguments expected, " + symb.type + " " + symb.value + " found", lexer.yyline());


            }
        }
    }

    @Override
    public String getASMSourceReplaced(List<GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        if (replacement == null || replacement.size() < values.size()) {
            return toString();
        }
        List<Object> oldVal = values;
        values = replacement;
        String ts = toString();
        values = oldVal;
        return ts;
    }

    public String paramsToStringReplaced(List<GraphSourceItem> container, List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
        if (replacement == null || replacement.size() < values.size()) {
            return paramsToString();
        }
        List<Object> oldVal = values;
        values = replacement;
        String ts = paramsToString();
        values = oldVal;
        return ts;
    }

    public String toStringNoQ(int i) {
        String ret = "";
        if (values.get(i) instanceof ConstantIndex) {
            ((ConstantIndex) values.get(i)).constantPool = constantPool;
            ret += ((ConstantIndex) values.get(i)).toStringNoQ();
        } else if (values.get(i) instanceof String) {
            ret += (String) values.get(i);
        } else if (values.get(i) instanceof RegisterNumber) {
            ret += ((RegisterNumber) values.get(i)).toStringNoName();
        } else {
            ret += values.get(i).toString();
        }
        return ret;
    }

    public String toString(int i) {
        String ret = "";
        if (values.get(i) instanceof ConstantIndex) {
            ((ConstantIndex) values.get(i)).constantPool = constantPool;
            ret += ((ConstantIndex) values.get(i)).toString();
        } else if (values.get(i) instanceof String) {
            ret += "\"" + Helper.escapeString((String) values.get(i)) + "\"";
        } else if (values.get(i) instanceof RegisterNumber) {
            ret += ((RegisterNumber) values.get(i)).toStringNoName();
        } else {
            ret += values.get(i).toString();
        }
        return ret;
    }

    public String paramsToString() {
        String ret = "";
        int pos = 0;
        for (int i = 0; i < values.size(); i++) {
            if (ignoredParts.contains(i)) {
                continue;
            }
            if (pos > 0) {
                ret += " ";
            }
            pos++;
            ret += toString(i);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "Push " + paramsToString();
    }

    @Override
    public void translate(Stack<GraphTargetItem> stack, List<GraphTargetItem> output, java.util.HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        int pos = 0;
        for (Object o : values) {
            if (ignoredParts.contains(pos)) {
                pos++;
                continue;
            }
            if (o instanceof ConstantIndex) {
                if ((constantPool == null) || (((ConstantIndex) o).index >= constantPool.size())) {
                    o = "CONSTANT" + ((ConstantIndex) o).index;
                } else {
                    o = constantPool.get(((ConstantIndex) o).index);
                }
            }
            /*if (o instanceof RegisterNumber) {
             if (regNames.containsKey(((RegisterNumber) o).number)) {
             ((RegisterNumber) o).name = regNames.get(((RegisterNumber) o).number);
             } else if (output.size() >= 2) { //chained assignments:, ignore for class prototype assignment
             GraphTargetItem last = output.get(output.size() - 1);
             GraphTargetItem prev = output.get(output.size() - 2);
             if (last instanceof SetTypeTreeItem) {
             if (prev instanceof StoreRegisterTreeItem) {
             StoreRegisterTreeItem str = (StoreRegisterTreeItem) prev;
             if (str.register.number == ((RegisterNumber) o).number) {
             SetTypeTreeItem stt = (SetTypeTreeItem) last;
             stt.setTempRegister(((RegisterNumber) o).number);
             if ((stt.getValue() instanceof IncrementTreeItem) && (((IncrementTreeItem) stt.getValue()).object.equals(stt.getObject()))) {
             stack.push(new PreIncrementTreeItem(this, stt.getObject()));
             } else if ((stt.getValue() instanceof DecrementTreeItem) && (((DecrementTreeItem) stt.getValue()).object.equals(stt.getObject()))) {
             stack.push(new PreDecrementTreeItem(this, stt.getObject()));
             } else {
             //stack.push(last);
             continue;
             }
             output.remove(output.size() - 1);
             output.remove(output.size() - 1);
             pos++;
             continue;
             }
             }
             }
             }
             }*/
            DirectValueTreeItem dvt = new DirectValueTreeItem(this, pos, o, constantPool);
            stack.push(dvt);
            if (o instanceof RegisterNumber) {
                dvt.computedRegValue = variables.get("__register" + ((RegisterNumber) o).number);
                if (regNames.containsKey(((RegisterNumber) o).number)) {
                    ((RegisterNumber) o).name = regNames.get(((RegisterNumber) o).number);
                }
            }
            pos++;
        }
    }
}
