/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.tags.base.HasSeparateAlphaChannel;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.helpers.Helper;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Vector;

/**
 * BufferedImage that has link to ImageTag. This is mainly used for PDF export
 * coupling.
 *
 * @author JPEXS
 */
public class ImageTagBufferedImage extends BufferedImage {

    private byte[] imageData;
    private ImageTag tag;
    private final BufferedImage image;

    public ImageTagBufferedImage(ImageTag tag, BufferedImage image) {
        super(1, 1, TYPE_INT_RGB);
        this.image = image;
        this.tag = tag;
    }

    public int getImageId() {
        return tag.getCharacterId();
    }

    public boolean isJpeg() {
        return tag.getOriginalImageFormat() == ImageFormat.JPEG;
    }

    public byte[] getAlphaChannel() {
        if (tag instanceof HasSeparateAlphaChannel) {
            HasSeparateAlphaChannel hsac = (HasSeparateAlphaChannel) tag;
            try {
                return hsac.getImageAlpha();
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    public byte[] getImageData() {
        if (imageData != null) {
            return imageData;
        }
        imageData = Helper.readStream(tag.getOriginalImageData());
        return imageData;
    }

    @Override
    public int getTransparency() {
        return image.getTransparency();
    }

    @Override
    public void releaseWritableTile(int tileX, int tileY) {
        image.releaseWritableTile(tileX, tileY);
    }

    @Override
    public WritableRaster getWritableTile(int tileX, int tileY) {
        return image.getWritableTile(tileX, tileY);
    }

    @Override
    public boolean hasTileWriters() {
        return image.hasTileWriters();
    }

    @Override
    public Point[] getWritableTileIndices() {
        return image.getWritableTileIndices();
    }

    @Override
    public boolean isTileWritable(int tileX, int tileY) {
        return image.isTileWritable(tileX, tileY);
    }

    @Override
    public void removeTileObserver(TileObserver to) {
        image.removeTileObserver(to);
    }

    @Override
    public void addTileObserver(TileObserver to) {
        image.addTileObserver(to);
    }

    @Override
    public void setData(Raster r) {
        image.setData(r);
    }

    @Override
    public WritableRaster copyData(WritableRaster outRaster) {
        return image.copyData(outRaster);
    }

    @Override
    public Raster getData(Rectangle rect) {
        return image.getData(rect);
    }

    @Override
    public Raster getData() {
        return image.getData();
    }

    @Override
    public Raster getTile(int tileX, int tileY) {
        return image.getTile(tileX, tileY);
    }

    @Override
    public int getTileGridYOffset() {
        return image.getTileGridXOffset();
    }

    @Override
    public int getTileGridXOffset() {
        return image.getTileGridXOffset();
    }

    @Override
    public int getTileHeight() {
        return image.getTileHeight();
    }

    @Override
    public int getTileWidth() {
        return image.getTileWidth();
    }

    @Override
    public int getMinTileY() {
        return image.getMinTileY();
    }

    @Override
    public int getMinTileX() {
        return image.getMinTileX();
    }

    @Override
    public int getNumYTiles() {
        return image.getNumYTiles();
    }

    @Override
    public int getNumXTiles() {
        return image.getNumXTiles();
    }

    @Override
    public SampleModel getSampleModel() {
        return image.getSampleModel();
    }

    @Override
    public int getMinY() {
        return image.getMinY();
    }

    @Override
    public int getMinX() {
        return image.getMinX();
    }

    @Override
    public String[] getPropertyNames() {
        return image.getPropertyNames();
    }

    @Override
    public Vector<RenderedImage> getSources() {
        return image.getSources();
    }

    @Override
    public String toString() {
        return image.toString();
    }

    @Override
    public void coerceData(boolean isAlphaPremultiplied) {
        image.coerceData(isAlphaPremultiplied);
    }

    @Override
    public boolean isAlphaPremultiplied() {
        return image.isAlphaPremultiplied();
    }

    @Override
    public BufferedImage getSubimage(int x, int y, int w, int h) {
        //?? FIXME ??
        return image.getSubimage(x, y, w, h);
    }

    @Override
    public Graphics2D createGraphics() {
        return image.createGraphics();
    }

    @Override
    public Graphics getGraphics() {
        return image.getGraphics();
    }

    @Override
    public Object getProperty(String name) {
        return image.getProperty(name);
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
        return image.getProperty(name, observer);
    }

    @Override
    public ImageProducer getSource() {
        return image.getSource();
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return image.getWidth(observer);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return image.getHeight(observer);
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    @Override
    public void setRGB(int x, int y, int rgb) {
        image.setRGB(x, y, rgb);
    }

    @Override
    public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        return image.getRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    @Override
    public int getRGB(int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public WritableRaster getAlphaRaster() {
        return image.getAlphaRaster();
    }

    @Override
    public WritableRaster getRaster() {
        return image.getRaster();
    }

    @Override
    public ColorModel getColorModel() {
        return image.getColorModel();
    }

    @Override
    public int getType() {
        return image.getType();
    }

    @Override
    public float getAccelerationPriority() {
        return image.getAccelerationPriority();
    }

    @Override
    public void setAccelerationPriority(float priority) {
        image.setAccelerationPriority(priority);
    }

    @Override
    public ImageCapabilities getCapabilities(GraphicsConfiguration gc) {
        return image.getCapabilities(gc);
    }

    @Override
    public void flush() {
        image.flush();
    }

    @Override
    public Image getScaledInstance(int width, int height, int hints) {
        return image.getScaledInstance(width, height, hints);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImageTagBufferedImage) {
            return tag.getCharacterId() == ((ImageTagBufferedImage) obj).tag.getCharacterId();
        }
        return image.equals(obj);
    }

    @Override
    public int hashCode() {
        return tag.getCharacterId();
    }
}
