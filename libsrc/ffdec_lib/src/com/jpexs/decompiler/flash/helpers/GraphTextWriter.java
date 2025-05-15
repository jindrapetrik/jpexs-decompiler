/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.GraphSourceItem;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public abstract class GraphTextWriter {

    /**
     * Start time
     */
    protected long startTime;

    /**
     * Suspend time
     */
    protected long suspendTime;

    /**
     * Code formatting
     */
    protected CodeFormatting formatting;
    
    /**
     * Line length
     */
    protected int lineLength = 0;
        
    /**
     * Trait index - instance initializer
     */
    public static final int TRAIT_INSTANCE_INITIALIZER = -1;

    /**
     * Trait index - class initializer
     */
    public static final int TRAIT_CLASS_INITIALIZER = -2;

    /**
     * Trait index - script initializer
     */
    public static final int TRAIT_SCRIPT_INITIALIZER = -3;

    /**
     * Trait index - unknown
     */
    public static final int TRAIT_UNKNOWN = -4;

    /**
     * Gets code formatting
     * @return Code formatting
     */
    public CodeFormatting getFormatting() {
        return formatting;
    }

    /**
     * Constructor.
     * @param formatting Code formatting
     */
    public GraphTextWriter(CodeFormatting formatting) {
        startTime = System.currentTimeMillis();
        this.formatting = formatting;
    }

    /**
     * Gets is highlighted.
     * @return Is highlighted
     */
    public boolean getIsHighlighted() {
        return false;
    }

    /**
     * Highlights specified text as instruction
     *
     * @param src Graph source item
     * @param startLineItem Start line item
     * @param pos Offset of instruction
     * @param data Highlight data
     * @return GraphTextWriter
     */
    public GraphTextWriter startOffset(GraphSourceItem src, GraphSourceItem startLineItem, int pos, HighlightData data) {
        return this;
    }

    /**
     * Ends offset.
     * @return GraphTextWriter
     */
    public GraphTextWriter endOffset() {
        return this;
    }

    /**
     * Highlights specified text as method
     *
     * @param index MethodInfo index
     * @param name Method name
     * @return GraphTextWriter
     */
    public GraphTextWriter startMethod(long index, String name) {
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

    /**
     * Ends method.
     * @return Writer
     */
    public GraphTextWriter endMethod() {
        return this;
    }

    /**
     * Ends function.
     * @return Writer
     */
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

    /**
     * Highlights specified text as class
     * @param className Class name
     * @return GraphTextWriter
     */
    public GraphTextWriter startClass(String className) {
        return this;
    }

    /**
     * Ends class.
     * @return GraphTextWriter
     */
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

    /**
     * Ends trait.
     * @return GraphTextWriter
     */
    public GraphTextWriter endTrait() {
        return this;
    }

    /**
     * Hilights special type.
     * @param text Text
     * @param type Highlight special type
     * @return GraphTextWriter
     */
    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type) {
        return hilightSpecial(text, type, "");
    }

    /**
     * Hilights special type.
     * @param text Text
     * @param type Highlight special type
     * @param specialValue Special value
     * @return GraphTextWriter
     */
    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, int specialValue) {
        return hilightSpecial(text, type, Integer.toString(specialValue), null);
    }

    /**
     * Hilights special type.
     * @param text Text
     * @param type Highlight special type
     * @param specialValue Special value
     * @return GraphTextWriter
     */
    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, int specialValue, HighlightData data) {
        return hilightSpecial(text, type, Integer.toString(specialValue), data);
    }

    /**
     * Hilights special type.
     * @param text Text
     * @param type Highlight special type
     * @param specialValue Special value
     * @return GraphTextWriter
     */
    public final GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue) {
        return hilightSpecial(text, type, specialValue, null);
    }

    /**
     * Hilights special type.
     * @param text Text
     * @param type Highlight special type
     * @param specialValue Special value
     * @param data Highlight data
     * @return GraphTextWriter
     */
    protected GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue, HighlightData data) {
        return this;
    }

    /**
     * Hilights offset.
     * @param text Text
     * @param offset Offset
     * @return GraphTextWriter
     */
    public static String hilightOffset(String text, long offset) {
        return "";
    }

    /**
     * Appends text with data.
     * @param str Text
     * @param data Highlight data
     * @return GraphTextWriter
     */
    public abstract GraphTextWriter appendWithData(String str, HighlightData data);

    /**
     * Appends text.
     * @param value Value
     * @return GraphTextWriter
     */
    public GraphTextWriter append(char value) {
        return append(Character.toString(value));
    }

    /**
     * Appends text.
     * @param value Value
     * @return GraphTextWriter
     */
    public GraphTextWriter append(int value) {
        return append(Integer.toString(value));
    }

    /**
     * Appends text.
     * @param value Value
     * @return GraphTextWriter
     */
    public GraphTextWriter append(long value) {
        return append(Long.toString(value));
    }

    /**
     * Appends text.
     * @param value Value
     * @return GraphTextWriter
     */
    public GraphTextWriter append(double value) {
        return append(Double.toString(value));
    }

    /**
     * Appends text.
     * @param str Text
     * @return GraphTextWriter
     */
    public abstract GraphTextWriter append(String str);

    /**
     * Appends text.
     * @param str Text
     * @param offset Offset
     * @param fileOffset File offset
     * @return GraphTextWriter
     */
    public abstract GraphTextWriter append(String str, long offset, long fileOffset);

    /**
     * Enlarges line length
     * @param len Length to add
     */
    protected final void addLineLength(int len) {
        lineLength += len;
    }
    
    /**
     * Appends text without highlight.
     * @param i Text
     * @return GraphTextWriter
     */
    public abstract GraphTextWriter appendNoHilight(int i);

    /**
     * Appends text without highlight.
     * @param str Text
     * @return GraphTextWriter
     */
    public abstract GraphTextWriter appendNoHilight(String str);

    /**
     * Indents text.
     * @return GraphTextWriter
     */
    public GraphTextWriter indent() {
        return this;
    }

    /**
     * Unindents text.
     * @return GraphTextWriter
     */
    public GraphTextWriter unindent() {
        return this;
    }

    /**
     * New line.
     * @return GraphTextWriter
     */
    public GraphTextWriter newLine() {
        lineLength = 0;        
        return this;
    }

    /**
     * Gets length.
     * @return Length
     */
    public int getLength() {
        return 0;
    }

    /**
     * Gets indent.
     * @return Indent
     */
    public int getIndent() {
        return 0;
    }

    /**
     * Suspends measure.
     */
    public void suspendMeasure() {
        suspendTime = System.currentTimeMillis();
    }

    /**
     * Continues measure.
     */
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

    /**
     * Starts block.
     * @return GraphTextWriter
     */
    public GraphTextWriter startBlock() {
        return startBlock("{");
    }
    
    /**
     * Starts block, but do not continue to new line.
     * @return GraphTextWriter
     */
    public GraphTextWriter startBlockNoNewLine() {
        if (formatting.beginBlockOnNewLine) {
            newLine();
        } else {
            append(" ");
        }
        return append("{");
    }

    /**
     * Ends block.
     * @param closing Closing
     * @return GraphTextWriter
     */
    private GraphTextWriter endBlock(String closing) {
        return unindent().append(closing);
    }

    /**
     * Ends block.
     * @return GraphTextWriter
     */
    public GraphTextWriter endBlock() {
        return endBlock("}");
    }

    /**
     * Space
     * @return GraphTextWriter
     */
    public GraphTextWriter space() {
        return append(" ");
    }

    public GraphTextWriter allowWrapHere() {
        if (Configuration.maxScriptLineLength.get() > 0 && lineLength > Configuration.maxScriptLineLength.get()) {
            newLine();
        }
        return this;
    }        
    
    /**
     * Space before call parenthesis.
     * @param argCount Argument count
     * @return GraphTextWriter
     */
    public GraphTextWriter spaceBeforeCallParenthesis(int argCount) {
        if (argCount > 0) {
            if (formatting.spaceBeforeParenthesesMethodCallParentheses) {
                space();
            }
        } else if (formatting.spaceBeforeParenthesesMethodCallEmptyParentheses) {
            space();
        }
        return this;
    }

    /**
     * Adds current method data.
     * @param data Highlight data
     * @return GraphTextWriter
     */
    public GraphTextWriter addCurrentMethodData(HighlightData data) {
        return this;
    }
}
