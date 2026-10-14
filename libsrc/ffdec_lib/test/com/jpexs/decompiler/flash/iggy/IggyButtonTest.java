/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 * This library is licensed under the GNU Lesser General Public License,
 * version 3.0 or later. See the license distributed with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyButtonConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.profiles.IggySwfProfile;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyButtonTest {

    @DataProvider(name = "layouts")
    public Object[][] layouts() {
        return new Object[][]{{0}, {1}, {2}};
    }

    /** Independently assembled native layout; does not use the button writer. */
    private static final class Fixture {
        final MemoryDataStream data = new MemoryDataStream();
        final List<IggyFixup.Field> fields = new ArrayList<>();
        final boolean wide;
        final int layout;
        long button, records, rootPlace;

        Fixture(int layout) {
            this.layout = layout;
            wide = layout == 2;
        }

        void put(long at, long value, int width, int operation) throws Exception {
            long end = data.position();
            data.seek(at, SeekMode.SET);
            if (width == 8) { data.writeUI64(value); }
            else if (width == 4) { data.writeUI32(value); }
            else if (width == 2) { data.writeUI16((int) value); }
            else { data.writeUI8((int) value); }
            data.seek(end, SeekMode.SET);
            if (operation != 0) { fields.add(new IggyFixup.Field(at, operation)); }
        }

        void u8(long at, int value) throws Exception { put(at, value, 1, 0); }
        void u16(long at, int value) throws Exception { put(at, value, 2, 4); }
        void u32(long at, long value) throws Exception { put(at, value, 4, 5); }
        void f32(long at, float value) throws Exception { u32(at, Float.floatToIntBits(value)); }
        void ref(long at, long target) throws Exception { put(at, target == 0 ? 1 : target - at, wide ? 8 : 4, 2); }
        long allocate(int size) throws Exception {
            data.pad8bytes();
            long at = data.position();
            data.writeBytes(new byte[size]);
            return at;
        }

        byte[] file() throws Exception {
            byte[] main = data.getAllBytes();
            IggyIndexBuilder index = new IggyIndexBuilder();
            IggyFixup.writeFields(index, fields, main.length);
            byte[] program = index.getIndexBytes();
            MemoryDataStream out = new MemoryDataStream();
            new IggyHeader(0x900, 1, wide ? 64 : 32, 1, 0, 0, new byte[12], 2).writeToDataStream(out);
            new IggySubFileEntry(1, main.length, 0, 64).writeToDataStream(out);
            new IggySubFileEntry(0, program.length, 0, 64 + main.length).writeToDataStream(out);
            out.writeBytes(main);
            out.writeBytes(program);
            return out.getAllBytes();
        }
    }

    private static Fixture fixture(int layout) throws Exception {
        Fixture f = new Fixture(layout);
        boolean w = f.wide;
        int p = w ? 8 : 4;
        f.allocate(w ? 184 : layout == 1 ? 128 : 64);
        int[] roots = w ? new int[]{0, 8, 16, 24, 32, 40, 112, 120, 136, 144, 152, 160}
                : layout == 1 ? new int[]{0, 4, 8, 12, 16, 20, 80, 84, 96, 100, 104, 108}
                : new int[]{0, 8, 12, 44, 56, 60};
        for (int at : roots) { f.ref(at, 0); }
        f.u32(w ? 64 : layout == 1 ? 40 : 4, 3);
        f.u32(w ? 68 : layout == 1 ? 44 : 32, 1);
        if (layout != 0) { f.f32(w ? 88 : 64, 24); }
        long dictionary = f.allocate(3 * p);
        f.ref(0, dictionary);
        f.ref(dictionary, 0);
        long sprite = f.allocate(w ? 72 : 36);
        f.u8(sprite, 3); f.u16(sprite + 2, 1); f.ref(sprite + (w ? 16 : 8), 0);
        f.u16(sprite + (w ? 40 : 20), 1); f.u16(sprite + (w ? 42 : 22), 1);
        long spriteShow = f.allocate(w ? 16 : 8);
        f.ref(spriteShow, 0); f.u8(spriteShow + p, 1);
        f.ref(sprite + (w ? 56 : 28), spriteShow); f.ref(sprite + (w ? 64 : 32), spriteShow);
        f.ref(dictionary + p, sprite);
        f.button = f.allocate(w ? 88 : 60);
        f.u8(f.button, 7); f.u16(f.button + 2, 2); f.ref(f.button + (w ? 16 : 8), 0);
        f.u16(f.button + (w ? 68 : 48), 3); f.u8(f.button + (w ? 70 : 50), 1);
        f.u32(f.button + (w ? 80 : 56), 8);
        int stride = w ? 72 : 64;
        f.records = f.allocate(3 * stride);
        f.ref(f.button + (w ? 72 : 52), f.records);
        f.ref(dictionary + 2 * p, f.button);
        if (layout != 0) {
            long symbols = f.allocate(w ? 24 : 12);
            f.ref(w ? 160 : 108, symbols); f.u32(w ? 176 : 120, 1);
            f.ref(symbols, f.button); f.u32(symbols + 2 * p, 2);
            String name = "TestButton";
            long text = f.allocate(2 * (name.length() + 1));
            for (int i = 0; i <= name.length(); i++) { f.u16(text + 2L * i, i == name.length() ? 0 : name.charAt(i)); }
            f.ref(symbols + p, text);
        }
        int[] states = {2, 12, 1};
        int[] depths = {5, 7, 100};
        for (int i = 0; i < 3; i++) {
            long r = f.records + (long) i * stride;
            f.put(r, states[i], 1, 3); f.u8(r + 1, i == 1 ? 7 : 15);
            f.u16(r + 4, 1); f.u16(r + 6, depths[i]);
            f.u8(r + 8, 1); f.u8(r + 9, 1); f.u8(r + 10, 1);
            for (int j = 0; j < 4; j++) { f.f32(r + 12 + 4 * j, j == 0 ? 0.5f : 1); }
            f.u16(r + 28, -12); f.u16(r + 34, 32);
            f.f32(r + 36, 1.25f); f.f32(r + 40, 0.25f);
            f.f32(r + 44, -0.5f); f.f32(r + 48, 2);
            f.f32(r + 52, -3.5f); f.f32(r + 56, 4.25f);
            f.ref(r + (w ? 64 : 60), 0);
        }
        long filter = f.allocate(w ? 56 : 44);
        f.u8(filter, 1); f.u8(filter + 1, 3); f.f32(filter + 4, 2); f.f32(filter + 8, 4);
        f.u8(f.records + stride + 2, 1); f.ref(f.records + stride + (w ? 64 : 60), filter);
        f.rootPlace = f.allocate(w ? 64 : 48);
        f.ref(f.rootPlace, 0); f.u8(f.rootPlace + p, 2);
        f.u8(f.rootPlace + p + 2, 127); f.u8(f.rootPlace + p + 3, 15);
        f.u16(f.rootPlace + (w ? 40 : 36), 1); f.ref(f.rootPlace + (w ? 48 : 40), f.button);
        long show = f.allocate(w ? 16 : 8);
        f.ref(show, 0); f.u8(show + p, 1); f.ref(f.rootPlace, show);
        f.ref(w ? 16 : 8, f.rootPlace); f.ref(w ? 24 : 12, show);
        return f;
    }

    private static IggyFile load(byte[] bytes) throws Exception {
        Path file = Files.createTempFile("iggy-buttons-", ".iggy");
        try { Files.write(file, bytes); return new IggyFile(file.toFile()); }
        finally { Files.delete(file); }
    }

    private static byte[] save(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream bytes = new MemoryDataStream();
        file.writeToDataStream(bytes);
        return bytes.getAllBytes();
    }

    private static SWF reparse(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false, false, "UTF-8", new IggySwfProfile());
    }

    private static DefineButton2Tag button(SWF swf, int id) {
        for (Tag tag : swf.getTags()) {
            if (tag instanceof DefineButton2Tag && ((DefineButton2Tag) tag).buttonId == id) { return (DefineButton2Tag) tag; }
        }
        throw new AssertionError("Missing button " + id);
    }

    @Test(dataProvider = "layouts")
    public void exportsIndependentNativeRecords(int layout) throws Exception {
        SWF swf = reparse(IggyToSwfConvertor.getSwf(load(fixture(layout).file())));
        DefineButton2Tag b = button(swf, 2);
        if (layout != 0) {
            boolean found = false;
            for (Tag tag : swf.getTags()) {
                if (tag instanceof SymbolClassTag && ((SymbolClassTag) tag).tags.contains(2)) {
                    SymbolClassTag symbols = (SymbolClassTag) tag;
                    Assert.assertEquals(symbols.names.get(symbols.tags.indexOf(2)), "TestButton"); found = true;
                }
            }
            Assert.assertTrue(found, "Button SymbolClass must survive export");
        }
        Assert.assertTrue(b.trackAsMenu);
        Assert.assertTrue(b.actions.isEmpty());
        Assert.assertEquals(b.characters.size(), 3);
        BUTTONRECORD up = b.characters.get(0), down = b.characters.get(1), hit = b.characters.get(2);
        Assert.assertTrue(up.buttonStateUp); Assert.assertFalse(up.buttonStateHitTest);
        Assert.assertTrue(down.buttonStateDown && down.buttonStateOver);
        Assert.assertTrue(hit.buttonStateHitTest); Assert.assertFalse(hit.buttonStateUp);
        Assert.assertEquals(up.characterId, 1); Assert.assertEquals(up.placeDepth, 5);
        Assert.assertEquals(up.placeMatrix.scaleX, 1.25f); Assert.assertEquals(up.placeMatrix.rotateSkew1, 0.25f);
        Assert.assertEquals(up.placeMatrix.rotateSkew0, -0.5f); Assert.assertEquals(up.placeMatrix.translateX, -70);
        Assert.assertEquals(up.placeMatrix.translateY, 85);
        Assert.assertEquals(up.colorTransform.getRedMulti(), 128);
        Assert.assertEquals(up.colorTransform.getRedAdd(), -12); Assert.assertEquals(up.colorTransform.getAlphaAdd(), 32);
        Assert.assertTrue(down.buttonHasBlendMode); Assert.assertEquals(down.blendMode, 8);
        Assert.assertFalse(up.buttonHasBlendMode); Assert.assertTrue(down.buttonHasFilterList);
        BLURFILTER blur = (BLURFILTER) down.filterList.get(0);
        Assert.assertEquals(blur.blurX, 2.0); Assert.assertEquals(blur.blurY, 4.0); Assert.assertEquals(blur.passes, 3);
    }

    @Test(dataProvider = "layouts")
    public void unchangedSwfPreservesOriginalGraph(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        byte[] original = file.getSwf().getOriginalData();
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(IggyToSwfConvertor.getSwf(file)));
        Assert.assertEquals(load(save(file)).getSwf().getOriginalData(), original);
    }

    @Test(dataProvider = "layouts")
    public void editsRecordsAndRelocatesPlacements(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        DefineButton2Tag b = button(swf, 2);
        b.trackAsMenu = false;
        b.characters.get(0).placeMatrix.translateX = 255;
        b.characters.get(0).colorTransform.alphaMultTerm = 64;
        b.characters.get(0).colorTransform.hasMultTerms = true;
        b.characters.get(1).blendMode = 7;
        b.characters.get(1).filterList.clear();
        b.setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        IggyFile loaded = load(save(file));
        IggyTimeline source = loaded.getSwf().getTimeline();
        long at = loaded.getSwf().getDictionary().get(2).getAddress();
        if (layout != 0) { Assert.assertEquals(loaded.getSwf().getSymbolClasses().get(0).getObjectAddress(), at); }
        ByteBuffer header = source.bytes(at, layout == 2 ? 88 : 60);
        Assert.assertEquals(header.getInt(layout == 2 ? 80 : 56), 8, "HitTest depth is excluded from display list capacity");
        long place = source.getCommands().get(0).getAddress();
        Assert.assertEquals(source.reference(place + (layout == 2 ? 48 : 40)), at);
        DefineButton2Tag actual = button(reparse(IggyToSwfConvertor.getSwf(loaded)), 2);
        Assert.assertFalse(actual.trackAsMenu);
        Assert.assertEquals(actual.characters.get(0).placeMatrix.translateX, 255);
        Assert.assertEquals(actual.characters.get(0).colorTransform.getAlphaMulti(), 64);
        Assert.assertEquals(actual.characters.get(1).blendMode, 7);
        Assert.assertFalse(actual.characters.get(1).buttonHasFilterList);
    }

    @Test(dataProvider = "layouts")
    public void importsLegacyButtonAndCxform(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        DefineButtonTag legacy = new DefineButtonTag(swf);
        legacy.buttonId = 10; legacy.actionBytes = new ByteArrayRange(new byte[]{0});
        BUTTONRECORD record = new BUTTONRECORD(swf, legacy);
        record.buttonStateUp = true; record.buttonStateHitTest = true;
        record.characterId = 1; record.placeDepth = 3; record.placeMatrix = new MATRIX();
        legacy.characters.add(record); swf.addTag(1, legacy);
        DefineButtonCxformTag cx = new DefineButtonCxformTag(swf);
        cx.buttonId = 10; cx.buttonColorTransform = new CXFORM();
        cx.buttonColorTransform.hasMultTerms = true;
        cx.buttonColorTransform.redMultTerm = 128; cx.buttonColorTransform.greenMultTerm = 256;
        cx.buttonColorTransform.blueMultTerm = 64; cx.buttonColorTransform.hasAddTerms = true;
        cx.buttonColorTransform.redAddTerm = 15;
        swf.addTag(2, cx);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        IggyFile loaded = load(save(file));
        Assert.assertEquals(loaded.getSwf().getDictionary().get(10).getType(), 7);
        DefineButton2Tag actual = button(reparse(IggyToSwfConvertor.getSwf(loaded)), 10);
        Assert.assertTrue(actual.characters.get(0).buttonStateHitTest);
        Assert.assertEquals(actual.characters.get(0).colorTransform.getRedMulti(), 128);
        Assert.assertEquals(actual.characters.get(0).colorTransform.getRedAdd(), 15);
        Assert.assertEquals(actual.characters.get(0).colorTransform.getAlphaMulti(), 256);
        Assert.assertFalse(actual.trackAsMenu);
        byte[] original = loaded.getSwf().getOriginalData();
        SwfToIggyConvertor.updateIggy(loaded.getSwf(), reparse(IggyToSwfConvertor.getSwf(loaded)));
        Assert.assertEquals(load(save(loaded)).getSwf().getOriginalData(), original);
    }

    @Test(dataProvider = "layouts")
    public void importsNewEmptyButton(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        DefineButton2Tag empty = new DefineButton2Tag(swf); empty.buttonId = 12; swf.addTag(1, empty);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        Assert.assertTrue(button(IggyToSwfConvertor.getSwf(load(save(file))), 12).characters.isEmpty());
    }

    @Test(dataProvider = "layouts")
    public void importsNewRichButtonWithNestedFilterRelocations(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = reparse(IggyToSwfConvertor.getSwf(file));
        DefineButton2Tag created = new DefineButton2Tag(swf); created.buttonId = 11; created.trackAsMenu = true;
        BUTTONRECORD record = new BUTTONRECORD(swf, created);
        record.buttonStateUp = record.buttonStateOver = record.buttonStateDown = record.buttonStateHitTest = true;
        record.characterId = 1; record.placeDepth = 65535; record.placeMatrix = new MATRIX();
        record.colorTransform = new CXFORMWITHALPHA();
        record.buttonHasBlendMode = true; record.blendMode = 9;
        record.buttonHasFilterList = true; record.filterList = new ArrayList<>();
        GRADIENTGLOWFILTER glow = new GRADIENTGLOWFILTER();
        glow.gradientColors[0].red = 17; glow.gradientRatio[1] = 230;
        record.filterList.add(glow);
        COLORMATRIXFILTER matrix = new COLORMATRIXFILTER(); matrix.matrix[4] = 0.25f;
        record.filterList.add(matrix); created.characters.add(record); created.setModified(true); swf.addTag(1, created);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        IggyFile loaded = load(save(file));
        DefineButton2Tag actual = button(reparse(IggyToSwfConvertor.getSwf(loaded)), 11);
        BUTTONRECORD result = actual.characters.get(0);
        Assert.assertTrue(actual.trackAsMenu);
        Assert.assertTrue(result.buttonStateUp && result.buttonStateOver && result.buttonStateDown && result.buttonStateHitTest);
        Assert.assertEquals(result.placeDepth, 65535); Assert.assertEquals(result.blendMode, 9);
        Assert.assertEquals(result.filterList.size(), 2);
        Assert.assertEquals(((GRADIENTGLOWFILTER) result.filterList.get(0)).gradientColors[0].red, 17);
        Assert.assertEquals(((GRADIENTGLOWFILTER) result.filterList.get(0)).gradientRatio[1], 230);
        Assert.assertEquals(((COLORMATRIXFILTER) result.filterList.get(1)).matrix[4], 0.25f);
        long at = loaded.getSwf().getDictionary().get(11).getAddress();
        Assert.assertEquals(loaded.getSwf().getTimeline().bytes(at + (layout == 2 ? 80 : 56), 4).getInt(), 65536);
    }

    @Test(dataProvider = "layouts")
    public void rejectsMissingCharacterReferences(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = IggyToSwfConvertor.getSwf(file);
        button(swf, 2).characters.get(0).characterId = 500;
        rejects(() -> SwfToIggyConvertor.updateIggy(file.getSwf(), swf), "Missing Iggy button character 500");
    }

    @Test(dataProvider = "layouts")
    public void resolvesImportedNumericCharacterIds(int layout) throws Exception {
        if (layout == 0) { return; }
        IggyFile file = load(fixture(layout).file());
        SWF swf = IggyToSwfConvertor.getSwf(file);
        ImportAssets2Tag imported = new ImportAssets2Tag(swf);
        imported.url = "buttons.swf"; imported.tags.add(500); imported.names.add("importedPicture"); swf.addTag(1, imported);
        button(swf, 2).characters.get(0).characterId = 500; button(swf, 2).setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(), reparse(swf));
        IggyFile loaded = load(save(file));
        Assert.assertEquals(button(IggyToSwfConvertor.getSwf(loaded), 2).characters.get(0).characterId, 500);
    }

    private static void rejects(RunnableIO operation, String message) throws Exception {
        try { operation.run(); Assert.fail("Expected " + message); }
        catch (IOException ex) { Assert.assertTrue(ex.getMessage().contains(message), ex.getMessage()); }
    }

    private interface RunnableIO { void run() throws Exception; }

    @Test(dataProvider = "layouts")
    public void rejectsAvm1Actions(int layout) throws Exception {
        SWF swf = new SWF();
        DefineButtonTag legacy = new DefineButtonTag(swf); legacy.buttonId = 5;
        legacy.actionBytes = new ByteArrayRange(new byte[]{7, 0});
        rejects(() -> IggyButtonConvertor.fromSwf(swf, layout == 2, legacy), "AVM1 button actions");
        DefineButton2Tag modern = new DefineButton2Tag(swf); modern.buttonId = 6;
        BUTTONCONDACTION action = new BUTTONCONDACTION(swf, modern);
        action.condOverUpToOverDown = true; action.actionBytes = legacy.actionBytes; modern.actions.add(action);
        rejects(() -> IggyButtonConvertor.fromSwf(swf, layout == 2, modern), "AVM1 button actions");
    }

    @Test(dataProvider = "layouts")
    public void rejectsMalformedNativeRecords(int layout) throws Exception {
        Fixture f = fixture(layout);
        f.u16(f.button + (f.wide ? 68 : 48), 65535);
        IggyFile negative = load(f.file());
        rejects(() -> IggyButtonConvertor.toSwf(new SWF(), negative.getSwf().getTimeline(), f.button, 2), "record count");
        Fixture noRecords = fixture(layout);
        noRecords.ref(noRecords.button + (noRecords.wide ? 72 : 52), 0);
        IggyFile missing = load(noRecords.file());
        rejects(() -> IggyButtonConvertor.toSwf(new SWF(), missing.getSwf().getTimeline(), noRecords.button, 2), "Missing Iggy button records");
        Fixture states = fixture(layout); states.u8(states.records, 16);
        IggyFile badStates = load(states.file());
        rejects(() -> IggyButtonConvertor.toSwf(new SWF(), badStates.getSwf().getTimeline(), states.button, 2), "button states");
    }

    @Test(dataProvider = "layouts")
    public void rejectsButtonSoundInsteadOfDroppingIt(int layout) throws Exception {
        IggyFile file = load(fixture(layout).file());
        SWF swf = IggyToSwfConvertor.getSwf(file);
        swf.addTag(new DefineButtonSoundTag(swf));
        rejects(() -> SwfToIggyConvertor.updateIggy(file.getSwf(), swf), "DefineButtonSound");
    }
}
