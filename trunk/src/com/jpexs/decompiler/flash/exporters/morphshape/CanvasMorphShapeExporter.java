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
import static com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter.color;
import static com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter.getHtmlPrefix;
import static com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter.getHtmlSuffix;
import static com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter.getJsPrefix;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

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
   
    
    
    public CanvasMorphShapeExporter(SWF swf,SHAPE shape, SHAPE endShape, ColorTransform colorTransform, double unitDivisor,int deltaX, int deltaY) {
        super(shape, endShape, colorTransform);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.unitDivisor = unitDivisor;
        this.swf = swf;
    }

    
    public String getHtml() {        
        int width = (int) (Math.max(shape.getBounds().getWidth(),shapeEnd.getBounds().getWidth())/unitDivisor);
        int height = (int) (Math.max(shape.getBounds().getHeight(),shapeEnd.getBounds().getHeight())/unitDivisor);
        
        return CanvasShapeExporter.getHtmlPrefix(width, height)+getJsPrefix()+shapeData+getJsSuffix(width,height)+CanvasShapeExporter.getHtmlSuffix();
    }
    
    
    public static String getJsSuffix(int width,int height){
        String ret = "";
        ret += "}\r\n";
        int step = Math.round(65535 / 100);
        int rate = 10;
        ret += "var step = "+step+";\r\n";
        ret += "var ratio = -1;\r\n";
        ret += "function nextFrame(ctx){\r\n";
        ret +="\tctx.clearRect(0,0," + width + "," + height + ");\r\n";
        ret +="\tratio = (ratio+step)%65535;\r\n";
        ret +="\tmorphshape(ctx,ratio);\r\n";
        ret +="}\r\n";

        ret +="window.setInterval(function(){nextFrame(ctx)},"+rate+");\r\n";
        return ret;          
    }
    
    public static String getJsPrefix(){
        String ret = CanvasShapeExporter.getJsPrefix();
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
        fillData += "ctx.fillStyle="+useRatioColor(color,colorEnd)+";\r\n";   
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
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
                        
            fillData += "var grd=ctx.createLinearGradient(" + useRatioPos(start.x, start2.x) + "," + useRatioPos(start.y, start2.y) + "," + useRatioPos(end.x, end2.x) + "," + useRatioPos(end.y, end2.y) + ");\r\n";
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

            
            fillData += "var grd=ctx.createRadialGradient("+focalPointRatio*16384+",0,0,0,0," + (16384 + 32768 * repeatCnt) + ");\r\n";
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
            for(int j=0;j<gradientRecords.length;j++){
                GRADRECORD r = gradientRecords[j];
                GRADRECORD r2 = gradientRecordsEnd[j];
                fillData += "var s = "+useRatioDouble(
                        pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0),
                        pos + (oneHeight * (revert ? 255 - r2.ratio : r2.ratio) / 255.0)
                )+";\r\nif(s<0) s = 0;\r\nif(s>1) s = 1;\r\n";
                fillData += "grd.addColorStop(s," + useRatioColor(r.color,r2.color) + ");\r\n";
                lastRadColor = useRatioColor(r.color,r2.color);
            }
            pos += oneHeight;
        }
        fillData += "ctx.fillStyle = grd;\r\n";
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
                    break;
                }
            }
        }
        if (image != null) {
            SerializableImage img = image.getImage();
            if (img != null) {
                colorTransform.apply(img);
                String format = image.getImageFormat();
                InputStream imageStream = image.getImageData();
                byte[] imageData;
                if (imageStream != null) {
                    imageData = Helper.readStream(image.getImageData());
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(img.getBufferedImage(), format.toUpperCase(Locale.ENGLISH), baos);
                    } catch (IOException ex) {
                    }
                    imageData = baos.toByteArray();
                }
                String base64ImgData = DatatypeConverter.printBase64Binary(imageData);
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

                fillData += "var img = document.createElement(\"img\");\r\nimg.src=\"data:image/" + format + ";base64," + base64ImgData + "\";\r\n";
                fillData += "var pat=ctx.createPattern(img,\"repeat\");\r\n";
                fillData += "ctx.fillStyle = pat;\r\n";
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
        
        strokeData += "ctx.strokeStyle=" + useRatioColor(color,colorEnd) + ";\r\n";
        strokeData += "ctx.lineWidth="+useRatioDouble(thickness,thicknessEnd)+";\r\n";
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                strokeData += "ctx.lineCap=\"butt\";\r\n";
                break;
            case LINESTYLE2.SQUARE_CAP:
                strokeData += "ctx.lineCap=\"square\";\r\n";
                break;
            default:
                strokeData += "ctx.lineCap=\"round\";\r\n";
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                strokeData += "ctx.lineJoin=\"bevel\";\r\n";
                break;
            case LINESTYLE2.ROUND_JOIN:
                strokeData += "ctx.lineJoin=\"round\";\r\n";
                break;
            default:
                strokeData += "ctx.lineJoin=\"miter\";\r\n";
                strokeData += "ctx.miterLimit=" + Integer.toString(miterLimit) + ";\r\n";
                break;
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        //TODO
    }

    @Override
    public void moveTo(double x, double y, double x2, double y2) {
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData += "ctx.moveTo("
                + useRatioPos(x,x2) + ","
                + useRatioPos(y,y2) + ");\r\n";
    }

    @Override
    public void lineTo(double x, double y, double x2, double y2) {
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData += "ctx.lineTo("
                + useRatioPos(x,x2) + ","
                + useRatioPos(y,y2) + ");\r\n";
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
        
        
        pathData += "ctx.quadraticCurveTo(" + useRatioPos(controlX,controlX2) + ","
                + useRatioPos(controlY,controlY2) + ","
                + useRatioPos(anchorX,anchorX2) + ","
                + useRatioPos(anchorY,anchorY2) + ");\r\n";
    }

    protected void finalizePath() {
        if (!"".equals(pathData)) {
            pathData = "ctx.beginPath();\r\n" + pathData + "ctx.closePath();\r\n" + strokeData;
            
            if (fillMatrix != null) {
                if (lastRadColor != null) {
                    pathData += "ctx.fillStyle=" + lastRadColor + ";\r\n ctx.fill(\"evenodd\");\r\n";
                }
                pathData += "ctx.save();\r\n";
                pathData += "ctx.clip();\r\n";
                pathData += "ctx.transform(" + useRatioDouble(fillMatrix.scaleX,fillMatrixEnd.scaleX) + "," + useRatioDouble(fillMatrix.rotateSkew0,fillMatrixEnd.rotateSkew0) + "," + useRatioDouble(fillMatrix.rotateSkew1,fillMatrixEnd.rotateSkew1) + "," + useRatioDouble(fillMatrix.scaleY,fillMatrixEnd.scaleY) + "," + useRatioDouble(fillMatrix.translateX,fillMatrixEnd.translateX) + "," + useRatioDouble(fillMatrix.translateY,fillMatrixEnd.translateY) + ");\r\n";
                pathData += "console.log('m:'+("+useRatioDouble(fillMatrix.scaleX,fillMatrixEnd.scaleX)+")+','+("+useRatioDouble(fillMatrix.scaleY,fillMatrixEnd.scaleY)+")+','+("+useRatioDouble(fillMatrix.rotateSkew0,fillMatrixEnd.rotateSkew0)+")+','+("+useRatioDouble(fillMatrix.rotateSkew1,fillMatrixEnd.rotateSkew1)+"));";
                pathData += fillData;
                pathData += "ctx.fillRect(" + (-16384 - 32768 * repeatCnt) + "," + (-16384 - 32768 * repeatCnt) + "," + (2 * 16384 + 32768 * 2 * repeatCnt) + "," + (2 * 16384 + 32768 * 2 * repeatCnt) + ");\r\n";
                pathData += "ctx.restore();\r\n";
                shapeData += pathData;
            } else {
                if (!"".equals(fillData)) {
                    pathData += "ctx.fill(\"evenodd\");\r\n";
                }
                shapeData += fillData + pathData;
            }
            if (!"".equals(strokeData)) {
                shapeData += "ctx.stroke();\r\n";
            }
        }

        repeatCnt = 0;
        pathData = "";
        fillData = "";
        strokeData = "";
        fillMatrix = null;
        fillMatrixEnd = null;
        lastRadColor = null;
    }

    private String useRatioPos(double a, double b){
        return (a / unitDivisor) + "+ratio*(" +((b-a)/unitDivisor)+")/"+DefineMorphShapeTag.MAX_RATIO;
    }
    
    private String useRatioInt(int a, int b){
        return "Math.round("+ a + "+ratio*(" +((b-a))+")/"+DefineMorphShapeTag.MAX_RATIO+")";
    }
    
     private String useRatioDouble(double a, double b){
        return "" + a + "+ratio*(" +((b-a))+")/"+DefineMorphShapeTag.MAX_RATIO;
    }

    public String getShapeData() {
        return shapeData;
    }
    
    private String useRatioColor(RGB color, RGB colorEnd){
        return "\"rgba(\"+" + useRatioInt(color.red,colorEnd.red)+"+\",\"+"+useRatioInt(color.green,colorEnd.green)+"+\",\"+"+useRatioInt(color.blue,colorEnd.blue)+"+\",\"+(("+useRatioInt((color instanceof RGBA)?((RGBA)color).alpha:255,(colorEnd instanceof RGBA)?((RGBA)colorEnd).alpha:255)   + ")/255)+\")\"";
    }       
}
