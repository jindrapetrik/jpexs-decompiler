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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ScriptInfo {

    @Internal
    private boolean modified = true;

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }

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

    public List<ScriptPack> getPacks(ABC abc, int scriptIndex, String packagePrefix, List<ABC> allAbcs) {
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
                DottedChain packageName = ns.getName(abc.constants); // assume not null package
                String objectName = name.getName(abc.constants, null, true, false);
                String namespaceSuffix = name.getNamespaceSuffix();
                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(j);
                if (!otherTraits.isEmpty()) {
                    traitIndices.addAll(otherTraits);
                    otherTraits.clear();
                }

                if (packagePrefix == null || packageName.toPrintableString(true).startsWith(packagePrefix)) {
                    ClassPath cp = new ClassPath(packageName, objectName, namespaceSuffix);
                    ret.add(new ScriptPack(cp, abc, allAbcs, scriptIndex, traitIndices));
                }
            }
        }
        if (ret.size() == 1) {
            ret.get(0).isSimple = true;
        }
        if (ret.isEmpty() && !otherTraits.isEmpty()) { //no public/package internal traits to determine common pack name
            //make each trait separate pack
            for (int traitIndex : otherTraits) {
                Trait t = traits.traits.get(traitIndex);
                Multiname name = t.getName(abc);
                Namespace ns = name.getNamespace(abc.constants);

                DottedChain packageName = ns.getName(abc.constants);
                String objectName = name.getName(abc.constants, null, true, false);
                String namespaceSuffix = name.getNamespaceSuffix();

                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(traitIndex);
                ClassPath cp = new ClassPath(packageName, objectName, namespaceSuffix);
                ret.add(new ScriptPack(cp, abc, allAbcs, scriptIndex, traitIndices));
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

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
    }

    public void delete(ABC abc, boolean d) {
        deleted = d;
        abc.method_info.get(init_index).delete(abc, d);
        traits.delete(abc, d);
    }
}
