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
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeLassoSelectionTest {

    private static Path2D.Double polygon(double... coordinates) {
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        path.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 2) {
            path.lineTo(coordinates[i], coordinates[i + 1]);
        }
        path.closePath();
        return path;
    }

    private static ShapeSelection.Selection at(ShapeSelection model, int x, int y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    @Test
    public void freehandSelectionsClipFillAndStrokeUniteWithRectanglesAndKeepUndo() throws Exception {
        DefineShape4Tag original = shape();
        rectangle(original, 0, 0, 4000, 2000, 1, 1);
        ShapeSelection model = selection(original);
        original.getSwf().addTag(original);
        original.setModified(false);
        original.getSwf().setModified(false);
        byte[] before = original.getData();
        Path2D.Double triangle = polygon(500, -200, 2500, -200, 1500, 1500);
        ShapeSelection.RectangleSelection lasso = model.selectArea(triangle, Collections.emptyList(), false);
        assertTrue(lasso.selected.stream().anyMatch(item -> item.stroke));
        assertTrue(lasso.selected.stream().anyMatch(item -> !item.stroke));
        ShapeSelection.RectangleSelection box = lasso.model.selectRectangle(new java.awt.geom.Rectangle2D.Double(3000, -200, 500, 700), lasso.selected, true);
        ShapeSelection.RectangleSelection combined = box.model.selectArea(polygon(3100, 200, 3800, 200, 3800, 1200, 3500, 1200, 3500, 600, 3100, 600), box.selected, true);
        Area selectedFill = new Area();
        for (ShapeSelection.Selection selected : combined.selected) {
            if (!selected.stroke) {
                selectedFill.add(new Area(selected.geometry));
            }
        }
        assertTrue(selectedFill.contains(1500, 300));
        assertFalse(selectedFill.contains(2400, 1200));
        assertTrue(selectedFill.contains(3250, 300));
        assertTrue(selectedFill.contains(3650, 1000));
        assertFalse(selectedFill.contains(3250, 1000));
        ShapeSelection.BatchEdit changed = combined.model.changeAll(combined.selected, item -> fill(Color.green), item -> {
            LINESTYLE2 line = Helper.deepCopy((LINESTYLE2) item.line);
            line.width = 400;
            line.color = new RGBA(Color.blue);
            return line;
        });
        assertEquals(original.getData(), before, "Selection and previews must not mutate the SWF");
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, changed.model, "Lasso styles", null), original.getSwf());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeSelection saved = new ShapeSelection((ShapeTag) restored.getCharacter(original.shapeId));
        assertEquals(at(saved, 1500, 300).fill.color.toColor(), Color.green);
        assertEquals(at(saved, 2400, 1200).fill.color.toColor(), Color.red);
        assertEquals(at(saved, 3650, 1000).fill.color.toColor(), Color.green);
        assertEquals(at(saved, 3250, 1000).fill.color.toColor(), Color.red);
        assertEquals(at(saved, 1500, 0).line.getWidth(), 400);
        assertEquals(at(saved, 1500, 0).line.getColor().toColor(), Color.blue);
        assertEquals(at(saved, 250, 0).line.getWidth(), 200);
        undo.undo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), before);
        assertFalse(undo.canUndo(original.getSwf()));
        undo.redo(original.getSwf());
        assertEquals(at(new ShapeSelection((ShapeTag) original.getSwf().getCharacter(original.shapeId)), 1500, 300).fill.color.toColor(), Color.green);
    }

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static MouseEvent mouse(Component canvas, int id, Point2D point, boolean shift) throws Exception {
        int modifiers = shift ? InputEvent.SHIFT_DOWN_MASK : 0;
        if (id == MouseEvent.MOUSE_DRAGGED) {
            modifiers |= MouseEvent.BUTTON1_DOWN_MASK;
        }
        MouseEvent event = new MouseEvent(canvas, id, 0, modifiers, (int) Math.round(point.getX()), (int) Math.round(point.getY()),
                1, false, id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED ? MouseEvent.NOBUTTON : MouseEvent.BUTTON1);
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
                } else if (id == MouseEvent.MOUSE_RELEASED) {
                    ((MouseListener) handler).mouseReleased(event);
                } else {
                    ((MouseListener) handler).mouseClicked(event);
                }
            }
        }
        return event;
    }

    private static Point2D screen(ImagePanel panel, double x, double y) {
        return panel.getTimelinedToPanelTransform().transform(new Point2D.Double(x, y), null);
    }

    private static void lasso(ImagePanel panel, Component canvas, boolean shift, double... coordinates) throws Exception {
        mouse(canvas, MouseEvent.MOUSE_PRESSED, screen(panel, coordinates[0], coordinates[1]), shift);
        for (int i = 2; i < coordinates.length; i += 2) {
            mouse(canvas, MouseEvent.MOUSE_DRAGGED, screen(panel, coordinates[i], coordinates[i + 1]), shift);
        }
        mouse(canvas, MouseEvent.MOUSE_RELEASED, screen(panel, coordinates[coordinates.length - 2], coordinates[coordinates.length - 1]), shift);
    }

    @Test
    public void clickingAnEdgeRefreshesTheMoveCursorWithoutMouseMotion() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                panel.setSize(800, 600);
                panel.doLayout();
                Component canvas = (Component) field(panel, "iconPanel");
                Point click = SwingUtilities.convertPoint(canvas, canvas.getWidth() / 2, canvas.getHeight() / 2, panel);
                AtomicBoolean selected = new AtomicBoolean();
                panel.setRectangleSelectionListener((area, additive) -> { });
                panel.setSelectionMoveListeners(point -> selected.get(), null);
                panel.setShapeReshapeHitTest(point -> new ImagePanel.ShapeReshapeGesture() {
                    @Override
                    public boolean isAnchor() {
                        return false;
                    }

                    @Override
                    public Shape getOutline() {
                        return null;
                    }

                    @Override
                    public Point2D getAnchor() {
                        return null;
                    }

                    @Override
                    public void update(Point2D point, boolean finished) {
                    }

                    @Override
                    public void cancel() {
                    }
                });
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!e.isConsumed()) {
                            selected.set(true);
                        }
                    }
                });
                panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_MOVED, 0, 0,
                        click.x, click.y, 0, false, MouseEvent.NOBUTTON));
                assertSame(canvas.getCursor(), field(panel, "reshapeEdgeCursor"));
                for (int id : new int[]{MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED}) {
                    panel.dispatchEvent(new MouseEvent(panel, id, 0, 0, click.x, click.y, 1, false, MouseEvent.BUTTON1));
                }
                assertTrue(selected.get());
                assertSame(canvas.getCursor(), field(panel, "moveCursor"));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    @Test
    public void placedFreehandGestureClosesOnReleaseAddsWithShiftAndCancelsWithoutChangingSelection() throws Exception {
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
                swf.setFile("lasso-selection-events.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                panel.setSelectionMode(false);
                panel.setAltSelectionEnabled(false);
                Component canvas = (Component) field(panel, "iconPanel");
                AtomicReference<ShapeSelection> model = new AtomicReference<>(new ShapeSelection(original));
                AtomicReference<List<ShapeSelection.Selection>> selected = new AtomicReference<>(new ArrayList<>());
                AtomicReference<Shape> region = new AtomicReference<>();
                AtomicReference<Boolean> additive = new AtomicReference<>();
                AtomicInteger selections = new AtomicInteger();
                BiConsumer<Shape, Boolean> listener = (area, add) -> {
                    region.set(area);
                    additive.set(add);
                    selections.incrementAndGet();
                    ShapeSelection.RectangleSelection result = model.get().selectArea(area, selected.get(), add);
                    model.set(result.model);
                    selected.set(result.selected);
                };
                panel.setAreaSelectionListener(listener, true);
                panel.setShapeDrawingTool(null, null);
                assertSame(canvas.getCursor(), field(panel, "lassoCursor"));
                panel.setSelectionMoveListeners(point -> model.get().selectionContains(selected.get(), point, 3, panel.getTimelinedToPanelTransform()), (point, finished) -> {
                    throw new AssertionError("A drag starting outside the selection must draw a lasso");
                });
                Point2D start = screen(panel, 500, -200);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, false);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, screen(panel, 2500, -200), false);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, screen(panel, 1500, 1500), false);
                assertEquals(selections.get(), 0);
                assertFalse(((Path2D) field(panel, "lassoSelectionPath")).getCurrentPoint().equals(start));
                mouse(canvas, MouseEvent.MOUSE_RELEASED, screen(panel, 1500, 1500), false);
                assertFalse(additive.get());
                assertEquals(selections.get(), 1);
                assertTrue(region.get().contains(1500, 300));
                assertFalse(region.get().contains(2400, 1200));
                boolean closed = false;
                for (PathIterator it = region.get().getPathIterator(null); !it.isDone(); it.next()) {
                    closed |= it.currentSegment(new double[6]) == PathIterator.SEG_CLOSE;
                }
                assertTrue(closed);
                assertTrue(selected.get().stream().anyMatch(item -> item.stroke));
                List<ShapeSelection.Selection> first = selected.get();
                panel.setAreaSelectionListener(listener, false);
                assertSame(canvas.getCursor(), field(panel, "defaultCursor"));
                assertSame(selected.get(), first);
                panel.setAreaSelectionListener(listener, true);
                assertSame(selected.get(), first);
                lasso(panel, canvas, true, 3000, -200, 4200, -200, 4200, 900, 3600, 900, 3600, 400, 3000, 400);
                assertEquals(selections.get(), 2);
                assertTrue(additive.get());
                Area selectedFill = new Area();
                for (ShapeSelection.Selection item : selected.get()) {
                    if (!item.stroke) {
                        selectedFill.add(new Area(item.geometry));
                    }
                }
                assertTrue(selectedFill.contains(1500, 300));
                assertTrue(selectedFill.contains(3800, 700));
                assertFalse(selectedFill.contains(3200, 700));
                List<ShapeSelection.Selection> combined = selected.get();
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, false);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, screen(panel, 3500, 2000), false);
                for (KeyListener handler : canvas.getKeyListeners()) {
                    handler.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                mouse(canvas, MouseEvent.MOUSE_RELEASED, start, false);
                assertEquals(selections.get(), 2);
                assertSame(selected.get(), combined);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, true);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, start, true);
                assertTrue(mouse(canvas, MouseEvent.MOUSE_CLICKED, start, true).isConsumed());
                assertEquals(selections.get(), 3);
                assertEquals(selected.get().size(), combined.size());
                List<ShapeSelection.Selection> beforeMove = selected.get();
                AtomicReference<Point2D> delta = new AtomicReference<>();
                AtomicInteger previews = new AtomicInteger();
                AtomicInteger commits = new AtomicInteger();
                panel.setSelectionMoveListeners(point -> model.get().selectionContains(selected.get(), point, 3, panel.getTimelinedToPanelTransform()), (point, finished) -> {
                    delta.set(point);
                    if (finished) {
                        commits.incrementAndGet();
                    } else {
                        previews.incrementAndGet();
                    }
                });
                Point2D inside = screen(panel, 1500, 300);
                inside = new Point2D.Double(Math.round(inside.getX()), Math.round(inside.getY()));
                Point2D destination = new Point2D.Double(inside.getX() + 35, inside.getY() - 20);
                Point2D from = panel.toTimelinedPoint(inside);
                Point2D to = panel.toTimelinedPoint(destination);
                panel.setAreaSelectionListener(listener, false);
                mouse(canvas, MouseEvent.MOUSE_MOVED, inside, false);
                assertSame(canvas.getCursor(), field(panel, "moveCursor"));
                mouse(canvas, MouseEvent.MOUSE_MOVED, start, false);
                assertSame(canvas.getCursor(), field(panel, "defaultCursor"));
                mouse(canvas, MouseEvent.MOUSE_MOVED, inside, true);
                assertSame(canvas.getCursor(), field(panel, "defaultCursor"));
                panel.setAreaSelectionListener(listener, true);
                mouse(canvas, MouseEvent.MOUSE_MOVED, inside, false);
                assertSame(canvas.getCursor(), field(panel, "moveCursor"));
                mouse(canvas, MouseEvent.MOUSE_MOVED, start, false);
                assertSame(canvas.getCursor(), field(panel, "lassoCursor"));
                mouse(canvas, MouseEvent.MOUSE_MOVED, inside, true);
                assertSame(canvas.getCursor(), field(panel, "lassoCursor"));
                mouse(canvas, MouseEvent.MOUSE_PRESSED, inside, false);
                assertSame(canvas.getCursor(), field(panel, "moveCursor"));
                assertNull(field(panel, "lassoSelectionPath"));
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, destination, false);
                assertSame(canvas.getCursor(), field(panel, "moveCursor"));
                assertEquals(previews.get(), 1);
                assertEquals(commits.get(), 0);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, destination, false);
                assertEquals(commits.get(), 1);
                assertEquals(delta.get().getX(), to.getX() - from.getX(), 1e-6);
                assertEquals(delta.get().getY(), to.getY() - from.getY(), 1e-6);
                assertEquals(selections.get(), 3);
                assertSame(selected.get(), beforeMove);
                // Shift must start another lasso even inside the selected fill.
                lasso(panel, canvas, true, 1500, 300, 2800, 300, 2800, 1300, 1500, 1300);
                assertEquals(selections.get(), 4);
                assertTrue(additive.get());
                assertEquals(commits.get(), 1);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setAreaSelectionListener(null, false);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
