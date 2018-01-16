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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS, Claus Wahlers
 */
public abstract class MorphShapeExporterBase implements IMorphShapeExporter {

    protected final SHAPE shape;

    protected final SHAPE shapeEnd;

    protected List<FILLSTYLE> _fillStyles;

    protected List<LINESTYLE> _lineStyles;

    protected List<FILLSTYLE> _fillStylesEnd;

    protected List<LINESTYLE> _lineStylesEnd;

    protected List<Map<Integer, List<IMorphEdge>>> _fillEdgeMaps;

    protected List<Map<Integer, List<IMorphEdge>>> _lineEdgeMaps;

    private boolean edgeMapsCreated;

    protected ColorTransform colorTransform;

    public MorphShapeExporterBase(SHAPE shape, SHAPE endShape, ColorTransform colorTransform) {
        this.shape = shape;
        this.shapeEnd = endShape;
        this.colorTransform = colorTransform;
        _fillStyles = new ArrayList<>();
        _lineStyles = new ArrayList<>();
        if (shape instanceof SHAPEWITHSTYLE) {
            SHAPEWITHSTYLE shapeWithStyle = (SHAPEWITHSTYLE) shape;
            _fillStyles.addAll(Arrays.asList(shapeWithStyle.fillStyles.fillStyles));
            _lineStyles.addAll(Arrays.asList(shapeWithStyle.lineStyles.lineStyles));
        }
        _fillStylesEnd = new ArrayList<>();
        _lineStylesEnd = new ArrayList<>();
        if (endShape instanceof SHAPEWITHSTYLE) {
            SHAPEWITHSTYLE shapeWithStyle = (SHAPEWITHSTYLE) endShape;
            _fillStylesEnd.addAll(Arrays.asList(shapeWithStyle.fillStyles.fillStyles));
            _lineStylesEnd.addAll(Arrays.asList(shapeWithStyle.lineStyles.lineStyles));
        }
    }

    public void export() {
        // Create edge maps
        _fillEdgeMaps = new ArrayList<>();
        _lineEdgeMaps = new ArrayList<>();
        createEdgeMaps(_fillStyles, _lineStyles, _fillStylesEnd, _lineStylesEnd, _fillEdgeMaps, _lineEdgeMaps);

        // Let the doc handler know that a shape export starts
        beginShape();
        // Export fills and strokes for each group separately
        for (int i = 0; i < _lineEdgeMaps.size(); i++) {
            // Export fills first
            exportFillPath(i);
            // Export strokes last
            exportLinePath(i);
        }
        // Let the doc handler know that we're done exporting a shape
        endShape();
    }

