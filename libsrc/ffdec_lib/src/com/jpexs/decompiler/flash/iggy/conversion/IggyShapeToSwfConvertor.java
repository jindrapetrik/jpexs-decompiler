/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyShape;
import com.jpexs.decompiler.flash.iggy.IggyShapeNode;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IggyShapeToSwfConvertor {

    private static int makeLengthsEmX(double val) {
        return (int) (val * 1024.0);
    }

    private static int makeLengthsEmY(double val) {
        return (int) (val * 1024.0);
    }

    public static SHAPE convertCharToShape(IggyShape igchar) {
        SHAPE shape = new SHAPE();
        List<SHAPERECORD> retList = new ArrayList<>();
        List<IggyShapeNode> ignodes = igchar.getNodes();

        int prevX = 0;
        int prevY = 0;

        for (IggyShapeNode ign : ignodes) {
            if (ign.getNodeType() == IggyShapeNode.NODE_TYPE_MOVE) {
                StyleChangeRecord scr = new StyleChangeRecord();
                scr.stateMoveTo = true;
                prevX = scr.moveDeltaX = makeLengthsEmX(ign.getTargetX());
                prevY = scr.moveDeltaY = makeLengthsEmY(ign.getTargetY());
                scr.fillStyles = new FILLSTYLEARRAY();
                scr.lineStyles = new LINESTYLEARRAY();
                scr.calculateBits();
                retList.add(scr);
            } else {

                int curX1 = makeLengthsEmX(ign.getTargetX());
                int curY1 = makeLengthsEmY(ign.getTargetY());

                int curX2 = makeLengthsEmX(ign.getControlX());
                int curY2 = makeLengthsEmY(ign.getControlY());

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
