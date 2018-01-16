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
 *
 * @author JPEXS
 */
public class ABCMethodIndexing {

    private final ABC abc;

    private Map<MethodInfo, Integer> bodyIdxFromMethod = new HashMap<>();

    public ABCMethodIndexing(ABC abc) {
        this.abc = abc;
        createBodyIdxFromMethodIdxMap(abc);
    }

    public final void createBodyIdxFromMethodIdxMap(ABC abc) {
        List<MethodBody> bodies = abc.bodies;
        Map<MethodInfo, Integer> map = new HashMap<>(bodies.size());
        for (int i = 0; i < bodies.size(); i++) {
            MethodBody mb = bodies.get(i);
            map.put(abc.method_info.get(mb.method_info), i);
        }

        bodyIdxFromMethod = map;
    }

    public int findMethodBodyIndex(MethodInfo methodInfo) {
        Integer bi = bodyIdxFromMethod.get(methodInfo);
        if (bi == null) {
            return -1;
        }

        return bi;
    }

    public int findMethodBodyIndex(int methodInfo) {
        if (methodInfo < 0 || methodInfo >= abc.method_info.size()) {
            return -1;
        }

        MethodInfo mi = abc.method_info.get(methodInfo);
        return findMethodBodyIndex(mi);
    }

    public MethodBody findMethodBody(MethodInfo methodInfo) {
        int bi = findMethodBodyIndex(methodInfo);
        if (bi != -1) {
            return abc.bodies.get(bi);
        }

        return null;
    }

    public MethodBody findMethodBody(int methodInfo) {
        int bi = findMethodBodyIndex(methodInfo);
        if (bi != -1) {
            return abc.bodies.get(bi);
        }

        return null;
    }

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
