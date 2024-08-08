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
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
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

/**
 * Canvas shape exporter.
 *
 * @author JPEXS
 */
public class CanvasShapeExporter extends ShapeExporterBase {

    /**
     * Draw command M
     */
    protected static final String DRAW_COMMAND_M = "M";

    /**
     * Draw command L
     */
    protected static final String DRAW_COMMAND_L = "L";

    /**
     * Draw command Q
     */
    protected static final String DRAW_COMMAND_Q = "Q";

    /**
     * Current draw command
     */
    protected String currentDrawCommand = "";

    /**
     * Path data
     */
    protected StringBuilder pathData = new StringBuilder();

    /**
     * Shape data
     */
    protected StringBuilder shapeData = new StringBuilder();

    /**
     * Stroke data
     */
    protected StringBuilder strokeData = new StringBuilder();

    /**
     * Fill data
     */
    protected StringBuilder fillData = new StringBuilder();

    /**
     * Delta X
     */
    protected double deltaX = 0;

    /**
     * Delta Y
     */
    protected double deltaY = 0;

    /**
     * Fill matrix
     */
    protected Matrix fillMatrix = null;

    /**
     * Last gradient color
     */
    protected String lastGradColor = null;

    /**
     * SWF
     */
    protected SWF swf;

    /**
     * Repeat count
     */
    protected int repeatCnt = 0;

    /**
     * Unit divisor
     */
    protected double unitDivisor;

    /**
     * Basic fill
     */
    protected RGB basicFill;

    /**
     * Line fill data
     */
    protected StringBuilder lineFillData = null;

    /**
     * Line last gradient color
     */
    protected String lineLastGradColor = null;

    /**
     * Line fill matrix
     */
    protected Matrix lineFillMatrix = null;

    /**
     * Line repeat count
     */
    protected int lineRepeatCnt = 0;

    /**
     * Fill width
     */
    protected int fillWidth = 0;

    /**
     * Fill height
     */
    protected int fillHeight = 0;

    /**
     * Aliased fill
     */
    protected boolean aliasedFill = false;

    /**
     * Gets JS prefix.
     * @return JS prefix
     */
    public static String getJsPrefix() {
        return "<script>var canvas=document.getElementById(\"myCanvas\");\r\n"
                + "var ctx=canvas.getContext(\"2d\");\r\n"
                + "enhanceContext(ctx);\r\n"
                + "var ctrans = new cxform(0,0,0,0,255,255,255,255);\r\n";
    }

    /**
     * Gets HTML prefix.
     * @param width Width
     * @param height Height
     * @return HTML prefix
     */
    public static String getHtmlPrefix(int width, int height) {
        return "<!DOCTYPE html>\r\n"
                + "<html>\r\n"
                + "<head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                + "<script type=\"text/javascript\" src=\"canvas.js\"></script>"
                + "<style type=\"text/css\">"
                + "#resizable {position:relative; display:inline-block; margin:0; padding:0;font-size:0px;} "
                + "#width_size {width:10px; position:absolute; right:-5px; top:0px; bottom:0px; cursor:e-resize;"
                + "-webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none;"
                + "} "
                + "#height_size {height:10px; position:absolute; bottom:-5px; left:0px; right:0px; cursor:n-resize;"
                + "-webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none;"
                + "} "
                /*+ "#both_size {height:10px; width:10px; position:absolute; bottom:-5px; right:-5px; cursor:nw-resize;"+
                 "-webkit-touch-callout: none; -webkit-user-select: none; -khtml-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none;"
                 +"} "*/
                + "#myCanvas {margin:0; padding:0;} "
                + "</style>"
                + "</head>"
                + "<body>\r\n"
                + "\r\n"
                + "<div id=\"resizable\"><canvas id=\"myCanvas\" width=\"" + width + "\" height=\"" + height + "\" style=\"border:1px solid #c3c3c3;\">\r\n"
                + "Your browser does not support the HTML5 canvas tag.\r\n"
                + "</canvas><div id=\"width_size\">&nbsp;</div><div id=\"height_size\">&nbsp;</div>"
                //+ "<div id=\"both_size\">&nbsp;</div>"
                + "</div>\r\n"
                + "\r\n";

    }

    /**
     * Gets JS suffix.
     * @return JS suffix
     */
    public static String getJsSuffix() {
        return "</script>\r\n";
    }

    /**
     * Gets HTML suffix.
     * @return HTML suffix
     */
    public static String getHtmlSuffix() {
        return "</body>\r\n"
                + "</html>";
    }

