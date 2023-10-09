/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import natorder.NaturalOrderComparator;

/**
 *
 * @author JPEXS
 */
public class AS3Package extends AS3ClassTreeItem {

    private final Openable openable;

    public String packageName;

    @SuppressWarnings("unchecked")
    private final Map<String, AS3Package> subPackages = new TreeMap<>(new NaturalOrderComparator());

    @SuppressWarnings("unchecked")
    private final Map<String, ScriptPack> scripts = new TreeMap<>(new NaturalOrderComparator());

    private List<AS3Package> sortedPackages;

    private List<ScriptPack> sortedScripts;

    private boolean flat;

    private boolean defaultPackage;

    private Integer compoundScriptIndex;

    private ABC abc;

    private ScriptPack compoundInitializerPack = null;

    private boolean partOfCompoundScript;

    public AS3Package(String packageName, Openable openable, boolean flat, boolean defaultPackage, ABC abc, boolean partOfCompoundScript, Integer compoundScriptIndex) {
        super(packageName, "", null);
        this.flat = flat;
        this.openable = openable;
        this.packageName = packageName;
        this.defaultPackage = defaultPackage;
        this.compoundScriptIndex = compoundScriptIndex;
        this.abc = abc;
        this.partOfCompoundScript = partOfCompoundScript;
    }

    public boolean isPartOfCompoundScript() {
        return partOfCompoundScript;
    }

    public void setCompoundInitializerPack(ScriptPack compoundInitializerPack) {
        this.compoundInitializerPack = compoundInitializerPack;
    }

    public ScriptPack getCompoundInitializerPack() {
        return compoundInitializerPack;
    }

    public boolean isDefaultPackage() {
        return defaultPackage;
    }

    public boolean isFlat() {
        return flat;
    }

    public boolean isCompoundScript() {
        return compoundScriptIndex != null;
    }

    public Integer getCompoundScriptIndex() {
        return compoundScriptIndex;
    }

    public ABC getAbc() {
        return abc;
    }

    @Override
    public Openable getOpenable() {
        return openable;
    }

    public List<AS3Package> getSubPackages() {
        if (sortedPackages == null) {
            List<AS3Package> list = new ArrayList<>();
            for (AS3Package subPackage : subPackages.values()) {
                list.add(subPackage);
            }

            sortedPackages = list;
        }

        return sortedPackages;
    }

    public List<ScriptPack> getScriptPacks() {
        if (sortedScripts == null) {
            List<ScriptPack> list = new ArrayList<>();
            for (ScriptPack script : scripts.values()) {
                list.add(script);
            }

            sortedScripts = list;
        }

        return sortedScripts;
    }

    public void addScriptPack(ScriptPack script) {
        ClassPath cp = script.getClassPath();
        scripts.put(cp.className + cp.namespaceSuffix, script);
        sortedScripts = null;
    }

    public void addSubPackage(AS3Package subPackage) {
        subPackages.put(subPackage.getNameWithNamespaceSuffix(), subPackage);
        sortedPackages = null;
    }

    public AS3Package getSubPackage(String packageName) {
        return subPackages.get(packageName);
    }

    public List<AS3ClassTreeItem> getAllChildren() {
        List<AS3ClassTreeItem> result = new ArrayList<>(getChildCount());
        result.addAll(subPackages.values());
        result.addAll(getScriptPacks());
        return result;
    }

    public AS3ClassTreeItem getChild(int index) {
        if (index < subPackages.size()) {
            return getSubPackages().get(index);
        }

        index -= subPackages.size();
        if (index < getScriptPacks().size()) {
            return getScriptPacks().get(index);
        }
        return null;
    }

    public int getChildCount() {
        return subPackages.size() + getScriptPacks().size();
    }

    public int getIndexOfChild(AS3ClassTreeItem child) {
        int res = 0;
        if (child instanceof AS3Package) {
            for (AS3Package pkg : subPackages.values()) {
                if (pkg.packageName.equals(((AS3Package) child).packageName)) {
                    break;
                }
                res++;
            }
            return res;
        }

        res = subPackages.size();
        for (ScriptPack pack : scripts.values()) {
            if (pack.equals(child)) {
                break;
            }
            res++;
        }

        return res;
    }

    public void clear() {
        subPackages.clear();
        scripts.clear();
        sortedPackages = null;
        sortedScripts = null;
    }

    @Override
    public String toString() {
        if (flat) {
            return packageName;
        }
        return IdentifiersDeobfuscation.printIdentifier(true, packageName);
    }

    @Override
    public boolean isModified() {
        List<ScriptPack> sps = getScriptPacks();
        for (ScriptPack sp : sps) {
            if (sp.isModified()) {
                return true;
            }
        }
        List<AS3Package> ps = getSubPackages();
        for (AS3Package p : ps) {
            if (p.isModified()) {
                return true;
            }
        }
        return false;
    }
}
