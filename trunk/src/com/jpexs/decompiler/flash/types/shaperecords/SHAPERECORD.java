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

import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class SHAPERECORD implements Cloneable, NeedsCharacters {

    public static float twipToPixel(int twip) {
        return ((float) twip) / 20.0f;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<Integer>();
        return ret;
    }

    private static class Path {

        public LINESTYLE lineStyle;
        public LINESTYLE2 lineStyle2;
        public boolean useLineStyle2;
        public FILLSTYLE fillStyle0;
        public FILLSTYLE fillStyle1;
        public List<SHAPERECORD> edges = new ArrayList<SHAPERECORD>();

        public void draw(int startX, int startY, Graphics2D g, int shapeNum) {
            AffineTransform oldAf = g.getTransform();
            AffineTransform trans20 = AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0);
            //g.setTransform(trans20);
            boolean ok = false;
            if (shapeNum == 4) {
                if (lineStyle2 == null) {
                    ok = false;
                } else if (!lineStyle2.hasFillFlag) {
                    g.setPaint(new Color(lineStyle2.color.red, lineStyle2.color.green, lineStyle2.color.blue, lineStyle2.color.alpha));
                    int capStyle = 0;
                    switch (lineStyle2.startCapStyle) {
                        case LINESTYLE2.NO_CAP:
                            capStyle = BasicStroke.CAP_BUTT;
                            break;
                        case LINESTYLE2.ROUND_CAP:
                            capStyle = BasicStroke.CAP_ROUND;
                            break;
                        case LINESTYLE2.SQUARE_CAP:
                            capStyle = BasicStroke.CAP_SQUARE;
                            break;
                    }
                    int joinStyle = 0;
                    switch (lineStyle2.joinStyle) {
                        case LINESTYLE2.BEVEL_JOIN:
                            joinStyle = BasicStroke.JOIN_BEVEL;
                            break;
                        case LINESTYLE2.MITER_JOIN:
                            joinStyle = BasicStroke.JOIN_MITER;
                            break;
                        case LINESTYLE2.ROUND_JOIN:
                            joinStyle = BasicStroke.JOIN_ROUND;
                            break;
                    }
                    if (joinStyle == BasicStroke.JOIN_MITER) {
                        g.setStroke(new BasicStroke(lineStyle2.width / 20, capStyle, joinStyle, lineStyle2.miterLimitFactor));
                    } else {
                        g.setStroke(new BasicStroke(lineStyle2.width / 20, capStyle, joinStyle));
                    }
                    ok = true;
                }
            } else {
                if (lineStyle == null) {
                    ok = false;
                } else {
                    g.setStroke(new BasicStroke(lineStyle.width / 20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    ok = true;
                }
            }
            if (ok) {
                g.draw(trans20.createTransformedShape(toGeneralPath(startX, startY)));
            }
            g.setTransform(oldAf);
            g.setClip(null);
        }

        private AffineTransform matrixToTransform(MATRIX mat) {
            return new AffineTransform(mat.getScaleXFloat(), mat.getRotateSkew0Float(),
                    mat.getRotateSkew1Float(), mat.getScaleYFloat(),
                    mat.translateX, mat.translateY);
            //mat.translateX,mat.translateY);
            /*AffineTransform move=AffineTransform.getTranslateInstance(mat.translateX, mat.translateY);
             AffineTransform rotate =AffineTransform.getRotateInstance(mat.getRotateSkew0Float(), mat.getRotateSkew1Float());
             AffineTransform scale=AffineTransform.getScaleInstance(mat.getScaleXFloat(), mat.getScaleYFloat());
             AffineTransform af=scale;
             AffineTransform scale20=AffineTransform.getScaleInstance(1/20.0, 1/20.0);
            
             //af.concatenate(move);
             //af.concatenate(scale20);
             af.concatenate(rotate);
             af.concatenate(scale);           
             return af;*/
        }

        public void fill(List<Tag> tags, int startX, int startY, Graphics2D g, int shapeNum) {
            AffineTransform oldAf = g.getTransform();
            AffineTransform trans20 = AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0);
            g.setTransform(trans20);
            int maxRepeat = 200;
            boolean ok = false;
            switch (fillStyle0.fillStyleType) {
                case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
                case FILLSTYLE.REPEATING_BITMAP:
                case FILLSTYLE.CLIPPED_BITMAP:
                case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                    ImageTag image = null;
                    for (Tag t : tags) {
                        if (t instanceof ImageTag) {
                            ImageTag i = (ImageTag) t;
                            if (i.getCharacterID() == fillStyle0.bitmapId) {
                                image = i;
                                break;
                            }
                        }
                    }
                    if (image != null) {
                        g.setClip(toGeneralPath(startX, startY));
                        AffineTransform btrans = matrixToTransform(fillStyle0.bitmapMatrix);
                        btrans.preConcatenate(AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0));
                        btrans.preConcatenate(AffineTransform.getTranslateInstance(startX / 20, startY / 20));
                        g.setTransform(btrans);
                        BufferedImage img = image.getImage(tags);
                        g.setPaint(new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight())));
                        g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                        g.setTransform(oldAf);
                        g.setClip(null);
                    }
                    break;
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                    float focRadFractions[] = new float[fillStyle0.focalGradient.gradientRecords.length];
                    Color focRadColors[] = new Color[fillStyle0.focalGradient.gradientRecords.length];
                    for (int i = 0; i < fillStyle0.focalGradient.gradientRecords.length; i++) {
                        focRadFractions[i] = fillStyle0.focalGradient.gradientRecords[i].getRatioFloat();
                        if (shapeNum >= 3) {
                            focRadColors[i] = new Color(fillStyle0.focalGradient.gradientRecords[i].colorA.red, fillStyle0.focalGradient.gradientRecords[i].colorA.green, fillStyle0.focalGradient.gradientRecords[i].colorA.blue, fillStyle0.focalGradient.gradientRecords[i].colorA.alpha);
                        } else {
                            focRadColors[i] = new Color(fillStyle0.focalGradient.gradientRecords[i].color.red, fillStyle0.focalGradient.gradientRecords[i].color.green, fillStyle0.focalGradient.gradientRecords[i].color.blue);
                        }
                    }
                    RGB focEndColor = fillStyle0.focalGradient.gradientRecords[fillStyle0.focalGradient.gradientRecords.length - 1].color;
                    RGBA focEndColorA = fillStyle0.focalGradient.gradientRecords[fillStyle0.focalGradient.gradientRecords.length - 1].colorA;
                    if (shapeNum >= 3) {
                        g.setPaint(new Color(focEndColorA.red, focEndColorA.green, focEndColorA.blue, focEndColorA.alpha));
                    } else {
                        g.setPaint(new Color(focEndColor.red, focEndColor.green, focEndColor.blue));
                    }
                    GeneralPath focPath = toGeneralPath(startX, startY);
                    g.fill(focPath);
                    g.setClip(focPath);
                    AffineTransform focTrans = matrixToTransform(fillStyle0.gradientMatrix);
                    focTrans.preConcatenate(AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0));
                    focTrans.preConcatenate(AffineTransform.getTranslateInstance(startX / 20, startY / 20));
                    g.setTransform(focTrans);
                    CycleMethod cm = CycleMethod.NO_CYCLE;
                    if (fillStyle0.focalGradient.spreadMode == GRADIENT.SPREAD_PAD_MODE) {
                        cm = CycleMethod.NO_CYCLE;
                    } else if (fillStyle0.focalGradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE) {
                        cm = CycleMethod.REFLECT;
                    } else if (fillStyle0.focalGradient.spreadMode == GRADIENT.SPREAD_REPEAT_MODE) {
                        cm = CycleMethod.REPEAT;
                    }


                    g.setPaint(new RadialGradientPaint(new Point(0, 0), 16384, new Point((int) (fillStyle0.focalGradient.focalPoint * 16384), 0), focRadFractions, focRadColors, cm));
                    g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    g.setTransform(oldAf);
                    g.setClip(null);
                    return;
                case FILLSTYLE.RADIAL_GRADIENT:
                    float radFractions[] = new float[fillStyle0.gradient.gradientRecords.length];
                    Color radColors[] = new Color[fillStyle0.gradient.gradientRecords.length];
                    for (int i = 0; i < fillStyle0.gradient.gradientRecords.length; i++) {
                        radFractions[i] = fillStyle0.gradient.gradientRecords[i].getRatioFloat();
                        if (shapeNum >= 3) {
                            radColors[i] = new Color(fillStyle0.gradient.gradientRecords[i].colorA.red, fillStyle0.gradient.gradientRecords[i].colorA.green, fillStyle0.gradient.gradientRecords[i].colorA.blue, fillStyle0.gradient.gradientRecords[i].colorA.alpha);
                        } else {
                            radColors[i] = new Color(fillStyle0.gradient.gradientRecords[i].color.red, fillStyle0.gradient.gradientRecords[i].color.green, fillStyle0.gradient.gradientRecords[i].color.blue);
                        }
                    }
                    RGB endColor = fillStyle0.gradient.gradientRecords[fillStyle0.gradient.gradientRecords.length - 1].color;
                    RGBA endColorA = fillStyle0.gradient.gradientRecords[fillStyle0.gradient.gradientRecords.length - 1].colorA;
                    if (shapeNum >= 3) {
                        g.setPaint(new Color(endColorA.red, endColorA.green, endColorA.blue, endColorA.alpha));
                    } else {
                        g.setPaint(new Color(endColor.red, endColor.green, endColor.blue));
                    }
                    GeneralPath path = toGeneralPath(startX, startY);
                    g.fill(path);
                    g.setClip(path);
                    AffineTransform trans = matrixToTransform(fillStyle0.gradientMatrix);
                    trans.preConcatenate(AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0));
                    trans.preConcatenate(AffineTransform.getTranslateInstance(startX / 20, startY / 20));
                    g.setTransform(trans);

                    CycleMethod cmRad = CycleMethod.NO_CYCLE;
                    if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_PAD_MODE) {
                        cmRad = CycleMethod.NO_CYCLE;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE) {
                        cmRad = CycleMethod.REFLECT;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REPEAT_MODE) {
                        cmRad = CycleMethod.REPEAT;
                    }

                    g.setPaint(new RadialGradientPaint(new Point(0, 0), 16384, radFractions, radColors, cmRad));
                    g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    g.setTransform(oldAf);
                    g.setClip(null);
                    return;
                case FILLSTYLE.LINEAR_GRADIENT:
                    float fractions[] = new float[fillStyle0.gradient.gradientRecords.length];
                    Color colors[] = new Color[fillStyle0.gradient.gradientRecords.length];
                    for (int i = 0; i < fillStyle0.gradient.gradientRecords.length; i++) {
                        fractions[i] = fillStyle0.gradient.gradientRecords[i].getRatioFloat();
                        if (shapeNum >= 3) {
                            colors[i] = new Color(fillStyle0.gradient.gradientRecords[i].colorA.red, fillStyle0.gradient.gradientRecords[i].colorA.green, fillStyle0.gradient.gradientRecords[i].colorA.blue, fillStyle0.gradient.gradientRecords[i].colorA.alpha);
                        } else {
                            colors[i] = new Color(fillStyle0.gradient.gradientRecords[i].color.red, fillStyle0.gradient.gradientRecords[i].color.green, fillStyle0.gradient.gradientRecords[i].color.blue);
                        }
                    }
                    GeneralPath pathLin = toGeneralPath(startX, startY);
                    g.fill(pathLin);
                    g.setClip(pathLin);
                    AffineTransform transLin = matrixToTransform(fillStyle0.gradientMatrix);
                    transLin.preConcatenate(AffineTransform.getScaleInstance(1 / 20.0, 1 / 20.0));
                    transLin.preConcatenate(AffineTransform.getTranslateInstance(startX / 20, startY / 20));
                    g.setTransform(transLin);

                    CycleMethod cmLin = CycleMethod.NO_CYCLE;
                    if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_PAD_MODE) {
                        cmLin = CycleMethod.NO_CYCLE;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE) {
                        cmLin = CycleMethod.REFLECT;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REPEAT_MODE) {
                        cmLin = CycleMethod.REPEAT;
                    }


                    g.setPaint(new LinearGradientPaint(new Point(-16384, 0), new Point(16384, 0), fractions, colors, cmLin));
                    g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    g.setTransform(oldAf);
                    g.setClip(null);
                    ok = true;
                    return;
                case FILLSTYLE.SOLID:
                    Color c = null;
                    if (shapeNum >= 3) {
                        c = new Color(fillStyle0.colorA.red, fillStyle0.colorA.green, fillStyle0.colorA.blue, fillStyle0.colorA.alpha);
                    } else {
                        c = new Color(fillStyle0.color.red, fillStyle0.color.green, fillStyle0.color.blue);
                    }
                    g.setPaint(c);
                    ok = true;
                    break;
            }
            if (ok) {
                GeneralPath path = toGeneralPath(startX, startY);
                g.fill(path);
            }
            g.setTransform(oldAf);
            g.setClip(null);
        }

        public void drawTo(List<Tag> tags, int startX, int startY, Graphics2D g, int shapeNum) {
            fill(tags, startX, startY, g, shapeNum);
            draw(startX, startY, g, shapeNum);
        }

        public GeneralPath toGeneralPath(int startX, int startY) {

            GeneralPath ret = new GeneralPath();
            int x = startX;
            int y = startY;
            for (SHAPERECORD rec : edges) {
                int nx = rec.changeX(x);
                int ny = rec.changeY(y);
                if (rec instanceof StyleChangeRecord) {
                    StyleChangeRecord scr = (StyleChangeRecord) rec;
                    if (scr.stateMoveTo) {
                        nx += startX;
                        ny += startY;
                        ret.moveTo(nx, ny);
                    }
                }
                if (rec instanceof StraightEdgeRecord) {
                    ret.lineTo(nx, ny);
                }
                if (rec instanceof CurvedEdgeRecord) {
                    CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                    ret.quadTo((x + cer.controlDeltaX), (y + cer.controlDeltaY), (x + cer.controlDeltaX + cer.anchorDeltaX), (y + cer.controlDeltaY + cer.anchorDeltaY));
                }
                x = nx;
                y = ny;
            }
            return ret;
        }

        public boolean sameStyle(Path p) {
            return fillStyle0 == p.fillStyle0 && ((useLineStyle2 && (p.lineStyle2 == lineStyle2)) || ((!useLineStyle2) && (p.lineStyle == lineStyle)));
        }

        public String toSVG(int shapeNum) {
            String ret = "";
            String params = "";
            String f = "";
            if (fillStyle0 != null) {
                if (fillStyle0.fillStyleType == FILLSTYLE.SOLID) {
                    f = " fill=\"" + ((shapeNum >= 3) ? fillStyle0.colorA.toHexRGB() : fillStyle0.color.toHexRGB()) + "\"";
                }
            }
            params += f;
            if ((!useLineStyle2) && lineStyle != null) {
                params += " stroke=\"" + ((shapeNum >= 3) ? lineStyle.colorA.toHexRGB() : lineStyle.color.toHexRGB()) + "\"";
            }
            if (useLineStyle2 && lineStyle2 != null) {
                params += " stroke-width=\"" + twipToPixel(lineStyle2.width) + "\"" + (lineStyle2.color != null ? " stroke=\"" + lineStyle2.color.toHexRGB() + "\"" : "");
            }
            String points = "";
            int x = 0;
            int y = 0;
            for (SHAPERECORD r : edges) {
                points += " " + r.toSWG(x, y);
                x = r.changeX(x);
                y = r.changeY(y);
            }
            ret += "<path" + params + " d=\"" + points + "\"/>";
            return ret;
        }
    }

    public abstract String toSWG(int oldX, int oldY);

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
        }
        return new RECT(min_x, max_x, min_y, max_y);
    }

    private static List<Path> getPaths(RECT bounds, int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, int numFillBits, int numLineBits, List<SHAPERECORD> records) {
        List<Path> paths = new ArrayList<Path>();
        Path path = new Path();
        int x = 0;
        int y = 0;
        int max_x = 0;
        int max_y = 0;
        int min_x = Integer.MAX_VALUE;
        int min_y = Integer.MAX_VALUE;
        boolean started = false;
        for (SHAPERECORD r : records) {
            if (r instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) r;
                paths.add(path);
                path = new Path();

                if (scr.stateNewStyles) {
                    fillStyles = scr.fillStyles;
                    lineStylesList = scr.lineStyles;
                    numFillBits = scr.numFillBits;
                    numLineBits = scr.numLineBits;
                }
                if (scr.stateFillStyle0) {
                    if (scr.fillStyle0 == 0) {
                        path.fillStyle0 = null;
                    } else {
                        path.fillStyle0 = fillStyles.fillStyles[scr.fillStyle0 - 1];
                    }
                }
                if (scr.stateFillStyle1) {
                    if (scr.fillStyle1 == 0) {
                        path.fillStyle1 = null;
                    } else {
                        path.fillStyle1 = fillStyles.fillStyles[scr.fillStyle1 - 1];
                    }
                }
                if (scr.stateLineStyle) {
                    if (shapeNum >= 4) {
                        path.useLineStyle2 = true;
                        if (scr.lineStyle == 0) {
                            path.lineStyle2 = null;
                        } else {
                            path.lineStyle2 = lineStylesList.lineStyles2[scr.lineStyle - 1];
                        }
                    } else {
                        path.useLineStyle2 = false;
                        if (scr.lineStyle == 0) {
                            path.lineStyle = null;
                        } else {
                            path.lineStyle = lineStylesList.lineStyles[scr.lineStyle - 1];
                        }
                    }
                }
            }
            path.edges.add(r);
            x = r.changeX(x);
            y = r.changeY(y);
            if (r.isMove()) {
                started = true;
            }
            if (x > max_x) {
                max_x = x;
            }
            if (y > max_y) {
                max_y = y;
            }
            if (started) {
                if (y < min_y) {
                    min_y = y;
                }
                if (x < min_x) {
                    min_x = x;
                }
            }
        }
        paths.add(path);
        List<Path> paths2 = new ArrayList<Path>();
        for (Path p : paths) {
            if (p.fillStyle0 != null) {
                paths2.add(p);
            }
            if (p.fillStyle1 != null) {
                Path f = new Path();
                f.edges = new ArrayList<SHAPERECORD>();
                f.fillStyle0 = p.fillStyle1;
                f.lineStyle = p.lineStyle;
                f.lineStyle2 = p.lineStyle2;
                f.useLineStyle2 = p.useLineStyle2;
                x = 0;
                y = 0;
                for (SHAPERECORD r : p.edges) {
                    x = r.changeX(x);
                    y = r.changeY(y);
                    SHAPERECORD r2 = null;
                    try {
                        r2 = (SHAPERECORD) r.clone();
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(SHAPERECORD.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    r2.flip();
                    f.edges.add(0, r2);
                }
                StyleChangeRecord scr = new StyleChangeRecord();
                scr.stateMoveTo = true;
                scr.moveDeltaX = x;
                scr.moveDeltaY = y;
                f.edges.add(0, scr);
                paths2.add(f);
            }
        }
        List<Path> paths3 = new ArrayList<Path>();
        for (Path p1 : paths2) {
            boolean found = false;
            for (Path p2 : paths3) {
                if (p1 == p2) {
                    continue;
                }
                if (p1.sameStyle(p2)) {
                    p2.edges.addAll(p1.edges);
                    found = true;
                    break;
                }
            }
            if (!found) {
                paths3.add(p1);
            }
        }
        bounds.Xmax = max_x;
        bounds.Ymax = max_y;
        bounds.Xmin = min_x;
        bounds.Ymin = min_y;
        return paths3;
    }

    /**
     * Convert shape to SVG
     *
     * @param shapeNum
     * @param fillStyles
     * @param lineStylesList
     * @param numFillBits
     * @param numLineBits
     * @param records
     * @return Shape converted to SVG
     */
    public static String shapeToSVG(int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, int numFillBits, int numLineBits, List<SHAPERECORD> records) {
        String ret = "";
        RECT bounds = new RECT();
        List<Path> paths = getPaths(bounds, shapeNum, fillStyles, lineStylesList, numFillBits, numLineBits, records);
        ret = "";
        for (Path p : paths) {
            ret += p.toSVG(shapeNum);
        }
        ret = "<?xml version='1.0' encoding='UTF-8' ?> \n"
                + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
                + "<svg width=\"" + (int) Math.ceil(twipToPixel(bounds.Xmax)) + "\"\n"
                + "     height=\"" + (int) Math.ceil(twipToPixel(bounds.Ymax)) + "\"\n"
                + "     viewBox=\"0 0 " + (int) Math.ceil(twipToPixel(bounds.Xmax)) + " " + (int) Math.ceil(twipToPixel(bounds.Ymax) + 50) + "\"\n"
                + "     xmlns=\"http://www.w3.org/2000/svg\"\n"
                + "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + ret + ""
                + "</svg>";
        return ret;
    }

    public static BufferedImage shapeToImage(List<Tag> tags, int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, int numFillBits, int numLineBits, List<SHAPERECORD> records) {
        RECT rect = new RECT();
        List<Path> paths = getPaths(rect, shapeNum, fillStyles, lineStylesList, numFillBits, numLineBits, records);
        BufferedImage ret = new BufferedImage(rect.getWidth() / 20 + 2, rect.getHeight() / 20 + 2, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D) ret.getGraphics();
        for (Path p : paths) {
            p.drawTo(tags, -rect.Xmin, -rect.Ymin, g, shapeNum);
        }
        return ret;
    }

    public abstract boolean isMove();
}
