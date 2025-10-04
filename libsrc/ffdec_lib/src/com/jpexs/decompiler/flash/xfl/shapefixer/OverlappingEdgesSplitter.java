package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.math.Intersections;
import com.jpexs.decompiler.flash.math.OverlapInterval;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author JPEXS
 */
public class OverlappingEdgesSplitter {
    private static class BezierEdgeWrapper {

        BezierEdge be;
        int layer;
        int pathIndex;
        int edgeIndex;

        public BezierEdgeWrapper(BezierEdge be, int layer, int shapeIndex, int edgeIndex) {
            this.be = be;
            this.layer = layer;
            this.pathIndex = shapeIndex;
            this.edgeIndex = edgeIndex;
        }

        double minX() {
            return bbox().getMinX();
        }

        double maxX() {
            return bbox().getMaxX();
        }

        double minY() {
            return bbox().getMinY();
        }

        double maxY() {
            return bbox().getMaxY();
        }

        Rectangle2D bbox() {
            return be.bbox();
        }
    }

    static final class Event implements Comparable<Event> {

        final Type type;
        final double x;
        final BezierEdgeWrapper e;

        enum Type {
            START, END
        }

        public Event(Type type, double x, BezierEdgeWrapper e) {
            this.type = type;
            this.x = x;
            this.e = e;
        }

        @Override
        public int compareTo(Event o) {
            int cx = Double.compare(this.x, o.x);
            if (cx != 0) {
                return cx;
            }
            int ct = this.type.ordinal() - o.type.ordinal();
            if (ct != 0) {
                return ct;
            }
            int ce = System.identityHashCode(this.e) - System.identityHashCode(o.e);
            return ce;
        }
    }

    static class BezierEdgePair {
        BezierEdgeWrapper be1;
        BezierEdgeWrapper be2; 

        public BezierEdgePair(BezierEdgeWrapper be1, BezierEdgeWrapper be2) {
            this.be1 = be1;
            this.be2 = be2;
        }       
    }
    
    static final class Sweep {

        Map<BezierEdgeWrapper, List<Double>> splitPoints = new LinkedHashMap<>();
        Map<BezierEdgeWrapper, List<Point2D>> splitPoints2D = new LinkedHashMap<>();
        Map<BezierEdgeWrapper, List<Point2D>> splitPointsControl = new LinkedHashMap<>();
        //Map<BezierEdgePair, List<OverlapInterval>> overlapIntervals = new LinkedHashMap<>();
        final java.util.Comparator<BezierEdgeWrapper> statusCmp = (e1, e2) -> {
            int cMinY = Double.compare(e1.minY(), e2.minY());
            if (cMinY != 0) {
                return cMinY;
            }
            return Integer.compare(System.identityHashCode(e1), System.identityHashCode(e2));
        };
        final java.util.TreeSet<BezierEdgeWrapper> status = new TreeSet<>(statusCmp);
        //final java.util.Set<BezierEdgeWrapper> status = new HashSet<>();
        final java.util.PriorityQueue<Event> pq = new java.util.PriorityQueue<>();

        // eps values for numeric robustness
        static final double EPS = 1e-9;

        void addEdge(BezierEdgeWrapper e) {
            pq.add(new Event(Event.Type.START, e.minX(), e));
            pq.add(new Event(Event.Type.END, e.maxX(), e));
        }

        void run() {
            //int total = pq.size();
            //int cnt = 0;
            while (!pq.isEmpty()) {
                /*if (cnt % 1000 == 0) {
                    System.err.println("Percent done: " + (Math.round((cnt * 100.0 / total) * 100.0) / 100.0));
                }
                cnt++;*/
                Event ev = pq.poll();

                switch (ev.type) {
                    case START:
                        BezierEdgeWrapper beMaxY = new BezierEdgeWrapper(null, 0, 0, 0) {
                            @Override
                            double minY() {
                                return ev.e.maxY();
                            }
                        };

                        for (BezierEdgeWrapper e2 : status.headSet(beMaxY, true)) {
                            /*if (e2.minY() > maxY) {
                                break;
                            }*/
                            checkPair(ev.e, e2);
                        }
                        status.add(ev.e);
                        break;
                    case END:
                        status.remove(ev.e);
                        break;
                }
            }
        }

