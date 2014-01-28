/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.shaperecords;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.BitmapExporter;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public abstract class SHAPERECORD implements Cloneable, NeedsCharacters, Serializable {

    public abstract void calculateBits();

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        return ret;
    }

    public abstract int changeX(int x);

    public abstract int changeY(int y);

    public abstract void flip();

    public static RECT getBounds(List<SHAPERECORD> records) {
        int x = 0;
        int y = 0;
        int max_x = 0;
        int max_y = 0;
        int min_x = Integer.MAX_VALUE;
        int min_y = Integer.MAX_VALUE;
        boolean started = false;
        for (SHAPERECORD r : records) {
            x = r.changeX(x);
            y = r.changeY(y);
            if (x > max_x) {
                max_x = x;
            }
            if (y > max_y) {
                max_y = y;
            }
            if (r.isMove()) {
                started = true;
            }
            if (started) {
                if (y < min_y) {
                    min_y = y;
                }
                if (x < min_x) {
                    min_x = x;
                }
            }
            if (r instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord curverEdge = (CurvedEdgeRecord) r;
                int x2 = x + curverEdge.controlDeltaX;
                int y2 = y + curverEdge.controlDeltaY;
                if (x2 > max_x) {
                    max_x = x2;
                }
                if (y2 > max_y) {
                    max_y = y2;
                }
                if (started) {
                    if (y2 < min_y) {
                        min_y = y2;
                    }
                    if (x2 < min_x) {
                        min_x = x2;
                    }
                }
            }
        }
        return new RECT(min_x, max_x, min_y, max_y);
    }

    public static CurvedEdgeRecord straightToCurve(StraightEdgeRecord ser) {
        CurvedEdgeRecord ret = new CurvedEdgeRecord();
        ret.controlDeltaX = ser.deltaX / 2;
        ret.controlDeltaY = ser.deltaY / 2;
        ret.anchorDeltaX = ser.deltaX - ret.controlDeltaX;
        ret.anchorDeltaY = ser.deltaY - ret.controlDeltaY;
        return ret;
    }

    public static SerializableImage shapeListToImage(SWF swf, List<SHAPE> shapes, int prevWidth, int prevHeight, Color color) {
        SerializableImage ret = new SerializableImage(prevWidth, prevHeight, SerializableImage.TYPE_INT_ARGB);
        Graphics g = ret.getGraphics();

        int maxw = 0;
        int maxh = 0;
        for (SHAPE s : shapes) {
            RECT r = SHAPERECORD.getBounds(s.shapeRecords);
            if (r.getWidth() > maxw) {
                maxw = r.getWidth();
            }
            if (r.getHeight() > maxh) {
                maxh = r.getHeight();
            }
        }
        maxw /= 20;
        maxh /= 20;

        int cols = (int) Math.ceil(Math.sqrt(shapes.size()));
        int pos = 0;
        int w2 = prevWidth / cols;
        int h2 = prevHeight / cols;

        int mh = maxh * w2 / maxw;
        int mw;
        if (mh > h2) {
            mw = maxw * h2 / maxh;
            mh = h2;
        } else {
            mw = w2;
        }

        float ratio = (float) mw / (float) maxw;

        loopy:
        for (int y = 0; y < cols; y++) {
            for (int x = 0; x < cols; x++) {
                if (pos >= shapes.size()) {
                    break loopy;
                }

                // shapeNum: 1
                SerializableImage img = BitmapExporter.export(swf, shapes.get(pos), color, false);

                int w1 = img.getWidth();
                int h1 = img.getHeight();

                int w = Math.round(ratio * w1);
                int h = Math.round(ratio * h1);
                int px = x * w2 + w2 / 2 - w / 2;
                int py = y * h2 + w2 - h;
                g.drawImage(img.getBufferedImage(), px, py, px + w, py + h, 0, 0, w1, h1, null);
                pos++;
            }
        }

        return ret;
    }

    public abstract boolean isMove();

    public static List<SHAPE> systemFontCharactersToSHAPES(String fontName, int fontStyle, int fontSize, String characters) {
        List<SHAPE> ret = new ArrayList<>();
        for (int i = 0; i < characters.length(); i++) {
            ret.add(systemFontCharacterToSHAPE(fontName, fontStyle, fontSize, characters.charAt(i)));
        }
        return ret;
    }

    public static SHAPE systemFontCharacterToSHAPE(final String fontName, final int fontStyle, int fontSize, char character) {
        int multiplier = 1;
        if (fontSize > 1024) {
            multiplier = fontSize / 1024;
            fontSize = 1024;
        }
        List<SHAPERECORD> retList = new ArrayList<>();
        Font f = new Font(FontTag.getFontNameWithFallback(fontName), fontStyle, fontSize);
        GlyphVector v = f.createGlyphVector((new JPanel()).getFontMetrics(f).getFontRenderContext(), "" + character);
        Shape shp = v.getOutline();
        double[] points = new double[6];
        int lastX = 0;
        int lastY = 0;
        int startX = 0;
        int startY = 0;
        for (PathIterator it = shp.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    StyleChangeRecord scr = new StyleChangeRecord();
                    scr.stateMoveTo = true;
                    scr.moveDeltaX = multiplier * (int) Math.round(points[0]);
                    scr.moveDeltaY = multiplier * (int) Math.round(points[1]);
                    scr.moveBits = SWFOutputStream.getNeededBitsS(scr.moveDeltaX, scr.moveDeltaY);
                    retList.add(scr);
                    lastX = (int) Math.round(points[0]);
                    lastY = (int) Math.round(points[1]);
                    startX = lastX;
                    startY = lastY;
                    break;
                case PathIterator.SEG_LINETO:
                    StraightEdgeRecord ser = new StraightEdgeRecord();
                    ser.deltaX = multiplier * (((int) Math.round(points[0])) - lastX);
                    ser.deltaY = multiplier * (((int) Math.round(points[1])) - lastY);

                    ser.generalLineFlag = ser.deltaX != 0 && ser.deltaY != 0;
                    if (ser.deltaX == 0) {
                        ser.vertLineFlag = true;
                    }
                    ser.numBits = SWFOutputStream.getNeededBitsS(ser.deltaX, ser.deltaY) - 2;
                    if (ser.numBits < 0) {
                        ser.numBits = 0;
                    }
                    retList.add(ser);
                    lastX = (int) Math.round(points[0]);
                    lastY = (int) Math.round(points[1]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    double[] cubicCoords = new double[]{
                        lastX, lastY,
                        Math.round(points[0]), Math.round(points[1]),
                        Math.round(points[2]), Math.round(points[3]),
                        Math.round(points[4]), Math.round(points[5])
                    };
                    double[][] quadCoords = approximateCubic(cubicCoords);
                    for (int i = 0; i < quadCoords.length; i++) {
                        CurvedEdgeRecord cer = new CurvedEdgeRecord();
                        cer.controlDeltaX = multiplier * (((int) Math.round(quadCoords[i][0])) - lastX);
                        cer.controlDeltaY = multiplier * (((int) Math.round(quadCoords[i][1])) - lastY);
                        cer.anchorDeltaX = multiplier * (((int) Math.round(quadCoords[i][2])) - ((int) Math.round(quadCoords[i][0])));
                        cer.anchorDeltaY = multiplier * (((int) Math.round(quadCoords[i][3])) - ((int) Math.round(quadCoords[i][1])));
                        cer.numBits = SWFOutputStream.getNeededBitsS(cer.controlDeltaX, cer.controlDeltaY, cer.anchorDeltaX, cer.anchorDeltaY) - 2;
                        if (cer.numBits < 0) {
                            cer.numBits = 0;
                        }
                        lastX = (int) Math.round(quadCoords[i][2]);
                        lastY = (int) Math.round(quadCoords[i][3]);
                        retList.add(cer);
                    }
                    break;
                case PathIterator.SEG_QUADTO:
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    cer.controlDeltaX = multiplier * (((int) Math.round(points[0])) - lastX);
                    cer.controlDeltaY = multiplier * (((int) Math.round(points[1])) - lastY);
                    cer.anchorDeltaX = multiplier * (((int) Math.round(points[2])) - (int) Math.round(points[0]));
                    cer.anchorDeltaY = multiplier * (((int) Math.round(points[3])) - (int) Math.round(points[1]));
                    cer.numBits = SWFOutputStream.getNeededBitsS(cer.controlDeltaX, cer.controlDeltaY, cer.anchorDeltaX, cer.anchorDeltaY) - 2;
                    if (cer.numBits < 0) {
                        cer.numBits = 0;
                    }
                    retList.add(cer);
                    lastX = (int) Math.round(points[2]);
                    lastY = (int) Math.round(points[3]);
                    break;
                case PathIterator.SEG_CLOSE: //Closing line back to last SEG_MOVETO
                    if ((startX == lastX) && (startY == lastY)) {
                        break;
                    }
                    StraightEdgeRecord closeSer = new StraightEdgeRecord();
                    closeSer.generalLineFlag = true;
                    closeSer.deltaX = multiplier * ((int) Math.round((startX - lastX)));
                    closeSer.deltaY = multiplier * ((int) Math.round((startY - lastY)));
                    closeSer.numBits = SWFOutputStream.getNeededBitsS(closeSer.deltaX, closeSer.deltaY) - 2;
                    if (closeSer.numBits < 0) {
                        closeSer.numBits = 0;
                    }
                    retList.add(closeSer);
                    lastX = startX;
                    lastY = startY;
                    break;
            }
        }
        SHAPE shape = new SHAPE();
        StyleChangeRecord init;
        if (!retList.isEmpty() && retList.get(0) instanceof StyleChangeRecord) {
            init = (StyleChangeRecord) retList.get(0);
        } else {
            init = new StyleChangeRecord();
            retList.add(0, init);
        }

        retList.add(new EndShapeRecord());
        init.stateFillStyle0 = true;
        init.fillStyle0 = 1;
        shape.shapeRecords = retList;
        shape.numFillBits = 1;
        shape.numLineBits = 0;
        return shape;
    }

    // Taken from org.apache.fop.afp.util
    private static double[][] approximateCubic(double[] cubicControlPointCoords) {
        if (cubicControlPointCoords.length < 8) {
            throw new IllegalArgumentException("Must have at least 8 coordinates");
        }

        //extract point objects from source array
        Point2D p0 = new Point2D.Double(cubicControlPointCoords[0], cubicControlPointCoords[1]);
        Point2D p1 = new Point2D.Double(cubicControlPointCoords[2], cubicControlPointCoords[3]);
        Point2D p2 = new Point2D.Double(cubicControlPointCoords[4], cubicControlPointCoords[5]);
        Point2D p3 = new Point2D.Double(cubicControlPointCoords[6], cubicControlPointCoords[7]);

        //calculates the useful base points
        Point2D pa = getPointOnSegment(p0, p1, 3.0 / 4.0);
        Point2D pb = getPointOnSegment(p3, p2, 3.0 / 4.0);

        //get 1/16 of the [P3, P0] segment
        double dx = (p3.getX() - p0.getX()) / 16.0;
        double dy = (p3.getY() - p0.getY()) / 16.0;

        //calculates control point 1
        Point2D pc1 = getPointOnSegment(p0, p1, 3.0 / 8.0);

        //calculates control point 2
        Point2D pc2 = getPointOnSegment(pa, pb, 3.0 / 8.0);
        pc2 = movePoint(pc2, -dx, -dy);

        //calculates control point 3
        Point2D pc3 = getPointOnSegment(pb, pa, 3.0 / 8.0);
        pc3 = movePoint(pc3, dx, dy);

        //calculates control point 4
        Point2D pc4 = getPointOnSegment(p3, p2, 3.0 / 8.0);

        //calculates the 3 anchor points
        Point2D pa1 = getMidPoint(pc1, pc2);
        Point2D pa2 = getMidPoint(pa, pb);
        Point2D pa3 = getMidPoint(pc3, pc4);

        //return the points for the four quadratic curves
        return new double[][]{
            {pc1.getX(), pc1.getY(), pa1.getX(), pa1.getY()},
            {pc2.getX(), pc2.getY(), pa2.getX(), pa2.getY()},
            {pc3.getX(), pc3.getY(), pa3.getX(), pa3.getY()},
            {pc4.getX(), pc4.getY(), p3.getX(), p3.getY()}};
    }

    private static Point2D.Double movePoint(Point2D point, double dx, double dy) {
        return new Point2D.Double(point.getX() + dx, point.getY() + dy);
    }

    private static Point2D getMidPoint(Point2D p0, Point2D p1) {
        return getPointOnSegment(p0, p1, 0.5);
    }

    private static Point2D getPointOnSegment(Point2D p0, Point2D p1, double ratio) {
        double x = p0.getX() + ((p1.getX() - p0.getX()) * ratio);
        double y = p0.getY() + ((p1.getY() - p0.getY()) * ratio);
        return new Point2D.Double(x, y);
    }

    public static SHAPE resizeSHAPE(SHAPE shp, int multiplier) {
        SHAPE ret = new SHAPE();
        ret.numFillBits = shp.numFillBits;
        ret.numLineBits = shp.numLineBits;
        List<SHAPERECORD> recs = new ArrayList<>();
        for (SHAPERECORD r : shp.shapeRecords) {
            SHAPERECORD c = (SHAPERECORD) Helper.deepCopy(r);
            if (c instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) c;
                scr.moveDeltaX = (multiplier * scr.moveDeltaX);
                scr.moveDeltaY = (multiplier * scr.moveDeltaY);
                scr.calculateBits();
            }
            if (c instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) c;
                cer.controlDeltaX = (multiplier * cer.controlDeltaX);
                cer.controlDeltaY = (multiplier * cer.controlDeltaY);
                cer.anchorDeltaX = (multiplier * cer.anchorDeltaX);
                cer.anchorDeltaY = (multiplier * cer.anchorDeltaY);
                cer.calculateBits();
            }
            if (c instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) c;
                ser.deltaX = (multiplier * ser.deltaX);
                ser.deltaY = (multiplier * ser.deltaY);
                ser.calculateBits();
            }
            recs.add(c);
        }
        ret.shapeRecords = recs;
        return ret;
    }
}
