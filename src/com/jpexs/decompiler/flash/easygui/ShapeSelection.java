/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.exporters.shape.CurvedEdge;
import com.jpexs.decompiler.flash.exporters.shape.IEdge;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.graphics.ExtendedBasicStroke;
import com.jpexs.helpers.Helper;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Hit testing for the fills and strokes of a shape, in rendering order.
 * Coordinates and line widths are in twips.
 *
 * @author JPEXS
 */
public class ShapeSelection {

    private final List<Selection> parts = new ArrayList<>();

    private static class Paths extends PathExporter {

        Paths(ShapeTag shape) {
            super(shape.getWindingRule(), shape.getShapeNum(), shape.getSwf(), shape.getShapes(), null);
        }

        List<List<IEdge>> fills() {
            return getFillPaths();
        }

        List<List<IEdge>> lines() {
            return getLinePaths();
        }
    }

    public static class Selection {

        public final FILLSTYLE fill;
        public final ILINESTYLE line;
        public final Shape area;
        public final Shape linePath;
        public final boolean stroke;
        public final Shape geometry;
        private final int index;
        private final int edgeIndex;

        private Selection(FILLSTYLE fill, ILINESTYLE line, Shape area, Shape linePath, Shape geometry, boolean stroke, int index) {
            this(fill, line, area, linePath, geometry, stroke, index, -1);
        }

        private Selection(FILLSTYLE fill, ILINESTYLE line, Shape area, Shape linePath, Shape geometry, boolean stroke, int index, int edgeIndex) {
            this.fill = fill;
            this.line = line;
            this.area = area;
            this.linePath = linePath;
            this.geometry = geometry;
            this.stroke = stroke;
            this.index = index;
            this.edgeIndex = edgeIndex;
        }
    }

