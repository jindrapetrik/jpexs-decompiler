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
 * License along with this library.
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

    protected static final String DRAW_COMMAND_M = "M";

    protected static final String DRAW_COMMAND_L = "L";

    protected static final String DRAW_COMMAND_Q = "Q";

    protected String currentDrawCommand = "";

    protected double deltaX = 0;

    protected double deltaY = 0;

    protected StringBuilder pathData = new StringBuilder();

    protected StringBuilder fillData = new StringBuilder();

    protected double unitDivisor;

    protected StringBuilder shapeData = new StringBuilder();

    protected StringBuilder strokeData = new StringBuilder();

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

        return CanvasShapeExporter.getHtmlPrefix(width, height) + getJsPrefix() + needed + CanvasShapeExporter.getDrawJs(width, height, shapeData.toString()) + getJsSuffix(width, height) + CanvasShapeExporter.getHtmlSuffix();
    }

    public static String getJsSuffix(int width, int height) {
        StringBuilder ret = new StringBuilder();
        ret.append("}\r\n");
        int step = Math.round(65535 / 100);
        int rate = 10;
        ret.append("var step = ").append(step).append(";\r\n");
        ret.append("var ratio = -1;\r\n");
        ret.append("function nextFrame(ctx){\r\n");
        ret.append("\tctx.clearRect(0,0,").append(width).append(",").append(height).append(");\r\n");
        ret.append("\tratio = (ratio+step)%65535;\r\n");
        ret.append("\tmorphshape(ctx,ratio);\r\n");
        ret.append("}\r\n");
        ret.append("window.setInterval(function(){nextFrame(ctx)},").append(rate).append(");\r\n");
        ret.append(CanvasShapeExporter.getJsSuffix());
        return ret.toString();
    }

    public static String getJsPrefix() {
        String ret = CanvasShapeExporter.getJsPrefix();
        ret += "function morphshape(ctx,ratio){\r\n";
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
        fillData.append("\tctx.fillStyle=").append(useRatioColor(color, colorEnd)).append(";\r\n");
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

            fillData.append("\tvar grd=ctx.createLinearGradient(").append(useRatioPos(start.x, start2.x)).append(",").append(useRatioPos(start.y, start2.y)).append(",").append(useRatioPos(end.x, end2.x)).append(",").append(useRatioPos(end.y, end2.y)).append(");\r\n");
        } else {
            fillMatrix = matrix;
            fillMatrixEnd = matrixEnd;
            fillData.append("\tvar grd=ctx.createRadialGradient(").append(useRatioDouble(focalPointRatio * 16384, focalPointRatioEnd * 16384)).append(",0,0,0,0,").append(16384 + 32768 * repeatCnt).append(");\r\n");
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
                fillData.append("\tvar s = ").append(useRatioDouble(
                        pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0),
                        pos + (oneHeight * (revert ? 255 - r2.ratio : r2.ratio) / 255.0)
                )).append(";\r\n\tif(s<0) s = 0;\r\n\tif(s>1) s = 1;\r\n");
                fillData.append("\tgrd.addColorStop(s,").append(useRatioColor(r.color, r2.color)).append(");\r\n");
                lastRadColor = useRatioColor(r.color, r2.color);
            }
            pos += oneHeight;
        }
        fillData.append("\tctx.fillStyle = grd;\r\n");
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
                    SerializableImage img = i.getImage();
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
                    fillMatrix = matrix;
                    fillMatrixEnd = matrixEnd;
                }

                fillData.append("\tvar fimg = ctrans.applyToImage(imageObj").append(bitmapId).append(");\r\n");
                fillData.append("\tvar pat=ctx.createPattern(fimg,\"repeat\");\r\n");
                fillData.append("\tctx.fillStyle = pat;\r\n");
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
        thickness /= SWF.unitDivisor;
        thicknessEnd /= SWF.unitDivisor;
        strokeData.append("\tvar scaleMode = \"").append(scaleMode).append("\";\r\n");
        if (color != null) { //for gradient line fill
            strokeData.append("\tctx.strokeStyle=").append(useRatioColor(color, colorEnd)).append(";\r\n");
        }
        strokeData.append("\tctx.lineWidth=").append(useRatioDouble(thickness, thicknessEnd)).append(";\r\n");
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                strokeData.append("\tctx.lineCap=\"butt\";\r\n");
                break;
            case LINESTYLE2.SQUARE_CAP:
                strokeData.append("\tctx.lineCap=\"square\";\r\n");
                break;
            default:
                strokeData.append("\tctx.lineCap=\"round\";\r\n");
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                strokeData.append("\tctx.lineJoin=\"bevel\";\r\n");
                break;
            case LINESTYLE2.ROUND_JOIN:
                strokeData.append("\tctx.lineJoin=\"round\";\r\n");
                break;
            default:
                strokeData.append("\tctx.lineJoin=\"miter\";\r\n");
                strokeData.append("\tctx.miterLimit=").append(miterLimit).append(";\r\n");
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
            lineFillMatrix = matrix;
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
        preStrokeData += "\tlctx.applyTransforms(ctx._matrix);\r\n";
        preStrokeData += "\tctx = lctx;\r\n";
        strokeData.insert(0, preStrokeData);
    }

    @Override
    public void moveTo(double x, double y, double x2, double y2) {
        currentDrawCommand = DRAW_COMMAND_M;
        pathData.append(currentDrawCommand).append(" ");
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData.append(Helper.doubleStr(x / unitDivisor)).append(" ")
                .append(Helper.doubleStr(x2 / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y2 / unitDivisor)).append(" ");
    }

    @Override
    public void lineTo(double x, double y, double x2, double y2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_L)) {
            currentDrawCommand = DRAW_COMMAND_L;
            pathData.append(currentDrawCommand).append(" ");
        }
        x += deltaX;
        y += deltaY;
        x2 += deltaX;
        y2 += deltaY;
        pathData.append(Helper.doubleStr(x / unitDivisor)).append(" ")
                .append(Helper.doubleStr(x2 / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y2 / unitDivisor)).append(" ");
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY, double controlX2, double controlY2, double anchorX2, double anchorY2) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_Q)) {
            currentDrawCommand = DRAW_COMMAND_Q;
            pathData.append(currentDrawCommand).append(" ");
        }
        controlX += deltaX;
        anchorX += deltaX;
        controlY += deltaY;
        anchorY += deltaY;

        controlX2 += deltaX;
        anchorX2 += deltaX;
        controlY2 += deltaY;
        anchorY2 += deltaY;

        pathData.append(Helper.doubleStr(controlX / unitDivisor)).append(" ")
                .append(Helper.doubleStr(controlX2 / unitDivisor)).append(" ")
                .append(Helper.doubleStr(controlY / unitDivisor)).append(" ")
                .append(Helper.doubleStr(controlY2 / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorX / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorX2 / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorY / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorY2 / unitDivisor)).append(" ");
    }

    protected void finalizePath() {
        if (pathData != null && pathData.length() > 0) {
            shapeData.append("\tvar pathData=\"").append(pathData.toString().trim()).append("\";\r\n");
            String drawStroke = "\tdrawMorphPath(ctx,pathData,ratio,true,scaleMode);\r\n";
            String drawFill = "\tdrawMorphPath(ctx,pathData,ratio,false);\r\n";;
            pathData = new StringBuilder();
            if (lineFillData != null) {
                String preLineFillData = "";
                preLineFillData += "\tvar oldctx = ctx;\r\n";
                preLineFillData += "\tctx.save();\r\n";
                preLineFillData += strokeData;
                preLineFillData += drawStroke;
                preLineFillData += "\tvar lfcanvas = document.createElement(\"canvas\");\r\n";
                preLineFillData += "\tlfcanvas.width = canvas.width;\r\n";
                preLineFillData += "\tlfcanvas.height = canvas.height;\r\n";
                preLineFillData += "\tvar lfctx = lfcanvas.getContext(\"2d\");\r\n";
                preLineFillData += "\tenhanceContext(lfctx);\r\n";
                preLineFillData += "\tlfctx.applyTransforms(ctx._matrix);\r\n";
                preLineFillData += "\tctx = lfctx;";
                if (lineLastRadColor != null) {
                    preLineFillData += "\tctx.fillStyle=" + lineLastRadColor + ";\r\n ctx.fill(\"evenodd\");\r\n";
                }
                preLineFillData += "\tctx.transform(" + useRatioDouble(lineFillMatrix.scaleX / unitDivisor, lineFillMatrixEnd.scaleX / unitDivisor)
                        + "," + useRatioDouble(lineFillMatrix.rotateSkew0 / unitDivisor, lineFillMatrixEnd.rotateSkew0 / unitDivisor)
                        + "," + useRatioDouble(lineFillMatrix.rotateSkew1 / unitDivisor, lineFillMatrixEnd.rotateSkew1 / unitDivisor)
                        + "," + useRatioDouble(lineFillMatrix.scaleY / unitDivisor, lineFillMatrixEnd.scaleY / unitDivisor)
                        + "," + useRatioDouble((lineFillMatrix.translateX + deltaX) / unitDivisor, (lineFillMatrixEnd.translateX + deltaX) / unitDivisor)
                        + "," + useRatioDouble((lineFillMatrix.translateY + deltaY) / unitDivisor, (lineFillMatrixEnd.translateY + deltaY) / unitDivisor) + ");\r\n";
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
                strokeData = new StringBuilder();
            } else {
                pathData.append(strokeData);
            }
            if (fillMatrix != null) {
                pathData.append(drawFill);
                if (lastRadColor != null) {
                    pathData.append("\tctx.fillStyle=").append(lastRadColor).append(";\r\n\tctx.fill(\"evenodd\");\r\n");
                }
                pathData.append("\tctx.save();\r\n");
                pathData.append("\tctx.clip();\r\n");
                pathData.append("\tctx.transform(").append(useRatioDouble(fillMatrix.scaleX / unitDivisor, fillMatrixEnd.scaleX / unitDivisor))
                        .append(",").append(useRatioDouble(fillMatrix.rotateSkew0 / unitDivisor, fillMatrixEnd.rotateSkew0 / unitDivisor))
                        .append(",").append(useRatioDouble(fillMatrix.rotateSkew1 / unitDivisor, fillMatrixEnd.rotateSkew1 / unitDivisor))
                        .append(",").append(useRatioDouble(fillMatrix.scaleY / unitDivisor, fillMatrixEnd.scaleY / unitDivisor))
                        .append(",").append(useRatioDouble((fillMatrix.translateX + deltaX) / unitDivisor, (fillMatrixEnd.translateX + deltaX) / unitDivisor))
                        .append(",").append(useRatioDouble((fillMatrix.translateY + deltaY) / unitDivisor, (fillMatrixEnd.translateY + deltaY) / unitDivisor)).append(");\r\n");

                if (fillWidth > 0) {//repeating bitmap glitch fix
                    //make bitmap 1px wider
                    double s_w = (fillWidth + 1) / (double) fillWidth;
                    double s_h = (fillHeight + 1) / (double) fillHeight;

                    pathData.append("\tctx.transform(").append(s_w).append(",0,0,").append(s_h).append(",-0.5,-0.5);\r\n");
                }

                pathData.append(fillData);
                pathData.append("\tctx.fillRect(").append(-16384 - 32768 * repeatCnt).append(",").append(-16384 - 32768 * repeatCnt).append(",").append(2 * 16384 + 32768 * 2 * repeatCnt).append(",").append(2 * 16384 + 32768 * 2 * repeatCnt).append(");\r\n");
                pathData.append("\tctx.restore();\r\n");
                shapeData.append(pathData);
            } else {
                if (fillData != null && fillData.length() > 0) {
                    pathData.append(drawFill).append("\r\n\tctx.fill(\"evenodd\");\r\n");
                }
                shapeData.append(fillData).append(pathData);
            }
            if (strokeData != null && strokeData.length() > 0) {
                shapeData.append(drawStroke).append("\r\n"); //"\tctx.stroke();\r\n";
            } else if (lineFillData != null) {
                shapeData.append(lineFillData);
            }
        }

        repeatCnt = 0;
        pathData = new StringBuilder();
        fillData = new StringBuilder();
        strokeData = new StringBuilder();
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
        return shapeData.toString();
    }

    private String useRatioColor(RGB color, RGB colorEnd) {
        return "tocolor(ctrans.apply([" + useRatioInt(color.red, colorEnd.red) + "," + useRatioInt(color.green, colorEnd.green) + "," + useRatioInt(color.blue, colorEnd.blue) + ",((" + useRatioInt((color instanceof RGBA) ? ((RGBA) color).alpha : 255, (colorEnd instanceof RGBA) ? ((RGBA) colorEnd).alpha : 255) + ")/255)]))";
    }
}
