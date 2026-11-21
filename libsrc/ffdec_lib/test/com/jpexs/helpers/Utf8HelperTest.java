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
package com.jpexs.helpers;

import com.jpexs.helpers.utf8.Utf8Helper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class Utf8HelperTest {
    @DataProvider(name = "samples")
    public static Object[][] provideSamples() {
        return new Object[][]{
                {new byte[] {'A'}, "A"},
                {new byte[] {'A', (byte)0b10000111, 'B'}, "A{invalid_utf8=135}B"},
                {new byte[] {'A', (byte)0b11000101, (byte)0b10011001, 'B'}, "AřB"},
                {new byte[] {'A', (byte)0b11100000, (byte)0b10100000, (byte)0b10000000, 'B'}, "A" + (char)0x0800 + "B"},                
                {new byte[] {'A', (byte)0b11110000, (byte)0b10011101, (byte) 0b10010011, (byte)0b10101100, 'B'}, "A𝓬B"},
                {new byte[] {'A', (byte)0b11000101}, "A{invalid_utf8=197}"},
                {new byte[] {'A', (byte)0b11000101, 'B'}, "A{invalid_utf8=197}B"}                
                };
    }
    
    @Test(dataProvider = "samples")
    public void testInvalidBytes(byte[] data, String expected) {
        Assert.assertEquals(Utf8Helper.decode(data), expected);        
    }

    @Test(dataProvider = "samples")
    public void testEncodeInvalidBytes(byte[] data, String escaped) {
        Assert.assertEquals(Utf8Helper.getBytes(escaped), data);
        Assert.assertEquals(Utf8Helper.getBytesLength(escaped), data.length);
    }

    @Test
    public void testEscapesAmongUnicodeText() {
        String text = "ř𝓬{invalid_utf8=255}𝓬ž{invalid_utf8:128}";
        byte[] prefix = "ř𝓬".getBytes(Utf8Helper.charset);
        byte[] middle = "𝓬ž".getBytes(Utf8Helper.charset);
        byte[] expected = new byte[prefix.length + middle.length + 2];
        System.arraycopy(prefix, 0, expected, 0, prefix.length);
        expected[prefix.length] = (byte) 255;
        System.arraycopy(middle, 0, expected, prefix.length + 1, middle.length);
        expected[expected.length - 1] = (byte) 128;
        Assert.assertEquals(Utf8Helper.getBytes(text), expected);
    }

    @Test
    public void testLiteralAndIncompleteEscapes() {
        String text = "{+invalid_utf8=128}{invalid_utf8=x}{invalid_utf8=12";
        Assert.assertEquals(Utf8Helper.getBytes(text), text.getBytes(Utf8Helper.charset));
    }

    @Test(timeOut = 5000)
    public void testManyInvalidBytes() {
        byte[] bytes = new byte[100000];
        java.util.Arrays.fill(bytes, (byte) 255);
        String escaped = Utf8Helper.decode(bytes);
        Assert.assertEquals(Utf8Helper.getBytes(escaped), bytes);
        Assert.assertEquals(Utf8Helper.getBytesLength(escaped), bytes.length);
    }
}
