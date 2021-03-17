package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class DualGraphics2D extends Graphics2D {

    private final Graphics2D first;

    private final Graphics2D second;

    public DualGraphics2D(Graphics2D first, Graphics2D second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void draw(Shape s) {
        first.draw(s);
        second.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean ok1 = first.drawImage(img, xform, obs);
        boolean ok2 = second.drawImage(img, xform, obs);
        return ok1 && ok2; //?
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        first.drawImage(img, op, x, y);
        second.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        first.drawRenderedImage(img, xform);
        second.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        first.drawRenderableImage(img, xform);
        second.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        first.drawString(str, x, y);
        second.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        first.drawString(str, x, y);
        second.drawString(str, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        first.drawString(iterator, x, y);
        second.drawString(iterator, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        first.drawString(iterator, x, y);
        second.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        first.drawGlyphVector(g, x, y);
        second.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        first.fill(s);
        second.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        boolean ok1 = first.hit(rect, s, onStroke);
        boolean ok2 = second.hit(rect, s, onStroke);
        return ok1 && ok2; //?
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return first.getDeviceConfiguration(); //?
    }

    @Override
    public void setComposite(Composite comp) {
        first.setComposite(comp);
        second.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        first.setPaint(paint);
        second.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        first.setStroke(s);
        second.setStroke(s);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        first.setRenderingHint(hintKey, hintValue);
        second.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return first.getRenderingHint(hintKey); //?
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        first.setRenderingHints(hints);
        second.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        first.addRenderingHints(hints);
        second.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return first.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        first.translate(x, y);
        second.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        first.translate(tx, ty);
        second.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        first.rotate(theta);
        second.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        first.rotate(theta, x, y);
        second.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        first.scale(sx, sy);
        second.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        first.shear(shx, shy);
        second.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        first.transform(Tx);
        second.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        first.setTransform(Tx);
        second.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return first.getTransform();
    }

    @Override
    public Paint getPaint() {
        return first.getPaint();
    }

    @Override
    public Composite getComposite() {
        return first.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        first.setBackground(color);
        second.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return first.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return first.getStroke();
    }

    @Override
    public void clip(Shape s) {
        first.clip(s);
        second.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return first.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return this; //?
    }

    @Override
    public Color getColor() {
        return first.getColor();
    }

    @Override
    public void setColor(Color c) {
        first.setColor(c);
        second.setColor(c);
    }

    @Override
    public void setPaintMode() {
        first.setPaintMode();
        second.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        first.setXORMode(c1);
        second.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return first.getFont();
    }

    @Override
    public void setFont(Font font) {
        first.setFont(font);
        second.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return first.getFontMetrics();
    }

    @Override
    public Rectangle getClipBounds() {
        return first.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        first.clearRect(x, y, width, height);
        second.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        first.setClip(x, y, width, height);
        second.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return first.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        first.setClip(clip);
        second.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        first.copyArea(x, y, width, height, dx, dy);
        second.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        first.drawLine(x1, y1, x2, y2);
        second.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        first.fillRect(x, y, width, height);
        second.fillRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        first.clearRect(x, y, width, height);
        second.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        first.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        second.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        first.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        second.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        first.drawOval(x, y, width, height);
        second.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        first.fillOval(x, y, width, height);
        second.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        first.drawArc(x, y, width, height, startAngle, arcAngle);
        second.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        first.fillArc(x, y, width, height, startAngle, arcAngle);
        second.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        first.drawPolyline(xPoints, yPoints, nPoints);
        second.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        first.drawPolyline(xPoints, yPoints, nPoints);
        second.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        first.fillPolygon(xPoints, yPoints, nPoints);
        second.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, x, y, observer);
        boolean ok2 = second.drawImage(img, x, y, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, x, y, width, height, observer);
        boolean ok2 = second.drawImage(img, x, y, width, height, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, x, y, bgcolor, observer);
        boolean ok2 = second.drawImage(img, x, y, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, x, y, width, height, bgcolor, observer);
        boolean ok2 = second.drawImage(img, x, y, width, height, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        boolean ok2 = second.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        boolean ok1 = first.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        boolean ok2 = second.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public void dispose() {
        first.dispose();
        second.dispose();
    }

}
