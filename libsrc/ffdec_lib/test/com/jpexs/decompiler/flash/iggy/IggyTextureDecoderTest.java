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

import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_BC7;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_DXT1;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_DXT3;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_DXT5;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_I_4;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_I_8;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_LA_44;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_LA_88;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_L_4;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_L_8;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_RGBA_4444_LE;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_RGBA_5551_LE;
import static com.jpexs.decompiler.flash.iggy.IggyTextureDecoder.FORMAT_RGBA_8888;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyBitmapConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyTextureDecoderTest {
    private static byte[] hex(String value) {
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }

    @DataProvider(name = "rawFormats")
    public Object[][] rawFormats() {
        return new Object[][] {
            {FORMAT_RGBA_8888, "0b162111", 0x110b1621},
            {FORMAT_RGBA_4444_LE, "218f", 0x88ff2211},
            {FORMAT_RGBA_5551_LE, "01fc", 0xffff0008},
            {FORMAT_LA_88, "8040", 0x40808080},
            {FORMAT_LA_44, "82", 0x88222222},
            {FORMAT_I_8, "40", 0x40404040},
            {FORMAT_I_4, "77", 0x77777777},
            {FORMAT_L_8, "7b", 0xff7b7b7b},
            {FORMAT_L_4, "77", 0xff777777}
        };
    }

    @Test(dataProvider = "rawFormats")
    public void packedChannels(int format, String bytes, int argb) throws Exception {
        for (int mipCount : new int[] {0, 1}) {
            Assert.assertEquals(IggyTextureDecoder.decode(1, 1, format | mipCount << 8, hex(bytes)), new int[] {argb});
        }
    }

    @Test(dataProvider = "rawFormats")
    public void companionOffsetAndAtlasCrop(int format, String bytes, int argb) throws Exception {
        Path directory = Files.createTempDirectory("iggy-texture-");
        Path resource = directory.resolve("texture.raw.iggytex");
        try {
            byte[] pixel = hex(bytes);
            byte[] data = new byte[13 + pixel.length * 3];
            System.arraycopy(pixel, 0, data, 13 + pixel.length, pixel.length);
            Files.write(resource, data);
            SWF swf = new SWF();
            swf.setFile(directory.resolve("movie.iggy").toString());
            IggyExternalImage image = new IggyExternalImage(swf);
            image.fileName = resource.getFileName().toString();
            image.resourceType = 8;
            image.resourceOffset = 13;
            image.resourceFormat = 0x200 | format;
            image.resourceWidth = 2;
            image.resourceHeight = 1;
            image.width = 3;
            image.height = 2;
            image.offsetX = 0.5f;
            image.scaleX = 0.5f;
            image.scaleY = 1;
            BufferedImage preview = IggyBitmapConvertor.readExternal(image);
            Assert.assertEquals(preview.getRGB(1, 1), argb);
            Assert.assertEquals(image.replacementImage.getLength(), 0);
            Files.write(resource, Arrays.copyOf(data, data.length - 1));
            try {
                IggyBitmapConvertor.readExternal(image);
                Assert.fail("Missing mip must be rejected");
            } catch (IOException expected) {
                Assert.assertTrue(expected.getMessage().contains("outside companion"));
            }
        } finally {
            Files.deleteIfExists(resource);
            Files.deleteIfExists(directory);
        }
    }

    @Test
    public void dxtColorAndTransparency() throws Exception {
        int[] pixels = IggyTextureDecoder.decode(4, 4, FORMAT_DXT1, hex("00f81f00e4e4e4e4"));
        Assert.assertEquals(Arrays.copyOf(pixels, 4), new int[] {0xffff0000, 0xff0000ff, 0xffaa0055, 0xff5500aa});
        pixels = IggyTextureDecoder.decode(3, 2, FORMAT_DXT1, hex("000000f8ffffffff"));
        Assert.assertEquals(pixels, new int[6]);
        pixels = IggyTextureDecoder.decode(5, 3, FORMAT_DXT1, hex("00f80000000000001f00000000000000"));
        Assert.assertEquals(pixels[3], 0xffff0000);
        Assert.assertEquals(pixels[4], 0xff0000ff);
        Assert.assertEquals(pixels[14], 0xff0000ff);
    }

    @Test
    public void dxtExplicitAlphaAndFourColorMode() throws Exception {
        int[] pixels = IggyTextureDecoder.decode(4, 4, FORMAT_DXT3, hex("1032547698badcfe000000f8ffffffff"));
        for (int i = 0; i < 16; i++) {
            Assert.assertEquals(pixels[i], i * 17 << 24 | 0xaa0000);
        }
    }

    @Test
    public void dxtInterpolatedAlpha() throws Exception {
        int[][] expected = {{255, 0, 218, 182, 145, 109, 72, 36}, {0, 255, 51, 102, 153, 204, 0, 255}};
        for (int branch = 0; branch < 2; branch++) {
            byte[] block = hex(branch == 0 ? "ff0088c6fa88c6fa00f8000000000000" : "00ff88c6fa88c6fa00f8000000000000");
            int[] pixels = IggyTextureDecoder.decode(4, 4, FORMAT_DXT5, block);
            for (int i = 0; i < 16; i++) {
                Assert.assertEquals(pixels[i], expected[branch][i % 8] << 24 | 0xff0000);
            }
        }
    }

    @Test
    public void mipLengthsAndBc7() throws Exception {
        Assert.assertEquals(IggyTextureDecoder.dataLength(4, 2, 0x300 | FORMAT_RGBA_8888), (8 + 2 + 1) * 4);
        Assert.assertEquals(IggyTextureDecoder.dataLength(5, 3, 0x300 | FORMAT_DXT1), (2 + 1 + 1) * 8);
        Assert.assertEquals(IggyTextureDecoder.dataLength(5, 3, 0x300 | FORMAT_DXT5), (2 + 1 + 1) * 16);
        Assert.assertEquals(IggyTextureDecoder.decode(4, 4, FORMAT_BC7, hex("20ff3f000000fcff0300000000000000"))[0], 0xffff0000);
    }

    @DataProvider(name = "invalid")
    public Object[][] invalid() {
        return new Object[][] {{1, 1, 12}, {1, 1, 14}, {1, 1, -1}, {1, 1, 65536},
            {0, 1, FORMAT_RGBA_8888}, {-1, 1, FORMAT_RGBA_8888}, {Integer.MAX_VALUE, Integer.MAX_VALUE, FORMAT_RGBA_8888},
            {1, 1, 0x200 | FORMAT_RGBA_8888}, {4, 4, 0x400 | FORMAT_RGBA_8888}};
    }

    @Test(dataProvider = "invalid", expectedExceptions = IOException.class)
    public void rejectsInvalidMetadata(int width, int height, int format) throws Exception {
        IggyTextureDecoder.dataLength(width, height, format);
    }

    @Test(expectedExceptions = IOException.class)
    public void rejectsMissingMip() throws Exception {
        IggyTextureDecoder.decode(4, 4, 0x200 | FORMAT_DXT5, new byte[16]);
    }

    @Test
    public void independentDxtReferencePixels() throws Exception {
        String[] expected = {"7614e16971eb102a8bc8c70b3657c0ce72d4bf496274fa21923f1ad9df8a3f3d", "49f01a419349b954b4a196610d90780bbfae57282237266a549cbfbbd6783dd3", "5694127e1ea9d485d8874f878bdfe06cb22f790f2ccf5b5b5eb40130b7ca4b6c"};
        int[] formats = {FORMAT_DXT1, FORMAT_DXT3, FORMAT_DXT5};
        for (int f = 0; f < formats.length; f++) {
            int format = formats[f];
            byte[] data = new byte[6 * (format == FORMAT_DXT1 ? 8 : 16)];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i * 73 + 19 + (i / 7) * 31);
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            for (int pixel : IggyTextureDecoder.decode(12, 8, format, data)) {
                for (int shift = 24; shift >= 0; shift -= 8) {
                    bytes.write(pixel >>> shift);
                }
            }
            StringBuilder hash = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray())) {
                hash.append(String.format("%02x", b & 255));
            }
            Assert.assertEquals(hash.toString(), expected[f], "Pillow DDS reference, format " + format);
        }
    }

    @Test
    public void metadataSurvivesBothContainerLayouts() throws Exception {
        Path path = Files.createTempFile("iggy-texture-roundtrip-", ".iggy");
        try {
            for (IggyFile.Variant variant : new IggyFile.Variant[] {IggyFile.Variant.X86, IggyFile.Variant.X64}) {
                SWF movie = new SWF();
                movie.version = 10;
                movie.frameRate = 24;
                movie.frameCount = 1;
                for (int format : new int[] {FORMAT_RGBA_8888, FORMAT_RGBA_4444_LE, FORMAT_RGBA_5551_LE, FORMAT_LA_88, FORMAT_LA_44, FORMAT_I_8, FORMAT_I_4, FORMAT_L_8, FORMAT_L_4, FORMAT_DXT1, FORMAT_DXT3, FORMAT_DXT5, FORMAT_BC7}) {
                    IggyExternalImage image = new IggyExternalImage(movie);
                    image.characterID = format + 1;
                    image.width = image.height = image.resourceWidth = image.resourceHeight = 4;
                    image.fileName = "texture.raw.iggytex";
                    image.resourceType = 8;
                    image.resourceOffset = 13 + format * 80;
                    image.resourceFormat = 0x200 | format;
                    movie.addTag(image);
                }
                movie.addTag(new ShowFrameTag(movie));
                IggyFile created = SwfToIggyConvertor.createIggy(movie, variant, "texture.swf");
                created.saveTo(path.toFile());
                IggyFile loaded = new IggyFile(path.toFile());
                SWF converted = IggyToSwfConvertor.getSwf(loaded);
                int count = 0;
                for (Tag tag : converted.getTags()) {
                    if (tag instanceof IggyExternalImage) {
                        IggyExternalImage image = (IggyExternalImage) tag;
                        Assert.assertEquals(image.resourceFormat, 0x200 | image.characterID - 1);
                        Assert.assertEquals(image.resourceOffset, 13L + (image.characterID - 1) * 80);
                        count++;
                    }
                }
                Assert.assertEquals(count, 13);
                byte[] original = Files.readAllBytes(path);
                SwfToIggyConvertor.updateIggy(loaded.getSwf(), converted);
                loaded.saveTo(path.toFile());
                Assert.assertEquals(Files.readAllBytes(path), original);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
