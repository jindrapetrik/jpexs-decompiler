package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.DynamicTextGlyphEntry;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import gnu.jpdf.PDFGraphics;
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
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class DualPdfGraphics2D extends Graphics2D implements BlendModeSetable, GraphicsGroupable, GraphicsTextDrawable {

    private final Graphics2D imageGraphics;

    private final PDFGraphics pdfGraphics;
    private final Map<Integer, Font> existingFonts;

    public DualPdfGraphics2D(Graphics2D first, PDFGraphics second, Map<Integer, Font> existingFonts) {
        this.imageGraphics = first;
        this.pdfGraphics = second;
        this.existingFonts = existingFonts;
    }

    @Override
    public void draw(Shape s) {
        imageGraphics.draw(s);
        pdfGraphics.draw(s);
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        boolean ok1 = imageGraphics.drawImage(img, xform, obs);
        boolean ok2 = pdfGraphics.drawImage(img, xform, obs);
        return ok1 && ok2; //?
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        imageGraphics.drawImage(img, op, x, y);
        pdfGraphics.drawImage(img, op, x, y);
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        imageGraphics.drawRenderedImage(img, xform);
        pdfGraphics.drawRenderedImage(img, xform);
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        imageGraphics.drawRenderableImage(img, xform);
        pdfGraphics.drawRenderableImage(img, xform);
    }

    @Override
    public void drawString(String str, int x, int y) {
        imageGraphics.drawString(str, x, y);
        pdfGraphics.drawString(str, x, y);
    }

    @Override
    public void drawString(String str, float x, float y) {
        imageGraphics.drawString(str, x, y);
        pdfGraphics.drawString(str, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        imageGraphics.drawString(iterator, x, y);
        pdfGraphics.drawString(iterator, x, y);
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        imageGraphics.drawString(iterator, x, y);
        pdfGraphics.drawString(iterator, x, y);
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        imageGraphics.drawGlyphVector(g, x, y);
        pdfGraphics.drawGlyphVector(g, x, y);
    }

    @Override
    public void fill(Shape s) {
        imageGraphics.fill(s);
        pdfGraphics.fill(s);
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        boolean ok1 = imageGraphics.hit(rect, s, onStroke);
        boolean ok2 = pdfGraphics.hit(rect, s, onStroke);
        return ok1 && ok2; //?
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return imageGraphics.getDeviceConfiguration(); //?
    }

    @Override
    public void setComposite(Composite comp) {
        imageGraphics.setComposite(comp);
        pdfGraphics.setComposite(comp);
    }

    @Override
    public void setPaint(Paint paint) {
        imageGraphics.setPaint(paint);
        pdfGraphics.setPaint(paint);
    }

    @Override
    public void setStroke(Stroke s) {
        imageGraphics.setStroke(s);
        pdfGraphics.setStroke(s);
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        imageGraphics.setRenderingHint(hintKey, hintValue);
        pdfGraphics.setRenderingHint(hintKey, hintValue);
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return imageGraphics.getRenderingHint(hintKey); //?
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        imageGraphics.setRenderingHints(hints);
        pdfGraphics.setRenderingHints(hints);
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        imageGraphics.addRenderingHints(hints);
        pdfGraphics.addRenderingHints(hints);
    }

    @Override
    public RenderingHints getRenderingHints() {
        return imageGraphics.getRenderingHints();
    }

    @Override
    public void translate(int x, int y) {
        imageGraphics.translate(x, y);
        pdfGraphics.translate(x, y);
    }

    @Override
    public void translate(double tx, double ty) {
        imageGraphics.translate(tx, ty);
        pdfGraphics.translate(tx, ty);
    }

    @Override
    public void rotate(double theta) {
        imageGraphics.rotate(theta);
        pdfGraphics.rotate(theta);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        imageGraphics.rotate(theta, x, y);
        pdfGraphics.rotate(theta, x, y);
    }

    @Override
    public void scale(double sx, double sy) {
        imageGraphics.scale(sx, sy);
        pdfGraphics.scale(sx, sy);
    }

    @Override
    public void shear(double shx, double shy) {
        imageGraphics.shear(shx, shy);
        pdfGraphics.shear(shx, shy);
    }

    @Override
    public void transform(AffineTransform Tx) {
        imageGraphics.transform(Tx);
        pdfGraphics.transform(Tx);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        imageGraphics.setTransform(Tx);
        pdfGraphics.setTransform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        return pdfGraphics.getTransform();
    }

    @Override
    public Paint getPaint() {
        return imageGraphics.getPaint();
    }

    @Override
    public Composite getComposite() {
        return imageGraphics.getComposite();
    }

    @Override
    public void setBackground(Color color) {
        imageGraphics.setBackground(color);
        pdfGraphics.setBackground(color);
    }

    @Override
    public Color getBackground() {
        return imageGraphics.getBackground();
    }

    @Override
    public Stroke getStroke() {
        return imageGraphics.getStroke();
    }

    @Override
    public void clip(Shape s) {
        imageGraphics.clip(s);
        pdfGraphics.clip(s);
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return imageGraphics.getFontRenderContext();
    }

    @Override
    public Graphics create() {
        return this; //?
    }

    @Override
    public Color getColor() {
        return imageGraphics.getColor();
    }

    @Override
    public void setColor(Color c) {
        imageGraphics.setColor(c);
        pdfGraphics.setColor(c);
    }

    @Override
    public void setPaintMode() {
        imageGraphics.setPaintMode();
        pdfGraphics.setPaintMode();
    }

    @Override
    public void setXORMode(Color c1) {
        imageGraphics.setXORMode(c1);
        pdfGraphics.setXORMode(c1);
    }

    @Override
    public Font getFont() {
        return imageGraphics.getFont();
    }

    @Override
    public void setFont(Font font) {
        imageGraphics.setFont(font);
        pdfGraphics.setFont(font);
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        return imageGraphics.getFontMetrics();
    }

    @Override
    public Rectangle getClipBounds() {
        return imageGraphics.getClipBounds();
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        imageGraphics.clearRect(x, y, width, height);
        pdfGraphics.clipRect(x, y, width, height);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        imageGraphics.setClip(x, y, width, height);
        pdfGraphics.setClip(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        return pdfGraphics.getClip();
    }

    @Override
    public void setClip(Shape clip) {
        imageGraphics.setClip(clip);
        pdfGraphics.setClip(clip);
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        imageGraphics.copyArea(x, y, width, height, dx, dy);
        pdfGraphics.copyArea(x, y, width, height, dx, dy);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        imageGraphics.drawLine(x1, y1, x2, y2);
        pdfGraphics.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        imageGraphics.fillRect(x, y, width, height);
        pdfGraphics.fillRect(x, y, width, height);
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        imageGraphics.clearRect(x, y, width, height);
        pdfGraphics.clearRect(x, y, width, height);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        imageGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        pdfGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        imageGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        pdfGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        imageGraphics.drawOval(x, y, width, height);
        pdfGraphics.drawOval(x, y, width, height);
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        imageGraphics.fillOval(x, y, width, height);
        pdfGraphics.fillOval(x, y, width, height);
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        imageGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
        pdfGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        imageGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
        pdfGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        imageGraphics.drawPolyline(xPoints, yPoints, nPoints);
        pdfGraphics.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        imageGraphics.drawPolyline(xPoints, yPoints, nPoints);
        pdfGraphics.drawPolyline(xPoints, yPoints, nPoints);
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        imageGraphics.fillPolygon(xPoints, yPoints, nPoints);
        pdfGraphics.fillPolygon(xPoints, yPoints, nPoints);
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, x, y, observer);
        boolean ok2 = pdfGraphics.drawImage(img, x, y, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, x, y, width, height, observer);
        boolean ok2 = pdfGraphics.drawImage(img, x, y, width, height, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, x, y, bgcolor, observer);
        boolean ok2 = pdfGraphics.drawImage(img, x, y, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
        boolean ok2 = pdfGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        boolean ok2 = pdfGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
        return ok1 && ok2;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        boolean ok1 = imageGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        boolean ok2 = pdfGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
        return ok1 && ok2;
    }

    @Override
    public void dispose() {
        imageGraphics.dispose();
        pdfGraphics.dispose();
    }

    @Override
    public void setBlendMode(int mode) {
        switch (mode) {
            case 0:
            case 1:
                pdfGraphics.setBlendMode("Normal");
                break;
            case 2:
                //Layer
                pdfGraphics.setBlendMode("Normal");
                break;
            case 3:
                pdfGraphics.setBlendMode("Multiply");
                break;
            case 4:
                pdfGraphics.setBlendMode("Screen");
                break;
            case 5:
                pdfGraphics.setBlendMode("Lighten");
                break;
            case 6:
                pdfGraphics.setBlendMode("Darken");
                break;
            case 7:
                pdfGraphics.setBlendMode("Difference");
                break;
            case 8:
                //Add
                pdfGraphics.setBlendMode("Normal");
                break;
            case 9:
                //Subtract
                pdfGraphics.setBlendMode("Normal");
                break;
            case 10:
                //Invert
                pdfGraphics.setBlendMode("Normal");
                break;
            case 11:
                //Alpha
                pdfGraphics.setBlendMode("Normal");
                break;
            case 12:
                //Erase
                pdfGraphics.setBlendMode("Normal");
                break;
            case 13:
                pdfGraphics.setBlendMode("Overlay");
                break;
            case 14:
                pdfGraphics.setBlendMode("HardLight");
                break;
            default: // Not implemented
                pdfGraphics.setBlendMode("Normal");
                break;
        }
    }

    @Override
    public Graphics createGroup() {
        return pdfGraphics.createXObject();
    }

    @Override
    public void drawGroup(Graphics g) {
        pdfGraphics.drawXObject(g);
    }

    @Override
    public void drawTextRecords(SWF swf, List<TEXTRECORD> textRecords, int numText, MATRIX textMatrixM, Matrix transformation, ColorTransform colorTransform) {
        Matrix textMatrix = new Matrix(textMatrixM);
        Matrix mat = transformation.clone();
        Matrix mat0 = mat.concatenate(textMatrix);
        Matrix trans = mat0.preConcatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor));
        FontTag font = null;
        int textHeight = 12;
        int x = 0;
        int y = 0;
        int textColor = 0;
        for (TEXTRECORD rec : textRecords) {

            if (rec.styleFlagsHasColor) {
                if (numText > 1) {
                    textColor = rec.textColorA.toInt();
                } else {
                    textColor = rec.textColor.toInt();
                }

                if (colorTransform != null) {
                    textColor = colorTransform.apply(textColor);
                }
            }

            if (rec.styleFlagsHasFont) {
                font = swf.getFont(rec.fontId);
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasXOffset) {
                int offsetX = rec.xOffset;
                x = offsetX;
            }
            if (rec.styleFlagsHasYOffset) {
                int offsetY = rec.yOffset;
                y = offsetY;
            }

            if (font == null) {
                continue;
            }

            StringBuilder text = new StringBuilder();
            int deltaX = 0;
            setColor(Color.green);
            for (int i = 0; i < rec.glyphEntries.size(); i++) {
                GLYPHENTRY entry = rec.glyphEntries.get(i);
                GLYPHENTRY nextEntry = i < rec.glyphEntries.size() - 1 ? rec.glyphEntries.get(i + 1) : null;
                if (entry.glyphIndex != -1) {
                    Character currentChar = font.glyphToChar(entry.glyphIndex);
                    Character nextChar = nextEntry == null ? null : font.glyphToChar(nextEntry.glyphIndex);

                    int calcAdvance = StaticTextTag.getAdvance(font, entry.glyphIndex, textHeight, currentChar, nextChar);
                    int spacing = entry.glyphAdvance - calcAdvance;
                    char ch = font.glyphToChar(entry.glyphIndex);
                    if (spacing != 0) {
                        if (text.length() > 0) {
                            drawText(x, y, trans, textColor, existingFonts, font, text.toString(), textHeight, pdfGraphics);
                        }
                        drawText(x + deltaX, y, trans, textColor, existingFonts, font, "" + currentChar, textHeight, pdfGraphics);

                        text = new StringBuilder();
                        x = x + deltaX + entry.glyphAdvance;
                        deltaX = 0;
                    } else {
                        text.append(ch);
                        deltaX += entry.glyphAdvance;
                    }

                } else if (entry instanceof DynamicTextGlyphEntry) {
                    DynamicTextGlyphEntry dynamicEntry = (DynamicTextGlyphEntry) entry;
                    text.append(dynamicEntry.character);
                    deltaX += entry.glyphAdvance;
                }
            }
            if (text.length() > 0) {
                drawText(x, y, trans, textColor, existingFonts, font, text.toString(), textHeight, pdfGraphics);
            }
            x = x + deltaX;
        }
    }

    private static void drawText(float x, float y, Matrix trans, int textColor, Map<Integer, Font> existingFonts, FontTag font, String text, int textHeight, PDFGraphics g) {
        int fontId = font.getFontId();
        if (existingFonts.containsKey(fontId)) {
            g.setExistingTtfFont(existingFonts.get(fontId).deriveFont((float) textHeight));
        } else {
            if (font.getCharacterCount() < 1) {
                String fontName = font.getFontName();
                File fontFile = FontTag.fontNameToFile(fontName);
                if (fontFile == null) {
                    fontFile = FontTag.fontNameToFile("Times New Roman");
                }
                if (fontFile == null) {
                    fontFile = FontTag.fontNameToFile("Arial");
                }
                if (fontFile == null) {
                    throw new RuntimeException("Font " + fontName + " not found in your system");
                }
                Font f = new Font("/MYFONT" + fontId, font.getFontStyle(), textHeight);
                existingFonts.put(fontId, f);
                try {
                    g.setTtfFont(f, fontFile);
                } catch (IOException ex) {
                    Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                FontExporter fe = new FontExporter();
                File tempFile;
                try {
                    tempFile = File.createTempFile("ffdec_font_export_", ".ttf");
                    fe.exportFont(font, FontExportMode.TTF, tempFile);
                    Font f = new Font("/MYFONT" + fontId, font.getFontStyle(), textHeight);
                    existingFonts.put(fontId, f);
                    g.setTtfFont(f, tempFile);
                } catch (IOException ex) {
                    Logger.getLogger(FrameExporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        g.setTransform(trans.toTransform());
        Color textColor2 = new Color(textColor, true);
        g.setColor(textColor2);
        g.drawString(text, (float) x, (float) y);
    }
}
