/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.model.BinaryOp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class GraphTargetItem implements Serializable {

    public static final int PRECEDENCE_PRIMARY = 0;
    public static final int PRECEDENCE_POSTFIX = 1;
    public static final int PRECEDENCE_UNARY = 2;
    public static final int PRECEDENCE_MULTIPLICATIVE = 3;
    public static final int PRECEDENCE_ADDITIVE = 4;
    public static final int PRECEDENCE_BITWISESHIFT = 5;
    public static final int PRECEDENCE_RELATIONAL = 6;
    public static final int PRECEDENCE_EQUALITY = 7;
    public static final int PRECEDENCE_BITWISEAND = 8;
    public static final int PRECEDENCE_BITWISEXOR = 9;
    public static final int PRECEDENCE_BITWISEOR = 10;
    public static final int PRECEDENCE_LOGICALAND = 11;
    public static final int PRECEDENCE_LOGICALOR = 12;
    public static final int PRECEDENCE_CONDITIONAL = 13;
    public static final int PRECEDENCE_ASSIGMENT = 14;
    public static final int PRECEDENCE_COMMA = 15;
    public static final int NOPRECEDENCE = 16;
    public GraphSourceItem src;
    public int pos = -1;
    public int precedence;
    public List<GraphSourceItemPos> moreSrc = new ArrayList<>();
    public GraphPart firstPart;
    public GraphTargetItem value;

    public GraphPart getFirstPart() {
        if (value == null) {
            return firstPart;
        }
        GraphPart ret = value.getFirstPart();
        if (ret == null) {
            return firstPart;
        }
        return ret;
    }

    public GraphTargetItem() {
        this(null, NOPRECEDENCE);
    }

    public GraphTargetItem(GraphSourceItem src, int precedence) {
        this.src = src;
        this.precedence = precedence;
    }

    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = new ArrayList<>();
        ret.add(new GraphSourceItemPos(src, pos));
        ret.addAll(moreSrc);
        if (value != null) {
            ret.addAll(value.getNeededSources());
        }
        return ret;
    }

    public String hilight(String str, boolean highlight) {
        if (src == null || !highlight) {
            return str;
        }
        return Highlighting.hilighOffset(str, src.getOffset() + pos + 1);
    }

    public String toStringSemicoloned(boolean highlight, List<Object> localData) {
        return toString(highlight, localData) + (needsSemicolon() ? hilight(";", highlight) : "");
    }

    public String toStringSemicoloned(boolean highlight, Object... localData) {
        List<Object> localData2 = new ArrayList<>();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toStringSemicoloned(highlight, localData2);
    }

    public boolean needsSemicolon() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public abstract String toString(boolean highlight, List<Object> localData);

    public String toString(boolean highlight, Object... localData) {
        List<Object> localData2 = new ArrayList<>();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toString(highlight, localData2);
    }

    public int getPrecedence() {
        return precedence;
    }

    public boolean isCompileTime() {
        return false;
    }

    public boolean hasSideEffect() {
        return false;
    }

    public boolean isVariableComputed() {
        return false;
    }

    /*public double toNumber() {
     return 0;
     }

     public boolean toBoolean() {
     return Double.compare(toNumber(), 0.0) != 0;
     }*/
    public Object getResult() {
        return null;
    }

    public String toStringNoQuotes(boolean highlight, List<Object> localData) {
        return toString(highlight, localData);
    }

    public String toStringNoQuotes(boolean highlight, Object... localData) {
        List<Object> localData2 = new ArrayList<>();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toStringNoQuotes(highlight, localData2);
    }

    public GraphTargetItem getNotCoerced() {
        return this;
    }

    public GraphTargetItem getThroughRegister() {
        return this;
    }

    public boolean needsNewLine() {
        return false;
    }

    public String toStringNL(boolean highlight, Object... localData) {
        List<Object> localData2 = new ArrayList<>();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toString(highlight, localData2) + (needsNewLine() ? "\r\n" : "");
    }

    public boolean isEmpty() {
        return false;
    }

    public GraphTargetItem getThroughNotCompilable() {
        return this;
    }

    public GraphTargetItem getThroughDuplicate() {
        return this;
    }

    public boolean valueEquals(GraphTargetItem target) {
        return equals(target);
    }

    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return new ArrayList<>();
    }

    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        return toSource(localData, generator);
    }

    protected List<GraphSourceItem> toSourceBinary(BinaryOp op, GraphSourceItem action) {
        List<GraphSourceItem> ret = new ArrayList<>();

        return ret;
    }

    protected List<GraphSourceItem> toSourceMerge(List<Object> localData, SourceGenerator gen, Object... tar) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (Object o : tar) {
            if (o instanceof GraphTargetItem) {
                ret.addAll(((GraphTargetItem) o).toSource(localData, gen));
            }
            if (o instanceof GraphSourceItem) {
                ret.add((GraphSourceItem) o);
            }
            if (o instanceof List) {
                List l = (List) o;
                for (Object o2 : l) {
                    if (o2 instanceof GraphSourceItem) {
                        ret.add((GraphSourceItem) o2);
                    }
                }
            }
        }
        return ret;
    }

    public abstract boolean hasReturnValue();

    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (value != null) {
            ret.add(value);
        }
        return ret;
    }
}
