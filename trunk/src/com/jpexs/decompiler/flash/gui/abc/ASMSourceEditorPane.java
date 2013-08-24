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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2Graph;
import com.jpexs.decompiler.flash.abc.avm2.parser.ASM3Parser;
import com.jpexs.decompiler.flash.abc.avm2.parser.ParseException;
import com.jpexs.decompiler.flash.gui.GraphFrame;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class ASMSourceEditorPane extends LineMarkedEditorPane implements CaretListener {

    public ABC abc;
    public int bodyIndex = -1;
    private List<Highlighting> disassembledHilights = new ArrayList<>();
    private DecompiledEditorPane decompiledEditor;
    private boolean ignoreCarret = false;
    private String name;
    private String textWithHex = "";
    private String textNoHex = "";
    private boolean hex = false;

    public boolean isHex() {
        return hex;
    }

    public void switchHex() {
        setHex(!hex);
    }

    public void setHex(boolean hex) {
        if (this.hex == hex) {
            return;
        }
        this.hex = hex;
        long oldOffset = getSelectedOffset();
        if (hex) {
            setText(textWithHex);
        } else {
            setText(textNoHex);
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

    public void hilighOffset(long offset) {
        if (isEditable()) {
            return;
        }
        for (Highlighting h2 : disassembledHilights) {
            if (h2.offset == offset) {
                ignoreCarret = true;
                try {
                    setCaretPosition(h2.startPos);
                } catch (IllegalArgumentException iex) {
                }
                getCaret().setVisible(true);
                ignoreCarret = false;
                break;
            }
        }
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public void setBodyIndex(int bodyIndex, ABC abc, String name) {
        this.bodyIndex = bodyIndex;
        this.abc = abc;
        this.name = name;
        if (bodyIndex == -1) {
            return;
        }
        String textWithHexTags = abc.bodies[bodyIndex].code.toASMSource(abc.constants, abc.bodies[bodyIndex], true);
        textWithHex = Helper.hexToComments(textWithHexTags);
        textNoHex = Helper.stripComments(textWithHexTags);
        setText(hex ? textWithHex : textNoHex);
    }

    public void graph() {
        AVM2Graph gr = new AVM2Graph(abc.bodies[bodyIndex].code, abc, abc.bodies[bodyIndex], false, -1, -1, new HashMap<Integer, GraphTargetItem>(), new Stack<GraphTargetItem>(), new HashMap<Integer, String>(), new ArrayList<String>(), new HashMap<Integer, Integer>(), abc.bodies[bodyIndex].code.visitCode(abc.bodies[bodyIndex]));
        (new GraphFrame(gr, name)).setVisible(true);
    }

    public void exec() {
        HashMap<Integer, Object> args = new HashMap<>();
        args.put(0, new Object()); //object "this"
        args.put(1, new Long(466561)); //param1
        Object o = abc.bodies[bodyIndex].code.execute(args, abc.constants);
        View.showMessageDialog(this, "Returned object:" + o.toString());
    }

    public boolean save(ConstantPool constants) {
        try {
            AVM2Code acode = ASM3Parser.parse(new ByteArrayInputStream(getText().getBytes("UTF-8")), constants, new DialogMissingSymbolHandler(), abc.bodies[bodyIndex]);
            acode.getBytes(abc.bodies[bodyIndex].codeBytes);
            abc.bodies[bodyIndex].code = acode;
        } catch (IOException ex) {
        } catch (ParseException ex) {
            View.showMessageDialog(this, (ex.text + " on line " + ex.line));
            selectLine((int) ex.line);
            return false;
        }
        return true;
    }

    @Override
    public void setText(String t) {
        disassembledHilights = Highlighting.getInstrHighlights(t);
        t = Highlighting.stripHilights(t);
        super.setText(t);
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

    public long getSelectedOffset() {
        int pos = getCaretPosition();
        Highlighting lastH = new Highlighting(0, 0, 0);
        for (Highlighting h : disassembledHilights) {
            if (pos < h.startPos) {
                break;
            }
            lastH = h;
        }
        return lastH.offset;
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
    }
}
