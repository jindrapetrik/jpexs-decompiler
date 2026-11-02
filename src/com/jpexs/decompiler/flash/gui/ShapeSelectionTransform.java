/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.BiConsumer;

/**
 * Axis-aligned transform handles in scene coordinates. Edits are converted back
 * to shape-local coordinates before they are passed to the listener.
 */
public final class ShapeSelectionTransform {

    static final int ROTATE = 20;
    static final int SHEAR_X = 21;
    static final int SHEAR_Y = 22;
    private static final int[] RESIZE_MODES = {Cursor.NW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
        Cursor.E_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR};
    private static final double[][] HANDLES = {{0, 0}, {.5, 0}, {1, 0}, {1, .5}, {1, 1}, {.5, 1}, {0, 1}, {0, .5}};

    /**
     * Layer matrix and pivot retained across committed operations and Undo/Redo.
     */
    public static final class State {

        private final Matrix matrix;
        private final Point2D pivot;
        private final RegistrationPointPosition position;

        private State(Matrix matrix, Point2D pivot, RegistrationPointPosition position) {
            this.matrix = matrix.clone();
            this.pivot = (Point2D) pivot.clone();
            this.position = position;
        }

        public State transformed(Matrix delta) {
            RegistrationPointPosition nextPosition = delta.rotateSkew0 != 0 || delta.rotateSkew1 != 0
                    || delta.scaleX < 0 || delta.scaleY < 0 ? null : position;
            return new State(delta.concatenate(matrix), delta.transform(pivot), nextPosition);
        }
    }

    private final Shape geometry;
    private final Matrix parentMatrix;
    private final BiConsumer<Matrix, Boolean> listener;
    private State state;
    private Matrix preview = new Matrix();
    private int active = Cursor.DEFAULT_CURSOR;
    private int handleIndex;
    private Point2D start;
    private Rectangle2D startBounds;
    private Point2D startPivot;
    private State startState;

    ShapeSelectionTransform(Shape geometry, Matrix parentMatrix, BiConsumer<Matrix, Boolean> listener) {
        this.geometry = geometry;
        this.parentMatrix = parentMatrix.clone();
        this.listener = listener;
        Rectangle2D bounds = parentMatrix.toTransform().createTransformedShape(geometry).getBounds2D();
        state = new State(new Matrix(), parentMatrix.inverse().transform(new Point2D.Double(bounds.getCenterX(), bounds.getCenterY())),
                RegistrationPointPosition.CENTER);
    }

    State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    Matrix getMatrix() {
        return parentMatrix.concatenate(state.matrix).concatenate(parentMatrix.inverse());
    }

    Rectangle2D getBounds() {
        return parentMatrix.concatenate(preview).toTransform().createTransformedShape(geometry).getBounds2D();
    }

    Point2D getPivot() {
        return parentMatrix.concatenate(preview).transform(state.pivot);
    }

    RegistrationPointPosition getPosition() {
        return state.position;
    }

    void setPivot(Point2D pivot, RegistrationPointPosition position) {
        state = new State(state.matrix, parentMatrix.inverse().transform(pivot), position);
    }

    private AffineTransform sceneToPanel(AffineTransform placement) {
        AffineTransform result = new AffineTransform(placement);
        result.concatenate(parentMatrix.inverse().toTransform());
        return result;
    }

    private static Point2D handle(Rectangle2D bounds, int index) {
        return new Point2D.Double(bounds.getX() + HANDLES[index][0] * bounds.getWidth(),
                bounds.getY() + HANDLES[index][1] * bounds.getHeight());
    }

    int hitTest(Point2D screen, AffineTransform placement) {
        if (screen == null) {
            return Cursor.DEFAULT_CURSOR;
        }
        if (active != Cursor.DEFAULT_CURSOR) {
            return active;
        }
        placement = sceneToPanel(placement);
        Rectangle2D bounds = getBounds();
        for (int i = 0; i < HANDLES.length; i++) {
            if (placement.transform(handle(bounds, i), null).distance(screen) <= 6) {
                handleIndex = i;
                return RESIZE_MODES[i];
            }
        }
        if (placement.transform(getPivot(), null).distance(screen) <= 6) {
            return Cursor.HAND_CURSOR;
        }
        try {
            Point2D local = placement.inverseTransform(screen, null);
            if (!bounds.contains(local)) {
                for (int i = 0; i < HANDLES.length; i += 2) {
                    if (placement.transform(handle(bounds, i), null).distance(screen) <= 18) {
                        return ROTATE;
                    }
                }
            }
            for (int i = 0; i < HANDLES.length; i += 2) {
                Point2D a = placement.transform(handle(bounds, i), null);
                Point2D b = placement.transform(handle(bounds, (i + 2) % HANDLES.length), null);
                if (Line2D.ptSegDist(a.getX(), a.getY(), b.getX(), b.getY(), screen.getX(), screen.getY()) <= 5) {
                    handleIndex = i;
                    return i == 0 || i == 4 ? SHEAR_X : SHEAR_Y;
                }
            }
            return bounds.contains(local) ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR;
        } catch (NoninvertibleTransformException ex) {
            return Cursor.DEFAULT_CURSOR;
        }
    }

