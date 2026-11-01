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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.panels.BrushPropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapeEraserTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(point(x, y), 0);
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

    private static LINESTYLE2 line() {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 400;
        line.color = new RGBA(Color.black);
        line.noClose = true;
        return line;
    }

    private static ShapeSelection stroked() {
        ShapeSelection empty = selection(shape());
        PencilStroke first = new PencilStroke(point(-1000, 0));
        first.add(point(1000, 0));
        ShapeSelection model = empty.drawStroke(first.getPath(), line()).model;
        PencilStroke second = new PencilStroke(point(-1000, 1000));
        second.add(point(1000, 1000));
        return model.drawStroke(second.getPath(), line()).model;
    }

    @Test
    public void oneEraserGestureCutsFillsAndStrokesAndSupportsOneUndoRedo() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection original = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        BrushStroke gesture = new BrushStroke(BrushShape.SQUARE, 400, point(1000, -1000));
        gesture.add(point(1000, 3000));
        ShapeSelection.BatchEdit edit = original.erase(gesture.getArea());
        assertTrue(edit.changed);
        assertTrue(edit.selected.isEmpty());
        assertEquals(shape.getData(), before, "Live erasing must not mutate the original tag");
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Eraser", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        assertNull(at(restored, 1000, 1000));
        assertNull(at(restored, 1000, 0));
        assertNull(at(restored, 1000, 2000));
        assertEquals(at(restored, 500, 1000).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 500, -50).fill.color.toColor(), Color.black);
        byte[] after = current(shape).getData();
        undo.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(undo.canUndo(shape.getSwf()));
        undo.redo(shape.getSwf());
        assertEquals(current(shape).getData(), after);
        ShapeSelection.BatchEdit all = restored.erase(new Rectangle2D.Double(-1000, -1000, 5000, 5000));
        assertTrue(all.model.getParts().isEmpty());
        new ShapeStyleChangeOperation(current(shape), all.model, "Eraser", null).doOperation();
        assertTrue(saved(current(shape)).getParts().isEmpty());
    }

    @Test
    public void eraserCutsTheInkEvenWhenItDoesNotTouchTheStrokeCenterline() {
        ShapeSelection model = stroked();
        ShapeSelection.BatchEdit edit = model.erase(new Rectangle2D.Double(-100, 100, 200, 200));
        assertTrue(edit.changed);
        assertNull(at(edit.model, 0, 150));
        assertNotNull(at(edit.model, 0, 0));
        assertNotNull(at(edit.model, 0, -150));
        assertNotNull(at(edit.model, 500, 150));
        assertTrue(at(edit.model, 0, 1000).stroke, "Unaffected strokes must keep their editable line styles");
        assertEquals(model.getParts().size(), 2);
        assertTrue(model.getParts().get(0).stroke, "Preparing an eraser preview must not mutate the model");
    }

    @Test
    public void allEightTipsEraseTheirActualFootprintAndEmptyGesturesAreNoOps() {
        DefineShape4Tag shape = shape();
        rectangle(shape, -2000, -2000, 4000, 4000, 1, 0);
        ShapeSelection original = selection(shape);
        for (BrushShape tip : BrushShape.values()) {
            Shape footprint = tip.tip(point(0, 0), 500);
            ShapeSelection.BatchEdit edit = original.erase(footprint);
            for (int y = -280; y < 280; y += 53) {
                for (int x = -275; x < 280; x += 51) {
                    assertEquals(at(edit.model, x, y) == null, footprint.contains(x, y), tip.name());
                }
            }
        }
        ShapeSelection.BatchEdit empty = original.erase(new Rectangle2D.Double(5000, 5000, 500, 500));
        assertFalse(empty.changed);
        assertSame(empty.model, original);
        assertFalse(original.erase(new Rectangle2D.Double()).changed);
    }

    @Test
    public void placedEraserUsesTheRenderedStrokeWidthAndKeepsItsStageOrientation() throws Exception {
        ShapeSelection model = stroked();
        AffineTransform placement = AffineTransform.getScaleInstance(2, 0.5);
        Point2D localCenter = placement.createInverse().transform(point(0, 250), null);
        BrushStroke eraser = new BrushStroke(BrushShape.SQUARE, 100, localCenter, placement);
        ShapeSelection.BatchEdit edit = model.erase(eraser.getArea(), placement);
        assertTrue(edit.changed, "The wide visible edge must be erased even outside the unscaled local stroke bounds");
        Point2D erased = placement.createInverse().transform(point(0, 250), null);
        Point2D kept = placement.createInverse().transform(point(0, 170), null);
        assertNull(edit.model.select(erased, 0));
        assertNotNull(edit.model.select(kept, 0));
        placement.rotate(Math.PI / 4);
        Shape footprint = new BrushStroke(BrushShape.SQUARE, 200, point(0, 0), placement).getArea();
        Rectangle2D screen = placement.createTransformedShape(footprint).getBounds2D();
        assertEquals(screen.getWidth(), 200.0, 0.01);
        assertEquals(screen.getHeight(), 200.0, 0.01);
    }

    @Test
    public void clippedStrokeOutlinesPreserveGradientAndBitmapPaint() throws Exception {
        for (int type : new int[]{FILLSTYLE.LINEAR_GRADIENT, FILLSTYLE.REPEATING_BITMAP}) {
            DefineShape4Tag shape = shape();
            ShapeSelection empty = selection(shape);
            shape.getSwf().addTag(shape);
            LINESTYLE2 line = line();
            line.hasFillFlag = true;
            line.color = null;
            FILLSTYLE fill = new FILLSTYLE();
            fill.inShape3 = true;
            fill.fillStyleType = type;
            MATRIX matrix = new MATRIX();
            matrix.translateX = 123;
            if (fill.hasGradient()) {
                fill.gradientMatrix = matrix;
                fill.gradient = new GRADIENT();
                GRADRECORD first = new GRADRECORD();
                first.ratio = 0;
                first.color = new RGBA(Color.red);
                GRADRECORD last = new GRADRECORD();
                last.ratio = 255;
                last.color = new RGBA(Color.blue);
                fill.gradient.gradientRecords = new GRADRECORD[]{first, last};
            } else {
                fill.bitmapMatrix = matrix;
                fill.bitmapId = 123;
            }
            line.fillType = fill;
            PencilStroke stroke = new PencilStroke(point(-1000, 0));
            stroke.add(point(1000, 0));
            ShapeSelection model = empty.drawStroke(stroke.getPath(), line).model;
            ShapeSelection.BatchEdit edit = model.erase(new Rectangle2D.Double(-100, 100, 200, 200));
            new ShapeStyleChangeOperation(shape, edit.model, "Eraser", null).doOperation();
            FILLSTYLE restored = at(saved(current(shape)), 500, 0).fill;
            assertEquals(restored.fillStyleType, type);
            assertEquals((restored.hasGradient() ? restored.gradientMatrix : restored.bitmapMatrix).translateX, 123);
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static List<Component> descendants(Container container) {
        List<Component> result = new ArrayList<>();
        for (Component child : container.getComponents()) {
            result.add(child);
            if (child instanceof Container) {
                result.addAll(descendants((Container) child));
            }
        }
        return result;
    }

    @Test
    public void eraserPropertiesOnlyExposeTipAndSizeAndNeverProvideFill() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                BrushPropertiesPanel panel = new BrushPropertiesPanel(true);
                Object hiddenFill = field(panel, "fillEditor");
                assertFalse(descendants(panel).contains(hiddenFill));
                assertNull(panel.getFillStyle(new Rectangle2D.Double()));
                assertEquals(panel.getBrushShape(), BrushShape.CIRCLE);
                assertEquals(panel.getBrushSize(), 10.0);
                assertEquals(((JComboBox<?>) field(panel, "shapeEditor")).getItemCount(), 8);
                ((JComboBox<?>) field(panel, "shapeEditor")).setSelectedItem(BrushShape.SQUARE);
                ((FloatPropertyField) field(panel, "sizeEditor")).setValue(40f);
                panel.setSwf(new SWF());
                assertEquals(panel.getBrushShape(), BrushShape.SQUARE);
                assertEquals(panel.getBrushSize(), 40.0);
                assertNull(panel.getFillStyle(new Rectangle2D.Double()));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
