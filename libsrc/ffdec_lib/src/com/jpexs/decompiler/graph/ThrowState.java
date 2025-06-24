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

import java.util.HashSet;
import java.util.Set;

/**
 * State of throwing exception in a method.
 *
 * @author JPEXS
 */
public class ThrowState {

    /**
     * Exception id
     */
    public int exceptionId;

    /**
     * Throw state
     */
    public int state;

    /**
     * Throwing parts
     */
    public Set<GraphPart> throwingParts = new HashSet<>();

    /**
     * Target part
     */
    public GraphPart targetPart;

    /**
     * Start part
     */
    public GraphPart startPart;

    /**
     * Catch parts
     */
    public Set<GraphPart> catchParts = new HashSet<>();

    /**
     * Hash code
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.exceptionId;
        return hash;
    }

    /**
     * Equals
     *
     * @param obj Object
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
        final ThrowState other = (ThrowState) obj;
        if (this.exceptionId != other.exceptionId) {
            return false;
        }
        return true;
    }
}
