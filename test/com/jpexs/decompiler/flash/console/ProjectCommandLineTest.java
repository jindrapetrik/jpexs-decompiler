/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProjectCommandLineTest {

    private static final String[] TYPES = {"flashdevelop", "flashdevelop_mxml", "intellij", "intellij_mxml", "vscode", "vscode_mxml"};

    private SWF input(boolean mxml) throws Exception {
        Path path = Paths.get(mxml ? "libsrc/ffdec_lib/testdata/mxml/Main.swf" : "libsrc/ffdec_lib/testdata/flexedit/Main.swf");
        try (InputStream stream = Files.newInputStream(path)) {
            return new SWF(stream, path.toAbsolutePath().toString(), path.getFileName().toString(), false);
        }
    }

    private void remove(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private Document xml(Path path) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
    }

    private String text(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private Path export(SWF swf, Path output, String type, boolean multiple) throws Exception {
        return CommandLineArgumentParser.exportProject(swf, output.toString(), type, multiple, Collections.emptyMap(),
                new ConsoleAbortRetryIgnoreHandler(AbortRetryIgnoreHandler.IGNORE, 0), null).toPath();
    }

    @DataProvider(name = "projectTypes")
    public Object[][] projectTypes() {
        List<Object[]> result = new ArrayList<>();
        for (String type : TYPES) {
            result.add(new Object[]{type});
        }
        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "outputModes")
    public Object[][] outputModes() {
        List<Object[]> result = new ArrayList<>();
        for (String type : TYPES) {
            result.add(new Object[]{type, false});
            result.add(new Object[]{type, true});
        }
        return result.toArray(new Object[0][]);
    }

    @Test(dataProvider = "outputModes")
    public void exportsProjectsToCorrectDirectoriesAndReferencesActualEntryPoint(String type, boolean multiple) throws Exception {
        Path temporary = Files.createTempDirectory("cli-project-");
        boolean parallel = Configuration.parallelSpeedUp.get();
        try {
            Configuration.parallelSpeedUp.set(false);
            Path output = temporary.resolve("output");
            Path root = multiple ? output.resolve(type) : output;
            boolean mxml = type.endsWith("_mxml");
            Path result = export(input(mxml), output, type, multiple);
            Assert.assertEquals(result, type.startsWith("flashdevelop") ? root.resolve("Main.as3proj") : root);
            String entry = "src/Main." + (mxml ? "mxml" : "as");
            Assert.assertTrue(Files.isRegularFile(root.resolve(entry)), entry);
            Assert.assertFalse(Files.exists(root.resolve("src/Main." + (mxml ? "as" : "mxml"))));
            if (!mxml) {
                Assert.assertTrue(Files.isRegularFile(root.resolve("src/pkg/MyClass.as")), "AS3 dependencies must be exported too");
            }
            if (type.startsWith("flashdevelop")) {
                Element target = (Element) xml(result).getElementsByTagName("compile").item(0);
                Assert.assertEquals(target.getAttribute("path"), entry);
            } else if (type.startsWith("intellij")) {
                Element configuration = (Element) xml(root.resolve("Main.iml")).getElementsByTagName("configuration").item(0);
                Assert.assertEquals(configuration.getAttribute("main-class"), "Main");
                Assert.assertEquals(configuration.getAttribute("pure-as"), mxml ? "false" : "true");
                xml(root.resolve(".idea/modules.xml"));
            } else {
                String project = text(root.resolve("asconfig.json"));
                Assert.assertTrue(Pattern.compile("\"config\"\\s*:\\s*\"flex\"").matcher(project).find());
                Assert.assertTrue(project.contains(mxml ? "src/Main.mxml" : "\"mainClass\": \"Main\""));
                Assert.assertFalse(project.contains("\"application\""), "Flash Player export must not generate an AIR launch target");
            }
        } finally {
            Configuration.parallelSpeedUp.set(parallel);
            remove(temporary);
        }
    }

    @Test
    public void combinesAllMxmlProjectTypesUsingTheSameMovieWithoutOutputCollisions() throws Exception {
        Path output = Files.createTempDirectory("cli-mxml-projects-");
        try {
            SWF swf = input(true);
            for (String type : new String[]{"flashdevelop_mxml", "intellij_mxml", "vscode_mxml"}) {
                export(swf, output, type, true);
            }
            Assert.assertTrue(Files.isRegularFile(output.resolve("flashdevelop_mxml/Main.as3proj")));
            Assert.assertTrue(Files.isRegularFile(output.resolve("intellij_mxml/Main.iml")));
            Assert.assertTrue(Files.isRegularFile(output.resolve("vscode_mxml/asconfig.json")));
            Assert.assertFalse(Files.exists(output.resolve("src")));
        } finally {
            remove(output);
        }
    }

    @Test(dataProvider = "projectTypes")
    public void rejectsUnsupportedMoviesBeforeCreatingProjectDirectories(String type) throws Exception {
        Path temporary = Files.createTempDirectory("cli-project-invalid-");
        try {
            Path output = temporary.resolve("output");
            try {
                export(new SWF(), output, type, false);
                Assert.fail("Unsupported movie must be rejected");
            } catch (java.io.IOException ex) {
                Assert.assertTrue(ex.getMessage().contains(type), ex.getMessage());
            }
            Assert.assertFalse(Files.exists(output));
        } finally {
            remove(temporary);
        }
    }

    @DataProvider(name = "platforms")
    public Object[][] platforms() {
        return new Object[][]{{"flashdevelop"}, {"vscode"}};
    }

    @Test
    public void rejectsPlainAs3ForMxmlProjectsAndUnknownProjectTypes() throws Exception {
        Path temporary = Files.createTempDirectory("cli-project-format-");
        try {
            SWF swf = input(false);
            for (String type : new String[]{"flashdevelop_mxml", "intellij_mxml", "vscode_mxml"}) {
                Path output = temporary.resolve(type);
                try {
                    export(swf, output, type, false);
                    Assert.fail("Plain AS3 is not a supported MXML document");
                } catch (java.io.IOException ex) {
                    Assert.assertTrue(ex.getMessage().contains(type));
                }
                Assert.assertFalse(Files.exists(output));
            }
            Path output = temporary.resolve("unknown");
            try {
                export(swf, output, "unknown", false);
                Assert.fail("Unknown project types must not fall back to another format");
            } catch (IllegalArgumentException ex) {
                Assert.assertTrue(ex.getMessage().contains("Unknown project export type"));
            }
            Assert.assertFalse(Files.exists(output));
        } finally {
            remove(temporary);
        }
    }

    @Test(dataProvider = "platforms")
    public void supportsAirPlatformForAs3Projects(String type) throws Exception {
        Path output = Files.createTempDirectory("cli-project-air-");
        boolean parallel = Configuration.parallelSpeedUp.get();
        try {
            Configuration.parallelSpeedUp.set(false);
            CommandLineArgumentParser.exportProject(input(false), output.toString(), type, false, Collections.singletonMap(type, "air"),
                    new ConsoleAbortRetryIgnoreHandler(AbortRetryIgnoreHandler.IGNORE, 0), null);
            if ("flashdevelop".equals(type)) {
                xml(output.resolve("application.xml"));
                Assert.assertTrue(Files.isRegularFile(output.resolve("bat/RunApp.bat")));
            } else {
                String project = text(output.resolve("asconfig.json"));
                Assert.assertTrue(Pattern.compile("\"config\"\\s*:\\s*\"air\"").matcher(project).find());
                Assert.assertTrue(project.contains("\"application\""));
                Assert.assertTrue(Files.isRegularFile(output.resolve("src/" + output.getFileName().toString().replace(" ", "").replace("_", "") + "-app.xml")));
            }
        } finally {
            Configuration.parallelSpeedUp.set(parallel);
            remove(output);
        }
    }

    @Test(dataProvider = "platforms")
    public void rejectsUnknownPlatformsBeforeCreatingOutput(String type) throws Exception {
        Path temporary = Files.createTempDirectory("cli-project-platform-");
        try {
            Path output = temporary.resolve("output");
            try {
                CommandLineArgumentParser.exportProject(input(false), output.toString(), type, false, Collections.singletonMap(type, "unknown"), null, null);
                Assert.fail("Unknown platform must be rejected");
            } catch (IllegalArgumentException ex) {
                Assert.assertTrue(ex.getMessage().contains("Use flash or air"));
            }
            Assert.assertFalse(Files.exists(output));
        } finally {
            remove(temporary);
        }
    }

    @Test
    public void documentsAllRegisteredProjectTypesAndAirOptions() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8.name())) {
            CommandLineHelp.printCmdLineUsage(output, "export");
            CommandLineHelp.printCmdLineUsage(output, "format");
        }
        String help = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        for (String type : TYPES) {
            Assert.assertTrue(CommandLineArgumentParser.PROJECT_EXPORT_TYPES.contains(type));
            Assert.assertTrue(Pattern.compile("(?m)^\\s*" + Pattern.quote(type) + "\\s+-").matcher(help).find(), type);
        }
        Assert.assertTrue(help.contains("flashdevelop:air"));
        Assert.assertTrue(help.contains("vscode:air"));
    }
}
