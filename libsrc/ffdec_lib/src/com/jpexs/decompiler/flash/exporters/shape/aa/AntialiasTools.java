package com.jpexs.decompiler.flash.exporters.shape.aa;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AntialiasTools {

    public static Shape contoursToShape(List<List<Vec2>> contours, int windingRule, boolean close) {
        GeneralPath path = new GeneralPath(windingRule);

        if (contours == null) {
            return path;
        }

        for (List<Vec2> contour : contours) {
            if (contour == null || contour.isEmpty()) {
                continue;
            }

            Vec2 first = contour.get(0);
            path.moveTo((float) first.x, (float) first.y);

            for (int i = 1; i < contour.size(); i++) {
                Vec2 p = contour.get(i);
                path.lineTo((float) p.x, (float) p.y);
            }

            if (close) {
                path.closePath();
            }
        }

        return path;
    }

    public static List<List<Vec2>> shapeToContours(Shape shape, double flatness) {
        List<List<Vec2>> contours = new ArrayList<List<Vec2>>();

        if (shape == null) {
            return contours;
        }

        PathIterator raw = shape.getPathIterator(null);
        FlatteningPathIterator it = new FlatteningPathIterator(raw, flatness);

        double[] coords = new double[6];

        List<Vec2> current = null;
        Vec2 moveStart = null;
        Vec2 last = null;

        while (!it.isDone()) {
            int type = it.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO: {
                    if (current != null && current.size() >= 2) {
                        finishContour(contours, current, moveStart);
                    }

                    current = new ArrayList<Vec2>();
                    moveStart = new Vec2(coords[0], coords[1]);
                    current.add(moveStart);
                    last = moveStart;
                    break;
                }

                case PathIterator.SEG_LINETO: {
                    if (current == null) {
                        current = new ArrayList<Vec2>();
                        moveStart = new Vec2(coords[0], coords[1]);
                        current.add(moveStart);
                        last = moveStart;
                    } else {
                        Vec2 p = new Vec2(coords[0], coords[1]);
                        if (!samePoint(last, p, 1e-12)) {
                            current.add(p);
                            last = p;
                        }
                    }
                    break;
                }

                case PathIterator.SEG_CLOSE: {
                    if (current != null) {
                        finishContour(contours, current, moveStart);
                        current = null;
                        moveStart = null;
                        last = null;
                    }
                    break;
                }

                default:
                    break;
            }

            it.next();
        }

        if (current != null && current.size() >= 2) {
            finishContour(contours, current, moveStart);
        }

        return contours;
    }

    private static void finishContour(List<List<Vec2>> contours, List<Vec2> current, Vec2 moveStart) {
        if (current == null || current.size() < 2) {
            return;
        }

        List<Vec2> contour = new ArrayList<Vec2>(current);

        if (moveStart != null && !samePoint(contour.get(contour.size() - 1), moveStart, 1e-12)) {
            contour.add(moveStart);
        }

        if (contour.size() >= 2 && samePoint(contour.get(0), contour.get(contour.size() - 1), 1e-12)) {
            contour.remove(contour.size() - 1);
        }

        if (contour.size() >= 3) {
            contours.add(contour);
        }
    }

    public static List<Vec2> removeDuplicateAndCollinear(List<Vec2> pts, double eps) {
        List<Vec2> a = new ArrayList<Vec2>();
        if (pts == null || pts.isEmpty()) {
            return a;
        }

        for (int i = 0; i < pts.size(); i++) {
            Vec2 p = pts.get(i);
            if (a.isEmpty() || !samePoint(a.get(a.size() - 1), p, eps)) {
                a.add(p);
            }
        }

        if (a.size() > 1 && samePoint(a.get(0), a.get(a.size() - 1), eps)) {
            a.remove(a.size() - 1);
        }

        boolean changed = true;
        while (changed && a.size() >= 3) {
            changed = false;
            for (int i = 0; i < a.size(); i++) {
                Vec2 prev = a.get((i - 1 + a.size()) % a.size());
                Vec2 curr = a.get(i);
                Vec2 next = a.get((i + 1) % a.size());

                if (Math.abs(triangleArea2(prev, curr, next)) <= eps
                        && dot(curr.x - prev.x, curr.y - prev.y, next.x - curr.x, next.y - curr.y) >= 0.0) {
                    a.remove(i);
                    changed = true;
                    break;
                }
            }
        }

        return a;
    }

    public static boolean samePoint(Vec2 a, Vec2 b, double eps) {
        return Math.abs(a.x - b.x) <= eps && Math.abs(a.y - b.y) <= eps;
    }

    public static double dot(double ax, double ay, double bx, double by) {
        return ax * bx + ay * by;
    }

    public static double triangleArea2(Vec2 a, Vec2 b, Vec2 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    public static List<Vec2> flattenCubicBezier(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, double tolerance) {
        List<Vec2> out = new ArrayList<Vec2>();
        out.add(p0);
        flattenCubicRecursive(p0, p1, p2, p3, tolerance * tolerance, out, 0);
        return out;
    }

    public static void flattenCubicRecursive(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3,
            double tol2, List<Vec2> out, int depth) {
        if (depth > 20 || cubicFlatEnough(p0, p1, p2, p3, tol2)) {
            out.add(p3);
            return;
        }

        Vec2 p01 = midpoint(p0, p1);
        Vec2 p12 = midpoint(p1, p2);
        Vec2 p23 = midpoint(p2, p3);
        Vec2 p012 = midpoint(p01, p12);
        Vec2 p123 = midpoint(p12, p23);
        Vec2 p0123 = midpoint(p012, p123);

        flattenCubicRecursive(p0, p01, p012, p0123, tol2, out, depth + 1);
        flattenCubicRecursive(p0123, p123, p23, p3, tol2, out, depth + 1);
    }

    public static boolean cubicFlatEnough(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, double tol2) {
        double d1 = pointLineDistanceSq(p1, p0, p3);
        double d2 = pointLineDistanceSq(p2, p0, p3);
        return Math.max(d1, d2) <= tol2;
    }

    public static List<Vec2> flattenQuadraticBezier(Vec2 p0, Vec2 p1, Vec2 p2, double tolerance) {
        List<Vec2> out = new ArrayList<Vec2>();
        out.add(p0);
        flattenQuadraticRecursive(p0, p1, p2, tolerance * tolerance, out, 0);
        return out;
    }

    public static void flattenQuadraticRecursive(Vec2 p0, Vec2 p1, Vec2 p2,
            double tol2, List<Vec2> out, int depth) {
        if (depth > 20 || quadraticFlatEnough(p0, p1, p2, tol2)) {
            out.add(p2);
            return;
        }

        Vec2 p01 = midpoint(p0, p1);
        Vec2 p12 = midpoint(p1, p2);
        Vec2 p012 = midpoint(p01, p12);

        flattenQuadraticRecursive(p0, p01, p012, tol2, out, depth + 1);
        flattenQuadraticRecursive(p012, p12, p2, tol2, out, depth + 1);
    }

    public static boolean quadraticFlatEnough(Vec2 p0, Vec2 p1, Vec2 p2, double tol2) {
        double d = pointLineDistanceSq(p1, p0, p2);
        return d <= tol2;
    }

    public static double pointLineDistanceSq(Vec2 p, Vec2 a, Vec2 b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double len2 = dx * dx + dy * dy;

        if (len2 == 0.0) {
            double ex = p.x - a.x;
            double ey = p.y - a.y;
            return ex * ex + ey * ey;
        }

        double t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / len2;
        double qx = a.x + t * dx;
        double qy = a.y + t * dy;
        double ex = p.x - qx;
        double ey = p.y - qy;
        return ex * ex + ey * ey;
    }

    public static Vec2 midpoint(Vec2 a, Vec2 b) {
        return new Vec2((a.x + b.x) * 0.5, (a.y + b.y) * 0.5);
    }

    public static void append(List<Vec2> dst, List<Vec2> src, boolean skipFirst) {
        for (int i = skipFirst ? 1 : 0; i < src.size(); i++) {
            dst.add(src.get(i));
        }
    }

    public static void appendReversed(List<Vec2> dst, List<Vec2> src, boolean skipFirst) {
        for (int i = src.size() - 1 - (skipFirst ? 1 : 0); i >= 0; i--) {
            dst.add(src.get(i));
        }
    }

    public static Rectangle2D boundsOfContours(List<List<Vec2>> contours) {
        if (contours == null || contours.isEmpty()) {
            return new Rectangle2D.Double();
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        boolean hasPoint = false;

        for (List<Vec2> contour : contours) {
            if (contour == null) {
                continue;
            }

            for (Vec2 p : contour) {
                if (p == null) {
                    continue;
                }

                hasPoint = true;

                if (p.x < minX) {
                    minX = p.x;
                }
                if (p.y < minY) {
                    minY = p.y;
                }
                if (p.x > maxX) {
                    maxX = p.x;
                }
                if (p.y > maxY) {
                    maxY = p.y;
                }
            }
        }

        if (!hasPoint) {
            return new Rectangle2D.Double();
        }

        return new Rectangle2D.Double(
                minX,
                minY,
                maxX - minX,
                maxY - minY
        );
    }

    private static boolean pointInContours(double px, double py, List<List<Vec2>> contours, int windingRule) {
        for (List<Vec2> contour : contours) {
            int n = contour.size();
            for (int i = 0; i < n; i++) {
                Vec2 a = contour.get(i);
                Vec2 b = contour.get((i + 1) % n);
                if (pointOnSegment(px, py, a, b, 1e-9)) {
                    return true;
                }
            }
        }

        if (windingRule == PathIterator.WIND_EVEN_ODD) {
            int crossings = 0;

            for (List<Vec2> contour : contours) {
                int n = contour.size();
                for (int i = 0; i < n; i++) {
                    Vec2 a = contour.get(i);
                    Vec2 b = contour.get((i + 1) % n);

                    if (((a.y > py) != (b.y > py))
                            && (px < (b.x - a.x) * (py - a.y) / (b.y - a.y) + a.x)) {
                        crossings++;
                    }
                }
            }

            return (crossings & 1) != 0;
        } else {
            int winding = 0;

            for (List<Vec2> contour : contours) {
                int n = contour.size();
                for (int i = 0; i < n; i++) {
                    Vec2 a = contour.get(i);
                    Vec2 b = contour.get((i + 1) % n);

                    if (a.y <= py) {
                        if (b.y > py && isLeft(a, b, px, py) > 0.0) {
                            winding++;
                        }
                    } else {
                        if (b.y <= py && isLeft(a, b, px, py) < 0.0) {
                            winding--;
                        }
                    }
                }
            }

            return winding != 0;
        }
    }

    private static double isLeft(Vec2 a, Vec2 b, double px, double py) {
        return (b.x - a.x) * (py - a.y) - (px - a.x) * (b.y - a.y);
    }

    private static boolean pointOnSegment(double px, double py, Vec2 a, Vec2 b, double eps) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double len2 = dx * dx + dy * dy;

        if (len2 <= eps * eps) {
            double ex = px - a.x;
            double ey = py - a.y;
            return ex * ex + ey * ey <= eps * eps;
        }

        double cross = dx * (py - a.y) - dy * (px - a.x);
        if (Math.abs(cross) > eps) {
            return false;
        }

        double dot = (px - a.x) * dx + (py - a.y) * dy;
        if (dot < -eps) {
            return false;
        }

        return dot <= len2 + eps;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static double srgbToLinear(int c) {
        double x = c / 255.0;
        if (x <= 0.04045) {
            return x / 12.92;
        }
        return Math.pow((x + 0.055) / 1.055, 2.4);
    }

    private static int linearToSrgb8(double x) {
        x = Math.max(0.0, Math.min(1.0, x));

        double s;
        if (x <= 0.0031308) {
            s = 12.92 * x;
        } else {
            s = 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055;
        }

        return clamp255((int) Math.round(s * 255.0));
    }

    @SuppressWarnings("unchecked")
    private static PreparedContours prepareContours(List<List<Vec2>> contours, int windingRule, int imageHeight) {
        if (contours == null || contours.isEmpty()) {
            return new PreparedContours(
                    windingRule,
                    new ArrayList<PreparedContour>(),
                    (List<PreparedEdge>[]) new List[imageHeight],
                    0, 0, 0, 0
            );
        }

        List<PreparedContour> preparedContours = new ArrayList<PreparedContour>();

        double globalMinX = Double.POSITIVE_INFINITY;
        double globalMinY = Double.POSITIVE_INFINITY;
        double globalMaxX = Double.NEGATIVE_INFINITY;
        double globalMaxY = Double.NEGATIVE_INFINITY;

        List<PreparedEdge>[] buckets = (List<PreparedEdge>[]) new List[imageHeight];

        for (List<Vec2> contour : contours) {
            List<Vec2> cleaned = AntialiasTools.removeDuplicateAndCollinear(contour, 1e-9);
            if (cleaned == null || cleaned.size() < 3) {
                continue;
            }

            Vec2[] pts = cleaned.toArray(new Vec2[0]);

            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;
            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;

            for (Vec2 p : pts) {
                if (p.x < minX) {
                    minX = p.x;
                }
                if (p.y < minY) {
                    minY = p.y;
                }
                if (p.x > maxX) {
                    maxX = p.x;
                }
                if (p.y > maxY) {
                    maxY = p.y;
                }
            }

            PreparedEdge[] edges = new PreparedEdge[pts.length];
            for (int i = 0; i < pts.length; i++) {
                Vec2 a = pts[i];
                Vec2 b = pts[(i + 1) % pts.length];
                PreparedEdge e = new PreparedEdge(a.x, a.y, b.x, b.y);
                edges[i] = e;

                int rowMin = clamp((int) Math.floor(e.minY), 0, imageHeight - 1);
                int rowMax = clamp((int) Math.ceil(e.maxY), 0, imageHeight - 1);

                for (int row = rowMin; row <= rowMax; row++) {
                    if (buckets[row] == null) {
                        buckets[row] = new ArrayList<PreparedEdge>();
                    }
                    buckets[row].add(e);
                }
            }

            preparedContours.add(new PreparedContour(pts, edges, minX, minY, maxX, maxY));

            if (minX < globalMinX) {
                globalMinX = minX;
            }
            if (minY < globalMinY) {
                globalMinY = minY;
            }
            if (maxX > globalMaxX) {
                globalMaxX = maxX;
            }
            if (maxY > globalMaxY) {
                globalMaxY = maxY;
            }
        }

        if (preparedContours.isEmpty()) {
            globalMinX = globalMinY = globalMaxX = globalMaxY = 0.0;
        }

        return new PreparedContours(
                windingRule,
                preparedContours,
                buckets,
                globalMinX, globalMinY, globalMaxX, globalMaxY
        );
    }

    private static boolean pointInPreparedContours(double px, double py, PreparedContours prepared) {
        if (prepared == null || prepared.contours.isEmpty()) {
            return false;
        }

        if (px < prepared.minX || px > prepared.maxX || py < prepared.minY || py > prepared.maxY) {
            return false;
        }

        int row = clamp((int) Math.floor(py), 0, prepared.edgeBucketsByRow.length - 1);
        List<PreparedEdge> candidateEdges = prepared.edgeBucketsByRow[row];
        if (candidateEdges == null || candidateEdges.isEmpty()) {
            return false;
        }

        final double eps = 1e-9;

        // Hrana
        for (PreparedEdge e : candidateEdges) {
            if (!e.bboxContains(px, py, eps)) {
                continue;
            }
            if (pointOnPreparedEdge(px, py, e, eps)) {
                return true;
            }
        }

        if (prepared.windingRule == PathIterator.WIND_EVEN_ODD) {
            int crossings = 0;

            for (PreparedEdge e : candidateEdges) {
                if (e.horizontal) {
                    continue;
                }

                if (((e.y0 > py) != (e.y1 > py))) {
                    double xCross = e.x0 + (py - e.y0) * e.dx / e.dy;
                    if (px < xCross) {
                        crossings++;
                    }
                }
            }

            return (crossings & 1) != 0;
        } else {
            int winding = 0;

            for (PreparedEdge e : candidateEdges) {
                if (e.horizontal) {
                    continue;
                }

                if (e.y0 <= py) {
                    if (e.y1 > py && isLeft(e.x0, e.y0, e.x1, e.y1, px, py) > 0.0) {
                        winding++;
                    }
                } else {
                    if (e.y1 <= py && isLeft(e.x0, e.y0, e.x1, e.y1, px, py) < 0.0) {
                        winding--;
                    }
                }
            }

            return winding != 0;
        }
    }

    private static boolean pointOnPreparedEdge(double px, double py, PreparedEdge e, double eps) {
        if (e.len2 <= eps * eps) {
            double ex = px - e.x0;
            double ey = py - e.y0;
            return ex * ex + ey * ey <= eps * eps;
        }

        double cross = e.dx * (py - e.y0) - e.dy * (px - e.x0);
        if (Math.abs(cross) > eps) {
            return false;
        }

        double dot = (px - e.x0) * e.dx + (py - e.y0) * e.dy;
        if (dot < -eps) {
            return false;
        }

        return dot <= e.len2 + eps;
    }

    private static double isLeft(double x0, double y0, double x1, double y1, double px, double py) {
        return (x1 - x0) * (py - y0) - (px - x0) * (y1 - y0);
    }

    public static final class SceneRasterizerMSAA {

        private final int width;
        private final int height;
        private final int sampleCount;
        private final int[] sampleOffsets;

        private static final int SUBPIXEL_BITS = 8;
        private static final int SUBPIXEL_SCALE = 1 << SUBPIXEL_BITS;

        private final float[] sampleA;
        private final float[] sampleR;
        private final float[] sampleG;
        private final float[] sampleB;

        private PreparedContours preparedClip = null;
        private int clipWindingRule = PathIterator.WIND_EVEN_ODD;

        public SceneRasterizerMSAA(int width, int height, int sampleGrid) {
            this.width = width;
            this.height = height;
            this.sampleCount = sampleGrid * sampleGrid;
            this.sampleOffsets = buildRegularSamplePattern(sampleGrid);

            int totalSamples = width * height * sampleCount;
            this.sampleA = new float[totalSamples];
            this.sampleR = new float[totalSamples];
            this.sampleG = new float[totalSamples];
            this.sampleB = new float[totalSamples];
        }

        public void clear(int argb) {
            int a8 = (argb >>> 24) & 0xFF;
            int r8 = (argb >>> 16) & 0xFF;
            int g8 = (argb >>> 8) & 0xFF;
            int b8 = argb & 0xFF;

            float a = a8 / 255.0f;
            float r = (float) srgbToLinear(r8) * a;
            float g = (float) srgbToLinear(g8) * a;
            float b = (float) srgbToLinear(b8) * a;

            for (int i = 0; i < sampleA.length; i++) {
                sampleA[i] = a;
                sampleR[i] = r;
                sampleG[i] = g;
                sampleB[i] = b;
            }
        }

        public void setClipContours(List<List<Vec2>> contours, int windingRule) {
            if (contours == null || contours.isEmpty()) {
                this.preparedClip = null;
                return;
            }
            this.preparedClip = prepareContours(contours, windingRule, height);
        }

        public void clearClip() {
            this.preparedClip = null;
        }

        public void setClipContour(List<Vec2> contour) {
            if (contour == null) {
                clearClip();
                return;
            }

            List<List<Vec2>> contours = new ArrayList<List<Vec2>>();
            contours.add(contour);
            setClipContours(contours, PathIterator.WIND_EVEN_ODD);
        }

        private boolean passesClip(double sx, double sy) {
            if (preparedClip == null) {
                return true;
            }
            return pointInPreparedContours(sx, sy, preparedClip);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours, int windingRule, Paint paint) {
            fillContoursWithPaint(contours, windingRule, paint, null);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours,
                int windingRule,
                Paint paint,
                AffineTransform paintTransform) {
            fillContoursWithPaint(contours, windingRule, paint, paintTransform, false);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours, int windingRule, Paint paint, AffineTransform paintTransform, boolean smooth) {
            if (contours == null || contours.isEmpty() || paint == null) {
                return;
            }

            PreparedContours prepared = prepareContours(contours, windingRule, height);
            BufferedImage paintImage = createPaintImage(paint, paintTransform, smooth);
            fillPreparedContoursWithPaintImage(prepared, paintImage);
        }

        private int[] buildRegularSamplePattern(int grid) {
            int[] arr = new int[grid * grid * 2];
            int idx = 0;

            for (int y = 0; y < grid; y++) {
                for (int x = 0; x < grid; x++) {
                    arr[idx++] = ((2 * x + 1) * SUBPIXEL_SCALE) / (2 * grid);
                    arr[idx++] = ((2 * y + 1) * SUBPIXEL_SCALE) / (2 * grid);
                }
            }
            return arr;
        }

        private void fillPreparedContoursWithPaintImage(PreparedContours prepared, BufferedImage paintImage) {
            if (prepared == null || prepared.contours.isEmpty()) {
                return;
            }

            int minX = clamp((int) Math.floor(prepared.minX), 0, width);
            int minY = clamp((int) Math.floor(prepared.minY), 0, height);
            int maxX = clamp((int) Math.ceil(prepared.maxX), 0, width);
            int maxY = clamp((int) Math.ceil(prepared.maxY), 0, height);

            for (int py = minY; py < maxY; py++) {
                for (int px = minX; px < maxX; px++) {
                    int base = (py * width + px) * sampleCount;

                    for (int s = 0; s < sampleCount; s++) {
                        double sx = px + (sampleOffsets[2 * s] / (double) SUBPIXEL_SCALE);
                        double sy = py + (sampleOffsets[2 * s + 1] / (double) SUBPIXEL_SCALE);

                        if (pointInPreparedContours(sx, sy, prepared) && passesClip(sx, sy)) {
                            int sampleX = clamp((int) Math.floor(sx), 0, width - 1);
                            int sampleY = clamp((int) Math.floor(sy), 0, height - 1);
                            int argb = paintImage.getRGB(sampleX, sampleY);

                            int a8 = (argb >>> 24) & 0xFF;
                            int r8 = (argb >>> 16) & 0xFF;
                            int g8 = (argb >>> 8) & 0xFF;
                            int b8 = argb & 0xFF;

                            float srcA = a8 / 255.0f;
                            if (srcA <= 0.0f) {
                                continue;
                            }

                            float srcR = (float) srgbToLinear(r8) * srcA;
                            float srcG = (float) srgbToLinear(g8) * srcA;
                            float srcB = (float) srgbToLinear(b8) * srcA;

                            int idx = base + s;
                            float dstA = sampleA[idx];
                            float invSrcA = 1.0f - srcA;

                            sampleR[idx] = srcR + sampleR[idx] * invSrcA;
                            sampleG[idx] = srcG + sampleG[idx] * invSrcA;
                            sampleB[idx] = srcB + sampleB[idx] * invSrcA;
                            sampleA[idx] = srcA + dstA * invSrcA;
                        }
                    }
                }
            }
        }

        private void fillContoursWithPaintImage(List<List<Vec2>> contours,
                int windingRule,
                BufferedImage paintImage) {
            double minXf = Double.POSITIVE_INFINITY;
            double minYf = Double.POSITIVE_INFINITY;
            double maxXf = Double.NEGATIVE_INFINITY;
            double maxYf = Double.NEGATIVE_INFINITY;

            for (List<Vec2> contour : contours) {
                for (Vec2 p : contour) {
                    if (p.x < minXf) {
                        minXf = p.x;
                    }
                    if (p.y < minYf) {
                        minYf = p.y;
                    }
                    if (p.x > maxXf) {
                        maxXf = p.x;
                    }
                    if (p.y > maxYf) {
                        maxYf = p.y;
                    }
                }
            }

            if (!(minXf <= maxXf) || !(minYf <= maxYf)) {
                return;
            }

            int minX = clamp((int) Math.floor(minXf), 0, width);
            int minY = clamp((int) Math.floor(minYf), 0, height);
            int maxX = clamp((int) Math.ceil(maxXf), 0, width);
            int maxY = clamp((int) Math.ceil(maxYf), 0, height);

            for (int py = minY; py < maxY; py++) {
                for (int px = minX; px < maxX; px++) {
                    int base = (py * width + px) * sampleCount;

                    for (int s = 0; s < sampleCount; s++) {
                        double sx = px + (sampleOffsets[2 * s] / (double) SUBPIXEL_SCALE);
                        double sy = py + (sampleOffsets[2 * s + 1] / (double) SUBPIXEL_SCALE);

                        if (pointInContours(sx, sy, contours, windingRule) && passesClip(sx, sy)) {
                            int sampleX = clamp((int) Math.floor(sx), 0, width - 1);
                            int sampleY = clamp((int) Math.floor(sy), 0, height - 1);
                            int argb = paintImage.getRGB(sampleX, sampleY);

                            int a8 = (argb >>> 24) & 0xFF;
                            int r8 = (argb >>> 16) & 0xFF;
                            int g8 = (argb >>> 8) & 0xFF;
                            int b8 = argb & 0xFF;

                            float srcA = a8 / 255.0f;
                            if (srcA <= 0.0f) {
                                continue;
                            }

                            float srcR = (float) srgbToLinear(r8) * srcA;
                            float srcG = (float) srgbToLinear(g8) * srcA;
                            float srcB = (float) srgbToLinear(b8) * srcA;

                            int idx = base + s;
                            float dstA = sampleA[idx];
                            float invSrcA = 1.0f - srcA;

                            sampleR[idx] = srcR + sampleR[idx] * invSrcA;
                            sampleG[idx] = srcG + sampleG[idx] * invSrcA;
                            sampleB[idx] = srcB + sampleB[idx] * invSrcA;
                            sampleA[idx] = srcA + dstA * invSrcA;
                        }
                    }
                }
            }
        }

        private BufferedImage createPaintImage(Paint paint, AffineTransform paintTransform, boolean smooth) {
            BufferedImage paintImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = paintImage.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (paint instanceof TexturePaint) {
                    if (smooth) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    } else {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                }

                if (paintTransform == null) {
                    paintTransform = new AffineTransform();
                }

                if (paintTransform != null) {
                    //g.transform(paintTransform);
                    g.setTransform(paintTransform);
                }

                g.setPaint(paint);

                Matrix m = new Matrix(paintTransform);            
                
                m = m.inverse();
                Rectangle2D r = m.transform(new Rectangle2D.Double(-width * 2, -height * 2, width * 4, height * 4));
                g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
               
            } finally {
                g.dispose();
            }
            return paintImage;
        }

        public void resolveTo(BufferedImage img) {
            for (int py = 0; py < height; py++) {
                for (int px = 0; px < width; px++) {
                    int base = (py * width + px) * sampleCount;

                    double sumA = 0.0;
                    double sumPR = 0.0;
                    double sumPG = 0.0;
                    double sumPB = 0.0;

                    for (int s = 0; s < sampleCount; s++) {
                        int idx = base + s;
                        sumA += sampleA[idx];
                        sumPR += sampleR[idx];
                        sumPG += sampleG[idx];
                        sumPB += sampleB[idx];
                    }

                    // Source z MSAA bufferu: linear + premultiplied
                    double srcA = sumA / sampleCount;
                    double srcPR = sumPR / sampleCount;
                    double srcPG = sumPG / sampleCount;
                    double srcPB = sumPB / sampleCount;

                    // Destination z původního obrázku: převést do linear + premultiplied
                    int dstArgb = img.getRGB(px, py);

                    int dstA8 = (dstArgb >>> 24) & 0xFF;
                    int dstR8 = (dstArgb >>> 16) & 0xFF;
                    int dstG8 = (dstArgb >>> 8) & 0xFF;
                    int dstB8 = dstArgb & 0xFF;

                    double dstA = dstA8 / 255.0;
                    double dstPR = srgbToLinear(dstR8) * dstA;
                    double dstPG = srgbToLinear(dstG8) * dstA;
                    double dstPB = srgbToLinear(dstB8) * dstA;

                    // SrcOver v linear premultiplied
                    double invSrcA = 1.0 - srcA;

                    double outA = srcA + dstA * invSrcA;
                    double outPR = srcPR + dstPR * invSrcA;
                    double outPG = srcPG + dstPG * invSrcA;
                    double outPB = srcPB + dstPB * invSrcA;

                    double outR = 0.0;
                    double outG = 0.0;
                    double outB = 0.0;

                    if (outA > 1e-12) {
                        outR = outPR / outA;
                        outG = outPG / outA;
                        outB = outPB / outA;
                    }

                    int outA8 = clamp255((int) Math.round(outA * 255.0));
                    int outR8 = linearToSrgb8(outR);
                    int outG8 = linearToSrgb8(outG);
                    int outB8 = linearToSrgb8(outB);

                    img.setRGB(px, py,
                            ((outA8 & 0xFF) << 24)
                            | ((outR8 & 0xFF) << 16)
                            | ((outG8 & 0xFF) << 8)
                            | (outB8 & 0xFF));
                }
            }
        }

    }

    static final class PreparedContours {

        final int windingRule;
        final List<PreparedContour> contours;
        final List<PreparedEdge>[] edgeBucketsByRow;
        final double minX, minY, maxX, maxY;

        PreparedContours(int windingRule,
                List<PreparedContour> contours,
                List<PreparedEdge>[] edgeBucketsByRow,
                double minX, double minY, double maxX, double maxY) {
            this.windingRule = windingRule;
            this.contours = contours;
            this.edgeBucketsByRow = edgeBucketsByRow;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    static final class PreparedContour {

        final Vec2[] points;
        final PreparedEdge[] edges;
        final double minX, minY, maxX, maxY;

        PreparedContour(Vec2[] points,
                PreparedEdge[] edges,
                double minX, double minY, double maxX, double maxY) {
            this.points = points;
            this.edges = edges;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        boolean bboxContains(double x, double y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }
    }

    static final class PreparedEdge {

        final double x0, y0, x1, y1;
        final double minX, minY, maxX, maxY;
        final double dx, dy;
        final double len2;
        final boolean horizontal;

        PreparedEdge(double x0, double y0, double x1, double y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.minX = Math.min(x0, x1);
            this.minY = Math.min(y0, y1);
            this.maxX = Math.max(x0, x1);
            this.maxY = Math.max(y0, y1);
            this.dx = x1 - x0;
            this.dy = y1 - y0;
            this.len2 = dx * dx + dy * dy;
            this.horizontal = Math.abs(dy) <= 1e-15;
        }

        boolean bboxContains(double x, double y, double eps) {
            return x >= minX - eps && x <= maxX + eps && y >= minY - eps && y <= maxY + eps;
        }
    }
}
