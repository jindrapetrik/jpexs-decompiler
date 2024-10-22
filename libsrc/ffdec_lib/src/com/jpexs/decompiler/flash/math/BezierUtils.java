/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.math;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Bezier utils. Based on
 * https://code.google.com/archive/p/degrafa/source/default/source which is
 * derived from an algorithm in the book "Graphics Gems"
 * <p>
 * This modification does not have cubic curves support.
 */
public class BezierUtils {

    private static final int MAX_DEPTH = 64; // maximum recursion depth
    private static final double[] Z_QUAD = new double[]{1.0, 2.0 / 3.0, 1.0 / 3.0, 1 / 3.0, 2.0 / 3.0, 1.0};
    private static final double EPSILON = 1.0 * Math.pow(2, -MAX_DEPTH - 1); // flatness tolerance

    /**
     * Constructor.
     */
    public BezierUtils() {

    }

    /**
     * Calculate point at t.
     * @param t Position
     * @param p0 Start point
     * @param p1 Control point
     * @param p2 End point
     * @return Point
     */
    public Point2D pointAt(double t, Point2D p0, Point2D p1, Point2D p2) {
        double xt = (1 - t) * (1 - t) * p0.getX() + 2 * (1 - t) * t * p1.getX() + t * t * p2.getX();
        double yt = (1 - t) * (1 - t) * p0.getY() + 2 * (1 - t) * t * p1.getY() + t * t * p2.getY();
        return new Point2D.Double(xt, yt);
    }

    /**
     * Calculate closest point to bezier.
     * @param _p Point
     * @param p0 Start point
     * @param p1 Control point
     * @param p2 End point
     * @return Position
     */
    public double closestPointToBezier(Point2D _p, Point2D p0, Point2D p1, Point2D p2) {
        Point2D p = p0;
        double deltaX = p.getX() - _p.getX();
        double deltaY = p.getY() - _p.getY();
        double d0 = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        p = p2;
        deltaX = p.getX() - _p.getX();
        deltaY = p.getY() - _p.getY();
        double d1 = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        int n = 2; // degree of input Bezier curve

        // array of control points
        List<Point2D> v = new ArrayList<>();
        v.add(p0);
        v.add(p1);
        v.add(p2);

        // instead of power form, convert the function whose zeros are required to Bezier form
        List<Point2D> w = toBezierForm(_p, v);

        // Find roots of the Bezier curve with control points stored in 'w' (algorithm is recursive, this is root depth of 0)
        List<Double> roots = findRoots(w, 2 * n - 1, 0);

        // compare the candidate distances to the endpoints and declare a winner :)
        double tMinimum;
        double dMinimum;
        if (d0 < d1) {
            tMinimum = 0;
            dMinimum = d0;
        } else {
            tMinimum = 1;
            dMinimum = d1;
        }

        // tbd - compare 2-norm squared
        for (int i = 0; i < roots.size(); i++) {
            double t = roots.get(i);
            if (t >= 0 && t <= 1) {
                p = pointAt(t, p0, p1, p2);
                deltaX = p.getX() - _p.getX();
                deltaY = p.getY() - _p.getY();
                double d = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                if (d < dMinimum) {
                    tMinimum = t;
                    dMinimum = d;
                }
            }
        }

        // tbd - alternate optima.
        return tMinimum;

    }

