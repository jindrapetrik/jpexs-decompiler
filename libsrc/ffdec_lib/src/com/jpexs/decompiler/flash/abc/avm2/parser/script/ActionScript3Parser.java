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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWC;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AsTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitAndAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitOrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitXorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.DeletePropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.DivideAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.EqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.GeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.GtAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.InstanceOfAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.IsTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LtAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.ModuloAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.MultiplyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.NegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.NeqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.RShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.StrictEqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.StrictNeqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.SubtractAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.TypeOfAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.URShiftAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
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
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
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
public class ActionScript3Parser {

    private long uniqLast = 0;

    private final boolean debugMode = false;

    private static final String AS3_NAMESPACE = "http://adobe.com/AS3/2006/builtin";

    private final ABC abc;

    private final List<ABC> otherABCs;

    private static final List<ABC> playerABCs = new ArrayList<>();

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private List<GraphTargetItem> commands(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        ParsedSymbol s = lex();
        if (s.type == SymbolType.MULTIPLY) {
            return new UnboundedTypeItem();
        } else if (s.type == SymbolType.VOID) {
            return new TypeItem(DottedChain.VOID);
        } else {
            lexer.pushback(s);
        }

        GraphTargetItem t = name(thisType, pkg, needsActivation, true, openedNamespaces, null, false, false, variables, importedClasses);
        t = applyType(thisType, pkg, needsActivation, importedClasses, openedNamespaces, t, new HashMap<>(), false, false, variables);
        return t;
    }

