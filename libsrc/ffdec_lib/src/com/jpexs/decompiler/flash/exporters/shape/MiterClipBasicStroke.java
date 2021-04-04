/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.shape;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MiterClipBasicStroke implements Stroke {

    private final BasicStroke stroke;

    public MiterClipBasicStroke(BasicStroke stroke) {
        this.stroke = stroke;
    }

    private static class Vector {

        public float x1;
        public float y1;
        public float x2;
        public float y2;

        public Vector(float x1, float y1, float x2, float y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public float getLength() {
            return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        }

        public float multiply(Vector v2) {
            return (x2 - x1) * (v2.x2 - v2.x1) + (y2 - y1) * (v2.y2 - v2.y1);
        }

        public float getAngle(Vector v2) {
            return (float) Math.acos(multiply(v2) / (getLength() * v2.getLength()));
        }

        @Override
        public String toString() {
            return "[" + x1 + "," + y1 + "] -> [" + x2 + "," + y2 + "]";
        }

        public Vector reverse() {
            return new Vector(x2, y2, x1, y1);
        }

        public Vector transform(AffineTransform t) {
            Point2D fromSrc = new Point2D.Float(x1, y1);
            Point2D toSrc = new Point2D.Float(x2, y2);
            Point2D fromDest = new Point2D.Float();
            Point2D toDest = new Point2D.Float();
            t.transform(fromSrc, fromDest);
            t.transform(toSrc, toDest);
            return new Vector((float) fromDest.getX(), (float) fromDest.getY(), (float) toDest.getX(), (float) toDest.getY());
        }

        public Vector parallel(float w) {
            float len = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            float xd = (y1 - y2) * w / len;
            float yd = (x2 - x1) * w / len;
            float x3 = x1 + xd;
            float y3 = y1 + yd;
            float x4 = x2 + xd;
            float y4 = y2 + yd;
            return new Vector(x3, y3, x4, y4);
        }

    }

    @Override
    public Shape createStrokedShape(Shape p) {
        if (stroke.getLineJoin() != BasicStroke.JOIN_MITER) {
            return stroke.createStrokedShape(p);
        }
        PathIterator pi = p.getPathIterator(new AffineTransform());
        int type;
        float points[] = new float[6];

        Area area = new Area(stroke.createStrokedShape(p));
        AffineTransform t = new AffineTransform();

        List<Vector> vectors = new ArrayList<>();
        List<Boolean> offPath = new ArrayList<>();
        float x = 0;
        float y = 0;
        while (!pi.isDone()) {
            type = pi.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    vectors.add(new Vector(x, y, points[0], points[1]));
                    offPath.add(true);
                    x = points[0];
                    y = points[1];
                    break;
                case PathIterator.SEG_LINETO:
                    vectors.add(new Vector(x, y, points[0], points[1]));
                    offPath.add(false);
                    x = points[0];
                    y = points[1];
                    break;
                case PathIterator.SEG_CUBICTO:
                    vectors.add(new Vector(x, y, points[0], points[1]));
                    offPath.add(true);
                    vectors.add(new Vector(points[0], points[1], points[2], points[3]));
                    offPath.add(true);
                    vectors.add(new Vector(points[2], points[3], points[4], points[5]));
                    offPath.add(false);
                    x = points[4];
                    y = points[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    vectors.add(new Vector(x, y, points[0], points[1]));
                    offPath.add(true);
                    vectors.add(new Vector(points[0], points[1], points[2], points[3]));
                    offPath.add(false);
                    x = points[2];
                    y = points[3];
                    break;
            }
            pi.next();
        }

        for (int i = 0; i < vectors.size() - 1; i++) {
            if (offPath.get(i)) {
                continue;
            }
            Vector u = vectors.get(i).transform(t);
            Vector v = vectors.get(i + 1).transform(t);

            float parallelSign = 1;
            float dx = u.x2 - u.x1;
            float dy = u.y2 - u.y1;

            float dx2 = v.x2 - v.x1;
            float dy2 = v.y2 - v.y1;

            if (dx <= 0 && dy <= 0 && dx2 >= 0 && dy2 <= 0) {
                parallelSign = -1;
            } else if (dx <= 0 && dy >= 0 && dx2 <= 0 && dy2 <= 0) {
                parallelSign = -1;
            } else if (dx >= 0 && dy <= 0 && dx2 >= 0 && dy2 >= 0) {
                parallelSign = -1;
            } else if (dx >= 0 && dy >= 0 && dx2 <= 0 && dy2 >= 0) {
                parallelSign = -1;
            }

            //https://math.stackexchange.com/questions/2593627/i-have-a-line-i-want-to-move-the-line-a-certain-distance-away-parallelly
            Vector perp1 = u.parallel((parallelSign * ((((float) stroke.getLineWidth()) / 2))));
            Vector perp2 = v.parallel((parallelSign * ((((float) stroke.getLineWidth()) / 2))));
            float intersectX;
            float intersectY;
            if (perp1.x1 == perp1.x2 && perp2.y1 == perp2.y2) {
                intersectX = perp1.x1;
                intersectY = perp2.y1;
            } else if (perp1.y1 == perp1.y2 && perp2.x1 == perp2.x2) {
                intersectX = perp2.x1;
                intersectY = perp1.y1;
            } else if (perp1.x1 == perp1.x2) {
                intersectX = perp1.x1;
                float line_b = (perp2.y2 - perp2.y1) / (perp2.x2 - perp2.x1);
                float line_d = perp2.y1 - line_b * perp2.x1;
                intersectY = line_b * intersectX + line_d;
            } else if (perp2.x1 == perp2.x2) {
                intersectX = perp2.x1;
                float line_a = (perp1.y2 - perp1.y1) / (perp1.x2 - perp1.x1);
                float line_c = perp1.y1 - line_a * perp1.x1;
                intersectY = line_a * intersectX + line_c;
            } else if (perp1.y1 == perp1.y2) {
                intersectY = perp1.y1;
                float line_b = (perp2.y2 - perp2.y1) / (perp2.x2 - perp2.x1);
                float line_d = perp2.y1 - line_b * perp2.x1;
                intersectX = (intersectY - line_d) / line_b;
            } else if (perp2.y1 == perp2.y2) {
                intersectY = perp2.y1;
                float line_a = (perp1.y2 - perp1.y1) / (perp1.x2 - perp1.x1);
                float line_c = perp1.y1 - line_a * perp1.x1;
                intersectX = intersectY - line_c / line_a;
            } else {
                float line_a = (perp1.y2 - perp1.y1) / (perp1.x2 - perp1.x1);
                float line_c = perp1.y1 - line_a * perp1.x1;
                float line_b = (perp2.y2 - perp2.y1) / (perp2.x2 - perp2.x1);
                float line_d = perp2.y1 - line_b * perp2.x1;

                intersectX = (line_d - line_c) / (line_a - line_b);
                intersectY = line_a * intersectX + line_c;
            }

            float ss = (float) Math.sqrt((intersectX - u.x2) * (intersectX - u.x2) + (intersectY - u.y2) * (intersectY - u.y2));
            float miter = stroke.getMiterLimit() * stroke.getLineWidth() / 2;
            float afterMiter = ss - miter;
            if (afterMiter > 0) {
                float ndx1a = intersectX - perp2.x1;
                float ndy1a = intersectY - perp2.y1;
                float ndxa = ndx1a * afterMiter / ss;
                float ndya = ndy1a * afterMiter / ss;

                float intmitxa = intersectX - ndxa;
                float intmitya = intersectY - ndya;

                float ndx1b = intersectX - perp1.x2;
                float ndy1b = intersectY - perp1.y2;
                float ndxb = ndx1b * afterMiter / ss;
                float ndyb = ndy1b * afterMiter / ss;

                float intmitxb = intersectX - ndxb;
                float intmityb = intersectY - ndyb;

                Path2D fp = new Path2D.Float(Path2D.WIND_EVEN_ODD);
                fp.moveTo(perp2.x1, perp2.y1);
                fp.lineTo(intmitxa, intmitya);
                fp.lineTo(intmitxb, intmityb);
                fp.lineTo(perp1.x2, perp1.y2);
                fp.closePath();
                area.add(new Area(fp));
            }

        }
        return area;
    }
}
