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
package com.jpexs.decompiler.flash.abc.avm2.parser;

import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ASM3Parser {

    private static class OffsetItem {

        public String label = "";
        public long insPosition;
        public int insOperandIndex;

        public OffsetItem(String label, long insOffset, int insOperandIndex) {
            this.label = label;
            this.insPosition = insOffset;
            this.insOperandIndex = insOperandIndex;
        }
    }

    private static class CaseOffsetItem extends OffsetItem {

        public CaseOffsetItem(String label, long insOffset, int insOperandIndex) {
            super(label, insOffset, insOperandIndex);
        }
    }

    private static class LabelItem {

        public String label = "";
        public int offset;

        public LabelItem(String label, int offset) {
            this.label = label;
            this.offset = offset;
        }
    }

    public static AVM2Code parse(InputStream is, ConstantPool constants, MethodBody body) throws IOException, ParseException {
        return parse(is, constants, null, body);
    }

    private static int checkMultinameIndex(ConstantPool constants, int index, int line) throws ParseException {
        if ((index < 0) || (index >= constants.constant_multiname.length)) {
            throw new ParseException("Invalid multiname index", line);
        }
        return index;
    }

    public static AVM2Code parse(InputStream is, ConstantPool constants, MissingSymbolHandler missingHandler, MethodBody body) throws IOException, ParseException {
        AVM2Code code = new AVM2Code();

        List<OffsetItem> offsetItems = new ArrayList<>();
        List<LabelItem> labelItems = new ArrayList<>();
        List<ABCException> exceptions = new ArrayList<>();
        List<Integer> exceptionIndices = new ArrayList<>();
        int offset = 0;

        Flasm3Lexer lexer = new Flasm3Lexer(new InputStreamReader(is, "UTF-8"));

        ParsedSymbol symb;
        AVM2Instruction lastIns = null;
        do {
            symb = lexer.yylex();
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_START) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new ParseException("Undefinex exception index", lexer.yyline());
                }
                exceptions.get(listIndex).start = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_END) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new ParseException("Undefinex exception index", lexer.yyline());
                }
                exceptions.get(listIndex).end = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_TARGET) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new ParseException("Undefinex exception index", lexer.yyline());
                }
                exceptions.get(listIndex).target = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EOF) {
                break;
            }
            if (symb.type == ParsedSymbol.TYPE_COMMENT) {
                if (lastIns != null) {
                    lastIns.comment = (String) symb.value;
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                if (((String) symb.value).toLowerCase(Locale.ENGLISH).equals("exception")) {
                    ParsedSymbol exIndex = lexer.yylex();
                    if (exIndex.type != ParsedSymbol.TYPE_INTEGER) {
                        throw new ParseException("Index expected", lexer.yyline());
                    }
                    ParsedSymbol exName = lexer.yylex();
                    if (exName.type != ParsedSymbol.TYPE_MULTINAME) {
                        throw new ParseException("Multiname expected", lexer.yyline());
                    }
                    ParsedSymbol exType = lexer.yylex();
                    if (exType.type != ParsedSymbol.TYPE_MULTINAME) {
                        throw new ParseException("Multiname expected", lexer.yyline());
                    }
                    ABCException ex = new ABCException();

                    ex.name_index = checkMultinameIndex(constants, (int) (long) (Long) exName.value, lexer.yyline());
                    ex.type_index = checkMultinameIndex(constants, (int) (long) (Long) exType.value, lexer.yyline());
                    exceptions.add(ex);
                    exceptionIndices.add((int) (long) (Long) exIndex.value);
                    continue;
                }
                boolean insFound = false;
                for (InstructionDefinition def : AVM2Code.instructionSet) {
                    if (def.instructionName.equals((String) symb.value)) {
                        insFound = true;
                        List<Integer> operandsList = new ArrayList<>();

                        for (int i = 0; i < def.operands.length; i++) {
                            ParsedSymbol parsedOperand = lexer.yylex();
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_MULTINAME) {
                                        operandsList.add(checkMultinameIndex(constants, (int) (long) (Long) parsedOperand.value, lexer.yyline()));
                                    } else {
                                        throw new ParseException("Multiname expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_STRING_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_STRING) {
                                        int sid = constants.getStringId((String) parsedOperand.value);
                                        if (sid == 0) {
                                            if ((missingHandler != null) && (missingHandler.missingString((String) parsedOperand.value))) {
                                                sid = constants.addString((String) parsedOperand.value);
                                            } else {
                                                throw new ParseException("Unknown String", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(sid);
                                    } else {
                                        throw new ParseException("String expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_INT_INDEX:

                                    if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER) {
                                        long intVal = (Long) parsedOperand.value;
                                        int iid = constants.getIntId(intVal);
                                        if (iid == 0) {
                                            if ((missingHandler != null) && (missingHandler.missingInt(intVal))) {
                                                iid = constants.addInt(intVal);
                                            } else {
                                                throw new ParseException("Unknown int", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(iid);
                                    } else {
                                        throw new ParseException("Integer expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_UINT_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER) {
                                        long intVal = (Long) parsedOperand.value;
                                        int iid = constants.getUIntId(intVal);
                                        if (iid == 0) {
                                            if ((missingHandler != null) && (missingHandler.missingUInt(intVal))) {
                                                iid = constants.addUInt(intVal);
                                            } else {
                                                throw new ParseException("Unknown uint", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(iid);
                                    } else {
                                        throw new ParseException("Integer expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_DOUBLE_INDEX:
                                    if ((parsedOperand.type == ParsedSymbol.TYPE_INTEGER) || (parsedOperand.type == ParsedSymbol.TYPE_FLOAT)) {

                                        double doubleVal = 0;
                                        if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER) {
                                            doubleVal = (Long) parsedOperand.value;
                                        }
                                        if (parsedOperand.type == ParsedSymbol.TYPE_FLOAT) {
                                            doubleVal = (Double) parsedOperand.value;
                                        }
                                        int did = constants.getDoubleId(doubleVal);
                                        if (did == 0) {
                                            if ((missingHandler != null) && (missingHandler.missingDouble(doubleVal))) {
                                                did = constants.addDouble(doubleVal);
                                            } else {
                                                throw new ParseException("Unknown double", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(did);
                                    } else {
                                        throw new ParseException("Float value expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_OFFSET:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                        offsetItems.add(new OffsetItem((String) parsedOperand.value, code.code.size(), i));
                                        operandsList.add(0);
                                    } else {
                                        throw new ParseException("Offset expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_CASE_BASEOFFSET:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                        offsetItems.add(new CaseOffsetItem((String) parsedOperand.value, code.code.size(), i));
                                        operandsList.add(0);
                                    } else {
                                        throw new ParseException("Offset expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.OPT_CASE_OFFSETS:

                                    if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER) {
                                        int patCount = (int) (long) (Long) parsedOperand.value;
                                        operandsList.add(patCount);

                                        for (int c = 0; c <= patCount; c++) {
                                            parsedOperand = lexer.yylex();
                                            if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                                offsetItems.add(new CaseOffsetItem((String) parsedOperand.value, code.code.size(), i + (c + 1)));
                                                operandsList.add(0);
                                            } else {
                                                throw new ParseException("Offset expected", lexer.yyline());
                                            }
                                        }
                                    } else {
                                        throw new ParseException("Case count expected", lexer.yyline());
                                    }
                                    break;
                                default:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER) {
                                        operandsList.add((int) (long) (Long) parsedOperand.value);
                                    } else {
                                        throw new ParseException("Integer expected", lexer.yyline());
                                    }
                            }
                        }

                        int[] operands = new int[operandsList.size()];
                        for (int i = 0; i < operandsList.size(); i++) {
                            operands[i] = operandsList.get(i);
                        }
                        lastIns = new AVM2Instruction(offset, def, operands, new byte[0]);
                        code.code.add(lastIns);
                        offset += lastIns.getBytes().length;
                        break;
                    }
                }
                if (symb.value.toString().toLowerCase().equals("ffdec_deobfuscatepop")) {
                    lastIns = new AVM2Instruction(offset, new DeobfuscatePopIns(), new int[0], new byte[0]);
                    code.code.add(lastIns);
                    offset += lastIns.getBytes().length;
                    insFound = true;
                }
                if (!insFound) {
                    throw new ParseException("Invalid instruction name:" + (String) symb.value, lexer.yyline());
                }
            } else if (symb.type == ParsedSymbol.TYPE_LABEL) {
                labelItems.add(new LabelItem((String) symb.value, offset));

            } else {
                throw new ParseException("Unexpected symbol", lexer.yyline());
            }
        } while (symb.type != ParsedSymbol.TYPE_EOF);

        code.compact();

        for (OffsetItem oi : offsetItems) {
            for (LabelItem li : labelItems) {
                if (oi.label.equals(li.label)) {
                    AVM2Instruction ins = code.code.get((int) oi.insPosition);
                    int relOffset;
                    if (oi instanceof CaseOffsetItem) {
                        relOffset = li.offset - (int) ins.offset;
                    } else {
                        relOffset = li.offset - ((int) ins.offset + ins.getBytes().length);
                    }
                    ins.operands[oi.insOperandIndex] = relOffset;
                }
            }
        }
        body.exceptions = new ABCException[exceptions.size()];
        for (int e = 0; e < exceptions.size(); e++) {
            body.exceptions[e] = exceptions.get(e);
        }
        return code;
    }
}
