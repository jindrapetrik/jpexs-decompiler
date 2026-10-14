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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.profiles.IggySwfProfile;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineFontInfoTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyCreationTest {

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    @DataProvider(name = "modernVariants")
    public Object[][] modernVariants() {
        return new Object[][]{{IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    private static SWF empty() {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameCount = 1;
        swf.frameRate = 24;
        swf.displayRect = new RECT(-200, 12800, -400, 9600);
        return swf;
    }

    private static SWF reparse(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return new SWF(new ByteArrayInputStream(out.toByteArray()), false, false, "UTF-8", new IggySwfProfile());
    }

    private static byte[] bytes(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return out.getAllBytes();
    }

    private static IggyFile reload(IggyFile file) throws Exception {
        return new IggyFile(new MemoryDataStream(bytes(file)));
    }

    private static <T> T find(SWF swf, Class<T> type) {
        for (Tag tag : swf.getTags()) {
            if (type.isInstance(tag)) {
                return type.cast(tag);
            }
        }
        throw new AssertionError("Missing " + type);
    }

    private static Tag character(SWF swf, int id) {
        for (Tag tag : swf.getTags()) {
            if (tag instanceof CharacterTag && ((CharacterTag) tag).getCharacterId() == id) {
                return tag;
            }
        }
        throw new AssertionError("Missing character " + id);
    }

    private static PlaceObject2Tag place(SWF swf, int id, int depth) {
        PlaceObject2Tag tag = new PlaceObject2Tag(swf);
        tag.placeFlagHasCharacter = true;
        tag.characterId = id;
        tag.depth = depth;
        tag.placeFlagHasMatrix = true;
        tag.matrix = new MATRIX();
        tag.matrix.translateX = 120;
        tag.matrix.translateY = -60;
        return tag;
    }

    private static SWF movie() throws Exception {
        SWF swf = empty();
        swf.frameCount = 2;
        swf.addTag(new SetBackgroundColorTag(swf, new RGB(12, 34, 56)));
        DefineFont2Tag font = new DefineFont2Tag(swf);
        font.fontID = 1;
        font.fontName = "New font";
        font.fontFlagsHasLayout = false;
        SHAPE glyph = new SHAPE();
        glyph.shapeRecords = Arrays.asList(new EndShapeRecord());
        font.glyphShapeTable = Arrays.asList(glyph);
        font.codeTable = Arrays.asList(65);
        swf.addTag(font);
        DefineEditTextTag text = new DefineEditTextTag(swf);
        text.characterID = 2;
        text.bounds = new RECT(0, 1000, 0, 400);
        text.hasText = true;
        text.initialText = "Fresh text Ă„Ĺ¤Äąâ„˘ÄąÄľ";
        text.hasFont = true;
        text.fontId = 1;
        text.fontHeight = 240;
        swf.addTag(text);
        DefineBitsJPEG2Tag image = new DefineBitsJPEG2Tag(swf);
        image.characterID = 3;
        BufferedImage pixels = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        pixels.setRGB(0, 0, 0xff123456);
        pixels.setRGB(1, 0, 0x8077aaee);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(pixels, "png", png);
        image.setImage(png.toByteArray());
        swf.addTag(image);
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeId = 4;
        shape.shapeBounds = new RECT(0, 200, 0, 200);
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = 0x40;
        fill.bitmapId = 3;
        fill.bitmapMatrix = new MATRIX();
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill};
        shape.shapes.shapeRecords = new ArrayList<>();
        StyleChangeRecord style = new StyleChangeRecord();
        style.stateMoveTo = true;
        style.stateFillStyle1 = true;
        style.fillStyle1 = 1;
        shape.shapes.shapeRecords.add(style);
        for (int[] delta : new int[][]{{200, 0}, {0, 200}, {-200, 0}, {0, -200}}) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = delta[0];
            edge.deltaY = delta[1];
            edge.calculateBits();
            shape.shapes.shapeRecords.add(edge);
        }
        shape.shapes.numFillBits = 1;
        shape.shapes.shapeRecords.add(new EndShapeRecord());
        swf.addTag(shape);
        DefineButton2Tag button = new DefineButton2Tag(swf);
        button.buttonId = 5;
        BUTTONRECORD record = new BUTTONRECORD();
        record.characterId = 4;
        record.placeDepth = 1;
        record.placeMatrix = new MATRIX();
        record.colorTransform = new CXFORMWITHALPHA();
        record.buttonStateUp = true;
        record.buttonStateHitTest = true;
        button.characters.add(record);
        swf.addTag(button);
        DefineBinaryDataTag binary = new DefineBinaryDataTag(swf);
        binary.tag = 6;
        binary.binaryData = new ByteArrayRange(new byte[]{7, 0, (byte) 255, 42});
        swf.addTag(binary);
        DefineSoundTag sound = new DefineSoundTag(swf);
        sound.soundId = 7;
        sound.soundFormat = 3;
        sound.soundRate = 3;
        sound.soundSize = true;
        sound.soundSampleCount = 2;
        sound.soundData = new ByteArrayRange(new byte[]{0, 0, 1, 0});
        swf.addTag(sound);
        DefineText2Tag staticText = new DefineText2Tag(swf);
        staticText.characterID = 8;
        staticText.textBounds = new RECT(0, 500, 0, 300);
        TEXTRECORD run = new TEXTRECORD();
        run.styleFlagsHasFont = true;
        run.fontId = 1;
        run.textHeight = 240;
        run.styleFlagsHasColor = true;
        run.textColorA = new RGBA(12, 34, 56, 78);
        GLYPHENTRY entry = new GLYPHENTRY();
        entry.glyphIndex = 0;
        entry.glyphAdvance = 100;
        run.glyphEntries = Arrays.asList(entry);
        staticText.textRecords.add(run);
        swf.addTag(staticText);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.spriteId = 9;
        sprite.frameCount = 2;
        sprite.addTag(place(swf, 2, 1));
        sprite.addTag(place(swf, 5, 2));
        sprite.addTag(new ShowFrameTag(swf));
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);
        DefineMorphShape2Tag morph = new DefineMorphShape2Tag(swf);
        morph.characterId = 10;
        swf.addTag(morph);
        DefineScalingGridTag grid = new DefineScalingGridTag(swf);
        grid.characterId = 9;
        grid.splitter = new RECT(20, 200, 40, 400);
        swf.addTag(grid);
        swf.addTag(place(swf, 9, 1));
        swf.addTag(place(swf, 8, 2));
        StartSoundTag start = new StartSoundTag(swf);
        start.soundId = 7;
        start.soundInfo = new SOUNDINFO();
        swf.addTag(start);
        FrameLabelTag label = new FrameLabelTag(swf);
        label.name = "first";
        swf.addTag(label);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new EndTag(swf));
        return reparse(swf);
    }

    @Test(dataProvider = "variants")
    public void createsEmptyMovie(IggyFile.Variant variant) throws Exception {
        SWF source = empty();
        source.addTag(new ShowFrameTag(source));
        IggyFile file = reload(SwfToIggyConvertor.createIggy(reparse(source), variant, "NovÄ‚Ëť.swf"));
        Assert.assertNull(file.getOriginalFile());
        Assert.assertEquals(file.getSwfName(), "NovÄ‚Ëť.swf");
        Assert.assertEquals(file.getHeader().is64(), variant == IggyFile.Variant.X64);
        Assert.assertEquals(file.getSwf().getHdr().getHeaderSize(), variant == IggyFile.Variant.X64 ? 184 : variant == IggyFile.Variant.X86_LEGACY ? 64 : 128);
        Assert.assertTrue(file.getSwf().getDictionary().isEmpty());
        Assert.assertEquals(file.getSwf().getTimeline().getCommands().size(), 1);
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        Assert.assertEquals(actual.frameCount, 1);
        Assert.assertEquals(actual.frameRate, 24f);
        Assert.assertEquals(actual.displayRect.Xmin, -200);
        Assert.assertEquals(actual.displayRect.Ymax, 9600);
    }

    @Test(dataProvider = "variants")
    public void createsAllDefinitionsAndTimelines(IggyFile.Variant variant) throws Exception {
        IggyFile file = SwfToIggyConvertor.createIggy(movie(), variant, "Fresh.swf");
        // The returned object must already expose its generated fonts and sprites.
        Assert.assertEquals(file.getSwf().getFonts().size(), 1);
        Assert.assertEquals(file.getSwf().getTexts().size(), 1);
        Assert.assertEquals(file.getSwf().getSpriteIds(), new HashSet<>(Arrays.asList(9)));
        file = reload(file);
        int[] kinds = {-1, 22, 6, 4, 1, 7, 20, 21, 5, 3, 2};
        for (int i = 0; i < kinds.length; i++) {
            Assert.assertEquals(file.getSwf().getDictionary().get(i).getType(), kinds[i], "Dictionary ID " + i);
        }
        Assert.assertEquals(file.getSwf().getScalingGrid(9), new float[]{1, 2, 10, 20});
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        Assert.assertEquals(((DefineEditTextTag) character(actual, 2)).initialText, "Fresh text Ă„Ĺ¤Äąâ„˘ÄąÄľ");
        Assert.assertEquals(((DefineFont2Tag) character(actual, 1)).codeTable, Arrays.asList(65));
        Assert.assertEquals(((com.jpexs.decompiler.flash.tags.base.ImageTag) character(actual, 3)).getImageCached().getBufferedImage().getRGB(0, 0), 0xff123456);
        Assert.assertEquals(((com.jpexs.decompiler.flash.tags.base.ShapeTag) character(actual, 4)).getShapes().fillStyles.fillStyles[0].bitmapId, 3);
        Assert.assertEquals(((DefineButton2Tag) character(actual, 5)).characters.get(0).characterId, 4);
        Assert.assertEquals(((DefineBinaryDataTag) character(actual, 6)).binaryData.getRangeData(), new byte[]{7, 0, (byte) 255, 42});
        Assert.assertEquals(((DefineSoundTag) character(actual, 7)).soundData.getRangeData(), new byte[]{0, 0, 1, 0});
        Assert.assertEquals(((DefineText2Tag) character(actual, 8)).textRecords.get(0).fontId, 1);
        DefineSpriteTag sprite = (DefineSpriteTag) character(actual, 9);
        Assert.assertEquals(sprite.frameCount, 2);
        Assert.assertEquals(((PlaceObjectTypeTag) sprite.getTags().get(0)).getCharacterId(), 2);
        Assert.assertEquals(find(actual, StartSoundTag.class).soundId, 7);
        Assert.assertEquals(find(actual, SetBackgroundColorTag.class).backgroundColor.red, 12);
        Assert.assertEquals(file.getSwf().getFrameLabels().get(0).getName(), "first");
        byte[] before = bytes(file);
        SwfToIggyConvertor.updateIggy(file.getSwf(), actual);
        Assert.assertEquals(bytes(file), before, "A generated movie must support a stable unchanged update");
        ((DefineEditTextTag) character(actual, 2)).initialText = "Edited after creation";
        SwfToIggyConvertor.updateIggy(file.getSwf(), actual);
        Assert.assertEquals(reload(file).getSwf().getTexts().get(0).getInitialText(), "Edited after creation");
    }

    @Test(dataProvider = "modernVariants")
    public void createsAs3ImportsAndSymbols(IggyFile.Variant variant) throws Exception {
        SWF swf = empty();
        swf.frameRate = 29.5f;
        FileAttributesTag attributes = new FileAttributesTag(swf);
        attributes.actionScript3 = true;
        swf.addTag(attributes);
        ImportAssets2Tag imports = new ImportAssets2Tag(swf);
        imports.url = "library.iggy";
        imports.tags.add(500);
        imports.names.add("ImportedAsset");
        swf.addTag(imports);
        swf.addTag(new DoABC2Tag(swf));
        DefineSceneAndFrameLabelDataTag scenes = new DefineSceneAndFrameLabelDataTag(swf);
        scenes.sceneOffsets = new long[]{0};
        scenes.sceneNames = new String[]{"Opening"};
        swf.addTag(scenes);
        SymbolClassTag symbols = new SymbolClassTag(swf);
        symbols.tags.add(0);
        symbols.names.add("DocumentClass");
        symbols.tags.add(500);
        symbols.names.add("ImportedAsset");
        swf.addTag(symbols);
        PlaceObject3Tag place = new PlaceObject3Tag(swf);
        place.depth = 1;
        place.placeFlagHasClassName = true;
        place.className = "ImportedAsset";
        swf.addTag(place);
        swf.addTag(new ShowFrameTag(swf));
        IggyFile file = reload(SwfToIggyConvertor.createIggy(reparse(swf), variant, "Imports.swf"));
        Assert.assertTrue(file.getSwf().hasAS3());
        Assert.assertEquals(file.getSwf().getScenes().get(0).getName(), "Opening");
        Assert.assertEquals(file.getSwfFrameRate(), 29.5f);
        Assert.assertEquals(file.getSwf().getAbcBlocks().size(), 1);
        Assert.assertEquals(file.getSwf().getDictionary().size(), 501);
        Assert.assertTrue(file.getSwf().getDictionary().get(500).isNull());
        Assert.assertEquals(file.getSwf().getImports().get(0).getLibrary(), "library.iggy");
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        Assert.assertEquals(find(actual, SymbolClassTag.class).tags, Arrays.asList(0, 500));
        Assert.assertEquals(find(actual, PlaceObject3Tag.class).getClassName(), "ImportedAsset");
        Assert.assertEquals(find(actual, PlaceObject3Tag.class).getCharacterId(), -1);
        long field = file.getSwf().getTimeline().getCommands().get(0).getAddress() + (variant == IggyFile.Variant.X64 ? 48 : 40);
        Assert.assertEquals(file.getSwf().getDictionaryId(field, 0), 500);
    }

    @Test(dataProvider = "variants")
    public void emitsCompleteNativeFixups(IggyFile.Variant variant) throws Exception {
        IggyFile file = SwfToIggyConvertor.createIggy(movie(), variant, "Fresh.swf");
        byte[] main = file.getEntryData(0);
        List<IggyFixup.Field> fields = IggyFixup.parse(variant == IggyFile.Variant.X64, file.getEntryData(1), main.length).getFields();
        Set<Long> pointers = new HashSet<>();
        Set<Long> libraries = new HashSet<>();
        for (IggyFixup.Field field : fields) {
            if (field.operation == 2) pointers.add(field.offset);
            if (field.operation == 12) libraries.add(field.offset);
        }
        Assert.assertTrue(pointers.contains(0L));
        Assert.assertTrue(pointers.contains(variant == IggyFile.Variant.X64 ? 8L : variant == IggyFile.Variant.X86_LEGACY ? 60L : 4L));
        for (IggySwf.DictionaryEntry entry : file.getSwf().getDictionary()) {
            if (!entry.isNull()) {
                Assert.assertTrue(libraries.contains(entry.getAddress() + 1), "Library fixup for ID " + entry.getId());
                Assert.assertTrue(pointers.contains(entry.getAddress() + (variant == IggyFile.Variant.X64 ? 16 : 8)), "Class pointer fixup for ID " + entry.getId());
            }
        }
    }

    @Test
    public void rejectsLegacyLimitationsAndMissingReferences() throws Exception {
        SWF swf = empty();
        swf.frameRate = 30;
        try { SwfToIggyConvertor.createIggy(swf, IggyFile.Variant.X86_LEGACY, "Bad.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("24 FPS")); }
        swf.frameRate = 24;
        swf.addTag(new DoABC2Tag(swf));
        try { SwfToIggyConvertor.createIggy(swf, IggyFile.Variant.X86_LEGACY, "Bad.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("AS3")); }
        SWF missing = empty();
        missing.addTag(place(missing, 42, 1));
        try { SwfToIggyConvertor.createIggy(missing, "Bad.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("Missing character")); }
    }

    @Test
    public void rejectsUnsupportedTagsAndPreservesDestinationOnFailure() throws Exception {
        Path destination = Files.createTempFile("iggy-create-reject-", ".iggy");
        try {
            byte[] original = {1, 2, 3, 4};
            Files.write(destination, original);
            SWF swf = empty();
            swf.addTag(new DoActionTag(swf));
            try { SwfToIggyConvertor.exportIggyToFile(swf, destination.toFile(), IggyFile.Variant.X64, "Bad.swf"); Assert.fail(); }
            catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("DoAction")); }
            Assert.assertEquals(Files.readAllBytes(destination), original);
        } finally { Files.delete(destination); }
    }

    @Test(dataProvider = "variants")
    public void savesNewContainerToFile(IggyFile.Variant variant) throws Exception {
        Path destination = Files.createTempFile("iggy-created-", ".iggy");
        try {
            Files.write(destination, new byte[65536]);
            SwfToIggyConvertor.exportIggyToFile(movie(), destination.toFile(), variant, "Saved.swf");
            IggyFile file = new IggyFile(destination.toFile());
            Assert.assertEquals(file.getSwfName(), "Saved.swf");
            Assert.assertEquals(Files.size(destination), (long) bytes(file).length);
            Assert.assertEquals(file.getSwf().getFonts().size(), 1);
        } finally { Files.delete(destination); }
    }
    @Test(dataProvider = "modernVariants")
    public void createsDocumentClassWithoutDictionary(IggyFile.Variant variant) throws Exception {
        SWF swf = empty();
        FileAttributesTag attributes = new FileAttributesTag(swf);
        attributes.actionScript3 = true;
        swf.addTag(attributes);
        swf.addTag(new DoABC2Tag(swf));
        SymbolClassTag symbols = new SymbolClassTag(swf);
        symbols.tags.add(0);
        symbols.names.add("DocumentClass");
        swf.addTag(symbols);
        swf.addTag(new ShowFrameTag(swf));
        IggyFile file = reload(SwfToIggyConvertor.createIggy(reparse(swf), variant, "Document.swf"));
        Assert.assertTrue(file.getSwf().getDictionary().isEmpty());
        Assert.assertEquals(file.getSwf().getSymbolClasses().get(0).getCharacterId(), 0L);
        Assert.assertEquals(find(reparse(IggyToSwfConvertor.getSwf(file)), SymbolClassTag.class).names, Arrays.asList("DocumentClass"));
    }

    @Test(dataProvider = "variants")
    public void standaloneConversion(IggyFile.Variant variant) throws Exception {
        Path input = Files.createTempFile("iggy-create-input-", ".swf");
        Path output = Files.createTempFile("iggy-create-output-", ".iggy");
        try {
            SWF source = movie();
            ByteArrayOutputStream serialized = new ByteArrayOutputStream();
            source.saveTo(serialized);
            Files.write(input, serialized.toByteArray());
            String argument = variant.toString().replace(' ', '_');
            SwfToIggyConvertor.main(new String[]{input.toString(), output.toString(), argument});
            IggyFile file = new IggyFile(output.toFile());
            Assert.assertEquals(file.getSwfName(), input.getFileName().toString());
            Assert.assertEquals(file.getHeader().is64(), variant == IggyFile.Variant.X64);
            Assert.assertEquals(file.getSwf().getDictionary().get(9).getType(), 3);
        } finally {
            Files.delete(input);
            Files.delete(output);
        }
    }

    @Test
    public void rejectsInvalidFramesAndCreationArguments() throws Exception {
        SWF source = empty();
        source.frameCount = 0;
        source.addTag(new ShowFrameTag(source));
        try { SwfToIggyConvertor.createIggy(source, "Bad.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("frame count")); }
        try { IggyFile.create(null, "Bad.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("variant")); }
        try { IggyFile.create(IggyFile.Variant.X64, "Bad\0.swf"); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("name")); }
        try { SwfToIggyConvertor.main(new String[]{"source", "output", "unknown"}); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("Unknown Iggy variant")); }
        IggyFile file = IggyFile.create(IggyFile.Variant.X64, "New.swf");
        try { file.saveChanges(); Assert.fail(); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains("saveTo")); }
    }

    @Test(dataProvider = "variants")
    public void promotesRgbStylesWithoutChangingSource(IggyFile.Variant variant) throws Exception {
        SWF swf = empty();
        for (int id = 1; id <= 2; id++) {
            com.jpexs.decompiler.flash.tags.base.ShapeTag shape = id == 1 ? new DefineShapeTag(swf) : new DefineShape2Tag(swf);
            shape.shapeId = id;
            shape.shapeBounds = new RECT(0, 200, 0, 200);
            SHAPEWITHSTYLE data = shape.getShapes();
            FILLSTYLE fill = new FILLSTYLE();
            if (id == 1) {
                fill.fillStyleType = FILLSTYLE.SOLID;
                fill.color = new RGB(12, 34, 56);
            } else {
                fill.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
                fill.gradientMatrix = new MATRIX();
                fill.gradient = new GRADIENT();
                GRADRECORD first = new GRADRECORD();
                first.ratio = 0;
                first.color = new RGB(12, 34, 56);
                GRADRECORD last = new GRADRECORD();
                last.ratio = 255;
                last.color = new RGB(78, 90, 123);
                fill.gradient.gradientRecords = new GRADRECORD[]{first, last};
            }
            data.fillStyles.fillStyles = new FILLSTYLE[]{fill};
            LINESTYLE line = new LINESTYLE();
            line.width = 20;
            line.color = new RGB(17, 51, 85);
            data.lineStyles.lineStyles = new LINESTYLE[]{line};
            data.numFillBits = data.numLineBits = 1;
            StyleChangeRecord style = new StyleChangeRecord();
            style.stateMoveTo = true;
            style.stateFillStyle1 = true;
            style.fillStyle1 = 1;
            style.stateLineStyle = true;
            style.lineStyle = 1;
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = edge.deltaY = 200;
            edge.calculateBits();
            data.shapeRecords = Arrays.asList(style, edge, new EndShapeRecord());
            swf.addTag(shape);
        }
        swf.addTag(place(swf, 1, 1));
        swf.addTag(place(swf, 2, 2));
        swf.addTag(new ShowFrameTag(swf));
        SWF source = reparse(swf);
        ByteArrayOutputStream original = new ByteArrayOutputStream();
        source.saveTo(original);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Rgb.swf");
        ByteArrayOutputStream after = new ByteArrayOutputStream();
        source.saveTo(after);
        Assert.assertEquals(after.toByteArray(), original.toByteArray());
        SWF actual = reparse(IggyToSwfConvertor.getSwf(reload(file)));
        DefineShape4Tag solid = (DefineShape4Tag) character(actual, 1);
        RGBA color = (RGBA) solid.shapes.fillStyles.fillStyles[0].color;
        Assert.assertEquals(color.red, 12);
        Assert.assertEquals(color.alpha, 255);
        Assert.assertEquals(((RGBA) solid.shapes.lineStyles.lineStyles2[0].color).blue, 85);
        DefineShape4Tag gradient = (DefineShape4Tag) character(actual, 2);
        GRADRECORD stop = gradient.shapes.fillStyles.fillStyles[0].gradient.gradientRecords[1];
        Assert.assertEquals(stop.ratio, 255);
        Assert.assertEquals(((RGBA) stop.color).alpha, 255);
        Assert.assertEquals(stop.color.blue, 123);
    }

    @Test
    public void usesCanonicalVariantNames() throws Exception {
        Assert.assertEquals(IggyFile.Variant.X86_LEGACY.toString(), "32 legacy");
        Assert.assertEquals(IggyFile.Variant.X86.toString(), "32");
        Assert.assertEquals(IggyFile.Variant.X64.toString(), "64");
        Assert.assertEquals(IggyFile.Variant.fromString("32_legacy"), IggyFile.Variant.X86_LEGACY);
        Assert.assertEquals(IggyFile.Variant.fromString("32"), IggyFile.Variant.X86);
        Assert.assertEquals(IggyFile.Variant.fromString("64"), IggyFile.Variant.X64);
        Assert.assertEquals(IggyFile.Variant.fromString("32_LEGACY"), IggyFile.Variant.X86_LEGACY);
    }

    @DataProvider(name = "aliasTargets")
    public Object[][] aliasTargets() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : new IggyFile.Variant[]{IggyFile.Variant.X86, IggyFile.Variant.X64}) {
            for (int id = 1; id <= 10; id++) {
                result.add(new Object[]{variant, id});
            }
        }
        return result.toArray(new Object[0][]);
    }

    private static byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return bytes.toByteArray();
    }

    private static void aliases(SWF swf, int id) {
        SymbolClassTag symbols = new SymbolClassTag(swf);
        symbols.tags.addAll(Arrays.asList(id, id, id));
        symbols.names.addAll(Arrays.asList("Original", "AliasA", "AliasB"));
        swf.addTag(swf.indexOfTag(find(swf, ShowFrameTag.class)), symbols);
        swf.assignClassesToSymbols();
    }

    private static Map<String, Integer> aliasNames(IggyFile file) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (IggySymbolClass symbol : file.getSwf().getSymbolClasses()) {
            result.put(symbol.getName(), (int) symbol.getCharacterId());
        }
        return result;
    }

    @Test(dataProvider = "aliasTargets")
    public void aliasesEverySupportedDefinition(IggyFile.Variant variant, int originalId) throws Exception {
        SWF source = movie();
        aliases(source, originalId);
        PlaceObject3Tag byClass = new PlaceObject3Tag(source);
        byClass.placeFlagHasClassName = true;
        byClass.className = "AliasA";
        byClass.depth = 8;
        if (originalId == 9) {
            source.addTag(source.indexOfTag(find(source, ShowFrameTag.class)), byClass);
        }
        StartSound2Tag bySoundClass = new StartSound2Tag(source);
        bySoundClass.soundClassName = "AliasB";
        bySoundClass.soundInfo = new SOUNDINFO();
        if (originalId == 7) {
            source.addTag(source.indexOfTag(find(source, ShowFrameTag.class)), bySoundClass);
        }
        byte[] before = bytes(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "aliases");
        Map<String, Integer> names = aliasNames(file);
        Assert.assertEquals(names.size(), 3);
        Assert.assertEquals(new HashSet<>(names.values()).size(), 3);
        Assert.assertEquals((int) names.get("Original"), originalId);
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        byte[] body = character(actual, originalId).getData();
        for (int id : names.values()) {
            Assert.assertEquals(file.getSwf().getDictionary().get(id).getType(), file.getSwf().getDictionary().get(originalId).getType());
            byte[] alias = character(actual, id).getData();
            Assert.assertEquals(Arrays.copyOfRange(alias, 2, alias.length), Arrays.copyOfRange(body, 2, body.length), "Alias content differs: ID " + originalId);
            if (originalId == 9) {
                Assert.assertEquals(file.getSwf().getScalingGrid(id), file.getSwf().getScalingGrid(originalId));
            }
        }
        if (originalId == 9) {
            boolean placed = false;
            for (Tag tag : actual.getTags()) {
                placed |= tag instanceof PlaceObject3Tag && "AliasA".equals(((PlaceObject3Tag) tag).className);
            }
            Assert.assertTrue(placed, "Class-only placement retains the alias name");
        }
        if (originalId == 7) {
            Assert.assertEquals(find(actual, StartSound2Tag.class).soundClassName, "AliasB");
        }
        for (int cycle = 0; cycle < 2; cycle++) {
            SwfToIggyConvertor.updateIggy(file, source);
            file = reload(file);
            Assert.assertEquals(aliasNames(file), names, "Updates reuse alias IDs for every definition kind");
        }
        List<String> warnings = new ArrayList<>();
        IggyFile tolerant = SwfToIggyConvertor.createIggy(source, variant, "aliases", warnings::add);
        Assert.assertEquals(aliasNames(tolerant), names);
        Assert.assertTrue(warnings.isEmpty());
        Assert.assertEquals(bytes(source), before, "Strict/update/tolerant conversion leaves the source untouched");
        byte[] stable = bytes(file);
        SwfToIggyConvertor.updateIggy(file, IggyToSwfConvertor.getSwf(file));
        Assert.assertEquals(bytes(file), stable);
    }

    @Test(dataProvider = "modernVariants")
    public void spriteAliasesDoNotDuplicateNestedDeclarations(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineSpriteTag original = (DefineSpriteTag) character(source, 9);
        Tag image = character(source, 3);
        source.removeTag(image);
        DefineSpriteTag nested = new DefineSpriteTag(source);
        nested.spriteId = 40;
        nested.frameCount = 1;
        nested.addTag(image);
        nested.addTag(place(source, 3, 1));
        nested.addTag(new ShowFrameTag(source));
        original.addTag(0, nested);
        original.addTag(1, place(source, 40, 3));
        DefineScalingGridTag childGrid = new DefineScalingGridTag(source);
        childGrid.characterId = 40;
        childGrid.splitter = new RECT(20, 200, 40, 400);
        original.addTag(2, childGrid);
        aliases(source, 9);
        byte[] before = bytes(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "nested");
        Assert.assertEquals(file.getSwf().getSpriteIds().size(), 4, "Original parent/child and two parent aliases");
        Assert.assertEquals(file.getSwf().getScalingGrid(40), new float[]{1, 2, 10, 20});
        for (int id : aliasNames(file).values()) {
            Assert.assertEquals(file.getSwf().getScalingGrid(id), file.getSwf().getScalingGrid(9));
            Assert.assertEquals(file.getSwf().getSpriteTimelines().get(id).getCommands().size(), file.getSwf().getSpriteTimelines().get(9).getCommands().size());
        }
        Assert.assertEquals(bytes(source), before);
    }

    @Test(dataProvider = "modernVariants")
    public void fontAliasesKeepSeparateFontInfo(IggyFile.Variant variant) throws Exception {
        SWF source = empty();
        DefineFontTag font = new DefineFontTag(source);
        font.fontId = 40;
        SHAPE glyph = new SHAPE();
        glyph.shapeRecords = Arrays.asList(new EndShapeRecord());
        font.glyphShapeTable.add(glyph);
        source.addTag(font);
        DefineFontInfoTag info = new DefineFontInfoTag(source);
        info.fontID = 40;
        info.fontName = "Aliased legacy font";
        info.fontFlagsBold = true;
        info.codeTable.add(65);
        source.addTag(info);
        source.addTag(new ShowFrameTag(source));
        aliases(source, 40);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "font-info");
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        for (int id : aliasNames(file).values()) {
            DefineFont2Tag converted = (DefineFont2Tag) character(actual, id);
            Assert.assertEquals(converted.fontName, info.fontName);
            Assert.assertEquals(converted.codeTable, Arrays.asList(65));
            Assert.assertTrue(converted.fontFlagsBold);
        }
    }

    @Test(dataProvider = "modernVariants")
    public void unsupportedAliasesStillFollowWarningPolicy(IggyFile.Variant variant) throws Exception {
        SWF source = empty();
        DefineVideoStreamTag video = new DefineVideoStreamTag(source);
        video.characterID = 40;
        source.addTag(video);
        source.addTag(new ShowFrameTag(source));
        aliases(source, 40);
        byte[] before = bytes(source);
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "unsupported", warnings::add);
        Assert.assertTrue(file.getSwf().getSymbolClasses().isEmpty());
        Assert.assertTrue(warnings.get(0).contains("DefineVideoStream"));
        Assert.assertEquals(bytes(source), before);
    }

    @Test(dataProvider = "modernVariants")
    public void losslessImageAliasesKeepPixelsAndAlpha(IggyFile.Variant variant) throws Exception {
        SWF source = empty();
        DefineBitsLossless2Tag image = new DefineBitsLossless2Tag(source);
        image.characterID = 40;
        BufferedImage pixels = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        pixels.setRGB(0, 0, 0xff123456);
        pixels.setRGB(1, 0, 0x800000ff);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(pixels, "png", png);
        image.setImage(png.toByteArray());
        source.addTag(image);
        source.addTag(new ShowFrameTag(source));
        aliases(source, 40);
        byte[] before = bytes(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "lossless");
        SWF actual = reparse(IggyToSwfConvertor.getSwf(file));
        for (int id : aliasNames(file).values()) {
            BufferedImage converted = ((ImageTag) character(actual, id)).getImageCached().getBufferedImage();
            Assert.assertEquals(converted.getWidth(), 2);
            Assert.assertEquals(converted.getHeight(), 1);
            Assert.assertEquals(converted.getRGB(0, 0), 0xff123456);
            Assert.assertEquals(converted.getRGB(1, 0), 0x800000ff);
        }
        Assert.assertEquals(bytes(source), before);
    }

    @Test(dataProvider = "modernVariants")
    public void externalImageAliasesKeepTextureReferences(IggyFile.Variant variant) throws Exception {
        SWF source = reparse(empty());
        IggyExternalImage image = new IggyExternalImage(source);
        image.characterID = 40;
        image.width = 2;
        image.height = 3;
        image.fileName = "external.raw.iggytex";
        image.scaleX = 0.5f;
        image.offsetY = 0.25f;
        image.resourceType = 8;
        image.resourceOffset = 96;
        image.resourceFormat = 0x100;
        image.resourceWidth = 64;
        image.resourceHeight = 64;
        source.addTag(image);
        source.addTag(new ShowFrameTag(source));
        aliases(source, 40);
        byte[] before = bytes(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "external");
        Path path = Files.createTempFile("iggy-aliased-images", ".iggy");
        try {
            file.saveTo(path.toFile());
            IggyFile loaded = new IggyFile(path.toFile());
            SWF actual = reparse(IggyToSwfConvertor.getSwf(loaded));
            for (int id : aliasNames(loaded).values()) {
                IggyExternalImage converted = (IggyExternalImage) character(actual, id);
                Assert.assertEquals(converted.width, 2);
                Assert.assertEquals(converted.height, 3);
                Assert.assertEquals(converted.scaleX, 0.5f);
                Assert.assertEquals(converted.offsetY, 0.25f);
                Assert.assertEquals(converted.resourceType, 8);
                Assert.assertEquals(converted.resourceOffset, 96L);
                Assert.assertEquals(converted.resourceFormat, 0x100);
                Assert.assertEquals(converted.resourceWidth, 64);
                Assert.assertEquals(converted.resourceHeight, 64);
            }
        } finally {
            Files.deleteIfExists(path);
        }
        Assert.assertEquals(bytes(source), before);
    }

}
