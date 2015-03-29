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
import java.util.ArrayList;
import java.util.List;

/**
 * Class FContour
 *
 * Stores a contour (list of FPoint).
 *
 */
public class FContour {

    List<FPoint> points;

    FContour() {
        this.points = new ArrayList<>();
    }

    FContour(FPoint[] points) {
        this.points = new ArrayList<>();
        for (FPoint p : points) {
            this.points.add(p);
        }
    }

    FContour(FPoint[] points, FPoint[] controlpoints) {
        this.points = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            this.points.add(new FPoint(points[i], controlpoints[i]));
        }
    }

    public List<FPoint> getPoints() {
        return points;
    }

    public FPoint[] getPointsArray() {
        FPoint[] pointsArray = points.toArray(new FPoint[points.size()]);
        return pointsArray;
    }

    public void setPoints(FPoint[] points) {
        this.points = new ArrayList<>();
        for (FPoint p : points) {
            this.points.add(p);
        }
    }
}