    private List<Point2D> toBezierForm(Point2D _p, List<Point2D> _v) {
        List<Point2D> c = new ArrayList<>(); // V(i) - P
        List<Point2D> d = new ArrayList<>(); // V(i+1) - V(i)
        List<Point2D> w = new ArrayList<>(); // control-points for Bezier curve whose zeros represent candidates for closest point to the input parametric curve
        int n = _v.size() - 1; //degree of B(t)
        int degree = 2 * n - 1; // degree of B(t) . P
        double pX = _p.getX();
        double pY = _p.getY();

        Point2D v;
        for (int i = 0; i <= n; i++) {
            v = _v.get(i);
            c.add(new Point2D.Double(v.getX() - pX, v.getY() - pY));
        }

        double s = n;
        for (int i = 0; i <= n - 1; i++) {
            v = _v.get(i);
            Point2D v1 = _v.get(i + 1);
            d.add(new Point2D.Double(s * (v1.getX() - v.getX()), s * (v1.getY() - v.getY())));
        }

        List<Double> cd = new ArrayList<>();
        for (int row = 0; row <= n - 1; row++) {
            Point2D di = d.get(row);
            double dX = di.getX();
            double dY = di.getY();
            for (int col = 0; col <= n; col++) {
                int k = getLinearIndex(n + 1, row, col);
                cd.add(dX * c.get(col).getX() + dY * c.get(col).getY());
            }
        }

        // Bezier is uniform parameterized
        double dInv = 1.0 / degree;
        for (int i = 0; i <= degree; i++) {
            w.add(new Point2D.Double(i * dInv, 0));
        }

        // reference to appropriate pre-computed coefficients
        double[] z = Z_QUAD;

        // accumulate y-coords of the control points along the skew diagonal of the (n-1) x n matrix of c.d and z values
        int m = n - 1;
        for (int k = 0; k <= n + m; k++) {
            int lb = Math.max(0, k - m);
            int ub = Math.min(k, n);
            for (int i = lb; i <= ub; i++) {
                int j = k - i;
                Point2D p = w.get(i + j);
                int index = getLinearIndex(n + 1, j, i);
                p.setLocation(p.getX(), p.getY() + cd.get(index) * z[index]);
                w.set(i + j, p);
            }
        }
        return w;
    }

    // how many times does the Bezier curve cross the horizontal axis - the number of roots is less than or equal to this count
    private int crossingCount(List<Point2D> _v, int _degree) {
        int nCrossings = 0;
        int sign = _v.get(0).getY() < 0 ? -1 : 1;
        int oldSign = sign;
        for (int i = 1; i <= _degree; i++) {
            sign = _v.get(i).getY() < 0 ? -1 : 1;
            if (sign != oldSign) {
                nCrossings++;
            }

            oldSign = sign;
        }

        return nCrossings;
    }

    // convert 2D array indices in a k x n matrix to a linear index (this is an interim step ahead of a future implementation optimized for 1D array indexing)
    private int getLinearIndex(int _n, int _row, int _col) {
        // no range-checking; you break it ... you buy it!
        return _row * _n + _col;
    }

    /**
     * subdivide( _c:Array, _t:Number, _left:Array, _right:Array ) - deCasteljau
     * subdivision of an arbitrary-order Bezier curve
     *
     * @param _c Array array of control points for the Bezier curve
     * @param _t Number t-parameter at which the curve is subdivided (must be in
     * (0,1) = no check at this point
     * @param _left Array reference to an array in which the control points,
     * <code>Array</code> of <code>Point</code> references, of the left control
     * cage after subdivision are stored
     * @param _right Array reference to an array in which the control points,
     * <code>Array</code> of <code>Point</code> references, of the right control
     * cage after subdivision are stored
     * @since 1.0
     */
    public void subdivide(List<Point2D> _c, double _t, List<Point2D> _left, List<Point2D> _right) {
        int degree = _c.size() - 1;
        int n = degree + 1;
        List<Point2D> p = new ArrayList<>(_c);
        double t1 = 1.0 - _t;

        for (int i = 1; i <= degree; ++i) {
            for (int j = 0; j <= degree - i; ++j) {
                Point2D vertex = new Point2D.Double();
                int ij = getLinearIndex(n, i, j);
                int im1j = getLinearIndex(n, i - 1, j);
                int im1jp1 = getLinearIndex(n, i - 1, j + 1);

                vertex.setLocation(t1 * p.get(im1j).getX() + _t * p.get(im1jp1).getX(), t1 * p.get(im1j).getY() + _t * p.get(im1jp1).getY());
                while (ij >= p.size()) {
                    p.add(new Point2D.Double(0, 0));
                }
                p.set(ij, vertex);
            }
        }

        for (int j = 0; j <= degree; j++) {
            int index = getLinearIndex(n, j, 0);
            _left.add(p.get(index));
        }

        for (int j = 0; j <= degree; j++) {
            int index = getLinearIndex(n, degree - j, j);
            _right.add(p.get(index));
        }
    }

