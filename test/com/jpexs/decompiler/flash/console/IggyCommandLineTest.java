/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.console;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.profiles.IggySwfProfile;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Exercise the real argument dispatcher in child JVMs because CLI commands exit. */
public class IggyCommandLineTest {

    public static class Runner {
        public static void main(String[] args) throws IOException {
            CommandLineArgumentParser.parseArguments(args);
        }
    }

    private static final class Result {
        final int exit;
        final String text;

        Result(int exit, String text) {
            this.exit = exit;
            this.text = text;
        }
    }

    private static Result run(Path directory, byte[] input, String... arguments) throws Exception {
        Path java = Paths.get(System.getProperty("java.home"), "bin", "java");
        if (!Files.exists(java)) {
            java = java.resolveSibling("java.exe");
        }
        List<String> command = new ArrayList<>(Arrays.asList(java.toString(), "-Djava.awt.headless=true",
                "-Duser.home=" + directory, "-cp", System.getProperty("java.class.path"), Runner.class.getName()));
        command.addAll(Arrays.asList(arguments));
        Path log = Files.createTempFile(directory, "command-", ".log");
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("APPDATA", directory.toString());
        builder.environment().put("XDG_CONFIG_HOME", directory.toString());
        Process child = builder.start();
        try {
            try (OutputStream stdin = child.getOutputStream()) {
                if (input != null) {
                    stdin.write(input);
                }
            }
            Assert.assertTrue(child.waitFor(45, TimeUnit.SECONDS), "CLI process timed out: " + command);
            return new Result(child.exitValue(), new String(Files.readAllBytes(log), StandardCharsets.UTF_8));
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly().waitFor();
            }
        }
    }

    private static void remove(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private static byte[] swf(float fps, boolean action, boolean oldCharset) throws Exception {
        SWF swf = new SWF("windows-1250");
        swf.version = oldCharset ? 5 : 10;
        swf.frameCount = 1;
        swf.frameRate = fps;
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeId = 10;
        swf.addTag(shape);
        PlaceObject2Tag place = new PlaceObject2Tag(swf);
        place.placeFlagHasCharacter = true;
        place.characterId = 10;
        place.depth = 1;
        swf.addTag(place);
        FrameLabelTag label = new FrameLabelTag(swf);
        label.name = "Příliš";
        swf.addTag(label);
        if (action) {
            swf.addTag(new DoActionTag(swf));
        }
        swf.addTag(new ShowFrameTag(swf));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return bytes.toByteArray();
    }

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{"32_legacy", 64}, {"32", 128}, {"64", 184}};
    }

    @Test(dataProvider = "variants")
    public void convertsBothDirections(String variant, int headerSize) throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-");
        try {
            Path input = directory.resolve("movie source.swf");
            Path nativeFile = directory.resolve("movie native.iggy");
            Path output = directory.resolve("movie result.swf");
            Files.write(input, swf(24, false, false));
            Result encode = run(directory, null, "-swf2iggy", input.toString(), nativeFile.toString(), variant);
            Assert.assertEquals(encode.exit, 0, encode.text);
            Assert.assertTrue(encode.text.contains("OK"));
            IggyFile file = new IggyFile(nativeFile.toFile());
            Assert.assertEquals(file.getSwfName(), "movie source.swf");
            Assert.assertEquals(file.getSwf().getHdr().getHeaderSize(), headerSize);
            Assert.assertEquals(file.getSwf().getDictionary().get(10).getType(), 1);
            Result decode = run(directory, null, "-iggy2swf", nativeFile.toString(), output.toString());
            Assert.assertEquals(decode.exit, 0, decode.text);
            SWF actual = new SWF(new ByteArrayInputStream(Files.readAllBytes(output)), false, false, "UTF-8", new IggySwfProfile());
            Assert.assertEquals(actual.frameCount, 1);
            Assert.assertEquals(actual.frameRate, 24f);
            Assert.assertNotNull(actual.getCharacter(10));
            boolean placed = false;
            for (Tag tag : actual.getTags()) {
                if (tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getCharacterId() == 10) {
                    placed = true;
                }
            }
            Assert.assertTrue(placed);
        } finally {
            remove(directory);
        }
    }

    @Test
    public void defaultsTo64AndUsesPreOptions() throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-options-");
        try {
            Path input = directory.resolve("input.swf");
            Path output = directory.resolve("output.iggy");
            Files.write(input, swf(30, false, true));
            Result result = run(directory, null, "-charset", "windows-1250", "-swfProfile", "standard",
                    "-swf2iggy", input.toString(), output.toString());
            Assert.assertEquals(result.exit, 0, result.text);
            IggyFile file = new IggyFile(output.toFile());
            Assert.assertTrue(file.getHeader().is64());
            Assert.assertEquals(file.getSwfFrameRate(), 30f);
            Assert.assertEquals(file.getSwf().getFrameLabels().get(0).getName(), "Příliš");
        } finally {
            remove(directory);
        }
    }

    @Test
    public void supportsStandardInputInBothDirections() throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-stdin-");
        try {
            Path nativeFile = directory.resolve("stdin movie.iggy");
            Path output = directory.resolve("result.swf");
            Result encode = run(directory, swf(24, false, false), "-swf2iggy", "/dev/stdin", nativeFile.toString(), "32");
            Assert.assertEquals(encode.exit, 0, encode.text);
            Assert.assertEquals(new IggyFile(nativeFile.toFile()).getSwfName(), "stdin movie.swf");
            Result decode = run(directory, Files.readAllBytes(nativeFile), "-iggy2swf", "/dev/stdin", output.toString());
            Assert.assertEquals(decode.exit, 0, decode.text);
            Assert.assertNotNull(new SWF(new ByteArrayInputStream(Files.readAllBytes(output)), false).getCharacter(10));
        } finally {
            remove(directory);
        }
    }

    @DataProvider(name = "badArguments")
    public Object[][] badArguments() {
        return new Object[][]{
            {new String[]{"-swf2iggy"}},
            {new String[]{"-swf2iggy", "input"}},
            {new String[]{"-swf2iggy", "input", "output", "bad-layout"}},
            {new String[]{"-swf2iggy", "input", "output", "64", "extra"}},
            {new String[]{"-iggy2swf"}},
            {new String[]{"-iggy2swf", "input"}},
            {new String[]{"-iggy2swf", "input", "output", "extra"}}
        };
    }

    @Test(dataProvider = "badArguments")
    public void rejectsBadArguments(String[] arguments) throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-arguments-");
        try {
            Result result = run(directory, null, arguments);
            Assert.assertEquals(result.exit, 1, result.text);
            Assert.assertTrue(result.text.contains("Bad Commandline Arguments"), result.text);
            Assert.assertTrue(result.text.contains(arguments[0]), result.text);
        } finally {
            remove(directory);
        }
    }

    @Test
    public void preservesOutputOnConversionFailure() throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-failure-");
        try {
            Path input = directory.resolve("input.swf");
            Path output = directory.resolve("output.iggy");
            byte[] before = {1, 2, 3, 4};
            Files.write(output, before);
            Files.write(input, swf(24, true, false));
            Result action = run(directory, null, "-swf2iggy", input.toString(), output.toString());
            Assert.assertEquals(action.exit, 0, action.text);
            Assert.assertTrue(action.text.contains("Warning:"), action.text);
            Assert.assertTrue(action.text.contains("DoAction"), action.text);
            Assert.assertNotNull(new IggyFile(output.toFile()).getSwf());
            Files.write(output, before);
            Files.write(input, swf(30, false, false));
            Result fps = run(directory, null, "-swf2iggy", input.toString(), output.toString(), "32_legacy");
            Assert.assertEquals(fps.exit, 1, fps.text);
            Assert.assertTrue(fps.text.contains("24 FPS"), fps.text);
            Assert.assertEquals(Files.readAllBytes(output), before);
            Files.write(input, new byte[]{1, 2, 3});
            Result invalid = run(directory, null, "-iggy2swf", input.toString(), output.toString());
            Assert.assertEquals(invalid.exit, 1, invalid.text);
            Assert.assertEquals(Files.readAllBytes(output), before);
            Result missing = run(directory, null, "-iggy2swf", directory.resolve("missing.iggy").toString(), output.toString());
            Assert.assertEquals(missing.exit, 1, missing.text);
            Assert.assertEquals(Files.readAllBytes(output), before);
        } finally {
            remove(directory);
        }
    }

    @Test
    public void exposesHelpAndExamples() throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-help-");
        try {
            for (String command : new String[]{"swf2iggy", "iggy2swf"}) {
                Result result = run(directory, null, "-help", command);
                Assert.assertEquals(result.exit, 0, result.text);
                Assert.assertTrue(result.text.contains("ffdec -" + command), result.text);
                Assert.assertFalse(result.text.contains("no example found"), result.text);
                if (command.equals("swf2iggy")) {
                    Assert.assertTrue(result.text.contains("32_legacy"), result.text);
                }
            }
            Result main = run(directory, null, "-help");
            Assert.assertEquals(main.exit, 0, main.text);
            Assert.assertTrue(main.text.contains("-swf2iggy"), main.text);
            Assert.assertTrue(main.text.contains("-iggy2swf"), main.text);
        } finally {
            remove(directory);
        }
    }

    @Test(dataProvider = "variants")
    public void rejectsAlchemyWithoutCreatingOrOverwritingOutput(String variant, int headerSize) throws Exception {
        Path directory = Files.createTempDirectory("iggy-cli-alchemy-");
        try {
            SWF source = new SWF(new ByteArrayInputStream(swf(24, true, false)), false);
            DoABC2Tag tag = new DoABC2Tag(source);
            MethodBody body = new MethodBody();
            body.method_info = 0;
            body.max_regs = 1;
            body.max_stack = 1;
            body.setCodeBytes(new byte[]{0x24, 0, 0x35, 0x29, 0x47});
            tag.getABC().method_info.add(new MethodInfo());
            tag.getABC().bodies.add(body);
            source.addTag(tag);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            source.saveTo(bytes);
            Path input = directory.resolve("source.swf");
            Path output = directory.resolve("output.iggy");
            Files.write(input, bytes.toByteArray());
            Result first = run(directory, null, "-swf2iggy", input.toString(), output.toString(), variant);
            Assert.assertEquals(first.exit, 1, first.text);
            Assert.assertTrue(first.text.contains("Alchemy instruction li8"), first.text);
            Assert.assertFalse(Files.exists(output), "Fatal ABC error must not create output");
            byte[] original = {1, 2, 3, 4};
            Files.write(output, original);
            Result second = run(directory, null, "-swf2iggy", input.toString(), output.toString(), variant);
            Assert.assertEquals(second.exit, 1, second.text);
            Assert.assertTrue(second.text.contains("Alchemy instruction li8"), second.text);
            Assert.assertEquals(Files.readAllBytes(output), original);
            Assert.assertEquals(Files.readAllBytes(input), bytes.toByteArray());
        } finally {
            remove(directory);
        }
    }
}
