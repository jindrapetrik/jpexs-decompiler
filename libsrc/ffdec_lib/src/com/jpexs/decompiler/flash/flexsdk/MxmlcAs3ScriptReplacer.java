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
package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.swf.SwfToSwcExporter;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceException;
import com.jpexs.decompiler.flash.importers.As3ScriptReplaceExceptionItem;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces AS3 script in SWF using MXMLC compiler.
 */
public class MxmlcAs3ScriptReplacer extends MxmlcRunner implements As3ScriptReplacerInterface {

    private ScriptPack initedPack;
    private File tempDir;
    private File pkgDir;
    private File swcFile;
    private List<File> dependenciesSwcFiles = new ArrayList<>();

    /**
     * Constructor.
     * @param flexSdkPath Path to Flex SDK.
     */
    public MxmlcAs3ScriptReplacer(String flexSdkPath) {
        super(flexSdkPath);
    }

    private synchronized boolean isInited(ScriptPack pack) {
        return tempDir != null && initedPack == pack;
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private SWF recompileSWF(SWF swf) throws IOException, InterruptedException {
        ByteArrayOutputStream swfOrigBaos = new ByteArrayOutputStream();
        swf.saveTo(swfOrigBaos);
        return new SWF(new ByteArrayInputStream(swfOrigBaos.toByteArray()), Configuration.parallelSpeedUp.get(), false);
    }

    private boolean isParentDeleted(ABC abc, List<ABC> allAbcs, DottedChain className) {
        int class_index = abc.findClassByName(className);
        if (class_index == -1) {
            for (ABC a : allAbcs) {
                class_index = abc.findClassByName(className);
                if (class_index != -1) {
                    abc = a;
                    break;
                }
            }
            if (class_index == -1) {
                return false;
            }
        }
        InstanceInfo ii = abc.instance_info.get(class_index);
        if (ii.deleted) {
            return true;
        }
        if (ii.super_index != 0 && isParentDeleted(abc, allAbcs, abc.constants.getMultiname(ii.super_index).getNameWithNamespace(abc.constants, false))) {
            return true;
        }
        for (int iface : ii.interfaces) {
            if (isParentDeleted(abc, allAbcs, abc.constants.getMultiname(iface).getNameWithNamespace(abc.constants, false))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void replaceScript(ScriptPack pack, String text, List<SWF> dependencies) throws As3ScriptReplaceException, IOException, InterruptedException {
        if (!pack.isSimple) {
            throw new IOException("Cannot compile such file"); //Alchemy, etc.  TODO: handle better            
        }

        if (!isInited(pack)) {
            initReplacement(pack, dependencies);
        }

        File scriptFileToCompile = new File(pkgDir, pack.getClassPath().className + ".as");
        //Write new script
        Helper.writeFile(scriptFileToCompile.getAbsolutePath(), text.getBytes("UTF-8"));
        File compiledSwfFile = new File(pkgDir, "out.swf");

        try {
            //Compile it (and subclasses stubs)
            List<String> args = new ArrayList<>(Arrays.asList("-strict=false",
                    "-include-inheritance-dependencies-only",
                    "-warnings=false",
                    "-library-path", swcFile.getAbsolutePath()));

            for (File depSwcFile : dependenciesSwcFiles) {
                args.add("-library-path");
                args.add(depSwcFile.getAbsolutePath());
            }

            args.addAll(Arrays.asList("-source-path", tempDir.getAbsolutePath(),
                    "-output", compiledSwfFile.getAbsolutePath(),
                    "-debug=true",
                    scriptFileToCompile.getAbsolutePath()));
            mxmlc(args);
        } catch (MxmlcException ex1) {
            //String compiledFilePath = scriptFileToCompile.getAbsolutePath();
            Pattern errPattern = Pattern.compile("^" + Pattern.quote(tempDir.getAbsolutePath()) + "(?<file>.*)\\((?<line>[0-9]+)\\): col: (?<col>[0-9]+) (?<message>.*)$");
            String err = ex1.getMxmlcErrorOutput();
            String[] errLines = err.split("\r?\n");
            List<As3ScriptReplaceExceptionItem> errorItems = new ArrayList<>();
            for (int i = 0; i < errLines.length; i++) {
                String line = errLines[i].trim();
                Matcher m = errPattern.matcher(line);
                if (m.matches()) {
                    String errFile = m.group("file");
                    int errLine = Integer.parseInt(m.group("line"));
                    int errCol = Integer.parseInt(m.group("col"));
                    String errMsg = m.group("message");
                    errorItems.add(new As3ScriptReplaceExceptionItem(errFile, errMsg, errLine, errCol));
                }
            }
            As3ScriptReplaceException ex;
            if (errorItems.isEmpty()) {
                ex = new As3ScriptReplaceException(err);
            } else {
                ex = new As3ScriptReplaceException(errorItems);
            }
            throw ex;
        }

        try (FileInputStream fis = new FileInputStream(compiledSwfFile)) {
            SWF newSWF = new SWF(fis, false, false);
            List<ABCContainerTag> newTags = newSWF.getAbcList();
            int oldScriptIndex = pack.scriptIndex;
            int oldClassIndex = -1;

            ScriptInfo oldScriptInfo = pack.abc.script_info.get(pack.scriptIndex);
            for (Trait t : oldScriptInfo.traits.traits) {
                if (t instanceof TraitClass) {
                    int traitClassIndex = ((TraitClass) t).class_info;
                    if (oldClassIndex == -1 || traitClassIndex < oldClassIndex) {
                        oldClassIndex = traitClassIndex;
                    }
                }
            }
            if (pack.isSimple) {
                oldScriptInfo.delete(pack.abc, true);
            } else {
                //NOO
            }
            pack.abc.pack(); // removes old classes/methods/scripts
            ABCContainerTag newTagsLast = newTags.get(newTags.size() - 1);
            ABC newLastAbc = newTagsLast.getABC();
            Map<Integer, Integer> classesMap = new HashMap<>();
            Map<Integer, Integer> scriptsMap = new HashMap<>();

            pack.abc.mergeABC(newLastAbc,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>(),
                    classesMap,
                    new HashMap<>(),
                    scriptsMap
            );

            //Reorder newly created scripts to be in place
            //where old script was
            List<Integer> addedScriptIndices = new ArrayList<>(scriptsMap.values());
            Collections.sort(addedScriptIndices);
            List<ScriptInfo> addedScripts = new ArrayList<>();
            for (int i = addedScriptIndices.size() - 1; i >= 0; i--) {
                int newScriptIndex = addedScriptIndices.get(i);
                addedScripts.add(0, pack.abc.script_info.remove(newScriptIndex));
            }
            for (int i = 0; i < addedScripts.size(); i++) {
                pack.abc.script_info.add(oldScriptIndex + i, addedScripts.get(i));
            }

            //IMPORTANT: Map newly created classes to their position as they
            //were in original script because FlashPlayer needs
            //parent class to be defined earlier
            if (oldClassIndex > -1) {
                List<Integer> addedClassIndices = new ArrayList<>(classesMap.values());
                Collections.sort(addedClassIndices);
                int totalClassCount = pack.abc.class_info.size();
                Map<Integer, Integer> classesRemap = new HashMap<>();
                for (int i = 0; i < addedClassIndices.size(); i++) {
                    classesRemap.put(addedClassIndices.get(i), oldClassIndex + i);
                }
                int mappingStart = oldClassIndex;
                for (int i = oldClassIndex; i < totalClassCount; i++) {
                    if (!classesRemap.containsKey(i)) {
                        for (int j = mappingStart; j < totalClassCount; j++) {
                            if (!classesRemap.containsValue(j)) {
                                classesRemap.put(i, j);
                                mappingStart = j + 1;
                                break;
                            }
                        }
                    }
                }
                pack.abc.reorganizeClasses(classesRemap);
            }
            ((Tag) pack.abc.parentTag).setModified(true);
            pack.abc.getSwf().getAbcIndex().refreshAbc(pack.abc);
            deinitReplacement(pack); //successful finish
        }

    }

    @Override
    public boolean isAvailable() {
        String flexLocation = Configuration.flexSdkLocation.get();
        return !(flexLocation.isEmpty() || MxmlcRunner.getMxmlcPath(flexLocation) == null);
    }

    @Override
    public synchronized void initReplacement(ScriptPack pack, List<SWF> dependencies) {
        if (tempDir != null) {
            deinitReplacement(pack);
        }
        try {
            tempDir = Files.createTempDirectory("ffdec-mxmlc-replace").toFile();
            pkgDir = tempDir;
            for (String pkgPart : pack.getClassPath().packageStr.toList()) {
                if (!pkgPart.isEmpty()) {
                    pkgDir = new File(pkgDir, pkgPart);
                }
            }
            pkgDir.mkdirs();

            swcFile = new File(tempDir, "out.swc");

            //Make copy without the old script
            Openable openable = pack.getOpenable();
            SWF swf = (openable instanceof SWF) ? (SWF) openable : ((ABC) openable).getSwf();
            SWF swfCopy = recompileSWF(swf);

            List<ABC> modAbcs = new ArrayList<>();

            List<ScriptPack> copyPacks = swfCopy.getAS3Packs();
            for (ScriptPack sp : copyPacks) {
                if (sp.getClassPath().equals(pack.getClassPath())) {
                    sp.abc.script_info.get(sp.scriptIndex).delete(sp.abc, true);
                    ((Tag) sp.abc.parentTag).setModified(true);
                    modAbcs.add(sp.abc);
                    break;
                }
            }

            List<ScriptPack> removedPacks = new ArrayList<>();

            //remove all subclasses from the SWC
            for (ScriptPack sp : copyPacks) {
                DottedChain dc = sp.getPathPackage().add(sp.getPathScriptName(), "");
                if (isParentDeleted(sp.abc, sp.allABCs, dc)) {
                    sp.abc.script_info.get(sp.scriptIndex).delete(sp.abc, true);
                    modAbcs.add(sp.abc);
                    removedPacks.add(sp);
                }
            }

            //Export subclasses so they can be compiled by Flex, but ONLY STUBS. 
            //No method code to avoid code compilation problems.
            //This compiled code won't be used at all in original SWF, 
            //it is used only by Flex to properly compile current script
            AS3ScriptExporter ex = new AS3ScriptExporter();
            ex.exportActionScript3(swfCopy, null, tempDir.getAbsolutePath(), removedPacks, new ScriptExportSettings(ScriptExportMode.AS_METHOD_STUBS, false, false, false /* ??? FIXME */, false, true), false, null);

            //now really remove the classes from SWF copy
            for (ABC a : modAbcs) {
                a.pack();
            }

            //Generate SWC file from the modified SWF file.
            //Flex then uses the code already present in the SWC, no need to decompile it (hurray!)
            SwfToSwcExporter swcExport = new SwfToSwcExporter();
            swcExport.exportSwf(swfCopy, swcFile, true);

            dependenciesSwcFiles = new ArrayList<>();
            int i = 0;
            for (SWF depSwf : dependencies) {
                i++;
                File depSwcFile = new File(tempDir, "dep" + i + ".swc");
                swcExport.exportSwf(depSwf, depSwcFile, false);
                dependenciesSwcFiles.add(depSwcFile);
            }
        } catch (IOException iex) {
            //ignore
        } catch (InterruptedException ex) {
            //ignore
        }
        this.initedPack = pack;
    }

    @Override
    public synchronized void deinitReplacement(ScriptPack pack) {
        if (tempDir != null && tempDir.exists()) {
            deleteFolder(tempDir);
        }
        tempDir = null;
    }
}
