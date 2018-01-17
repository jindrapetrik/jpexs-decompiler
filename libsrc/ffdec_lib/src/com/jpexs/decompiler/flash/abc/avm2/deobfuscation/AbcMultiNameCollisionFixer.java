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
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.usages.MultinameUsage;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AbcMultiNameCollisionFixer {

    public int fixCollisions(SWF swf) {
        int ret = 0;
        for (ABCContainerTag tag : swf.getAbcList()) {
            ret += this.fixCollisions(tag.getABC());
        }
        return ret;
    }

    public int fixCollisions(ABC abc) {
        Set<MultinameUsage> collidingUsages = abc.getCollidingMultinameUsages();
        Set<Integer> collidingMultinameIndices = new HashSet<>();
        for (MultinameUsage usage : collidingUsages) {
            collidingMultinameIndices.add(usage.getMultinameIndex());
        }
        Set<String> newNames = new HashSet<>();
        int ret = 0;
        for (int multiNameIndex : collidingMultinameIndices) {
            Multiname multiName = abc.constants.getMultiname(multiNameIndex);
            int namespace = multiName.namespace_index;
            String oldName = abc.constants.getString(multiName.name_index);
            String selectedBaseName = oldName + "_" + multiName.namespace_index;
            String selectedName = selectedBaseName;
            int cnt = 0;
            while (!newNames.contains(selectedName) && abc.constants.getStringId(selectedName, false) > -1) { //already exists such name, but was not added during this collisionFixes
                cnt++;
                selectedName = selectedBaseName + "_" + cnt;
            }
            newNames.add(selectedName);
            int newNameIndex = abc.constants.getStringId(selectedName, true);
            multiName.name_index = newNameIndex;

            //Find other names with same name and namespace which are not listed as colliding, but are related
            for (int m = 1; m < abc.constants.getMultinameCount(); m++) {
                if (collidingMultinameIndices.contains(m)) {
                    continue;
                }
                Multiname other = abc.constants.getMultiname(m);
                if (other.hasOwnName() && other.hasOwnNamespace()) {
                    int otherNamespace = other.namespace_index;
                    if (namespace == otherNamespace) {
                        if (Objects.equals(oldName, abc.constants.getString(other.name_index))) {
                            other.name_index = newNameIndex;
                            ret++;
                        }
                    }
                } else if (other.hasOwnName() && other.hasOwnNamespaceSet()) {
                    NamespaceSet otherNamespaceSet = abc.constants.getNamespaceSet(other.namespace_set_index);
                    //NamespaceSet with only one namespace - this one
                    if (otherNamespaceSet.namespaces.length == 1) {
                        int otherNamespace = otherNamespaceSet.namespaces[0];
                        if (namespace == otherNamespace) {
                            if (Objects.equals(oldName, abc.constants.getString(other.name_index))) {
                                other.name_index = newNameIndex;
                                ret++;
                            }
                        }
                    }
                    //What if there are more namespaces in the set and how about runtime resolved names?
                }
            }

            ret++;
        }
        if (ret > 0) {
            ((Tag) abc.parentTag).setModified(true);
        }
        return ret;
    }
}
