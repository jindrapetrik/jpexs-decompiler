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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Indexing of ABCs for faster access. Indexes ABC classes for faster class and
 * property resolving
 *
 * @author JPEXS
 */
public final class AbcIndexing {

    private AbcIndexing parent = null;

    private final List<ABC> abcs = new ArrayList<>();

    private ABC selectedAbc = null;

    /**
     * Creates empty index with parent.
     *
     * @param parent If not property/class found, search also in this parent
     */
    public AbcIndexing(AbcIndexing parent) {
        this(null, parent);
    }

    /**
     * Creates index from SWF file with parent
     *
     * @param swf SWF file to load initial ABCs from
     * @param parent If not property/class found, search also in this parent
     */
    public AbcIndexing(SWF swf, AbcIndexing parent) {
        this.parent = parent;
        if (swf != null) {
            for (ABCContainerTag at : swf.getAbcList()) {
                addAbc(at.getABC());
            }
        }
    }

    /**
     * Creates index from SWF file.
     *
     * @param swf SWF file to load initial ABCs from
     */
    public AbcIndexing(SWF swf) {
        this(swf, null);
    }

    /**
     * Creates empty index
     */
    public AbcIndexing() {
        this(null, null);
    }

    /**
     * Property key
     */
    public static class PropertyDef {

        private final String propName;

        private final GraphTargetItem parent;

        private int propNsIndex = 0;

        private ABC abc = null;

        @Override
        public String toString() {
            return parent.toString() + ":" + propName + (propNsIndex > 0 ? "[ns:" + propNsIndex + "]" : "");
        }

        private void setPrivate(ABC abc, int propNsIndex) {
            this.propNsIndex = propNsIndex;
            this.abc = abc;
        }

        public String getPropertyName() {
            return propName;
        }

        /**
         * Creates key to property.
         *
         * @param propName Name of the property
         * @param parent Parent type (usually TypeItem)
         * @param abc ABC for private/protected namespace resolving
         * @param propNsIndex Index of property(trait) namespace for
         * private/protected namespace reolving
         */
        public PropertyDef(String propName, GraphTargetItem parent, ABC abc, int propNsIndex) {
            this.propName = propName;
            this.parent = parent;
            if (abc == null || propNsIndex <= 0) {
                return;
            }
            int k = abc.constants.getNamespace(propNsIndex).kind;
            if (k != Namespace.KIND_PACKAGE) {
                setPrivate(abc, propNsIndex);
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.propName);
            hash = 17 * hash + Objects.hashCode(this.parent);
            hash = 17 * hash + this.propNsIndex;
            hash = 17 * hash + System.identityHashCode(this.abc);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PropertyDef other = (PropertyDef) obj;
            if (!Objects.equals(this.propName, other.propName)) {
                return false;
            }
            if (!Objects.equals(this.parent, other.parent)) {
                return false;
            }
            if (this.propNsIndex != other.propNsIndex) {
                return false;
            }
            return (this.abc == other.abc);
        }
    }

    /**
     * Namespaced property key
     */
    public static class PropertyNsDef {

        private final String propName;

        private final DottedChain ns;

        private int propNsIndex = 0;

        private ABC abc = null;

        private void setPrivate(ABC abc, int propNsIndex) {
            this.propNsIndex = propNsIndex;
            this.abc = abc;
        }

        public String getPropertyName() {
            return propName;
        }

        @Override
        public String toString() {
            return ns.toString() + ":" + propName + (propNsIndex > 0 ? "[ns:" + propNsIndex + "]" : "");
        }

