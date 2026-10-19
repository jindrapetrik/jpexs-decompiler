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
package com.jpexs.decompiler.flash.xfl;
import com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdjustColorConversionTest {
    @Test
    public void preservesZeroHueAcrossFloatSaturationMatrices() {
        for (int saturation=-99; saturation<=100; saturation++) {
            for (int contrast:new int[]{-30,0,30}) {
                ColorMatrixConvertor source=new ColorMatrixConvertor();
                source.setBrightness(17);source.setContrast(contrast);source.setSaturation(saturation);
                ColorMatrixConvertor inverse=new ColorMatrixConvertor(source.getMatrix());
                Assert.assertEquals(inverse.getHue(),0,"saturation="+saturation+" contrast="+contrast);
                Assert.assertEquals(inverse.getBrightness(),17);
                Assert.assertEquals(inverse.getContrast(),contrast);
                Assert.assertEquals(inverse.getSaturation(),saturation);
                ColorMatrixConvertor rebuilt=new ColorMatrixConvertor();
                rebuilt.setBrightness(inverse.getBrightness());rebuilt.setContrast(inverse.getContrast());
                rebuilt.setSaturation(inverse.getSaturation());rebuilt.setHue(inverse.getHue());
                for(int i=0;i<20;i++)Assert.assertEquals(rebuilt.getMatrix()[i],source.getMatrix()[i]);
            }
        }
    }
    @Test
    public void preservesCombinedAdjustParameters() {
        for(int f=0;f<=60;f++) {
            double t=f/60.0,q=t+.5*(t<.5?t:1-t)*(1-2*t);
            ColorMatrixConvertor source=new ColorMatrixConvertor();
            source.setBrightness((int)Math.round(-40+80*t*t*t));source.setContrast((int)Math.round(-30+60*t*t));
            source.setSaturation((int)Math.round(-40+80*q));source.setHue((int)Math.round(-90+180*t));
            ColorMatrixConvertor inverse=new ColorMatrixConvertor(source.getMatrix());
            Assert.assertEquals(inverse.getBrightness(),source.getBrightness());Assert.assertEquals(inverse.getContrast(),source.getContrast());
            Assert.assertEquals(inverse.getSaturation(),source.getSaturation());Assert.assertEquals(inverse.getHue(),source.getHue());
        }
    }
}
