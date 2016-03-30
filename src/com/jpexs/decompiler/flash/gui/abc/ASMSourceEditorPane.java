/*
 *  Copyright (C) 2010-2016 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.types.Decimal;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.editor.DebuggableEditorPane;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.docs.As3PCodeDocs;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.awt.Point;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author JPEXS
 */
public class ASMSourceEditorPane extends DebuggableEditorPane implements CaretListener {

    public ABC abc;

    public int bodyIndex = -1;

    private int scriptIndex = -1;

    public int getScriptIndex() {
        return scriptIndex;
    }

    private List<Highlighting> disassembledHilights = new ArrayList<>();

    private List<Highlighting> specialHilights = new ArrayList<>();

    private List<DocsListener> docsListeners = new ArrayList<>();

    private final DecompiledEditorPane decompiledEditor;

    private boolean ignoreCarret = false;

    private String name;

    private HighlightedText textWithHex;

    private HighlightedText textNoHex;

    private HighlightedText textHexOnly;

    private ScriptExportMode exportMode = ScriptExportMode.PCODE;

    private Trait trait;

    private int firstInstrLine = -1;

    private Map<String, InstructionDefinition> insNameToDef = new HashMap<>();

    public void addDocsListener(DocsListener l) {
        docsListeners.add(l);
    }

    public void removeDocsListener(DocsListener l) {
        docsListeners.remove(l);
    }

    public ABCPanel getAbcPanel() {
        return decompiledEditor.getAbcPanel();
    }

    public ScriptExportMode getExportMode() {
        return exportMode;
    }

