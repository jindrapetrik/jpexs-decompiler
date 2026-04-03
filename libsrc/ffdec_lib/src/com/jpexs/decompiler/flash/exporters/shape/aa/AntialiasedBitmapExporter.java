package com.jpexs.decompiler.flash.exporters.shape.aa;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;

/**
 * Antialiased Shape Bitmap Exporter
 *
 * @author JPEXS
 */
public class AntialiasedBitmapExporter extends BitmapExporter {

    private static final Point POINT_NEG16384_0 = new Point(-16384, 0);

    private static final Point POINT_16384_0 = new Point(16384, 0);

    private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    private final List<List<Vec2>> contours = new ArrayList<>();

    private AntialiasTools.SceneRasterizerMSAA rz;

    private Vec2 lastPos = null;

    private int pos = 0;

    private Matrix vecTrans;

    /**
     * Exports a shape to a bitmap.
     *
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
     * @param aaScale Antialias conflation reducing scale coefficient
     */
    public static void export(int windingRule, int shapeNum, SWF swf, SHAPE shape, Color defaultColor, SerializableImage image, double unzoom, Matrix transformation, Matrix strokeTransformation, ColorTransform colorTransform, boolean scaleStrokes, boolean canUseSmoothing, int aaScale) {
        AntialiasedBitmapExporter exporter = new AntialiasedBitmapExporter(windingRule, shapeNum, swf, shape, defaultColor, colorTransform, (int) Math.round(20 / unzoom));
        exporter.setCanUseSmoothing(canUseSmoothing);
        exporter.exportTo(shapeNum, image, unzoom, transformation, strokeTransformation, scaleStrokes, aaScale);
    }

