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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CallSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
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
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
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
import com.jpexs.decompiler.graph.model.WhileItem;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    private long uniqLast = 0;
    private final boolean debugMode = false;
    private static final String AS3_NAMESPACE = "http://adobe.com/AS3/2006/builtin";

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private List<GraphTargetItem> commands(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<NameAVM2Item> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, List<NameAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;

        ParsedSymbol s = lex();
        if (s.type == SymbolType.MULTIPLY) {
            return new UnboundedAVM2Item();
        }
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP);
        ret = new NameAVM2Item(lexer.yyline(),s.value.toString(), null, false, openedNamespaces, openedNamespacesKinds);
        variables.add((NameAVM2Item) ret);
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP);
            NameAVM2Item var = new NameAVM2Item(lexer.yyline(),s.value.toString(), null, false, openedNamespaces, openedNamespacesKinds);
            ret = new GetPropertyAVM2Item(null, ret, var);
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem memberOrCall(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN, SymbolType.PARENT_OPEN)) {
            switch (s.type) {
                case DOT:
                case BRACKET_OPEN:
                    lexer.pushback(s);
                    ret = member(openedNamespaces, openedNamespacesKinds, ret, registerVars, inFunction, inMethod, variables);
                    break;
                case PARENT_OPEN:
                    ret = new CallAVM2Item(ret, call(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables));
                    break;
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem member(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT)) {
            NameAVM2Item var = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
            ret = new GetPropertyAVM2Item(null, ret, var);
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private NameAVM2Item qname(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, List<NameAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        NameAVM2Item ret = new NameAVM2Item(lexer.yyline(),s.value.toString(), null, false, openedNamespaces, openedNamespacesKinds);
        variables.add((NameAVM2Item) ret);
        return ret;
    }

    private NameAVM2Item name(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        NameAVM2Item ret = new NameAVM2Item(lexer.yyline(),s.value.toString(), null, false, openedNamespaces, openedNamespacesKinds);
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            ret.appendName(s.value.toString());
        }
        if (s.type == SymbolType.BRACKET_OPEN) {
            ret.setIndex(expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
            expectedType(SymbolType.BRACKET_CLOSE);
        } else {
            lexer.pushback(s);
        }
        variables.add((NameAVM2Item) ret);
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

    private List<GraphTargetItem> call(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private MethodAVM2Item method(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, boolean isStatic, int namespaceKind, boolean withBody, String functionName, boolean isMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        FunctionAVM2Item f = function(openedNamespaces, openedNamespacesKinds, withBody, functionName, isMethod, variables);
        return new MethodAVM2Item(isStatic, namespaceKind, functionName, f.paramTypes, f.paramNames, f.paramValues, f.body, variables, f.retType);
    }

    private FunctionAVM2Item function(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, boolean withBody, String functionName, boolean isMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();
        List<GraphTargetItem> paramTypes = new ArrayList<>();
        List<GraphTargetItem> paramValues = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            paramNames.add(s.value.toString());
            s = lex();
            if (s.type == SymbolType.COLON) {
                paramTypes.add(type(openedNamespaces, openedNamespacesKinds, variables));
                s = lex();
            } else {
                paramTypes.add(new UnboundedAVM2Item());
            }
            if (s.type == SymbolType.ASSIGN) {
                paramValues.add(expression(openedNamespaces, openedNamespacesKinds, null, isMethod, isMethod, isMethod, variables));
            } else {
                if (!paramValues.isEmpty()) {
                    throw new ParseException("Some of parameters do not have default values", lexer.yyline());
                }
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }
        s = lex();
        GraphTargetItem retType;
        if (s.type == SymbolType.COLON) {
            retType = type(openedNamespaces, openedNamespacesKinds, variables);
        } else {
            retType = new UnboundedAVM2Item();
            lexer.pushback(s);
        }
        List<GraphTargetItem> body = null;
        List<NameAVM2Item> subvariables = new ArrayList<>();
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);

            body = commands(openedNamespaces, openedNamespacesKinds, new Stack<Loop>(), new HashMap<Loop, String>(), new HashMap<String, Integer>(), true, isMethod, 0, subvariables);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        //return new FunctionAVM2Item(null, functionName, paramNames, body, constantPool, -1, subvariables);
        return new FunctionAVM2Item(functionName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

    private GraphTargetItem traits(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, String packageName, String classNameStr, boolean isInterface, List<GraphTargetItem> traits) throws ParseException, IOException {
        ParsedSymbol s;
        GraphTargetItem constr = null;
        List<NameAVM2Item> variables = new ArrayList<>();
        looptrait:
        while (true) {
            s = lex();
            boolean isStatic = false;
            int nsKind = -1;
            boolean isGetter = false;
            boolean isSetter = false;
            //TODO: namespace name
            //TODO: final, dynamic
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE, SymbolType.PROTECTED, SymbolType.GET, SymbolType.SET)) {
                if (s.type == SymbolType.STATIC) {
                    if (isInterface) {
                        throw new ParseException("Interface cannot have static traits", lexer.yyline());
                    }
                    if (classNameStr == null) {
                        throw new ParseException("No static keyword allowed here", lexer.yyline());
                    }
                    if (isStatic) {
                        throw new ParseException("Only one static identifier allowed", lexer.yyline());
                    }
                    isStatic = true;
                } else if (s.type == SymbolType.GET) {
                    if (classNameStr == null) {
                        throw new ParseException("No get keyword allowed here", lexer.yyline());
                    }
                    if (isGetter || isSetter) {
                        throw new ParseException("Only one get/set keyword allowed", lexer.yyline());
                    }
                    isGetter = true;
                } else if (s.type == SymbolType.SET) {
                    if (classNameStr == null) {
                        throw new ParseException("No set keyword allowed here", lexer.yyline());
                    }
                    if (isGetter || isSetter) {
                        throw new ParseException("Only one get/set keyword allowed", lexer.yyline());
                    }
                    isSetter = true;
                } else {
                    if (nsKind != -1) {
                        throw new ParseException("Only one access identifier allowed", lexer.yyline());
                    }
                }
                switch (s.type) {
                    case PUBLIC:
                        nsKind = Namespace.KIND_PACKAGE;
                        break;
                    case PRIVATE:
                        nsKind = Namespace.KIND_PRIVATE;
                        break;
                    case PROTECTED:
                        nsKind = Namespace.KIND_PROTECTED;
                        break;
                }
                s = lex();
            }
            if (nsKind == -1) {
                nsKind = Namespace.KIND_PACKAGE_INTERNAL;
            }
            if (nsKind == Namespace.KIND_PROTECTED && isStatic) {
                nsKind = Namespace.KIND_STATIC_PROTECTED;
            }
            switch (s.type) {
                case CLASS:
                    if (classNameStr != null) {
                        throw new ParseException("Nested classes not supported", lexer.yyline());
                    }
                    GraphTargetItem classTypeStr = type(openedNamespaces, openedNamespacesKinds, variables);
                    s = lex();
                    GraphTargetItem extendsTypeStr = null;
                    if (s.type == SymbolType.EXTENDS) {
                        extendsTypeStr = type(openedNamespaces, openedNamespacesKinds, variables);
                        s = lex();
                    }
                    List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                    if (s.type == SymbolType.IMPLEMENTS) {
                        do {
                            GraphTargetItem implementsTypeStr = type(openedNamespaces, openedNamespacesKinds, variables);
                            implementsTypeStrs.add(implementsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    traits.add((classTraits(openedNamespaces, openedNamespacesKinds, packageName, nsKind, false, classTypeStr, extendsTypeStr, implementsTypeStrs, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                case INTERFACE:
                    if (classNameStr != null) {
                        throw new ParseException("Nested interfaces not supported", lexer.yyline());
                    }
                    GraphTargetItem interfaceTypeStr = type(openedNamespaces, openedNamespacesKinds, variables);
                    s = lex();
                    List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                    if (s.type == SymbolType.EXTENDS) {
                        do {
                            GraphTargetItem intExtendsTypeStr = type(openedNamespaces, openedNamespacesKinds, variables);
                            intExtendsTypeStrs.add(intExtendsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    traits.add((classTraits(openedNamespaces, openedNamespacesKinds, packageName, nsKind, true, interfaceTypeStr, null, intExtendsTypeStrs, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;

                case FUNCTION:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String fname = s.value.toString();
                    if (classNameStr != null && fname.equals(classNameStr)) { //constructor
                        if (isStatic) {
                            throw new ParseException("Constructor cannot be static", lexer.yyline());
                        }
                        constr = (method(openedNamespaces, openedNamespacesKinds, false, nsKind, !isInterface, "", true, variables));
                    } else {
                        if (isStatic) {
                            GraphTargetItem t;
                            MethodAVM2Item ft = method(openedNamespaces, openedNamespacesKinds, isStatic, nsKind, !isInterface, fname, true, variables);
                            traits.add(ft);
                            if (isGetter || isSetter) {
                                throw new ParseException("Getter or Setter cannot be static", lexer.yyline());
                            }
                        } else {
                            MethodAVM2Item ft = method(openedNamespaces, openedNamespacesKinds, isStatic, nsKind, !isInterface, fname, true, variables);
                            GraphTargetItem t;
                            if (isGetter) {
                                GetterAVM2Item g = new GetterAVM2Item(isStatic, ft.namespaceKind, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                                t = g;
                            } else if (isSetter) {
                                SetterAVM2Item st = new SetterAVM2Item(isStatic, ft.namespaceKind, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                                t = st;
                            } else {
                                t = ft;
                            }

                            traits.add(ft);
                        }
                    }
                    //}
                    break;
                case CONST:
                case VAR:
                    boolean isConst = s.type == SymbolType.CONST;
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();
                    GraphTargetItem type = null;
                    if (s.type == SymbolType.COLON) {
                        type = type(openedNamespaces, openedNamespacesKinds, variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        GraphTargetItem tar;
                        if (isConst) {
                            tar = new ConstAVM2Item(isStatic, nsKind, vcname, type, expression(openedNamespaces, openedNamespacesKinds, new HashMap<String, Integer>(), false, false, true, variables));
                        } else {
                            tar = new SlotAVM2Item(isStatic, nsKind, vcname, type, expression(openedNamespaces, openedNamespacesKinds, new HashMap<String, Integer>(), false, false, true, variables));
                        }
                        traits.add(tar);
                        s = lex();
                    }
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

    private GraphTargetItem classTraits(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, String packageName, int namespaceKind, boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<NameAVM2Item> variables) throws IOException, ParseException {

        GraphTargetItem ret = null;

        ParsedSymbol s = null;
        MethodAVM2Item constr = null;
        List<GraphTargetItem> traits = new ArrayList<>();

        String classNameStr = nameStr.toString();

        openedNamespaces = new ArrayList<>(openedNamespaces);
        openedNamespacesKinds = new ArrayList<>(openedNamespacesKinds);
        
        openedNamespacesKinds.add(Namespace.KIND_PRIVATE);
        openedNamespaces.add(packageName + ":" + classNameStr);
        openedNamespacesKinds.add(Namespace.KIND_PACKAGE);
        openedNamespaces.add("");
        openedNamespacesKinds.add(Namespace.KIND_PRIVATE);
        openedNamespaces.add(classNameStr + ".as$");
        openedNamespacesKinds.add(Namespace.KIND_PACKAGE);
        openedNamespaces.add(packageName);
        openedNamespacesKinds.add(Namespace.KIND_PACKAGE_INTERNAL);
        openedNamespaces.add(packageName);
        openedNamespacesKinds.add(Namespace.KIND_NAMESPACE);
        openedNamespaces.add(AS3_NAMESPACE);
        openedNamespacesKinds.add(Namespace.KIND_PROTECTED);
        openedNamespaces.add(packageName + ":" + classNameStr);
        openedNamespacesKinds.add(Namespace.KIND_STATIC_PROTECTED);
        openedNamespaces.add(packageName + ":" + classNameStr);

        traits(openedNamespaces, openedNamespacesKinds, packageName, classNameStr, isInterface, traits);

        if (isInterface) {
            return new InterfaceAVM2Item(namespaceKind, classNameStr, implementsStr, traits);
        } else {
            return new ClassAVM2Item(namespaceKind, classNameStr, extendsStr, implementsStr, constr, traits);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<NameAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            /*case INT:
             expectedType(SymbolType.PARENT_OPEN);                
             ret = new ToIntegerAVM2Item(null, expression(openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
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
             ret = new ToNumberAVM2Item(null, expression(openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
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
             ret = new ToStringAVM2Item(null, expression(openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             ret = memberOrCall(ret, registerVars, inFunction, inMethod, variables);
             }
             break;*/
            default:
                return null;
        }
        //return ret;
    }

    private GraphTargetItem command(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<NameAVM2Item> variables) throws IOException, ParseException {
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

        switch (s.type) {
            case WITH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem wvar = (name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> wcmd = commands(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new WithAVM2Item(null, wvar, wcmd);
                break;
            case DELETE:
                GraphTargetItem varDel = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                if (varDel instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item gm = (GetPropertyAVM2Item) varDel;
                    ret = new DeletePropertyAVM2Item(null, gm.object, gm.propertyName);
                } else if (varDel instanceof NameAVM2Item) {
                    variables.remove(varDel);
                    ret = new DeletePropertyAVM2Item(null, null, (NameAVM2Item) varDel);
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case FUNCTION:
                s = lexer.lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                ret = (function(openedNamespaces, openedNamespacesKinds, true, s.value.toString(), false, variables));
                break;
            case VAR:
                s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                String varIdentifier = s.value.toString();
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type(openedNamespaces, openedNamespacesKinds, variables);
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    GraphTargetItem varval = (expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
                    ret = new NameAVM2Item(lexer.yyline(),varIdentifier, varval, true, openedNamespaces, openedNamespacesKinds);
                    variables.add((NameAVM2Item) ret);
                } else {
                    ret = new NameAVM2Item(lexer.yyline(),varIdentifier, null, true, openedNamespaces, openedNamespacesKinds);
                    variables.add((NameAVM2Item) ret);
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret = new BlockItem(null, commands(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                GraphTargetItem varincdec = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, varincdec);
                }
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    List<GraphTargetItem> args = call(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                    ret = new ConstructSuperAVM2Item(null, new LocalRegAVM2Item(null, 0, null), args);
                } else {//no costructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem ifExpr = (expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                GraphTargetItem onTrue = command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                List<GraphTargetItem> onTrueList = new ArrayList<>();
                onTrueList.add(onTrue);
                s = lex();
                List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    onFalseList = new ArrayList<>();
                    onFalseList.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                } else {
                    lexer.pushback(s);
                }
                ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> whileExpr = new ArrayList<>();
                whileExpr.add(commaExpression(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> whileBody = new ArrayList<>();
                Loop wloop = new Loop(uniqId(), null, null);
                if (loopLabel != null) {
                    loopLabels.put(wloop, loopLabel);
                }
                loops.push(wloop);
                whileBody.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                ret = new WhileItem(null, wloop, whileExpr, whileBody);
                break;
            case DO:
                List<GraphTargetItem> doBody = new ArrayList<>();
                Loop dloop = new Loop(uniqId(), null, null);
                loops.push(dloop);
                if (loopLabel != null) {
                    loopLabels.put(dloop, loopLabel);
                }
                doBody.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> doExpr = new ArrayList<>();
                doExpr.add(commaExpression(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new DoWhileItem(null, dloop, doBody, doExpr);
                break;
            case FOR:
                expectedType(SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                boolean each = false;
                GraphTargetItem collection = null;
                String objIdent = null;
                int innerExprReg = 0;
                if (s.type == SymbolType.EACH) {
                    each = true;
                    forin = true;
                }
                if (s.type == SymbolType.VAR || s.type == SymbolType.IDENTIFIER || each) {
                    ParsedSymbol s2 = null;
                    ParsedSymbol ssel = s;
                    if (s.type == SymbolType.VAR) {
                        s2 = lex();
                        ssel = s2;
                    }

                    if (forin) {
                        expected(ssel, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.VAR);
                    }

                    if (ssel.type == SymbolType.IDENTIFIER) {
                        objIdent = ssel.value.toString();

                        ParsedSymbol s3 = lex();
                        if (s3.type == SymbolType.IN) {
                            if (inFunction) {
                                for (int i = 0; i < 256; i++) {
                                    if (!registerVars.containsValue(i)) {
                                        registerVars.put(objIdent, i);
                                        innerExprReg = i;
                                        break;
                                    }
                                }
                            }
                            collection = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                            forin = true;
                        } else {
                            lexer.pushback(s3);
                            if (s2 != null) {
                                lexer.pushback(s2);
                            }
                            lexer.pushback(s);
                        }
                    } else {
                        if (s2 != null) {
                            lexer.pushback(s2);
                        }
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
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
                    GraphTargetItem fc = command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                    if (fc != null) { //can be empty command
                        forFirstCommands.add(fc);
                    }
                    forExpr = (expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.SEMICOLON);
                    forFinalCommands.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                }
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> forBody = new ArrayList<>();
                forBody.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables));
                if (forin) {

                    NameAVM2Item obj = new NameAVM2Item(lexer.yyline(),objIdent, null, false, openedNamespaces, openedNamespacesKinds);
                    variables.add(obj);
                    if (each) {
                        ret = new ForEachInAVM2Item(null, floop, new InAVM2Item(null, obj, collection), forBody);
                    } else {

                        ret = new ForInAVM2Item(null, floop, new InAVM2Item(null, obj, collection), forBody);
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
                GraphTargetItem switchExpr = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
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
                        GraphTargetItem curCaseExpr = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                        caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        caseExprsAll.add(curCaseExpr);
                        valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    List<GraphTargetItem> caseCmd = commands(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                    caseCmds.add(caseCmd);
                    s = lex();
                }
                List<GraphTargetItem> defCmd = new ArrayList<>();
                if (s.type == SymbolType.DEFAULT) {
                    expectedType(SymbolType.COLON);
                    defCmd = commands(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
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
                GraphTargetItem retexpr = expression(openedNamespaces, openedNamespacesKinds, true, registerVars, inFunction, inMethod, true, variables);
                if (retexpr == null) {
                    ret = new ReturnVoidAVM2Item(null);
                } else {
                    ret = new ReturnValueAVM2Item(null, retexpr);
                }
                break;
            case TRY:
                List<GraphTargetItem> tryCommands = new ArrayList<>();
                tryCommands.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                s = lex();
                boolean found = false;
                List<List<GraphTargetItem>> catchCommands = null;
                List<ExceptionSAVM2item> catchExceptions = new ArrayList<>();
                if (s.type == SymbolType.CATCH) {
                    expectedType(SymbolType.PARENT_OPEN);
                    NameAVM2Item ename = qname(openedNamespaces, openedNamespacesKinds, variables);
                    expectedType(SymbolType.COLON);
                    NameAVM2Item etype = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                    ABCException e = new ABCException();
                    catchExceptions.add(new ExceptionSAVM2item(etype, ename));
                    expectedType(SymbolType.PARENT_CLOSE);
                    catchCommands = new ArrayList<>();
                    List<GraphTargetItem> cc = new ArrayList<>();
                    cc.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = new ArrayList<>();
                    finallyCommands.add(command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                TryAVM2Item tai = new TryAVM2Item(tryCommands, null, catchCommands, finallyCommands);
                tai.catchExceptions2 = catchExceptions;
                ret = tai;
                break;
            case THROW:
                ret = new ThrowAVM2Item(null, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
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
                ret = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                if (debugMode) {
                    System.out.println("/command");
                }
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (ret == null) {  //can be popped expression            
            buf.pushAllBack(lexer);
            ret = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<NameAVM2Item> variables) throws IOException, ParseException {
        return expression(openedNamespaces, openedNamespacesKinds, false, registerVars, inFunction, inMethod, allowRemainder, variables);
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

    private GraphTargetItem expressionRemainder(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<NameAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        switch (s.type) {
            case TERNAR:
                GraphTargetItem terOnTrue = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.COLON);
                GraphTargetItem terOnFalse = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                ret = new TernarOpItem(null, expr, terOnTrue, terOnFalse);
                break;
            case SHIFT_LEFT:
                ret = new LShiftAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case SHIFT_RIGHT:
                ret = new RShiftAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case USHIFT_RIGHT:
                ret = new URShiftAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case BITAND:
                ret = new BitAndAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case BITOR:
                ret = new BitOrAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case DIVIDE:
                ret = new DivideAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case MODULO:
                ret = new ModuloAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case EQUALS:
                ret = new EqAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case STRICT_EQUALS:
                ret = new StrictEqAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case NOT_EQUAL:
                ret = new NeqAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case STRICT_NOT_EQUAL:
                ret = new StrictNeqAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case LOWER_THAN:
                ret = new LtAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case LOWER_EQUAL:
                ret = new LeAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_THAN:
                ret = new GtAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_EQUAL:
                ret = new GeAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case AND:
                ret = new AndItem(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case OR:
                ret = new OrItem(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case MINUS:
                ret = new SubtractAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case MULTIPLY:
                ret = new MultiplyAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case PLUS:
                ret = new AddAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case XOR:
                ret = new BitXorAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
                break;
            case AS:
                //TODO
                break;
            case INSTANCEOF:
                ret = new InstanceOfAVM2Item(null, expr, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, false, variables));
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
                GraphTargetItem assigned = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
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
                if (expr instanceof NameAVM2Item) {
                    ((NameAVM2Item) expr).setStoreValue(assigned);
                    ((NameAVM2Item) expr).setDefinition(false);
                    ret = expr;
                } else if (expr instanceof GetPropertyAVM2Item) {
                    ret = new SetPropertyAVM2Item(null, ((GetPropertyAVM2Item) expr).object, ((GetPropertyAVM2Item) expr).propertyName, assigned);
                } else {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                break;
            case INCREMENT: //postincrement
                if (!(expr instanceof NameAVM2Item) && !(expr instanceof GetPropertyAVM2Item)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostIncrementAVM2Item(null, expr);
                break;
            case DECREMENT: //postdecrement
                if (!(expr instanceof NameAVM2Item) && !(expr instanceof GetPropertyAVM2Item)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostDecrementAVM2Item(null, expr);
                break;
            case DOT: //member
            case BRACKET_OPEN: //member
            case PARENT_OPEN: //function call
                lexer.pushback(s);
                ret = memberOrCall(openedNamespaces, openedNamespacesKinds, expr, registerVars, inFunction, inMethod, variables);
                break;

            default:
                lexer.pushback(s);
                if (expr instanceof ParenthesisItem) {
                    if (isType(((ParenthesisItem) expr).value)) {
                        GraphTargetItem expr2 = expression(openedNamespaces, openedNamespacesKinds, false, registerVars, inFunction, inMethod, true, variables);
                        if (expr2 != null) {
                            ret = new CoerceAVM2Item(null, ((ParenthesisItem) expr).value, expr2);
                        }
                    }
                }

        }
        ret = fixPrecedence(ret);
        return ret;
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

    private int brackets(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<NameAVM2Item> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
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

    private GraphTargetItem commaExpression(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forInLevel, List<NameAVM2Item> variables) throws IOException, ParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(openedNamespaces, openedNamespacesKinds, loops, loopLabels, registerVars, inFunction, inMethod, forInLevel, false, variables);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new ParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expression(List<String> openedNamespaces, List<Integer> openedNamespacesKinds, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<NameAVM2Item> variables) throws IOException, ParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean existsRemainder = false;
        boolean assocRight = false;
        switch (s.type) {
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
                    GraphTargetItem num = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables);
                    if (num instanceof IntegerValueAVM2Item) {
                        ((IntegerValueAVM2Item) num).value = -(Long) ((IntegerValueAVM2Item) num).value;
                        ret = num;
                    } else if (num instanceof FloatValueAVM2Item) {
                        Double d = (Double) ((FloatValueAVM2Item) num).value;
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
                ret = new TypeOfAVM2Item(null, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
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

                    GraphTargetItem n = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, allowRemainder, variables);
                    expectedType(SymbolType.COLON);
                    GraphTargetItem v = expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, allowRemainder, variables);

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
                int arrCnt = brackets(openedNamespaces, openedNamespacesKinds, inBrackets, registerVars, inFunction, inMethod, variables);
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
                ret = function(openedNamespaces, openedNamespacesKinds, true, fname, false, variables);
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
                GraphTargetItem varDel = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                if (varDel instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item gm = (GetPropertyAVM2Item) varDel;
                    ret = new DeletePropertyAVM2Item(null, gm.object, gm.propertyName);
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem prevar = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, prevar);
                }
                existsRemainder = true;
                break;
            case NOT:
                ret = new NotItem(null, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
                existsRemainder = true;
                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = memberOrCall(openedNamespaces, openedNamespacesKinds, ret, registerVars, inFunction, inMethod, variables);
                existsRemainder = true;
                break;
            case NEW:
                GraphTargetItem newvar = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                expectedType(SymbolType.PARENT_OPEN);
                if (newvar instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item mem = (GetPropertyAVM2Item) newvar;
                    ret = new ConstructPropAVM2Item(null, mem.object, mem.propertyName, call(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables));
                } else if (newvar instanceof NameAVM2Item) {
                    ret = new ConstructAVM2Item(null, newvar, call(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables));
                } else {
                    throw new ParseException("Invalid new item", lexer.yyline());
                }
                existsRemainder = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                lexer.pushback(s);
                GraphTargetItem var = name(openedNamespaces, openedNamespacesKinds, registerVars, inFunction, inMethod, variables);
                var = memberOrCall(openedNamespaces, openedNamespacesKinds, var, registerVars, inFunction, inMethod, variables);
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
                rem = expressionRemainder(openedNamespaces, openedNamespacesKinds, rem, registerVars, inFunction, inMethod, assocRight, variables);
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

    private PackageAVM2Item parsePackage() throws IOException, ParseException {
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
        List<Integer> openedNamespacesKinds = new ArrayList<>();
        List<String> openedNamespaces = new ArrayList<>();
        s = lex();
        while(s.type == SymbolType.IMPORT){
            String impPackage = "";
            String impName = null;
            boolean all = false;
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            impName = s.value.toString();
            s = lex();
            while(s.type == SymbolType.DOT){        
                if(!"".equals(impPackage)){
                    impPackage += ".";
                }
                impPackage += impName;
                
                s = lex();
                if(s.type == SymbolType.MULTIPLY){
                    impName = null;
                    break;
                }
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                
                impName = s.value.toString();
                s = lex();
            }
                        
            String fullImp = impName==null?impPackage:impPackage+":"+impName;
            openedNamespaces.add(fullImp);
            openedNamespacesKinds.add(Namespace.KIND_PACKAGE);
            
            expectedType(SymbolType.SEMICOLON);
        }
        lexer.pushback(s);

        traits(openedNamespaces, openedNamespacesKinds, name, null, false, items);
        expectedType(SymbolType.CURLY_CLOSE);
        return new PackageAVM2Item(name, items);
    }

    public PackageAVM2Item packageFromString(String str) throws ParseException, IOException {
        this.constantPool = constantPool;
        lexer = new ActionScriptLexer(new StringReader(str));

        PackageAVM2Item ret = parsePackage();
        if (lexer.lex().type != SymbolType.EOF) {
            throw new ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        return ret;
    }

    public ScriptInfo scriptFromTree(PackageAVM2Item pkg, ABC abc, List<ABC> otherABCs) {
        AVM2SourceGenerator gen = new AVM2SourceGenerator(abc, new ArrayList<ABC>());
        List<AVM2Instruction> ret = new ArrayList<>();
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<String, Integer>(), 0, Boolean.FALSE, 0);
        String className = "";
        for (GraphTargetItem it : pkg.items) {
            if (it instanceof ClassAVM2Item) {
                className = ((ClassAVM2Item) it).className;
            }
        }
        return gen.generateScriptInfo(localData, pkg.items);
    }

    public ScriptInfo scriptFromString(String s, ABC abc, List<ABC> otherABCs) throws ParseException, IOException {
        PackageAVM2Item pkg = packageFromString(s);
        return scriptFromTree(pkg, abc, otherABCs);
    }
}
