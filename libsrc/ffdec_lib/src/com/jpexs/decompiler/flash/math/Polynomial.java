/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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

import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Based on Node.js library kld-polynomial:
 * https://github.com/thelonious/kld-polynomial
 */
public class Polynomial {

    private double[] coefs;
    private String _variable;
    private int _s;

    /**
     * Constructor.
     * @param coefs Coefficients of the polynomial
     */
    public Polynomial(List<Double> coefs) {
        this.coefs = new double[coefs.size()];

        for (int i = 0; i < coefs.size(); i++) {
            this.coefs[i] = coefs.get(coefs.size() - 1 - i);
        }

        this._variable = "t";
        this._s = 0;
    }

    /**
     * Get the degree of the polynomial.
     * @return Degree of the polynomial
     */
    public int getDegree() {
        return this.coefs.length - 1;
    }

    private List<Double> getLinearRoot() {
        List<Double> result = new ArrayList<>();
        double a = this.coefs[1];

        if (a != 0) {
            result.add(-this.coefs[0] / a);
        }

        return result;
    }

    private List<Double> getQuadraticRoots() {
        List<Double> results = new ArrayList<>();

        if (this.getDegree() == 2) {
            double a = this.coefs[2];
            double b = this.coefs[1] / a;
            double c = this.coefs[0] / a;
            double d = b * b - 4 * c;

            if (d > 0) {
                double e = Math.sqrt(d);

                results.add(0.5 * (-b + e));
                results.add(0.5 * (-b - e));
            } else if (d == 0) {
                // really two roots with same value, but we only return one
                results.add(0.5 * -b);
            }
            // else imaginary results
        }

        return results;
    }

    private boolean coefSelectionFunc(int i, int n, double[] a) {
        return i < n && a[i] < 0;
    }

    private void find2Max(Reference<Double> max, Reference<Double> nearmax, double bi, int i, int n, double[] a) {
        if (coefSelectionFunc(i, n, a)) {
            if (max.getVal() < bi) {
                nearmax.setVal(max.getVal());
                max.setVal(bi);
            } else if (nearmax.getVal() < bi) {
                nearmax.setVal(bi);
            }
        }
    }

    ;

    /**
     * @return negX, posX
     */
    private double[] boundsUpperRealFujiwara() {
        double[] ax = this.coefs;
        double[] a = ax;
        int n = a.length - 1;
        double an = a[n];

        if (an != 1) {
            a = new double[a.length];
            for (int i = 0; i < this.coefs.length; i++) {
                a[i] = ax[i] / an;
            }
        }

        double[] b = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            double v = a[i];
            if (i < n) {
                b[i] = Math.pow(Math.abs((i == 0) ? v / 2 : v), 1 / (double) (n - i));
            } else {
                b[i] = v;
            }
        }

        // eslint-disable-next-line unicorn/no-fn-reference-in-iterator
        double max_pos = 0;
        double nearmax_pos = 0;
        for (int i = 0; i < b.length; i++) {
            if (i < n && a[i] < 0) {
                if (max_pos < b[i]) {
                    nearmax_pos = max_pos;
                    max_pos = b[i];
                } else if (nearmax_pos < b[i]) {
                    nearmax_pos = b[i];
                }
            }
        }

        double max_neg = 0;
        double nearmax_neg = 0;
        for (int i = 0; i < b.length; i++) {
            if (i < n && ((n % 2 == i % 2) ? a[i] < 0 : a[i] > 0)) {
                if (max_neg < b[i]) {
                    nearmax_neg = max_neg;
                    max_neg = b[i];
                } else if (nearmax_neg < b[i]) {
                    nearmax_neg = b[i];
                }
            }
        }

