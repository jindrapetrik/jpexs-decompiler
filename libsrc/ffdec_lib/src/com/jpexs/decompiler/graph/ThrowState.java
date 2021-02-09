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
