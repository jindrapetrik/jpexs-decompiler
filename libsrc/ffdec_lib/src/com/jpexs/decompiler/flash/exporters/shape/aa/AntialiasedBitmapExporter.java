package com.jpexs.decompiler.flash.exporters.shape.aa;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
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

    private final List<List<Vec2>> contours = new ArrayList<>();

    private final List<List<StrokePoint>> strokePaths = new ArrayList<>();

    private AntialiasTools.SceneRasterizerMSAA rz;

    private Vec2 lastPos = null;

    private Matrix vecTrans;

    private boolean inLines = false;

    private boolean closeLine = false;

    private boolean pixelHinting;

    private double alignedStrokeWidth;

    private double curveFlatness = 1.0;

    private static final int BASE_TILE_SIZE = 4096;

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
        super(windingRule, shapeNum, swf, shape, defaultColor, colorTransform);
    }

    @Override
    protected void exportTo(int shapeNum, SerializableImage image, double unzoom, Matrix transformation, Matrix strokeTransformation, boolean scaleStrokes, int aaScale) {
        this.strokeTransformation = strokeTransformation;
        calculateThicknessScale();

        RECT bounds = new RECT(shape.getBounds(shapeNum));

        int maxStrokeWidth = shape.getMaxStrokeWidth(shapeNum);

        bounds.Xmin -= maxStrokeWidth;
        bounds.Ymin -= maxStrokeWidth;
        bounds.Xmax += maxStrokeWidth;
        bounds.Ymax += maxStrokeWidth;

        vecTrans = transformation.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor));
        ExportRectangle shapeViewRect = vecTrans.transform(new ExportRectangle(bounds));

        if (shapeViewRect.xMax > image.getWidth()) {
            shapeViewRect.xMax = image.getWidth();
        }

        if (shapeViewRect.yMax > image.getHeight()) {
            shapeViewRect.yMax = image.getHeight();
        }

        if (shapeViewRect.xMin < 0) {
            shapeViewRect.xMin = 0;
        }

        if (shapeViewRect.yMin < 0) {
            shapeViewRect.yMin = 0;
        }

        shapeViewRect.xMin = Math.floor(shapeViewRect.xMin - unzoom - 0.5);
        shapeViewRect.yMin = Math.floor(shapeViewRect.yMin - unzoom - 0.5);
        shapeViewRect.xMax = Math.ceil(shapeViewRect.xMax + unzoom);
        shapeViewRect.yMax = Math.ceil(shapeViewRect.yMax + unzoom);

        int width = (int) shapeViewRect.getWidth();
        int height = (int) shapeViewRect.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        int tileSize = BASE_TILE_SIZE / aaScale;

        int cx = (int) Math.ceil(width / (double) tileSize);
        int cy = (int) Math.ceil(height / (double) tileSize);

        vecTrans = vecTrans.preConcatenate(Matrix.getTranslateInstance(-shapeViewRect.xMin, -shapeViewRect.yMin));

        curveFlatness = 1.0 / aaScale;
        this.image = image;
        this.scaleStrokes = scaleStrokes;
        graphics = (Graphics2D) image.getGraphics();

        Matrix vecTransBase = vecTrans;

        for (int y = 0; y < cy; y++) {
            for (int x = 0; x < cx; x++) {
                vecTrans = vecTransBase.preConcatenate(Matrix.getTranslateInstance(-tileSize * x, -tileSize * y));

                int subWidth = tileSize;
                int subHeight = tileSize;

                if (x * tileSize + tileSize > width) {
                    subWidth = width - x * tileSize;
                }
                if (y * tileSize + tileSize > height) {
                    subHeight = height - y * tileSize;
                }

                BufferedImage image2 = new BufferedImage(subWidth, subHeight, BufferedImage.TYPE_INT_ARGB_PRE);
                Graphics2D g2 = (Graphics2D) image2.getGraphics();
                g2.setComposite(AlphaComposite.Src);
                g2.setColor(new Color(0, 0, 0, 0));
                g2.fillRect(0, 0, subWidth, subHeight);
                try {
                    rz = new AntialiasTools.SceneRasterizerMSAA(subWidth, subHeight, aaScale);
                } catch (OutOfMemoryError er) {
                    System.gc();
                    return;
                }
                rz.clear(0x00000000);
                Shape clip = graphics.getClip();
                if (clip != null) {
                    AffineTransform t = new AffineTransform(graphics.getTransform());
                    t.preConcatenate(AffineTransform.getTranslateInstance(-shapeViewRect.xMin - tileSize * x, -shapeViewRect.yMin - tileSize * y));
                    clip = t.createTransformedShape(clip);
                    List<List<Vec2>> clipContours = AntialiasTools.shapeToContours(clip, curveFlatness);
                    rz.setClipContours(clipContours, Path2D.WIND_EVEN_ODD);
                }

                AffineTransform at = transformation.toTransform();
                at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
                at.preConcatenate(AffineTransform.getTranslateInstance(-shapeViewRect.xMin - tileSize * x, -shapeViewRect.yMin - tileSize * y));
                graphics.setTransform(at);

                defaultStroke = graphics.getStroke();
                this.unzoom = unzoom;
                this.aaScale = 1; //aaScale;
                super.export();

                rz.resolveTo(image2);

                graphics.setComposite(AlphaComposite.SrcOver);
                graphics.setTransform(new AffineTransform());
                graphics.drawImage(image2, (int) shapeViewRect.xMin + tileSize * x, (int) shapeViewRect.yMin + tileSize * y, null);
            }
        }

        /*
        g.setColor(new Color(0,0,0,128));
        g.setStroke(new BasicStroke(1));
        g.drawRect((int) viewRect.xMin, (int) viewRect.yMin, (int) viewRect.getWidth(), (int) viewRect.getHeight());
         */
    }

    /**
     * Returns the image.
     *
     * @return Image
     */
    @Override
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
        inLines = true;
    }

    @Override
    public void endLines(boolean close) {
        closeLine = close;
        finalizePath();
        inLines = false;
    }

    private Vec2 transformPathPoint(double x, double y) {
        com.jpexs.decompiler.flash.exporters.commonshape.Point point = vecTrans.transform(x, y);
        return new Vec2(point.x, point.y);
    }

    @Override
    public void moveTo(double x, double y) {
        if (inLines) {
            List<StrokePoint> points = new ArrayList<>();
            Vec2 anchor = transformPathPoint(x, y);
            points.add(new StrokePoint(PathIterator.SEG_MOVETO, anchor.x, anchor.y));
            strokePaths.add(points);
        } else {
            contours.add(new ArrayList<>());
            lineTo(x, y);
        }
    }

    @Override
    public void lineTo(double x, double y) {
        Vec2 anchor = transformPathPoint(x, y);
        if (inLines) {
            strokePaths.get(strokePaths.size() - 1).add(new StrokePoint(PathIterator.SEG_LINETO, anchor.x, anchor.y));
        } else {
            contours.get(contours.size() - 1).add(anchor);
            lastPos = anchor;
        }
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        Vec2 control = transformPathPoint(controlX, controlY);
        Vec2 anchor = transformPathPoint(anchorX, anchorY);
        if (inLines) {
            strokePaths.get(strokePaths.size() - 1).add(new StrokePoint(PathIterator.SEG_QUADTO, control.x, control.y, anchor.x, anchor.y));
        } else {
            List<Vec2> contour = contours.get(contours.size() - 1);
            AntialiasTools.append(contour, AntialiasTools.flattenQuadraticBezier(lastPos, control, anchor, curveFlatness), true);
            lastPos = anchor;
        }
    }

    private Shape createStrokePath() {
        // Keep Bézier tangents for stroke joins and caps. Flatten the outline for MSAA.
        Path2D.Double path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        for (int i = 0; i < strokePaths.size(); i++) {
            appendStrokeSubpath(path, strokePaths.get(i), closeLine && i == strokePaths.size() - 1,
                    alignedStrokeWidth, pixelHinting);
        }
        return path;
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

        for (int i = 0; i < c.size(); i++) {
            double x = Math.round(c.get(i).x);
            double y = Math.round(c.get(i).y);
            c.set(i, new Vec2(x, y));
        }

        cs.add(c);

        AffineTransform t = new AffineTransform();
        if (dx2 != dx && dy2 != dy) {
            double scaleX = (sx2 - sx) / (double) (dx2 - dx);
            double scaleY = (sy2 - sy) / (double) (dy2 - dy);

            t = AffineTransform.getScaleInstance(scaleX, scaleY);
            t.preConcatenate(fillTransform);
        }

        rz.fillContoursWithPaint(cs, windingRule, Color.black, t, smooth, image);
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
                    rz.fillContoursWithPaint(contours, Path2D.WIND_EVEN_ODD, fillPaint, fillTransform, fillSmooth, null);
                } else {
                    drawImage(((TexturePaint) fillPaint).getImage(), 0, 0, null, fillSmooth);
                }
            } else {
                rz.fillContoursWithPaint(contours, Path2D.WIND_EVEN_ODD, fillPaint);
            }
        }

        if ((linePaint != null && lineStroke != null) || lineColor != null) {
            Stroke stroke = lineStroke == null ? defaultStroke : lineStroke;
            Shape shape = createStrokePath();
            Shape strokedShape = stroke.createStrokedShape(shape);
            List<List<Vec2>> strokeContours = AntialiasTools.shapeToContours(strokedShape, curveFlatness);

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
        strokePaths.clear();
        closeLine = false;
        lineStroke = null;
        lineColor = null;
        fillPaint = null;
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
        linePaint = null;
        lineTransform = null;
        this.pixelHinting = pixelHinting;

        boolean hairline = thickness == 0;
        lineColor = color == null ? null : color.toColor();
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

        thickness *= unzoom / SWF.unitDivisor;

        if (hairline || Configuration.useMinimumStrokeWidth1Px.get()) {
            thickness = Math.max(1, thickness);
        }

        alignedStrokeWidth = getAlignedStrokeWidth(thickness, pixelHinting);
        if (alignedStrokeWidth > 0) {
            thickness = alignedStrokeWidth;
        }

        lineStroke = createLineStroke((float) thickness, startCaps, endCaps, joints, miterLimit, noClose);
    }
}
