/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AVM2 item base class.
 *
 * @author JPEXS
 */
public abstract class AVM2Item extends GraphTargetItem {

    private AVM2Instruction instruction;

    private AVM2Instruction lineStartIns;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param precedence Precedence
     */
    public AVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence) {
        this(instruction, lineStartIns, precedence, null);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param precedence Precedence
     * @param value Value
     */
    public AVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence, GraphTargetItem value) {
        super(AVM2GraphTargetDialect.INSTANCE, instruction, lineStartIns, precedence, value);
        if (instruction instanceof AVM2Instruction) {
            this.instruction = (AVM2Instruction) instruction;
        }
        if (lineStartIns instanceof AVM2Instruction) {
            this.lineStartIns = (AVM2Instruction) lineStartIns;
        }
    }

    /**
     * Gets instruction.
     *
     * @return Instruction
     */
    public AVM2Instruction getInstruction() {
        return instruction;
    }

    /**
     * Gets line start instruction.
     *
     * @return Line start instruction
     */
    public AVM2Instruction getLineStartIns() {
        return lineStartIns;
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    /**
     * Gets how to qualify a property of the class found through the scope
     * chain (findproperty) when a local variable of the same name would
     * shadow it in ActionScript. Haxe assigns fields so, as in
     * findproperty key; getlocal1; initproperty key for this.key = key.
     *
     * @param localData Local data
     * @param propertyName Property name
     * @return "this", the class name, or null when it does not need to be
     * qualified
     */
    private static String getShadowedTraitQualifier(LocalData localData, GraphTargetItem propertyName) {
        if (!(propertyName instanceof FullMultinameAVM2Item) || localData.localRegNames == null || localData.abc == null
                || localData.callStack == null || localData.callStack.isEmpty()) {
            return null;
        }
        MethodBody body = localData.callStack.get(localData.callStack.size() - 1);
        //in an activation, the scope chain finds the local variable itself
        if (body == null || localData.abc.method_info.get(body.method_info).flagNeed_activation()) {
            return null;
        }
        int multinameIndex = ((FullMultinameAVM2Item) propertyName).multinameIndex;
        if (multinameIndex <= 0 || multinameIndex >= localData.constantsAvm2.getMultinameCount()) {
            return null;
        }
        Multiname m = localData.constantsAvm2.getMultiname(multinameIndex);
        if (m.name_index <= 0) {
            return null;
        }
        String name = localData.constantsAvm2.getString(m.name_index);
        if (!localData.localRegNames.containsValue(name)) {
            return null;
        }
        ABC abc = localData.abc;
        if (isInitializer(abc, body.method_info)) {
            //Flex names registers of the static code like the static vars it sets
            return null;
        }
        int classIndex = localData.classIndex > -1 ? localData.classIndex : findMethodClass(abc, body.method_info);
        if (classIndex < 0) {
            return null;
        }
        if (hasTraitNamed(abc, abc.instance_info.get(classIndex).instance_traits, name)) {
            return "this";
        }
        if (hasTraitNamed(abc, abc.class_info.get(classIndex).static_traits, name)) {
            return abc.constants.getString(abc.instance_info.get(classIndex).getName(abc.constants).name_index);
        }
        return null;
    }

    /**
     * Checks whether a method is a class or script initializer.
     *
     * @param abc ABC
     * @param methodInfo Method info index
     * @return True if it is
     */
    private static boolean isInitializer(ABC abc, int methodInfo) {
        for (int i = 0; i < abc.class_info.size(); i++) {
            if (abc.class_info.get(i).cinit_index == methodInfo) {
                return true;
            }
        }
        for (int i = 0; i < abc.script_info.size(); i++) {
            if (abc.script_info.get(i).init_index == methodInfo) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the class whose initializers or methods include a method.
     *
     * @param abc ABC
     * @param methodInfo Method info index
     * @return Class index or -1
     */
    private static int findMethodClass(ABC abc, int methodInfo) {
        for (int i = 0; i < abc.instance_info.size(); i++) {
            if (abc.instance_info.get(i).iinit_index == methodInfo
                    || hasMethod(abc.instance_info.get(i).instance_traits, methodInfo)
                    || hasMethod(abc.class_info.get(i).static_traits, methodInfo)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasMethod(Traits traits, int methodInfo) {
        for (Trait t : traits.traits) {
            if ((t instanceof TraitMethodGetterSetter) && ((TraitMethodGetterSetter) t).method_info == methodInfo) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTraitNamed(ABC abc, Traits traits, String name) {
        for (Trait t : traits.traits) {
            Multiname tm = t.getName(abc);
            if (tm != null && tm.name_index > 0 && name.equals(abc.constants.getString(tm.name_index))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Formats property.
     * @param writer Writer
     * @param object Object
     * @param propertyName Property name
     * @param localData Local data
     * @param isStatic Is static
     * @param nullCondition Null condition - ?.
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    protected GraphTextWriter formatProperty(GraphTextWriter writer, GraphTargetItem object, GraphTargetItem propertyName, LocalData localData, boolean isStatic, boolean nullCondition) throws InterruptedException {
        boolean empty = object.getThroughDuplicate() instanceof FindPropertyAVM2Item;
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    empty = true;
                }
            }
        }

        if (object.getThroughDuplicate() instanceof FindPropertyAVM2Item) {
            //TODO: this might not be a good idea - #595,
            //but removing this fails testNames test
            FindPropertyAVM2Item fp = (FindPropertyAVM2Item) object.getThroughDuplicate();
            if (fp.propertyName instanceof FullMultinameAVM2Item) {
                
                if (propertyName instanceof FullMultinameAVM2Item) {
                    if (!((FullMultinameAVM2Item) propertyName).property) {
                        ((FullMultinameAVM2Item) fp.propertyName).property = false; // for constructprop
                    }
                }
                propertyName = fp.propertyName;
                                
            }
        }

        if (!empty && object != null) {
            if (object.getPrecedence() > PRECEDENCE_PRIMARY || (object instanceof IntegerValueAVM2Item)) {
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
            TypeItem parentType = null;
            if (localData.classIndex > -1) {
                DottedChain thisClass = localData.abc.instance_info.get(localData.classIndex).getName(localData.constantsAvm2).getNameWithNamespace(localData.usedDeobfuscations, localData.abc, localData.constantsAvm2, false);
                parentType = new TypeItem(thisClass);
            }
            String qualifier = getShadowedTraitQualifier(localData, propertyName);
            if (qualifier != null) {
                writer.append(qualifier).append(".");
            }
            ((FullMultinameAVM2Item) propertyName).appendTo(writer, localData, false, parentType, isStatic);
            return writer;
        }

        if (propertyName instanceof FullMultinameAVM2Item) {

            HighlightData data = new HighlightData();
            int multinameIndex = ((FullMultinameAVM2Item) propertyName).multinameIndex;
            int namespaceIndex = localData.constantsAvm2.getMultiname(multinameIndex).namespace_index;
            GraphTargetItem returnType = object.returnType();
            if (returnType instanceof ApplyTypeAVM2Item) {
                ApplyTypeAVM2Item ati = (ApplyTypeAVM2Item) returnType;
                data.propertyType = ati.object.toString();
                data.propertySubType = ati.params.get(0).toString();
            } else {
                data.propertyType = returnType.toString();
            }
            data.namespaceIndex = namespaceIndex;
            data.isStatic = isStatic;
            
            String operator = nullCondition ? "?." : ".";
            
            String localName = "";   
            boolean isAttribute = false;
            boolean isValidName = false;
            String namespaceSuffix = "";
            
            if (multinameIndex >= 0  && multinameIndex < localData.constantsAvm2.getMultinameCount()) {
                Reference<DottedChain> customNsRef = new Reference<>(null);
                isAttribute = localData.constantsAvm2.getMultiname(multinameIndex).isAttribute();
                localName = localData.constantsAvm2.getMultiname(multinameIndex).getNameAndCustomNamespace(new HashSet<>(), localData.abc, new ArrayList<>(), true, true, customNsRef);
                namespaceSuffix = localData.constantsAvm2.getMultiname(multinameIndex).getNamespaceSuffix();
                
                if ("*".equals(localName)) {
                    isValidName = true;
                }
                if (isAttribute) {
                    isValidName = true;
                }
                if (!"".equals(namespaceSuffix)) {
                    isValidName = true;
                }
                if (IdentifiersDeobfuscation.isValidName(true, localName)) {
                    isValidName = true;
                }                
            } else {
                isValidName = true;
            }                      
            
            if (!Configuration.as3QNameObfuscatedPropsInSquareBrackets.get()) {
                isValidName = true;
            }

            FullMultinameAVM2Item multiname = (FullMultinameAVM2Item) propertyName;
            if (isAttribute && multiname.namespace != null) {
                // E4X namespace qualifiers cannot contain property expressions
                // such as this.xmlNamespace. A QName keeps the runtime namespace
                // and also works for attribute assignment and deletion.
                writer.append(operator).append("@[new QName(");
                multiname.namespace.toString(writer, localData);
                writer.append(",");
                if (multiname.name != null) {
                    multiname.name.toString(writer, localData);
                } else {
                    int nameIndex = localData.constantsAvm2.getMultiname(multinameIndex).name_index;
                    new StringAVM2Item(null, null, nameIndex == 0 ? "*" : localData.constantsAvm2.getString(nameIndex)).toString(writer, localData);
                }
                return writer.append(")]");
            }
            
            if (isValidName) {
                if (((FullMultinameAVM2Item) propertyName).name != null) {
                    //x[name] needs no dot, but x.ns::[name] and x.@[name] do
                    if (((FullMultinameAVM2Item) propertyName).namespace != null || isAttribute) {
                        writer.allowWrapHere().hilightSpecial(operator, HighlightSpecialType.PROPERTY_TYPE, 0, data);
                    }                
                } else {
                    writer.allowWrapHere().hilightSpecial(operator, HighlightSpecialType.PROPERTY_TYPE, 0, data);                                
                }
            }
            
            ((FullMultinameAVM2Item) propertyName).appendTo(writer, localData, true, returnType, isStatic);
            return writer;
        } else {
            writer.append("[").allowWrapHere();
            propertyName.toString(writer, localData);
            return writer.append("]");
        }
    }

    /**
     * Gets local register name.
     * @param swf SWF
     * @param localRegNames Local register names
     * @param reg Register
     * @param usedDeobfuscations Used deobfuscations
     * @return Local register name
     */
    public static String localRegName(SWF swf, HashMap<Integer, String> localRegNames, int reg, Set<String> usedDeobfuscations) {
        if (localRegNames.containsKey(reg)) {
            return IdentifiersDeobfuscation.printIdentifier(swf, usedDeobfuscations, true, localRegNames.get(reg));
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

    /**
     * Creates instruction.
     * @param instructionCode Instruction code
     * @param operands Operands
     * @return Instruction
     */
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

    /**
     * Gets free register.
     * @param localData Local data
     * @param generator Generator
     * @return Free register
     */
    public static int getFreeRegister(SourceGeneratorLocalData localData, SourceGenerator generator) {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        return g.getFreeRegister(localData);
    }

    /**
     * Kills register.
     * @param localData Local data
     * @param generator Generator
     * @param regNumber Register number
     */
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

    /**
     * Whether target must stay intact. A special case.
     * @param target Target
     * @return Whether target must stay intact
     */
    public static boolean mustStayIntact1(GraphTargetItem target) {
        target = target.getNotCoerced();
        if (target instanceof ExceptionAVM2Item) {
            return true;
        }
        return false;
    }

    /**
     * Whether target must stay intact. Version 2. A special case.
     * @param target Target
     * @return Whether target must stay intact. Version 2.
     */
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
