/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.exporters;

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
public abstract class ShapeExporterBase implements IShapeExporter {

    protected final SHAPE shape;

    protected List<FILLSTYLE> _fillStyles;
    protected List<LINESTYLE> _lineStyles;

    protected List<Map<Integer, List<IEdge>>> fillEdgeMaps;
    protected List<Map<Integer, List<IEdge>>> lineEdgeMaps;
    protected Map<Integer, List<IEdge>> currentFillEdgeMap;
    protected Map<Integer, List<IEdge>> currentLineEdgeMap;
    private int numGroups;
    protected Map<String, List<IEdge>> coordMap;
    private final ExportRectangle bounds = new ExportRectangle(Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);

    private boolean edgeMapsCreated;
    protected ColorTransform colorTransform;

    public ShapeExporterBase(SHAPE shape,ColorTransform colorTransform) {
        this.shape = shape;
        this.colorTransform = colorTransform;
        _fillStyles = new ArrayList<>();
        _lineStyles = new ArrayList<>();
        if (shape instanceof SHAPEWITHSTYLE) {
            SHAPEWITHSTYLE shapeWithStyle = (SHAPEWITHSTYLE) shape;
            _fillStyles.addAll(Arrays.asList(shapeWithStyle.fillStyles.fillStyles));
            _lineStyles.addAll(Arrays.asList(shapeWithStyle.lineStyles.lineStyles));
        }
    }

    public void export() {
        // Create edge maps
        createEdgeMaps();
        // Let the doc handler know that a shape export starts
        beginShape();
        // Export fills and strokes for each group separately
        for (int i = 0; i < numGroups; i++) {
            // Export fills first
            exportFillPath(i);
            // Export strokes last
            exportLinePath(i);
        }
        // Let the doc handler know that we're done exporting a shape
        endShape(bounds.xMin, bounds.yMin, bounds.xMax, bounds.yMax);
    }

