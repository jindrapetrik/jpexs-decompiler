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
package com.jpexs.decompiler.flash.action.gui;

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.gui.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.parser.ASMParser;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.gui.GraphFrame;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import jsyntaxpane.DefaultSyntaxKit;

public class ActionPanel extends JPanel implements ActionListener {

   public LineMarkedEditorPane editor;
   public LineMarkedEditorPane decompiledEditor;
   public List<Tag> list;
   public JSplitPane splitPane;
   public JSplitPane splitPane2;
   public JButton saveButton = new JButton("Save");
   public JButton editButton = new JButton("Edit");
   public JButton cancelButton = new JButton("Cancel");
   public JButton graphButton = new JButton("Graph");
   public JButton saveHexButton = new JButton("Save hex");
   public JButton loadHexButton = new JButton("Load hex");
   public JLabel asmLabel = new JLabel("P-code source");
   public JLabel decLabel = new JLabel("ActionScript source");
   public List<Highlighting> decompiledHilights = new ArrayList<Highlighting>();
   public List<Highlighting> disassembledHilights = new ArrayList<Highlighting>();
   public String lastDisasm = "";
   private boolean ignoreCarret = false;
   private boolean editMode = false;
   private List<com.jpexs.decompiler.flash.action.Action> lastCode;
   private ASMSource src;

   public void setSource(ASMSource src) {
      this.src = src;
      Main.startWork("Decompiling...");
      final ASMSource asm = (ASMSource) src;
      (new Thread() {
         @Override
         public void run() {
            editor.setText("; Disassembling...");
            if (Main.DO_DECOMPILE) {
               decompiledEditor.setText("//Decompiling...");
            }
            lastDisasm = asm.getASMSource(SWF.DEFAULT_VERSION);
            disassembledHilights = Highlighting.getInstrHighlights(lastDisasm);
            lastDisasm = Highlighting.stripHilights(lastDisasm);
            editor.setText(lastDisasm);
            if (Main.DO_DECOMPILE) {
               List<com.jpexs.decompiler.flash.action.Action> as = asm.getActions(SWF.DEFAULT_VERSION);
               lastCode = as;
               com.jpexs.decompiler.flash.action.Action.setActionsAddresses(as, 0, SWF.DEFAULT_VERSION);
               String s = com.jpexs.decompiler.flash.action.Action.actionsToSource(as, SWF.DEFAULT_VERSION);
               decompiledHilights = Highlighting.getInstrHighlights(s);
               decompiledEditor.setText(Highlighting.stripHilights(s));
            }
            setEditMode(false);
            Main.stopWork();
         }
      }).start();
   }

   public ActionPanel() {
      this.list = list;
      DefaultSyntaxKit.initKit();
      editor = new LineMarkedEditorPane();
      editor.setEditable(false);
      decompiledEditor = new LineMarkedEditorPane();
      decompiledEditor.setEditable(false);



      JPanel panB = new JPanel();
      panB.setLayout(new BorderLayout());
      asmLabel.setHorizontalAlignment(SwingConstants.CENTER);
      asmLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
      panB.add(asmLabel, BorderLayout.NORTH);
      panB.add(new JScrollPane(editor), BorderLayout.CENTER);

      JPanel buttonsPan = new JPanel();
      buttonsPan.setLayout(new FlowLayout());
      buttonsPan.add(editButton);
      buttonsPan.add(saveButton);
      buttonsPan.add(cancelButton);
      buttonsPan.add(graphButton);
      //buttonsPan.add(saveHexButton);
      //buttonsPan.add(loadHexButton);
      panB.add(buttonsPan, BorderLayout.SOUTH);

      graphButton.addActionListener(this);
      graphButton.setActionCommand("GRAPH");
      saveHexButton.addActionListener(this);
      saveHexButton.setActionCommand("SAVEHEXACTION");
      loadHexButton.addActionListener(this);
      loadHexButton.setActionCommand("LOADHEXACTION");
      saveButton.addActionListener(this);
      saveButton.setActionCommand("SAVEACTION");
      editButton.addActionListener(this);
      editButton.setActionCommand("EDITACTION");
      cancelButton.addActionListener(this);
      cancelButton.setActionCommand("CANCELACTION");
      saveButton.setVisible(false);
      cancelButton.setVisible(false);


      JPanel panA = new JPanel();
      panA.setLayout(new BorderLayout());
      panA.add(new JScrollPane(decompiledEditor), BorderLayout.CENTER);
      panA.add(decLabel, BorderLayout.NORTH);
      decLabel.setHorizontalAlignment(SwingConstants.CENTER);
      decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));





      setLayout(new BorderLayout());
      //add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB)), BorderLayout.CENTER);
      add(splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB), BorderLayout.CENTER);
