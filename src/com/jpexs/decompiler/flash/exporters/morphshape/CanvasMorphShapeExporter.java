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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;

/**
 *
 * @author JPEXS
 */
public class CanvasMorphShapeExporter extends MorphShapeExporterBase {

    protected double deltaX = 0;
    protected double deltaY = 0;
    protected String pathData = "";
    protected String fillData = "";
    protected double unitDivisor;
    protected String shapeData = "";
    protected String strokeData = "";
    protected Matrix fillMatrix = null;
    protected Matrix fillMatrixEnd = null;
    protected String lastRadColor = null;
    protected int repeatCnt = 0;
    protected SWF swf;

    protected String lineFillData = null;
    protected String lineLastRadColor = null;
    protected Matrix lineFillMatrix = null;
    protected Matrix lineFillMatrixEnd = null;
    protected int lineRepeatCnt = 0;
    
    protected int fillWidth;
    protected int fillHeight;

    public CanvasMorphShapeExporter(SWF swf, SHAPE shape, SHAPE endShape, ColorTransform colorTransform, double unitDivisor, int deltaX, int deltaY) {
        super(shape, endShape, colorTransform);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.unitDivisor = unitDivisor;
        this.swf = swf;
    }

    public String getHtml(String needed) {
        int width = (int) (Math.max(shape.getBounds().getWidth(), shapeEnd.getBounds().getWidth()) / unitDivisor);
        int height = (int) (Math.max(shape.getBounds().getHeight(), shapeEnd.getBounds().getHeight()) / unitDivisor);

        return CanvasShapeExporter.getHtmlPrefix(width, height) + getJsPrefix() + needed + CanvasShapeExporter.getDrawJs(width,height,shapeData) + getJsSuffix(width, height) + CanvasShapeExporter.getHtmlSuffix();
    }

    public static String getJsSuffix(int width, int height) {
        String ret = "";
        ret += "}\r\n";
        int step = Math.round(65535 / 100);
        int rate = 10;
        ret += "var step = " + step + ";\r\n";
        ret += "var ratio = -1;\r\n";
        ret += "function nextFrame(ctx){\r\n";
        ret += "\tctx.clearRect(0,0," + width + "," + height + ");\r\n";
        ret += "\tratio = (ratio+step)%65535;\r\n";
        ret += "\tmorphshape(ctx,ratio);\r\n";
        ret += "}\r\n";
        ret +="window.setInterval(function(){nextFrame(ctx)},"+rate+");\r\n";
        ret += "</script>\r\n";
        return ret;          
    }
    
    public static String getJsPrefix(){
        String ret = "<script>\r\n" +CanvasShapeExporter.getJsPrefix();
        ret+="function morphshape(ctx,ratio){\r\n";
        return ret;
    }

