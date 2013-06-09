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
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class SHAPERECORD implements Cloneable, NeedsCharacters {

    private static final float DESCALE = 20; //20

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> ret = new HashSet<>();
        return ret;
    }

    private static String edgesToStr(List<SHAPERECORD> records) {
        int x = 0;
        int y = 0;
        String ret = "";
        for (SHAPERECORD rec : records) {
            x = rec.changeX(x);
            y = rec.changeY(y);
            ret += ("[" + x + "," + y + "],");
        }
        return ret;

    }

    private static class Path {

        public boolean used = false;
        public LINESTYLE lineStyle;
        public LINESTYLE2 lineStyle2;
        public boolean useLineStyle2;
        public FILLSTYLE fillStyle0;
        public FILLSTYLE fillStyle1;
        public List<SHAPERECORD> edges = new ArrayList<>();
        public Point start;
        public Point end;

        public void draw(int startX, int startY, Graphics2D g, int shapeNum) {
            AffineTransform oldAf = g.getTransform();
            AffineTransform trans20 = AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE);
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
                        g.setStroke(new BasicStroke(lineStyle2.width / DESCALE, capStyle, joinStyle, lineStyle2.miterLimitFactor));
                    } else {
                        g.setStroke(new BasicStroke(lineStyle2.width / DESCALE, capStyle, joinStyle));
                    }
                    ok = true;
                }
            } else {
                if (lineStyle == null) {
                    ok = false;
                } else {
                    g.setStroke(new BasicStroke(lineStyle.width / DESCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    ok = true;
                }
            }
            if (ok) {
                g.draw(trans20.createTransformedShape(toGeneralPath(startX, startY)));
            }
            g.setTransform(oldAf);
            g.setClip(null);
        }

        public void fill(List<Tag> tags, int startX, int startY, Graphics2D g, int shapeNum) {
            AffineTransform oldAf = g.getTransform();
            AffineTransform trans20 = AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE);
            g.setTransform(trans20);
            int maxRepeat = 10; //TODO:better handle gradient repeating
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
                        AffineTransform btrans = SWF.matrixToTransform(fillStyle0.bitmapMatrix);
                        btrans.preConcatenate(AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE));
                        btrans.preConcatenate(AffineTransform.getTranslateInstance(startX / DESCALE, startY / DESCALE));
                        g.setTransform(btrans);
                        BufferedImage img = image.getImage(tags);
                        g.setPaint(new TexturePaint(img, new Rectangle(img.getWidth(), img.getHeight())));
                        g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                        g.setTransform(oldAf);
                        g.setClip(null);
                    }
                    break;
                case FILLSTYLE.FOCAL_RADIAL_GRADIENT:

                    List<Color> colorsFocRad = new ArrayList<>();
                    List<Float> ratiosFocRad = new ArrayList<>();
                    for (int i = 0; i < fillStyle0.gradient.gradientRecords.length; i++) {
                        if ((i > 0) && (fillStyle0.gradient.gradientRecords[i - 1].ratio == fillStyle0.gradient.gradientRecords[i].ratio)) {
                            continue;
                        }
                        ratiosFocRad.add(fillStyle0.gradient.gradientRecords[i].getRatioFloat());
                        if (shapeNum >= 3) {
                            colorsFocRad.add(new Color(fillStyle0.gradient.gradientRecords[i].colorA.red, fillStyle0.gradient.gradientRecords[i].colorA.green, fillStyle0.gradient.gradientRecords[i].colorA.blue, fillStyle0.gradient.gradientRecords[i].colorA.alpha));
                        } else {
                            colorsFocRad.add(new Color(fillStyle0.gradient.gradientRecords[i].color.red, fillStyle0.gradient.gradientRecords[i].color.green, fillStyle0.gradient.gradientRecords[i].color.blue));
                        }
                    }


                    float focRadFractions[] = new float[ratiosFocRad.size()];
                    for (int i = 0; i < ratiosFocRad.size(); i++) {
                        focRadFractions[i] = ratiosFocRad.get(i);
                    }
                    Color focRadColors[] = colorsFocRad.toArray(new Color[colorsFocRad.size()]);

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
                    AffineTransform focTrans = SWF.matrixToTransform(fillStyle0.gradientMatrix);
                    focTrans.preConcatenate(AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE));
                    focTrans.preConcatenate(AffineTransform.getTranslateInstance(startX / DESCALE, startY / DESCALE));
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
                    List<Color> colorsRad = new ArrayList<>();
                    List<Float> ratiosRad = new ArrayList<>();
                    for (int i = 0; i < fillStyle0.gradient.gradientRecords.length; i++) {
                        if ((i > 0) && (fillStyle0.gradient.gradientRecords[i - 1].ratio == fillStyle0.gradient.gradientRecords[i].ratio)) {
                            continue;
                        }
                        ratiosRad.add(fillStyle0.gradient.gradientRecords[i].getRatioFloat());
                        if (shapeNum >= 3) {
                            colorsRad.add(new Color(fillStyle0.gradient.gradientRecords[i].colorA.red, fillStyle0.gradient.gradientRecords[i].colorA.green, fillStyle0.gradient.gradientRecords[i].colorA.blue, fillStyle0.gradient.gradientRecords[i].colorA.alpha));
                        } else {
                            colorsRad.add(new Color(fillStyle0.gradient.gradientRecords[i].color.red, fillStyle0.gradient.gradientRecords[i].color.green, fillStyle0.gradient.gradientRecords[i].color.blue));
                        }
                    }



                    float ratiosRadAr[] = new float[ratiosRad.size()];
                    for (int i = 0; i < ratiosRad.size(); i++) {
                        ratiosRadAr[i] = ratiosRad.get(i);
                    }
                    Color colorsRadArr[] = colorsRad.toArray(new Color[colorsRad.size()]);

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
                    AffineTransform trans = SWF.matrixToTransform(fillStyle0.gradientMatrix);
                    trans.preConcatenate(AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE));
                    trans.preConcatenate(AffineTransform.getTranslateInstance(startX / DESCALE, startY / DESCALE));
                    g.setTransform(trans);

                    CycleMethod cmRad = CycleMethod.NO_CYCLE;
                    if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_PAD_MODE) {
                        cmRad = CycleMethod.NO_CYCLE;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE) {
                        cmRad = CycleMethod.REFLECT;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REPEAT_MODE) {
                        cmRad = CycleMethod.REPEAT;
                    }

                    g.setPaint(new RadialGradientPaint(new Point(0, 0), 16384, ratiosRadAr, colorsRadArr, cmRad));
                    g.fill(new Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    g.setTransform(oldAf);
                    g.setClip(null);
                    return;
                case FILLSTYLE.LINEAR_GRADIENT:
                    List<Color> colors = new ArrayList<>();
                    List<Float> ratios = new ArrayList<>();
                    for (int i = 0; i < fillStyle0.gradient.gradientRecords.length; i++) {
                        if ((i > 0) && (fillStyle0.gradient.gradientRecords[i - 1].ratio == fillStyle0.gradient.gradientRecords[i].ratio)) {
                            continue;
                        }
                        ratios.add(fillStyle0.gradient.gradientRecords[i].getRatioFloat());
                        if (shapeNum >= 3) {
                            colors.add(new Color(fillStyle0.gradient.gradientRecords[i].colorA.red, fillStyle0.gradient.gradientRecords[i].colorA.green, fillStyle0.gradient.gradientRecords[i].colorA.blue, fillStyle0.gradient.gradientRecords[i].colorA.alpha));
                        } else {
                            colors.add(new Color(fillStyle0.gradient.gradientRecords[i].color.red, fillStyle0.gradient.gradientRecords[i].color.green, fillStyle0.gradient.gradientRecords[i].color.blue));
                        }
                    }



                    float ratiosAr[] = new float[ratios.size()];
                    for (int i = 0; i < ratios.size(); i++) {
                        ratiosAr[i] = ratios.get(i);
                    }
                    Color colorsArr[] = colors.toArray(new Color[colors.size()]);

                    GeneralPath pathLin = toGeneralPath(startX, startY);
                    g.fill(pathLin);
                    g.setClip(pathLin);
                    AffineTransform transLin = SWF.matrixToTransform(fillStyle0.gradientMatrix);
                    transLin.preConcatenate(AffineTransform.getScaleInstance(1 / DESCALE, 1 / DESCALE));
                    transLin.preConcatenate(AffineTransform.getTranslateInstance(startX / DESCALE, startY / DESCALE));
                    g.setTransform(transLin);

                    CycleMethod cmLin = CycleMethod.NO_CYCLE;
                    if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_PAD_MODE) {
                        cmLin = CycleMethod.NO_CYCLE;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE) {
                        cmLin = CycleMethod.REFLECT;
                    } else if (fillStyle0.gradient.spreadMode == GRADIENT.SPREAD_REPEAT_MODE) {
                        cmLin = CycleMethod.REPEAT;
                    }


                    g.setPaint(new LinearGradientPaint(new Point(-16384, 0), new Point(16384, 0), ratiosAr, colorsArr, cmLin));
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
                path.closePath();
                //path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
                g.fill(path);
            }
            g.setTransform(oldAf);
            g.setClip(null);
        }

        public void drawTo(List<Tag> tags, int startX, int startY, Graphics2D g, int shapeNum) {
            g.setComposite(AlphaComposite.SrcOver);
            fill(tags, startX, startY, g, shapeNum);
            draw(startX, startY, g, shapeNum);
        }

        public GeneralPath toGeneralPath(int startX, int startY) {

            GeneralPath ret = new GeneralPath();
            int x = startX;
            int y = startY;
            boolean wasMoveTo = false;
            for (SHAPERECORD rec : edges) {
                int nx = rec.changeX(x);
                int ny = rec.changeY(y);
                if (rec instanceof StyleChangeRecord) {
                    StyleChangeRecord scr = (StyleChangeRecord) rec;
                    if (scr.stateMoveTo) {
                        nx += startX;
                        ny += startY;
                        if ((!wasMoveTo) || ((nx != x) || (ny != y))) {
                            ret.moveTo(nx, ny);
                        }
                        wasMoveTo = true;
                    }
                }
                if (rec instanceof StraightEdgeRecord) {
                    if (!wasMoveTo) {
                        ret.moveTo(x, y);
                        wasMoveTo = true;
                    }
                    ret.lineTo(nx, ny);
                }
                if (rec instanceof CurvedEdgeRecord) {
                    if (!wasMoveTo) {
                        ret.moveTo(x, y);
                        wasMoveTo = true;
                    }
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
                params += " stroke-width=\"" + SWF.twipToPixel(lineStyle2.width) + "\"" + (lineStyle2.color != null ? " stroke=\"" + lineStyle2.color.toHexRGB() + "\"" : "");
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
    static int cnt = 0;

    private static List<Path> getPaths(RECT bounds, int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, List<SHAPERECORD> records) {
        List<Path> paths = new ArrayList<>();
        Path path = null;
        int x = 0;
        int y = 0;
        int max_x = 0;
        int max_y = 0;
        int min_x = Integer.MAX_VALUE;
        int min_y = Integer.MAX_VALUE;
        boolean started = false;
        FILLSTYLE lastFillStyle0 = null;
        FILLSTYLE lastFillStyle1 = null;
        LINESTYLE lastLineStyle = null;
        LINESTYLE2 lastLineStyle2 = null;
        for (SHAPERECORD r : records) {
            if (r instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) r;
                if (path != null) {
                    path.end = new Point(x, y);
                    paths.add(path);
                }
                path = new Path();


                if (scr.stateNewStyles) {
                    fillStyles = scr.fillStyles;
                    lineStylesList = scr.lineStyles;
                    //numFillBits = scr.numFillBits;
                    //numLineBits = scr.numLineBits;
                }
                if (scr.stateFillStyle0) {
                    if ((scr.fillStyle0 == 0) || (fillStyles == null)) {
                        path.fillStyle0 = null;
                    } else {
                        path.fillStyle0 = fillStyles.fillStyles[scr.fillStyle0 - 1];
                    }
                } else {
                    path.fillStyle0 = lastFillStyle0;
                }

                lastFillStyle0 = path.fillStyle0;
                if (scr.stateFillStyle1) {
                    if ((scr.fillStyle1 == 0) || (fillStyles == null)) {
                        path.fillStyle1 = null;
                    } else {
                        path.fillStyle1 = fillStyles.fillStyles[scr.fillStyle1 - 1];
                    }
                } else {
                    path.fillStyle1 = lastFillStyle1;
                }
                lastFillStyle1 = path.fillStyle1;
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
                } else {
                    if (shapeNum >= 4) {
                        path.lineStyle2 = lastLineStyle2;
                    } else {
                        path.lineStyle = lastLineStyle;
                    }
                }

                lastLineStyle = path.lineStyle;
                lastLineStyle2 = path.lineStyle2;
                if (started && (!scr.stateMoveTo)) {
                    scr.stateMoveTo = true;
                    scr.moveDeltaX = x;
                    scr.moveDeltaY = y;
                }
            }
            if (path == null) {
                path = new Path();
                path.start = new Point(x, y);
            }
            path.edges.add(r);
            x = r.changeX(x);
            y = r.changeY(y);
            if (path.start == null) {
                path.start = new Point(x, y);
            }
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
        path.end = new Point(x, y);
        paths.add(path);
        List<Path> paths2 = new ArrayList<>();
        for (Path p : paths) {
            if ((p.fillStyle0 == null) && (p.fillStyle1 == null)) {
                paths2.add(p);
            }
            if (p.fillStyle0 != null) {
                paths2.add(p);
            }
            if (p.fillStyle1 != null) {
                Path f = new Path();
                boolean flip = true;
                f.fillStyle0 = p.fillStyle1;
                f.lineStyle = p.lineStyle;
                f.lineStyle2 = p.lineStyle2;
                f.useLineStyle2 = p.useLineStyle2;
                f.edges = new ArrayList<>();
                f.start = p.end;
                f.end = p.start;
                if (!flip) {
                    f.edges = p.edges;
                } else {
                    x = 0;
                    y = 0;
                    boolean revstarted = false;
                    List<SHAPERECORD> chedges = new ArrayList<>();
                    for (SHAPERECORD r : p.edges) {
                        int oldX = x;
                        int oldY = y;
                        x = r.changeX(x);
                        y = r.changeY(y);
                        if (r instanceof StyleChangeRecord) {
                            StyleChangeRecord mv = (StyleChangeRecord) r;
                            if (mv.stateMoveTo) {
                                StyleChangeRecord mv2 = null;
                                try {
                                    mv2 = (StyleChangeRecord) mv.clone();
                                } catch (CloneNotSupportedException ex) {
                                    Logger.getLogger(SHAPERECORD.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                mv2.moveDeltaX = oldX;
                                mv2.moveDeltaY = oldY;
                                r = mv2;
                                if (!revstarted) {
                                    revstarted = true;
                                    continue;
                                }
                            }
                        }
                        chedges.add(r);
                    }
                    int finishX = x;
                    int finishY = y;
                    StyleChangeRecord scr = new StyleChangeRecord();
                    scr.stateMoveTo = true;
                    scr.moveDeltaX = finishX;
                    scr.moveDeltaY = finishY;
                    f.edges.add(scr);

                    x = finishX;
                    y = finishY;
                    for (int e = chedges.size() - 1; e >= 0; e--) {
                        SHAPERECORD r = chedges.get(e);
                        SHAPERECORD r2 = null;
                        try {
                            r2 = (SHAPERECORD) r.clone();
                        } catch (CloneNotSupportedException ex) {
                            Logger.getLogger(SHAPERECORD.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        r2.flip();

                        x = r.changeX(x);
                        y = r.changeY(y);
                        f.edges.add(r2);
                    }
                }
                paths2.add(f);
            }
        }
        List<List<Path>> groupedPaths = new ArrayList<>();
        for (Path p1 : paths2) {
            boolean found = false;
            for (List<Path> list : groupedPaths) {
                if (list.contains(p1)) {
                    continue;
                }
                if (p1.sameStyle(list.get(0))) {
                    list.add(p1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                List<Path> newitem = new ArrayList<>();
                newitem.add(p1);
                groupedPaths.add(newitem);
            }
        }
        List<Path> subpath;
        List<Path> edges;
        Point k2;
        Point k1;

        List<Path> ret = new ArrayList<>();

        for (int e = 0; e < groupedPaths.size(); e++) {
            List<Path> pathList = groupedPaths.get(e);
            List<List<Path>> m = new ArrayList<>();
            Map<Point, List<Path>> ind = new HashMap<>();
            int usedCount = 0;
            for (int f = 0; f < pathList.size(); f++) {
                path = pathList.get(f);
                if (path.start.equals(path.end)) {
                    path.used = true;
                    usedCount++;
                    List<Path> psh = new ArrayList<>();
                    psh.add(path);
                    m.add(psh);
                } else {
                    path.used = false;
                    if (!ind.containsKey(path.start)) {
                        ind.put(path.start, new ArrayList<Path>());
                    }
                    ind.get(path.start).add(path);
                }
            }
            for (int f = 0; f < pathList.size(); f++) {
                if (usedCount == pathList.size()) {
                    break;
                }
                path = pathList.get(f);
                if (!path.used) {
                    subpath = new ArrayList<>();
                    subpath.add(path);
                    path.used = true;
                    usedCount++;
                    edges = ind.get(path.start);
                    for (int l = 0; l < edges.size(); l++) {
                        if (edges.get(l) == path) {
                            edges.remove(l);
                        }
                    }
                    k1 = path.start;
                    k2 = path.end;
                    while (!k2.equals(k1)) {
                        if (!ind.containsKey(k2)) {
                            break;
                        }
                        edges = ind.get(k2);
                        if (edges.isEmpty()) {
                            break;
                        }
                        path = edges.remove(0);
                        subpath.add(path);
                        path.used = true;
                        usedCount++;
                        k2 = path.end;
                    }
                    m.add(subpath);
                }
            }

            if (!m.isEmpty()) {
                Path onepath = null;
                for (List<Path> list : m) {
                    for (Path p : list) {
                        if (onepath == null) {
                            onepath = new Path();
                            onepath.fillStyle0 = p.fillStyle0;
                        }
                        if (onepath.start == null) {
                            onepath.start = p.start;
                        }
                        onepath.edges.addAll(p.edges);
                        onepath.end = p.end;
                    }
                }
                if (onepath != null) {
                    ret.add(onepath);
                }
            }
        }
        bounds.Xmax = max_x;
        bounds.Ymax = max_y;
        bounds.Xmin = min_x;
        bounds.Ymin = min_y;
        return ret;
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
        List<Path> paths = getPaths(bounds, shapeNum, fillStyles, lineStylesList, /*numFillBits, numLineBits,*/ records);
        ret = "";
        for (Path p : paths) {
            ret += p.toSVG(shapeNum);
        }
        ret = "<?xml version='1.0' encoding='UTF-8' ?> \n"
                + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
                + "<svg width=\"" + (int) Math.ceil(SWF.twipToPixel(bounds.Xmax)) + "\"\n"
                + "     height=\"" + (int) Math.ceil(SWF.twipToPixel(bounds.Ymax)) + "\"\n"
                + "     viewBox=\"0 0 " + (int) Math.ceil(SWF.twipToPixel(bounds.Xmax)) + " " + (int) Math.ceil(SWF.twipToPixel(bounds.Ymax) + 50) + "\"\n"
                + "     xmlns=\"http://www.w3.org/2000/svg\"\n"
                + "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + ret + ""
                + "</svg>";
        return ret;
    }
    private static HashMap<String, BufferedImage> cache = new HashMap<>();

    public static BufferedImage shapeToImage(List<Tag> tags, int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, List<SHAPERECORD> records) {
        return shapeToImage(tags, shapeNum, fillStyles, lineStylesList, records, null);
    }

    public static List<GeneralPath> shapeToPaths(List<Tag> tags, int shapeNum, List<SHAPERECORD> records) {
        RECT rect = new RECT();
        List<Path> paths = getPaths(rect, shapeNum, null, null, records);
        List<GeneralPath> ret = new ArrayList<>();
        for (Path p : paths) {
            ret.add(p.toGeneralPath(-rect.Xmin, -rect.Ymin));
        }
        return ret;
    }

    public static BufferedImage shapeToImage(List<Tag> tags, int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, List<SHAPERECORD> records, Color defaultColor) {
        String key = "shape_" + records.hashCode() + "_" + defaultColor.hashCode();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        RECT rect = new RECT();
        List<Path> paths = getPaths(rect, shapeNum, fillStyles, lineStylesList, /*numFillBits, numLineBits,*/ records);
        BufferedImage ret = new BufferedImage(
                (int) (rect.getWidth() / DESCALE + 2), (int) (rect.getHeight() / DESCALE + 2), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) ret.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Path p : paths) {
            if ((p.fillStyle0 == null) && (p.lineStyle == null) && (p.lineStyle2 == null) && (defaultColor != null)) {
                p.fillStyle0 = new FILLSTYLE();
                p.fillStyle0.fillStyleType = FILLSTYLE.SOLID;
                p.fillStyle0.color = new RGB(defaultColor);
                p.fillStyle0.colorA = new RGBA(defaultColor);
            }
            p.drawTo(tags, -rect.Xmin, -rect.Ymin, g, shapeNum);
        }
        cache.put(key, ret);
        return ret;
    }

    public abstract boolean isMove();
}