    protected AntialiasedBitmapExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, Color defaultColor, ColorTransform colorTransform, int thicknessDivisor) {
        super(windingRule, shapeNum, swf, shape, defaultColor, colorTransform, thicknessDivisor);
    }

    @Override
    protected void exportTo(int shapeNum, SerializableImage image, double unzoom, Matrix transformation, Matrix strokeTransformation, boolean scaleStrokes, int aaScale) {
        rz = new AntialiasTools.SceneRasterizerMSAA(image.getWidth(), image.getHeight(), 4);
        rz.clear(0x00000000);
        vecTrans = transformation.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor));
        super.exportTo(shapeNum, image, unzoom, transformation, strokeTransformation, scaleStrokes, aaScale);
        rz.resolveTo(image.getBufferedImage());
    }

    private void calculateThicknessScale(ExportRectangle bounds, Matrix transformation) {
        com.jpexs.decompiler.flash.exporters.commonshape.Point p00 = strokeTransformation.transform(0, 0);
        com.jpexs.decompiler.flash.exporters.commonshape.Point p11 = strokeTransformation.transform(1, 1);
        thicknessScale = p00.distanceTo(p11) / Math.sqrt(2);
        thicknessScaleX = Math.abs(p11.x - p00.x);
        thicknessScaleY = Math.abs(p11.y - p00.y);
    }

    /**
     * Returns the image.
     *
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
            //path.closePath();
        }

        finalizePath();
    }

    @Override
    public void moveTo(double x, double y) {
        Point2D src = new Point2D.Double(x, y);
        Point2D dst = new Point2D.Double();
        dst = vecTrans.transform(src);
        contours.add(new ArrayList<>());
        lastPos = new Vec2(dst.getX(), dst.getY());
        contours.get(contours.size() - 1).add(lastPos);
    }

    @Override
    public void lineTo(double x, double y) {
        Point2D src = new Point2D.Double(x, y);
        Point2D dst = new Point2D.Double();
        dst = vecTrans.transform(src);

        lastPos = new Vec2(dst.getX(), dst.getY());
        contours.get(contours.size() - 1).add(lastPos);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        Point2D src = new Point2D.Double(controlX, controlY);
        Point2D dst = new Point2D.Double();
        dst = vecTrans.transform(src);
        Vec2 controlVec2 = new Vec2(dst.getX(), dst.getY());
        src = new Point2D.Double(anchorX, anchorY);
        dst = vecTrans.transform(src);
        Vec2 anchorVec2 = new Vec2(dst.getX(), dst.getY());

        List<Vec2> contour = contours.get(contours.size() - 1);
        AntialiasTools.append(contour, AntialiasTools.flattenQuadraticBezier(lastPos, controlVec2, anchorVec2, 1.0), true);
        lastPos = anchorVec2;
    }

    private void drawImage(BufferedImage image, int dx, int dy, int dx2, int dy2, int sx, int sy, int sx2, int sy2, ImageObserver obs, boolean smooth) {
        List<List<Vec2>> cs = new ArrayList<>();
        List<Vec2> c = new ArrayList<>();
        Point2D dst = new Point2D.Double();

        fillTransform.transform(new Point2D.Double(dx, dy), dst);
        c.add(new Vec2(dst.getX(), dst.getY()));

        fillTransform.transform(new Point2D.Double(dx2, dy), dst);
        c.add(new Vec2(dst.getX(), dst.getY()));

        fillTransform.transform(new Point2D.Double(dx2, dy2), dst);
        c.add(new Vec2(dst.getX(), dst.getY()));

        fillTransform.transform(new Point2D.Double(dx, dy2), dst);
        c.add(new Vec2(dst.getX(), dst.getY()));

        cs.add(c);
        AffineTransform t = new AffineTransform();
        if (dx2 != dx && dy2 != dy) {
            t = AffineTransform.getScaleInstance((sx2 - sx) / (double) (dx2 - dx), (sy2 - sy) / (double) (dy2 - dy));
            t.preConcatenate(fillTransform);
        }

        rz.fillContoursWithPaint(cs, windingRule, new TexturePaint(image, new Rectangle2D.Double(sx, sy, sx2 - sx, sy2 - sy)), t, smooth);
    }

    private void drawImage(BufferedImage image, int x, int y, ImageObserver obs, boolean smooth) {
        drawImage(image, x, y, x + image.getWidth(), y + image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), obs, smooth);
    }

    /**
     * Finalizes the path.
     */
    @Override
    protected void finalizePath() {
        if (fillPaint != null) {
            if (fillPaint instanceof MultipleGradientPaint) {
                fillTransform.preConcatenate(graphics.getTransform());
                rz.fillContoursWithPaint(contours, Path2D.WIND_EVEN_ODD, fillPaint, fillTransform);
            } else if (fillPaint instanceof TexturePaint) {
                //rz.setClipContours(contours, Path2D.WIND_EVEN_ODD);
                fillTransform.preConcatenate(graphics.getTransform());

                if (fillRepeat) {
                    rz.fillContoursWithPaint(contours, Path2D.WIND_EVEN_ODD, fillPaint, fillTransform, fillSmooth);
                } else {
                    drawImage(((TexturePaint) fillPaint).getImage(), 0, 0, null, fillSmooth);
                }
            } else {
                rz.fillContoursWithPaint(contours, Path2D.WIND_EVEN_ODD, fillPaint);
            }
        }

        if ((linePaint != null && lineStroke != null) || lineColor != null) {
            Stroke stroke = lineStroke == null ? defaultStroke : lineStroke;
            Shape shape = AntialiasTools.contoursToShape(contours, Path2D.WIND_EVEN_ODD, false);
            Shape strokedShape = stroke.createStrokedShape(shape);
            List<List<Vec2>> strokeContours = AntialiasTools.shapeToContours(strokedShape, 1.0);

            if (linePaint != null && lineStroke != null) {
                if (linePaint instanceof MultipleGradientPaint) {
                    lineTransform.preConcatenate(graphics.getTransform());
                    rz.fillContoursWithPaint(strokeContours, Path2D.WIND_NON_ZERO, linePaint, lineTransform);
                } else if (linePaint instanceof TexturePaint) {
                    lineTransform.preConcatenate(graphics.getTransform());
                    rz.fillContoursWithPaint(strokeContours, Path2D.WIND_NON_ZERO, linePaint, lineTransform);
                } else {
                    rz.fillContoursWithPaint(strokeContours, Path2D.WIND_NON_ZERO, linePaint);
                }
            } else if (lineColor != null) {
                rz.fillContoursWithPaint(strokeContours, Path2D.WIND_NON_ZERO, lineColor);
            }
        }

        contours.clear();
        lineStroke = null;
        lineColor = null;
        fillPaint = null;
    }
}
