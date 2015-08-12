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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.tags.font.CharacterRanges;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author JPEXS
 */
public class XFLConverter {

    private static final Logger logger = Logger.getLogger(XFLConverter.class.getName());

    public static final int KEY_MODE_NORMAL = 9728;

    public static final int KEY_MODE_CLASSIC_TWEEN = 22017;

    public static final int KEY_MODE_SHAPE_TWEEN = 17922;

    public static final int KEY_MODE_MOTION_TWEEN = 8195;

    public static final int KEY_MODE_SHAPE_LAYERS = 8192;

    private static final Random random = new Random(123); // predictable random

    private XFLConverter() {
    }

    private static void convertShapeEdge(MATRIX mat, SHAPERECORD record, int x, int y, StringBuilder ret) {
        if (record instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) record;
            Point p = new Point(scr.moveDeltaX, scr.moveDeltaY);
            //p = mat.apply(p);
            if (scr.stateMoveTo) {
                ret.append("! ").append(p.x).append(" ").append(p.y);
            }
        } else if (record instanceof StraightEdgeRecord) {
            StraightEdgeRecord ser = (StraightEdgeRecord) record;
            if (ser.generalLineFlag || ser.vertLineFlag) {
                y += ser.deltaY;
            }
            if (ser.generalLineFlag || (!ser.vertLineFlag)) {
                x += ser.deltaX;
            }
            Point p = new Point(x, y);
            //p = mat.apply(p);
            ret.append("| ").append(p.x).append(" ").append(p.y);
        } else if (record instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) record;
            int controlX = cer.controlDeltaX + x;
            int controlY = cer.controlDeltaY + y;
            int anchorX = cer.anchorDeltaX + controlX;
            int anchorY = cer.anchorDeltaY + controlY;
            Point control = new Point(controlX, controlY);
            //control = mat.apply(control);
            Point anchor = new Point(anchorX, anchorY);
            //anchor = mat.apply(anchor);
            ret.append("[ ").append(control.x).append(" ").append(control.y).append(" ").append(anchor.x).append(" ").append(anchor.y);
        }
    }

    private static void convertShapeEdges(int startX, int startY, MATRIX mat, List<SHAPERECORD> records, StringBuilder ret) {
        int x = startX;
        int y = startY;
        ret.append("!").append(startX).append(" ").append(startY);
        for (SHAPERECORD rec : records) {
            convertShapeEdge(mat, rec, x, y, ret);
            x = rec.changeX(x);
            y = rec.changeY(y);
        }
    }

    private static void convertLineStyle(LINESTYLE ls, int shapeNum, StringBuilder ret) {
        ret.append("<SolidStroke weight=\"").append(((float) ls.width) / SWF.unitDivisor)
                .append("\">"
                        + "<fill>"
                        + "<SolidColor color=\"")
                .append(ls.color.toHexRGB()).append("\"")
                .append(shapeNum == 3 ? " alpha=\"" + ((RGBA) ls.color).getAlphaFloat() + "\"" : "").append(" />"
                        + "</fill>"
                        + "</SolidStroke>");
    }

    private static void convertLineStyle(HashMap<Integer, CharacterTag> characters, LINESTYLE2 ls, int shapeNum, StringBuilder ret) {
        StringBuilder params = new StringBuilder();
        if (ls.pixelHintingFlag) {
            params.append(" pixelHinting=\"true\"");
        }
        if (ls.width == 1) {
            params.append(" solidStyle=\"hairline\"");
        }
        if ((!ls.noHScaleFlag) && (!ls.noVScaleFlag)) {
            params.append(" scaleMode=\"normal\"");
        } else if ((!ls.noHScaleFlag) && ls.noVScaleFlag) {
            params.append(" scaleMode=\"horizontal\"");
        } else if (ls.noHScaleFlag && (!ls.noVScaleFlag)) {
            params.append(" scaleMode=\"vertical\"");
        }

        switch (ls.endCapStyle) {  //What about endCapStyle?
            case LINESTYLE2.NO_CAP:
                params.append(" caps=\"none\"");
                break;
            case LINESTYLE2.SQUARE_CAP:
                params.append(" caps=\"square\"");
                break;
        }
        switch (ls.joinStyle) {
            case LINESTYLE2.BEVEL_JOIN:
                params.append(" joints=\"bevel\"");
                break;
            case LINESTYLE2.MITER_JOIN:
                params.append(" joints=\"miter\"");
                float miterLimitFactor = toFloat(ls.miterLimitFactor);
                if (miterLimitFactor != 3.0f) {
                    params.append(" miterLimit=\"").append(miterLimitFactor).append("\"");
                }
                break;
        }

        ret.append("<SolidStroke weight=\"").append(((float) ls.width) / SWF.unitDivisor).append("\"");
        ret.append(params);
        ret.append(">");
        ret.append("<fill>");

        if (!ls.hasFillFlag) {
            RGBA color = (RGBA) ls.color;
            ret.append("<SolidColor color=\"").append(color.toHexRGB()).append("\"").
                    append(color.getAlphaFloat() != 1 ? " alpha=\"" + color.getAlphaFloat() + "\"" : "").
                    append(" />");
        } else {
            convertFillStyle(null/* FIXME */, characters, ls.fillType, shapeNum, ret);
        }
        ret.append("</fill>");
        ret.append("</SolidStroke>");
    }

    private static float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    private static void convertFillStyle(MATRIX mat, HashMap<Integer, CharacterTag> characters, FILLSTYLE fs, int shapeNum, StringBuilder ret) {
        /* todo: use matrix
         if (mat == null) {
         mat = new MATRIX();
         }*/
        //ret.append("<FillStyle index=\"").append(index).append("\">");
        switch (fs.fillStyleType) {
            case FILLSTYLE.SOLID:
                ret.append("<SolidColor color=\"");
                ret.append(fs.color.toHexRGB());
                ret.append("\"");
                if (shapeNum >= 3) {
                    ret.append(" alpha=\"").append(((RGBA) fs.color).getAlphaFloat()).append("\"");
                }
                ret.append(" />");
                break;
            case FILLSTYLE.REPEATING_BITMAP:
            case FILLSTYLE.CLIPPED_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                CharacterTag bitmapCh = characters.get(fs.bitmapId);
                if (bitmapCh instanceof ImageTag) {
                    ImageTag it = (ImageTag) bitmapCh;
                    ret.append("<BitmapFill");
                    ret.append(" bitmapPath=\"");
                    ret.append("bitmap").append(bitmapCh.getCharacterId()).append(".").append(it.getImageFormat());
                } else {
                    if (bitmapCh != null) {
                        logger.log(Level.SEVERE, "Suspicious bitmapfill:{0}", bitmapCh.getClass().getSimpleName());
                    }
                    ret.append("<SolidColor color=\"#ffffff\" />");
                    return;
                }
                ret.append("\"");

                if ((fs.fillStyleType == FILLSTYLE.CLIPPED_BITMAP) || (fs.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
                    ret.append(" bitmapIsClipped=\"true\"");
                }

                ret.append(">");
                ret.append("<matrix>");
                convertMatrix(fs.bitmapMatrix, ret);
                ret.append("</matrix>");
                ret.append("</BitmapFill>");
                break;
            case FILLSTYLE.LINEAR_GRADIENT:
            case FILLSTYLE.RADIAL_GRADIENT:
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:

                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    ret.append("<LinearGradient");
                } else {
                    ret.append("<RadialGradient");
                    ret.append(" focalPointRatio=\"");
                    if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                        ret.append(((FOCALGRADIENT) fs.gradient).focalPoint);
                    } else {
                        ret.append("0");
                    }
                    ret.append("\"");
                }

                int interpolationMode;
                if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                    interpolationMode = fs.gradient.interpolationMode;
                } else {
                    interpolationMode = fs.gradient.interpolationMode;
                }
                int spreadMode;
                if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                    spreadMode = fs.gradient.spreadMode;
                } else {
                    spreadMode = fs.gradient.spreadMode;
                }
                if (interpolationMode == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
                    ret.append(" interpolationMethod=\"linearRGB\"");
                }
                switch (spreadMode) {
                    case GRADIENT.SPREAD_PAD_MODE:

                        break;
                    case GRADIENT.SPREAD_REFLECT_MODE:
                        ret.append(" spreadMethod=\"reflect\"");
                        break;
                    case GRADIENT.SPREAD_REPEAT_MODE:
                        ret.append(" spreadMethod=\"repeat\"");
                        break;
                }

                ret.append(">");

                ret.append("<matrix>");
                convertMatrix(fs.gradientMatrix, ret);
                ret.append("</matrix>");
                GRADRECORD[] records;
                if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                    records = fs.gradient.gradientRecords;
                } else {
                    records = fs.gradient.gradientRecords;
                }
                for (GRADRECORD rec : records) {
                    ret.append("<GradientEntry");
                    ret.append(" color=\"").append(rec.color.toHexRGB()).append("\"");
                    if (shapeNum >= 3) {
                        ret.append(" alpha=\"").append(((RGBA) rec.color).getAlphaFloat()).append("\"");
                    }
                    ret.append(" ratio=\"").append(rec.getRatioFloat()).append("\"");
                    ret.append(" />");
                }
                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    ret.append("</LinearGradient>");
                } else {
                    ret.append("</RadialGradient>");
                }
                break;
        }
        //ret.append("</FillStyle>");
    }

    private static void convertMatrix(MATRIX matrix, StringBuilder ret) {
        Matrix m = new Matrix(matrix);
        ret.append("<Matrix ");
        ret.append("tx=\"").append(((float) m.translateX) / SWF.unitDivisor).append("\" ");
        ret.append("ty=\"").append(((float) m.translateY) / SWF.unitDivisor).append("\" ");
        if (m.scaleX != 1.0 || m.scaleY != 1.0) {
            ret.append("a=\"").append(m.scaleX).append("\" ");
            ret.append("d=\"").append(m.scaleY).append("\" ");
        }
        if (m.rotateSkew0 != 0.0 || m.rotateSkew1 != 0.0) {
            ret.append("b=\"").append(m.rotateSkew0).append("\" ");
            ret.append("c=\"").append(m.rotateSkew1).append("\" ");
        }
        ret.append("/>");
    }

    private static boolean shapeHasMultiLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles) {
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, false);
        return layers.size() > 1;
    }

    private static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape, boolean useLayers) {
        StringBuilder ret = new StringBuilder();
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, morphshape);
        if (layers.size() == 1 && !useLayers) {
            ret.append(layers.get(0));
        } else {
            int layer = 1;
            for (int l = layers.size() - 1; l >= 0; l--) {
                ret.append("<DOMLayer name=\"Layer ").append(layer++).append("\">"); //color=\"#4FFF4F\"
                ret.append("<frames>");
                ret.append("<DOMFrame index=\"0\" motionTweenScale=\"false\" keyMode=\"").append(KEY_MODE_SHAPE_LAYERS).append("\">");
                ret.append("<elements>");
                ret.append(layers.get(l));
                ret.append("</elements>");
                ret.append("</DOMFrame>");
                ret.append("</frames>");
                ret.append("</DOMLayer>");
            }
        }
        return ret.toString();
    }

    /**
     * Remove bugs in shape:
     *
     * ... straightrecord straightrecord stylechange straightrecord (-2,0) <--
     * merge this with previous stylegchange
     *
     * @param shapeRecords
     * @return
     */
    private static List<SHAPERECORD> smoothShape(List<SHAPERECORD> shapeRecords) {
        List<SHAPERECORD> ret = new ArrayList<>(shapeRecords.size());
        for (SHAPERECORD rec : shapeRecords) {
            ret.add(rec.clone());
        }

        for (int i = 1; i < ret.size() - 1; i++) {
            if (ret.get(i) instanceof StraightEdgeRecord && (ret.get(i - 1) instanceof StyleChangeRecord) && (ret.get(i + 1) instanceof StyleChangeRecord)) {
                StraightEdgeRecord ser = (StraightEdgeRecord) ret.get(i);
                StyleChangeRecord scr = (StyleChangeRecord) ret.get(i - 1);
                StyleChangeRecord scr2 = (StyleChangeRecord) ret.get(i + 1);
                if ((!scr.stateMoveTo && !scr.stateNewStyles) && Math.abs(ser.deltaX) < 5 && Math.abs(ser.deltaY) < 5) {
                    if (i >= 2) {
                        SHAPERECORD rbef = ret.get(i - 2);
                        if (rbef instanceof StraightEdgeRecord) {
                            StraightEdgeRecord ser_b = (StraightEdgeRecord) rbef;
                            ser_b.generalLineFlag = true;
                            ser_b.deltaX = ser.changeX(ser_b.deltaX);
                            ser_b.deltaY = ser.changeY(ser_b.deltaY);
                        } else if (rbef instanceof CurvedEdgeRecord) {
                            CurvedEdgeRecord cer_b = (CurvedEdgeRecord) rbef;
                            cer_b.anchorDeltaX = ser.changeX(cer_b.anchorDeltaX);
                            cer_b.anchorDeltaY = ser.changeY(cer_b.anchorDeltaY);
                        } else {
                            //???
                        }

                        ret.remove(i - 1);
                        ret.remove(i - 1);
                        if (scr.stateFillStyle0 && !scr2.stateFillStyle0) {
                            scr2.stateFillStyle0 = true;
                            scr2.fillStyle0 = scr.fillStyle0;
                        }
                        if (scr.stateFillStyle1 && !scr2.stateFillStyle1) {
                            scr2.stateFillStyle1 = true;
                            scr2.fillStyle1 = scr.fillStyle1;
                        }
                        if (scr.stateLineStyle && !scr2.stateLineStyle) {
                            scr2.stateLineStyle = true;
                            scr2.lineStyle = scr.lineStyle;
                        }

                        i -= 2;
                    }
                }
            }

        }
        return ret;
    }

    private static List<String> getShapeLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape) {
        if (mat == null) {
            mat = new MATRIX();
        }
        shapeRecords = smoothShape(shapeRecords);
        List<SHAPERECORD> edges = new ArrayList<>();
        int lineStyleCount = 0;
        int fillStyle0 = -1;
        int fillStyle1 = -1;
        int strokeStyle = -1;
        StringBuilder fillsStr = new StringBuilder();
        StringBuilder strokesStr = new StringBuilder();
        fillsStr.append("<fills>");
        strokesStr.append("<strokes>");
        List<String> layers = new ArrayList<>();
        StringBuilder currentLayer = new StringBuilder();

        int fillStyleCount = 0;
        if (fillStyles != null) {
            for (FILLSTYLE fs : fillStyles.fillStyles) {
                fillsStr.append("<FillStyle index=\"").append(fillStyleCount + 1).append("\">");
                convertFillStyle(mat, characters, fs, shapeNum, fillsStr);
                fillsStr.append("</FillStyle>");
                fillStyleCount++;
            }
        }
        if (lineStyles != null) {
            if (shapeNum <= 3 && lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr.append("<StrokeStyle index=\"").append(lineStyleCount + 1).append("\">");
                    convertLineStyle(lineStyles.lineStyles[l], shapeNum, strokesStr);
                    strokesStr.append("</StrokeStyle>");
                    lineStyleCount++;
                }
            } else if (lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr.append("<StrokeStyle index=\"").append(lineStyleCount + 1).append("\">");
                    convertLineStyle(characters, (LINESTYLE2) lineStyles.lineStyles[l], shapeNum, strokesStr);
                    strokesStr.append("</StrokeStyle>");
                    lineStyleCount++;
                }
            }
        }

        fillsStr.append("</fills>");
        strokesStr.append("</strokes>");

        int layer = 1;

        if ((fillStyleCount > 0) || (lineStyleCount > 0)) {
            currentLayer.append("<DOMShape isFloating=\"true\">");
            currentLayer.append(fillsStr);
            currentLayer.append(strokesStr);
            currentLayer.append("<edges>");
        }
        int x = 0;
        int y = 0;
        int startEdgeX = 0;
        int startEdgeY = 0;

        LINESTYLEARRAY actualLinestyles = lineStyles;
        int strokeStyleOrig = 0;
        fillStyleCount = fillStyles.fillStyles.length;
        for (SHAPERECORD edge : shapeRecords) {
            if (edge instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) edge;
                boolean styleChange = false;
                int lastFillStyle1 = fillStyle1;
                int lastFillStyle0 = fillStyle0;
                int lastStrokeStyle = strokeStyle;
                if (scr.stateNewStyles) {
                    fillsStr.setLength(0);
                    strokesStr.setLength(0);
                    fillsStr.append("<fills>");
                    strokesStr.append("<strokes>");
                    if (fillStyleCount > 0 || lineStyleCount > 0) {

                        if ((fillStyle0 > 0) || (fillStyle1 > 0) || (strokeStyle > 0)) {

                            boolean empty = false;
                            if ((fillStyle0 <= 0) && (fillStyle1 <= 0) && (strokeStyle > 0) && morphshape) {
                                if (shapeNum == 4) {
                                    if (strokeStyleOrig > 0) {
                                        if (!((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                            RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                            if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                                empty = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!empty) {
                                currentLayer.append("<Edge");
                                if (fillStyle0 > -1) {
                                    currentLayer.append(" fillStyle0=\"").append(fillStyle0).append("\"");
                                }
                                if (fillStyle1 > -1) {
                                    currentLayer.append(" fillStyle1=\"").append(fillStyle1).append("\"");
                                }
                                if (strokeStyle > -1) {
                                    currentLayer.append(" strokeStyle=\"").append(strokeStyle).append("\"");
                                }
                                currentLayer.append(" edges=\"");
                                convertShapeEdges(startEdgeX, startEdgeY, mat, edges, currentLayer);
                                currentLayer.append("\" />");
                            }
                        }

                        currentLayer.append("</edges>");
                        currentLayer.append("</DOMShape>");
                        String currentLayerString = currentLayer.toString();
                        if (!currentLayerString.contains("<edges></edges>")) { //no empty layers,  TODO:handle this better
                            layers.add(currentLayerString);
                        }
                        currentLayer.setLength(0);
                    }

                    currentLayer.append("<DOMShape isFloating=\"true\">");
                    //ret += convertShape(characters, null, shape);
                    for (int f = 0; f < scr.fillStyles.fillStyles.length; f++) {
                        fillsStr.append("<FillStyle index=\"").append(f + 1).append("\">");
                        convertFillStyle(mat, characters, scr.fillStyles.fillStyles[f], shapeNum, fillsStr);
                        fillsStr.append("</FillStyle>");
                        fillStyleCount++;
                    }

                    lineStyleCount = 0;
                    if (shapeNum <= 3) {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesStr.append("<StrokeStyle index=\"").append(lineStyleCount + 1).append("\">");
                            convertLineStyle(scr.lineStyles.lineStyles[l], shapeNum, strokesStr);
                            strokesStr.append("</StrokeStyle>");
                            lineStyleCount++;
                        }
                    } else {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesStr.append("<StrokeStyle index=\"").append(lineStyleCount + 1).append("\">");
                            convertLineStyle(characters, (LINESTYLE2) scr.lineStyles.lineStyles[l], shapeNum, strokesStr);
                            strokesStr.append("</StrokeStyle>");
                            lineStyleCount++;
                        }
                    }
                    fillsStr.append("</fills>");
                    strokesStr.append("</strokes>");
                    currentLayer.append(fillsStr);
                    currentLayer.append(strokesStr);
                    currentLayer.append("<edges>");
                    actualLinestyles = scr.lineStyles;
                }
                if (scr.stateFillStyle0) {
                    int fillStyle0_new = scr.fillStyle0;// == 0 ? 0 : fillStylesMap.get(fillStyleCount - lastFillStyleCount + scr.fillStyle0 - 1) + 1;
                    if (morphshape) { //???
                        fillStyle1 = fillStyle0_new;
                    } else {
                        fillStyle0 = fillStyle0_new;
                    }
                    styleChange = true;
                }
                if (scr.stateFillStyle1) {
                    int fillStyle1_new = scr.fillStyle1;// == 0 ? 0 : fillStylesMap.get(fillStyleCount - lastFillStyleCount + scr.fillStyle1 - 1) + 1;
                    if (morphshape) {
                        fillStyle0 = fillStyle1_new;
                    } else {
                        fillStyle1 = fillStyle1_new;
                    }
                    styleChange = true;
                }
                if (scr.stateLineStyle) {
                    strokeStyle = scr.lineStyle;// == 0 ? 0 : lineStyleCount - lastLineStyleCount + scr.lineStyle;
                    strokeStyleOrig = scr.lineStyle - 1;
                    styleChange = true;
                }
                if (!edges.isEmpty()) {
                    if ((fillStyle0 > 0) || (fillStyle1 > 0) || (strokeStyle > 0)) {
                        boolean empty = false;
                        if ((fillStyle0 <= 0) && (fillStyle1 <= 0) && (strokeStyle > 0) && morphshape) {
                            if (shapeNum == 4) {
                                if (strokeStyleOrig > 0) {
                                    if (!((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                        RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                        if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                            empty = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (!empty) {
                            currentLayer.append("<Edge");
                            if (fillStyle0 > -1) {
                                currentLayer.append(" fillStyle0=\"").append(lastFillStyle0).append("\"");
                            }
                            if (fillStyle1 > -1) {
                                currentLayer.append(" fillStyle1=\"").append(lastFillStyle1).append("\"");
                            }
                            if (strokeStyle > -1) {
                                currentLayer.append(" strokeStyle=\"").append(lastStrokeStyle).append("\"");
                            }
                            currentLayer.append(" edges=\"");
                            convertShapeEdges(startEdgeX, startEdgeY, mat, edges, currentLayer);
                            currentLayer.append("\" />");
                        }

                        startEdgeX = x;
                        startEdgeY = y;
                    }
                    edges.clear();
                }
            }
            edges.add(edge);
            x = edge.changeX(x);
            y = edge.changeY(y);
        }
        if (!edges.isEmpty()) {
            if ((fillStyle0 > 0) || (fillStyle1 > 0) || (strokeStyle > 0)) {

                boolean empty = false;
                if ((fillStyle0 <= 0) && (fillStyle1 <= 0) && (strokeStyle > 0) && morphshape) {
                    if (shapeNum == 4) {
                        if (strokeStyleOrig > 0) {
                            if (!((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                    empty = true;
                                }
                            }
                        }
                    }
                }
                if (!empty) {
                    currentLayer.append("<Edge");
                    if (fillStyle0 > -1) {
                        currentLayer.append(" fillStyle0=\"").append(fillStyle0).append("\"");
                    }
                    if (fillStyle1 > -1) {
                        currentLayer.append(" fillStyle1=\"").append(fillStyle1).append("\"");
                    }
                    if (strokeStyle > -1) {
                        currentLayer.append(" strokeStyle=\"").append(strokeStyle).append("\"");
                    }
                    currentLayer.append(" edges=\"");
                    convertShapeEdges(startEdgeX, startEdgeY, mat, edges, currentLayer);
                    currentLayer.append("\" />");
                }
            }
        }
        edges.clear();
        fillsStr.append("</fills>");
        strokesStr.append("</strokes>"); // todo: these fillsStr and strokeStr are not used, why?
        if (currentLayer.length() > 0) {
            currentLayer.append("</edges>");
            currentLayer.append("</DOMShape>");

            String currentLayerString = currentLayer.toString();
            if (!currentLayerString.contains("<edges></edges>")) { //no empty layers, TODO:handle this better
                layers.add(currentLayerString);
            }
        }
        return layers;
    }

    private static int getLayerCount(List<Tag> tags) {
        int maxDepth = 0;
        for (Tag t : tags) {
            if (t instanceof PlaceObjectTypeTag) {
                int d = ((PlaceObjectTypeTag) t).getDepth();
                if (d > maxDepth) {
                    maxDepth = d;
                }
                int cd = ((PlaceObjectTypeTag) t).getClipDepth();
                if (cd > maxDepth) {
                    maxDepth = cd;
                }
            }
        }
        return maxDepth;
    }

    private static void walkShapeUsages(List<Tag> timeLineTags, HashMap<Integer, CharacterTag> characters, HashMap<Integer, Integer> usages) {
        for (Tag t : timeLineTags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                walkShapeUsages(sprite.subTags, characters, usages);
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int ch = po.getCharacterId();
                if (ch > -1) {
                    if (!usages.containsKey(ch)) {
                        usages.put(ch, 0);
                    }
                    int usageCount = usages.get(ch);
                    if (po.getInstanceName() != null) {
                        usageCount++;
                    }
                    if (po.getColorTransform() != null) {
                        usageCount++;
                    }
                    if (po.cacheAsBitmap()) {
                        usageCount++;
                    }
                    MATRIX mat = po.getMatrix();
                    if (mat != null) {
                        if (!mat.isEmpty()) {
                            usageCount++;
                        }
                    }
                    usages.put(ch, usageCount + 1);
                }
            }
        }
    }

    private static List<Integer> getNonLibraryShapes(List<Tag> tags, HashMap<Integer, CharacterTag> characters) {
        HashMap<Integer, Integer> usages = new HashMap<>();
        walkShapeUsages(tags, characters, usages);
        List<Integer> ret = new ArrayList<>();
        for (int ch : usages.keySet()) {
            if (usages.get(ch) < 2) {
                if (characters.get(ch) instanceof ShapeTag) {
                    ShapeTag shp = (ShapeTag) characters.get(ch);
                    if (!shapeHasMultiLayers(characters, null, shp.getShapeNum(), shp.getShapes().shapeRecords, shp.getShapes().fillStyles, shp.getShapes().lineStyles)) {
                        ret.add(ch);
                    }
                }

            }
        }
        return ret;
    }

    private static HashMap<Integer, CharacterTag> getCharacters(List<Tag> tags) {
        HashMap<Integer, CharacterTag> ret = new HashMap<>();
        int maxId = 0;
        for (Tag t : tags) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                if (ct.getCharacterId() > maxId) {
                    maxId = ct.getCharacterId();
                }
            }
        }
        for (Tag t : tags) {
            if (t instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag ssh = (SoundStreamHeadTypeTag) t;
                ssh.setVirtualCharacterId(++maxId);
            }
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                ret.put(ct.getCharacterId(), ct);
            }
        }
        return ret;
    }

    private static final String[] BLENDMODES = {
        null,
        null,
        "layer",
        "multiply",
        "screen",
        "lighten",
        "darken",
        "difference",
        "add",
        "subtract",
        "invert",
        "alpha",
        "erase",
        "overlay",
        "hardligh"
    };

    private static double radToDeg(double rad) {
        return rad * 180 / Math.PI;
    }

    private static String doubleToString(double d, int precision) {
        double m = Math.pow(10, precision);
        d = Math.round(d * m) / m;
        return doubleToString(d);
    }

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    private static void convertFilter(FILTER filter, StringBuilder ret) {
        if (filter instanceof DROPSHADOWFILTER) {
            DROPSHADOWFILTER dsf = (DROPSHADOWFILTER) filter;
            ret.append("<DropShadowFilter");
            if (dsf.dropShadowColor.alpha != 255) {
                ret.append(" alpha=\"").append(doubleToString(dsf.dropShadowColor.getAlphaFloat())).append("\"");
            }
            ret.append(" angle=\"").append(doubleToString(radToDeg(dsf.angle))).append("\"");
            ret.append(" blurX=\"").append(doubleToString(dsf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(dsf.blurY)).append("\"");
            ret.append(" color=\"").append(dsf.dropShadowColor.toHexRGB()).append("\"");
            ret.append(" distance=\"").append(doubleToString(dsf.distance)).append("\"");
            if (!dsf.compositeSource) {
                ret.append(" hideObject=\"true\"");
            }
            if (dsf.innerShadow) {
                ret.append(" inner=\"true\"");
            }
            if (dsf.knockout) {
                ret.append(" knockout=\"true\"");
            }
            ret.append(" quality=\"").append(dsf.passes).append("\"");
            ret.append(" strength=\"").append(doubleToString(dsf.strength, 2)).append("\"");
            ret.append(" />");
        } else if (filter instanceof BLURFILTER) {
            BLURFILTER bf = (BLURFILTER) filter;
            ret.append("<BlurFilter");
            ret.append(" blurX=\"").append(doubleToString(bf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(bf.blurY)).append("\"");
            ret.append(" quality=\"").append(bf.passes).append("\"");
            ret.append(" />");
        } else if (filter instanceof GLOWFILTER) {
            GLOWFILTER gf = (GLOWFILTER) filter;
            ret.append("<GlowFilter");
            if (gf.glowColor.alpha != 255) {
                ret.append(" alpha=\"").append(gf.glowColor.getAlphaFloat()).append("\"");
            }
            ret.append(" blurX=\"").append(doubleToString(gf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(gf.blurY)).append("\"");
            ret.append(" color=\"").append(gf.glowColor.toHexRGB()).append("\"");

            if (gf.innerGlow) {
                ret.append(" inner=\"true\"");
            }
            if (gf.knockout) {
                ret.append(" knockout=\"true\"");
            }
            ret.append(" quality=\"").append(gf.passes).append("\"");
            ret.append(" strength=\"").append(doubleToString(gf.strength, 2)).append("\"");
            ret.append(" />");
        } else if (filter instanceof BEVELFILTER) {
            BEVELFILTER bf = (BEVELFILTER) filter;
            ret.append("<BevelFilter");
            ret.append(" blurX=\"").append(doubleToString(bf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(bf.blurY)).append("\"");
            ret.append(" quality=\"").append(bf.passes).append("\"");
            ret.append(" angle=\"").append(doubleToString(radToDeg(bf.angle))).append("\"");
            ret.append(" distance=\"").append(bf.distance).append("\"");
            if (bf.highlightColor.alpha != 255) {
                ret.append(" highlightAlpha=\"").append(bf.highlightColor.getAlphaFloat()).append("\"");
            }
            ret.append(" highlightColor=\"").append(bf.highlightColor.toHexRGB()).append("\"");
            if (bf.knockout) {
                ret.append(" knockout=\"true\"");
            }
            if (bf.shadowColor.alpha != 255) {
                ret.append(" shadowAlpha=\"").append(bf.shadowColor.getAlphaFloat()).append("\"");
            }
            ret.append(" shadowColor=\"").append(bf.shadowColor.toHexRGB()).append("\"");
            ret.append(" strength=\"").append(doubleToString(bf.strength, 2)).append("\"");
            if (bf.onTop && !bf.innerShadow) {
                ret.append(" type=\"full\"");
            } else if (!bf.innerShadow) {
                ret.append(" type=\"outer\"");
            }
            ret.append(" />");
        } else if (filter instanceof GRADIENTGLOWFILTER) {
            GRADIENTGLOWFILTER ggf = (GRADIENTGLOWFILTER) filter;
            ret.append("<GradientGlowFilter");
            ret.append(" angle=\"").append(doubleToString(radToDeg(ggf.angle))).append("\"");

            ret.append(" blurX=\"").append(doubleToString(ggf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(ggf.blurY)).append("\"");
            ret.append(" quality=\"").append(ggf.passes).append("\"");
            ret.append(" distance=\"").append(doubleToString(ggf.distance)).append("\"");
            if (ggf.knockout) {
                ret.append(" knockout=\"true\"");
            }
            ret.append(" strength=\"").append(doubleToString(ggf.strength, 2)).append("\"");
            if (ggf.onTop && !ggf.innerShadow) {
                ret.append(" type=\"full\"");
            } else if (!ggf.innerShadow) {
                ret.append(" type=\"outer\"");
            }
            ret.append(">");
            for (int g = 0; g < ggf.gradientColors.length; g++) {
                RGBA gc = ggf.gradientColors[g];
                ret.append("<GradientEntry color=\"").append(gc.toHexRGB()).append("\"");
                if (gc.alpha != 255) {
                    ret.append(" alpha=\"").append(gc.getAlphaFloat()).append("\"");
                }
                ret.append(" ratio=\"").append(doubleToString(((float) ggf.gradientRatio[g]) / 255.0)).append("\"");
                ret.append("/>");
            }
            ret.append("</GradientGlowFilter>");
        } else if (filter instanceof GRADIENTBEVELFILTER) {
            GRADIENTBEVELFILTER gbf = (GRADIENTBEVELFILTER) filter;
            ret.append("<GradientBevelFilter");
            ret.append(" angle=\"").append(doubleToString(radToDeg(gbf.angle))).append("\"");

            ret.append(" blurX=\"").append(doubleToString(gbf.blurX)).append("\"");
            ret.append(" blurY=\"").append(doubleToString(gbf.blurY)).append("\"");
            ret.append(" quality=\"").append(gbf.passes).append("\"");
            ret.append(" distance=\"").append(doubleToString(gbf.distance)).append("\"");
            if (gbf.knockout) {
                ret.append(" knockout=\"true\"");
            }
            ret.append(" strength=\"").append(doubleToString(gbf.strength, 2)).append("\"");
            if (gbf.onTop && !gbf.innerShadow) {
                ret.append(" type=\"full\"");
            } else if (!gbf.innerShadow) {
                ret.append(" type=\"outer\"");
            }
            ret.append(">");
            for (int g = 0; g < gbf.gradientColors.length; g++) {
                RGBA gc = gbf.gradientColors[g];
                ret.append("<GradientEntry color=\"").append(gc.toHexRGB()).append("\"");
                if (gc.alpha != 255) {
                    ret.append(" alpha=\"").append(gc.getAlphaFloat()).append("\"");
                }
                ret.append(" ratio=\"").append(doubleToString(((float) gbf.gradientRatio[g]) / 255.0)).append("\"");
                ret.append("/>");
            }
            ret.append("</GradientBevelFilter>");
        } else if (filter instanceof COLORMATRIXFILTER) {
            COLORMATRIXFILTER cmf = (COLORMATRIXFILTER) filter;
            convertAdjustColorFilter(cmf, ret);
        }
    }

    private static String convertSymbolInstance(String name, MATRIX matrix, ColorTransform colorTransform, boolean cacheAsBitmap, int blendMode, List<FILTER> filters, boolean isVisible, RGBA backgroundColor, CLIPACTIONS clipActions, CharacterTag tag, HashMap<Integer, CharacterTag> characters, List<Tag> tags, FLAVersion flaVersion) {
        StringBuilder ret = new StringBuilder();
        if (matrix == null) {
            matrix = new MATRIX();
        }
        if (tag instanceof DefineButtonTag) {
            DefineButtonTag bt = (DefineButtonTag) tag;
            for (Tag t : tags) {
                if (t instanceof DefineButtonCxformTag) {
                    DefineButtonCxformTag bcx = (DefineButtonCxformTag) t;
                    if (bcx.buttonId == bt.buttonId) {
                        colorTransform = bcx.buttonColorTransform;
                    }
                }
            }
        }

        ret.append("<DOMSymbolInstance libraryItemName=\"" + "Symbol ").append(tag.getCharacterId()).append("\"");
        if (name != null) {
            ret.append(" name=\"").append(xmlString(name)).append("\"");
        }
        String blendModeStr = null;
        if (blendMode < BLENDMODES.length) {
            blendModeStr = BLENDMODES[blendMode];
        }
        if (blendModeStr != null) {
            ret.append(" blendMode=\"").append(blendModeStr).append("\"");
        }
        if (tag instanceof ShapeTag) {
            ret.append(" symbolType=\"graphic\" loop=\"loop\"");
        } else if (tag instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) tag;
            RECT spriteRect = sprite.getRect();
            double centerPoint3DX = twipToPixel(matrix.translateX + spriteRect.getWidth() / 2);
            double centerPoint3DY = twipToPixel(matrix.translateY + spriteRect.getHeight() / 2);
            ret.append(" centerPoint3DX=\"").append(centerPoint3DX).append("\" centerPoint3DY=\"").append(centerPoint3DY).append("\"");
        } else if (tag instanceof ButtonTag) {
            ret.append(" symbolType=\"button\"");
        }
        if (cacheAsBitmap) {
            ret.append(" cacheAsBitmap=\"true\"");
        }
        if (!isVisible && flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
            ret.append(" isVisible=\"false\"");
        }
        ret.append(">");
        ret.append("<matrix>");
        convertMatrix(matrix, ret);
        ret.append("</matrix>");
        ret.append("<transformationPoint><Point/></transformationPoint>");

        if (backgroundColor != null) {
            ret.append("<MatteColor color=\"").append(backgroundColor.toHexRGB()).append("\"");
            if (backgroundColor.alpha != 255) {
                ret.append(" alpha=\"").append(doubleToString(backgroundColor.getAlphaFloat())).append("\"");
            }
            ret.append("/>");
        }
        if (colorTransform != null) {
            ret.append("<color><Color");
            if (colorTransform.getRedMulti() != 255) {
                ret.append(" redMultiplier=\"").append(((float) colorTransform.getRedMulti()) / 255.0f).append("\"");
            }
            if (colorTransform.getGreenMulti() != 255) {
                ret.append(" greenMultiplier=\"").append(((float) colorTransform.getGreenMulti()) / 255.0f).append("\"");
            }
            if (colorTransform.getBlueMulti() != 255) {
                ret.append(" blueMultiplier=\"").append(((float) colorTransform.getBlueMulti()) / 255.0f).append("\"");
            }
            if (colorTransform.getAlphaMulti() != 255) {
                ret.append(" alphaMultiplier=\"").append(((float) colorTransform.getAlphaMulti()) / 255.0f).append("\"");
            }

            if (colorTransform.getRedAdd() != 0) {
                ret.append(" redOffset=\"").append(colorTransform.getRedAdd()).append("\"");
            }
            if (colorTransform.getGreenAdd() != 0) {
                ret.append(" greenOffset=\"").append(colorTransform.getGreenAdd()).append("\"");
            }
            if (colorTransform.getBlueAdd() != 0) {
                ret.append(" blueOffset=\"").append(colorTransform.getBlueAdd()).append("\"");
            }
            if (colorTransform.getAlphaAdd() != 0) {
                ret.append(" alphaOffset=\"").append(colorTransform.getAlphaAdd()).append("\"");
            }

            ret.append("/></color>");
        }
        if (filters != null) {
            ret.append("<filters>");
            for (FILTER f : filters) {
                convertFilter(f, ret);
            }
            ret.append("</filters>");
        }
        if (tag instanceof DefineButtonTag) {
            ret.append("<Actionscript><script><![CDATA[");
            ret.append("on(press){\r\n");
            ret.append(convertActionScript(((DefineButtonTag) tag)));
            ret.append("}");
            ret.append("]]></script></Actionscript>");
        }
        if (tag instanceof DefineButton2Tag) {
            DefineButton2Tag db2 = (DefineButton2Tag) tag;
            if (!db2.actions.isEmpty()) {
                ret.append("<Actionscript><script><![CDATA[");
                for (BUTTONCONDACTION bca : db2.actions) {
                    ret.append(convertActionScript(bca));
                }
                ret.append("]]></script></Actionscript>");
            }
        }
        if (clipActions != null) {
            ret.append("<Actionscript><script><![CDATA[");
            for (CLIPACTIONRECORD rec : clipActions.clipActionRecords) {
                ret.append(convertActionScript(rec));
            }
            ret.append("]]></script></Actionscript>");
        }
        ret.append("</DOMSymbolInstance>");
        return ret.toString();
    }

    private static String convertActionScript(ASMSource as) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        try {
            Action.actionsToSource(as, as.getActions(), as.toString()/*FIXME?*/, writer);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    private static long getTimestamp(SWF swf) {
        Date date = swf.getFileModificationDate();
        return date.getTime() / 1000;
    }

    private static void convertLibrary(SWF swf, Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, List<Integer> nonLibraryShapes, String backgroundColor, List<Tag> tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion, StringBuilder ret) {

        //TODO: Imported assets
        //linkageImportForRS="true" linkageIdentifier="xxx" linkageURL="yyy.swf"
        List<String> media = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if ((symbol instanceof ShapeTag) && nonLibraryShapes.contains(symbol.getCharacterId())) {
                continue; //shapes with 1 ocurrence and single layer are not added to library
            }
            if ((symbol instanceof ShapeTag) || (symbol instanceof DefineSpriteTag) || (symbol instanceof ButtonTag)) {
                StringBuilder symbolStr = new StringBuilder();

                symbolStr.append("<DOMSymbolItem xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" name=\"Symbol ").append(symbol.getCharacterId()).append("\" lastModified=\"").append(getTimestamp(swf)).append("\""); //TODO:itemID
                if (symbol instanceof ShapeTag) {
                    symbolStr.append(" symbolType=\"graphic\"");
                } else if (symbol instanceof ButtonTag) {
                    symbolStr.append(" symbolType=\"button\"");
                    if (((ButtonTag) symbol).trackAsMenu()) {
                        symbolStr.append("  trackAsMenu=\"true\"");
                    }
                }
                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr.append(" linkageClassName=\"").append(xmlString(characterClasses.get(symbol.getCharacterId()))).append("\"");
                }
                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr.append(" linkageIdentifier=\"").append(xmlString(characterVariables.get(symbol.getCharacterId()))).append("\"");
                }
                if (linkageExportForAS) {
                    symbolStr.append(" linkageExportForAS=\"true\"");
                }
                symbolStr.append(">");
                symbolStr.append("<timeline>");
                String itemIcon = null;
                if (symbol instanceof ButtonTag) {
                    itemIcon = "0";
                    symbolStr.append("<DOMTimeline name=\"Symbol ").append(symbol.getCharacterId()).append("\" currentFrame=\"0\">");
                    symbolStr.append("<layers>");

                    ButtonTag button = (ButtonTag) symbol;
                    List<BUTTONRECORD> records = button.getRecords();

                    int maxDepth = 0;
                    for (BUTTONRECORD rec : records) {
                        if (rec.placeDepth > maxDepth) {
                            maxDepth = rec.placeDepth;
                        }
                    }
                    for (int i = maxDepth; i >= 1; i--) {
                        symbolStr.append("<DOMLayer name=\"Layer ").append(maxDepth - i + 1).append("\"");
                        if (i == 1) {
                            symbolStr.append(" current=\"true\" isSelected=\"true\"");
                        }
                        symbolStr.append(" color=\"").append(randomOutlineColor()).append("\">");
                        symbolStr.append("<frames>");
                        int lastFrame = 0;
                        loopframes:
                        for (int frame = 1; frame <= 4; frame++) {
                            for (BUTTONRECORD rec : records) {
                                if (rec.placeDepth == i) {
                                    boolean ok = false;
                                    switch (frame) {
                                        case 1:
                                            ok = rec.buttonStateUp;
                                            break;
                                        case 2:
                                            ok = rec.buttonStateOver;
                                            break;
                                        case 3:
                                            ok = rec.buttonStateDown;
                                            break;
                                        case 4:
                                            ok = rec.buttonStateHitTest;
                                            break;
                                    }
                                    if (!ok) {
                                        continue;
                                    }
                                    CXFORMWITHALPHA colorTransformAlpha = null;
                                    int blendMode = 0;
                                    List<FILTER> filters = new ArrayList<>();
                                    if (button instanceof DefineButton2Tag) {
                                        colorTransformAlpha = rec.colorTransform;
                                        if (rec.buttonHasBlendMode) {
                                            blendMode = rec.blendMode;
                                        }
                                        if (rec.buttonHasFilterList) {
                                            filters = rec.filterList;
                                        }
                                    }
                                    CharacterTag character = characters.get(rec.characterId);
                                    MATRIX matrix = rec.placeMatrix;
                                    String recCharStr;
                                    if (character instanceof TextTag) {
                                        recCharStr = convertText(null, (TextTag) character, matrix, filters, null);
                                    } else if (character instanceof DefineVideoStreamTag) {
                                        recCharStr = convertVideoInstance(null, matrix, (DefineVideoStreamTag) character, null);
                                    } else {
                                        recCharStr = convertSymbolInstance(null, matrix, colorTransformAlpha, false, blendMode, filters, true, null, null, characters.get(rec.characterId), characters, tags, flaVersion);
                                    }
                                    int duration = frame - lastFrame;
                                    lastFrame = frame;
                                    if (duration > 0) {
                                        if (duration > 1) {
                                            symbolStr.append("<DOMFrame index=\"");
                                            symbolStr.append((frame - duration));
                                            symbolStr.append("\"");
                                            symbolStr.append(" duration=\"").append(duration - 1).append("\"");
                                            symbolStr.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\">");
                                            symbolStr.append("<elements>");
                                            symbolStr.append("</elements>");
                                            symbolStr.append("</DOMFrame>");
                                        }
                                        symbolStr.append("<DOMFrame index=\"");
                                        symbolStr.append((frame - 1));
                                        symbolStr.append("\"");
                                        symbolStr.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\">");
                                        symbolStr.append("<elements>");
                                        symbolStr.append(recCharStr);
                                        symbolStr.append("</elements>");
                                        symbolStr.append("</DOMFrame>");
                                    }
                                }
                            }
                        }
                        symbolStr.append("</frames>");
                        symbolStr.append("</DOMLayer>");
                    }
                    symbolStr.append("</layers>");
                    symbolStr.append("</DOMTimeline>");
                } else if (symbol instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) symbol;
                    if (sprite.subTags.isEmpty()) { //probably AS2 class
                        continue;
                    }
                    symbolStr.append(convertTimeline(sprite.spriteId, nonLibraryShapes, backgroundColor, tags, sprite.getSubTags(), characters, "Symbol " + symbol.getCharacterId(), flaVersion, files));
                } else if (symbol instanceof ShapeTag) {
                    itemIcon = "1";
                    ShapeTag shape = (ShapeTag) symbol;
                    symbolStr.append("<DOMTimeline name=\"Symbol ").append(symbol.getCharacterId()).append("\" currentFrame=\"0\">");
                    symbolStr.append("<layers>");
                    symbolStr.append(convertShape(characters, null, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, true));
                    symbolStr.append("</layers>");
                    symbolStr.append("</DOMTimeline>");
                }
                symbolStr.append("</timeline>");
                symbolStr.append("</DOMSymbolItem>");
                String symbolStr2 = prettyFormatXML(symbolStr.toString());
                String symbolFile = "Symbol " + symbol.getCharacterId() + ".xml";
                files.put(symbolFile, Utf8Helper.getBytes(symbolStr2));
                String symbLinkStr = "";
                symbLinkStr += "<Include href=\"" + symbolFile + "\"";
                if (itemIcon != null) {
                    symbLinkStr += " itemIcon=\"" + itemIcon + "\"";
                }
                symbLinkStr += " loadImmediate=\"false\"";
                if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
                    symbLinkStr += " lastModified=\"" + getTimestamp(swf) + "\"";
                    //TODO: itemID=\"518de416-00000341\"
                }
                symbLinkStr += "/>";
                symbols.add(symbLinkStr);
            } else if (symbol instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) symbol;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SerializableImage image = imageTag.getImage(false);
                ImageFormat format = imageTag.getImageFormat();
                ImageHelper.write(image.getBufferedImage(), format, baos);
                String symbolFile = "bitmap" + symbol.getCharacterId() + "." + imageTag.getImageFormat();
                files.put(symbolFile, baos.toByteArray());
                String mediaLinkStr = "<DOMBitmapItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp(swf) + "\" externalFileSize=\"" + baos.toByteArray().length + "\"";
                switch (format) {
                    case PNG:
                    case GIF:
                        mediaLinkStr += " useImportedJPEGData=\"false\" compressionType=\"lossless\" originalCompressionType=\"lossless\"";
                        break;
                    case JPEG:
                        mediaLinkStr += " isJPEG=\"true\"";
                        break;
                }
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    mediaLinkStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                }
                mediaLinkStr += " quality=\"50\" href=\"" + symbolFile + "\" bitmapDataHRef=\"M " + (media.size() + 1) + " " + getTimestamp(swf) + ".dat\" frameRight=\"" + image.getWidth() + "\" frameBottom=\"" + image.getHeight() + "\"/>\n";
                media.add(mediaLinkStr);

            } else if ((symbol instanceof SoundStreamHeadTypeTag) || (symbol instanceof DefineSoundTag)) {
                int soundFormat = 0;
                int soundRate = 0;
                boolean soundType = false;
                boolean soundSize = false;
                long soundSampleCount = 0;
                byte[] soundData = SWFInputStream.BYTE_ARRAY_EMPTY;
                int[] rateMap = {5, 11, 22, 44};
                String exportFormat = "flv";
                if (symbol instanceof SoundStreamHeadTypeTag) {
                    SoundStreamHeadTypeTag sstream = (SoundStreamHeadTypeTag) symbol;
                    soundFormat = sstream.getSoundFormatId();
                    soundRate = sstream.getSoundRate();
                    soundType = sstream.getSoundType();
                    soundSize = sstream.getSoundSize();
                    soundSampleCount = sstream.getSoundSampleCount();
                    boolean found = false;
                    for (Tag t : tags) {
                        if (found && (t instanceof SoundStreamBlockTag)) {
                            SoundStreamBlockTag bl = (SoundStreamBlockTag) t;
                            soundData = bl.streamSoundData.getRangeData();
                            break;
                        }
                        if (t == symbol) {
                            found = true;
                        }
                    }
                } else if (symbol instanceof DefineSoundTag) {
                    DefineSoundTag sound = (DefineSoundTag) symbol;
                    soundFormat = sound.soundFormat;
                    soundRate = sound.soundRate;
                    soundType = sound.soundType;
                    soundData = sound.soundData.getRangeData();
                    soundSize = sound.soundSize;
                    soundSampleCount = sound.soundSampleCount;
                }
                int format = 0;
                int bits = 0;
                if ((soundFormat == SoundFormat.FORMAT_ADPCM)
                        || (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN)
                        || (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN)) {
                    exportFormat = "wav";
                    if (soundType) { //stereo
                        format += 1;
                    }
                    switch (soundRate) {
                        case 0:
                            format += 2;
                            break;
                        case 1:
                            format += 6;
                            break;
                        case 2:
                            format += 10;
                            break;
                        case 3:
                            format += 14;
                            break;
                    }
                }
                if (soundFormat == SoundFormat.FORMAT_SPEEX) {
                    bits = 18;
                }
                if (soundFormat == SoundFormat.FORMAT_ADPCM) {
                    exportFormat = "wav";
                    try {
                        SWFInputStream sis = new SWFInputStream(swf, soundData);
                        int adpcmCodeSize = (int) sis.readUB(2, "adpcmCodeSize");
                        bits = 2 + adpcmCodeSize;
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                if (soundFormat == SoundFormat.FORMAT_MP3) {
                    exportFormat = "mp3";
                    if (!soundType) { //mono
                        format += 1;
                    }
                    format += 4; //quality best
                    try {
                        MP3SOUNDDATA s = new MP3SOUNDDATA(new SWFInputStream(swf, soundData), false);
                        //sis.readSI16();
                        //MP3FRAME frame = new MP3FRAME(sis);
                        MP3FRAME frame = s.frames.get(0);
                        int bitRate = frame.getBitRate();

                        switch (bitRate) {
                            case 8:
                                bits = 6;
                                break;
                            case 16:
                                bits = 7;
                                break;
                            case 20:
                                bits = 8;
                                break;
                            case 24:
                                bits = 9;
                                break;
                            case 32:
                                bits = 10;
                                break;
                            case 48:
                                bits = 11;
                                break;
                            case 56:
                                bits = 12;
                                break;
                            case 64:
                                bits = 13;
                                break;
                            case 80:
                                bits = 14;
                                break;
                            case 112:
                                bits = 15;
                                break;
                            case 128:
                                bits = 16;
                                break;
                            case 160:
                                bits = 17;
                                break;

                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                SoundTag st = (SoundTag) symbol;
                SoundFormat fmt = st.getSoundFormat();
                byte[] data = SWFInputStream.BYTE_ARRAY_EMPTY;
                try {
                    data = new SoundExporter().exportSound(st, SoundExportMode.MP3_WAV);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                String symbolFile = "sound" + symbol.getCharacterId() + "." + exportFormat;
                files.put(symbolFile, data);
                String mediaLinkStr = "<DOMSoundItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp(swf) + "\" externalFileSize=\"" + data.length + "\"";
                mediaLinkStr += " href=\"" + symbolFile + "\"";
                mediaLinkStr += " format=\"";
                mediaLinkStr += rateMap[soundRate] + "kHz";
                mediaLinkStr += " " + (soundSize ? "16bit" : "8bit");
                mediaLinkStr += " " + (soundType ? "Stereo" : "Mono");
                mediaLinkStr += "\"";
                mediaLinkStr += " exportFormat=\"" + format + "\" exportBits=\"" + bits + "\" sampleCount=\"" + soundSampleCount + "\"";

                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    mediaLinkStr += " linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                }

                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    mediaLinkStr += " linkageIdentifier=\"" + xmlString(characterVariables.get(symbol.getCharacterId())) + "\"";
                }
                if (linkageExportForAS) {
                    mediaLinkStr += " linkageExportForAS=\"true\"";
                }

                mediaLinkStr += "/>\n";
                media.add(mediaLinkStr);

            } else if (symbol instanceof DefineVideoStreamTag) {
                DefineVideoStreamTag video = (DefineVideoStreamTag) symbol;
                String videoType = "no media";
                switch (video.codecID) {
                    case 2:
                        videoType = "h263 media";
                        break;
                    case 3:
                        videoType = "screen share media";
                        break;
                    case 4:
                        videoType = "vp6 media";
                        break;
                    case 5:
                        videoType = "vp6 alpha media";
                        break;
                }

                byte[] data = SWFInputStream.BYTE_ARRAY_EMPTY;
                try {
                    data = new MovieExporter().exportMovie(video, MovieExportMode.FLV);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                String symbolFile = "movie" + symbol.getCharacterId() + "." + "flv";
                String mediaLinkStr = "";
                if (data.length == 0) { //Video has zero length, this probably means it is "Video - Actionscript-controlled"
                    long ts = getTimestamp(swf);
                    String datFileName = "M " + (datfiles.size() + 1) + " " + ts + ".dat";
                    mediaLinkStr = "<DOMVideoItem name=\"" + symbolFile + "\" sourceExternalFilepath=\"./LIBRARY/" + symbolFile + "\" sourceLastImported=\"" + ts + "\" videoDataHRef=\"" + datFileName + "\" channels=\"0\" isSpecial=\"true\" />";
                    //Use the dat file, otherwise it does not work
                    datfiles.put(datFileName, new byte[]{ //Magic numbers, if anybody knows why, please tell me
                        (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x59, (byte) 0x40, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
                    });
                } else {
                    files.put(symbolFile, data);
                    mediaLinkStr = "<DOMVideoItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp(swf) + "\" externalFileSize=\"" + data.length + "\"";
                    mediaLinkStr += " href=\"" + symbolFile + "\"";
                    mediaLinkStr += " videoType=\"" + videoType + "\"";
                    mediaLinkStr += " fps=\"" + (int) swf.frameRate + "\""; // todo: is the cast to in needed?
                    mediaLinkStr += " width=\"" + video.width + "\"";
                    mediaLinkStr += " height=\"" + video.height + "\"";
                    double len = (double) video.numFrames / swf.frameRate;
                    mediaLinkStr += " length=\"" + len + "\"";
                    boolean linkageExportForAS = false;
                    if (characterClasses.containsKey(symbol.getCharacterId())) {
                        linkageExportForAS = true;
                        mediaLinkStr += " linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                    }
                    if (characterVariables.containsKey(symbol.getCharacterId())) {
                        linkageExportForAS = true;
                        mediaLinkStr += " linkageIdentifier=\"" + xmlString(characterVariables.get(symbol.getCharacterId())) + "\"";
                    }
                    if (linkageExportForAS) {
                        mediaLinkStr += " linkageExportForAS=\"true\"";
                    }
                    mediaLinkStr += "/>\n";
                }
                media.add(mediaLinkStr);
            }

        }
        if (!media.isEmpty()) {
            ret.append("<media>");
            for (String m : media) {
                ret.append(m);
            }
            ret.append("</media>");
        }
        if (!symbols.isEmpty()) {
            ret.append("<symbols>");
            for (String s : symbols) {
                ret.append(s);
            }
            ret.append("</symbols>");
        }
    }

    private static String prettyFormatXML(String input) {
        int indent = 5;
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (TransformerFactoryConfigurationError | IllegalArgumentException | TransformerException e) {
            logger.log(Level.SEVERE, "Pretty print error", e);
            return input;
        }
    }

    private static void convertFrame(boolean shapeTween, HashMap<Integer, CharacterTag> characters, List<Tag> tags, SoundStreamHeadTypeTag soundStreamHead, StartSoundTag startSound, int frame, int duration, String actionScript, String elements, HashMap<String, byte[]> files, StringBuilder ret) {
        DefineSoundTag sound = null;
        if (startSound != null) {
            for (Tag t : tags) {
                if (t instanceof DefineSoundTag) {
                    DefineSoundTag s = (DefineSoundTag) t;
                    if (s.soundId == startSound.soundId) {
                        sound = s;
                        break;
                    }
                }
            }
        }

        ret.append("<DOMFrame index=\"").append(frame).append("\"");
        if (duration > 1) {
            ret.append(" duration=\"").append(duration).append("\"");
        }
        if (shapeTween) {
            ret.append(" tweenType=\"shape\" keyMode=\"").append(KEY_MODE_SHAPE_TWEEN).append("\"");
        } else {
            ret.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\"");
        }
        String soundEnvelopeStr = "";
        if (soundStreamHead != null && startSound == null) {
            String soundName = "sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat().toString().toLowerCase();
            ret.append(" soundName=\"").append(soundName).append("\"");
            ret.append(" soundSync=\"stream\"");
            soundEnvelopeStr += "<SoundEnvelope>";
            soundEnvelopeStr += "<SoundEnvelopePoint level0=\"32768\" level1=\"32768\"/>";
            soundEnvelopeStr += "</SoundEnvelope>";
        }
        if (startSound != null && sound != null) {
            String soundName = "sound" + sound.soundId + "." + sound.getExportFormat().toString().toLowerCase();
            ret.append(" soundName=\"").append(soundName).append("\"");
            if (startSound.soundInfo.hasInPoint) {
                ret.append(" inPoint44=\"").append(startSound.soundInfo.inPoint).append("\"");
            }
            if (startSound.soundInfo.hasOutPoint) {
                ret.append(" outPoint44=\"").append(startSound.soundInfo.outPoint).append("\"");
            }
            if (startSound.soundInfo.hasLoops) {
                if (startSound.soundInfo.loopCount == 32767) {
                    ret.append(" soundLoopMode=\"loop\"");
                }
                ret.append(" soundLoop=\"").append(startSound.soundInfo.loopCount).append("\"");
            }

            if (startSound.soundInfo.syncStop) {
                ret.append(" soundSync=\"stop\"");
            } else if (startSound.soundInfo.syncNoMultiple) {
                ret.append(" soundSync=\"start\"");
            }
            soundEnvelopeStr += "<SoundEnvelope>";
            if (startSound.soundInfo.hasEnvelope) {
                SOUNDENVELOPE[] envelopeRecords = startSound.soundInfo.envelopeRecords;
                for (SOUNDENVELOPE env : envelopeRecords) {
                    soundEnvelopeStr += "<SoundEnvelopePoint mark44=\"" + env.pos44 + "\" level0=\"" + env.leftLevel + "\" level1=\"" + env.rightLevel + "\"/>";
                }

                if (envelopeRecords.length == 1
                        && envelopeRecords[0].leftLevel == 32768
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 0) {
                    ret.append(" soundEffect=\"left channel\"");
                } else if (envelopeRecords.length == 1
                        && envelopeRecords[0].leftLevel == 0
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 32768) {
                    ret.append(" soundEffect=\"right channel\"");
                } else if (envelopeRecords.length == 2
                        && envelopeRecords[0].leftLevel == 32768
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 0
                        && envelopeRecords[1].leftLevel == 0
                        && envelopeRecords[1].pos44 == sound.soundSampleCount
                        && envelopeRecords[1].rightLevel == 32768) {
                    ret.append(" soundEffect=\"fade left to right\"");
                } else if (envelopeRecords.length == 2
                        && envelopeRecords[0].leftLevel == 0
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 32768
                        && envelopeRecords[1].leftLevel == 32768
                        && envelopeRecords[1].pos44 == sound.soundSampleCount
                        && envelopeRecords[1].rightLevel == 0) {
                    ret.append(" soundEffect=\"fade right to left\"");
                } else {
                    ret.append(" soundEffect=\"custom\"");
                }
                //TODO: fade in, fade out

            } else {
                soundEnvelopeStr += "<SoundEnvelopePoint level0=\"32768\" level1=\"32768\"/>";
            }
            soundEnvelopeStr += "</SoundEnvelope>";
        }
        ret.append(">");

        ret.append(soundEnvelopeStr);
        if (!actionScript.isEmpty()) {
            ret.append("<Actionscript><script><![CDATA[");
            ret.append(actionScript);
            ret.append("]]></script></Actionscript>");
        }
        ret.append("<elements>");
        ret.append(elements);
        ret.append("</elements>");
        ret.append("</DOMFrame>");
    }

    private static String convertVideoInstance(String instanceName, MATRIX matrix, DefineVideoStreamTag video, CLIPACTIONS clipActions) {
        StringBuilder ret = new StringBuilder();
        ret.append("<DOMVideoInstance libraryItemName=\"movie").append(video.characterID).append(".flv\" frameRight=\"").append(20 * video.width).append("\" frameBottom=\"").append(20 * video.height).append("\"");
        if (instanceName != null) {
            ret.append(" name=\"").append(xmlString(instanceName)).append("\"");
        }
        ret.append(">");
        ret.append("<matrix>");
        convertMatrix(matrix, ret);
        ret.append("</matrix>");
        ret.append("<transformationPoint>");
        ret.append("<Point />");
        ret.append("</transformationPoint>");
        ret.append("</DOMVideoInstance>");
        return ret.toString();
    }

    private static void convertFrames(String prevStr, String afterStr, List<Integer> nonLibraryShapes, List<Tag> tags, List<Tag> timelineTags, HashMap<Integer, CharacterTag> characters, int depth, FLAVersion flaVersion, HashMap<String, byte[]> files, StringBuilder ret) {
        StringBuilder ret2 = new StringBuilder();
        prevStr += "<frames>";
        int frame = -1;
        String elements;
        String lastElements = "";

        int duration = 1;

        CharacterTag character = null;
        MATRIX matrix = null;
        String instanceName = null;
        ColorTransform colorTransForm = null;
        boolean cacheAsBitmap = false;
        int blendMode = 0;
        List<FILTER> filters = new ArrayList<>();
        boolean isVisible = true;
        RGBA backGroundColor = null;
        CLIPACTIONS clipActions = null;
        int characterId = -1;
        int ratio = -1;
        boolean shapeTween = false;
        boolean lastShapeTween = false;
        MorphShapeTag shapeTweener = null;

        //Add ShowFrameTag to the end when there is one last missing
        List<Tag> timTags = new ArrayList<>(timelineTags);
        boolean needsFrameAdd = false;
        SWF swf = null;
        for (int i = timTags.size() - 1; i >= 0; i--) {
            if (timTags.get(i) instanceof ShowFrameTag) {
                break;
            }
            if (timTags.get(i) instanceof PlaceObjectTypeTag) {
                needsFrameAdd = true;
                swf = timTags.get(i).getSwf();
                break;
            }
        }
        if (needsFrameAdd) {
            timTags.add(new ShowFrameTag(swf));
        }

        for (Tag t : timTags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                if (po.getDepth() == depth) {
                    int newCharId = po.getCharacterId();
                    if (newCharId == -1) {
                        newCharId = characterId;
                    }
                    characterId = newCharId;
                    if (characters.containsKey(characterId)) {
                        character = characters.get(characterId);
                        if (po.flagMove()) {
                            MATRIX matrix2 = po.getMatrix();
                            if (matrix2 != null) {
                                matrix = matrix2;
                            }
                            String instanceName2 = po.getInstanceName();
                            if (instanceName2 != null) {
                                instanceName = instanceName2;
                            }
                            ColorTransform colorTransForm2 = po.getColorTransform();
                            if (colorTransForm2 != null) {
                                colorTransForm = colorTransForm2;
                            }

                            CLIPACTIONS clipActions2 = po.getClipActions();
                            if (clipActions2 != null) {
                                clipActions = clipActions2;
                            }
                            if (po.cacheAsBitmap()) {
                                cacheAsBitmap = true;
                            }
                            int blendMode2 = po.getBlendMode();
                            if (blendMode2 > 0) {
                                blendMode = blendMode2;
                            }
                            List<FILTER> filters2 = po.getFilters();
                            if (filters2 != null) {
                                filters = filters2;
                            }
                            int ratio2 = po.getRatio();
                            if (ratio2 > -1) {
                                ratio = ratio2;
                            }
                        } else {
                            matrix = po.getMatrix();
                            instanceName = po.getInstanceName();
                            colorTransForm = po.getColorTransform();
                            cacheAsBitmap = po.cacheAsBitmap();
                            blendMode = po.getBlendMode();
                            filters = po.getFilters();
                            ratio = po.getRatio();
                            clipActions = po.getClipActions();
                        }

                    }
                }
            }

            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                if (rt.getDepth() == depth) {
                    if (shapeTween && character != null) {
                        MorphShapeTag m = (MorphShapeTag) character;
                        shapeTweener = m;
                        shapeTween = false;
                    }
                    character = null;
                    matrix = null;
                    instanceName = null;
                    colorTransForm = null;
                    cacheAsBitmap = false;
                    blendMode = 0;
                    filters = new ArrayList<>();
                    isVisible = true;
                    backGroundColor = null;
                    characterId = -1;
                    clipActions = null;
                }
            }

            if (t instanceof ShowFrameTag) {
                if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(characterId) || shapeTweener != null)) {
                    ShapeTag shape = (ShapeTag) character;
                    elements = convertShape(characters, matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false);
                    shapeTween = false;
                    shapeTweener = null;
                } else if (character != null) {
                    if (character instanceof MorphShapeTag) {
                        MorphShapeTag m = (MorphShapeTag) character;
                        elements = convertShape(characters, matrix, 3, m.getStartEdges().shapeRecords, m.getFillStyles().getStartFillStyles(), m.getLineStyles().getStartLineStyles(m.getShapeNum()), true, false);
                        shapeTween = true;
                    } else {
                        shapeTween = false;
                        if (character instanceof TextTag) {
                            elements = convertText(instanceName, (TextTag) character, matrix, filters, clipActions);
                        } else if (character instanceof DefineVideoStreamTag) {
                            elements = convertVideoInstance(instanceName, matrix, (DefineVideoStreamTag) character, clipActions);
                        } else {
                            elements = convertSymbolInstance(instanceName, matrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, character, characters, tags, flaVersion);
                        }
                    }
                } else {
                    elements = "";
                }

                frame++;
                if (!elements.equals(lastElements) && frame > 0) {
                    convertFrame(lastShapeTween, characters, tags, null, null, frame - duration, duration, "", lastElements, files, ret2);
                    duration = 1;
                } else if (frame == 0) {
                    duration = 1;
                } else {
                    duration++;
                }

                lastShapeTween = shapeTween;
                lastElements = elements;
            }
        }
        if (!lastElements.isEmpty()) {
            frame++;
            convertFrame(lastShapeTween, characters, tags, null, null, (frame - duration < 0 ? 0 : frame - duration), duration, "", lastElements, files, ret2);
        }
        afterStr = "</frames>" + afterStr;

        if (ret2.length() > 0) {
            ret.append(prevStr).append(ret2).append(afterStr);
        }
    }

    private static void convertFonts(List<Tag> tags, StringBuilder ret) {
        StringBuilder ret2 = new StringBuilder();
        for (Tag t : tags) {
            if (t instanceof FontTag) {
                FontTag font = (FontTag) t;
                int fontId = font.getFontId();
                String fontName = null;
                for (Tag t2 : tags) {
                    if (t2 instanceof DefineFontNameTag) {
                        if (((DefineFontNameTag) t2).fontId == fontId) {
                            fontName = ((DefineFontNameTag) t2).fontName;
                        }
                    }
                }
                if (fontName == null) {
                    fontName = font.getFontNameIntag();
                }
                if (fontName == null) {
                    fontName = FontTag.defaultFontName;
                }
                int fontStyle = font.getFontStyle();
                String installedFont;
                if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                    fontName = new Font(installedFont, fontStyle, 10).getPSName();
                }
                String embedRanges = "";

                String fontChars = font.getCharacters(tags);
                if ("".equals(fontChars)) {
                    continue;
                }
                String embeddedCharacters = fontChars;
                embeddedCharacters = embeddedCharacters.replace("\u00A0", ""); //nonbreak space
                embeddedCharacters = embeddedCharacters.replace(".", "");
                boolean hasAllRanges = false;
                for (int r = 0; r < CharacterRanges.rangeCount(); r++) {
                    int[] codes = CharacterRanges.rangeCodes(r);
                    boolean hasAllInRange = true;
                    for (int i = 0; i < codes.length; i++) {
                        if (!fontChars.contains("" + (char) codes[i])) {
                            hasAllInRange = false;
                            break;
                        }
                    }
                    if (hasAllInRange) {
                        //remove all found characters
                        for (int i = 0; i < codes.length; i++) {
                            embeddedCharacters = embeddedCharacters.replace("" + (char) codes[i], "");
                        }
                        if (!"".equals(embedRanges)) {
                            embedRanges += "|";
                        }
                        embedRanges += (r + 1);
                    } else {
                        hasAllRanges = false;
                    }
                }
                if (hasAllRanges) {
                    embedRanges = "9999";
                }
                ret2.append("<DOMFontItem name=\"Font ").append(fontId).append("\" font=\"").append(xmlString(fontName)).append("\" size=\"0\" id=\"").append(fontId).append("\" embedRanges=\"").append(embedRanges).append("\"").append(!"".equals(embeddedCharacters) ? " embeddedCharacters=\"" + xmlString(embeddedCharacters) + "\"" : "").append(" />");
            }

        }

        if (ret2.length() > 0) {
            ret.append("<fonts>").append(ret2).append("</fonts>");
        }
    }

    private static String convertActionScriptLayer(int spriteId, List<Tag> tags, List<Tag> timeLineTags, String backgroundColor) {
        StringBuilder ret = new StringBuilder();

        String script = "";
        int duration = 0;
        int frame = 0;
        for (Tag t : tags) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag dia = (DoInitActionTag) t;
                if (dia.spriteId == spriteId) {
                    script += convertActionScript(dia);
                }
            }
        }
        if (!script.isEmpty()) {
            script = "#initclip\r\n" + script + "#endinitclip\r\n";
        }
        for (Tag t : timeLineTags) {
            if (t instanceof DoActionTag) {
                DoActionTag da = (DoActionTag) t;
                script += convertActionScript(da);
            }
            if (t instanceof ShowFrameTag) {

                if (script.isEmpty()) {
                    duration++;
                } else {
                    if (duration > 0) {
                        ret.append("<DOMFrame index=\"").append(frame - duration).append("\"");
                        if (duration > 1) {
                            ret.append(" duration=\"").append(duration).append("\"");
                        }
                        ret.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\">");
                        ret.append("<elements>");
                        ret.append("</elements>");
                        ret.append("</DOMFrame>");
                    }
                    ret.append("<DOMFrame index=\"").append(frame).append("\"");
                    ret.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\">");
                    ret.append("<Actionscript><script><![CDATA[");
                    ret.append(script);
                    ret.append("]]></script></Actionscript>");
                    ret.append("<elements>");
                    ret.append("</elements>");
                    ret.append("</DOMFrame>");
                    script = "";
                    duration = 0;
                }
                frame++;
            }
        }
        String retStr = ret.toString();
        if (!retStr.isEmpty()) {
            retStr = "<DOMLayer name=\"Script Layer\" color=\"" + randomOutlineColor() + "\">"
                    + "<frames>"
                    + retStr
                    + "</frames>"
                    + "</DOMLayer>";
        }
        return retStr;
    }

    private static String convertLabelsLayer(int spriteId, List<Tag> tags, List<Tag> timeLineTags, String backgroundColor) {
        StringBuilder ret = new StringBuilder();
        int duration = 0;
        int frame = 0;
        String frameLabel = "";
        boolean isAnchor = false;
        for (Tag t : timeLineTags) {
            if (t instanceof FrameLabelTag) {
                FrameLabelTag fl = (FrameLabelTag) t;
                frameLabel = fl.getLabelName();
                isAnchor = fl.isNamedAnchor();
            }
            if (t instanceof ShowFrameTag) {

                if (frameLabel.isEmpty()) {
                    duration++;
                } else {
                    if (duration > 0) {
                        ret.append("<DOMFrame index=\"").append(frame - duration).append("\"");
                        if (duration > 1) {
                            ret.append(" duration=\"").append(duration).append("\"");
                        }
                        ret.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\">");
                        ret.append("<elements>");
                        ret.append("</elements>");
                        ret.append("</DOMFrame>");
                    }
                    ret.append("<DOMFrame index=\"").append(frame).append("\"");
                    ret.append(" keyMode=\"").append(KEY_MODE_NORMAL).append("\"");
                    ret.append(" name=\"").append(frameLabel).append("\"");
                    if (isAnchor) {
                        ret.append(" labelType=\"anchor\" bookmark=\"true\"");
                    } else {
                        ret.append(" labelType=\"name\"");
                    }
                    ret.append(">");
                    ret.append("<elements>");
                    ret.append("</elements>");
                    ret.append("</DOMFrame>");
                    frameLabel = "";
                    duration = 0;
                }
                frame++;
            }
        }
        String retStr = ret.toString();
        if (!retStr.isEmpty()) {
            retStr = "<DOMLayer name=\"Labels Layer\" color=\"" + randomOutlineColor() + "\">"
                    + "<frames>"
                    + retStr
                    + "</frames>"
                    + "</DOMLayer>";
        }
        return retStr;
    }

    private static void convertSoundLayer(int layerIndex, String backgroundColor, HashMap<Integer, CharacterTag> characters, List<Tag> tags, List<Tag> timeLineTags, HashMap<String, byte[]> files, StringBuilder ret) {
        StringBuilder ret2 = new StringBuilder();
        StartSoundTag lastStartSound = null;
        SoundStreamHeadTypeTag lastSoundStreamHead = null;
        StartSoundTag startSound = null;
        SoundStreamHeadTypeTag soundStreamHead = null;
        int duration = 1;
        int frame = 0;
        for (Tag t : timeLineTags) {
            if (t instanceof StartSoundTag) {
                startSound = (StartSoundTag) t;

                for (Tag ta : tags) {
                    if (ta instanceof DefineSoundTag) {
                        DefineSoundTag s = (DefineSoundTag) ta;
                        if (s.soundId == startSound.soundId) {
                            if (!files.containsKey("sound" + s.soundId + "." + s.getExportFormat().toString().toLowerCase())) { //Sound was not exported
                                startSound = null; // ignore
                            }
                            break;
                        }
                    }
                }

            }
            if (t instanceof SoundStreamHeadTypeTag) {
                soundStreamHead = (SoundStreamHeadTypeTag) t;
                if (!files.containsKey("sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat().toString().toLowerCase())) { //Sound was not exported
                    soundStreamHead = null; // ignore
                }
            }
            if (t instanceof ShowFrameTag) {
                if (soundStreamHead != null || startSound != null) {
                    if (lastSoundStreamHead != null || lastStartSound != null) {
                        convertFrame(false, characters, tags, lastSoundStreamHead, lastStartSound, frame, duration, "", "", files, ret2);
                    }
                    frame += duration;
                    duration = 1;
                    lastSoundStreamHead = soundStreamHead;
                    lastStartSound = startSound;
                    soundStreamHead = null;
                    startSound = null;
                } else {
                    duration++;
                }
            }
        }
        if (lastSoundStreamHead != null || lastStartSound != null) {
            if (frame < 0) {
                frame = 0;
                duration = 1;
            }
            convertFrame(false, characters, tags, lastSoundStreamHead, lastStartSound, frame, duration, "", "", files, ret2);
        }

        if (ret2.length() > 0) {
            ret.append("<DOMLayer name=\"Layer ").append(layerIndex).append("\" color=\"").append(randomOutlineColor()).append("\">"
                    + "<frames>").append(ret2).append("</frames>"
                            + "</DOMLayer>");
        }
    }

    private static String randomOutlineColor() {
        RGB outlineColor = new RGB();
        do {
            outlineColor.red = random.nextInt(256);
            outlineColor.green = random.nextInt(256);
            outlineColor.blue = random.nextInt(256);
        } while ((outlineColor.red + outlineColor.green + outlineColor.blue) / 3 < 128);
        return outlineColor.toHexRGB();
    }

    private static String convertTimeline(int spriteId, List<Integer> nonLibraryShapes, String backgroundColor, List<Tag> tags, List<Tag> timelineTags, HashMap<Integer, CharacterTag> characters, String name, FLAVersion flaVersion, HashMap<String, byte[]> files) {
        StringBuilder ret = new StringBuilder();
        ret.append("<DOMTimeline name=\"").append(name).append("\">");
        ret.append("<layers>");

        String labelsLayer = convertLabelsLayer(spriteId, tags, timelineTags, backgroundColor);
        ret.append(labelsLayer);
        String scriptLayer = convertActionScriptLayer(spriteId, tags, timelineTags, backgroundColor);
        ret.append(scriptLayer);

        int index = 0;

        if (!labelsLayer.isEmpty()) {
            index++;
        }

        if (!scriptLayer.isEmpty()) {
            index++;
        }

        int layerCount = getLayerCount(timelineTags);
        Stack<Integer> parentLayers = new Stack<>();

        for (int d = layerCount; d >= 1; d--, index++) {
            for (Tag t : timelineTags) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                    if (po.getClipDepth() == d) {
                        for (int m = po.getDepth(); m < po.getClipDepth(); m++) {
                            parentLayers.push(index);
                        }

                        ret.append("<DOMLayer name=\"Layer ").append(index + 1).append("\" color=\"").append(randomOutlineColor()).append("\" ");
                        ret.append(" layerType=\"mask\" locked=\"true\"");
                        ret.append(">");
                        convertFrames("", "", nonLibraryShapes, tags, timelineTags, characters, po.getDepth(), flaVersion, files, ret);
                        ret.append("</DOMLayer>");
                        index++;
                        break;
                    }
                }
            }

            boolean hasClipDepth = false;
            for (Tag t : timelineTags) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                    if (po.getDepth() == d) {
                        if (po.getClipDepth() != -1) {
                            hasClipDepth = true;
                            break;
                        }
                    }
                }
            }
            if (hasClipDepth) {
                index--;
                continue;
            }
            int parentLayer = -1;
            if (!parentLayers.isEmpty()) {
                parentLayer = parentLayers.pop();
            }
            String layerPrev = "";

            layerPrev += "<DOMLayer name=\"Layer " + (index + 1) + "\" color=\"" + randomOutlineColor() + "\" ";
            if (d == 1) {
                layerPrev += " current=\"true\" isSelected=\"true\"";
            }
            if (parentLayer != -1) {
                if (parentLayer != d) {
                    layerPrev += " parentLayerIndex=\"" + (parentLayer) + "\" locked=\"true\"";
                }
            }
            layerPrev += ">";
            String layerAfter = "</DOMLayer>";
            int prevLength = ret.length();
            convertFrames(layerPrev, layerAfter, nonLibraryShapes, tags, timelineTags, characters, d, flaVersion, files, ret);
            if (ret.length() == prevLength) {
                index--;
            }
        }

        int soundLayerIndex = layerCount;
        layerCount++;
        convertSoundLayer(soundLayerIndex, backgroundColor, characters, tags, timelineTags, files, ret);
        ret.append("</layers>");
        ret.append("</DOMTimeline>");
        return ret.toString();
    }

    private static void writeFile(AbortRetryIgnoreHandler handler, final byte[] data, final String file) throws IOException, InterruptedException {
        new RetryTask(() -> {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
        }, handler).run();
    }

    private static Map<Integer, String> getCharacterClasses(List<Tag> tags) {
        Map<Integer, String> ret = new HashMap<>();
        for (Tag t : tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tags.size(); i++) {
                    if (!ret.containsKey(sc.tags.get(i)) && !ret.containsValue(sc.names.get(i))) {
                        ret.put(sc.tags.get(i), sc.names.get(i));
                    }
                }
            }
        }
        return ret;
    }

    private static Map<Integer, String> getCharacterVariables(List<Tag> tags) {
        Map<Integer, String> ret = new HashMap<>();
        for (Tag t : tags) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag ea = (ExportAssetsTag) t;
                for (int i = 0; i < ea.tags.size(); i++) {
                    if (!ret.containsKey(ea.tags.get(i))) {
                        ret.put(ea.tags.get(i), ea.names.get(i));
                    }
                }
            }
        }
        return ret;
    }

    private static String convertText(String instanceName, TextTag tag, MATRIX m, List<FILTER> filters, CLIPACTIONS clipActions) {
        StringBuilder ret = new StringBuilder();

        MATRIX matrix = new MATRIX(m);
        CSMTextSettingsTag csmts = null;
        StringBuilder filterStr = new StringBuilder();
        if (filters != null) {
            filterStr.append("<filters>");
            for (FILTER f : filters) {
                convertFilter(f, ret);
            }
            filterStr.append("</filters>");
        }

        SWF swf = tag.getSwf();
        for (Tag t : swf.tags) {
            if (t instanceof CSMTextSettingsTag) {
                CSMTextSettingsTag c = (CSMTextSettingsTag) t;
                if (c.textID == tag.getCharacterId()) {
                    csmts = c;
                    break;
                }
            }
        }

        String fontRenderingMode = "standard";
        String antiAlias = "";
        if (csmts != null) {
            if (csmts.thickness == 0 & csmts.sharpness == 0) {
                fontRenderingMode = null;
            } else {
                fontRenderingMode = "customThicknessSharpness";
            }
            antiAlias = " antiAliasSharpness=\"" + doubleToString(csmts.sharpness) + "\" antiAliasThickness=\"" + doubleToString(csmts.thickness) + "\"";
        }
        String left = "";
        RECT bounds = tag.getBounds();
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            MATRIX textMatrix = tag.getTextMatrix();
            left = " left=\"" + doubleToString((textMatrix.translateX) / SWF.unitDivisor) + "\"";
        }
        StringBuilder matStr = new StringBuilder();
        matStr.append("<matrix>");
        convertMatrix(matrix, matStr);
        matStr.append("</matrix>");
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            List<TEXTRECORD> textRecords = new ArrayList<>();
            if (tag instanceof DefineTextTag) {
                textRecords = ((DefineTextTag) tag).textRecords;
            } else if (tag instanceof DefineText2Tag) {
                textRecords = ((DefineText2Tag) tag).textRecords;
            }

            for (TEXTRECORD rec : textRecords) {
                if (rec.styleFlagsHasFont) {
                    FontTag ft = swf.getFont(rec.fontId);
                    if (ft != null && ft.isSmall()) {
                        fontRenderingMode = "bitmap";
                        break;
                    }
                }
            }

            ret.append("<DOMStaticText");
            ret.append(left);
            if (fontRenderingMode != null) {
                ret.append(" fontRenderingMode=\"").append(fontRenderingMode).append("\"");
            }
            if (instanceName != null) {
                ret.append(" instanceName=\"").append(xmlString(instanceName)).append("\"");
            }
            ret.append(antiAlias);
            Map<String, Object> attrs = TextTag.getTextRecordsAttributes(textRecords, swf);

            ret.append(" width=\"").append(tag.getBounds().getWidth() / 2).append("\" height=\"").append(tag.getBounds().getHeight()).append("\" autoExpand=\"true\" isSelectable=\"false\">");
            ret.append(matStr);

            ret.append("<textRuns>");
            int fontId = -1;
            FontTag font = null;
            String fontName = null;
            String psFontName = null;
            int textHeight = -1;
            RGB textColor = null;
            RGBA textColorA = null;
            boolean newline = false;
            boolean firstRun = true;
            @SuppressWarnings("unchecked")
            List<Integer> leftMargins = (List<Integer>) attrs.get("allLeftMargins");
            @SuppressWarnings("unchecked")
            List<Integer> letterSpacings = (List<Integer>) attrs.get("allLetterSpacings");
            for (int r = 0; r < textRecords.size(); r++) {
                TEXTRECORD rec = textRecords.get(r);
                if (rec.styleFlagsHasColor) {
                    if (tag instanceof DefineTextTag) {
                        textColor = rec.textColor;
                    } else {
                        textColorA = rec.textColorA;
                    }
                }
                if (rec.styleFlagsHasFont) {
                    fontId = rec.fontId;
                    fontName = null;
                    textHeight = rec.textHeight;
                    font = swf.getFont(fontId);
                    for (Tag t : swf.tags) {
                        if (t instanceof DefineFontNameTag) {
                            if (((DefineFontNameTag) t).fontId == fontId) {
                                fontName = ((DefineFontNameTag) t).fontName;
                            }
                        }
                    }
                    if ((fontName == null) && (font != null)) {
                        fontName = font.getFontNameIntag();
                    }
                    if (fontName == null) {
                        fontName = FontTag.defaultFontName;
                    }
                    int fontStyle = 0;
                    if (font != null) {
                        fontStyle = font.getFontStyle();
                    }
                    String installedFont;
                    if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                        psFontName = new Font(installedFont, fontStyle, 10).getPSName();
                    } else {
                        psFontName = fontName;
                    }
                }
                newline = false;
                if (!firstRun && rec.styleFlagsHasYOffset) {
                    newline = true;
                }
                firstRun = false;
                if (font != null) {
                    ret.append("<DOMTextRun>");
                    ret.append("<characters>").append(xmlString((newline ? "\r" : "") + rec.getText(font))).append("</characters>");
                    ret.append("<textAttrs>");

                    ret.append("<DOMTextAttrs aliasText=\"false\" rotation=\"true\" size=\"").append(twipToPixel(textHeight)).append("\" bitmapSize=\"").append(textHeight).append("\"");
                    ret.append(" letterSpacing=\"").append(doubleToString(twipToPixel(letterSpacings.get(r)))).append("\"");
                    ret.append(" indent=\"").append(doubleToString(twipToPixel((int) attrs.get("indent")))).append("\"");
                    ret.append(" leftMargin=\"").append(doubleToString(twipToPixel(leftMargins.get(r)))).append("\"");
                    ret.append(" lineSpacing=\"").append(doubleToString(twipToPixel((int) attrs.get("lineSpacing")))).append("\"");
                    ret.append(" rightMargin=\"").append(doubleToString(twipToPixel((int) attrs.get("rightMargin")))).append("\"");

                    if (textColor != null) {
                        ret.append(" fillColor=\"").append(textColor.toHexRGB()).append("\"");
                    } else if (textColorA != null) {
                        ret.append(" fillColor=\"").append(textColorA.toHexRGB()).append("\" alpha=\"").append(textColorA.getAlphaFloat()).append("\"");
                    }
                    ret.append(" face=\"").append(psFontName).append("\"");
                    ret.append("/>");

                    ret.append("</textAttrs>");
                    ret.append("</DOMTextRun>");
                }
            }
            ret.append("</textRuns>");
            ret.append(filterStr);
            ret.append("</DOMStaticText>");
        } else if (tag instanceof DefineEditTextTag) {
            DefineEditTextTag det = (DefineEditTextTag) tag;
            String tagName;
            FontTag ft = swf.getFont(det.fontId);
            if (ft != null && ft.isSmall()) {
                fontRenderingMode = "bitmap";
            }
            if (!det.useOutlines) {
                fontRenderingMode = "device";
            }
            if (det.wasStatic) {
                tagName = "DOMStaticText";
            } else if (det.readOnly) {
                tagName = "DOMDynamicText";
            } else {
                tagName = "DOMInputText";
            }
            ret.append("<").append(tagName);
            if (fontRenderingMode != null) {
                ret.append(" fontRenderingMode=\"").append(fontRenderingMode).append("\"");
            }
            if (instanceName != null) {
                ret.append(" name=\"").append(xmlString(instanceName)).append("\"");
            }
            ret.append(antiAlias);
            double width = twipToPixel(bounds.getWidth());
            double height = twipToPixel(bounds.getHeight());
            //There is usually 4px difference between width/height and XML width/height
            //If somebody knows what that means, tell me
            double padding = 2;
            width -= 2 * padding;
            height -= 2 * padding;
            if (det.hasLayout) {
                width -= twipToPixel(det.rightMargin);
                width -= twipToPixel(det.leftMargin);
            }
            ret.append(" width=\"").append(width).append("\"");
            ret.append(" height=\"").append(height).append("\"");
            if (det.border) {
                ret.append("  border=\"true\"");
            }
            if (det.html) {
                ret.append(" renderAsHTML=\"true\"");
            }
            if (det.noSelect) {
                ret.append(" isSelectable=\"false\"");
            }
            if (det.multiline && det.wordWrap) {
                ret.append(" lineType=\"multiline\"");
            } else if (det.multiline && (!det.wordWrap)) {
                ret.append(" lineType=\"multiline no wrap\"");
            } else if (det.password) {
                ret.append(" lineType=\"password\"");
            }
            if (det.hasMaxLength) {
                ret.append(" maxCharacters=\"").append(det.maxLength).append("\"");
            }
            if (!det.variableName.isEmpty()) {
                ret.append(" variableName=\"").append(det.variableName).append("\"");
            }
            ret.append(">");
            ret.append(matStr);
            ret.append("<textRuns>");
            String txt = "";
            if (det.hasText) {
                txt = det.initialText;
            }

            if (det.html) {
                ret.append(convertHTMLText(swf.tags, det, txt));
            } else {
                ret.append("<DOMTextRun>");
                ret.append("<characters>").append(xmlString(txt)).append("</characters>");
                int leftMargin = -1;
                int rightMargin = -1;
                int indent = -1;
                int lineSpacing = -1;
                String alignment = null;
                boolean italic = false;
                boolean bold = false;
                String fontFace = null;
                int size = -1;
                RGBA textColor = null;
                if (det.hasTextColor) {
                    textColor = det.textColor;
                }
                if (det.hasFont) {
                    String fontName = null;
                    for (Tag u : swf.tags) {
                        if (u instanceof DefineFontNameTag) {
                            if (((DefineFontNameTag) u).fontId == det.fontId) {
                                fontName = ((DefineFontNameTag) u).fontName;
                            }
                        }
                        if (fontName != null && ft != null) {
                            break;
                        }
                    }
                    if (ft != null) {
                        if (fontName == null) {
                            fontName = ft.getFontNameIntag();
                        }
                        if (fontName == null) {
                            fontName = FontTag.defaultFontName;
                        }
                        italic = ft.isItalic();
                        bold = ft.isBold();
                        size = det.fontHeight;
                        fontFace = fontName;
                        String installedFont = null;
                        if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                            fontName = installedFont;
                            fontFace = new Font(installedFont, (italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0) | (!italic && !bold ? Font.PLAIN : 0), size < 0 ? 10 : size).getPSName();
                        }

                    }
                }
                if (det.hasLayout) {
                    leftMargin = det.leftMargin;
                    rightMargin = det.rightMargin;
                    indent = det.indent;
                    lineSpacing = det.leading;
                    String[] alignNames = {"left", "right", "center", "justify"};
                    if (det.align < alignNames.length) {
                        alignment = alignNames[det.align];
                    } else {
                        alignment = "unknown";
                    }
                }
                ret.append("<textAttrs>");
                ret.append("<DOMTextAttrs");
                if (alignment != null) {
                    ret.append(" alignment=\"").append(alignment).append("\"");
                }
                ret.append(" rotation=\"true\""); //?
                if (indent > -1) {
                    ret.append(" indent=\"").append(twipToPixel(indent)).append("\"");
                }
                if (leftMargin > -1) {
                    ret.append(" leftMargin=\"").append(twipToPixel(leftMargin)).append("\"");
                }
                if (lineSpacing > -1) {
                    ret.append(" lineSpacing=\"").append(twipToPixel(lineSpacing)).append("\"");
                }
                if (rightMargin > -1) {
                    ret.append(" rightMargin=\"").append(twipToPixel(rightMargin)).append("\"");
                }
                if (size > -1) {
                    ret.append(" size=\"").append(twipToPixel(size)).append("\"");
                    ret.append(" bitmapSize=\"").append(size).append("\"");
                }
                if (fontFace != null) {
                    ret.append(" face=\"").append(fontFace).append("\"");
                }
                if (textColor != null) {
                    ret.append(" fillColor=\"").append(textColor.toHexRGB()).append("\" alpha=\"").append(textColor.getAlphaFloat()).append("\"");
                }
                ret.append("/>");
                ret.append("</textAttrs>");
                ret.append("</DOMTextRun>");
            }
            ret.append("</textRuns>");
            ret.append(filterStr);
            ret.append("</").append(tagName).append(">");
        }
        return ret.toString();
    }

    public static void convertSWF(AbortRetryIgnoreHandler handler, SWF swf, String swfFileName, String outfile, boolean compressed, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion flaVersion) throws IOException, InterruptedException {

        FileAttributesTag fa = swf.getFileAttributes();

        boolean useAS3 = false;
        boolean useNetwork = false;
        if (fa != null) {
            useAS3 = fa.actionScript3;
            useNetwork = fa.useNetwork;
        }

        if (!useAS3 && flaVersion.minASVersion() > 2) {
            throw new IllegalArgumentException("FLA version " + flaVersion + " does not support AS1/2");
        }
        File file = new File(outfile);
        File outDir = file.getParentFile();
        Path.createDirectorySafe(outDir);
        StringBuilder domDocument = new StringBuilder();
        String baseName = swfFileName;
        File f = new File(baseName);
        baseName = f.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }
        final HashMap<String, byte[]> files = new HashMap<>();
        final HashMap<String, byte[]> datfiles = new HashMap<>();
        HashMap<Integer, CharacterTag> characters = getCharacters(swf.tags);
        List<Integer> nonLibraryShapes = getNonLibraryShapes(swf.tags, characters);
        Map<Integer, String> characterClasses = getCharacterClasses(swf.tags);
        Map<Integer, String> characterVariables = getCharacterVariables(swf.tags);

        String backgroundColor = "#ffffff";
        for (Tag t : swf.tags) {
            if (t instanceof SetBackgroundColorTag) {
                SetBackgroundColorTag sbc = (SetBackgroundColorTag) t;
                backgroundColor = sbc.backgroundColor.toHexRGB();
            }
        }
        domDocument.append("<DOMDocument xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" currentTimeline=\"1\" xflVersion=\"").append(flaVersion.xflVersion()).append("\" creatorInfo=\"").append(generator).append("\" platform=\"Windows\" versionInfo=\"Saved by ").append(generatorVerName).append("\" majorVersion=\"").append(generatorVersion).append("\" buildNumber=\"\" nextSceneIdentifier=\"2\" playOptionsPlayLoop=\"false\" playOptionsPlayPages=\"false\" playOptionsPlayFrameActions=\"false\" autoSaveHasPrompted=\"true\"");
        domDocument.append(" backgroundColor=\"").append(backgroundColor).append("\"");
        domDocument.append(" frameRate=\"").append((int) swf.frameRate).append("\"");

        double width = twipToPixel(swf.displayRect.getWidth());
        double height = twipToPixel(swf.displayRect.getHeight());
        if (Double.compare(width, 550) != 0) {
            domDocument.append(" width=\"").append(doubleToString(width)).append("\"");
        }
        if (Double.compare(height, 400) != 0) {
            domDocument.append(" height=\"").append(doubleToString(height)).append("\"");
        }
        domDocument.append(">");
        convertFonts(swf.tags, domDocument);
        convertLibrary(swf, characterVariables, characterClasses, nonLibraryShapes, backgroundColor, swf.tags, characters, files, datfiles, flaVersion, domDocument);
        domDocument.append("<timelines>");
        domDocument.append(convertTimeline(0, nonLibraryShapes, backgroundColor, swf.tags, swf.tags, characters, "Scene 1", flaVersion, files));
        domDocument.append("</timelines>");
        domDocument.append("</DOMDocument>");
        String domDocumentStr = prettyFormatXML(domDocument.toString());

        for (Tag t : swf.tags) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag dia = (DoInitActionTag) t;
                int chid = dia.getCharacterId();
                if (characters.containsKey(chid)) {
                    if (characters.get(chid) instanceof DefineSpriteTag) {
                        DefineSpriteTag sprite = (DefineSpriteTag) characters.get(chid);
                        if (sprite.subTags.isEmpty()) {
                            String data = convertActionScript(dia);
                            String expName = dia.getSwf().getExportName(dia.spriteId);
                            expName = expName != null ? expName : "_unk_";
                            String expPath = expName;
                            final String prefix = "__Packages.";
                            if (expPath.startsWith(prefix)) {
                                expPath = expPath.substring(prefix.length());
                            }
                            String expDir = "";
                            if (expPath.contains(".")) {
                                expDir = expPath.substring(0, expPath.lastIndexOf('.'));
                                expDir = expDir.replace(".", File.separator);
                            }
                            expPath = expPath.replace(".", File.separator);
                            File cdir = new File(outDir.getAbsolutePath() + File.separator + expDir);
                            Path.createDirectorySafe(cdir);
                            writeFile(handler, Utf8Helper.getBytes(data), outDir.getAbsolutePath() + File.separator + expPath + ".as");
                        }
                    }
                }
            }
        }

        int flaSwfVersion = swf.version > flaVersion.maxSwfVersion() ? flaVersion.maxSwfVersion() : swf.version;
        boolean greaterThanCC = flaVersion.ordinal() >= FLAVersion.CC.ordinal();
        StringBuilder publishSettings = new StringBuilder();
        publishSettings.append("<flash_profiles>\n");
        publishSettings.append("<flash_profile version=\"1.0\" name=\"Default\" current=\"true\">\n");
        publishSettings.append("  <PublishFormatProperties enabled=\"true\">\n");
        publishSettings.append("    <defaultNames>1</defaultNames>\n");
        publishSettings.append("    <flash>1</flash>\n");
        publishSettings.append("    <projectorWin>0</projectorWin>\n");
        publishSettings.append("    <projectorMac>0</projectorMac>\n");
        publishSettings.append("    <html>1</html>\n");
        publishSettings.append("    <gif>0</gif>\n");
        publishSettings.append("    <jpeg>0</jpeg>\n");
        publishSettings.append("    <png>0</png>\n");
        publishSettings.append(greaterThanCC ? "    <svg>0</svg>\n" : "    <qt>0</qt>\n");
        publishSettings.append("    <rnwk>0</rnwk>\n");
        publishSettings.append("    <swc>0</swc>\n");
        publishSettings.append("    <flashDefaultName>1</flashDefaultName>\n");
        publishSettings.append("    <projectorWinDefaultName>1</projectorWinDefaultName>\n");
        publishSettings.append("    <projectorMacDefaultName>1</projectorMacDefaultName>\n");
        publishSettings.append("    <htmlDefaultName>1</htmlDefaultName>\n");
        publishSettings.append("    <gifDefaultName>1</gifDefaultName>\n");
        publishSettings.append("    <jpegDefaultName>1</jpegDefaultName>\n");
        publishSettings.append("    <pngDefaultName>1</pngDefaultName>\n");
        publishSettings.append(greaterThanCC ? "    <svgDefaultName>1</svgDefaultName>\n" : "    <qtDefaultName>1</qtDefaultName>\n");
        publishSettings.append("    <rnwkDefaultName>1</rnwkDefaultName>\n");
        publishSettings.append("    <swcDefaultName>1</swcDefaultName>\n");
        publishSettings.append("    <flashFileName>").append(baseName).append(".swf</flashFileName>\n");
        publishSettings.append("    <projectorWinFileName>").append(baseName).append(".exe</projectorWinFileName>\n");
        publishSettings.append("    <projectorMacFileName>").append(baseName).append(".app</projectorMacFileName>\n");
        publishSettings.append("    <htmlFileName>").append(baseName).append(".html</htmlFileName>\n");
        publishSettings.append("    <gifFileName>").append(baseName).append(".gif</gifFileName>\n");
        publishSettings.append("    <jpegFileName>").append(baseName).append(".jpg</jpegFileName>\n");
        publishSettings.append("    <pngFileName>").append(baseName).append(".png</pngFileName>\n");
        publishSettings.append(greaterThanCC ? "    <svgFileName>1</svgFileName>\n" : "    <qtFileName>1</qtFileName>\n");
        publishSettings.append("    <rnwkFileName>").append(baseName).append(".smil</rnwkFileName>\n");
        publishSettings.append("    <swcFileName>").append(baseName).append(".swc</swcFileName>\n");
        publishSettings.append("  </PublishFormatProperties>\n");
        publishSettings.append("  <PublishHtmlProperties enabled=\"true\">\n");
        publishSettings.append("    <VersionDetectionIfAvailable>0</VersionDetectionIfAvailable>\n");
        publishSettings.append("    <VersionInfo>12,0,0,0;11,2,0,0;11,1,0,0;10,3,0,0;10,2,153,0;10,1,52,0;9,0,124,0;8,0,24,0;7,0,14,0;6,0,79,0;5,0,58,0;4,0,32,0;3,0,8,0;2,0,1,12;1,0,0,1;</VersionInfo>\n");
        publishSettings.append("    <UsingDefaultContentFilename>1</UsingDefaultContentFilename>\n");
        publishSettings.append("    <UsingDefaultAlternateFilename>1</UsingDefaultAlternateFilename>\n");
        publishSettings.append("    <ContentFilename>").append(baseName).append("_content.html</ContentFilename>\n");
        publishSettings.append("    <AlternateFilename>").append(baseName).append("_alternate.html</AlternateFilename>\n");
        publishSettings.append("    <UsingOwnAlternateFile>0</UsingOwnAlternateFile>\n");
        publishSettings.append("    <OwnAlternateFilename></OwnAlternateFilename>\n");
        publishSettings.append("    <Width>").append(width).append("</Width>\n");
        publishSettings.append("    <Height>").append(height).append("</Height>\n");
        publishSettings.append("    <Align>0</Align>\n");
        publishSettings.append("    <Units>0</Units>\n");
        publishSettings.append("    <Loop>1</Loop>\n");
        publishSettings.append("    <StartPaused>0</StartPaused>\n");
        publishSettings.append("    <Scale>0</Scale>\n");
        publishSettings.append("    <HorizontalAlignment>1</HorizontalAlignment>\n");
        publishSettings.append("    <VerticalAlignment>1</VerticalAlignment>\n");
        publishSettings.append("    <Quality>4</Quality>\n");
        publishSettings.append("    <DeblockingFilter>0</DeblockingFilter>\n");
        publishSettings.append("    <WindowMode>0</WindowMode>\n");
        publishSettings.append("    <DisplayMenu>1</DisplayMenu>\n");
        publishSettings.append("    <DeviceFont>0</DeviceFont>\n");
        publishSettings.append("    <TemplateFileName></TemplateFileName>\n");
        publishSettings.append("    <showTagWarnMsg>1</showTagWarnMsg>\n");
        publishSettings.append("  </PublishHtmlProperties>\n");
        publishSettings.append("  <PublishFlashProperties enabled=\"true\">\n");
        publishSettings.append("    <TopDown></TopDown>\n");
        publishSettings.append("    <FireFox></FireFox>\n");
        publishSettings.append("    <Report>0</Report>\n");
        publishSettings.append("    <Protect>0</Protect>\n");
        publishSettings.append("    <OmitTraceActions>0</OmitTraceActions>\n");
        publishSettings.append("    <Quality>80</Quality>\n");
        publishSettings.append("    <DeblockingFilter>0</DeblockingFilter>\n");
        publishSettings.append("    <StreamFormat>0</StreamFormat>\n");
        publishSettings.append("    <StreamCompress>7</StreamCompress>\n");
        publishSettings.append("    <EventFormat>0</EventFormat>\n");
        publishSettings.append("    <EventCompress>7</EventCompress>\n");
        publishSettings.append("    <OverrideSounds>0</OverrideSounds>\n");
        publishSettings.append("    <Version>").append(flaSwfVersion).append("</Version>\n");
        publishSettings.append("    <ExternalPlayer>").append(FLAVersion.swfVersionToPlayer(flaSwfVersion)).append("</ExternalPlayer>\n");
        publishSettings.append("    <ActionScriptVersion>").append(useAS3 ? "3" : "2").append("</ActionScriptVersion>\n");
        publishSettings.append("    <PackageExportFrame>1</PackageExportFrame>\n");
        publishSettings.append("    <PackagePaths></PackagePaths>\n");
        publishSettings.append("    <AS3PackagePaths>.</AS3PackagePaths>\n");
        publishSettings.append("    <AS3ConfigConst>CONFIG::FLASH_AUTHORING=&quot;true&quot;;</AS3ConfigConst>\n");
        publishSettings.append("    <DebuggingPermitted>0</DebuggingPermitted>\n");
        publishSettings.append("    <DebuggingPassword></DebuggingPassword>\n");
        publishSettings.append("    <CompressMovie>").append(swf.compression == SWFCompression.NONE ? "0" : "1").append("</CompressMovie>\n");
        publishSettings.append("    <CompressionType>").append(swf.compression == SWFCompression.LZMA ? "1" : "0").append("</CompressionType>\n");
        publishSettings.append("    <InvisibleLayer>1</InvisibleLayer>\n");
        publishSettings.append("    <DeviceSound>0</DeviceSound>\n");
        publishSettings.append("    <StreamUse8kSampleRate>0</StreamUse8kSampleRate>\n");
        publishSettings.append("    <EventUse8kSampleRate>0</EventUse8kSampleRate>\n");
        publishSettings.append("    <UseNetwork>").append(useNetwork ? 1 : 0).append("</UseNetwork>\n");
        publishSettings.append("    <DocumentClass>").append(xmlString(characterClasses.containsKey(0) ? characterClasses.get(0) : "")).append("</DocumentClass>\n");
        publishSettings.append("    <AS3Strict>2</AS3Strict>\n");
        publishSettings.append("    <AS3Coach>4</AS3Coach>\n");
        publishSettings.append("    <AS3AutoDeclare>4096</AS3AutoDeclare>\n");
        publishSettings.append("    <AS3Dialect>AS3</AS3Dialect>\n");
        publishSettings.append("    <AS3ExportFrame>1</AS3ExportFrame>\n");
        publishSettings.append("    <AS3Optimize>1</AS3Optimize>\n");
        publishSettings.append("    <ExportSwc>0</ExportSwc>\n");
        publishSettings.append("    <ScriptStuckDelay>15</ScriptStuckDelay>\n");
        publishSettings.append("    <IncludeXMP>1</IncludeXMP>\n");
        publishSettings.append("    <HardwareAcceleration>0</HardwareAcceleration>\n");
        publishSettings.append("    <AS3Flags>4102</AS3Flags>\n");
        publishSettings.append("    <DefaultLibraryLinkage>rsl</DefaultLibraryLinkage>\n");
        publishSettings.append("    <RSLPreloaderMethod>wrap</RSLPreloaderMethod>\n");
        publishSettings.append("    <RSLPreloaderSWF>$(AppConfig)/ActionScript 3.0/rsls/loader_animation.swf</RSLPreloaderSWF>\n");
        if (greaterThanCC) {
            publishSettings.append("    <LibraryPath>\n");
            publishSettings.append("      <library-path-entry>\n");
            publishSettings.append("        <swc-path>$(AppConfig)/ActionScript 3.0/libs</swc-path>\n");
            publishSettings.append("        <linkage>merge</linkage>\n");
            publishSettings.append("      </library-path-entry>\n");
            publishSettings.append("      <library-path-entry>\n");
            publishSettings.append("        <swc-path>$(FlexSDK)/frameworks/libs/flex.swc</swc-path>\n");
            publishSettings.append("        <linkage>merge</linkage>\n");
            publishSettings.append("        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("      </library-path-entry>\n");
            publishSettings.append("      <library-path-entry>\n");
            publishSettings.append("        <swc-path>$(FlexSDK)/frameworks/libs/core.swc</swc-path>\n");
            publishSettings.append("        <linkage>merge</linkage>\n");
            publishSettings.append("        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("      </library-path-entry>\n");
            publishSettings.append("    </LibraryPath>\n");
            publishSettings.append("    <LibraryVersions>\n");
            publishSettings.append("    </LibraryVersions> ");
        } else {
            publishSettings.append("    <LibraryPath>\n");
            publishSettings.append("      <library-path-entry>\n");
            publishSettings.append("        <swc-path>$(AppConfig)/ActionScript 3.0/libs</swc-path>\n");
            publishSettings.append("        <linkage>merge</linkage>\n");
            publishSettings.append("      </library-path-entry>\n");
            publishSettings.append("      <library-path-entry>\n");
            publishSettings.append("        <swc-path>$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc</swc-path>\n");
            publishSettings.append("        <linkage usesDefault=\"true\">rsl</linkage>\n");
            publishSettings.append("        <rsl-url>http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("        <policy-file-url>http://fpdownload.adobe.com/pub/swz/crossdomain.xml</policy-file-url>\n");
            publishSettings.append("        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("      </library-path-entry>\n");
            publishSettings.append("    </LibraryPath>\n");
            publishSettings.append("    <LibraryVersions>\n");
            publishSettings.append("      <library-version>\n");
            publishSettings.append("        <swc-path>$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc</swc-path>\n");
            publishSettings.append("        <feature name=\"tlfText\" majorVersion=\"2\" minorVersion=\"0\" build=\"232\"/>\n");
            publishSettings.append("        <rsl-url>http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("        <policy-file-url>http://fpdownload.adobe.com/pub/swz/crossdomain.xml</policy-file-url>\n");
            publishSettings.append("        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n");
            publishSettings.append("      </library-version>\n");
            publishSettings.append("    </LibraryVersions>\n");
        }
        publishSettings.append("  </PublishFlashProperties>\n");
        publishSettings.append("  <PublishJpegProperties enabled=\"true\">\n");
        publishSettings.append("    <Width>").append(width).append("</Width>\n");
        publishSettings.append("    <Height>").append(height).append("</Height>\n");
        publishSettings.append("    <Progressive>0</Progressive>\n");
        publishSettings.append("    <DPI>4718592</DPI>\n");
        publishSettings.append("    <Size>0</Size>\n");
        publishSettings.append("    <Quality>80</Quality>\n");
        publishSettings.append("    <MatchMovieDim>1</MatchMovieDim>\n");
        publishSettings.append("  </PublishJpegProperties>\n");
        publishSettings.append("  <PublishRNWKProperties enabled=\"true\">\n");
        publishSettings.append("    <exportFlash>1</exportFlash>\n");
        publishSettings.append("    <flashBitRate>0</flashBitRate>\n");
        publishSettings.append("    <exportAudio>1</exportAudio>\n");
        publishSettings.append("    <audioFormat>0</audioFormat>\n");
        publishSettings.append("    <singleRateAudio>0</singleRateAudio>\n");
        publishSettings.append("    <realVideoRate>100000</realVideoRate>\n");
        publishSettings.append("    <speed28K>1</speed28K>\n");
        publishSettings.append("    <speed56K>1</speed56K>\n");
        publishSettings.append("    <speedSingleISDN>0</speedSingleISDN>\n");
        publishSettings.append("    <speedDualISDN>0</speedDualISDN>\n");
        publishSettings.append("    <speedCorporateLAN>0</speedCorporateLAN>\n");
        publishSettings.append("    <speed256K>0</speed256K>\n");
        publishSettings.append("    <speed384K>0</speed384K>\n");
        publishSettings.append("    <speed512K>0</speed512K>\n");
        publishSettings.append("    <exportSMIL>1</exportSMIL>\n");
        publishSettings.append("  </PublishRNWKProperties>\n");
        publishSettings.append("  <PublishGifProperties enabled=\"true\">\n");
        publishSettings.append("    <Width>").append(width).append("</Width>\n");
        publishSettings.append("    <Height>").append(height).append("</Height>\n");
        publishSettings.append("    <Animated>0</Animated>\n");
        publishSettings.append("    <MatchMovieDim>1</MatchMovieDim>\n");
        publishSettings.append("    <Loop>1</Loop>\n");
        publishSettings.append("    <LoopCount></LoopCount>\n");
        publishSettings.append("    <OptimizeColors>1</OptimizeColors>\n");
        publishSettings.append("    <Interlace>0</Interlace>\n");
        publishSettings.append("    <Smooth>1</Smooth>\n");
        publishSettings.append("    <DitherSolids>0</DitherSolids>\n");
        publishSettings.append("    <RemoveGradients>0</RemoveGradients>\n");
        publishSettings.append("    <TransparentOption></TransparentOption>\n");
        publishSettings.append("    <TransparentAlpha>128</TransparentAlpha>\n");
        publishSettings.append("    <DitherOption></DitherOption>\n");
        publishSettings.append("    <PaletteOption></PaletteOption>\n");
        publishSettings.append("    <MaxColors>255</MaxColors>\n");
        publishSettings.append("    <PaletteName></PaletteName>\n");
        publishSettings.append("  </PublishGifProperties>\n");
        publishSettings.append("  <PublishPNGProperties enabled=\"true\">\n");
        publishSettings.append("    <Width>").append(width).append("</Width>\n");
        publishSettings.append("    <Height>").append(height).append("</Height>\n");
        publishSettings.append("    <OptimizeColors>1</OptimizeColors>\n");
        publishSettings.append("    <Interlace>0</Interlace>\n");
        publishSettings.append("    <Transparent>0</Transparent>\n");
        publishSettings.append("    <Smooth>1</Smooth>\n");
        publishSettings.append("    <DitherSolids>0</DitherSolids>\n");
        publishSettings.append("    <RemoveGradients>0</RemoveGradients>\n");
        publishSettings.append("    <MatchMovieDim>1</MatchMovieDim>\n");
        publishSettings.append("    <DitherOption></DitherOption>\n");
        publishSettings.append("    <FilterOption></FilterOption>\n");
        publishSettings.append("    <PaletteOption></PaletteOption>\n");
        publishSettings.append("    <BitDepth>24-bit with Alpha</BitDepth>\n");
        publishSettings.append("    <MaxColors>255</MaxColors>\n");
        publishSettings.append("    <PaletteName></PaletteName>\n");
        publishSettings.append("  </PublishPNGProperties>\n");
        if (!greaterThanCC) {
            publishSettings.append("  <PublishQTProperties enabled=\"true\">\n");
            publishSettings.append("    <Width>").append(width).append("</Width>\n");
            publishSettings.append("    <Height>").append(height).append("</Height>\n");
            publishSettings.append("    <MatchMovieDim>1</MatchMovieDim>\n");
            publishSettings.append("    <UseQTSoundCompression>0</UseQTSoundCompression>\n");
            publishSettings.append("    <AlphaOption></AlphaOption>\n");
            publishSettings.append("    <LayerOption></LayerOption>\n");
            publishSettings.append("    <QTSndSettings>00000000</QTSndSettings>\n");
            publishSettings.append("    <ControllerOption>0</ControllerOption>\n");
            publishSettings.append("    <Looping>0</Looping>\n");
            publishSettings.append("    <PausedAtStart>0</PausedAtStart>\n");
            publishSettings.append("    <PlayEveryFrame>0</PlayEveryFrame>\n");
            publishSettings.append("    <Flatten>1</Flatten>\n");
            publishSettings.append("  </PublishQTProperties>\n");
        }
        publishSettings.append("</flash_profile>\n");
        publishSettings.append("</flash_profiles>");
        String publishSettingsStr = publishSettings.toString();

        if (compressed) {
            final String domDocumentF = domDocumentStr;
            final String publishSettingsF = publishSettingsStr;
            final String outfileF = outfile;
            new RetryTask(() -> {
                try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outfileF))) {
                    out.putNextEntry(new ZipEntry("DOMDocument.xml"));
                    out.write(Utf8Helper.getBytes(domDocumentF));
                    out.putNextEntry(new ZipEntry("PublishSettings.xml"));
                    out.write(Utf8Helper.getBytes(publishSettingsF));
                    for (String fileName : files.keySet()) {
                        out.putNextEntry(new ZipEntry("LIBRARY/" + fileName));
                        out.write(files.get(fileName));
                    }
                    for (String fileName : datfiles.keySet()) {
                        out.putNextEntry(new ZipEntry("bin/" + fileName));
                        out.write(datfiles.get(fileName));
                    }
                }
            }, handler).run();

        } else {
            Path.createDirectorySafe(outDir);
            writeFile(handler, Utf8Helper.getBytes(domDocumentStr), outDir.getAbsolutePath() + File.separator + "DOMDocument.xml");
            writeFile(handler, Utf8Helper.getBytes(publishSettingsStr), outDir.getAbsolutePath() + File.separator + "PublishSettings.xml");
            File libraryDir = new File(outDir.getAbsolutePath() + File.separator + "LIBRARY");
            libraryDir.mkdir();
            File binDir = new File(outDir.getAbsolutePath() + File.separator + "bin");
            binDir.mkdir();
            for (String fileName : files.keySet()) {
                writeFile(handler, files.get(fileName), libraryDir.getAbsolutePath() + File.separator + fileName);
            }
            for (String fileName : datfiles.keySet()) {
                writeFile(handler, datfiles.get(fileName), binDir.getAbsolutePath() + File.separator + fileName);
            }
            writeFile(handler, Utf8Helper.getBytes("PROXY-CS5"), outfile);
        }
        if (useAS3) {
            try {
                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false);
                swf.exportActionScript(handler, Path.combine(outDir.getAbsolutePath(), "scripts"), scriptExportSettings, parallel, null);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error during ActionScript3 export", ex);
            }
        }

    }

    private static int normHue(double h) {
        if (Double.isNaN(h)) {
            h = -Math.PI;
        }
        int ret = (int) Math.round(h * 180 / Math.PI);
        while (ret > 180) {
            ret -= 360;
        }
        while (ret < -180) {
            ret += 360;
        }
        return ret;
    }

    private static int normBrightness(double b) {
        if (Double.isNaN(b)) {
            b = -100;
        }
        return (int) Math.round(b);
    }

    private static int normSaturation(double s) {
        if (Double.isNaN(s)) {
            return -100;
        } else if (s == 1) {
            return 0;
        } else if (s - 1 < 0) {
            return (int) Math.round((s - 1) * 100);
        } else {
            return (int) Math.round(((s - 1) * 100) / 3);
        }
    }

    private static int normContrast(double c) {
        double[] ctrMap = {
            //      0     1     2     3     4     5     6     7     8     9
            /*0*/0, 0.01, 0.02, 0.04, 0.05, 0.06, 0.07, 0.08, 0.1, 0.11,
            /*1*/ 0.12, 0.14, 0.15, 0.16, 0.17, 0.18, 0.20, 0.21, 0.22, 0.24,
            /*2*/ 0.25, 0.27, 0.28, 0.30, 0.32, 0.34, 0.36, 0.38, 0.40, 0.42,
            /*3*/ 0.44, 0.46, 0.48, 0.5, 0.53, 0.56, 0.59, 0.62, 0.65, 0.68,
            /*4*/ 0.71, 0.74, 0.77, 0.80, 0.83, 0.86, 0.89, 0.92, 0.95, 0.98,
            /*5*/ 1.0, 1.06, 1.12, 1.18, 1.24, 1.30, 1.36, 1.42, 1.48, 1.54,
            /*6*/ 1.60, 1.66, 1.72, 1.78, 1.84, 1.90, 1.96, 2.0, 2.12, 2.25,
            /*7*/ 2.37, 2.50, 2.62, 2.75, 2.87, 3.0, 3.2, 3.4, 3.6, 3.8,
            /*8*/ 4.0, 4.3, 4.7, 4.9, 5.0, 5.5, 6.0, 6.5, 6.8, 7.0,
            /*9*/ 7.3, 7.5, 7.8, 8.0, 8.4, 8.7, 9.0, 9.4, 9.6, 9.8,
            /*10*/ 10.0};
        if (c == 127) {
            return 0;
        } else if (c - 127 < 0) {
            return (int) Math.round((c - 127) * 100.0 / 127.0);
        } else {
            c = (c - 127) / 127;
            for (int i = 0; i < ctrMap.length; i++) {
                if (ctrMap[i] >= c) {
                    return i;
                }
            }
        }
        return ctrMap.length - 1;
    }

    private static boolean sameDouble(double a, double b) {
        final double EPSILON = 0.00001;
        return a == b ? true : Math.abs(a - b) < EPSILON;
    }

    private static void convertAdjustColorFilter(COLORMATRIXFILTER filter, StringBuilder ret) {
        float[][] matrix = new float[5][5];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                matrix[j][i] = filter.matrix[index];
                index++;
            }
        }
        double a11 = matrix[0][0], a12 = matrix[0][1], a13 = matrix[0][2],
                a21 = matrix[1][0], a22 = matrix[1][1], a23 = matrix[1][2],
                a31 = matrix[2][0], a32 = matrix[2][1], a33 = matrix[2][2],
                a41 = matrix[4][0];

        double b, c, h, s;
        b = (24872168661075.0 * a11 * a11 - 151430415740925.0 * a12 + 341095051289483.0 * a12 * a12 - 15302094789450.0 * a13 + 82428663495404.0 * a12 * a13
                - 4592294873812.0 * a13 * a13 + 43556251470.0 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                        - 930 * a11 * (287 * a12 + 178 * a13)) + 2384730956550.0 * a12 * a41 + 240977870700.0 * a13 * a41
                - 685925220 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))
                * a41 + 465 * a11 * (466201717582.0 * a12 + 55756962908.0 * a13 + 764132175 * (-127 + 2 * a41)))
                / (391687695450.0 * a11 * a11 + 5371575610858.0 * a12 * a12 + 1298089188904.0 * a12 * a13 - 72319604312.0 * a13 * a13
                + 1860 * a11 * (1835439833 * a12 + 219515602 * a13));
        c = (127 * (495225 * a11 + 1661845 * a12 + 167930 * a13
                + 478 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))))
                / 717495;
        h = 2 * (Math.atan((-465 * a11 + 287 * a12 + 178 * a13 + Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                - 930 * a11 * (287 * a12 + 178 * a13))) / (500. * (a12 - a13))) + Math.PI/*+ Pi*C(1)*/);
        s = (1543 * (-103355550 * a11 * a11 - 158872382 * a12 * a12 + 190161784 * a12 * a13 - 134644952 * a13 * a13
                + 1661845 * a12 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13
                        - 930 * a11 * (287 * a12 + 178 * a13)) + 167930 * a13
                * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13 + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13))
                + 465 * a11 * (274372 * a12 + 170168 * a13 + 1065 * Math.sqrt(216225 * a11 * a11 + 332369 * a12 * a12 - 397828 * a12 * a13
                        + 281684 * a13 * a13 - 930 * a11 * (287 * a12 + 178 * a13)))))
                / (195843847725.0 * a11 * a11 + 2685787805429.0 * a12 * a12 + 649044594452.0 * a12 * a13 - 36159802156.0 * a13 * a13
                + 930 * a11 * (1835439833 * a12 + 219515602 * a13));

        if (sameDouble(410 * a12, 1543 * a31) && sameDouble(410 * a12, 1543 * a32) && sameDouble(3047 * a12, 1543 * a21) && sameDouble(3047 * a12, 1543 * a23)
                && sameDouble(a22, a11 + (1504 * a12) / 1543.) && sameDouble((1133 * a12) / 1543. + a33, a11)
                /*&& (b == (195961 * a11 + 439039 * a12 + 1543 * (-127 + 2 * a41)) / (3086 * a11 + 6914 * a12))
                 && (c == 127 * a11 + (439039 * a12) / 1543.) && (s == (1543 * (a11 - a12)) / (1543 * a11 + 3457 * a12))
                 */ && !sameDouble(a11, a12) && !sameDouble(1543 * a11 + 3457 * a12, 0)) {
            h = 0;
        }

        ret.append("<AdjustColorFilter brightness=\"").append(normBrightness(b)).append("\" contrast=\"").append(normContrast(c)).append("\" saturation=\"").append(normSaturation(s)).append("\" hue=\"").append(normHue(h)).append("\"/>");
    }

    private static String convertHTMLText(List<Tag> tags, DefineEditTextTag det, String html) {
        HTMLTextParser tparser = new HTMLTextParser(tags, det);
        XMLReader parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(tparser);
            parser.setErrorHandler(tparser);
            html = "<?xml version=\"1.0\"?>\n"
                    + "<!DOCTYPE some_name [ \n"
                    + "<!ENTITY nbsp \"&#160;\"> \n"
                    + "]><html>" + html + "</html>";
            try {
                parser.parse(new InputSource(new StringReader(html)));
            } catch (SAXParseException spe) {
                System.out.println(html);
                System.err.println(tparser.result);
            }
        } catch (SAXException | IOException e) {
            logger.log(Level.SEVERE, "Error while converting HTML", e);
        }
        return tparser.result;
    }

    private static String xmlString(String s) {
        return s.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("&", "&amp;").replace("\r\n", "&#xD;").replace("\r", "&#xD;").replace("\n", "&#xD;");
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static class HTMLTextParser extends DefaultHandler {

        public String result = "";

        private String fontFace = "";

        private String color = "";

        private int size = -1;

        private int indent = -1;

        private int leftMargin = -1;

        private int rightMargin = -1;

        private int lineSpacing = -1;

        private double letterSpacing = -1;

        private String alignment = null;

        private final List<Tag> tags;

        private boolean bold = false;

        private boolean italic = false;

        private boolean underline = false;

        private boolean li = false;

        private String url = null;

        private String target = null;

        @Override
        public void error(SAXParseException e) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
        }

        public HTMLTextParser(List<Tag> tags, DefineEditTextTag det) {
            if (det.hasFont) {
                String fontName = null;
                FontTag ft = null;
                for (Tag u : tags) {
                    if (u instanceof DefineFontNameTag) {
                        if (((DefineFontNameTag) u).fontId == det.fontId) {
                            fontName = ((DefineFontNameTag) u).fontName;
                        }
                    }
                    if (u instanceof FontTag) {
                        if (((FontTag) u).getFontId() == det.fontId) {
                            ft = (FontTag) u;
                        }
                    }
                    if (fontName != null && ft != null) {
                        break;
                    }
                }
                if (ft != null) {
                    if (fontName == null) {
                        fontName = ft.getFontNameIntag();
                    }
                    if (fontName == null) {
                        fontName = FontTag.defaultFontName;
                    }
                    italic = ft.isItalic();
                    bold = ft.isBold();
                    size = det.fontHeight;
                    fontFace = new Font(fontName, (italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0) | (!italic && !bold ? Font.PLAIN : 0), size < 0 ? 10 : size).getPSName();
                }
            }
            if (det.hasLayout) {
                leftMargin = det.leftMargin;
                rightMargin = det.rightMargin;
                indent = det.indent;
                lineSpacing = det.leading;
                String[] alignNames = {"left", "right", "center", "justify"};
                if (det.align < alignNames.length) {
                    alignment = alignNames[det.align];
                } else {
                    alignment = "unknown";
                }
            }
            this.tags = tags;
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
            switch (qName) {
                case "a":
                    String href = attributes.getValue("href");
                    if (href != null) {
                        url = href;
                    }
                    String t = attributes.getValue("target");
                    if (t != null) {
                        target = t;
                    }
                    break;
                case "b":
                    bold = true;
                    break;
                case "i":
                    italic = true;
                    break;
                case "u":
                    underline = true;
                    break;
                case "li":
                    li = true;
                    break;
                case "p":
                    String a = attributes.getValue("align");
                    if (a != null) {
                        alignment = a;
                    }
                    if (!result.isEmpty()) {
                        putText("\r\n");
                    }
                    break;
                case "font":
                    //kerning  ?
                    String ls = attributes.getValue("letterSpacing");
                    if (ls != null) {
                        letterSpacing = Double.parseDouble(ls);
                    }
                    String s = attributes.getValue("size");
                    if (s != null) {
                        size = Integer.parseInt(s);
                    }
                    String c = attributes.getValue("color");
                    if (c != null) {
                        color = c;
                    }
                    String f = attributes.getValue("face");
                    if (f != null) {
                        for (Tag tag : tags) {
                            if (tag instanceof FontTag) {
                                FontTag ft = (FontTag) tag;
                                String fontName = null;
                                if (f.equals(ft.getFontNameIntag())) {
                                    for (Tag u : tags) {
                                        if (u instanceof DefineFontNameTag) {
                                            if (((DefineFontNameTag) u).fontId == ft.getFontId()) {
                                                fontName = ((DefineFontNameTag) u).fontName;
                                            }
                                        }
                                    }
                                    if (fontName == null) {
                                        fontName = ft.getFontNameIntag();
                                    }
                                    String installedFont;
                                    if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                                        fontFace = new Font(installedFont, (italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0) | (!italic && !bold ? Font.PLAIN : 0), size < 0 ? 10 : size).getPSName();
                                    } else {
                                        fontFace = fontName;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName,
                String qName) throws SAXException {
            if (qName.equals("a")) {
                url = null;
                target = null;
            }
            if (qName.equals("b")) {
                bold = false;
            }
            if (qName.equals("i")) {
                italic = false;
            }
            if (qName.equals("u")) {
                underline = false;
            }
            if (qName.equals("li")) {
                li = false;
            }
        }

        private void putText(String txt) {

            result += "<DOMTextRun>";
            result += "<characters>" + xmlString(txt) + "</characters>";
            result += "<textAttrs>";
            result += "<DOMTextAttrs";
            if (alignment != null) {
                result += " alignment=\"" + alignment + "\"";
            }
            result += " rotation=\"true\""; //?
            if (indent > -1) {
                result += " indent=\"" + twipToPixel(indent) + "\"";
            }
            if (leftMargin > -1) {
                result += " leftMargin=\"" + twipToPixel(leftMargin) + "\"";
            }
            if (letterSpacing > -1) {
                result += " letterSpacing=\"" + letterSpacing + "\"";
            }
            if (lineSpacing > -1) {
                result += " lineSpacing=\"" + twipToPixel(lineSpacing) + "\"";
            }
            if (rightMargin > -1) {
                result += " rightMargin=\"" + twipToPixel(rightMargin) + "\"";
            }
            if (size > -1) {
                result += " size=\"" + size + "\"";
                result += " bitmapSize=\"" + (size * 20) + "\"";
            }
            if (fontFace != null) {
                result += " face=\"" + fontFace + "\"";
            }
            if (color != null) {
                result += " fillColor=\"" + color + "\"";
            }
            if (url != null) {
                result += " url=\"" + url + "\"";
            }
            if (target != null) {
                result += " target=\"" + target + "\"";
            }
            result += "/>";
            result += "</textAttrs>";
            result += "</DOMTextRun>";
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            putText(new String(ch, start, length));

        }

        @Override
        public void endDocument() {
            if (this.result.isEmpty()) {
                putText("");
            }
        }
    }
}
