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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.IntegerValueTypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Integer value.
 *
 * @author JPEXS
 */
public class IntegerValueAVM2Item extends NumberValueAVM2Item implements IntegerValueTypeItem {

    /**
     * Value
     */
    public Integer value;

    /**
     * Formatted
     */
    private String formatted;

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     */
    public IntegerValueAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Integer value) {
        super(instruction, lineStartIns);
        this.value = value;
        detectFormat();
    }

    /**
     * Should be called after value change
     */
    public void detectFormat() {
        precedence = PRECEDENCE_PRIMARY;

        if (Configuration.smartNumberFormatting.get()) {
            if (value >= 256 && value <= 0xffffffffL) {
                int intVal = (int) (long) (value & 0xffffff);
                int r = (intVal >> 16) & 0xff;
                int g = (intVal >> 8) & 0xff;
                int b = intVal & 0xff;
                if ((r == b && b == g) // gray
                        || (((intVal & 0x0f0000) == 0 || (intVal & 0x0f0000) == 0x0f0000) // a(0/F)b(0/F)c(0/F)
                        && ((intVal & 0x000f00) == 0 || (intVal & 0x000f00) == 0x000f00)
                        && ((intVal & 0x00000f) == 0 || (intVal & 0x00000f) == 0x00000f))
                        || ((((intVal & 0xf00000) >> 20) == ((intVal & 0x0f0000) >> 16)) // aabbcc
                        && (((intVal & 0x00f000) >> 12) == ((intVal & 0x000f00) >> 8))
                        && (((intVal & 0x0000f0) >> 4) == ((intVal & 0x00000f))))) {
                    this.formatted = "0x" + Long.toHexString(value);
                    return;
                }
            }

            long value2 = (long) value;
            if (value2 > 0 && value2 % 60 == 0) {
                int thousandCount = 0;
                value2 /= 60;
                boolean isHour = false;
                boolean isDay = false;
                if (value2 % 60 == 0) {
                    value2 /= 60;
                    isHour = true;
                    if (value2 % 24 == 0) {
                        value2 /= 24;
                        isDay = true;
                    }
                }

                // check milli, micro and nanoseconds
                while (thousandCount < 3) {
                    if (value2 % 1000 == 0) {
                        thousandCount++;
                        value2 /= 1000;
                    } else {
                        break;
                    }
                }

                if (value2 < 1000) {
                    List<Integer> factors = new ArrayList<>();
                    if (value2 > 1) {
                        factors.add((int) value2);
                    }
                    if (isDay) {
                        factors.add(24);
                    }
                    if (isHour) {
                        factors.add(60);
                    }
                    factors.add(60);
                    for (int i = 0; i < thousandCount; i++) {
                        factors.add(1000);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < factors.size(); i++) {
                        if (i != 0) {
                            sb.append(" * ");
                        }
                        sb.append(factors.get(i));
                    }
                    precedence = PRECEDENCE_MULTIPLICATIVE;

                    this.formatted = sb.toString();
                    return;
                }
            }
        }
        this.formatted = EcmaScript.toString(value);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append(formatted);
    }

    @Override
    public Object getResult() {
        return value; //(Double) (double) (long) value;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2Instruction ins;
        if (value >= -128 && value <= 127) {
            ins = new AVM2Instruction(0, AVM2Instructions.PushByte, new int[]{(int) (long) value});
        } else if (value >= -32768 && value <= 32767) {
            ins = new AVM2Instruction(0, AVM2Instructions.PushShort, new int[]{((int) (long) value)});
        } else {
            ins = new AVM2Instruction(0, AVM2Instructions.PushInt, new int[]{((AVM2SourceGenerator) generator).abcIndex.getSelectedAbc().constants.getIntId(value, true)});
        }

        return toSourceMerge(localData, generator, ins);
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.INT;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int intValue() {
        return (int) (long) value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntegerValueAVM2Item other = (IntegerValueAVM2Item) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }
    
    @Override
    public long getAsLong() {
        return (long) value;
    }
}
