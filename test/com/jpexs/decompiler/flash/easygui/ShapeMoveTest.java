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
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

public class ShapeMoveTest {

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    private static void addClean(ShapeTag shape) {
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static ShapeSelection saved(ShapeTag original) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        original.getSwf().saveTo(output);
        SWF restored = new SWF(new ByteArrayInputStream(output.toByteArray()), false);
        return new ShapeSelection((ShapeTag) restored.getCharacter(original.shapeId));
    }

    @Test
    public void moveCutsTheSourceOverwritesTheDestinationAndUndoRestoresOriginalBytes() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        rectangle(shape, 2000, 0, 2000, 1000, 2, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] before = shape.getData();
        ShapeSelection.BatchEdit edit = model.moveAll(Collections.singletonList(at(model, 500, 500)), 2000, 0);
        assertEquals(edit.selected.size(), 1);
        assertTrue(edit.selected.get(0).geometry.contains(2500, 500));
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Move", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        assertNull(at(restored, 500, 500));
        assertEquals(at(restored, 2500, 500).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 3500, 500).fill.color.toColor(), Color.blue);
        ShapeSelection.BatchEdit again = edit.model.moveAll(edit.selected, 5000, 0);
        assertNull(at(again.model, 2500, 500));
        assertEquals(at(again.model, 7500, 500).fill.color.toColor(), Color.red);
        history.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(history.canUndo(shape.getSwf()));
        history.redo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 2500, 500).fill.color.toColor(), Color.red);
        ShapeSelection.BatchEdit repaint = edit.model.changeAll(edit.selected, item -> fill(Color.green), null);
        assertEquals(at(repaint.model, 2500, 500).fill.color.toColor(), Color.green);
    }

    @Test
    public void rectangularSelectionMovesOnlyTheClippedFillAndStrokeAndKeepsTheSelection() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.RectangleSelection box = model.selectRectangle(new Rectangle2D.Double(500, -200, 1000, 1000), Collections.emptyList(), false);
        ShapeSelection.BatchEdit edit = box.model.moveAll(box.selected, 3000, 1000);
        assertEquals(edit.selected.size(), box.selected.size());
        new ShapeStyleChangeOperation(shape, edit.model, "Move", null).doOperation();
        ShapeSelection restored = saved(current(shape));
        assertNull(at(restored, 1000, 400));
        assertNull(at(restored, 1000, 0));
        assertEquals(at(restored, 250, 400).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 1000, 1200).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 4000, 1400).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 4000, 1000).line.getWidth(), 200);
        assertEquals(at(restored, 250, 0).line.getWidth(), 200);
        assertTrue(edit.model.selectionContains(edit.selected, new Point2D.Double(4000, 1400), 0, new AffineTransform()));
        assertFalse(edit.model.selectionContains(edit.selected, new Point2D.Double(1000, 1400), 0, new AffineTransform()));
    }

    @Test
    public void adjacentMovedEdgesKeepTheirJoinAndTheRemainingEdgesStayJoined() throws Exception {
        DefineShape4Tag shape = shape();
        LINESTYLE2 line = shape.shapes.lineStyles.lineStyles2[0];
        line.width = 400;
        line.joinStyle = LINESTYLE2.MITER_JOIN;
        line.miterLimitFactor = 4;
        rectangle(shape, 0, 0, 2000, 2000, 0, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        List<ShapeSelection.Selection> edges = Arrays.asList(at(model, 1000, 0), at(model, 0, 1000));
        ShapeSelection.BatchEdit edit = model.moveAll(edges, 5000, 0);
        assertTrue(edit.model.getParts().stream().anyMatch(part -> part.line != null && part.area.contains(4850, -150)));
        assertTrue(edit.model.getParts().stream().anyMatch(part -> part.line != null && part.area.contains(2150, 2150)));
        new ShapeStyleChangeOperation(shape, edit.model, "Move", null).doOperation();
        ShapeSelection restored = saved(current(shape));
        assertNull(at(restored, 1000, 0));
        assertEquals(at(restored, 6000, 0).line.getWidth(), 400);
        assertEquals(at(restored, 5000, 1000).line.getWidth(), 400);
        assertEquals(ShapeSelection.updateSelection(edit.selected, at(edit.model, 6000, 0), true).size(), 1);
    }

    @Test
    public void movingEveryEdgePreservesTheClosedStrokeAndDoesNotMoveItsFill() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> edges = Arrays.asList(at(model, 1000, 0), at(model, 2000, 1000),
                at(model, 1000, 2000), at(model, 0, 1000));
        ShapeSelection.BatchEdit edit = model.moveAll(edges, 5000, 0);
        assertEquals(edit.selected.size(), 4);
        assertEquals(at(edit.model, 1000, 1000).fill.color.toColor(), Color.red);
        assertNull(at(edit.model, 6000, 1000));
        assertTrue(edit.model.getParts().stream().filter(part -> part.line != null).anyMatch(part -> {
            PathIterator iterator = part.geometry.getPathIterator(null);
            double[] coords = new double[6];
            while (!iterator.isDone()) {
                if (iterator.currentSegment(coords) == PathIterator.SEG_CLOSE) {
                    return true;
                }
                iterator.next();
            }
            return false;
        }));
    }

    @Test
    public void gradientAndBitmapMatricesMoveWithTheirGeometryWithoutChangingTheSourceStyles() {
        DefineShape4Tag shape = shape();
        FILLSTYLE gradient = shape.shapes.fillStyles.fillStyles[0];
        gradient.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
        gradient.gradient = new GRADIENT();
        gradient.gradient.gradientRecords = new GRADRECORD[0];
        gradient.gradientMatrix = new MATRIX();
        gradient.gradientMatrix.translateX = 100;
        FILLSTYLE bitmap = shape.shapes.fillStyles.fillStyles[1];
        bitmap.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
        bitmap.bitmapId = 99;
        bitmap.bitmapMatrix = new MATRIX();
        bitmap.bitmapMatrix.translateX = 300;
        LINESTYLE2 line = shape.shapes.lineStyles.lineStyles2[0];
        line.hasFillFlag = true;
        line.fillType = gradient;
        line.color = null;
        rectangle(shape, 0, 0, 1000, 1000, 1, 1);
        rectangle(shape, 2000, 0, 1000, 1000, 2, 0);
        ShapeSelection model = selection(shape);
        ShapeSelection.BatchEdit edit = model.moveAll(Arrays.asList(at(model, 500, 500), at(model, 2500, 500), at(model, 500, 0)), 5000, 2000);
        assertEquals(at(edit.model, 5500, 2500).fill.gradientMatrix.translateX, 5100);
        assertEquals(at(edit.model, 5500, 2500).fill.gradientMatrix.translateY, 2000);
        assertEquals(at(edit.model, 7500, 2500).fill.bitmapMatrix.translateX, 5300);
        assertEquals(((LINESTYLE2) at(edit.model, 5500, 2000).line).fillType.gradientMatrix.translateX, 5100);
        assertEquals(gradient.gradientMatrix.translateX, 100);
        assertEquals(bitmap.bitmapMatrix.translateX, 300);
    }

    @Test
    public void movedQuadraticEdgesRemainCurvesAndCoverDestinationStrokes() throws Exception {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = 1000;
        curve.controlDeltaY = 2000;
        curve.anchorDeltaX = 1000;
        curve.anchorDeltaY = -2000;
        curve.calculateBits();
        shape.shapes.shapeRecords.add(curve);
        move(shape.shapes.shapeRecords, 6000, 1500, 0, 1);
        edge(shape.shapes.shapeRecords, 0, 3000);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.BatchEdit edit = model.moveAll(Collections.singletonList(at(model, 1000, 1000)), 5000, 2000);
        PathIterator iterator = edit.selected.get(0).geometry.getPathIterator(null);
        double[] coords = new double[6];
        iterator.next();
        assertEquals(iterator.currentSegment(coords), PathIterator.SEG_QUADTO);
        assertEquals(coords[0], 6000.0);
        assertEquals(coords[1], 4000.0);
        new ShapeStyleChangeOperation(shape, edit.model, "Move", null).doOperation();
        ShapeSelection restored = saved(current(shape));
        assertNull(at(restored, 1000, 1000));
        assertEquals(at(restored, 6000, 3000).line.getWidth(), 200);
        assertEquals(at(restored, 6000, 2000).line.getWidth(), 200);
        ShapeSelection.BatchEdit away = edit.model.moveAll(edit.selected, 5000, 0);
        assertNull(at(away.model, 6000, 3000));
    }

    @Test
    public void zeroDistanceAndEmptySelectionDoNotCreateAnEdit() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        assertFalse(model.moveAll(new ArrayList<>(), 5000, 0).changed);
        assertFalse(model.moveAll(Collections.singletonList(at(model, 500, 500)), 0.2, -0.1).changed);
    }
}
