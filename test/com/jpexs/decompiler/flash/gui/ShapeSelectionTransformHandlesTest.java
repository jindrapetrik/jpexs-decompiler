/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import org.testng.annotations.Test;

public class ShapeSelectionTransformHandlesTest {

    private static class Selection implements BiConsumer<Matrix, Boolean> {

        final Matrix parent;
        final AffineTransform sceneToPanel = new AffineTransform(.07, 0, 0, .07, 500, 250);
        final AffineTransform localToPanel;
        Shape geometry = new Rectangle2D.Double(0, 0, 4000, 2400);
        ShapeSelectionTransform handles;
        int commits;

        Selection(Matrix parent) {
            this.parent = parent;
            localToPanel = new AffineTransform(sceneToPanel);
            localToPanel.concatenate(parent.toTransform());
            handles = new ShapeSelectionTransform(geometry, parent, this);
        }

        @Override
        public void accept(Matrix delta, Boolean finished) {
            if (finished) {
                commits++;
                ShapeSelectionTransform.State next = handles.getState().transformed(delta);
                geometry = delta.toTransform().createTransformedShape(geometry);
                handles = new ShapeSelectionTransform(geometry, parent, this);
                handles.setState(next);
            }
        }

        AffineTransform initialFrame() {
            Rectangle2D bounds = parent.toTransform().createTransformedShape(geometry).getBounds2D();
            AffineTransform frame = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
            frame.scale(bounds.getWidth(), bounds.getHeight());
            return frame;
        }

        Point2D point(AffineTransform frame, double x, double y) {
            return sceneToPanel.transform(frame.transform(new Point2D.Double(x, y), null), null);
        }

        void drag(Point2D start, Point2D end, int mode) {
            assertEquals(handles.hitTest(start, localToPanel), mode);
            assertTrue(handles.press(start, localToPanel));
            assertTrue(handles.drag(end, localToPanel, false));
            assertTrue(handles.release());
        }

        void assertFrame(AffineTransform expected) {
            // Both hit testing and rendering must use the carried frame,
            // including along skewed edges rather than its axis-aligned bounds.
            assertEquals(handles.hitTest(point(expected, 1, .5), localToPanel), Cursor.E_RESIZE_CURSOR);
            assertEquals(handles.hitTest(point(expected, 1, 1), localToPanel), Cursor.SE_RESIZE_CURSOR);
            assertEquals(handles.hitTest(point(expected, .25, 0), localToPanel), ShapeSelectionTransform.SHEAR_X);
            assertEquals(handles.hitTest(point(expected, 1, .25), localToPanel), ShapeSelectionTransform.SHEAR_Y);
            Rectangle2D screenBounds = sceneToPanel.createTransformedShape(expected.createTransformedShape(new Rectangle2D.Double(0, 0, 1, 1))).getBounds2D();
            double offsetX = 10 - screenBounds.getMinX();
            double offsetY = 10 - screenBounds.getMinY();
            BufferedImage image = new BufferedImage((int) Math.ceil(screenBounds.getWidth()) + 20,
                    (int) Math.ceil(screenBounds.getHeight()) + 20, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.translate(offsetX, offsetY);
            handles.paint(g, localToPanel);
            g.dispose();
            for (double fraction : new double[]{.2, .35, .65, .8}) {
                Point2D edge = point(expected, fraction, 0);
                edge.setLocation(edge.getX() + offsetX, edge.getY() + offsetY);
                boolean black = false;
                for (int y = (int) edge.getY() - 1; y <= (int) edge.getY() + 1; y++) {
                    for (int x = (int) edge.getX() - 1; x <= (int) edge.getX() + 1; x++) {
                        black |= image.getRGB(x, y) == Color.BLACK.getRGB();
                    }
                }
                assertTrue(black, "The frame must follow the rotated/skewed top edge");
            }
        }

        AffineTransform rotate(AffineTransform frame, double angle) {
            Point2D pivot = point(frame, .5, .5);
            Point2D corner = point(frame, 1, 0);
            double distance = pivot.distance(corner);
            Point2D start = new Point2D.Double(corner.getX() + 12 * (corner.getX() - pivot.getX()) / distance,
                    corner.getY() + 12 * (corner.getY() - pivot.getY()) / distance);
            Point2D end = AffineTransform.getRotateInstance(angle, pivot.getX(), pivot.getY()).transform(start, null);
            drag(start, end, ShapeSelectionTransform.ROTATE);
            Point2D scenePivot = frame.transform(new Point2D.Double(.5, .5), null);
            AffineTransform result = AffineTransform.getRotateInstance(angle, scenePivot.getX(), scenePivot.getY());
            result.concatenate(frame);
            assertFrame(result);
            return result;
        }
    }