    protected void createEdgeMaps(List<FILLSTYLE> fillStyles, List<LINESTYLE> lineStyles,
            List<FILLSTYLE> fillStylesEnd, List<LINESTYLE> lineStylesEnd,
            List<Map<Integer, List<IMorphEdge>>> fillEdgeMaps, List<Map<Integer, List<IMorphEdge>>> lineEdgeMaps) {
        if (!edgeMapsCreated) {
            int xPos = 0;
            int yPos = 0;
            int xPosEnd = 0;
            int yPosEnd = 0;
            int fillStyleIdxOffset = 0;
            int lineStyleIdxOffset = 0;
            int currentFillStyleIdx0 = 0;
            int currentFillStyleIdx1 = 0;
            int currentLineStyleIdx = 0;
            List<IMorphEdge> subPath = new ArrayList<>();
            Map<Integer, List<IMorphEdge>> currentFillEdgeMap = new HashMap<>();
            Map<Integer, List<IMorphEdge>> currentLineEdgeMap = new HashMap<>();
            List<SHAPERECORD> records = shape.shapeRecords;
            List<SHAPERECORD> recordsEnd = shapeEnd.shapeRecords;
            if (records.size() != recordsEnd.size()) {
                throw new Error("Begin and end shaperecord list length should be the same.");
            }
            for (int i = 0; i < records.size(); i++) {
                SHAPERECORD shapeRecord = records.get(i);
                SHAPERECORD shapeRecordEnd = recordsEnd.get(i);
                if ((shapeRecord instanceof StyleChangeRecord && !(shapeRecordEnd instanceof StyleChangeRecord))
                        || (shapeRecord instanceof StraightEdgeRecord && !(shapeRecordEnd instanceof StraightEdgeRecord))
                        || (shapeRecord instanceof CurvedEdgeRecord && !(shapeRecordEnd instanceof CurvedEdgeRecord))
                        || (shapeRecord instanceof EndShapeRecord && !(shapeRecordEnd instanceof EndShapeRecord))) {
                    throw new Error("Begin and end shaperecord should have the same type.");
                }
                if (shapeRecord instanceof StyleChangeRecord) {
                    StyleChangeRecord styleChangeRecord = (StyleChangeRecord) shapeRecord;
                    if (styleChangeRecord.stateLineStyle || styleChangeRecord.stateFillStyle0 || styleChangeRecord.stateFillStyle1) {
                        processSubPath(subPath, currentLineStyleIdx, currentFillStyleIdx0, currentFillStyleIdx1, currentFillEdgeMap, currentLineEdgeMap);
                        subPath = new ArrayList<>();
                    }
                    if (styleChangeRecord.stateNewStyles) {
                        fillStyleIdxOffset = fillStyles.size();
                        lineStyleIdxOffset = lineStyles.size();
                        appendFillStyles(fillStyles, styleChangeRecord.fillStyles.fillStyles);
                        appendLineStyles(lineStyles, styleChangeRecord.lineStyles.lineStyles);
                        appendFillStyles(fillStylesEnd, styleChangeRecord.fillStyles.fillStyles);
                        appendLineStyles(lineStylesEnd, styleChangeRecord.lineStyles.lineStyles);
                    }
                    // Check if all styles are reset to 0.
                    // This (probably) means that a new group starts with the next record
                    if (styleChangeRecord.stateLineStyle && styleChangeRecord.lineStyle == 0
                            && styleChangeRecord.stateFillStyle0 && styleChangeRecord.fillStyle0 == 0
                            && styleChangeRecord.stateFillStyle1 && styleChangeRecord.fillStyle1 == 0) {
                        // do not clean the edges for morphshapes
                        //cleanEdgeMap(currentFillEdgeMap);
                        //cleanEdgeMap(currentLineEdgeMap);
                        fillEdgeMaps.add(currentFillEdgeMap);
                        lineEdgeMaps.add(currentLineEdgeMap);
                        currentFillEdgeMap = new HashMap<>();
                        currentLineEdgeMap = new HashMap<>();
                        currentLineStyleIdx = 0;
                        currentFillStyleIdx0 = 0;
                        currentFillStyleIdx1 = 0;
                    } else {
                        if (styleChangeRecord.stateLineStyle) {
                            currentLineStyleIdx = styleChangeRecord.lineStyle;
                            if (currentLineStyleIdx > 0) {
                                currentLineStyleIdx += lineStyleIdxOffset;
                            }
                        }
                        if (styleChangeRecord.stateFillStyle0) {
                            currentFillStyleIdx0 = styleChangeRecord.fillStyle0;
                            if (currentFillStyleIdx0 > 0) {
                                currentFillStyleIdx0 += fillStyleIdxOffset;
                            }
                        }
                        if (styleChangeRecord.stateFillStyle1) {
                            currentFillStyleIdx1 = styleChangeRecord.fillStyle1;
                            if (currentFillStyleIdx1 > 0) {
                                currentFillStyleIdx1 += fillStyleIdxOffset;
                            }
                        }
                    }
                    if (styleChangeRecord.stateMoveTo) {
                        xPos = styleChangeRecord.moveDeltaX;
                        yPos = styleChangeRecord.moveDeltaY;
                    }

                    StyleChangeRecord styleChangeRecordEnd = (StyleChangeRecord) shapeRecordEnd;
                    if (styleChangeRecordEnd.stateMoveTo) {
                        xPosEnd = styleChangeRecordEnd.moveDeltaX;
                        yPosEnd = styleChangeRecordEnd.moveDeltaY;
                    }
                } else if (shapeRecord instanceof StraightEdgeRecord) {
                    StraightEdgeRecord straightEdgeRecord = (StraightEdgeRecord) shapeRecord;
                    int xPosFrom = xPos;
                    int yPosFrom = yPos;
                    if (straightEdgeRecord.generalLineFlag) {
                        xPos += straightEdgeRecord.deltaX;
                        yPos += straightEdgeRecord.deltaY;
                    } else if (straightEdgeRecord.vertLineFlag) {
                        yPos += straightEdgeRecord.deltaY;
                    } else {
                        xPos += straightEdgeRecord.deltaX;
                    }

                    StraightEdgeRecord straightEdgeRecordEnd = (StraightEdgeRecord) shapeRecordEnd;
                    int xPosEndFrom = xPosEnd;
                    int yPosEndFrom = yPosEnd;
                    if (straightEdgeRecordEnd.generalLineFlag) {
                        xPosEnd += straightEdgeRecordEnd.deltaX;
                        yPosEnd += straightEdgeRecordEnd.deltaY;
                    } else if (straightEdgeRecordEnd.vertLineFlag) {
                        yPosEnd += straightEdgeRecordEnd.deltaY;
                    } else {
                        xPosEnd += straightEdgeRecordEnd.deltaX;
                    }

                    subPath.add(new StraightMorphEdge(xPosFrom, yPosFrom, xPos, yPos, xPosEndFrom, yPosEndFrom, xPosEnd, yPosEnd, currentLineStyleIdx, currentFillStyleIdx1));
                } else if (shapeRecord instanceof CurvedEdgeRecord) {
                    CurvedEdgeRecord curvedEdgeRecord = (CurvedEdgeRecord) shapeRecord;
                    int xPosFrom = xPos;
                    int yPosFrom = yPos;
                    int xPosControl = xPos + curvedEdgeRecord.controlDeltaX;
                    int yPosControl = yPos + curvedEdgeRecord.controlDeltaY;
                    xPos = xPosControl + curvedEdgeRecord.anchorDeltaX;
                    yPos = yPosControl + curvedEdgeRecord.anchorDeltaY;

                    CurvedEdgeRecord curvedEdgeRecordEnd = (CurvedEdgeRecord) shapeRecordEnd;
                    int xPosEndFrom = xPosEnd;
                    int yPosEndFrom = yPosEnd;
                    int xPosEndControl = xPosEnd + curvedEdgeRecordEnd.controlDeltaX;
                    int yPosEndControl = yPosEnd + curvedEdgeRecordEnd.controlDeltaY;
                    xPosEnd = xPosEndControl + curvedEdgeRecordEnd.anchorDeltaX;
                    yPosEnd = yPosEndControl + curvedEdgeRecordEnd.anchorDeltaY;

                    subPath.add(new CurvedMorphEdge(xPosFrom, yPosFrom, xPosControl, yPosControl, xPos, yPos, xPosEndFrom, yPosEndFrom, xPosEndControl, yPosEndControl, xPosEnd, yPosEnd, currentLineStyleIdx, currentFillStyleIdx1));
                } else if (shapeRecord instanceof EndShapeRecord) {
                    // We're done. Process the last subpath, if any
                    processSubPath(subPath, currentLineStyleIdx, currentFillStyleIdx0, currentFillStyleIdx1, currentFillEdgeMap, currentLineEdgeMap);
                    // do not clean the edges for morphshapes
                    //cleanEdgeMap(currentFillEdgeMap);
                    //cleanEdgeMap(currentLineEdgeMap);
                    fillEdgeMaps.add(currentFillEdgeMap);
                    lineEdgeMaps.add(currentLineEdgeMap);
                }
            }
            edgeMapsCreated = true;
        }
    }

