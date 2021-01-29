/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightType;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class HighlightedTextWriter extends GraphTextWriter {

    private final StringBuilder sb = new StringBuilder();

    private final boolean hilight;

    private boolean newLine = true;

    private int indent = 0;

    private final Stack<GraphSourceItemPosition> offsets = new Stack<>();

    private boolean toStringCalled = false;

    private int newLineCount = 0;

    private final Stack<Highlighting> hilightStack = new Stack<>();

    public List<Highlighting> traitHilights = new ArrayList<>();

    public List<Highlighting> classHilights = new ArrayList<>();

    public List<Highlighting> methodHilights = new ArrayList<>();

    public List<Highlighting> instructionHilights = new ArrayList<>();

    public List<Highlighting> specialHilights = new ArrayList<>();

    public HighlightedTextWriter(CodeFormatting formatting, boolean hilight) {
        super(formatting);
        this.hilight = hilight;
    }

    public HighlightedTextWriter(CodeFormatting formatting, boolean hilight, int indent) {
        super(formatting);
        this.hilight = hilight;
        this.indent = indent;
    }

    @Override
    public boolean getIsHighlighted() {
        return hilight;
    }

    /**
     * Highlights specified text as instruction by adding special tags
     *
     * @param src
     * @param startLineItem
     * @param pos Offset of instruction
     * @param data
     * @return HighlightedTextWriter
     */
    @Override
    public HighlightedTextWriter startOffset(GraphSourceItem src, GraphSourceItem startLineItem, int pos, HighlightData data) {
        GraphSourceItemPosition itemPos = new GraphSourceItemPosition();
        itemPos.graphSourceItem = src;
        itemPos.startLineItem = startLineItem;
        itemPos.position = pos;
        itemPos.data = data;
        offsets.add(itemPos);
        return this;
    }

    @Override
    public HighlightedTextWriter endOffset() {
        offsets.pop();
        return this;
    }

    /**
     * Highlights specified text as method by adding special tags
     *
     * @param index MethodInfo index
     * @return HighlightedTextWriter
     */
    @Override
    public HighlightedTextWriter startMethod(long index) {
        HighlightData data = new HighlightData();
        data.index = index;
        return start(data, HighlightType.METHOD);
    }

    @Override
    public HighlightedTextWriter startFunction(String name) {
        HighlightData data = new HighlightData();
        data.localName = name;
        return start(data, HighlightType.METHOD);
    }

    @Override
    public HighlightedTextWriter endMethod() {
        return end(HighlightType.METHOD);
    }

    @Override
    public HighlightedTextWriter endFunction() {
        return end(HighlightType.METHOD);
    }

    /**
     * Highlights specified text as class by adding special tags
     *
     * @param index Class index
     * @return HighlightedTextWriter
     */
    @Override
    public HighlightedTextWriter startClass(long index) {
        HighlightData data = new HighlightData();
        data.index = index;
        return start(data, HighlightType.CLASS);
    }

    @Override
    public HighlightedTextWriter startClass(String className) {
        HighlightData data = new HighlightData();
        data.localName = className;
        return start(data, HighlightType.CLASS);
    }

    @Override
    public HighlightedTextWriter endClass() {
        return end(HighlightType.CLASS);
    }

    /**
     * Highlights specified text as trait by adding special tags
     *
     * @param index Trait index
     * @return HighlightedTextWriter
     */
    @Override
    public HighlightedTextWriter startTrait(long index) {
        HighlightData data = new HighlightData();
        data.index = index;
        return start(data, HighlightType.TRAIT);
    }

    @Override
    public HighlightedTextWriter endTrait() {
        return end(HighlightType.TRAIT);
    }

    @Override
    protected HighlightedTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        HighlightData ndata = new HighlightData();
        ndata.merge(data);
        ndata.subtype = type;
        ndata.specialValue = specialValue;
        start(ndata, HighlightType.SPECIAL);
        appendNoHilight(text);
        return end(HighlightType.SPECIAL);
    }

    @Override
    public HighlightedTextWriter append(String str) {
        return appendWithData(str, null);
    }

    @Override
    public HighlightedTextWriter appendWithData(String str, HighlightData data) {
        Highlighting h = null;
        if (!offsets.empty()) {
            GraphSourceItemPosition itemPos = offsets.peek();
            GraphSourceItem src = itemPos.graphSourceItem;
            int pos = itemPos.position;
            if (src != null && hilight) {
                HighlightData ndata = new HighlightData();
                ndata.merge(itemPos.data);
                ndata.merge(data);
                long virtualAddress = src.getVirtualAddress();
                if (virtualAddress != -1) {
                    ndata.offset = virtualAddress + pos;
                } else {
                    ndata.offset = src.getAddress() + pos;
                }
                ndata.fileOffset = src.getFileOffset();
                if (itemPos.startLineItem != null) {
                    ndata.firstLineOffset = itemPos.startLineItem.getLineOffset();
                }
                h = new Highlighting(sb.length() - newLineCount, ndata, HighlightType.OFFSET, str);
                instructionHilights.add(h);
            }
        }
        appendToSb(str);
        fixNewLineCount(str);
        if (h != null) {
            h.len = sb.length() - newLineCount - h.startPos;
        }
        return this;
    }

    @Override
    public HighlightedTextWriter append(String str, long offset, long fileOffset) {
        Highlighting h = null;
        if (hilight) {
            HighlightData data = new HighlightData();
            data.offset = offset;
            data.fileOffset = fileOffset;
            h = new Highlighting(sb.length() - newLineCount, data, HighlightType.OFFSET, str);
            instructionHilights.add(h);
        }
        appendToSb(str);
        if (h != null) {
            h.len = sb.length() - newLineCount - h.startPos;
        }
        return this;
    }

    @Override
    public HighlightedTextWriter appendNoHilight(int i) {
        appendNoHilight(Integer.toString(i));
        return this;
    }

    @Override
    public HighlightedTextWriter appendNoHilight(String str) {
        appendToSb(str);
        return this;
    }

    @Override
    public HighlightedTextWriter indent() {
        indent++;
        return this;
    }

    @Override
    public HighlightedTextWriter unindent() {
        indent--;
        return this;
    }

    @Override
    public HighlightedTextWriter newLine() {
        appendToSb(formatting.newLineChars);
        newLine = true;
        newLineCount++;
        return this;
    }

    @Override
    public int getLength() {
        return sb.length();
    }

    @Override
    public int getIndent() {
        return indent;
    }

    @Override
    public String toString() {
        if (toStringCalled) {
            throw new Error("HighlightedTextWriter.toString() was already called.");
        }
        if (Configuration._debugMode.get()) {
            long stopTime = System.currentTimeMillis();
            long time = stopTime - startTime;
            if (time > 500) {
                System.out.println("Rendering is too slow: " + Helper.formatTimeSec(time) + " length: " + sb.length());
            }
        }
        toStringCalled = true;
        return sb.toString();
    }

    private HighlightedTextWriter start(HighlightData data, HighlightType type) {
        if (hilight) {
            Highlighting h = new Highlighting(sb.length() - newLineCount, data, type, null);
            hilightStack.add(h);
        }
        return this;
    }

    private HighlightedTextWriter end(HighlightType expectedType) {
        if (hilight) {
            Highlighting h = hilightStack.pop();
            h.len = sb.length() - newLineCount - h.startPos;

            if (!expectedType.equals(h.type)) {
                throw new Error("Hilighting mismatch.");
            }

            switch (h.type) {
                case CLASS:
                    classHilights.add(h);
                    break;
                case METHOD:
                    methodHilights.add(h);
                    break;
                case TRAIT:
                    traitHilights.add(h);
                    break;
                case SPECIAL:
                    specialHilights.add(h);
                    break;
                case OFFSET:
                    instructionHilights.add(h);
                    break;
            }
        }
        return this;
    }

    private void appendToSb(String str) {
        if (newLine) {
            newLine = false;
            appendIndent();
        }
        sb.append(str);
    }

    private void fixNewLineCount(String str) {
        int nl = 0;
        int rn = 0;
        char prevChar = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '\r' || ch == '\n') {
                rn++;
            }

            if (ch == '\r' || (prevChar != '\r' && ch == '\n')) {
                nl++;
            }

            prevChar = ch;
        }

        newLineCount += rn - nl;
    }

    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            appendNoHilight(formatting.indentString);
        }
    }
}
