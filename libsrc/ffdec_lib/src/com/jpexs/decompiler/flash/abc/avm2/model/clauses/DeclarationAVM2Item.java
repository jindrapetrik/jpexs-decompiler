/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model.clauses;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 * Declaration.
 *
 * @author JPEXS
 */
public class DeclarationAVM2Item extends AVM2Item {

    /**
     * Assignment
     */
    public GraphTargetItem assignment;

    /**
     * Type
     */
    public GraphTargetItem type;

    /**
     * Type is null
     */
    public boolean typeIsNull = false;

    /**
     * Show value
     */
    public boolean showValue = true;

    /**
     * Constructor.
     *
     * @param assignment Assignment
     * @param type Type
     */
    public DeclarationAVM2Item(GraphTargetItem assignment, GraphTargetItem type) {
        super(assignment.getSrc(), assignment.getLineStartItem(), assignment.getPrecedence());
        this.type = type;
        this.assignment = assignment;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(type);
        visitor.visit(assignment);
    }

    /**
     * Constructor.
     *
     * @param assignment Assignment
     */
    public DeclarationAVM2Item(GraphTargetItem assignment) {
        this(assignment, null);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {

        if (assignment instanceof LocalRegAVM2Item) { //for..in
            LocalRegAVM2Item lti = (LocalRegAVM2Item) assignment;
            String localName = localRegName(localData.localRegNames, lti.regIndex);
            HighlightData srcData = getSrcData();
            srcData.localName = localName;
            srcData.declaration = true;
            srcData.regIndex = lti.regIndex;
            srcData.declaredType = DottedChain.ALL;
            writer.append("var ");
            writer.append(localName);
            return writer;
        }

        if (assignment instanceof GetSlotAVM2Item) { //for..in
            GetSlotAVM2Item sti = (GetSlotAVM2Item) assignment;
            HighlightData srcData = getSrcData();
            srcData.localName = sti.getNameAsStr(localData);
            srcData.declaration = true;
            srcData.declaredType = DottedChain.ALL;
            writer.append("var ");
            sti.getName(writer, localData);
            return writer;
        }

        if (assignment instanceof SetLocalAVM2Item) {
            SetLocalAVM2Item lti = (SetLocalAVM2Item) assignment;
            String localName = localRegName(localData.localRegNames, lti.regIndex);
            HighlightData srcData = getSrcData();
            srcData.localName = localName;
            srcData.declaration = true;
            srcData.regIndex = lti.regIndex;

            GraphTargetItem val = lti.value;
            srcData.declaredType = (type instanceof TypeItem) ? ((TypeItem) type).fullTypeName : DottedChain.ALL;
            writer.append("var ");
            writer.append(localName);
            writer.append(":");
            type.appendTry(writer, localData);
            if (showValue) {
                writer.append(" = ");
                SetTypeIns.handleNumberToInt(val, type).toString(writer, localData);
            }
            return writer;
        }
        if (assignment instanceof SetSlotAVM2Item) {
            SetSlotAVM2Item ssti = (SetSlotAVM2Item) assignment;
            HighlightData srcData = getSrcData();
            srcData.localName = ssti.getNameAsStr(localData);
            srcData.declaration = true;

            GraphTargetItem val = ssti.value;
            srcData.declaredType = (type instanceof TypeItem) ? ((TypeItem) type).fullTypeName : DottedChain.ALL;
            writer.append("var ");
            ssti.getName(writer, localData);
            writer.append(":");

            type.appendTry(writer, localData);
            if (showValue) {
                writer.append(" = ");
                SetTypeIns.handleNumberToInt(val, type).toString(writer, localData);
            }
            return writer;
        }

        if (assignment instanceof SetPropertyAVM2Item) {
            SetPropertyAVM2Item spti = (SetPropertyAVM2Item) assignment;
            HighlightData srcData = getSrcData();
            srcData.localName = ((FullMultinameAVM2Item) spti.propertyName).resolvedMultinameName;
            srcData.declaration = true;

            GraphTargetItem val = spti.value;
            srcData.declaredType = (type instanceof TypeItem) ? ((TypeItem) type).fullTypeName : DottedChain.ALL;
            writer.append("var ");
            writer.append(IdentifiersDeobfuscation.printIdentifier(true, ((FullMultinameAVM2Item) spti.propertyName).resolvedMultinameName));
            writer.append(":");

            type.appendTry(writer, localData);
            if (showValue) {
                writer.append(" = ");
                SetTypeIns.handleNumberToInt(val, type).toString(writer, localData);
            }
            return writer;
        }

        writer.append("var ");
        assignment.toString(writer, localData);
        writer.append(":");

        type.appendTry(writer, localData);
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
