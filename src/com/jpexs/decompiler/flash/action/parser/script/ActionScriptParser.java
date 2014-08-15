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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.AsciiToCharActionItem;
import com.jpexs.decompiler.flash.action.model.CallActionItem;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.CastOpActionItem;
import com.jpexs.decompiler.flash.action.model.CharToAsciiActionItem;
import com.jpexs.decompiler.flash.action.model.CloneSpriteActionItem;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DeleteActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EvalActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommandActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
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
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StartDragActionItem;
import com.jpexs.decompiler.flash.action.model.StopActionItem;
import com.jpexs.decompiler.flash.action.model.StopAllSoundsActionItem;
import com.jpexs.decompiler.flash.action.model.StopDragActionItem;
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
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TryActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.WithActionItem;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
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
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BinaryOp;
import com.jpexs.decompiler.graph.model.BlockItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
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

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    private final int swfVersion;

    public ActionScriptParser(int swfVersion) {
        this.swfVersion = swfVersion;
    }

    private long uniqLast = 0;
    private final boolean debugMode = false;

    private String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    private List<GraphTargetItem> commands(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<VariableActionItem> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(registerVars, inFunction, inMethod, forinlevel, true, variables)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;

        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP);
        ret = new VariableActionItem(s.value.toString(), null, false);
        variables.add((VariableActionItem) ret);
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP);
            ret = new GetMemberActionItem(null, ret, pushConst(s.value.toString()));
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem memberOrCall(GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN, SymbolType.PARENT_OPEN)) {
            switch (s.type) {
                case DOT:
                case BRACKET_OPEN:
                    lexer.pushback(s);
                    ret = member(ret, registerVars, inFunction, inMethod, variables);
                    break;
                case PARENT_OPEN:
                    if (ret instanceof GetMemberActionItem) {
                        GetMemberActionItem mem = (GetMemberActionItem) ret;
                        ret = new CallMethodActionItem(null, mem.object, mem.memberName, call(registerVars, inFunction, inMethod, variables));
                    } else if (ret instanceof VariableActionItem) {
                        VariableActionItem var = (VariableActionItem) ret;
                        ret = new CallFunctionActionItem(null, pushConst(var.getVariableName()), call(registerVars, inFunction, inMethod, variables));
                    } else {
                        ret = new CallFunctionActionItem(null, ret, call(registerVars, inFunction, inMethod, variables));
                    }
                    break;
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem member(GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN)) {
            if (s.type == SymbolType.BRACKET_OPEN) {
                ret = new GetMemberActionItem(null, ret, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.BRACKET_CLOSE);
                s = lex();
                continue;
            }
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolGroup.GLOBALFUNC, SymbolGroup.GLOBALCONST);
            //TODO: Handle properties (?)
            if (false) {//GraphTargetItem.propertyNamesList.contains(s.value.toString())) {
                //ret.add(new ActionPush((Long) (long) (int) GraphTargetItem.propertyNamesList.indexOf(s.value.toString())));
                //ret.add(new ActionGetProperty());
            } else {
                ret = new GetMemberActionItem(null, ret, pushConst(s.value.toString()));
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem variable(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
        ret = new VariableActionItem(s.value.toString(), null, false);
        variables.add((VariableActionItem) ret);
        ret = (member(ret, registerVars, inFunction, inMethod, variables));
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

    private List<GraphTargetItem> call(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(registerVars, inFunction, inMethod, true, variables));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private FunctionActionItem function(boolean withBody, String functionName, boolean isMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = null;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
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
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);

            body = commands(new HashMap<String, Integer>(), true, isMethod, 0, subvariables);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        return new FunctionActionItem(null, functionName, paramNames, body, constantPool, -1, subvariables);
    }

    private GraphTargetItem traits(boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<VariableActionItem> variables) throws IOException, ParseException {

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

        ParsedSymbol s = null;
        FunctionActionItem constr = null;
        List<GraphTargetItem> staticFunctions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars = new ArrayList<>();
        List<GraphTargetItem> functions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> vars = new ArrayList<>();

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
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC);
                    String fname = s.value.toString();
                    if (fname.equals(classNameStr)) { //constructor
                        constr = (function(!isInterface, "", true, variables));
                    } else {
                        if (!isInterface) {
                            if (isStatic) {
                                FunctionActionItem ft = function(!isInterface, "", true, variables);
                                ft.calculatedFunctionName = pushConst(fname);
                                staticFunctions.add(ft);
                            } else {
                                FunctionActionItem ft = function(!isInterface, "", true, variables);
                                ft.calculatedFunctionName = pushConst(fname);
                                functions.add(ft);
                            }
                        }
                    }
                    break;
                case VAR:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String ident = s.value.toString();
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        if (isStatic) {
                            staticVars.add(new MyEntry<GraphTargetItem, GraphTargetItem>(pushConst(ident), expression(new HashMap<String, Integer>(), false, false, true, variables)));
                        } else {
                            vars.add(new MyEntry<GraphTargetItem, GraphTargetItem>(pushConst(ident), expression(new HashMap<String, Integer>(), false, false, true, variables)));
                        }
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
            return new ClassActionItem(nameStr, extendsStr, implementsStr, constr, functions, vars, staticFunctions, staticVars);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            case GETVERSION:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new GetVersionActionItem(null);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBCharToAsciiActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBCHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBAsciiToCharActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBSUBSTRING:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val1 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index1 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len1 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new MBStringExtractActionItem(null, val1, index1, len1);
                break;
            case SUBSTR:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val2 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index2 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len2 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StringExtractActionItem(null, val2, index2, len2);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case RANDOM:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RandomNumberActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case INT:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new ToIntegerActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NUMBER_OP:
                s = lex();
                if (s.type == SymbolType.DOT) {
                    VariableActionItem vi = new VariableActionItem(s.value.toString(), null, false);
                    variables.add(vi);
                    ret = memberOrCall(vi, registerVars, inFunction, inMethod, variables);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToNumberActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
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
                    ret = memberOrCall(vi2, registerVars, inFunction, inMethod, variables);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToStringActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    ret = memberOrCall(ret, registerVars, inFunction, inMethod, variables);
                }
                break;
            case ORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CharToAsciiActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new AsciiToCharActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case DUPLICATEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem src3 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem tar3 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem dep3 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new CloneSpriteActionItem(null, src3, tar3, dep3);
                break;
            case GETTIMER:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetTimeActionItem(null);
                break;
            default:
                return null;
        }
        return ret;
    }

    private GraphTargetItem command(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<VariableActionItem> variables) throws IOException, ParseException {
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
        switch (s.type) {
            case FSCOMMAND:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new FSCommandActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CALL:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CallActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case SET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem name1 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem value1 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new SetVariableActionItem(null, name1, value1);
                break;
            case WITH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem wvar = (variable(registerVars, inFunction, inMethod, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> wcmd = commands(registerVars, inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new WithActionItem(null, wvar, wcmd);
                break;
            case DELETE:
                GraphTargetItem varDel = variable(registerVars, inFunction, inMethod, variables);
                if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, gm.object, gm.memberName);
                } else if (varDel instanceof VariableActionItem) {
                    variables.remove(varDel);
                    ret = new DeleteActionItem(null, null, pushConst(((VariableActionItem) varDel).getVariableName()));
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case TRACE:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TraceActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case GETURL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url = (expression(registerVars, inFunction, inMethod, true, variables));
                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int getuMethod = 1;
                GraphTargetItem target = null;
                if (s.type == SymbolType.COMMA) {
                    target = (expression(registerVars, inFunction, inMethod, true, variables));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        if (s.value.equals("GET")) {
                            getuMethod = 1;
                        } else if (s.value.equals("POST")) {
                            getuMethod = 2;
                        } else {
                            throw new ParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                    target = new DirectValueActionItem(null, 0, "", new ArrayList<String>());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetURL2ActionItem(null, url, target, getuMethod);
                break;
            case GOTOANDSTOP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtsFrame = expression(registerVars, inFunction, inMethod, true, variables);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    s = lex();
                    gtsFrame = expression(registerVars, inFunction, inMethod, true, variables);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2ActionItem(null, gtsFrame, false, false, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NEXTFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new NextFrameActionItem(null);
                break;
            case PLAY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PlayActionItem(null);
                break;
            case PREVFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PrevFrameActionItem(null);
                break;
            case TELLTARGET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem tellTarget = expression(registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> tellcmds = commands(registerVars, inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new TellTargetActionItem(null, tellTarget, tellcmds);
                break;
            case STOP:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopActionItem(null);
                break;
            case STOPALLSOUNDS:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopAllSoundsActionItem(null);
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new ToggleHighQualityActionItem(null);
                break;

            case STOPDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopDragActionItem(null);
                break;

            case TARGETPATH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TargetPathActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                SymbolType unloadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem unTargetOrNum = expression(registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.PARENT_CLOSE);
                if (unloadType == SymbolType.UNLOADMOVIE) {
                    ret = new UnLoadMovieActionItem(null, unTargetOrNum);
                }
                if (unloadType == SymbolType.UNLOADMOVIENUM) {
                    ret = new UnLoadMovieNumActionItem(null, unTargetOrNum);
                }
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                SymbolType printType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem printTarget = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem printBBox = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);

                switch (printType) {
                    case PRINT:
                        ret = new PrintActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTNUM:
                        ret = new PrintNumActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAP:
                        ret = new PrintAsBitmapActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAPNUM:
                        ret = new PrintAsBitmapNumActionItem(null, printTarget, printBBox);
                        break;
                }
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                SymbolType loadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url2 = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem targetOrNum = (expression(registerVars, inFunction, inMethod, true, variables));

                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int lvmethod = 1;
                if (s.type == SymbolType.COMMA) {
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.STRING);
                    if (s.value.equals("POST")) {
                        lvmethod = 2;
                    } else if (s.value.equals("GET")) {
                        lvmethod = 1;
                    } else {
                        throw new ParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                switch (loadType) {
                    case LOADVARIABLES:
                        ret = new LoadVariablesActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIE:
                        ret = new LoadMovieActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADVARIABLESNUM:
                        ret = new LoadVariablesNumActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIENUM:
                        ret = new LoadMovieNumActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                }
                break;
            case GOTOANDPLAY:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtpFrame = expression(registerVars, inFunction, inMethod, true, variables);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?                    
                    s = lex();
                    gtpFrame = expression(registerVars, inFunction, inMethod, true, variables);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2ActionItem(null, gtpFrame, false, true, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case REMOVEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RemoveSpriteActionItem(null, (expression(registerVars, inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case STARTDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem dragTarget = (expression(registerVars, inFunction, inMethod, true, variables));
                GraphTargetItem lockCenter = null;
                GraphTargetItem constrain = null;
                GraphTargetItem x1 = null;
                GraphTargetItem y1 = null;
                GraphTargetItem x2 = null;
                GraphTargetItem y2 = null;
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    lockCenter = (expression(registerVars, inFunction, inMethod, true, variables));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        constrain = new DirectValueActionItem(null, 0, new Long(1), new ArrayList<String>());
                        x1 = (expression(registerVars, inFunction, inMethod, true, variables));
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            y1 = (expression(registerVars, inFunction, inMethod, true, variables));
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                x2 = (expression(registerVars, inFunction, inMethod, true, variables));
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    y2 = (expression(registerVars, inFunction, inMethod, true, variables));
                                } else {
                                    lexer.pushback(s);
                                    y2 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());
                                }
                            } else {
                                lexer.pushback(s);
                                x2 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());
                                y2 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());
                            }
                        } else {
                            lexer.pushback(s);
                            x2 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());
                            y2 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());
                            y1 = new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>());

                        }
                    } else {
                        lexer.pushback(s);
                        constrain = new DirectValueActionItem(null, 0, new Long(0), new ArrayList<String>());
                        //ret.add(new ActionPush(Boolean.FALSE));
                    }
                } else {
                    lockCenter = new DirectValueActionItem(null, 0, new Long(0), new ArrayList<String>());
                    constrain = new DirectValueActionItem(null, 0, new Long(0), new ArrayList<String>());
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StartDragActionItem(null, dragTarget, lockCenter, constrain, x1, y1, x2, y2);
                break;

            case IFFRAMELOADED:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem iflExpr = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> iflComs = commands(registerVars, inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new IfFrameLoadedActionItem(iflExpr, iflComs, null);
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
                ret = (traits(false, classTypeStr, extendsTypeStr, implementsTypeStrs, variables));
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
                ret = (traits(true, interfaceTypeStr, null, intExtendsTypeStrs, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s = lexer.lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC);
                ret = (function(true, s.value.toString(), false, variables));
                break;
            case VAR:
                s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                String varIdentifier = s.value.toString();
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type(variables);
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    GraphTargetItem varval = (expression(registerVars, inFunction, inMethod, true, variables));
                    ret = new VariableActionItem(varIdentifier, varval, true);
                    variables.add((VariableActionItem) ret);
                } else {
                    ret = new VariableActionItem(varIdentifier, null, true);
                    variables.add((VariableActionItem) ret);
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret = new BlockItem(null, commands(registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                GraphTargetItem varincdec = variable(registerVars, inFunction, inMethod, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, varincdec);
                }
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    List<GraphTargetItem> args = call(registerVars, inFunction, inMethod, variables);
                    VariableActionItem supItem = new VariableActionItem(s.value.toString(), null, false);
                    variables.add(supItem);
                    ret = new CallMethodActionItem(null, supItem, new DirectValueActionItem(null, 0, new Undefined(), constantPool), args);
                } else {//no costructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem ifExpr = (expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                GraphTargetItem onTrue = command(registerVars, inFunction, inMethod, forinlevel, true, variables);
                List<GraphTargetItem> onTrueList = new ArrayList<>();
                onTrueList.add(onTrue);
                s = lex();
                List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    onFalseList = new ArrayList<>();
                    onFalseList.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                } else {
                    lexer.pushback(s);
                }
                ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> whileExpr = new ArrayList<>();
                whileExpr.add(commaExpression(registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> whileBody = new ArrayList<>();
                whileBody.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                ret = new WhileItem(null, null, whileExpr, whileBody);
                break;
            case DO:
                List<GraphTargetItem> doBody = new ArrayList<>();
                doBody.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> doExpr = new ArrayList<>();
                doExpr.add(commaExpression(registerVars, inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new DoWhileItem(null, null, doBody, doExpr);
                break;
            case FOR:
                expectedType(SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                GraphTargetItem collection = null;
                String objIdent = null;
                int innerExprReg = 0;
                if (s.type == SymbolType.VAR || s.type == SymbolType.IDENTIFIER) {
                    ParsedSymbol s2 = null;
                    ParsedSymbol ssel = s;
                    if (s.type == SymbolType.VAR) {
                        s2 = lex();
                        ssel = s2;
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
                            collection = expression(registerVars, inFunction, inMethod, true, variables);
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
                    GraphTargetItem fc = command(registerVars, inFunction, inMethod, forinlevel, true, variables);
                    if (fc != null) { //can be empty command
                        forFirstCommands.add(fc);
                    }
                    forExpr = (expression(registerVars, inFunction, inMethod, true, variables));
                    expectedType(SymbolType.SEMICOLON);
                    forFinalCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                }
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> forBody = new ArrayList<>();
                forBody.add(command(registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables));
                if (forin) {
                    ret = new ForInActionItem(null, null, pushConst(objIdent), collection, forBody);
                } else {
                    ret = new ForItem(null, null, forFirstCommands, forExpr, forFinalCommands, forBody);
                }
                break;
            case SWITCH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem switchExpr = expression(registerVars, inFunction, inMethod, true, variables);
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
                        GraphTargetItem curCaseExpr = expression(registerVars, inFunction, inMethod, true, variables);
                        caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        caseExprsAll.add(curCaseExpr);
                        valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    List<GraphTargetItem> caseCmd = commands(registerVars, inFunction, inMethod, forinlevel, variables);
                    caseCmds.add(caseCmd);
                    s = lex();
                }
                List<GraphTargetItem> defCmd = new ArrayList<>();
                if (s.type == SymbolType.DEFAULT) {
                    expectedType(SymbolType.COLON);
                    defCmd = commands(registerVars, inFunction, inMethod, forinlevel, variables);
                    s = lexer.lex();
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                ret = new SwitchItem(null, null, switchExpr, caseExprsAll, caseCmds, defCmd, valueMapping);
                break;
            case BREAK:
                ret = new BreakItem(null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case CONTINUE:
                ret = new ContinueItem(null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case RETURN:
                GraphTargetItem retexpr = expression(true, registerVars, inFunction, inMethod, true, variables);
                if (retexpr == null) {
                    retexpr = new DirectValueActionItem(null, 0, new Undefined(), new ArrayList<String>());
                }
                ret = new ReturnActionItem(null, retexpr);
                break;
            case TRY:
                List<GraphTargetItem> tryCommands = new ArrayList<>();
                tryCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                s = lex();
                boolean found = false;
                List<List<GraphTargetItem>> catchCommands = null;
                List<GraphTargetItem> catchExceptions = new ArrayList<>();
                if (s.type == SymbolType.CATCH) {
                    expectedType(SymbolType.PARENT_OPEN);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING);
                    catchExceptions.add(pushConst((String) s.value));
                    expectedType(SymbolType.PARENT_CLOSE);
                    catchCommands = new ArrayList<>();
                    List<GraphTargetItem> cc = new ArrayList<>();
                    cc.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                    catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = new ArrayList<>();
                    finallyCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true, variables));
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                ret = new TryActionItem(tryCommands, catchExceptions, catchCommands, finallyCommands);
                break;
            case THROW:
                ret = new ThrowActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
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
                ret = expression(registerVars, inFunction, inMethod, true, variables);
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
            ret = expression(registerVars, inFunction, inMethod, true, variables);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ParseException {
        return expression(false, registerVars, inFunction, inMethod, allowRemainder, variables);
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

    private GraphTargetItem expressionRemainder(GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        switch (s.type) {
            case TERNAR:
                GraphTargetItem terOnTrue = expression(registerVars, inFunction, inMethod, true, variables);
                expectedType(SymbolType.COLON);
                GraphTargetItem terOnFalse = expression(registerVars, inFunction, inMethod, true, variables);
                ret = new TernarOpItem(null, expr, terOnTrue, terOnFalse);
                break;
            case SHIFT_LEFT:
                ret = new LShiftActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case SHIFT_RIGHT:
                ret = new RShiftActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case USHIFT_RIGHT:
                ret = new URShiftActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case BITAND:
                ret = new BitAndActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case BITOR:
                ret = new BitOrActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case DIVIDE:
                ret = new DivideActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case MODULO:
                ret = new ModuloActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case EQUALS:
                ret = new EqActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables), true/*FIXME SWF version?*/);
                break;
            case STRICT_EQUALS:
                ret = new StrictEqActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case NOT_EQUAL:
                ret = new NeqActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables), true/*FIXME SWF version?*/);
                break;
            case STRICT_NOT_EQUAL:
                ret = new StrictNeqActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case LOWER_THAN:
                ret = new LtActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables), true/*FIXME SWF version?*/);
                break;
            case LOWER_EQUAL:
                ret = new LeActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_THAN:
                ret = new GtActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case GREATER_EQUAL:
                ret = new GeActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables), true/*FIXME SWF version?*/);
                break;
            case AND:
                ret = new AndItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case OR:
                ret = new OrItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case MINUS:
                ret = new SubtractActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case MULTIPLY:
                ret = new MultiplyActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case PLUS:
                ret = new AddActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables), true);
                break;
            case XOR:
                ret = new BitXorActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
                break;
            case AS:

                break;
            case INSTANCEOF:
                ret = new InstanceOfActionItem(null, expr, expression(registerVars, inFunction, inMethod, false, variables));
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
                GraphTargetItem assigned = expression(registerVars, inFunction, inMethod, true, variables);
                switch (s.type) {
                    case ASSIGN:
                        //assigned = assigned;
                        break;
                    case ASSIGN_BITAND:
                        assigned = new BitAndActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_BITOR:
                        assigned = new BitOrActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_DIVIDE:
                        assigned = new DivideActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_MINUS:
                        assigned = new SubtractActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_MODULO:
                        assigned = new ModuloActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_MULTIPLY:
                        assigned = new MultiplyActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_PLUS:
                        assigned = new AddActionItem(null, expr, assigned, true/*TODO:SWF version?*/);
                        break;
                    case ASSIGN_SHIFT_LEFT:
                        assigned = new LShiftActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_SHIFT_RIGHT:
                        assigned = new RShiftActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_USHIFT_RIGHT:
                        assigned = new URShiftActionItem(null, expr, assigned);
                        break;
                    case ASSIGN_XOR:
                        assigned = new BitXorActionItem(null, expr, assigned);
                        break;
                }
                if (expr instanceof VariableActionItem) {
                    ((VariableActionItem) expr).setStoreValue(assigned);
                    ((VariableActionItem) expr).setDefinition(false);
                    ret = expr;
                } else if (expr instanceof GetMemberActionItem) {
                    ret = new SetMemberActionItem(null, ((GetMemberActionItem) expr).object, ((GetMemberActionItem) expr).memberName, assigned);
                } else {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                break;
            case INCREMENT: //postincrement
                if (!(expr instanceof VariableActionItem) && !(expr instanceof GetMemberActionItem)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostIncrementActionItem(null, expr);
                break;
            case DECREMENT: //postdecrement
                if (!(expr instanceof VariableActionItem) && !(expr instanceof GetMemberActionItem)) {
                    throw new ParseException("Invalid assignment", lexer.yyline());
                }
                ret = new PostDecrementActionItem(null, expr);
                break;
            case DOT: //member
            case BRACKET_OPEN: //member
            case PARENT_OPEN: //function call
                lexer.pushback(s);
                ret = memberOrCall(expr, registerVars, inFunction, inMethod, variables);
                break;
            case IDENTIFIER:
                switch (s.value.toString()) {
                    case "add":
                        ret = new StringAddActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "eq":
                        ret = new StringEqActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "ne":
                        ret = new StringNeActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "lt":
                        ret = new StringLtActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "ge":
                        ret = new StringGeActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "gt":
                        ret = new StringGtActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    case "le":
                        ret = new StringLeActionItem(null, expr, expression(registerVars, inFunction, inMethod, allowRemainder, variables));
                        break;
                    default:
                        lexer.pushback(s);
                }
                break;
            default:
                lexer.pushback(s);
        }

        if (ret == null) {
            if (expr instanceof ParenthesisItem) {
                if (isType(((ParenthesisItem) expr).value)) {
                    GraphTargetItem expr2 = expression(false, registerVars, inFunction, inMethod, true, variables);
                    if (expr2 != null) {
                        ret = new CastOpActionItem(null, ((ParenthesisItem) expr).value, expr2);
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
        while (item instanceof GetMemberActionItem) {
            item = ((GetMemberActionItem) item).object;
        }
        if (item instanceof VariableActionItem) {
            return true;
        }
        return false;
    }

    private int brackets(List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(registerVars, inFunction, inMethod, true, variables));
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

    private GraphTargetItem commaExpression(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forInLevel, List<VariableActionItem> variables) throws IOException, ParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(registerVars, inFunction, inMethod, forInLevel, false, variables);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(registerVars, inFunction, inMethod, true, variables));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new ParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expression(boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean existsRemainder = false;
        boolean assocRight = false;
        switch (s.type) {
            case NEGATE:
                versionRequired(s, 5);
                ret = expression(registerVars, inFunction, inMethod, false, variables);
                ret = new BitXorActionItem(null, ret, new DirectValueActionItem(4.294967295E9));
                existsRemainder = true;
                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new DirectValueActionItem(null, 0, -(double) (Double) s.value, new ArrayList<String>());
                    existsRemainder = true;
                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new DirectValueActionItem(null, 0, -(long) (Long) s.value, new ArrayList<String>());
                    existsRemainder = true;
                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expression(registerVars, inFunction, inMethod, true, variables);
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
                    } else {;
                        ret = (new SubtractActionItem(null, new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<String>()), num));
                    }
                }
                break;
            case TYPEOF:
                ret = new TypeOfActionItem(null, expression(registerVars, inFunction, inMethod, false, variables));
                existsRemainder = true;
                break;
            case TRUE:
                ret = new DirectValueActionItem(null, 0, Boolean.TRUE, new ArrayList<String>());
                existsRemainder = true;
                break;
            case NULL:
                ret = new DirectValueActionItem(null, 0, new Null(), new ArrayList<String>());
                existsRemainder = true;
                break;
            case UNDEFINED:
                ret = new DirectValueActionItem(null, 0, new Undefined(), new ArrayList<String>());
                break;
            case FALSE:
                ret = new DirectValueActionItem(null, 0, Boolean.FALSE, new ArrayList<String>());
                existsRemainder = true;
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
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    objectNames.add(0, pushConst((String) s.value));
                    expectedType(SymbolType.COLON);
                    objectValues.add(0, expression(registerVars, inFunction, inMethod, true, variables));
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new InitObjectActionItem(null, objectNames, objectValues);
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(inBrackets, registerVars, inFunction, inMethod, variables);
                ret = new InitArrayActionItem(null, inBrackets);
                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                if (s.isType(SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC)) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                ret = function(true, fname, false, variables);
                break;
            case STRING:
                ret = pushConst(s.value.toString());
                ret = memberOrCall(ret, registerVars, inFunction, inMethod, variables);
                existsRemainder = true;
                break;
            case NEWLINE:
                ret = new DirectValueActionItem(null, 0, "\r", new ArrayList<String>());
                existsRemainder = true;
                break;
            case NAN:
                ret = new DirectValueActionItem(null, 0, Double.NaN, new ArrayList<String>());
                existsRemainder = true;
                break;
            case INFINITY:
                ret = new DirectValueActionItem(null, 0, Double.POSITIVE_INFINITY, new ArrayList<String>());
                existsRemainder = true;
                break;
            case INTEGER:
            case DOUBLE:
                ret = new DirectValueActionItem(null, 0, s.value, new ArrayList<String>());
                existsRemainder = true;
                break;
            case DELETE:
                GraphTargetItem varDel = variable(registerVars, inFunction, inMethod, variables);
                if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, gm.object, gm.memberName);
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem prevar = variable(registerVars, inFunction, inMethod, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, prevar);
                }
                existsRemainder = true;
                break;
            case NOT:
                ret = new NotItem(null, expression(registerVars, inFunction, inMethod, false, variables));
                existsRemainder = true;
                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = memberOrCall(ret, registerVars, inFunction, inMethod, variables);
                existsRemainder = true;
                break;
            case NEW:
                GraphTargetItem newvar = variable(registerVars, inFunction, inMethod, variables);
                expectedType(SymbolType.PARENT_OPEN);
                if (newvar instanceof GetMemberActionItem) {
                    GetMemberActionItem mem = (GetMemberActionItem) newvar;
                    ret = new NewMethodActionItem(null, mem.object, mem.memberName, call(registerVars, inFunction, inMethod, variables));
                } else if (newvar instanceof VariableActionItem) {
                    ret = new NewObjectActionItem(null, new DirectValueActionItem(((VariableActionItem) newvar).getVariableName()), call(registerVars, inFunction, inMethod, variables));
                } else {
                    throw new ParseException("Invalid new item", lexer.yyline());
                }
                existsRemainder = true;
                break;
            case EVAL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem evar = new EvalActionItem(null, expression(registerVars, inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                evar = memberOrCall(evar, registerVars, inFunction, inMethod, variables);
                ret = evar;
                existsRemainder = true;
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                if (s.value.equals("not")) {
                    ret = new NotItem(null, expression(registerVars, inFunction, inMethod, false, variables));
                } else {
                    lexer.pushback(s);
                    GraphTargetItem var = variable(registerVars, inFunction, inMethod, variables);
                    var = memberOrCall(var, registerVars, inFunction, inMethod, variables);
                    ret = var;
                }
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
                rem = expressionRemainder(rem, registerVars, inFunction, inMethod, assocRight, variables);
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

    private DirectValueActionItem pushConst(String s) throws IOException, ParseException {
        int index = constantPool.indexOf(s);
        if (index == -1) {
            constantPool.add(s);
            index = constantPool.indexOf(s);
        }
        return new DirectValueActionItem(null, 0, new ConstantIndex(index), constantPool);
    }
    private ActionScriptLexer lexer = null;
    private List<String> constantPool;

    public List<GraphTargetItem> treeFromString(String str, List<String> constantPool) throws ParseException, IOException {
        List<GraphTargetItem> retTree = new ArrayList<>();
        this.constantPool = constantPool;
        lexer = new ActionScriptLexer(new StringReader(str));

        List<VariableActionItem> vars = new ArrayList<>();
        retTree.addAll(commands(new HashMap<String, Integer>(), false, false, 0, vars));
        for (VariableActionItem v : vars) {
            String varName = v.getVariableName();
            GraphTargetItem stored = v.getStoreValue();
            if (v.isDefinition()) {
                v.setBoxedValue(new DefineLocalActionItem(null, pushConst(varName), stored));
            } else {
                if (stored != null) {
                    v.setBoxedValue(new SetVariableActionItem(null, pushConst(varName), stored));
                } else {
                    v.setBoxedValue(new GetVariableActionItem(null, pushConst(varName)));
                }
            }
        }
        if (lexer.lex().type != SymbolType.EOF) {
            throw new ParseException("Parsing finished before end of the file", lexer.yyline());
        }
        return retTree;
    }

    public List<Action> actionsFromTree(List<GraphTargetItem> tree, List<String> constantPool) throws CompilationException {
        ActionSourceGenerator gen = new ActionSourceGenerator(swfVersion, constantPool);
        List<Action> ret = new ArrayList<>();
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<String, Integer>(), 0, Boolean.FALSE, 0);
        List<GraphSourceItem> srcList = gen.generate(localData, tree);
        for (GraphSourceItem s : srcList) {
            if (s instanceof Action) {
                ret.add((Action) s);
            }
        }
        ret.add(0, new ActionConstantPool(constantPool));
        return ret;
    }

    public List<Action> actionsFromString(String s) throws ParseException, IOException, CompilationException {
        List<String> constantPool = new ArrayList<>();
        List<GraphTargetItem> tree = treeFromString(s, constantPool);
        return actionsFromTree(tree, constantPool);
    }

    private void versionRequired(ParsedSymbol s, int min) throws ParseException {
        versionRequired(s.value.toString(), min, Integer.MAX_VALUE);
    }

    private void versionRequired(ParsedSymbol s, int min, int max) throws ParseException {
        versionRequired(s.value.toString(), min, max);
    }

    private void versionRequired(String type, int min, int max) throws ParseException {
        if (min == max && swfVersion != min) {
            throw new ParseException(type + " requires SWF version " + min, lexer.yyline());
        }
        if (swfVersion < min) {
            throw new ParseException(type + " requires at least SWF version " + min, lexer.yyline());
        }
        if (swfVersion > max) {
            throw new ParseException(type + " requires SWF version lower than " + max, lexer.yyline());
        }
    }
}
