package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
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
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Reference;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ShapeFixer2 {

    private List<Layer> splitToLayers(
            List<SHAPERECORD> records,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles
    ) {
        List<Layer> result = new ArrayList<>();

        Layer currentLayer = new Layer();
        currentLayer.fillStyleArray = baseFillStyles;
        currentLayer.lineStyleArray = baseLineStyles;

        List<BezierEdge> currentEdges = new ArrayList<>();

        int fillStyle0 = 0;
        int fillStyle1 = 0;
        int lineStyle = 0;
        int x = 0;
        int y = 0;
        for (SHAPERECORD rec : records) {
            assert (rec != null);
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo
                        || scr.stateNewStyles
                        || scr.stateFillStyle0
                        || scr.stateFillStyle1
                        || scr.stateLineStyle) {
                    if (!currentEdges.isEmpty()) {
                        Path path = new Path();
                        path.edges = currentEdges;
                        path.fillStyle0 = fillStyle0;
                        path.fillStyle1 = fillStyle1;
                        path.lineStyle = lineStyle;
                        currentLayer.paths.add(path);
                        currentEdges = new ArrayList<>();
                    }
                }
                if (scr.stateNewStyles) {
                    if (!currentLayer.paths.isEmpty()) {
                        result.add(currentLayer);
                    }
                    currentLayer = new Layer();
                    currentLayer.fillStyleArray = scr.fillStyles;
                    currentLayer.lineStyleArray = scr.lineStyles;
                    fillStyle0 = 0;
                    fillStyle1 = 0;
                    lineStyle = 0;
                }
                if (scr.stateFillStyle0) {
                    fillStyle0 = scr.fillStyle0;
                }
                if (scr.stateFillStyle1) {
                    fillStyle1 = scr.fillStyle1;
                }
                if (scr.stateLineStyle) {
                    lineStyle = scr.lineStyle;
                }
            }
            if (rec instanceof StraightEdgeRecord) {
                int x2 = rec.changeX(x);
                int y2 = rec.changeY(y);
                BezierEdge be = new BezierEdge(x, y, x2, y2);
                currentEdges.add(be);
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                int cx = x + cer.controlDeltaX;
                int cy = y + cer.controlDeltaY;
                int ax = cx + cer.anchorDeltaX;
                int ay = cy + cer.anchorDeltaY;
                BezierEdge be = new BezierEdge(x, y, cx, cy, ax, ay);
                currentEdges.add(be);
            }
            if (rec instanceof EndShapeRecord) {
                if (!currentEdges.isEmpty()) {
                    Path path = new Path();
                    path.edges = currentEdges;
                    path.fillStyle0 = fillStyle0;
                    path.fillStyle1 = fillStyle1;
                    path.lineStyle = lineStyle;
                    currentLayer.paths.add(path);
                    result.add(currentLayer);
                    currentEdges = new ArrayList<>();
                }
            }
            x = rec.changeX(x);
            y = rec.changeY(y);
        }
        return result;
    }

    private List<ShapeRecordAdvanced> combineLayers(List<Layer> layers, FILLSTYLEARRAY baseFillStyles, LINESTYLEARRAY baseLineStyles) {
        List<ShapeRecordAdvanced> ret = new ArrayList<>();
        double dx;
        double dy;
        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            if (layer.paths.isEmpty()) {
                continue;
            }
            if (layer.fillStyleArray != baseFillStyles && layer.lineStyleArray != baseLineStyles) {
                StyleChangeRecordAdvanced scr = new StyleChangeRecordAdvanced();
                scr.stateNewStyles = true;
                scr.fillStyles = layer.fillStyleArray;
                scr.lineStyles = layer.lineStyleArray;
                scr.stateFillStyle0 = true;
                scr.fillStyle0 = 0;
                scr.stateFillStyle1 = true;
                scr.fillStyle1 = 0;
                scr.stateLineStyle = true;
                scr.lineStyle = 0;
                ret.add(scr);
            }
            for (Path path : layer.paths) {
                if (path.edges.isEmpty()) {
                    continue;
                }
                StyleChangeRecordAdvanced scr = new StyleChangeRecordAdvanced();
                scr.stateMoveTo = true;
                dx = scr.moveDeltaX = path.edges.get(0).points.get(0).getX();
                dy = scr.moveDeltaY = path.edges.get(0).points.get(0).getY();

                scr.stateFillStyle0 = true;
                scr.fillStyle0 = path.fillStyle0;
                scr.stateFillStyle1 = true;
                scr.fillStyle1 = path.fillStyle1;
                scr.stateLineStyle = true;
                scr.lineStyle = path.lineStyle;
                ret.add(scr);
                for (BezierEdge be : path.edges) {
                    if (!be.getBeginPoint().equals(new Point2D.Double(dx, dy))) {
                        StyleChangeRecordAdvanced sm = new StyleChangeRecordAdvanced();
                        sm.stateMoveTo = true;
                        sm.moveDeltaX = be.getBeginPoint().getX();
                        sm.moveDeltaY = be.getBeginPoint().getY();
                        ret.add(sm);
                    }
                    dx = be.getEndPoint().getX();
                    dy = be.getEndPoint().getY();

                    ShapeRecordAdvanced sra = bezierToAdvancedRecord(be);
                    ret.add(sra);
                }
            }
        }
        return ret;
    }

    private ShapeRecordAdvanced bezierToAdvancedRecord(BezierEdge be) {
        if (be.points.size() == 2) {
            StraightEdgeRecordAdvanced ser = new StraightEdgeRecordAdvanced();
            ser.deltaX = be.points.get(1).getX() - be.points.get(0).getX();
            ser.deltaY = be.points.get(1).getY() - be.points.get(0).getY();
            return ser;
        }
        if (be.points.size() == 3) {
            CurvedEdgeRecordAdvanced cer = new CurvedEdgeRecordAdvanced();
            cer.controlDeltaX = be.points.get(1).getX() - be.points.get(0).getX();
            cer.controlDeltaY = be.points.get(1).getY() - be.points.get(0).getY();
            cer.anchorDeltaX = be.points.get(2).getX() - be.points.get(1).getX();
            cer.anchorDeltaY = be.points.get(2).getY() - be.points.get(1).getY();
            return cer;
        }
        return null;
    }

    public List<ShapeRecordAdvanced> fix(
            List<SHAPERECORD> records,
            int shapeNum,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles,
            boolean wasSmall
    ) {

        List<Layer> layers = splitToLayers(records, baseFillStyles, baseLineStyles);

        getSingleFillLayers(layers, records, baseFillStyles, baseLineStyles, shapeNum);

        /*for (Layer layer : layers) {
            subtractAreas(layer);
        }
        
        removeEmpty(layers);*/

        OverlappingEdgesSplitter splitter = new OverlappingEdgesSplitter();
        splitter.splitOverlappingEdges(layers);       

        for (Layer layer : layers) {
            detectEdgeFills(layer);
        }
        
        for (Layer layer : layers) {
            layer.round(wasSmall);
        }
        
        removeEmpty(layers);
        
        /*for (Layer layer : layers) {
            fixFillSides(layer);
        }*/
        
        System.err.println("=============");
        for (int i = 0; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).paths.size(); j++) {
                Path p = layers.get(i).paths.get(j);
                System.err.println(p.toString());
                //System.err.println("FS0: " + p.fillStyle0);
                //System.err.println("FS1: " + p.fillStyle1);
                //System.err.println("LS: " + p.lineStyle);
                //System.err.println("----------");
            }
        }
        //System.exit(0);
        return combineLayers(layers, baseFillStyles, baseLineStyles);
    }

    private void removeEmpty(List<Layer> layers) {
        for (int i = 0; i < layers.size(); i++) {
            for (int j = 0; j < layers.get(i).paths.size(); j++) {
                for (int e = 0; e < layers.get(i).paths.get(j).edges.size(); e++) {
                    if (layers.get(i).paths.get(j).edges.get(e).isEmpty()) {
                        layers.get(i).paths.get(j).edges.remove(e);
                        e--;
                    }
                }
                if (layers.get(i).paths.get(j).edges.isEmpty()) {
                    layers.get(i).paths.remove(j);
                    j--;
                    if (layers.get(i).paths.isEmpty()) {
                        layers.remove(i);
                        i--;
                    }
                }
            }
        }
    }
    
    private void detectEdgeFills(Layer layer) {
        
        double epsBase = 1e-4;
        
        List<Rectangle2D> bboxes = new ArrayList<>();
        for (Path path : layer.paths){
            path.toArea();
            bboxes.add(path.bbox);
        }
        Set<BezierEdge> allEdges = new HashSet<>();
        List<StyledEdge> styledEdges = new ArrayList<>();
        for (Path path : layer.paths) {
            for (BezierEdge be : path.edges) {                
                if (allEdges.contains(be) || allEdges.contains(be.reverse())) {
                    continue;
                }
                Point2D mid = be.midPoint();
                Point2D n = be.unitNormal();
                double len = Math.hypot(be.getEndPoint().getX() - be.getBeginPoint().getX(), be.getEndPoint().getY() - be.getBeginPoint().getY());
                double eps = Math.max(epsBase, 1e-4 * len);
                
                double xL = mid.getX() - n.getX()*eps;
                double yL = mid.getY() - n.getY()*eps;
                double xR = mid.getX() + n.getX()*eps;
                double yR = mid.getY() + n.getY()*eps;
                
                int leftPath = topmostPathAt(xL, yL, layer.paths, bboxes);
                int rightPath = topmostPathAt(xR, yR, layer.paths, bboxes);
                
                int leftFill = leftPath == -1 ? 0 : layer.paths.get(leftPath).fillStyle0;                        
                int rightFill = rightPath == -1 ? 0 : layer.paths.get(rightPath).fillStyle0;

                int leftLine = leftPath == -1 ? 0 : layer.paths.get(leftPath).lineStyle;
                int rightLine = rightPath == -1 ? 0 : layer.paths.get(rightPath).lineStyle;   
                    
                int newLine = (leftLine == path.lineStyle || rightLine == path.lineStyle) ? path.lineStyle : 0;                
                
                if (leftFill == rightFill && newLine == 0) {
                    continue;
                }
                
                allEdges.add(be);
                
                StyledEdge styledEdge = new StyledEdge(be, leftFill, rightFill, newLine);
                styledEdges.add(styledEdge);
            }
        }
        layer.paths.clear();                
        
        int lastFs0 = -1;
        int lastFs1 = -1;
        int lastLs = -1;
        Path currentPath = null;
        for (StyledEdge edge : styledEdges) {            
            if (edge.fillStyle0 != lastFs0 || edge.fillStyle1 != lastFs1 || edge.lineStyle != lastLs) {
                currentPath = new Path();
                currentPath.fillStyle0 = edge.fillStyle0;
                currentPath.fillStyle1 = edge.fillStyle1;
                currentPath.lineStyle = edge.lineStyle;
                layer.paths.add(currentPath);
            }
            
            assert(currentPath != null);
            currentPath.edges.add(edge.edge);
            
            lastFs0 = edge.fillStyle0;
            lastFs1 = edge.fillStyle1;
            lastLs = edge.lineStyle;
        }
    }
    
    private static int topmostPathAt(
            double x, double y,
            List<Path> paths,
            List<Rectangle2D> bboxes
            ) {

        for (int i = paths.size() - 1; i >= 0; i--) {
            Rectangle2D bb = bboxes.get(i);
            if (!bb.contains(x, y)) {
                continue;
            }
            // Use contains on the shape with its own winding rule
            if (paths.get(i).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
    
    private static class StyledEdge {
        BezierEdge edge;
        int fillStyle0 = 0;
        int fillStyle1 = 0;
        int lineStyle = 0;

        public StyledEdge(BezierEdge edge) {
            this.edge = edge;
        }

        public StyledEdge(BezierEdge edge, int fillStyle0, int fillStyle1, int lineStyle) {
            this.edge = edge;
            this.fillStyle0 = fillStyle0;
            this.fillStyle1 = fillStyle1;
            this.lineStyle = lineStyle;
        }
        
    }
    
    private void fixFillSides(Layer layer) {
        List<Path> paths = layer.paths;

        buildContainment(paths);

        for (Path poly : paths) {
            for (Path child : poly.children) {
                child.parent = poly;
            }
        }

        for (Path path : paths) {
            int depth = 0;

            Path parent = path.parent;
            while (parent != null) {
                parent = parent.parent;
                depth++;
            }
            path.filled = depth % 2 == 0;
        }
        
        for (Path path: paths) {
            boolean clockwise = !path.counterClockWise;
            int fillStyle = path.fillStyle0;
            path.fillStyle0 = 0;
            if (path.filled == clockwise) {
                path.fillStyle1 = fillStyle;
            } else {
                path.fillStyle0 = fillStyle;
            }
        }
        
        List<Path> newPaths = new ArrayList<>();
        Map<BezierEdge, List<Integer>> edge2Fs0 = new HashMap<>();
        Map<BezierEdge, List<Integer>> edge2Fs1 = new HashMap<>();
        
        for (Path path : paths) {
            for (BezierEdge edge : path.edges) {
                if (path.fillStyle0 == 0 && path.fillStyle1 == 0) {
                    continue;
                }               
                BezierEdge edgeRev = edge.reverse();               
                
                
                if (!edge2Fs0.containsKey(edge)) {
                    edge2Fs0.put(edge, new ArrayList<>());
                }
                edge2Fs0.get(edge).add(path.fillStyle0);

                if (!edge2Fs1.containsKey(edgeRev)) {
                    edge2Fs1.put(edgeRev, new ArrayList<>());
                }
                edge2Fs1.get(edgeRev).add(path.fillStyle0);                
                
                if (!edge2Fs1.containsKey(edge)) {
                    edge2Fs1.put(edge, new ArrayList<>());
                }
                edge2Fs1.get(edge).add(path.fillStyle1);


                if (!edge2Fs0.containsKey(edgeRev)) {
                    edge2Fs0.put(edgeRev, new ArrayList<>());
                }
                edge2Fs0.get(edgeRev).add(path.fillStyle1);                                
            }
        }
        
        Set<BezierEdge> existingEdges = new LinkedHashSet<>();
        
        for (int p = paths.size() - 1; p >= 0; p--) {
            Path path = paths.get(p);
            int lastFs0 = -1;
            int lastFs1 = -1;
            for (int e = path.edges.size() - 1; e >= 0; e--) {
                BezierEdge edge = path.edges.get(e);
                
                List<Integer> fs0List = edge2Fs0.get(edge);
                List<Integer> fs1List = edge2Fs1.get(edge);
                
                if (p == 1 && e == 34) {
                    System.err.println("yyy");
                }
                
                for (int i = 0; i < fs0List.size(); i++) {
                    Integer fs = fs0List.get(i);
                    if (fs1List.contains(fs)) {
                        fs0List.remove(i);
                        fs1List.remove(fs);
                        i--;                        
                    }
                }
                                
                
                int fs0 = 0;
                if (!fs0List.isEmpty()) {
                    fs0 = fs0List.get(fs0List.size() - 1);
                }
                int fs1 = 0;
                if (!fs1List.isEmpty()) {
                    fs1 = fs1List.get(fs1List.size() - 1);
                }
                
                if (fs0 == 0 && fs1 == 0 && path.lineStyle == 0) {
                    System.err.println("no fill or linestyle - " + edge.toSvg() + " original fs = " + path.fillStyle0+", ls = " + path.lineStyle);
                    path.edges.remove(e);
                    continue;
                }
                
                BezierEdge edgeRev = edge.reverse();
                
                if (existingEdges.contains(edge) || existingEdges.contains(edgeRev)) {
                    path.edges.remove(e);
                    continue;
                }
                
                existingEdges.add(edge);
                existingEdges.add(edgeRev);
                
                if (lastFs0 > -1 && lastFs1 > -1 && fs0 != lastFs0 || fs1 != lastFs1) {
                    Path newPath = new Path();
                    newPath.edges = new ArrayList<>();
                    newPath.fillStyle0 = lastFs0;
                    newPath.fillStyle1 = lastFs1;
                    newPath.lineStyle = path.lineStyle;
                    for (int n = e + 1; e + 1 < path.edges.size(); n++) {
                        newPath.edges.add(path.edges.remove(e + 1));
                    }
                    if (!newPath.edges.isEmpty()) {
                        paths.add(p + 1, newPath);     
                    }
                }
                
                lastFs0 = fs0;
                lastFs1 = fs1;
            }
            if (lastFs0 > -1 && lastFs1 > -1) {
                path.fillStyle0 = lastFs0;
                path.fillStyle1 = lastFs1;
            }
            if (path.edges.isEmpty()) {
                paths.remove(p);
            }
        }
    }

    private void subtractAreas(Layer layer) {
        for (Path path : layer.paths) {
            path.toArea();
        }

        for (int p1 = 0; p1 < layer.paths.size(); p1++) {
            System.err.println("FROM " + layer.paths.get(p1));
            for (int p2 = p1 + 1; p2 < layer.paths.size(); p2++) {
                System.err.println("Subtract " + layer.paths.get(p2));
                layer.paths.get(p1).area.subtract(layer.paths.get(p2).area);
            }
            layer.paths.get(p1).fromArea();
            System.err.println("Result: " + layer.paths.get(p1));
            System.err.println("");
        }
    }

    private void getSingleFillLayers(List<Layer> layers, List<SHAPERECORD> records, FILLSTYLEARRAY baseFillStyles, LINESTYLEARRAY baseLineStyles, int shapeNum) {
        SHAPEWITHSTYLE shp = new SHAPEWITHSTYLE();
        shp.shapeRecords = records;
        shp.fillStyles = baseFillStyles;
        shp.lineStyles = baseLineStyles;

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
        Map<Integer, Integer> globalToLocalLineStyleMap = new LinkedHashMap<>();
        int lastFs = 0;
        int lastLs = 0;
        globalToLocalFillStyleMap.put(0, 0);
        globalToLocalLineStyleMap.put(0, 0);
        for (int i = 0; i < baseFillStyles.fillStyles.length; i++) {
            lastFs++;
            globalToLocalFillStyleMap.put(lastFs, lastFs);
        }
        for (int i = 0; i < (shapeNum == 4 ? baseLineStyles.lineStyles2.length : baseLineStyles.lineStyles.length); i++) {
            lastLs++;
            globalToLocalLineStyleMap.put(lastLs, lastLs);
        }
        for (SHAPERECORD rec : records) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateNewStyles) {
                    for (int i = 0; i < scr.fillStyles.fillStyles.length; i++) {
                        lastFs++;
                        globalToLocalFillStyleMap.put(lastFs, i + 1);
                    }
                    for (int i = 0; i < (shapeNum == 4 ? scr.lineStyles.lineStyles2.length : scr.lineStyles.lineStyles.length); i++) {
                        lastLs++;
                        globalToLocalLineStyleMap.put(lastLs, i + 1);
                    }
                }
            }
        }

        //assert(layers.size() == fillList.size());
        for (int i = 0; i < fillList.size(); i++) {
            if (fillList.get(i).isEmpty()) {
                fillList.remove(i);
                i--;
                continue;
            }
        }

        for (int layer = 0; layer < layers.size(); layer++) {
            Layer layerObj = layers.get(layer);
            layerObj.paths.clear();
            int fillStyleIdx = Integer.MAX_VALUE;
            int lineStyleIdx = Integer.MAX_VALUE;
            List<IEdge> currentList = new ArrayList<>();
            List<List<IEdge>> allLists = new ArrayList<>();
            List<Integer> listFills = new ArrayList<>();
            List<Integer> listLines = new ArrayList<>();
            int lastToX = Integer.MAX_VALUE;
            int lastToY = Integer.MAX_VALUE;
            int lastMoveToX = Integer.MAX_VALUE;
            int lastMoveToY = Integer.MAX_VALUE;
            for (int i = 0; i < fillList.get(layer).size(); i++) {
                IEdge e = fillList.get(layer).get(i);
                if (fillStyleIdx != e.getFillStyleIdx()
                        || lineStyleIdx != e.getLineStyleIdx()
                        || (e.getFromX() != lastToX) || (e.getFromY() != lastToY)
                        || (e.getFromX() == lastMoveToX && e.getFromY() == lastMoveToY)) {
                    if (fillStyleIdx != Integer.MAX_VALUE) {
                        allLists.add(currentList);
                        listFills.add(fillStyleIdx);
                        listLines.add(lineStyleIdx);
                        currentList = new ArrayList<>();
                    }
                    fillStyleIdx = e.getFillStyleIdx();
                    lineStyleIdx = e.getLineStyleIdx();
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
                listLines.add(lineStyleIdx);
            }

            /*List<ClosedPath> closedPaths = new ArrayList<>();
            for (int i = 0; i < allLists.size(); i++) {
                List<IEdge> list = allLists.get(i);
                closedPaths.add(new ClosedPath(list, listFills.get(i)));
            }

            buildContainment(closedPaths);

            for (ClosedPath poly : closedPaths) {
                for (ClosedPath child : poly.children) {
                    child.parent = poly;
                }
            }

            for (ClosedPath poly : closedPaths) {
                int depth = 0;

                ClosedPath parent = poly.parent;
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

             */
            for (int i = 0; i < allLists.size(); i++) {
                List<IEdge> list = allLists.get(i);
                fillStyleIdx = listFills.get(i);
                lineStyleIdx = listLines.get(i);
                //boolean clockwise = !polygon.ccw;

                Path path = new Path();
                layerObj.paths.add(path);

                int localFs = globalToLocalFillStyleMap.get(fillStyleIdx);
                int localLs = globalToLocalLineStyleMap.get(lineStyleIdx);

                /*if (polygon.filled == clockwise) {
                    path.fillStyle1 = localFs;
                } else {
                    path.fillStyle0 = localFs;
                }*/
                path.fillStyle0 = localFs;
                path.lineStyle = localLs;

                for (IEdge e : list) {
                    BezierEdge be = iEdgeToBezier(e);
                    //BezierEdge beRev = be.reverse();                    
                    path.edges.add(be);

                    //BezierEdge search = new BezierEdge(180.0, -3040.0, 480.0, -3400.0);
                    //boolean print = false;

                    /*if (be.equals(search)) {
                        System.err.println("xxx");
                        System.err.println("" + polygon);
                        print = true;
                    }
                    if (be.equals(search.reverse())) {
                        System.err.println("yyy");
                        print = true;
                    }*/
 /*if (print) {
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
                    }*/
                }

            }
        }

    }

    private BezierEdge iEdgeToBezier(IEdge ie) {
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

    static class GridIndex {

        // Simple uniform grid over bbox domain
        private final double cellSize;
        private final Map<Long, List<Path>> cells = new HashMap<>();
        private final double minX;
        private final double minY;

        GridIndex(Collection<Path> polys, double cellSize) {
            this.cellSize = cellSize;
            // Compute global origin (minX/minY) to keep keys small
            double minx = Double.POSITIVE_INFINITY;
            double miny = Double.POSITIVE_INFINITY;
            for (Path w : polys) {
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
            for (Path w : polys) {
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

        List<Path> query(Rectangle2D r) {
            // Collect candidates from overlapping cells (deduplicated)
            HashSet<Path> set = new HashSet<>();
            forEachCell(r, (gx, gy) -> {
                List<Path> bucket = cells.get(key(gx, gy));
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

    private static void buildContainment(List<Path> polygons) {

        Map<Integer, List<Path>> byStyle = polygons.stream()
                .collect(java.util.stream.Collectors.groupingBy(w -> w.fillStyle0));

        for (Map.Entry<Integer, List<Path>> e : byStyle.entrySet()) {
            List<Path> group = e.getValue();
            if (e.getKey() == 0) { //no fill
                continue;
            }

            double avgW = group.stream().mapToDouble(w -> w.bbox.getWidth()).average().orElse(1.0);
            double avgH = group.stream().mapToDouble(w -> w.bbox.getHeight()).average().orElse(1.0);
            double cellSize = Math.max(1.0, Math.max(avgW, avgH));

            GridIndex index = new GridIndex(group, cellSize);

            group.sort((a, b) -> Double.compare(b.areaValue, a.areaValue));

            for (int i = group.size() - 1; i >= 0; i--) {
                Path inner = group.get(i);
                List<Path> candidates = index.query(inner.bbox);

                Path bestParent = null;
                double bestArea = Double.POSITIVE_INFINITY;

                for (Path outer : candidates) {
                    if (outer == inner) {
                        continue;
                    }
                    if (outer.areaValue <= inner.areaValue) {
                        continue; // only larger can contain
                    }
                    if (!outer.bbox.contains(inner.bbox)) {
                        continue; // cheap reject
                    }
                    if (outer.contains(inner)) {
                        if (outer.areaValue < bestArea) {
                            bestArea = outer.areaValue;
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
}
