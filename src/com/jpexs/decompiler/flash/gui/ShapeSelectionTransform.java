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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.BiConsumer;

/**
 * A transform frame initially aligned with the scene, then carried along with
 * the selected geometry. Edits are passed to the listener in shape-local twips.
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
        private final Matrix frame;
        private final Point2D pivot;
        private final RegistrationPointPosition position;

        private State(Matrix matrix, Matrix frame, Point2D pivot, RegistrationPointPosition position) {
            this.matrix = matrix.clone();
            this.frame = frame.clone();
            this.pivot = (Point2D) pivot.clone();
            this.position = position;
        }

        public State transformed(Matrix delta) {
            return new State(delta.concatenate(matrix), delta.concatenate(frame), delta.transform(pivot), position);
        }
    }

    private final Matrix parentMatrix;
    private final BiConsumer<Matrix, Boolean> listener;
    private State state;
    private Matrix preview = new Matrix();
    private int active = Cursor.DEFAULT_CURSOR;
    private int handleIndex;
    private Point2D start;
    private Point2D startPivot;
    private Point2D startScene;
    private Point2D startScenePivot;
    private State startState;

    ShapeSelectionTransform(Shape geometry, Matrix parentMatrix, BiConsumer<Matrix, Boolean> listener) {
        this.parentMatrix = parentMatrix.clone();
        this.listener = listener;
        Rectangle2D bounds = parentMatrix.toTransform().createTransformedShape(geometry).getBounds2D();
        AffineTransform frame = AffineTransform.getTranslateInstance(bounds.getX(), bounds.getY());
        frame.scale(Math.max(1, bounds.getWidth()), Math.max(1, bounds.getHeight()));
        Matrix localFrame = parentMatrix.inverse().concatenate(new Matrix(frame));
        state = new State(new Matrix(), localFrame, localFrame.transform(new Point2D.Double(.5, .5)),
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
        return parentMatrix.concatenate(preview).concatenate(state.frame).toTransform()
                .createTransformedShape(new Rectangle2D.Double(0, 0, 1, 1)).getBounds2D();
    }

    Point2D getPivot() {
        return parentMatrix.concatenate(preview).transform(state.pivot);
    }

    RegistrationPointPosition getPosition() {
        return state.position;
    }

    void setPivot(Point2D pivot, RegistrationPointPosition position) {
        state = new State(state.matrix, state.frame, parentMatrix.inverse().transform(pivot), position);
    }

    void setPivotPosition(RegistrationPointPosition position) {
        state = new State(state.matrix, state.frame,
                state.frame.transform(new Point2D.Double(position.getPositionX(), position.getPositionY())), position);
    }

    private AffineTransform sceneToPanel(AffineTransform placement) {
        AffineTransform result = new AffineTransform(placement);
        result.concatenate(parentMatrix.inverse().toTransform());
        return result;
    }

    private static Point2D handle(int index) {
        return new Point2D.Double(HANDLES[index][0], HANDLES[index][1]);
    }

    private AffineTransform frameToPanel(AffineTransform placement) {
        AffineTransform result = new AffineTransform(placement);
        result.concatenate(preview.toTransform());
        result.concatenate(state.frame.toTransform());
        return result;
    }

    private static Path2D frameOutline(AffineTransform frame) {
        Path2D result = new Path2D.Double();
        for (int i = 0; i < HANDLES.length; i += 2) {
            Point2D point = frame.transform(handle(i), null);
            if (i == 0) {
                result.moveTo(point.getX(), point.getY());
            } else {
                result.lineTo(point.getX(), point.getY());
            }
        }
        result.closePath();
        return result;
    }

    int hitTest(Point2D screen, AffineTransform placement) {
        if (screen == null) {
            return Cursor.DEFAULT_CURSOR;
        }
        if (active != Cursor.DEFAULT_CURSOR) {
            return active;
        }
        AffineTransform frame = frameToPanel(placement);
        Path2D outline = frameOutline(frame);
        for (int i = 0; i < HANDLES.length; i++) {
            if (frame.transform(handle(i), null).distance(screen) <= 6) {
                handleIndex = i;
                return RESIZE_MODES[i];
            }
        }
        if (placement.transform(preview.transform(state.pivot), null).distance(screen) <= 6) {
            return Cursor.HAND_CURSOR;
        }
        if (!outline.contains(screen)) {
            for (int i = 0; i < HANDLES.length; i += 2) {
                if (frame.transform(handle(i), null).distance(screen) <= 18) {
                    return ROTATE;
                }
            }
        }
        for (int i = 0; i < HANDLES.length; i += 2) {
            Point2D a = frame.transform(handle(i), null);
            Point2D b = frame.transform(handle((i + 2) % HANDLES.length), null);
            if (Line2D.ptSegDist(a.getX(), a.getY(), b.getX(), b.getY(), screen.getX(), screen.getY()) <= 5) {
                handleIndex = i;
                return i == 0 || i == 4 ? SHEAR_X : SHEAR_Y;
            }
        }
        return outline.contains(screen) ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR;
    }

    int cursorMode(Point2D screen, AffineTransform placement) {
        int mode = hitTest(screen, placement);
        AffineTransform frame = frameToPanel(placement);
        if (mode >= Cursor.SW_RESIZE_CURSOR && mode <= Cursor.E_RESIZE_CURSOR) {
            Point2D center = frame.transform(new Point2D.Double(.5, .5), null);
            Point2D point = frame.transform(handle(handleIndex), null);
            int direction = Math.floorMod((int) Math.round(Math.atan2(point.getY() - center.getY(), point.getX() - center.getX()) / (Math.PI / 4)), 4);
            return new int[]{Cursor.E_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR}[direction];
        }
        if (mode == SHEAR_X || mode == SHEAR_Y) {
            Point2D a = frame.transform(handle(handleIndex), null);
            Point2D b = frame.transform(handle((handleIndex + 2) % HANDLES.length), null);
            return Math.abs(b.getX() - a.getX()) >= Math.abs(b.getY() - a.getY()) ? SHEAR_X : SHEAR_Y;
        }
        return mode;
    }

    boolean press(Point2D screen, AffineTransform placement) {
        active = hitTest(screen, placement);
        if (active == Cursor.DEFAULT_CURSOR) {
            return false;
        }
        try {
            start = frameToPanel(placement).inverseTransform(screen, null);
            startPivot = state.frame.inverse().transform(state.pivot);
            startScene = sceneToPanel(placement).inverseTransform(screen, null);
            startScenePivot = getPivot();
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
        Point2D scenePoint;
        try {
            AffineTransform frame = new AffineTransform(placement);
            frame.concatenate(startState.frame.toTransform());
            point = frame.inverseTransform(screen, null);
            scenePoint = sceneToPanel(placement).inverseTransform(screen, null);
        } catch (NoninvertibleTransformException ex) {
            return true;
        }
        double dx = point.getX() - start.getX();
        double dy = point.getY() - start.getY();
        if (active == Cursor.HAND_CURSOR) {
            setPivot(new Point2D.Double(startScenePivot.getX() + scenePoint.getX() - startScene.getX(),
                    startScenePivot.getY() + scenePoint.getY() - startScene.getY()), null);
            return true;
        }
        AffineTransform delta = new AffineTransform();
        if (active == Cursor.MOVE_CURSOR) {
            delta.translate(Math.rint(scenePoint.getX() - startScene.getX()), Math.rint(scenePoint.getY() - startScene.getY()));
        } else if (active == ROTATE) {
            double angle = Math.atan2(scenePoint.getY() - startScenePivot.getY(), scenePoint.getX() - startScenePivot.getX())
                    - Math.atan2(startScene.getY() - startScenePivot.getY(), startScene.getX() - startScenePivot.getX());
            if (constrain) {
                angle = Math.rint(angle / (Math.PI / 12)) * Math.PI / 12;
            }
            delta.rotate(angle, startScenePivot.getX(), startScenePivot.getY());
        } else if (active == SHEAR_X || active == SHEAR_Y) {
            Point2D opposite = handle((handleIndex + 4) % HANDLES.length);
            delta.translate(opposite.getX(), opposite.getY());
            delta.shear(active == SHEAR_X ? dx * (handleIndex == 0 ? -1 : 1) : 0,
                    active == SHEAR_Y ? dy * (handleIndex == 6 ? -1 : 1) : 0);
            delta.translate(-opposite.getX(), -opposite.getY());
        } else {
            Point2D anchor = (Point2D) startPivot.clone();
            Point2D handle = handle(handleIndex);
            Point2D opposite = handle((handleIndex + 4) % HANDLES.length);
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
        preview = active == Cursor.MOVE_CURSOR || active == ROTATE
                ? parentMatrix.inverse().concatenate(new Matrix(delta)).concatenate(parentMatrix)
                : startState.frame.concatenate(new Matrix(delta)).concatenate(startState.frame.inverse());
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
            AffineTransform frame = frameToPanel(placement);
            Shape box = frameOutline(frame);
            g.setColor(Color.white);
            g.setStroke(new BasicStroke(3));
            g.draw(box);
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(1));
            g.draw(box);
            for (int i = 0; i < HANDLES.length; i++) {
                Point2D point = frame.transform(handle(i), null);
                ImagePanel.drawHandle(g, point.getX(), point.getY());
            }
            ImagePanel.drawRegistrationPoint(g, placement.transform(preview.transform(state.pivot), null));
        } finally {
            g.dispose();
        }
    }
}
