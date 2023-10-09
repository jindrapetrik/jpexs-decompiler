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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.graph.Loop;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class AVM2FinalProcessLocalData extends FinalProcessLocalData {

    public HashMap<Integer, String> localRegNames;
    public Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new HashMap<>();

    public AVM2FinalProcessLocalData(List<Loop> loops, HashMap<Integer, String> localRegNames, Map<Integer, Set<Integer>> setLocalPosToGetLocalPos) {
        super(loops);
        this.localRegNames = localRegNames;
        this.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;
    }

    public Set<Integer> getSetLocalUsages(int setLocalPos) {
        if (setLocalPosToGetLocalPos == null) {
            return new HashSet<>();
        }
        if (!setLocalPosToGetLocalPos.containsKey(setLocalPos)) {
            return new HashSet<>();
        }
        return setLocalPosToGetLocalPos.get(setLocalPos);
    }

}
