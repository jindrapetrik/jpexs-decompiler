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
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.gui.GraphFrame;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.Helper;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import jsyntaxpane.DefaultSyntaxKit;

public class ActionPanel extends JPanel implements ActionListener {

    private boolean debugRecompile = false;
    public LineMarkedEditorPane editor;
    public LineMarkedEditorPane decompiledEditor;
    public LineMarkedEditorPane recompiledEditor;
    public List<Tag> list;
    public JSplitPane splitPane;
    public JSplitPane splitPane2;
    public JButton saveButton = new JButton("Save", View.getIcon("save16"));
    public JButton editButton = new JButton("Edit", View.getIcon("edit16"));
    public JButton cancelButton = new JButton("Cancel", View.getIcon("cancel16"));
    public JLabel betaLabel = new JLabel("(Beta)");
    public JButton editDecompiledButton = new JButton("Edit", View.getIcon("edit16"));
    public JButton saveDecompiledButton = new JButton("Save", View.getIcon("save16"));
    public JButton cancelDecompiledButton = new JButton("Cancel", View.getIcon("cancel16"));
    public JToggleButton hexButton;
    public JButton saveHexButton = new JButton("Save hex");
    public JButton loadHexButton = new JButton("Load hex");
    public JLabel asmLabel = new JLabel("P-code source");
    public JLabel decLabel = new JLabel("ActionScript source");
    public List<Highlighting> decompiledHilights = new ArrayList<Highlighting>();
    public List<Highlighting> disassembledHilights = new ArrayList<Highlighting>();
    public String lastDisasm = "";
    private boolean ignoreCarret = false;
    private boolean editMode = false;
    private boolean editDecompiledMode = false;
    private List<com.jpexs.decompiler.flash.action.Action> lastCode;
    private ASMSource src;
    public JPanel topButtonsPan;
    private String srcWithHex;
    private String srcNoHex;

    public void setText(String text) {
        int pos = editor.getCaretPosition();
        Highlighting lastH = new Highlighting(0, 0, 0);
        for (Highlighting h : disassembledHilights) {
            if (pos < h.startPos) {
                break;
            }
            lastH = h;
        }
        long offset = lastH.offset;
        disassembledHilights = Highlighting.getInstrHighlights(text);
        editor.setText(Highlighting.stripHilights(text));
        for (Highlighting h : disassembledHilights) {
            if (h.offset == offset) {
                editor.setCaretPosition(h.startPos);
                break;
            }
        }

    }

    public void setHex(boolean hex) {
        setText(hex ? srcWithHex : srcNoHex);
    }

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
                lastDisasm = asm.getASMSource(SWF.DEFAULT_VERSION, true);
                srcWithHex = Helper.hexToComments(lastDisasm);
                srcNoHex = Helper.stripComments(lastDisasm);
                setHex(hexButton.isSelected());
                if (Main.DO_DECOMPILE) {
                    List<com.jpexs.decompiler.flash.action.Action> as = asm.getActions(SWF.DEFAULT_VERSION);
                    lastCode = as;
                    //com.jpexs.decompiler.flash.action.Action.setActionsAddresses(as, 0, SWF.DEFAULT_VERSION);
                    String s = com.jpexs.decompiler.flash.action.Action.actionsToSource(as, SWF.DEFAULT_VERSION);
                    decompiledHilights = Highlighting.getInstrHighlights(s);
                    String stripped = Highlighting.stripHilights(s);
                    /*try {
                     ActionScriptParser.parse(stripped);
                     } catch (ParseException ex) {
                     JOptionPane.showMessageDialog(null, ex.getMessage());
                     Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
                     } catch (IOException ex) {
                     Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
                     }*/
                    decompiledEditor.setText(stripped);
                    if (debugRecompile) {
                        try {
                            ActionScriptParser ps = new ActionScriptParser();

                            recompiledEditor.setText(Highlighting.stripHilights(com.jpexs.decompiler.flash.action.Action.actionsToString(0, ps.parse(stripped), null, SWF.DEFAULT_VERSION, false, 0)));
                        } catch (ParseException ex) {
                            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                setEditMode(false);
                setDecompiledEditMode(false);
                Main.stopWork();
            }
        }).start();
    }

    public void hilightOffset(long offset) {
    }

    public ActionPanel() {
        this.list = list;
        DefaultSyntaxKit.initKit();
        editor = new LineMarkedEditorPane();
        editor.setEditable(false);
        decompiledEditor = new LineMarkedEditorPane();
        decompiledEditor.setEditable(false);

        recompiledEditor = new LineMarkedEditorPane();
        recompiledEditor.setEditable(false);

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.setActionCommand("GRAPH");
        graphButton.addActionListener(this);
        graphButton.setToolTipText("View Graph");
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hex16"));
        hexButton.setActionCommand("HEX");
        hexButton.addActionListener(this);
        hexButton.setToolTipText("View Hex");
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        topButtonsPan = new JPanel();
        topButtonsPan.setLayout(new BoxLayout(topButtonsPan, BoxLayout.X_AXIS));
        topButtonsPan.add(graphButton);
        topButtonsPan.add(hexButton);
        JPanel panCode = new JPanel(new BorderLayout());
        panCode.add(new JScrollPane(editor), BorderLayout.CENTER);
        panCode.add(topButtonsPan, BorderLayout.NORTH);

        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        asmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        asmLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        panB.add(asmLabel, BorderLayout.NORTH);
        panB.add(panCode, BorderLayout.CENTER);



        JPanel buttonsPan = new JPanel();
        buttonsPan.setLayout(new FlowLayout());
        buttonsPan.add(editButton);
        buttonsPan.add(saveButton);
        buttonsPan.add(cancelButton);

        editButton.setMargin(new Insets(3, 3, 3, 10));
        saveButton.setMargin(new Insets(3, 3, 3, 10));
        cancelButton.setMargin(new Insets(3, 3, 3, 10));


        JPanel decButtonsPan = new JPanel(new FlowLayout());
        decButtonsPan.add(editDecompiledButton);
        decButtonsPan.add(betaLabel);
        decButtonsPan.add(saveDecompiledButton);
        decButtonsPan.add(cancelDecompiledButton);

        editDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        saveDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        cancelDecompiledButton.setMargin(new Insets(3, 3, 3, 10));

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



        saveDecompiledButton.addActionListener(this);
        saveDecompiledButton.setActionCommand("SAVEDECOMPILED");
        editDecompiledButton.addActionListener(this);
        editDecompiledButton.setActionCommand("EDITDECOMPILED");

        cancelDecompiledButton.addActionListener(this);
        cancelDecompiledButton.setActionCommand("CANCELDECOMPILED");
        saveDecompiledButton.setVisible(false);
        cancelDecompiledButton.setVisible(false);

        JPanel panA = new JPanel();
        panA.setLayout(new BorderLayout());
        panA.add(new JScrollPane(decompiledEditor), BorderLayout.CENTER);
        panA.add(decLabel, BorderLayout.NORTH);
        panA.add(decButtonsPan, BorderLayout.SOUTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));


        JPanel panC = new JPanel();
        panC.setLayout(new BorderLayout());
        panC.add(new JScrollPane(recompiledEditor), BorderLayout.CENTER);
        panC.add(new JLabel("Recompiled"), BorderLayout.NORTH);




        setLayout(new BorderLayout());
        //add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tagTree), splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB)), BorderLayout.CENTER);
        add(splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, debugRecompile ? new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panB, panC) : panB), BorderLayout.CENTER);
