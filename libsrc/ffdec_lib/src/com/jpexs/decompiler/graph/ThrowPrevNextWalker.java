/*
 * Copyright (C) 2018 JPEXS, All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Prev-Next walker which also use try/catch blocks.
 * @author JPEXS
 */
public class ThrowPrevNextWalker implements PrevNextWalker {
    private final Map<GraphPart, List<GraphPart>> throwMap = new LinkedHashMap<>();
    private final Map<GraphPart, List<GraphPart>> reverseThrowMap = new LinkedHashMap<>();

    
    
    public ThrowPrevNextWalker(List<ThrowState> throwStates) {        
        for (ThrowState ts : throwStates) {
            for (GraphPart t : ts.throwingParts) {
                if (!throwMap.containsKey(t)) {
                    throwMap.put(t, new ArrayList<>());
                }
                throwMap.get(t).add(ts.targetPart);                                
            }
            
            if (!reverseThrowMap.containsKey(ts.targetPart)) {
                reverseThrowMap.put(ts.targetPart, new ArrayList<>());
            }
            reverseThrowMap.get(ts.targetPart).addAll(ts.throwingParts);
        }
    }    
    
    @Override
    public List<? extends GraphPart> getPrev(GraphPart node) {
        List<GraphPart> ret = new ArrayList<>();
        ret.addAll(node.refs);
        if (reverseThrowMap.containsKey(node)) {
            ret.addAll(reverseThrowMap.get(node));
        }
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i).start < 0) {
                ret.remove(i);
            }
        }
        return ret;
    }

    @Override
    public List<? extends GraphPart> getNext(GraphPart node) {
        List<GraphPart> ret = new ArrayList<>();
        ret.addAll(node.nextParts);
        if (throwMap.containsKey(node)) {
            ret.addAll(throwMap.get(node));
        }
        return ret;
    }

}