    private String getDrawJs(int width, int height, String id, RECT rect) {
        return "var originalWidth=" + width + ";\r\nvar originalHeight=" + height + ";\r\n function drawFrame(){\r\n"
                + "\tctx.save();\r\n\tctx.transform(canvas.width/originalWidth,0,0,canvas.height/originalHeight,0,0);\r\n"
                + "\tplace(\"" + id + "\",canvas,ctx,[" + (1 / unitDivisor) + ",0.0,0.0," + (1 / unitDivisor) + ","
                + (-rect.Xmin / unitDivisor) + "," + (-rect.Ymin / unitDivisor) + "],ctrans,1,0,0,0);\r\n"
                + "\tctx.restore();\r\n}\r\n\tdrawFrame();\r\n";
    }

    /**
     * Gets HTML.
     * @param needed Needed
     * @param id ID
     * @param rect Rectangle
     * @return HTML
     */
    public String getHtml(String needed, String id, RECT rect) {
        int width = (int) (rect.getWidth() / unitDivisor);
        int height = (int) (rect.getHeight() / unitDivisor);

        return getHtmlPrefix(width, height) + getJsPrefix() + needed + getDrawJs(width, height, id, rect) + getJsSuffix() + getHtmlSuffix();
    }

    /**
     * Gets shape data.
     * @return Shape data
     */
    public String getShapeData() {
        return shapeData.toString();
    }

    /**
     * Constructor.
     * @param windingRule Winding rule
     * @param shapeNum Shape number
     * @param basicFill Basic fill
     * @param unitDivisor Unit divisor
     * @param swf SWF
     * @param shape Shape
     * @param colorTransform Color transform
     * @param deltaX Delta X
     * @param deltaY Delta Y
     */
    public CanvasShapeExporter(int windingRule, int shapeNum, RGB basicFill, double unitDivisor, SWF swf, SHAPE shape, ColorTransform colorTransform, int deltaX, int deltaY) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
        this.swf = swf;
        this.unitDivisor = unitDivisor;
        this.basicFill = basicFill;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void beginShape() {
        shapeData = new StringBuilder();
    }

    @Override
    public void endShape() {
    }

    @Override
    public void beginFills() {
        aliasedFill = false;
    }

