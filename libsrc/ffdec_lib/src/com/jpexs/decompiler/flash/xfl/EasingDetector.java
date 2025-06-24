/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.math.BezierEdge;
import java.util.List;

/**
 * Easing detector.
 *
 * @author JPEXS
 */
public class EasingDetector {

    public static double framePercentToRatio(double framePercent, int ease) {
        BezierEdge be = new BezierEdge(0, 0, 50, 65535 / 2 + ease * 65535 / 100 / 2, 100, 65535);
        BezierEdge line = new BezierEdge(framePercent, 0, framePercent, 65535);
        return be.getIntersections(line).get(0).getY();
    }

    public static double ratioToFramePercent(double ratio, int ease) {
        BezierEdge be = new BezierEdge(0, 0, 50, 50 + ease / 2.0, 100, 65535);
        BezierEdge line = new BezierEdge(0, ratio, 100, ratio);
        return be.getIntersections(line).get(0).getX();
    }

    public static Integer getEaseFromShapeRatios(List<Integer> ratios) {
        if (ratios.isEmpty()) {
            return null;
        }
        if (ratios.get(0) != 0) {
            ratios.add(0, 0);
        }
        if (ratios.get(ratios.size() - 1) != 65535) {
            ratios.add(65535);
        }

        int ease = 100;
        while (true) {
            double minDist = Double.MAX_VALUE;
            double maxDist = Double.MIN_VALUE;

            for (int f = 0; f < ratios.size(); f++) {
                double framePct = f * 100 / (double) (ratios.size() - 1);
                double tweenPct = ratios.get(f);
                double tweenPctShouldBe = Math.round(framePercentToRatio(framePct, ease));
                double dist = tweenPctShouldBe - tweenPct;

                if (dist > maxDist) {
                    maxDist = dist;
                }
                if (dist < minDist) {
                    minDist = dist;
                }
            }
            if (minDist > -5 && maxDist < 5) {
                break;
            }
            if (ease == -100) {
                return null;
            }
            ease--;
        }
        return ease;
    }
}
