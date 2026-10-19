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
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyWarningTest {

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    private static SWF movie() {
        SWF swf = new SWF();
        swf.version = 11;
        swf.frameRate = 24;
        swf.frameCount = 1;
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeId = 10;
        swf.addTag(shape);
        swf.addTag(place(swf, 10, 1));
        return swf;
    }

    private static PlaceObject2Tag place(SWF swf, int id, int depth) {
        PlaceObject2Tag tag = new PlaceObject2Tag(swf);
        tag.placeFlagHasCharacter = true;
        tag.characterId = id;
        tag.depth = depth;
        return tag;
    }

    private static byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return out.toByteArray();
    }

    private static boolean absent(IggyFile file, int id) {
        return id >= file.getSwf().getDictionary().size() || file.getSwf().getDictionary().get(id).isNull();
    }

    private static boolean mentions(List<String> warnings, String text) {
        return warnings.stream().anyMatch(warning -> warning.contains(text));
    }

    @Test(dataProvider = "variants")
    public void skipsRootAndNestedActionsWithoutChangingSource(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        source.addTag(new DoActionTag(source));
        DefineSpriteTag sprite = new DefineSpriteTag(source);
        sprite.spriteId = 11;
        sprite.frameCount = 1;
        sprite.addTag(place(source, 10, 1));
        sprite.addTag(new DoActionTag(source));
        sprite.addTag(new ShowFrameTag(source));
        source.addTag(sprite);
        source.addTag(place(source, 11, 2));
        ExportAssetsTag exports = new ExportAssetsTag(source);
        exports.tags.add(10);
        exports.names.add("Shape");
        source.addTag(exports);
        source.addTag(new ShowFrameTag(source));
        byte[] before = bytes(source);
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Actions.swf", warnings::add);
        Assert.assertEquals(warnings.size(), 3);
        Assert.assertTrue(mentions(warnings, "DoAction"));
        Assert.assertTrue(mentions(warnings, "main/sprite11/"));
        Assert.assertTrue(mentions(warnings, "ExportAssets"));
        Assert.assertEquals(bytes(source), before);
        Assert.assertEquals(file.getSwf().getDictionary().get(10).getType(), 1);
        Assert.assertEquals(file.getSwf().getDictionary().get(11).getType(), 3);
        Assert.assertEquals(file.getSwf().getSpriteTimelines().get(11).getCommands().size(), 2);
        Assert.assertEquals(IggyToSwfConvertor.getSwf(file).frameCount, 1);
        try {
            SwfToIggyConvertor.createIggy(source, variant, "Strict.swf");
            Assert.fail("The default API must remain strict");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains("Unsupported tag"));
        }
    }

    @Test(dataProvider = "variants")
    public void skipsUnsupportedDefinitionsAndTheirReferences(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineVideoStreamTag video = new DefineVideoStreamTag(source);
        video.characterID = 20;
        video.width = video.height = 4;
        video.numFrames = 1;
        source.addTag(video);
        source.addTag(place(source, 20, 2));
        DefineSpriteTag sprite = new DefineSpriteTag(source);
        sprite.spriteId = 21;
        sprite.frameCount = 1;
        sprite.addTag(place(source, 20, 1));
        sprite.addTag(place(source, 10, 2));
        sprite.addTag(new ShowFrameTag(source));
        source.addTag(sprite);
        SymbolClassTag symbols = new SymbolClassTag(source);
        symbols.tags.add(20);
        symbols.names.add("VideoClass");
        symbols.tags.add(10);
        symbols.names.add("ShapeClass");
        source.addTag(symbols);
        PlaceObject3Tag byClass = new PlaceObject3Tag(source);
        byClass.placeFlagHasClassName = true;
        byClass.className = "VideoClass";
        byClass.depth = 3;
        source.addTag(byClass);
        source.addTag(new ShowFrameTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Video.swf", warnings::add);
        Assert.assertTrue(absent(file, 20));
        Assert.assertEquals(file.getSwf().getDictionary().get(21).getType(), 3);
        Assert.assertEquals(file.getSwf().getSpriteTimelines().get(21).getCommands().size(), 2);
        Assert.assertEquals(file.getSwf().getTimeline().getCommands().size(), 2);
        Assert.assertTrue(mentions(warnings, "DefineVideoStream"));
        Assert.assertTrue(mentions(warnings, "omitted definition"));
        if (variant != IggyFile.Variant.X86_LEGACY) {
            Assert.assertEquals(file.getSwf().getSymbolClasses().size(), 1);
            Assert.assertEquals(file.getSwf().getSymbolClasses().get(0).getCharacterId(), 10L);
        }
    }

    @Test(dataProvider = "variants")
    public void removesDependentDefinitionsTransitively(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineFont4Tag font = new DefineFont4Tag(source);
        font.fontID = 30;
        source.addTag(font);
        DefineEditTextTag text = new DefineEditTextTag(source);
        text.characterID = 31;
        text.hasFont = true;
        text.fontId = 30;
        text.fontHeight = 240;
        source.addTag(text);
        source.addTag(place(source, 31, 2));
        source.addTag(new ShowFrameTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Cff.swf", warnings::add);
        Assert.assertTrue(absent(file, 30));
        Assert.assertTrue(absent(file, 31));
        Assert.assertTrue(mentions(warnings, "DefineFont4"));
        Assert.assertTrue(mentions(warnings, "DefineEditText"));
        Assert.assertEquals(file.getSwf().getTimeline().getCommands().size(), 2);
    }

    @Test(dataProvider = "variants")
    public void skipsUnsupportedContentReportedBySpecializedConverters(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineButtonTag button = new DefineButtonTag(source);
        button.buttonId = 20;
        BUTTONRECORD record = new BUTTONRECORD(source, button);
        record.characterId = 10;
        record.placeDepth = 1;
        record.placeMatrix = new MATRIX();
        record.buttonStateUp = true;
        button.characters.add(record);
        button.setActionBytes(new byte[]{6, 0});
        source.addTag(button);
        source.addTag(place(source, 20, 2));
        DefineShape4Tag winding = new DefineShape4Tag(source);
        winding.shapeId = 21;
        winding.usesFillWindingRule = true;
        source.addTag(winding);
        source.addTag(place(source, 21, 3));
        PlaceObject3Tag hidden = new PlaceObject3Tag(source);
        hidden.depth = 4;
        hidden.placeFlagHasCharacter = true;
        hidden.characterId = 10;
        hidden.placeFlagHasVisible = true;
        hidden.visible = 0;
        source.addTag(hidden);
        source.addTag(new ShowFrameTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Features.swf", warnings::add);
        Assert.assertTrue(absent(file, 20));
        Assert.assertTrue(absent(file, 21));
        Assert.assertTrue(mentions(warnings, "AVM1 button actions"));
        Assert.assertTrue(mentions(warnings, "nonzero fill winding"));
        Assert.assertTrue(mentions(warnings, "visible"));
        Assert.assertEquals(file.getSwf().getTimeline().getCommands().size(), 3);
    }

    @DataProvider(name = "placeAttributes")
    public Object[][] placeAttributes() {
        List<Object[]> cases = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (int attributes = 0; attributes < 4; attributes++) {
                for (boolean cache : new boolean[]{false, true}) {
                    cases.add(new Object[]{variant, attributes, cache});
                }
            }
        }
        return cases.toArray(new Object[0][]);
    }

    private static PlaceObject3Tag attributedPlace(SWF swf, int attributes, boolean cache, boolean update) {
        PlaceObject3Tag place = new PlaceObject3Tag(swf);
        place.depth = 7;
        place.placeFlagMove = update;
        place.placeFlagHasCharacter = !update;
        place.characterId = 10;
        place.placeFlagHasMatrix = true;
        place.matrix = new MATRIX();
        place.matrix.translateX = update ? 300 : 100;
        place.matrix.translateY = update ? 400 : 200;
        place.placeFlagHasName = true;
        place.name = update ? "updated" : "placed";
        place.placeFlagHasCacheAsBitmap = true;
        place.bitmapCache = cache ? 1 : 0;
        if (attributes != 2) {
            place.placeFlagHasVisible = true;
            place.visible = attributes == 1 ? 1 : 0;
        }
        if (attributes >= 2) {
            place.placeFlagOpaqueBackground = true;
            place.backgroundColor = new RGBA(10, 20, 30, 255);
        }
        return place;
    }

    private static void assertAttributedPlace(PlaceObjectTypeTag place, boolean cache, boolean update) {
        Assert.assertEquals(place.getDepth(), 7);
        Assert.assertEquals(place.flagMove(), update);
        Assert.assertEquals(place.getCharacterId(), update ? -1 : 10);
        Assert.assertEquals(place.getInstanceName(), update ? "updated" : "placed");
        Assert.assertEquals(place.getMatrix().translateX, update ? 300 : 100);
        Assert.assertEquals(place.getMatrix().translateY, update ? 400 : 200);
        Assert.assertEquals(place.cacheAsBitmap(), cache);
        Assert.assertNull(place.getVisible());
        Assert.assertNull(place.getBackgroundColor());
    }

    @Test(dataProvider = "placeAttributes")
    public void retainsPlacementsWhenOmittingUnsupportedAttributes(IggyFile.Variant variant, int attributes, boolean cache) throws Exception {
        SWF source = movie();
        source.addTag(attributedPlace(source, attributes, cache, false));
        source.addTag(attributedPlace(source, attributes, cache, true));
        DefineSpriteTag sprite = new DefineSpriteTag(source);
        sprite.spriteId = 11;
        sprite.frameCount = 1;
        sprite.addTag(attributedPlace(source, attributes, cache, false));
        sprite.addTag(attributedPlace(source, attributes, cache, true));
        sprite.addTag(new ShowFrameTag(source));
        source.addTag(sprite);
        source.addTag(new ShowFrameTag(source));
        byte[] before = bytes(source);
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Attributes.swf", warnings::add);
        file.updateFlashEntry();
        MemoryDataStream data = new MemoryDataStream();
        file.writeToDataStream(data);
        SWF result = IggyToSwfConvertor.getSwf(new IggyFile(new MemoryDataStream(data.getAllBytes())));
        result = new SWF(new ByteArrayInputStream(bytes(result)), false, "UTF-8");
        List<PlaceObjectTypeTag> rootPlaces = new ArrayList<>();
        for (Tag tag : result.getTags()) {
            if (tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getDepth() == 7) {
                rootPlaces.add((PlaceObjectTypeTag) tag);
            }
        }
        Assert.assertEquals(rootPlaces.size(), 2);
        assertAttributedPlace(rootPlaces.get(0), cache, false);
        assertAttributedPlace(rootPlaces.get(1), cache, true);
        DefineSpriteTag convertedSprite = (DefineSpriteTag) result.getCharacter(11);
        Assert.assertEquals(convertedSprite.getTags().size(), 3);
        assertAttributedPlace((PlaceObjectTypeTag) convertedSprite.getTags().get(0), cache, false);
        assertAttributedPlace((PlaceObjectTypeTag) convertedSprite.getTags().get(1), cache, true);
        Assert.assertEquals(warnings.size(), attributes == 3 ? 8 : 4);
        Assert.assertTrue(mentions(warnings, "PlaceObject3"));
        Assert.assertTrue(mentions(warnings, "main/sprite11/"));
        Assert.assertFalse(mentions(warnings, "Skipping PlaceObject"));
        Assert.assertFalse(mentions(warnings, "cache"));
        Assert.assertEquals(bytes(source), before, "Source SWF must remain unchanged");
        if (attributes != 1) {
            try {
                SwfToIggyConvertor.createIggy(source, variant, "Strict.swf");
                Assert.fail("Strict API must still reject unsupported attributes");
            } catch (IOException ex) {
                Assert.assertTrue(ex.getMessage().contains("visibility or opaque background"), ex.getMessage());
            }
        }
    }

    @Test(dataProvider = "variants")
    public void skipsUnsupportedImportSettings(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        ImportAssets2Tag imports = new ImportAssets2Tag(source);
        imports.url = "library.swf";
        imports.downloadNow = 0;
        imports.tags.add(50);
        imports.names.add("ImportedAsset");
        source.addTag(imports);
        source.addTag(place(source, 50, 2));
        source.addTag(new ShowFrameTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "Imports.swf", warnings::add);
        Assert.assertTrue(file.getSwf().getImports().isEmpty());
        Assert.assertTrue(absent(file, 50));
        Assert.assertTrue(mentions(warnings, "ImportAssets2"));
        Assert.assertTrue(mentions(warnings, "omitted definition"));
    }

    @Test
    public void skipsAs3TagsForLegacyLayout() throws Exception {
        SWF source = movie();
        FileAttributesTag attributes = new FileAttributesTag(source);
        attributes.actionScript3 = true;
        source.addTag(attributes);
        source.addTag(new DoABC2Tag(source));
        SymbolClassTag symbols = new SymbolClassTag(source);
        symbols.tags.add(0);
        symbols.names.add("DocumentClass");
        source.addTag(symbols);
        source.addTag(new ShowFrameTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, IggyFile.Variant.X86_LEGACY, "Legacy.swf", warnings::add);
        Assert.assertFalse(file.getSwf().hasAS3());
        Assert.assertTrue(file.getSwf().getAbcBlocks().isEmpty());
        Assert.assertTrue(file.getSwf().getSymbolClasses().isEmpty());
        Assert.assertEquals(warnings.size(), 3);
    }

    @Test
    public void doesNotHideInvalidReferencesOrMovieProperties() throws Exception {
        SWF source = movie();
        source.addTag(place(source, 42, 2));
        source.addTag(new ShowFrameTag(source));
        try {
            SwfToIggyConvertor.createIggy(source, IggyFile.Variant.X64, "Invalid.swf", warning -> { });
            Assert.fail();
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains("Missing character"), ex.getMessage());
        }
        source = movie();
        source.frameRate = 30;
        source.addTag(new ShowFrameTag(source));
        try {
            SwfToIggyConvertor.createIggy(source, IggyFile.Variant.X86_LEGACY, "Invalid.swf", warning -> { });
            Assert.fail();
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains("24 FPS"), ex.getMessage());
        }
    }
    @Test
    public void standaloneCliAlsoWarnsAndContinues() throws Exception {
        Path directory = Files.createTempDirectory("iggy-warning-cli-");
        Path input = directory.resolve("input.swf");
        Path output = directory.resolve("output.iggy");
        PrintStream previous = System.err;
        ByteArrayOutputStream warnings = new ByteArrayOutputStream();
        try {
            SWF source = movie();
            source.addTag(new DoActionTag(source));
            source.addTag(attributedPlace(source, 3, true, false));
            source.addTag(new ShowFrameTag(source));
            Files.write(input, bytes(source));
            System.setErr(new PrintStream(warnings, true, "UTF-8"));
            SwfToIggyConvertor.main(new String[]{input.toString(), output.toString(), "64"});
            IggyFile result = new IggyFile(output.toFile());
            Assert.assertEquals(result.getSwf().getDictionary().get(10).getType(), 1);
            SWF converted = IggyToSwfConvertor.getSwf(result);
            boolean found = false;
            for (Tag tag : converted.getTags()) {
                if (tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getDepth() == 7) {
                    assertAttributedPlace((PlaceObjectTypeTag) tag, true, false);
                    found = true;
                }
            }
            Assert.assertTrue(found, "CLI must retain the placement");
            String text = new String(warnings.toByteArray(), StandardCharsets.UTF_8);
            Assert.assertTrue(text.contains("Warning:"));
            Assert.assertTrue(text.contains("DoAction"));
            Assert.assertTrue(text.contains("visible"));
            Assert.assertTrue(text.contains("opaqueBackground"));
            Assert.assertFalse(text.contains("Skipping PlaceObject"));
        } finally {
            System.setErr(previous);
            Files.deleteIfExists(input);
            Files.deleteIfExists(output);
            Files.delete(directory);
        }
    }

}
