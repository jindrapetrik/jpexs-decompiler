/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;

/**
 *
 * @author JPEXS
 */
public class CanvasShapeExporter extends ShapeExporterBase {

    protected String pathData = "";
    protected String shapeData = "";
    protected String html = "";
    protected String strokeData = "";
    protected String fillData = "";
    protected double deltaX = 0;
    protected double deltaY = 0;
    protected Matrix fillMatrix = null;
    protected String lastRadColor = null;
    protected SWF swf;
    protected int repeatCnt = 0;
    protected double unitDivisor;
    protected RGB basicFill;
    protected String lineFillData = null;
    protected String lineLastRadColor = null;
    protected Matrix lineFillMatrix = null;
    protected int lineRepeatCnt = 0;

    public static String getJsPrefix() {
        return "<script>var canvas=document.getElementById(\"myCanvas\");\r\n"
                + "var ctx=canvas.getContext(\"2d\");\r\n"
                + "var enhanceContext = function(context) {\r\n"
                + "  var m = [1,0,0,1,0,0];\r\n"
                + "  context._matrices = [m];\r\n"
                + "\r\n"
                + "  //the stack of saved matrices\r\n"
                + "  context._savedMatrices = [[m]];\r\n"
                + "\r\n"
                + "  var super_ = context.__proto__;\r\n"
                + "  context.__proto__ = ({\r\n"
                + "\r\n"
                + "    save: function() {\r\n"
                + "      this._savedMatrices.push(this._matrices.slice());\r\n"
                + "      super_.save.call(this);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    //if the stack of matrices we're managing doesn't have a saved matrix,\r\n"
                + "    //we won't even call the context's original `restore` method.\r\n"
                + "    restore: function() {\r\n"
                + "      if(this._savedMatrices.length == 0)\r\n"
                + "        return;\r\n"
                + "      super_.restore.call(this);\r\n"
                + "      this._matrices = this._savedMatrices.pop();\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    scale: function(x, y) {\r\n"
                + "      super_.scale.call(this, x, y);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    rotate: function(theta) {\r\n"
                + "      super_.rotate.call(this, theta);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    translate: function(x, y) {\r\n"
                + "      super_.translate.call(this, x, y);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    transform: function(a, b, c, d, e, f) {\r\n"
                + "      this._matrices.push([a,b,c,d,e,f]);\r\n"
                + "      super_.transform.call(this, a, b, c, d, e, f);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    setTransform: function(a, b, c, d, e, f) {\r\n"
                + "      this._matrices=[];\r\n"
                + "      this._matrices.push([a,b,c,d,e,f]);\r\n"
                + "      super_.setTransform.call(this, a, b, c, d, e, f);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    resetTransform: function() {\r\n"
                + "      super_.resetTransform.call(this);\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    applyTransforms: function(m) {\r\n"
                + "      this.setTransform(1,0,0,1,0,0);"
                + "      for(var i=0;i<m.length;i++){this.transform(m[i][0],m[i][1],m[i][2],m[i][3],m[i][4],m[i][5]);}\r\n"
                + "    },\r\n"
                + "\r\n"
                + "    __proto__: super_\r\n"
                + "  });\r\n"
                + "\r\n"
                + "  return context;  \r\n"
                + "};\r\n"
                + "\r\n"
                + "enhanceContext(ctx);\r\n";
    }

    public static String getHtmlPrefix(int width, int height) {
        return "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                + "</head>"
                + "<body>\r\n"
                + "\r\n"
                + "<canvas id=\"myCanvas\" width=\"" + width + "\" height=\"" + height + "\" style=\"border:1px solid #c3c3c3;\">\r\n"
                + "Your browser does not support the HTML5 canvas tag.\r\n"
                + "</canvas>\r\n"
                + "\r\n";

    }

    public static String getJsSuffix(){
        return "</script>\r\n";
    }
    
    public static String getHtmlSuffix() {
        return "</body>\r\n"
                + "</html>";
    }

    public String getHtml() {
        RECT r = shape.getBounds();
        int width = (int) (r.getWidth() / unitDivisor);
        int height = (int) (r.getHeight() / unitDivisor);

        return getHtmlPrefix(width, height) + getJsPrefix() + shapeData + getJsSuffix() + getHtmlSuffix();
    }

    public String getShapeData() {
        return shapeData;
    }

