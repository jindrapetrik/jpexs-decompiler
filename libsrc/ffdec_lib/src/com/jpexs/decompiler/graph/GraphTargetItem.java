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
 * License along with this library.
 */
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.model.BinaryOp;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class GraphTargetItem implements Serializable, Cloneable {

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

    private GraphSourceItem src;

    protected int precedence;

    private List<GraphSourceItemPos> moreSrc;

    public GraphPart firstPart;

    public GraphTargetItem value;

    private HighlightData srcData;

    public GraphSourceItem lineStartItem;

    public GraphSourceItem getLineStartItem() {
        return lineStartItem;
    }

    protected static GraphTargetItem valToItem(Object r) {
        if (r == null) {
            return null;
        }
        if (r instanceof Boolean) {
            if ((Boolean) r) {
                return new TrueItem(null, null);
            } else {
                return new FalseItem(null, null);
            }
        }
        if (r instanceof String) {
            return new StringAVM2Item(null, null, (String) r);
        }
        if (r instanceof Long) {
            return new IntegerValueAVM2Item(null, null, (Long) r);
        }
        if (r instanceof Integer) {
            return new IntegerValueAVM2Item(null, null, (long) (int) (Integer) r);
        }

        if (r instanceof Double) {
            return new FloatValueAVM2Item(null, null, (Double) r);
        }
        if (r instanceof Null) {
            return new NullAVM2Item(null, null);
        }
        if (r instanceof Undefined) {
            return new UndefinedAVM2Item(null, null);
        }
        if (r instanceof ArrayType) {
            List<GraphTargetItem> vals = new ArrayList<>();
            ArrayType at = (ArrayType) r;
            for (Object v : at.values) {
                vals.add(valToItem(v));
            }
            return new NewArrayAVM2Item(null, null, vals);
        }
        if (r instanceof ObjectType) {
            List<NameValuePair> props = new ArrayList<>();
            ObjectType ot = (ObjectType) r;
            for (String k : ot.getAttributeNames()) {
                props.add(new NameValuePair(valToItem(k), valToItem(ot.getAttribute(k))));
            }
            return new NewObjectAVM2Item(null, null, props);
        }
        return null;
    }

    public static GraphTargetItem simplifySomething(GraphTargetItem it, String implicitCoerce) {
        if ((it instanceof SimpleValue) && implicitCoerce.isEmpty()) {
            if (((SimpleValue) it).isSimpleValue()) {
                return it;
            }
        }

        if (!it.isCompileTime() && !(!implicitCoerce.isEmpty() && it.isConvertedCompileTime(new HashSet<>()))) {
            return it;
        }
        Object r = it.getResult();
        switch (implicitCoerce) {
            case "String":
                r = EcmaScript.toString(r);
                break;
            case "Number":
                r = EcmaScript.toNumber(r);
                break;
            case "int":
                r = EcmaScript.toInt32(r);
                break;
            case "Boolean":
                r = EcmaScript.toBoolean(r);
                break;
        }

        GraphTargetItem it2 = valToItem(r);
        if (it2 == null) {
            return it;
        }
        return it2;
    }

    public GraphTargetItem simplify(String implicitCoerce) {
        return simplifySomething(this, implicitCoerce);
    }

    public int getLine() {
        if (src != null) {
            return src.getLine();
        }
        return 0;
    }

    public String getFile() {
        if (src != null) {
            return src.getFile();
        }
        return null;
    }

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
        this(null, null, NOPRECEDENCE);
    }

    public GraphTargetItem(GraphSourceItem src, GraphSourceItem lineStartItem, int precedence) {
        this(src, lineStartItem, precedence, null);
    }

    public GraphTargetItem(GraphSourceItem src, GraphSourceItem lineStartItem, int precedence, GraphTargetItem value) {
        this.src = src;
        this.lineStartItem = lineStartItem;
        this.precedence = precedence;
        this.value = value;
    }

    public GraphSourceItem getSrc() {
        return src;
    }

    public List<GraphSourceItemPos> getMoreSrc() {
        if (moreSrc == null) {
            moreSrc = new ArrayList<>();
        }

        return moreSrc;
    }

    protected HighlightData getSrcData() {
        if (srcData == null) {
            srcData = new HighlightData();
        }

        return srcData;
    }

    protected int getPos() {
        return 0;
    }

    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = new ArrayList<>();
        ret.add(new GraphSourceItemPos(src, getPos()));
        if (moreSrc != null) {
            ret.addAll(moreSrc);
        }

        if (value != null) {
            ret.addAll(value.getNeededSources());
        }

        return ret;
    }

    public GraphTextWriter toStringSemicoloned(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendTry(writer, localData);
        if (needsSemicolon()) {
            writer.appendNoHilight(";");
        }
        writer.endOffset();
        return writer;
    }

    public boolean needsSemicolon() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    public GraphTextWriter toStringBoolean(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "Boolean");
    }

    public GraphTextWriter toStringString(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "String");
    }

    public GraphTextWriter toStringInt(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "int");
    }

    public GraphTextWriter toStringNumber(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "Number");
    }

    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData, String implicitCoerce) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendTry(writer, localData, implicitCoerce);
        writer.endOffset();
        return writer;
    }

    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "");
    }

    public abstract GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException;

    public GraphTextWriter appendTry(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return appendTry(writer, localData, "");
    }

    public GraphTextWriter appendTry(GraphTextWriter writer, LocalData localData, String implicitCoerce) throws InterruptedException {
        GraphTargetItem t = this;
        if (!implicitCoerce.isEmpty()) {    //if implicit coerce equals explicit
            if (t instanceof ConvertAVM2Item) {
                if (implicitCoerce.equals((((ConvertAVM2Item) t).type.toString()))) {
                    t = t.value;
                }
            }
        }
        if (!implicitCoerce.isEmpty() && Configuration.simplifyExpressions.get()) {
            t = t.simplify(implicitCoerce);
        }
        return t.appendTo(writer, localData);

    }

    public String toString(LocalData localData) throws InterruptedException {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        toString(writer, localData);
        return writer.toString();
    }

    public int getPrecedence() {
        return precedence;
    }

    public boolean isCompileTime() {
        Set<GraphTargetItem> dependencies = new HashSet<>();
        dependencies.add(this);
        return isCompileTime(dependencies);
    }

    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false;
    }

    public boolean isConvertedCompileTime(Set<GraphTargetItem> dependencies) {
        return isCompileTime();
    }

    public boolean hasSideEffect() {
        return false;
    }

    public boolean isVariableComputed() {
        return false;
    }

    public Object getResult() {
        return null;
    }

    public Double getResultAsNumber() {
        return EcmaScript.toNumberAs2(getResult());
    }

    public String getResultAsString() {
        return EcmaScript.toString(getResult());
    }

    public Boolean getResultAsBoolean() {
        return EcmaScript.toBoolean(getResult());
    }

    public String toStringNoQuotes(LocalData localData) {
        try {
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            toStringNoQuotes(writer, localData);
            return writer.toString();
        } catch (InterruptedException ex) {
            //ignore
        }
        return "";
    }

    public GraphTextWriter toStringNoQuotes(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendToNoQuotes(writer, localData);
        writer.endOffset();
        return writer;
    }

    public GraphTextWriter appendToNoQuotes(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData);
    }

    public GraphTargetItem getNotCoerced() {
        return this;
    }

    public GraphTargetItem getNotCoercedNoDup() {
        return getNotCoerced();
    }

    public GraphTargetItem getThroughRegister() {
        return this;
    }

    public boolean needsNewLine() {
        return false;
    }

    public boolean handlesNewLine() {
        return false;
    }

    public GraphTextWriter toStringNL(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendTry(writer, localData);
        if (needsNewLine()) {
            writer.newLine();
        }
        writer.endOffset();
        return writer;
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

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return new ArrayList<>();
    }

    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (!hasReturnValue()) {
            return toSource(localData, generator);
        }
        return generator.generateDiscardValue(localData, this);
    }

    protected List<GraphSourceItem> toSourceBinary(BinaryOp op, GraphSourceItem action) {
        List<GraphSourceItem> ret = new ArrayList<>();

        return ret;
    }

    public static List<GraphSourceItem> toSourceMerge(SourceGeneratorLocalData localData, SourceGenerator gen, Object... tar) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (Object o : tar) {
            if (o == null) {
                continue;
            }
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
                    if (o2 instanceof GraphTargetItem) {
                        ret.addAll(((GraphTargetItem) o2).toSource(localData, gen));
                    }
                }
            }
        }
        return ret;
    }

    public abstract boolean hasReturnValue();

    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        visit(new AbstractGraphTargetVisitor() {
            @Override
            public void visit(GraphTargetItem item) {
                if (item != null) {
                    ret.add(item);
                }
            }
        });
        return ret;
    }

    public Set<GraphTargetItem> getAllSubItemsRecursively() {
        Set<GraphTargetItem> ret = new HashSet<>();
        visitRecursively(new AbstractGraphTargetVisitor() {
            @Override
            public void visit(GraphTargetItem item) {
                ret.add(item);
            }
        });
        return ret;
    }

    public final void visitRecursively(GraphTargetVisitorInterface visitor) {
        Set<GraphTargetItem> visitedItems = new HashSet<>();
        visit(new AbstractGraphTargetVisitor() {
            @Override
            public void visit(GraphTargetItem item) {
                if (item != null && !visitedItems.contains(item)) {
                    visitedItems.add(item);
                    visitor.visit(item);
                    item.visit(this);
                }
            }
        });
    }

    public final void visitRecursivelyNoBlock(GraphTargetVisitorInterface visitor) {
        Set<GraphTargetItem> visitedItems = new HashSet<>();
        visitNoBlock(new AbstractGraphTargetVisitor() {
            @Override
            public void visit(GraphTargetItem item) {
                if (item != null && !visitedItems.contains(item)) {
                    visitedItems.add(item);
                    visitor.visit(item);
                    item.visitNoBlock(this);
                }
            }
        });
    }

    public void visit(GraphTargetVisitorInterface visitor) {
        if (value != null) {
            visitor.visit(value);
        }
    }

    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
        visit(visitor);
    }

    public abstract GraphTargetItem returnType();

    @Override
    protected GraphTargetItem clone() {
        try {
            return (GraphTargetItem) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public GraphTargetItem invert(GraphSourceItem src) {
        return new NotItem(src, getLineStartItem(), this);
    }

    public GraphTextWriter appendCommands(GraphTargetItem prevLineItem, GraphTextWriter writer, LocalData localData, List<GraphTargetItem> commands, boolean asBlock) throws InterruptedException {

        //This may be useful in the future, but we must handle obfuscated SWFs where there is only one debugline instruction on the beggining.
        final boolean useLineInfo = false;

        int prevLine = prevLineItem == null ? 0 : prevLineItem.getLine();
        if (asBlock) {
            writer.startBlock();
        }
        boolean first = true;
        for (GraphTargetItem ti : commands) {
            if (!ti.isEmpty()) {
                //Use stored line information if available to place commands on same line
                if (!first && (!useLineInfo || (ti.getLine() < 1 || prevLine < 1 || (prevLine >= 1 && prevLine != ti.getLine())))) {
                    writer.newLine();
                }
                prevLine = ti.getLine();
                first = false;
                ti.toStringSemicoloned(writer, localData);
            }
        }
        if (asBlock) {
            if (!first) {
                writer.newLine();
            }

            writer.endBlock();
        }
        return writer;
    }

    public GraphTextWriter appendBlock(GraphTargetItem prevLineItem, GraphTextWriter writer, LocalData localData, List<GraphTargetItem> commands) throws InterruptedException {
        appendCommands(prevLineItem, writer, localData, commands, true);
        return writer;
    }

    public long getAsLong() {
        if (this instanceof DirectValueActionItem) {
            DirectValueActionItem dvai = (DirectValueActionItem) this;
            return (long) (double) EcmaScript.toNumberAs2(dvai.value);
        }

        return 0;
    }

    public boolean isIdentical(GraphTargetItem other) {
        return this == other;
    }
}
