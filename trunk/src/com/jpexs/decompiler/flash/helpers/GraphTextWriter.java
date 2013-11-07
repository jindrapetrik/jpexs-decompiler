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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.graph.GraphSourceItem;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class GraphTextWriter {
    
    public static final String INDENT_STRING = "   ";
    public static final String NEW_LINE = "\r\n";
    protected long startTime;
    protected long suspendTime;

    public GraphTextWriter() {
        startTime = System.currentTimeMillis();
    }

    public boolean getIsHighlighted() {
        return false;
    }

    /**
     * Highlights specified text as instruction
     *
     * @param pos Offset of instruction
     * @return GraphTextWriter
     */
    public GraphTextWriter startOffset(GraphSourceItem src, int pos) {
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
    
    public GraphTextWriter endMethod() {
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
    
    public GraphTextWriter hilightSpecial(String text, String type) {
        return this;
    }
    
    public GraphTextWriter hilightSpecial(String text, String type, int index) {
        return this;
    }
    
    public static String hilighOffset(String text, long offset) {
        return "";
    }

    public GraphTextWriter append(String str) {
        return this;
    }

    public GraphTextWriter append(String str, long offset) {
        return this;
    }

    public GraphTextWriter appendNoHilight(int i) {
        return this;
    }

    public GraphTextWriter appendNoHilight(String str) {
        return this;
    }

    public GraphTextWriter indent() {
        return this;
    }

    public GraphTextWriter unindent() {
        return this;
    }

    public GraphTextWriter newLine() {
        return this;
    }

    public GraphTextWriter stripSemicolon() {
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
}
