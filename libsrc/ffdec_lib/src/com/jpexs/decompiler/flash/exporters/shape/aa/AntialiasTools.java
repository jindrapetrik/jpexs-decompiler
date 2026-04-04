package com.jpexs.decompiler.flash.exporters.shape.aa;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.Graphics;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class AntialiasTools {

    private static final int FIXED_SHIFT = 8;
    private static final int FIXED_ONE = 1 << FIXED_SHIFT;

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

    private static long isLeft(int x0, int y0, int x1, int y1, int px, int py) {
        return (long) (x1 - x0) * (py - y0) - (long) (px - x0) * (y1 - y0);
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

    private static int toFixed(double v) {
        return (int) Math.round(v * FIXED_ONE);
    }

    private static int fixedFloorToInt(int v) {
        return v >> FIXED_SHIFT;
    }

    private static int fixedCeilToInt(int v) {
        return (v + FIXED_ONE - 1) >> FIXED_SHIFT;
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static float srgbToLinear(int c) {
        float x = c / 255.0f;
        if (x <= 0.04045) {
            return x / 12.92f;
        }
        return (float) Math.pow((x + 0.055) / 1.055, 2.4);
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
                    new PreparedEdge[imageHeight][],
                    0, 0, 0, 0
            );
        }

        List<PreparedContour> preparedContours = new ArrayList<PreparedContour>();

        int globalMinX = Integer.MAX_VALUE;
        int globalMinY = Integer.MAX_VALUE;
        int globalMaxX = Integer.MIN_VALUE;
        int globalMaxY = Integer.MIN_VALUE;

        @SuppressWarnings("unchecked")
        List<PreparedEdge>[] buckets = (List<PreparedEdge>[]) new List[imageHeight];

        for (List<Vec2> contour : contours) {
            List<Vec2> cleaned = AntialiasTools.removeDuplicateAndCollinear(contour, 1e-9);
            if (cleaned == null || cleaned.size() < 3) {
                continue;
            }

            int pointCount = cleaned.size();
            int[] xs = new int[pointCount];
            int[] ys = new int[pointCount];

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < pointCount; i++) {
                Vec2 p = cleaned.get(i);
                int fx = toFixed(p.x);
                int fy = toFixed(p.y);
                xs[i] = fx;
                ys[i] = fy;
                if (fx < minX) {
                    minX = fx;
                }
                if (fy < minY) {
                    minY = fy;
                }
                if (fx > maxX) {
                    maxX = fx;
                }
                if (fy > maxY) {
                    maxY = fy;
                }
            }

            PreparedEdge[] edges = new PreparedEdge[pointCount];
            for (int i = 0; i < pointCount; i++) {
                int x0 = xs[i];
                int y0 = ys[i];
                int x1 = xs[(i + 1) % pointCount];
                int y1 = ys[(i + 1) % pointCount];
                PreparedEdge e = new PreparedEdge(x0, y0, x1, y1);
                edges[i] = e;

                int rowMin = clamp(fixedFloorToInt(e.minY), 0, imageHeight - 1);
                int rowMax = clamp(fixedCeilToInt(e.maxY), 0, imageHeight - 1);

                for (int row = rowMin; row <= rowMax; row++) {
                    if (buckets[row] == null) {
                        buckets[row] = new ArrayList<PreparedEdge>();
                    }
                    buckets[row].add(e);
                }
            }

            preparedContours.add(new PreparedContour(xs, ys, edges, minX, minY, maxX, maxY));

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
            globalMinX = globalMinY = globalMaxX = globalMaxY = 0;
        }

        PreparedEdge[][] arrayBuckets = new PreparedEdge[imageHeight][];
        for (int i = 0; i < imageHeight; i++) {
            if (buckets[i] != null) {
                arrayBuckets[i] = buckets[i].toArray(new PreparedEdge[0]);
            }
        }

        return new PreparedContours(
                windingRule,
                preparedContours,
                arrayBuckets,
                globalMinX, globalMinY, globalMaxX, globalMaxY
        );
    }

    private static boolean pointInPreparedContours(int pxFixed, int pyFixed, PreparedContours prepared) {
        if (prepared == null || prepared.contours.isEmpty()) {
            return false;
        }

        if (pxFixed < prepared.minX || pxFixed > prepared.maxX || pyFixed < prepared.minY || pyFixed > prepared.maxY) {
            return false;
        }

        int row = clamp(fixedFloorToInt(pyFixed), 0, prepared.edgeBucketsByRow.length - 1);
        PreparedEdge[] candidateEdges = prepared.edgeBucketsByRow[row];
        if (candidateEdges == null || candidateEdges.length == 0) {
            return false;
        }

        final int eps = 1;
        final int edgeCount = candidateEdges.length;

        if (prepared.windingRule == PathIterator.WIND_EVEN_ODD) {
            int crossings = 0;

            for (int i = 0; i < edgeCount; i++) {
                PreparedEdge e = candidateEdges[i];

                if (e.bboxContains(pxFixed, pyFixed, eps) && pointOnPreparedEdge(pxFixed, pyFixed, e, eps)) {
                    return true;
                }

                if (!e.horizontal && ((e.y0 > pyFixed) != (e.y1 > pyFixed))) {
                    long xCross = ((long) e.x0 << FIXED_SHIFT) + ((long) (pyFixed - e.y0) * e.dxOverDyFixed);
                    if (((long) pxFixed << FIXED_SHIFT) < xCross) {
                        crossings++;
                    }
                }
            }

            return (crossings & 1) != 0;
        } else {
            int winding = 0;

            for (int i = 0; i < edgeCount; i++) {
                PreparedEdge e = candidateEdges[i];

                if (e.bboxContains(pxFixed, pyFixed, eps) && pointOnPreparedEdge(pxFixed, pyFixed, e, eps)) {
                    return true;
                }

                if (!e.horizontal) {
                    long dpy = (long) pyFixed - e.y0;
                    long cross = (long) e.dx * dpy - (long) (pxFixed - e.x0) * e.dy;
                    if (e.y0 <= pyFixed) {
                        if (e.y1 > pyFixed && cross > 0L) {
                            winding++;
                        }
                    } else {
                        if (e.y1 <= pyFixed && cross < 0L) {
                            winding--;
                        }
                    }
                }
            }

            return winding != 0;
        }
    }

    private static boolean pointOnPreparedEdge(int pxFixed, int pyFixed, PreparedEdge e, int eps) {
        if (e.len2 <= (long) eps * eps) {
            long ex = (long) pxFixed - e.x0;
            long ey = (long) pyFixed - e.y0;
            return ex * ex + ey * ey <= (long) eps * eps;
        }

        long cross = (long) e.dx * ((long) pyFixed - e.y0) - (long) e.dy * ((long) pxFixed - e.x0);
        if (cross > eps || cross < -eps) {
            return false;
        }

        long dot = ((long) pxFixed - e.x0) * e.dx + ((long) pyFixed - e.y0) * e.dy;
        if (dot < -eps) {
            return false;
        }

        return dot <= e.len2 + eps;
    }

    public static final class SceneRasterizerMSAA {

        private final int width;
        private final int height;
        private final int sampleCount;
        private final int[] sampleOffsets;
        private ColorMode colorMode = ColorMode.SRGB_FLASH_COMPAT;

        private static final int SUBPIXEL_BITS = 8;
        private static final int SUBPIXEL_SCALE = 1 << SUBPIXEL_BITS;

        private final float[] sampleA;
        private final float[] sampleR;
        private final float[] sampleG;
        private final float[] sampleB;

        private PreparedContours preparedClip = null;
        private int clipWindingRule = PathIterator.WIND_EVEN_ODD;

        private static final float[] SRGB_TO_LINEAR_8 = buildSrgbToLinear8();

        private static final int LINEAR_TO_SRGB_LUT_SIZE = 4096;
        private static final int[] LINEAR_TO_SRGB_8 = buildLinearToSrgb8();

        private static int[] buildLinearToSrgb8() {
            int[] t = new int[LINEAR_TO_SRGB_LUT_SIZE + 1];
            for (int i = 0; i <= LINEAR_TO_SRGB_LUT_SIZE; i++) {
                double x = i / (double) LINEAR_TO_SRGB_LUT_SIZE;
                double s;
                if (x <= 0.0031308) {
                    s = 12.92 * x;
                } else {
                    s = 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055;
                }
                int v = (int) Math.round(s * 255.0);
                if (v < 0) {
                    v = 0;
                }
                if (v > 255) {
                    v = 255;
                }
                t[i] = v;
            }
            return t;
        }

        private static int linearToSrgb8Fast(float x) {
            if (x <= 0f) {
                return 0;
            }
            if (x >= 1f) {
                return 255;
            }
            int idx = (int) (x * LINEAR_TO_SRGB_LUT_SIZE + 0.5f);
            if (idx < 0) {
                idx = 0;
            }
            if (idx > LINEAR_TO_SRGB_LUT_SIZE) {
                idx = LINEAR_TO_SRGB_LUT_SIZE;
            }
            return LINEAR_TO_SRGB_8[idx];
        }

        private static float[] buildSrgbToLinear8() {
            float[] t = new float[256];
            for (int i = 0; i < 256; i++) {
                double x = i / 255.0;
                if (x <= 0.04045) {
                    t[i] = (float) (x / 12.92);
                } else {
                    t[i] = (float) Math.pow((x + 0.055) / 1.055, 2.4);
                }
            }
            return t;
        }

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
            float r;
            float g;
            float b;

            if (colorMode == ColorMode.SRGB_FLASH_COMPAT) {
                r = (r8 / 255.0f) * a;
                g = (g8 / 255.0f) * a;
                b = (b8 / 255.0f) * a;
            } else {
                r = SRGB_TO_LINEAR_8[r8] * a;
                g = SRGB_TO_LINEAR_8[g8] * a;
                b = SRGB_TO_LINEAR_8[b8] * a;
            }

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

        private boolean passesClip(int sxFixed, int syFixed) {
            if (preparedClip == null) {
                return true;
            }
            return pointInPreparedContours(sxFixed, syFixed, preparedClip);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours, int windingRule, Paint paint) {
            fillContoursWithPaint(contours, windingRule, paint, null);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours,
                int windingRule,
                Paint paint,
                AffineTransform paintTransform) {
            fillContoursWithPaint(contours, windingRule, paint, paintTransform, false, null);
        }

        public void fillContoursWithPaint(List<List<Vec2>> contours, int windingRule, Paint paint, AffineTransform paintTransform, boolean smooth, BufferedImage imagePaint) {
            if (contours == null || contours.isEmpty() || paint == null) {
                return;
            }

            PreparedContours prepared = prepareContours(contours, windingRule, height);
            BufferedImage paintImage = createPaintImage(paint, paintTransform, smooth, imagePaint);
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

            int minX = clamp(fixedFloorToInt(prepared.minX), 0, width);
            int minY = clamp(fixedFloorToInt(prepared.minY), 0, height);
            int maxX = clamp(fixedCeilToInt(prepared.maxX), 0, width);
            int maxY = clamp(fixedCeilToInt(prepared.maxY), 0, height);

            for (int py = minY; py < maxY; py++) {
                for (int px = minX; px < maxX; px++) {
                    int base = (py * width + px) * sampleCount;

                    int pxFixedBase = px << FIXED_SHIFT;
                    int pyFixedBase = py << FIXED_SHIFT;
                    for (int s = 0; s < sampleCount; s++) {
                        int sxFixed = pxFixedBase + sampleOffsets[2 * s];
                        int syFixed = pyFixedBase + sampleOffsets[2 * s + 1];

                        if (pointInPreparedContours(sxFixed, syFixed, prepared) && passesClip(sxFixed, syFixed)) {
                            int sampleX = clamp(fixedFloorToInt(sxFixed), 0, width - 1);
                            int sampleY = clamp(fixedFloorToInt(syFixed), 0, height - 1);
                            int argb = paintImage.getRGB(sampleX, sampleY);

                            int a8 = (argb >>> 24) & 0xFF;
                            int r8 = (argb >>> 16) & 0xFF;
                            int g8 = (argb >>> 8) & 0xFF;
                            int b8 = argb & 0xFF;

                            float srcA = a8 / 255.0f;
                            if (srcA <= 0.0f) {
                                continue;
                            }

                            float srcR;
                            float srcG;
                            float srcB;
                            if (colorMode == ColorMode.SRGB_FLASH_COMPAT) {
                                srcR = (r8 / 255.0f) * srcA;
                                srcG = (g8 / 255.0f) * srcA;
                                srcB = (b8 / 255.0f) * srcA;
                            } else {
                                srcR = SRGB_TO_LINEAR_8[r8] * srcA;
                                srcG = SRGB_TO_LINEAR_8[g8] * srcA;
                                srcB = SRGB_TO_LINEAR_8[b8] * srcA;
                            }

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

                        if (pointInContours(sx, sy, contours, windingRule) && passesClip(toFixed(sx), toFixed(sy))) {
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

        private BufferedImage createPaintImage(Paint paint, AffineTransform paintTransform, boolean smooth, BufferedImage textureImage) {
            BufferedImage paintImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = paintImage.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                if (paint instanceof TexturePaint || textureImage != null) {
                    if (smooth) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    } else {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                }

                if (paintTransform == null) {
                    paintTransform = new AffineTransform();
                }

                g.setTransform(paintTransform);

                g.setPaint(paint);

                if (textureImage != null) {
                    g.drawImage(textureImage, 0, 0, null);
                } else {
                    Matrix m = new Matrix(paintTransform);

                    m = m.inverse();
                    Rectangle2D r = m.transform(new Rectangle2D.Double(-width * 2, -height * 2, width * 4, height * 4));
                    g.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
                }
            } finally {
                g.dispose();
            }
            return paintImage;
        }

        public void resolveTo(BufferedImage img) {
            int[] dst = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            final boolean dstIsPremultiplied = img.isAlphaPremultiplied();

            int pixelIndex = 0;
            int sampleBase = 0;

            for (int py = 0; py < height; py++) {
                for (int px = 0; px < width; px++, pixelIndex++, sampleBase += sampleCount) {
                    float sumA = 0f;
                    float sumPR = 0f;
                    float sumPG = 0f;
                    float sumPB = 0f;

                    for (int s = 0; s < sampleCount; s++) {
                        int idx = sampleBase + s;
                        sumA += sampleA[idx];
                        sumPR += sampleR[idx];
                        sumPG += sampleG[idx];
                        sumPB += sampleB[idx];
                    }

                    float srcA = sumA / sampleCount;
                    float srcPR = sumPR / sampleCount;
                    float srcPG = sumPG / sampleCount;
                    float srcPB = sumPB / sampleCount;

                    int dstArgb = dst[pixelIndex];
                    int dstA8 = (dstArgb >>> 24) & 0xFF;
                    int dstR8 = (dstArgb >>> 16) & 0xFF;
                    int dstG8 = (dstArgb >>> 8) & 0xFF;
                    int dstB8 = dstArgb & 0xFF;

                    float dstA = dstA8 / 255.0f;
                    float dstPR = 0f;
                    float dstPG = 0f;
                    float dstPB = 0f;

                    if (dstA > 1e-12f) {
                        if (colorMode == ColorMode.SRGB_FLASH_COMPAT) {
                            if (dstIsPremultiplied) {
                                dstPR = dstR8 / 255.0f;
                                dstPG = dstG8 / 255.0f;
                                dstPB = dstB8 / 255.0f;
                            } else {
                                dstPR = (dstR8 / 255.0f) * dstA;
                                dstPG = (dstG8 / 255.0f) * dstA;
                                dstPB = (dstB8 / 255.0f) * dstA;
                            }
                        } else {
                            if (dstIsPremultiplied) {
                                float dstRs = (dstR8 / 255.0f) / dstA;
                                float dstGs = (dstG8 / 255.0f) / dstA;
                                float dstBs = (dstB8 / 255.0f) / dstA;

                                dstRs = clamp01(dstRs);
                                dstGs = clamp01(dstGs);
                                dstBs = clamp01(dstBs);

                                float dstRl = srgbToLinearFloat(dstRs);
                                float dstGl = srgbToLinearFloat(dstGs);
                                float dstBl = srgbToLinearFloat(dstBs);

                                dstPR = dstRl * dstA;
                                dstPG = dstGl * dstA;
                                dstPB = dstBl * dstA;
                            } else {
                                float dstRl = srgb8ToLinearFloat(dstR8);
                                float dstGl = srgb8ToLinearFloat(dstG8);
                                float dstBl = srgb8ToLinearFloat(dstB8);

                                dstPR = dstRl * dstA;
                                dstPG = dstGl * dstA;
                                dstPB = dstBl * dstA;
                            }
                        }
                    }

                    float invSrcA = 1.0f - srcA;

                    float outA = srcA + dstA * invSrcA;
                    float outPR = srcPR + dstPR * invSrcA;
                    float outPG = srcPG + dstPG * invSrcA;
                    float outPB = srcPB + dstPB * invSrcA;

                    int outA8 = clamp255((int) (outA * 255.0f + 0.5f));
                    int outR8;
                    int outG8;
                    int outB8;

                    if (outA > 1e-12f) {
                        if (colorMode == ColorMode.SRGB_FLASH_COMPAT) {
                            float outRs = clamp01(outPR / outA);
                            float outGs = clamp01(outPG / outA);
                            float outBs = clamp01(outPB / outA);

                            if (dstIsPremultiplied) {
                                outR8 = clamp255((int) (outRs * outA * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * outA * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * outA * 255.0f + 0.5f));
                            } else {
                                outR8 = clamp255((int) (outRs * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * 255.0f + 0.5f));
                            }
                        } else {
                            float outRl = clamp01(outPR / outA);
                            float outGl = clamp01(outPG / outA);
                            float outBl = clamp01(outPB / outA);

                            if (dstIsPremultiplied) {
                                float outRs = linearToSrgbFloat(outRl);
                                float outGs = linearToSrgbFloat(outGl);
                                float outBs = linearToSrgbFloat(outBl);

                                outR8 = clamp255((int) (outRs * outA * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * outA * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * outA * 255.0f + 0.5f));
                            } else {
                                outR8 = linearToSrgb8Fast(outRl);
                                outG8 = linearToSrgb8Fast(outGl);
                                outB8 = linearToSrgb8Fast(outBl);
                            }
                        }
                    } else {
                        outR8 = outG8 = outB8 = 0;
                    }

                    dst[pixelIndex] = (outA8 << 24) | (outR8 << 16) | (outG8 << 8) | outB8;
                }
            }
        }

        private static float clamp01(float v) {
            if (v < 0f) {
                return 0f;
            }
            if (v > 1f) {
                return 1f;
            }
            return v;
        }

        private static float srgb8ToLinearFloat(int c8) {
            return SRGB_TO_LINEAR_8[c8];
        }

        private static float srgbToLinearFloat(float s) {
            s = clamp01(s);
            if (s <= 0.04045f) {
                return s / 12.92f;
            }
            return (float) Math.pow((s + 0.055f) / 1.055f, 2.4f);
        }

        private static float linearToSrgbFloat(float l) {
            l = clamp01(l);
            if (l <= 0.0031308f) {
                return 12.92f * l;
            }
            return (float) (1.055 * Math.pow(l, 1.0 / 2.4) - 0.055);
        }

        public void resolveToReplace(BufferedImage img) {
            int[] dst = ((java.awt.image.DataBufferInt) img.getRaster().getDataBuffer()).getData();
            final boolean dstIsPremultiplied = img.isAlphaPremultiplied();

            int pixelIndex = 0;
            int sampleBase = 0;

            for (int py = 0; py < height; py++) {
                for (int px = 0; px < width; px++, pixelIndex++, sampleBase += sampleCount) {
                    float sumA = 0f;
                    float sumPR = 0f;
                    float sumPG = 0f;
                    float sumPB = 0f;

                    for (int s = 0; s < sampleCount; s++) {
                        int idx = sampleBase + s;
                        sumA += sampleA[idx];
                        sumPR += sampleR[idx];
                        sumPG += sampleG[idx];
                        sumPB += sampleB[idx];
                    }

                    float outA = sumA / sampleCount;
                    float outPR = sumPR / sampleCount;
                    float outPG = sumPG / sampleCount;
                    float outPB = sumPB / sampleCount;

                    int outA8 = clamp255((int) (outA * 255.0f + 0.5f));
                    int outR8;
                    int outG8;
                    int outB8;

                    if (outA > 1e-12f) {
                        if (colorMode == ColorMode.SRGB_FLASH_COMPAT) {
                            float outRs = clamp01(outPR / outA);
                            float outGs = clamp01(outPG / outA);
                            float outBs = clamp01(outPB / outA);

                            if (dstIsPremultiplied) {
                                outR8 = clamp255((int) (outRs * outA * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * outA * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * outA * 255.0f + 0.5f));
                            } else {
                                outR8 = clamp255((int) (outRs * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * 255.0f + 0.5f));
                            }
                        } else {
                            float outRl = clamp01(outPR / outA);
                            float outGl = clamp01(outPG / outA);
                            float outBl = clamp01(outPB / outA);

                            if (dstIsPremultiplied) {
                                float outRs = linearToSrgbFloat(outRl);
                                float outGs = linearToSrgbFloat(outGl);
                                float outBs = linearToSrgbFloat(outBl);

                                outR8 = clamp255((int) (outRs * outA * 255.0f + 0.5f));
                                outG8 = clamp255((int) (outGs * outA * 255.0f + 0.5f));
                                outB8 = clamp255((int) (outBs * outA * 255.0f + 0.5f));
                            } else {
                                outR8 = linearToSrgb8Fast(outRl);
                                outG8 = linearToSrgb8Fast(outGl);
                                outB8 = linearToSrgb8Fast(outBl);
                            }
                        }
                    } else {
                        outR8 = outG8 = outB8 = 0;
                    }

                    dst[pixelIndex] = (outA8 << 24) | (outR8 << 16) | (outG8 << 8) | outB8;
                }
            }
        }

        public void setColorMode(ColorMode colorMode) {
            if (colorMode == null) {
                this.colorMode = ColorMode.LINEAR;
            } else {
                this.colorMode = colorMode;
            }
        }

        public ColorMode getColorMode() {
            return colorMode;
        }
    }

    static final class PreparedContours {

        final int windingRule;
        final List<PreparedContour> contours;
        final PreparedEdge[][] edgeBucketsByRow;
        final int minX;
        final int minY;
        final int maxX;
        final int maxY;

        PreparedContours(int windingRule,
                List<PreparedContour> contours,
                PreparedEdge[][] edgeBucketsByRow,
                int minX, int minY, int maxX, int maxY) {
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

        final int[] xs;
        final int[] ys;
        final PreparedEdge[] edges;
        final int minX;
        final int minY;
        final int maxX;
        final int maxY;

        PreparedContour(int[] xs,
                int[] ys,
                PreparedEdge[] edges,
                int minX, int minY, int maxX, int maxY) {
            this.xs = xs;
            this.ys = ys;
            this.edges = edges;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        boolean bboxContains(int x, int y) {
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }
    }

    static final class PreparedEdge {

        final int x0;
        final int y0;
        final int x1;
        final int y1;
        final int minX;
        final int minY;
        final int maxX;
        final int maxY;
        final int dx;
        final int dy;
        final int dxOverDyFixed;
        final long len2;
        final boolean horizontal;

        PreparedEdge(int x0, int y0, int x1, int y1) {
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
            this.len2 = (long) dx * dx + (long) dy * dy;
            this.horizontal = (dy == 0);
            this.dxOverDyFixed = horizontal ? 0 : (int) Math.round((double) dx * FIXED_ONE / (double) dy);
        }

        boolean bboxContains(int x, int y, int eps) {
            return x >= minX - eps && x <= maxX + eps && y >= minY - eps && y <= maxY + eps;
        }
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.uiScale", "1.0");
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(800, 800);
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("c:\\FlashRelated\\ten.png"));
        } catch (IOException ex) {
            Logger.getLogger(AntialiasTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        final BufferedImage fimg = img;
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                /*AffineTransform t = AffineTransform.getScaleInstance(16, 16);
                t.preConcatenate(AffineTransform.getTranslateInstance(100, 100));*/
                g2.translate(100, 100);
                g2.scale(16, 16);
                //g2.setTransform(t);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setPaint(new TexturePaint(fimg, new Rectangle2D.Double(0, 0, 10, 10)));
                //g2.fillRect(0,0,10,10);
                double eps = 0; //0.5; //0.5 / 16.0;   // půl cílového pixelu v "user space"
                g2.fill(new Rectangle2D.Double(0, 0, 10 - eps, 10 - eps));
            }

        };
        f.setContentPane(p);
        f.setVisible(true);
    }

    public static enum ColorMode {
        LINEAR,
        SRGB_FLASH_COMPAT
    }
}
