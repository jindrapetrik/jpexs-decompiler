/*
 *  Copyright (C) 2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.helpers;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.ImageCapabilities;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class SerializableImage implements Serializable {

    public static int TYPE_INT_ARGB = BufferedImage.TYPE_INT_ARGB;
    public static int TYPE_INT_RGB = BufferedImage.TYPE_INT_RGB;
    public static int TYPE_INT_ARGB_PRE = BufferedImage.TYPE_INT_ARGB_PRE;
    public static int TYPE_4BYTE_ABGR = BufferedImage.TYPE_4BYTE_ABGR;

    private BufferedImage image;

    public SerializableImage() {
    }

    public SerializableImage(BufferedImage image) {
        this.image = image;
    }
    
    public SerializableImage(int i, int i1, int i2) {
        image = new BufferedImage(i, i1, i2);
    }

    public SerializableImage(ColorModel cm, WritableRaster wr, boolean bln, Hashtable<?, ?> hshtbl) {
        image = new BufferedImage(cm, wr, bln, hshtbl);
    }

    public SerializableImage(int i, int i1, int i2, IndexColorModel icm) {
        image = new BufferedImage(i, i1, i2, icm);
    }

    public BufferedImage getBufferedImage() {
        return image;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SerializableImage image = new SerializableImage();
        image.image = this.image;
        return image;
    }

    public Graphics getGraphics() {
        return image.getGraphics();
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

    public int[] getRGB(int i, int i1, int i2, int i3, int[] ints, int i4, int i5) {
        return image.getRGB(i, i1, i2, i3, ints, i4, i5);
    }

    public synchronized void setRGB(int i, int i1, int i2) {
        image.setRGB(i, i1, i2);
    }

    public void setRGB(int i, int i1, int i2, int i3, int[] ints, int i4, int i5) {
        image.setRGB(i, i1, i2, i3, ints, i4, i5);
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
        ImageIO.write(image, "png", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        image = ImageIO.read(in);
    }
}
