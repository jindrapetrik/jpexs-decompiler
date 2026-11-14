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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.panels.BrushPropertiesPanel;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeBrushTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(point(x, y), 0);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static ShapeSelection saved(ShapeTag shape) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shape.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return new ShapeSelection((ShapeTag) restored.getCharacter(shape.shapeId));
    }

    @Test
    public void everyTipMakesSingleStampsAndContinuousSparseMouseStrokes() {
        for (BrushShape tip : BrushShape.values()) {
            BrushStroke stroke = new BrushStroke(tip, 200, point(0, 0));
            assertTrue(stroke.getArea().contains(0, 0), tip.name());
            Rectangle2D bounds = stroke.getArea().getBounds2D();
            assertEquals(Math.max(bounds.getWidth(), bounds.getHeight()), 200.0, 0.01);
            stroke.add(point(10000, 5000));
            stroke.add(point(10000, -5000));
            Area geometry = stroke.getArea();
            for (int i = 0; i <= 100; i++) {
                assertTrue(geometry.contains(i * 100, i * 50), "Gap in " + tip);
                assertTrue(geometry.contains(10000, 5000 - i * 100), "Gap at corner in " + tip);
            }
            Area copy = stroke.getArea();
            copy.reset();
            assertFalse(stroke.getArea().isEmpty());
        }
        assertTrue(BrushShape.DIAGONAL_DOWN.tip(point(0, 0), 200).contains(50, 50));
        assertFalse(BrushShape.DIAGONAL_UP.tip(point(0, 0), 200).contains(50, 50));
    }

    @Test
    public void placedBrushTipsAndPaintStayAlignedWithTheScreen() throws Exception {
        AffineTransform placement = AffineTransform.getTranslateInstance(2500, 1800);
        placement.rotate(Math.PI / 4);
        placement.shear(0.3, -0.15);
        placement.scale(-2, 0.5);
        Point2D center = point(1000, 700);
        Point2D placedCenter = placement.transform(center, null);
        for (BrushShape tip : BrushShape.values()) {
            BrushStroke stroke = new BrushStroke(tip, 400, center, placement);
            Area displayed = new Area(placement.createTransformedShape(stroke.getArea()));
            Rectangle2D expected = tip.tip(placedCenter, 400).getBounds2D();
            assertEquals(displayed.getBounds2D().getWidth(), expected.getWidth(), 0.01, tip.name());
            assertEquals(displayed.getBounds2D().getHeight(), expected.getHeight(), 0.01, tip.name());
            Area cursor = new Area(placement.createTransformedShape(tip.tip(center, 400, placement)));
            assertEquals(cursor.getBounds2D().getWidth(), expected.getWidth(), 0.01);
            assertEquals(cursor.getBounds2D().getHeight(), expected.getHeight(), 0.01);
        }
        BrushStroke square = new BrushStroke(BrushShape.SQUARE, 400, center, placement);
        Area displayed = new Area(placement.createTransformedShape(square.getArea()));
        assertTrue(displayed.contains(placedCenter.getX() + 190, placedCenter.getY() + 190), "Square corners must not turn into a diamond");
        Point2D end = placement.createInverse().transform(point(placedCenter.getX() + 4000, placedCenter.getY()), null);
        square.add(end);
        displayed = new Area(placement.createTransformedShape(square.getArea()));
        for (int x = 0; x <= 4000; x += 100) {
            assertTrue(displayed.contains(placedCenter.getX() + x, placedCenter.getY() + 190));
        }
        DefineShape4Tag shape = shape();
        ShapeSelection model = selection(shape);
        ShapeTag painted = ShapeStyleChangeOperation.createShape(shape, model.paint(square.getArea(), fill(Color.black)).model);
        Rectangle2D savedBounds = placement.createTransformedShape(new ShapeSelection(painted).getParts().get(0).geometry).getBounds2D();
        assertEquals(savedBounds.getWidth(), 4400, 5);
        assertEquals(savedBounds.getHeight(), 400, 5);
    }

    @Test
    public void oneBrushGestureOverwritesFillsAndLinesAndRoundTripsWithOneUndo() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection original = selection(shape);
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        byte[] before = shape.getData();
        BrushStroke stroke = new BrushStroke(BrushShape.SQUARE, 400, point(1000, -1000));
        stroke.add(point(1000, 3000));
        ShapeSelection.BatchEdit painted = original.paint(stroke.getArea(), fill(Color.green));
        assertTrue(painted.changed);
        assertTrue(painted.selected.isEmpty());
        assertEquals(shape.getData(), before, "Preparing the preview must not mutate the SWF");
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(shape, painted.model, "Brush", null), shape.getSwf());
        ShapeSelection restored = saved(current(shape));
        assertEquals(at(restored, 1000, -1000).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 1000, 1000).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 500, 1000).fill.color.toColor(), Color.red);
        assertFalse(at(restored, 1000, 0).stroke);
        // Removing the new paint must not reveal the old fill or stroke underneath.
        ShapeSelection.BatchEdit erased = restored.deleteAll(Collections.singletonList(at(restored, 1000, 1000)));
        assertNull(at(erased.model, 1000, 1000));
        assertNull(at(erased.model, 1000, 0));
        byte[] after = current(shape).getData();
        undo.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(undo.canUndo(shape.getSwf()));
        undo.redo(shape.getSwf());
        assertEquals(current(shape).getData(), after);
    }

    @Test
    public void gradientAndBitmapPaintKeepTheirMatricesAndDetachedStyles() throws Exception {
        for (int type : new int[]{FILLSTYLE.LINEAR_GRADIENT, FILLSTYLE.RADIAL_GRADIENT, FILLSTYLE.REPEATING_BITMAP}) {
            DefineShape4Tag shape = shape();
            ShapeSelection original = selection(shape);
            shape.getSwf().addTag(shape);
            FILLSTYLE paint = new FILLSTYLE();
            paint.inShape3 = true;
            paint.fillStyleType = type;
            MATRIX matrix = new MATRIX();
            matrix.hasScale = true;
            matrix.scaleX = 0.1f;
            matrix.scaleY = 0.2f;
            matrix.translateX = 500;
            matrix.translateY = -200;
            if (paint.hasGradient()) {
                paint.gradientMatrix = matrix;
                paint.gradient = new GRADIENT();
                GRADRECORD first = new GRADRECORD();
                first.ratio = 0;
                first.color = new RGBA(255, 0, 0, 120);
                GRADRECORD last = new GRADRECORD();
                last.ratio = 255;
                last.color = new RGBA(0, 0, 255, 255);
                paint.gradient.gradientRecords = new GRADRECORD[]{first, last};
            } else {
                paint.bitmapMatrix = matrix;
                paint.bitmapId = 123;
            }
            BrushStroke stroke = new BrushStroke(BrushShape.CIRCLE, 500, point(0, 0));
            ShapeSelection.BatchEdit edit = original.paint(stroke.getArea(), paint);
            new ShapeStyleChangeOperation(shape, edit.model, "Brush", null).doOperation();
            FILLSTYLE restored = at(saved(current(shape)), 0, 0).fill;
            assertEquals(restored.fillStyleType, type);
            MATRIX restoredMatrix = paint.hasGradient() ? restored.gradientMatrix : restored.bitmapMatrix;
            assertEquals(restoredMatrix.translateX, 500);
            assertEquals(restoredMatrix.translateY, -200);
            assertEquals(restoredMatrix.scaleX, matrix.scaleX, 0.0001);
            matrix.translateX = 999;
            assertEquals((paint.hasGradient() ? edit.model.getParts().get(0).fill.gradientMatrix
                    : edit.model.getParts().get(0).fill.bitmapMatrix).translateX, 500);
        }
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @Test
    public void brushPropertiesOfferAllTipsAndNonEmptyFillTypes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                BrushPropertiesPanel panel = new BrushPropertiesPanel();
                assertEquals(panel.getBrushShape(), BrushShape.CIRCLE);
                assertEquals(panel.getBrushSize(), 10.0);
                Object fill = field(panel, "fillEditor");
                JComboBox<?> types = (JComboBox<?>) field(fill, "typeComboBox");
                assertEquals(types.getItemCount(), 4);
                assertEquals(panel.getFillStyle(new Rectangle2D.Double()).fillStyleType, FILLSTYLE.SOLID);
                JComboBox<?> tips = (JComboBox<?>) field(panel, "shapeEditor");
                assertEquals(tips.getItemCount(), 8);
                types.setSelectedIndex(1);
                assertEquals(panel.getFillStyle(new Rectangle2D.Double(0, 0, 1000, 400)).fillStyleType, FILLSTYLE.LINEAR_GRADIENT);
                types.setSelectedIndex(2);
                assertEquals(panel.getFillStyle(new Rectangle2D.Double()).fillStyleType, FILLSTYLE.RADIAL_GRADIENT);
                types.setSelectedIndex(3);
                assertNotNull(panel.getFillStyle(new Rectangle2D.Double()), "An unavailable bitmap must preserve a valid paint");
                assertTrue(types.getSelectedIndex() < 3);
                ((FloatPropertyField) field(panel, "sizeEditor")).setValue(40f);
                assertEquals(panel.getBrushSize(), 40.0);
                SWF swf = new SWF();
                DefineBitsLossless2Tag first = new DefineBitsLossless2Tag(swf);
                swf.addTag(first);
                panel.setSwf(swf);
                types.setSelectedIndex(3);
                assertEquals(panel.getFillStyle(new Rectangle2D.Double()).bitmapId, first.getCharacterId());
                DefineBitsLossless2Tag second = new DefineBitsLossless2Tag(swf);
                swf.addTag(second);
                panel.setSwf(swf);
                JComboBox<?> bitmaps = (JComboBox<?>) field(fill, "bitmapComboBox");
                assertEquals(bitmaps.getItemCount(), 2);
                bitmaps.setSelectedIndex(1);
                panel.setSwf(swf);
                assertEquals(panel.getFillStyle(new Rectangle2D.Double()).bitmapId, second.getCharacterId());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void drawingEventsUseSceneCoordinatesAndDoNotStartRectangleSelection() throws Exception {
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
                swf.setFile("brush-events.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(false);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                AtomicInteger rectangles = new AtomicInteger();
                panel.setRectangleSelectionListener((area, additive) -> rectangles.incrementAndGet());
                List<Point2D> points = new ArrayList<>();
                AtomicInteger commits = new AtomicInteger();
                AtomicInteger cancellations = new AtomicInteger();
                panel.setShapeDrawingTool(point -> BrushShape.SQUARE.tip(point, 200), new ImagePanel.ShapeDrawListener() {
                    @Override
                    public void begin(Point2D point) {
                        points.add(point);
                    }

                    @Override
                    public void update(Point2D point, boolean finished) {
                        points.add(point);
                        if (finished) {
                            commits.incrementAndGet();
                        }
                    }

                    @Override
                    public void cancel() {
                        cancellations.incrementAndGet();
                    }
                });
                Component canvas = (Component) field(panel, "iconPanel");
                AffineTransform transform = panel.getTimelinedToPanelTransform();
                Point2D start = transform.transform(point(1000, 700), null);
                Point2D end = transform.transform(point(2500, 1500), null);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, MouseEvent.BUTTON1);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end, MouseEvent.NOBUTTON);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end, MouseEvent.BUTTON1);
                assertEquals(commits.get(), 1);
                assertEquals(rectangles.get(), 0);
                assertTrue(points.get(0).distance(point(1000, 700)) < 60);
                assertTrue(points.get(points.size() - 1).distance(point(2500, 1500)) < 60);
                for (MouseListener listener : panel.getMouseListeners()) {
                    listener.mouseExited(new MouseEvent(panel, MouseEvent.MOUSE_EXITED, 0, 0, -1, -1, 0, false));
                }
                assertNull(field(panel, "shapeDrawingPosition"), "The brush outline must disappear outside the canvas");
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, MouseEvent.BUTTON1);
                MouseEvent outside = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, 0, 0, 820, 620, 1, false, MouseEvent.BUTTON1);
                for (MouseListener listener : panel.getMouseListeners()) {
                    listener.mouseReleased(outside);
                }
                assertEquals(commits.get(), 2, "Releasing outside the canvas still finishes the stroke");
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start, MouseEvent.BUTTON1);
                for (KeyListener listener : canvas.getKeyListeners()) {
                    listener.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end, MouseEvent.BUTTON1);
                assertEquals(commits.get(), 2, "Escape cancels without committing");
                assertEquals(cancellations.get(), 1);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setShapeDrawingTool(null, null);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }

    private static void mouse(Component canvas, int id, Point2D point, int button) throws Exception {
        MouseEvent event = new MouseEvent(canvas, id, 0, id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.BUTTON1_DOWN_MASK : 0,
                (int) Math.round(point.getX()), (int) Math.round(point.getY()), 1, false, button);
        if (id == MouseEvent.MOUSE_DRAGGED) {
            for (Object handler : (List<?>) field(canvas, "mouseMotionListeners")) {
                MouseMotionListener listener = (MouseMotionListener) handler;
                listener.mouseDragged(event);
            }
        } else {
            for (Object handler : (List<?>) field(canvas, "mouseListeners")) {
                MouseListener listener = (MouseListener) handler;
                if (id == MouseEvent.MOUSE_PRESSED) {
                    listener.mousePressed(event);
                } else {
                    listener.mouseReleased(event);
                }
            }
        }
    }
}
