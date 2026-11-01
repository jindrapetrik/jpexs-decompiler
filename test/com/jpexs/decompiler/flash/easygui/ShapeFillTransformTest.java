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
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.Helper;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeFillTransformTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(point(x, y), 0);
    }

    @Test
    public void gradientPreviewIsDetachedAndOnlyChangesTheClickedConnectedRegion() throws Exception {
        DefineShape4Tag original = shape();
        original.shapes.fillStyles.fillStyles[0] = gradient(FILLSTYLE.LINEAR_GRADIENT);
        rectangle(original, 0, 0, 1000, 1000, 1, 0);
        rectangle(original, 2000, 0, 1000, 1000, 1, 0);
        ShapeSelection before = selection(original);
        original.getSwf().addTag(original);
        original.setModified(false);
        original.getSwf().setModified(false);
        byte[] bytes = original.getData();
        ShapeSelection.Selection selected = at(before, 500, 500);
        assertTrue(ShapeFillTransform.canTransform(selected));
        ShapeFillTransform gesture = new ShapeFillTransform(before, selected);
        MATRIX matrix = Helper.deepCopy(selected.fill.gradientMatrix);
        matrix.translateX += 200;
        ShapeSelection.BatchEdit preview = gesture.setMatrix(matrix);
        assertTrue(preview.changed);
        assertEquals(at(preview.model, 500, 500).fill.gradientMatrix.translateX, matrix.translateX);
        assertEquals(at(preview.model, 2500, 500).fill.gradientMatrix.translateX, selected.fill.gradientMatrix.translateX);
        assertEquals(original.getData(), bytes);
        assertFalse(original.getSwf().isModified());
        matrix.translateX += 300;
        ShapeSelection.BatchEdit finished = gesture.setMatrix(matrix);
        assertEquals(at(finished.model, 500, 500).fill.gradientMatrix.translateX, matrix.translateX);
        assertEquals(at(preview.model, 500, 500).fill.gradientMatrix.translateX, matrix.translateX - 300);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, finished.model, "Transform", null), original.getSwf());
        byte[] after = ((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData();
        undo.undo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), bytes);
        assertFalse(undo.canUndo(original.getSwf()));
        assertFalse(original.getSwf().isModified());
        undo.redo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), after);
        assertFalse(gesture.setMatrix(selected.fill.gradientMatrix).changed);
    }

    @Test
    public void bitmapAndFocalGradientKeepTheirOtherParameters() {
        for (int type : new int[]{FILLSTYLE.FOCAL_RADIAL_GRADIENT, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP}) {
            DefineShape4Tag original = shape();
            FILLSTYLE fill = type == FILLSTYLE.FOCAL_RADIAL_GRADIENT ? gradient(type) : new FILLSTYLE();
            if (type != FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                fill.fillStyleType = type;
                fill.bitmapId = 77;
                fill.bitmapMatrix = new Matrix(20, 30, 2, 3, 400, 500).toMATRIX();
            }
            original.shapes.fillStyles.fillStyles[0] = fill;
            rectangle(original, 0, 0, 1000, 1000, 1, 0);
            ShapeSelection model = selection(original);
            ShapeSelection.Selection selected = at(model, 500, 500);
            ShapeFillTransform gesture = new ShapeFillTransform(model, selected);
            MATRIX matrix = Helper.deepCopy(type == FILLSTYLE.FOCAL_RADIAL_GRADIENT ? fill.gradientMatrix : fill.bitmapMatrix);
            matrix.translateY += 456;
            ShapeSelection.BatchEdit moved = gesture.setMatrix(matrix);
            FILLSTYLE result = at(moved.model, 500, 500).fill;
            assertEquals(result.fillStyleType, type);
            if (fill.hasBitmap()) {
                assertEquals(result.bitmapId, 77);
                assertEquals(result.bitmapMatrix, matrix);
            } else {
                ShapeSelection.BatchEdit focused = gesture.setFocalPoint(-0.5f);
                result = at(focused.model, 500, 500).fill;
                assertEquals(result.gradientMatrix, matrix);
                assertEquals(((FOCALGRADIENT) result.gradient).focalPoint, -0.5f);
                assertEquals(((FOCALGRADIENT) selected.fill.gradient).focalPoint, 0.375f);
            }
        }
    }

    @Test
    public void gradientStrokeTransformOnlyChangesTheSelectedEdgeAndKeepsItsLineStyle() {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 200;
        line.hasFillFlag = true;
        line.fillType = gradient(FILLSTYLE.RADIAL_GRADIENT);
        line.noHScaleFlag = true;
        ShapeSelection model = selection(shape()).drawStroke(new Line2D.Double(0, 0, 1000, 0), line).model;
        model = model.drawStroke(new Line2D.Double(0, 2000, 1000, 2000), line).model;
        ShapeSelection.Selection selected = at(model, 500, 0);
        assertTrue(ShapeFillTransform.canTransform(selected));
        MATRIX matrix = Helper.deepCopy(line.fillType.gradientMatrix);
        matrix.translateX += 700;
        ShapeSelection.BatchEdit edit = new ShapeFillTransform(model, selected).setMatrix(matrix);
        LINESTYLE2 actual = (LINESTYLE2) at(edit.model, 500, 0).line;
        assertEquals(actual.fillType.gradientMatrix, matrix);
        assertEquals(actual.width, 200);
        assertTrue(actual.noHScaleFlag);
        assertEquals(((LINESTYLE2) at(edit.model, 500, 2000).line).fillType.gradientMatrix, line.fillType.gradientMatrix);
        assertFalse(ShapeFillTransform.canTransform(null));
    }

    private static LINESTYLE2 gradientLine() {
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 100;
        line.hasFillFlag = true;
        line.fillType = gradient(FILLSTYLE.LINEAR_GRADIENT);
        return line;
    }

    private static MATRIX strokeMatrix(ShapeSelection model, double x, double y) {
        return ((LINESTYLE2) at(model, x, y).line).fillType.gradientMatrix;
    }

    @Test
    public void selectsMatchingStylesAroundOneFaceIncludingNonAdjacentEdgesWithOneUndoStep() throws Exception {
        DefineShape4Tag original = shape();
        LINESTYLE2 matching = gradientLine();
        LINESTYLE2 other = Helper.deepCopy(matching);
        other.width = 200;
        original.shapes.lineStyles.lineStyles2 = new LINESTYLE2[]{matching, other};
        move(original.shapes.shapeRecords, 0, 0, 0, 1);
        edge(original.shapes.shapeRecords, 1000, 0);
        move(original.shapes.shapeRecords, 1000, 0, 0, 2);
        edge(original.shapes.shapeRecords, 0, 1000);
        move(original.shapes.shapeRecords, 1000, 1000, 0, 1);
        edge(original.shapes.shapeRecords, -1000, 0);
        edge(original.shapes.shapeRecords, 0, -1000);
        rectangle(original, 2000, 0, 1000, 1000, 0, 1);
        ShapeSelection model = selection(original);
        original.getSwf().addTag(original);
        byte[] before = original.getData();
        ShapeSelection.RectangleSelection selected = model.selectFillTransform(point(500, 0), new AffineTransform());
        assertEquals(selected.selected.size(), 3);
        MATRIX matrix = Helper.deepCopy(matching.fillType.gradientMatrix);
        matrix.translateX += 500;
        ShapeSelection.BatchEdit edit = new ShapeFillTransform(selected.model, selected.selected).setMatrix(matrix);
        assertEquals(edit.selected.size(), 3);
        for (Point2D point : new Point2D[]{point(500, 0), point(500, 1000), point(0, 500)}) {
            assertEquals(strokeMatrix(edit.model, point.getX(), point.getY()), matrix);
        }
        assertEquals(at(edit.model, 1000, 500).line.getWidth(), 200);
        assertEquals(strokeMatrix(edit.model, 1000, 500), matching.fillType.gradientMatrix);
        assertEquals(strokeMatrix(edit.model, 2500, 0), matching.fillType.gradientMatrix);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, edit.model, "Transform", null), original.getSwf());
        byte[] after = ((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData();
        undo.undo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), before);
        assertFalse(undo.canUndo(original.getSwf()));
        undo.redo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), after);
    }

    @Test
    public void aSharedStrokeSelectsTheFaceOnTheClickedSideWithoutSelectingItsNeighbour() {
        LINESTYLE2 line = gradientLine();
        ShapeSelection model = selection(shape()).drawShape(new Rectangle2D.Double(0, 0, 2000, 2000), null, line).model;
        model = model.drawStroke(new Line2D.Double(1000, 0, 1000, 2000), line).model;
        AffineTransform placement = AffineTransform.getRotateInstance(Math.PI / 4);
        placement.scale(2, 0.5);
        ShapeSelection.RectangleSelection selected = model.selectFillTransform(point(990, 500), placement);
        assertEquals(selected.selected.size(), 4);
        MATRIX matrix = Helper.deepCopy(line.fillType.gradientMatrix);
        matrix.translateY += 700;
        ShapeSelection.BatchEdit edit = new ShapeFillTransform(selected.model, selected.selected).setMatrix(matrix);
        for (Point2D point : new Point2D[]{point(500, 0), point(500, 2000), point(0, 500), point(1000, 500)}) {
            assertEquals(strokeMatrix(edit.model, point.getX(), point.getY()), matrix);
        }
        assertEquals(strokeMatrix(edit.model, 1500, 0), line.fillType.gradientMatrix);
        assertEquals(strokeMatrix(edit.model, 1500, 2000), line.fillType.gradientMatrix);
        assertEquals(strokeMatrix(edit.model, 2000, 500), line.fillType.gradientMatrix);
    }

    @Test
    public void clickingOutsideHalfOfACurvedOutlineSelectsItsWholeContour() {
        LINESTYLE2 line = gradientLine();
        ShapeSelection model = selection(shape()).drawShape(new Ellipse2D.Double(0, 0, 2000, 1000), null, line).model;
        model = model.drawShape(new Ellipse2D.Double(4000, 0, 2000, 1000), null, line).model;
        ShapeSelection.RectangleSelection selected = model.selectFillTransform(point(2010, 500), new AffineTransform());
        assertTrue(selected.selected.size() >= 4);
        MATRIX matrix = Helper.deepCopy(line.fillType.gradientMatrix);
        matrix.translateX += 300;
        ShapeSelection.BatchEdit edit = new ShapeFillTransform(selected.model, selected.selected).setMatrix(matrix);
        for (Point2D point : new Point2D[]{point(0, 500), point(2000, 500), point(1000, 0), point(1000, 1000)}) {
            assertEquals(strokeMatrix(edit.model, point.getX(), point.getY()), matrix);
        }
        assertEquals(strokeMatrix(edit.model, 5000, 0), line.fillType.gradientMatrix);
    }

    @Test
    public void bitmapStrokeGroupsShareOneMatrixAndOpenStrokesKeepSingleEdgeSelection() {
        LINESTYLE2 line = gradientLine();
        line.fillType = new FILLSTYLE();
        line.fillType.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
        line.fillType.bitmapId = 7;
        line.fillType.bitmapMatrix = new Matrix(20, 20, 0, 0, 100, 200).toMATRIX();
        ShapeSelection model = selection(shape()).drawShape(new Rectangle2D.Double(0, 0, 1000, 1000), null, line).model;
        ShapeSelection.RectangleSelection selected = model.selectFillTransform(point(500, -10), new AffineTransform());
        assertEquals(selected.selected.size(), 4);
        MATRIX matrix = Helper.deepCopy(line.fillType.bitmapMatrix);
        matrix.translateX += 300;
        ShapeSelection.BatchEdit edit = new ShapeFillTransform(selected.model, selected.selected).setMatrix(matrix);
        for (Point2D point : new Point2D[]{point(0, 500), point(1000, 500), point(500, 0), point(500, 1000)}) {
            assertEquals(((LINESTYLE2) at(edit.model, point.getX(), point.getY()).line).fillType.bitmapMatrix, matrix);
        }
        model = selection(shape()).drawStroke(new Line2D.Double(0, 0, 1000, 0), line).model;
        selected = model.selectFillTransform(point(500, 0), new AffineTransform());
        assertEquals(selected.selected.size(), 1);
    }

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static void mouse(Component canvas, int id, Point2D point) throws Exception {
        MouseEvent event = new MouseEvent(canvas, id, 0, id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.BUTTON1_DOWN_MASK : 0,
                (int) Math.round(point.getX()), (int) Math.round(point.getY()), 1, false,
                id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.NOBUTTON : MouseEvent.BUTTON1);
        if (id == MouseEvent.MOUSE_DRAGGED) {
            for (Object handler : (List<?>) field(canvas, "mouseMotionListeners")) {
                ((MouseMotionListener) handler).mouseDragged(event);
            }
        } else {
            for (Object handler : (List<?>) field(canvas, "mouseListeners")) {
                if (id == MouseEvent.MOUSE_PRESSED) {
                    ((MouseListener) handler).mousePressed(event);
                } else {
                    ((MouseListener) handler).mouseReleased(event);
                }
            }
        }
    }

    @Test
    public void placedHandlesTakePriorityOverTheClickToolAndSupportCancel() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                DefineShape4Tag shape = ShapeSceneEditTest.scene();
                SWF swf = shape.getSwf();
                swf.setFile("gradient-transform-events.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(false);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                Component canvas = (Component) field(panel, "iconPanel");
                AtomicInteger clicked = new AtomicInteger();
                panel.setShapeDrawingTool(null, new ImagePanel.ShapeDrawListener() {
                    @Override
                    public void begin(Point2D point) {
                        clicked.incrementAndGet();
                    }

                    @Override
                    public void update(Point2D point, boolean finished) { }

                    @Override
                    public void cancel() { }
                });
                AtomicInteger starts = new AtomicInteger();
                AtomicInteger finishes = new AtomicInteger();
                AtomicReference<Boolean> canceled = new AtomicReference<>();
                panel.setGradientTransformGestureListeners(starts::incrementAndGet, cancel -> {
                    finishes.incrementAndGet();
                    canceled.set(cancel);
                });
                MATRIX matrix = gradient(FILLSTYLE.LINEAR_GRADIENT).gradientMatrix;
                AtomicReference<MATRIX> updated = new AtomicReference<>(matrix);
                panel.setGradientTransform(matrix, true, updated::set);
                Point2D center = panel.getTimelinedToPanelTransform().transform(point(matrix.translateX, matrix.translateY), null);
                Point2D end = point(Math.round(center.getX()) + 35, Math.round(center.getY()) - 20);
                Point2D localStart = panel.toTimelinedPoint(new Point((int) Math.round(center.getX()), (int) Math.round(center.getY())));
                Point2D localEnd = panel.toTimelinedPoint(end);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, center);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                assertEquals(clicked.get(), 0);
                assertEquals(starts.get(), 1);
                assertEquals(finishes.get(), 1);
                assertFalse(canceled.get());
                assertEquals(updated.get().translateX, matrix.translateX + localEnd.getX() - localStart.getX(), 1.0);
                assertEquals(updated.get().translateY, matrix.translateY + localEnd.getY() - localStart.getY(), 1.0);
                MATRIX committed = updated.get();
                center = panel.getTimelinedToPanelTransform().transform(point(committed.translateX, committed.translateY), null);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, center);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, point(center.getX() - 40, center.getY() + 10));
                for (KeyListener listener : canvas.getKeyListeners()) {
                    listener.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                assertTrue(canceled.get());
                assertEquals(updated.get(), committed);
                assertEquals(finishes.get(), 2);
                // The bitmap Y-axis uses image pixels as its units.
                MATRIX bitmap = new Matrix(20, 20, 0, 0, 1000, 1000).toMATRIX();
                panel.setBitmapTransform(bitmap, 128, 64, updated::set);
                Point2D axis = panel.getTimelinedToPanelTransform().transform(new Matrix(bitmap).transform(point(0, 64)), null);
                Point2D axisEnd = point(Math.round(axis.getX()) + 15, Math.round(axis.getY()) + 25);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, axis);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, axisEnd);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, axisEnd);
                assertEquals(clicked.get(), 0);
                assertEquals(updated.get().translateX, bitmap.translateX);
                assertEquals(updated.get().translateY, bitmap.translateY);
                assertFalse(updated.get().equals(bitmap));
                MATRIX radial = new Matrix(0.1, 0.1, 0, 0, 1500, 1000).toMATRIX();
                AtomicReference<Float> focal = new AtomicReference<>(0.25f);
                panel.setFocalGradientTransform(radial, 0.25f, updated::set, focal::set);
                Method focalShape = ImagePanel.class.getDeclaredMethod("getFocalGradientHandleShape");
                focalShape.setAccessible(true);
                Shape triangle = (Shape) focalShape.invoke(panel);
                double cx = 0;
                double cy = 0;
                int count = 0;
                double[] coords = new double[6];
                for (PathIterator it = triangle.getPathIterator(null); !it.isDone(); it.next()) {
                    int type = it.currentSegment(coords);
                    if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                        cx += coords[0];
                        cy += coords[1];
                        count++;
                    }
                }
                Point2D focalStart = point(cx / count, cy / count);
                Point2D tip = panel.getTimelinedToPanelTransform().transform(new Matrix(radial).transform(point(0.25 * 16384, 0)), null);
                Point2D target = panel.getTimelinedToPanelTransform().transform(new Matrix(radial).transform(point(0.75 * 16384, 0)), null);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, focalStart);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, point(focalStart.getX() + target.getX() - tip.getX(), focalStart.getY() + target.getY() - tip.getY()));
                mouse(canvas, MouseEvent.MOUSE_RELEASED, target);
                assertEquals(focal.get(), 0.75f, 0.02f);
                assertEquals(clicked.get(), 0);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearGradientTransform();
                panel.setGradientTransformGestureListeners(null, null);
                panel.setShapeDrawingTool(null, null);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
