/*
 *  Copyright (C) 2010-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui.action;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.CachedScript;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.SearchListener;
import com.jpexs.decompiler.flash.gui.SearchPanel;
import com.jpexs.decompiler.flash.gui.SearchResultsDialog;
import com.jpexs.decompiler.flash.gui.TagEditorPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.controls.NoneSelectedButtonGroup;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.tree.TreePath;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;
import jsyntaxpane.actions.ActionUtils;

public class ActionPanel extends JPanel implements SearchListener<ActionSearchResult>, TagEditorPanel {

    private MainPanel mainPanel;

    public LineMarkedEditorPane editor;

    public LineMarkedEditorPane decompiledEditor;

    public JPersistentSplitPane splitPane;

    public JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton editButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));

    public JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    public JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));

    public JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));

    public JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    public JToggleButton hexButton;

    public JToggleButton hexOnlyButton;

    public JToggleButton constantsViewButton;

    public JToggleButton resolveConstantsButton;

    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));

    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));

    public List<Highlighting> decompiledHilights = new ArrayList<>();

    public List<Highlighting> disassembledHilights = new ArrayList<>();

    private boolean ignoreCarret = false;

    private boolean editMode = false;

    private boolean editDecompiledMode = false;

    private ActionList lastCode;

    private ASMSource src;

    public JPanel topButtonsPan;

    private HighlightedText srcWithHex;

    private HighlightedText srcNoHex;

    private HighlightedText srcHexOnly;

    private HighlightedText srcConstants;

    private String lastDecompiled = "";

    private ASMSource lastASM;

    public SearchPanel<ActionSearchResult> searchPanel;

    private CancellableWorker setSourceWorker;

    public void clearSource() {
        lastCode = null;
        lastASM = null;
        lastDecompiled = null;
        searchPanel.clear();
        src = null;
        srcWithHex = null;
        srcNoHex = null;
        srcHexOnly = null;
        srcConstants = null;
    }

    public String getStringUnderCursor() {
        int pos = decompiledEditor.getCaretPosition();

        SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(decompiledEditor);
        if (sDoc != null) {
            Token t = sDoc.getTokenAt(pos + 1);
            String ident = null;
            //It should be identifier or obfuscated identifier
            if (t != null && (t.type == TokenType.IDENTIFIER || t.type == TokenType.REGEX)) {
                CharSequence tData = t.getText(sDoc);
                ident = tData.toString();
                //We need to get unescaped identifier, so we use our Lexer
                ActionScriptLexer lex = new ActionScriptLexer(new StringReader(ident));
                try {
                    ParsedSymbol symb = lex.lex();
                    if (symb.type == SymbolType.IDENTIFIER) {
                        ident = (String) symb.value;
                    } else {
                        ident = null;
                    }
                } catch (IOException | ActionParseException ex) {
                    ident = null;
                }
            }
            if (ident == null) {
                Highlighting h = Highlighting.searchPos(decompiledHilights, pos);
                if (h != null) {
                    List<Action> list = lastCode;
                    Action lastIns = null;
                    int inspos = 0;
                    Action selIns = null;
                    for (Action ins : list) {
                        if (h.getProperties().offset == ins.getOffset()) {
                            selIns = ins;
                            break;
                        }
                        if (ins.getOffset() > h.getProperties().offset) {
                            inspos = (int) (h.getProperties().offset - lastIns.getAddress());
                            selIns = lastIns;
                            break;
                        }
                        lastIns = ins;
                    }
                    if (selIns != null) {
                        if (selIns instanceof ActionPush) {
                            ActionPush ap = (ActionPush) selIns;
                            Object var = ap.values.get(inspos - 1);
                            String identifier = null;
                            if (var instanceof String) {
                                identifier = (String) var;
                            }
                            if (var instanceof ConstantIndex) {
                                identifier = ap.constantPool.get(((ConstantIndex) var).index);
                            }
                            return identifier;
                        }
                    }

                }
            } else {
                return ident;
            }
        }
        return null;
    }

    public boolean search(final String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.isEmpty())) {
            searchPanel.setOptions(ignoreCase, regexp);
            SWF swf = mainPanel.getCurrentSwf();
            Map<String, ASMSource> asms = swf.getASMs(false);
            final List<ActionSearchResult> found = new ArrayList<>();
            Pattern pat;
            if (regexp) {
                pat = Pattern.compile(txt, ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
            } else {
                pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE) : 0);
            }
            int pos = 0;
            for (Entry<String, ASMSource> item : asms.entrySet()) {
                pos++;
                String workText = AppStrings.translate("work.searching");
                String decAdd = "";
                ASMSource asm = item.getValue();
                if (!SWF.isCached(asm)) {
                    decAdd = ", " + AppStrings.translate("work.decompiling");
                }
                Main.startWork(workText + " \"" + txt + "\"" + decAdd + " - (" + pos + "/" + asms.size() + ") " + item.getKey() + "... ");
                try {
                    if (pat.matcher(SWF.getCached(asm, null).text).find()) {
                        found.add(new ActionSearchResult(asm, item.getKey()));
                    }
                } catch (InterruptedException ex) {
                    break;
                }
            }

            Main.stopWork();
            searchPanel.setSearchText(txt);
            View.execInEventDispatch(() -> {
                SearchResultsDialog<ActionSearchResult> sr = new SearchResultsDialog<>(ActionPanel.this.mainPanel.getMainFrame().getWindow(), txt, ActionPanel.this);
                sr.setResults(found);
                sr.setVisible(true);
            });
            return true;
            //return searchPanel.setResults(found);
        }
        return false;
    }

    private void setDecompiledText(final String text) {
        View.execInEventDispatch(() -> {
            ignoreCarret = true;
            decompiledEditor.setText(text);
            ignoreCarret = false;
        });
    }

    private void setEditorText(final String text, final String contentType) {
        View.execInEventDispatch(() -> {
            ignoreCarret = true;
            editor.changeContentType(contentType);
            editor.setText(text);
            ignoreCarret = false;
        });
    }

    private void setText(final HighlightedText text, final String contentType) {
        View.execInEventDispatch(() -> {
            int pos = editor.getCaretPosition();
            Highlighting lastH = null;
            for (Highlighting h : disassembledHilights) {
                if (pos < h.startPos) {
                    break;
                }
                lastH = h;
            }
            Long offset = lastH == null ? 0 : lastH.getProperties().offset;
            disassembledHilights = text.instructionHilights;
            String stripped = text.text;
            setEditorText(stripped, contentType);
            Highlighting h = Highlighting.searchOffset(disassembledHilights, offset);
            if (h != null) {
                if (h.startPos <= editor.getDocument().getLength()) {
                    editor.setCaretPosition(h.startPos);
                }
            }
        });
    }

    private HighlightedText getHighlightedText(ScriptExportMode exportMode) {
        ASMSource asm = (ASMSource) src;
        DisassemblyListener listener = getDisassemblyListener();
        asm.addDisassemblyListener(listener);
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        try {
            asm.getASMSource(exportMode, writer, lastCode);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        asm.removeDisassemblyListener(listener);
        return new HighlightedText(writer);
    }

    public void setHex(ScriptExportMode exportMode) {
        switch (exportMode) {
            case PCODE:
                if (srcNoHex == null) {
                    srcNoHex = getHighlightedText(exportMode);
                }

                setText(srcNoHex, "text/flasm");
                break;
            case PCODE_HEX:
                if (srcWithHex == null) {
                    srcWithHex = getHighlightedText(exportMode);
                }

                setText(srcWithHex, "text/flasm");
                break;
            case HEX:
                if (srcHexOnly == null) {
                    HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                    Helper.byteArrayToHexWithHeader(writer, src.getActionBytes());
                    srcHexOnly = new HighlightedText(writer);
                }

                setText(srcHexOnly, "text/plain");
                break;
            case CONSTANTS:
                if (srcConstants == null) {
                    srcConstants = getHighlightedText(exportMode);
                }

                setText(srcConstants, "text/plain");
                break;
            default:
                throw new Error("Export mode not supported: " + exportMode);
        }
    }

    private DisassemblyListener getDisassemblyListener() {
        DisassemblyListener listener = new DisassemblyListener() {
            int percent = 0;

            String phase = "";

            private void progress(String phase, long pos, long total) {
                if (total < 1) {
                    return;
                }
                int newpercent = (int) (pos * 100 / total);
                if (((newpercent > percent) || (!this.phase.equals(phase))) && newpercent <= 100) {
                    percent = newpercent;
                    this.phase = phase;
                    // todo: honfika: it is very slow to show every percent
                    setEditorText("; " + AppStrings.translate("work.disassembling") + " - " + phase + " " + percent + "%...", "text/flasm");
                }
            }

            @Override
            public void progressReading(long pos, long total) {
                progress(AppStrings.translate("disassemblingProgress.reading"), pos, total);
            }

            @Override
            public void progressToString(long pos, long total) {
                progress(AppStrings.translate("disassemblingProgress.toString"), pos, total);
            }

            @Override
            public void progressDeobfuscating(long pos, long total) {
                progress(AppStrings.translate("disassemblingProgress.deobfuscating"), pos, total);
            }
        };
        return listener;
    }

    public void setSource(final ASMSource src, final boolean useCache) {
        if (setSourceWorker != null) {
            setSourceWorker.cancel(true);
            setSourceWorker = null;
        }

        this.src = src;
        final ASMSource asm = (ASMSource) src;

        CancellableWorker worker = new CancellableWorker() {

            @Override
            protected Void doInBackground() throws Exception {
                setEditorText("; " + AppStrings.translate("work.disassembling") + "...", "text/flasm");
                if (Configuration.decompile.get()) {
                    setDecompiledText("// " + AppStrings.translate("work.waitingfordissasembly") + "...");
                } else {
                    lastDecompiled = Helper.getDecompilationSkippedComment();
                    setDecompiledText(lastDecompiled);
                }
                DisassemblyListener listener = getDisassemblyListener();
                asm.addDisassemblyListener(listener);
                ActionList actions = asm.getActions();
                lastCode = actions;
                asm.removeDisassemblyListener(listener);
                srcWithHex = null;
                srcNoHex = null;
                srcHexOnly = null;
                srcConstants = null;
                setHex(getExportMode());
                if (Configuration.decompile.get()) {
                    setDecompiledText("// " + AppStrings.translate("work.decompiling") + "...");
                    if (!useCache) {
                        SWF.uncache(asm);
                    }

                    CachedScript sc = SWF.getCached(asm, actions);
                    decompiledHilights = sc.hilights;
                    lastDecompiled = sc.text;
                    lastASM = asm;
                    setDecompiledText(lastDecompiled);
                }
                setEditMode(false);
                setDecompiledEditMode(false);
                return null;
            }

            @Override
            protected void done() {
                View.execInEventDispatch(() -> {
                    setSourceWorker = null;
                    Main.stopWork();

                    try {
                        get();
                    } catch (CancellationException ex) {
                        setEditorText("; " + AppStrings.translate("work.canceled"), "text/flasm");
                    } catch (Exception ex) {
                        setDecompiledText("// " + AppStrings.translate("decompilationError") + ": " + ex);
                    }
                });
            }
        };
        worker.execute();
        setSourceWorker = worker;
        Main.startWork(AppStrings.translate("work.decompiling") + "...", worker);
    }

    public void hilightOffset(long offset) {
    }

    public ActionPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        editor = new LineMarkedEditorPane();
        editor.setEditable(false);
        decompiledEditor = new LineMarkedEditorPane();
        decompiledEditor.setEditable(false);

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.addActionListener(this::graphButtonActionPerformed);
        graphButton.setToolTipText(AppStrings.translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hexas16"));
        hexButton.addActionListener(this::hexButtonActionPerformed);
        hexButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        hexOnlyButton = new JToggleButton(View.getIcon("hex16"));
        hexOnlyButton.addActionListener(this::hexOnlyButtonActionPerformed);
        hexOnlyButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexOnlyButton.setMargin(new Insets(3, 3, 3, 3));

        // todo: change icon
        constantsViewButton = new JToggleButton(View.getIcon("constantpool16"));
        constantsViewButton.addActionListener(this::constantsViewButtonActionPerformed);
        constantsViewButton.setToolTipText(AppStrings.translate("button.viewConstants"));
        constantsViewButton.setMargin(new Insets(3, 3, 3, 3));

        NoneSelectedButtonGroup exportModeButtonGroup = new NoneSelectedButtonGroup();
        exportModeButtonGroup.add(hexButton);
        exportModeButtonGroup.add(hexOnlyButton);
        exportModeButtonGroup.add(constantsViewButton);

        resolveConstantsButton = new JToggleButton(View.getIcon("constantpool16"));
        resolveConstantsButton.addActionListener(this::resolveConstantsButtonActionPerformed);
        resolveConstantsButton.setToolTipText(AppStrings.translate("button.resolveConstants"));
        resolveConstantsButton.setMargin(new Insets(3, 3, 3, 3));
        resolveConstantsButton.setSelected(Configuration.resolveConstants.get());

        topButtonsPan = new JPanel();
        topButtonsPan.setLayout(new BoxLayout(topButtonsPan, BoxLayout.X_AXIS));
        topButtonsPan.add(graphButton);
        topButtonsPan.add(hexButton);
        topButtonsPan.add(hexOnlyButton);
        topButtonsPan.add(constantsViewButton);
        topButtonsPan.add(Box.createRigidArea(new Dimension(10, 0)));
        topButtonsPan.add(resolveConstantsButton);

        JPanel panCode = new JPanel(new BorderLayout());
        panCode.add(new JScrollPane(editor), BorderLayout.CENTER);
        panCode.add(topButtonsPan, BorderLayout.NORTH);

        JPanel panB = new JPanel();
        panB.setLayout(new BorderLayout());
        asmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //asmLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
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
        decButtonsPan.add(experimentalLabel);
        decButtonsPan.add(saveDecompiledButton);
        decButtonsPan.add(cancelDecompiledButton);

        editDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        saveDecompiledButton.setMargin(new Insets(3, 3, 3, 10));
        cancelDecompiledButton.setMargin(new Insets(3, 3, 3, 10));

        //buttonsPan.add(saveHexButton);
        //buttonsPan.add(loadHexButton);
        panB.add(buttonsPan, BorderLayout.SOUTH);

        saveButton.addActionListener(this::saveActionButtonActionPerformed);
        editButton.addActionListener(this::editActionButtonActionPerformed);
        cancelButton.addActionListener(this::cancelActionButtonActionPerformed);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        saveDecompiledButton.addActionListener(this::saveDecompiledButtonActionPerformed);
        editDecompiledButton.addActionListener(this::editDecompiledButtonActionPerformed);
        cancelDecompiledButton.addActionListener(this::cancelDecompiledButtonActionPerformed);
        saveDecompiledButton.setVisible(false);
        cancelDecompiledButton.setVisible(false);

        JPanel decPanel = new JPanel(new BorderLayout());
        decPanel.add(new JScrollPane(decompiledEditor), BorderLayout.CENTER);
        //decPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel panA = new JPanel();
        panA.setLayout(new BorderLayout());
        panA.add(decPanel, BorderLayout.CENTER);
        panA.add(decLabel, BorderLayout.NORTH);
        panA.add(decButtonsPan, BorderLayout.SOUTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));

        setLayout(new BorderLayout());
        add(splitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB, Configuration.guiActionSplitPaneDividerLocationPercent), BorderLayout.CENTER);

        editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
        decompiledEditor.setFont(new Font("Monospaced", Font.PLAIN, decompiledEditor.getFont().getSize()));
        decompiledEditor.changeContentType("text/actionscript");

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
                Highlighting lastH = null;
                for (Highlighting h : disassembledHilights) {
                    if (pos < h.startPos) {
                        break;
                    }
                    lastH = h;
                }
                Long ofs = lastH == null ? 0 : lastH.getProperties().offset;
                Highlighting h2 = Highlighting.searchOffset(decompiledHilights, ofs);
                if (h2 != null) {
                    ignoreCarret = true;
                    if (h2.startPos <= decompiledEditor.getDocument().getLength()) {
                        decompiledEditor.setCaretPosition(h2.startPos);
                    }
                    decompiledEditor.getCaret().setVisible(true);
                    ignoreCarret = false;

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
                Highlighting h = Highlighting.searchPos(decompiledHilights, pos);
                if (h != null) {
                    Highlighting h2 = Highlighting.searchOffset(disassembledHilights, h.getProperties().offset);
                    if (h2 != null) {
                        ignoreCarret = true;
                        if (h2.startPos > 0 && h2.startPos < editor.getText().length()) {
                            editor.setCaretPosition(h2.startPos);
                        }
                        editor.getCaret().setVisible(true);
                        ignoreCarret = false;
                    }
                }
            }
        });

        editor.addTextChangedListener(this::editorTextChanged);
        decompiledEditor.addTextChangedListener(this::decompiledEditorTextChanged);
    }

    private void editorTextChanged() {
        setModified(true);
    }

    private void decompiledEditorTextChanged() {
        setDecompiledModified(true);
    }

    private boolean isModified() {
        return saveButton.isVisible() && saveButton.isEnabled();
    }

    private void setModified(boolean value) {
        saveButton.setEnabled(value);
    }

    private boolean isDecompiledModified() {
        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    private void setDecompiledModified(boolean value) {
        saveDecompiledButton.setEnabled(value);
    }

    public void setEditMode(boolean val) {
        if (val) {
            if (hexOnlyButton.isSelected()) {
                setHex(ScriptExportMode.HEX);
            } else if (constantsViewButton.isSelected()) {
                setHex(ScriptExportMode.CONSTANTS);
            } else {
                setHex(ScriptExportMode.PCODE);
            }
        }

        editor.setEditable(val);
        saveButton.setVisible(val);
        saveButton.setEnabled(false);
        editButton.setVisible(!val);
        cancelButton.setVisible(val);

        editor.getCaret().setVisible(true);
        asmLabel.setIcon(val ? View.getIcon("editing16") : null); // this line is not working
        topButtonsPan.setVisible(!val);
        editMode = val;
        editor.requestFocusInWindow();
    }

    public void setDecompiledEditMode(boolean val) {
        if (lastASM == null) {
            return;
        }

        int lastLine = decompiledEditor.getLine();
        int prefLines = lastASM.getPrefixLineCount();
        if (val) {
            String newText = lastASM.removePrefixAndSuffix(lastDecompiled);
            setDecompiledText(newText);
            if (lastLine > -1) {
                if (lastLine - prefLines >= 0) {
                    decompiledEditor.gotoLine(lastLine - prefLines + 1);
                }
            }
        } else {
            String newText = lastDecompiled;
            setDecompiledText(newText);
            if (lastLine > -1) {
                decompiledEditor.gotoLine(lastLine + prefLines + 1);
            }
        }

        decompiledEditor.setEditable(val);
        saveDecompiledButton.setVisible(val);
        saveDecompiledButton.setEnabled(false);
        editDecompiledButton.setVisible(!val);
        experimentalLabel.setVisible(!val);
        cancelDecompiledButton.setVisible(val);

        decompiledEditor.getCaret().setVisible(true);
        decLabel.setIcon(val ? View.getIcon("editing16") : null);
        editDecompiledMode = val;
        decompiledEditor.requestFocusInWindow();
    }

    private void graphButtonActionPerformed(ActionEvent evt) {
        if (lastCode != null) {
            try {
                GraphDialog gf = new GraphDialog(mainPanel.getMainFrame().getWindow(), new ActionGraph(lastCode, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION), "");
                gf.setVisible(true);
            } catch (InterruptedException ex) {
                Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void editActionButtonActionPerformed(ActionEvent evt) {
        setEditMode(true);
    }

    private void hexButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode());
    }

    private void hexOnlyButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode());
    }

    private void constantsViewButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode());
    }

    private void resolveConstantsButtonActionPerformed(ActionEvent evt) {
        boolean resolve = resolveConstantsButton.isSelected();
        Configuration.resolveConstants.set(resolve);

        srcWithHex = null;
        srcNoHex = null;
        // srcHexOnly = null; is not needed since it does not contains the resolved constant names
        setHex(getExportMode());
    }

    private void cancelActionButtonActionPerformed(ActionEvent evt) {
        setEditMode(false);
        setHex(getExportMode());
    }

    private void saveActionButtonActionPerformed(ActionEvent evt) {
        try {
            String text = editor.getText();
            String trimmed = text.trim();
            if (trimmed.startsWith(Helper.hexData)) {
                src.setActionBytes(Helper.getBytesFromHexaText(text));
            } else if (trimmed.startsWith(Helper.constants)) {
                setConstantPools(Helper.getConstantPoolsFromText(text));
            } else {
                src.setActions(ASMParser.parse(0, true, text, src.getSwf().version, false));
            }

            SWF.uncache(src);
            src.setModified();
            mainPanel.refreshTree(src.getSwf());
            setSource(this.src, false);
            View.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            editButton.setVisible(true);
            editor.setEditable(false);
            editMode = false;
        } catch (IOException ex) {
        } catch (ActionParseException ex) {
            editor.gotoLine((int) ex.line);
            editor.markError();
            View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setConstantPools(List<List<String>> constantPools) {
        try {
            ActionList actions = src.getActions();
            int poolIdx = 0;
            for (Action action : actions) {
                if (action instanceof ActionConstantPool) {
                    ActionConstantPool cPool = (ActionConstantPool) action;
                    List<String> constantPool = constantPools.get(poolIdx);
                    cPool.constantPool = constantPool;

                    poolIdx++;
                    if (constantPools.size() <= poolIdx) {
                        break;
                    }
                }
            }

            src.setActions(actions);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void editDecompiledButtonActionPerformed(ActionEvent evt) {
        if (View.showConfirmDialog(null, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS12Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            setDecompiledEditMode(true);
        }
    }

    private void cancelDecompiledButtonActionPerformed(ActionEvent evt) {
        setDecompiledEditMode(false);
    }

    private void saveDecompiledButtonActionPerformed(ActionEvent evt) {
        try {
            ActionScriptParser par = new ActionScriptParser(mainPanel.getCurrentSwf().version);
            src.setActions(par.actionsFromString(decompiledEditor.getText()));
            src.setModified();
            setSource(src, false);

            View.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
            setDecompiledEditMode(false);
        } catch (IOException ex) {
            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, "IOException during action compiling", ex);
        } catch (ActionParseException ex) {
            View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (CompilationException ex) {
            View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private ScriptExportMode getExportMode() {
        ScriptExportMode exportMode = hexOnlyButton.isSelected() ? ScriptExportMode.HEX
                : hexButton.isSelected() ? ScriptExportMode.PCODE_HEX
                        : constantsViewButton.isSelected() ? ScriptExportMode.CONSTANTS
                                : ScriptExportMode.PCODE;
        return exportMode;
    }

    @Override
    public void updateSearchPos(ActionSearchResult item) {
        TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
        TreePath tp = ttm.getTreePath(item.src);
        mainPanel.tagTree.setSelectionPath(tp);
        mainPanel.tagTree.scrollPathToVisible(tp);
        decompiledEditor.setCaretPosition(0);

        View.execInEventDispatchLater(() -> {
            searchPanel.showQuickFindDialog(decompiledEditor);
        });
    }

    @Override
    public boolean tryAutoSave() {
        // todo: implement
        return false;
    }

    @Override
    public boolean isEditing() {
        return (saveButton.isVisible() && saveButton.isEnabled())
                || (saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled());
    }
}
