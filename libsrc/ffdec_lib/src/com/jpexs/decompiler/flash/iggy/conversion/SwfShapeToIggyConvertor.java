/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggyShapeNode;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts SWF shape to Iggy shape.
 *
 * @author JPEXS
 */
public class SwfShapeToIggyConvertor {

    public static IggyShape convertShape(int shapeNum, SHAPE swfShape) {
        return convertShape(shapeNum, swfShape, 1024);
    }

    public static IggyShape convertShape(int shapeNum, SHAPE swfShape, int unitsPerEm) {
        List<IggyShapeNode> nodes = new ArrayList<>();
        RECT bounds = swfShape.getBounds(shapeNum);
        boolean first = true;
        int curX = 0;
        int curY = 0;
        for (SHAPERECORD rec : swfShape.shapeRecords) {
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                curX += ser.deltaX;
                curY += ser.deltaY;
                nodes.add(new IggyShapeNode(curX / (float) unitsPerEm, curY / (float) unitsPerEm, 0f, 0f, IggyShapeNode.NODE_TYPE_LINE_TO, 0, first));
            } else if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                int controlX = curX + cer.controlDeltaX;
                int controlY = curY + cer.controlDeltaY;
                curX = curX + cer.controlDeltaX + cer.anchorDeltaX;
                curY = curY + cer.controlDeltaY + cer.anchorDeltaY;
                nodes.add(new IggyShapeNode(curX / (float) unitsPerEm, curY / (float) unitsPerEm, controlX / (float) unitsPerEm, controlY / (float) unitsPerEm, IggyShapeNode.NODE_TYPE_CURVE_POINT, 0, first));
            } else if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo) {
                    curX = scr.moveDeltaX;
                    curY = scr.moveDeltaY;
                    nodes.add(new IggyShapeNode(curX / (float) unitsPerEm, curY / (float) unitsPerEm, 0f, 0f, IggyShapeNode.NODE_TYPE_MOVE, 0, first));
                }
            }
            first = false;
        }
        IggyShape ret = new IggyShape(bounds.Xmin / (float) unitsPerEm, bounds.Ymin / (float) unitsPerEm, bounds.Xmax / (float) unitsPerEm, bounds.Ymax / (float) unitsPerEm, nodes);
        return ret;
    }
}
