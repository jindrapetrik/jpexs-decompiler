/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// Adapted for FFDec on 2026-11-17. See NOTICE.txt in this package.
package com.jpexs.decompiler.flash.math.area;

import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An <code>BezierArea</code> object stores and manipulates a
 * resolution-independent description of an enclosed area of
 * 2-dimensional space.
 * <code>BezierArea</code> objects can be transformed and can perform
 * various Constructive BezierArea Geometry (CAG) operations when combined
 * with other <code>BezierArea</code> objects.
 * The CAG operations include area
 * {@link #add addition}, {@link #subtract subtraction},
 * {@link #intersect intersection}, and {@link #exclusiveOr exclusive or}.
 * See the linked method documentation for examples of the various
 * operations.
 * <p>
 * The <code>BezierArea</code> class implements the <code>Shape</code>
 * interface and provides full support for all of its hit-testing
 * and path iteration facilities, but an <code>BezierArea</code> is more
 * specific than a generalized path in a number of ways:
 * <ul>
 * <li>Only closed paths and sub-paths are stored.
 *     <code>BezierArea</code> objects constructed from unclosed paths
 *     are implicitly closed during construction as if those paths
 *     had been filled by the <code>Graphics2D.fill</code> method.
 * <li>The interiors of the individual stored sub-paths are all
 *     non-empty and non-overlapping.  Paths are decomposed during
 *     construction into separate component non-overlapping parts,
 *     empty pieces of the path are discarded, and then these
 *     non-empty and non-overlapping properties are maintained
 *     through all subsequent CAG operations.  Outlines of different
 *     component sub-paths may touch each other, as long as they
 *     do not cross so that their enclosed areas overlap.
 * <li>The geometry of the path describing the outline of the
 *     <code>BezierArea</code> resembles the path from which it was
 *     constructed only in that it describes the same enclosed
 *     2-dimensional area, but may use entirely different types
 *     and ordering of the path segments to do so.
 * </ul>
 * Interesting issues which are not always obvious when using
 * the <code>BezierArea</code> include:
 * <ul>
 * <li>Creating an <code>BezierArea</code> from an unclosed (open)
 *     <code>Shape</code> results in a closed outline in the
 *     <code>BezierArea</code> object.
 * <li>Creating an <code>BezierArea</code> from a <code>Shape</code>
 *     which encloses no area (even when "closed") produces an
 *     empty <code>BezierArea</code>.  A common example of this issue
 *     is that producing an <code>BezierArea</code> from a line will
 *     be empty since the line encloses no area.  An empty
 *     <code>BezierArea</code> will iterate no geometry in its
 *     <code>PathIterator</code> objects.
 * <li>A self-intersecting <code>Shape</code> may be split into
 *     two (or more) sub-paths each enclosing one of the
 *     non-intersecting portions of the original path.
 * <li>An <code>BezierArea</code> may take more path segments to
 *     describe the same geometry even when the original
 *     outline is simple and obvious.  The analysis that the
 *     <code>BezierArea</code> class must perform on the path may
 *     not reflect the same concepts of "simple and obvious"
 *     as a human being perceives.
 * </ul>
 *
 * @since 1.2
 */
public strictfp class BezierArea implements Shape, Cloneable {
    private static Vector<Curve> EmptyCurves = new Vector<>();

    private Vector<Curve> curves;

    /**
     * Default constructor which creates an empty area.
     * @since 1.2
     */
    public BezierArea() {
        curves = EmptyCurves;
    }

    /**
     * The <code>BezierArea</code> class creates an area geometry from the
     * specified {@link Shape} object.  The geometry is explicitly
     * closed, if the <code>Shape</code> is not already closed.  The
     * fill rule (even-odd or winding) specified by the geometry of the
     * <code>Shape</code> is used to determine the resulting enclosed area.
     * @param s  the <code>Shape</code> from which the area is constructed
     * @throws NullPointerException if <code>s</code> is null
     * @since 1.2
     */
    public BezierArea(Shape s) {
        if (s instanceof BezierArea) {
            curves = ((BezierArea) s).curves;
        } else {
            curves = pathToCurves(s.getPathIterator(null));
        }
    }

    private static Vector<Curve> pathToCurves(PathIterator pi) {
        Vector<Curve> curves = new Vector<>();
        int windingRule = pi.getWindingRule();
        // coords array is big enough for holding:
        //     coordinates returned from currentSegment (6)
        //     OR
        //         two subdivided quadratic curves (2+4+4=10)
        //         AND
        //             0-1 horizontal splitting parameters
        //             OR
        //             2 parametric equation derivative coefficients
        //     OR
        //         three subdivided cubic curves (2+6+6+6=20)
        //         AND
        //             0-2 horizontal splitting parameters
        //             OR
        //             3 parametric equation derivative coefficients
        double coords[] = new double[23];
        double movx = 0, movy = 0;
        double curx = 0, cury = 0;
        double newx, newy;
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                Curve.insertLine(curves, curx, cury, movx, movy);
                curx = movx = coords[0];
                cury = movy = coords[1];
                Curve.insertMove(curves, movx, movy);
                break;
            case PathIterator.SEG_LINETO:
                newx = coords[0];
                newy = coords[1];
                Curve.insertLine(curves, curx, cury, newx, newy);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_QUADTO:
                newx = coords[2];
                newy = coords[3];
                Curve.insertQuad(curves, curx, cury, coords);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_CUBICTO:
                newx = coords[4];
                newy = coords[5];
                Curve.insertCubic(curves, curx, cury, coords);
                curx = newx;
                cury = newy;
                break;
            case PathIterator.SEG_CLOSE:
                Curve.insertLine(curves, curx, cury, movx, movy);
                curx = movx;
                cury = movy;
                break;
            }
            pi.next();
        }
        Curve.insertLine(curves, curx, cury, movx, movy);
        AreaOp operator;
        if (windingRule == PathIterator.WIND_EVEN_ODD) {
            operator = new AreaOp.EOWindOp();
        } else {
            operator = new AreaOp.NZWindOp();
        }
        return operator.calculate(curves, EmptyCurves);
    }

    /**
     * Adds the shape of the specified <code>BezierArea</code> to the
     * shape of this <code>BezierArea</code>.
     * The resulting shape of this <code>BezierArea</code> will include
     * the union of both shapes, or all areas that were contained
     * in either this or the specified <code>BezierArea</code>.
     * <pre>
     *     // Example:
     *     BezierArea a1 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     BezierArea a2 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.add(a2);
     *
     *        a1(before)     +         a2         =     a1(after)
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param   rhs  the <code>BezierArea</code> to be added to the
     *          current shape
     * @throws NullPointerException if <code>rhs</code> is null
     * @since 1.2
     */
    public void add(BezierArea rhs) {
        curves = new AreaOp.AddOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * Subtracts the shape of the specified <code>BezierArea</code> from the
     * shape of this <code>BezierArea</code>.
     * The resulting shape of this <code>BezierArea</code> will include
     * areas that were contained only in this <code>BezierArea</code>
     * and not in the specified <code>BezierArea</code>.
     * <pre>
     *     // Example:
     *     BezierArea a1 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     BezierArea a2 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.subtract(a2);
     *
     *        a1(before)     -         a2         =     a1(after)
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     * @param   rhs  the <code>BezierArea</code> to be subtracted from the
     *          current shape
     * @throws NullPointerException if <code>rhs</code> is null
     * @since 1.2
     */
    public void subtract(BezierArea rhs) {
        curves = new AreaOp.SubOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * Sets the shape of this <code>BezierArea</code> to the intersection of
     * its current shape and the shape of the specified <code>BezierArea</code>.
     * The resulting shape of this <code>BezierArea</code> will include
     * only areas that were contained in both this <code>BezierArea</code>
     * and also in the specified <code>BezierArea</code>.
     * <pre>
     *     // Example:
     *     BezierArea a1 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     BezierArea a2 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.intersect(a2);
     *
     *      a1(before)   intersect     a2         =     a1(after)
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     * @param   rhs  the <code>BezierArea</code> to be intersected with this
     *          <code>BezierArea</code>
     * @throws NullPointerException if <code>rhs</code> is null
     * @since 1.2
     */
    public void intersect(BezierArea rhs) {
        curves = new AreaOp.IntOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * Sets the shape of this <code>BezierArea</code> to be the combined area
     * of its current shape and the shape of the specified <code>BezierArea</code>,
     * minus their intersection.
     * The resulting shape of this <code>BezierArea</code> will include
     * only areas that were contained in either this <code>BezierArea</code>
     * or in the specified <code>BezierArea</code>, but not in both.
     * <pre>
     *     // Example:
     *     BezierArea a1 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     BezierArea a2 = new BezierArea([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.exclusiveOr(a2);
     *
     *        a1(before)    xor        a2         =     a1(after)
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param   rhs  the <code>BezierArea</code> to be exclusive ORed with this
     *          <code>BezierArea</code>.
     * @throws NullPointerException if <code>rhs</code> is null
     * @since 1.2
     */
    public void exclusiveOr(BezierArea rhs) {
        curves = new AreaOp.XorOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
    }

    /**
     * Removes all of the geometry from this <code>BezierArea</code> and
     * restores it to an empty area.
     * @since 1.2
     */
    public void reset() {
        curves = new Vector<>();
        invalidateBounds();
    }

    /**
     * Tests whether this <code>BezierArea</code> object encloses any area.
     * @return    <code>true</code> if this <code>BezierArea</code> object
     * represents an empty area; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean isEmpty() {
        return (curves.size() == 0);
    }

    /**
     * Tests whether this <code>BezierArea</code> consists entirely of
     * straight edged polygonal geometry.
     * @return    <code>true</code> if the geometry of this
     * <code>BezierArea</code> consists entirely of line segments;
     * <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean isPolygonal() {
        Enumeration<Curve> enum_ = curves.elements();
        while (enum_.hasMoreElements()) {
            if ((enum_.nextElement()).getOrder() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether this <code>BezierArea</code> is rectangular in shape.
     * @return    <code>true</code> if the geometry of this
     * <code>BezierArea</code> is rectangular in shape; <code>false</code>
     * otherwise.
     * @since 1.2
     */
    public boolean isRectangular() {
        int size = curves.size();
        if (size == 0) {
            return true;
        }
        if (size > 3) {
            return false;
        }
        Curve c1 = curves.get(1);
        Curve c2 = curves.get(2);
        if (c1.getOrder() != 1 || c2.getOrder() != 1) {
            return false;
        }
        if (c1.getXTop() != c1.getXBot() || c2.getXTop() != c2.getXBot()) {
            return false;
        }
        if (c1.getYTop() != c2.getYTop() || c1.getYBot() != c2.getYBot()) {
            // One might be able to prove that this is impossible...
            return false;
        }
        return true;
    }

    /**
     * Tests whether this <code>BezierArea</code> is comprised of a single
     * closed subpath.  This method returns <code>true</code> if the
     * path contains 0 or 1 subpaths, or <code>false</code> if the path
     * contains more than 1 subpath.  The subpaths are counted by the
     * number of {@link PathIterator#SEG_MOVETO SEG_MOVETO}  segments
     * that appear in the path.
     * @return    <code>true</code> if the <code>BezierArea</code> is comprised
     * of a single basic geometry; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean isSingular() {
        if (curves.size() < 3) {
            return true;
        }
        Enumeration<Curve> enum_ = curves.elements();
        enum_.nextElement(); // First Order0 "moveto"
        while (enum_.hasMoreElements()) {
            if ((enum_.nextElement()).getOrder() == 0) {
                return false;
            }
        }
        return true;
    }

    private Rectangle2D cachedBounds;
    private void invalidateBounds() {
        cachedBounds = null;
    }
    private Rectangle2D getCachedBounds() {
        if (cachedBounds != null) {
            return cachedBounds;
        }
        Rectangle2D r = new Rectangle2D.Double();
        if (curves.size() > 0) {
            Curve c = curves.get(0);
            // First point is always an order 0 curve (moveto)
            r.setRect(c.getX0(), c.getY0(), 0, 0);
            for (int i = 1; i < curves.size(); i++) {
                (curves.get(i)).enlarge(r);
            }
        }
        return (cachedBounds = r);
    }

    /**
     * Returns a high precision bounding {@link Rectangle2D} that
     * completely encloses this <code>BezierArea</code>.
     * <p>
     * The BezierArea class will attempt to return the tightest bounding
     * box possible for the Shape.  The bounding box will not be
     * padded to include the control points of curves in the outline
     * of the Shape, but should tightly fit the actual geometry of
     * the outline itself.
     * @return    the bounding <code>Rectangle2D</code> for the
     * <code>BezierArea</code>.
     * @since 1.2
     */
    public Rectangle2D getBounds2D() {
        return getCachedBounds().getBounds2D();
    }

    /**
     * Returns a bounding {@link Rectangle} that completely encloses
     * this <code>BezierArea</code>.
     * <p>
     * The BezierArea class will attempt to return the tightest bounding
     * box possible for the Shape.  The bounding box will not be
     * padded to include the control points of curves in the outline
     * of the Shape, but should tightly fit the actual geometry of
     * the outline itself.  Since the returned object represents
     * the bounding box with integers, the bounding box can only be
     * as tight as the nearest integer coordinates that encompass
     * the geometry of the Shape.
     * @return    the bounding <code>Rectangle</code> for the
     * <code>BezierArea</code>.
     * @since 1.2
     */
    public Rectangle getBounds() {
        return getCachedBounds().getBounds();
    }

    /**
     * Returns an exact copy of this <code>BezierArea</code> object.
     * @return    Created clone object
     * @since 1.2
     */
    public Object clone() {
        return new BezierArea(this);
    }

    /**
     * Tests whether the geometries of the two <code>BezierArea</code> objects
     * are equal.
     * This method will return false if the argument is null.
     * @param   other  the <code>BezierArea</code> to be compared to this
     *          <code>BezierArea</code>
     * @return  <code>true</code> if the two geometries are equal;
     *          <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean equals(BezierArea other) {
        // REMIND: A *much* simpler operation should be possible...
        // Should be able to do a curve-wise comparison since all Areas
        // should evaluate their curves in the same top-down order.
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        Vector<Curve> c = new AreaOp.XorOp().calculate(this.curves, other.curves);
        return c.isEmpty();
    }

    /**
     * Transforms the geometry of this <code>BezierArea</code> using the specified
     * {@link AffineTransform}.  The geometry is transformed in place, which
     * permanently changes the enclosed area defined by this object.
     * @param t  the transformation used to transform the area
     * @throws NullPointerException if <code>t</code> is null
     * @since 1.2
     */
    public void transform(AffineTransform t) {
        if (t == null) {
            throw new NullPointerException("transform must not be null");
        }
        // REMIND: A simpler operation can be performed for some types
        // of transform.
        curves = pathToCurves(getPathIterator(t));
        invalidateBounds();
    }

    /**
     * Creates a new <code>BezierArea</code> object that contains the same
     * geometry as this <code>BezierArea</code> transformed by the specified
     * <code>AffineTransform</code>.  This <code>BezierArea</code> object
     * is unchanged.
     * @param t  the specified <code>AffineTransform</code> used to transform
     *           the new <code>BezierArea</code>
     * @throws NullPointerException if <code>t</code> is null
     * @return   a new <code>BezierArea</code> object representing the transformed
     *           geometry.
     * @since 1.2
     */
    public BezierArea createTransformedArea(AffineTransform t) {
        BezierArea a = new BezierArea(this);
        a.transform(t);
        return a;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y) {
        if (!getCachedBounds().contains(x, y)) {
            return false;
        }
        Enumeration<Curve> enum_ = curves.elements();
        int crossings = 0;
        while (enum_.hasMoreElements()) {
            Curve c = enum_.nextElement();
            crossings += c.crossingsFor(x, y);
        }
        return ((crossings & 1) == 1);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(double x, double y, double w, double h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().contains(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c != null && c.covers(y, y+h));
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().intersects(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c == null || !c.isEmpty());
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Creates a {@link PathIterator} for the outline of this
     * <code>BezierArea</code> object.  This <code>BezierArea</code> object is unchanged.
     * @param at an optional <code>AffineTransform</code> to be applied to
     * the coordinates as they are returned in the iteration, or
     * <code>null</code> if untransformed coordinates are desired
     * @return    the <code>PathIterator</code> object that returns the
     *          geometry of the outline of this <code>BezierArea</code>, one
     *          segment at a time.
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at) {
        return new BezierAreaIterator(curves, at);
    }

    /**
     * Creates a <code>PathIterator</code> for the flattened outline of
     * this <code>BezierArea</code> object.  Only uncurved path segments
     * represented by the SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point
     * types are returned by the iterator.  This <code>BezierArea</code>
     * object is unchanged.
     * @param at an optional <code>AffineTransform</code> to be
     * applied to the coordinates as they are returned in the
     * iteration, or <code>null</code> if untransformed coordinates
     * are desired
     * @param flatness the maximum amount that the control points
     * for a given curve can vary from colinear before a subdivided
     * curve is replaced by a straight line connecting the end points
     * @return    the <code>PathIterator</code> object that returns the
     * geometry of the outline of this <code>BezierArea</code>, one segment
     * at a time.
     * @since 1.2
     */
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
}

strictfp class BezierAreaIterator implements PathIterator {
    private AffineTransform transform;
    private Vector<Curve> curves;
    private int index;
    private Curve prevcurve;
    private Curve thiscurve;

    public BezierAreaIterator(Vector<Curve> curves, AffineTransform at) {
        this.curves = curves;
        this.transform = at;
        if (curves.size() >= 1) {
            thiscurve = curves.get(0);
        }
    }

    public int getWindingRule() {
        // REMIND: Which is better, EVEN_ODD or NON_ZERO?
        //         The paths calculated could be classified either way.
        //return WIND_EVEN_ODD;
        return WIND_NON_ZERO;
    }

    public boolean isDone() {
        return (prevcurve == null && thiscurve == null);
    }

    public void next() {
        if (prevcurve != null) {
            prevcurve = null;
        } else {
            prevcurve = thiscurve;
            index++;
            if (index < curves.size()) {
                thiscurve = curves.get(index);
                if (thiscurve.getOrder() != 0 &&
                    prevcurve.getX1() == thiscurve.getX0() &&
                    prevcurve.getY1() == thiscurve.getY0())
                {
                    prevcurve = null;
                }
            } else {
                thiscurve = null;
            }
        }
    }

    public int currentSegment(float coords[]) {
        double dcoords[] = new double[6];
        int segtype = currentSegment(dcoords);
        int numpoints = (segtype == SEG_CLOSE ? 0
                         : (segtype == SEG_QUADTO ? 2
                            : (segtype == SEG_CUBICTO ? 3
                               : 1)));
        for (int i = 0; i < numpoints * 2; i++) {
            coords[i] = (float) dcoords[i];
        }
        return segtype;
    }

    public int currentSegment(double coords[]) {
        int segtype;
        int numpoints;
        if (prevcurve != null) {
            // Need to finish off junction between curves
            if (thiscurve == null || thiscurve.getOrder() == 0) {
                return SEG_CLOSE;
            }
            coords[0] = thiscurve.getX0();
            coords[1] = thiscurve.getY0();
            segtype = SEG_LINETO;
            numpoints = 1;
        } else if (thiscurve == null) {
            throw new NoSuchElementException("area iterator out of bounds");
        } else {
            segtype = thiscurve.getSegment(coords);
            numpoints = thiscurve.getOrder();
            if (numpoints == 0) {
                numpoints = 1;
            }
        }
        if (transform != null) {
            transform.transform(coords, 0, coords, 0, numpoints);
        }
        return segtype;
    }
}
