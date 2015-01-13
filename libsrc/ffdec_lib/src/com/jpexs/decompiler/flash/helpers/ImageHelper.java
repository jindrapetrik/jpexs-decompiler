/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.helpers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class ImageHelper {

    public static BufferedImage read(InputStream input) throws IOException {
        BufferedImage in = ImageIO.read(input);
        int type = in.getType();
        if (type != BufferedImage.TYPE_INT_ARGB && type != BufferedImage.TYPE_INT_RGB) {
            // convert to ARGB
            int width = in.getWidth();
            int height = in.getHeight();
            int[] imgData = in.getRGB(0, 0, width, height, null, 0, width);
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            newImage.getRaster().setDataElements(0, 0, width, height, imgData);
            return newImage;
        }
        
        return in;
    }

    public static void write(BufferedImage image, String formatName, OutputStream output) throws IOException {
        ImageIO.write(image, formatName, output);
    }

    public static void write(BufferedImage image, String formatName, ByteArrayOutputStream output) {
        try {
            ImageIO.write(image, formatName, output);
        } catch (IOException ex) {
            Logger.getLogger(ImageHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
