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
package com.jpexs.asdec.action.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.SWF;
import com.jpexs.asdec.abc.gui.LineMarkedEditorPane;
import com.jpexs.asdec.action.TagNode;
import com.jpexs.asdec.action.parser.ASMParser;
import com.jpexs.asdec.action.parser.ParseException;
import com.jpexs.asdec.helpers.Highlighting;
import com.jpexs.asdec.tags.Tag;
import com.jpexs.asdec.tags.base.ASMSource;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;

public class ActionPanel extends JPanel implements TreeSelectionListener, ActionListener {

   public JTree tagTree;
   public LineMarkedEditorPane editor;
   public LineMarkedEditorPane decompiledEditor;
   public List<Tag> list;
   public JSplitPane splitPane;
   public JSplitPane splitPane2;
   public JButton saveButton = new JButton("Save");
   public JButton editButton = new JButton("Edit");
   public JButton cancelButton = new JButton("Cancel");
   public JButton saveHexButton = new JButton("Save hex");
   public JButton loadHexButton = new JButton("Load hex");
   public JLabel asmLabel = new JLabel("P-code source");
   public JLabel decLabel = new JLabel("ActionScript source");
   public List<Highlighting> decompiledHilights = new ArrayList<Highlighting>();
   public List<Highlighting> disassembledHilights = new ArrayList<Highlighting>();

