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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * XML.
 *
 * @author JPEXS
 */
public class XMLAVM2Item extends AVM2Item {

    /**
     * Parts of XML.
     */
    public List<GraphTargetItem> parts;

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param parts Parts
     */
    public XMLAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, List<GraphTargetItem> parts) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.parts = parts;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(parts);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        for (int i = 0; i < parts.size(); i++) {
            GraphTargetItem part = parts.get(i);
            GraphTargetItem partBefore = i > 0 ? parts.get(i - 1) : null;
            GraphTargetItem partAfter = i < parts.size() - 1 ? parts.get(i + 1) : null;
            if (part instanceof StringAVM2Item) {
                String s = ((StringAVM2Item) part).getValue();
                if (partAfter instanceof EscapeXAttrAVM2Item) {
                    if (s.endsWith("\"")) {
                        s = s.substring(0, s.length() - 1);
                    }
                }
                if (partBefore instanceof EscapeXAttrAVM2Item) {
                    if (s.startsWith("\"")) {
                        s = s.substring(1);
                    }
                }
                writer.append(s);
            } else if ((part instanceof EscapeXElemAVM2Item) || (part instanceof EscapeXAttrAVM2Item)) {
                part.toString(writer, localData);
            } else {
                writer.append("{");
                part.appendTo(writer, localData);
                writer.append("}");
            }
        }
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.XML);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.parts);
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
        final XMLAVM2Item other = (XMLAVM2Item) obj;
        if (!Objects.equals(this.parts, other.parts)) {
            return false;
        }
        return true;
    }

}
