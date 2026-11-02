/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeEyedropperTest.gradient;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.JTriStateCheckBox;
import com.jpexs.decompiler.flash.easygui.properties.panels.BrushPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.DrawingShapesPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.DrawingStyleSettings;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapeDrawingSettingsTest {

    private static final Rectangle2D BOUNDS = new Rectangle2D.Double(100, 200, 3000, 2000);

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static void solid(Object panel, String editor, Color color) throws Exception {
        Object fill = field(panel, editor);
        Method method = fill.getClass().getDeclaredMethod("setSolidColor", Color.class);
        method.setAccessible(true);
        method.invoke(fill, color);
    }

    private static JComboBox<?> type(Object panel, String editor) throws Exception {
        return (JComboBox<?>) field(field(panel, editor), "typeComboBox");
    }

    private static class Settings {

        final BrushPropertiesPanel brush = new BrushPropertiesPanel();
        final ShapePropertiesPanel pencil = new ShapePropertiesPanel(true);
        final DrawingShapesPropertiesPanel shapes = new DrawingShapesPropertiesPanel();
        final ShapePropertiesPanel paint = ShapePropertiesPanel.createFillSettings();
        final ShapePropertiesPanel ink = new ShapePropertiesPanel(true);
        final ShapePropertiesPanel pen = ShapePropertiesPanel.createDrawingStyles();
        final DrawingStyleSettings shared = new DrawingStyleSettings(brush, pencil, shapes, paint, ink, pen, () -> { });

        ShapePropertiesPanel styles() throws Exception {
            return (ShapePropertiesPanel) field(shapes, "styles");
        }

        void assertFill(Color color) {
            assertEquals(brush.getFillStyle(BOUNDS).color.toColor(), color);
            assertEquals(paint.getFillStyle(BOUNDS).color.toColor(), color);
            assertEquals(shapes.getFill(BOUNDS).color.toColor(), color);
            assertEquals(pen.getFillStyle(BOUNDS).color.toColor(), color);
        }
    }

    @Test
    public void fillsAreSharedInBothDirectionsAndBrushRestoresTheLastNonemptyFill() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Settings settings = new Settings();
                solid(settings.paint, "fillEditor", Color.red);
                settings.assertFill(Color.red);
                solid(settings.brush, "fillEditor", Color.blue);
                settings.assertFill(Color.blue);
                solid(settings.styles(), "fillEditor", new Color(20, 30, 40, 50));
                settings.assertFill(new Color(20, 30, 40, 50));
                solid(settings.pen, "fillEditor", Color.green);
                settings.assertFill(Color.green);
                type(settings.styles(), "fillEditor").setSelectedIndex(0);
                assertNull(settings.paint.getFillStyle(BOUNDS));
                assertNull(settings.shapes.getFill(BOUNDS));
                assertNotNull(settings.brush.getFillStyle(BOUNDS));
                settings.shared.activateBrush();
                settings.assertFill(Color.green);
                // A line has no fill, but choosing it must not change shared paint.
                Map<?, ?> buttons = (Map<?, ?>) field(settings.shapes, "kindButtons");
                ((JToggleButton) buttons.get(DrawingShape.LINE)).doClick();
                assertNull(settings.shapes.getFill(BOUNDS));
                ((JToggleButton) buttons.get(DrawingShape.RECTANGLE)).doClick();
                settings.assertFill(Color.green);
                assertEquals(settings.ink.getStrokeStyle().color.toColor(), Color.black);
                settings.shared.setSwf(null);
                settings.assertFill(Color.green);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        });
    }

    @Test
    public void strokesShareWidthScalingCapsJoinMiterAndNoneWithoutChangingFill() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Settings settings = new Settings();
                LINESTYLE2 sampled = new LINESTYLE2();
                sampled.color = new RGBA(Color.orange);
                sampled.width = 97;
                sampled.noHScaleFlag = true;
                sampled.noVScaleFlag = true;
                sampled.startCapStyle = LINESTYLE2.SQUARE_CAP;
                sampled.endCapStyle = LINESTYLE2.NO_CAP;
                sampled.joinStyle = LINESTYLE2.MITER_JOIN;
                sampled.miterLimitFactor = 7.5f;
                settings.shared.setSampledStroke(sampled);
                sampled.width = 999;
                for (ShapePropertiesPanel panel : new ShapePropertiesPanel[]{settings.pencil, settings.ink, settings.styles(), settings.pen}) {
                    LINESTYLE2 line = panel.getStrokeStyle();
                    assertEquals(line.width, 97);
                    assertTrue(line.noHScaleFlag);
                    assertTrue(line.noVScaleFlag);
                    assertEquals(line.startCapStyle, LINESTYLE2.SQUARE_CAP);
                    assertEquals(line.endCapStyle, LINESTYLE2.NO_CAP);
                    assertEquals(line.joinStyle, LINESTYLE2.MITER_JOIN);
                    assertEquals(line.miterLimitFactor, 7.5f);
                }
                ((FloatPropertyField) field(settings.pencil, "widthEditor")).setValue(12.5f);
                assertEquals(settings.ink.getStrokeStyle().width, 250);
                assertEquals(settings.shapes.getStroke(BOUNDS).width, 250);
                solid(settings.ink, "strokeEditor", Color.magenta);
                assertEquals(settings.pencil.getStrokeStyle().color.toColor(), Color.magenta);
                assertEquals(settings.shapes.getStroke(BOUNDS).color.toColor(), Color.magenta);
                type(settings.styles(), "strokeEditor").setSelectedIndex(0);
                assertNull(settings.pencil.getStrokeStyle());
                assertNull(settings.ink.getStrokeStyle());
                ((FloatPropertyField) field(settings.ink, "widthEditor")).setValue(8f);
                assertEquals(settings.pencil.getDrawingStrokeStyle().width, 160);
                assertEquals(settings.styles().getDrawingStrokeStyle().width, 160);
                type(settings.pencil, "strokeEditor").setSelectedIndex(1);
                assertEquals(settings.ink.getStrokeStyle().width, 160);
                assertEquals(settings.shapes.getStroke(BOUNDS).miterLimitFactor, 7.5f);
                settings.assertFill(Color.black);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        });
    }

    @Test
    public void gradientsAndBitmapsKeepTheirSettingsAndNewGradientsStillFitTheDrawnGeometry() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                Settings settings = new Settings();
                DefineShape4Tag shape = ShapeSelectionTest.shape();
                DefineBitsLossless2Tag bitmap = new DefineBitsLossless2Tag(shape.getSwf());
                shape.getSwf().addTag(bitmap);
                settings.shared.setSwf(shape.getSwf());
                type(settings.paint, "fillEditor").setSelectedIndex(2);
                Object editor = field(settings.paint, "fillEditor");
                ((JComboBox<?>) field(editor, "flowComboBox")).setSelectedIndex(GRADIENT.SPREAD_REFLECT_MODE);
                ((JTriStateCheckBox) field(editor, "linearRGBCheckBox")).doClick();
                settings.shared.setSwf(shape.getSwf());
                for (FILLSTYLE fill : new FILLSTYLE[]{settings.brush.getFillStyle(BOUNDS),
                    settings.shapes.getFill(BOUNDS), settings.paint.getFillStyle(BOUNDS)}) {
                    assertEquals(fill.fillStyleType, FILLSTYLE.LINEAR_GRADIENT);
                    assertEquals(fill.gradient.spreadMode, GRADIENT.SPREAD_REFLECT_MODE);
                    assertEquals(fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
                    assertEquals(fill.gradientMatrix.scaleX, (float) (BOUNDS.getWidth() / 32768));
                    assertEquals(fill.gradientMatrix.translateX, (int) BOUNDS.getCenterX());
                }
                assertNull(settings.paint.getFillStyle(null).gradientMatrix);
                FILLSTYLE sampled = gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                settings.shared.setSampledFill(sampled);
                assertEquals(settings.brush.getFillStyle(BOUNDS).gradientMatrix.translateX, 234);
                assertEquals(settings.shapes.getFill(BOUNDS).gradientMatrix.scaleY, 0.25f);
                sampled.gradientMatrix.translateX = 12345;
                assertEquals(settings.paint.getFillStyle(BOUNDS).gradientMatrix.translateX, 234);
                LINESTYLE2 stroke = new LINESTYLE2();
                stroke.width = 80;
                stroke.hasFillFlag = true;
                stroke.fillType = sampled;
                settings.shared.setSampledStroke(stroke);
                ((JComboBox<?>) field(field(settings.pencil, "strokeEditor"), "flowComboBox")).setSelectedIndex(GRADIENT.SPREAD_REPEAT_MODE);
                assertEquals(settings.ink.getStrokeStyle().fillType.gradient.spreadMode, GRADIENT.SPREAD_REPEAT_MODE);
                assertEquals(settings.shapes.getStroke(BOUNDS).fillType.gradientMatrix.translateX, 12345);
                FILLSTYLE fill = new FILLSTYLE();
                fill.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
                fill.bitmapId = bitmap.getCharacterId();
                fill.bitmapMatrix = new MATRIX();
                fill.bitmapMatrix.translateX = 789;
                settings.shared.setSampledFill(fill);
                assertTrue(settings.brush.getFillStyle(BOUNDS).hasBitmap());
                assertEquals(settings.shapes.getFill(BOUNDS).bitmapId, bitmap.getCharacterId());
                assertEquals(settings.paint.getFillStyle(BOUNDS).bitmapMatrix.translateX, 789);
                ((JTriStateCheckBox) field(field(settings.brush, "fillEditor"), "smoothedCheckBox")).doClick();
                for (FILLSTYLE paint : new FILLSTYLE[]{settings.brush.getFillStyle(BOUNDS), settings.paint.getFillStyle(BOUNDS),
                    settings.shapes.getFill(BOUNDS), settings.pen.getFillStyle(BOUNDS)}) {
                    assertEquals(paint.fillStyleType, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                    assertEquals(paint.bitmapId, bitmap.getCharacterId());
                    assertEquals(paint.bitmapMatrix, fill.bitmapMatrix);
                }
                stroke.fillType = fill;
                settings.shared.setSampledStroke(stroke);
                ((JTriStateCheckBox) field(field(settings.ink, "strokeEditor"), "smoothedCheckBox")).doClick();
                for (LINESTYLE2 ink : new LINESTYLE2[]{settings.ink.getStrokeStyle(BOUNDS), settings.pencil.getStrokeStyle(BOUNDS),
                    settings.shapes.getStroke(BOUNDS), settings.pen.getStrokeStyle(BOUNDS)}) {
                    assertEquals(ink.fillType.fillStyleType, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                    assertEquals(ink.fillType.bitmapId, bitmap.getCharacterId());
                    assertEquals(ink.fillType.bitmapMatrix, fill.bitmapMatrix);
                    assertEquals(ink.width, stroke.width);
                }
                assertEquals(settings.ink.applyStrokeChange(stroke, BOUNDS, "smoothed").fillType.fillStyleType,
                        FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                ((JTriStateCheckBox) field(field(settings.paint, "fillEditor"), "repeatCheckBox")).doClick();
                for (FILLSTYLE paint : new FILLSTYLE[]{settings.brush.getFillStyle(BOUNDS), settings.paint.getFillStyle(BOUNDS),
                    settings.shapes.getFill(BOUNDS), settings.pen.getFillStyle(BOUNDS)}) {
                    assertEquals(paint.fillStyleType, FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP);
                    assertEquals(paint.bitmapId, bitmap.getCharacterId());
                    assertEquals(paint.bitmapMatrix, fill.bitmapMatrix);
                }
                assertEquals(settings.ink.getStrokeStyle(BOUNDS).fillType.fillStyleType, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                ((JTriStateCheckBox) field(field(settings.pencil, "strokeEditor"), "repeatCheckBox")).doClick();
                for (LINESTYLE2 ink : new LINESTYLE2[]{settings.ink.getStrokeStyle(BOUNDS), settings.pencil.getStrokeStyle(BOUNDS),
                    settings.shapes.getStroke(BOUNDS), settings.pen.getStrokeStyle(BOUNDS)}) {
                    assertEquals(ink.fillType.fillStyleType, FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP);
                    assertEquals(ink.fillType.bitmapId, bitmap.getCharacterId());
                    assertEquals(ink.fillType.bitmapMatrix, fill.bitmapMatrix);
                    assertEquals(ink.width, stroke.width);
                }
                assertEquals(settings.ink.applyStrokeChange(stroke, BOUNDS, "repeat").fillType.fillStyleType, FILLSTYLE.REPEATING_BITMAP);
                settings.shared.setSwf(null);
                assertFalse(settings.paint.getFillStyle(BOUNDS).hasBitmap());
                settings.assertFill(Color.white);
            } catch (Exception ex) {
                throw new AssertionError(ex);
            }
        });
    }
}
