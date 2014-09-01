/*
 *  Copyright (C) 2012-2014 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.treeitems.TreeElementItem;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public class ScriptPack implements TreeElementItem {

    public ABC abc;
    public int scriptIndex;
    public List<Integer> traitIndices;
    private final ClassPath path;

    @Override
    public SWF getSwf() {
        return abc.swf;
    }

    public ClassPath getPath() {
        return path;
    }

    public ScriptPack(ClassPath path, ABC abc, int scriptIndex, List<Integer> traitIndices) {
        this.abc = abc;
        this.scriptIndex = scriptIndex;
        this.traitIndices = traitIndices;
        this.path = path;
    }

    public String getPathPackage() {
        String packageName = "";
        for (int t : traitIndices) {
            Multiname name = abc.script_info.get(scriptIndex).traits.traits.get(t).getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                packageName = ns.getName(abc.constants); //assume not null
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
                scriptName = name.getName(abc.constants, new ArrayList<String>(), false);
            }
        }
        return scriptName;
    }

    /*public String getPath() {
     String packageName = "";
     String scriptName = "";
     for (int t : traitIndices) {
     Multiname name = abc.script_info[scriptIndex].traits.traits.get(t).getName(abc);
     Namespace ns = name.getNamespace(abc.constants);
     if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
     packageName = ns.getName(abc.constants);
     scriptName = name.getName(abc.constants, new ArrayList<String>());
     }
     }
     return packageName.equals("") ? scriptName : packageName + "." + scriptName;
     }*/
    private static String makeDirPath(String packageName) {
        if (packageName.isEmpty()) {
            return "";
        }
        String[] pathParts;
        if (packageName.contains(".")) {
            pathParts = packageName.split("\\.");
        } else {
            pathParts = new String[]{packageName};
        }
        for (int i = 0; i < pathParts.length; i++) {
            pathParts[i] = Helper.makeFileName(pathParts[i]);
        }
        return Helper.joinStrings(pathParts, File.separator);
    }

    public void convert(final NulWriter writer, final List<ABCContainerTag> abcList, final List<Trait> traits, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {
        for (int t : traitIndices) {
            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.convertPackaged(null, "", abcList, abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<String>(), parallel);
            } else {
                trait.convert(null, "", abcList, abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<String>(), parallel);
            }
        }
    }

    public void appendTo(GraphTextWriter writer, List<ABCContainerTag> abcList, List<Trait> traits, ScriptExportMode exportMode, boolean parallel) throws InterruptedException {
        for (int t : traitIndices) {
            Trait trait = traits.get(t);
            Multiname name = trait.getName(abc);
            Namespace ns = name.getNamespace(abc.constants);
            if ((ns.kind == Namespace.KIND_PACKAGE) || (ns.kind == Namespace.KIND_PACKAGE_INTERNAL)) {
                trait.toStringPackaged(null, "", abcList, abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<String>(), parallel);
            } else {
                trait.toString(null, "", abcList, abc, false, exportMode, scriptIndex, -1, writer, new ArrayList<String>(), parallel);
            }
        }
    }

    public void toSource(GraphTextWriter writer, final List<ABCContainerTag> abcList, final List<Trait> traits, final ScriptExportMode exportMode, final boolean parallel) throws InterruptedException {
        writer.suspendMeasure();
        int timeout = Configuration.decompilationTimeoutFile.get();
        try {
            CancellableWorker.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    convert(new NulWriter(), abcList, traits, exportMode, parallel);
                    return null;
                }
            }, timeout, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            writer.continueMeasure();
            Logger.getLogger(ScriptPack.class.getName()).log(Level.SEVERE, "Decompilation error", ex);
            Helper.appendTimeoutComment(writer, timeout);
            return;
        } catch (ExecutionException ex) {
            writer.continueMeasure();
            Logger.getLogger(ScriptPack.class.getName()).log(Level.SEVERE, "Decompilation error", ex);
            Helper.appendErrorComment(writer, ex);
            return;
        }
        writer.continueMeasure();

        appendTo(writer, abcList, traits, exportMode, parallel);
    }

    public File export(String directory, List<ABCContainerTag> abcList, ScriptExportMode exportMode, boolean parallel) throws IOException {
        String scriptName = getPathScriptName();
        String packageName = getPathPackage();
        File outDir = new File(directory + File.separatorChar + makeDirPath(packageName));
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                if (!outDir.exists()) {
                    throw new IOException("cannot create directory " + outDir);
                }
            }
        }
        String fileName = outDir.toString() + File.separator + Helper.makeFileName(scriptName) + ".as";

        File file = new File(fileName);
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            try {
                toSource(writer, abcList, abc.script_info.get(scriptIndex).traits.traits, exportMode, parallel);
            } catch (InterruptedException ex) {
                Logger.getLogger(ScriptPack.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file;
    }
}
