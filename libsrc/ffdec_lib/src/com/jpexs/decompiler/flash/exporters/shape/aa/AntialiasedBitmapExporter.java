package com.jpexs.decompiler.flash.exporters.shape.aa;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.graphics.ExtendedBasicStroke;
import com.jpexs.helpers.SerializableImage;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
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

    private AntialiasTools.SceneRasterizerMSAA rz;

    private Vec2 lastPos = null;

    private Matrix vecTrans;
    private Matrix vecTransLines;

    private ExportRectangle viewRect;

    private boolean inLines = false;

    private boolean closeLine = false;

    private double curveFlattness = 1.0;

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
        //double s = Math.max(transformation.scaleX,transformation.scaleY);

        RECT bounds = new RECT(shape.getBounds(shapeNum));

        int maxStrokeWidth = shape.getMaxStrokeWidth(shapeNum);

        bounds.Xmin -= maxStrokeWidth;
        bounds.Ymin -= maxStrokeWidth;
        bounds.Xmax += maxStrokeWidth;
        bounds.Ymax += maxStrokeWidth;

        vecTrans = transformation.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor));
        viewRect = vecTrans.transform(new ExportRectangle(bounds));

        /*viewRect.xMin -= 1;
        viewRect.yMin -= 1;
        viewRect.yMax += 1;
        viewRect.xMax += 1;*/
        viewRect.xMin = Math.floor(viewRect.xMin - unzoom - 0.5);
        viewRect.yMin = Math.floor(viewRect.yMin - unzoom - 0.5);
        viewRect.xMax = Math.ceil(viewRect.xMax + unzoom);
        viewRect.yMax = Math.ceil(viewRect.yMax + unzoom);

        int width = (int) viewRect.getWidth();
        int height = (int) viewRect.getHeight();
        vecTrans = vecTrans.preConcatenate(Matrix.getTranslateInstance(-viewRect.xMin, -viewRect.yMin));
        vecTransLines = vecTrans.preConcatenate(Matrix.getTranslateInstance(-0.5, -0.5));
        Graphics2D g = (Graphics2D) image.getGraphics();

        //g.setTransform(t);
        if (width <= 0 || height <= 0) {
            return;
        }

        curveFlattness = 1.0 / aaScale;

        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = (Graphics2D) image2.getGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, width, height);
        try {
            rz = new AntialiasTools.SceneRasterizerMSAA(width, height, aaScale);
        } catch (OutOfMemoryError er) {
            System.gc();
            return;
        }
        rz.clear(0x00000000);
        Shape clip = g.getClip();
        if (clip != null) {
            AffineTransform t = new AffineTransform(g.getTransform());
            t.preConcatenate(AffineTransform.getTranslateInstance(-viewRect.xMin, -viewRect.yMin));
            clip = t.createTransformedShape(clip);
            List<List<Vec2>> clipContours = AntialiasTools.shapeToContours(clip, curveFlattness);
            rz.setClipContours(clipContours, Path2D.WIND_EVEN_ODD);
        }

        this.image = image;
        this.scaleStrokes = scaleStrokes;

        graphics = (Graphics2D) image.getGraphics();
        setTransform(graphics, transformation);
        defaultStroke = graphics.getStroke();
        this.unzoom = unzoom;
        this.aaScale = 1; //aaScale;
        super.export();

        //super.exportTo(shapeNum, image, unzoom, transformation, strokeTransformation, scaleStrokes, aaScale);
        //rz.resolveToReplace(image2);
        rz.resolveTo(image2);

        g.setComposite(AlphaComposite.SrcOver);
        g.setTransform(new AffineTransform());
        g.drawImage(image2, (int) viewRect.xMin, (int) viewRect.yMin, null);

        /*
        g.setColor(new Color(0,0,0,128));
        g.setStroke(new BasicStroke(1));
        g.drawRect((int) viewRect.xMin, (int) viewRect.yMin, (int) viewRect.getWidth(), (int) viewRect.getHeight());
         */
    }

    @Override
    protected void setTransform(Graphics2D g, Matrix transformation) {
        AffineTransform at = transformation.toTransform();
        at.preConcatenate(AffineTransform.getScaleInstance(1 / SWF.unitDivisor, 1 / SWF.unitDivisor));
        at.preConcatenate(AffineTransform.getTranslateInstance(-viewRect.xMin, -viewRect.yMin));
        graphics.setTransform(at);
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
        inLines = true;
    }

    @Override
    public void endLines(boolean close) {
        closeLine = close;
        /*if (close) {            
            //path.closePath();
        }*/

        finalizePath();

        inLines = false;
    }

    @Override
    public void moveTo(double x, double y) {
        Point2D src = new Point2D.Double(x, y);
        Point2D dst = new Point2D.Double();
        dst = (inLines ? vecTransLines : vecTrans).transform(src);
        contours.add(new ArrayList<>());
        lastPos = new Vec2(dst.getX(), dst.getY());
        contours.get(contours.size() - 1).add(lastPos);
    }

    @Override
    public void lineTo(double x, double y) {
        Point2D src = new Point2D.Double(x, y);
        Point2D dst = new Point2D.Double();
        dst = (inLines ? vecTransLines : vecTrans).transform(src);

        lastPos = new Vec2(dst.getX(), dst.getY());
        contours.get(contours.size() - 1).add(lastPos);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        Point2D src = new Point2D.Double(controlX, controlY);
        Point2D dst = new Point2D.Double();
        dst = (inLines ? vecTransLines : vecTrans).transform(src);
        Vec2 controlVec2 = new Vec2(dst.getX(), dst.getY());
        src = new Point2D.Double(anchorX, anchorY);
        dst = (inLines ? vecTransLines : vecTrans).transform(src);
        Vec2 anchorVec2 = new Vec2(dst.getX(), dst.getY());

        List<Vec2> contour = contours.get(contours.size() - 1);
        AntialiasTools.append(contour, AntialiasTools.flattenQuadraticBezier(lastPos, controlVec2, anchorVec2, curveFlattness), true);
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

        /*int w = sx2 - sx;
        int h = sy2 - sy;
        
        int one = (int) (1 * unzoom);
        BufferedImage imgTrans = new BufferedImage(w + 2, h + 2, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = (Graphics2D) imgTrans.getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0,255,0,255));
        g.fillRect(0, 0, w + 2, h + 2);
        
        g.drawImage(image, 1, 1, 1 + w, 1 + h, sx,sy,sx2,sy2, null);
         */
        //t.concatenate(AffineTransform.getTranslateInstance(-1, -1));
        //t.preConcatenate(AffineTransform.getTranslateInstance(-1 * unzoom, -1 * unzoom));
        //new Rectangle2D.Double(sx, sy, sx2 - sx, sy2 - sy)
        //rz.fillContoursWithPaint(cs, windingRule, new TexturePaint(imgTrans, new Rectangle2D.Double(0,0,w+2,h+2)), t, smooth);
        //new TexturePaint(image, new Rectangle2D.Double(sx, sy, sx2 - sx, sy2 - sy))
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
            Shape shape = AntialiasTools.contoursToShape(contours, Path2D.WIND_EVEN_ODD, closeLine);
            Shape strokedShape = stroke.createStrokedShape(shape);
            List<List<Vec2>> strokeContours = AntialiasTools.shapeToContours(strokedShape, curveFlattness);

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

        thickness *= unzoom / SWF.unitDivisor;

        //always display minimum stroke of 1 pixel, no matter how zoomed it is
        if (thickness < 1) {
            thickness = 1;
        }

        if (joinStyle == BasicStroke.JOIN_MITER) {
            lineStroke = new ExtendedBasicStroke((float) thickness, capStyle, ExtendedBasicStroke.JOIN_MITER_CLIP, miterLimit);
        } else {
            lineStroke = new BasicStroke((float) thickness, capStyle, joinStyle);
        }
    }
}
