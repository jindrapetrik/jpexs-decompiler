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
package com.jpexs.decompiler.flash.action.parser.script.variables;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.LexBufferer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolGroup;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ActionScript 1/2 parser.
 *
 * @author JPEXS
 */
public class ActionScript2VariableParser {

    private static final GraphTargetDialect DIALECT = ActionGraphTargetDialect.INSTANCE;

    /**
     * Builtin classes that can be casted to
     */
    public static final List<String> BUILTIN_CASTS = Arrays.asList(new String[]{
        "flash.display.BitmapData",
        "flash.external.ExternalInterface",
        "flash.filters.BevelFilter",
        "flash.filters.BitmapFilter",
        "flash.filters.BlurFilter",
        "flash.filters.ColorMatrixFilter",
        "flash.filters.ConvolutionFilter",
        "flash.filters.DisplacementMapFilter",
        "flash.filters.DropShadowFilter",
        "flash.filters.GlowFilter",
        "flash.filters.GradientBevelFilter",
        "flash.filters.GradientGlowFilter",
        "flash.geom.ColorTransform",
        "flash.geom.Matrix",
        "flash.geom.Point",
        "flash.geom.Rectangle",
        "flash.geom.Transform",
        "flash.net.FileReference",
        "flash.net.FileReferenceList",
        "flash.text.TextRenderer",
        "Button",
        "Camera",
        "Color",
        "ContextMenu",
        "ContextMenuItem",
        "CustomActions",
        "Error",
        "System.IME",
        "LoadVars",
        "LocalConnection",
        "Microphone",
        "MovieClip",
        "MovieClipLoader",
        "NetConnection",
        "NetStream",
        "PrintJob",
        "System.security",
        "SharedObject",
        "Sound",
        "TextField",
        "TextFormat",
        "TextSnapshot",
        "XML",
        "XMLNode",
        "XMLSocket",
        "XMLUI"
    });

    /**
     * Swf version
     */
    private final int swfVersion;
    /**
     * Swf classes
     */
    private List<String> swfClasses = new ArrayList<>();
    /**
     * Charset
     */
    private String charset;

    /**
     * Constructor
     *
     * @param swf Swf
     */
    public ActionScript2VariableParser(SWF swf) {
        this.swfVersion = swf.version;
        this.charset = swf.getCharset();
        parseSwfClasses(swf);
    }

    private long uniqLast = 0;

    private final boolean debugMode = false;

    /**
     * Parse SWF classes
     *
     * @param swf SWF
     */
    private void parseSwfClasses(SWF swf) {
        Map<String, ASMSource> asms = swf.getASMs(false);
        for (ASMSource s : asms.values()) {
            if (s instanceof DoInitActionTag) {
                String exportName = swf.getExportName(((DoInitActionTag) s).spriteId);

                if (exportName != null) {
                    final String PREFIX = "__Packages.";
                    if (exportName.startsWith(PREFIX)) {
                        String className = exportName.substring(PREFIX.length());
                        swfClasses.add(className);
                    }
                }
            }
        }
    }

    private String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    private void commands(boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        //List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        while (command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval)) {
            //empty
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        //return ret;
    }

