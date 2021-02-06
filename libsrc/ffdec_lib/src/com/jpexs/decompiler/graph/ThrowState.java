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
}
