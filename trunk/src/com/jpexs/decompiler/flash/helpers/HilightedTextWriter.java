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

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.helpers.Helper;
import java.util.Stack;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class HilightedTextWriter {
    
    public static final String INDENT_STRING = "   ";
    private StringBuilder sb = new StringBuilder();
    private boolean hilight;
    private boolean newLine = true;
    private int indent = 0;
    private Stack<GraphSourceItemPosition> offsets = new Stack<>();
    private Stack<LoopWithType> loopStack = new Stack<>();

    public HilightedTextWriter(boolean hilight) {
        this.hilight = hilight;
    }

    public boolean getIsHighlighted() {
        return hilight;
    }

    public HilightedTextWriter startOffset(GraphSourceItem src, int pos) {
        GraphSourceItemPosition itemPos = new GraphSourceItemPosition();
        itemPos.graphSourceItem = src;
        itemPos.position = pos;
        offsets.add(itemPos);
        return this;
    }
    
    public HilightedTextWriter endOffset() {
        offsets.pop();
        return this;
    }
    
    public HilightedTextWriter startMethod(long index) {
        if (hilight) {
            appendNoHilight(Highlighting.HLOPEN);
            appendNoHilight(Helper.escapeString("type=method;index=" + index));
            appendNoHilight(Highlighting.HLEND);
        }
        return this;
    }
    
    public HilightedTextWriter endMethod() {
        if (hilight) {
            appendNoHilight(Highlighting.HLCLOSE);
        }
        return this;
    }
    
    public HilightedTextWriter startLoop(long loopId, int loopType) {
        LoopWithType loop = new LoopWithType();
        loop.loopId = loopId;
        loop.type = loopType;
        loopStack.add(loop);
        return this;
    }
    
    public HilightedTextWriter endLoop(long loopId)  {
        LoopWithType loopIdInStack = loopStack.pop();
        if (loopId != loopIdInStack.loopId) {
            throw new Error("LoopId mismatch");
        }
        return this;
    }
    
    public long getLoop() {
        if (loopStack.isEmpty()) {
            return -1;
        }
        return loopStack.peek().loopId;
    }
    
    public long getNonSwitchLoop() {
        if (loopStack.isEmpty()) {
            return -1;
        }

        int pos = loopStack.size() - 1;
        LoopWithType loop;
        do {
            loop = loopStack.get(pos);
            pos--;
        } while ((pos >= 0) && (loop.type == LoopWithType.LOOP_TYPE_SWITCH));

        if (loop.type == LoopWithType.LOOP_TYPE_SWITCH) {
            return -1;
        }

        return loop.loopId;
    }
    
    public HilightedTextWriter append(String str) {
        GraphSourceItemPosition itemPos = offsets.peek();
        GraphSourceItem src = itemPos.graphSourceItem;
        int pos = itemPos.position;
        if (src != null && hilight) {
            appendToSb(Highlighting.hilighOffset(str, src.getOffset() + pos + 1));
        } else {
            appendToSb(str);
        }
        return this;
    }

    public HilightedTextWriter appendNoHilight(String str) {
        appendToSb(str);
        return this;
    }

    public HilightedTextWriter indent() {
        indent++;
        return this;
    }

    public HilightedTextWriter unindent() {
        indent--;
        return this;
    }

    public HilightedTextWriter newLine() {
        sb.append("\r\n");
        newLine = true;
        return this;
    }

    public HilightedTextWriter stripSemicolon() {
        // hack
        if (sb.charAt(sb.length() - 1) == ';') {
            sb.setLength(sb.length() - 1);
        }
        return this;
    }

    public int getLength() {
        return sb.length();
    }
    
    public String toString() {
        return sb.toString();
    }
    
    private void appendToSb(String str) {
        if (newLine) {
            newLine = false;
            appendIndent();
        }
        sb.append(str);
    }
    
    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            appendNoHilight(INDENT_STRING);
        }
    }
}
