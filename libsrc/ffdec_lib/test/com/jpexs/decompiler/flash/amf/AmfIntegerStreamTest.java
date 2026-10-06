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
package com.jpexs.decompiler.flash.amf;

import com.jpexs.decompiler.flash.amf.amf0.Amf0InputStream;
import com.jpexs.decompiler.flash.amf.amf3.Amf3InputStream;
import com.jpexs.helpers.MemoryInputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class AmfIntegerStreamTest {

    @Test
    public void testAmf0UnsignedAndSigned32() throws IOException {
        assertEquals(amf0(new byte[]{(byte) 0x80, 0, 0, 0}).readU32("test"), 0x80000000L);
        assertEquals(amf0(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}).readU32("test"), 0xffffffffL);
        assertEquals(amf0(new byte[]{(byte) 0x80, 0, 0, 0}).readS32("test"), -2147483648L);
        assertEquals(amf0(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}).readS32("test"), -1L);
    }

    @Test
    public void testAmf3UnsignedAndSigned32() throws IOException {
        assertEquals(amf3(new byte[]{(byte) 0x80, 0, 0, 0}).readU32("test"), 0x80000000L);
        assertEquals(amf3(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}).readU32("test"), 0xffffffffL);
        assertEquals(amf3(new byte[]{(byte) 0x80, 0, 0, 0}).readS32("test"), -2147483648L);
        assertEquals(amf3(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}).readS32("test"), -1L);
    }

    private Amf0InputStream amf0(byte[] data) throws IOException {
        return new Amf0InputStream(new MemoryInputStream(data));
    }

    private Amf3InputStream amf3(byte[] data) throws IOException {
        return new Amf3InputStream(new MemoryInputStream(data));
    }
}
