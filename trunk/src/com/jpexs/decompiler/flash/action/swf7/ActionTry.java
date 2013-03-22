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
package com.jpexs.decompiler.flash.action.swf7;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.*;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActionTry extends Action {

   public boolean catchInRegisterFlag;
   public boolean finallyBlockFlag;
   public boolean catchBlockFlag;
   public String catchName;
   public int catchRegister;
   public List<Action> tryBody;
   public List<Action> catchBody;
   public List<Action> finallyBody;

   public ActionTry(int actionLength, SWFInputStream sis, int version) throws IOException {
      super(0x8F, actionLength);
      sis.readUB(5);
      catchInRegisterFlag = sis.readUB(1) == 1;
      finallyBlockFlag = sis.readUB(1) == 1;
      catchBlockFlag = sis.readUB(1) == 1;
      int trySize = sis.readUI16();
      int catchSize = sis.readUI16();
      int finallySize = sis.readUI16();
      if (catchInRegisterFlag) {
         catchRegister = sis.readUI8();
      } else {
         catchName = sis.readString();
      }
      byte tryBodyBytes[] = sis.readBytes(trySize);
      byte catchBodyBytes[] = sis.readBytes(catchSize);
      byte finallyBodyBytes[] = sis.readBytes(finallySize);
      tryBody = (new SWFInputStream(new ByteArrayInputStream(tryBodyBytes), version)).readActionList();
      catchBody = (new SWFInputStream(new ByteArrayInputStream(catchBodyBytes), version)).readActionList();
      finallyBody = (new SWFInputStream(new ByteArrayInputStream(finallyBodyBytes), version)).readActionList();
   }

   @Override
   public void setAddress(long address, int version) {
      super.setAddress(address, version);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUB(5, 0);
         sos.writeUB(1, catchInRegisterFlag ? 1 : 0);
         sos.writeUB(1, finallyBlockFlag ? 1 : 0);
         sos.writeUB(1, catchBlockFlag ? 1 : 0);
         byte tryBodyBytes[] = Action.actionsToBytes(tryBody, false, version);
         byte catchBodyBytes[] = Action.actionsToBytes(catchBody, false, version);
         byte finallyBodyBytes[] = Action.actionsToBytes(finallyBody, false, version);
         sos.writeUI16(tryBodyBytes.length);
         sos.writeUI16(catchBodyBytes.length);
         sos.writeUI16(finallyBodyBytes.length);
         if (catchInRegisterFlag) {
            sos.writeUI8(catchRegister);
         } else {
            sos.writeString(catchName);
         }
         Action.setActionsAddresses(tryBody, address + baos.toByteArray().length, version);
         sos.write(tryBodyBytes);
         Action.setActionsAddresses(catchBody, address + baos.toByteArray().length, version);
         sos.write(catchBodyBytes);
         Action.setActionsAddresses(finallyBody, address + baos.toByteArray().length, version);
         sos.close();
      } catch (IOException e) {
      }
   }

   @Override
   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUB(5, 0);
         sos.writeUB(1, catchInRegisterFlag ? 1 : 0);
         sos.writeUB(1, finallyBlockFlag ? 1 : 0);
         sos.writeUB(1, catchBlockFlag ? 1 : 0);
         byte tryBodyBytes[] = Action.actionsToBytes(tryBody, false, version);
         byte catchBodyBytes[] = Action.actionsToBytes(catchBody, false, version);
         byte finallyBodyBytes[] = Action.actionsToBytes(finallyBody, false, version);
         sos.writeUI16(tryBodyBytes.length);
         sos.writeUI16(catchBodyBytes.length);
         sos.writeUI16(finallyBodyBytes.length);
         if (catchInRegisterFlag) {
            sos.writeUI8(catchRegister);
         } else {
            sos.writeString(catchName);
         }
         sos.write(tryBodyBytes);
         sos.write(catchBodyBytes);
         sos.write(finallyBodyBytes);
         sos.close();
      } catch (IOException e) {
      }
      return surroundWithAction(baos.toByteArray(), version);
   }

   public ActionTry(boolean ignoreNops, List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
      super(0x8F, 0);
      lexBlockOpen(lexer);
      tryBody = ASMParser.parse(ignoreNops, labels, address + 4 + 6, lexer, constantPool, version);
      ParsedSymbol symb = lexer.yylex();
      catchBlockFlag = false;
      if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
         if (((String) symb.value).toLowerCase().equals("catch")) {
            catchBlockFlag = true;
            ParsedSymbol catchedVal = lexer.yylex();
            if (catchedVal.type == ParsedSymbol.TYPE_REGISTER) {
               catchInRegisterFlag = true;
               catchRegister = ((RegisterNumber) catchedVal.value).number;
            } else if (catchedVal.type == ParsedSymbol.TYPE_STRING) {
               catchInRegisterFlag = false;
               catchName = (String) catchedVal.value;
            } else {
               throw new ParseException("Catched name or register expected", lexer.yyline());
            }
            lexBlockOpen(lexer);
            catchBody = ASMParser.parse(ignoreNops, labels, address + 4 + 6 + Action.actionsToBytes(tryBody, false, version).length, lexer, constantPool, version);
            symb = lexer.yylex();
         }
         if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
            if (((String) symb.value).toLowerCase().equals("finally")) {
               finallyBlockFlag = true;
               lexBlockOpen(lexer);
               finallyBody = ASMParser.parse(ignoreNops, labels, address + 4 + 6 + Action.actionsToBytes(tryBody, false, version).length + Action.actionsToBytes(catchBody, false, version).length, lexer, constantPool, version);
            } else {
               finallyBlockFlag = false;
               lexer.yypushback(lexer.yylength());
            }
         } else {
            finallyBlockFlag = false;
            lexer.yypushback(lexer.yylength());
         }
      } else {
         lexer.yypushback(lexer.yylength());
      }
   }

   @Override
   public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version, boolean hex) {
      String ret = "";
      ret += "Try {";
      ret += Action.actionsToString(tryBody, knownAddreses, constantPool, version, hex);
      ret += "}";
      if (catchBlockFlag) {
         ret += "\r\nCatch ";
         if (catchInRegisterFlag) {
            ret += "register" + catchRegister;
         } else {
            ret += "\"" + Helper.escapeString(catchName) + "\"";
         }
         ret += " {\r\n";
         ret += Action.actionsToString(catchBody, knownAddreses, constantPool, version, hex);
         ret += "}";
      }
      if (finallyBlockFlag) {
         ret += "\r\nFinally {\r\n";
         ret += Action.actionsToString(finallyBody, knownAddreses, constantPool, version, hex);
         ret += "}";
      }
      return ret;
   }

   @Override
   public List<Long> getAllRefs(int version) {
      List<Long> ret = new ArrayList<Long>();
      ret.addAll(Action.getActionsAllRefs(tryBody, version));
      ret.addAll(Action.getActionsAllRefs(catchBody, version));
      ret.addAll(Action.getActionsAllRefs(finallyBody, version));
      return ret;
   }

   @Override
   public List<Action> getAllIfsOrJumps() {
      List<Action> ret = new ArrayList<Action>();
      ret.addAll(Action.getActionsAllIfsOrJumps(tryBody));
      ret.addAll(Action.getActionsAllIfsOrJumps(catchBody));
      ret.addAll(Action.getActionsAllIfsOrJumps(finallyBody));
      return ret;
   }
}
