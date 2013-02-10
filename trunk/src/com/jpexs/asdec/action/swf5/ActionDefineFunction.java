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
package com.jpexs.asdec.action.swf5;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.ASMParser;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.Label;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.swf4.ActionPush;
import com.jpexs.asdec.action.swf7.ActionDefineFunction2;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.FunctionTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class ActionDefineFunction extends Action {

   public String functionName;
   public List<String> paramNames = new ArrayList<String>();
   public List<Action> code;
   public int codeSize;
   private int version;

   public void setConstantPool(List<String> constantPool){
      for(Action a:code){
         if(a instanceof ActionPush){
            ((ActionPush)a).constantPool=constantPool;
         }
         if(a instanceof ActionDefineFunction2){
            ((ActionDefineFunction2)a).setConstantPool(constantPool);
         }
         if(a instanceof ActionDefineFunction){
            ((ActionDefineFunction)a).setConstantPool(constantPool);
         }
      }
   }
   public ActionDefineFunction(int actionLength, SWFInputStream sis, int version) throws IOException {
      super(0x9B, actionLength);
      this.version=version;
      //byte data[]=sis.readBytes(actionLength);
      //sis=new SWFInputStream(new ByteArrayInputStream(data),version);
      functionName = sis.readString();
      int numParams = sis.readUI16();
      for (int i = 0; i < numParams; i++) {
         paramNames.add(sis.readString());
      }
      codeSize = sis.readUI16();
      //code = new ArrayList<Action>();
      code = (new SWFInputStream(new ByteArrayInputStream(sis.readBytes(codeSize)), version)).readActionList();
   }

   public ActionDefineFunction(List<Label> labels, long address, FlasmLexer lexer, List<String> constantPool, int version) throws IOException, ParseException {
      super(0x9B, 0);
      functionName = lexString(lexer);
      int numParams = (int) lexLong(lexer);
      for (int i = 0; i < numParams; i++) {
         paramNames.add(lexString(lexer));
      }
      lexBlockOpen(lexer);
      code = ASMParser.parse(labels, address + getPreLen(version), lexer, constantPool, version);
   }

   @Override
   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
      try {
         sos.writeString(functionName);
         sos.writeUI16(paramNames.size());
         for (String s : paramNames) {
            sos.writeString(s);
         }
         byte codeBytes[] = Action.actionsToBytes(code, false, version);
         sos.writeUI16(codeBytes.length);
         sos.close();


         baos2.write(surroundWithAction(baos.toByteArray(), version));
         baos2.write(codeBytes);
      } catch (IOException e) {
      }
      return baos2.toByteArray();
   }

   private long getPreLen(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeString(functionName);
         sos.writeUI16(paramNames.size());
         for (String s : paramNames) {
            sos.writeString(s);
         }
         sos.writeUI16(0);
         sos.close();
      } catch (IOException e) {
      }

      return surroundWithAction(baos.toByteArray(), version).length;
   }

   @Override
   public void setAddress(long address, int version) {
      super.setAddress(address, version);
      Action.setActionsAddresses(code, address + getPreLen(version), version);
   }

   @Override
   public String getASMSource(List<Long> knownAddreses, List<String> constantPool, int version) {
      String paramStr = "";
      for (int i = 0; i < paramNames.size(); i++) {
         paramStr += "\"" + Helper.escapeString(paramNames.get(i)) + "\"";
         paramStr += " ";
      }
      return "DefineFunction \"" + Helper.escapeString(functionName) + "\" " + paramNames.size() + " " + paramStr + " {\r\n" + Action.actionsToString(code, knownAddreses, constantPool, version) + "}";
   }

   @Override
   public List<Long> getAllRefs(int version) {
      return Action.getActionsAllRefs(code, version);
   }

   @Override
   public List<Action> getAllIfsOrJumps() {
      return Action.getActionsAllIfsOrJumps(code);
   }
   
   
   @Override
   public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, HashMap<Integer, String> regNames) {
      stack.push(new FunctionTreeItem(this, functionName, paramNames, Action.actionsToTree(regNames, code,version ), constants));
   }
}
