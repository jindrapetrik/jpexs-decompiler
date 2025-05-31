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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.simpleparser.CatchScope;
import com.jpexs.decompiler.flash.simpleparser.ClassScope;
import com.jpexs.decompiler.flash.simpleparser.FunctionScope;
import com.jpexs.decompiler.flash.simpleparser.MethodScope;
import com.jpexs.decompiler.flash.simpleparser.SimpleParseException;
import com.jpexs.decompiler.flash.simpleparser.SimpleParser;
import com.jpexs.decompiler.flash.simpleparser.TraitVarConstValueScope;
import com.jpexs.decompiler.flash.simpleparser.Type;
import com.jpexs.decompiler.flash.simpleparser.Variable;
import com.jpexs.decompiler.flash.simpleparser.VariableOrScope;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ActionScript 1/2 parser.
 *
 * @author JPEXS
 */
public class ActionScript2SimpleParser implements SimpleParser {

    /**
     * Swf version
     */
    private final int swfVersion;

    /**
     * Constructor
     *
     * @param swf Swf
     */
    public ActionScript2SimpleParser(SWF swf) {
        this.swfVersion = swf.version;
    }

    private final boolean debugMode = false;

    private void commands(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        if (debugMode) {
            System.out.println("commands:");
        }
        while (command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval)) {
            //empty
        }
        if (debugMode) {
            System.out.println("/commands");
        }
    }

    private String type(List<SimpleParseException> errors, boolean definition, List<VariableOrScope> variables) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol s = lex();
        if (!expectedIdentifier(errors, s, lexer.yyline())) {
            return null;
        }
        ParsedSymbol lastIdent = s;
        Variable vret = new Variable(false, s.value.toString(), s.position);
        variables.add(vret);
        String ret = s.value.toString();
        s = lex();
        while (s.type == SymbolType.DOT) {
            ret += ".";
            s = lex();
            if (!expectedIdentifier(errors, s, lexer.yyline())) {
                return null;
            }
            lastIdent = s;
            ret += s.value.toString();
            s = lex();
        }
        lexer.pushback(s);
        Type t = new Type(definition, ret, lastIdent.position);
        variables.add(t);
        return ret;
    }

    private boolean expected(List<SimpleParseException> errors, ParsedSymbol symb, int line, Object... expected) throws IOException {
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
            return false;
        }
        return true;
    }

    private ParsedSymbol expectedType(List<SimpleParseException> errors, Object... type) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol symb = lex();
        if (!expected(errors, symb, lexer.yyline(), type)) {
            return null;
        }
        return symb;
    }

    private ParsedSymbol lex() throws IOException, InterruptedException, SimpleParseException, ActionParseException, ActionParseException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
            s = lex();
            if (!expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                break;
            }
        }
        return ret;
    }

    private FunctionScope function(List<SimpleParseException> errors, boolean withBody, String functionName, int functionNamePosition, boolean isMethod, List<VariableOrScope> variables, boolean inTellTarget, Reference<Boolean> hasEval, boolean isStatic) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol s;
        expectedType(errors, SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();
        List<Integer> paramPositions = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            if (expectedIdentifier(errors, s, lexer.yyline())) {
                paramNames.add(s.value.toString());
                paramPositions.add(s.position);
            }
            s = lex();
            if (s.type == SymbolType.COLON) {
                type(errors, false, variables);
                s = lex();
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                if (!expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                    break;
                }
            }
        }
        List<VariableOrScope> subvariables = new ArrayList<>();
        Reference<Boolean> subHasEval = new Reference<>(false);

        if (!isMethod && !functionName.isEmpty()) {
            variables.add(0, new Variable(true, functionName, functionNamePosition));
        }

        for (int i = 0; i < paramNames.size(); i++) {
            subvariables.add(new Variable(true, paramNames.get(i), paramPositions.get(i)));
        }

        if (withBody) {
            expectedType(errors, SymbolType.CURLY_OPEN);
            commands(errors, true, isMethod, 0, inTellTarget, subvariables, subHasEval);
            expectedType(errors, SymbolType.CURLY_CLOSE);
        }

        if (subHasEval.getVal()) {
            hasEval.setVal(true);
        }

        if (isMethod) {
            return new MethodScope(subvariables, isStatic);
        }
        return new FunctionScope(subvariables, isStatic);
    }

    private boolean traits(List<SimpleParseException> errors, boolean isInterface, String className, List<VariableOrScope> variables, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {

        ParsedSymbol s;

        List<VariableOrScope> traitVariables = new ArrayList<>();

        looptrait:
        while (true) {
            boolean isStatic = false;
            s = lex();
            int staticPos = -1;
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE)) {
                if (s.type == SymbolType.STATIC) {
                    isStatic = true;
                    staticPos = s.position;
                }
                s = lex();
            }
            switch (s.type) {
                case FUNCTION:
                    s = lex();

                    if (s.type == SymbolType.SET) {
                        s = lex();
                    } else if (s.type == SymbolType.GET) {
                        s = lex();
                    }

                    if (!expectedIdentifier(errors, s, lexer.yyline())) {
                        break;
                    }                    
                    
                    String simpleClassName = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
                    
                    if (!simpleClassName.equals(s.value.toString())) { //not constructor
                        traitVariables.add(new Variable(true, isStatic ? className + "." + s.value.toString() : "this." + s.value.toString(), s.position, isStatic));                    
                    } else {
                        if (isStatic) {
                            errors.add(new SimpleParseException("Constructor cannot be static", lexer.yyline(), staticPos));                            
                        }
                    }
                    
                    if (!isInterface) {
                        variables.add(function(errors, !isInterface, isStatic ? className + "." + s.value.toString() : "this." + s.value.toString(), isStatic ? -1 : s.position, true, traitVariables, inTellTarget, hasEval, isStatic));
                    }                    
                    break;
                case VAR:
                    s = lex();
                    if (!expectedIdentifier(errors, s, lexer.yyline())) {
                        break;
                    }
                    traitVariables.add(new Variable(true, isStatic ? className + "." + s.value.toString() : "this." + s.value.toString(), s.position, isStatic));                    
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(errors, false, variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        List<VariableOrScope> subVariables = new ArrayList<>();
                        expression(errors, false, false, false, true, subVariables, false, hasEval);
                        variables.add(new TraitVarConstValueScope(subVariables, isStatic));
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

        variables.addAll(0, traitVariables);

        return true;
    }

    private boolean expressionCommands(List<SimpleParseException> errors, ParsedSymbol s, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        if (debugMode) {
            System.out.println("expressionCommands:");
        }
        boolean ret;

        switch (s.type) {
            case DUPLICATEMOVIECLIP:
            case FSCOMMAND:
            case FSCOMMAND2:
            case SET:
            case TRACE:
            case GETURL:
            case GOTOANDSTOP:
            case GOTOANDPLAY:
            case NEXTFRAME:
            case PLAY:
            case PREVFRAME:
            case STOP:
            case STOPALLSOUNDS:
            case TOGGLEHIGHQUALITY:
            case STOPDRAG:
            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
            case REMOVEMOVIECLIP:
            case STARTDRAG:
            case CALL:
            case GETVERSION:
            case MBORD:
            case MBCHR:
            case MBLENGTH:
            case MBSUBSTRING:
            case SUBSTR:
            case LENGTH:
            case RANDOM:
            case INT:
            case NUMBER_OP:
            case STRING_OP:
            case ORD:
            case CHR:
            case GETTIMER:
            case TARGETPATH:
                variables.add(new Variable(false, (String) s.value, s.position));
                break;
        }

        switch (s.type) {
            case DUPLICATEMOVIECLIP:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case FSCOMMAND:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                if (s.isType(SymbolType.COMMA)) {
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                } else {
                    lexer.pushback(s);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case FSCOMMAND2:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                while (s.isType(SymbolType.COMMA)) {
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                }
                expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case SET:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                hasEval.setVal(true); //FlashPro does this (using definelocal for funcs) only for eval func, but we will also use set since it is generated by obfuscated identifiers
                ret = true;
                break;
            case TRACE:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;

            case GETURL:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                if (s.type == SymbolType.COMMA) {
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        s = lex();
                        expected(errors, s, lexer.yyline(), SymbolType.STRING);
                        if (s.value.equals("GET")) {
                            //empty
                        } else if (s.value.equals("POST")) {
                            //empty
                        } else {
                            errors.add(new SimpleParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline(), s.position));
                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case GOTOANDSTOP:
            case GOTOANDPLAY:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                } else {
                    lexer.pushback(s);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case NEXTFRAME:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case PLAY:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case PREVFRAME:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case STOP:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case STOPALLSOUNDS:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;

            case STOPDRAG:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);

                s = lex();
                expected(errors, s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                if (s.type == SymbolType.COMMA) {
                    s = lex();
                    expected(errors, s, lexer.yyline(), SymbolType.STRING);
                    if (s.value.equals("POST")) {
                        //empty
                    } else if (s.value.equals("GET")) {
                        //empty
                    } else {
                        errors.add(new SimpleParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline(), s.position));
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case REMOVEMOVIECLIP:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case STARTDRAG:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                                } else {
                                    lexer.pushback(s);
                                }
                            } else {
                                lexer.pushback(s);
                            }
                        } else {
                            lexer.pushback(s);

                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case CALL:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case GETVERSION:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBORD:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBCHR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBLENGTH:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBSUBSTRING:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case SUBSTR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.COMMA);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case LENGTH:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case RANDOM:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case INT:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case NUMBER_OP:
                ParsedSymbol sopn = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    Variable vi = new Variable(false, sopn.value.toString(), sopn.position);
                    variables.add(vi);
                    ret = true;
                } else {
                    expected(errors, s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    ret = true;
                }
                break;
            case STRING_OP:
                ParsedSymbol sop = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    Variable vi2 = new Variable(false, sop.value.toString(), sop.position);
                    variables.add(vi2);
                    ret = true;
                } else {
                    expected(errors, s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    expectedType(errors, SymbolType.PARENT_CLOSE);
                    ret = true;
                }
                break;
            case ORD:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case CHR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case GETTIMER:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case TARGETPATH:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            default:
                return false;
        }
        if (debugMode) {
            System.out.println("/expressionCommands");
        }
        return ret;
    }

    private boolean isIdentifier(ParsedSymbol s, Object... exceptions) {
        for (Object ex : exceptions) {
            if (s.isType(ex)) {
                return true;
            }
        }
        return s.isType(SymbolType.IDENTIFIER,
                SymbolType.TRUE, SymbolType.FALSE, SymbolGroup.GLOBALCONST,
                SymbolType.GET, SymbolType.SET,
                SymbolType.EACH, SymbolGroup.GLOBALFUNC,
                SymbolType.NUMBER_OP, SymbolType.STRING_OP);
    }

    private boolean expectedIdentifier(List<SimpleParseException> errors, ParsedSymbol s, int line, Object... exceptions) throws IOException {
        for (Object ex : exceptions) {
            if (s.isType(ex)) {
                return true;
            }
        }
        if (!isIdentifier(s)) {
            errors.add(new SimpleParseException(SymbolType.IDENTIFIER + " expected but " + s.type + " found", line, s.position));
            return false;
        }
        return true;
    }

    private boolean command(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        LexBufferer buf = new LexBufferer();
        lexer.addListener(buf);
        if (debugMode) {
            System.out.println("command:");
        }
        boolean ret = false;
        ParsedSymbol s = lex();
        if (s.type == SymbolType.EOF) {
            return false;
        }
        if (s.group == SymbolGroup.GLOBALFUNC) {
            ParsedSymbol s2 = lex();
            if (s2.type != SymbolType.PARENT_OPEN) {
                lexer.removeListener(buf);
                buf.pushAllBack(lexer);

                ret = expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
                    lexer.pushback(s);
                }
                return ret;
            } else {
                lexer.pushback(s2);
            }
        }

        switch (s.type) {
            case WITH:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                expectedType(errors, SymbolType.CURLY_OPEN);
                commands(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(errors, SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case DELETE:
                expression(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                ret = true;
                break;
            case TELLTARGET:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                expectedType(errors, SymbolType.CURLY_OPEN);
                commands(errors, inFunction, inMethod, forinlevel, true, variables, hasEval);
                expectedType(errors, SymbolType.CURLY_CLOSE);
                ret = true;
                break;

            case IFFRAMELOADED:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                expectedType(errors, SymbolType.CURLY_OPEN);
                commands(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(errors, SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case CLASS:
                String className = type(errors, true, variables);
                if (className != null) {
                    s = lex();
                    if (s.type == SymbolType.EXTENDS) {
                        type(errors, false, variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.IMPLEMENTS) {
                        do {
                            type(errors, false, variables);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    variables.add(new Variable(true, "this", s.position));
                    List<VariableOrScope> subVariables = new ArrayList<>();
                    traits(errors, false, className, subVariables, inTellTarget, hasEval);
                    ClassScope cs = new ClassScope(subVariables);
                    variables.add(cs);

                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    ret = true;
                }
                break;
            case INTERFACE:
                String interfaceName = type(errors, true, variables);
                if (interfaceName != null) {
                    s = lex();

                    if (s.type == SymbolType.EXTENDS) {
                        do {
                            type(errors, false, variables);
                            s = lex();
                        } while (s.type == SymbolType.COMMA);
                    }
                    expected(errors, s, lexer.yyline(), SymbolType.CURLY_OPEN);
                    traits(errors, true, interfaceName, variables, inTellTarget, hasEval);
                    expectedType(errors, SymbolType.CURLY_CLOSE);
                    ret = true;
                }
                break;
            case FUNCTION:
                s = lexer.lex();
                if (expectedIdentifier(errors, s, lexer.yyline())) {
                    variables.add(function(errors, true, s.value.toString(), s.position, false, variables, inTellTarget, hasEval, false));
                }
                break;
            case VAR:
                s = lex();
                if (expectedIdentifier(errors, s, lexer.yyline())) {
                    String varIdentifier = s.value.toString();
                    int varPosition = s.position;
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(errors, false, variables);
                        s = lex();
                        //TODO: handle value type
                    }

                    if (s.type == SymbolType.ASSIGN) {
                        expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                        Variable vret = new Variable(true, varIdentifier, varPosition);
                        variables.add(vret);
                    } else {
                        Variable vret = new Variable(true, varIdentifier, varPosition);
                        variables.add(vret);
                        lexer.pushback(s);
                    }
                    ret = true;
                }
                break;
            case CURLY_OPEN:
                commands(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(errors, SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                expression(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                ret = true;
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    call(errors, inFunction, inMethod, inTellTarget, variables, hasEval);
                    Variable supItem = new Variable(false, s.value.toString(), s.position);
                    variables.add(supItem);
                    ret = true;
                } else { //no constructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                s = lex();
                if (s.type == SymbolType.ELSE) {
                    command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                } else {
                    lexer.pushback(s);
                }
                ret = true;
                break;
            case WHILE:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                ret = true;
                break;
            case DO:
                command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(errors, SymbolType.WHILE);
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case FOR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                String objIdent;
                Variable item;
                boolean define = false;
                if (s.type == SymbolType.VAR || isIdentifier(s)) {
                    ParsedSymbol s2 = null;
                    ParsedSymbol ssel = s;
                    if (s.type == SymbolType.VAR) {
                        s2 = lex();
                        ssel = s2;
                        define = true;
                    }

                    if (isIdentifier(ssel)) {
                        objIdent = ssel.value.toString();

                        ParsedSymbol s3 = lex();
                        if (s3.type == SymbolType.IN) {
                            item = new Variable(define, objIdent, ssel.position);
                            variables.add(item);

                            expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
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
                if (!forin) {
                    command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    expectedType(errors, SymbolType.SEMICOLON);
                    command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                command(errors, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, inTellTarget, variables, hasEval);
                ret = true;
                break;
            case SWITCH:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                expectedType(errors, SymbolType.CURLY_OPEN);
                s = lex();
                while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                    //List<GraphTargetItem> caseExprs; = new ArrayList<>();
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        if (s.type != SymbolType.DEFAULT) {
                            expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                        }
                        expectedType(errors, SymbolType.COLON);
                        s = lex();
                    }
                    lexer.pushback(s);
                    commands(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                    s = lex();
                }
                expected(errors, s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case BREAK:
                ret = true;
                break;
            case CONTINUE:
                ret = true;
                break;
            case RETURN:
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                ret = true;
                break;
            case TRY:
                command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                s = lex();
                boolean found = false;
                while (s.type == SymbolType.CATCH) {
                    expectedType(errors, SymbolType.PARENT_OPEN);
                    ParsedSymbol si = lex();
                    if (expectedIdentifier(errors, si, lexer.yyline(), SymbolType.STRING)) {
                        s = lex();
                        if (s.type == SymbolType.COLON) {
                            type(errors, false, variables);
                        } else {
                            lexer.pushback(s);
                        }
                        expectedType(errors, SymbolType.PARENT_CLOSE);

                        List<VariableOrScope> subvariables = new ArrayList<>();

                        command(errors, inFunction, inMethod, forinlevel, inTellTarget, subvariables, hasEval);

                        variables.add(new CatchScope(new Variable(true, (String) si.value, si.position), subvariables));
                    }
                    s = lex();
                    found = true;
                }
                if (s.type == SymbolType.FINALLY) {
                    command(errors, inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
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
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                ret = true;
                break;
            case SEMICOLON: //empty command
                if (debugMode) {
                    System.out.println("/command");
                }
                return true;
            case DIRECTIVE:
                switch ((String) s.value) {
                    case "strict":
                        ret = true;
                        break;
                    default:
                        errors.add(new SimpleParseException("Unknown directive: #" + s.value, lexer.yyline(), s.position));
                }
                break;
            default:
                lexer.pushback(s);
                ret = expression(errors, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (!ret) {  //can be popped expression
            buf.pushAllBack(lexer);
            ret = expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;
    }

    private boolean expression(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        ParsedSymbol symb;
        do {
            boolean prim = expressionPrimary(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, true, hasEval);
            if (!prim) {
                return false;
            }
            expression1(errors, prim, GraphTargetItem.NOPRECEDENCE, inFunction, inMethod, inTellTarget, allowRemainder, variables, hasEval);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
        if (debugMode) {
            System.out.println("/expression");
        }
        return true;
    }

    private ParsedSymbol peekLex() throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol lookahead = lex();
        lexer.pushback(lookahead);
        return lookahead;
    }

    private static final String[] operatorIdentifiers = new String[]{"add", "eq", "ne", "lt", "ge", "gt", "le"};

    private boolean isBinaryOperator(ParsedSymbol s) {
        if (s.type == SymbolType.IDENTIFIER && Arrays.asList(operatorIdentifiers).contains(s.value.toString())) {
            return true;
        }
        return s.type.isBinary();
    }

    private int getSymbPrecedence(ParsedSymbol s) {
        if (s.type == SymbolType.IDENTIFIER && Arrays.asList(operatorIdentifiers).contains(s.value.toString())) {
            switch (s.value.toString()) {
                case "add":
                    return GraphTargetItem.PRECEDENCE_ADDITIVE;
                case "eq":
                case "ne":
                    return GraphTargetItem.PRECEDENCE_EQUALITY;
                case "lt":
                case "ge":
                case "gt":
                case "le":
                    return GraphTargetItem.PRECEDENCE_RELATIONAL;
            }
        }
        return s.type.getPrecedence();
    }

    private boolean expression1(List<SimpleParseException> errors, boolean lhs, int min_precedence, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol op;
        boolean rhs;
        ParsedSymbol lookahead = peekLex();
        if (debugMode) {
            System.out.println("expression1:");
        }
        //Note: algorithm from http://en.wikipedia.org/wiki/Operator-precedence_parser
        //with relation operators reversed as we have precedence in reverse order
        while (isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) <= /* >= on wiki */ min_precedence) {
            op = lookahead;
            lex();

            //Note: Handle ternar operator as Binary
            //http://stackoverflow.com/questions/13681293/how-can-i-incorporate-ternary-operators-into-a-precedence-climbing-algorithm
            if (op.type == SymbolType.TERNAR) {
                if (debugMode) {
                    System.out.println("ternar-middle:");
                }
                expression(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                expectedType(errors, SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, true, hasEval);
            if (rhs == false) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekLex();
            while ((isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) < /* > on wiki */ getSymbPrecedence(op))
                    || (lookahead.type.isRightAssociative() && getSymbPrecedence(lookahead) == getSymbPrecedence(op))) {
                rhs = expression1(errors, rhs, getSymbPrecedence(lookahead), inFunction, inMethod, inTellTarget, allowRemainder, variables, hasEval);
                lookahead = peekLex();
            }

            switch (op.type) {
                case TERNAR:
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
                case FULLAND:
                case FULLOR:
                case MINUS:
                case MULTIPLY:
                case PLUS:
                case XOR:
                case INSTANCEOF:
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
                    lhs = true;
                    break;
                case IDENTIFIER:
                    switch (op.value.toString()) {
                        case "add":
                        case "eq":
                        case "ne":
                        case "lt":
                        case "ge":
                        case "gt":
                        case "le":
                            lhs = true;
                            break;
                    }
                    break;
            }
        }

        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private int brackets(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                    expected(errors, s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                }
            }
        } else {
            lexer.pushback(s);
            return -1;
        }
        return arrCnt;
    }

    private boolean handleVariable(List<SimpleParseException> errors, ParsedSymbol s, boolean ret, List<VariableOrScope> variables, Reference<Boolean> allowMemberOrCall, boolean inFunction, boolean inMethod, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        if (s.value.equals("not")) {
            expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
            ret = true;
        } else {
            String varName = s.value.toString();

            Variable vret = new Variable(false, varName, s.position);
            variables.add(vret);
            allowMemberOrCall.setVal(true);

            if (varName.equals("this")) {
                ParsedSymbol s2 = lex();
                if (s2.type == SymbolType.DOT) {
                    ParsedSymbol s3 = lex();
                    if (isIdentifier(s3)) {
                        Variable thisVar = new Variable(false, "this." + s3.value.toString(), s3.position);
                        variables.add(thisVar);
                    } else {
                        lexer.pushback(s3);
                        lexer.pushback(s2);
                    }
                } else {
                    lexer.pushback(s2);
                }
            }
            ParsedSymbol ss = lex();
            String fullName = varName;
            while (ss.type == SymbolType.DOT) {
                ParsedSymbol si = lex();
                if (!isIdentifier(si)) {
                    lexer.pushback(si);
                    break;
                }
                fullName += ".";
                fullName += si.value.toString();
                Variable v = new Variable(false, fullName, si.position);
                variables.add(v);
                ss = lex();
            }
            lexer.pushback(ss);

            ret = true;
        }
        return ret;
    }

    private boolean expressionPrimary(List<SimpleParseException> errors, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, boolean allowCall, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean allowMemberOrCall = false;
        boolean ret = false;
        ParsedSymbol s = lex();

        switch (s.type) {
            case PREPROCESSOR:
                expectedType(errors, SymbolType.PARENT_OPEN);
                switch ("" + s.value) {
                    //AS 1/2:
                    //AS2:
                    case "constant":
                        s = lexer.lex();
                        expected(errors, s, lexer.yyline(), SymbolType.INTEGER);
                        ret = true;
                        break;
                    case "enumerate":
                        expression(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    //Both ASs
                    case "dup":
                        expression(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    case "push":
                        expression(errors, inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    case "pop":
                        ret = true;
                        break;
                    case "strict":
                        s = lexer.lex();
                        expected(errors, s, lexer.yyline(), SymbolType.INTEGER);
                        ret = true;
                        break;
                    case "goto":
                        s = lexer.lex();
                        expectedIdentifier(errors, s, lexer.yyline());
                        ret = true;
                        //errors.add(new ActionVariableParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline()));
                        break;
                    default:
                        errors.add(new SimpleParseException("Unknown preprocessor instruction: §§" + s.value, lexer.yyline(), s.position));

                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                break;
            case NEGATE:
                versionRequired(errors, s, 5);
                expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = true;

                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = true;

                } else {
                    lexer.pushback(s);
                    expressionPrimary(errors, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                    ret = true;
                }
                break;
            case TYPEOF:
                expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                allowMemberOrCall = true;
                break;
            case TRUE:
                ret = true;
                allowMemberOrCall = true;
                break;
            case NULL:
                ret = true;
                allowMemberOrCall = true;
                break;
            case UNDEFINED:
                ret = true;
                allowMemberOrCall = true;
                break;
            case FALSE:
                ret = true;
                allowMemberOrCall = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expectedIdentifier(errors, s, lexer.yyline());
                    expectedType(errors, SymbolType.COLON);
                    expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
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
                brackets(errors, inFunction, inMethod, inTellTarget, variables, hasEval);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FUNCTION:
                s = lex();
                String fname = "";
                int fnamePos = -1;
                if (isIdentifier(s)) {
                    fname = s.value.toString();
                    fnamePos = s.position;
                } else {
                    lexer.pushback(s);
                }
                variables.add(function(errors, true, fname, fnamePos, false, variables, inTellTarget, hasEval, false));
                ret = true;
                allowMemberOrCall = true;
                break;
            case STRING:
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEWLINE:
                ret = true;
                allowMemberOrCall = true;
                break;
            case INTEGER:
            case DOUBLE:
                ret = true;
                allowMemberOrCall = true;
                break;
            case DELETE:
                expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                break;
            case NOT:
                expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                break;
            case PARENT_OPEN:
                boolean pexpr = expression(errors, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                if (!pexpr) {
                    errors.add(new SimpleParseException("Expression expected", lexer.yyline(), s.position));
                }
                expectedType(errors, SymbolType.PARENT_CLOSE);
                allowMemberOrCall = true;
                ret = true;
                break;
            case NEW:
                ParsedSymbol s1 = lex();
                if (s1.type == SymbolType.NUMBER_OP || s1.type == SymbolType.STRING_OP) {
                    ParsedSymbol s2 = lex();
                    if (s2.type == SymbolType.PARENT_OPEN) {
                        lexer.pushback(s2);
                    } else {
                        lexer.pushback(s2);
                        lexer.pushback(s1);
                        expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                    }
                } else {
                    lexer.pushback(s1);
                    expressionPrimary(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                }
                expectedType(errors, SymbolType.PARENT_OPEN);
                call(errors, inFunction, inMethod, inTellTarget, variables, hasEval);
                ret = true;
                allowMemberOrCall = true;

                break;
            case EVAL:
                expectedType(errors, SymbolType.PARENT_OPEN);
                expression(errors, inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(errors, SymbolType.PARENT_CLOSE);
                hasEval.setVal(true);
                allowMemberOrCall = true;
                ret = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                Reference<Boolean> allowMemberOrCallRef = new Reference<>(allowMemberOrCall);
                ret = handleVariable(errors, s, ret, variables, allowMemberOrCallRef, inFunction, inMethod, inTellTarget, hasEval);
                allowMemberOrCall = allowMemberOrCallRef.getVal();
                break;
            default:

                boolean isGlobalFuncVar = false;
                if (s.group == SymbolGroup.GLOBALFUNC) {
                    ParsedSymbol s2 = peekLex();
                    if (s2.type != SymbolType.PARENT_OPEN) {
                        Reference<Boolean> allowMemberOrCallRef2 = new Reference<>(allowMemberOrCall);
                        ret = handleVariable(errors, s, ret, variables, allowMemberOrCallRef2, inFunction, inMethod, inTellTarget, hasEval);
                        allowMemberOrCall = allowMemberOrCallRef2.getVal();
                        isGlobalFuncVar = true;
                    }
                }

                if (!isGlobalFuncVar) {
                    boolean excmd = expressionCommands(errors, s, inFunction, inMethod, inTellTarget, variables, hasEval);
                    if (excmd) {
                        ret = excmd;
                        allowMemberOrCall = true;
                        break;
                    }
                    lexer.pushback(s);
                }
        }

        if (allowMemberOrCall && ret) {
            ret = memberOrCall(errors, ret, inFunction, inMethod, inTellTarget, variables, allowCall, hasEval);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private boolean memberOrCall(List<SimpleParseException> errors, boolean ret, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, boolean allowCall, Reference<Boolean> hasEval) throws IOException, InterruptedException, SimpleParseException, ActionParseException {
        ParsedSymbol op = lex();
        while (op.isType(SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.DOT)) {
            if (op.type == SymbolType.PARENT_OPEN) {
                if (!allowCall) {
                    break;
                }
                call(errors, inFunction, inMethod, inTellTarget, variables, hasEval);
                ret = true;
            }
            if (op.type == SymbolType.BRACKET_OPEN) {
                expression(errors, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                expectedType(errors, SymbolType.BRACKET_CLOSE);
                ret = true;
            }
            if (op.type == SymbolType.DOT) {
                ParsedSymbol s = lex();
                expectedIdentifier(errors, s, lexer.yyline(), SymbolType.THIS, SymbolType.SUPER);

                ret = true;
            }
            op = lex();
        }

        switch (op.type) {
            case INCREMENT: //postincrement
                ret = true;
                op = lex();
                break;
            case DECREMENT: //postdecrement
                ret = true;
                op = lex();
                break;
        }

        lexer.pushback(op);
        return ret;
    }

    private ActionScriptLexer lexer = null;

    @Override
    public void parse(
            String str,
            Map<Integer, List<Integer>> definitionPosToReferences,
            Map<Integer, Integer> referenceToDefinition,
            List<SimpleParseException> errors
    ) throws SimpleParseException, IOException, InterruptedException {

        List<VariableOrScope> vars = new ArrayList<>();
        try {
            lexer = new ActionScriptLexer(new StringReader(str));
            if (swfVersion >= ActionScriptLexer.SWF_VERSION_CASE_SENSITIVE) {
                lexer.setCaseSensitiveIdentifiers(true);
            }

            ParsedSymbol symb = lexer.lex();
            boolean inOnHandler = false;

            if (symb.type == SymbolType.IDENTIFIER && ("on".equals(symb.value) || "onClipEvent".equals(symb.value))) {
                expectedType(errors, SymbolType.PARENT_OPEN);
                symb = lexer.lex();
                boolean condEmpty = true;
                while (symb.type == SymbolType.IDENTIFIER) {
                    condEmpty = false;
                    switch ((String) symb.value) {
                        case "press":
                            break;
                        case "release":
                            break;
                        case "releaseOutside":
                            break;
                        case "rollOver":
                            break;
                        case "rollOut":
                            break;
                        case "dragOut":
                            break;
                        case "dragOver":
                            break;
                        case "keyPress":
                            symb = lexer.lex();
                            expected(errors, symb, lexer.yyline(), SymbolType.STRING);
                            Integer key = CLIPACTIONRECORD.stringToKey((String) symb.value);
                            if (key == null) {
                                errors.add(new SimpleParseException("Invalid key", lexer.yyline(), symb.position));
                            }
                            break;
                        case "keyUp":
                            break;
                        case "keyDown":
                            break;
                        case "mouseUp":
                            break;
                        case "mouseDown":
                            break;
                        case "mouseMove":
                            break;
                        case "unload":
                            break;
                        case "enterFrame":
                            break;
                        case "load":
                            break;
                        case "data":
                            break;
                        default:
                            errors.add(new SimpleParseException("Unrecognized event type", lexer.yyline(), symb.position));
                    }
                    symb = lexer.lex();
                    if (symb.type == SymbolType.PARENT_CLOSE) {
                        break;
                    }
                    expected(errors, symb, lexer.yyline(), SymbolType.COMMA);
                    symb = lexer.lex();
                }
                expected(errors, symb, lexer.yyline(), SymbolType.PARENT_CLOSE);
                if (condEmpty) {
                    errors.add(new SimpleParseException("condition must be non empty", lexer.yyline(), symb.position));
                }
                expectedType(errors, SymbolType.CURLY_OPEN);
                inOnHandler = true;
            } else {
                lexer.pushback(symb);
            }
            Reference<Boolean> hasEval = new Reference<>(false);
            commands(errors, false, false, 0, false, vars, hasEval);

            if (inOnHandler) {
                expectedType(errors, SymbolType.CURLY_CLOSE);
            }
            if (lexer.lex().type != SymbolType.EOF) {
                errors.add(new SimpleParseException("Parsing finished before end of the file", lexer.yyline(), lexer.yychar()));
            }
        } catch (ActionParseException ex) {
            errors.add(new SimpleParseException(ex.getMessage(), ex.line, ex.position));
        }
        SimpleParser.parseVariablesList(new ArrayList<>(), vars, definitionPosToReferences, referenceToDefinition, errors, false);
    }

    private void versionRequired(List<SimpleParseException> errors, ParsedSymbol s, int min) throws SimpleParseException {
        versionRequired(errors, s.value.toString(), min, Integer.MAX_VALUE, s.position);
    }

    private void versionRequired(List<SimpleParseException> errors, String type, int min, int max, long position) throws SimpleParseException {
        if (min == max && swfVersion != min) {
            errors.add(new SimpleParseException(type + " requires SWF version " + min, lexer.yyline(), position));
        }
        if (swfVersion < min) {
            errors.add(new SimpleParseException(type + " requires at least SWF version " + min, lexer.yyline(), position));
        }
        if (swfVersion > max) {
            errors.add(new SimpleParseException(type + " requires SWF version lower than " + max, lexer.yyline(), position));
        }
    }
}