//      splitPane.setResizeWeight(0.5);
      splitPane2.setResizeWeight(0.5);
      editor.setContentType("text/flasm");
      editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
      decompiledEditor.setContentType("text/actionscript");
      decompiledEditor.setFont(new Font("Monospaced", Font.PLAIN, decompiledEditor.getFont().getSize()));

      //tagTree.addTreeSelectionListener(this);
      editor.addCaretListener(new CaretListener() {
         @Override
         public void caretUpdate(CaretEvent e) {
            if (ignoreCarret) {
               return;
            }
            editor.getCaret().setVisible(true);
            int pos = editor.getCaretPosition();
            Highlighting lastH = new Highlighting(0, 0, 0);
            for (Highlighting h : disassembledHilights) {
               if (pos < h.startPos) {
                  break;
               }
               lastH = h;
            }
            for (Highlighting h2 : decompiledHilights) {
               if (h2.offset == lastH.offset) {
                  ignoreCarret = true;
                  decompiledEditor.setCaretPosition(h2.startPos);
                  decompiledEditor.getCaret().setVisible(true);
                  ignoreCarret = false;
                  break;
               }
            }
         }
      });
      decompiledEditor.addCaretListener(new CaretListener() {
         @Override
         public void caretUpdate(CaretEvent e) {
            if (ignoreCarret) {
               return;
            }
            if (editMode) {
               return;
            }
            decompiledEditor.getCaret().setVisible(true);
            int pos = decompiledEditor.getCaretPosition();
            for (Highlighting h : decompiledHilights) {
               if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
                  for (Highlighting h2 : disassembledHilights) {
                     if (h2.offset == h.offset) {
                        ignoreCarret = true;
                        editor.setCaretPosition(h2.startPos);
                        editor.getCaret().setVisible(true);
                        ignoreCarret = false;
                        break;
                     }
                  }
                  break;
               }
            }
         }
      });
   }

   public void initSplits() {
      //splitPane.setDividerLocation(getWidth() / 3);
      splitPane2.setDividerLocation(getWidth() / 2);
   }

   public void display() {
      setVisible(true);
      splitPane.setDividerLocation(0.5);
      splitPane2.setDividerLocation(0.5);
   }

   public void setEditMode(boolean val) {
      if (val) {
         editor.setEditable(true);
         saveButton.setVisible(true);
         editButton.setVisible(false);
         cancelButton.setVisible(true);
         editor.getCaret().setVisible(true);
      } else {
         editor.setEditable(false);
         saveButton.setVisible(false);
         editButton.setVisible(true);
         cancelButton.setVisible(false);
         editor.getCaret().setVisible(true);
      }
      editMode = val;
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("GRAPH")) {
         if (lastCode != null) {
            GraphFrame gf = new GraphFrame(new ActionGraph(lastCode, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>(), SWF.DEFAULT_VERSION), "");
            gf.setVisible(true);
         }
      } else if (e.getActionCommand().equals("EDITACTION")) {
         setEditMode(true);
      } else if (e.getActionCommand().equals("CANCELACTION")) {
         setEditMode(false);
         editor.setText(lastDisasm);
      } else if (e.getActionCommand().equals("SAVEACTION")) {
         try {
            src.setActions(ASMParser.parse(true, new ByteArrayInputStream(editor.getText().getBytes()), SWF.DEFAULT_VERSION), SWF.DEFAULT_VERSION);
            JOptionPane.showMessageDialog(this, "Code successfully saved");
         } catch (IOException ex) {
         } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "" + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
         }
         saveButton.setVisible(false);
         editButton.setVisible(true);
         editor.setEditable(false);
      }
   }
}