        private void checkPair(BezierEdgeWrapper e1, BezierEdgeWrapper e2) {
            if (e1 == null || e2 == null) {
                return;
            }

            List<Double> t1Ref = new ArrayList<>();
            List<Double> t2Ref = new ArrayList<>();
            List<Point2D> intPoint = new ArrayList<>();
            
            if (!Intersections.rectIntersection(e1.bbox(), e2.bbox())) {
                return;
            }
            
            boolean hasIntersections = e1.be.intersects(e2.be, t1Ref, t2Ref, intPoint);
            List<OverlapInterval> overlapIntervals = e1.be.overlap(e2.be);
            
            if (!hasIntersections && overlapIntervals.isEmpty()) {
                return;
            }
            
            

            if (!splitPoints.containsKey(e1)) {
                splitPoints.put(e1, new ArrayList<>());
            }
            if (!splitPoints.containsKey(e2)) {
                splitPoints.put(e2, new ArrayList<>());
            }
            
            if (!splitPoints2D.containsKey(e1)) {
                splitPoints2D.put(e1, new ArrayList<>());
            }
            if (!splitPoints2D.containsKey(e2)) {
                splitPoints2D.put(e2, new ArrayList<>());
            }
            if (!splitPointsControl.containsKey(e1)) {
                splitPointsControl.put(e1, new ArrayList<>());
            }
            if (!splitPointsControl.containsKey(e2)) {
                splitPointsControl.put(e2, new ArrayList<>());
            }
            splitPoints.get(e1).addAll(t1Ref);
            splitPoints.get(e2).addAll(t2Ref);
            splitPoints2D.get(e1).addAll(intPoint);
            splitPoints2D.get(e2).addAll(intPoint);
            for (int i = 0; i < intPoint.size(); i++) {
                splitPointsControl.get(e1).add(null);
                splitPointsControl.get(e2).add(null);
            }
                    
            
            if (!overlapIntervals.isEmpty()) {
                for (OverlapInterval interval : overlapIntervals) {                    
                    if (interval.t0 == interval.t1) {
                        continue;                        
                    }
                    BezierEdge middle;                    
                    if (interval.t0 == 1.0) {
                        if (interval.t1 == 0.0) {
                            middle = e1.be;
                        } else {
                            middle = e1.be.split(Arrays.asList(interval.t1)).get(0);
                        }
                    } else {
                        List<BezierEdge> splitted = e1.be.split(Arrays.asList(interval.t0, interval.t1));
                        middle = splitted.get(1);
                    }
                    
                    splitPoints.get(e1).add(interval.t0);
                    splitPoints.get(e1).add(interval.t1);
                    splitPoints.get(e2).add(interval.u0);
                    splitPoints.get(e2).add(interval.u1);
                    
                    
                    System.err.println("Overlapping " + e1.be.toSvg()+" AND " + e2.be.toSvg()+" by " + middle.toSvg());
                    
                    splitPoints2D.get(e1).add(middle.getBeginPoint());
                    splitPoints2D.get(e1).add(middle.getEndPoint());
                    splitPointsControl.get(e1).add(middle.getControlPoint());
                    splitPointsControl.get(e1).add(middle.getControlPoint());
                    
                    splitPoints2D.get(e2).add(middle.getBeginPoint());
                    splitPoints2D.get(e2).add(middle.getEndPoint());
                    splitPointsControl.get(e2).add(middle.getControlPoint());
                    splitPointsControl.get(e2).add(middle.getControlPoint());
                }
            }
            
        }
    }
    
