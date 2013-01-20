/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.asdec.tags;

import com.jpexs.asdec.SWFInputStream;
import com.jpexs.asdec.SWFOutputStream;
import com.jpexs.asdec.tags.base.AloneTag;
import com.jpexs.asdec.tags.base.CharacterTag;
import com.jpexs.asdec.types.ALPHABITMAPDATA;
import com.jpexs.asdec.types.ALPHACOLORMAPDATA;
import com.jpexs.asdec.types.RGBA;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.InflaterInputStream;

public class DefineBitsLossless2Tag extends CharacterTag implements AloneTag {

   public int characterID;
   public int bitmapFormat;
   public int bitmapWidth;
   public int bitmapHeight;
   public int bitmapColorTableSize;
   public byte zlibBitmapData[]; //TODO: Parse ALPHACOLORMAPDATA,ALPHABITMAPDATA
   public static final int FORMAT_8BIT_COLORMAPPED = 3;
   public static final int FORMAT_15BIT_RGB = 4;
   public static final int FORMAT_24BIT_RGB = 5;

   @Override
   public int getCharacterID() {
      return characterID;
   }

   public DefineBitsLossless2Tag(byte[] data, int version, long pos) throws IOException {
      super(36, "DefineBitsLossless2", data, pos);
      SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
      characterID = sis.readUI16();
      bitmapFormat = sis.readUI8();
      bitmapWidth = sis.readUI16();
      bitmapHeight = sis.readUI16();
      if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
         bitmapColorTableSize = sis.readUI8();
      }
      zlibBitmapData = sis.readBytes(sis.available());
   }
   private ALPHACOLORMAPDATA colorMapData;
   private ALPHABITMAPDATA bitmapData;
   private boolean decompressed = false;

   public ALPHACOLORMAPDATA getColorMapData() {
      if (!decompressed) {
         uncompressData();
      }
      return colorMapData;
   }

   public ALPHABITMAPDATA getBitmapData() {
      if (!decompressed) {
         uncompressData();
      }
      return bitmapData;
   }

   private void uncompressData() {
      try {
         SWFInputStream sis = new SWFInputStream(new InflaterInputStream(new ByteArrayInputStream(zlibBitmapData)), 10);
         if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            colorMapData = sis.readALPHACOLORMAPDATA(bitmapColorTableSize, bitmapWidth, bitmapHeight);
         }
         if ((bitmapFormat == FORMAT_15BIT_RGB) || (bitmapFormat == FORMAT_24BIT_RGB)) {
            bitmapData = sis.readALPHABITMAPDATA(bitmapFormat, bitmapWidth, bitmapHeight);
         }
      } catch (IOException ex) {
      }
      decompressed = true;
   }

   /**
    * Gets data bytes
    *
    * @param version SWF version
    * @return Bytes of data
    */
   @Override
   public byte[] getData(int version) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream os = baos;
      SWFOutputStream sos = new SWFOutputStream(os, version);
      try {
         sos.writeUI16(characterID);
         sos.writeUI8(bitmapFormat);
         sos.writeUI16(bitmapWidth);
         sos.writeUI16(bitmapHeight);
         if (bitmapFormat == FORMAT_8BIT_COLORMAPPED) {
            sos.writeUI8(bitmapColorTableSize);
         }
         sos.write(zlibBitmapData);
      } catch (IOException e) {
      }
      return baos.toByteArray();
   }

   public BufferedImage getImage() {
      BufferedImage bi = new BufferedImage(bitmapWidth, bitmapHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics g = bi.getGraphics();
      ALPHACOLORMAPDATA colorMapData = null;
      ALPHABITMAPDATA bitmapData = null;
      if (bitmapFormat == DefineBitsLossless2Tag.FORMAT_8BIT_COLORMAPPED) {
         colorMapData = getColorMapData();
      }
      if ((bitmapFormat == DefineBitsLossless2Tag.FORMAT_15BIT_RGB) || (bitmapFormat == DefineBitsLossless2Tag.FORMAT_24BIT_RGB)) {
         bitmapData = getBitmapData();
      }
      int pos32aligned = 0;
      int pos = 0;
      for (int y = 0; y < bitmapHeight; y++) {
         for (int x = 0; x < bitmapWidth; x++) {
            if ((bitmapFormat == DefineBitsLossless2Tag.FORMAT_8BIT_COLORMAPPED)) {
               RGBA color = colorMapData.colorTableRGB[colorMapData.colorMapPixelData[pos32aligned]];
               g.setColor(new Color(color.red, color.green, color.blue, color.alpha));
            }
            if ((bitmapFormat == DefineBitsLossless2Tag.FORMAT_15BIT_RGB) || (bitmapFormat == DefineBitsLossless2Tag.FORMAT_24BIT_RGB)) {
               g.setColor(new Color(bitmapData.bitmapPixelData[pos].red, bitmapData.bitmapPixelData[pos].green, bitmapData.bitmapPixelData[pos].blue, bitmapData.bitmapPixelData[pos].alpha));
            }
            g.fillRect(x, y, 1, 1);
            pos32aligned++;
            pos++;
         }
         while ((pos32aligned % 4 != 0)) {
            pos32aligned++;
         }
      }
      return bi;
   }
}
