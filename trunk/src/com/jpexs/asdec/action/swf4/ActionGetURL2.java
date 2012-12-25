/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.action.swf4;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetURL2TreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGetURL2 extends Action {

   public int sendVarsMethod;
   public static final int GET = 1;
   public static final int POST = 2;
   public boolean loadTargetFlag;
   public boolean loadVariablesFlag;

   public ActionGetURL2(SWFInputStream sis) throws IOException {
      super(0x9A, 1);
      sendVarsMethod = (int) sis.readUB(2);
      sis.readUB(4); //reserved
      loadTargetFlag = sis.readUB(1) == 1;
      loadVariablesFlag = sis.readUB(1) == 1;
   }

   @Override
   public String toString() {
      return "GetURL2 " + sendVarsMethod + " " + loadTargetFlag + " " + loadVariablesFlag;
   }

   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUB(2, sendVarsMethod);
         sos.writeUB(4, 0);
         sos.writeUB(1, loadTargetFlag ? 1 : 0);
         sos.writeUB(1, loadVariablesFlag ? 1 : 0);
         sos.close();
      } catch (IOException e) {
      }
      return surroundWithAction(baos.toByteArray(), version);
   }

   public ActionGetURL2(FlasmLexer lexer) throws IOException, ParseException {
      super(0x9A, 0);
      sendVarsMethod = (int) lexLong(lexer);
      loadTargetFlag = lexBoolean(lexer);
      loadVariablesFlag = lexBoolean(lexer);
   }

   @Override
   public void translate(Stack<TreeItem> stack, ConstantPool constants, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      TreeItem targetString = stack.pop();
      TreeItem urlString = stack.pop();
      output.add(new GetURL2TreeItem(this, urlString, targetString, sendVarsMethod, loadTargetFlag, loadTargetFlag));
   }
}