    private static class TPoint {
        double t;
        Point2D point;
        Point2D controlPoint;

        public TPoint(double t, Point2D point, Point2D controlPoint) {
            this.t = t;
            this.point = point;
            this.controlPoint = controlPoint;
        }
        
        
    }

    private void handleBewList(List<BezierEdgeWrapper> bewList, List<Layer> layers) {
        Map<Integer, List<BezierEdgeWrapper>> bewMap = bewList.stream()
                .collect(Collectors.groupingBy(b -> b.layer));

        for (Map.Entry<Integer, List<BezierEdgeWrapper>> entry : bewMap.entrySet()) {

            Set<BezierEdgeWrapper> bewsToIgnore = new LinkedHashSet<>();

            Map<BezierEdge, BezierEdgeWrapper> existingEdges = new HashMap<>();

            //eliminate duplicates
            for (BezierEdgeWrapper bew1 : entry.getValue()) {
                BezierEdge be = bew1.be;
                BezierEdge rev = bew1.be.reverse();

                BezierEdgeWrapper prevBew = existingEdges.get(be);
                if (prevBew != null) {
                    bewsToIgnore.add(prevBew);
                }
                existingEdges.put(be, bew1);

                BezierEdgeWrapper prevRevBew = existingEdges.get(rev);
                if (prevRevBew != null) {
                    bewsToIgnore.add(prevRevBew);
                }
                existingEdges.put(rev, bew1);
            }

            //eliminate duplicates
            /*for (BezierEdgeWrapper bew1 : entry.getValue()) {
                for (BezierEdgeWrapper bew2 : entry.getValue()) {
                    if (bew1 != bew2) {
                        if (bew1.beOriginal.equals(bew2.beOriginal)
                                || bew1.beOriginal.equalsReverse(bew2.beOriginal)) {
                            bewsToIgnore.add(bew1);
                        }
                    }
                }
            }*/
            boolean useSweep = true;

            Map<BezierEdgeWrapper, List<Double>> splitPointsMap = new LinkedHashMap<>();
            Map<BezierEdgeWrapper, List<Point2D>> splitPoints2DMap = new LinkedHashMap<>();
            Map<BezierEdgeWrapper, List<Point2D>> splitPointsControlMap = new LinkedHashMap<>();

            if (useSweep) {
                Sweep sweep = new Sweep();
                for (BezierEdgeWrapper bew : entry.getValue()) {
                    if (bewsToIgnore.contains(bew)) {
                        continue;
                    }
                    sweep.addEdge(bew);
                }
                sweep.run();
                splitPointsMap = sweep.splitPoints;
                splitPoints2DMap = sweep.splitPoints2D;
                splitPointsControlMap = sweep.splitPointsControl;
            } else {

                for (BezierEdgeWrapper bew1 : entry.getValue()) {
                    for (BezierEdgeWrapper bew2 : entry.getValue()) {
                        if (bew1 != bew2) {
                            List<Double> t1Ref = new ArrayList<>();
                            List<Double> t2Ref = new ArrayList<>();
                            List<Point2D> intPoints = new ArrayList<>();
                            if (bew1.be.intersects(bew2.be, t1Ref, t2Ref, intPoints)) {
                                if (!splitPointsMap.containsKey(bew1)) {
                                    splitPointsMap.put(bew1, new ArrayList<>());
                                }
                                splitPointsMap.get(bew1).addAll(t1Ref);

                                if (!splitPointsMap.containsKey(bew2)) {
                                    splitPointsMap.put(bew2, new ArrayList<>());
                                }
                                splitPointsMap.get(bew2).addAll(t2Ref);
                            }
                        }
                    }
                }
            }

            List<BezierEdgeWrapper> splittedBewList = new ArrayList<>(splitPointsMap.keySet());

            splittedBewList.sort((BezierEdgeWrapper o1, BezierEdgeWrapper o2) -> {
                int dShapeIndex = o1.pathIndex - o2.pathIndex;
                if (dShapeIndex != 0) {
                    return dShapeIndex;
                }
                int dEIndex = o1.edgeIndex - o2.edgeIndex;
                if (dEIndex != 0) {
                    return dEIndex;
                }
                return System.identityHashCode(o1) - System.identityHashCode(o2);
            });

            for (int i = splittedBewList.size() - 1; i >= 0; i--) {
                BezierEdgeWrapper bew = splittedBewList.get(i);

                List<Double> splitT = splitPointsMap.get(bew);
                List<Point2D> splitPoint = splitPoints2DMap.get(bew);
                List<Point2D> splitControls = splitPointsControlMap.get(bew);
               
                List<TPoint> splits = new ArrayList<>();
                for (int j = 0; j < splitT.size(); j++) {
                    splits.add(new TPoint(splitT.get(j), splitPoint.get(j), splitControls.get(j)));
                }

                splits.sort((a, b) -> Double.compare(a.t, b.t));
                
                BezierEdge be = bew.be;
                List<Double> realSplitT = new ArrayList<>();
                for (TPoint tp : splits) {
                    if (tp.t == 0.0 || tp.t == 1.0) {
                        continue;
                    }

                    realSplitT.add(tp.t);
                }

                if (realSplitT.isEmpty()) {
                    continue;
                }

                List<BezierEdge> splitted = be.split(realSplitT);
                if (splits.get(0).t != 0.0) {
                    splits.add(0, new TPoint(0.0, bew.be.getBeginPoint(), null));
                }
                if (splits.get(splits.size() - 1).t != 1.0) {
                    splits.add(new TPoint(1.0, bew.be.getEndPoint(), null));
                }
                
                int p = 0;
                for (int j = 0; j < splits.size(); j++) {
                    if (splits.get(j).t == 0.0 || splits.get(j).t == 1.0) {
                        continue;
                    }                    
                    splitted.get(p).setBeginPoint(splits.get(j - 1).point);
                    splitted.get(p).setEndPoint(splits.get(j).point);
                    if (splits.get(j - 1).controlPoint == splits.get(j).controlPoint && splits.get(j).controlPoint != null) {
                        splitted.get(p).setControlPoint(splits.get(j).controlPoint);
                    }
                    p++;
                }
                layers.get(bew.layer).paths.get(bew.pathIndex).edges.remove(bew.edgeIndex);
                int pos = 0;
                for (BezierEdge bes : splitted) {
                    layers.get(bew.layer).paths.get(bew.pathIndex).edges.add(bew.edgeIndex + pos, bes);
                    pos++;
                }
            }
        }
    }

