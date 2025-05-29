/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.avm2.parser.script.*;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.NumberContext;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.AddAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.simpleparser.SimpleParseException;
import com.jpexs.decompiler.flash.simpleparser.CatchScope;
import com.jpexs.decompiler.flash.simpleparser.ClassScope;
import com.jpexs.decompiler.flash.simpleparser.FunctionScope;
import com.jpexs.decompiler.flash.simpleparser.Scope;
import com.jpexs.decompiler.flash.simpleparser.SimpleParser;
import com.jpexs.decompiler.flash.simpleparser.Type;
import com.jpexs.decompiler.flash.simpleparser.Variable;
import com.jpexs.decompiler.flash.simpleparser.VariableOrScope;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActionScript 3 parser.
 *
 * @author JPEXS
 */
public class ActionScript3SimpleParser implements SimpleParser {
    
    private static final GraphTargetDialect DIALECT = AVM2GraphTargetDialect.INSTANCE;

    private long uniqLast = 0;

    private final boolean debugMode = false;

    private static final String AS3_NAMESPACE = "http://adobe.com/AS3/2006/builtin";
    private final ABC abc;

//    private final AbcIndexing abcIndex;

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private void commands(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("commands:");
        }
        while (command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc)) {
        }
        if (debugMode) {
            System.out.println("/commands");
        }        
    }

    private boolean type(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();
        if (s.type == SymbolType.MULTIPLY) {
            //return TypeItem.UNBOUNDED;
            variables.add(new Type(false, "*", s.position));
            return true;
        } else if (s.type == SymbolType.VOID) {
            //return new TypeItem(DottedChain.VOID);
            variables.add(new Type(false, "void", s.position));
            return true;
        } else {
            lexer.pushback(s);
        }

        //GraphTargetItem t = ;
        boolean t = name(allOpenedNamespaces, thisType, pkg, needsActivation, true, openedNamespaces, null, false, false, variables, importedClasses, abc);
        //t =         
        applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, t, new HashMap<>(), false, false, variables, abc);
        //return t;
        return true;
    }

    private boolean memberOrCall(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("memberOrCall:");
        }
        ParsedSymbol s = lex();
        boolean ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.NULL_DOT, SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME, SymbolType.FILTER, SymbolType.DESCENDANTS)) {
            switch (s.type) {
                case BRACKET_OPEN:
                case DOT:
                case NULL_DOT:
                case TYPENAME:
                    lexer.pushback(s);
                    ret = member(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
                    break;
                case FILTER:
                    needsActivation.setVal(true);
                    //ret = new XMLFilterAVM2Item(ret,;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, inMethod, variables, true, abc);
                    //, openedNamespaces);
                    expectedType(SymbolType.PARENT_CLOSE);
                    ret = true;
                    break;
                case PARENT_OPEN:
                    //ret = new CallAVM2Item(openedNamespaces, lexer.yyline(), ret,;
                    call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    //, abcIndex);
                    ret = true;
                    break;
                case DESCENDANTS:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                    //ret = new GetDescendantsAVM2Item(ret, s.type == SymbolType.MULTIPLY ? null : s.value.toString(), openedNamespaces);
                    ret = true;
                    break;

            }
            s = lex();
        }
        if (s.type == SymbolType.INCREMENT) {
            if (!ret) {
                //!isNameOrProp(ret)                 
                throw new AVM2ParseException("Invalid assignment", lexer.yyline(), s.position);
            }
            //ret = new PostIncrementAVM2Item(null, null, ret);
            ret = true;
            s = lex();

        } else if (s.type == SymbolType.DECREMENT) {
            if (!ret) { 
                //(!isNameOrProp(ret)) {
                throw new AVM2ParseException("Invalid assignment", lexer.yyline(), s.position);
            }
            //ret = new PostDecrementAVM2Item(null, null, ret);
            ret = true;
            s = lex();
        }

        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/memberOrCall");
        }
        return ret;
    }

    private boolean applyType(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        boolean ret = obj;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.TYPENAME) {
            //List<GraphTargetItem> params = new ArrayList<>();
            do {
                s = lex();
                if (s.isType(SymbolType.MULTIPLY)) {
                    //params.add(new NullAVM2Item(null, null));
                } else {
                    lexer.pushback(s);
                    //params.add(;
                    expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc);
                    
                }
                s = lex();
            } while (s.type == SymbolType.COMMA);
            if (s.type == SymbolType.USHIFT_RIGHT) {
                s = new ParsedSymbol(s.position + 2, SymbolGroup.OPERATOR, SymbolType.GREATER_THAN);
                lexer.pushback(s);
                s = new ParsedSymbol(s.position + 1, SymbolGroup.OPERATOR, SymbolType.GREATER_THAN);
                lexer.pushback(s);
            }
            if (s.type == SymbolType.SHIFT_RIGHT) {
                s = new ParsedSymbol(s.position + 1, SymbolGroup.OPERATOR, SymbolType.GREATER_THAN);
                lexer.pushback(s);
            }
            expected(s, lexer.yyline(), SymbolType.GREATER_THAN);
            //ret = new ApplyTypeAVM2Item(null, null, ret, params);
            ret = true;
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private boolean member(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("member:");
        }
        boolean ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT, SymbolType.NULL_DOT, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME)) {
            ParsedSymbol s2 = lex();
            boolean attr = false;
            boolean nullDot = false;
            if (s.type == SymbolType.NULL_DOT) {
                nullDot = true;
                lexer.pushback(s2);
            } else if (s.type == SymbolType.DOT) {
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
                //ret = ;
                applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
                ret = true;
                s = lex();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                //GraphTargetItem index = ;
                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                expectedType(SymbolType.BRACKET_CLOSE);
                //ret = new IndexAVM2Item(attr, ret, index, null, openedNamespaces);
                ret = true;
                s = lex();
            } else {
                s = lex();
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                String propName = s.value.toString(); //Can be *
                int propPosition = s.position;
                //GraphTargetItem propItem = null;
                s = lex();
                //String nsSuffix = "";
                //GraphTargetItem ns = null;
                if (s.type == SymbolType.NAMESPACE_OP) {
                    //ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, false, null, lexer.yyline(), new DottedChain(new String[]{propName}, new String[]{""} /*FIXME ???*/), null, openedNamespaces, abcIndex);
                    //variables.add((UnresolvedAVM2Item) ns);
                    variables.add(new Variable(false, propName, propPosition));
                    s = lex();
                    if (s.type == SymbolType.BRACKET_OPEN) {
                        //propItem = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.BRACKET_CLOSE);
                        //propName = null;
                    } else {
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        ///propName = s.value.toString();
                        //propItem = null;
                    }
                } else {
                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        //nsSuffix = "#" + s.value;
                    } else {
                        lexer.pushback(s);
                    }
                }
                /*
                if (ns != null) {
                    ret = new NamespacedAVM2Item(ns, propName, propItem, ret, attr, openedNamespaces, null);
                } else {
                    ret = new PropertyAVM2Item(ret, attr, propName, nsSuffix, abcIndex, openedNamespaces, new ArrayList<>(), nullDot);
                }*/
                ret = true;
                s = lex();
            }
        }
        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/member");
        }
        return ret;
    }

    private boolean name(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, boolean typeOnly, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, List<DottedChain> importedClasses, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
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
        int identPos = s.position;
        s = lex();
        boolean attrBracket = false;
        String nsSuffix = "";
        if (s.type == SymbolType.NAMESPACESUFFIX) {
            s = lex();
            nsSuffix = "#" + s.value;
        }

        name = name.add(attribute, name2, nsSuffix);
        //variables.add(new Variable(false, name.toPrintableString(true), identPos));
        while (s.isType(SymbolType.DOT)) {
            variables.add(new Variable(false, name.toPrintableString(true), identPos));
            //name += s.value.toString(); //. or ::
            s = lex();
            name2 = "";
            attribute = false;
            if (s.type == SymbolType.ATTRIBUTE) {
                attribute = true;
                s = lex();
                if (s.type == SymbolType.MULTIPLY) {
                    name2 += s.value.toString();
                    identPos = s.position;
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    name2 += s.value.toString();
                    identPos = s.position;
                } else {
                    if (s.type != SymbolType.BRACKET_OPEN) {
                        throw new AVM2ParseException("Attribute identifier or bracket expected", lexer.yyline(), s.position);
                    }
                    attrBracket = true;
                    continue;
                }
            } else {
                expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.NAMESPACE, SymbolType.MULTIPLY);
                name2 += s.value.toString();
                identPos = s.position;
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
                variables.add(new Variable(false, (nsAtribute ? "@" : "") + nsname + "::" + nsprop, s.position));
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                //nspropItem =    
                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                expectedType(SymbolType.BRACKET_CLOSE);
            }
            name = name.getWithoutLast();
            s = lex();
        } else {
            variables.add(new Variable(false, name.toPrintableString(true), identPos));
        }

        boolean ret = false;
        if (!name.isEmpty()) {
            //UnresolvedAVM2Item unr = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), name, null, openedNamespaces, abcIndex);
            //variables.add(unr);
            //variables.add(new Variable(false, name.toPrintableString(true), ))
            //ret = unr;
            ret = true;
        }
        if (nsname != null) {
            //UnresolvedAVM2Item ns = new UnresolvedAVM2Item(new ArrayList<>(), importedClasses, typeOnly, null, lexer.yyline(), new DottedChain(new String[]{nsname}), null, openedNamespaces, abcIndex);
            //variables.add(ns);
            //ret = new NamespacedAVM2Item(ns, nsprop, nspropItem, ret, nsAtribute, openedNamespaces, null);
        }
        if (s.type == SymbolType.BRACKET_OPEN) {
            lexer.pushback(s);
            if (attrBracket) {
                lexer.pushback(new ParsedSymbol(s.position - 1, SymbolGroup.OPERATOR, SymbolType.ATTRIBUTE, "@"));
                lexer.pushback(new ParsedSymbol(s.position - 2, SymbolGroup.OPERATOR, SymbolType.DOT, "."));
            }
            ret = member(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, AVM2ParseException, SimpleParseException {
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
            throw new AVM2ParseException("" + expStr + " expected but " + symb.type + " found", line, symb.position);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            //ret.add(;
            expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private void method(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, boolean isPrivate, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, String customAccess, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, boolean override, boolean isFinal, TypeItem thisType, List<NamespaceItem> openedNamespaces, boolean isStatic, String functionName, boolean isMethod, List<VariableOrScope> variables, ABC abc, int methodNamePos) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        //FunctionAVM2Item f = ;
        function(allOpenedNamespaces, metadata, pkg, isInterface, isNative, needsActivation, importedClasses, thisType, openedNamespaces, functionName, isMethod, variables, abc, methodNamePos);
        //return new MethodAVM2Item(allOpenedNamespaces, outsidePackage, isPrivate, f.metadata, f.pkg, f.isInterface, f.isNative, customAccess, f.needsActivation, f.hasRest, f.line, override, isFinal, isStatic, functionName, f.paramTypes, f.paramNames, f.paramValues, f.body, f.subvariables, f.retType);
    }

    private void function(List<List<NamespaceItem>> allOpenedNamespaces, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, TypeItem thisType, List<NamespaceItem> openedNamespaces, String functionName, boolean isMethod, List<VariableOrScope> variables, ABC abc, int functionNamePos) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

        openedNamespaces = new ArrayList<>(openedNamespaces); //local copy
        allOpenedNamespaces.add(openedNamespaces);
        int line = lexer.yyline();
        ParsedSymbol s;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();
        List<Integer> paramPositions = new ArrayList<>();
        //List<GraphTargetItem> paramTypes = new ArrayList<>();
        //List<GraphTargetItem> paramValues = new ArrayList<>();
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
            paramPositions.add(s.position);
            s = lex();
            if (!hasRest) {
                GraphTargetItem currentType;
                if (s.type == SymbolType.COLON) {
                    //paramTypes.add(currentType = ;
                    type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    s = lex();
                } else {
                    //paramTypes.add(currentType = TypeItem.UNBOUNDED);
                }
                if (s.type == SymbolType.ASSIGN) {
                    //GraphTargetItem currentValue =;
                    expression(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, null, isMethod, isMethod, isMethod, variables, false, abc);
                    //paramValues.add(currentValue);
                    s = lex();
                } /*else if (!paramValues.isEmpty()) {
                    throw new AVM2ParseException("Some of parameters do not have default values", lexer.yyline());
                }*/
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
            if (hasRest) {
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
            }
        }
        s = lex();
        //GraphTargetItem retType;
        if (s.type == SymbolType.COLON) {
            //retType = ;
            type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
        } else {
            //retType = TypeItem.UNBOUNDED;
            lexer.pushback(s);
        }
        List<GraphTargetItem> body = null;
        List<VariableOrScope> subvariables = new ArrayList<>();
        if (functionName != null && !functionName.isEmpty()) {
            subvariables.add(new Variable(true, functionName, functionNamePos));
        }
        //subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), false, "this", "", null, true, openedNamespaces, abcIndex, false));
        for (int i = 0; i < paramNames.size() - (hasRest ? 1 : 0); i++) {
            subvariables.add(new Variable(true, paramNames.get(i), paramPositions.get(i)));
            //subvariables.add(new NameAVM2Item(paramTypes.get(i), lexer.yyline(), false, paramNames.get(i), "", null, true, openedNamespaces, abcIndex, false));
           
        }
        if (hasRest) {
            subvariables.add(new Variable(true, paramNames.get(paramNames.size() - 1), paramPositions.get(paramNames.size() - 1)));
            //subvariables.add(new NameAVM2Item(TypeItem.UNBOUNDED, lexer.yyline(), false, paramNames.get(paramNames.size() - 1), "", null, true, openedNamespaces, abcIndex, false));
        }
        subvariables.add(new Variable(true, "arguments", -1)); //??? FIXME
        //subvariables.add(new NameAVM2Item(thisType, lexer.yyline(), false, "arguments", "", null, true, openedNamespaces, abcIndex, false));
        int parCnt = subvariables.size();
        Reference<Boolean> needsActivation2 = new Reference<>(false);
        if (!isInterface && !isNative) {
            expectedType(SymbolType.CURLY_OPEN);
            //body = ;
            commands(allOpenedNamespaces, thisType, pkg, needsActivation2, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, isMethod, 0, subvariables, abc);
            expectedType(SymbolType.CURLY_CLOSE);
        } else {
            expectedType(SymbolType.SEMICOLON);
        }

        /*for (int i = 0; i < parCnt; i++) {
            subvariables.remove(0);
        }*/
        //return new FunctionAVM2Item(metadata, pkg, isInterface, isNative, needsActivation2.getVal(), hasRest, line, functionName, paramTypes, paramNames, paramValues, body, subvariables, retType);        
        FunctionScope fs = new FunctionScope(subvariables);
        variables.add(fs);
    }

    private List<Map.Entry<String, Map<String, String>>> parseMetadata() throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
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

    private void classTraits(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, Reference<Boolean> cinitNeedsActivation, List<GraphTargetItem> cinit, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, NamespaceItem pkg, String classNameStr, boolean isInterface, List<GraphTargetItem> traits, Reference<Boolean> iinitNeedsActivation, Reference<GraphTargetItem> iinit, ABC abc, List<VariableOrScope> classVariables) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {

        /*NamespaceItem publicNs = new NamespaceItem("", Namespace.KIND_PACKAGE);
        NamespaceItem privateNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_PRIVATE);
        NamespaceItem protectedNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_PROTECTED);
        NamespaceItem staticProtectedNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_STATIC_PROTECTED);
        NamespaceItem packageInternalNs = new NamespaceItem(pkg.name, Namespace.KIND_PACKAGE_INTERNAL);
        NamespaceItem interfaceNs = new NamespaceItem(pkg.name.toRawString().isEmpty() ? classNameStr : pkg.name.toRawString() + ":" + classNameStr, Namespace.KIND_NAMESPACE);
*/
        openedNamespaces = new ArrayList<>(openedNamespaces);
        /*allOpenedNamespaces.add(openedNamespaces);
        for (List<NamespaceItem> ln : allOpenedNamespaces) {
            if (!ln.contains(publicNs)) {
                ln.add(publicNs);
            }
        }
        openedNamespaces.add(privateNs);
        openedNamespaces.add(protectedNs);
        openedNamespaces.add(staticProtectedNs);
*/
        Stack<Loop> cinitLoops = new Stack<>();
        Map<Loop, String> cinitLoopLabels = new HashMap<>();
        HashMap<String, Integer> cinitRegisterVars = new HashMap<>();

        looptraits:
        while (true) {
            //TypeItem thisType = new TypeItem(pkg.name.addWithSuffix(classNameStr));
            TypeItem thisType = null;
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
                        throw new AVM2ParseException("Only one final keyword allowed", lexer.yyline(), s.position);
                    }
                    preSymbols.add(s);
                    isFinal = true;
                } else if (s.type == SymbolType.OVERRIDE) {
                    if (isOverride) {
                        throw new AVM2ParseException("Only one override keyword allowed", lexer.yyline(), s.position);
                    }
                    preSymbols.add(s);
                    isOverride = true;
                } else if (s.type == SymbolType.STATIC) {
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have static traits", lexer.yyline(), s.position);
                    }
                    if (classNameStr == null) {
                        throw new AVM2ParseException("No static keyword allowed here", lexer.yyline(), s.position);
                    }
                    if (isStatic) {
                        throw new AVM2ParseException("Only one static keyword allowed", lexer.yyline(), s.position);
                    }
                    preSymbols.add(s);
                    isStatic = true;
                } else if (s.type == SymbolType.NAMESPACE) {
                    preSymbols.add(s);
                    break;
                } else if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        throw new AVM2ParseException("Only one native keyword allowed", lexer.yyline(), s.position);
                    }
                    preSymbols.add(s);
                    isNative = true;
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    customNs = s.value.toString();
                    if (isInterface) {
                        throw new AVM2ParseException("Namespace attributes are not permitted on interface methods", lexer.yyline(), s.position);
                    }
                    preSymbols.add(s);
                } else if (namespace != null) {
                    throw new AVM2ParseException("Only one access identifier allowed", lexer.yyline(), s.position);
                }
                switch (s.type) {
                    case PUBLIC:
                        //namespace = publicNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position);
                        }
                        preSymbols.add(s);
                        break;
                    case PRIVATE:
                        isPrivate = true;
                        //namespace = privateNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position);
                        }
                        preSymbols.add(s);
                        break;
                    case PROTECTED:
                        //namespace = protectedNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position);
                        }
                        preSymbols.add(s);
                        break;
                    case INTERNAL:
                        //namespace = packageInternalNs;
                        if (isInterface) {
                            throw new AVM2ParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position);
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

            /*if (isInterface) {
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
            }*/

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
                    int fnamePos = s.position;

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
                            throw new AVM2ParseException("Missing method name", lexer.yyline(), s.position);
                        }
                    } else {
                        fname = s.value.toString();
                    }
                    if (fname.equals(classNameStr)) { //constructor
                        if (isStatic) {
                            throw new AVM2ParseException("Constructor cannot be static", lexer.yyline(), s.position);
                        }
                        if (isOverride) {
                            throw new AVM2ParseException("Override flag not allowed for constructor", lexer.yyline(), s.position);
                        }
                        if (isFinal) {
                            throw new AVM2ParseException("Final flag not allowed for constructor", lexer.yyline(), s.position);
                        }
                        if (isInterface) {
                            throw new AVM2ParseException("Interface cannot have constructor", lexer.yyline(), s.position);
                        }
                        //iinit.setVal(;
                        method(allOpenedNamespaces, outsidePackage, isPrivate, metadata, pkg, false, false, customNs, iinitNeedsActivation, importedClasses, false, false, thisType, openedNamespaces, false, "", true, classVariables,abc, fnamePos);
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

                        //MethodAVM2Item ft;
                        //ft = ;
                        method(allOpenedNamespaces, outsidePackage, isPrivate, metadata, namespace, isInterface, isNative, customNs, new Reference<>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, fname, true, classVariables, abc, fnamePos);

                        /*if (isGetter) {
                            if (!ft.paramTypes.isEmpty()) {
                                throw new AVM2ParseException("Getter can't have any parameters", lexer.yyline());
                            }
                        }*/

                        /*if (isSetter) {
                            if (ft.paramTypes.size() != 1) {
                                throw new AVM2ParseException("Getter must have exactly one parameter", lexer.yyline());
                            }
                        }*/

                        /*if (isStatic && isInterface) {
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

                        traits.add(t);*/
                    }
                    //}
                    break;
                case NAMESPACE:
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have namespace fields", lexer.yyline(), s.position);
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
                        throw new AVM2ParseException("Override flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position);
                    }
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position);
                    }
                    if (isInterface) {
                        throw new AVM2ParseException("Interface cannot have variable/const fields", lexer.yyline(), s.position);
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();

                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        namespace = new NamespaceItem((Integer) s.value);
                        s = lex();
                    }

                    //GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        //type = ;
                        type(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, classVariables, abc);
                        s = lex();
                    } else {
                        //type = TypeItem.UNBOUNDED;
                    }

                    //GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        //value = ;
                        expression(allOpenedNamespaces, thisType, pkg, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, classVariables, false, abc);
                        s = lex();
                    }
                    /*GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(metadata, namespace, customNs, isStatic, vcname, type, value, lexer.yyline(), false);
                    } else {
                        tar = new SlotAVM2Item(metadata, namespace, customNs, isStatic, vcname, type, value, lexer.yyline());
                    }
                    traits.add(tar);*/
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);
                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }

                    //GraphTargetItem cmd = ;
                    boolean cmd = command(allOpenedNamespaces, null, null, cinitNeedsActivation, importedClasses, openedNamespaces, cinitLoops, cinitLoopLabels, cinitRegisterVars, true, false, 0, false, classVariables, abc);
                    //if (cmd != null) {
                    if (cmd) {
                        //traits.add(cmd);
                        //empty
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
            Reference<Integer> numberUsageRef,
            Reference<Integer> numberRoundingRef,
            Reference<Integer> numberPrecisionRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables
    ) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {

        Stack<Loop> sinitLoops = new Stack<>();
        Map<Loop, String> sinitLoopLabels = new HashMap<>();

        HashMap<String, Integer> sinitRegisterVars = new HashMap<>();
        while (scriptTraitsBlock(
                importedClasses,
                openedNamespaces,
                allOpenedNamespaces,
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
            Reference<Integer> numberUsageRef,
            Reference<Integer> numberRoundingRef,
            Reference<Integer> numberPrecisionRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            Stack<Loop> sinitLoops,
            Map<Loop, String> sinitLoopLabels,
            HashMap<String, Integer> sinitRegisterVars,
            List<VariableOrScope> sinitVariables
    ) throws AVM2ParseException, SimpleParseException, SimpleParseException, IOException, CompilationException, InterruptedException {
        ParsedSymbol s;
        boolean inPackage = false;
        s = lex();               
        
        
        //NamespaceItem publicNs;
        //NamespaceItem packageInternalNs;
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
            //publicNs = new NamespaceItem(pkgName, Namespace.KIND_PACKAGE);
            //packageInternalNs = new NamespaceItem(pkgName, Namespace.KIND_PACKAGE_INTERNAL);
            s = lex();
            inPackage = true;
        } else {
            //publicNs = null;
            //packageInternalNs = new NamespaceItem(scriptName + "$" + scriptIndex, Namespace.KIND_PRIVATE);
        }
        lexer.pushback(s);

        allOpenedNamespaces.add(openedNamespaces);
        //NamespaceItem emptyNs = new NamespaceItem("", Namespace.KIND_PACKAGE);
        //openedNamespaces.add(emptyNs);
        //NamespaceItem as3Ns = new NamespaceItem(AS3_NAMESPACE, Namespace.KIND_NAMESPACE);
        //as3Ns.forceResolve(abcIndex);
        //openedNamespaces.add(as3Ns);       

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
            //NamespaceItem ns = packageInternalNs;
            List<ParsedSymbol> preSymbols = new ArrayList<>();
            while (s.isType(SymbolType.FINAL, SymbolType.DYNAMIC, SymbolType.PUBLIC)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        throw new AVM2ParseException("Only one final keyword allowed", lexer.yyline(), s.position);
                    }
                    isFinal = true;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.PUBLIC) {
                    if (!inPackage) {
                        throw new AVM2ParseException("public only allowed inside package", lexer.yyline(), s.position);

                    }
                    if (isPublic) {
                        throw new AVM2ParseException("Only one public keyword allowed", lexer.yyline(), s.position);
                    }
                    isPublic = true;
                    //ns = publicNs;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.DYNAMIC) {
                    if (isDynamic) {
                        throw new AVM2ParseException("Only one dynamic keyword allowed", lexer.yyline(), s.position);
                    }
                    isDynamic = true;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        throw new AVM2ParseException("Only one native keyword allowed", lexer.yyline(), s.position);
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
                            //extendsTypeStr = ;
                            type(allOpenedNamespaces, null, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                            s = lex();
                        }
                        if (s.type == SymbolType.IMPLEMENTS) {
                            do {
                                //GraphTargetItem implementsTypeStr = ;
                                type(allOpenedNamespaces, null, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                                //interfaces.add(implementsTypeStr);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    } else {
                        if (s.type == SymbolType.EXTENDS) {
                            do {
                                //GraphTargetItem intExtendsTypeStr = ;
                                type(allOpenedNamespaces, null, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                                //interfaces.add(intExtendsTypeStr);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    }

                    /*if (extendsTypeStr != null) {
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
                    }*/

                    List<GraphTargetItem> cinit = new ArrayList<>();
                    Reference<Boolean> cinitNeedsActivation = new Reference<>(false);

                    Reference<GraphTargetItem> iinit = new Reference<>(null);
                    List<VariableOrScope> cinitVariables = new ArrayList<>();
                    List<VariableOrScope> iinitVariables = new ArrayList<>();
                    Reference<Boolean> iinitNeedsActivation = new Reference<>(false);

                    List<GraphTargetItem> subTraits = new ArrayList<>();

                    List<VariableOrScope> classVariables = new ArrayList<>();
                    cinitVariables = classVariables;
                    iinitVariables = classVariables;
                    
                    
                    
                    classTraits(allOpenedNamespaces, !inPackage, cinitNeedsActivation, cinit, importedClasses, subOpenedNamespaces, null, subNameStr, isInterface, subTraits, iinitNeedsActivation, iinit, abc, classVariables);

                    sinitVariables.add(new ClassScope(classVariables));

                    /*if (isInterface) {
                        traits.add(new InterfaceAVM2Item(metadata, importedClasses, ns, subOpenedNamespaces, isFinal, subNameStr, interfaces, subTraits, nullable));
                    } else {
                        traits.add(new ClassAVM2Item(metadata, importedClasses, ns, subOpenedNamespaces, isFinal, isDynamic, subNameStr, extendsTypeStr, interfaces, cinit, cinitNeedsActivation.getVal(), cinitVariables, iinit.getVal(), iinitVariables, subTraits, iinitNeedsActivation.getVal(), nullable));
                    }*/

                    expectedType(SymbolType.CURLY_CLOSE);
                    break;
                case FUNCTION:
                    isEmpty = false;
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String fname = s.value.toString();

                    //traits.add(;
                    method(allOpenedNamespaces, !inPackage, false, metadata, null, false, isNative, null, new Reference<>(false), importedClasses, false, isFinal, null, openedNamespaces, true, fname, true, sinitVariables, abc, s.position);
                    break;
                case CONST:
                case VAR:
                    isEmpty = false;
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position);
                    }

                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String vcname = s.value.toString();
                    s = lex();
                    //GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        //type = ;
                        type(allOpenedNamespaces, null, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                        s = lex();
                    } else {
                        //type = TypeItem.UNBOUNDED;
                    }

                    //GraphTargetItem value = null;

                    if (s.type == SymbolType.ASSIGN) {
                        //value = ;
                        expression(allOpenedNamespaces, null, null, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, sinitVariables, false, abc);
                        s = lex();
                    }
                    /*GraphTargetItem tar;
                    if (isConst) {
                        tar = new ConstAVM2Item(metadata, ns, null, true, vcname, type, value, lexer.yyline(), false);
                    } else {
                        tar = new SlotAVM2Item(metadata, ns, null, true, vcname, type, value, lexer.yyline());
                    }
                    traits.add(tar);*/
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                case NAMESPACE:
                    isEmpty = false;
                    if (isFinal) {
                        throw new AVM2ParseException("Final flag not allowed for namespaces", lexer.yyline(), s.position);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String nname = s.value.toString();
                    //String nval;
                    s = lex();

                    boolean generatedNs = false;
                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        //nval = s.value.toString();
                        s = lex();
                    } else {
                        generatedNs = true;
                        //nval = ns.name.toRawString() + ":" + nname;
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    //traits.add(new ConstAVM2Item(metadata, ns, null, true, nname, new TypeItem(DottedChain.NAMESPACE), new StringAVM2Item(null, null, nval), lexer.yyline(), generatedNs));
                    break;
                default:
                    lexer.pushback(s);

                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }

                    if (parseImportsUsages(importedClasses, openedNamespaces, numberUsageRef, numberPrecisionRef, numberRoundingRef, abc)) {
                        break;
                    }
                    boolean cmd = command(allOpenedNamespaces, null, null, sinitNeedsActivation, importedClasses, openedNamespaces, sinitLoops, sinitLoopLabels, sinitRegisterVars, true, false, 0, false, sinitVariables, abc);
                    if (cmd) {
                        //traits.add(cmd);
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

    private boolean expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<VariableOrScope> variables) throws IOException, AVM2ParseException, SimpleParseException {
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
                return false;
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

    private void xmltag(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> usesVars, List<String> openedTags, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s;
        //List<GraphTargetItem> rets = new ArrayList<>();
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
                    //addS(rets, sb);
                    //rets.add(;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    expectedType(SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_ATTRVALVAR_BEGIN: // ={...}         esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    //addS(rets, sb);
                    //rets.add(new EscapeXAttrAVM2Item(null, null,   ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    sb.append("\"");
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_VAR_BEGIN: // {...}                esc_xelem
                    usesVars.setVal(true);
                    //addS(rets, sb);
                    //rets.add(new EscapeXElemAVM2Item(null, null, ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_FINISHVARTAG_BEGIN: // </{...}>    add
                    usesVars.setVal(true);
                    sb.append("</");
                    //addS(rets, sb);

                    //rets.add(;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.begin(ActionScriptLexer.XMLCLOSETAGFINISH);
                    s = lex();
                    while (s.isType(SymbolType.XML_TEXT)) {
                        sb.append(s.value);
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.GREATER_THAN);
                    sb.append(">");
                    //addS(rets, sb);

                    if (openedTags.isEmpty()) {
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline(), s.position);
                    }
                    openedTags.remove(openedTags.size() - 1);
                    break;
                case XML_STARTVARTAG_BEGIN: // <{...}>      add
                    //GraphTargetItem ex = ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    sub.add("*");
                    sb.append("<");
                    //addS(rets, sb);
                    //rets.add(ex);
                    //rets.addAll(;
                    xmltag(allOpenedNamespaces, thisType, pkg, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    break;
                case XML_STARTTAG_BEGIN:    // <xxx>
                    sub.add(s.value.toString().trim().substring(1)); //remove < from beginning
                    //List<GraphTargetItem> st = ;
                    xmltag(allOpenedNamespaces, thisType, pkg, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    sb.append(s.value.toString());
                    //addS(rets, sb);
                    //rets.addAll(st);
                    break;
                case XML_FINISHTAG:
                    String tname = s.value.toString().substring(2, s.value.toString().length() - 1).trim();
                    if (openedTags.isEmpty()) {
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline(), s.position);
                    }
                    String lastTName = openedTags.get(openedTags.size() - 1);
                    if (lastTName.equals(tname) || lastTName.equals("*")) {
                        openedTags.remove(openedTags.size() - 1);
                    } else {
                        throw new AVM2ParseException("XML : Closing unopened tag", lexer.yyline(), s.position);
                    }
                    sb.append(s.value.toString());
                    break;
                case XML_STARTFINISHTAG_END:
                    openedTags.remove(openedTags.size() - 1); //close last tag
                    sb.append(s.value.toString());
                    break;
                case EOF:
                    throw new AVM2ParseException("End of file before XML finish", lexer.yyline(), s.position);
                default:
                    sb.append(s.value.toString());
                    break;
            }
        } while (!openedTags.isEmpty());
        //addS(rets, sb);       
    }

    private boolean xml(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        List<String> openedTags = new ArrayList<>();
        //List<GraphTargetItem> xmlParts =;
        xmltag(allOpenedNamespaces, thisType, pkg, new Reference<>(false), openedTags, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
        lexer.setEnableWhiteSpace(true);
        lexer.begin(ActionScriptLexer.YYINITIAL);
        ParsedSymbol s = lexer.lex();
        while (s.isType(SymbolType.XML_WHITESPACE)) {
            //addS(xmlParts, new StringBuilder(s.value.toString()));
            s = lexer.lex();
        }
        lexer.setEnableWhiteSpace(false);
        lexer.pushback(s);
        //GraphTargetItem ret = add(xmlParts);
        //ret = new XMLAVM2Item(ret);
        //lexer.yybegin(ActionScriptLexer.YYINITIAL);
        //TODO: Order of additions as in official compiler
        return true;
    }

    private boolean command(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        LexBufferer buf = new LexBufferer();
        lexer.addListener(buf);
        boolean ret = false;
        if (debugMode) {
            System.out.println("command:");
        }
        ParsedSymbol s = lex();
        if (s.type == SymbolType.EOF) {
            return false;
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
                //GraphTargetItem ns = ;
                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                //ret = new DefaultXMLNamespace(null, null, ns);
                ret = true;
                //TODO: use dxns for attribute namespaces instead of dxnslate
            }
        }
        if (!ret) {
            switch (s.type) {
                case USE:
                    expectedType(SymbolType.NAMESPACE);
                    //GraphTargetItem ns = ;
                    type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    //openedNamespaces.add(new NamespaceItem(ns.toString(), Namespace.KIND_PACKAGE /*FIXME?*/));
                    break;
                case WITH:
                    needsActivation.setVal(true);
                    expectedType(SymbolType.PARENT_OPEN);
                    //GraphTargetItem wvar = ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    /*if (!isNameOrProp(wvar)) {
                        throw new AVM2ParseException("Not a property or name", lexer.yyline());
                    }*/
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    List<VariableOrScope> withVars = new ArrayList<>();
                    //List<GraphTargetItem> wcmd = ;
                    commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, withVars, abc);
                    variables.addAll(withVars);
                    /*for (AssignableAVM2Item a : withVars) {
                        if (a instanceof UnresolvedAVM2Item) {
                            UnresolvedAVM2Item ua = (UnresolvedAVM2Item) a;
                            ua.scopeStack.add(0, wvar);
                        }
                    }*/
                    expectedType(SymbolType.CURLY_CLOSE);
                    //ret = new WithAVM2Item(null, null, wvar, wcmd);
                    //((WithAVM2Item) ret).subvariables = withVars;
                    //FIXME!!!
                    ret = true;
                    break;                
                case FUNCTION:
                    s = lexer.lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    needsActivation.setVal(true);
                    //ret = (;
                    function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, s.value.toString(), false, variables, abc, s.position);
                    ret = true;
                    break;
                case VAR:
                case CONST:
                    boolean isConst = s.type == SymbolType.CONST;
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String varIdentifier = s.value.toString();
                    int varPos = s.position;
                    s = lex();
                    //GraphTargetItem type;
                    if (s.type == SymbolType.COLON) {
                        //type = 
                        type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        s = lex();
                    } else {
                        //type = TypeItem.UNBOUNDED;
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        //GraphTargetItem varval = (;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        //NameAVM2Item vret = new NameAVM2Item(type, lexer.yyline(), false, varIdentifier, "", varval, true, openedNamespaces, abcIndex, isConst);                       
                        //variables.add((NameAVM2Item) vret);
                        variables.add(new Variable(true, varIdentifier, varPos));
                        ret = true;
                    } else {
                        //NameAVM2Item vret = new NameAVM2Item(type, lexer.yyline(), false, varIdentifier, "", null, true, openedNamespaces, abcIndex, isConst);
                        //variables.add((NameAVM2Item) vret);
                        variables.add(new Variable(true, varIdentifier, varPos));
                        lexer.pushback(s);
                        ret = true;
                    }
                    break;
                case CURLY_OPEN:
                    //ret = new BlockItem(DIALECT, null, null, ;
                    commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables, abc);
                    expectedType(SymbolType.CURLY_CLOSE);
                    ret = true;
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
                        //List<GraphTargetItem> args = ;
                        call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                        ret = true;
                        //ret = new ConstructSuperAVM2Item(null, null, new LocalRegAVM2Item(null, null, 0, null, new TypeItem("Object") /*?*/), args);
                    } else { //no constructor call, but it could be calling parent methods... => handle in expression
                        lexer.pushback(ss2);
                        lexer.pushback(s);
                    }
                    break;
                case IF:
                    expectedType(SymbolType.PARENT_OPEN);
                    //GraphTargetItem ifExpr = (;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    //GraphTargetItem onTrue = ;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    /*List<GraphTargetItem> onTrueList = new ArrayList<>();
                    onTrueList.add(onTrue);*/
                    s = lex();
                    //List<GraphTargetItem> onFalseList = null;
                    if (s.type == SymbolType.ELSE) {
                        //onFalseList = new ArrayList<>();
                        //onFalseList.add(
                        command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    } else {
                        lexer.pushback(s);
                    }
                    //ret = new IfItem(DIALECT, null, null, ifExpr, onTrueList, onFalseList);
                    ret = true;
                    break;
                case WHILE:
                    expectedType(SymbolType.PARENT_OPEN);
                    //List<GraphTargetItem> whileExpr = new ArrayList<>();
                    //whileExpr.add(;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    //List<GraphTargetItem> whileBody = new ArrayList<>();
                    Loop wloop = new Loop(uniqId(), null, null);
                    if (loopLabel != null) {
                        loopLabels.put(wloop, loopLabel);
                    }
                    loops.push(wloop);
                    //whileBody.add(;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    //ret = new WhileItem(DIALECT, null, null, wloop, whileExpr, whileBody);
                    loops.pop();
                    ret = true;
                    break;
                case DO:
                    //List<GraphTargetItem> doBody = new ArrayList<>();
                    Loop dloop = new Loop(uniqId(), null, null);
                    loops.push(dloop);
                    if (loopLabel != null) {
                        loopLabels.put(dloop, loopLabel);
                    }
                    //doBody.add(;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    
                    expectedType(SymbolType.WHILE);
                    expectedType(SymbolType.PARENT_OPEN);
                    //List<GraphTargetItem> doExpr = new ArrayList<>();
                    //doExpr.add(;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    //ret = new DoWhileItem(DIALECT, null, null, dloop, doBody, doExpr);
                    loops.pop();
                    ret = true;
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
                    //GraphTargetItem firstCommand = ;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, false, variables, abc);
                    /*if (firstCommand instanceof NameAVM2Item) {
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
*/
                    Loop floop = new Loop(uniqId(), null, null);
                    loops.push(floop);
                    if (loopLabel != null) {
                        loopLabels.put(floop, loopLabel);
                    }
                    //List<GraphTargetItem> forFinalCommands = new ArrayList<>();
                    //tem forExpr = null;
                    //List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                    if (!forin) {
                        s = lex();
                        /*if (firstCommand != null) { //can be empty command
                            forFirstCommands.add(firstCommand);
                        }
                        */
                        if (!s.isType(SymbolType.PARENT_CLOSE)) {
                            lexer.pushback(s);
                            //GraphTargetItem firstCommand = command(thisType,pkg,needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables);
                            //forExpr =;
                            expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                            /*if (forExpr == null) {
                                forExpr = new TrueItem(DIALECT, null, null);
                            }*/
                            expectedType(SymbolType.SEMICOLON);
                            //GraphTargetItem fcom = ;
                            command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                            /*if (fcom != null) {
                                forFinalCommands.add(fcom);
                            }*/
                        } else {
                            lexer.pushback(s);
                        }
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> forBody = new ArrayList<>();
                    //forBody.add(;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables, abc);
                    /*if (forin) {
                        if (each) {
                            ret = new ForEachInAVM2Item(null, null, floop, inexpr, forBody);
                        } else {

                            ret = new ForInAVM2Item(null, null, floop, inexpr, forBody);
                        }
                    } else {
                        ret = new ForItem(DIALECT, null, null, floop, forFirstCommands, forExpr, forFinalCommands, forBody);
                    }*/                    
                    loops.pop();
                    ret = true;
                    break;
                case SWITCH:
                    Loop sloop = new Loop(-uniqId(), null, null); //negative id marks switch = no continue
                    loops.push(sloop);
                    if (loopLabel != null) {
                        loopLabels.put(sloop, loopLabel);
                    }
                    expectedType(SymbolType.PARENT_OPEN);
                    //GraphTargetItem switchExpr = ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.CURLY_OPEN);
                    s = lex();
                    /*int exprReg = 0;
                    for (int i = 0; i < 256; i++) {
                        if (!registerVars.containsValue(i)) {
                            registerVars.put("__switch" + uniqId(), i);
                            exprReg = i;
                            break;
                        }
                    }*/
                    //List<List<ActionIf>> caseIfs = new ArrayList<>();
                    //List<List<GraphTargetItem>> caseCmds = new ArrayList<>();
                    //List<GraphTargetItem> caseExprsAll = new ArrayList<>();
                    //List<Integer> valueMapping = new ArrayList<>();
                    //int pos = 0;
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                            if (s.type != SymbolType.DEFAULT) {
                                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
                            }
                            expectedType(SymbolType.COLON);
                            s = lex();
                            //caseExprsAll.add(curCaseExpr);
                            //valueMapping.add(pos);
                        }
                        //pos++;
                        lexer.pushback(s);
                        //List<GraphTargetItem> caseCmd = ;
                        commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, variables, abc);
                        //caseCmds.add(caseCmd);
                        s = lex();
                    }
                    expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    //ret = new SwitchItem(DIALECT, null, null, sloop, switchExpr, caseExprsAll, caseCmds, valueMapping);
                    ret = true;
                    loops.pop();
                    break;
                case BREAK:
                    s = lex();
                    long bloopId = 0;
                    if (loops.isEmpty()) {
                        throw new AVM2ParseException("No loop to break", lexer.yyline(), s.position);
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
                            throw new AVM2ParseException("Identifier of loop expected", lexer.yyline(), s.position);
                        }
                    } else {
                        lexer.pushback(s);
                        bloopId = loops.peek().id;
                    }
                    //ret = new BreakItem(DIALECT, null, null, bloopId);
                    ret = true;
                    break;
                case CONTINUE:
                    s = lex();
                    long cloopId = 0;
                    if (loops.isEmpty()) {
                        throw new AVM2ParseException("No loop to continue", lexer.yyline(), s.position);
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
                            throw new AVM2ParseException("Identifier of loop expected", lexer.yyline(), s.position);
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
                            throw new AVM2ParseException("No loop to continue", lexer.yyline(), s.position);
                        }
                    }
                    //TODO: handle switch (???)
                    //ret = new ContinueItem(DIALECT, null, null, cloopId);
                    ret = true;
                    break;
                case RETURN:
                    //GraphTargetItem retexpr = ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, true, variables, false, abc);
                    /*if (retexpr == null) {
                        ret = new ReturnVoidAVM2Item(null, null);
                    } else {
                        ret = new ReturnValueAVM2Item(null, null, retexpr);
                    }*/
                    ret = true;
                    break;
                case TRY:
                    needsActivation.setVal(true);
                    //List<GraphTargetItem> tryCommands = new ArrayList<>();
                    //tryCommands.add(;
                    command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                    s = lex();
                    boolean found = false;
                    //List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
                    //List<NameAVM2Item> catchExceptions = new ArrayList<>();
                    int varCnt = variables.size();
                    //List<List<AssignableAVM2Item>> catchesVars = new ArrayList<>();
                    while (s.type == SymbolType.CATCH) {
                        expectedType(SymbolType.PARENT_OPEN);
                        s = lex();
                        expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);

                        String enamestr = s.value.toString();
                        int ePos = s.position;
                        expectedType(SymbolType.COLON);
                        //GraphTargetItem etype = ;
                        type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        //NameAVM2Item e = new NameAVM2Item(etype, lexer.yyline(), false, enamestr, "", new ExceptionAVM2Item(null)/*?*/, true/*?*/, openedNamespaces, abcIndex, false);
                        //variables.add(e);
                        //catchExceptions.add(e);
                        //e.setSlotNumber(1);
                        //e.setSlotScope(Integer.MAX_VALUE); //will be changed later
                        expectedType(SymbolType.PARENT_CLOSE);
                        List<VariableOrScope> catchVars = new ArrayList<>();
                        expectedType(SymbolType.CURLY_OPEN);
                        //List<GraphTargetItem> cc = ;
                        commands(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, catchVars, abc);
                        expectedType(SymbolType.CURLY_CLOSE);
                        variables.add(new CatchScope(new Variable(true, enamestr, ePos), catchVars));
                        //catchesVars.add(catchVars);
                        //List<AssignableAVM2Item> newVariables = new ArrayList<>(catchVars);

                        /*for (int i = 0; i < newVariables.size(); i++) {
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
                        variables.addAll(newVariables);*/

                        //catchCommands.add(cc);
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

                    //List<GraphTargetItem> finallyCommands = null;
                    if (s.type == SymbolType.FINALLY) {
                        //finallyCommands = new ArrayList<>();
                        //finallyCommands.add(;
                        command(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, forinlevel, true, variables, abc);
                        found = true;
                        s = lex();
                    }
                    if (!found) {
                        expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                    }
                    lexer.pushback(s);
                    /*TryAVM2Item tai = new TryAVM2Item(tryCommands, null, catchCommands, finallyCommands, "");
                    tai.catchVariables = catchesVars;
                    tai.catchExceptions2 = catchExceptions;
                    ret = tai;*/
                    ret = true;
                    break;
                case THROW:
                    //ret = new ThrowAVM2Item(null, null, ;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    ret = true;
                    break;
                default:
                    //GraphTargetItem valcmd = ;
                    boolean valcmd = expressionCommands(s, registerVars, inFunction, inMethod, forinlevel, variables);
                    if (valcmd) {
                        ret = valcmd;
                        break;
                    }
                    if (s.type == SymbolType.SEMICOLON) {
                        //return new EmptyCommand(DIALECT);
                        return true;
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
        if (!ret) {  //can be popped expression
            buf.pushAllBack(lexer);
            ret = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    /*private GraphTargetItem expressionRemainder(TypeItem thisType, String pkg, Reference<Boolean> needsActivation, List<NamespaceItem> openedNamespaces, GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableOrScope> variables, List<DottedChain> importedClasses) throws IOException, AVM2ParseException, SimpleParseException {
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

    private void brackets(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                //ret.add(;
                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                s = lex();
                if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                    expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                }
            }
        } else {
            lexer.pushback(s);
        }
    }

    private boolean expression(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        return expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, allowRemainder, variables, allowComma, abc);
    }

    private boolean expression(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

        //List<GraphTargetItem> commaItems = new ArrayList<>();
        ParsedSymbol symb;
        do {
            boolean prim = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
            if (!prim) {
                return false;
            }
            //GraphTargetItem item = ;
            expression1(allOpenedNamespaces, prim, GraphTargetItem.NOPRECEDENCE, thisType, pkg, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, allowRemainder, variables, abc);
            //commaItems.add(item);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
        /*if (commaItems.size() == 1) {
            return commaItems.get(0);
        }
        return new CommaExpressionItem(DIALECT, null, null, commaItems);*/
        return true;
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

    private ParsedSymbol peekExprToken() throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol lookahead = lex();
        xmlToLowerThanFix(lookahead);
        regexpToDivideFix(lookahead);

        lexer.pushback(lookahead);
        return lookahead;
    }

    private boolean expression1(List<List<NamespaceItem>> allOpenedNamespaces, boolean lhs, int min_precedence, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expression1:");
        }
        ParsedSymbol lookahead = peekExprToken();

        ParsedSymbol op;
        boolean rhs;
        boolean mhs = false;

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
            if (!rhs) {
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

                    //lhs = new AsTypeAVM2Item(null, null, lhs, rhs); //???
                    lhs = true;
                    allowRemainder = false;
                    break;

                case IN:
                    //lhs = new InAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;

                case TERNAR: //???
                    //lhs = new TernarOpItem(DIALECT, null, null, lhs, mhs, rhs);
                    lhs = true;
                    break;
                case SHIFT_LEFT:
                    //lhs = new LShiftAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case SHIFT_RIGHT:
                    //lhs = new RShiftAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case USHIFT_RIGHT:
                    //lhs = new URShiftAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case BITAND:
                    //lhs = new BitAndAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case BITOR:
                    //lhs = new BitOrAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case DIVIDE:
                    //lhs = new DivideAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MODULO:
                    //lhs = new ModuloAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case EQUALS:
                    //lhs = new EqAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case STRICT_EQUALS:
                    //lhs = new StrictEqAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case NOT_EQUAL:
                    //lhs = new NeqAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case STRICT_NOT_EQUAL:
                    //lhs = new StrictNeqAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case LOWER_THAN:
                    //lhs = new LtAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case LOWER_EQUAL:
                    //lhs = new LeAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case GREATER_THAN:
                    //lhs = new GtAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case GREATER_EQUAL:
                    //lhs = new GeAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case AND:
                    //lhs = new AndItem(DIALECT, null, null, lhs, rhs);
                    lhs = true;
                    break;
                case OR:
                    //lhs = new OrItem(DIALECT, null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MINUS:
                    //lhs = new SubtractAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MULTIPLY:
                    //lhs = new MultiplyAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case PLUS:
                    //lhs = new AddAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case XOR:
                    //lhs = new BitXorAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case INSTANCEOF:
                    //lhs = new InstanceOfAVM2Item(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case IS:
                    //GraphTargetItem istype = rhs;                    
                    //lhs = new IsTypeAVM2Item(null, null, lhs, istype);
                    lhs = true;
                    break;
                case NULL_COALESCE:
                    //lhs = new NullCoalesceAVM2Item(null, null, lhs, rhs);
                    lhs = true;
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
                    /*GraphTargetItem assigned = rhs;
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
                            assigned = new AndItem(DIALECT, null, null, lhs, assigned);
                            break;
                        case ASSIGN_OR:
                            assigned = new OrItem(DIALECT, null, null, lhs, assigned);
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
                    lhs = as;*/
                    lhs = true;
                    break;
            }
        }
        /*if (lhs instanceof ParenthesisItem) {
            GraphTargetItem coerced = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);
            if (coerced != null && isType(((ParenthesisItem) lhs).value)) {
                lhs = new CoerceAVM2Item(null, null, ((ParenthesisItem) lhs).value, coerced);
            }
        }*/

        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private boolean expressionPrimary(List<List<NamespaceItem>> allOpenedNamespaces, TypeItem thisType, NamespaceItem pkg, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean ret = false;
        ParsedSymbol s = lex();
        boolean allowMemberOrCall = false;
        switch (s.type) {
            case PREPROCESSOR:
                expectedType(SymbolType.PARENT_OPEN);
                switch ("" + s.value) {
                    //AS3
                    case "hasnext":
                        //GraphTargetItem hnIndex = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        //GraphTargetItem hnObject = 
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        //ret = new HasNextAVM2Item(null, null, hnIndex, hnObject);
                        ret = true;
                        break;
                    case "newactivation":
                        //ret = new NewActivationAVM2Item(null, null);
                        ret = true;
                        break;
                    case "nextname":
                        //GraphTargetItem nnIndex = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        //GraphTargetItem nnObject = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);

                        //ret = new NextNameAVM2Item(null, null, nnIndex, nnObject);
                        ret = true;
                        allowMemberOrCall = true;
                        break;
                    case "nextvalue":
                        //GraphTargetItem nvIndex = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        expectedType(SymbolType.COMMA);
                        //GraphTargetItem nvObject = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);

                        //ret = new NextNameAVM2Item(null, null, nvIndex, nvObject);
                        ret = true;
                        allowMemberOrCall = true;
                        break;
                    //Both ASs
                    case "dup":
                        //ret = new DuplicateItem(DIALECT, null, null, ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        ret = true;
                        break;
                    case "push":
                        //ret = new PushItem(
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                        ret = true;
                        break;
                    case "pop":
                        //ret = new PopItem(DIALECT, null, null);
                        ret = true;
                        break;
                    case "goto":
                    case "multiname":
                        throw new AVM2ParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline(), s.position);
                    default:
                        throw new AVM2ParseException("Unknown preprocessor instruction: §§" + s.value, lexer.yyline(), s.position);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case REGEXP:
                //String p = (String) s.value;
                //p = p.substring(1);
                //int spos = p.lastIndexOf('/');
                //String mod = p.substring(spos + 1);
                //p = p.substring(0, spos);
                //ret = new RegExpAvm2Item(p, mod, null, null);
                allowMemberOrCall = true;
                ret = true;
                break;
            case XML_STARTTAG_BEGIN:
            case XML_STARTVARTAG_BEGIN:
            case XML_CDATA:
            case XML_COMMENT:
                lexer.pushback(s);
                ret = xml(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                break;
            case STRING:
                //ret = new StringAVM2Item(null, null, s.value.toString());
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEGATE:
                ret = expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc);
                //ret = new BitNotAVM2Item(null, null, ret);
                ret = true;

                break;
            case PLUS:
                //GraphTargetItem nump = ;
                expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);
                /*if (abc.hasFloatSupport()) {
                    ret = new UnPlusAVM2Item(null, null, nump);
                } else {
                    ret = new CoerceAVM2Item(null, null, nump, TypeItem.NUMBER);
                }*/
                ret = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    //ret = new DoubleValueAVM2Item(null, null, -(Double) s.value);
                    ret = true;
                } else if (s.isType(SymbolType.INTEGER)) {
                    //ret = new IntegerValueAVM2Item(null, null, -(Integer) s.value);
                    ret = true;
                } else if (s.isType(SymbolType.DECIMAL)) {
                    //ret = new DecimalValueAVM2Item(null, null, ((Decimal128) s.value).multiply(Decimal128.NEG1));
                    ret = true;
                } else if (s.isType(SymbolType.FLOAT)) {
                    //ret = new FloatValueAVM2Item(null, null, -(Float) s.value);
                    ret = true;
                } else {
                    lexer.pushback(s);
                    //GraphTargetItem num = ;
                    expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);
                    /*if (num instanceof IntegerValueAVM2Item) {
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
                    }*/
                    ret = true;
                }
                break;
            case TYPEOF:
                //ret = new TypeOfAVM2Item(null, null, ;
                expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc);
                ret = true;
                break;
            case TRUE:
                //ret = new BooleanAVM2Item(null, null, true);
                ret = true;
                break;
            case NULL:
                //ret = new NullAVM2Item(null, null);
                ret = true;
                break;
            case UNDEFINED:
                //ret = new UndefinedAVM2Item(null, null);
                ret = true;
                break;
            case FALSE:
                //ret = new BooleanAVM2Item(null, null, false);
                ret = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                //List<NameValuePair> nvs = new ArrayList<>();

                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.STRING, SymbolType.INTEGER, SymbolType.DOUBLE, SymbolType.PARENT_OPEN);

                    //GraphTargetItem n;
                    if (s.type == SymbolType.PARENT_OPEN) { //special for obfuscated SWFs
                        //n = ;
                        expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);
                        expectedType(SymbolType.PARENT_CLOSE);
                    } else {
                        //n = new StringAVM2Item(null, null, s.value.toString());
                    }
                    expectedType(SymbolType.COLON);
                    //GraphTargetItem v =;
                    expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, allowRemainder, variables, false, abc);

                    //NameValuePair nv = new NameValuePair(n, v);
                    //nvs.add(nv);
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                //ret = new NewObjectAVM2Item(null, null, nvs);
                ret = true;
                allowMemberOrCall = true;                
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                //List<GraphTargetItem> inBrackets = new ArrayList<>();
                //int arrCnt = ;
                brackets(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                //ret = new NewArrayAVM2Item(null, null, inBrackets);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                int fnamePos = -1;
                if (s.isType(SymbolGroup.IDENTIFIER)) {
                    fname = s.value.toString();
                    fnamePos = s.position;
                } else {
                    lexer.pushback(s);
                }
                needsActivation.setVal(true);
                //ret = ;
                function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, fname, false, variables, abc, fnamePos);
                ret = true;
                allowMemberOrCall = true;
                break;
            case NAN:
                //ret = new NanAVM2Item(null, null);
                ret = true;
                break;
            case INFINITY:
                //ret = new DoubleValueAVM2Item(null, null, Double.POSITIVE_INFINITY)
                ret = true;
                break;
            case INTEGER:
                //ret = new IntegerValueAVM2Item(null, null, (Integer) s.value);
                ret = true;
                break;
            case DOUBLE:
                //ret = new DoubleValueAVM2Item(null, null, (Double) s.value);
                ret = true;
                allowMemberOrCall = true; // 5.2.toString();
                break;
            case DECIMAL:
                if (!abc.hasDecimalSupport()) {
                    throw new AVM2ParseException("The ABC has no decimal support", lexer.yyline(), s.position);
                }
                //ret = new DecimalValueAVM2Item(null, null, (Decimal128) s.value);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FLOAT:
                if (!abc.hasFloatSupport()) {
                    throw new AVM2ParseException("The ABC has no float support", lexer.yyline(), s.position);
                }
                //ret = new FloatValueAVM2Item(null, null, (Float) s.value);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FLOAT4:
                if (!abc.hasFloat4Support()) {
                    //parse again as method call
                    lexer.yypushbackstr(lexer.yytext().substring("float4".length()));
                    lexer.pushback(new ParsedSymbol(lexer.yychar() /*???*/, SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, "float4"));
                    ret = name(allOpenedNamespaces, thisType, pkg, needsActivation, false, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses, abc);
                } else {
                    ret = true; //new Float4ValueAVM2Item(null, null, (Float4) s.value);
                }
                allowMemberOrCall = true;
                break;
            case DELETE:
                //GraphTargetItem varDel = ;
                expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, true, variables, abc);
                /*if (!isNameOrProp(varDel)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }*/
                //ret = new DeletePropertyAVM2Item(varDel, lexer.yyline());
                ret = true;
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                //GraphTargetItem varincdec = ;
                expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false/*?*/, variables, abc);
                /*if (!isNameOrProp(varincdec)) {
                    throw new AVM2ParseException("Not a property or name", lexer.yyline());
                }*/
                /*if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementAVM2Item(null, null, varincdec);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementAVM2Item(null, null, varincdec);
                }*/
                ret = true;
                break;
            case NOT:
                //ret = new NotItem(DIALECT, null, null, ;
                expressionPrimary(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, false, variables, abc);
                ret = true;
                break;
            case PARENT_OPEN:
                //ret = new ParenthesisItem(DIALECT, null, null, ;
                expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, true, abc);
                expectedType(SymbolType.PARENT_CLOSE);
                /*if (ret.value == null) {
                    throw new AVM2ParseException("Expression in parenthesis expected", lexer.yyline());
                }*/
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEW:
                s = lex();
                if (s.type == SymbolType.XML_STARTTAG_BEGIN) {
                    lexer.yypushbackstr(s.value.toString().substring(1), ActionScriptLexer.YYINITIAL);
                    s = new ParsedSymbol(s.position, SymbolGroup.OPERATOR, SymbolType.LOWER_THAN);
                }
                if (s.type == SymbolType.FUNCTION) {
                    s = lexer.lex();
                    String ffname = "";
                    int ffnamePos = -1;
                    if (s.isType(SymbolGroup.IDENTIFIER)) {
                        ffname = s.value.toString();
                        ffnamePos = s.position;
                    } else {
                        lexer.pushback(s);
                    }
                    needsActivation.setVal(true);
                    function(allOpenedNamespaces, new ArrayList<>(), pkg, false, false, needsActivation, importedClasses, thisType, openedNamespaces, ffname, false, variables, abc, ffnamePos);
                    ret = true;
                } else if (s.type == SymbolType.LOWER_THAN) {
                    //GraphTargetItem subtype = ;
                    type(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    expectedType(SymbolType.GREATER_THAN);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.BRACKET_OPEN);
                    lexer.pushback(s);
                    List<GraphTargetItem> params = new ArrayList<>();
                    brackets(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    //ret = new InitVectorAVM2Item(subtype, params, openedNamespaces);
                    ret = true;
                } else if (s.type == SymbolType.PARENT_OPEN) {
                    //GraphTargetItem newvar = ;
                    boolean newvar = expression(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, true, variables, false, abc);
                    //newvar = ;
                    applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables, abc);
                    expectedType(SymbolType.PARENT_CLOSE);
                    expectedType(SymbolType.PARENT_OPEN);
                    //ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, 
                    call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    //, abcIndex);
                    ret = true;

                } else {
                    lexer.pushback(s);
                    boolean newvar = name(allOpenedNamespaces, thisType, pkg, needsActivation, false /*?*/, openedNamespaces, registerVars, inFunction, inMethod, variables, importedClasses, abc);
                    applyType(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, variables, abc);
                    expectedType(SymbolType.PARENT_OPEN);
                    //ret = new ConstructSomethingAVM2Item(lexer.yyline(), openedNamespaces, newvar, 
                    
                    call(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, variables, abc);
                    //, abcIndex);
                    ret = true;
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
                boolean excmd = expressionCommands(s, registerVars, inFunction, inMethod, -1, variables);
                if (excmd) {
                    //?
                    ret = excmd;
                    allowMemberOrCall = true; //?
                    break;
                }
                lexer.pushback(s);
        }
        if (allowMemberOrCall && ret) {
            ret = memberOrCall(allOpenedNamespaces, thisType, pkg, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, variables, abc);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private ActionScriptLexer lexer = null;

    private List<String> constantPool;

    private boolean parseImportsUsages(List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Reference<Integer> numberUsageRef, Reference<Integer> numberPrecisionRef, Reference<Integer> numberRoundingRef, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

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
                    //Note: in this case, fullName attribute will be changed to real NS including NamespaceItem
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
                            throw new AVM2ParseException("Invalid use kind", lexer.yyline(), s.position);
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
                                        throw new AVM2ParseException("Rounding expected - one of: CEILING, UP, HALF_UP, HALF_EVEN, HALF_DOWN, DOWN, FLOOR", lexer.yyline(), s.position);
                                }
                                numberRoundingRef.setVal(rounding);
                                break;
                            case "precision":
                                s = lex();
                                expected(s, lexer.yyline(), SymbolType.INTEGER);
                                int precision = (Integer) s.value;
                                if (precision < 1 || precision > 34) {
                                    throw new AVM2ParseException("Invalid precision - must be between 1 and 34", lexer.yyline(), s.position);
                                }
                                numberPrecisionRef.setVal(precision);
                                break;
                            default:
                                throw new AVM2ParseException("Invalid use kind", lexer.yyline(), s.position);
                        }
                    }
                    s = lex();
                } while (s.isType(SymbolType.COMMA));
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

    private void parseScript(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            Reference<Integer> numberContextRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables
    ) throws IOException, AVM2ParseException, SimpleParseException, CompilationException, InterruptedException {
        
        Reference<Integer> numberUsageRef = new Reference<>(NumberContext.USE_NUMBER);
        Reference<Integer> numberRoundingRef = new Reference<>(NumberContext.ROUND_HALF_EVEN);
        Reference<Integer> numberPrecisionRef = new Reference<>(34);
        scriptTraits(importedClasses, openedNamespaces, allOpenedNamespaces, numberUsageRef, numberRoundingRef, numberPrecisionRef, abc, sinitNeedsActivation, sinitVariables);

        NumberContext nc = new NumberContext(numberUsageRef.getVal(), numberPrecisionRef.getVal(), numberRoundingRef.getVal());
        if (!nc.isDefault()) {
            numberContextRef.setVal(nc.toParam());
        }
    }

    /**
     * Converts string to script traits.
     *
     * @param importedClasses Imported classes
     * @param openedNamespaces Opened namespaces
     * @param allOpenedNamespaces All opened namespaces
     * @param str String to parse
     * @param numberContextRef Number context reference
     * @param abc ABC
     * @param sinitNeedsActivation Script initializer needs activation
     * @param sinitVariables Script initializer variables
     * @throws AVM2ParseException On parsing error
     * @throws IOException On I/O error
     * @throws CompilationException On compilation error
     * @throws InterruptedException On interrupt
     */
    private void scriptTraitsFromString(
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            String str,
            Reference<Integer> numberContextRef,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables
    ) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {
        lexer = new ActionScriptLexer(str);

        parseScript(importedClasses, openedNamespaces, allOpenedNamespaces, numberContextRef, abc, sinitNeedsActivation, sinitVariables);
        ParsedSymbol s = lexer.lex();
        if (s.type != SymbolType.EOF) {
            throw new AVM2ParseException("Parsing finished before end of the file", lexer.yyline(), s.position);
        }        
    }


    @Override
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors
    ) throws SimpleParseException, IOException, InterruptedException {
        List<List<NamespaceItem>> allOpenedNamespaces = new ArrayList<>();
        Reference<Integer> numberContextRef = new Reference<>(null);
        Reference<Boolean> sinitNeedsActivation = new Reference<>(false);
        List<VariableOrScope> vars = new ArrayList<>();
        List<DottedChain> importedClasses = new ArrayList<>();
        List<NamespaceItem> openedNamespaces = new ArrayList<>();
        try {
            scriptTraitsFromString(importedClasses, openedNamespaces, allOpenedNamespaces, str, numberContextRef, abc, sinitNeedsActivation, vars);
        } catch (AVM2ParseException ex) {
            //Logger.getLogger(ActionScript3SimpleParser.class.getName()).log(Level.SEVERE, null, ex);
            throw new SimpleParseException(str, ex.line, ex.position);
        } catch (CompilationException ex) {
            //Logger.getLogger(ActionScript3SimpleParser.class.getName()).log(Level.SEVERE, null, ex);
            throw new SimpleParseException(str, ex.line);
        }
        Map<String, Integer> varNameToDefinitionPosition = new LinkedHashMap<>();

        parseVariablesList(new ArrayList<>(), vars, definitionPosToReferences, referenceToDefinition, varNameToDefinitionPosition);        
    }

    

    private void parseVariablesList(
            List<VariableOrScope> privateVariables,
            List<VariableOrScope> sharedVariables,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            Map<String, Integer> parentVarNameToDefinitionPosition
    ) {
        Map<String, Integer> privateVarNameToDefinitionPosition = new LinkedHashMap<>();
        privateVarNameToDefinitionPosition.putAll(parentVarNameToDefinitionPosition);

        for (VariableOrScope vt : privateVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    privateVarNameToDefinitionPosition.put(v.name, v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                } else {
                    if (!privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarNameToDefinitionPosition);
            }
        }
        for (VariableOrScope vt : sharedVariables) {
            if (vt instanceof Variable) {
                Variable v = (Variable) vt;
                if (v.definition) {
                    parentVarNameToDefinitionPosition.put(v.name, v.position);
                    privateVarNameToDefinitionPosition.put(v.name, v.position);
                    definitionPosToReferences.put(v.position, new ArrayList<>());
                } else {
                    if (!privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        parentVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        privateVarNameToDefinitionPosition.put(v.name, -v.position - 1);
                        definitionPosToReferences.put(-v.position - 1, new ArrayList<>());
                        definitionPosToReferences.get(-v.position - 1).add(v.position);
                        referenceToDefinition.put(v.position, -v.position - 1);
                    } else {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarNameToDefinitionPosition);
            }
        }
    }
    /**
     * Constructor.
     * @param abc ABC
     */
    public ActionScript3SimpleParser(ABC abc) {
        this.abc = abc;
        
    }      
}
