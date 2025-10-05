package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.math.Intersections;
import com.jpexs.decompiler.flash.math.OverlapInterval;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

        int id;
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
        List<OverlapPair> overlapPairs = new ArrayList<>();
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
            pq.add(new Event(Event.Type.START, e.minX() - 10, e));
            pq.add(new Event(Event.Type.END, e.maxX() + 10, e));
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
            List<OverlapInterval> overlapIntervals = new ArrayList<>();
            overlapIntervals = e1.be.overlap(e2.be);

            if (!hasIntersections && overlapIntervals.isEmpty()) {
                return;
            }

            if (!splitPoints.containsKey(e1)) {
                splitPoints.put(e1, new ArrayList<>());
            }
            if (!splitPoints.containsKey(e2)) {
                splitPoints.put(e2, new ArrayList<>());
            }

            splitPoints.get(e1).addAll(t1Ref);
            splitPoints.get(e2).addAll(t2Ref);

            if (!overlapIntervals.isEmpty()) {
                for (OverlapInterval interval : overlapIntervals) {
                    if (interval.t0 == interval.t1) {
                        continue;
                    }

                    overlapPairs.add(new OverlapPair(e1.id, e2.id, interval.t0, interval.t1, interval.u0, interval.u1));
                    splitPoints.get(e1).add(interval.t0);
                    splitPoints.get(e1).add(interval.t1);

                    splitPoints.get(e2).add(interval.u0);
                    splitPoints.get(e2).add(interval.u1);
                }
            }

        }
    }

    private void handleBewList(List<BezierEdgeWrapper> bewList, List<Layer> layers, Map<Integer, List<OutputEdge>> outs) {

        Map<Integer, List<BezierEdgeWrapper>> bewMap = bewList.stream()
                .collect(Collectors.groupingBy(b -> b.layer));

        for (Map.Entry<Integer, List<BezierEdgeWrapper>> entry : bewMap.entrySet()) {

            int id = 0;
            for (BezierEdgeWrapper bew : entry.getValue()) {
                bew.id = id++;
            }

            int layer = entry.getKey();
            Map<Integer, List<Fragment>> splittedEdges = new HashMap<>();

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

            Map<BezierEdgeWrapper, List<Double>> splitPointsMap = new LinkedHashMap<>();
            List<OverlapPair> overlapPairs = new ArrayList<>();

            Sweep sweep = new Sweep();
            for (BezierEdgeWrapper bew : entry.getValue()) {
                if (bewsToIgnore.contains(bew)) {
                    continue;
                }
                sweep.addEdge(bew);
            }
            sweep.run();
            splitPointsMap = sweep.splitPoints;
            overlapPairs = sweep.overlapPairs;

            List<BezierEdgeWrapper> splittedBewList = new ArrayList<>(splitPointsMap.keySet());
            List<Intersection> intersectionsList = new ArrayList<>();

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

            Set<Integer> splittedIds = new HashSet<>();

            for (int i = splittedBewList.size() - 1; i >= 0; i--) {
                BezierEdgeWrapper bew = splittedBewList.get(i);
                splittedIds.add(bew.id);
                
                if (bew.be.getBeginPoint().getX() == 2755.0) {
                    System.err.println("xxx");
                }

                final double epsT = 1e-9; // param eps
                
                List<Double> splitT = splitPointsMap.get(bew);

                splitT.sort((a, b) -> Double.compare(a, b));

                BezierEdge be = bew.be;
                List<Double> realSplitT = new ArrayList<>();
                for (Double t : splitT) {
                    if (t < epsT || t >= 1.0 - epsT) {
                        continue;
                    }

                    realSplitT.add(t);
                }

                if (realSplitT.isEmpty()) {
                    splittedIds.remove(bew.id);
                    continue;
                }

                if (splitT.get(0) != 0.0) {
                    splitT.add(0, 0.0);
                }
                if (splitT.get(splitT.size() - 1) != 1.0) {
                    splitT.add(1.0);
                }

                List<Double> uniq = new ArrayList<>();
                double prev = Double.NaN;
                
                for (Double t : splitT) {
                    if (uniq.isEmpty() || Math.abs(t - prev) > epsT) {
                        uniq.add(Math.max(0, Math.min(1, t)));
                        prev = t;
                        Point2D pat = bew.be.pointAt(t);
                        intersectionsList.add(new Intersection(bew.id, t, pat.getX(), pat.getY()));
                    }
                }

                List<Fragment> frs = new ArrayList<>();
                for (int j = 0; j + 1 < uniq.size(); j++) {
                    double t0 = uniq.get(j);
                    double t1 = uniq.get(j + 1);
                    if (t1 - t0 <= epsT) {
                        continue; // skip zero-length
                    }
                    frs.add(new Fragment(new Edge(bew.id, bew.be, bew.pathIndex), t0, t1, false, bew.pathIndex));
                }
                splittedEdges.put(bew.id, frs);
            }

            Map<Integer, List<FragRef>> comps = buildOverlapComponents(splittedEdges, overlapPairs);

            List<OutputEdge> outSplitted = normalizeOverlaps(splittedEdges, comps);

            remapIntersections(intersectionsList, splittedEdges, comps, outSplitted, 1);

            List<OutputEdge> allOut = new ArrayList<>();
            System.err.println("NON Splitted edges:");
            for (BezierEdgeWrapper bew : entry.getValue()) {
                if (!splittedIds.contains(bew.id)) {
                    allOut.add(new OutputEdge(new Edge(bew.id, bew.be, bew.pathIndex), bew.pathIndex));
                    System.err.println(bew.be.toSvg());
                }
            }

            System.err.println("Splitted edges:");
            for (OutputEdge ie : outSplitted) {
                System.err.println("" + ie.geom.be.toSvg());
            }

            allOut.addAll(outSplitted);

            snapEndpoints(allOut, 2);
            
            outs.put(layer, allOut);
        }
    }

    public static final class Intersection {

        public final int edgeId;
        public final double t; // original param on that edge
        public final double x, y;

        public Intersection(int e, double t, double x, double y) {
            this.edgeId = e;
            this.t = t;
            this.x = x;
            this.y = y;
        }
    }

    public static final class IntersectionOut {

        public final OutputEdge edge; // where it ended up
        public final double u;        // local param in [0,1] on edge.geom
        public final double x, y;     // (optional) exact point

        public IntersectionOut(OutputEdge e, double u, double x, double y) {
            this.edge = e;
            this.u = u;
            this.x = x;
            this.y = y;
        }
    }

    public static List<IntersectionOut> remapIntersections(
            List<Intersection> oldInts,
            Map<Integer, List<Fragment>> fragsPerEdge,
            Map<Integer, List<FragRef>> comps,
            List<OutputEdge> outputs,
            double tol) {

        // Build fast lookup: for each original fragment tell which OutputEdge instance represents it (by fillStyle).
        // Multiple participants in a component create multiple OutputEdges with same geometry; we pick by matching fillStyle.
        Map<String, OutputEdge> rep = new HashMap<>();

        // Mark components first
        Set<String> compMembers = new HashSet<>();
        for (Map.Entry<Integer, List<FragRef>> e : comps.entrySet()) {
            List<FragRef> refs = e.getValue();
            // canonical geometry is from min ref:
            FragRef canRef = refs.stream()
                    .min((a, b) -> {
                        int c = Integer.compare(a.edgeId, b.edgeId);
                        return c != 0 ? c : Integer.compare(a.idxInEdge, b.idxInEdge);
                    }).get();
            Fragment canFrag = fragsPerEdge.get(canRef.edgeId).get(canRef.idxInEdge);
            Edge canonical = canFrag.toEdge();

            // find all OutputEdges built from this component:
            List<OutputEdge> outs = new ArrayList<>();
            for (OutputEdge oe : outputs) {
                // Heuristic: same object reference? Not guaranteed. Use geometry equality within tol.
                if (sameGeometry(oe.geom, canonical, tol)) {
                    outs.add(oe);
                }
            }
            // Map each member (edgeId:idx) to corresponding OutputEdge by fill style
            for (FragRef r : refs) {
                Fragment f = fragsPerEdge.get(r.edgeId).get(r.idxInEdge);
                compMembers.add(r.edgeId + ":" + r.idxInEdge);
                OutputEdge match = outs.stream()
                        .filter(oe -> oe.pathIndex == f.pathIndex)
                        .findFirst().orElse(null);
                if (match != null) {
                    rep.put(r.edgeId + ":" + r.idxInEdge, match);
                }
            }
        }

        // For non-overlap fragments, map to their own OutputEdge (geometry equals fragment)
        for (Map.Entry<Integer, List<Fragment>> e : fragsPerEdge.entrySet()) {
            int edgeId = e.getKey();
            List<Fragment> L = e.getValue();
            for (int i = 0; i < L.size(); i++) {
                String key = edgeId + ":" + i;
                if (compMembers.contains(key)) {
                    continue;
                }
                Fragment f = L.get(i);
                // Find OutputEdge with same geometry & fill
                OutputEdge match = outputs.stream()
                        .filter(oe -> oe.pathIndex == f.pathIndex && sameGeometry(oe.geom, f.toEdge(), tol))
                        .findFirst().orElse(null);
                if (match != null) {
                    rep.put(key, match);
                }
            }
        }

        // Now convert each original intersection
        List<IntersectionOut> result = new ArrayList<>();
        for (Intersection in : oldInts) {
            List<Fragment> frs = fragsPerEdge.get(in.edgeId);
            if (frs == null) {
                continue;
            }
            // locate fragment [t0,t1] containing t
            Fragment f = locateFragment(frs, in.t);
            if (f == null) {
                continue;
            }
            OutputEdge dst = rep.get(in.edgeId + ":" + frs.indexOf(f));
            if (dst == null) {
                continue;
            }
            // convert original t to local u on fragment
            double uLocal = (in.t - f.t0) / (f.t1 - f.t0);
            if (f.reversed) {
                uLocal = 1.0 - uLocal;
            }

            result.add(new IntersectionOut(dst, uLocal, in.x, in.y));
        }

        // optional: deduplicate by (dst, rounded point)
        return result;
    }

    static Fragment locateFragment(List<Fragment> fs, double t) {
        // Binary search over sorted fragments by t0/t1
        int lo = 0, hi = fs.size() - 1;
        final double eps = 1e-12;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            Fragment f = fs.get(mid);
            if (t < f.t0 - eps) {
                hi = mid - 1;
            } else if (t > f.t1 + eps) {
                lo = mid + 1;
            } else {
                return f;
            }
        }
        return null;
    }

    static boolean sameGeometry(Edge a, Edge b, double tol) {
        // As with geometricallyCoincident but cheaper thresholds.
        return geometricallyCoincident(
                new Fragment(a, 0, 1, false, a.pathIndex),
                new Fragment(b, 0, 1, false, b.pathIndex),
                tol);
    }

    public static List<OutputEdge> normalizeOverlaps(
            Map<Integer, List<Fragment>> fragsPerEdge,
            Map<Integer, List<FragRef>> comps) {

        List<OutputEdge> out = new ArrayList<>();
        // Keep a mark for fragments which belong to any overlap component
        Set<String> inComp = new HashSet<>();
        for (List<FragRef> refs : comps.values()) {
            // pick canonical: smallest (edgeId, idx)
            FragRef canRef = refs.stream()
                    .min((a, b) -> {
                        int c = Integer.compare(a.edgeId, b.edgeId);
                        return c != 0 ? c : Integer.compare(a.idxInEdge, b.idxInEdge);
                    }).get();
            Fragment canFrag = fragsPerEdge.get(canRef.edgeId).get(canRef.idxInEdge);
            Edge canonicalGeom = canFrag.toEdge();

            for (FragRef r : refs) {
                Fragment f = fragsPerEdge.get(r.edgeId).get(r.idxInEdge);
                inComp.add(r.edgeId + ":" + r.idxInEdge);

                // Align orientation (optional – if you require identical direction):
                Edge g = canonicalGeom;
                // If you must ensure f's direction equals canonical, just use canonical as-is.
                // If you want "two same segments", the geometry object can literally be same.

                out.add(new OutputEdge(g, f.pathIndex));
            }
        }

        // Non-overlapped fragments go through unchanged
        for (Map.Entry<Integer, List<Fragment>> e : fragsPerEdge.entrySet()) {
            List<Fragment> fs = e.getValue();
            for (int i = 0; i < fs.size(); i++) {
                String key = e.getKey() + ":" + i;
                if (inComp.contains(key)) {
                    continue; // already replaced
                }
                Fragment f = fs.get(i);
                out.add(new OutputEdge(f.toEdge(), f.pathIndex));
            }
        }
        return out;
    }

    public static class OutputEdge {

        public final Edge geom;     // canonical geometry
        public final int pathIndex; // one per participant (you will have N of these)

        public OutputEdge(Edge g, int f) {
            this.geom = g;
            this.pathIndex = f;
        }
    }

    private static Map<Integer, List<FragRef>> buildOverlapComponents(
            Map<Integer, List<Fragment>> fragsPerEdge,
            List<OverlapPair> overlaps) {

        // Index all fragments by a running id for DSU
        List<FragRef> allRefs = new ArrayList<>();
        Map<Integer, Integer> baseIndex = new HashMap<>(); // edgeId -> start index
        int cnt = 0;
        for (Map.Entry<Integer, List<Fragment>> e : fragsPerEdge.entrySet()) {
            baseIndex.put(e.getKey(), cnt);
            for (int i = 0; i < e.getValue().size(); i++) {
                allRefs.add(new FragRef(e.getKey(), i));
            }
            cnt += e.getValue().size();
        }

        DSU dsu = new DSU(cnt);

        // For each reported overlap, link fragments whose [t0,t1] lies inside.
        for (OverlapPair op : overlaps) {
            List<Fragment> A = fragsPerEdge.get(op.edgeA);
            List<Fragment> B = fragsPerEdge.get(op.edgeB);
            if (A == null || B == null) {
                continue;
            }

            double aMin = Math.min(op.a0, op.a1);
            double aMax = Math.max(op.a0, op.a1);
            double bMin = Math.min(op.b0, op.b1);
            double bMax = Math.max(op.b0, op.b1);

            final double epsT = 1e-9;

            for (int i = 0; i < A.size(); i++) {
                Fragment fa = A.get(i);
                if (fa.t0 + epsT >= aMax || fa.t1 - epsT <= aMin) {
                    continue; // no overlap in param
                }
                for (int j = 0; j < B.size(); j++) {
                    Fragment fb = B.get(j);
                    if (fb.t0 + epsT >= bMax || fb.t1 - epsT <= bMin) {
                        continue;
                    }

                    // Optional: geometric check with tolerance to avoid false positives:
                    if (!geometricallyCoincident(fa, fb, /*tol=*/ 10)) {
                        continue;
                    }

                    int ia = baseIndex.get(op.edgeA) + i;
                    int ib = baseIndex.get(op.edgeB) + j;
                    dsu.u(ia, ib);
                }
            }
        }

        // Gather components
        Map<Integer, List<FragRef>> comps = new HashMap<>();
        for (int k = 0; k < allRefs.size(); k++) {
            int root = dsu.f(k);
            comps.computeIfAbsent(root, key -> new ArrayList<>()).add(allRefs.get(k));
        }
        return comps;
    }

    // Geometric equality test within tolerance using sampling
    static boolean geometricallyCoincident(Fragment f1, Fragment f2, double tol) {
        // If line-line: check collinearity + projection overlap more strictly.
        // For general case: symmetric sample Hausdorff approx.
        final int S = 24;
        double maxd = 0;
        for (int i = 0; i <= S; i++) {
            double u = (double) i / S;
            Point2D p = f1.toEdge().be.pointAt(u);
            double d = distancePointToEdge(p, f2.toEdge());
            maxd = Math.max(maxd, d);
            if (maxd > tol) {
                return false;
            }
        }
        for (int i = 0; i <= S; i++) {
            double u = (double) i / S;
            Point2D p = f2.toEdge().be.pointAt(u);
            double d = distancePointToEdge(p, f1.toEdge());
            maxd = Math.max(maxd, d);
            if (maxd > tol) {
                return false;
            }
        }
        return true;
    }

