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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class BitmapExporter extends ShapeExporterBase implements IShapeExporter {

    private static final Cache<SerializableImage> cache = Cache.getInstance(false);
    private static final Cache<Double> cacheDeltaX = Cache.getInstance(false);
    private static final Cache<Double> cacheDeltaY = Cache.getInstance(false);

    private SerializableImage image;
    private Graphics2D graphics;
    private final Color defaultColor;
    private final boolean putToCache;
    public double deltaX;
    public double deltaY;
    private final SWF swf;
    private GeneralPath path;
    private Paint fillPathPaint;
    private Paint fillPaint;
    private AffineTransform fillTransform;
    private Color lineColor;
    private Stroke lineStroke;
    private Stroke defaultStroke;

    static int imageid = 0;
    
    public static SerializableImage export(SWF swf, SHAPE shape, Color defaultColor, boolean putToCache) {
        return export(swf, shape, defaultColor, putToCache, null);
    }
    
    public static SerializableImage export(SWF swf, SHAPE shape, Color defaultColor, boolean putToCache, Matrix matrix) {
        BitmapExporter exporter = new BitmapExporter(swf, shape, defaultColor, putToCache);
        exporter.export();
        if (matrix != null) {
            matrix.translate(exporter.deltaX, exporter.deltaY);
        }
        return exporter.getImage();
    }
    
    public BitmapExporter(SWF swf, SHAPE shape) {
        this(swf, shape, null, true);
    }

    public BitmapExporter(SWF swf, SHAPE shape, boolean putToCache) {
        this(swf, shape, null, putToCache);
    }

    public BitmapExporter(SWF swf, SHAPE shape, Color defaultColor) {
        this(swf, shape, defaultColor, true);
    }

    public BitmapExporter(SWF swf, SHAPE shape, Color defaultColor, boolean putToCache) {
        super(shape);
        this.swf = swf;
        this.defaultColor = defaultColor;
        this.putToCache = putToCache;
    }

    @Override
    public void export() {
        List<SHAPERECORD> records = shape.shapeRecords;
        String key = "shape_" + records.hashCode() + "_" + (defaultColor == null ? "null" : defaultColor.hashCode());
        if (cache.contains(key)) {
            image = (SerializableImage) cache.get(key);
            deltaX = (double) cacheDeltaX.get(key);
            deltaY = (double) cacheDeltaY.get(key);
            return;
        }
        RECT bounds = SHAPERECORD.getBounds(records);
        int maxLineWidthTwips = 0;
        if (shape instanceof SHAPEWITHSTYLE) {
            SHAPEWITHSTYLE shapeWithStyle = (SHAPEWITHSTYLE) shape;
            for (LINESTYLE lineStyle : shapeWithStyle.lineStyles.lineStyles) {
                if (lineStyle.width > maxLineWidthTwips) {
                    maxLineWidthTwips = lineStyle.width;
                }
            }
        }
        double maxLineWidth = maxLineWidthTwips / unitDivisor / 2;
        deltaX = bounds.Xmin / unitDivisor - maxLineWidth;
        deltaY = bounds.Ymin / unitDivisor - maxLineWidth;
        image = new SerializableImage(
            (int) (bounds.getWidth() / unitDivisor + 2 + maxLineWidth), (int) (bounds.getHeight() / unitDivisor + 2 + maxLineWidth), SerializableImage.TYPE_INT_ARGB);
        graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        defaultStroke = graphics.getStroke();
        super.export();
        try {
            ImageIO.write(image.getBufferedImage(), "png", new File("c:\\10\\imageid" + imageid ++ + ".png"));
        } catch (IOException ex) {
            Logger.getLogger(BitmapExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (putToCache) {
            cache.put(key, image);
            cacheDeltaX.put(key, deltaX);
            cacheDeltaY.put(key, deltaY);
        }
    }

    public SerializableImage getImage() {
        return image;
    }

    public static void clearShapeCache() {
        cache.clear();
    }

    @Override
    public void beginShape() {
    }

    @Override
    public void endShape(double xMin, double yMin, double xMax, double yMax) {
    }

    @Override
    public void beginFills() {
    }

    @Override
    public void endFills() {
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void endLines() {
        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        finalizePath();
        fillPaint = color.toColor();
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        switch (type) {
            case FILLSTYLE.LINEAR_GRADIENT: {
                List<Color> colors = new ArrayList<>();
                List<Float> ratios = new ArrayList<>();
                for (int i = 0; i < gradientRecords.length; i++) {
                    if ((i > 0) && (gradientRecords[i - 1].ratio == gradientRecords[i].ratio)) {
                        continue;
                    }
                    ratios.add(gradientRecords[i].getRatioFloat());
                    colors.add(gradientRecords[i].color.toColor());
                }

                float[] ratiosArr = new float[ratios.size()];
                for (int i = 0; i < ratios.size(); i++) {
                    ratiosArr[i] = ratios.get(i);
                }
                Color[] colorsArr = colors.toArray(new Color[colors.size()]);

                MultipleGradientPaint.CycleMethod cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                if (spreadMethod == GRADIENT.SPREAD_PAD_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                } else if (spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REFLECT;
                } else if (spreadMethod == GRADIENT.SPREAD_REPEAT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REPEAT;
                }

                fillPathPaint = null;
                fillPaint = new LinearGradientPaint(new java.awt.Point(-16384, 0), new java.awt.Point(16384, 0), ratiosArr, colorsArr, cm);
                matrix.translateX -= deltaX;
                matrix.translateY -= deltaY;
                fillTransform = matrix.toTransform();
            }
            break;
            case FILLSTYLE.RADIAL_GRADIENT: {
                List<Color> colors = new ArrayList<>();
                List<Float> ratios = new ArrayList<>();
                for (int i = 0; i < gradientRecords.length; i++) {
                    if ((i > 0) && (gradientRecords[i - 1].ratio == gradientRecords[i].ratio)) {
                        continue;
                    }
                    ratios.add(gradientRecords[i].getRatioFloat());
                    colors.add(gradientRecords[i].color.toColor());
                }

                float[] ratiosArr = new float[ratios.size()];
                for (int i = 0; i < ratios.size(); i++) {
                    ratiosArr[i] = ratios.get(i);
                }
                Color[] colorsArr = colors.toArray(new Color[colors.size()]);

                MultipleGradientPaint.CycleMethod cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                if (spreadMethod == GRADIENT.SPREAD_PAD_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                } else if (spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REFLECT;
                } else if (spreadMethod == GRADIENT.SPREAD_REPEAT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REPEAT;
                }

                Color endColor = gradientRecords[gradientRecords.length - 1].color.toColor();
                fillPathPaint = endColor;
                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, ratiosArr, colorsArr, cm);
                matrix.translateX -= deltaX;
                matrix.translateY -= deltaY;
                fillTransform = matrix.toTransform();
            }
            break;
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT: {
                List<Color> colors = new ArrayList<>();
                List<Float> ratios = new ArrayList<>();
                for (int i = 0; i < gradientRecords.length; i++) {
                    if ((i > 0) && (gradientRecords[i - 1].ratio == gradientRecords[i].ratio)) {
                        continue;
                    }
                    ratios.add(gradientRecords[i].getRatioFloat());
                    colors.add(gradientRecords[i].color.toColor());
                }

                float[] ratiosArr = new float[ratios.size()];
                for (int i = 0; i < ratios.size(); i++) {
                    ratiosArr[i] = ratios.get(i);
                }
                Color[] colorsArr = colors.toArray(new Color[colors.size()]);

                MultipleGradientPaint.CycleMethod cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                if (spreadMethod == GRADIENT.SPREAD_PAD_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                } else if (spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REFLECT;
                } else if (spreadMethod == GRADIENT.SPREAD_REPEAT_MODE) {
                    cm = MultipleGradientPaint.CycleMethod.REPEAT;
                }

                Color endColor = gradientRecords[gradientRecords.length - 1].color.toColor();
                fillPathPaint = endColor;
                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm);
                matrix.translateX -= deltaX;
                matrix.translateY -= deltaY;
                fillTransform = matrix.toTransform();
            }
            break;
        }
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth) {
        finalizePath();
        ImageTag image = null;
        for (Tag t : swf.tags) {
            if (t instanceof ImageTag) {
                ImageTag i = (ImageTag) t;
                if (i.getCharacterId() == bitmapId) {
                    image = i;
                    break;
                }
            }
        }
        if (image != null) {
            SerializableImage img = image.getImage(swf.tags);
            if (img != null) {
                fillPaint = new TexturePaint(img.getBufferedImage(), new java.awt.Rectangle(img.getWidth(), img.getHeight()));
                matrix.translateX -= deltaX;
                matrix.translateY -= deltaY;
                fillTransform = matrix.toTransform();
            }
        }
    }

    @Override
    public void endFill() {
        finalizePath();
        fillPaint = null;
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
        lineColor = color.toColor();
        int capStyle = BasicStroke.CAP_ROUND;
        switch (startCaps) {
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
        int joinStyle = BasicStroke.JOIN_ROUND;
        switch (joints) {
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
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle, miterLimit);
        } else {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle);
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
    }

    @Override
    public void moveTo(double x, double y) {
        path.moveTo(x - deltaX, y - deltaY);
    }

    @Override
    public void lineTo(double x, double y) {
        path.lineTo(x - deltaX, y - deltaY);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        path.quadTo(controlX - deltaX, controlY - deltaY, anchorX - deltaX, anchorY - deltaY);
    }

    protected void finalizePath() {
        final int maxRepeat = 10; // TODO: better handle gradient repeating
        if (path != null) {
            if (fillPaint != null) {
                if (fillPaint instanceof MultipleGradientPaint) {
                    AffineTransform oldAf = graphics.getTransform();
                    if (fillPathPaint != null) {
                        graphics.setPaint(fillPathPaint);
                    }
                    graphics.fill(path);
                    graphics.setClip(path);
                    graphics.setTransform(fillTransform);

                    graphics.setPaint(fillPaint);
                    graphics.fill(new java.awt.Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    graphics.setTransform(oldAf);
                    graphics.setClip(null);
                } else if (fillPaint instanceof TexturePaint) {
                    AffineTransform oldAf = graphics.getTransform();
                    graphics.setClip(path);
                    graphics.setTransform(fillTransform);

                    graphics.setPaint(fillPaint);
                    graphics.fill(new java.awt.Rectangle(-16384 * maxRepeat, -16384 * maxRepeat, 16384 * 2 * maxRepeat, 16384 * 2 * maxRepeat));
                    graphics.setTransform(oldAf);
                    graphics.setClip(null);
                } else {
                    graphics.setPaint(fillPaint);
                    graphics.fill(path);
                }
            }
            if (lineColor != null) {
                graphics.setColor(lineColor);
                graphics.setStroke(lineStroke == null ? defaultStroke : lineStroke);
                graphics.draw(path);
            }
        }
        path = new GeneralPath();
        lineStroke = null;
        lineColor = null;
        fillPaint = null;
    }
}
