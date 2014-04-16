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
        createEdgeMaps(_fillStyles, _lineStyles, _fillEdgeMaps, _lineEdgeMaps);
        
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
                if ((shapeRecord instanceof StyleChangeRecord && !(shapeRecordEnd instanceof StyleChangeRecord)) ||
                    (shapeRecord instanceof StraightEdgeRecord && !(shapeRecordEnd instanceof StraightEdgeRecord)) ||
                    (shapeRecord instanceof CurvedEdgeRecord && !(shapeRecordEnd instanceof CurvedEdgeRecord)) ||
                    (shapeRecord instanceof EndShapeRecord && !(shapeRecordEnd instanceof EndShapeRecord))) {
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

                    StraightEdgeRecord straightEdgeRecordEnd = (StraightEdgeRecord) shapeRecordEnd;
                    PointInt fromEnd = new PointInt(xPosEnd, yPosEnd);
                    if (straightEdgeRecordEnd.generalLineFlag) {
                        xPosEnd += straightEdgeRecordEnd.deltaX;
                        yPosEnd += straightEdgeRecordEnd.deltaY;
                    } else {
                        if (straightEdgeRecordEnd.vertLineFlag) {
                            yPosEnd += straightEdgeRecordEnd.deltaY;
                        } else {
                            xPosEnd += straightEdgeRecordEnd.deltaX;
                        }
                    }
                    PointInt toEnd = new PointInt(xPosEnd, yPosEnd);

                    subPath.add(new StraightMorphEdge(from, to, fromEnd, toEnd, currentLineStyleIdx, currentFillStyleIdx1));
                } else if (shapeRecord instanceof CurvedEdgeRecord) {
                    CurvedEdgeRecord curvedEdgeRecord = (CurvedEdgeRecord) shapeRecord;
                    PointInt from = new PointInt(xPos, yPos);
                    int xPosControl = xPos + curvedEdgeRecord.controlDeltaX;
                    int yPosControl = yPos + curvedEdgeRecord.controlDeltaY;
                    xPos = xPosControl + curvedEdgeRecord.anchorDeltaX;
                    yPos = yPosControl + curvedEdgeRecord.anchorDeltaY;
                    PointInt control = new PointInt(xPosControl, yPosControl);
                    PointInt to = new PointInt(xPos, yPos);

                    CurvedEdgeRecord curvedEdgeRecordEnd = (CurvedEdgeRecord) shapeRecordEnd;
                    PointInt fromEnd = new PointInt(xPosEnd, yPosEnd);
                    int xPosControlEnd = xPosEnd + curvedEdgeRecordEnd.controlDeltaX;
                    int yPosControlEnd = yPosEnd + curvedEdgeRecordEnd.controlDeltaY;
                    xPosEnd = xPosControlEnd + curvedEdgeRecordEnd.anchorDeltaX;
                    yPosEnd = yPosControlEnd + curvedEdgeRecordEnd.anchorDeltaY;
                    PointInt controlEnd = new PointInt(xPosControlEnd, yPosControlEnd);
                    PointInt toEnd = new PointInt(xPosEnd, yPosEnd);

                    subPath.add(new CurvedMorphEdge(from, control, to, fromEnd, controlEnd, toEnd, currentLineStyleIdx, currentFillStyleIdx1));
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
        PointInt pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
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
                    moveTo(e.getFrom().x, e.getFrom().y, e.getFromEnd().x, e.getFromEnd().y);
                }
                if (e instanceof CurvedMorphEdge) {
                    CurvedMorphEdge c = (CurvedMorphEdge) e;
                    curveTo(c.getControl().x, c.getControl().y, c.to.x, c.to.y, c.getControlEnd().x, c.getControlEnd().y, c.toEnd.x, c.toEnd.y);
                } else {
                    lineTo(e.getTo().x, e.getTo().y, e.getToEnd().x, e.getToEnd().y);
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
        List<IMorphEdge> path = createPathFromEdgeMap(_lineEdgeMaps.get(groupIndex));
        PointInt pos = new PointInt(Integer.MAX_VALUE, Integer.MAX_VALUE);
        int lineStyleIdx = Integer.MAX_VALUE;
        LINESTYLE lineStyle;
        if (path.size() > 0) {
            beginLines();
            for (int i = 0; i < path.size(); i++) {
                IMorphEdge e = path.get(i);
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
                    moveTo(e.getFrom().x, e.getFrom().y, e.getFromEnd().x, e.getFromEnd().y);
                }
                if (e instanceof CurvedMorphEdge) {
                    CurvedMorphEdge c = (CurvedMorphEdge) e;
                    curveTo(c.getControl().x, c.getControl().y, c.to.x, c.to.y, c.getControlEnd().x, c.getControlEnd().y, c.toEnd.x, c.toEnd.y);
                } else {
                    lineTo(e.getTo().x, e.getTo().y, e.getToEnd().x, e.getToEnd().y);
                }
                pos = e.getTo();
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
                Map<String, List<IMorphEdge>> coordMap = createCoordMap(subPath);
                while (subPath.size() > 0) {
                    idx = 0;
                    while (idx < subPath.size()) {
                        if (prevEdge == null || prevEdge.getTo().equals(subPath.get(idx).getFrom())) {
                            IMorphEdge edge = subPath.remove(idx);
                            tmpPath.add(edge);
                            removeEdgeFromCoordMap(coordMap, edge);
                            prevEdge = edge;
                        } else {
                            IMorphEdge edge = findNextEdgeInCoordMap(coordMap, prevEdge);
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

    protected Map<String, List<IMorphEdge>> createCoordMap(List<IMorphEdge> path) {
        Map<String, List<IMorphEdge>> coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            PointInt from = path.get(i).getFrom();
            String key = from.x + "_" + from.y;
            List<IMorphEdge> coordMapArray = coordMap.get(key);
            if (coordMapArray == null) {
                List<IMorphEdge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(key, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
        return coordMap;
    }

    protected void removeEdgeFromCoordMap(Map<String, List<IMorphEdge>> coordMap, IMorphEdge edge) {
        String key = edge.getFrom().x + "_" + edge.getFrom().y;
        List<IMorphEdge> coordMapArray = coordMap.get(key);
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

    protected IMorphEdge findNextEdgeInCoordMap(Map<String, List<IMorphEdge>> coordMap, IMorphEdge edge) {
        String key = edge.getTo().x + "_" + edge.getTo().y;
        List<IMorphEdge> coordMapArray = coordMap.get(key);
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
