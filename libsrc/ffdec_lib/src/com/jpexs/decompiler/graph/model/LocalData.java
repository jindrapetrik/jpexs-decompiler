/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class LocalData {

    public static LocalData empty = new LocalData();

    public ConstantPool constants;

    public AVM2ConstantPool constantsAvm2;

    public HashMap<Integer, String> localRegNames;

    public List<DottedChain> fullyQualifiedNames;

    public static LocalData create(ConstantPool constants) {
        LocalData localData = new LocalData();
        localData.constants = constants;
        return localData;
    }

    public static LocalData create(AVM2ConstantPool constants, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames) {
        LocalData localData = new LocalData();
        localData.constantsAvm2 = constants;
        localData.localRegNames = localRegNames;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        return localData;
    }
}
