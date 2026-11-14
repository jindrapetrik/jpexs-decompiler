/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ShapeSelectionTest {

    static FILLSTYLE fill(Color color) {
        FILLSTYLE fill = new FILLSTYLE();
        fill.color = new RGBA(color);
        return fill;
    }

    static DefineShape4Tag shape() {
        DefineShape4Tag shape = new DefineShape4Tag(new SWF());
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill(Color.red), fill(Color.blue)};
        LINESTYLE2 line = new LINESTYLE2();
        line.color = new RGBA(Color.black);
        line.width = 200;
        shape.shapes.lineStyles.lineStyles2 = new LINESTYLE2[]{line};
        shape.shapes.shapeRecords = new ArrayList<>();
        shape.shapeBounds = new RECT(-100, 4100, -600, 2600);
        return shape;
    }

    static StyleChangeRecord move(List<SHAPERECORD> records, int x, int y, int fill, int line) {
        StyleChangeRecord change = new StyleChangeRecord();
        change.stateMoveTo = true;
        change.moveDeltaX = x;
        change.moveDeltaY = y;
        change.stateFillStyle0 = true;
        change.stateFillStyle1 = true;
        change.fillStyle1 = fill;
        change.stateLineStyle = true;
        change.lineStyle = line;
        records.add(change);
        return change;
    }

    static void edge(List<SHAPERECORD> records, int x, int y) {
        StraightEdgeRecord edge = new StraightEdgeRecord();
        edge.generalLineFlag = true;
        edge.deltaX = x;
        edge.deltaY = y;
        edge.simplify();
        edge.calculateBits();
        records.add(edge);
    }

    static void rectangle(ShapeTag shape, int x, int y, int w, int h, int fill, int line) {
        List<SHAPERECORD> records = shape.getShapes().shapeRecords;
        move(records, x, y, fill, line);
        edge(records, w, 0);
        edge(records, 0, h);
        edge(records, -w, 0);
        edge(records, 0, -h);
    }

    static ShapeSelection selection(ShapeTag shape) {
        shape.getShapes().shapeRecords.add(new EndShapeRecord());
        return new ShapeSelection(shape);
    }

    private static ShapeSelection.Selection at(ShapeSelection selection, double x, double y) {
        return selection.select(new Point2D.Double(x, y), 0);
    }

    @Test
    public void selectsOnlyTheClickedComponentAndKeepsHoles() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        rectangle(shape, 500, 500, 1000, 1000, 1, 0);
        rectangle(shape, 750, 750, 500, 500, 1, 0);
        rectangle(shape, 3000, 0, 1000, 1000, 1, 0);
        ShapeSelection selection = selection(shape);
        ShapeSelection.Selection outer = at(selection, 200, 200);
        assertSame(outer.fill, shape.shapes.fillStyles.fillStyles[0]);
        assertNull(outer.line);
        assertTrue(outer.area.contains(200, 200));
        assertFalse(outer.area.contains(600, 600));
        assertFalse(outer.area.contains(1000, 1000));
        assertFalse(outer.area.contains(3500, 500));
        assertNull(at(selection, 600, 600));
        assertTrue(at(selection, 1000, 1000).area.contains(1000, 1000));
        assertFalse(at(selection, 1000, 1000).area.contains(200, 200));
        assertFalse(at(selection, 3500, 500).area.contains(200, 200));
        assertNull(at(selection, -100, -100));
    }

    @Test
    public void strokeWinsOverItsFillAndSupportsScreenSpaceTolerance() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection selection = selection(shape);
        ShapeSelection.Selection border = at(selection, 0, 1000);
        assertSame(border.line, shape.shapes.lineStyles.lineStyles2[0]);
        assertNull(border.fill);
        assertNotNull(border.linePath);
        assertFalse(border.area.contains(1000, 1000));
        ShapeSelection.Selection center = at(selection, 1000, 1000);
        assertNotNull(center.fill);
        assertNull(center.line);
        assertFalse(center.area.contains(0, 1000));
        assertNotNull(selection.select(new Point2D.Double(-140, 1000), 60));
        assertNull(selection.select(new Point2D.Double(-170, 1000), 60));
    }

    @Test
    public void selectsOneEdgeOfAClosedStroke() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection selection = selection(shape);
        ShapeSelection.Selection top = at(selection, 1000, 0);
        assertEquals(top.geometry.getBounds2D().getWidth(), 2000.0);
        assertEquals(top.geometry.getBounds2D().getHeight(), 0.0);
        assertTrue(top.area.contains(1000, 0));
        assertFalse(top.area.contains(0, 1000));
        assertFalse(top.area.contains(2000, 1000));
        assertFalse(top.area.contains(1000, 2000));
        ShapeSelection.Selection left = at(selection, 0, 1000);
        assertEquals(left.geometry.getBounds2D().getWidth(), 0.0);
        assertEquals(left.geometry.getBounds2D().getHeight(), 2000.0);
    }

    @Test
    public void distinguishesConsecutiveCollinearEdgesUsingTheNearestEdge() {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 1000, 0);
        edge(shape.shapes.shapeRecords, 1000, 0);
        ShapeSelection selection = selection(shape);
        assertEquals(at(selection, 200, 0).geometry.getBounds2D().getMaxX(), 1000.0);
        assertEquals(at(selection, 1800, 0).geometry.getBounds2D().getMinX(), 1000.0);
        ShapeSelection.Selection tolerant = selection.select(new Point2D.Double(1800, 140), 60);
        assertEquals(tolerant.geometry.getBounds2D().getMinX(), 1000.0);
        assertEquals(tolerant.geometry.getBounds2D().getMaxX(), 2000.0);
    }

    @Test
    public void aStrokeDividesTheFillIntoBoundedRegions() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        move(shape.shapes.shapeRecords, 1000, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 0, 2000);
        ShapeSelection selection = selection(shape);
        ShapeSelection.Selection left = at(selection, 400, 1000);
        assertTrue(left.area.contains(400, 1000));
        assertFalse(left.area.contains(1600, 1000));
        assertTrue(at(selection, 1600, 1000).area.contains(1600, 1000));
        assertFalse(at(selection, 1600, 1000).area.contains(400, 1000));
    }

    @Test
    public void disconnectedStrokesOfTheSameStyleAreSelectedSeparately() {
        DefineShape4Tag shape = shape();
        move(shape.shapes.shapeRecords, 0, 500, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        move(shape.shapes.shapeRecords, 0, 1500, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection selection = selection(shape);
        assertTrue(at(selection, 1000, 500).area.contains(1000, 500));
        assertFalse(at(selection, 1000, 500).area.contains(1000, 1500));
    }

    @Test
    public void respectsGroupPaintOrderAndExcludesCoveredAreas() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        move(shape.shapes.shapeRecords, 0, 0, 0, 0);
        rectangle(shape, 1000, -500, 1500, 3000, 2, 0);
        ShapeSelection selection = selection(shape);
        assertSame(at(selection, 1500, 0).fill, shape.shapes.fillStyles.fillStyles[1]);
        ShapeSelection.Selection red = at(selection, 500, 1000);
        assertSame(red.fill, shape.shapes.fillStyles.fillStyles[0]);
        assertTrue(red.area.contains(500, 1000));
        assertFalse(red.area.contains(1500, 1000));
    }

    @Test
    public void readsStylesIntroducedByStyleChangeRecords() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        StyleChangeRecord change = move(shape.shapes.shapeRecords, 3000, 0, 1, 1);
        change.stateNewStyles = true;
        change.fillStyles = new FILLSTYLEARRAY();
        FILLSTYLE addedFill = fill(Color.green);
        change.fillStyles.fillStyles = new FILLSTYLE[]{addedFill};
        change.lineStyles = new LINESTYLEARRAY();
        LINESTYLE2 addedLine = new LINESTYLE2();
        addedLine.color = new RGBA(Color.magenta);
        addedLine.width = 100;
        change.lineStyles.lineStyles2 = new LINESTYLE2[]{addedLine};
        edge(shape.shapes.shapeRecords, 1000, 0);
        edge(shape.shapes.shapeRecords, 0, 1000);
        edge(shape.shapes.shapeRecords, -1000, 0);
        edge(shape.shapes.shapeRecords, 0, -1000);
        ShapeSelection selection = selection(shape);
        assertSame(at(selection, 3500, 500).fill, addedFill);
        assertSame(at(selection, 3000, 500).line, addedLine);
    }

    @Test
    public void handlesLegacyLineStylesAndCurvedFillEdges() {
        DefineShape3Tag shape = new DefineShape3Tag(new SWF());
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill(Color.red)};
        LINESTYLE line = new LINESTYLE();
        line.color = new RGBA(Color.black);
        line.width = 100;
        shape.shapes.lineStyles.lineStyles = new LINESTYLE[]{line};
        shape.shapes.shapeRecords = new ArrayList<>();
        move(shape.shapes.shapeRecords, 0, 0, 1, 1);
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = 1000;
        curve.controlDeltaY = -1000;
        curve.anchorDeltaX = 1000;
        curve.anchorDeltaY = 1000;
        curve.calculateBits();
        shape.shapes.shapeRecords.add(curve);
        edge(shape.shapes.shapeRecords, 0, 2000);
        edge(shape.shapes.shapeRecords, -2000, 0);
        edge(shape.shapes.shapeRecords, 0, -2000);
        ShapeSelection selection = selection(shape);
        assertSame(at(selection, 1000, -250).fill, shape.shapes.fillStyles.fillStyles[0]);
        assertNull(at(selection, 100, -400));
        assertSame(at(selection, 1000, -500).line, line);
        ShapeSelection.Selection selectedCurve = at(selection, 1000, -500);
        assertEquals(selectedCurve.geometry.getBounds2D().getMaxY(), 0.0);
        assertFalse(selectedCurve.area.contains(1000, 2000));
    }
}
