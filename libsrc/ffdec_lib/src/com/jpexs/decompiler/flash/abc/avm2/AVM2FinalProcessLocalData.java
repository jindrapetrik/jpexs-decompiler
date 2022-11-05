/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AVM2FinalProcessLocalData extends FinalProcessLocalData {

    public HashMap<Integer, String> localRegNames;

    public AVM2FinalProcessLocalData(List<Loop> loops, HashMap<Integer, String> localRegNames) {
        super(loops);
        this.localRegNames = localRegNames;
    }

}