    public CanvasShapeExporter(RGB basicFill, double unitDivisor, SWF swf, SHAPE shape, ColorTransform colorTransform, int deltaX, int deltaY) {
        super(shape, colorTransform);
        this.swf = swf;
        this.unitDivisor = unitDivisor;
        this.basicFill = basicFill;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void beginShape() {
        shapeData = "";
    }

    @Override
    public void endShape() {
    }

    @Override
    public void beginFills() {
    }

    @Override
    public void endFills() {
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void endLines() {
        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        finalizePath();
        if (color == null) {
            color = basicFill;
        }
        fillData += "\tctx.fillStyle=\"" + color(color) + "\";\r\n";
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();

        //TODO: How many repeats is ideal?        
        final int REPEAT_CNT = 5;

        repeatCnt = spreadMethod == GRADIENT.SPREAD_PAD_MODE ? 0 : REPEAT_CNT;

        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            Point start = matrix.transform(new Point(-16384 - 32768 * repeatCnt, 0));
            Point end = matrix.transform(new Point(16384 + 32768 * repeatCnt, 0));
            start.x += deltaX;
            start.y += deltaY;
            end.x += deltaX;
            end.y += deltaY;
            fillData += "\tvar grd=ctx.createLinearGradient(" + Double.toString(start.x / unitDivisor) + "," + Double.toString(start.y / unitDivisor) + "," + Double.toString(end.x / unitDivisor) + "," + Double.toString(end.y / unitDivisor) + ");\r\n";
        } else {
            matrix.translateX /= unitDivisor;
            matrix.translateY /= unitDivisor;
            matrix.scaleX /= unitDivisor;
            matrix.scaleY /= unitDivisor;
            matrix.rotateSkew0 /= unitDivisor;
            matrix.rotateSkew1 /= unitDivisor;
            fillMatrix = matrix;

            matrix.translateX += deltaX / unitDivisor;
            matrix.translateY += deltaY / unitDivisor;

            fillData += "\tvar grd=ctx.createRadialGradient(" + focalPointRatio * 16384 + ",0,0,0,0," + (16384 + 32768 * repeatCnt) + ");\r\n";
        }
        int repeatTotal = repeatCnt * 2 + 1;
        double oneHeight = 1.0 / repeatTotal;
        double pos = 0;
        boolean revert = false;
        if (type != FILLSTYLE.LINEAR_GRADIENT && spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
            revert = true;
        }
        for (int i = 0; i < repeatTotal; i++) {
            if (spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
                revert = !revert;
            }
            for (GRADRECORD r : gradientRecords) {
                fillData += "\tgrd.addColorStop(" + Double.toString(pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0)) + ",\"" + color(r.color) + "\");\r\n";
                lastRadColor = color(r.color);
            }
            pos += oneHeight;
        }
        fillData += "\tctx.fillStyle = grd;\r\n";
    }

