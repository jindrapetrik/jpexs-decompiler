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
import com.jpexs.decompiler.flash.FlashPlayerVersion;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Exports SWF to IntelliJ IDEA project.
 *
 * @author JPEXS
 */
public class SwfIntelliJIdeaExporter {

    private static final String PROJECTID_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int PROJECTID_LENGTH = 27;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String generateProjectId() {
        StringBuilder sb = new StringBuilder(PROJECTID_LENGTH);
        for (int i = 0; i < PROJECTID_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(PROJECTID_CHARACTERS.length());
            sb.append(PROJECTID_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

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

    /**
     * Exports SWF to IntelliJ IDEA project.
     *
     * @param swf SWF to export
     * @param outDir Output directory
     * @param handler Handler for abort, retry, ignore
     * @throws IOException On I/O error
     */
    public void exportIntelliJIdeaProject(SWF swf, File outDir, AbortRetryIgnoreHandler handler) throws IOException {
        exportIntelliJIdeaProject(swf, outDir, handler, null);
    }

    /**
     * Exports SWF to IntelliJ IDEA project.
     *
     * @param swf SWF to export
     * @param outDir Output directory
     * @param handler Handler for abort, retry, ignore
     * @param eventListener Event listener
     * @throws IOException On I/O error
     */
    public void exportIntelliJIdeaProject(SWF swf, File outDir, AbortRetryIgnoreHandler handler, EventListener eventListener) throws IOException {
        if (!swf.isAS3()) {
            throw new IllegalArgumentException("SWF must be AS3");
        }

        if (!canExportSwf(swf)) {
            throw new IllegalArgumentException("SWF must not contain main timeline");
        }

        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                throw new IOException("Cannot create directory");
            }
        }
        if (!outDir.isDirectory()) {
            throw new IOException("The selected file is not a directory");
        }

        String simpleName = swf.getShortFileName();
        if (simpleName.contains(".")) {
            simpleName = simpleName.substring(0, simpleName.lastIndexOf("."));
        }

        File ideaDir = new File(outDir, ".idea");

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

        String flashPlayerVersion = FlashPlayerVersion.getFlashPlayerBySwfVersion(swf.version);

        String imlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module type=\"Flex\" version=\"4\">\n"
                + "  <component name=\"FlexBuildConfigurationManager\" active=\"" + simpleName + "\">\n"
                + "    <configurations>\n"
                + "      <configuration name=\"" + simpleName + "\" pure-as=\"true\" main-class=\"" + documentClass + "\" output-file=\"" + simpleName + ".swf\" output-folder=\"$MODULE_DIR$/out/production/" + simpleName + "\">\n"
                + "        <dependencies target-player=\"" + flashPlayerVersion + "\">\n"
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
        try (FileOutputStream fos = new FileOutputStream(new File(outDir, simpleName + ".iml"))) {
            fos.write(Utf8Helper.getBytes(imlData));
        }

        ideaDir.mkdir();

        long created = System.currentTimeMillis();
        String workspaceXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project version=\"4\">\n"
                + "  <component name=\"ChangeListManager\">\n"
                + "    <list default=\"true\" id=\"" + UUID.randomUUID() + "\" name=\"Changes\" comment=\"\" />\n"
                + "    <option name=\"SHOW_DIALOG\" value=\"false\" />\n"
                + "    <option name=\"HIGHLIGHT_CONFLICTS\" value=\"true\" />\n"
                + "    <option name=\"HIGHLIGHT_NON_ACTIVE_CHANGELIST\" value=\"false\" />\n"
                + "    <option name=\"LAST_RESOLUTION\" value=\"IGNORE\" />\n"
                + "  </component>\n"
                + "  <component name=\"ProjectColorInfo\"><![CDATA[{\n"
                + "  \"associatedIndex\": 8\n"
                + "}]]></component>\n"
                + "  <component name=\"ProjectId\" id=\"" + generateProjectId() + "\" />\n"
                + "  <component name=\"ProjectViewState\">\n"
                + "    <option name=\"hideEmptyMiddlePackages\" value=\"true\" />\n"
                + "    <option name=\"showLibraryContents\" value=\"true\" />\n"
                + "  </component>\n"
                + "  <component name=\"PropertiesComponent\"><![CDATA[{\n"
                + "  \"keyToString\": {\n"
                + "    \"Flash App." + simpleName + ".executor\": \"Run\",\n"
                + "    \"RunOnceActivity.ShowReadmeOnStart\": \"true\",\n"
                + "    \"kotlin-language-version-configured\": \"true\",\n"
                + "    \"nodejs_package_manager_path\": \"npm\",\n"
                + "    \"vue.rearranger.settings.migration\": \"true\"\n"
                + "  }\n"
                + "}]]></component>\n"
                + "  <component name=\"RunManager\">\n"
                + "    <configuration name=\"" + simpleName + "\" type=\"FlashRunConfigurationType\">\n"
                + "      <option name=\"BCName\" value=\"" + simpleName + "\" />\n"
                + "      <option name=\"IOSSimulatorDevice\" value=\"\" />\n"
                + "      <option name=\"IOSSimulatorSdkPath\" value=\"\" />\n"
                + "      <option name=\"adlOptions\" value=\"\" />\n"
                + "      <option name=\"airProgramParameters\" value=\"\" />\n"
                + "      <option name=\"appDescriptorForEmulator\" value=\"Android\" />\n"
                + "      <option name=\"clearAppDataOnEachLaunch\" value=\"true\" />\n"
                + "      <option name=\"debugTransport\" value=\"USB\" />\n"
                + "      <option name=\"debuggerSdkRaw\" value=\"BC SDK\" />\n"
                + "      <option name=\"emulator\" value=\"NexusOne\" />\n"
                + "      <option name=\"emulatorAdlOptions\" value=\"\" />\n"
                + "      <option name=\"fastPackaging\" value=\"true\" />\n"
                + "      <option name=\"fullScreenHeight\" value=\"0\" />\n"
                + "      <option name=\"fullScreenWidth\" value=\"0\" />\n"
                + "      <option name=\"launchUrl\" value=\"false\" />\n"
                + "      <option name=\"launcherParameters\">\n"
                + "        <LauncherParameters>\n"
                + "          <option name=\"browser\" value=\"a7bb68e0-33c0-4d6f-a81a-aac1fdb870c8\" />\n"
                + "          <option name=\"launcherType\" value=\"OSDefault\" />\n"
                + "          <option name=\"newPlayerInstance\" value=\"false\" />\n"
                + "          <option name=\"playerPath\" value=\"FlashPlayerDebugger.exe\" />\n"
                + "        </LauncherParameters>\n"
                + "      </option>\n"
                + "      <option name=\"mobileRunTarget\" value=\"Emulator\" />\n"
                + "      <option name=\"moduleName\" value=\"" + simpleName + "\" />\n"
                + "      <option name=\"overriddenMainClass\" value=\"\" />\n"
                + "      <option name=\"overriddenOutputFileName\" value=\"\" />\n"
                + "      <option name=\"overrideMainClass\" value=\"false\" />\n"
                + "      <option name=\"runTrusted\" value=\"true\" />\n"
                + "      <option name=\"screenDpi\" value=\"0\" />\n"
                + "      <option name=\"screenHeight\" value=\"0\" />\n"
                + "      <option name=\"screenWidth\" value=\"0\" />\n"
                + "      <option name=\"url\" value=\"http://\" />\n"
                + "      <option name=\"usbDebugPort\" value=\"7936\" />\n"
                + "      <method v=\"2\">\n"
                + "        <option name=\"Make\" enabled=\"true\" />\n"
                + "      </method>\n"
                + "    </configuration>\n"
                + "  </component>\n"
                + "  <component name=\"SharedIndexes\">\n"
                + "    <attachedChunks>\n"
                + "      <set>\n"
                + "        <option value=\"bundled-jdk-9f38398b9061-39b83d9b5494-intellij.indexing.shared.core-IU-241.18034.62\" />\n"
                + "        <option value=\"bundled-js-predefined-1d06a55b98c1-0b3e54e931b4-JavaScript-IU-241.18034.62\" />\n"
                + "      </set>\n"
                + "    </attachedChunks>\n"
                + "  </component>\n"
                + "  <component name=\"SpellCheckerSettings\" RuntimeDictionaries=\"0\" Folders=\"0\" CustomDictionaries=\"0\" DefaultDictionary=\"application-level\" UseSingleDictionary=\"true\" transferred=\"true\" />\n"
                + "  <component name=\"TaskManager\">\n"
                + "    <task active=\"true\" id=\"Default\" summary=\"Default task\">\n"
                + "      <changelist id=\"" + UUID.randomUUID() + "\" name=\"Changes\" comment=\"\" />\n"
                + "      <created>" + created + "</created>\n"
                + "      <option name=\"number\" value=\"Default\" />\n"
                + "      <option name=\"presentableId\" value=\"Default\" />\n"
                + "      <updated>" + created + "</updated>\n"
                + "      <workItem from=\"" + created + "\" duration=\"0\" />\n"
                + "    </task>\n"
                + "    <servers />\n"
                + "  </component>\n"
                + "  <component name=\"TypeScriptGeneratedFilesManager\">\n"
                + "    <option name=\"version\" value=\"3\" />\n"
                + "  </component>\n"
                + "</project>";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, "workspace.xml"))) {
            fos.write(Utf8Helper.getBytes(workspaceXml));
        }

        String modulesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<project version=\"4\">\n"
                + "  <component name=\"ProjectModuleManager\">\n"
                + "    <modules>\n"
                + "      <module fileurl=\"file://$PROJECT_DIR$/" + simpleName + ".iml" + "\" filepath=\"$PROJECT_DIR$/" + simpleName + ".iml" + "\" />\n"
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
                + "  <component name=\"ProjectRootManager\">\n"
                + "    <output url=\"file://$PROJECT_DIR$/out\" />\n"
                + "  </component>\n"
                + "</project>";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, "misc.xml"))) {
            fos.write(Utf8Helper.getBytes(miscXml));
        }

        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, ".name"))) {
            fos.write(Utf8Helper.getBytes(simpleName + ".iml"));
        }

        String gitIgnore = "# Default ignored files\n"
                + "/shelf/\n"
                + "/workspace.xml\n"
                + "# Editor-based HTTP Client requests\n"
                + "/httpRequests/\n"
                + "# Datasource local storage ignored files\n"
                + "/dataSources/\n"
                + "/dataSources.local.xml\n";
        try (FileOutputStream fos = new FileOutputStream(new File(ideaDir, ".gitignore"))) {
            fos.write(Utf8Helper.getBytes(gitIgnore));
        }

        boolean parallel = Configuration.parallelSpeedUp.get();
        ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false, false, "/_assets/", Configuration.linkAllClasses.get(), false);
        swf.exportActionScript(handler, new File(outDir, "src").getAbsolutePath(), scriptExportSettings, parallel, eventListener);
    }
}
