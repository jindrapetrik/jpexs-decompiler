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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class ExtendsActionItem extends ActionItem {

    public GraphTargetItem subclass;

    public GraphTargetItem superclass;

    public ExtendsActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem subclass, GraphTargetItem superclass) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.subclass = subclass;
        this.superclass = superclass;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        subclass.toString(writer, localData);
        writer.append(" extends ");
        return stripQuotes(superclass, localData, writer);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(subclass.getNeededSources());
        ret.addAll(superclass.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.subclass);
        hash = 29 * hash + Objects.hashCode(this.superclass);
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
        final ExtendsActionItem other = (ExtendsActionItem) obj;
        if (!Objects.equals(this.subclass, other.subclass)) {
            return false;
        }
        if (!Objects.equals(this.superclass, other.superclass)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExtendsActionItem other = (ExtendsActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.subclass, other.subclass)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.superclass, other.superclass)) {
            return false;
        }
        return true;
    }

}
