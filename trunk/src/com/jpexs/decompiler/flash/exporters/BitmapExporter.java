/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.decompiler.flash.types.ColorTransform;
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
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class BitmapExporter extends ShapeExporterBase implements IShapeExporter {

    private SerializableImage image;
    private Graphics2D graphics;
    private final Color defaultColor;
    private double deltaX;
    private double deltaY;
    private final SWF swf;
    private GeneralPath path;
    private Paint fillPathPaint;
    private Paint fillPaint;
    private AffineTransform fillTransform;
    private Color lineColor;
    private Stroke lineStroke;
    private Stroke defaultStroke;
    private double unitDivisor;

    public static SerializableImage export(SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform) {
        BitmapExporter exporter = new BitmapExporter(swf, shape, defaultColor, colorTransform);
        exporter.export();
        return exporter.getImage();
    }

    public static void exportTo(SWF swf, SHAPE shape, Color defaultColor, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        BitmapExporter exporter = new BitmapExporter(swf, shape, defaultColor, colorTransform);
        exporter.exportTo(image, transformation);
    }

    private BitmapExporter(SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform) {
        super(shape, colorTransform);
        this.swf = swf;
        this.defaultColor = defaultColor;
    }

    @Override
    public void export() {
        List<SHAPERECORD> records = shape.shapeRecords;
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
        unitDivisor = SWF.unitDivisor;
        double maxLineWidth = maxLineWidthTwips / unitDivisor / 2;
        deltaX = bounds.Xmin / unitDivisor - maxLineWidth;
        deltaY = bounds.Ymin / unitDivisor - maxLineWidth;
        double width = bounds.getWidth() / unitDivisor + 2 * (maxLineWidth + 1);
        double height = bounds.getHeight() / unitDivisor + 2 * (maxLineWidth + 1);
        image = new SerializableImage((int) width, (int) height, SerializableImage.TYPE_INT_ARGB);
        image.fillTransparent();

        graphics = (Graphics2D) image.getGraphics();

        defaultStroke = graphics.getStroke();
        super.export();
    }

    private void exportTo(SerializableImage image, Matrix transformation) {
        this.image = image;
        graphics = (Graphics2D) image.getGraphics();
        AffineTransform at = transformation.toTransform();
        at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
        unitDivisor = 1;
        graphics.setTransform(at);
        defaultStroke = graphics.getStroke();
        super.export();
    }

    public SerializableImage getImage() {
        return image;
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
        if (color == null) {
            fillPaint = defaultColor;
        } else {
            fillPaint = color.toColor();
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        matrix.translateX /= unitDivisor;
        matrix.translateY /= unitDivisor;
        matrix.scaleX /= unitDivisor;
        matrix.scaleY /= unitDivisor;
        matrix.rotateSkew0 /= unitDivisor;
        matrix.rotateSkew1 /= unitDivisor;
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
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        matrix.translateX /= unitDivisor;
        matrix.translateY /= unitDivisor;
        matrix.scaleX /= unitDivisor;
        matrix.scaleY /= unitDivisor;
        matrix.rotateSkew0 /= unitDivisor;
        matrix.rotateSkew1 /= unitDivisor;
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
            SerializableImage img = image.getImage();
            if (img != null) {
                img = colorTransform.apply(img);
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
        thickness /= unitDivisor;
        lineColor = color == null ? null : color.toColor();
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
        path.moveTo(x / unitDivisor - deltaX, y / unitDivisor - deltaY);
    }

    @Override
    public void lineTo(double x, double y) {
        path.lineTo(x / unitDivisor - deltaX, y / unitDivisor - deltaY);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        path.quadTo(controlX / unitDivisor - deltaX, controlY / unitDivisor - deltaY,
                anchorX / unitDivisor - deltaX, anchorY / unitDivisor - deltaY);
    }

    protected void finalizePath() {
        if (path != null) {
            if (fillPaint != null) {
                if (fillPaint instanceof MultipleGradientPaint) {
                    AffineTransform oldAf = graphics.getTransform();
                    if (fillPathPaint != null) {
                        graphics.setPaint(fillPathPaint);
                    }
                    graphics.fill(path);
                    graphics.setClip(path);
                    Matrix inverse = null;
                    try {
                        inverse = new Matrix(new AffineTransform(fillTransform).createInverse());
                    } catch (NoninvertibleTransformException ex) {
                    }

                    fillTransform.preConcatenate(oldAf);
                    graphics.setTransform(fillTransform);
                    graphics.setPaint(fillPaint);

                    if (inverse != null) {
                        ExportRectangle rect = inverse.transform(new ExportRectangle(path.getBounds2D()));
                        double minX = rect.xMin;
                        double minY = rect.yMin;
                        graphics.fill(new java.awt.Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                    }

                    graphics.setTransform(oldAf);
                    graphics.setClip(null);
                } else if (fillPaint instanceof TexturePaint) {
                    AffineTransform oldAf = graphics.getTransform();
                    graphics.setClip(path);
                    Matrix inverse = null;
                    try {
                        inverse = new Matrix(new AffineTransform(fillTransform).createInverse());
                    } catch (NoninvertibleTransformException ex) {
                    }

                    fillTransform.preConcatenate(oldAf);
                    graphics.setTransform(fillTransform);
                    graphics.setPaint(fillPaint);

                    if (inverse != null) {
                        ExportRectangle rect = inverse.transform(new ExportRectangle(path.getBounds2D()));
                        double minX = rect.xMin;
                        double minY = rect.yMin;
                        graphics.fill(new java.awt.Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                    }

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
        path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);  //For correct intersections display
        lineStroke = null;
        lineColor = null;
        fillPaint = null;
    }
}
