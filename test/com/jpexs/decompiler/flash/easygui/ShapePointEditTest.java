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
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.edge;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.move;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePointPropertiesPanel;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DisplayPoint;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.PointUpdateListener;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapePointEditTest {

    private static DefineShape4Tag rectangleShape() {
        DefineShape4Tag shape = shape();
        shape.shapes.fillStyles.fillStyles[0] = gradient(FILLSTYLE.LINEAR_GRADIENT);
        rectangle(shape, 0, 0, 1000, 1000, 1, 1);
        selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        return shape;
    }

    private static long edges(ShapeTag shape) {
        return shape.shapes.shapeRecords.stream().filter(record -> record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord).count();
    }

    private static void point(DisplayPoint point, int x, int y) {
        assertEquals(point.x, x);
        assertEquals(point.y, y);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    @Test
    public void selectToolMovesClosedCornersAndBendsEdgesWithFixedEndpoints() throws Exception {
        DefineShape4Tag original = rectangleShape();
        AffineTransform transform = new AffineTransform(0.12, 0.04, 0.03, 0.10, 50, 20);
        ShapePointEdit.OutlineHit corner = ShapePointEdit.findOutlineHit(original.shapes, 4, new Point2D.Double(20, -15), transform, 1);
        assertTrue(corner.isAnchor());
        ShapePointEdit anchorEdit = new ShapePointEdit(original);
        assertTrue(anchorEdit.reshapeOutline(corner, 120, -80));
        List<DisplayPoint> points = anchorEdit.getPoints();
        point(points.get(0), 120, -80);
        point(points.get(4), 120, -80);
        point(points.get(1), 1000, 0);
        PathIterator cornerOutline = ShapePointEdit.getOutline(anchorEdit.getShape().getShapes()).getPathIterator(null);
        double[] coordinates = new double[6];
        assertEquals(cornerOutline.currentSegment(coordinates), PathIterator.SEG_MOVETO);
        assertEquals(coordinates[0], 120.0);
        assertEquals(coordinates[1], -80.0);

        ShapePointEdit.OutlineHit edge = ShapePointEdit.findOutlineHit(original.shapes, 4, new Point2D.Double(500, 1000), transform, 1);
        assertFalse(edge.isAnchor());
        ShapePointEdit edgeEdit = new ShapePointEdit(original);
        assertTrue(edgeEdit.reshapeOutline(edge, 0, 150));
        points = edgeEdit.getPoints();
        point(points.get(2), 1000, 1000);
        point(points.get(3), 500, 1300);
        assertFalse(points.get(3).onPath);
        point(points.get(4), 0, 1000);
        PathIterator edgeOutline = ShapePointEdit.getOutline(edgeEdit.getShape().getShapes()).getPathIterator(null);
        edgeOutline.next();
        edgeOutline.next();
        edgeOutline.next();
        assertEquals(edgeOutline.currentSegment(coordinates), PathIterator.SEG_QUADTO);
        assertEquals(coordinates[0], 500.0);
        assertEquals(coordinates[1], 1300.0);
        assertEquals(coordinates[2], 0.0);
        assertEquals(coordinates[3], 1000.0);
        assertTrue(edgeEdit.reshapeOutline(edge, 0, -50));
        point(edgeEdit.getPoints().get(3), 500, 900);
        assertFalse(new ShapePointEdit(original).isChanged());
        assertEquals(edges(edgeEdit.getShape()), edges(original));
    }

    @Test
    public void selectToolGrabsThickStrokesAtTheirOuterEdgesAndCorners() {
        DefineShape4Tag shape = shape();
        shape.shapes.lineStyles.lineStyles2[0].width = 600;
        rectangle(shape, 0, 0, 1000, 1000, 1, 1);
        rectangle(shape, 2000, 0, 1000, 1000, 1, 0);
        AffineTransform placement = new AffineTransform();
        for (double scale : new double[]{0.025, 0.05, 0.1}) {
            Point2D corner = new Point2D.Double(-210, -210);
            ShapePointEdit.OutlineHit anchor = ShapePointEdit.findOutlineHit(shape.shapes, 4, corner, placement, scale);
            assertEquals(anchor.anchor, new Point2D.Double(0, 0));
            for (int y : new int[]{-299, 299}) {
                Point2D pointer = new Point2D.Double(500, y);
                ShapePointEdit.OutlineHit hit = ShapePointEdit.findOutlineHit(shape.shapes, 4, pointer, placement, scale);
                assertFalse(hit.isAnchor());
                assertEquals(hit.edgeIndex, 0);
            }
            assertNull(ShapePointEdit.findOutlineHit(shape.shapes, 4, new Point2D.Double(500, -301), placement, scale));
            assertNull(ShapePointEdit.findOutlineHit(shape.shapes, 4, new Point2D.Double(2500, -299), placement, scale));
        }
    }

    @Test
    public void selectToolUsesPlacementScaleOnlyForScalingStrokes() {
        DefineShape4Tag shape = rectangleShape();
        shape.shapes.lineStyles.lineStyles2[0].width = 600;
        AffineTransform placement = AffineTransform.getScaleInstance(2, 2);
        Point2D pointer = new Point2D.Double(500, -250);
        assertFalse(ShapePointEdit.findOutlineHit(shape.shapes, 4, pointer, placement, 0.05).isAnchor());
        shape.shapes.lineStyles.lineStyles2[0].noHScaleFlag = true;
        shape.shapes.lineStyles.lineStyles2[0].noVScaleFlag = true;
        assertNull(ShapePointEdit.findOutlineHit(shape.shapes, 4, pointer, placement, 0.05));
        assertFalse(ShapePointEdit.findOutlineHit(shape.shapes, 4, new Point2D.Double(500, -149), placement, 0.05).isAnchor());

        DefineShape3Tag legacy = new DefineShape3Tag(new SWF());
        LINESTYLE line = new LINESTYLE();
        line.width = 600;
        legacy.shapes.lineStyles.lineStyles = new LINESTYLE[]{line};
        legacy.shapes.shapeRecords.clear();
        rectangle(legacy, 0, 0, 1000, 1000, 0, 1);
        assertFalse(ShapePointEdit.findOutlineHit(legacy.shapes, 3, pointer, placement, 0.05).isAnchor());
    }

    @Test
    public void pointPreviewsAreDetachedPreserveStylesAndCommitAsOneUndoStep() throws Exception {
        DefineShape4Tag original = rectangleShape();
        byte[] before = original.getData();
        ShapePointEdit edit = new ShapePointEdit(original);
        assertFalse(edit.isChanged());
        List<DisplayPoint> points = edit.getPoints();
        points.get(1).x = 1400;
        points.get(1).y = -200;
        edit.updatePoints(points);
        assertTrue(edit.isChanged());
        point(new ShapePointEdit(edit.getShape()).getPoints().get(2), 1000, 1000);
        points.get(1).x = 1600;
        edit.updatePoints(points);
        ShapeTag result = edit.getShape();
        ShapeTag canonical = (ShapeTag) original.cloneTag();
        assertEquals(result.shapes.fillStyles.fillStyles, canonical.shapes.fillStyles.fillStyles);
        assertEquals(result.shapes.lineStyles.lineStyles2, canonical.shapes.lineStyles.lineStyles2);
        assertEquals(original.getData(), before);
        assertFalse(original.getSwf().isModified());
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, result, "Points", null), original.getSwf());
        byte[] after = current(original).getData();
        point(new ShapePointEdit(current(original)).getPoints().get(1), 1600, -200);
        undo.undo(original.getSwf());
        assertEquals(current(original).getData(), before);
        assertFalse(undo.canUndo(original.getSwf()));
        assertFalse(original.getSwf().isModified());
        undo.redo(original.getSwf());
        assertEquals(current(original).getData(), after);
        edit.updatePoints(new ShapePointEdit(original).getPoints());
        assertFalse(edit.isChanged());
    }

    @Test
    public void deletingTheDuplicateClosingAnchorKeepsAClosedContourAndSupportsUndo() throws Exception {
        DefineShape4Tag original = rectangleShape();
        byte[] before = original.getData();
        ShapePointEdit edit = new ShapePointEdit(original);
        edit.removePoints(Arrays.asList(0, 4));
        List<DisplayPoint> points = edit.getPoints();
        assertEquals(edges(edit.getShape()), 3L);
        point(points.get(0), 1000, 0);
        point(points.get(points.size() - 1), 1000, 0);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, edit.getShape(), "Delete points", null), original.getSwf());
        assertEquals(edges(current(original)), 3L);
        undo.undo(original.getSwf());
        assertEquals(current(original).getData(), before);
        undo.redo(original.getSwf());
        assertEquals(edges(current(original)), 3L);
    }

    @Test
    public void deletingInteriorAnchorsAndOpenEndpointsPreservesOtherCoordinates() throws Exception {
        DefineShape4Tag original = rectangleShape();
        ShapePointEdit edit = new ShapePointEdit(original);
        edit.removePoints(Collections.singletonList(1));
        assertEquals(edges(edit.getShape()), 3L);
        point(edit.getPoints().get(1), 1000, 1000);
        point(edit.getPoints().get(2), 0, 1000);
        DefineShape4Tag open = shape();
        move(open.shapes.shapeRecords, 100, 200, 0, 1);
        edge(open.shapes.shapeRecords, 500, 0);
        edge(open.shapes.shapeRecords, 0, 500);
        selection(open);
        edit = new ShapePointEdit(open);
        edit.removePoints(Collections.singletonList(0));
        assertEquals(edges(edit.getShape()), 1L);
        point(edit.getPoints().get(0), 600, 200);
        point(edit.getPoints().get(1), 600, 700);
        edit.removePoints(Collections.singletonList(1));
        assertEquals(edges(edit.getShape()), 0L);
        edit = new ShapePointEdit(original);
        edit.removePoints(Arrays.asList(0, 1, 2, 3, 4));
        assertEquals(edges(edit.getShape()), 0L);
    }

    @Test
    public void controlPointsCanMoveBeRemovedOrSplitWithoutMovingTheNextAnchor() throws Exception {
        DefineShape4Tag original = shape();
        move(original.shapes.shapeRecords, 0, 0, 1, 1);
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = 500;
        curve.controlDeltaY = -400;
        curve.anchorDeltaX = 500;
        curve.anchorDeltaY = 400;
        curve.calculateBits();
        original.shapes.shapeRecords.add(curve);
        edge(original.shapes.shapeRecords, 0, 1000);
        selection(original);
        ShapePointEdit edit = new ShapePointEdit(original);
        List<DisplayPoint> points = edit.getPoints();
        points.get(1).y = -800;
        edit.updatePoints(points);
        point(edit.getPoints().get(2), 1000, 0);
        point(edit.getPoints().get(3), 1000, 1000);
        edit.removePoints(Collections.singletonList(1));
        assertTrue(edit.getShape().shapes.shapeRecords.get(1) instanceof StraightEdgeRecord);
        point(edit.getPoints().get(1), 1000, 0);
        edit = new ShapePointEdit(original);
        edit.removePoints(Collections.singletonList(2));
        assertEquals(edges(edit.getShape()), 1L);
        point(edit.getPoints().get(1), 1000, 1000);
        edit = new ShapePointEdit(original);
        edit.splitEdge(1, 0.5);
        assertEquals(edges(edit.getShape()), 3L);
        point(edit.getPoints().get(2), 500, -200);
        point(edit.getPoints().get(4), 1000, 0);
        assertFalse(new ShapePointEdit(original).isChanged());
    }

    @Test
    public void identicalRecordsAndStyleChangesDoNotRedirectPointDeletion() throws Exception {
        DefineShape4Tag original = shape();
        move(original.shapes.shapeRecords, 0, 0, 0, 1);
        edge(original.shapes.shapeRecords, 1000, 0);
        edge(original.shapes.shapeRecords, 1000, 0);
        move(original.shapes.shapeRecords, 5000, 0, 1, 0);
        edge(original.shapes.shapeRecords, 1000, 0);
        selection(original);
        ShapePointEdit edit = new ShapePointEdit(original);
        edit.removePoints(Collections.singletonList(2));
        assertEquals(edges(edit.getShape()), 2L);
        point(edit.getPoints().get(1), 1000, 0);
        point(edit.getPoints().get(2), 5000, 0);
        point(edit.getPoints().get(3), 6000, 0);
        StyleChangeRecord first = (StyleChangeRecord) original.shapes.shapeRecords.get(0);
        first.stateMoveTo = false;
        edit = new ShapePointEdit(original);
        List<DisplayPoint> points = edit.getPoints();
        points.get(0).y = 200;
        edit.updatePoints(points);
        point(edit.getPoints().get(1), 1000, 0);
    }

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    @Test
    public void coordinateInputIsDeferredAndCanRefreshTheEditorWithoutReenteringItsDocument() throws Exception {
        AtomicReference<ImagePanel> panelRef = new AtomicReference<>();
        AtomicInteger updates = new AtomicInteger();
        AtomicReference<List<DisplayPoint>> edited = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                AppStrings.setResourceClass(MainFrame.class);
                ImagePanel panel = new ImagePanel();
                panelRef.set(panel);
                panel.setHilightedPoints(new ShapePointEdit(rectangleShape()).getPoints());
                panel.addPointUpdateListener(new PointUpdateListener() {
                    @Override
                    public void pointsUpdated(List<DisplayPoint> points) {
                        updates.incrementAndGet();
                        edited.set(new ArrayList<>(points));
                        panel.setHilightedPoints(new ArrayList<>(points));
                        panel.setSelectedPointIndices(Collections.singletonList(1));
                    }

                    @Override
                    public boolean edgeSplit(int position, double splitPoint) {
                        return false;
                    }

                    @Override
                    public boolean pointRemoved(int position) {
                        return false;
                    }
                });
                panel.setSelectedPointIndices(Collections.singletonList(1));
                assertEquals(updates.get(), 0);
                ((JTextField) field(panel, "pointXTextField")).setText("85");
                assertEquals(updates.get(), 0);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        SwingUtilities.invokeAndWait(() -> {
            try {
                assertEquals(updates.get(), 1);
                point(edited.get().get(1), 1700, 0);
            } finally {
                panelRef.get().clearAll();
            }
        });
    }

    private static Point2D screen(ImagePanel panel, DisplayPoint point) {
        return panel.getTimelinedToPanelTransform().transform(point.toPoint2D(), null);
    }

    @Test
    public void floatPositionPropertiesKeepFractionalCoordinatesOtherAxesAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel stage = new ImagePanel();
            ImagePanel legacy = new ImagePanel();
            try {
                ShapePointPropertiesPanel properties = new ShapePointPropertiesPanel(stage);
                FloatPropertyField x = (FloatPropertyField) field(properties, "xField");
                FloatPropertyField y = (FloatPropertyField) field(properties, "yField");
                assertFalse(x.isEnabled());
                assertNull(x.getValue());
                DefineShape4Tag original = rectangleShape();
                List<Integer> selected = Arrays.asList(1, 2);
                stage.setHilightedPoints(new ShapePointEdit(original).getPoints());
                stage.setSelectedPointIndices(selected);
                legacy.setHilightedPoints(new ShapePointEdit(original).getPoints());
                assertFalse(((Component) field(stage, "pointEditPanel")).isVisible());
                assertTrue(((Component) field(legacy, "pointEditPanel")).isVisible());
                assertTrue(field(legacy, "pointXTextField") instanceof JTextField);
                assertEquals(x.getValue(), Float.valueOf(50));
                assertEquals(y.getValue(), Float.valueOf(0));
                UndoManager undo = new UndoManager();
                AtomicInteger edits = new AtomicInteger();
                stage.addPointUpdateListener(new PointUpdateListener() {
                    @Override
                    public void pointsUpdated(List<DisplayPoint> points) {
                        try {
                            ShapeTag before = current(original);
                            ShapePointEdit edit = new ShapePointEdit(before);
                            edit.updatePoints(points);
                            undo.doOperation(new ShapeStyleChangeOperation(before, edit.getShape(), "Position", (tag, forward) -> {
                                try {
                                    stage.setHilightedPoints(new ShapePointEdit(tag).getPoints());
                                    stage.setSelectedPointIndices(selected);
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }), original.getSwf());
                            edits.incrementAndGet();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public boolean edgeSplit(int position, double splitPoint) {
                        return false;
                    }

                    @Override
                    public boolean pointRemoved(int position) {
                        return false;
                    }
                });
                x.setValue(12.35f);
                List<DisplayPoint> points = new ShapePointEdit(current(original)).getPoints();
                point(points.get(1), 247, 0);
                point(points.get(2), 247, 1000);
                y.setValue(-0.15f);
                points = new ShapePointEdit(current(original)).getPoints();
                point(points.get(1), 247, -3);
                point(points.get(2), 247, 997);
                x.setValue(12.35f);
                assertEquals(edits.get(), 2);
                undo.undo(original.getSwf());
                assertEquals(x.getValue(), Float.valueOf(12.35f));
                assertEquals(y.getValue(), Float.valueOf(0));
                undo.undo(original.getSwf());
                assertEquals(x.getValue(), Float.valueOf(50));
                assertFalse(undo.canUndo(original.getSwf()));
                undo.redo(original.getSwf());
                assertEquals(x.getValue(), Float.valueOf(12.35f));
                stage.setSelectedPointIndices(Collections.emptyList());
                assertFalse(x.isEnabled());
                assertNull(x.getValue());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                stage.clearAll();
                legacy.clearAll();
            }
        });
    }

    private static void mouse(Component canvas, int id, Point2D point) throws Exception {
        mouse(canvas, id, point, false);
    }

    private static void mouse(Component canvas, int id, Point2D point, boolean ctrl) throws Exception {
        int modifiers = (id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.BUTTON1_DOWN_MASK : 0) | (ctrl ? InputEvent.CTRL_DOWN_MASK : 0);
        MouseEvent event = new MouseEvent(canvas, id, 0, modifiers,
                (int) Math.round(point.getX()), (int) Math.round(point.getY()), 1, false,
                id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED ? MouseEvent.NOBUTTON : MouseEvent.BUTTON1);
        if (id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED) {
            for (Object handler : (List<?>) field(canvas, "mouseMotionListeners")) {
                if (id == MouseEvent.MOUSE_DRAGGED) {
                    ((MouseMotionListener) handler).mouseDragged(event);
                } else {
                    ((MouseMotionListener) handler).mouseMoved(event);
                }
            }
        } else {
            for (Object handler : (List<?>) field(canvas, "mouseListeners")) {
                if (id == MouseEvent.MOUSE_PRESSED) {
                    ((MouseListener) handler).mousePressed(event);
                } else if (id == MouseEvent.MOUSE_CLICKED) {
                    ((MouseListener) handler).mouseClicked(event);
                } else {
                    ((MouseListener) handler).mouseReleased(event);
                }
            }
        }
    }

    private static void key(Component canvas, int code) {
        for (KeyListener listener : canvas.getKeyListeners()) {
            listener.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, code, KeyEvent.CHAR_UNDEFINED));
        }
    }

    @Test
    public void ctrlSplittingAnEdgePreservesViewportThroughUndoRedo() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                DefineShape4Tag original = ShapeSceneEditTest.scene();
                SWF swf = original.getSwf();
                swf.setFile("point-split-viewport.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                panel.setSelectionMode(false);
                panel.setMultiSelect(false);
                panel.setAltSelectionEnabled(false);
                panel.setPointEditToolbarVisible(false);
                ShapePointEdit initial = new ShapePointEdit(original);
                panel.setHilightedPoints(initial.getPoints(), initial.getPathStarts());
                Zoom zoom = new Zoom();
                zoom.value = 1.37;
                panel.zoom(zoom);
                panel.setViewPosition(new Point2D.Double(-231.5, -571.25));
                UndoManager undo = new UndoManager();
                AtomicInteger splits = new AtomicInteger();
                panel.addPointUpdateListener(new PointUpdateListener() {
                    @Override
                    public void pointsUpdated(List<DisplayPoint> points) {
                        throw new AssertionError("Ctrl-click must only split the edge");
                    }

                    @Override
                    public boolean pointRemoved(int position) {
                        return false;
                    }

                    @Override
                    public boolean edgeSplit(int position, double splitPoint) {
                        try {
                            ShapePointEdit edit = new ShapePointEdit(current(original));
                            edit.splitEdge(position, splitPoint);
                            ImagePanel.ShapeEditContext context = panel.getShapeEditContext();
                            undo.doOperation(new ShapeStyleChangeOperation(current(original), edit.getShape(), "Split edge", (tag, forward) -> {
                                panel.restoreShapeEditContext(tag, context);
                                try {
                                    ShapePointEdit refreshed = new ShapePointEdit(tag);
                                    panel.setHilightedPoints(refreshed.getPoints(), refreshed.getPathStarts());
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }), swf);
                            splits.incrementAndGet();
                            return false;
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                Component canvas = (Component) field(panel, "iconPanel");
                Field ctrl = canvas.getClass().getDeclaredField("ctrlDown");
                ctrl.setAccessible(true);
                ctrl.set(canvas, true);
                Point2D hit = panel.getTimelinedToPanelTransform().transform(new Point2D.Double(2000, 0), null);
                AffineTransform before = panel.getTimelinedToPanelTransform();
                byte[] data = original.getData();
                mouse(canvas, MouseEvent.MOUSE_MOVED, hit, true);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, hit, true);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, hit, true);
                assertEquals(splits.get(), 1);
                assertEquals(edges(current(original)), edges(original) + 1);
                assertEquals(panel.getTimelinedToPanelTransform(), before, "Ctrl-click must not move or zoom the canvas");
                undo.undo(swf);
                assertEquals(current(original).getData(), data);
                assertEquals(panel.getTimelinedToPanelTransform(), before);
                undo.redo(swf);
                assertEquals(panel.getTimelinedToPanelTransform(), before);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }

    @Test
    public void placedPointsUseTheSceneMatrixAndCommitOrCancelOneGesture() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                DefineShape4Tag shape = ShapeSceneEditTest.scene();
                SWF swf = shape.getSwf();
                swf.setFile("point-edit-events.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                panel.setSelectionMode(false);
                panel.setMultiSelect(false);
                panel.setAltSelectionEnabled(false);
                Component canvas = (Component) field(panel, "iconPanel");
                List<DisplayPoint> original = new ShapePointEdit(shape).getPoints();
                panel.setHilightedPoints(new ShapePointEdit(shape).getPoints());
                AtomicInteger starts = new AtomicInteger();
                AtomicInteger finishes = new AtomicInteger();
                AtomicReference<Boolean> canceled = new AtomicReference<>();
                AtomicReference<List<DisplayPoint>> updated = new AtomicReference<>();
                AtomicReference<List<Integer>> deleted = new AtomicReference<>();
                panel.setPointEditGestureListeners(starts::incrementAndGet, cancel -> {
                    finishes.incrementAndGet();
                    canceled.set(cancel);
                });
                panel.addPointUpdateListener(new PointUpdateListener() {
                    @Override
                    public void pointsUpdated(List<DisplayPoint> points) {
                        updated.set(new ArrayList<>(points));
                    }

                    @Override
                    public boolean edgeSplit(int position, double splitPoint) {
                        return false;
                    }

                    @Override
                    public boolean pointRemoved(int position) {
                        throw new AssertionError("Delete must be batched");
                    }

                    @Override
                    public void pointsRemoved(List<Integer> positions) {
                        deleted.set(positions);
                    }
                });
                Point2D start = screen(panel, original.get(1));
                start = new Point((int) Math.round(start.getX()), (int) Math.round(start.getY()));
                Point2D end = new Point((int) start.getX() + 40, (int) start.getY() - 20);
                Point2D from = panel.toTimelinedPoint(start);
                Point2D to = panel.toTimelinedPoint(end);
                mouse(canvas, MouseEvent.MOUSE_MOVED, start);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                assertNull(updated.get(), "Populating the coordinate fields must not edit the shape");
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                assertEquals(starts.get(), 1);
                assertEquals(finishes.get(), 0);
                assertEquals(updated.get().get(1).x, original.get(1).x + to.getX() - from.getX(), 1.0);
                assertEquals(updated.get().get(1).y, original.get(1).y + to.getY() - from.getY(), 1.0);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                assertEquals(finishes.get(), 1);
                assertFalse(canceled.get());
                mouse(canvas, MouseEvent.MOUSE_CLICKED, end);
                assertEquals(panel.getSelectedPointIndices(), Collections.singletonList(1));
                panel.setHilightedPoints(new ShapePointEdit(shape).getPoints());
                mouse(canvas, MouseEvent.MOUSE_MOVED, start);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                key(canvas, KeyEvent.VK_ESCAPE);
                assertEquals(finishes.get(), 2);
                assertTrue(canceled.get());
                @SuppressWarnings("unchecked")
                List<DisplayPoint> restored = (List<DisplayPoint>) field(panel, "hilightedPoints");
                point(restored.get(1), original.get(1).x, original.get(1).y);
                panel.setSelectedPointIndices(Arrays.asList(1, 2));
                key(canvas, KeyEvent.VK_DELETE);
                assertEquals(deleted.get(), Arrays.asList(2, 1));
                // Screen-space rectangle selection uses the same placed coordinates.
                panel.setHilightedPoints(new ShapePointEdit(shape).getPoints());
                mouse(canvas, MouseEvent.MOUSE_MOVED, new Point(5, 5));
                Point2D anchor = screen(panel, original.get(2));
                mouse(canvas, MouseEvent.MOUSE_PRESSED, new Point2D.Double(anchor.getX() - 12, anchor.getY() - 12));
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, new Point2D.Double(anchor.getX() + 12, anchor.getY() + 12));
                mouse(canvas, MouseEvent.MOUSE_RELEASED, new Point2D.Double(anchor.getX() + 12, anchor.getY() + 12));
                assertEquals(panel.getSelectedPointIndices(), Collections.singletonList(2));
                AffineTransform placement = panel.getTimelinedToPanelTransform();
                assertTrue(Math.abs(placement.getShearX()) > 0);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setPointEditGestureListeners(null, null);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
