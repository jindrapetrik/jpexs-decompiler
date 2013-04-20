package com.jpexs.decompiler.flash.graph;

import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class GraphTargetItem {

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
    public int pos = 0;
    public int precedence;
    public List<GraphSourceItemPos> moreSrc = new ArrayList<GraphSourceItemPos>();

    public GraphTargetItem(GraphSourceItem src, int precedence) {
        this.src = src;
        this.precedence = precedence;
    }

    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = new ArrayList<GraphSourceItemPos>();
        ret.add(new GraphSourceItemPos(src, pos));
        ret.addAll(moreSrc);
        return ret;
    }

    public String hilight(String str) {
        if (src == null) {
            return str;
        }
        return Highlighting.hilighOffset(str, src.getOffset());
    }

    public String toStringSemicoloned(List localData) {
        return toString(localData) + (needsSemicolon() ? ";" : "");
    }

    public String toStringSemicoloned(Object... localData) {
        List localData2 = new ArrayList();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toStringSemicoloned(localData2);
    }

    public boolean needsSemicolon() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public abstract String toString(List localData);

    public String toString(Object... localData) {
        List localData2 = new ArrayList();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toString(localData2);
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

    public double toNumber() {
        return 0;
    }

    public boolean toBoolean() {
        return Double.compare(toNumber(), 0.0) != 0;
    }

    public String toStringNoQuotes(List localData) {
        return toString(localData);
    }

    public String toStringNoQuotes(Object... localData) {
        List localData2 = new ArrayList();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toStringNoQuotes(localData2);
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

    public String toStringNL(Object... localData) {
        List localData2 = new ArrayList();
        for (Object o : localData) {
            localData2.add(o);
        }
        return toString(localData2) + (needsNewLine() ? "\r\n" : "");
    }

    public boolean isEmpty() {
        return false;
    }

    public GraphTargetItem getThroughNotCompilable() {
        return this;
    }
}