    public ShapeSelection(ShapeTag shape) {
        Paths paths = new Paths(shape);
        SHAPEWITHSTYLE styles = shape.getShapes();
        List<FILLSTYLE> fills = new ArrayList<>(Arrays.asList(styles.fillStyles.fillStyles));
        List<ILINESTYLE> lines = new ArrayList<>();
        addLineStyles(lines, styles.lineStyles, shape.getShapeNum());
        for (SHAPERECORD record : styles.shapeRecords) {
            if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateNewStyles) {
                StyleChangeRecord change = (StyleChangeRecord) record;
                fills.addAll(Arrays.asList(change.fillStyles.fillStyles));
                addLineStyles(lines, change.lineStyles, shape.getShapeNum());
            }
        }
        for (int group = 0; group < paths.fills().size(); group++) {
            for (Map.Entry<Integer, GeneralPath> entry : pathsByStyle(paths.fills().get(group), true, shape.getWindingRule()).entrySet()) {
                int index = entry.getKey() - 1;
                if (index >= 0 && index < fills.size()) {
                    Shape area = new Area(entry.getValue());
                    parts.add(new Selection(fills.get(index), null, area, null, area, false, parts.size()));
                }
            }
            for (Map.Entry<Integer, GeneralPath> entry : pathsByStyle(paths.lines().get(group), false, shape.getWindingRule()).entrySet()) {
                int index = entry.getKey() - 1;
                if (index < 0 || index >= lines.size()) {
                    continue;
                }
                ILINESTYLE line = lines.get(index);
                for (Path2D.Double path : splitPaths(entry.getValue())) {
                    if (!(line instanceof LINESTYLE2) || !((LINESTYLE2) line).noClose) {
                        Point2D start = firstPoint(path);
                        if (start.equals(path.getCurrentPoint())) {
                            path.closePath();
                        }
                    }
                    parts.add(new Selection(null, line, createStroke(line).createStrokedShape(path), path, path, true, parts.size()));
                }
            }
        }
    }

    private ShapeSelection() {
    }

    public static class Edit {

        public final ShapeSelection model;
        public final Selection selected;

        private Edit(ShapeSelection model, Selection selected) {
            this.model = model;
            this.selected = selected;
        }
    }

    public static class BatchEdit {

        public final ShapeSelection model;
        public final List<Selection> selected;
        public final boolean changed;

        private BatchEdit(ShapeSelection model, List<Selection> selected, boolean changed) {
            this.model = model;
            this.selected = selected;
            this.changed = changed;
        }
    }

    public static class RectangleSelection {

        public final ShapeSelection model;
        public final List<Selection> selected;

        private RectangleSelection(ShapeSelection model, List<Selection> selected) {
            this.model = model;
            this.selected = selected;
        }
    }

    private static class EdgeFragment {

        final Path2D.Double geometry;
        final int originalEdge;
        final boolean inside;

        EdgeFragment(Path2D.Double geometry, int originalEdge, boolean inside) {
            this.geometry = geometry;
            this.originalEdge = originalEdge;
            this.inside = inside;
        }
    }

    /**
     * Clips fill selections and subdivides stroke edges without changing the SWF.
     * Stroke fragments stay in one continuous path until their styles change.
     */
    public RectangleSelection selectRectangle(Rectangle2D rectangle, List<Selection> current, boolean additive) {
        return selectArea(rectangle, current, additive);
    }

    /**
     * Selects a polygonal area in local coordinates, including transformed screen rectangles.
     */
    public RectangleSelection selectArea(Shape selectionArea, List<Selection> current, boolean additive) {
        Area region = new Area(selectionArea);
        if (region.isEmpty()) {
            return new RectangleSelection(this, additive ? new ArrayList<>(current) : new ArrayList<>());
        }
        List<Line2D.Double> boundaries = areaBoundaries(selectionArea);
        ShapeSelection result = new ShapeSelection();
        List<Selection> selected = new ArrayList<>();
        Map<Integer, List<Selection>> previous = new LinkedHashMap<>();
        if (additive) {
            for (Selection item : current) {
                previous.computeIfAbsent(item.index, key -> new ArrayList<>()).add(item);
            }
        }
        for (Selection part : parts) {
            List<Selection> old = previous.getOrDefault(part.index, Collections.emptyList());
            if (!part.stroke) {
                Selection copy = result.addPart(part.fill, part.line, part.geometry, false);
                Area clipped = new Area(part.geometry);
                clipped.intersect(region);
                if (part.fill == null) {
                    clipped.reset();
                } else {
                    // Filled areas hidden by later fills are not editable here.
                    for (int i = part.index + 1; i < parts.size(); i++) {
                        if (parts.get(i).fill != null) {
                            clipped.subtract(new Area(parts.get(i).geometry));
                        }
                    }
                    Area visible = new Area(displaySelection(part).area);
                    visible.intersect(clipped);
                    if (visible.isEmpty()) {
                        clipped.reset();
                    }
                }
                for (Selection item : old) {
                    clipped.add(new Area(item.geometry));
                }
                if (!clipped.isEmpty()) {
                    selected.add(rebase(copy, copy, clipped, -1));
                }
                continue;
            }
            List<Path2D.Double> edges = strokeEdges(part.geometry);
            List<EdgeFragment> fragments = new ArrayList<>();
            Path2D.Double geometry = new Path2D.Double();
            int edgeIndex = 0;
            for (Path2D.Double path : splitPaths(part.geometry)) {
                boolean first = true;
                for (Path2D.Double edge : strokeEdges(path)) {
                    double[][] points = edgePoints(edge);
                    List<Double> cuts = areaCuts(points, region, boundaries);
                    for (int i = 1; i < cuts.size(); i++) {
                        double from = cuts.get(i - 1);
                        double to = cuts.get(i);
                        Path2D.Double fragment = edgePath(subcurve(points, from, to));
                        geometry.append(fragment, !first);
                        first = false;
                        double[] middle = curvePoint(points, (from + to) / 2);
                        fragments.add(new EdgeFragment(fragment, edgeIndex, containsInclusive(region, boundaries, middle)));
                    }
                    edgeIndex++;
                }
                if (isClosed(path)) {
                    geometry.closePath();
                }
            }
            Selection copy = result.addPart(null, part.line, geometry, true);
            for (int i = 0; i < fragments.size(); i++) {
                EdgeFragment fragment = fragments.get(i);
                boolean keep = fragment.inside && part.line != null;
                for (Selection item : old) {
                    if (item.edgeIndex == fragment.originalEdge || item.edgeIndex < 0
                            && samePath(item.geometry, edges.get(fragment.originalEdge))) {
                        keep = true;
                    }
                }
                if (keep) {
                    selected.add(rebase(copy, copy, fragment.geometry, i));
                }
            }
        }
        return new RectangleSelection(result, selected);
    }

    private static List<Line2D.Double> areaBoundaries(Shape area) {
        List<Line2D.Double> boundaries = new ArrayList<>();
        for (Path2D.Double edge : strokeEdges(area)) {
            boundaries.add(new Line2D.Double(firstPoint(edge), edge.getCurrentPoint()));
        }
        return boundaries;
    }

    private static boolean containsInclusive(Area area, List<Line2D.Double> boundaries, double[] point) {
        if (area.contains(point[0], point[1])) {
            return true;
        }
        for (Line2D.Double boundary : boundaries) {
            double dx = boundary.x2 - boundary.x1;
            double dy = boundary.y2 - boundary.y1;
            double length = Math.hypot(dx, dy);
            double px = point[0] - boundary.x1;
            double py = point[1] - boundary.y1;
            double along = length == 0 ? -1 : (px * dx + py * dy) / length;
            if (length > 0 && along >= -1e-7 && along <= length + 1e-7 && Math.abs(px * dy - py * dx) / length <= 1e-7) {
                return true;
            }
        }
        return false;
    }

    private static List<Double> areaCuts(double[][] points, Area area, List<Line2D.Double> boundaries) {
        List<Double> cuts = new ArrayList<>(Arrays.asList(0.0, 1.0));
        for (Line2D.Double boundary : boundaries) {
            double dx = boundary.x2 - boundary.x1;
            double dy = boundary.y2 - boundary.y1;
            double[] distances = new double[points.length];
            for (int i = 0; i < points.length; i++) {
                distances[i] = (points[i][0] - boundary.x1) * dy - (points[i][1] - boundary.y1) * dx;
            }
            double a = distances[0];
            double b = distances[1];
            double[] equation;
            if (points.length == 2) {
                equation = new double[]{a, b - a};
            } else if (points.length == 3) {
                double c = distances[2];
                equation = new double[]{a, 2 * (b - a), a - 2 * b + c};
            } else {
                double c = distances[2];
                double d = distances[3];
                equation = new double[]{a, 3 * (b - a), 3 * (a - 2 * b + c), -a + 3 * b - 3 * c + d};
            }
            double[] roots = new double[3];
            int count = equation.length == 4 ? CubicCurve2D.solveCubic(equation, roots)
                    : QuadCurve2D.solveQuadratic(Arrays.copyOf(equation, 3), roots);
            for (int i = 0; i < count; i++) {
                double t = roots[i];
                if (t > 1e-9 && t < 1 - 1e-9 && containsInclusive(area, boundaries, curvePoint(points, t))) {
                    cuts.add(t);
                }
            }
        }
        Collections.sort(cuts);
        List<Double> unique = new ArrayList<>();
        for (double cut : cuts) {
            if (unique.isEmpty() || cut - unique.get(unique.size() - 1) > 1e-9) {
                unique.add(cut);
            }
        }
        return unique;
    }

    private static double[][] edgePoints(Shape edge) {
        PathIterator iterator = edge.getPathIterator(null);
        double[] coords = new double[6];
        iterator.currentSegment(coords);
        double[] start = new double[]{coords[0], coords[1]};
        iterator.next();
        int type = iterator.currentSegment(coords);
        int count = type == PathIterator.SEG_LINETO ? 2 : type == PathIterator.SEG_QUADTO ? 3 : 4;
        double[][] points = new double[count][2];
        points[0] = start;
        for (int i = 1; i < count; i++) {
            points[i] = new double[]{coords[2 * (i - 1)], coords[2 * (i - 1) + 1]};
        }
        return points;
    }

    private static double[][] splitCurve(double[][] points, double t, boolean left) {
        double[][] work = new double[points.length][2];
        double[][] result = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            work[i] = points[i].clone();
        }
        result[left ? 0 : points.length - 1] = work[left ? 0 : points.length - 1].clone();
        for (int level = 1; level < points.length; level++) {
            for (int i = 0; i < points.length - level; i++) {
                for (int axis = 0; axis < 2; axis++) {
                    work[i][axis] += (work[i + 1][axis] - work[i][axis]) * t;
                }
            }
            result[left ? level : points.length - level - 1] = work[left ? 0 : points.length - level - 1].clone();
        }
        return result;
    }

    private static double[] curvePoint(double[][] points, double t) {
        if (t == 0 || t == 1) {
            return points[t == 0 ? 0 : points.length - 1].clone();
        }
        return splitCurve(points, t, true)[points.length - 1];
    }

    private static double[][] subcurve(double[][] points, double from, double to) {
        double[][] end = to == 1 ? points : splitCurve(points, to, true);
        double[][] result = from == 0 ? end : splitCurve(end, from / to, false);
        result[0] = curvePoint(points, from);
        result[result.length - 1] = curvePoint(points, to);
        return result;
    }

    private static Path2D.Double edgePath(double[][] points) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(points[0][0], points[0][1]);
        if (points.length == 2) {
            path.lineTo(points[1][0], points[1][1]);
        } else if (points.length == 3) {
            path.quadTo(points[1][0], points[1][1], points[2][0], points[2][1]);
        } else {
            path.curveTo(points[1][0], points[1][1], points[2][0], points[2][1], points[3][0], points[3][1]);
        }
        return path;
    }

    public static boolean sameSelection(Selection first, Selection second) {
        if (first.stroke != second.stroke || first.index != second.index) {
            return false;
        }
        return first.stroke ? samePath(first.geometry, second.geometry)
                : new Area(first.geometry).equals(new Area(second.geometry));
    }

    public static List<Selection> updateSelection(List<Selection> current, Selection clicked, boolean additive) {
        List<Selection> result = additive ? new ArrayList<>(current) : new ArrayList<>();
        if (clicked == null) {
            return result;
        }
        for (int i = 0; i < result.size(); i++) {
            if (sameSelection(result.get(i), clicked)) {
                result.remove(i);
                return result;
            }
        }
        if (additive && !clicked.stroke) {
            Area combined = new Area();
            List<Selection> overlapping = new ArrayList<>();
            for (Selection item : result) {
                if (!item.stroke && item.index == clicked.index) {
                    Area intersection = new Area(item.geometry);
                    intersection.intersect(new Area(clicked.geometry));
                    if (!intersection.isEmpty()) {
                        overlapping.add(item);
                        combined.add(new Area(item.geometry));
                    }
                }
            }
            if (!overlapping.isEmpty()) {
                Area missing = new Area(clicked.geometry);
                missing.subtract(combined);
                if (missing.isEmpty()) {
                    combined.subtract(new Area(clicked.geometry));
                } else {
                    combined.add(new Area(clicked.geometry));
                }
                result.removeAll(overlapping);
                if (!combined.isEmpty()) {
                    result.add(new Selection(clicked.fill, null, combined, null, combined, false, clicked.index));
                }
                return result;
            }
        }
        result.add(clicked);
        return result;
    }

    private static boolean samePath(Shape first, Shape second) {
        PathIterator a = first.getPathIterator(null);
        PathIterator b = second.getPathIterator(null);
        double[] ac = new double[6];
        double[] bc = new double[6];
        while (!a.isDone() && !b.isDone()) {
            Arrays.fill(ac, 0);
            Arrays.fill(bc, 0);
            if (a.currentSegment(ac) != b.currentSegment(bc) || !Arrays.equals(ac, bc)) {
                return false;
            }
            a.next();
            b.next();
        }
        return a.isDone() && b.isDone();
    }

    private static FILLSTYLE normalizedFill(FILLSTYLE fill) {
        if (fill == null) {
            return null;
        }
        FILLSTYLE result = fill.toShapeNum(4);
        result.inShape3 = true;
        if (result.gradient != null) {
            for (GRADRECORD stop : result.gradient.gradientRecords) {
                stop.inShape3 = true;
            }
        }
        return result;
    }

    private static boolean sameLine(ILINESTYLE first, ILINESTYLE second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        LINESTYLE2 a = first instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) first) : ((LINESTYLE) first).toLineStyle2();
        LINESTYLE2 b = second instanceof LINESTYLE2 ? Helper.deepCopy((LINESTYLE2) second) : ((LINESTYLE) second).toLineStyle2();
        if (a.hasFillFlag) {
            a.fillType = normalizedFill(a.fillType);
        }
        if (b.hasFillFlag) {
            b.fillType = normalizedFill(b.fillType);
        }
        return a.equals(b);
    }

    public BatchEdit changeAll(List<Selection> selected, Function<Selection, FILLSTYLE> fillChange,
            Function<Selection, ILINESTYLE> lineChange) {
        Map<Selection, FILLSTYLE> fills = new IdentityHashMap<>();
        Map<Selection, ILINESTYLE> lines = new IdentityHashMap<>();
        Map<Integer, List<Selection>> byPart = new LinkedHashMap<>();
        for (Selection item : selected) {
            byPart.computeIfAbsent(item.index, key -> new ArrayList<>()).add(item);
            if (!item.stroke && fillChange != null) {
                FILLSTYLE value = fillChange.apply(item);
                if (!Objects.equals(normalizedFill(item.fill), normalizedFill(value))) {
                    fills.put(item, value);
                }
            } else if (item.stroke && lineChange != null) {
                ILINESTYLE value = lineChange.apply(item);
                if (!sameLine(item.line, value)) {
                    lines.put(item, value);
                }
            }
        }
        if (fills.isEmpty() && lines.isEmpty()) {
            return new BatchEdit(this, new ArrayList<>(selected), false);
        }
        ShapeSelection result = new ShapeSelection();
        Map<Selection, Selection> replacements = new IdentityHashMap<>();
        for (int i = 0; i < parts.size(); i++) {
            Selection part = parts.get(i);
            List<Selection> items = byPart.getOrDefault(i, java.util.Collections.emptyList());
            boolean changedFill = items.stream().anyMatch(fills::containsKey);
            boolean changedLine = items.stream().anyMatch(lines::containsKey);
            if (changedFill) {
                Area remaining = new Area(part.geometry);
                for (Selection item : items) {
                    if (fills.containsKey(item)) {
                        remaining.subtract(new Area(item.geometry));
                    }
                }
                Selection rest = remaining.isEmpty() ? null : result.addPart(part.fill, null, remaining, false);
                for (Selection item : items) {
                    if (fills.containsKey(item)) {
                        replacements.put(item, result.addPart(fills.get(item), null, item.geometry, false));
                    } else {
                        replacements.put(item, rebase(item, rest, item.geometry, -1));
                    }
                }
            } else if (changedLine) {
                result.changeStrokeEdges(part, items, lines, replacements);
            } else {
                Selection copy = result.addPart(part.fill, part.line, part.geometry, part.stroke);
                for (Selection item : items) {
                    replacements.put(item, rebase(item, copy, item.geometry, item.edgeIndex));
                }
            }
        }
        List<Selection> next = new ArrayList<>();
        for (Selection item : selected) {
            next.add(replacements.get(item));
        }
        return new BatchEdit(result, next, true);
    }

    private static Selection rebase(Selection old, Selection part, Shape geometry, int edgeIndex) {
        Shape area = old.stroke ? (part.line == null ? new Area() : createStroke(part.line).createStrokedShape(geometry)) : new Area(geometry);
        if (old.stroke) {
            Area clipped = new Area(area);
            clipped.intersect(new Area(part.area));
            area = clipped;
        }
        return new Selection(part.fill, part.line, area, old.stroke ? geometry : null,
                geometry, old.stroke, part.index, edgeIndex);
    }

    private void changeStrokeEdges(Selection part, List<Selection> selected, Map<Selection, ILINESTYLE> changes,
            Map<Selection, Selection> replacements) {
        List<Path2D.Double> allEdges = strokeEdges(part.geometry);
        Map<Integer, Selection> selectedEdges = new LinkedHashMap<>();
        List<ILINESTYLE> styles = new ArrayList<>();
        for (int i = 0; i < allEdges.size(); i++) {
            styles.add(part.line);
        }
        for (Selection item : selected) {
            int edge = item.edgeIndex;
            if (edge < 0) {
                for (int i = 0; i < allEdges.size(); i++) {
                    if (samePath(item.geometry, allEdges.get(i))) {
                        edge = i;
                        break;
                    }
                }
            }
            if (edge < 0) {
                throw new IllegalArgumentException("Selected stroke edge is missing");
            }
            selectedEdges.put(edge, item);
            if (changes.containsKey(item)) {
                styles.set(edge, changes.get(item));
            }
        }
        int offset = 0;
        for (Path2D.Double path : splitPaths(part.geometry)) {
            int count = strokeEdges(path).size();
            int start = 0;
            boolean closed = isClosed(path);
            if (closed) {
                for (int i = 0; i < count; i++) {
                    if (!sameLine(styles.get(offset + i), styles.get(offset + (i + count - 1) % count))) {
                        start = i;
                        break;
                    }
                }
            }
            int position = 0;
            while (position < count) {
                List<Integer> run = new ArrayList<>();
                int first = offset + (start + position) % count;
                ILINESTYLE style = styles.get(first);
                while (position < count) {
                    int edge = offset + (start + position) % count;
                    if (!sameLine(style, styles.get(edge))) {
                        break;
                    }
                    run.add(edge);
                    position++;
                }
                Path2D.Double geometry = new Path2D.Double();
                for (int edge : run) {
                    geometry.append(allEdges.get(edge), true);
                }
                if (closed && run.size() == count) {
                    geometry.closePath();
                }
                Selection updated = addPart(null, style, geometry, true);
                for (int local = 0; local < run.size(); local++) {
                    int edge = run.get(local);
                    Selection old = selectedEdges.get(edge);
                    if (old != null) {
                        replacements.put(old, rebase(old, updated, allEdges.get(edge), local));
                    }
                }
            }
            offset += count;
        }
    }

    public Edit change(Selection selected, FILLSTYLE fill, ILINESTYLE line) {
        ShapeSelection result = new ShapeSelection();
        Selection changed = null;
        for (int i = 0; i < parts.size(); i++) {
            Selection part = parts.get(i);
            if (i == selected.index) {
                if (!selected.stroke) {
                    Area remaining = new Area(part.geometry);
                    remaining.subtract(new Area(selected.geometry));
                    if (!remaining.isEmpty()) {
                        result.addPart(part.fill, part.line, remaining, false);
                    }
                }
                if (selected.stroke) {
                    if (selected.edgeIndex >= 0) {
                        result.addRemainingStroke(part, selected.edgeIndex);
                    }
                    Selection addedFill = fill == null ? null : result.addPart(fill, null, new Area(selected.geometry), false);
                    Selection stroke = result.addPart(null, line, selected.geometry, true);
                    changed = addedFill == null ? stroke : addedFill;
                } else {
                    Selection filled = result.addPart(fill, null, selected.geometry, false);
                    changed = line == null ? filled : result.addPart(null, line, selected.geometry, true);
                }
            } else {
                result.addPart(part.fill, part.line, part.geometry, part.stroke);
            }
        }
        return new Edit(result, changed);
    }

    private void addRemainingStroke(Selection part, int selectedEdge) {
        int offset = 0;
        for (Path2D.Double path : splitPaths(part.geometry)) {
            List<Path2D.Double> edges = strokeEdges(path);
            int index = selectedEdge - offset;
            if (index < 0 || index >= edges.size()) {
                addPart(null, part.line, path, true);
            } else if (isClosed(path)) {
                // Keep the other edges joined, including the original closing vertex.
                Path2D.Double remaining = new Path2D.Double();
                for (int i = 1; i < edges.size(); i++) {
                    remaining.append(edges.get((index + i) % edges.size()), true);
                }
                if (remaining.getCurrentPoint() != null) {
                    addPart(null, part.line, remaining, true);
                }
            } else {
                addStrokeRun(part.line, edges.subList(0, index));
                addStrokeRun(part.line, edges.subList(index + 1, edges.size()));
            }
            offset += edges.size();
        }
    }

    private void addStrokeRun(ILINESTYLE line, List<Path2D.Double> edges) {
        if (edges.isEmpty()) {
            return;
        }
        Path2D.Double path = new Path2D.Double();
        for (Path2D.Double edge : edges) {
            path.append(edge, true);
        }
        addPart(null, line, path, true);
    }

    private Selection addPart(FILLSTYLE fill, ILINESTYLE line, Shape geometry, boolean stroke) {
        Shape area = stroke ? (line == null ? new Area() : createStroke(line).createStrokedShape(geometry)) : new Area(geometry);
        Selection part = new Selection(Helper.deepCopy(fill), Helper.deepCopy(line), area,
                stroke || line != null ? geometry : null, geometry, stroke, parts.size());
        parts.add(part);
        return part;
    }

    List<Selection> getParts() {
        return parts;
    }

    public Selection displaySelection(Selection selected) {
        if (selected.stroke) {
            return selected;
        }
        Area visible = new Area(selected.geometry);
        for (int above = selected.index + 1; above < parts.size(); above++) {
            Selection covering = parts.get(above);
            if (covering.line != null || covering.fill != null) {
                visible.subtract(new Area(covering.area));
            }
        }
        return new Selection(selected.fill, selected.line, visible, selected.linePath,
                selected.geometry, false, selected.index);
    }

    public Selection select(Point2D point, double tolerance) {
        return select(point, tolerance, new AffineTransform());
    }

    /**
     * Uses display coordinates for stroke tolerance and the nearest edge.
     */
    public Selection select(Point2D point, double tolerance, AffineTransform localToDisplay) {
        Point2D displayPoint = localToDisplay.transform(point, null);
        for (int i = parts.size() - 1; i >= 0; i--) {
            Selection part = parts.get(i);
            if (part.line != null) {
                Shape lineArea = part.stroke ? part.area : createStroke(part.line).createStrokedShape(part.geometry);
                Shape displayedLine = localToDisplay.createTransformedShape(lineArea);
                if (displayedLine.contains(displayPoint) || (tolerance > 0 && new BasicStroke((float) (2 * tolerance))
                        .createStrokedShape(displayedLine).contains(displayPoint))) {
                    List<Path2D.Double> edges = strokeEdges(part.geometry);
                    int nearest = -1;
                    double nearestDistance = Double.POSITIVE_INFINITY;
                    for (int edge = 0; edge < edges.size(); edge++) {
                        double distance = distanceToPath(localToDisplay.createTransformedShape(edges.get(edge)), displayPoint);
                        if (distance < nearestDistance) {
                            nearest = edge;
                            nearestDistance = distance;
                        }
                    }
                    if (nearest >= 0) {
                        Shape edge = edges.get(nearest);
                        Area selectedArea = new Area(createStroke(part.line).createStrokedShape(edge));
                        selectedArea.intersect(new Area(lineArea));
                        return new Selection(null, part.line, selectedArea, edge, edge, true, i, nearest);
                    }
                }
            }
            if (part.fill != null && new Area(part.geometry).contains(point)) {
                Area visible = new Area(part.geometry);
                Area editable = new Area(part.geometry);
                for (int above = i + 1; above < parts.size(); above++) {
                    Selection covering = parts.get(above);
                    if (visible.intersects(covering.area.getBounds2D())) {
                        visible.subtract(new Area(covering.area));
                    }
                    if (covering.stroke && covering.line != null) {
                        editable.subtract(new Area(new BasicStroke(0.01f).createStrokedShape(covering.linePath)));
                    } else if (covering.fill != null) {
                        editable.subtract(new Area(covering.geometry));
                    }
                }
                if (!visible.contains(point)) {
                    return null;
                }
                Shape geometry = editable.contains(point) ? connectedFillAt(editable, point) : connectedFillAt(visible, point);
                return new Selection(part.fill, part.line, connectedFillAt(visible, point), part.linePath, geometry, false, i);
            }
        }
        return null;
    }

    private static boolean isClosed(Shape path) {
        double[] coords = new double[6];
        for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            if (iterator.currentSegment(coords) == PathIterator.SEG_CLOSE) {
                return true;
            }
        }
        return false;
    }

    private static List<Path2D.Double> strokeEdges(Shape path) {
        List<Path2D.Double> edges = new ArrayList<>();
        double[] coords = new double[6];
        double x = 0;
        double y = 0;
        double startX = 0;
        double startY = 0;
        for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO) {
                x = startX = coords[0];
                y = startY = coords[1];
                continue;
            }
            Path2D.Double edge = new Path2D.Double();
            edge.moveTo(x, y);
            switch (type) {
                case PathIterator.SEG_LINETO:
                    edge.lineTo(coords[0], coords[1]);
                    x = coords[0];
                    y = coords[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    edge.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    x = coords[2];
                    y = coords[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    edge.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    x = coords[4];
                    y = coords[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    if (x == startX && y == startY) {
                        continue;
                    }
                    edge.lineTo(startX, startY);
                    x = startX;
                    y = startY;
                    break;
                default:
                    continue;
            }
            edges.add(edge);
        }
        return edges;
    }

    private static double distanceToPath(Shape path, Point2D point) {
        double distance = Double.POSITIVE_INFINITY;
        double[] coords = new double[6];
        double x = 0;
        double y = 0;
        // Flatten only for the distance calculation; the selected curve stays exact.
        for (PathIterator iterator = path.getPathIterator(null, 0.25); !iterator.isDone(); iterator.next()) {
            int type = iterator.currentSegment(coords);
            if (type == PathIterator.SEG_LINETO) {
                distance = Math.min(distance, Line2D.ptSegDist(x, y, coords[0], coords[1], point.getX(), point.getY()));
            }
            x = coords[0];
            y = coords[1];
        }
        return distance;
    }

    private static void addLineStyles(List<ILINESTYLE> lines, LINESTYLEARRAY styles, int shapeNum) {
        lines.addAll(Arrays.asList(shapeNum < 4 ? styles.lineStyles : styles.lineStyles2));
    }

    private static Map<Integer, GeneralPath> pathsByStyle(List<IEdge> edges, boolean fill, int windingRule) {
        Map<Integer, GeneralPath> paths = new LinkedHashMap<>();
        for (IEdge edge : edges) {
            int style = fill ? edge.getFillStyleIdx() : edge.getLineStyleIdx();
            GeneralPath path = paths.computeIfAbsent(style, key -> new GeneralPath(
                    windingRule == ShapeTag.WIND_EVEN_ODD ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO));
            Point2D current = path.getCurrentPoint();
            if (current == null || current.getX() != edge.getFromX() || current.getY() != edge.getFromY()) {
                path.moveTo(edge.getFromX(), edge.getFromY());
            }
            if (edge instanceof CurvedEdge) {
                CurvedEdge curve = (CurvedEdge) edge;
                path.quadTo(curve.getControlX(), curve.getControlY(), curve.getToX(), curve.getToY());
            } else {
                path.lineTo(edge.getToX(), edge.getToY());
            }
        }
        return paths;
    }

    private static Stroke createStroke(ILINESTYLE line) {
        int cap = BasicStroke.CAP_ROUND;
        int join = BasicStroke.JOIN_ROUND;
        float miter = 3;
        if (line instanceof LINESTYLE2) {
            LINESTYLE2 line2 = (LINESTYLE2) line;
            cap = line2.startCapStyle == LINESTYLE2.NO_CAP ? BasicStroke.CAP_BUTT
                    : line2.startCapStyle == LINESTYLE2.SQUARE_CAP ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_ROUND;
            join = line2.joinStyle == LINESTYLE2.BEVEL_JOIN ? BasicStroke.JOIN_BEVEL
                    : line2.joinStyle == LINESTYLE2.MITER_JOIN ? BasicStroke.JOIN_MITER : BasicStroke.JOIN_ROUND;
            miter = Math.max(1, line2.miterLimitFactor);
        }
        float width = Math.max(1, line.getWidth());
        return join == BasicStroke.JOIN_MITER
                ? new ExtendedBasicStroke(width, cap, ExtendedBasicStroke.JOIN_MITER_CLIP, miter)
                : new BasicStroke(width, cap, join);
    }

    private static Point2D firstPoint(Path2D path) {
        double[] coords = new double[6];
        path.getPathIterator(null).currentSegment(coords);
        return new Point2D.Double(coords[0], coords[1]);
    }

    private static List<Path2D.Double> splitPaths(Shape shape) {
        List<Path2D.Double> paths = new ArrayList<>();
        Path2D.Double path = null;
        double[] coords = new double[6];
        for (PathIterator iterator = shape.getPathIterator(null); !iterator.isDone(); iterator.next()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    path = new Path2D.Double();
                    paths.add(path);
                    path.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
            }
        }
        return paths;
    }

    private static boolean isInside(Area inner, Area outer) {
        Area remainder = new Area(inner);
        remainder.subtract(outer);
        return remainder.isEmpty();
    }

    private static Area connectedFillAt(Shape shape, Point2D point) {
        List<Area> contours = new ArrayList<>();
        Area selected = null;
        for (Path2D.Double path : splitPaths(shape)) {
            Area contour = new Area(path);
            contours.add(contour);
            if (contour.contains(point) && (selected == null || isInside(contour, selected))) {
                selected = contour;
            }
        }
        Area result = new Area(selected);
        // Remove holes, including any disconnected islands nested inside them.
        for (Area contour : contours) {
            if (contour != selected && isInside(contour, selected)) {
                result.subtract(contour);
            }
        }
        return result;
    }
}
