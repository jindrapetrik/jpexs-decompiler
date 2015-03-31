/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import java.util.ArrayList;
import java.util.List;

public class ScriptInfo {

    @Internal
    public boolean deleted;

    public int init_index; //MethodInfo

    public Traits traits;

    public ScriptInfo() {
        traits = new Traits();
    }

    public ScriptInfo(Traits traits) {
        this.traits = traits;
    }

    public List<ScriptPack> getPacks(ABC abc, int scriptIndex, String packagePrefix) {
        List<ScriptPack> ret = new ArrayList<>();

        List<Integer> otherTraits = new ArrayList<>();
        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            Multiname name = t.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if (!((ns.kind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (ns.kind == Namespace.KIND_PACKAGE))) {
                otherTraits.add(j);
            }
        }
        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            Multiname name = t.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (ns.kind == Namespace.KIND_PACKAGE)) {
                String packageName = ns.getName(abc.constants, false); // assume not null package
                String objectName = name.getName(abc.constants, null, false);
                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(j);
                if (!otherTraits.isEmpty()) {
                    traitIndices.addAll(otherTraits);
                    otherTraits.clear();
                }

                if (packagePrefix == null || packageName.startsWith(packagePrefix)) {
                    ClassPath cp = new ClassPath(packageName, objectName);
                    ret.add(new ScriptPack(cp, abc, scriptIndex, traitIndices));
                }
            }
        }
        return ret;
    }

    public int removeTraps(int scriptIndex, ABC abc, String path) throws InterruptedException {
        return traits.removeTraps(scriptIndex, -1, true, abc, path);
    }

    @Override
    public String toString() {
        return "method_index=" + init_index + "\r\n" + traits.toString();
    }

    public String toString(ABC abc, List<String> fullyQualifiedNames) {
        return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
    }

    public void delete(ABC abc, boolean d) {
        deleted = d;
        abc.method_info.get(init_index).delete(abc, d);
        traits.delete(abc, d);
    }
}
