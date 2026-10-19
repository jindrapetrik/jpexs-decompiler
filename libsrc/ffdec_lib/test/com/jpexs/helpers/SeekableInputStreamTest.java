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

import com.jpexs.helpers.streams.SeekableInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class SeekableInputStreamTest {

    @Test
    public void testFakeMemoryInputStreamPositionAndSeek() throws IOException {
        assertPositionAndSeek(new FakeMemoryInputStream(new ByteArrayInputStream(new byte[]{1, 2})));
    }

    @Test
    public void testReReadableInputStreamPositionAndSeek() throws IOException {
        assertPositionAndSeek(new ReReadableInputStream(new ByteArrayInputStream(new byte[]{1, 2})));
    }

    private void assertPositionAndSeek(SeekableInputStream stream) throws IOException {
        assertEquals(stream.read(), 1);
        stream.seek(0);
        assertEquals(stream.read(), 1);
        stream.seek(2);
        assertEquals(stream.read(), -1);
        assertEquals(stream.read(), -1);
        assertEquals(stream.available(), 0);

        try {
            stream.seek(3);
            fail("Seeking beyond the end must fail");
        } catch (IOException expected) {
            // expected
        }
        assertEquals(stream.read(), -1);
    }
}