    public static String color(Color color) {
        return color(new RGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
    }

    public static String color(RGB rgb) {
        if ((rgb instanceof RGBA) && (((RGBA) rgb).alpha < 255)) {
            RGBA rgba = (RGBA) rgb;
            return "rgba(" + rgba.red + "," + rgba.green + "," + rgba.blue + "," + rgba.getAlphaFloat() + ")";
        } else {
            return rgb.toHexRGB();
        }

    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        ImageTag image = null;
        for (Tag t : swf.tags) {
            if (t instanceof ImageTag) {
                ImageTag i = (ImageTag) t;
                if (i.getCharacterId() == bitmapId) {
                    image = i;
                    break;
                }
            }
        }
        if (image != null) {
            SerializableImage img = image.getImage();
            if (img != null) {
                colorTransform.apply(img);
                if (matrix != null) {
                    matrix.translateX /= unitDivisor;
                    matrix.translateY /= unitDivisor;
                    matrix.scaleX /= unitDivisor;
                    matrix.scaleY /= unitDivisor;
                    matrix.rotateSkew0 /= unitDivisor;
                    matrix.rotateSkew1 /= unitDivisor;
                    fillMatrix = matrix;
                }

                fillData += "\tvar pat=ctx.createPattern(image" + bitmapId + ",\"repeat\");\r\n";
                fillData += "\tctx.fillStyle = pat;\r\n";
            }
        }
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
        thickness /= unitDivisor;

        if (color != null) { //gradient lines have no color
            strokeData += "\tctx.strokeStyle=\"" + color(color) + "\";\r\n";
        }
        strokeData += "\tctx.lineWidth=" + Double.toString(thickness == 0 ? 1 : thickness) + ";\r\n";
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                strokeData += "\tctx.lineCap=\"butt\";\r\n";
                break;
            case LINESTYLE2.SQUARE_CAP:
                strokeData += "\tctx.lineCap=\"square\";\r\n";
                break;
            default:
                strokeData += "\tctx.lineCap=\"round\";\r\n";
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                strokeData += "\tctx.lineJoin=\"bevel\";\r\n";
                break;
            case LINESTYLE2.ROUND_JOIN:
                strokeData += "\tctx.lineJoin=\"round\";\r\n";
                break;
            default:
                strokeData += "\tctx.lineJoin=\"miter\";\r\n";
                strokeData += "\tctx.miterLimit=" + Integer.toString(miterLimit) + ";\r\n";
                break;
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        lineFillData = "";

        //TODO: How many repeats is ideal?        
        final int REPEAT_CNT = 5;

        lineRepeatCnt = spreadMethod == GRADIENT.SPREAD_PAD_MODE ? 0 : REPEAT_CNT;

        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            Point start = matrix.transform(new Point(-16384 - 32768 * lineRepeatCnt, 0));
            Point end = matrix.transform(new Point(16384 + 32768 * lineRepeatCnt, 0));
            start.x += deltaX;
            start.y += deltaY;
            end.x += deltaX;
            end.y += deltaY;
            lineFillData += "\tvar grd=ctx.createLinearGradient(" + Double.toString(start.x / unitDivisor) + "," + Double.toString(start.y / unitDivisor) + "," + Double.toString(end.x / unitDivisor) + "," + Double.toString(end.y / unitDivisor) + ");\r\n";
        } else {
            matrix.translateX /= unitDivisor;
            matrix.translateY /= unitDivisor;
            matrix.scaleX /= unitDivisor;
            matrix.scaleY /= unitDivisor;
            matrix.rotateSkew0 /= unitDivisor;
            matrix.rotateSkew1 /= unitDivisor;
            lineFillMatrix = matrix;

            matrix.translateX += deltaX / unitDivisor;
            matrix.translateY += deltaY / unitDivisor;

            lineFillData += "\tvar grd=ctx.createRadialGradient(" + focalPointRatio * 16384 + ",0,0,0,0," + (16384 + 32768 * lineRepeatCnt) + ");\r\n";
        }
        int repeatTotal = lineRepeatCnt * 2 + 1;
        double oneHeight = 1.0 / repeatTotal;
        double pos = 0;
        boolean revert = false;
        if (type != FILLSTYLE.LINEAR_GRADIENT && spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
            revert = true;
        }
        for (int i = 0; i < repeatTotal; i++) {
            if (spreadMethod == GRADIENT.SPREAD_REFLECT_MODE) {
                revert = !revert;
            }
            for (GRADRECORD r : gradientRecords) {
                lineFillData += "\tgrd.addColorStop(" + Double.toString(pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0)) + ",\"" + color(r.color) + "\");\r\n";
                lineLastRadColor = color(r.color);
            }
            pos += oneHeight;
        }
        lineFillData += "\tctx.fillStyle = grd;\r\n";

        String preStrokeData = "";

