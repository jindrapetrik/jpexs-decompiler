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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_BITAND;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_BITOR;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_DIVIDE;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_MINUS;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_MODULO;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_MULTIPLY;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_PLUS;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_SHIFT_LEFT;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_SHIFT_RIGHT;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_USHIFT_RIGHT;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.ASSIGN_XOR;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.DECREMENT;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.DO;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.INCREMENT;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.PARENT_OPEN;
import static com.jpexs.decompiler.flash.action.parser.script.SymbolType.SWITCH;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.action.swf6.ActionInstanceOf;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    private static List<Action> commands(ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        System.out.println("commands:");
        List<Action> cmd = new ArrayList<Action>();
        while ((cmd = command(lexer)) != null) {
            ret.addAll(cmd);
        }

        System.out.println("/commands");
        return ret;
    }

    private static List<Action> type(ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret=new ArrayList<Action>();
        
        ParsedSymbol s = lex(lexer);
        expected(s,lexer.yyline(), SymbolType.IDENTIFIER);
        ret.add(new ActionPush(s.value));
        ret.add(new ActionGetVariable());
        s = lex(lexer);
        while ((s.type == SymbolType.DOT) || (s.type == SymbolType.TYPENAME)) {
            if (s.type == SymbolType.TYPENAME) {
                s = lex(lexer);
                break;
            }
            s = lex(lexer);
            expected(s,lexer.yyline(), SymbolType.IDENTIFIER);
            ret.add(new ActionPush(s.value));
            ret.add(new ActionGetMember());
            s = lex(lexer);            
        }
        lexer.pushback(s);
        return ret;
    }

    private static List<Action> variable(ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = lex(lexer);
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER);
        ret.add(new ActionPush(s.value));
        ret.add(new ActionGetVariable());
        s = lex(lexer);
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN)) {
            if (s.type == SymbolType.BRACKET_OPEN) {
                ret.addAll(expression(lexer));
                expected(lexer, SymbolType.BRACKET_CLOSE);
                ret.add(new ActionGetMember());
                s = lex(lexer);
                continue;
            }
            s = lex(lexer);
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER);
            ret.add(new ActionPush(s.value));
            ret.add(new ActionGetMember());
            s = lex(lexer);
        }
        lexer.pushback(s);
        return ret;
    }

    private static void expected(ParsedSymbol symb, int line, SymbolType... expected) throws IOException, ParseException {
        boolean found = false;
        for (SymbolType t : expected) {
            if (symb.type == t) {
                found = true;
            }
        }
        if (!found) {
            String expStr = "";
            boolean first = true;
            for (SymbolType e : expected) {
                if (!first) {
                    expStr += " or ";
                }
                expStr += e;
                first = false;
            }
            throw new ParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private static ParsedSymbol expected(ActionScriptLexer lexer, SymbolType... type) throws IOException, ParseException {
        ParsedSymbol symb = lex(lexer);
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private static ParsedSymbol lex(ActionScriptLexer lexer) throws IOException, ParseException {
        ParsedSymbol ret = lexer.lex();
        System.out.println(ret);
        return ret;
    }

    private static List<Action> call(ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        //expected(lexer, SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        int cnt = 0;
        ParsedSymbol s = lex(lexer);
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            cnt++;
            ret.addAll(expression(lexer));
            s = lex(lexer);
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        ret.add(new ActionPush(new Long(cnt)));
        return ret;
    }

    private static List<Action> function(ActionScriptLexer lexer, boolean withBody,String functionName) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = null;
        expected(lexer, SymbolType.PARENT_OPEN);
        s = lex(lexer);
        List<String> paramNames=new ArrayList<String>();
        List<Integer> paramRegs=new ArrayList<Integer>();
        int regPos=1;
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            expected(lexer, SymbolType.IDENTIFIER);
            s = lex(lexer);
            paramRegs.add(regPos);
            regPos++;
            paramNames.add(s.value.toString());
            if (s.type == SymbolType.COLON) {
                type(lexer);
                s = lex(lexer);
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }
        if (withBody) {
            expected(lexer, SymbolType.CURLY_OPEN);
            ret.addAll(commands(lexer));
            expected(lexer, SymbolType.CURLY_CLOSE);
        }
        int len=Action.actionsToBytes(ret, false, SWF.DEFAULT_VERSION).length;
        ret.add(0,new ActionDefineFunction2(functionName,
                                            false,
                                            false,
                                            false,
                                            false,
                                            false,
                                            false,
                                            false,
                                            false,
                                            false,
                                            0,len,SWF.DEFAULT_VERSION,paramNames,paramRegs));
        return ret;
    }

    private static List<Action> gettoset(List<Action> get, List<Action> value) {
        List<Action> ret = new ArrayList<Action>();
        ret.addAll(get);
        if (!ret.isEmpty()) {
            Action a = ret.get(ret.size() - 1);
            if (a instanceof ActionGetVariable) {
                ret.remove(ret.size() - 1);
                ret.addAll(value);
                ret.add(new ActionSetVariable());
            } else if (a instanceof ActionGetMember) {
                ret.remove(ret.size() - 1);
                ret.addAll(value);
                ret.add(new ActionSetMember());
            }
        }
        return ret;
    }

    private static List<Action> traits(ActionScriptLexer lexer, boolean isInterface) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = null;

        looptrait:
        while (true) {
            s = lex(lexer);
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE)) {
                s = lex(lexer);
            }
            switch (s.type) {
                case FUNCTION:
                    s = lex(lexer);
                    expected(s,lexer.yyline(),SymbolType.IDENTIFIER);
                    ret.addAll(function(lexer, !isInterface,s.value.toString()));
                    break;
                case VAR:
                    expected(lexer, SymbolType.IDENTIFIER);
                    s = lex(lexer);
                    if (s.type == SymbolType.COLON) {
                        type(lexer);
                        s = lex(lexer);
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        expression(lexer);
                        s = lex(lexer);
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
        return ret;
    }

    private static List<Action> command(ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        System.out.println("command:");
        ParsedSymbol s = lex(lexer);
        if (s == null) {
            return null;
        }
        switch (s.type) {
            case SUPER:
                lexer.pushback(s);
                ret.addAll(variable(lexer));
                s = lex(lexer);
                if (s.type == SymbolType.PARENT_OPEN) {
                    if (ret.get(ret.size() - 1) instanceof ActionGetMember) {
                        ret.remove(ret.size() - 1);
                        ret.addAll(call(lexer));
                        ret.add(new ActionCallMethod());
                    } else if (ret.get(ret.size() - 1) instanceof ActionGetVariable) {
                        ret.remove(ret.size() - 1);
                        ret.addAll(call(lexer));
                        ret.add(new ActionCallFunction());
                    }
                    s = lex(lexer);
                }
                lexer.pushback(s);

                break;
            case CLASS:
                type(lexer);
                s = lex(lexer);
                if (s.type == SymbolType.EXTENDS) {
                    type(lexer);
                    s = lex(lexer);
                }
                if (s.type == SymbolType.IMPLEMENTS) {
                    type(lexer);
                    s = lex(lexer);
                    while (s.type == SymbolType.COMMA) {
                        type(lexer);
                        s = lex(lexer);
                    }
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                traits(lexer, false);
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case INTERFACE:
                type(lexer);
                s = lex(lexer);
                if (s.type == SymbolType.EXTENDS) {
                    type(lexer);
                    s = lex(lexer);
                    while (s.type == SymbolType.COMMA) {
                        type(lexer);
                        s = lex(lexer);
                    }
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                traits(lexer, true);
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s=lexer.lex();
                expected(s,lexer.yyline(), SymbolType.IDENTIFIER);
                ret.addAll(function(lexer, true,s.value.toString()));
                break;
            case NEW:
                ret.addAll(type(lexer));
                expected(lexer, SymbolType.PARENT_OPEN);
                if (ret.get(ret.size() - 1) instanceof ActionGetMember) {
                            ret.remove(ret.size() - 1);
                            ret.addAll(call(lexer));
                            ret.add(new ActionNewMethod());
                } else if (ret.get(ret.size() - 1) instanceof ActionGetVariable) {
                    ret.remove(ret.size() - 1);
                    ret.addAll(call(lexer));
                    ret.add(new ActionNewObject());
                }
                break;
            case VAR:
                s = lex(lexer);
                expected(s,lexer.yyline(),SymbolType.IDENTIFIER);
                ret.add(new ActionPush(s.value));
                s = lex(lexer);
                if (s.type == SymbolType.COLON) {
                    type(lexer);
                    s = lex(lexer);
                    //TODO: handle value type
                }
                
                if (s.type == SymbolType.ASSIGN) {
                    ret.addAll(expression(lexer));
                    ret.add(new ActionDefineLocal2());
                } else {
                    ret.add(new ActionDefineLocal());
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret.addAll(commands(lexer));
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT:
            case DECREMENT:
                //TODO
                variable(lexer);
                break;
            case IDENTIFIER:
            case THIS:
                lexer.pushback(s);
                ret.addAll(variable(lexer));
                s = lex(lexer);
                switch (s.type) {
                    case ASSIGN:
                        gettoset(ret, expression(lexer));
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
                        //TODO
                        expression(lexer);
                        break;
                    case INCREMENT:
                    case DECREMENT:
                        //TODO
                        break;
                    case PARENT_OPEN: //function call
                        if (ret.get(ret.size() - 1) instanceof ActionGetMember) {
                            ret.remove(ret.size() - 1);
                            ret.addAll(call(lexer));
                            ret.add(new ActionCallMethod());
                        } else if (ret.get(ret.size() - 1) instanceof ActionGetVariable) {
                            ret.remove(ret.size() - 1);
                            ret.addAll(call(lexer));
                            ret.add(new ActionCallFunction());
                        } 
                        ret.add(new ActionPop());
                        break;
                    default:
                        throw new ParseException("Not a command", lexer.yyline());
                }
                break;
            case IF:
                expected(lexer, SymbolType.PARENT_OPEN);
                ret.addAll(expression(lexer));
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> onTrue=command(lexer);
                List<Action> onFalse=null;
                s = lex(lexer);
                if (s.type == SymbolType.ELSE) {
                    onFalse=command(lexer);
                } else {
                    lexer.pushback(s);
                }
                byte onTrueBytes[]=Action.actionsToBytes(onTrue, false, SWF.DEFAULT_VERSION);
                int onTrueLen=onTrueBytes.length;
                ret.add(new ActionNot());
                ActionIf ifaif=new ActionIf(0);
                ret.add(ifaif);
                ret.addAll(onTrue);
                ifaif.setJumpOffset(onTrueLen);
                ActionJump ajmp=null;
                if(onFalse!=null){
                    ajmp=new ActionJump(0);
                    ret.add(ajmp);
                    ifaif.setJumpOffset(onTrueLen+ajmp.getBytes(SWF.DEFAULT_VERSION).length);
                    byte onFalseBytes[]=Action.actionsToBytes(onFalse, false, SWF.DEFAULT_VERSION);
                    int onFalseLen=onFalseBytes.length;
                    ajmp.setJumpOffset(onFalseLen);
                    ret.addAll(onFalse);
                }
                break;
            case WHILE:
                expected(lexer, SymbolType.PARENT_OPEN);
                List<Action> whileExpr=expression(lexer);
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> whileBody=command(lexer);                
                whileExpr.add(new ActionNot());
                ActionIf whileaif=new ActionIf(0);
                whileExpr.add(whileaif);
                ActionJump whileajmp=new ActionJump(0);
                whileBody.add(whileajmp);
                int whileExprLen=Action.actionsToBytes(whileExpr, false, SWF.DEFAULT_VERSION).length;
                int whileBodyLen=Action.actionsToBytes(whileBody, false, SWF.DEFAULT_VERSION).length;
                whileajmp.setJumpOffset(-(
                        whileExprLen
                        +
                        whileBodyLen
                        ));
                whileaif.setJumpOffset(whileBodyLen);
                ret.addAll(whileExpr);
                ret.addAll(whileBody);
                break;
            case DO:
                List<Action> doBody=command(lexer);
                expected(lexer, SymbolType.WHILE);
                expected(lexer, SymbolType.PARENT_OPEN);
                doBody.addAll(expression(lexer));
                expected(lexer, SymbolType.PARENT_CLOSE);
                int doBodyLen=Action.actionsToBytes(doBody, false, SWF.DEFAULT_VERSION).length;
                ret.addAll(doBody);
                ret.add(new ActionIf(-doBodyLen));
                break;
            case FOR:
                expected(lexer, SymbolType.PARENT_OPEN);
                s = lex(lexer);
                boolean forin = false;
                if (s.type == SymbolType.VAR) {
                    ParsedSymbol s2 = lex(lexer);
                    if (s2.type == SymbolType.IDENTIFIER) {
                        ParsedSymbol s3 = lex(lexer);
                        if (s3.type == SymbolType.IN) {
                            expression(lexer);
                            forin = true;
                        }
                    } else {
                        lexer.pushback(s2);
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                }
                List<Action> forFinalCommands=new ArrayList<Action>();
                List<Action> forExpr =new ArrayList<Action>();
                if (!forin) {
                    ret.addAll(command(lexer));
                    expected(lexer, SymbolType.SEMICOLON);
                    forExpr=expression(lexer);
                    expected(lexer, SymbolType.SEMICOLON);
                    forFinalCommands= command(lexer);
                }
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> forBody=command(lexer);
                forBody.addAll(forFinalCommands);                
                forExpr.add(new ActionNot());
                ActionIf foraif=new ActionIf(0);
                forExpr.add(foraif);
                ActionJump forajmp=new ActionJump(0);
                forBody.add(forajmp);
                int forExprLen=Action.actionsToBytes(forExpr, false, SWF.DEFAULT_VERSION).length;
                int forBodyLen=Action.actionsToBytes(forBody, false, SWF.DEFAULT_VERSION).length;
                forajmp.setJumpOffset(-(
                        forExprLen
                        +
                        forBodyLen
                        ));
                foraif.setJumpOffset(forBodyLen);
                ret.addAll(forExpr);
                ret.addAll(forBody);                
                break;
            case SWITCH:
                expected(lexer, SymbolType.PARENT_OPEN);
                expression(lexer);
                expected(lexer, SymbolType.PARENT_CLOSE);
                expected(lexer, SymbolType.CURLY_OPEN);
                s = lex(lexer);
                while (s.type == SymbolType.CASE) {
                    expression(lexer);
                    expected(lexer, SymbolType.COLON);
                    commands(lexer);
                    s = lex(lexer);
                }
                if (s.type == SymbolType.DEFAULT) {
                    expected(lexer, SymbolType.COLON);
                    command(lexer);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                break;
            case BREAK:
            case CONTINUE:
                s = lex(lexer);
                if (s.type != SymbolType.IDENTIFIER) {
                    lexer.pushback(s);
                }
                break;
            case RETURN:
                expression(lexer, true);
                break;
            case TRY:
                command(lexer);
                s = lex(lexer);
                boolean found = false;
                if (s.type == SymbolType.CATCH) {
                    expected(lexer, SymbolType.PARENT_OPEN);
                    expected(lexer, SymbolType.IDENTIFIER);
                    expected(lexer, SymbolType.PARENT_CLOSE);
                    command(lexer);
                    s = lex(lexer);
                    found = true;
                }
                if (s.type == SymbolType.FINALLY) {
                    command(lexer);
                    found = true;
                    s = lex(lexer);
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                break;
            case THROW:
                expression(lexer);
                break;
            default:
                lexer.pushback(s);
                System.out.println("/command");
                return null;
        }
        s = lex(lexer);
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }
        System.out.println("/command");
        return ret;

    }

    private static List<Action> expression(ActionScriptLexer lexer) throws IOException, ParseException {
        return expression(lexer, false);
    }

    private static List<Action> expressionRemainder(ActionScriptLexer lexer) throws IOException, ParseException {
        return expressionRemainder(new ArrayList<Action>(),lexer);
    }
    private static List<Action> expressionRemainder(List<Action> expr,ActionScriptLexer lexer) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = lex(lexer);
        switch (s.type) {
            /*case INCREMENT:
            case DECREMENT:
                //TODO postincrement
                expressionRemainder(lexer);
                break;*/
            case BITAND:
                ret.addAll(expression(lexer));
                ret.add(new ActionBitAnd());
                break;
            case BITOR:
                ret.addAll(expression(lexer));
                ret.add(new ActionBitOr());
                break;
            case DIVIDE:
                ret.addAll(expression(lexer));
                ret.add(new ActionDivide());
                break;
            case EQUALS:
                ret.addAll(expression(lexer));
                ret.add(new ActionEquals());
                break;
            case NOT_EQUAL:
                ret.addAll(expression(lexer));
                ret.add(new ActionEquals());
                ret.add(new ActionNot());
                break;
            case LOWER_THAN:
                ret.addAll(expression(lexer));
                ret.add(new ActionLess());
                break;
            case LOWER_EQUAL:
                ret.addAll(expression(lexer));
                ret.add(new ActionGreater());
                ret.add(new ActionNot());
                break;
            case GREATER_THAN:
                ret.addAll(expression(lexer));
                ret.add(new ActionGreater());
                break;
            case GREATER_EQUAL:
                ret.addAll(expression(lexer));
                ret.add(new ActionLess());
                ret.add(new ActionNot());
                break;
            case AND:
                ret.add(new ActionPushDuplicate());
                ret.add(new ActionNot());
                List<Action> andExpr=expression(lexer);
                andExpr.add(0,new ActionPop());
                int andExprLen=Action.actionsToBytes(andExpr, false, SWF.DEFAULT_VERSION).length;
                ret.add(new ActionIf(andExprLen));
                ret.addAll(andExpr);
                break;
            case OR:
                ret.add(new ActionPushDuplicate());
                List<Action> orExpr=expression(lexer);
                orExpr.add(0,new ActionPop());
                int orExprLen=Action.actionsToBytes(orExpr, false, SWF.DEFAULT_VERSION).length;
                ret.add(new ActionIf(orExprLen));
                ret.addAll(orExpr);
                break;
            case MINUS:
                ret.addAll(expression(lexer));
                ret.add(new ActionSubtract());
                break;
            case MULTIPLY:
                ret.addAll(expression(lexer));
                ret.add(new ActionMultiply());
                break;
            case PLUS:
                ret.addAll(expression(lexer));
                ret.add(new ActionAdd());
                break;
            case XOR:
                ret.addAll(expression(lexer));
                ret.add(new ActionBitXor());                
                break;
            case AS:

                break;
            case INSTANCEOF:
                ret.addAll(expression(lexer));
                ret.add(new ActionInstanceOf());
                break;
            case IS:

                break;
            case TYPEOF:
                ret.addAll(expression(lexer));
                ret.add(new ActionTypeOf());
                break;
            default:
                lexer.pushback(s);
        }
        return ret;
    }

    private static List<Action> expression(ActionScriptLexer lexer, boolean allowEmpty) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = lex(lexer);
        switch (s.type) {
            case CURLY_OPEN: //Object literal
                s = lex(lexer);
                int objCnt = 0;
                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    objCnt++;
                    ret.addAll(expression(lexer));
                    expected(lexer, SymbolType.COLON);
                    ret.addAll(expression(lexer));
                    s = lex(lexer);
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret.add(new ActionPush(new Long(objCnt)));
                ret.add(new ActionInitObject());
                break;
            case BRACKET_OPEN: //Array literal
                s = lex(lexer);
                int arrCnt = 0;
                while (s.type != SymbolType.BRACKET_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    arrCnt++;
                    expression(lexer);
                    s = lex(lexer);
                    if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                    }
                }
                ret.add(new ActionPush(new Long(arrCnt)));
                ret.add(new ActionInitObject());

                break;
            case FUNCTION:
                s=lexer.lex();
                String fname="";
                if(s.type==SymbolType.IDENTIFIER){
                    fname=s.value.toString();
                }else{
                    lexer.pushback(s);
                }
                ret.addAll(function(lexer, true,fname));
                break;
            case STRING:
            case INTEGER:
            case DOUBLE:
                ret.add(new ActionPush(s.value));
                ret.addAll(expressionRemainder(lexer));
                break;
            case DELETE:
                ret.addAll(variable(lexer));
                ret.add(new ActionDelete());
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                List<Action> prevar=variable(lexer);
                ret.addAll(prevar);
                Action prelast=ret.remove(ret.size()-1);
                ret.addAll(prevar);
                if(s.type == SymbolType.INCREMENT){
                    ret.add(new ActionIncrement());
                }
                if(s.type == SymbolType.DECREMENT){
                    ret.add(new ActionDecrement());
                }
                ret.add(new ActionStoreRegister(0)); //TODO: ensure correct register number here
                if(prelast instanceof ActionGetVariable){
                    ret.add(new ActionSetVariable());
                }
                if(prelast instanceof ActionGetMember){
                    ret.add(new ActionSetMember());
                }
                ret.add(new ActionPush(new RegisterNumber(0))); //TODO: ensure correct register number here                
                ret.addAll(expressionRemainder(lexer));
                break;
            case NOT:
                ret.addAll(expression(lexer));
                ret.add(new ActionNot());
                ret.addAll(expressionRemainder(lexer));
                break;
            case PARENT_OPEN:
                ret.addAll(expression(lexer));
                expected(lexer, SymbolType.PARENT_CLOSE);
                ret.addAll(expressionRemainder(lexer));
                break;
            case NEW:
                ret.addAll(variable(lexer));
                expected(lexer, SymbolType.PARENT_OPEN);
                if (ret.get(ret.size() - 1) instanceof ActionGetMember) {
                            ret.remove(ret.size() - 1);
                            ret.addAll(call(lexer));
                            ret.add(new ActionNewMethod());
                } else if (ret.get(ret.size() - 1) instanceof ActionGetVariable) {
                    ret.remove(ret.size() - 1);
                    ret.addAll(call(lexer));
                    ret.add(new ActionNewObject());
                }  
                
                break;
            case IDENTIFIER:
            case THIS:
                lexer.pushback(s);
                List<Action> var=variable(lexer);
                
                s = lex(lexer);
                switch (s.type) {
                    case ASSIGN:
                        gettoset(var, expression(lexer));
                        ret.addAll(var);
                        break;
                    case ASSIGN_BITAND:
                    case ASSIGN_BITOR:
                    case ASSIGN_DIVIDE:
                    case ASSIGN_MINUS:
                    case ASSIGN_MODULO:
                    case ASSIGN_MULTIPLY:
                    case ASSIGN_PLUS:
                    case ASSIGN_XOR:
                        List<Action> varset=new ArrayList<Action>();                        
                        varset.addAll(var);
                        var.addAll(expression(lexer));
                        switch(s.type){
                            case ASSIGN_BITAND:
                                var.add(new ActionBitAnd());
                                break;
                            case ASSIGN_BITOR:
                                var.add(new ActionBitOr());
                                break;
                            case ASSIGN_DIVIDE:
                                var.add(new ActionDivide());
                                break;
                            case ASSIGN_MINUS:
                                var.add(new ActionSubtract());
                                break;
                            case ASSIGN_MODULO:
                                var.add(new ActionModulo());
                                break;
                            case ASSIGN_MULTIPLY:
                                var.add(new ActionMultiply());
                                break;
                            case ASSIGN_PLUS:
                                var.add(new ActionAdd());
                                break;
                        }                        
                        gettoset(varset, var);
                        ret.addAll(varset);
                        
                        break;
                    case INCREMENT:
                    case DECREMENT:
                        ret.addAll(var);
                        ret.addAll(var);
                        Action at=ret.remove(ret.size()-1);
                        ret.addAll(var);
                        if(s.type==SymbolType.INCREMENT){
                            ret.add(new ActionIncrement());
                        }
                        if(s.type==SymbolType.DECREMENT){
                            ret.add(new ActionDecrement());
                        }
                        if(at instanceof ActionGetMember){
                            ret.add(new ActionSetMember());
                        }
                        if(at instanceof ActionGetVariable){
                            ret.add(new ActionSetVariable());
                        }
                        break;
                    case PARENT_OPEN: //function call
                        if (var.get(var.size() - 1) instanceof ActionGetMember) {
                            var.remove(var.size() - 1);
                            var.addAll(call(lexer));
                            var.add(new ActionCallMethod());
                        } else if (var.get(var.size() - 1) instanceof ActionGetVariable) {
                            var.remove(var.size() - 1);
                            var.addAll(call(lexer));
                            var.add(new ActionCallFunction());
                        }
                        ret.addAll(var);
                        break;
                    default:
                        ret.addAll(var);
                        lexer.pushback(s);
                        expressionRemainder(lexer);
                }
                break;
            default:
                lexer.pushback(s);
        }
        return ret;
    }

    public static List<Action> parse(String str) throws ParseException, IOException {
        List<Action> ret = new ArrayList<Action>();
        ActionScriptLexer lexer = null;
        try {
            lexer = new ActionScriptLexer(new InputStreamReader(new ByteArrayInputStream(str.getBytes("UTF8")), "UTF8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ActionScriptParser.class.getName()).log(Level.SEVERE, null, ex);
            return ret;
        }


        ret.addAll(commands(lexer));

        if (lexer.lex() != null) {
            throw new ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        return ret;
    }
}
