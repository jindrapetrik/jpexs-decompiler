/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class ThrowState {

    public int exceptionId;
    public int state;

    public Set<GraphPart> throwingParts = new HashSet<>();
    public GraphPart targetPart;
    public GraphPart startPart;
    public Set<GraphPart> catchParts = new HashSet<>();

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.exceptionId;
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
        final ThrowState other = (ThrowState) obj;
        if (this.exceptionId != other.exceptionId) {
            return false;
        }
        return true;
    }
}
