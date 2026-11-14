/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BitmapStrokeCapsTest {

    @Test
    public void msaaSquareCapFollowsCurveTangent() throws IOException, InterruptedException {
        SWF swf;
        try (FileInputStream input = new FileInputStream("testdata/caps/caps.swf")) {
            swf = new SWF(input, false, "UTF-8");
        }
        BufferedImage image = SWF.frameToImageGet(swf.getTimeline(), 0, 0, null, 0,
                swf.displayRect, new Matrix(), null, Color.WHITE, 1.0, true, 4).getBufferedImage();

        // Pixels just inside and outside the sloped edge of the magenta square cap.
        Assert.assertEquals(image.getRGB(32, 116), new Color(231, 17, 219).getRGB());
        Assert.assertEquals(image.getRGB(54, 125), Color.WHITE.getRGB());
    }
}
