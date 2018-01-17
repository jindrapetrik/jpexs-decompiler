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

import com.jpexs.helpers.FileHashMap;
import java.io.File;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author JPEXS
 */
public class FileHashMapTest {

    @Test
    public void testFileHashMap() throws Exception {
        File tfile = new File("fmtest.bin");
        FileHashMap<String, String> map = new FileHashMap<>(tfile);
        assertTrue(map.isEmpty());
        assertEquals(map.size(), 0);
        map.put("A", "cat");
        assertEquals(map.get("A"), "cat");
        assertNull(map.get("B"));
        map.put("B", "dog");
        assertEquals(map.get("B"), "dog");
        assertEquals(map.get("A"), "cat");
        map.put("C", "parrot");
        assertTrue(map.containsKey("A"));
        assertTrue(map.containsKey("B"));
        assertTrue(map.containsKey("C"));
        assertFalse(map.containsKey("D"));
        assertEquals(map.size(), 3);
        map.remove("A");
        assertFalse(map.containsKey("A"));
        map.put("X", "tac");
        map.delete();
        try {
            map.get("A");
            fail();
        } catch (NullPointerException nfe) {
            //okay
        }

    }
}
