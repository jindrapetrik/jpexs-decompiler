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
package com.jpexs.decompiler.flash.helpers;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Bc7DecoderTest {

    @DataProvider(name = "modes")
    public Object[][] modes() {
        // Independent expected results from Pillow's BC7 DDS decoder, in ARGB
        // byte order. Each image contains 1024 nonuniform blocks and exercises
        // every partition, rotation and index selection available in that mode.
        return new Object[][] {
            {0, "bd9a5ce964f3dceaa3feadebbfb428ac727f4e72f07400160f8e326158514fc5"},
            {1, "78a59e50e678a5708afe2e9a86e90cf4f574f671552a2d8a739acacd7c979e7a"},
            {2, "999ee27cda8691e392076db125049b833e432d9b7b6ce432ba481b33260bceb1"},
            {3, "82b4e5c9a34c712c04cd807e573b30f0afa1e7fab11434f713e8d047d1196e3a"},
            {4, "b71c80620eb67502aff9bd13d059c7fb68540bfa0871ef0dd576b5a15f9dbe9c"},
            {5, "2d27ee2ccede6513d9a9b899eda028555b29f694e9d609fae0e71dea92004e7a"},
            {6, "910b3fdaf57dda1826499fbb4031409b416ee11eb912cde49f67c524b865d603"},
            {7, "6ee81da93789bbfca4dc4d2e2dcdcd818a528175f1fb179701db46c832f43a4c"}
        };
    }

    @Test(dataProvider = "modes")
    public void allModesMatchIndependentDecoder(int mode, String expected) throws Exception {
        byte[] data = new byte[1024 * 16];
        int state = 0x12345678 + mode;
        for (int block = 0; block < 1024; block++) {
            int offset = block * 128;
            for (int i = 0; i < 16; i++) {
                state = state * 1664525 + 1013904223;
                data[block * 16 + i] = (byte) (state >>> 24);
            }
            putBits(data, offset, 1 << mode, mode + 1);
            if (mode == 0 || mode == 1 || mode == 2 || mode == 3 || mode == 7) {
                putBits(data, offset + mode + 1, block % 64, mode == 0 ? 4 : 6);
            }
            if (mode == 4 || mode == 5) {
                putBits(data, offset + mode + 1, block % 8, mode == 4 ? 3 : 2);
            }
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int pixel : Bc7Decoder.decode(128, 128, data)) {
            digest.update((byte) (pixel >>> 24));
            digest.update((byte) (pixel >>> 16));
            digest.update((byte) (pixel >>> 8));
            digest.update((byte) pixel);
        }
        StringBuilder hash = new StringBuilder();
        for (byte value : digest.digest()) {
            hash.append(String.format("%02x", value & 255));
        }
        Assert.assertEquals(hash.toString(), expected);
    }

    @Test
    public void partialBlocksAreCroppedWithCorrectRowStride() throws Exception {
        byte[] data = new byte[64];
        solidBlock(data, 0, 127, 0, 0, 255);
        solidBlock(data, 1, 0, 0, 127, 255);
        solidBlock(data, 2, 0, 127, 0, 255);
        solidBlock(data, 3, 0, 0, 0, 0);
        int[] pixels = Bc7Decoder.decode(5, 5, data);
        Assert.assertEquals(pixels.length, 25);
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                Assert.assertEquals(pixels[y * 5 + x], y < 4
                        ? (x < 4 ? 0xffff0000 : 0xff0000ff)
                        : (x < 4 ? 0xff00ff00 : 0));
            }
        }
    }

    @Test
    public void reservedModeIsTransparentBlack() throws Exception {
        byte[] data = new byte[16];
        Arrays.fill(data, (byte) 255);
        data[0] = 0;
        Assert.assertEquals(Bc7Decoder.decode(4, 4, data), new int[16]);
    }

    @DataProvider(name = "invalid")
    public Object[][] invalid() {
        return new Object[][] {
            {0, 4, 16}, {-1, 4, 16}, {4, 0, 16}, {4, -1, 16},
            {Integer.MAX_VALUE, 4, 16}, {4, 4, 15}, {5, 5, 63}
        };
    }

    @Test(dataProvider = "invalid", expectedExceptions = IOException.class)
    public void invalidDimensionsAndTruncatedBlocksAreRejected(int width, int height, int length) throws Exception {
        Bc7Decoder.decode(width, height, new byte[length]);
    }

    private static void putBits(byte[] data, int offset, int value, int count) {
        for (int i = 0; i < count; i++) {
            int bit = offset + i;
            data[bit / 8] = (byte) ((data[bit / 8] & ~(1 << (bit % 8))) | (((value >>> i) & 1) << (bit % 8)));
        }
    }

    private static void solidBlock(byte[] data, int block, int red, int green, int blue, int alpha) {
        // Mode 5 stores 7-bit RGB and 8-bit alpha endpoints with no P-bits.
        int offset = block * 128;
        putBits(data, offset, 32, 6);
        int bit = offset + 8;
        for (int channel : new int[] {red, green, blue, alpha}) {
            int count = bit < offset + 50 ? 7 : 8;
            putBits(data, bit, channel, count);
            putBits(data, bit + count, channel, count);
            bit += 2 * count;
        }
    }
}
