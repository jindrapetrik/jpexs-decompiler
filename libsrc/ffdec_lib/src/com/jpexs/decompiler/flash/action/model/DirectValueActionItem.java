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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class DirectValueActionItem extends ActionItem implements SimpleValue {

    public Object value;

    public final List<String> constants;

    public GraphTargetItem computedRegValue;

    public final int pos;

    public DirectValueActionItem(Object o) {
        this(null, null, 0, o, new ArrayList<>());
    }

    public DirectValueActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int instructionPos, Object value, List<String> constants) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.constants = constants;
        this.value = value;
        this.pos = instructionPos;
    }

    @Override
    protected int getPos() {
        return pos;
    }

    @Override
    public boolean isVariableComputed() {
        return (computedRegValue != null);
    }

    @Override
    public Object getResult() {
        if (computedRegValue != null) {
            return computedRegValue.getResult();
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Float) {
            return (double) (Float) value;
        }
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof String) {
            return value;
        }
        if (value instanceof ConstantIndex) {
            return (this.constants.get(((ConstantIndex) value).index));
        }
        if (value instanceof RegisterNumber) {
            return Undefined.INSTANCE; //has not computed value
        }
        return value;
    }

    @Override
    public boolean isSimpleValue() {
        return !(value instanceof RegisterNumber);
    }

    @Override
    public String toStringNoQuotes(LocalData localData) {
        if (value instanceof Double) {
            if (Double.compare((double) (Double) value, 0) == 0) {
                return "0";
            }
        }
        if (value instanceof Float) {
            if (Float.compare((float) (Float) value, 0) == 0) {
                return "0";
            }
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof ConstantIndex) {
            return this.constants.get(((ConstantIndex) value).index);
        }
        return value.toString();
    }

    @Override
    public GraphTextWriter appendToNoQuotes(GraphTextWriter writer, LocalData localData) {
        if (value instanceof Double) {
            if (Double.compare((double) (Double) value, 0) == 0) {
                return writer.append("0");
            }
        }
        if (value instanceof Float) {
            if (Float.compare((float) (Float) value, 0) == 0) {
                return writer.append("0");
            }
        }
        if (value instanceof String) {
            return writer.append((String) value);
        }
        if (value instanceof ConstantIndex) {
            return writer.append(this.constants.get(((ConstantIndex) value).index));
        }
        return writer.append(value.toString());
    }

    public String toStringNoH(ConstantPool constants) {
        if (value instanceof ConstantIndex) {
            return this.constants.get(((ConstantIndex) value).index);
        }

        return EcmaScript.toString(value);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        if (value instanceof Double) {
            if (Double.compare((double) (Double) value, 0) == 0) {
                return writer.append("0");
            }
        }
        if (value instanceof Float) {
            if (Float.compare((float) (Float) value, 0) == 0) {
                return writer.append("0");
            }
        }

        if (value instanceof String) {
            return writer.append("\"").append(Helper.escapeActionScriptString((String) value)).append("\"");
        }
        if (value instanceof ConstantIndex) {
            return writer.append("\"").append(Helper.escapeActionScriptString(this.constants.get(((ConstantIndex) value).index))).append("\"");
        }
        if (value instanceof RegisterNumber) {

            HighlightData srcData = getSrcData();
            srcData.localName = ((RegisterNumber) value).translate();
            srcData.regIndex = ((RegisterNumber) value).number;

            return writer.appendWithData(((RegisterNumber) value).translate(), srcData);
        }
        //return writer.append(value.toString());
        return writer.append(EcmaScript.toString(value, true)); // todo, use this line
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (computedRegValue != null) {
            if (dependencies.contains(computedRegValue)) {
                return false;
            }
            dependencies.add(computedRegValue);
        }
        return (value instanceof Double) || (value instanceof Float) || (value instanceof Boolean) || (value instanceof Long) || (value == Null.INSTANCE) || (computedRegValue != null && computedRegValue.isCompileTime(dependencies)) || (value instanceof String) || (value instanceof ConstantIndex);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(value);
        hash = 71 * hash + Objects.hashCode(constants);
        hash = 71 * hash + pos;
        return hash;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DirectValueActionItem)) {
            return false;
        }
        final DirectValueActionItem other = (DirectValueActionItem) obj;
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        if (!Objects.equals(constants, other.constants)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DirectValueActionItem other = (DirectValueActionItem) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.constants, other.constants)) {
            return false;
        }
        if (other.pos != this.pos) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, new ActionPush(value));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    public boolean isString() {
        return (value instanceof String) || (value instanceof ConstantIndex);
    }

    public String getAsString() {
        if (!isString()) {
            return null;
        }
        return (String) getResult();
    }

    @Override
    public String toString() {
        return "" + getResult();
    }
}