        public PropertyNsDef(String propName, DottedChain ns, ABC abc, int nsIndex) {
            this.propName = propName;
            this.ns = ns;
            if (abc == null || nsIndex <= 0) {
                return;
            }
            int k = abc.constants.getNamespace(nsIndex).kind;
            if (k != Namespace.KIND_PACKAGE) {
                setPrivate(abc, nsIndex);
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.propName);
            hash = 19 * hash + Objects.hashCode(this.ns);
            hash = 19 * hash + this.propNsIndex;
            hash = 19 * hash + System.identityHashCode(this.abc);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PropertyNsDef other = (PropertyNsDef) obj;
            if (!Objects.equals(this.propName, other.propName)) {
                return false;
            }
            if (!Objects.equals(this.ns, other.ns)) {
                return false;
            }
            if (this.propNsIndex != other.propNsIndex) {
                return false;
            }
            return (this.abc == other.abc);
        }
    }

    public static class TraitIndex {

        public Trait trait;

        public ABC abc;

        public GraphTargetItem returnType;

        public ValueKind value;

        public GraphTargetItem objType;

        public TraitIndex(Trait trait, ABC abc, GraphTargetItem type, ValueKind value, GraphTargetItem objType) {
            this.trait = trait;
            this.abc = abc;
            this.returnType = type;
            this.value = value;
            this.objType = objType;
        }
    }

    public static class ClassIndex {

        public int index;

        public ABC abc;

        public ClassIndex parent;

        @Override
        public String toString() {
            return abc.constants.getMultiname(abc.instance_info.get(index).name_index).getNameWithNamespace(abc.constants, true).toPrintableString(true);
        }

        public ClassIndex(int index, ABC abc, ClassIndex parent) {
            this.index = index;
            this.abc = abc;
            this.parent = parent;
        }
    }

    private final Map<GraphTargetItem, ClassIndex> classes = new HashMap<>();

    private final Map<PropertyDef, TraitIndex> instanceProperties = new HashMap<>();

    private final Map<PropertyDef, TraitIndex> classProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> instanceNsProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> classNsProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> scriptProperties = new HashMap<>();

    public ClassIndex findClass(GraphTargetItem cls) {
        if (!classes.containsKey(cls)) {
            if (parent == null) {
                return null;
            }
            return parent.findClass(cls);
        }
        return classes.get(cls);
    }

    public TraitIndex findScriptProperty(DottedChain ns) {
        return findScriptProperty(ns.getLast(), ns.getWithoutLast());
    }

    public TraitIndex findScriptProperty(String propName, DottedChain ns) {
        PropertyNsDef nsd = new PropertyNsDef(propName, ns, null, 0);
        if (!scriptProperties.containsKey(nsd)) {
            if (parent != null) {
                return parent.findScriptProperty(propName, ns);
            }
            return null;
        }
        return scriptProperties.get(nsd);
    }

    public TraitIndex findNsProperty(PropertyNsDef prop, boolean findStatic, boolean findInstance) {

        if (findStatic && classNsProperties.containsKey(prop)) {
            if (!classNsProperties.containsKey(prop)) {
                if (parent != null) {
                    TraitIndex ret = parent.findNsProperty(prop, findStatic, findInstance);
                    if (ret != null) {
                        return ret;
                    }
                }
            } else {
                return classNsProperties.get(prop);
            }
        }
        if (findInstance && instanceNsProperties.containsKey(prop)) {
            if (!instanceNsProperties.containsKey(prop)) {
                if (parent != null) {
                    TraitIndex ret = parent.findNsProperty(prop, findStatic, findInstance);
                    if (ret != null) {
                        return ret;
                    }
                }
            } else {
                return instanceNsProperties.get(prop);
            }
        }
        return null;
    }

