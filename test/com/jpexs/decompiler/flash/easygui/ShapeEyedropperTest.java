/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.fill;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapeStylePreviewPanel;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.testng.annotations.Test;

public class ShapeEyedropperTest {

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void assertDisplayOnly(Component component) {
        assertTrue(component.isEnabled(), "Preview must retain its normal colors");
        assertFalse(component instanceof AbstractButton || component instanceof JComboBox || component instanceof JTextComponent,
                "Preview must use labels instead of editing controls");
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                assertDisplayOnly(child);
            }
        }
    }

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static ShapeSelection.Selection sample(ShapeSelection model, double x, double y) {
        return model.sampleStyle(point(x, y), new AffineTransform(), 1);
    }

    static FILLSTYLE gradient(int type) {
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = type;
        fill.inShape3 = true;
        fill.gradientMatrix = new MATRIX();
        fill.gradientMatrix.hasScale = true;
        fill.gradientMatrix.scaleX = 0.125f;
        fill.gradientMatrix.scaleY = 0.25f;
        fill.gradientMatrix.translateX = 234;
        fill.gradientMatrix.translateY = 567;
        fill.gradient = type == FILLSTYLE.FOCAL_RADIAL_GRADIENT ? new FOCALGRADIENT() : new GRADIENT();
        if (fill.gradient instanceof FOCALGRADIENT) {
            ((FOCALGRADIENT) fill.gradient).focalPoint = 0.375f;
        }
        fill.gradient.gradientRecords = new GRADRECORD[2];
        for (int i = 0; i < 2; i++) {
            GRADRECORD stop = new GRADRECORD();
            stop.inShape3 = true;
            stop.ratio = i * 255;
            stop.color = new RGBA(i == 0 ? Color.red : new Color(0, 100, 255, 120));
            fill.gradient.gradientRecords[i] = stop;
        }
        return fill;
    }

    @Test
    public void samplesTheVisibleFillOrStrokeWithoutSelectingOrModifyingTheShape() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 1);
        ShapeSelection model = selection(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        ShapeSelection.Selection fill = sample(model, 500, 500);
        assertFalse(fill.stroke);
        assertEquals(fill.fill.color.toColor(), Color.red);
        ShapeSelection.Selection stroke = sample(model, 50, 500);
        assertTrue(stroke.stroke);
        assertEquals(stroke.line.getWidth(), 200);
        assertNull(sample(model, 1500, 500));
        fill.fill.color = new RGBA(Color.blue);
        ((LINESTYLE2) stroke.line).width = 999;
        assertEquals(sample(model, 500, 500).fill.color.toColor(), Color.red);
        assertEquals(sample(model, 50, 500).line.getWidth(), 200);
        assertEquals(shape.getData(), before);
        assertFalse(shape.isModified());
        assertFalse(shape.getSwf().isModified());
        model = model.drawShape(new Rectangle2D.Double(200, 200, 600, 600), fill(Color.blue), null).model;
        assertEquals(sample(model, 500, 500).fill.color.toColor(), Color.blue);
        assertEquals(sample(model, 150, 500).fill.color.toColor(), Color.red);
    }

    @Test
    public void emptyStrokeEnclosuresDoNotSampleANonexistentFill() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 0, 1);
        ShapeSelection model = selection(shape);
        assertNull(sample(model, 500, 500));
        assertTrue(sample(model, 0, 500).stroke);
    }

    @Test
    public void samplesStrokeWidthsInPlacedShapesIncludingNonScalingStrokes() {
        AffineTransform placement = AffineTransform.getTranslateInstance(3000, -1000);
        placement.rotate(Math.PI / 4);
        placement.scale(4, 2);
        for (boolean nonScaling : new boolean[]{false, true}) {
            LINESTYLE2 line = new LINESTYLE2();
            line.color = new RGBA(Color.black);
            line.width = 200;
            line.noHScaleFlag = nonScaling;
            line.noVScaleFlag = nonScaling;
            ShapeSelection model = selection(shape()).drawStroke(new Line2D.Double(0, 0, 2000, 0), line).model;
            assertNotNull(model.sampleStyle(point(1000, 25), placement, 1));
            assertEquals(model.sampleStyle(point(1000, 75), placement, 1) == null, nonScaling);
            assertNotNull(model.sampleStyle(point(1000, 75), placement, 400), "Minimum rendered width must also be pickable");
        }
    }

    @Test
    public void sampledGradientAndBitmapFillsRetainTheirSettingsInPaintProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefineShape4Tag shape = shape();
            ShapeSelection empty = selection(shape);
            DefineBitsLossless2Tag bitmap = new DefineBitsLossless2Tag(shape.getSwf());
            shape.getSwf().addTag(bitmap);
            ShapePropertiesPanel paint = ShapePropertiesPanel.createFillSettings();
            paint.setDrawingSwf(shape.getSwf());
            FILLSTYLE imageFill = new FILLSTYLE();
            imageFill.fillStyleType = FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
            imageFill.bitmapId = bitmap.getCharacterId();
            imageFill.bitmapMatrix = new MATRIX();
            imageFill.bitmapMatrix.translateX = 120;
            imageFill.bitmapMatrix.translateY = -456;
            FILLSTYLE[] fills = {gradient(FILLSTYLE.LINEAR_GRADIENT), gradient(FILLSTYLE.RADIAL_GRADIENT),
                gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT), imageFill};
            for (FILLSTYLE fill : fills) {
                ShapeSelection model = empty.drawShape(new Rectangle2D.Double(0, 0, 1000, 1000), fill, null).model;
                ShapeSelection.Selection sampled = sample(model, 500, 500);
                paint.setSelectedStyles(sampled.fill, null, sampled.geometry, false);
                FILLSTYLE actual = paint.getFillStyle(new Rectangle2D.Double(3000, 4000, 500, 800));
                assertEquals(actual.fillStyleType, fill.fillStyleType);
                if (fill.hasGradient()) {
                    assertEquals(actual.gradientMatrix, fill.gradientMatrix);
                    assertEquals(actual.gradient, fill.gradient);
                    actual.gradient.gradientRecords[0].color = new RGBA(Color.black);
                    assertEquals(sample(model, 500, 500).fill.gradient.gradientRecords[0].color.toColor(), Color.red);
                } else {
                    assertEquals(actual.bitmapId, fill.bitmapId);
                    assertEquals(actual.bitmapMatrix, fill.bitmapMatrix);
                }
            }
        });
    }

    @Test
    public void sampledStrokeRetainsAllParametersAndItsGradientInInkProperties() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LINESTYLE2 line = new LINESTYLE2();
            line.width = 123;
            line.hasFillFlag = true;
            line.fillType = gradient(FILLSTYLE.LINEAR_GRADIENT);
            line.noHScaleFlag = true;
            line.pixelHintingFlag = true;
            line.startCapStyle = LINESTYLE2.SQUARE_CAP;
            line.endCapStyle = LINESTYLE2.NO_CAP;
            line.joinStyle = LINESTYLE2.MITER_JOIN;
            line.miterLimitFactor = 8;
            DefineShape4Tag shape = shape();
            ShapeSelection model = selection(shape).drawStroke(new Line2D.Double(0, 0, 2000, 0), line).model;
            ShapeSelection.Selection sample = sample(model, 1000, 0);
            ShapePropertiesPanel ink = new ShapePropertiesPanel(true);
            ink.setDrawingSwf(shape.getSwf());
            ink.setSelectedStyles(null, sample.line, sample.geometry, true);
            LINESTYLE2 actual = ink.getStrokeStyle();
            assertEquals(actual.width, line.width);
            assertTrue(actual.noHScaleFlag);
            assertFalse(actual.noVScaleFlag);
            assertTrue(actual.pixelHintingFlag);
            assertEquals(actual.startCapStyle, line.startCapStyle);
            assertEquals(actual.endCapStyle, line.endCapStyle);
            assertEquals(actual.joinStyle, line.joinStyle);
            assertEquals(actual.miterLimitFactor, line.miterLimitFactor);
            assertTrue(actual.hasFillFlag);
            assertEquals(actual.fillType.gradientMatrix, line.fillType.gradientMatrix);
            assertEquals(actual.fillType.gradient, line.fillType.gradient);
            actual.fillType.gradientMatrix.translateX = 9999;
            assertEquals(sample(model, 1000, 0).line.getWidth(), 123);
            assertEquals(((LINESTYLE2) sample(model, 1000, 0).line).fillType.gradientMatrix.translateX, 234);
        });
    }

    @Test
    public void previewShowsSampledStylesWithoutAllowingEditsOrChangingTheShape() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                DefineShape4Tag shape = shape();
                rectangle(shape, 0, 0, 1000, 1000, 1, 1);
                ShapeSelection empty = selection(shape);
                DefineBitsLossless2Tag bitmap = new DefineBitsLossless2Tag(shape.getSwf());
                shape.getSwf().addTag(bitmap);
                ShapeStylePreviewPanel preview = new ShapeStylePreviewPanel();
                preview.setShape(shape);
                FILLSTYLE image = new FILLSTYLE();
                image.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
                image.bitmapId = bitmap.getCharacterId();
                image.bitmapMatrix = new MATRIX();
                image.bitmapMatrix.translateX = 123;
                FILLSTYLE[] fills = {fill(Color.black), gradient(FILLSTYLE.LINEAR_GRADIENT),
                    gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT), image};
                shape.setModified(false);
                byte[] before = shape.getData();
                for (FILLSTYLE fill : fills) {
                    fill.inShape3 = true;
                    ShapeSelection model = empty.drawShape(new Rectangle2D.Double(0, 0, 1000, 1000), fill, null).model;
                    preview.setSample(sample(model, 500, 500));
                    Component fillPreview = (Component) field(preview, "fillPreview");
                    assertDisplayOnly(fillPreview);
                    assertDisplayOnly((Component) field(preview, "strokePreview"));
                    JLabel value = (JLabel) field(fillPreview, "valueLabel");
                    if (fill.fillStyleType == FILLSTYLE.SOLID) {
                        assertEquals(value.getText(), "#000000 (100%)");
                        assertNotNull(value.getIcon(), "Solid color must keep its swatch");
                        BufferedImage imageSwatch = (BufferedImage) ((ImageIcon) value.getIcon()).getImage();
                        assertEquals(imageSwatch.getRGB(8, 8), Color.black.getRGB());
                    } else if (fill.hasBitmap()) {
                        assertTrue(value.getText().contains(Integer.toString(image.bitmapId)));
                    }
                    if (fill.hasGradient()) {
                        JPanel details = (JPanel) field(fillPreview, "details");
                        GradientEditor gradient = (GradientEditor) details.getComponent(0);
                        Component swatch = gradient.getComponent(0);
                        MouseEvent press = new MouseEvent(swatch, MouseEvent.MOUSE_PRESSED, 0, 0, 5, 5, 1, false, MouseEvent.BUTTON1);
                        for (MouseListener listener : swatch.getMouseListeners()) {
                            listener.mousePressed(press);
                        }
                        assertEquals(fill.gradient.gradientRecords[0].color.toColor(), Color.red);
                    }
                }
                LINESTYLE2 line = new LINESTYLE2();
                line.width = 123;
                line.hasFillFlag = true;
                line.fillType = gradient(FILLSTYLE.LINEAR_GRADIENT);
                line.joinStyle = LINESTYLE2.MITER_JOIN;
                line.miterLimitFactor = 8;
                ShapeSelection model = empty.drawStroke(new Line2D.Double(0, 0, 2000, 0), line).model;
                preview.setSample(sample(model, 1000, 0));
                assertEquals(((JLabel) field(preview, "widthLabel")).getText(), "6.15 px");
                assertEquals(((JLabel) field(preview, "miterLabel")).getText(), "8");
                assertDisplayOnly((Component) field(preview, "fillPreview"));
                assertDisplayOnly((Component) field(preview, "strokePreview"));
                preview.setSample(null);
                assertEquals(((JLabel) field(preview, "widthLabel")).getText(), "-");
                assertNull(((JLabel) field(field(preview, "strokePreview"), "valueLabel")).getIcon());
                assertDisplayOnly((Component) field(preview, "fillPreview"));
                assertDisplayOnly((Component) field(preview, "strokePreview"));
                assertEquals(shape.getData(), before);
                assertFalse(shape.isModified());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
