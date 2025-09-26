/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.CurvedEdge;
import com.jpexs.decompiler.flash.exporters.shape.IEdge;
import com.jpexs.decompiler.flash.exporters.shape.ShapeExporterBase;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Reference;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Switched fill sides fixer. This will fix orientation of fillstyle0 and
 * fillstyle1 to be on left and right side of the vector.
 *
 * @author JPEXS
 */
public class SwitchedFillSidesFixer {

    private static double polygonArea(List<IEdge> loop) {
        double area = 0;
        for (IEdge e : loop) {
            assert (e != null);
            if (e instanceof CurvedEdge) {
                CurvedEdge ce = (CurvedEdge) e;
                area += (e.getFromX() * ce.getControlY() - ce.getControlX() * e.getFromY());
                area += (ce.getControlX() * ce.getToY() - ce.getToX() * ce.getControlY());
                continue;
            }
            area += (e.getFromX() * e.getToY() - e.getToX() * e.getFromY());
        }
        return area / 2.0;
    }

    private BezierEdge iedgeToBezier(IEdge ie) {
        assert (ie != null);
        if (ie instanceof CurvedEdge) {
            CurvedEdge ce = (CurvedEdge) ie;
            return new BezierEdge(ce.getFromX(), ce.getFromY(),
                    ce.getControlX(), ce.getControlY(),
                    ce.getToX(), ce.getToY()
            );
        } else {
            return new BezierEdge(ie.getFromX(), ie.getFromY(), ie.getToX(), ie.getToY());
        }
    }

    private static class Polygon {

        List<IEdge> list;
        List<Polygon> children = new ArrayList<>();
        boolean ccw = false;
        GeneralPath path;
        int fillStyle;
        boolean filled = true;
        Polygon parent = null;
        Area areaObj;
        double area;
        Rectangle2D bbox;

        public Polygon(List<IEdge> list, int fillStyle) {
            this.list = list;
            /*double polyArea = polygonArea(list);
            if (polyArea < 0) {
                ccw = true;
            }*/
            path = toPath();
            //this.ccw = PathOrientation.orientationSingleClosed(path) == PathOrientation.Orientation.COUNTER_CLOCKWISE;
            Reference<PathOrientation.Orientation> orientationRef = new Reference<>(null);
            Reference<Double> areaRef = new Reference<>(0.0);
            PathOrientation.orientationSingleClosed(path, orientationRef, areaRef);
            this.ccw = orientationRef.getVal() == PathOrientation.Orientation.COUNTER_CLOCKWISE;
            this.area = areaRef.getVal();
            this.areaObj = new Area(path);
            this.bbox = this.areaObj.getBounds2D();
            this.fillStyle = fillStyle;
        }

        private GeneralPath toPath() {
            GeneralPath gp = new GeneralPath();
            int lastX = Integer.MAX_VALUE;
            int lastY = Integer.MAX_VALUE;
            for (IEdge e : list) {
                if (lastX == Integer.MAX_VALUE || lastX != e.getFromX() || lastY != e.getFromY()) {
                    gp.moveTo(e.getFromX(), e.getFromY());
                }
                if (e instanceof CurvedEdge) {
                    CurvedEdge ce = (CurvedEdge) e;
                    gp.quadTo(ce.getControlX(), ce.getControlY(), ce.getToX(), ce.getToY());
                } else {
                    gp.lineTo(e.getToX(), e.getToY());
                }
                lastX = e.getToX();
                lastY = e.getToY();
            }
            if (lastX == list.get(0).getFromX() && lastY == list.get(0).getFromY()) {
                gp.closePath();
            }
            return gp;
        }