    public TraitIndex findProperty(PropertyDef prop, boolean findStatic, boolean findInstance) {

        //search all static first
        if (findStatic && classProperties.containsKey(prop)) {
            if (!classProperties.containsKey(prop)) {
                if (parent != null) {
                    TraitIndex ret = parent.findProperty(prop, findStatic, findInstance);
                    if (ret != null) {
                        return ret;
                    }
                }
            } else {
                return classProperties.get(prop);
            }
        }

        //now search instance
        if (findInstance && instanceProperties.containsKey(prop)) {
            if (!instanceProperties.containsKey(prop)) {
                if (parent != null) {
                    TraitIndex ret = parent.findProperty(prop, findStatic, findInstance);
                    if (ret != null) {
                        return ret;
                    }
                }
            } else {
                return instanceProperties.get(prop);
            }
        }

        //now search parent class
        AbcIndexing.ClassIndex ci = findClass(prop.parent);
        if (ci != null && ci.parent != null && (prop.abc == null || prop.propNsIndex == 0)) {
            ci = ci.parent;
            //parent protected
            DottedChain parentClass = ci.abc.instance_info.get(ci.index).getName(ci.abc.constants).getNameWithNamespace(ci.abc.constants, true);
            TraitIndex pti = findProperty(new PropertyDef(prop.propName, new TypeItem(parentClass), ci.abc, ci.abc.instance_info.get(ci.index).protectedNS), findStatic, findInstance);
            if (pti != null) {
                return pti;
            }
            //parent public
            return findProperty(new PropertyDef(prop.propName, new TypeItem(parentClass), null, 0), findStatic, findInstance);
        }

        return null;
    }

    public static GraphTargetItem multinameToType(int m_index, AVM2ConstantPool constants) {
        if (m_index == 0) {
            return TypeItem.UNBOUNDED;
        }
        Multiname m = constants.getMultiname(m_index);
        if (m.kind == Multiname.TYPENAME) {
            GraphTargetItem obj = multinameToType(m.qname_index, constants);
            List<GraphTargetItem> params = new ArrayList<>();
            for (int pm : m.params) {
                params.add(multinameToType(pm, constants));
            }
            return new ApplyTypeAVM2Item(null, null, obj, params);
        } else {
            return new TypeItem(m.getNameWithNamespace(constants, true));
        }
    }

