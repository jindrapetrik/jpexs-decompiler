/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Updates paint styles on the original edges in a single style bank. */
final class ShapeStyleRecords {

    private static final BasicStroke BOUNDARY = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final int MAX_STYLE_INDEX = (1 << 15) - 1;

    private static final class BoundaryEdge {

        private final int index;
        private final int fill0;
        private final int fill1;
        private final int line;
        private final boolean selectedFill;
        private final double[][] points;

        private BoundaryEdge(int index, StyleChangeRecord style, boolean selectedFill, double[][] points) {
            this.index = index;
            this.fill0 = style.fillStyle0;
            this.fill1 = style.fillStyle1;
            this.line = style.lineStyle;
            this.selectedFill = selectedFill;
            this.points = points;
        }
    }

    private ShapeStyleRecords() {
    }

    /** Returns null if the visible face cannot be represented by the original fill edges. */
    static SHAPEWITHSTYLE applyInk(SHAPEWITHSTYLE source, int fillGroup, int fillStyle, Shape region,
            Shape previousStroke, ILINESTYLE lineStyle) {
        List<BoundaryEdge> edges = boundaryEdges(source, fillGroup, fillStyle, region, true);
        Path2D oldStrokeEdges = new Path2D.Double();
        for (BoundaryEdge edge : edges) {
            if (edge.line != 0) {
                append(oldStrokeEdges, edge.points);
            }
        }
        if (!coversFill(edges, region) || !covers(oldStrokeEdges, previousStroke)) {
            return null;
        }
        LINESTYLE2[] lines = source.lineStyles.lineStyles2;
        int target = 0;
        if (lineStyle != null) {
            while (target < lines.length && !ShapeSelection.sameLine(lines[target], lineStyle)) {
                target++;
            }
            if (target == lines.length) {
                if (target >= MAX_STYLE_INDEX) {
                    return null;
                }
                lines = Arrays.copyOf(lines, target + 1);
                lines[target] = lineStyle instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) lineStyle) : ((LINESTYLE) lineStyle).toLineStyle2();
            }
            target++;
        }
        Map<Integer, StyleChangeRecord> changes = new HashMap<>();
        BitSet removed = new BitSet();
        for (BoundaryEdge edge : edges) {
            int desired = edge.selectedFill ? target : 0;
            if (desired != edge.line) {
                if (desired == 0 && edge.fill0 == 0 && edge.fill1 == 0) {
                    removed.set(edge.index);
                } else {
                    StyleChangeRecord change = new StyleChangeRecord();
                    change.stateLineStyle = true;
                    change.lineStyle = desired;
                    changes.put(edge.index, change);
                }
            }
        }
        SHAPEWITHSTYLE result = rewrite(source, changes, removed);
        if (result != source) {
            result.lineStyles.lineStyles2 = lines;
        }
        return result;
    }

    /** Changes the selected side of each edge, retaining its line and opposite fill. */
    static SHAPEWITHSTYLE applyPaint(SHAPEWITHSTYLE source, int fillGroup, int fillStyle, Shape region, FILLSTYLE fill) {
        List<BoundaryEdge> edges = boundaryEdges(source, fillGroup, fillStyle, region, false);
        if (!coversFill(edges, region)) {
            return null;
        }
        FILLSTYLE[] fills = source.fillStyles.fillStyles;
        int target = 0;
        if (fill != null) {
            FILLSTYLE normalized = ShapeSelection.normalizedFill(fill);
            // A lower fill index could put the recolored face behind an overlapping fill.
            target = fillStyle;
            while (target < fills.length && !normalized.equals(ShapeSelection.normalizedFill(fills[target]))) {
                target++;
            }
            if (target == fills.length) {
                if (target >= MAX_STYLE_INDEX) {
                    return null;
                }
                fills = Arrays.copyOf(fills, target + 1);
                fills[target] = normalized;
            }
            target++;
        }
        Map<Integer, StyleChangeRecord> changes = new HashMap<>();
        BitSet removed = new BitSet();
        for (BoundaryEdge edge : edges) {
            StyleChangeRecord change = new StyleChangeRecord();
            change.stateFillStyle0 = edge.fill0 == fillStyle;
            change.stateFillStyle1 = edge.fill1 == fillStyle;
            change.fillStyle0 = change.stateFillStyle0 ? target : edge.fill0;
            change.fillStyle1 = change.stateFillStyle1 ? target : edge.fill1;
            if (change.fillStyle0 == 0 && change.fillStyle1 == 0 && edge.line == 0) {
                removed.set(edge.index);
            } else {
                changes.put(edge.index, change);
            }
        }
        SHAPEWITHSTYLE result = rewrite(source, changes, removed);
        result.fillStyles.fillStyles = fills;
        return result;
    }

    private static List<BoundaryEdge> boundaryEdges(SHAPEWITHSTYLE source, int fillGroup, int fillStyle, Shape region, boolean includeStrokes) {
        Shape boundary = BOUNDARY.createStrokedShape(region);
        List<BoundaryEdge> edges = new ArrayList<>();
        int x = 0;
        int y = 0;
        StyleChangeRecord style = new StyleChangeRecord();
        int group = 0;
        for (int i = 0; i < source.shapeRecords.size(); i++) {
            SHAPERECORD record = source.shapeRecords.get(i);
            int endX = record.changeX(x);
            int endY = record.changeY(y);
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                applyStyles(style, change);
                if (change.stateFillStyle0 && change.stateFillStyle1 && change.stateLineStyle
                        && style.fillStyle0 == 0 && style.fillStyle1 == 0 && style.lineStyle == 0) {
                    group++;
                }
            } else if (isEdge(record)) {
                boolean selectedFill = group == fillGroup && style.fillStyle0 != style.fillStyle1
                        && (style.fillStyle0 == fillStyle || style.fillStyle1 == fillStyle);
                if (selectedFill || includeStrokes && style.lineStyle != 0) {
                    double[][] points = edgePoints(record, x, y, endX, endY);
                    if (ShapeSelection.followsBoundary(points, boundary)) {
                        edges.add(new BoundaryEdge(i, style, selectedFill, points));
                    }
                }
            }
            x = endX;
            y = endY;
        }
        return edges;
    }

    private static boolean coversFill(List<BoundaryEdge> edges, Shape region) {
        Path2D fillEdges = new Path2D.Double();
        for (BoundaryEdge edge : edges) {
            if (edge.selectedFill) {
                append(fillEdges, edge.points);
            }
        }
        return fillEdges.getCurrentPoint() != null && covers(fillEdges, region);
    }

    private static boolean covers(Shape original, Shape boundary) {
        Shape area = BOUNDARY.createStrokedShape(original);
        return ShapeSelection.strokeEdges(boundary).stream()
                .allMatch(edge -> ShapeSelection.followsBoundary(ShapeSelection.edgePoints(edge), area));
    }

    private static boolean isEdge(SHAPERECORD record) {
        return record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord;
    }

    private static double[][] edgePoints(SHAPERECORD record, int x, int y, int endX, int endY) {
        if (record instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord curve = (CurvedEdgeRecord) record;
            return new double[][]{{x, y}, {x + curve.controlDeltaX, y + curve.controlDeltaY}, {endX, endY}};
        }
        return new double[][]{{x, y}, {endX, endY}};
    }

    private static void append(Path2D path, double[][] points) {
        path.moveTo(points[0][0], points[0][1]);
        if (points.length == 2) {
            path.lineTo(points[1][0], points[1][1]);
        } else {
            path.quadTo(points[1][0], points[1][1], points[2][0], points[2][1]);
        }
    }

    private static void applyStyles(StyleChangeRecord style, StyleChangeRecord change) {
        style.fillStyle0 = change.stateFillStyle0 ? change.fillStyle0 : style.fillStyle0;
        style.fillStyle1 = change.stateFillStyle1 ? change.fillStyle1 : style.fillStyle1;
        style.lineStyle = change.stateLineStyle ? change.lineStyle : style.lineStyle;
    }

    private static SHAPEWITHSTYLE rewrite(SHAPEWITHSTYLE source, Map<Integer, StyleChangeRecord> changes, BitSet removed) {
        if (changes.isEmpty() && removed.isEmpty()) {
            return source;
        }
        SHAPEWITHSTYLE result = SHAPEWITHSTYLE.createEmpty(4);
        result.fillStyles.fillStyles = source.fillStyles.fillStyles;
        result.lineStyles.lineStyles2 = source.lineStyles.lineStyles2;
        result.shapeRecords.clear();
        int x = 0;
        int y = 0;
        int writtenX = 0;
        int writtenY = 0;
        StyleChangeRecord original = new StyleChangeRecord();
        StyleChangeRecord written = new StyleChangeRecord();
        for (int i = 0; i < source.shapeRecords.size(); i++) {
            SHAPERECORD record = source.shapeRecords.get(i);
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                applyStyles(original, change);
                applyStyles(written, change);
            } else if (isEdge(record) && !removed.get(i)) {
                StyleChangeRecord edit = changes.get(i);
                boolean move = writtenX != x || writtenY != y;
                StyleChangeRecord change = styleDifference(original, written, edit, move);
                if (change != null) {
                    change.stateMoveTo = move;
                    change.moveDeltaX = x;
                    change.moveDeltaY = y;
                    change.calculateBits();
                    result.shapeRecords.add(change);
                    applyStyles(written, change);
                    writtenX = x;
                    writtenY = y;
                }
            }
            x = record.changeX(x);
            y = record.changeY(y);
            if (removed.get(i)) {
                continue;
            }
            result.shapeRecords.add(record);
            writtenX = record.changeX(writtenX);
            writtenY = record.changeY(writtenY);
        }
        return result;
    }

    private static StyleChangeRecord styleDifference(StyleChangeRecord original, StyleChangeRecord written, StyleChangeRecord edit, boolean move) {
        int fill0 = edit != null && edit.stateFillStyle0 ? edit.fillStyle0 : original.fillStyle0;
        int fill1 = edit != null && edit.stateFillStyle1 ? edit.fillStyle1 : original.fillStyle1;
        int line = edit != null && edit.stateLineStyle ? edit.lineStyle : original.lineStyle;
        if (!move && fill0 == written.fillStyle0 && fill1 == written.fillStyle1 && line == written.lineStyle) {
            return null;
        }
        StyleChangeRecord change = new StyleChangeRecord();
        change.fillStyle0 = fill0;
        change.fillStyle1 = fill1;
        change.lineStyle = line;
        change.stateFillStyle0 = fill0 != written.fillStyle0;
        change.stateFillStyle1 = fill1 != written.fillStyle1;
        change.stateLineStyle = line != written.lineStyle;
        return change;
    }
}
