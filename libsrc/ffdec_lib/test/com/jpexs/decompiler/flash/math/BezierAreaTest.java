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
package com.jpexs.decompiler.flash.math;

import com.jpexs.decompiler.flash.math.area.BezierArea;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BezierAreaTest {

    @Test
    public void largeCurvedAreaHasStableContourSegments() {
        Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        // Stagger the curved contours so many edges are active in the same sweep rows.
        for (int i = 0; i < 48; i++) {
            int x = i * 10;
            path.moveTo(x, i);
            path.quadTo(x + 2, i - 1.5, x + 5, i);
            path.lineTo(x + 5, i + 80);
            path.lineTo(x, i + 80);
            path.closePath();
        }
        BezierArea area = new BezierArea(path);
        Assert.assertEquals(segmentFingerprint(area.getPathIterator(null)), 0x37c7941d9da8235fL);
        Assert.assertTrue(area.contains(1, 40));
        Assert.assertFalse(area.contains(7, 40));
    }

    @Test
    public void windingRulesAndBooleanOperationsPreserveHoles() {
        Rectangle2D outer = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D inner = new Rectangle2D.Double(25, 25, 50, 50);
        Path2D nested = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        nested.append(outer, false);
        nested.append(inner, false);

        BezierArea withHole = new BezierArea(nested);
        Assert.assertTrue(withHole.contains(10, 10));
        Assert.assertFalse(withHole.contains(50, 50));

        nested.setWindingRule(Path2D.WIND_NON_ZERO);
        BezierArea full = new BezierArea(nested);
        Assert.assertTrue(full.contains(50, 50));

        BezierArea innerArea = new BezierArea(inner);
        BezierArea intersection = new BezierArea(withHole);
        intersection.intersect(innerArea);
        Assert.assertTrue(intersection.isEmpty());

        BezierArea difference = new BezierArea(full);
        difference.subtract(innerArea);
        difference.exclusiveOr(withHole);
        Assert.assertTrue(difference.isEmpty());
        Assert.assertTrue(full.contains(50, 50));
    }

    private static long segmentFingerprint(PathIterator iterator) {
        int[] coordinateCounts = {2, 2, 4, 6, 0};
        double[] coordinates = new double[6];
        long hash = 0;
        for (; !iterator.isDone(); iterator.next()) {
            int segment = iterator.currentSegment(coordinates);
            hash = 31 * hash + segment;
            for (int i = 0; i < coordinateCounts[segment]; i++) {
                hash = 31 * hash + Double.doubleToLongBits(coordinates[i]);
            }
        }
        return hash;
    }
}
