/*
 *  Copyright (C) 2012-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.model.clauses;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class DeclarationAVM2Item extends AVM2Item {

    public GraphTargetItem assignment;
    public String type;

    public DeclarationAVM2Item(GraphTargetItem assignment, String type) {
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
            String type = "*";
            if (lti.value instanceof CoerceAVM2Item) {
                type = ((CoerceAVM2Item) lti.value).type;
            }
            if (lti.value instanceof ConvertAVM2Item) {
                type = ((ConvertAVM2Item) lti.value).type;
            }
            writer.append("var ");
            writer.append(localRegName(localData.localRegNames, lti.regIndex) + ":" + type + " = ");
            return lti.value.toString(writer, localData);
        }
        if (assignment instanceof SetSlotAVM2Item) {
            SetSlotAVM2Item ssti = (SetSlotAVM2Item) assignment;
            writer.append("var ");
            ssti.getName(writer, localData);
            writer.append(":");
            writer.append(type);
            writer.append(" = ");
            return ssti.value.toString(writer, localData);
        }
        writer.append("var ");
        return assignment.toString(writer, localData);
    }
}
