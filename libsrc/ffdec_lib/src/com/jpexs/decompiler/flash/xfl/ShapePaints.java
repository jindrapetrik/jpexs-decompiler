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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Keeps independent SWF paints separate in the planar XFL shape model. */
final class ShapePaints {

    private ShapePaints() {
    }

    // Keep each SWF style group on its own plane. Rebase style references
    // before splitting, since a group can still use a style from an earlier bank.
    static List<SHAPEWITHSTYLE> splitLayers(SHAPEWITHSTYLE source, int shapeNum) {
        List<SHAPEWITHSTYLE> layers = new ArrayList<>();
        List<FILLSTYLE> fills = new ArrayList<>(Arrays.asList(source.fillStyles.fillStyles));
        List<LINESTYLE> lines = new ArrayList<>();
        List<LINESTYLE2> lines2 = new ArrayList<>();
        if (shapeNum <= 3) {
            lines.addAll(Arrays.asList(source.lineStyles.lineStyles));
        } else {
            lines2.addAll(Arrays.asList(source.lineStyles.lineStyles2));
        }
        int fillOffset = 0;
        int lineOffset = 0;
        int fill0 = 0;
        int fill1 = 0;
        int line = 0;
        int x = 0;
        int y = 0;
        boolean hasEdges = false;
        SHAPEWITHSTYLE layer = layerAt(x, y, fill0, fill1, line);
        for (SHAPERECORD record : source.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record.clone();
                if (change.stateNewStyles) {
                    if (hasEdges) {
                        layer.shapeRecords.add(new EndShapeRecord());
                        layers.add(layer);
                    }
                    layer = layerAt(x, y, fill0, fill1, line);
                    hasEdges = false;
                    fillOffset = fills.size();
                    lineOffset = shapeNum <= 3 ? lines.size() : lines2.size();
                    fills.addAll(Arrays.asList(change.fillStyles.fillStyles));
                    if (shapeNum <= 3) {
                        lines.addAll(Arrays.asList(change.lineStyles.lineStyles));
                    } else {
                        lines2.addAll(Arrays.asList(change.lineStyles.lineStyles2));
                    }
                    change.stateNewStyles = false;
                }
                if (change.stateFillStyle0) {
                    fill0 = change.fillStyle0 = change.fillStyle0 == 0 ? 0 : change.fillStyle0 + fillOffset;
                }
                if (change.stateFillStyle1) {
                    fill1 = change.fillStyle1 = change.fillStyle1 == 0 ? 0 : change.fillStyle1 + fillOffset;
                }
                if (change.stateLineStyle) {
                    line = change.lineStyle = change.lineStyle == 0 ? 0 : change.lineStyle + lineOffset;
                }
                layer.shapeRecords.add(change);
            } else if (!(record instanceof EndShapeRecord)) {
                layer.shapeRecords.add(record);
                hasEdges = true;
            }
            x = record.changeX(x);
            y = record.changeY(y);
        }
        if (hasEdges) {
            layer.shapeRecords.add(new EndShapeRecord());
            layers.add(layer);
        }
        for (SHAPEWITHSTYLE part : layers) {
            compactStyles(part, fills, lines, lines2, shapeNum);
        }
        return layers;
    }

    private static SHAPEWITHSTYLE layerAt(int x, int y, int fill0, int fill1, int line) {
        SHAPEWITHSTYLE layer = empty();
        layer.shapeRecords = new ArrayList<>();
        StyleChangeRecord start = new StyleChangeRecord();
        start.stateMoveTo = true;
        start.moveDeltaX = x;
        start.moveDeltaY = y;
        start.stateFillStyle0 = true;
        start.fillStyle0 = fill0;
        start.stateFillStyle1 = true;
        start.fillStyle1 = fill1;
        start.stateLineStyle = true;
        start.lineStyle = line;
        layer.shapeRecords.add(start);
        return layer;
    }

    private static void compactStyles(SHAPEWITHSTYLE layer, List<FILLSTYLE> fills,
            List<LINESTYLE> lines, List<LINESTYLE2> lines2, int shapeNum) {
        BitSet usedFills = new BitSet();
        BitSet usedLines = new BitSet();
        int fill0 = 0;
        int fill1 = 0;
        int line = 0;
        for (SHAPERECORD record : layer.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                if (change.stateFillStyle0) {
                    fill0 = change.fillStyle0;
                }
                if (change.stateFillStyle1) {
                    fill1 = change.fillStyle1;
                }
                if (change.stateLineStyle) {
                    line = change.lineStyle;
                }
            } else if (record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord) {
                usedFills.set(fill0);
                usedFills.set(fill1);
                usedLines.set(line);
            }
        }
        int[] fillMap = new int[fills.size() + 1];
        List<FILLSTYLE> localFills = new ArrayList<>();
        for (int i = usedFills.nextSetBit(1); i >= 0; i = usedFills.nextSetBit(i + 1)) {
            localFills.add(fills.get(i - 1));
            fillMap[i] = localFills.size();
        }
        int[] lineMap = new int[(shapeNum <= 3 ? lines.size() : lines2.size()) + 1];
        List<LINESTYLE> localLines = new ArrayList<>();
        List<LINESTYLE2> localLines2 = new ArrayList<>();
        for (int i = usedLines.nextSetBit(1); i >= 0; i = usedLines.nextSetBit(i + 1)) {
            if (shapeNum <= 3) {
                localLines.add(lines.get(i - 1));
                lineMap[i] = localLines.size();
            } else {
                localLines2.add(lines2.get(i - 1));
                lineMap[i] = localLines2.size();
            }
        }
        layer.fillStyles.fillStyles = localFills.toArray(new FILLSTYLE[0]);
        layer.lineStyles.lineStyles = localLines.toArray(new LINESTYLE[0]);
        layer.lineStyles.lineStyles2 = localLines2.toArray(new LINESTYLE2[0]);
        for (SHAPERECORD record : layer.shapeRecords) {
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                if (change.stateFillStyle0) {
                    change.fillStyle0 = fillMap[change.fillStyle0];
                }
                if (change.stateFillStyle1) {
                    change.fillStyle1 = fillMap[change.fillStyle1];
                }
                if (change.stateLineStyle) {
                    change.lineStyle = lineMap[change.lineStyle];
                }
            }
        }
    }

    // Coincident boundaries can carry different pairs of fills in SWF. FLA editor
    // merges them in a single DOMShape and can discard an adjacent region.
    static boolean hasCoincidentFillEdges(SHAPEWITHSTYLE shape) {
        Set<BezierEdge> edges = new HashSet<>();
        int x = 0;
        int y = 0;
        int fill0 = 0;
        int fill1 = 0;
        for (SHAPERECORD record : shape.shapeRecords) {
            BezierEdge edge = null;
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                if (change.stateNewStyles) {
                    edges.clear();
                }
                if (change.stateFillStyle0) {
                    fill0 = change.fillStyle0;
                }
                if (change.stateFillStyle1) {
                    fill1 = change.fillStyle1;
                }
            } else if (fill0 != fill1) {
                if (record instanceof StraightEdgeRecord) {
                    edge = new BezierEdge(x, y, record.changeX(x), record.changeY(y));
                } else if (record instanceof CurvedEdgeRecord) {
                    CurvedEdgeRecord curve = (CurvedEdgeRecord) record;
                    edge = new BezierEdge(x, y, x + curve.controlDeltaX, y + curve.controlDeltaY,
                            record.changeX(x), record.changeY(y));
                    // Compare monotone collinear curves with straight boundaries.
                    edge.shrinkToLine();
                }
            }
            x = record.changeX(x);
            y = record.changeY(y);
            if (edge != null && !edge.isEmpty()) {
                if (edges.contains(edge) || edges.contains(edge.reverse())) {
                    return true;
                }
                edges.add(edge);
            }
        }
        return false;
    }

    static boolean hasOverlappingFills(MorphShapeTag morph) {
        if (morph.getFillStyles().fillStyles.length < 2) {
            return false;
        }
        int shapeNum = morph.getShapeNum() == 1 ? 3 : 4;
        for (int ratio : new int[]{0, 32768, 65535}) {
            SHAPEWITHSTYLE shape = morph.getShapeAtRatio(ratio);
            if (hasOverlappingFills(morph.getSwf(), shapeNum, shape, ShapeTag.WIND_EVEN_ODD)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasOverlappingFills(SWF swf, int shapeNum, SHAPEWITHSTYLE shape, int windingRule) {
        if (shape.fillStyles.fillStyles.length < 2) {
            return false;
        }
        List<Area> areas = new ArrayList<>();
        for (int i = 0; i < shape.fillStyles.fillStyles.length; i++) {
            Area area = new Area(PathExporter.exportFillStyle(windingRule, shapeNum, swf, shape, i + 1));
            if (area.isEmpty()) {
                continue;
            }
            for (Area previous : areas) {
                if (!previous.getBounds2D().intersects(area.getBounds2D())) {
                    continue;
                }
                Area intersection = new Area(previous);
                intersection.intersect(area);
                if (!intersection.isEmpty()) {
                    return true;
                }
            }
            areas.add(area);
        }
        return false;
    }

    // SWF paints fills in style order, followed by strokes. Each returned
    // shape has its own plane so an overlapping fill cannot erase another.
    static List<SHAPEWITHSTYLE> paints(SWF swf, int shapeNum, SHAPEWITHSTYLE shape) {
        return paints(swf, shapeNum, shape, ShapeTag.WIND_EVEN_ODD);
    }

    static List<SHAPEWITHSTYLE> paints(SWF swf, int shapeNum, SHAPEWITHSTYLE shape, int windingRule) {
        List<SHAPEWITHSTYLE> result = new ArrayList<>();
        for (int i = 0; i < shape.fillStyles.fillStyles.length; i++) {
            SHAPEWITHSTYLE paint = empty();
            paint.fillStyles.fillStyles = new FILLSTYLE[]{shape.fillStyles.fillStyles[i]};
            // Area resolves SWF's fill rule into closed contours, including holes.
            // Its outer contours are counterclockwise, so XFL fillStyle0 is the inside.
            paint.shapeRecords = records(new Area(PathExporter.exportFillStyle(windingRule, shapeNum, swf, shape, i + 1)), true);
            result.add(paint);
        }
        int lineCount = shapeNum <= 3 ? shape.lineStyles.lineStyles.length : shape.lineStyles.lineStyles2.length;
        for (int i = 0; i < lineCount; i++) {
            SHAPEWITHSTYLE paint = empty();
            if (shapeNum <= 3) {
                paint.lineStyles.lineStyles = new LINESTYLE[]{shape.lineStyles.lineStyles[i]};
            } else {
                paint.lineStyles.lineStyles2 = new LINESTYLE2[]{shape.lineStyles.lineStyles2[i]};
            }
            paint.shapeRecords = records(PathExporter.exportLineStyle(windingRule, shapeNum, swf, shape, i + 1), false);
            result.add(paint);
        }
        return result;
    }

    private static SHAPEWITHSTYLE empty() {
        SHAPEWITHSTYLE result = new SHAPEWITHSTYLE();
        result.fillStyles = new FILLSTYLEARRAY();
        result.fillStyles.fillStyles = new FILLSTYLE[0];
        result.lineStyles = new LINESTYLEARRAY();
        result.lineStyles.lineStyles = new LINESTYLE[0];
        result.lineStyles.lineStyles2 = new LINESTYLE2[0];
        return result;
    }

    private static List<SHAPERECORD> records(Shape path, boolean fill) {
        List<SHAPERECORD> result = new ArrayList<>();
        int x = 0;
        int y = 0;
        int startX = 0;
        int startY = 0;
        boolean hasPath = false;
        double[] p = new double[6];
        for (PathIterator it = path.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(p);
            if (type == PathIterator.SEG_MOVETO) {
                if (fill && hasPath) {
                    close(result, x, y, startX, startY);
                }
                StyleChangeRecord move = new StyleChangeRecord();
                move.stateMoveTo = true;
                move.moveDeltaX = x = (int) Math.round(p[0]);
                move.moveDeltaY = y = (int) Math.round(p[1]);
                startX = x;
                startY = y;
                hasPath = true;
                move.stateFillStyle0 = fill;
                move.fillStyle0 = fill ? 1 : 0;
                move.stateLineStyle = !fill;
                move.lineStyle = fill ? 0 : 1;
                result.add(move);
            } else if (type == PathIterator.SEG_LINETO) {
                StraightEdgeRecord edge = new StraightEdgeRecord();
                edge.generalLineFlag = true;
                edge.deltaX = (int) Math.round(p[0]) - x;
                edge.deltaY = (int) Math.round(p[1]) - y;
                // Rounding a path to twips can collapse a tiny segment.
                // Flash 4 can crash when selecting/deselecting such an empty edge.
                if (edge.deltaX != 0 || edge.deltaY != 0) {
                    result.add(edge);
                }
                x += edge.deltaX;
                y += edge.deltaY;
            } else if (type == PathIterator.SEG_QUADTO) {
                CurvedEdgeRecord edge = new CurvedEdgeRecord();
                int cx = (int) Math.round(p[0]);
                int cy = (int) Math.round(p[1]);
                edge.controlDeltaX = cx - x;
                edge.controlDeltaY = cy - y;
                x = (int) Math.round(p[2]);
                y = (int) Math.round(p[3]);
                edge.anchorDeltaX = x - cx;
                edge.anchorDeltaY = y - cy;
                if (edge.controlDeltaX != 0 || edge.controlDeltaY != 0
                        || edge.anchorDeltaX != 0 || edge.anchorDeltaY != 0) {
                    result.add(edge);
                }
            } else if (type == PathIterator.SEG_CLOSE) {
                close(result, x, y, startX, startY);
                x = startX;
                y = startY;
            } else {
                throw new IllegalArgumentException("Unexpected SWF path segment " + type);
            }
        }
        if (fill && hasPath) {
            close(result, x, y, startX, startY);
        }
        result.add(new EndShapeRecord());
        return result;
    }

    private static void close(List<SHAPERECORD> records, int x, int y, int startX, int startY) {
        if (x != startX || y != startY) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = startX - x;
            edge.deltaY = startY - y;
            records.add(edge);
        }
    }

}
