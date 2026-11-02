/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A detached fill-transform gesture, evaluated against the original selection.
 */
public final class ShapeFillTransform {

    private final ShapeSelection model;
    private final List<ShapeSelection.Selection> selected;
    private final FILLSTYLE fill;

    public ShapeFillTransform(ShapeSelection model, ShapeSelection.Selection selected) {
        this(model, Collections.singletonList(selected));
    }

    public ShapeFillTransform(ShapeSelection model, List<ShapeSelection.Selection> selected) {
        this.model = model;
        this.selected = new ArrayList<>(selected);
        this.fill = Helper.deepCopy(fillOf(selected.get(0)));
    }

    public static FILLSTYLE fillOf(ShapeSelection.Selection selected) {
        if (selected == null) {
            return null;
        }
        return selected.stroke ? selected.line instanceof LINESTYLE2 && ((LINESTYLE2) selected.line).hasFillFlag
                ? ((LINESTYLE2) selected.line).fillType : null : selected.fill;
    }

    public static boolean canTransform(ShapeSelection.Selection selected) {
        FILLSTYLE fill = fillOf(selected);
        return fill != null && (fill.hasGradient() && fill.gradientMatrix != null || fill.hasBitmap() && fill.bitmapMatrix != null);
    }

    public ShapeSelection.BatchEdit setMatrix(MATRIX matrix) {
        if (fill.hasGradient()) {
            fill.gradientMatrix = Helper.deepCopy(matrix);
        } else {
            fill.bitmapMatrix = Helper.deepCopy(matrix);
        }
        return edit();
    }

    public ShapeSelection.BatchEdit setFocalPoint(float focalPoint) {
        ((FOCALGRADIENT) fill.gradient).focalPoint = focalPoint;
        return edit();
    }

    private ShapeSelection.BatchEdit edit() {
        boolean stroke = selected.get(0).stroke;
        return model.changeAll(selected, stroke ? null : item -> Helper.deepCopy(fill),
                !stroke ? null : item -> {
                    LINESTYLE2 line = item.line instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) item.line)
                            : ((LINESTYLE) item.line).toLineStyle2();
                    line.fillType = Helper.deepCopy(fill);
                    return line;
                });
    }
}
