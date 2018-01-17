/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
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
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class BitmapExporter extends ShapeExporterBase {

    private static final Point POINT_NEG16384_0 = new Point(-16384, 0);

    private static final Point POINT_16384_0 = new Point(16384, 0);

    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    private SerializableImage image;

    private Graphics2D graphics;

    private final Color defaultColor;

    private final SWF swf;

    private final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);  //For correct intersections display;

    private Paint fillPaint;

    private AffineTransform fillTransform;

    private Paint linePaint;

    private AffineTransform lineTransform;

    private Color lineColor;

    private Stroke lineStroke;

    private Stroke defaultStroke;

    private Matrix strokeTransformation;

    private static boolean linearGradientColorWarnignShown = false;

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

    public static void export(SWF swf, SHAPE shape, Color defaultColor, SerializableImage image, Matrix transformation, Matrix strokeTransformation, ColorTransform colorTransform) {
        BitmapExporter exporter = new BitmapExporter(swf, shape, defaultColor, colorTransform);
        exporter.exportTo(image, transformation, strokeTransformation);
    }

    private BitmapExporter(SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform) {
        super(swf, shape, colorTransform);
        this.swf = swf;
        this.defaultColor = defaultColor;
    }

    private void exportTo(SerializableImage image, Matrix transformation, Matrix strokeTransformation) {
        this.image = image;
        this.strokeTransformation = strokeTransformation.clone();
        this.strokeTransformation.scaleX /= SWF.unitDivisor;
        this.strokeTransformation.scaleY /= SWF.unitDivisor;

        graphics = (Graphics2D) image.getGraphics();
        AffineTransform at = transformation.toTransform();
        at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
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
    public void endLines(boolean close) {
        if (close) {
            path.closePath();
        }

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

                if (colorsArr.length >= 2) {
                    fillPaint = new LinearGradientPaint(POINT_NEG16384_0, POINT_16384_0, ratiosArr, colorsArr, cm, cstype, IDENTITY_TRANSFORM);
                } else {
                    if (!linearGradientColorWarnignShown) {
                        Logger.getLogger(BitmapExporter.class.getName()).log(Level.WARNING, "Linear gradient fill should have at least 2 gradient records.");
                        linearGradientColorWarnignShown = true;
                    }

                    if (colorsArr.length == 1) {
                        fillPaint = colorsArr[0];
                    } else {
                        fillPaint = null;
                    }
                }

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

                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, ratiosArr, colorsArr, cm);
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

                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
                fillTransform = matrix.toTransform();
            }
            break;
        }
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        ImageTag imageTag = swf.getImage(bitmapId);
        if (imageTag != null) {
            SerializableImage img = imageTag.getImageCached();
            if (img != null) {
                if (colorTransform != null) {
                    img = colorTransform.apply(img);
                }

                fillPaint = new TexturePaint(img.getBufferedImage(), new java.awt.Rectangle(img.getWidth(), img.getHeight()));
                fillTransform = matrix.toTransform();
                return;
            }
        }

        // fill with red in case any error
        fillPaint = Color.RED;
        fillTransform = matrix.toTransform();
    }

    @Override
    public void endFill() {
        finalizePath();
        fillPaint = null;
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
        finalizePath();
        linePaint = null;
        lineTransform = null;
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
                thickness *= strokeTransformation.scaleY;
                break;
            case "HORIZONTAL":
                thickness *= strokeTransformation.scaleX;
                break;
            case "NORMAL":
                thickness *= Math.max(strokeTransformation.scaleX, strokeTransformation.scaleY);
                break;
        }

        if (thickness < 0) {
            thickness = -thickness;
        }

        if (joinStyle == BasicStroke.JOIN_MITER) {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle, miterLimit);
        } else {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle);
        }

        // Do not scale strokes automatically:
        try {
            AffineTransform t = (AffineTransform) graphics.getTransform().clone();
            t.translate(-0.5, -0.5);
            lineStroke = new TransformedStroke(lineStroke, t);
        } catch (NoninvertibleTransformException net) {
            // ignore
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        MultipleGradientPaint.ColorSpaceType cstype = MultipleGradientPaint.ColorSpaceType.SRGB;
        if (interpolationMethod == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
            cstype = MultipleGradientPaint.ColorSpaceType.LINEAR_RGB;
        }
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

                linePaint = new LinearGradientPaint(POINT_NEG16384_0, POINT_16384_0, ratiosArr, colorsArr, cm, cstype, IDENTITY_TRANSFORM);
                lineTransform = matrix.toTransform();
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
                switch (spreadMethod) {
                    case GRADIENT.SPREAD_PAD_MODE:
                        cm = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                        break;
                    case GRADIENT.SPREAD_REFLECT_MODE:
                        cm = MultipleGradientPaint.CycleMethod.REFLECT;
                        break;
                    case GRADIENT.SPREAD_REPEAT_MODE:
                        cm = MultipleGradientPaint.CycleMethod.REPEAT;
                        break;
                }

                linePaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, ratiosArr, colorsArr, cm);
                lineTransform = matrix.toTransform();
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

                linePaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
                lineTransform = matrix.toTransform();
            }
            break;
        }
    }

    @Override
    public void moveTo(double x, double y) {
        path.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        path.lineTo(x, y);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        path.quadTo(controlX, controlY, anchorX, anchorY);
    }

    protected void finalizePath() {
        if (fillPaint != null) {
            graphics.setComposite(AlphaComposite.SrcOver);
            if (fillPaint instanceof MultipleGradientPaint) {
                AffineTransform oldAf = graphics.getTransform();
                graphics.setClip(path);
                Matrix inverse = null;
                try {
                    double scx = fillTransform.getScaleX();
                    double scy = fillTransform.getScaleY();
                    double shx = fillTransform.getShearX();
                    double shy = fillTransform.getShearY();
                    double det = scx * scy - shx * shy;
                    if (Math.abs(det) <= Double.MIN_VALUE) {
                        // use only the translate values
                        // todo: make it better
                        fillTransform.setToTranslation(fillTransform.getTranslateX(), fillTransform.getTranslateY());
                    }

                    inverse = new Matrix(new AffineTransform(fillTransform).createInverse());

                } catch (NoninvertibleTransformException ex) {
                    // it should never happen as we already checked the determinant of the matrix
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
                    double scx = fillTransform.getScaleX();
                    double scy = fillTransform.getScaleY();
                    double shx = fillTransform.getShearX();
                    double shy = fillTransform.getShearY();
                    double det = scx * scy - shx * shy;
                    if (Math.abs(det) <= Double.MIN_VALUE) {
                        // use only the translate values
                        // todo: make it better
                        fillTransform.setToTranslation(fillTransform.getTranslateX(), fillTransform.getTranslateY());
                    }

                    inverse = new Matrix(new AffineTransform(fillTransform).createInverse());
                } catch (NoninvertibleTransformException ex) {
                    // it should never happen as we already checked the determinant of the matrix
                }

                fillTransform.preConcatenate(oldAf);
                graphics.setTransform(fillTransform);
                graphics.setPaint(fillPaint);

                if (inverse != null) {
                    ExportRectangle rect = inverse.transform(new ExportRectangle(path.getBounds2D()));
                    double minX = rect.xMin;
                    double minY = rect.yMin;
                    graphics.fill(new Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                }

                graphics.setTransform(oldAf);
                graphics.setClip(null);
            } else {
                graphics.setPaint(fillPaint);
                graphics.fill(path);
            }
        }
        if (linePaint != null && lineStroke != null) {
            Shape strokedShape = lineStroke.createStrokedShape(path);
            graphics.setComposite(AlphaComposite.SrcOver);
            if (linePaint instanceof MultipleGradientPaint) {
                AffineTransform oldAf = graphics.getTransform();
                graphics.setClip(strokedShape);
                Matrix inverse = null;
                try {
                    double scx = lineTransform.getScaleX();
                    double scy = lineTransform.getScaleY();
                    double shx = lineTransform.getShearX();
                    double shy = lineTransform.getShearY();
                    double det = scx * scy - shx * shy;
                    if (Math.abs(det) <= Double.MIN_VALUE) {
                        // use only the translate values
                        // todo: make it better
                        lineTransform.setToTranslation(lineTransform.getTranslateX(), lineTransform.getTranslateY());
                    }

                    inverse = new Matrix(new AffineTransform(lineTransform).createInverse());

                } catch (NoninvertibleTransformException ex) {
                    // it should never happen as we already checked the determinant of the matrix
                }

                lineTransform.preConcatenate(oldAf);
                graphics.setTransform(lineTransform);
                graphics.setPaint(linePaint);

                if (inverse != null) {
                    ExportRectangle rect = inverse.transform(new ExportRectangle(strokedShape.getBounds2D()));
                    double minX = rect.xMin;
                    double minY = rect.yMin;
                    graphics.fill(new java.awt.Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                }

                graphics.setTransform(oldAf);
                graphics.setClip(null);
            } else if (linePaint instanceof TexturePaint) {
                AffineTransform oldAf = graphics.getTransform();
                graphics.setClip(strokedShape);
                Matrix inverse = null;
                try {
                    double scx = lineTransform.getScaleX();
                    double scy = lineTransform.getScaleY();
                    double shx = lineTransform.getShearX();
                    double shy = lineTransform.getShearY();
                    double det = scx * scy - shx * shy;
                    if (Math.abs(det) <= Double.MIN_VALUE) {
                        // use only the translate values
                        // todo: make it better
                        lineTransform.setToTranslation(lineTransform.getTranslateX(), lineTransform.getTranslateY());
                    }

                    inverse = new Matrix(new AffineTransform(lineTransform).createInverse());
                } catch (NoninvertibleTransformException ex) {
                    // it should never happen as we already checked the determinant of the matrix
                }

                lineTransform.preConcatenate(oldAf);
                graphics.setTransform(lineTransform);
                graphics.setPaint(linePaint);

                if (inverse != null) {
                    ExportRectangle rect = inverse.transform(new ExportRectangle(path.getBounds2D()));
                    double minX = rect.xMin;
                    double minY = rect.yMin;
                    graphics.fill(new Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                }

                graphics.setTransform(oldAf);
                graphics.setClip(null);
            } else {
                graphics.setPaint(linePaint);
                graphics.fill(strokedShape);
            }
        } else if (lineColor != null) {
            graphics.setColor(lineColor);
            graphics.setStroke(lineStroke == null ? defaultStroke : lineStroke);
            graphics.draw(path);
        }

        path.reset();
        lineStroke = null;
        lineColor = null;
        fillPaint = null;
    }
}
