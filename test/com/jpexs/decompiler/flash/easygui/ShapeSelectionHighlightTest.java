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
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeSelectionHighlightTest {

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void mouse(Component canvas, int id, int x, int y) throws Exception {
        MouseEvent event = new MouseEvent(canvas, id, 0, id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.BUTTON1_DOWN_MASK : 0,
                x, y, 1, false, id == MouseEvent.MOUSE_DRAGGED ? MouseEvent.NOBUTTON : MouseEvent.BUTTON1);
        if (id == MouseEvent.MOUSE_DRAGGED) {
            for (Object listener : (List<?>) field(canvas, "mouseMotionListeners")) {
                ((MouseMotionListener) listener).mouseDragged(event);
            }
        } else {
            for (Object listener : (List<?>) field(canvas, "mouseListeners")) {
                if (id == MouseEvent.MOUSE_PRESSED) {
                    ((MouseListener) listener).mousePressed(event);
                } else {
                    ((MouseListener) listener).mouseReleased(event);
                }
            }
        }
    }

    private static BufferedImage paintCanvas(Component canvas) {
        BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, 80, 80);
            canvas.paint(g);
        } finally {
            g.dispose();
        }
        return image;
    }

    @Test
    public void partiallySelectedCircleDoesNotStallWhileCombiningFillAndStrokeFragments() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SWF swf;
        try (FileInputStream input = new FileInputStream("libsrc/ffdec_lib/testdata/graphics/graphics.swf")) {
            swf = new SWF(input, false);
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                ShapeSelection model = new ShapeSelection((ShapeTag) swf.getCharacter(15));
                LINESTYLE2 ink = new LINESTYLE2();
                ink.width = 100;
                ink.color = new RGBA(Color.ORANGE);
                model = model.outlineRegion(model.fillRegionAt(new Point2D.Double(2500, 2000)), ink).model;
                DepthState placed = swf.getTimeline().getFrame(5).layers.get(4);
                Timelined sprite = (Timelined) placed.getCharacter();
                Matrix placement = new Matrix(placed.matrix).concatenate(new Matrix(sprite.getTimeline().getFrame(0).layers.get(1).matrix));
                AffineTransform transform = AffineTransform.getScaleInstance(.05, .05);
                transform.concatenate(placement.toTransform());
                Field swfField = ImagePanel.class.getDeclaredField("swf");
                swfField.setAccessible(true);
                swfField.set(panel, swf);
                Method paint = ImagePanel.class.getDeclaredMethod("paintShapeSelection", Graphics2D.class, AffineTransform.class);
                paint.setAccessible(true);
                for (Rectangle2D box : new Rectangle2D[]{new Rectangle2D.Double(286, 158, 121, 121), new Rectangle2D.Double(274, 195, 112, 86)}) {
                    ShapeSelection.RectangleSelection result = model.selectArea(transform.createInverse().createTransformedShape(box), Collections.emptyList(), false);
                    model = result.model;
                    List<Shape> fills = new ArrayList<>();
                    List<ImagePanel.LineHilight> lines = new ArrayList<>();
                    for (ShapeSelection.Selection item : result.selected) {
                        item = model.displaySelection(item);
                        if (item.stroke) {
                            lines.add(new ImagePanel.LineHilight(item.area, item.linePath, item.line));
                        } else {
                            fills.add(item.area);
                        }
                    }
                    assertTrue(!fills.isEmpty() && lines.size() > 1, "The selection must combine a clipped fill and adjacent curved stroke fragments");
                    panel.setHilightedShapes(fills, lines);
                    BufferedImage image = new BufferedImage(2100, 1400, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = image.createGraphics();
                    try {
                        g.scale(2, 2);
                        g.setClip(0, 0, 1050, 700);
                        long start = System.nanoTime();
                        paint.invoke(panel, g, transform);
                        assertTrue(System.nanoTime() - start < 1_000_000_000L,
                                "Preparing the first selection frame must not stall the Swing thread for seconds");
                    } finally {
                        g.dispose();
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    @Test
    public void completingShapeSelectionDoesNotQueueFullSceneRenders() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            AtomicInteger renders = new AtomicInteger();
            List<TimerTask> scheduled = new ArrayList<>();
            Timer recordingTimer = new Timer("selection-render-test", true) {
                @Override
                public void schedule(TimerTask task, long delay) {
                    renders.incrementAndGet();
                    scheduled.add(task);
                }
            };
            try {
                SWF swf = ShapeSceneEditTest.scene().getSwf();
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                AtomicInteger selections = new AtomicInteger();
                panel.setRectangleSelectionListener((area, additive) -> {
                    selections.incrementAndGet();
                    panel.setHilightedShapes(Collections.singletonList(area), Collections.emptyList());
                });
                Field timerField = ImagePanel.class.getDeclaredField("timer");
                timerField.setAccessible(true);
                Timer oldTimer = (Timer) timerField.get(panel);
                if (oldTimer != null) {
                    oldTimer.cancel();
                }
                timerField.set(panel, recordingTimer);
                for (String name : new String[]{"fpsShouldBe", "fpsIs"}) {
                    Field fps = ImagePanel.class.getDeclaredField(name);
                    fps.setAccessible(true);
                    fps.setFloat(panel, 24);
                }
                Method redraw = ImagePanel.class.getDeclaredMethod("redraw");
                redraw.setAccessible(true);
                redraw.invoke(panel);
                assertTrue(renders.get() > 0, "The render recorder must observe normal scene refreshes");
                renders.set(0);
                Component canvas = (Component) field(panel, "iconPanel");
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 10, 15);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, 60, 55);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 60, 55);
                assertEquals(selections.get(), 1);
                assertEquals(renders.get(), 0, "Completing a shape selection only changes the overlay, not the scene");
                AtomicInteger frameRefreshes = new AtomicInteger();
                panel.addEventListener(new MediaDisplayListener() {
                    @Override
                    public void mediaDisplayStateChanged(MediaDisplay source) {
                        frameRefreshes.incrementAndGet();
                    }

                    @Override
                    public void playingFinished(MediaDisplay source) {
                    }

                    @Override
                    public void statusChanged(String status) {
                    }
                });
                Method schedule = ImagePanel.class.getDeclaredMethod("scheduleTask", boolean.class, long.class, boolean.class);
                schedule.setAccessible(true);
                schedule.invoke(panel, true, 0L, true);
                scheduled.get(scheduled.size() - 1).run();
                assertTrue(frameRefreshes.get() > 0, "The initial scene must still render");
                int initialRefreshes = frameRefreshes.get();
                scheduled.get(scheduled.size() - 1).run();
                assertEquals(frameRefreshes.get(), initialRefreshes, "A stopped shape scene must not render again on each timer tick");
                int beforeResize = renders.get();
                for (ComponentListener listener : canvas.getComponentListeners()) {
                    listener.componentResized(new ComponentEvent(canvas, ComponentEvent.COMPONENT_RESIZED));
                }
                assertTrue(renders.get() > beforeResize, "Resizing the viewport must refresh the centered scene even while the timer is idle");
                panel.setRectangleSelectionListener(null);
                schedule.invoke(panel, true, 0L, false);
                scheduled.get(scheduled.size() - 1).run();
                assertTrue(frameRefreshes.get() > initialRefreshes, "Other preview modes must keep their periodic refresh");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                recordingTimer.cancel();
            }
        });
    }

    @Test
    public void selectionRectangleTracksDraggingWithoutWaitingForASceneRender() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                AtomicInteger selections = new AtomicInteger();
                panel.setRectangleSelectionListener((area, additive) -> selections.incrementAndGet());
                Component canvas = (Component) field(panel, "iconPanel");
                canvas.setSize(80, 80);
                BufferedImage before = paintCanvas(canvas);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, 10, 15);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, 60, 55);
                BufferedImage dragged = paintCanvas(canvas);
                int changed = 0;
                for (int y = 0; y < 80; y++) {
                    for (int x = 0; x < 80; x++) {
                        if (dragged.getRGB(x, y) != before.getRGB(x, y)) {
                            changed++;
                        }
                    }
                }
                assertTrue(changed > 0, "The marquee must be painted from the current pointer without a rendered scene image");
                assertEquals(selections.get(), 0);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, 60, 55);
                assertEquals(selections.get(), 1);
                BufferedImage released = paintCanvas(canvas);
                assertEquals(released.getRGB(0, 0, 80, 80, null, 0, 80), before.getRGB(0, 0, 80, 80, null, 0, 80),
                        "The marquee must disappear immediately on release");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    private static BufferedImage render(ImagePanel panel, AffineTransform transform, Color color) throws Exception {
        return render(panel, transform, color, null);
    }

    private static BufferedImage render(ImagePanel panel, AffineTransform transform, Color color, Consumer<Graphics2D> scene) throws Exception {
        BufferedImage image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setClip(0, 0, image.getWidth(), image.getHeight());
            g.setColor(color);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            if (scene != null) {
                scene.accept(g);
            }
            Method paint = ImagePanel.class.getDeclaredMethod("paintShapeSelection", Graphics2D.class, AffineTransform.class);
            paint.setAccessible(true);
            paint.invoke(panel, g, transform);
        } finally {
            g.dispose();
        }
        return image;
    }

    @Test
    public void selectionInvertsOnlyItsRasterDotsAndKeepsTheOriginalColorBetweenThem() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                Rectangle2D selection = new Rectangle2D.Double(4, 4, 16, 16);
                panel.setHilightedShapes(Collections.singletonList(selection), Collections.emptyList());
                for (Color color : new Color[]{Color.BLACK, Color.WHITE, Color.RED, new Color(51, 255, 0)}) {
                    BufferedImage image = render(panel, new AffineTransform(), color);
                    int inverted = color.getRGB() ^ 0x00ffffff;
                    int dotPixels = 0;
                    for (int y = 0; y < image.getHeight(); y++) {
                        for (int x = 0; x < image.getWidth(); x++) {
                            int pixel = image.getRGB(x, y);
                            if (selection.contains(x, y)) {
                                assertTrue(pixel == color.getRGB() || pixel == inverted, "Selection must retain or invert the exact scene color");
                                if (pixel == inverted) {
                                    dotPixels++;
                                }
                            } else {
                                assertEquals(pixel, color.getRGB(), "Highlight must stay inside the selection");
                            }
                        }
                    }
                    assertEquals(dotPixels, 32, "The Flash raster covers one eighth of the selected area");
                }
                // An overlapping selected stroke must not cancel the fill's inversion.
                BufferedImage fillOnly = render(panel, new AffineTransform(), Color.BLACK);
                panel.setHilightedShapes(Collections.singletonList(selection), Collections.singletonList(new ImagePanel.LineHilight(selection, null, 6)));
                BufferedImage withStroke = render(panel, new AffineTransform(), Color.BLACK);
                assertEquals(withStroke.getRGB(0, 0, 48, 48, null, 0, 48), fillOnly.getRGB(0, 0, 48, 48, null, 0, 48));

                // The raster remains aligned to the canvas under rotation and zoom.
                panel.setHilightedShapes(Collections.singletonList(new Rectangle2D.Double(-100, -100, 200, 200)), Collections.emptyList());
                BufferedImage reference = render(panel, new AffineTransform(), Color.RED);
                AffineTransform placed = AffineTransform.getTranslateInstance(20, 15);
                placed.rotate(0.4);
                placed.scale(1.8, 0.7);
                Rectangle2D local = new Rectangle2D.Double(-8, -8, 16, 16);
                panel.setHilightedShapes(Collections.singletonList(local), Collections.emptyList());
                BufferedImage transformed = render(panel, placed, Color.RED);
                Shape interior = placed.createTransformedShape(new Rectangle2D.Double(-6, -6, 12, 12));
                for (int y = 0; y < 48; y++) {
                    for (int x = 0; x < 48; x++) {
                        if (interior.contains(x, y)) {
                            assertEquals(transformed.getRGB(x, y), reference.getRGB(x, y));
                        }
                    }
                }
                // Four pixels is a minimum; wider strokes retain their displayed width.
                panel.setHilightedShapes(Collections.singletonList(new Rectangle2D.Double(0, 0, 48, 48)), Collections.emptyList());
                BufferedImage fullRaster = render(panel, new AffineTransform(), Color.BLACK);
                for (double zoom : new double[]{0.25, 1, 8}) {
                    for (int width : new int[]{1, 16, 400}) {
                        Line2D center = new Line2D.Double(4 / zoom, 16 / zoom, 36 / zoom, 16 / zoom);
                        Shape area = new BasicStroke(width).createStrokedShape(center);
                        panel.setHilightedShapes(Collections.emptyList(), Collections.singletonList(new ImagePanel.LineHilight(area, center, width)));
                        BufferedImage highlighted = render(panel, AffineTransform.getScaleInstance(zoom, zoom), Color.BLACK);
                        double halfWidth = Math.max(4, width * zoom) / 2;
                        for (int py = 0; py < 48; py++) {
                            for (int px = 8; px < 32; px++) {
                                assertEquals(highlighted.getRGB(px, py), py >= 16 - halfWidth && py < 16 + halfWidth
                                        ? fullRaster.getRGB(px, py) : Color.BLACK.getRGB(), "Stroke highlight must retain its width with a four-pixel minimum");
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }

    @Test
    public void widenedStrokesKeepTheirPaintCapsTransparencyAndScaleFlags() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                panel.getZoom().value = 20; // One local unit per screen pixel in this render test.
                Line2D center = new Line2D.Double(4, 16, 36, 16);
                LINESTYLE2 line = new LINESTYLE2();
                line.width = 1;
                line.startCapStyle = line.endCapStyle = LINESTYLE2.NO_CAP;
                line.color = new RGBA(Color.RED);
                Consumer<Graphics2D> source = g -> {
                    g.setColor(line.color.toColor());
                    g.setStroke(new BasicStroke(line.width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    g.draw(center);
                };
                Shape area = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND).createStrokedShape(center);
                for (Color color : new Color[]{Color.RED, new Color(255, 0, 0, 128)}) {
                    line.color = new RGBA(color);
                    panel.setHilightedShapes(Collections.emptyList(), Collections.emptyList());
                    BufferedImage original = render(panel, new AffineTransform(), Color.BLUE, source);
                    panel.setHilightedShapes(Collections.emptyList(), Collections.singletonList(new ImagePanel.LineHilight(area, center, line)));
                    BufferedImage selected = render(panel, new AffineTransform(), Color.BLUE, source);
                    assertEquals(selected.getRGB(9, 14), original.getRGB(9, 16), "The added strip must use the original stroke color and alpha");
                    assertEquals(selected.getRGB(9, 16), original.getRGB(9, 16), "Original translucent ink must not be painted twice");
                    assertEquals(selected.getRGB(3, 15), Color.BLUE.getRGB(), "A butt cap must stay a butt cap when widened");
                    assertEquals(line.width, 1, "Highlighting must not change the SWF style");
                }
                line.color = new RGBA(Color.RED);
                line.startCapStyle = line.endCapStyle = LINESTYLE2.SQUARE_CAP;
                panel.setHilightedShapes(Collections.emptyList(), Collections.singletonList(new ImagePanel.LineHilight(area, center, line)));
                BufferedImage square = render(panel, new AffineTransform(), Color.BLUE, source);
                assertEquals(square.getRGB(2, 15), Color.RED.getRGB(), "The widened stroke must retain its square cap");

                line.hasFillFlag = true;
                line.fillType = new FILLSTYLE();
                line.fillType.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
                line.fillType.gradientMatrix = new MATRIX();
                line.fillType.gradientMatrix.hasScale = true;
                line.fillType.gradientMatrix.scaleX = line.fillType.gradientMatrix.scaleY = 32f / 32768;
                line.fillType.gradientMatrix.translateX = 20;
                line.fillType.gradientMatrix.translateY = 16;
                line.fillType.gradient = new GRADIENT();
                GRADRECORD first = new GRADRECORD();
                first.ratio = 0;
                first.color = new RGBA(Color.RED);
                GRADRECORD last = new GRADRECORD();
                last.ratio = 255;
                last.color = new RGBA(Color.BLUE);
                line.fillType.gradient.gradientRecords = new GRADRECORD[]{first, last};
                panel.setHilightedShapes(Collections.emptyList(), Collections.singletonList(new ImagePanel.LineHilight(area, center, line)));
                BufferedImage gradient = render(panel, new AffineTransform(), Color.WHITE, g -> {
                    g.setPaint(new LinearGradientPaint(4, 16, 36, 16, new float[]{0, 1}, new Color[]{Color.RED, Color.BLUE}));
                    g.setStroke(new BasicStroke(1));
                    g.draw(center);
                });
                assertTrue(new Color(gradient.getRGB(9, 14)).getRed() > new Color(gradient.getRGB(9, 14)).getBlue());
                assertTrue(new Color(gradient.getRGB(31, 14)).getBlue() > new Color(gradient.getRGB(31, 14)).getRed());
                assertEquals(line.fillType.gradientMatrix.translateX, 20);

                // The renderer's scale modes apply before enforcing the minimum width.
                line.hasFillFlag = false;
                line.width = 8;
                line.startCapStyle = line.endCapStyle = LINESTYLE2.NO_CAP;
                panel.getZoom().value = 10;
                AffineTransform placed = AffineTransform.getScaleInstance(1.5, 2);
                Line2D placedCenter = new Line2D.Double(4 / 1.5, 8, 36 / 1.5, 8);
                for (int mode = 0; mode < 4; mode++) {
                    line.noHScaleFlag = (mode & 2) != 0;
                    line.noVScaleFlag = (mode & 1) != 0;
                    panel.setHilightedShapes(Collections.emptyList(), Collections.singletonList(new ImagePanel.LineHilight(area, placedCenter, line)));
                    BufferedImage scaled = render(panel, placed, Color.BLACK);
                    double displayedWidth = mode == 3 ? 4 : mode == 2 ? 16 : mode == 1 ? 12 : 4 * Math.sqrt(12.5);
                    assertEquals(scaled.getRGB(8, 16), Color.WHITE.getRGB());
                    int outside = (int) Math.ceil(16 + displayedWidth / 2) + 1;
                    assertEquals(scaled.getRGB(8, outside), Color.BLACK.getRGB());
                    if (displayedWidth > 8) {
                        assertEquals(scaled.getRGB(8, 20), Color.WHITE.getRGB(), "Wide strokes must not be reduced to four pixels");
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }
}