    @Test
    public void rotationSkewAndScalingKeepTheirFrameAxesAcrossCommitsAndStateRestoration() {
        for (boolean reflected : new boolean[]{false, true}) {
            AffineTransform placement = AffineTransform.getRotateInstance(Math.PI / 4);
            placement.shear(.2, -.1);
            placement.scale(reflected ? -1.1 : 1.1, .8);
            Selection selection = new Selection(new Matrix(placement));
            AffineTransform originalFrame = selection.initialFrame();
            selection.assertFrame(originalFrame);
            AffineTransform frame = selection.rotate(originalFrame, Math.PI / 5);

            selection.drag(selection.point(frame, .25, 0), selection.point(frame, .45, 0), ShapeSelectionTransform.SHEAR_X);
            AffineTransform shear = new AffineTransform(1, 0, -.2, 1, .2, 0);
            frame.concatenate(shear);
            selection.assertFrame(frame);

            selection.drag(selection.point(frame, 1, .5), selection.point(frame, 1.25, .5), Cursor.E_RESIZE_CURSOR);
            frame.concatenate(new AffineTransform(1.5, 0, 0, 1, -.25, 0));
            selection.assertFrame(frame);

            selection.drag(selection.point(frame, 1, 1), selection.point(frame, 1.125, 1.25), Cursor.SE_RESIZE_CURSOR);
            frame.concatenate(new AffineTransform(1.25, 0, 0, 1.5, -.125, -.25));
            selection.assertFrame(frame);

            selection.drag(selection.point(frame, 1, .25), selection.point(frame, 1, .4), ShapeSelectionTransform.SHEAR_Y);
            frame.concatenate(new AffineTransform(1, .15, 0, 1, 0, 0));
            selection.assertFrame(frame);
            frame = selection.rotate(frame, -Math.PI / 7);

            selection.handles.setPivotPosition(RegistrationPointPosition.TOP_RIGHT);
            Point2D expectedPivot = frame.transform(new Point2D.Double(1, 0), null);
            assertEquals(selection.handles.getPivot().distance(expectedPivot), 0.0, 1e-7);
            selection.handles.setPivotPosition(RegistrationPointPosition.CENTER);
            ShapeSelectionTransform.State before = selection.handles.getState();
            AffineTransform beforeFrame = new AffineTransform(frame);
            selection.drag(selection.point(frame, 1, .5), selection.point(frame, .25, .5), Cursor.E_RESIZE_CURSOR);
            frame.concatenate(new AffineTransform(-.5, 0, 0, 1, .75, 0));
            selection.assertFrame(frame);
            ShapeSelectionTransform.State after = selection.handles.getState();
            selection.handles.setState(before);
            selection.assertFrame(beforeFrame);
            selection.handles.setState(after);
            selection.assertFrame(frame);
            selection.rotate(frame, Math.PI / 8);
            assertEquals(selection.commits, 8);

            // A different selection starts aligned to the scene again.
            selection.handles = new ShapeSelectionTransform(selection.geometry, selection.parent, selection);
            selection.assertFrame(selection.initialFrame());
        }
    }
}
