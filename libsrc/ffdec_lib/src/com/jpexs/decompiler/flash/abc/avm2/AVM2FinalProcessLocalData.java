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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.helpers.LinkedIdentityHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AVM2 final process local data.
 *
 * @author JPEXS
 */
public class AVM2FinalProcessLocalData extends FinalProcessLocalData {

    /**
     * Local register names - register number to name mapping.
     */
    public HashMap<Integer, String> localRegNames;

    /**
     * Set local position to get local position mapping.
     */
    public Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new HashMap<>();
    
    /**
     * Bottom set locals
     */
    public LinkedIdentityHashSet<SetLocalAVM2Item> bottomSetLocals = new LinkedIdentityHashSet<>();

    /**
     * Constructs AVM2 final process local data.
     *
     * @param loops List of loops
     * @param localRegNames Local register names - register number to name
     * mapping
     * @param setLocalPosToGetLocalPos Set local position to get local position
     * mapping
     * @param bottomSetLocals Bottom set locals
     */
    public AVM2FinalProcessLocalData(List<Loop> loops, HashMap<Integer, String> localRegNames, Map<Integer, Set<Integer>> setLocalPosToGetLocalPos, LinkedIdentityHashSet<SetLocalAVM2Item> bottomSetLocals) {
        super(loops);
        this.localRegNames = localRegNames;
        this.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;
        this.bottomSetLocals = bottomSetLocals;
    }

    /**
     * Gets getlocal positions for setlocal position.
     *
     * @param setLocalPos Setlocal position
     * @return Set of getlocal positions
     */
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
