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
package com.jpexs.decompiler.flash.gui.action;

import com.jpexs.decompiler.flash.Configuration;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionGraph;
import com.jpexs.decompiler.flash.action.parser.ParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.ASMParser;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptParser;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;
import com.jpexs.decompiler.flash.gui.GraphFrame;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.TagTreeModel;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.helpers.Cache;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.tree.TreePath;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.actions.DocumentSearchData;

public class ActionPanel extends JPanel implements ActionListener {

    public LineMarkedEditorPane editor;
    public LineMarkedEditorPane decompiledEditor;
    public List<Tag> list;
    public JSplitPane splitPane;
    public JButton saveButton = new JButton(translate("button.save"), View.getIcon("save16"));
    public JButton editButton = new JButton(translate("button.edit"), View.getIcon("edit16"));
    public JButton cancelButton = new JButton(translate("button.cancel"), View.getIcon("cancel16"));
    public JLabel experimentalLabel = new JLabel(translate("action.edit.experimental"));
    public JButton editDecompiledButton = new JButton(translate("button.edit"), View.getIcon("edit16"));
    public JButton saveDecompiledButton = new JButton(translate("button.save"), View.getIcon("save16"));
    public JButton cancelDecompiledButton = new JButton(translate("button.cancel"), View.getIcon("cancel16"));
    public JToggleButton hexButton;
    public JLabel asmLabel = new JLabel(translate("panel.disassembled"));
    public JLabel decLabel = new JLabel(translate("panel.decompiled"));
    public List<Highlighting> decompiledHilights = new ArrayList<>();
    public List<Highlighting> disassembledHilights = new ArrayList<>();
    public String lastDisasm = "";
    private boolean ignoreCarret = false;
    private boolean editMode = false;
    private boolean editDecompiledMode = false;
    private List<com.jpexs.decompiler.flash.action.Action> lastCode;
    private ASMSource src;
    public JPanel topButtonsPan;
    private String srcWithHex;
    private String srcNoHex;
    private String lastDecompiled = "";
    private ASMSource lastASM;
    public JPanel searchPanel;
    public JLabel searchPos;
    private List<ASMSource> found = new ArrayList<>();
    private int foundPos = 0;
    private JLabel searchForLabel;
    private String searchFor;
    private boolean searchIgnoreCase;
    private boolean searchRegexp;
    private Cache cache = Cache.getInstance(true);

    public void clearCache() {
        cache.clear();
    }

