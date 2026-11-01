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
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import org.testng.annotations.Test;

public class ShapeEdgeIntersectionTest {

    private static LINESTYLE2 line(Color color) {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 20;
        line.color = new RGBA(color);
        return line;
    }

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(new Point2D.Double(x, y), 0.1);
    }

    private static void bounds(ShapeSelection model, double x, double y, double bx, double by, double width, double height) {
        ShapeSelection.Selection selected = at(model, x, y);
        assertNotNull(selected);
        assertTrue(selected.stroke);
        Rectangle2D bounds = selected.geometry.getBounds2D();
        assertEquals(bounds.getX(), bx, 0.01);
        assertEquals(bounds.getY(), by, 0.01);
        assertEquals(bounds.getWidth(), width, 0.01);
        assertEquals(bounds.getHeight(), height, 0.01);
    }

    private static ShapeSelection saved(ShapeSelection model) throws Exception {
        DefineShape4Tag original = shape();
        selection(original);
        original.getSwf().addTag(original);
        new ShapeStyleChangeOperation(original, model, "Draw", null).doOperation();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF swf = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return new ShapeSelection((ShapeTag) swf.getCharacter(original.shapeId));
    }

    @Test
    public void crossingLinesSplitBothStrokesAndRemainIndividuallyEditableAfterSaving() throws Exception {
        ShapeSelection model = selection(shape()).drawStroke(new Line2D.Double(0, 500, 1000, 500), line(Color.black)).model;
        model = model.drawShape(new Line2D.Double(500, 0, 500, 1000), null, line(Color.blue)).model;
        for (ShapeSelection current : new ShapeSelection[]{model, saved(model)}) {
            bounds(current, 200, 500, 0, 500, 500, 0);
            bounds(current, 800, 500, 500, 500, 500, 0);
            bounds(current, 500, 200, 500, 0, 0, 500);
            bounds(current, 500, 800, 500, 500, 0, 500);
            ShapeSelection deleted = current.deleteAll(Collections.singletonList(at(current, 200, 500))).model;
            assertNull(at(deleted, 200, 500));
            assertNotNull(at(deleted, 800, 500));
            assertNotNull(at(deleted, 500, 200));
            assertNotNull(at(deleted, 500, 800));
            ShapeSelection recolored = current.changeAll(Collections.singletonList(at(current, 500, 200)), null,
                    item -> line(Color.green)).model;
            assertEquals(at(recolored, 500, 200).line.getColor().toColor(), Color.green);
            assertEquals(at(recolored, 500, 800).line.getColor().toColor(), Color.blue);
        }
    }

    @Test
    public void endpointOnAnEdgeSplitsOnlyTheInteriorAndParallelEdgesAreUnchanged() throws Exception {
        ShapeSelection model = selection(shape()).drawStroke(new Line2D.Double(0, 500, 1000, 500), line(Color.black)).model;
        model = model.drawStroke(new Line2D.Double(500, 0, 500, 500), line(Color.blue)).model;
        model = model.drawStroke(new Line2D.Double(0, 1000, 1000, 1000), line(Color.green)).model;
        model = saved(model);
        bounds(model, 200, 500, 0, 500, 500, 0);
        bounds(model, 800, 500, 500, 500, 500, 0);
        bounds(model, 500, 200, 500, 0, 0, 500);
        bounds(model, 200, 1000, 0, 1000, 1000, 0);
    }

    @Test
    public void quadraticCrossingsRetainCurvesAndSplitAtBothIntersections() throws Exception {
        ShapeSelection model = selection(shape()).drawStroke(new QuadCurve2D.Double(0, 0, 500, 1000, 1000, 0), line(Color.black)).model;
        model = model.drawShape(new Line2D.Double(-100, 375, 1100, 375), null, line(Color.blue)).model;
        for (ShapeSelection current : new ShapeSelection[]{model, saved(model)}) {
            bounds(current, 500, 375, 250, 375, 500, 0);
            ShapeSelection.Selection curve = at(current, 100, 180);
            assertTrue(curve.stroke);
            PathIterator it = curve.geometry.getPathIterator(null);
            it.next();
            assertEquals(it.currentSegment(new double[6]), PathIterator.SEG_QUADTO);
            assertEquals(curve.geometry.getBounds2D().getMaxX(), 250, 0.01);
            ShapeSelection rest = current.deleteAll(Collections.singletonList(curve)).model;
            assertNull(at(rest, 100, 180));
            assertNotNull(at(rest, 500, 500));
            assertNotNull(at(rest, 900, 180));
        }
    }

    @Test
    public void curveAgainstCurveSplitsBothWithoutFlattening() throws Exception {
        ShapeSelection model = selection(shape()).drawStroke(new QuadCurve2D.Double(0, 0, 500, 1000, 1000, 0), line(Color.black)).model;
        model = model.drawStroke(new QuadCurve2D.Double(0, 500, 500, -500, 1000, 500), line(Color.blue)).model;
        for (ShapeSelection current : new ShapeSelection[]{model, saved(model)}) {
            assertTrue(at(current, 100, 180).geometry.getBounds2D().getMaxX() < 200);
            assertTrue(at(current, 900, 320).geometry.getBounds2D().getMinX() > 800);
        }
    }

    @Test
    public void ovalCrossingsSurviveSwfQuadraticConversionAndPreserveClosure() throws Exception {
        ShapeSelection model = selection(shape()).drawShape(new Ellipse2D.Double(0, 0, 2000, 1000), null, line(Color.black)).model;
        model = model.drawShape(new Line2D.Double(-500, 250, 2500, 250), null, line(Color.blue)).model;
        ShapeSelection parsed = saved(model);
        ShapeSelection.Selection middle = at(parsed, 1000, 250);
        assertTrue(middle.geometry.getBounds2D().getMinX() > 0);
        assertTrue(middle.geometry.getBounds2D().getMaxX() < 2000);
        assertEquals(parsed.getParts().size(), 2);
        Shape oval = parsed.getParts().get(0).geometry;
        PathIterator it = oval.getPathIterator(null);
        int last = -1;
        while (!it.isDone()) {
            last = it.currentSegment(new double[6]);
            it.next();
        }
        assertEquals(last, PathIterator.SEG_CLOSE);
    }

    @Test
    public void selfCrossingNewPathIsSplitAndFillBoundariesAlsoSplitNewStrokes() throws Exception {
        Path2D.Double bow = new Path2D.Double();
        bow.moveTo(0, 0);
        bow.lineTo(1000, 1000);
        bow.lineTo(0, 1000);
        bow.lineTo(1000, 0);
        ShapeSelection model = selection(shape()).drawStroke(bow, line(Color.black)).model;
        model = saved(model);
        bounds(model, 200, 200, 0, 0, 500, 500);
        bounds(model, 800, 800, 500, 500, 500, 500);
        model = selection(shape()).drawShape(new Rectangle2D.Double(0, 0, 1000, 1000), fill(Color.red), null).model;
        model = saved(model.drawStroke(new Line2D.Double(-500, 500, 1500, 500), line(Color.black)).model);
        bounds(model, 500, 500, 0, 500, 1000, 0);
        assertEquals(at(model, 500, 200).fill.color.toColor(), Color.red);
    }

    @Test
    public void subdivisionIsPartOfTheSingleDrawingUndoRedoOperation() throws Exception {
        DefineShape4Tag original = shape();
        ShapeSelection first = selection(original).drawStroke(new Line2D.Double(0, 500, 1000, 500), line(Color.black)).model;
        ShapeTag initial = ShapeStyleChangeOperation.createShape(original, first);
        initial.getSwf().addTag(initial);
        initial.setModified(false);
        initial.getSwf().setModified(false);
        byte[] before = initial.getData();
        ShapeSelection.BatchEdit edit = new ShapeSelection(initial).drawShape(new Line2D.Double(500, 0, 500, 1000), null, line(Color.blue));
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(initial, edit.model, "Draw", null), initial.getSwf());
        ShapeTag after = (ShapeTag) initial.getSwf().getCharacter(initial.getCharacterId());
        byte[] drawn = after.getData();
        bounds(new ShapeSelection(after), 200, 500, 0, 500, 500, 0);
        undo.undo(initial.getSwf());
        ShapeTag undone = (ShapeTag) initial.getSwf().getCharacter(initial.getCharacterId());
        assertEquals(undone.getData(), before);
        bounds(new ShapeSelection(undone), 200, 500, 0, 500, 1000, 0);
        assertFalse(undo.canUndo(initial.getSwf()));
        undo.redo(initial.getSwf());
        ShapeTag redone = (ShapeTag) initial.getSwf().getCharacter(initial.getCharacterId());
        assertEquals(redone.getData(), drawn);
        bounds(new ShapeSelection(redone), 500, 800, 500, 500, 0, 500);
    }

    @Test
    public void subdividedDrawingRetainsItsOwnGeometrySnapshot() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(1000, 0);
        ShapeSelection model = selection(shape()).drawShape(path, null, line(Color.black)).model;
        path.lineTo(2000, 1000);
        bounds(model, 500, 0, 0, 0, 1000, 0);
        assertNull(at(model, 1500, 500));
    }
}
