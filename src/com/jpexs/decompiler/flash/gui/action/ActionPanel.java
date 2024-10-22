/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.ValueTooLargeException;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ConstantPoolTooBigException;
import com.jpexs.decompiler.flash.action.deobfuscation.BrokenScriptDetector;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptLexer;
import com.jpexs.decompiler.flash.action.parser.script.ParsedSymbol;
import com.jpexs.decompiler.flash.action.parser.script.SymbolType;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.ConfigurationItemChangeListener;
import com.jpexs.decompiler.flash.docs.As12PCodeDocs;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DebugPanel;
import com.jpexs.decompiler.flash.gui.DebuggerHandler;
import com.jpexs.decompiler.flash.gui.DocsPanel;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.HeaderLabel;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.ScrollablePanel;
import com.jpexs.decompiler.flash.gui.SearchListener;
import com.jpexs.decompiler.flash.gui.SearchPanel;
import com.jpexs.decompiler.flash.gui.TagEditorPanel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.gui.controls.NoneSelectedButtonGroup;
import com.jpexs.decompiler.flash.gui.editor.DebuggableEditorPane;
import com.jpexs.decompiler.flash.gui.editor.LinkHandler;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTreeModel;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.search.ActionScriptSearch;
import com.jpexs.decompiler.flash.search.ActionSearchResult;
import com.jpexs.decompiler.flash.search.ScriptSearchListener;
import com.jpexs.decompiler.flash.search.ScriptSearchResult;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import javax.swing.tree.TreePath;
import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;
import jsyntaxpane.actions.ActionUtils;

/**
 * @author JPEXS
 */
public class ActionPanel extends JPanel implements SearchListener<ScriptSearchResult>, TagEditorPanel {

    private static final Logger logger = Logger.getLogger(ActionPanel.class.getName());

    private JPanel brokenHintPanel;
    private MainPanel mainPanel;

    public DebuggableEditorPane editor;

    public DebuggableEditorPane decompiledEditor;

    public JPersistentSplitPane splitPane;

    public JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton editButton = new JButton(AppStrings.translate("button.edit.script.disassembled"), View.getIcon("edit16"));

    public JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    public JButton editDecompiledButton = new JButton(AppStrings.translate("button.edit.script.decompiled"), View.getIcon("edit16"));

