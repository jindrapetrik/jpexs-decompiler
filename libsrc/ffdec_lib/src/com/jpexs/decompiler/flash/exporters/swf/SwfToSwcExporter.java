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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.Dependency;
import com.jpexs.decompiler.flash.exporters.script.DependencyType;
import com.jpexs.decompiler.flash.importers.SwfXmlImporter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Helper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SwfToSwcExporter {

    private static final String DEPENDENCY_NAMESPACE = "n";
    private static final String DEPENDENCY_INHERITANCE = "i";
    private static final String DEPENDENCY_EXPRESSION = "e";
    private static final String DEPENDENCY_SIGNATURE = "s";

    private static String sha256(InputStream is) {
        String output;
        int read;
        byte[] buffer = new byte[8192];

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            BigInteger bigInt = new BigInteger(1, hash);
            output = bigInt.toString(16);
            while (output.length() < 32) {
                output = "0" + output;
            }
        } catch (Exception e) {
            return null;
        }

        return output;
    }

    public SwfToSwcExporter() {

    }

    private String dottedChainToId(DottedChain dc) {
        if (dc.getWithoutLast().isEmpty()) {
            return dc.getLast();
        }
        return dc.getWithoutLast().toRawString() + ":" + dc.getLast();
    }

    private String generateCatalog(SWF swf, byte[] swfBytes, boolean skipDependencies) {
        StringBuilder sb = new StringBuilder();

        final String libraryFileName = "library.swf";

        final String SWC_VERSION = "1.2";
        final String FLEX_VERSION = "4.6.0";
        final String FLEX_BUILD = "23201";
        final String MINIMUM_SUPPORTED_VERSION = "3.0.0";

        sb.append("<?xml version=\"1.0\" encoding =\"utf-8\"?>\n")
                .append("<swc xmlns=\"http://www.adobe.com/flash/swccatalog/9\">\n")
                .append("  <versions>\n")
                .append("    <swc version=\"").append(SWC_VERSION).append("\" />\n")
                .append("    <flex version=\"").append(FLEX_VERSION).append("\" build=\"").append(FLEX_BUILD).append("\" minimumSupportedVersion=\"").append(MINIMUM_SUPPORTED_VERSION).append("\" />\n")
                .append("  </versions>\n")
                .append("  <features>\n")
                .append("    <feature-script-deps />\n")
                .append("    <feature-files />\n")
                .append("  </features>\n")
                .append("  <libraries>\n")
                .append("    <library path=\"").append(libraryFileName).append("\">\n");

        final long TIME_NOW = new Date().getTime();

        Set<DottedChain> definedObjects = new HashSet<>();

        List<ABCContainerTag> abcTagList = swf.getAbcList();

        List<ScriptPack> packs = swf.getAS3Packs();

        for (ScriptPack pack : packs) {
            ClassPath cp = pack.getClassPath();
            definedObjects.add(cp.packageStr.add(cp.className, "" /*strip*/));
        }

        List<ABC> allAbcList = new ArrayList<>();
        for (int i = 0; i < abcTagList.size(); i++) {
            allAbcList.add(abcTagList.get(i).getABC());
        }

        for (ABCContainerTag abcContainer : abcTagList) {
            if (!(abcContainer instanceof DoABC2Tag)) {
                continue; //DoABCTag 1 does not have name
            }
            DoABC2Tag abcTag = (DoABC2Tag) abcContainer;
            String scriptName = abcTag.name;
            sb.append("      <script name=\"").append(scriptName).append("\" mod=\"").append(TIME_NOW).append("\">\n");
            List<ScriptPack> tagPacks = abcTag.getABC().getScriptPacks(null, allAbcList);

            for (ScriptPack pack : tagPacks) {
                ClassPath cp = pack.getClassPath();
                String defId = dottedChainToId(cp.packageStr.add(cp.className, "" /*strip*/));

                sb.append("        <def id=\"").append(defId).append("\" />\n");

                Set<DottedChain> allDeps = new HashSet<>();
                allDeps.add(new DottedChain(new String[]{"AS3"}, ""));
                sb.append("        <dep id=\"AS3\" type=\"").append(DEPENDENCY_NAMESPACE).append("\" />\n");
                if (!skipDependencies) {
                    List<Dependency> dependencies = new ArrayList<>();
                    List<String> uses = new ArrayList<>();
                    pack.abc.script_info.get(pack.scriptIndex).traits.getDependencies(null, pack.abc, dependencies, uses, new DottedChain(new String[]{"NO:PACKAGE"}, ""), new ArrayList<>());

                    for (Dependency d : dependencies) {
                        if ("*".equals(d.getId().getLast())) {
                            continue;
                        }
                        //some toplevel "imports" can be only method calls
                        if (d.getId().getWithoutLast().isEmpty() && !definedObjects.contains(d.getId())) {
                            continue;
                        }

                        if (allDeps.contains(d.getId())) {
                            continue;
                        }
                        sb.append("        <dep id=\"").append(dottedChainToId(d.getId())).append("\" type=\"").append(getDependencyStr(d.getType())).append("\" />\n");

                        allDeps.add(d.getId());
                    }
                }
            }
            sb.append("      </script>\n");
        }
        String sha256Hash = sha256(new ByteArrayInputStream(swfBytes));
        sb.append("      <digests>\n")
                .append("        <digest type=\"SHA-256\" signed=\"false\" value=\"").append(sha256Hash).append("\" />\n")
                .append("      </digests>\n")
                .append("    </library>\n")
                .append("  </libraries>\n")
                .append("  <files>\n")
                .append("  </files>\n")
                .append("</swc>\n");
        return sb.toString();
    }

    private static String getDependencyStr(DependencyType depType) {
        switch (depType) {
            case EXPRESSION:
                return DEPENDENCY_EXPRESSION;
            case INHERITANCE:
                return DEPENDENCY_INHERITANCE;
            case SIGNATURE:
                return DEPENDENCY_SIGNATURE;
            case NAMESPACE:
                return DEPENDENCY_NAMESPACE;
            default:
                return null;
        }
    }

    private SWF recompileSWF(SWF swf) throws IOException, InterruptedException {
        ByteArrayOutputStream swfOrigBaos = new ByteArrayOutputStream();
        swf.saveTo(swfOrigBaos);
        return new SWF(new ByteArrayInputStream(swfOrigBaos.toByteArray()), Configuration.parallelSpeedUp.get(), false);
    }

    private static void printDelay(String title, long t1, long t2) {
        //System.out.println("time " + title + ": " + (t2 - t1));
    }

    public void exportSwf(SWF swf, File outSwcFile, boolean skipDependencies) throws IOException {
        long t4 = 0;
        //Make local copy of SWF so we do not modify original
        try {
            long t1 = System.currentTimeMillis();
            swf = recompileSWF(swf);
            long t2 = System.currentTimeMillis();
            printDelay("swc.recompile", t1, t2);

            final String HASH = "myhash";
            String abcTagXml = new String(Helper.readStream(SwfToSwcExporter.class.getResourceAsStream("/com/jpexs/decompiler/flash/exporters/swf/swc_main_abctag.xml")), "UTF-8");
            abcTagXml = abcTagXml.replace("%hash%", HASH);
            SwfXmlImporter xmlImporter = new SwfXmlImporter();
            DoABC2Tag mainAbcTag = (DoABC2Tag) xmlImporter.importObject(abcTagXml, DoABC2Tag.class, swf);
            ReadOnlyTagList list = swf.getTags();
            final String documentClassStub = "_%hash%_flash_display_Sprite";
            String documentClass = documentClassStub.replace("%hash%", HASH);
            boolean documentClassSet = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof ABCContainerTag) {
                    swf.addTag(i, mainAbcTag);
                    break;
                }
            }

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof SymbolClassTag) {
                    SymbolClassTag sct = (SymbolClassTag) list.get(i);
                    for (int j = 0; j < sct.tags.size(); j++) {
                        if (sct.tags.get(j) == 0) {
                            sct.names.set(j, documentClass);
                            documentClassSet = true;
                            break;
                        }
                    }
                }
            }
            long t3 = System.currentTimeMillis();
            printDelay("swc.documentTag", t2, t3);

            if (!documentClassSet) {
                throw new IOException("Original document class not found!");
            }
            swf = recompileSWF(swf);
            t4 = System.currentTimeMillis();
            printDelay("swc.recompile2", t3, t4);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
            throw new RuntimeException(ex);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        swf.saveTo(baos);
        byte[] swfBytes = baos.toByteArray();
        long t5 = System.currentTimeMillis();
        printDelay("swc.getBytes", t4, t5);

        String catalogStr = generateCatalog(swf, swfBytes, skipDependencies);

        long t6 = System.currentTimeMillis();
        printDelay("swc.generateCatalog", t5, t6);

        File tempFile = new File((outSwcFile.getAbsolutePath()) + ".tmp");
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(tempFile);
            zos = new ZipOutputStream(fos);

            ZipEntry libraryEntry = new ZipEntry("library.swf");
            zos.putNextEntry(libraryEntry);
            zos.write(swfBytes);
            zos.closeEntry();

            ZipEntry catalogEntry = new ZipEntry("catalog.xml");
            zos.putNextEntry(catalogEntry);
            zos.write(catalogStr.getBytes("UTF-8"));
            zos.closeEntry();

            zos.close();
            zos = null;
            fos.close();
            fos = null;
            outSwcFile.delete();
            tempFile.renameTo(outSwcFile);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException ex) {
                    //ignore
                }

            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
        long t7 = System.currentTimeMillis();
        printDelay("swc.zip", t6, t7);

    }
}
