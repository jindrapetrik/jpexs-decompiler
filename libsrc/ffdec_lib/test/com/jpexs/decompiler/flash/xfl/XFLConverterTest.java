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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.ApplicationInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.NameCharacterTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.flash.fla.reader.BinaryFlaDocument;
import com.jpexs.flash.fla.reader.BinaryFlaReader;
import com.jpexs.flash.fla.reader.FlaRecord;
import com.jpexs.helpers.ByteArrayRange;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.testng.Assert.*;

/**
 *
 * @author JPEXS
 */
public class XFLConverterTest {
    @DataProvider(name = "provideFiles")
    public Object[][] provideFiles() {
        return new Object[][]{
            {"testdata/graphics/graphics.swf"},
            {"testdata/sounds/sounds.swf"}
        };
    }
    
    @Test(dataProvider = "provideFiles")
    public void testFlaExport(String fileName) throws Exception {       
        File outputDir = File.createTempFile("ffdec_flatest", "");                
        outputDir.delete();
        outputDir.mkdir();
        
        try(FileInputStream fis = new FileInputStream(fileName)) {
            SWF swf = new SWF(fis, true);

            swf.exportFla(new AbortRetryIgnoreHandler(){
                @Override
                public int handle(Throwable thrown) {
                    return AbortRetryIgnoreHandler.ABORT;
                }
                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }            
            }, 
            outputDir + "/" + ( new File(fileName).getName().replace(".swf", ".fla")),
            new File(fileName).getName(),
            ApplicationInfo.APPLICATION_NAME, ApplicationInfo.applicationVerName, ApplicationInfo.version, true, FLAVersion.CS6,
            null);            
            
            //TODO: Actually do some tests - asserts. Without them it is not much useful - it just tests that no exception is thrown.
            
        } finally {
            removeDirectory(outputDir);
        }
    }
    
    @Test
    public void groupsLibraryItemsAndKeepsExplicitPaths() throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 24;
        swf.frameCount = 1;
        DefineSpriteTag clip = new DefineSpriteTag(swf);
        swf.addTag(clip);
        DefineSpriteTag nestedClip = new DefineSpriteTag(swf);
        swf.addTag(nestedClip);
        name(swf, nestedClip, "Custom/Nested/Clip");
        DefineButton2Tag button = new DefineButton2Tag(swf);
        swf.addTag(button);
        DefineShapeTag graphic = new DefineShapeTag(swf);
        swf.addTag(graphic);
        name(swf, graphic, "Drawing");
        DefineFont2Tag font = new DefineFont2Tag(swf);
        font.fontName = "Arial";
        SHAPE glyph = new SHAPE();
        glyph.shapeRecords = Collections.singletonList(new EndShapeRecord());
        font.glyphShapeTable = Collections.singletonList(glyph);
        font.codeTable = Collections.singletonList(65);
        swf.addTag(font);
        DefineVideoStreamTag video = new DefineVideoStreamTag(swf);
        swf.addTag(video);
        DefineSoundTag sound = new DefineSoundTag(swf);
        sound.soundFormat = 3;
        sound.soundRate = 1;
        sound.soundSampleCount = 1;
        sound.soundData = new ByteArrayRange(new byte[]{0});
        swf.addTag(sound);
        DefineBitsLossless2Tag bitmap = new DefineBitsLossless2Tag(swf);
        swf.addTag(bitmap);

        int depth = 1;
        for (CharacterTag character : Arrays.asList(clip, nestedClip, button, graphic, video, bitmap)) {
            swf.addTag(new PlaceObject2Tag(swf, false, depth++, character.getCharacterId(),
                    new MATRIX(), null, -1, null, -1, null));
        }
        StartSoundTag startSound = new StartSoundTag(swf);
        startSound.soundId = sound.getCharacterId();
        swf.addTag(startSound);
        SoundStreamHead2Tag stream = new SoundStreamHead2Tag(swf);
        stream.streamSoundCompression = 3;
        stream.streamSoundRate = 1;
        stream.streamSoundSampleCount = 1;
        swf.addTag(stream);
        SoundStreamBlockTag block = new SoundStreamBlockTag(swf);
        block.streamSoundData = new ByteArrayRange(new byte[]{0});
        swf.addTag(block);
        swf.addTag(new ShowFrameTag(swf));

        Path directory = Files.createTempDirectory("ffdec-fla-folders-");
        boolean previous = Configuration.useFoldersInFlaExport.get();
        try {
            Configuration.useFoldersInFlaExport.set(false);
            Path flat = directory.resolve("flat.fla");
            export(swf, flat, FLAVersion.CS6, true);
            try (ZipFile zip = new ZipFile(flat.toFile())) {
                assertNotNull(zip.getEntry("LIBRARY/Symbol " + clip.getCharacterId() + ".xml"));
                assertNotNull(zip.getEntry("LIBRARY/Drawing.xml"));
                assertNotNull(zip.getEntry("LIBRARY/Custom/Nested/Clip.xml"));
                assertEquals(attributes(document(zip), "DOMFolderItem", "name"),
                        new HashSet<>(Arrays.asList("Custom", "Custom/Nested")));
            }
            Configuration.useFoldersInFlaExport.set(true);
            Path grouped = directory.resolve("grouped.fla");
            export(swf, grouped, FLAVersion.CS6, true);
            try (ZipFile zip = new ZipFile(grouped.toFile())) {
                Document document = document(zip);
                assertEquals(attributes(document, "DOMFolderItem", "name"), new HashSet<>(Arrays.asList(
                        "Movie Clips", "Buttons", "Graphics", "Fonts", "Videos", "Sounds", "Bitmaps", "Custom", "Custom/Nested")));
                Set<String> symbols = new HashSet<>(Arrays.asList("Movie Clips/Symbol " + clip.getCharacterId(),
                        "Buttons/Symbol " + button.getCharacterId(), "Graphics/Drawing", "Custom/Nested/Clip"));
                assertEquals(attributes(document, "DOMSymbolInstance", "libraryItemName"), symbols);
                for (String symbol : symbols) {
                    assertNotNull(zip.getEntry("LIBRARY/" + symbol + ".xml"));
                }
                String bitmapName = "Bitmaps/Bitmap " + bitmap.getCharacterId() + ".png";
                String soundName = "Sounds/Sound " + sound.getCharacterId() + ".wav";
                String videoName = "Videos/movie" + video.getCharacterId() + ".flv";
                assertEquals(attributes(document, "DOMBitmapInstance", "libraryItemName"), Collections.singleton(bitmapName));
                assertEquals(attributes(document, "DOMVideoInstance", "libraryItemName"), Collections.singleton(videoName));
                assertEquals(attributes(document, "DOMVideoItem", "name"), Collections.singleton(videoName));
                String streamName = "Sounds/" + stream.getRanges().get(0).getFlaExportName() + ".wav";
                Set<String> sounds = new HashSet<>(Arrays.asList(soundName, streamName));
                assertEquals(attributes(document, "DOMSoundItem", "name"), sounds);
                assertEquals(attributes(document, "DOMFrame", "soundName"), sounds);
                assertEquals(attributes(document, "DOMFontItem", "name"), Collections.singleton("Fonts/Font " + font.getCharacterId()));
                assertNotNull(zip.getEntry("LIBRARY/" + bitmapName));
                assertNotNull(zip.getEntry("LIBRARY/" + soundName));
                assertNotNull(zip.getEntry("LIBRARY/" + streamName));
            }
            export(swf, directory.resolve("unpacked.fla"), FLAVersion.CS6, false);
            assertTrue(Files.isRegularFile(directory.resolve("unpacked/LIBRARY/Graphics/Drawing.xml")));
            Path binary = directory.resolve("legacy.fla");
            export(swf, binary, FLAVersion.CS4, true);
            BinaryFlaDocument binaryDocument = new BinaryFlaReader().read(binary.toFile());
            assertTrue(binaryDocument.getIssues().isEmpty(), binaryDocument.getIssues().toString());
            FlaRecord folders = (FlaRecord) binaryDocument.getContents().get("libraryFolders");
            List<?> entries = (List<?>) folders.get("entries");
            assertTrue(entries.stream().anyMatch(item -> "Graphics".equals(((FlaRecord) item).get("name"))));
        } finally {
            Configuration.useFoldersInFlaExport.set(previous);
            removeDirectory(directory.toFile());
        }
    }

    private static void name(SWF swf, CharacterTag character, String path) {
        NameCharacterTag tag = new NameCharacterTag(swf);
        tag.characterId = character.getCharacterId();
        tag.name = path;
        swf.addTag(tag);
    }

    private static void export(SWF swf, Path target, FLAVersion version, boolean compressed) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = false;
        swf.exportXfl(null, target.toString(), "movie.swf", "Test", "Test", "1", false, version, settings, null);
    }

    private static Document document(ZipFile zip) throws Exception {
        try (InputStream stream = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        }
    }

    private static Set<String> attributes(Document document, String tag, String attribute) {
        NodeList nodes = document.getElementsByTagName(tag);
        Set<String> values = new HashSet<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (node.hasAttribute(attribute)) {
                values.add(node.getAttribute(attribute));
            }
        }
        return values;
    }

    private void removeDirectory(File dir){
        try (Stream<Path> dirStream = Files.walk(Paths.get(dir.getAbsolutePath()))) {
            dirStream
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(File::delete);
        } catch (Exception ex) {
            //ignore
        }
    }
}
