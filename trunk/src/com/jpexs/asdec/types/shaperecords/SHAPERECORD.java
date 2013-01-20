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
package com.jpexs.asdec.types.shaperecords;

import com.jpexs.asdec.types.FILLSTYLE;
import com.jpexs.asdec.types.FILLSTYLEARRAY;
import com.jpexs.asdec.types.LINESTYLE;
import com.jpexs.asdec.types.LINESTYLE2;
import com.jpexs.asdec.types.LINESTYLEARRAY;
import com.jpexs.asdec.types.RECT;
import com.jpexs.asdec.types.RGB;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author JPEXS
 */
public abstract class SHAPERECORD {

   public static float twipToPixel(int twip) {
      return ((float) twip) / 20.0f;
   }

   private static class Path {
      public LINESTYLE lineStyle;
      public LINESTYLE2 lineStyle2;
      public boolean useLineStyle2;
      public FILLSTYLE fillStyle0;
      public FILLSTYLE fillStyle1;
      public String points;
   }
   
   public static RECT getBounds(List<SHAPERECORD> records){      
      int x = 0;
      int y = 0;
      int max_x = 0;
      int max_y = 0;
      int min_x = Integer.MAX_VALUE;
      int min_y = Integer.MAX_VALUE;
      boolean started = false;
      for (SHAPERECORD r : records) {
         if (r instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) r;            
            if (scr.stateMoveTo) {
               x = scr.moveDeltaX;
               y = scr.moveDeltaY;
               started = true;
            }
         } 
         if (r instanceof StraightEdgeRecord) {
            StraightEdgeRecord ser = (StraightEdgeRecord) r;
            if (ser.generalLineFlag) {
               x += ser.deltaX;
               y += ser.deltaY;
               started = true;
            } else if (ser.vertLineFlag) {
               y += ser.deltaY;
               started = true;
            } else {
               x += ser.deltaX;
               started = true;
            }
         }
         if (r instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) r;
            x += cer.controlDeltaX + cer.anchorDeltaX;
            y += cer.controlDeltaY + cer.anchorDeltaY;
            started = true;
         }
         if (x > max_x) {
            max_x = x;
         }
         if (y > max_y) {
            max_y = y;
         }
         if (started) {
            if (y < min_y) {
               min_y = y;
            }
            if (x < min_x) {
               min_x = x;
            }
         }
      }
      return new RECT(min_x,max_x,min_y,max_y);
   }

   /**
    * EXPERIMENTAL - convert shape to SVG
    * 
    * TODO: Fix fill styles
    * 
    * @param shapeNum
    * @param fillStyles
    * @param lineStylesList
    * @param numFillBits
    * @param numLineBits
    * @param records
    * @return Shape converted to SVG
    */
   public static String shapeToSVG(int shapeNum, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStylesList, int numFillBits, int numLineBits, List<SHAPERECORD> records) {
      String ret = "";
      List<Path> paths = new ArrayList<Path>();
      Path path = new Path();
      boolean styleChanged = false;
      int x = 0;
      int y = 0;
      int max_x = 0;
      int max_y = 0;
      int min_x = Integer.MAX_VALUE;
      int min_y = Integer.MAX_VALUE;
      boolean started = false;
      for (SHAPERECORD r : records) {
         if (r instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) r;
            if (scr.stateNewStyles) {
               fillStyles = scr.fillStyles;
               lineStylesList = scr.lineStyles;
               numFillBits = scr.numFillBits;
               numLineBits = scr.numLineBits;
            }
            if (scr.stateFillStyle0) {
               styleChanged = true;
               if (scr.fillStyle0 == 0) {
                  path.fillStyle0 = null;
               } else {
                  path.fillStyle0 = fillStyles.fillStyles[scr.fillStyle0 - 1];
               }
            }
            if (scr.stateFillStyle1) {
               styleChanged = true;
               if (scr.fillStyle1 == 0) {
                  path.fillStyle1 = null;
               } else {
                  path.fillStyle1 = fillStyles.fillStyles[scr.fillStyle1 - 1];
               }
            }
            if (scr.stateLineStyle) {
               styleChanged = true;
               if (shapeNum>=4) {
                  path.useLineStyle2 = true;
                  if (scr.lineStyle == 0) {
                     path.lineStyle2 = null;
                  } else {
                     path.lineStyle2 = lineStylesList.lineStyles2[scr.lineStyle - 1];
                  }
               } else {
                  path.useLineStyle2 = false;
                  if (scr.lineStyle == 0) {
                     path.lineStyle = null;
                  } else {
                     path.lineStyle = lineStylesList.lineStyles[scr.lineStyle - 1];
                  }
               }
            }
            if (scr.stateMoveTo) {
               ret += "M " + twipToPixel(scr.moveDeltaX) + " " + twipToPixel(scr.moveDeltaY);
               x = scr.moveDeltaX;
               y = scr.moveDeltaY;

               started = true;
            }
         } else if (styleChanged) {
            styleChanged = false;
            path.points = ret;
            ret = "M " + twipToPixel(x) + " " + twipToPixel(y);
            paths.add(path);
            path = new Path();
         }
         if (r instanceof StraightEdgeRecord) {
            StraightEdgeRecord ser = (StraightEdgeRecord) r;
            if (ser.generalLineFlag) {
               ret += "l " + twipToPixel(ser.deltaX) + " " + twipToPixel(ser.deltaY);
               x += ser.deltaX;
               y += ser.deltaY;
               started = true;
            } else if (ser.vertLineFlag) {
               ret += "v " + twipToPixel(ser.deltaY);
               y += ser.deltaY;
               started = true;
            } else {
               ret += "h " + twipToPixel(ser.deltaX);
               x += ser.deltaX;
               started = true;
            }
         }
         if (r instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) r;
            ret += "q " + twipToPixel(cer.controlDeltaX) + " " + twipToPixel(cer.controlDeltaY) + " " + twipToPixel(cer.controlDeltaX + cer.anchorDeltaX) + " " + twipToPixel(cer.controlDeltaY + cer.anchorDeltaY);
            x += cer.controlDeltaX + cer.anchorDeltaX;
            y += cer.controlDeltaY + cer.anchorDeltaY;
            started = true;
         }
         if (x > max_x) {
            max_x = x;
         }
         if (y > max_y) {
            max_y = y;
         }
         if (started) {
            if (y < min_y) {
               min_y = y;
            }
            if (x < min_x) {
               min_x = x;
            }
         }
      }
      path.points = ret;
      paths.add(path);
      ret = "";
      for (Path p : paths) {
         String params = "";
         String f = "";
         if (p.fillStyle0 != null) {
            if (p.fillStyle0.fillStyleType == FILLSTYLE.SOLID) {
               f = " fill=\"" + ((shapeNum >= 3) ? p.fillStyle0.colorA.toHexRGB() : p.fillStyle0.color.toHexRGB()) + "\"";
            }
         } else if (p.fillStyle1 != null) {
            if (p.fillStyle1.fillStyleType == FILLSTYLE.SOLID) {
               f = " fill=\"" + ((shapeNum >= 3) ? p.fillStyle1.colorA.toHexRGB() : p.fillStyle1.color.toHexRGB()) + "\"";
            }
         }
         if (p.fillStyle0 != null && p.fillStyle1 != null) {
            Random rnd = new Random();
            f = " fill=\"" + (new RGB(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))).toHexRGB() + "\"";
         }
         params += f;
         if ((!p.useLineStyle2) && p.lineStyle != null) {
            params += " stroke=\"" + ((shapeNum >= 3) ? p.lineStyle.colorA.toHexRGB() : p.lineStyle.color.toHexRGB()) + "\"";
         }
         if (p.useLineStyle2 && p.lineStyle2 != null) {
            params += " stroke-width=\"" + twipToPixel(p.lineStyle2.width) + "\" stroke=\"" + p.lineStyle2.color.toHexRGB() + "\"";
         }
         ret += "<path" + params + " d=\"" + p.points + "\"/>";
      }
      ret = "<?xml version='1.0' encoding='UTF-8' ?> \n"
              + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
              + "<svg width=\"" + (int) Math.ceil(twipToPixel(max_x)) + "\"\n"
              + "     height=\"" + (int) Math.ceil(twipToPixel(max_y)) + "\"\n"
              + "     viewBox=\"" + (int) Math.ceil(twipToPixel(min_x)) + " " + (int) Math.ceil(twipToPixel(min_y)) + " " + (int) Math.ceil(twipToPixel(max_x)) + " " + (int) Math.ceil(twipToPixel(max_y) + 50) + "\"\n"
              + "     xmlns=\"http://www.w3.org/2000/svg\"\n"
              + "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + ret + ""
              + "</svg>";
      return ret;
   }
}
