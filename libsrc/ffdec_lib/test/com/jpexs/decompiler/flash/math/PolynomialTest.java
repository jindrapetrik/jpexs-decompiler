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

import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class PolynomialTest {

    @Test
    public void testRootFindingIsScaleInvariant() {
        Polynomial regular = new Polynomial(Arrays.asList(1.0, -0.5));
        Polynomial scaled = new Polynomial(Arrays.asList(1e-13, -5e-14));
        List<Double> regularRoots = regular.getRoots();
        List<Double> scaledRoots = scaled.getRoots();
        assertEquals(regularRoots.size(), 1);
        assertEquals(scaledRoots.size(), 1);
        assertEquals(scaledRoots.get(0), regularRoots.get(0), 1e-12);
    }
}
