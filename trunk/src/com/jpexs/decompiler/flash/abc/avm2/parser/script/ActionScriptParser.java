/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWC;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DefaultXMLNamespace;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXAttrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXElemAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetDescendantsAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitAndAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitOrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitXorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.DeletePropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.DivideAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.EqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.GeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.GtAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.InstanceOfAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LtAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.ModuloAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.MultiplyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.NeqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.RShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.StrictEqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.StrictNeqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.SubtractAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.TypeOfAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.URShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BinaryOp;
import com.jpexs.decompiler.graph.model.BlockItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.ParenthesisItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    private long uniqLast = 0;
    private final boolean debugMode = false;
    private static final String AS3_NAMESPACE = "http://adobe.com/AS3/2006/builtin";

    private ABC abc;
    private List<ABC> otherABCs;

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private List<GraphTargetItem> commands(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        if (s.type == SymbolType.MULTIPLY) {
            return new UnboundedTypeItem();
        } else if (s.type == SymbolType.VOID) {
            return new TypeItem("void");
        } else {
            lexer.pushback(s);
        }

        return name(needsActivation, true, openedNamespaces, null, false, false, variables, importedClasses);
    }

    private GraphTargetItem memberOrCall(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.PARENT_OPEN)) {
            switch (s.type) {
                case DOT:
                    lexer.pushback(s);
                    ret = member(needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
                    break;
                case PARENT_OPEN:
                    ret = new CallAVM2Item(ret, call(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    break;
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem member(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT)) {
            s = lex();
            boolean attr = false;
            if (s.type == SymbolType.ATTRIBUTE) {
                attr = true;
                s = lex();
            }
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            String propName = s.value.toString();
            s = lex();
            GraphTargetItem ns = null;
            if (s.type == SymbolType.NAMESPACE_OP) {
                s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                ns = new UnresolvedAVM2Item(new ArrayList<String>(), importedClasses, false, null, lexer.yyline(), propName, null, openedNamespaces);
                variables.add((UnresolvedAVM2Item) ns);
                propName = s.value.toString();
                s = lex();
            }
            GraphTargetItem index = null;
            if (s.type == SymbolType.BRACKET_OPEN) {
                index = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.BRACKET_CLOSE);
            } else {
                lexer.pushback(s);
            }
            if (ns != null) {
                ret = new NamespacedAVM2Item(index, ns, propName, ret, attr, openedNamespaces, null);
            } else {
                ret = new PropertyAVM2Item(ret, (attr ? "@" : "") + propName, index, abc, otherABCs, openedNamespaces, new ArrayList<MethodBody>());
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem name(Reference<Boolean> needsActivation, boolean typeOnly, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, List<String> importedClasses) throws IOException, ParseException {
        ParsedSymbol s = lex();
        String name = "";
        if (s.type == SymbolType.ATTRIBUTE) {
            name += "@";
            s = lex();
        }
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        name += s.value.toString();
        s = lex();

        while (s.isType(SymbolType.DOT)) {
            name += s.value.toString(); //. or ::            
            s = lex();
            if (s.type == SymbolType.ATTRIBUTE) {
                name += "@";
                s = lex();
                if (s.type == SymbolType.IDENTIFIER) {
                    name += s.value.toString();
                } else {
                    if (s.type != SymbolType.BRACKET_OPEN) {
                        throw new ParseException("Attribute identifier or bracket expected", lexer.yyline());
                    }
                    continue;
                }
            } else {
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.NAMESPACE);
                name += s.value.toString();
            }
            s = lex();
        }
        String nsname = null;
        String nsprop = null;
        if (s.type == SymbolType.NAMESPACE_OP) {
            if (name.contains(".")) {
                nsname = name.substring(name.lastIndexOf('.') + 1);
            } else {
                nsname = name;
            }
            s = lex();
            if (s.type == SymbolType.IDENTIFIER) {
                nsprop = s.value.toString();
            } else {
                nsprop = null;
                lexer.pushback(s);
            }
            if (name.contains(".")) {
                name = name.substring(0, name.lastIndexOf('.'));
            } else {
                name = null;
            }
            s = lex();
        }

        List<String> params = new ArrayList<>();
        if (s.type == SymbolType.TYPENAME) {
            s = lex();
            do {
                String p = "";
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                p = s.value.toString();
                s = lex();
                while (s.type == SymbolType.DOT) {
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    name += "." + s.value.toString();
                    s = lex();
                }
                params.add(p);
            } while (s.type == SymbolType.COMMA);
            expected(s, lexer.yyline(), SymbolType.GREATER_THAN);
            s = lex();
            /*if(!openedNamespaces.contains(AS3vecNs)){
             openedNamespaces.add(AS3vecNs);
             } */
        }
        GraphTargetItem index = null;
        if (s.type == SymbolType.BRACKET_OPEN) {
            index = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
            expectedType(SymbolType.BRACKET_CLOSE);
        } else {
            lexer.pushback(s);
        }

        GraphTargetItem ret = null;
        if (name != null) {
            UnresolvedAVM2Item unr = new UnresolvedAVM2Item(params, importedClasses, typeOnly, null, lexer.yyline(), name, null, openedNamespaces);
            unr.setIndex(index);
            variables.add(unr);
            ret = unr;
        }
        if (nsname != null) {
            boolean attr = nsname.startsWith("@");
            if (attr) {
                nsname = nsname.substring(1);
            }
            UnresolvedAVM2Item ns = new UnresolvedAVM2Item(params, importedClasses, typeOnly, null, lexer.yyline(), nsname, null, openedNamespaces);
            variables.add(ns);
            ret = new NamespacedAVM2Item(index, ns, nsprop, ret, attr, openedNamespaces, null);
        }
        return ret;
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, ParseException {
        boolean found = false;
        for (Object t : expected) {
            if (symb.type == t) {
                found = true;
            }
            if (symb.group == t) {
                found = true;
            }
        }
        if (!found) {
            String expStr = "";
            boolean first = true;
            for (Object e : expected) {
                if (!first) {
                    expStr += " or ";
                }
                expStr += e;
                first = false;
            }
            throw new ParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, ParseException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, ParseException {
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private MethodAVM2Item method(String customAccess, Reference<Boolean> needsActivation, List<String> importedClasses, boolean override, boolean isFinal, GraphTargetItem thisType, List<Integer> openedNamespaces, boolean isStatic, int namespace, boolean withBody, String functionName, boolean isMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        FunctionAVM2Item f = function(needsActivation, importedClasses, namespace, thisType, openedNamespaces, withBody, functionName, isMethod, variables);
        return new MethodAVM2Item(customAccess, f.needsActivation, f.hasRest, f.line, override, isFinal, isStatic, f.namespace, functionName, f.paramTypes, f.paramNames, f.paramValues, f.body, f.subvariables, f.retType);
    }

    private FunctionAVM2Item function(Reference<Boolean> needsActivation, List<String> importedClasses, int namespace, GraphTargetItem thisType, List<Integer> openedNamespaces, boolean withBody, String functionName, boolean isMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        openedNamespaces = new ArrayList<>(openedNamespaces); //local copy
        int line = lexer.yyline();
        ParsedSymbol s;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();
        List<GraphTargetItem> paramTypes = new ArrayList<>();
        List<GraphTargetItem> paramValues = new ArrayList<>();
        boolean hasRest = false;
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            if (s.type == SymbolType.REST) {
                hasRest = true;
                s = lex();
            }
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);

            paramNames.add(s.value.toString());
            s = lex();
            if (!hasRest) {
                if (s.type == SymbolType.COLON) {
                    paramTypes.add(type(needsActivation, importedClasses, openedNamespaces, variables));
                    s = lex();
                } else {
                    paramTypes.add(new UnboundedTypeItem());
                }
                if (s.type == SymbolType.ASSIGN) {
                    paramValues.add(expression(new Reference<Boolean>(false), importedClasses, openedNamespaces, null, isMethod, isMethod, isMethod, variables));
                } else {
                    if (!paramValues.isEmpty()) {
                        throw new ParseException("Some of parameters do not have default values", lexer.yyline());
                    }
                }
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
            if (hasRest) {
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
            }
        }
        s = lex();
        GraphTargetItem retType;
        if (s.type == SymbolType.COLON) {
            retType = type(needsActivation, importedClasses, openedNamespaces, variables);
        } else {
            retType = new UnboundedTypeItem();
            lexer.pushback(s);
        }
        List<GraphTargetItem> body = null;
        List<AssignableAVM2Item> subvariables = new ArrayList<>();
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), "this", null, true, openedNamespaces));
        for (int i = 0; i < paramNames.size(); i++) {
            subvariables.add(new NameAVM2Item(paramTypes.get(i), lexer.yyline(), paramNames.get(i), null, true, openedNamespaces));
        }
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), "arguments", null, true, openedNamespaces));
        int parCnt = subvariables.size();
        Reference<Boolean> needsActivation2 = new Reference<>(false);
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);
            body = commands(needsActivation2, importedClasses, openedNamespaces, new Stack<Loop>(), new HashMap<Loop, String>(), new HashMap<String, Integer>(), true, isMethod, 0, subvariables);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        for (int i = 0; i < parCnt; i++) {
            subvariables.remove(0);
        }
        return new FunctionAVM2Item(needsActivation2.getVal(), namespace, hasRest, line, functionName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

    private GraphTargetItem traits(List<String> importedClasses, int privateNs, int protectedNs, int publicNs, int packageInternalNs, int protectedStaticNs, List<Integer> openedNamespaces, String packageName, String classNameStr, boolean isInterface, List<GraphTargetItem> traits) throws ParseException, IOException, CompilationException {
        ParsedSymbol s;
        GraphTargetItem constr = null;
        List<AssignableAVM2Item> variables = new ArrayList<>();
        TypeItem thisType = new TypeItem("".equals(packageName) ? classNameStr : packageName + "." + classNameStr);
        looptrait:
        while (true) {
            s = lex();
            boolean isStatic = false;
            int namespace = -1;
            boolean isGetter = false;
            boolean isSetter = false;
            boolean isOverride = false;
            boolean isFinal = false;
            boolean isDynamic = false;
            String customAccess = null;
            //TODO: namespace name
            //TODO: final, dynamic
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE, SymbolType.PROTECTED, SymbolType.OVERRIDE, SymbolType.FINAL, SymbolType.DYNAMIC, SymbolType.IDENTIFIER)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        throw new ParseException("Only one final keyword allowed", lexer.yyline());
                    }
                    isFinal = true;
                } else if (s.type == SymbolType.DYNAMIC) {
                    if (isDynamic) {
                        throw new ParseException("Only one dynamic keyword allowed", lexer.yyline());
                    }
                    isDynamic = true;
                } else if (s.type == SymbolType.OVERRIDE) {
                    if (isOverride) {
                        throw new ParseException("Only one override keyword allowed", lexer.yyline());
                    }
                    isOverride = true;
                } else if (s.type == SymbolType.STATIC) {
                    if (isInterface) {
                        throw new ParseException("Interface cannot have static traits", lexer.yyline());
                    }
                    if (classNameStr == null) {
                        throw new ParseException("No static keyword allowed here", lexer.yyline());
                    }
                    if (isStatic) {
                        throw new ParseException("Only one static keyword allowed", lexer.yyline());
                    }
                    isStatic = true;
                } else if (s.type == SymbolType.IDENTIFIER) {
                    customAccess = s.value.toString();
                    namespace = -2;
                } else {
                    if (namespace != -1) {
                        throw new ParseException("Only one access identifier allowed", lexer.yyline());
                    }
                }
                switch (s.type) {
                    case PUBLIC:
                        namespace = publicNs;
                        break;
                    case PRIVATE:
                        namespace = privateNs;
                        break;
                    case PROTECTED:
                        namespace = protectedNs;
                        break;
                }
                s = lex();
            }
            if (namespace == -1) {
                namespace = packageInternalNs;
            }
            if (namespace == protectedNs && isStatic) {
                namespace = protectedStaticNs;
            }
            switch (s.type) {
                case CLASS:
                    if (classNameStr != null) {
                        throw new ParseException("Nested classes not supported", lexer.yyline());
                    }
                    if (isOverride) {
                        throw new ParseException("Override flag not allowed for classes", lexer.yyline());
                    }

                    //GraphTargetItem classTypeStr = type(needsActivation, importedClasses, openedNamespaces, variables);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String classTypeStr = s.value.toString();
                    GraphTargetItem extendsTypeStr = null;
                    s = lex();
                    if (s.type == SymbolType.EXTENDS) {
                        extendsTypeStr = type(new Reference<Boolean>(false), importedClasses, openedNamespaces, variables);
                        s = lex();
                    }
                    List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                    if (s.type == SymbolType.IMPLEMENTS) {
                        do {
                            GraphTargetItem implementsTypeStr = type(new Reference<Boolean>(false), importedClasses, openedNamespaces, variables);
                            implementsTypeStrs.add(implementsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    if (customAccess != null) {
                        throw new ParseException("Class cannot have custom namespace", lexer.yyline());
                    }
                    traits.add((classTraits(packageInternalNs, importedClasses, privateNs, isDynamic, isFinal, openedNamespaces, packageName, namespace, false, classTypeStr, extendsTypeStr, implementsTypeStrs, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                case INTERFACE:
                    if (classNameStr != null) {
                        throw new ParseException("Nested interfaces not supported", lexer.yyline());
                    }
                    if (isOverride) {
                        throw new ParseException("Override flag not allowed for interfaces", lexer.yyline());
                    }
                    if (isFinal) {
                        throw new ParseException("Final flag not allowed for interfaces", lexer.yyline());
                    }
                    if (isDynamic) {
                        throw new ParseException("Dynamic flag not allowed for interfaces", lexer.yyline());
                    }
                    //GraphTargetItem interfaceTypeStr = type(needsActivation, importedClasses, openedNamespaces, variables);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String intTypeStr = s.value.toString();
                    s = lex();
                    List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                    if (s.type == SymbolType.EXTENDS) {
                        do {
                            GraphTargetItem intExtendsTypeStr = type(new Reference<Boolean>(false), importedClasses, openedNamespaces, variables);
                            intExtendsTypeStrs.add(intExtendsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    if (customAccess != null) {
                        throw new ParseException("Interface cannot have custom namespace", lexer.yyline());
                    }
                    traits.add((classTraits(packageInternalNs, importedClasses, privateNs, false, isFinal, openedNamespaces, packageName, namespace, true, intTypeStr, null, intExtendsTypeStrs, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;

                case FUNCTION:

                    if (isDynamic) {
                        throw new ParseException("Dynamic flag not allowed for methods", lexer.yyline());
                    }
                    s = lex();
                    if (s.type == SymbolType.GET) {
                        if (classNameStr == null) {
                            throw new ParseException("No get keyword allowed here", lexer.yyline());
                        }
                        isGetter = true;
                        s = lex();
                    } else if (s.type == SymbolType.SET) {
                        if (classNameStr == null) {
                            throw new ParseException("No set keyword allowed here", lexer.yyline());
                        }
                        isSetter = true;
                        s = lex();
                    }

                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String fname = s.value.toString();
                    if (classNameStr != null && fname.equals(classNameStr)) { //constructor
                        if (isStatic) {
                            throw new ParseException("Constructor cannot be static", lexer.yyline());
                        }
                        if (isStatic) {
                            throw new ParseException("Constructor cannot be static", lexer.yyline());
                        }
                        if (isOverride) {
                            throw new ParseException("Override flag not allowed for constructor", lexer.yyline());
                        }
                        if (isFinal) {
                            throw new ParseException("Final flag not allowed for constructor", lexer.yyline());
                        }
                        constr = (method(customAccess, new Reference<Boolean>(false), importedClasses, false, false, thisType, openedNamespaces, false, namespace, !isInterface, "", true, variables));
                    } else {
                        if (isStatic) {
                            GraphTargetItem t;
                            MethodAVM2Item ft = method(customAccess, new Reference<Boolean>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, namespace, !isInterface, fname, true, variables);
                            traits.add(ft);
                            if (isGetter || isSetter) {
                                throw new ParseException("Getter or Setter cannot be static", lexer.yyline());
                            }
                        } else {
                            MethodAVM2Item ft = method(customAccess, new Reference<Boolean>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, namespace, !isInterface, fname, true, variables);
                            GraphTargetItem t;
                            if (isGetter) {
                                if (!ft.paramTypes.isEmpty()) {
                                    throw new ParseException("Getter can't have any parameters", lexer.yyline());
                                }
                                GetterAVM2Item g = new GetterAVM2Item(customAccess, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.namespace, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                                t = g;
                            } else if (isSetter) {
                                if (ft.paramTypes.size() != 1) {
                                    throw new ParseException("Getter must have exactly one parameter", lexer.yyline());
                                }
                                SetterAVM2Item st = new SetterAVM2Item(customAccess, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.namespace, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                                t = st;
                            } else {
                                t = ft;
                            }

                            traits.add(ft);
                        }
                    }
                    //}
                    break;
                case NAMESPACE:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String nname = s.value.toString();
                    String nval = "";
                    s = lex();

                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        nval = s.value.toString();
                        s = lex();
                    } else {
                        nval = (packageName.isEmpty() ? classNameStr : packageName + ":" + classNameStr) + "/" + nname;
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    ConstAVM2Item ns = new ConstAVM2Item(customAccess, true, namespace, nname, new TypeItem("Namespace"), new StringAVM2Item(null, nval));
                    traits.add(ns);
                    break;
                case CONST:
                case VAR:
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isOverride) {
                        throw new ParseException("Override flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }
                    if (isFinal) {
                        throw new ParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }
                    if (isDynamic) {
                        throw new ParseException("Dynamic flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();
                    GraphTargetItem type = null;
                    if (s.type == SymbolType.COLON) {
                        type = type(new Reference<Boolean>(false), importedClasses, openedNamespaces, variables);
                        s = lex();
                    }

                    GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        value = expression(new Reference<Boolean>(false), importedClasses, openedNamespaces, new HashMap<String, Integer>(), false, false, true, variables);
                        s = lex();
                    }
                    GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(customAccess, isStatic, namespace, vcname, type, value);
                    } else {
                        tar = new SlotAVM2Item(customAccess, isStatic, namespace, vcname, type, value);
                    }
                    traits.add(tar);
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);
                    break looptrait;

            }
        }
        return constr;
    }

    private GraphTargetItem classTraits(int packageInternalNs, List<String> importedClasses, int privateNs, boolean isDynamic, boolean isFinal, List<Integer> openedNamespaces, String packageName, int namespace, boolean isInterface, String nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<AssignableAVM2Item> variables) throws IOException, ParseException, CompilationException {

        GraphTargetItem ret = null;

        ParsedSymbol s = null;
        List<GraphTargetItem> traits = new ArrayList<>();

        String classNameStr = nameStr;

        openedNamespaces = new ArrayList<>(openedNamespaces);
        //openedNamespacesKinds = new ArrayList<>(openedNamespacesKinds);

        //int privateNs = 0;
        int protectedNs = 0;
        int publicNs = namespace;
        int protectedStaticNs = 0;

        openedNamespaces.add(protectedNs = abc.constants.addNamespace(new Namespace(Namespace.KIND_PROTECTED, abc.constants.getStringId(packageName.isEmpty() ? classNameStr : packageName + ":" + classNameStr, true))));
        openedNamespaces.add(protectedStaticNs = abc.constants.addNamespace(new Namespace(Namespace.KIND_STATIC_PROTECTED, abc.constants.getStringId(packageName.isEmpty() ? classNameStr : packageName + ":" + classNameStr, true))));

        List<Integer> indices = new ArrayList<>();
        List<String> names = new ArrayList<>();
        List<String> namespaces = new ArrayList<>();
        AVM2SourceGenerator.parentNamesAddNames(abc, otherABCs, AVM2SourceGenerator.resolveType(((TypeItem) ((UnresolvedAVM2Item) extendsStr).resolve(new ArrayList<GraphTargetItem>(), new ArrayList<String>(), abc, otherABCs, new ArrayList<MethodBody>(), new ArrayList<AssignableAVM2Item>())), abc), indices, names, namespaces);
        for (int i = 0; i < names.size(); i++) {
            if (namespaces.get(i).isEmpty()) {
                continue;
            }
            openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_STATIC_PROTECTED, abc.constants.getStringId(namespaces.get(i) + ":" + names.get(i), true)), 0, true));
        }

        GraphTargetItem constr = traits(importedClasses, privateNs, protectedNs, publicNs, packageInternalNs, protectedStaticNs, openedNamespaces, packageName, classNameStr, isInterface, traits);

        if (isInterface) {
            return new InterfaceAVM2Item(openedNamespaces, isFinal, namespace, classNameStr, implementsStr, traits);
        } else {
            return new ClassAVM2Item(openedNamespaces, protectedNs, isDynamic, isFinal, namespace, classNameStr, extendsStr, implementsStr, constr, traits);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            /*case INT:
             expectedType(SymbolType.PARENT_OPEN);                
             ret = new ToIntegerAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             break;
             case NUMBER_OP:
             s = lex();
             if (s.type == SymbolType.DOT) {
             VariableAVM2Item vi = new VariableAVM2Item(s.value.toString(), null, false);
             variables.add(vi);
             ret = memberOrCall(vi, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToNumberAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             }
             break;
             case STRING_OP:
             ParsedSymbol sop = s;
             s = lex();
             if (s.type == SymbolType.DOT) {
             lexer.pushback(s);
             VariableAVM2Item vi2 = new VariableAVM2Item(sop.value.toString(), null, false);
             variables.add(vi2);
             ret = memberOrCall(vi2, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToStringAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             ret = memberOrCall(ret, registerVars, inFunction, inMethod, variables);
             }
             break;*/
            default:
                return null;
        }
        //return ret;
    }

    private GraphTargetItem add(Object a) {
        if (a instanceof List) {
            List l = (List) a;
            if (l.isEmpty()) {
                return null;
            }
            GraphTargetItem o = add(l.get(0));
            for (int i = 1; i < l.size(); i++) {
                o = add(o, l.get(i));
            }
            return o;
        }
        if (a instanceof StringBuilder) {
            if (((StringBuilder) a).length() == 0) {
                return null;
            }
            GraphTargetItem ret = new StringAVM2Item(null, a.toString());
            ((StringBuilder) a).setLength(0);
            return ret;
        }
        if (a instanceof String) {
            return new StringAVM2Item(null, (String) a);
        }
        if (a instanceof GraphTargetItem) {
            return (GraphTargetItem) a;
        }
        return null;
    }

    private GraphTargetItem add(Object a, Object b) {
        GraphTargetItem ta = add(a);
        GraphTargetItem tb = add(b);
        if (ta == null && tb == null) {
            return null;
        }
        if (ta == null) {
            return tb;
        }
        if (tb == null) {
            return ta;
        }
        return new AddAVM2Item(null, ta, tb);
    }

    private void addS(List<GraphTargetItem> rets, StringBuilder sb) {
        if (sb.length() > 0) {
            if (!rets.isEmpty() && (rets.get(rets.size() - 1) instanceof StringAVM2Item)) {
                ((StringAVM2Item) rets.get(rets.size() - 1)).value += sb.toString();
            } else {
                rets.add(new StringAVM2Item(null, sb.toString()));
            }
            sb.setLength(0);
        }
    }

    private List<GraphTargetItem> xmltag(Reference<Boolean> usesVars, List<String> openedTags, Reference<Integer> closedVarTags, Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = null;
        List<GraphTargetItem> rets = new ArrayList<>();
        //GraphTargetItem ret = null;
        StringBuilder sb = new StringBuilder();
        loop:
        do {
            s = lex();
            List<String> sub = new ArrayList<>();
            Reference<Integer> subclose = new Reference<>(0);
            Reference<Boolean> subusesvars = new Reference<>(false);
            switch (s.type) {
                case XML_ATTRNAMEVAR_BEGIN: //add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_ATTRVALVAR_BEGIN: //esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    addS(rets, sb);
                    rets.add(new EscapeXAttrAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_INSTRATTRNAMEVAR_BEGIN: //add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_INSTRATTRVALVAR_BEGIN: //esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    addS(rets, sb);
                    rets.add(new EscapeXAttrAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_VAR_BEGIN: //esc_xelem
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(new EscapeXElemAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_FINISHVARTAG_BEGIN: //add
                    usesVars.setVal(true);
                    closedVarTags.setVal(closedVarTags.getVal() + 1);
                    sb.append("</");
                    addS(rets, sb);

                    rets.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.GREATER_THAN);
                    sb.append(">");
                    addS(rets, sb);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_STARTVARTAG_BEGIN: //add                    
                    //openedTags.add("*");

                    //ret = add(ret, );
                    GraphTargetItem ex = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    sub.add("*");
                    sb.append("<");
                    addS(rets, sb);
                    rets.add(ex);
                    rets.addAll(xmltag(subusesvars, sub, subclose, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    closedVarTags.setVal(subclose.getVal() + subclose.getVal());
                    break;
                case XML_INSTRVARTAG_BEGIN: //add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    sb.append("<?");
                    addS(rets, sb);
                    rets.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLINSTROPENTAG);
                    break;
                case XML_STARTTAG_BEGIN:
                    sub.add(s.value.toString().trim().substring(1)); //remove < from beginning
                    List<GraphTargetItem> st = xmltag(subusesvars, sub, closedVarTags, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
                    sb.append(s.value.toString());
                    addS(rets, sb);
                    rets.addAll(st);
                    closedVarTags.setVal(subclose.getVal() + subclose.getVal());
                    break;
                /*case XML_STARTTAG_END:                    
                 sb.append(s.value.toString());
                 ret = addstr(ret,sb);
                 break;*/
                case XML_FINISHTAG:
                    String tname = s.value.toString().substring(2, s.value.toString().length() - 1).trim();
                    if (openedTags.contains(tname)) {
                        openedTags.remove(tname);
                    } else if (openedTags.contains("*")) {
                        openedTags.remove("*");
                    } else {
                        throw new ParseException("XML : Closing unopened tag", lexer.yyline());
                    }
                    sb.append(s.value.toString());
                    break;
                case XML_STARTFINISHTAG_END:
                    openedTags.remove(openedTags.size() - 1); //close last tag
                    sb.append(s.value.toString());
                    break;
                case EOF:
                    throw new ParseException("End of file before XML finish", lexer.yyline());
                default:
                    sb.append(s.value.toString());
                    break;
            }
        } while (!(openedTags.isEmpty() || closedVarTags.getVal() >= openedTags.size()));
        addS(rets, sb);
        return rets;
    }

    private GraphTargetItem xml(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        List<String> openedTags = new ArrayList<>();
        int closedVarTags = 0;

        GraphTargetItem ret = add(xmltag(new Reference<Boolean>(false), openedTags, new Reference<Integer>(closedVarTags), needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
        ret = new XMLAVM2Item(ret);
        lexer.yybegin(ActionScriptLexer.YYINITIAL);
        //TODO: Order of additions as in official compiler
        return ret;
    }

    private GraphTargetItem command(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        LexBufferer buf = new LexBufferer();
        lexer.addListener(buf);
        GraphTargetItem ret = null;
        if (debugMode) {
            System.out.println("command:");
        }
        ParsedSymbol s = lex();
        if (s.type == SymbolType.EOF) {
            return null;
        }
        String loopLabel = null;

        if (s.type == SymbolType.IDENTIFIER) {
            ParsedSymbol sc = lex();
            if (sc.type == SymbolType.COLON) {
                loopLabel = s.value.toString();
                s = lex();
            } else {
                lexer.pushback(sc);
            }
        }

        if (s.type == SymbolType.DEFAULT) {
            ParsedSymbol sx = lex();
            if (sx.type != SymbolType.IDENTIFIER) {
                lexer.pushback(sx);
            } else {
                if (!sx.value.equals("xml")) {
                    lexer.pushback(sx);
                } else {
                    expectedType(SymbolType.NAMESPACE);
                    expectedType(SymbolType.ASSIGN);
                    GraphTargetItem ns = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    ret = new DefaultXMLNamespace(null, ns);
                    //TODO: use dxns for attribute namespaces instead of dxnslate
                }
            }
        }
        if (ret == null) {
            switch (s.type) {
                case USE:
                    expectedType(SymbolType.NAMESPACE);
                    GraphTargetItem ns = type(needsActivation, importedClasses, openedNamespaces, variables);
                    openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE /*FIXME?*/, abc.constants.getStringId(ns.toString(), true)), 0, true));
                    break;
                case WITH:
                    needsActivation.setVal(true);
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem wvar = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//(name(false, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    if (!isNameOrProp(wvar)) {
                        throw new ParseException("Not a property or name", lexer.yyline());
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    List<AssignableAVM2Item> withVars = new ArrayList<>();
                    List<GraphTargetItem> wcmd = commands(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, withVars);
                    variables.addAll(withVars);
                    for (AssignableAVM2Item a : withVars) {
                        if (a instanceof UnresolvedAVM2Item) {
                            UnresolvedAVM2Item ua = (UnresolvedAVM2Item) a;
                            ua.scopeStack.add(0, wvar);
                        }
                    }
                    expectedType(SymbolType.CURLY_CLOSE);
                    ret = new WithAVM2Item(null, wvar, wcmd);
                    ((WithAVM2Item) ret).subvariables = withVars;
                    break;
                /*case DELETE:
                 GraphTargetItem varDel = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                 if(!isNameOrProp(varDel)){
                 throw new ParseException("Not a property or name", lexer.yyline());
                 }
                 if (varDel instanceof GetPropertyAVM2Item) {
                 GetPropertyAVM2Item gm = (GetPropertyAVM2Item) varDel;
                 ret = new DeletePropertyAVM2Item(null, gm.object, gm.propertyName);
                 } else if (varDel instanceof NameAVM2Item) {
                 variables.remove(varDel);
                 ret = new DeletePropertyAVM2Item(null, null, (NameAVM2Item) varDel);
                 } else {
                 throw new ParseException("Not a property", lexer.yyline());
                 }
                 break;*/
                case FUNCTION:
                    s = lexer.lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    needsActivation.setVal(true);
                    ret = (function(needsActivation, importedClasses, 0/*?*/, TypeItem.UNBOUNDED, openedNamespaces, true, s.value.toString(), false, variables));
                    break;
                case VAR:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String varIdentifier = s.value.toString();
                    s = lex();
                    GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        type = type(needsActivation, importedClasses, openedNamespaces, variables);
                        s = lex();
                    } else {
                        type = new UnboundedTypeItem();
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        GraphTargetItem varval = (expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                        ret = new NameAVM2Item(type, lexer.yyline(), varIdentifier, varval, true, openedNamespaces);
                        variables.add((NameAVM2Item) ret);
                    } else {
                        ret = new NameAVM2Item(type, lexer.yyline(), varIdentifier, null, true, openedNamespaces);
                        variables.add((NameAVM2Item) ret);
                        lexer.pushback(s);
                    }
                    break;
                case CURLY_OPEN:
                    ret = new BlockItem(null, commands(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                /*case INCREMENT: //preincrement
                 case DECREMENT: //predecrement
                 GraphTargetItem varincdec = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                 if(!isNameOrProp(varincdec)){
                 throw new ParseException("Not a property or name", lexer.yyline());
                 }
                 if (s.type == SymbolType.INCREMENT) {
                 ret = new PreIncrementAVM2Item(null, varincdec);
                 } else if (s.type == SymbolType.DECREMENT) {
                 ret = new PreDecrementAVM2Item(null, varincdec);
                 }
                 break;*/
                case SUPER: //constructor call
                    ParsedSymbol ss2 = lex();
                    if (ss2.type == SymbolType.PARENT_OPEN) {
                        List<GraphTargetItem> args = call(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
                        ret = new ConstructSuperAVM2Item(null, new LocalRegAVM2Item(null, 0, null), args);
                    } else {//no costructor call, but it could be calling parent methods... => handle in expression
                        lexer.pushback(ss2);
                        lexer.pushback(s);
                    }
                    break;
                case IF:
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem ifExpr = (expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    GraphTargetItem onTrue = command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                    List<GraphTargetItem> onTrueList = new ArrayList<>();
                    onTrueList.add(onTrue);
                    s = lex();
                    List<GraphTargetItem> onFalseList = null;
                    if (s.type == SymbolType.ELSE) {
                        onFalseList = new ArrayList<>();
                        onFalseList.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    } else {
                        lexer.pushback(s);
                    }
                    ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                    break;
                case WHILE:
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> whileExpr = new ArrayList<>();
                    whileExpr.add(commaExpression(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> whileBody = new ArrayList<>();
                    Loop wloop = new Loop(uniqId(), null, null);
                    if (loopLabel != null) {
                        loopLabels.put(wloop, loopLabel);
                    }
                    loops.push(wloop);
                    whileBody.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    ret = new WhileItem(null, wloop, whileExpr, whileBody);
                    break;
                case DO:
                    List<GraphTargetItem> doBody = new ArrayList<>();
                    Loop dloop = new Loop(uniqId(), null, null);
                    loops.push(dloop);
                    if (loopLabel != null) {
                        loopLabels.put(dloop, loopLabel);
                    }
                    doBody.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    expectedType(SymbolType.WHILE);
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> doExpr = new ArrayList<>();
                    doExpr.add(commaExpression(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    ret = new DoWhileItem(null, dloop, doBody, doExpr);
                    break;
                case FOR:
                    s = lex();
                    boolean forin = false;
                    boolean each = false;
                    GraphTargetItem collection = null;
                    if (s.type == SymbolType.EACH) {
                        each = true;
                        forin = true;
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    GraphTargetItem firstCommand = command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, false, variables);
                    if (firstCommand instanceof NameAVM2Item) {
                        NameAVM2Item nai = (NameAVM2Item) firstCommand;
                        if (nai.isDefinition() && nai.getAssignedValue() == null) {
                            firstCommand = expressionRemainder(needsActivation, openedNamespaces, firstCommand, registerVars, inFunction, inMethod, true, variables, importedClasses);
                        }
                    }
                    InAVM2Item inexpr = null;
                    if (firstCommand instanceof InAVM2Item) {
                        forin = true;
                        inexpr = (InAVM2Item) firstCommand;
                    } else {
                        if (forin) {
                            throw new ParseException("In expression required", lexer.yyline());
                        }
                    }

                    Loop floop = new Loop(uniqId(), null, null);
                    loops.push(floop);
                    if (loopLabel != null) {
                        loopLabels.put(floop, loopLabel);
                    }
                    List<GraphTargetItem> forFinalCommands = new ArrayList<>();
                    GraphTargetItem forExpr = null;
                    List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    if (!forin) {
                        //GraphTargetItem firstCommand = command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                        if (firstCommand != null) { //can be empty command
                            forFirstCommands.add(firstCommand);
                        }
                        forExpr = (expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                        expectedType(SymbolType.SEMICOLON);
                        forFinalCommands.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> forBody = new ArrayList<>();
                    forBody.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables));
                    if (forin) {
                        if (each) {
                            ret = new ForEachInAVM2Item(null, floop, inexpr, forBody);
                        } else {

                            ret = new ForInAVM2Item(null, floop, inexpr, forBody);
                        }
                    } else {
                        ret = new ForItem(null, floop, forFirstCommands, forExpr, forFinalCommands, forBody);
                    }
                    break;
                case SWITCH:
                    Loop sloop = new Loop(-uniqId(), null, null); //negative id marks switch = no continue
                    loops.push(sloop);
                    if (loopLabel != null) {
                        loopLabels.put(sloop, loopLabel);
                    }
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem switchExpr = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    s = lex();
                    //ret.addAll(switchExpr);
                    int exprReg = 0;
                    for (int i = 0; i < 256; i++) {
                        if (!registerVars.containsValue(i)) {
                            registerVars.put("__switch" + uniqId(), i);
                            exprReg = i;
                            break;
                        }
                    }
                    List<List<ActionIf>> caseIfs = new ArrayList<>();
                    List<List<GraphTargetItem>> caseCmds = new ArrayList<>();
                    List<GraphTargetItem> caseExprsAll = new ArrayList<>();
                    List<Integer> valueMapping = new ArrayList<>();
                    int pos = 0;
                    while (s.type == SymbolType.CASE) {
                        List<GraphTargetItem> caseExprs = new ArrayList<>();
                        while (s.type == SymbolType.CASE) {
                            GraphTargetItem curCaseExpr = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                            caseExprs.add(curCaseExpr);
                            expectedType(SymbolType.COLON);
                            s = lex();
                            caseExprsAll.add(curCaseExpr);
                            valueMapping.add(pos);
                        }
                        pos++;
                        lexer.pushback(s);
                        List<GraphTargetItem> caseCmd = commands(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                        caseCmds.add(caseCmd);
                        s = lex();
                    }
                    List<GraphTargetItem> defCmd = new ArrayList<>();
                    if (s.type == SymbolType.DEFAULT) {
                        expectedType(SymbolType.COLON);
                        defCmd = commands(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                        s = lexer.lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    ret = new SwitchItem(null, sloop, switchExpr, caseExprsAll, caseCmds, defCmd, valueMapping);
                    break;
                case BREAK:
                    s = lex();
                    long bloopId = 0;
                    if (loops.isEmpty()) {
                        throw new ParseException("No loop to break", lexer.yyline());
                    }
                    if (s.type == SymbolType.IDENTIFIER) {
                        String breakLabel = s.value.toString();
                        for (Loop l : loops) {
                            if (breakLabel.equals(loopLabels.get(l))) {
                                bloopId = l.id;
                                break;
                            }
                        }
                        if (bloopId == 0) {
                            throw new ParseException("Identifier of loop expected", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                        bloopId = loops.peek().id;
                    }
                    ret = new BreakItem(null, bloopId);
                    break;
                case CONTINUE:
                    s = lex();
                    long cloopId = 0;
                    if (loops.isEmpty()) {
                        throw new ParseException("No loop to continue", lexer.yyline());
                    }
                    if (s.type == SymbolType.IDENTIFIER) {
                        String continueLabel = s.value.toString();
                        for (Loop l : loops) {
                            if (l.id < 0) { //negative id marks switch => no continue
                                continue;
                            }
                            if (continueLabel.equals(loopLabels.get(l))) {
                                cloopId = l.id;
                                break;
                            }
                        }
                        if (cloopId == -1) {
                            throw new ParseException("Identifier of loop expected", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                        for (int i = loops.size() - 1; i >= 0; i--) {
                            if (loops.get(i).id >= 0) {//no switches
                                cloopId = loops.peek().id;
                                break;
                            }
                        }
                        if (cloopId <= 0) {
                            throw new ParseException("No loop to continue", lexer.yyline());
                        }
                    }
                    //TODO: handle switch
                    ret = new ContinueItem(null, cloopId);
                    break;
                case RETURN:
                    GraphTargetItem retexpr = expression(needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables);
                    if (retexpr == null) {
                        ret = new ReturnVoidAVM2Item(null);
                    } else {
                        ret = new ReturnValueAVM2Item(null, retexpr);
                    }
                    break;
                case TRY:
                    needsActivation.setVal(true);
                    List<GraphTargetItem> tryCommands = new ArrayList<>();
                    tryCommands.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    s = lex();
                    boolean found = false;
                    List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
                    List<NameAVM2Item> catchExceptions = new ArrayList<>();
                    int varCnt = variables.size();
                    List<List<AssignableAVM2Item>> catchesVars = new ArrayList<>();
                    while (s.type == SymbolType.CATCH) {
                        expectedType(SymbolType.PARENT_OPEN);
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);

                        String enamestr = s.value.toString();
                        expectedType(SymbolType.COLON);
                        GraphTargetItem etype = type(needsActivation, importedClasses, openedNamespaces, variables);
                        NameAVM2Item e = new NameAVM2Item(etype, lexer.yyline(), enamestr, new ExceptionAVM2Item(null)/*?*/, true/*?*/, openedNamespaces);
                        variables.add(e);
                        catchExceptions.add(e);
                        e.setSlotNumber(1);
                        e.setSlotScope(Integer.MAX_VALUE); //will be changed later
                        expectedType(SymbolType.PARENT_CLOSE);
                        List<GraphTargetItem> cc = new ArrayList<>();
                        List<AssignableAVM2Item> catchVars = new ArrayList<>();
                        cc.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, catchVars));
                        catchesVars.add(catchVars);
                        variables.addAll(catchVars);

                        for (AssignableAVM2Item a : catchVars) {
                            if (a instanceof UnresolvedAVM2Item) {
                                UnresolvedAVM2Item ui = (UnresolvedAVM2Item) a;
                                if (ui.getVariableName().equals(e.getVariableName())) {
                                    try {
                                        ui.resolve(new ArrayList<GraphTargetItem>(), new ArrayList<String>(), abc, otherABCs, new ArrayList<MethodBody>(), variables);
                                    } catch (CompilationException ex) {
                                        //ignore
                                    }
                                    ui.setSlotNumber(e.getSlotNumber());
                                    ui.setSlotScope(e.getSlotScope());
                                }

                            }
                        }

                        catchCommands.add(cc);
                        s = lex();
                        found = true;
                    }
                    //TODO:
                    for (int i = varCnt; i < variables.size(); i++) {
                        AssignableAVM2Item av = variables.get(i);
                        if (av instanceof UnresolvedAVM2Item) {
                            UnresolvedAVM2Item ui = (UnresolvedAVM2Item) av;
                            for (NameAVM2Item e : catchExceptions) {
                                if (ui.getVariableName().equals(e.getVariableName())) {
                                    try {
                                        ui.resolve(new ArrayList<GraphTargetItem>(), new ArrayList<String>(), abc, otherABCs, new ArrayList<MethodBody>(), variables);
                                    } catch (CompilationException ex) {
                                        //ignore
                                    }
                                    ui.setSlotNumber(e.getSlotNumber());
                                    ui.setSlotScope(e.getSlotScope());
                                }
                            }
                        }
                    }

                    List<GraphTargetItem> finallyCommands = null;
                    if (s.type == SymbolType.FINALLY) {
                        finallyCommands = new ArrayList<>();
                        finallyCommands.add(command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                        found = true;
                        s = lex();
                    }
                    if (!found) {
                        expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                    }
                    lexer.pushback(s);
                    TryAVM2Item tai = new TryAVM2Item(tryCommands, null, catchCommands, finallyCommands);
                    tai.catchVariables = catchesVars;
                    tai.catchExceptions2 = catchExceptions;
                    ret = tai;
                    break;
                case THROW:
                    ret = new ThrowAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    break;
                default:
                    GraphTargetItem valcmd = expressionCommands(s, registerVars, inFunction, inMethod, forinlevel, variables);
                    if (valcmd != null) {
                        ret = valcmd;
                        break;
                    }
                    if (s.type == SymbolType.SEMICOLON) {
                        return null;
                    }
                    lexer.pushback(s);
                    ret = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    if (debugMode) {
                        System.out.println("/command");
                    }
            }
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (ret == null) {  //can be popped expression            
            buf.pushAllBack(lexer);
            ret = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        return expression(needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, allowRemainder, variables);
    }

    private GraphTargetItem fixPrecedence(GraphTargetItem expr) {
        GraphTargetItem ret = expr;
        if (expr instanceof BinaryOp) {
            BinaryOp bo = (BinaryOp) expr;
            GraphTargetItem left = bo.getLeftSide();
            //GraphTargetItem right=bo.getRightSide();
            if (left.getPrecedence() > bo.getPrecedence()) {
                if (left instanceof BinaryOp) {
                    BinaryOp leftBo = (BinaryOp) left;
                    bo.setLeftSide(leftBo.getRightSide());
                    leftBo.setRightSide(expr);
                    return left;
                }
            }
        }
        return ret;
    }

    private GraphTargetItem expressionRemainder(Reference<Boolean> needsActivation, List<Integer> openedNamespaces, GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, List<String> importedClasses) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        switch (s.type) {
            case DESCENDANTS:
                ParsedSymbol d = lex();
                expected(d, lexer.yyline(), SymbolType.IDENTIFIER);
                ret = new GetDescendantsAVM2Item(expr, d.value.toString(), openedNamespaces);
                allowRemainder = true;
                break;
            case FILTER:
                needsActivation.setVal(true);
                ret = new XMLFilterAVM2Item(expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables), openedNamespaces);
                expectedType(SymbolType.PARENT_CLOSE);
                allowRemainder = true;
                break;
            /*case NAMESPACE_OP:
             s = lex();
             if (s.type == SymbolType.BRACKET_OPEN) {
             GraphTargetItem index = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
             NameAVM2Item name = new NameAVM2Item(new UnboundedTypeItem(), lexer.yyline(), null, null, false, openedNamespaces);
             name.setIndex(index);
             name.setNs(expr);
             ret = name;
             expectedType(SymbolType.BRACKET_CLOSE);
             } else {
             lexer.pushback(s);
             GraphTargetItem name = name(needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses);
             if (name instanceof UnresolvedAVM2Item) {
             ((UnresolvedAVM2Item) name).setNs(expr);
             //((UnresolvedAVM2Item) name).unresolved = false;
             //TODO
             } else {
             throw new ParseException("Not a property name", lexer.yyline());
             }
             ret = name;
             }
             break;*/
            case IN:
                ret = new InAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                break;
            case TERNAR:
                GraphTargetItem terOnTrue = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.COLON);
                GraphTargetItem terOnFalse = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                ret = new TernarOpItem(null, expr, terOnTrue, terOnFalse);
                break;
            case SHIFT_LEFT:
                ret = new LShiftAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case SHIFT_RIGHT:
                ret = new RShiftAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case USHIFT_RIGHT:
                ret = new URShiftAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case BITAND:
                ret = new BitAndAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case BITOR:
                ret = new BitOrAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case DIVIDE:
                ret = new DivideAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case MODULO:
                ret = new ModuloAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case EQUALS:
                ret = new EqAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case STRICT_EQUALS:
                ret = new StrictEqAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case NOT_EQUAL:
                ret = new NeqAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case STRICT_NOT_EQUAL:
                ret = new StrictNeqAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case LOWER_THAN:
                ret = new LtAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case LOWER_EQUAL:
                ret = new LeAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_THAN:
            case XML_STARTVARTAG_BEGIN:
            case XML_STARTTAG_BEGIN:
                if (s.type != SymbolType.GREATER_THAN) {
                    lexer.yypushbackstr(s.value.toString().substring(1)); //parse again as GREATER_THAN
                }
                ret = new GtAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_EQUAL:
                ret = new GeAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case AND:
                ret = new AndItem(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case OR:
                ret = new OrItem(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case MINUS:
                ret = new SubtractAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case MULTIPLY:
                ret = new MultiplyAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case PLUS:
                ret = new AddAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case XOR:
                ret = new BitXorAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case AS:
                //TODO
                break;
            case INSTANCEOF:
                ret = new InstanceOfAVM2Item(null, expr, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, false, variables));
                break;
            case IS:

                break;

            case ASSIGN:
            case ASSIGN_BITAND:
            case ASSIGN_BITOR:
            case ASSIGN_DIVIDE:
            case ASSIGN_MINUS:
            case ASSIGN_MODULO:
            case ASSIGN_MULTIPLY:
            case ASSIGN_PLUS:
            case ASSIGN_SHIFT_LEFT:
            case ASSIGN_SHIFT_RIGHT:
            case ASSIGN_USHIFT_RIGHT:
            case ASSIGN_XOR:
                GraphTargetItem assigned = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                switch (s.type) {
                    case ASSIGN:
                        //assigned = assigned;
                        break;
                    case ASSIGN_BITAND:
                        assigned = new BitAndAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_BITOR:
                        assigned = new BitOrAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_DIVIDE:
                        assigned = new DivideAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_MINUS:
                        assigned = new SubtractAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_MODULO:
                        assigned = new ModuloAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_MULTIPLY:
                        assigned = new MultiplyAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_PLUS:
                        assigned = new AddAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_SHIFT_LEFT:
                        assigned = new LShiftAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_SHIFT_RIGHT:
                        assigned = new RShiftAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_USHIFT_RIGHT:
                        assigned = new URShiftAVM2Item(null, expr, assigned);
                        break;
                    case ASSIGN_XOR:
                        assigned = new BitXorAVM2Item(null, expr, assigned);
                        break;
                }

                if (!(expr instanceof AssignableAVM2Item)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                AssignableAVM2Item as = ((AssignableAVM2Item) expr).copy();
                if ((as instanceof UnresolvedAVM2Item) || (as instanceof NameAVM2Item)) {
                    variables.add(as);
                }
                as.setAssignedValue(assigned);
                if (expr instanceof NameAVM2Item) {
                    ((NameAVM2Item) expr).setDefinition(false);
                }
                ret = as;
                break;
            case INCREMENT: //postincrement
                if (!(expr instanceof AssignableAVM2Item)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostIncrementAVM2Item(null, expr);
                break;
            case DECREMENT: //postdecrement
                if (!(expr instanceof AssignableAVM2Item)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostDecrementAVM2Item(null, expr);
                break;
            case DOT: //member
            case BRACKET_OPEN: //member
            case PARENT_OPEN: //function call
                lexer.pushback(s);
                ret = memberOrCall(needsActivation, importedClasses, openedNamespaces, expr, registerVars, inFunction, inMethod, variables);
                break;

            default:
                lexer.pushback(s);
                if (expr instanceof ParenthesisItem) {
                    if (isType(((ParenthesisItem) expr).value)) {
                        GraphTargetItem expr2 = expression(needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables);
                        if (expr2 != null) {
                            ret = new CoerceAVM2Item(null, ((ParenthesisItem) expr).value, expr2);
                        }
                    }
                }

        }
        ret = fixPrecedence(ret);
        return ret;
    }

    private boolean isNameOrProp(GraphTargetItem item) {
        if (item instanceof UnresolvedAVM2Item) {
            return true; //we don't know yet
        }
        if (item instanceof NameAVM2Item) {
            return true;
        }
        if (item instanceof PropertyAVM2Item) {
            return true;
        }
        return false;
    }

    private boolean isType(GraphTargetItem item) {
        if (item == null) {
            return false;
        }
        while (item instanceof GetPropertyAVM2Item) {
            item = ((GetPropertyAVM2Item) item).object;
        }
        if (item instanceof NameAVM2Item) {
            return true;
        }
        return false;
    }

    private int brackets(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                s = lex();
                if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                    expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                }
            }
        } else {
            lexer.pushback(s);
            return -1;
        }
        return arrCnt;
    }

    private GraphTargetItem commaExpression(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forInLevel, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forInLevel, false, variables);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new ParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expression(Reference<Boolean> needsActivation, List<String> importedClasses, List<Integer> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, ParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean existsRemainder = false;
        boolean assocRight = false;
        switch (s.type) {
            case XML_STARTTAG_BEGIN:
                lexer.pushback(s);
                ret = xml(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
                existsRemainder = true;
                break;
            case STRING:
                ret = new StringAVM2Item(null, s.value.toString());
                existsRemainder = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new FloatValueAVM2Item(null, (Double) s.value);
                    existsRemainder = true;
                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new IntegerValueAVM2Item(null, (Long) s.value);
                    existsRemainder = true;
                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    if (num instanceof IntegerValueAVM2Item) {
                        ((IntegerValueAVM2Item) num).value = -((IntegerValueAVM2Item) num).value;
                        ret = num;
                    } else if (num instanceof FloatValueAVM2Item) {
                        Double d = ((FloatValueAVM2Item) num).value;
                        if (d.isInfinite()) {
                            ((FloatValueAVM2Item) num).value = Double.NEGATIVE_INFINITY;
                        } else {
                            ((FloatValueAVM2Item) num).value = -d;
                        }
                        ret = (num);
                    } else {
                        ret = (new SubtractAVM2Item(null, new IntegerValueAVM2Item(null, 0L), num));
                    }
                }
                break;
            case TYPEOF:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TypeOfAVM2Item(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                existsRemainder = true;
                break;
            case TRUE:
                ret = new BooleanAVM2Item(null, true);
                existsRemainder = true;
                break;
            case NULL:
                ret = new NullAVM2Item(null);
                existsRemainder = true;
                break;
            case UNDEFINED:
                ret = new UndefinedAVM2Item(null);
                break;
            case FALSE:
                ret = new BooleanAVM2Item(null, false);
                existsRemainder = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                List<NameValuePair> nvs = new ArrayList<>();

                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING);

                    GraphTargetItem n = new StringAVM2Item(null, s.value.toString());
//expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);
                    expectedType(SymbolType.COLON);
                    GraphTargetItem v = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);

                    NameValuePair nv = new NameValuePair(n, v);
                    nvs.add(nv);
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new NewObjectAVM2Item(null, nvs);
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(needsActivation, importedClasses, openedNamespaces, inBrackets, registerVars, inFunction, inMethod, variables);
                ret = new NewArrayAVM2Item(null, inBrackets);
                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                if (s.isType(SymbolType.IDENTIFIER)) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                needsActivation.setVal(true);
                ret = function(needsActivation, importedClasses, 0/*?*/, TypeItem.UNBOUNDED, openedNamespaces, true, fname, false, variables);
                break;
            case NAN:
                ret = new NanAVM2Item(null);
                existsRemainder = true;
                break;
            case INFINITY:
                ret = new FloatValueAVM2Item(null, Double.POSITIVE_INFINITY);
                existsRemainder = true;
                break;
            case INTEGER:
                ret = new IntegerValueAVM2Item(null, (Long) s.value);
                existsRemainder = true;
                break;
            case DOUBLE:
                ret = new FloatValueAVM2Item(null, (Double) s.value);
                existsRemainder = true;
                break;
            case DELETE:
                GraphTargetItem varDel = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                if (!isNameOrProp(varDel)) {
                    throw new ParseException("Not a property or name", lexer.yyline());
                }
                ret = new DeletePropertyAVM2Item(varDel, lexer.yyline());
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem varincdec = expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                if (!isNameOrProp(varincdec)) {
                    throw new ParseException("Not a property or name", lexer.yyline());
                }
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, varincdec);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, varincdec);
                }
                existsRemainder = true;
                break;
            case NOT:
                ret = new NotItem(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                existsRemainder = true;
                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = memberOrCall(needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
                existsRemainder = true;
                break;
            case NEW:
                GraphTargetItem newvar = name(needsActivation, true, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses);
                expectedType(SymbolType.PARENT_OPEN);
                ret = new ConstructSomethingAVM2Item(openedNamespaces, newvar, call(needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                existsRemainder = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
            case ATTRIBUTE:
                lexer.pushback(s);
                GraphTargetItem var = name(needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses);
                var = memberOrCall(needsActivation, importedClasses, openedNamespaces, var, registerVars, inFunction, inMethod, variables);
                ret = var;
                existsRemainder = true;
                break;
            default:
                GraphTargetItem excmd = expressionCommands(s, registerVars, inFunction, inMethod, -1, variables);
                if (excmd != null) {
                    existsRemainder = true; //?
                    ret = excmd;
                    break;
                }
                lexer.pushback(s);
        }
        if (allowRemainder && existsRemainder) {
            GraphTargetItem rem = ret;
            do {
                rem = expressionRemainder(needsActivation, openedNamespaces, rem, registerVars, inFunction, inMethod, assocRight, variables, importedClasses);
                if (rem != null) {
                    ret = rem;
                }
            } while ((!assocRight) && (rem != null));
        }
        if (debugMode) {
            System.out.println("/expression");
        }
        return ret;
    }

    private ActionScriptLexer lexer = null;
    private List<String> constantPool;

    private PackageAVM2Item parsePackage(String fileName) throws IOException, ParseException, CompilationException {
        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.PACKAGE);
        String name = "";
        s = lex();
        if (s.type != SymbolType.CURLY_OPEN) {
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            name = s.value.toString();
        }
        while (s.type != SymbolType.CURLY_OPEN) {
            expected(s, lexer.yyline(), SymbolType.DOT);
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            name += "." + s.value.toString();
        }
        List<GraphTargetItem> items = new ArrayList<>();
        List<Integer> openedNamespaces = new ArrayList<>();

        int privateNs = 0;
        int publicNs = 0;
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        if (fileName.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        String className = fileName;
        if (className.endsWith(".as")) {
            className = className.substring(0, className.length() - 3);
        }
        openedNamespaces.add(privateNs = abc.constants.addNamespace(new Namespace(Namespace.KIND_PRIVATE, 0))); //abc.constants.getStringId(name + ":" + className, true)
        int packageInternalNs = 0;
        openedNamespaces.add(packageInternalNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE_INTERNAL, abc.constants.getStringId(name, true)), 0, true));
        openedNamespaces.add(publicNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("", true)), 0, true));

        openedNamespaces.add(abc.constants.addNamespace(new Namespace(Namespace.KIND_PRIVATE, 0))); //abc.constants.getStringId(fileName + "$", true)
        if (!name.isEmpty()) {
            openedNamespaces.add(publicNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(name, true)), 0, true));
        }

        openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_NAMESPACE, abc.constants.getStringId(AS3_NAMESPACE, true)), 0, true));

        List<String> importedClasses = new ArrayList<>();

        s = lex();
        while (s.type == SymbolType.IMPORT) {
            String impPackage = "";
            String impName = null;
            boolean all = false;
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            impName = s.value.toString();
            s = lex();
            while (s.type == SymbolType.DOT) {
                if (!"".equals(impPackage)) {
                    impPackage += ".";
                }
                impPackage += impName;

                s = lex();
                if (s.type == SymbolType.MULTIPLY) {
                    impName = null;
                    s = lex();
                    break;
                }
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);

                impName = s.value.toString();
                s = lex();
            }

            if (impName == null) {
                openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(impPackage, true)), 0, true));
            } else {
                importedClasses.add(impPackage + "." + impName);
            }

            expected(s, lexer.yyline(), SymbolType.SEMICOLON);
            s = lex();
        }
        lexer.pushback(s);

        traits(importedClasses, privateNs, 0, publicNs, packageInternalNs, 0, openedNamespaces, name, null, false, items);
        expectedType(SymbolType.CURLY_CLOSE);
        return new PackageAVM2Item(name, items);
    }

    public PackageAVM2Item packageFromString(String str, String fileName) throws ParseException, IOException, CompilationException {
        this.constantPool = constantPool;
        lexer = new ActionScriptLexer(new StringReader(str));

        PackageAVM2Item ret = parsePackage(fileName);
        if (lexer.lex().type != SymbolType.EOF) {
            throw new ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        return ret;
    }

    public void addScriptFromTree(PackageAVM2Item pkg, boolean documentClass) throws ParseException, CompilationException {
        AVM2SourceGenerator gen = new AVM2SourceGenerator(abc, otherABCs);
        List<AVM2Instruction> ret = new ArrayList<>();
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<String, Integer>(), 0, Boolean.FALSE, 0);
        String className = "";
        for (GraphTargetItem it : pkg.items) {
            if (it instanceof ClassAVM2Item) {
                className = ((ClassAVM2Item) it).className;
            }
        }
        localData.documentClass = documentClass;
        abc.script_info.add(gen.generateScriptInfo(pkg, localData, pkg.items));
    }

    public void addScript(String s, boolean documentClass, String fileName) throws ParseException, IOException, CompilationException {
        PackageAVM2Item pkg = packageFromString(s, fileName);
        addScriptFromTree(pkg, documentClass);
    }

    public ActionScriptParser(ABC abc, List<ABC> otherABCs) {
        this.abc = abc;
        this.otherABCs = otherABCs;
    }

    public static void compile(String src, String dst) {
        System.err.println("WARNING: AS3 compiler is not finished yet. This is only used for debuggging!");
        try {
            SWC swc = new SWC(new FileInputStream(Configuration.getPlayerSWC()));
            SWF swf = new SWF(swc.getSWF("library.swf"), true);
            List<ABC> playerABCs = new ArrayList<>();
            for (Tag t : swf.tags) {
                if (t instanceof ABCContainerTag) {
                    playerABCs.add(((ABCContainerTag) t).getABC());
                }
            }
            ABC abc = new ABC(swf);
            ActionScriptParser parser = new ActionScriptParser(abc, playerABCs);
            parser.addScript(new String(Helper.readFile(src), "UTF-8"), true, src);
            abc.saveToStream(new FileOutputStream(new File(dst)));
        } catch (Exception ex) {
            Logger.getLogger(ActionScriptParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

}
