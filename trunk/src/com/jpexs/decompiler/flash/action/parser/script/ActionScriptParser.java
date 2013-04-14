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
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.action.swf7.ActionExtends;
import com.jpexs.decompiler.flash.action.swf7.ActionImplementsOp;
import com.jpexs.decompiler.flash.action.swf7.ActionThrow;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.action.treemodel.ReturnTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionScriptParser {

    public static final int REGISTER_PARENT = 1;
    public static final int REGISTER_ROOT = 2;
    public static final int REGISTER_SUPER = 3;
    public static final int REGISTER_ARGUMENTS = 4;
    public static final int REGISTER_THIS = 5;
    public static final int REGISTER_GLOBAL = 6;
    private static long uniqLast = 0;

    private static String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    private static List<Action> commands(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        System.out.println("commands:");
        List<Action> cmd = new ArrayList<Action>();
        while ((cmd = command(lexer, registerVars, inFunction, inMethod)) != null) {
            ret.addAll(cmd);
        }

        System.out.println("/commands");
        return ret;
    }

    private static void fixLoop(List<Action> code, int breakOffset) {
        fixLoop(code, breakOffset, Integer.MAX_VALUE);
    }

    private static void fixLoop(List<Action> code, int breakOffset, int continueOffset) {
        int pos = 0;
        for (Action a : code) {
            pos += a.getBytes(SWF.DEFAULT_VERSION).length;
            if (a instanceof ActionJump) {
                ActionJump aj = (ActionJump) a;
                if (aj.isContinue && (continueOffset != Integer.MAX_VALUE)) {
                    aj.setJumpOffset(-pos + continueOffset);
                    aj.isContinue = false;
                }
                if (aj.isBreak) {
                    aj.setJumpOffset(-pos + breakOffset);
                    aj.isBreak = false;
                }
            }
        }
    }

    private static List<Action> nonempty(List<Action> list) {
        if (list == null) {
            return new ArrayList<Action>();
        }
        return list;
    }

    private static List<Action> typeToActions(List<String> type, boolean set) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        if (type.isEmpty()) {
            return ret;
        }
        ret.add(new ActionPush(type.get(0)));
        if (type.size() == 1 && set) {
            ret.add(new ActionSetVariable());
        } else {
            ret.add(new ActionGetVariable());
        }
        for (int i = 1; i < type.size(); i++) {
            ret.add(new ActionPush(type.get(i)));
            if ((i == type.size() - 1) && set) {
                ret.add(new ActionSetMember());
            } else {
                ret.add(new ActionGetMember());
            }
        }
        return ret;
    }

    private static List<String> type(ActionScriptLexer lexer) throws IOException, ParseException {
        List<String> ret = new ArrayList<String>();

        ParsedSymbol s = lex(lexer);
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
        ret.add(s.value.toString());
        s = lex(lexer);
        while (s.type == SymbolType.DOT) {
            s = lex(lexer);
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            ret.add(s.value.toString());
            s = lex(lexer);
        }
        lexer.pushback(s);
        return ret;
    }

    private static List<Action> variable(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = lex(lexer);
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER);
        if (registerVars.containsKey(s.value.toString())) {
            ret.add(new ActionPush(new RegisterNumber(registerVars.get(s.value.toString()))));
        } else {
            if (inMethod) {
                ret.add(new ActionPush(new RegisterNumber(REGISTER_THIS)));
                ret.add(new ActionPush(s.value));
                ret.add(new ActionGetMember());
            } else {
                ret.add(new ActionPush(s.value));
                ret.add(new ActionGetVariable());
            }
        }
        s = lex(lexer);
        while (s.isType(SymbolType.DOT, SymbolType.BRACKET_OPEN)) {
            if (s.type == SymbolType.BRACKET_OPEN) {
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
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

    private static List<Action> call(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        //expected(lexer, SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        int cnt = 0;
        ParsedSymbol s = lex(lexer);
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            cnt++;
            ret.addAll(0,expression(lexer, registerVars, inFunction, inMethod));
            s = lex(lexer);
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        ret.add(new ActionPush(new Long(cnt)));
        return ret;
    }

    private static List<Action> function(ActionScriptLexer lexer, boolean withBody, String functionName, boolean isMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = null;
        expected(lexer, SymbolType.PARENT_OPEN);
        s = lex(lexer);
        List<String> paramNames = new ArrayList<String>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex(lexer); 
            expected(s,lexer.yyline(), SymbolType.IDENTIFIER);
            paramNames.add(s.value.toString());
            s = lex(lexer);            
            if (s.type == SymbolType.COLON) {
                type(lexer);
                s = lex(lexer);
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }


        List<Integer> paramRegs = new ArrayList<Integer>();
        HashMap<String, Integer> registerVars = new HashMap<String, Integer>();
        registerVars.put("_parent", REGISTER_PARENT);
        registerVars.put("_root", REGISTER_ROOT);
        registerVars.put("super", REGISTER_SUPER);
        registerVars.put("arguments", REGISTER_ARGUMENTS);
        registerVars.put("this", REGISTER_THIS);
        registerVars.put("_global", REGISTER_GLOBAL);
        for (int i = 0; i < paramNames.size(); i++) {
            registerVars.put(paramNames.get(i), (7 + (paramNames.size()-i-1)));
        }
        boolean preloadParentFlag = false;
        boolean preloadRootFlag = false;
        boolean preloadSuperFlag = false;
        boolean preloadArgumentsFlag = false;
        boolean preloadThisFlag = false;
        boolean preloadGlobalFlag = false;

        boolean suppressParentFlag = false;
        boolean suppressArgumentsFlag = false;
        boolean suppressThisFlag = false;
        if (withBody) {
            HashSet<Integer> usedRegisters = new HashSet<Integer>();
            expected(lexer, SymbolType.CURLY_OPEN);
            List<Action> body = commands(lexer, registerVars, true, isMethod);
            for (Action a : body) {
                if (a instanceof ActionStoreRegister) {
                    usedRegisters.add(((ActionStoreRegister) a).registerNumber);
                }
                if (a instanceof ActionPush) {
                    ActionPush ap = (ActionPush) a;
                    for (Object o : ap.values) {
                        if (o instanceof RegisterNumber) {
                            usedRegisters.add(((RegisterNumber) o).number);
                        }
                    }
                }
            }
            if (usedRegisters.contains(REGISTER_PARENT)) {
                preloadParentFlag = true;
            } else {
                suppressParentFlag = true;
            }
            if (usedRegisters.contains(REGISTER_ROOT)) {
                preloadRootFlag = true;
            }
            if (usedRegisters.contains(REGISTER_SUPER)) {
                preloadSuperFlag = true;
            }
            if (usedRegisters.contains(REGISTER_ARGUMENTS)) {
                preloadArgumentsFlag = true;
            } else {
                suppressArgumentsFlag = true;
            }
            if (usedRegisters.contains(REGISTER_THIS)) {
                preloadThisFlag = true;
            } else {
                suppressThisFlag = true;
            }
            if (usedRegisters.contains(REGISTER_GLOBAL)) {
                preloadGlobalFlag = true;
            }

            int newpos = 0;
            HashMap<Integer, Integer> registerMap = new HashMap<Integer, Integer>();
            if (preloadParentFlag) {
                registerMap.put(1, newpos);
                newpos++;
            }
            if (preloadRootFlag) {
                registerMap.put(2, newpos);
                newpos++;
            }
            if (preloadSuperFlag) {
                registerMap.put(3, newpos);
                newpos++;
            }
            if (preloadArgumentsFlag) {
                registerMap.put(4, newpos);
                newpos++;
            }
            if (preloadThisFlag) {
                registerMap.put(5, newpos);
                newpos++;
            }
            if (preloadGlobalFlag) {
                registerMap.put(6, newpos);
                newpos++;
            }
            if(newpos<1){
                newpos=1;
            }
            for (int i = 0; i < paramNames.size(); i++) {
                registerMap.put(7 + i, newpos);
                paramRegs.add(newpos);
                newpos++;
            }

            for (Action a : body) {
                if (a instanceof ActionStoreRegister) {
                    if (registerMap.containsKey(((ActionStoreRegister) a).registerNumber)) {
                        ((ActionStoreRegister) a).registerNumber = registerMap.get(((ActionStoreRegister) a).registerNumber);
                    }
                }
                if (a instanceof ActionPush) {
                    ActionPush ap = (ActionPush) a;
                    for (Object o : ap.values) {
                        if (o instanceof RegisterNumber) {
                            if (registerMap.containsKey(((RegisterNumber) o).number)) {
                                ((RegisterNumber) o).number = registerMap.get(((RegisterNumber) o).number);
                            }
                        }
                    }
                }
            }

            expected(lexer, SymbolType.CURLY_CLOSE);
            ret.addAll(body);
        } else {
            for (int i = 0; i < paramNames.size(); i++) {
                paramRegs.add(1 + i);
            }
        }
        int len = Action.actionsToBytes(ret, false, SWF.DEFAULT_VERSION).length;
        ret.add(0, new ActionDefineFunction2(functionName,
                preloadParentFlag,
                preloadRootFlag,
                suppressParentFlag,
                preloadSuperFlag,
                suppressArgumentsFlag,
                preloadArgumentsFlag,
                suppressThisFlag,
                preloadThisFlag,
                preloadGlobalFlag,
                0, len, SWF.DEFAULT_VERSION, paramNames, paramRegs));
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

    private static List<Action> traits(ActionScriptLexer lexer, boolean isInterface, List<String> nameStr, List<String> extendsStr, List<List<String>> implementsStr) throws IOException, ParseException {

        List<Action> ret = new ArrayList<Action>();
        for (int i = 0; i < nameStr.size() - 1; i++) {
            List<Action> notBody = new ArrayList<Action>();
            List<String> globalClassTypeStr = new ArrayList<String>();
            globalClassTypeStr.add("_global");
            for (int j = 0; j <= i; j++) {
                globalClassTypeStr.add(nameStr.get(j));
            }
            notBody.add(new ActionPush((Long) 0L));
            notBody.add(new ActionPush("Object"));
            notBody.add(new ActionNewObject());
            notBody.addAll(typeToActions(globalClassTypeStr, true));
            ret.addAll(typeToActions(globalClassTypeStr, false));
            ret.add(new ActionNot());
            ret.add(new ActionNot());
            ret.add(new ActionIf(Action.actionsToBytes(notBody, false, SWF.DEFAULT_VERSION).length));
            ret.addAll(notBody);
        }
        List<Action> ifbody = new ArrayList<Action>();
        List<String> globalClassTypeStr = new ArrayList<String>();
        globalClassTypeStr.add("_global");
        globalClassTypeStr.addAll(nameStr);


        ParsedSymbol s = null;
        List<Action> constr = new ArrayList<Action>();
        looptrait:
        while (true) {
            s = lex(lexer);
            boolean isStatic = false;
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE)) {
                if (s.type == SymbolType.STATIC) {
                    isStatic = true;
                }
                s = lex(lexer);
            }
            switch (s.type) {
                case FUNCTION:
                    s = lex(lexer);
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String fname = s.value.toString();
                    if (fname.equals(nameStr.get(nameStr.size() - 1))) { //constructor
                        constr = new ArrayList<Action>();
                        constr.addAll(function(lexer, !isInterface, fname, true));
                        constr.add(new ActionStoreRegister(1));
                        constr.addAll(typeToActions(globalClassTypeStr, true));

                    } else {
                        ifbody.addAll(function(lexer, !isInterface, fname, true));
                        ifbody.add(new ActionPush(new RegisterNumber(isStatic ? 1 : 2)));
                        ifbody.add(new ActionPush(fname));
                        ifbody.add(new ActionSetMember());
                    }
                    break;
                case VAR:
                    s = lex(lexer);
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String ident = s.value.toString();
                    s = lex(lexer);
                    if (s.type == SymbolType.COLON) {
                        type(lexer);
                        s = lex(lexer);
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        ifbody.addAll(expression(lexer, new HashMap<String, Integer>(), false, false));
                        ifbody.add(new ActionPush(new RegisterNumber(isStatic ? 1 : 2)));
                        ifbody.add(new ActionPush(ident));
                        ifbody.add(new ActionSetMember());
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

        if (!isInterface) {
            ifbody.add(new ActionPush((Long) 1L));
            ifbody.add(new ActionPush(new Null()));
            ifbody.addAll(typeToActions(globalClassTypeStr, false));
            ifbody.add(new ActionPush("prototype"));
            ifbody.add(new ActionGetMember());
            ifbody.add(new ActionPush((Long) 3L));
            ifbody.add(new ActionPush("ASSetPropFlags"));
            ifbody.add(new ActionCallFunction());
        }

        if (constr.isEmpty()) {
            constr.add(new ActionDefineFunction(null, new ArrayList<String>(), 0, SWF.DEFAULT_VERSION));
            constr.add(new ActionStoreRegister(1));
            constr.addAll(typeToActions(globalClassTypeStr, true));
        }
        if (!extendsStr.isEmpty()) {
            constr.addAll(typeToActions(globalClassTypeStr, false));
            constr.addAll(typeToActions(extendsStr, false));
            constr.add(new ActionExtends());
        }
        constr.add(new ActionPush(new RegisterNumber(1)));
        constr.add(new ActionPush("prototype"));
        constr.add(new ActionGetMember());
        constr.add(new ActionStoreRegister(2));
        constr.add(new ActionPop());

        for (List<String> imp : implementsStr) {
            List<String> globImp = new ArrayList<String>();
            globImp.add("_global");
            globImp.addAll(imp);
            constr.addAll(typeToActions(globImp, false));
            constr.addAll(typeToActions(globalClassTypeStr, false));
            constr.add(new ActionImplementsOp());
        }
        ifbody.addAll(0, constr);

        ret.addAll(typeToActions(globalClassTypeStr, false));
        ret.add(new ActionNot());
        ret.add(new ActionNot());
        ret.add(new ActionIf(Action.actionsToBytes(ifbody, false, SWF.DEFAULT_VERSION).length));
        ret.addAll(ifbody);
        ret.add(new ActionPop());
        return ret;
    }

    private static List<Action> command(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        System.out.println("command:");
        ParsedSymbol s = lex(lexer);
        if (s.type == SymbolType.EOF) {
            return null;
        }
        switch (s.type) {
            case CLASS:
                List<String> classTypeStr = type(lexer);
                s = lex(lexer);
                List<String> extendsTypeStr = new ArrayList<String>();
                if (s.type == SymbolType.EXTENDS) {
                    extendsTypeStr = type(lexer);
                    s = lex(lexer);
                }
                List<List<String>> implementsTypeStrs = new ArrayList<List<String>>();
                if (s.type == SymbolType.IMPLEMENTS) {
                    do {
                        List<String> implementsTypeStr = type(lexer);
                        implementsTypeStrs.add(implementsTypeStr);
                        s = lex(lexer);
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret.addAll(traits(lexer, false, classTypeStr, extendsTypeStr, implementsTypeStrs));
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case INTERFACE:
                List<String> interfaceTypeStr = type(lexer);
                s = lex(lexer);
                List<List<String>> intExtendsTypeStrs = new ArrayList<List<String>>();

                if (s.type == SymbolType.EXTENDS) {
                    do {
                        List<String> intExtendsTypeStr = type(lexer);
                        intExtendsTypeStrs.add(intExtendsTypeStr);
                        s = lex(lexer);
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret.addAll(traits(lexer, true, interfaceTypeStr, new ArrayList<String>(), intExtendsTypeStrs));
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s = lexer.lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                ret.addAll(function(lexer, true, s.value.toString(), false));
                break;
            case NEW:
                List<Action> newcmds=new ArrayList<Action>();
                newcmds.addAll(typeToActions(type(lexer), false));
                expected(lexer, SymbolType.PARENT_OPEN);
                if (newcmds.get(newcmds.size() - 1) instanceof ActionGetMember) {
                    newcmds.remove(newcmds.size() - 1);
                    newcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                    newcmds.add(new ActionNewMethod());
                } else if (newcmds.get(newcmds.size() - 1) instanceof ActionGetVariable) {
                    newcmds.remove(newcmds.size() - 1);
                    newcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                    newcmds.add(new ActionNewObject());
                }
                ret.addAll(newcmds);
                break;
            case VAR:
                s = lex(lexer);
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                ret.add(new ActionPush(s.value));
                String varIdentifier = s.value.toString();
                s = lex(lexer);
                if (s.type == SymbolType.COLON) {
                    type(lexer);
                    s = lex(lexer);
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                    if (inFunction) {
                        for (int i = 0; i < 256; i++) {
                            if (!registerVars.containsValue(i)) {
                                registerVars.put(varIdentifier, i);
                                ret.add(new ActionStoreRegister(i));
                                ret.add(new ActionPop());
                                break;
                            }
                        }
                    } else {
                        ret.add(new ActionDefineLocal());
                    }
                } else {
                    if (inFunction) {
                        for (int i = 0; i < 256; i++) {
                            if (!registerVars.containsValue(i)) {
                                registerVars.put(varIdentifier, i);
                                break;
                            }
                        }
                    } else {
                        ret.add(new ActionDefineLocal2());
                    }
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret.addAll(commands(lexer, registerVars, inFunction, inMethod));
                expected(lexer, SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT:
            case DECREMENT:
                List<Action> varincdec = variable(lexer, registerVars, inFunction, inMethod);
                List<Action> incdecval = new ArrayList<Action>();
                incdecval.addAll(varincdec);
                if (s.type == SymbolType.INCREMENT) {
                    incdecval.add(new ActionIncrement());
                } else if (s.type == SymbolType.DECREMENT) {
                    incdecval.add(new ActionDecrement());
                }
                varincdec = gettoset(varincdec, incdecval);
                ret.addAll(varincdec);
                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                lexer.pushback(s);
                List<Action> var = variable(lexer, registerVars, inFunction, inMethod);
                s = lex(lexer);
                switch (s.type) {
                    case ASSIGN:
                        ret.addAll(var);
                        ret = gettoset(ret, expression(lexer, registerVars, inFunction, inMethod));
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
                        List<Action> varset = new ArrayList<Action>();
                        varset.addAll(var);
                        var.addAll(expression(lexer, registerVars, inFunction, inMethod));
                        switch (s.type) {
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
                        varset = gettoset(varset, var);
                        ret.addAll(varset);
                        break;
                    case INCREMENT:
                    case DECREMENT:
                        ret.addAll(var);
                        ret.addAll(var);
                        Action at = ret.remove(ret.size() - 1);
                        ret.addAll(var);
                        if (s.type == SymbolType.INCREMENT) {
                            ret.add(new ActionIncrement());
                        }
                        if (s.type == SymbolType.DECREMENT) {
                            ret.add(new ActionDecrement());
                        }
                        if (at instanceof ActionGetMember) {
                            ret.add(new ActionSetMember());
                        }
                        if (at instanceof ActionGetVariable) {
                            ret.add(new ActionSetVariable());
                        }
                        break;
                    case PARENT_OPEN: //function call
                        List<Action> callcmds=new ArrayList<Action>();
                        callcmds.addAll(var);
                        if (callcmds.get(callcmds.size() - 1) instanceof ActionGetMember) {
                            callcmds.remove(callcmds.size() - 1);
                            callcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                            callcmds.add(new ActionCallMethod());
                            callcmds.add(new ActionPop());
                        } else if (callcmds.get(callcmds.size() - 1) instanceof ActionGetVariable) {
                            callcmds.remove(callcmds.size() - 1);
                            ActionPush ap = (ActionPush) callcmds.get(callcmds.size() - 1);
                            if (ap.values.get(0).equals("trace")) {
                                callcmds.remove(callcmds.size() - 1);
                                callcmds.addAll(expression(lexer, registerVars, inFunction, inMethod));
                                expected(lexer, SymbolType.COMMA, SymbolType.PARENT_CLOSE);
                                callcmds.add(new ActionTrace());
                            } else {
                                callcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                                callcmds.add(new ActionCallFunction());
                                callcmds.add(new ActionPop());
                            }
                        }
                        ret.addAll(callcmds);
                        break;
                    default:
                        throw new ParseException("Not a command", lexer.yyline());
                }
                break;
            case IF:
                expected(lexer, SymbolType.PARENT_OPEN);
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> onTrue = command(lexer, registerVars, inFunction, inMethod);
                List<Action> onFalse = null;
                s = lex(lexer);
                if (s.type == SymbolType.ELSE) {
                    onFalse = command(lexer, registerVars, inFunction, inMethod);
                } else {
                    lexer.pushback(s);
                }
                byte onTrueBytes[] = Action.actionsToBytes(onTrue, false, SWF.DEFAULT_VERSION);
                int onTrueLen = onTrueBytes.length;
                ret.add(new ActionNot());
                ActionIf ifaif = new ActionIf(0);
                ret.add(ifaif);
                ret.addAll(onTrue);
                ifaif.setJumpOffset(onTrueLen);
                ActionJump ajmp = null;
                if (onFalse != null) {
                    if (!((!nonempty(onTrue).isEmpty())
                            && (onTrue.get(onTrue.size() - 1) instanceof ActionJump)
                            && ((((ActionJump) onTrue.get(onTrue.size() - 1)).isContinue)
                            || (((ActionJump) onTrue.get(onTrue.size() - 1)).isBreak)))) {
                        ajmp = new ActionJump(0);
                        ret.add(ajmp);
                        onTrueLen += ajmp.getBytes(SWF.DEFAULT_VERSION).length;
                    }
                    ifaif.setJumpOffset(onTrueLen);
                    byte onFalseBytes[] = Action.actionsToBytes(onFalse, false, SWF.DEFAULT_VERSION);
                    int onFalseLen = onFalseBytes.length;
                    if (ajmp != null) {
                        ajmp.setJumpOffset(onFalseLen);
                    }
                    ret.addAll(onFalse);
                }
                break;
            case WHILE:
                expected(lexer, SymbolType.PARENT_OPEN);
                List<Action> whileExpr = expression(lexer, registerVars, inFunction, inMethod);
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> whileBody = command(lexer, registerVars, inFunction, inMethod);
                whileExpr.add(new ActionNot());
                ActionIf whileaif = new ActionIf(0);
                whileExpr.add(whileaif);
                ActionJump whileajmp = new ActionJump(0);
                whileBody.add(whileajmp);
                int whileExprLen = Action.actionsToBytes(whileExpr, false, SWF.DEFAULT_VERSION).length;
                int whileBodyLen = Action.actionsToBytes(whileBody, false, SWF.DEFAULT_VERSION).length;
                whileajmp.setJumpOffset(-(whileExprLen
                        + whileBodyLen));
                whileaif.setJumpOffset(whileBodyLen);
                ret.addAll(whileExpr);
                fixLoop(whileBody, whileBodyLen, -whileExprLen);
                ret.addAll(whileBody);
                break;
            case DO:
                List<Action> doBody = command(lexer, registerVars, inFunction, inMethod);
                expected(lexer, SymbolType.WHILE);
                expected(lexer, SymbolType.PARENT_OPEN);
                List<Action> doExpr = expression(lexer, registerVars, inFunction, inMethod);

                expected(lexer, SymbolType.PARENT_CLOSE);
                int doBodyLen = Action.actionsToBytes(doBody, false, SWF.DEFAULT_VERSION).length;
                int doExprLen = Action.actionsToBytes(doExpr, false, SWF.DEFAULT_VERSION).length;

                ret.addAll(doBody);
                ret.addAll(doExpr);
                ActionIf doif = new ActionIf(0);
                ret.add(doif);
                doif.setJumpOffset(-doBodyLen - doExprLen - doif.getBytes(SWF.DEFAULT_VERSION).length);
                fixLoop(doBody, doBodyLen + doExprLen + doif.getBytes(SWF.DEFAULT_VERSION).length, doBodyLen);
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
                            expression(lexer, registerVars, inFunction, inMethod);
                            forin = true;
                        }
                    } else {
                        lexer.pushback(s2);
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                }
                List<Action> forFinalCommands = new ArrayList<Action>();
                List<Action> forExpr = new ArrayList<Action>();
                if (!forin) {
                    ret.addAll(nonempty(command(lexer, registerVars, inFunction, inMethod)));
                    expected(lexer, SymbolType.SEMICOLON);
                    forExpr = expression(lexer, registerVars, inFunction, inMethod);
                    expected(lexer, SymbolType.SEMICOLON);
                    forFinalCommands = command(lexer, registerVars, inFunction, inMethod);
                }
                expected(lexer, SymbolType.PARENT_CLOSE);
                List<Action> forBody = command(lexer, registerVars, inFunction, inMethod);
                forExpr.add(new ActionNot());
                ActionIf foraif = new ActionIf(0);
                forExpr.add(foraif);
                ActionJump forajmp = new ActionJump(0);
                int forajmpLen = forajmp.getBytes(SWF.DEFAULT_VERSION).length;
                int forExprLen = Action.actionsToBytes(forExpr, false, SWF.DEFAULT_VERSION).length;
                int forBodyLen = Action.actionsToBytes(forBody, false, SWF.DEFAULT_VERSION).length;
                int forFinalLen = Action.actionsToBytes(forFinalCommands, false, SWF.DEFAULT_VERSION).length;
                forajmp.setJumpOffset(-(forExprLen
                        + forBodyLen + forFinalLen + forajmpLen));
                foraif.setJumpOffset(forBodyLen + forFinalLen + forajmpLen);
                ret.addAll(forExpr);
                ret.addAll(forBody);
                ret.addAll(forFinalCommands);
                ret.add(forajmp);
                fixLoop(forBody, forBodyLen + forFinalLen + forajmpLen, forBodyLen);
                break;
            case SWITCH:
                expected(lexer, SymbolType.PARENT_OPEN);
                List<Action> switchExpr = expression(lexer, registerVars, inFunction, inMethod);
                expected(lexer, SymbolType.PARENT_CLOSE);
                expected(lexer, SymbolType.CURLY_OPEN);
                s = lex(lexer);
                ret.addAll(switchExpr);
                int exprReg = 0;
                for (int i = 0; i < 256; i++) {
                    if (!registerVars.containsValue(i)) {
                        registerVars.put("__switch" + uniqId(), i);
                        exprReg = i;
                        break;
                    }
                }
                boolean firstCase = true;
                List<List<ActionIf>> caseIfs = new ArrayList<List<ActionIf>>();
                List<List<Action>> caseCmds = new ArrayList<List<Action>>();
                List<List<List<Action>>> caseExprsAll = new ArrayList<List<List<Action>>>();
                while (s.type == SymbolType.CASE) {
                    List<List<Action>> caseExprs = new ArrayList<List<Action>>();
                    List<ActionIf> caseIfsOne = new ArrayList<ActionIf>();
                    while (s.type == SymbolType.CASE) {
                        List<Action> curCaseExpr = expression(lexer, registerVars, inFunction, inMethod);
                        caseExprs.add(curCaseExpr);
                        if (firstCase) {
                            curCaseExpr.add(0,new ActionStoreRegister(exprReg));
                        } else {
                            curCaseExpr.add(0, new ActionPush(new RegisterNumber(exprReg)));
                        }
                        curCaseExpr.add(new ActionStrictEquals());
                        ActionIf aif = new ActionIf(0);
                        caseIfsOne.add(aif);
                        curCaseExpr.add(aif);
                        ret.addAll(curCaseExpr);
                        firstCase = false;
                        expected(lexer, SymbolType.COLON);
                        s = lex(lexer);
                    }
                    caseExprsAll.add(caseExprs);
                    caseIfs.add(caseIfsOne);
                    lexer.pushback(s);
                    List<Action> caseCmd = commands(lexer, registerVars, inFunction, inMethod);
                    caseCmds.add(caseCmd);
                    s = lex(lexer);
                }
                ActionJump defJump = new ActionJump(0);
                ret.add(defJump);
                List<Action> defCmd = new ArrayList<Action>();
                if (s.type == SymbolType.DEFAULT) {
                    expected(lexer, SymbolType.COLON);
                    defCmd = commands(lexer, registerVars, inFunction, inMethod);
                    s=lexer.lex();
                }
                for (List<Action> caseCmd : caseCmds) {
                    ret.addAll(caseCmd);
                }
                ret.addAll(defCmd);

                List<List<Integer>> exprLengths = new ArrayList<List<Integer>>();
                for (List<List<Action>> caseExprs : caseExprsAll) {
                    List<Integer> lengths = new ArrayList<Integer>();
                    for (List<Action> caseExpr : caseExprs) {
                        lengths.add(Action.actionsToBytes(caseExpr, false, SWF.DEFAULT_VERSION).length);
                    }
                    exprLengths.add(lengths);
                }
                List<Integer> caseLengths = new ArrayList<Integer>();
                for (List<Action> caseCmd : caseCmds) {
                    caseLengths.add(Action.actionsToBytes(caseCmd, false, SWF.DEFAULT_VERSION).length);
                }
                int defLength = Action.actionsToBytes(defCmd, false, SWF.DEFAULT_VERSION).length;

                for (int i = 0; i < caseIfs.size(); i++) {
                    for (int c = 0; c < caseIfs.get(i).size(); c++) {
                        int jmpPos = 0;
                        for (int j = c+1; j < caseIfs.get(i).size(); j++) {
                            jmpPos += exprLengths.get(i).get(j);
                        }
                        for (int k = i + 1; k < caseIfs.size(); k++) {
                            for (int m = 0; m < caseIfs.get(k).size(); m++) {
                                jmpPos += exprLengths.get(k).get(m);
                            }
                        }
                        jmpPos += defJump.getBytes(SWF.DEFAULT_VERSION).length;
                        for (int n = 0; n < i; n++) {
                            jmpPos += caseLengths.get(n);
                        }
                        caseIfs.get(i).get(c).setJumpOffset(jmpPos);
                    }
                }
                int defJmpPos = 0;
                for (int i = 0; i < caseIfs.size(); i++) {
                    defJmpPos += caseLengths.get(i);
                }

                defJump.setJumpOffset(defJmpPos);
                List<Action> caseCmdsAll = new ArrayList<Action>();
                int breakOffset = 0;
                for (int i = 0; i < caseCmds.size(); i++) {
                    caseCmdsAll.addAll(caseCmds.get(i));
                    breakOffset += caseLengths.get(i);
                }
                breakOffset += defLength;
                fixLoop(caseCmdsAll, breakOffset);

                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                break;
            case BREAK:
                ActionJump abreak = new ActionJump(0);
                abreak.isBreak = true;
                ret.add(abreak);
                break;
            case CONTINUE:
                ActionJump acontinue = new ActionJump(0);
                acontinue.isContinue = true;
                ret.add(acontinue);
                break;
            case RETURN:
                List<Action> retexpr = expression(lexer, true, registerVars, inFunction, inMethod);
                if (retexpr.isEmpty()) {
                    ret.add(new ActionPush(new Undefined()));
                }
                ret.add(new ActionReturn());
                break;
            case TRY:
                List<Action> tryCommands = command(lexer, registerVars, inFunction, inMethod);
                s = lex(lexer);
                boolean found = false;
                List<Action> catchCommands = null;
                String catchName = null;
                int catchSize = 0;
                if (s.type == SymbolType.CATCH) {
                    expected(lexer, SymbolType.PARENT_OPEN);
                    s = lex(lexer);
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    catchName = s.value.toString();
                    expected(lexer, SymbolType.PARENT_CLOSE);
                    catchCommands = command(lexer, registerVars, inFunction, inMethod);
                    s = lex(lexer);
                    found = true;
                    catchSize = Action.actionsToBytes(catchCommands, false, SWF.DEFAULT_VERSION).length;
                    tryCommands.add(new ActionJump(catchSize));
                }
                List<Action> finallyCommands = null;
                int finallySize = 0;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = command(lexer, registerVars, inFunction, inMethod);
                    found = true;
                    s = lex(lexer);
                    finallySize = Action.actionsToBytes(finallyCommands, false, SWF.DEFAULT_VERSION).length;
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                int trySize = Action.actionsToBytes(tryCommands, false, SWF.DEFAULT_VERSION).length;

                ret.add(new ActionTry(false, finallyCommands != null, catchCommands != null, catchName, 0, trySize, catchSize, finallySize, SWF.DEFAULT_VERSION));
                ret.addAll(tryCommands);
                if (catchCommands != null) {
                    ret.addAll(catchCommands);
                }
                if (finallyCommands != null) {
                    ret.addAll(finallyCommands);
                }
                break;
            case THROW:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionThrow());
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

    private static List<Action> expression(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        return expression(lexer, false, registerVars, inFunction, inMethod);
    }

    private static List<Action> expressionRemainder(ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        return expressionRemainder(new ArrayList<Action>(), lexer, registerVars, inFunction, inMethod);
    }

    private static List<Action> expressionRemainder(List<Action> expr, ActionScriptLexer lexer, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
        List<Action> ret = new ArrayList<Action>();
        ParsedSymbol s = lex(lexer);
        switch (s.type) {
            case BITAND:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionBitAnd());
                break;
            case BITOR:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionBitOr());
                break;
            case DIVIDE:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionDivide());
                break;
            case EQUALS:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionEquals2());
                break;
            case STRICT_EQUALS:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionStrictEquals());
                break;
            case NOT_EQUAL:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionEquals());
                ret.add(new ActionNot());
                break;
            case STRICT_NOT_EQUAL:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionStrictEquals());
                ret.add(new ActionNot());
                break;
            case LOWER_THAN:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionLess2());
                break;
            case LOWER_EQUAL:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionGreater());
                ret.add(new ActionNot());
                break;
            case GREATER_THAN:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionGreater());
                break;
            case GREATER_EQUAL:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionLess2());
                ret.add(new ActionNot());
                break;
            case AND:
                ret.add(new ActionPushDuplicate());
                ret.add(new ActionNot());
                List<Action> andExpr = expression(lexer, registerVars, inFunction, inMethod);
                andExpr.add(0, new ActionPop());
                int andExprLen = Action.actionsToBytes(andExpr, false, SWF.DEFAULT_VERSION).length;
                ret.add(new ActionIf(andExprLen));
                ret.addAll(andExpr);
                break;
            case OR:
                ret.add(new ActionPushDuplicate());
                List<Action> orExpr = expression(lexer, registerVars, inFunction, inMethod);
                orExpr.add(0, new ActionPop());
                int orExprLen = Action.actionsToBytes(orExpr, false, SWF.DEFAULT_VERSION).length;
                ret.add(new ActionIf(orExprLen));
                ret.addAll(orExpr);
                break;
            case MINUS:
                List<Action> minusExp = expression(lexer, registerVars, inFunction, inMethod);
                if ((minusExp.size() == 1) && (minusExp.get(0) instanceof ActionPush) && (((ActionPush) minusExp.get(0)).values.get(0).equals(new Long(1)))) {
                    ret.add(new ActionDecrement());
                } else {
                    ret.addAll(minusExp);
                    ret.add(new ActionSubtract());
                }
                break;
            case MULTIPLY:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionMultiply());
                break;
            case PLUS:
                List<Action> plusExp = expression(lexer, registerVars, inFunction, inMethod);
                if ((plusExp.size() == 1) && (plusExp.get(0) instanceof ActionPush) && (((ActionPush) plusExp.get(0)).values.get(0).equals(new Long(1)))) {
                    ret.add(new ActionIncrement());
                } else {
                    ret.addAll(plusExp);
                    ret.add(new ActionAdd2());
                }
                break;
            case XOR:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionBitXor());
                break;
            case AS:

                break;
            case INSTANCEOF:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionInstanceOf());
                break;
            case IS:

                break;
            case TYPEOF:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionTypeOf());
                break;
            default:
                lexer.pushback(s);
        }
        return ret;
    }

    private static List<Action> expression(ActionScriptLexer lexer, boolean allowEmpty, HashMap<String, Integer> registerVars, boolean inFunction, boolean inMethod) throws IOException, ParseException {
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
                    ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                    expected(lexer, SymbolType.COLON);
                    ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
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
                    expression(lexer, registerVars, inFunction, inMethod);
                    s = lex(lexer);
                    if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                    }
                }
                ret.add(new ActionPush(new Long(arrCnt)));
                ret.add(new ActionInitObject());

                break;
            case FUNCTION:
                s = lexer.lex();
                String fname = "";
                if (s.type == SymbolType.IDENTIFIER) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                ret.addAll(function(lexer, true, fname, false));
                break;
            case STRING:
            case INTEGER:
            case DOUBLE:
                ret.add(new ActionPush(s.value));
                ret.addAll(expressionRemainder(lexer, registerVars, inFunction, inMethod));
                break;
            case DELETE:
                ret.addAll(variable(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionDelete());
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                List<Action> prevar = variable(lexer, registerVars, inFunction, inMethod);
                ret.addAll(prevar);
                Action prelast = ret.remove(ret.size() - 1);
                ret.addAll(prevar);
                if (s.type == SymbolType.INCREMENT) {
                    ret.add(new ActionIncrement());
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret.add(new ActionDecrement());
                }
                int tmpReg = 0;
                for (int i = 0; i < 256; i++) {
                    if (!registerVars.containsValue(i)) {
                        tmpReg = i;
                        break;
                    }
                }
                ret.add(new ActionStoreRegister(tmpReg));
                if (prelast instanceof ActionGetVariable) {
                    ret.add(new ActionSetVariable());
                }
                if (prelast instanceof ActionGetMember) {
                    ret.add(new ActionSetMember());
                }
                ret.add(new ActionPush(new RegisterNumber(tmpReg)));
                ret.addAll(expressionRemainder(lexer, registerVars, inFunction, inMethod));
                break;
            case NOT:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                ret.add(new ActionNot());
                ret.addAll(expressionRemainder(lexer, registerVars, inFunction, inMethod));
                break;
            case PARENT_OPEN:
                ret.addAll(expression(lexer, registerVars, inFunction, inMethod));
                expected(lexer, SymbolType.PARENT_CLOSE);
                ret.addAll(expressionRemainder(lexer, registerVars, inFunction, inMethod));
                break;
            case NEW:
                List<Action> newcmds=new ArrayList<Action>();
                newcmds.addAll(variable(lexer, registerVars, inFunction, inMethod));
                expected(lexer, SymbolType.PARENT_OPEN);
                if (newcmds.get(newcmds.size() - 1) instanceof ActionGetMember) {
                    newcmds.remove(newcmds.size() - 1);
                    newcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                    newcmds.add(new ActionNewMethod());
                } else if (newcmds.get(newcmds.size() - 1) instanceof ActionGetVariable) {
                    newcmds.remove(newcmds.size() - 1);
                    newcmds.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                    newcmds.add(new ActionNewObject());
                }
                ret.addAll(newcmds);

                break;
            case IDENTIFIER:
            case THIS:
                lexer.pushback(s);
                List<Action> var = variable(lexer, registerVars, inFunction, inMethod);

                s = lex(lexer);
                switch (s.type) {
                    case ASSIGN:
                        var = gettoset(var, expression(lexer, registerVars, inFunction, inMethod));
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
                        List<Action> varset = new ArrayList<Action>();
                        varset.addAll(var);
                        var.addAll(expression(lexer, registerVars, inFunction, inMethod));
                        switch (s.type) {
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
                        int asTmpReg = 0;
                        for (int i = 0; i < 256; i++) {
                            if (!registerVars.containsValue(i)) {
                                asTmpReg = i;
                                break;
                            }
                        }
                        var.add(new ActionStoreRegister(asTmpReg));
                        varset = gettoset(varset, var);
                        ret.addAll(varset);
                        ret.add(new ActionPush(new RegisterNumber(asTmpReg)));
                        break;
                    case INCREMENT:
                    case DECREMENT:
                        ret.addAll(var);
                        ret.addAll(var);
                        Action at = ret.remove(ret.size() - 1);
                        ret.addAll(var);
                        if (s.type == SymbolType.INCREMENT) {
                            ret.add(new ActionIncrement());
                        }
                        if (s.type == SymbolType.DECREMENT) {
                            ret.add(new ActionDecrement());
                        }
                        if (at instanceof ActionGetMember) {
                            ret.add(new ActionSetMember());
                        }
                        if (at instanceof ActionGetVariable) {
                            ret.add(new ActionSetVariable());
                        }
                        break;
                    case PARENT_OPEN: //function call
                        if (var.get(var.size() - 1) instanceof ActionGetMember) {
                            var.remove(var.size() - 1);
                            var.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                            var.add(new ActionCallMethod());
                        } else if (var.get(var.size() - 1) instanceof ActionGetVariable) {
                            var.remove(var.size() - 1);
                            var.addAll(0,call(lexer, registerVars, inFunction, inMethod));
                            var.add(new ActionCallFunction());
                        }
                        ret.addAll(var);
                        break;
                    default:
                        ret.addAll(var);
                        lexer.pushback(s);
                        ret.addAll(expressionRemainder(lexer, registerVars, inFunction, inMethod));
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


        ret.addAll(commands(lexer, new HashMap<String, Integer>(), false, false));

        if (lexer.lex().type!=SymbolType.EOF) {
            throw new ParseException("Parsing finisned before end of the file", lexer.yyline());
        }
        Action.setActionsAddresses(ret, 0, SWF.DEFAULT_VERSION);
        return ret;
    }
}
