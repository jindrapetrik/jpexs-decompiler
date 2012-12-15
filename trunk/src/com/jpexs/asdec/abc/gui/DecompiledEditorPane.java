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

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.ConvertException;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import com.jpexs.asdec.helpers.Highlighting;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class DecompiledEditorPane extends LineMarkedEditorPane implements MouseListener,CaretListener {

   private List<Highlighting> highlights = new ArrayList<Highlighting>();
   private List<Highlighting> traitHighlights = new ArrayList<Highlighting>();
   private List<Highlighting> methodHighlights = new ArrayList<Highlighting>();
   private ABC abc;
   private int classIndex;
   public int lastTraitIndex = 0;

   public void setNoTrait()
   {
      Main.abcMainFrame.detailPanel.showCard(DetailPanel.UNSUPPORTED_TRAIT_CARD);
   }

   private boolean displayMethod(int pos,int methodIndex)
   {
      int bi = abc.findBodyIndex(methodIndex);
      if (bi == -1) {
         return false;
      }
      Main.abcMainFrame.detailPanel.showCard(DetailPanel.METHOD_TRAIT_CARD);            
      if (Main.abcMainFrame.detailPanel.methodTraitPanel.methodCodePanel.sourceTextArea.bodyIndex != bi) {
         Main.abcMainFrame.detailPanel.methodTraitPanel.methodCodePanel.sourceTextArea.setBodyIndex(bi, abc);
         Main.abcMainFrame.detailPanel.methodTraitPanel.methodBodyParamsPanel.loadFromBody(abc.bodies[bi]);
         Main.abcMainFrame.detailPanel.methodTraitPanel.methodInfoPanel.load(abc.bodies[bi].method_info, abc);
      }
      boolean success=false;
      for (Highlighting h : highlights) {
         if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
            try {
               Main.abcMainFrame.detailPanel.methodTraitPanel.methodCodePanel.sourceTextArea.selectInstruction(abc.bodies[bi].code.adr2pos(h.offset));

            } catch (ConvertException ex) {
            }
            success=true;
            //return true;
         }
      }
      return success;
   }
   
   public void caretUpdate(CaretEvent e) {
      getCaret().setVisible(true);
      int pos = getCaretPosition();
      for (Highlighting tm : methodHighlights) {
         if ((pos >= tm.startPos) && (pos < tm.startPos + tm.len)) {
            displayMethod(pos,(int)tm.offset);
            return;
         }
      }
      for (Highlighting th : traitHighlights) {
         if ((pos >= th.startPos) && (pos < th.startPos + th.len)) {
            lastTraitIndex = (int) th.offset;
            Trait tr=abc.findTraitByTraitId(classIndex, (int) th.offset);
            if(tr!=null){
               if(tr instanceof TraitSlotConst){
                  Main.abcMainFrame.detailPanel.slotConstTraitPanel.load((TraitSlotConst)tr, abc);
                  Main.abcMainFrame.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD);
                  return;
               }
            }
            displayMethod(pos,abc.findMethodIdByTraitId(classIndex, (int) th.offset));
            return;
         }         
      }
      setNoTrait();
   }

   private class BufferedClass {

      public String text;
      public List<Highlighting> highlights;
      public List<Highlighting> traitHighlights;
      public List<Highlighting> methodHighlights;

      public BufferedClass(String text, List<Highlighting> highlights, List<Highlighting> traitHighlights,List<Highlighting> methodHighlights) {
         this.text = text;
         this.highlights = highlights;
         this.traitHighlights = traitHighlights;
         this.methodHighlights = methodHighlights;
      }
   }
   private HashMap<Integer, BufferedClass> bufferedClasses = new HashMap<Integer, BufferedClass>();

   public void gotoLastTrait() {
      gotoTrait(lastTraitIndex);
   }

   public void gotoTrait(int traitId) {
      if(traitId==-1)
      {
         setCaretPosition(0);
         return;
      }
      for (Highlighting th : traitHighlights) {
         if (th.offset == traitId) {
            try{
               setCaretPosition(th.startPos + th.len - 1);
               setCaretPosition(th.startPos);
            }catch(IllegalArgumentException iae){

            }
            return;
         }
      }
      setCaretPosition(0);
   }

   public DecompiledEditorPane() {
      addMouseListener(this);
      setEditable(false);
      getCaret().setVisible(true);
      addCaretListener(this);
   }

   public void setClassIndex(int index, ABC abc) {
      setText("//Please wait...");

      String hilightedCode = "";
      if (!bufferedClasses.containsKey(index)) {
         hilightedCode = abc.classToString(index, true, false);
         highlights = Highlighting.getInstrHighlights(hilightedCode);
         traitHighlights = Highlighting.getTraitHighlights(hilightedCode);
         methodHighlights = Highlighting.getMethodHighlights(hilightedCode);
         hilightedCode = Highlighting.stripHilights(hilightedCode);
         bufferedClasses.put(index, new BufferedClass(hilightedCode, highlights, traitHighlights,methodHighlights));
      } else {
         BufferedClass bc = bufferedClasses.get(index);
         hilightedCode = bc.text;
         highlights = bc.highlights;
         traitHighlights = bc.traitHighlights;
         methodHighlights=bc.methodHighlights;
      }
      setText(hilightedCode);
      this.abc = abc;
      classIndex = index;
   }

   public void reloadClass() {
      if (bufferedClasses.containsKey(classIndex)) {
         bufferedClasses.remove(classIndex);
      }
      setClassIndex(classIndex, abc);
      setNoTrait();
   }

   public void setABC(ABC abc) {
      this.abc = abc;
      bufferedClasses.clear();
      setText("");
   }

   public void mouseClicked(MouseEvent e) {
   }

   public void mousePressed(MouseEvent e) {
      

   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }
}
