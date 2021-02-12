/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class Traits implements Cloneable, Serializable {

    public List<Trait> traits;

    public Traits() {
        traits = new ArrayList<>();
    }

    public Traits(int initialCapacity) {
        traits = new ArrayList<>(initialCapacity);
    }

    public void delete(ABC abc, boolean d) {
        for (Trait t : traits) {
            t.delete(abc, d);
        }
    }

    public int addTrait(Trait t) {
        traits.add(t);
        return traits.size() - 1;
    }

    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int ret = 0;
        for (Trait t : traits) {
            ret += t.removeTraps(scriptIndex, classIndex, isStatic, abc, path);
        }
        return ret;
    }

    @Override
    public String toString() {
        String s = "";
        for (int t = 0; t < traits.size(); t++) {
            if (t > 0) {
                s += "\r\n";
            }
            s += traits.get(t).toString();
        }
        return s;
    }

    public String toString(ABC abc, List<DottedChain> fullyQualifiedNames) {
        String s = "";
        for (int t = 0; t < traits.size(); t++) {
            if (t > 0) {
                s += "\r\n";
            }
            s += traits.get(t).toString(abc, fullyQualifiedNames);
        }
        return s;
    }

    private class TraitConvertTask implements Callable<Void> {

        Trait trait;

        boolean makePackages;

        String path;

        ABC abc;

        boolean isStatic;

        ScriptExportMode exportMode;

        int scriptIndex;

        int classIndex;

        NulWriter writer;

        List<DottedChain> fullyQualifiedNames;

        int traitIndex;

        boolean parallel;

        Trait parent;

        ConvertData convertData;

        public TraitConvertTask(Trait trait, Trait parent, ConvertData convertData, boolean makePackages, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, int traitIndex, boolean parallel) {
            this.trait = trait;
            this.parent = parent;
            this.convertData = convertData;
            this.makePackages = makePackages;
            this.path = path;
            this.abc = abc;
            this.isStatic = isStatic;
            this.exportMode = exportMode;
            this.scriptIndex = scriptIndex;
            this.classIndex = classIndex;
            this.writer = writer;
            this.fullyQualifiedNames = fullyQualifiedNames;
            this.traitIndex = traitIndex;
            this.parallel = parallel;
        }

        @Override
        public Void call() throws InterruptedException {
            if (makePackages) {
                trait.convertPackaged(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            } else {
                trait.convert(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            }
            return null;
        }
    }

    public GraphTextWriter toString(Class[] traitTypes, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {

        List<Trait> ordered = new ArrayList<>(traits);
        loopi:
        for (int i = 0; i < ordered.size(); i++) {
            for (int j = i + 1; j < ordered.size(); j++) {
                if (i == j) {
                    continue;
                }
                Trait o1 = ordered.get(i);
                Trait o2 = ordered.get(j);
                Multiname m2 = abc.constants.getMultiname(o2.name_index);
                if (!convertData.assignedValues.containsKey(o1)) {
                    continue;
                }
                GraphTargetItem v1 = convertData.assignedValues.get(o1).value;


                Set<GraphTargetItem> subitems1 = v1.getAllSubItemsRecursively();
                subitems1.add(v1);
                for (GraphTargetItem si : subitems1) {
                    if (si instanceof GetPropertyAVM2Item) {
                        GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) si;
                        Multiname sm1 = abc.constants.getMultiname(((FullMultinameAVM2Item) getProp.propertyName).multinameIndex);
                        if (getProp.object instanceof FindPropertyAVM2Item && sm1.equals(m2)) {
                            ordered.add(j + 1, o1);
                            ordered.remove(i);
                            i--;
                            continue loopi;
                        }
                    }
                    if (si instanceof GetLexAVM2Item) {
                        GetLexAVM2Item lex = (GetLexAVM2Item) si;
                        if (lex.propertyName.equals(m2)) {
                            ordered.add(j + 1, o1);
                            ordered.remove(i);
                            i--;
                            continue loopi;
                        }
                    }
                }
            }
        }

        for (Trait trait : ordered) {
            int t = traits.indexOf(trait);
            if (traitTypes != null) {
                boolean found = false;
                for (Class c : traitTypes) {
                    if (c.isInstance(trait)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }
            if (!trait.isVisible(isStatic, abc)) {
                continue;
            }
            writer.newLine();
            int h = abc.getGlobalTraitId(TraitType.METHOD /*non-initializer*/, isStatic, classIndex, t);
            if (trait instanceof TraitClass) {
                writer.startClass(((TraitClass) trait).class_info);
            } else {
                writer.startTrait(h);
            }
            if (makePackages) {
                trait.toStringPackaged(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            } else {
                trait.toString(parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel);
            }
            if (trait instanceof TraitClass) {
                writer.endClass();
            } else {
                writer.endTrait();
            }
        }
        return writer;
    }

    public void convert(Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel) throws InterruptedException {
        if (!parallel || traits.size() < 2) {
            for (int t = 0; t < traits.size(); t++) {
                TraitConvertTask task = new TraitConvertTask(traits.get(t), parent, convertData, makePackages, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, t, parallel);
                task.call();
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.getParallelThreadCount());
            List<Future<Void>> futureResults;

            futureResults = new ArrayList<>();
            for (int t = 0; t < traits.size(); t++) {
                // each convert task needs a separate NulWriter, because they are executed parallel
                TraitConvertTask task = new TraitConvertTask(traits.get(t), parent, convertData, makePackages, path, abc, isStatic, exportMode, scriptIndex, classIndex, new NulWriter(), fullyQualifiedNames, t, parallel);
                Future<Void> future = executor.submit(task);
                futureResults.add(future);
            }

            for (int f = 0; f < futureResults.size(); f++) {
                try {
                    futureResults.get(f).get();
                } catch (InterruptedException ex) {
                    executor.shutdownNow();
                    throw ex;
                } catch (ExecutionException ex) {
                    Logger.getLogger(Traits.class.getName()).log(Level.SEVERE, "Error during traits converting", ex);
                }
            }
            executor.shutdown();
        }
    }

    @Override
    public Traits clone() {
        try {
            Traits ret = (Traits) super.clone();

            if (traits != null) {
                ret.traits = new ArrayList<>(traits.size());
                for (int i = 0; i < traits.size(); i++) {
                    ret.traits.add(traits.get(i).clone());
                }
            }

            return ret;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    public void getDependencies(int scriptIndex, int classIndex, boolean isStatic, String customNs, ABC abc, List<Dependency> dependencies, List<String> uses, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        for (Trait t : traits) {
            t.getDependencies(scriptIndex, classIndex, isStatic, customNs, abc, dependencies, uses, ignorePackage, fullyQualifiedNames);
        }
    }

    public void getMethodInfos(ABC abc, boolean isStatic, int classIndex, List<MethodId> methodInfos) {
        for (int t = 0; t < traits.size(); t++) {
            Trait trait = traits.get(t);
            trait.getMethodInfos(abc, abc.getGlobalTraitId(TraitType.METHOD /*non-initializer*/, isStatic, classIndex, t), classIndex, methodInfos);
        }
    }
}
