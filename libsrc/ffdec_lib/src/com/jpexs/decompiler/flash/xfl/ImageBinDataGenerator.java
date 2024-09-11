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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.helpers.Helper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author JPEXS
 */
public class ImageBinDataGenerator {

    public void generateBinData(InputStream is, OutputStream os, ImageFormat format) throws IOException {
        byte[] inputData = Helper.readStream(is);
        BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(inputData));
        if (format == ImageFormat.JPEG) {
            os.write(inputData);
            BinDataOutputStream dw2 = new BinDataOutputStream(os);
            dw2.writeUI32(0);
            dw2.writeUI32(20 * bimg.getWidth());
            dw2.writeUI32(0);
            dw2.writeUI32(20 * bimg.getHeight());
        } else {
            //https://stackoverflow.com/questions/4082812/xfl-what-are-the-bin-dat-files/4082907#4082907
            os.write(0x03);
            os.write(0x05);
            BinDataOutputStream w = new BinDataOutputStream(os);

            int decRowLen = 4 * bimg.getWidth();
            w.writeUI16(decRowLen);

            w.writeUI16(bimg.getWidth());
            w.writeUI16(bimg.getHeight());

            w.writeUI32(0);
            w.writeUI32(0);
            w.writeUI32(20 * bimg.getWidth());
            w.writeUI32(20 * bimg.getHeight());

            w.write(0x01); //has transparency
            w.write(0x01); //compressed variant

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DeflaterOutputStream def = new DeflaterOutputStream(baos, new Deflater(1));

            for (int y = 0; y < bimg.getHeight(); y++) {
                for (int x = 0; x < bimg.getWidth(); x++) {
                    int rgba = bimg.getRGB(x, y);
                    def.write((rgba >> 24) & 0xFF); //a 
                    def.write((rgba >> 16) & 0xFF); //b
                    def.write((rgba >> 8) & 0xFF); //g
                    def.write(rgba & 0xFF); //r                                                       
                }
            }
            def.flush();
            def.finish();
            byte[] data = baos.toByteArray();
            int pos = 0;
            while (pos < data.length) {
                int cnt = 2048; //it seems that using large chunk sizes like 0xFFFF crashes flash. 2024 is used in CS5.
                if (pos + cnt > data.length) {
                    cnt = data.length - pos;
                }
                w.writeUI16(cnt);
                os.write(data, pos, cnt);
                pos += cnt;
            }
            w.writeUI16(0);
        }
    }
}