        return new double[]{
            -2 * max_neg,
            2 * max_pos
        };
    }

    private class Rect {

        double minX;
        double minY;
        double maxX;
        double maxY;

        public Rect(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

    }

    private double[] boundsLowerRealFujiwara() {
        Polynomial poly = new Polynomial(new ArrayList<>());

        poly.coefs = new double[coefs.length];
        for (int i = 0; i < coefs.length; i++) {
            poly.coefs[coefs.length - 1 - i] = coefs[i];
        }

        double[] res = poly.boundsUpperRealFujiwara();

        res[0] = 1 / res[0];
        res[1] = 1 / res[1];

        return res;
    }

    private Rect bounds() {
        double[] urb = this.boundsUpperRealFujiwara();
        Rect rb = new Rect(urb[0], 0, urb[1], 0);

        if (urb[0] == 0 && urb[1] == 0) {
            return rb;
        }

        if (urb[0] == 0) {
            rb.minX = this.boundsLowerRealFujiwara()[1];
        } else if (urb[1] == 0) {
            rb.maxX = this.boundsLowerRealFujiwara()[0];
        }

        if (rb.minX > rb.maxX) {
            rb.minX = rb.maxX = 0;
        }

        return rb;
        // TODO: if sure that there are no complex roots
        // (maybe by using Sturm's theorem) use:
        // return this.boundsRealLaguerre();
    }

    private double eval(double x) {
        if (Double.isNaN(x)) {
            throw new RuntimeException("Parameter must be a number");
        }

        double result = 0;

        for (int i = this.coefs.length - 1; i >= 0; i--) {
            result = result * x + this.coefs[i];
        }

        return result;
    }

    /**
     * Estimate the error of the polynomial.
     * @return Error estimate
     */
    public double zeroErrorEstimate() {
        return zeroErrorEstimate(null);
    }

    /**
     * Estimate the error of the polynomial.
     * @param maxAbsX Maximum absolute value of x
     * @return Error estimate
     */
    public double zeroErrorEstimate(Double maxAbsX) {
        Polynomial poly = this;
        double ERRF = 1e-15;

        if (maxAbsX == null) {
            Rect rb = poly.bounds();

            maxAbsX = Math.max(Math.abs(rb.minX), Math.abs(rb.maxX));
        }

        if (maxAbsX < 0.001) {
            return 2 * Math.abs(poly.eval(ERRF));
        }

        int n = poly.coefs.length - 1;
        double an = poly.coefs[n];

        double m = 0;
        for (int i = 0; i < poly.coefs.length; i++) {
            double v = poly.coefs[i];
            double nm = v / an * Math.pow(maxAbsX, i);
            m = nm > m ? nm : m;
        }

        double x = 10 * ERRF * m;
        return x;
    }

    /**
     * Gets the roots of the polynomial.
     * @return List of roots
     */
    public List<Double> getCubicRoots() {
        List<Double> results = new ArrayList<>();

        if (this.getDegree() == 3) {
            double c3 = this.coefs[3];
            double c2 = this.coefs[2] / c3;
            double c1 = this.coefs[1] / c3;
            double c0 = this.coefs[0] / c3;

            double a = (3 * c1 - c2 * c2) / 3;
            double b = (2 * c2 * c2 * c2 - 9 * c1 * c2 + 27 * c0) / 27;
            double offset = c2 / 3;
            double discrim = b * b / 4 + a * a * a / 27;
            double halfB = b / 2;

            double ZEROepsilon = this.zeroErrorEstimate();

            if (Math.abs(discrim) <= ZEROepsilon) {
                discrim = 0;
            }

            if (discrim > 0) {
                double e = Math.sqrt(discrim);
                double root; // eslint-disable-line no-shadow

                double tmp = -halfB + e;

                if (tmp >= 0) {
                    root = Math.pow(tmp, 1 / 3.0);
                } else {
                    root = -Math.pow(-tmp, 1 / 3.0);
                }

                tmp = -halfB - e;

                if (tmp >= 0) {
                    root += Math.pow(tmp, 1 / 3.0);
                } else {
                    root -= Math.pow(-tmp, 1 / 3.0);
                }

                results.add(root - offset);
            } else if (discrim < 0) {
                double distance = Math.sqrt(-a / 3.0);
                double angle = Math.atan2(Math.sqrt(-discrim), -halfB) / 3.0;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double sqrt3 = Math.sqrt(3);

                results.add(2 * distance * cos - offset);
                results.add(-distance * (cos + sqrt3 * sin) - offset);
                results.add(-distance * (cos - sqrt3 * sin) - offset);
            } else {
                double tmp;

                if (halfB >= 0) {
                    tmp = -Math.pow(halfB, 1 / 3.0);
                } else {
                    tmp = Math.pow(-halfB, 1 / 3.0);
                }

                results.add(2 * tmp - offset);
                // really should return next root twice, but we return only one
                results.add(-tmp - offset);
            }
        }

        return results;
    }

    /**
     * Simplify the polynomial.
     */
    public void simplifyEquals() {
        simplifyEquals(1e-12);
    }

    /**
     * Simplify the polynomial.
     * @param tolerance Tolerance
     */
    public void simplifyEquals(double tolerance) {
        for (int i = this.getDegree(); i >= 0; i--) {
            if (Math.abs(this.coefs[i]) <= tolerance) {
                double[] newc = new double[this.coefs.length - 1];
                for (int j = 0; j < newc.length; j++) {
                    newc[j] = this.coefs[j];
                }
                this.coefs = newc;
            } else {
                break;
            }
        }
    }

    private void divideEqualsScalar(double scalar) {
        for (int i = 0; i < this.coefs.length; i++) {
            this.coefs[i] /= scalar;
        }
    }

    private Polynomial getDerivative() {
        Polynomial pol = new Polynomial(new ArrayList<>());
        List<Double> newCoefs = new ArrayList<>();
        pol.coefs = new double[this.coefs.length - 1];
        for (int i = 1; i < this.coefs.length; i++) {
            pol.coefs[i - 1] = i * this.coefs[i];
        }

        return pol;
    }

    /**
     * Get the roots of the polynomial.
     * @return List of roots
     */
    public List<Double> getRoots() {
        List<Double> result;

        this.simplifyEquals();

        switch (this.getDegree()) {
            case 0:
                result = new ArrayList<>();
                break;
            case 1:
                result = this.getLinearRoot();
                break;
            case 2:
                result = this.getQuadraticRoots();
                break;
            case 3:
                result = this.getCubicRoots();
                break;
            case 4:
                result = this.getQuarticRoots();
                break;
            default:
                result = new ArrayList<>();
        }

        return result;
    }

    private static Double sign(Double x) {
        // eslint-disable-next-line no-self-compare
        if (x == null) {
            return null;
        }
        return x < 0 ? -1.0 : 1.0;
    }

    private interface DoubleFunc {

        public double apply(double val);
    }

    private static double newtonSecantBisection(double x0, DoubleFunc f, DoubleFunc df, int max_iterations, Double min, Double max) {
        double x;
        double prev_dfx = 0;
        double dfx;
        double prev_x_ef_correction = 0;
        double x_correction;
        double x_new;
        double y;
        Double y_atmin = null;
        Double y_atmax = null;

        x = x0;

        double ACCURACY = 14;
        double min_correction_factor = Math.pow(10, -ACCURACY);
        boolean isBounded = min != null && max != null;

        if (isBounded) {
            if (min > max) {
                throw new RuntimeException("Min must be greater than max");
            }

            y_atmin = f.apply(min);
            y_atmax = f.apply(max);

            if (Double.compare(sign(y_atmin), sign(y_atmax)) == 0) {
                throw new RuntimeException("Y values of bounds must be of opposite sign");
            }
        }

        for (int i = 0; i < max_iterations; i++) {
            dfx = df.apply(x);

            if (dfx == 0) {
                if (prev_dfx == 0) {
                    // error
                    throw new RuntimeException("df(x) is zero");
                } else {
                    // use previous derivation value
                    dfx = prev_dfx;
                }
                // or move x a little?
                // dfx = df(x != 0 ? x + x * 1e-15 : 1e-15);
            }

            prev_dfx = dfx;
            y = f.apply(x);
            x_correction = y / dfx;
            x_new = x - x_correction;

            boolean isEnoughCorrection = (Math.abs(x_correction) <= min_correction_factor * Math.abs(x))
                    || (prev_x_ef_correction == (x - x_correction) - x);
            if (isEnoughCorrection) {
                break;
            }

            if (isBounded) {
                if (Double.compare(sign(y), sign(y_atmax)) == 0) {
                    max = x;
                    y_atmax = y;
                } else if (Double.compare(sign(y), sign(y_atmin)) == 0) {
                    min = x;
                    y_atmin = y;
                } else {
                    x = x_new;
                    break;
                }

                if ((x_new < min) || (x_new > max)) {
                    if (Double.compare(sign(y_atmin), sign(y_atmax)) == 0) {
                        break;
                    }

                    double RATIO_LIMIT = 50;
                    double AIMED_BISECT_OFFSET = 0.25; // [0, 0.5)
                    double dy = y_atmax - y_atmin;
                    double dx = max - min;

                    if (dy == 0) {
                        x_correction = x - (min + dx * 0.5);
                    } else if (Math.abs(dy / Math.min(y_atmin, y_atmax)) > RATIO_LIMIT) {
                        x_correction = x - (min + dx * (0.5 + (Math.abs(y_atmin) < Math.abs(y_atmax) ? -AIMED_BISECT_OFFSET : AIMED_BISECT_OFFSET)));
                    } else {
                        x_correction = x - (min - y_atmin / dy * dx);
                    }
                    x_new = x - x_correction;

                    isEnoughCorrection = (Math.abs(x_correction) <= min_correction_factor * Math.abs(x))
                            || (prev_x_ef_correction == (x - x_correction) - x);
                    if (isEnoughCorrection) {
                        break;
                    }
                }
            }

            prev_x_ef_correction = x - x_new;
            x = x_new;
        }

        return x;
    }

    private List<Double> getQuarticRoots() {
        List<Double> results = new ArrayList<>();
        int n = this.getDegree();

        if (n == 4) {
            Polynomial poly = new Polynomial(new ArrayList<>());

            poly.coefs = Arrays.copyOf(this.coefs, this.coefs.length);
            poly.divideEqualsScalar(poly.coefs[n]);

            double ERRF = 1e-15;

            if (Math.abs(poly.coefs[0]) < 10 * ERRF * Math.abs(poly.coefs[3])) {
                poly.coefs[0] = 0;
            }

            Polynomial poly_d = poly.getDerivative();
            List<Double> derrt = poly_d.getRoots();
            derrt.sort(new Comparator<Double>() {
                @Override
                public int compare(Double a, Double b) {
                    if (Double.compare(a, b) == 0) {
                        return 0;
                    }
                    if (a - b < 0) {
                        return -1;
                    }
                    return 1;
                }
            });
            List<Double> dery = new ArrayList<>();
            int nr = derrt.size() - 1;
            Rect rb = this.bounds();

            double maxabsX = Math.max(Math.abs(rb.minX), Math.abs(rb.maxX));
            double ZEROepsilon = this.zeroErrorEstimate(maxabsX);

            for (int i = 0; i <= nr; i++) {
                dery.add(poly.eval(derrt.get(i)));
            }

            for (int i = 0; i <= nr; i++) {
                if (Math.abs(dery.get(i)) < ZEROepsilon) {
                    dery.set(i, 0.0);
                }
            }

            int i = 0;
            double dx = Math.max(0.1 * (rb.maxX - rb.minX) / n, ERRF);
            List<Double> guesses = new ArrayList<>();
            List<double[]> minmax = new ArrayList<>();

            if (nr > -1) {
                if (dery.get(0) != 0) {
                    if (Double.compare(sign(dery.get(0)), sign(poly.eval(derrt.get(0) - dx) - dery.get(0))) != 0) {
                        guesses.add(derrt.get(0) - dx);
                        minmax.add(new double[]{rb.minX, derrt.get(0)});
                    }
                } else {
                    results.add(derrt.get(0));
                    results.add(derrt.get(0));
                    i++;
                }

                for (; i < nr; i++) {
                    if (dery.get(i + 1) == 0) {
                        results.add(derrt.get(i + 1));
                        results.add(derrt.get(i + 1));
                        i++;
                    } else if (Double.compare(sign(dery.get(i)), sign(dery.get(i + 1))) != 0) {
                        guesses.add((derrt.get(i) + derrt.get(i + 1)) / 2);
                        minmax.add(new double[]{derrt.get(i), derrt.get(i + 1)});
                    }
                }
                if (dery.get(nr) != 0 && Double.compare(sign(dery.get(nr)), sign(poly.eval(derrt.get(nr) + dx) - dery.get(nr))) != 0) {
                    guesses.add(derrt.get(nr) + dx);
                    minmax.add(new double[]{derrt.get(nr), rb.maxX});
                }
            }

            DoubleFunc f = new DoubleFunc() {
                @Override
                public double apply(double x) {
                    return poly.eval(x);
                }
            };

            DoubleFunc df = new DoubleFunc() {
                @Override
                public double apply(double x) {
                    return poly_d.eval(x);
                }
            };

            if (!guesses.isEmpty()) {
                for (i = 0; i < guesses.size(); i++) {
                    guesses.set(i, Polynomial.newtonSecantBisection(guesses.get(i), f, df, 32, minmax.get(i)[0], minmax.get(i)[1]));
                }
            }

            results.addAll(guesses);
        }

        return results;
    }
}
