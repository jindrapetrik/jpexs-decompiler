package fontastic;

/*
 * Fontastic
 * A font file writer to create TTF and WOFF (Webfonts).
 * http://code.andreaskoller.com/libraries/fontastic
 *
 * Copyright (C) 2013 Andreas Koller http://andreaskoller.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Andreas Koller http://andreaskoller.com
 * @modified 06/19/2013
 * @version 0.4 (4)
 */
/**
 * Class FPoint extends PVector
 *
 * Stores a point with x and y coordinates and optional PVector controlPoint1
 * and controlPoint2.
 *
 */
public class FPoint {

    public double x;

    public double y;

    public FPoint controlPoint;

    private boolean hasControlPoint;

    public FPoint() {
    }

    public FPoint(FPoint point) {
        this.x = point.x;
        this.y = point.y;
        this.hasControlPoint = false;
    }

    public FPoint(double x, double y) {
        this.x = x;
        this.y = y;
        this.hasControlPoint = false;
    }

    public FPoint(FPoint point, FPoint controlPoint) {
        this.x = point.x;
        this.y = point.y;
        this.controlPoint = controlPoint;
        this.hasControlPoint = true;
    }

    public void setControlPoint(FPoint controlPoint1) {
        this.controlPoint = controlPoint1;
        this.hasControlPoint = true;
    }

    public void setControlPoint(float x, float y) {
        this.controlPoint = new FPoint(x, y);
        this.hasControlPoint = true;
    }

    public boolean hasControlPoint1() {
        return hasControlPoint;
    }
}
