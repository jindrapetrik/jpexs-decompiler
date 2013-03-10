/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.action.parser;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.flashlite.ActionFSCommand2;
import com.jpexs.decompiler.flash.action.flashlite.ActionStrictMode;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.swf3.*;
import com.jpexs.decompiler.flash.action.swf4.*;
import com.jpexs.decompiler.flash.action.swf5.*;
import com.jpexs.decompiler.flash.action.swf6.*;
import com.jpexs.decompiler.flash.action.swf7.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ASMParser {

   public static List<Action> parse(boolean ignoreNops, List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
      List<Action> list = new ArrayList<Action>();
      while (true) {
         ParsedSymbol symb = lexer.yylex();
         if (symb.type == ParsedSymbol.TYPE_LABEL) {
            labels.add(new Label((String) symb.value, address));
         } else if (symb.type == ParsedSymbol.TYPE_COMMENT) {
            if (!list.isEmpty()) {
               String cmt = (String) symb.value;
               if (cmt.equals("compileTime")) {
                  Action a = list.get(list.size() - 1);
                  if (a instanceof ActionIf) {
                     ((ActionIf) a).compileTime = true;
                  }
               }
            }
         } else if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
            String instructionName = ((String) symb.value).toLowerCase();
            if (instructionName.equals("GetURL".toLowerCase())) {
               list.add(new ActionGetURL(lexer));
            } else if (instructionName.equals("GoToLabel".toLowerCase())) {
               list.add(new ActionGoToLabel(lexer));
            } else if (instructionName.equals("GotoFrame".toLowerCase())) {
               list.add(new ActionGotoFrame(lexer));
            } else if (instructionName.equals("NextFrame".toLowerCase())) {
               list.add(new ActionNextFrame());
            } else if (instructionName.equals("Play".toLowerCase())) {
               list.add(new ActionPlay());
            } else if (instructionName.equals("PrevFrame".toLowerCase())) {
               list.add(new ActionPrevFrame());
            } else if (instructionName.equals("SetTarget".toLowerCase())) {
               list.add(new ActionSetTarget(lexer));
            } else if (instructionName.equals("Stop".toLowerCase())) {
               list.add(new ActionStop());
            } else if (instructionName.equals("StopSounds".toLowerCase())) {
               list.add(new ActionStopSounds());
            } else if (instructionName.equals("ToggleQuality".toLowerCase())) {
               list.add(new ActionToggleQuality());
            } else if (instructionName.equals("WaitForFrame".toLowerCase())) {
               list.add(new ActionWaitForFrame(lexer));
            } else if (instructionName.equals("Add".toLowerCase())) {
               list.add(new ActionAdd());
            } else if (instructionName.equals("And".toLowerCase())) {
               list.add(new ActionAnd());
            } else if (instructionName.equals("AsciiToChar".toLowerCase())) {
               list.add(new ActionAsciiToChar());
            } else if (instructionName.equals("Call".toLowerCase())) {
               list.add(new ActionCall());
            } else if (instructionName.equals("CharToAscii".toLowerCase())) {
               list.add(new ActionCharToAscii());
            } else if (instructionName.equals("CloneSprite".toLowerCase())) {
               list.add(new ActionCloneSprite());
            } else if (instructionName.equals("Divide".toLowerCase())) {
               list.add(new ActionDivide());
            } else if (instructionName.equals("EndDrag".toLowerCase())) {
               list.add(new ActionEndDrag());
            } else if (instructionName.equals("Equals".toLowerCase())) {
               list.add(new ActionEquals());
            } else if (instructionName.equals("GetProperty".toLowerCase())) {
               list.add(new ActionGetProperty());
            } else if (instructionName.equals("GetTime".toLowerCase())) {
               list.add(new ActionGetTime());
            } else if (instructionName.equals("GetURL2".toLowerCase())) {
               list.add(new ActionGetURL2(lexer));
            } else if (instructionName.equals("GetVariable".toLowerCase())) {
               list.add(new ActionGetVariable());
            } else if (instructionName.equals("GotoFrame2".toLowerCase())) {
               list.add(new ActionGotoFrame2(lexer));
            } else if (instructionName.equals("If".toLowerCase())) {
               list.add(new ActionIf(lexer));
            } else if (instructionName.equals("Jump".toLowerCase())) {
               list.add(new ActionJump(lexer));
            } else if (instructionName.equals("Less".toLowerCase())) {
               list.add(new ActionLess());
            } else if (instructionName.equals("MBAsciiToChar".toLowerCase())) {
               list.add(new ActionMBAsciiToChar());
            } else if (instructionName.equals("MBCharToAscii".toLowerCase())) {
               list.add(new ActionMBCharToAscii());
            } else if (instructionName.equals("MBStringExtract".toLowerCase())) {
               list.add(new ActionMBStringExtract());
            } else if (instructionName.equals("MBStringLength".toLowerCase())) {
               list.add(new ActionMBStringLength());
            } else if (instructionName.equals("Multiply".toLowerCase())) {
               list.add(new ActionMultiply());
            } else if (instructionName.equals("Not".toLowerCase())) {
               list.add(new ActionNot());
            } else if (instructionName.equals("Or".toLowerCase())) {
               list.add(new ActionOr());
            } else if (instructionName.equals("Pop".toLowerCase())) {
               list.add(new ActionPop());
            } else if (instructionName.equals("Push".toLowerCase())) {
               list.add(new ActionPush(lexer, constantPool));
            } else if (instructionName.equals("RandomNumber".toLowerCase())) {
               list.add(new ActionRandomNumber());
            } else if (instructionName.equals("RemoveSprite".toLowerCase())) {
               list.add(new ActionRemoveSprite());
            } else if (instructionName.equals("SetProperty".toLowerCase())) {
               list.add(new ActionSetProperty());
            } else if (instructionName.equals("SetTarget2".toLowerCase())) {
               list.add(new ActionSetTarget2());
            } else if (instructionName.equals("SetVariable".toLowerCase())) {
               list.add(new ActionSetVariable());
            } else if (instructionName.equals("StartDrag".toLowerCase())) {
               list.add(new ActionStartDrag());
            } else if (instructionName.equals("StringAdd".toLowerCase())) {
               list.add(new ActionStringAdd());
            } else if (instructionName.equals("StringEquals".toLowerCase())) {
               list.add(new ActionStringEquals());
            } else if (instructionName.equals("StringExtract".toLowerCase())) {
               list.add(new ActionStringExtract());
            } else if (instructionName.equals("StringLength".toLowerCase())) {
               list.add(new ActionStringLength());
            } else if (instructionName.equals("StringLess".toLowerCase())) {
               list.add(new ActionStringLess());
            } else if (instructionName.equals("Subtract".toLowerCase())) {
               list.add(new ActionSubtract());
            } else if (instructionName.equals("ToInteger".toLowerCase())) {
               list.add(new ActionToInteger());
            } else if (instructionName.equals("Trace".toLowerCase())) {
               list.add(new ActionTrace());
            } else if (instructionName.equals("WaitForFrame2".toLowerCase())) {
               list.add(new ActionWaitForFrame2(lexer));
            } else if (instructionName.equals("Add2".toLowerCase())) {
               list.add(new ActionAdd2());
            } else if (instructionName.equals("BitAnd".toLowerCase())) {
               list.add(new ActionBitAnd());
            } else if (instructionName.equals("BitLShift".toLowerCase())) {
               list.add(new ActionBitLShift());
            } else if (instructionName.equals("BitOr".toLowerCase())) {
               list.add(new ActionBitOr());
            } else if (instructionName.equals("BitRShift".toLowerCase())) {
               list.add(new ActionBitRShift());
            } else if (instructionName.equals("BitURShift".toLowerCase())) {
               list.add(new ActionBitURShift());
            } else if (instructionName.equals("BitXor".toLowerCase())) {
               list.add(new ActionBitXor());
            } else if (instructionName.equals("CallFunction".toLowerCase())) {
               list.add(new ActionCallFunction());
            } else if (instructionName.equals("CallMethod".toLowerCase())) {
               list.add(new ActionCallMethod());
            } else if (instructionName.equals("ConstantPool".toLowerCase())) {
               ActionConstantPool acp = new ActionConstantPool(lexer);
               constantPool = acp.constantPool;
               list.add(acp);
            } else if (instructionName.equals("Decrement".toLowerCase())) {
               list.add(new ActionDecrement());
            } else if (instructionName.equals("DefineFunction".toLowerCase())) {
               list.add(new ActionDefineFunction(ignoreNops, labels, address, lexer, constantPool, version));
            } else if (instructionName.equals("DefineLocal".toLowerCase())) {
               list.add(new ActionDefineLocal());
            } else if (instructionName.equals("DefineLocal2".toLowerCase())) {
               list.add(new ActionDefineLocal2());
            } else if (instructionName.equals("Delete".toLowerCase())) {
               list.add(new ActionDelete());
            } else if (instructionName.equals("Delete2".toLowerCase())) {
               list.add(new ActionDelete2());
            } else if (instructionName.equals("Enumerate".toLowerCase())) {
               list.add(new ActionEnumerate());
            } else if (instructionName.equals("Equals2".toLowerCase())) {
               list.add(new ActionEquals2());
            } else if (instructionName.equals("GetMember".toLowerCase())) {
               list.add(new ActionGetMember());
            } else if (instructionName.equals("Increment".toLowerCase())) {
               list.add(new ActionIncrement());
            } else if (instructionName.equals("InitArray".toLowerCase())) {
               list.add(new ActionInitArray());
            } else if (instructionName.equals("InitObject".toLowerCase())) {
               list.add(new ActionInitObject());
            } else if (instructionName.equals("Less2".toLowerCase())) {
               list.add(new ActionLess2());
            } else if (instructionName.equals("Modulo".toLowerCase())) {
               list.add(new ActionModulo());
            } else if (instructionName.equals("NewMethod".toLowerCase())) {
               list.add(new ActionNewMethod());
            } else if (instructionName.equals("NewObject".toLowerCase())) {
               list.add(new ActionNewObject());
            } else if (instructionName.equals("PushDuplicate".toLowerCase())) {
               list.add(new ActionPushDuplicate());
            } else if (instructionName.equals("Return".toLowerCase())) {
               list.add(new ActionReturn());
            } else if (instructionName.equals("SetMember".toLowerCase())) {
               list.add(new ActionSetMember());
            } else if (instructionName.equals("StackSwap".toLowerCase())) {
               list.add(new ActionStackSwap());
            } else if (instructionName.equals("StoreRegister".toLowerCase())) {
               list.add(new ActionStoreRegister(lexer));
            } else if (instructionName.equals("TargetPath".toLowerCase())) {
               list.add(new ActionTargetPath());
            } else if (instructionName.equals("ToNumber".toLowerCase())) {
               list.add(new ActionToNumber());
            } else if (instructionName.equals("ToString".toLowerCase())) {
               list.add(new ActionToString());
            } else if (instructionName.equals("TypeOf".toLowerCase())) {
               list.add(new ActionTypeOf());
            } else if (instructionName.equals("With".toLowerCase())) {
               list.add(new ActionWith(ignoreNops, labels, address, lexer, constantPool, version));
            } else if (instructionName.equals("Enumerate2".toLowerCase())) {
               list.add(new ActionEnumerate2());
            } else if (instructionName.equals("Greater".toLowerCase())) {
               list.add(new ActionGreater());
            } else if (instructionName.equals("InstanceOf".toLowerCase())) {
               list.add(new ActionInstanceOf());
            } else if (instructionName.equals("StrictEquals".toLowerCase())) {
               list.add(new ActionStrictEquals());
            } else if (instructionName.equals("StringGreater".toLowerCase())) {
               list.add(new ActionStringGreater());
            } else if (instructionName.equals("CastOp".toLowerCase())) {
               list.add(new ActionCastOp());
            } else if (instructionName.equals("DefineFunction2".toLowerCase())) {
               list.add(new ActionDefineFunction2(ignoreNops, labels, address, lexer, constantPool, version));
            } else if (instructionName.equals("Extends".toLowerCase())) {
               list.add(new ActionExtends());
            } else if (instructionName.equals("ImplementsOp".toLowerCase())) {
               list.add(new ActionImplementsOp());
            } else if (instructionName.equals("Throw".toLowerCase())) {
               list.add(new ActionThrow());
            } else if (instructionName.equals("Try".toLowerCase())) {
               list.add(new ActionTry(ignoreNops, labels, address, lexer, constantPool, version));
            } else if (instructionName.equals("FSCommand2".toLowerCase())) {
               list.add(new ActionFSCommand2());
            } else if (instructionName.equals("StrictMode".toLowerCase())) {
               list.add(new ActionStrictMode(lexer));
            } else if (instructionName.equals("Nop".toLowerCase())) {
               if (!ignoreNops) {
                  list.add(new ActionNop());
               }
            } else {
               throw new ParseException("Unknown instruction name :" + instructionName, lexer.yyline());
            }
            if (instructionName.equals("Nop".toLowerCase())) {
               if (!ignoreNops) {
                  address += 1;
               }
            } else {
               address += (list.get(list.size() - 1)).getBytes(version).length;
            }
         } else if (symb.type == ParsedSymbol.TYPE_EOL) {
         } else if ((symb.type == ParsedSymbol.TYPE_BLOCK_END) || (symb.type == ParsedSymbol.TYPE_EOF)) {
            return list;
         } else {
            throw new ParseException("Label or Instruction name expected, found:" + symb.type, lexer.yyline());
         }
      }
   }

   public static List<Action> parse(boolean ignoreNops, InputStream is, int version) throws IOException, ParseException {
      FlasmLexer lexer = new FlasmLexer(is);
      List<Label> labels = new ArrayList<Label>();
      List<Action> ret = parse(ignoreNops, labels, 0, lexer, new ArrayList<String>(), version);
      List<Action> links = Action.getActionsAllIfsOrJumps(ret);
      Action.setActionsAddresses(ret, 0, version);
      for (Action link : links) {
         boolean found = false;
         String identifier = null;
         if (link instanceof ActionJump) {
            identifier = ((ActionJump) link).identifier;
            for (Label label : labels) {
               if (((ActionJump) link).identifier.equals(label.name)) {
                  ((ActionJump) link).offset = (int) (label.address - (((ActionJump) link).getAddress() + ((ActionJump) link).getBytes(version).length));
                  found = true;
                  break;
               }
            }
         }
         if (link instanceof ActionIf) {
            identifier = ((ActionIf) link).identifier;
            for (Label label : labels) {
               if (((ActionIf) link).identifier.equals(label.name)) {
                  ((ActionIf) link).offset = (int) (label.address - (((ActionIf) link).getAddress() + ((ActionIf) link).getBytes(version).length));
                  found = true;
                  break;
               }
            }
         }
         if ((link instanceof ActionJump) || (link instanceof ActionIf)) {
            if (!found) {
               //System.err.println("TARGET NOT FOUND - identifier:" + identifier+" addr: ofs"+Helper.formatAddress(link.getAddress()));
            }
         }
      }
      return ret;
   }
}
