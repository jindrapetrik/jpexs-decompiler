/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * NOT WORKING STUB!!! FIXME
 *
 * @author JPEXS
 */
public class TweenDetector {

    public static List<TweenRange> detectRanges(List<DepthState> depthStates) {
        //TODO: make this working :-(
        if (depthStates.size() < 2 || depthStates.get(0).placeObjectTag == depthStates.get(1).placeObjectTag) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(new TweenRange(0, depthStates.size() - 1)));
        /*

         List<TweenRange> ret = new ArrayList<>();
         double tolerance = 1;
         int min = 3;
         int startpos = 0;
         Double last = null;
         int i = min;
         List<Double> translateX=new ArrayList<>();
         List<Double> translateY=new ArrayList<>();
         List<Double> scaleX=new ArrayList<>();
         List<Double> scaleY=new ArrayList<>();
         List<Double> rotateSkew0=new ArrayList<>();
         List<Double> rotateSkew1=new ArrayList<>();


         Set<MATRIX> ms=new HashSet<MATRIX>();
         ms.addAll(matrices);
         if(ms.size()==1){
         return new ArrayList<>();
         }


         for(MATRIX n:matrices){
         //...
         }

         for (; startpos + i <= matrices.size() + 1; i++) {
         double errTranslateX = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(translateX, startpos, i);
         double errTranslateY = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(translateY, startpos, i);
         double errScaleX = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(scaleX, startpos, i);
         double errScaleY = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(scaleY, startpos, i);
         double errRotateSkew0 = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(rotateSkew0, startpos, i);
         double errRotateSkew1 = startpos + i > matrices.size() ? Double.MAX_VALUE : getErrorLevel(rotateSkew1, startpos, i);
         double err = startpos + i > matrices.size()?Double.MAX_VALUE:(errTranslateX/20+errTranslateY/20+0.1*errScaleX+0.1*errScaleY+errRotateSkew0/360+errRotateSkew1/360);
         if (err > tolerance) {
         if (last == null) {
         startpos++;
         i = min - 1;
         continue;
         }
         ret.add(new TweenRange(startpos, startpos+i-1-1));
         startpos = startpos + i -1;
         i = min - 1;
         last = null;
         continue;
         }
         last = err;
         }
         return ret;*/
    }

    private static double getErrorLevel(List<Double> yValues, int start, int len) {
        double[] ret = calc(yValues, start, len);
        double a = ret[0];
        double b = ret[1];
        double sumdelta = 0;
        double maxdelta = 0;
        for (int i = start; i < start + len; i++) {
            double yorig = yValues.get(i);
            double ynew = a + b * (i - start);
            double ydelta = Math.abs(ynew - yorig);
            sumdelta += ydelta;
            if (ydelta > maxdelta) {
                maxdelta = ydelta;
            }
        }
        return maxdelta; //sumdelta / len;
    }

    private static double[] calc(List<Double> yValues, int start, int len) {
        List<Double> xValues = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            xValues.add((double) i);
        }

        yValues = yValues.subList(start, start + len);

        // get the value of the gradient using the formula b = cov[x,y] / var[x]
        double b = covariance(xValues, yValues) / variance(xValues);
        // get the value of the y-intercept using the formula a = ybar + b * xbar
        double a = mean(yValues) - b * mean(xValues);
        return new double[]{a, b};
    }

    /*
     * Calculate the covariance of two sets of data
     *
     * @param x
     *          The first set of data
     * @param y
     *          The second set of data
     * @return The covariance of x and y
     */
    private static double covariance(List<Double> x, List<Double> y) {
        double xmean = mean(x);
        double ymean = mean(y);

        double result = 0;

        for (int i = 0; i < x.size(); i++) {
            result += (x.get(i) - xmean) * (y.get(i) - ymean);
        }

        result /= x.size() - 1;

        return result;
    }

    /**
     * Calculate the mean of a data set
     *
     * @param data The data set to calculate the mean of
     * @return The mean of the data set
     */
    private static double mean(List<Double> data) {
        double sum = 0;

        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i);
        }

        return sum / data.size();
    }

    /**
     * Calculate the variance of a data set
     *
     * @param data The data set to calculate the variance of
     * @return The variance of the data set
     */
    private static double variance(List<Double> data) {
        // Get the mean of the data set
        double mean = mean(data);

        double sumOfSquaredDeviations = 0;

        // Loop through the data set
        for (int i = 0; i < data.size(); i++) {
            // sum the difference between the data element and the mean squared
            sumOfSquaredDeviations += Math.pow(data.get(i) - mean, 2);
        }

        // Divide the sum by the length of the data set - 1 to get our result
        return sumOfSquaredDeviations / (data.size() - 1);
    }
}