    // is the control polygon for a Bezier curve suitably linear for subdivision to terminate?
    private boolean isControlPolygonLinear(List<Point2D> _v, int _degree) {
        // Given array of control points, _v, find the distance from each interior control point to line connecting v[0] and v[degree]

        // implicit equation for line connecting first and last control points
        double a = _v.get(0).getY() - _v.get(_degree).getY();
        double b = _v.get(_degree).getX() - _v.get(0).getX();
        double c = _v.get(0).getX() * _v.get(_degree).getY() - _v.get(_degree).getX() * _v.get(0).getY();

        double abSquared = a * a + b * b;
        List<Double> distance = new ArrayList<>(); // Distances from control points to line

        distance.add(0.0);
        for (int i = 1; i < _degree; i++) {
            // Compute distance from each of the points to that line
            distance.add(a * _v.get(i).getX() + b * _v.get(i).getY() + c);
            if (distance.get(i) > 0.0) {
                distance.set(i, (distance.get(i) * distance.get(i)) / abSquared);
            }
            if (distance.get(i) < 0.0) {
                distance.set(i, -((distance.get(i) * distance.get(i)) / abSquared));
            }
        }

        // Find the largest distance
        double maxDistanceAbove = 0.0;
        double maxDistanceBelow = 0.0;
        for (int i = 1; i < _degree; i++) {
            if (distance.get(i) < 0.0) {
                maxDistanceBelow = Math.min(maxDistanceBelow, distance.get(i));
            }
            if (distance.get(i) > 0.0) {
                maxDistanceAbove = Math.max(maxDistanceAbove, distance.get(i));
            }
        }

        // Implicit equation for zero line
        double a1 = 0.0;
        double b1 = 1.0;
        double c1 = 0.0;

        // Implicit equation for "above" line
        double a2 = a;
        double b2 = b;
        double c2 = c + maxDistanceAbove;

        double det = a1 * b2 - a2 * b1;
        double dInv = 1.0 / det;

        double intercept1 = (b1 * c2 - b2 * c1) * dInv;

        //  Implicit equation for "below" line
        a2 = a;
        b2 = b;
        c2 = c + maxDistanceBelow;

        double intercept2 = (b1 * c2 - b2 * c1) * dInv;

        // Compute intercepts of bounding box
        double leftIntercept = Math.min(intercept1, intercept2);
        double rightIntercept = Math.max(intercept1, intercept2);

        double error = 0.5 * (rightIntercept - leftIntercept);

        return error < EPSILON;
    }

    // return roots in [0,1] of a polynomial in Bernstein-Bezier form
    private List<Double> findRoots(List<Point2D> _w, int _degree, int _depth) {
        List<Double> t = new ArrayList<>(); // t-values of roots
        int m = 2 * _degree - 1;

        switch (crossingCount(_w, _degree)) {
            case 0:
                return new ArrayList<>();
            case 1:
                // Unique solution - stop recursion when the tree is deep enough (return 1 solution at midpoint)
                if (_depth >= MAX_DEPTH) {
                    t.add(0.5 * (_w.get(0).getX() + _w.get(m).getX()));
                    return t;
                }

                if (isControlPolygonLinear(_w, _degree)) {
                    t.add(computeXIntercept(_w, _degree));
                    return t;
                }
                break;
        }

        // Otherwise, solve recursively after subdividing control polygon
        List<Point2D> left = new ArrayList<>();
        List<Point2D> right = new ArrayList<>();

        // child solutions
        subdivide(_w, 0.5, left, right);
        List<Double> leftT = findRoots(left, _degree, _depth + 1);
        List<Double> rightT = findRoots(right, _degree, _depth + 1);

        t.addAll(leftT);
        t.addAll(rightT);
        return t;
    }

    // compute intersection of line segment from first to last control point with horizontal axis
    private double computeXIntercept(List<Point2D> _v, int _degree) {
        double XNM = _v.get(_degree).getX() - _v.get(0).getX();
        double YNM = _v.get(_degree).getY() - _v.get(0).getY();
        double XMK = _v.get(0).getX();
        double YMK = _v.get(0).getY();

        double detInv = -1.0 / YNM;

        return (XNM * YMK - YNM * XMK) * detInv;
    }

}
