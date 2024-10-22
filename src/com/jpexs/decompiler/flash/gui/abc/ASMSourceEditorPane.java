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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.Flasm3Lexer;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ParsedSymbol;
import com.jpexs.decompiler.flash.abc.types.Float4;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.docs.As3PCodeDocs;
import com.jpexs.decompiler.flash.docs.As3PCodeOtherDocs;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.editor.DebuggableEditorPane;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import macromedia.asc.util.Decimal128;

/**
 * @author JPEXS
 */
public class ASMSourceEditorPane extends DebuggableEditorPane implements CaretListener {

    public ABC abc;

    public int bodyIndex = -1;

    public int methodIndex = -1;

    private int scriptIndex = -1;

    public int getScriptIndex() {
        return scriptIndex;
    }

    private HighlightedText highlightedText = HighlightedText.EMPTY;

    private final List<DocsListener> docsListeners = new ArrayList<>();

    private final DecompiledEditorPane decompiledEditor;

    private boolean ignoreCaret = false;

    private String name;

    private HighlightedText textWithHex;

    private HighlightedText textNoHex;

    private HighlightedText textHexOnly;

    private ScriptExportMode exportMode = ScriptExportMode.PCODE;

    private Trait trait;

    private int firstInstrLine = -1;

    private final Map<String, InstructionDefinition> insNameToDef = new HashMap<>();

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
        if (trait != null && exportMode != ScriptExportMode.AS && exportMode != ScriptExportMode.AS_METHOD_STUBS) {
            trait.convertTraitHeader(abc, writer);
        }
        if (bodyIndex > -1) {
            MethodBody body = abc.bodies.get(bodyIndex);
            abc.bodies.get(bodyIndex).getCode().toASMSource(abc, abc.constants, abc.method_info.get(body.method_info), body, exportMode, writer);
        } else {
            writer.appendNoHilight("method");
            if (Configuration.indentAs3PCode.get()) {
                writer.indent();
            }
            writer.newLine();

            abc.method_info.get(methodIndex).toASMSource(abc, writer);

            if (Configuration.indentAs3PCode.get()) {
                writer.unindent();
            }
            writer.appendNoHilight("end ; method").newLine();
        }
        if (trait != null && exportMode != ScriptExportMode.AS && exportMode != ScriptExportMode.AS_METHOD_STUBS) {
            if (Configuration.indentAs3PCode.get()) {
                writer.unindent();
            }
            writer.appendNoHilight("end ; trait").newLine();
        }
        writer.finishHilights();
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
                if (bodyIndex > -1) {
                    Helper.byteArrayToHexWithHeader(writer, abc.bodies.get(bodyIndex).getCodeBytes());
                }
                writer.finishHilights();
                textHexOnly = new HighlightedText(writer);
            }
            setText(textHexOnly);
        }
        hilightOffset(oldOffset);
    }

    public void setIgnoreCaret(boolean ignoreCaret) {
        this.ignoreCaret = ignoreCaret;
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
        for (Highlighting sh : highlightedText.getSpecialHighlights()) {
            if (type.equals(sh.getProperties().subtype)) {
                if (sh.getProperties().specialValue.equals(specialValue)) {
                    h2 = sh;
                    break;
                }
            }
        }
        if (h2 != null) {
            ignoreCaret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCaret = false;
        }
    }

    public void hilightOffset(long offset) {
        if (isEditable()) {
            return;
        }
        Highlighting h2 = Highlighting.searchOffset(highlightedText.getInstructionHighlights(), offset);
        if (h2 != null) {
            ignoreCaret = true;
            if (h2.startPos <= getDocument().getLength()) {
                setCaretPosition(h2.startPos);
            }
            getCaret().setVisible(true);
            ignoreCaret = false;
        }
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public void setMethod(String scriptPathName, int methodIndex, int bodyIndex, ABC abc, String name, Trait trait, int scriptIndex) {
        this.methodIndex = methodIndex;
        this.bodyIndex = bodyIndex;
        this.abc = abc;
        this.name = name;
        this.trait = trait;
        this.scriptIndex = scriptIndex;
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
        setScriptName(aname, aname);
        setHex(exportMode, true);
    }

    public void graph() {
        try {
            AVM2Graph gr = new AVM2Graph(null /*?*/, abc.bodies.get(bodyIndex).getCode(), abc, abc.bodies.get(bodyIndex), false, -1, -1, new HashMap<>(), new ScopeStack(), new ScopeStack(), new HashMap<>(), new ArrayList<>(), new HashMap<>()); //, abc.bodies.get(bodyIndex).getCode().visitCode(abc.bodies.get(bodyIndex)));
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
            ViewMessages.showMessageDialog(this, "Returned object:" + o.toString());
        } catch (AVM2ExecutionException ex) {
            Logger.getLogger(ASMSourceEditorPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean save() {
        try {
            String text = getText();
            if (text.trim().startsWith(Helper.hexData)) {
                if (bodyIndex > -1) {
                    byte[] data = Helper.getBytesFromHexaText(text);
                    MethodBody mb = abc.bodies.get(bodyIndex);
                    mb.setCodeBytes(data);
                }
            } else {
                AVM2Code acode = ASM3Parser.parse(abc, new StringReader(text), trait, new MissingSymbolHandler() {
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
                    public boolean missingDecimal(Decimal128 value) {
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
                }, bodyIndex == -1 ? null : abc.bodies.get(bodyIndex), abc.method_info.get(methodIndex));
                if (bodyIndex > -1) {
                    abc.bodies.get(bodyIndex).setCode(acode);
                }
            }

            ((Tag) abc.parentTag).setModified(true);
            abc.script_info.get(scriptIndex).setModified(true);
            textWithHex = null;
            textNoHex = null;
            textHexOnly = null;
            setHex(exportMode, true);
        } catch (IOException | InterruptedException ex) {
            //ignored
        } catch (AVM2ParseException ex) {
            gotoLine((int) ex.line);
            markError();
            ViewMessages.showMessageDialog(Main.getDefaultMessagesComponent(), (ex.text + " on line " + ex.line), Main.getMainFrame().translate("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        abc.fireChanged();
        return true;
    }

    @Override
    public void setText(String t) {
        setText(new HighlightedText(t));
    }

    public void setText(HighlightedText highlightedText) {
        this.highlightedText = highlightedText;
        if (!highlightedText.getInstructionHighlights().isEmpty()) {
            int firstPos = highlightedText.getInstructionHighlights().get(0).startPos;
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
        methodIndex = -1;
        scriptIndex = -1;
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
        return Highlighting.searchPos(highlightedText.getSpecialHighlights(), getCaretPosition());
    }

    public long getSelectedOffset() {
        int pos = getCaretPosition();
        Highlighting lastH = null;
        for (Highlighting h : highlightedText.getInstructionHighlights()) {
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

    private String getLevel() {
        int currentLine = getLine();

        int caretPos = getCaretPosition();
        Flasm3Lexer lexer = new Flasm3Lexer(new StringReader(getText().replace("\r\n", "\n")));
        ParsedSymbol symb;
        String lastLevel;
        final Integer[] singleUse = new Integer[]{
            ParsedSymbol.TYPE_KEYWORD_FINAL,
            ParsedSymbol.TYPE_KEYWORD_OVERRIDE,
            ParsedSymbol.TYPE_KEYWORD_METADATA,
            ParsedSymbol.TYPE_KEYWORD_NEED_REST,
            ParsedSymbol.TYPE_KEYWORD_NEED_ARGUMENTS,
            ParsedSymbol.TYPE_KEYWORD_NEED_ACTIVATION,
            ParsedSymbol.TYPE_KEYWORD_SET_DXNS,
            ParsedSymbol.TYPE_KEYWORD_IGNORE_REST,
            ParsedSymbol.TYPE_KEYWORD_HAS_PARAM_NAMES,
            ParsedSymbol.TYPE_KEYWORD_HAS_OPTIONAL
        };

        final Integer[] openingBlocks = new Integer[]{
            ParsedSymbol.TYPE_KEYWORD_METHOD,
            ParsedSymbol.TYPE_KEYWORD_CODE,
            ParsedSymbol.TYPE_KEYWORD_BODY,
            ParsedSymbol.TYPE_KEYWORD_TRAIT,
            ParsedSymbol.TYPE_KEYWORD_METADATA_BLOCK
        };
        final Integer[] singleLine = new Integer[]{
            ParsedSymbol.TYPE_KEYWORD_ITEM,
            ParsedSymbol.TYPE_KEYWORD_NAME,
            ParsedSymbol.TYPE_KEYWORD_FLAG,
            ParsedSymbol.TYPE_KEYWORD_PARAM,
            ParsedSymbol.TYPE_KEYWORD_PARAMNAME,
            ParsedSymbol.TYPE_KEYWORD_OPTIONAL,
            ParsedSymbol.TYPE_KEYWORD_RETURNS,
            ParsedSymbol.TYPE_KEYWORD_MAXSTACK,
            ParsedSymbol.TYPE_KEYWORD_LOCALCOUNT,
            ParsedSymbol.TYPE_KEYWORD_INITSCOPEDEPTH,
            ParsedSymbol.TYPE_KEYWORD_MAXSCOPEDEPTH,
            ParsedSymbol.TYPE_KEYWORD_TRY,
            ParsedSymbol.TYPE_KEYWORD_DISPID,
            ParsedSymbol.TYPE_KEYWORD_SLOTID,
            ParsedSymbol.TYPE_KEYWORD_TYPE,
            ParsedSymbol.TYPE_KEYWORD_VALUE
        };
        final Integer[] parameters = new Integer[]{
            ParsedSymbol.TYPE_KEYWORD_FROM,
            ParsedSymbol.TYPE_KEYWORD_TO,
            ParsedSymbol.TYPE_KEYWORD_TARGET,
            ParsedSymbol.TYPE_KEYWORD_NAME,
            ParsedSymbol.TYPE_KEYWORD_TYPE,
            ParsedSymbol.TYPE_KEYWORD_SLOT,
            ParsedSymbol.TYPE_KEYWORD_CONST,
            ParsedSymbol.TYPE_KEYWORD_GETTER,
            ParsedSymbol.TYPE_KEYWORD_SETTER
        };
        final List<Integer> openingBlocksList = Arrays.asList(openingBlocks);
        final List<Integer> singleLineList = Arrays.asList(singleLine);
        final List<Integer> parameterList = Arrays.asList(parameters);
        final List<Integer> singleUseList = Arrays.asList(singleUse);

        final int TYPE_IGNORED = 0;
        final int TYPE_OPENING_BLOCK = 1;
        final int TYPE_LINE_BLOCK = 2;
        final int TYPE_PARAMETER = 3;
        final int TYPE_SINGLE_USE = 4;
        final int TYPE_CLOSING_BLOCK = 5;

        Stack<String> levels = new Stack<>();
        Stack<Integer> types = new Stack<>();
        Stack<Integer> lines = new Stack<>();

        int prev = -1;
        int lastType;
        int lastLine = 0;
        do {
            try {
                symb = lexer.lex();
            } catch (IOException | AVM2ParseException ex) {
                break; //error
            }
            int line = lexer.yyline();

            lastLevel = null;
            if (!levels.isEmpty()) {
                lastLevel = levels.peek();
            }

            if (line != lastLine && !levels.isEmpty()) {
                while (types.peek() == TYPE_LINE_BLOCK || types.peek() == TYPE_PARAMETER) {
                    levels.pop();
                    types.pop();
                    lines.pop();
                }
            }

            int type = TYPE_IGNORED;
            if (symb.type == ParsedSymbol.TYPE_KEYWORD_METHOD && "trait".equals(lastLevel)) {
                type = TYPE_PARAMETER;
            } else if (symb.type == ParsedSymbol.TYPE_KEYWORD_NAME && "try".equals(lastLevel)) {
                type = TYPE_PARAMETER;
            } else if (openingBlocksList.contains(symb.type)) {
                type = TYPE_OPENING_BLOCK;
            } else if (singleLineList.contains(symb.type)) {
                type = TYPE_LINE_BLOCK;
            } else if (parameterList.contains(symb.type)) {
                type = TYPE_PARAMETER;
            } else if (singleUseList.contains(symb.type)) {
                type = TYPE_SINGLE_USE;
            } else if (symb.type == ParsedSymbol.TYPE_KEYWORD_END) {
                if (levels.isEmpty()) {
                    break; //error
                }
                levels.pop();
                types.pop();
                lines.pop();
                type = TYPE_CLOSING_BLOCK;
            }

            boolean aboutToBreak = false;
            //lexer.yylength()
            if (caretPos < lexer.yychar() + lexer.yylength()) {
                aboutToBreak = true;
            }

            if (type != TYPE_IGNORED) {
                if (!levels.isEmpty()) {
                    if (types.peek() == TYPE_PARAMETER) {
                        levels.pop();
                        types.pop();
                        lines.pop();
                    }
                }
                if (type != TYPE_CLOSING_BLOCK) {
                    levels.push((String) symb.value);
                    types.push(type);
                    lines.push(lexer.yyline());
                }

            }
            if (aboutToBreak) {
                break;
            }

            if (type == TYPE_SINGLE_USE) {
                if (!levels.isEmpty()) {
                    levels.pop();
                    types.pop();
                    lines.pop();
                }
            }
            prev = symb.type;
            if (type == ParsedSymbol.TYPE_KEYWORD_CODE) {
                //do not process code itself - it's too long
                break;
            }
            lastLine = line;
        } while (symb.type != ParsedSymbol.TYPE_EOF);
        String ret = String.join(".", levels);
        return ret;
    }

    public void updateDocs() {
        String path = getLevel();

        Color c = UIManager.getColor("EditorPane.background");
        int light = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        boolean nightMode = light <= 128;

        String pathNoTrait = path;
        if (path.startsWith("trait.method")) {
            pathNoTrait = path.substring("trait.".length());
        }
        if (pathNoTrait.startsWith("method.body.code")) {
            String curLine = getCurrentLineText();

            if (curLine == null) {
                return;
            }
            try {
                Flasm3Lexer lexer = new Flasm3Lexer(new StringReader(curLine));
                ParsedSymbol symb = lexer.lex();
                while (symb.type == ParsedSymbol.TYPE_LABEL) {
                    symb = lexer.lex();
                }
                if (symb.type == ParsedSymbol.TYPE_INSTRUCTION_NAME) {
                    String insName = (String) symb.value;
                    int argumentToHilight = -1;
                    int column = 0;
                    try {
                        int caretPosition = getCaretPosition();
                        int rowStart = Utilities.getRowStart(this, caretPosition);
                        column = caretPosition - rowStart;
                    } catch (BadLocationException ex) {
                        //ignore
                    }
                    symb = lexer.lex();
                    if (symb.pos <= column) {
                        argumentToHilight++;
                        int parentLevel = 0;
                        Stack<Integer> parentsStack = new Stack<>();
                        while (symb.type != ParsedSymbol.TYPE_EOF) {
                            if (symb.pos >= column) {
                                break;
                            }
                            if (symb.type == ParsedSymbol.TYPE_PARENT_OPEN
                                    || symb.type == ParsedSymbol.TYPE_BRACKET_OPEN) {
                                parentsStack.push(symb.type);
                                parentLevel++;
                            }
                            if (symb.type == ParsedSymbol.TYPE_PARENT_CLOSE) {
                                if (parentsStack.isEmpty()) {
                                    throw new IOException("parent stack empty");
                                }
                                if (parentsStack.pop() != ParsedSymbol.TYPE_PARENT_OPEN) {
                                    throw new IOException("invalid parent");
                                }
                                parentLevel--;
                            }
                            if (symb.type == ParsedSymbol.TYPE_BRACKET_CLOSE) {
                                if (parentsStack.isEmpty()) {
                                    throw new IOException("parent stack empty");
                                }
                                if (parentsStack.pop() != ParsedSymbol.TYPE_BRACKET_OPEN) {
                                    throw new IOException("invalid parent");
                                }
                                parentLevel--;
                            }
                            if (parentLevel == 0 && symb.type == ParsedSymbol.TYPE_COMMA) {
                                argumentToHilight++;
                            }
                            symb = lexer.lex();
                        }
                    }
                    if (AVM2Code.instructionAliases.containsKey(insName)) {
                        insName = AVM2Code.instructionAliases.get(insName);
                    }
                    Point loc = getLineLocation(getLine() + 1);
                    if (loc != null) {
                        SwingUtilities.convertPointToScreen(loc, this);
                    }
                    if (insNameToDef.containsKey(insName)) {
                        fireDocs("instruction." + insName, As3PCodeDocs.getDocsForIns(insName, false, true, true, nightMode, argumentToHilight), loc);
                        return;
                    }
                }
            } catch (IOException | AVM2ParseException iex) {
                //ignore
            }
        }
        String pathDocs = As3PCodeOtherDocs.getDocsForPath(pathNoTrait, nightMode);
        if (pathDocs == null) {
            fireNoDocs();
        } else {
            Point loc = getLineLocation(getLine() + 1);
            if (loc != null) {
                SwingUtilities.convertPointToScreen(loc, this);
            }
            fireDocs(pathNoTrait, pathDocs, loc);
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        updateDocs();

        if (ignoreCaret) {
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
