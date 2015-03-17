/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AVM2Item extends GraphTargetItem {

    public AVM2Instruction instruction;

    public boolean hidden = false;

    public AVM2Item(GraphSourceItem instruction, int precedence) {
        super(instruction, precedence);
        if (instruction instanceof AVM2Instruction) {
            this.instruction = (AVM2Instruction) instruction;
        }
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    protected GraphTextWriter formatProperty(GraphTextWriter writer, GraphTargetItem object, GraphTargetItem propertyName, LocalData localData) throws InterruptedException {
        boolean empty = object instanceof FindPropertyAVM2Item;
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    empty = true;
                }
            }
        }

        if (object instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item fp = (FindPropertyAVM2Item) object;
            if (fp.propertyName instanceof FullMultinameAVM2Item) {
                propertyName = fp.propertyName;
            }
        }

        if (!empty) {
            if (object.getPrecedence() > PRECEDENCE_PRIMARY) {
                writer.append("(");
                object.toString(writer, localData);
                writer.append(")");
                empty = false;
            } else {
                int writerLength = writer.getLength();
                object.toString(writer, localData);
                if (writerLength == writer.getLength()) {
                    empty = true;
                }
            }
        }

        if (empty) {
            return propertyName.toString(writer, localData);
        }
        if (propertyName instanceof FullMultinameAVM2Item) {
            if (((FullMultinameAVM2Item) propertyName).name != null) {
                return propertyName.toString(writer, localData);
            } else {
                writer.append(".");
                return propertyName.toString(writer, localData);
            }
        } else {
            writer.append("[");
            propertyName.toString(writer, localData);
            return writer.append("]");
        }
    }

    public static String localRegName(HashMap<Integer, String> localRegNames, int reg) {
        if (localRegNames.containsKey(reg)) {
            return IdentifiersDeobfuscation.printIdentifier(true, localRegNames.get(reg));
        } else {
            if (reg == 0) {
                return "this";
            }
            return String.format(Configuration.registerNameFormat.get(), reg);
        }
    }

    /*@Override
     public boolean hasReturnValue() {
     return false;
     }*/
    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (!hasReturnValue()) {
            return toSource(localData, generator);
        }
        List<GraphSourceItem> ret = toSource(localData, generator);
        ret.add(new AVM2Instruction(0, new PopIns(), null));
        return ret;
    }

    public static AVM2Instruction ins(InstructionDefinition def, Integer... operands) {
        List<Integer> ops = new ArrayList<>();
        for (Integer o : operands) {
            if (o != null) {
                ops.add(o);
            }
        }
        int[] opArr = new int[ops.size()];
        for (int i = 0; i < ops.size(); i++) {
            opArr[i] = ops.get(i);
        }

        return new AVM2Instruction(0, def, opArr);
    }

    public static int getFreeRegister(SourceGeneratorLocalData localData, SourceGenerator generator) {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        return g.getFreeRegister(localData);
    }

    public static void killRegister(SourceGeneratorLocalData localData, SourceGenerator generator, int regNumber) {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        g.killRegister(localData, regNumber);
    }
}
