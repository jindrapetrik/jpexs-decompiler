/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

/**
 * Tool modes available while editing a shape.
 *
 * @author JPEXS
 */
public enum ShapeTool {
    BRUSH("brush"),
    PENCIL("pencil"),
    PEN("pen"),
    SHAPES("shapes"),
    ERASER("eraser"),
    PAINT("paint"),
    INK("ink"),
    GRADIENT_TRANSFORM("gradientTransform"),
    EYEDROPPER("eyedropper"),
    SELECT("select"),
    EDIT_POINTS("editPoints"),
    ANCHOR_POINT("anchorPoint"),
    LASSO("lasso");

    private final String id;

    ShapeTool(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
