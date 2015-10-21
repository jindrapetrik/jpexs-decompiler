/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ScriptPack extends AS3ClassTreeItem {

    private static final Logger logger = Logger.getLogger(ScriptPack.class.getName());

    public final ABC abc;

    public List<ABC> allABCs;

    public final int scriptIndex;

    public final List<Integer> traitIndices;

    private final ClassPath path;

    public boolean isSimple = false;

    public boolean scriptInitializerIsEmpty = false;

    @Override
    public SWF getSwf() {
        return abc.getSwf();
    }

    public ClassPath getClassPath() {
        return path;
    }

    public ScriptPack(ClassPath path, ABC abc, List<ABC> allAbcs, int scriptIndex, List<Integer> traitIndices) {
        super(path.className, path);
        this.abc = abc;
        this.scriptIndex = scriptIndex;
        this.traitIndices = traitIndices;
        this.path = path;
        this.allABCs = allAbcs;
    }

    public DottedChain getPathPackage() {
        DottedChain packageName = DottedChain.EMPTY;
        for (int t : traitIndices) {
            Multiname name = abc.script_info.get(scriptIndex).traits.traits.get(t).getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                packageName = ns.getName(abc.constants); // assume not null
            }
        }
        return packageName;
    }

    public String getPathScriptName() {
        String scriptName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info.get(scriptIndex).traits.traits.get(t).getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                scriptName = name.getName(abc.constants, null, false);
            }
        }
        return scriptName;
    }

    public File getExportFile(String directory, ScriptExportSettings exportSettings) {
        if (exportSettings.singleFile) {
            return null;
        }

        String scriptName = getPathScriptName();
        DottedChain packageName = getPathPackage();
        File outDir = new File(directory + File.separatorChar + packageName.toFilePath());
        String fileName = outDir.toString() + File.separator + Helper.makeFileName(scriptName) + exportSettings.getFileExtension();
        return new File(fileName);
    }

    /*public String getPath() {
     String packageName = "";
     String scriptName = "";
     for (int t : traitIndices) {
     Multiname name = abc.script_info[scriptIndex].traits.traits.get(t).getName(abc);
     Namespace ns = name.getNamespace(abc.constants);
     if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
     packageName = ns.getName(abc.constants);
     scriptName = name.getName(abc.constants, new ArrayList<>());
     }
     }
     return packageName.equals("") ? scriptName : packageName + "." + scriptName;
     }*/
    public void convert(final NulWriter writer, final List<Trait> traits, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {

        int script_init = abc.script_info.get(scriptIndex).init_index;
        int bodyIndex = abc.findBodyIndex(script_init);
        if (bodyIndex != -1) {
            List<Traits> ts = new ArrayList<>();
            //initialize all classes traits
            for (Trait t : traits) {
                if (t instanceof TraitClass) {
                    ts.add(abc.class_info.get(((TraitClass) t).class_info).static_traits);

                    for (Trait trait : abc.class_info.get(((TraitClass) t).class_info).static_traits.traits) {
                        if (trait instanceof TraitSlotConst) {
                            ((TraitSlotConst) trait).assignedValue = null;
                        }
                    }
                    for (Trait trait : abc.instance_info.get(((TraitClass) t).class_info).instance_traits.traits) {
                        if (trait instanceof TraitSlotConst) {
                            ((TraitSlotConst) trait).assignedValue = null;
                        }
                    }
                }
            }

            writer.mark();
            abc.bodies.get(bodyIndex).convert(path +/*packageName +*/ "/.scriptinitializer", exportMode, true, script_init, scriptIndex, -1, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_SCRIPT_INITIALIZER, writer, new ArrayList<DottedChain>(), ts, true);
            scriptInitializerIsEmpty = !writer.getMark();

        }
        for (int t : traitIndices) {
            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.convertPackaged(null, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            } else {
                trait.convert(null, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            }
        }
    }

    private void appendTo(GraphTextWriter writer, List<Trait> traits, ScriptExportMode exportMode, boolean parallel) throws InterruptedException {
        boolean first = true;
        //script initializer
        int script_init = abc.script_info.get(scriptIndex).init_index;
        int bodyIndex = abc.findBodyIndex(script_init);
        if (bodyIndex != -1 && Configuration.enableScriptInitializerDisplay.get()) {
            //Note: There must be trait/method highlight even if the initializer is empty to TraitList in GUI to work correctly
            //TODO: handle this better in GUI(?)
            writer.startTrait(GraphTextWriter.TRAIT_SCRIPT_INITIALIZER);
            writer.startMethod(script_init);
            if (!scriptInitializerIsEmpty) {
                writer.startBlock();
                abc.bodies.get(bodyIndex).toString(path +/*packageName +*/ "/.scriptinitializer", exportMode, abc, null, writer, new ArrayList<>());
                writer.endBlock();
            } else {
                writer.append(" ");
            }
            writer.endMethod();
            writer.endTrait();
            if (!scriptInitializerIsEmpty) {
                writer.newLine();
            }
            first = false;
        } else {
            //"/*classInitializer*/";
        }

        for (int t : traitIndices) {
            if (!first) {
                writer.newLine();
            }

            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.toStringPackaged(null, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            } else {
                trait.toString(null, "", abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<>(), parallel);
            }

            first = false;
        }
    }

    public void toSource(GraphTextWriter writer, final List<Trait> traits, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {
        writer.suspendMeasure();
        int timeout = Configuration.decompilationTimeoutFile.get();
        try {
            CancellableWorker.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    convert(new NulWriter(), traits, exportMode, parallel);
                    return null;
                }
            }, timeout, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            writer.continueMeasure();
            logger.log(Level.SEVERE, "Decompilation error", ex);
            Helper.appendTimeoutCommentAs3(writer, timeout, 0);
            return;
        } catch (ExecutionException ex) {
            writer.continueMeasure();
            logger.log(Level.SEVERE, "Decompilation error", ex);
            Helper.appendErrorComment(writer, ex);
            return;
        }
        writer.continueMeasure();

        appendTo(writer, traits, exportMode, parallel);
    }

    public File export(File file, ScriptExportSettings exportSettings, boolean parallel) throws IOException, InterruptedException {
        if (!exportSettings.singleFile) {
            if (file.exists() && !Configuration.overwriteExistingFiles.get()) {
                return file;
            }
        }

        Path.createDirectorySafe(file.getParentFile());

        try (FileTextWriter writer = exportSettings.singleFile ? null : new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            FileTextWriter writer2 = exportSettings.singleFile ? exportSettings.singleFileWriter : writer;
            toSource(writer2, abc.script_info.get(scriptIndex).traits.traits, exportSettings.mode, parallel);
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, "The file path is probably too long", ex);
        }

        return file;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(abc);
        hash = 79 * hash + scriptIndex;
        hash = 79 * hash + Objects.hashCode(path);
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
        final ScriptPack other = (ScriptPack) obj;
        if (!Objects.equals(abc, other.abc)) {
            return false;
        }
        if (scriptIndex != other.scriptIndex) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isModified() {
        return abc.script_info.get(scriptIndex).isModified();
    }
}
