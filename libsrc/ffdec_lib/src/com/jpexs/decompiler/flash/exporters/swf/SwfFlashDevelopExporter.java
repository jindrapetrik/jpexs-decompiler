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
import com.jpexs.decompiler.flash.FlashPlayerVersion;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Exports SWF to FlashDevelop project.
 *
 * @author JPEXS
 */
public class SwfFlashDevelopExporter {

    public static boolean canExportSwf(SWF swf) {
        if (!swf.isAS3()) {
            return false;
        }
        //Cannot export if it has something on main timeline
        for (Tag t : swf.getTags()) {
            if ((t instanceof PlaceObjectTypeTag)
                    || (t instanceof SoundStreamBlockTag)
                    || (t instanceof VideoFrameTag)
                    || (t instanceof StartSoundTag)
                    || (t instanceof StartSound2Tag)) {
                return false;
            }
        }
        return true;
    }

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    /**
     * Exports SWF to FlashDevelop project.
     *
     * @param swf SWF to export
     * @param outFile Output file
     * @param air
     * @param handler Handler for abort, retry, ignore
     * @throws IOException On I/O error
     */
    public void exportFlashDevelopProject(SWF swf, File outFile, boolean air, AbortRetryIgnoreHandler handler) throws IOException {
        exportFlashDevelopProject(swf, outFile, air, handler, null);
    }

