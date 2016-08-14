package com.jpexs.decompiler.flash.flexsdk;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.LinkReportExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfToSwcExporter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class As3ScriptReplacer extends MxmlcRunner {

    private LinkReportExporter linkReporter;

    public As3ScriptReplacer(String flexSdkPath, LinkReportExporter linkReporter) {
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

    public void replaceScript(SWF swf, ScriptPack oldPack, String txt) throws IOException, MxmlcException, InterruptedException {
        if (!oldPack.isSimple) {
            throw new IOException("Cannot compile such file"); //Alchemy, etc.
        }

        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory("ffdec-mxmlc-replace").toFile();
            File pkgDir = tempDir;
            for (String pkgPart : oldPack.getClassPath().packageStr.toList()) {
                if (!pkgPart.isEmpty()) {
                    pkgDir = new File(pkgDir, pkgPart);
                }
            }
            pkgDir.mkdirs();
            File scriptFileToCompile = new File(pkgDir, oldPack.getClassPath().className + ".as");
            File compiledSwfFile = new File(pkgDir, "out.swf");
            File swcFile = new File(pkgDir, "out.swc");

            //Make copy without the old script
            SWF swfCopy = recompileSWF(swf);
            List<ABC> modAbcs = new ArrayList<>();

            List<ScriptPack> copyPacks = swfCopy.getAS3Packs();
            for (ScriptPack sp : copyPacks) {
                if (sp.getClassPath().equals(oldPack.getClassPath())) {
                    sp.abc.script_info.get(sp.scriptIndex).delete(sp.abc, true);
                    ((Tag) sp.abc.parentTag).setModified(true);
                    modAbcs.add(sp.abc);
                    break;
                }
            }

            //remove all subclasses
            for (ScriptPack sp : copyPacks) {
                DottedChain dc = sp.getPathPackage().add(sp.getPathScriptName());
                if (isParentDeleted(sp.abc, sp.allABCs, dc)) {
                    sp.abc.script_info.get(sp.scriptIndex).delete(sp.abc, true);
                    modAbcs.add(sp.abc);
                }
            }
            for (ABC a : modAbcs) {
                a.pack();
            }
            SwfToSwcExporter swcExport = new SwfToSwcExporter();
            swcExport.exportSwf(swfCopy, swcFile, true);

            Helper.writeFile(scriptFileToCompile.getAbsolutePath(), txt.getBytes("UTF-8"));
            mxmlc("-include-inheritance-dependencies-only", "-warnings=false", "-library-path", swcFile.getAbsolutePath(), "-source-path", tempDir.getAbsolutePath(), "-output", compiledSwfFile.getAbsolutePath(), "-debug=true", scriptFileToCompile.getAbsolutePath());

            try (FileInputStream fis = new FileInputStream(compiledSwfFile)) {
                SWF newSWF = new SWF(fis, false, false);
                List<ABCContainerTag> newTags = newSWF.getAbcList();
                ScriptInfo oldScriptInfo = oldPack.abc.script_info.get(oldPack.scriptIndex);
                if (oldPack.isSimple) {
                    oldScriptInfo.delete(oldPack.abc, true);
                } else {
                    //NOO
                }
                int oldTagIndex = swf.getTags().indexOf((Tag) oldPack.abc.parentTag);
                oldPack.abc.pack(); // removes old classes/methods/scripts
                if (oldPack.abc.script_info.isEmpty()) {
                    swf.removeTag(oldTagIndex);
                }
                ABCContainerTag lastTag = newTags.get(newTags.size() - 1);
                ((Tag) lastTag).setSwf(swf);
                swf.addTag(oldTagIndex + 1, (Tag) lastTag);
                ((Tag) lastTag).setModified(true);
                ((Tag) oldPack.abc.parentTag).setModified(true);
            }
        } finally {
            if (tempDir != null && tempDir.exists()) {
                //deleteFolder(tempDir);
            }
        }
    }
}