    private boolean type(List<VariableOrScope> variables) throws IOException, ActionParseException, InterruptedException {
        //GraphTargetItem ret;

        ParsedSymbol s = lex();
        expectedIdentifier(s, lexer.yyline());
        Variable vret = new Variable(false, s.value.toString(), s.position);
        variables.add(vret);
        //ret = vret;
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expectedIdentifier(s, lexer.yyline());
            //ret = new GetMemberActionItem(null, null, ret, pushConst(s.value.toString()));
            s = lex();
        }
        lexer.pushback(s);
        //return ret;
        return true;
    }

    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, ActionParseException {
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
            throw new ActionParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, ActionParseException, InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            //ret.add(;
            expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private void function(boolean withBody, String functionName, boolean isMethod, List<VariableOrScope> variables, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol s;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();
        List<Integer> paramPositions = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            expectedIdentifier(s, lexer.yyline());
            paramNames.add(s.value.toString());
            paramPositions.add(s.position);
            s = lex();
            if (s.type == SymbolType.COLON) {
                type(variables);
                s = lex();
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }
        List<GraphTargetItem> body = null;
        List<VariableOrScope> subvariables = new ArrayList<>();
        List<VariableOrScope> subfunctions = new ArrayList<>();
        Reference<Boolean> subHasEval = new Reference<>(false);

        for (int i = 0; i < paramNames.size(); i++) {
            subvariables.add(new Variable(true, paramNames.get(i), paramPositions.get(i)));
        }

        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);
            //body = ;
            commands(true, isMethod, 0, inTellTarget, subvariables, subHasEval);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        if (subHasEval.getVal()) {
            hasEval.setVal(true);
        }

        //FunctionActionItem retf = new FunctionActionItem(null, null, functionName, paramNames, new HashMap<>() /*?*/, body, constantPool, -1, subvariables, subfunctions, subHasEval.getVal(), paramPositions, null);
        //functions.add(retf);
        variables.add(new FunctionScope(subvariables));

        //return retf;
    }

    private boolean traits(boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<VariableOrScope> variables, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {

        GraphTargetItem ret = null;

        ParsedSymbol s;
        //List<MyEntry<GraphTargetItem, GraphTargetItem>> traits = new ArrayList<>();
        //List<Boolean> traitsStatic = new ArrayList<>();

        /*String classNameStr = "";
        if (nameStr instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) nameStr;
            if (mem.memberName instanceof VariableActionItem) {
                classNameStr = ((VariableActionItem) mem.memberName).getVariableName();
            } else if (mem.memberName instanceof DirectValueActionItem) {
                classNameStr = ((DirectValueActionItem) mem.memberName).toStringNoQuotes(LocalData.empty);
            }
        } else if (nameStr instanceof VariableActionItem) {
            VariableActionItem var = (VariableActionItem) nameStr;
            classNameStr = var.getVariableName();
        }*/
        looptrait:
        while (true) {
            s = lex();
            boolean isGetter = false;
            boolean isSetter = false;
            boolean isStatic = false;
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE)) {
                if (s.type == SymbolType.STATIC) {
                    isStatic = true;
                }
                s = lex();
            }
            switch (s.type) {
                case FUNCTION:
                    s = lex();

                    if (s.type == SymbolType.SET) {
                        isSetter = true;
                        s = lex();
                    } else if (s.type == SymbolType.GET) {
                        isGetter = true;
                        s = lex();
                    }

                    expectedIdentifier(s, lexer.yyline());
                    String fname = s.value.toString();
                    //if (fname.equals(classNameStr)) { //constructor
                    //actually there's no difference, it's instance trait
                    //}
                    if (!isInterface) {
                        if (isStatic) {
                            //FunctionActionItem ft =;
                            function(!isInterface, "", true, variables, inTellTarget, hasEval);
                            /*ft.calculatedFunctionName = pushConst(fname);
                            ft.isSetter = isSetter;
                            ft.isGetter = isGetter;
                            traits.add(new MyEntry<>(ft.calculatedFunctionName, ft));
                            traitsStatic.add(true);*/

                            if (isSetter) {
                                //add return getter automatically
                                /*GraphTargetItem callM = new CallMethodActionItem(null, null, nameStr, pushConst("__get__" + fname), new ArrayList<>());
                                GraphTargetItem retV = new ReturnActionItem(null, null, callM);
                                ft.actions.add(retV);*/
                            }
                        } else {
                            //FunctionActionItem ft = ;
                            function(!isInterface, "", true, variables, inTellTarget, hasEval);
                            /*ft.calculatedFunctionName = pushConst(fname);
                            ft.isSetter = isSetter;
                            ft.isGetter = isGetter;
                            traits.add(new MyEntry<>(ft.calculatedFunctionName, ft));
                            traitsStatic.add(false);
                             */
                            if (isSetter) {
                                //add return getter automatically
                                /*GraphTargetItem thisVar = new VariableActionItem("this", null, false);
                                ft.addVariable((VariableActionItem) thisVar);
                                GraphTargetItem callM = new CallMethodActionItem(null, null, thisVar, pushConst("__get__" + fname), new ArrayList<>());
                                GraphTargetItem retV = new ReturnActionItem(null, null, callM);
                                ft.actions.add(retV);*/
                            }
                        }

                    }
                    break;
                case VAR:
                    s = lex();
                    expectedIdentifier(s, lexer.yyline());
                    //String ident = s.value.toString();
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        //traits.add(new MyEntry<>(pushConst(ident), ;
                        expression(false, false, false, true, variables, false, hasEval);
                        //traitsStatic.add(isStatic);
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

        /*
        if (isInterface) {
            return new InterfaceActionItem(nameStr, implementsStr);
        } else {
            return new ClassActionItem(nameStr, extendsStr, implementsStr, traits, traitsStatic);
        }*/
        return true;
    }

    private boolean expressionCommands(ParsedSymbol s, boolean inFunction, boolean inMethod, boolean inTellTarget, int forinlevel, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expressionCommands:");
        }
        boolean ret = false;
        switch (s.type) {
            case DUPLICATEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem src3 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem tar3 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem dep3 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new CloneSpriteActionItem(null, null, src3, tar3, dep3);
                ret = true;
                break;
            case FSCOMMAND:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem command = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                //GraphTargetItem parameter = null;
                if (s.isType(SymbolType.COMMA)) {
                    //parameter = ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                } else {
                    lexer.pushback(s);
                }
                //ret = new FSCommandActionItem(null, null, command, parameter);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case FSCOMMAND2:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem arg0 = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                //List<GraphTargetItem> args = new ArrayList<>();
                //args.add(arg0);
                s = lex();
                while (s.isType(SymbolType.COMMA)) {
                    //args.add(0,;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                }
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                //ret = new FSCommand2ActionItem(null, null, args);
                ret = true;
                break;
            case SET:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem name1 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem value1 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new SetVariableActionItem(null, null, name1, value1);
                //((SetVariableActionItem) ret).forceUseSet = true;
                hasEval.setVal(true); //FlashPro does this (using definelocal for funcs) only for eval func, but we will also use set since it is generated by obfuscated identifiers
                ret = true;
                break;
            case TRACE:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new TraceActionItem(null, null, (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;

            case GETURL:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem url = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                //int getuMethod = 0;
                //GraphTargetItem target;
                if (s.type == SymbolType.COMMA) {
                    //target = (;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        if (s.value.equals("GET")) {
                            //getuMethod = 1;
                        } else if (s.value.equals("POST")) {
                            //getuMethod = 2;
                        } else {
                            throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                    //target = new DirectValueActionItem(null, null, 0, "", new ArrayList<>());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new GetURL2ActionItem(null, null, url, target, getuMethod);
                ret = true;
                break;
            case GOTOANDSTOP:
            case GOTOANDPLAY:
                SymbolType gtKind = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem gtsFrame = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                int gtsSceneBias = -1;
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    /*if ((gtsFrame instanceof DirectValueActionItem) && (((DirectValueActionItem) gtsFrame).value instanceof Long)) {
                        gtsSceneBias = (int) (long) (Long) ((DirectValueActionItem) gtsFrame).value;
                    } else {
                        throw new ActionParseException("Scene bias must be number", lexer.yyline());
                    }*/

                    //gtsFrame = ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                } else {
                    lexer.pushback(s);
                }
                //ret = new GotoFrame2ActionItem(null, null, gtsFrame, gtsSceneBias != -1, gtKind == SymbolType.GOTOANDPLAY, gtsSceneBias);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case NEXTFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new NextFrameActionItem(null, null);
                ret = true;
                break;
            case PLAY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new PlayActionItem(null, null);
                ret = true;
                break;
            case PREVFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new PrevFrameActionItem(null, null);
                ret = true;
                break;
            case STOP:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new StopActionItem(null, null);
                ret = true;
                break;
            case STOPALLSOUNDS:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new StopAllSoundsActionItem(null, null);
                ret = true;
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new ToggleHighQualityActionItem(null, null);
                ret = true;
                break;

            case STOPDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new StopDragActionItem(null, null);
                ret = true;
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                SymbolType unloadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem unTargetOrNum = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                /*if (unloadType == SymbolType.UNLOADMOVIE) {
                    ret = new UnLoadMovieActionItem(null, null, unTargetOrNum);
                }
                if (unloadType == SymbolType.UNLOADMOVIENUM) {
                    ret = new UnLoadMovieNumActionItem(null, null, unTargetOrNum);
                }*/
                ret = true;
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                SymbolType printType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem printTarget = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem printBBox = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);

                /*
                switch (printType) {
                    case PRINT:
                        ret = new PrintActionItem(null, null, printTarget, printBBox);
                        break;
                    case PRINTNUM:
                        ret = new PrintNumActionItem(null, null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAP:
                        ret = new PrintAsBitmapActionItem(null, null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAPNUM:
                        ret = new PrintAsBitmapNumActionItem(null, null, printTarget, printBBox);
                        break;
                }*/
                ret = true;
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                SymbolType loadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem url2 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem targetOrNum = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);

                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                //int lvmethod = 0;
                if (s.type == SymbolType.COMMA) {
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.STRING);
                    if (s.value.equals("POST")) {
                        //lvmethod = 2;
                    } else if (s.value.equals("GET")) {
                        //lvmethod = 1;
                    } else {
                        throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                /*switch (loadType) {
                    case LOADVARIABLES:
                        ret = new LoadVariablesActionItem(null, null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIE:
                        ret = new LoadMovieActionItem(null, null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADVARIABLESNUM:
                        ret = new LoadVariablesNumActionItem(null, null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIENUM:
                        ret = new LoadMovieNumActionItem(null, null, url2, targetOrNum, lvmethod);
                        break;
                }*/
                ret = true;
                break;
            case REMOVEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new RemoveSpriteActionItem(null, null, (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case STARTDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem dragTarget = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                /*GraphTargetItem lockCenter;
                GraphTargetItem constrain;
                GraphTargetItem x1 = null;
                GraphTargetItem y1 = null;
                GraphTargetItem x2 = null;
                GraphTargetItem y2 = null;*/
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    //lockCenter = (;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        //constrain = new DirectValueActionItem(null, null, 0, 1L, new ArrayList<>());
                        //x1 = (;
                        expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            //y1 = (;
                            expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                //x2 = (;
                                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    //y2 = (;
                                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                                } else {
                                    lexer.pushback(s);
                                    //y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                                }
                            } else {
                                lexer.pushback(s);
                                //x2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                                //y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            }
                        } else {
                            lexer.pushback(s);
                            //x2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            //y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            //y1 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());

                        }
                    } else {
                        lexer.pushback(s);
                        //constrain = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                    }
                } else {
                    //lockCenter = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                    //constrain = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new StartDragActionItem(null, null, dragTarget, lockCenter, constrain, x1, y1, x2, y2);
                ret = true;
                break;
            case CALL:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new CallActionItem(null, null, (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case GETVERSION:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new GetVersionActionItem(null, null);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBORD:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new MBCharToAsciiActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBCHR:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new MBAsciiToCharActionItem(null, null,;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new MBStringLengthActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case MBSUBSTRING:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem val1 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem index1 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem len1 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new MBStringExtractActionItem(null, null, val1, index1, len1);
                ret = true;
                break;
            case SUBSTR:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem val2 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem index2 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.COMMA);
                //GraphTargetItem len2 = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new StringExtractActionItem(null, null, val2, index2, len2);
                ret = true;
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new StringLengthActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case RANDOM:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new RandomNumberActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case INT:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new ToIntegerActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case NUMBER_OP:
                ParsedSymbol sopn = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    Variable vi = new Variable(false, sopn.value.toString(), sopn.position);
                    variables.add(vi);
                    //ret = vi;
                    ret = true;
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    //ret = new ToNumberActionItem(null, null, ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    expectedType(SymbolType.PARENT_CLOSE);
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
                    //ret = vi2;
                    ret = true;
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    //ret = new ToStringActionItem(null, null, ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    expectedType(SymbolType.PARENT_CLOSE);
                    //ret = memberOrCall(ret, inFunction, inMethod, variables, functions);
                    ret = true;
                }
                break;
            case ORD:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new CharToAsciiActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case CHR:
                expectedType(SymbolType.PARENT_OPEN);
                //ret = new AsciiToCharActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = true;
                break;
            case GETTIMER:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new GetTimeActionItem(null, null);
                ret = true;
                break;
            case TARGETPATH:
                expectedType(SymbolType.PARENT_OPEN);
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
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

    private void expectedIdentifier(ParsedSymbol s, int line, Object... exceptions) throws IOException, ActionParseException {
        for (Object ex : exceptions) {
            if (s.isType(ex)) {
                return;
            }
        }
        if (!isIdentifier(s)) {
            throw new ActionParseException(SymbolType.IDENTIFIER + " expected but " + s.type + " found", line);
        }
    }

    private boolean command(boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, boolean mustBeCommand, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
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

                ret = expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
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
                expectedType(SymbolType.PARENT_OPEN);
                expression(inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                commands(inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                //ret = new WithActionItem(null, null, wvar, wcmd);
                ret = true;
                break;
            case DELETE:
                //GraphTargetItem varDel = 
                expression(inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                /*if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, null, gm.object, gm.memberName);
                } else if (varDel instanceof VariableActionItem) {
                    variables.remove(varDel);
                    ret = new DeleteActionItem(null, null, null, pushConst(((VariableActionItem) varDel).getVariableName()));
                } else if ((varDel instanceof EvalActionItem) || (varDel instanceof ParenthesisItem)) {
                    ret = new DeleteActionItem(null, null, null, varDel.value);
                } else if (varDel instanceof DirectValueActionItem) {
                    ret = new DeleteActionItem(null, null, null, varDel);
                } else {
                    ret = new DeleteActionItem(null, null, null, varDel);
                }*/
                ret = true;

                break;
            case TELLTARGET:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem tellTarget =
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                //List<GraphTargetItem> tellcmds = ;
                commands(inFunction, inMethod, forinlevel, true, variables, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                /*TellTargetActionItem tt = new TellTargetActionItem(null, null, tellTarget, tellcmds);
                if (inTellTarget) {
                    tt.nested = true;
                }
                ret = tt;*/
                ret = true;

                break;

            case IFFRAMELOADED:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem iflExpr = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                //List<GraphTargetItem> iflComs = ;
                commands(inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                //ret = new IfFrameLoadedActionItem(iflExpr, iflComs, null, null);
                ret = true;

                break;
            case CLASS:
                type(variables);
                s = lex();
                if (s.type == SymbolType.EXTENDS) {
                    type(variables);
                    s = lex();
                }
                if (s.type == SymbolType.IMPLEMENTS) {
                    do {
                        type(variables);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                traits(false, null, null, new ArrayList<>(), variables, inTellTarget, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case INTERFACE:
                //GraphTargetItem interfaceTypeStr = ;
                type(variables);
                s = lex();

                if (s.type == SymbolType.EXTENDS) {
                    do {
                        type(variables);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                traits(true, null, null, new ArrayList<>(), variables, inTellTarget, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case FUNCTION:
                s = lexer.lex();
                expectedIdentifier(s, lexer.yyline());
                function(true, s.value.toString(), false, variables, inTellTarget, hasEval);
                break;
            case VAR:
                s = lex();
                expectedIdentifier(s, lexer.yyline());
                String varIdentifier = s.value.toString();
                int varPosition = s.position;
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type(variables);
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    //GraphTargetItem varval = (;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    //ret = new VariableActionItem(varIdentifier, varval, true);
                    Variable vret = new Variable(true, varIdentifier, varPosition);
                    variables.add(vret);
                    //ret = vret
                } else {
                    Variable vret = new Variable(true, varIdentifier, varPosition);
                    variables.add(vret);
                    //ret = vret
                    lexer.pushback(s);
                }
                ret = true;
                break;
            case CURLY_OPEN:
                commands(inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = true;
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                //GraphTargetItem varincdec = ;
                expression(inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                /*if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, null, varincdec);
                }*/
                ret = true;
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, hasEval);
                    Variable supItem = new Variable(false, s.value.toString(), s.position);
                    variables.add(supItem);
                    //ret = new CallMethodActionItem(null, null, supItem, new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, constantPool), args);
                    ret = true;
                } else { //no constructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem ifExpr = (;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //GraphTargetItem onTrue = ;
                command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                //List<GraphTargetItem> onTrueList = new ArrayList<>();
                //onTrueList.add(onTrue);
                s = lex();
                //List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    //onFalseList = new ArrayList<>();
                    //onFalseList.add(
                    command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                    //);
                } else {
                    lexer.pushback(s);
                }
                //ret = new IfItem(DIALECT, null, null, ifExpr, onTrueList, onFalseList);
                ret = true;
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                //List<GraphTargetItem> whileExpr = new ArrayList<>();
                //whileExpr.add(
                expression(inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //List<GraphTargetItem> whileBody = new ArrayList<>();
                //whileBody.add(
                command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                //ret = new WhileItem(DIALECT, null, null, null, whileExpr, whileBody);
                ret = true;
                break;
            case DO:
                //List<GraphTargetItem> doBody = new ArrayList<>();
                //doBody.add(;
                command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                //List<GraphTargetItem> doExpr = new ArrayList<>();
                //doExpr.add(;
                expression(inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = new DoWhileItem(DIALECT, null, null, null, doBody, doExpr);
                ret = true;
                break;
            case FOR:
                expectedType(SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                //GraphTargetItem collection = null;
                String objIdent;
                Variable item = null;
                int innerExprReg = 0;
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
                            if (inFunction) {
                                /*for (int i = 0; i < 256; i++) {
                                 if (!registerVars.containsValue(i)) {
                                 registerVars.put(objIdent, i);
                                 innerExprReg = i;
                                 break;
                                 }
                                 }*/
                            }

                            item = new Variable(define, objIdent, ssel.position);

                            /*item.setStoreValue(new GraphTargetItem(DIALECT) {

                                @Override
                                public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
                                    return writer;
                                }

                                @Override
                                public boolean hasReturnValue() {
                                    return false;
                                }

                                @Override
                                public GraphTargetItem returnType() {
                                    return TypeItem.UNBOUNDED;
                                }

                                //toSource is Empty
                            });*/
                            variables.add(item);

                            //collection = ;
                            expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
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
                /*List<GraphTargetItem> forFinalCommands = new ArrayList<>();
                GraphTargetItem forExpr = null;
                List<GraphTargetItem> forFirstCommands = new ArrayList<>();*/
                if (!forin) {
                    //GraphTargetItem fc = ;
                    command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                    /*if (fc != null) { //can be empty command
                        forFirstCommands.add(fc);
                    }*/
                    //forExpr = ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    /*if (forExpr == null) {
                        forExpr = new TrueItem(DIALECT, null, null);
                    }*/
                    expectedType(SymbolType.SEMICOLON);
                    //GraphTargetItem fcom = ;
                    command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                    /*if (fcom != null) {
                        forFinalCommands.add(fcom);
                    }*/
                }
                expectedType(SymbolType.PARENT_CLOSE);
                //List<GraphTargetItem> forBody = new ArrayList<>();
                //forBody.add(;
                command(inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, inTellTarget, true, variables, hasEval);
                /*if (forin) {
                    ret = new ForInActionItem(null, null, null, item, collection, forBody);
                } else {
                    ret = new ForItem(DIALECT, null, null, null, forFirstCommands, forExpr, forFinalCommands, forBody);
                }*/
                ret = true;
                break;
            case SWITCH:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem switchExpr = ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                s = lex();
                //ret.addAll(switchExpr);
                /*int exprReg = 0;
                 for (int i = 0; i < 256; i++) {
                 if (!registerVars.containsValue(i)) {
                 registerVars.put("__switch" + uniqId(), i);
                 exprReg = i;
                 break;
                 }
                 }*/
 /*List<List<ActionIf>> caseIfs = new ArrayList<>();
                List<List<GraphTargetItem>> caseCmds = new ArrayList<>();
                List<GraphTargetItem> caseExprsAll = new ArrayList<>();
                List<Integer> valueMapping = new ArrayList<>();*/
                int pos = 0;
                while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                    //List<GraphTargetItem> caseExprs; = new ArrayList<>();
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        if (s.type != SymbolType.DEFAULT) {
                            expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                        }
                        //GraphTargetItem curCaseExpr = s.type == SymbolType.DEFAULT ? new DefaultItem(DIALECT) :;
                        //caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        //caseExprsAll.add(curCaseExpr);
                        //valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    //List<GraphTargetItem> caseCmd = ;
                    commands(inFunction, inMethod, forinlevel, inTellTarget, variables, hasEval);
                    //caseCmds.add(caseCmd);
                    s = lex();
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                //ret = new SwitchItem(DIALECT, null, null, null, switchExpr, caseExprsAll, caseCmds, valueMapping);
                ret = true;
                break;
            case BREAK:
                //ret = new BreakItem(DIALECT, null, null, 0); //? There is no more than 1 level continue/break in AS1/2
                ret = true;
                break;
            case CONTINUE:
                //ret = new ContinueItem(DIALECT, null, null, 0); //? There is no more than 1 level continue/break in AS1/2
                ret = true;
                break;
            case RETURN:
                //GraphTargetItem retexpr =;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                /*if (retexpr == null) {
                    retexpr = new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, new ArrayList<>());
                }*/
                //ret = new ReturnActionItem(null, null, retexpr);
                ret = true;
                break;
            case TRY:
                //List<GraphTargetItem> tryCommands = new ArrayList<>();
                //tryCommands.add(;
                command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                s = lex();
                boolean found = false;
                /*List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
                List<GraphTargetItem> catchExceptionNames = new ArrayList<>();
                List<GraphTargetItem> catchExceptionTypes = new ArrayList<>();
                 */
                while (s.type == SymbolType.CATCH) {
                    expectedType(SymbolType.PARENT_OPEN);
                    ParsedSymbol si = lex();
                    expectedIdentifier(si, lexer.yyline(), SymbolType.STRING);
                    //catchExceptionNames.add(pushConst((String) si.value));
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        //catchExceptionTypes.add(;
                        type(variables);
                    } else {
                        //catchExceptionTypes.add(null);
                        lexer.pushback(s);
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    //List<GraphTargetItem> cc = new ArrayList<>();
                    //cc.add(;

                    List<VariableOrScope> subvariables = new ArrayList<>();

                    command(inFunction, inMethod, forinlevel, inTellTarget, true, subvariables, hasEval);

                    //Treat Catch as function - it is closure for variables
                    //FunctionActionItem retf = new FunctionActionItem(null, null, "catch", Arrays.asList((String) si.value), new HashMap<>() /*?*/, null, constantPool, -1, subvariables, subfunctions, false, Arrays.asList(si.position), null);
                    //functions.add(retf);
                    variables.add(new CatchScope(new Variable(true, (String) si.value, si.position), subvariables));

                    //catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                //List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    //finallyCommands = new ArrayList<>();
                    //finallyCommands.add(;
                    command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, hasEval);
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                //ret = new TryActionItem(tryCommands, catchExceptionNames, catchExceptionTypes, catchCommands, finallyCommands);
                ret = true;
                break;
            case THROW:
                //ret = new ThrowActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                ret = true;
                break;
            case SEMICOLON: //empty command
                if (debugMode) {
                    System.out.println("/command");
                }
                //return new EmptyCommand(DIALECT);
                return true;
            case DIRECTIVE:
                switch ((String) s.value) {
                    case "strict":
                        //ret = new StrictModeActionItem(null, null, 1);
                        ret = true;
                        break;
                    default:
                        throw new ActionParseException("Unknown directive: #" + s.value, lexer.yyline());
                }
                break;
            default:
                lexer.pushback(s);
                ret = expression(inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (!ret) {  //can be popped expression
            buf.pushAllBack(lexer);
            ret = expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;
    }

    private boolean expression(boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, boolean allowComma, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expression:");
        }
        //List<GraphTargetItem> commaItems = new ArrayList<>();
        ParsedSymbol symb;
        do {
            boolean prim = expressionPrimary(false, inFunction, inMethod, inTellTarget, allowRemainder, variables, true, hasEval);
            if (!prim) {
                return false;
            }
            //GraphTargetItem expr = ;
            expression1(prim, GraphTargetItem.NOPRECEDENCE, inFunction, inMethod, inTellTarget, allowRemainder, variables, hasEval);
            //commaItems.add(expr);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
        if (debugMode) {
            System.out.println("/expression");
        }
        /*if (commaItems.size() == 1) {
            return commaItems.get(0);
        }
        return new CommaExpressionItem(DIALECT, null, null, commaItems);*/
        return true;
    }

    private ParsedSymbol peekLex() throws IOException, ActionParseException, InterruptedException {
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

    private boolean expression1(boolean lhs, int min_precedence, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol op;
        boolean rhs;
        boolean mhs = false;
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
                mhs = expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                expectedType(SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(allowRemainder, inFunction, inMethod, inTellTarget, allowRemainder, variables, true, hasEval);
            if (rhs == false) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekLex();
            while ((isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) < /* > on wiki */ getSymbPrecedence(op))
                    || (lookahead.type.isRightAssociative() && getSymbPrecedence(lookahead) == getSymbPrecedence(op))) {
                rhs = expression1(rhs, getSymbPrecedence(lookahead), inFunction, inMethod, inTellTarget, allowRemainder, variables, hasEval);
                lookahead = peekLex();
            }

            switch (op.type) {

                case TERNAR:
                    //lhs = new TernarOpItem(DIALECT, null, null, lhs, mhs, rhs);
                    lhs = true;
                    break;
                case SHIFT_LEFT:
                    //lhs = new LShiftActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case SHIFT_RIGHT:
                    //lhs = new RShiftActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case USHIFT_RIGHT:
                    //lhs = new URShiftActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case BITAND:
                    //lhs = new BitAndActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case BITOR:
                    //lhs = new BitOrActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case DIVIDE:
                    //lhs = new DivideActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MODULO:
                    //lhs = new ModuloActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case EQUALS:
                    //lhs = new EqActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    lhs = true;
                    break;
                case STRICT_EQUALS:
                    //lhs = new StrictEqActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case NOT_EQUAL:
                    //lhs = new NeqActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    lhs = true;
                    break;
                case STRICT_NOT_EQUAL:
                    //lhs = new StrictNeqActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case LOWER_THAN:
                    //lhs = new LtActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    lhs = true;
                    break;
                case LOWER_EQUAL:
                    //lhs = new LeActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case GREATER_THAN:
                    //lhs = new GtActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case GREATER_EQUAL:
                    //lhs = new GeActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
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
                case FULLAND:
                    //lhs = new AndActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case FULLOR:
                    //lhs = new OrActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MINUS:
                    //lhs = new SubtractActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case MULTIPLY:
                    //lhs = new MultiplyActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case PLUS:
                    //lhs = new AddActionItem(null, null, lhs, rhs, swfVersion >= 5);
                    lhs = true;
                    break;
                case XOR:
                    //lhs = new BitXorActionItem(null, null, lhs, rhs);
                    lhs = true;
                    break;
                case INSTANCEOF:
                    //lhs = new InstanceOfActionItem(null, null, lhs, rhs);
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
                    /*GraphTargetItem assigned = rhs;
                    switch (op.type) {
                        case ASSIGN:
                            //assigned = assigned;
                            break;
                        case ASSIGN_BITAND:
                            assigned = new BitAndActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_BITOR:
                            assigned = new BitOrActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_DIVIDE:
                            assigned = new DivideActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MINUS:
                            assigned = new SubtractActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MODULO:
                            assigned = new ModuloActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_MULTIPLY:
                            assigned = new MultiplyActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_PLUS:
                            assigned = new AddActionItem(null, null, lhs, assigned, swfVersion >= 5);
                            break;
                        case ASSIGN_SHIFT_LEFT:
                            assigned = new LShiftActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_RIGHT:
                            assigned = new RShiftActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_USHIFT_RIGHT:
                            assigned = new URShiftActionItem(null, null, lhs, assigned);
                            break;
                        case ASSIGN_XOR:
                            assigned = new BitXorActionItem(null, null, lhs, assigned);
                            break;
                    }
                    if (lhs instanceof GetPropertyActionItem) {
                        lhs = new SetPropertyActionItem(null, null, ((GetPropertyActionItem) lhs).target, ((GetPropertyActionItem) lhs).propertyIndex, assigned);
                    } else if (lhs instanceof VariableActionItem) {
                        if (assigned != rhs) {
                            lhs = new VariableActionItem(((VariableActionItem) lhs).getVariableName(), assigned, false);
                            variables.add((VariableActionItem) lhs);
                        } else {
                            ((VariableActionItem) lhs).setStoreValue(assigned);
                            ((VariableActionItem) lhs).setDefinition(false);
                        }
                    } else if (lhs instanceof GetMemberActionItem) {
                        lhs = new SetMemberActionItem(null, null, ((GetMemberActionItem) lhs).object, ((GetMemberActionItem) lhs).memberName, assigned);
                    } else {
                        throw new ActionParseException("Invalid assignment", lexer.yyline());
                    }*/
                    lhs = true;
                    break;
                case IDENTIFIER:
                    switch (op.value.toString()) {
                        case "add":
                            //lhs = new StringAddActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "eq":
                            //lhs = new StringEqActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "ne":
                            //lhs = new StringNeActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "lt":
                            //lhs = new StringLtActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "ge":
                            //lhs = new StringGeActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "gt":
                            //lhs = new StringGtActionItem(null, null, lhs, rhs);
                            lhs = true;
                            break;
                        case "le":
                            //lhs = new StringLeActionItem(null, null, lhs, rhs);
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

    /*private boolean isType(GraphTargetItem item) {
        if (item == null) {
            return false;
        }
        while (item instanceof GetMemberActionItem) {
            item = ((GetMemberActionItem) item).object;
        }
        return (item instanceof VariableActionItem);
    }*/
    private int brackets(List<GraphTargetItem> ret, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
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
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
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

    private boolean handleVariable(ParsedSymbol s, boolean ret, List<VariableOrScope> variables, Reference<Boolean> allowMemberOrCall, boolean inFunction, boolean inMethod, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (s.value.equals("not")) {
            //ret = new NotItem(DIALECT, null, null, ;
            expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
            ret = true;
        } else {
            String varName = s.value.toString();

            Variable vret = new Variable(false, varName, s.position);
            variables.add(vret);
            //ret = vret;
            allowMemberOrCall.setVal(true);
            ret = true;
        }
        return ret;
    }

    private boolean expressionPrimary(boolean allowEmpty, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableOrScope> variables, boolean allowCall, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean allowMemberOrCall = false;
        boolean ret = false;
        ParsedSymbol s = lex();

        switch (s.type) {
            case PREPROCESSOR:
                expectedType(SymbolType.PARENT_OPEN);
                switch ("" + s.value) {
                    //AS 1/2:
                    //AS2:
                    case "constant":
                        s = lexer.lex();
                        expected(s, lexer.yyline(), SymbolType.INTEGER);
                        //ret = new UnresolvedConstantActionItem((int) (long) (Long) s.value);
                        ret = true;
                        break;
                    case "enumerate":
                        //ret = new EnumerateActionItem(null, null, ;
                        expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    //Both ASs
                    case "dup":
                        //ret = new DuplicateItem(DIALECT, null, null,;
                        expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    case "push":
                        //ret = new PushItem(;
                        expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, false, hasEval);
                        ret = true;
                        break;
                    case "pop":
                        //ret = new PopItem(DIALECT, null, null);
                        ret = true;
                        break;
                    case "strict":
                        s = lexer.lex();
                        expected(s, lexer.yyline(), SymbolType.INTEGER);
                        //ret = new StrictModeActionItem(null, null, (int) (long) (Long) s.value);
                        ret = true;
                        break;
                    case "goto": //TODO
                        throw new ActionParseException("Compiling §§" + s.value + " is not available, sorry", lexer.yyline());
                    default:
                        throw new ActionParseException("Unknown preprocessor instruction: §§" + s.value, lexer.yyline());

                }
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NEGATE:
                versionRequired(s, 5);
                expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                //ret = new BitXorActionItem(null, null, ret, new DirectValueActionItem(4.294967295E9));
                ret = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    //ret = new DirectValueActionItem(null, null, 0, -(double) (Double) s.value, new ArrayList<>());
                    ret = true;

                } else if (s.isType(SymbolType.INTEGER)) {
                    //ret = new DirectValueActionItem(null, null, 0, -(long) (Long) s.value, new ArrayList<>());
                    ret = true;

                } else {
                    lexer.pushback(s);
                    //GraphTargetItem num =;
                    expressionPrimary(false, inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                    ret = true;
                    /*if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Long)) {
                        ((DirectValueActionItem) num).value = -(Long) ((DirectValueActionItem) num).value;
                        ret = num;
                    } else if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Double)) {
                        Double d = (Double) ((DirectValueActionItem) num).value;
                        if (d.isInfinite()) {
                            ((DirectValueActionItem) num).value = Double.NEGATIVE_INFINITY;
                        } else {
                            ((DirectValueActionItem) num).value = -d;
                        }
                        ret = (num);
                    } else if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Float)) {
                        ((DirectValueActionItem) num).value = -(Float) ((DirectValueActionItem) num).value;
                        ret = (num);
                    } else {
                        ret = (new SubtractActionItem(null, null, new DirectValueActionItem(null, null, 0, (Long) 0L, new ArrayList<>()), num));
                    }*/
                }
                break;
            case TYPEOF:
                //ret = new TypeOfActionItem(null, null,;
                expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;
                allowMemberOrCall = true;
                break;
            case TRUE:
                //ret = new DirectValueActionItem(null, null, 0, Boolean.TRUE, new ArrayList<>());
                ret = true;
                allowMemberOrCall = true;
                break;
            case NULL:
                //ret = new DirectValueActionItem(null, null, 0, Null.INSTANCE, new ArrayList<>());/
                ret = true;
                allowMemberOrCall = true;
                break;
            case UNDEFINED:
                //ret = new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, new ArrayList<>());r
                ret = true;
                allowMemberOrCall = true;
                break;
            case FALSE:
                //ret = new DirectValueActionItem(null, null, 0, Boolean.FALSE, new ArrayList<>());
                ret = true;
                allowMemberOrCall = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                //List<GraphTargetItem> objectNames = new ArrayList<>();
                //List<GraphTargetItem> objectValues = new ArrayList<>();
                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expectedIdentifier(s, lexer.yyline());
                    //objectNames.add(0, pushConst((String) s.value));
                    expectedType(SymbolType.COLON);
                    //objectValues.add(0, ;
                    expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                //ret = new InitObjectActionItem(null, null, objectNames, objectValues);
                ret = true;
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(inBrackets, inFunction, inMethod, inTellTarget, variables, hasEval);
                //ret = new InitArrayActionItem(null, null, inBrackets);
                ret = true;
                allowMemberOrCall = true;
                break;
            case FUNCTION:
                s = lex();
                String fname = "";
                if (isIdentifier(s)) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                //ret = ;
                function(true, fname, false, variables, inTellTarget, hasEval);
                ret = true;
                allowMemberOrCall = true;
                break;
            case STRING:
                //ret = pushConst(s.value.toString());
                ret = true;
                allowMemberOrCall = true;
                break;
            case NEWLINE:
                //ret = new DirectValueActionItem(null, null, 0, "\n", new ArrayList<>());
                ret = true;
                allowMemberOrCall = true;
                break;
            case INTEGER:
            case DOUBLE:
                //ret = new DirectValueActionItem(null, null, 0, s.value, new ArrayList<>());
                ret = true;
                allowMemberOrCall = true;
                break;
            case DELETE:
                //GraphTargetItem varDel = ;
                expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                /*if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, null, gm.object, gm.memberName);
                } else {
                    if (varDel instanceof VariableActionItem) {
                        varDel = pushConst(((VariableActionItem) varDel).getVariableName());
                    }
                    ret = new DeleteActionItem(null, null, null, varDel);
                }*/
                ret = true;
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                //GraphTargetItem prevar = ;
                expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                /*if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, null, prevar);
                }*/
                ret = true;

                break;
            case NOT:
                //ret = new NotItem(DIALECT, null, null,;
                expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, true, hasEval);
                ret = true;

                break;
            case PARENT_OPEN:
                boolean pexpr = expression(inFunction, inMethod, inTellTarget, true, variables, true, hasEval);
                if (!pexpr) {
                    throw new ActionParseException("Expression expected", lexer.yyline());
                }
                //ret = new ParenthesisItem(DIALECT, null, null, pexpr);
                expectedType(SymbolType.PARENT_CLOSE);
                allowMemberOrCall = true;
                ret = true;
                break;
            case NEW:
                //GraphTargetItem newvar = ;
                ParsedSymbol s1 = lex();
                if (s1.type == SymbolType.NUMBER_OP || s1.type == SymbolType.STRING_OP) {
                    ParsedSymbol s2 = lex();
                    if (s2.type == SymbolType.PARENT_OPEN) {
                        lexer.pushback(s2);
                    } else {
                        lexer.pushback(s2);
                        lexer.pushback(s1);
                        expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                    }
                } else {
                    lexer.pushback(s1);
                    expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                }
                expectedType(SymbolType.PARENT_OPEN);
                call(inFunction, inMethod, inTellTarget, variables, hasEval);
                /*expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                if (newvar instanceof ToNumberActionItem) {
                    List<GraphTargetItem> args = new ArrayList<>();
                    if (((ToNumberActionItem) newvar).value != null) {
                        args.add(((ToNumberActionItem) newvar).value);
                    }
                    ret = new NewObjectActionItem(null, null, pushConst("Number"), args);
                } else if (newvar instanceof ToStringActionItem) {
                    List<GraphTargetItem> args = new ArrayList<>();
                    if (((ToStringActionItem) newvar).value != null) {
                        args.add(((ToStringActionItem) newvar).value);
                    }
                    ret = new NewObjectActionItem(null, null, pushConst("String"), args);
                } else if (newvar instanceof GetMemberActionItem) {

                    GetMemberActionItem ca = (GetMemberActionItem) newvar;
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, hasEval);
                    ret = new NewMethodActionItem(null, null, ca.object, ca.memberName, args);
                } else if (newvar instanceof VariableActionItem) {
                    VariableActionItem cf = (VariableActionItem) newvar;
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, hasEval);
                    ret = new NewObjectActionItem(null, null, pushConst(cf.getVariableName()), args);
                } else {
                    throw new ActionParseException("Invalid new item", lexer.yyline());
                }*/
                ret = true; //Here we do not check for new item type
                allowMemberOrCall = true;

                break;
            case EVAL:
                expectedType(SymbolType.PARENT_OPEN);
                //GraphTargetItem evar = new EvalActionItem(null, null, ;
                expression(inFunction, inMethod, inTellTarget, true, variables, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                hasEval.setVal(true);
                //ret = evar;
                allowMemberOrCall = true;
                ret = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                Reference<Boolean> allowMemberOrCallRef = new Reference<>(allowMemberOrCall);
                ret = handleVariable(s, ret, variables, allowMemberOrCallRef, inFunction, inMethod, inTellTarget, hasEval);
                allowMemberOrCall = allowMemberOrCallRef.getVal();

                break;
            default:

                boolean isGlobalFuncVar = false;
                if (s.group == SymbolGroup.GLOBALFUNC) {
                    ParsedSymbol s2 = peekLex();
                    if (s2.type != SymbolType.PARENT_OPEN) {
                        Reference<Boolean> allowMemberOrCallRef2 = new Reference<>(allowMemberOrCall);
                        ret = handleVariable(s, ret, variables, allowMemberOrCallRef2, inFunction, inMethod, inTellTarget, hasEval);
                        allowMemberOrCall = allowMemberOrCallRef2.getVal();
                        isGlobalFuncVar = true;
                    }
                }

                if (!isGlobalFuncVar) {
                    boolean excmd = expressionCommands(s, inFunction, inMethod, inTellTarget, -1, variables, hasEval);
                    if (excmd) {
                        //?
                        ret = excmd;
                        allowMemberOrCall = true; //?
                        break;
                    }
                    lexer.pushback(s);
                }
        }

        if (allowMemberOrCall && ret) {
            ret = memberOrCall(ret, inFunction, inMethod, inTellTarget, variables, allowCall, hasEval);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    /*
    private boolean isCastOp(GraphTargetItem item) {
        LocalData localData = LocalData.create(new ConstantPool(constantPool));
        List<String> items = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            if (mem.memberName instanceof DirectValueActionItem) {
                items.add(0, mem.memberName.toStringNoQuotes(localData));
            }
            item = mem.object;
        }
        if (item instanceof VariableActionItem) {
            VariableActionItem v = (VariableActionItem) item;
            items.add(0, v.getVariableName());
        }

        if (items.isEmpty()) {
            return false;
        }
        String fullName = String.join(".", items);
        if (BUILTIN_CASTS.contains(fullName)) {
            return true;
        }
        if (swfClasses.contains(fullName)) {
            return true;
        }
        return false;
    }*/
    private boolean memberOrCall(boolean ret, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableOrScope> variables, boolean allowCall, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol op = lex();
        while (op.isType(SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.DOT)) {
            if (op.type == SymbolType.PARENT_OPEN) {
                if (!allowCall) {
                    break;
                }
                //List<GraphTargetItem> args = ;
                call(inFunction, inMethod, inTellTarget, variables, hasEval);
                /*if (isCastOp(ret) && args.size() == 1) {
                    ret = new CastOpActionItem(null, null, ret, args.get(0));
                } else if (ret instanceof GetMemberActionItem) {
                    GetMemberActionItem mem = (GetMemberActionItem) ret;
                    ret = new CallMethodActionItem(null, null, mem.object, mem.memberName, args);
                } else if (ret instanceof VariableActionItem) {
                    VariableActionItem var = (VariableActionItem) ret;

                    if (var.getVariableName().equals("getProperty")
                            && args.size() == 2
                            && (args.get(1) instanceof VariableActionItem)
                            && (Action.propertyNamesListLowerCase.contains(((VariableActionItem) args.get(1)).getVariableName().toLowerCase()))) {
                        ret = new GetPropertyActionItem(null, null, args.get(0), Action.propertyNamesListLowerCase.indexOf(((VariableActionItem) args.get(1)).getVariableName().toLowerCase()));
                    } else if (var.getVariableName().equals("setProperty")
                            && args.size() == 3
                            && (args.get(1) instanceof VariableActionItem)
                            && (Action.propertyNamesListLowerCase.contains(((VariableActionItem) args.get(1)).getVariableName().toLowerCase()))) {
                        ret = new SetPropertyActionItem(null, null, args.get(0), Action.propertyNamesListLowerCase.indexOf(((VariableActionItem) args.get(1)).getVariableName().toLowerCase()), args.get(2));
                    } else {
                        ret = new CallFunctionActionItem(null, null, var, args);
                    }
                } else if (ret instanceof EvalActionItem) {
                    EvalActionItem ev = (EvalActionItem) ret;
                    ret = new CallFunctionActionItem(null, null, ev.value, args);
                } else {
                    ret = new CallFunctionActionItem(null, null, ret, args);
                }*/
                ret = true;
            }
            if (op.type == SymbolType.BRACKET_OPEN) {
                //GraphTargetItem rhs = ;
                expression(inFunction, inMethod, inTellTarget, false, variables, false, hasEval);
                //ret = new GetMemberActionItem(null, null, ret, rhs);
                expectedType(SymbolType.BRACKET_CLOSE);
                ret = true;
            }
            if (op.type == SymbolType.DOT) {
                ParsedSymbol s = lex();
                expectedIdentifier(s, lexer.yyline(), SymbolType.THIS, SymbolType.SUPER);

                //ret = new GetMemberActionItem(null, null, ret, pushConst(s.value.toString()));
                ret = true;
            }
            op = lex();
        }

        switch (op.type) {
            case INCREMENT: //postincrement
                /*if (!(ret instanceof VariableActionItem) && !(ret instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }*/
                //ret = new PostIncrementActionItem(null, null, ret);
                ret = true;
                op = lex();
                break;
            case DECREMENT: //postdecrement
                /*if (!(ret instanceof VariableActionItem) && !(ret instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }*/
                //ret = new PostDecrementActionItem(null, null, ret);
                ret = true;
                op = lex();
                break;
        }

        lexer.pushback(op);
        return ret;
    }

    private DirectValueActionItem pushConst(String s) throws IOException, ActionParseException {

        //ActionConstantPool was introduced in SWF 5
        if (swfVersion < 5) {
            return new DirectValueActionItem(null, null, 0, s, constantPool);
        }

        int index = constantPool.indexOf(s);
        if (index == -1) {
            if (ActionConstantPool.calculateSize(constantPool) + ActionConstantPool.calculateSize(s) <= 0xffff) {
                // constant pool is not full
                constantPool.add(s);
                index = constantPool.indexOf(s);
            }
        }

        if (index == -1) {
            return new DirectValueActionItem(null, null, 0, s, constantPool);
        }

        return new DirectValueActionItem(null, null, 0, new ConstantIndex(index), constantPool);
    }

    private ActionScriptLexer lexer = null;

    private List<String> constantPool;

    /**
     * Convert a string to a high-level model.
     *
     * @param str The string to convert
     * @throws ActionParseException On parse error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void parse(String str, Map<Integer, List<Integer>> definitionPosToReferences, Map<Integer, Integer> referenceToDefinition) throws ActionParseException, IOException, InterruptedException {
        this.constantPool = new ArrayList<>();
        lexer = new ActionScriptLexer(new StringReader(str));
        if (swfVersion >= ActionScriptLexer.SWF_VERSION_CASE_SENSITIVE) {
            lexer.setCaseSensitiveIdentifiers(true);
        }

        ParsedSymbol symb = lexer.lex();
        boolean inOnHandler = false;

        if (symb.type == SymbolType.IDENTIFIER && ("on".equals(symb.value) || "onClipEvent".equals(symb.value))) {
            expectedType(SymbolType.PARENT_OPEN);
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
                        expected(symb, lexer.yyline(), SymbolType.STRING);
                        Integer key = CLIPACTIONRECORD.stringToKey((String) symb.value);
                        if (key == null) {
                            throw new ActionParseException("Invalid key", lexer.yyline());
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
                        throw new ActionParseException("Unrecognized event type", lexer.yyline());
                }
                symb = lexer.lex();
                if (symb.type == SymbolType.PARENT_CLOSE) {
                    break;
                }
                expected(symb, lexer.yyline(), SymbolType.COMMA);
                symb = lexer.lex();
            }
            expected(symb, lexer.yyline(), SymbolType.PARENT_CLOSE);
            if (condEmpty) {
                throw new ActionParseException("condition must be non empty", lexer.yyline());
            }
            expectedType(SymbolType.CURLY_OPEN);
            inOnHandler = true;
        } else {
            lexer.pushback(symb);
        }

        List<VariableOrScope> vars = new ArrayList<>();
        List<VariableOrScope> functions = new ArrayList<>();
        Reference<Boolean> hasEval = new Reference<>(false);
        commands(false, false, 0, false, vars, hasEval);
        Map<String, Integer> varNameToDefinitionPosition = new LinkedHashMap<>();

        parseVariablesList(new ArrayList<>(), vars, definitionPosToReferences, referenceToDefinition, varNameToDefinitionPosition);

        /*for (VariableTreeItem v : vars) {
            if (v.isDefinition()) {
                varNameToDefinitionPosition.put(v.getVariableName(), v.getPosition());
                definitionPosToReferences.put(v.getPosition(), new ArrayList<>());
            } else {
                if (varNameToDefinitionPosition.containsKey(v.getVariableName())) {
                    int definitionPos = varNameToDefinitionPosition.get(v.getVariableName());
                    definitionPosToReferences.get(definitionPos).add(v.getPosition());
                    referenceToDefinition.put(v.getPosition(), definitionPos);
                }
            }
        }
        for (FunctionActionItem f : functions) {
            parseFunction(f, varNameToDefinitionPosition, definitionPosToReferences, referenceToDefinition);
        }*/
        if (inOnHandler) {
            expectedType(SymbolType.CURLY_CLOSE);
        }

        if (lexer.lex().type != SymbolType.EOF) {
            throw new ActionParseException("Parsing finished before end of the file", lexer.yyline());
        }
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
                    if (privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    } else {
                        //first usage, take as definition (?)
                        privateVarNameToDefinitionPosition.put(v.name, v.position);
                        definitionPosToReferences.put(v.position, new ArrayList<>());
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
                    if (privateVarNameToDefinitionPosition.containsKey(v.name)) {
                        int definitionPos = privateVarNameToDefinitionPosition.get(v.name);
                        definitionPosToReferences.get(definitionPos).add(v.position);
                        referenceToDefinition.put(v.position, definitionPos);
                    } else {
                        //first usage, take as definition (?)
                        parentVarNameToDefinitionPosition.put(v.name, v.position);
                        privateVarNameToDefinitionPosition.put(v.name, v.position);
                        definitionPosToReferences.put(v.position, new ArrayList<>());
                    }
                }
            }
            if (vt instanceof Scope) {
                Scope vs = (Scope) vt;
                parseVariablesList(vs.getPrivateItems(), vs.getSharedItems(), definitionPosToReferences, referenceToDefinition, privateVarNameToDefinitionPosition);
            }
        }
    }

    private void versionRequired(ParsedSymbol s, int min) throws ActionParseException {
        versionRequired(s.value.toString(), min, Integer.MAX_VALUE);
    }

    private void versionRequired(ParsedSymbol s, int min, int max) throws ActionParseException {
        versionRequired(s.value.toString(), min, max);
    }

    private void versionRequired(String type, int min, int max) throws ActionParseException {
        if (min == max && swfVersion != min) {
            throw new ActionParseException(type + " requires SWF version " + min, lexer.yyline());
        }
        if (swfVersion < min) {
            throw new ActionParseException(type + " requires at least SWF version " + min, lexer.yyline());
        }
        if (swfVersion > max) {
            throw new ActionParseException(type + " requires SWF version lower than " + max, lexer.yyline());
        }
    }
}
