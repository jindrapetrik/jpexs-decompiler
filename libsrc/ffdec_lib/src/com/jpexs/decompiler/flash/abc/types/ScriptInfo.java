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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.graph.DottedChain;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Script info in ABC file.
 *
 * @author JPEXS
 */
public class ScriptInfo {

    /**
     * Modified flag
     */
    @Internal
    private boolean modified = true;

    /**
     * Deleted flag
     */
    @Internal
    public boolean deleted;

    /**
     * Script initializer method index
     */
    public int init_index;

    /**
     * Traits
     */
    public Traits traits;

    /**
     * Script packs cache
     */
    private List<ScriptPack> cachedPacks;

    /**
     * Constructs new script info
     */
    public ScriptInfo() {
        traits = new Traits();
    }

    /**
     * Constructs new script info.
     *
     * @param traits Traits
     */
    public ScriptInfo(Traits traits) {
        this.traits = traits;
    }

    /**
     * Clears packs cache.
     */
    public void clearPacksCache() {
        cachedPacks = null;
    }

    /**
     * Set modified flag.
     *
     * @param modified Modified flag
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Check if script is modified.
     *
     * @return True if script is modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Gets simple pack name. Finds the first public trait package name. If
     * there is no public trait or there are more than one public traits,
     * returns null.
     *
     * @param abc ABC file
     * @param usedDeobfuscations Used deobfuscations
     * @return Simple pack name - Can be null!
     */
    public DottedChain getSimplePackName(ABC abc, Set<String> usedDeobfuscations) {
        List<Integer> packageTraits = new ArrayList<>();

        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            if (t.name_index >= abc.constants.getMultinameCount()) {
                continue;
            }
            Multiname name = t.getName(abc);
            int nskind = name.getSimpleNamespaceKind(abc.constants);
            if ((nskind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (nskind == Namespace.KIND_PACKAGE)) {
                packageTraits.add(j);
            }
        }
        if (packageTraits.isEmpty() || packageTraits.size() > 1) {
            return null;
        }
        return traits.traits.get(packageTraits.get(0)).getName(abc).getNameWithNamespace(usedDeobfuscations, abc, abc.constants, true);
    }

    /**
     * Gets script packs.
     *
     * @param abc ABC file
     * @param scriptIndex Script index
     * @param packagePrefix Package prefix
     * @param allAbcs All ABC files
     * @return Script packs
     */
    public List<ScriptPack> getPacks(ABC abc, int scriptIndex, String packagePrefix, List<ABC> allAbcs) {
        if (packagePrefix == null && cachedPacks != null) {
            return new ArrayList<>(cachedPacks);
        }
        List<ScriptPack> ret = new ArrayList<>();

        List<Integer> otherTraits = new ArrayList<>();
        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            Multiname name = t.getName(abc);
            int nskind = name.getSimpleNamespaceKind(abc.constants);
            if (!((nskind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (nskind == Namespace.KIND_PACKAGE))) {
                otherTraits.add(j);
            }
        }

        int publicTraitsCount = 0;
        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            Multiname name = t.getName(abc);
            int nskind = name.getSimpleNamespaceKind(abc.constants);
            if ((nskind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (nskind == Namespace.KIND_PACKAGE)) {
                publicTraitsCount++;
            }
        }

        boolean isSimple = publicTraitsCount == 1;

        for (int j = 0; j < traits.traits.size(); j++) {
            Trait t = traits.traits.get(j);
            if (!isSimple && (t instanceof TraitSlotConst)) {
                continue;
            }
            Multiname name = t.getName(abc);
            int nskind = name.getSimpleNamespaceKind(abc.constants);
            if ((nskind == Namespace.KIND_PACKAGE_INTERNAL)
                    || (nskind == Namespace.KIND_PACKAGE)) {
                DottedChain packageName = name.getSimpleNamespaceName(abc.constants); // assume not null package
                String objectName = name.getName(new LinkedHashSet<>() /*???*/, abc, abc.constants, null, true, false);
                String namespaceSuffix = name.getNamespaceSuffix();
                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(j);
                if (!otherTraits.isEmpty()) {
                    traitIndices.addAll(otherTraits);
                    otherTraits.clear();
                }

                if (packagePrefix == null || packageName.toPrintableString(new LinkedHashSet<>(), abc.getSwf(), true).startsWith(packagePrefix)) {

                    ClassPath cp = new ClassPath(packageName, objectName, namespaceSuffix, abc.getSwf());
                    ScriptPack pack = new ScriptPack(cp, abc, allAbcs, scriptIndex, traitIndices);
                    pack.isSimple = isSimple;
                    ret.add(pack);
                }
            }
        }
        if (ret.isEmpty() && !otherTraits.isEmpty()) { //no public/package internal traits to determine common pack name
            //make each trait separate pack
            for (int traitIndex : otherTraits) {
                Trait t = traits.traits.get(traitIndex);
                Multiname name = t.getName(abc);

                DottedChain packageName = name.getSimpleNamespaceName(abc.constants);
                String objectName = name.getName(new LinkedHashSet<>()/*???*/, abc, abc.constants, null, true, false);
                String namespaceSuffix = name.getNamespaceSuffix();

                List<Integer> traitIndices = new ArrayList<>();

                traitIndices.add(traitIndex);
                ClassPath cp = new ClassPath(packageName, objectName, namespaceSuffix, abc.getSwf());
                ret.add(new ScriptPack(cp, abc, allAbcs, scriptIndex, traitIndices));
            }
        }
        if (!isSimple) {
            ret.add(new ScriptPack(new ClassPath(DottedChain.EMPTY, "script_" + scriptIndex, "", abc.getSwf()), abc, allAbcs, scriptIndex, new ArrayList<>()));
        }
        if (packagePrefix == null) {
            cachedPacks = new ArrayList<>(ret);
        }
        return ret;
    }

    /**
     * Removes traps - deobfuscation.
     *
     * @param scriptIndex Script index
     * @param abc ABC file
     * @param path Path
     * @return Number of removed traps
     * @throws InterruptedException Interrupted exception
     */
    public int removeTraps(int scriptIndex, ABC abc, String path) throws InterruptedException {
        return traits.removeTraps(scriptIndex, -1, true, abc, path);
    }

    /**
     * To string.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "method_index=" + init_index + "\r\n" + traits.toString();
    }

    /**
     * To string.
     *
     * @param abc ABC file
     * @param fullyQualifiedNames Fully qualified names
     * @return String representation
     */
    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        return "method_index=" + init_index + "\r\n" + traits.toString(abc, fullyQualifiedNames);
    }

    /**
     * Delete script info.
     *
     * @param abc ABC file
     * @param d Deleted flag
     */
    public void delete(ABC abc, boolean d) {
        deleted = d;
        abc.method_info.get(init_index).delete(abc, d);
        traits.delete(abc, d);
        if (d) {
            clearPacksCache();
        }
    }
}
