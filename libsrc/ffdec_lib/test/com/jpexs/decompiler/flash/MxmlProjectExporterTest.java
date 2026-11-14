/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.script.MxmlExporter;
import com.jpexs.decompiler.flash.exporters.swf.SwfMxmlProjectExporter.ProjectType;
import com.jpexs.decompiler.flash.exporters.swf.SwfMxmlProjectExporter;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MxmlProjectExporterTest {

    private SWF input(String name) throws Exception {
        try (InputStream stream = Files.newInputStream(Paths.get("testdata/mxml/" + name + ".swf"))) {
            return new SWF(stream, false);
        }
    }

    private Document xml(Path path) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
    }

    private String text(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private void assertFlashDevelopSectionComments(Document project, boolean air) {
        String[][] sections = {{"output", "Output SWF options"},
            {"classpaths", "Other classes to be compiled into your SWF"},
            {"build", "Build options"},
            {"compileTargets", "Class files to compile (other referenced classes will automatically be included)"},
            {"hiddenPaths", "Paths to exclude from the Project Explorer tree"},
            {"preBuildCommand", "Executed before build"},
            {"postBuildCommand", "Executed after build"},
            {"options", "Other project options"}};
        for (String[] section : sections) {
            Node element = project.getElementsByTagName(section[0]).item(0);
            Assert.assertNotNull(element, "Missing FlashDevelop section " + section[0]);
            Node previous = element.getPreviousSibling();
            while (previous != null && previous.getNodeType() == Node.TEXT_NODE && previous.getTextContent().trim().isEmpty()) {
                previous = previous.getPreviousSibling();
            }
            Assert.assertNotNull(previous, "Missing comment before " + section[0]);
            Assert.assertEquals(previous.getNodeType(), Node.COMMENT_NODE, section[0]);
            Assert.assertEquals(previous.getNodeValue().trim(), section[1]);
        }
        if (air) {
            for (String section : new String[]{"includeLibraries", "libraryPaths", "externalLibraryPaths", "rslPaths", "intrinsics", "library", "storage"}) {
                Assert.assertEquals(project.getElementsByTagName(section).getLength(), 1, section);
            }
        }
    }

    private void remove(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.delete(file);
            }
        }
    }

    @DataProvider(name = "supported")
    public Object[][] supported() {
        return new Object[][]{{"Main"}, {"FeatureShowcase"}, {"ClassDependencies"}, {"AssetsAndSkins"},
            {"DesktopMain"}, {"MxDesktopMain"}, {"FeatureModule"}, {"MxFeatureModule"}, {"MxApplicationMain"},
            {"InheritedApplication"}, {"InheritedModule"}, {"MxmlInheritedApplication"},
            {"DataTypes"}, {"DataAndResources"}, {"AdvancedMxml"}, {"OptimizedShowcase"},
            {"CompactHost"}, {"LegacyShowcase"}, {"LegacyStates"}};
    }

    @Test(dataProvider = "supported")
    public void detectsSupportedDocumentsWithoutDecompilingMethods(String name) throws Exception {
        SWF swf = input(name);
        Map<MethodBody, Object> graphs = new IdentityHashMap<>();
        for (ScriptPack pack : swf.getAS3Packs()) {
            for (MethodBody body : pack.abc.bodies) {
                graphs.put(body, body.convertedItems);
            }
        }
        Assert.assertTrue(MxmlExporter.canExportSwf(swf), name);
        for (Map.Entry<MethodBody, Object> entry : graphs.entrySet()) {
            Assert.assertSame(entry.getKey().convertedItems, entry.getValue(), "Menu detection must not decompile bytecode");
        }
    }

    @Test
    public void rejectsUnsupportedAndChangedRootsWithoutCachingStaleResults() throws Exception {
        Assert.assertFalse(MxmlExporter.canExportSwf(null));
        Assert.assertFalse(MxmlExporter.canExportSwf(new SWF()));
        try (InputStream stream = Files.newInputStream(Paths.get("testdata/as3_new/bin/as3_new.flex.swf"))) {
            Assert.assertFalse(MxmlExporter.canExportSwf(new SWF(stream, false)));
        }
        SWF swf = input("Main");
        Assert.assertTrue(MxmlExporter.canExportSwf(swf));
        ScriptPack main = swf.getAS3Packs().stream().filter(pack -> "Main".equals(pack.getClassPath().toRawString())).findFirst().get();
        int index = ((TraitClass) main.getPublicTrait()).class_info;
        int parent = main.abc.instance_info.get(index).super_index;
        main.abc.instance_info.get(index).super_index = 0;
        Assert.assertFalse(MxmlExporter.canExportSwf(swf));
        main.abc.instance_info.get(index).super_index = parent;
        Assert.assertTrue(MxmlExporter.canExportSwf(swf));
        main.abc.instance_info.get(index).super_index = main.abc.instance_info.get(index).name_index;
        Assert.assertFalse(MxmlExporter.canExportSwf(swf));
        main.abc.instance_info.get(index).super_index = parent;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag symbols = (SymbolClassTag) tag;
                int document = symbols.tags.indexOf(0);
                if (document >= 0) {
                    symbols.names.set(document, "missing.Document");
                }
            }
        }
        Assert.assertFalse(MxmlExporter.canExportSwf(swf));
    }

    @Test
    public void rejectsTimelineContentThatWouldBeLostBySourceExport() throws Exception {
        SWF swf = input("Main");
        Assert.assertTrue(MxmlExporter.canExportSwf(swf));
        swf.addTag(new PlaceObject2Tag(swf));
        Assert.assertFalse(MxmlExporter.canExportSwf(swf));
    }

    @Test
    public void rejectsRootWithoutSupportedConstructor() throws Exception {
        SWF swf = input("Main");
        ScriptPack main = swf.getAS3Packs().stream().filter(pack -> "Main".equals(pack.getClassPath().toRawString())).findFirst().get();
        int cls = ((TraitClass) main.getPublicTrait()).class_info;
        AVM2Code empty = new AVM2Code();
        empty.code.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, new int[0]));
        main.abc.findBody(main.abc.instance_info.get(cls).iinit_index).setCode(empty);
        Assert.assertFalse(MxmlExporter.canExportSwf(swf));
    }

    @Test
    public void detectsDirectModuleWithoutBootstrap() throws Exception {
        SWF swf = input("FeatureModule");
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag symbols = (SymbolClassTag) tag;
                int document = symbols.tags.indexOf(0);
                if (document >= 0) {
                    symbols.names.set(document, "FeatureModule");
                }
            }
        }
        Assert.assertEquals(MxmlExporter.getRootType(swf), "spark.modules.Module");
    }

    @DataProvider(name = "projects")
    public Object[][] projects() {
        return new Object[][]{{ProjectType.MXML}, {ProjectType.FLASH_DEVELOP}, {ProjectType.VS_CODE}, {ProjectType.INTELLIJ_IDEA}};
    }

    @Test(dataProvider = "projects")
    public void exportsLocalizedMxmlProjectsWithDependenciesAndCorrectEntryPoint(ProjectType type) throws Exception {
        Path output = Files.createTempDirectory("mxml-project-");
        try {
            Path destination = type == ProjectType.FLASH_DEVELOP ? output.resolve("Project & Demo.as3proj") : output;
            Path main = new SwfMxmlProjectExporter().exportProject(input("DataAndResources"), destination.toFile(), type).toPath();
            Path source = type == ProjectType.MXML ? output : output.resolve("src");
            Assert.assertEquals(main, source.resolve("DataAndResources.mxml"));
            Assert.assertTrue(Files.isRegularFile(source.resolve("model/Product.as")));
            Assert.assertTrue(Files.isRegularFile(source.resolve("locale/en_GB/showcase.properties")));
            Assert.assertTrue(Files.isRegularFile(source.resolve("DataAndResources-config.xml")));
            Assert.assertTrue(Files.isDirectory(source.resolve("_assets")));
            Assert.assertFalse(Files.exists(source.resolve("DataAndResources.as")));
            if (type == ProjectType.FLASH_DEVELOP) {
                Document project = xml(destination);
                assertFlashDevelopSectionComments(project, false);
                Assert.assertEquals(((Element) project.getElementsByTagName("compile").item(0)).getAttribute("path"), "src/DataAndResources.mxml");
                NodeList options = project.getElementsByTagName("option");
                boolean localeConfig = false;
                for (int i = 0; i < options.getLength(); i++) {
                    localeConfig |= "en_US,en_GB".equals(((Element) options.item(i)).getAttribute("locale"));
                }
                Assert.assertTrue(localeConfig);
            } else if (type == ProjectType.VS_CODE) {
                String project = text(output.resolve("asconfig.json"));
                Assert.assertTrue(project.contains("\"config\": \"flex\""));
                Assert.assertTrue(project.contains("\"files\": [\"src/DataAndResources.mxml\"]"));
                Assert.assertTrue(project.contains("\"src/locale/{locale}\""));
                Assert.assertTrue(project.contains("\"locale\": [\"en_US\", \"en_GB\"]"));
                Assert.assertFalse(project.contains("\"load-config\""), "Do not load an automatically loaded main config twice");
                Assert.assertFalse(project.contains("\"application\""));
                Assert.assertTrue(Files.isRegularFile(output.resolve(".vscode/tasks.json")));
                Assert.assertTrue(Files.isRegularFile(output.resolve(".vscode/extensions.json")));
                Assert.assertFalse(Files.exists(output.resolve(".vscode/settings.json")), "Do not invent a local SDK path");
            } else if (type == ProjectType.INTELLIJ_IDEA) {
                Document project = xml(output.resolve("DataAndResources.iml"));
                Element configuration = (Element) project.getElementsByTagName("configuration").item(0);
                Assert.assertEquals(configuration.getAttribute("pure-as"), "false");
                Assert.assertEquals(configuration.getAttribute("main-class"), "DataAndResources");
                Assert.assertTrue(text(output.resolve("DataAndResources.iml")).contains("-locale=en_US,en_GB"));
                xml(output.resolve(".idea/modules.xml"));
                xml(output.resolve(".idea/runConfigurations/DataAndResources.xml"));
            }
            if (type != ProjectType.MXML) {
                Assert.assertFalse(Files.exists(output.resolve("application.xml")));
            }
        } finally {
            remove(output);
        }
    }

    @Test(dataProvider = "projects")
    public void rejectsUnsupportedProjectBeforeWritingFiles(ProjectType type) throws Exception {
        Path output = Files.createTempDirectory("mxml-project-invalid-");
        try {
            Path destination = type == ProjectType.FLASH_DEVELOP ? output.resolve("Invalid.as3proj") : output;
            try {
                new SwfMxmlProjectExporter().exportProject(new SWF(), destination.toFile(), type);
                Assert.fail("Unsupported input must be rejected");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("supported Flex MXML document root"));
            }
            try (Stream<Path> files = Files.list(output)) {
                Assert.assertEquals(files.count(), 0L);
            }
        } finally {
            remove(output);
        }
    }

    @Test
    public void preservesAirAndModuleProjectNatureAndOverwritePreference() throws Exception {
        Path output = Files.createTempDirectory("mxml-project-nature-");
        boolean overwrite = Configuration.overwriteExistingFiles.get();
        try {
            SwfMxmlProjectExporter exporter = new SwfMxmlProjectExporter();
            exporter.exportProject(input("DesktopMain"), output.toFile(), ProjectType.INTELLIJ_IDEA);
            Element configuration = (Element) xml(output.resolve("DesktopMain.iml")).getElementsByTagName("configuration").item(0);
            Assert.assertEquals(configuration.getAttribute("target-platform"), "Desktop");
            Assert.assertEquals(xml(output.resolve("application.xml")).getElementsByTagName("content").item(0).getTextContent(), "DesktopMain.swf");
            Files.write(output.resolve("DesktopMain.iml"), "keep project".getBytes(StandardCharsets.UTF_8));
            Configuration.overwriteExistingFiles.set(false);
            exporter.exportProject(input("DesktopMain"), output.toFile(), ProjectType.INTELLIJ_IDEA);
            Assert.assertEquals(text(output.resolve("DesktopMain.iml")), "keep project");
            Configuration.overwriteExistingFiles.set(true);
            Path flashDevelop = output.resolve("fd-air/Desktop project.as3proj");
            exporter.exportProject(input("DesktopMain"), flashDevelop.toFile(), ProjectType.FLASH_DEVELOP);
            assertFlashDevelopSectionComments(xml(flashDevelop), true);
            Assert.assertTrue(Files.isRegularFile(output.resolve("fd-air/bat/RunApp.bat")));
            Assert.assertTrue(text(output.resolve("fd-air/bat/RunApp.bat")).contains("%FD_CUR_SDK%"));
            Assert.assertEquals(xml(output.resolve("fd-air/application.xml")).getElementsByTagName("content").item(0).getTextContent(), "Desktop project.swf");
            Path vsCode = output.resolve("vs-air");
            exporter.exportProject(input("DesktopMain"), vsCode.toFile(), ProjectType.VS_CODE);
            Assert.assertTrue(text(vsCode.resolve("asconfig.json")).contains("\"config\": \"air\""));
            Assert.assertTrue(text(vsCode.resolve("asconfig.json")).contains("\"application\": \"application.xml\""));
            Path module = output.resolve("module");
            exporter.exportProject(input("FeatureModule"), module.toFile(), ProjectType.INTELLIJ_IDEA);
            configuration = (Element) xml(module.resolve("FeatureModule.iml")).getElementsByTagName("configuration").item(0);
            Assert.assertEquals(configuration.getAttribute("output-type"), "RuntimeLoadedModule");
            Assert.assertFalse(Files.exists(module.resolve("application.xml")));
            Assert.assertFalse(Files.exists(module.resolve(".idea/runConfigurations/FeatureModule.xml")));
        } finally {
            Configuration.overwriteExistingFiles.set(overwrite);
            remove(output);
        }
    }
}