        public boolean contains(Polygon other) {
            if (other.areaObj.isEmpty()) {
                return false;
            }
            Area diff = new Area(other.areaObj);
            diff.subtract(areaObj);
            return diff.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (IEdge e : list) {
                sb.append("M ").append(e.getFromX()).append(" ").append(e.getFromY()).append(" ");
                if (e instanceof CurvedEdge) {
                    CurvedEdge ce = (CurvedEdge) e;
                    sb.append("Q ").append(ce.getControlX()).append(" ").append(ce.getControlY()).append(" ");
                } else {
                    sb.append("L ");
                }
                sb.append(e.getToX()).append(" ").append(e.getToY()).append(" ");
            }
            return sb.toString().trim();
        }

    }

    static class GridIndex {

        // Simple uniform grid over bbox domain
        private final double cellSize;
        private final Map<Long, List<Polygon>> cells = new HashMap<>();
        private final double minX, minY;

        GridIndex(Collection<Polygon> polys, double cellSize) {
            this.cellSize = cellSize;
            // Compute global origin (minX/minY) to keep keys small
            double minx = Double.POSITIVE_INFINITY, miny = Double.POSITIVE_INFINITY;
            for (Polygon w : polys) {
                Rectangle2D b = w.bbox;
                if (b.getMinX() < minx) {
                    minx = b.getMinX();
                }
                if (b.getMinY() < miny) {
                    miny = b.getMinY();
                }
            }
            this.minX = minx;
            this.minY = miny;

            // Insert
            for (Polygon w : polys) {
                forEachCell(w.bbox, (gx, gy) -> {
                    cells.computeIfAbsent(key(gx, gy), k -> new ArrayList<>()).add(w);
                });
            }
        }

        private long key(int gx, int gy) {
            // Pack two 32-bit ints into one long
            return ((long) gx << 32) ^ (gy & 0xffffffffL);
        }

        private int gx(double x) {
            return (int) Math.floor((x - minX) / cellSize);
        }

        private int gy(double y) {
            return (int) Math.floor((y - minY) / cellSize);
        }

        private void forEachCell(Rectangle2D r, CellConsumer cc) {
            int x0 = gx(r.getMinX());
            int x1 = gx(r.getMaxX());
            int y0 = gy(r.getMinY());
            int y1 = gy(r.getMaxY());
            for (int x = x0; x <= x1; x++) {
                for (int y = y0; y <= y1; y++) {
                    cc.accept(x, y);
                }
            }
        }

        List<Polygon> query(Rectangle2D r) {
            // Collect candidates from overlapping cells (deduplicated)
            HashSet<Polygon> set = new HashSet<>();
            forEachCell(r, (gx, gy) -> {
                List<Polygon> bucket = cells.get(key(gx, gy));
                if (bucket != null) {
                    set.addAll(bucket);
                }
            });
            return new ArrayList<>(set);
        }
    }

    interface CellConsumer {

        void accept(int gx, int gy);
    }

    public static void buildContainment(List<Polygon> polygons) {

        Map<Integer, List<Polygon>> byStyle = polygons.stream()
                .collect(java.util.stream.Collectors.groupingBy(w -> w.fillStyle));

        for (Map.Entry<Integer, List<Polygon>> e : byStyle.entrySet()) {
            List<Polygon> group = e.getValue();

            double avgW = group.stream().mapToDouble(w -> w.bbox.getWidth()).average().orElse(1.0);
            double avgH = group.stream().mapToDouble(w -> w.bbox.getHeight()).average().orElse(1.0);
            double cellSize = Math.max(1.0, Math.max(avgW, avgH));

            GridIndex index = new GridIndex(group, cellSize);

            group.sort((a, b) -> Double.compare(b.area, a.area));

            for (int i = group.size() - 1; i >= 0; i--) {
                Polygon inner = group.get(i);
                List<Polygon> candidates = index.query(inner.bbox);

                Polygon bestParent = null;
                double bestArea = Double.POSITIVE_INFINITY;

                for (Polygon outer : candidates) {
                    if (outer == inner) {
                        continue;
                    }
                    if (outer.area <= inner.area) {
                        continue; // only larger can contain
                    }
                    if (!outer.bbox.contains(inner.bbox)) {
                        continue; // cheap reject
                    }
                    if (outer.contains(inner)) {
                        if (outer.area < bestArea) {
                            bestArea = outer.area;
                            bestParent = outer;
                        }
                    }
                }
                if (bestParent != null) {
                    bestParent.children.add(inner);
                }
            }
        }
    }

