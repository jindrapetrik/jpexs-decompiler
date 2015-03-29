/*
 * $Id: EContourPoint.java,v 1.10 2004/12/27 04:56:02 eed3si9n Exp $
 *
 * $Copyright: copyright (c) 2004, e.e d3si9n $
 * $License:
 * This source code is part of DoubleType.
 * DoubleType is a graphical typeface designer.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * In addition, as a special exception, e.e d3si9n gives permission to
 * link the code of this program with any Java Platform that is available
 * to public with free of charge, including but not limited to
 * Sun Microsystem's JAVA(TM) 2 RUNTIME ENVIRONMENT (J2RE),
 * and distribute linked combinations including the two.
 * You must obey the GNU General Public License in all respects for all
 * of the code used other than Java Platform. If you modify this file,
 * you may extend this exception to your version of the file, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 * $
 */
package org.doubletype.ossa.adapter;

/**
 * @author e.e
 */
public final class EContourPoint implements EPoint {

    private boolean isOn;

    private double x;

    private double y;

    private EControlPoint controlPoint1;

    private EControlPoint controlPoint2;

    public EContourPoint() {
        super();
    }

    public EContourPoint(double a_x, double a_y, boolean a_isOn) {
        x = a_x;
        y = a_y;

        setOn(a_isOn);
    }

    public static final int k_defaultPixelSize = 16;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public EControlPoint getControlPoint1() {
        return controlPoint1;
    }

    public EControlPoint getControlPoint2() {
        return controlPoint2;
    }

    public void setControlPoint1(EControlPoint a_point) {
        controlPoint1 = a_point;
    }

    public void setControlPoint2(EControlPoint a_point) {
        controlPoint2 = a_point;
    }

    public boolean hasControlPoint1() {
        return getControlPoint1() != null;
    }

    public boolean hasControlPoint2() {
        return getControlPoint2() != null;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean a_isOnCurve) {
        isOn = a_isOnCurve;
    }
}