    public void splitOverlappingEdges(
            List<Layer> layers
    ) {
        List<BezierEdgeWrapper> strokesBewList = new ArrayList<>();
        List<BezierEdgeWrapper> fillsBewList = new ArrayList<>();
        
        for (int layer = 0; layer < layers.size(); layer++) {
            for (int p = 0; p < layers.get(layer).paths.size(); p++) {
                Path path = layers.get(layer).paths.get(p);
                for (int e = 0; e < path.edges.size(); e++) {
                    BezierEdge be = path.edges.get(e);
                    BezierEdgeWrapper bew = new BezierEdgeWrapper(be, layer, p, e);
                    if (path.fillStyle0 == 0 && path.fillStyle1 == 0) {
                        strokesBewList.add(bew);
                    } else {
                        fillsBewList.add(bew);
                    }
                }
            }
        }
        
        handleBewList(strokesBewList, layers);
        handleBewList(fillsBewList, layers);

        for (int layer = 0; layer < layers.size(); layer++) {
            for (int p = 0; p < layers.get(layer).paths.size(); p++) {
                Path path = layers.get(layer).paths.get(p);
                for (int e = 0; e < path.edges.size(); e++) {
                    BezierEdge be1 = path.edges.get(e);
                    be1.shrinkToLine();
                }
            }
        }
    }
}
