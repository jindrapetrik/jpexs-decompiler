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
package com.jpexs.decompiler.flash.shapes;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Transforms shapes with matrix.
 * @author JPEXS
 */
public class ShapeTransformer {

    /**
     * Transform styles.
     * @param matrix Matrix
     * @param fillStyles Fill styles
     * @param lineStyles Line styles
     * @param shapeNum Shape type (DefineShape = 1, DefineShape2 = 2, etc.)
     */
    public void transformStyles(Matrix matrix, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, int shapeNum) {
        List<FILLSTYLE> fillStyleToTransform = new ArrayList<>();
        for (FILLSTYLE fs : fillStyles.fillStyles) {
            fillStyleToTransform.add(fs);
        }
        
        double strokeScale = Math.max(Math.abs(matrix.scaleX), Math.abs(matrix.scaleY));
        if (shapeNum >= 4) {
            for (LINESTYLE2 ls : lineStyles.lineStyles2) {
                if (ls.hasFillFlag) {
                    fillStyleToTransform.add(ls.fillType);
                }
                ls.width *= strokeScale;
            }            
        } else {
            for (LINESTYLE ls : lineStyles.lineStyles) {
                ls.width *= strokeScale;
            }
        }

        for (FILLSTYLE fs : fillStyleToTransform) {
            switch (fs.fillStyleType) {
                case FILLSTYLE.CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                case FILLSTYLE.REPEATING_BITMAP:
                    fs.bitmapMatrix = new Matrix(fs.bitmapMatrix).preConcatenate(matrix).toMATRIX();
                    break;
                case FILLSTYLE.LINEAR_GRADIENT:
                case FILLSTYLE.RADIAL_GRADIENT:
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                    fs.gradientMatrix = new Matrix(fs.gradientMatrix).preConcatenate(matrix).toMATRIX();
                    break;
            }
        }
    }

    /**
     * Transform morph styles.
     * @param matrix Matrix
     * @param fillStyles Fill styles
     * @param lineStyles Line styles
     * @param morphShapeNum Morphshape type (DefineMorphshape = 1, DefineMorphshape2 = 2)
     * @param doStart Modify start styles
     * @param doEnd Modify end styles
     */
    public void transformMorphStyles(Matrix matrix, MORPHFILLSTYLEARRAY fillStyles, MORPHLINESTYLEARRAY lineStyles, int morphShapeNum, boolean doStart, boolean doEnd) {
        List<MORPHFILLSTYLE> fillStyleToTransform = new ArrayList<>();
        for (MORPHFILLSTYLE fs : fillStyles.fillStyles) {
            fillStyleToTransform.add(fs);
        }

        if (morphShapeNum == 2) {
            for (MORPHLINESTYLE2 ls : lineStyles.lineStyles2) {
                if (ls.hasFillFlag) {
                    fillStyleToTransform.add(ls.fillType);
                }
            }
        }

        for (MORPHFILLSTYLE fs : fillStyleToTransform) {
            switch (fs.fillStyleType) {
                case FILLSTYLE.CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                case FILLSTYLE.REPEATING_BITMAP:
                    if (doStart) {
                        fs.startBitmapMatrix = new Matrix(fs.startBitmapMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    if (doEnd) {
                        fs.endBitmapMatrix = new Matrix(fs.endBitmapMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    break;
                case FILLSTYLE.LINEAR_GRADIENT:
                case FILLSTYLE.RADIAL_GRADIENT:
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                    if (doStart) {
                        fs.startGradientMatrix = new Matrix(fs.startGradientMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    if (doEnd) {
                        fs.endGradientMatrix = new Matrix(fs.endGradientMatrix).preConcatenate(matrix).toMATRIX();
                    }
                    break;
            }
        }
    }

    /**
     * Transform SHAPE.
     * @param matrix Matrix
     * @param shape SHAPE
     * @param shapeNum Shape type (DefineShape = 1, DefineShape2 = 2, etc.)
     */
    public void transformSHAPE(Matrix matrix, SHAPE shape, int shapeNum) {
        transformShapeRecords(matrix, shape.shapeRecords, shapeNum);
    }
    
    /**
     * Transform SHAPERECORDs.
     * @param matrix Matrix
     * @param shapeRecords Records
     * @param shapeNum Shape type (DefineShape = 1, DefineShape2 = 2, etc.)
     */
    public void transformShapeRecords(Matrix matrix, List<SHAPERECORD> shapeRecords, int shapeNum) {
        int x = 0;
        int y = 0;
        StyleChangeRecord lastStyleChangeRecord = null;
        boolean wasMoveTo = false;
        for (SHAPERECORD rec : shapeRecords) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                lastStyleChangeRecord = scr;
                if (scr.stateNewStyles) {
                    transformStyles(matrix, scr.fillStyles, scr.lineStyles, shapeNum);
                }
                if (scr.stateMoveTo) {
                    Point nextPoint = new Point(scr.moveDeltaX, scr.moveDeltaY);
                    x = scr.changeX(x);
                    y = scr.changeY(y);
                    Point nextPoint2 = matrix.transform(nextPoint);
                    scr.moveDeltaX = nextPoint2.x;
                    scr.moveDeltaY = nextPoint2.y;
                    scr.calculateBits();
                    wasMoveTo = true;
                }
            }

            if (((rec instanceof StraightEdgeRecord) || (rec instanceof CurvedEdgeRecord)) && !wasMoveTo) {
                if (lastStyleChangeRecord != null) {
                    Point nextPoint2 = matrix.transform(new Point(x, y));
                    if (nextPoint2.x != 0 || nextPoint2.y != 0) {
                        lastStyleChangeRecord.stateMoveTo = true;
                        lastStyleChangeRecord.moveDeltaX = nextPoint2.x;
                        lastStyleChangeRecord.moveDeltaY = nextPoint2.y;
                        lastStyleChangeRecord.calculateBits();
                        wasMoveTo = true;
                    }
                }
            }
            if (rec instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) rec;
                ser.generalLineFlag = true;
                ser.vertLineFlag = false;
                Point currentPoint = new Point(x, y);
                Point nextPoint = new Point(x + ser.deltaX, y + ser.deltaY);
                x = ser.changeX(x);
                y = ser.changeY(y);
                Point currentPoint2 = matrix.transform(currentPoint);
                Point nextPoint2 = matrix.transform(nextPoint);
                ser.deltaX = nextPoint2.x - currentPoint2.x;
                ser.deltaY = nextPoint2.y - currentPoint2.y;
                ser.simplify();
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                Point currentPoint = new Point(x, y);
                Point controlPoint = new Point(x + cer.controlDeltaX, y + cer.controlDeltaY);
                Point anchorPoint = new Point(x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY);
                x = cer.changeX(x);
                y = cer.changeY(y);

                Point currentPoint2 = matrix.transform(currentPoint);
                Point controlPoint2 = matrix.transform(controlPoint);
                Point anchorPoint2 = matrix.transform(anchorPoint);

                cer.controlDeltaX = controlPoint2.x - currentPoint2.x;
                cer.controlDeltaY = controlPoint2.y - currentPoint2.y;
                cer.anchorDeltaX = anchorPoint2.x - controlPoint2.x;
                cer.anchorDeltaY = anchorPoint2.y - controlPoint2.y;
                cer.calculateBits();
            }
        }
    }
}
