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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class GetLexAVM2Item extends AVM2Item {

    public Multiname propertyName;

    private final DottedChain fullPropertyName;

    public GetLexAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Multiname propertyName, AVM2ConstantPool constants) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.propertyName = propertyName;
        this.fullPropertyName = propertyName.getNameWithNamespace(constants, true);
    }

    public String getRawPropertyName() {
        return fullPropertyName.toRawString();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        String localName = propertyName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true);
        getSrcData().localName = localName;
        return writer.append(propertyName.getName(localData.constantsAvm2, localData.fullyQualifiedNames, false, true));
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
