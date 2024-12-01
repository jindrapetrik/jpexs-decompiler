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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DoubleValueAVM2Item;
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
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Reference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Graph target item - an item in high level representation of the code.
 * Decompilation target.
 *
 * @author JPEXS
 */
public abstract class GraphTargetItem implements Serializable, Cloneable {

    //Precedence levels
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

    public static final int PRECEDENCE_NULLCOALESCE = 13;
    
    public static final int PRECEDENCE_CONDITIONAL = 14;

    public static final int PRECEDENCE_ASSIGNMENT = 15;

    public static final int PRECEDENCE_COMMA = 16;

    public static final int NOPRECEDENCE = 17;

    /**
     * Source item
     */
    private GraphSourceItem src;

    /**
     * Precedence level
     */
    protected int precedence;

    /**
     * More source items
     */
    private List<GraphSourceItemPos> moreSrc;

    /**
     * First part of the graph
     */
    public GraphPart firstPart;

    /**
     * Value
     */
    public GraphTargetItem value;

    /**
     * Source hilight data
     */
    private HighlightData srcData;

    /**
     * Line start item
     */
    public GraphSourceItem lineStartItem;
    
    /**
     * ASM Position
     */
    protected int pos = 0;

    /**
     * Gets the line start item
     *
     * @return Line start item
     */
    public GraphSourceItem getLineStartItem() {
        return lineStartItem;
    }

