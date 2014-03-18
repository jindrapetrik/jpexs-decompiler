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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
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
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.awt.Point;
import java.io.ByteArrayInputStream;
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
import javax.imageio.ImageIO;
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

    public static final int KEY_MODE_NORMAL = 9728;
    public static final int KEY_MODE_CLASSIC_TWEEN = 22017;
    public static final int KEY_MODE_SHAPE_TWEEN = 17922;
    public static final int KEY_MODE_MOTION_TWEEN = 8195;
    public static final int KEY_MODE_SHAPE_LAYERS = 8192;

    private XFLConverter() {
    }

    public static String convertShapeEdge(MATRIX mat, SHAPERECORD record, int x, int y) {
        String ret = "";
        if (record instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) record;
            Point p = new Point(scr.moveDeltaX, scr.moveDeltaY);
            //p = mat.apply(p);
            if (scr.stateMoveTo) {
                return "! " + p.x + " " + p.y;
            }
            return "";
        }
        if (record instanceof StraightEdgeRecord) {
            StraightEdgeRecord ser = (StraightEdgeRecord) record;
            if (ser.generalLineFlag || ser.vertLineFlag) {
                y += ser.deltaY;
            }
            if (ser.generalLineFlag || (!ser.vertLineFlag)) {
                x += ser.deltaX;
            }
            Point p = new Point(x, y);
            //p = mat.apply(p);
            return "| " + p.x + " " + p.y;
        }
        if (record instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) record;
            int controlX = cer.controlDeltaX + x;
            int controlY = cer.controlDeltaY + y;
            int anchorX = cer.anchorDeltaX + controlX;
            int anchorY = cer.anchorDeltaY + controlY;
            Point control = new Point(controlX, controlY);
            //control = mat.apply(control);
            Point anchor = new Point(anchorX, anchorY);
            //anchor = mat.apply(anchor);
            return "[ " + control.x + " " + control.y + " " + anchor.x + " " + anchor.y;
        }
        return ret;
    }

    public static String convertShapeEdges(int startX, int startY, MATRIX mat, List<SHAPERECORD> records) {
        String ret = "";
        int x = startX;
        int y = startY;
        ret += "!" + startX + " " + startY;
        for (SHAPERECORD rec : records) {
            ret += convertShapeEdge(mat, rec, x, y);
            x = rec.changeX(x);
            y = rec.changeY(y);
        }
        return ret;
    }

    public static String convertLineStyle(LINESTYLE ls, int shapeNum) {
        return "<SolidStroke weight=\"" + (((float) ls.width) / 20.0) + "\">"
                + "<fill>"
                + "<SolidColor color=\"" + ls.color.toHexRGB() + "\""
                + (shapeNum == 3 ? " alpha=\"" + ((RGBA) ls.color).getAlphaFloat() + "\"" : "")
                + " />"
                + "</fill>"
                + "</SolidStroke>";
    }

    public static String convertLineStyle(HashMap<Integer, CharacterTag> characters, LINESTYLE2 ls, int shapeNum) {
        String ret = "";
        String params = "";
        if (ls.pixelHintingFlag) {
            params += " pixelHinting=\"true\"";
        }
        if (ls.width == 1) {
            params += " solidStyle=\"hairline\"";
        }
        if ((!ls.noHScaleFlag) && (!ls.noVScaleFlag)) {
            params += " scaleMode=\"normal\"";
        } else if ((!ls.noHScaleFlag) && ls.noVScaleFlag) {
            params += " scaleMode=\"horizontal\"";
        } else if (ls.noHScaleFlag && (!ls.noVScaleFlag)) {
            params += " scaleMode=\"vertical\"";
        }

        switch (ls.endCapStyle) {  //What about endCapStyle?
            case LINESTYLE2.NO_CAP:
                params += " caps=\"none\"";
                break;
            case LINESTYLE2.SQUARE_CAP:
                params += " caps=\"square\"";
                break;
        }
        switch (ls.joinStyle) {
            case LINESTYLE2.BEVEL_JOIN:
                params += " joints=\"bevel\"";
                break;
            case LINESTYLE2.MITER_JOIN:
                params += " joints=\"miter\"";
                float miterLimitFactor = toFloat(ls.miterLimitFactor);
                if (miterLimitFactor != 3.0f) {
                    params += " miterLimit=\"" + miterLimitFactor + "\"";
                }
                break;
        }

        ret += "<SolidStroke weight=\"" + (((float) ls.width) / 20.0) + "\"";
        ret += params;
        ret += ">";
        ret += "<fill>";

        if (!ls.hasFillFlag) {
            RGBA color = (RGBA) ls.color;
            ret += "<SolidColor color=\"" + color.toHexRGB() + "\""
                    + (color.getAlphaFloat() != 1 ? " alpha=\"" + color.getAlphaFloat() + "\"" : "")
                    + " />";
        } else {
            ret += convertFillStyle(null/* FIXME */, characters, ls.fillType, shapeNum);
        }
        ret += "</fill>";
        ret += "</SolidStroke>";
        return ret;
    }

    private static float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    public static String convertFillStyle(MATRIX mat, HashMap<Integer, CharacterTag> characters, FILLSTYLE fs, int shapeNum) {
        if (mat == null) {
            mat = new MATRIX();
        }
        String ret = "";
        //ret += "<FillStyle index=\"" + index + "\">";
        switch (fs.fillStyleType) {
            case FILLSTYLE.SOLID:
                ret += "<SolidColor color=\"";
                ret += fs.color.toHexRGB();
                ret += "\"";
                if (shapeNum >= 3) {
                    ret += " alpha=\"" + ((RGBA) fs.color).getAlphaFloat() + "\"";
                }
                ret += " />";
                break;
            case FILLSTYLE.REPEATING_BITMAP:
            case FILLSTYLE.CLIPPED_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                ret += "<BitmapFill";
                ret += " bitmapPath=\"";
                CharacterTag bitmapCh = characters.get(fs.bitmapId);
                if (bitmapCh instanceof ImageTag) {
                    ImageTag it = (ImageTag) bitmapCh;
                    ret += "bitmap" + bitmapCh.getCharacterId() + "." + it.getImageFormat();
                } else {
                    if (bitmapCh != null) {
                        Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Suspicious bitmapfill:" + bitmapCh.getClass().getSimpleName());
                    }
                    return "<SolidColor color=\"#ffffff\" />";
                }
                ret += "\"";

                if ((fs.fillStyleType == FILLSTYLE.CLIPPED_BITMAP) || (fs.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
                    ret += " bitmapIsClipped=\"true\"";
                }

                ret += ">";
                ret += "<matrix>" + convertMatrix(fs.bitmapMatrix) + "</matrix>";
                ret += "</BitmapFill>";
                break;
            case FILLSTYLE.LINEAR_GRADIENT:
            case FILLSTYLE.RADIAL_GRADIENT:
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:

                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    ret += "<LinearGradient";
                } else {
                    ret += "<RadialGradient";
                    ret += " focalPointRatio=\"";
                    if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                        ret += ((FOCALGRADIENT) fs.gradient).focalPoint;
                    } else {
                        ret += "0";
                    }
                    ret += "\"";
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
                    ret += " interpolationMethod=\"linearRGB\"";
                }
                switch (spreadMode) {
                    case GRADIENT.SPREAD_PAD_MODE:

                        break;
                    case GRADIENT.SPREAD_REFLECT_MODE:
                        ret += " spreadMethod=\"reflect\"";
                        break;
                    case GRADIENT.SPREAD_REPEAT_MODE:
                        ret += " spreadMethod=\"repeat\"";
                        break;
                }

                ret += ">";

                ret += "<matrix>" + convertMatrix(fs.gradientMatrix) + "</matrix>";
                GRADRECORD[] records;
                if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                    records = fs.gradient.gradientRecords;
                } else {
                    records = fs.gradient.gradientRecords;
                }
                for (GRADRECORD rec : records) {
                    ret += "<GradientEntry";
                    ret += " color=\"" + (shapeNum >= 3 ? rec.color.toHexRGB() : rec.color.toHexRGB()) + "\"";
                    if (shapeNum >= 3) {
                        ret += " alpha=\"" + ((RGBA) rec.color).getAlphaFloat() + "\"";
                    }
                    ret += " ratio=\"" + rec.getRatioFloat() + "\"";
                    ret += " />";
                }
                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    ret += "</LinearGradient>";
                } else {
                    ret += "</RadialGradient>";
                }
                break;
        }
        //ret += "</FillStyle>";
        return ret;

    }

    public static String convertMatrix(MATRIX m) {
        String ret = "";
        if (m == null) {
            m = new MATRIX();
        }
        ret += "<Matrix ";
        ret += "tx=\"" + (((float) m.translateX) / 20.0) + "\" ";
        ret += "ty=\"" + (((float) m.translateY) / 20.0) + "\" ";
        if (m.hasScale) {
            ret += "a=\"" + m.getScaleXFloat() + "\" ";
            ret += "d=\"" + m.getScaleYFloat() + "\" ";
        }
        if (m.hasRotate) {
            ret += "b=\"" + m.getRotateSkew0Float() + "\" ";
            ret += "c=\"" + m.getRotateSkew1Float() + "\" ";
        }
        ret += "/>";
        return ret;
    }
    /*
     public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, SHAPE shape) {
     return convertShape(characters, mat, shapeNum, shape.shapeRecords);
     }

     public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, SHAPEWITHSTYLE shape,boolean morphShape) {
     return convertShape(characters, mat, shapeNum, shape.shapeRecords, shape.fillStyles, shape.lineStyles, morphShape);
     }

     public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, ShapeTag shape, boolean morphShape) {
     return convertShape(characters, mat, shape.getShapeNum(), shape.getShapes(),morphShape);
     }

     public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords,boolean useLayers) {
     return convertShape(characters, mat, shapeNum, shapeRecords, null, null, false,true);
     }*/

    private static boolean shapeHasMultiLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles) {
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, false);
        return layers.size() > 1;
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape, boolean useLayers) {
        String ret = "";
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, morphshape);
        if (layers.size() == 1 && !useLayers) {
            ret += layers.get(0);
        } else {
            int layer = 1;
            for (int l = layers.size() - 1; l >= 0; l--) {
                ret += "<DOMLayer name=\"Layer " + (layer++) + "\">"; //color=\"#4FFF4F\"
                ret += "<frames>";
                ret += "<DOMFrame index=\"0\" motionTweenScale=\"false\" keyMode=\"" + KEY_MODE_SHAPE_LAYERS + "\">";
                ret += "<elements>";
                ret += layers.get(l);
                ret += "</elements>";
                ret += "</DOMFrame>";
                ret += "</frames>";
                ret += "</DOMLayer>";
            }
        }
        return ret;
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
        List<SHAPERECORD> ret = new ArrayList<>(shapeRecords);

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
                        if (i >= 2) {
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

        }
        return ret;
    }

    public static List<String> getShapeLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape) {
        if (mat == null) {
            mat = new MATRIX();
        }
        shapeRecords = smoothShape(shapeRecords);
        List<SHAPERECORD> edges = new ArrayList<>();
        int lineStyleCount = 0;
        int fillStyle0 = -1;
        int fillStyle1 = -1;
        int strokeStyle = -1;
        String fillsStr = "";
        String strokesStr = "";
        fillsStr += "<fills>";
        strokesStr += "<strokes>";
        List<String> layers = new ArrayList<>();
        String currentLayer = "";

        int fillStyleCount = 0;
        if (fillStyles != null) {
            for (FILLSTYLE fs : fillStyles.fillStyles) {
                fillsStr += "<FillStyle index=\"" + (fillStyleCount + 1) + "\">";
                fillsStr += convertFillStyle(mat, characters, fs, shapeNum);
                fillsStr += "</FillStyle>";
                fillStyleCount++;
            }
        }
        if (lineStyles != null) {
            if ((shapeNum == 4) && (lineStyles.lineStyles != null)) { //(shapeNum == 4) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                    strokesStr += convertLineStyle(characters, (LINESTYLE2) lineStyles.lineStyles[l], shapeNum);
                    strokesStr += "</StrokeStyle>";
                    lineStyleCount++;
                }
            } else if (lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                    strokesStr += convertLineStyle(lineStyles.lineStyles[l], shapeNum);
                    strokesStr += "</StrokeStyle>";
                    lineStyleCount++;
                }
            }
        }

        fillsStr += "</fills>";
        strokesStr += "</strokes>";

        int layer = 1;

        if ((fillStyleCount > 0) || (lineStyleCount > 0)) {
            currentLayer += "<DOMShape isFloating=\"true\">";
            currentLayer += fillsStr;
            currentLayer += strokesStr;
            currentLayer += "<edges>";
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
                    fillsStr = "<fills>";
                    strokesStr = "<strokes>";
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
                                currentLayer += "<Edge";
                                if (fillStyle0 > -1) {
                                    currentLayer += " fillStyle0=\"" + fillStyle0 + "\"";
                                }
                                if (fillStyle1 > -1) {
                                    currentLayer += " fillStyle1=\"" + fillStyle1 + "\"";
                                }
                                if (strokeStyle > -1) {
                                    currentLayer += " strokeStyle=\"" + strokeStyle + "\"";
                                }
                                currentLayer += " edges=\"" + convertShapeEdges(startEdgeX, startEdgeY, mat, edges) + "\" />";
                            }
                        }

                        currentLayer += "</edges>";
                        currentLayer += "</DOMShape>";
                        if (!currentLayer.contains("<edges></edges>")) { //no empty layers,  TODO:handle this better
                            layers.add(currentLayer);
                        }
                        currentLayer = "";
                    }

                    currentLayer += "<DOMShape isFloating=\"true\">";
                    //ret += convertShape(characters, null, shape);
                    for (int f = 0; f < scr.fillStyles.fillStyles.length; f++) {
                        fillsStr += "<FillStyle index=\"" + (f + 1) + "\">";
                        fillsStr += convertFillStyle(mat, characters, scr.fillStyles.fillStyles[f], shapeNum);
                        fillsStr += "</FillStyle>";
                        fillStyleCount++;
                    }

                    lineStyleCount = 0;
                    if (shapeNum == 4) {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                            strokesStr += convertLineStyle(characters, (LINESTYLE2) scr.lineStyles.lineStyles[l], shapeNum);
                            strokesStr += "</StrokeStyle>";
                            lineStyleCount++;
                        }
                    } else {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                            strokesStr += convertLineStyle(scr.lineStyles.lineStyles[l], shapeNum);
                            strokesStr += "</StrokeStyle>";
                            lineStyleCount++;
                        }
                    }
                    fillsStr += "</fills>";
                    strokesStr += "</strokes>";
                    currentLayer += fillsStr;
                    currentLayer += strokesStr;
                    currentLayer += "<edges>";
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
                            currentLayer += "<Edge";
                            if (fillStyle0 > -1) {
                                currentLayer += " fillStyle0=\"" + lastFillStyle0 + "\"";
                            }
                            if (fillStyle1 > -1) {
                                currentLayer += " fillStyle1=\"" + lastFillStyle1 + "\"";
                            }
                            if (strokeStyle > -1) {
                                currentLayer += " strokeStyle=\"" + lastStrokeStyle + "\"";
                            }
                            currentLayer += " edges=\"" + convertShapeEdges(startEdgeX, startEdgeY, mat, edges) + "\" />";
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
                    currentLayer += "<Edge";
                    if (fillStyle0 > -1) {
                        currentLayer += " fillStyle0=\"" + fillStyle0 + "\"";
                    }
                    if (fillStyle1 > -1) {
                        currentLayer += " fillStyle1=\"" + fillStyle1 + "\"";
                    }
                    if (strokeStyle > -1) {
                        currentLayer += " strokeStyle=\"" + strokeStyle + "\"";
                    }
                    currentLayer += " edges=\"" + convertShapeEdges(startEdgeX, startEdgeY, mat, edges) + "\" />";
                }
            }
        }
        edges.clear();
        fillsStr += "</fills>";
        strokesStr += "</strokes>";
        if (!currentLayer.isEmpty()) {
            currentLayer += "</edges>";
            currentLayer += "</DOMShape>";

            if (!currentLayer.contains("<edges></edges>")) { //no empty layers, TODO:handle this better
                layers.add(currentLayer);
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

    public static String convertFilter(FILTER filter) {
        String ret = "";
        if (filter instanceof DROPSHADOWFILTER) {
            DROPSHADOWFILTER dsf = (DROPSHADOWFILTER) filter;
            ret += "<DropShadowFilter";
            if (dsf.dropShadowColor.alpha != 255) {
                ret += " alpha=\"" + doubleToString(dsf.dropShadowColor.getAlphaFloat()) + "\"";
            }
            ret += " angle=\"" + doubleToString(radToDeg(dsf.angle)) + "\"";
            ret += " blurX=\"" + doubleToString(dsf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(dsf.blurY) + "\"";
            ret += " color=\"" + dsf.dropShadowColor.toHexRGB() + "\"";
            ret += " distance=\"" + doubleToString(dsf.distance) + "\"";
            if (!dsf.compositeSource) {
                ret += " hideObject=\"true\"";
            }
            if (dsf.innerShadow) {
                ret += " inner=\"true\"";
            }
            if (dsf.knockout) {
                ret += " knockout=\"true\"";
            }
            ret += " quality=\"" + dsf.passes + "\"";
            ret += " strength=\"" + doubleToString(dsf.strength, 2) + "\"";
            ret += " />";
        } else if (filter instanceof BLURFILTER) {
            BLURFILTER bf = (BLURFILTER) filter;
            ret += "<BlurFilter";
            ret += " blurX=\"" + doubleToString(bf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(bf.blurY) + "\"";
            ret += " quality=\"" + bf.passes + "\"";
            ret += " />";
        } else if (filter instanceof GLOWFILTER) {
            GLOWFILTER gf = (GLOWFILTER) filter;
            ret += "<GlowFilter";
            if (gf.glowColor.alpha != 255) {
                ret += " alpha=\"" + gf.glowColor.getAlphaFloat() + "\"";
            }
            ret += " blurX=\"" + doubleToString(gf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(gf.blurY) + "\"";
            ret += " color=\"" + gf.glowColor.toHexRGB() + "\"";

            if (gf.innerGlow) {
                ret += " inner=\"true\"";
            }
            if (gf.knockout) {
                ret += " knockout=\"true\"";
            }
            ret += " quality=\"" + gf.passes + "\"";
            ret += " strength=\"" + doubleToString(gf.strength, 2) + "\"";
            ret += " />";
        } else if (filter instanceof BEVELFILTER) {
            BEVELFILTER bf = (BEVELFILTER) filter;
            ret += "<BevelFilter";
            ret += " blurX=\"" + doubleToString(bf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(bf.blurY) + "\"";
            ret += " quality=\"" + bf.passes + "\"";
            ret += " angle=\"" + doubleToString(radToDeg(bf.angle)) + "\"";
            ret += " distance=\"" + bf.distance + "\"";
            if (bf.highlightColor.alpha != 255) {
                ret += " highlightAlpha=\"" + bf.highlightColor.getAlphaFloat() + "\"";
            }
            ret += " highlightColor=\"" + bf.highlightColor.toHexRGB() + "\"";
            if (bf.knockout) {
                ret += " knockout=\"true\"";
            }
            if (bf.shadowColor.alpha != 255) {
                ret += " shadowAlpha=\"" + bf.shadowColor.getAlphaFloat() + "\"";
            }
            ret += " shadowColor=\"" + bf.shadowColor.toHexRGB() + "\"";
            ret += " strength=\"" + doubleToString(bf.strength, 2) + "\"";
            if (bf.onTop && !bf.innerShadow) {
                ret += " type=\"full\"";
            } else if (!bf.innerShadow) {
                ret += " type=\"outer\"";
            }
            ret += " />";
        } else if (filter instanceof GRADIENTGLOWFILTER) {
            GRADIENTGLOWFILTER ggf = (GRADIENTGLOWFILTER) filter;
            ret += "<GradientGlowFilter";
            ret += " angle=\"" + doubleToString(radToDeg(ggf.angle)) + "\"";

            ret += " blurX=\"" + doubleToString(ggf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(ggf.blurY) + "\"";
            ret += " quality=\"" + ggf.passes + "\"";
            ret += " distance=\"" + doubleToString(ggf.distance) + "\"";
            if (ggf.knockout) {
                ret += " knockout=\"true\"";
            }
            ret += " strength=\"" + doubleToString(ggf.strength, 2) + "\"";
            if (ggf.onTop && !ggf.innerShadow) {
                ret += " type=\"full\"";
            } else if (!ggf.innerShadow) {
                ret += " type=\"outer\"";
            }
            ret += ">";
            for (int g = 0; g < ggf.gradientColors.length; g++) {
                RGBA gc = ggf.gradientColors[g];
                ret += "<GradientEntry color=\"" + gc.toHexRGB() + "\"";
                if (gc.alpha != 255) {
                    ret += " alpha=\"" + gc.getAlphaFloat() + "\"";
                }
                ret += " ratio=\"" + doubleToString(((float) ggf.gradientRatio[g]) / 255.0) + "\"";
                ret += "/>";
            }
            ret += "</GradientGlowFilter>";
        } else if (filter instanceof GRADIENTBEVELFILTER) {
            GRADIENTBEVELFILTER gbf = (GRADIENTBEVELFILTER) filter;
            ret += "<GradientBevelFilter";
            ret += " angle=\"" + doubleToString(radToDeg(gbf.angle)) + "\"";

            ret += " blurX=\"" + doubleToString(gbf.blurX) + "\"";
            ret += " blurY=\"" + doubleToString(gbf.blurY) + "\"";
            ret += " quality=\"" + gbf.passes + "\"";
            ret += " distance=\"" + doubleToString(gbf.distance) + "\"";
            if (gbf.knockout) {
                ret += " knockout=\"true\"";
            }
            ret += " strength=\"" + doubleToString(gbf.strength, 2) + "\"";
            if (gbf.onTop && !gbf.innerShadow) {
                ret += " type=\"full\"";
            } else if (!gbf.innerShadow) {
                ret += " type=\"outer\"";
            }
            ret += ">";
            for (int g = 0; g < gbf.gradientColors.length; g++) {
                RGBA gc = gbf.gradientColors[g];
                ret += "<GradientEntry color=\"" + gc.toHexRGB() + "\"";
                if (gc.alpha != 255) {
                    ret += " alpha=\"" + gc.getAlphaFloat() + "\"";
                }
                ret += " ratio=\"" + doubleToString(((float) gbf.gradientRatio[g]) / 255.0) + "\"";
                ret += "/>";
            }
            ret += "</GradientBevelFilter>";
        } else if (filter instanceof COLORMATRIXFILTER) {
            COLORMATRIXFILTER cmf = (COLORMATRIXFILTER) filter;
            ret += convertAdjustColorFilter(cmf);
        }
        return ret;
    }

    public static String convertSymbolInstance(String name, MATRIX matrix, ColorTransform colorTransform, boolean cacheAsBitmap, int blendMode, List<FILTER> filters, boolean isVisible, RGBA backgroundColor, CLIPACTIONS clipActions, CharacterTag tag, HashMap<Integer, CharacterTag> characters, List<Tag> tags, FLAVersion flaVersion) {
        String ret = "";
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

        ret += "<DOMSymbolInstance libraryItemName=\"" + "Symbol " + tag.getCharacterId() + "\"";
        if (name != null) {
            ret += " name=\"" + xmlString(name) + "\"";
        }
        String blendModeStr = null;
        if (blendMode < BLENDMODES.length) {
            blendModeStr = BLENDMODES[blendMode];
        }
        if (blendModeStr != null) {
            ret += " blendMode=\"" + blendModeStr + "\"";
        }
        if (tag instanceof ShapeTag) {
            ret += " symbolType=\"graphic\" loop=\"loop\"";
        } else if (tag instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) tag;
            RECT spriteRect = sprite.getRect();
            double centerPoint3DX = twipToPixel(matrix.translateX + spriteRect.getWidth() / 2);
            double centerPoint3DY = twipToPixel(matrix.translateY + spriteRect.getHeight() / 2);
            ret += " centerPoint3DX=\"" + centerPoint3DX + "\" centerPoint3DY=\"" + centerPoint3DY + "\"";
        } else if (tag instanceof ButtonTag) {
            ret += " symbolType=\"button\"";
        }
        if (cacheAsBitmap) {
            ret += " cacheAsBitmap=\"true\"";
        }
        if (!isVisible && flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
            ret += " isVisible=\"false\"";
        }
        ret += ">";
        ret += "<matrix>";
        ret += convertMatrix(matrix);
        ret += "</matrix>";
        ret += "<transformationPoint><Point/></transformationPoint>";

        if (backgroundColor != null) {
            ret += "<MatteColor color=\"" + backgroundColor.toHexRGB() + "\"";
            if (backgroundColor.alpha != 255) {
                ret += " alpha=\"" + doubleToString(backgroundColor.getAlphaFloat()) + "\"";
            }
            ret += "/>";
        }
        if (colorTransform != null) {
            ret += "<color><Color";
            if (colorTransform.getRedMulti() != 255) {
                ret += " redMultiplier=\"" + (((float) colorTransform.getRedMulti()) / 255.0f) + "\"";
            }
            if (colorTransform.getGreenMulti() != 255) {
                ret += " greenMultiplier=\"" + (((float) colorTransform.getGreenMulti()) / 255.0f) + "\"";
            }
            if (colorTransform.getBlueMulti() != 255) {
                ret += " blueMultiplier=\"" + (((float) colorTransform.getBlueMulti()) / 255.0f) + "\"";
            }
            if (colorTransform.getAlphaMulti() != 255) {
                ret += " alphaMultiplier=\"" + (((float) colorTransform.getAlphaMulti()) / 255.0f) + "\"";
            }

            if (colorTransform.getRedAdd() != 0) {
                ret += " redOffset=\"" + colorTransform.getRedAdd() + "\"";
            }
            if (colorTransform.getGreenAdd() != 0) {
                ret += " greenOffset=\"" + colorTransform.getGreenAdd() + "\"";
            }
            if (colorTransform.getBlueAdd() != 0) {
                ret += " blueOffset=\"" + colorTransform.getBlueAdd() + "\"";
            }
            if (colorTransform.getAlphaAdd() != 0) {
                ret += " alphaOffset=\"" + colorTransform.getAlphaAdd() + "\"";
            }

            ret += "/></color>";
        }
        if (filters != null) {
            ret += "<filters>";
            for (FILTER f : filters) {
                ret += convertFilter(f);
            }
            ret += "</filters>";
        }
        if (tag instanceof DefineButtonTag) {
            ret += "<Actionscript><script><![CDATA[";
            ret += "on(press){\r\n";
            ret += convertActionScript(((DefineButtonTag) tag));
            ret += "}";
            ret += "]]></script></Actionscript>";
        }
        if (tag instanceof DefineButton2Tag) {
            DefineButton2Tag db2 = (DefineButton2Tag) tag;
            if (!db2.actions.isEmpty()) {
                ret += "<Actionscript><script><![CDATA[";
                for (BUTTONCONDACTION bca : db2.actions) {
                    ret += convertActionScript(bca);
                }
                ret += "]]></script></Actionscript>";
            }
        }
        if (clipActions != null) {
            ret += "<Actionscript><script><![CDATA[";
            for (CLIPACTIONRECORD rec : clipActions.clipActionRecords) {
                ret += convertActionScript(rec);
            }
            ret += "]]></script></Actionscript>";
        }
        ret += "</DOMSymbolInstance>";
        return ret;
    }

    private static String convertActionScript(ASMSource as) {
        HilightedTextWriter writer = new HilightedTextWriter(Configuration.getCodeFormatting(), false);
        try {
            Action.actionsToSource(as, as.getActions(), as.toString(), writer);
        } catch (InterruptedException ex) {
            Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    private static long getTimestamp() {
        return new Date().getTime() / 1000;
    }

    public static String convertLibrary(SWF swf, Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, List<Integer> nonLibraryShapes, String backgroundColor, List<Tag> tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion) {

        //TODO: Imported assets
        //linkageImportForRS="true" linkageIdentifier="xxx" linkageURL="yyy.swf"
        String ret = "";
        List<String> media = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if ((symbol instanceof ShapeTag) && nonLibraryShapes.contains(symbol.getCharacterId())) {
                continue; //shapes with 1 ocurrence and single layer are not added to library
            }
            if ((symbol instanceof ShapeTag) || (symbol instanceof DefineSpriteTag) || (symbol instanceof ButtonTag)) {
                String symbolStr = "";

                symbolStr += "<DOMSymbolItem xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" name=\"Symbol " + symbol.getCharacterId() + "\" lastModified=\"" + getTimestamp() + "\""; //TODO:itemID
                if (symbol instanceof ShapeTag) {
                    symbolStr += " symbolType=\"graphic\"";
                } else if (symbol instanceof ButtonTag) {
                    symbolStr += " symbolType=\"button\"";
                    if (((ButtonTag) symbol).trackAsMenu()) {
                        symbolStr += "  trackAsMenu=\"true\"";
                    }
                }
                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr += " linkageClassName=\"" + xmlString(characterClasses.get(symbol.getCharacterId())) + "\"";
                }
                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr += " linkageIdentifier=\"" + xmlString(characterVariables.get(symbol.getCharacterId())) + "\"";
                }
                if (linkageExportForAS) {
                    symbolStr += " linkageExportForAS=\"true\"";
                }
                symbolStr += ">";
                symbolStr += "<timeline>";
                String itemIcon = null;
                if (symbol instanceof ButtonTag) {
                    itemIcon = "0";
                    symbolStr += "<DOMTimeline name=\"Symbol " + symbol.getCharacterId() + "\" currentFrame=\"0\">";
                    symbolStr += "<layers>";

                    ButtonTag button = (ButtonTag) symbol;
                    List<BUTTONRECORD> records = button.getRecords();
                    String[] frames = {"", "", "", ""};

                    int maxDepth = 0;
                    for (BUTTONRECORD rec : records) {
                        if (rec.placeDepth > maxDepth) {
                            maxDepth = rec.placeDepth;
                        }
                    }
                    for (int i = maxDepth; i >= 1; i--) {
                        symbolStr += "<DOMLayer name=\"Layer " + (maxDepth - i + 1) + "\"";
                        if (i == 1) {
                            symbolStr += " current=\"true\" isSelected=\"true\"";
                        }
                        symbolStr += " color=\"" + randomOutlineColor() + "\">";
                        symbolStr += "<frames>";
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
                                    String recCharStr = "";
                                    if (character instanceof TextTag) {
                                        recCharStr = convertText(null, tags, (TextTag) character, matrix, filters, null);
                                    } else if (character instanceof DefineVideoStreamTag) {
                                        recCharStr = convertVideoInstance(null, matrix, (DefineVideoStreamTag) character, null);
                                    } else {
                                        recCharStr = convertSymbolInstance(null, matrix, colorTransformAlpha, false, blendMode, filters, true, null, null, characters.get(rec.characterId), characters, tags, flaVersion);
                                    }
                                    int duration = frame - lastFrame;
                                    lastFrame = frame;
                                    if (duration > 0) {
                                        if (duration > 1) {
                                            symbolStr += "<DOMFrame index=\"";
                                            symbolStr += (frame - duration);
                                            symbolStr += "\"";
                                            symbolStr += " duration=\"" + (duration - 1) + "\"";
                                            symbolStr += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                                            symbolStr += "<elements>";
                                            symbolStr += "</elements>";
                                            symbolStr += "</DOMFrame>";
                                        }
                                        symbolStr += "<DOMFrame index=\"";
                                        symbolStr += (frame - 1);
                                        symbolStr += "\"";
                                        symbolStr += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                                        symbolStr += "<elements>";
                                        symbolStr += recCharStr;
                                        symbolStr += "</elements>";
                                        symbolStr += "</DOMFrame>";
                                    }
                                }
                            }
                        }
                        symbolStr += "</frames>";
                        symbolStr += "</DOMLayer>";
                    }
                    symbolStr += "</layers>";
                    symbolStr += "</DOMTimeline>";
                } else if (symbol instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) symbol;
                    if (sprite.subTags.isEmpty()) { //probably AS2 class
                        continue;
                    }
                    symbolStr += convertTimeline(sprite.spriteId, nonLibraryShapes, backgroundColor, tags, sprite.getSubTags(), characters, "Symbol " + symbol.getCharacterId(), flaVersion, files);
                } else if (symbol instanceof ShapeTag) {
                    itemIcon = "1";
                    ShapeTag shape = (ShapeTag) symbol;
                    symbolStr += "<DOMTimeline name=\"Symbol " + symbol.getCharacterId() + "\" currentFrame=\"0\">";
                    symbolStr += "<layers>";
                    symbolStr += convertShape(characters, null, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, true);
                    symbolStr += "</layers>";
                    symbolStr += "</DOMTimeline>";
                }
                symbolStr += "</timeline>";
                symbolStr += "</DOMSymbolItem>";
                symbolStr = prettyFormatXML(symbolStr);
                String symbolFile = "Symbol " + symbol.getCharacterId() + ".xml";
                files.put(symbolFile, Utf8Helper.getBytes(symbolStr));
                String symbLinkStr = "";
                symbLinkStr += "<Include href=\"" + symbolFile + "\"";
                if (itemIcon != null) {
                    symbLinkStr += " itemIcon=\"" + itemIcon + "\"";
                }
                symbLinkStr += " loadImmediate=\"false\"";
                if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
                    symbLinkStr += " lastModified=\"" + getTimestamp() + "\"";
                    //TODO: itemID=\"518de416-00000341\"
                }
                symbLinkStr += "/>";
                symbols.add(symbLinkStr);
            } else if (symbol instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) symbol;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SerializableImage image = imageTag.getImage();
                String format = imageTag.getImageFormat();
                try {
                    ImageIO.write(image.getBufferedImage(), format.toUpperCase(), baos);
                } catch (IOException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
                String symbolFile = "bitmap" + symbol.getCharacterId() + "." + imageTag.getImageFormat();
                files.put(symbolFile, baos.toByteArray());
                String mediaLinkStr = "<DOMBitmapItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp() + "\" externalFileSize=\"" + baos.toByteArray().length + "\"";
                switch (format) {
                    case "png":
                    case "gif":
                        mediaLinkStr += " useImportedJPEGData=\"false\" compressionType=\"lossless\" originalCompressionType=\"lossless\"";
                        break;
                    case "jpg":
                        mediaLinkStr += " isJPEG=\"true\"";
                        break;
                }
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    mediaLinkStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                }
                mediaLinkStr += " quality=\"50\" href=\"" + symbolFile + "\" bitmapDataHRef=\"M " + (media.size() + 1) + " " + getTimestamp() + ".dat\" frameRight=\"" + image.getWidth() + "\" frameBottom=\"" + image.getHeight() + "\"/>\n";
                media.add(mediaLinkStr);

            } else if ((symbol instanceof SoundStreamHeadTypeTag) || (symbol instanceof DefineSoundTag)) {
                int soundFormat = 0;
                int soundRate = 0;
                boolean soundType = false;
                boolean soundSize = false;
                long soundSampleCount = 0;
                byte[] soundData = new byte[0];
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
                            soundData = bl.getData();
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
                    soundData = sound.soundData;
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
                    SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(soundData), swf.version);
                    exportFormat = "wav";
                    try {
                        int adpcmCodeSize = (int) sis.readUB(2);
                        bits = 2 + adpcmCodeSize;
                    } catch (IOException ex) {
                        Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (soundFormat == SoundFormat.FORMAT_MP3) {
                    exportFormat = "mp3";
                    if (!soundType) { //mono
                        format += 1;
                    }
                    format += 4; //quality best
                    SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(soundData), swf.version);
                    try {
                        MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
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
                        Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                SoundTag st = (SoundTag) symbol;
                SoundFormat fmt = st.getSoundFormat();
                byte[] data = new byte[0];
                try {
                    data = swf.exportSound(st, SoundExportMode.MP3_WAV);
                } catch (IOException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }

                String symbolFile = "sound" + symbol.getCharacterId() + "." + exportFormat;
                files.put(symbolFile, data);
                String mediaLinkStr = "<DOMSoundItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp() + "\" externalFileSize=\"" + data.length + "\"";
                mediaLinkStr += " href=\"" + symbolFile + "\"";
                mediaLinkStr += " format=\"";
                mediaLinkStr += rateMap[soundRate] + "kHz";
                mediaLinkStr += " " + (soundSize ? "16bit" : "8bit");
                mediaLinkStr += " " + (soundType ? "Stereo" : "Mono");
                mediaLinkStr += "\"";
                mediaLinkStr += " exportFormat=\"" + format + "\" exportBits=\"" + bits + "\" sampleCount=\"" + soundSampleCount + "\"";

                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    mediaLinkStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                }

                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    mediaLinkStr += " linkageExportForAS=\"true\" linkageIdentifier=\"" + xmlString(characterVariables.get(symbol.getCharacterId())) + "\"";
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

                byte[] data = new byte[0];
                try {
                    data = swf.exportMovie(video, MovieExportMode.FLV);
                } catch (IOException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
                String symbolFile = "movie" + symbol.getCharacterId() + "." + "flv";
                String mediaLinkStr = "";
                if (data.length == 0) { //Video has zero length, this probably means it is "Video - Actionscript-controlled"
                    long ts = getTimestamp();
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
                    mediaLinkStr = "<DOMVideoItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp() + "\" externalFileSize=\"" + data.length + "\"";
                    mediaLinkStr += " href=\"" + symbolFile + "\"";
                    mediaLinkStr += " videoType=\"" + videoType + "\"";
                    mediaLinkStr += " fps=\"" + swf.frameRate + "\"";
                    mediaLinkStr += " width=\"" + video.width + "\"";
                    mediaLinkStr += " height=\"" + video.height + "\"";
                    double len = ((double) video.numFrames) / ((double) swf.frameRate);
                    mediaLinkStr += " length=\"" + len + "\"";
                    if (characterClasses.containsKey(symbol.getCharacterId())) {
                        mediaLinkStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterId()) + "\"";
                    }
                    if (characterVariables.containsKey(symbol.getCharacterId())) {
                        mediaLinkStr += " linkageExportForAS=\"true\" linkageIdentifier=\"" + xmlString(characterVariables.get(symbol.getCharacterId())) + "\"";
                    }
                    mediaLinkStr += "/>\n";
                }
                media.add(mediaLinkStr);
            }

        }
        if (!media.isEmpty()) {
            ret += "<media>";
            for (String m : media) {
                ret += m;
            }
            ret += "</media>";
        }
        if (!symbols.isEmpty()) {
            ret += "<symbols>";
            for (String s : symbols) {
                ret += s;
            }
            ret += "</symbols>";
        }
        return ret;
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
            System.err.println(input);
            Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Pretty print error", e);
            return input;
        }
    }

    private static String convertFrame(boolean shapeTween, HashMap<Integer, CharacterTag> characters, List<Tag> tags, SoundStreamHeadTypeTag soundStreamHead, StartSoundTag startSound, int frame, int duration, String actionScript, String elements, HashMap<String, byte[]> files) {
        String ret = "";
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

        ret += "<DOMFrame index=\"" + (frame) + "\"";
        if (duration > 1) {
            ret += " duration=\"" + duration + "\"";
        }
        if (shapeTween) {
            ret += " tweenType=\"shape\" keyMode=\"" + KEY_MODE_SHAPE_TWEEN + "\"";
        } else {
            ret += " keyMode=\"" + KEY_MODE_NORMAL + "\"";
        }
        String soundEnvelopeStr = "";
        if (soundStreamHead != null && startSound == null) {
            String soundName = "sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat();
            ret += " soundName=\"" + soundName + "\"";
            ret += " soundSync=\"stream\"";
            soundEnvelopeStr += "<SoundEnvelope>";
            soundEnvelopeStr += "<SoundEnvelopePoint level0=\"32768\" level1=\"32768\"/>";
            soundEnvelopeStr += "</SoundEnvelope>";
        }
        if (startSound != null && sound != null) {
            String soundName = "sound" + sound.soundId + "." + sound.getExportFormat();
            ret += " soundName=\"" + soundName + "\"";
            if (startSound.soundInfo.hasInPoint) {
                ret += " inPoint44=\"" + startSound.soundInfo.inPoint + "\"";
            }
            if (startSound.soundInfo.hasOutPoint) {
                ret += " outPoint44=\"" + startSound.soundInfo.outPoint + "\"";
            }
            if (startSound.soundInfo.hasLoops) {
                if (startSound.soundInfo.loopCount == 32767) {
                    ret += " soundLoopMode=\"loop\"";
                }
                ret += " soundLoop=\"" + startSound.soundInfo.loopCount + "\"";
            }

            if (startSound.soundInfo.syncStop) {
                ret += " soundSync=\"stop\"";
            } else if (startSound.soundInfo.syncNoMultiple) {
                ret += " soundSync=\"start\"";
            }
            soundEnvelopeStr += "<SoundEnvelope>";
            if (startSound.soundInfo.hasEnvelope) {
                for (SOUNDENVELOPE env : startSound.soundInfo.envelopeRecords) {
                    soundEnvelopeStr += "<SoundEnvelopePoint mark44=\"" + env.pos44 + "\" level0=\"" + env.leftLevel + "\" level1=\"" + env.rightLevel + "\"/>";
                }

                if (startSound.soundInfo.envelopeRecords.length == 1
                        && startSound.soundInfo.envelopeRecords[0].leftLevel == 32768
                        && startSound.soundInfo.envelopeRecords[0].pos44 == 0
                        && startSound.soundInfo.envelopeRecords[0].rightLevel == 0) {
                    ret += " soundEffect=\"left channel\"";
                } else if (startSound.soundInfo.envelopeRecords.length == 1
                        && startSound.soundInfo.envelopeRecords[0].leftLevel == 0
                        && startSound.soundInfo.envelopeRecords[0].pos44 == 0
                        && startSound.soundInfo.envelopeRecords[0].rightLevel == 32768) {
                    ret += " soundEffect=\"right channel\"";
                } else if (startSound.soundInfo.envelopeRecords.length == 2
                        && startSound.soundInfo.envelopeRecords[0].leftLevel == 32768
                        && startSound.soundInfo.envelopeRecords[0].pos44 == 0
                        && startSound.soundInfo.envelopeRecords[0].rightLevel == 0
                        && startSound.soundInfo.envelopeRecords[1].leftLevel == 0
                        && startSound.soundInfo.envelopeRecords[1].pos44 == sound.soundSampleCount
                        && startSound.soundInfo.envelopeRecords[1].rightLevel == 32768) {
                    ret += " soundEffect=\"fade left to right\"";
                } else if (startSound.soundInfo.envelopeRecords.length == 2
                        && startSound.soundInfo.envelopeRecords[0].leftLevel == 0
                        && startSound.soundInfo.envelopeRecords[0].pos44 == 0
                        && startSound.soundInfo.envelopeRecords[0].rightLevel == 32768
                        && startSound.soundInfo.envelopeRecords[1].leftLevel == 32768
                        && startSound.soundInfo.envelopeRecords[1].pos44 == sound.soundSampleCount
                        && startSound.soundInfo.envelopeRecords[1].rightLevel == 0) {
                    ret += " soundEffect=\"fade right to left\"";
                } else {
                    ret += " soundEffect=\"custom\"";
                }
                //TODO: fade in, fade out

            } else {
                soundEnvelopeStr += "<SoundEnvelopePoint level0=\"32768\" level1=\"32768\"/>";
            }
            soundEnvelopeStr += "</SoundEnvelope>";
        }
        ret += ">";

        ret += soundEnvelopeStr;
        if (!actionScript.isEmpty()) {
            ret += "<Actionscript><script><![CDATA[";
            ret += actionScript;
            ret += "]]></script></Actionscript>";
        }
        ret += "<elements>";
        ret += elements;
        ret += "</elements>";
        ret += "</DOMFrame>";
        return ret;
    }

    private static String convertVideoInstance(String instanceName, MATRIX matrix, DefineVideoStreamTag video, CLIPACTIONS clipActions) {
        String ret = "<DOMVideoInstance libraryItemName=\"movie" + video.characterID + ".flv\" frameRight=\"" + (20 * video.width) + "\" frameBottom=\"" + (20 * video.height) + "\"";
        if (instanceName != null) {
            ret += " name=\"" + xmlString(instanceName) + "\"";
        }
        ret += ">";
        ret += "<matrix>";
        ret += convertMatrix(matrix);
        ret += "</matrix>";
        ret += "<transformationPoint>";
        ret += "<Point />";
        ret += "</transformationPoint>";
        ret += "</DOMVideoInstance>";
        return ret;
    }

    private static String convertFrames(String prevStr, String afterStr, List<Integer> nonLibraryShapes, List<Tag> tags, List<Tag> timelineTags, HashMap<Integer, CharacterTag> characters, int depth, FLAVersion flaVersion, HashMap<String, byte[]> files) {
        String ret = "";
        prevStr += "<frames>";
        int frame = -1;
        String elements = "";
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

        for (Tag t : timelineTags) {
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
                elements = "";

                if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(characterId) || shapeTweener != null)) {
                    ShapeTag shape = (ShapeTag) character;
                    elements += convertShape(characters, matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false);
                    shapeTween = false;
                    shapeTweener = null;
                } else if (character != null) {
                    if (character instanceof MorphShapeTag) {
                        MorphShapeTag m = (MorphShapeTag) character;
                        elements += convertShape(characters, matrix, 3, m.getStartEdges().shapeRecords, m.getFillStyles().getStartFillStyles(), m.getLineStyles().getStartLineStyles(m.getShapeNum()), true, false);
                        shapeTween = true;
                    } else {
                        shapeTween = false;
                        if (character instanceof TextTag) {
                            elements += convertText(instanceName, tags, (TextTag) character, matrix, filters, clipActions);
                        } else if (character instanceof DefineVideoStreamTag) {
                            elements += convertVideoInstance(instanceName, matrix, (DefineVideoStreamTag) character, clipActions);
                        } else {
                            elements += convertSymbolInstance(instanceName, matrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, character, characters, tags, flaVersion);
                        }
                    }
                }

                frame++;
                if (!elements.equals(lastElements) && frame > 0) {
                    ret += convertFrame(lastShapeTween, characters, tags, null, null, frame - duration, duration, "", lastElements, files);
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
            ret += convertFrame(lastShapeTween, characters, tags, null, null, (frame - duration < 0 ? 0 : frame - duration), duration, "", lastElements, files);
        }
        afterStr = "</frames>" + afterStr;
        if (!ret.isEmpty()) {
            ret = prevStr + ret + afterStr;
        }
        return ret;
    }

    public static String convertFonts(List<Tag> tags) {
        String ret = "";
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
                    fontName = font.getFontName();
                }
                int fontStyle = font.getFontStyle();
                String installedFont;
                if ((installedFont = FontTag.isFontInstalled(fontName)) != null) {
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
                    int codes[] = CharacterRanges.rangeCodes(r);
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
                ret += "<DOMFontItem name=\"Font " + fontId + "\" font=\"" + xmlString(fontName) + "\" size=\"0\" id=\"" + fontId + "\" embedRanges=\"" + embedRanges + "\"" + (!"".equals(embeddedCharacters) ? " embeddedCharacters=\"" + xmlString(embeddedCharacters) + "\"" : "") + " />";
            }

        }

        if (!"".equals(ret)) {
            ret = "<fonts>" + ret + "</fonts>";
        }

        return ret;
    }

    public static String convertActionScriptLayer(int spriteId, List<Tag> tags, List<Tag> timeLineTags, String backgroundColor) {
        String ret = "";

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
                        ret += "<DOMFrame index=\"" + (frame - duration) + "\"";
                        if (duration > 1) {
                            ret += " duration=\"" + duration + "\"";
                        }
                        ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                        ret += "<elements>";
                        ret += "</elements>";
                        ret += "</DOMFrame>";
                    }
                    ret += "<DOMFrame index=\"" + frame + "\"";
                    ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                    ret += "<Actionscript><script><![CDATA[";
                    ret += script;
                    ret += "]]></script></Actionscript>";
                    ret += "<elements>";
                    ret += "</elements>";
                    ret += "</DOMFrame>";
                    script = "";
                    duration = 0;
                }
                frame++;
            }
        }
        if (!ret.isEmpty()) {
            ret = "<DOMLayer name=\"Script Layer\" color=\"" + randomOutlineColor() + "\">"
                    + "<frames>"
                    + ret
                    + "</frames>"
                    + "</DOMLayer>";
        }
        return ret;
    }

    public static String convertLabelsLayer(int spriteId, List<Tag> tags, List<Tag> timeLineTags, String backgroundColor) {
        String ret = "";
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
                        ret += "<DOMFrame index=\"" + (frame - duration) + "\"";
                        if (duration > 1) {
                            ret += " duration=\"" + duration + "\"";
                        }
                        ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                        ret += "<elements>";
                        ret += "</elements>";
                        ret += "</DOMFrame>";
                    }
                    ret += "<DOMFrame index=\"" + frame + "\"";
                    ret += " keyMode=\"" + KEY_MODE_NORMAL + "\"";
                    ret += " name=\"" + frameLabel + "\"";
                    if (isAnchor) {
                        ret += " labelType=\"anchor\" bookmark=\"true\"";
                    } else {
                        ret += " labelType=\"name\"";
                    }
                    ret += ">";
                    ret += "<elements>";
                    ret += "</elements>";
                    ret += "</DOMFrame>";
                    frameLabel = "";
                    duration = 0;
                }
                frame++;
            }
        }
        if (!ret.isEmpty()) {
            ret = "<DOMLayer name=\"Labels Layer\" color=\"" + randomOutlineColor() + "\">"
                    + "<frames>"
                    + ret
                    + "</frames>"
                    + "</DOMLayer>";
        }
        return ret;
    }

    public static String convertSoundLayer(int layerIndex, String backgroundColor, HashMap<Integer, CharacterTag> characters, List<Tag> tags, List<Tag> timeLineTags, HashMap<String, byte[]> files) {
        String ret = "";
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
                            if (!files.containsKey("sound" + s.soundId + "." + s.getExportFormat())) { //Sound was not exported
                                startSound = null; //ignore
                            }
                            break;
                        }
                    }
                }

            }
            if (t instanceof SoundStreamHeadTypeTag) {
                soundStreamHead = (SoundStreamHeadTypeTag) t;
                if (!files.containsKey("sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat())) { //Sound was not exported
                    soundStreamHead = null; //ignore
                }
            }
            if (t instanceof ShowFrameTag) {
                if (soundStreamHead != null || startSound != null) {
                    if (lastSoundStreamHead != null || lastStartSound != null) {
                        ret += convertFrame(false, characters, tags, lastSoundStreamHead, lastStartSound, frame, duration, "", "", files);
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
            ret += convertFrame(false, characters, tags, lastSoundStreamHead, lastStartSound, frame, duration, "", "", files);
        }
        if (!ret.isEmpty()) {
            ret = "<DOMLayer name=\"Layer " + layerIndex + "\" color=\"" + randomOutlineColor() + "\">"
                    + "<frames>" + ret + "</frames>"
                    + "</DOMLayer>";
        }
        return ret;
    }

    private static String randomOutlineColor() {
        RGB outlineColor = new RGB();
        Random rnd = new Random();
        do {
            outlineColor.red = rnd.nextInt(256);
            outlineColor.green = rnd.nextInt(256);
            outlineColor.blue = rnd.nextInt(256);
        } while ((outlineColor.red + outlineColor.green + outlineColor.blue) / 3 < 128);
        return outlineColor.toHexRGB();
    }

    public static String convertTimeline(int spriteId, List<Integer> nonLibraryShapes, String backgroundColor, List<Tag> tags, List<Tag> timelineTags, HashMap<Integer, CharacterTag> characters, String name, FLAVersion flaVersion, HashMap<String, byte[]> files) {
        String ret = "";
        ret += "<DOMTimeline name=\"" + name + "\">";
        ret += "<layers>";

        ret += convertLabelsLayer(spriteId, tags, timelineTags, backgroundColor);
        ret += convertActionScriptLayer(spriteId, tags, timelineTags, backgroundColor);

        int layerCount = getLayerCount(timelineTags);
        Stack<Integer> parentLayers = new Stack<>();
        int index = 0;
        for (int d = layerCount; d >= 1; d--, index++) {
            for (Tag t : timelineTags) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                    if (po.getClipDepth() == d) {
                        for (int m = po.getDepth(); m < po.getClipDepth(); m++) {
                            parentLayers.push(index);
                        }

                        ret += "<DOMLayer name=\"Layer " + (index + 1) + "\" color=\"" + randomOutlineColor() + "\" ";
                        ret += " layerType=\"mask\" locked=\"true\"";
                        ret += ">";
                        ret += convertFrames("", "", nonLibraryShapes, tags, timelineTags, characters, po.getDepth(), flaVersion, files);
                        ret += "</DOMLayer>";
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
            String cf = convertFrames(layerPrev, layerAfter, nonLibraryShapes, tags, timelineTags, characters, d, flaVersion, files);
            if (cf.isEmpty()) {
                index--;
            }
            ret += cf;
        }

        int soundLayerIndex = layerCount;
        layerCount++;
        ret += convertSoundLayer(soundLayerIndex, backgroundColor, characters, tags, timelineTags, files);
        ret += "</layers>";
        ret += "</DOMTimeline>";
        return ret;
    }

    private static void writeFile(AbortRetryIgnoreHandler handler, final byte[] data, final String file) throws IOException {
        new RetryTask(new RunnableIOEx() {
            @Override
            public void run() throws IOException {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }
            }
        }, handler).run();
    }

    private static Map<Integer, String> getCharacterClasses(List<Tag> tags) {
        Map<Integer, String> ret = new HashMap<>();
        for (Tag t : tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tags.length; i++) {
                    if (!ret.containsKey(sc.tags[i]) && !ret.containsValue(sc.names[i])) {
                        ret.put(sc.tags[i], sc.names[i]);
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

    public static String convertText(String instanceName, List<Tag> tags, TextTag tag, MATRIX matrix, List<FILTER> filters, CLIPACTIONS clipActions) {
        String ret = "";
        if (matrix == null) {
            matrix = new MATRIX();
        }
        CSMTextSettingsTag csmts = null;
        String filterStr = "";
        if (filters != null) {
            filterStr += "<filters>";
            for (FILTER f : filters) {
                filterStr += convertFilter(f);
            }
            filterStr += "</filters>";
        }

        for (Tag t : tags) {
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
        String matStr = "";
        matStr += "<matrix>";
        RECT bounds = tag.getBounds();
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            MATRIX textMatrix = tag.getTextMatrix();
            matrix = matrix.merge(textMatrix);
        } else {
            matrix.translateX += bounds.Xmin;
            matrix.translateY += bounds.Ymin;

            //I do not know why, but there is (always?) 2px difference
            matrix.translateX += 2 * 20;
            matrix.translateY += 2 * 20;
        }
        matStr += convertMatrix(matrix);
        matStr += "</matrix>";
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            List<TEXTRECORD> textRecords = new ArrayList<>();
            if (tag instanceof DefineTextTag) {
                textRecords = ((DefineTextTag) tag).textRecords;
            } else if (tag instanceof DefineText2Tag) {
                textRecords = ((DefineText2Tag) tag).textRecords;
            }

            looprec:
            for (TEXTRECORD rec : textRecords) {
                if (rec.styleFlagsHasFont) {
                    for (Tag t : tags) {
                        if (t instanceof FontTag) {
                            FontTag ft = (FontTag) t;
                            if (ft.getFontId() == rec.fontId) {
                                if (ft.isSmall()) {
                                    fontRenderingMode = "bitmap";
                                    break looprec;
                                }
                            }
                        }
                    }
                }
            }

            ret += "<DOMStaticText";
            if (fontRenderingMode != null) {
                ret += " fontRenderingMode=\"" + fontRenderingMode + "\"";
            }
            if (instanceName != null) {
                ret += " instanceName=\"" + xmlString(instanceName) + "\"";
            }
            ret += antiAlias;
            Map<String, Object> attrs = TextTag.getTextRecordsAttributes(textRecords, tags);

            ret += " width=\"" + tag.getBounds().getWidth() / 2 + "\" height=\"" + tag.getBounds().getHeight() + "\" autoExpand=\"true\" isSelectable=\"false\">";
            ret += matStr;

            ret += "<textRuns>";
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
                    font = null;
                    for (Tag t : tags) {
                        if (t instanceof FontTag) {
                            if (((FontTag) t).getFontId() == fontId) {
                                font = (FontTag) t;
                            }
                        }
                        if (t instanceof DefineFontNameTag) {
                            if (((DefineFontNameTag) t).fontId == fontId) {
                                fontName = ((DefineFontNameTag) t).fontName;
                            }
                        }
                    }
                    if ((fontName == null) && (font != null)) {
                        fontName = font.getFontName();
                    }
                    int fontStyle = 0;
                    if (font != null) {
                        fontStyle = font.getFontStyle();
                    }
                    String installedFont;
                    if ((installedFont = FontTag.isFontInstalled(fontName)) != null) {
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
                    ret += "<DOMTextRun>";
                    ret += "<characters>" + xmlString((newline ? "\r" : "") + rec.getText(font)) + "</characters>";
                    ret += "<textAttrs>";

                    ret += "<DOMTextAttrs aliasText=\"false\" rotation=\"true\" size=\"" + twipToPixel(textHeight) + "\" bitmapSize=\"" + textHeight + "\"";
                    ret += " letterSpacing=\"" + doubleToString(twipToPixel(letterSpacings.get(r))) + "\"";
                    ret += " indent=\"" + doubleToString(twipToPixel((int) attrs.get("indent"))) + "\"";
                    ret += " leftMargin=\"" + doubleToString(twipToPixel(leftMargins.get(r))) + "\"";
                    ret += " lineSpacing=\"" + doubleToString(twipToPixel((int) attrs.get("lineSpacing"))) + "\"";
                    ret += " rightMargin=\"" + doubleToString(twipToPixel((int) attrs.get("rightMargin"))) + "\"";

                    if (textColor != null) {
                        ret += " fillColor=\"" + textColor.toHexRGB() + "\"";
                    } else if (textColorA != null) {
                        ret += " fillColor=\"" + textColorA.toHexRGB() + "\" alpha=\"" + textColorA.getAlphaFloat() + "\"";
                    }
                    ret += " face=\"" + psFontName + "\"";
                    ret += "/>";

                    ret += "</textAttrs>";
                    ret += "</DOMTextRun>";
                }
            }
            ret += "</textRuns>";
            ret += filterStr;
            ret += "</DOMStaticText>";
        } else if (tag instanceof DefineEditTextTag) {
            DefineEditTextTag det = (DefineEditTextTag) tag;
            String tagName;
            for (Tag t : tags) {
                if (t instanceof FontTag) {
                    FontTag ft = (FontTag) t;
                    if (ft.getFontId() == det.fontId) {
                        if (ft.isSmall()) {
                            fontRenderingMode = "bitmap";
                            break;
                        }
                    }
                }
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
            ret += "<" + tagName;
            if (fontRenderingMode != null) {
                ret += " fontRenderingMode=\"" + fontRenderingMode + "\"";
            }
            if (instanceName != null) {
                ret += " name=\"" + xmlString(instanceName) + "\"";
            }
            ret += antiAlias;
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
            ret += " width=\"" + width + "\"";
            ret += " height=\"" + height + "\"";
            if (det.border) {
                ret += "  border=\"true\"";
            }
            if (det.html) {
                ret += " renderAsHTML=\"true\"";
            }
            if (det.noSelect) {
                ret += " isSelectable=\"false\"";
            }
            if (det.multiline && det.wordWrap) {
                ret += " lineType=\"multiline\"";
            } else if (det.multiline && (!det.wordWrap)) {
                ret += " lineType=\"multiline no wrap\"";
            } else if (det.password) {
                ret += " lineType=\"password\"";
            }
            if (det.hasMaxLength) {
                ret += " maxCharacters=\"" + det.maxLength + "\"";
            }
            if (!det.variableName.isEmpty()) {
                ret += " variableName=\"" + det.variableName + "\"";
            }
            ret += ">";
            ret += matStr;
            ret += "<textRuns>";
            String txt = "";
            if (det.hasText) {
                txt = det.initialText;
            }

            if (det.html) {
                ret += convertHTMLText(tags, det, txt);
            } else {
                ret += "<DOMTextRun>";
                ret += "<characters>" + xmlString(txt) + "</characters>";
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
                            fontName = ft.getFontName();
                        }
                        italic = ft.isItalic();
                        bold = ft.isBold();
                        size = det.fontHeight;
                        fontFace = fontName;
                        String installedFont = null;
                        if ((installedFont = FontTag.isFontInstalled(fontName)) != null) {
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
                    alignment = alignNames[det.align];
                }
                ret += "<textAttrs>";
                ret += "<DOMTextAttrs";
                if (alignment != null) {
                    ret += " alignment=\"" + alignment + "\"";
                }
                ret += " rotation=\"true\""; //?
                if (indent > -1) {
                    ret += " indent=\"" + twipToPixel(indent) + "\"";
                }
                if (leftMargin > -1) {
                    ret += " leftMargin=\"" + twipToPixel(leftMargin) + "\"";
                }
                if (lineSpacing > -1) {
                    ret += " lineSpacing=\"" + twipToPixel(lineSpacing) + "\"";
                }
                if (rightMargin > -1) {
                    ret += " rightMargin=\"" + twipToPixel(rightMargin) + "\"";
                }
                if (size > -1) {
                    ret += " size=\"" + twipToPixel(size) + "\"";
                    ret += " bitmapSize=\"" + size + "\"";
                }
                if (fontFace != null) {
                    ret += " face=\"" + fontFace + "\"";
                }
                if (textColor != null) {
                    ret += " fillColor=\"" + textColor.toHexRGB() + "\" alpha=\"" + textColor.getAlphaFloat() + "\"";
                }
                ret += "/>";
                ret += "</textAttrs>";
                ret += "</DOMTextRun>";
            }
            ret += "</textRuns>";
            ret += filterStr;
            ret += "</" + tagName + ">";
        }
        return ret;
    }

    public static void convertSWF(AbortRetryIgnoreHandler handler, SWF swf, String swfFileName, String outfile, boolean compressed, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion flaVersion) throws IOException {

        FileAttributesTag fa = null;
        for (Tag t : swf.tags) {
            if (t instanceof FileAttributesTag) {
                fa = (FileAttributesTag) t;
            }
        }

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
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                if (!outDir.exists()) {
                    throw new IOException("cannot create directory " + outDir);
                }
            }
        }
        String domDocument = "";
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
        domDocument += "<DOMDocument xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" currentTimeline=\"1\" xflVersion=\"" + flaVersion.xflVersion() + "\" creatorInfo=\"" + generator + "\" platform=\"Windows\" versionInfo=\"Saved by " + generatorVerName + "\" majorVersion=\"" + generatorVersion + "\" buildNumber=\"\" nextSceneIdentifier=\"2\" playOptionsPlayLoop=\"false\" playOptionsPlayPages=\"false\" playOptionsPlayFrameActions=\"false\" autoSaveHasPrompted=\"true\"";
        domDocument += " backgroundColor=\"" + backgroundColor + "\"";
        domDocument += " frameRate=\"" + swf.frameRate + "\"";

        double width = twipToPixel(swf.displayRect.getWidth());
        double height = twipToPixel(swf.displayRect.getHeight());
        if (Double.compare(width, 550) != 0) {
            domDocument += " width=\"" + doubleToString(width) + "\"";
        }
        if (Double.compare(height, 400) != 0) {
            domDocument += " height=\"" + doubleToString(height) + "\"";
        }
        domDocument += ">";
        domDocument += convertFonts(swf.tags);
        domDocument += convertLibrary(swf, characterVariables, characterClasses, nonLibraryShapes, backgroundColor, swf.tags, characters, files, datfiles, flaVersion);
        domDocument += "<timelines>";
        domDocument += convertTimeline(0, nonLibraryShapes, backgroundColor, swf.tags, swf.tags, characters, "Scene 1", flaVersion, files);
        domDocument += "</timelines>";
        domDocument += "</DOMDocument>";
        domDocument = prettyFormatXML(domDocument);

        for (Tag t : swf.tags) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag dia = (DoInitActionTag) t;
                int chid = dia.getCharacterId();
                if (characters.containsKey(chid)) {
                    if (characters.get(chid) instanceof DefineSpriteTag) {
                        DefineSpriteTag sprite = (DefineSpriteTag) characters.get(chid);
                        if (sprite.subTags.isEmpty()) {
                            String data = convertActionScript(dia);
                            String expPath = dia.getExportName();
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
                            if (!cdir.exists()) {
                                if (!cdir.mkdirs()) {
                                    if (!cdir.exists()) {
                                        throw new IOException("cannot create directory " + cdir);
                                    }
                                }
                            }
                            writeFile(handler, Utf8Helper.getBytes(data), outDir.getAbsolutePath() + File.separator + expPath + ".as");
                        }
                    }
                }
            }
        }

        int flaSwfVersion = swf.version > flaVersion.maxSwfVersion() ? flaVersion.maxSwfVersion() : swf.version;
        String publishSettings = "<flash_profiles>\n"
                + "<flash_profile version=\"1.0\" name=\"Default\" current=\"true\">\n"
                + "  <PublishFormatProperties enabled=\"true\">\n"
                + "    <defaultNames>1</defaultNames>\n"
                + "    <flash>1</flash>\n"
                + "    <projectorWin>0</projectorWin>\n"
                + "    <projectorMac>0</projectorMac>\n"
                + "    <html>1</html>\n"
                + "    <gif>0</gif>\n"
                + "    <jpeg>0</jpeg>\n"
                + "    <png>0</png>\n"
                + (flaVersion.ordinal() >= FLAVersion.CC.ordinal() ? "    <svg>0</svg>\n" : "    <qt>0</qt>\n")
                + "    <rnwk>0</rnwk>\n"
                + "    <swc>0</swc>\n"
                + "    <flashDefaultName>1</flashDefaultName>\n"
                + "    <projectorWinDefaultName>1</projectorWinDefaultName>\n"
                + "    <projectorMacDefaultName>1</projectorMacDefaultName>\n"
                + "    <htmlDefaultName>1</htmlDefaultName>\n"
                + "    <gifDefaultName>1</gifDefaultName>\n"
                + "    <jpegDefaultName>1</jpegDefaultName>\n"
                + "    <pngDefaultName>1</pngDefaultName>\n"
                + (flaVersion.ordinal() >= FLAVersion.CC.ordinal() ? "    <svgDefaultName>1</svgDefaultName>\n" : "    <qtDefaultName>1</qtDefaultName>\n")
                + "    <rnwkDefaultName>1</rnwkDefaultName>\n"
                + "    <swcDefaultName>1</swcDefaultName>\n"
                + "    <flashFileName>" + baseName + ".swf</flashFileName>\n"
                + "    <projectorWinFileName>" + baseName + ".exe</projectorWinFileName>\n"
                + "    <projectorMacFileName>" + baseName + ".app</projectorMacFileName>\n"
                + "    <htmlFileName>" + baseName + ".html</htmlFileName>\n"
                + "    <gifFileName>" + baseName + ".gif</gifFileName>\n"
                + "    <jpegFileName>" + baseName + ".jpg</jpegFileName>\n"
                + "    <pngFileName>" + baseName + ".png</pngFileName>\n"
                + (flaVersion.ordinal() >= FLAVersion.CC.ordinal() ? "    <svgFileName>1</svgFileName>\n" : "    <qtFileName>1</qtFileName>\n")
                + "    <rnwkFileName>" + baseName + ".smil</rnwkFileName>\n"
                + "    <swcFileName>" + baseName + ".swc</swcFileName>\n"
                + "  </PublishFormatProperties>\n"
                + "  <PublishHtmlProperties enabled=\"true\">\n"
                + "    <VersionDetectionIfAvailable>0</VersionDetectionIfAvailable>\n"
                + "    <VersionInfo>12,0,0,0;11,2,0,0;11,1,0,0;10,3,0,0;10,2,153,0;10,1,52,0;9,0,124,0;8,0,24,0;7,0,14,0;6,0,79,0;5,0,58,0;4,0,32,0;3,0,8,0;2,0,1,12;1,0,0,1;</VersionInfo>\n"
                + "    <UsingDefaultContentFilename>1</UsingDefaultContentFilename>\n"
                + "    <UsingDefaultAlternateFilename>1</UsingDefaultAlternateFilename>\n"
                + "    <ContentFilename>" + baseName + "_content.html</ContentFilename>\n"
                + "    <AlternateFilename>" + baseName + "_alternate.html</AlternateFilename>\n"
                + "    <UsingOwnAlternateFile>0</UsingOwnAlternateFile>\n"
                + "    <OwnAlternateFilename></OwnAlternateFilename>\n"
                + "    <Width>" + width + "</Width>\n"
                + "    <Height>" + height + "</Height>\n"
                + "    <Align>0</Align>\n"
                + "    <Units>0</Units>\n"
                + "    <Loop>1</Loop>\n"
                + "    <StartPaused>0</StartPaused>\n"
                + "    <Scale>0</Scale>\n"
                + "    <HorizontalAlignment>1</HorizontalAlignment>\n"
                + "    <VerticalAlignment>1</VerticalAlignment>\n"
                + "    <Quality>4</Quality>\n"
                + "    <DeblockingFilter>0</DeblockingFilter>\n"
                + "    <WindowMode>0</WindowMode>\n"
                + "    <DisplayMenu>1</DisplayMenu>\n"
                + "    <DeviceFont>0</DeviceFont>\n"
                + "    <TemplateFileName></TemplateFileName>\n"
                + "    <showTagWarnMsg>1</showTagWarnMsg>\n"
                + "  </PublishHtmlProperties>\n"
                + "  <PublishFlashProperties enabled=\"true\">\n"
                + "    <TopDown></TopDown>\n"
                + "    <FireFox></FireFox>\n"
                + "    <Report>0</Report>\n"
                + "    <Protect>0</Protect>\n"
                + "    <OmitTraceActions>0</OmitTraceActions>\n"
                + "    <Quality>80</Quality>\n"
                + "    <DeblockingFilter>0</DeblockingFilter>\n"
                + "    <StreamFormat>0</StreamFormat>\n"
                + "    <StreamCompress>7</StreamCompress>\n"
                + "    <EventFormat>0</EventFormat>\n"
                + "    <EventCompress>7</EventCompress>\n"
                + "    <OverrideSounds>0</OverrideSounds>\n"
                + "    <Version>" + flaSwfVersion + "</Version>\n"
                + "    <ExternalPlayer>" + FLAVersion.swfVersionToPlayer(flaSwfVersion) + "</ExternalPlayer>\n"
                + "    <ActionScriptVersion>" + (useAS3 ? "3" : "2") + "</ActionScriptVersion>\n"
                + "    <PackageExportFrame>1</PackageExportFrame>\n"
                + "    <PackagePaths></PackagePaths>\n"
                + "    <AS3PackagePaths>.</AS3PackagePaths>\n"
                + "    <AS3ConfigConst>CONFIG::FLASH_AUTHORING=&quot;true&quot;;</AS3ConfigConst>\n"
                + "    <DebuggingPermitted>0</DebuggingPermitted>\n"
                + "    <DebuggingPassword></DebuggingPassword>\n"
                + "    <CompressMovie>" + (swf.compressed ? "1" : "0") + "</CompressMovie>\n"
                + "    <CompressionType>" + (swf.lzma ? "1" : "0") + "</CompressionType>\n"
                + "    <InvisibleLayer>1</InvisibleLayer>\n"
                + "    <DeviceSound>0</DeviceSound>\n"
                + "    <StreamUse8kSampleRate>0</StreamUse8kSampleRate>\n"
                + "    <EventUse8kSampleRate>0</EventUse8kSampleRate>\n"
                + "    <UseNetwork>" + (useNetwork ? 1 : 0) + "</UseNetwork>\n"
                + "    <DocumentClass>" + xmlString(characterClasses.containsKey(0) ? characterClasses.get(0) : "") + "</DocumentClass>\n"
                + "    <AS3Strict>2</AS3Strict>\n"
                + "    <AS3Coach>4</AS3Coach>\n"
                + "    <AS3AutoDeclare>4096</AS3AutoDeclare>\n"
                + "    <AS3Dialect>AS3</AS3Dialect>\n"
                + "    <AS3ExportFrame>1</AS3ExportFrame>\n"
                + "    <AS3Optimize>1</AS3Optimize>\n"
                + "    <ExportSwc>0</ExportSwc>\n"
                + "    <ScriptStuckDelay>15</ScriptStuckDelay>\n"
                + "    <IncludeXMP>1</IncludeXMP>\n"
                + "    <HardwareAcceleration>0</HardwareAcceleration>\n"
                + "    <AS3Flags>4102</AS3Flags>\n"
                + "    <DefaultLibraryLinkage>rsl</DefaultLibraryLinkage>\n"
                + "    <RSLPreloaderMethod>wrap</RSLPreloaderMethod>\n"
                + "    <RSLPreloaderSWF>$(AppConfig)/ActionScript 3.0/rsls/loader_animation.swf</RSLPreloaderSWF>\n"
                + ((flaVersion.ordinal() >= FLAVersion.CC.ordinal()) ? ("    <LibraryPath>\n"
                + "      <library-path-entry>\n"
                + "        <swc-path>$(AppConfig)/ActionScript 3.0/libs</swc-path>\n"
                + "        <linkage>merge</linkage>\n"
                + "      </library-path-entry>\n"
                + "      <library-path-entry>\n"
                + "        <swc-path>$(FlexSDK)/frameworks/libs/flex.swc</swc-path>\n"
                + "        <linkage>merge</linkage>\n"
                + "        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n"
                + "      </library-path-entry>\n"
                + "      <library-path-entry>\n"
                + "        <swc-path>$(FlexSDK)/frameworks/libs/core.swc</swc-path>\n"
                + "        <linkage>merge</linkage>\n"
                + "        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n"
                + "      </library-path-entry>\n"
                + "    </LibraryPath>\n"
                + "    <LibraryVersions>\n"
                + "    </LibraryVersions> ")
                : "    <LibraryPath>\n"
                + "      <library-path-entry>\n"
                + "        <swc-path>$(AppConfig)/ActionScript 3.0/libs</swc-path>\n"
                + "        <linkage>merge</linkage>\n"
                + "      </library-path-entry>\n"
                + "      <library-path-entry>\n"
                + "        <swc-path>$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc</swc-path>\n"
                + "        <linkage usesDefault=\"true\">rsl</linkage>\n"
                + "        <rsl-url>http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz</rsl-url>\n"
                + "        <policy-file-url>http://fpdownload.adobe.com/pub/swz/crossdomain.xml</policy-file-url>\n"
                + "        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n"
                + "      </library-path-entry>\n"
                + "    </LibraryPath>\n"
                + "    <LibraryVersions>\n"
                + "      <library-version>\n"
                + "        <swc-path>$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc</swc-path>\n"
                + "        <feature name=\"tlfText\" majorVersion=\"2\" minorVersion=\"0\" build=\"232\"/>\n"
                + "        <rsl-url>http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz</rsl-url>\n"
                + "        <policy-file-url>http://fpdownload.adobe.com/pub/swz/crossdomain.xml</policy-file-url>\n"
                + "        <rsl-url>textLayout_2.0.0.232.swz</rsl-url>\n"
                + "      </library-version>\n"
                + "    </LibraryVersions>\n")
                + "  </PublishFlashProperties>\n"
                + "  <PublishJpegProperties enabled=\"true\">\n"
                + "    <Width>" + width + "</Width>\n"
                + "    <Height>" + height + "</Height>\n"
                + "    <Progressive>0</Progressive>\n"
                + "    <DPI>4718592</DPI>\n"
                + "    <Size>0</Size>\n"
                + "    <Quality>80</Quality>\n"
                + "    <MatchMovieDim>1</MatchMovieDim>\n"
                + "  </PublishJpegProperties>\n"
                + "  <PublishRNWKProperties enabled=\"true\">\n"
                + "    <exportFlash>1</exportFlash>\n"
                + "    <flashBitRate>0</flashBitRate>\n"
                + "    <exportAudio>1</exportAudio>\n"
                + "    <audioFormat>0</audioFormat>\n"
                + "    <singleRateAudio>0</singleRateAudio>\n"
                + "    <realVideoRate>100000</realVideoRate>\n"
                + "    <speed28K>1</speed28K>\n"
                + "    <speed56K>1</speed56K>\n"
                + "    <speedSingleISDN>0</speedSingleISDN>\n"
                + "    <speedDualISDN>0</speedDualISDN>\n"
                + "    <speedCorporateLAN>0</speedCorporateLAN>\n"
                + "    <speed256K>0</speed256K>\n"
                + "    <speed384K>0</speed384K>\n"
                + "    <speed512K>0</speed512K>\n"
                + "    <exportSMIL>1</exportSMIL>\n"
                + "  </PublishRNWKProperties>\n"
                + "  <PublishGifProperties enabled=\"true\">\n"
                + "    <Width>" + width + "</Width>\n"
                + "    <Height>" + height + "</Height>\n"
                + "    <Animated>0</Animated>\n"
                + "    <MatchMovieDim>1</MatchMovieDim>\n"
                + "    <Loop>1</Loop>\n"
                + "    <LoopCount></LoopCount>\n"
                + "    <OptimizeColors>1</OptimizeColors>\n"
                + "    <Interlace>0</Interlace>\n"
                + "    <Smooth>1</Smooth>\n"
                + "    <DitherSolids>0</DitherSolids>\n"
                + "    <RemoveGradients>0</RemoveGradients>\n"
                + "    <TransparentOption></TransparentOption>\n"
                + "    <TransparentAlpha>128</TransparentAlpha>\n"
                + "    <DitherOption></DitherOption>\n"
                + "    <PaletteOption></PaletteOption>\n"
                + "    <MaxColors>255</MaxColors>\n"
                + "    <PaletteName></PaletteName>\n"
                + "  </PublishGifProperties>\n"
                + "  <PublishPNGProperties enabled=\"true\">\n"
                + "    <Width>" + width + "</Width>\n"
                + "    <Height>" + height + "</Height>\n"
                + "    <OptimizeColors>1</OptimizeColors>\n"
                + "    <Interlace>0</Interlace>\n"
                + "    <Transparent>0</Transparent>\n"
                + "    <Smooth>1</Smooth>\n"
                + "    <DitherSolids>0</DitherSolids>\n"
                + "    <RemoveGradients>0</RemoveGradients>\n"
                + "    <MatchMovieDim>1</MatchMovieDim>\n"
                + "    <DitherOption></DitherOption>\n"
                + "    <FilterOption></FilterOption>\n"
                + "    <PaletteOption></PaletteOption>\n"
                + "    <BitDepth>24-bit with Alpha</BitDepth>\n"
                + "    <MaxColors>255</MaxColors>\n"
                + "    <PaletteName></PaletteName>\n"
                + "  </PublishPNGProperties>\n"
                + ((flaVersion.ordinal() >= FLAVersion.CC.ordinal()) ? ""
                : ("  <PublishQTProperties enabled=\"true\">\n"
                + "    <Width>" + width + "</Width>\n"
                + "    <Height>" + height + "</Height>\n"
                + "    <MatchMovieDim>1</MatchMovieDim>\n"
                + "    <UseQTSoundCompression>0</UseQTSoundCompression>\n"
                + "    <AlphaOption></AlphaOption>\n"
                + "    <LayerOption></LayerOption>\n"
                + "    <QTSndSettings>00000000</QTSndSettings>\n"
                + "    <ControllerOption>0</ControllerOption>\n"
                + "    <Looping>0</Looping>\n"
                + "    <PausedAtStart>0</PausedAtStart>\n"
                + "    <PlayEveryFrame>0</PlayEveryFrame>\n"
                + "    <Flatten>1</Flatten>\n"
                + "  </PublishQTProperties>\n"))
                + "</flash_profile>\n"
                + "</flash_profiles>";

        if (compressed) {
            final String domDocumentF = domDocument;
            final String publishSettingsF = publishSettings;
            final String outfileF = outfile;
            new RetryTask(new RunnableIOEx() {
                @Override
                public void run() throws IOException {
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
                }
            }, handler).run();

        } else {

            if (!outDir.exists()) {
                if (!outDir.mkdirs()) {
                    if (!outDir.exists()) {
                        throw new IOException("cannot create directory " + outDir);
                    }
                }
            }
            writeFile(handler, Utf8Helper.getBytes(domDocument), outDir.getAbsolutePath() + File.separator + "DOMDocument.xml");
            writeFile(handler, Utf8Helper.getBytes(publishSettings), outDir.getAbsolutePath() + File.separator + "PublishSettings.xml");
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
                swf.exportActionScript(handler, outDir.getAbsolutePath(), ScriptExportMode.AS, parallel);
            } catch (Exception ex) {
                Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Error during ActionScript3 export", ex);
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

    public static String convertAdjustColorFilter(COLORMATRIXFILTER filter) {
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

        return "<AdjustColorFilter brightness=\"" + normBrightness(b) + "\" contrast=\"" + normContrast(c) + "\" saturation=\"" + normSaturation(s) + "\" hue=\"" + normHue(h) + "\"/>";
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
            Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Error while converting HTML", e);
        }
        return tparser.result;
    }

    private static String xmlString(String s) {
        return s.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("&", "&amp;").replace("\r\n", "&#xD;").replace("\r", "&#xD;").replace("\n", "&#xD;");
    }

    private static double twipToPixel(double tw) {
        return tw / 20.0;
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
                        fontName = ft.getFontName();
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
                alignment = alignNames[det.align];
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
                                if (f.equals(ft.getFontName())) {
                                    for (Tag u : tags) {
                                        if (u instanceof DefineFontNameTag) {
                                            if (((DefineFontNameTag) u).fontId == ft.getFontId()) {
                                                fontName = ((DefineFontNameTag) u).fontName;
                                            }
                                        }
                                    }
                                    if (fontName == null) {
                                        fontName = ft.getFontName();
                                    }
                                    String installedFont;
                                    if ((installedFont = FontTag.isFontInstalled(fontName)) != null) {
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
