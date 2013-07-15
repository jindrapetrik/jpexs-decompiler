/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.treemodel.AsciiToCharTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CallFunctionTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CallMethodTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CallTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CharToAsciiTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.CloneSpriteTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ConstantPool;
import com.jpexs.decompiler.flash.action.treemodel.DefineLocalTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.DefineRegisterTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.DeleteTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.EvalTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.FunctionTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetTimeTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetURL2TreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetVariableTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetVersionTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GotoFrame2TreeItem;
import com.jpexs.decompiler.flash.action.treemodel.InitArrayTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.InitObjectTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.LoadMovieNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.LoadMovieTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.LoadVariablesNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.LoadVariablesTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.MBAsciiToCharTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.MBCharToAsciiTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.MBStringExtractTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.MBStringLengthTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.NewMethodTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.NewObjectTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.NextFrameTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PlayTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostDecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PostIncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PrevFrameTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PrintAsBitmapNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PrintAsBitmapTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PrintNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.PrintTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.RandomNumberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.RemoveSpriteTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ReturnTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetVariableTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StartDragTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StopAllSoundsTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StopDragTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StopTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StringExtractTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StringLengthTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ThrowTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ToIntegerTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ToNumberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ToStringTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.ToggleHighQualityTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.TraceTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.TypeOfTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.UnLoadMovieNumTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.UnLoadMovieTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.ClassTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.ForInTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.IfFrameLoadedTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.InterfaceTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.TellTargetTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.TryTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.WithTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.AddTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.BitAndTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.BitOrTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.BitXorTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.DivideTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.EqTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.GeTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.GtTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.InstanceOfTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.LeTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.LtTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.ModuloTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.MultiplyTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.NeqTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.PreDecrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.PreIncrementTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.StrictEqTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.StrictNeqTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.SubtractTreeItem;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.graph.AndItem;
import com.jpexs.decompiler.flash.graph.BinaryOp;
import com.jpexs.decompiler.flash.graph.BlockItem;
import com.jpexs.decompiler.flash.graph.BreakItem;
import com.jpexs.decompiler.flash.graph.CommaExpressionItem;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.DoWhileItem;
import com.jpexs.decompiler.flash.graph.ForItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.IfItem;
import com.jpexs.decompiler.flash.graph.NotItem;
import com.jpexs.decompiler.flash.graph.OrItem;
import com.jpexs.decompiler.flash.graph.ParenthesisItem;
import com.jpexs.decompiler.flash.graph.SwitchItem;
import com.jpexs.decompiler.flash.graph.TernarOpItem;
import com.jpexs.decompiler.flash.graph.WhileItem;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    public static final int REGISTER_THIS = 1;
    public static final int REGISTER_ARGUMENTS = 2;
    public static final int REGISTER_SUPER = 3;
    public static final int REGISTER_ROOT = 4;
    public static final int REGISTER_PARENT = 5;
    public static final int REGISTER_GLOBAL = 6;
    private long uniqLast = 0;
    private boolean debugMode = false;

    private String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    private List<GraphTargetItem> commands(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(registerVars, inFunction, inMethod, forinlevel, true)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private List<GraphTargetItem> nonempty(List<GraphTargetItem> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private GraphTargetItem type() throws IOException, ParseException {
        GraphTargetItem ret = null;

        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
        ret = new GetVariableTreeItem(null, pushConst(s.value.toString()));
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            ret = new GetMemberTreeItem(null, ret, pushConst(s.value.toString()));
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem memberOrCall(GraphTargetItem newcmds, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        ParsedSymbol s = lex();
        GraphTargetItem ret = newcmds;
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN, SymbolType.PARENT_OPEN)) {
            switch (s.type) {
                case DOT:
                case BRACKET_OPEN:
                    lexer.pushback(s);
                    ret = member(ret, registerVars, inFunction, inMethod);
                    break;
                case PARENT_OPEN:
                    if (ret instanceof GetMemberTreeItem) {
                        GetMemberTreeItem mem = (GetMemberTreeItem) ret;
                        ret = new CallMethodTreeItem(null, mem.object, mem.memberName, call(registerVars, inFunction, inMethod));
                    } else if (ret instanceof GetVariableTreeItem) {
                        GetVariableTreeItem var = (GetVariableTreeItem) ret;
                        ret = new CallFunctionTreeItem(null, var.name, call(registerVars, inFunction, inMethod));
                    } else {
                        ret = new CallFunctionTreeItem(null, ret, call(registerVars, inFunction, inMethod));
                    }
                    break;
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem member(GraphTargetItem obj, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        GraphTargetItem ret = obj;
        ParsedSymbol s = lex();
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN)) {
            if (s.type == SymbolType.BRACKET_OPEN) {
                ret = new GetMemberTreeItem(null, ret, expression(registerVars, inFunction, inMethod, true));
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
                ret = new GetMemberTreeItem(null, ret, pushConst(s.value.toString()));
            }
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    private GraphTargetItem variable(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER);
        if (registerVars.containsKey(s.value.toString())) {
            ret = new DirectValueTreeItem(null, 0, new RegisterNumber(registerVars.get(s.value.toString())), new ArrayList<String>());
        } else {
            if (inMethod) {
                ret = new DirectValueTreeItem(null, 0, new RegisterNumber(REGISTER_THIS), new ArrayList<String>());
                //TODO: Handle properties (?)
                if (false) { //GraphTargetItem.propertyNamesList.contains(s.value.toString())) {
                    //ret.add(new ActionPush((Long) (long) (int) GraphTargetItem.propertyNamesList.indexOf(s.value.toString())));
                    //ret.add(new ActionGetProperty());
                } else {
                    ret = new GetMemberTreeItem(null, ret, pushConst(s.value.toString()));
                }
            } else {
                ret = new GetVariableTreeItem(null, pushConst(s.value.toString()));
            }
        }
        ret = (member(ret, registerVars, inFunction, inMethod));
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

    private List<GraphTargetItem> call(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(registerVars, inFunction, inMethod, true));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private FunctionTreeItem function(boolean withBody, String functionName, boolean isMethod) throws IOException, ParseException {
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
                type();
                s = lex();
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }
        HashMap<String, Integer> registerVars = new HashMap<>();
        registerVars.put("_parent", REGISTER_PARENT);
        registerVars.put("_root", REGISTER_ROOT);
        registerVars.put("super", REGISTER_SUPER);
        registerVars.put("arguments", REGISTER_ARGUMENTS);
        registerVars.put("this", REGISTER_THIS);
        registerVars.put("_global", REGISTER_GLOBAL);
        for (int i = 0; i < paramNames.size(); i++) {
            registerVars.put(paramNames.get(i), (7 + i)); //(paramNames.size() - i)));
        }
        List<GraphTargetItem> body = null;
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);
            body = commands(registerVars, true, isMethod, 0);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        return new FunctionTreeItem(null, functionName, paramNames, body, constantPool, -1);
    }

    private GraphTargetItem traits(boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr) throws IOException, ParseException {

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
        FunctionTreeItem constr = null;
        List<GraphTargetItem> staticFunctions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars = new ArrayList<>();
        List<GraphTargetItem> functions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> vars = new ArrayList<>();

        String classNameStr = "";
        if (nameStr instanceof GetMemberTreeItem) {
            GetMemberTreeItem mem = (GetMemberTreeItem) nameStr;
            if (mem.memberName instanceof DirectValueTreeItem) {
                classNameStr = ((DirectValueTreeItem) mem.memberName).toStringNoQuotes(new ConstantPool(constantPool));
            }
        } else if (nameStr instanceof GetVariableTreeItem) {
            GetVariableTreeItem var = (GetVariableTreeItem) nameStr;
            if (var.name instanceof DirectValueTreeItem) {
                classNameStr = ((DirectValueTreeItem) var.name).toStringNoQuotes(new ConstantPool(constantPool));
            }
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
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String fname = s.value.toString();
                    if (fname.equals(classNameStr)) { //constructor
                        constr = (function(!isInterface, "", true));
                    } else {
                        if (!isInterface) {
                            if (isStatic) {
                                FunctionTreeItem ft = function(!isInterface, "", true);
                                ft.calculatedFunctionName = pushConst(fname);
                                staticFunctions.add(ft);
                            } else {
                                FunctionTreeItem ft = function(!isInterface, "", true);
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
                        type();
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        if (isStatic) {
                            staticVars.add(new MyEntry<GraphTargetItem, GraphTargetItem>(pushConst(ident), expression(new HashMap<String, Integer>(), false, false, true)));
                        } else {
                            vars.add(new MyEntry<GraphTargetItem, GraphTargetItem>(pushConst(ident), expression(new HashMap<String, Integer>(), false, false, true)));
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
            return new InterfaceTreeItem(nameStr, implementsStr);
        } else {
            return new ClassTreeItem(nameStr, extendsStr, implementsStr, constr, functions, vars, staticFunctions, staticVars);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel) throws IOException, ParseException {
        GraphTargetItem ret = null;
        switch (s.type) {
            case GETVERSION:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new GetVersionTreeItem(null);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBCharToAsciiTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBCHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBAsciiToCharTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBSUBSTRING:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val1 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index1 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len1 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new MBStringExtractTreeItem(null, val1, index1, len1);
                break;
            case SUBSTR:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val2 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index2 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len2 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StringExtractTreeItem(null, val2, index2, len2);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case RANDOM:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RandomNumberTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case INT:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new ToIntegerTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            /*case TARGETPATH:
             expectedType(SymbolType.PARENT_OPEN);
             ret.addAll(expression(registerVars, inFunction, inMethod, true));
             expectedType(SymbolType.PARENT_CLOSE);
             ret.add(new ActionTargetPath());
             break;*/
            case NUMBER_OP:
                s = lex();
                if (s.type == SymbolType.DOT) {
                    ret = memberOrCall(new GetVariableTreeItem(null, new DirectValueTreeItem(null, 0, s.value, new ArrayList<String>())), registerVars, inFunction, inMethod);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToNumberTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                    expectedType(SymbolType.PARENT_CLOSE);
                }
                break;
            case STRING_OP:
                s = lex();
                if (s.type == SymbolType.DOT) {
                    ret = memberOrCall(new GetVariableTreeItem(null, new DirectValueTreeItem(null, 0, s.value, new ArrayList<String>())), registerVars, inFunction, inMethod);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToStringTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                    expectedType(SymbolType.PARENT_CLOSE);
                    ret = memberOrCall(ret, registerVars, inFunction, inMethod);
                }
                break;
            case ORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CharToAsciiTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new AsciiToCharTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case DUPLICATEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem src3 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem tar3 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem dep3 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new CloneSpriteTreeItem(null, src3, tar3, dep3);
                break;
            case GETTIMER:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetTimeTreeItem(null);
                break;
            default:
                return null;
        }
        return ret;
    }

    private GraphTargetItem command(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand) throws IOException, ParseException {
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
            case CALL:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CallTreeItem(null, (expression(registerVars, inFunction, inMethod, true)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthTreeItem(null, (expression(registerVars, inFunction, inMethod, true)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthTreeItem(null, (expression(registerVars, inFunction, inMethod, true)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case SET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem name1 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem value1 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new SetVariableTreeItem(null, name1, value1);
                break;
            case WITH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem wvar = (variable(registerVars, inFunction, inMethod));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> wcmd = commands(registerVars, inFunction, inMethod, forinlevel);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new WithTreeItem(null, wvar, wcmd);
                break;
            case DELETE:
                GraphTargetItem varDel = variable(registerVars, inFunction, inMethod);
                if (varDel instanceof GetMemberTreeItem) {
                    GetMemberTreeItem gm = (GetMemberTreeItem) varDel;
                    ret = new DeleteTreeItem(null, gm.object, gm.memberName);
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case TRACE:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TraceTreeItem(null, (expression(registerVars, inFunction, inMethod, true)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case GETURL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url = (expression(registerVars, inFunction, inMethod, true));
                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int getuMethod = 1;
                GraphTargetItem target = null;
                if (s.type == SymbolType.COMMA) {
                    target = (expression(registerVars, inFunction, inMethod, true));
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
                    target = new DirectValueTreeItem(null, 0, "", new ArrayList<String>());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetURL2TreeItem(null, url, target, getuMethod);
                break;
            case GOTOANDSTOP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtsFrame = expression(registerVars, inFunction, inMethod, true);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    s = lex();
                    gtsFrame = expression(registerVars, inFunction, inMethod, true);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2TreeItem(null, gtsFrame, false, false, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NEXTFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new NextFrameTreeItem(null);
                break;
            case PLAY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PlayTreeItem(null);
                break;
            case PREVFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PrevFrameTreeItem(null);
                break;
            case TELLTARGET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem tellTarget = expression(registerVars, inFunction, inMethod, true);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> tellcmds = commands(registerVars, inFunction, inMethod, forinlevel);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new TellTargetTreeItem(null, tellTarget, tellcmds);
                break;
            case STOP:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopTreeItem(null);
                break;
            case STOPALLSOUNDS:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopAllSoundsTreeItem(null);
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new ToggleHighQualityTreeItem(null);
                break;

            case STOPDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopDragTreeItem(null);
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                SymbolType unloadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem unTargetOrNum = expression(registerVars, inFunction, inMethod, true);
                expectedType(SymbolType.PARENT_CLOSE);
                if (unloadType == SymbolType.UNLOADMOVIE) {
                    ret = new UnLoadMovieTreeItem(null, unTargetOrNum);
                }
                if (unloadType == SymbolType.UNLOADMOVIENUM) {
                    ret = new UnLoadMovieNumTreeItem(null, unTargetOrNum);
                }
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                SymbolType printType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem printTarget = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem printBBox = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);

                switch (printType) {
                    case PRINT:
                        ret = new PrintTreeItem(null, printTarget, printBBox);
                        break;
                    case PRINTNUM:
                        ret = new PrintNumTreeItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAP:
                        ret = new PrintAsBitmapTreeItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAPNUM:
                        ret = new PrintAsBitmapNumTreeItem(null, printTarget, printBBox);
                        break;
                }
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                SymbolType loadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url2 = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.COMMA);
                GraphTargetItem targetOrNum = (expression(registerVars, inFunction, inMethod, true));



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
                        ret = new LoadVariablesTreeItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIE:
                        ret = new LoadMovieTreeItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADVARIABLESNUM:
                        ret = new LoadVariablesNumTreeItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIENUM:
                        ret = new LoadMovieNumTreeItem(null, url2, targetOrNum, lvmethod);
                        break;
                }
                break;
            case GOTOANDPLAY:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtpFrame = expression(registerVars, inFunction, inMethod, true);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?                    
                    s = lex();
                    gtpFrame = expression(registerVars, inFunction, inMethod, true);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2TreeItem(null, gtpFrame, true, false, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case REMOVEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RemoveSpriteTreeItem(null, (expression(registerVars, inFunction, inMethod, true)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case STARTDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem dragTarget = (expression(registerVars, inFunction, inMethod, true));
                GraphTargetItem lockCenter = null;
                GraphTargetItem constrain = null;
                GraphTargetItem x1 = null;
                GraphTargetItem y1 = null;
                GraphTargetItem x2 = null;
                GraphTargetItem y2 = null;
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    lockCenter = (expression(registerVars, inFunction, inMethod, true));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        constrain = new DirectValueTreeItem(null, 0, Boolean.TRUE, new ArrayList<String>());
                        x1 = (expression(registerVars, inFunction, inMethod, true));
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            y1 = (expression(registerVars, inFunction, inMethod, true));
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                x2 = (expression(registerVars, inFunction, inMethod, true));
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    y2 = (expression(registerVars, inFunction, inMethod, true));
                                } else {
                                    lexer.pushback(s);
                                    y2 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());
                                }
                            } else {
                                lexer.pushback(s);
                                x2 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());
                                y2 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());
                            }
                        } else {
                            lexer.pushback(s);
                            x2 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());
                            y2 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());
                            y1 = new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>());

                        }
                    } else {
                        lexer.pushback(s);
                        constrain = new DirectValueTreeItem(null, 0, Boolean.FALSE, new ArrayList<String>());
                        //ret.add(new ActionPush(Boolean.FALSE));
                    }
                } else {
                    lockCenter = new DirectValueTreeItem(null, 0, Boolean.FALSE, new ArrayList<String>());
                    constrain = new DirectValueTreeItem(null, 0, Boolean.FALSE, new ArrayList<String>());
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StartDragTreeItem(null, dragTarget, lockCenter, constrain, x1, y1, x2, y2);
                break;

            case IFFRAMELOADED:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem iflExpr = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> iflComs = commands(registerVars, inFunction, inMethod, forinlevel);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new IfFrameLoadedTreeItem(iflExpr, iflComs, null);
                break;
            case CLASS:
                GraphTargetItem classTypeStr = type();
                s = lex();
                GraphTargetItem extendsTypeStr = null;
                if (s.type == SymbolType.EXTENDS) {
                    extendsTypeStr = type();
                    s = lex();
                }
                List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                if (s.type == SymbolType.IMPLEMENTS) {
                    do {
                        GraphTargetItem implementsTypeStr = type();
                        implementsTypeStrs.add(implementsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(false, classTypeStr, extendsTypeStr, implementsTypeStrs));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INTERFACE:
                GraphTargetItem interfaceTypeStr = type();
                s = lex();
                List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                if (s.type == SymbolType.EXTENDS) {
                    do {
                        GraphTargetItem intExtendsTypeStr = type();
                        intExtendsTypeStrs.add(intExtendsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(true, interfaceTypeStr, null, intExtendsTypeStrs));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s = lexer.lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                ret = (function(true, s.value.toString(), false));
                break;
            case NEW:
                GraphTargetItem type = type();
                expectedType(SymbolType.PARENT_OPEN);
                if (type instanceof GetMemberTreeItem) {
                    GetMemberTreeItem mem = (GetMemberTreeItem) type;
                    ret = new NewMethodTreeItem(null, mem.object, mem.memberName, call(registerVars, inFunction, inMethod));
                } else if (type instanceof GetVariableTreeItem) {
                    GetVariableTreeItem var = (GetVariableTreeItem) type;
                    ret = new NewObjectTreeItem(null, var.name, call(registerVars, inFunction, inMethod));
                } else {
                    ret = new NewObjectTreeItem(null, ret, call(registerVars, inFunction, inMethod));
                }
                break;
            case VAR:
                s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                String varIdentifier = s.value.toString();
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type();
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {

                    if (!inFunction) {
                        //ret.add(pushConst(varIdentifier));
                    }
                    GraphTargetItem varval = (expression(registerVars, inFunction, inMethod, true));
                    if (inFunction) {
                        for (int i = 1; i < 256; i++) {
                            if (!registerVars.containsValue(i)) {
                                registerVars.put(varIdentifier, i);
                                ret = new StoreRegisterTreeItem(null, new RegisterNumber(i), varval, true);
                                break;
                            }
                        }
                    } else {
                        ret = new DefineLocalTreeItem(null, pushConst(varIdentifier), varval);
                    }
                } else {
                    if (inFunction) {
                        for (int i = 1; i < 256; i++) {
                            if (!registerVars.containsValue(i)) {
                                registerVars.put(varIdentifier, i);
                                ret = new DefineRegisterTreeItem(varIdentifier, i);
                                break;
                            }
                        }
                    } else {
                        ret = new DefineLocalTreeItem(null, pushConst(varIdentifier), null);
                    }
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret = new BlockItem(null, commands(registerVars, inFunction, inMethod, forinlevel));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                GraphTargetItem varincdec = variable(registerVars, inFunction, inMethod);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementTreeItem(null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementTreeItem(null, varincdec);
                }
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
            case PARENT_OPEN:
            case EVAL:
                ParsedSymbol varS = s;
                boolean isEval = false;
                GraphTargetItem var;
                if (s.type == SymbolType.PARENT_OPEN) {
                    var = expression(registerVars, inFunction, inMethod, true);
                    expectedType(SymbolType.PARENT_CLOSE);
                    memberOrCall(var, registerVars, inFunction, inMethod);
                } else if (s.type == SymbolType.EVAL) {
                    expectedType(SymbolType.PARENT_OPEN);
                    var = expression(registerVars, inFunction, inMethod, true);
                    var = new EvalTreeItem(null, var);
                    expectedType(SymbolType.PARENT_CLOSE);
                    var = memberOrCall(var, registerVars, inFunction, inMethod);
                    isEval = true;
                } else {
                    lexer.pushback(s);
                    var = variable(registerVars, inFunction, inMethod);
                }
                s = lex();
                switch (s.type) {
                    case ASSIGN:
                        ret = var;
                        ret = Action.gettoset(ret, expression(registerVars, inFunction, inMethod, true));
                        break;
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
                        GraphTargetItem valtoappend = (expression(registerVars, inFunction, inMethod, true));

                        switch (s.type) {
                            case ASSIGN_BITAND:
                                ret = Action.gettoset(var, new BitAndTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_BITOR:
                                ret = Action.gettoset(var, new BitOrTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_DIVIDE:
                                ret = Action.gettoset(var, new DivideTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_MINUS:
                                ret = Action.gettoset(var, new SubtractTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_MODULO:
                                ret = Action.gettoset(var, new ModuloTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_MULTIPLY:
                                ret = Action.gettoset(var, new MultiplyTreeItem(null, var, valtoappend));
                                break;
                            case ASSIGN_PLUS:
                                ret = Action.gettoset(var, new AddTreeItem(null, var, valtoappend, true));
                                break;
                        }
                        break;
                    case INCREMENT: //postincrement
                        ret = new PostIncrementTreeItem(null, var);
                        break;
                    case DECREMENT: //postdecrement
                        ret = new PostDecrementTreeItem(null, var);
                        break;
                    case PARENT_OPEN: //function call
                        ret = var;
                        if (varS.type == SymbolType.SUPER || varS.type == SymbolType.THIS) {
                            List<GraphTargetItem> args = call(registerVars, inFunction, inMethod);
                            ret = new CallMethodTreeItem(null, ret, new DirectValueTreeItem(null, 0, new Undefined(), constantPool), args);
                        } else {
                            lexer.pushback(s);
                            ret = memberOrCall(ret, registerVars, inFunction, inMethod);
                        }
                        break;
                    default:
                        if (isEval) {
                            ret = var;
                        } else {
                            if (mustBeCommand) {
                                throw new ParseException("Not a command", lexer.yyline());
                            }
                        }
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem ifExpr = (expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                GraphTargetItem onTrue = command(registerVars, inFunction, inMethod, forinlevel, true);
                List<GraphTargetItem> onTrueList = new ArrayList<>();
                onTrueList.add(onTrue);
                s = lex();
                List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    onFalseList = new ArrayList<>();
                    onFalseList.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                } else {
                    lexer.pushback(s);
                }
                ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> whileExpr = new ArrayList<>();
                whileExpr.add(commaExpression(registerVars, inFunction, inMethod, forinlevel));
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> whileBody = new ArrayList<>();
                whileBody.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                ret = new WhileItem(null, null, whileExpr, whileBody);
                break;
            case DO:
                List<GraphTargetItem> doBody = new ArrayList<>();
                doBody.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> doExpr = new ArrayList<>();
                doExpr.add(commaExpression(registerVars, inFunction, inMethod, forinlevel));
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
                            collection = expression(registerVars, inFunction, inMethod, true);
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
                    forFirstCommands.add((command(registerVars, inFunction, inMethod, forinlevel, true)));
                    forExpr = (expression(registerVars, inFunction, inMethod, true));
                    expectedType(SymbolType.SEMICOLON);
                    forFinalCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                }
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> forBody = new ArrayList<>();
                forBody.add(command(registerVars, inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true));
                if (forin) {
                    ret = new ForInTreeItem(null, null, pushConst(objIdent), collection, forBody);
                } else {
                    ret = new ForItem(null, null, forFirstCommands, forExpr, forFinalCommands, forBody);
                }
                break;
            case SWITCH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem switchExpr = expression(registerVars, inFunction, inMethod, true);
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
                        GraphTargetItem curCaseExpr = expression(registerVars, inFunction, inMethod, true);
                        caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        caseExprsAll.add(curCaseExpr);
                        valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    List<GraphTargetItem> caseCmd = commands(registerVars, inFunction, inMethod, forinlevel);
                    caseCmds.add(caseCmd);
                    s = lex();
                }
                List<GraphTargetItem> defCmd = new ArrayList<>();
                if (s.type == SymbolType.DEFAULT) {
                    expectedType(SymbolType.COLON);
                    defCmd = commands(registerVars, inFunction, inMethod, forinlevel);
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
                GraphTargetItem retexpr = expression(true, registerVars, inFunction, inMethod, true);
                if (retexpr == null) {
                    retexpr = new DirectValueTreeItem(null, 0, new Undefined(), new ArrayList<String>());
                }
                ret = new ReturnTreeItem(null, retexpr);
                break;
            case TRY:
                List<GraphTargetItem> tryCommands = new ArrayList<>();
                tryCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true));
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
                    cc.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                    catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = new ArrayList<>();
                    finallyCommands.add(command(registerVars, inFunction, inMethod, forinlevel, true));
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                ret = new TryTreeItem(tryCommands, catchExceptions, catchCommands, finallyCommands);
                break;
            case THROW:
                ret = new ThrowTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                break;
            default:
                GraphTargetItem valcmd = expressionCommands(s, registerVars, inFunction, inMethod, forinlevel);
                if (valcmd != null) {
                    ret = valcmd;
                    break;
                }
                if (s.type != SymbolType.SEMICOLON) {
                    lexer.pushback(s);
                }
                if (debugMode) {
                    System.out.println("/command");
                }
                return null;
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }
        if (debugMode) {
            System.out.println("/command");
        }
        if (ret == null && (!mustBeCommand)) {
            buf.pushAllBack(lexer);
        }
        lexer.removeListener(buf);
        return ret;

    }

    private GraphTargetItem expression(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder) throws IOException, ParseException {
        return expression(false, registerVars, inFunction, inMethod, allowRemainder);
    }

    /*private List<GraphTargetItem> expressionRemainder(GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder) throws IOException, ParseException {
     return expressionRemainder(null, registerVars, inFunction, inMethod, allowRemainder);
     }*/
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

    private GraphTargetItem expressionRemainder(GraphTargetItem expr, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder) throws IOException, ParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        switch (s.type) {
            case TERNAR:
                GraphTargetItem terOnTrue = expression(registerVars, inFunction, inMethod, false);
                expectedType(SymbolType.COLON);
                GraphTargetItem terOnFalse = expression(registerVars, inFunction, inMethod, false);
                ret = new TernarOpItem(null, expr, terOnTrue, terOnFalse);
                break;
            case BITAND:
                ret = new BitAndTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case BITOR:
                ret = new BitOrTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case DIVIDE:
                ret = new DivideTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case MODULO:
                ret = new ModuloTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case EQUALS:
                ret = new EqTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false), true/*FIXME SWF version?*/);
                break;
            case STRICT_EQUALS:
                ret = new StrictEqTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case NOT_EQUAL:
                ret = new NeqTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false), true/*FIXME SWF version?*/);
                break;
            case STRICT_NOT_EQUAL:
                ret = new StrictNeqTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case LOWER_THAN:
                ret = new LtTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false), true/*FIXME SWF version?*/);
                break;
            case LOWER_EQUAL:
                ret = new LeTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case GREATER_THAN:
                ret = new GtTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case GREATER_EQUAL:
                ret = new GeTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false), true/*FIXME SWF version?*/);
                break;
            case AND:
                ret = new AndItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case OR:
                ret = new OrItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case MINUS:
                ret = new SubtractTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case MULTIPLY:
                ret = new MultiplyTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case PLUS:
                ret = new AddTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false), true);
                break;
            case XOR:
                ret = new BitXorTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case AS:

                break;
            case INSTANCEOF:
                ret = new InstanceOfTreeItem(null, expr, expression(registerVars, inFunction, inMethod, false));
                break;
            case IS:

                break;
            default:
                lexer.pushback(s);
        }
        ret = fixPrecedence(ret);
        return ret;
    }

    private int brackets(List<GraphTargetItem> ret, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(registerVars, inFunction, inMethod, true));
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

    private GraphTargetItem commaExpression(HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, int forInLevel) throws IOException, ParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(registerVars, inFunction, inMethod, forInLevel, false);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(registerVars, inFunction, inMethod, true));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new ParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expression(boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod, boolean allowRemainder) throws IOException, ParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();
        boolean existsRemainder = false;
        boolean assocRight = false;
        switch (s.type) {
            case MINUS:
                GraphTargetItem num = expression(registerVars, inFunction, inMethod, true);
                if ((num instanceof DirectValueTreeItem)
                        && (((DirectValueTreeItem) num).value instanceof Long)) {
                    ((DirectValueTreeItem) num).value = -(Long) ((DirectValueTreeItem) num).value;
                    ret = num;
                } else if ((num instanceof DirectValueTreeItem)
                        && (((DirectValueTreeItem) num).value instanceof Double)) {
                    Double d = (Double) ((DirectValueTreeItem) num).value;
                    if (d.isInfinite()) {
                        ((DirectValueTreeItem) num).value = Double.NEGATIVE_INFINITY;
                    } else {
                        ((DirectValueTreeItem) num).value = -d;
                    }
                    ret = (num);
                } else if ((num instanceof DirectValueTreeItem)
                        && (((DirectValueTreeItem) num).value instanceof Float)) {
                    ((DirectValueTreeItem) num).value = -(Float) ((DirectValueTreeItem) num).value;
                    ret = (num);
                } else {;
                    ret = (new SubtractTreeItem(null, new DirectValueTreeItem(null, 0, (Long) 0L, new ArrayList<String>()), num));
                }
                break;
            case TYPEOF:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TypeOfTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                existsRemainder = true;
                break;
            case TRUE:
                ret = new DirectValueTreeItem(null, 0, Boolean.TRUE, new ArrayList<String>());
                existsRemainder = true;
                break;
            case NULL:
                ret = new DirectValueTreeItem(null, 0, new Null(), new ArrayList<String>());
                existsRemainder = true;
                break;
            case UNDEFINED:
                ret = new DirectValueTreeItem(null, 0, new Undefined(), new ArrayList<String>());
                break;
            case FALSE:
                ret = new DirectValueTreeItem(null, 0, Boolean.FALSE, new ArrayList<String>());
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
                    objectNames.add(expression(registerVars, inFunction, inMethod, true));
                    expectedType(SymbolType.COLON);
                    objectValues.add(expression(registerVars, inFunction, inMethod, true));
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new InitObjectTreeItem(null, objectNames, objectValues);
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(inBrackets, registerVars, inFunction, inMethod);
                ret = new InitArrayTreeItem(null, inBrackets);
                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                if (s.type == SymbolType.IDENTIFIER) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                ret = function(true, fname, false);
                break;
            case STRING:
                ret = pushConst(s.value.toString());
                ret = memberOrCall(ret, registerVars, inFunction, inMethod);
                existsRemainder = true;
                break;
            case NEWLINE:
                ret = new DirectValueTreeItem(null, 0, "\r", new ArrayList<String>());
                existsRemainder = true;
                break;
            case NAN:
                ret = new DirectValueTreeItem(null, 0, Double.NaN, new ArrayList<String>());
                existsRemainder = true;
                break;
            case INFINITY:
                ret = new DirectValueTreeItem(null, 0, Double.POSITIVE_INFINITY, new ArrayList<String>());
                existsRemainder = true;
                break;
            case INTEGER:
            case DOUBLE:
                ret = new DirectValueTreeItem(null, 0, s.value, new ArrayList<String>());
                existsRemainder = true;
                break;
            case DELETE:
                GraphTargetItem varDel = variable(registerVars, inFunction, inMethod);
                if (varDel instanceof GetMemberTreeItem) {
                    GetMemberTreeItem gm = (GetMemberTreeItem) varDel;
                    ret = new DeleteTreeItem(null, gm.object, gm.memberName);
                } else {
                    throw new ParseException("Not a property", lexer.yyline());
                }
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem prevar = variable(registerVars, inFunction, inMethod);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementTreeItem(null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementTreeItem(null, prevar);
                }
                existsRemainder = true;
                break;
            case NOT:
                ret = new NotItem(null, expression(registerVars, inFunction, inMethod, true));
                existsRemainder = true;
                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(registerVars, inFunction, inMethod, true));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = memberOrCall(ret, registerVars, inFunction, inMethod);
                existsRemainder = true;
                break;
            case NEW:
                GraphTargetItem newvar = variable(registerVars, inFunction, inMethod);
                expectedType(SymbolType.PARENT_OPEN);
                if (newvar instanceof GetMemberTreeItem) {
                    GetMemberTreeItem mem = (GetMemberTreeItem) newvar;
                    ret = new NewMethodTreeItem(null, mem.object, mem.memberName, call(registerVars, inFunction, inMethod));
                } else if (newvar instanceof GetVariableTreeItem) {
                    GetVariableTreeItem gv = (GetVariableTreeItem) newvar;
                    ret = new NewObjectTreeItem(null, gv.name, call(registerVars, inFunction, inMethod));
                } else {
                    throw new ParseException("Invalid new item", lexer.yyline());
                }
                break;
            case IDENTIFIER:
            case THIS:
            case EVAL:
                GraphTargetItem var;
                if (s.type == SymbolType.EVAL) {
                    expectedType(SymbolType.PARENT_OPEN);
                    var = new EvalTreeItem(null, expression(registerVars, inFunction, inMethod, true));
                    expectedType(SymbolType.PARENT_CLOSE);
                    var = memberOrCall(var, registerVars, inFunction, inMethod);

                } else {
                    lexer.pushback(s);
                    var = variable(registerVars, inFunction, inMethod);
                }

                GetVariableTreeItem gva = null;
                GetMemberTreeItem gmb = null;
                RegisterNumber reg = null;
                if (var instanceof GetVariableTreeItem) {
                    gva = (GetVariableTreeItem) var;
                } else if (var instanceof GetMemberTreeItem) {
                    gmb = (GetMemberTreeItem) var;
                } else if (var instanceof DirectValueTreeItem) {
                    if (((DirectValueTreeItem) var).value instanceof RegisterNumber) {
                        reg = (RegisterNumber) ((DirectValueTreeItem) var).value;
                    }
                }

                s = lex();
                switch (s.type) {
                    case ASSIGN:
                        GraphTargetItem varval = expression(registerVars, inFunction, inMethod, true);
                        if (gva != null) {
                            ret = new SetVariableTreeItem(null, gva.name, varval);
                        } else if (gmb != null) {
                            ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, varval);
                        } else if (reg != null) {
                            ret = new StoreRegisterTreeItem(null, reg, varval, false);
                        } else {
                            throw new ParseException("Invalid assignment", lexer.yyline());
                        }
                        existsRemainder = true;
                        assocRight = true;
                        break;
                    case ASSIGN_BITAND:
                    case ASSIGN_BITOR:
                    case ASSIGN_DIVIDE:
                    case ASSIGN_MINUS:
                    case ASSIGN_MODULO:
                    case ASSIGN_MULTIPLY:
                    case ASSIGN_PLUS:
                    case ASSIGN_XOR:
                        //List<GraphTargetItem> varset = new ArrayList<>();
                        //varset.addAll(var);
                        if (gva == null && gmb == null) {
                            throw new ParseException("Invalid assignment", lexer.yyline());
                        }
                        GraphTargetItem varval2 = expression(registerVars, inFunction, inMethod, true);

                        switch (s.type) {
                            case ASSIGN_BITAND:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new BitAndTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new BitAndTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_BITOR:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new BitOrTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new BitOrTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_DIVIDE:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new DivideTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new DivideTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_MINUS:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new SubtractTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new SubtractTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_MODULO:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new ModuloTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new ModuloTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_MULTIPLY:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new MultiplyTreeItem(null, gva, varval2));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new MultiplyTreeItem(null, gmb, varval2));
                                }
                                break;
                            case ASSIGN_PLUS:
                                if (gva != null) {
                                    ret = new SetVariableTreeItem(null, gva.name, new AddTreeItem(null, gva, varval2, true/*TODO:SWF version?*/));
                                } else {
                                    ret = new SetMemberTreeItem(null, gmb.object, gmb.memberName, new AddTreeItem(null, gmb, varval2, true/*TODO:SWF version?*/));
                                }
                                break;
                        }
                        existsRemainder = true;
                        assocRight = true;
                        break;
                    case INCREMENT: //postincrement
                        if (gva == null && gmb == null) {
                            throw new ParseException("Invalid assignment", lexer.yyline());
                        }
                        ret = new PostIncrementTreeItem(null, var);
                        break;
                    case DECREMENT: //postdecrement
                        if (gva == null && gmb == null) {
                            throw new ParseException("Invalid assignment", lexer.yyline());
                        }
                        ret = new PostDecrementTreeItem(null, var);
                        break;
                    case PARENT_OPEN: //function call
                        lexer.pushback(s);
                        ret = memberOrCall(var, registerVars, inFunction, inMethod);
                        existsRemainder = true;
                        break;
                    default:
                        ret = var;
                        lexer.pushback(s);
                        existsRemainder = true;
                    //ret.addAll(expressionRemainder(registerVars, inFunction, inMethod));
                }
                break;
            default:
                GraphTargetItem excmd = expressionCommands(s, registerVars, inFunction, inMethod, -1);
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
                rem = expressionRemainder(rem, registerVars, inFunction, inMethod, assocRight);
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

    private DirectValueTreeItem pushConst(String s) throws IOException, ParseException {
        int index = constantPool.indexOf(s);
        if (index == -1) {
            constantPool.add(s);
            index = constantPool.indexOf(s);
        }
        return new DirectValueTreeItem(null, 0, new ConstantIndex(index), constantPool);
    }
    private ActionScriptLexer lexer = null;
    private List<String> constantPool;

    public List<GraphTargetItem> treeFromString(String str, List<String> constantPool) throws ParseException, IOException {
        List<GraphTargetItem> retTree = new ArrayList<>();
        this.constantPool = constantPool;
        try {
            lexer = new ActionScriptLexer(new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF8")), "UTF8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ActionScriptParser.class.getName()).log(Level.SEVERE, null, ex);
            return retTree;
        }
        retTree.addAll(commands(new HashMap<String, Integer>(), false, false, 0));
        if (lexer.lex().type != SymbolType.EOF) {
            throw new ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        return retTree;
    }

    public List<Action> actionsFromTree(List<GraphTargetItem> tree, List<String> constantPool) {
        ActionScriptSourceGenerator gen = new ActionScriptSourceGenerator(constantPool);
        List<Action> ret = new ArrayList<>();
        List<Object> localDate = new ArrayList<>();
        localDate.add(new HashMap<String, Integer>()); //registerVars
        localDate.add(Boolean.FALSE); //inFunction
        localDate.add(Boolean.FALSE); //inMethod
        localDate.add((Integer) 0); //forInLevel
        List<GraphSourceItem> srcList = gen.generate(localDate, tree);
        for (GraphSourceItem s : srcList) {
            if (s instanceof Action) {
                ret.add((Action) s);
            }
        }
        ret.add(0, new ActionConstantPool(constantPool));
        return ret;
    }

    public List<Action> actionsFromString(String s) throws ParseException, IOException {
        List<String> constantPool = new ArrayList<>();
        List<GraphTargetItem> tree = treeFromString(s, constantPool);
        return actionsFromTree(tree, constantPool);
    }
}
