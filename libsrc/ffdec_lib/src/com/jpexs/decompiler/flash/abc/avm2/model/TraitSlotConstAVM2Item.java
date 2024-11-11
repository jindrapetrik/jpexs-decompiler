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

import com.jpexs.decompiler.flash.abc.types.AssignedValue;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.TraitType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class TraitSlotConstAVM2Item extends AVM2Item {

    private final TraitSlotConst trait;
    private final GraphTargetItem assignedValue;
    private final boolean isStatic;
    private final int scriptIndex;
    private final int classIndex;
    private final int initializer;
    private final int methodIndex;
    private final int traitIndex;

            
    /**
     *
     * @param instruction
     * @param lineStartIns
     * @param trait
     * @param isStatic
     * @param assignedValue
     * @param classIndex
     * @param scriptIndex
     * @param initializer
     * @param methodIndex
     * @param traitIndex
     */
    public TraitSlotConstAVM2Item(
            GraphSourceItem instruction,
            GraphSourceItem lineStartIns,
            TraitSlotConst trait, 
            GraphTargetItem assignedValue,
            boolean isStatic,
            int scriptIndex,
            int classIndex,
            int initializer,
            int methodIndex,
            int traitIndex
            ) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.trait = trait;
        this.assignedValue = assignedValue;
        this.isStatic = isStatic;
        this.scriptIndex = scriptIndex;
        this.classIndex = classIndex;
        this.initializer = initializer;
        this.methodIndex = methodIndex;
        this.traitIndex = traitIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        
        writer.endMethod();
        writer.endTrait();
        
        int h = traitIndex;
        if (initializer != GraphTextWriter.TRAIT_SCRIPT_INITIALIZER) {
            h = localData.abc.getGlobalTraitId(TraitType.METHOD , isStatic, classIndex, traitIndex);
        }
        writer.startTrait(h);
        ConvertData cd = new ConvertData();
        cd.assignedValues.put(trait, new AssignedValue(null, assignedValue, initializer, methodIndex));
        boolean insideInterface = classIndex > -1 ? localData.abc.instance_info.get(classIndex).isInterface() : false;        
        trait.toString(
                localData.swfVersion,
                localData.abcIndex,
                DottedChain.EMPTY /*??*/,
                null, 
                cd,
                "trait " + trait.getName(localData.abc), 
                localData.abc, 
                isStatic, 
                localData.exportMode, 
                scriptIndex,
                classIndex,
                writer,
                localData.fullyQualifiedNames,
                false,
                insideInterface
        );
        writer.endTrait();
        
        writer.startTrait(initializer);
        writer.startMethod(methodIndex, null);        
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return null;
    }        

    @Override
    public boolean needsSemicolon() {
        return false;
    }    

    @Override
    public boolean hasSingleNewLineAround() {
        return true;
    }            

    @Override
    public boolean handlesNewLine() {
        return true;
    }

    public TraitSlotConst getTrait() {
        return trait;
    }            
}
