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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyImportConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.*;
import com.jpexs.decompiler.flash.profiles.IggySwfProfile;
import com.jpexs.decompiler.flash.tags.*;
import com.jpexs.decompiler.flash.types.*;
import com.jpexs.decompiler.flash.types.shaperecords.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyImportTest {

    @DataProvider(name = "layouts")
    public Object[][] layouts() { return new Object[][] {{1}, {2}}; }

    /** Independent binary fixture; does not use the import writer under test. */
    private static final class Fixture {
        final boolean wide;
        final MemoryDataStream data = new MemoryDataStream();
        final List<IggyFixup.Field> fields = new ArrayList<>();
        Fixture(boolean wide) { this.wide = wide; }
        long allocate(int size) throws Exception {
            data.pad8bytes(); long at = data.position(); data.writeBytes(new byte[size]); return at;
        }
        void value(long at, long value, int size, int operation) throws Exception {
            long end = data.position(); data.seek(at, SeekMode.SET);
            if (size == 8) { data.writeUI64(value); }
            else if (size == 4) { data.writeUI32(value); }
            else if (size == 2) { data.writeUI16((int) value); }
            else { data.writeUI8((int) value); }
            data.seek(end, SeekMode.SET);
            if (operation != 0) { fields.add(new IggyFixup.Field(at, operation)); }
        }
        void u32(long at, long value) throws Exception { value(at, value, 4, 5); }
        void ref(long at, long target) throws Exception { value(at, target == 0 ? 1 : target - at, wide ? 8 : 4, 2); }
        long string(String text) throws Exception {
            long at = allocate((text.length() + 1) * 2);
            for (int i = 0; i <= text.length(); i++) { value(at + 2L * i, i == text.length() ? 0 : text.charAt(i), 2, 4); }
            return at;
        }
    }

    private static byte[] fixture(int layout, boolean imports, boolean placements) throws Exception {
        return fixture(layout, imports, placements, false);
    }

    private static byte[] fixture(int layout, boolean imports, boolean placements, boolean symbolClass) throws Exception {
        boolean wide = layout == 2; int p = wide ? 8 : 4, stride = wide ? 24 : 12;
        Fixture f = new Fixture(wide);
        f.allocate(wide ? 184 : layout == 1 ? 128 : 64);
        for (int at : wide ? new int[] {0,8,16,24,32,40,112,120,136,144,152,160}
                : layout == 1 ? new int[] {0,4,8,12,16,20,80,84,96,100,104,108}
                : new int[] {0,8,12,44,56,60}) { f.ref(at, 0); }
        f.u32(wide ? 64 : layout == 1 ? 40 : 4, 4);
        f.u32(wide ? 68 : layout == 1 ? 44 : 32, 1);
        if (layout != 0) { f.u32(wide ? 88 : 64, Float.floatToIntBits(24)); }
        long dictionary = f.allocate(4 * p); f.ref(0, dictionary);
        for (int i = 0; i < 4; i++) { f.ref(dictionary + (long) i * p, 0); }
        long local = f.allocate(wide ? 32 : 16); f.value(local, 27, 1, 0); f.ref(dictionary + p, local);
        long placeholder = 0;
        if (imports) {
            placeholder = f.allocate(wide ? 32 : 16);
            f.value(placeholder, 0x0002ff12, 4, 0);
            f.ref(placeholder + (wide ? 16 : 8), 0); f.ref(dictionary + 2L * p, placeholder);
            long table = f.allocate(2 * stride), symbols = f.allocate(2 * stride);
            f.ref(wide ? 112 : 80, table); f.u32(wide ? 128 : 88, 2);
            f.ref(table, f.string("../library_ž.swf")); f.ref(table + p, symbols); f.u32(table + 2 * p, 2);
            f.ref(table + stride, f.string("classes_only.swf")); f.ref(table + stride + p, 0); f.u32(table + stride + 2 * p, 0);
            if (wide) { f.u32(table + 20, 0x12345678); }
            for (int i = 0; i < 2; i++) {
                long at = symbols + (long) i * stride;
                f.ref(at, i == 0 ? placeholder : 0); f.ref(at + p, f.string(i == 0 ? "ui.Widget" : "ui.Font"));
                f.u32(at + 2 * p, i + 2);
                if (wide) { f.u32(at + 20, 0x87654321L + i); }
            }
            if (symbolClass) {
                long binding = f.allocate(stride);
                f.ref(wide ? 160 : 108, binding); f.u32(wide ? 176 : 120, 1);
                f.ref(binding, placeholder); f.ref(binding + p, f.string("ui.WidgetClass")); f.u32(binding + 2 * p, 2);
            }
        }
        long show = f.allocate(wide ? 16 : 8); f.ref(show, 0); f.value(show + p, 1, 1, 0);
        long first = show;
        if (placements) {
            long fixes = f.allocate(p + 4), entries = f.allocate(16);
            f.ref(wide ? 136 : 96, fixes); f.ref(fixes, entries); f.u32(fixes + p, 2);
            for (int i = 1; i >= 0; i--) {
                long at = f.allocate(wide ? 64 : 48); f.ref(at, first); first = at;
                f.value(at + p, 2, 1, 0); f.value(at + p + 1, 0, 1, 3);
                f.value(at + p + 2, 127, 1, 0); f.value(at + p + 3, 15, 1, 0); f.u32(at + p + 4, 2);
                f.value(at + (wide ? 40 : 36), i + 1, 2, 4);
                long field = at + (wide ? 48 : 40); f.ref(field, i == 0 ? placeholder : 0);
                f.u32(entries + 8L * i, field); f.u32(entries + 8L * i + 4, i + 2);
            }
        }
        f.ref(wide ? 16 : 8, first); f.ref(wide ? 24 : 12, show);
        byte[] main = f.data.getAllBytes(); IggyIndexBuilder index = new IggyIndexBuilder();
        IggyFixup.writeFields(index, f.fields, main.length);
        byte[] program = index.getIndexBytes(); MemoryDataStream result = new MemoryDataStream();
        new IggyHeader(0x900, 1, wide ? 64 : 32, 1, 0, 0, new byte[12], 2).writeToDataStream(result);
        new IggySubFileEntry(1, main.length, 0, 64).writeToDataStream(result);
        new IggySubFileEntry(0, program.length, 0, 64 + main.length).writeToDataStream(result);
        result.writeBytes(main); result.writeBytes(program); return result.getAllBytes();
    }

    private static IggyFile load(byte[] data) throws Exception {
        Path path = Files.createTempFile("iggy-import-", ".iggy");
        try { Files.write(path, data); return new IggyFile(path.toFile()); }
        finally { Files.delete(path); }
    }
    private static byte[] save(IggyFile file) throws Exception {
        file.updateFlashEntry(); MemoryDataStream data = new MemoryDataStream(); file.writeToDataStream(data); return data.getAllBytes();
    }
    private static SWF reparse(SWF swf) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream(); swf.saveTo(data);
        return new SWF(new ByteArrayInputStream(data.toByteArray()), false, false, "UTF-8", new IggySwfProfile());
    }
    private static List<ImportAssets2Tag> imports(SWF swf) {
        List<ImportAssets2Tag> result = new ArrayList<>();
        for (Tag tag : swf.getTags()) { if (tag instanceof ImportAssets2Tag) { result.add((ImportAssets2Tag) tag); } }
        return result;
    }
    private static ImportAssets2Tag addImport(SWF swf, int id) {
        ImportAssets2Tag tag = new ImportAssets2Tag(swf); tag.url = "new_library.swf";
        if (id != 0) { tag.tags.add(id); tag.names.add("ui.NewSymbol"); }
        swf.addTag(1, tag); return tag;
    }

    @Test(dataProvider = "layouts")
    public void exportsSymbolsEmptyLibrariesAndPreservesUnchangedBinary(int layout) throws Exception {
        byte[] original = fixture(layout, true, true); IggyFile file = load(original);
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file)); List<ImportAssets2Tag> tags = imports(swf);
        Assert.assertEquals(tags.size(), 2); Assert.assertEquals(tags.get(0).url, "../library_ž.swf");
        Assert.assertEquals(tags.get(0).tags, Arrays.asList(2, 3));
        Assert.assertEquals(tags.get(0).names, Arrays.asList("ui.Widget", "ui.Font"));
        Assert.assertEquals(tags.get(0).downloadNow, 1); Assert.assertEquals(tags.get(0).hasDigest, 0);
        Assert.assertEquals(tags.get(1).url, "classes_only.swf"); Assert.assertTrue(tags.get(1).tags.isEmpty());
        Assert.assertTrue(swf.getTags().indexOf(tags.get(0)) < swf.getTags().indexOf(tags.get(1)));
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf); Assert.assertEquals(save(file), original);
    }

    @Test(dataProvider = "layouts")
    public void importedIdsRemainAvailableToSymbolClass(int layout) throws Exception {
        byte[] original = fixture(layout, true, false, true); IggyFile file = load(original);
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file)); boolean found = false;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag classes = (SymbolClassTag) tag;
                Assert.assertEquals(classes.tags, Arrays.asList(2));
                Assert.assertEquals(classes.names, Arrays.asList("ui.WidgetClass")); found = true;
            }
        }
        Assert.assertTrue(found);
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf); Assert.assertEquals(save(file), original);
    }

    @Test(dataProvider = "layouts")
    public void editsNamesAddsEmptyDependencyAndExtendsDictionary(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, true)); SWF swf = IggyToSwfConvertor.getSwf(file);
        List<ImportAssets2Tag> tags = imports(swf); tags.get(0).url = "../new_library.swf";
        tags.get(0).names.set(0, "ui.OtherWidget"); tags.get(0).tags.add(500); tags.get(0).names.add("ui.NewSymbol");
        addImport(swf, 0); SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        byte[] saved = save(file); IggyFile result = load(saved);
        List<IggyImport> libraries = result.getSwf().getImports(); Assert.assertEquals(libraries.size(), 3);
        Assert.assertEquals(libraries.get(0).getLibrary(), "new_library.swf");
        Assert.assertEquals(libraries.get(1).getLibrary(), "../new_library.swf");
        Assert.assertEquals(libraries.get(1).getSymbols().get(0).getName(), "ui.OtherWidget");
        Assert.assertEquals(libraries.get(1).getSymbols().get(2).getCharacterId(), 500L);
        Assert.assertEquals(result.getSwf().getDictionary().size(), 501); Assert.assertTrue(result.getSwf().getDictionary().get(500).isNull());
        if (layout == 2) { Assert.assertEquals(libraries.get(1).getSymbols().get(0).getReserved(), 0x87654321L); }
        SWF exported = reparse(IggyToSwfConvertor.getSwf(result));
        SwfToIggyConvertor.updateIggy(result.getSwf(), exported); Assert.assertEquals(save(result), saved);
    }

    @Test(dataProvider = "layouts")
    public void newImportedCharacterCanBePlacedAndResolvedThroughDictionaryFixup(int layout) throws Exception {
        IggyFile file = load(fixture(layout, false, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        addImport(swf, 500); PlaceObject3Tag place = new PlaceObject3Tag(swf);
        place.placeFlagHasCharacter = true; place.characterId = 500; place.depth = 1;
        swf.addTag(swf.getTags().size() - 2, place);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggyFile result = load(save(file));
        Assert.assertTrue(result.getSwf().getDictionary().get(500).isNull());
        Assert.assertEquals(result.getSwf().getImports().get(0).getSymbols().get(0).getCharacterId(), 500L);
        SWF exported = reparse(IggyToSwfConvertor.getSwf(result)); boolean found = false;
        for (Tag tag : exported.getTags()) { if (tag instanceof PlaceObject3Tag) { Assert.assertEquals(((PlaceObject3Tag) tag).characterId, 500); found = true; } }
        Assert.assertTrue(found);
    }

    @Test(dataProvider = "layouts")
    public void removesUnusedLibrariesAndClearsPlaceholder(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        for (ImportAssets2Tag tag : imports(swf)) { swf.removeTag(tag); }
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggyFile result = load(save(file));
        Assert.assertTrue(result.getSwf().getImports().isEmpty()); Assert.assertEquals(result.getSwf().getHdr().getImportCount(), 0L);
        Assert.assertEquals(result.getSwf().getHdr().getImportsAddress(), 0L);
        Assert.assertTrue(result.getSwf().getDictionary().get(2).isNull());
    }

    @Test(dataProvider = "layouts")
    public void remapsImportIdAndItsTimelineReferences(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, true)); SWF swf = IggyToSwfConvertor.getSwf(file);
        imports(swf).get(0).tags.set(0, 500);
        for (Tag tag : swf.getTags()) {
            if (tag instanceof PlaceObject3Tag && ((PlaceObject3Tag) tag).characterId == 2) {
                ((PlaceObject3Tag) tag).characterId = 500;
            }
        }
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggyFile result = load(save(file));
        Assert.assertTrue(result.getSwf().getDictionary().get(2).isNull());
        List<Integer> placed = new ArrayList<>();
        for (Tag tag : reparse(IggyToSwfConvertor.getSwf(result)).getTags()) {
            if (tag instanceof PlaceObject3Tag) { placed.add(((PlaceObject3Tag) tag).characterId); }
        }
        Assert.assertEquals(placed, Arrays.asList(500, 3));
    }

    @Test(dataProvider = "layouts")
    public void editedPlacementOfImportPlaceholderKeepsRuntimeDictionaryFixup(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, true)); SWF swf = IggyToSwfConvertor.getSwf(file);
        for (Tag tag : swf.getTags()) {
            if (tag instanceof PlaceObject3Tag) { ((PlaceObject3Tag) tag).depth += 10; }
        }
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggySwf movie = load(save(file)).getSwf();
        List<Integer> ids = new ArrayList<>();
        for (IggyTimeline.Command command : movie.getTimeline().getCommands()) {
            if (command.getType() == 2) {
                long field = command.getAddress() + (layout == 2 ? 48 : 40);
                Assert.assertEquals(movie.getTimeline().reference(field), 0L);
                ids.add(movie.getDictionaryId(field, 0));
            }
        }
        Assert.assertEquals(ids, Arrays.asList(2, 3));
    }

    @Test(dataProvider = "layouts")
    public void bitmapFillCanReferToAnImportedCharacter(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        DefineShape4Tag shape = new DefineShape4Tag(swf); shape.shapeId = 10;
        shape.shapeBounds = new RECT(0, 20, 0, 20); shape.edgeBounds = new RECT(shape.shapeBounds);
        FILLSTYLE fill = new FILLSTYLE(); fill.fillStyleType = 64; fill.bitmapId = 2; fill.bitmapMatrix = new MATRIX();
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[] {fill}; shape.shapes.shapeRecords.clear();
        StyleChangeRecord move = new StyleChangeRecord(); move.stateMoveTo = true; move.stateFillStyle0 = true; move.fillStyle0 = 1;
        shape.shapes.shapeRecords.add(move);
        for (int[] delta : new int[][] {{20,0},{0,20},{-20,0},{0,-20}}) {
            StraightEdgeRecord edge = new StraightEdgeRecord(); edge.deltaX = delta[0]; edge.deltaY = delta[1];
            edge.generalLineFlag = true; edge.simplify(); edge.calculateBits(); shape.shapes.shapeRecords.add(edge);
        }
        shape.shapes.shapeRecords.add(new EndShapeRecord()); swf.addTag(1, shape);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggyFile result = load(save(file));
        IggySwf movie = result.getSwf(); IggyTimeline t = movie.getTimeline();
        long object = movie.getDictionary().get(10).getAddress();
        long groups = t.reference(object + (layout == 2 ? 72 : 44));
        long fills = t.reference(groups + (layout == 2 ? 32 : 24));
        long field = fills + (layout == 2 ? 8 + 64 : 4 + 56);
        Assert.assertEquals(t.reference(field), 0L); Assert.assertEquals(movie.getDictionaryId(field, 0), 2);
        boolean found = false;
        for (Tag tag : reparse(IggyToSwfConvertor.getSwf(result)).getTags()) {
            if (tag instanceof DefineShape4Tag) {
                Assert.assertEquals(((DefineShape4Tag) tag).shapes.fillStyles.fillStyles[0].bitmapId, 2); found = true;
            }
        }
        Assert.assertTrue(found);
    }

    @Test(dataProvider = "layouts", expectedExceptions = IOException.class)
    public void removingReferencedSymbolIsRejected(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, true)); SWF swf = IggyToSwfConvertor.getSwf(file);
        for (ImportAssets2Tag tag : imports(swf)) { swf.removeTag(tag); }
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
    }

    @Test(dataProvider = "layouts")
    public void legacyTagIsNormalizedToImportAssets2(int layout) throws Exception {
        IggyFile file = load(fixture(layout, false, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        ImportAssetsTag legacy = new ImportAssetsTag(swf); legacy.url = "old_library.swf";
        legacy.tags.add(7); legacy.names.add("ui.OldSymbol"); swf.addTag(1, legacy);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf)); IggyFile result = load(save(file));
        ImportAssets2Tag tag = imports(reparse(IggyToSwfConvertor.getSwf(result))).get(0);
        Assert.assertEquals(tag.url, legacy.url); Assert.assertEquals(tag.tags, legacy.tags); Assert.assertEquals(tag.names, legacy.names);
    }

    @Test(dataProvider = "layouts")
    public void revertingImportEditsRestoresOriginalBytes(int layout) throws Exception {
        byte[] original = fixture(layout, true, false); IggyFile file = load(original);
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file)), changed = reparse(swf);
        addImport(changed, 50); SwfToIggyConvertor.updateIggy(file.getSwf(), changed);
        Assert.assertEquals(file.getSwf().getImports().size(), 3);
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf); Assert.assertEquals(save(file), original);
    }

    @Test(dataProvider = "layouts", expectedExceptions = IOException.class)
    public void repeatedUpdatesCannotReintroduceReferenceToRemovedImport(int layout) throws Exception {
        IggyFile file = load(fixture(layout, true, true)); SWF source = IggyToSwfConvertor.getSwf(file);
        for (ImportAssets2Tag tag : imports(source)) { source.removeTag(tag); }
        SWF withoutReferences = reparse(source);
        List<Tag> remove = new ArrayList<>();
        for (Tag tag : withoutReferences.getTags()) { if (tag instanceof PlaceObject3Tag) { remove.add(tag); } }
        for (Tag tag : remove) { withoutReferences.removeTag(tag); }
        SwfToIggyConvertor.updateIggy(file.getSwf(), withoutReferences);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(source));
    }

    @DataProvider(name = "invalid")
    public Object[][] invalid() { return new Object[][] {{"zero"}, {"large"}, {"duplicate"}, {"local"}, {"definition"}, {"nested"}, {"digest"}, {"deferred"}, {"url"}, {"name"}, {"mismatch"}}; }

    @Test(dataProvider = "invalid", expectedExceptions = IOException.class)
    public void invalidImportsAreRejected(String reason) throws Exception {
        IggyFile file = load(fixture(2, false, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        ImportAssets2Tag tag = addImport(swf, 7);
        switch (reason) {
            case "zero": tag.tags.set(0, 0); break;
            case "large": tag.tags.set(0, 65536); break;
            case "duplicate": addImport(swf, 7); break;
            case "local": tag.tags.set(0, 1); break;
            case "definition": DefineSoundTag sound = new DefineSoundTag(swf); sound.soundId = 7; swf.addTag(sound); break;
            case "nested": DefineSpriteTag sprite = new DefineSpriteTag(swf); sprite.spriteId = 8; sprite.addTag(new ImportAssets2Tag(swf)); swf.addTag(sprite); break;
            case "digest": tag.hasDigest = 1; break;
            case "deferred": tag.downloadNow = 0; break;
            case "url": tag.url = ""; break;
            case "name": tag.names.set(0, "bad\0name"); break;
            case "mismatch": tag.names.clear(); break;
            default: throw new AssertionError(reason);
        }
        IggyImportConvertor.updateImports(file.getSwf(), swf);
    }

    @Test(expectedExceptions = IOException.class)
    public void older32BitLayoutRejectsUnsupportedImportTable() throws Exception {
        IggyFile file = load(fixture(0, false, false)); SWF swf = IggyToSwfConvertor.getSwf(file);
        addImport(swf, 0); SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
    }
}
