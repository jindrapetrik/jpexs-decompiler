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
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
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

/**
 * Exports SWF to FlashDevelop project.
 *
 * @author JPEXS
 */
public class SwfVsCodeExporter {

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
     * @param outDir Output file
     * @param air
     * @param handler Handler for abort, retry, ignore
     * @throws IOException On I/O error
     */
    public void exportVsCodeProject(SWF swf, File outDir, boolean air, AbortRetryIgnoreHandler handler) throws IOException {
        exportVsCodeProject(swf, outDir, air, handler, null);
    }

    /**
     * Exports SWF to FlashDevelop project.
     *
     * @param swf SWF to export
     * @param outDir Output file
     * @param air AIR
     * @param handler Handler for abort, retry, ignore
     * @param eventListener Event listener
     * @throws IOException On I/O error
     */
    public void exportVsCodeProject(SWF swf, File outDir, boolean air, AbortRetryIgnoreHandler handler, EventListener eventListener) throws IOException {
        if (!swf.isAS3()) {
            throw new IllegalArgumentException("SWF must be AS3");
        }

        if (!canExportSwf(swf)) {
            throw new IllegalArgumentException("SWF must not contain main timeline");
        }

        String simpleName = outDir.getName();
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

        project = "{\n"
                + "\t\"config\": \"air\",\n"
                + "\t\"compilerOptions\": {\n"
                + "\t\t\"source-path\": [\n"
                + "\t\t\t\"src\"\n"
                + "\t\t],\n"
                + "\t\t\"library-path\": [\n"
                + "\t\t\t\"libs\"\n"
                + "\t\t],\n"
                + "\t\t\"output\": \"bin/" + simpleNameNoSpaces + ".swf\",\n"
                + "\t\t\"default-background-color\": \"" + (bgColorTag == null ? "#FFFFFF" : bgColorTag.backgroundColor.toHexRGB()) + "\",\n"
                + "\t\t\"default-frame-rate\": " + doubleToString(swf.frameRate) + ",\n"
                + "\t\t\"default-size\": {\n"
                + "\t\t\t\"width\": " + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor) + ",\n"
                + "\t\t\t\"height\": " + doubleToString(swf.displayRect.getWidth() / SWF.unitDivisor) + "\n"
                + "\t\t},\n"
                + "\t\t\"swf-version\": " + swf.version + "\n"
                //
                + "\t},\n"
                + "\t\"application\": \"src/" + simpleNameNoSpaces + "-app.xml\",\n"
                + "\t\"mainClass\": \"" + documentClass + "\"\n"
                + "}";

        try (FileOutputStream fos = new FileOutputStream(outDir.toPath().resolve("asconfig.json").toFile())) {
            fos.write(Utf8Helper.getBytes(project));
        }

        File vscodeDir = outDir.toPath().resolve(".vscode").toFile();
        vscodeDir.mkdirs();

        String launch = "{\n"
                + "\t\"version\": \"0.2.0\",\n"
                + "\t\"configurations\": [\n"
                + "\t\t{\n"
                + "\t\t\t\"type\": \"swf\",\n"
                + "\t\t\t\"request\": \"launch\",\n"
                + "\t\t\t\"name\": \"Launch SWF\"\n"
                + "\t\t}\n"
                + "\t]\n"
                + "}";
        try (FileOutputStream fos = new FileOutputStream(vscodeDir.toPath().resolve("launch.json").toFile())) {
            fos.write(Utf8Helper.getBytes(launch));
        }

        String settings = "{\n"
                + "\t\"as3mxml.sdk.framework\": \"c:\\\\flex\"\n"
                + "}";
        try (FileOutputStream fos = new FileOutputStream(vscodeDir.toPath().resolve("settings.json").toFile())) {
            fos.write(Utf8Helper.getBytes(settings));
        }

