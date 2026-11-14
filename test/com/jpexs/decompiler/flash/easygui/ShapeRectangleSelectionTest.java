/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.edge;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.fill;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.move;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

public class ShapeRectangleSelectionTest {

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static ShapeSelection roundTrip(ShapeTag original) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return new ShapeSelection((ShapeTag) restored.getCharacter(original.shapeId));
    }

    private static void addClean(ShapeTag shape) {
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
    }

    private static LINESTYLE2 blue(ShapeSelection.Selection item) {
        LINESTYLE2 line = Helper.deepCopy((LINESTYLE2) item.line);
        line.color = new RGBA(Color.blue);
        return line;
    }

    private static ShapeSelection.RectangleSelection select(ShapeSelection model, double x, double y, double w, double h) {
        return model.selectRectangle(new Rectangle2D.Double(x, y, w, h), Collections.emptyList(), false);
    }

    @Test
    public void rectangleClipsBothFillAndStrokeAndOnlyPropertyChangesEnterHistory() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] before = shape.getData();
        ShapeSelection.RectangleSelection box = select(model, 500, -200, 1000, 1200);
        assertEquals(box.selected.size(), 2);
        assertEquals(shape.getData(), before);
        assertFalse(shape.getSwf().isModified());
        ShapeSelection.BatchEdit edit = box.model.changeAll(box.selected, item -> fill(Color.green), ShapeRectangleSelectionTest::blue);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Rectangle", null), shape.getSwf());
        ShapeSelection saved = roundTrip(current(shape));
        assertEquals(at(saved, 1000, 500).fill.color.toColor(), Color.green);
        assertEquals(at(saved, 250, 500).fill.color.toColor(), Color.red);
        assertEquals(at(saved, 1000, 1500).fill.color.toColor(), Color.red);
        assertEquals(at(saved, 1000, 0).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 250, 0).line.getColor().toColor(), Color.black);
        assertEquals(at(saved, 1750, 0).line.getColor().toColor(), Color.black);
        history.undo(shape.getSwf());
        assertSame(current(shape), shape);
        assertEquals(shape.getData(), before);
        assertFalse(history.canUndo(shape.getSwf()));
        assertFalse(shape.getSwf().isModified());
        history.redo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 1000, 0).line.getColor().toColor(), Color.blue);
    }

    @Test
    public void ctrlRectanglesUniteOverlapsAndKeepPreviouslySelectedEdgesAfterFurtherSplits() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 4000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.RectangleSelection first = select(model, 500, -200, 1000, 1200);
        ShapeSelection.RectangleSelection second = first.model.selectRectangle(new Rectangle2D.Double(1000, -200, 1500, 1200), first.selected, true);
        ShapeSelection.RectangleSelection third = second.model.selectRectangle(new Rectangle2D.Double(3000, -200, 500, 700), second.selected, true);
        assertEquals(third.selected.stream().filter(item -> !item.stroke).count(), 1L);
        Area selectedFill = new Area(third.selected.stream().filter(item -> !item.stroke).findFirst().get().geometry);
        assertTrue(selectedFill.contains(750, 500));
        assertTrue(selectedFill.contains(2000, 500));
        assertTrue(selectedFill.contains(3250, 250));
        assertFalse(selectedFill.contains(2750, 250));
        ShapeSelection.BatchEdit edit = third.model.changeAll(third.selected, item -> fill(Color.green), ShapeRectangleSelectionTest::blue);
        new ShapeStyleChangeOperation(shape, edit.model, "Rectangle", null).doOperation();
        ShapeSelection saved = roundTrip(current(shape));
        for (int x : new int[]{750, 1250, 2000, 3250}) {
            assertEquals(at(saved, x, 0).line.getColor().toColor(), Color.blue);
        }
        for (int x : new int[]{250, 2750, 3750}) {
            assertEquals(at(saved, x, 0).line.getColor().toColor(), Color.black);
        }
        assertEquals(at(saved, 2000, 500).fill.color.toColor(), Color.green);
        assertEquals(at(saved, 2750, 500).fill.color.toColor(), Color.red);
    }

    private static DefineShape4Tag curvedShape() {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = 1000;
        curve.controlDeltaY = 2000;
        curve.anchorDeltaX = 1000;
        curve.anchorDeltaY = -2000;
        curve.calculateBits();
        shape.shapes.shapeRecords.add(curve);
        return shape;
    }

    @Test
    public void quadraticStrokeIsSplitExactlyWithoutFlattening() throws Exception {
        DefineShape4Tag shape = curvedShape();
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.RectangleSelection box = select(model, 500, 500, 1000, 1000);
        assertEquals(box.selected.size(), 1);
        PathIterator path = box.selected.get(0).geometry.getPathIterator(null);
        double[] coords = new double[6];
        assertEquals(path.currentSegment(coords), PathIterator.SEG_MOVETO);
        assertEquals(coords[0], 500.0, 1e-7);
        assertEquals(coords[1], 750.0, 1e-7);
        path.next();
        assertEquals(path.currentSegment(coords), PathIterator.SEG_QUADTO);
        assertEquals(coords[0], 1000.0, 1e-7);
        assertEquals(coords[1], 1250.0, 1e-7);
        assertEquals(coords[2], 1500.0, 1e-7);
        assertEquals(coords[3], 750.0, 1e-7);
        path.next();
        assertTrue(path.isDone());
        ShapeSelection.BatchEdit edit = box.model.changeAll(box.selected, null, ShapeRectangleSelectionTest::blue);
        new ShapeStyleChangeOperation(shape, edit.model, "Curve", null).doOperation();
        ShapeSelection saved = roundTrip(current(shape));
        assertEquals(at(saved, 1000, 1000).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 200, 360).line.getColor().toColor(), Color.black);
        assertEquals(at(saved, 1800, 360).line.getColor().toColor(), Color.black);
    }

    @Test
    public void curveCanEnterTheSameRectangleTwiceAndCtrlCanKeepBothFragments() throws Exception {
        DefineShape4Tag shape = curvedShape();
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.RectangleSelection box = select(model, -100, 200, 2200, 600);
        assertEquals(box.selected.size(), 2);
        ShapeSelection.RectangleSelection another = box.model.selectRectangle(new Rectangle2D.Double(900, 900, 200, 200), box.selected, true);
        assertTrue(another.selected.size() >= 3);
        ShapeSelection.BatchEdit edit = another.model.changeAll(another.selected, null, ShapeRectangleSelectionTest::blue);
        new ShapeStyleChangeOperation(shape, edit.model, "Curve", null).doOperation();
        ShapeSelection saved = roundTrip(current(shape));
        assertEquals(at(saved, 300, 510).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 1000, 1000).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 1700, 510).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 700, 910).line.getColor().toColor(), Color.black);
        assertEquals(at(saved, 1300, 910).line.getColor().toColor(), Color.black);
    }

    @Test
    public void fillRectangleKeepsHolesAndCtrlClicksDoNotCreateOverlappingEdits() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        rectangle(shape, 500, 500, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        ShapeSelection.RectangleSelection box = select(model, 250, 250, 1000, 1000);
        assertEquals(box.selected.size(), 1);
        assertTrue(box.selected.get(0).geometry.contains(300, 300));
        assertFalse(box.selected.get(0).geometry.contains(1000, 1000));
        List<ShapeSelection.Selection> all = ShapeSelection.updateSelection(box.selected, at(box.model, 100, 100), true);
        assertEquals(all.size(), 1);
        ShapeSelection.BatchEdit edit = box.model.changeAll(all, item -> fill(Color.green), null);
        assertEquals(at(edit.model, 1800, 1800).fill.color.toColor(), Color.green);
        assertNull(at(edit.model, 1000, 1000));
        assertTrue(ShapeSelection.updateSelection(all, at(box.model, 100, 100), true).isEmpty());
    }

    @Test
    public void emptyRectangleReplacesOrPreservesSelectionAndDoesNotPickBoundaryTangencies() {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection model = selection(shape);
        ShapeSelection.RectangleSelection box = select(model, 500, -200, 1000, 400);
        assertEquals(box.selected.size(), 1);
        assertTrue(box.model.selectRectangle(new Rectangle2D.Double(4000, 4000, 100, 100), box.selected, false).selected.isEmpty());
        assertEquals(box.model.selectRectangle(new Rectangle2D.Double(4000, 4000, 100, 100), box.selected, true).selected.size(), 1);
        assertTrue(select(model, 2000, 0, 100, 100).selected.isEmpty());
    }

    @Test
    public void rotatedAndSkewedMarqueesKeepTheirActualAreaAndSplitStrokeAtTheScreenBorder() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 4000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        AffineTransform placement = new AffineTransform(1, 0.5, 0.4, 1.2, 1000, 2000);
        Rectangle2D screenBox = new Rectangle2D.Double(1500, 2000, 1500, 2000);
        Shape localBox = placement.createInverse().createTransformedShape(screenBox);
        ShapeSelection.RectangleSelection box = model.selectArea(localBox, Collections.emptyList(), false);
        ShapeSelection.BatchEdit edit = box.model.changeAll(box.selected, item -> fill(Color.green), ShapeRectangleSelectionTest::blue);
        new ShapeStyleChangeOperation(shape, edit.model, "Transformed rectangle", null).doOperation();
        ShapeSelection saved = roundTrip(current(shape));
        assertEquals(at(saved, 1000, 500).fill.color.toColor(), Color.green);
        // This point is inside the local bounding box but outside the screen rectangle.
        assertEquals(at(saved, 1700, 1200).fill.color.toColor(), Color.red);
        assertEquals(at(saved, 1000, 0).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 2500, 0).line.getColor().toColor(), Color.black);
        assertEquals(at(saved, 250, 0).line.getColor().toColor(), Color.black);
    }

    @Test
    public void strokeHitToleranceUsesScreenPixelsWithAnisotropicScaling() {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        shape.shapes.lineStyles.lineStyles2[0].width = 20;
        ShapeSelection model = selection(shape);
        AffineTransform placement = new AffineTransform(0.2, 0.1, 0, 0.01, 100, 200);
        assertTrue(model.select(new Point2D.Double(1000, 50), 3, placement).stroke);
        assertNull(model.select(new Point2D.Double(1000, 1000), 3, placement));
    }
}
