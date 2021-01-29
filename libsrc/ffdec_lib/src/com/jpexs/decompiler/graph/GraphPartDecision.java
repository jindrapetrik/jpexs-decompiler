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
 * License along with this library. */
package com.jpexs.decompiler.graph;

import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class GraphPartDecision {
    public GraphPart part;
    public int way;

    public GraphPartDecision(GraphPart part, int way) {
        this.part = part;
        this.way = way;
    }

    @Override
    public String toString() {
        return part.toString() + ":" + way;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.part);
        hash = 59 * hash + this.way;
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
        final GraphPartDecision other = (GraphPartDecision) obj;
        if (this.way != other.way) {
            return false;
        }
        if (!Objects.equals(this.part, other.part)) {
            return false;
        }
        return true;
    }

}
