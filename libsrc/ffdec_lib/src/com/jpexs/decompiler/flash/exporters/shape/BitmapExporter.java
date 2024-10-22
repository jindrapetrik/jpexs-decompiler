/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.exporters.ImageTagBufferedImage;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.graphics.ExtendedBasicStroke;
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
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Bitmap exporter.
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

    private final GeneralPath path;

    private Shape aliasedShape;

    private Paint fillPaint;

    private boolean fillRepeat;

    private boolean fillSmooth;

    private AffineTransform fillTransform;

    private Paint linePaint;

    private AffineTransform lineTransform;

    private Color lineColor;

    private Stroke lineStroke;

    private Stroke defaultStroke;

    private Matrix strokeTransformation;

    private double thicknessScale;
    private double thicknessScaleX;
    private double thicknessScaleY;

    private double unzoom;

    private static boolean linearGradientColorWarningShown = false;

    private boolean scaleStrokes;

    private boolean aliasedFill = false;

    @Override
    public void beginAliasedFills() {
        aliasedFill = true;
    }

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
         * <p>
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

    /**
     * Exports a shape to a bitmap.
     * @param windingRule Winding rule
     * @param shapeNum Shape number
     * @param swf SWF
     * @param shape Shape
     * @param defaultColor Default color
     * @param image Image
     * @param unzoom Unzoom
     * @param transformation Transformation
     * @param strokeTransformation Stroke transformation
     * @param colorTransform Color transform
     * @param scaleStrokes Scale strokes
     * @param canUseSmoothing Can use smoothing
     */
    public static void export(int windingRule, int shapeNum, SWF swf, SHAPE shape, Color defaultColor, SerializableImage image, double unzoom, Matrix transformation, Matrix strokeTransformation, ColorTransform colorTransform, boolean scaleStrokes, boolean canUseSmoothing) {
        BitmapExporter exporter = new BitmapExporter(windingRule, shapeNum, swf, shape, defaultColor, colorTransform);
        exporter.setCanUseSmoothing(canUseSmoothing);
        exporter.exportTo(shapeNum, image, unzoom, transformation, strokeTransformation, scaleStrokes);
    }

    private BitmapExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
        this.swf = swf;
        this.defaultColor = defaultColor;
        path = new GeneralPath(windingRule == ShapeTag.WIND_NONZERO ? GeneralPath.WIND_NON_ZERO : GeneralPath.WIND_EVEN_ODD);
    }

    private void exportTo(int shapeNum, SerializableImage image, double unzoom, Matrix transformation, Matrix strokeTransformation, boolean scaleStrokes) {
        this.image = image;
        this.scaleStrokes = scaleStrokes;
        ExportRectangle bounds = new ExportRectangle(shape.getBounds(shapeNum));
        this.strokeTransformation = strokeTransformation;
        calculateThicknessScale(bounds, transformation);

        graphics = (Graphics2D) image.getGraphics();
        AffineTransform at = transformation.toTransform();
        at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
        graphics.setTransform(at);
        defaultStroke = graphics.getStroke();
        this.unzoom = unzoom;
        super.export();
    }

    private void calculateThicknessScale(ExportRectangle bounds, Matrix transformation) {
        com.jpexs.decompiler.flash.exporters.commonshape.Point p00 = strokeTransformation.transform(0, 0);
        com.jpexs.decompiler.flash.exporters.commonshape.Point p11 = strokeTransformation.transform(1, 1);
        thicknessScale = p00.distanceTo(p11) / Math.sqrt(2);
        thicknessScaleX = Math.abs(p11.x - p00.x);
        thicknessScaleY = Math.abs(p11.y - p00.y);

        /*Matrix transPre = transformation.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
        p00 = transPre.transform(0, 0);
        p11 = transPre.transform(1, 1);
        realZoom = p00.distanceTo(p11) / Math.sqrt(2);
        System.out.println("realZoom=" + realZoom);*/
    }

    /**
     * Returns the image.
     * @return Image
     */
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
        aliasedFill = false;
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

        List<Color> colors = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        int lastRatio = -1;
        for (int i = 0; i < gradientRecords.length; i++) {
            if ((i > 0) && (gradientRecords[i - 1].ratio == gradientRecords[i].ratio)) {
                if (lastRatio < 255) {
                    lastRatio++;
                }
            } else {
                if (gradientRecords[i].ratio > lastRatio) {
                    lastRatio = gradientRecords[i].ratio;
                } else if (lastRatio < 255) {
                    lastRatio++;
                }
            }
            ratios.add(lastRatio / 255f);
            colors.add(gradientRecords[i].color.toColor());
            if (lastRatio == 255) {
                break;
            }
        }

        if (colors.size() == 1) {
            colors.add(colors.get(0));
            ratios.set(0, 0f);
            ratios.add(1f);
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

        switch (type) {
            case FILLSTYLE.LINEAR_GRADIENT:
                fillPaint = new LinearGradientPaint(POINT_NEG16384_0, POINT_16384_0, ratiosArr, colorsArr, cm, cstype, IDENTITY_TRANSFORM);
                break;
            case FILLSTYLE.RADIAL_GRADIENT:
                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point(0, 0), ratiosArr, colorsArr, cm, cstype, new AffineTransform());
                break;
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                fillPaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
                break;
        }
        fillTransform = matrix.toTransform();
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
                BufferedImage bufImg = img.getBufferedImage();
                if (colorTransform == null) {
                    bufImg = new ImageTagBufferedImage(imageTag, bufImg);
                }
                fillPaint = new TexturePaint(bufImg, new java.awt.Rectangle(img.getWidth(), img.getHeight()));

                fillTransform = matrix.toTransform();
                fillRepeat = repeat;
                fillSmooth = smooth;
                return;
            }
        }

        fillPaint = SWF.ERROR_COLOR;
        fillTransform = matrix.toTransform();
    }

    @Override
    public void endFill() {
        finalizePath();
        fillPaint = null;
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
        linePaint = null;
        lineTransform = null;

        if (thickness == 0) {
            lineColor = null;
            return;
        }

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
        if (scaleStrokes) {
            switch (scaleMode) {
                case "VERTICAL":
                    thickness *= thicknessScaleY;
                    break;
                case "HORIZONTAL":
                    thickness *= thicknessScaleX;
                    break;
                case "NORMAL":
                    thickness *= thicknessScale;
                    break;
                case "NONE":
                    break;
            }
        }

        //always display minimum stroke of 1 pixel, no matter how zoomed it is
        if (thickness * unzoom < 1 * SWF.unitDivisor) {
            thickness = 1 * SWF.unitDivisor / unzoom;
        }

        if (joinStyle == BasicStroke.JOIN_MITER) {
            //lineStroke =  new BasicStroke((float) thickness, capStyle, joinStyle, miterLimit);
            /*if (Configuration.allowMiterClipLinestyle.get()) {
                lineStroke = new MiterClipBasicStroke((BasicStroke) lineStroke);
            }*/
            lineStroke = new ExtendedBasicStroke((float) thickness, capStyle, ExtendedBasicStroke.JOIN_MITER_CLIP, miterLimit);
        } else {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle);
        }

        // Do not scale strokes automatically:
        try {
            AffineTransform t = (AffineTransform) strokeTransformation.toTransform().clone();
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

        List<Color> colors = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        int lastRatio = -1;
        for (int i = 0; i < gradientRecords.length; i++) {
            if ((i > 0) && (gradientRecords[i - 1].ratio == gradientRecords[i].ratio)) {
                if (lastRatio < 255) {
                    lastRatio++;
                }
            } else {
                if (gradientRecords[i].ratio > lastRatio) {
                    lastRatio = gradientRecords[i].ratio;
                } else if (lastRatio < 255) {
                    lastRatio++;
                }
            }
            ratios.add(lastRatio / 255f);
            colors.add(gradientRecords[i].color.toColor());
            if (lastRatio == 255) {
                break;
            }
        }

        if (colors.size() == 1) {
            colors.add(colors.get(0));
            ratios.set(0, 0f);
            ratios.add(1f);
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

        switch (type) {
            case FILLSTYLE.LINEAR_GRADIENT:
                linePaint = new LinearGradientPaint(POINT_NEG16384_0, POINT_16384_0, ratiosArr, colorsArr, cm, cstype, IDENTITY_TRANSFORM);
                break;
            case FILLSTYLE.RADIAL_GRADIENT:
                linePaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, ratiosArr, colorsArr, cm);
                break;
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:
                linePaint = new RadialGradientPaint(new java.awt.Point(0, 0), 16384, new java.awt.Point((int) (focalPointRatio * 16384), 0), ratiosArr, colorsArr, cm, cstype, AffineTransform.getTranslateInstance(0, 0));
                break;
        }

        lineTransform = matrix.toTransform();
    }

    @Override
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        ImageTag imageTag = swf.getImage(bitmapId);
        if (imageTag != null) {
            SerializableImage img = imageTag.getImageCached();
            if (img != null) {
                if (colorTransform != null) {
                    img = colorTransform.apply(img);
                }

                linePaint = new TexturePaint(img.getBufferedImage(), new java.awt.Rectangle(img.getWidth(), img.getHeight()));
                lineTransform = matrix.toTransform();
                return;
            }
        }

        linePaint = SWF.ERROR_COLOR;
        lineTransform = matrix.toTransform();
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

    /**
     * Finalizes the path.
     */
    protected void finalizePath() {
        if (fillPaint != null) {
            Shape shp = path;
            if (aliasedFill) {
                aliasedShape = new BasicStroke((float) (SWF.unitDivisor / unzoom / 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND).createStrokedShape(shp);
                return;
            } else if (aliasedShape != null) {
                Area a = new Area(shp);
                a.add(new Area(aliasedShape));
                shp = a;
            }
            graphics.setComposite(AlphaComposite.SrcOver);
            if (fillPaint instanceof MultipleGradientPaint) {
                AffineTransform oldAf = graphics.getTransform();
                Shape prevClip = graphics.getClip();
                graphics.clip(shp);
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
                    ExportRectangle rect = inverse.transform(new ExportRectangle(shp.getBounds2D()));
                    double minX = rect.xMin;
                    double minY = rect.yMin;
                    graphics.fill(new java.awt.Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                }

                graphics.setTransform(oldAf);
                graphics.setClip(prevClip);
            } else if (fillPaint instanceof TexturePaint) {
                AffineTransform oldAf = graphics.getTransform();
                Shape prevClip = graphics.getClip();
                graphics.clip(shp);
                Matrix inverse = null;
                //if (fillRepeat) {
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
                //}
                fillTransform.preConcatenate(oldAf);
                graphics.setTransform(fillTransform);
                graphics.setPaint(fillPaint);

                Object interpolationBefore = graphics.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                if (fillSmooth) {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                } else {
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                }
                if (fillRepeat) {
                    if (inverse != null) {

                        ExportRectangle rect = inverse.transform(new ExportRectangle(shp.getBounds2D()));
                        double minX = rect.xMin;
                        double minY = rect.yMin;
                        graphics.fill(new Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                    }
                } else {
                    if (inverse != null) {
                        ExportRectangle rect = inverse.transform(new ExportRectangle(shp.getBounds2D()));
                        //left
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                (int) rect.xMin, 0, 0, ((TexturePaint) fillPaint).getImage().getHeight(),
                                0, 0, 1, ((TexturePaint) fillPaint).getImage().getHeight(),
                                null);
                        //top left
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                (int) rect.xMin, (int) rect.yMin, 0, 0,
                                0, 0, 1, 1,
                                null);
                        //top
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                0, (int) rect.yMin, ((TexturePaint) fillPaint).getImage().getWidth(), 0,
                                0, 0, ((TexturePaint) fillPaint).getImage().getWidth(), 1,
                                null);
                        //top right
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                ((TexturePaint) fillPaint).getImage().getWidth(), (int) rect.yMin, (int) rect.xMax, 0,
                                ((TexturePaint) fillPaint).getImage().getWidth() - 1, 0, ((TexturePaint) fillPaint).getImage().getWidth(), 1,
                                null);
                        //right
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                ((TexturePaint) fillPaint).getImage().getWidth(), 0, (int) rect.xMax, ((TexturePaint) fillPaint).getImage().getHeight(),
                                ((TexturePaint) fillPaint).getImage().getWidth() - 1, 0, ((TexturePaint) fillPaint).getImage().getWidth(),
                                ((TexturePaint) fillPaint).getImage().getHeight(),
                                null);
                        //bottom right
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                ((TexturePaint) fillPaint).getImage().getWidth(), ((TexturePaint) fillPaint).getImage().getHeight(), (int) rect.xMax, (int) rect.yMax,
                                ((TexturePaint) fillPaint).getImage().getWidth() - 1, ((TexturePaint) fillPaint).getImage().getHeight() - 1,
                                ((TexturePaint) fillPaint).getImage().getWidth(), ((TexturePaint) fillPaint).getImage().getHeight(),
                                null);
                        //bottom
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                0, ((TexturePaint) fillPaint).getImage().getHeight(), ((TexturePaint) fillPaint).getImage().getWidth(), (int) rect.yMax,
                                0, ((TexturePaint) fillPaint).getImage().getHeight() - 1, ((TexturePaint) fillPaint).getImage().getWidth(), ((TexturePaint) fillPaint).getImage().getHeight(),
                                null);
                        //bottom left
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(),
                                (int) rect.xMin, ((TexturePaint) fillPaint).getImage().getHeight(), 0, (int) rect.yMax,
                                0, ((TexturePaint) fillPaint).getImage().getHeight() - 1,
                                1, ((TexturePaint) fillPaint).getImage().getHeight(),
                                null);

                        //actual central image
                        graphics.drawImage(((TexturePaint) fillPaint).getImage(), 0, 0, null);
                    }
                }
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationBefore);

                graphics.setTransform(oldAf);
                graphics.setClip(prevClip);
            } else {
                graphics.setPaint(fillPaint);
                graphics.fill(shp);
            }
        }
        if (linePaint != null && lineStroke != null) {
            if (true) {
                //return;
            }
            Shape strokedShape = lineStroke.createStrokedShape(path);
            graphics.setComposite(AlphaComposite.SrcOver);
            if (linePaint instanceof MultipleGradientPaint) {
                AffineTransform oldAf = graphics.getTransform();
                Shape prevClip = graphics.getClip();
                graphics.clip(strokedShape);
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
                graphics.setClip(prevClip);
            } else if (linePaint instanceof TexturePaint) {
                AffineTransform oldAf = graphics.getTransform();
                Shape prevClip = graphics.getClip();
                graphics.clip(strokedShape);
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
                    graphics.fill(new Rectangle((int) minX, (int) minY, (int) (rect.xMax - minX), (int) (rect.yMax - minY)));
                }

                graphics.setTransform(oldAf);
                graphics.setClip(prevClip);
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
