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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.simpleparser.CatchScope;
import com.jpexs.decompiler.flash.simpleparser.ClassScope;
import com.jpexs.decompiler.flash.simpleparser.ClassTrait;
import com.jpexs.decompiler.flash.simpleparser.FunctionScope;
import com.jpexs.decompiler.flash.simpleparser.Import;
import com.jpexs.decompiler.flash.simpleparser.LinkHandler;
import com.jpexs.decompiler.flash.simpleparser.MethodScope;
import com.jpexs.decompiler.flash.simpleparser.Namespace;
import com.jpexs.decompiler.flash.simpleparser.Path;
import com.jpexs.decompiler.flash.simpleparser.Separator;
import com.jpexs.decompiler.flash.simpleparser.SimpleParseException;
import com.jpexs.decompiler.flash.simpleparser.SimpleParser;
import com.jpexs.decompiler.flash.simpleparser.TraitVarConstValueScope;
import com.jpexs.decompiler.flash.simpleparser.Type;
import com.jpexs.decompiler.flash.simpleparser.Variable;
import com.jpexs.decompiler.flash.simpleparser.VariableOrScope;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * ActionScript 3 parser.
 *
 * @author JPEXS
 */
public class ActionScript3SimpleParser implements SimpleParser {

    private long uniqLast = 0;

    private final boolean debugMode = false;

    private final ABC abc;

    private long uniqId() {
        uniqLast++;
        return uniqLast;
    }