        preStrokeData += "\tvar lcanvas = document.createElement(\"canvas\");\r\n";
        preStrokeData += "\tlcanvas.width = canvas.width;\r\n";
        preStrokeData += "\tlcanvas.height=canvas.height;\r\n";
        preStrokeData += "\tvar lctx = lcanvas.getContext(\"2d\");\r\n";
        preStrokeData += "\tenhanceContext(lctx);\r\n";
        preStrokeData += "\tlctx.applyTransforms(ctx._matrices);\r\n";
        preStrokeData += "\tctx = lctx;\r\n";
        strokeData = preStrokeData + strokeData;
    }

    @Override
    public void moveTo(double x, double y) {
        x += deltaX;
        y += deltaY;
        pathData += "\tctx.moveTo("
                + Helper.doubleStr(x / unitDivisor) + ","
                + Helper.doubleStr(y / unitDivisor) + ");\r\n";
    }

    @Override
    public void lineTo(double x, double y) {
        x += deltaX;
        y += deltaY;
        pathData += "\tctx.lineTo(" + Helper.doubleStr(x / unitDivisor) + ","
                + Helper.doubleStr(y / unitDivisor) + ");\r\n";
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        controlX += deltaX;
        anchorX += deltaX;
        controlY += deltaY;
        anchorY += deltaY;
        pathData += "\tctx.quadraticCurveTo(" + Helper.doubleStr(controlX / unitDivisor) + ","
                + Helper.doubleStr(controlY / unitDivisor) + ","
                + Helper.doubleStr(anchorX / unitDivisor) + ","
                + Helper.doubleStr(anchorY / unitDivisor) + ");\r\n";
    }

    protected void finalizePath() {
        if (!"".equals(pathData)) {
            pathData = "\tctx.beginPath();\r\n" + pathData + "\tctx.closePath();\r\n";

            if (lineFillData != null) {
                String preLineFillData = "";
                preLineFillData += "\tvar oldctx = ctx;\r\n";
                preLineFillData += "\tctx.save();\r\n";
                preLineFillData += strokeData;
                preLineFillData += pathData;
                preLineFillData += "\tctx.stroke();\r\n";
                preLineFillData += "\tvar lfcanvas = document.createElement(\"canvas\");\r\n";
                preLineFillData += "\tlfcanvas.width = canvas.width;\r\n";
                preLineFillData += "\tlfcanvas.height=canvas.height;\r\n";
                preLineFillData += "\tvar lfctx = lfcanvas.getContext(\"2d\");\r\n";
                preLineFillData += "\tenhanceContext(lfctx);\r\n";
                preLineFillData += "\tlfctx.applyTransforms(ctx._matrices);\r\n";
                preLineFillData += "\tctx = lfctx;";
                if (lineLastRadColor != null) {
                    preLineFillData += "\tctx.fillStyle=\"" + lineLastRadColor + "\";\r\n ctx.fill(\"evenodd\");\r\n";
                }

                preLineFillData += "\tctx.transform(" + Helper.doubleStr(lineFillMatrix.scaleX) + "," + Helper.doubleStr(lineFillMatrix.rotateSkew0) + "," + Helper.doubleStr(lineFillMatrix.rotateSkew1) + "," + Helper.doubleStr(lineFillMatrix.scaleY) + "," + Helper.doubleStr(lineFillMatrix.translateX) + "," + Helper.doubleStr(lineFillMatrix.translateY) + ");\r\n";
                lineFillData = preLineFillData + lineFillData;
                lineFillData += "\tctx.fillRect(" + (-16384 - 32768 * lineRepeatCnt) + "," + (-16384 - 32768 * lineRepeatCnt) + "," + (2 * 16384 + 32768 * 2 * lineRepeatCnt) + "," + (2 * 16384 + 32768 * 2 * lineRepeatCnt) + ");\r\n";

                lineFillData += "\tctx = oldctx;\r\n";

                //lcanvas - stroke
                //lfcanvas - stroke background
                lineFillData += "\tvar limgd = lctx.getImageData(0, 0, lcanvas.width, lcanvas.height);\r\n"
                        + "\tvar lpix = limgd.data;\r\n"
                        + "\tvar lfimgd = lfctx.getImageData(0, 0, lfcanvas.width, lfcanvas.height);\r\n"
                        + "\tvar lfpix = lfimgd.data;\r\n"
                        + "\tvar imgd = ctx.getImageData(0, 0, canvas.width, canvas.height);\r\n"
                        + "\tvar pix = imgd.data;\r\n"
                        + "\tfor (var i = 0; i < lpix.length; i += 4) {\r\n"
                        + "\t\tif(lpix[i+3]>0){ pix[i] = lfpix[i]; pix[i+1] = lfpix[i+1]; pix[i+2] = lfpix[i+2]; pix[i+3] = lfpix[i+3];}\r\n"
                        + "\t}\r\n"
                        + "\tctx.putImageData(imgd, 0, 0);\r\n";
                lineFillData += "\tctx.restore();\r\n";
                strokeData = "";
            } else {
                pathData += strokeData;
            }
            if (fillMatrix != null) {
                if (lastRadColor != null) {
                    pathData += "\tctx.fillStyle=\"" + lastRadColor + "\";\r\n ctx.fill(\"evenodd\");\r\n";
                }
                pathData += "\tctx.save();\r\n";
                pathData += "\tctx.clip();\r\n";
                pathData += "\tctx.transform(" + Helper.doubleStr(fillMatrix.scaleX) + "," + Helper.doubleStr(fillMatrix.rotateSkew0) + "," + Helper.doubleStr(fillMatrix.rotateSkew1) + "," + Helper.doubleStr(fillMatrix.scaleY) + "," + Helper.doubleStr(fillMatrix.translateX) + "," + Helper.doubleStr(fillMatrix.translateY) + ");\r\n";
                pathData += fillData;
                pathData += "\tctx.fillRect(" + (-16384 - 32768 * repeatCnt) + "," + (-16384 - 32768 * repeatCnt) + "," + (2 * 16384 + 32768 * 2 * repeatCnt) + "," + (2 * 16384 + 32768 * 2 * repeatCnt) + ");\r\n";
                pathData += "\tctx.restore();\r\n";
                shapeData += pathData;
            } else {
                if (!"".equals(fillData)) {
                    pathData += "\tctx.fill(\"evenodd\");\r\n";
                }
                shapeData += fillData + pathData;
            }
            if (!"".equals(strokeData)) {
                shapeData += "\tctx.stroke();\r\n";
            } else if (lineFillData != null) {
                shapeData += lineFillData;
            }
        }

        repeatCnt = 0;

        pathData = "";
        fillData = "";
        strokeData = "";
        fillMatrix = null;
        lastRadColor = null;

        lineRepeatCnt = 0;
        lineFillData = null;
        lineLastRadColor = null;
        lineFillMatrix = null;
    }
    
  
}
