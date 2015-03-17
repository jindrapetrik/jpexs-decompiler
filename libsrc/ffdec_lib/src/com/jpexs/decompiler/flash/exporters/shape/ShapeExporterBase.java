/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.PointInt;
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
import com.jpexs.helpers.Cache;
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
public abstract class ShapeExporterBase implements IShapeExporter {

    protected final SHAPE shape;

    private final List<FILLSTYLE> _fillStyles;

    private final List<LINESTYLE> _lineStyles;

    private final List<List<IEdge>> _fillPaths;

    private final List<List<IEdge>> _linePaths;

    private final ColorTransform colorTransform;

    private static final Cache<SHAPE, ShapeExportData> exportDataCache = Cache.getInstance(true, true, "shapeExportDataCache");

    public ShapeExporterBase(SHAPE shape, ColorTransform colorTransform) {
        this.shape = shape;
        this.colorTransform = colorTransform;

        ShapeExportData cachedData = exportDataCache.get(shape);
        if (cachedData == null) {
            List<FILLSTYLE> fillStyles = new ArrayList<>();
            List<LINESTYLE> lineStyles = new ArrayList<>();
            if (shape instanceof SHAPEWITHSTYLE) {
                SHAPEWITHSTYLE shapeWithStyle = (SHAPEWITHSTYLE) shape;
                fillStyles.addAll(Arrays.asList(shapeWithStyle.fillStyles.fillStyles));
                lineStyles.addAll(Arrays.asList(shapeWithStyle.lineStyles.lineStyles));
            }

            // Create edge maps
            List<Map<Integer, List<IEdge>>> fillEdgeMaps = new ArrayList<>();
            List<Map<Integer, List<IEdge>>> lineEdgeMaps = new ArrayList<>();
            createEdgeMaps(shape, fillStyles, lineStyles, fillEdgeMaps, lineEdgeMaps);
            int count = lineEdgeMaps.size();
            List<List<IEdge>> fillPaths = new ArrayList<>(count);
            List<List<IEdge>> linePaths = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                fillPaths.add(createPathFromEdgeMap(fillEdgeMaps.get(i)));
                linePaths.add(createPathFromEdgeMap(lineEdgeMaps.get(i)));
            }

            cachedData = new ShapeExportData();
            cachedData.fillPaths = fillPaths;
            cachedData.linePaths = linePaths;
            cachedData.fillStyles = fillStyles;
            cachedData.lineStyles = lineStyles;
            exportDataCache.put(shape, cachedData);
        }

