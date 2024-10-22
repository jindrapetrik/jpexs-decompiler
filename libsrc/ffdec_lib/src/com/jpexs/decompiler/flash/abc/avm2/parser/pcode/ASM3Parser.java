/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.parser.pcode;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.NumberContext;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.UnknownInstruction;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.io.Reader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import macromedia.asc.util.Decimal128;

/**
 * Parses AVM2 P-code.
 *
 * @author JPEXS
 */
public class ASM3Parser {

    /**
     * Constructor.
     */
    public ASM3Parser() {
    }

    private static class OffsetItem {

        public String label = "";

        public long insPosition;

        public int insOperandIndex;

        public int line;

        public OffsetItem(String label, long insOffset, int insOperandIndex, int line) {
            this.label = label;
            this.insPosition = insOffset;
            this.insOperandIndex = insOperandIndex;
            this.line = line;
        }
    }

    private static class CaseOffsetItem extends OffsetItem {

        public CaseOffsetItem(String label, long insOffset, int insOperandIndex, int line) {
            super(label, insOffset, insOperandIndex, line);
        }
    }

    private static class LabelItem {

        public String label = "";

        public int offset;

        public LabelItem(String label, int offset) {
            this.label = label;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return label + " at address " + offset;
        }

    }

    private static int checkMultinameIndex(AVM2ConstantPool constants, int index, int line) throws AVM2ParseException {
        if ((index < 0) || (index >= constants.getMultinameCount())) {
            throw new AVM2ParseException("Invalid multiname index", line);
        }
        return index;
    }

    private static void expectEnd(Flasm3Lexer lexer) throws IOException, AVM2ParseException {
        expected(ParsedSymbol.TYPE_KEYWORD_END, "end", lexer);
        ParsedSymbol symb = lexer.lex();
        if (symb.type != ParsedSymbol.TYPE_COMMENT) {
            lexer.pushback(symb);
        }
    }

    private static void expected(int type, String expStr, Flasm3Lexer lexer) throws IOException, AVM2ParseException {
        ParsedSymbol s = lexer.lex();
        if (s.type != type) {
            throw new AVM2ParseException(expStr + " expected", lexer.yyline());
        }
    }

    private static int getInteger(ParsedSymbol s, int line) throws AVM2ParseException {
        return getInteger(s, line, false);
    }
    private static int getInteger(ParsedSymbol s, int line, boolean orNull) throws AVM2ParseException {
        
        String expected = "integer" + (orNull ? " or null" : "") + " expected";
        if (s.type != ParsedSymbol.TYPE_NUMBER) {
            throw new AVM2ParseException(expected, line);
        }
        String nval = (String) s.value;
        if (
                nval.endsWith("d") 
                || nval.endsWith("m") 
                || nval.contains("e")
                || nval.contains("E")
                || nval.contains(".")
                ) {
            throw new AVM2ParseException(expected, line);
        }
        if (nval.endsWith("i") || nval.endsWith("u")) {
            nval = nval.substring(0, nval.length() - 1);
        }
        return Integer.parseInt(nval);
    }
    
    private static long getUInteger(ParsedSymbol s, int line, boolean orNull) throws AVM2ParseException {
        
        String expected = "unsigned integer" + (orNull ? " or null" : "") + " expected";
        if (s.type != ParsedSymbol.TYPE_NUMBER) {
            throw new AVM2ParseException(expected, line);
        }
        String nval = (String) s.value;
        if (
                nval.endsWith("d") 
                || nval.endsWith("m") 
                || nval.contains("e")
                || nval.contains("E")
                || nval.contains(".")
                ) {
            throw new AVM2ParseException(expected, line);
        }
        if (nval.endsWith("i") || nval.endsWith("u")) {
            nval = nval.substring(0, nval.length() - 1);
        }
        long result = Long.parseLong(nval);
        if (result < 0) {
            throw new AVM2ParseException(expected, line);
        }
        return result;
    }
    
    
    private static float getFloat(ParsedSymbol s, int line, boolean orNull) throws AVM2ParseException {
        String expected = "float" + (orNull ? " or null" : "") + " expected";
        if (s.type != ParsedSymbol.TYPE_NUMBER) {
            throw new AVM2ParseException(expected, line);
        }
        
        String nval = (String) s.value;
        if (
                nval.endsWith("d") 
                || nval.endsWith("m") 
                ) {
            throw new AVM2ParseException(expected, line);
        }
        
        
        if (
                nval.endsWith("i")
                || nval.endsWith("u")
                || nval.endsWith("f")
                ) {
            nval = nval.substring(0, nval.length() - 1);
        }
        
        return Float.parseFloat(nval);
    }
    
    private static Decimal128 getDecimal(ParsedSymbol s, int line, boolean orNull) throws AVM2ParseException {
        String expected = "decimal" + (orNull ? " or null" : "") + " expected";
        if (s.type != ParsedSymbol.TYPE_NUMBER) {
            throw new AVM2ParseException(expected, line);
        }
        String nval = (String) s.value;
        
        if (
                nval.endsWith("i")
                || nval.endsWith("u")
                || nval.endsWith("d")
                || nval.endsWith("m")
                ) {
            nval = nval.substring(0, nval.length() - 1);
        }
        return new Decimal128(nval);
    }
    private static double getDouble(ParsedSymbol s, int line, boolean orNull) throws AVM2ParseException {
        String expected = "double" + (orNull ? " or null" : "") + " expected";
        if (s.type != ParsedSymbol.TYPE_NUMBER) {
            throw new AVM2ParseException(expected, line);
        }
        String nval = (String) s.value;
        if (nval.endsWith("m")) {
            throw new AVM2ParseException(expected, line);
        }
        if (nval.endsWith("i") || nval.endsWith("u") || nval.endsWith("d")) {
            nval = nval.substring(0, nval.length() - 1);
        }
        return Double.parseDouble(nval);
    }        
    
    private static void expected(ParsedSymbol s, int type, String expStr, int line) throws IOException, AVM2ParseException {
        if (s.type != type) {
            throw new AVM2ParseException(expStr + " expected", line);
        }
    }

