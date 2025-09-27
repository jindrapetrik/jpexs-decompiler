package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.helpers.Reference;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Path area calculator and orientation detector.
 *
 * @author JPEXS
 */
public final class PathArea {

    public enum Orientation {
        CLOCKWISE, COUNTER_CLOCKWISE, DEGENERATE, OPEN_CONTOUR
    }

    /**
     * Result per closed subpath in drawing order. Open contours are reported
     * once as OPEN_CONTOUR.
     */
    public static void orientations(Shape shape, List<Orientation> result, List<Double> areas) {
        // Flatten curves to line segments for robust area computation.
        // flatness ~0.5px is usually fine; limit prevents infinite subdivision on pathological curves.
        final double flatness = 0.5;
        final int limit = 10;

        PathIterator it = shape.getPathIterator((AffineTransform) null);
        FlatteningPathIterator fpi = new FlatteningPathIterator(it, flatness, limit);

        double[] coords = new double[6];
        List<double[]> current = new ArrayList<>();

        double startX = 0;
        double startY = 0;
        double lastX = 0;
        double lastY = 0;
        boolean hasOpen = false;

        while (!fpi.isDone()) {
            int seg = fpi.currentSegment(coords);
            switch (seg) {
                case PathIterator.SEG_MOVETO:
                    // Start a new subpath
                    if (!current.isEmpty()) {
                        // Previous subpath ended without SEG_CLOSE
                        result.add(Orientation.OPEN_CONTOUR);
                        areas.add(0.0);
                        hasOpen = true;
                        current.clear();
                    }
                    startX = lastX = coords[0];
                    startY = lastY = coords[1];
                    current.add(new double[]{lastX, lastY});
                    break;

                case PathIterator.SEG_LINETO:
                    lastX = coords[0];
                    lastY = coords[1];
                    current.add(new double[]{lastX, lastY});
                    break;

                case PathIterator.SEG_CLOSE:
                    // Close current subpath by linking back to start point
                    if (!current.isEmpty()) {
                        Reference<Orientation> orientationRef = new Reference<>(null);
                        Reference<Double> areaRef = new Reference<>(0.0);
                        orientationOfClosedRing(current, areaRef, orientationRef);
                        result.add(orientationRef.getVal());
                        areas.add(areaRef.getVal());
                        current.clear();
                    } else {
                        // SEG_CLOSE without points – ignore
                    }
                    // Reset last point to start of next potential subpath
                    lastX = startX;
                    lastY = startY;
                    break;

                default:
                    // Should not happen because we flattened, but keep for completeness
                    throw new IllegalStateException("Unexpected segment type: " + seg);
            }
            fpi.next();
        }

        // If path ended without SEG_CLOSE for the last subpath
        if (!current.isEmpty()) {
            result.add(Orientation.OPEN_CONTOUR);
            areas.add(0.0);
            hasOpen = true;
        }

    }

    /**
     * Compute orientation of a closed ring using the shoelace formula over its
     * vertices.
     */
    private static void orientationOfClosedRing(List<double[]> pts, Reference<Double> areaRef, Reference<Orientation> resultRef) {
        // Ensure first != last; algorithm handles implicit closing edge (last->first)
        if (pts.size() < 3) {
            resultRef.setVal(Orientation.DEGENERATE);
            areaRef.setVal(0.0);
            return;
        }
        double area2 = 0.0; // 2 * signed area
        for (int i = 0, n = pts.size(); i < n; i++) {
            double[] a = pts.get(i);
            double[] b = pts.get((i + 1) % n);
            area2 += (a[0] * b[1]) - (b[0] * a[1]);
        }
        // Tolerance to treat near-zero areas as degenerate (units are in user space)
        double eps = 1e-9;
        if (Math.abs(area2) <= eps) {
            resultRef.setVal(Orientation.DEGENERATE);
            areaRef.setVal(0.0);
            return;
        }

        resultRef.setVal(area2 > 0 ? Orientation.CLOCKWISE : Orientation.COUNTER_CLOCKWISE);
        areaRef.setVal(Math.abs(area2 / 2));
    }

    public static void orientationSingleClosed(Shape shape, Reference<Orientation> orientationRef, Reference<Double> areaRef) {
        List<Orientation> result = new ArrayList<>();
        List<Double> areas = new ArrayList<>();
        orientations(shape, result, areas);
        if (result.isEmpty()) {
            orientationRef.setVal(Orientation.DEGENERATE);
            areaRef.setVal(0.0);
            return;
        }
        // Prefer the first closed orientation encountered
        for (int i = 0; i < result.size(); i++) {
            Orientation o = result.get(i);
            if (o != Orientation.OPEN_CONTOUR) {
                orientationRef.setVal(o);
                areaRef.setVal(areas.get(i));
                return;
            }
        }
        orientationRef.setVal(Orientation.OPEN_CONTOUR);
        areaRef.setVal(0.0);
    }
}