    protected void createEdgeMaps() {
        if (!edgeMapsCreated) {
            int xPos = 0;
            int yPos = 0;
            int fillStyleIdxOffset = 0;
            int lineStyleIdxOffset = 0;
            int currentFillStyleIdx0 = 0;
            int currentFillStyleIdx1 = 0;
            int currentLineStyleIdx = 0;
            List<IEdge> subPath = new ArrayList<>();
            numGroups = 0;
            fillEdgeMaps = new ArrayList<>();
            lineEdgeMaps = new ArrayList<>();
            currentFillEdgeMap = new HashMap<>();
            currentLineEdgeMap = new HashMap<>();
            List<SHAPERECORD> records = shape.shapeRecords;
            for (int i = 0; i < records.size(); i++) {
                SHAPERECORD shapeRecord = records.get(i);
                if (shapeRecord instanceof StyleChangeRecord) {
                    StyleChangeRecord styleChangeRecord = (StyleChangeRecord) shapeRecord;
                    if (styleChangeRecord.stateLineStyle || styleChangeRecord.stateFillStyle0 || styleChangeRecord.stateFillStyle1) {
                        processSubPath(subPath, currentLineStyleIdx, currentFillStyleIdx0, currentFillStyleIdx1);
                        subPath = new ArrayList<>();
                    }
                    if (styleChangeRecord.stateNewStyles) {
                        fillStyleIdxOffset = _fillStyles.size();
                        lineStyleIdxOffset = _lineStyles.size();
                        appendFillStyles(_fillStyles, styleChangeRecord.fillStyles.fillStyles);
                        appendLineStyles(_lineStyles, styleChangeRecord.lineStyles.lineStyles);
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
                        numGroups++;
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
                    processSubPath(subPath, currentLineStyleIdx, currentFillStyleIdx0, currentFillStyleIdx1);
                    cleanEdgeMap(currentFillEdgeMap);
                    cleanEdgeMap(currentLineEdgeMap);
                    fillEdgeMaps.add(currentFillEdgeMap);
                    lineEdgeMaps.add(currentLineEdgeMap);
                    numGroups++;
                }
            }
            edgeMapsCreated = true;
        }
    }

    protected void processSubPath(List<IEdge> subPath, int lineStyleIdx, int fillStyleIdx0, int fillStyleIdx1) {
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

    private void calculateBound(IEdge edge) {
        PointInt from = edge.getFrom();
        PointInt to = edge.getTo();
        calculateBound(from);
        calculateBound(to);
        if (edge instanceof CurvedEdge) {
            CurvedEdge curvedEdge = (CurvedEdge) edge;
            PointInt control = curvedEdge.getControl();
            calculateBound(control);
        }
    }

    private void calculateBound(PointInt point) {
        if (point.x < bounds.xMin) {
            bounds.xMin = point.x;
        }
        if (point.x > bounds.xMax) {
            bounds.xMax = point.x;
        }
        if (point.y < bounds.yMin) {
            bounds.yMin = point.y;
        }
        if (point.y > bounds.yMax) {
            bounds.yMax = point.y;
        }
    }

    protected void exportFillPath(int groupIndex) {
        List<IEdge> path = createPathFromEdgeMap(fillEdgeMaps.get(groupIndex));
        PointInt pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
        int fillStyleIdx = Integer.MAX_VALUE;
        if (path.size() > 0) {
            beginFills();
            for (int i = 0; i < path.size(); i++) {
                IEdge e = path.get(i);
                calculateBound(e);
                if (fillStyleIdx != e.getFillStyleIdx()) {
                    if (fillStyleIdx != Integer.MAX_VALUE) {
                        endFill();
                    }
                    fillStyleIdx = e.getFillStyleIdx();
                    pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    try {
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
                    } catch (Exception ex) {
                        // Font shapes define no fillstyles per se, but do reference fillstyle index 1,
                        // which represents the font color. We just report null in this case.
                        beginFill(null);
                    }
                }
                if (!pos.equals(e.getFrom())) {
                    moveTo(e.getFrom().x, e.getFrom().y);
                }
                if (e instanceof CurvedEdge) {
                    CurvedEdge c = (CurvedEdge) e;
                    curveTo(c.getControl().x, c.getControl().y, c.to.x, c.to.y);
                } else {
                    lineTo(e.getTo().x, e.getTo().y);
                }
                pos = e.getTo();
            }
            if (fillStyleIdx != Integer.MAX_VALUE) {
                endFill();
            }
            endFills();
        }
    }

    protected void exportLinePath(int groupIndex) {
        List<IEdge> path = createPathFromEdgeMap(lineEdgeMaps.get(groupIndex));
        PointInt pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
        int lineStyleIdx = Integer.MAX_VALUE;
        LINESTYLE lineStyle;
        if (path.size() > 0) {
            beginLines();
            for (int i = 0; i < path.size(); i++) {
                IEdge e = path.get(i);
                calculateBound(e);
                if (lineStyleIdx != e.getLineStyleIdx()) {
                    lineStyleIdx = e.getLineStyleIdx();
                    pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    try {
                        lineStyle = _lineStyles.get(lineStyleIdx - 1);
                    } catch (Exception ex) {
                        lineStyle = null;
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
                                scaleMode = "HORIZONTAL";
                            } else if (lineStyle2.noVScaleFlag) {
                                scaleMode = "VERTICAL";
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
                        lineStyle(1, new RGB(Color.BLACK), false, "NORMAL", 0, 0, 0, 3);
                    }
                }
                if (!e.getFrom().equals(pos)) {
                    moveTo(e.getFrom().x, e.getFrom().y);
                }
                if (e instanceof CurvedEdge) {
                    CurvedEdge c = (CurvedEdge) e;
                    curveTo(c.getControl().x, c.getControl().y, c.to.x, c.to.y);
                } else {
                    lineTo(e.getTo().x, e.getTo().y);
                }
                pos = e.getTo();
            }
            endLines();
        }
    }

    protected List<IEdge> createPathFromEdgeMap(Map<Integer, List<IEdge>> edgeMap) {
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

    protected void cleanEdgeMap(Map<Integer, List<IEdge>> edgeMap) {
        for (Integer styleIdx : edgeMap.keySet()) {
            List<IEdge> subPath = edgeMap.get(styleIdx);
            if (subPath != null && subPath.size() > 0) {
                int idx;
                IEdge prevEdge = null;
                List<IEdge> tmpPath = new ArrayList<>();
                createCoordMap(subPath);
                while (subPath.size() > 0) {
                    idx = 0;
                    while (idx < subPath.size()) {
                        if (prevEdge == null || prevEdge.getTo().equals(subPath.get(idx).getFrom())) {
                            IEdge edge = subPath.remove(idx);
                            tmpPath.add(edge);
                            removeEdgeFromCoordMap(edge);
                            prevEdge = edge;
                        } else {
                            IEdge edge = findNextEdgeInCoordMap(prevEdge);
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

    protected void createCoordMap(List<IEdge> path) {
        coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            PointInt from = path.get(i).getFrom();
            String key = from.x + "_" + from.y;
            List<IEdge> coordMapArray = coordMap.get(key);
            if (coordMapArray == null) {
                List<IEdge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(key, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
    }

    protected void removeEdgeFromCoordMap(IEdge edge) {
        String key = edge.getFrom().x + "_" + edge.getFrom().y;
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

    protected IEdge findNextEdgeInCoordMap(IEdge edge) {
        String key = edge.getTo().x + "_" + edge.getTo().y;
        List<IEdge> coordMapArray = coordMap.get(key);
        if (coordMapArray != null && coordMapArray.size() > 0) {
            return coordMapArray.get(0);
        }
        return null;
    }

    protected void appendFillStyles(List<FILLSTYLE> v1, FILLSTYLE[] v2) {
        for (int i = 0; i < v2.length; i++) {
            v1.add(v2[i]);
        }
    }

    protected void appendLineStyles(List<LINESTYLE> v1, LINESTYLE[] v2) {
        for (int i = 0; i < v2.length; i++) {
            v1.add(v2[i]);
        }
    }

    protected void appendEdges(List<IEdge> v1, List<IEdge> v2) {
        for (int i = 0; i < v2.size(); i++) {
            v1.add(v2.get(i));
        }
    }
}
