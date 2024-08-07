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
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Exports SWF to FlashDevelop project.
 * @author JPEXS
 */
public class SwfFlashDevelopExporter {

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    public void exportFlashDevelopProject(SWF swf, File outFile, AbortRetryIgnoreHandler handler) throws IOException {
        exportFlashDevelopProject(swf, outFile, handler, null);
    }

    public void exportFlashDevelopProject(SWF swf, File outFile, AbortRetryIgnoreHandler handler, EventListener eventListener) throws IOException {
        if (!swf.isAS3()) {
            throw new IllegalArgumentException("SWF must be AS3");
        }

        String simpleName = outFile.getName();
        if (simpleName.contains(".")) {
            simpleName = simpleName.substring(0, simpleName.lastIndexOf("."));
        }

        SetBackgroundColorTag bgColorTag = swf.getBackgroundColor();

        String documentClass = swf.getDocumentClass();

        String srcPath = "src";
        String project = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<project version=\"2\">\n"
                + "  <!-- Generated with " + ApplicationInfo.applicationVerName  +" -->\n"
                + "  <!-- Output SWF options -->\n"
                + "  <output>\n"
                + "    <movie outputType=\"Application\" />\n"
                + "    <movie input=\"\" />\n"
                + "    <movie path=\"" + simpleName + ".swf\" />\n"
                + "    <movie fps=\"30\" />\n"
                + "    <movie width=\"" + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor) + "\" />\n"
                + "    <movie height=\"" + doubleToString(swf.displayRect.getHeight() / SWF.unitDivisor) + "\" />\n"
                + "    <movie version=\"" + swf.version + "\" />\n"
                + "    <movie minorVersion=\"0\" />\n"
                + "    <movie platform=\"Flash Player\" />\n"
                + "    <movie background=\"" + (bgColorTag == null ? "#FFFFFF" : bgColorTag.backgroundColor.toHexRGB()) + "\" />\n"
                + "  </output>\n"
                + "  <!-- Other classes to be compiled into your SWF -->\n"
                + "  <classpaths>\n"
                + "    <class path=\"" + srcPath + "\" />\n"
                + "  </classpaths>\n"
                + "  <!-- Build options -->\n"
                + "  <build>\n"
                + "    <option accessible=\"False\" />\n"
                + "    <option allowSourcePathOverlap=\"False\" />\n"
                + "    <option benchmark=\"False\" />\n"
                + "    <option es=\"False\" />\n"
                + "    <option loadConfig=\"\" />\n"
                + "    <option optimize=\"True\" />\n"
                + "    <option showActionScriptWarnings=\"True\" />\n"
                + "    <option showBindingWarnings=\"True\" />\n"
                + "    <option showDeprecationWarnings=\"True\" />\n"
                + "    <option showUnusedTypeSelectorWarnings=\"True\" />\n"
                + "    <option strict=\"True\" />\n"
                + "    <option useNetwork=\"True\" />\n"
                + "    <option useResourceBundleMetadata=\"True\" />\n"
                + "    <option warnings=\"True\" />\n"
                + "    <option verboseStackTraces=\"False\" />\n"
                + "    <option additional=\"\" />\n"
                + "    <option customSDK=\"\" />\n"
                + "  </build>\n"
                + "  <!-- Class files to compile (other referenced classes will automatically be included) -->\n"
                + "  <compileTargets>\n"
                + (documentClass == null
                        ? "    <!-- example: <compile path=\"classes\\Main.as\" /> -->\n"
                        : "<compile path=\"" + srcPath + "/" + documentClass.replace(".", "/") + ".as\" />\n")
                + "  </compileTargets>\n"
                + "  <!-- Paths to exclude from the Project Explorer tree -->\n"
                + "  <hiddenPaths>\n"
                + "    <!-- example: <hidden path=\"...\" /> -->\n"
                + "  </hiddenPaths>\n"
                + "  <!-- Executed before build -->\n"
                + "  <preBuildCommand />\n"
                + "  <!-- Executed after build -->\n"
                + "  <postBuildCommand alwaysRun=\"False\" />\n"
                + "  <!-- Other project options -->\n"
                + "  <options>\n"
                + "    <option showHiddenPaths=\"False\" />\n"
                + "    <option testMovie=\"Default\" />\n"
                + "  </options>\n"
                + "</project>";

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(Utf8Helper.getBytes(project));
        }

        boolean parallel = Configuration.parallelSpeedUp.get();
        ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false, false);
        swf.exportActionScript(handler, outFile.toPath().getParent().resolve(srcPath).toFile().getAbsolutePath(), scriptExportSettings, parallel, eventListener);
    }
}
