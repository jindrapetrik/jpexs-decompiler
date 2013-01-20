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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public abstract class SHAPERECORD implements Cloneable {

   public static float twipToPixel(int twip) {
      return ((float) twip) / 20.0f;
   }

   private static class Path {

      public LINESTYLE lineStyle;
      public LINESTYLE2 lineStyle2;
      public boolean useLineStyle2;
      public FILLSTYLE fillStyle0;
      public FILLSTYLE fillStyle1;
      public List<SHAPERECORD> edges = new ArrayList<SHAPERECORD>();

      public boolean sameStyle(Path p) {
         return fillStyle0 == p.fillStyle0 && ((useLineStyle2 && (p.lineStyle2 == lineStyle2)) || ((!useLineStyle2) && (p.lineStyle == lineStyle)));
      }

      public String toSVG(int shapeNum) {
         String ret = "";
         String params = "";
         String f = "";
         if (fillStyle0 != null) {
            if (fillStyle0.fillStyleType == FILLSTYLE.SOLID) {
               f = " fill=\"" + ((shapeNum >= 3) ? fillStyle0.colorA.toHexRGB() : fillStyle0.color.toHexRGB()) + "\"";
            }
         }
         params += f;
         if ((!useLineStyle2) && lineStyle != null) {
            params += " stroke=\"" + ((shapeNum >= 3) ? lineStyle.colorA.toHexRGB() : lineStyle.color.toHexRGB()) + "\"";
         }
         if (useLineStyle2 && lineStyle2 != null) {
            params += " stroke-width=\"" + twipToPixel(lineStyle2.width) + "\" stroke=\"" + lineStyle2.color.toHexRGB() + "\"";
         }
         String points = "";
         int x = 0;
         int y = 0;
         for (SHAPERECORD r : edges) {
            points += " " + r.toSWG(x, y);
            x = r.changeX(x);
            y = r.changeY(y);
         }
         ret += "<path" + params + " d=\"" + points + "\"/>";
         return ret;
      }
   }

   public abstract String toSWG(int oldX, int oldY);

   public abstract int changeX(int x);

   public abstract int changeY(int y);

   public abstract void flip();

   public static RECT getBounds(List<SHAPERECORD> records) {
      int x = 0;
      int y = 0;
      int max_x = 0;
      int max_y = 0;
      int min_x = Integer.MAX_VALUE;
      int min_y = Integer.MAX_VALUE;
      boolean started = false;
      for (SHAPERECORD r : records) {
         x = r.changeX(x);
         y = r.changeY(y);
         if (x > max_x) {
            max_x = x;
         }
         if (y > max_y) {
            max_y = y;
         }
         if (r.isMove()) {
            started = true;
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
      return new RECT(min_x, max_x, min_y, max_y);
   }

   /**
    * Convert shape to SVG
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
            paths.add(path);
            path = new Path();

            if (scr.stateNewStyles) {
               fillStyles = scr.fillStyles;
               lineStylesList = scr.lineStyles;
               numFillBits = scr.numFillBits;
               numLineBits = scr.numLineBits;
            }
            if (scr.stateFillStyle0) {
               if (scr.fillStyle0 == 0) {
                  path.fillStyle0 = null;
               } else {
                  path.fillStyle0 = fillStyles.fillStyles[scr.fillStyle0 - 1];
               }
            }
            if (scr.stateFillStyle1) {
               if (scr.fillStyle1 == 0) {
                  path.fillStyle1 = null;
               } else {
                  path.fillStyle1 = fillStyles.fillStyles[scr.fillStyle1 - 1];
               }
            }
            if (scr.stateLineStyle) {
               if (shapeNum >= 4) {
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
         }
         path.edges.add(r);
         x = r.changeX(x);
         y = r.changeY(y);
         if (r.isMove()) {
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
      paths.add(path);
      List<Path> paths2 = new ArrayList<Path>();
      for (Path p : paths) {
         if (p.fillStyle0 != null) {
            paths2.add(p);
         }
         if (p.fillStyle1 != null) {
            Path f = new Path();
            f.edges = new ArrayList<SHAPERECORD>();
            f.fillStyle0 = p.fillStyle1;
            f.lineStyle = p.lineStyle;
            f.lineStyle2 = p.lineStyle2;
            f.useLineStyle2 = p.useLineStyle2;
            x = 0;
            y = 0;
            for (SHAPERECORD r : p.edges) {
               x = r.changeX(x);
               y = r.changeY(y);
               SHAPERECORD r2 = null;
               try {
                  r2 = (SHAPERECORD) r.clone();
               } catch (CloneNotSupportedException ex) {
                  Logger.getLogger(SHAPERECORD.class.getName()).log(Level.SEVERE, null, ex);
               }
               r2.flip();
               f.edges.add(0, r2);
            }
            StyleChangeRecord scr = new StyleChangeRecord();
            scr.stateMoveTo = true;
            scr.moveDeltaX = x;
            scr.moveDeltaY = y;
            f.edges.add(0, scr);
            paths2.add(f);
         }
      }
      List<Path> paths3 = new ArrayList<Path>();
      for (Path p1 : paths2) {
         boolean found = false;
         for (Path p2 : paths3) {
            if (p1 == p2) {
               continue;
            }
            if (p1.sameStyle(p2)) {
               p2.edges.addAll(p1.edges);
               found = true;
               break;
            }
         }
         if (!found) {
            paths3.add(p1);
         }
      }
      ret = "";
      for (Path p : paths3) {
         ret += p.toSVG(shapeNum);
      }
      ret = "<?xml version='1.0' encoding='UTF-8' ?> \n"
              + "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n"
              + "<svg width=\"" + (int) Math.ceil(twipToPixel(max_x)) + "\"\n"
              + "     height=\"" + (int) Math.ceil(twipToPixel(max_y)) + "\"\n"
              + "     viewBox=\"0 0 " + (int) Math.ceil(twipToPixel(max_x)) + " " + (int) Math.ceil(twipToPixel(max_y) + 50) + "\"\n"
              + "     xmlns=\"http://www.w3.org/2000/svg\"\n"
              + "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + ret + ""
              + "</svg>";
      return ret;
   }

   public abstract boolean isMove();
}
