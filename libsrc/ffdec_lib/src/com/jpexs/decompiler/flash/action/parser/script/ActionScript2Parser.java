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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.AsciiToCharActionItem;
import com.jpexs.decompiler.flash.action.model.CallActionItem;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.CastOpActionItem;
import com.jpexs.decompiler.flash.action.model.CharToAsciiActionItem;
import com.jpexs.decompiler.flash.action.model.CloneSpriteActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DeleteActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EnumerateActionItem;
import com.jpexs.decompiler.flash.action.model.EvalActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommand2ActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommandActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.GetTimeActionItem;
import com.jpexs.decompiler.flash.action.model.GetURL2ActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.GetVersionActionItem;
import com.jpexs.decompiler.flash.action.model.GotoFrame2ActionItem;
import com.jpexs.decompiler.flash.action.model.InitArrayActionItem;
import com.jpexs.decompiler.flash.action.model.InitObjectActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesNumActionItem;
import com.jpexs.decompiler.flash.action.model.MBAsciiToCharActionItem;
import com.jpexs.decompiler.flash.action.model.MBCharToAsciiActionItem;
import com.jpexs.decompiler.flash.action.model.MBStringExtractActionItem;
import com.jpexs.decompiler.flash.action.model.MBStringLengthActionItem;
import com.jpexs.decompiler.flash.action.model.NewMethodActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.NextFrameActionItem;
import com.jpexs.decompiler.flash.action.model.PlayActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PrevFrameActionItem;
import com.jpexs.decompiler.flash.action.model.PrintActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapNumActionItem;
import com.jpexs.decompiler.flash.action.model.PrintNumActionItem;
import com.jpexs.decompiler.flash.action.model.RandomNumberActionItem;
import com.jpexs.decompiler.flash.action.model.RemoveSpriteActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StartDragActionItem;
import com.jpexs.decompiler.flash.action.model.StopActionItem;
import com.jpexs.decompiler.flash.action.model.StopAllSoundsActionItem;
import com.jpexs.decompiler.flash.action.model.StopDragActionItem;
import com.jpexs.decompiler.flash.action.model.StrictModeActionItem;
import com.jpexs.decompiler.flash.action.model.StringExtractActionItem;
import com.jpexs.decompiler.flash.action.model.StringLengthActionItem;
import com.jpexs.decompiler.flash.action.model.TargetPathActionItem;
import com.jpexs.decompiler.flash.action.model.ThrowActionItem;
import com.jpexs.decompiler.flash.action.model.ToIntegerActionItem;
import com.jpexs.decompiler.flash.action.model.ToNumberActionItem;
import com.jpexs.decompiler.flash.action.model.ToStringActionItem;
import com.jpexs.decompiler.flash.action.model.ToggleHighQualityActionItem;
import com.jpexs.decompiler.flash.action.model.TraceActionItem;
import com.jpexs.decompiler.flash.action.model.TypeOfActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.UnresolvedConstantActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TryActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.WithActionItem;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.model.operations.AndActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitAndActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitOrActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitXorActionItem;
import com.jpexs.decompiler.flash.action.model.operations.DivideActionItem;
import com.jpexs.decompiler.flash.action.model.operations.EqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.GeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.GtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.InstanceOfActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LShiftActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.ModuloActionItem;
import com.jpexs.decompiler.flash.action.model.operations.MultiplyActionItem;
import com.jpexs.decompiler.flash.action.model.operations.NeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.OrActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.RShiftActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictNeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringAddActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringGeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringGtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringLeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringLtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringNeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.SubtractActionItem;
import com.jpexs.decompiler.flash.action.model.operations.URShiftActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.FSCOMMAND;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPEVENTFLAGS;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
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
import com.jpexs.decompiler.graph.model.LocalData;
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
import com.jpexs.helpers.Reference;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ActionScript 1/2 parser.
 *
 * @author JPEXS
 */
public class ActionScript2Parser {

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
     * Target source
     */
    private final ASMSource targetSource;
    /**
     * Charset
     */
    private String charset;

    /**
     * Constructor
     * @param swf Swf
     * @param targetSource Target source
     */
    public ActionScript2Parser(SWF swf, ASMSource targetSource) {
        this.swfVersion = swf.version;
        this.charset = swf.getCharset();
        parseSwfClasses(swf);
        this.targetSource = targetSource;
    }

    private long uniqLast = 0;

    private final boolean debugMode = false;

    /**
     * Parse SWF classes
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

    private List<GraphTargetItem> commands(boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd;
        while ((cmd = command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(List<VariableActionItem> variables) throws IOException, ActionParseException, InterruptedException {
        GraphTargetItem ret;

        ParsedSymbol s = lex();
        expectedIdentifier(s, lexer.yyline());
        ret = new VariableActionItem(s.value.toString(), null, false);
        variables.add((VariableActionItem) ret);
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expectedIdentifier(s, lexer.yyline());
            ret = new GetMemberActionItem(null, null, ret, pushConst(s.value.toString()));
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    /*private GraphTargetItem variable(boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
     GraphTargetItem ret = null;
     ParsedSymbol s = lex();
     expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
     ret = new VariableActionItem(s.value.toString(), null, false);
     variables.add((VariableActionItem) ret);
     ret = (member(ret, inFunction, inMethod, variables, functions));
     return ret;
     }*/
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

    private List<GraphTargetItem> call(boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private FunctionActionItem function(boolean withBody, String functionName, boolean isMethod, List<VariableActionItem> variables, List<FunctionActionItem> functions, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        GraphTargetItem ret = null;
        ParsedSymbol s;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            expectedIdentifier(s, lexer.yyline());
            paramNames.add(s.value.toString());
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
        List<VariableActionItem> subvariables = new ArrayList<>();
        List<FunctionActionItem> subfunctions = new ArrayList<>();
        Reference<Boolean> subHasEval = new Reference<>(false);
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);
            body = commands(true, isMethod, 0, inTellTarget, subvariables, subfunctions, subHasEval);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        if (subHasEval.getVal()) {
            hasEval.setVal(true);
        }

        FunctionActionItem retf = new FunctionActionItem(null, null, functionName, paramNames, new HashMap<>() /*?*/, body, constantPool, -1, subvariables, subfunctions, subHasEval.getVal());
        functions.add(retf);
        return retf;
    }

