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
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static AVM2Code parse(InputStream is, ConstantPool constants, Trait trait, MethodBody body, MethodInfo info) throws IOException, ParseException {
        return parse(is, constants, trait, null, body, info);
    }

    private static int checkMultinameIndex(ConstantPool constants, int index, int line) throws ParseException {
        if ((index < 0) || (index >= constants.getMultinameCount())) {
            throw new ParseException("Invalid multiname index", line);
        }
        return index;
    }

    private static void expected(int type, String expStr, Flasm3Lexer lexer) throws IOException, ParseException {
        ParsedSymbol s = lexer.lex();
        if (s.type != type) {
            throw new ParseException(expStr + " expected", lexer.yyline());
        }
    }

    private static void expected(ParsedSymbol s, int type, String expStr) throws IOException, ParseException {
        if (s.type != type) {
            throw new ParseException(expStr + " expected", 0);
        }
    }

    public static boolean parseSlotConst(InputStream is, ConstantPool constants, TraitSlotConst tsc) throws IOException, ParseException {
        Flasm3Lexer lexer = null;
        try {
            lexer = new Flasm3Lexer(new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ASM3Parser.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        expected(ParsedSymbol.TYPE_KEYWORD_TRAIT, "trait", lexer);
        int name_index = parseMultiName(constants, lexer);

        ParsedSymbol symb = lexer.lex();


        int flags = 0;
        while (symb.type == ParsedSymbol.TYPE_KEYWORD_FLAG) {
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
                    throw new ParseException("Invalid trait flag", lexer.yyline());
            }
            symb = lexer.lex();
        }

        switch (symb.type) {
            case ParsedSymbol.TYPE_KEYWORD_SLOT:
            case ParsedSymbol.TYPE_KEYWORD_CONST:
                expected(ParsedSymbol.TYPE_KEYWORD_SLOTID, "slotid", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                int slotid = (int) (long) (Long) symb.value;
                expected(ParsedSymbol.TYPE_KEYWORD_TYPE, "type", lexer);
                int type = parseMultiName(constants, lexer);
                expected(ParsedSymbol.TYPE_KEYWORD_VALUE, "value", lexer);
                ValueKind val = parseValue(constants, lexer);
                tsc.slot_id = slotid;
                tsc.type_index = type;
                tsc.value_kind = val.value_kind;
                tsc.value_index = val.value_index;
                tsc.kindFlags = flags;
                break;
            /*case ParsedSymbol.TYPE_KEYWORD_CLASS:
             break;
             case ParsedSymbol.TYPE_KEYWORD_FUNCTION:
             break;
             case ParsedSymbol.TYPE_KEYWORD_METHOD:
             case ParsedSymbol.TYPE_KEYWORD_GETTER:
             case ParsedSymbol.TYPE_KEYWORD_SETTER:
             break;*/
            default:
                throw new ParseException("Unexpected trait type", lexer.yyline());
        }
        tsc.name_index = name_index;
        return true;
    }

    private static int parseNamespaceSet(ConstantPool constants, Flasm3Lexer lexer) throws ParseException, IOException {
        List<Integer> namespaceList = new ArrayList<>();
        ParsedSymbol s = lexer.lex();
        if (s.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
            return 0;
        }
        expected(s, ParsedSymbol.TYPE_BRACKET_OPEN, "[");
        s = lexer.lex();
        if (s.type != ParsedSymbol.TYPE_BRACKET_CLOSE) {
            lexer.pushback(s);
            do {
                namespaceList.add(parseNamespace(constants, lexer));
                s = lexer.lex();
            } while (s.type == ParsedSymbol.TYPE_COMMA);
            expected(s, ParsedSymbol.TYPE_BRACKET_CLOSE, "]");
        }
        loopn:
        for (int n = 1; n < constants.getNamespaceSetCount(); n++) {
            int nss[] = constants.getNamespaceSet(n).namespaces;
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
        int nss[] = new int[namespaceList.size()];
        for (int i = 0; i < nss.length; i++) {
            nss[i] = namespaceList.get(i);
        }
        return constants.addNamespaceSet(new NamespaceSet(nss));
    }

    private static int parseNamespace(ConstantPool constants, Flasm3Lexer lexer) throws ParseException, IOException {

        ParsedSymbol type = lexer.lex();
        int kind = 0;
        switch (type.type) {
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
                throw new ParseException("Namespace kind expected", lexer.yyline());
        }

        expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
        ParsedSymbol name = lexer.lex();
        expected(name, ParsedSymbol.TYPE_STRING, "String");
        ParsedSymbol c = lexer.lex();
        int index = 0;
        if (c.type == ParsedSymbol.TYPE_COMMA) {
            ParsedSymbol extra = lexer.lex();
            expected(name, ParsedSymbol.TYPE_STRING, "String");
            try {
                index = Integer.parseInt((String) extra.value);
            } catch (NumberFormatException nfe) {
                throw new ParseException("Number expected", lexer.yyline());
            }
        } else {
            lexer.pushback(c);
        }
        expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);

        return constants.getNamespaceId(new Namespace(kind, constants.getStringId((String) name.value, true)), index, true);
    }

    private static int parseMultiName(ConstantPool constants, Flasm3Lexer lexer) throws ParseException, IOException {
        ParsedSymbol s = lexer.lex();
        int kind = 0;
        int name_index = -1;
        int namespace_index = -1;
        int namespace_set_index = -1;
        int qname_index = -1;
        List<Integer> params = new ArrayList<>();

        switch (s.type) {
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
                throw new ParseException("Name expected", lexer.yyline());
        }

        switch (s.type) {
            case ParsedSymbol.TYPE_KEYWORD_QNAME:
            case ParsedSymbol.TYPE_KEYWORD_QNAMEA:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                namespace_index = parseNamespace(constants, lexer);
                expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                ParsedSymbol name = lexer.lex();
                expected(name, ParsedSymbol.TYPE_STRING, "String");
                name_index = constants.getStringId((String) name.value, true);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAME:
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMEA:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                ParsedSymbol rtqName = lexer.lex();
                expected(rtqName, ParsedSymbol.TYPE_STRING, "String");
                name_index = constants.getStringId((String) rtqName.value, true);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMEL:
            case ParsedSymbol.TYPE_KEYWORD_RTQNAMELA:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAME:
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMEA:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                ParsedSymbol mName = lexer.lex();
                expected(mName, ParsedSymbol.TYPE_STRING, "String");
                name_index = constants.getStringId((String) mName.value, true);
                expected(ParsedSymbol.TYPE_COMMA, ",", lexer);
                namespace_set_index = parseNamespaceSet(constants, lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMEL:
            case ParsedSymbol.TYPE_KEYWORD_MULTINAMELA:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                namespace_set_index = parseNamespaceSet(constants, lexer);
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            case ParsedSymbol.TYPE_KEYWORD_TYPENAME:
                expected(ParsedSymbol.TYPE_PARENT_OPEN, "(", lexer);
                qname_index = parseMultiName(constants, lexer);
                expected(ParsedSymbol.TYPE_LOWERTHAN, "<", lexer);
                params.add(parseMultiName(constants, lexer));
                ParsedSymbol nt = lexer.lex();
                while (nt.type == ParsedSymbol.TYPE_COMMA) {
                    params.add(parseMultiName(constants, lexer));
                    nt = lexer.lex();
                }
                expected(nt, ParsedSymbol.TYPE_GREATERTHAN, ">");
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
        }

        return constants.getMultinameId(new Multiname(kind, name_index, namespace_index, namespace_set_index, qname_index, params), true);
    }

    public static ValueKind parseValue(ConstantPool constants, Flasm3Lexer lexer) throws IOException, ParseException {
        ParsedSymbol type = lexer.lex();
        ParsedSymbol value;
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
                    expected(value, ParsedSymbol.TYPE_INTEGER, "Integer or null");
                    value_index = constants.getIntId((Long) value.value, true);
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
                    expected(value, ParsedSymbol.TYPE_INTEGER, "UInteger");
                    value_index = constants.getUIntId((Long) value.value, true);
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
                    expected(value, ParsedSymbol.TYPE_FLOAT, "Double or null");
                    value_index = constants.getDoubleId((Double) value.value, true);
                }
                expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                break;
            /*case ParsedSymbol.TYPE_KEYWORD_DECIMAL:
             value_kind = ValueKind.CONSTANT_Decimal;
             break;*/
            case ParsedSymbol.TYPE_INTEGER:
                value_kind = ValueKind.CONSTANT_Int;
                value_index = constants.getIntId((Long) type.value, true);
                break;
            case ParsedSymbol.TYPE_FLOAT:
                value_kind = ValueKind.CONSTANT_Double;
                value_index = constants.getDoubleId((Double) type.value, true);
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
                    expected(value, ParsedSymbol.TYPE_STRING, "String or null");
                    expected(ParsedSymbol.TYPE_PARENT_CLOSE, ")", lexer);
                    value_index = constants.getStringId((String) value.value, true);
                }
                break;
            case ParsedSymbol.TYPE_KEYWORD_TRUE:
                value_kind = ValueKind.CONSTANT_True;
                break;
            case ParsedSymbol.TYPE_KEYWORD_FALSE:
                value_kind = ValueKind.CONSTANT_False;
                break;
            case ParsedSymbol.TYPE_KEYWORD_NULL:
                value_kind = ValueKind.CONSTANT_Null;
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
                lexer.pushback(type);
                value_index = parseNamespace(constants, lexer);
                break;
            default:
                if (Configuration.debugMode.get()) {
                    throw new ParseException("Not supported valueType.", lexer.yyline());
                }
        }
        return new ValueKind(value_index, value_kind);
    }

    public static AVM2Code parse(InputStream is, ConstantPool constants, Trait trait, MissingSymbolHandler missingHandler, MethodBody body, MethodInfo info) throws IOException, ParseException {
        AVM2Code code = new AVM2Code();

        List<OffsetItem> offsetItems = new ArrayList<>();
        List<LabelItem> labelItems = new ArrayList<>();
        List<ABCException> exceptions = new ArrayList<>();
        List<Integer> exceptionIndices = new ArrayList<>();
        int offset = 0;


        Flasm3Lexer lexer = new Flasm3Lexer(new InputStreamReader(is, "UTF-8"));

        ParsedSymbol symb;
        AVM2Instruction lastIns = null;
        List<String> exceptionsFrom = new ArrayList<>();
        List<String> exceptionsTo = new ArrayList<>();
        List<String> exceptionsTargets = new ArrayList<>();
        info.flags = 0;
        info.name_index = 0;
        List<Integer> paramTypes = new ArrayList<>();
        List<Integer> paramNames = new ArrayList<>();
        List<ValueKind> optional = new ArrayList<>();
        do {
            symb = lexer.lex();
            if (Arrays.asList(ParsedSymbol.TYPE_KEYWORD_BODY, ParsedSymbol.TYPE_KEYWORD_CODE, ParsedSymbol.TYPE_KEYWORD_METHOD).contains(symb.type)) {
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_TRAIT) {
                if (trait == null) {
                    throw new ParseException("No trait expected", lexer.yyline());
                }
                symb = lexer.lex();
                switch (symb.type) {
                    case ParsedSymbol.TYPE_KEYWORD_METHOD:
                    case ParsedSymbol.TYPE_KEYWORD_GETTER:
                    case ParsedSymbol.TYPE_KEYWORD_SETTER:
                        if (!(trait instanceof TraitMethodGetterSetter)) {
                            throw new ParseException("Unxpected trait type", lexer.yyline());
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
                        expected(ParsedSymbol.TYPE_KEYWORD_DISPID, "dispid", lexer);
                        symb = lexer.lex();
                        expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                        tm.disp_id = (int) (long) (Long) symb.value;

                        break;
                    case ParsedSymbol.TYPE_KEYWORD_FUNCTION:
                        if (!(trait instanceof TraitFunction)) {
                            throw new ParseException("Unxpected trait type", lexer.yyline());
                        }
                        break;

                }
                continue;
            }
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_NAME) {
                symb = lexer.lex();
                if (symb.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                    info.name_index = 0;
                } else {
                    expected(symb, ParsedSymbol.TYPE_STRING, "String or null");
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
                    expected(symb, ParsedSymbol.TYPE_STRING, "String or null");
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
                expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                body.max_stack = (int) (long) (Long) symb.value;
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_LOCALCOUNT) {
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                body.max_regs = (int) (long) (Long) symb.value;
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_INITSCOPEDEPTH) {
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                body.init_scope_depth = (int) (long) (Long) symb.value;
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_MAXSCOPEDEPTH) {
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_INTEGER, "Integer");
                body.max_scope_depth = (int) (long) (Long) symb.value;
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_RETURNS) {
                info.ret_type = parseMultiName(constants, lexer);
                continue;
            }

            if (symb.type == ParsedSymbol.TYPE_KEYWORD_FLAG) {
                symb = lexer.lex();
                switch (symb.type) {
                    case ParsedSymbol.TYPE_KEYWORD_EXPLICIT:
                        info.setFlagExplicit();
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
                expected(ParsedSymbol.TYPE_KEYWORD_FROM, "From", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier");
                exceptionsFrom.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TO, "To", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier");
                exceptionsTo.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TARGET, "Target", lexer);
                symb = lexer.lex();
                expected(symb, ParsedSymbol.TYPE_IDENTIFIER, "Identifier");
                exceptionsTargets.add((String) symb.value);
                expected(ParsedSymbol.TYPE_KEYWORD_TYPE, "Type", lexer);
                ABCException ex = new ABCException();
                ex.type_index = parseMultiName(constants, lexer);
                expected(ParsedSymbol.TYPE_KEYWORD_NAME, "Name", lexer);
                ex.name_index = parseMultiName(constants, lexer);
                exceptions.add(ex);
                continue;
            }
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
                    ParsedSymbol exIndex = lexer.lex();
                    if (exIndex.type != ParsedSymbol.TYPE_INTEGER) {
                        throw new ParseException("Index expected", lexer.yyline());
                    }
                    ParsedSymbol exName = lexer.lex();
                    if (exName.type != ParsedSymbol.TYPE_MULTINAME) {
                        throw new ParseException("Multiname expected", lexer.yyline());
                    }
                    ParsedSymbol exType = lexer.lex();
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
                            ParsedSymbol parsedOperand = lexer.lex();
                            switch (def.operands[i]) {
                                case AVM2Code.DAT_MULTINAME_INDEX:
                                    lexer.pushback(parsedOperand);
                                    operandsList.add(parseMultiName(constants, lexer));
                                    /*if (parsedOperand.type == ParsedSymbol.TYPE_MULTINAME) {
                                     operandsList.add(checkMultinameIndex(constants, (int) (long) (Long) parsedOperand.value, lexer.yyline()));
                                     } else {
                                     throw new ParseException("Multiname expected", lexer.yyline());
                                     }*/
                                    break;
                                case AVM2Code.DAT_STRING_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
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
                                            throw new ParseException("String or null expected", lexer.yyline());
                                        }
                                    }
                                    break;
                                case AVM2Code.DAT_INT_INDEX:

                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
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
                                            throw new ParseException("Integer or null expected", lexer.yyline());
                                        }
                                    }
                                    break;
                                case AVM2Code.DAT_UINT_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
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
                                            throw new ParseException("Integer or null expected", lexer.yyline());
                                        }
                                    }
                                    break;
                                case AVM2Code.DAT_DOUBLE_INDEX:
                                    if (parsedOperand.type == ParsedSymbol.TYPE_KEYWORD_NULL) {
                                        operandsList.add(0);
                                    } else {
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
                                            throw new ParseException("Float or null expected", lexer.yyline());
                                        }
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
                                            parsedOperand = lexer.lex();
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
        for (LabelItem li : labelItems) {
            int ind;
            ind = exceptionsFrom.indexOf(li.label);
            if (ind > -1) {
                exceptions.get(ind).start = li.offset;
            }

            ind = exceptionsTo.indexOf(li.label);
            if (ind > -1) {
                exceptions.get(ind).end = li.offset;
            }

            ind = exceptionsTargets.indexOf(li.label);
            if (ind > -1) {
                exceptions.get(ind).target = li.offset;
            }
        }

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
        }
        return code;
    }
}
