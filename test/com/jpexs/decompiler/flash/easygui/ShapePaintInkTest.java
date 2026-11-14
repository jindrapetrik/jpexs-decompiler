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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapePaintInkTest {

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

    private static ShapeSelection roundTrip(ShapeSelection model) throws Exception {
        DefineShape4Tag original = shape();
        selection(original);
        original.getSwf().addTag(original);
        new ShapeStyleChangeOperation(original, model, "Draw", null).doOperation();
        return saved(current(original));
    }

    @Test
    public void adjoiningFacesWithDifferentStrokeStylesRemainPaintableAfterInkAndSave() throws Exception {
        ShapeSelection model = selection(shape());
        int[][] segments = new int[][]{{78, 56, 433, 298}, {55, 330, 322, 21},
            {278, -100, 275, 327}, {20, 254, 465, 220}};
        for (int i = 0; i < segments.length; i++) {
            int[] s = segments[i];
            LINESTYLE2 style = new LINESTYLE2();
            style.color = new RGBA(i % 2 == 0 ? Color.black : Color.blue);
            style.width = 20 + i * 10;
            model = roundTrip(model.drawShape(new Line2D.Double(s[0] * 20, s[1] * 20, s[2] * 20, s[3] * 20), null, style).model);
        }
        Point2D upper = point(250 * 20, 135 * 20);
        Point2D lower = point(225 * 20, 210 * 20);
        ShapeSelection.Selection region = model.fillRegionAt(lower);
        assertNotNull(region, "The mixed-style enclosure must be recognized before painting");
        assertFalse(region.geometry.contains(upper));
        model = roundTrip(model.setRegionFill(model.fillRegionAt(upper), fill(Color.red)).model);
        assertNotNull(model.fillRegionAt(lower), "Filling a neighbouring face must not open the enclosure");
        LINESTYLE2 ink = new LINESTYLE2();
        ink.color = new RGBA(Color.magenta);
        ink.width = 120;
        model = roundTrip(model.outlineRegion(model.fillRegionAt(upper), ink).model);
        region = model.fillRegionAt(lower);
        assertNotNull(region, "Changing a neighbouring outline must not open the enclosure");
        model = roundTrip(model.setRegionFill(region, fill(Color.green)).model);
        assertEquals(at(model, lower.getX(), lower.getY()).fill.color.toColor(), Color.green);
        assertEquals(at(model, upper.getX(), upper.getY()).fill.color.toColor(), Color.red);
        assertNull(model.fillRegionAt(point(400 * 20, 100 * 20)));
    }

    @Test
    public void mixedStyleEnclosuresAreIndependentOfTheirLocalCoordinateOrigin() throws Exception {
        for (int offset : new int[]{0, 32768, 65536, 262144, 1000000, -1000000}) {
            ShapeSelection model = selection(shape());
            int[][] points = new int[][]{{0, 0}, {1900, 1370}, {2500, -830}, {0, 0}};
            for (int i = 0; i < 3; i++) {
                LINESTYLE2 style = new LINESTYLE2();
                style.color = new RGBA(i == 0 ? Color.black : Color.magenta);
                style.width = 20 + 80 * i;
                style.startCapStyle = i;
                style.endCapStyle = i;
                model = roundTrip(model.drawShape(new Line2D.Double(offset + points[i][0], offset + points[i][1],
                        offset + points[i + 1][0], offset + points[i + 1][1]), null, style).model);
            }
            assertNotNull(model.fillRegionAt(point(offset + 1400, offset + 200)), "Enclosure at " + offset);
        }
    }

    @Test
    public void changingOneFaceKeepsOtherMixedStyleFacesClosed() throws Exception {
        DefineShape4Tag original = shape();
        LINESTYLE2[] styles = new LINESTYLE2[4];
        int[][] segments = new int[][]{{1160, 1580, 8440, 5300}, {1660, 6380, 6000, 640},
            {5400, -1820, 5760, 7300}, {960, 5700, 8620, 3800}};
        for (int i = 0; i < segments.length; i++) {
            int[] s = segments[i];
            LINESTYLE2 style = new LINESTYLE2();
            styles[i] = style;
            style.color = new RGBA(i % 2 == 0 ? Color.black : Color.blue);
            style.width = 20 + i * 10;
            move(original.shapes.shapeRecords, s[0], s[1], 0, i + 1);
            edge(original.shapes.shapeRecords, s[2] - s[0], s[3] - s[1]);
        }
        original.shapes.lineStyles.lineStyles2 = styles;
        ShapeSelection model = selection(original);
        List<Point2D> inside = new ArrayList<>();
        for (int y = 1000; y < 6000; y += 500) {
            for (int x = 2000; x < 7000; x += 500) {
                if (model.fillRegionAt(point(x, y)) != null && at(model, x, y) == null) {
                    inside.add(point(x, y));
                }
            }
        }
        assertFalse(inside.isEmpty());
        Point2D painted = inside.get(0);
        model = roundTrip(model.setRegionFill(model.fillRegionAt(painted), fill(Color.red)).model);
        for (Point2D point : inside) {
            assertNotNull(model.fillRegionAt(point), "Face opened by Paint at " + point);
        }
        LINESTYLE2 ink = new LINESTYLE2();
        ink.color = new RGBA(Color.magenta);
        ink.width = 120;
        model = roundTrip(model.outlineRegion(model.fillRegionAt(painted), ink).model);
        for (Point2D point : inside) {
            assertNotNull(model.fillRegionAt(point), "Face opened by Ink at " + point);
        }
        Point2D neighbour = point(4000, 3500);
        model = roundTrip(model.setRegionFill(model.fillRegionAt(neighbour), fill(Color.green)).model);
        assertEquals(at(model, neighbour.getX(), neighbour.getY()).fill.color.toColor(), Color.green);
        assertEquals(model.fillRegionAt(painted).fill.color.toColor(), Color.red);
    }

    private static DefineShape4Tag adjacentInkTriangles() {
        // Crossing paths from graphic 15 in graphics_mod.swf. Ink on the first
        // triangle used to open the neighbouring triangle in the working model.
        DefineShape4Tag original = shape();
        int[][] paths = new int[][]{
            {4190, 4549, 4211, 3927, 4233, 3295, 4256, 2626, 4286, 1741, 4289, 1678},
            {4246, 1720, 4286, 1741, 4865, 2037, 5192, 2205, 5597, 2412, 6042, 2640},
            {5590, 1339, 5044, 1866, 4865, 2037, 4256, 2626, 3600, 3259, 3171, 3672},
            {5618, 3177, 5385, 2644, 5192, 2205, 5044, 1866, 4670, 1013},
            {3836, 4337, 4211, 3927, 4762, 3325, 5385, 2644, 5597, 2412, 6269, 1678},
            {3157, 3234, 3600, 3259, 4233, 3295, 4762, 3325, 5406, 3361}
        };
        original.shapes.lineStyles.lineStyles2[0].width = 20;
        for (int[] path : paths) {
            move(original.shapes.shapeRecords, path[0], path[1], 0, 1);
            for (int i = 2; i < path.length; i += 2) {
                edge(original.shapes.shapeRecords, path[i] - path[i - 2], path[i + 1] - path[i - 1]);
            }
        }
        return original;
    }

    @Test
    public void consecutiveInkEditsKeepTheAdjacentTriangleAvailableBeforeReloading() throws Exception {
        DefineShape4Tag original = adjacentInkTriangles();
        AtomicReference<ShapeSelection> active = new AtomicReference<>(selection(original));
        original.getSwf().addTag(original);
        byte[] before = original.getData();
        LINESTYLE2 ink = new LINESTYLE2();
        ink.color = new RGBA(Color.black);
        ink.width = 80;
        UndoManager undo = new UndoManager();
        for (Point2D click : new Point2D[]{point(4466, 2135), point(5034, 2036)}) {
            ShapeSelection previous = active.get();
            ShapeSelection.Selection region = previous.fillRegionAt(click);
            assertNotNull(region, "The next triangle must remain available without reloading the SWF");
            ShapeSelection.BatchEdit edit = previous.outlineRegion(region, ink);
            assertTrue(edit.changed);
            undo.doOperation(new ShapeStyleChangeOperation(current(original), edit.model, "Ink", (tag, forward) -> {
                active.set(forward ? edit.committedModel(tag) : previous);
            }), original.getSwf());
            assertNotNull(active.get().fillRegionAt(point(5034, 2036)));
        }
        assertEquals(active.get().select(point(4954.5, 1951.5), 1).line.getWidth(), 80);
        assertEquals(active.get().select(point(5118, 2035.5), 1).line.getWidth(), 80);
        assertEquals(active.get().select(point(5028.5, 2121), 1).line.getWidth(), 80);
        assertNull(active.get().fillRegionAt(point(5034, 2036)).fill);
        byte[] after = current(original).getData();
        undo.undo(original.getSwf());
        assertNotNull(active.get().fillRegionAt(point(5034, 2036)));
        undo.undo(original.getSwf());
        assertEquals(current(original).getData(), before);
        undo.redo(original.getSwf());
        assertNotNull(active.get().fillRegionAt(point(5034, 2036)));
        undo.redo(original.getSwf());
        assertEquals(current(original).getData(), after);
        assertNotNull(active.get().fillRegionAt(point(5034, 2036)));
    }

    private static Set<String> lineGraph(ShapeSelection model) {
        Set<String> graph = new TreeSet<>();
        for (ShapeSelection.Selection part : model.getParts()) {
            if (!part.stroke || part.line == null) {
                continue;
            }
            double x = 0;
            double y = 0;
            double sx = 0;
            double sy = 0;
            double[] coords = new double[6];
            for (PathIterator it = part.geometry.getPathIterator(null); !it.isDone(); it.next()) {
                int type = it.currentSegment(coords);
                if (type == PathIterator.SEG_MOVETO) {
                    x = sx = coords[0];
                    y = sy = coords[1];
                    continue;
                }
                assertTrue(type == PathIterator.SEG_LINETO || type == PathIterator.SEG_CLOSE);
                double ex = type == PathIterator.SEG_CLOSE ? sx : coords[0];
                double ey = type == PathIterator.SEG_CLOSE ? sy : coords[1];
                if (Math.hypot(ex - x, ey - y) > 1e-6) {
                    String a = Math.round(x * 1e6) + "," + Math.round(y * 1e6);
                    String b = Math.round(ex * 1e6) + "," + Math.round(ey * 1e6);
                    graph.add(a.compareTo(b) < 0 ? a + ":" + b : b + ":" + a);
                }
                x = ex;
                y = ey;
            }
        }
        return graph;
    }

    @Test
    public void inkPreservesAllSharedEdgesAcrossDifferentFaceOrdersWithoutSerialization() {
        Point2D[] faces = {point(4466, 2135), point(5034, 2036), point(4800, 2700),
            point(5391, 2420), point(4200, 2700), point(4300, 3500)};
        for (int start = 0; start < faces.length; start++) {
            for (int direction : new int[]{-1, 1}) {
                ShapeSelection model = selection(adjacentInkTriangles());
                Set<String> before = lineGraph(model);
                for (int step = 0; step < faces.length; step++) {
                    Point2D click = faces[(start + direction * step + faces.length) % faces.length];
                    ShapeSelection.Selection region = model.fillRegionAt(click);
                    assertNotNull(region, "Every region must remain closed before applying Ink");
                    LINESTYLE2 ink = new LINESTYLE2();
                    ink.color = new RGBA(step % 2 == 0 ? Color.orange : Color.blue);
                    ink.width = 40 + step * 20;
                    ShapeSelection.BatchEdit edit = model.outlineRegion(region, ink);
                    assertTrue(edit.changed);
                    model = edit.model;
                    assertEquals(lineGraph(model), before, "Ink must change styles without moving or trimming shared edges");
                    assertFalse(model.outlineRegion(model.fillRegionAt(click), ink).changed, "Repeating the same style is a no-op");
                    for (Point2D face : faces) {
                        assertNotNull(model.fillRegionAt(face), "A neighbouring region must remain fillable without serialization");
                    }
                }
            }
        }
    }

    @Test
    public void inkJoinsMissingOutlineToExistingEdgeEndpoints() throws Exception {
        ShapeSelection model = selection(shape()).drawShape(new Rectangle2D.Double(0, 0, 1000, 1000), fill(Color.red), null).model;
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 200;
        line.color = new RGBA(Color.black);
        model = model.drawStroke(new Line2D.Double(0, 0, 500, 0), line).model;
        line.width = 40;
        line.color = new RGBA(Color.blue);
        model = model.outlineRegion(model.fillRegionAt(point(500, 500)), line).model;
        assertEquals(at(model, 250, 0).line.getWidth(), 40);
        assertEquals(at(model, 750, 0).line.getWidth(), 40);
        model = model.setRegionFill(model.fillRegionAt(point(500, 500)), null).model;
        assertNotNull(model.fillRegionAt(point(500, 500)), "The new outline must join the existing endpoints without a gap");
        model = roundTrip(model);
        assertNotNull(model.fillRegionAt(point(500, 500)));
    }

    @Test
    public void inkAppliesMiterJoinsAtCornersSharedByDifferentPaths() throws Exception {
        ShapeSelection model = selection(shape());
        int[][] edges = {{0, 0, 1000, 0}, {1000, 0, 500, 1000}, {500, 1000, 0, 0}};
        for (int[] edge : edges) {
            LINESTYLE2 original = new LINESTYLE2();
            original.width = 20;
            original.color = new RGBA(Color.black);
            model = model.drawStroke(new Line2D.Double(edge[0], edge[1], edge[2], edge[3]), original).model;
        }
        LINESTYLE2 ink = new LINESTYLE2();
        ink.width = 200;
        ink.color = new RGBA(Color.blue);
        ink.startCapStyle = LINESTYLE2.NO_CAP;
        ink.endCapStyle = LINESTYLE2.NO_CAP;
        ink.joinStyle = LINESTYLE2.MITER_JOIN;
        ink.miterLimitFactor = 4;
        model = model.outlineRegion(model.fillRegionAt(point(500, 400)), ink).model;
        assertNotNull(at(model, -80, -80), "The corner must be a miter join rather than two separately capped edges");
        model = roundTrip(model);
        assertNotNull(at(model, -80, -80));
        assertFalse(model.outlineRegion(model.fillRegionAt(point(500, 400)), ink).changed);
    }

    @Test
    public void paintFillsAnEmptyStrokeEnclosureAndPreservesItsOutlineWithUndoRedo() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 0, 1);
        ShapeSelection model = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        ShapeSelection.Selection region = model.fillRegionAt(point(500, 500));
        assertNotNull(region);
        assertNull(region.fill);
        assertTrue(region.geometry.contains(500, 500));
        ShapeSelection.BatchEdit edit = model.setRegionFill(region, fill(Color.green));
        assertTrue(edit.changed);
        assertTrue(edit.selected.isEmpty());
        assertEquals(shape.getData(), before);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Paint", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.green);
        assertTrue(at(restored, 500, 0).stroke);
        assertEquals(at(restored, 500, 0).line.getWidth(), 200);
        assertNull(at(restored, 1500, 500));
        assertFalse(restored.setRegionFill(restored.fillRegionAt(point(500, 500)), fill(Color.green)).changed);
        byte[] after = current(shape).getData();
        undo.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(undo.canUndo(shape.getSwf()));
        undo.redo(shape.getSwf());
        assertEquals(current(shape).getData(), after);
    }

    @Test
    public void paintOnlyChangesTheClickedConnectedFillAndNoneRemovesIt() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        rectangle(shape, 2000, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        ShapeSelection.BatchEdit edit = model.setRegionFill(model.fillRegionAt(point(500, 500)), fill(Color.blue));
        assertEquals(at(edit.model, 500, 500).fill.color.toColor(), Color.blue);
        assertEquals(at(edit.model, 2500, 500).fill.color.toColor(), Color.red);
        ShapeSelection.BatchEdit removed = edit.model.setRegionFill(edit.model.fillRegionAt(point(500, 500)), null);
        assertNull(at(removed.model, 500, 500));
        assertEquals(at(removed.model, 2500, 500).fill.color.toColor(), Color.red);
    }

    @Test
    public void paintRecolorsOnlyTheSelectedSideOfAnExistingStrokedEdge() throws Exception {
        DefineShape4Tag original = adjacentFills(1);
        ShapeSelection model = selection(original);
        original.getSwf().addTag(original);
        byte[] before = original.getData();

        ShapeSelection.BatchEdit edit = model.setRegionFill(model.fillRegionAt(point(1500, 500)), fill(Color.green));
        assertTrue(edit.changed);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, edit.model, "Paint", null), original.getSwf());
        ShapeTag painted = current(original);
        assertEquals(edgeRecords(painted), edgeRecords(original));
        ShapeSelection restored = saved(painted);
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 1500, 500).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 1000, 500).line.getWidth(), 200);
        assertEquals(at(restored, 1000, 500).line.getColor().toColor(), Color.black);
        ShapeSelection cleared = restored.setRegionFill(restored.fillRegionAt(point(1500, 500)), null).model;
        assertNull(at(cleared, 1500, 500));
        assertEquals(at(cleared, 1000, 500).line.getWidth(), 200);
        assertEquals(at(cleared, 500, 500).fill.color.toColor(), Color.red);

        byte[] after = painted.getData();
        undo.undo(original.getSwf());
        assertEquals(current(original).getData(), before);
        undo.redo(original.getSwf());
        assertEquals(current(original).getData(), after);
    }

    @Test
    public void crossingStrokesDivideTheBucketIntoSeparateFaces() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 0, 1);
        move(shape.shapes.shapeRecords, 1000, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 0, 2000);
        move(shape.shapes.shapeRecords, 0, 1000, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection model = selection(shape);
        ShapeSelection.Selection region = model.fillRegionAt(point(500, 500));
        assertNotNull(region);
        assertFalse(region.geometry.contains(1500, 500));
        assertFalse(region.geometry.contains(500, 1500));
        ShapeSelection.BatchEdit edit = model.setRegionFill(region, fill(Color.green));
        assertEquals(at(edit.model, 500, 500).fill.color.toColor(), Color.green);
        assertNull(at(edit.model, 1500, 500));
        assertNull(at(edit.model, 500, 1500));
        assertTrue(at(edit.model, 1000, 500).stroke);
    }

    @Test
    public void nestedContoursKeepHolesAndOpenGapsNeverFillTheOutside() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 3000, 3000, 0, 1);
        rectangle(shape, 1000, 1000, 1000, 1000, 0, 1);
        ShapeSelection model = selection(shape);
        ShapeSelection.Selection ring = model.fillRegionAt(point(500, 500));
        assertNotNull(ring);
        assertFalse(ring.geometry.contains(1500, 1500));
        ShapeSelection painted = model.setRegionFill(ring, fill(Color.red)).model;
        assertNull(at(painted, 1500, 1500));
        ShapeSelection both = painted.setRegionFill(painted.fillRegionAt(point(1500, 1500)), fill(Color.blue)).model;
        assertEquals(at(both, 1500, 1500).fill.color.toColor(), Color.blue);
        assertEquals(at(both, 500, 500).fill.color.toColor(), Color.red);
        assertNull(model.fillRegionAt(point(-100, -100)));
        DefineShape4Tag open = shape();
        move(open.shapes.shapeRecords, 0, 0, 0, 1);
        edge(open.shapes.shapeRecords, 1000, 0);
        edge(open.shapes.shapeRecords, 0, 1000);
        edge(open.shapes.shapeRecords, -1000, 0);
        edge(open.shapes.shapeRecords, 0, -999);
        assertNull(selection(open).fillRegionAt(point(500, 500)), "A one-twip gap must keep the face open");
    }

    @Test
    public void curvedStrokeEnclosuresAndGradientPaintRoundTrip() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                DefineShape4Tag shape = shape();
                ShapeSelection empty = selection(shape);
                shape.getSwf().addTag(shape);
                LINESTYLE2 line = new LINESTYLE2();
                line.width = 40;
                line.color = new RGBA(Color.black);
                Shape curve = new Ellipse2D.Double(0, 0, 2000, 1000);
                ShapeSelection model = empty.drawStroke(curve, line).model;
                ShapeSelection.Selection region = model.fillRegionAt(point(1000, 500));
                assertNotNull(region);
                assertFalse(region.geometry.contains(50, 50));
                ShapePropertiesPanel settings = ShapePropertiesPanel.createFillSettings();
                Object fillEditor = field(settings, "fillEditor");
                ((JComboBox<?>) field(fillEditor, "typeComboBox")).setSelectedIndex(2);
                FILLSTYLE paint = settings.getFillStyle(region.geometry.getBounds2D());
                ShapeSelection.BatchEdit edit = model.setRegionFill(region, paint);
                new ShapeStyleChangeOperation(shape, edit.model, "Paint", null).doOperation();
                ShapeSelection parsed = saved(current(shape));
                FILLSTYLE restored = at(parsed, 1000, 500).fill;
                assertEquals(restored.fillStyleType, FILLSTYLE.LINEAR_GRADIENT);
                assertEquals(restored.gradient.gradientRecords.length, paint.gradient.gradientRecords.length);
                assertTrue(at(parsed, 1000, 0).stroke);
                assertNull(parsed.fillRegionAt(point(2500, 500)));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void inkUpdatesTheGraphicsSwfCircleAndKeepsOtherGeometryWithUndoRedo() throws Exception {
        SWF swf;
        try (FileInputStream input = new FileInputStream("libsrc/ffdec_lib/testdata/graphics/graphics.swf")) {
            swf = new SWF(input, false);
        }
        ShapeTag circle = (ShapeTag) swf.getCharacter(15);
        byte[] before = circle.getData();
        ShapeSelection model = new ShapeSelection(circle);
        LINESTYLE2 style = new LINESTYLE2();
        style.color = new RGBA(Color.black);
        style.width = 80;
        ShapeSelection.BatchEdit edit = model.outlineRegion(model.fillRegionAt(point(2500, 2000)), style);
        assertTrue(edit.changed);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(circle, edit.model, "Ink", null), swf);
        assertEquals(edgeRecords(current(circle)), edgeRecords(circle));
        ShapeSelection restored = saved(current(circle));
        // The exact leftmost vertex also belongs to the rectangle's bottom
        // stroke, so sample the circle just below that shared junction.
        for (Point2D point : new Point2D[]{point(2500, 1000), point(3500, 2000), point(2500, 3000), point(1518.3125, 2200.6875)}) {
            ShapeSelection.Selection edge = restored.select(point, 1);
            assertNotNull(edge);
            assertTrue(edge.stroke);
            assertEquals(edge.line.getWidth(), 80);
        }
        assertEquals(at(restored, 2500, 2000).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 500, 0).line.getWidth(), 20);
        byte[] after = current(circle).getData();
        undo.undo(swf);
        assertEquals(current(circle).getData(), before);
        assertFalse(undo.canUndo(swf));
        undo.redo(swf);
        assertEquals(current(circle).getData(), after);
    }

    @Test
    public void inkChangesTheWholeBoundaryAndKeepsTheFillWithUndoRedo() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 1);
        ShapeSelection model = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        ShapeSelection.Selection region = model.fillRegionAt(point(500, 500));
        LINESTYLE2 style = new LINESTYLE2();
        style.color = new RGBA(Color.green);
        style.width = 300;
        style.noHScaleFlag = true;
        style.noVScaleFlag = true;
        style.startCapStyle = LINESTYLE2.SQUARE_CAP;
        style.endCapStyle = LINESTYLE2.SQUARE_CAP;
        style.joinStyle = LINESTYLE2.MITER_JOIN;
        style.miterLimitFactor = 8;
        ShapeSelection.BatchEdit edit = model.outlineRegion(region, style);
        assertTrue(edit.changed);
        assertTrue(edit.selected.isEmpty());
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Ink", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        ShapeSelection.Selection top = at(restored, 500, 0);
        assertEquals(top.line.getColor().toColor(), Color.green);
        assertEquals(top.line.getWidth(), 300);
        assertTrue(((LINESTYLE2) top.line).noHScaleFlag && ((LINESTYLE2) top.line).noVScaleFlag);
        assertEquals(((LINESTYLE2) top.line).miterLimitFactor, 8f);
        assertEquals(at(restored, 1000, 500).line.getColor().toColor(), Color.green);
        assertEquals(at(restored, 500, 1000).line.getColor().toColor(), Color.green);
        assertEquals(at(restored, 0, 500).line.getColor().toColor(), Color.green);
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.red);
        assertFalse(restored.outlineRegion(restored.fillRegionAt(point(500, 500)), style).changed);
        ShapeSelection removed = restored.outlineRegion(restored.fillRegionAt(point(500, 500)), null).model;
        assertFalse(at(removed, 500, 0) != null && at(removed, 500, 0).stroke);
        assertFalse(at(removed, 1000, 500) != null && at(removed, 1000, 500).stroke);
        byte[] after = current(shape).getData();
        undo.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(undo.canUndo(shape.getSwf()));
        assertFalse(shape.getSwf().isModified());
        undo.redo(shape.getSwf());
        assertEquals(current(shape).getData(), after);
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void inkAppliesAStrokeToTheExistingFillEdges() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        LINESTYLE2 style = new LINESTYLE2();
        style.width = 40;
        style.color = new RGBA(Color.blue);
        ShapeSelection.BatchEdit edit = model.outlineRegion(model.fillRegionAt(point(500, 500)), style);
        assertTrue(edit.changed);
        ShapeTag edited = ShapeStyleChangeOperation.createShape(shape, edit.model);
        assertEquals(edgeRecords(edited), edgeRecords(shape));
        int fillStyle = 0;
        int lineStyle = 0;
        for (SHAPERECORD record : edited.getShapes().shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                fillStyle = change.stateFillStyle1 ? change.fillStyle1 : fillStyle;
                lineStyle = change.stateLineStyle ? change.lineStyle : lineStyle;
            } else if (record instanceof StraightEdgeRecord) {
                assertEquals(fillStyle, 1);
                assertTrue(lineStyle > 0);
                assertEquals(edited.getShapes().lineStyles.lineStyles2[lineStyle - 1].width, 40);
            }
        }
        assertEquals(at(edit.model, 500, 500).fill.color.toColor(), Color.red);
        for (Point2D point : new Point2D[]{point(0, 500), point(1000, 500), point(500, 0), point(500, 1000)}) {
            ShapeSelection.Selection border = edit.model.select(point, 0);
            assertTrue(border.stroke);
            assertEquals(border.line.getWidth(), 40);
            assertEquals(border.line.getColor().toColor(), Color.blue);
        }
    }

    private static List<SHAPERECORD> edgeRecords(ShapeTag shape) {
        return shape.getShapes().shapeRecords.stream()
                .filter(record -> record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord)
                .collect(Collectors.toList());
    }

    private static DefineShape4Tag adjacentFills(int leftLine) {
        DefineShape4Tag original = shape();
        List<SHAPERECORD> records = original.shapes.shapeRecords;
        move(records, 0, 0, 1, leftLine);
        edge(records, 1000, 0);
        StyleChangeRecord shared = new StyleChangeRecord();
        shared.stateFillStyle0 = true;
        shared.fillStyle0 = 2;
        records.add(shared);
        edge(records, 0, 1000);
        StyleChangeRecord left = new StyleChangeRecord();
        left.stateFillStyle0 = true;
        records.add(left);
        edge(records, -1000, 0);
        edge(records, 0, -1000);
        move(records, 1000, 0, 2, 0);
        edge(records, 1000, 0);
        edge(records, 0, 1000);
        edge(records, -1000, 0);
        return original;
    }

    @Test
    public void inkPreservesBothFillStylesOnASharedEdgeAndReusesItAfterReload() throws Exception {
        DefineShape4Tag original = adjacentFills(0);
        ShapeSelection model = selection(original);
        original.getSwf().addTag(original);

        LINESTYLE2 style = new LINESTYLE2();
        style.width = 60;
        style.color = new RGBA(Color.green);
        ShapeSelection.BatchEdit edit = model.outlineRegion(model.fillRegionAt(point(500, 500)), style);
        assertTrue(edit.changed);
        new ShapeStyleChangeOperation(original, edit.model, "Ink", null).doOperation();
        assertEquals(edgeRecords(current(original)), edgeRecords(original));
        ShapeSelection reloaded = saved(current(original));
        assertEquals(at(reloaded, 500, 500).fill.color.toColor(), Color.red);
        assertEquals(at(reloaded, 1500, 500).fill.color.toColor(), Color.blue);
        assertEquals(at(reloaded, 1000, 500).line.getWidth(), 60);
        assertNull(at(reloaded, 1500, -1));
        assertFalse(reloaded.outlineRegion(reloaded.fillRegionAt(point(500, 500)), style).changed);
    }

    @Test
    public void inkOnlyReplacesTheBoundaryOfTheClickedFaceAndLeavesNeighbouringEdges() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 0, 1);
        move(shape.shapes.shapeRecords, 1000, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 0, 2000);
        move(shape.shapes.shapeRecords, 0, 1000, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection model = selection(shape);
        LINESTYLE2 style = new LINESTYLE2();
        style.width = 20;
        style.color = new RGBA(Color.green);
        ShapeSelection outlined = model.outlineRegion(model.fillRegionAt(point(500, 500)), style).model;
        assertNull(at(outlined, 500, 500), "Ink must not add a fill");
        assertEquals(at(outlined, 500, 0).line.getColor().toColor(), Color.green);
        assertEquals(at(outlined, 1000, 500).line.getColor().toColor(), Color.green);
        assertEquals(at(outlined, 500, 1000).line.getColor().toColor(), Color.green);
        assertEquals(at(outlined, 0, 500).line.getColor().toColor(), Color.green);
        assertEquals(at(outlined, 1500, 0).line.getColor().toColor(), Color.black);
        assertEquals(at(outlined, 1000, 1500).line.getColor().toColor(), Color.black);
        assertEquals(at(outlined, 500, -50) == null, true, "The old wider border must not remain beneath the new one");
    }

    @Test
    public void paintAndInkExposeOnlyTheirOwnSettingsAndNoneIsAvailable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel paint = ShapePropertiesPanel.createFillSettings();
                ShapePropertiesPanel ink = new ShapePropertiesPanel(true);
                assertNotNull(((JPanel) field(paint, "fillPanel")).getParent());
                assertNull(((JPanel) field(paint, "strokePanel")).getParent());
                assertNull(((JPanel) field(ink, "fillPanel")).getParent());
                assertNotNull(((JPanel) field(ink, "strokePanel")).getParent());
                assertEquals(paint.getFillStyle().color.toColor(), Color.black);
                assertNull(paint.getStrokeStyle());
                assertNotNull(ink.getStrokeStyle());
                assertNull(ink.getFillStyle());
                ((JComboBox<?>) field(field(paint, "fillEditor"), "typeComboBox")).setSelectedIndex(0);
                ((JComboBox<?>) field(field(ink, "strokeEditor"), "typeComboBox")).setSelectedIndex(0);
                assertNull(paint.getFillStyle());
                assertNull(ink.getStrokeStyle());
                ShapeSelection model = selection(shape());
                assertFalse(model.setRegionFill(null, fill(Color.red)).changed);
                assertFalse(model.outlineRegion(null, null).changed);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
