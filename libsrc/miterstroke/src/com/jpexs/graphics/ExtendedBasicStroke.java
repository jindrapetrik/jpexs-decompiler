package com.jpexs.graphics;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Extended Basic Stroke which allows MITER_CLIP join style.
 * @author JPEXS
 */
public class ExtendedBasicStroke implements Stroke {
    
    Set<Point2D> testPoints = new HashSet<>();
    
    /**
     * Indicates a mitered line join style. See the class overview for an
     * illustration.
     */
    public static final int JOIN_MITER = 0;

    /**
     * Indicates a rounded line join style. See the class overview for an
     * illustration.
     */
    public static final int JOIN_ROUND = 1;

    /**
     * Indicates a bevelled line join style. See the class overview for an
     * illustration.
     */
    public static final int JOIN_BEVEL = 2;
    
    /**
     * Indicates a mitered clipped line join style. See the class overview for an
     * illustration.
     */
    public static final int JOIN_MITER_CLIP = 3;

    /**
     * Indicates a flat line cap style. See the class overview for an
     * illustration.
     */
    public static final int CAP_BUTT = 0;

    /**
     * Indicates a rounded line cap style. See the class overview for an
     * illustration.
     */
    public static final int CAP_ROUND = 1;

    /**
     * Indicates a square line cap style. See the class overview for an
     * illustration.
     */
    public static final int CAP_SQUARE = 2;

    /**
     * The stroke width.
     */
    private final float width;

    /**
     * The line cap style.
     */
    private final int cap;

    /**
     * The line join style.
     */
    private final int join;

    /**
     * The miter limit.
     */
    private final float limit;

    /**
     * Creates a new <code>BasicStroke</code> instance with the given
     * attributes.
     *
     * @param width the line width (>= 0.0f).
     * @param cap the line cap style (one of {@link #CAP_BUTT},
     *             {@link #CAP_ROUND} or {@link #CAP_SQUARE}).
     * @param join the line join style (one of {@link #JOIN_ROUND},
     *              {@link #JOIN_BEVEL}, or {@link #JOIN_MITER}).
     * @param miterlimit the limit to trim the miter join. The miterlimit must
     * be greater than or equal to 1.0f.
     *
     * @throws IllegalArgumentException If one input parameter doesn't meet its
     * needs.
     */
    public ExtendedBasicStroke(float width, int cap, int join, float miterlimit) {
        if (width < 0.0f) {
            throw new IllegalArgumentException("width " + width + " < 0");
        } else if (cap < CAP_BUTT || cap > CAP_SQUARE) {
            throw new IllegalArgumentException("cap " + cap + " out of range ["
                    + CAP_BUTT + ".." + CAP_SQUARE + "]");
        } else if (miterlimit < 1.0f && join == JOIN_MITER) {
            throw new IllegalArgumentException("miterlimit " + miterlimit
                    + " < 1.0f while join == JOIN_MITER");
        } else if (join < JOIN_MITER || join > JOIN_MITER_CLIP) {
            throw new IllegalArgumentException("join " + join + " out of range ["
                    + JOIN_MITER + ".." + JOIN_MITER_CLIP
                    + "]");
        } 

        this.width = width;
        this.cap = cap;
        this.join = join;
        limit = miterlimit;
    }