    private void fixSidesInLayer(
            List<List<IEdge>> fillList,
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            int layer,
            int startIndex, int endIndex,
            Map<Integer, Integer> globalToLocalFillStyleMap
    ) {

        layer++;

        if (layer >= fillList.size()) {
            Logger.getLogger(SwitchedFillSidesFixer.class.getName()).warning("FillResolver - Layer value larger than fill list size.");
            return;
        }
        int fillStyleIdx = Integer.MAX_VALUE;
        List<IEdge> currentList = new ArrayList<>();
        List<List<IEdge>> allLists = new ArrayList<>();
        List<Integer> listFills = new ArrayList<>();
        int lastToX = Integer.MAX_VALUE;
        int lastToY = Integer.MAX_VALUE;
        int lastMoveToX = Integer.MAX_VALUE;
        int lastMoveToY = Integer.MAX_VALUE;
        for (int i = 0; i < fillList.get(layer).size(); i++) {
            IEdge e = fillList.get(layer).get(i);
            if (fillStyleIdx != e.getFillStyleIdx()
                    || (e.getFromX() != lastToX) || (e.getFromY() != lastToY)
                    || (e.getFromX() == lastMoveToX && e.getFromY() == lastMoveToY)) {
                if (fillStyleIdx != Integer.MAX_VALUE) {
                    allLists.add(currentList);
                    listFills.add(fillStyleIdx);
                    currentList = new ArrayList<>();
                }
                fillStyleIdx = e.getFillStyleIdx();
                lastMoveToX = e.getFromX();
                lastMoveToY = e.getFromY();
            }
            currentList.add(e);
            lastToX = e.getToX();
            lastToY = e.getToY();
        }
        if (!currentList.isEmpty()) {
            allLists.add(currentList);
            listFills.add(fillStyleIdx);
        }

        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < allLists.size(); i++) {
            List<IEdge> list = allLists.get(i);
            polygons.add(new Polygon(list, listFills.get(i)));
        }

        /*for (Polygon outer : polygons) {
            for (Polygon inner : polygons) {
                if (outer != inner && inner.fillStyle == outer.fillStyle) {
                    boolean cont = outer.contains(inner);

                    if (cont) {
                        if (inner.children.contains(outer)) {
                            inner.children.remove(outer);
                        }
                        outer.children.add(inner);
                    }
                }
            }
        }

        loopmod:
        while (true) {
            for (Polygon poly : polygons) {
                for (int c = 0; c < poly.children.size(); c++) {
                    for (int c2 = 0; c2 < poly.children.size(); c2++) {
                        if (poly.children.get(c).children.contains(poly.children.get(c2))) {
                            poly.children.remove(c2);
                            continue loopmod;
                        }
                    }
                }
            }
            break;
        }*/
        buildContainment(polygons);

        for (Polygon poly : polygons) {
            for (Polygon child : poly.children) {
                child.parent = poly;
            }
        }

        for (Polygon poly : polygons) {
            int depth = 0;

            Polygon parent = poly.parent;
            while (parent != null) {
                parent = parent.parent;
                depth++;
            }

            poly.filled = depth % 2 == 0;
        }

        Map<BezierEdge, List<Integer>> beToFillStyle0List = new LinkedHashMap<>();
        Map<BezierEdge, List<Integer>> beToFillStyle1List = new LinkedHashMap<>();

        Map<BezierEdge, Integer> beToFillStyle0 = new LinkedHashMap<>();
        Map<BezierEdge, Integer> beToFillStyle1 = new LinkedHashMap<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            List<IEdge> list = polygon.list;
            fillStyleIdx = listFills.get(i);
            boolean clockwise = !polygon.ccw;

