/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.swf4;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.EndOfStreamException;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ActionPush extends Action {

    public List<Object> values;

    public List<Object> replacement;

    public List<String> constantPool;

    public List<Integer> ignoredParts = new ArrayList<>();

    @Override
    public boolean isIgnored() {
        return ignoredParts.size() == values.size();
    }

    @Override
    public void setIgnored(boolean ignored, int pos) {
        if (ignored) {
            if (!ignoredParts.contains(pos)) {
                ignoredParts.add(pos);
                if (ignoredParts.size() == values.size()) {
                    super.setIgnored(ignored, 0);
                }
            }
        } else {
            if (ignoredParts.contains(pos)) {
                ignoredParts.remove(pos);
                super.setIgnored(false, 0);
            }
        }

    }

    public ActionPush(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x96, actionLength);
        int type;
        values = new ArrayList<>();
        DumpInfo di = sis.dumpInfo;
        sis = sis.getLimitedStream(actionLength);
        sis.dumpInfo = di;
        try {
            while (sis.available() > 0) {
                type = sis.readUI8("type");
                switch (type) {
                    case 0:
                        values.add(sis.readString("string"));
                        break;
                    case 1:
                        values.add(sis.readFLOAT("float"));
                        break;
                    case 2:
                        values.add(new Null());
                        break;
                    case 3:
                        values.add(new Undefined());
                        break;
                    case 4:
                        values.add(new RegisterNumber(sis.readUI8("registerNumber")));
                        break;
                    case 5:
                        int b = sis.readUI8("boolean");
                        if (b == 0) {
                            values.add((Boolean) false);
                        } else {
                            values.add((Boolean) true);
                        }

                        break;
                    case 6:
                        values.add(sis.readDOUBLE("double"));
                        break;
                    case 7:
                        long el = sis.readSI32("integer");
                        values.add((Long) el);
                        break;
                    case 8:
                        values.add(new ConstantIndex(sis.readUI8("constantIndex")));
                        break;
                    case 9:
                        values.add(new ConstantIndex(sis.readUI16("constantIndex")));
                        break;
                }
            }
        } catch (EndOfStreamException ex) {
        }
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        for (Object o : values) {
            if (o instanceof String) {
                sos.writeUI8(0);
                sos.writeString((String) o);
            } else if (o instanceof Float) {
                sos.writeUI8(1);
                sos.writeFLOAT((Float) o);
            } else if (o instanceof Null) {
                sos.writeUI8(2);
            } else if (o instanceof Undefined) {
                sos.writeUI8(3);
            } else if (o instanceof RegisterNumber) {
                sos.writeUI8(4);
                sos.writeUI8(((RegisterNumber) o).number);
            } else if (o instanceof Boolean) {
                sos.writeUI8(5);
                sos.writeUI8((Boolean) o ? 1 : 0);
            } else if (o instanceof Double || o instanceof Long) {
                if (o instanceof Long) {
                    long l = (Long) o;
                    if (l < -0x80000000 || l > 0x7fffffff) {
                        o = (double) l;
                    }
                }
                if (o instanceof Double) {
                    sos.writeUI8(6);
                    sos.writeDOUBLE((Double) o);
                } else if (o instanceof Long) {
                    sos.writeUI8(7);
                    sos.writeSI32((Long) o);
                }
            } else if (o instanceof ConstantIndex) {
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
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        int res = 0;
        for (Object o : values) {
            if (o instanceof String) {
                res += Utf8Helper.getBytesLength((String) o) + 2;
            } else if (o instanceof Float) {
                res += 5;
            } else if (o instanceof Null) {
                res++;
            } else if (o instanceof Undefined) {
                res++;
            } else if (o instanceof RegisterNumber) {
                res += 2;
            } else if (o instanceof Boolean) {
                res += 2;
            } else if (o instanceof Double || o instanceof Long) {
                if (o instanceof Long) {
                    long l = (Long) o;
                    if (l < -0x80000000 || l > 0x7fffffff) {
                        o = (double) l;
                    }
                }
                if (o instanceof Double) {
                    res += 9;
                } else if (o instanceof Long) {
                    res += 5;
                }
            } else if (o instanceof ConstantIndex) {
                int cIndex = ((ConstantIndex) o).index;
                if (cIndex < 256) {
                    res += 2;
                } else {
                    res += 3;
                }
            }
        }

        return res;
    }

    public static boolean isValidValue(Object value) {
        if (value instanceof String) {
            for (char ch : ((String) value).toCharArray()) {
                if (ch == 0) {
                    return false;
                }
            }
        }
        if (value instanceof Long) {
            long l = (Long) value;
            if (l < -0x80000000 || l > 0x7fffffff) {
                return false;
            }
        }
        return true;
    }

    public ActionPush(Object value) {
        super(0x96, 0);
        this.values = new ArrayList<>();
        this.values.add(value);
        updateLength(SWF.DEFAULT_VERSION);
    }

    public ActionPush(FlasmLexer lexer, List<String> constantPool) throws IOException, ActionParseException {
        super(0x96, 0);
        this.constantPool = constantPool;
        values = new ArrayList<>();
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
                        throw new ActionParseException("Arguments expected", lexer.yyline());
                    } else {
                        break loop;
                    }
                case ASMParsedSymbol.TYPE_COMMENT:
                    break;
                default:
                    throw new ActionParseException("Arguments expected, " + symb.type + " " + symb.value + " found", lexer.yyline());
            }
        }
    }

    @Override
    public GraphTextWriter getASMSourceReplaced(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode, GraphTextWriter writer) {
        if (replacement == null || replacement.size() < values.size()) {
            return toString(writer);
        }
        List<Object> oldVal = values;
        values = replacement;
        toString(writer);
        values = oldVal;
        return writer;
    }

    public GraphTextWriter paramsToStringReplaced(List<? extends GraphSourceItem> container, Set<Long> knownAddreses, ScriptExportMode exportMode, GraphTextWriter writer) {
        if (replacement == null || replacement.size() < values.size()) {
            return paramsToString(writer);
        }
        List<Object> oldVal = values;
        values = replacement;
        paramsToString(writer);
        values = oldVal;
        return writer;
    }

    public String toStringNoQ(int i) {
        String ret;
        if (values.get(i) instanceof ConstantIndex) {
            ((ConstantIndex) values.get(i)).constantPool = constantPool;
            ret = ((ConstantIndex) values.get(i)).toStringNoQ();
        } else if (values.get(i) instanceof String) {
            ret = (String) values.get(i);
        } else if (values.get(i) instanceof RegisterNumber) {
            ret = ((RegisterNumber) values.get(i)).toStringNoName();
        } else {
            ret = values.get(i).toString();
        }
        return ret;
    }

    public String toString(int i) {
        String ret;
        if (values.get(i) instanceof ConstantIndex) {
            ((ConstantIndex) values.get(i)).constantPool = constantPool;
            ret = ((ConstantIndex) values.get(i)).toString();
        } else if (values.get(i) instanceof String) {
            ret = "\"" + Helper.escapeActionScriptString((String) values.get(i)) + "\"";
        } else if (values.get(i) instanceof RegisterNumber) {
            ret = ((RegisterNumber) values.get(i)).toStringNoName();
        } else {
            ret = values.get(i).toString();
        }
        return ret;
    }

    public GraphTextWriter paramsToString(GraphTextWriter writer) {
        int pos = 0;
        for (int i = 0; i < values.size(); i++) {
            if (ignoredParts.contains(i)) {
                continue;
            }
            if (pos > 0) {
                writer.appendNoHilight(" ");
            }
            writer.append(toString(i), getAddress() + pos + 1);
            pos++;
        }
        return writer;
    }

    @Override
    public String toString() {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        toString(writer);
        return writer.toString();
    }

    public GraphTextWriter toString(GraphTextWriter writer) {
        writer.appendNoHilight("Push ");
        paramsToString(writer);
        return writer;
    }

    @Override
    public void translate(TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int staticOperation, String path) {
        int pos = 0;
        for (Object o : values) {
            if (ignoredParts.contains(pos)) {
                pos++;
                continue;
            }
            if (o instanceof ConstantIndex) {
                if ((constantPool == null) || (((ConstantIndex) o).index >= constantPool.size())) {
                    o = "§§constant" + ((ConstantIndex) o).index;
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
             if (last instanceof SetTypeActionItem) {
             if (prev instanceof StoreRegisterActionItem) {
             StoreRegisterActionItem str = (StoreRegisterActionItem) prev;
             if (str.register.number == ((RegisterNumber) o).number) {
             SetTypeActionItem stt = (SetTypeActionItem) last;
             stt.setTempRegister(((RegisterNumber) o).number);
             if ((stt.getValue() instanceof IncrementActionItem) && (((IncrementActionItem) stt.getValue()).object.equals(stt.getObject()))) {
             stack.push(new PreIncrementActionItem(this, stt.getObject()));
             } else if ((stt.getValue() instanceof DecrementActionItem) && (((DecrementActionItem) stt.getValue()).object.equals(stt.getObject()))) {
             stack.push(new PreDecrementActionItem(this, stt.getObject()));
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
            if (o instanceof Boolean) {
                Boolean b = (Boolean) o;
                if (b) {
                    stack.push(new TrueItem(this));
                } else {
                    stack.push(new FalseItem(this));
                }
            } else {
                DirectValueActionItem dvt = new DirectValueActionItem(this, pos, o, constantPool);

                if (o instanceof RegisterNumber) {//TemporaryRegister
                    dvt.computedRegValue = variables.get("__register" + ((RegisterNumber) o).number);
                    if (regNames.containsKey(((RegisterNumber) o).number)) {
                        ((RegisterNumber) o).name = regNames.get(((RegisterNumber) o).number);
                    }
                }
                if (dvt.computedRegValue instanceof TemporaryRegister) {
                    stack.push(new TemporaryRegister(((RegisterNumber) o).number, ((TemporaryRegister) dvt.computedRegValue).value));
                } else {
                    stack.push(dvt);
                }
            }
            pos++;
        }
    }

    @Override
    public int getStackPushCount(BaseLocalData localData, TranslateStack stack) {
        return values.size();
    }
}