        _fillStyles = cachedData.fillStyles;
        _lineStyles = cachedData.lineStyles;
        _fillPaths = cachedData.fillPaths;
        _linePaths = cachedData.linePaths;
    }

    public void export() {
        // Let the doc handler know that a shape export starts
        beginShape();
        // Export fills and strokes for each group separately
        for (int i = 0; i < _linePaths.size(); i++) {
            // Export fills first
            exportFillPath(_fillPaths.get(i));
            // Export strokes last
            exportLinePath(_linePaths.get(i));
        }
        // Let the doc handler know that we're done exporting a shape
        endShape();
    }

    private void createEdgeMaps(SHAPE shape, List<FILLSTYLE> fillStyles, List<LINESTYLE> lineStyles,
            List<Map<Integer, List<IEdge>>> fillEdgeMaps, List<Map<Integer, List<IEdge>>> lineEdgeMaps) {
        int xPos = 0;
        int yPos = 0;
        int fillStyleIdxOffset = 0;
        int lineStyleIdxOffset = 0;
        int currentFillStyleIdx0 = 0;
        int currentFillStyleIdx1 = 0;
        int currentLineStyleIdx = 0;
        List<IEdge> subPath = new ArrayList<>();
        Map<Integer, List<IEdge>> currentFillEdgeMap = new HashMap<>();
        Map<Integer, List<IEdge>> currentLineEdgeMap = new HashMap<>();
        List<SHAPERECORD> records = shape.shapeRecords;
        for (int i = 0; i < records.size(); i++) {
            SHAPERECORD shapeRecord = records.get(i);
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
                }
                // Check if all styles are reset to 0.
                // This (probably) means that a new group starts with the next record
                if (styleChangeRecord.stateLineStyle && styleChangeRecord.lineStyle == 0
                        && styleChangeRecord.stateFillStyle0 && styleChangeRecord.fillStyle0 == 0
                        && styleChangeRecord.stateFillStyle1 && styleChangeRecord.fillStyle1 == 0) {
                    cleanEdgeMap(currentFillEdgeMap);
                    cleanEdgeMap(currentLineEdgeMap);
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
            } else if (shapeRecord instanceof StraightEdgeRecord) {
                StraightEdgeRecord straightEdgeRecord = (StraightEdgeRecord) shapeRecord;
                PointInt from = new PointInt(xPos, yPos);
                if (straightEdgeRecord.generalLineFlag) {
                    xPos += straightEdgeRecord.deltaX;
                    yPos += straightEdgeRecord.deltaY;
                } else {
                    if (straightEdgeRecord.vertLineFlag) {
                        yPos += straightEdgeRecord.deltaY;
                    } else {
                        xPos += straightEdgeRecord.deltaX;
                    }
                }
                PointInt to = new PointInt(xPos, yPos);
                subPath.add(new StraightEdge(from, to, currentLineStyleIdx, currentFillStyleIdx1));
            } else if (shapeRecord instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord curvedEdgeRecord = (CurvedEdgeRecord) shapeRecord;
                PointInt from = new PointInt(xPos, yPos);
                int xPosControl = xPos + curvedEdgeRecord.controlDeltaX;
                int yPosControl = yPos + curvedEdgeRecord.controlDeltaY;
                xPos = xPosControl + curvedEdgeRecord.anchorDeltaX;
                yPos = yPosControl + curvedEdgeRecord.anchorDeltaY;
                PointInt control = new PointInt(xPosControl, yPosControl);
                PointInt to = new PointInt(xPos, yPos);
                subPath.add(new CurvedEdge(from, control, to, currentLineStyleIdx, currentFillStyleIdx1));
            } else if (shapeRecord instanceof EndShapeRecord) {
                // We're done. Process the last subpath, if any
                processSubPath(subPath, currentLineStyleIdx, currentFillStyleIdx0, currentFillStyleIdx1, currentFillEdgeMap, currentLineEdgeMap);
                cleanEdgeMap(currentFillEdgeMap);
                cleanEdgeMap(currentLineEdgeMap);
                fillEdgeMaps.add(currentFillEdgeMap);
                lineEdgeMaps.add(currentLineEdgeMap);
            }
        }
    }

    private void processSubPath(List<IEdge> subPath, int lineStyleIdx, int fillStyleIdx0, int fillStyleIdx1,
            Map<Integer, List<IEdge>> currentFillEdgeMap, Map<Integer, List<IEdge>> currentLineEdgeMap) {
        List<IEdge> path;
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

    private void exportFillPath(List<IEdge> path) {
        PointInt pos = PointInt.MAX;
        int fillStyleIdx = Integer.MAX_VALUE;
        if (path.size() > 0) {
            beginFills();
            for (int i = 0; i < path.size(); i++) {
                IEdge e = path.get(i);
                if (fillStyleIdx != e.getFillStyleIdx()) {
                    if (fillStyleIdx != Integer.MAX_VALUE) {
                        endFill();
                    }
                    fillStyleIdx = e.getFillStyleIdx();
                    pos = PointInt.MAX;
                    if (fillStyleIdx - 1 < _fillStyles.size()) {
                        Matrix matrix;
                        FILLSTYLE fillStyle = _fillStyles.get(fillStyleIdx - 1);
                        switch (fillStyle.fillStyleType) {
                            case FILLSTYLE.SOLID:
                                // Solid fill
                                beginFill(colorTransform.apply(fillStyle.color));
                                break;
                            case FILLSTYLE.LINEAR_GRADIENT:
                            case FILLSTYLE.RADIAL_GRADIENT:
                            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                                // Gradient fill
                                matrix = new Matrix(fillStyle.gradientMatrix);
                                beginGradientFill(
                                        fillStyle.fillStyleType,
                                        colorTransform.apply(fillStyle.gradient.gradientRecords),
                                        matrix,
                                        fillStyle.gradient.spreadMode,
                                        fillStyle.gradient.interpolationMode,
                                        (fillStyle.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyle.gradient).focalPoint : 0
                                );
                                break;
                            case FILLSTYLE.REPEATING_BITMAP:
                            case FILLSTYLE.CLIPPED_BITMAP:
                            case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                            case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                                // Bitmap fill
                                matrix = new Matrix(fillStyle.bitmapMatrix);
                                beginBitmapFill(
                                        fillStyle.bitmapId,
                                        matrix,
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP),
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.CLIPPED_BITMAP),
                                        colorTransform
                                );
                                break;
                        }
                    } else {
                        // Font shapes define no fillstyles per se, but do reference fillstyle index 1,
                        // which represents the font color. We just report null in this case.
                        beginFill(null);
                    }
                }
                if (!pos.equals(e.getFrom())) {
                    moveTo(e.getFrom().getX(), e.getFrom().getY());
                }
                if (e instanceof CurvedEdge) {
                    CurvedEdge c = (CurvedEdge) e;
                    curveTo(c.getControl().getX(), c.getControl().getY(), c.to.getX(), c.to.getY());
                } else {
                    lineTo(e.getTo().getX(), e.getTo().getY());
                }
                pos = e.getTo();
            }
            if (fillStyleIdx != Integer.MAX_VALUE) {
                endFill();
            }
            endFills();
        }
    }

    private void exportLinePath(List<IEdge> path) {
        PointInt pos = PointInt.MAX;
        int lineStyleIdx = Integer.MAX_VALUE;
        if (path.size() > 0) {
            beginLines();
            for (int i = 0; i < path.size(); i++) {
                IEdge e = path.get(i);
                if (lineStyleIdx != e.getLineStyleIdx()) {
                    lineStyleIdx = e.getLineStyleIdx();
                    pos = PointInt.MAX;
                    LINESTYLE lineStyle = null;
                    try {
                        lineStyle = _lineStyles.get(lineStyleIdx - 1);
                    } catch (Exception ex) {
                    }
                    if (lineStyle != null) {
                        String scaleMode = "NORMAL";
                        boolean pixelHintingFlag = false;
                        int startCapStyle = LINESTYLE2.ROUND_CAP;
                        int endCapStyle = LINESTYLE2.ROUND_CAP;
                        int joinStyle = LINESTYLE2.ROUND_JOIN;
                        int miterLimitFactor = 3;
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
                                colorTransform.apply(lineStyle.color),
                                pixelHintingFlag,
                                scaleMode,
                                startCapStyle,
                                endCapStyle,
                                joinStyle,
                                miterLimitFactor);

                        if (hasFillFlag) {
                            LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;
                            FILLSTYLE fillStyle = lineStyle2.fillType;
                            switch (fillStyle.fillStyleType) {
                                case FILLSTYLE.LINEAR_GRADIENT:
                                case FILLSTYLE.RADIAL_GRADIENT:
                                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                                    // Gradient fill
                                    Matrix matrix = new Matrix(fillStyle.gradientMatrix);
                                    lineGradientStyle(
                                            fillStyle.fillStyleType,
                                            fillStyle.gradient.gradientRecords,
                                            matrix,
                                            fillStyle.gradient.spreadMode,
                                            fillStyle.gradient.interpolationMode,
                                            (fillStyle.gradient instanceof FOCALGRADIENT) ? ((FOCALGRADIENT) fillStyle.gradient).focalPoint : 0
                                    );
                                    break;
                            }
                        }
                    } else {
                        // We should never get here
                        lineStyle(1, new RGB(Color.black), false, "NORMAL", 0, 0, 0, 3);
                    }
                }
                if (!e.getFrom().equals(pos)) {
                    moveTo(e.getFrom().getX(), e.getFrom().getY());
                }
                if (e instanceof CurvedEdge) {
                    CurvedEdge c = (CurvedEdge) e;
                    curveTo(c.getControl().getX(), c.getControl().getY(), c.to.getX(), c.to.getY());
                } else {
                    lineTo(e.getTo().getX(), e.getTo().getY());
                }
                pos = e.getTo();
            }
            endLines();
        }
    }

    private List<IEdge> createPathFromEdgeMap(Map<Integer, List<IEdge>> edgeMap) {
        List<IEdge> newPath = new ArrayList<>();
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

    private void cleanEdgeMap(Map<Integer, List<IEdge>> edgeMap) {
        for (Integer styleIdx : edgeMap.keySet()) {
            List<IEdge> subPath = edgeMap.get(styleIdx);
            if (subPath != null && subPath.size() > 0) {
                int idx;
                IEdge prevEdge = null;
                List<IEdge> tmpPath = new ArrayList<>();
                Map<String, List<IEdge>> coordMap = createCoordMap(subPath);
                while (subPath.size() > 0) {
                    idx = 0;
                    while (idx < subPath.size()) {
                        if (prevEdge == null || prevEdge.getTo().equals(subPath.get(idx).getFrom())) {
                            IEdge edge = subPath.remove(idx);
                            tmpPath.add(edge);
                            removeEdgeFromCoordMap(coordMap, edge);
                            prevEdge = edge;
                        } else {
                            IEdge edge = findNextEdgeInCoordMap(coordMap, prevEdge);
                            if (edge != null) {
                                idx = subPath.indexOf(edge);
                            } else {
                                idx = 0;
                                prevEdge = null;
                            }
                        }
                    }
                }
                edgeMap.put(styleIdx, tmpPath);
            }
        }
    }

    private Map<String, List<IEdge>> createCoordMap(List<IEdge> path) {
        Map<String, List<IEdge>> coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            PointInt from = path.get(i).getFrom();
            String key = from.getX() + "_" + from.getY();
            List<IEdge> coordMapArray = coordMap.get(key);
            if (coordMapArray == null) {
                List<IEdge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(key, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
        return coordMap;
    }

    private void removeEdgeFromCoordMap(Map<String, List<IEdge>> coordMap, IEdge edge) {
        String key = edge.getFrom().getX() + "_" + edge.getFrom().getY();
        List<IEdge> coordMapArray = coordMap.get(key);
        if (coordMapArray != null) {
            if (coordMapArray.size() == 1) {
                coordMap.remove(key);
            } else {
                int i = coordMapArray.indexOf(edge);
                if (i > -1) {
                    coordMapArray.remove(i);
                }
            }
        }
    }

    private IEdge findNextEdgeInCoordMap(Map<String, List<IEdge>> coordMap, IEdge edge) {
        String key = edge.getTo().getX() + "_" + edge.getTo().getY();
        List<IEdge> coordMapArray = coordMap.get(key);
        if (coordMapArray != null && coordMapArray.size() > 0) {
            return coordMapArray.get(0);
        }
        return null;
    }

    private void appendFillStyles(List<FILLSTYLE> v1, FILLSTYLE[] v2) {
        v1.addAll(Arrays.asList(v2));
    }

    private void appendLineStyles(List<LINESTYLE> v1, LINESTYLE[] v2) {
        v1.addAll(Arrays.asList(v2));
    }

    private void appendEdges(List<IEdge> v1, List<IEdge> v2) {
        for (int i = 0; i < v2.size(); i++) {
            v1.add(v2.get(i));
        }
    }

    public static void clearCache() {
        exportDataCache.clear();
    }
}
