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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.NumberContext;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructSuperAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecimalValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DefaultXMLNamespace;
import com.jpexs.decompiler.flash.abc.avm2.model.DoubleValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXAttrAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.EscapeXElemAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.Float4ValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetDescendantsAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.HasNextAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextNameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.RegExpAvm2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.model.operations.BitNotAVM2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.model.operations.UnPlusAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BlockItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.EmptyCommand;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.ParenthesisItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import macromedia.asc.util.Decimal128;

/**
 * ActionScript 3 parser.
 *
 * @author JPEXS
 */
public class ActionScript3Parser {

    private long uniqLast = 0;

    private final boolean debugMode = false;

    private static final String AS3_NAMESPACE = "http://adobe.com/AS3/2006/builtin";

    private final AbcIndexing abcIndex;

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private List<GraphTargetItem> commands(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd;
        while ((cmd = command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol s = lex();
        if (s.type == SymbolType.MULTIPLY) {
            return TypeItem.UNBOUNDED;
        } else if (s.type == SymbolType.VOID) {
            return new TypeItem(DottedChain.VOID);
        } else {
            lexer.pushback(s);
        }

        GraphTargetItem t = name(allOpenedNamespaces, thisType, pkg, needsActivation, true, openedNamespaces, null, false, false, variables, importedClasses, abc);
        t = applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, t, new HashMap<>(), false, false, variables, abc);
        return t;
    }

    private GraphTargetItem memberOrCall(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        if (debugMode) {
            System.out.println("memberOrCall:");
        }
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME, SymbolType.FILTER, SymbolType.DESCENDANTS)) {
            switch (s.type) {
                case BRACKET_OPEN:
                case DOT:
                case TYPENAME:
                    lexer.pushback(s);
                    ret = member(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
                    break;
                case FILTER:
                    needsActivation.setVal(true);
                    ret = new XMLFilterAVM2Item(ret, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, inMethod, variables, true, abc), openedNamespaces);
                    expectedType(SymbolType.PARENT_CLOSE);
                    break;
                case PARENT_OPEN:
                    ret = new CallAVM2Item(openedNamespaces, lexer.yyline(), ret, call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc), abcIndex);
                    break;
                case DESCENDANTS:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                    ret = new GetDescendantsAVM2Item(ret, s.type == SymbolType.MULTIPLY ? null : s.value.toString(), openedNamespaces);
                    break;

            }
            s = lex();
        }
        if (s.type == SymbolType.INCREMENT) {
            if (!isNameOrProp(ret)) {
                throw new AVM2ParseException("Invalid assignment", lexer.yyline());
            }
            ret = new PostIncrementAVM2Item(null, null, ret);
            s = lex();

        } else if (s.type == SymbolType.DECREMENT) {
            if (!isNameOrProp(ret)) {
                throw new AVM2ParseException("Invalid assignment", lexer.yyline());
            }
            ret = new PostDecrementAVM2Item(null, null, ret);
            s = lex();
        }

        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/memberOrCall");
        }
        return ret;
    }

    private GraphTargetItem applyType(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.TYPENAME) {
            List<GraphTargetItem> params = new ArrayList<>();
            do {
                s = lex();
                if (s.isType(SymbolType.MULTIPLY)) {
                    params.add(new NullAVM2Item(null, null));
                } else {
                    lexer.pushback(s);
                    params.add(expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc));
                }
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
            ret = new ApplyTypeAVM2Item(null, null, ret, params);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private GraphTargetItem member(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
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
                ret = applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
                s = lex();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                GraphTargetItem index = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                expectedType(SymbolType.BRACKET_CLOSE);
                ret = new IndexAVM2Item(attr, ret, index, null, openedNamespaces);
                s = lex();
            } else {
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                String propName = s.value.toString(); //Can be *
                GraphTargetItem propItem = null;
                s = lex();
                String nsSuffix = "";
                GraphTargetItem ns = null;
                if (s.type == SymbolType.NAMESPACE_OP) {
                    ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, false, null, lexer.yyline(), new DottedChain(new String[]{propName}, new String[]{""} /*FIXME ???*/), null, openedNamespaces, abcIndex);
                    variables.add((UnresolvedAVM2Item) ns);
                    s = lex();
                    if (s.type == SymbolType.BRACKET_OPEN) {
                        propItem = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.BRACKET_CLOSE);
                        propName = null;
                    } else {
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        propName = s.value.toString();
                        propItem = null;
                    }
                } else {
                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        nsSuffix = "#" + s.value;
                    } else {
                        lexer.pushback(s);
                    }
                }

                if (ns != null) {
                    ret = new NamespacedAVM2Item(ns, propName, propItem, ret, attr, openedNamespaces, null);
                } else {
                    ret = new PropertyAVM2Item(ret, attr, propName, nsSuffix, abcIndex, openedNamespaces, new ArrayList<>());
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

    private GraphTargetItem name(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, boolean typeOnly, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, List<DottedChain> importedClasses, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol s = lex();
        DottedChain name = new DottedChain(new String[]{}, new String[]{""});
        boolean attribute = false;
        String name2 = "";
        if (s.type == SymbolType.ATTRIBUTE) {
            attribute = true;
            s = lex();
        }
        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        name2 += s.value.toString();
        s = lex();
        boolean attrBracket = false;
        String nsSuffix = "";
        if (s.type == SymbolType.NAMESPACESUFFIX) {
            s = lex();
            nsSuffix = "#" + s.value;
        }

        name = name.add(attribute, name2, nsSuffix);
        while (s.isType(SymbolType.DOT)) {
            //name += s.value.toString(); //. or ::
            s = lex();
            name2 = "";
            attribute = false;
            if (s.type == SymbolType.ATTRIBUTE) {
                attribute = true;
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
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.NAMESPACE, SymbolType.MULTIPLY);
                name2 += s.value.toString();
            }
            s = lex();
            nsSuffix = "";
            if (s.type == SymbolType.NAMESPACESUFFIX) {
                nsSuffix = "#" + s.value;
                s = lex();
            }
            name = name.add(attribute, name2, nsSuffix);
        }
        String nsname = null;
        String nsprop = null;
        boolean nsAtribute = false;
        GraphTargetItem nspropItem = null;
        if (s.type == SymbolType.NAMESPACE_OP) {
            nsname = name.getLast();
            nsAtribute = name.isLastAttribute();
            s = lex();
            if (s.group == SymbolGroup.IDENTIFIER) {
                nsprop = s.value.toString();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                nspropItem = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                expectedType(SymbolType.BRACKET_CLOSE);
            }
            name = name.getWithoutLast();
            s = lex();
        }

        GraphTargetItem ret = null;
        if (!name.isEmpty()) {
            UnresolvedAVM2Item unr = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), name, null, openedNamespaces, abcIndex);
            //unr.setIndex(index);
            variables.add(unr);
            ret = unr;
        }
        if (nsname != null) {
            UnresolvedAVM2Item ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), new DottedChain(new String[]{nsname}), null, openedNamespaces, abcIndex);
            variables.add(ns);
            ret = new NamespacedAVM2Item(ns, nsprop, nspropItem, ret, nsAtribute, openedNamespaces, null);
        }
        if (s.type == SymbolType.BRACKET_OPEN) {
            lexer.pushback(s);
            if (attrBracket) {
                lexer.pushback(new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.ATTRIBUTE, "@"));
                lexer.pushback(new ParsedSymbol(SymbolGroup.OPERATOR, SymbolType.DOT, "."));
            }
            ret = member(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
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

    private ParsedSymbol expectedType(Object... type) throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, AVM2ParseException, InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private MethodAVM2Item method(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, boolean isPrivate, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, String customAccess, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, boolean override, boolean isFinal, TypeItem thisType, List<NamespaceItem> openedNamespaces, boolean isStatic, String functionName, boolean isMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        FunctionAVM2Item f = function(allOpenedNamespaces, metadata, pkg, isInterface, isNative, needsActivation, importedClasses, thisType, openedNamespaces, functionName, isMethod, variables, abc);
        return new MethodAVM2Item(allOpenedNamespaces, outsidePackage, isPrivate, f.metadata, f.pkg, f.isInterface, f.isNative, customAccess, f.needsActivation, f.hasRest, f.line, override, isFinal, isStatic, functionName, f.paramTypes, f.paramNames, f.paramValues, f.body, f.subvariables, f.retType);
    }

    private FunctionAVM2Item function(List<List<NamespaceItem>> allOpenedNamespaces, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, TypeItem thisType, List<NamespaceItem> openedNamespaces, String functionName, boolean isMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {

        openedNamespaces = new ArrayList<>(openedNamespaces); //local copy
        allOpenedNamespaces.add(openedNamespaces);
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
                GraphTargetItem currentType;
                if (s.type == SymbolType.COLON) {
                    paramTypes.add(currentType = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc));
                    s = lex();
                } else {
                    paramTypes.add(currentType = TypeItem.UNBOUNDED);
                }
                if (s.type == SymbolType.ASSIGN) {
                    GraphTargetItem currentValue = expression(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, null, isMethod, isMethod, isMethod, variables, false, abc);
                    paramValues.add(currentValue);
                    s = lex();
                } else if (!paramValues.isEmpty()) {
                    throw new AVM2ParseException("Some of parameters do not have default values", lexer.yyline());
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
            retType = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
        } else {
            retType = TypeItem.UNBOUNDED;
            lexer.pushback(s);
        }
        List<GraphTargetItem> body = null;
        List<AssignableAVM2Item> subvariables = new ArrayList<>();
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), false, "this", "", null, true, openedNamespaces, abcIndex, false));
        for (int i = 0; i < paramNames.size() - (hasRest ? 1 : 0); i++) {
            subvariables.add(new NameAVM2Item(paramTypes.get(i), lexer.yyline(), false, paramNames.get(i), "", null, true, openedNamespaces, abcIndex, false));
        }
        if (hasRest) {
            subvariables.add(new NameAVM2Item(TypeItem.UNBOUNDED, lexer.yyline(), false, paramNames.get(paramNames.size() - 1), "", null, true, openedNamespaces, abcIndex, false));
        }
        subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), false, "arguments", "", null, true, openedNamespaces, abcIndex, false));
        int parCnt = subvariables.size();
        Reference<Boolean> needsActivation2 = new Reference<>(false);
        if (!isInterface && !isNative) {
            expectedType(SymbolType.CURLY_OPEN);
            body = commands(allOpenedNamespaces, thisType, pkg, needsActivation2, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, isMethod, 0, subvariables, abc);
            expectedType(SymbolType.CURLY_CLOSE);
        } else {
            expectedType(SymbolType.SEMICOLON);
        }

        for (int i = 0; i < parCnt; i++) {
            subvariables.remove(0);
        }
        return new FunctionAVM2Item(metadata, pkg, isInterface, isNative, needsActivation2.getVal(), hasRest, line, functionName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

    private List<Map.Entry<String, Map<String, String>>> parseMetadata() throws IOException, AVM2ParseException, InterruptedException {
        List<Map.Entry<String, Map<String, String>>> metadata = new ArrayList<>();
        ParsedSymbol s = lex();
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
            expected(s, lexer.yyline(), SymbolType.BRACKET_CLOSE);
            s = lex();

            /*
             * Skip Embed metadata - these are loaded automatically by the
             * assignment in SymbolClass tag
             */
            if (!"Embed".equals(name)) {
                metadata.add(en);
            }
        }
        lexer.pushback(s);
        return metadata;
    }

    private void classTraits(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, List<AssignableAVM2Item> cinitVariables, Reference<Boolean> cinitNeedsActivation, List<GraphTargetItem> cinit, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, NamespaceItem pkg, String classNameStr, boolean isInterface, List<GraphTargetItem> traits, List<AssignableAVM2Item> iinitVariables, Reference<Boolean> iinitNeedsActivation, Reference<GraphTargetItem> iinit, ABC abc) throws AVM2ParseException, IOException, CompilationException, InterruptedException {

        NamespaceItem publicNs = new NamespaceItem("", Namespace.KIND_PACKAGE);
        NamespaceItem privateNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_PRIVATE);
        NamespaceItem protectedNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_PROTECTED);
        NamespaceItem staticProtectedNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_STATIC_PROTECTED);
        NamespaceItem packageInternalNs = new NamespaceItem(pkg.name, Namespace.KIND_PACKAGE_INTERNAL);
        NamespaceItem interfaceNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_NAMESPACE);

        openedNamespaces = new ArrayList<>(openedNamespaces);
        allOpenedNamespaces.add(openedNamespaces);
        for (List<NamespaceItem> ln : allOpenedNamespaces) {
            if (!ln.contains(publicNs)) {
                ln.add(publicNs);
            }
        }
        openedNamespaces.add(privateNs);
        openedNamespaces.add(protectedNs);
        openedNamespaces.add(staticProtectedNs);
        
        Stack<Loop> cinitLoops = new Stack<>();
        Map<Loop, String> cinitLoopLabels = new HashMap<>();
        HashMap<String, Integer> cinitRegisterVars = new HashMap<>();

        looptraits:
        while (true) {
            TypeItem thisType = new TypeItem(pkg.name.addWithSuffix(classNameStr));
            boolean isGetter = false;
            boolean isSetter = false;
            boolean isOverride = false;
            boolean isStatic = false;
            boolean isFinal = false;
            boolean isPrivate = false;
            boolean isNative = false;

            String customNs = null;
            List<ParsedSymbol> preSymbols = new ArrayList<>();
            String rawCustomNs = null;
            NamespaceItem namespace = null;
            //static class initializer
            /*if (s.type == SymbolType.CURLY_OPEN) {
                cinit.addAll(commands(allOpenedNamespaces, thisType, pkg, cinitNeedsActivation, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, false, 0, cinitVariables, abc));
                expectedType(SymbolType.CURLY_CLOSE);
            } else {
                lexer.pushback(s);
            }*/
            List<Map.Entry<String, Map<String, String>>> metadata = parseMetadata();
            //s = lex();
            
            ParsedSymbol s = lex();            
            while (s.isType(SymbolType.NATIVE, SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE, SymbolType.PROTECTED, SymbolType.OVERRIDE, SymbolType.FINAL, SymbolType.DYNAMIC, SymbolGroup.IDENTIFIER, SymbolType.INTERNAL, SymbolType.PREPROCESSOR)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        throw new AVM2ParseException("Only one final keyword allowed", lexer.yyline());
                    }
                    preSymbols.add(s);
                    isFinal = true;                    
                } else if (s.type == SymbolType.OVERRIDE) {
                    if (isOverride) {
                        throw new AVM2ParseException("Only one override keyword allowed", lexer.yyline());
                    }
                    preSymbols.add(s);
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
                    preSymbols.add(s);
                    isStatic = true;
                } else if (s.type == SymbolType.NAMESPACE) {
                    preSymbols.add(s);
                    break;
                } else if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        throw new AVM2ParseException("Only one native keyword allowed", lexer.yyline());
                    }
                    preSymbols.add(s);
                    isNative = true;
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    customNs = s.value.toString();                    
                    if (isInterface) {
                        throw new AVM2ParseException("Namespace attributes are not permitted on interface methods", lexer.yyline());
                    }
                    preSymbols.add(s);
                } else if (namespace != null) {
                    throw new AVM2ParseException("Only one access identifier allowed", lexer.yyline());
                }
                switch (s.type) {
                    case PUBLIC:
                        namespace = publicNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline());
                        }
                        preSymbols.add(s);
                        break;
                    case PRIVATE:
                        isPrivate = true;
                        namespace = privateNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline());
                        }
                        preSymbols.add(s);
                        break;
                    case PROTECTED:
                        namespace = protectedNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline());
                        }
                        preSymbols.add(s);
                        break;
                    case INTERNAL:
                        namespace = packageInternalNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline());
                        }
                        preSymbols.add(s);
                        break;
                    case PREPROCESSOR:
                        if (((String) s.value).toLowerCase().equals("namespace")) {
                            preSymbols.add(s);
                            s = lex();
                            expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                            preSymbols.add(s);
                            s = lex();                            
                            expected(s, lexer.yyline(), SymbolType.STRING);
                            preSymbols.add(s);                            
                            namespace = new NamespaceItem((String) s.value, Namespace.KIND_NAMESPACE);
                            s = lex();
                            expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                            preSymbols.add(s);                            
                            
                        } else {
                            lexer.pushback(s);
                        }
                        break;
                }
                s = lex();
            }

            if (isInterface) {
                namespace = interfaceNs;
            }

            if (namespace == null && customNs == null) {
                namespace = packageInternalNs;
            }
            if (namespace == protectedNs && isStatic) {
                namespace = staticProtectedNs;
            }
            if (namespace == null && customNs != null) {
                //Special: it will be resolved later:
                namespace = new NamespaceItem(customNs, NamespaceItem.KIND_NAMESPACE_CUSTOM);
            }

            switch (s.type) {
                case FUNCTION:
                    s = lex();
                    if (s.type == SymbolType.GET) {
                        isGetter = true;
                        s = lex();
                    } else if (s.type == SymbolType.SET) {
                        isSetter = true;
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.PARENT_OPEN);
                    String fname = null;

                    //fix for methods with name "get" or "set" - they are not getters/setters!
                    if (s.isType(SymbolType.PARENT_OPEN)) {
                        lexer.pushback(s);
                        if (isGetter) {
                            fname = "get";
                            isGetter = false;
                        } else if (isSetter) {
                            fname = "set";
                            isSetter = false;
                        } else {
                            throw new AVM2ParseException("Missing method name", lexer.yyline());
                        }
                    } else {
                        fname = s.value.toString();
                    }
                    if (fname.equals(classNameStr)) { //constructor
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
                        iinit.setVal(method(allOpenedNamespaces, outsidePackage, isPrivate, metadata, pkg, false, false, customNs, iinitNeedsActivation, importedClasses, false, false, thisType, openedNamespaces, false, "", true, iinitVariables, abc));
                    } else {
                        GraphTargetItem t;
                        if (classNameStr == null) {
                            isStatic = true;
                        }

                        s = lex();
                        if (s.type == SymbolType.NAMESPACESUFFIX) {
                            namespace = new NamespaceItem((Integer) s.value);
                        } else {
                            lexer.pushback(s);
                        }

                        MethodAVM2Item ft;
                        ft = method(allOpenedNamespaces, outsidePackage, isPrivate, metadata, namespace, isInterface, isNative, customNs, new Reference<>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, fname, true, new ArrayList<>(), abc);

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
                        if (isGetter) {
                            GetterAVM2Item g = new GetterAVM2Item(allOpenedNamespaces, outsidePackage, ft.isPrivate(), ft.metadata, ft.pkg, isInterface, isNative, customNs, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                            t = g;
                        } else if (isSetter) {
                            SetterAVM2Item st = new SetterAVM2Item(allOpenedNamespaces, outsidePackage, ft.isPrivate(), ft.metadata, ft.pkg, isInterface, isNative, customNs, ft.needsActivation, ft.hasRest, ft.line, ft.isOverride(), ft.isFinal(), isStatic, ft.functionName, ft.paramTypes, ft.paramNames, ft.paramValues, ft.body, ft.subvariables, ft.retType);
                            t = st;
                        } else {
                            t = ft;
                        }
                        //NOTE: Looks like TraitFunction does not work in FlashPlayer - use MethodTrait instead
                        /*else {
                         t = function(metadata, pkg, isInterface, new Reference<Boolean>(false), importedClasses, namespace, thisType, openedNamespaces, fname, false, new ArrayList<AssignableAVM2Item>());
                         }*/

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
                    String nval;
                    s = lex();

                    boolean generatedNs = false;
                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        nval = s.value.toString();
                        s = lex();
                    } else {
                        nval = (pkg.name.toRawString() + ":" + classNameStr) + "/" + nname;
                        generatedNs = true;
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    ConstAVM2Item ns = new ConstAVM2Item(metadata, namespace, customNs, true, nname, new TypeItem(DottedChain.NAMESPACE), new StringAVM2Item(null, null, nval), lexer.yyline(), generatedNs);
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
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have variable/const fields", lexer.yyline());
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();

                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        namespace = new NamespaceItem((Integer) s.value);
                        s = lex();
                    }

                    GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        type = type(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>(), abc);
                        s = lex();
                    } else {
                        type = TypeItem.UNBOUNDED;
                    }

                    GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        value = expression(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, isStatic || isConst ? cinitVariables : iinitVariables, false, abc);
                        s = lex();
                    }
                    GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(metadata, namespace, customNs, isStatic, vcname, type, value, lexer.yyline(), false);
                    } else {
                        tar = new SlotAVM2Item(metadata, namespace, customNs, isStatic, vcname, type, value, lexer.yyline());
                    }
                    traits.add(tar);
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);
                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }
                    
                    GraphTargetItem cmd = command(allOpenedNamespaces, null, publicNs, cinitNeedsActivation, importedClasses, openedNamespaces, cinitLoops, cinitLoopLabels, cinitRegisterVars, true, false, 0, false, cinitVariables, abc);
                    if (cmd != null) {
                        traits.add(cmd);
                    } else {
                        break looptraits;
                    }
            }
        }
    }

    private void scriptTraits(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            int scriptIndex,
            String scriptName,
            List<GraphTargetItem> traits, 
            Reference<Integer> numberUsageRef, 
            Reference<Integer> numberRoundingRef, 
            Reference<Integer> numberPrecisionRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<AssignableAVM2Item> sinitVariables            
            ) throws AVM2ParseException, IOException, CompilationException, InterruptedException {

        Stack<Loop> sinitLoops = new Stack<>();
        Map<Loop, String> sinitLoopLabels = new HashMap<>();
            
        HashMap<String, Integer> sinitRegisterVars = new HashMap<>();
        while (scriptTraitsBlock(
                importedClasses,
                openedNamespaces,
                allOpenedNamespaces, 
                scriptIndex,
                scriptName, 
                traits, 
                numberUsageRef,
                numberRoundingRef, 
                numberPrecisionRef,
                abc,
                sinitNeedsActivation,
                sinitLoops,
                sinitLoopLabels,
                sinitRegisterVars,
                sinitVariables
                )) {
            //empty
        }
    }

    private boolean scriptTraitsBlock(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            int scriptIndex,
            String scriptName,
            List<GraphTargetItem> traits,
            Reference<Integer> numberUsageRef, 
            Reference<Integer> numberRoundingRef,
            Reference<Integer> numberPrecisionRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            Stack<Loop> sinitLoops,
            Map<Loop, String> sinitLoopLabels,
            HashMap<String, Integer> sinitRegisterVars,
            List<AssignableAVM2Item> sinitVariables        
            ) throws AVM2ParseException, IOException, CompilationException, InterruptedException {
        ParsedSymbol s;
        boolean inPackage = false;
        s = lex();
        NamespaceItem publicNs;
        NamespaceItem packageInternalNs;
        DottedChain pkgName = DottedChain.TOPLEVEL;
        if (s.type == SymbolType.PACKAGE) {
            s = lex();
            if (s.type != SymbolType.CURLY_OPEN) {
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                pkgName = pkgName.addWithSuffix(s.value.toString());
                s = lex();
            }
            while (s.type == SymbolType.DOT) {
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                pkgName = pkgName.addWithSuffix(s.value.toString());
                s = lex();
            }
            expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
            publicNs = new NamespaceItem(pkgName, Namespace.KIND_PACKAGE);
            packageInternalNs = new NamespaceItem(pkgName, Namespace.KIND_PACKAGE_INTERNAL);
            s = lex();
            inPackage = true;
        } else {
            publicNs = null;
            packageInternalNs = new NamespaceItem(scriptName + "$" + scriptIndex, Namespace.KIND_PRIVATE);
        }
        lexer.pushback(s);

        allOpenedNamespaces.add(openedNamespaces);
        NamespaceItem emptyNs = new NamespaceItem("", Namespace.KIND_PACKAGE);
        openedNamespaces.add(emptyNs);
        NamespaceItem as3Ns = new NamespaceItem(AS3_NAMESPACE, Namespace.KIND_NAMESPACE);
        as3Ns.forceResolve(abcIndex);
        openedNamespaces.add(as3Ns);

        for (List<NamespaceItem> ln : allOpenedNamespaces) {
            if (publicNs != null && !ln.contains(publicNs)) {
                ln.add(publicNs);
            }
            if (!ln.contains(packageInternalNs)) {
                ln.add(packageInternalNs);
            }
        }

        parseImportsUsages(importedClasses, openedNamespaces, numberUsageRef, numberPrecisionRef, numberRoundingRef, abc);

        boolean isEmpty = true;

        looptrait:
        while (true) {
            List<Map.Entry<String, Map<String, String>>> metadata = parseMetadata();
            s = lex();
            boolean isFinal = false;
            boolean isDynamic = false;
            boolean isPublic = false;
            boolean isNative = false;
            NamespaceItem ns = packageInternalNs;
            List<ParsedSymbol> preSymbols = new ArrayList<>();
            while (s.isType(SymbolType.FINAL, SymbolType.DYNAMIC, SymbolType.PUBLIC)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        throw new AVM2ParseException("Only one final keyword allowed", lexer.yyline());
                    }
                    isFinal = true;
                    preSymbols.add(s);                    
                }
                if (s.type == SymbolType.PUBLIC) {
                    if (!inPackage) {
                        throw new AVM2ParseException("public only allowed inside package", lexer.yyline());

                    }
                    if (isPublic) {
                        throw new AVM2ParseException("Only one public keyword allowed", lexer.yyline());
                    }
                    isPublic = true;
                    ns = publicNs;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.DYNAMIC) {
                    if (isDynamic) {
                        throw new AVM2ParseException("Only one dynamic keyword allowed", lexer.yyline());
                    }
                    isDynamic = true;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        throw new AVM2ParseException("Only one native keyword allowed", lexer.yyline());
                    }
                    isNative = true;
                    preSymbols.add(s);
                }
                s = lex();
            }

            switch (s.type) {
                case CLASS:
                case INTERFACE:
                    isEmpty = false;
                    List<NamespaceItem> subOpenedNamespaces = new ArrayList<>(openedNamespaces);
                    boolean isInterface = false;
                    if (s.type == SymbolType.INTERFACE) {
                        isInterface = true;
                    }
                    GraphTargetItem extendsTypeStr = null;
                    List<GraphTargetItem> interfaces = new ArrayList<>();
                    String subNameStr;

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    subNameStr = s.value.toString();
                    s = lex();
                    
                    boolean nullable = true;
                    
                    if (s.type == SymbolType.NOT) {
                        s = lex();
                        nullable = false;
                    } else if (s.type == SymbolType.TERNAR) {
                        s = lex();
                    }
                    
                    if (!isInterface) {

                        if (s.type == SymbolType.EXTENDS) {
                            extendsTypeStr = type(allOpenedNamespaces, null, ns, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>(), abc);
                            s = lex();
                        }
                        if (s.type == SymbolType.IMPLEMENTS) {
                            do {
                                GraphTargetItem implementsTypeStr = type(allOpenedNamespaces, null, ns, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>(), abc);
                                interfaces.add(implementsTypeStr);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    } else {
                        if (s.type == SymbolType.EXTENDS) {
                            do {
                                GraphTargetItem intExtendsTypeStr = type(allOpenedNamespaces, null, ns, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>(), abc);
                                interfaces.add(intExtendsTypeStr);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    }

                    if (extendsTypeStr != null) {
                        List<Integer> indices = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        List<String> namespaces = new ArrayList<>();
                        //FIXME for Private classes in script (?)
                        AVM2SourceGenerator.parentNamesAddNames(abcIndex, scriptIndex, AVM2SourceGenerator.resolveType(new SourceGeneratorLocalData(new HashMap<>(), 0, false, 0), ((TypeItem) ((UnresolvedAVM2Item) extendsTypeStr)
                                .resolve(null, pkgName.addWithSuffix(subNameStr).toRawString(), null, new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), new ArrayList<>())), abcIndex), indices, names, namespaces);
                        for (int i = 0; i < names.size(); i++) {
                            if (namespaces.get(i) == null || namespaces.get(i).isEmpty()) {
                                continue;
                            }
                            subOpenedNamespaces.add(new NamespaceItem(namespaces.get(i) + ":" + names.get(i), Namespace.KIND_STATIC_PROTECTED));
                        }
                    }

                    List<GraphTargetItem> cinit = new ArrayList<>();
                    Reference<Boolean> cinitNeedsActivation = new Reference<>(false);

                    Reference<GraphTargetItem> iinit = new Reference<>(null);
                    List<AssignableAVM2Item> cinitVariables = new ArrayList<>();
                    List<AssignableAVM2Item> iinitVariables = new ArrayList<>();
                    Reference<Boolean> iinitNeedsActivation = new Reference<>(false);

                    List<GraphTargetItem> subTraits = new ArrayList<>();

                    classTraits(allOpenedNamespaces, !inPackage, cinitVariables, cinitNeedsActivation, cinit, importedClasses, subOpenedNamespaces, ns, subNameStr, isInterface, subTraits, iinitVariables, iinitNeedsActivation, iinit, abc);

                    if (isInterface) {
                        traits.add(new InterfaceAVM2Item(metadata, importedClasses, ns, subOpenedNamespaces, isFinal, subNameStr, interfaces, subTraits, nullable));
                    } else {
                        traits.add(new ClassAVM2Item(metadata, importedClasses, ns, subOpenedNamespaces, isFinal, isDynamic, subNameStr, extendsTypeStr, interfaces, cinit, cinitNeedsActivation.getVal(), cinitVariables, iinit.getVal(), iinitVariables, subTraits, iinitNeedsActivation.getVal(), nullable));
                    }

                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                case FUNCTION:
                    isEmpty = false;
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String fname = s.value.toString();

                    traits.add(method(allOpenedNamespaces, !inPackage, false, metadata, ns, false, isNative, null, new Reference<>(false), importedClasses, false, isFinal, null, openedNamespaces, true, fname, true, new ArrayList<>(), abc));
                    break;
                case CONST:
                case VAR:
                    isEmpty = false;
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline());
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();
                    GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        type = type(allOpenedNamespaces, null, ns, new Reference<>(false), importedClasses, openedNamespaces, new ArrayList<>(), abc);
                        s = lex();
                    } else {
                        type = TypeItem.UNBOUNDED;
                    }

                    GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        value = expression(allOpenedNamespaces, null, ns, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, sinitVariables, false, abc);
                        s = lex();
                    }
                    GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(metadata, ns, null, true, vcname, type, value, lexer.yyline(), false);
                    } else {
                        tar = new SlotAVM2Item(metadata, ns, null, true, vcname, type, value, lexer.yyline());
                    }
                    traits.add(tar);
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                case NAMESPACE:
                    isEmpty = false;
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for namespaces", lexer.yyline());
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String nname = s.value.toString();
                    String nval;
                    s = lex();

                    boolean generatedNs = false;
                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        nval = s.value.toString();
                        s = lex();
                    } else {
                        generatedNs = true;
                        nval = ns.name.toRawString() + ":" + nname;
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    traits.add(new ConstAVM2Item(metadata, ns, null, true, nname, new TypeItem(DottedChain.NAMESPACE), new StringAVM2Item(null, null, nval), lexer.yyline(), generatedNs));
                    break;
                default:
                    lexer.pushback(s);
                    
                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }
                    
                    if (parseImportsUsages(importedClasses, openedNamespaces, numberUsageRef, numberPrecisionRef, numberRoundingRef, abc)) {
                        break;
                    }
                    GraphTargetItem cmd = command(allOpenedNamespaces, null, publicNs, sinitNeedsActivation, importedClasses, openedNamespaces, sinitLoops, sinitLoopLabels, sinitRegisterVars, true, false, 0, false, sinitVariables, abc);
                    if (cmd != null) {
                        traits.add(cmd);
                        isEmpty = false;
                    } else {                        
                        break looptrait;
                    }
            }

        }
        if (inPackage) {
            expectedType(SymbolType.CURLY_CLOSE);
        }
        return !isEmpty;
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<AssignableAVM2Item> variables) throws IOException, AVM2ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            /*case INT:
             expectedType(SymbolType.PARENT_OPEN);
             ret = new ToIntegerAVM2Item(null, null,  expression(allOpenedNamespaces, thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             break;
             case NUMBER_OP:
             s = lex();
             if (s.type == SymbolType.DOT) {
             VariableAVM2Item vi = new VariableAVM2Item(s.value.toString(), null, false);
             variables.add(vi);
             ret = memberOrCall(allOpenedNamespaces, thisType,vi, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToNumberAVM2Item(null, null,  expression(allOpenedNamespaces, thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
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
             ret = memberOrCall(allOpenedNamespaces, thisType,vi2, registerVars, inFunction, inMethod, variables);
             } else {
             expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
             ret = new ToStringAVM2Item(null, null,  expression(allOpenedNamespaces, thisType,pkg,needsActivation, importedClasses, openedNamespaces,openedNamespacesKinds,registerVars, inFunction, inMethod, true, variables));
             expectedType(SymbolType.PARENT_CLOSE);
             ret = memberOrCall(allOpenedNamespaces, thisType,ret, registerVars, inFunction, inMethod, variables);
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
            GraphTargetItem ret = new StringAVM2Item(null, null, a.toString());
            ((StringBuilder) a).setLength(0);
            return ret;
        }
        if (a instanceof String) {
            return new StringAVM2Item(null, null, (String) a);
        }
        if (a instanceof GraphTargetItem) {
            return (GraphTargetItem) a;
        }
        return null;
    }

    private GraphTargetItem add(Object a, Object b) {
        GraphTargetItem ta = add(a);
        GraphTargetItem tb = add(b);

        if ((ta instanceof StringAVM2Item) && (tb instanceof StringAVM2Item)) {
            String sa = ((StringAVM2Item) ta).getValue();
            String sb = ((StringAVM2Item) tb).getValue();

            return new StringAVM2Item(ta.getSrc(), ta.getLineStartItem(), sa + sb);
        }

        if (ta == null && tb == null) {
            return null;
        }
        if (ta == null) {
            return tb;
        }
        if (tb == null) {
            return ta;
        }
        return new AddAVM2Item(null, null, ta, tb);
    }

    private void addS(List<GraphTargetItem> rets, StringBuilder sb) {
        if (sb.length() > 0) {
            if (!rets.isEmpty() && (rets.get(rets.size() - 1) instanceof StringAVM2Item)) {
                StringAVM2Item stringItem = ((StringAVM2Item) rets.get(rets.size() - 1));
                stringItem.setValue(stringItem.getValue() + sb.toString());
            } else {
                rets.add(new StringAVM2Item(null, null, sb.toString()));
            }
            sb.setLength(0);
        }
    }

    private List<GraphTargetItem> xmltag(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> usesVars, List<String> openedTags, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol s;
        List<GraphTargetItem> rets = new ArrayList<>();
        //GraphTargetItem ret = null;
        StringBuilder sb = new StringBuilder();
        loop:
        do {
            s = lex();
            List<String> sub = new ArrayList<>();
            Reference<Boolean> subusesvars = new Reference<>(false);
            switch (s.type) {
                case XML_ATTRNAMEVAR_BEGIN: // {...}=       add
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_ATTRVALVAR_BEGIN: // ={...}         esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    addS(rets, sb);
                    rets.add(new EscapeXAttrAVM2Item(null, null, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc)));
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_VAR_BEGIN: // {...}                esc_xelem
                    usesVars.setVal(true);
                    addS(rets, sb);
                    rets.add(new EscapeXElemAVM2Item(null, null, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc)));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_FINISHVARTAG_BEGIN: // </{...}>    add
                    usesVars.setVal(true);
                    sb.append("</");
                    addS(rets, sb);

                    rets.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.begin(ActionScriptLexer.XMLCLOSETAGFINISH);
                    s = lex();
                    while (s.isType(SymbolType.XML_TEXT)) {
                        sb.append(s.value);
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.GREATER_THAN);
                    sb.append(">");
                    addS(rets, sb);

                    if (openedTags.isEmpty()) {
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline());
                    }
                    openedTags.remove(openedTags.size() - 1);
                    break;
                case XML_STARTVARTAG_BEGIN: // <{...}>      add
                    GraphTargetItem ex = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    sub.add("*");
                    sb.append("<");
                    addS(rets, sb);
                    rets.add(ex);
                    rets.addAll(xmltag(allOpenedNamespaces, thisType, pkg, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc));
                    break;
                case XML_STARTTAG_BEGIN:    // <xxx>
                    sub.add(s.value.toString().trim().substring(1)); //remove < from beginning
                    List<GraphTargetItem> st = xmltag(allOpenedNamespaces, thisType, pkg, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    sb.append(s.value.toString());
                    addS(rets, sb);
                    rets.addAll(st);
                    break;
                case XML_FINISHTAG:
                    String tname = s.value.toString().substring(2, s.value.toString().length() - 1).trim();
                    if (openedTags.isEmpty()) {
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline());
                    }
                    String lastTName = openedTags.get(openedTags.size() - 1);
                    if (lastTName.equals(tname) || lastTName.equals("*")) {
                        openedTags.remove(openedTags.size() - 1);
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
        } while (!openedTags.isEmpty());
        addS(rets, sb);
        return rets;
    }

    private GraphTargetItem xml(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        List<String> openedTags = new ArrayList<>();
        List<GraphTargetItem> xmlParts = xmltag(allOpenedNamespaces, thisType, pkg, new Reference<>(false), openedTags, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
        lexer.setEnableWhiteSpace(true);
        lexer.begin(ActionScriptLexer.YYINITIAL);
        ParsedSymbol s = lexer.lex();
        while (s.isType(SymbolType.XML_WHITESPACE)) {
            addS(xmlParts, new StringBuilder(s.value.toString()));
            s = lexer.lex();
        }
        lexer.setEnableWhiteSpace(false);
        lexer.pushback(s);
        GraphTargetItem ret = add(xmlParts);
        ret = new XMLAVM2Item(ret);
        //lexer.yybegin(ActionScriptLexer.YYINITIAL);
        //TODO: Order of additions as in official compiler
        return ret;
    }

    private GraphTargetItem command(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
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
            } else if (!sx.value.equals("xml")) {
                lexer.pushback(sx);
            } else {
                expectedType(SymbolType.NAMESPACE);
                expectedType(SymbolType.ASSIGN);
                GraphTargetItem ns = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                ret = new DefaultXMLNamespace(null, null, ns);
                //TODO: use dxns for attribute namespaces instead of dxnslate
            }
        }
        if (ret == null) {
            switch (s.type) {
                case USE:
                    expectedType(SymbolType.NAMESPACE);
                    GraphTargetItem ns = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    openedNamespaces.add(new NamespaceItem(ns.toString(), Namespace.KIND_PACKAGE /*FIXME?*/));
                    break;
                case WITH:
                    needsActivation.setVal(true);
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem wvar = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    if (!isNameOrProp(wvar)) {
                        throw new AVM2ParseException("Not a property or name", lexer.yyline());
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    List<AssignableAVM2Item> withVars = new ArrayList<>();
                    List<GraphTargetItem> wcmd = commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, withVars, abc);
                    variables.addAll(withVars);
                    for (AssignableAVM2Item a : withVars) {
                        if (a instanceof UnresolvedAVM2Item) {
                            UnresolvedAVM2Item ua = (UnresolvedAVM2Item) a;
                            ua.scopeStack.add(0, wvar);
                        }
                    }
                    expectedType(SymbolType.CURLY_CLOSE);
                    ret = new WithAVM2Item(null, null, wvar, wcmd);
                    ((WithAVM2Item) ret).subvariables = withVars;
                    break;
                /*case DELETE:
                 GraphTargetItem varDel = expression(allOpenedNamespaces, thisType,pkg,needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(allOpenedNamespaces, thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                 if(!isNameOrProp(varDel)){
                 throw new ParseException("Not a property or name", lexer.yyline());
                 }
                 if (varDel instanceof GetPropertyAVM2Item) {
                 GetPropertyAVM2Item gm = (GetPropertyAVM2Item) varDel;
                 ret = new DeletePropertyAVM2Item(null, null,  gm.object, gm.propertyName);
                 } else if (varDel instanceof NameAVM2Item) {
                 variables.remove(varDel);
                 ret = new DeletePropertyAVM2Item(null, null,  null, (NameAVM2Item) varDel);
                 } else {
                 throw new ParseException("Not a property", lexer.yyline());
                 }
                 break;*/
                case FUNCTION:
                    s = lexer.lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    needsActivation.setVal(true);
                    ret = (function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, s.value.toString(), false, variables, abc));
                    break;
                case VAR:
                case CONST:
                    boolean isConst = s.type == SymbolType.CONST;
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String varIdentifier = s.value.toString();
                    s = lex();
                    GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        type = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        s = lex();
                    } else {
                        type = TypeItem.UNBOUNDED;
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        GraphTargetItem varval = (expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                        ret = new NameAVM2Item(type, lexer.yyline(), false, varIdentifier, "", varval, true, openedNamespaces, abcIndex, isConst);
                        variables.add((NameAVM2Item) ret);
                    } else {
                        ret = new NameAVM2Item(type, lexer.yyline(), false, varIdentifier, "", null, true, openedNamespaces, abcIndex, isConst);
                        variables.add((NameAVM2Item) ret);
                        lexer.pushback(s);
                    }
                    break;
                case CURLY_OPEN:
                    ret = new BlockItem(null, null, commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables, abc));
                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                /*case INCREMENT: //preincrement
                 case DECREMENT: //predecrement
                 GraphTargetItem varincdec = expression(allOpenedNamespaces, thisType,pkg,needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables);//name(allOpenedNamespaces, thisType,false, openedNamespaces, registerVars, inFunction, inMethod, variables);
                 if(!isNameOrProp(varincdec)){
                 throw new ParseException("Not a property or name", lexer.yyline());
                 }
                 if (s.type == SymbolType.INCREMENT) {
                 ret = new PreIncrementAVM2Item(null, null,  varincdec);
                 } else if (s.type == SymbolType.DECREMENT) {
                 ret = new PreDecrementAVM2Item(null, null,  varincdec);
                 }
                 break;*/
                case SUPER: //constructor call
                    ParsedSymbol ss2 = lex();
                    if (ss2.type == SymbolType.PARENT_OPEN) {
                        List<GraphTargetItem> args = call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                        ret = new ConstructSuperAVM2Item(null, null, new LocalRegAVM2Item(null, null, 0, null, new TypeItem("Object") /*?*/), args);
                    } else { //no constructor call, but it could be calling parent methods... => handle in expression
                        lexer.pushback(ss2);
                        lexer.pushback(s);
                    }
                    break;
                case IF:
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem ifExpr = (expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                    expectedType(SymbolType.PARENT_CLOSE);
                    GraphTargetItem onTrue = command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    List<GraphTargetItem> onTrueList = new ArrayList<>();
                    onTrueList.add(onTrue);
                    s = lex();
                    List<GraphTargetItem> onFalseList = null;
                    if (s.type == SymbolType.ELSE) {
                        onFalseList = new ArrayList<>();
                        onFalseList.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc));
                    } else {
                        lexer.pushback(s);
                    }
                    ret = new IfItem(null, null, ifExpr, onTrueList, onFalseList);
                    break;
                case WHILE:
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> whileExpr = new ArrayList<>();
                    whileExpr.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc));
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> whileBody = new ArrayList<>();
                    Loop wloop = new Loop(uniqId(), null, null);
                    if (loopLabel != null) {
                        loopLabels.put(wloop, loopLabel);
                    }
                    loops.push(wloop);
                    whileBody.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc));
                    ret = new WhileItem(null, null, wloop, whileExpr, whileBody);
                    loops.pop();
                    break;
                case DO:
                    List<GraphTargetItem> doBody = new ArrayList<>();
                    Loop dloop = new Loop(uniqId(), null, null);
                    loops.push(dloop);
                    if (loopLabel != null) {
                        loopLabels.put(dloop, loopLabel);
                    }
                    doBody.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc));
                    expectedType(SymbolType.WHILE);
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> doExpr = new ArrayList<>();
                    doExpr.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc));
                    expectedType(SymbolType.PARENT_CLOSE);
                    ret = new DoWhileItem(null, null, dloop, doBody, doExpr);
                    loops.pop();
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
                    GraphTargetItem firstCommand = command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, false, variables, abc);
                    if (firstCommand instanceof NameAVM2Item) {
                        NameAVM2Item nai = (NameAVM2Item) firstCommand;
                        if (nai.isDefinition() && nai.getAssignedValue() == null) { //Declared value in for..in
                            firstCommand = expression1(allOpenedNamespaces, firstCommand, GraphTargetItem.NOPRECEDENCE, thisType, pkg, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables, abc);
                        }
                    }
                    InAVM2Item inexpr = null;
                    if (firstCommand instanceof InAVM2Item) {
                        forin = true;
                        inexpr = (InAVM2Item) firstCommand;
                    } else if (forin) {
                        throw new AVM2ParseException("In expression required", lexer.yyline());
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
                        s = lex();
                        if (firstCommand != null) { //can be empty command
                            forFirstCommands.add(firstCommand);
                        }
                        lexer.pushback(s);
                        //GraphTargetItem firstCommand = command(thisType,pkg,needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                        forExpr = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        if (forExpr == null) {
                            forExpr = new TrueItem(null, null);
                        }
                        expectedType(SymbolType.SEMICOLON);
                        GraphTargetItem fcom = command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                        if (fcom != null) {
                            forFinalCommands.add(fcom);
                        }
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> forBody = new ArrayList<>();
                    forBody.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables, abc));
                    if (forin) {
                        if (each) {
                            ret = new ForEachInAVM2Item(null, null, floop, inexpr, forBody);
                        } else {

                            ret = new ForInAVM2Item(null, null, floop, inexpr, forBody);
                        }
                    } else {
                        ret = new ForItem(null, null, floop, forFirstCommands, forExpr, forFinalCommands, forBody);
                    }
                    loops.pop();
                    break;
                case SWITCH:
                    Loop sloop = new Loop(-uniqId(), null, null); //negative id marks switch = no continue
                    loops.push(sloop);
                    if (loopLabel != null) {
                        loopLabels.put(sloop, loopLabel);
                    }
                    expectedType(SymbolType.PARENT_OPEN);
                    GraphTargetItem switchExpr = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
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
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                            GraphTargetItem curCaseExpr = s.type == SymbolType.DEFAULT ? new DefaultItem() : expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
                            expectedType(SymbolType.COLON);
                            s = lex();
                            caseExprsAll.add(curCaseExpr);
                            valueMapping.add(pos);
                        }
                        pos++;
                        lexer.pushback(s);
                        List<GraphTargetItem> caseCmd = commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables, abc);
                        caseCmds.add(caseCmd);
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    ret = new SwitchItem(null, null, sloop, switchExpr, caseExprsAll, caseCmds, valueMapping);
                    loops.pop();
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
                    ret = new BreakItem(null, null, bloopId);
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
                            if (loops.get(i).id >= 0) { //no switches
                                cloopId = loops.get(i).id;
                                break;
                            }
                        }
                        if (cloopId <= 0) {
                            throw new AVM2ParseException("No loop to continue", lexer.yyline());
                        }
                    }
                    //TODO: handle switch
                    ret = new ContinueItem(null, null, cloopId);
                    break;
                case RETURN:
                    GraphTargetItem retexpr = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables, false, abc);
                    if (retexpr == null) {
                        ret = new ReturnVoidAVM2Item(null, null);
                    } else {
                        ret = new ReturnValueAVM2Item(null, null, retexpr);
                    }
                    break;
                case TRY:
                    needsActivation.setVal(true);
                    List<GraphTargetItem> tryCommands = new ArrayList<>();
                    tryCommands.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc));
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
                        GraphTargetItem etype = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        NameAVM2Item e = new NameAVM2Item(etype, lexer.yyline(), false, enamestr, "", new ExceptionAVM2Item(null)/*?*/, true/*?*/, openedNamespaces, abcIndex, false);
                        //variables.add(e);
                        catchExceptions.add(e);
                        e.setSlotNumber(1);
                        e.setSlotScope(Integer.MAX_VALUE); //will be changed later
                        expectedType(SymbolType.PARENT_CLOSE);
                        List<AssignableAVM2Item> catchVars = new ArrayList<>();
                        expectedType(SymbolType.CURLY_OPEN);
                        List<GraphTargetItem> cc = commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, catchVars, abc);
                        expectedType(SymbolType.CURLY_CLOSE);
                        catchesVars.add(catchVars);
                        List<AssignableAVM2Item> newVariables = new ArrayList<>(catchVars);

                        for (int i = 0; i < newVariables.size(); i++) {
                            AssignableAVM2Item a = newVariables.get(i);
                            if (a instanceof UnresolvedAVM2Item) {
                                UnresolvedAVM2Item ui = (UnresolvedAVM2Item) a;
                                if (ui.getVariableName().equals(DottedChain.parseWithSuffix(e.getVariableName()))) {
                                    List<AssignableAVM2Item> catchedVarAsList = new ArrayList<>();
                                    catchedVarAsList.add(e);
                                    try {
                                        ui.resolve(null, null, null, new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), catchedVarAsList);
                                    } catch (CompilationException ex) {
                                        // ignore
                                    }
                                    ui.setSlotNumber(e.getSlotNumber());
                                    ui.setSlotScope(e.getSlotScope());
                                    newVariables.remove(i);
                                    i--;
                                }

                            }
                        }
                        variables.addAll(newVariables);

                        catchCommands.add(cc);
                        s = lex();
                        found = true;
                    }
                    //TODO:
                    /*for (int i = varCnt; i < variables.size(); i++) {
                        AssignableAVM2Item av = variables.get(i);
                        if (av instanceof UnresolvedAVM2Item) {
                            UnresolvedAVM2Item ui = (UnresolvedAVM2Item) av;
                            for (NameAVM2Item e : catchExceptions) {
                                if (ui.getVariableName().equals(DottedChain.parseWithSuffix(e.getVariableName()))) {
                                    try {
                                        ui.resolve(null, null, null, new ArrayList<>(), new ArrayList<>(), abcIndex, new ArrayList<>(), variables);
                                    } catch (CompilationException ex) {
                                        // ignore
                                    }
                                    ui.setSlotNumber(e.getSlotNumber());
                                    ui.setSlotScope(e.getSlotScope());
                                }
                            }
                        }
                    }*/

                    List<GraphTargetItem> finallyCommands = null;
                    if (s.type == SymbolType.FINALLY) {
                        finallyCommands = new ArrayList<>();
                        finallyCommands.add(command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc));
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
                    ret = new ThrowAVM2Item(null, null, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                    break;
                default:
                    GraphTargetItem valcmd = expressionCommands(s, registerVars, inFunction, inMethod, forinlevel, variables);
                    if (valcmd != null) {
                        ret = valcmd;
                        break;
                    }
                    if (s.type == SymbolType.SEMICOLON) {
                        return new EmptyCommand();
                    }
                    lexer.pushback(s);
                    ret = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
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
            ret = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    /*private GraphTargetItem expressionRemainder(TypeItem thisType, String pkg, Reference<Boolean> needsActivation, List<NamespaceItem> openedNamespaces, GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, List<DottedChain> importedClasses) throws IOException, AVM2ParseException {
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
        return (item instanceof IndexAVM2Item);
    }

    private boolean isType(GraphTargetItem item) {
        if (item == null) {
            return false;
        }
        while (item instanceof GetPropertyAVM2Item) {
            item = ((GetPropertyAVM2Item) item).object;
        }
        return (item instanceof NameAVM2Item);
    }

    private int brackets(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
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

    private GraphTargetItem expression(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        return expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, allowRemainder, variables, allowComma, abc);
    }

    private GraphTargetItem expression(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, InterruptedException {

        List<GraphTargetItem> commaItems = new ArrayList<>();
        ParsedSymbol symb;
        do {
            GraphTargetItem prim = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
            if (prim == null) {
                return null;
            }
            GraphTargetItem item = expression1(allOpenedNamespaces, prim, GraphTargetItem.NOPRECEDENCE, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
            commaItems.add(item);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
        if (commaItems.size() == 1) {
            return commaItems.get(0);
        }
        return new CommaExpressionItem(null, null, commaItems);
    }

    /**
     * Lexer can return XML opentags instead of greater. In expression, we need
     * greater sign only
     *
     * @param symb Symbol to fix
     */
    private void xmlToLowerThanFix(ParsedSymbol symb) {
        if (symb.isType(SymbolType.XML_STARTVARTAG_BEGIN, SymbolType.XML_STARTTAG_BEGIN)) {
            lexer.yypushbackstr(symb.value.toString().substring(1)); //parse again as LOWER_THAN
            String pb = symb.value.toString().substring(1);
            symb.type = SymbolType.LOWER_THAN;
            symb.group = SymbolGroup.OPERATOR;
            symb.value = "<";
            if (pb.charAt(0) == '=') {
                symb.type = SymbolType.LOWER_EQUAL;
                symb.value = "<=";
                pb = pb.substring(1);
            }
            lexer.yypushbackstr(pb); //parse again as LOWER_THAN
        }
    }

    private void regexpToDivideFix(ParsedSymbol symb) {
        if (symb.isType(SymbolType.REGEXP)) {
            String pb = symb.value.toString().substring(1);
            symb.type = SymbolType.DIVIDE;
            symb.group = SymbolGroup.OPERATOR;
            symb.value = "/";
            if (pb.charAt(0) == '=') {
                symb.type = SymbolType.ASSIGN_DIVIDE;
                symb.value = "/=";
                pb = pb.substring(1);
            }
            lexer.yypushbackstr(pb); //parse again as DIVIDE

        }
    }

    private ParsedSymbol peekExprToken() throws IOException, AVM2ParseException, InterruptedException {
        ParsedSymbol lookahead = lex();
        xmlToLowerThanFix(lookahead);
        regexpToDivideFix(lookahead);

        lexer.pushback(lookahead);
        return lookahead;
    }

    private GraphTargetItem expression1(List<List<NamespaceItem>> allOpenedNamespaces, GraphTargetItem lhs, int min_precedence, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
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
                mhs = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);
                expectedType(SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
            if (rhs == null) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekExprToken();
            while ((lookahead.type.isBinary() && lookahead.type.getPrecedence() < /* > on wiki */ op.type.getPrecedence())
                    || (lookahead.type.isRightAssociative() && lookahead.type.getPrecedence() == op.type.getPrecedence())) {
                rhs = expression1(allOpenedNamespaces, rhs, lookahead.type.getPrecedence(), thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
                lookahead = peekExprToken();
            }

            switch (op.type) {
                case AS:
                    //GraphTargetItem type = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables);

                    lhs = new AsTypeAVM2Item(null, null, lhs, rhs); //???
                    allowRemainder = false;
                    break;

                case IN:
                    lhs = new InAVM2Item(null, null, lhs, rhs);
                    break;

                case TERNAR: //???
                    lhs = new TernarOpItem(null, null, lhs, mhs, rhs);
                    break;
                case SHIFT_LEFT:
                    lhs = new LShiftAVM2Item(null, null, lhs, rhs);
                    break;
                case SHIFT_RIGHT:
                    lhs = new RShiftAVM2Item(null, null, lhs, rhs);
                    break;
                case USHIFT_RIGHT:
                    lhs = new URShiftAVM2Item(null, null, lhs, rhs);
                    break;
                case BITAND:
                    lhs = new BitAndAVM2Item(null, null, lhs, rhs);
                    break;
                case BITOR:
                    lhs = new BitOrAVM2Item(null, null, lhs, rhs);
                    break;
                case DIVIDE:
                    lhs = new DivideAVM2Item(null, null, lhs, rhs);
                    break;
                case MODULO:
                    lhs = new ModuloAVM2Item(null, null, lhs, rhs);
                    break;
                case EQUALS:
                    lhs = new EqAVM2Item(null, null, lhs, rhs);
                    break;
                case STRICT_EQUALS:
                    lhs = new StrictEqAVM2Item(null, null, lhs, rhs);
                    break;
                case NOT_EQUAL:
                    lhs = new NeqAVM2Item(null, null, lhs, rhs);
                    break;
                case STRICT_NOT_EQUAL:
                    lhs = new StrictNeqAVM2Item(null, null, lhs, rhs);
                    break;
                case LOWER_THAN:
                    lhs = new LtAVM2Item(null, null, lhs, rhs);
                    break;
                case LOWER_EQUAL:
                    lhs = new LeAVM2Item(null, null, lhs, rhs);
                    break;
                case GREATER_THAN:
                    lhs = new GtAVM2Item(null, null, lhs, rhs);
                    break;
                case GREATER_EQUAL:
                    lhs = new GeAVM2Item(null, null, lhs, rhs);
                    break;
                case AND:
                    lhs = new AndItem(null, null, lhs, rhs);
                    break;
                case OR:
                    lhs = new OrItem(null, null, lhs, rhs);
                    break;
                case MINUS:
                    lhs = new SubtractAVM2Item(null, null, lhs, rhs);
                    break;
                case MULTIPLY:
                    lhs = new MultiplyAVM2Item(null, null, lhs, rhs);
                    break;
                case PLUS:
                    lhs = new AddAVM2Item(null, null, lhs, rhs);
                    break;
                case XOR:
                    lhs = new BitXorAVM2Item(null, null, lhs, rhs);
                    break;
                case INSTANCEOF:
                    lhs = new InstanceOfAVM2Item(null, null, lhs, rhs);
                    break;
                case IS:
                    GraphTargetItem istype = rhs;
                    lhs = new IsTypeAVM2Item(null, null, lhs, istype);
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
                case ASSIGN_AND:
                case ASSIGN_OR:
                    GraphTargetItem assigned = rhs;
                    switch (op.type) {
                        case ASSIGN_BITAND:
                            assigned = new BitAndAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_BITOR:
                            assigned = new BitOrAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_DIVIDE:
                            assigned = new DivideAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MINUS:
                            assigned = new SubtractAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MODULO:
                            assigned = new ModuloAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MULTIPLY:
                            assigned = new MultiplyAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_PLUS:
                            assigned = new AddAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_LEFT:
                            assigned = new LShiftAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_RIGHT:
                            assigned = new RShiftAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_USHIFT_RIGHT:
                            assigned = new URShiftAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_XOR:
                            assigned = new BitXorAVM2Item(null, null, lhs, assigned);
                            break;
                        case ASSIGN_AND:
                            assigned = new AndItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_OR:
                            assigned = new OrItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN:
                        default:
                            //assigned = assigned;
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
            }
        }
        if (lhs instanceof ParenthesisItem) {
            GraphTargetItem coerced = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);
            if (coerced != null && isType(((ParenthesisItem) lhs).value)) {
                lhs = new CoerceAVM2Item(null, null, ((ParenthesisItem) lhs).value, coerced);
            }
        }

        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private GraphTargetItem expressionPrimary(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<AssignableAVM2Item> variables, ABC abc) throws IOException, AVM2ParseException, InterruptedException {
        if (debugMode) {
            System.out.println("primary:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean allowMemberOrCall = false;
        switch (s.type) {
            case PREPROCESSOR:
                expectedType(SymbolType.PARENT_OPEN);
                switch ("" + s.value) {
                    //AS3
                    case "hasnext":
                        GraphTargetItem hnIndex = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        GraphTargetItem hnObject = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        ret = new HasNextAVM2Item(null, null, hnIndex, hnObject);
                        break;
                    case "newactivation":
                        ret = new NewActivationAVM2Item(null, null);
                        break;
                    case "nextname":
                        GraphTargetItem nnIndex = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        GraphTargetItem nnObject = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);

                        ret = new NextNameAVM2Item(null, null, nnIndex, nnObject);
                        allowMemberOrCall = true;
                        break;
                    case "nextvalue":
                        GraphTargetItem nvIndex = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        GraphTargetItem nvObject = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);

                        ret = new NextNameAVM2Item(null, null, nvIndex, nvObject);
                        allowMemberOrCall = true;
                        break;
                    //Both ASs
                    case "dup":
                        ret = new DuplicateItem(null, null, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                        break;
                    case "push":
                        ret = new PushItem(expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc));
                        break;
                    case "pop":
                        ret = new PopItem(null, null);
                        break;
                    case "goto": //TODO
                    case "multiname":
                        throw new AVM2ParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline());
                    default:
                        throw new AVM2ParseException("Unknown preprocessor instruction: §§" + s.value, lexer.yyline());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case REGEXP:
                String p = (String) s.value;
                p = p.substring(1);
                int spos = p.lastIndexOf('/');
                String mod = p.substring(spos + 1);
                p = p.substring(0, spos);
                ret = new RegExpAvm2Item(p, mod, null, null);
                allowMemberOrCall = true;

                break;
            case XML_STARTTAG_BEGIN:
            case XML_STARTVARTAG_BEGIN:
            case XML_CDATA:
            case XML_COMMENT:
                lexer.pushback(s);
                ret = xml(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                break;
            case STRING:
                ret = new StringAVM2Item(null, null, s.value.toString());
                allowMemberOrCall = true;
                break;
            case NEGATE:
                ret = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc);
                ret = new BitNotAVM2Item(null, null, ret);

                break;
            case PLUS:
                GraphTargetItem nump = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);                    
                if (abc.hasFloatSupport()) {
                    ret = new UnPlusAVM2Item(null, null, nump);
                } else {
                    ret = new CoerceAVM2Item(null, null, nump, TypeItem.NUMBER);
                }
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new DoubleValueAVM2Item(null, null, -(Double) s.value);
                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new IntegerValueAVM2Item(null, null, -(Integer) s.value);
                } else if (s.isType(SymbolType.DECIMAL)) {
                    ret = new DecimalValueAVM2Item(null, null, ((Decimal128) s.value).multiply(Decimal128.NEG1));
                            } else if (s.isType(SymbolType.FLOAT)) {
                    ret = new FloatValueAVM2Item(null, null, -(Float) s.value);
                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);
                    if (num instanceof IntegerValueAVM2Item) {
                        ((IntegerValueAVM2Item) num).value = -((IntegerValueAVM2Item) num).value;
                        ((IntegerValueAVM2Item) num).detectFormat();
                        ret = num;
                    } else if (num instanceof DoubleValueAVM2Item) {
                        Double d = ((DoubleValueAVM2Item) num).value;
                        if (d.isInfinite()) {
                            ((DoubleValueAVM2Item) num).value = Double.NEGATIVE_INFINITY;
                        } else {
                            ((DoubleValueAVM2Item) num).value = -d;
                        }
                        ret = (num);
                    } else {
                        ret = (new NegAVM2Item(null, null, num));
                    }
                }
                break;
            case TYPEOF:
                ret = new TypeOfAVM2Item(null, null, expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc));
                break;
            case TRUE:
                ret = new BooleanAVM2Item(null, null, true);

                break;
            case NULL:
                ret = new NullAVM2Item(null, null);

                break;
            case UNDEFINED:
                ret = new UndefinedAVM2Item(null, null);
                break;
            case FALSE:
                ret = new BooleanAVM2Item(null, null, false);

                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                List<NameValuePair> nvs = new ArrayList<>();

                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.STRING, SymbolType.INTEGER, SymbolType.DOUBLE, SymbolType.PARENT_OPEN);

                    GraphTargetItem n;
                    if (s.type == SymbolType.PARENT_OPEN) { //special for obfuscated SWFs
                        n = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);
                        expectedType(SymbolType.PARENT_CLOSE);
                    } else {
                        n = new StringAVM2Item(null, null, s.value.toString());
                    }
                    expectedType(SymbolType.COLON);
                    GraphTargetItem v = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);

                    NameValuePair nv = new NameValuePair(n, v);
                    nvs.add(nv);
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new NewObjectAVM2Item(null, null, nvs);
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, inBrackets, registerVars, inFunction, inMethod, variables, abc);
                ret = new NewArrayAVM2Item(null, null, inBrackets);
                allowMemberOrCall = true;

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
                ret = function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, fname, false, variables, abc);
                allowMemberOrCall = true;
                break;
            case NAN:
                ret = new NanAVM2Item(null, null);

                break;
            case INFINITY:
                ret = new DoubleValueAVM2Item(null, null, Double.POSITIVE_INFINITY);

                break;
            case INTEGER:
                ret = new IntegerValueAVM2Item(null, null, (Integer) s.value);

                break;
            case DOUBLE:
                ret = new DoubleValueAVM2Item(null, null, (Double) s.value);
                allowMemberOrCall = true; // 5.2.toString();
                break;           
            case DECIMAL:
                if (!abc.hasDecimalSupport()) {
                    throw new AVM2ParseException("The ABC has no decimal support", lexer.yyline());
                }
                ret = new DecimalValueAVM2Item(null, null, (Decimal128) s.value);
                allowMemberOrCall = true;
                break;
            case FLOAT:
                if (!abc.hasFloatSupport()) {
                    throw new AVM2ParseException("The ABC has no float support", lexer.yyline());
                }
                ret = new FloatValueAVM2Item(null, null, (Float) s.value);
                allowMemberOrCall = true;
                break;
            case FLOAT4:
                if (!abc.hasFloat4Support()) {
                    //parse again as method call
                    lexer.yypushbackstr(lexer.yytext().substring("float4".length()));                                        
                    lexer.pushback(new ParsedSymbol(SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, "float4"));
                    ret = name(allOpenedNamespaces, thisType, pkg, needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses, abc);                    
                } else {
                    ret = new Float4ValueAVM2Item(null, null, (Float4) s.value);                    
                }
                allowMemberOrCall = true;
                break;
            case DELETE:
                GraphTargetItem varDel = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);
                if (!isNameOrProp(varDel)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }
                ret = new DeletePropertyAVM2Item(varDel, lexer.yyline());
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem varincdec = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false/*?*/, variables, abc);
                if (!isNameOrProp(varincdec)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, null, varincdec);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, null, varincdec);
                }

                break;
            case NOT:
                ret = new NotItem(null, null, expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc));

                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, null, expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc));
                expectedType(SymbolType.PARENT_CLOSE);
                if (ret.value == null) {
                    throw new AVM2ParseException("Expression in parenthesis expected", lexer.yyline());
                }
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
                    ret = function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, ffname, false, variables, abc);
                } else if (s.type == SymbolType.LOWER_THAN) {
                    GraphTargetItem subtype = type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    expectedType(SymbolType.GREATER_THAN);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.BRACKET_OPEN);
                    lexer.pushback(s);
                    List<GraphTargetItem> params = new ArrayList<>();
                    brackets(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, params, registerVars, inFunction, inMethod, variables, abc);
                    ret = new InitVectorAVM2Item(subtype, params, openedNamespaces);
                } else if (s.type == SymbolType.PARENT_OPEN) {
                    GraphTargetItem newvar = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    newvar = applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.PARENT_OPEN);
                    ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc), abcIndex);

                } else {
                    lexer.pushback(s);
                    GraphTargetItem newvar = name(allOpenedNamespaces, thisType, pkg, needsActivation, false /*?*/, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses, abc);
                    newvar = applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables, abc);
                    expectedType(SymbolType.PARENT_OPEN);
                    ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc), abcIndex);
                }
                allowMemberOrCall = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
            case ATTRIBUTE:
                lexer.pushback(s);
                ret = name(allOpenedNamespaces, thisType, pkg, needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses, abc);
                allowMemberOrCall = true;

                //var = memberOrCall(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, var, registerVars, inFunction, inMethod, variables);
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
            ret = memberOrCall(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private ActionScriptLexer lexer = null;

    private List<String> constantPool;

    private boolean parseImportsUsages(List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Reference<Integer> numberUsageRef, Reference<Integer> numberPrecisionRef, Reference<Integer> numberRoundingRef, ABC abc) throws IOException, AVM2ParseException, InterruptedException {

        boolean isEmpty = true;
        ParsedSymbol s;        

        s = lex();
        while (s.isType(SymbolType.IMPORT, SymbolType.USE)) {
            
            if (s.isType(SymbolType.IMPORT)) {
                isEmpty = false;
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                DottedChain fullName = new DottedChain(new String[]{});
                fullName = fullName.add(s.value.toString(), "");
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
                    fullName = fullName.add(s.value.toString(), "");
                    s = lex();
                }

                /*else if (isUse) {
                    //Note: in this case, fullName attribute will be changed to real NS insude NamespaceItem
                    openedNamespaces.add(new NamespaceItem(fullName, Namespace.KIND_NAMESPACE));
                } else */
                if (isStar) {
                    openedNamespaces.add(new NamespaceItem(fullName, Namespace.KIND_PACKAGE));
                } else {
                    importedClasses.add(fullName);
                }
                expected(s, lexer.yyline(), SymbolType.SEMICOLON);                
            } else if (s.isType(SymbolType.USE)) {
                isEmpty = false;
                do {
                    s = lex();
                    if (s.isType(SymbolType.NAMESPACE)) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        DottedChain fullName = new DottedChain(new String[]{});
                        fullName = fullName.add(s.value.toString(), "");
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
                            fullName = fullName.add(s.value.toString(), "");
                            s = lex();
                        }
                        lexer.pushback(s);
                    } else {
                        if (!abc.hasDecimalSupport()) {
                            throw new AVM2ParseException("Invalid use kind", lexer.yyline());
                        }
                        
                        expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                        String pragmaItemName = (String) s.value;
                        switch (pragmaItemName) {
                            case "Number":
                                numberUsageRef.setVal(NumberContext.USE_NUMBER);
                                break;
                            case "decimal":
                                numberUsageRef.setVal(NumberContext.USE_DECIMAL);
                                break;
                            case "double":
                                numberUsageRef.setVal(NumberContext.USE_DOUBLE);
                                break;
                            case "int":
                                numberUsageRef.setVal(NumberContext.USE_INT);
                                break;
                            case "uint":
                                numberUsageRef.setVal(NumberContext.USE_UINT);
                                break;
                            case "rounding":
                                s = lex();
                                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                                String roundingIdentifier = (String) s.value;
                                int rounding;
                                switch (roundingIdentifier) {
                                    case "CEILING":
                                        rounding = NumberContext.ROUND_CEILING;
                                        break;
                                    case "UP":
                                        rounding = NumberContext.ROUND_UP;
                                        break;
                                    case "HALF_UP":
                                        rounding = NumberContext.ROUND_HALF_UP;
                                        break;
                                    case "HALF_EVEN":
                                        rounding = NumberContext.ROUND_HALF_EVEN;
                                        break;
                                    case "HALF_DOWN":
                                        rounding = NumberContext.ROUND_HALF_DOWN;
                                        break;
                                    case "DOWN":
                                        rounding = NumberContext.ROUND_DOWN;
                                        break;
                                    case "FLOOR":
                                        rounding = NumberContext.ROUND_FLOOR;
                                        break;
                                    default:
                                        throw new AVM2ParseException("Rounding expected - one of: CEILING, UP, HALF_UP, HALF_EVEN, HALF_DOWN, DOWN, FLOOR", lexer.yyline());
                                }
                                numberRoundingRef.setVal(rounding);
                                break;
                            case "precision":
                                s = lex();
                                expected(s, lexer.yyline(), SymbolType.INTEGER);
                                int precision = (Integer) s.value;
                                if (precision < 1 || precision > 34) {
                                    throw new AVM2ParseException("Invalid precision - must be between 1 and 34", lexer.yyline());
                                }
                                numberPrecisionRef.setVal(precision);
                                break;
                            default:
                                throw new AVM2ParseException("Invalid use kind", lexer.yyline());
                        }
                    }                    
                    s = lex();
                }while(s.isType(SymbolType.COMMA));
                expected(s, lexer.yyline(), SymbolType.SEMICOLON);
            }
            /*boolean isUse = s.type == SymbolType.USE;
            if (isUse) {
                
                expectedType(SymbolType.NAMESPACE);
            }*/
            
            s = lex();
        }
        lexer.pushback(s);
        return !isEmpty;
    }

    private List<GraphTargetItem> parseScript(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            int scriptIndex, 
            String fileName, 
            Reference<Integer> numberContextRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<AssignableAVM2Item> sinitVariables
            ) throws IOException, AVM2ParseException, CompilationException, InterruptedException {

        //int scriptPrivateNs;
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        if (fileName.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        List<GraphTargetItem> items = new ArrayList<>();
        Reference<Integer> numberUsageRef = new Reference<>(NumberContext.USE_NUMBER);
        Reference<Integer> numberRoundingRef = new Reference<>(NumberContext.ROUND_HALF_EVEN);
        Reference<Integer> numberPrecisionRef = new Reference<>(34);
        scriptTraits(importedClasses, openedNamespaces, allOpenedNamespaces, scriptIndex, fileName, items, numberUsageRef, numberRoundingRef, numberPrecisionRef, abc, sinitNeedsActivation, sinitVariables);
        
        NumberContext nc = new NumberContext(numberUsageRef.getVal(), numberPrecisionRef.getVal(), numberRoundingRef.getVal());
        if (!nc.isDefault()) {
            numberContextRef.setVal(nc.toParam());
        }
        return items;
    }

    /**
     * Converts string to script traits.
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param allOpenedNamespaces All opened namespaces
     * @param str String to parse
     * @param fileName File name
     * @param scriptIndex Script index
     * @param numberContextRef Number context reference
     * @param abc ABC
     * @param sinitNeedsActivation Script initializer needs activation
     * @param sinitVariables Script initializer variables
     * @return List of script traits
     * @throws AVM2ParseException On parsing error
     * @throws IOException On I/O error
     * @throws CompilationException On compilation error
     * @throws InterruptedException On interrupt
     */
    public List<GraphTargetItem> scriptTraitsFromString(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces, 
            String str, 
            String fileName, 
            int scriptIndex, 
            Reference<Integer> numberContextRef, 
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<AssignableAVM2Item> sinitVariables
            ) throws AVM2ParseException, IOException, CompilationException, InterruptedException {
        lexer = new ActionScriptLexer(str);

        List<GraphTargetItem> ret = parseScript(importedClasses, openedNamespaces, allOpenedNamespaces, scriptIndex, fileName, numberContextRef, abc, sinitNeedsActivation, sinitVariables);
        if (lexer.lex().type != SymbolType.EOF) {
            throw new AVM2ParseException("Parsing finished before end of the file", lexer.yyline());
        }
        return ret;
    }

    /**
     * Adds script from tree.
     * @param sinitVariables Script initializer variables
     * @param sinitNeedsActivation Script initializer needs activation
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param allOpenedNamespaces All opened namespaces
     * @param items Items
     * @param classPos Class position
     * @param documentClass Document class
     * @param numberContext Number context
     * @throws AVM2ParseException On parsing error
     * @throws CompilationException On compilation error
     */
    public void addScriptFromTree(List<AssignableAVM2Item> sinitVariables, boolean sinitNeedsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, List<List<NamespaceItem>> allOpenedNamespaces, List<GraphTargetItem> items, int classPos, String documentClass, Integer numberContext) throws AVM2ParseException, CompilationException {
        AVM2SourceGenerator gen = new AVM2SourceGenerator(abcIndex);
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<>(), 0, Boolean.FALSE, 0);
        localData.documentClass = documentClass;
        localData.numberContext = numberContext;
        ScriptInfo si = new ScriptInfo();
        int scriptIndex = abcIndex.getSelectedAbc().script_info.size();
        abcIndex.getSelectedAbc().script_info.add(si);
        try {
            gen.generateScriptInfo(sinitVariables, sinitNeedsActivation, importedClasses, openedNamespaces, scriptIndex, si, allOpenedNamespaces, localData, items, classPos);
        } catch (Exception ex) {
            Logger.getLogger(ActionScript3Parser.class.getName()).log(Level.FINE, "Script generation exception", ex);
            abcIndex.getSelectedAbc().script_info.remove(si);
            throw ex;
        }

        abcIndex.getSelectedAbc().fireChanged();
    }

    /**
     * Adds script.
     * @param s Source code
     * @param fileName File name
     * @param classPos Class position
     * @param scriptIndex Script index
     * @param documentClass Document class
     * @param abc ABC
     * @throws AVM2ParseException On parsing error
     * @throws IOException On I/O error
     * @throws CompilationException On compilation error
     * @throws InterruptedException On interrupt
     */
    public void addScript(String s, String fileName, int classPos, int scriptIndex, String documentClass, ABC abc) throws AVM2ParseException, IOException, CompilationException, InterruptedException {
        List<List<NamespaceItem>> allOpenedNamespaces = new ArrayList<>();
        Reference<Integer> numberContextRef = new Reference<>(null);
        Reference<Boolean> sinitNeedsActivation = new Reference<>(false);
        List<AssignableAVM2Item> sinitVariables = new ArrayList<>();
        List<DottedChain> importedClasses = new ArrayList<>();
        List<NamespaceItem> openedNamespaces = new ArrayList<>();
        List<GraphTargetItem> traits = scriptTraitsFromString(importedClasses, openedNamespaces, allOpenedNamespaces, s, fileName, scriptIndex, numberContextRef, abc, sinitNeedsActivation, sinitVariables);
        addScriptFromTree(sinitVariables, sinitNeedsActivation.getVal(), importedClasses, openedNamespaces, allOpenedNamespaces, traits, classPos, documentClass, numberContextRef.getVal());
    }

    /**
     * Constructor.
     * @param abcIndex ABC index
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public ActionScript3Parser(AbcIndexing abcIndex) throws IOException, InterruptedException {
        SWF.initPlayer();

        this.abcIndex = abcIndex;
    }

    /**
     * Compiles AS3 source code.
     * @param src Source code
     * @param abc ABC
     * @param abcIndex ABC index
     * @param fileName File name
     * @param classPos Class position
     * @param scriptIndex Script index
     * @param air AIR
     * @param documentClass Document class
     * @throws AVM2ParseException On parsing error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     * @throws CompilationException On compilation error
     */
    public static void compile(String src, ABC abc, AbcIndexing abcIndex, String fileName, int classPos, int scriptIndex, boolean air, String documentClass) throws AVM2ParseException, IOException, InterruptedException, CompilationException {
        //List<ABC> parABCs = new ArrayList<>();
        SWF.initPlayer();
        ActionScript3Parser parser = new ActionScript3Parser(abcIndex);
        boolean success = false;
        ABC originalAbc = ((ABCContainerTag) ((Tag) abc.parentTag).cloneTag()).getABC();
        Set<Integer> modifiedScripts = new HashSet<>();        
        for (int i = 0; i < abc.script_info.size(); i++) {
            if (abc.script_info.get(i).isModified()) {
                modifiedScripts.add(i);
            }
        }
        try {
            parser.addScript(src, fileName, classPos, scriptIndex, documentClass, abc);
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
                abc.resetMethodIndexing();
                
                for (int i = 0; i < abc.script_info.size(); i++) {
                    if (modifiedScripts.contains(i)) {
                        abc.script_info.get(i).setModified(true);
                    }
                }
            }
        }
    }

    /**
     * Compiles AS3 source code.
     * @param swf SWF
     * @param srcFile Source file
     * @param destFile Target file
     * @param classPos Class position
     * @param scriptIndex Script index
     */
    public static void compile(SWF swf, String srcFile, String destFile, int classPos, int scriptIndex) {
        System.err.println("WARNING: AS3 compiler is not finished yet. This is only used for debuggging!");
        try {
            SWF.initPlayer();
            ABC abc = new ABC(null);
            AbcIndexing abcIndex = swf.getAbcIndex();
            abcIndex.selectAbc(abc);
            ActionScript3Parser parser = new ActionScript3Parser(abcIndex);
            parser.addScript(new String(Helper.readFile(srcFile), Utf8Helper.charset), srcFile, classPos, scriptIndex, swf.getDocumentClass(), abc);
            try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(destFile)))) {
                abc.saveToStream(fos);
            }
        } catch (Exception ex) {
            Logger.getLogger(ActionScript3Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }
}
