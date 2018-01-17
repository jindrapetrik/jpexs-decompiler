/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.GraphSourceItem;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public abstract class GraphTextWriter {

    protected long startTime;

    protected long suspendTime;

    protected CodeFormatting formatting;

    public static final int TRAIT_INSTANCE_INITIALIZER = -1;

    public static final int TRAIT_CLASS_INITIALIZER = -2;

    public static final int TRAIT_SCRIPT_INITIALIZER = -3;

    public static final int TRAIT_UNKNOWN = -4;

    public CodeFormatting getFormatting() {
        return formatting;
    }

    public GraphTextWriter(CodeFormatting formatting) {
        startTime = System.currentTimeMillis();
        this.formatting = formatting;
    }

    public boolean getIsHighlighted() {
        return false;
    }

    /**
     * Highlights specified text as instruction
     *
     * @param src
     * @param startLineItem
     * @param pos Offset of instruction
     * @param data
     * @return GraphTextWriter
     */
    public GraphTextWriter startOffset(GraphSourceItem src, GraphSourceItem startLineItem, int pos, HighlightData data) {
        return this;
    }

    public GraphTextWriter endOffset() {
        return this;
    }

    /**
     * Highlights specified text as method
     *
     * @param index MethodInfo index
     * @return GraphTextWriter
     */
    public GraphTextWriter startMethod(long index) {
        return this;
    }

    /**
     * Highlights specified text as method/function
     *
     * @param name Function name
     * @return GraphTextWriter
     */
    public GraphTextWriter startFunction(String name) {
        return this;
    }

    public GraphTextWriter endMethod() {
        return this;
    }

    public GraphTextWriter endFunction() {
        return this;
    }

    /**
     * Highlights specified text as class
     *
     * @param index Class index
     * @return GraphTextWriter
     */
    public GraphTextWriter startClass(long index) {
        return this;
    }

    public GraphTextWriter startClass(String className) {
        return this;
    }

    public GraphTextWriter endClass() {
        return this;
    }

    /**
     * Highlights specified text as trait
     *
     * @param index Trait index
     * @return GraphTextWriter
     */
    public GraphTextWriter startTrait(long index) {
        return this;
    }

    public GraphTextWriter endTrait() {
        return this;
    }

    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type) {
        return hilightSpecial(text, type, "");
    }

    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, int specialValue) {
        return hilightSpecial(text, type, Integer.toString(specialValue), null);
    }

    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, int specialValue, HighlightData data) {
        return hilightSpecial(text, type, Integer.toString(specialValue), data);
    }

    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue) {
        return hilightSpecial(text, type, specialValue, null);
    }

    protected GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        return this;
    }

    public static String hilighOffset(String text, long offset) {
        return "";
    }

    public abstract GraphTextWriter appendWithData(String str, HighlightData data);

    public GraphTextWriter append(char value) {
        return append(Character.toString(value));
    }

    public GraphTextWriter append(int value) {
        return append(Integer.toString(value));
    }

    public GraphTextWriter append(long value) {
        return append(Long.toString(value));
    }

    public GraphTextWriter append(double value) {
        return append(Double.toString(value));
    }

    public abstract GraphTextWriter append(String str);

    public abstract GraphTextWriter append(String str, long offset, long fileOffset);

    public abstract GraphTextWriter appendNoHilight(int i);

    public abstract GraphTextWriter appendNoHilight(String str);

    public GraphTextWriter indent() {
        return this;
    }

    public GraphTextWriter unindent() {
        return this;
    }

    public GraphTextWriter newLine() {
        return this;
    }

    public int getLength() {
        return 0;
    }

    public int getIndent() {
        return 0;
    }

    public void suspendMeasure() {
        suspendTime = System.currentTimeMillis();
    }

    public void continueMeasure() {
        long time = System.currentTimeMillis();
        startTime += time - suspendTime;
    }

    @Override
    public String toString() {
        return "";
    }

    private GraphTextWriter startBlock(String opening) {
        if (formatting.beginBlockOnNewLine) {
            newLine();
        } else {
            append(" ");
        }
        return append(opening).newLine().indent();
    }

    public GraphTextWriter startBlock() {
        return startBlock("{");
    }

    private GraphTextWriter endBlock(String closing) {
        return unindent().append(closing);
    }

    public GraphTextWriter endBlock() {
        return endBlock("}");
    }

    public GraphTextWriter space() {
        return append(" ");
    }

    public GraphTextWriter spaceBeforeCallParenthesies(int argCount) {
        if (argCount > 0) {
            if (formatting.spaceBeforeParenthesesMethodCallParentheses) {
                space();
            }
        } else if (formatting.spaceBeforeParenthesesMethodCallEmptyParentheses) {
            space();
        }
        return this;
    }
}
