/*
 *  Copyright (C) 2012-2014 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class DeclarationAVM2Item extends AVM2Item {

    public GraphTargetItem assignment;
    public GraphTargetItem type;

    public DeclarationAVM2Item(GraphTargetItem assignment, GraphTargetItem type) {
        super(assignment.src, assignment.getPrecedence());
        this.type = type;
        this.assignment = assignment;
    }

    public DeclarationAVM2Item(GraphTargetItem assignment) {
        this(assignment, null);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (assignment instanceof SetLocalAVM2Item) {            
            SetLocalAVM2Item lti = (SetLocalAVM2Item) assignment;
            srcData.put("regIndex",""+lti.regIndex);
            srcData.put("declaration", "true");
            GraphTargetItem coerType = TypeItem.UNBOUNDED;
            if (lti.value instanceof CoerceAVM2Item) {
                coerType = ((CoerceAVM2Item) lti.value).typeObj;
            }
            if (lti.value instanceof ConvertAVM2Item) {
                coerType = ((ConvertAVM2Item) lti.value).type;
            }
            writer.append("var ");
            writer.append(localRegName(localData.localRegNames, lti.regIndex));
            writer.append(":");
            coerType.appendTo(writer, localData);
            writer.append(" = ");
            return lti.value.toString(writer, localData);
        }
        if (assignment instanceof SetSlotAVM2Item) {
            SetSlotAVM2Item ssti = (SetSlotAVM2Item) assignment;
            srcData.put("slotName",""+ssti.getNameAsStr(localData));
            srcData.put("declaration", "true");
            writer.append("var ");
            ssti.getName(writer, localData);
            writer.append(":");
            type.appendTo(writer, localData);
            writer.append(" = ");
            return ssti.value.toString(writer, localData);
        }
        writer.append("var ");
        return assignment.toString(writer, localData);
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
