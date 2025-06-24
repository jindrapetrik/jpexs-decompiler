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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ABC method indexing.
 *
 * @author JPEXS
 */
public class ABCMethodIndexing {

    /**
     * ABC
     */
    private final ABC abc;

    /**
     * Method body index for method info
     */
    private Map<MethodInfo, Integer> bodyIdxFromMethod = new HashMap<>();

    /**
     * Constructs ABC method indexing.
     *
     * @param abc ABC
     */
    public ABCMethodIndexing(ABC abc) {
        this.abc = abc;
        createBodyIdxFromMethodIdxMap(abc);
    }

    /**
     * Creates body index from method index map.
     *
     * @param abc ABC
     */
    public final void createBodyIdxFromMethodIdxMap(ABC abc) {
        List<MethodBody> bodies = abc.bodies;
        Map<MethodInfo, Integer> map = new HashMap<>(bodies.size());
        for (int i = 0; i < bodies.size(); i++) {
            MethodBody mb = bodies.get(i);
            map.put(abc.method_info.get(mb.method_info), i);
        }

        bodyIdxFromMethod = map;
    }

    /**
     * Finds method body index for method info.
     *
     * @param methodInfo Method info
     * @return Method body index or -1 if not found
     */
    public int findMethodBodyIndex(MethodInfo methodInfo) {
        Integer bi = bodyIdxFromMethod.get(methodInfo);
        if (bi == null) {
            return -1;
        }

        return bi;
    }

    /**
     * Finds method body index for method info.
     *
     * @param methodInfo Method info index
     * @return Method body index or -1 if not found
     */
    public int findMethodBodyIndex(int methodInfo) {
        if (methodInfo < 0 || methodInfo >= abc.method_info.size()) {
            return -1;
        }

        MethodInfo mi = abc.method_info.get(methodInfo);
        return findMethodBodyIndex(mi);
    }

    /**
     * Finds method body for method info.
     *
     * @param methodInfo Method info
     * @return Method body or null if not found
     */
    public MethodBody findMethodBody(MethodInfo methodInfo) {
        int bi = findMethodBodyIndex(methodInfo);
        if (bi != -1) {
            return abc.bodies.get(bi);
        }

        return null;
    }

    /**
     * Finds method body for method info.
     *
     * @param methodInfo Method info index
     * @return Method body or null if not found
     */
    public MethodBody findMethodBody(int methodInfo) {
        int bi = findMethodBodyIndex(methodInfo);
        if (bi != -1) {
            return abc.bodies.get(bi);
        }

        return null;
    }

    /**
     * Finds method traits.
     *
     * @param pack Script pack
     * @param bodyIndex Method body index
     * @return Method traits
     */
    public List<Trait> findMethodTraits(ScriptPack pack, int bodyIndex) {
        int methodInfo = abc.bodies.get(bodyIndex).method_info;
        List<Trait> traits = abc.script_info.get(pack.scriptIndex).traits.traits;
        List<Trait> resultTraits = new ArrayList<>();
        for (int ti : pack.traitIndices) {
            Trait t = traits.get(ti);
            findTraits(abc, t, methodInfo, resultTraits);
        }

        return resultTraits;
    }

    /**
     * Finds traits.
     *
     * @param abc ABC
     * @param trait Trait
     * @param methodInfo Method info index
     * @param result Result list
     */
    private static void findTraits(ABC abc, Trait trait, int methodInfo, List<Trait> result) {
        if (trait instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) trait;
        } else if (trait instanceof TraitFunction) {
            TraitFunction tf = (TraitFunction) trait;
            if (tf.method_info == methodInfo) {
                result.add(trait);
            }
        } else if (trait instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) trait;
            if (tmgs.method_info == methodInfo) {
                result.add(trait);
            }
        } else if (trait instanceof TraitClass) {
            TraitClass tc = (TraitClass) trait;
            InstanceInfo instanceInfo = abc.instance_info.get(tc.class_info);
            for (Trait t : instanceInfo.instance_traits.traits) {
                findTraits(abc, t, methodInfo, result);
            }

            ClassInfo classInfo = abc.class_info.get(tc.class_info);
            for (Trait t : classInfo.static_traits.traits) {
                findTraits(abc, t, methodInfo, result);
            }
        }
    }
}
