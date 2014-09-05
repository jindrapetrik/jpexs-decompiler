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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.UnknownInstructionCode;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.pcode.MissingSymbolHandler;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.gui.GraphDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.HilightedText;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class ASMSourceEditorPane extends LineMarkedEditorPane implements CaretListener {

    public ABC abc;
    public int bodyIndex = -1;
    private List<Highlighting> disassembledHilights = new ArrayList<>();
    private List<Highlighting> specialHilights = new ArrayList<>();
    private final DecompiledEditorPane decompiledEditor;
    private boolean ignoreCarret = false;
    private String name;
    private HilightedText textWithHex;
    private HilightedText textNoHex;
    private HilightedText textHexOnly;
    private ScriptExportMode exportMode = ScriptExportMode.PCODE;
    private Trait trait;

    public ABCPanel getAbcPanel() {
        return decompiledEditor.getAbcPanel();
    }

    public ScriptExportMode getExportMode() {
        return exportMode;
    }

    private HilightedText getHilightedText(ScriptExportMode exportMode) {
        HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
        abc.bodies.get(bodyIndex).code.toASMSource(abc.constants, trait, abc.method_info.get(abc.bodies.get(bodyIndex).method_info), abc.bodies.get(bodyIndex), exportMode, writer);
        return new HilightedText(writer);
    }

    public void setHex(ScriptExportMode exportMode, boolean force) {
        if (this.exportMode == exportMode & !force) {
            return;
        }
        this.exportMode = exportMode;
        long oldOffset = getSelectedOffset();
        if (exportMode == ScriptExportMode.PCODE) {
            setContentType("text/flasm");
            if (textNoHex == null) {
                textNoHex = getHilightedText(exportMode);
            }
            setText(textNoHex);
        } else if (exportMode == ScriptExportMode.PCODE_HEX) {
            setContentType("text/flasm");
            if (textWithHex == null) {
                textWithHex = getHilightedText(exportMode);
            }
            setText(textWithHex);
        } else {
            setContentType("text/plain");
            if (textHexOnly == null) {
                HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), true);
                Helper.byteArrayToHexWithHeader(writer, abc.bodies.get(bodyIndex).code.getBytes());
                textHexOnly = new HilightedText(writer);
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
    }

    public void hilighSpecial(String type, int index) {
        Highlighting h2 = null;
        for (Highlighting sh : specialHilights) {
            if (type.equals(sh.getPropertyString("subtype"))) {
                if (sh.getPropertyString("index").equals("" + index)) {
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
        Highlighting h2 = Highlighting.search(disassembledHilights, "offset", "" + offset);
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

    public void setBodyIndex(int bodyIndex, ABC abc, String name, Trait trait) {
        this.bodyIndex = bodyIndex;
        this.abc = abc;
        this.name = name;
        this.trait = trait;
        if (bodyIndex == -1) {
            return;
        }
        textWithHex = null;
        textNoHex = null;
        setHex(exportMode, true);
    }

    public void graph() {
        try {
            AVM2Graph gr = new AVM2Graph(abc.bodies.get(bodyIndex).code, abc, abc.bodies.get(bodyIndex), false, -1, -1, new HashMap<Integer, GraphTargetItem>(), new ScopeStack(), new HashMap<Integer, String>(), new ArrayList<String>(), new HashMap<Integer, Integer>(), abc.bodies.get(bodyIndex).code.visitCode(abc.bodies.get(bodyIndex)));
            (new GraphDialog(getAbcPanel().getMainPanel().getMainFrame().getWindow(), gr, name)).setVisible(true);
        } catch (InterruptedException ex) {
            Logger.getLogger(ASMSourceEditorPane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void exec() {
        HashMap<Integer, Object> args = new HashMap<>();
        args.put(0, new Object()); //object "this"
        args.put(1, new Long(466561)); //param1
        Object o = abc.bodies.get(bodyIndex).code.execute(args, abc.constants);
        View.showMessageDialog(this, "Returned object:" + o.toString());
    }

    public boolean save() {
        try {
            String text = getText();
            if (text.trim().startsWith("#hexdata")) {
                byte[] data = Helper.getBytesFromHexaText(text);
                MethodBody mb = abc.bodies.get(bodyIndex);
                mb.codeBytes = data;
                try {
                    ABCInputStream ais = new ABCInputStream(new MemoryInputStream(mb.codeBytes));
                    mb.code = new AVM2Code(ais);
                } catch (UnknownInstructionCode re) {
                    mb.code = new AVM2Code();
                    Logger.getLogger(ASMSourceEditorPane.class.getName()).log(Level.SEVERE, null, re);
                }
                mb.code.compact();
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
                }, abc.bodies.get(bodyIndex), abc.method_info.get(abc.bodies.get(bodyIndex).method_info));
                acode.getBytes(abc.bodies.get(bodyIndex).codeBytes);
                abc.bodies.get(bodyIndex).code = acode;
            }
            ((Tag) abc.parentTag).setModified(true);
        } catch (IOException ex) {
        } catch (InterruptedException ex) {
        } catch (ParseException ex) {
            View.showMessageDialog(this, (ex.text + " on line " + ex.line));
            selectLine((int) ex.line);
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

    public void setText(HilightedText hilightedText) {
        disassembledHilights = hilightedText.instructionHilights;
        specialHilights = hilightedText.specialHilights;
        super.setText(hilightedText.text);
        setCaretPosition(0);
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
        if (lineCnt == -1) {
            //lineEnd = text.length() - 1;
        }
        //select(lineStart, lineEnd);
        setCaretPosition(lineStart);
        //requestFocus();
    }

    public void selectLine(int line) {
        String text = getText();
        int lineCnt = 1;
        int lineStart = 0;
        int lineEnd = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCnt++;
                if (lineCnt == line) {
                    lineStart = i;
                }
                if (lineCnt == line + 1) {
                    lineEnd = i;
                }
            }
        }
        if (lineCnt == -1) {
            lineEnd = text.length() - 1;
        }
        select(lineStart, lineEnd);
        requestFocus();
    }

    public Highlighting getSelectedSpecial() {
        return Highlighting.search(specialHilights, getCaretPosition());
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
        return lastH == null ? 0 : lastH.getPropertyLong("offset");
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        if (isEditable()) {
            return;
        }
        if (ignoreCarret) {
            return;
        }
        getCaret().setVisible(true);

        decompiledEditor.hilightOffset(getSelectedOffset());
        Highlighting spec = getSelectedSpecial();
        if (spec != null) {
            decompiledEditor.hilightSpecial(spec.getPropertyString("subtype"), (int) (long) spec.getPropertyLong("index"));
        }
    }
}