    private void commands(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, int forinlevel, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("commands:");
        }
        while (command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc)) {
        }
        if (debugMode) {
            System.out.println("/commands");
        }
    }

    private Path type(Reference<Variable> subTypeRef, List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();
        subTypeRef.setVal(null);
        if (s.type == SymbolType.MULTIPLY) {
            variables.add(new Type(false, new Path("*"), s.position));
            return new Path("*");
        } else if (s.type == SymbolType.VOID) {
            variables.add(new Type(false, new Path("void"), s.position));
            return new Path("void");
        } else {
            lexer.pushback(s);
        }

        boolean t = true;
        List<VariableOrScope> nameVars = new ArrayList<>();
        name(errors, thisType, needsActivation, openedNamespaces, null, false, false, true, nameVars, importedClasses, abc);
        Path ret = new Path("*");
        Variable nameVar = null;
        if (!nameVars.isEmpty() && nameVars.get(nameVars.size() - 1) instanceof Variable) {
            nameVar = (Variable) nameVars.get(nameVars.size() - 1);
            ret = nameVar.name;
        }

        List<VariableOrScope> applyVars = new ArrayList<>();
        applyTypeType(errors, thisType, needsActivation, importedClasses, openedNamespaces, t, new HashMap<>(), false, false, true, applyVars, abc);
        if (nameVar != null && !applyVars.isEmpty() && applyVars.get(applyVars.size() - 1) instanceof Variable) {
            subTypeRef.setVal((Variable) applyVars.get(applyVars.size() - 1));
            nameVar.subType = (Variable) applyVars.get(applyVars.size() - 1);
        }
        variables.addAll(applyVars);
        variables.addAll(nameVars);
        return ret;
    }

    private boolean applyTypeType(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        boolean ret = obj;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.TYPENAME) {
            boolean first = true;
            do {
                s = lex();
                if (s.isType(SymbolType.MULTIPLY)) {
                    //*
                } else {
                    lexer.pushback(s);
                    Reference<Variable> subTypeRef = new Reference<>(null);
                    type(subTypeRef, errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc);
                }
                first = false;
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
            expected(errors, s, lexer.yyline(), SymbolType.GREATER_THAN);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private Path memberOrCall(Path lastVarName, List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
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
                    lastVarName = member(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    ret = true;
                    break;
                case FILTER:
                    needsActivation.setVal(true);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, inMethod, variables, true, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    ret = true;
                    break;
                case PARENT_OPEN:
                    lastVarName = call(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    ret = true;
                    break;
                case DESCENDANTS:
                    s = lex();
                    expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY);
                    ret = true;
                    break;

            }
            s = lex();
        }
        if (s.type == SymbolType.INCREMENT) {
            if (!ret) {
                //!isNameOrProp(ret)                 
                errors.add(new SimpleParseException("Invalid assignment", lexer.yyline(), s.position));
            }
            ret = true;
            s = lex();

        } else if (s.type == SymbolType.DECREMENT) {
            if (!ret) {
                //(!isNameOrProp(ret)) {
                errors.add(new SimpleParseException("Invalid assignment", lexer.yyline(), s.position));
            }
            ret = true;
            s = lex();
        }

        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/memberOrCall");
        }
        return lastVarName;
    }

    private boolean applyType(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        boolean ret = obj;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.TYPENAME) {
            do {
                s = lex();
                if (s.isType(SymbolType.MULTIPLY)) {
                    //*
                } else {
                    lexer.pushback(s);
                    expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, false, variables, abc);
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
            expected(errors, s, lexer.yyline(), SymbolType.GREATER_THAN);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private Path member(Path lastVarName, List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("member:");
        }
        boolean ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT, SymbolType.NULL_DOT, SymbolType.BRACKET_OPEN, SymbolType.TYPENAME)) {
            ParsedSymbol s2 = lex();
            if (s.type == SymbolType.NULL_DOT) {
                variables.add(new Separator(lastVarName, s.position));
                lexer.pushback(s2);
            } else if (s.type == SymbolType.DOT) {
                variables.add(new Separator(lastVarName, s.position));
                if (s2.type == SymbolType.ATTRIBUTE) {
                    //attribute
                } else {
                    lexer.pushback(s2);
                }
            } else {
                lexer.pushback(s2);
            }
            if (s.type == SymbolType.TYPENAME) {
                lexer.pushback(s);
                applyType(errors, thisType, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, isStatic, variables, abc);
                ret = true;
                s = lex();
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                lastVarName = lastVarName.add(Path.PATH_BRACKETS);
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                s = lex();
                if (expected(errors, s, lexer.yyline(), SymbolType.BRACKET_CLOSE)) {
                    variables.add(new Separator(lastVarName, s.position));
                }
                ret = true;
                s = lex();
            } else {
                s = lex();
                if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.MULTIPLY)) {
                    break;
                }
                String propName = s.value.toString(); //Can be *
                int propPosition = s.position;
                s = lex();
                if (s.type == SymbolType.NAMESPACE_OP) {
                    variables.add(new Variable(false, new Path(propName), propPosition));
                    s = lex();
                    if (s.type == SymbolType.BRACKET_OPEN) {
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        expectedType(errors, SymbolType.BRACKET_CLOSE);
                    } else {
                        expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    }
                    lastVarName = lastVarName.add(propName + "::" + s.value.toString());
                    variables.add(new Variable(false, lastVarName, s.position, null));
                } else {
                    lastVarName = lastVarName.add(propName);
                    variables.add(new Variable(false, lastVarName, propPosition, null));
                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        //nsSuffix = "#" + s.value;
                    } else {
                        lexer.pushback(s);
                    }
                }
                ret = true;
                s = lex();
            }
        }
        lexer.pushback(s);

        if (debugMode) {
            System.out.println("/member");
        }
        return lastVarName;
    }

    private Path name(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, List<DottedChain> importedClasses, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();

        String lastName = "";
        if (s.type == SymbolType.ATTRIBUTE) {
            lastName = s.value.toString();
            s = lex();
        }
        if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP)) {
            return new Path();
        }
        lastName += s.value.toString();
        int identPos = s.position;
        s = lex();
        boolean attrBracket = false;
        if (s.type == SymbolType.NAMESPACESUFFIX) {
            s = lex();
            lastName += "#" + s.value;
        }

        Path fullName = new Path(lastName);

        while (s.isType(SymbolType.DOT)) {
            variables.add(new Variable(false, fullName, identPos));
            variables.add(new Separator(fullName, s.position));
            s = lex();
            lastName = "";
            if (s.type == SymbolType.ATTRIBUTE) {
                lastName += s.value.toString();
                s = lex();
                if (s.type == SymbolType.MULTIPLY) {
                    lastName += s.value.toString();
                    identPos = s.position;
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    lastName += s.value.toString();
                    identPos = s.position;
                } else {
                    if (s.type != SymbolType.BRACKET_OPEN) {
                        errors.add(new SimpleParseException("Attribute identifier or bracket expected", lexer.yyline(), s.position));
                    }
                    attrBracket = true;
                    continue;
                }
            } else {
                if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.NAMESPACE, SymbolType.MULTIPLY)) {
                    lexer.pushback(s);
                    return new Path();
                }
                lastName = s.value.toString();
                identPos = s.position;
            }
            s = lex();
            if (s.type == SymbolType.NAMESPACESUFFIX) {
                lastName += "#" + s.value;
                s = lex();
            }
            fullName = fullName.add(lastName);
        }
        if (s.type == SymbolType.NAMESPACE_OP) {
            variables.add(new Namespace(false, new Path(lastName), identPos));
            s = lex();
            if (s.group == SymbolGroup.IDENTIFIER) {
                String nsprop = s.value.toString();
                variables.add(new Variable(false, fullName.getParent().add(fullName.getLast().toString() + "::" + nsprop), s.position));
            } else if (s.type == SymbolType.BRACKET_OPEN) {
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                expectedType(errors, SymbolType.BRACKET_CLOSE);
            }
            s = lex();
        } else {
            variables.add(new Variable(false, fullName, identPos));
        }

        Path ret = fullName;
        if (s.type == SymbolType.BRACKET_OPEN) {
            lexer.pushback(s);
            if (attrBracket) {
                lexer.pushback(new ParsedSymbol(s.position - 1, SymbolGroup.OPERATOR, SymbolType.ATTRIBUTE, "@"));
                lexer.pushback(new ParsedSymbol(s.position - 2, SymbolGroup.OPERATOR, SymbolType.DOT, "."));
            }
            ret = member(fullName, errors, thisType, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, isStatic, variables, abc);
        } else {
            lexer.pushback(s);
        }
        return ret;
    }

    private boolean expected(List<SimpleParseException> errors, ParsedSymbol symb, int line, Object... expected) throws IOException, AVM2ParseException, SimpleParseException {
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
            errors.add(new SimpleParseException("" + expStr + " expected but " + symb.type + " found", line, symb.position));
        }
        return found;
    }

    private ParsedSymbol expectedType(List<SimpleParseException> errors, Object... type) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol symb = lex();
        expected(errors, symb, lexer.yyline(), type);
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

    private Path call(Path lastVarName, List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
            s = lex();
            if (!expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                break;
            }
        }
        if (s.type == SymbolType.PARENT_CLOSE) {
            lastVarName = lastVarName.add(Path.PATH_PARENTHESIS);
            variables.add(new Separator(lastVarName, s.position));
        }

        return lastVarName;
    }

    private void method(List<SimpleParseException> errors, boolean outsidePackage, boolean isPrivate, boolean isInterface, boolean isNative, String customAccess, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, boolean override, boolean isFinal, TypeItem thisType, List<NamespaceItem> openedNamespaces, boolean isStatic, Path functionName, boolean isMethod, List<VariableOrScope> variables, ABC abc, int methodNamePos, Reference<Path> returnTypeRef, Reference<Variable> returnSubTypeRef) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        function(errors, isInterface, isNative, needsActivation, importedClasses, thisType, openedNamespaces, functionName, isMethod, variables, abc, methodNamePos, isStatic, returnTypeRef, returnSubTypeRef);
    }

    private void function(List<SimpleParseException> errors, boolean isInterface, boolean isNative, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, TypeItem thisType, List<NamespaceItem> openedNamespaces, Path functionName, boolean isMethod, List<VariableOrScope> variables, ABC abc, int functionNamePos, boolean isStatic, Reference<Path> returnTypeRef, Reference<Variable> returnSubTypeRef) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

        ParsedSymbol s = lex();
        expected(errors, s, lexer.yyline(), SymbolType.PARENT_OPEN);
        int scopePos = s.position;
        s = lex();
        List<Path> paramNames = new ArrayList<>();
        List<Integer> paramPositions = new ArrayList<>();
        List<Path> paramTypes = new ArrayList<>();
        List<Variable> paramSubTypes = new ArrayList<>();

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
            if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                break;
            }

            paramNames.add(new Path(s.value.toString()));
            paramPositions.add(s.position);
            s = lex();
            if (!hasRest) {
                if (s.type == SymbolType.COLON) {
                    Reference<Variable> subTypeRef = new Reference<>(null);
                    paramTypes.add(type(subTypeRef, errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc));
                    paramSubTypes.add(subTypeRef.getVal());
                    s = lex();
                } else {
                    paramTypes.add(new Path("*"));
                    paramSubTypes.add(null);
                }
                if (s.type == SymbolType.ASSIGN) {
                    expression(errors, thisType, new Reference<>(false), importedClasses, openedNamespaces, null, isMethod, isMethod, isMethod, isStatic, variables, false, abc);
                    s = lex();
                }
                /*else if (!paramValues.isEmpty()) {
                    errors.add(new SimpleParseException("Some of parameters do not have default values", lexer.yyline()));
                }*/
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                if (!expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                    break;
                }
            }
            if (hasRest) {
                if (!expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE)) {
                    break;
                }
            }
        }
        s = lex();
        if (s.type == SymbolType.COLON) {
            returnTypeRef.setVal(type(returnSubTypeRef, errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc));
        } else {
            returnTypeRef.setVal(new Path("*"));
            returnSubTypeRef.setVal(null);
            lexer.pushback(s);
        }
        List<VariableOrScope> subvariables = new ArrayList<>();
        if (!isMethod && functionName != null && !functionName.isEmpty()) {
            subvariables.add(new Variable(true, new Path(functionName), functionNamePos));
        }
        for (int i = 0; i < paramNames.size() - (hasRest ? 1 : 0); i++) {
            subvariables.add(new Variable(true, paramNames.get(i), paramPositions.get(i), null, paramTypes.get(i), null, paramSubTypes.get(i), null));
        }
        if (hasRest) {
            subvariables.add(new Variable(true, paramNames.get(paramNames.size() - 1), paramPositions.get(paramNames.size() - 1), null, new Path("Array"), null));
        }
        Reference<Boolean> needsActivation2 = new Reference<>(false);

        if (!isInterface && !isNative) {
            s = lex();
            expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
            subvariables.add(new Variable(true, new Path("arguments"), -s.position - 1, null, new Path("Array"), null));
            commands(errors, thisType, needsActivation2, importedClasses, openedNamespaces, new Stack<>(), new HashMap<>(), new HashMap<>(), true, isMethod, isStatic, 0, subvariables, abc);
            s = lex();
            expected(errors, s, lexer.yyline(), SymbolType.CURLY_CLOSE);
        } else {
            s = lex();
            expected(errors, s, lexer.yyline(), SymbolType.SEMICOLON);
        }
        int scopeEndPos = s.position;

        if (isMethod) {

            MethodScope ms = new MethodScope(scopePos, scopeEndPos, subvariables, isStatic);
            variables.add(ms);
        } else {
            FunctionScope fs = new FunctionScope(scopePos, scopeEndPos, subvariables, isStatic);
            variables.add(fs);
        }
    }

    private void parseMetadata(List<SimpleParseException> errors) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.BRACKET_OPEN)) {
            s = lex();
            expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
            String name = s.value.toString();
            Map.Entry<String, Map<String, String>> en = new AbstractMap.SimpleEntry<>(name, new HashMap<>());
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
                        expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        String key = s.value.toString();
                        expectedType(errors, SymbolType.ASSIGN);
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolGroup.STRING);
                        String value = s.value.toString();
                        en.getValue().put(key, value);
                        s = lex();
                    } while (s.isType(SymbolType.COMMA));
                }
                expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                s = lex();
            }
            expected(errors, s, lexer.yyline(), SymbolType.BRACKET_CLOSE);
            s = lex();
        }
        lexer.pushback(s);
    }

    private void classTraits(List<SimpleParseException> errors, boolean outsidePackage, Reference<Boolean> cinitNeedsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Path pkg, String classNameStr, boolean isInterface, Reference<Boolean> iinitNeedsActivation, ABC abc, List<VariableOrScope> classVariables) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {

        Stack<Loop> cinitLoops = new Stack<>();
        Map<Loop, String> cinitLoopLabels = new HashMap<>();

        List<VariableOrScope> traitVariables = new ArrayList<>();

        looptraits:
        while (true) {
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
            parseMetadata(errors);

            ParsedSymbol s = lex();
            loops: while (s.isType(SymbolType.NATIVE, SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE, SymbolType.PROTECTED, SymbolType.OVERRIDE, SymbolType.FINAL, SymbolType.DYNAMIC, SymbolGroup.IDENTIFIER, SymbolType.INTERNAL, SymbolType.PREPROCESSOR)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        errors.add(new SimpleParseException("Only one final keyword allowed", lexer.yyline(), s.position));
                    }
                    preSymbols.add(s);
                    isFinal = true;
                } else if (s.type == SymbolType.OVERRIDE) {
                    if (isOverride) {
                        errors.add(new SimpleParseException("Only one override keyword allowed", lexer.yyline(), s.position));
                    }
                    preSymbols.add(s);
                    isOverride = true;
                } else if (s.type == SymbolType.STATIC) {
                    if (isInterface) {
                        errors.add(new SimpleParseException("Interface cannot have static traits", lexer.yyline(), s.position));
                    }
                    if (classNameStr == null) {
                        errors.add(new SimpleParseException("No static keyword allowed here", lexer.yyline(), s.position));
                    }
                    if (isStatic) {
                        errors.add(new SimpleParseException("Only one static keyword allowed", lexer.yyline(), s.position));
                    }
                    preSymbols.add(s);
                    isStatic = true;
                } else if (s.type == SymbolType.NAMESPACE) {
                    preSymbols.add(s);
                    break;
                } else if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        errors.add(new SimpleParseException("Only one native keyword allowed", lexer.yyline(), s.position));
                    }
                    preSymbols.add(s);
                    isNative = true;
                } else if (s.group == SymbolGroup.IDENTIFIER) {
                    customNs = s.value.toString();
                    classVariables.add(new Namespace(false, new Path(s.value.toString()), s.position));
                    if (isInterface) {
                        errors.add(new SimpleParseException("Namespace attributes are not permitted on interface methods", lexer.yyline(), s.position));
                    }
                    preSymbols.add(s);
                }
                /*else if (namespace != null) {
                    errors.add(new SimpleParseException("Only one access identifier allowed", lexer.yyline(), s.position));
                }*/
                switch (s.type) {
                    case PUBLIC:
                        if (isInterface) {
                            errors.add(new SimpleParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position));
                        }
                        preSymbols.add(s);
                        break;
                    case PRIVATE:
                        isPrivate = true;
                        if (isInterface) {
                            errors.add(new SimpleParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position));
                        }
                        preSymbols.add(s);
                        break;
                    case PROTECTED:
                        if (isInterface) {
                            errors.add(new SimpleParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position));
                        }
                        preSymbols.add(s);
                        break;
                    case INTERNAL:
                        if (isInterface) {
                            errors.add(new SimpleParseException("Interface members cannot be declared public, private, protected, or internal", lexer.yyline(), s.position));
                        }
                        preSymbols.add(s);
                        break;
                    case PREPROCESSOR:
                        if (((String) s.value).toLowerCase().equals("namespace")) {
                            preSymbols.add(s);
                            s = lex();
                            expected(errors, s, lexer.yyline(), SymbolType.PARENT_OPEN);
                            preSymbols.add(s);
                            s = lex();
                            expected(errors, s, lexer.yyline(), SymbolType.STRING);
                            preSymbols.add(s);
                            s = lex();
                            expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                            preSymbols.add(s);

                        } else {
                            lexer.pushback(s);
                            break loops;
                        }
                        break;
                }
                s = lex();
            }

            Path fullClassName = pkg.add(classNameStr);
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
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.PARENT_OPEN)) {
                        break;
                    }
                    Path fname;
                    int fnamePos = s.position;

                    //fix for methods with name "get" or "set" - they are not getters/setters!
                    if (s.isType(SymbolType.PARENT_OPEN)) {
                        lexer.pushback(s);
                        if (isGetter) {
                            fname = new Path("get");
                            //isGetter = false;
                        } else if (isSetter) {
                            fname = new Path("set");
                            //isSetter = false;
                        } else {
                            errors.add(new SimpleParseException("Missing method name", lexer.yyline(), s.position));
                            break;
                        }
                    } else {
                        fname = new Path(s.value.toString());
                    }
                    if (fname.toString().equals(classNameStr)) { //constructor
                        if (isStatic) {
                            errors.add(new SimpleParseException("Constructor cannot be static", lexer.yyline(), s.position));
                        }
                        if (isOverride) {
                            errors.add(new SimpleParseException("Override flag not allowed for constructor", lexer.yyline(), s.position));
                        }
                        if (isFinal) {
                            errors.add(new SimpleParseException("Final flag not allowed for constructor", lexer.yyline(), s.position));
                        }
                        if (isInterface) {
                            errors.add(new SimpleParseException("Interface cannot have constructor", lexer.yyline(), s.position));
                        }
                        method(errors, outsidePackage, isPrivate, false, false, customNs, iinitNeedsActivation, importedClasses, false, false, thisType, openedNamespaces, false, new Path(), true, classVariables, abc, fnamePos, new Reference<>(null), new Reference<>(null));
                    } else {
                        if (classNameStr == null) {
                            isStatic = true;
                        }

                        s = lex();
                        if (s.type == SymbolType.NAMESPACESUFFIX) {
                            //ignore
                        } else {
                            lexer.pushback(s);
                        }

                        Reference<Path> returnTypeRef = new Reference<>(null);
                        Reference<Variable> returnSubTypeRef = new Reference<>(null);
                        method(errors, outsidePackage, isPrivate, isInterface, isNative, customNs, new Reference<>(false), importedClasses, isOverride, isFinal, thisType, openedNamespaces, isStatic, fname, true, classVariables, abc, fnamePos, returnTypeRef, returnSubTypeRef);

                        traitVariables.add(new ClassTrait(fullClassName, fname, customNs, fnamePos, isStatic, new Path("Function"), returnTypeRef.getVal(), null, returnSubTypeRef.getVal()));

                        /*
                        if (isGetter) {
                            if (!ft.paramTypes.isEmpty()) {
                                errors.add(new SimpleParseException("Getter can't have any parameters", lexer.yyline()));
                            }
                        }

                        if (isSetter) {
                            if (ft.paramTypes.size() != 1) {
                                errors.add(new SimpleParseException("Getter must have exactly one parameter", lexer.yyline()));
                            }
                        }

                        if (isStatic && isInterface) {
                            if (isInterface) {
                                errors.add(new SimpleParseException("Interface cannot have static fields", lexer.yyline()));
                            }
                        }
                         */
                    }
                    break;
                case NAMESPACE:
                    if (isInterface) {
                        errors.add(new SimpleParseException("Interface cannot have namespace fields", lexer.yyline(), s.position));
                    }
                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    Path nname = new Path(s.value.toString());
                    int npos = s.position;

                    traitVariables.add(new ClassTrait(fullClassName, nname, customNs, npos, isStatic, new Path("Namespace"), null, null, null));
                    s = lex();

                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolType.STRING);
                        s = lex();
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }

                    classVariables.add(new Variable(true, nname, npos));
                    break;
                case CONST:
                case VAR:
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isOverride) {
                        errors.add(new SimpleParseException("Override flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position));
                    }
                    if (isFinal) {
                        errors.add(new SimpleParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position));
                    }
                    if (isInterface) {
                        errors.add(new SimpleParseException("Interface cannot have variable/const fields", lexer.yyline(), s.position));
                    }

                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    ParsedSymbol nameSymbol = s;
                    s = lex();

                    if (s.type == SymbolType.NAMESPACESUFFIX) {
                        //ignore
                        s = lex();
                    }

                    Reference<Variable> subTypeRef = new Reference<>(null);
                    Path traitType = new Path("*");
                    if (s.type == SymbolType.COLON) {
                        traitType = type(subTypeRef, errors, thisType, new Reference<>(false), importedClasses, openedNamespaces, classVariables, abc);
                        s = lex();
                    }

                    traitVariables.add(new ClassTrait(fullClassName, new Path(nameSymbol.value.toString()), customNs, nameSymbol.position, isStatic, traitType, "Function".equals(traitType) ? new Path("*") : null, subTypeRef.getVal(), null));

                    if (s.type == SymbolType.ASSIGN) {
                        List<VariableOrScope> constVarVariables = new ArrayList<>();
                        expression(errors, thisType, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, isStatic, true, constVarVariables, false, abc);
                        int scopePos = s.position;
                        s = lex();
                        classVariables.add(new TraitVarConstValueScope(scopePos, s.position, constVarVariables, isStatic));
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);
                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }

                    boolean cmd = command(errors, null, cinitNeedsActivation, importedClasses, openedNamespaces, cinitLoops, cinitLoopLabels, new HashMap<>(), true, false, true, 0, false, classVariables, abc);
                    if (cmd) {
                        //empty
                    } else {
                        break looptraits;
                    }
            }
        }
        classVariables.addAll(0, traitVariables);
    }

    private void scriptTraits(
            List<SimpleParseException> errors,
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables,
            List<Path> externalTypes
    ) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {

        Stack<Loop> sinitLoops = new Stack<>();
        Map<Loop, String> sinitLoopLabels = new HashMap<>();

        HashMap<String, Integer> sinitRegisterVars = new HashMap<>();
        while (scriptTraitsBlock(
                errors,
                importedClasses,
                openedNamespaces,
                allOpenedNamespaces,
                abc,
                sinitNeedsActivation,
                sinitLoops,
                sinitLoopLabels,
                sinitRegisterVars,
                sinitVariables,
                externalTypes
        )) {
            //empty
        }
    }

    private boolean scriptTraitsBlock(
            List<SimpleParseException> errors,
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            Stack<Loop> sinitLoops,
            Map<Loop, String> sinitLoopLabels,
            HashMap<String, Integer> sinitRegisterVars,
            List<VariableOrScope> sinitVariables,
            List<Path> externalTypes
    ) throws AVM2ParseException, SimpleParseException, SimpleParseException, IOException, CompilationException, InterruptedException {
        ParsedSymbol s;
        boolean inPackage = false;
        s = lex();

        DottedChain pkgNameDc = DottedChain.TOPLEVEL;
        Path pkgName = new Path();
        if (s.type == SymbolType.PACKAGE) {
            s = lex();
            if (s.type != SymbolType.CURLY_OPEN) {
                expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                pkgNameDc = pkgNameDc.addWithSuffix(s.value.toString());
                pkgName = pkgName.add(s.value.toString());
                s = lex();
            }
            while (s.type == SymbolType.DOT) {
                s = lex();
                expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                pkgNameDc = pkgNameDc.addWithSuffix(s.value.toString());
                pkgName = pkgName.add(s.value.toString());
                s = lex();
            }
            expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
            s = lex();
            inPackage = true;
        }
        lexer.pushback(s);

        allOpenedNamespaces.add(openedNamespaces);

        for (String name : abc.getSwf().getAbcIndex().getPackageObjects(pkgNameDc)) {
            externalTypes.add(pkgName.add(name));
        }

        List<VariableOrScope> sinitTraitVariables = new ArrayList<>();

        parseImportsUsages(errors, sinitVariables, importedClasses, openedNamespaces, abc, externalTypes);

        boolean isEmpty = true;

        looptrait:
        while (true) {
            parseMetadata(errors);
            s = lex();
            boolean isFinal = false;
            boolean isDynamic = false;
            boolean isPublic = false;
            boolean isNative = false;
            boolean isInternal = false;
            List<ParsedSymbol> preSymbols = new ArrayList<>();
            while (s.isType(SymbolType.FINAL, SymbolType.DYNAMIC, SymbolType.PUBLIC, SymbolType.INTERNAL, SymbolType.NATIVE)) {
                if (s.type == SymbolType.FINAL) {
                    if (isFinal) {
                        errors.add(new SimpleParseException("Only one final keyword allowed", lexer.yyline(), s.position));
                    }
                    isFinal = true;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.INTERNAL) {
                    if (!inPackage) {
                        errors.add(new SimpleParseException("internal only allowed inside package", lexer.yyline(), s.position));

                    }
                    if (isPublic || isInternal) {
                        errors.add(new SimpleParseException("Only one public/internal keyword allowed", lexer.yyline(), s.position));
                    }
                    isInternal = true;
                    //ns = internalNs;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.PUBLIC) {
                    if (!inPackage) {
                        errors.add(new SimpleParseException("public only allowed inside package", lexer.yyline(), s.position));

                    }
                    if (isPublic || isInternal) {
                        errors.add(new SimpleParseException("Only one public/internal keyword allowed", lexer.yyline(), s.position));
                    }
                    isPublic = true;
                    //ns = publicNs;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.DYNAMIC) {
                    if (isDynamic) {
                        errors.add(new SimpleParseException("Only one dynamic keyword allowed", lexer.yyline(), s.position));
                    }
                    isDynamic = true;
                    preSymbols.add(s);
                }
                if (s.type == SymbolType.NATIVE) {
                    if (isNative) {
                        errors.add(new SimpleParseException("Only one native keyword allowed", lexer.yyline(), s.position));
                    }
                    isNative = true;
                    preSymbols.add(s);
                }
                s = lex();
            }

            switch (s.type) {
                case CLASS:
                case INTERFACE:
                    int scopePos = s.position;
                    isEmpty = false;
                    List<NamespaceItem> subOpenedNamespaces = new ArrayList<>(openedNamespaces);
                    boolean isInterface = false;
                    if (s.type == SymbolType.INTERFACE) {
                        isInterface = true;
                    }
                    String subNameStr;

                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    subNameStr = s.value.toString();
                    int subNamePos = s.position;
                    sinitTraitVariables.add(new Type(true, pkgName.add(subNameStr), subNamePos));
                    s = lex();

                    if (s.type == SymbolType.NOT) {
                        s = lex();
                    } else if (s.type == SymbolType.TERNAR) {
                        s = lex();
                    }

                    if (!isInterface) {

                        if (s.type == SymbolType.EXTENDS) {
                            type(new Reference<>(null), errors, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                            s = lex();
                        }
                        if (s.type == SymbolType.IMPLEMENTS) {
                            do {
                                type(new Reference<>(null), errors, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    } else {
                        if (s.type == SymbolType.EXTENDS) {
                            do {
                                type(new Reference<>(null), errors, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                                s = lex();
                            } while (s.type == SymbolType.COMMA);
                        }
                        expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    }

                    Reference<Boolean> cinitNeedsActivation = new Reference<>(false);
                    Reference<Boolean> iinitNeedsActivation = new Reference<>(false);
                    List<VariableOrScope> classVariables = new ArrayList<>();
                    classVariables.add(new Variable(true, new Path("this"), s.position, false, pkgName.add(subNameStr), null));

                    classTraits(errors, !inPackage, cinitNeedsActivation, importedClasses, subOpenedNamespaces, pkgName, subNameStr, isInterface, iinitNeedsActivation, abc, classVariables);

                    s = lex();
                    sinitVariables.add(new ClassScope(scopePos, s.position, classVariables));

                    expected(errors, s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    break;
                case FUNCTION:
                    isEmpty = false;
                    s = lex();
                    expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    String fname = s.value.toString();

                    Reference<Path> returnTypeRef = new Reference<>(null);
                    Reference<Variable> returnSubTypeRef = new Reference<>(null);
                    method(errors, !inPackage, false, false, isNative, null, new Reference<>(false), importedClasses, false, isFinal, null, openedNamespaces, true, new Path(""), true, sinitVariables, abc, s.position, returnTypeRef, returnSubTypeRef);

                    sinitTraitVariables.add(new Variable(true, new Path(fname), s.position, true, new Path("Function"), returnTypeRef.getVal(), null, returnSubTypeRef.getVal()));
                    break;
                case CONST:
                case VAR:
                    isEmpty = false;
                    boolean isConst = s.type == SymbolType.CONST;
                    if (isFinal) {
                        errors.add(new SimpleParseException("Final flag not allowed for " + (isConst ? "consts" : "vars"), lexer.yyline(), s.position));
                    }

                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    ParsedSymbol traitSymb = s;
                    s = lex();
                    Path traitType = new Path("*");
                    Reference<Variable> subTypeRef = new Reference<>(null);
                    if (s.type == SymbolType.COLON) {
                        traitType = type(subTypeRef, errors, null, new Reference<>(false), importedClasses, openedNamespaces, sinitVariables, abc);
                        s = lex();
                    }
                    sinitTraitVariables.add(new Variable(true, new Path(traitSymb.value.toString()), traitSymb.position, null, traitType, traitType.equals("Function") ? new Path("*") : null, subTypeRef.getVal(), null));

                    if (s.type == SymbolType.ASSIGN) {
                        expression(errors, null, new Reference<>(false), importedClasses, openedNamespaces, new HashMap<>(), false, false, true, true, sinitVariables, false, abc);
                        s = lex();
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                case NAMESPACE:
                    isEmpty = false;
                    if (isFinal) {
                        errors.add(new SimpleParseException("Final flag not allowed for namespaces", lexer.yyline(), s.position));
                    }
                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    sinitTraitVariables.add(new Variable(true, new Path(s.value.toString()), s.position, null, new Path("Namespace"), null));

                    s = lex();

                    if (s.type == SymbolType.ASSIGN) {
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolType.STRING);
                        s = lex();
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);

                    for (int i = preSymbols.size() - 1; i >= 0; i--) {
                        lexer.pushback(preSymbols.get(i));
                    }

                    if (parseImportsUsages(errors, sinitVariables, importedClasses, openedNamespaces, abc, externalTypes)) {
                        break;
                    }
                    boolean cmd = command(errors, null, sinitNeedsActivation, importedClasses, openedNamespaces, sinitLoops, sinitLoopLabels, sinitRegisterVars, true, false, true, 0, false, sinitVariables, abc);
                    if (cmd) {
                        isEmpty = false;
                    } else {
                        break looptrait;
                    }
            }

        }
        if (inPackage) {
            expectedType(errors, SymbolType.CURLY_CLOSE);
        }
        sinitVariables.addAll(0, sinitTraitVariables);
        return !isEmpty;
    }

    private void xmltag(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> usesVars, List<String> openedTags, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s;
        StringBuilder sb = new StringBuilder();
        loop:
        do {
            s = lex();
            List<String> sub = new ArrayList<>();
            Reference<Boolean> subusesvars = new Reference<>(false);
            switch (s.type) {
                case XML_ATTRNAMEVAR_BEGIN: // {...}=       add
                    usesVars.setVal(true);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    expectedType(errors, SymbolType.ASSIGN);
                    sb.append("=");
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAGATTRIB);
                    break;
                case XML_ATTRVALVAR_BEGIN: // ={...}         esc_xattr
                    usesVars.setVal(true);
                    sb.append("\"");
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    sb.append("\"");
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    break;
                case XML_VAR_BEGIN: // {...}                esc_xelem
                    usesVars.setVal(true);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XML);
                    break;
                case XML_FINISHVARTAG_BEGIN: // </{...}>    add
                    usesVars.setVal(true);
                    sb.append("</");
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    lexer.begin(ActionScriptLexer.XMLCLOSETAGFINISH);
                    s = lex();
                    while (s.isType(SymbolType.XML_TEXT)) {
                        sb.append(s.value);
                        s = lex();
                    }
                    expected(errors, s, lexer.yyline(), SymbolType.GREATER_THAN);
                    sb.append(">");

                    if (openedTags.isEmpty()) {
                        errors.add(new SimpleParseException("XML : Closing unopened tag", lexer.yyline(), s.position));
                    }
                    openedTags.remove(openedTags.size() - 1);
                    break;
                case XML_STARTVARTAG_BEGIN: // <{...}>      add
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    lexer.yybegin(ActionScriptLexer.XMLOPENTAG);
                    sub.add("*");
                    sb.append("<");
                    xmltag(errors, thisType, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    break;
                case XML_STARTTAG_BEGIN:    // <xxx>
                    sub.add(s.value.toString().trim().substring(1)); //remove < from beginning
                    xmltag(errors, thisType, subusesvars, sub, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    sb.append(s.value.toString());
                    break;
                case XML_FINISHTAG:
                    String tname = s.value.toString().substring(2, s.value.toString().length() - 1).trim();
                    if (openedTags.isEmpty()) {
                        errors.add(new SimpleParseException("XML : Closing unopened tag", lexer.yyline(), s.position));
                    }
                    String lastTName = openedTags.get(openedTags.size() - 1);
                    if (lastTName.equals(tname) || lastTName.equals("*")) {
                        openedTags.remove(openedTags.size() - 1);
                    } else {
                        errors.add(new SimpleParseException("XML : Closing unopened tag", lexer.yyline(), s.position));
                    }
                    sb.append(s.value.toString());
                    break;
                case XML_STARTFINISHTAG_END:
                    openedTags.remove(openedTags.size() - 1); //close last tag
                    sb.append(s.value.toString());
                    break;
                case EOF:
                    errors.add(new SimpleParseException("End of file before XML finish", lexer.yyline(), s.position));
                    return;
                default:
                    sb.append(s.value.toString());
                    break;
            }
        } while (!openedTags.isEmpty());
    }

    private boolean xml(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        List<String> openedTags = new ArrayList<>();
        xmltag(errors, thisType, new Reference<>(false), openedTags, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
        lexer.setEnableWhiteSpace(true);
        lexer.begin(ActionScriptLexer.YYINITIAL);
        ParsedSymbol s = lexer.lex();
        while (s.isType(SymbolType.XML_WHITESPACE)) {
            s = lexer.lex();
        }
        lexer.setEnableWhiteSpace(false);
        lexer.pushback(s);
        //TODO: Order of additions as in official compiler
        return true;
    }

    private boolean command(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, Stack<Loop> loops, Map<Loop, String> loopLabels, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, int forinlevel, boolean mustBeCommand, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
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
                expectedType(errors, SymbolType.NAMESPACE);
                expectedType(errors, SymbolType.ASSIGN);
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                ret = true;
                //TODO: use dxns for attribute namespaces instead of dxnslate
            }
        }
        if (!ret) {
            switch (s.type) {
                case USE:
                    expectedType(errors, SymbolType.NAMESPACE);
                    type(new Reference<>(null), errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    break;
                case WITH:
                    needsActivation.setVal(true);
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    /*if (!isNameOrProp(wvar)) {
                        errors.add(new SimpleParseException("Not a property or name", lexer.yyline()));
                    }*/
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    expectedType(errors, SymbolType.CURLY_OPEN);
                    List<VariableOrScope> withVars = new ArrayList<>();
                    commands(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, withVars, abc);
                    variables.addAll(withVars);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    //FIXME?
                    ret = true;
                    break;
                case FUNCTION:
                    s = lexer.lex();
                    expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    needsActivation.setVal(true);
                    function(errors, false, false, needsActivation, importedClasses, thisType, openedNamespaces, new Path(s.value.toString()), false, variables, abc, s.position, false, new Reference<>(null), new Reference<>(null));
                    ret = true;
                    break;
                case VAR:
                case CONST:
                    s = lex();
                    if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER)) {
                        break;
                    }
                    Path varIdentifier = new Path(s.value.toString());
                    int varPos = s.position;
                    s = lex();
                    Path varType = new Path("*");
                    Reference<Variable> varSubTypeRef = new Reference<>(null);
                    if (s.type == SymbolType.COLON) {
                        varType = type(varSubTypeRef, errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        s = lex();
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        variables.add(new Variable(true, varIdentifier, varPos, null, varType, null, varSubTypeRef.getVal(), null));
                        ret = true;
                    } else {
                        variables.add(new Variable(true, varIdentifier, varPos, null, varType, null, varSubTypeRef.getVal(), null));
                        lexer.pushback(s);
                        ret = true;
                    }
                    break;
                case CURLY_OPEN:
                    commands(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, variables, abc);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    ret = true;
                    break;
                case SUPER: //constructor call
                    ParsedSymbol ss2 = lex();
                    if (ss2.type == SymbolType.PARENT_OPEN) {
                        Path lastVarName = new Path("super");
                        lastVarName = call(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                        ret = true;
                    } else { //no constructor call, but it could be calling parent methods... => handle in expression
                        lexer.pushback(ss2);
                        lexer.pushback(s);
                    }
                    break;
                case IF:
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                    s = lex();
                    if (s.type == SymbolType.ELSE) {
                        command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                    } else {
                        lexer.pushback(s);
                    }
                    ret = true;
                    break;
                case WHILE:
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, true, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    Loop wloop = new Loop(uniqId(), null, null);
                    if (loopLabel != null) {
                        loopLabels.put(wloop, loopLabel);
                    }
                    loops.push(wloop);
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                    loops.pop();
                    ret = true;
                    break;
                case DO:
                    Loop dloop = new Loop(uniqId(), null, null);
                    loops.push(dloop);
                    if (loopLabel != null) {
                        loopLabels.put(dloop, loopLabel);
                    }
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);

                    expectedType(errors, SymbolType.WHILE);
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, true, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    loops.pop();
                    ret = true;
                    break;
                case FOR:
                    s = lex();
                    boolean forin = false;
                    if (s.type == SymbolType.EACH) {
                        forin = true;
                        s = lex();
                    }
                    expected(errors, s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, false, variables, abc);
                    Loop floop = new Loop(uniqId(), null, null);
                    loops.push(floop);
                    if (loopLabel != null) {
                        loopLabels.put(floop, loopLabel);
                    }
                    s = lex();
                    if (s.type == SymbolType.IN) {
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, false, variables, false, abc);
                    } else {
                        lexer.pushback(s);
                    }
                    if (!forin) {
                        s = lex();
                        if (!s.isType(SymbolType.PARENT_CLOSE)) {
                            lexer.pushback(s);
                            expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                            expectedType(errors, SymbolType.SEMICOLON);
                            command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                        } else {
                            lexer.pushback(s);
                        }
                    }
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forin ? forinlevel + 1 : forinlevel, true, variables, abc);
                    loops.pop();
                    ret = true;
                    break;
                case SWITCH:
                    Loop sloop = new Loop(-uniqId(), null, null); //negative id marks switch = no continue
                    loops.push(sloop);
                    if (loopLabel != null) {
                        loopLabels.put(sloop, loopLabel);
                    }
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    expectedType(errors, SymbolType.CURLY_OPEN);
                    s = lex();
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                            if (s.type != SymbolType.DEFAULT) {
                                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, true, abc);
                            }
                            expectedType(errors, SymbolType.COLON);
                            s = lex();
                        }
                        lexer.pushback(s);
                        commands(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, variables, abc);
                        s = lex();
                    }
                    expected(errors, s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                    ret = true;
                    loops.pop();
                    break;
                case BREAK:
                    s = lex();
                    long bloopId = 0;
                    if (loops.isEmpty()) {
                        errors.add(new SimpleParseException("No loop to break", lexer.yyline(), s.position));
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
                            errors.add(new SimpleParseException("Identifier of loop expected", lexer.yyline(), s.position));
                        }
                    } else {
                        lexer.pushback(s);
                        //bloopId = loops.peek().id;
                    }
                    ret = true;
                    break;
                case CONTINUE:
                    s = lex();
                    long cloopId = 0;
                    if (loops.isEmpty()) {
                        errors.add(new SimpleParseException("No loop to continue", lexer.yyline(), s.position));
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
                            errors.add(new SimpleParseException("Identifier of loop expected", lexer.yyline(), s.position));
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
                            errors.add(new SimpleParseException("No loop to continue", lexer.yyline(), s.position));
                        }
                    }
                    //TODO: handle switch (???)
                    ret = true;
                    break;
                case RETURN:
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, true, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    ret = true;
                    break;
                case TRY:
                    needsActivation.setVal(true);
                    command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                    s = lex();
                    boolean found = false;
                    while (s.type == SymbolType.CATCH) {
                        int scopePos = s.position;
                        expectedType(errors, SymbolType.PARENT_OPEN);
                        s = lex();
                        if (!expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP)) {
                            break;
                        }
                        Path enamestr = new Path(s.value.toString());
                        int ePos = s.position;
                        expectedType(errors, SymbolType.COLON);
                        Reference<Variable> catchSubType = new Reference<>(null);
                        Path catchType = type(catchSubType, errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc);
                        expectedType(errors, SymbolType.PARENT_CLOSE);
                        List<VariableOrScope> catchVars = new ArrayList<>();
                        expectedType(errors, SymbolType.CURLY_OPEN);
                        commands(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, catchVars, abc);
                        int eScopePos = s.position;
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                        variables.add(new CatchScope(eScopePos, s.position, new Variable(true, enamestr, ePos, null, catchType, null, catchSubType.getVal(), null), catchVars));
                        s = lex();
                        found = true;
                    }
                    if (s.type == SymbolType.FINALLY) {
                        command(errors, thisType, needsActivation, importedClasses, openedNamespaces, loops, loopLabels, registerVars, inFunction, inMethod, isStatic, forinlevel, true, variables, abc);
                        found = true;
                        s = lex();
                    }
                    if (!found) {
                        expected(errors, s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                    }
                    lexer.pushback(s);
                    ret = true;
                    break;
                case THROW:
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    ret = true;
                    break;
                default:
                    if (s.type == SymbolType.SEMICOLON) {
                        return true;
                    }
                    lexer.pushback(s);
                    ret = expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, true, abc);
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
            ret = expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private void brackets(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                s = lex();
                if (!expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                    break;
                }
            }
        } else {
            lexer.pushback(s);
        }
    }

    private boolean expression(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        return expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, allowComma, abc);
    }

    private boolean expression(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

        ParsedSymbol symb;
        do {
            boolean prim = expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, abc);
            if (!prim) {
                return false;
            }
            expression1(errors, prim, GraphTargetItem.NOPRECEDENCE, thisType, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, abc);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
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
            String pb = symb.value.toString().substring(1);
            symb.type = SymbolType.LOWER_THAN;
            symb.group = SymbolGroup.OPERATOR;
            symb.value = "<";
            int pos = 1;
            if (pb.charAt(0) == '=') {
                symb.type = SymbolType.LOWER_EQUAL;
                symb.value = "<=";
                pb = pb.substring(1);
                pos++;
            }
            lexer.yypushbackstr(pb, ActionScriptLexer.YYINITIAL, pos); //parse again as LOWER_THAN
        }
    }

    private void regexpToDivideFix(ParsedSymbol symb) {
        if (symb.isType(SymbolType.REGEXP)) {
            String pb = symb.value.toString().substring(1);
            symb.type = SymbolType.DIVIDE;
            symb.group = SymbolGroup.OPERATOR;
            symb.value = "/";
            int pos = 1;
            if (pb.charAt(0) == '=') {
                symb.type = SymbolType.ASSIGN_DIVIDE;
                symb.value = "/=";
                pb = pb.substring(1);
                pos++;
            }
            lexer.yypushbackstr(pb, ActionScriptLexer.YYINITIAL, pos); //parse again as DIVIDE

        }
    }

    private ParsedSymbol peekExprToken() throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        ParsedSymbol lookahead = lex();
        xmlToLowerThanFix(lookahead);
        regexpToDivideFix(lookahead);

        lexer.pushback(lookahead);
        return lookahead;
    }

    private boolean expression1(List<SimpleParseException> errors, boolean lhs, int min_precedence, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, boolean allowRemainder, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expression1:");
        }
        ParsedSymbol lookahead = peekExprToken();

        ParsedSymbol op;
        boolean rhs;

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
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, false, abc);
                expectedType(errors, SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, abc);
            if (!rhs) {
                lexer.pushback(op);
                errors.add(new SimpleParseException("Missing operand", lexer.yyline(), op.position));
                return false;
            }

            lookahead = peekExprToken();
            while ((lookahead.type.isBinary() && lookahead.type.getPrecedence() < /* > on wiki */ op.type.getPrecedence())
                    || (lookahead.type.isRightAssociative() && lookahead.type.getPrecedence() == op.type.getPrecedence())) {
                rhs = expression1(errors, rhs, lookahead.type.getPrecedence(), thisType, needsActivation, importedClasses, openedNamespaces, allowEmpty, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, abc);
                if (!rhs) {
                    break;
                }
                lookahead = peekExprToken();
            }

            switch (op.type) {
                case AS:
                    lhs = true;
                    allowRemainder = false;
                    break;

                case IN:
                case TERNAR: //???
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                case USHIFT_RIGHT:
                case BITAND:
                case BITOR:
                case DIVIDE:
                case MODULO:
                case EQUALS:
                case STRICT_EQUALS:
                case NOT_EQUAL:
                case STRICT_NOT_EQUAL:
                case LOWER_THAN:
                case LOWER_EQUAL:
                case GREATER_THAN:
                case GREATER_EQUAL:
                case AND:
                case OR:
                case MINUS:
                case MULTIPLY:
                case PLUS:
                case XOR:
                case INSTANCEOF:
                case IS:
                case NULL_COALESCE:
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
                    lhs = true;
                    break;
            }
        }
        //???
        /*if (lhs instanceof ParenthesisItem) {
            expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, false, abc);            
        }*/

        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private boolean expressionPrimary(List<SimpleParseException> errors, TypeItem thisType, Reference<Boolean> needsActivation, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean isStatic, boolean allowRemainder, List<VariableOrScope> variables, ABC abc) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean ret = false;
        ParsedSymbol s = lex();
        Path lastVarName = new Path();
        boolean allowMemberOrCall = false;
        switch (s.type) {
            case PREPROCESSOR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                switch ("" + s.value) {
                    //AS3
                    case "hasnext":
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        expectedType(errors, SymbolType.COMMA);
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        ret = true;
                        break;
                    case "newactivation":
                        ret = true;
                        break;
                    case "nextname":
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        expectedType(errors, SymbolType.COMMA);
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        ret = true;
                        allowMemberOrCall = true;
                        break;
                    case "nextvalue":
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        expectedType(errors, SymbolType.COMMA);
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        ret = true;
                        allowMemberOrCall = true;
                        break;
                    //Both ASs
                    case "dup":
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        ret = true;
                        allowMemberOrCall = true;                        
                        break;
                    case "push":
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                        ret = true;
                        break;
                    case "pop":
                        ret = true;
                        allowMemberOrCall = true;
                        break;
                    case "swap":
                        ret = true;
                        break;
                    case "goto":
                        expectedType(errors, SymbolGroup.IDENTIFIER);
                        //errors.add(new SimpleParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline(), s.position));
                        ret = true;
                        break;
                    case "multiname":
                        expectedType(errors, SymbolType.INTEGER);
                        //errors.add(new SimpleParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline(), s.position));
                        ret = true;
                        break;
                    default:
                        errors.add(new SimpleParseException("Unknown preprocessor instruction: §§" + s.value, lexer.yyline(), s.position));
                        break;
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                break;
            case REGEXP:
                allowMemberOrCall = true;
                ret = true;
                break;
            case XML_STARTTAG_BEGIN:
            case XML_STARTVARTAG_BEGIN:
            case XML_CDATA:
            case XML_COMMENT:
                lexer.pushback(s);
                ret = xml(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                break;
            case STRING:
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEGATE:
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, false, variables, abc);
                ret = true;

                break;
            case PLUS:
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, true, variables, abc);
                ret = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = true;
                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = true;
                } else if (s.isType(SymbolType.DECIMAL)) {
                    ret = true;
                } else if (s.isType(SymbolType.FLOAT)) {
                    ret = true;
                } else {
                    lexer.pushback(s);
                    expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, true, variables, abc);
                    ret = true;
                }
                break;
            case TYPEOF:
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, false, variables, abc);
                ret = true;
                break;
            case TRUE:
                ret = true;
                break;
            case NULL:
                ret = true;
                break;
            case UNDEFINED:
                ret = true;
                break;
            case FALSE:
                ret = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();

                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER, SymbolType.STRING, SymbolType.INTEGER, SymbolType.DOUBLE, SymbolType.PARENT_OPEN);

                    if (s.type == SymbolType.PARENT_OPEN) { //special for obfuscated SWFs
                        expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, false, abc);
                        expectedType(errors, SymbolType.PARENT_CLOSE);
                    }
                    expectedType(errors, SymbolType.COLON);
                    expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, allowRemainder, variables, false, abc);

                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = true;
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                brackets(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FUNCTION:
                s = lexer.lex();
                Path fname = new Path();
                int fnamePos = -1;
                if (s.isType(SymbolGroup.IDENTIFIER)) {
                    fname = new Path(s.value.toString());
                    fnamePos = s.position;
                } else {
                    lexer.pushback(s);
                }
                needsActivation.setVal(true);
                function(errors, false, false, needsActivation, importedClasses, thisType, openedNamespaces, fname, false, variables, abc, fnamePos, false, new Reference<>(null), new Reference<>(null));
                ret = true;
                allowMemberOrCall = true;
                break;
            /*case NAN:
                ret = true;
                break;*/
            case INFINITY:
                ret = true;
                break;
            case INTEGER:
                ret = true;
                break;
            case DOUBLE:
                ret = true;
                allowMemberOrCall = true; // 5.2.toString();
                break;
            case DECIMAL:
                if (!abc.hasDecimalSupport()) {
                    errors.add(new SimpleParseException("The ABC has no decimal support", lexer.yyline(), s.position));
                }
                ret = true;
                allowMemberOrCall = true;
                break;
            case FLOAT:
                if (!abc.hasFloatSupport()) {
                    errors.add(new SimpleParseException("The ABC has no float support", lexer.yyline(), s.position));
                }
                ret = true;
                allowMemberOrCall = true;
                break;
            case FLOAT4:
                if (!abc.hasFloat4Support()) {
                    //parse again as method call
                    lexer.yypushbackstr(lexer.yytext().substring("float4".length()), ActionScriptLexer.YYINITIAL, "float4".length());
                    lexer.pushback(new ParsedSymbol(lexer.yychar() /*???*/, SymbolGroup.IDENTIFIER, SymbolType.IDENTIFIER, "float4"));
                    name(errors, thisType, needsActivation, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, importedClasses, abc);
                    ret = true;
                } else {
                    ret = true; //new Float4ValueAVM2Item(null, null, (Float4) s.value);
                }
                allowMemberOrCall = true;
                break;
            case DELETE:
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, true, variables, abc);
                ret = true;
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, false/*?*/, variables, abc);
                /*if (!isNameOrProp(varincdec)) {
                    errors.add(new SimpleParseException("Not a property or name", lexer.yyline()));
                }*/
                ret = true;
                break;
            case NOT:
                expressionPrimary(errors, thisType, needsActivation, importedClasses, openedNamespaces, false, registerVars, inFunction, inMethod, isStatic, false, variables, abc);
                ret = true;
                break;
            case PARENT_OPEN:
                expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, true, abc);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                /*if (ret.value == null) {
                    errors.add(new SimpleParseException("Expression in parenthesis expected", lexer.yyline()));
                }*/
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEW:
                s = lex();
                if (s.type == SymbolType.XML_STARTTAG_BEGIN) {
                    lexer.yypushbackstr(s.value.toString().substring(1), ActionScriptLexer.YYINITIAL, 1);
                    s = new ParsedSymbol(s.position, SymbolGroup.OPERATOR, SymbolType.LOWER_THAN);
                }
                if (s.type == SymbolType.FUNCTION) {
                    s = lexer.lex();
                    Path ffname = new Path();
                    int ffnamePos = -1;
                    if (s.isType(SymbolGroup.IDENTIFIER)) {
                        ffname = new Path(s.value.toString());
                        ffnamePos = s.position;
                    } else {
                        lexer.pushback(s);
                    }
                    needsActivation.setVal(true);
                    function(errors, false, false, needsActivation, importedClasses, thisType, openedNamespaces, ffname, false, variables, abc, ffnamePos, false, new Reference<>(null), new Reference<>(null));
                    ret = true;
                } else if (s.type == SymbolType.LOWER_THAN) {
                    type(new Reference<>(null), errors, thisType, needsActivation, importedClasses, openedNamespaces, variables, abc);
                    expectedType(errors, SymbolType.GREATER_THAN);
                    s = lex();
                    expected(errors, s, lexer.yyline(), SymbolType.BRACKET_OPEN);
                    lexer.pushback(s);
                    brackets(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    ret = true;
                } else if (s.type == SymbolType.PARENT_OPEN) {
                    boolean newvar = expression(errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, true, variables, false, abc);
                    applyType(errors, thisType, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    lastVarName = call(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    ret = true;

                } else {
                    lexer.pushback(s);
                    boolean newvar = true;
                    name(errors, thisType, needsActivation, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, importedClasses, abc);
                    applyType(errors, thisType, needsActivation, importedClasses, openedNamespaces, newvar, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    lastVarName = call(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, abc);
                    ret = true;
                }
                allowMemberOrCall = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
            case ATTRIBUTE:
                lexer.pushback(s);
                lastVarName = name(errors, thisType, needsActivation, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, importedClasses, abc);
                ret = true;
                allowMemberOrCall = true;
                break;
            default:
                lexer.pushback(s);
                if (s.isType(SymbolGroup.IDENTIFIER)) {
                    lastVarName = name(errors, thisType, needsActivation, openedNamespaces, registerVars, inFunction, inMethod, isStatic, variables, importedClasses, abc);
                    ret = true;
                    allowMemberOrCall = true;                
                }                                
        }
        if (allowMemberOrCall && ret) {
            memberOrCall(lastVarName, errors, thisType, needsActivation, importedClasses, openedNamespaces, ret, registerVars, inFunction, inMethod, isStatic, variables, abc);
            ret = true;
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private ActionScriptLexer lexer = null;

    private boolean parseImportsUsages(List<SimpleParseException> errors, List<VariableOrScope> variables, List<DottedChain> importedClasses, List<NamespaceItem> openedNamespaces, ABC abc, List<Path> externalTypes) throws IOException, AVM2ParseException, SimpleParseException, InterruptedException {

        boolean isEmpty = true;
        ParsedSymbol s;

        s = lex();
        while (s.isType(SymbolType.IMPORT, SymbolType.USE)) {

            if (s.isType(SymbolType.IMPORT)) {
                isEmpty = false;
                s = lex();
                expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                Path fullName = new Path(s.value.toString());
                Path lastName = new Path(s.value.toString());
                s = lex();
                boolean isStar = false;
                int varPos = -1;
                List<String> nameParts = new ArrayList<>();
                nameParts.add(lastName.toString());
                while (s.type == SymbolType.DOT) {
                    s = lex();
                    if (s.type == SymbolType.MULTIPLY) {
                        isStar = true;
                        s = lex();
                        break;
                    }
                    expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                    fullName = fullName.add(s.value.toString());
                    lastName = new Path(s.value.toString());
                    varPos = s.position;
                    nameParts.add(s.value.toString());
                    s = lex();
                }

                if (isStar) {
                    for (String n : abc.getSwf().getAbcIndex().getPackageObjects(new DottedChain(nameParts))) {
                        externalTypes.add(fullName.add(n));
                    }
                } else {
                    externalTypes.add(fullName);
                    variables.add(new Import(fullName, lastName, varPos));
                }
                expected(errors, s, lexer.yyline(), SymbolType.SEMICOLON);
            } else if (s.isType(SymbolType.USE)) {
                isEmpty = false;
                do {
                    s = lex();
                    if (s.isType(SymbolType.NAMESPACE)) {
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                        Path fullName = new Path(s.value.toString());
                        int lastPos = s.position;
                        s = lex();
                        while (s.type == SymbolType.DOT) {
                            s = lex();
                            if (s.type == SymbolType.MULTIPLY) {
                                s = lex();
                                break;
                            }
                            expected(errors, s, lexer.yyline(), SymbolGroup.IDENTIFIER);
                            fullName = fullName.add(s.value.toString());
                            lastPos = s.position;
                            s = lex();
                        }
                        variables.add(new Namespace(false, fullName, lastPos));
                        lexer.pushback(s);
                    } else {
                        if (!abc.hasDecimalSupport()) {
                            errors.add(new SimpleParseException("Invalid use kind", lexer.yyline(), s.position));
                        }

                        expected(errors, s, lexer.yyline(), SymbolType.IDENTIFIER);
                        String pragmaItemName = (String) s.value;
                        switch (pragmaItemName) {
                            case "Number":
                            case "decimal":
                            case "double":
                            case "int":
                            case "uint":
                                break;
                            case "rounding":
                                s = lex();
                                expected(errors, s, lexer.yyline(), SymbolType.IDENTIFIER);
                                String roundingIdentifier = (String) s.value;
                                switch (roundingIdentifier) {
                                    case "CEILING":
                                    case "UP":
                                    case "HALF_UP":
                                    case "HALF_EVEN":
                                    case "HALF_DOWN":
                                    case "DOWN":
                                    case "FLOOR":
                                        break;
                                    default:
                                        errors.add(new SimpleParseException("Rounding expected - one of: CEILING, UP, HALF_UP, HALF_EVEN, HALF_DOWN, DOWN, FLOOR", lexer.yyline(), s.position));
                                }
                                break;
                            case "precision":
                                s = lex();
                                expected(errors, s, lexer.yyline(), SymbolType.INTEGER);
                                int precision = (Integer) s.value;
                                if (precision < 1 || precision > 34) {
                                    errors.add(new SimpleParseException("Invalid precision - must be between 1 and 34", lexer.yyline(), s.position));
                                }
                                break;
                            default:
                                errors.add(new SimpleParseException("Invalid use kind", lexer.yyline(), s.position));
                        }
                    }
                    s = lex();
                } while (s.isType(SymbolType.COMMA));
                expected(errors, s, lexer.yyline(), SymbolType.SEMICOLON);
            }

            s = lex();
        }
        lexer.pushback(s);
        return !isEmpty;
    }

    private void parseScript(
            List<SimpleParseException> errors,
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables,
            List<Path> externalTypes
    ) throws IOException, AVM2ParseException, SimpleParseException, CompilationException, InterruptedException {
        scriptTraits(errors, importedClasses, openedNamespaces, allOpenedNamespaces, abc, sinitNeedsActivation, sinitVariables, externalTypes);
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
            List<SimpleParseException> errors,
            List<DottedChain> importedClasses,
            List<NamespaceItem> openedNamespaces,
            List<List<NamespaceItem>> allOpenedNamespaces,
            String str,
            ABC abc,
            Reference<Boolean> sinitNeedsActivation,
            List<VariableOrScope> sinitVariables,
            List<Path> externalTypes
    ) throws AVM2ParseException, SimpleParseException, IOException, CompilationException, InterruptedException {
        lexer = new ActionScriptLexer(str);

        parseScript(errors, importedClasses, openedNamespaces, allOpenedNamespaces, abc, sinitNeedsActivation, sinitVariables, externalTypes);
        ParsedSymbol s = lexer.lex();
        if (s.type != SymbolType.EOF) {
            errors.add(new SimpleParseException("Parsing finished before end of the file", lexer.yyline(), s.position));
        }
    }

    @Override
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors,
            List<Path> externalTypes,
            Map<Integer, Integer> referenceToExternalTypeIndex,
            Map<Integer, List<Integer>> externalTypeIndexToReference,
            LinkHandler linkHandler,
            Map<Integer, Path> referenceToExternalTraitKey,
            Map<Path, List<Integer>> externalTraitKeyToReference,
            Map<Integer, Path> separatorPosToType,
            Map<Integer, Boolean> separatorIsStatic,
            Map<Path, List<Variable>> localTypeTraits,
            Map<Integer, Path> definitionToType,
            Map<Integer, Path> definitionToCallType,
            Integer caretPosition,
            List<Variable> variableSuggestions
    ) throws SimpleParseException, IOException, InterruptedException {
        List<List<NamespaceItem>> allOpenedNamespaces = new ArrayList<>();
        Reference<Boolean> sinitNeedsActivation = new Reference<>(false);
        List<VariableOrScope> vars = new ArrayList<>();
        List<DottedChain> importedClasses = new ArrayList<>();
        List<NamespaceItem> openedNamespaces = new ArrayList<>();
        for (String name : abc.getSwf().getAbcIndex().getPackageObjects(DottedChain.TOPLEVEL)) {
            externalTypes.add(new Path(name));
        }
        externalTypes.add(new Path("__AS3__", "vec", "Vector"));
        try {
            scriptTraitsFromString(errors, importedClasses, openedNamespaces, allOpenedNamespaces, str, abc, sinitNeedsActivation, vars, externalTypes);
        } catch (AVM2ParseException ex) {
            //Logger.getLogger(ActionScript3SimpleParser.class.getName()).log(Level.SEVERE, null, ex);
            throw new SimpleParseException(str, ex.line, ex.position);
        } catch (CompilationException ex) {
            //Logger.getLogger(ActionScript3SimpleParser.class.getName()).log(Level.SEVERE, null, ex);
            throw new SimpleParseException(str, ex.line);
        }
        SimpleParser.parseVariablesList(vars, definitionPosToReferences, referenceToDefinition, errors, true, externalTypes, referenceToExternalTypeIndex, externalTypeIndexToReference, linkHandler, referenceToExternalTraitKey, externalTraitKeyToReference, separatorPosToType, separatorIsStatic, localTypeTraits, definitionToType, definitionToCallType, caretPosition, variableSuggestions);
    }

    /**
     * Constructor.
     *
     * @param abc ABC
     */
    public ActionScript3SimpleParser(ABC abc) {
        this.abc = abc;

    }
}
