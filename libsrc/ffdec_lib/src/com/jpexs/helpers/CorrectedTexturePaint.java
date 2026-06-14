package com.jpexs.helpers;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Texture paint with better tiling.
 * @author JPEXS
 */
public final class CorrectedTexturePaint extends TexturePaint {
 
    public CorrectedTexturePaint(BufferedImage img, Rectangle2D anchor) {
        super(img, anchor);
    }

    @Override
    public PaintContext createContext(
            ColorModel cm,
            Rectangle deviceBounds,
            Rectangle2D userBounds,
            AffineTransform xform,
            RenderingHints hints) {

        Rectangle2D anchor = getAnchorRect();
        BufferedImage img = getImage();
        try {
            AffineTransform t = new AffineTransform(xform);
            t.translate(anchor.getX(), anchor.getY());
            t.scale(anchor.getWidth() / img.getWidth(),
                    anchor.getHeight() / img.getHeight());

            Object interpolation = hints == null
                    ? null
                    : hints.get(RenderingHints.KEY_INTERPOLATION);

            boolean bilinear
                    = RenderingHints.VALUE_INTERPOLATION_BILINEAR.equals(interpolation);

            return new Context(img, t.createInverse(), bilinear);
        } catch (NoninvertibleTransformException e) {
            throw new IllegalArgumentException(e);
        }
    } 

    private static final class Context implements PaintContext {

        private final BufferedImage img;
        private final AffineTransform inverse;
        private final ColorModel cm = ColorModel.getRGBdefault();
        private final boolean bilinear;

        Context(BufferedImage img, AffineTransform inverse, boolean bilinear) {
            this.img = img;
            this.inverse = inverse;
            this.bilinear = bilinear;
        }

        @Override
        public ColorModel getColorModel() {
            return cm;
        }

        @Override
        public Raster getRaster(int x, int y, int w, int h) {
            WritableRaster r = cm.createCompatibleWritableRaster(w, h);
            int[] pixel = new int[4];

            Point2D.Double src = new Point2D.Double();

            for (int yy = 0; yy < h; yy++) {
                for (int xx = 0; xx < w; xx++) {
                    src.x = x + xx + 0.5;
                    src.y = y + yy + 0.5;
                    inverse.transform(src, src);
                    src.x -= 0.5;
                    src.y -= 0.5;

                    int argb = bilinear
                            ? getRGBBilinear(src.x, src.y)
                            : getRGBNearest(src.x, src.y);

                    pixel[0] = (argb >>> 16) & 0xff;
                    pixel[1] = (argb >>> 8) & 0xff;
                    pixel[2] = argb & 0xff;
                    pixel[3] = (argb >>> 24) & 0xff;

                    r.setPixel(xx, yy, pixel);
                }
            }

            return r;
        }

        private int getRGBNearest(double x, double y) {
            int sx = Math.floorMod((int) Math.floor(x), img.getWidth());
            int sy = Math.floorMod((int) Math.floor(y), img.getHeight());
            return img.getRGB(sx, sy);
        }

        private int getRGBBilinear(double x, double y) {
            int w = img.getWidth();
            int h = img.getHeight();

            int x0 = (int) Math.floor(x);
            int y0 = (int) Math.floor(y);

            double fx = x - x0;
            double fy = y - y0;

            int ix0 = Math.floorMod(x0, w);
            int iy0 = Math.floorMod(y0, h);
            int ix1 = Math.floorMod(x0 + 1, w);
            int iy1 = Math.floorMod(y0 + 1, h);

            int c00 = img.getRGB(ix0, iy0);
            int c10 = img.getRGB(ix1, iy0);
            int c01 = img.getRGB(ix0, iy1);
            int c11 = img.getRGB(ix1, iy1);

            int a = bilerp((c00 >>> 24) & 0xff, (c10 >>> 24) & 0xff,
                    (c01 >>> 24) & 0xff, (c11 >>> 24) & 0xff, fx, fy);
            int r = bilerp((c00 >>> 16) & 0xff, (c10 >>> 16) & 0xff,
                    (c01 >>> 16) & 0xff, (c11 >>> 16) & 0xff, fx, fy);
            int g = bilerp((c00 >>> 8) & 0xff, (c10 >>> 8) & 0xff,
                    (c01 >>> 8) & 0xff, (c11 >>> 8) & 0xff, fx, fy);
            int b = bilerp(c00 & 0xff, c10 & 0xff,
                    c01 & 0xff, c11 & 0xff, fx, fy);

            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private static int bilerp(
                int c00, int c10,
                int c01, int c11,
                double fx, double fy) {

            double top = c00 + fx * (c10 - c00);
            double bottom = c01 + fx * (c11 - c01);
            return clamp((int) Math.round(top + fy * (bottom - top)));
        }

        private static int clamp(int v) {
            return v < 0 ? 0 : v > 255 ? 255 : v;
        }

        @Override
        public void dispose() {
        }
    }
}
