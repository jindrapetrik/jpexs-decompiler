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
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SwfIntelliJIdeaExporter {

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    public void exportIntelliJIdeaProject(SWF swf, File outFile, AbortRetryIgnoreHandler handler) throws IOException {
        exportIntelliJIdeaProject(swf, outFile, handler, null);
    }

    public void exportIntelliJIdeaProject(SWF swf, File outFile, AbortRetryIgnoreHandler handler, EventListener eventListener) throws IOException {
        if (!swf.isAS3()) {
            throw new IllegalArgumentException("SWF must be AS3");
        }

        String simpleName = outFile.getName();
        if (simpleName.contains(".")) {
            simpleName = simpleName.substring(0, simpleName.lastIndexOf("."));
        }

        File baseDir = outFile.getParentFile();
        File ideaDir = new File(baseDir, ".idea");

        String documentClass = swf.getDocumentClass();
        if (documentClass == null) {
            documentClass = "";
        }

        List<String> additionalOptions = new ArrayList<>();

        additionalOptions.add("-default-size " + Math.round(swf.displayRect.getWidth() / SWF.unitDivisor) + " " + Math.round(swf.displayRect.getHeight() / SWF.unitDivisor));
        additionalOptions.add("-default-frame-rate " + Math.round(swf.frameRate));
        if (swf.getBackgroundColor() != null) {
            additionalOptions.add("-default-background-color " + swf.getBackgroundColor().backgroundColor.toHexRGB());
        }

        String imlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module type=\"Flex\" version=\"4\">\n"
                + "  <component name=\"FlexBuildConfigurationManager\" active=\"" + simpleName + "\">\n"
                + "    <configurations>\n"
                + "      <configuration name=\"" + simpleName + "\" pure-as=\"true\" main-class=\"" + documentClass + "\" output-file=\"" + simpleName + ".swf\" output-folder=\"$MODULE_DIR$/out/production/" + simpleName + "\">\n"
                + "        <dependencies target-player=\"25.0\">\n"
                + "          <sdk name=\"flex\" />\n"
                + "        </dependencies>\n"
                + "        <compiler-options>\n"
                + "          <option name=\"additionalOptions\" value=\"" + String.join(" ", additionalOptions) + "\" />\n"
                + "        </compiler-options>\n"
                + "        <packaging-air-desktop />\n"
                + "        <packaging-android />\n"
                + "        <packaging-ios />\n"
                + "      </configuration>\n"
                + "    </configurations>\n"
                + "    <compiler-options />\n"
                + "  </component>\n"
                + "  <component name=\"NewModuleRootManager\" inherit-compiler-output=\"true\">\n"
                + "    <exclude-output />\n"
                + "    <content url=\"file://$MODULE_DIR$\">\n"
                + "      <sourceFolder url=\"file://$MODULE_DIR$/src\" isTestSource=\"false\" />\n"
                + "    </content>\n"
                + "    <orderEntry type=\"jdk\" jdkName=\"flex\" jdkType=\"Flex SDK Type (new)\" />\n"
                + "    <orderEntry type=\"sourceFolder\" forTests=\"false\" />\n"
                + "  </component>\n"
                + "</module>";
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(Utf8Helper.getBytes(imlData));
        }

        ideaDir.mkdir();

        String modulesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project version=\"4\">\n"
                + "  <component name=\"ProjectModuleManager\">\n"
                + "    <modules>\n"
                + "      <module fileurl=\"file://$PROJECT_DIR$/" + outFile.getName() + "\" filepath=\"$PROJECT_DIR$/" + outFile.getName() + "\" />\n"
                + "    </modules>\n"
                + "  </component>\n"
                + "</project>";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, "modules.xml"))) {
            fos.write(Utf8Helper.getBytes(modulesXml));
        }

        String flexCompilerXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project version=\"4\">\n"
                + "  <component name=\"FlexIdeProjectLevelCompilerOptionsHolder\">\n"
                + "    <compiler-options />\n"
                + "  </component>\n"
                + "</project>";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, "flexCompiler.xml"))) {
            fos.write(Utf8Helper.getBytes(flexCompilerXml));
        }

        String miscXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project version=\"4\">\n"
                + "  <component name=\"ProjectRootManager\" version=\"2\" languageLevel=\"JDK_22\" project-jdk-name=\"flex\" project-jdk-type=\"Flex SDK Type (new)\">\n"
                + "    <output url=\"file://$PROJECT_DIR$/out\" />\n"
                + "  </component>\n"
                + "</project>";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, "misc.xml"))) {
            fos.write(Utf8Helper.getBytes(miscXml));
        }

        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, ".name"))) {
            fos.write(Utf8Helper.getBytes(outFile.getName()));
        }

        String gitIgnore = "# Default ignored files\n"
                + "/shelf/\n"
                + "/workspace.xml\n"
                + "# Editor-based HTTP Client requests\n"
                + "/httpRequests/\n"
                + "# Datasource local storage ignored files\n"
                + "/dataSources/\n"
                + "/dataSources.local.xml";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, ".gitignore"))) {
            fos.write(Utf8Helper.getBytes(gitIgnore));
        }

        boolean parallel = Configuration.parallelSpeedUp.get();
        ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false, false);
        swf.exportActionScript(handler, new File(outFile.getParentFile(), "src").getAbsolutePath(), scriptExportSettings, parallel, eventListener);
    }
}
