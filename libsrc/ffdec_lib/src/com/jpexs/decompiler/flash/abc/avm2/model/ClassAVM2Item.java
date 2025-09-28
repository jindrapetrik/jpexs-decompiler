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
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 * Class.
 *
 * @author JPEXS
 */
public class ClassAVM2Item extends AVM2Item {

    /**
     * Class name
     */
    public Multiname className;

    /**
     * Class name as string
     */
    public DottedChain classNameAsStr;

    /**
     * Constructor.
     *
     * @param className Class name
     */
    public ClassAVM2Item(Multiname className) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.className = className;
    }

    /**
     * Constructor.
     *
     * @param className Class name as string
     */
    public ClassAVM2Item(DottedChain className) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.classNameAsStr = className;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        if (classNameAsStr != null) {
            if (localData.fullyQualifiedNames != null && localData.fullyQualifiedNames.contains(classNameAsStr)) {
                return writer.append(classNameAsStr.toPrintableString(localData.usedDeobfuscations, localData.swf, true));
            }
            return writer.append(IdentifiersDeobfuscation.printIdentifier(localData.abc.getSwf(), localData.usedDeobfuscations, true, classNameAsStr.getLast()));
        }
        return writer.append(className.getName(localData.usedDeobfuscations, localData.abc, localData.constantsAvm2, localData.fullyQualifiedNames, false, true));
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

}