    boolean press(Point2D screen, AffineTransform placement) {
        active = hitTest(screen, placement);
        if (active == Cursor.DEFAULT_CURSOR) {
            return false;
        }
        try {
            placement = sceneToPanel(placement);
            start = placement.inverseTransform(screen, null);
            startBounds = getBounds();
            startPivot = getPivot();
            startState = state;
            return true;
        } catch (NoninvertibleTransformException ex) {
            active = Cursor.DEFAULT_CURSOR;
            return false;
        }
    }

    boolean drag(Point2D screen, AffineTransform placement, boolean constrain) {
        if (active == Cursor.DEFAULT_CURSOR) {
            return false;
        }
        Point2D point;
        try {
            placement = sceneToPanel(placement);
            point = placement.inverseTransform(screen, null);
        } catch (NoninvertibleTransformException ex) {
            return true;
        }
        double dx = point.getX() - start.getX();
        double dy = point.getY() - start.getY();
        if (active == Cursor.HAND_CURSOR) {
            setPivot(new Point2D.Double(startPivot.getX() + dx, startPivot.getY() + dy), null);
            return true;
        }
        AffineTransform delta = new AffineTransform();
        if (active == Cursor.MOVE_CURSOR) {
            delta.translate(Math.rint(dx), Math.rint(dy));
        } else if (active == ROTATE) {
            double angle = Math.atan2(point.getY() - startPivot.getY(), point.getX() - startPivot.getX())
                    - Math.atan2(start.getY() - startPivot.getY(), start.getX() - startPivot.getX());
            if (constrain) {
                angle = Math.rint(angle / (Math.PI / 12)) * Math.PI / 12;
            }
            delta.rotate(angle, startPivot.getX(), startPivot.getY());
        } else if (active == SHEAR_X || active == SHEAR_Y) {
            Point2D opposite = handle(startBounds, (handleIndex + 4) % HANDLES.length);
            delta.translate(opposite.getX(), opposite.getY());
            delta.shear(active == SHEAR_X && startBounds.getHeight() != 0 ? dx / (startBounds.getHeight() * (handleIndex == 0 ? -1 : 1)) : 0,
                    active == SHEAR_Y && startBounds.getWidth() != 0 ? dy / (startBounds.getWidth() * (handleIndex == 6 ? -1 : 1)) : 0);
            delta.translate(-opposite.getX(), -opposite.getY());
        } else {
            Point2D anchor = (Point2D) startPivot.clone();
            Point2D handle = handle(startBounds, handleIndex);
            Point2D opposite = handle(startBounds, (handleIndex + 4) % HANDLES.length);
            if (Math.abs(handle.getX() - anchor.getX()) < 1e-9) {
                anchor.setLocation(opposite.getX(), anchor.getY());
            }
            if (Math.abs(handle.getY() - anchor.getY()) < 1e-9) {
                anchor.setLocation(anchor.getX(), opposite.getY());
            }
            double sx = HANDLES[handleIndex][0] == .5 || handle.getX() == anchor.getX() ? 1 : 1 + dx / (handle.getX() - anchor.getX());
            double sy = HANDLES[handleIndex][1] == .5 || handle.getY() == anchor.getY() ? 1 : 1 + dy / (handle.getY() - anchor.getY());
            if (constrain) {
                double scale = Math.abs(sx - 1) > Math.abs(sy - 1) ? sx : sy;
                sx = scale;
                sy = scale;
            }
            delta.translate(anchor.getX(), anchor.getY());
            delta.scale(sx, sy);
            delta.translate(-anchor.getX(), -anchor.getY());
        }
        preview = parentMatrix.inverse().concatenate(new Matrix(delta)).concatenate(parentMatrix);
        listener.accept(preview.clone(), false);
        return true;
    }

    boolean release() {
        if (active == Cursor.DEFAULT_CURSOR) {
            return false;
        }
        boolean pivotOnly = active == Cursor.HAND_CURSOR;
        active = Cursor.DEFAULT_CURSOR;
        if (!pivotOnly) {
            listener.accept(preview.clone(), true);
        }
        preview = new Matrix();
        return true;
    }

    boolean cancel() {
        if (active == Cursor.DEFAULT_CURSOR) {
            return false;
        }
        if (active == Cursor.HAND_CURSOR) {
            state = startState;
        }
        active = Cursor.DEFAULT_CURSOR;
        preview = new Matrix();
        listener.accept(new Matrix(), false);
        return true;
    }

    void apply(Matrix matrix) {
        listener.accept(parentMatrix.inverse().concatenate(matrix).concatenate(parentMatrix), true);
    }

    void paint(Graphics2D graphics, AffineTransform placement) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            placement = sceneToPanel(placement);
            Rectangle2D bounds = getBounds();
            Shape box = placement.createTransformedShape(bounds);
            g.setColor(Color.white);
            g.setStroke(new BasicStroke(3));
            g.draw(box);
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(1));
            g.draw(box);
            for (int i = 0; i < HANDLES.length; i++) {
                Point2D point = placement.transform(handle(bounds, i), null);
                ImagePanel.drawHandle(g, point.getX(), point.getY());
            }
            ImagePanel.drawRegistrationPoint(g, placement.transform(getPivot(), null));
        } finally {
            g.dispose();
        }
    }
}
