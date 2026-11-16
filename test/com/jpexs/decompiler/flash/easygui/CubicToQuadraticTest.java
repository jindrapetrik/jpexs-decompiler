/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.DisplayPoint;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Color;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Random;
import org.testng.annotations.Test;

public class CubicToQuadraticTest {

    @Test
    public void usesOneTwoOrThreeSegmentsInsteadOfAlwaysFour() {
        double[] exactQuadratic = {0, 0, 200, 400, 500, 400, 900, 0};
        double[][] reduced = CubicToQuadratic.approximate(exactQuadratic);
        assertEquals(reduced.length, 1);
        assertEquals(reduced[0], new double[]{300, 600, 900, 0});
        assertEquals(CubicToQuadratic.approximate(new double[]{0, 0, 200, 400, 510, 400, 900, 0}).length, 1);
        assertEquals(CubicToQuadratic.approximate(new double[]{0, 0, 200, 600, 600, 600, 900, 0}).length, 2);
        assertEquals(CubicToQuadratic.approximate(new double[]{0, 0, 0, 1000, 0, 1000, 2000, 0}).length, 3);
        assertEquals(SHAPERECORD.approximateCubic(exactQuadratic).length, 4,
                "Unrelated library conversions keep their existing behavior");
    }

    private static Point2D cubic(double[] coordinates, double t) {
        double[] work = coordinates.clone();
        for (int remaining = 3; remaining > 0; remaining--) {
            for (int i = 0; i < remaining * 2; i++) {
                work[i] += t * (work[i + 2] - work[i]);
            }
        }
        return new Point2D.Double(work[0], work[1]);
    }

    private static void assertAccurate(double[] original) {
        double[][] reduced = CubicToQuadratic.approximate(original);
        Point2D start = new Point2D.Double(original[0], original[1]);
        for (int i = 0; i < reduced.length; i++) {
            double[] quad = reduced[i];
            for (int sample = 0; sample <= 120; sample++) {
                double t = sample / 120.0;
                double u = 1 - t;
                Point2D actual = new Point2D.Double(u * u * start.getX() + 2 * u * t * quad[0] + t * t * quad[2],
                        u * u * start.getY() + 2 * u * t * quad[1] + t * t * quad[3]);
                double error = actual.distance(cubic(original, (i + t) / reduced.length));
                assertTrue(error <= CubicToQuadratic.MAX_ERROR + 1e-5, "Approximation error: " + error);
            }
            if (i > 0) {
                double[] previous = reduced[i - 1];
                assertEquals(quad[0] - start.getX(), start.getX() - previous[0], 1e-5);
                assertEquals(quad[1] - start.getY(), start.getY() - previous[1], 1e-5);
            }
            start = new Point2D.Double(quad[2], quad[3]);
        }
        assertEquals(start, new Point2D.Double(original[6], original[7]));
    }

    @Test
    public void limitsErrorAndKeepsContinuousJoinsForInflectionsLoopsDegeneraciesAndLargeCoordinates() {
        assertAccurate(new double[]{0, 0, 0, 0, 0, 0, 0, 0});
        assertAccurate(new double[]{10, 20, 30, 40, 50, 60, 70, 80});
        assertAccurate(new double[]{0, 0, 0, 2000, 2000, -2000, 2000, 0});
        assertAccurate(new double[]{0, 0, 2000, -2000, -2000, -2000, 0, 0});
        assertAccurate(new double[]{0, 0, 2000, 0, -1000, 0, 1000, 0});
        assertAccurate(new double[]{1000000000, 1000000000, 1000000200, 1000000600,
            1000000600, 1000000600, 1000000900, 1000000000});
        Random random = new Random(789465);
        for (int i = 0; i < 80; i++) {
            double[] curve = new double[8];
            for (int j = 0; j < curve.length; j++) {
                curve[j] = random.nextDouble() * 10000 - 5000;
            }
            assertAccurate(curve);
        }
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    @Test
    public void strokeCreatesFewerEditablePointsAndRetainsThemAcrossSavingAndUndo() throws Exception {
        DefineShape4Tag original = ShapeSelectionTest.shape();
        ShapeSelection model = ShapeSelectionTest.selection(original);
        SWF swf = original.getSwf();
        swf.addTag(original);
        original.setModified(false);
        swf.setModified(false);
        byte[] before = original.getData();
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 80;
        line.color = new RGBA(Color.blue);
        line.noClose = true;
        ShapeSelection.BatchEdit edit = model.drawStroke(new CubicCurve2D.Double(0, 0, 200, 600, 600, 600, 900, 0), line);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, edit.model, "Pen", null), swf);
        ShapeTag drawn = current(original);
        assertEquals(drawn.shapes.shapeRecords.stream().filter(record -> record instanceof CurvedEdgeRecord).count(), 2L);
        List<DisplayPoint> points = new ShapePointEdit(drawn).getPoints();
        assertEquals(points.size(), 5);
        assertEquals(points.stream().filter(point -> point.onPath).count(), 3L);
        assertEquals(points.get(4).toPoint2D(), new Point2D.Double(900, 0));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeTag saved = (ShapeTag) restored.getCharacter(original.shapeId);
        assertEquals(saved.getData(), drawn.getData());
        assertEquals(new ShapePointEdit(saved).getPoints().size(), 5);
        undo.undo(swf);
        assertEquals(current(original).getData(), before);
        assertFalse(undo.canUndo(swf));
        undo.redo(swf);
        assertEquals(current(original).getData(), drawn.getData());
    }

    @Test
    public void fillWriterUsesTheSameReductionAsStrokeIntersectionGeometry() throws Exception {
        DefineShape4Tag original = ShapeSelectionTest.shape();
        ShapeSelection model = ShapeSelectionTest.selection(original);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);
        path.curveTo(200, 400, 500, 400, 900, 0);
        path.closePath();
        ShapeSelection.BatchEdit edit = model.paint(path, ShapeSelectionTest.fill(Color.blue));
        boolean cubic = false;
        double[] coordinates = new double[6];
        for (ShapeSelection.Selection part : edit.model.getParts()) {
            for (PathIterator it = part.geometry.getPathIterator(null); !it.isDone(); it.next()) {
                cubic |= it.currentSegment(coordinates) == PathIterator.SEG_CUBICTO;
            }
        }
        assertTrue(cubic, "The fill must exercise the writer's cubic conversion");
        ShapeTag drawn = ShapeStyleChangeOperation.createShape(original, edit.model);
        assertEquals(drawn.shapes.shapeRecords.stream().filter(record -> record instanceof CurvedEdgeRecord).count(), 2L);
        assertTrue(new ShapeSelection(drawn).select(new Point2D.Double(400, 100), 0).fill.color.toColor().equals(Color.blue));
    }
}
