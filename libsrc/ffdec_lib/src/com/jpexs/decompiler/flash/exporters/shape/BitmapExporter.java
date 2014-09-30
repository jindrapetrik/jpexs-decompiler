/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
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
public class BitmapExporter extends ShapeExporterBase {

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

    private class TransformedStroke implements Stroke {

        /**
         * To make this serializable without problems.
         */
        private static final long serialVersionUID = 1;

        /**
         * the AffineTransform used to transform the shape before stroking.
         */
        private final AffineTransform transform;
        /**
         * The inverse of {@link #transform}, used to transform back after
         * stroking.
         */
        private final AffineTransform inverse;

        /**
         * Our base stroke.
         */
        private final Stroke stroke;

        /**
         * Creates a TransformedStroke based on another Stroke and an
         * AffineTransform.
         */
        public TransformedStroke(Stroke base, AffineTransform at)
                throws NoninvertibleTransformException {
            this.transform = new AffineTransform(at);
            this.inverse = transform.createInverse();
            this.stroke = base;
        }

        /**
         * Strokes the given Shape with this stroke, creating an outline.
         *
         * This outline is distorted by our AffineTransform relative to the
         * outline which would be given by the base stroke, but only in terms of
         * scaling (i.e. thickness of the lines), as translation and rotation
         * are undone after the stroking.
         */
        @Override
        public Shape createStrokedShape(Shape s) {
            Shape sTrans = transform.createTransformedShape(s);
            Shape sTransStroked = stroke.createStrokedShape(sTrans);
            Shape sStroked = inverse.createTransformedShape(sTransStroked);
            return sStroked;
        }
    }

    public static void export(SWF swf, SHAPE shape, Color defaultColor, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        BitmapExporter exporter = new BitmapExporter(swf, shape, defaultColor, colorTransform);
        exporter.exportTo(image, transformation);
    }

    private BitmapExporter(SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform) {
        super(shape, colorTransform);
        this.swf = swf;
        this.defaultColor = defaultColor;
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
    public void endShape() {
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
        MultipleGradientPaint.ColorSpaceType cstype = MultipleGradientPaint.ColorSpaceType.SRGB;
        if (interpolationMethod == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
            cstype = MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
        }
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
                fillPaint = new LinearGradientPaint(new java.awt.Point(-16384, 0), new java.awt.Point(16384, 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
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
                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
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
        switch (scaleMode) {
            case "VERTICAL":
                thickness *= graphics.getTransform().getScaleY();
                break;
            case "HORIZONTAL":
                thickness *= graphics.getTransform().getScaleX();
                break;
            case "NORMAL":
                thickness *= Math.max(graphics.getTransform().getScaleX(), graphics.getTransform().getScaleY());
                break;
        }
        if (joinStyle == BasicStroke.JOIN_MITER) {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle, miterLimit);
        } else {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle);
        }
        //Do not scale strokes automatically:
        try {
            lineStroke = new TransformedStroke(lineStroke, graphics.getTransform());
        } catch (NoninvertibleTransformException net) {
            //ignore
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
                graphics.setComposite(AlphaComposite.SrcOver);
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
