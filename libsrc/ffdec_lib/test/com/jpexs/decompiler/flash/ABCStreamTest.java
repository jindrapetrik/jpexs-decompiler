/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

    @Test
    public void testU30() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ABCOutputStream aos = new ABCOutputStream(baos);) {
            long number = 1531;
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
