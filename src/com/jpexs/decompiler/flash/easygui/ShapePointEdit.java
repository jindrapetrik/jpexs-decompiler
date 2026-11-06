/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.DisplayPoint;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Detached point edits using the same anchor/control-point order as ImagePanel.
 * Shape records and style boundaries are retained, including shared fill/stroke
 * edges and embedded style arrays.
 */
public final class ShapePointEdit {

    private final ShapeTag shape;
    private final List<DisplayPoint> originalPoints;
    private boolean topologyChanged;

    private static final class Vertex {
        private final SHAPERECORD record;
        private final int kind; // 0 move, 1 control, 2 endpoint
        private final DisplayPoint point;
        private final int startX;
        private final int startY;

        private Vertex(SHAPERECORD record, int kind, int x, int y, int startX, int startY) {
            this.record = record;
            this.kind = kind;
            this.point = new DisplayPoint(x, y, kind != 1);
            this.startX = startX;
            this.startY = startY;
        }
    }

    public ShapePointEdit(ShapeTag original) throws IOException, InterruptedException {
        this(original, original.getShapes());
    }

    ShapePointEdit(ShapeTag original, SHAPEWITHSTYLE layer) throws IOException, InterruptedException {
        shape = (ShapeTag) original.cloneTag();
        if (layer != original.getShapes()) {
            shape.shapes = Helper.deepCopy(layer);
        }
        // An omitted first move is an implicit move to (0, 0).
        StyleChangeRecord style = null;
        boolean moved = false;
        List<SHAPERECORD> records = shape.shapes.shapeRecords;
        for (int i = 0; i < records.size(); i++) {
            SHAPERECORD record = records.get(i);
            if (record instanceof StyleChangeRecord) {
                style = (StyleChangeRecord) record;
                moved |= style.stateMoveTo;
            } else if (edge(record)) {
                if (!moved) {
                    if (style == null) {
                        style = new StyleChangeRecord();
                        records.add(i, style);
                    }
                    style.stateMoveTo = true;
                    style.moveDeltaX = 0;
                    style.moveDeltaY = 0;
                    style.calculateBits();
                }
                break;
            }
        }
        originalPoints = getPoints();
    }

    private static boolean edge(SHAPERECORD record) {
        return record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord;
    }

