/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.shapes.ShapeTransformer;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import java.awt.geom.Point2D;
import java.io.IOException;

/**
 * Moves an entire shape relative to its fixed local origin. Each preview starts
 * from the same snapshot, including gradient and bitmap fill matrices.
 */
public final class ShapeAnchorMove {

    private final ShapeTag original;
    private final Point2D start;

    public ShapeAnchorMove(ShapeTag original, Point2D start) throws IOException, InterruptedException {
        this.original = (ShapeTag) original.cloneTag();
        this.start = new Point2D.Double(start.getX(), start.getY());
    }

    /**
     * Returns a detached preview, or null when the pointer is back at its start.
     * Pointer coordinates are shape-local twips supplied by the stage.
     */
    public ShapeTag moveTo(Point2D point) throws IOException, InterruptedException {
        double dx = Math.rint(point.getX() - start.getX());
        double dy = Math.rint(point.getY() - start.getY());
        if (dx == 0 && dy == 0) {
            return null;
        }
        ShapeTag moved = (ShapeTag) original.cloneTag();
        Matrix translation = Matrix.getTranslateInstance(dx, dy);
        ShapeTransformer transformer = new ShapeTransformer();
        transformer.transformSHAPE(translation, moved.shapes, moved.getShapeNum());
        transformer.transformStyles(translation, moved.shapes.fillStyles, moved.shapes.lineStyles, moved.getShapeNum());
        moved.shapes.clearCachedOutline();
        moved.updateBounds();
        moved.setModified(true);
        return moved;
    }
}
