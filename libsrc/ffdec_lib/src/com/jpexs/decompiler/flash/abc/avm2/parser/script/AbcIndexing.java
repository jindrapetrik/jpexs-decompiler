/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Indexing of ABCs for faster access. Indexes ABC classes for faster class and
 * property resolving.
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
            rebuildPkgToObjectsNameMap();
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

        private static final String BUILT_IN_NS = "http://adobe.com/AS3/2006/builtin";

        private static Map<ABC, Integer> builtInNsPerAbc = new WeakHashMap<>();

        private final String propName;

        private String propNsString = null;

        private final GraphTargetItem parent;

        private int propNsIndex = 0;

        private ABC abc = null;

        /**
         * To string
         * @return String
         */
        @Override
        public String toString() {
            return parent.toString() + ":" + propName + (propNsIndex > 0 ? "[ns:" + propNsIndex + "]" : "") + (propNsString != null ? "[ns: " + propNsString + "]" : "");
        }

        private void setPrivate(ABC abc, int propNsIndex) {
            this.propNsIndex = propNsIndex;
            this.abc = abc;
        }

        private void setProtected(ABC abc, int propNsIndex) {
            this.abc = null;
            this.propNsString = abc.constants.getNamespace(propNsIndex).getRawName(abc.constants);
        }

        /**
         * Gets property name
         * @return Property name
         */
        public String getPropertyName() {
            return propName;
        }

        /**
         * Gets property namespace string
         * @return Property namespace string
         */
        public String getPropNsString() {
            return propNsString;
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

            int builtInIndex = -1;
            if (abc != null) {
                Integer builtInNs = builtInNsPerAbc.get(abc);

                if (builtInNs == null) {
                    //we need to avoid modifying the ABC
                    /*    builtInIndex = abc.constants.getNamespaceId(Namespace.KIND_NAMESPACE, BUILT_IN_NS, 0, true);
                    builtInNsPerAbc.put(abc, builtInIndex);*/
                    builtInIndex = Integer.MIN_VALUE; //??
                } else {
                    builtInIndex = builtInNs;
                }
            }

            this.propName = propName;
            this.parent = parent;
            if (abc == null || propNsIndex <= 0) {
                return;
            }
            int k = abc.constants.getNamespace(propNsIndex).kind;
            if (k != Namespace.KIND_PACKAGE && propNsIndex != builtInIndex) {
                if (k == Namespace.KIND_PROTECTED || k == Namespace.KIND_STATIC_PROTECTED) {
                    setProtected(abc, propNsIndex);
                } else {
                    setPrivate(abc, propNsIndex);
                }
            }
        }

        /**
         * Creates key to property.
         * @param propName Property name
         * @param parent Parent type
         * @param propNsString Namespace string
         */
        public PropertyDef(String propName, GraphTargetItem parent, String propNsString) {
            this.propName = propName;
            this.parent = parent;
            this.abc = null;
            this.propNsString = propNsString;
        }

        /**
         * Hash code
         * @return Hash code
         */
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + Objects.hashCode(this.propName);
            hash = 37 * hash + Objects.hashCode(this.propNsString);
            hash = 37 * hash + Objects.hashCode(this.parent);
            hash = 37 * hash + this.propNsIndex;
            return hash;
        }

        /**
         * Equals
         * @param obj Object
         * @return True if equals
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PropertyDef other = (PropertyDef) obj;
            if (this.propNsIndex != other.propNsIndex) {
                return false;
            }
            if (!Objects.equals(this.propName, other.propName)) {
                return false;
            }
            if (!Objects.equals(this.propNsString, other.propNsString)) {
                return false;
            }
            return Objects.equals(this.parent, other.parent);
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

        /**
         * Gets property name
         * @return Property name
         */
        public String getPropertyName() {
            return propName;
        }

        /**
         * To string
         * @return String
         */
        @Override
        public String toString() {
            return ns.toString() + ":" + propName + (propNsIndex > 0 ? "[ns:" + propNsIndex + "]" : "");
        }

        /**
         * Constructs namespaced property key
         * @param propName Property name
         * @param ns Namespace
         * @param abc ABC
         * @param nsIndex Namespace index
         */
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

        /**
         * Hash code
         * @return Hash code
         */
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.propName);
            hash = 19 * hash + Objects.hashCode(this.ns);
            hash = 19 * hash + this.propNsIndex;
            hash = 19 * hash + System.identityHashCode(this.abc);
            return hash;
        }

        /**
         * Equals
         * @param obj Object
         * @return True if equals
         */
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

    /**
     * Trait index
     */
    public static class TraitIndex {

        /**
         * Trait
         */
        public Trait trait;

        /**
         * ABC
         */
        public ABC abc;

        /**
         * Return type
         */
        public GraphTargetItem returnType;

        /**
         * Call return type
         */
        public GraphTargetItem callReturnType;

        /**
         * Value kind
         */
        public ValueKind value;

        /**
         * Object type
         */
        public GraphTargetItem objType;

        /**
         * Constructs trait index
         * @param trait Trait
         * @param abc ABC
         * @param type Type
         * @param callType Call type
         * @param value Value
         * @param objType Object type
         */
        public TraitIndex(Trait trait, ABC abc, GraphTargetItem type, GraphTargetItem callType, ValueKind value, GraphTargetItem objType) {
            this.trait = trait;
            this.abc = abc;
            this.returnType = type;
            this.callReturnType = callType;
            this.value = value;
            this.objType = objType;
        }
    }

    /**
     * Class definition
     */
    private static class ClassDef {

        /**
         * Class type
         */
        public GraphTargetItem type;

        /**
         * Package
         */
        public DottedChain pkg;

        private GraphTargetItem noNsType(GraphTargetItem type) {
            TypeItem ti = (TypeItem) type;
            ti = (TypeItem) ti.clone();
            ti.ns = null;
            return ti;
        }

        /**
         * Constructs class definition
         * @param type Type
         * @param abc ABC
         * @param scriptIndex Script index
         */
        public ClassDef(GraphTargetItem type, ABC abc, Integer scriptIndex) {
            this.type = type;
            if (scriptIndex != null) {
                for (Trait t : abc.script_info.get(scriptIndex).traits.traits) {
                    Multiname m = t.getName(abc);
                    int nskind = m.getSimpleNamespaceKind(abc.constants);
                    if (nskind == Namespace.KIND_PACKAGE) {
                        pkg = m.getSimpleNamespaceName(abc.constants);
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(noNsType(this.type));
            hash = 17 * hash + Objects.hashCode(this.pkg);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassDef other = (ClassDef) obj;
            if (!Objects.equals(noNsType(this.type), noNsType(other.type))) {
                return false;
            }
            return Objects.equals(this.pkg, other.pkg);
        }

    }

    /**
     * Class index
     */
    public static class ClassIndex {

        /**
         * Index of class in ABC
         */
        public int index;

        /**
         * ABC
         */
        public ABC abc;

        /**
         * Parent class index
         */
        public ClassIndex parent;

        /**
         * Script index
         */
        public Integer scriptIndex;

        /**
         * To string
         * @return String
         */
        @Override
        public String toString() {
            return abc.constants.getMultiname(abc.instance_info.get(index).name_index).getNameWithNamespace(abc.constants, true).toPrintableString(true);
        }

        /**
         * Constructs class index
         * @param index Index
         * @param abc ABC
         * @param parent Parent
         * @param scriptIndex Script index
         */
        public ClassIndex(int index, ABC abc, ClassIndex parent, Integer scriptIndex) {
            this.index = index;
            this.abc = abc;
            this.parent = parent;
            this.scriptIndex = scriptIndex;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.index;
            hash = 37 * hash + System.identityHashCode(this.abc);
            hash = 37 * hash + Objects.hashCode(this.parent);
            hash = 37 * hash + Objects.hashCode(this.scriptIndex);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassIndex other = (ClassIndex) obj;
            if (this.index != other.index) {
                return false;
            }
            if (this.abc != other.abc) {
                return false;
            }
            if (!Objects.equals(this.parent, other.parent)) {
                return false;
            }
            return Objects.equals(this.scriptIndex, other.scriptIndex);
        }
    }

    private Map<DottedChain, Set<String>> pkgToObjectsName = new LinkedHashMap<>();

    private final Map<ClassDef, ClassIndex> classes = new HashMap<>();

    private final Map<PropertyDef, TraitIndex> instanceProperties = new HashMap<>();

    private final Map<PropertyDef, TraitIndex> classProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> instanceNsProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> classNsProperties = new HashMap<>();

    private final Map<PropertyNsDef, TraitIndex> scriptProperties = new HashMap<>();

    /**
     * Rebuilds package to objects name map.
     */
    public void rebuildPkgToObjectsNameMap() {
        pkgToObjectsName.clear();
        for (ClassDef cd : classes.keySet()) {
            if (!(cd.type instanceof TypeItem)) {
                continue;
            }
            if (!pkgToObjectsName.containsKey(cd.pkg)) {
                pkgToObjectsName.put(cd.pkg, new LinkedHashSet<>());
            }
            pkgToObjectsName.get(cd.pkg).add(((TypeItem) cd.type).fullTypeName.getLast());
        }
        for (PropertyNsDef nsdef : scriptProperties.keySet()) {
            if (!pkgToObjectsName.containsKey(nsdef.ns)) {
                pkgToObjectsName.put(nsdef.ns, new LinkedHashSet<>());
            }
            pkgToObjectsName.get(nsdef.ns).add(nsdef.propName);
        }
    }

    /**
     * Gets package objects.
     * @param pkg Package
     * @return Set of class names
     */
    public Set<String> getPackageObjects(DottedChain pkg) {
        Set<String> classNames = new LinkedHashSet<>();
        if (pkgToObjectsName.containsKey(pkg)) {
            classNames.addAll(pkgToObjectsName.get(pkg));
        }
        if (parent != null) {
            classNames.addAll(parent.getPackageObjects(pkg));
        }
        return classNames;
    }

    /**
     * Finds class in index.
     * @param cls Class to find
     * @param abc ABC
     * @param scriptIndex Script index
     * @return Class index or null
     */
    public ClassIndex findClass(GraphTargetItem cls, ABC abc, Integer scriptIndex) {
        ClassDef keyWithScriptIndex = new ClassDef(cls, abc, scriptIndex);
        if (classes.containsKey(keyWithScriptIndex)) {
            return classes.get(keyWithScriptIndex);
        }

        ClassDef keyWithNoScriptIndex = new ClassDef(cls, abc, null);
        if (classes.containsKey(keyWithNoScriptIndex)) {
            return classes.get(keyWithNoScriptIndex);
        }

        if (parent == null) {
            return null;
        }
        return parent.findClass(cls, abc, scriptIndex);
    }

    /**
     * Finds property type or call type.
     * @param abc ABC
     * @param cls Class
     * @param propName Property name
     * @param ns Namespace
     * @param findStatic Find static properties
     * @param findInstance Find instance properties
     * @param findProtected Find protected namespace properties
     * @param type Property type
     * @param callType Call type
     */
    public void findPropertyTypeOrCallType(ABC abc, GraphTargetItem cls, String propName, int ns, boolean findStatic, boolean findInstance, boolean findProtected, Reference<GraphTargetItem> type, Reference<GraphTargetItem> callType) {
        TraitIndex traitIndex = findProperty(new PropertyDef(propName, cls, abc, ns), findStatic, findInstance, findProtected);
        if (traitIndex == null) {
            type.setVal(TypeItem.UNKNOWN);
            callType.setVal(TypeItem.UNKNOWN);
        } else {
            type.setVal(traitIndex.returnType);
            callType.setVal(traitIndex.callReturnType);
        }
    }

    /**
     * Finds property type.
     * @param abc ABC
     * @param cls Class
     * @param propName Property name
     * @param ns Namespace
     * @param findStatic Find static properties
     * @param findInstance Find instance properties
     * @param findProtected Find protected namespace properties
     * @return Trait index or null
     */
    public GraphTargetItem findPropertyType(ABC abc, GraphTargetItem cls, String propName, int ns, boolean findStatic, boolean findInstance, boolean findProtected) {
        TraitIndex traitIndex = findProperty(new PropertyDef(propName, cls, abc, ns), findStatic, findInstance, findProtected);
        if (traitIndex == null) {
            return TypeItem.UNBOUNDED;
        }
        return traitIndex.returnType;
    }

    /**
     * Finds property call type.
     * @param abc ABC
     * @param cls Class
     * @param propName Property name
     * @param ns Namespace
     * @param findStatic Find static properties
     * @param findInstance Find instance properties
     * @param findProtected Find protected namespace properties
     * @return Trait index or null
     */
    public GraphTargetItem findPropertyCallType(ABC abc, GraphTargetItem cls, String propName, int ns, boolean findStatic, boolean findInstance, boolean findProtected) {
        TraitIndex traitIndex = findProperty(new PropertyDef(propName, cls, abc, ns), findStatic, findInstance, findProtected);
        if (traitIndex == null) {
            return TypeItem.UNBOUNDED;
        }
        return traitIndex.callReturnType;
    }

    /**
     * Finds script property
     * @param ns Namespace
     * @return Trait index or null
     */
    public TraitIndex findScriptProperty(DottedChain ns) {
        return findScriptProperty(ns.getLast(), ns.getWithoutLast());
    }

    /**
     * Finds script property
     * @param propName Property name
     * @param ns Namespace
     * @return Trait index or null
     */
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

    /**
     * Finds property with namespace.
     * @param prop Property to find
     * @param findStatic Find static properties
     * @param findInstance Find instance properties
     * @return Trait index or null
     */
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

    /**
     * Finds property in index
     * @param prop Property to find
     * @param findStatic Find static properties
     * @param findInstance Find instance properties
     * @param findProtected Find protected namespace properties
     * @return Trait index or null
     */
    public TraitIndex findProperty(PropertyDef prop, boolean findStatic, boolean findInstance, boolean findProtected) {
        /*System.out.println("searching " + prop);
        for (PropertyDef p : instanceProperties.keySet()) {
            if (p.parent.equals(new TypeItem("tests_classes.TestConvertParent"))) {
                System.out.println("- " + p);
            }
        }
        System.out.println("-----------");
         */
        //search all static first
        if (findStatic && classProperties.containsKey(prop)) {
            TraitIndex ti = classProperties.get(prop);
            if (ti != null) {
                return ti;
            }
            if (parent != null) {
                TraitIndex ret = parent.findProperty(prop, findStatic, findInstance, findProtected);
                if (ret != null) {
                    return ret;
                }
            }
        }

        //now search instance
        if (findInstance && instanceProperties.containsKey(prop)) {
            TraitIndex ti = instanceProperties.get(prop);
            if (ti != null) {
                return ti;
            }
            if (parent != null) {
                TraitIndex ret = parent.findProperty(prop, findStatic, findInstance, findProtected);
                if (ret != null) {
                    return ret;
                }
            }
        }

        //now search parent class
        AbcIndexing.ClassIndex ci = findClass(prop.parent, prop.abc, null);
        if (ci != null && ci.parent != null && (prop.abc == null || prop.propNsIndex == 0)) {
            AbcIndexing.ClassIndex ciParent = ci.parent;
            DottedChain parentClass = ciParent.abc.instance_info.get(ciParent.index).getName(ciParent.abc.constants).getNameWithNamespace(ciParent.abc.constants, true);
            TraitIndex pti = findProperty(new PropertyDef(prop.propName, new TypeItem(parentClass), prop.getPropNsString()), findStatic, findInstance, findProtected);
            if (pti != null) {
                return pti;
            }
        }

        if (findProtected && prop.propNsIndex == 0) {
            if (ci != null) {
                int protNs = ci.abc.instance_info.get(ci.index).protectedNS;
                PropertyDef prop2 = new PropertyDef(prop.propName, prop.parent, ci.abc, protNs);
                TraitIndex pti = findProperty(prop2, findStatic, findInstance, false);
                if (pti != null) {
                    return pti;
                }
            }
        }
        if (parent != null) {
            TraitIndex pti = parent.findProperty(prop, findStatic, findInstance, findProtected);
            if (pti != null) {
                return pti;
            }
        }

        return null;
    }

    /**
     * Converts multiname to type
     * @param m_index Multiname index
     * @param constants AVM2 constant pool
     * @return Type
     */
    public static GraphTargetItem multinameToType(int m_index, AVM2ConstantPool constants) {
        if (m_index == 0) {
            return TypeItem.UNBOUNDED;
        }
        Multiname m = constants.getMultiname(m_index);
        if (m.isCyclic()) {
            return null;
        }
        if (m.kind == Multiname.TYPENAME) {
            GraphTargetItem obj = multinameToType(m.qname_index, constants);
            if (obj == null) {
                return null;
            }
            List<GraphTargetItem> params = new ArrayList<>();
            for (int pm : m.params) {
                GraphTargetItem r = multinameToType(pm, constants);
                if (r == null) {
                    return null;
                }
                if (pm == 0) {
                    r = new NullAVM2Item(null, null);
                }
                params.add(r);
            }
            return new ApplyTypeAVM2Item(null, null, obj, params);
        } else {
            if (m.namespace_index != 0 && m.getNamespace(constants).kind == Namespace.KIND_PRIVATE) {
                return new TypeItem(m.getName(constants, new ArrayList<>(), true, true), "ns:" + m.namespace_index);
            }
            return new TypeItem(m.getNameWithNamespace(constants, true));
        }
    }

    private static GraphTargetItem getTraitCallReturnType(ABC abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            return TypeItem.UNBOUNDED;
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return TypeItem.UNBOUNDED;
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                return TypeItem.UNBOUNDED;
            }
            return multinameToType(abc.method_info.get(tmgs.method_info).ret_type, abc.constants);
        }

        if (t instanceof TraitFunction) {
            TraitFunction tf = (TraitFunction) t;
            return multinameToType(abc.method_info.get(tf.method_info).ret_type, abc.constants);

        }
        return TypeItem.UNBOUNDED;
    }

    private static GraphTargetItem getTraitReturnType(ABC abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) t;
            if (tsc.type_index == 0) {
                return TypeItem.UNBOUNDED;
            }
            return multinameToType(tsc.type_index, abc.constants);
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return multinameToType(abc.method_info.get(tmgs.method_info).ret_type, abc.constants);
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                if (abc.method_info.get(tmgs.method_info).param_types.length > 0) {
                    return multinameToType(abc.method_info.get(tmgs.method_info).param_types[0], abc.constants);
                } else {
                    return TypeItem.UNBOUNDED;
                }
            }
            return new TypeItem(DottedChain.FUNCTION);
        }

        if (t instanceof TraitFunction) {
            return new TypeItem(DottedChain.FUNCTION);
        }
        return TypeItem.UNBOUNDED;
    }

    /**
     * Indexes traits
     * @param abc ABC
     * @param name_index Name index
     * @param ts Traits
     * @param map Map to index
     * @param mapNs Map to index
     */
    protected void indexTraits(ABC abc, int name_index, Traits ts, Map<PropertyDef, TraitIndex> map, Map<PropertyNsDef, TraitIndex> mapNs) {
        for (Trait t : ts.traits) {
            ValueKind propValue = null;
            if (t instanceof TraitSlotConst) {
                TraitSlotConst tsc = (TraitSlotConst) t;
                propValue = new ValueKind(tsc.value_index, tsc.value_kind);
            }
            if (map != null) {
                PropertyDef dp = new PropertyDef(t.getName(abc).getName(abc.constants, new ArrayList<>() /*?*/, true, false), multinameToType(name_index, abc.constants), abc, abc.constants.getMultiname(t.name_index).namespace_index);
                map.put(dp, new TraitIndex(t, abc, getTraitReturnType(abc, t), getTraitCallReturnType(abc, t), propValue, multinameToType(name_index, abc.constants)));
            }
            if (mapNs != null) {
                Multiname m = abc.constants.getMultiname(t.name_index);
                PropertyNsDef ndp = new PropertyNsDef(t.getName(abc).getName(abc.constants, new ArrayList<>() /*?*/, true, true/*FIXME ???*/), m == null || m.namespace_index == 0 ? DottedChain.EMPTY : m.getNamespace(abc.constants).getName(abc.constants), abc, m == null ? 0 : m.namespace_index);
                TraitIndex ti = new TraitIndex(t, abc, getTraitReturnType(abc, t), getTraitCallReturnType(abc, t), propValue, multinameToType(name_index, abc.constants));
                if (!mapNs.containsKey(ndp)) {
                    mapNs.put(ndp, ti);
                }
            }

        }
    }

    /**
     * Refreshes selected ABC
     */
    public void refreshSelected() {
        refreshAbc(getSelectedAbc());
    }

    /**
     * Refreshes ABC in index
     * @param abc ABC to refresh
     */
    public void refreshAbc(ABC abc) {
        if (abc == null) {
            return;
        }
        removeAbc(abc);
        addAbc(abc);
        rebuildPkgToObjectsNameMap();
    }

    /**
     * Removes ABC from index
     * @param abc ABC to remove
     */
    public void removeAbc(ABC abc) {
        abcs.remove(abc);
        Set<ClassDef> gti_keys = new HashSet<>(classes.keySet());
        for (ClassDef key : gti_keys) {
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

    /**
     * Adds ABC to index
     *
     * @param abc ABC to add
     */
    public void addAbc(ABC abc) {
        if (abc == null) {
            return;
        }
        List<ClassIndex> addedClasses = new ArrayList<>();

        for (int i = 0; i < abc.script_info.size(); i++) {
            indexTraits(abc, 0, abc.script_info.get(i).traits, null, scriptProperties);
            for (int t = 0; t < abc.script_info.get(i).traits.traits.size(); t++) {
                Trait tr = abc.script_info.get(i).traits.traits.get(t);
                if (tr instanceof TraitClass) {
                    TraitClass tc = (TraitClass) tr;
                    InstanceInfo ii = abc.instance_info.get(tc.class_info);
                    if (ii.deleted) {
                        continue;
                    }
                    ClassInfo ci = abc.class_info.get(tc.class_info);
                    int nsKind = abc.constants.getMultiname(tc.name_index).getSimpleNamespaceKind(abc.constants);
                    Integer classScriptIndex = nsKind == Namespace.KIND_PACKAGE ? null : i;
                    ClassIndex cindex = new ClassIndex(tc.class_info, abc, null, classScriptIndex);
                    addedClasses.add(cindex);
                    GraphTargetItem cname = multinameToType(ii.name_index, abc.constants);
                    classes.put(new ClassDef(cname, abc, classScriptIndex), cindex);

                    indexTraits(abc, ii.name_index, ii.instance_traits, instanceProperties, instanceNsProperties);
                    indexTraits(abc, ii.name_index, ci.static_traits, classProperties, classNsProperties);
                }
            }
        }

        for (ClassIndex cindex : addedClasses) {
            int parentClassName = abc.instance_info.get(cindex.index).super_index;
            if (parentClassName > 0) {
                TypeItem parentClass = new TypeItem(abc.constants.getMultiname(parentClassName).getNameWithNamespace(abc.constants, true));
                ClassIndex parentClassIndex = findClass(parentClass, abc, null);
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

    /**
     * Selects ABC for indexing
     *
     * @param abc ABC to select
     */
    public void selectAbc(ABC abc) {
        if (abcs.contains(abc)) {
            selectedAbc = abc;
        } else {
            addAbc(abc);
            rebuildPkgToObjectsNameMap();
        }
    }

    /**
     * Gets selected ABC
     *
     * @return Selected ABC
     */
    public ABC getSelectedAbc() {
        return selectedAbc;
    }

    /**
     * Converts namespace value to name
     * @param valueStr Namespace value
     * @return Namespace name
     */
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

    /**
     * Checks if class is instance of another class
     * @param abc ABC
     * @param classIndex Class index
     * @param searchClassName Class name to search
     * @return True if class is instance of another class
     */
    public boolean isInstanceOf(ABC abc, int classIndex, DottedChain searchClassName) {
        DottedChain clsName = abc.instance_info.get(classIndex).getName(abc.constants).getNameWithNamespace(abc.constants, false);
        if (searchClassName.equals(clsName)) {
            return true;
        }
        if (abc.instance_info.get(classIndex).super_index == 0) {
            return false;
        }
        DottedChain parentClassName = abc.constants.getMultiname(abc.instance_info.get(classIndex).super_index).getNameWithNamespace(abc.constants, false);

        AbcIndexing.ClassIndex ci = findClass(new TypeItem(parentClassName), abc, null);
        if (ci == null) {
            return false;
        }
        return isInstanceOf(ci.abc, ci.index, searchClassName);
    }
}