    private List<Vertex> vertices() {
        List<Vertex> result = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (SHAPERECORD record : shape.shapes.shapeRecords) {
            if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateMoveTo) {
                StyleChangeRecord move = (StyleChangeRecord) record;
                result.add(new Vertex(record, 0, move.moveDeltaX, move.moveDeltaY, x, y));
            } else if (record instanceof StraightEdgeRecord) {
                result.add(new Vertex(record, 2, record.changeX(x), record.changeY(y), x, y));
            } else if (record instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord curve = (CurvedEdgeRecord) record;
                result.add(new Vertex(record, 1, x + curve.controlDeltaX, y + curve.controlDeltaY, x, y));
                result.add(new Vertex(record, 2, record.changeX(x), record.changeY(y), x, y));
            }
            x = record.changeX(x);
            y = record.changeY(y);
        }
        return result;
    }

    public List<DisplayPoint> getPoints() {
        List<DisplayPoint> result = new ArrayList<>();
        for (Vertex vertex : vertices()) {
            result.add(new DisplayPoint(vertex.point));
        }
        return result;
    }

    public List<Integer> getPathStarts() {
        List<Integer> result = new ArrayList<>();
        List<Vertex> points = vertices();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).kind == 0) {
                result.add(i);
            }
        }
        return result;
    }

    public void updatePoints(List<DisplayPoint> points) {
        List<Vertex> vertices = vertices();
        if (points.size() != vertices.size()) {
            throw new IllegalArgumentException("Point count changed during a point move");
        }
        int position = 0;
        int x = 0;
        int y = 0;
        for (SHAPERECORD record : shape.shapes.shapeRecords) {
            if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateMoveTo) {
                DisplayPoint point = points.get(position++);
                StyleChangeRecord move = (StyleChangeRecord) record;
                move.moveDeltaX = point.x;
                move.moveDeltaY = point.y;
                move.calculateBits();
            } else if (record instanceof StraightEdgeRecord) {
                setEdge(record, x, y, null, points.get(position++));
            } else if (record instanceof CurvedEdgeRecord) {
                DisplayPoint control = points.get(position++);
                setEdge(record, x, y, control, points.get(position++));
            }
            x = record.changeX(x);
            y = record.changeY(y);
        }
    }

    private static void setEdge(SHAPERECORD record, int x, int y, DisplayPoint control, DisplayPoint end) {
        if (record instanceof StraightEdgeRecord) {
            StraightEdgeRecord line = (StraightEdgeRecord) record;
            line.generalLineFlag = true;
            line.deltaX = end.x - x;
            line.deltaY = end.y - y;
            line.simplify();
            line.calculateBits();
        } else {
            CurvedEdgeRecord curve = (CurvedEdgeRecord) record;
            curve.controlDeltaX = control.x - x;
            curve.controlDeltaY = control.y - y;
            curve.anchorDeltaX = end.x - control.x;
            curve.anchorDeltaY = end.y - control.y;
            curve.calculateBits();
        }
    }

    private Vertex vertex(SHAPERECORD record, int kind) {
        for (Vertex vertex : vertices()) {
            if (vertex.record == record && vertex.kind == kind) {
                return vertex;
            }
        }
        return null;
    }

    private SHAPERECORD nextEdge(int index) {
        List<SHAPERECORD> records = shape.shapes.shapeRecords;
        for (int i = index + 1; i < records.size(); i++) {
            SHAPERECORD record = records.get(i);
            if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateMoveTo) {
                break;
            }
            if (edge(record)) {
                return record;
            }
        }
        return null;
    }

    public void removePoints(List<Integer> positions) {
        List<Vertex> all = vertices();
        List<Integer> descending = new ArrayList<>(positions);
        descending.sort(Collections.reverseOrder());
        Set<SHAPERECORD> removedMoves = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int position : descending) {
            if (position >= 0 && position < all.size()) {
                remove(all.get(position), removedMoves);
            }
        }
    }

    private void remove(Vertex selected, Set<SHAPERECORD> removedMoves) {
        Vertex current = vertex(selected.record, selected.kind);
        if (current == null) {
            return;
        }
        List<SHAPERECORD> records = shape.shapes.shapeRecords;
        int index = indexOf(current.record);
        if (current.kind == 1) {
            StraightEdgeRecord line = new StraightEdgeRecord();
            setEdge(line, current.startX, current.startY, null, vertex(current.record, 2).point);
            records.set(index, line);
        } else {
            StyleChangeRecord move = null;
            for (int i = index; i >= 0; i--) {
                if (records.get(i) instanceof StyleChangeRecord && ((StyleChangeRecord) records.get(i)).stateMoveTo) {
                    move = (StyleChangeRecord) records.get(i);
                    break;
                }
            }
            SHAPERECORD next = nextEdge(index);
            boolean closing = current.kind == 2 && next == null && move != null
                    && current.point.x == move.moveDeltaX && current.point.y == move.moveDeltaY;
            if (current.kind == 0 || closing) {
                if (!removedMoves.add(move)) {
                    return;
                }
                int moveIndex = indexOf(move);
                SHAPERECORD first = nextEdge(moveIndex);
                if (first == null) {
                    move.stateMoveTo = false;
                } else {
                    SHAPERECORD last = first;
                    while (nextEdge(indexOf(last)) != null) {
                        last = nextEdge(indexOf(last));
                    }
                    Vertex end = vertex(last, 2);
                    DisplayPoint start = vertex(first, 2).point;
                    if (last != first && end.point.x == move.moveDeltaX && end.point.y == move.moveDeltaY) {
                        Vertex control = vertex(last, 1);
                        setEdge(last, end.startX, end.startY, control == null ? null : control.point, start);
                    }
                    move.moveDeltaX = start.x;
                    move.moveDeltaY = start.y;
                    move.calculateBits();
                    records.remove(indexOf(first));
                }
            } else {
                if (next != null) {
                    Vertex control = vertex(next, 1);
                    setEdge(next, current.startX, current.startY, control == null ? null : control.point, vertex(next, 2).point);
                    if (next instanceof StraightEdgeRecord && ((StraightEdgeRecord) next).deltaX == 0 && ((StraightEdgeRecord) next).deltaY == 0) {
                        records.remove(indexOf(next));
                    }
                }
                records.remove(indexOf(current.record));
            }
        }
        topologyChanged = true;
    }

    public void splitEdge(int position, double t) {
        List<Vertex> all = vertices();
        if (position < 0 || position >= all.size() || t <= 0 || t >= 1) {
            return;
        }
        Vertex selected = all.get(position);
        if (!edge(selected.record)) {
            return;
        }
        Vertex end = vertex(selected.record, 2);
        DisplayPoint start = new DisplayPoint(selected.startX, selected.startY);
        SHAPERECORD second;
        if (selected.record instanceof StraightEdgeRecord) {
            DisplayPoint middle = interpolate(start, end.point, t);
            second = new StraightEdgeRecord();
            setEdge(selected.record, start.x, start.y, null, middle);
            setEdge(second, middle.x, middle.y, null, end.point);
        } else {
            DisplayPoint control = vertex(selected.record, 1).point;
            DisplayPoint left = interpolate(start, control, t);
            DisplayPoint right = interpolate(control, end.point, t);
            DisplayPoint middle = interpolate(left, right, t);
            second = new CurvedEdgeRecord();
            setEdge(selected.record, start.x, start.y, left, middle);
            setEdge(second, middle.x, middle.y, right, end.point);
        }
        List<SHAPERECORD> records = shape.shapes.shapeRecords;
        records.add(indexOf(selected.record) + 1, second);
        topologyChanged = true;
    }

    private static DisplayPoint interpolate(DisplayPoint a, DisplayPoint b, double t) {
        return new DisplayPoint((int) Math.round(a.x + (b.x - a.x) * t), (int) Math.round(a.y + (b.y - a.y) * t));
    }

    private int indexOf(SHAPERECORD target) {
        List<SHAPERECORD> records = shape.shapes.shapeRecords;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    public boolean isChanged() {
        List<DisplayPoint> points = getPoints();
        if (topologyChanged || points.size() != originalPoints.size()) {
            return true;
        }
        for (int i = 0; i < points.size(); i++) {
            DisplayPoint a = points.get(i);
            DisplayPoint b = originalPoints.get(i);
            if (a.x != b.x || a.y != b.y) {
                return true;
            }
        }
        return false;
    }

    public ShapeTag getShape() {
        shape.shapes.clearCachedOutline();
        shape.updateBounds();
        shape.setModified(true);
        return shape;
    }
}
