/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
}