    /**
     * Creates a new <code>BasicStroke</code> instance with the given
     * attributes. The miter limit defaults to <code>10.0</code>.
     *
     * @param width the line width (>= 0.0f).
     * @param cap the line cap style (one of {@link #CAP_BUTT},
     *             {@link #CAP_ROUND} or {@link #CAP_SQUARE}).
     * @param join the line join style (one of {@link #JOIN_ROUND},
     *              {@link #JOIN_BEVEL}, or {@link #JOIN_MITER}).
     *
     * @throws IllegalArgumentException If one input parameter doesn't meet its
     * needs.
     */
    public ExtendedBasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10);
    }

    /**
     * Creates a new <code>BasicStroke</code> instance with the given line
     * width. The default values are:
     * <ul>
     * <li>line cap style: {@link #CAP_SQUARE};</li>
     * <li>line join style: {@link #JOIN_MITER};</li>
     * <li>miter limit: <code>10.0f</code>.
     * </ul>
     *
     * @param width the line width (>= 0.0f).
     *
     * @throws IllegalArgumentException If <code>width</code> is negative.
     */
    public ExtendedBasicStroke(float width) {
        this(width, CAP_SQUARE, JOIN_MITER, 10);
    }

    /**
     * Creates a new <code>BasicStroke</code> instance. The default values are:
     * <ul>
     * <li>line width: <code>1.0f</code>;</li>
     * <li>line cap style: {@link #CAP_SQUARE};</li>
     * <li>line join style: {@link #JOIN_MITER};</li>
     * <li>miter limit: <code>10.0f</code>.
     * </ul>
     */
    public ExtendedBasicStroke() {
        this(1, CAP_SQUARE, JOIN_MITER, 10);
    }

    /**
     * Creates a shape representing the stroked outline of the given shape. THIS
     * METHOD IS NOT YET IMPLEMENTED.
     *
     * @param s the shape.
     */
    @Override
    public Shape createStrokedShape(Shape s) {
        GeneralPath path = new GeneralPath();
        PathConsumer2D p2d = new PathConsumer2D() {
            @Override
            public void moveTo(float x, float y) {
                path.moveTo(x, y);
            }

            @Override
            public void lineTo(float x, float y) {
                path.lineTo(x, y);
            }

            @Override
            public void quadTo(float x1, float y1, float x2, float y2) {
                path.quadTo(x1, y1, x2, y2);
            }

            @Override
            public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
                path.curveTo(x1, y1, x2, y2, x3, y3);
            }

            @Override
            public void closePath() {
                path.closePath();
            }

            @Override
            public void pathDone() {

            }

            @Override
            public long getNativeConsumer() {
                return 0;
            }
        };

        Stroker stroker = new Stroker(p2d, width, cap, join, limit);

        float[] coords = new float[6];

        PathIterator pi = s.getPathIterator(new AffineTransform());
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    stroker.moveTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_LINETO:
                    stroker.lineTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_QUADTO:
                    stroker.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;

                case PathIterator.SEG_CUBICTO:
                    stroker.curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;

                case PathIterator.SEG_CLOSE:
                    stroker.closePath();
                    break;
            }
            pi.next();
        }

        stroker.pathDone();
              
        return path;

    }

    /**
     * Returns the line width.
     *
     * @return The line width.
     */
    public float getLineWidth() {
        return width;
    }

    /**
     * Returns a code indicating the line cap style (one of {@link #CAP_BUTT},
     * {@link #CAP_ROUND}, {@link #CAP_SQUARE}).
     *
     * @return A code indicating the line cap style.
     */
    public int getEndCap() {
        return cap;
    }

    /**
     * Returns a code indicating the line join style (one of {@link #JOIN_BEVEL},
     * {@link #JOIN_MITER} or {@link #JOIN_ROUND}).
     *
     * @return A code indicating the line join style.
     */
    public int getLineJoin() {
        return join;
    }

    /**
     * Returns the miter limit.
     *
     * @return The miter limit.
     */
    public float getMiterLimit() {
        return limit;
    }

    /**
     * Returns the hash code for this object. The hash is calculated by xoring
     * the hash, cap, join, limit, dash array and phase values (converted to
     * <code>int</code> first with <code>Float.floatToIntBits()</code> if the
     * value is a <code>float</code>).
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int hash = Float.floatToIntBits(width);
        hash ^= cap;
        hash ^= join;
        hash ^= Float.floatToIntBits(limit);

        return hash;
    }

    /**
     * Compares this <code>BasicStroke</code> for equality with an arbitrary
     * object. This method returns <code>true</code> if and only if:
     * <ul>
     * <li><code>o</code> is an instanceof <code>BasicStroke</code>;</li>
     * <li>this object has the same width, line cap style, line join style,
     * miter limit, dash array and dash phase as <code>o</code>.</li>
     * </ul>
     *
     * @param o the object (<code>null</code> permitted).
     *
     * @return <code>true</code> if this stroke is equal to <code>o</code> and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExtendedBasicStroke)) {
            return false;
        }
        ExtendedBasicStroke s = (ExtendedBasicStroke) o;
        return width == s.width && cap == s.cap && join == s.join
                && limit == s.limit;
    }
}
