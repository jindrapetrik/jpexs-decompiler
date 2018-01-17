/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.abc.ABCInputStream;
import com.jpexs.decompiler.flash.abc.ABCOutputStream;
import com.jpexs.helpers.MemoryInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class ABCStreamTest {

    @BeforeClass
    public void init() {
        //Main.initLogging(false);
    }

    private List<Long> getTestNumbers(long min, long max) {
        List<Long> res = new ArrayList<>();
        addWhenBetween(res, 1531, min, max);
        long x = 1;
        for (int i = 0; i < 32; i++) {
            x <<= 1;
            addWhenBetween(res, x - 1, min, max);
            addWhenBetween(res, x, min, max);
            addWhenBetween(res, x + 1, min, max);
            addWhenBetween(res, -(x - 1), min, max);
            addWhenBetween(res, -x, min, max);
            addWhenBetween(res, -(x + 1), min, max);
        }

        return res;
    }

    private void addWhenBetween(List<Long> list, long num, long min, long max) {
        if (num >= min && num <= max) {
            list.add(num);
        }
    }

    @Test
    public void testU30() {
        for (long number : getTestNumbers(0, (1L << 30) - 1)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ABCOutputStream aos = new ABCOutputStream(baos);) {
                aos.writeU30(number);
                aos.close();
                try (MemoryInputStream mis = new MemoryInputStream(baos.toByteArray());
                        ABCInputStream ais = new ABCInputStream(mis);) {
                    assertEquals(number, ais.readU30("test"));
                    assertEquals(0, mis.available());
                }
            } catch (IOException ex) {
                fail();
            }
        }
    }

    @Test
    public void testU32() {
        for (long number : getTestNumbers(0, (1L << 32) - 1)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ABCOutputStream aos = new ABCOutputStream(baos);) {
                aos.writeU32(number);
                aos.close();
                try (MemoryInputStream mis = new MemoryInputStream(baos.toByteArray());
                        ABCInputStream ais = new ABCInputStream(mis);) {
                    assertEquals(number, ais.readU32("test"));
                    assertEquals(0, mis.available());
                }
            } catch (IOException ex) {
                fail();
            }
        }
    }

    @Test
    public void testS32() {
        for (long number : getTestNumbers(-(1L << 31), (1 << 31) - 1)) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ABCOutputStream aos = new ABCOutputStream(baos);) {
                aos.writeS32(number);
                aos.close();
                try (MemoryInputStream mis = new MemoryInputStream(baos.toByteArray());
                        ABCInputStream ais = new ABCInputStream(mis);) {
                    assertEquals(number, ais.readS32("test"));
                    assertEquals(0, mis.available());
                }
            } catch (IOException ex) {
                fail();
            }
        }
    }
}
