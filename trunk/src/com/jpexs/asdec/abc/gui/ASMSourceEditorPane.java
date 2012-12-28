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
package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.flowgraph.Graph;
import com.jpexs.asdec.abc.avm2.parser.ASM3Parser;
import com.jpexs.asdec.abc.avm2.parser.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.JOptionPane;

public class ASMSourceEditorPane extends LineMarkedEditorPane {

   public ABC abc;
   public int bodyIndex = -1;

   public ASMSourceEditorPane() {
   }

   public void setBodyIndex(int bodyIndex, ABC abc) {
      this.bodyIndex = bodyIndex;
      this.abc = abc;
      setText(abc.bodies[bodyIndex].code.toASMSource(abc.constants, abc.bodies[bodyIndex]));
   }

   public void graph() {
      Graph gr = new Graph(abc.bodies[bodyIndex].code);
      //(new GraphTreeFrame(gr)).setVisible(true);
      (new GraphFrame(gr, "")).setVisible(true);
   }

   public void exec() {
      HashMap args = new HashMap();
      args.put(0, new Object()); //object "this"
      args.put(1, new Long(466561)); //param1
      Object o = abc.bodies[bodyIndex].code.execute(args, abc.constants);
      JOptionPane.showMessageDialog(this, "Returned object:" + o.toString());
   }

   public boolean save(ConstantPool constants) {
      try {
         AVM2Code acode = ASM3Parser.parse(new ByteArrayInputStream(getText().getBytes()), constants, new DialogMissingSymbolHandler(), abc.bodies[bodyIndex]);
         acode.getBytes(abc.bodies[bodyIndex].codeBytes);
         abc.bodies[bodyIndex].code = acode;
      } catch (IOException ex) {
      } catch (ParseException ex) {
         JOptionPane.showMessageDialog(this, (ex.text + " on line " + ex.line));
         selectLine((int) ex.line);
         return false;
      }
      return true;
   }

   public void verify(ConstantPool constants, ABC abc) {
      try {
         AVM2Code acode = ASM3Parser.parse(new ByteArrayInputStream(getText().getBytes()), constants, new DialogMissingSymbolHandler(), abc.bodies[bodyIndex]);
         //acode.clearSecureSWF(abc.constants, abc.bodies[bodyIndex]);
         setText(acode.toASMSource(constants, abc.bodies[bodyIndex]));


         //Main.mainFrame.decompiledTextArea.setBody(mb, abc);
      } catch (IOException ex) {
      } catch (ParseException ex) {
         JOptionPane.showMessageDialog(this, (ex.text + " on line " + ex.line));
         selectLine((int) ex.line);
         return;
      }
      JOptionPane.showMessageDialog(this, ("Code OK"));
   }

   public void selectInstruction(int pos) {
      String text = getText();
      int lineCnt = 1;
      int lineStart = 0;
      int lineEnd;
      int instrCount = 0;
      int dot = -2;
      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == '\n') {

            lineCnt++;
            lineEnd = i;
            String ins = text.substring(lineStart, lineEnd).trim();
            if (!((i > 0) && (text.charAt(i - 1) == ':'))) {
               if (!ins.startsWith("exception ")) {
                  instrCount++;
               }
            }
            if (instrCount == pos + 1) {
               break;
            }
            lineStart = i + 1;
         }
      }
      if (lineCnt == -1) {
         //lineEnd = text.length() - 1;
      }
      //select(lineStart, lineEnd);
      setCaretPosition(lineStart);
      //requestFocus();
   }

   public void selectLine(int line) {
      String text = getText();
      int lineCnt = 1;
      int lineStart = 0;
      int lineEnd = -1;
      for (int i = 0; i < text.length(); i++) {
         if (text.charAt(i) == '\n') {
            lineCnt++;
            if (lineCnt == line) {
               lineStart = i;
            }
            if (lineCnt == line + 1) {
               lineEnd = i;
            }
         }
      }
      if (lineCnt == -1) {
         lineEnd = text.length() - 1;
      }
      select(lineStart, lineEnd);
      requestFocus();
   }
}