   private String lastDisasm="";
   private boolean ignoreCarret=false;
   private boolean editMode=false;
   public ActionPanel(List<Tag> list) {
      this.list = list;
      DefaultSyntaxKit.initKit();
      editor = new LineMarkedEditorPane();
      editor.setEditable(false);
      decompiledEditor = new LineMarkedEditorPane();
      decompiledEditor.setEditable(false);
      tagTree = new JTree(new TagTreeModel(list));

      DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
      ClassLoader cldr = this.getClass().getClassLoader();
      java.net.URL imageURL = cldr.getResource("com/jpexs/asdec/gui/graphics/as16.png");
      ImageIcon leafIcon = new ImageIcon(imageURL);
      treeRenderer.setLeafIcon(leafIcon);
      tagTree.setCellRenderer(treeRenderer);

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
      //buttonsPan.add(saveHexButton);
      //buttonsPan.add(loadHexButton);
      panB.add(buttonsPan, BorderLayout.SOUTH);

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
      add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB)), BorderLayout.CENTER);
      splitPane.setResizeWeight(0.5);
      splitPane2.setResizeWeight(0.5);
      editor.setContentType("text/flasm");
      decompiledEditor.setContentType("text/actionscript");
      tagTree.addTreeSelectionListener(this);
      editor.addCaretListener(new CaretListener() {
         @Override
         public void caretUpdate(CaretEvent e) {
            if(ignoreCarret){
               return;
            }
            editor.getCaret().setVisible(true);
            int pos = editor.getCaretPosition();
            Highlighting lastH = new Highlighting(0, 0, 0);
            for (Highlighting h : disassembledHilights) {
               if (pos < h.startPos) {
                  for (Highlighting h2 : decompiledHilights) {
                     if (h2.offset == lastH.offset) {
                        ignoreCarret=true;
                        decompiledEditor.setCaretPosition(h2.startPos);
                        decompiledEditor.getCaret().setVisible(true);
                        ignoreCarret=false;
                        break;
                     }
                  }
                  break;
               }
               lastH = h;
            }
            for (Highlighting h2 : decompiledHilights) {
               if (h2.offset == lastH.offset) {
                  ignoreCarret=true;
                  decompiledEditor.setCaretPosition(h2.startPos);
                  decompiledEditor.getCaret().setVisible(true);
                  ignoreCarret=false;
                  break;
               }
            }
         }
      });
      decompiledEditor.addCaretListener(new CaretListener() {
         @Override
         public void caretUpdate(CaretEvent e) {
            if(ignoreCarret){
               return;
            }
            if(editMode){
               return;
            }
            decompiledEditor.getCaret().setVisible(true);
            int pos = decompiledEditor.getCaretPosition();
            for (Highlighting h : decompiledHilights) {
               if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
                  for (Highlighting h2 : disassembledHilights) {
                     if (h2.offset == h.offset) {
                        ignoreCarret=true;
                        editor.setCaretPosition(h2.startPos);
                        editor.getCaret().setVisible(true);
                        ignoreCarret=false;
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
      splitPane.setDividerLocation(getWidth() / 3);
      splitPane2.setDividerLocation(getWidth() / 3);
   }

   public void valueChanged(TreeSelectionEvent e) {
      if (Main.isWorking()) {
         return;
      }
      Object obj = tagTree.getLastSelectedPathComponent();
      if (obj instanceof TagNode) {
         obj = ((TagNode) obj).tag;
         if (obj instanceof ASMSource) {
            Main.startWork("Decompiling...");
            final ASMSource asm = (ASMSource) obj;
            (new Thread() {
               @Override
               public void run() {
                  lastDisasm = asm.getASMSource(SWF.DEFAULT_VERSION);
                  disassembledHilights = Highlighting.getInstrHighlights(lastDisasm);     
                  lastDisasm=Highlighting.stripHilights(lastDisasm);
                  editor.setText(lastDisasm);
                  if (Main.DO_DECOMPILE) {
                     List<com.jpexs.asdec.action.Action> as = asm.getActions(SWF.DEFAULT_VERSION);
                     com.jpexs.asdec.action.Action.setActionsAddresses(as, 0, SWF.DEFAULT_VERSION);
                     decompiledEditor.setText("//Decompiling...");
                     String s = com.jpexs.asdec.action.Action.actionsToSource(as, SWF.DEFAULT_VERSION);
                     decompiledHilights = Highlighting.getInstrHighlights(s);
                     decompiledEditor.setText(Highlighting.stripHilights(s));
                  }
                  setEditMode(false);
                  Main.stopWork();
               }
            }).start();
         }
      }
   }

   public void display() {
      setVisible(true);
      splitPane.setDividerLocation(0.5);
      splitPane2.setDividerLocation(0.5);
   }

   public List<TagNode> getSelectedNodes() {
      List<TagNode> ret = new ArrayList<TagNode>();
      TreePath tps[] = tagTree.getSelectionPaths();
      if (tps == null) {
         return ret;
      }
      for (TreePath tp : tps) {
         TagNode te = (TagNode) tp.getLastPathComponent();
         ret.add(te);
      }
      return ret;
   }

   public void setEditMode(boolean val){
      if(val){
         editor.setEditable(true);
         saveButton.setVisible(true);
         editButton.setVisible(false);
         cancelButton.setVisible(true);
         editor.getCaret().setVisible(true);
      }else{
         editor.setEditable(false);
         saveButton.setVisible(false);
         editButton.setVisible(true);
         cancelButton.setVisible(false);
         editor.getCaret().setVisible(true);
      }
      editMode=val;
   }
   
   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("EDITACTION")) {
         setEditMode(true);
      }else
      if (e.getActionCommand().equals("CANCELACTION")) {
         setEditMode(false);
         editor.setText(lastDisasm);
      }else
      if (e.getActionCommand().equals("SAVEACTION")) {
         TagNode ti = (TagNode) tagTree.getLastSelectedPathComponent();
         if (ti.tag instanceof ASMSource) {
            ASMSource dat = (ASMSource) ti.tag;
            try {
               dat.setActions(ASMParser.parse(new ByteArrayInputStream(editor.getText().getBytes()), SWF.DEFAULT_VERSION), SWF.DEFAULT_VERSION);
               valueChanged(null);
               JOptionPane.showMessageDialog(this, "Code successfully saved");
            } catch (IOException ex) {
            } catch (ParseException ex) {
               JOptionPane.showMessageDialog(this, "" + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
            }
         }
         saveButton.setVisible(false);
         editButton.setVisible(true);
         editor.setEditable(false);
      }
   }
}