    @Override
    public void endFills() {
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void endLines(boolean close) {
        if (close) {
            pathData.append('Z').append(" ");
        }

        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        finalizePath();
        if (color == null) {
            fillData.append("\tctx.fillStyle=defaultFill;\r\n");
        } else {
            fillData.append("\tctx.fillStyle=").append(color(color)).append(";\r\n");
        }
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
            fillData.append("\tvar grd=ctx.createLinearGradient(").append(start.x / unitDivisor).append(",").append(start.y / unitDivisor).append(",").append(end.x / unitDivisor).append(",").append(end.y / unitDivisor).append(");\r\n");
        } else {
            fillMatrix = matrix;
            fillData.append("\tvar grd=ctx.createRadialGradient(").append(focalPointRatio * 16384).append(",0,0,0,0,").append(16384 + 32768 * repeatCnt).append(");\r\n");
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
                fillData.append("\tgrd.addColorStop(").append(pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0)).append(",").append(color(r.color)).append(");\r\n");
                lastGradColor = color(r.color);
            }
            pos += oneHeight;
        }
        fillData.append("\tctx.fillStyle = grd;\r\n");
    }

    /**
     * Color.
     * @param color Color
     * @return Color
     */
    public static String color(int color) {
        return color(new RGBA(color));
    }

    /**
     * Color.
     * @param rgb RGB
     * @return Color
     */
    public static String color(RGB rgb) {
        if ((rgb instanceof RGBA) && (((RGBA) rgb).alpha < 255)) {
            RGBA rgba = (RGBA) rgb;
            return "tocolor(ctrans.apply([" + rgba.red + "," + rgba.green + "," + rgba.blue + "," + rgba.getAlphaFloat() + "]))";
        } else {
            return "tocolor(ctrans.apply([" + rgb.red + "," + rgb.green + "," + rgb.blue + ",1]))";
        }

    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        ImageTag image = swf.getImage(bitmapId);
        if (image != null) {
            SerializableImage img = image.getImageCached();
            if (img != null) {
                fillWidth = img.getWidth();
                fillHeight = img.getHeight();
                if (colorTransform != null) {
                    colorTransform.apply(img);
                }

                if (matrix != null) {
                    fillMatrix = matrix;
                }

                fillData.append("\tvar fimg = ctrans.applyToImage(imageObj").append(bitmapId).append(");\r\n");
                fillData.append("\tvar pat=ctx.createPattern(fimg,\"repeat\");\r\n");
                fillData.append("\tctx.fillStyle = pat;\r\n");
                return;
            }
        }

        fillData.append("\tctx.fillStyle=").append(color(SWF.ERROR_COLOR.getRGB())).append(";\r\n");
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
        thickness /= SWF.unitDivisor;
        strokeData.append("\tvar scaleMode = \"").append(scaleMode).append("\";\r\n");

        if (color != null) { //gradient lines have no color
            strokeData.append("\tctx.strokeStyle=").append(color(color)).append(";\r\n");
        }
        strokeData.append("\tctx.lineWidth=").append(thickness == 0 ? 1 : thickness).append(";\r\n");
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
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        lineFillData = new StringBuilder();

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
            lineFillData.append("\tvar grd=ctx.createLinearGradient(").append(Double.toString(start.x / unitDivisor)).append(",").append(Double.toString(start.y / unitDivisor)).append(",").append(Double.toString(end.x / unitDivisor)).append(",").append(Double.toString(end.y / unitDivisor)).append(");\r\n");
        } else {
            lineFillMatrix = matrix;
            lineFillData.append("\tvar grd=ctx.createRadialGradient(").append(focalPointRatio * 16384).append(",0,0,0,0,").append(16384 + 32768 * lineRepeatCnt).append(");\r\n");
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
                lineFillData.append("\tgrd.addColorStop(").append(Double.toString(pos + (oneHeight * (revert ? 255 - r.ratio : r.ratio) / 255.0))).append(",").append(color(r.color)).append(");\r\n");
                lineLastGradColor = color(r.color);
            }
            pos += oneHeight;
        }
        lineFillData.append("\tctx.fillStyle = grd;\r\n");

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
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        //TODO
    }

    @Override
    public void moveTo(double x, double y) {
        currentDrawCommand = DRAW_COMMAND_M;
        pathData.append(currentDrawCommand).append(" ");
        x += deltaX;
        y += deltaY;
        pathData.append(Helper.doubleStr(x / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y / unitDivisor)).append(" ");
    }

    @Override
    public void lineTo(double x, double y) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_L)) {
            currentDrawCommand = DRAW_COMMAND_L;
            pathData.append(currentDrawCommand).append(" ");
        }
        x += deltaX;
        y += deltaY;
        pathData.append(Helper.doubleStr(x / unitDivisor)).append(" ")
                .append(Helper.doubleStr(y / unitDivisor)).append(" ");
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        if (!currentDrawCommand.equals(DRAW_COMMAND_Q)) {
            currentDrawCommand = DRAW_COMMAND_Q;
            pathData.append(currentDrawCommand).append(" ");
        }
        controlX += deltaX;
        anchorX += deltaX;
        controlY += deltaY;
        anchorY += deltaY;
        pathData.append(Helper.doubleStr(controlX / unitDivisor)).append(" ")
                .append(Helper.doubleStr(controlY / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorX / unitDivisor)).append(" ")
                .append(Helper.doubleStr(anchorY / unitDivisor)).append(" ");
    }

    /**
     * Finalizes path.
     */
    protected void finalizePath() {
        if (pathData != null && pathData.length() > 0) {
            shapeData.append("\tvar pathData=\"").append(pathData.toString().trim()).append("\";\r\n");
            String drawStroke = "\tdrawPath(ctx,pathData,true,scaleMode);\r\n";
            String drawFill = "\tdrawPath(ctx,pathData,false);\r\n";
            ;
            pathData = new StringBuilder();
            if (lineFillData != null) {
                StringBuilder preLineFillData = new StringBuilder();
                preLineFillData.append("\tvar oldctx = ctx;\r\n");
                preLineFillData.append("\tctx.save();\r\n");
                preLineFillData.append(strokeData);
                preLineFillData.append(drawStroke);
                preLineFillData.append("\tvar lfcanvas = document.createElement(\"canvas\");\r\n");
                preLineFillData.append("\tlfcanvas.width = canvas.width;\r\n");
                preLineFillData.append("\tlfcanvas.height=canvas.height;\r\n");
                preLineFillData.append("\tvar lfctx = lfcanvas.getContext(\"2d\");\r\n");
                preLineFillData.append("\tenhanceContext(lfctx);\r\n");
                preLineFillData.append("\tlfctx.applyTransforms(ctx._matrix);\r\n");
                preLineFillData.append("\tctx = lfctx;");
                if (lineLastGradColor != null) {
                    preLineFillData.append("\tctx.fillStyle=").append(lineLastGradColor).append(";\r\n\tctx.fill(\"").append(windingRule == ShapeTag.WIND_NONZERO ? "nonzero" : "evenodd").append("\");\r\n");
                }

                if (lineFillMatrix != null) {
                    preLineFillData.append("\tctx.transform(").append(Helper.doubleStr(lineFillMatrix.scaleX / unitDivisor))
                            .append(",").append(Helper.doubleStr(lineFillMatrix.rotateSkew0 / unitDivisor))
                            .append(",").append(Helper.doubleStr(lineFillMatrix.rotateSkew1 / unitDivisor))
                            .append(",").append(Helper.doubleStr(lineFillMatrix.scaleY / unitDivisor))
                            .append(",").append(Helper.doubleStr((lineFillMatrix.translateX + deltaX) / unitDivisor))
                            .append(",").append(Helper.doubleStr((lineFillMatrix.translateY + deltaY) / unitDivisor)).append(");\r\n");
                }

                lineFillData.insert(0, preLineFillData);
                lineFillData.append("\tctx.fillRect(").append(-16384 - 32768 * lineRepeatCnt).append(",").append(-16384 - 32768 * lineRepeatCnt).append(",").append(2 * 16384 + 32768 * 2 * lineRepeatCnt).append(",").append(2 * 16384 + 32768 * 2 * lineRepeatCnt).append(");\r\n");

                lineFillData.append("\tctx = oldctx;\r\n");

                //lcanvas - stroke
                //lfcanvas - stroke background
                lineFillData.append("\tvar limgd = lctx.getImageData(0, 0, lcanvas.width, lcanvas.height);\r\n"
                        + "\tvar lpix = limgd.data;\r\n"
                        + "\tvar lfimgd = lfctx.getImageData(0, 0, lfcanvas.width, lfcanvas.height);\r\n"
                        + "\tvar lfpix = lfimgd.data;\r\n"
                        + "\tvar imgd = ctx.getImageData(0, 0, canvas.width, canvas.height);\r\n"
                        + "\tvar pix = imgd.data;\r\n"
                        + "\tfor (var i = 0; i < lpix.length; i += 4) {\r\n"
                        + "\t\tif(lpix[i+3]>0){ pix[i] = lfpix[i]; pix[i+1] = lfpix[i+1]; pix[i+2] = lfpix[i+2]; pix[i+3] = lfpix[i+3];}\r\n"
                        + "\t}\r\n"
                        + "\tctx.putImageData(imgd, 0, 0);\r\n");
                lineFillData.append("\tctx.restore();\r\n");
                strokeData = new StringBuilder();
            } else {
                pathData.append(strokeData);
            }
            if (fillMatrix != null) {
                pathData.append(drawFill);
                if (lastGradColor != null) {
                    pathData.append("\tctx.fillStyle=").append(lastGradColor).append(";\r\n\tctx.fill(\"").append(windingRule == ShapeTag.WIND_NONZERO ? "nonzero" : "evenodd").append("\");\r\n");
                }
                pathData.append("\tctx.save();\r\n");
                pathData.append("\tctx.clip();\r\n");
                pathData.append("\tctx.transform(").append(Helper.doubleStr(fillMatrix.scaleX / unitDivisor))
                        .append(",").append(Helper.doubleStr(fillMatrix.rotateSkew0 / unitDivisor))
                        .append(",").append(Helper.doubleStr(fillMatrix.rotateSkew1 / unitDivisor))
                        .append(",").append(Helper.doubleStr(fillMatrix.scaleY / unitDivisor))
                        .append(",").append(Helper.doubleStr((fillMatrix.translateX + deltaX) / unitDivisor))
                        .append(",").append(Helper.doubleStr((fillMatrix.translateY + deltaY) / unitDivisor)).append(");\r\n");
                if (fillWidth > 0) { //repeating bitmap glitch fix
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
                    pathData.append(drawFill).append("\tctx.fill(\"").append(windingRule == ShapeTag.WIND_NONZERO ? "nonzero" : "evenodd").append("\");\r\n");
                }
                shapeData.append(fillData).append(pathData);
            }
            if (strokeData != null && strokeData.length() > 0) {
                shapeData.append(drawStroke).append("\r\n");
            } else if (lineFillData != null) {
                shapeData.append(lineFillData);
            }
        }

        repeatCnt = 0;

        pathData = new StringBuilder();
        fillData = new StringBuilder();
        strokeData = new StringBuilder();
        fillMatrix = null;
        lastGradColor = null;

        lineRepeatCnt = 0;
        lineFillData = null;
        lineLastGradColor = null;
        lineFillMatrix = null;

        fillWidth = 0;
        fillHeight = 0;
    }

    @Override
    public void beginAliasedFills() {
        aliasedFill = true;
    }
}