// Distance from point to edge (line or quadratic), via projection / sampling.
    static double distancePointToEdge(Point2D p, Edge e) {
        return e.be.distanceFromPoint(p);
    }

    public static void snapEndpoints(List<OutputEdge> edges, double snapTol) {
        // Collect all endpoints
        List<Point2D> pts = new ArrayList<>();
        for (OutputEdge e : edges) {
            Point2D a = e.geom.be.getBeginPoint();
            Point2D c = e.geom.be.getControlPoint();
            Point2D b = e.geom.be.getEndPoint();
            pts.add(a);
            pts.add(c);
            pts.add(b);
        }

        // Build clusters of points within snapTol (simple O(n^2) is often OK; use grid hash if needed)
        int n = pts.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        java.util.function.IntUnaryOperator find = new java.util.function.IntUnaryOperator() {
            @Override
            public int applyAsInt(int x) {
                return parent[x] == x ? x : (parent[x] = applyAsInt(parent[x]));
            }
        };
        java.util.function.BiConsumer<Integer, Integer> unite = (ii, jj) -> {
            int i = find.applyAsInt(ii), j = find.applyAsInt(jj);
            if (i != j) {
                parent[j] = i;
            }
        };

        double tol2 = snapTol * snapTol;
        for (int i = 0; i < n; i++) {
            Point2D pi = pts.get(i);
            for (int j = i + 1; j < n; j++) {
                Point2D pj = pts.get(j);
                double dx = pi.getX() - pj.getX();
                double dy = pi.getY() - pj.getY();
                if (dx * dx + dy * dy <= tol2) {
                    unite.accept(i, j);
                }
            }
        }

        // Compute cluster representatives as arithmetic mean for stability
        Map<Integer, Point2D> rep = new HashMap<>();
        Map<Integer, Integer> cnt = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int r = find.applyAsInt(i);
            Point2D p = pts.get(i);
            rep.compute(r, (k, v) -> {
                if (v == null) {
                    return new Point2D.Double(p.getX(), p.getY());
                }
                v.setLocation(v.getX() + p.getX(), v.getY() + p.getY());
                return v;
            });
            cnt.merge(r, 1, Integer::sum);
        }
        for (Map.Entry<Integer, Point2D> e : rep.entrySet()) {
            int c = cnt.get(e.getKey());
            e.getValue().setLocation(e.getValue().getX() / c, e.getValue().getY() / c);;            
        }

        // Write snapped endpoints back into edges
        int idx = 0;
        for (OutputEdge e : edges) {
            int rA = find.applyAsInt(idx++);
            int rC = find.applyAsInt(idx++);
            int rB = find.applyAsInt(idx++);
            Point2D A = rep.get(rA);
            Point2D C = rep.get(rC);
            Point2D B = rep.get(rB);
            // Rebuild edge with snapped endpoints (keep control point for quads via De Casteljau split at 0/1)
            if (!e.geom.be.isQuad()) {
                // create new LineEdge with same fill/id but snapped ends
                e.geom.be.setBeginPoint(A);
                e.geom.be.setEndPoint(B);
            } else {
                double dx0 = A.getX() - e.geom.be.getBeginPoint().getX();
                double dy0 = A.getY() - e.geom.be.getBeginPoint().getY();
                double dx1 = B.getX() - e.geom.be.getEndPoint().getX();
                double dy1 = B.getY() - e.geom.be.getEndPoint().getY();
                
                e.geom.be.setBeginPoint(A);
                /*e.geom.be.setControlPoint(new Point2D.Double(
                        e.geom.be.getControlPoint().getX() + 0.5 * (dx0 + dx1),
                        e.geom.be.getControlPoint().getY() + 0.5 * (dy0 + dy1)
                ));*/
                e.geom.be.setControlPoint(C);
                e.geom.be.setEndPoint(B);                
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

        Map<Integer, List<OutputEdge>> outStrokes = new HashMap<>();
        Map<Integer, List<OutputEdge>> outFills = new HashMap<>();

        handleBewList(strokesBewList, layers, outStrokes);
        handleBewList(fillsBewList, layers, outFills);

        Map<Integer, List<OutputEdge>> outAll = outFills;
        for (int layer : outStrokes.keySet()) {
            if (!outAll.containsKey(layer)) {
                outAll.put(layer, new ArrayList<>());
            }
            outAll.get(layer).addAll(outStrokes.get(layer));
        }

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            Map<Integer, List<OutputEdge>> pathIndexToEdge = outAll.get(i).stream()
                    .collect(Collectors.groupingBy(b -> b.pathIndex));
            for (int p = 0; p < layer.paths.size(); p++) {
                Path path = layer.paths.get(p);
                path.edges.clear();
                if (pathIndexToEdge.containsKey(p)) {
                    for (OutputEdge oe : pathIndexToEdge.get(p)) {
                        path.edges.add(oe.geom.be);
                    }
                }
            }
            for (int p = 0; p < layer.paths.size(); p++) {
                if (layer.paths.get(p).edges.isEmpty()) {
                    layer.paths.remove(p);
                    p--;
                }
            }
        }

        /*for (int layer = 0; layer < layers.size(); layer++) {
            for (int p = 0; p < layers.get(layer).paths.size(); p++) {
                Path path = layers.get(layer).paths.get(p);
                for (int e = 0; e < path.edges.size(); e++) {
                    BezierEdge be1 = path.edges.get(e);
                    be1.shrinkToLine();
                }
            }
        }*/
    }

    private static class Edge {

        int id;
        BezierEdge be;
        int pathIndex;

        public Edge(int id, BezierEdge be, int fillStyle) {
            this.id = id;
            this.be = be;
            this.pathIndex = fillStyle;
        }

    }

    private static class Fragment {

        Edge source;
        double t0;
        double t1;
        boolean reversed;
        int pathIndex;

        public Fragment(Edge source, double t0, double t1, boolean reversed, int fillStyle) {
            this.source = source;
            this.t0 = t0;
            this.t1 = t1;
            this.reversed = reversed;
            this.pathIndex = fillStyle;
        }

        public Edge toEdge() {
            return new Edge(source.id, toBezierEdge(), pathIndex);
        }

        public BezierEdge toBezierEdge() {
            if (t0 == 0.0) {
                if (t1 == 1.0) {
                    return source.be;
                }
                return source.be.split(Arrays.asList(t1)).get(0);
            }
            if (t1 == 1.0) {
                return source.be.split(Arrays.asList(t0)).get(1);
            }

            return source.be.split(Arrays.asList(t0, t1)).get(1);
        }

    }

    private static class OverlapPair {

        int edgeA;
        int edgeB;
        double a0;
        double a1;
        double b0;
        double b1; // can be a0>a1 or b0>b1 if opposite direction

        public OverlapPair(int edgeA, int edgeB, double a0, double a1, double b0, double b1) {
            this.edgeA = edgeA;
            this.edgeB = edgeB;
            this.a0 = a0;
            this.a1 = a1;
            this.b0 = b0;
            this.b1 = b1;
        }
    }

    // Tiny union-find
    static final class DSU {

        int[] p;
        int[] r;

        DSU(int n) {
            p = new int[n];
            r = new int[n];
            for (int i = 0; i < n; i++) {
                p[i] = i;
            }
        }

        int f(int x) {
            return p[x] == x ? x : (p[x] = f(p[x]));
        }

        void u(int a, int b) {
            a = f(a);
            b = f(b);
            if (a == b) {
                return;
            }
            if (r[a] < r[b]) {
                int t = a;
                a = b;
                b = t;
            }
            p[b] = a;
            if (r[a] == r[b]) {
                r[a]++;
            }
        }
    }

    static final class FragRef {

        final int edgeId;
        final int idxInEdge; // index in that edge's fragment list

        FragRef(int e, int i) {
            edgeId = e;
            idxInEdge = i;
        }
    }

    public static void main(String[] args) {
        OverlappingEdgesSplitter sp = new OverlappingEdgesSplitter();
        List<Layer> layers = new ArrayList<>();
        Layer lay = new Layer();
        lay.fillStyleArray = new FILLSTYLEARRAY();
        lay.lineStyleArray = new LINESTYLEARRAY();
        lay.paths = new ArrayList<>();
        Path p = new Path();
        
        //M 105 1238 Q -84 1185 -273 1133
        //M -456 1085L -452 1087Q -229 1144 -49 1195
        
        p.edges.add(new BezierEdge(105,1238 , -84 ,1185 ,-273, 1133));

        p.edges.add(new BezierEdge(-456, 1085, -452, 1087));
        p.edges.add(new BezierEdge(-452,1087, -229, 1144, -49, 1195));
        lay.paths.add(p);
        layers.add(lay);

        System.err.println("BEFORE:");
        for (Path path : layers.get(0).paths) {
            System.err.println("" + path.toString());
        }
        System.err.println("-----------------");
        sp.splitOverlappingEdges(layers);

        
        System.err.println("AFTER:");
        System.err.println("----------------");
        for (Path path : layers.get(0).paths) {
            System.err.println("" + path.toString());
        }
    }
}
