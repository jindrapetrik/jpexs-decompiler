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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
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

/**
 *
 * @author JPEXS
 */
public abstract class AVM2Item extends GraphTargetItem {

    private AVM2Instruction instruction;

    private AVM2Instruction lineStartIns;

    public AVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence) {
        this(instruction, lineStartIns, precedence, null);
    }

    public AVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence, GraphTargetItem value) {
        super(instruction, lineStartIns, precedence, value);
        if (instruction instanceof AVM2Instruction) {
            this.instruction = (AVM2Instruction) instruction;
        }
        if (lineStartIns instanceof AVM2Instruction) {
            this.lineStartIns = (AVM2Instruction) lineStartIns;
        }
    }

    public AVM2Instruction getInstruction() {
        return instruction;
    }

    public AVM2Instruction getLineStartIns() {
        return lineStartIns;
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    protected GraphTextWriter formatProperty(GraphTextWriter writer, GraphTargetItem object, GraphTargetItem propertyName, LocalData localData) throws InterruptedException {
        boolean empty = object.getThroughDuplicate() instanceof FindPropertyAVM2Item;
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    empty = true;
                }
            }
        }

        if (object.getThroughDuplicate() instanceof FindPropertyAVM2Item) {
            FindPropertyAVM2Item fp = (FindPropertyAVM2Item) object.getThroughDuplicate();
            if (fp.propertyName instanceof FullMultinameAVM2Item) {
                propertyName = fp.propertyName;
            }
        }

        if (!empty && object != null) {
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
                if (((FullMultinameAVM2Item) propertyName).namespace != null) {
                    writer.append(".");
                }
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
        ret.add(new AVM2Instruction(0, AVM2Instructions.Pop, null));
        return ret;
    }

    public static AVM2Instruction ins(int instructionCode, Integer... operands) {
        InstructionDefinition def = AVM2Code.instructionSet[instructionCode];
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

    @Override
    public boolean isIdentical(GraphTargetItem other) {
        GraphTargetItem tiName = this;
        while (tiName instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName).computedValue != null) {
                tiName = ((LocalRegAVM2Item) tiName).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }

        GraphTargetItem tiName2 = other;
        if (tiName2 != null) {
            tiName2 = tiName2.getThroughDuplicate();
        }
        while (tiName2 instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName2).computedValue != null) {
                tiName2 = ((LocalRegAVM2Item) tiName2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }
        if (tiName != tiName2) {
            return false;
        }
        return true;
    }

    public static boolean mustStayIntact1(GraphTargetItem target) {
        target = target.getNotCoerced();
        if (target instanceof ExceptionAVM2Item) {
            return true;
        }
        return false;
    }

    public static boolean mustStayIntact2(GraphTargetItem target) {
        target = target.getNotCoerced();
        if (target instanceof NextValueAVM2Item) {
            return true;
        }
        if (target instanceof NextNameAVM2Item) {
            return true;
        }
        return false;
    }
}
