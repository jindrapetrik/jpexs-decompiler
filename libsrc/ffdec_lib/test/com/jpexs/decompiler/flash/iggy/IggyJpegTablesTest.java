/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyJpegTablesTest {

    private static final class Jpeg {
        byte[] tables;
        byte[] image;
        byte[] standalone;
    }

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    @DataProvider(name = "broken")
    public Object[][] broken() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (String defect : new String[]{"missing", "empty", "tables", "image"}) {
                result.add(new Object[]{variant, defect});
            }
        }
        return result.toArray(new Object[0][]);
    }

    private static Jpeg jpeg(int width, int height) throws IOException {
        BufferedImage pixels = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels.setRGB(x, y, ((x * 17) & 255) << 16 | ((y * 23) & 255) << 8 | ((x * 11 + y * 7) & 255));
            }
        }
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        Assert.assertTrue(ImageIO.write(pixels, "jpeg", encoded));
        byte[] full = encoded.toByteArray();
        ByteArrayOutputStream tables = new ByteArrayOutputStream();
        ByteArrayOutputStream image = new ByteArrayOutputStream();
        tables.write(new byte[]{(byte) 255, (byte) 216});
        image.write(new byte[]{(byte) 255, (byte) 216});
        int at = 2;
        while (at < full.length) {
            Assert.assertEquals(full[at] & 255, 255);
            int marker = full[at + 1] & 255;
            if (marker == 0xda) {
                // Preserve the scan header, entropy-coded bytes and EOI verbatim.
                image.write(full, at, full.length - at);
                break;
            }
            int size = 2 + ((full[at + 2] & 255) << 8 | full[at + 3] & 255);
            (marker == 0xdb || marker == 0xc4 ? tables : image).write(full, at, size);
            at += size;
        }
        // Assemble the independent expected output without invoking JpegFixer.
        ByteArrayOutputStream standalone = new ByteArrayOutputStream();
        standalone.write(tables.toByteArray());
        standalone.write(image.toByteArray(), 2, image.size() - 2);
        tables.write(new byte[]{(byte) 255, (byte) 217});
        Jpeg result = new Jpeg();
        result.tables = tables.toByteArray();
        result.image = image.toByteArray();
        result.standalone = standalone.toByteArray();
        return result;
    }

    private static PlaceObject2Tag place(SWF swf, int id) {
        return new PlaceObject2Tag(swf, false, 1, id, new MATRIX(), null, -1, null, -1, null);
    }

    private static DefineShape3Tag shape(SWF swf, int id, int bitmap) {
        DefineShape3Tag tag = new DefineShape3Tag(swf);
        tag.shapeId = id;
        tag.shapeBounds = new RECT(0, 320, 0, 240);
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = 0x41;
        fill.bitmapId = bitmap;
        fill.bitmapMatrix = new MATRIX();
        fill.bitmapMatrix.hasScale = true;
        fill.bitmapMatrix.scaleX = 20;
        fill.bitmapMatrix.scaleY = 20;
        tag.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill};
        tag.shapes.shapeRecords.clear();
        StyleChangeRecord style = new StyleChangeRecord();
        style.stateMoveTo = true;
        style.stateFillStyle1 = true;
        style.fillStyle1 = 1;
        tag.shapes.shapeRecords.add(style);
        for (int[] delta : new int[][]{{320, 0}, {0, 240}, {-320, 0}, {0, -240}}) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = delta[0];
            edge.deltaY = delta[1];
            edge.calculateBits();
            tag.shapes.shapeRecords.add(edge);
        }
        tag.shapes.shapeRecords.add(new EndShapeRecord());
        tag.shapes.numFillBits = 1;
        return tag;
    }

    private static SWF movie(boolean duplicateSoi) throws Exception {
        Jpeg first = jpeg(16, 12);
        Jpeg second = jpeg(8, 10);
        Assert.assertEquals(first.tables, second.tables, "Images must actually share the same tables");
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 24;
        swf.frameCount = 1;
        swf.displayRect = new RECT(0, 640, 0, 480);
        JPEGTablesTag tables = new JPEGTablesTag(swf);
        tables.jpegData = new ByteArrayRange(first.tables);
        swf.addTag(tables);
        for (int id : new int[]{3, 7}) {
            byte[] data = id == 3 ? first.image : second.image;
            if (duplicateSoi) {
                byte[] duplicate = new byte[data.length + 2];
                duplicate[0] = (byte) 255;
                duplicate[1] = (byte) 216;
                System.arraycopy(data, 0, duplicate, 2, data.length);
                data = duplicate;
            }
            DefineBitsTag bits = new DefineBitsTag(swf);
            bits.characterID = id;
            bits.jpegData = new ByteArrayRange(data);
            swf.addTag(bits);
        }
        swf.addTag(shape(swf, 8, 3));
        swf.addTag(shape(swf, 10, 7));
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.spriteId = 9;
        sprite.frameCount = 1;
        sprite.addTag(place(swf, 10));
        sprite.addTag(new ShowFrameTag(swf));
        sprite.addTag(new EndTag(swf));
        swf.addTag(sprite);
        swf.addTag(place(swf, 8));
        PlaceObject2Tag spritePlace = place(swf, 9);
        spritePlace.depth = 2;
        spritePlace.matrix.translateX = 320;
        swf.addTag(spritePlace);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new EndTag(swf));
        return reparse(swf);
    }

    private static byte[] bytes(SWF swf) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return out.toByteArray();
    }

    private static SWF reparse(SWF swf) throws Exception {
        return new SWF(new ByteArrayInputStream(bytes(swf)), false, "UTF-8");
    }

    private static IggyFile reload(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return new IggyFile(new MemoryDataStream(out.getAllBytes()));
    }

    private static ImageTag image(SWF swf, int id) {
        return (ImageTag) swf.getCharacter(id);
    }

    private static void verify(SWF swf) throws Exception {
        for (int id : new int[]{3, 7}) {
            Jpeg expected = id == 3 ? jpeg(16, 12) : jpeg(8, 10);
            Assert.assertTrue(image(swf, id) instanceof DefineBitsJPEG2Tag);
            DefineBitsJPEG2Tag actual = (DefineBitsJPEG2Tag) image(swf, id);
            Assert.assertEquals(actual.imageData.getRangeData(), expected.standalone, "No JPEG recompression");
            BufferedImage reference = ImageIO.read(new ByteArrayInputStream(expected.standalone));
            BufferedImage decoded = ImageIO.read(actual.getOriginalImageData());
            Assert.assertEquals(decoded.getWidth(), reference.getWidth());
            Assert.assertEquals(decoded.getHeight(), reference.getHeight());
            Assert.assertEquals(decoded.getRGB(0, 0, decoded.getWidth(), decoded.getHeight(), null, 0, decoded.getWidth()),
                    reference.getRGB(0, 0, reference.getWidth(), reference.getHeight(), null, 0, reference.getWidth()));
        }
        for (Tag tag : swf.getTags()) {
            Assert.assertFalse(tag instanceof JPEGTablesTag || tag instanceof DefineBitsTag);
        }
        Assert.assertEquals(((ShapeTag) swf.getCharacter(8))
                .getShapes().fillStyles.fillStyles[0].bitmapId, 3);
        Assert.assertEquals(((ShapeTag) swf.getCharacter(10))
                .getShapes().fillStyles.fillStyles[0].bitmapId, 7);
        DefineSpriteTag sprite = (DefineSpriteTag) swf.getCharacter(9);
        Assert.assertEquals(((PlaceObjectTypeTag) sprite.getTags().get(0)).getCharacterId(), 10);
    }

    @Test(dataProvider = "variants")
    public void sharedTablesRoundTrip(IggyFile.Variant variant) throws Exception {
        for (boolean duplicateSoi : new boolean[]{false, true}) {
            SWF source = movie(duplicateSoi);
            byte[] before = bytes(source);
            IggyFile file = reload(SwfToIggyConvertor.createIggy(source, variant, "shared-jpeg.swf"));
            verify(reparse(IggyToSwfConvertor.getSwf(file)));
            Assert.assertEquals(bytes(source), before, "Conversion must leave the source SWF intact");
        }
    }

    @Test(dataProvider = "variants")
    public void tolerantConversionKeepsImagesWithoutWarnings(IggyFile.Variant variant) throws Exception {
        SWF source = movie(false);
        byte[] before = bytes(source);
        List<String> warnings = new ArrayList<>();
        IggyFile file = reload(SwfToIggyConvertor.createIggy(source, variant, "shared-jpeg.swf", warnings::add));
        Assert.assertTrue(warnings.isEmpty(), warnings.toString());
        verify(reparse(IggyToSwfConvertor.getSwf(file)));
        Assert.assertEquals(bytes(source), before);
    }

    @Test(dataProvider = "variants")
    public void updateExistingBitmap(IggyFile.Variant variant) throws Exception {
        SWF initial = movie(false);
        ((DefineBitsTag) image(initial, 3)).jpegData = new ByteArrayRange(jpeg(8, 10).image);
        image(initial, 3).setModified(true);
        IggyFile file = SwfToIggyConvertor.createIggy(reparse(initial), variant, "update-jpeg.swf");
        SWF replacement = movie(true);
        byte[] before = bytes(replacement);
        SwfToIggyConvertor.updateIggy(file.getSwf(), replacement);
        verify(reparse(IggyToSwfConvertor.getSwf(reload(file))));
        Assert.assertEquals(bytes(replacement), before);
    }

    @Test(dataProvider = "variants")
    public void standaloneImageWithEmptyTables(IggyFile.Variant variant) throws Exception {
        SWF source = movie(false);
        source.getJtt().jpegData = ByteArrayRange.EMPTY;
        source.getJtt().setModified(true);
        ((DefineBitsTag) image(source, 3)).jpegData = new ByteArrayRange(jpeg(16, 12).standalone);
        ((DefineBitsTag) image(source, 7)).jpegData = new ByteArrayRange(jpeg(8, 10).standalone);
        image(source, 3).setModified(true);
        image(source, 7).setModified(true);
        verify(reparse(IggyToSwfConvertor.getSwf(reload(SwfToIggyConvertor.createIggy(reparse(source), variant, "empty-tables.swf")))));
    }

    private static SWF brokenMovie(String defect) throws Exception {
        SWF source = movie(false);
        if ("missing".equals(defect)) {
            source.removeTag(source.getJtt());
        } else if ("empty".equals(defect)) {
            source.getJtt().jpegData = ByteArrayRange.EMPTY;
            source.getJtt().setModified(true);
        } else if ("tables".equals(defect)) {
            source.getJtt().jpegData = new ByteArrayRange(new byte[]{(byte) 255, (byte) 216, (byte) 255, (byte) 219, 0, 3, 0, (byte) 255, (byte) 217});
            source.getJtt().setModified(true);
        } else {
            for (int id : new int[]{3, 7}) {
                ((DefineBitsTag) image(source, id)).jpegData = new ByteArrayRange(new byte[]{(byte) 255, (byte) 216, (byte) 255, (byte) 217});
                image(source, id).setModified(true);
            }
        }
        return reparse(source);
    }

    @Test(dataProvider = "broken")
    public void strictRejectsMissingOrBrokenData(IggyFile.Variant variant, String defect) throws Exception {
        try {
            SwfToIggyConvertor.createIggy(brokenMovie(defect), variant, "broken-jpeg.swf");
            Assert.fail("Invalid JPEG must not become an error placeholder");
        } catch (IOException ex) {
            Assert.assertTrue(ex.getMessage().contains("DefineBits"), ex.getMessage());
            Assert.assertTrue(ex.getMessage().contains("JPEG"), ex.getMessage());
        }
    }

    @Test(dataProvider = "broken")
    public void tolerantWarnsAndPrunesReferences(IggyFile.Variant variant, String defect) throws Exception {
        SWF source = brokenMovie(defect);
        byte[] before = bytes(source);
        List<String> warnings = new ArrayList<>();
        SWF result = IggyToSwfConvertor.getSwf(reload(SwfToIggyConvertor.createIggy(source, variant, "broken-jpeg.swf", warnings::add)));
        Assert.assertTrue(warnings.stream().anyMatch(w -> w.contains("DefineBits") && w.contains("JPEG")), warnings.toString());
        Assert.assertNull(result.getCharacter(3));
        Assert.assertNull(result.getCharacter(7));
        Assert.assertNull(result.getCharacter(8));
        Assert.assertNull(result.getCharacter(10));
        for (Tag tag : result.getTags()) {
            if (tag instanceof PlaceObjectTypeTag) {
                Assert.assertEquals(((PlaceObjectTypeTag) tag).getCharacterId(), 9, "Only the empty sprite may remain placed");
            }
        }
        Assert.assertEquals(result.frameCount, 1);
        Assert.assertEquals(bytes(source), before);
    }
}
