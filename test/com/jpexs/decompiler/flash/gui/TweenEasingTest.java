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
import com.jpexs.decompiler.flash.types.RECT;
import java.awt.event.MouseEvent;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.DataProvider;
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
    public void interpolatesScaleAndTranslationAndNormalizesFlags() {
        MATRIX start = new MATRIX();
        start.translateX = 10;
        start.translateY = -10;

        MATRIX end = new MATRIX();
        end.hasScale = true;
        end.scaleX = 2;
        end.scaleY = 3;
        end.translateX = 30;
        end.translateY = 10;

        MATRIX middle = TweenEasing.interpolate(start, end, 0.5);
        assertEquals(middle.translateX, 20);
        assertEquals(middle.translateY, 0);
        assertEquals(middle.scaleX, 1.5f, 0.00001f);
        assertEquals(middle.scaleY, 2f, 0.00001f);
        assertEquals(middle.rotateSkew0, 0f, 0.00001f);
        assertEquals(middle.rotateSkew1, 0f, 0.00001f);
        assertTrue(middle.hasScale);
        assertFalse(middle.hasRotate);

        MATRIX identity = TweenEasing.interpolate(new MATRIX(), new MATRIX(), 0.5);
        assertFalse(identity.hasScale);
        assertFalse(identity.hasRotate);
    }

    @DataProvider(name = "rotations")
    public Object[][] rotations() {
        return new Object[][]{
            {0.0, 180.0, 90.0}, {0.0, -180.0, -90.0},
            {0.0, 72.0, 36.0}, {72.0, 0.0, 36.0},
            {170.0, -170.0, 180.0}, {-170.0, 170.0, -180.0},
            {90.0, -90.0, 0.0}, {179.4, -0.5, 89.45}
        };
    }

    @Test(dataProvider = "rotations")
    public void rotationKeepsSizeAndUsesShortestPath(double start, double end, double middle) {
        // Also cover unequal axis scales, skew and reflected objects.
        for (double scaleY : new double[]{3, -3}) {
            for (double skew : new double[]{0, 20}) {
                MATRIX first = matrix(start, 2, scaleY, skew);
                MATRIX last = matrix(end, 2, scaleY, skew);
                assertMatrixEquals(TweenEasing.interpolate(first, last, 0.5),
                        matrix(middle, 2, scaleY, skew));
                for (int frame = 1; frame < 10; frame++) {
                    MATRIX value = TweenEasing.interpolate(first, last, frame / 10.0);
                    assertEquals(Math.hypot(value.scaleX, value.rotateSkew0), 2, 0.000001);
                    assertEquals(Math.hypot(value.rotateSkew1, value.scaleY), 3, 0.000001);
                    assertEquals(determinant(value), 2 * scaleY * Math.cos(Math.toRadians(skew)), 0.000002);
                }
            }
        }
    }

    @Test
    public void interpolatesScaleAndSkewSeparatelyFromRotation() {
        assertMatrixEquals(TweenEasing.interpolate(matrix(10, 2, 3, 20),
                matrix(70, 4, 5, 40), 0.5), matrix(40, 3, 4, 30));
        assertMatrixEquals(TweenEasing.interpolate(matrix(10, 2, -3, 20),
                matrix(70, 4, -5, 40), 0.5), matrix(40, 3, -4, 30));
        // Crossing between reflected and unreflected states collapses one axis.
        assertMatrixEquals(TweenEasing.interpolate(matrix(30, 2, 3, 0),
                matrix(30, 2, -3, 0), 0.5), matrix(30, 2, 0, 0));
    }

    @Test
    public void collapsedAxesHaveFiniteInterpolationAndEndpointsStayExact() {
        for (MATRIX collapsed : new MATRIX[]{matrix(0, 0, 0, 0),
            matrix(30, 0, 3, 0), matrix(30, 2, 0, 0)}) {
            MATRIX other = matrix(30, 2, 3, 0);
            assertMatrixEquals(TweenEasing.interpolate(collapsed, other, 0), collapsed);
            assertMatrixEquals(TweenEasing.interpolate(collapsed, other, 1), other);
            assertMatrixEquals(TweenEasing.interpolate(collapsed, other, 0.5),
                    matrix(30, Math.hypot(collapsed.scaleX, collapsed.rotateSkew0) / 2 + 1,
                            Math.hypot(collapsed.rotateSkew1, collapsed.scaleY) / 2 + 1.5, 0));
            assertMatrixEquals(TweenEasing.interpolate(other, collapsed, 0.5),
                    TweenEasing.interpolate(collapsed, other, 0.5));
        }
    }

    @Test(dataProvider = "rotations")
    public void rotationKeepsObjectCenter(double start, double end, double middle) {
        // Geometry can be far from the character's local origin, with a center
        // on a half twip. Interpolating translation alone makes it orbit.
        RECT bounds = new RECT(12001, 14000, 8000, 9001);
        for (double scaleY : new double[]{3, -3}) {
            MATRIX first = positionCenter(matrix(start, 2, scaleY, 20), bounds, 5000, -4000);
            MATRIX last = positionCenter(matrix(end, 2, scaleY, 20), bounds, 5000, -4000);
            for (int frame = 0; frame <= 10; frame++) {
                double progress = frame / 10.0;
                MATRIX value = TweenEasing.interpolate(first, last, progress, bounds);
                assertCenterOnPath(value, first, last, bounds, progress);
                assertEquals(Math.hypot(value.getScaleXFloat(), value.getRotateSkew0Float()), 2, 0.000001);
                assertEquals(Math.hypot(value.getRotateSkew1Float(), value.getScaleYFloat()), 3, 0.000001);
            }
        }
    }

    @Test
    public void centeredTweenInterpolatesPositionWithScaleSkewAndEasing() {
        RECT bounds = new RECT(-9000, -8001, 15000, 17001);
        MATRIX first = positionCenter(matrix(10, 2, 3, 20), bounds, 1000, -2000);
        MATRIX last = positionCenter(matrix(70, 4, 5, 40), bounds, 3000, 4000);
        for (int easing : new int[]{-100, 0, 100}) {
            for (int frame = 1; frame < 10; frame++) {
                double progress = TweenEasing.ease(frame / 10.0, easing);
                MATRIX value = TweenEasing.interpolate(first, last, progress, bounds);
                assertCenterOnPath(value, first, last, bounds, progress);
            }
        }
        MATRIX middle = TweenEasing.interpolate(first, last, 0.5, bounds);
        MATRIX expected = matrix(40, 3, 4, 30);
        expected.translateX = middle.translateX;
        expected.translateY = middle.translateY;
        assertMatrixEquals(middle, expected);
    }

    @Test
    public void centeredInterpolationPreservesEndpointsAndSupportsMissingBounds() {
        RECT bounds = new RECT(1000, 2000, 3000, 4000);
        MATRIX first = new MATRIX();
        MATRIX last = positionCenter(matrix(72, 2, 3, 20), bounds, 5000, -4000);
        assertEquals(TweenEasing.interpolate(first, last, 0, bounds), first);
        assertEquals(TweenEasing.interpolate(first, last, 1, bounds), last);
        assertMatrixEquals(TweenEasing.interpolate(first, last, 0.5, null),
                TweenEasing.interpolate(first, last, 0.5));
        MATRIX translation = new MATRIX();
        translation.translateX = 100;
        translation.translateY = -200;
        assertMatrixEquals(TweenEasing.interpolate(first, translation, 0.5, bounds),
                TweenEasing.interpolate(first, translation, 0.5));
    }

    @DataProvider(name = "transformationPoints")
    public Object[][] transformationPoints() {
        return new Object[][]{
            {RegistrationPointPosition.TOP_LEFT, 12001.0, 8000.0},
            {RegistrationPointPosition.TOP, 13000.5, 8000.0},
            {RegistrationPointPosition.TOP_RIGHT, 14000.0, 8000.0},
            {RegistrationPointPosition.LEFT, 12001.0, 8500.5},
            {RegistrationPointPosition.CENTER, 13000.5, 8500.5},
            {RegistrationPointPosition.RIGHT, 14000.0, 8500.5},
            {RegistrationPointPosition.BOTTOM_LEFT, 12001.0, 9001.0},
            {RegistrationPointPosition.BOTTOM, 13000.5, 9001.0},
            {RegistrationPointPosition.BOTTOM_RIGHT, 14000.0, 9001.0}
        };
    }

    @Test(dataProvider = "transformationPoints")
    public void selectedTransformationPointFollowsEndpointPath(RegistrationPointPosition position, double x, double y) {
        RECT bounds = new RECT(12001, 14000, 8000, 9001);
        for (double scaleX : new double[]{2, -2}) {
            for (double scaleY : new double[]{3, -3}) {
                // Negative scales reverse the displayed sides of the object.
                double localX = scaleX > 0 ? x : 26001 - x;
                double localY = scaleY > 0 ? y : 17001 - y;
                assertTransformationPointFollowsPath(matrix(0, scaleX, scaleY, 0),
                        matrix(170, 4, 5, 40), bounds, position, localX, localY);
            }
        }
        // A quarter turn maps stage X to reversed local Y and stage Y to local X.
        MATRIX quarterTurn = matrix(0, 0, 0, 0);
        quarterTurn.rotateSkew0 = 2;
        quarterTurn.rotateSkew1 = -3;
        assertTransformationPointFollowsPath(quarterTurn, matrix(170, 4, 5, 40), bounds, position,
                12001 + (y - 8000) / 1001 * 1999, 8000 + (14000 - x) / 1999 * 1001);
    }

    @Test
    public void initiallyRotatedObjectUsesStageTopLeft() {
        RECT bounds = new RECT(1000, 3000, 2000, 3000);
        MATRIX first = matrix(0, -2, -2, 0);
        first.rotateSkew0 = 1;
        first.rotateSkew1 = -1;
        // This rotated rectangle's stage bounding-box corner maps outside
        // the local rectangle. It must not be replaced by a local corner.
        assertTransformationPointFollowsPath(first, matrix(-90, 3, 3, 0), bounds,
                RegistrationPointPosition.TOP_LEFT, 2600, 3800);
    }

    @Test
    public void skewedObjectUsesStageTopRight() {
        RECT bounds = new RECT(12001, 14000, 8000, 9001);
        MATRIX first = matrix(0, 2, 3, 0);
        first.rotateSkew0 = 1;
        first.rotateSkew1 = 1;
        assertTransformationPointFollowsPath(first, matrix(70, 4, 5, 40), bounds,
                RegistrationPointPosition.TOP_RIGHT, 15000.4, 7000.2);
    }

    @Test(dataProvider = "transformationPoints")
    public void collapsedStartTransformationPointsRemainFinite(RegistrationPointPosition position, double x, double y) {
        RECT bounds = new RECT(12001, 14000, 8000, 9001);
        for (MATRIX first : new MATRIX[]{matrix(0, 0, 0, 0), matrix(0, 0, 3, 0), matrix(0, 2, 0, 0)}) {
            assertTransformationPointFollowsPath(first, matrix(70, 4, 5, 40), bounds, position, x, y);
        }
    }

    private void assertTransformationPointFollowsPath(MATRIX first, MATRIX last, RECT bounds,
            RegistrationPointPosition position, double x, double y) {
        first.translateX = (int) Math.round(5000 - first.scaleX * x - first.rotateSkew1 * y);
        first.translateY = (int) Math.round(-4000 - first.rotateSkew0 * x - first.scaleY * y);
        last.translateX = (int) Math.round(5000 - last.scaleX * x - last.rotateSkew1 * y);
        last.translateY = (int) Math.round(-4000 - last.rotateSkew0 * x - last.scaleY * y);
        for (int easing : new int[]{-100, 0, 100}) {
            for (int frame = 0; frame <= 10; frame++) {
                double progress = TweenEasing.ease(frame / 10.0, easing);
                MATRIX value = TweenEasing.interpolate(first, last, progress, bounds, position);
                double expectedX = (first.scaleX * x + first.rotateSkew1 * y + first.translateX) * (1 - progress)
                        + (last.scaleX * x + last.rotateSkew1 * y + last.translateX) * progress;
                double expectedY = (first.rotateSkew0 * x + first.scaleY * y + first.translateY) * (1 - progress)
                        + (last.rotateSkew0 * x + last.scaleY * y + last.translateY) * progress;
                assertEquals(value.scaleX * x + value.rotateSkew1 * y + value.translateX, expectedX, 0.50001);
                assertEquals(value.rotateSkew0 * x + value.scaleY * y + value.translateY, expectedY, 0.50001);
            }
        }
        assertEquals(TweenEasing.interpolate(first, last, 0, bounds, position), first);
        assertEquals(TweenEasing.interpolate(first, last, 1, bounds, position), last);
        assertMatrixEquals(TweenEasing.interpolate(first, last, 0.5, null, position),
                TweenEasing.interpolate(first, last, 0.5));
    }

    private MATRIX positionCenter(MATRIX matrix, RECT bounds, double x, double y) {
        matrix.translateX = (int) Math.round(x - transformedCenterX(matrix, bounds));
        matrix.translateY = (int) Math.round(y - transformedCenterY(matrix, bounds));
        return matrix;
    }

    private double transformedCenterX(MATRIX matrix, RECT bounds) {
        return matrix.getScaleXFloat() * ((double) bounds.Xmin + bounds.Xmax) / 2
                + matrix.getRotateSkew1Float() * ((double) bounds.Ymin + bounds.Ymax) / 2 + matrix.translateX;
    }

    private double transformedCenterY(MATRIX matrix, RECT bounds) {
        return matrix.getRotateSkew0Float() * ((double) bounds.Xmin + bounds.Xmax) / 2
                + matrix.getScaleYFloat() * ((double) bounds.Ymin + bounds.Ymax) / 2 + matrix.translateY;
    }

    private void assertCenterOnPath(MATRIX actual, MATRIX first, MATRIX last, RECT bounds, double progress) {
        double x = transformedCenterX(first, bounds) * (1 - progress) + transformedCenterX(last, bounds) * progress;
        double y = transformedCenterY(first, bounds) * (1 - progress) + transformedCenterY(last, bounds) * progress;
        // Translation in a SWF matrix is rounded to whole twips.
        assertEquals(transformedCenterX(actual, bounds), x, 0.50001);
        assertEquals(transformedCenterY(actual, bounds), y, 0.50001);
    }

    private MATRIX matrix(double rotation, double scaleX, double scaleY, double skew) {
        MATRIX result = new MATRIX();
        result.hasScale = true;
        result.hasRotate = true;
        result.scaleX = (float) (scaleX * Math.cos(Math.toRadians(rotation)));
        result.rotateSkew0 = (float) (scaleX * Math.sin(Math.toRadians(rotation)));
        result.rotateSkew1 = (float) (-scaleY * Math.sin(Math.toRadians(rotation + skew)));
        result.scaleY = (float) (scaleY * Math.cos(Math.toRadians(rotation + skew)));
        return result;
    }

    private double determinant(MATRIX matrix) {
        return (double) matrix.getScaleXFloat() * matrix.getScaleYFloat()
                - (double) matrix.getRotateSkew0Float() * matrix.getRotateSkew1Float();
    }

    private void assertMatrixEquals(MATRIX actual, MATRIX expected) {
        assertEquals(actual.getScaleXFloat(), expected.getScaleXFloat(), 0.000001f);
        assertEquals(actual.getScaleYFloat(), expected.getScaleYFloat(), 0.000001f);
        assertEquals(actual.getRotateSkew0Float(), expected.getRotateSkew0Float(), 0.000001f);
        assertEquals(actual.getRotateSkew1Float(), expected.getRotateSkew1Float(), 0.000001f);
        assertEquals(actual.translateX, expected.translateX);
        assertEquals(actual.translateY, expected.translateY);
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