            for (IEdge e : list) {
                BezierEdge be = iedgeToBezier(e);
                BezierEdge beRev = be.reverse();
                int localFs = globalToLocalFillStyleMap.get(fillStyleIdx);

                BezierEdge search = new BezierEdge(180.0, -3040.0, 480.0, -3400.0);

                boolean print = false;

                if (be.equals(search)) {
                    System.err.println("xxx");
                    System.err.println("" + polygon);
                    print = true;
                }
                if (be.equals(search.reverse())) {
                    System.err.println("yyy");
                    print = true;
                }

                if (print) {
                    System.err.println("localFS: " + localFs);
                    System.err.println("filled: " + polygon.filled);
                    System.err.println("clockwise: " + clockwise);
                }

                if (polygon.filled == clockwise) {
                    if (!beToFillStyle1List.containsKey(be)) {
                        beToFillStyle1List.put(be, new ArrayList<>());
                    }
                    if (!beToFillStyle0List.containsKey(beRev)) {
                        beToFillStyle0List.put(beRev, new ArrayList<>());
                    }
                    beToFillStyle1List.get(be).add(localFs);
                    beToFillStyle0List.get(beRev).add(localFs);

                    if (print) {
                        System.err.println("setting FS1 and rev FS0");
                    }

                } else {
                    if (!beToFillStyle0List.containsKey(be)) {
                        beToFillStyle0List.put(be, new ArrayList<>());
                    }
                    if (!beToFillStyle1List.containsKey(beRev)) {
                        beToFillStyle1List.put(beRev, new ArrayList<>());
                    }

                    beToFillStyle0List.get(be).add(localFs);
                    beToFillStyle1List.get(beRev).add(localFs);

                    if (print) {
                        System.err.println("setting FS0 and rev FS1");
                    }

                }

                if (print) {
                    System.err.println("");
                }

            }

        }

        for (BezierEdge be : beToFillStyle0List.keySet()) {
            /*for (int i = beToFillStyle0List.get(be).size() - 1; i >= 0; i--) {
                Integer fs = beToFillStyle0List.get(be).get(i);
                if (beToFillStyle1List.containsKey(be) && beToFillStyle1List.get(be).contains(fs)) {
                    beToFillStyle0List.get(be).remove(fs);
                    beToFillStyle1List.get(be).remove(fs);
                }
            }*/
            int fs = -1;
            if (beToFillStyle0List.get(be).size() == 1) {
                fs = beToFillStyle0List.get(be).get(0);
            }
            if (!beToFillStyle0.containsKey(be) || beToFillStyle0.get(be) > 0 || fs == -1) {
                beToFillStyle0.put(be, fs);
            }
            if (!beToFillStyle1.containsKey(be.reverse()) || beToFillStyle1.get(be.reverse()) > 0 || fs == -1) {
                beToFillStyle1.put(be.reverse(), fs);
            }
        }

        for (BezierEdge be : beToFillStyle1List.keySet()) {
            int fs = -1;
            if (beToFillStyle1List.get(be).size() == 1) {
                fs = beToFillStyle1List.get(be).get(0);
            }
            if (!beToFillStyle1.containsKey(be) || beToFillStyle1.get(be) > 0 || fs == -1) {
                beToFillStyle1.put(be, fs);
            }
            if (!beToFillStyle0.containsKey(be.reverse()) || beToFillStyle0.get(be.reverse()) > 0 || fs == -1) {
                beToFillStyle0.put(be.reverse(), fs);
            }
        }

        for (int i = startIndex; i < endIndex; i++) {
            List<BezierEdge> shape = shapes.get(i);
            for (int j = 0; j < shape.size(); j++) {
                BezierEdge be = shape.get(j);
                if (be.isEmpty()) {
                    continue;
                }

                Integer fs0before = fillStyles0.get(i);
                Integer fs1before = fillStyles1.get(i);

                if (fs0before == 0 && fs1before == 0) { //only strokes
                    break;
                }

                Integer fs0after = beToFillStyle0.get(be);
                Integer fs1after = beToFillStyle1.get(be);

                if (fs0after == null) {
                    fs0after = 0;
                }
                if (fs1after == null) {
                    fs1after = 0;
                }

                if (fs0after == -1 || fs1after == -1) {
                    break;
                }

                if (fs0after == 0 && Objects.equals(fs1after, fs1before)) {
                    fs0after = fs0before;
                } else if (fs1after == 0 && Objects.equals(fs0after, fs0before)) {
                    fs1after = fs1before;
                }

                fillStyles0.set(i, fs0after);
                fillStyles1.set(i, fs1after);

                if (!Objects.equals(fs0before, fs0after) || !Objects.equals(fs1before, fs1after)) {
                    Logger.getLogger(SwitchedFillSidesFixer.class.getName()).log(Level.FINE, "Changed edge {0} - old: {1}, {2} new: {3}, {4}", new Object[]{be, fs0before, fs1before, fs0after, fs1after});
                }
                break;
            }
        }
    }

    public void fixSwitchedFills(
            int shapeNum,
            List<SHAPERECORD> records,
            FILLSTYLEARRAY fillStyles,
            LINESTYLEARRAY lineStyles,
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> layers
    ) {

        SHAPEWITHSTYLE shp = new SHAPEWITHSTYLE();
        shp.shapeRecords = records;
        shp.fillStyles = fillStyles;
        shp.lineStyles = lineStyles;

        List<List<IEdge>> fillList = new ArrayList<>();

        SWF swf = new SWF();
        new ShapeExporterBase(ShapeTag.WIND_EVEN_ODD, shapeNum, swf, shp, null) {
            @Override
            protected void handleFillPaths(List<List<IEdge>> fillPaths) {
                fillList.addAll(fillPaths);
            }

            @Override
            public void beginShape() {
            }

            @Override
            public void endShape() {
            }

            @Override
            public void beginFills() {
            }

            @Override
            public void endFills() {
            }

            @Override
            public void beginLines() {
            }

            @Override
            public void endLines(boolean close) {
            }

            @Override
            public void beginFill(RGB color) {
            }

            @Override
            public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
            }

            @Override
            public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
            }

            @Override
            public void endFill() {
            }

            @Override
            public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
            }

            @Override
            public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
            }

            @Override
            public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
            }

            @Override
            public void moveTo(double x, double y) {
            }

            @Override
            public void lineTo(double x, double y) {
            }

            @Override
            public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
            }
        };

        Map<Integer, Integer> globalToLocalFillStyleMap = new LinkedHashMap<>();
        int lastFs = 0;
        globalToLocalFillStyleMap.put(0, 0);
        for (int i = 0; i < fillStyles.fillStyles.length; i++) {
            lastFs++;
            globalToLocalFillStyleMap.put(lastFs, lastFs);
        }
        for (SHAPERECORD rec : records) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateNewStyles) {
                    for (int i = 0; i < scr.fillStyles.fillStyles.length; i++) {
                        lastFs++;
                        globalToLocalFillStyleMap.put(lastFs, i + 1);
                    }
                }
            }
        }

        int from = 0;
        for (int i = 1; i < layers.size(); i++) {
            if (!layers.get(i).equals(layers.get(i - 1))) {
                fixSidesInLayer(fillList, shapes, fillStyles0, fillStyles1, layers.get(i - 1), from, i, globalToLocalFillStyleMap);
                from = i;
            }
        }
        if (!layers.isEmpty()) {
            fixSidesInLayer(fillList, shapes, fillStyles0, fillStyles1, layers.get(layers.size() - 1), from, layers.size(), globalToLocalFillStyleMap);
        }
    }
}