        String app = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
                + "<application xmlns=\"http://ns.adobe.com/air/application/3.1\">\n"
                + "\n"
                + "<!-- Adobe AIR Application Descriptor File Template.\n"
                + "\n"
                + "\tSpecifies parameters for identifying, installing, and launching AIR applications.\n"
                + "\n"
                + "\txmlns - The Adobe AIR namespace: http://ns.adobe.com/air/application/3.1\n"
                + "\t\t\tThe last segment of the namespace specifies the version \n"
                + "\t\t\tof the AIR runtime required for this application to run.\n"
                + "\t\t\t\n"
                + "\tminimumPatchLevel - The minimum patch level of the AIR runtime required to run \n"
                + "\t\t\tthe application. Optional.\n"
                + "-->\n"
                + "\n"
                + "\t<!-- A universally unique application identifier. Must be unique across all AIR applications.\n"
                + "\tUsing a reverse DNS-style name as the id is recommended. (Eg. com.example.ExampleApplication.) Required. -->\n"
                + "\t<id>test-flex</id>\n"
                + "\n"
                + "\t<!-- Used as the filename for the application. Required. -->\n"
                + "\t<filename>" + simpleNameNoSpaces + "</filename>\n"
                + "\n"
                + "\t<!-- The name that is displayed in the AIR application installer. \n"
                + "\tMay have multiple values for each language. See samples or xsd schema file. Optional. -->\n"
                + "\t<name></name>\n"
                + "\t\n"
                + "\t<!-- A string value of the format <0-999>.<0-999>.<0-999> that represents application version which can be used to check for application upgrade. \n"
                + "\tValues can also be 1-part or 2-part. It is not necessary to have a 3-part value.\n"
                + "\tAn updated version of application must have a versionNumber value greater than the previous version. Required for namespace >= 2.5 . -->\n"
                + "\t<versionNumber>1.0.0</versionNumber>\n"
                + "\t\t         \n"
                + "\t<!-- A string value (such as \"v1\", \"2.5\", or \"Alpha 1\") that represents the version of the application, as it should be shown to users. Optional. -->\n"
                + "\t<!-- <versionLabel></versionLabel> -->\n"
                + "\n"
                + "\t<!-- Description, displayed in the AIR application installer.\n"
                + "\tMay have multiple values for each language. See samples or xsd schema file. Optional. -->\n"
                + "\t<!-- <description></description> -->\n"
                + "\n"
                + "\t<!-- Copyright information. Optional -->\n"
                + "\t<!-- <copyright></copyright> -->\n"
                + "\n"
                + "\t<!-- Publisher ID. Used if you're updating an application created prior to 1.5.3 -->\n"
                + "\t<!-- <publisherID></publisherID> -->\n"
                + "\n"
                + "\t<!-- Settings for the application's initial window. Required. -->\n"
                + "\t<initialWindow>\n"
                + "\t\t<!-- The main SWF or HTML file of the application. Required. -->\n"
                + "\t\t<!-- Note: In Flash Builder, the SWF reference is set automatically. -->\n"
                + "\t\t<content>" + simpleNameNoSpaces + ".swf</content>\n"
                + "\t\t\n"
                + "\t\t<!-- The title of the main window. Optional. -->\n"
                + "\t\t<!-- <title></title> -->\n"
                + "\n"
                + "\t\t<!-- The type of system chrome to use (either \"standard\" or \"none\"). Optional. Default standard. -->\n"
                + "\t\t<!-- <systemChrome></systemChrome> -->\n"
                + "\n"
                + "\t\t<!-- Whether the window is transparent. Only applicable when systemChrome is none. Optional. Default false. -->\n"
                + "\t\t<!-- <transparent></transparent> -->\n"
                + "\n"
                + "\t\t<!-- Whether the window is initially visible. Optional. Default false. -->\n"
                + "\t\t<visible>true</visible>\n"
                + "\n"
                + "\t\t<!-- Whether the user can minimize the window. Optional. Default true. -->\n"
                + "\t\t<!-- <minimizable></minimizable> -->\n"
                + "\n"
                + "\t\t<!-- Whether the user can maximize the window. Optional. Default true. -->\n"
                + "\t\t<!-- <maximizable></maximizable> -->\n"
                + "\n"
                + "\t\t<!-- Whether the user can resize the window. Optional. Default true. -->\n"
                + "\t\t<!-- <resizable></resizable> -->\n"
                + "\n"
                + "\t\t<!-- The window's initial width in pixels. Optional. -->\n"
                + "\t\t<!-- <width></width> -->\n"
                + "\n"
                + "\t\t<!-- The window's initial height in pixels. Optional. -->\n"
                + "\t\t<!-- <height></height> -->\n"
                + "\n"
                + "\t\t<!-- The window's initial x position. Optional. -->\n"
                + "\t\t<!-- <x></x> -->\n"
                + "\n"
                + "\t\t<!-- The window's initial y position. Optional. -->\n"
                + "\t\t<!-- <y></y> -->\n"
                + "\n"
                + "\t\t<!-- The window's minimum size, specified as a width/height pair in pixels, such as \"400 200\". Optional. -->\n"
                + "\t\t<!-- <minSize></minSize> -->\n"
                + "\n"
                + "\t\t<!-- The window's initial maximum size, specified as a width/height pair in pixels, such as \"1600 1200\". Optional. -->\n"
                + "\t\t<!-- <maxSize></maxSize> -->\n"
                + "\n"
                + "        <!-- The initial aspect ratio of the app when launched (either \"portrait\" or \"landscape\"). Optional. Mobile only. Default is the natural orientation of the device -->\n"
                + "\n"
                + "        <!-- <aspectRatio></aspectRatio> -->\n"
                + "\n"
                + "        <!-- Whether the app will begin auto-orienting on launch. Optional. Mobile only. Default false -->\n"
                + "\n"
                + "        <!-- <autoOrients></autoOrients> -->\n"
                + "\n"
                + "        <!-- Whether the app launches in full screen. Optional. Mobile only. Default false -->\n"
                + "\n"
                + "        <!-- <fullScreen></fullScreen> -->\n"
                + "\n"
                + "        <!-- The render mode for the app (either auto, cpu, gpu, or direct). Optional. Default auto -->\n"
                + "\n"
                + "        <!-- <renderMode></renderMode> -->\n"
                + "\n"
                + "\t\t<!-- Whether or not to pan when a soft keyboard is raised or lowered (either \"pan\" or \"none\").  Optional.  Defaults \"pan.\" -->\n"
                + "\t\t<!-- <softKeyboardBehavior></softKeyboardBehavior> -->\n"
                + "\t</initialWindow>\n"
                + "\n"
                + "\t<!-- We recommend omitting the supportedProfiles element, -->\n"
                + "\t<!-- which in turn permits your application to be deployed to all -->\n"
                + "\t<!-- devices supported by AIR. If you wish to restrict deployment -->\n"
                + "\t<!-- (i.e., to only mobile devices) then add this element and list -->\n"
                + "\t<!-- only the profiles which your application does support. -->\n"
                + "\t<!-- <supportedProfiles>desktop extendedDesktop mobileDevice extendedMobileDevice</supportedProfiles> -->\n"
                + "\n"
                + "\t<!-- The subpath of the standard default installation location to use. Optional. -->\n"
                + "\t<!-- <installFolder></installFolder> -->\n"
                + "\n"
                + "\t<!-- The subpath of the Programs menu to use. (Ignored on operating systems without a Programs menu.) Optional. -->\n"
                + "\t<!-- <programMenuFolder></programMenuFolder> -->\n"
                + "\n"
                + "\t<!-- The icon the system uses for the application. For at least one resolution,\n"
                + "\tspecify the path to a PNG file included in the AIR package. Optional. -->\n"
                + "\t<!-- <icon>\n"
                + "\t\t<image16x16></image16x16>\n"
                + "\t\t<image32x32></image32x32>\n"
                + "\t\t<image36x36></image36x36>\n"
                + "\t\t<image48x48></image48x48>\n"
                + "\t\t<image57x57></image57x57>\n"
                + "\t\t<image72x72></image72x72>\n"
                + "\t\t<image114x114></image114x114>\n"
                + "\t\t<image128x128></image128x128>\n"
                + "\t</icon> -->\n"
                + "\n"
                + "\t<!-- Whether the application handles the update when a user double-clicks an update version\n"
                + "\tof the AIR file (true), or the default AIR application installer handles the update (false).\n"
                + "\tOptional. Default false. -->\n"
                + "\t<!-- <customUpdateUI></customUpdateUI> -->\n"
                + "\t\n"
                + "\t<!-- Whether the application can be launched when the user clicks a link in a web browser.\n"
                + "\tOptional. Default false. -->\n"
                + "\t<!-- <allowBrowserInvocation></allowBrowserInvocation> -->\n"
                + "\n"
                + "\t<!-- Listing of file types for which the application can register. Optional. -->\n"
                + "\t<!-- <fileTypes> -->\n"
                + "\n"
                + "\t\t<!-- Defines one file type. Optional. -->\n"
                + "\t\t<!-- <fileType> -->\n"
                + "\n"
                + "\t\t\t<!-- The name that the system displays for the registered file type. Required. -->\n"
                + "\t\t\t<!-- <name></name> -->\n"
                + "\n"
                + "\t\t\t<!-- The extension to register. Required. -->\n"
                + "\t\t\t<!-- <extension></extension> -->\n"
                + "\t\t\t\n"
                + "\t\t\t<!-- The description of the file type. Optional. -->\n"
                + "\t\t\t<!-- <description></description> -->\n"
                + "\t\t\t\n"
                + "\t\t\t<!-- The MIME content type. -->\n"
                + "\t\t\t<!-- <contentType></contentType> -->\n"
                + "\t\t\t\n"
                + "\t\t\t<!-- The icon to display for the file type. Optional. -->\n"
                + "\t\t\t<!-- <icon>\n"
                + "\t\t\t\t<image16x16></image16x16>\n"
                + "\t\t\t\t<image32x32></image32x32>\n"
                + "\t\t\t\t<image48x48></image48x48>\n"
                + "\t\t\t\t<image128x128></image128x128>\n"
                + "\t\t\t</icon> -->\n"
                + "\t\t\t\n"
                + "\t\t<!-- </fileType> -->\n"
                + "\t<!-- </fileTypes> -->\n"
                + "\n"
                + "    <!-- iOS specific capabilities -->\n"
                + "\t<!-- <iPhone> -->\n"
                + "\t\t<!-- A list of plist key/value pairs to be added to the application Info.plist -->\n"
                + "\t\t<!-- <InfoAdditions>\n"
                + "            <![CDATA[\n"
                + "                <key>UIDeviceFamily</key>\n"
                + "                <array>\n"
                + "                    <string>1</string>\n"
                + "                    <string>2</string>\n"
                + "                </array>\n"
                + "                <key>UIStatusBarStyle</key>\n"
                + "                <string>UIStatusBarStyleBlackOpaque</string>\n"
                + "                <key>UIRequiresPersistentWiFi</key>\n"
                + "                <string>YES</string>\n"
                + "            ]]>\n"
                + "        </InfoAdditions> -->\n"
                + "        <!-- A list of plist key/value pairs to be added to the application Entitlements.plist -->\n"
                + "\t\t<!-- <Entitlements>\n"
                + "            <![CDATA[\n"
                + "                <key>keychain-access-groups</key>\n"
                + "                <array>\n"
                + "                    <string></string>\n"
                + "                    <string></string>\n"
                + "                </array>\n"
                + "            ]]>\n"
                + "        </Entitlements> -->\n"
                + "\t<!-- Display Resolution for the app (either \"standard\" or \"high\"). Optional. Default \"standard\" -->\n"
                + "\t<!-- <requestedDisplayResolution></requestedDisplayResolution> -->\n"
                + "\t<!-- </iPhone> -->\n"
                + "\n"
                + "\t<!-- Specify Android specific tags that get passed to AndroidManifest.xml file. -->\n"
                + "    <!--<android> -->\n"
                + "    <!--\t<manifestAdditions>\n"
                + "\t\t<![CDATA[\n"
                + "\t\t\t<manifest android:installLocation=\"auto\">\n"
                + "\t\t\t\t<uses-permission android:name=\"android.permission.INTERNET\"/>\n"
                + "\t\t\t\t<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/>\n"
                + "\t\t\t\t<uses-permission android:name=\"android.permission.ACCESS_FINE_LOCATION\"/>\n"
                + "\t\t\t\t<uses-feature android:required=\"true\" android:name=\"android.hardware.touchscreen.multitouch\"/>\n"
                + "\t\t\t\t<application android:enabled=\"true\">\n"
                + "\t\t\t\t\t<activity android:excludeFromRecents=\"false\">\n"
                + "\t\t\t\t\t\t<intent-filter>\n"
                + "\t\t\t\t\t\t\t<action android:name=\"android.intent.action.MAIN\"/>\n"
                + "\t\t\t\t\t\t\t<category android:name=\"android.intent.category.LAUNCHER\"/>\n"
                + "\t\t\t\t\t\t</intent-filter>\n"
                + "\t\t\t\t\t</activity>\n"
                + "\t\t\t\t</application>\n"
                + "            </manifest>\n"
                + "\t\t]]>\n"
                + "        </manifestAdditions> -->\n"
                + "\t    <!-- Color depth for the app (either \"32bit\" or \"16bit\"). Optional. Default 16bit before namespace 3.0, 32bit after -->\n"
                + "        <!-- <colorDepth></colorDepth> -->\n"
                + "    <!-- </android> -->\n"
                + "\t<!-- End of the schema for adding the android specific tags in AndroidManifest.xml file -->\n"
                + "\n"
                + "</application>";

        File srcDir = outDir.toPath().resolve("src").toFile();
        srcDir.mkdirs();

        try (FileOutputStream fos = new FileOutputStream(srcDir.toPath().resolve(simpleNameNoSpaces + "-app.xml").toFile())) {
            fos.write(Utf8Helper.getBytes(app));
        }

        File libsDir = outDir.toPath().resolve("libs").toFile();
        libsDir.mkdirs();

        boolean parallel = Configuration.parallelSpeedUp.get();
        ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, false, true, false, false, "/_assets/", Configuration.linkAllClasses.get(), false);
        swf.exportActionScript(handler, srcDir.getAbsolutePath(), scriptExportSettings, parallel, eventListener);
    }
}
