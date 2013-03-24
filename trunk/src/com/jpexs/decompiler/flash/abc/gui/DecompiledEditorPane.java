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
package com.jpexs.decompiler.flash.abc.gui;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class DecompiledEditorPane extends LineMarkedEditorPane implements CaretListener {

   private List<Highlighting> highlights = new ArrayList<Highlighting>();
   private List<Highlighting> traitHighlights = new ArrayList<Highlighting>();
   private List<Highlighting> methodHighlights = new ArrayList<Highlighting>();
   private List<Highlighting> classHighlights = new ArrayList<Highlighting>();
   private Highlighting currentMethodHighlight;
   private ABC abc;
   private ScriptInfo script;
   public int lastTraitIndex = 0;
   private boolean ignoreCarret = false;
   private boolean reset = false;
   private ABCPanel abcPanel;

   public void setNoTrait() {
      abcPanel.detailPanel.showCard(DetailPanel.UNSUPPORTED_TRAIT_CARD);
   }

   public void hilightOffset(long offset) {
      if (currentMethodHighlight == null) {
         return;
      }
      for (Highlighting h2 : highlights) {
         if ((h2.startPos >= currentMethodHighlight.startPos) && (h2.startPos + h2.len <= currentMethodHighlight.startPos + currentMethodHighlight.len)) {
            if (h2.offset == offset) {
               ignoreCarret = true;
               try {
                  setCaretPosition(h2.startPos);
               } catch (IllegalArgumentException ie) {
               }
               getCaret().setVisible(true);
               ignoreCarret = false;
               break;
            }
         }
      }
   }

   public void setClassIndex(int classIndex) {
      this.classIndex = classIndex;
   }

   private boolean displayMethod(int pos, int methodIndex, String name) {
      if (abc == null) {
         return false;
      }
      int bi = abc.findBodyIndex(methodIndex);
      if (bi == -1) {
         return false;
      }
      abcPanel.detailPanel.showCard(DetailPanel.METHOD_TRAIT_CARD);
      if (reset || (abcPanel.detailPanel.methodTraitPanel.methodCodePanel.getBodyIndex() != bi)) {
         abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setBodyIndex(bi, abc, name);
         abcPanel.detailPanel.methodTraitPanel.methodBodyParamsPanel.loadFromBody(abc.bodies[bi]);
         abcPanel.detailPanel.methodTraitPanel.methodInfoPanel.load(abc.bodies[bi].method_info, abc);
         abcPanel.detailPanel.setEditMode(false);
      }
      boolean success = false;
      for (Highlighting h : highlights) {
         if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
            abcPanel.detailPanel.methodTraitPanel.methodCodePanel.hilighOffset(h.offset);
            success = true;
            //return true;
         }
      }
      return success;
   }

   public void displayClass(int classIndex) {
      if (abcPanel.navigator.getClassIndex() != classIndex) {
         abcPanel.navigator.setClassIndex(classIndex);
      }
   }
   private int classIndex = -1;

   public void resetEditing() {
      reset = true;
      caretUpdate(null);
      reset = false;
   }

   @Override
   public void caretUpdate(CaretEvent e) {
      if (abc == null) {
         return;
      }
      if (ignoreCarret) {
         return;
      }

      getCaret().setVisible(true);
      int pos = getCaretPosition();
      abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(true);
      try {
         classIndex = -1;
         for (Highlighting cm : classHighlights) {
            if ((pos >= cm.startPos) && (pos < cm.startPos + cm.len)) {
               classIndex = (int) cm.offset;
               displayClass(classIndex);
               break;
            }
         }

         for (Highlighting tm : methodHighlights) {
            if ((pos >= tm.startPos) && (pos < tm.startPos + tm.len)) {
               String name = "";
               if (abc != null) {
                  name = abc.instance_info[classIndex].getName(abc.constants).getNameWithNamespace(abc.constants);
               }
               for (Highlighting th : traitHighlights) {
                  if ((pos >= th.startPos) && (pos < th.startPos + th.len)) {
                     lastTraitIndex = (int) th.offset;
                     if (abc != null) {
                        Trait t = abc.findTraitByTraitId(classIndex, lastTraitIndex);
                        if (t != null) {
                           name += ":" + t.getName(abc).getName(abc.constants, new ArrayList<String>());
                        }
                     }
                  }
               }

               displayMethod(pos, (int) tm.offset, name);
               currentMethodHighlight = tm;


               return;
            }
         }

         if (classIndex == -1) {
            setNoTrait();
            return;
         }
         for (Highlighting th : traitHighlights) {
            if ((pos >= th.startPos) && (pos < th.startPos + th.len)) {
               lastTraitIndex = (int) th.offset;
               Trait tr = abc.findTraitByTraitId(classIndex, (int) th.offset);
               if (tr != null) {
                  if (tr instanceof TraitSlotConst) {
                     abcPanel.detailPanel.slotConstTraitPanel.load((TraitSlotConst) tr, abc);
                     abcPanel.detailPanel.showCard(DetailPanel.SLOT_CONST_TRAIT_CARD);
                     abcPanel.detailPanel.setEditMode(false);
                     return;
                  }
               }
               currentMethodHighlight = th;
               String name = "";
               if (abc != null) {
                  name = abc.instance_info[classIndex].getName(abc.constants).getNameWithNamespace(abc.constants);
                  Trait t = abc.findTraitByTraitId(classIndex, lastTraitIndex);
                  if (t != null) {
                     name += ":" + t.getName(abc).getName(abc.constants, new ArrayList<String>());
                  }
               }

               displayMethod(pos, abc.findMethodIdByTraitId(classIndex, (int) th.offset), name);
               return;
            }
         }
         setNoTrait();
      } finally {
         abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setIgnoreCarret(false);
      }
   }

   private class BufferedClass {

      public String text;
      public List<Highlighting> highlights;
      public List<Highlighting> traitHighlights;
      public List<Highlighting> methodHighlights;
      public List<Highlighting> classHighlights;

      public BufferedClass(String text, List<Highlighting> highlights, List<Highlighting> traitHighlights, List<Highlighting> methodHighlights, List<Highlighting> classHighlights) {
         this.text = text;
         this.highlights = highlights;
         this.traitHighlights = traitHighlights;
         this.methodHighlights = methodHighlights;
         this.classHighlights = classHighlights;
      }
   }
   private HashMap<ScriptInfo, BufferedClass> bufferedClasses = new HashMap<ScriptInfo, BufferedClass>();

   public void gotoLastTrait() {
      gotoTrait(lastTraitIndex);
   }

   public void gotoTrait(int traitId) {
      if (traitId == -1) {
         setCaretPosition(0);
         return;
      }

      for (Highlighting tc : classHighlights) {
         if (tc.offset == classIndex) {
            for (Highlighting th : traitHighlights) {
               if ((th.startPos > tc.startPos) && (th.startPos + th.len < tc.startPos + tc.len)) {
                  if (th.offset == traitId) {
                     try {
                        ignoreCarret = true;
                        setCaretPosition(th.startPos + th.len - 1);
                        ignoreCarret = false;
                     } catch (IllegalArgumentException iae) {
                     }
                     final int pos = th.startPos;
                     new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                           try {
                              setCaretPosition(pos);
                           } catch (IllegalArgumentException iae) {
                           }
                        }
                     }, 100);
                     return;
                  }
               }
            }
            break;
         }
      }



      setCaretPosition(0);
   }

   public DecompiledEditorPane(ABCPanel abcPanel) {
      setEditable(false);
      getCaret().setVisible(true);
      addCaretListener(this);
      this.abcPanel = abcPanel;
   }
   private List<DoABCTag> abcList;

   public void clearScriptCache() {
      bufferedClasses.clear();
   }

   public void setScript(ScriptInfo script, ABC abc, List<DoABCTag> abcList) {
      if (script == null) {
         highlights = new ArrayList<Highlighting>();
         traitHighlights = new ArrayList<Highlighting>();
         methodHighlights = new ArrayList<Highlighting>();
         this.script = null;
         return;
      }
      setText("//Please wait...");

      String hilightedCode;
      if (!bufferedClasses.containsKey(script)) {
         hilightedCode = script.convert(abcList, abc, false, true);
         highlights = Highlighting.getInstrHighlights(hilightedCode);
         traitHighlights = Highlighting.getTraitHighlights(hilightedCode);
         methodHighlights = Highlighting.getMethodHighlights(hilightedCode);
         classHighlights = Highlighting.getClassHighlights(hilightedCode);
         hilightedCode = Highlighting.stripHilights(hilightedCode);
         bufferedClasses.put(script, new BufferedClass(hilightedCode, highlights, traitHighlights, methodHighlights, classHighlights));
      } else {
         BufferedClass bc = bufferedClasses.get(script);
         hilightedCode = bc.text;
         highlights = bc.highlights;
         traitHighlights = bc.traitHighlights;
         methodHighlights = bc.methodHighlights;
         classHighlights = bc.classHighlights;
      }
      this.abc = abc;
      this.abcList = abcList;
      this.script = script;
      setText(hilightedCode);
   }

   public void reloadClass() {
      int ci = classIndex;
      if (bufferedClasses.containsKey(script)) {
         bufferedClasses.remove(script);
      }
      setScript(script, abc, abcList);
      setNoTrait();
      setClassIndex(ci);
   }

   public int getClassIndex() {
      return classIndex;
   }

   public void setABC(ABC abc) {
      this.abc = abc;
      bufferedClasses.clear();
      setText("");
   }
}