    public JButton saveDecompiledButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    public JButton cancelDecompiledButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));

    public JToggleButton hexButton;

    public JToggleButton hexOnlyButton;

    public JToggleButton constantsViewButton;

    public JToggleButton resolveConstantsButton;

    public JToggleButton showFileOffsetInPcodeHexButton;

    public JToggleButton showOriginalBytesInPcodeHexButton;

    public JLabel asmLabel = new HeaderLabel(AppStrings.translate("panel.disassembled"));

    public JLabel decLabel = new HeaderLabel(AppStrings.translate("panel.decompiled"));

    private boolean ignoreCaret = false;

    private boolean editMode = false;

    private boolean editDecompiledMode = false;

    private ActionList lastCode;

    private ASMSource src;

    public JPanel topButtonsPan;

    private HighlightedText srcWithHex;

    private HighlightedText srcNoHex;

    private HighlightedText srcHexOnly;

    private HighlightedText srcConstants;

    private HighlightedText disassembledText = HighlightedText.EMPTY;

    private HighlightedText lastDecompiled = HighlightedText.EMPTY;

    private ASMSource lastASM;

    public SearchPanel<ScriptSearchResult> searchPanel;

    private CancellableWorker setSourceWorker;

    private List<Runnable> scriptListeners = new ArrayList<>();

    private boolean scriptLoaded = true;

    public void addScriptListener(Runnable listener) {
        scriptListeners.add(listener);
    }

    public void removeScriptListener(Runnable listener) {
        scriptListeners.remove(listener);
    }

    public void runWhenLoaded(Runnable l) {
        if (scriptLoaded) {
            l.run();
        } else {
            addScriptListener(new Runnable() {
                @Override
                public void run() {
                    l.run();
                    removeScriptListener(this);
                }
            });
        }
    }

    private void fireScript() {
        List<Runnable> list = new ArrayList<>(scriptListeners);
        for (Runnable r : list) {
            r.run();
        }
    }

    public void clearSource() {
        View.checkAccess();

        lastCode = null;
        lastASM = null;
        lastDecompiled = HighlightedText.EMPTY;
        searchPanel.clear();
        src = null;
        srcWithHex = null;
        srcNoHex = null;
        srcHexOnly = null;
        srcConstants = null;
    }

    public String getStringUnderCursor() {
        View.checkAccess();
        int pos = decompiledEditor.getCaretPosition();
        return getStringUnderPosition(pos, decompiledEditor);
    }

    public String getStringUnderPosition(int pos, JTextComponent component) {
        SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(component);
        if (sDoc != null) {
            Token t = sDoc.getTokenAt(pos + 1);
            String ident = null;
            //It should be identifier or obfuscated identifier
            if (t != null && (t.type == TokenType.IDENTIFIER || t.type == TokenType.REGEX)) {
                CharSequence tData = t.getText(sDoc);
                ident = tData.toString();
                //We need to get unescaped identifier, so we use our Lexer
                ActionScriptLexer lex = new ActionScriptLexer(new StringReader(ident));
                if (src != null) {
                    if (src.getSwf().version >= ActionScriptLexer.SWF_VERSION_CASE_SENSITIVE) {
                        lex.setCaseSensitiveIdentifiers(true);
                    }
                }
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
                Highlighting h = Highlighting.searchPos(lastDecompiled.getInstructionHighlights(), pos);
                if (h != null) {
                    List<Action> list = lastCode;
                    Action lastIns = null;
                    int inspos = 0;
                    Action selIns = null;
                    for (Action ins : list) {
                        if (h.getProperties().offset == ins.getAddress()) {
                            selIns = ins;
                            break;
                        }
                        if (ins.getAddress() > h.getProperties().offset && lastIns != null) {
                            inspos = (int) (h.getProperties().offset - lastIns.getAddress());
                            selIns = lastIns;
                            break;
                        }
                        lastIns = ins;
                    }
                    if (selIns != null) {
                        if ((selIns instanceof ActionPush) && (inspos > 0)) {
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

    public List<ActionSearchResult> search(SWF swf, final String txt, boolean ignoreCase, boolean regexp, boolean pcode, CancellableWorker<Void> worker, Map<String, ASMSource> scope) {
        if (txt != null && !txt.isEmpty()) {
            searchPanel.setOptions(ignoreCase, regexp);

            String workText = AppStrings.translate("work.searching");
            String decAdd = AppStrings.translate("work.decompiling");
            return new ActionScriptSearch().searchAs2(swf, txt, ignoreCase, regexp, pcode, new ScriptSearchListener() {
                @Override
                public void onDecompile(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\", " + decAdd + " - (" + pos + "/" + total + ") " + name + "... ", worker);
                }

                @Override
                public void onSearch(int pos, int total, String name) {
                    Main.startWork(workText + " \"" + txt + "\" - (" + pos + "/" + total + ") " + name + "... ", worker);
                }
            }, scope);
        }

        return null;
    }

    private void setDecompiledText(final String scriptName, final String breakPointScriptName, final String text) {
        View.checkAccess();

        ignoreCaret = true;
        decompiledEditor.setScriptName(scriptName, breakPointScriptName);
        decompiledEditor.setText(text);
        BrokenScriptDetector det = new BrokenScriptDetector();
        if (det.codeIsBroken(text)) {
            brokenHintPanel.setVisible(true);
        } else {
            brokenHintPanel.setVisible(false);
        }

        ignoreCaret = false;
    }

    private void setEditorText(final String scriptName, final String breakPointScriptName, final String text, final String contentType) {
        View.checkAccess();

        ignoreCaret = true;
        editor.setScriptName("#PCODE " + scriptName, "#PCODE " + breakPointScriptName);
        editor.changeContentType(contentType);
        editor.setText(text);
        ignoreCaret = false;
    }

    private void setText(final HighlightedText text, final String contentType, final String scriptName, final String breakPointScriptName) {
        View.checkAccess();

        int pos = editor.getCaretPosition();
        Highlighting lastH = null;
        for (Highlighting h : disassembledText.getInstructionHighlights()) {
            if (pos < h.startPos) {
                break;
            }
            lastH = h;
        }
        Long offset = lastH == null ? 0 : lastH.getProperties().offset;
        disassembledText = text;
        setEditorText(scriptName, breakPointScriptName, text.text, contentType);
        Highlighting h = Highlighting.searchOffset(disassembledText.getInstructionHighlights(), offset);
        if (h != null) {
            if (h.startPos <= editor.getDocument().getLength()) {
                editor.setCaretPosition(h.startPos);
            }
        }
    }

    private HighlightedText getHighlightedText(ScriptExportMode exportMode, ActionList actions) {
        if (actions == null) {
            logger.log(Level.WARNING, "Action list is null");
            return HighlightedText.EMPTY;
        }

        ASMSource asm = (ASMSource) src;
        DisassemblyListener listener = getDisassemblyListener();
        asm.addDisassemblyListener(listener);
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        try {
            asm.getASMSource(exportMode, writer, actions);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        asm.removeDisassemblyListener(listener);
        writer.finishHilights();
        return new HighlightedText(writer);
    }

    private void updateHexButtons(ScriptExportMode exportMode) {
        View.checkAccess();

        showFileOffsetInPcodeHexButton.setVisible(exportMode == ScriptExportMode.PCODE_HEX);
        showOriginalBytesInPcodeHexButton.setVisible(exportMode == ScriptExportMode.PCODE_HEX);
        resolveConstantsButton.setVisible(exportMode != ScriptExportMode.CONSTANTS && exportMode != ScriptExportMode.HEX);
    }

    private void setHex(ScriptExportMode exportMode, String scriptName, String breakPointScriptName, ActionList actions) {
        View.checkAccess();
        updateHexButtons(exportMode);

        switch (exportMode) {
            case PCODE:
                if (srcNoHex == null) {
                    srcNoHex = getHighlightedText(exportMode, actions);
                }

                setText(srcNoHex, "text/flasm", scriptName, breakPointScriptName);
                break;
            case PCODE_HEX:
                if (srcWithHex == null) {
                    srcWithHex = getHighlightedText(exportMode, actions);
                }

                setText(srcWithHex, "text/flasm", scriptName, breakPointScriptName);
                break;
            case HEX:
                if (srcHexOnly == null) {
                    HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                    Helper.byteArrayToHexWithHeader(writer, src.getActionBytes().getRangeData());
                    writer.finishHilights();
                    srcHexOnly = new HighlightedText(writer);
                }

                setText(srcHexOnly, "text/plain", scriptName, breakPointScriptName);
                break;
            case CONSTANTS:
                if (srcConstants == null) {
                    srcConstants = getHighlightedText(exportMode, actions);
                }

                setText(srcConstants, "text/plain", scriptName, breakPointScriptName);
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
                    View.execInEventDispatch(() -> {
                        setEditorText("-", "-", "; " + AppStrings.translate("work.disassembling") + " - " + phase + " " + percent + "%...", "text/flasm");
                    });
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
        View.checkAccess();

        scriptLoaded = false;

        if (setSourceWorker != null) {
            setSourceWorker.cancel(true);
            setSourceWorker = null;
        }

        clearSource();
        this.src = src;
        final ASMSource asm = (ASMSource) src;

        if (!useCache) {
            SWF.uncache(asm);
        }

        boolean decompile = Configuration.decompile.get();
        HighlightedText decompiledText;
        if (!decompile) {
            decompiledText = new HighlightedText(Helper.getDecompilationSkippedComment());
        } else {
            decompiledText = SWF.getFromCache(asm);
        }

        HighlightedText fdecompiledText = decompiledText;

        setDecompiledEditMode(false);
        setEditMode(false);

        ActionList actions = SWF.getActionListFromCache(asm);

        boolean disassemblingNeeded = actions == null;
        boolean decompileNeeded = decompiledText == null;

        if (disassemblingNeeded || decompileNeeded) {
            CancellableWorker worker = new CancellableWorker("actionPanel") {
                @Override
                protected Void doInBackground() throws Exception {

                    ActionList innerActions = actions;
                    if (disassemblingNeeded) {
                        View.execInEventDispatch(() -> {
                            editor.setShowMarkers(false);
                            setEditorText(asm.getScriptName(), asm.getExportedScriptName(), "; " + AppStrings.translate("work.disassembling") + "...", "text/flasm");
                            if (decompileNeeded) {
                                decompiledEditor.setShowMarkers(false);
                                setDecompiledText("-", "-", "// " + AppStrings.translate("work.waitingfordissasembly") + "...");
                            }
                        });

                        DisassemblyListener listener = getDisassemblyListener();
                        asm.addDisassemblyListener(listener);
                        innerActions = asm.getActions();
                        asm.removeDisassemblyListener(listener);
                    }

                    if (decompileNeeded) {                        
                        View.execInEventDispatch(() -> {
                            decompiledEditor.setShowMarkers(false);
                            if (src.getSwf().needsCalculatingAS2UninitializeClassTraits(src)) {
                                setEditorText(asm.getScriptName(), asm.getExportedScriptName(), "; ...", "text/flasm");
                                setDecompiledText("-", "-", "// " + AppStrings.translate("work.decompiling.allScripts.ucf") + "...");
                            } else {
                                setDecompiledText("-", "-", "// " + AppStrings.translate("work.decompiling") + "...");
                            }
                        });

                        HighlightedText htext = SWF.getCached(asm, innerActions);
                        ActionList finalActions = innerActions;
                        View.execInEventDispatch(() -> {
                            setSourceCompleted(asm, htext, finalActions);
                        });
                    } else {
                        ActionList finalActions = innerActions;
                        View.execInEventDispatch(() -> {
                            setSourceCompleted(asm, fdecompiledText, finalActions);
                        });
                    }

                    return null;
                }

                @Override
                protected void done() {
                    View.execInEventDispatch(() -> {
                        setSourceWorker = null;
                        if (!Main.isDebugging()) {
                            Main.stopWork();
                        }

                        try {
                            get();
                        } catch (CancellationException ex) {
                            editor.setShowMarkers(false);
                            setEditorText("-", "-", "; " + AppStrings.translate("work.canceled"), "text/flasm");
                            setDecompiledText("-", "-", "// " + AppStrings.translate("work.canceled"));
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, "Error", ex);
                            decompiledEditor.setShowMarkers(false);
                            setDecompiledText("-", "-", "// " + AppStrings.translate("decompilationError") + ": " + ex);
                        }
                    });
                }
            };

            worker.execute();
            setSourceWorker = worker;
            if (!Main.isDebugging()) {
                decompiledEditor.setShowMarkers(false);
                Main.startWork(AppStrings.translate("work.decompiling") + "...", worker);
            }
        } else {
            setSourceCompleted(asm, decompiledText, actions);
        }
    }

    private void setSourceCompleted(ASMSource asm, HighlightedText decompiledText, ActionList actions) {
        View.checkAccess();

        if (decompiledText == null) {
            decompiledText = HighlightedText.EMPTY;
        }

        editor.setShowMarkers(true);
        decompiledEditor.setShowMarkers(Configuration.decompile.get());
        lastASM = asm;
        lastCode = actions;
        lastDecompiled = decompiledText;

        setHex(getExportMode(), asm.getScriptName(), asm.getExportedScriptName(), actions);
        setDecompiledText(asm.getScriptName(), asm.getExportedScriptName(), decompiledText.text);
        scriptLoaded = true;
        fireScript();
    }

    public void hilightOffset(long offset) {
        View.checkAccess();
    }

    public int getLocalDeclarationOfPos(int pos) {
        View.checkAccess();

        Highlighting sh = Highlighting.searchPos(lastDecompiled.getSpecialHighlights(), pos);
        Highlighting h = Highlighting.searchPos(lastDecompiled.getInstructionHighlights(), pos);

        if (h == null) {
            return -1;
        }

        List<Highlighting> tms = Highlighting.searchAllPos(lastDecompiled.getMethodHighlights(), pos);
        if (tms.isEmpty()) {
            return -1;
        }
        for (Highlighting tm : tms) {

            List<Highlighting> tm_tms = Highlighting.searchAllLocalNames(lastDecompiled.getMethodHighlights(), tm.getProperties().localName);
            //is it already declaration?
            if (h.getProperties().declaration || (sh != null && sh.getProperties().declaration)) {
                return -1; //no jump
            }

            String lname = h.getProperties().localName;
            if ("this".equals(lname)) {
                Highlighting ch = Highlighting.searchPos(lastDecompiled.getClassHighlights(), pos);
                //    int cindex = (int) ch.getProperties().index;
                return ch.startPos;
            }

            HighlightData hData = h.getProperties();
            HighlightData search = new HighlightData();
            search.declaration = hData.declaration;
            //search.declaredType = hData.declaredType;
            search.localName = hData.localName;
            //search.specialValue = hData.specialValue;
            if (search.isEmpty()) {
                return -1;
            }
            search.declaration = true;

            for (Highlighting tm1 : tm_tms) {
                Highlighting rh = Highlighting.search(lastDecompiled.getInstructionHighlights(), search, tm1.startPos, tm1.startPos + tm1.len);
                if (rh != null) {
                    return rh.startPos;
                }
            }
        }

        return -1;
    }

    public ActionPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
        editor = new DebuggableEditorPane();
        editor.setEditable(false);
        decompiledEditor = new DebuggableEditorPane();
        decompiledEditor.setEditable(false);
        decompiledEditor.setLinkHandler(new LinkHandler() {
            @Override
            public boolean isLink(Token token) {
                int pos = token.start;
                Highlighting h = Highlighting.searchPos(lastDecompiled.getInstructionHighlights(), pos);
                if (h != null) {
                    if (h.getProperties().localName != null && !h.getProperties().declaration) {
                        return getLocalDeclarationOfPos(pos) != -1;
                    }
                }
                return false;
            }

            @Override
            public void handleLink(Token token) {
                int pos = token.start;
                int tpos = getLocalDeclarationOfPos(pos);
                if (tpos > -1) {
                    //System.err.println("goto " + tpos);
                    decompiledEditor.setCaretPosition(tpos);
                } else {
                    //System.err.println("cannot handle");
                }
            }

            @Override
            public Highlighter.HighlightPainter linkPainter() {
                return decompiledEditor.linkPainter();
            }
        });

        searchPanel = new SearchPanel<>(new FlowLayout(), this);

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.addActionListener(this::graphButtonActionPerformed);
        graphButton.setToolTipText(AppStrings.translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hexas16"));
        hexButton.addActionListener(this::hexButtonActionPerformed);
        hexButton.setToolTipText(AppStrings.translate("button.viewhexpcode"));
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        hexOnlyButton = new JToggleButton(View.getIcon("hex16"));
        hexOnlyButton.addActionListener(this::hexOnlyButtonActionPerformed);
        hexOnlyButton.setToolTipText(AppStrings.translate("button.viewhex"));
        hexOnlyButton.setMargin(new Insets(3, 3, 3, 3));

        constantsViewButton = new JToggleButton(View.getIcon("constantpool16"));
        constantsViewButton.addActionListener(this::constantsViewButtonActionPerformed);
        constantsViewButton.setToolTipText(AppStrings.translate("button.viewConstants"));
        constantsViewButton.setMargin(new Insets(3, 3, 3, 3));

        NoneSelectedButtonGroup exportModeButtonGroup = new NoneSelectedButtonGroup();
        exportModeButtonGroup.add(hexButton);
        exportModeButtonGroup.add(hexOnlyButton);
        exportModeButtonGroup.add(constantsViewButton);

        resolveConstantsButton = new JToggleButton(View.getIcon("resolveconst16"));
        resolveConstantsButton.addActionListener(this::resolveConstantsButtonActionPerformed);
        resolveConstantsButton.setToolTipText(AppStrings.translate("button.resolveConstants"));
        resolveConstantsButton.setMargin(new Insets(3, 3, 3, 3));
        resolveConstantsButton.setSelected(Configuration.resolveConstants.get());

        showFileOffsetInPcodeHexButton = new JToggleButton(View.getIcon("fileoffset16"));
        showFileOffsetInPcodeHexButton.addActionListener(this::showFileOffsetInPcodeHexButtonActionPerformed);
        showFileOffsetInPcodeHexButton.setToolTipText(AppStrings.translate("button.showFileOffsetInPcodeHex"));
        showFileOffsetInPcodeHexButton.setMargin(new Insets(3, 3, 3, 3));
        showFileOffsetInPcodeHexButton.setSelected(Configuration.showFileOffsetInPcodeHex.get());

        showOriginalBytesInPcodeHexButton = new JToggleButton(View.getIcon("originalbytes16"));
        showOriginalBytesInPcodeHexButton.addActionListener(this::showOriginalBytesInPcodeHexButtonActionPerformed);
        showOriginalBytesInPcodeHexButton.setToolTipText(AppStrings.translate("button.showOriginalBytesInPcodeHex"));
        showOriginalBytesInPcodeHexButton.setMargin(new Insets(3, 3, 3, 3));
        showOriginalBytesInPcodeHexButton.setSelected(Configuration.showOriginalBytesInPcodeHex.get());

        topButtonsPan = new JPanel();
        topButtonsPan.setLayout(new BoxLayout(topButtonsPan, BoxLayout.X_AXIS));
        topButtonsPan.add(graphButton);
        topButtonsPan.add(Box.createRigidArea(new Dimension(10, 0)));
        topButtonsPan.add(hexButton);
        topButtonsPan.add(hexOnlyButton);
        topButtonsPan.add(constantsViewButton);
        topButtonsPan.add(Box.createRigidArea(new Dimension(10, 0)));
        topButtonsPan.add(resolveConstantsButton);
        topButtonsPan.add(Box.createRigidArea(new Dimension(10, 0)));
        topButtonsPan.add(showFileOffsetInPcodeHexButton);
        topButtonsPan.add(showOriginalBytesInPcodeHexButton);

        if (hexOnlyButton.isSelected()) {
            updateHexButtons(ScriptExportMode.HEX);
        } else if (constantsViewButton.isSelected()) {
            updateHexButtons(ScriptExportMode.CONSTANTS);
        } else {
            updateHexButtons(ScriptExportMode.PCODE);
        }

        JPanel panCode = new JPanel(new BorderLayout());

        //sourceTextArea.addDocsListener(docsPanel);
        if (Configuration.displayAs12PCodeDocsPanel.get()) {
            final DocsPanel docsPanel = new DocsPanel();
            ScrollablePanel scrollablePanel = new ScrollablePanel(new BorderLayout());
            scrollablePanel.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
            scrollablePanel.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.STRETCH);
            scrollablePanel.add(docsPanel, BorderLayout.CENTER);
            panCode.add(new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, new FasterScrollPane(editor), new FasterScrollPane(scrollablePanel), Configuration.guiActionDocsSplitPaneDividerLocationPercent), BorderLayout.CENTER);

            editor.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    String lineText = editor.getCurrentLineText();
                    if (lineText != null) {
                        FlasmLexer lexer = new FlasmLexer(new StringReader(lineText));
                        try {
                            ASMParsedSymbol symb = lexer.lex();
                            while (symb.type == ASMParsedSymbol.TYPE_LABEL) {
                                symb = lexer.lex();
                            }
                            if (symb.type == ASMParsedSymbol.TYPE_INSTRUCTION_NAME) {
                                String actionName = (String) symb.value;
                                Color c = UIManager.getColor("EditorPane.background");
                                int light = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                                boolean nightMode = light <= 128;

                                int argumentToHilight = -1;
                                int column = 0;
                                try {
                                    int rowStart = Utilities.getRowStart(editor, e.getDot());
                                    column = e.getDot() - rowStart;
                                } catch (BadLocationException ex) {
                                    //ignore
                                }
                                symb = lexer.lex();
                                if (symb.pos <= column) {
                                    argumentToHilight++;
                                    while (symb.type != ASMParsedSymbol.TYPE_EOL && symb.type != ASMParsedSymbol.TYPE_EOF) {
                                        if (symb.pos >= column) {
                                            break;
                                        }
                                        if (symb.type == ASMParsedSymbol.TYPE_COMMA) {
                                            argumentToHilight++;
                                        }
                                        symb = lexer.lex();
                                    }
                                }

                                String docs = As12PCodeDocs.getDocsForIns(actionName, true, true, nightMode, argumentToHilight);
                                Point loc = editor.getLineLocation(editor.getLine() + 1);
                                if (loc != null) {
                                    SwingUtilities.convertPointToScreen(loc, editor);
                                }

                                docsPanel.docs("action." + actionName, docs, loc);
                            } else {
                                docsPanel.noDocs();
                            }
                        } catch (IOException | ActionParseException ex) {
                            //ignore
                        }
                    }
                }
            });
        } else {
            panCode.add(new FasterScrollPane(editor), BorderLayout.CENTER);
        }
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
        if (Configuration.editorMode.get()) {
            editButton.setVisible(false);
            saveButton.setEnabled(false);
            cancelButton.setEnabled(false);
        } else {
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
        }

        saveDecompiledButton.addActionListener(this::saveDecompiledButtonActionPerformed);
        editDecompiledButton.addActionListener(this::editDecompiledButtonActionPerformed);
        cancelDecompiledButton.addActionListener(this::cancelDecompiledButtonActionPerformed);
        if (Configuration.editorMode.get()) {
            editDecompiledButton.setVisible(false);
            saveDecompiledButton.setEnabled(false);
            cancelDecompiledButton.setEnabled(false);
        } else {
            saveDecompiledButton.setVisible(false);
            cancelDecompiledButton.setVisible(false);
        }

        JPanel panA = new JPanel(new BorderLayout());

        panA.add(decLabel, BorderLayout.NORTH);

        DebugPanel debugPanel = new DebugPanel();

        JPanel panelWithHint = new JPanel(new BorderLayout());
        brokenHintPanel = new JPanel(new BorderLayout(10, 10));
        brokenHintPanel.add(new JLabel("<html>" + AppStrings.translate("script.seemsBroken") + "</html>"), BorderLayout.CENTER);
        brokenHintPanel.setBackground(new Color(253, 205, 137));
        brokenHintPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), new EmptyBorder(5, 5, 5, 5)));

        panelWithHint.add(brokenHintPanel, BorderLayout.NORTH);
        panelWithHint.add(new FasterScrollPane(decompiledEditor), BorderLayout.CENTER);

        brokenHintPanel.setVisible(false);

        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JToggleButton deobfuscateButton = new JToggleButton(View.getIcon("deobfuscate16"));
        deobfuscateButton.setMargin(new Insets(5, 5, 5, 5));
        deobfuscateButton.addActionListener(this::deobfuscateButtonActionPerformed);
        deobfuscateButton.setToolTipText(AppStrings.translate("button.deobfuscate"));
        deobfuscateButton.setSelected(Configuration.autoDeobfuscate.get());
        Configuration.autoDeobfuscate.addListener(new ConfigurationItemChangeListener<Boolean>() {
            @Override
            public void configurationItemChanged(Boolean newValue) {
                deobfuscateButton.setSelected(newValue);
            }
        });

        JButton deobfuscateOptionsButton = new JButton(View.getIcon("deobfuscateoptions16"));
        deobfuscateOptionsButton.addActionListener(this::deobfuscateOptionsButtonActionPerformed);
        deobfuscateOptionsButton.setToolTipText(AppStrings.translate("button.deobfuscate_options"));
        deobfuscateOptionsButton.setMargin(new Insets(0, 0, 0, 0));
        deobfuscateOptionsButton.setPreferredSize(new Dimension(30, deobfuscateButton.getPreferredSize().height));
        iconsPanel.add(deobfuscateButton);
        iconsPanel.add(deobfuscateOptionsButton);

        iconsPanel.add(deobfuscateButton);
        iconsPanel.add(deobfuscateOptionsButton);

        JButton breakpointListButton = new JButton(View.getIcon("breakpointlist16"));
        breakpointListButton.setMargin(new Insets(5, 5, 5, 5));
        breakpointListButton.addActionListener(this::breakPointListButtonActionPerformed);
        breakpointListButton.setToolTipText(AppStrings.translate("button.breakpointList"));
        iconsPanel.add(breakpointListButton);

        JPanel panelWithToolbar = new JPanel(new BorderLayout());
        panelWithToolbar.add(iconsPanel, BorderLayout.NORTH);
        panelWithToolbar.add(panelWithHint, BorderLayout.CENTER);

        panA.add(new JPersistentSplitPane(JSplitPane.VERTICAL_SPLIT, panelWithToolbar, debugPanel, Configuration.guiActionVarsSplitPaneDividerLocationPercent), BorderLayout.CENTER);
        panA.add(decButtonsPan, BorderLayout.SOUTH);

        //decPanel.add(searchPanel, BorderLayout.NORTH);
        Main.getDebugHandler().addConnectionListener(new DebuggerHandler.ConnectionListener() {
            @Override
            public void connected() {
                decButtonsPan.setVisible(false);
            }

            @Override
            public void disconnected() {
                decButtonsPan.setVisible(true);
            }
        });

        debugPanel.setVisible(false);

        decLabel.setHorizontalAlignment(SwingConstants.CENTER);

        setLayout(new BorderLayout());

        if (Configuration.displayAs12PCodePanel.get()) {
            add(splitPane = new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB, Configuration.guiActionSplitPaneDividerLocationPercent), BorderLayout.CENTER);
        } else {
            splitPane = null;
            add(panA, BorderLayout.CENTER);
        }

        editor.setFont(Configuration.getSourceFont());
        decompiledEditor.setFont(Configuration.getSourceFont());
        decompiledEditor.changeContentType("text/actionscript");

        editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCaret) {
                    return;
                }
                if (editMode || editDecompiledMode) {
                    return;
                }
                editor.getCaret().setVisible(true);
                int pos = editor.getCaretPosition();
                Highlighting lastH = null;
                for (Highlighting h : disassembledText.getInstructionHighlights()) {
                    if (pos < h.startPos) {
                        break;
                    }
                    lastH = h;
                }
                Long ofs = lastH == null ? 0 : lastH.getProperties().offset;
                Highlighting h2 = Highlighting.searchOffset(lastDecompiled.getInstructionHighlights(), ofs);
                if (h2 != null) {
                    ignoreCaret = true;
                    if (h2.startPos <= decompiledEditor.getDocument().getLength()) {
                        decompiledEditor.setCaretPosition(h2.startPos);
                    }
                    decompiledEditor.getCaret().setVisible(true);
                    ignoreCaret = false;

                }
            }
        });

        decompiledEditor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (ignoreCaret) {
                    return;
                }
                if (editMode || editDecompiledMode) {
                    return;
                }
                decompiledEditor.getCaret().setVisible(true);
                int pos = decompiledEditor.getCaretPosition();
                Highlighting h = Highlighting.searchPos(lastDecompiled.getInstructionHighlights(), pos);
                if (h != null) {
                    Highlighting h2 = Highlighting.searchOffset(disassembledText.getInstructionHighlights(), h.getProperties().offset);
                    if (h2 != null) {
                        ignoreCaret = true;
                        if (h2.startPos > 0 && h2.startPos < editor.getText().length()) {
                            editor.setCaretPosition(h2.startPos);
                        }
                        editor.getCaret().setVisible(true);
                        ignoreCaret = false;
                    }
                }
            }
        });

        editor.addTextChangedListener(this::editorTextChanged);
        decompiledEditor.addTextChangedListener(this::decompiledEditorTextChanged);
    }

    private void deobfuscateButtonActionPerformed(ActionEvent evt) {
        JToggleButton toggleButton = (JToggleButton) evt.getSource();
        boolean selected = toggleButton.isSelected();

        if (ViewMessages.showConfirmDialog(Main.getDefaultMessagesComponent(), AppStrings.translate("message.confirm.autodeobfuscate") + "\r\n" + (selected ? AppStrings.translate("message.confirm.on") : AppStrings.translate("message.confirm.off")), AppStrings.translate("message.confirm"), JOptionPane.OK_CANCEL_OPTION, Configuration.warningDeobfuscation, JOptionPane.OK_OPTION) == JOptionPane.OK_OPTION) {
            Configuration.autoDeobfuscate.set(selected);
            mainPanel.autoDeobfuscateChanged();
        } else {
            toggleButton.setSelected(Configuration.autoDeobfuscate.get());
        }
    }

    private void deobfuscateOptionsButtonActionPerformed(ActionEvent evt) {
        JPopupMenu popupMenu = new JPopupMenu();
        JCheckBoxMenuItem simplifyExpressionsMenuItem = new JCheckBoxMenuItem(AppStrings.translate("deobfuscate_options.simplify_expressions"));
        simplifyExpressionsMenuItem.setSelected(Configuration.simplifyExpressions.get());
        simplifyExpressionsMenuItem.addActionListener(this::simplifyExpressionsMenuItemActionPerformed);
        JCheckBoxMenuItem removeObfuscatedDeclarationsMenuItem = new JCheckBoxMenuItem(AppStrings.translate("deobfuscate_options.remove_obfuscated_declarations"));
        removeObfuscatedDeclarationsMenuItem.setSelected(Configuration.deobfuscateAs12RemoveInvalidNamesAssignments.get());
        removeObfuscatedDeclarationsMenuItem.addActionListener(this::removeObfuscatedDeclarationsMenuItemActionPerformed);

        popupMenu.add(simplifyExpressionsMenuItem);
        popupMenu.add(removeObfuscatedDeclarationsMenuItem);

        JButton sourceButton = (JButton) evt.getSource();
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());
    }

    private void simplifyExpressionsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
        Configuration.simplifyExpressions.set(menuItem.isSelected());
        mainPanel.autoDeobfuscateChanged();
    }

    private void removeObfuscatedDeclarationsMenuItemActionPerformed(ActionEvent evt) {
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) evt.getSource();
        Configuration.deobfuscateAs12RemoveInvalidNamesAssignments.set(menuItem.isSelected());
        mainPanel.autoDeobfuscateChanged();
    }

    private void breakPointListButtonActionPerformed(ActionEvent evt) {
        Main.showBreakpointsList();
    }

    private void editorTextChanged() {
        setModified(true);
    }

    private void decompiledEditorTextChanged() {
        setDecompiledModified(true);
    }

    private boolean isModified() {
        View.checkAccess();

        return saveButton.isVisible() && saveButton.isEnabled();
    }

    private void setModified(boolean value) {
        View.checkAccess();

        editMode = true;
        mainPanel.setEditingStatus();
        saveButton.setEnabled(value);
        cancelButton.setEnabled(value);
    }

    private boolean isDecompiledModified() {
        View.checkAccess();

        return saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled();
    }

    private void setDecompiledModified(boolean value) {
        View.checkAccess();

        editDecompiledMode = true;
        mainPanel.setEditingStatus();
        saveDecompiledButton.setEnabled(value);
        cancelDecompiledButton.setEnabled(value);
    }

    public void setEditMode(boolean val) {
        View.checkAccess();

        if (val) {
            if (hexOnlyButton.isSelected()) {
                setHex(ScriptExportMode.HEX, src.getScriptName(), src.getExportedScriptName(), lastCode);
            } else if (constantsViewButton.isSelected()) {
                setHex(ScriptExportMode.CONSTANTS, src.getScriptName(), src.getExportedScriptName(), lastCode);
            } else {
                setHex(ScriptExportMode.PCODE, src.getScriptName(), src.getExportedScriptName(), lastCode);
            }
        }

        if (Configuration.editorMode.get()) {
            editor.setEditable(true);
            editButton.setVisible(false);
            saveButton.setVisible(true);
            saveButton.setEnabled(false);
            cancelButton.setVisible(true);
            cancelButton.setEnabled(false);
        } else {
            editor.setEditable(val);
            saveButton.setVisible(val);
            saveButton.setEnabled(false);
            editButton.setVisible(!val);
            cancelButton.setVisible(val);
        }

        editor.getCaret().setVisible(true);
        asmLabel.setIcon(val ? View.getIcon("editing16") : null); // this line is not working
        topButtonsPan.setVisible(!val);
        editMode = val;
        if (val) {
            editor.requestFocusInWindow();
        }
    }

    public void setDecompiledEditMode(boolean val) {
        View.checkAccess();

        if (src != null) {
            if (val) {
                setDecompiledText(src.getScriptName(), src.getExportedScriptName(), lastDecompiled.text);
            } else {
                setDecompiledText(src.getScriptName(), src.getExportedScriptName(), lastDecompiled.text);
            }
        }

        if (Configuration.editorMode.get()) {
            decompiledEditor.setEditable(true);
            editDecompiledButton.setVisible(false);
            saveDecompiledButton.setVisible(true);
            saveDecompiledButton.setEnabled(false);
            cancelDecompiledButton.setVisible(true);
            cancelDecompiledButton.setEnabled(false);
        } else {
            decompiledEditor.setEditable(val);
            saveDecompiledButton.setVisible(val);
            saveDecompiledButton.setEnabled(false);
            editDecompiledButton.setVisible(!val);
            cancelDecompiledButton.setVisible(val);
        }
        decompiledEditor.getCaret().setVisible(true);
        decLabel.setIcon(val ? View.getIcon("editing16") : null);
        editDecompiledMode = val;
        if (val) {
            decompiledEditor.requestFocusInWindow();
        }
    }

    private void graphButtonActionPerformed(ActionEvent evt) {
        if (lastCode != null) {
            try {
                boolean insideDoInitAction = (this.src instanceof DoInitActionTag);
                GraphDialog gf = new GraphDialog(mainPanel.getMainFrame().getWindow(), new ActionGraph(new HashMap<>(), this.src.getScriptName(), insideDoInitAction, false, lastCode, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION, Utf8Helper.charsetName), "");
                gf.setVisible(true);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void editActionButtonActionPerformed(ActionEvent evt) {
        setEditMode(true);
        mainPanel.setEditingStatus();
    }

    private void hexButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void hexOnlyButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void constantsViewButtonActionPerformed(ActionEvent evt) {
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void resolveConstantsButtonActionPerformed(ActionEvent evt) {
        boolean resolve = resolveConstantsButton.isSelected();
        Configuration.resolveConstants.set(resolve);

        srcWithHex = null;
        srcNoHex = null;
        // srcHexOnly = null; is not needed since it does not contains the resolved constant names
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void showFileOffsetInPcodeHexButtonActionPerformed(ActionEvent evt) {
        boolean resolve = showFileOffsetInPcodeHexButton.isSelected();
        Configuration.showFileOffsetInPcodeHex.set(resolve);

        srcWithHex = null;
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void showOriginalBytesInPcodeHexButtonActionPerformed(ActionEvent evt) {
        boolean resolve = showOriginalBytesInPcodeHexButton.isSelected();
        Configuration.showOriginalBytesInPcodeHex.set(resolve);

        srcWithHex = null;
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
    }

    private void cancelActionButtonActionPerformed(ActionEvent evt) {
        setEditMode(false);
        setHex(getExportMode(), src.getScriptName(), src.getExportedScriptName(), lastCode);
        mainPanel.clearEditingStatus();
    }

    private void saveAction(boolean refreshTree) {
        try {
            String text = editor.getText();
            String trimmed = text.trim();
            if (trimmed.startsWith(Helper.hexData)) {
                src.setActionBytes(Helper.getBytesFromHexaText(text));
            } else if (trimmed.startsWith(Helper.constants)) {
                List<List<String>> constantPools = Helper.getConstantPoolsFromText(text);
                try {
                    Action.setConstantPools(src, constantPools, true);
                } catch (ConstantPoolTooBigException ex) {
                    ViewMessages.showMessageDialog(this, AppStrings.translate("error.constantPoolTooBig").replace("%index%", Integer.toString(ex.index)).replace("%size%", Integer.toString(ex.size)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                src.setActions(ASMParser.parse(0, true, text, src.getSwf().version, false, src.getSwf().getCharset()));
            }

            SWF.uncache(src);
            src.setModified();
            setSource(this.src, false);
            if (refreshTree) {
                ViewMessages.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
            }
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            editButton.setVisible(true);
            editor.setEditable(false);
            editMode = false;
            mainPanel.clearEditingStatus();
            if (refreshTree) {
                mainPanel.refreshTree(src.getSwf());
            }
        } catch (IOException ex) {
            //ignored
        } catch (ActionParseException ex) {
            editor.gotoLine((int) ex.line);
            editor.markError();
            ViewMessages.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void saveActionButtonActionPerformed(ActionEvent evt) {
        saveAction(true);
    }

    private void editDecompiledButtonActionPerformed(ActionEvent evt) {
        setDecompiledEditMode(true);
        mainPanel.setEditingStatus();
    }

    private void cancelDecompiledButtonActionPerformed(ActionEvent evt) {
        setDecompiledEditMode(false);
        mainPanel.clearEditingStatus();
    }

    private void saveDecompiled(boolean refreshTree) {
        try {
            ActionScript2Parser par = new ActionScript2Parser(mainPanel.getCurrentSwf(), src);
            src.setActions(par.actionsFromString(decompiledEditor.getText(), src.getSwf().getCharset()));
            SWF.uncache(src);
            src.setModified();
            setSource(src, false);

            if (refreshTree) {
                ViewMessages.showMessageDialog(this, AppStrings.translate("message.action.saved"), AppStrings.translate("dialog.message.title"), JOptionPane.INFORMATION_MESSAGE, Configuration.showCodeSavedMessage);
            }
            setDecompiledEditMode(false);
            mainPanel.clearEditingStatus();
            if (refreshTree) {
                mainPanel.refreshTree(src.getSwf());
            }
        } catch (ValueTooLargeException ex) {
            ViewMessages.showMessageDialog(this, AppStrings.translate("error.action.save.valueTooLarge"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IOException during action compiling", ex);
        } catch (ActionParseException ex) {
            decompiledEditor.gotoLine((int) ex.line);
            decompiledEditor.markError();
            ViewMessages.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (CompilationException ex) {
            decompiledEditor.gotoLine((int) ex.line);
            decompiledEditor.markError();
            ViewMessages.showMessageDialog(this, AppStrings.translate("error.action.save").replace("%error%", ex.text).replace("%line%", Long.toString(ex.line)), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void saveDecompiledButtonActionPerformed(ActionEvent evt) {
        saveDecompiled(true);
    }

    private ScriptExportMode getExportMode() {
        ScriptExportMode exportMode = hexOnlyButton.isSelected() ? ScriptExportMode.HEX
                : hexButton.isSelected() ? ScriptExportMode.PCODE_HEX
                : constantsViewButton.isSelected() ? ScriptExportMode.CONSTANTS
                : ScriptExportMode.PCODE;
        return exportMode;
    }

    @Override
    public void updateSearchPos(String searchedText, boolean ignoreCase, boolean regExp, ScriptSearchResult item) {
        View.checkAccess();

        if (!(item instanceof ActionSearchResult)) {
            return;
        }
        ActionSearchResult result = (ActionSearchResult) item;

        searchPanel.setOptions(ignoreCase, regExp);
        searchPanel.setSearchText(searchedText);
        Runnable onScriptComplete = new Runnable() {
            @Override
            public void run() {
                decompiledEditor.setCaretPosition(0);

                if (result.isPcode()) {
                    searchPanel.showQuickFindDialog(editor);
                } else {
                    searchPanel.showQuickFindDialog(decompiledEditor);
                }
                removeScriptListener(this);
            }
        };

        addScriptListener(onScriptComplete);

        AbstractTagTreeModel ttm = mainPanel.getCurrentTree().getFullModel();
        TreePath tp = ttm.getTreePath(result.getSrc());
        mainPanel.getCurrentTree().setSelectionPath(tp);
        mainPanel.getCurrentTree().scrollPathToVisible(tp);

    }

    @Override
    public boolean tryAutoSave() {
        View.checkAccess();

        boolean ok = true;
        if (saveButton.isVisible() && saveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveAction(false);
            ok = ok && !(saveButton.isVisible() && saveButton.isEnabled());
        }
        if (saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveDecompiled(false);
            ok = ok && !(saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled());
        }
        return ok;
    }

    @Override
    public boolean isEditing() {
        View.checkAccess();

        return (saveButton.isVisible() && saveButton.isEnabled())
                || (saveDecompiledButton.isVisible() && saveDecompiledButton.isEnabled());
    }
}
