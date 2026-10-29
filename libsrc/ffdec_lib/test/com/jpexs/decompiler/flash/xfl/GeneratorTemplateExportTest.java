/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.flash.fla.reader.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GeneratorTemplateExportTest {

    private SWF movie() throws Exception {
        return movie("f4-generator");
    }

    private SWF movie(String name) throws Exception {
        try (InputStream input = Files.newInputStream(Paths.get("testdata/generator/" + name + ".swt"))) {
            return new SWF(input, false);
        }
    }

    private byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return output.toByteArray();
    }

    private Path export(SWF swf, Path directory, FLAVersion version) throws Exception {
        Path file = directory.resolve("template.fla");
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = true;
        swf.exportXfl(null, file.toString(), "template.swt", "Test", "Test", "1", false,
                version, settings, null);
        return file;
    }

    private void cleanup(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private List<FlaRecord> records(BinaryFlaDocument document, String kind) {
        List<FlaRecord> result = new ArrayList<>();
        Set<FlaRecord> seen = Collections.newSetFromMap(new IdentityHashMap<FlaRecord, Boolean>());
        for (FlaStream stream : document.getStreams().values()) {
            collect(stream.getRoot(), kind, result, seen);
        }
        return result;
    }

    private void collect(FlaRecord record, String kind, List<FlaRecord> result, Set<FlaRecord> seen) {
        if (record == null || !seen.add(record)) {
            return;
        }
        if (record.getClassName().equals(kind)) {
            result.add(record);
        }
        for (FlaRecord child : record.getChildren()) {
            collect(child, kind, result, seen);
        }
    }

    @DataProvider(name = "binaryTargets")
    public Object[][] binaryTargets() {
        return new Object[][]{{FLAVersion.F3}, {FLAVersion.F4}, {FLAVersion.F5}, {FLAVersion.MX},
            {FLAVersion.MX2004}, {FLAVersion.F8}, {FLAVersion.CS3}, {FLAVersion.CS4}};
    }

    @Test(dataProvider = "binaryTargets")
    public void flash3TemplateKeepsObjectInstanceAndFrameCommands(FLAVersion version) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-generator-f3-");
        try {
            SWF swf = movie("f3-generator");
            byte[] original = bytes(swf);
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, version).toFile());
            assertTrue(output.getIssues().isEmpty(), output.getIssues().toString());
            BinaryFlaDocument nativeFile = new BinaryFlaReader().read(Paths.get("testdata/generator/f3-generator.fla").toFile());
            assertEquals(output.getLibraryDescriptors().size(), nativeFile.getLibraryDescriptors().size(),
                    "The consumed Generator placeholder must not remain as an extra Library symbol");
            assertFalse(output.getLibraryDescriptors().stream().anyMatch(item -> "Symbol 1".equals(item.get("name"))));
            List<FlaRecord> commands = records(output, "CCommandObj");
            assertEquals(commands.size(), 1, "Only the standalone Chart is a CCommandObj");
            FlaRecord chart = records(nativeFile, "CCommandObj").get(0);
            for (String field : Arrays.asList("command", "displayLabel", "matrixWords", "bounds")) {
                assertEquals(commands.get(0).get(field), chart.get(field), field);
            }
            FlaRecord brightness = records(output, "CPicSymbol").get(0);
            FlaRecord nativeBrightness = records(nativeFile, "CPicSymbol").get(0);
            for (String field : Arrays.asList("legacyName", "firstFrame", "loopMode", "matrixWords")) {
                assertEquals(brightness.get(field), nativeBrightness.get(field), field);
            }
            assertTrue(brightness.get("legacyName").toString().contains("SetBrightnessCommand percent=\"20\""));
            assertTrue(output.getLibraryDescriptors().stream().anyMatch(item -> "MyRect".equals(item.get("name"))
                    && ((Number) item.get("symbolType")).intValue() == 0));
            List<FlaRecord> frames = records(output, "CPicFrame");
            assertEquals(frames.stream().filter(frame -> frame.get("extensionString") != null
                    && frame.get("extensionString").toString().contains("JPEGSetCommand")).count(), 1L);
            String expectedJpeg = records(nativeFile, "CPicFrame").stream()
                    .map(frame -> frame.get("extensionString")).filter(value -> value != null
                    && value.toString().contains("JPEGSetCommand")).findFirst().get().toString();
            assertTrue(frames.stream().anyMatch(frame -> expectedJpeg.equals(frame.get("extensionString"))));
            assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test(dataProvider = "binaryTargets")
    public void nativeTemplateRestoresCommandAndLibraryName(FLAVersion version) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-generator-");
        try {
            SWF swf = movie();
            byte[] original = bytes(swf);
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, version).toFile());
            assertTrue(output.getIssues().isEmpty(), output.getIssues().toString());
            BinaryFlaDocument nativeFile = new BinaryFlaReader().read(Paths.get("testdata/generator/f4-generator.fla").toFile());
            assertEquals(output.getLibraryDescriptors().size(), nativeFile.getLibraryDescriptors().size(),
                    "The consumed Generator placeholder must not remain as an extra Library symbol");
            List<FlaRecord> actual = records(output, "CCommandObj");
            assertEquals(actual.size(), 1);
            FlaRecord expected = records(nativeFile, "CCommandObj").get(0);
            for (String field : Arrays.asList("command", "displayLabel", "matrixWords", "bounds")) {
                assertEquals(actual.get(0).get(field), expected.get(field), field);
            }
            assertTrue(output.getLibraryDescriptors().stream().anyMatch(item -> "MyRect".equals(item.get("name"))));
            assertFalse(output.getLibraryDescriptors().stream().anyMatch(item -> item.get("name").toString().startsWith("__ffdec_generator_")));
            FlaRecord publish = (FlaRecord) output.getContents().get("publishSettings");
            Object settings = publish.get("legacyProperties");
            if (settings == null) {
                settings = ((List<?>) publish.get("profiles")).get(0);
            }
            Map<String, String> properties = new HashMap<>();
            for (Object entry : (List<?>) ((FlaRecord) settings).get("entries")) {
                FlaRecord e = (FlaRecord) entry;
                properties.put((String) e.get("key"), (String) e.get("value"));
            }
            assertEquals(properties.get("Vector::Template"), "1");
            assertEquals(properties.get("Vector::Generator LoadOrder"), "Bottom up");
            assertEquals(properties.get("Vector::Generator Width"), "550");
            assertEquals(bytes(swf), original, "Export must not modify the source template");
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void commandPlaceholderUsedAsAnOrdinaryInstanceStaysInLibrary() throws Exception {
        SWF swf = movie("f3-generator");
        PlaceObject2Tag ordinary = new PlaceObject2Tag(swf);
        ordinary.depth = 5;
        ordinary.placeFlagHasCharacter = true;
        ordinary.characterId = 1;
        ordinary.placeFlagHasName = true;
        ordinary.name = "ordinaryBlank";
        swf.addTag(swf.getTags().size() - 1, ordinary);
        Path directory = Files.createTempDirectory("ffdec-generator-shared-blank-");
        try {
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, FLAVersion.F3).toFile());
            assertEquals(records(output, "CCommandObj").size(), 1);
            FlaRecord instance = records(output, "CPicSprite").stream()
                    .filter(record -> "ordinaryBlank".equals(record.get("instanceName"))).findFirst().get();
            assertTrue(output.getLibraryDescriptors().stream().anyMatch(item -> "Symbol 1".equals(item.get("name"))
                    && item.get("libraryId").equals(instance.get("libraryId"))));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void explicitlyNamedEmptySymbolIsPreserved() throws Exception {
        SWF swf = movie("f3-generator");
        NameCharacterTag name = new NameCharacterTag(swf);
        name.characterId = 1;
        name.name = "Blank asset";
        swf.addTag(name);
        Path directory = Files.createTempDirectory("ffdec-generator-named-blank-");
        try {
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, FLAVersion.F3).toFile());
            assertEquals(records(output, "CCommandObj").size(), 1);
            assertTrue(output.getLibraryDescriptors().stream().anyMatch(item -> "Blank asset".equals(item.get("name"))));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void linkedEmptySymbolIsPreserved() throws Exception {
        SWF swf = movie("f3-generator");
        ExportAssetsTag exports = new ExportAssetsTag(swf);
        exports.tags.add(1);
        exports.names.add("BlankAsset");
        swf.addTag(0, exports);
        Path directory = Files.createTempDirectory("ffdec-generator-linked-blank-");
        try {
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, FLAVersion.F5).toFile());
            assertEquals(records(output, "CCommandObj").size(), 1);
            assertTrue(output.getLibraryDescriptors().stream().anyMatch(item -> "Symbol 1".equals(item.get("name"))));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void movingRemovingAndReusingDepthDoesNotLeakCommands() throws Exception {
        SWF swf = movie();
        PlaceObject2Tag move = new PlaceObject2Tag(swf);
        move.depth = 1;
        move.placeFlagMove = true;
        move.placeFlagHasMatrix = true;
        move.matrix = new MATRIX();
        move.matrix.hasScale = true;
        move.matrix.scaleX = 1.5f;
        move.matrix.scaleY = 0.5f;
        move.matrix.hasRotate = true;
        move.matrix.rotateSkew0 = 0.25f;
        move.matrix.rotateSkew1 = -0.125f;
        move.matrix.translateX = -713;
        move.matrix.translateY = 1317;
        swf.addTag(move);
        swf.addTag(new ShowFrameTag(swf));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        swf.addTag(remove);
        swf.addTag(new ShowFrameTag(swf));
        PlaceObject2Tag ordinary = new PlaceObject2Tag(swf);
        ordinary.depth = 1;
        ordinary.placeFlagHasCharacter = true;
        ordinary.characterId = 3;
        ordinary.placeFlagHasMatrix = true;
        ordinary.matrix = new MATRIX();
        swf.addTag(ordinary);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 4;
        Path directory = Files.createTempDirectory("ffdec-generator-timeline-");
        try {
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, FLAVersion.F4).toFile());
            List<FlaRecord> commands = records(output, "CCommandObj");
            assertEquals(commands.size(), 2);
            assertEquals(commands.get(1).get("matrixWords"), Arrays.asList(98304, 16384, -8192, 32768, -713, 1317));
            assertEquals(commands.get(0).get("command"), commands.get(1).get("command"));
            assertEquals(records(output, "CPicSprite").size(), 2, "Ordinary instances must survive depth reuse");
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void commandsInsideLibraryTimelineKeepParametersAndEscapes() throws Exception {
        SWF swf = movie();
        GenCommandTag command = new GenCommandTag(swf);
        command.typeMovie = true;
        command.depth = 1;
        command.command = "example.CustomCommand";
        command.parameterNames.add("text");
        command.commandValues.add("Quotes \" & <xml> \\ path\nnext line");
        DefineSpriteTag symbol = (DefineSpriteTag) swf.getCharacter(3);
        symbol.addTag(1, command);
        Path directory = Files.createTempDirectory("ffdec-generator-nested-");
        try {
            byte[] original = bytes(swf);
            BinaryFlaDocument output = new BinaryFlaReader().read(export(swf, directory, FLAVersion.F5).toFile());
            List<FlaRecord> commands = records(output, "CCommandObj");
            assertEquals(commands.size(), 1, "An attached command must keep its original symbol");
            assertTrue(records(output, "CPicSymbol").stream().anyMatch(record -> command.getCommandString().equals(record.get("legacyName"))));
            assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void xflUsesNamedFoldersAndKeepsNamedShapesInLibrary() throws Exception {
        SWF swf = movie();
        NameCharacterTag shapeName = new NameCharacterTag(swf);
        shapeName.characterId = 2;
        shapeName.name = "Assets/Red shape";
        swf.addTag(shapeName);
        Path directory = Files.createTempDirectory("ffdec-generator-names-");
        try (ZipFile zip = new ZipFile(export(swf, directory, FLAVersion.CS6).toFile())) {
            assertNotNull(zip.getEntry("LIBRARY/MyRect.xml"));
            assertNotNull(zip.getEntry("LIBRARY/Assets/Red shape.xml"));
            String document;
            try (InputStream input = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = input.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                document = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
            assertTrue(document.contains("name=\"Assets\""), document);
            assertFalse(document.contains("__ffdec_generator_"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void unsafeAndDuplicateNamesCannotOverwriteLibraryFiles() throws Exception {
        SWF swf = movie();
        NameCharacterTag first = new NameCharacterTag(swf);
        first.characterId = 1;
        first.name = "../Folder/CON";
        swf.addTag(first);
        NameCharacterTag second = new NameCharacterTag(swf);
        second.characterId = 2;
        second.name = "../Folder/CON";
        swf.addTag(second);
        Map<com.jpexs.decompiler.flash.tags.base.CharacterTag, String> names = new IdentityHashMap<>();
        Set<com.jpexs.decompiler.flash.tags.base.CharacterTag> characters = new LinkedHashSet<>(swf.getCharacters(true).values());
        Set<String> folders = CharacterLibraryNames.initialize(swf.getTags(), characters, names, new ArrayList<>());
        assertEquals(names.get(swf.getCharacter(1)), "_/Folder/_CON");
        assertEquals(names.get(swf.getCharacter(2)), "_/Folder/_CON 2");
        assertEquals(names.get(swf.getCharacter(3)), "MyRect");
        assertTrue(folders.contains("_/Folder"));
    }

    @Test
    public void emptyNamesStillReserveTheirGeneratedFallback() throws Exception {
        SWF swf = movie();
        NameCharacterTag empty = new NameCharacterTag(swf);
        empty.characterId = 1;
        empty.name = "/";
        swf.addTag(empty);
        NameCharacterTag collision = new NameCharacterTag(swf);
        collision.characterId = 2;
        collision.name = "Symbol 1";
        swf.addTag(collision);
        Map<com.jpexs.decompiler.flash.tags.base.CharacterTag, String> names = new IdentityHashMap<>();
        CharacterLibraryNames.initialize(swf.getTags(), new LinkedHashSet<>(swf.getCharacters(true).values()), names, new ArrayList<>());
        assertFalse(names.containsKey(swf.getCharacter(1)));
        assertEquals(names.get(swf.getCharacter(2)), "Symbol 1 2");
    }
}
