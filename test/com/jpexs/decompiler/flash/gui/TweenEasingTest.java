/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.event.MouseEvent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class TweenEasingTest {

    @Test
    public void linearBezierKeepsProgress() {
        assertEquals(TweenEasing.ease(0, 0, 0, 1, 1), 0, 0.00001);
        assertEquals(TweenEasing.ease(0.25, 0, 0, 1, 1), 0.25, 0.00001);
        assertEquals(TweenEasing.ease(0.5, 0, 0, 1, 1), 0.5, 0.00001);
        assertEquals(TweenEasing.ease(1, 0, 0, 1, 1), 1, 0.00001);
    }

    @Test
    public void easeInStartsSlowerAndEaseOutStartsFaster() {
        assertTrue(TweenEasing.ease(0.25, -100) < 0.25);
        assertTrue(TweenEasing.ease(0.25, 100) > 0.25);
        assertEquals(TweenEasing.ease(0.25, 0), 0.25, 0.00001);
        assertEquals(TweenEasing.ease(0.25, -100), 0.0625, 0.00001);
        assertEquals(TweenEasing.ease(0.25, 100), 0.4375, 0.00001);
    }

    @Test
    public void customEaseStartsAsLinearCurve() {
        CustomEasePanel panel = new CustomEasePanel(10, "Frames", "Tween", "Frame:");
        assertEquals(panel.getValue(0), 0, 0.00001);
        assertEquals(panel.getValue(0.25), 0.25, 0.00001);
        assertEquals(panel.getValue(0.5), 0.5, 0.00001);
        assertEquals(panel.getValue(1), 1, 0.00001);
    }

    @Test
    public void customEasePointCanBeAddedAndDragged() {
        CustomEasePanel panel = new CustomEasePanel(10, "Frames", "Tween", "Frame:");
        panel.setSize(520, 250);
        panel.dispatchEvent(mouseEvent(panel, MouseEvent.MOUSE_PRESSED, 291, 109));
        panel.dispatchEvent(mouseEvent(panel, MouseEvent.MOUSE_DRAGGED, 291, 63));
        panel.dispatchEvent(mouseEvent(panel, MouseEvent.MOUSE_RELEASED, 291, 63));

        assertEquals(panel.getValue(0.5), 0.75, 0.02);

        CustomEasePanel restored = new CustomEasePanel(10, "Frames", "Tween", "Frame:");
        restored.setCurveData(panel.getCurveData());
        assertEquals(restored.getValue(0.25), panel.getValue(0.25), 0.00001);
        assertEquals(restored.getValue(0.5), panel.getValue(0.5), 0.00001);
        assertEquals(restored.getValue(0.75), panel.getValue(0.75), 0.00001);
    }

    private MouseEvent mouseEvent(CustomEasePanel panel, int id, int x, int y) {
        return new MouseEvent(panel, id, System.currentTimeMillis(), 0, x, y, 1, false,
                MouseEvent.BUTTON1);
    }

    @Test
    public void interpolatesAllMatrixComponentsAndNormalizesFlags() {
        MATRIX start = new MATRIX();
        start.translateX = 10;
        start.translateY = -10;

        MATRIX end = new MATRIX();
        end.hasScale = true;
        end.scaleX = 2;
        end.scaleY = 3;
        end.hasRotate = true;
        end.rotateSkew0 = 0.5f;
        end.rotateSkew1 = -0.25f;
        end.translateX = 30;
        end.translateY = 10;

        MATRIX middle = TweenEasing.interpolate(start, end, 0.5);
        assertEquals(middle.translateX, 20);
        assertEquals(middle.translateY, 0);
        assertEquals(middle.scaleX, 1.5f, 0.00001f);
        assertEquals(middle.scaleY, 2f, 0.00001f);
        assertEquals(middle.rotateSkew0, 0.25f, 0.00001f);
        assertEquals(middle.rotateSkew1, -0.125f, 0.00001f);
        assertTrue(middle.hasScale);
        assertTrue(middle.hasRotate);

        MATRIX identity = TweenEasing.interpolate(new MATRIX(), new MATRIX(), 0.5);
        assertFalse(identity.hasScale);
        assertFalse(identity.hasRotate);
    }

    @Test
    public void interpolatesColorTransformFromIdentity() {
        CXFORMWITHALPHA end = new CXFORMWITHALPHA();
        end.hasAddTerms = true;
        end.redAddTerm = 100;
        end.alphaAddTerm = -40;
        end.hasMultTerms = true;
        end.redMultTerm = 128;
        end.greenMultTerm = 64;
        end.blueMultTerm = 256;
        end.alphaMultTerm = 0;

        CXFORMWITHALPHA middle = TweenEasing.interpolate(null, end, 0.5);
        assertEquals(middle.redAddTerm, 50);
        assertEquals(middle.alphaAddTerm, -20);
        assertEquals(middle.redMultTerm, 192);
        assertEquals(middle.greenMultTerm, 160);
        assertEquals(middle.blueMultTerm, 256);
        assertEquals(middle.alphaMultTerm, 128);
        assertTrue(middle.hasAddTerms);
        assertTrue(middle.hasMultTerms);
    }

    @Test
    public void interpolatesRatio() {
        assertEquals(TweenEasing.interpolate(0, 65535, 0), 0);
        assertEquals(TweenEasing.interpolate(0, 65535, 0.5), 32768);
        assertEquals(TweenEasing.interpolate(0, 65535, 1), 65535);
    }
}
