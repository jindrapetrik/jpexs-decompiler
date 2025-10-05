package com.jpexs.decompiler.flash.math;

/**
 *
 * @author JPEXS
 */
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QuadOverlap {

    /** Polyline with parameter values for each vertex. */
    private static class ParamPoly {
        final List<Point2D.Double> pts = new ArrayList<>();
        final List<Double> params = new ArrayList<>();
    }

    /** Public entry: find overlapping sub-curve intervals within tolerance. */
    public static List<OverlapInterval> findQuadraticOverlaps(
            Point2D.Double A0, Point2D.Double A1, Point2D.Double A2,
            Point2D.Double B0, Point2D.Double B1, Point2D.Double B2,
            double tol) {

        // Tolerances: tune as needed
        final double flatTol = tol * 0.5;   // Hausdorff bound for flattening
        final double distTol = tol;         // max allowed normal offset between near-collinear segments
        final double angleTol = Math.toRadians(1);//0.01;       // radians; ~0.057° — treat as parallel if below

        ParamPoly polyA = flattenQuad(A0, A1, A2, flatTol);
        ParamPoly polyB = flattenQuad(B0, B1, B2, flatTol);

        List<OverlapInterval> raw = overlapPolylines(polyA, polyB, distTol, angleTol);
        return mergeIntervals(raw, 1e-6, 1e-6);
    }

    // ---------- Step 1: Adaptive flattening with param tracking ----------

    /** Flatten quadratic Bézier via recursive de Casteljau, recording t at each vertex. */
    private static ParamPoly flattenQuad(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2, double flatTol) {
        ParamPoly out = new ParamPoly();
        // Start with first vertex
        out.pts.add(new Point2D.Double(P0.x, P0.y));
        out.params.add(0.0);
        // Recursive subdivision stack
        subdivideQuad(P0, P1, P2, 0.0, 1.0, flatTol, out);
        // Ensure last vertex
        if (out.params.get(out.params.size() - 1) < 1.0 - 1e-15) {
            out.pts.add(new Point2D.Double(P2.x, P2.y));
            out.params.add(1.0);
        }
        return out;
    }

    /** Subdivide until control polygon is flat enough. */
    private static void subdivideQuad(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2,
                                      double t0, double t1, double flatTol, ParamPoly out) {
        if (quadFlatEnough(P0, P1, P2, flatTol)) {
            // Append end point and parameter
            out.pts.add(new Point2D.Double(P2.x, P2.y));
            out.params.add(t1);
            return;
        }
        // de Casteljau split at t=0.5
        SplitRes s = splitQuadHalf(P0, P1, P2);
        double tm = (t0 + t1) * 0.5;
        subdivideQuad(P0, s.P01, s.P012, t0, tm, flatTol, out);
        subdivideQuad(s.P012, s.P12, P2, tm, t1, flatTol, out);
    }

    /** Flatness test: distance of control point to baseline. */
    private static boolean quadFlatEnough(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2, double tol) {
        double d = distPointToSegment(P1, P0, P2);
        return d <= tol;
    }

    private static class SplitRes {
        Point2D.Double P01, P12, P012;
    }

    /** Split quadratic at 0.5 via de Casteljau. */
    private static SplitRes splitQuadHalf(Point2D.Double P0, Point2D.Double P1, Point2D.Double P2) {
        Point2D.Double P01 = lerp(P0, P1, 0.5);
        Point2D.Double P12 = lerp(P1, P2, 0.5);
        Point2D.Double P012 = lerp(P01, P12, 0.5);
        SplitRes r = new SplitRes();
        r.P01 = P01; r.P12 = P12; r.P012 = P012;
        return r;
    }

    private static Point2D.Double lerp(Point2D.Double a, Point2D.Double b, double t) {
        return new Point2D.Double(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
    }

    // ---------- Step 2: Segment-segment near-coincidence with param mapping ----------

    private static List<OverlapInterval> overlapPolylines(ParamPoly A, ParamPoly B, double distTol, double angleTol) {
        List<OverlapInterval> out = new ArrayList<>();
        for (int i = 0; i + 1 < A.pts.size(); i++) {
            Point2D.Double a0 = A.pts.get(i), a1 = A.pts.get(i + 1);
            double ta0 = A.params.get(i), ta1 = A.params.get(i + 1);

            // Precompute direction and length for A
            double ax = a1.x - a0.x, ay = a1.y - a0.y;
            double alen = Math.hypot(ax, ay);
            if (alen == 0) continue;
            double ux = ax / alen, uy = ay / alen; // unit dir

            for (int j = 0; j + 1 < B.pts.size(); j++) {
                Point2D.Double b0 = B.pts.get(j), b1 = B.pts.get(j + 1);
                double ub0 = B.params.get(j), ub1 = B.params.get(j + 1);

                double bx = b1.x - b0.x, by = b1.y - b0.y;
                double blen = Math.hypot(bx, by);
                if (blen == 0) continue;
                double vx = bx / blen, vy = by / blen;

                // Angle test: |sin(theta)| = |ux*vy - uy*vx|
                double sinTh = Math.abs(ux * vy - uy * vx);
                if (sinTh > angleTol) continue;

                // Normal offset (distance between supporting lines) test
                // Compute signed distance of b0 to line A
                double nx = -uy, ny = ux; // left normal of A
                double off = ((b0.x - a0.x) * nx + (b0.y - a0.y) * ny);
                if (Math.abs(off) > distTol) continue;

                // Project endpoints onto A direction to get 1D intervals
                double a0s = 0.0, a1s = alen;
                double b0s = (b0.x - a0.x) * ux + (b0.y - a0.y) * uy;
                double b1s = (b1.x - a0.x) * ux + (b1.y - a0.y) * uy;

                // Normalize B's interval direction (ensure b0s <= b1s)
                double bbMin = Math.min(b0s, b1s);
                double bbMax = Math.max(b0s, b1s);

                double s0 = Math.max(a0s, bbMin);
                double s1 = Math.min(a1s, bbMax);
                if (s1 <= s0) continue; // no overlap along the axis

                // Convert 1D s back to points on A-line, then to params on A/B segments
                // Fraction along A segment:
                double fa0 = clamp01((s0 - a0s) / (a1s - a0s));
                double fa1 = clamp01((s1 - a0s) / (a1s - a0s));

                // For B: we need to know which endpoint was smaller
                boolean bIncreasing = b0s <= b1s;
                double fb0 = (s0 - (bIncreasing ? b0s : b1s)) / (Math.abs(b1s - b0s));
                double fb1 = (s1 - (bIncreasing ? b0s : b1s)) / (Math.abs(b1s - b0s));
                fb0 = clamp01(fb0); fb1 = clamp01(fb1);

                // Map fractions to global t/u via linear interpolation of per-vertex params
                double tStart = lerp(ta0, ta1, fa0);
                double tEnd   = lerp(ta0, ta1, fa1);
                double uStart = lerp(ub0, ub1, bIncreasing ? fb0 : (1.0 - fb0));
                double uEnd   = lerp(ub0, ub1, bIncreasing ? fb1 : (1.0 - fb1));

                out.add(new OverlapInterval(tStart, tEnd, uStart, uEnd));
            }
        }
        return out;
    }

    private static double clamp01(double x) {
        if (x < 0) return 0;
        if (x > 1) return 1;
        return x;
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // ---------- Step 3: Merge touching intervals ----------

    /** Merge intervals that touch within tolerances. */
    private static List<OverlapInterval> mergeIntervals(List<OverlapInterval> in, double tTol, double uTol) {
        if (in.isEmpty()) return in;
        // Sort by t0 then u0
        Collections.sort(in, (p, q) -> {
            int c = Double.compare(p.t0, q.t0);
            if (c != 0) return c;
            return Double.compare(p.u0, q.u0);
        });
        List<OverlapInterval> out = new ArrayList<>();
        OverlapInterval cur = in.get(0);
        for (int i = 1; i < in.size(); i++) {
            OverlapInterval nx = in.get(i);
            if (Math.abs(nx.t0 - cur.t1) <= tTol && Math.abs(nx.u0 - cur.u1) <= uTol) {
                // Extend
                cur = new OverlapInterval(cur.t0, Math.max(cur.t1, nx.t1), cur.u0, Math.max(cur.u1, nx.u1));
            } else {
                out.add(cur);
                cur = nx;
            }
        }
        out.add(cur);
        return out;
    }

    // ---------- Geometry helpers ----------

    /** Distance of point C to segment AB. */
    private static double distPointToSegment(Point2D.Double C, Point2D.Double A, Point2D.Double B) {
        double vx = B.x - A.x, vy = B.y - A.y;
        double wx = C.x - A.x, wy = C.y - A.y;
        double vv = vx * vx + vy * vy;
        if (vv == 0) return Math.hypot(wx, wy);
        double t = (wx * vx + wy * vy) / vv;
        if (t <= 0) return Math.hypot(wx, wy);
        if (t >= 1) return Math.hypot(C.x - B.x, C.y - B.y);
        double px = A.x + t * vx, py = A.y + t * vy;
        return Math.hypot(C.x - px, C.y - py);
    }
}