    protected void processSubPath(List<IMorphEdge> subPath, int lineStyleIdx, int fillStyleIdx0, int fillStyleIdx1,
            Map<Integer, List<IMorphEdge>> currentFillEdgeMap, Map<Integer, List<IMorphEdge>> currentLineEdgeMap) {
        List<IMorphEdge> path;
        if (fillStyleIdx0 != 0) {
            path = currentFillEdgeMap.get(fillStyleIdx0);
            if (path == null) {
                path = new ArrayList<>();
                currentFillEdgeMap.put(fillStyleIdx0, path);
            }
            for (int j = subPath.size() - 1; j >= 0; j--) {
                path.add(subPath.get(j).reverseWithNewFillStyle(fillStyleIdx0));
            }
        }
        if (fillStyleIdx1 != 0) {
            path = currentFillEdgeMap.get(fillStyleIdx1);
            if (path == null) {
                path = new ArrayList<>();
                currentFillEdgeMap.put(fillStyleIdx1, path);
            }
            appendEdges(path, subPath);
        }
        if (lineStyleIdx != 0) {
            path = currentLineEdgeMap.get(lineStyleIdx);
            if (path == null) {
                path = new ArrayList<>();
                currentLineEdgeMap.put(lineStyleIdx, path);
            }
            appendEdges(path, subPath);
        }
    }

