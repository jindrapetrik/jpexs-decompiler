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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class TypeItem extends GraphTargetItem {

    public static TypeItem BOOLEAN = new TypeItem("Boolean");

    public static TypeItem STRING = new TypeItem("String");

    public static TypeItem ARRAY = new TypeItem("Array");

    public static UnboundedTypeItem UNBOUNDED = new UnboundedTypeItem();

    public DottedChain fullTypeName;

    public TypeItem(String s) {
        this(s == null ? new DottedChain() : new DottedChain(s.split("\\.")));
    }

    public TypeItem(DottedChain fullTypeName) {
        this(fullTypeName, new ArrayList<GraphTargetItem>());
    }

    public TypeItem(DottedChain fullTypeName, List<GraphTargetItem> subtypes) {
        super(null, NOPRECEDENCE);
        this.fullTypeName = fullTypeName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.fullTypeName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeItem other = (TypeItem) obj;
        if (!Objects.equals(this.fullTypeName, other.fullTypeName)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (localData.fullyQualifiedNames.contains(fullTypeName)) {
            writer.hilightSpecial(IdentifiersDeobfuscation.printNamespace(localData.constantsAvm2 != null, fullTypeName.toPrintableString()), HighlightSpecialType.TYPE_NAME, fullTypeName.toPrintableString());
        } else {
            writer.hilightSpecial(IdentifiersDeobfuscation.printIdentifier(localData.constantsAvm2 != null, fullTypeName.getLast()), HighlightSpecialType.TYPE_NAME, fullTypeName.toPrintableString());
        }

        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return this;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public String toString() {
        return fullTypeName.toString();
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }
}
