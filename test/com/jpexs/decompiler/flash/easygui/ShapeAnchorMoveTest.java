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
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.DisplayPoint;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeAnchorMoveTest {

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private static FILLSTYLE bitmap() {
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
        fill.bitmapId = 17;
        fill.bitmapMatrix = new Matrix(20, 30, 2, 3, 500, 600).toMATRIX();
        return fill;
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static void assertTranslated(ShapeTag original, ShapeTag moved, int dx, int dy) throws Exception {
        List<DisplayPoint> before = new ShapePointEdit(original).getPoints();
        List<DisplayPoint> after = new ShapePointEdit(moved).getPoints();
        assertEquals(after.size(), before.size());
        for (int i = 0; i < before.size(); i++) {
            assertEquals(after.get(i).x, before.get(i).x + dx);
            assertEquals(after.get(i).y, before.get(i).y + dy);
            assertEquals(after.get(i).onPath, before.get(i).onPath);
        }
    }

    @Test
    public void movesAllContoursAndPaintMatricesWithOneUndoWithoutChangingTheOriginal() throws Exception {
        DefineShape4Tag original = shape();
        original.shapes.fillStyles.fillStyles = new FILLSTYLE[]{gradient(FILLSTYLE.LINEAR_GRADIENT), bitmap()};
        LINESTYLE2 line = original.shapes.lineStyles.lineStyles2[0];
        line.hasFillFlag = true;
        line.fillType = gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
        line.noHScaleFlag = true;
        line.joinStyle = LINESTYLE2.MITER_JOIN;
        line.miterLimitFactor = 7;
        rectangle(original, 0, 0, 1000, 1000, 1, 1);
        rectangle(original, 3000, 2000, 1000, 1000, 2, 1);
        selection(original);
        original.getSwf().addTag(original);
        original.setModified(false);
        original.getSwf().setModified(false);
        byte[] before = original.getData();
        ShapeAnchorMove gesture = new ShapeAnchorMove(original, point(100, 200));
        assertNull(gesture.moveTo(point(100, 200)));
        ShapeTag preview = gesture.moveTo(point(300, 100));
        ShapeTag moved = gesture.moveTo(point(800, -300));
        assertTranslated(original, preview, 200, -100);
        assertTranslated(original, moved, 700, -500);
        assertEquals(moved.shapes.fillStyles.fillStyles[0].gradientMatrix.translateX,
                original.shapes.fillStyles.fillStyles[0].gradientMatrix.translateX + 700);
        assertEquals(moved.shapes.fillStyles.fillStyles[1].bitmapMatrix.translateY, 100);
        LINESTYLE2 expected = ((ShapeTag) original.cloneTag()).shapes.lineStyles.lineStyles2[0];
        expected.fillType.gradientMatrix.translateX += 700;
        expected.fillType.gradientMatrix.translateY -= 500;
        assertEquals(moved.shapes.lineStyles.lineStyles2[0], expected);
        assertEquals(original.getData(), before);
        assertFalse(original.getSwf().isModified());
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, moved, "Move", null), original.getSwf());
        byte[] after = current(original).getData();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        assertTranslated(original, (ShapeTag) restored.getCharacter(original.shapeId), 700, -500);
        undo.undo(original.getSwf());
        assertEquals(current(original).getData(), before);
        assertFalse(undo.canUndo(original.getSwf()));
        assertFalse(original.getSwf().isModified());
        undo.redo(original.getSwf());
        assertEquals(current(original).getData(), after);
        assertNull(gesture.moveTo(point(100, 200)));
    }

    @Test
    public void embeddedStyleArraysMoveWithTheGeometry() throws Exception {
        DefineShape4Tag original = shape();
        rectangle(original, 100, 200, 1000, 1000, 1, 1);
        StyleChangeRecord embedded = new StyleChangeRecord();
        embedded.stateNewStyles = true;
        embedded.fillStyles = Helper.deepCopy(original.shapes.fillStyles);
        embedded.fillStyles.fillStyles = new FILLSTYLE[]{bitmap()};
        embedded.lineStyles = Helper.deepCopy(original.shapes.lineStyles);
        embedded.lineStyles.lineStyles2[0].hasFillFlag = true;
        embedded.lineStyles.lineStyles2[0].fillType = gradient(FILLSTYLE.RADIAL_GRADIENT);
        original.shapes.shapeRecords.add(embedded);
        rectangle(original, 3000, 2000, 1000, 1000, 1, 1);
        selection(original);
        ShapeTag moved = new ShapeAnchorMove(original, point(0, 0)).moveTo(point(-250, 400));
        assertTranslated(original, moved, -250, 400);
        StyleChangeRecord actual = (StyleChangeRecord) moved.shapes.shapeRecords.stream()
                .filter(record -> record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateNewStyles).findFirst().get();
        assertEquals(actual.fillStyles.fillStyles[0].bitmapMatrix.translateX, 250);
        assertEquals(actual.lineStyles.lineStyles2[0].fillType.gradientMatrix.translateY,
                embedded.lineStyles.lineStyles2[0].fillType.gradientMatrix.translateY + 400);
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
    public void sceneDraggingKeepsTheOriginAndPlacementFixedAndSupportsCancel() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                ShapeTag original = ShapeSceneEditTest.scene();
                SWF swf = original.getSwf();
                swf.setFile("anchor-point-events.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                panel.setSelectionMode(false);
                panel.setShapeOriginVisible(true);
                ImagePanel.ShapeEditContext context = panel.getShapeEditContext();
                Matrix placement = panel.getParentMatrix();
                Point2D origin = panel.getTimelinedToPanelTransform().transform(point(0, 0), null);
                AtomicReference<ShapeAnchorMove> gesture = new AtomicReference<>();
                UndoManager undo = new UndoManager();
                panel.setShapeDrawingTool(null, new ImagePanel.ShapeDrawListener() {
                    @Override
                    public void begin(Point2D point) {
                        try {
                            gesture.set(new ShapeAnchorMove(current(original), point));
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void update(Point2D point, boolean finished) {
                        try {
                            ShapeTag moved = gesture.get().moveTo(point);
                            panel.setShapePreview(finished ? null : moved);
                            if (finished && moved != null) {
                                undo.doOperation(new ShapeStyleChangeOperation(current(original), moved, "Move",
                                        (tag, forward) -> panel.restoreShapeEditContext(tag, context)), swf);
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    @Override
                    public void cancel() {
                        panel.setShapePreview(null);
                    }
                }, ImagePanel.ShapeDrawingPointer.MOVE);
                Component canvas = (Component) field(panel, "iconPanel");
                Point2D start = panel.getTimelinedToPanelTransform().transform(point(1000, 700), null);
                start = new Point((int) Math.round(start.getX()), (int) Math.round(start.getY()));
                Point2D end = point(start.getX() + 40, start.getY() - 25);
                Point2D from = panel.toTimelinedPoint(start);
                Point2D to = panel.toTimelinedPoint(end);
                byte[] before = current(original).getData();
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                assertFalse(undo.canUndo(swf));
                assertEquals(current(original).getData(), before);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                assertTranslated(original, current(original), (int) Math.rint(to.getX() - from.getX()), (int) Math.rint(to.getY() - from.getY()));
                assertEquals(panel.getParentMatrix().toTransform(), placement.toTransform());
                assertEquals(panel.getTimelinedToPanelTransform().transform(point(0, 0), null), origin);
                undo.undo(swf);
                assertEquals(current(original).getData(), before);
                assertFalse(undo.canUndo(swf));
                undo.redo(swf);
                byte[] moved = current(original).getData();
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, point(end.getX() + 80, end.getY()));
                for (KeyListener listener : canvas.getKeyListeners()) {
                    listener.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                assertEquals(current(original).getData(), moved);
                assertNull(field(panel, "shapePreview"));
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                canvas.paint(graphics);
                graphics.dispose();
                int x = (int) Math.round(origin.getX());
                int y = (int) Math.round(origin.getY());
                assertEquals(new Color(image.getRGB(x, y), true), Color.red);
                assertEquals(new Color(image.getRGB(x + 9, y), true), Color.black);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.setShapeDrawingTool(null, null);
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
