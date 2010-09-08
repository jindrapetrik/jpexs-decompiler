/*
 *  Copyright (C) 2010 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.parser;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


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

    public static AVM2Code parse(InputStream is, ConstantPool constants) throws IOException, ParseException {
        return parse(is, constants, null);
    }

    public static AVM2Code parse(InputStream is, ConstantPool constants, MissingSymbolHandler missingHandler) throws IOException, ParseException {
        AVM2Code code = new AVM2Code();

        List<OffsetItem> offsetItems = new ArrayList<OffsetItem>();
        List<LabelItem> labelItems = new ArrayList<LabelItem>();
        int offset = 0;

        Flasm3Lexer lexer = new Flasm3Lexer(is);

        ParsedSymbol symb;
        AVM2Instruction lastIns = null;
        do {
            symb = lexer.yylex();
            if (symb.type == ParsedSymbol.TYPE_EOF) break;
            if (symb.type == ParsedSymbol.TYPE_COMMENT) {
                if (lastIns != null) {
                    lastIns.comment = (String) symb.value;
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                boolean insFound = false;
                for (InstructionDefinition def : AVM2Code.instructionSet) {
                    if (def.instructionName.equals((String) symb.value)) {
                        insFound = true;
                        List<Integer> operandsList = new ArrayList<Integer>();

                        for (int i = 0; i < def.operands.length; i++) {
                            ParsedSymbol parsedOperand = lexer.yylex();
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_MULTINAME) {
                                        operandsList.add((int) (long) (Long) parsedOperand.value);
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
                                        if (parsedOperand.type == ParsedSymbol.TYPE_INTEGER)
                                            doubleVal = (Long) parsedOperand.value;
                                        if (parsedOperand.type == ParsedSymbol.TYPE_FLOAT)
                                            doubleVal = (Double) parsedOperand.value;
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

                        int operands[] = new int[operandsList.size()];
                        for (int i = 0; i < operandsList.size(); i++) {
                            operands[i] = operandsList.get(i);
                        }
                        lastIns = new AVM2Instruction(offset, def, operands, new byte[0]);
                        code.code.add(lastIns);
                        offset += lastIns.getBytes().length;
                        break;
                    }
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

        for (OffsetItem oi : offsetItems) {
            for (LabelItem li : labelItems) {
                if (oi.label.equals(li.label)) {
                    AVM2Instruction ins = code.code.get((int) oi.insPosition);
                    int relOffset = 0;
                    if (oi instanceof CaseOffsetItem) {
                        relOffset = li.offset - (int) ins.offset;
                    } else {
                        relOffset = li.offset - ((int) ins.offset + ins.getBytes().length);
                    }
                    ins.operands[oi.insOperandIndex] = relOffset;
                }
            }
        }

        /* BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = "";
        Pattern patInsName = Pattern.compile("^([a-z0-9_]+) ");
        Pattern patLabelName = Pattern.compile("^([a-zA-Z_0-9]+): ");
        Pattern patInt = Pattern.compile("^([+-]?[0-9]+) ");
        Pattern patDouble = Pattern.compile("^([+-]?[0-9e.]+) ");
        Pattern patMultiname = Pattern.compile("^m\\[([0-9]+)\\]\"[^\"]*\" ");
        Pattern patString = Pattern.compile("\"([^\"]*)\" ");
        Pattern patofs = Pattern.compile("^([a-zA-Z_0-9]+) ");


        long line = 0;
        
        while ((s = br.readLine()) != null) {
            line++;
            s += " ";
            Matcher m = patInsName.matcher(s);
            if (m.find()) {
                String insName = m.group(1);
                boolean insFound = false;
                for (InstructionDefinition def : AVM2Code.instructionSet) {
                    if (def.instructionName.equals(insName)) {
                        insFound = true;
                        s = s.substring(insName.length() + 1);
                        List<Integer> operandsList = new ArrayList<Integer>();

                        for (int i = 0; i < def.operands.length; i++) {
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                    m = patMultiname.matcher(s);
                                    if (m.find()) {
                                        operandsList.add(Integer.parseInt(m.group(1)));
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid multiname", line);
                                    }
                                    break;
                                case AVM2Code.DAT_STRING_INDEX:
                                    m = patString.matcher(s);
                                    if (m.find()) {
                                        String str = m.group(1);
                                        int sid = constants.getStringId(str);
                                        if (sid == 0) {
                                            if((missingHandler!=null)&&(missingHandler.missingString(str))){
                                                sid=constants.addString(str);
                                            }else{
                                                throw new ParseException("Unknown String", line);
                                            }
                                        }
                                        operandsList.add(sid);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid String", line);
                                    }
                                    break;
                                case AVM2Code.DAT_INT_INDEX:
                                    m = patInt.matcher(s);
                                    if (m.find()) {
                                        long intVal=Integer.parseInt(m.group(1));
                                        int iid = constants.getIntId(intVal);
                                        if (iid == 0) {
                                            if((missingHandler!=null)&&(missingHandler.missingInt(intVal))){
                                                iid=constants.addInt(intVal);
                                            }else{
                                                throw new ParseException("Unknown int", line);
                                            }
                                        }
                                        operandsList.add(iid);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid int value", line);
                                    }
                                    break;
                                case AVM2Code.DAT_UINT_INDEX:
                                    m = patInt.matcher(s);
                                    if (m.find()) {
                                        long intVal=Integer.parseInt(m.group(1));
                                        int iid = constants.getUIntId(intVal);
                                        if (iid == 0) {
                                            if((missingHandler!=null)&&(missingHandler.missingUInt(intVal))){
                                                iid=constants.addUInt(intVal);
                                            }else{
                                            throw new ParseException("Unknown uint", line);
                                            }
                                        }
                                        operandsList.add(iid);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid uint value", line);
                                    }
                                    break;
                                case AVM2Code.DAT_DOUBLE_INDEX:
                                    m = patDouble.matcher(s);
                                    if (m.find()) {
                                        double doubleVal=Double.parseDouble(m.group(1));
                                        int did = constants.getDoubleId(doubleVal);
                                        if (did == 0) {
                                            if((missingHandler!=null)&&(missingHandler.missingDouble(doubleVal))){
                                                did=constants.addDouble(doubleVal);
                                            }else{
                                                throw new ParseException("Unknown double", line);
                                            }
                                        }
                                        operandsList.add(did);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid double value", line);
                                    }
                                    break;
                                case AVM2Code.DAT_OFFSET:
                                    m = patofs.matcher(s);
                                    if (m.find()) {
                                        offsetItems.add(new OffsetItem(m.group(1), code.code.size(), i));
                                        operandsList.add(0);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid offset value", line);
                                    }
                                    break;
                                case AVM2Code.DAT_CASE_BASEOFFSET:
                                    m = patofs.matcher(s);
                                    if (m.find()) {
                                        offsetItems.add(new CaseOffsetItem(m.group(1), code.code.size(), i));
                                        operandsList.add(0);
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid offset value", line);
                                    }
                                    break;
                                case AVM2Code.OPT_CASE_OFFSETS:
                                    m = patInt.matcher(s);
                                    if (m.find()) {
                                        int patCount = Integer.parseInt(m.group(1));
                                        operandsList.add(patCount);
                                        s = s.substring(m.group(0).length());
                                        m = patofs.matcher(s);
                                        int k = 1;
                                        for (int c = 0; c <= patCount; c++) {
                                            if (m.find()) {
                                                offsetItems.add(new CaseOffsetItem(m.group(1), code.code.size(), i + k));
                                                operandsList.add(0);
                                                s = s.substring(m.group(0).length());
                                                m = patofs.matcher(s);
                                                k++;
                                            } else {
                                                throw new ParseException("Invalid case count", line);
                                            }
                                        }
                                    } else {
                                        throw new ParseException("Invalid case count", line);
                                    }
                                    break;
                                default:
                                    m = patInt.matcher(s);
                                    if (m.find()) {
                                        operandsList.add(Integer.parseInt(m.group(1)));
                                        s = s.substring(m.group(0).length());
                                    } else {
                                        throw new ParseException("Invalid value", line);
                                    }
                            }
                        }

                        int operands[] = new int[operandsList.size()];
                        for (int i = 0; i < operandsList.size(); i++) {
                            operands[i] = operandsList.get(i);
                        }
                        AVM2Instruction ins = new AVM2Instruction(offset, def, operands, new byte[0]);
                        code.code.add(ins);
                        offset += ins.getBytes().length;
                        break;
                    }
                }
                if (!insFound) {
                    throw new ParseException("Invalid instruction name:" + insName, line);
                }
            } else {
                m = patLabelName.matcher(s);
                if (m.find()) {
                    labelItems.add(new LabelItem(m.group(1), offset));
                } else {
                    throw new ParseException("Invalid instruction name", line);
                }
            }
        }

        for (OffsetItem oi : offsetItems) {
            for (LabelItem li : labelItems) {
                if (oi.label.equals(li.label)) {
                    AVM2Instruction ins = code.code.get((int) oi.insPosition);
                    int relOffset = 0;
                    if (oi instanceof CaseOffsetItem) {
                        relOffset = li.offset - (int) ins.offset;
                    } else {
                        relOffset = li.offset - ((int) ins.offset + ins.getBytes().length);
                    }
                    ins.operands[oi.insOperandIndex] = relOffset;
                }
            }
        }*/
        return code;
    }
}
