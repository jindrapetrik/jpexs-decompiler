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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import natorder.NaturalOrderComparator;

/**
 * ActionScript 3 package AS3ClassTreeItem
 *
 * @author JPEXS
 */
public class AS3Package extends AS3ClassTreeItem {

    /**
     * Openable
     */
    private final Openable openable;

    /**
     * Package name
     */
    public String packageName;

    
    private final String DEFAULT_PACKAGE_NAME = AppResources.translate("package.default");
    
    /**
     * All subPackages
     */
    @SuppressWarnings("unchecked")
    private final Map<String, AS3Package> subPackages = new TreeMap<>(new Comparator<String>() {
            NaturalOrderComparator noc = new NaturalOrderComparator();
            
            
            @Override
            public int compare(String o1, String o2) {
                if (Objects.equals(o1, o2)) {
                    return 0;
                }
                if (DEFAULT_PACKAGE_NAME.equals(o1)) {
                    return -1;
                }
                if (DEFAULT_PACKAGE_NAME.equals(o2)) {
                    return 1;
                }                
                return noc.compare(o1, o2);
            }
        }
    );
    
    
    /**
     * All scripts in this package
     */
    @SuppressWarnings("unchecked")
    private final Map<String, ScriptPack> scripts = new TreeMap<>(new NaturalOrderComparator());

    /**
     * Sorted packages
     */
    private List<AS3Package> sortedPackages;

    /**
     * Sorted scripts
     */
    private List<ScriptPack> sortedScripts;

    /**
     * Whether the package is flat = in the format "mypkg.sub1.sub2" instead of
     * "sub1"
     */
    private final boolean flat;

    /**
     * Whether this is default package
     */
    private final boolean defaultPackage;

    /**
     * If this package is compound script package itself, index of scriptInfo
     */
    private final Integer compoundScriptIndex;

    /**
     * ABC
     */
    private final ABC abc;

    /**
     * ScriptPack with compound script initializer
     */
    private ScriptPack compoundInitializerPack = null;

    /**
     * Whether this package is part of compound script
     */
    private final boolean partOfCompoundScript;

    /**
     * Constructor.
     *
     * @param packageName Package name
     * @param openable Openable
     * @param flat Whether the package is flat = in the format "mypkg.sub1.sub2"
     * instead of "sub1"
     * @param defaultPackage Whether this is default package
     * @param abc ABC of this package
     * @param partOfCompoundScript Whether this package is part of compound
     * script
     * @param compoundScriptIndex If this package is compound script package
     * itself, index of scriptInfo
     */
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

    /**
     * Checks whether this package is part of compound script.
     *
     * @return Whether this package is part of compound script
     */
    public boolean isPartOfCompoundScript() {
        return partOfCompoundScript;
    }

    /**
     * Sets ScriptPack with compound script initializer.
     *
     * @param compoundInitializerPack ScriptPack
     */
    public void setCompoundInitializerPack(ScriptPack compoundInitializerPack) {
        this.compoundInitializerPack = compoundInitializerPack;
    }

    /**
     * Gets ScriptPack with compound script initializer.
     *
     * @return ScriptPack
     */
    public ScriptPack getCompoundInitializerPack() {
        return compoundInitializerPack;
    }

    /**
     * Checks whether it is default package.
     *
     * @return Whether it is default package
     */
    public boolean isDefaultPackage() {
        return defaultPackage;
    }

    /**
     * Checks whether the package is flat. Flat = in the format
     * "mypkg.sub1.sub2" instead of "sub1".
     *
     * @return Whether the package is flat
     */
    public boolean isFlat() {
        return flat;
    }

    /**
     * Checks whether it is a compound script. Not just a part of it.
     *
     * @return Whether it is a compound script
     */
    public boolean isCompoundScript() {
        return compoundScriptIndex != null;
    }

    /**
     * Gets index of compound scriptInfo.
     *
     * @return Index of compound scriptInfo
     */
    public Integer getCompoundScriptIndex() {
        return compoundScriptIndex;
    }

    /**
     * Gets ABC.
     *
     * @return ABC
     */
    public ABC getAbc() {
        return abc;
    }

    /**
     * Gets Openable.
     *
     * @return Openable
     */
    @Override
    public Openable getOpenable() {
        return openable;
    }

    /**
     * Gets subpackages.
     *
     * @return Subpackages
     */
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

    /**
     * Gets ScriptPacks in this package.
     *
     * @return ScriptPacks
     */
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

    /**
     * Adds ScriptPack to the package.
     *
     * @param script ScriptPack
     */
    public void addScriptPack(ScriptPack script) {        
        /*ClassPath cp = script.getClassPath();
        scripts.put(cp.className + cp.namespaceSuffix, script);*/
        scripts.put(script.getPrintableNameWithNamespaceSuffix(), script);
        sortedScripts = null;
    }

    /**
     * Adds subpackage.
     *
     * @param subPackage Subpackage
     */
    public void addSubPackage(AS3Package subPackage) {
        subPackages.put(subPackage.getNameWithNamespaceSuffix(), subPackage);
        sortedPackages = null;
    }

    /**
     * Gets subpackage by name.
     *
     * @param packageName Package name
     * @return Subpackage
     */
    public AS3Package getSubPackage(String packageName) {
        /*String printableName;
        if (packageName.equals(DEFAULT_PACKAGE_NAME)) {
            printableName = DEFAULT_PACKAGE_NAME;
        } else {
            printableName = DottedChain.parseNoSuffix(packageName).toPrintableString(new LinkedHashSet<>(), getSwf(), true);
        }*/
        return subPackages.get(packageName);
    }

    /**
     * Gets all subpackages and scripts in this package.
     *
     * @return All subpackages and scripts
     */
    public List<AS3ClassTreeItem> getAllChildren() {
        List<AS3ClassTreeItem> result = new ArrayList<>(getChildCount());
        result.addAll(subPackages.values());
        result.addAll(getScriptPacks());
        return result;
    }

    /**
     * Gets child at index.
     *
     * @param index Index
     * @return Child at index
     */
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

    /**
     * Gets child count.
     *
     * @return Child count
     */
    public int getChildCount() {
        return subPackages.size() + getScriptPacks().size();
    }

    /**
     * Gets index of child.
     *
     * @param child Child
     * @return Index of child
     */
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

    /**
     * Clears subpackages and scripts in this package.
     */
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
        return packageName;
        //return IdentifiersDeobfuscation.printIdentifier(getSwf(), new LinkedHashSet<>(), true, packageName);
    }

    /**
     * Gets modified flag.
     *
     * @return Modified flag
     */
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

    @Override
    protected SWF getSwf() {
        Openable op = getOpenable();
        if (op instanceof SWF) {
            return (SWF) op;
        }
        if (op instanceof ABC) {
            return ((ABC) op).getSwf();
        }
        return null;
    }
}
