/*
 *  Copyright (C) 2010-2022 JPEXS, All rights reserved.
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

import static com.jpexs.helpers.JpegFixer.EOI;
import static com.jpexs.helpers.JpegFixer.SOI;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class JpegFixerTest {

    private JpegFixer fixer = new JpegFixer();

    @DataProvider(name = "bytes")
    public static Object[][] provideSamples() {
        return new Object[][]{
            {
                new byte[]{(byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI, (byte) 0xFF, (byte) SOI, (byte) 0x21, (byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) SOI, (byte) 0x21, (byte) 0xFF, (byte) EOI},},
            {
                new byte[]{(byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI},
                new byte[]{(byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI},},
            {
                new byte[]{(byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI, 0x23},
                new byte[]{(byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI, 0x23},},
            {
                new byte[]{(byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) EOI},},
            {
                new byte[]{(byte) 0xFF},
                new byte[]{(byte) 0xFF},},
            {
                new byte[]{(byte) 0x26},
                new byte[]{(byte) 0x26},},
            {
                new byte[]{},
                new byte[]{},},
            {
                new byte[]{(byte) 0xFF, (byte) SOI, 0x27, 0x37, 0x47, 0x57, (byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) SOI, 0x27, 0x37, 0x47, 0x57, (byte) 0xFF, (byte) EOI}
            },
            {
                new byte[]{(byte) 0xFF, (byte) SOI, 0x28, 0x38, (byte) 0xFF, (byte) EOI, (byte) 0xFF, (byte) SOI, 0x48, 0x58, (byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) SOI, 0x28, 0x38, 0x48, 0x58, (byte) 0xFF, (byte) EOI}
            },
            {
                new byte[]{(byte) 0xFF, (byte) SOI, 0x29, (byte) 0xFF, (byte) SOI, 0x39, (byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) SOI, 0x29, 0x39, (byte) 0xFF, (byte) EOI},},
            {
                new byte[]{(byte) 0xFF, (byte) SOI, (byte) 0xFF, (byte) SOI, 0x2A, (byte) 0xFF, (byte) EOI},
                new byte[]{(byte) 0xFF, (byte) SOI, 0x2A, (byte) 0xFF, (byte) EOI}
            }
        };
    }

    @Test(dataProvider = "bytes")
    public void testFixJpeg(byte[] inputData, byte[] expectedOutput) throws IOException {
        JpegFixer fixer = new JpegFixer();
        ByteArrayInputStream bais = new ByteArrayInputStream(inputData);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fixer.fixJpeg(bais, baos);
        byte[] actualOutput = baos.toByteArray();
        try {
            Assert.assertEquals(Helper.byteArrToString(actualOutput), Helper.byteArrToString(expectedOutput), "Bytes do not match");
        } catch (AssertionError er) {
            System.out.println("Bytes do not match:");
            System.out.println("INPUT: " + Helper.byteArrToString(inputData));
            System.out.println("ACTUAL OUTPUT: " + Helper.byteArrToString(actualOutput));
            System.out.println("EXPECTED OUTPUT: " + Helper.byteArrToString(expectedOutput));
            Assert.fail();
        }
    }
}