    private static GraphTargetItem getTraitReturnType(ABC abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) t;
            if (tsc.type_index == 0) {
                return TypeItem.UNBOUNDED;
            }
            return PropertyAVM2Item.multinameToType(tsc.type_index, abc.constants);
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return PropertyAVM2Item.multinameToType(abc.method_info.get(tmgs.method_info).ret_type, abc.constants);
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                if (abc.method_info.get(tmgs.method_info).param_types.length > 0) {
                    return PropertyAVM2Item.multinameToType(abc.method_info.get(tmgs.method_info).param_types[0], abc.constants);
                } else {
                    return TypeItem.UNBOUNDED;
                }
            }
        }
        if (t instanceof TraitFunction) {
            return new TypeItem(DottedChain.FUNCTION);
        }
        return TypeItem.UNBOUNDED;
    }

    protected void indexTraits(ABC abc, int name_index, Traits ts, Map<PropertyDef, TraitIndex> map, Map<PropertyNsDef, TraitIndex> mapNs) {
        for (Trait t : ts.traits) {
            ValueKind propValue = null;
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
            }
            if (map != null) {
                PropertyDef dp = new PropertyDef(t.getName(abc).getName(abc.constants, new ArrayList<>() /*?*/, true, true /*FIXME ???*/), multinameToType(name_index, abc.constants), abc, abc.constants.getMultiname(t.name_index).namespace_index);
                map.put(dp, new TraitIndex(t, abc, getTraitReturnType(abc, t), propValue, multinameToType(name_index, abc.constants)));
            }
            if (mapNs != null) {
                Multiname m = abc.constants.getMultiname(t.name_index);
                PropertyNsDef ndp = new PropertyNsDef(t.getName(abc).getName(abc.constants, new ArrayList<>() /*?*/, true, true/*FIXME ???*/), m == null || m.namespace_index == 0 ? DottedChain.EMPTY : m.getNamespace(abc.constants).getName(abc.constants), abc, m == null ? 0 : m.namespace_index);
                TraitIndex ti = new TraitIndex(t, abc, getTraitReturnType(abc, t), propValue, multinameToType(name_index, abc.constants));
                if (!mapNs.containsKey(ndp)) {
                    mapNs.put(ndp, ti);
                }
            }

        }
    }

    public void refreshSelected() {
        refreshAbc(getSelectedAbc());
    }

    public void refreshAbc(ABC abc) {
        if (abc == null) {
            return;
        }
        removeAbc(abc);
        addAbc(abc);
    }

    public void removeAbc(ABC abc) {
        abcs.remove(abc);
        Set<GraphTargetItem> gti_keys = new HashSet<>(classes.keySet());
        for (GraphTargetItem key : gti_keys) {
            if (classes.get(key).abc == abc) {
                classes.remove(key);
            }
        }
        Set<PropertyDef> pd_keys = new HashSet<>(instanceProperties.keySet());

        for (PropertyDef key : pd_keys) {
            if (instanceProperties.get(key).abc == abc) {
                instanceProperties.remove(key);
            }
        }
        pd_keys = new HashSet<>(classProperties.keySet());
        for (PropertyDef key : pd_keys) {
            if (classProperties.get(key).abc == abc) {
                classProperties.remove(key);
            }
        }

        Set<PropertyNsDef> pnd_keys = new HashSet<>(scriptProperties.keySet());

        for (PropertyNsDef key : pnd_keys) {
            if (scriptProperties.get(key).abc == abc) {
                scriptProperties.remove(key);
            }
        }

        pnd_keys = new HashSet<>(classNsProperties.keySet());
        for (PropertyNsDef key : pnd_keys) {
            if (classNsProperties.get(key).abc == abc) {
                classNsProperties.remove(key);
            }
        }

        pnd_keys = new HashSet<>(instanceNsProperties.keySet());
        for (PropertyNsDef key : pnd_keys) {
            if (instanceNsProperties.get(key).abc == abc) {
                instanceNsProperties.remove(key);
            }
        }

    }

    public void addAbc(ABC abc) {
        if (abc == null) {
            return;
        }
        List<ClassIndex> addedClasses = new ArrayList<>();
        for (int i = 0; i < abc.instance_info.size(); i++) {
            InstanceInfo ii = abc.instance_info.get(i);
            if (ii.deleted) {
                continue;
            }
            ClassInfo ci = abc.class_info.get(i);
            ClassIndex cindex = new ClassIndex(i, abc, null);
            addedClasses.add(cindex);
            GraphTargetItem cname = multinameToType(ii.name_index, abc.constants);
            classes.put(cname, cindex);

            indexTraits(abc, ii.name_index, ii.instance_traits, instanceProperties, instanceNsProperties);
            indexTraits(abc, ii.name_index, ci.static_traits, classProperties, classNsProperties);
        }
        for (int i = 0; i < abc.script_info.size(); i++) {
            indexTraits(abc, 0, abc.script_info.get(i).traits, null, scriptProperties);
        }

        for (ClassIndex cindex : addedClasses) {
            int parentClassName = abc.instance_info.get(cindex.index).super_index;
            if (parentClassName > 0) {
                TypeItem parentClass = new TypeItem(abc.constants.getMultiname(parentClassName).getNameWithNamespace(abc.constants, true));
                ClassIndex parentClassIndex = findClass(parentClass);
                if (parentClassIndex == null) {
                    //Parent class can be deleted, do not check. TODO: handle this better
                    //throw new RuntimeException("Parent class " + parentClass + " definition not found!");
                }
                cindex.parent = parentClassIndex;
            }
        }
        abcs.add(abc);
        selectedAbc = abc;
    }

    public void selectAbc(ABC abc) {
        if (abcs.contains(abc)) {
            selectedAbc = abc;
        } else {
            addAbc(abc);
        }
    }

    public ABC getSelectedAbc() {
        return selectedAbc;
    }

    public DottedChain nsValueToName(String valueStr) {
        for (ABC abc : abcs) {
            DottedChain ret = abc.nsValueToName(valueStr);
            if (!ret.isEmpty()) {
                return ret;
            }
        }
        if (parent != null) {
            return parent.nsValueToName(valueStr);
        }
        return null;
    }
}
