/*
 *  Copyright (C) 2011 JPEXS
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
import com.jpexs.asdec.abc.methodinfo_parser.MethodInfoParser;
import com.jpexs.asdec.abc.methodinfo_parser.ParseException;
import com.jpexs.asdec.abc.types.ValueKind;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.helpers.Helper;
import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.*;
import jsyntaxpane.syntaxkits.Flasm3MethodInfoSyntaxKit;

/**
 *
 * @author JPEXS
 */
public class SlotConstTraitDetailPanel extends JPanel implements TraitDetail {

   public JEditorPane slotConstEditor;
   private ABC abc;
   private TraitSlotConst trait;

   public SlotConstTraitDetailPanel() {
      slotConstEditor = new JEditorPane();
      setLayout(new BorderLayout());
      add(new JLabel("Type and Value:"), BorderLayout.NORTH);
      add(new JScrollPane(slotConstEditor), BorderLayout.CENTER);
      slotConstEditor.setContentType("text/flasm3_methodinfo");
      Flasm3MethodInfoSyntaxKit sk = (Flasm3MethodInfoSyntaxKit) slotConstEditor.getEditorKit();
      sk.deinstallComponent(slotConstEditor, "jsyntaxpane.components.LineNumbersRuler");
   }

   public void load(TraitSlotConst trait, ABC abc) {
      this.abc = abc;
      this.trait = trait;
      String s;
      String typeStr;
      if (trait.type_index > 0) {
         typeStr = "m[" + trait.type_index + "]\"" + Helper.escapeString(abc.constants.constant_multiname[trait.type_index].toString(abc.constants, new ArrayList<String>())) + "\"";
      } else {
         typeStr = "*";
      }
      String valueStr = "";
      if (trait.value_kind != 0) {
         valueStr = " = " + (new ValueKind(trait.value_index, trait.value_kind)).toString(abc.constants);
      }

      s = typeStr + valueStr;

      slotConstEditor.setText(s);
   }

   public boolean save() {
      try {
         if (!MethodInfoParser.parseSlotConst(slotConstEditor.getText(), trait, abc)) {
            return false;
         }
      } catch (ParseException ex) {
         JOptionPane.showMessageDialog(slotConstEditor, ex.text, "SlotConst typevalue Error", JOptionPane.ERROR_MESSAGE);
         return false;
      }
      return true;
   }
}
