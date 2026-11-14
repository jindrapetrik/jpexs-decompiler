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
package com.jpexs.decompiler.graph;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Tests for GraphPartMarkedArrayList. */
public class GraphPartMarkedArrayListTest {

    private static GraphPartMarkedArrayList<String> list(String... items) {
        GraphPartMarkedArrayList<String> ret = new GraphPartMarkedArrayList<>();
        for (String item : items) {
            ret.clearCurrentParts();
            ret.startPart(new GraphPart(item.charAt(0), item.charAt(0)));
            ret.add(item);
        }
        return ret;
    }

    @Test
    public void testSubListMiddle() {
        GraphPartMarkedArrayList<String> source = list("a", "b", "c", "d", "e");
        List<String> sub = source.subList(1, 4);
        Assert.assertEquals(sub, Arrays.asList("b", "c", "d"));
        Assert.assertEquals(source, Arrays.asList("a", "b", "c", "d", "e"));
    }

    @Test
    public void testSubListKeepsParts() {
        GraphPartMarkedArrayList<String> source = list("a", "b", "c", "d", "e");
        GraphPartMarkedArrayList<String> sub = (GraphPartMarkedArrayList<String>) source.subList(2, 4);
        Assert.assertEquals(sub, Arrays.asList("c", "d"));
        Assert.assertSame(sub.getPartsAt(0), source.getPartsAt(2));
        Assert.assertSame(sub.getPartsAt(1), source.getPartsAt(3));
        Assert.assertNotSame(source.getPartsAt(2), source.getPartsAt(3));
    }

    @Test
    public void testSubListBounds() {
        GraphPartMarkedArrayList<String> source = list("a", "b", "c");
        Assert.assertEquals(source.subList(0, 3), Arrays.asList("a", "b", "c"));
        Assert.assertEquals(source.subList(0, 0), Arrays.asList());
        Assert.assertEquals(source.subList(3, 3), Arrays.asList());
        Assert.assertEquals(source.subList(2, 3), Arrays.asList("c"));
    }
}
