/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
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

    private final ShapeTag tag;

    private static final double unitDivisor = 20;

    protected List<FILLSTYLE> _fillStyles;
    protected List<LINESTYLE> _lineStyles;

    protected List<Map<Integer, List<IEdge>>> fillEdgeMaps;
    protected List<Map<Integer, List<IEdge>>> lineEdgeMaps;
    protected Map<Integer, List<IEdge>> currentFillEdgeMap;
    protected Map<Integer, List<IEdge>> currentLineEdgeMap;
    private int numGroups;
    protected Map<String, List<IEdge>> coordMap;
    private Rectangle bounds = new Rectangle(Double.MAX_VALUE, Double.MAX_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);

    private boolean edgeMapsCreated;

    public ShapeExporterBase(ShapeTag tag) {
        this.tag = tag;
        _fillStyles = new ArrayList<>();
        _fillStyles.addAll(Arrays.asList(tag.getShapes().fillStyles.fillStyles));
        _lineStyles = new ArrayList<>();
        _lineStyles.addAll(Arrays.asList(tag.getShapes().lineStyles.lineStyles));
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
            double xPos = 0;
            double yPos = 0;
            Point from;
            Point to;
            Point control;
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
            List<SHAPERECORD> records = tag.getShapes().shapeRecords;
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
                        xPos = styleChangeRecord.moveDeltaX / unitDivisor;
                        yPos = styleChangeRecord.moveDeltaY / unitDivisor;
                    }
                } else if (shapeRecord instanceof StraightEdgeRecord) {
                    StraightEdgeRecord straightEdgeRecord = (StraightEdgeRecord) shapeRecord;
                    from = new Point(roundPixels400(xPos), roundPixels400(yPos));
                    if (straightEdgeRecord.generalLineFlag) {
                        xPos += straightEdgeRecord.deltaX / unitDivisor;
                        yPos += straightEdgeRecord.deltaY / unitDivisor;
                    } else {
                        if (straightEdgeRecord.vertLineFlag) {
                            yPos += straightEdgeRecord.deltaY / unitDivisor;
                        } else {
                            xPos += straightEdgeRecord.deltaX / unitDivisor;
                        }
                    }
                    to = new Point(roundPixels400(xPos), roundPixels400(yPos));
                    subPath.add(new StraightEdge(from, to, currentLineStyleIdx, currentFillStyleIdx1));
                } else if (shapeRecord instanceof CurvedEdgeRecord) {
                    CurvedEdgeRecord curvedEdgeRecord = (CurvedEdgeRecord) shapeRecord;
                    from = new Point(roundPixels400(xPos), roundPixels400(yPos));
                    double xPosControl = xPos + curvedEdgeRecord.controlDeltaX / unitDivisor;
                    double yPosControl = yPos + curvedEdgeRecord.controlDeltaY / unitDivisor;
                    xPos = xPosControl + curvedEdgeRecord.anchorDeltaX / unitDivisor;
                    yPos = yPosControl + curvedEdgeRecord.anchorDeltaY / unitDivisor;
                    control = new Point(xPosControl, yPosControl);
                    to = new Point(roundPixels400(xPos), roundPixels400(yPos));
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
        Point from = edge.getFrom();
        Point to = edge.getTo();
        if (from.x < bounds.xMin) {
            bounds.xMin = from.x;
        }
        if (from.x > bounds.xMax) {
            bounds.xMax = from.x;
        }
        if (to.y < bounds.yMin) {
            bounds.yMin = to.y;
        }
        if (to.y > bounds.yMax) {
            bounds.yMax = to.y;
        }
    }

    protected void exportFillPath(int groupIndex) {
        List<IEdge> path = createPathFromEdgeMap(fillEdgeMaps.get(groupIndex));
        Point pos = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
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
                    pos = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    try {
                        Matrix matrix;
                        FILLSTYLE fillStyle = _fillStyles.get(fillStyleIdx - 1);
                        switch (fillStyle.fillStyleType) {
                            case FILLSTYLE.SOLID:
                                // Solid fill
                                beginFill(fillStyle.color);
                                break;
                            case FILLSTYLE.LINEAR_GRADIENT:
                            case FILLSTYLE.RADIAL_GRADIENT:
                            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                                // Gradient fill
                                List<RGB> colors = new ArrayList<>();
                                List<Integer> ratios = new ArrayList<>();
                                GRADRECORD gradientRecord;
                                matrix = new Matrix(fillStyle.gradientMatrix);
                                for (int gri = 0; gri < fillStyle.gradient.gradientRecords.length; gri++) {
                                    gradientRecord = fillStyle.gradient.gradientRecords[gri];
                                    colors.add(gradientRecord.color);
                                    ratios.add(gradientRecord.ratio);
                                }
                                beginGradientFill(
                                        fillStyle.fillStyleType,
                                        colors, ratios, matrix,
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
                                //matrix = new MATRIX(m.scaleX / 20.0, m.scaleY / 20.0, m.getRotation(), m.translateX / 20.0, m.translateY / 20.0);
                                beginBitmapFill(
                                        fillStyle.bitmapId,
                                        matrix,
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP),
                                        (fillStyle.fillStyleType == FILLSTYLE.REPEATING_BITMAP || fillStyle.fillStyleType == FILLSTYLE.CLIPPED_BITMAP)
                                );
                                break;
                        }
                    } catch (Exception ex) {
                        // Font shapes define no fillstyles per se, but do reference fillstyle index 1,
                        // which represents the font color. We just report solid black in this case.
                        beginFill(new RGB(Color.BLACK));
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
        Point pos = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
        int lineStyleIdx = Integer.MAX_VALUE;
        LINESTYLE lineStyle;
        if (path.size() > 0) {
            beginLines();
            for (int i = 0; i < path.size(); i++) {
                IEdge e = path.get(i);
                calculateBound(e);
                if (lineStyleIdx != e.getLineStyleIdx()) {
                    lineStyleIdx = e.getLineStyleIdx();
                    pos = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
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
                                lineStyle.width / 20.0,
                                lineStyle.color,
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
                                    List<RGB> colors = new ArrayList<>();
                                    List<Integer> ratios = new ArrayList<>();
                                    GRADRECORD gradientRecord;
                                    Matrix matrix = new Matrix(fillStyle.gradientMatrix);
                                    for (int gri = 0; gri < fillStyle.gradient.gradientRecords.length; gri++) {
                                        gradientRecord = fillStyle.gradient.gradientRecords[gri];
                                        colors.add(gradientRecord.color);
                                        ratios.add(gradientRecord.ratio);
                                    }
                                    lineGradientStyle(
                                            fillStyle.fillStyleType,
                                            colors, ratios, matrix,
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
            Point from = path.get(i).getFrom();
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

    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }

    protected double roundPixels400(double pixels) {
        return Math.round(pixels * 10000) / 10000.0;
    }
}