    private GraphTargetItem memberOrCall(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        if (debugMode) {
            System.out.println("memberOrCall:");
        }
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME, SymbolType.FILTER)) {
            switch (s.type) {
                case BRACKET_OPEN:
                case DOT:
                case TYPENAME:
                    lexer.pushback(s);
                    ret = member(thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
                    break;
                case FILTER:
                    needsActivation.setVal(true);
                    ret = new XMLFilterAVM2Item(ret, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, inMethod, variables), openedNamespaces);
                    expectedType(SymbolType.PARENT_CLOSE);
                    break;
                case PARENT_OPEN:
                    ret = new CallAVM2Item(lexer.yyline(), ret, call(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    break;

            }
            s = lex();
        }
        if (s.type == SymbolType.INCREMENT) {
            if (!isNameOrProp(ret)) {
                throw new AVM2ParseException("Invalid assignment", lexer.yyline());
            }
            ret = new PostIncrementAVM2Item(null, ret);
            s = lex();

        } else if (s.type == SymbolType.DECREMENT) {
            if (!isNameOrProp(ret)) {
                throw new AVM2ParseException("Invalid assignment", lexer.yyline());
            }
            ret = new PostDecrementAVM2Item(null, ret);
            s = lex();
        }

        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/memberOrCall");
        }
        return ret;
    }

    private GraphTargetItem applyType(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.TYPENAME) {
            List<GraphTargetItem> params = new ArrayList<>();
            do {
                params.add(expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables)
                );
                s = lex();
            } while (s.type == SymbolType.COMMA);
            if (s.type == SymbolType.USHIFT_RIGHT) {
                s = new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_THAN);
                lexer.pushback(s);
                lexer.pushback(s);
            }
            if (s.type == SymbolType.SHIFT_RIGHT) {
                s = new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.GREATER_THAN);
                lexer.pushback(s);
            }
            expected(s, lexer.yyline(), SymbolType.GREATER_THAN);
            ret = new ApplyTypeAVM2Item(null, ret, params);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private GraphTargetItem member(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        if (debugMode) {
            System.out.println("member:");
        }
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME)) {
            ParsedSymbol s2 = lex();
            boolean attr = false;
            if (s.type == SymbolType.DOT) {
                if (s2.type == SymbolType.ATTRIBUTE) {
                    attr = true;
                } else {
                    lexer.pushback(s2);
                }

            } else {
                lexer.pushback(s2);
            }
            if (s.type == SymbolType.TYPENAME) {
                lexer.pushback(s);
                ret = applyType(thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
                s = lex();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                GraphTargetItem index = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.BRACKET_CLOSE);
                ret = new IndexAVM2Item(attr, ret, index, null, openedNamespaces);
                s = lex();
            } else {
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                String propName = s.value.toString();
                GraphTargetItem propItem = null;
                s = lex();
                GraphTargetItem ns = null;
                if (s.type == SymbolType.NAMESPACE_OP) {
                    ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, false, null, lexer.yyline(), new DottedChain(propName), null, openedNamespaces);
                    variables.add((UnresolvedAVM2Item) ns);
                    s = lex();
                    if (s.type == SymbolType.BRACKET_OPEN) {
                        propItem = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                        expectedType(SymbolType.BRACKET_CLOSE);
                        propName = null;
                    } else {
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        propName = s.value.toString();
                        propItem = null;
                    }
                } else {
                    lexer.pushback(s);
                }
                if (ns != null) {
                    ret = new NamespacedAVM2Item(ns, propName, propItem, ret, attr, openedNamespaces, null);
                } else {
                    ret = new PropertyAVM2Item(ret, (attr ? "@" : "") + propName, abc, otherABCs, openedNamespaces, new ArrayList<>());
                }
                s = lex();
            }
        }
        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/member");
        }
        return ret;
    }

    private GraphTargetItem name(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, boolean typeOnly, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, List<DottedChain> importedClasses) throws IOException, AVM2ParseException {
        ParsedSymbol s = lex();
        DottedChain name = new DottedChain();
        String name2 = "";
        if (s.type == SymbolType.ATTRIBUTE) {
            name2 += "@";
            s = lex();
        }
        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        name2 += s.value.toString();
        s = lex();
        boolean attrBracket = false;

        name = name.add(name2);
        while (s.isType(SymbolType.DOT)) {
            //name += s.value.toString(); //. or ::
            s = lex();
            name2 = "";
            if (s.type == SymbolType.ATTRIBUTE) {
                name2 += "@";
                s = lex();
                if (s.type == SymbolType.MULTIPLY) {
                    name2 += s.value.toString();
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    name2 += s.value.toString();
                } else {
                    if (s.type != SymbolType.BRACKET_OPEN) {
                        throw new AVM2ParseException("Attribute identifier or bracket expected", lexer.yyline());
                    }
                    attrBracket = true;
                    continue;
                }
            } else {
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.NAMESPACE);
                name2 += s.value.toString();
            }
            name = name.add(name2);
            s = lex();
        }
        String nsname = null;
        String nsprop = null;
        GraphTargetItem nspropItem = null;
        if (s.type == SymbolType.NAMESPACE_OP) {
            nsname = name.getLast();
            s = lex();
            if (s.group == SymbolGroup.IDENTIFIER) {
                nsprop = s.value.toString();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                nspropItem = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.BRACKET_CLOSE);
            }
            name = name.getWithoutLast();
            s = lex();
        }

        GraphTargetItem ret = null;
        if (!name.isEmpty()) {
            UnresolvedAVM2Item unr = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), name, null, openedNamespaces);
            //unr.setIndex(index);
            variables.add(unr);
            ret = unr;
        }
        if (nsname != null) {
            boolean attr = nsname.startsWith("@");
            if (attr) {
                nsname = nsname.substring(1);
            }
            UnresolvedAVM2Item ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), new DottedChain(nsname), null, openedNamespaces);
            variables.add(ns);
            ret = new NamespacedAVM2Item(ns, nsprop, nspropItem, ret, attr, openedNamespaces, null);
        }
        if (s.type == SymbolType.BRACKET_OPEN) {
            lexer.pushback(s);
            if (attrBracket) {
                lexer.pushback(new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ATTRIBUTE, "@"));
                lexer.pushback(new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DOT, "."));
            }
            ret = member(thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, AVM2ParseException {
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
            throw new AVM2ParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, AVM2ParseException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, AVM2ParseException {
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private MethodAVM2Item method(List<Map.Entry<String, Map<String, String>>> metadata, DottedChain pkg, boolean isInterface, String customAccess, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, boolean override, boolean isFinal, TypeItem thisType, List<Integer> openedNamespaces, boolean isStatic, int namespace, String functionName, boolean isMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        FunctionAVM2Item f = function(metadata, pkg, isInterface, needsActivation, importedClasses, namespace, thisType, openedNamespaces, functionName, isMethod, variables);
        return new MethodAVM2Item(f.metadata, f.pkg, f.isInterface, customAccess, f.needsActivation, f.hasRest, f.line, override, isFinal, isStatic, f.namespace, functionName, f.paramTypes, f.paramNames, f.paramValues, f.body, f.subvariables, f.retType);
    }

    private FunctionAVM2Item function(List<Map.Entry<String, Map<String, String>>> metadata, DottedChain pkg, boolean isInterface, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, int namespace, TypeItem thisType, List<Integer> openedNamespaces, String functionName, boolean isMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {

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
            expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);

            paramNames.add(s.value.toString());
            s = lex();
            if (!hasRest) {
                if (s.type == SymbolType.COLON) {
                    paramTypes.add(type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables));
                    s = lex();
                } else {
                    paramTypes.add(new UnboundedTypeItem());
                }
                if (s.type == SymbolType.ASSIGN) {
                    paramValues.add(expression(thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, null, isMethod, isMethod, isMethod, variables));
                    s = lex();
                } else {
                    if (!paramValues.isEmpty()) {
                        throw new AVM2ParseException("Some of parameters do not have default values", lexer.yyline());
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
            retType = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);
        } else {
            retType = new UnboundedTypeItem();
            lexer.pushback(s);
        }
        List<GraphTargetItem> body = null;
        List<AssignableAVM2Item> subvariables = new ArrayList<>();
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), "this", null, true, openedNamespaces));
        for (int i = 0; i < paramNames.size() - (hasRest ? 1 : 0); i++) {
            subvariables.add(new NameAVM2Item(paramTypes.get(i), lexer.yyline(), paramNames.get(i), null, true, openedNamespaces));
        }
        if (hasRest) {
            subvariables.add(new NameAVM2Item(TypeItem.UNBOUNDED, lexer.yyline(), paramNames.get(paramNames.size() - 1), null, true, openedNamespaces));
        }
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), "arguments", null, true, openedNamespaces));
        int parCnt = subvariables.size();
        Reference<Boolean> needsActivation2 = new Reference<>(false);
        if (!isInterface) {
            expectedType(SymbolType.CURLY_OPEN);
            body = commands(thisType, pkg, needsActivation2, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, isMethod, 0, subvariables);
            expectedType(SymbolType.CURLY_CLOSE);
        } else {
            expectedType(SymbolType.SEMICOLON);
        }

        for (int i = 0; i < parCnt; i++) {
            subvariables.remove(0);
        }
        return new FunctionAVM2Item(metadata, pkg, isInterface, needsActivation2.getVal(), namespace, hasRest, line, functionName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

    private GraphTargetItem traits(String scriptName, boolean scriptTraits, List<AssignableAVM2Item> sinitVariables, Reference<Boolean> sinitNeedsActivation, List<GraphTargetItem> staticInitializer, List<DottedChain> importedClasses, int privateNs, int protectedNs, int publicNs, int packageInternalNs, int protectedStaticNs, List<Integer> openedNamespaces, DottedChain pkg, String classNameStr, boolean isInterface, List<GraphTargetItem> traits) throws AVM2ParseException, IOException, CompilationException {
        ParsedSymbol s;
        GraphTargetItem constr = null;
        TypeItem thisType = pkg == null && classNameStr == null ? null : new TypeItem(pkg == null || pkg.isEmpty() ? new DottedChain(classNameStr) : pkg.add(classNameStr));
        List<AssignableAVM2Item> constrVariables = new ArrayList<>();
        List<Integer> originalOpenedNamespaces = openedNamespaces;
        int originalPrivateNs = privateNs;
        boolean inPkg = pkg != null;
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

            if (scriptTraits && s.type == SymbolType.PACKAGE) {
                if (inPkg) {
                    throw new AVM2ParseException("No subpackages allowed", lexer.yyline());
                }
                openedNamespaces = new ArrayList<>();
                lexer.pushback(s);
                PackageAVM2Item p = parsePackage(openedNamespaces);
                pkg = p.packageName;
                inPkg = true;
                publicNs = p.publicNs;
                importedClasses = p.importedClasses;
                s = lex();
            }

            List<Map.Entry<String, Map<String, String>>> metadata = new ArrayList<>();
            while (s.isType(SymbolType.BRACKET_OPEN)) {
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                String name = s.value.toString();
                Map.Entry<String, Map<String, String>> en = new AbstractMap.SimpleEntry<>(name, new HashMap<String, String>());
                s = lex();
                if (s.isType(SymbolType.PARENT_OPEN)) {
                    s = lex();
                    if (s.isType(SymbolGroup.STRING)) {
                        en.getValue().put("", s.value.toString());
                        s = lex();
                    } else {
                        lexer.pushback(s);
                        do {
                            s = lex();
                            expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                            String key = s.value.toString();
                            expectedType(SymbolType.ASSIGN);
                            s = lex();
                            expected(s, lexer.yyline(), SymbolGroup.STRING);
                            String value = s.value.toString();
                            en.getValue().put(key, value);
                            s = lex();
                        } while (s.isType(SymbolType.COMMA));
                    }
                    expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                    s = lex();
                }
                metadata.add(en);
                expected(s, lexer.yyline(), SymbolType.BRACKET_CLOSE);
                s = lex();
            }

            if (inPkg || classNameStr != null) {
                if (s.type == SymbolType.CURLY_OPEN) {
                    staticInitializer.addAll(commands(thisType, pkg, sinitNeedsActivation, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, false, 0, sinitVariables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    s = lex();
                }

                while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE, SymbolType.PROTECTED, SymbolType.OVERRIDE, SymbolType.FINAL, SymbolType.DYNAMIC, SymbolGroup.IDENTIFIER)) {
                    if (s.type == SymbolType.FINAL) {
                        if (isFinal) {
                            throw new AVM2ParseException("Only one final keyword allowed", lexer.yyline());
                        }
                        isFinal = true;
                    } else if (s.type == SymbolType.DYNAMIC) {
                        if (isDynamic) {
                            throw new AVM2ParseException("Only one dynamic keyword allowed", lexer.yyline());
                        }
                        isDynamic = true;
                    } else if (s.type == SymbolType.OVERRIDE) {
                        if (isOverride) {
                            throw new AVM2ParseException("Only one override keyword allowed", lexer.yyline());
                        }
                        isOverride = true;
                    } else if (s.type == SymbolType.STATIC) {
                        if (isInterface) {
                            throw new AVM2ParseException("Interface cannot have static traits", lexer.yyline());
                        }
                        if (classNameStr == null) {
                            throw new AVM2ParseException("No static keyword allowed here", lexer.yyline());
                        }
                        if (isStatic) {
                            throw new AVM2ParseException("Only one static keyword allowed", lexer.yyline());
                        }
                        isStatic = true;
                    } else if (s.type == SymbolType.NAMESPACE) {
                        break;
                    } else if (s.type == SymbolType.NATIVE) {
                        throw new AVM2ParseException("Cannot compile native code", lexer.yyline());
                    } else if (s.group == SymbolGroup.IDENTIFIER) {
                        customAccess = s.value.toString();
                        namespace = -2;
                    } else {
                        if (namespace != -1) {
                            throw new AVM2ParseException("Only one access identifier allowed", lexer.yyline());
                        }
                    }
                    switch (s.type) {
                        case PUBLIC:
                            namespace = publicNs;
                            if (isInterface) {
                                throw new AVM2ParseException("Interface cannot have public, private or protected modifier", lexer.yyline());
                            }
                            break;
                        case PRIVATE:
                            namespace = privateNs;
                            if (isInterface) {
                                throw new AVM2ParseException("Interface cannot have public, private or protected modifier", lexer.yyline());
                            }
                            break;
                        case PROTECTED:
                            namespace = protectedNs;
                            if (isInterface) {
                                throw new AVM2ParseException("Interface cannot have public, private or protected modifier", lexer.yyline());
                            }
                            break;
                    }
                    s = lex();
                }
            } else {
                namespace = privateNs;
            }
            if (namespace == -1) {
                if (isInterface) {
                    namespace = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_NAMESPACE, abc.constants.getStringId(pkg == null || pkg.isEmpty() ? classNameStr : pkg + ":" + classNameStr, true)), 0, true);
                } else {
                    namespace = packageInternalNs;
                }
            }
            if (namespace == protectedNs && isStatic) {
                namespace = protectedStaticNs;
            }
            switch (s.type) {
                /*case PACKAGE:
                 lexer.pushback(s);
                 traits.add(parsePackage(openedNamespaces));
                 break;*/
                case CLASS:
                    List<Integer> subNamespaces = new ArrayList<>(openedNamespaces);
                    if (classNameStr != null) {
                        throw new AVM2ParseException("Nested classes not supported", lexer.yyline());
                    }
                    if (isOverride) {
                        throw new AVM2ParseException("Override flag not allowed for classes", lexer.yyline());
                    }

                    //GraphTargetItem classTypeStr = type(thisType,pkg,needsActivation, importedClasses, openedNamespaces, variables);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String classTypeStr = s.value.toString();
                    GraphTargetItem extendsTypeStr = null;
                    s = lex();
                    if (s.type == SymbolType.EXTENDS) {
                        extendsTypeStr = type(thisType, pkg, new Reference<>(false), importedClasses, subNamespaces, new ArrayList<>());
                        s = lex();
                    }
                    List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                    if (s.type == SymbolType.IMPLEMENTS) {
                        do {
                            GraphTargetItem implementsTypeStr = type(thisType, pkg, new Reference<>(false), importedClasses, subNamespaces, new ArrayList<>());
                            implementsTypeStrs.add(implementsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    if (customAccess != null) {
                        throw new AVM2ParseException("Class cannot have custom namespace", lexer.yyline());
                    }
                    traits.add((classTraits(metadata, scriptName, publicNs, pkg, importedClasses, isDynamic, isFinal, subNamespaces, pkg, namespace, false, classTypeStr, extendsTypeStr, implementsTypeStrs, new ArrayList<>())));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                case INTERFACE:
                    if (classNameStr != null) {
                        throw new AVM2ParseException("Nested interfaces not supported", lexer.yyline());
                    }
                    if (isOverride) {
                        throw new AVM2ParseException("Override flag not allowed for interfaces", lexer.yyline());
                    }
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for interfaces", lexer.yyline());
                    }
                    if (isDynamic) {
                        throw new AVM2ParseException("Dynamic flag not allowed for interfaces", lexer.yyline());
                    }
                    //GraphTargetItem interfaceTypeStr = type(thisType,pkg,needsActivation, importedClasses, openedNamespaces, variables);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String intTypeStr = s.value.toString();
                    s = lex();
                    List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                    if (s.type == SymbolType.EXTENDS) {
                        do {
                            GraphTargetItem intExtendsTypeStr = type(thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>());
                            intExtendsTypeStrs.add(intExtendsTypeStr);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    if (customAccess != null) {
                        throw new AVM2ParseException("Interface cannot have custom namespace", lexer.yyline());
                    }
                    traits.add((classTraits(metadata, scriptName, publicNs, pkg, importedClasses, false, isFinal, openedNamespaces, pkg, namespace, true, intTypeStr, null, intExtendsTypeStrs, new ArrayList<>())));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;

                case FUNCTION:

                    if (isDynamic) {
                        throw new AVM2ParseException("Dynamic flag not allowed for methods", lexer.yyline());
                    }
                    s = lex();
                    if (s.type == SymbolType.GET) {
                        if (classNameStr == null) {
                            throw new AVM2ParseException("No get keyword allowed here", lexer.yyline());
                        }
                        isGetter = true;
                        s = lex();
                    } else if (s.type == SymbolType.SET) {
                        if (classNameStr == null) {
                            throw new AVM2ParseException("No set keyword allowed here", lexer.yyline());
                        }
                        isSetter = true;
                        s = lex();
                    }

                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String fname = s.value.toString();
                    if (classNameStr != null && fname.equals(classNameStr)) { //constructor
                        if (isStatic) {
                            throw new AVM2ParseException("Constructor cannot be static", lexer.yyline());
                        }
                        if (isStatic) {
                            throw new AVM2ParseException("Constructor cannot be static", lexer.yyline());
                        }
                        if (isOverride) {
                            throw new AVM2ParseException("Override flag not allowed for constructor", lexer.yyline());
                        }
                        if (isFinal) {
                            throw new AVM2ParseException("Final flag not allowed for constructor", lexer.yyline());
                        }
                        if (isInterface) {
                            throw new AVM2ParseException("Interface cannot have constructor", lexer.yyline());
                        }
                        constr = (method(metadata, pkg, false, customAccess, new Reference<>(false), importedClasses, false, false, thisType, openedNamespaces, false, namespace, "", true, constrVariables));
                    } else {
                        MethodAVM2Item ft = method(metadata, pkg, isInterface, customAccess, new Reference<>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, namespace, fname, true, new ArrayList<>());

                        if (isGetter) {
                            if (!ft.paramTypes.isEmpty()) {
                                throw new AVM2ParseException("Getter can't have any parameters", lexer.yyline());
                            }
                        }

                        if (isSetter) {
                            if (ft.paramTypes.size() != 1) {
                                throw new AVM2ParseException("Getter must have exactly one parameter", lexer.yyline());
                            }
                        }

                        if (isStatic && isInterface) {
                            if (isInterface) {
                                throw new AVM2ParseException("Interface cannot have static fields", lexer.yyline());
                            }
                        }
                        GraphTargetItem t;
                        if (isGetter) {
                            GetterAVM2Item g = new GetterAVM2Item(ft.metadata, ft.pkg, isInterface, customAccess, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.namespace, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                            t = g;
                        } else if (isSetter) {
                            SetterAVM2Item st = new SetterAVM2Item(ft.metadata, ft.pkg, isInterface, customAccess, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.namespace, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                            t = st;
                        } else {
                            t = ft;
                        }

                        traits.add(t);
                    }
                    //}
                    break;
                case NAMESPACE:
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have namespace fields", lexer.yyline());
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String nname = s.value.toString();
                    String nval = "";
                    s = lex();

                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        nval = s.value.toString();
                        s = lex();
                    } else {
                        nval = (pkg == null || pkg.isEmpty() ? classNameStr : pkg + ":" + classNameStr) + "/" + nname;
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    ConstAVM2Item ns = new ConstAVM2Item(metadata, pkg, customAccess, true, namespace, nname, new TypeItem(DottedChain.NAMESPACE), new StringAVM2Item(null, nval), lexer.yyline());
                    traits.add(ns);
                    break;
                case CONST:
                case VAR:
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isOverride) {
                        throw new AVM2ParseException("Override flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }
                    if (isDynamic) {
                        throw new AVM2ParseException("Dynamic flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have variable/const fields", lexer.yyline());
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();
                    GraphTargetItem type = null;
                    if (s.type == SymbolType.COLON) {
                        type = type(thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>());
                        s = lex();
                    } else {
                        type = TypeItem.UNBOUNDED;
                    }

                    GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        value = expression(thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, isStatic || isConst ? sinitVariables : constrVariables);
                        s = lex();
                    }
                    GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(metadata, pkg, customAccess, isStatic, namespace, vcname, type, value, lexer.yyline());
                    } else {
                        tar = new SlotAVM2Item(metadata, pkg, customAccess, isStatic, namespace, vcname, type, value, lexer.yyline());
                    }
                    traits.add(tar);
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    if (s.type == SymbolType.CURLY_CLOSE && inPkg && classNameStr == null) {
                        inPkg = false;
                        pkg = null;
                        openedNamespaces = originalOpenedNamespaces;
                        privateNs = originalPrivateNs;
                    } else {
                        lexer.pushback(s);
                        break looptrait;
                    }

            }
        }
        return constr;
    }

    private GraphTargetItem classTraits(List<Map.Entry<String, Map<String, String>>> metadata, String scriptName, int gpublicNs, DottedChain pkg, List<DottedChain> importedClasses, boolean isDynamic, boolean isFinal, List<Integer> openedNamespaces, DottedChain packageName, int namespace, boolean isInterface, String nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException, CompilationException {

        GraphTargetItem ret = null;

        ParsedSymbol s = null;
        List<GraphTargetItem> traits = new ArrayList<>();

        String classNameStr = nameStr;

        openedNamespaces = new ArrayList<>(openedNamespaces);

        int publicNs = 0;
        int privateNs = 0;
        int packageInternalNs = 0;
        if (pkg != null) {
            openedNamespaces.add(packageInternalNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE_INTERNAL, abc.constants.getStringId(pkg, true)), 0, true));
        }
        if (pkg != null && !pkg.isEmpty()) {
            openedNamespaces.add(publicNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("", true)), 0, true));
        } else {
            publicNs = gpublicNs;
        }

        openedNamespaces.add(privateNs = abc.constants.addNamespace(new Namespace(Namespace.KIND_PRIVATE, 0))); //abc.constants.getStringId(fileName + "$", true)

        openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_NAMESPACE, abc.constants.getStringId(AS3_NAMESPACE, true)), 0, true));

        //int privateNs = 0;
        int protectedNs = 0;
        //int publicNs = namespace;
        int protectedStaticNs = 0;

        openedNamespaces.add(protectedNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PROTECTED, abc.constants.getStringId(packageName == null ? (scriptName + "$0:"/*FIXME?*/ + classNameStr) : packageName.isEmpty() ? classNameStr : packageName.toRawString() + ":" + classNameStr, true)), 0, true));
        openedNamespaces.add(protectedStaticNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_STATIC_PROTECTED, abc.constants.getStringId(packageName == null || packageName.isEmpty() ? classNameStr : packageName.toRawString() + ":" + classNameStr, true)), 0, true));

        if (extendsStr != null) {
            List<Integer> indices = new ArrayList<>();
            List<String> names = new ArrayList<>();
            List<String> namespaces = new ArrayList<>();
            //FIXME for Private classes in script!!!
            AVM2SourceGenerator.parentNamesAddNames(abc, otherABCs, AVM2SourceGenerator.resolveType(new SourceGeneratorLocalData(new HashMap<>(), 0, false, 0), ((TypeItem) ((UnresolvedAVM2Item) extendsStr).resolve(null, new ArrayList<>(), new ArrayList<>(), abc, otherABCs, new ArrayList<>(), new ArrayList<>())), abc, otherABCs), indices, names, namespaces);
            for (int i = 0; i < names.size(); i++) {
                if (namespaces.get(i).isEmpty()) {
                    continue;
                }
                openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_STATIC_PROTECTED, abc.constants.getStringId(namespaces.get(i) + ":" + names.get(i), true)), 0, true));
            }
        }

        Reference<Boolean> staticNeedsActivation = new Reference<>(false);
        List<GraphTargetItem> staticInit = new ArrayList<>();
        List<AssignableAVM2Item> sinitVariables = new ArrayList<>();
        GraphTargetItem constr = traits(scriptName, false, sinitVariables, staticNeedsActivation, staticInit, importedClasses, privateNs, protectedNs, publicNs, packageInternalNs, protectedStaticNs, openedNamespaces, packageName, classNameStr, isInterface, traits);

        if (isInterface) {
            return new InterfaceAVM2Item(metadata, importedClasses, packageName, openedNamespaces, isFinal, namespace, classNameStr, implementsStr, traits);
        } else {
            return new ClassAVM2Item(metadata, importedClasses, packageName, openedNamespaces, protectedNs, isDynamic, isFinal, namespace, classNameStr, extendsStr, implementsStr, staticInit, staticNeedsActivation.getVal(), sinitVariables, constr, traits);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            /*case INT:
             expectedType(SymbolType.PARENT_OPEN);
             ret = new ToIntegerAVM2Item(null, expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             break;
             case NUMBER_OP:
             s = lex();
             if (s.type == SymbolType.DOT) {
             VariableAVM2Item vi = new VariableAVM2Item(s.value.toString(), null, false);
             variables.add(vi);
             ret = memberOrCall(thisType,vi, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToNumberAVM2Item(null, expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
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
             ret = memberOrCall(thisType,vi2, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToStringAVM2Item(null, expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             ret = memberOrCall(thisType,ret, registerVars, inFunction, inMethod, variables);
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

    private List<GraphTargetItem> xmltag(TypeItem thisType, DottedChain pkg, Reference<Boolean> usesVars, List<String> openedTags, Reference<Integer> closedVarTags, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
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
                    rets.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_ATTRVALVAR_BEGIN: //esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    addS(rets, sb);
                    rets.add(new EscapeXAttrAVM2Item(null, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_INSTRATTRNAMEVAR_BEGIN: //add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_INSTRATTRVALVAR_BEGIN: //esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    addS(rets, sb);
                    rets.add(new EscapeXAttrAVM2Item(null, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_VAR_BEGIN: //esc_xelem
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(new EscapeXElemAVM2Item(null, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_FINISHVARTAG_BEGIN: //add
                    usesVars.setVal(true);
                    closedVarTags.setVal(closedVarTags.getVal() + 1);
                    sb.append("</");
                    addS(rets, sb);

                    rets.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.GREATER_THAN);
                    sb.append(">");
                    addS(rets, sb);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_STARTVARTAG_BEGIN: //add
                    //openedTags.add("*");

                    //ret = add(ret, );
                    GraphTargetItem ex = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    sub.add("*");
                    sb.append("<");
                    addS(rets, sb);
                    rets.add(ex);
                    rets.addAll(xmltag(thisType, pkg, subusesvars, sub, subclose, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    closedVarTags.setVal(subclose.getVal() + subclose.getVal());
                    break;
                case XML_INSTRVARTAG_BEGIN: //add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    sb.append("<?");
                    addS(rets, sb);
                    rets.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLINSTROPENTAG);
                    break;
                case XML_STARTTAG_BEGIN:
                    sub.add(s.value.toString().trim().substring(1)); //remove < from beginning
                    List<GraphTargetItem> st = xmltag(thisType, pkg, subusesvars, sub, closedVarTags, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
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
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline());
                    }
                    sb.append(s.value.toString());
                    break;
                case XML_STARTFINISHTAG_END:
                    openedTags.remove(openedTags.size() - 1); //close last tag
                    sb.append(s.value.toString());
                    break;
                case EOF:
                    throw new AVM2ParseException("End of file before XML finish", lexer.yyline());
                default:
                    sb.append(s.value.toString());
                    break;
            }
        } while (!(openedTags.isEmpty() || closedVarTags.getVal() >= openedTags.size()));
        addS(rets, sb);
        return rets;
    }

    private GraphTargetItem xml(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        List<String> openedTags = new ArrayList<>();
        int closedVarTags = 0;

        GraphTargetItem ret = add(xmltag(thisType, pkg, new Reference<>(false), openedTags, new Reference<>(closedVarTags), needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
        ret = new XMLAVM2Item(ret);
        lexer.yybegin(ActionScriptLexer.YYINITIAL);
        //TODO: Order of additions as in official compiler
        return ret;
    }

    private GraphTargetItem command(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
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

        if (s.group == SymbolGroup.IDENTIFIER) {
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
            if (sx.group != SymbolGroup.IDENTIFIER) {
                lexer.pushback(sx);
            } else {
                if (!sx.value.equals("xml")) {
                    lexer.pushback(sx);
                } else {
                    expectedType(SymbolType.NAMESPACE);
                    expectedType(SymbolType.ASSIGN);
                    GraphTargetItem ns = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    ret = new DefaultXMLNamespace(null, ns);
                    //TODO: use dxns for attribute namespaces instead of dxnslate
                }
            }
        }
        if (ret == null) {
            switch (s.type) {
                case USE:
                    expectedType(SymbolType.NAMESPACE);
                    GraphTargetItem ns = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);
                    openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE /*FIXME?*/, abc.constants.getStringId(ns.toString(), true)), 0, true));
                    break;
                case WITH:
                    needsActivation.setVal(true);
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem wvar = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//(name(thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables));
                    if (!isNameOrProp(wvar)) {
                        throw new AVM2ParseException("Not a property or name", lexer.yyline());
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    List<AssignableAVM2Item> withVars = new ArrayList<>();
                    List<GraphTargetItem> wcmd = commands(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, withVars);
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
                 GraphTargetItem varDel = expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
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
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    needsActivation.setVal(true);
                    ret = (function(new ArrayList<Map.Entry<String, Map<String, String>>>(), pkg, false, needsActivation, importedClasses, 0/*?*/, thisType, openedNamespaces, s.value.toString(), false, variables));
                    break;
                case VAR:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String varIdentifier = s.value.toString();
                    s = lex();
                    GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        type = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);
                        s = lex();
                    } else {
                        type = new UnboundedTypeItem();
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        GraphTargetItem varval = (expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                        ret = new NameAVM2Item(type, lexer.yyline(), varIdentifier, varval, true, openedNamespaces);
                        variables.add((NameAVM2Item) ret);
                    } else {
                        ret = new NameAVM2Item(type, lexer.yyline(), varIdentifier, null, true, openedNamespaces);
                        variables.add((NameAVM2Item) ret);
                        lexer.pushback(s);
                    }
                    break;
                case CURLY_OPEN:
                    ret = new BlockItem(null, commands(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                /*case INCREMENT: //preincrement
                 case DECREMENT: //predecrement
                 GraphTargetItem varincdec = expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
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
                        List<GraphTargetItem> args = call(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
                        ret = new ConstructSuperAVM2Item(null, new LocalRegAVM2Item(null, 0, null), args);
                    } else {//no costructor call, but it could be calling parent methods... => handle in expression
                        lexer.pushback(ss2);
                        lexer.pushback(s);
                    }
                    break;
                case IF:
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem ifExpr = (expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    GraphTargetItem onTrue = command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                    List<GraphTargetItem> onTrueList = new ArrayList<>();
                    onTrueList.add(onTrue);
                    s = lex();
                    List<GraphTargetItem> onFalseList = null;
                    if (s.type == SymbolType.ELSE) {
                        onFalseList = new ArrayList<>();
                        onFalseList.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    } else {
                        lexer.pushback(s);
                    }
                    ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                    break;
                case WHILE:
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> whileExpr = new ArrayList<>();
                    whileExpr.add(commaExpression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> whileBody = new ArrayList<>();
                    Loop wloop = new Loop(uniqId(), null, null);
                    if (loopLabel != null) {
                        loopLabels.put(wloop, loopLabel);
                    }
                    loops.push(wloop);
                    whileBody.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    ret = new WhileItem(null, wloop, whileExpr, whileBody);
                    break;
                case DO:
                    List<GraphTargetItem> doBody = new ArrayList<>();
                    Loop dloop = new Loop(uniqId(), null, null);
                    loops.push(dloop);
                    if (loopLabel != null) {
                        loopLabels.put(dloop, loopLabel);
                    }
                    doBody.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    expectedType(SymbolType.WHILE);
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> doExpr = new ArrayList<>();
                    doExpr.add(commaExpression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables));
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
                    GraphTargetItem firstCommand = command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, false, variables);
                    if (firstCommand instanceof NameAVM2Item) {
                        NameAVM2Item nai = (NameAVM2Item) firstCommand;
                        if (nai.isDefinition() && nai.getAssignedValue() == null) { //Declared value in for..in
                            firstCommand = expression1(firstCommand, GraphTargetItem.NOPRECEDENCE, thisType, pkg, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables);
                        }
                    }
                    InAVM2Item inexpr = null;
                    if (firstCommand instanceof InAVM2Item) {
                        forin = true;
                        inexpr = (InAVM2Item) firstCommand;
                    } else {
                        if (forin) {
                            throw new AVM2ParseException("In expression required", lexer.yyline());
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
                        //GraphTargetItem firstCommand = command(thisType,pkg,needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                        if (firstCommand != null) { //can be empty command
                            forFirstCommands.add(firstCommand);
                        }
                        forExpr = (expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                        expectedType(SymbolType.SEMICOLON);
                        GraphTargetItem fcom = command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                        if (fcom != null) {
                            forFinalCommands.add(fcom);
                        }
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> forBody = new ArrayList<>();
                    forBody.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables));
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
                    GraphTargetItem switchExpr = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
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
                            GraphTargetItem curCaseExpr = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                            caseExprs.add(curCaseExpr);
                            expectedType(SymbolType.COLON);
                            s = lex();
                            caseExprsAll.add(curCaseExpr);
                            valueMapping.add(pos);
                        }
                        pos++;
                        lexer.pushback(s);
                        List<GraphTargetItem> caseCmd = commands(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                        caseCmds.add(caseCmd);
                        s = lex();
                    }
                    List<GraphTargetItem> defCmd = new ArrayList<>();
                    if (s.type == SymbolType.DEFAULT) {
                        expectedType(SymbolType.COLON);
                        defCmd = commands(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables);
                        s = lexer.lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    ret = new SwitchItem(null, sloop, switchExpr, caseExprsAll, caseCmds, defCmd, valueMapping);
                    break;
                case BREAK:
                    s = lex();
                    long bloopId = 0;
                    if (loops.isEmpty()) {
                        throw new AVM2ParseException("No loop to break", lexer.yyline());
                    }
                    if (s.group == SymbolGroup.IDENTIFIER) {
                        String breakLabel = s.value.toString();
                        for (Loop l : loops) {
                            if (breakLabel.equals(loopLabels.get(l))) {
                                bloopId = l.id;
                                break;
                            }
                        }
                        if (bloopId == 0) {
                            throw new AVM2ParseException("Identifier of loop expected", lexer.yyline());
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
                        throw new AVM2ParseException("No loop to continue", lexer.yyline());
                    }
                    if (s.group == SymbolGroup.IDENTIFIER) {
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
                            throw new AVM2ParseException("Identifier of loop expected", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                        for (int i = loops.size() - 1; i >= 0; i--) {
                            if (loops.get(i).id >= 0) {//no switches
                                cloopId = loops.get(i).id;
                                break;
                            }
                        }
                        if (cloopId <= 0) {
                            throw new AVM2ParseException("No loop to continue", lexer.yyline());
                        }
                    }
                    //TODO: handle switch
                    ret = new ContinueItem(null, cloopId);
                    break;
                case RETURN:
                    GraphTargetItem retexpr = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables);
                    if (retexpr == null) {
                        ret = new ReturnVoidAVM2Item(null);
                    } else {
                        ret = new ReturnValueAVM2Item(null, retexpr);
                    }
                    break;
                case TRY:
                    needsActivation.setVal(true);
                    List<GraphTargetItem> tryCommands = new ArrayList<>();
                    tryCommands.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                    s = lex();
                    boolean found = false;
                    List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
                    List<NameAVM2Item> catchExceptions = new ArrayList<>();
                    int varCnt = variables.size();
                    List<List<AssignableAVM2Item>> catchesVars = new ArrayList<>();
                    while (s.type == SymbolType.CATCH) {
                        expectedType(SymbolType.PARENT_OPEN);
                        s = lex();
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);

                        String enamestr = s.value.toString();
                        expectedType(SymbolType.COLON);
                        GraphTargetItem etype = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);
                        NameAVM2Item e = new NameAVM2Item(etype, lexer.yyline(), enamestr, new ExceptionAVM2Item(null)/*?*/, true/*?*/, openedNamespaces);
                        variables.add(e);
                        catchExceptions.add(e);
                        e.setSlotNumber(1);
                        e.setSlotScope(Integer.MAX_VALUE); //will be changed later
                        expectedType(SymbolType.PARENT_CLOSE);
                        List<GraphTargetItem> cc = new ArrayList<>();
                        List<AssignableAVM2Item> catchVars = new ArrayList<>();
                        cc.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, catchVars));
                        catchesVars.add(catchVars);
                        variables.addAll(catchVars);

                        for (AssignableAVM2Item a : catchVars) {
                            if (a instanceof UnresolvedAVM2Item) {
                                UnresolvedAVM2Item ui = (UnresolvedAVM2Item) a;
                                if (ui.getVariableName().equals(e.getVariableName())) {
                                    try {
                                        ui.resolve(null, new ArrayList<>(), new ArrayList<>(), abc, otherABCs, new ArrayList<>(), variables);
                                    } catch (CompilationException ex) {
                                        // ignore
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
                                        ui.resolve(null, new ArrayList<>(), new ArrayList<>(), abc, otherABCs, new ArrayList<>(), variables);
                                    } catch (CompilationException ex) {
                                        // ignore
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
                        finallyCommands.add(command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables));
                        found = true;
                        s = lex();
                    }
                    if (!found) {
                        expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                    }
                    lexer.pushback(s);
                    TryAVM2Item tai = new TryAVM2Item(tryCommands, null, catchCommands, finallyCommands, "");
                    tai.catchVariables = catchesVars;
                    tai.catchExceptions2 = catchExceptions;
                    ret = tai;
                    break;
                case THROW:
                    ret = new ThrowAVM2Item(null, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
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
                    ret = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
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
            ret = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        return expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, allowRemainder, variables);
    }

    private GraphTargetItem fixPrecedence(GraphTargetItem expr) {
        System.out.println("Fixing " + expr);
        GraphTargetItem ret = expr;

        /*
         fix > :
         a || b > c   =>   a || (b > c)

         a < 0 || (b > c) + 1

         */
        if (expr instanceof BinaryOp) {
            BinaryOp bo = (BinaryOp) expr;
            GraphTargetItem left = bo.getLeftSide();
            GraphTargetItem right = bo.getRightSide();
            if (left.getPrecedence() > bo.getPrecedence()) {
                if (left instanceof BinaryOp) {
                    BinaryOp leftBo = (BinaryOp) left;
                    bo.setLeftSide(leftBo.getRightSide());
                    leftBo.setRightSide(expr);
                    System.out.println("fixed");
                    return left;
                }
            }
        }
        return ret;
    }

    /*private GraphTargetItem expressionRemainder(TypeItem thisType, String pkg, Reference<Boolean> needsActivation, List<Integer> openedNamespaces, GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, List<DottedChain> importedClasses) throws IOException, AVM2ParseException {
     GraphTargetItem ret = null;
     ParsedSymbol s = lex();

     ret = fixPrecedence(ret);
     return ret;
     }*/
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
        if (item instanceof IndexAVM2Item) {
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

    private int brackets(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
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

    private GraphTargetItem commaExpression(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forInLevel, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forInLevel, false, variables);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new AVM2ParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expression(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        GraphTargetItem prim = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables);
        if (prim == null) {
            return null;
        }
        return expression1(prim, GraphTargetItem.NOPRECEDENCE, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables);
    }

    /**
     * Lexer can return XML opentags instead of greater. In expression, we need
     * greater sign only
     *
     * @param symb
     */
    private void xmlToGreaterFix(ParsedSymbol symb) {
        if (symb.isType(SymbolType.XML_STARTVARTAG_BEGIN, SymbolType.XML_STARTTAG_BEGIN)) {
            lexer.yypushbackstr(symb.value.toString().substring(1)); //parse again as GREATER_THAN
            symb.type = SymbolType.GREATER_THAN;
            symb.group = SymbolGroup.OPERATOR;
        }
    }

    private ParsedSymbol peekExprToken() throws IOException, AVM2ParseException {
        ParsedSymbol lookahead = lex();
        xmlToGreaterFix(lookahead);

        lexer.pushback(lookahead);
        return lookahead;
    }

    private GraphTargetItem expression1(GraphTargetItem lhs, int min_precedence, TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        if (debugMode) {
            System.out.println("expression1:");
        }
        ParsedSymbol lookahead = peekExprToken();

        ParsedSymbol op;
        GraphTargetItem rhs;
        GraphTargetItem mhs = null;

        //Note: algorithm from http://en.wikipedia.org/wiki/Operator-precedence_parser
        //with relation operators reversed as we have precedence in reverse order
        while (lookahead.type.isBinary() && lookahead.type.getPrecedence() <= /* >= on wiki */ min_precedence) {
            op = lookahead;
            lex();

            //Note: Handle ternar operator as Binary
            //http://stackoverflow.com/questions/13681293/how-can-i-incorporate-ternary-operators-into-a-precedence-climbing-algorithm
            if (op.type == SymbolType.TERNAR) {
                if (debugMode) {
                    System.out.println("ternar-middle:");
                }
                mhs = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);
                expectedType(SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables);
            if (rhs == null) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekExprToken();
            while ((lookahead.type.isBinary() && lookahead.type.getPrecedence() < /* > on wiki */ op.type.getPrecedence())
                    || (lookahead.type.isRightAssociative() && lookahead.type.getPrecedence() == op.type.getPrecedence())) {
                rhs = expression1(rhs, lookahead.type.getPrecedence(), thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables);
                lookahead = peekExprToken();
            }

            switch (op.type) {
                case AS:
                    //GraphTargetItem type = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);

                    lhs = new AsTypeAVM2Item(null, lhs, rhs); //???
                    allowRemainder = false;
                    break;

                case IN:
                    lhs = new InAVM2Item(null, lhs, rhs);
                    break;

                case TERNAR: //???
                    lhs = new TernarOpItem(null, lhs, mhs, rhs);
                    break;
                case SHIFT_LEFT:
                    lhs = new LShiftAVM2Item(null, lhs, rhs);
                    break;
                case SHIFT_RIGHT:
                    lhs = new RShiftAVM2Item(null, lhs, rhs);
                    break;
                case USHIFT_RIGHT:
                    lhs = new URShiftAVM2Item(null, lhs, rhs);
                    break;
                case BITAND:
                    lhs = new BitAndAVM2Item(null, lhs, rhs);
                    break;
                case BITOR:
                    lhs = new BitOrAVM2Item(null, lhs, rhs);
                    break;
                case DIVIDE:
                    lhs = new DivideAVM2Item(null, lhs, rhs);
                    break;
                case MODULO:
                    lhs = new ModuloAVM2Item(null, lhs, rhs);
                    break;
                case EQUALS:
                    lhs = new EqAVM2Item(null, lhs, rhs);
                    break;
                case STRICT_EQUALS:
                    lhs = new StrictEqAVM2Item(null, lhs, rhs);
                    break;
                case NOT_EQUAL:
                    lhs = new NeqAVM2Item(null, lhs, rhs);
                    break;
                case STRICT_NOT_EQUAL:
                    lhs = new StrictNeqAVM2Item(null, lhs, rhs);
                    break;
                case LOWER_THAN:
                    lhs = new LtAVM2Item(null, lhs, rhs);
                    break;
                case LOWER_EQUAL:
                    lhs = new LeAVM2Item(null, lhs, rhs);
                    break;
                case GREATER_THAN:
                    lhs = new GtAVM2Item(null, lhs, rhs);
                    break;
                case GREATER_EQUAL:
                    lhs = new GeAVM2Item(null, lhs, rhs);
                    break;
                case AND:
                    lhs = new AndItem(null, lhs, rhs);
                    break;
                case OR:
                    lhs = new OrItem(null, lhs, rhs);
                    break;
                case MINUS:
                    lhs = new SubtractAVM2Item(null, lhs, rhs);
                    break;
                case MULTIPLY:
                    lhs = new MultiplyAVM2Item(null, lhs, rhs);
                    break;
                case PLUS:
                    lhs = new AddAVM2Item(null, lhs, rhs);
                    break;
                case XOR:
                    lhs = new BitXorAVM2Item(null, lhs, rhs);
                    break;
                case INSTANCEOF:
                    lhs = new InstanceOfAVM2Item(null, lhs, rhs);
                    break;
                case IS:
                    GraphTargetItem istype = rhs;//type(thisType,pkg,needsActivation, importedClasses, openedNamespaces, variables);
                    lhs = new IsTypeAVM2Item(null, lhs, istype);
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
                    GraphTargetItem assigned = rhs;
                    switch (op.type) {
                        case ASSIGN:
                            //assigned = assigned;
                            break;
                        case ASSIGN_BITAND:
                            assigned = new BitAndAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_BITOR:
                            assigned = new BitOrAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_DIVIDE:
                            assigned = new DivideAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_MINUS:
                            assigned = new SubtractAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_MODULO:
                            assigned = new ModuloAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_MULTIPLY:
                            assigned = new MultiplyAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_PLUS:
                            assigned = new AddAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_LEFT:
                            assigned = new LShiftAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_RIGHT:
                            assigned = new RShiftAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_USHIFT_RIGHT:
                            assigned = new URShiftAVM2Item(null, lhs, assigned);
                            break;
                        case ASSIGN_XOR:
                            assigned = new BitXorAVM2Item(null, lhs, assigned);
                            break;
                    }

                    if (!(lhs instanceof AssignableAVM2Item)) {
                        throw new AVM2ParseException("Invalid assignment", lexer.yyline());
                    }
                    AssignableAVM2Item as = ((AssignableAVM2Item) lhs).copy();
                    if ((as instanceof UnresolvedAVM2Item) || (as instanceof NameAVM2Item)) {
                        variables.add(as);
                    }
                    as.setAssignedValue(assigned);
                    if (lhs instanceof NameAVM2Item) {
                        ((NameAVM2Item) lhs).setDefinition(false);
                    }
                    lhs = as;
                    break;
                case DESCENDANTS:
                    expected(lookahead, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                    lookahead = lex();
                    lhs = new GetDescendantsAVM2Item(lhs, lookahead.type == SymbolType.MULTIPLY ? null : lookahead.value.toString(), openedNamespaces);
                    allowRemainder = true;
                    break;
            }
        }
        if (lhs instanceof ParenthesisItem) {
            GraphTargetItem coerced = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);
            if (coerced != null && isType(((ParenthesisItem) lhs).value)) {
                lhs = new CoerceAVM2Item(null, ((ParenthesisItem) lhs).value, coerced);
            }
        }

        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private GraphTargetItem expressionPrimary(TypeItem thisType, DottedChain pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<Integer> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        if (debugMode) {
            System.out.println("primary:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean allowMemberOrCall = false;
        switch (s.type) {
            case XML_STARTTAG_BEGIN:
                lexer.pushback(s);
                ret = xml(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables);
                break;
            case STRING:
                ret = new StringAVM2Item(null, s.value.toString());
                allowMemberOrCall = true;
                break;
            case NEGATE:
                ret = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables);
                ret = new NegAVM2Item(null, ret);

                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new FloatValueAVM2Item(null, -(Double) s.value);

                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new IntegerValueAVM2Item(null, -(Long) s.value);

                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables);
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
                ret = new TypeOfAVM2Item(null, expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables));
                break;
            case TRUE:
                ret = new BooleanAVM2Item(null, true);

                break;
            case NULL:
                ret = new NullAVM2Item(null);

                break;
            case UNDEFINED:
                ret = new UndefinedAVM2Item(null);
                break;
            case FALSE:
                ret = new BooleanAVM2Item(null, false);

                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                List<NameValuePair> nvs = new ArrayList<>();

                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.STRING);

                    GraphTargetItem n = new StringAVM2Item(null, s.value.toString());
//expression(thisType,pkg,needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);
                    expectedType(SymbolType.COLON);
                    GraphTargetItem v = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables);

                    NameValuePair nv = new NameValuePair(n, v);
                    nvs.add(nv);
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new NewObjectAVM2Item(null, nvs);
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(thisType, pkg, needsActivation, importedClasses, openedNamespaces, inBrackets, registerVars, inFunction, inMethod, variables);
                ret = new NewArrayAVM2Item(null, inBrackets);

                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                if (s.isType(SymbolGroup.IDENTIFIER)) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                needsActivation.setVal(true);
                ret = function(new ArrayList<>(), pkg, false, needsActivation, importedClasses, 0/*?*/, thisType, openedNamespaces, fname, false, variables);
                allowMemberOrCall = true;
                break;
            case NAN:
                ret = new NanAVM2Item(null);

                break;
            case INFINITY:
                ret = new FloatValueAVM2Item(null, Double.POSITIVE_INFINITY);

                break;
            case INTEGER:
                ret = new IntegerValueAVM2Item(null, (Long) s.value);

                break;
            case DOUBLE:
                ret = new FloatValueAVM2Item(null, (Double) s.value);

                break;
            case DELETE:
                GraphTargetItem varDel = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables);//name(thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                if (!isNameOrProp(varDel)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }
                ret = new DeletePropertyAVM2Item(varDel, lexer.yyline());
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem varincdec = expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false/*?*/, variables);//name(thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                if (!isNameOrProp(varincdec)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, varincdec);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, varincdec);
                }

                break;
            case NOT:
                ret = new NotItem(null, expressionPrimary(thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables));

                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                allowMemberOrCall = true;
                break;
            case NEW:
                s = lex();
                if (s.type == SymbolType.XML_STARTTAG_BEGIN) {
                    lexer.yypushbackstr(s.value.toString().substring(1), ActionScriptLexer.YYINITIAL);
                    s = new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.LOWER_THAN);
                }
                if (s.type == SymbolType.FUNCTION) {
                    s = lexer.lex();
                    String ffname = "";
                    if (s.isType(SymbolGroup.IDENTIFIER)) {
                        ffname = s.value.toString();
                    } else {
                        lexer.pushback(s);
                    }
                    needsActivation.setVal(true);
                    ret = function(new ArrayList<Map.Entry<String, Map<String, String>>>(), pkg, false, needsActivation, importedClasses, 0/*?*/, thisType, openedNamespaces, ffname, false, variables);
                } else if (s.type == SymbolType.LOWER_THAN) {
                    GraphTargetItem subtype = type(thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);
                    expectedType(SymbolType.GREATER_THAN);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.BRACKET_OPEN);
                    lexer.pushback(s);
                    List<GraphTargetItem> params = new ArrayList<>();
                    brackets(thisType, pkg, needsActivation, importedClasses, openedNamespaces, params, registerVars, inFunction, inMethod, variables);
                    ret = new InitVectorAVM2Item(subtype, params, openedNamespaces);
                } else if (s.type == SymbolType.PARENT_OPEN) {
                    GraphTargetItem newvar = expression(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);
                    newvar = applyType(thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables);
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.PARENT_OPEN);
                    ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, call(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));

                } else {
                    lexer.pushback(s);
                    GraphTargetItem newvar = name(thisType, pkg, needsActivation, false /*?*/, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses);
                    newvar = applyType(thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables);
                    expectedType(SymbolType.PARENT_OPEN);
                    ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, call(thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables));
                }
                allowMemberOrCall = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
            case ATTRIBUTE:
                lexer.pushback(s);
                ret = name(thisType, pkg, needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses);
                allowMemberOrCall = true;

                //var = memberOrCall(thisType, pkg, needsActivation, importedClasses, openedNamespaces, var, registerVars, inFunction, inMethod, variables);
                //ret = var;
                break;
            default:
                GraphTargetItem excmd = expressionCommands(s, registerVars, inFunction, inMethod, -1, variables);
                if (excmd != null) {
                    //?
                    ret = excmd;
                    allowMemberOrCall = true; //?
                    break;
                }
                lexer.pushback(s);
        }
        if (allowMemberOrCall && ret != null) {
            ret = memberOrCall(thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private ActionScriptLexer lexer = null;

    private List<String> constantPool;

    private PackageAVM2Item parsePackage(List<Integer> openedNamespaces) throws IOException, AVM2ParseException, CompilationException {
        List<GraphTargetItem> items = new ArrayList<>();
        expectedType(SymbolType.PACKAGE);
        DottedChain name = DottedChain.EMPTY;
        ParsedSymbol s = lex();
        if (s.type != SymbolType.CURLY_OPEN) {
            expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
            name = name.add(s.value.toString());
            s = lex();
        }
        while (s.type != SymbolType.CURLY_OPEN) {
            expected(s, lexer.yyline(), SymbolType.DOT);
            s = lex();
            expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
            name.add(s.value.toString());
            s = lex();
        }

        List<DottedChain> importedClasses = new ArrayList<>();

        s = lex();
        while (s.type == SymbolType.IMPORT) {
            boolean all = false;
            s = lex();
            expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
            DottedChain imp = new DottedChain();
            imp = imp.add(s.value.toString());
            s = lex();
            boolean isStar = false;
            while (s.type == SymbolType.DOT) {

                s = lex();
                if (s.type == SymbolType.MULTIPLY) {
                    isStar = true;
                    s = lex();
                    break;
                }
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                imp = imp.add(s.value.toString());
                s = lex();
            }

            if (isStar) {
                openedNamespaces.add(abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(imp.toString(), true)), 0, true));
            } else {
                importedClasses.add(imp);
            }

            expected(s, lexer.yyline(), SymbolType.SEMICOLON);
            s = lex();
        }
        lexer.pushback(s);

        int publicNs;
        openedNamespaces.add(publicNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(name, true)), 0, true));

        //traits(false, new ArrayList<AssignableAVM2Item>(), new Reference<Boolean>(false), new ArrayList<GraphTargetItem>(), importedClasses, privateNs, 0, publicNs, packageInternalNs, 0, openedNamespaces, name, null, false, items);
        //expectedType(SymbolType.CURLY_CLOSE);
        return new PackageAVM2Item(publicNs, importedClasses, name, items);
    }

    private List<GraphTargetItem> parseScript(String fileName) throws IOException, AVM2ParseException, CompilationException {

        List<Integer> openedNamespaces = new ArrayList<>();

        int scriptPrivateNs = 0;

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
        openedNamespaces.add(scriptPrivateNs = abc.constants.addNamespace(new Namespace(Namespace.KIND_PRIVATE, 0))); //abc.constants.getStringId(name + ":" + className, true)

        int publicNs;
        openedNamespaces.add(publicNs = abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("", true)), 0, true));

        List<GraphTargetItem> items = new ArrayList<>();
        traits(fileName, true, new ArrayList<>(), new Reference<>(false), new ArrayList<>(), new ArrayList<>(), scriptPrivateNs, 0, publicNs, 0, 0, openedNamespaces, null, null, false, items);
        return items;
    }

    public List<GraphTargetItem> scriptTraitsFromString(String str, String fileName) throws AVM2ParseException, IOException, CompilationException {
        lexer = new ActionScriptLexer(str);

        List<GraphTargetItem> ret = parseScript(fileName);
        if (lexer.lex().type != SymbolType.EOF) {
            throw new AVM2ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        return ret;
    }

    public void addScriptFromTree(List<GraphTargetItem> items, boolean documentClass, int classPos) throws AVM2ParseException, CompilationException {
        AVM2SourceGenerator gen = new AVM2SourceGenerator(abc, otherABCs);
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<>(), 0, Boolean.FALSE, 0);
        localData.documentClass = documentClass;
        abc.script_info.add(gen.generateScriptInfo(localData, items, classPos));
    }

    public void addScript(String s, boolean documentClass, String fileName, int classPos) throws AVM2ParseException, IOException, CompilationException {
        List<GraphTargetItem> traits = scriptTraitsFromString(s, fileName);
        addScriptFromTree(traits, documentClass, classPos);
    }

    public ActionScript3Parser(ABC abc, List<ABC> otherABCs) {
        this.abc = abc;
        this.otherABCs = otherABCs;
    }

    private static void initPlayer() throws IOException, InterruptedException {
        if (playerABCs.isEmpty()) {
            if (Configuration.getPlayerSWC() == null) {
                throw new IOException("Player SWC library not found, please place it to " + Configuration.getFlashLibPath());
            }
            SWC swc = new SWC(new FileInputStream(Configuration.getPlayerSWC()));
            SWF swf = new SWF(swc.getSWF("library.swf"), true);
            for (Tag t : swf.tags) {
                if (t instanceof ABCContainerTag) {
                    playerABCs.add(((ABCContainerTag) t).getABC());
                }
            }
        }
    }

    public static void compile(String src, ABC abc, List<ABC> otherABCs, boolean documentClass, String fileName, int classPos) throws AVM2ParseException, IOException, InterruptedException, CompilationException {
        List<ABC> parABCs = new ArrayList<>();
        initPlayer();
        parABCs.addAll(playerABCs);
        parABCs.addAll(otherABCs);
        ActionScript3Parser parser = new ActionScript3Parser(abc, parABCs);
        boolean success = false;
        ABC originalAbc = ((ABCContainerTag) ((Tag) abc.parentTag).cloneTag()).getABC();
        try {
            parser.addScript(src, documentClass, fileName, classPos);
            success = true;
        } finally {
            if (!success) {
                // restore original constant pool and other lists
                abc.constants = originalAbc.constants;
                abc.method_info = originalAbc.method_info;
                abc.metadata_info = originalAbc.metadata_info;
                abc.instance_info = originalAbc.instance_info;
                abc.class_info = originalAbc.class_info;
                abc.script_info = originalAbc.script_info;
                abc.bodies = originalAbc.bodies;
            }
        }
    }

    public static void compile(SWF swf, String src, String dst, int classPos) {
        System.err.println("WARNING: AS3 compiler is not finished yet. This is only used for debuggging!");
        try {
            initPlayer();
            ABC abc = new ABC(null);
            ActionScript3Parser parser = new ActionScript3Parser(abc, playerABCs);
            parser.addScript(new String(Helper.readFile(src), Utf8Helper.charset), true, src, classPos);
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(dst)))) {
                abc.saveToStream(fos);
            }
        } catch (Exception ex) {
            Logger.getLogger(ActionScript3Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
