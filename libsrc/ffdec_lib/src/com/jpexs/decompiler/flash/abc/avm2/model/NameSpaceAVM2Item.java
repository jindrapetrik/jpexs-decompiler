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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;

/**
 *
 * @author JPEXS
 */
public class NameSpaceAVM2Item extends AVM2Item {

    public int namespaceIndex;

    public NameSpaceAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int namespaceIndex) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.namespaceIndex = namespaceIndex;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        if (namespaceIndex == 0) {
            return writer.append("*"); //?
        }
        AVM2ConstantPool constants = localData.constantsAvm2;

        DottedChain dc = localData.abc.findCustomNs(namespaceIndex);
        String nsname = dc != null ? dc.getLast() : null;

        if (nsname != null) {
            String identifier = IdentifiersDeobfuscation.printIdentifier(true, nsname);
            if (identifier != null && !identifier.isEmpty()) {
                writer.append(identifier);
                return writer;
            }
        }

        writer.append("new Namespace").spaceBeforeCallParenthesies(1).append("(");
        writer.append("\"").append(Helper.escapeActionScriptString(constants.getNamespace(namespaceIndex).getRawName(constants))).append("\""); //assume not null name        
        writer.append(")");
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Namespace");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
