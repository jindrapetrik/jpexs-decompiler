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
package com.jpexs.decompiler.flash.action.as2;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DeleteActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.NewMethodActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.ProgressListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import natorder.NaturalOrderComparator;

/**
 * Uninitialized class fields detector for ActionScript 2.
 *
 * @author JPEXS
 */
public class UninitializedClassFieldsDetector {

    private List<ProgressListener> progressListeners = new ArrayList<>();

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    private void fireProgress(ASMSource asm, String asmPath) {
        if (asm instanceof DoInitActionTag) {
            DoInitActionTag doi = (DoInitActionTag) asm;
            String exportName = doi.getSwf().getExportName(doi.spriteId);
            if (exportName != null) {
                asmPath = DottedChain.parseNoSuffix(exportName).toPrintableString(false);
            }
        }
        for (ProgressListener listener : progressListeners) {
            listener.status(asmPath);
        }
    }

    /**
     * Gets path of variable and its getMembers: a.b.c.d => [a,b,c,d].
     *
     * @param item Item to get path from
     * @return List of path or null if not members path
     */
    private List<String> getMembersPath(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            if (!(mem.memberName instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem dv = ((DirectValueActionItem) mem.memberName);
            if (!dv.isString()) {
                return null;
            }
            ret.add(0, dv.getAsString());
            item = mem.object;
        }
        if (item instanceof DirectValueActionItem) {
            DirectValueActionItem dv1 = (DirectValueActionItem) item;
            if (dv1.value instanceof RegisterNumber) {
                RegisterNumber rn = (RegisterNumber) dv1.value;
                if ("this".equals(rn.name)) {
                    ret.add(0, "this");
                    return ret;
                }
            }
        }
        if (!((item instanceof GetVariableActionItem))) {
            return null;
        }
        GetVariableActionItem gv = (GetVariableActionItem) item;
        if (!(gv.name instanceof DirectValueActionItem)) {
            return null;
        }
        DirectValueActionItem dv = ((DirectValueActionItem) gv.name);
        if (!dv.isString()) {
            return null;
        }
        String varName = dv.getAsString();
        ret.add(0, varName);
        return ret;
    }

    /**
     * Gets full path of item: a.b.c.d => [a,b,c,d].
     *
     * @param item Item to get path from
     * @return List of path or null if not members path
     */
    private List<String> getFullPath(GraphTargetItem item) {
        if (item instanceof GetMemberActionItem) {
            return getMembersPath(item);
        }
        if (item instanceof SetVariableActionItem) {
            SetVariableActionItem sv = (SetVariableActionItem) item;
            if (!(sv.name instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem nDv = (DirectValueActionItem) sv.name;
            if (!nDv.isString()) {
                return null;
            }
            List<String> ret = new ArrayList<>();
            ret.add(nDv.getAsString());
            return ret;
        }

        GraphTargetItem name;
        GraphTargetItem objectName;

        if (item instanceof SetMemberActionItem) {
            SetMemberActionItem sm = (SetMemberActionItem) item;
            name = sm.objectName;
            objectName = sm.object;
        } else if (item instanceof CallMethodActionItem) {
            CallMethodActionItem cm = (CallMethodActionItem) item;
            name = cm.methodName;
            objectName = cm.scriptObject;
        } else if (item instanceof NewMethodActionItem) {
            NewMethodActionItem nm = (NewMethodActionItem) item;
            name = nm.methodName;
            objectName = nm.scriptObject;
        } else if (item instanceof DeleteActionItem) {
            DeleteActionItem d = (DeleteActionItem) item;
            name = d.propertyName;
            objectName = d.object;
        } else {
            return null;
        }

        if (!(name instanceof DirectValueActionItem)) {
            return null;
        }
        DirectValueActionItem onDv = (DirectValueActionItem) name;
        if (!onDv.isString()) {
            return null;
        }
        String currentMemberName = onDv.getAsString();
        List<String> path = getMembersPath(objectName);
        if (path == null) {
            return null;
        }
        path.add(currentMemberName);
        return path;
    }

    /**
     * Checks whether the class contains a trait.
     *
     * @param classTraits Class traits
     * @param classInheritance Class inheritance
     * @param className Class name
     * @param name Trait name
     * @return Whether the class contains the trait
     */
    private boolean containsTrait(Map<String, Map<String, Trait>> classTraits, Map<String, List<String>> classInheritance, String className, String name) {
        if (!classTraits.containsKey(className)) {
            return false;
        }
        if (classTraits.get(className).containsKey(name)) {
            return true;
        }
        for (String parent : classInheritance.get(className)) {
            if (classTraits.containsKey(parent) && classTraits.get(parent).containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates uninitialized class traits.
     *
     * @param swf SWF
     * @return Map of class name to map of trait name to trait
     * @throws java.lang.InterruptedException On interruption
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Trait>> calculateAs2UninitializedClassTraits(SWF swf) throws InterruptedException {
        if (swf.isAS3()) {
            return new HashMap<>();
        }
        final Map<String, Map<String, Trait>> resultInstance = Collections.synchronizedMap(new LinkedHashMap<>());
        final Map<String, Map<String, Trait>> resultStatic = Collections.synchronizedMap(new LinkedHashMap<>());
        Map<String, ASMSource> asms = swf.getASMs(false);

        CancellableWorker worker = CancellableWorker.getCurrent();

        final Map<String, Map<String, Trait>> classTraits = Collections.synchronizedMap(new LinkedHashMap<>(ActionScript2Classes.getClassToTraits()));
        final Map<String, List<String>> classInheritance = Collections.synchronizedMap(new HashMap<>(ActionScript2Classes.getClassInheritance()));

        Map<String, List<GraphTargetItem>> cachedTrees = Collections.synchronizedMap(new HashMap<>());

        final Set<Thread> subThreads = Collections.synchronizedSet(new HashSet<>());

        Runnable cancelListener = new Runnable() {
            @Override
            public void run() {
                for (Thread t : subThreads) {
                    t.interrupt();
                }
            }
        };

        if (worker != null) {
            worker.addCancelListener(cancelListener);
        }

        try {
            asms.entrySet().parallelStream()
                    .filter(item -> item.getValue() instanceof DoInitActionTag)
                    .forEach(entry -> {
                        if (worker != null && worker.isCancelled()) {
                            return;
                        }
                        subThreads.add(Thread.currentThread());
                        fireProgress(entry.getValue(), entry.getKey());

                        String key = entry.getKey();
                        ASMSource asm = asms.get(key);
                        if (asm instanceof DoInitActionTag) {
                            DoInitActionTag doi = (DoInitActionTag) asm;
                            String exportName = doi.getSwf().getCharacter(doi.getCharacterId()).getExportName();
                            if (exportName != null && exportName.startsWith("__Packages.")) {
                                //fireProgress(doi, key);
                                List<GraphTargetItem> tree = new ArrayList<>();
                                try {
                                    tree = asm.getActionsToTree();
                                } catch (Throwable t) {
                                    //ignore
                                }
                                cachedTrees.put(key, tree);
                                //get all assigned traits and inheritance tree                
                                for (GraphTargetItem item : tree) {
                                    if (item instanceof InterfaceActionItem) {
                                        InterfaceActionItem iai = (InterfaceActionItem) item;
                                        String className = String.join(".", getMembersPath(iai.name));
                                        classInheritance.put(className, new ArrayList<>());
                                        if (iai.superInterfaces != null) {
                                            for (GraphTargetItem imp : iai.superInterfaces) {
                                                String imtName = String.join(".", getMembersPath(imp));
                                                classInheritance.get(className).add(imtName);
                                            }
                                        }
                                    }
                                    if (item instanceof ClassActionItem) {
                                        ClassActionItem cai = (ClassActionItem) item;
                                        final String className = String.join(".", getMembersPath(cai.className));
                                        classInheritance.put(className, new ArrayList<>());
                                        if (!classTraits.containsKey(className)) {
                                            classTraits.put(className, new LinkedHashMap<>());
                                        }
                                        for (int i = 0; i < cai.traits.size(); i++) {
                                            MyEntry<GraphTargetItem, GraphTargetItem> en = cai.traits.get(i);
                                            if (!(en.getKey() instanceof DirectValueActionItem)) {
                                                continue;
                                            }
                                            DirectValueActionItem dv = (DirectValueActionItem) en.getKey();
                                            String name = dv.getAsString();
                                            GraphTargetItem value = en.getValue();
                                            boolean isStatic = cai.traitsStatic.get(i);
                                            if (value instanceof FunctionActionItem) {
                                                if (name.startsWith("__get__") || name.startsWith("__set__")) {
                                                    String vname = name.substring(7);
                                                    Variable v = new Variable(isStatic, vname, vname, className);
                                                    classTraits.get(className).put(vname, v);
                                                }
                                                Method m = new Method(isStatic, name, "Unknown" /*FIXME?*/, className);
                                                classTraits.get(className).put(name, m);
                                            } else {
                                                Variable v = new Variable(isStatic, name, name, className);
                                                classTraits.get(className).put(name, v);
                                            }
                                        }
                                        if (cai.extendsOp != null) {
                                            String parentClassName = String.join(".", getMembersPath(cai.extendsOp));
                                            classInheritance.get(className).add(parentClassName);
                                        } else {
                                            classInheritance.get(className).add("Object");
                                        }
                                        if (cai.implementsOp != null) {
                                            for (GraphTargetItem imp : cai.implementsOp) {
                                                String imtName = String.join(".", getMembersPath(imp));
                                                classInheritance.get(className).add(imtName);
                                            }
                                        }
                                    }
                                }

                                //Detect this.x assigns
                                for (GraphTargetItem item : tree) {
                                    if (item instanceof ClassActionItem) {
                                        ClassActionItem cai = (ClassActionItem) item;
                                        final String className = String.join(".", getMembersPath(cai.className));
                                        for (int i = 0; i < cai.traits.size(); i++) {
                                            MyEntry<GraphTargetItem, GraphTargetItem> en = cai.traits.get(i);
                                            if (!(en.getKey() instanceof DirectValueActionItem)) {
                                                continue;
                                            }
                                            GraphTargetItem value = en.getValue();
                                            if (value instanceof GraphTargetItem) {
                                                AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
                                                    @Override
                                                    public boolean visit(GraphTargetItem item) {
                                                        List<String> path = getFullPath(item);
                                                        if (path != null) {
                                                            List<String> parent = new ArrayList<>(path);
                                                            parent.remove(parent.size() - 1);
                                                            if (parent.size() == 1) {
                                                                if (parent.get(0).equals("this")) {
                                                                    String name = path.get(path.size() - 1);
                                                                    if (!containsTrait(classTraits, classInheritance, className, name) && (!resultInstance.containsKey(className) || !resultInstance.get(className).containsKey(name))) {
                                                                        Variable v = new Variable(false, name, null, className);
                                                                        if (!resultInstance.containsKey(className)) {
                                                                            resultInstance.put(className, new TreeMap<>(new NaturalOrderComparator()));
                                                                        }
                                                                        resultInstance.get(className).put(name, v);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        return true;
                                                    }
                                                };
                                                visitor.visit(value);
                                                value.visitRecursively(visitor);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    });

            if (worker != null && worker.isCancelled()) {
                throw new InterruptedException();
            }

            //Complete inheritance tree
            for (String className : classInheritance.keySet()) {
                for (int i = 0; i < classInheritance.get(className).size(); i++) {
                    String parentClass = classInheritance.get(className).get(i);
                    if (classInheritance.containsKey(parentClass)) {
                        for (String p : classInheritance.get(parentClass)) {
                            if (!classInheritance.get(className).contains(p)) {
                                classInheritance.get(className).add(p);
                            }
                        }
                    }
                }
            }

            if (worker != null && worker.isCancelled()) {
                throw new InterruptedException();
            }

            //getting static classname.x assigns
            asms.entrySet().parallelStream().forEach(entry -> {
                if (worker != null && worker.isCancelled()) {
                    return;
                }
                subThreads.add(Thread.currentThread());
                fireProgress(entry.getValue(), entry.getKey());
                String key = entry.getKey();
                ASMSource asm = asms.get(key);
                //fireProgress(asm, key);                    
                List<GraphTargetItem> tree = new ArrayList<>();
                if (cachedTrees.containsKey(key)) {
                    tree = cachedTrees.get(key);
                } else {
                    try {
                        tree = asm.getActionsToTree();
                    } catch (Throwable t) {
                        //ignore
                    }
                }
                for (GraphTargetItem item : tree) {
                    AbstractGraphTargetVisitor visitor = new AbstractGraphTargetVisitor() {
                        @Override
                        public boolean visit(GraphTargetItem item) {
                            if ((item instanceof SetMemberActionItem)
                                    || (item instanceof CallMethodActionItem)
                                    || (item instanceof NewMethodActionItem)
                                    || (item instanceof DeleteActionItem)
                                    || (item instanceof GetMemberActionItem)) {
                                List<String> path = getFullPath(item);
                                if (path != null) {
                                    List<String> parent = new ArrayList<>(path);
                                    parent.remove(parent.size() - 1);
                                    String name = path.get(path.size() - 1);

                                    String className = String.join(".", parent);
                                    if (classInheritance.containsKey(className)) {
                                        //it's a class
                                        if (!containsTrait(classTraits, classInheritance, className, name)
                                                && (!resultInstance.containsKey(className) || !resultInstance.get(className).containsKey(name))
                                                && (!resultStatic.containsKey(className) || !resultStatic.get(className).containsKey(name))) {
                                            if (!resultStatic.containsKey(className)) {
                                                resultStatic.put(className, new TreeMap<>(new NaturalOrderComparator()));
                                            }
                                            Variable v = new Variable(true, name, null, className);
                                            resultStatic.get(className).put(name, v);
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                    };
                    visitor.visit(item);
                    item.visitRecursively(visitor);
                }
            });

            if (worker != null && worker.isCancelled()) {
                throw new InterruptedException();
            }


            /*for (String cls:result.keySet()) {
                System.err.println("class "+cls);
                for(String name:result.get(cls).keySet()) {
                    System.err.println("- " +result.get(cls).get(name));
                }
            }*/
            Map<String, Map<String, Trait>> result = new LinkedHashMap<>();
            for (String className : resultInstance.keySet()) {
                for (String traitName : resultInstance.get(className).keySet()) {
                    if (!result.containsKey(className)) {
                        result.put(className, new LinkedHashMap<>());
                    }
                    result.get(className).put(traitName, resultInstance.get(className).get(traitName));
                }
            }
            for (String className : resultStatic.keySet()) {
                for (String traitName : resultStatic.get(className).keySet()) {
                    if (!result.containsKey(className)) {
                        result.put(className, new LinkedHashMap<>());
                    }
                    result.get(className).put(traitName, resultStatic.get(className).get(traitName));
                }
            }
            return result;
        } finally {
            //Removed cached version of classes - allow reparsing using detected uninitialized fields
            for (String key : asms.keySet()) {
                ASMSource asm = asms.get(key);
                if (asm instanceof DoInitActionTag) {
                    DoInitActionTag doi = (DoInitActionTag) asm;
                    String exportName = doi.getSwf().getCharacter(doi.getCharacterId()).getExportName();
                    if (exportName != null && exportName.startsWith("__Packages.")) {
                        SWF.uncache(doi);
                    }
                }
            }
            if (worker != null) {
                worker.removeCancelListener(cancelListener);
            }
        }
    }
}
