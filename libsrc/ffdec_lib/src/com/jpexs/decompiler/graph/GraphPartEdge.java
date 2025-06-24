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
package com.jpexs.decompiler.graph;

import java.util.Objects;

/**
 * Edge of a graph. Represents a connection between two GraphParts.
 *
 * @author JPEXS
 */
public class GraphPartEdge {

    /**
     * From part
     */
    public GraphPart from;

    /**
     * To part
     */
    public GraphPart to;

    /**
     * Constructs a new edge
     *
     * @param from From
     * @param to To
     */
    public GraphPartEdge(GraphPart from, GraphPart to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Hash code
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.from);
        hash = 79 * hash + Objects.hashCode(this.to);
        return hash;
    }

    /**
     * Equals
     *
     * @param obj Object to compare
     * @return True if equals
     */
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
        final GraphPartEdge other = (GraphPartEdge) obj;
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        if (!Objects.equals(this.to, other.to)) {
            return false;
        }
        return true;
    }

    /**
     * To string
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return from.toString() + " -> " + to.toString();
    }

}
