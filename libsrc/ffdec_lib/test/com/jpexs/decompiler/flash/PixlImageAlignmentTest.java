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
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.UnknownTag;
import com.jpexs.decompiler.flash.tags.profiles.castlecrashers.PixlImage;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PixlImageAlignmentTest {

    private SWF read(byte[] bytes) throws Exception {
        return new SWF(new ByteArrayInputStream(bytes), false, "UTF-8", new CastleCrashersSwfProfile());
    }

    private byte[] save(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return out.toByteArray();
    }

    private PixlImage image(SWF swf, int tagId, int characterId) throws Exception {
        PixlImage image = new PixlImage(swf);
        image.characterID = characterId;
        image.bounds.Xmax = 40;
        image.bounds.Ymax = 40;
        image.padding = new ByteArrayRange(new byte[7]);
        image.headerBeforeDimensions = new ByteArrayRange("4c584950000000000000000000000000");
        image.width = 2;
        image.height = 2;
        image.formatID = PixlImage.FORMAT_ARGB;
        image.bitmapInfo = new ByteArrayRange(new byte[8]);
        image.imageDataOffset = 96;
        image.textureInfo = new ByteArrayRange(new byte[8]);
        image.texturePointer = 112;
        image.imageInfo = new ByteArrayRange(new byte[28]);
        image.imageIdentifier = characterId;
        image.reservedData = new ByteArrayRange(new byte[12]);
        image.imageData = new ByteArrayRange("332211ff1020408010203000cdcdcdcd");
        image.footer = new ByteArrayRange("24000000280000003000000003000000");
        byte[] payload = image.getData();
        return new PixlImage(tagId, new SWFInputStream(swf, payload), new ByteArrayRange(payload));
    }

    private UnknownTag spacer(SWF swf, int payloadSize) {
        UnknownTag spacer = new UnknownTag(swf, 222);
        spacer.unknownData = new ByteArrayRange(new byte[payloadSize]);
        return spacer;
    }

    private SWF fixture(int depth, SWFCompression compression, int spacerSize) throws Exception {
        SWF swf = read(new byte[]{70, 87, 83, 6, 16, 0, 0, 0, 8, 0, 0, 12, 1, 0, 0, 0});
        List<Tag> children = new ArrayList<>();
        children.add(image(swf, PixlImage.ID, 42));
        children.add(image(swf, PixlImage.ID_INTERNAL, 43));
        for (int i = 0; i < depth; i++) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.spriteId = 100 + i;
            sprite.frameCount = 1;
            sprite.hasEndTag = true;
            for (Tag child : children) {
                sprite.addTag(child);
            }
            children = Arrays.<Tag>asList(sprite);
        }
        for (Tag child : children) {
            swf.addTag(child);
        }
        swf.addTag(spacer(swf, spacerSize));
        swf.compression = compression;
        // Start each edit from parsed, unmodified tags with valid original ranges.
        return read(save(swf));
    }

    private List<PixlImage> images(Iterable<Tag> tags) {
        List<PixlImage> images = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag instanceof PixlImage) {
                images.add((PixlImage) tag);
            } else if (tag instanceof DefineSpriteTag) {
                images.addAll(images(((DefineSpriteTag) tag).getTags()));
            }
        }
        return images;
    }

    private byte[] block(PixlImage image) {
        byte[] payload = image.getData();
        return Arrays.copyOfRange(payload, 20 + image.padding.getLength(), payload.length);
    }

    private void assertUnmodified(Iterable<Tag> tags) {
        for (Tag tag : tags) {
            assertFalse(tag.isModified());
            if (tag instanceof DefineSpriteTag) {
                assertUnmodified(((DefineSpriteTag) tag).getTags());
            }
        }
    }

    private void assertAligned(SWF swf, List<byte[]> expectedBlocks) {
        List<PixlImage> images = images(swf.getTags());
        assertEquals(images.size(), expectedBlocks.size());
        for (int i = 0; i < images.size(); i++) {
            PixlImage image = images.get(i);
            long payloadOffset = image.getDataPos();
            assertEquals(image.padding.getLength(), (int) (-(payloadOffset + 20) & 31));
            assertEquals((payloadOffset + 20 + image.padding.getLength()) & 31, 0L);
            assertEquals(block(image), expectedBlocks.get(i));
        }
    }

    @DataProvider(name = "positions")
    public Object[][] positions() {
        List<Object[]> cases = new ArrayList<>();
        // All offsets modulo 32, plus the short/long tag header boundary.
        for (int size : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 62, 63}) {
            for (int depth : new int[]{0, 2}) {
                for (SWFCompression compression : new SWFCompression[]{SWFCompression.NONE, SWFCompression.ZLIB}) {
                    cases.add(new Object[]{size, depth, compression});
                }
            }
        }
        return cases.toArray(new Object[cases.size()][]);
    }

    @Test(dataProvider = "positions")
    public void testMoveUnmodifiedTags(int spacerSize, int depth, SWFCompression compression) throws Exception {
        SWF swf = fixture(depth, compression, spacerSize);
        byte[] baseline = save(swf);
        List<PixlImage> originalImages = images(swf.getTags());
        List<byte[]> blocks = new ArrayList<>();
        List<byte[]> payloads = new ArrayList<>();
        for (PixlImage image : originalImages) {
            blocks.add(block(image));
            payloads.add(image.getData());
        }
        assertUnmodified(swf.getTags());
        Tag spacer = swf.getTags().get(swf.getTags().size() - 1);
        swf.removeTag(spacer);
        swf.addTag(0, spacer);
        assertUnmodified(swf.getTags());

        byte[] moved = save(swf);
        SWF reloaded = read(moved);
        assertAligned(reloaded, blocks);
        assertEquals(reloaded.compression, compression);
        assertEquals(save(swf), moved);
        assertEquals(save(reloaded), moved);
        assertUnmodified(swf.getTags());
        for (int i = 0; i < originalImages.size(); i++) {
            // Saving does not change the editable padding or standalone getData().
            assertEquals(originalImages.get(i).getData(), payloads.get(i));
        }

        swf.removeTag(spacer);
        swf.addTag(spacer);
        assertEquals(save(swf), baseline);
    }

    @Test
    public void testEditInsideSprite() throws Exception {
        SWF swf = fixture(2, SWFCompression.NONE, 0);
        DefineSpriteTag outer = (DefineSpriteTag) swf.getTags().get(0);
        DefineSpriteTag inner = (DefineSpriteTag) outer.getTags().get(0);
        List<byte[]> blocks = new ArrayList<>();
        for (PixlImage image : images(swf.getTags())) {
            blocks.add(block(image));
        }
        inner.addTag(1, spacer(swf, 13));
        assertAligned(read(save(swf)), blocks);
        for (PixlImage image : images(swf.getTags())) {
            assertFalse(image.isModified());
        }
    }

    @Test
    public void testScriptFreeSerialization() throws Exception {
        SWF swf = fixture(2, SWFCompression.NONE, 0);
        DefineSpriteTag sprite = (DefineSpriteTag) swf.getTags().get(0);
        DoActionTag script = new DoActionTag(swf);
        script.setActionBytes(new byte[]{0});
        sprite.addTag(0, script);
        List<byte[]> blocks = new ArrayList<>();
        for (PixlImage image : images(swf.getTags())) {
            blocks.add(block(image));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SWFOutputStream sos = new SWFOutputStream(out, 6, "UTF-8", 0);
        // A valid SWF header, then an extra two-byte tag before the sprite.
        sos.write(new byte[]{70, 87, 83, 6, 0, 0, 0, 0, 8, 0, 0, 12, 1, 0});
        spacer(swf, 0).writeTag(sos);
        sprite.writeTagNoScripts(sos);
        sos.writeUI16(0);
        byte[] bytes = out.toByteArray();
        for (int i = 0; i < 4; i++) {
            bytes[4 + i] = (byte) (bytes.length >> (8 * i));
        }
        SWF reloaded = read(bytes);
        assertAligned(reloaded, blocks);
        assertEquals(((DefineSpriteTag) reloaded.getTags().get(1)).getTags().size(), 1);
        assertTrue(sprite.getTags().get(0) instanceof DoActionTag);
    }
}
