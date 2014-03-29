/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class AssignableAVM2Item extends AVM2Item{

    protected GraphTargetItem assignedValue;
    
    public AssignableAVM2Item() {
        this(null);
    }
    
    public AssignableAVM2Item(GraphTargetItem storeValue) {
        super(null, PRECEDENCE_PRIMARY);
        this.assignedValue = storeValue;
    }
    public abstract List<GraphSourceItem> toSourcePreChange(SourceGeneratorLocalData localData, SourceGenerator generator, List<GraphSourceItem> change);
    public abstract List<GraphSourceItem> toSourcePostChange(SourceGeneratorLocalData localData, SourceGenerator generator, List<GraphSourceItem> change);

    public GraphTargetItem getAssignedValue(){
        return assignedValue;
    }
    
    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

}
