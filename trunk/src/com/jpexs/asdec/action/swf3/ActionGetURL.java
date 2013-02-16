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
package com.jpexs.asdec.action.swf3;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.parser.FlasmLexer;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.GetURLTreeItem;
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionGetURL extends Action {

   public String urlString;
   public String targetString;

   public ActionGetURL(int actionLength, SWFInputStream sis, int version) throws IOException {
      super(0x83, actionLength);
      byte data[] = sis.readBytes(actionLength);
      sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      urlString = sis.readString();
      targetString = sis.readString();
   }

   public ActionGetURL(FlasmLexer lexer) throws IOException, ParseException {
      super(0x83, 0);
      urlString = lexString(lexer);
      targetString = lexString(lexer);
   }

   @Override
   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeString(urlString);
         sos.writeString(targetString);
         sos.close();
      } catch (IOException e) {
      }
      return surroundWithAction(baos.toByteArray(), version);
   }

   @Override
   public String toString() {
      return "GetUrl \"" + Helper.escapeString(urlString) + "\" \"" + Helper.escapeString(targetString) + "\"";
   }

   @Override
   public void translate(Stack<TreeItem> stack, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      output.add(new GetURLTreeItem(this, urlString, targetString));
   }   
}