    private GraphTargetItem traits(boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<VariableActionItem> variables, List<FunctionActionItem> functions, boolean inTellTarget, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {

        GraphTargetItem ret = null;
        /*for (int i = 0; i < nameStr.size() - 1; i++) {
         List<GraphTargetItem> notBody = new ArrayList<>();
         List<String> globalClassTypeStr = new ArrayList<>();
         globalClassTypeStr.add("_global");
         for (int j = 0; j <= i; j++) {
         globalClassTypeStr.add(nameStr.get(j));
         }

         List<GraphTargetItem> val = new ArrayList<>();
         val.add(new ActionPush((Long) 0L));
         val.add(pushConst("Object"));
         val.add(new ActionNewObject());
         notBody.addAll(typeToActions(globalClassTypeStr, val));
         ret.addAll(typeToActions(globalClassTypeStr, null));
         ret.add(new ActionNot());
         ret.add(new ActionNot());
         ret.add(new ActionIf(GraphTargetItem.actionsToBytes(notBody, false, SWF.DEFAULT_VERSION).length));
         ret.addAll(notBody);
         }
         List<GraphTargetItem> ifbody = new ArrayList<>();
         List<String> globalClassTypeStr = new ArrayList<>();
         globalClassTypeStr.add("_global");
         globalClassTypeStr.addAll(nameStr);*/

        ParsedSymbol s;
        List<MyEntry<GraphTargetItem, GraphTargetItem>> traits = new ArrayList<>();
        List<Boolean> traitsStatic = new ArrayList<>();

        String classNameStr = "";
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
        }

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
                    if (fname.equals(classNameStr)) { //constructor
                        //actually there's no difference, it's instance trait
                    }
                    if (!isInterface) {
                        if (isStatic) {
                            FunctionActionItem ft = function(!isInterface, "", true, variables, functions, inTellTarget, hasEval);
                            ft.calculatedFunctionName = pushConst(fname);
                            ft.isSetter = isSetter;
                            ft.isGetter = isGetter;
                            //staticFunctions.add(ft);
                            traits.add(new MyEntry<>(ft.calculatedFunctionName, ft));
                            traitsStatic.add(true);

                            if (isSetter) {
                                //add return getter automatically
                                GraphTargetItem callM = new CallMethodActionItem(null, null, nameStr, pushConst("__get__" + fname), new ArrayList<>());
                                GraphTargetItem retV = new ReturnActionItem(null, null, callM);
                                ft.actions.add(retV);
                            }
                        } else {
                            FunctionActionItem ft = function(!isInterface, "", true, variables, functions, inTellTarget, hasEval);
                            ft.calculatedFunctionName = pushConst(fname);
                            ft.isSetter = isSetter;
                            ft.isGetter = isGetter;
                            //instanceFunctions.add(ft);
                            traits.add(new MyEntry<>(ft.calculatedFunctionName, ft));
                            traitsStatic.add(false);

                            if (isSetter) {
                                //add return getter automatically
                                GraphTargetItem thisVar = new VariableActionItem("this", null, false);
                                ft.addVariable((VariableActionItem) thisVar);
                                GraphTargetItem callM = new CallMethodActionItem(null, null, thisVar, pushConst("__get__" + fname), new ArrayList<>());
                                GraphTargetItem retV = new ReturnActionItem(null, null, callM);
                                ft.actions.add(retV);
                            }
                        }

                    }
                    break;
                case VAR:
                    s = lex();
                    expectedIdentifier(s, lexer.yyline());
                    String ident = s.value.toString();
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        traits.add(new MyEntry<>(pushConst(ident), expression(false, false, false, true, variables, functions, false, hasEval)));
                        traitsStatic.add(isStatic);
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

        if (isInterface) {
            return new InterfaceActionItem(nameStr, implementsStr);
        } else {
            return new ClassActionItem(nameStr, extendsStr, implementsStr, traits, traitsStatic);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, boolean inFunction, boolean inMethod, boolean inTellTarget, int forinlevel, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expressionCommands:");
        }
        GraphTargetItem ret = null;
        switch (s.type) {
            case DUPLICATEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem src3 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem tar3 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem dep3 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new CloneSpriteActionItem(null, null, src3, tar3, dep3);
                break;
            case FSCOMMAND:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem command = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                s = lex();
                GraphTargetItem parameter = null;
                if (s.isType(SymbolType.COMMA)) {
                    parameter = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);                
                } else {
                    lexer.pushback(s);
                }
                ret = new FSCommandActionItem(null, null, command, parameter);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case FSCOMMAND2:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem arg0 = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                List<GraphTargetItem> args = new ArrayList<>();
                args.add(arg0);
                s = lex();
                while (s.isType(SymbolType.COMMA)) {
                    args.add(0, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    s = lex();
                }
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE);
                ret = new FSCommand2ActionItem(null, null, args);
                break;
            case SET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem name1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem value1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new SetVariableActionItem(null, null, name1, value1);
                ((SetVariableActionItem) ret).forceUseSet = true;
                hasEval.setVal(true); //FlashPro does this (using definelocal for funcs) only for eval func, but we will also use set since it is generated by obfuscated identifiers
                break;
            case TRACE:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TraceActionItem(null, null, (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case GETURL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int getuMethod = 0;
                GraphTargetItem target;
                if (s.type == SymbolType.COMMA) {
                    target = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        if (s.value.equals("GET")) {
                            getuMethod = 1;
                        } else if (s.value.equals("POST")) {
                            getuMethod = 2;
                        } else {
                            throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                    target = new DirectValueActionItem(null, null, 0, "", new ArrayList<>());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetURL2ActionItem(null, null, url, target, getuMethod);
                break;
            case GOTOANDSTOP:
            case GOTOANDPLAY:
                SymbolType gtKind = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtsFrame = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                int gtsSceneBias = -1;
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    if ((gtsFrame instanceof DirectValueActionItem) && (((DirectValueActionItem) gtsFrame).value instanceof Long)) {
                        gtsSceneBias = (int) (long) (Long) ((DirectValueActionItem) gtsFrame).value;
                    } else {
                        throw new ActionParseException("Scene bias must be number", lexer.yyline());
                    }

                    gtsFrame = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2ActionItem(null, null, gtsFrame, gtsSceneBias != -1, gtKind == SymbolType.GOTOANDPLAY, gtsSceneBias);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NEXTFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new NextFrameActionItem(null, null);
                break;
            case PLAY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PlayActionItem(null, null);
                break;
            case PREVFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PrevFrameActionItem(null, null);
                break;
            case STOP:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopActionItem(null, null);
                break;
            case STOPALLSOUNDS:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopAllSoundsActionItem(null, null);
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new ToggleHighQualityActionItem(null, null);
                break;

            case STOPDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopDragActionItem(null, null);
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                SymbolType unloadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem unTargetOrNum = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                if (unloadType == SymbolType.UNLOADMOVIE) {
                    ret = new UnLoadMovieActionItem(null, null, unTargetOrNum);
                }
                if (unloadType == SymbolType.UNLOADMOVIENUM) {
                    ret = new UnLoadMovieNumActionItem(null, null, unTargetOrNum);
                }
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                SymbolType printType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem printTarget = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem printBBox = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);

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
                }
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                SymbolType loadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem targetOrNum = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));

                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int lvmethod = 0;
                if (s.type == SymbolType.COMMA) {
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.STRING);
                    if (s.value.equals("POST")) {
                        lvmethod = 2;
                    } else if (s.value.equals("GET")) {
                        lvmethod = 1;
                    } else {
                        throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                switch (loadType) {
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
                }
                break;
            case REMOVEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RemoveSpriteActionItem(null, null, (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case STARTDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem dragTarget = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                GraphTargetItem lockCenter;
                GraphTargetItem constrain;
                GraphTargetItem x1 = null;
                GraphTargetItem y1 = null;
                GraphTargetItem x2 = null;
                GraphTargetItem y2 = null;
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    lockCenter = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        constrain = new DirectValueActionItem(null, null, 0, 1L, new ArrayList<>());
                        x1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            y1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                x2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    y2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                                } else {
                                    lexer.pushback(s);
                                    y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                                }
                            } else {
                                lexer.pushback(s);
                                x2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                                y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            }
                        } else {
                            lexer.pushback(s);
                            x2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            y2 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                            y1 = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());

                        }
                    } else {
                        lexer.pushback(s);
                        constrain = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                        //ret.add(new ActionPush(Boolean.FALSE));
                    }
                } else {
                    lockCenter = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                    constrain = new DirectValueActionItem(null, null, 0, 0L, new ArrayList<>());
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StartDragActionItem(null, null, dragTarget, lockCenter, constrain, x1, y1, x2, y2);
                break;
            case CALL:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CallActionItem(null, null, (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case GETVERSION:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new GetVersionActionItem(null, null);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBCharToAsciiActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBCHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBAsciiToCharActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBSUBSTRING:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len1 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new MBStringExtractActionItem(null, null, val1, index1, len1);
                break;
            case SUBSTR:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len2 = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StringExtractActionItem(null, null, val2, index2, len2);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case RANDOM:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RandomNumberActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case INT:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new ToIntegerActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NUMBER_OP:
                ParsedSymbol sopn = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    VariableActionItem vi = new VariableActionItem(sopn.value.toString(), null, false);
                    variables.add(vi);
                    ret = vi; //memberOrCall(vi, inFunction, inMethod, variables, functions);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToNumberActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    expectedType(SymbolType.PARENT_CLOSE);
                }
                break;
            case STRING_OP:
                ParsedSymbol sop = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    VariableActionItem vi2 = new VariableActionItem(sop.value.toString(), null, false);
                    variables.add(vi2);
                    ret = vi2; //memberOrCall(vi2, inFunction, inMethod, variables, functions);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToStringActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    expectedType(SymbolType.PARENT_CLOSE);
                    //ret = memberOrCall(ret, inFunction, inMethod, variables, functions);
                }
                break;
            case ORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CharToAsciiActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new AsciiToCharActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case GETTIMER:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetTimeActionItem(null, null);
                break;
            case TARGETPATH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TargetPathActionItem(null, null, (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            default:
                return null;
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

    private GraphTargetItem command(boolean inFunction, boolean inMethod, int forinlevel, boolean inTellTarget, boolean mustBeCommand, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
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
        if (s.group == SymbolGroup.GLOBALFUNC) {
            ParsedSymbol s2 = lex();
            if (s2.type != SymbolType.PARENT_OPEN) {
                lexer.removeListener(buf);
                buf.pushAllBack(lexer);

                ret = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
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
                GraphTargetItem wvar = expression(inFunction, inMethod, inTellTarget, false, variables, functions, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> wcmd = commands(inFunction, inMethod, forinlevel, inTellTarget, variables, functions, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new WithActionItem(null, null, wvar, wcmd);
                break;
            case DELETE:
                GraphTargetItem varDel = expression(inFunction, inMethod, inTellTarget, false, variables, functions, false, hasEval);
                if (varDel instanceof GetMemberActionItem) {
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
                }
                break;
            case TELLTARGET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem tellTarget = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> tellcmds = commands(inFunction, inMethod, forinlevel, true, variables, functions, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                TellTargetActionItem tt = new TellTargetActionItem(null, null, tellTarget, tellcmds);
                if (inTellTarget) {
                    tt.nested = true;
                }
                ret = tt;
                break;

            case IFFRAMELOADED:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem iflExpr = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> iflComs = commands(inFunction, inMethod, forinlevel, inTellTarget, variables, functions, hasEval);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new IfFrameLoadedActionItem(iflExpr, iflComs, null, null);
                break;
            case CLASS:
                GraphTargetItem classTypeStr = type(variables);
                s = lex();
                GraphTargetItem extendsTypeStr = null;
                if (s.type == SymbolType.EXTENDS) {
                    extendsTypeStr = type(variables);
                    s = lex();
                }
                List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                if (s.type == SymbolType.IMPLEMENTS) {
                    do {
                        GraphTargetItem implementsTypeStr = type(variables);
                        implementsTypeStrs.add(implementsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(false, classTypeStr, extendsTypeStr, implementsTypeStrs, variables, functions, inTellTarget, hasEval));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INTERFACE:
                GraphTargetItem interfaceTypeStr = type(variables);
                s = lex();
                List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                if (s.type == SymbolType.EXTENDS) {
                    do {
                        GraphTargetItem intExtendsTypeStr = type(variables);
                        intExtendsTypeStrs.add(intExtendsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(true, interfaceTypeStr, null, intExtendsTypeStrs, variables, functions, inTellTarget, hasEval));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s = lexer.lex();
                expectedIdentifier(s, lexer.yyline());
                ret = (function(true, s.value.toString(), false, variables, functions, inTellTarget, hasEval));
                break;
            case VAR:
                s = lex();
                expectedIdentifier(s, lexer.yyline());
                String varIdentifier = s.value.toString();
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type(variables);
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    GraphTargetItem varval = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    ret = new VariableActionItem(varIdentifier, varval, true);
                    variables.add((VariableActionItem) ret);
                } else {
                    ret = new VariableActionItem(varIdentifier, null, true);
                    variables.add((VariableActionItem) ret);
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret = new BlockItem(null, null, commands(inFunction, inMethod, forinlevel, inTellTarget, variables, functions, hasEval));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                GraphTargetItem varincdec = expression(inFunction, inMethod, inTellTarget, false, variables, functions, false, hasEval);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, null, varincdec);
                }
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, functions, hasEval);
                    VariableActionItem supItem = new VariableActionItem(s.value.toString(), null, false);
                    variables.add(supItem);
                    ret = new CallMethodActionItem(null, null, supItem, new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, constantPool), args);
                } else { //no constructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem ifExpr = (expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                GraphTargetItem onTrue = command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval);
                List<GraphTargetItem> onTrueList = new ArrayList<>();
                onTrueList.add(onTrue);
                s = lex();
                List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    onFalseList = new ArrayList<>();
                    onFalseList.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                } else {
                    lexer.pushback(s);
                }
                ret = new IfItem(null, null, ifExpr, onTrueList, onFalseList);
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> whileExpr = new ArrayList<>();
                whileExpr.add(expression(inFunction, inMethod, inTellTarget, true, variables, functions, true, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> whileBody = new ArrayList<>();
                whileBody.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                ret = new WhileItem(null, null, null, whileExpr, whileBody);
                break;
            case DO:
                List<GraphTargetItem> doBody = new ArrayList<>();
                doBody.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> doExpr = new ArrayList<>();
                doExpr.add(expression(inFunction, inMethod, inTellTarget, true, variables, functions, true, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new DoWhileItem(null, null, null, doBody, doExpr);
                break;
            case FOR:
                expectedType(SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                GraphTargetItem collection = null;
                String objIdent;
                VariableActionItem item = null;
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

                            item = new VariableActionItem(objIdent, null, define);

                            item.setStoreValue(new GraphTargetItem() {

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
                            });

                            variables.add(item);

                            collection = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
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
                List<GraphTargetItem> forFinalCommands = new ArrayList<>();
                GraphTargetItem forExpr = null;
                List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                if (!forin) {
                    GraphTargetItem fc = command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval);
                    if (fc != null) { //can be empty command
                        forFirstCommands.add(fc);
                    }
                    forExpr = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                    if (forExpr == null) {
                        forExpr = new TrueItem(null, null);
                    }
                    expectedType(SymbolType.SEMICOLON);
                    GraphTargetItem fcom = command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval);
                    if (fcom != null) {
                        forFinalCommands.add(fcom);
                    }
                }
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> forBody = new ArrayList<>();
                forBody.add(command(inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, inTellTarget, true, variables, functions, hasEval));
                if (forin) {
                    ret = new ForInActionItem(null, null, null, item, collection, forBody);
                } else {
                    ret = new ForItem(null, null, null, forFirstCommands, forExpr, forFinalCommands, forBody);
                }
                break;
            case SWITCH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem switchExpr = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
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
                List<List<ActionIf>> caseIfs = new ArrayList<>();
                List<List<GraphTargetItem>> caseCmds = new ArrayList<>();
                List<GraphTargetItem> caseExprsAll = new ArrayList<>();
                List<Integer> valueMapping = new ArrayList<>();
                int pos = 0;
                while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                    //List<GraphTargetItem> caseExprs; = new ArrayList<>();
                    while (s.type == SymbolType.CASE || s.type == SymbolType.DEFAULT) {
                        GraphTargetItem curCaseExpr = s.type == SymbolType.DEFAULT ? new DefaultItem() : expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                        //caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        caseExprsAll.add(curCaseExpr);
                        valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    List<GraphTargetItem> caseCmd = commands(inFunction, inMethod, forinlevel, inTellTarget, variables, functions, hasEval);
                    caseCmds.add(caseCmd);
                    s = lex();
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                ret = new SwitchItem(null, null, null, switchExpr, caseExprsAll, caseCmds, valueMapping);
                break;
            case BREAK:
                ret = new BreakItem(null, null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case CONTINUE:
                ret = new ContinueItem(null, null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case RETURN:
                GraphTargetItem retexpr = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
                if (retexpr == null) {
                    retexpr = new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, new ArrayList<>());
                }
                ret = new ReturnActionItem(null, null, retexpr);
                break;
            case TRY:
                List<GraphTargetItem> tryCommands = new ArrayList<>();
                tryCommands.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                s = lex();
                boolean found = false;
                List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
                List<GraphTargetItem> catchExceptionNames = new ArrayList<>();
                List<GraphTargetItem> catchExceptionTypes = new ArrayList<>();

                while (s.type == SymbolType.CATCH) {
                    expectedType(SymbolType.PARENT_OPEN);
                    s = lex();
                    expectedIdentifier(s, lexer.yyline(), SymbolType.STRING);
                    catchExceptionNames.add(pushConst((String) s.value));
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        catchExceptionTypes.add(type(variables));
                    } else {
                        catchExceptionTypes.add(null);
                        lexer.pushback(s);
                    }
                    expectedType(SymbolType.PARENT_CLOSE);
                    List<GraphTargetItem> cc = new ArrayList<>();
                    cc.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                    catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = new ArrayList<>();
                    finallyCommands.add(command(inFunction, inMethod, forinlevel, inTellTarget, true, variables, functions, hasEval));
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                ret = new TryActionItem(tryCommands, catchExceptionNames, catchExceptionTypes, catchCommands, finallyCommands);
                break;
            case THROW:
                ret = new ThrowActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                break;
            case SEMICOLON: //empty command
                if (debugMode) {
                    System.out.println("/command");
                }
                return new EmptyCommand();
            case DIRECTIVE:
                switch((String)s.value) {
                    case "strict":
                        ret = new StrictModeActionItem(null, null, 1);
                        break;
                    default:
                        throw new ActionParseException("Unknown directive: #" + s.value, lexer.yyline());
                }
                break;
            default:
                lexer.pushback(s);
                ret = expression(inFunction, inMethod, inTellTarget, true, variables, functions, true, hasEval);
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (ret == null) {  //can be popped expression
            buf.pushAllBack(lexer);
            ret = expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableActionItem> variables, List<FunctionActionItem> functions, boolean allowComma, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("expression:");
        }
        List<GraphTargetItem> commaItems = new ArrayList<>();
        ParsedSymbol symb;
        do {
            GraphTargetItem prim = expressionPrimary(false, inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, true, hasEval);
            if (prim == null) {
                return null;
            }
            GraphTargetItem expr = expression1(prim, GraphTargetItem.NOPRECEDENCE, inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, hasEval);
            commaItems.add(expr);
            symb = lex();
        } while (allowComma && symb != null && symb.type == SymbolType.COMMA);
        if (symb != null) {
            lexer.pushback(symb);
        }
        if (debugMode) {
            System.out.println("/expression");
        }
        if (commaItems.size() == 1) {
            return commaItems.get(0);
        }
        return new CommaExpressionItem(null, null, commaItems);
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

    private GraphTargetItem expression1(GraphTargetItem lhs, int min_precedence, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol op;
        GraphTargetItem rhs;
        GraphTargetItem mhs = null;
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
                mhs = expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, false, hasEval);
                expectedType(SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(allowRemainder, inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, true, hasEval);
            if (rhs == null) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekLex();
            while ((isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) < /* > on wiki */ getSymbPrecedence(op))
                    || (lookahead.type.isRightAssociative() && getSymbPrecedence(lookahead) == getSymbPrecedence(op))) {
                rhs = expression1(rhs, getSymbPrecedence(lookahead), inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, hasEval);
                lookahead = peekLex();
            }

            switch (op.type) {

                case TERNAR:
                    lhs = new TernarOpItem(null, null, lhs, mhs, rhs);
                    break;
                case SHIFT_LEFT:
                    lhs = new LShiftActionItem(null, null, lhs, rhs);
                    break;
                case SHIFT_RIGHT:
                    lhs = new RShiftActionItem(null, null, lhs, rhs);
                    break;
                case USHIFT_RIGHT:
                    lhs = new URShiftActionItem(null, null, lhs, rhs);
                    break;
                case BITAND:
                    lhs = new BitAndActionItem(null, null, lhs, rhs);
                    break;
                case BITOR:
                    lhs = new BitOrActionItem(null, null, lhs, rhs);
                    break;
                case DIVIDE:
                    lhs = new DivideActionItem(null, null, lhs, rhs);
                    break;
                case MODULO:
                    lhs = new ModuloActionItem(null, null, lhs, rhs);
                    break;
                case EQUALS:
                    lhs = new EqActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case STRICT_EQUALS:
                    lhs = new StrictEqActionItem(null, null, lhs, rhs);
                    break;
                case NOT_EQUAL:
                    lhs = new NeqActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case STRICT_NOT_EQUAL:
                    lhs = new StrictNeqActionItem(null, null, lhs, rhs);
                    break;
                case LOWER_THAN:
                    lhs = new LtActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case LOWER_EQUAL:
                    lhs = new LeActionItem(null, null, lhs, rhs);
                    break;
                case GREATER_THAN:
                    lhs = new GtActionItem(null, null, lhs, rhs);
                    break;
                case GREATER_EQUAL:
                    lhs = new GeActionItem(null, null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case AND:
                    lhs = new AndItem(null, null, lhs, rhs);
                    break;
                case OR:
                    lhs = new OrItem(null, null, lhs, rhs);
                    break;
                case FULLAND:
                    lhs = new AndActionItem(null, null, lhs, rhs);
                    break;
                case FULLOR:
                    lhs = new OrActionItem(null, null, lhs, rhs);
                    break;
                case MINUS:
                    lhs = new SubtractActionItem(null, null, lhs, rhs);
                    break;
                case MULTIPLY:
                    lhs = new MultiplyActionItem(null, null, lhs, rhs);
                    break;
                case PLUS:
                    lhs = new AddActionItem(null, null, lhs, rhs, swfVersion >= 5);
                    break;
                case XOR:
                    lhs = new BitXorActionItem(null, null, lhs, rhs);
                    break;
                case INSTANCEOF:
                    lhs = new InstanceOfActionItem(null, null, lhs, rhs);
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
                    }
                    break;
                case IDENTIFIER:
                    switch (op.value.toString()) {
                        case "add":
                            lhs = new StringAddActionItem(null, null, lhs, rhs);
                            break;
                        case "eq":
                            lhs = new StringEqActionItem(null, null, lhs, rhs);
                            break;
                        case "ne":
                            lhs = new StringNeActionItem(null, null, lhs, rhs);
                            break;
                        case "lt":
                            lhs = new StringLtActionItem(null, null, lhs, rhs);
                            break;
                        case "ge":
                            lhs = new StringGeActionItem(null, null, lhs, rhs);
                            break;
                        case "gt":
                            lhs = new StringGtActionItem(null, null, lhs, rhs);
                            break;
                        case "le":
                            lhs = new StringLeActionItem(null, null, lhs, rhs);
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

    private boolean isType(GraphTargetItem item) {
        if (item == null) {
            return false;
        }
        while (item instanceof GetMemberActionItem) {
            item = ((GetMemberActionItem) item).object;
        }
        return (item instanceof VariableActionItem);
    }

    private int brackets(List<GraphTargetItem> ret, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableActionItem> variables, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
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

    private GraphTargetItem handleVariable(ParsedSymbol s, GraphTargetItem ret, List<VariableActionItem> variables, Reference<Boolean> allowMemberOrCall, boolean inFunction, boolean inMethod, boolean inTellTarget, List<FunctionActionItem> functions, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (s.value.equals("not")) {
            ret = new NotItem(null, null, expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval));
        } else {
            String varName = s.value.toString();
            /*if (s.type == SymbolType.PATH) { //only with slash syntax
                ParsedSymbol s2 = lex();
                while (s2.type == SymbolType.COLON) {
                    s2 = lex();
                    expected(s2, lexer.yyline(), SymbolType.IDENTIFIER);
                    varName += ":" + s2.value.toString();
                    s2 = lex();
                }
                lexer.pushback(s2);
            }*/

            ret = new VariableActionItem(varName, null, false);
            variables.add((VariableActionItem) ret);
            allowMemberOrCall.setVal(true);
        }
        return ret;
    }

    private GraphTargetItem expressionPrimary(boolean allowEmpty, boolean inFunction, boolean inMethod, boolean inTellTarget, boolean allowRemainder, List<VariableActionItem> variables, List<FunctionActionItem> functions, boolean allowCall, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean allowMemberOrCall = false;
        GraphTargetItem ret = null;
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
                        ret = new UnresolvedConstantActionItem((int) (long) (Long) s.value);
                        break;
                    case "enumerate":
                        ret = new EnumerateActionItem(null, null, expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, false, hasEval));
                        break;
                    //Both ASs
                    case "dup":
                        ret = new DuplicateItem(null, null, expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, false, hasEval));
                        break;
                    case "push":
                        ret = new PushItem(expression(inFunction, inMethod, inTellTarget, allowRemainder, variables, functions, false, hasEval));
                        break;
                    case "pop":
                        ret = new PopItem(null, null);
                        break;
                    case "strict":
                        s = lexer.lex();
                        expected(s, lexer.yyline(), SymbolType.INTEGER);
                        ret = new StrictModeActionItem(null, null, (int) (long) (Long) s.value);
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
                ret = expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval);
                ret = new BitXorActionItem(null, null, ret, new DirectValueActionItem(4.294967295E9));

                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new DirectValueActionItem(null, null, 0, -(double) (Double) s.value, new ArrayList<>());

                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new DirectValueActionItem(null, null, 0, -(long) (Long) s.value, new ArrayList<>());

                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expressionPrimary(false, inFunction, inMethod, inTellTarget, true, variables, functions, true, hasEval);
                    if ((num instanceof DirectValueActionItem)
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
                    }
                }
                break;
            case TYPEOF:
                ret = new TypeOfActionItem(null, null, expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval));
                allowMemberOrCall = true;
                break;
            case TRUE:
                ret = new DirectValueActionItem(null, null, 0, Boolean.TRUE, new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case NULL:
                ret = new DirectValueActionItem(null, null, 0, Null.INSTANCE, new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case UNDEFINED:
                ret = new DirectValueActionItem(null, null, 0, Undefined.INSTANCE, new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case FALSE:
                ret = new DirectValueActionItem(null, null, 0, Boolean.FALSE, new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                List<GraphTargetItem> objectNames = new ArrayList<>();
                List<GraphTargetItem> objectValues = new ArrayList<>();
                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expectedIdentifier(s, lexer.yyline());
                    objectNames.add(0, pushConst((String) s.value));
                    expectedType(SymbolType.COLON);
                    objectValues.add(0, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new InitObjectActionItem(null, null, objectNames, objectValues);
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(inBrackets, inFunction, inMethod, inTellTarget, variables, functions, hasEval);
                ret = new InitArrayActionItem(null, null, inBrackets);
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
                ret = function(true, fname, false, variables, functions, inTellTarget, hasEval);
                allowMemberOrCall = true;
                break;
            case STRING:
                ret = pushConst(s.value.toString());
                allowMemberOrCall = true;
                break;
            case NEWLINE:
                ret = new DirectValueActionItem(null, null, 0, "\n", new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case INTEGER:
            case DOUBLE:
                ret = new DirectValueActionItem(null, null, 0, s.value, new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case DELETE:
                GraphTargetItem varDel = expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval);
                if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, null, gm.object, gm.memberName);
                } else {
                    if (varDel instanceof VariableActionItem) {
                        varDel = pushConst(((VariableActionItem) varDel).getVariableName());
                    }
                    ret = new DeleteActionItem(null, null, null, varDel);
                }
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem prevar = expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, null, prevar);
                }

                break;
            case NOT:
                ret = new NotItem(null, null, expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, true, hasEval));

                break;
            case PARENT_OPEN:
                GraphTargetItem pexpr = expression(inFunction, inMethod, inTellTarget, true, variables, functions, true, hasEval);
                if (pexpr == null) {
                    throw new ActionParseException("Expression expected", lexer.yyline());
                }
                ret = new ParenthesisItem(null, null, pexpr);
                expectedType(SymbolType.PARENT_CLOSE);
                allowMemberOrCall = true;
                break;
            case NEW:
                GraphTargetItem newvar = expressionPrimary(false, inFunction, inMethod, inTellTarget, false, variables, functions, false, hasEval);
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
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, functions, hasEval);
                    ret = new NewMethodActionItem(null, null, ca.object, ca.memberName, args);
                } else if (newvar instanceof VariableActionItem) {
                    VariableActionItem cf = (VariableActionItem) newvar;
                    expectedType(SymbolType.PARENT_OPEN);
                    List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, functions, hasEval);
                    ret = new NewObjectActionItem(null, null, pushConst(cf.getVariableName()), args);
                } else {
                    throw new ActionParseException("Invalid new item", lexer.yyline());
                }
                allowMemberOrCall = true;

                break;
            case EVAL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem evar = new EvalActionItem(null, null, expression(inFunction, inMethod, inTellTarget, true, variables, functions, false, hasEval));
                expectedType(SymbolType.PARENT_CLOSE);
                hasEval.setVal(true);
                //evar = memberOrCall(evar, inFunction, inMethod, variables, functions);
                ret = evar;
                allowMemberOrCall = true;

                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                Reference<Boolean> allowMemberOrCallRef = new Reference<>(allowMemberOrCall);
                ret = handleVariable(s, ret, variables, allowMemberOrCallRef, inFunction, inMethod, inTellTarget, functions, hasEval);
                allowMemberOrCall = allowMemberOrCallRef.getVal();

                break;
            default:

                boolean isGlobalFuncVar = false;
                if (s.group == SymbolGroup.GLOBALFUNC) {
                    ParsedSymbol s2 = peekLex();
                    if (s2.type != SymbolType.PARENT_OPEN) {
                        Reference<Boolean> allowMemberOrCallRef2 = new Reference<>(allowMemberOrCall);
                        ret = handleVariable(s, ret, variables, allowMemberOrCallRef2, inFunction, inMethod, inTellTarget, functions, hasEval);
                        allowMemberOrCall = allowMemberOrCallRef2.getVal();
                        isGlobalFuncVar = true;
                    }
                }

                if (!isGlobalFuncVar) {
                    GraphTargetItem excmd = expressionCommands(s, inFunction, inMethod, inTellTarget, -1, variables, functions, hasEval);
                    if (excmd != null) {
                        //?
                        ret = excmd;
                        allowMemberOrCall = true; //?
                        break;
                    }
                    lexer.pushback(s);
                }
        }

        if (allowMemberOrCall && ret != null) {
            ret = memberOrCall(ret, inFunction, inMethod, inTellTarget, variables, functions, allowCall, hasEval);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

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
    }

    private GraphTargetItem memberOrCall(GraphTargetItem ret, boolean inFunction, boolean inMethod, boolean inTellTarget, List<VariableActionItem> variables, List<FunctionActionItem> functions, boolean allowCall, Reference<Boolean> hasEval) throws IOException, ActionParseException, InterruptedException {
        ParsedSymbol op = lex();
        while (op.isType(SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.DOT)) {
            if (op.type == SymbolType.PARENT_OPEN) {
                if (!allowCall) {
                    break;
                }
                List<GraphTargetItem> args = call(inFunction, inMethod, inTellTarget, variables, functions, hasEval);
                if (isCastOp(ret) && args.size() == 1) {
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
                }
            }
            if (op.type == SymbolType.BRACKET_OPEN) {
                GraphTargetItem rhs = expression(inFunction, inMethod, inTellTarget, false, variables, functions, false, hasEval);
                ret = new GetMemberActionItem(null, null, ret, rhs);
                expectedType(SymbolType.BRACKET_CLOSE);
            }
            if (op.type == SymbolType.DOT) {
                ParsedSymbol s = lex();
                expectedIdentifier(s, lexer.yyline(), SymbolType.THIS, SymbolType.SUPER);

                ret = new GetMemberActionItem(null, null, ret, pushConst(s.value.toString()));
            }
            op = lex();
        }

        switch (op.type) {
            case INCREMENT: //postincrement
                if (!(ret instanceof VariableActionItem) && !(ret instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostIncrementActionItem(null, null, ret);
                op = lex();
                break;
            case DECREMENT: //postdecrement
                if (!(ret instanceof VariableActionItem) && !(ret instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostDecrementActionItem(null, null, ret);
                op = lex();
                break;
        }

        lexer.pushback(op);
        return ret;
    }

    private DirectValueActionItem pushConst(String s) throws IOException, ActionParseException {
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
     * @param str The string to convert
     * @param constantPool The constant pool to use
     * @return The high-level model
     * @throws ActionParseException On parse error
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public List<GraphTargetItem> treeFromString(String str, List<String> constantPool) throws ActionParseException, IOException, InterruptedException {
        List<GraphTargetItem> retTree = new ArrayList<>();
        this.constantPool = constantPool;
        lexer = new ActionScriptLexer(new StringReader(str));
        if (swfVersion >= ActionScriptLexer.SWF_VERSION_CASE_SENSITIVE) {
            lexer.setCaseSensitiveIdentifiers(true);
        }

        BUTTONCONDACTION newButtonCond = new BUTTONCONDACTION();

        if (targetSource instanceof BUTTONCONDACTION) {
            ParsedSymbol symb = lexer.lex();
            if (symb.type != SymbolType.IDENTIFIER || !"on".equals(symb.value)) {
                throw new ActionParseException("on keyword expected but " + symb + " found", lexer.yyline());
            }
            expectedType(SymbolType.PARENT_OPEN);
            symb = lexer.lex();
            boolean condEmpty = true;
            while (symb.type == SymbolType.IDENTIFIER) {
                condEmpty = false;
                switch ((String) symb.value) {
                    case "press":
                        newButtonCond.condOverUpToOverDown = true;
                        break;
                    case "release":
                        newButtonCond.condOverDownToOverUp = true;
                        break;
                    case "releaseOutside":
                        newButtonCond.condOutDownToIdle = true;
                        break;
                    case "rollOver":
                        newButtonCond.condIdleToOverUp = true;
                        break;
                    case "rollOut":
                        newButtonCond.condOverUpToIddle = true;
                        break;
                    case "dragOut":
                        newButtonCond.condOverDownToOutDown = true;
                        break;
                    case "dragOver":
                        newButtonCond.condOutDownToOverDown = true;
                        break;
                    case "keyPress":
                        symb = lexer.lex();
                        expected(symb, lexer.yyline(), SymbolType.STRING);
                        Integer key = CLIPACTIONRECORD.stringToKey((String) symb.value);
                        if (key == null) {
                            throw new ActionParseException("Invalid key", lexer.yyline());
                        }
                        newButtonCond.condKeyPress = key;
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
        }

        CLIPEVENTFLAGS newClipEventFlags = new CLIPEVENTFLAGS();
        int newClipActionRecordKey = 0;
        if (targetSource instanceof CLIPACTIONRECORD) {
            ParsedSymbol symb = lexer.lex();
            if (symb.type != SymbolType.IDENTIFIER || (!"on".equals(symb.value) && !"onClipEvent".equals(symb.value))) {
                throw new ActionParseException("on or onClipEvent keyword expected but " + symb + " found", lexer.yyline());
            }
            expectedType(SymbolType.PARENT_OPEN);
            if ("on".equals(symb.value)) {
                symb = lexer.lex();
                boolean condEmpty = true;
                while (symb.type == SymbolType.IDENTIFIER) {
                    condEmpty = false;
                    switch ((String) symb.value) {
                        case "press":
                            newClipEventFlags.clipEventPress = true;
                            break;
                        case "release":
                            newClipEventFlags.clipEventRelease = true;
                            break;
                        case "releaseOutside":
                            newClipEventFlags.clipEventReleaseOutside = true;
                            break;
                        case "rollOver":
                            newClipEventFlags.clipEventRollOver = true;
                            break;
                        case "rollOut":
                            newClipEventFlags.clipEventRollOut = true;
                            break;
                        case "dragOut":
                            newClipEventFlags.clipEventDragOut = true;
                            break;
                        case "dragOver":
                            newClipEventFlags.clipEventDragOver = true;
                            break;
                        case "initialize":
                            newClipEventFlags.clipEventInitialize = true;
                            break;
                        case "construct":
                            newClipEventFlags.clipEventConstruct = true;
                            break;

                        case "keyPress":
                            symb = lexer.lex();
                            expected(symb, lexer.yyline(), SymbolType.STRING);
                            Integer key = CLIPACTIONRECORD.stringToKey((String) symb.value);
                            if (key == null) {
                                throw new ActionParseException("Invalid key", lexer.yyline());
                            }
                            newClipActionRecordKey = key;
                            newClipEventFlags.clipEventKeyPress = true;
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
            } else if ("onClipEvent".equals(symb.value)) {
                symb = lexer.lex();
                expected(symb, lexer.yyline(), SymbolType.IDENTIFIER);

                switch ((String) symb.value) {
                    case "keyUp":
                        newClipEventFlags.clipEventKeyUp = true;
                        break;
                    case "keyDown":
                        newClipEventFlags.clipEventKeyDown = true;
                        break;
                    case "mouseUp":
                        newClipEventFlags.clipEventMouseUp = true;
                        break;
                    case "mouseDown":
                        newClipEventFlags.clipEventMouseDown = true;
                        break;
                    case "mouseMove":
                        newClipEventFlags.clipEventMouseMove = true;
                        break;
                    case "unload":
                        newClipEventFlags.clipEventUnload = true;
                        break;
                    case "enterFrame":
                        newClipEventFlags.clipEventEnterFrame = true;
                        break;
                    case "load":
                        newClipEventFlags.clipEventLoad = true;
                        break;
                    case "data":
                        newClipEventFlags.clipEventData = true;
                        break;
                    default:
                        throw new ActionParseException("Unrecognized clipEvent type", lexer.yyline());
                }
                expectedType(SymbolType.PARENT_CLOSE);
            }
            expectedType(SymbolType.CURLY_OPEN);
        }

        List<VariableActionItem> vars = new ArrayList<>();
        List<FunctionActionItem> functions = new ArrayList<>();
        Reference<Boolean> hasEval = new Reference<>(false);
        retTree.addAll(commands(false, false, 0, false, vars, functions, hasEval));
        for (VariableActionItem v : vars) {
            String varName = v.getVariableName();
            GraphTargetItem stored = v.getStoreValue();
            int propIndex = -1;
            boolean hasSubVars = false;
            propIndex = Action.propertyNamesListLowerCase.indexOf(varName.toLowerCase());
            if (v.isDefinition()) {
                if (hasSubVars) {
                    throw new ActionParseException("Invalid : character in variable definition", lexer.yyline());
                }
                v.setBoxedValue(new DefineLocalActionItem(null, null, pushConst(varName), stored));
            } else if (stored != null) {
                if (propIndex > -1) {
                    v.setBoxedValue(new SetPropertyActionItem(null, null, pushConst(""), propIndex, stored));
                } else {
                    v.setBoxedValue(new SetVariableActionItem(null, null, pushConst(varName), stored));
                }

            } else if (propIndex > -1) {
                v.setBoxedValue(new GetPropertyActionItem(null, null, pushConst(""), propIndex));
            } else {
                v.setBoxedValue(new GetVariableActionItem(null, null, pushConst(varName)));
            }
        }

        if ((targetSource instanceof BUTTONCONDACTION) || (targetSource instanceof CLIPACTIONRECORD)) {
            expectedType(SymbolType.CURLY_CLOSE);
        }

        if (lexer.lex().type != SymbolType.EOF) {
            throw new ActionParseException("Parsing finished before end of the file", lexer.yyline());
        }
        if (targetSource instanceof BUTTONCONDACTION) {
            BUTTONCONDACTION targetButtonCond = (BUTTONCONDACTION) targetSource;
            targetButtonCond.condIdleToOverDown = newButtonCond.condIdleToOverDown;
            targetButtonCond.condIdleToOverUp = newButtonCond.condIdleToOverUp;
            targetButtonCond.condOutDownToIdle = newButtonCond.condOutDownToIdle;
            targetButtonCond.condOutDownToOverDown = newButtonCond.condOutDownToOverDown;
            targetButtonCond.condOverDownToIdle = newButtonCond.condOverDownToIdle;
            targetButtonCond.condOverDownToOutDown = newButtonCond.condOverDownToOutDown;
            targetButtonCond.condOverDownToOverUp = newButtonCond.condOverDownToOverUp;
            targetButtonCond.condOverUpToIddle = newButtonCond.condOverUpToIddle;
            targetButtonCond.condOverUpToOverDown = newButtonCond.condOverUpToOverDown;
            targetButtonCond.condKeyPress = newButtonCond.condKeyPress;
        }

        if (targetSource instanceof CLIPACTIONRECORD) {
            CLIPACTIONRECORD targetClipActionRecord = (CLIPACTIONRECORD) targetSource;
            targetClipActionRecord.eventFlags = newClipEventFlags;
            targetClipActionRecord.keyCode = newClipActionRecordKey;
            targetClipActionRecord.getParentClipActions().calculateAllEventFlags();
        }
        return retTree;
    }

    private List<GraphSourceItem> generateActionList(List<GraphTargetItem> tree, List<String> constantPool, boolean secondRun) throws CompilationException {
        ActionSourceGenerator gen = new ActionSourceGenerator(swfVersion, constantPool, charset);
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(new HashMap<>(), 0, Boolean.FALSE, 0);
        localData.secondRun = secondRun;
        return gen.generate(localData, tree);
    }

    private List<Action> actionsFromTree(List<GraphTargetItem> tree, List<String> constantPool, boolean doOrder, String charset) throws CompilationException, NeedsGenerateAgainException {
        List<Action> ret = new ArrayList<>();

        List<GraphSourceItem> srcList = generateActionList(tree, constantPool, doOrder == false);

        if (doOrder) {
            List<String> orderedConstantPool = new ArrayList<>();
            boolean canChangeInPlace;
            int lastIndex = constantPool.size() - 1;
            if (lastIndex <= ActionPush.MAX_CONSTANT_INDEX_TYPE8) {
                //can change constant indices as ActionPush contains always 1 byte per constant
                canChangeInPlace = true;
            } else {
                //variable number bytes per ActionPush constant,
                //must generate again to make relative offsets in jumps work
                canChangeInPlace = false;
            }

            //create ordered constant pool, update constantindices when we can changeinplace
            for (GraphSourceItem src : srcList) {
                if (src instanceof ActionPush) {
                    ActionPush ap = (ActionPush) src;
                    for (int i = 0; i < ap.values.size(); i++) {
                        Object val = ap.values.get(i);
                        if (val instanceof ConstantIndex) {
                            ConstantIndex ci = (ConstantIndex) val;
                            String cval = constantPool.get(ci.index);
                            int orderedIndex = orderedConstantPool.indexOf(cval);
                            if (orderedIndex == -1) {
                                orderedIndex = orderedConstantPool.size();
                                orderedConstantPool.add(cval);
                            }
                            if (canChangeInPlace) {
                                //Do NOT change ci.index directly - it may be cloned from other location
                                ap.values.set(i, new ConstantIndex(orderedIndex));
                            }
                        }
                    }
                }
            }
            if (!canChangeInPlace) {
                //generate again, as number of bytes per ActionPush can change
                throw new NeedsGenerateAgainException(orderedConstantPool);
            }
            constantPool = orderedConstantPool;
        }
        for (GraphSourceItem s : srcList) {
            if (s instanceof Action) {
                ret.add((Action) s);
            }
        }
        ret.add(0, new ActionConstantPool(constantPool, charset));
        return ret;
    }

    /**
     * Converts a string to a list of actions.
     * @param s The string to convert
     * @param charset Charset
     * @return List of actions
     * @throws ActionParseException On parsing error
     * @throws IOException On I/O error
     * @throws CompilationException On compilation error
     * @throws InterruptedException On interrupt
     */
    public List<Action> actionsFromString(String s, String charset) throws ActionParseException, IOException, CompilationException, InterruptedException {
        try {
            List<String> constantPool = new ArrayList<>();
            List<GraphTargetItem> tree = treeFromString(s, constantPool);
            return actionsFromTree(tree, constantPool, true, charset);
        } catch (NeedsGenerateAgainException nga) {
            //Can happen when constantpool needs reordering and number of constants > 256
            try {
                List<String> newConstantPool = nga.getNewConstantPool();
                List<GraphTargetItem> tree = treeFromString(s, newConstantPool);
                return actionsFromTree(tree, newConstantPool, false /*do not order again*/, charset);
            } catch (NeedsGenerateAgainException ex) {
                //should not happen as doOrder parameter is set to false
                return new ArrayList<>();
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
