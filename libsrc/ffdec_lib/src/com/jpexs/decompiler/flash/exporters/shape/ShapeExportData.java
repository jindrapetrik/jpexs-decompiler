/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.exporters.commonshape.FillStyle;
import com.jpexs.decompiler.flash.exporters.commonshape.LineStyle;
import java.util.List;

/**
 * Shape export data.
 *
 * @author JPEXS
 */
public class ShapeExportData {

    /**
     * Fill styles
     */
    public List<FillStyle> fillStyles;

    /**
     * Line styles
     */
    public List<LineStyle> lineStyles;

    /**
     * Fill paths
     */
    public List<List<IEdge>> fillPaths;

    /**
     * Line paths
     */
    public List<List<IEdge>> linePaths;
}
