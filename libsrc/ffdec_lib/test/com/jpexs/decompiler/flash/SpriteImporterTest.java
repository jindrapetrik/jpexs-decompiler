/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License, version 3 or later.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.importers.SpriteImporter;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.images.apng.Png;
import com.jpexs.images.apng.chunks.Actl;
import com.jpexs.images.apng.chunks.Chunk;
import com.jpexs.images.apng.chunks.Fctl;
import com.jpexs.images.apng.chunks.Fdat;
import com.jpexs.images.apng.chunks.Idat;
import com.jpexs.images.apng.chunks.Iend;
import dev.matrixlab.webp4j.WebPCodec;
import dev.matrixlab.webp4j.gif.GifToWebPConfig;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import net.weiner.kevin.AnimatedGifEncoder;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class SpriteImporterTest {

    private BufferedImage image(int width, int color) {
        BufferedImage image = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            image.setRGB(x, 0, color);
        }
        return image;
    }

    private byte[] encode(BufferedImage image, String format) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Assert.assertTrue(ImageIO.write(image, format, out));
        return out.toByteArray();
    }

    private DefineSpriteTag importImage(byte[] data) throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 10;
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        swf.addTag(sprite);
        sprite.setTimelined(swf);
        // Replacing an existing timeline must discard its old frames.
        sprite.addTag(new ShowFrameTag(swf));
        sprite.addTag(new ShowFrameTag(swf));
        Assert.assertTrue(new SpriteImporter().importSprite(sprite, new ByteArrayInputStream(data)));
        int frames = 0;
        for (Tag tag : sprite.getTags()) {
            if (tag instanceof ShowFrameTag) {
                frames++;
            }
        }
        Assert.assertEquals(sprite.frameCount, frames);
        Assert.assertEquals(sprite.getTimeline().getFrameCount(), frames);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        SWF restored = new SWF(new ByteArrayInputStream(out.toByteArray()), false);
        DefineSpriteTag restoredSprite = (DefineSpriteTag) restored.getCharacter(sprite.spriteId);
        Assert.assertEquals(restoredSprite.frameCount, frames);
        Assert.assertEquals(restoredSprite.getTimeline().getFrameCount(), frames);
        return restoredSprite;
    }

    private List<BufferedImage> placedImages(DefineSpriteTag sprite) {
        List<BufferedImage> images = new ArrayList<>();
        for (Tag tag : sprite.getTags()) {
            if (tag instanceof PlaceObject2Tag) {
                PlaceObject2Tag place = (PlaceObject2Tag) tag;
                Assert.assertTrue(place.placeFlagHasMatrix);
                ShapeTag shape = (ShapeTag) sprite.getSwf().getCharacter(place.characterId);
                int bitmapId = shape.getShapes().fillStyles.fillStyles[0].bitmapId;
                images.add(((ImageTag) sprite.getSwf().getCharacter(bitmapId)).getImageCached().getBufferedImage());
            }
        }
        return images;
    }

    @Test
    public void staticRasterImages() throws Exception {
        for (String format : Arrays.asList("png", "gif", "bmp", "jpg")) {
            BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, 0xffff0000);
            DefineSpriteTag sprite = importImage(encode(image, format));
            Assert.assertEquals(sprite.frameCount, 1, format);
            Assert.assertEquals(placedImages(sprite).get(0).getWidth(), 3, format);
        }
    }

    @Test
    public void svgCreatesVectorShape() throws Exception {
        String svg = "\uFEFF<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"3\" height=\"2\">"
                + "<rect width=\"3\" height=\"2\" fill=\"red\"/></svg>";
        DefineSpriteTag sprite = importImage(svg.getBytes("UTF-8"));
        Assert.assertEquals(sprite.frameCount, 1);
        PlaceObject2Tag place = (PlaceObject2Tag) sprite.getTags().get(0);
        ShapeTag shape = (ShapeTag) sprite.getSwf().getCharacter(place.characterId);
        Assert.assertEquals(shape.getRect().getWidth(), 60);
        Assert.assertEquals(shape.getRect().getHeight(), 40);
        for (Tag tag : sprite.getSwf().getTags()) {
            Assert.assertFalse(tag instanceof ImageTag);
        }
    }

    @Test
    public void gifTiming() throws Exception {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encoder.start(out);
        encoder.setDelay(200);
        encoder.addFrame(image(2, 0xffff0000));
        encoder.setDelay(300);
        encoder.addFrame(image(2, 0xff0000ff));
        encoder.finish();
        DefineSpriteTag sprite = importImage(out.toByteArray());
        Assert.assertEquals(sprite.frameCount, 5);
        Assert.assertEquals(placedImages(sprite).size(), 2);
    }

    private void addPngFrame(Png png, int sequence, int color, int x, int dispose, int blend, boolean idat) throws Exception {
        png.addChunk(new Fctl(sequence, 1, 1, x, 0, 1, 10, dispose, blend));
        Png source = new Png(new ByteArrayInputStream(encode(image(1, color), "png")));
        for (Chunk chunk : source.getChunks()) {
            if (chunk instanceof Idat) {
                byte[] compressed = ((Idat) chunk).getFrameData();
                int split = compressed.length / 2;
                byte[] first = Arrays.copyOfRange(compressed, 0, split);
                byte[] second = Arrays.copyOfRange(compressed, split, compressed.length);
                png.addChunk(idat ? new Idat(first) : new Fdat(sequence + 1, first));
                png.addChunk(idat ? new Idat(second) : new Fdat(sequence + 2, second));
            }
        }
    }

    @Test
    public void apngCompositionAndDisposal() throws Exception {
        Png png = new Png();
        Png source = new Png(new ByteArrayInputStream(encode(image(2, 0), "png")));
        png.addChunk(source.getChunk(0));
        png.addChunk(new Actl(4, 0));
        // Separate default image is not part of the animation.
        for (Chunk chunk : source.getChunks()) {
            if (chunk instanceof Idat) {
                png.addChunk(chunk);
            }
        }
        addPngFrame(png, 0, 0xffff0000, 0, Fctl.DISPOSE_OP_NONE, Fctl.BLEND_OP_SOURCE, false);
        addPngFrame(png, 3, 0xff0000ff, 1, Fctl.DISPOSE_OP_PREVIOUS, Fctl.BLEND_OP_OVER, false);
        addPngFrame(png, 6, 0xff00ff00, 0, Fctl.DISPOSE_OP_BACKGROUND, Fctl.BLEND_OP_SOURCE, false);
        addPngFrame(png, 9, 0, 1, Fctl.DISPOSE_OP_NONE, Fctl.BLEND_OP_OVER, false);
        png.addChunk(new Iend());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        png.writeTo(out);
        DefineSpriteTag sprite = importImage(out.toByteArray());
        List<BufferedImage> frames = placedImages(sprite);
        Assert.assertEquals(sprite.frameCount, 4);
        Assert.assertEquals(frames.get(0).getRGB(0, 0), 0xffff0000);
        Assert.assertEquals(frames.get(1).getRGB(1, 0), 0xff0000ff);
        Assert.assertEquals(frames.get(2).getRGB(0, 0), 0xff00ff00);
        Assert.assertEquals(frames.get(2).getRGB(1, 0), 0);
        Assert.assertEquals(frames.get(3).getRGB(0, 0), 0);
        Assert.assertEquals(frames.get(3).getRGB(1, 0), 0);
    }

    @Test
    public void apngFirstFrameInIdat() throws Exception {
        Png source = new Png(new ByteArrayInputStream(encode(image(1, 0), "png")));
        Png png = new Png();
        png.addChunk(source.getChunk(0));
        png.addChunk(new Actl(2, 0));
        addPngFrame(png, 0, 0xffff0000, 0, 0, 0, true);
        addPngFrame(png, 1, 0xff0000ff, 0, 0, 0, false);
        png.addChunk(new Iend());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        png.writeTo(out);
        List<BufferedImage> frames = placedImages(importImage(out.toByteArray()));
        Assert.assertEquals(frames.size(), 2);
        Assert.assertEquals(frames.get(0).getRGB(0, 0), 0xffff0000);
        Assert.assertEquals(frames.get(1).getRGB(0, 0), 0xff0000ff);
    }

    @Test
    public void webpStaticAndAnimated() throws Exception {
        if (!WebPCodec.isAvailable()) {
            throw new SkipException("WEBP is unavailable on this platform");
        }
        DefineSpriteTag still = importImage(WebPCodec.encodeLosslessImage(image(2, 0xffff0000)));
        Assert.assertEquals(still.frameCount, 1);
        Assert.assertEquals(placedImages(still).get(0).getRGB(0, 0), 0xffff0000);
        byte[] animated = WebPCodec.createAnimatedWebP(Arrays.asList(image(2, 0xffff0000), image(2, 0xff0000ff)),
                new int[]{200, 300}, GifToWebPConfig.createLosslessConfig());
        DefineSpriteTag sprite = importImage(animated);
        Assert.assertEquals(sprite.frameCount, 5);
        List<BufferedImage> frames = placedImages(sprite);
        Assert.assertEquals(frames.size(), 2);
        Assert.assertEquals(frames.get(0).getRGB(0, 0), 0xffff0000);
        Assert.assertEquals(frames.get(1).getRGB(0, 0), 0xff0000ff);
    }

    @Test
    public void invalidImagePreservesTimeline() throws Exception {
        SWF swf = new SWF();
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        ShowFrameTag original = new ShowFrameTag(swf);
        sprite.addTag(original);
        for (byte[] data : Arrays.asList(new byte[0], new byte[]{1, 2, 3, 4})) {
            try {
                new SpriteImporter().importSprite(sprite, new ByteArrayInputStream(data));
                Assert.fail("Invalid image accepted");
            } catch (IOException ex) {
                Assert.assertEquals(sprite.getTags().size(), 1);
                Assert.assertSame(sprite.getTags().get(0), original);
            }
        }
    }
}