    /**
     * Converts a value to an item
     *
     * FIXME!!! This should only convert to values relatable to current Graph type, 
     * e.g. ActionItems for AS1/2, AVM2Items for AS3
     * 
     * @param r Value
     * @return Graph target item
     */
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
            return new DoubleValueAVM2Item(null, null, (double) (Long) r);
        }
        if (r instanceof Integer) {
            return new IntegerValueAVM2Item(null, null, (Integer) r);
        }

        if (r instanceof Double) {
            return new DoubleValueAVM2Item(null, null, (Double) r);
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

    /**
     * Simplifies something
     *
     * @param it Graph target item
     * @param implicitCoerce Implicit coerce
     * @return Simplified graph target item
     */
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

    /**
     * Simplifies this.
     *
     * @param implicitCoerce Implicit coerce
     * @return Simplified graph target item
     */
    public GraphTargetItem simplify(String implicitCoerce) {
        return simplifySomething(this, implicitCoerce);
    }

    /**
     * Gets line.
     *
     * @return Line
     */
    public int getLine() {
        if (src != null) {
            return src.getLine();
        }
        return 0;
    }

    /**
     * Gets file.
     *
     * @return File
     */
    public String getFile() {
        if (src != null) {
            return src.getFile();
        }
        return null;
    }

    /**
     * Gets first graph part.
     *
     * @return First graph part
     */
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

    /**
     * Constructs GraphTargetItem
     */
    public GraphTargetItem() {
        this(null, null, NOPRECEDENCE);
    }

    /**
     * Constructs GraphTargetItem
     *
     * @param src Source item
     * @param lineStartItem Line start item
     * @param precedence Precedence
     */
    public GraphTargetItem(GraphSourceItem src, GraphSourceItem lineStartItem, int precedence) {
        this(src, lineStartItem, precedence, null);
    }

    /**
     * Constructs GraphTargetItem
     *
     * @param src Source item
     * @param lineStartItem Line start item
     * @param precedence Precedence
     * @param value Value
     */
    public GraphTargetItem(GraphSourceItem src, GraphSourceItem lineStartItem, int precedence, GraphTargetItem value) {
        this.src = src;
        this.lineStartItem = lineStartItem;
        this.precedence = precedence;
        this.value = value;
    }

    /**
     * Gets source item
     *
     * @return Source item
     */
    public GraphSourceItem getSrc() {
        return src;
    }

    /**
     * Gets more source items
     *
     * @return More source items
     */
    public List<GraphSourceItemPos> getMoreSrc() {
        if (moreSrc == null) {
            moreSrc = new ArrayList<>();
        }

        return moreSrc;
    }

    /**
     * Gets highlight src data
     *
     * @return Highlight src data
     */
    protected HighlightData getSrcData() {
        if (srcData == null) {
            srcData = new HighlightData();
        }

        return srcData;
    }

    /**
     * Gets position
     *
     * @return Position
     */
    public int getPos() {
        return pos;
    }
    
    /**
     * Sets position
     * @param pos Position
     */
    public void setPos(int pos) {
        this.pos = pos;
    }

    /**
     * Gets needed sources
     *
     * @return Needed sources
     */
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

    /**
     * Converts this to string semicoloned.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringSemicoloned(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
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

    /**
     * Checks if semicolon is needed
     *
     * @return True if semicolon is needed
     */
    public boolean needsSemicolon() {
        return true;
    }

    /**
     * Converts this to string as Boolean.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringBoolean(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "Boolean");
    }

    /**
     * Converts this to string as String.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringString(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "String");
    }

    /**
     * Converts this to string as int.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringInt(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "int");
    }

    /**
     * Converts this to string as Number.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringNumber(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "Number");
    }

    /**
     * Converts this to string.
     *
     * @return String
     */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Converts this to string.
     *
     * @param writer Writer
     * @param localData Local data
     * @param implicitCoerce Implicit coerce
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData, String implicitCoerce) throws InterruptedException {
        if (CancellableWorker.isInterrupted()) {
            throw new InterruptedException();
        }

        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendTry(writer, localData, implicitCoerce);
        writer.endOffset();
        return writer;
    }

    /**
     * Converts this to string.
     *
     * @param writer Writer
     * @param localData Local data
     * @return String
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toString(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData, "");
    }

    /**
     * Converts this to string.
     *
     * @param localData Local data
     * @return String
     * @throws InterruptedException On interrupt
     */
    public String toString(LocalData localData) throws InterruptedException {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        toString(writer, localData);
        writer.finishHilights();
        return writer.toString();
    }

    /**
     * Append this to a writer.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public abstract GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException;

    /**
     * Append this to a writer.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter appendTry(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return appendTry(writer, localData, "");
    }

    /**
     * Append this to a writer.
     *
     * @param writer Writer
     * @param localData Local data
     * @param implicitCoerce Implicit coerce
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter appendTry(GraphTextWriter writer, LocalData localData, String implicitCoerce) throws InterruptedException {
        GraphTargetItem t = this;
        if (!implicitCoerce.isEmpty()) {    //if implicit coerce equals explicit
            /*if (t instanceof ConvertAVM2Item) {
                if (implicitCoerce.equals((((ConvertAVM2Item) t).type.toString()))) {
                    t = t.value;
                }
            }*/
            if (localData.abc != null) { //its AS3
                List<String> numberTypes = Arrays.asList("int", "uint", "Number");
                String returnTypeStr = t.returnType().toString();

                //To avoid Error: Implicit coercion of a value of type XXX to an unrelated type YYY
                if (!t.returnType().equals(TypeItem.UNBOUNDED)
                        && !implicitCoerce.equals(returnTypeStr)
                        && !(numberTypes.contains(implicitCoerce) && numberTypes.contains(returnTypeStr))
                        && !(implicitCoerce.equals("Boolean") && !returnTypeStr.equals("Function"))) {
                    t = new ConvertAVM2Item(null, null, t, new TypeItem(implicitCoerce));
                }
            }
        }
        if (!implicitCoerce.isEmpty() && Configuration.simplifyExpressions.get()) {
            t = t.simplify(implicitCoerce);
        }
        return t.appendTo(writer, localData);

    }

    /**
     * Gets precedence.
     *
     * @return Precedence
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Checks if this can be evaluated statically.
     *
     * @return True if this can be evaluated statically
     */
    public boolean isCompileTime() {
        Set<GraphTargetItem> dependencies = new HashSet<>();
        if (!((this instanceof SimpleValue) && ((SimpleValue) this).isSimpleValue())) {
            dependencies.add(this);
        }
        return isCompileTime(dependencies);
    }

    /**
     * Checks if this can be evaluated statically.
     *
     * @param dependencies Dependencies
     * @return True if this can be evaluated statically
     */
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false;
    }

    /**
     * Checks if this can be evaluated statically.
     *
     * @param dependencies Dependencies
     * @return True if this can be evaluated statically
     */
    public boolean isConvertedCompileTime(Set<GraphTargetItem> dependencies) {
        return isCompileTime();
    }

    /**
     * Checks whether this has side effects. For example function call has side
     * effect, but variable access does not.
     *
     * @return True if this has side effects
     */
    public boolean hasSideEffect() {
        Reference<Boolean> ref = new Reference<>(false);
        visitRecursively(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item.hasSideEffect()) {
                    ref.setVal(Boolean.TRUE);
                }
                return true;
            }
        });
        return ref.getVal();
    }

    /**
     * Checks whether it is computed via variables.
     *
     * @return True if it is computed via variables
     */
    public boolean isVariableComputed() {
        return false;
    }

    /**
     * Computes EcmaScript result.
     *
     * @return EcmaScript result
     */
    public Object getResult() {
        return null;
    }

    /**
     * Computes EcmaScript result as number.
     *
     * @return EcmaScript result as number
     */
    public Double getResultAsNumber() {
        return EcmaScript.toNumberAs2(getResult());
    }

    /**
     * Computes EcmaScript result as string.
     *
     * @return EcmaScript result as string
     */
    public String getResultAsString() {
        return EcmaScript.toString(getResult());
    }

    /**
     * Computes EcmaScript result as boolean.
     *
     * @return EcmaScript result as boolean
     */
    public Boolean getResultAsBoolean() {
        return EcmaScript.toBoolean(getResult());
    }

    /**
     * Converts this to string without quotes.
     *
     * @param localData Local data
     * @return String
     */
    public String toStringNoQuotes(LocalData localData) {
        try {
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            toStringNoQuotes(writer, localData);
            writer.finishHilights();
            return writer.toString();
        } catch (InterruptedException ex) {
            //ignore
        }
        return "";
    }

    /**
     * Converts this to string without quotes.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringNoQuotes(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendToNoQuotes(writer, localData);
        writer.endOffset();
        return writer;
    }

    /**
     * Appends this to a writer without quotes.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter appendToNoQuotes(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return toString(writer, localData);
    }

    /**
     * Gets this without coercion.
     *
     * @return This without coercion
     */
    public GraphTargetItem getNotCoerced() {
        return this;
    }

    /**
     * Gets this without coercion and without duplicates.
     *
     * @return This without coercion and without duplicates
     */
    public GraphTargetItem getNotCoercedNoDup() {
        return getNotCoerced();
    }

    /**
     * Gets this through registers.
     *
     * @return This through registers
     */
    public GraphTargetItem getThroughRegister() {
        return this;
    }

    /**
     * Checks whether this needs a new line.
     *
     * @return True if this needs a new line
     */
    public boolean needsNewLine() {
        return false;
    }

    /**
     * Checks whether this handles new line.
     *
     * @return True if this handles new line
     */
    public boolean handlesNewLine() {
        return false;
    }
    
    /**
     * Checks whether this item needs single newline before and after.
     * @return True if needs
     */
    public boolean hasSingleNewLineAround() {
        return false;
    }

    /**
     * Converts this to string with new line.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toStringNL(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.startOffset(src, getLineStartItem(), getPos(), srcData);
        appendTry(writer, localData);
        if (needsNewLine()) {
            writer.newLine();
        }
        writer.endOffset();
        return writer;
    }

    /**
     * Checks whether this is empty.
     *
     * @return True if this is empty
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Gets through items that cannot be statically computed.
     *
     * @return Through item that cannot be statically computed
     */
    public GraphTargetItem getThroughNotCompilable() {
        return this;
    }

    /**
     * Gets item through duplicates.
     *
     * @return Item through duplicates
     */
    public GraphTargetItem getThroughDuplicate() {
        return this;
    }

    /**
     * Checks whether the value equals.
     *
     * @param target Target
     * @return True if the value equals
     */
    public boolean valueEquals(GraphTargetItem target) {
        return equals(target);
    }

    /**
     * Converts this to source (low level code).
     *
     * @param localData Local data
     * @param generator Source generator
     * @return Source
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return new ArrayList<>();
    }

    /**
     * Converts this to source (low level code) and ignore return value.
     *
     * @param localData Local data
     * @param generator Source generator
     * @return Source
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (!hasReturnValue()) {
            return toSource(localData, generator);
        }
        return generator.generateDiscardValue(localData, this);
    }

    /**
     * Converts this to source (low level code). with BinaryOp
     *
     * @param op Binary operation
     * @param action Action
     * @return Source
     */
    protected List<GraphSourceItem> toSourceBinary(BinaryOp op, GraphSourceItem action) {
        List<GraphSourceItem> ret = new ArrayList<>();

        return ret;
    }

    /**
     * Merges Object list to one list of GraphTargetItems
     *
     * @param localData Local data
     * @param gen Source generator
     * @param tar Objects
     * @return List of GraphTargetItems
     * @throws CompilationException On compilation error
     */
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

    /**
     * Checks whether this has return value.
     *
     * @return True if this has return value
     */
    public abstract boolean hasReturnValue();

    /**
     * Gets all sub items.
     *
     * @return All sub items
     */
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        visit(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item != null) {
                    ret.add(item);
                }
                return true;
            }
        });
        return ret;
    }

    /**
     * Gets all sub items recursively.
     *
     * @return All sub items recursively
     */
    public Set<GraphTargetItem> getAllSubItemsRecursively() {
        Set<GraphTargetItem> ret = new HashSet<>();
        visitRecursively(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                ret.add(item);
                return true;
            }
        });
        return ret;
    }

    /**
     * Visits this recursively.
     *
     * @param visitor Visitor
     */
    public final void visitRecursively(GraphTargetVisitorInterface visitor) {
        Set<GraphTargetItem> visitedItems = new LinkedIdentityHashSet<>();
        visit(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item != null && !visitedItems.contains(item)) {
                    visitedItems.add(item);
                    if (visitor.visit(item)) {
                        item.visit(this);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Visits this recursively without using Blocks.
     *
     * @param visitor Visitor
     */
    public final void visitRecursivelyNoBlock(GraphTargetRecursiveVisitorInterface visitor) {
        Set<GraphTargetItem> visitedItems = new HashSet<>();
        Stack<GraphTargetItem> parentStack = new Stack<>();
        parentStack.add(this);
        visitNoBlock(new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item != null && !visitedItems.contains(item)) {
                    visitedItems.add(item);
                    visitor.visit(item, parentStack);
                    parentStack.push(item);
                    item.visitNoBlock(this);
                    parentStack.pop();
                }
                return true;
            }
        });
    }

    /**
     * Visits this.
     *
     * @param visitor Visitor
     */
    public void visit(GraphTargetVisitorInterface visitor) {
        if (value != null) {
            visitor.visit(value);
        }
    }

    /**
     * Visits this without using Blocks.
     *
     * @param visitor Visitor
     */
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
        visit(visitor);
    }

    /**
     * Gets return type.
     *
     * @return Return type
     */
    public abstract GraphTargetItem returnType();

    /**
     * Clone this.
     *
     * @return Cloned item
     */
    @Override
    public GraphTargetItem clone() {
        try {
            return (GraphTargetItem) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    /**
     * Inverts this item.
     *
     * @param src Source item
     * @return Inverted item
     */
    public GraphTargetItem invert(GraphSourceItem src) {
        return new NotItem(src, getLineStartItem(), this);
    }

    /**
     * Appends this to a writer.
     *
     * @param prevLineItem Previous line item
     * @param writer Writer
     * @param localData Local data
     * @param commands Commands
     * @param asBlock As block
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter appendCommands(GraphTargetItem prevLineItem, GraphTextWriter writer, LocalData localData, List<GraphTargetItem> commands, boolean asBlock) throws InterruptedException {

        //This may be useful in the future, but we must handle obfuscated SWFs where there is only one debugline instruction on the beginning.
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

    /**
     * Append this to a writer as a Block.
     *
     * @param prevLineItem Previous line item
     * @param writer Writer
     * @param localData Local data
     * @param commands Commands
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter appendBlock(GraphTargetItem prevLineItem, GraphTextWriter writer, LocalData localData, List<GraphTargetItem> commands) throws InterruptedException {
        appendCommands(prevLineItem, writer, localData, commands, true);
        return writer;
    }

    /**
     * Gets this as long.
     *
     * @return This as long
     */
    public long getAsLong() {
        if (this instanceof DirectValueActionItem) {
            DirectValueActionItem dvai = (DirectValueActionItem) this;
            return (long) (double) EcmaScript.toNumberAs2(dvai.value);
        }

        return 0;
    }

    /**
     * Checks whether this is identical to other.
     *
     * @param other Other
     * @return True if this is identical to other
     */
    public boolean isIdentical(GraphTargetItem other) {
        return this == other;
    }

    /**
     * Alternative of Objects.equals() for GraphTargetItem.
     *
     * @param o1 Object 1
     * @param o2 Object 2
     * @return True if objects are value equal
     */
    public static boolean objectsValueEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if ((o1 instanceof GraphTargetItem) && (o2 instanceof GraphTargetItem)) {
            GraphTargetItem gt1 = (GraphTargetItem) o1;
            GraphTargetItem gt2 = (GraphTargetItem) o2;
            return gt1.valueEquals(gt2);
        }
        if ((o1 instanceof List) && (o2 instanceof List)) {
            List l1 = (List) o1;
            List l2 = (List) o2;

            if (l1.size() != l2.size()) {
                return false;
            }
            for (int i = 0; i < l1.size(); i++) {
                if (!objectsValueEquals(l1.get(i), l2.get(i))) {
                    return false;
                }
            }
        }
        return o1.equals(o2);
    }
}
