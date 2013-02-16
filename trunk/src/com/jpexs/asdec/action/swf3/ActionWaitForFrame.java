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
import com.jpexs.asdec.action.treemodel.TreeItem;
import com.jpexs.asdec.action.treemodel.WaitForFrameTreeItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class ActionWaitForFrame extends Action {

   public int frame;
   public int skipCount;

   public ActionWaitForFrame(SWFInputStream sis) throws IOException {
      super(0x8A, 3);
      frame = sis.readUI16();
      skipCount = sis.readUI8();
   }

   @Override
   public String toString() {
      return "WaitForFrame " + frame + " " + skipCount;
   }

   @Override
   public byte[] getBytes(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SWFOutputStream sos = new SWFOutputStream(baos, version);
      try {
         sos.writeUI16(frame);
         sos.writeUI8(skipCount);
         sos.close();
      } catch (IOException e) {
      }
      return surroundWithAction(baos.toByteArray(), version);
   }

   public ActionWaitForFrame(FlasmLexer lexer) throws IOException, ParseException {
      super(0x8A, 0);
      frame = (int) lexLong(lexer);
      skipCount = (int) lexLong(lexer);
   }

   @Override
   public void translate(Stack<TreeItem> stack, List<TreeItem> output, java.util.HashMap<Integer, String> regNames) {
      output.add(new WaitForFrameTreeItem(this, frame, skipCount));
   }
}
