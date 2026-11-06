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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.types.RECT;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ImagePanelZoomTest {

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    @DataProvider(name = "views")
    public Object[][] views() {
        List<Object[]> result = new ArrayList<>();
        for (int mode = 0; mode < 4; mode++) {
            for (boolean fit : new boolean[]{false, true}) {
                for (boolean lowQuality : new boolean[]{false, true}) {
                    result.add(new Object[]{mode, fit, lowQuality});
                }
            }
        }
        return result.toArray(new Object[0][]);
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layout((Container) child);
            }
        }
    }

    private static void settle(ImagePanel panel, Component canvas) throws Exception {
        Method calcRect = canvas.getClass().getDeclaredMethod("calcRect");
        calcRect.setAccessible(true);
        Method update = ImagePanel.class.getDeclaredMethod("updateScrollBars");
        update.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            layout(panel);
            calcRect.invoke(canvas);
            update.invoke(panel);
        }
        layout(panel);
    }

    private static Rectangle2D sceneBounds(ImagePanel panel) {
        RECT rect = panel.getTopTimelined().getRect();
        AffineTransform transform = panel.getTimelinedToPanelTransform();
        transform.concatenate(panel.getParentMatrix().inverse().toTransform());
        return transform.createTransformedShape(new Rectangle2D.Double(rect.Xmin, rect.Ymin, rect.getWidth(), rect.getHeight())).getBounds2D();
    }

    private static void assertView(ImagePanel panel, Component canvas, Point2D local, Point cursor) throws Exception {
        Rectangle2D bounds = sceneBounds(panel);
        Point2D after = panel.getTimelinedToPanelTransform().transform(local, null);
        boolean fitsX = bounds.getWidth() <= canvas.getWidth() + 1e-7;
        boolean fitsY = bounds.getHeight() <= canvas.getHeight() + 1e-7;
        assertEquals(((JScrollBar) field(panel, "horizontalScrollBar")).isVisible(), !fitsX);
        assertEquals(((JScrollBar) field(panel, "verticalScrollBar")).isVisible(), !fitsY);
        if (fitsX) {
            assertEquals(bounds.getCenterX(), canvas.getWidth() / 2.0, 1e-6, "A fitting scene must be centered horizontally");
        } else {
            assertEquals(after.getX(), cursor.getX(), 1e-6, "The overflowing axis must retain the wheel cursor anchor");
        }
        if (fitsY) {
            assertEquals(bounds.getCenterY(), canvas.getHeight() / 2.0, 1e-6, "A fitting scene must be centered vertically");
        } else {
            assertEquals(after.getY(), cursor.getY(), 1e-6, "The overflowing axis must retain the wheel cursor anchor");
        }
    }

    private static void wheel(ImagePanel panel, Component canvas, Point cursor, int rotation) throws Exception {
        Point2D local = panel.toTimelinedPoint(cursor);
        MouseWheelEvent event = new MouseWheelEvent(canvas, MouseWheelEvent.MOUSE_WHEEL, 0, InputEvent.CTRL_DOWN_MASK,
                cursor.x, cursor.y, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, rotation);
        for (Object listener : (List<?>) field(canvas, "mouseWheelListeners")) {
            ((MouseWheelListener) listener).mouseWheelMoved(event);
        }
        settle(panel, canvas);
        assertView(panel, canvas, local, cursor);
        // Repeated render/layout passes must not change the viewport again.
        settle(panel, canvas);
        assertView(panel, canvas, local, cursor);
    }

    @Test
    public void stageDropUsesTheZoomedNestedViewport() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                SWF swf = ShapeSceneEditTest.scene().getSwf();
                panel.setSize(800, 600);
                panel.setSelectionMode(true);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                Zoom zoom = new Zoom();
                zoom.value = 1.37;
                panel.zoom(zoom);
                panel.setViewPosition(new Point2D.Double(-124.25, -257.5));
                Component canvas = (Component) field(panel, "iconPanel");
                settle(panel, canvas);

                Point cursor = new Point(123, 174);
                Point2D local = panel.toTimelinedPoint(cursor);
                Point drop = SwingUtilities.convertPoint(canvas, cursor, panel);
                Point2D dropPoint = panel.getStageDropPoint(drop);
                assertEquals(dropPoint.getX(), local.getX(), 1e-6);
                assertEquals(dropPoint.getY(), local.getY(), 1e-6);
                assertNull(panel.getStageDropPoint(SwingUtilities.convertPoint(canvas, new Point(-1, 174), panel)));

                Path2D.Double outline = new Path2D.Double();
                outline.moveTo(-250, 0);
                outline.lineTo(130, 750);
                outline.lineTo(420, 85);
                outline.closePath();
                AffineTransform transform = panel.getTimelinedToPanelTransform();
                Rectangle2D bounds = transform.createTransformedShape(outline).getBounds2D();
                Point2D anchor = transform.inverseTransform(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY()), null);
                Point translation = EasySwfPanel.getStageDropTranslation(dropPoint, anchor);
                transform.translate(translation.x, translation.y);
                Rectangle2D placed = transform.createTransformedShape(outline).getBounds2D();
                assertEquals(placed.getCenterX(), cursor.x, 0.5 * (Math.abs(transform.getScaleX()) + Math.abs(transform.getShearX())) + 1e-6);
                assertEquals(placed.getCenterY(), cursor.y, 0.5 * (Math.abs(transform.getShearY()) + Math.abs(transform.getScaleY())) + 1e-6);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }

    @Test(dataProvider = "views")
    public void wheelCentersFittingAxesAndAnchorsOverflowingAxes(int mode, boolean fit, boolean lowQuality) throws Exception {
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
                swf.setFile("wheel-zoom-" + mode + "-" + fit + "-" + lowQuality + ".swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(true);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                if (mode >= 1) {
                    panel.openDepth(0, 2);
                }
                if (mode >= 2) {
                    panel.openDepth(0, 2);
                }
                if (mode == 3) {
                    panel.setPointEditToolbarVisible(false);
                    ShapePointEdit edit = new ShapePointEdit(shape);
                    panel.setHilightedPoints(edit.getPoints(), edit.getPathStarts());
                }
                Field quality = panel.getClass().getDeclaredField("lowQuality");
                quality.setAccessible(true);
                quality.set(panel, lowQuality);
                if (fit) {
                    panel.zoomFit();
                } else {
                    Zoom zoom = new Zoom();
                    zoom.value = 1.37;
                    panel.zoom(zoom);
                    panel.setViewPosition(new Point2D.Double(-124.25, -257.5));
                }
                Component canvas = (Component) field(panel, "iconPanel");
                Field ctrl = canvas.getClass().getDeclaredField("ctrlDown");
                ctrl.setAccessible(true);
                ctrl.set(canvas, true);
                settle(panel, canvas);
                // No preceding mouse-move/render event: wheel coordinates are authoritative.
                Point first = new Point(123, 174);
                wheel(panel, canvas, first, -1);
                assertFalse(panel.getZoom().fit);
                wheel(panel, canvas, first, 1);
                Point second = new Point(511, 327);
                for (int i = 0; i < 4; i++) {
                    wheel(panel, canvas, second, -1);
                }
                for (int i = 0; i < 4; i++) {
                    wheel(panel, canvas, second, 1);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }

    @DataProvider(name = "sceneSizes")
    public Object[][] sceneSizes() {
        return new Object[][]{{600, 400, false, false}, {1000, 400, true, false},
            {600, 900, false, true}, {1000, 900, true, true}, {790, 570, false, false},
            {790, 900, true, true}, {1000, 570, true, true}};
    }

    @DataProvider(name = "scrollViews")
    public Object[][] scrollViews() {
        return new Object[][]{{0, false}, {1, false}, {2, false}, {3, false},
            {0, true}, {1, true}, {2, true}, {3, true}};
    }

    private static void scroll(ImagePanel panel, JScrollBar bar, int value, boolean horizontal) {
        Point2D local = new Point2D.Double(1000, 700);
        Point2D before = panel.getTimelinedToPanelTransform().transform(local, null);
        int oldValue = bar.getValue();
        Rectangle2D bounds = sceneBounds(panel);
        RECT rect = panel.getTopTimelined().getRect();
        double scale = bounds.getWidth() / rect.getWidth();
        double oldOffset = horizontal ? bounds.getMinX() - rect.Xmin * scale : bounds.getMinY() - rect.Ymin * scale;
        bar.setValue(value);
        assertTrue(bar.getValue() != oldValue, "The scrollbar must have room to scroll");
        Point2D after = panel.getTimelinedToPanelTransform().transform(local, null);
        // Zoom keeps fractional twips while scrollbar values are integral.
        double delta = -bar.getValue() * scale - oldOffset;
        assertEquals(after.getX(), before.getX() + (horizontal ? delta : 0), 1e-6, "Scrollbar must move the displayed scene");
        assertEquals(after.getY(), before.getY() + (horizontal ? 0 : delta), 1e-6, "Scrollbar must move the displayed scene");
    }

    @Test(dataProvider = "scrollViews")
    public void scrollbarsMoveZoomedTimelinesSpritesShapesAndEditedPoints(int mode, boolean lowQuality) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                DefineShape4Tag shape = ShapeSceneEditTest.scene();
                SWF swf = shape.getSwf();
                swf.setFile("scroll-" + mode + "-" + lowQuality + ".swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(true);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                if (mode >= 1) {
                    panel.openDepth(0, 2);
                }
                if (mode >= 2) {
                    panel.openDepth(0, 2);
                }
                if (mode == 3) {
                    panel.setPointEditToolbarVisible(false);
                    ShapePointEdit edit = new ShapePointEdit(shape);
                    panel.setHilightedPoints(edit.getPoints(), edit.getPathStarts());
                }
                Field quality = ImagePanel.class.getDeclaredField("lowQuality");
                quality.setAccessible(true);
                quality.set(panel, lowQuality);
                Zoom zoom = new Zoom();
                zoom.value = 3;
                panel.zoom(zoom);
                Component canvas = (Component) field(panel, "iconPanel");
                settle(panel, canvas);
                JScrollBar horizontal = (JScrollBar) field(panel, "horizontalScrollBar");
                JScrollBar vertical = (JScrollBar) field(panel, "verticalScrollBar");
                assertTrue(horizontal.isVisible());
                assertTrue(vertical.isVisible());
                // Both discrete changes and intermediate thumb-drag values must move the canvas.
                scroll(panel, horizontal, horizontal.getValue() + 300, true);
                vertical.setValueIsAdjusting(true);
                scroll(panel, vertical, vertical.getValue() + 400, false);
                scroll(panel, vertical, vertical.getValue() + 200, false);
                vertical.setValueIsAdjusting(false);
                Point2D beforeRedraw = panel.getTimelinedToPanelTransform().transform(new Point2D.Double(), null);
                settle(panel, canvas);
                Point2D afterRedraw = panel.getTimelinedToPanelTransform().transform(new Point2D.Double(), null);
                assertEquals(afterRedraw.distance(beforeRedraw), 0.0, 1e-6, "Redraw must preserve the scrolled viewport");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    @Test(dataProvider = "sceneSizes")
    public void scrollbarsFollowSceneSizeAndSmallScenesRemainCentered(int width, int height, boolean horizontal, boolean vertical) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                SWF swf = ShapeSceneEditTest.scene().getSwf();
                // Nonzero origins must be included when centering the scene.
                swf.displayRect = new RECT(-2000, width * 20 - 2000, 1000, height * 20 + 1000);
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setSelectionMode(true);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                Component canvas = (Component) field(panel, "iconPanel");
                Zoom large = new Zoom();
                large.value = 3;
                panel.zoom(large);
                settle(panel, canvas);
                Zoom normal = new Zoom();
                normal.value = 1;
                panel.zoom(normal);
                panel.setViewPosition(new Point2D.Double(-213.5, -147.25));
                settle(panel, canvas);
                assertEquals(((JScrollBar) field(panel, "horizontalScrollBar")).isVisible(), horizontal);
                assertEquals(((JScrollBar) field(panel, "verticalScrollBar")).isVisible(), vertical);
                Rectangle2D bounds = sceneBounds(panel);
                if (!horizontal) {
                    assertEquals(bounds.getCenterX(), canvas.getWidth() / 2.0, 1e-6);
                }
                if (!vertical) {
                    assertEquals(bounds.getCenterY(), canvas.getHeight() / 2.0, 1e-6);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }
}
