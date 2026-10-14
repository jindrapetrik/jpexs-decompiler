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
import com.jpexs.decompiler.flash.iggy.conversion.IggyVectorConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyNativeSaveTest {
    @DataProvider(name = "widths")
    public Object[][] widths() {
        return new Object[][] {{false}, {true}};
    }

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][] {{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    private static List<byte[]> compact(boolean wide, List<IggyFixup.Field> fields, long size) throws Exception {
        IggyIndexBuilder builder = new IggyIndexBuilder();
        IggyFixup.writeFields(builder, fields, size);
        List<byte[]> result = IggyFixup.compact(wide, builder.getIndexBytes(), size);
        int next = 0;
        for (byte[] program : result) {
            Assert.assertTrue(program.length <= IggyFixup.NATIVE_BUFFER_SIZE);
            for (IggyFixup.Field field : IggyFixup.parse(wide, program, size).getFields()) {
                Assert.assertEquals(field.offset, fields.get(next).offset);
                Assert.assertEquals(field.operation, fields.get(next++).operation);
            }
        }
        Assert.assertEquals(next, fields.size());
        return result;
    }

    @Test(dataProvider = "widths")
    public void repeatedPointerAndContextWrites(boolean wide) throws Exception {
        List<IggyFixup.Field> fields = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            fields.add(new IggyFixup.Field(i * 16L, 2));
        }
        fields.add(new IggyFixup.Field(3200000, 2));
        for (int operation = 8; operation <= 15; operation++) {
            fields.add(new IggyFixup.Field(3200000, operation));
        }
        List<byte[]> result = compact(wide, fields, 3200008);
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.get(0).length < 7000);
    }

    @Test(dataProvider = "widths")
    public void partitionsIrregularFieldsWithoutLosingScalarSwaps(boolean wide) throws Exception {
        Random random = new Random(15256);
        int[] operations = {2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15};
        List<IggyFixup.Field> fields = new ArrayList<>();
        long at = 0;
        for (int i = 0; i < 100000; i++) {
            at += random.nextInt(400) + 1;
            fields.add(new IggyFixup.Field(at, operations[random.nextInt(operations.length)]));
        }
        Assert.assertTrue(compact(wide, fields, at + 8).size() > 1);
    }

    @Test(dataProvider = "widths")
    public void partitionRestartsAtTargetBaseAndRetainsTemplateOrder(boolean wide) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // A template deliberately lists a later field before an earlier field.
        for (int b : new int[] {1, 16, 2, 8, 8, 0, 2}) {
            bytes.write(b);
        }
        for (int i = 0; i < 70000; i++) {
            bytes.write(0);
        }
        List<byte[]> chunks = IggyFixup.compact(wide, bytes.toByteArray(), 1120000);
        Assert.assertEquals(chunks.size(), 2);
        int next = 0;
        for (byte[] chunk : chunks) {
            Assert.assertTrue(chunk.length <= IggyFixup.NATIVE_BUFFER_SIZE);
            for (IggyFixup.Field field : IggyFixup.parse(wide, chunk, 1120000).getFields()) {
                Assert.assertEquals(field.offset, (next / 2) * 16L + (next % 2 == 0 ? 8 : 0));
                Assert.assertEquals(field.operation, next++ % 2 == 0 ? 8 : 2);
            }
        }
        Assert.assertEquals(next, 140000);
    }

    @Test(dataProvider = "widths")
    public void skipBoundariesAndEmptyProgram(boolean wide) throws Exception {
        for (int distance : new int[] {1, 2, 31, 32, 33, 255, 256, 257, 1000000}) {
            List<IggyFixup.Field> fields = new ArrayList<>();
            fields.add(new IggyFixup.Field(distance, 2));
            compact(wide, fields, distance + 8L);
        }
        compact(wide, new ArrayList<>(), 0);
    }

    private static DefineShape3Tag shape(SWF swf, int styles, int edges) {
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeId = 1;
        shape.shapeBounds = new RECT(0, 800, 0, 800);
        if ((styles & 1) != 0) {
            FILLSTYLE fill = new FILLSTYLE();
            fill.color = new RGBA(255, 0, 0, 255);
            shape.shapes.fillStyles.fillStyles = new FILLSTYLE[] {fill};
        }
        if ((styles & 2) != 0) {
            LINESTYLE line = new LINESTYLE();
            line.width = 20;
            line.color = new RGBA(0, 0, 255, 255);
            shape.shapes.lineStyles.lineStyles = new LINESTYLE[] {line};
        }
        StyleChangeRecord start = new StyleChangeRecord();
        start.stateMoveTo = true;
        start.stateFillStyle1 = (styles & 1) != 0;
        start.fillStyle1 = start.stateFillStyle1 ? 1 : 0;
        start.stateLineStyle = (styles & 2) != 0;
        start.lineStyle = start.stateLineStyle ? 1 : 0;
        shape.shapes.shapeRecords.add(start);
        int[][] deltas = {{800, 0}, {0, 800}, {-800, 0}, {0, -800}};
        for (int i = 0; i < edges; i++) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.deltaX = deltas[i % 4][0];
            edge.deltaY = deltas[i % 4][1];
            edge.generalLineFlag = true;
            edge.calculateBits();
            shape.shapes.shapeRecords.add(edge);
        }
        shape.shapes.shapeRecords.add(new EndShapeRecord());
        return shape;
    }

    private static long reference(ByteBuffer data, long field, boolean wide) {
        long relative = wide ? data.getLong((int) field) : data.getInt((int) field);
        Assert.assertNotEquals(relative, 1L, "Native style table must not be null");
        return field + relative;
    }

    private static void tables(IggyObjectData object, boolean wide, long collection, boolean end, int fills, int lines) {
        ByteBuffer data = ByteBuffer.wrap(object.data).order(ByteOrder.LITTLE_ENDIAN);
        long groups = reference(data, collection + (end ? wide ? 8 : 4 : wide ? 24 : 12), wide);
        long fillTable = reference(data, groups + (wide ? 32 : 24), wide);
        long lineTable = reference(data, groups + (wide ? 40 : 28), wide);
        Assert.assertEquals(data.getInt((int) fillTable), fills);
        Assert.assertEquals(data.getInt((int) lineTable), lines);
        for (long field : new long[] {groups + (wide ? 32 : 24), groups + (wide ? 40 : 28)}) {
            Assert.assertTrue(object.fields.stream().anyMatch(f -> f.offset == field && f.operation == 2));
        }
        Assert.assertTrue(object.fields.stream().anyMatch(f -> f.offset == fillTable && f.operation == 5));
        Assert.assertTrue(object.fields.stream().anyMatch(f -> f.offset == lineTable && f.operation == 5));
    }

    @Test(dataProvider = "widths")
    public void ordinaryShapesAlwaysHaveStyleTables(boolean wide) throws Exception {
        SWF swf = new SWF();
        for (int styles = 0; styles < 4; styles++) {
            IggyObjectData object = IggyVectorConvertor.fromSwf(swf, wide, shape(swf, styles, 4));
            tables(object, wide, object.address + (wide ? 48 : 32), false, styles & 1, (styles & 2) / 2);
        }
    }

    @Test(dataProvider = "widths")
    public void morphEndpointsHaveEmptyStyleTables(boolean wide) throws Exception {
        SWF swf = new SWF();
        DefineMorphShape2Tag morph = new DefineMorphShape2Tag(swf);
        morph.characterId = 1;
        morph.startBounds = morph.endBounds = new RECT(0, 800, 0, 800);
        morph.startEdges = shape(swf, 0, 4).shapes;
        morph.endEdges = shape(swf, 0, 4).shapes;
        IggyObjectData object = IggyVectorConvertor.fromSwf(swf, wide, morph);
        tables(object, wide, object.address + (wide ? 48 : 32), false, 0, 0);
        tables(object, wide, object.address + (wide ? 80 : 48), true, 0, 0);
    }

    private static byte[] bytes(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream stream = new MemoryDataStream();
        file.writeToDataStream(stream);
        return stream.getAllBytes();
    }

    @Test(dataProvider = "variants")
    public void emptyContainerWithLongNameAlsoUsesNativeSizedPrograms(IggyFile.Variant variant) throws Exception {
        char[] chars = new char[40000];
        Arrays.fill(chars, 'x');
        String name = new String(chars) + ".swf";
        IggyFile file = IggyFile.create(variant, name);
        Assert.assertTrue(mainPrograms(file) >= 1);
        IggyFile reloaded = new IggyFile(new MemoryDataStream(bytes(file)));
        Assert.assertEquals(reloaded.getSwfName(), name);
    }

    private static int mainPrograms(IggyFile file) {
        int count = 0;
        for (int i = 0; i < file.getNumEntries(); i++) {
            IggySubFileEntry entry = file.getSubFileEntry(i);
            if (entry.getType() == 0 && entry.getTargetIndex() == 0) {
                Assert.assertTrue(entry.getSize() <= IggyFixup.NATIVE_BUFFER_SIZE);
                count++;
            }
        }
        return count;
    }

    @Test(dataProvider = "variants")
    public void largeCreatedAndEditedContainersUseBoundedPrograms(IggyFile.Variant variant) throws Exception {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 24;
        swf.frameCount = 1;
        swf.displayRect = new RECT(0, 1000, 0, 1000);
        swf.addTag(shape(swf, 2, 24000));
        swf.addTag(new ShowFrameTag(swf));
        IggyFile file = SwfToIggyConvertor.createIggy(swf, variant, "large.swf");
        Assert.assertTrue(mainPrograms(file) > 1);
        byte[] original = bytes(file);
        file = new IggyFile(new MemoryDataStream(original));
        Assert.assertEquals(bytes(file), original, "Unchanged containers retain their bytes");
        for (int i = 0; i < file.getNumEntries(); i++) {
            IggySubFileEntry entry = file.getSubFileEntry(i);
            if (entry.getType() == 0) {
                entry.copyFlag = 1;
                entry.metadata = 7;
                entry.size2 = 42;
            }
        }
        SWF edited = IggyToSwfConvertor.getSwf(file);
        ShapeTag shape = (ShapeTag) edited.getCharacter(1);
        shape.shapeBounds.Xmax += 20;
        shape.setModified(true);
        SwfToIggyConvertor.updateIggy(file.getSwf(), edited);
        byte[] saved = bytes(file);
        IggyFile reloaded = new IggyFile(new MemoryDataStream(saved));
        Assert.assertTrue(mainPrograms(reloaded) > 1);
        for (int i = 0; i < reloaded.getNumEntries(); i++) {
            IggySubFileEntry entry = reloaded.getSubFileEntry(i);
            if (entry.getType() == 0) {
                Assert.assertEquals(entry.getCopyFlag(), 1);
                Assert.assertEquals(entry.getMetadata(), 7);
                Assert.assertEquals(entry.size2, 42L);
            }
        }
        Assert.assertEquals(IggyToSwfConvertor.getSwf(reloaded).getCharacter(1).getCharacterId(), 1);
        Assert.assertEquals(bytes(reloaded), saved);
    }
}