    @Override
    public void beginShape() {
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
    public void beginFill(RGB color, RGB colorEnd) {
        finalizePath();
        fillData += "\tctx.fillStyle=" + useRatioColor(color, colorEnd) + ";\r\n";
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
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

            Point start2 = matrixEnd.transform(new Point(-16384 - 32768 * repeatCnt, 0));
            Point end2 = matrixEnd.transform(new Point(16384 + 32768 * repeatCnt, 0));
            start2.x += deltaX;
            start2.y += deltaY;
            end2.x += deltaX;
            end2.y += deltaY;

            fillData += "\tvar grd=ctx.createLinearGradient(" + useRatioPos(start.x, start2.x) + "," + useRatioPos(start.y, start2.y) + "," + useRatioPos(end.x, end2.x) + "," + useRatioPos(end.y, end2.y) + ");\r\n";
        } else {
            matrix.translateX /= unitDivisor;
            matrix.translateY /= unitDivisor;
            matrix.scaleX /= unitDivisor;
            matrix.scaleY /= unitDivisor;
            matrix.rotateSkew0 /= unitDivisor;
            matrix.rotateSkew1 /= unitDivisor;
            matrix.translateX += deltaX / unitDivisor;
            matrix.translateY += deltaY / unitDivisor;
            fillMatrix = matrix;

            matrixEnd.translateX /= unitDivisor;
            matrixEnd.translateY /= unitDivisor;
            matrixEnd.scaleX /= unitDivisor;
            matrixEnd.scaleY /= unitDivisor;
            matrixEnd.rotateSkew0 /= unitDivisor;
            matrixEnd.rotateSkew1 /= unitDivisor;
            matrixEnd.translateX += deltaX / unitDivisor;
            matrixEnd.translateY += deltaY / unitDivisor;
            fillMatrixEnd = matrixEnd;

            fillData += "\tvar grd=ctx.createRadialGradient(" + useRatioDouble(focalPointRatio * 16384, focalPointRatioEnd * 16384) + ",0,0,0,0," + (16384 + 32768 * repeatCnt) + ");\r\n";
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
            for (int j = 0; j < gradientRecords.length; j++) {
                GRADRECORD r = gradientRecords[j];
                GRADRECORD r2 = gradientRecordsEnd[j];
                fillData += "\tvar s = " + useRatioDouble(
                        pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0),
                        pos + (oneHeight * (revert ? 255 - r2.ratio : r2.ratio) / 255.0)
                ) + ";\r\n\tif(s<0) s = 0;\r\n\tif(s>1) s = 1;\r\n";
                fillData += "\tgrd.addColorStop(s," + useRatioColor(r.color, r2.color) + ");\r\n";
                lastRadColor = useRatioColor(r.color, r2.color);
            }
            pos += oneHeight;
        }
        fillData += "\tctx.fillStyle = grd;\r\n";
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, Matrix matrixEnd, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        ImageTag image = null;
        for (Tag t : swf.tags) {
            if (t instanceof ImageTag) {
                ImageTag i = (ImageTag) t;
                if (i.getCharacterId() == bitmapId) {
                    image = i;
                    SerializableImage img=i.getImage();
                    fillWidth = img.getWidth();
                    fillHeight = img.getHeight();
                    break;
                }
            }
        }
        if (image != null) {
            SerializableImage img = image.getImage();
            if (img != null) {
                colorTransform.apply(img);                                                
                if (matrix != null) {
                    fillMatrix = matrix.clone();
                    fillMatrix.translateX /= unitDivisor;
                    fillMatrix.translateY /= unitDivisor;
                    fillMatrix.scaleX /= unitDivisor;
                    fillMatrix.scaleY /= unitDivisor;
                    fillMatrix.rotateSkew0 /= unitDivisor;
                    fillMatrix.rotateSkew1 /= unitDivisor;
                    fillMatrix.translateX += deltaX / unitDivisor;
                    fillMatrix.translateY += deltaY / unitDivisor;

                    fillMatrixEnd = matrixEnd.clone();
                    fillMatrixEnd.translateX /= unitDivisor;
                    fillMatrixEnd.translateY /= unitDivisor;
                    fillMatrixEnd.scaleX /= unitDivisor;
                    fillMatrixEnd.scaleY /= unitDivisor;
                    fillMatrixEnd.rotateSkew0 /= unitDivisor;
                    fillMatrixEnd.rotateSkew1 /= unitDivisor;
                    fillMatrixEnd.translateX += deltaX / unitDivisor;
                    fillMatrixEnd.translateY += deltaY / unitDivisor;

                }

                fillData += "\tvar fimg = ctrans.applyToImage(image" + bitmapId + ");\r\n";
                fillData += "\tvar pat=ctx.createPattern(fimg,\"repeat\");\r\n";                
                fillData += "\tctx.fillStyle = pat;\r\n";
            }
        }
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
        thickness /= unitDivisor;
        thicknessEnd /= unitDivisor;
        if (color != null) { //for gradient line fill
            strokeData += "\tctx.strokeStyle=" + useRatioColor(color, colorEnd) + ";\r\n";
        }
        strokeData += "\tctx.lineWidth=" + useRatioDouble(thickness, thicknessEnd) + ";\r\n";
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
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
        lineFillData = "";

        //TODO: How many repeats is ideal?        
        final int REPEAT_CNT = 5;

