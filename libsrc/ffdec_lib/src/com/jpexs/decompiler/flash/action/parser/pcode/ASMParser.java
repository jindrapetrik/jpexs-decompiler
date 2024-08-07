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
package com.jpexs.decompiler.flash.action.parser.pcode;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.flashlite.ActionFSCommand2;
import com.jpexs.decompiler.flash.action.flashlite.ActionStrictMode;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.special.*;
import com.jpexs.decompiler.flash.action.swf3.*;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.swf6.*;
import com.jpexs.decompiler.flash.action.swf7.*;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.helpers.Helper;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ActionScript 1-2 parser.
 * @author JPEXS
 */
public class ASMParser {

    private static final Logger logger = Logger.getLogger(ASMParser.class.getName());

    private static Action parseAction(String instructionName, FlasmLexer lexer, List<String> constantPool, int version, String charset) throws IOException, ActionParseException {
        Action a = null;
        if (instructionName.compareToIgnoreCase("GetURL") == 0) {
            a = new ActionGetURL(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("GoToLabel") == 0) {
            a = new ActionGoToLabel(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("GotoFrame") == 0) {
            a = new ActionGotoFrame(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("NextFrame") == 0) {
            a = new ActionNextFrame();
        } else if (instructionName.compareToIgnoreCase("Play") == 0) {
            a = new ActionPlay();
        } else if (instructionName.compareToIgnoreCase("PrevFrame") == 0) {
            a = new ActionPrevFrame();
        } else if (instructionName.compareToIgnoreCase("SetTarget") == 0) {
            a = new ActionSetTarget(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Stop") == 0) {
            a = new ActionStop();
        } else if (instructionName.compareToIgnoreCase("StopSounds") == 0) {
            a = new ActionStopSounds();
        } else if (instructionName.compareToIgnoreCase("ToggleQuality") == 0) {
            a = new ActionToggleQuality();
        } else if (instructionName.compareToIgnoreCase("WaitForFrame") == 0) {
            a = new ActionWaitForFrame(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Add") == 0) {
            a = new ActionAdd();
        } else if (instructionName.compareToIgnoreCase("And") == 0) {
            a = new ActionAnd();
        } else if (instructionName.compareToIgnoreCase("AsciiToChar") == 0) {
            a = new ActionAsciiToChar();
        } else if (instructionName.compareToIgnoreCase("Call") == 0) {
            a = new ActionCall();
        } else if (instructionName.compareToIgnoreCase("CharToAscii") == 0) {
            a = new ActionCharToAscii();
        } else if (instructionName.compareToIgnoreCase("CloneSprite") == 0) {
            a = new ActionCloneSprite();
        } else if (instructionName.compareToIgnoreCase("Divide") == 0) {
            a = new ActionDivide();
        } else if (instructionName.compareToIgnoreCase("EndDrag") == 0) {
            a = new ActionEndDrag();
        } else if (instructionName.compareToIgnoreCase("Equals") == 0) {
            a = new ActionEquals(charset);
        } else if (instructionName.compareToIgnoreCase("GetProperty") == 0) {
            a = new ActionGetProperty();
        } else if (instructionName.compareToIgnoreCase("GetTime") == 0) {
            a = new ActionGetTime();
        } else if (instructionName.compareToIgnoreCase("GetURL2") == 0) {
            a = new ActionGetURL2(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("GetVariable") == 0) {
            a = new ActionGetVariable();
        } else if (instructionName.compareToIgnoreCase("GotoFrame2") == 0) {
            a = new ActionGotoFrame2(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("If") == 0) {
            a = new ActionIf(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Jump") == 0) {
            a = new ActionJump(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Less") == 0) {
            a = new ActionLess();
        } else if (instructionName.compareToIgnoreCase("MBAsciiToChar") == 0) {
            a = new ActionMBAsciiToChar();
        } else if (instructionName.compareToIgnoreCase("MBCharToAscii") == 0) {
            a = new ActionMBCharToAscii();
        } else if (instructionName.compareToIgnoreCase("MBStringExtract") == 0) {
            a = new ActionMBStringExtract();
        } else if (instructionName.compareToIgnoreCase("MBStringLength") == 0) {
            a = new ActionMBStringLength();
        } else if (instructionName.compareToIgnoreCase("Multiply") == 0) {
            a = new ActionMultiply();
        } else if (instructionName.compareToIgnoreCase("Not") == 0) {
            a = new ActionNot();
        } else if (instructionName.compareToIgnoreCase("Or") == 0) {
            a = new ActionOr();
        } else if (instructionName.compareToIgnoreCase("Pop") == 0) {
            a = new ActionPop();
        } else if (instructionName.compareToIgnoreCase("Push") == 0) {
            a = new ActionPush(lexer, constantPool, charset);
        } else if (instructionName.compareToIgnoreCase("RandomNumber") == 0) {
            a = new ActionRandomNumber();
        } else if (instructionName.compareToIgnoreCase("RemoveSprite") == 0) {
            a = new ActionRemoveSprite();
        } else if (instructionName.compareToIgnoreCase("SetProperty") == 0) {
            a = new ActionSetProperty();
        } else if (instructionName.compareToIgnoreCase("SetTarget2") == 0) {
            a = new ActionSetTarget2(charset);
        } else if (instructionName.compareToIgnoreCase("SetVariable") == 0) {
            a = new ActionSetVariable();
        } else if (instructionName.compareToIgnoreCase("StartDrag") == 0) {
            a = new ActionStartDrag();
        } else if (instructionName.compareToIgnoreCase("StringAdd") == 0) {
            a = new ActionStringAdd();
        } else if (instructionName.compareToIgnoreCase("StringEquals") == 0) {
            a = new ActionStringEquals();
        } else if (instructionName.compareToIgnoreCase("StringExtract") == 0) {
            a = new ActionStringExtract();
        } else if (instructionName.compareToIgnoreCase("StringLength") == 0) {
            a = new ActionStringLength();
        } else if (instructionName.compareToIgnoreCase("StringLess") == 0) {
            a = new ActionStringLess();
        } else if (instructionName.compareToIgnoreCase("Subtract") == 0) {
            a = new ActionSubtract();
        } else if (instructionName.compareToIgnoreCase("ToInteger") == 0) {
            a = new ActionToInteger();
        } else if (instructionName.compareToIgnoreCase("Trace") == 0) {
            a = new ActionTrace();
        } else if (instructionName.compareToIgnoreCase("WaitForFrame2") == 0) {
            a = new ActionWaitForFrame2(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Add2") == 0) {
            a = new ActionAdd2();
        } else if (instructionName.compareToIgnoreCase("BitAnd") == 0) {
            a = new ActionBitAnd();
        } else if (instructionName.compareToIgnoreCase("BitLShift") == 0) {
            a = new ActionBitLShift();
        } else if (instructionName.compareToIgnoreCase("BitOr") == 0) {
            a = new ActionBitOr();
        } else if (instructionName.compareToIgnoreCase("BitRShift") == 0) {
            a = new ActionBitRShift();
        } else if (instructionName.compareToIgnoreCase("BitURShift") == 0) {
            a = new ActionBitURShift();
        } else if (instructionName.compareToIgnoreCase("BitXor") == 0) {
            a = new ActionBitXor();
        } else if (instructionName.compareToIgnoreCase("CallFunction") == 0) {
            a = new ActionCallFunction();
        } else if (instructionName.compareToIgnoreCase("CallMethod") == 0) {
            a = new ActionCallMethod();
        } else if (instructionName.compareToIgnoreCase("ConstantPool") == 0) {
            a = new ActionConstantPool(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Decrement") == 0) {
            a = new ActionDecrement();
        } else if (instructionName.compareToIgnoreCase("DefineFunction") == 0) {
            a = new ActionDefineFunction(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("DefineLocal") == 0) {
            a = new ActionDefineLocal();
        } else if (instructionName.compareToIgnoreCase("DefineLocal2") == 0) {
            a = new ActionDefineLocal2();
        } else if (instructionName.compareToIgnoreCase("Delete") == 0) {
            a = new ActionDelete();
        } else if (instructionName.compareToIgnoreCase("Delete2") == 0) {
            a = new ActionDelete2();
        } else if (instructionName.compareToIgnoreCase("Enumerate") == 0) {
            a = new ActionEnumerate();
        } else if (instructionName.compareToIgnoreCase("Equals2") == 0) {
            a = new ActionEquals2();
        } else if (instructionName.compareToIgnoreCase("GetMember") == 0) {
            a = new ActionGetMember();
        } else if (instructionName.compareToIgnoreCase("Increment") == 0) {
            a = new ActionIncrement();
        } else if (instructionName.compareToIgnoreCase("InitArray") == 0) {
            a = new ActionInitArray();
        } else if (instructionName.compareToIgnoreCase("InitObject") == 0) {
            a = new ActionInitObject();
        } else if (instructionName.compareToIgnoreCase("Less2") == 0) {
            a = new ActionLess2();
        } else if (instructionName.compareToIgnoreCase("Modulo") == 0) {
            a = new ActionModulo();
        } else if (instructionName.compareToIgnoreCase("NewMethod") == 0) {
            a = new ActionNewMethod();
        } else if (instructionName.compareToIgnoreCase("NewObject") == 0) {
            a = new ActionNewObject();
        } else if (instructionName.compareToIgnoreCase("PushDuplicate") == 0) {
            a = new ActionPushDuplicate(charset);
        } else if (instructionName.compareToIgnoreCase("Return") == 0) {
            a = new ActionReturn();
        } else if (instructionName.compareToIgnoreCase("SetMember") == 0) {
            a = new ActionSetMember();
        } else if (instructionName.compareToIgnoreCase("StackSwap") == 0) {
            a = new ActionStackSwap(charset);
        } else if (instructionName.compareToIgnoreCase("StoreRegister") == 0) {
            a = new ActionStoreRegister(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("TargetPath") == 0) {
            a = new ActionTargetPath();
        } else if (instructionName.compareToIgnoreCase("ToNumber") == 0) {
            a = new ActionToNumber();
        } else if (instructionName.compareToIgnoreCase("ToString") == 0) {
            a = new ActionToString();
        } else if (instructionName.compareToIgnoreCase("TypeOf") == 0) {
            a = new ActionTypeOf();
        } else if (instructionName.compareToIgnoreCase("With") == 0) {
            a = new ActionWith(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Enumerate2") == 0) {
            a = new ActionEnumerate2();
        } else if (instructionName.compareToIgnoreCase("Greater") == 0) {
            a = new ActionGreater();
        } else if (instructionName.compareToIgnoreCase("InstanceOf") == 0) {
            a = new ActionInstanceOf();
        } else if (instructionName.compareToIgnoreCase("StrictEquals") == 0) {
            a = new ActionStrictEquals();
        } else if (instructionName.compareToIgnoreCase("StringGreater") == 0) {
            a = new ActionStringGreater();
        } else if (instructionName.compareToIgnoreCase("CastOp") == 0) {
            a = new ActionCastOp();
        } else if (instructionName.compareToIgnoreCase("DefineFunction2") == 0) {
            a = new ActionDefineFunction2(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Extends") == 0) {
            a = new ActionExtends(charset);
        } else if (instructionName.compareToIgnoreCase("ImplementsOp") == 0) {
            a = new ActionImplementsOp(charset);
        } else if (instructionName.compareToIgnoreCase("Throw") == 0) {
            a = new ActionThrow();
        } else if (instructionName.compareToIgnoreCase("Try") == 0) {
            a = new ActionTry(lexer, version, charset);
        } else if (instructionName.compareToIgnoreCase("FSCommand2") == 0) {
            a = new ActionFSCommand2(charset);
        } else if (instructionName.compareToIgnoreCase("StrictMode") == 0) {
            a = new ActionStrictMode(lexer, charset);
        } else if (instructionName.compareToIgnoreCase("Nop") == 0) {
            a = new ActionNop(charset);
        } else if (instructionName.compareToIgnoreCase("End") == 0) {
            a = new ActionEnd(charset);
        } else if (instructionName.compareToIgnoreCase("FFDec_DeobfuscatePop") == 0) {
            a = new ActionDeobfuscatePop();
        } else if (instructionName.compareToIgnoreCase("FFDec_DeobfuscateJump") == 0) {
            a = new ActionDeobfuscateJump(lexer, charset);
        } else if (instructionName.length() == 10 && instructionName.substring(0, 8).compareToIgnoreCase("Unknown_") == 0) {
            int actionCode = Integer.parseInt(instructionName.substring(8), 16);
            a = new ActionUnknown(actionCode, 0, charset);
        } else {
            throw new ActionParseException("Unknown instruction name :" + instructionName, lexer.yyline());
        }

        a.updateLength();
        return a;
    }

    private static List<Action> parseAllActions(FlasmLexer lexer, int version, String charset) throws IOException, ActionParseException {
        List<Action> list = new ArrayList<>();
        Stack<GraphSourceItemContainer> containers = new Stack<>();
        List<String> emptyList = new ArrayList<>();
        while (true) {
            ASMParsedSymbol symb = lexer.lex();
            if (symb.type == ASMParsedSymbol.TYPE_BLOCK_END) {
                if (containers.isEmpty()) {
                    throw new ActionParseException("Block end without start", lexer.yyline());
                }
                GraphSourceItemContainer a = containers.peek();
                if (!a.parseDivision(0, lexer)) {
                    containers.pop();
                }
            } else if (symb.type == ASMParsedSymbol.TYPE_INSTRUCTION_NAME) {
                String instructionName = (String) symb.value;
                Action a = parseAction(instructionName, lexer, emptyList, version, charset);
                if (a instanceof GraphSourceItemContainer) {
                    containers.push((GraphSourceItemContainer) a);
                }
                if (a != null) {
                    list.add(a);
                }
            } else if ((symb.type == ASMParsedSymbol.TYPE_BLOCK_END) || (symb.type == ASMParsedSymbol.TYPE_EOF)) {
                return list;
            }
        }
    }

    public static ActionList parse(boolean ignoreNops, List<Label> labels, Map<Action, Integer> lineMap, long address, FlasmLexer lexer, List<String> constantPool, int version, String charset) throws IOException, ActionParseException {
        ActionList list = new ActionList(charset);
        Stack<GraphSourceItemContainer> containers = new Stack<>();

        ActionConstantPool cpool = new ActionConstantPool(constantPool, charset);
        cpool.setAddress(address);
        address += cpool.getTotalActionLength();
        list.add(cpool);

        while (true) {
            ASMParsedSymbol symb = lexer.lex();
            if (symb.type == ASMParsedSymbol.TYPE_LABEL) {
                labels.add(new Label((String) symb.value, address));
            } else if (symb.type == ASMParsedSymbol.TYPE_COMMENT) {
                if (!list.isEmpty()) {
                    String cmt = (String) symb.value;
                    if (cmt.equals("compileTimeJump")) {
                        Action a = list.get(list.size() - 1);
                        if (a instanceof ActionIf) {
                            ((ActionIf) a).ignoreUsed = false;
                        }
                    } else if (cmt.equals("compileTimeIgnore")) {
                        Action a = list.get(list.size() - 1);
                        if (a instanceof ActionIf) {
                            ((ActionIf) a).jumpUsed = false;
                        }
                    }
                }
            } else if (symb.type == ASMParsedSymbol.TYPE_BLOCK_END) {
                if (containers.isEmpty()) {
                    throw new ActionParseException("Block end without start", lexer.yyline());
                }
                GraphSourceItemContainer a = containers.peek();
                if (!a.parseDivision(address - ((Action) a).getAddress(), lexer)) {
                    containers.pop();
                }
            } else if (symb.type == ASMParsedSymbol.TYPE_INSTRUCTION_NAME) {
                String instructionName = (String) symb.value;
                Action a = parseAction(instructionName, lexer, constantPool, version, charset);
                if (ignoreNops && a instanceof ActionNop) {
                    a = null;
                }
                if (a instanceof ActionConstantPool) {
                    a = null;
                }
                if (a instanceof ActionNop) {
                    a.setAddress(address);
                    address += 1;
                } else if (a != null) {
                    a.setAddress(address);
                    address += a.getTotalActionLength();
                }
                if (a instanceof GraphSourceItemContainer) {
                    containers.push((GraphSourceItemContainer) a);
                }
                if (a != null) {
                    list.add(a);
                    lineMap.put(a, lexer.yyline());
                }
            } else if (symb.type == ASMParsedSymbol.TYPE_EOL) {
                //empty
            } else if ((symb.type == ASMParsedSymbol.TYPE_BLOCK_END) || (symb.type == ASMParsedSymbol.TYPE_EOF)) {
                return list;
            } else {
                throw new ActionParseException("Label or Instruction name expected, found:" + symb.type + " " + symb.value, lexer.yyline());
            }
        }
    }

    public static ActionList parse(long address, boolean ignoreNops, String source, int version, boolean throwOnError, String charset) throws IOException, ActionParseException {
        FlasmLexer lexer = new FlasmLexer(new StringReader(source));
        List<Action> list = parseAllActions(lexer, version, charset);

        List<String> constantPool = new ArrayList<>();
        for (Action a : list) {
            if (a instanceof ActionConstantPool) {
                constantPool.addAll(((ActionConstantPool) a).constantPool);
            }
        }

        lexer = new FlasmLexer(new StringReader(source));
        List<Label> labels = new ArrayList<>();
        Map<Action, Integer> lineMap = new HashMap<>();
        ActionList ret = parse(ignoreNops, labels, lineMap, address, lexer, constantPool, version, charset);
        //Action.setActionsAddresses(ret, address, version);
        for (Action link : ret) {
            if (!(link instanceof ActionIf || link instanceof ActionJump)) {
                continue;
            }

            boolean found = false;
            String identifier = null;
            if (link instanceof ActionJump) {
                identifier = ((ActionJump) link).identifier;

                for (Label label : labels) {
                    ActionJump actionJump = (ActionJump) link;

                    if (actionJump.identifier.equals(label.name)) {
                        int offset = (int) (label.address - (actionJump.getAddress() + actionJump.getTotalActionLength()));
                        if (offset < -0x8000 || offset > 0x7fff) {
                            String message = "Jump offset is too large:" + offset + " addr: ofs" + Helper.formatAddress(link.getAddress());
                            if (throwOnError) {
                                Integer line = lineMap.get(link);
                                if (line == null) {
                                    line = -1;
                                }

                                throw new ActionParseException(message, line);
                            } else {
                                logger.log(Level.SEVERE, message);
                            }
                        }

                        actionJump.setJumpOffset(offset);
                        found = true;
                        break;
                    }
                }
            } else if (link instanceof ActionIf) {
                ActionIf actionIf = (ActionIf) link;
                identifier = actionIf.identifier;

                for (Label label : labels) {
                    if (actionIf.identifier.equals(label.name)) {
                        int offset = (int) (label.address - (actionIf.getAddress() + actionIf.getTotalActionLength()));
                        if (offset < -0x8000 || offset > 0x7fff) {
                            String message = "If offset is too large:" + offset + " addr: ofs" + Helper.formatAddress(link.getAddress());
                            if (throwOnError) {
                                Integer line = lineMap.get(link);
                                if (line == null) {
                                    line = -1;
                                }

                                throw new ActionParseException(message, line);
                            } else {
                                logger.log(Level.SEVERE, message);
                            }
                        }

                        actionIf.setJumpOffset(offset);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                String message = "TARGET NOT FOUND - identifier:" + identifier + " addr: ofs" + Helper.formatAddress(link.getAddress());
                if (throwOnError) {
                    Integer line = lineMap.get(link);
                    if (line == null) {
                        line = -1;
                    }

                    throw new ActionParseException(message, line);
                } else {
                    logger.log(Level.SEVERE, message);
                }
            }
        }

        if (ret.size() == 0 || !(ret.get(ret.size() - 1) instanceof ActionEnd)) {
            ret.add(new ActionEnd(charset));
        }

        return ret;
    }
}