    private HighlightedText getHighlightedText(ScriptExportMode exportMode) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        abc.bodies.get(bodyIndex).getCode().toASMSource(abc.constants, trait, abc.method_info.get(abc.bodies.get(bodyIndex).method_info), abc.bodies.get(bodyIndex), exportMode, writer);
        return new HighlightedText(writer);
    }

    public void setHex(ScriptExportMode exportMode, boolean force) {
        if (this.exportMode == exportMode && !force) {
            return;
        }
        this.exportMode = exportMode;
        long oldOffset = getSelectedOffset();
        if (exportMode == ScriptExportMode.PCODE) {
            changeContentType("text/flasm3");
            if (textNoHex == null) {
                textNoHex = getHighlightedText(exportMode);
            }
            setText(textNoHex);
        } else if (exportMode == ScriptExportMode.PCODE_HEX) {
            changeContentType("text/flasm3");
            if (textWithHex == null) {
                textWithHex = getHighlightedText(exportMode);
            }
            setText(textWithHex);
        } else {
            changeContentType("text/plain");
            if (textHexOnly == null) {
                HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
                Helper.byteArrayToHexWithHeader(writer, abc.bodies.get(bodyIndex).getCodeBytes());
                textHexOnly = new HighlightedText(writer);
            }
            setText(textHexOnly);
        }
        hilighOffset(oldOffset);
    }

    public void setIgnoreCarret(boolean ignoreCarret) {
        this.ignoreCarret = ignoreCarret;
    }

    public ASMSourceEditorPane(DecompiledEditorPane decompiledEditor) {
        this.decompiledEditor = decompiledEditor;
        addCaretListener(this);
        for (InstructionDefinition def : AVM2Code.allInstructionSet) {
            if (def != null) {
                insNameToDef.put(def.instructionName, def);
            }
        }
    }

    public void hilighSpecial(HighlightSpecialType type, String specialValue) {
        Highlighting h2 = null;
        for (Highlighting sh : specialHilights) {
            if (type.equals(sh.getProperties().subtype)) {
                if (sh.getProperties().specialValue.equals(specialValue)) {
                    h2 = sh;
                    break;
                }
            }
        }
        if (h2 != null) {
            ignoreCarret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCarret = false;
        }
    }

    public void hilighOffset(long offset) {
        if (isEditable()) {
            return;
        }
        Highlighting h2 = Highlighting.searchOffset(disassembledHilights, offset);
        if (h2 != null) {
            ignoreCarret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCarret = false;
        }
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public void setBodyIndex(String scriptPathName, int bodyIndex, ABC abc, String name, Trait trait, int scriptIndex) {
        this.bodyIndex = bodyIndex;
        this.abc = abc;
        this.name = name;
        this.trait = trait;
        this.scriptIndex = scriptIndex;
        if (bodyIndex == -1) {
            return;
        }
        textWithHex = null;
        textNoHex = null;
        textHexOnly = null;
        List<ABCContainerTag> cs = abc.getAbcTags();
        int abcIndex = -1;
        for (int i = 0; i < cs.size(); i++) {
            if (cs.get(i).getABC() == abc) {
                abcIndex = i;
                break;
            }
        }
        String aname = "#PCODE abc:" + abcIndex + ",body:" + bodyIndex + ";" + scriptPathName;
        setScriptName(aname);
        setHex(exportMode, true);
    }

    public void graph() {
        try {
            AVM2Graph gr = new AVM2Graph(abc.bodies.get(bodyIndex).getCode(), abc, abc.bodies.get(bodyIndex), false, -1, -1, new HashMap<>(), new ScopeStack(), new HashMap<>(), new ArrayList<>(), new HashMap<>(), abc.bodies.get(bodyIndex).getCode().visitCode(abc.bodies.get(bodyIndex)));
            (new GraphDialog(getAbcPanel().getMainPanel().getMainFrame().getWindow(), gr, name)).setVisible(true);
        } catch (InterruptedException ex) {
            Logger.getLogger(ASMSourceEditorPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exec() {
        HashMap<Integer, Object> args = new HashMap<>();
        args.put(0, new Object()); //object "this"
        args.put(1, 466561L); //param1
        try {
            Object o = abc.bodies.get(bodyIndex).getCode().execute(args, abc.constants);
            View.showMessageDialog(this, "Returned object:" + o.toString());
        } catch (AVM2ExecutionException ex) {
            Logger.getLogger(ASMSourceEditorPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean save() {
        try {
            String text = getText();
            if (text.trim().startsWith(Helper.hexData)) {
                byte[] data = Helper.getBytesFromHexaText(text);
                MethodBody mb = abc.bodies.get(bodyIndex);
                mb.setCodeBytes(data);
            } else {
                AVM2Code acode = ASM3Parser.parse(new StringReader(text), abc.constants, trait, new MissingSymbolHandler() {
                    //no longer ask for adding new constants
                    @Override
                    public boolean missingString(String value) {
                        return true;
                    }

                    @Override
                    public boolean missingInt(long value) {
                        return true;
                    }

                    @Override
                    public boolean missingUInt(long value) {
                        return true;
                    }

                    @Override
                    public boolean missingDouble(double value) {
                        return true;
                    }

                    @Override
                    public boolean missingDecimal(Decimal value) {
                        return true;
                    }

                    @Override
                    public boolean missingFloat(float value) {
                        return true;
                    }

                    @Override
                    public boolean missingFloat4(Float4 value) {
                        return true;
                    }

                }, abc.bodies.get(bodyIndex), abc.method_info.get(abc.bodies.get(bodyIndex).method_info));
                //acode.getBytes(abc.bodies.get(bodyIndex).getCodeBytes());
                abc.bodies.get(bodyIndex).setCode(acode);
            }

            ((Tag) abc.parentTag).setModified(true);
            abc.script_info.get(scriptIndex).setModified(true);
            textWithHex = null;
            textNoHex = null;
            textHexOnly = null;
        } catch (IOException ex) {
        } catch (InterruptedException ex) {
        } catch (AVM2ParseException ex) {
            View.showMessageDialog(this, (ex.text + " on line " + ex.line));
            gotoLine((int) ex.line);
            markError();
            return false;
        }
        return true;
    }

    @Override
    public void setText(String t) {
        disassembledHilights = new ArrayList<>();
        specialHilights = new ArrayList<>();
        super.setText(t);
        setCaretPosition(0);
    }

    public void setText(HighlightedText highlightedText) {
        disassembledHilights = highlightedText.instructionHilights;
        if (!disassembledHilights.isEmpty()) {
            int firstPos = disassembledHilights.get(0).startPos;
            String txt = highlightedText.text;
            txt = txt.replace("\r", "");
            int line = 0;
            for (int i = 0; i < firstPos; i++) {
                if (txt.charAt(i) == '\n') {
                    line++;
                }
            }
            firstInstrLine = line;
        }
        specialHilights = highlightedText.specialHilights;
        super.setText(highlightedText.text);
        setCaretPosition(0);
    }

    @Override
    public int firstLineOffset() {
        return firstInstrLine;
    }

    public void gotoInstrLine(int line) {
        super.gotoLine(firstInstrLine + line);
    }

    public void clear() {
        setText("");
        bodyIndex = -1;
        setCaretPosition(0);
    }

    public void selectInstruction(int pos) {
        String text = getText();
        int lineCnt = 1;
        int lineStart = 0;
        int lineEnd;
        int instrCount = 0;
        int dot = -2;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {

                lineCnt++;
                lineEnd = i;
                String ins = text.substring(lineStart, lineEnd).trim();
                if (!((i > 0) && (text.charAt(i - 1) == ':'))) {
                    if (!ins.startsWith("exception ")) {
                        instrCount++;
                    }
                }
                if (instrCount == pos + 1) {
                    break;
                }
                lineStart = i + 1;
            }
        }
        //if (lineCnt == -1) {
        //    lineEnd = text.length() - 1;
        //}
        //select(lineStart, lineEnd);
        setCaretPosition(lineStart);
        //requestFocus();
    }

    public Highlighting getSelectedSpecial() {
        return Highlighting.searchPos(specialHilights, getCaretPosition());
    }

    public long getSelectedOffset() {
        int pos = getCaretPosition();
        Highlighting lastH = null;
        for (Highlighting h : disassembledHilights) {
            if (pos < h.startPos) {
                break;
            }
            lastH = h;
        }
        return lastH == null ? 0 : lastH.getProperties().offset;
    }

    private void fireDocs(String identifier, String value, Point screenLocation) {
        for (DocsListener l : docsListeners) {
            l.docs(identifier, value, screenLocation);
        }
    }

    private void fireNoDocs() {
        for (DocsListener l : docsListeners) {
            l.noDocs();
        }
    }

    public void updateDocs() {
        String curLine = getCurrentLineText();

        if (curLine == null) {
            return;
        }
        //strip labels, e.g. ofs123:pushint 25
        if (curLine.matches("^\\p{L}+:")) {
            curLine = curLine.substring(curLine.indexOf(":") + 1).trim();
        }

        //strip instruction arguments, we want only its name
        if (curLine.contains(" ")) {
            curLine = curLine.substring(0, curLine.indexOf(" "));
        }
        //strip comments, e.g. pushnull;comment
        if (curLine.contains(";")) {
            curLine = curLine.substring(0, curLine.indexOf(";"));
        }
        String insName = curLine.toLowerCase();
        if (insNameToDef.containsKey(insName)) {
            Point loc = getLineLocation(getLine() + 1);
            if (loc != null) {
                SwingUtilities.convertPointToScreen(loc, this);
            }
            fireDocs(insName, As3PCodeDocs.getDocsForIns(insName, false, true, true), loc);
        } else {
            fireNoDocs();
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        updateDocs();

        if (ignoreCarret) {
            return;
        }
        if (isEditable()) {
            return;
        }
        getCaret().setVisible(true);

        decompiledEditor.hilightOffset(getSelectedOffset());
        Highlighting spec = getSelectedSpecial();
        if (spec != null) {
            decompiledEditor.hilightSpecial(spec.getProperties().subtype, spec.getProperties().index);
        }
    }
}
