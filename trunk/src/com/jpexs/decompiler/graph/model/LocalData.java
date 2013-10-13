/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.action.model.ConstantPool;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class LocalData {

    public ConstantPool constants;
    public com.jpexs.decompiler.flash.abc.avm2.ConstantPool constantsAvm2;
    public HashMap<Integer, String> localRegNames;
    public List<String> fullyQualifiedNames;
 
    public static LocalData create(ConstantPool constants) {
        LocalData localData = new LocalData();
        localData.constants = constants;
        return localData;
    }

    public static LocalData create(com.jpexs.decompiler.flash.abc.avm2.ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        LocalData localData = new LocalData();
        localData.constantsAvm2 = constants;
        localData.localRegNames = localRegNames;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        return localData;
    }
}
