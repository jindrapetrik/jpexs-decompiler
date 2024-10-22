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
package com.jpexs.helpers;

import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Serializable image.
 *
 * @author JPEXS
 */
public class SerializableImage implements Serializable {

    public static int TYPE_INT_ARGB = BufferedImage.TYPE_INT_ARGB;

    public static int TYPE_INT_RGB = BufferedImage.TYPE_INT_RGB;

    public static int TYPE_INT_ARGB_PRE = BufferedImage.TYPE_INT_ARGB_PRE;

    public static int TYPE_4BYTE_ABGR = BufferedImage.TYPE_4BYTE_ABGR;

    private BufferedImage image;

    private transient Graphics graphics;

    private SerializableImage() {
    }

    public SerializableImage(BufferedImage image) {
        this.image = image;
    }

    public SerializableImage(int width, int height, int imageType) {
        image = new BufferedImage(width, height, imageType);
    }

    public SerializableImage(int width, int height, int imageType, int[] pixels) {
        if (imageType != BufferedImage.TYPE_INT_ARGB_PRE && imageType != BufferedImage.TYPE_INT_RGB) {
            throw new Error("Unsuppported image type: " + imageType);
        }

        image = new BufferedImage(width, height, imageType);
        image.getRaster().setDataElements(0, 0, width, height, pixels);
    }

    public SerializableImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
        image = new BufferedImage(cm, raster, isRasterPremultiplied, properties);
    }

    public SerializableImage(int width, int height, int imageType, IndexColorModel cm) {
        image = new BufferedImage(width, height, imageType, cm);
    }

    public BufferedImage getBufferedImage() {
        return image;
    }

    public BufferedImage getCompatibleBufferedImage() {
        BufferedImage img = getBufferedImage();

        if (GraphicsEnvironment.isHeadless()) { //No GUI, no compatible image
            return img;
        }
        GraphicsConfiguration conf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        if (img.getColorModel().equals(conf.getColorModel())) {
            return img;
        }
        BufferedImage img2 = conf.createCompatibleImage(img.getWidth(), img.getHeight(), img.getTransparency());
        Graphics2D g2d = img2.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        return img2;
    }

    public void fillTransparent() {
        // Make all pixels transparent
        Graphics2D g = (Graphics2D) getGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0, 0, 0, 0f));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        SerializableImage retImage = new SerializableImage();
        retImage.image = image;
        return retImage;
    }

    public Graphics getGraphics() {
        //One graphics rule them all
        if (graphics != null) {
            return graphics;
        }
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return graphics = g;
    }

    public int getType() {
        return image.getType();
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getRGB(int i, int i1) {
        return image.getRGB(i, i1);
    }

    public synchronized void setRGB(int i, int i1, int i2) {
        image.setRGB(i, i1, i2);
    }

    public ColorModel getColorModel() {
        return image.getColorModel();
    }

    public WritableRaster getRaster() {
        return image.getRaster();
    }

    @Override
    public String toString() {
        return image.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            ImageHelper.write(image, ImageFormat.PNG, out);
        } catch (Exception ex) {
            // ignore
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        image = ImageHelper.read(in);
    }
}
