/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
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
import com.jpexs.decompiler.flash.gui.TagTreeModel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.helpers.HilightedText;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
import jsyntaxpane.DefaultSyntaxKit;

public class ActionPanel extends JPanel implements ActionListener, SearchListener<ActionSearchResult> {

    static final String ACTION_GRAPH = "GRAPH";
    static final String ACTION_HEX = "HEX";
    static final String ACTION_HEX_ONLY = "HEXONLY";
    static final String ACTION_SAVE_ACTION = "SAVEACTION";
    static final String ACTION_EDIT_ACTION = "EDITACTION";
    static final String ACTION_CANCEL_ACTION = "CANCELACTION";
    static final String ACTION_SAVE_DECOMPILED = "SAVEDECOMPILED";
    static final String ACTION_EDIT_DECOMPILED = "EDITDECOMPILED";
    static final String ACTION_CANCEL_DECOMPILED = "CANCELDECOMPILED";

    private MainPanel mainPanel;
    public LineMarkedEditorPane editor;
    public LineMarkedEditorPane decompiledEditor;
    public JSplitPane splitPane;
    public JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));
    public JButton editButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));
    public JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));
    public JLabel experimentalLabel = new JLabel(AppStrings.translate("action.edit.experimental"));
    public JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));
    public JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));
    public JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));
    public JToggleButton hexButton;
    public JToggleButton hexOnlyButton;
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
    private HilightedText srcWithHex;
    private HilightedText srcNoHex;
    private HilightedText srcHexOnly;
    private String lastDecompiled = "";
    private ASMSource lastASM;
    public SearchPanel<ActionSearchResult> searchPanel;
    private Cache<ASMSource, CachedScript> cache = Cache.getInstance(true);
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
    }

    public void clearCache() {
        cache.clear();
    }

    public String getStringUnderCursor() {
        int pos = decompiledEditor.getCaretPosition();
        Highlighting h = Highlighting.search(decompiledHilights, pos);
        if (h != null) {
            List<Action> list = lastCode;
            Action lastIns = null;
            int inspos = 0;
            Action selIns = null;
            for (Action ins : list) {
                if (h.getPropertyLong("offset") == ins.getOffset()) {
                    selIns = ins;
                    break;
                }
                if (ins.getOffset() > h.getPropertyLong("offset")) {
                    inspos = (int) (h.getPropertyLong("offset") - lastIns.getAddress());
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
        return null;
    }

    private CachedScript getCached(ASMSource pack) {
        return (CachedScript) cache.get(pack);
    }

    private boolean isCached(ASMSource src) {
        return cache.contains(src);
    }

    private void cacheScript(ASMSource src, List<Action> actions) throws InterruptedException {
        if (!cache.contains(src)) {
            if (actions == null) {
                actions = src.getActions();
            }
            HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
            Action.actionsToSource(src, actions, src.toString()/*FIXME?*/, writer);
            List<Highlighting> hilights = writer.instructionHilights;
            String srcNoHex = writer.toString();
            cache.put(src, new CachedScript(srcNoHex, hilights));
        }
    }

    public boolean search(final String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.isEmpty())) {
            searchPanel.setOptions(ignoreCase, regexp);
            SWF swf = mainPanel.getCurrentSwf();
            Map<String, ASMSource> asms = swf.getASMs();
            final List<ActionSearchResult> found = new ArrayList<>();
            Pattern pat;
            if (regexp) {
                pat = Pattern.compile(txt, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } else {
                pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            }
            int pos = 0;
            for (Entry<String, ASMSource> item : asms.entrySet()) {
                pos++;
                String workText = AppStrings.translate("work.searching");
                String decAdd = "";
                if (!isCached(item.getValue())) {
                    decAdd = ", " + AppStrings.translate("work.decompiling");
                }
                Main.startWork(workText + " \"" + txt + "\"" + decAdd + " - (" + pos + "/" + asms.size() + ") " + item.getKey() + "... ");
                try {
                    cacheScript(item.getValue(), null);
                } catch (InterruptedException ex) {
                    break;
                }
                if (pat.matcher(getCached(item.getValue()).text).find()) {
                    found.add(new ActionSearchResult(item.getValue(), item.getKey()));
                }
            }

            Main.stopWork();
            searchPanel.setSearchText(txt);
            View.execInEventDispatch(new Runnable() {

                @Override
                public void run() {
                    SearchResultsDialog<ActionSearchResult> sr = new SearchResultsDialog<>(ActionPanel.this.mainPanel.getMainFrame().getWindow(), txt, ActionPanel.this);
                    sr.setResults(found);
                    sr.setVisible(true);
                }
            });
            return true;
            //return searchPanel.setResults(found);
        }
        return false;
    }

    private void setDecompiledText(final String text) {
        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                ignoreCarret = true;
                decompiledEditor.setText(text, "text/actionscript");
                ignoreCarret = false;
            }
        });
    }

    private void setEditorText(final String text, final String contentType) {
        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                ignoreCarret = true;
                editor.setText(text, contentType);
                ignoreCarret = false;
            }
        });
    }

    private void setText(final HilightedText text, final String contentType) {
        View.execInEventDispatch(new Runnable() {

            @Override
            public void run() {
                int pos = editor.getCaretPosition();
                Highlighting lastH = null;
                for (Highlighting h : disassembledHilights) {
                    if (pos < h.startPos) {
                        break;
                    }
                    lastH = h;
                }
                String offset = lastH == null ? "0" : lastH.getPropertyString("offset");
                disassembledHilights = text.instructionHilights;
                String stripped = text.text;
                setEditorText(stripped, contentType);
                Highlighting h = Highlighting.search(disassembledHilights, "offset", offset);
                if (h != null) {
                    if (h.startPos <= editor.getDocument().getLength()) {
                        editor.setCaretPosition(h.startPos);
                    }
                }
            }
        });
    }

    private HilightedText getHilightedText(ScriptExportMode exportMode) {
        ASMSource asm = (ASMSource) src;
        DisassemblyListener listener = getDisassemblyListener();
        asm.addDisassemblyListener(listener);
        HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
        try {
            asm.getASMSource(exportMode, writer, lastCode);
        } catch (InterruptedException ex) {
            Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        asm.removeDisassemblyListener(listener);
        return new HilightedText(writer);
    }

    public void setHex(ScriptExportMode exportMode) {
        if (exportMode != ScriptExportMode.HEX) {
            if (exportMode == ScriptExportMode.PCODE) {
                if (srcNoHex == null) {
                    srcNoHex = getHilightedText(exportMode);
                }
                setText(srcNoHex, "text/flasm");
            } else {
                if (srcWithHex == null) {
                    srcWithHex = getHilightedText(exportMode);
                }
                setText(srcWithHex, "text/flasm");
            }
        } else {
            if (srcHexOnly == null) {
                HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
                Helper.byteArrayToHexWithHeader(writer, src.getActionBytes());
                srcHexOnly = new HilightedText(writer);
            }
            setText(srcHexOnly, "text/plain");
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
                    setDecompiledText("//" + AppStrings.translate("work.waitingfordissasembly") + "...");
                }
                DisassemblyListener listener = getDisassemblyListener();
                asm.addDisassemblyListener(listener);
                ActionList actions = asm.getActions();
                lastCode = actions;
                asm.removeDisassemblyListener(listener);
                srcWithHex = null;
                srcNoHex = null;
                srcHexOnly = null;
                setHex(getExportMode());
                if (Configuration.decompile.get()) {
                    setDecompiledText("//" + AppStrings.translate("work.decompiling") + "...");
                    if (!useCache) {
                        uncache(asm);
                    }
                    cacheScript(asm, actions);
                    CachedScript sc = getCached(asm);
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
                setSourceWorker = null;
                Main.stopWork();

                View.execInEventDispatch(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            get();
                        } catch (CancellationException ex) {
                            setEditorText("; " + AppStrings.translate("work.canceled"), "text/flasm");
                        } catch (Exception ex) {
                            setDecompiledText("//" + AppStrings.translate("decompilationError") + ": " + ex);
                        }
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
        DefaultSyntaxKit.initKit();
        this.mainPanel = mainPanel;
        editor = new LineMarkedEditorPane();
        editor.setEditable(false);
        decompiledEditor = new LineMarkedEditorPane();
        decompiledEditor.setEditable(false);

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.setActionCommand(ACTION_GRAPH);
        graphButton.addActionListener(this);
        graphButton.setToolTipText(AppStrings.translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hexas16"));
        hexButton.setActionCommand(ACTION_HEX);
        hexButton.addActionListener(this);
        hexButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        hexOnlyButton = new JToggleButton(View.getIcon("hex16"));
        hexOnlyButton.setActionCommand(ACTION_HEX_ONLY);
        hexOnlyButton.addActionListener(this);
        hexOnlyButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexOnlyButton.setMargin(new Insets(3, 3, 3, 3));

        topButtonsPan = new JPanel();
        topButtonsPan.setLayout(new BoxLayout(topButtonsPan, BoxLayout.X_AXIS));
        topButtonsPan.add(graphButton);
        topButtonsPan.add(hexButton);
        topButtonsPan.add(hexOnlyButton);
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

        saveButton.addActionListener(this);
        saveButton.setActionCommand(ACTION_SAVE_ACTION);
        editButton.addActionListener(this);
        editButton.setActionCommand(ACTION_EDIT_ACTION);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand(ACTION_CANCEL_ACTION);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);

        saveDecompiledButton.addActionListener(this);
        saveDecompiledButton.setActionCommand(ACTION_SAVE_DECOMPILED);
        editDecompiledButton.addActionListener(this);
        editDecompiledButton.setActionCommand(ACTION_EDIT_DECOMPILED);

        cancelDecompiledButton.addActionListener(this);
        cancelDecompiledButton.setActionCommand(ACTION_CANCEL_DECOMPILED);
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
        add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB), BorderLayout.CENTER);
        splitPane.setResizeWeight(0.5);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                Configuration.guiActionSplitPaneDividerLocation.set((int) pce.getNewValue());
            }
        });
        editor.setFont(new Font("Monospaced", Font.PLAIN, editor.getFont().getSize()));
        decompiledEditor.setFont(new Font("Monospaced", Font.PLAIN, decompiledEditor.getFont().getSize()));

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
                Highlighting lastH = null;
                for (Highlighting h : disassembledHilights) {
                    if (pos < h.startPos) {
                        break;
                    }
                    lastH = h;
                }
                String ofs = lastH == null ? "0" : lastH.getPropertyString("offset");
                Highlighting h2 = Highlighting.search(decompiledHilights, "offset", ofs);
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
                Highlighting h = Highlighting.search(decompiledHilights, pos);
                if (h != null) {
                    Highlighting h2 = Highlighting.search(disassembledHilights, "offset", h.getPropertyString("offset"));
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
    }

    public void initSplits() {
        int split = Configuration.guiActionSplitPaneDividerLocation.get(getWidth() / 2);
        if (split == 0) {
            split = getWidth() / 2;
        }
        splitPane.setDividerLocation(split);
    }

    public void display() {
        setVisible(true);
        splitPane.setDividerLocation(0.5);
    }

    public void setEditMode(boolean val) {
        boolean rawEdit = hexOnlyButton.isSelected();

        if (val) {
            if (rawEdit) {
                setText(srcHexOnly, "text/plain");
            } else {
                setText(srcNoHex, "text/flasm");
            }
            editor.setEditable(true);
            saveButton.setVisible(true);
            editButton.setVisible(false);
            cancelButton.setVisible(true);
            editor.getCaret().setVisible(true);
            asmLabel.setIcon(View.getIcon("editing16"));
        } else {
            setHex(getExportMode());
            editor.setEditable(false);
            saveButton.setVisible(false);
            editButton.setVisible(true);
            cancelButton.setVisible(false);
            editor.getCaret().setVisible(true);
            asmLabel.setIcon(null);
        }
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
            decompiledEditor.setEditable(true);
            saveDecompiledButton.setVisible(true);
            editDecompiledButton.setVisible(false);
            experimentalLabel.setVisible(false);
            cancelDecompiledButton.setVisible(true);
            decompiledEditor.getCaret().setVisible(true);
            decLabel.setIcon(View.getIcon("editing16"));
        } else {
            String newText = lastDecompiled;
            setDecompiledText(newText);
            if (lastLine > -1) {
                decompiledEditor.gotoLine(lastLine + prefLines + 1);
            }
            decompiledEditor.setEditable(false);
            saveDecompiledButton.setVisible(false);
            editDecompiledButton.setVisible(true);
            experimentalLabel.setVisible(true);
            cancelDecompiledButton.setVisible(false);
            decompiledEditor.getCaret().setVisible(true);
            decLabel.setIcon(null);
        }
        editDecompiledMode = val;
        decompiledEditor.requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_GRAPH:
                if (lastCode != null) {
                    try {
                        GraphDialog gf = new GraphDialog(mainPanel.getMainFrame().getWindow(), new ActionGraph(lastCode, new HashMap<Integer, String>(), new HashMap<String, GraphTargetItem>(), new HashMap<String, GraphTargetItem>(), SWF.DEFAULT_VERSION), "");
                        gf.setVisible(true);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case ACTION_EDIT_ACTION:
                setEditMode(true);
                break;
            case ACTION_HEX:
            case ACTION_HEX_ONLY:
                if (e.getActionCommand() == ACTION_HEX) {
                    hexOnlyButton.setSelected(false);
                } else {
                    hexButton.setSelected(false);
                }
                setHex(getExportMode());
                break;
            case ACTION_CANCEL_ACTION:
                setEditMode(false);
                setHex(getExportMode());
                break;
            case ACTION_SAVE_ACTION:
                try {
                    String text = editor.getText();
                    if (text.trim().startsWith("#hexdata")) {
                        src.setActionBytes(Helper.getBytesFromHexaText(text));
                    } else {
                        src.setActions(ASMParser.parse(0, true, text, src.getSwf().version, false));
                    }
                    src.setModified();
                    setSource(this.src, false);
                    View.showMessageDialog(this, AppStrings.translate("message.action.saved"));
                    saveButton.setVisible(false);
                    cancelButton.setVisible(false);
                    editButton.setVisible(true);
                    editor.setEditable(false);
                    editMode = false;
                } catch (IOException ex) {
                } catch (ActionParseException ex) {
                    View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    editor.gotoLine((int) ex.line);
                }
                break;
            case ACTION_EDIT_DECOMPILED:
                if (View.showConfirmDialog(null, AppStrings.translate("message.confirm.experimental.function"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, Configuration.warningExperimentalAS12Edit, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
                    setDecompiledEditMode(true);
                }
                break;
            case ACTION_CANCEL_DECOMPILED:
                setDecompiledEditMode(false);
                break;
            case ACTION_SAVE_DECOMPILED:
                try {
                    ActionScriptParser par = new ActionScriptParser(mainPanel.getCurrentSwf().version);
                    src.setActions(par.actionsFromString(decompiledEditor.getText()));
                    src.setModified();
                    setSource(src, false);

                    View.showMessageDialog(this, AppStrings.translate("message.action.saved"));
                    setDecompiledEditMode(false);
                } catch (IOException ex) {
                    Logger.getLogger(ActionPanel.class.getName()).log(Level.SEVERE, "IOException during action compiling", ex);
                } catch (ActionParseException ex) {
                    View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                } catch (CompilationException ex) {
                    View.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                }
                break;
        }
    }

    private ScriptExportMode getExportMode() {
        ScriptExportMode exportMode = hexOnlyButton.isSelected() ? ScriptExportMode.HEX
                : (hexButton.isSelected() ? ScriptExportMode.PCODE_HEX : ScriptExportMode.PCODE);
        return exportMode;
    }

    @Override
    public void updateSearchPos(ActionSearchResult item) {
        TagTreeModel ttm = (TagTreeModel) mainPanel.tagTree.getModel();
        TreePath tp = ttm.getTagPath(item.src);
        mainPanel.tagTree.setSelectionPath(tp);
        mainPanel.tagTree.scrollPathToVisible(tp);
        decompiledEditor.setCaretPosition(0);

        View.execInEventDispatchLater(new Runnable() {

            @Override
            public void run() {
                searchPanel.showQuickFindDialog(decompiledEditor);
            }
        });
    }

    private void uncache(ASMSource pack) {
        cache.remove(pack);
    }
}
