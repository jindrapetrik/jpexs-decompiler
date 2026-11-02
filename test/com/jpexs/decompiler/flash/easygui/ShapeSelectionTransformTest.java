/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

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
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.RegistrationPointPosition;
import com.jpexs.decompiler.flash.gui.ShapeSelectionTransform;
import com.jpexs.decompiler.flash.gui.TransformPanel;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeSelectionTransformTest {

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    @Test
    public void partialSelectionTransformsAboveItsDestinationUntilDroppedAndSupportsUndo() throws Exception {
        DefineShape4Tag original = shape();
        rectangle(original, 0, 0, 1000, 1000, 1, 0);
        rectangle(original, 2000, 0, 4000, 1000, 2, 0);
        ShapeSelection model = selection(original);
        original.getSwf().addTag(original);
        byte[] before = original.getData();
        ShapeSelection.RectangleSelection selected = model.selectRectangle(new Rectangle2D.Double(0, 0, 600, 1000), Collections.emptyList(), false);
        AffineTransform matrix = new AffineTransform(2, 0, 0, 1, 2000, 0);
        ShapeSelection.BatchEdit transformed = selected.model.transformFloatingSelection(selected.selected, matrix);
        assertTrue(transformed.changed);
        assertTrue(transformed.model.hasFloatingSelection());
        assertNull(at(transformed.model, 300, 500));
        assertEquals(at(transformed.model, 800, 500).fill.color.toColor(), Color.red);
        assertEquals(at(transformed.model, 2500, 500).fill.color.toColor(), Color.red);
        ShapeSelection.BatchEdit away = transformed.model.moveFloatingSelection(transformed.selected, 5000, 0);
        assertEquals(at(away.model, 2500, 500).fill.color.toColor(), Color.blue);
        ShapeSelection.BatchEdit dropped = transformed.model.mergeFloatingSelection();
        ShapeSelection.BatchEdit afterDrop = dropped.model.moveFloatingSelection(Collections.singletonList(at(dropped.model, 2500, 500)), 5000, 0);
        assertNull(at(afterDrop.model, 2500, 500));
        assertEquals(original.getData(), before);
        UndoManager undo = new UndoManager();
        ShapeSelection[] active = {selected.model};
        undo.doOperation(new ShapeStyleChangeOperation(original, transformed.model, "Transform", (tag, forward) -> {
            active[0] = forward ? transformed.committedModel(tag) : selected.model;
        }), original.getSwf());
        byte[] after = ((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData();
        assertTrue(active[0].hasFloatingSelection());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        assertEquals(at(new ShapeSelection((ShapeTag) restored.getCharacter(original.shapeId)), 2500, 500).fill.color.toColor(), Color.red);
        undo.undo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), before);
        assertFalse(active[0].hasFloatingSelection());
        undo.redo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), after);
        assertTrue(active[0].hasFloatingSelection());
        assertEquals(at(active[0].moveFloatingSelection(transformed.selected, 5000, 0).model, 2500, 500).fill.color.toColor(), Color.blue);
    }

    @Test
    public void transformationsCarryGradientAndBitmapMatricesAndScaleStrokeWidthsWithoutRotatingTheirThickness() {
        for (boolean bitmap : new boolean[]{false, true}) {
            DefineShape4Tag original = shape();
            FILLSTYLE fill = bitmap ? new FILLSTYLE() : ShapeEyedropperTest.gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
            if (bitmap) {
                fill.fillStyleType = FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
                fill.bitmapId = 77;
                fill.bitmapMatrix = new Matrix(20, 20, 0, 0, 100, 200).toMATRIX();
            }
            original.shapes.fillStyles.fillStyles[0] = fill;
            LINESTYLE2 line = original.shapes.lineStyles.lineStyles2[0];
            line.hasFillFlag = true;
            line.fillType = fill;
            rectangle(original, 0, 0, 1000, 1000, 1, 1);
            ShapeSelection model = selection(original);
            ShapeSelection.RectangleSelection selected = model.selectRectangle(new Rectangle2D.Double(-200, -200, 1400, 1400), Collections.emptyList(), false);
            AffineTransform delta = new AffineTransform(0, 2, -2, 0, 4000, 0);
            ShapeSelection.BatchEdit edited = selected.model.transformFloatingSelection(selected.selected, delta);
            FILLSTYLE changed = at(edited.model, 3000, 1000).fill;
            MATRIX expected = new Matrix(delta).concatenate(new Matrix(bitmap ? fill.bitmapMatrix : fill.gradientMatrix)).toMATRIX();
            assertEquals(bitmap ? changed.bitmapMatrix : changed.gradientMatrix, expected);
            assertEquals(changed.fillStyleType, fill.fillStyleType);
            LINESTYLE2 transformedLine = (LINESTYLE2) at(edited.model, 4000, 1000).line;
            assertEquals(transformedLine.width, line.width * 2);
            assertEquals(bitmap ? transformedLine.fillType.bitmapMatrix : transformedLine.fillType.gradientMatrix, expected);
            assertEquals(line.width, 200);
            line.noHScaleFlag = true;
            line.noVScaleFlag = true;
            ShapeSelection fixed = selection(original);
            ShapeSelection.BatchEdit fixedWidth = fixed.transformFloatingSelection(Collections.singletonList(at(fixed, 500, 0)), delta);
            assertEquals(at(fixedWidth.model, 4000, 1000).line.getWidth(), 200);
            assertFalse(model.transformFloatingSelection(selected.selected, new AffineTransform()).changed);
        }
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

    @Test
    public void placedSelectionHandlesTakePrioritySupportCancelAndUseTheExistingTransformPanel() throws Exception {
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
                swf.setFile("shape-selection-transform.swf");
                panel.setSize(800, 600);
                panel.doLayout();
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.openDepth(0, 2);
                panel.openDepth(0, 2);
                AffineTransform placement = panel.getParentMatrix().toTransform();
                panel.setSelectionMode(false);
                panel.setAltSelectionEnabled(false);
                TransformPanel properties = new TransformPanel(panel, false);
                Component canvas = (Component) field(panel, "iconPanel");
                panel.setAreaSelectionListener((area, additive) -> {
                    throw new AssertionError("Transform handles must take priority over the selection rectangle");
                }, false);
                AtomicReference<Shape> geometry = new AtomicReference<>(new Rectangle2D.Double(0, 0, 4000, 2400));
                AtomicInteger commits = new AtomicInteger();
                AtomicReference<Matrix> preview = new AtomicReference<>();
                BiConsumer<Matrix, Boolean> listener = new BiConsumer<Matrix, Boolean>() {
                    @Override
                    public void accept(Matrix delta, Boolean finished) {
                        preview.set(delta);
                        if (finished) {
                            commits.incrementAndGet();
                            ShapeSelectionTransform.State state = panel.getShapeSelectionTransformState().transformed(delta);
                            geometry.set(delta.toTransform().createTransformedShape(geometry.get()));
                            panel.setShapeSelectionTransform(geometry.get(), this);
                            panel.restoreShapeSelectionTransformState(state);
                        }
                    }
                };
                panel.setShapeSelectionTransform(geometry.get(), listener);
                Rectangle2D sceneBounds = placement.createTransformedShape(geometry.get()).getBounds2D();
                AffineTransform sceneToPanel = panel.getTimelinedToPanelTransform();
                sceneToPanel.concatenate(placement.createInverse());
                Point2D scenePivot = new Point2D.Double(sceneBounds.getCenterX(), sceneBounds.getCenterY());
                Point2D pivot = sceneToPanel.transform(scenePivot, null);
                BufferedImage overlay = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = overlay.createGraphics();
                graphics.setColor(Color.RED);
                graphics.fillRect(0, 0, overlay.getWidth(), overlay.getHeight());
                Object handles = field(panel, "shapeSelectionTransform");
                Method paint = handles.getClass().getDeclaredMethod("paint", Graphics2D.class, AffineTransform.class);
                paint.setAccessible(true);
                paint.invoke(handles, graphics, panel.getTimelinedToPanelTransform());
                graphics.dispose();
                Rectangle2D screenBounds = sceneToPanel.createTransformedShape(sceneBounds).getBounds2D();
                int edgeY = (int) screenBounds.getMinY();
                for (double fraction : new double[]{.2, .35, .65, .8}) {
                    int edgeX = (int) (screenBounds.getMinX() + screenBounds.getWidth() * fraction);
                    boolean found = false;
                    for (int y = edgeY - 1; y <= edgeY + 1; y++) {
                        found |= overlay.getRGB(edgeX, y) == Color.BLACK.getRGB();
                    }
                    assertTrue(found, "The top edge must stay horizontal even when the shape placement is rotated or skewed");
                }
                mouse(canvas, MouseEvent.MOUSE_PRESSED, pivot);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, new Point2D.Double(pivot.getX() + 15, pivot.getY() + 10));
                assertNull(panel.getRegistrationPointPosition());
                for (KeyListener key : canvas.getKeyListeners()) {
                    key.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                assertEquals(panel.getRegistrationPointPosition(), RegistrationPointPosition.CENTER);
                assertEquals(panel.getRegistrationPoint().distance(scenePivot), 0.0, 1e-6);
                Point2D start = sceneToPanel.transform(new Point2D.Double(sceneBounds.getMaxX(), sceneBounds.getCenterY()), null);
                Point2D end = new Point2D.Double(start.getX() + screenBounds.getWidth() / 4, start.getY());
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                assertEquals(commits.get(), 0);
                Matrix scenePreview = new Matrix(placement).concatenate(preview.get()).concatenate(new Matrix(placement).inverse());
                assertEquals(scenePreview.scaleX, 1.5, .02);
                assertEquals(scenePreview.scaleY, 1.0, 1e-6);
                assertEquals(scenePreview.rotateSkew0, 0.0, 1e-6);
                assertEquals(scenePreview.rotateSkew1, 0.0, 1e-6);
                for (KeyListener key : canvas.getKeyListeners()) {
                    key.keyPressed(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                }
                assertEquals(preview.get().toTransform(), new AffineTransform());
                assertEquals(commits.get(), 0);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                assertEquals(commits.get(), 1);
                assertEquals(panel.getNewMatrix().scaleX, 1.5, .02);
                assertEquals(panel.getParentMatrix().toTransform(), placement);
                ((JTextField) field(properties, "moveHorizontalTextField")).setText("100");
                ((JTextField) field(properties, "moveVerticalTextField")).setText("50");
                Method move = TransformPanel.class.getDeclaredMethod("applyMoveActionPerformed", java.awt.event.ActionEvent.class);
                move.setAccessible(true);
                move.invoke(properties, new Object[]{null});
                Rectangle2D movedBounds = placement.createTransformedShape(geometry.get()).getBounds2D();
                assertEquals(movedBounds.getMinX(), 2000, 1e-6);
                assertEquals(movedBounds.getMinY(), 1000, 1e-6);
                Matrix absolute = new Matrix(1, 1, .2, .1, 300, 400);
                panel.setFullTransformMatrix(absolute);
                assertEquals(panel.getNewMatrix().scaleX, absolute.scaleX, 1e-6);
                assertEquals(panel.getNewMatrix().rotateSkew0, absolute.rotateSkew0, 1e-6);
                assertEquals(panel.getNewMatrix().translateX, absolute.translateX, 1e-6);
                assertEquals(commits.get(), 3);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
