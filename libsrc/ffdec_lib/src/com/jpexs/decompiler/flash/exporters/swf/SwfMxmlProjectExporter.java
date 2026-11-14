/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.exporters.swf;

import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.FlashPlayerVersion;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.MxmlExporter;
import com.jpexs.helpers.Helper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Exports recovered MXML sources and optional Flex IDE project metadata.
 *
 * @author JPEXS
 */
public class SwfMxmlProjectExporter {

    public enum ProjectType {
        MXML, FLASH_DEVELOP, VS_CODE, INTELLIJ_IDEA
    }

    /**
     * Exports a source directory or an IDE project containing recovered sources.
     * @param swf Source movie
     * @param destination Directory, or the .as3proj file for FlashDevelop
     * @param type Project format
     * @return Main MXML source file
     * @throws IOException Unsupported input or I/O error
     * @throws InterruptedException Interrupted decompilation
     */
    public File exportProject(SWF swf, File destination, ProjectType type) throws IOException, InterruptedException {
        String rootType = MxmlExporter.getRootType(swf);
        if (rootType == null) {
            throw new IOException("SWF does not have a supported Flex MXML document root");
        }
        if (type == ProjectType.MXML) {
            return new MxmlExporter().exportMxml(swf, destination);
        }
        File project = destination.getAbsoluteFile();
        File directory = type == ProjectType.FLASH_DEVELOP ? project.getParentFile() : project;
        File source = new MxmlExporter().exportMxml(swf, new File(directory, "src"));
        String mainPath = relative(directory, source);
        String mainClass = relative(new File(directory, "src"), source).replace('/', '.').replaceFirst("\\.mxml$", "");
        String name = type == ProjectType.FLASH_DEVELOP ? project.getName().replaceFirst("(?i)\\.as3proj$", "") : source.getName().replaceFirst("\\.mxml$", "");
        boolean air = rootType.endsWith("WindowedApplication");
        boolean module = rootType.endsWith(".Module");
        String output = name + ".swf";
        File config = new File(source.getParentFile(), source.getName().replaceFirst("\\.mxml$", "-config.xml"));
        List<String> locales = readLocales(config);
        Files.createDirectories(new File(directory, "bin").toPath());
        if (air) {
            write(new File(directory, "application.xml"), airDescriptor(swf, name, output));
        }
        switch (type) {
            case FLASH_DEVELOP:
                write(project, flashDevelop(swf, mainPath, output, locales, air));
                if (air) {
                    write(new File(directory, "bat/RunApp.bat"), "@echo off\r\nsetlocal\r\ncd /d \"%~dp0..\"\r\n"
                            + "set \"MXML_FLEX_SDK=%FD_CUR_SDK%\"\r\n"
                            + "if not defined MXML_FLEX_SDK set \"MXML_FLEX_SDK=%FLEX_HOME%\"\r\n"
                            + "if not exist \"%MXML_FLEX_SDK%\\bin\\adl.exe\" (\r\n"
                            + "  echo Select an Apache Flex SDK with AIR support in FlashDevelop or set FLEX_HOME.\r\n"
                            + "  exit /b 1\r\n)\r\n\"%MXML_FLEX_SDK%\\bin\\adl.exe\" application.xml bin\r\n");
                }
                break;
            case VS_CODE:
                write(new File(directory, "asconfig.json"), vsCode(swf, mainPath, output, locales, air));
                write(new File(directory, ".vscode/extensions.json"), "{\"recommendations\":[\"bowlerhatllc.vscode-as3mxml\"]}\n");
                write(new File(directory, ".vscode/tasks.json"), "{\n  \"version\": \"2.0.0\",\n  \"tasks\": [{\"type\": \"actionscript\", \"debug\": true, \"group\": {\"kind\": \"build\", \"isDefault\": true}, \"problemMatcher\": []}]\n}\n");
                if (!module) {
                    write(new File(directory, ".vscode/launch.json"), "{\"version\":\"0.2.0\",\"configurations\":[{\"type\":\"swf\",\"request\":\"launch\",\"name\":\"Launch SWF\"}]}\n");
                }
                break;
            case INTELLIJ_IDEA:
                write(new File(directory, name + ".iml"), idea(swf, name, mainClass, output, locales, air, module));
                write(new File(directory, ".idea/modules.xml"), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project version=\"4\"><component name=\"ProjectModuleManager\"><modules><module fileurl=\"file://$PROJECT_DIR$/"
                        + xml(name) + ".iml\" filepath=\"$PROJECT_DIR$/" + xml(name) + ".iml\"/></modules></component></project>\n");
                if (!module) {
                    write(new File(directory, ".idea/runConfigurations/" + name + ".xml"), "<component name=\"ProjectRunConfigurationManager\">"
                            + "<configuration default=\"false\" name=\"" + xml(name) + "\" type=\"FlashRunConfigurationType\">"
                            + "<option name=\"BCName\" value=\"" + xml(name) + "\"/><option name=\"moduleName\" value=\"" + xml(name) + "\"/>"
                            + "<method v=\"2\"><option name=\"Make\" enabled=\"true\"/></method></configuration></component>\n");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown MXML project type");
        }
        write(new File(directory, "README-MXML.txt"), "Recovered Flex MXML project.\n\n"
                + "Main source: " + mainPath + "\nRequires an Apache Flex SDK; select it in your IDE.\n"
                + "FlashDevelop: open the .as3proj file.\n"
                + "VS Code: install the ActionScript & MXML extension and open this directory.\n"
                + "IntelliJ IDEA: install the Flash/Flex plugin, open this directory, and select a Flex SDK in the module build configuration.\n"
                + (module ? "This is a runtime-loaded module; load it from a host application.\n" : "")
                + (air ? "This is an AIR desktop application; the SDK must include the AIR APIs.\n" : "")
                + "\nTo compile manually (use amxmlc for AIR):\n"
                + "mxmlc -source-path+=src -output=bin/" + quote(output) + " -- " + quote(mainPath) + "\n"
                + "Use -source-path+= when adding paths to retain locale configuration.\n");
        return source;
    }

    private String relative(File directory, File file) {
        return directory.toPath().relativize(file.toPath()).toString().replace('\\', '/');
    }

    private List<String> readLocales(File config) throws IOException {
        List<String> locales = new ArrayList<>();
        if (!config.isFile()) {
            return locales;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            NodeList elements = factory.newDocumentBuilder().parse(config).getElementsByTagName("locale-element");
            for (int i = 0; i < elements.getLength(); i++) {
                String locale = elements.item(i).getTextContent().trim();
                if (!locale.matches("[A-Za-z0-9_]+")) {
                    throw new IOException("Unsupported project locale " + locale);
                }
                if (!locales.contains(locale)) {
                    locales.add(locale);
                }
            }
        } catch (ParserConfigurationException | SAXException ex) {
            throw new IOException("Cannot read recovered locale configuration", ex);
        }
        return locales;
    }

    private void write(File file, String content) throws IOException, InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        Path path = file.toPath();
        if (Files.exists(path) && !Configuration.overwriteExistingFiles.get()) {
            return;
        }
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private String xml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private String quote(String value) {
        return "\"" + Helper.escapeJavaString(value) + "\"";
    }

    private String flashDevelop(SWF swf, String main, String output, List<String> locales, boolean air) {
        String[] version = (air ? FlashPlayerVersion.getAirBySwfVersion(swf.version) : FlashPlayerVersion.getFlashPlayerBySwfVersion(swf.version)).split("\\.");
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<project version=\"2\">\n"
                + "  <!-- Generated with " + ApplicationInfo.applicationVerName + " -->\n"
                + "  <!-- Output SWF options -->\n"
                + "  <output>\n"
                + "    <movie outputType=\"Application\" />\n"
                + "    <movie input=\"\" />\n"
                + "    <movie path=\"bin/" + xml(output) + "\" />\n"
                + "    <movie fps=\"" + Math.round(swf.frameRate) + "\" />\n"
                + "    <movie width=\"" + Math.round(swf.displayRect.getWidth() / SWF.unitDivisor) + "\" />\n"
                + "    <movie height=\"" + Math.round(swf.displayRect.getHeight() / SWF.unitDivisor) + "\" />\n"
                + "    <movie version=\"" + version[0] + "\" />\n"
                + "    <movie minorVersion=\"" + version[1] + "\" />\n"
                + "    <movie platform=\"" + (air ? "AIR" : "Flash Player") + "\" />\n"
                + (swf.getBackgroundColor() == null ? "" : "    <movie background=\"" + swf.getBackgroundColor().backgroundColor.toHexRGB() + "\" />\n")
                + "  </output>\n"
                + "  <!-- Other classes to be compiled into your SWF -->\n"
                + "  <classpaths>\n    <class path=\"src\" />\n"
                + (locales.isEmpty() ? "" : "    <class path=\"src/locale/{locale}\" />\n")
                + "  </classpaths>\n"
                + "  <!-- Build options -->\n"
                + "  <build>\n"
                + "    <option locale=\"" + xml(String.join(",", locales)) + "\" />\n"
                + "    <option additional=\"-swf-version=" + swf.version + " -static-link-runtime-shared-libraries=true\" />\n"
                + "    <option allowSourcePathOverlap=\"True\" />\n"
                + "  </build>\n"
                + (air ? "  <!-- SWC Include Libraries -->\n  <includeLibraries>\n    <!-- example: <element path=\"...\" /> -->\n  </includeLibraries>\n"
                        + "  <!-- SWC Libraries -->\n  <libraryPaths>\n    <!-- example: <element path=\"...\" /> -->\n  </libraryPaths>\n"
                        + "  <!-- External Libraries -->\n  <externalLibraryPaths>\n    <!-- example: <element path=\"...\" /> -->\n  </externalLibraryPaths>\n"
                        + "  <!-- Runtime Shared Libraries -->\n  <rslPaths>\n    <!-- example: <element path=\"...\" /> -->\n  </rslPaths>\n"
                        + "  <!-- Intrinsic Libraries -->\n  <intrinsics>\n    <!-- example: <element path=\"...\" /> -->\n  </intrinsics>\n"
                        + "  <!-- Assets to embed into the output SWF -->\n  <library>\n"
                        + "    <!-- example: <asset path=\"...\" id=\"...\" update=\"...\" glyphs=\"...\" mode=\"...\" place=\"...\" sharepoint=\"...\" /> -->\n  </library>\n" : "")
                + "  <!-- Class files to compile (other referenced classes will automatically be included) -->\n"
                + "  <compileTargets>\n    <compile path=\"" + xml(main) + "\" />\n  </compileTargets>\n"
                + "  <!-- Paths to exclude from the Project Explorer tree -->\n"
                + "  <hiddenPaths>\n"
                + (air ? "    <hidden path=\"obj\" />\n" : "    <!-- example: <hidden path=\"...\" /> -->\n")
                + "  </hiddenPaths>\n"
                + "  <!-- Executed before build -->\n  <preBuildCommand />\n"
                + "  <!-- Executed after build -->\n  <postBuildCommand alwaysRun=\"False\" />\n"
                + "  <!-- Other project options -->\n"
                + "  <options>\n    <option showHiddenPaths=\"False\" />\n"
                + "    <option testMovie=\"" + (air ? "Custom" : "Default") + "\" />\n"
                + (air ? "    <option testMovieCommand=\"bat\\RunApp.bat\" />\n" : "") + "  </options>\n"
                + (air ? "  <!-- Plugin storage -->\n  <storage />\n" : "")
                + "</project>\n";
    }

    private String vsCode(SWF swf, String main, String output, List<String> locales, boolean air) {
        return "{\n  \"config\": " + quote(air ? "air" : "flex") + ",\n  \"compilerOptions\": {\n"
                + "    \"source-path\": [\"src\"" + (locales.isEmpty() ? "" : ", \"src/locale/{locale}\"") + "],\n"
                + (locales.isEmpty() ? "" : "    \"locale\": [" + locales.stream().map(this::quote).collect(Collectors.joining(", ")) + "],\n")
                + "    \"output\": " + quote("bin/" + output) + ",\n    \"swf-version\": " + swf.version + ",\n"
                + "    \"default-frame-rate\": " + Math.round(swf.frameRate) + ",\n"
                + "    \"default-size\": {\"width\": " + Math.round(swf.displayRect.getWidth() / SWF.unitDivisor)
                + ", \"height\": " + Math.round(swf.displayRect.getHeight() / SWF.unitDivisor) + "},\n"
                + (swf.getBackgroundColor() == null ? "" : "    \"default-background-color\": " + quote(swf.getBackgroundColor().backgroundColor.toHexRGB()) + ",\n")
                + "    \"static-link-runtime-shared-libraries\": true\n  },\n"
                + (air ? "  \"application\": \"application.xml\",\n" : "")
                + "  \"files\": [" + quote(main) + "]\n}\n";
    }

    private String idea(SWF swf, String name, String main, String output, List<String> locales, boolean air, boolean module) {
        String options = "-swf-version=" + swf.version + " -static-link-runtime-shared-libraries=true";
        options += " -default-frame-rate=" + Math.round(swf.frameRate) + " -default-size="
                + Math.round(swf.displayRect.getWidth() / SWF.unitDivisor) + "," + Math.round(swf.displayRect.getHeight() / SWF.unitDivisor);
        if (swf.getBackgroundColor() != null) {
            options += " -default-background-color=" + swf.getBackgroundColor().backgroundColor.toHexRGB();
        }
        if (!locales.isEmpty()) {
            options += " -locale=" + String.join(",", locales);
            options += " -source-path+=\"$MODULE_DIR$/src/locale/{locale}\"";
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<module type=\"Flex\" version=\"4\">\n"
                + "  <component name=\"FlexBuildConfigurationManager\" active=\"" + xml(name) + "\"><configurations>\n"
                + "    <configuration name=\"" + xml(name) + "\" pure-as=\"false\" main-class=\"" + xml(main) + "\""
                + " target-platform=\"" + (air ? "Desktop" : "Web") + "\" output-type=\"" + (module ? "RuntimeLoadedModule" : "Application") + "\""
                + " output-file=\"" + xml(output) + "\" output-folder=\"$MODULE_DIR$/bin\">\n"
                + "      <dependencies target-player=\"" + FlashPlayerVersion.getFlashPlayerBySwfVersion(swf.version) + "\"><sdk name=\"flex\"/></dependencies>\n"
                + "      <compiler-options><option name=\"additionalOptions\" value=\"" + xml(options) + "\"/></compiler-options>\n"
                + (air ? "      <packaging-air-desktop use-generated-descriptor=\"false\" custom-descriptor-path=\"$MODULE_DIR$/application.xml\"/>\n"
                        : "      <packaging-air-desktop/>\n")
                + "      <packaging-android/><packaging-ios/>\n"
                + "    </configuration>\n  </configurations><compiler-options/></component>\n"
                + "  <component name=\"NewModuleRootManager\" inherit-compiler-output=\"true\"><exclude-output/>"
                + "<content url=\"file://$MODULE_DIR$\"><sourceFolder url=\"file://$MODULE_DIR$/src\" isTestSource=\"false\"/>"
                + "<excludeFolder url=\"file://$MODULE_DIR$/bin\"/></content>"
                + "<orderEntry type=\"jdk\" jdkName=\"flex\" jdkType=\"Flex SDK Type (new)\"/><orderEntry type=\"sourceFolder\" forTests=\"false\"/></component>\n</module>\n";
    }

    private String airDescriptor(SWF swf, String name, String output) {
        String id = name.replaceAll("[^A-Za-z0-9.-]", "");
        if (id.isEmpty()) {
            id = "RecoveredApplication";
        }
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<application xmlns=\"http://ns.adobe.com/air/application/"
                + FlashPlayerVersion.getAirBySwfVersion(swf.version) + "\"><id>" + xml(id) + "</id>"
                + "<versionNumber>1.0.0</versionNumber><filename>" + xml(name) + "</filename><name>" + xml(name) + "</name>"
                + "<initialWindow><content>" + xml(output) + "</content><visible>true</visible></initialWindow></application>\n";
    }
}