//      splitPane.setResizeWeight(0.5);
        splitPane2.setResizeWeight(0.5);
        editor.setContentType("text/flasm");
        editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
        decompiledEditor.setContentType("text/actionscript");
        decompiledEditor.setFont(new Font("Monospaced", Font.PLAIN, decompiledEditor.getFont().getSize()));

        if (debugRecompile) {
            recompiledEditor.setContentType("text/flasm");
            recompiledEditor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
        }
        //tagTree.addTreeSelectionListener(this);
        editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCarret) {
                    return;
                }
                if (editMode || editDecompiledMode) {
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
                if (editMode || editDecompiledMode) {
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
            setText(srcNoHex);
            editor.setEditable(true);
            saveButton.setVisible(true);
            editButton.setVisible(false);
            cancelButton.setVisible(true);
            editor.getCaret().setVisible(true);
        } else {
            setText(hexButton.isSelected() ? srcWithHex : srcNoHex);
            editor.setEditable(false);
            saveButton.setVisible(false);
            editButton.setVisible(true);
            cancelButton.setVisible(false);
            editor.getCaret().setVisible(true);
        }
        topButtonsPan.setVisible(!val);
        editMode = val;
    }

    public void setDecompiledEditMode(boolean val) {
        if (val) {
            decompiledEditor.setEditable(true);
            saveDecompiledButton.setVisible(true);
            editDecompiledButton.setVisible(false);
            betaLabel.setVisible(false);
            cancelDecompiledButton.setVisible(true);
            decompiledEditor.getCaret().setVisible(true);
        } else {
            decompiledEditor.setEditable(false);
            saveDecompiledButton.setVisible(false);
            editDecompiledButton.setVisible(true);
            betaLabel.setVisible(true);
            cancelDecompiledButton.setVisible(false);
            decompiledEditor.getCaret().setVisible(true);
        }
        editDecompiledMode = val;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("GRAPH")) {
            if (lastCode != null) {
                GraphFrame gf = new GraphFrame(new ActionGraph(lastCode, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>(), SWF.DEFAULT_VERSION), "");
                gf.setVisible(true);
            }
        } else if (e.getActionCommand().equals("EDITACTION")) {
            setEditMode(true);
        } else if (e.getActionCommand().equals("HEX")) {
            setHex(hexButton.isSelected());
        } else if (e.getActionCommand().equals("CANCELACTION")) {
            setEditMode(false);
            setHex(hexButton.isSelected());
        } else if (e.getActionCommand().equals("SAVEACTION")) {
            try {
                src.setActions(ASMParser.parse(0, src.getPos(), true, new ByteArrayInputStream(editor.getText().getBytes()), SWF.DEFAULT_VERSION), SWF.DEFAULT_VERSION);
                setSource(this.src);
                JOptionPane.showMessageDialog(this, "Code successfully saved");
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                editButton.setVisible(true);
                editor.setEditable(false);
                editMode = false;
            } catch (IOException ex) {
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "" + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getActionCommand().equals("EDITDECOMPILED")) {
            setDecompiledEditMode(true);
        } else if (e.getActionCommand().equals("CANCELDECOMPILED")) {
            setDecompiledEditMode(false);
        } else if (e.getActionCommand().equals("SAVEDECOMPILED")) {
            try {
                ActionScriptParser par = new ActionScriptParser();
                src.setActions(par.parse(decompiledEditor.getText()), SWF.DEFAULT_VERSION);
                setSource(this.src);
                JOptionPane.showMessageDialog(this, "Code successfully saved");
                saveDecompiledButton.setVisible(false);
                cancelDecompiledButton.setVisible(false);
                editDecompiledButton.setVisible(true);
                betaLabel.setVisible(true);
                decompiledEditor.setEditable(false);
                editDecompiledMode = false;
            } catch (IOException ex) {
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "" + ex.text + " on line " + ex.line, "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }
}
