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
package com.jpexs.decompiler.flash.abc.types.traits;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.search.MethodId;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Reference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a list of traits in ABC file.
 *
 * @author JPEXS
 */
public class Traits implements Cloneable, Serializable {

    /**
     * List of traits
     */
    public List<Trait> traits;

    /**
     * Constructs a new Traits object.
     */
    public Traits() {
        traits = new ArrayList<>();
    }

    /**
     * Constructs a new Traits object with the specified initial capacity.
     *
     * @param initialCapacity Initial capacity
     */
    public Traits(int initialCapacity) {
        traits = new ArrayList<>(initialCapacity);
    }

    /**
     * Deletes traits.
     *
     * @param abc ABC file
     * @param d Delete flag
     */
    public void delete(ABC abc, boolean d) {
        for (Trait t : traits) {
            t.delete(abc, d);
        }
    }

    /**
     * Adds a trait to the list.
     *
     * @param t Trait to add
     * @return Index of the added trait
     */
    public int addTrait(Trait t) {
        traits.add(t);
        return traits.size() - 1;
    }

    /**
     * Removes traps - deobfuscation.
     *
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static
     * @param abc ABC file
     * @param path Path
     * @return Number of removed traps
     * @throws InterruptedException On interrupt
     */
    public int removeTraps(int scriptIndex, int classIndex, boolean isStatic, ABC abc, String path) throws InterruptedException {
        int ret = 0;
        for (Trait t : traits) {
            ret += t.removeTraps(scriptIndex, classIndex, isStatic, abc, path);
        }
        return ret;
    }

    /**
     * To string.
     *
     * @return String representation
     */
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

    /**
     * To string.
     *
     * @param abc ABC file
     * @param fullyQualifiedNames Fully qualified names
     * @return String representation
     */
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

    /**
     * Conversion task.
     */
    private class TraitConvertTask implements Callable<Void> {

        /**
         * Trait
         */
        Trait trait;

        /**
         * Make packages flag
         */
        boolean makePackages;

        /**
         * Path
         */
        String path;

        /**
         * ABC file
         */
        ABC abc;

        /**
         * Is static flag
         */
        boolean isStatic;

        /**
         * Export mode
         */
        ScriptExportMode exportMode;

        /**
         * Script index
         */
        int scriptIndex;

        /**
         * Class index
         */
        int classIndex;

        /**
         * Writer
         */
        NulWriter writer;

        /**
         * Fully qualified names
         */
        List<DottedChain> fullyQualifiedNames;

        /**
         * Trait index
         */
        int traitIndex;

        /**
         * Parallel flag
         */
        boolean parallel;

        /**
         * Parent trait
         */
        Trait parent;

        /**
         * Convert data
         */
        ConvertData convertData;
        private final int swfVersion;

        /**
         * ABC indexing
         */
        AbcIndexing abcIndex;

        /**
         * Scope stack
         */
        ScopeStack scopeStack;

        /**
         * Constructs a new TraitConvertTask object.
         *
         * @param swfVersion SWF version
         * @param abcIndex ABC indexing
         * @param trait Trait
         * @param parent Parent trait
         * @param convertData Convert data
         * @param makePackages Make packages flag
         * @param path Path
         * @param abc ABC file
         * @param isStatic Is static flag
         * @param exportMode Export mode
         * @param scriptIndex Script index
         * @param classIndex Class index
         * @param writer Writer
         * @param fullyQualifiedNames Fully qualified names
         * @param traitIndex Trait index
         * @param parallel Parallel flag
         * @param scopeStack Scope stack
         */
        public TraitConvertTask(int swfVersion, AbcIndexing abcIndex, Trait trait, Trait parent, ConvertData convertData, boolean makePackages, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, int traitIndex, boolean parallel, ScopeStack scopeStack) {
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
            this.swfVersion = swfVersion;
            this.abcIndex = abcIndex;
            this.scopeStack = scopeStack;
        }

        /**
         * Calls the task.
         *
         * @return Null
         * @throws InterruptedException On interrupt
         */
        @Override
        public Void call() throws InterruptedException {
            if (makePackages) {
                trait.convertPackaged(swfVersion, abcIndex, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, scopeStack);
            } else {
                trait.convert(swfVersion, abcIndex, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, scopeStack);
            }
            return null;
        }
    }