    /**
     * Exports SWF to FlashDevelop project.
     *
     * @param swf SWF to export
     * @param outFile Output file
     * @param air AIR
     * @param handler Handler for abort, retry, ignore
     * @param eventListener Event listener
     * @throws IOException On I/O error
     */
    public void exportFlashDevelopProject(SWF swf, File outFile, boolean air, AbortRetryIgnoreHandler handler, EventListener eventListener) throws IOException {
        if (!swf.isAS3()) {
            throw new IllegalArgumentException("SWF must be AS3");
        }

        if (!canExportSwf(swf)) {
            throw new IllegalArgumentException("SWF must not contain main timeline");
        }

        String simpleName = outFile.getName();
        if (simpleName.contains(".")) {
            simpleName = simpleName.substring(0, simpleName.lastIndexOf("."));
        }

        String simpleNameNoSpaces = simpleName.replace(" ", "").replace("_", "");

        SetBackgroundColorTag bgColorTag = swf.getBackgroundColor();

        String documentClass = swf.getDocumentClass();

        String srcPath = "src";
        String project;

        String flashPlayerVersion = FlashPlayerVersion.getFlashPlayerBySwfVersion(swf.version);
        String[] flashPlayerVersions = flashPlayerVersion.split("\\.");

        String airVersion = FlashPlayerVersion.getAirBySwfVersion(swf.version);
        String[] airVersions = airVersion.split("\\.");

        if (air) {

            project = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<project version=\"2\">\n"
                    + "  <!-- Output SWF options -->\n"
                    + "  <output>\n"
                    + "    <movie outputType=\"Application\" />\n"
                    + "    <movie input=\"\" />\n"
                    + "    <movie path=\"bin\\" + simpleNameNoSpaces + ".swf\" />\n"
                    + "    <movie fps=\"" + doubleToString(swf.frameRate) + "\" />\n"
                    + "    <movie width=\"" + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor) + "\" />\n"
                    + "    <movie height=\"" + doubleToString(swf.displayRect.getHeight() / SWF.unitDivisor) + "\" />\n"
                    + "    <movie version=\"" + airVersions[0] + "\" />\n"
                    + "    <movie minorVersion=\"" + airVersions[1] + "\" />\n"
                    + "    <movie platform=\"AIR\" />\n"
                    + "    <movie background=\"" + (bgColorTag == null ? "#FFFFFF" : bgColorTag.backgroundColor.toHexRGB()) + "\" />\n"
                    + "    <movie preferredSDK=\"\" />\n"
                    + "  </output>\n"
                    + "  <!-- Other classes to be compiled into your SWF -->\n"
                    + "  <classpaths>\n"
                    + "    <class path=\"src\" />\n"
                    + "  </classpaths>\n"
                    + "  <!-- Build options -->\n"
                    + "  <build>\n"
                    + "    <option accessible=\"False\" />\n"
                    + "    <option advancedTelemetry=\"False\" />\n"
                    + "    <option allowSourcePathOverlap=\"False\" />\n"
                    + "    <option benchmark=\"False\" />\n"
                    + "    <option es=\"False\" />\n"
                    + "    <option inline=\"False\" />\n"
                    + "    <option locale=\"\" />\n"
                    + "    <option loadConfig=\"\" />\n"
                    + "    <option optimize=\"True\" />\n"
                    + "    <option omitTraces=\"True\" />\n"
                    + "    <option showActionScriptWarnings=\"True\" />\n"
                    + "    <option showBindingWarnings=\"True\" />\n"
                    + "    <option showInvalidCSS=\"True\" />\n"
                    + "    <option showDeprecationWarnings=\"True\" />\n"
                    + "    <option showUnusedTypeSelectorWarnings=\"True\" />\n"
                    + "    <option strict=\"True\" />\n"
                    + "    <option useNetwork=\"True\" />\n"
                    + "    <option useResourceBundleMetadata=\"True\" />\n"
                    + "    <option warnings=\"True\" />\n"
                    + "    <option verboseStackTraces=\"False\" />\n"
                    + "    <option linkReport=\"\" />\n"
                    + "    <option loadExterns=\"\" />\n"
                    + "    <option staticLinkRSL=\"True\" />\n"
                    + "    <option additional=\"-swf-version=" + swf.version + "\" />\n"
                    + "    <option compilerConstants=\"\" />\n"
                    + "    <option minorVersion=\"\" />\n"
                    + "  </build>\n"
                    + "  <!-- SWC Include Libraries -->\n"
                    + "  <includeLibraries>\n"
                    + "    <!-- example: <element path=\"...\" /> -->\n"
                    + "  </includeLibraries>\n"
                    + "  <!-- SWC Libraries -->\n"
                    + "  <libraryPaths>\n"
                    + "    <!-- example: <element path=\"...\" /> -->\n"
                    + "  </libraryPaths>\n"
                    + "  <!-- External Libraries -->\n"
                    + "  <externalLibraryPaths>\n"
                    + "    <!-- example: <element path=\"...\" /> -->\n"
                    + "  </externalLibraryPaths>\n"
                    + "  <!-- Runtime Shared Libraries -->\n"
                    + "  <rslPaths>\n"
                    + "    <!-- example: <element path=\"...\" /> -->\n"
                    + "  </rslPaths>\n"
                    + "  <!-- Intrinsic Libraries -->\n"
                    + "  <intrinsics>\n"
                    + "    <!-- example: <element path=\"...\" /> -->\n"
                    + "  </intrinsics>\n"
                    + "  <!-- Assets to embed into the output SWF -->\n"
                    + "  <library>\n"
                    + "    <!-- example: <asset path=\"...\" id=\"...\" update=\"...\" glyphs=\"...\" mode=\"...\" place=\"...\" sharepoint=\"...\" /> -->\n"
                    + "  </library>\n"
                    + "  <!-- Class files to compile (other referenced classes will automatically be included) -->\n"
                    + "  <compileTargets>\n"
                    + (documentClass == null
                            ? "    <!-- example: <compile path=\"classes\\Main.as\" /> -->\n"
                            : "<compile path=\"" + srcPath + "/" + documentClass.replace(".", "/") + ".as\" />\n")
                    + "  </compileTargets>\n"
                    + "  <!-- Paths to exclude from the Project Explorer tree -->\n"
                    + "  <hiddenPaths>\n"
                    + "    <hidden path=\"obj\" />\n"
                    + "  </hiddenPaths>\n"
                    + "  <!-- Executed before build -->\n"
                    + "  <preBuildCommand />\n"
                    + "  <!-- Executed after build -->\n"
                    + "  <postBuildCommand alwaysRun=\"False\" />\n"
                    + "  <!-- Other project options -->\n"
                    + "  <options>\n"
                    + "    <option showHiddenPaths=\"False\" />\n"
                    + "    <option testMovie=\"Custom\" />\n"
                    + "    <option testMovieCommand=\"bat\\RunApp.bat\" />\n"
                    + "  </options>\n"
                    + "  <!-- Plugin storage -->\n"
                    + "  <storage />\n"
                    + "</project>";
        } else {
            project = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<project version=\"2\">\n"
                    + "  <!-- Generated with " + ApplicationInfo.applicationVerName + " -->\n"
                    + "  <!-- Output SWF options -->\n"
                    + "  <output>\n"
                    + "    <movie outputType=\"Application\" />\n"
                    + "    <movie input=\"\" />\n"
                    + "    <movie path=\"" + simpleName + ".swf\" />\n"
                    + "    <movie fps=\"" + doubleToString(swf.frameRate) + "\" />\n"
                    + "    <movie width=\"" + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor) + "\" />\n"
                    + "    <movie height=\"" + doubleToString(swf.displayRect.getHeight() / SWF.unitDivisor) + "\" />\n"
                    + "    <movie version=\"" + flashPlayerVersions[0] + "\" />\n"
                    + "    <movie minorVersion=\"" + flashPlayerVersions[1] + "\" />\n"
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
                    + "    <option additional=\"-swf-version=" + swf.version + "\" />\n"
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
        }

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(Utf8Helper.getBytes(project));
        }

        if (air) {
            String applicationXml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> \n"
                    + "<application xmlns=\"http://ns.adobe.com/air/application/" + airVersion + "\">\n"
                    + "\t\n"
                    + "\t<id>" + simpleNameNoSpaces + "</id> \n"
                    + "\t<versionNumber>1.0</versionNumber> \n"
                    + "\t<filename>" + simpleNameNoSpaces + "</filename> \n"
                    + "\t\n"
                    + "\t<name>" + simpleName + "</name> \n"
                    + "\t<description></description> \n"
                    + "\t<copyright></copyright> \n"
                    + "\t\n"
                    + "\t<initialWindow> \n"
                    + "\t\t<title>" + simpleName + "</title> \n"
                    + "\t\t<content>" + simpleNameNoSpaces + ".swf</content> \n"
                    + "\t\t<systemChrome>standard</systemChrome> \n"
                    + "\t\t<transparent>false</transparent> \n"
                    + "\t\t<visible>true</visible> \n"
                    + "\t\t<minimizable>true</minimizable> \n"
                    + "\t\t<maximizable>true</maximizable> \n"
                    + "\t\t<resizable>true</resizable> \n"
                    + "\t</initialWindow> \n"
                    + "\t\n"
                    + "\t<!-- \n"
                    + "\tMore options:\n"
                    + "\thttp://livedocs.adobe.com/flex/3/html/File_formats_1.html#1043413\n"
                    + "\t-->\n"
                    + "</application>";
            try (FileOutputStream fos = new FileOutputStream(outFile.toPath().getParent().resolve("application.xml").toFile())) {
                fos.write(Utf8Helper.getBytes(applicationXml));
            }

            Path batDirPath = outFile.toPath().getParent().resolve("bat");
            batDirPath.toFile().mkdir();
            String[] batFiles = new String[]{
                "CreateCertificate.bat",
                "PackageApp.bat",
                "Packager.bat",
                "RunApp.bat",
                "SetupApp.bat",
                "SetupSDK.bat"
            };
            for (String batFile : batFiles) {
                InputStream is = SwfFlashDevelopExporter.class.getResourceAsStream("/com/jpexs/helpers/resource/fd_air/bat/" + batFile);
                byte[] data = Helper.readStream(is);
                String strData = Utf8Helper.decode(data);
                strData = strData.replace("<app_fullname>", simpleName);
                strData = strData.replace("<app_nospaces>", simpleNameNoSpaces);
                try (FileOutputStream fos = new FileOutputStream(batDirPath.resolve(batFile).toFile())) {
                    fos.write(Utf8Helper.getBytes(strData));
                }
            }
        }

        boolean parallel = Configuration.parallelSpeedUp.get();
        ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false, false, "/_assets/", Configuration.linkAllClasses.get(), false);
        swf.exportActionScript(handler, outFile.toPath().getParent().resolve(srcPath).toFile().getAbsolutePath(), scriptExportSettings, parallel, eventListener);
    }
}
