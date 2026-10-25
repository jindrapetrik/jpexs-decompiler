/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TweenQualityCommandLineTest {

    public static class Runner {
        public static void main(String[] args) throws IOException {
            CommandLineArgumentParser.parseArguments(args);
        }
    }

    private static final class Result {
        int exit;
        String text;
    }

    private Result run(Path directory, String... arguments) throws Exception {
        Path java = Paths.get(System.getProperty("java.home"), "bin", "java");
        if (!Files.exists(java)) {
            java = java.resolveSibling("java.exe");
        }
        List<String> command = new ArrayList<>(Arrays.asList(java.toString(), "-Xmx512m",
                "-Djava.awt.headless=true", "-Duser.home=" + directory, "-cp",
                System.getProperty("java.class.path"), Runner.class.getName()));
        command.addAll(Arrays.asList(arguments));
        Path log = Files.createTempFile(directory, "cli-", ".log");
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("APPDATA", directory.toString());
        builder.environment().put("XDG_CONFIG_HOME", directory.toString());
        Process child = builder.start();
        try {
            child.getOutputStream().close();
            Assert.assertTrue(child.waitFor(45, TimeUnit.SECONDS), "CLI export timed out");
            Result result = new Result();
            result.exit = child.exitValue();
            result.text = new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
            return result;
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly().waitFor();
            }
        }
    }

    private void remove(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    @DataProvider(name = "exports")
    public Object[][] exports() {
        return new Object[][]{{"fla", null}, {"xfl", null}, {"fla", "0"}, {"xfl", "0"},
            {"fla", "50"}, {"xfl", "50"}, {"fla", "100"}, {"xfl", "100"}};
    }

    @Test(dataProvider = "exports")
    public void controlsCompressedAndDirectoryExportsIndependentlyOfGuiPreference(String format, String quality) throws Exception {
        Path directory = Files.createTempDirectory("tween-quality-cli-");
        try {
            List<String> arguments = new ArrayList<>(Arrays.asList("-config",
                    "lastFlaExportTweenDetectionQuality=100"));
            if (quality != null) {
                arguments.add("-tweenquality");
                arguments.add(quality);
            }
            arguments.addAll(Arrays.asList("-format", format + ":cs6", "-export", format,
                    directory.resolve("output").toString(),
                    Paths.get("libsrc/ffdec_lib/testdata/xfl/graphics-tweens.swf").toAbsolutePath().toString()));
            Result result = run(directory, arguments.toArray(new String[0]));
            Assert.assertEquals(result.exit, 0, result.text);
            Document document;
            if (format.equals("fla")) {
                try (ZipFile zip = new ZipFile(directory.resolve("output/graphics-tweens.fla").toFile())) {
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(zip.getInputStream(zip.getEntry("DOMDocument.xml")));
                }
            } else {
                document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(directory.resolve("output/graphics-tweens/DOMDocument.xml").toFile());
            }
            int motion = 0;
            int shape = 0;
            NodeList frames = document.getElementsByTagName("DOMFrame");
            for (int i = 0; i < frames.getLength(); i++) {
                String type = ((Element) frames.item(i)).getAttribute("tweenType");
                if (type.equals("shape")) {
                    shape++;
                } else if (!type.isEmpty()) {
                    motion++;
                }
            }
            Assert.assertEquals(motion, quality == null || quality.equals("0") ? 0 : 1, result.text);
            Assert.assertEquals(shape, 7);
        } finally {
            remove(directory);
        }
    }

    @DataProvider(name = "invalid")
    public Object[][] invalid() {
        return new Object[][]{{"-1"}, {"101"}, {"1.5"}, {"not-a-number"}, {null}};
    }

    @Test(dataProvider = "invalid")
    public void rejectsInvalidOrMissingQuality(String quality) throws Exception {
        Path directory = Files.createTempDirectory("tween-quality-invalid-");
        try {
            Result result = quality == null ? run(directory, "-tweenquality") : run(directory, "-tweenquality", quality);
            Assert.assertEquals(result.exit, 1, result.text);
            Assert.assertTrue(result.text.contains("-tweenQuality"), result.text);
            Assert.assertTrue(result.text.contains("0-100"), result.text);
        } finally {
            remove(directory);
        }
    }
}
