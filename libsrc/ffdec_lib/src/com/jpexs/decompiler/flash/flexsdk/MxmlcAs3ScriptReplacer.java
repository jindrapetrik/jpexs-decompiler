package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.script.AS3ScriptExporter;
import com.jpexs.decompiler.flash.exporters.script.LinkReportExporter;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.exporters.swf.SwfToSwcExporter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.jpexs.decompiler.flash.importers.As3ScriptReplacerInterface;

public class MxmlcAs3ScriptReplacer extends MxmlcRunner implements As3ScriptReplacerInterface {

    private LinkReportExporter linkReporter;

    public MxmlcAs3ScriptReplacer(String flexSdkPath, LinkReportExporter linkReporter) {
        super(flexSdkPath);
        this.linkReporter = linkReporter;
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
        return new SWF(new ByteArrayInputStream(swfOrigBaos.toByteArray()), false, false);
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
        if (ii.super_index != 0 && isParentDeleted(abc, allAbcs, abc.constants.getMultiname(ii.super_index).getNameWithNamespace(abc.constants))) {
            return true;
        }
        for (int iface : ii.interfaces) {
            if (isParentDeleted(abc, allAbcs, abc.constants.getMultiname(iface).getNameWithNamespace(abc.constants))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void replaceScript(ScriptPack pack, String text) throws AVM2ParseException, CompilationException, IOException, InterruptedException {
        if (!pack.isSimple) {
            throw new IOException("Cannot compile such file"); //Alchemy, etc.
        }

        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ffdec-mxmlc-replace").toFile();
            File pkgDir = tempDir;
            for (String pkgPart : pack.getClassPath().packageStr.toList()) {
                if (!pkgPart.isEmpty()) {
                    pkgDir = new File(pkgDir, pkgPart);
                }
            }
            pkgDir.mkdirs();
            File scriptFileToCompile = new File(pkgDir, pack.getClassPath().className + ".as");
            File compiledSwfFile = new File(pkgDir, "out.swf");
            File swcFile = new File(pkgDir, "out.swc");

            //Make copy without the old script
            SWF swfCopy = recompileSWF(pack.getSwf());
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
                DottedChain dc = sp.getPathPackage().add(sp.getPathScriptName());
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
            ex.exportActionScript3(swfCopy, null, tempDir.getAbsolutePath(), removedPacks, new ScriptExportSettings(ScriptExportMode.AS_METHOD_STUBS, false), false, null);

            //now really remove the classes from SWF copy
            for (ABC a : modAbcs) {
                a.pack();
            }
            //Generate SWC file from the modified SWF file.
            //Flex then uses the code already present in the SWC, no need to decompile it (hurray!)
            SwfToSwcExporter swcExport = new SwfToSwcExporter();
            swcExport.exportSwf(swfCopy, swcFile, true);

            //Write new script
            Helper.writeFile(scriptFileToCompile.getAbsolutePath(), text.getBytes("UTF-8"));

            try {
                //Compile it (and subclasses stubs)
                mxmlc("-strict=false", "-include-inheritance-dependencies-only", "-warnings=false", "-library-path", swcFile.getAbsolutePath(), "-source-path", tempDir.getAbsolutePath(), "-output", compiledSwfFile.getAbsolutePath(), "-debug=true", scriptFileToCompile.getAbsolutePath());
            } catch (MxmlcException ex1) {
                throw new AVM2ParseException(ex1.getMxmlcErrorOutput(), 0);
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
            }
        } finally {
            if (tempDir != null && tempDir.exists()) {
                deleteFolder(tempDir);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        String flexLocation = Configuration.flexSdkLocation.get();
        return !(flexLocation.isEmpty() || (!new File(MxmlcRunner.getMxmlcPath(flexLocation)).exists()));
    }
}
