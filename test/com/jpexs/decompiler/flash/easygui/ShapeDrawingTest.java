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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.IntegerPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.panels.DrawingShapesPropertiesPanel;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeDrawingTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static ShapeDrawingGesture gesture(DrawingShape kind) {
        return new ShapeDrawingGesture(kind, point(1000, 2000), new AffineTransform(), 8, 5, 100);
    }

    private static List<Point2D> vertices(Shape shape) {
        List<Point2D> points = new ArrayList<>();
        double[] coords = new double[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                points.add(point(coords[0], coords[1]));
            }
        }
        return points;
    }

    @Test
    public void boxesCirclesAndOvalsUseStageBoundsInEveryDragDirection() {
        for (DrawingShape kind : new DrawingShape[]{DrawingShape.RECTANGLE, DrawingShape.ROUND_RECTANGLE,
            DrawingShape.OVAL, DrawingShape.CIRCLE, DrawingShape.SQUARE}) {
            ShapeDrawingGesture drawing = gesture(kind);
            drawing.update(point(200, 1600));
            assertTrue(drawing.release());
            Rectangle2D bounds = drawing.getGeometry().getBounds2D();
            assertEquals(bounds.getX(), 200.0);
            assertEquals(bounds.getWidth(), 800.0);
            boolean constrained = kind == DrawingShape.CIRCLE || kind == DrawingShape.SQUARE;
            assertEquals(bounds.getY(), constrained ? 1200.0 : 1600.0);
            assertEquals(bounds.getHeight(), constrained ? 800.0 : 400.0);
        }
        ShapeDrawingGesture line = gesture(DrawingShape.LINE);
        line.update(point(1500, 1900));
        assertEquals(vertices(line.getGeometry()).get(0), point(1000, 2000));
        assertEquals(vertices(line.getGeometry()).get(1), point(1500, 1900));
        ShapeDrawingGesture rounded = gesture(DrawingShape.ROUND_RECTANGLE);
        rounded.update(point(1800, 2400));
        assertFalse(rounded.getGeometry().contains(1001, 2001));
        assertTrue(rounded.getGeometry().contains(1100, 2100));
        ShapeDrawingGesture clamped = new ShapeDrawingGesture(DrawingShape.ROUND_RECTANGLE, point(0, 0), new AffineTransform(), 6, 5, 99999);
        clamped.update(point(1000, 200));
        assertTrue(clamped.getGeometry().contains(500, 100));
    }

    @Test
    public void regularPolygonsRotateTheirFirstVertexTowardsTheMouse() {
        DrawingShape[] kinds = {DrawingShape.TRIANGLE, DrawingShape.PENTAGON, DrawingShape.POLYGON};
        int[] counts = {3, 5, 8};
        for (int i = 0; i < kinds.length; i++) {
            ShapeDrawingGesture drawing = gesture(kinds[i]);
            drawing.update(point(1300, 2400));
            List<Point2D> vertices = vertices(drawing.getGeometry());
            assertEquals(vertices.size(), counts[i]);
            assertEquals(vertices.get(0).distance(point(1300, 2400)), 0.0, 0.0001);
            for (Point2D vertex : vertices) {
                assertEquals(vertex.distance(point(1000, 2000)), 500.0, 0.0001);
            }
            drawing.update(point(600, 2300));
            assertEquals(vertices(drawing.getGeometry()).get(0).distance(point(600, 2300)), 0.0, 0.0001);
        }
    }

    @Test
    public void starLocksItsInnerRadiusThenSetsTheOuterRadiusAndRotation() {
        ShapeDrawingGesture star = gesture(DrawingShape.STAR);
        star.update(point(1100, 2000));
        assertFalse(star.release());
        assertTrue(star.isAwaitingOuterRadius());
        assertNotNull(star.getGuide());
        star.update(point(1000, 2300));
        List<Point2D> vertices = vertices(star.getGeometry());
        assertEquals(vertices.size(), 10);
        assertEquals(vertices.get(0).distance(point(1000, 2300)), 0.0, 0.0001);
        for (int i = 0; i < vertices.size(); i++) {
            assertEquals(vertices.get(i).distance(point(1000, 2000)), i % 2 == 0 ? 300.0 : 100.0, 0.0001);
        }
        assertTrue(star.release());
        ShapeDrawingGesture zero = gesture(DrawingShape.STAR);
        assertTrue(zero.release());
        assertFalse(zero.isAwaitingOuterRadius());
    }

    @Test
    public void placedPrimitivesKeepTheirStageGeometryAndRotation() throws Exception {
        AffineTransform placement = AffineTransform.getTranslateInstance(2000, 500);
        placement.rotate(Math.PI / 4);
        placement.shear(0.3, -0.2);
        placement.scale(-2, 0.5);
        Point2D start = placement.createInverse().transform(point(1000, 2000), null);
        Point2D end = placement.createInverse().transform(point(1800, 2400), null);
        ShapeDrawingGesture box = new ShapeDrawingGesture(DrawingShape.RECTANGLE, start, placement, 6, 5, 100);
        box.update(end);
        Rectangle2D bounds = placement.createTransformedShape(box.getGeometry()).getBounds2D();
        assertEquals(bounds.getX(), 1000.0, 0.0001);
        assertEquals(bounds.getY(), 2000.0, 0.0001);
        assertEquals(bounds.getWidth(), 800.0, 0.0001);
        assertEquals(bounds.getHeight(), 400.0, 0.0001);
        ShapeDrawingGesture polygon = new ShapeDrawingGesture(DrawingShape.POLYGON, start, placement, 7, 5, 0);
        polygon.update(end);
        List<Point2D> vertices = vertices(placement.createTransformedShape(polygon.getGeometry()));
        assertEquals(vertices.size(), 7);
        assertEquals(vertices.get(0).distance(point(1800, 2400)), 0.0, 0.0001);
    }

    @Test
    public void fillAndStrokeAreSavedTogetherInOneUndoAndRedoStep() throws Exception {
        DefineShape4Tag shape = shape();
        ShapeSelection model = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        ShapeDrawingGesture box = gesture(DrawingShape.ROUND_RECTANGLE);
        box.update(point(2000, 3000));
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 100;
        line.color = new RGBA(Color.blue);
        ShapeSelection.BatchEdit edit = model.drawShape(box.getGeometry(), fill(Color.red), line);
        assertTrue(edit.changed);
        assertEquals(edit.model.getParts().size(), 2);
        ShapeStyleChangeOperation.createShape(shape, edit.model);
        assertEquals(shape.getData(), before);
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Draw shape", null), shape.getSwf());
        ShapeTag current = (ShapeTag) shape.getSwf().getCharacter(shape.shapeId);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shape.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeSelection parsed = new ShapeSelection((ShapeTag) restored.getCharacter(shape.shapeId));
        assertEquals(parsed.select(point(1500, 2500), 0).fill.color.toColor(), Color.red);
        ShapeSelection.Selection stroke = parsed.select(point(1000, 2500), 0);
        assertTrue(stroke.stroke);
        assertEquals(stroke.line.getColor().toColor(), Color.blue);
        byte[] after = current.getData();
        undo.undo(shape.getSwf());
        assertEquals(((ShapeTag) shape.getSwf().getCharacter(shape.shapeId)).getData(), before);
        assertFalse(undo.canUndo(shape.getSwf()));
        assertFalse(shape.getSwf().isModified());
        undo.redo(shape.getSwf());
        assertEquals(((ShapeTag) shape.getSwf().getCharacter(shape.shapeId)).getData(), after);
    }

    @Test
    public void noneStylesAndLinesDoNotCreateUnwantedFillsOrHistory() {
        ShapeSelection empty = selection(shape());
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 20;
        line.color = new RGBA(Color.black);
        ShapeDrawingGesture box = gesture(DrawingShape.RECTANGLE);
        box.update(point(2000, 3000));
        assertFalse(empty.drawShape(box.getGeometry(), null, null).changed);
        ShapeSelection outlined = empty.drawShape(box.getGeometry(), null, line).model;
        assertEquals(outlined.getParts().size(), 1);
        assertTrue(outlined.getParts().get(0).stroke);
        ShapeSelection filled = empty.drawShape(box.getGeometry(), fill(Color.red), null).model;
        assertEquals(filled.getParts().size(), 1);
        assertNull(filled.getParts().get(0).line);
        ShapeDrawingGesture segment = gesture(DrawingShape.LINE);
        segment.update(point(2000, 3000));
        ShapeSelection drawn = empty.drawShape(segment.getGeometry(), fill(Color.red), line).model;
        assertEquals(drawn.getParts().size(), 1);
        assertTrue(drawn.getParts().get(0).stroke);
        assertFalse(empty.drawShape(gesture(DrawingShape.RECTANGLE).getGeometry(), fill(Color.red), line).changed);
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void layoutTree(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layoutTree((Container) child);
            }
        }
    }

    @Test
    public void propertiesShowTheRelevantOptionsAndIndependentNoneStyles() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                DrawingShapesPropertiesPanel panel = new DrawingShapesPropertiesPanel();
                Map<?, ?> kinds = (Map<?, ?>) field(panel, "kindButtons");
                assertEquals(kinds.size(), 10);
                assertEquals(panel.getKind(), DrawingShape.RECTANGLE);
                panel.setSize(480, 900);
                layoutTree(panel);
                JToggleButton firstSwatch = (JToggleButton) kinds.get(DrawingShape.LINE);
                Point palettePosition = SwingUtilities.convertPoint(firstSwatch, 0, 0, panel);
                Component palette = (Component) field(panel, "kindEditor");
                Component paletteLabel = palette.getParent().getComponent(0);
                int paletteLeft = SwingUtilities.convertPoint(paletteLabel, 0, 0, panel).x;
                int paletteRight = SwingUtilities.convertPoint(palette, palette.getWidth(), 0, panel).x;
                assertEquals((paletteLeft + paletteRight) / 2.0, panel.getWidth() / 2.0, 1.0);
                Component parameters = ((Component) field(panel, "sidesEditor")).getParent();
                Point parameterPosition = SwingUtilities.convertPoint(parameters, 0, 0, panel);
                assertEquals(parameterPosition.x + parameters.getWidth() / 2.0, panel.getWidth() / 2.0, 1.0);
                for (Object value : kinds.values()) {
                    JToggleButton button = (JToggleButton) value;
                    assertNotNull(button.getIcon());
                    assertNotNull(button.getToolTipText());
                    assertEquals(button.getAccessibleContext().getAccessibleName(), button.getToolTipText());
                    button.doClick();
                    layoutTree(panel);
                    assertEquals(SwingUtilities.convertPoint(firstSwatch, 0, 0, panel), palettePosition,
                            "The palette must stay fixed when the options below change");
                    assertEquals(SwingUtilities.convertPoint(parameters, 0, 0, panel), parameterPosition,
                            "The centered parameter block must stay fixed when its contents change");
                }
                Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1000, 500);
                assertNotNull(panel.getFill(bounds));
                assertNotNull(panel.getStroke(bounds));
                Object styles = field(panel, "styles");
                ((JComboBox<?>) field(field(styles, "fillEditor"), "typeComboBox")).setSelectedIndex(0);
                assertNull(panel.getFill(bounds));
                assertNotNull(panel.getStroke(bounds));
                ((JComboBox<?>) field(field(styles, "strokeEditor"), "typeComboBox")).setSelectedIndex(0);
                assertNull(panel.getStroke(bounds));
                ((JToggleButton) kinds.get(DrawingShape.POLYGON)).doClick();
                assertEquals(panel.getKind(), DrawingShape.POLYGON);
                ((IntegerPropertyField) field(panel, "sidesEditor")).setValue(9);
                layoutTree(panel);
                Point parameterFieldPosition = SwingUtilities.convertPoint((Component) field(panel, "sidesEditor"), 0, 0, panel);
                assertEquals(panel.getSides(), 9);
                assertTrue(((Component) field(panel, "sidesEditor")).isVisible());
                ((JToggleButton) kinds.get(DrawingShape.STAR)).doClick();
                layoutTree(panel);
                assertEquals(SwingUtilities.convertPoint((Component) field(panel, "anglesEditor"), 0, 0, panel), parameterFieldPosition);
                assertEquals(panel.getKind(), DrawingShape.STAR);
                assertFalse(((Component) field(panel, "sidesEditor")).isVisible());
                ((IntegerPropertyField) field(panel, "anglesEditor")).setValue(7);
                assertEquals(panel.getAngles(), 7);
                String hint = ((JLabel) field(panel, "starHint")).getText();
                panel.setOuterRadiusPending(true);
                layoutTree(panel);
                assertEquals(SwingUtilities.convertPoint(firstSwatch, 0, 0, panel), palettePosition);
                assertFalse(hint.equals(((JLabel) field(panel, "starHint")).getText()));
                ((JToggleButton) kinds.get(DrawingShape.ROUND_RECTANGLE)).doClick();
                ((JToggleButton) kinds.get(DrawingShape.ROUND_RECTANGLE)).doClick();
                assertEquals(panel.getKind(), DrawingShape.ROUND_RECTANGLE);
                layoutTree(panel);
                assertEquals(SwingUtilities.convertPoint((Component) field(panel, "radiusEditor"), 0, 0, panel), parameterFieldPosition);
                assertEquals(kinds.values().stream().filter(button -> ((JToggleButton) button).isSelected()).count(), 1L);
                ((FloatPropertyField) field(panel, "radiusEditor")).setValue(12.5f);
                assertEquals(panel.getCornerRadius(), 250.0);
                panel.setSwf(new SWF());
                assertNull(panel.getFill(bounds));
                assertNull(panel.getStroke(bounds));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void starPendingStateReceivesHoverAndCancelsAfterMouseRelease() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires desktop graphics");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                SWF swf = ShapeSceneEditTest.scene().getSwf();
                swf.setFile("star-events.swf");
                panel.setSelectionMode(false);
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                AtomicReference<ShapeDrawingGesture> gesture = new AtomicReference<>();
                AtomicInteger commits = new AtomicInteger();
                AtomicInteger hovers = new AtomicInteger();
                AtomicInteger cancellations = new AtomicInteger();
                panel.setShapeDrawingTool(point -> ShapeDrawingGesture.cursor(point, new AffineTransform(), 80), new ImagePanel.ShapeDrawListener() {
                    @Override
                    public void begin(Point2D point) {
                        if (gesture.get() == null) {
                            gesture.set(new ShapeDrawingGesture(DrawingShape.STAR, point, new AffineTransform(), 6, 5, 0));
                        }
                        gesture.get().update(point);
                    }

                    @Override
                    public void update(Point2D point, boolean finished) {
                        gesture.get().update(point);
                        if (finished && gesture.get().release()) {
                            commits.incrementAndGet();
                            gesture.set(null);
                        }
                    }

                    @Override
                    public boolean isPending() {
                        return gesture.get() != null && gesture.get().isAwaitingOuterRadius();
                    }

                    @Override
                    public void move(Point2D point) {
                        if (isPending()) {
                            hovers.incrementAndGet();
                            gesture.get().update(point);
                        }
                    }

                    @Override
                    public void cancel() {
                        gesture.set(null);
                        cancellations.incrementAndGet();
                    }
                });
                Component canvas = (Component) field(panel, "iconPanel");
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 100, 100);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 140, 100);
                assertTrue(gesture.get().isAwaitingOuterRadius());
                assertEquals(commits.get(), 0);
                mouse(canvas, MouseEvent.MOUSE_MOVED, 180, 150);
                assertEquals(hovers.get(), 1);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 180, 150);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 180, 150);
                assertEquals(commits.get(), 1);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 100, 100);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 140, 100);
                for (KeyListener listener : canvas.getKeyListeners()) {
                    listener.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                assertNull(gesture.get());
                assertEquals(cancellations.get(), 1);
                assertEquals(commits.get(), 1);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 100, 100);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 140, 100);
                panel.setShapeDrawingGuide(gesture.get().getGuide());
                panel.setShapeDrawingTool(null, null);
                assertEquals(cancellations.get(), 2);
                assertNull(field(panel, "shapeDrawingGuide"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setShapeDrawingTool(null, null);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }

    private static void mouse(Component canvas, int id, int x, int y) throws Exception {
        MouseEvent event = new MouseEvent(canvas, id, 0, 0, x, y, 1, false,
                id == MouseEvent.MOUSE_MOVED ? MouseEvent.NOBUTTON : MouseEvent.BUTTON1);
        if (id == MouseEvent.MOUSE_MOVED) {
            for (Object handler : (List<?>) field(canvas, "mouseMotionListeners")) {
                ((MouseMotionListener) handler).mouseMoved(event);
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
}
