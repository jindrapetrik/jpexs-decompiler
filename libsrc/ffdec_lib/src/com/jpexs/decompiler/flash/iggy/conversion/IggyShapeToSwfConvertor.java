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
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts Iggy shape to SWF shape.
 *
 * @author JPEXS
 */
public class IggyShapeToSwfConvertor {

    public static SHAPE convertCharToShape(IggyShape igchar) {
        return convertCharToShape(igchar, 1024);
    }

    /**
     * Use the source font's em square without rounding via an intermediate
     * Font2.
     */
    public static SHAPE convertCharToShape(IggyShape igchar, int unitsPerEm) {
        SHAPE shape = new SHAPE();
        List<SHAPERECORD> retList = new ArrayList<>();
        List<IggyShapeNode> ignodes = igchar.getNodes();

        int prevX = 0;
        int prevY = 0;

        for (IggyShapeNode ign : ignodes) {
            if (ign.getNodeType() == IggyShapeNode.NODE_TYPE_MOVE) {
                StyleChangeRecord scr = new StyleChangeRecord();
                scr.stateMoveTo = true;
                prevX = scr.moveDeltaX = (int) Math.round(ign.getTargetX() * (double) unitsPerEm);
                prevY = scr.moveDeltaY = (int) Math.round(ign.getTargetY() * (double) unitsPerEm);
                scr.fillStyles = new FILLSTYLEARRAY();
                scr.lineStyles = new LINESTYLEARRAY();
                scr.calculateBits();
                retList.add(scr);
            } else {

                int curX1 = (int) Math.round(ign.getTargetX() * (double) unitsPerEm);
                int curY1 = (int) Math.round(ign.getTargetY() * (double) unitsPerEm);

                int curX2 = (int) Math.round(ign.getControlX() * (double) unitsPerEm);
                int curY2 = (int) Math.round(ign.getControlY() * (double) unitsPerEm);

                if (ign.getNodeType() == IggyShapeNode.NODE_TYPE_LINE_TO) {
                    StraightEdgeRecord ser = new StraightEdgeRecord();
                    ser.deltaX = curX1 - prevX;
                    ser.deltaY = curY1 - prevY;
                    ser.generalLineFlag = true;
                    ser.simplify();
                    ser.calculateBits();
                    prevX = curX1;
                    prevY = curY1;
                    retList.add(ser);
                } else if (ign.getNodeType() == IggyShapeNode.NODE_TYPE_CURVE_POINT) {
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    cer.controlDeltaX = curX2 - prevX;
                    cer.controlDeltaY = curY2 - prevY;
                    cer.anchorDeltaX = curX1 - curX2;
                    cer.anchorDeltaY = curY1 - curY2;
                    prevX = curX1;
                    prevY = curY1;
                    cer.calculateBits();
                    retList.add(cer);
                }
            }
        }
        if (!retList.isEmpty()) {
            StyleChangeRecord init;
            if (retList.get(0) instanceof StyleChangeRecord) {
                init = (StyleChangeRecord) retList.get(0);
            } else {
                init = new StyleChangeRecord();
                retList.add(0, init);
            }
            init.stateFillStyle0 = true;
            init.fillStyle0 = 1;
            shape.numFillBits = 1;

        } else {
            shape.numFillBits = 0;

        }
        retList.add(new EndShapeRecord());
        shape.shapeRecords = retList;
        shape.numLineBits = 0;

        return shape;
    }
}
