/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapePencilTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static LINESTYLE2 line() {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 240;
        line.color = new RGBA(0, 100, 50, 180);
        line.noClose = true;
        line.noHScaleFlag = true;
        line.startCapStyle = LINESTYLE2.SQUARE_CAP;
        line.endCapStyle = LINESTYLE2.SQUARE_CAP;
        line.joinStyle = LINESTYLE2.MITER_JOIN;
        line.miterLimitFactor = 7;
        return line;
    }

    private static ShapeTag current(ShapeTag shape) {
        return (ShapeTag) shape.getSwf().getCharacter(shape.shapeId);
    }

    private static ShapeSelection saved(ShapeTag shape) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shape.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return new ShapeSelection((ShapeTag) restored.getCharacter(shape.shapeId));
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void pencilAddsOneEditableOpenStrokeAndUndoRestoresTheOriginalBytes() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        ShapeSelection original = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        PencilStroke stroke = new PencilStroke(point(-1000, 500));
        stroke.add(point(3000, 500));
        stroke.add(point(3000, 1500));
        LINESTYLE2 style = line();
        ShapeSelection.BatchEdit edit = original.drawStroke(stroke.getPath(), style);
        assertTrue(edit.changed);
        assertTrue(edit.selected.isEmpty());
        assertEquals(edit.model.getParts().size(), original.getParts().size() + 1);
        assertEquals(shape.getData(), before, "Previewing must not modify the SWF");
        style.width = 999;
        stroke.add(point(9000, 9000));
        assertEquals(edit.model.getParts().get(1).line.getWidth(), 240);
        assertEquals(edit.model.getParts().get(1).geometry.getBounds2D().getMaxX(), 3000.0);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Pencil", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        assertEquals(restored.getParts().size(), 2);
        ShapeSelection.Selection added = restored.getParts().get(1);
        assertTrue(added.stroke);
        assertNull(added.fill, "Pencil must store a stroke rather than an outlined fill");
        LINESTYLE2 actual = (LINESTYLE2) added.line;
        assertEquals(actual.width, 240);
        assertEquals(actual.color.toColor(), new Color(0, 100, 50, 180));
        assertTrue(actual.noClose);
        assertTrue(actual.noHScaleFlag);
        assertFalse(actual.noVScaleFlag);
        assertEquals(actual.startCapStyle, LINESTYLE2.SQUARE_CAP);
        assertEquals(actual.endCapStyle, LINESTYLE2.SQUARE_CAP);
        assertEquals(actual.joinStyle, LINESTYLE2.MITER_JOIN);
        assertEquals(actual.miterLimitFactor, 7f);
        assertEquals(restored.select(point(1000, 1000), 0).fill.color.toColor(), Color.red);
        assertTrue(restored.select(point(1000, 500), 0).stroke);
        byte[] after = current(shape).getData();
        undo.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(undo.canUndo(shape.getSwf()));
        undo.redo(shape.getSwf());
        assertEquals(current(shape).getData(), after);
    }

    @Test
    public void noMovementNoneAndZeroWidthDoNotCreateAnEditButStraightLinesDo() {
        ShapeSelection model = selection(shape());
        PencilStroke stroke = new PencilStroke(point(100.1, 200.1));
        stroke.add(point(100.2, 200.2));
        assertFalse(model.drawStroke(stroke.getPath(), line()).changed);
        stroke.add(point(2000, 200));
        assertTrue(model.drawStroke(stroke.getPath(), line()).changed);
        assertFalse(model.drawStroke(stroke.getPath(), null).changed);
        LINESTYLE2 zero = line();
        zero.width = 0;
        assertFalse(model.drawStroke(stroke.getPath(), zero).changed);
        PencilStroke vertical = new PencilStroke(point(0, 0));
        vertical.add(point(0, 2000));
        assertTrue(model.drawStroke(vertical.getPath(), line()).changed);
        Path2D.Double copy = vertical.getPath();
        copy.reset();
        assertEquals(vertical.getPath().getCurrentPoint(), point(0, 2000));
    }

    @Test
    public void pencilPropertiesKeepStrokeSettingsAndSerializeGradientAndBitmapInk() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel(true);
                assertNull(((JPanel) field(panel, "fillPanel")).getParent());
                assertTrue(((JPanel) field(panel, "strokePanel")).isVisible());
                assertEquals(panel.getStrokeStyle().width, 20);
                assertEquals(panel.getStrokeStyle().color.toColor(), Color.black);
                ((FloatPropertyField) field(panel, "widthEditor")).setValue(12f);
                ((JComboBox<?>) field(panel, "scaleComboBox")).setSelectedIndex(3);
                ((JComboBox<?>) field(panel, "capComboBox")).setSelectedIndex(2);
                ((JComboBox<?>) field(panel, "joinComboBox")).setSelectedIndex(2);
                ((FloatPropertyField) field(panel, "miterEditor")).setValue(8f);
                DefineShape4Tag shape = shape();
                ShapeSelection original = selection(shape);
                shape.getSwf().addTag(shape);
                DefineBitsLossless2Tag image = new DefineBitsLossless2Tag(shape.getSwf());
                shape.getSwf().addTag(image);
                panel.setDrawingSwf(shape.getSwf());
                LINESTYLE2 style = panel.getStrokeStyle();
                assertEquals(style.width, 240);
                assertTrue(style.noHScaleFlag && style.noVScaleFlag);
                assertEquals(style.startCapStyle, LINESTYLE2.SQUARE_CAP);
                assertEquals(style.joinStyle, LINESTYLE2.MITER_JOIN);
                assertEquals(style.miterLimitFactor, 8f);
                JComboBox<?> types = (JComboBox<?>) field(field(panel, "strokeEditor"), "typeComboBox");
                types.setSelectedIndex(0);
                assertNull(panel.getStrokeStyle());
                PencilStroke stroke = new PencilStroke(point(0, 0));
                stroke.add(point(1000, 1000));
                int[] expected = {FILLSTYLE.LINEAR_GRADIENT, FILLSTYLE.RADIAL_GRADIENT, FILLSTYLE.REPEATING_BITMAP};
                for (int i = 0; i < expected.length; i++) {
                    types.setSelectedIndex(i + 2);
                    style = panel.getStrokeStyle(new Rectangle2D.Double(-100, -100, 1200, 1200));
                    assertTrue(style.hasFillFlag);
                    assertEquals(style.fillType.fillStyleType, expected[i]);
                    ShapeSelection.BatchEdit edit = original.drawStroke(stroke.getPath(), style);
                    new ShapeStyleChangeOperation(current(shape), edit.model, "Pencil", null).doOperation();
                    LINESTYLE2 restored = (LINESTYLE2) saved(current(shape)).getParts().get(0).line;
                    assertTrue(restored.hasFillFlag);
                    assertEquals(restored.fillType.fillStyleType, expected[i]);
                    assertEquals(restored.width, 240);
                    if (i == 2) {
                        assertEquals(restored.fillType.bitmapId, image.getCharacterId());
                    } else {
                        assertEquals(restored.fillType.gradient.gradientRecords.length, style.fillType.gradient.gradientRecords.length);
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void cursorRemainsCircularAndTracksWidthAndScaleModesOnPlacedShapes() {
        AffineTransform placement = AffineTransform.getRotateInstance(Math.PI / 4);
        placement.scale(2, 3);
        Point2D point = point(2000, 1000);
        LINESTYLE2 style = line();
        style.width = 200;
        for (int mode = 0; mode < 4; mode++) {
            style.noHScaleFlag = mode == 2 || mode == 3;
            style.noVScaleFlag = mode == 1 || mode == 3;
            Rectangle2D cursor = placement.createTransformedShape(PencilStroke.cursor(point, style, placement, 1)).getBounds2D();
            double x = placement.getScaleX() + placement.getShearX();
            double y = placement.getShearY() + placement.getScaleY();
            double scale = mode == 3 ? 1 : mode == 1 ? Math.abs(x) : mode == 2 ? Math.abs(y) : Math.hypot(x, y) / Math.sqrt(2);
            assertEquals(cursor.getWidth(), 200 * scale, 0.01);
            assertEquals(cursor.getHeight(), cursor.getWidth(), 0.01);
        }
        Rectangle2D empty = placement.createTransformedShape(PencilStroke.cursor(point, null, placement, 20)).getBounds2D();
        assertEquals(empty.getWidth(), 20.0, 0.01);
    }
}