    public String getStringUnderCursor() {
        int pos = decompiledEditor.getCaretPosition();
        for (Highlighting h : decompiledHilights) {
            if ((pos >= h.startPos) && (pos < h.startPos + h.len)) {
                List<Action> list = lastCode;
                Action lastIns = null;
                int inspos = 0;
                Action selIns = null;
                for (Action ins : list) {
                    if (h.offset == ins.getOffset()) {
                        selIns = ins;
                        break;
                    }
                    if (ins.getOffset() > h.offset) {
                        inspos = (int) (h.offset - lastIns.getAddress());
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
        }
        return null;
    }

    private CachedScript getCached(ASMSource pack) {
        return (CachedScript) cache.get(pack);
    }

    private void cacheScript(ASMSource src) {
        if (!cache.contains(src)) {
            List<Action> as = src.getActions(SWF.DEFAULT_VERSION);
            String s = com.jpexs.decompiler.flash.action.Action.actionsToSource(as, SWF.DEFAULT_VERSION);
            List<Highlighting> hilights = Highlighting.getInstrHighlights(s);
            String srcNoHex = Highlighting.stripHilights(s);
            cache.put(src, new CachedScript(srcNoHex, hilights));
        }
    }

    private List<ASMSource> getASMs(List<TagNode> nodes) {
        List<ASMSource> ret = new ArrayList<>();
        for (TagNode n : nodes) {
            if (n.tag instanceof ASMSource) {
                //cacheScript((ASMSource) n.tag);
                ret.add((ASMSource) n.tag);
            }
            ret.addAll(getASMs(n.subItems));
        }
        return ret;
    }

    public boolean search(String txt, boolean ignoreCase, boolean regexp) {
        if ((txt != null) && (!txt.equals(""))) {
            searchIgnoreCase = ignoreCase;
            searchRegexp = regexp;
            List<Object> tags = new ArrayList<Object>(Main.swf.tags);
            List<TagNode> list = Main.swf.createASTagList(tags, null);
            List<ASMSource> asms = getASMs(list);
            found = new ArrayList<>();
            Pattern pat = null;
            if (regexp) {
                pat = Pattern.compile(txt, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } else {
                pat = Pattern.compile(Pattern.quote(txt), ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            }
            for (ASMSource s : asms) {
                cacheScript(s);
                if (pat.matcher(getCached(s).text).find()) {
                    found.add(s);
                }
            }

            if (found.isEmpty()) {
                searchPanel.setVisible(false);
                return false;
            } else {
                foundPos = 0;
                setSource(found.get(foundPos), true);
                searchPanel.setVisible(true);
                searchFor = txt;
                updateSearchPos();
                searchForLabel.setText(translate("search.info").replace("%text%", txt) + " ");
            }
            return true;
        }
        return false;
    }

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
        editor.setText("; " + translate("work.gettinghilights") + "...");
        disassembledHilights = Highlighting.getInstrHighlights(text);
        String stripped = Highlighting.stripHilights(text);
        /*if(stripped.length()>30000){
         editor.setContentType("text/plain");
         }else{
         editor.setContentType("text/flasm");
         }*/
        editor.setText(stripped);
        for (Highlighting h : disassembledHilights) {
            if (h.offset == offset) {
                if (h.startPos <= editor.getText().length()) {
                    editor.setCaretPosition(h.startPos);
                }
                break;
            }
        }

    }

    public void setHex(boolean hex) {
        setText(hex ? srcWithHex : srcNoHex);
    }

    public void setSource(ASMSource src, final boolean useCache) {
        this.src = src;
        Main.startWork(translate("work.decompiling") + "...");
        final ASMSource asm = (ASMSource) src;
        (new Thread() {
            @Override
            public void run() {
                editor.setText("; " + translate("work.disassembling") + "...");
                if ((Boolean) Configuration.getConfig("decompile", Boolean.TRUE)) {
                    decompiledEditor.setText("//" + translate("work.waitingfordissasembly") + "...");
                }
                DisassemblyListener listener = new DisassemblyListener() {
                    int percent = 0;
                    String phase = "";

                    @Override
                    public void progress(String phase, long pos, long total) {
                        if (total < 1) {
                            return;
                        }
                        int newpercent = (int) (pos * 100 / total);
                        if (((newpercent > percent) || (!this.phase.equals(phase))) && newpercent <= 100) {
                            percent = newpercent;
                            this.phase = phase;
                            editor.setText("; " + translate("work.disassembling") + " - " + phase + " " + percent + "%...");
                        }
                    }
                };
                asm.addDisassemblyListener(listener);
                lastDisasm = asm.getASMSource(SWF.DEFAULT_VERSION, true);
                asm.removeDisassemblyListener(listener);
                srcWithHex = Helper.hexToComments(lastDisasm);
                srcNoHex = Helper.stripComments(lastDisasm);
                setHex(hexButton.isSelected());
                if ((Boolean) Configuration.getConfig("decompile", Boolean.TRUE)) {
                    decompiledEditor.setText("//" + translate("work.decompiling") + "...");
                    String stripped = "";
                    if (!useCache) {
                        uncache(asm);
                    }
                    cacheScript(asm);
                    CachedScript sc = getCached(asm);
                    lastCode = asm.getActions(SWF.DEFAULT_VERSION);
                    decompiledHilights = sc.hilights;
                    lastDecompiled = sc.text;
                    lastASM = asm;
                    stripped = lastDecompiled;
                    decompiledEditor.setText(asm.getActionSourcePrefix() + lastDecompiled + asm.getActionSourceSuffix());
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
        DefaultSyntaxKit.initKit();
        editor = new LineMarkedEditorPane();
        editor.setEditable(false);
        decompiledEditor = new LineMarkedEditorPane();
        decompiledEditor.setEditable(false);

        searchPanel = new JPanel(new FlowLayout());

        JButton prevSearchButton = new JButton(View.getIcon("prev16"));
        prevSearchButton.setMargin(new Insets(3, 3, 3, 3));
        prevSearchButton.addActionListener(this);
        prevSearchButton.setActionCommand("SEARCHPREV");
        JButton nextSearchButton = new JButton(View.getIcon("next16"));
        nextSearchButton.setMargin(new Insets(3, 3, 3, 3));
        nextSearchButton.addActionListener(this);
        nextSearchButton.setActionCommand("SEARCHNEXT");
        JButton cancelSearchButton = new JButton(View.getIcon("cancel16"));
        cancelSearchButton.setMargin(new Insets(3, 3, 3, 3));
        cancelSearchButton.addActionListener(this);
        cancelSearchButton.setActionCommand("SEARCHCANCEL");
        searchPos = new JLabel("0/0");
        searchForLabel = new JLabel(translate("search.info").replace("%text%", ""));
        searchPanel.add(searchForLabel);
        searchPanel.add(prevSearchButton);
        searchPanel.add(new JLabel("Script "));
        searchPanel.add(searchPos);
        searchPanel.add(nextSearchButton);
        searchPanel.add(cancelSearchButton);


        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.setActionCommand("GRAPH");
        graphButton.addActionListener(this);
        graphButton.setToolTipText(translate("button.viewgraph"));
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hex16"));
        hexButton.setActionCommand("HEX");
        hexButton.addActionListener(this);
        hexButton.setToolTipText(translate("button.viewhex"));
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

        JPanel decPanel = new JPanel(new BorderLayout());
        decPanel.add(new JScrollPane(decompiledEditor), BorderLayout.CENTER);
        decPanel.add(searchPanel, BorderLayout.NORTH);

        searchPanel.setVisible(false);
        JPanel panA = new JPanel();
        panA.setLayout(new BorderLayout());
        panA.add(decPanel, BorderLayout.CENTER);
        panA.add(decLabel, BorderLayout.NORTH);
        panA.add(decButtonsPan, BorderLayout.SOUTH);
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        decLabel.setBorder(new BevelBorder(BevelBorder.RAISED));






        setLayout(new BorderLayout());
        add(splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panA, panB), BorderLayout.CENTER);
        splitPane.setResizeWeight(0.5);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                Configuration.setConfig("gui.action.splitPane.dividerLocation", pce.getNewValue());
            }
        });
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
                                if (h2.startPos > 0 && h2.startPos < editor.getText().length()) {
                                    editor.setCaretPosition(h2.startPos);
                                }
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
        splitPane.setDividerLocation((Integer) Configuration.getConfig("gui.action.splitPane.dividerLocation", getWidth() / 2));
    }

    public void display() {
        setVisible(true);
        splitPane.setDividerLocation(0.5);
    }

    public void setEditMode(boolean val) {
        if (val) {
            setText(srcNoHex);
            editor.setEditable(true);
            saveButton.setVisible(true);
            editButton.setVisible(false);
            cancelButton.setVisible(true);
            editor.getCaret().setVisible(true);
            asmLabel.setIcon(View.getIcon("editing16"));
        } else {
            setText(hexButton.isSelected() ? srcWithHex : srcNoHex);
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
        String pref = lastASM.getActionSourcePrefix();
        int lastPos = decompiledEditor.getCaretPosition();
        if (val) {
            String newText = lastDecompiled;
            decompiledEditor.setText(newText);
            if (lastPos > -1) {
                int newpos = lastPos - pref.length();
                if (newpos < newText.length() && newpos >= 0) {
                    decompiledEditor.setCaretPosition(newpos);
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
            String newText = pref + lastDecompiled + lastASM.getActionSourceSuffix();
            decompiledEditor.setText(newText);
            if (lastPos > -1) {
                int newpos = lastPos + pref.length();
                if (newpos < newText.length()) {
                    decompiledEditor.setCaretPosition(newpos);
                }
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
        if (e.getActionCommand().equals("SEARCHCANCEL")) {
            foundPos = 0;
            searchPanel.setVisible(false);
            found = new ArrayList<>();
            searchFor = null;
        }
        if (e.getActionCommand().equals("SEARCHPREV")) {
            foundPos--;
            if (foundPos < 0) {
                foundPos += found.size();
            }
            updateSearchPos();
        }
        if (e.getActionCommand().equals("SEARCHNEXT")) {
            foundPos = (foundPos + 1) % found.size();
            updateSearchPos();
        }
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
                src.setActions(ASMParser.parse(0, src.getPos(), true, new StringReader(editor.getText()), SWF.DEFAULT_VERSION), SWF.DEFAULT_VERSION);
                setSource(this.src, false);
                JOptionPane.showMessageDialog(this, translate("message.action.saved"));
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                editButton.setVisible(true);
                editor.setEditable(false);
                editMode = false;
            } catch (IOException ex) {
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), translate("error"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getActionCommand().equals("EDITDECOMPILED")) {
            setDecompiledEditMode(true);
        } else if (e.getActionCommand().equals("CANCELDECOMPILED")) {
            setDecompiledEditMode(false);
        } else if (e.getActionCommand().equals("SAVEDECOMPILED")) {
            try {
                ActionScriptParser par = new ActionScriptParser();
                src.setActions(par.parse(decompiledEditor.getText()), SWF.DEFAULT_VERSION);
                setSource(this.src, false);
                JOptionPane.showMessageDialog(this, translate("message.action.saved"));
                setDecompiledEditMode(false);
            } catch (IOException ex) {
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, translate("error.action.save").replace("%error%", ex.text).replace("%line%", "" + ex.line), translate("error"), JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public void updateSearchPos() {
        searchPos.setText((foundPos + 1) + "/" + found.size());
        setSource(found.get(foundPos), true);
        TagTreeModel ttm = (TagTreeModel) Main.mainFrame.tagTree.getModel();
        TreePath tp = ttm.getTagPath(found.get(foundPos));
        Main.mainFrame.tagTree.setSelectionPath(tp);
        Main.mainFrame.tagTree.scrollPathToVisible(tp);
        decompiledEditor.setCaretPosition(0);
        java.util.Timer t = new java.util.Timer();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DocumentSearchData dsd = DocumentSearchData.getFromEditor(decompiledEditor);
                dsd.setPattern(searchFor, searchRegexp, searchIgnoreCase);
                dsd.showQuickFindDialogEx(decompiledEditor, searchIgnoreCase, searchRegexp);
            }
        });
    }

    private void uncache(ASMSource pack) {
        cache.remove(pack);
    }
}