        lineRepeatCnt = spreadMethod == GRADIENT.SPREAD_PAD_MODE ? 0 : REPEAT_CNT;

        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            Point start = matrix.transform(new Point(-16384 - 32768 * repeatCnt, 0));
            Point end = matrix.transform(new Point(16384 + 32768 * repeatCnt, 0));
            start.x += deltaX;
            start.y += deltaY;
            end.x += deltaX;
            end.y += deltaY;

            Point start2 = matrixEnd.transform(new Point(-16384 - 32768 * repeatCnt, 0));
            Point end2 = matrixEnd.transform(new Point(16384 + 32768 * repeatCnt, 0));
            start2.x += deltaX;
            start2.y += deltaY;
            end2.x += deltaX;
            end2.y += deltaY;
            lineFillData += "\tvar grd=ctx.createLinearGradient(" + useRatioPos(start.x, start2.x) + "," + useRatioPos(start.y, start2.y) + "," + useRatioPos(end.x, end2.x) + "," + useRatioPos(end.y, end2.y) + ");\r\n";
        } else {
            matrix.translateX /= unitDivisor;
            matrix.translateY /= unitDivisor;
            matrix.scaleX /= unitDivisor;
            matrix.scaleY /= unitDivisor;
            matrix.rotateSkew0 /= unitDivisor;
            matrix.rotateSkew1 /= unitDivisor;
            matrix.translateX += deltaX / unitDivisor;
            matrix.translateY += deltaY / unitDivisor;
            lineFillMatrix = matrix;

            matrixEnd.translateX /= unitDivisor;
            matrixEnd.translateY /= unitDivisor;
            matrixEnd.scaleX /= unitDivisor;
            matrixEnd.scaleY /= unitDivisor;
            matrixEnd.rotateSkew0 /= unitDivisor;
            matrixEnd.rotateSkew1 /= unitDivisor;
            matrixEnd.translateX += deltaX / unitDivisor;
            matrixEnd.translateY += deltaY / unitDivisor;
            lineFillMatrixEnd = matrixEnd;

            lineFillData += "\tvar grd=ctx.createRadialGradient(" + useRatioDouble(focalPointRatio * 16384, focalPointRatioEnd * 16384) + ",0,0,0,0," + (16384 + 32768 * repeatCnt) + ");\r\n";
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
            for (int j = 0; j < gradientRecords.length; j++) {
                GRADRECORD r = gradientRecords[j];
                GRADRECORD r2 = gradientRecordsEnd[j];
                lineFillData += "\tvar s=" + useRatioDouble(pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0), pos + (oneHeight * (revert ? 255 - r2.ratio : r2.ratio) / 255.0)) + ";\r\n";
                lineFillData += "\tif(s<0) s = 0;\r\n\tif(s>1) s = 1;\r\n";
                lineFillData += "\tgrd.addColorStop(s," + useRatioColor(r.color, r2.color) + ");\r\n";
                lineLastRadColor = useRatioColor(r.color, r2.color);
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
    public void moveTo(double x, double y, double x2, double y2) {
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData += "\tctx.moveTo("
                + useRatioPos(x, x2) + ","
                + useRatioPos(y, y2) + ");\r\n";
    }

    @Override
    public void lineTo(double x, double y, double x2, double y2) {
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData += "\tctx.lineTo("
                + useRatioPos(x, x2) + ","
                + useRatioPos(y, y2) + ");\r\n";
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2) {
        controlX += deltaX;
        anchorX += deltaX;
        controlY += deltaY;
        anchorY += deltaY;

        controlX2 += deltaX;
        anchorX2 += deltaX;
        controlY2 += deltaY;
        anchorY2 += deltaY;

        pathData += "\tctx.quadraticCurveTo(" + useRatioPos(controlX, controlX2) + ","
                + useRatioPos(controlY, controlY2) + ","
                + useRatioPos(anchorX, anchorX2) + ","
                + useRatioPos(anchorY, anchorY2) + ");\r\n";
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
                preLineFillData += "\tlfcanvas.height = canvas.height;\r\n";
                preLineFillData += "\tvar lfctx = lfcanvas.getContext(\"2d\");\r\n";
                preLineFillData += "\tenhanceContext(lfctx);\r\n";
                preLineFillData += "\tlfctx.applyTransforms(ctx._matrices);\r\n";
                preLineFillData += "\tctx = lfctx;";
                if (lineLastRadColor != null) {
                    preLineFillData += "\tctx.fillStyle=" + lineLastRadColor + ";\r\n ctx.fill(\"evenodd\");\r\n";
                }
                preLineFillData += "\tctx.transform(" + useRatioDouble(lineFillMatrix.scaleX, lineFillMatrixEnd.scaleX) + "," + useRatioDouble(lineFillMatrix.rotateSkew0, lineFillMatrixEnd.rotateSkew0) + "," + useRatioDouble(lineFillMatrix.rotateSkew1, lineFillMatrixEnd.rotateSkew1) + "," + useRatioDouble(lineFillMatrix.scaleY, lineFillMatrixEnd.scaleY) + "," + useRatioDouble(lineFillMatrix.translateX, lineFillMatrixEnd.translateX) + "," + useRatioDouble(lineFillMatrix.translateY, lineFillMatrixEnd.translateY) + ");\r\n";
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
                    pathData += "\tctx.fillStyle=" + lastRadColor + ";\r\n\tctx.fill(\"evenodd\");\r\n";
                }
                pathData += "\tctx.save();\r\n";
                pathData += "\tctx.clip();\r\n";
                pathData += "\tctx.transform(" + useRatioDouble(fillMatrix.scaleX, fillMatrixEnd.scaleX) + "," + useRatioDouble(fillMatrix.rotateSkew0, fillMatrixEnd.rotateSkew0) + "," + useRatioDouble(fillMatrix.rotateSkew1, fillMatrixEnd.rotateSkew1) + "," + useRatioDouble(fillMatrix.scaleY, fillMatrixEnd.scaleY) + "," + useRatioDouble(fillMatrix.translateX, fillMatrixEnd.translateX) + "," + useRatioDouble(fillMatrix.translateY, fillMatrixEnd.translateY) + ");\r\n";
                
                if(fillWidth>0){//repeating bitmap glitch fix
                    //make bitmap 1px wider
                    double s_w = (fillWidth+1)/(double)fillWidth;
                    double s_h = (fillHeight+1)/(double)fillHeight;
                                       
                    pathData += "\tctx.transform("+s_w+",0,0,"+s_h+",-0.5,-0.5);\r\n";
                }
                
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
        fillMatrixEnd = null;
        lastRadColor = null;

        lineRepeatCnt = 0;
        lineFillData = null;
        lineLastRadColor = null;
        lineFillMatrix = null;
        lineFillMatrixEnd = null;
        
        fillWidth = 0;
        fillHeight = 0;
    }

    private String useRatioPos(double a, double b) {
        return Helper.doubleStr(a / unitDivisor) + "+ratio*(" + Helper.doubleStr((b - a) / unitDivisor) + ")/" + DefineMorphShapeTag.MAX_RATIO;
    }

    private String useRatioInt(int a, int b) {
        return "Math.round(" + a + "+ratio*(" + ((b - a)) + ")/" + DefineMorphShapeTag.MAX_RATIO + ")";
    }

    private String useRatioDouble(double a, double b) {
        return "" + a + "+ratio*(" + (Helper.doubleStr(b - a)) + ")/" + DefineMorphShapeTag.MAX_RATIO;
    }

    public String getShapeData() {
        return shapeData;
    }

    private String useRatioColor(RGB color, RGB colorEnd) {
        return "tocolor(ctrans.apply([" + useRatioInt(color.red, colorEnd.red) + "," + useRatioInt(color.green, colorEnd.green) + "," + useRatioInt(color.blue, colorEnd.blue) + ",((" + useRatioInt((color instanceof RGBA) ? ((RGBA) color).alpha : 255, (colorEnd instanceof RGBA) ? ((RGBA) colorEnd).alpha : 255) + ")/255)]))";
    }
    
}