    protected void exportFillPath(int groupIndex) {
        List<IMorphEdge> path = createPathFromEdgeMap(_fillEdgeMaps.get(groupIndex));
        int posX = Integer.MAX_VALUE;
        int posY = Integer.MAX_VALUE;
        int fillStyleIdx = Integer.MAX_VALUE;
        if (path.size() > 0) {
            beginFills();
            for (int i = 0; i < path.size(); i++) {
                IMorphEdge e = path.get(i);
                if (fillStyleIdx != e.getFillStyleIdx()) {
                    if (fillStyleIdx != Integer.MAX_VALUE) {
                        endFill();
                    }
                    fillStyleIdx = e.getFillStyleIdx();
                    posX = Integer.MAX_VALUE;
                    posY = Integer.MAX_VALUE;
                    if (fillStyleIdx - 1 < _fillStyles.size()) {
                        Matrix matrix;
                        Matrix matrixEnd;
                        FILLSTYLE fillStyle = _fillStyles.get(fillStyleIdx - 1);
                        FILLSTYLE fillStyleEnd = _fillStylesEnd.get(fillStyleIdx - 1);
                        switch (fillStyle.fillStyleType) {
                            case FILLSTYLE.SOLID:
                                // Solid fill
                                if (colorTransform == null) {
                                    beginFill(fillStyle.color, fillStyleEnd.color);
                                } else {
                                    beginFill(colorTransform.apply(fillStyle.color), colorTransform.apply(fillStyleEnd.color));
                                }
                                break;
                            case FILLSTYLE.LINEAR_GRADIENT:
                            case FILLSTYLE.RADIAL_GRADIENT:
                            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                                // Gradient fill
                                matrix = new Matrix(fillStyle.gradientMatrix);
                                matrixEnd = new Matrix(fillStyleEnd.gradientMatrix);
                                beginGradientFill(
                                        fillStyle.fillStyleType,
                                        colorTransform == null ? fillStyle.gradient.gradientRecords : colorTransform.apply(fillStyle.gradient.gradientRecords),
                                        colorTransform == null ? fillStyleEnd.gradient.gradientRecords : colorTransform.apply(fillStyleEnd.gradient.gradientRecords),
                                        matrix,
                                        matrixEnd,
                                        fillStyle.gradient.spreadMode,
                                        fillStyle.gradient.interpolationMode,
                                        (fillStyle.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyle.gradient).focalPoint : 0,
                                        (fillStyleEnd.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyleEnd.gradient).focalPoint : 0
                                );
                                break;
                            case FILLSTYLE.REPEATING_BITMAP:
                            case FILLSTYLE.CLIPPED_BITMAP:
                            case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                            case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                                // Bitmap fill
                                matrix = new Matrix(fillStyle.bitmapMatrix);
                                matrixEnd = new Matrix(fillStyleEnd.bitmapMatrix);
                                beginBitmapFill(
                                        fillStyle.bitmapId,
                                        matrix,
                                        matrixEnd,
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP),
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.CLIPPED_BITMAP),
                                        colorTransform
                                );
                                break;
                        }
                    } else {
                        // Font shapes define no fillstyles per se, but do reference fillstyle index 1,
                        // which represents the font color. We just report null in this case.
                        beginFill(null, null);
                    }
                }
                if (posX != e.getFromX() || posY != e.getFromY()) {
                    moveTo(e.getFromX(), e.getFromY(), e.getFromEndX(), e.getFromEndY());
                }
                if (e instanceof CurvedMorphEdge) {
                    CurvedMorphEdge c = (CurvedMorphEdge) e;
                    curveTo(c.getControlX(), c.getControlY(), c.toX, c.toY, c.getControlEndX(), c.getControlEndY(), c.toEndX, c.toEndY);
                } else {
                    lineTo(e.getToX(), e.getToY(), e.getToEndX(), e.getToEndY());
                }
                posX = e.getToX();
                posY = e.getToY();
            }
            if (fillStyleIdx != Integer.MAX_VALUE) {
                endFill();
            }
            endFills();
        }
    }

    protected void exportLinePath(int groupIndex) {
        List<IMorphEdge> path = createPathFromEdgeMap(_lineEdgeMaps.get(groupIndex));
        int posX = Integer.MAX_VALUE;
        int posY = Integer.MAX_VALUE;
        int lineStyleIdx = Integer.MAX_VALUE;
        if (path.size() > 0) {
            beginLines();
            for (int i = 0; i < path.size(); i++) {
                IMorphEdge e = path.get(i);
                if (lineStyleIdx != e.getLineStyleIdx()) {
                    lineStyleIdx = e.getLineStyleIdx();
                    posX = Integer.MAX_VALUE;
                    posY = Integer.MAX_VALUE;
                    LINESTYLE lineStyle = null;
                    LINESTYLE lineStyleEnd = null;
                    try {
                        lineStyle = _lineStyles.get(lineStyleIdx - 1);
                        lineStyleEnd = _lineStylesEnd.get(lineStyleIdx - 1);
                    } catch (Exception ex) {
                    }
                    if (lineStyle != null) {
                        String scaleMode = "NORMAL";
                        boolean pixelHintingFlag = false;
                        int startCapStyle = LINESTYLE2.ROUND_CAP;
                        int endCapStyle = LINESTYLE2.ROUND_CAP;
                        int joinStyle = LINESTYLE2.ROUND_JOIN;
                        float miterLimitFactor = 3f;
                        boolean hasFillFlag = false;
                        if (lineStyle instanceof LINESTYLE2) {
                            LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;
                            if (lineStyle2.noHScaleFlag && lineStyle2.noVScaleFlag) {
                                scaleMode = "NONE";
                            } else if (lineStyle2.noHScaleFlag) {
                                scaleMode = "VERTICAL";
                            } else if (lineStyle2.noVScaleFlag) {
                                scaleMode = "HORIZONTAL";
                            }
                            pixelHintingFlag = lineStyle2.pixelHintingFlag;
                            startCapStyle = lineStyle2.startCapStyle;
                            endCapStyle = lineStyle2.endCapStyle;
                            joinStyle = lineStyle2.joinStyle;
                            miterLimitFactor = lineStyle2.miterLimitFactor;
                            hasFillFlag = lineStyle2.hasFillFlag;
                        }
                        lineStyle(
                                lineStyle.width,
                                lineStyleEnd.width,
                                colorTransform == null ? lineStyle.color : colorTransform.apply(lineStyle.color),
                                colorTransform == null ? lineStyleEnd.color : colorTransform.apply(lineStyleEnd.color),
                                pixelHintingFlag,
                                scaleMode,
                                startCapStyle,
                                endCapStyle,
                                joinStyle,
                                miterLimitFactor);

                        if (hasFillFlag) {
                            LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;
                            FILLSTYLE fillStyle = lineStyle2.fillType;
                            LINESTYLE2 lineStyle2End = (LINESTYLE2) lineStyleEnd;
                            FILLSTYLE fillStyleEnd = lineStyle2End.fillType;
                            switch (fillStyle.fillStyleType) {
                                case FILLSTYLE.LINEAR_GRADIENT:
                                case FILLSTYLE.RADIAL_GRADIENT:
                                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                                    // Gradient fill
                                    Matrix matrix = new Matrix(fillStyle.gradientMatrix);
                                    Matrix matrixEnd = new Matrix(fillStyleEnd.gradientMatrix);
                                    lineGradientStyle(
                                            fillStyle.fillStyleType,
                                            fillStyle.gradient.gradientRecords,
                                            fillStyleEnd.gradient.gradientRecords,
                                            matrix,
                                            matrixEnd,
                                            fillStyle.gradient.spreadMode,
                                            fillStyle.gradient.interpolationMode,
                                            (fillStyle.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyle.gradient).focalPoint : 0,
                                            (fillStyleEnd.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyleEnd.gradient).focalPoint : 0
                                    );
                                    break;
                            }
                        }
                    } else {
                        // We should never get here
                        lineStyle(1, 1, new RGB(Color.black), new RGB(Color.BLACK), false, "NORMAL", 0, 0, 0, 3);
                    }
                }
                if (posX != e.getFromX() || posY != e.getFromY()) {
                    moveTo(e.getFromX(), e.getFromY(), e.getFromEndX(), e.getFromEndY());
                }
                if (e instanceof CurvedMorphEdge) {
                    CurvedMorphEdge c = (CurvedMorphEdge) e;
                    curveTo(c.getControlX(), c.getControlY(), c.toX, c.toY, c.getControlEndX(), c.getControlEndY(), c.toEndX, c.toEndY);
                } else {
                    lineTo(e.getToX(), e.getToY(), e.getToEndX(), e.getToEndY());
                }
                posX = e.getToX();
                posY = e.getToY();
            }
            endLines();
        }
    }

    protected List<IMorphEdge> createPathFromEdgeMap(Map<Integer, List<IMorphEdge>> edgeMap) {
        List<IMorphEdge> newPath = new ArrayList<>();
        List<Integer> styleIdxArray = new ArrayList<>();
        for (Integer styleIdx : edgeMap.keySet()) {
            styleIdxArray.add(styleIdx);
        }
        Collections.sort(styleIdxArray);
        for (int i = 0; i < styleIdxArray.size(); i++) {
            appendEdges(newPath, edgeMap.get(styleIdxArray.get(i)));
        }
        return newPath;
    }

    protected void cleanEdgeMap(Map<Integer, List<IMorphEdge>> edgeMap) {
        for (Integer styleIdx : edgeMap.keySet()) {
            List<IMorphEdge> subPath = edgeMap.get(styleIdx);
            if (subPath != null && subPath.size() > 0) {
                int idx;
                IMorphEdge prevEdge = null;
                List<IMorphEdge> tmpPath = new ArrayList<>();
                Map<Long, List<IMorphEdge>> coordMap = createCoordMap(subPath);
                while (subPath.size() > 0) {
                    idx = 0;
                    while (idx < subPath.size()) {
                        if (prevEdge != null) {
                            IMorphEdge subPathEdge = subPath.get(idx);
                            if (prevEdge.getToX() != subPathEdge.getFromX() || prevEdge.getToY() != subPathEdge.getFromY()) {
                                IMorphEdge edge = findNextEdgeInCoordMap(coordMap, prevEdge);
                                if (edge != null) {
                                    idx = subPath.indexOf(edge);
                                } else {
                                    idx = 0;
                                    prevEdge = null;
                                }
                                continue;
                            }
                        }

                        IMorphEdge edge = subPath.remove(idx);
                        tmpPath.add(edge);
                        removeEdgeFromCoordMap(coordMap, edge);
                        prevEdge = edge;
                    }
                }
                edgeMap.put(styleIdx, tmpPath);
            }
        }
    }

    protected Map<Long, List<IMorphEdge>> createCoordMap(List<IMorphEdge> path) {
        Map<Long, List<IMorphEdge>> coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            IMorphEdge edge = path.get(i);
            long fromLong = (((long) edge.getFromX()) << 32) | (edge.getFromY() & 0xffffffffL);
            List<IMorphEdge> coordMapArray = coordMap.get(fromLong);
            if (coordMapArray == null) {
                List<IMorphEdge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(fromLong, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
        return coordMap;
    }

    protected void removeEdgeFromCoordMap(Map<Long, List<IMorphEdge>> coordMap, IMorphEdge edge) {
        long fromLong = (((long) edge.getFromX()) << 32) | (edge.getFromY() & 0xffffffffL);
        List<IMorphEdge> coordMapArray = coordMap.get(fromLong);
        if (coordMapArray != null) {
            if (coordMapArray.size() == 1) {
                coordMap.remove(fromLong);
            } else {
                int i = coordMapArray.indexOf(edge);
                if (i > -1) {
                    coordMapArray.remove(i);
                }
            }
        }
    }

    protected IMorphEdge findNextEdgeInCoordMap(Map<Long, List<IMorphEdge>> coordMap, IMorphEdge edge) {
        long toLong = (((long) edge.getToX()) << 32) | (edge.getToY() & 0xffffffffL);
        List<IMorphEdge> coordMapArray = coordMap.get(toLong);
        if (coordMapArray != null && coordMapArray.size() > 0) {
            return coordMapArray.get(0);
        }
        return null;
    }

    protected void appendFillStyles(List<FILLSTYLE> v1, FILLSTYLE[] v2) {
        v1.addAll(Arrays.asList(v2));
    }

    protected void appendLineStyles(List<LINESTYLE> v1, LINESTYLE[] v2) {
        v1.addAll(Arrays.asList(v2));
    }

    protected void appendEdges(List<IMorphEdge> v1, List<IMorphEdge> v2) {
        for (int i = 0; i < v2.size(); i++) {
            v1.add(v2.get(i));
        }
    }
}
