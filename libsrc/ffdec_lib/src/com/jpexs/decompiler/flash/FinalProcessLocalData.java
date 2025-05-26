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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.graph.Loop;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Final decompilation processing local d ata.
 *
 * @author JPEXS
 */
public class FinalProcessLocalData {

    /**
     * Temporary registers used in the code.
     */
    public final HashSet<Integer> temporaryRegisters;

    /**
     * Loops in the code.
     */
    public final List<Loop> loops;

    /**
     * Register usage. Map of setLocal ip to set of getLocal ips.
     */
    public Map<Integer, Set<Integer>> registerUsage;

    /**
     * Returns register usage for given setLocal ip.
     *
     * @param setLocalIp SetLocal ip
     * @return Set of getLocal ips
     */
    public Set<Integer> getRegisterUsage(int setLocalIp) {
        if (registerUsage == null) {
            return new HashSet<>();
        }
        if (!registerUsage.containsKey(setLocalIp)) {
            return new HashSet<>();
        }
        return registerUsage.get(setLocalIp);
    }

    /**
     * Constructs new FinalProcessLocalData.
     *
     * @param loops Loops in the code
     */
    public FinalProcessLocalData(List<Loop> loops) {
        temporaryRegisters = new HashSet<>();
        registerUsage = new HashMap<>();
        this.loops = loops;
    }
}
