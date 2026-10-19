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
package com.jpexs.decompiler.flash.math;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class DistancesTest {

    @Test
    public void testBatchDistanceIsSymmetric() {
        List<BezierEdge> small = rectangle(0, 0, 10, 10);
        List<BezierEdge> large = rectangle(0, 0, 100, 100);
        double smallToLarge = Distances.getBatchDistance(small, large);
        double largeToSmall = Distances.getBatchDistance(large, small);
        assertEquals(smallToLarge, largeToSmall, 0.000001);
        assertTrue(smallToLarge > 80);
    }

    private List<BezierEdge> rectangle(double x1, double y1, double x2, double y2) {
        List<BezierEdge> result = new ArrayList<>();
        result.add(new BezierEdge(x1, y1, x2, y1));
        result.add(new BezierEdge(x2, y1, x2, y2));
        result.add(new BezierEdge(x2, y2, x1, y2));
        result.add(new BezierEdge(x1, y2, x1, y1));
        return result;
    }
}
