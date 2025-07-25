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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.Objects;
import java.util.Set;

/**
 * Find and get property.
 *
 * @author JPEXS
 */
public class GetLexAVM2Item extends AVM2Item {

    /**
     * Property name
     */
    public Multiname propertyName;

    /**
     * Type
     */
    public GraphTargetItem type;

    /**
     * Call type
     */
    public GraphTargetItem callType;

    /**
     * Is static
     */
    public boolean isStatic;

    /**
     * Full property name
     */
    private final DottedChain fullPropertyName;

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param propertyName Property name
     * @param abc ABC
     * @param constants Constants
     * @param type Type
     * @param callType Call type
     * @param isStatic Is static
     */
    public GetLexAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Multiname propertyName, ABC abc, AVM2ConstantPool constants, GraphTargetItem type, GraphTargetItem callType, boolean isStatic, Set<String> usedDeobfuscations) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.propertyName = propertyName;
        this.type = type;
        this.callType = callType;
        this.fullPropertyName = propertyName.getNameWithNamespace(usedDeobfuscations, abc, constants, true);
        this.isStatic = isStatic;
    }

    /**
     * Gets the raw property name.
     * @return Raw property name
     */
    public String getRawPropertyName() {
        return fullPropertyName.toRawString();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        Reference<DottedChain> customNsRef = new Reference<>(null);
        String localName = propertyName.getNameAndCustomNamespace(localData.usedDeobfuscations, localData.abc, localData.fullyQualifiedNames, false, true, customNsRef);
        DottedChain customNs = customNsRef.getVal();
        if (customNs != null) {
            String nsname = customNs.getLast();
            String identifier = IdentifiersDeobfuscation.printIdentifier(localData.abc.getSwf(), localData.usedDeobfuscations, true, nsname);                    
            writer.hilightSpecial(identifier, HighlightSpecialType.TYPE_NAME, customNs.toRawString());
            writer.appendNoHilight("::");
            getSrcData().localName = nsname + "::" + localName;            
            return writer.append(localName);
        }
        
        
        getSrcData().localName = localName;
        return writer.append(localName);
    }

    @Override
    public GraphTargetItem returnType() {
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.propertyName);
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
        final GetLexAVM2Item other = (GetLexAVM2Item) obj;
        if (!Objects.equals(this.propertyName, other.propertyName)) {
            return false;
        }
        return true;
    }

}
