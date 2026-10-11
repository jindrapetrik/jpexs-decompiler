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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.profiles.CastleCrashersSwfProfile;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.profiles.castlecrashers.PixlImage;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PixlImageTest {

    private byte[] imageData(int headerPadding, int pixelPadding, boolean textureSlotAtPixelEnd) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(out, 6, "UTF-8");
        sos.writeUI16(42);
        sos.writeUI16(0);
        sos.writeSI32(-200);
        sos.writeSI32(200);
        sos.writeSI32(-100);
        sos.writeSI32(100);
        for (int i = 0; i < headerPadding; i++) {
            sos.writeUI8(0xcd);
        }
        sos.write(new byte[]{'L', 'X', 'I', 'P'});
        sos.write(new byte[12]);
        sos.writeUI32(2);
        sos.writeUI32(2);
        sos.writeUI32(PixlImage.FORMAT_ARGB);
        sos.write(new byte[8]);
        sos.writeUI32(96 + pixelPadding);
        sos.write(new byte[8]);
        // Preserve both the game's and the Ruby importer's texture slot values.
        sos.writeUI32(16 + pixelPadding + (textureSlotAtPixelEnd ? 96 : 0));
        sos.write(new byte[28]);
        sos.writeUI32(42);
        sos.write(new byte[12]);
        for (int i = 0; i < pixelPadding; i++) {
            sos.writeUI8(0xa5);
        }
        sos.write(new byte[]{(byte) 0xcd, (byte) 0xcd, (byte) 0xcd, (byte) 0xcd,
            0x33, 0x22, 0x11, (byte) 0xff, 0x10, 0x20, 0x40, (byte) 0x80,
            0x10, 0x20, 0x30, 0});
        sos.writeUI32(36);
        sos.writeUI32(40);
        sos.writeUI32(48);
        sos.writeUI32(3);
        return out.toByteArray();
    }

    private SWF readSwf(byte[] payload, boolean profile) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{70, 87, 83, 6, 0, 0, 0, 0, 8, 0, 0, 12, 1, 0});
        SWFOutputStream sos = new SWFOutputStream(out, 6, "UTF-8");
        sos.writeUI16((PixlImage.ID << 6) | 63);
        sos.writeUI32(payload.length);
        sos.write(payload);
        sos.writeUI16(0);
        byte[] bytes = out.toByteArray();
        for (int i = 0; i < 4; i++) {
            bytes[4 + i] = (byte) (bytes.length >> (8 * i));
        }
        return new SWF(new ByteArrayInputStream(bytes), false, "UTF-8",
                profile ? new CastleCrashersSwfProfile() : null);
    }

    @DataProvider(name = "layouts")
    public Object[][] layouts() {
        return new Object[][]{{4, 0, true}, {16, 0, false}, {4, 8, true}, {16, 8, false}};
    }

    @Test(dataProvider = "layouts")
    public void testDecodeAndRoundTrip(int headerPadding, int pixelPadding, boolean textureSlotAtPixelEnd) throws Exception {
        byte[] payload = imageData(headerPadding, pixelPadding, textureSlotAtPixelEnd);
        SWF swf = readSwf(payload, true);
        assertTrue(swf.getTags().get(0) instanceof PixlImage);
        PixlImage image = (PixlImage) swf.getTags().get(0);
        assertEquals(image.getCharacterId(), 42);
        assertEquals(image.imageIdentifier, 42L);
        assertEquals(image.imageDataOffset, (long) (96 + pixelPadding));
        assertEquals(image.formatID, PixlImage.FORMAT_ARGB);
        assertEquals(image.getImageDimension(), new Dimension(2, 2));
        assertEquals(image.bounds.Xmin, -200);
        assertEquals(image.bounds.Ymax, 100);
        assertEquals(image.getRect().Xmin, -200);
        assertEquals(image.getRect().getWidth(), 400);
        MATRIX bitmapMatrix = image.getShape(image.getRect(), false, 3).fillStyles.fillStyles[0].bitmapMatrix;
        assertEquals(bitmapMatrix.scaleX, 200.0f);
        assertEquals(bitmapMatrix.scaleY, 100.0f);
        assertEquals(bitmapMatrix.translateX, -200);
        assertEquals(bitmapMatrix.translateY, -100);
        assertEquals(image.padding.getLength(), headerPadding);
        assertFalse(image.importSupported());

        BufferedImage bitmap = image.getImageCached().getBufferedImage();
        assertEquals(bitmap.getRGB(0, 0), 0xcdffffff);
        assertEquals(bitmap.getRGB(1, 0), 0xff112233);
        assertEquals(bitmap.getRGB(0, 1), 0x80804020);
        assertEquals(bitmap.getRGB(1, 1), 0);
        BufferedImage png = ImageIO.read(image.getImageData());
        assertEquals(png.getRGB(0, 1), bitmap.getRGB(0, 1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.getData(new SWFOutputStream(out, 6, "UTF-8"));
        assertEquals(out.toByteArray(), payload);
        image.setCharacterId(123);
        // The engine gets the SWF character ID only from the outer header.
        assertEquals(image.imageIdentifier, 42L);
    }

    @Test
    public void testRelocationTableAndExtraData() throws Exception {
        byte[] payload = imageData(4, 0, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(payload, 0, payload.length - 16);
        SWFOutputStream sos = new SWFOutputStream(out, 6, "UTF-8");
        sos.writeUI32(0xa5a5a5a5L); // Extra bytes are not pixels or the relocation table.
        sos.writeUI32(12); // An additional null pointer field.
        sos.writeUI32(36);
        sos.writeUI32(40);
        sos.writeUI32(48);
        sos.writeUI32(4);
        byte[] extended = out.toByteArray();
        PixlImage image = (PixlImage) readSwf(extended, true).getCharacter(42);
        assertEquals(image.footer.getLength(), 20);
        assertEquals(image.getImageCached().getRGB(0, 0), 0xcdffffff);
        assertEquals(image.getImageCached().getRGB(1, 0), 0xff112233);
        assertEquals(image.getData(), extended);
    }

    @Test
    public void testInvalidPixelOffsetAndUnsupportedFormat() throws Exception {
        PixlImage image = (PixlImage) readSwf(imageData(4, 0, true), true).getCharacter(42);
        for (long offset : new long[]{0, 95, 104, 0xffffffffL}) {
            image.imageDataOffset = offset;
            image.clearCache();
            assertEquals(image.getImageCached().getWidth(), 1);
            assertEquals(image.getImageCached().getRGB(0, 0), 0);
        }
        image.imageDataOffset = 96;
        image.formatID = 5;
        image.clearCache();
        assertEquals(image.getImageCached().getWidth(), 1);
    }

    @Test
    public void testDirectPlacement() throws Exception {
        SWF swf = readSwf(imageData(4, 0, true), true);
        PixlImage image = (PixlImage) swf.getCharacter(42);
        PlaceObject2Tag place = new PlaceObject2Tag(swf);
        place.placeFlagHasCharacter = true;
        place.characterId = 42;
        place.depth = 1;
        place.placeFlagHasMatrix = true;
        place.matrix = new MATRIX();
        swf.addTag(1, place);
        swf.addTag(2, new ShowFrameTag(swf));
        swf.resetTimeline();
        BufferedImage frame = SWF.frameToImageGet(swf.getTimeline(), 0, 0, null, 0,
                image.getRect(), new Matrix(), null, null, 1.0, false, 1).getBufferedImage();
        assertEquals(frame.getWidth(), 20);
        assertEquals(frame.getHeight(), 10);
        assertEquals(frame.getRGB(15, 2), 0xff112233);
        assertEquals(frame.getRGB(15, 7), 0);
    }

    @Test
    public void testProfileRegistration() throws Exception {
        CastleCrashersSwfProfile profile = new CastleCrashersSwfProfile();
        assertTrue(profile.supportsTagId(148));
        assertEquals(profile.getTagIdClass(148), PixlImage.class);
        assertEquals(profile.getSupportedTags().get(148), PixlImage.class);
        assertFalse(profile.supportsTagId(149));
        assertTrue(readSwf(imageData(4, 0, true), false).getTags().get(0) instanceof UnknownTag);
        // The Ruby import template contains an empty placeholder tag.
        assertTrue(readSwf(new byte[0], true).getTags().get(0) instanceof UnknownTag);
    }

    @Test(expectedExceptions = IOException.class)
    public void testTruncatedHeader() throws Exception {
        byte[] truncated = new byte[40];
        SWF swf = readSwf(new byte[0], false);
        new PixlImage(PixlImage.ID, new SWFInputStream(swf, truncated), new ByteArrayRange(truncated));
    }

    @Test
    public void testTruncatedPixels() throws Exception {
        byte[] payload = imageData(4, 0, true);
        PixlImage image = (PixlImage) readSwf(payload, true).getTags().get(0);
        image.imageData = ByteArrayRange.EMPTY;
        assertEquals(image.getImageCached().getWidth(), 1);
        assertEquals(image.getImageCached().getRGB(0, 0), 0);
        image.clearCache();
        image.width = Integer.MAX_VALUE;
        assertEquals(image.getImageCached().getWidth(), 1);
    }
}