    private static void parseTraitParams(ABC abc, Flasm3Lexer lexer, Trait t) throws IOException, AVM2ParseException {
        ParsedSymbol symb;

        List<Map.Entry<String, Map<String, String>>> metadata = new ArrayList<>();

        int flags = 0;
        while (true) {
            symb = lexer.lex();
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_FLAG) {
                symb = lexer.lex();
                switch (symb.type) {
                    case ParsedSymbol.TYPE_KEYWORD_FINAL:
                        flags |= Trait.ATTR_Final;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_OVERRIDE:
                        flags |= Trait.ATTR_Override;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_METADATA:
                        flags |= Trait.ATTR_Metadata;
                        break;
                    default:
                        throw new AVM2ParseException("Invalid trait flag", lexer.yyline());
                }
            } else if (symb.type == ParsedSymbol.TYPE_KEYWORD_METADATA_BLOCK) {
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_STRING, "string metadata", lexer.yyline());
                String mkey = (String) symb.value;
                symb = lexer.lex();
                Map<String, String> items = new HashMap<>();
                while (symb.type == ParsedSymbol.TYPE_KEYWORD_ITEM) {
                    symb = lexer.lex();
                    if (symb.type != ParsedSymbol.TYPE_KEYWORD_NULL && symb.type != ParsedSymbol.TYPE_STRING) {
                        throw new AVM2ParseException("string/null value expected", lexer.yyline());
                    }
                    String key = symb.type == ParsedSymbol.TYPE_KEYWORD_NULL ? null : (String) symb.value;
                    symb = lexer.lex();
                    expected(symb, ParsedSymbol.TYPE_STRING, "string value", lexer.yyline());
                    String val = (String) symb.value;
                    items.put(key, val);
                    symb = lexer.lex();
                }
                expected(symb, ParsedSymbol.TYPE_KEYWORD_END, "end", lexer.yyline());
                symb = lexer.lex();
                if (symb.type != ParsedSymbol.TYPE_COMMENT) {
                    lexer.pushback(symb);
                }
                metadata.add(new AbstractMap.SimpleEntry<>(mkey, items));
            } else {
                lexer.pushback(symb);
                break;
            }
        }

        t.kindFlags = flags;
        if ((flags & Trait.ATTR_Metadata) > 0) {
            int[] metadataArray = new int[metadata.size()];
            for (int i = 0; i < metadata.size(); i++) {
                Map.Entry<String, Map<String, String>> entry = metadata.get(i);
                int mkey = abc.constants.getStringId(entry.getKey(), true);
                Map<String, String> items = entry.getValue();
                int[] keys = new int[items.size()];
                int[] vals = new int[items.size()];

                int pos = 0;
                for (String key : items.keySet()) {
                    int ikey = abc.constants.getStringId(key, true);
                    int ival = abc.constants.getStringId(items.get(key), true);
                    keys[pos] = ikey;
                    vals[pos] = ival;
                    pos++;
                }
                MetadataInfo mi = new MetadataInfo(mkey, keys, vals);
                metadataArray[i] = abc.getMetadataId(mi, true);
            }
            t.metadata = metadataArray;
        }

    }

    /**
     * Parses a class.
     * @param abc ABC
     * @param reader Reader
     * @param constants Constant pool
     * @param tc Trait class
     * @return True if parsed successfully
     * @throws IOException On I/O error
     * @throws AVM2ParseException On parse error
     */
    public static boolean parseClass(ABC abc, Reader reader, AVM2ConstantPool constants, TraitClass tc) throws IOException, AVM2ParseException {
        Flasm3Lexer lexer = new Flasm3Lexer(reader);
        return parseClass(abc, lexer, constants, tc);
    }

    private static boolean parseClass(ABC abc, Flasm3Lexer lexer, AVM2ConstantPool constants, TraitClass tc) throws IOException, AVM2ParseException {
        expected(ParsedSymbol.TYPE_KEYWORD_TRAIT, "trait", lexer);
        expected(ParsedSymbol.TYPE_KEYWORD_CLASS, "class", lexer);
        int name_index = parseMultiName(constants, lexer);
        parseTraitParams(abc, lexer, tc);
        expected(ParsedSymbol.TYPE_KEYWORD_SLOTID, "slotid", lexer);
        ParsedSymbol symb;
        symb = lexer.lex();
        int slotid = (int) getUInteger(symb, lexer.yyline(), false);

        expected(ParsedSymbol.TYPE_KEYWORD_CLASS, "class", lexer);
        expected(ParsedSymbol.TYPE_KEYWORD_INSTANCE, "instance", lexer);

        int instance_name_index = parseMultiName(constants, lexer);
        expected(ParsedSymbol.TYPE_KEYWORD_EXTENDS, "extends", lexer);
        int super_index = parseMultiName(constants, lexer);
        symb = lexer.lex();
        List<Integer> ifacesList = new ArrayList<>();
        while (symb.type == ParsedSymbol.TYPE_KEYWORD_IMPLEMENTS) {
            ifacesList.add(parseMultiName(constants, lexer));
            symb = lexer.lex();
        }
        int[] interfaces = new int[ifacesList.size()];
        for (int i = 0; i < ifacesList.size(); i++) {
            interfaces[i] = ifacesList.get(i);
        }
        int instanceFlags = 0;
        while (symb.type == ParsedSymbol.TYPE_KEYWORD_FLAG) {
            symb = lexer.lex();
            switch (symb.type) {
                case ParsedSymbol.TYPE_KEYWORD_SEALED:
                    instanceFlags |= InstanceInfo.CLASS_SEALED;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_FINAL:
                    instanceFlags |= InstanceInfo.CLASS_FINAL;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_INTERFACE:
                    instanceFlags |= InstanceInfo.CLASS_INTERFACE;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_PROTECTEDNS:
                    instanceFlags |= InstanceInfo.CLASS_PROTECTEDNS;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_NON_NULLABLE:
                    instanceFlags |= InstanceInfo.CLASS_NON_NULLABLE;
                    break;
                default:
                    throw new AVM2ParseException("SEALED,FINAL,INTERFACE,PROTECTEDNS or NON_NULLABLE expected", lexer.yyline());
            }
            symb = lexer.lex();
        }
        int protectedns = 0;
        if (symb.type == ParsedSymbol.TYPE_KEYWORD_PROTECTEDNS_BLOCK && ((instanceFlags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS)) {
            protectedns = parseNamespace(constants, lexer);
        } else {
            lexer.pushback(symb);
        }
        expectEnd(lexer); //instance
        expectEnd(lexer); //class
        expectEnd(lexer); //trait

        InstanceInfo ii = abc.instance_info.get(tc.class_info);
        ii.name_index = instance_name_index;
        ii.super_index = super_index;
        ii.interfaces = interfaces;
        ii.flags = instanceFlags;
        if ((instanceFlags & InstanceInfo.CLASS_PROTECTEDNS) == InstanceInfo.CLASS_PROTECTEDNS) {
            ii.protectedNS = protectedns;
        }

        tc.slot_id = slotid;
        tc.name_index = name_index;

        return true;
    }

    private static boolean parseSlotConst(ABC abc, Flasm3Lexer lexer, AVM2ConstantPool constants, TraitSlotConst tsc) throws IOException, AVM2ParseException {
        expected(ParsedSymbol.TYPE_KEYWORD_TRAIT, "trait", lexer);
        ParsedSymbol symb = lexer.lex();
        if (symb.type == ParsedSymbol.TYPE_KEYWORD_SLOT) {
            tsc.kindType = Trait.TRAIT_SLOT;
        } else if (symb.type == ParsedSymbol.TYPE_KEYWORD_CONST) {
            tsc.kindType = Trait.TRAIT_CONST;
        } else {
            throw new AVM2ParseException("slot or const expected", lexer.yyline());
        }
        int name_index = parseMultiName(constants, lexer);
        parseTraitParams(abc, lexer, tsc);

        expected(ParsedSymbol.TYPE_KEYWORD_SLOTID, "slotid", lexer);
        symb = lexer.lex();
        int slotid = getInteger(symb, lexer.yyline());
        expected(ParsedSymbol.TYPE_KEYWORD_TYPE, "type", lexer);
        int type = parseMultiName(constants, lexer);
        symb = lexer.lex();
        if (symb.type == ParsedSymbol.TYPE_KEYWORD_VALUE) {
            ValueKind val = parseValue(constants, lexer);
            tsc.value_kind = val.value_kind;
            tsc.value_index = val.value_index;
        } else {
            tsc.value_kind = ValueKind.CONSTANT_Undefined;
            tsc.value_index = 0;
        }
        tsc.slot_id = slotid;
        tsc.type_index = type;
        tsc.name_index = name_index;
        return true;
    }

    /**
     * Parses a slot or const.
     * @param abc ABC
     * @param reader Reader
     * @param constants Constant pool
     * @param tsc Trait slot/const
     * @return True if parsed successfully
     * @throws IOException On I/O error
     * @throws AVM2ParseException On parse error
     */
    public static boolean parseSlotConst(ABC abc, Reader reader, AVM2ConstantPool constants, TraitSlotConst tsc) throws IOException, AVM2ParseException {
        Flasm3Lexer lexer = new Flasm3Lexer(reader);
        return parseSlotConst(abc, lexer, constants, tsc);
    }

    
    private static int parseNumberContext(Flasm3Lexer lexer) throws AVM2ParseException, IOException {
        ParsedSymbol s = lexer.lex();
        expected(s, ParsedSymbol.TYPE_KEYWORD_NUMBERCONTEXT, "NumberContext", lexer.yyline());
        expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
        s = lexer.lex();
        int usage;
        switch (s.type) {
            case ParsedSymbol.TYPE_KEYWORD_NUMBER:
                usage = NumberContext.USE_NUMBER;
                break;
            case ParsedSymbol.TYPE_KEYWORD_DECIMAL:
                usage = NumberContext.USE_DECIMAL;
                break;
            case ParsedSymbol.TYPE_KEYWORD_DOUBLE:
                usage = NumberContext.USE_DOUBLE;
                break;
            case ParsedSymbol.TYPE_KEYWORD_INT:
                usage = NumberContext.USE_INT;
                break;
            case ParsedSymbol.TYPE_KEYWORD_UINT:
                usage = NumberContext.USE_UINT;
                break;
            default:
                throw new AVM2ParseException("Usage expected - one of: Number, decimal, double, int or uint", lexer.yyline());
        }
        int rounding = NumberContext.ROUND_HALF_UP;   
        int precision = 34;
        if (usage == NumberContext.USE_NUMBER || usage == NumberContext.USE_DECIMAL) {
            expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
            s = lexer.lex();
            switch (s.type) {
                case ParsedSymbol.TYPE_KEYWORD_CEILING:
                    rounding = NumberContext.ROUND_CEILING;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_UP:
                    rounding = NumberContext.ROUND_UP;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_HALF_UP:
                    rounding = NumberContext.ROUND_HALF_UP;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_HALF_EVEN:
                    rounding = NumberContext.ROUND_HALF_EVEN;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_HALF_DOWN:
                    rounding = NumberContext.ROUND_HALF_DOWN;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_DOWN:
                    rounding = NumberContext.ROUND_DOWN;
                    break;
                case ParsedSymbol.TYPE_KEYWORD_FLOOR:
                    rounding = NumberContext.ROUND_FLOOR;
                    break;
                default:
                    throw new AVM2ParseException("Rounding expected - one of: CEILING, UP, HALF_UP, HALF_EVEN, HALF_DOWN, DOWN, FLOOR", lexer.yyline());        
            }
            s = lexer.lex();
            if (s.type == ParsedSymbol.TYPE_COMMA) {
                s = lexer.lex();
                precision = (int) getUInteger(s, lexer.yyline(), false);
                if (precision > 34) {
                    throw new AVM2ParseException("Precision must not exceed 34", lexer.yyline());
                }
            } else {
                lexer.pushback(s);
            }
        }
        expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
        return new NumberContext(usage, precision, rounding).toParam();
    }
    
    private static int parseNamespaceSet(AVM2ConstantPool constants, Flasm3Lexer lexer) throws AVM2ParseException, IOException {
        List<Integer> namespaceList = new ArrayList<>();
        ParsedSymbol s = lexer.lex();

        if (s.type == ParsedSymbol.TYPE_KEYWORD_UNKNOWN) {
            expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
            s = lexer.lex();
            int index = (int)getUInteger(s, lexer.yyline(), false);
            expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
            return index;
        }

        if (s.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
            return 0;
        }
        expected(s, ParsedSymbol.TYPE_BRACKET_OPEN, "[", lexer.yyline());
        s = lexer.lex();
        if (s.type != ParsedSymbol.TYPE_BRACKET_CLOSE) {
            lexer.pushback(s);
            do {
                namespaceList.add(parseNamespace(constants, lexer));
                s = lexer.lex();
            } while (s.type == ParsedSymbol.TYPE_COMMA);
            expected(s, ParsedSymbol.TYPE_BRACKET_CLOSE, "]", lexer.yyline());
        }
        loopn:
        for (int n = 1; n < constants.getNamespaceSetCount(); n++) {
            int[] nss = constants.getNamespaceSet(n).namespaces;
            if (nss.length != namespaceList.size()) {
                continue;
            }
            for (int i = 0; i < nss.length; i++) {
                if (nss[i] != namespaceList.get(i)) {
                    continue loopn;
                }
            }
            return n;
        }
        int[] nss = new int[namespaceList.size()];
        for (int i = 0; i < nss.length; i++) {
            nss[i] = namespaceList.get(i);
        }
        return constants.addNamespaceSet(new NamespaceSet(nss));
    }

    private static int parseNamespace(AVM2ConstantPool constants, Flasm3Lexer lexer) throws AVM2ParseException, IOException {

        ParsedSymbol type = lexer.lex();
        int kind = 0;
        switch (type.type) {
            case ParsedSymbol.TYPE_KEYWORD_UNKNOWN:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                ParsedSymbol s = lexer.lex();
                int unkId = (int)getUInteger(s, kind, false);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                return unkId;
            case ParsedSymbol.TYPE_KEYWORD_NULL:
                return 0;
            case ParsedSymbol.TYPE_KEYWORD_NAMESPACE:
                kind = Namespace.KIND_NAMESPACE;
                break;
            case ParsedSymbol.TYPE_KEYWORD_PRIVATENAMESPACE:
                kind = Namespace.KIND_PRIVATE;
                break;
            case ParsedSymbol.TYPE_KEYWORD_PACKAGENAMESPACE:
                kind = Namespace.KIND_PACKAGE;
                break;
            case ParsedSymbol.TYPE_KEYWORD_PACKAGEINTERNALNS:
                kind = Namespace.KIND_PACKAGE_INTERNAL;
                break;
            case ParsedSymbol.TYPE_KEYWORD_PROTECTEDNAMESPACE:
                kind = Namespace.KIND_PROTECTED;
                break;
            case ParsedSymbol.TYPE_KEYWORD_EXPLICITNAMESPACE:
                kind = Namespace.KIND_EXPLICIT;
                break;
            case ParsedSymbol.TYPE_KEYWORD_STATICPROTECTEDNS:
                kind = Namespace.KIND_STATIC_PROTECTED;
                break;
            default:
                throw new AVM2ParseException("Namespace kind expected", lexer.yyline());
        }

        expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
        ParsedSymbol name = lexer.lex();
        if (name.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
            //empty
        } else if (name.type == ParsedSymbol.TYPE_STRING) {
            //empty
        } else {
            throw new AVM2ParseException("String or null expected", lexer.yyline());
        }
        ParsedSymbol c = lexer.lex();
        int index = 0;
        if (c.type == ParsedSymbol.TYPE_COMMA) {
            ParsedSymbol extra = lexer.lex();
            expected(extra, ParsedSymbol.TYPE_STRING, "String", lexer.yyline());
            try {
                index = Integer.parseInt((String) extra.value);
            } catch (NumberFormatException nfe) {
                throw new AVM2ParseException("Number expected", lexer.yyline());
            }
        } else {
            lexer.pushback(c);
        }
        expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);

        return constants.getNamespaceId(kind, name.type == ParsedSymbol.TYPE_KEYWORD_NULL ? null : (String) name.value, index, true);
    }

    private static int parseMultiName(AVM2ConstantPool constants, Flasm3Lexer lexer) throws AVM2ParseException, IOException {
        ParsedSymbol s = lexer.lex();
        int kind = 0;

        switch (s.type) {
            case ParsedSymbol.TYPE_KEYWORD_UNKNOWN:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                s = lexer.lex();
                int unkId = (int)getUInteger(s, kind, false);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                return unkId;
            case ParsedSymbol.TYPE_KEYWORD_NULL:
                return 0;
            case ParsedSymbol.TYPE_KEYWORD_QNAME:
                kind = Multiname.QNAME;
                break;
            case ParsedSymbol.TYPE_KEYWORD_QNAMEA:
                kind = Multiname.QNAMEA;
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAME:
                kind = Multiname.RTQNAME;
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMEA:
                kind = Multiname.RTQNAMEA;
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMEL:
                kind = Multiname.RTQNAMEL;
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMELA:
                kind = Multiname.RTQNAMELA;
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAME:
                kind = Multiname.MULTINAME;
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMEA:
                kind = Multiname.MULTINAMEA;
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMEL:
                kind = Multiname.MULTINAMEL;
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMELA:
                kind = Multiname.MULTINAMELA;
                break;
            case ParsedSymbol.TYPE_KEYWORD_TYPENAME:
                kind = Multiname.TYPENAME;
                break;
            default:
                throw new AVM2ParseException("Name expected", lexer.yyline());
        }

        Multiname multiname = null;
        switch (kind) {
            case Multiname.QNAME:
            case Multiname.QNAMEA: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                int namespace_index = parseNamespace(constants, lexer);
                expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                ParsedSymbol name = lexer.lex();
                int name_index;
                if (name.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    name_index = 0;
                } else {
                    expected(name, ParsedSymbol.TYPE_STRING, "String", lexer.yyline());
                    name_index = constants.getStringId((String) name.value, true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createQName(kind == Multiname.QNAMEA, name_index, namespace_index);
                break;
            }
            case Multiname.RTQNAME:
            case Multiname.RTQNAMEA: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                ParsedSymbol rtqName = lexer.lex();
                int name_index;
                if (rtqName.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    name_index = 0;
                } else {
                    expected(rtqName, ParsedSymbol.TYPE_STRING, "String", lexer.yyline());
                    name_index = constants.getStringId((String) rtqName.value, true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createRTQName(kind == Multiname.RTQNAMEA, name_index);
                break;
            }
            case Multiname.RTQNAMEL:
            case Multiname.RTQNAMELA: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createRTQNameL(kind == Multiname.RTQNAMELA);
                break;
            }
            case Multiname.MULTINAME:
            case Multiname.MULTINAMEA: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                ParsedSymbol mName = lexer.lex();
                int name_index;
                if (mName.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    name_index = 0;
                } else {
                    expected(mName, ParsedSymbol.TYPE_STRING, "String", lexer.yyline());
                    name_index = constants.getStringId((String) mName.value, true);
                }
                expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                int namespace_set_index = parseNamespaceSet(constants, lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createMultiname(kind == Multiname.MULTINAMEA, name_index, namespace_set_index);
                break;
            }
            case Multiname.MULTINAMEL:
            case Multiname.MULTINAMELA: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                int namespace_set_index = parseNamespaceSet(constants, lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createMultinameL(kind == Multiname.MULTINAMELA, namespace_set_index);
                break;
            }
            case Multiname.TYPENAME: {
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                int qname_index = parseMultiName(constants, lexer);
                expected(ParsedSymbol.TYPE_LOWERTHAN, "<", lexer);
                List<Integer> paramsList = new ArrayList<>();
                paramsList.add(parseMultiName(constants, lexer));
                ParsedSymbol nt = lexer.lex();
                while (nt.type == ParsedSymbol.TYPE_COMMA) {
                    paramsList.add(parseMultiName(constants, lexer));
                    nt = lexer.lex();
                }
                int[] params = Helper.toIntArray(paramsList);
                expected(nt, ParsedSymbol.TYPE_GREATERTHAN, ">", lexer.yyline());
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                multiname = Multiname.createTypeName(qname_index, params);
                break;
            }
        }

        return constants.getMultinameId(multiname, true);
    }

    /**
     * Parses value.
     * @param constants Constant pool
     * @param lexer Lexer
     * @return Value kind
     * @throws IOException On I/O error
     * @throws AVM2ParseException On parse error
     */
    public static ValueKind parseValue(AVM2ConstantPool constants, Flasm3Lexer lexer) throws IOException, AVM2ParseException {
        ParsedSymbol type = lexer.lex();
        ParsedSymbol value;
        ParsedSymbol temp;
        int value_index = 0;
        int value_kind = 0;
        switch (type.type) {
            case ParsedSymbol.TYPE_KEYWORD_INTEGER:
                value_kind = ValueKind.CONSTANT_Int;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    value_index = constants.getIntId(getInteger(value, lexer.yyline(), true), true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_UINTEGER:
                value_kind = ValueKind.CONSTANT_UInt;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    value_index = constants.getUIntId(getUInteger(value, lexer.yyline(), true), true);
                }

                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_DOUBLE:
                value_kind = ValueKind.CONSTANT_Double;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    value_index = constants.getDoubleId(getDouble(value, lexer.yyline(), true), true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;       
            case ParsedSymbol.TYPE_KEYWORD_FLOAT:
                value_kind = ValueKind.CONSTANT_DecimalOrFloat;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    value_index = constants.getFloatId(getFloat(value, lexer.yyline(), true), true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_FLOAT4:
                value_kind = ValueKind.CONSTANT_Float4;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    float f1 = getFloat(value, lexer.yyline(), true);
                    expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                    value = lexer.lex();
                    float f2 = getFloat(value, lexer.yyline(), true);
                    expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                    value = lexer.lex();                    
                    float f3 = getFloat(value, lexer.yyline(), true);
                    expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                    value = lexer.lex();                    
                    float f4 = getFloat(value, lexer.yyline(), true);
                    Float4 fval = new Float4(f1, f2, f3, f4);
                    value_index = constants.getFloat4Id(fval, true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;  
            case ParsedSymbol.TYPE_NUMBER:
                String nval = (String) type.value;
                if (nval.endsWith("m")) {
                    nval = nval.substring(0, nval.length() - 1);
                    value_kind = ValueKind.CONSTANT_DecimalOrFloat;
                    value_index = constants.getDecimalId(new Decimal128(nval), true);
                    break;
                }
                if (nval.endsWith("d") || nval.contains("e") || nval.contains("E") || nval.contains(".")) {
                    if (nval.endsWith("d")) {
                        nval = nval.substring(0, nval.length() - 1);
                    }
                    value_kind = ValueKind.CONSTANT_Double;
                    value_index = constants.getDoubleId(Double.parseDouble(nval), true);
                    break;
                }
                
                if (nval.endsWith("u")) {
                    nval = nval.substring(0, nval.length() - 1);
                }
                if (nval.endsWith("i")) {
                    nval = nval.substring(0, nval.length() - 1);
                }                
                                
                value_kind = ValueKind.CONSTANT_Int;
                value_index = constants.getIntId(Integer.parseInt(nval), true);
                break;
            case ParsedSymbol.TYPE_STRING:
                value_kind = ValueKind.CONSTANT_Utf8;
                value_index = constants.getStringId((String) type.value, true);
                break;
            case ParsedSymbol.TYPE_KEYWORD_UTF8:
                value_kind = ValueKind.CONSTANT_Utf8;
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value = lexer.lex();
                if (value.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    value_index = 0;
                } else {
                    expected(value, ParsedSymbol.TYPE_STRING, "String or null", lexer.yyline());
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                    value_index = constants.getStringId((String) value.value, true);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_TRUE:
                value_kind = ValueKind.CONSTANT_True;
                value_index = ValueKind.CONSTANT_True;
                temp = lexer.lex();
                if (temp.type == ParsedSymbol.TYPE_PARENT_OPEN) {
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                } else {
                    lexer.pushback(temp);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_FALSE:
                value_kind = ValueKind.CONSTANT_False;
                value_index = ValueKind.CONSTANT_False;
                temp = lexer.lex();
                if (temp.type == ParsedSymbol.TYPE_PARENT_OPEN) {
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                } else {
                    lexer.pushback(temp);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_NULL:
                value_kind = ValueKind.CONSTANT_Null;
                value_index = ValueKind.CONSTANT_Null;
                temp = lexer.lex();
                if (temp.type == ParsedSymbol.TYPE_PARENT_OPEN) {
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                } else {
                    lexer.pushback(temp);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_VOID:
                value_kind = ValueKind.CONSTANT_Undefined;
                value_index = ValueKind.CONSTANT_Undefined;
                temp = lexer.lex();
                if (temp.type == ParsedSymbol.TYPE_PARENT_OPEN) {
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                } else {
                    lexer.pushback(temp);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_NAMESPACE:
            case ParsedSymbol.TYPE_KEYWORD_PACKAGEINTERNALNS:
            case ParsedSymbol.TYPE_KEYWORD_PROTECTEDNAMESPACE:
            case ParsedSymbol.TYPE_KEYWORD_EXPLICITNAMESPACE:
            case ParsedSymbol.TYPE_KEYWORD_STATICPROTECTEDNS:
            case ParsedSymbol.TYPE_KEYWORD_PRIVATENAMESPACE:
            case ParsedSymbol.TYPE_KEYWORD_PACKAGENAMESPACE:

                switch (type.type) {
                    case ParsedSymbol.TYPE_KEYWORD_NAMESPACE:
                        value_kind = ValueKind.CONSTANT_Namespace;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_PACKAGEINTERNALNS:
                        value_kind = ValueKind.CONSTANT_PackageInternalNs;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_PROTECTEDNAMESPACE:
                        value_kind = ValueKind.CONSTANT_ProtectedNamespace;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_EXPLICITNAMESPACE:
                        value_kind = ValueKind.CONSTANT_ExplicitNamespace;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_STATICPROTECTEDNS:
                        value_kind = ValueKind.CONSTANT_StaticProtectedNs;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_PRIVATENAMESPACE:
                        value_kind = ValueKind.CONSTANT_PrivateNs;
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_PACKAGENAMESPACE:
                        value_kind = ValueKind.CONSTANT_PackageNamespace;
                        break;
                }
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                value_index = parseNamespace(constants, lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            default:
                if (Configuration._debugMode.get()) {
                    throw new AVM2ParseException("Not supported valueType.", lexer.yyline());
                }
        }
        return new ValueKind(value_index, value_kind);
    }

    /**
     * Parses code.
     * @param abc ABC
     * @param reader Reader
     * @param trait Trait
     * @param body Method body
     * @param info Method info
     * @return AVM2 code
     * @throws IOException On I/O error
     * @throws AVM2ParseException On parse error
     * @throws InterruptedException On interrupt
     */
    public static AVM2Code parse(ABC abc, Reader reader, Trait trait, MethodBody body, MethodInfo info) throws IOException, AVM2ParseException, InterruptedException {
        return parse(abc, reader, trait, null, body, info);
    }

    /**
     * Parses code.
     * @param abc ABC
     * @param reader Reader
     * @param trait Trait
     * @param missingHandler Missing symbol handler
     * @param body Method body
     * @param info Method info
     * @return AVM2 code
     * @throws IOException On I/O error
     * @throws AVM2ParseException On parse error
     * @throws InterruptedException On interrupt
     */
    public static AVM2Code parse(ABC abc, Reader reader, Trait trait, MissingSymbolHandler missingHandler, MethodBody body, MethodInfo info) throws IOException, AVM2ParseException, InterruptedException {
        AVM2ConstantPool constants = abc.constants;
        AVM2Code code = new AVM2Code();
        boolean autoCloseBlocks = true; //TODO? Put to false. But how about old imports?
        List<OffsetItem> offsetItems = new ArrayList<>();
        Map<String, Integer> labelToOffset = new HashMap<>();
        List<ABCException> exceptions = new ArrayList<>();
        List<Integer> exceptionIndices = new ArrayList<>();
        int offset = 0;

        Flasm3Lexer lexer = new Flasm3Lexer(reader);

        ParsedSymbol symb;
        AVM2Instruction lastIns = null;
        List<String> exceptionsFrom = new ArrayList<>();
        List<String> exceptionsTo = new ArrayList<>();
        List<String> exceptionsTargets = new ArrayList<>();
        List<Integer> exceptionLines = new ArrayList<>();
        info.flags = 0;
        info.name_index = 0;
        List<Integer> paramTypes = new ArrayList<>();
        List<Integer> paramNames = new ArrayList<>();
        List<ValueKind> optional = new ArrayList<>();
        Stack<Integer> blockStack = new Stack<>();
        if (body != null) {
            body.traits = new Traits();
        }
        do {
            symb = lexer.lex();
            if (Arrays.asList(ParsedSymbol.TYPE_KEYWORD_BODY, ParsedSymbol.TYPE_KEYWORD_CODE, ParsedSymbol.TYPE_KEYWORD_METHOD).contains(symb.type)) {
                if (body == null && symb.type == ParsedSymbol.TYPE_KEYWORD_BODY) {
                    throw new AVM2ParseException("This method cannot have a body.", lexer.yyline());
                }
                blockStack.push(symb.type);
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_TRAIT) {
                blockStack.push(symb.type);
                if (blockStack.contains(ParsedSymbol.TYPE_KEYWORD_BODY)) {
                    lexer.pushback(symb);
                    TraitSlotConst tsc = new TraitSlotConst();
                    parseSlotConst(abc, lexer, constants, tsc);
                    body.traits.addTrait(tsc);
                } else {
                    if (trait == null) {
                        throw new AVM2ParseException("No trait expected", lexer.yyline());
                    }
                    symb = lexer.lex();
                    switch (symb.type) {
                        case ParsedSymbol.TYPE_KEYWORD_METHOD:
                        case ParsedSymbol.TYPE_KEYWORD_GETTER:
                        case ParsedSymbol.TYPE_KEYWORD_SETTER:
                            if (!(trait instanceof TraitMethodGetterSetter)) {
                                throw new AVM2ParseException("Unxpected trait type", lexer.yyline());
                            }
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) trait;
                            switch (symb.type) {
                                case ParsedSymbol.TYPE_KEYWORD_METHOD:
                                    tm.kindType = Trait.TRAIT_METHOD;
                                    break;
                                case ParsedSymbol.TYPE_KEYWORD_GETTER:
                                    tm.kindType = Trait.TRAIT_GETTER;
                                    break;
                                case ParsedSymbol.TYPE_KEYWORD_SETTER:
                                    tm.kindType = Trait.TRAIT_SETTER;
                                    break;
                            }
                            tm.name_index = parseMultiName(constants, lexer);
                            parseTraitParams(abc, lexer, trait);
                            expected(ParsedSymbol.TYPE_KEYWORD_DISPID, "dispid", lexer);
                            symb = lexer.lex();
                            tm.disp_id = (int)getUInteger(symb, lexer.yyline(), false);

                            break;
                        case ParsedSymbol.TYPE_KEYWORD_FUNCTION:
                            if (!(trait instanceof TraitFunction)) {
                                throw new AVM2ParseException("Unxpected trait type", lexer.yyline());
                            }

                            //NAME
                            parseTraitParams(abc, lexer, trait);
                            break;
                    }
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_NAME) {
                symb = lexer.lex();
                if (symb.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    info.name_index = 0;
                } else {
                    expected(symb, ParsedSymbol.TYPE_STRING, "String or null", lexer.yyline());
                    info.name_index = constants.getStringId((String) symb.value, true);
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_PARAM) {
                paramTypes.add(parseMultiName(constants, lexer));
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_PARAMNAME) {
                symb = lexer.lex();
                if (symb.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    paramNames.add(0);
                } else {
                    expected(symb, ParsedSymbol.TYPE_STRING, "String or null", lexer.yyline());
                    paramNames.add(constants.getStringId((String) symb.value, true));
                }
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_OPTIONAL) {
                optional.add(parseValue(constants, lexer));
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_MAXSTACK) {
                symb = lexer.lex();
                body.max_stack = (int)getUInteger(symb, lexer.yyline(), false);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_LOCALCOUNT) {
                symb = lexer.lex();
                body.max_regs = (int)getUInteger(symb, lexer.yyline(), false);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_INITSCOPEDEPTH) {
                symb = lexer.lex();
                body.init_scope_depth = (int)getUInteger(symb, lexer.yyline(), false);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_MAXSCOPEDEPTH) {
                symb = lexer.lex();
                body.max_scope_depth = (int)getUInteger(symb, lexer.yyline(), false);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_RETURNS) {
                info.ret_type = parseMultiName(constants, lexer);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_FLAG) {
                symb = lexer.lex();
                switch (symb.type) {
                    case ParsedSymbol.TYPE_KEYWORD_NATIVE:
                        info.setFlagNative();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_HAS_OPTIONAL:
                        info.setFlagHas_optional();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_HAS_PARAM_NAMES:
                        info.setFlagHas_paramnames();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_IGNORE_REST:
                        info.setFlagIgnore_Rest();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_NEED_ACTIVATION:
                        info.setFlagNeed_activation();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_NEED_ARGUMENTS:
                        info.setFlagNeed_Arguments();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_NEED_REST:
                        info.setFlagNeed_rest();
                        break;
                    case ParsedSymbol.TYPE_KEYWORD_SET_DXNS:
                        info.setFlagSetsdxns();
                        break;
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_TRY) {
                exceptionLines.add(lexer.yyline());
                expected(ParsedSymbol.TYPE_KEYWORD_FROM, "From", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier", lexer.yyline());
                exceptionsFrom.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TO, "To", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier", lexer.yyline());
                exceptionsTo.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TARGET, "Target", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier", lexer.yyline());
                exceptionsTargets.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TYPE, "Type", lexer);
                ABCException ex = new ABCException();
                ex.type_index = parseMultiName(constants, lexer);
                expected(ParsedSymbol.TYPE_KEYWORD_NAME, "Name", lexer);
                ex.name_index = parseMultiName(constants, lexer);
                exceptions.add(ex);
                symb = lexer.lex();
                if (symb.type != ParsedSymbol.TYPE_KEYWORD_END) {
                    lexer.pushback(symb);
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_START) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new AVM2ParseException("Undefined exception index", lexer.yyline());
                }
                exceptions.get(listIndex).start = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_END) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new AVM2ParseException("Undefined exception index", lexer.yyline());
                }
                exceptions.get(listIndex).end = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EXCEPTION_TARGET) {
                int exIndex = (Integer) symb.value;
                int listIndex = exceptionIndices.indexOf(exIndex);
                if (listIndex == -1) {
                    throw new AVM2ParseException("Undefined exception index", lexer.yyline());
                }
                exceptions.get(listIndex).target = offset;
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_EOF) {
                break;
            }
            if (symb.type == ParsedSymbol.TYPE_COMMENT) {
                if (lastIns != null && blockStack.contains(ParsedSymbol.TYPE_KEYWORD_CODE)) {
                    lastIns.comment = (String) symb.value;
                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_END) {
                if (blockStack.isEmpty()) {
                    throw new AVM2ParseException("End block encountered but there is no block opened", lexer.yyline());
                }
                blockStack.pop();
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                if (((String) symb.value).toLowerCase(Locale.ENGLISH).equals("exception")) {
                    ParsedSymbol exIndexSymbol = lexer.lex();
                    int exIndex = getInteger(exIndexSymbol, lexer.yyline());
                    ParsedSymbol exName = lexer.lex();
                    if (exName.type != ParsedSymbol.TYPE_MULTINAME) {
                        throw new AVM2ParseException("Multiname expected", lexer.yyline());
                    }
                    ParsedSymbol exType = lexer.lex();
                    if (exType.type != ParsedSymbol.TYPE_MULTINAME) {
                        throw new AVM2ParseException("Multiname expected", lexer.yyline());
                    }
                    ABCException ex = new ABCException();

                    ex.name_index = checkMultinameIndex(constants, (int) (long) (Long) exName.value, lexer.yyline());
                    ex.type_index = checkMultinameIndex(constants, (int) (long) (Long) exType.value, lexer.yyline());
                    exceptions.add(ex);
                    exceptionIndices.add(exIndex);
                    continue;
                }
                String insName = (String) symb.value;
                if (AVM2Code.instructionAliases.containsKey(insName)) {
                    insName = AVM2Code.instructionAliases.get(insName); //search original unaliased name
                }
                boolean insFound = false;
                for (InstructionDefinition def : AVM2Code.instructionSet) {
                    if (def != null && !(def instanceof UnknownInstruction) && def.instructionName.equals(insName)) {
                        insFound = true;
                        List<Integer> operandsList = new ArrayList<>();

                        for (int i = 0; i < def.operands.length; i++) {
                            ParsedSymbol parsedOperand = lexer.lex();
                            if (i > 0) {
                                if (parsedOperand.type == ParsedSymbol.TYPE_COMMA) {
                                    parsedOperand = lexer.lex();
                                }
                            }
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                case AVM2Code.DAT_NAMESPACE_INDEX:
                                case AVM2Code.DAT_STRING_INDEX:
                                case AVM2Code.DAT_INT_INDEX:
                                case AVM2Code.DAT_UINT_INDEX:
                                case AVM2Code.DAT_DOUBLE_INDEX:
                                case AVM2Code.DAT_DECIMAL_INDEX:
                                case AVM2Code.DAT_FLOAT_INDEX:
                                case AVM2Code.DAT_FLOAT4_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_UNKNOWN) {
                                        expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                                        ParsedSymbol indexSymb = lexer.lex();
                                        int unkIndex = (int)getUInteger(indexSymb, lexer.yyline(), false);
                                        expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                                        operandsList.add(unkIndex);
                                        continue;
                                    }
                                    break;
                            }
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                    lexer.pushback(parsedOperand);
                                    operandsList.add(parseMultiName(constants, lexer));
                                    break;
                                case AVM2Code.DAT_NAMESPACE_INDEX:
                                    lexer.pushback(parsedOperand);
                                    operandsList.add(parseNamespace(constants, lexer));
                                    break;
                                case AVM2Code.DAT_STRING_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else if (parsedOperand.type == ParsedSymbol.TYPE_STRING) {
                                        int sid = constants.getStringId((String) parsedOperand.value, false);
                                        if (sid == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingString((String) parsedOperand.value))) {
                                                sid = constants.addString((String) parsedOperand.value);
                                            } else {
                                                throw new AVM2ParseException("Unknown String", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(sid);
                                    } else {
                                        throw new AVM2ParseException("String or null expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_INT_INDEX:

                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {                                        
                                        int intVal = getInteger(parsedOperand, lexer.yyline(), true);
                                        int iid = constants.getIntId(intVal, false);
                                        if (iid == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingInt(intVal))) {
                                                iid = constants.addInt(intVal);
                                            } else {
                                                throw new AVM2ParseException("Unknown int", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(iid);
                                    }
                                    break;
                                case AVM2Code.DAT_UINT_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
                                        long intVal = getUInteger(parsedOperand, lexer.yyline(), true);
                                        int iid = constants.getUIntId(intVal, false);
                                        if (iid == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingUInt(intVal))) {
                                                iid = constants.addUInt(intVal);
                                            } else {
                                                throw new AVM2ParseException("Unknown uint", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(iid);
                                    }
                                    break;
                                case AVM2Code.DAT_DOUBLE_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
                                        double doubleVal = getDouble(parsedOperand, lexer.yyline(), true);
                                        int did = constants.getDoubleId(doubleVal, false);
                                        if (did == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingDouble(doubleVal))) {
                                                did = constants.addDouble(doubleVal);
                                            } else {
                                                throw new AVM2ParseException("Unknown double", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(did);
                                    }
                                    break;
                                case AVM2Code.DAT_DECIMAL_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else{
                                        Decimal128 decimalVal = getDecimal(parsedOperand, lexer.yyline(), true);
                                        int did = constants.getDecimalId(decimalVal, false);
                                        if (did == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingDecimal(decimalVal))) {
                                                did = constants.addDecimal(decimalVal);
                                            } else {
                                                throw new AVM2ParseException("Unknown decimal", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(did);
                                    }
                                    break;
                                case AVM2Code.DAT_NUMBER_CONTEXT:
                                    lexer.pushback(parsedOperand);
                                    operandsList.add(parseNumberContext(lexer));
                                    break;
                                case AVM2Code.DAT_FLOAT_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {

                                        float floatVal = getFloat(parsedOperand, lexer.yyline(), true);
                                        int fid = constants.getFloatId(floatVal, false);
                                        if (fid == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingFloat(floatVal))) {
                                                fid = constants.addFloat(floatVal);
                                            } else {
                                                throw new AVM2ParseException("Unknown float", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(fid);
                                    }
                                    break;
                                case AVM2Code.DAT_FLOAT4_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
                                        float[] float4Vals = new float[4];
                                        for (int k = 0; k < 4; k++) {
                                            float4Vals[k] = getFloat(parsedOperand, lexer.yyline(), false);
                                            if (k + 1 < 4) { //not last one
                                                parsedOperand = lexer.lex();
                                            }
                                        }
                                        Float4 float4Val = new Float4(float4Vals);
                                        int f4id = constants.getFloat4Id(float4Val, false);
                                        if (f4id == -1) {
                                            if ((missingHandler != null) && (missingHandler.missingFloat4(float4Val))) {
                                                f4id = constants.addFloat4(float4Val);
                                            } else {
                                                throw new AVM2ParseException("Unknown float4", lexer.yyline());
                                            }
                                        }
                                        operandsList.add(f4id);
                                    }
                                    break;
                                case AVM2Code.DAT_OFFSET:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                        offsetItems.add(new OffsetItem((String) parsedOperand.value, code.code.size(), i, lexer.yyline()));
                                        operandsList.add(0);
                                    } else {
                                        throw new AVM2ParseException("Offset expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.DAT_CASE_BASEOFFSET:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                        offsetItems.add(new CaseOffsetItem((String) parsedOperand.value, code.code.size(), i, lexer.yyline()));
                                        operandsList.add(0);
                                    } else {
                                        throw new AVM2ParseException("Offset expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.OPT_CASE_OFFSETS:

                                    if (parsedOperand.type == ParsedSymbol.TYPE_BRACKET_OPEN) {
                                        parsedOperand = lexer.lex();

                                        int c = 0;
                                        while (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                            offsetItems.add(new CaseOffsetItem((String) parsedOperand.value, code.code.size(), i + (c + 1), lexer.yyline()));
                                            c++;
                                            parsedOperand = lexer.lex();
                                            if (parsedOperand.type == ParsedSymbol.TYPE_BRACKET_CLOSE) {
                                                break;
                                            }
                                            if (parsedOperand.type == ParsedSymbol.TYPE_COMMA) {
                                                parsedOperand = lexer.lex();
                                            }
                                        }
                                        if (parsedOperand.type != ParsedSymbol.TYPE_BRACKET_CLOSE) {
                                            throw new AVM2ParseException("Bracket close ] expected", lexer.yyline());
                                        }
                                        if (c == 0) {
                                            throw new AVM2ParseException("At least single offset expected", lexer.yyline());
                                        }

                                        operandsList.add(c - 1);
                                        for (int d = 0; d < c; d++) {
                                            operandsList.add(0);
                                        }
                                    } else if (parsedOperand.type == ParsedSymbol.TYPE_NUMBER) { //old syntax
                                        int patCount = (int)getUInteger(parsedOperand, lexer.yyline(), false);
                                        operandsList.add(patCount);

                                        for (int c = 0; c <= patCount; c++) {
                                            parsedOperand = lexer.lex();
                                            if (parsedOperand.type == ParsedSymbol.TYPE_COMMA) {
                                                parsedOperand = lexer.lex();
                                            }
                                            if (parsedOperand.type == ParsedSymbol.TYPE_IDENTIFIER) {
                                                offsetItems.add(new CaseOffsetItem((String) parsedOperand.value, code.code.size(), i + (c + 1), lexer.yyline()));
                                                operandsList.add(0);
                                            } else {
                                                throw new AVM2ParseException("Offset expected", lexer.yyline());
                                            }
                                        }
                                    } else {
                                        throw new AVM2ParseException("Bracket open [ or case count expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.OPT_S8:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_NUMBER) {
                                        int val = getInteger(parsedOperand, lexer.yyline(), false);
                                        if (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) {
                                            throw new AVM2ParseException("Byte value expected (" + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE + "). Use pushshort or pushint to push larger values", lexer.yyline());
                                        }
                                        operandsList.add((int) val);
                                    } else {
                                        throw new AVM2ParseException("Integer expected", lexer.yyline());
                                    }
                                    break;
                                case AVM2Code.OPT_S16:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_NUMBER) {
                                        int val = getInteger(parsedOperand, lexer.yyline(), false);
                                        if (val < Short.MIN_VALUE || val > Short.MAX_VALUE) {
                                            throw new AVM2ParseException("Short value expected (" + Short.MIN_VALUE + " to " + Short.MAX_VALUE + "). Use pushint to push larger values", lexer.yyline());
                                        }
                                        operandsList.add((int) val);
                                    } else {
                                        throw new AVM2ParseException("Integer expected", lexer.yyline());
                                    }
                                    break;
                                default:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_NUMBER) {
                                        int val = getInteger(parsedOperand, lexer.yyline());                                        
                                        operandsList.add(val);
                                    } else {
                                        throw new AVM2ParseException("Integer expected", lexer.yyline());
                                    }
                            }
                        }

                        int[] operands = new int[operandsList.size()];
                        for (int i = 0; i < operandsList.size(); i++) {
                            operands[i] = operandsList.get(i);
                        }
                        lastIns = new AVM2Instruction(offset, def, operands);
                        code.code.add(lastIns);
                        offset += lastIns.getBytesLength();
                        break;
                    }
                }
                if (symb.value.toString().toLowerCase().equals("ffdec_deobfuscatepop")) {
                    lastIns = new AVM2Instruction(offset, DeobfuscatePopIns.getInstance(), null);
                    code.code.add(lastIns);
                    offset += lastIns.getBytesLength();
                    insFound = true;
                }
                if (!insFound) {
                    throw new AVM2ParseException("Invalid instruction name:" + (String) symb.value, lexer.yyline());
                }
            } else if (symb.type == ParsedSymbol.TYPE_LABEL) {
                labelToOffset.put((String) symb.value, offset);

            } else {
                throw new AVM2ParseException("Unexpected symbol", lexer.yyline());
            }
        } while (symb.type != ParsedSymbol.TYPE_EOF);

        if (!autoCloseBlocks && !blockStack.isEmpty()) {
            throw new AVM2ParseException("End of the block expected: " + blockStack.size() + "x", lexer.yyline());
        }

        code.compact();

        for (int i = 0; i < exceptions.size(); i++) {
            if (!labelToOffset.containsKey(exceptionsFrom.get(i))) {
                throw new AVM2ParseException("Label " + exceptionsFrom.get(i) + " for exception from not defined", exceptionLines.get(i));
            }
            exceptions.get(i).start = labelToOffset.get(exceptionsFrom.get(i));

            if (!labelToOffset.containsKey(exceptionsTo.get(i))) {
                throw new AVM2ParseException("Label " + exceptionsTo.get(i) + " for exception to not defined", exceptionLines.get(i));
            }
            exceptions.get(i).end = labelToOffset.get(exceptionsTo.get(i));

            if (!labelToOffset.containsKey(exceptionsTargets.get(i))) {
                throw new AVM2ParseException("Label " + exceptionsTargets.get(i) + "for exception target not defined", exceptionLines.get(i));
            }
            exceptions.get(i).target = labelToOffset.get(exceptionsTargets.get(i));
        }

        for (OffsetItem oi : offsetItems) {
            if (CancellableWorker.isInterrupted()) {
                throw new InterruptedException();
            }
            if (!labelToOffset.containsKey(oi.label)) {
                throw new AVM2ParseException("Label " + oi.label + " not defined", oi.line);
            }
            int labelOffset = labelToOffset.get(oi.label);
            AVM2Instruction ins = code.code.get((int) oi.insPosition);
            int relOffset;
            if (oi instanceof CaseOffsetItem) {
                relOffset = labelOffset - (int) ins.getAddress();
            } else {
                relOffset = labelOffset - ((int) ins.getAddress() + ins.getBytesLength());
            }
            ins.operands[oi.insOperandIndex] = relOffset;
        }
        if (body != null) {
            body.exceptions = new ABCException[exceptions.size()];
            for (int e = 0; e < exceptions.size(); e++) {
                body.exceptions[e] = exceptions.get(e);
            }
        }

        info.param_types = new int[paramTypes.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            info.param_types[i] = paramTypes.get(i);
        }

        if (info.flagHas_paramnames()) {
            info.paramNames = new int[paramNames.size()];
            for (int i = 0; i < paramNames.size(); i++) {
                info.paramNames[i] = paramNames.get(i);
            }
        }

        if (info.flagHas_optional()) {
            info.optional = new ValueKind[optional.size()];
            for (int i = 0; i < optional.size(); i++) {
                info.optional[i] = optional.get(i);
            }
        } else {
            info.optional = new ValueKind[0];
        }
        abc.refreshMultinameNamespaceSuffixes();
        return code;
    }
}
