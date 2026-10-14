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
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.SerializableImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyVectorConversionTest {

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    // Each side is a separate subpath. All four must participate in the same fill.
    private static void side(SHAPE shape, int x, int y, int dx, int dy, int scale) {
        StyleChangeRecord move = new StyleChangeRecord();
        move.stateMoveTo = true;
        move.moveDeltaX = x * scale;
        move.moveDeltaY = y * scale;
        move.calculateBits();
        shape.shapeRecords.add(move);
        StraightEdgeRecord edge = new StraightEdgeRecord();
        edge.deltaX = dx * scale;
        edge.deltaY = dy * scale;
        edge.generalLineFlag = true;
        edge.simplify();
        edge.calculateBits();
        shape.shapeRecords.add(edge);
    }

    private static SHAPEWITHSTYLE geometry(int scale) {
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.shapeRecords = new ArrayList<>();
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = FILLSTYLE.SOLID;
        fill.color = new RGBA(255, 0, 0, 255);
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.lineStyles = new LINESTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[]{fill};
        shape.lineStyles.lineStyles2 = new LINESTYLE2[0];
        shape.numFillBits = 1;
        side(shape, 0, 0, 1600, 0, scale);
        StyleChangeRecord first = (StyleChangeRecord) shape.shapeRecords.get(0);
        first.stateFillStyle1 = true;
        first.fillStyle1 = 1;
        // Deliberately interleave the two contours instead of emitting closed paths.
        side(shape, 400, 400, 800, 0, scale);
        side(shape, 1600, 1200, -1600, 0, scale);
        side(shape, 1200, 400, 0, 400, scale);
        side(shape, 1600, 0, 0, 1200, scale);
        side(shape, 1200, 800, -800, 0, scale);
        side(shape, 0, 1200, 0, -1200, scale);
        side(shape, 400, 800, 0, -400, scale);
        shape.shapeRecords.add(new EndShapeRecord());
        return shape;
    }

    private static SWF movie() {
        SWF swf = new SWF();
        swf.version = 11;
        swf.frameRate = 24;
        swf.frameCount = 1;
        return swf;
    }

    private static SWF reparse(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false, "UTF-8");
    }

    private static SWF convert(SWF source, IggyFile.Variant variant) throws Exception {
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "contours.swf");
        MemoryDataStream bytes = new MemoryDataStream();
        file.writeToDataStream(bytes);
        return reparse(IggyToSwfConvertor.getSwf(new IggyFile(new MemoryDataStream(bytes.getAllBytes()))));
    }

    private static int[] render(ShapeTag shape) {
        SerializableImage image = new SerializableImage(180, 140, BufferedImage.TYPE_INT_ARGB);
        Matrix transform = Matrix.getTranslateInstance(80, 80);
        BitmapExporter.export(shape.getWindingRule(), shape.getShapeNum(), shape.getSwf(), shape.getShapes(),
                null, image, 1, transform, transform, null, true, true, 1);
        return image.getBufferedImage().getRGB(0, 0, 180, 140, null, 0, 180);
    }

    @Test(dataProvider = "variants")
    public void preservesFilledContoursAcrossPenMoves(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineShape4Tag shape = new DefineShape4Tag(source);
        shape.shapeId = 97;
        shape.shapeBounds = shape.edgeBounds = new RECT(0, 1600, 0, 1200);
        shape.shapes = geometry(1);
        source.addTag(shape);
        source.addTag(new ShowFrameTag(source));
        source = reparse(source);
        int[] expected = render((ShapeTag) source.getCharacter(97));
        Assert.assertEquals(expected[14 * 180 + 14], 0xffff0000, "Filled outer contour");
        Assert.assertEquals(expected[34 * 180 + 34], 0, "Unfilled inner contour");
        SWF result = convert(source, variant);
        Assert.assertEquals(render((ShapeTag) result.getCharacter(97)), expected, "All rendered pixels must match");
    }

    @Test(dataProvider = "variants")
    public void preservesMorphContoursAcrossPenMoves(IggyFile.Variant variant) throws Exception {
        SWF source = movie();
        DefineMorphShape2Tag morph = new DefineMorphShape2Tag(source);
        morph.characterId = 97;
        morph.startBounds = morph.startEdgeBounds = new RECT(0, 1600, 0, 1200);
        morph.endBounds = morph.endEdgeBounds = new RECT(0, 3200, 0, 2400);
        morph.startEdges = geometry(1);
        morph.endEdges = geometry(2);
        ((StyleChangeRecord) morph.endEdges.shapeRecords.get(0)).stateFillStyle1 = false;
        MORPHFILLSTYLE fill = new MORPHFILLSTYLE();
        fill.fillStyleType = FILLSTYLE.SOLID;
        fill.startColor = new RGBA(255, 0, 0, 255);
        fill.endColor = new RGBA(0, 255, 0, 255);
        morph.morphFillStyles.fillStyles = new MORPHFILLSTYLE[]{fill};
        morph.morphLineStyles.lineStyles2 = new MORPHLINESTYLE2[0];
        source.addTag(morph);
        source.addTag(new ShowFrameTag(source));
        source = reparse(source);
        DefineMorphShape2Tag expected = (DefineMorphShape2Tag) source.getCharacter(97);
        DefineMorphShape2Tag result = (DefineMorphShape2Tag) convert(source, variant).getCharacter(97);
        for (int ratio : new int[]{0, 32768, 65535}) {
            Assert.assertEquals(render(result.getShapeTagAtRatio(ratio)), render(expected.getShapeTagAtRatio(ratio)),
                    "Morph pixels at ratio " + ratio);
        }
    }
}
