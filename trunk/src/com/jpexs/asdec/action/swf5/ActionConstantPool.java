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
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.parser.ParsedSymbol;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ActionConstantPool extends Action {

   public List<String> constantPool = new ArrayList<String>();

   public ActionConstantPool(int actionLength, SWFInputStream sis, int version) throws IOException {
      super(0x88, actionLength);
      sis = new SWFInputStream(new ByteArrayInputStream(sis.readBytes(actionLength)), version);
      int count = sis.readUI16();
      for (int i = 0; i < count; i++) {
         constantPool.add(sis.readString());
      }
   }

   public ActionConstantPool(FlasmLexer lexer) throws IOException, ParseException {
      super(0x88, 0);
      while (true) {
         ParsedSymbol symb = lexer.yylex();
         if (symb.type == ParsedSymbol.TYPE_STRING) {
            constantPool.add((String) symb.value);
         } else {
            lexer.yypushback(lexer.yylength());
            break;
         }
      }
   }

   @Override
   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUI16(constantPool.size());
         for (String s : constantPool) {
            sos.writeString(s);
         }
         sos.close();
      } catch (IOException e) {
      }
      return surroundWithAction(baos.toByteArray(), version);
   }

   @Override
   public String toString() {
      String ret = "";
      for (int i = 0; i < constantPool.size(); i++) {
         if (i > 0) {
            ret += " ";
         }
         ret += "\"" + Helper.escapeString(constantPool.get(i)) + "\"";
      }
      return "ConstantPool " + ret;
   }

   @Override
   public void translate(Stack<TreeItem> stack, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      
   }
}