    /**
     * To string.
     *
     * @param swfVersion SWF version
     * @param packageName Package name
     * @param first Whether to add newline
     * @param abcIndex ABC indexing
     * @param traitTypes Trait types
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC file
     * @param isStatic Is static flag
     * @param exportMode Export mode
     * @param makePackages Make packages flag
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel flag
     * @param ignoredTraitNames Ignored trait names
     * @param insideInterface Inside interface flag
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    public GraphTextWriter toString(int swfVersion, DottedChain packageName, Reference<Boolean> first, AbcIndexing abcIndex, Class[] traitTypes, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, GraphTextWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, List<String> ignoredTraitNames, boolean insideInterface) throws InterruptedException {
        List<Trait> ordered = traits;

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
            if (ignoredTraitNames.contains(trait.getName(abc).getName(abc.constants, new ArrayList<>(), false, false))) {
                continue;
            }

            if ((trait instanceof TraitSlotConst) && convertData.assignedValues.containsKey((TraitSlotConst) trait) && isStatic) {
                continue;
            }

            if (!first.getVal()) {
                writer.newLine();
            }
            first.setVal(false);
            int h = abc.getGlobalTraitId(TraitType.METHOD, isStatic, classIndex, t);
            writer.startTrait(h);
            if (makePackages) {
                trait.toStringPackaged(swfVersion, abcIndex, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
            } else {
                trait.toString(swfVersion, abcIndex, packageName, parent, convertData, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, parallel, insideInterface);
            }
            writer.endTrait();
        }
        return writer;
    }

    /**
     * Converts traits.
     *
     * @param swfVersion SWF version
     * @param abcIndex ABC indexing
     * @param parent Parent trait
     * @param convertData Convert data
     * @param path Path
     * @param abc ABC file
     * @param isStatic Is static flag
     * @param exportMode Export mode
     * @param makePackages Make packages flag
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param writer Writer
     * @param fullyQualifiedNames Fully qualified names
     * @param parallel Parallel flag
     * @param scopeStack Scope stack
     * @throws InterruptedException On interrupt
     */
    public void convert(int swfVersion, AbcIndexing abcIndex, Trait parent, ConvertData convertData, String path, ABC abc, boolean isStatic, ScriptExportMode exportMode, boolean makePackages, int scriptIndex, int classIndex, NulWriter writer, List<DottedChain> fullyQualifiedNames, boolean parallel, ScopeStack scopeStack) throws InterruptedException {
        if (!parallel || traits.size() < 2) {
            for (int t = 0; t < traits.size(); t++) {
                TraitConvertTask task = new TraitConvertTask(swfVersion, abcIndex, traits.get(t), parent, convertData, makePackages, path, abc, isStatic, exportMode, scriptIndex, classIndex, writer, fullyQualifiedNames, t, parallel, scopeStack);
                task.call();
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(Configuration.getParallelThreadCount());
            List<Future<Void>> futureResults;

            futureResults = new ArrayList<>();
            for (int t = 0; t < traits.size(); t++) {
                // each convert task needs a separate NulWriter, because they are executed parallel
                TraitConvertTask task = new TraitConvertTask(swfVersion, abcIndex, traits.get(t), parent, convertData, makePackages, path, abc, isStatic, exportMode, scriptIndex, classIndex, new NulWriter(), fullyQualifiedNames, t, parallel, scopeStack);
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

    /**
     * Clones the traits.
     *
     * @return Cloned traits
     */
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

    /**
     * Gets dependencies.
     *
     * @param abcIndex ABC indexing
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param isStatic Is static flag
     * @param customNs Custom namespace
     * @param abc ABC file
     * @param dependencies Dependencies
     * @param ignorePackage Ignore package
     * @param fullyQualifiedNames Fully qualified names
     * @param uses Uses
     * @throws InterruptedException On interrupt
     */
    public void getDependencies(AbcIndexing abcIndex, int scriptIndex, int classIndex, boolean isStatic, String customNs, ABC abc, List<Dependency> dependencies, DottedChain ignorePackage, List<DottedChain> fullyQualifiedNames, List<String> uses, Reference<Integer> numberContextRef) throws InterruptedException {
        for (Trait t : traits) {
            t.getDependencies(abcIndex, scriptIndex, classIndex, isStatic, customNs, abc, dependencies, ignorePackage, fullyQualifiedNames, uses, numberContextRef);
        }
    }

    /**
     * Gets method infos.
     *
     * @param abc ABC file
     * @param isStatic Is static flag
     * @param classIndex Class index
     * @param methodInfos Method infos
     */
    public void getMethodInfos(ABC abc, boolean isStatic, int classIndex, List<MethodId> methodInfos) {
        for (int t = 0; t < traits.size(); t++) {
            Trait trait = traits.get(t);
            trait.getMethodInfos(abc, abc.getGlobalTraitId(TraitType.METHOD /*non-initializer*/, isStatic, classIndex, t), classIndex, methodInfos);
        }
    }
}
