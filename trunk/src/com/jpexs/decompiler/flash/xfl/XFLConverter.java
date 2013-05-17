/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import static com.jpexs.decompiler.flash.types.FILLSTYLE.*;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author JPEXS
 */
public class XFLConverter {

    public static final int KEY_MODE_NORMAL = 9728;
    public static final int KEY_MODE_CLASSIC_TWEEN = 22017;
    public static final int KEY_MODE_SHAPE_TWEEN = 17922;
    public static final int KEY_MODE_MOTION_TWEEN = 8195;

    private XFLConverter() {
    }

    public static String convertShapeEdge(MATRIX mat, SHAPERECORD record, int x, int y) {
        String ret = "";
        if (record instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) record;
            Point p = new Point(scr.moveDeltaX, scr.moveDeltaY);
            p = mat.apply(p);
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
            p = mat.apply(p);
            return "| " + p.x + " " + p.y;
        }
        if (record instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) record;
            int controlX = cer.controlDeltaX + x;
            int controlY = cer.controlDeltaY + y;
            int anchorX = cer.anchorDeltaX + controlX;
            int anchorY = cer.anchorDeltaY + controlY;
            Point control = new Point(controlX, controlY);
            control = mat.apply(control);
            Point anchor = new Point(anchorX, anchorY);
            anchor = mat.apply(anchor);
            return "[ " + control.x + " " + control.y + " " + anchor.x + " " + anchor.y;
        }
        return ret;
    }

    public static String convertShapeEdges(MATRIX mat, List<SHAPERECORD> records) {
        String ret = "";
        int x = 0;
        int y = 0;
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
                + "<SolidColor color=\"" + (shapeNum == 3 ? ls.colorA.toHexRGB() : ls.color.toHexRGB())
                + (shapeNum == 3 ? " alpha=\"" + ls.colorA.getAlphaFloat() + "\"" : "")
                + "\"/>"
                + "</fill>"
                + "</SolidStroke>";
    }

    public static String convertLineStyle(HashMap<Integer, CharacterTag> characters, LINESTYLE2 ls, int shapeNum) {
        String ret = "";
        String params = "";
        if (ls.pixelHintingFlag) {
            params += " pixelHinting=\"true\"";
        }
        if ((!ls.noHScaleFlag) && (!ls.noVScaleFlag)) {
            params += " scaleMode=\"normal\"";
        } else if ((!ls.noHScaleFlag) && ls.noVScaleFlag) {
            params += " scaleMode=\"horizontal\"";
        } else if (ls.noHScaleFlag && (!ls.noVScaleFlag)) {
            params += " scaleMode=\"vertical\"";
        }

        switch (ls.startCapStyle) {  //What about endCapStyle?
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
            ret += "<SolidColor color=\"" + ls.color.toHexRGB()
                    + (ls.color.getAlphaFloat() != 1 ? " alpha=\"" + ls.color.getAlphaFloat() + "\"" : "")
                    + "\"/>";
        } else {
            ret += convertFillStyle(characters, ls.fillType, shapeNum);
        }
        ret += "</fill>";
        ret += "</SolidStroke>";
        return ret;
    }

    private static float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    public static String convertFillStyle(HashMap<Integer, CharacterTag> characters, FILLSTYLE fs, int shapeNum) {
        String ret = "";
        //ret += "<FillStyle index=\"" + index + "\">";
        switch (fs.fillStyleType) {
            case SOLID:
                ret += "<SolidColor color=\"";
                if (shapeNum >= 3) {
                    ret += fs.colorA.toHexRGB();
                } else {
                    ret += fs.color.toHexRGB();
                }
                ret += "\"";
                if (shapeNum >= 3) {
                    ret += " alpha=\"" + fs.colorA.getAlphaFloat() + "\"";
                }
                ret += " />";
                break;
            case REPEATING_BITMAP:
            case CLIPPED_BITMAP:
            case NON_SMOOTHED_REPEATING_BITMAP:
            case NON_SMOOTHED_CLIPPED_BITMAP:
                ret += "<BitmapFill";
                ret += " bitmapPath=\"";
                CharacterTag bitmapCh = characters.get(fs.bitmapId);
                if (bitmapCh instanceof ImageTag) {
                    ImageTag it = (ImageTag) bitmapCh;
                    ret += "bitmap" + bitmapCh.getCharacterID() + "." + it.getImageFormat();
                }
                ret += "\"";

                if ((fs.fillStyleType == CLIPPED_BITMAP) || (fs.fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
                    ret += " bitmapIsClipped=\"true\"";
                }

                ret += ">";
                ret += "<matrix>" + convertMatrix(fs.bitmapMatrix) + "</matrix>";
                ret += "</BitmapFill>";
                break;
            case LINEAR_GRADIENT:
            case RADIAL_GRADIENT:
            case FOCAL_RADIAL_GRADIENT:

                if (fs.fillStyleType == LINEAR_GRADIENT) {
                    ret += "<LinearGradient";
                } else {
                    ret += "<RadialGradient";
                    ret += " focalPointRatio=\"";
                    if (fs.fillStyleType == FOCAL_RADIAL_GRADIENT) {
                        ret += fs.focalGradient.focalPoint;
                    } else {
                        ret += "0";
                    }
                    ret += "\"";
                }

                int interpolationMode;
                if (fs.fillStyleType == FOCAL_RADIAL_GRADIENT) {
                    interpolationMode = fs.focalGradient.interPolationMode;
                } else {
                    interpolationMode = fs.gradient.interPolationMode;
                }
                int spreadMode;
                if (fs.fillStyleType == FOCAL_RADIAL_GRADIENT) {
                    spreadMode = fs.focalGradient.spreadMode;
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
                GRADRECORD records[];
                if (fs.fillStyleType == FOCAL_RADIAL_GRADIENT) {
                    records = fs.focalGradient.gradientRecords;
                } else {
                    records = fs.gradient.gradientRecords;
                }
                for (GRADRECORD rec : records) {
                    ret += "<GradientEntry";
                    ret += " color=\"" + (shapeNum == 3 ? rec.colorA.toHexRGB() : rec.color.toHexRGB()) + "\"";
                    if (shapeNum == 3) {
                        ret += " alpha=\"" + rec.colorA.getAlphaFloat() + "\"";
                    }
                    ret += " ratio=\"" + rec.getRatioFloat() + "\"";
                    ret += " />";
                }
                if (fs.fillStyleType == LINEAR_GRADIENT) {
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
        ret += "<Matrix ";
        ret += "tx=\"" + (((float) m.translateX) / 20.0) + "\" ";
        ret += "ty=\"" + (((float) m.translateY) / 20.0) + "\" ";
        if (m.hasScale) {
            ret += "a=\"" + m.toFloat(m.scaleX) + "\" ";
            ret += "d=\"" + m.toFloat(m.scaleY) + "\" ";
        }
        if (m.hasRotate) {
            ret += "b=\"" + m.toFloat(m.rotateSkew0) + "\" ";
            ret += "c=\"" + m.toFloat(m.rotateSkew1) + "\" ";
        }
        ret += "/>";
        return ret;
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, SHAPE shape) {
        return convertShape(characters, mat, shapeNum, shape.shapeRecords);
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, SHAPEWITHSTYLE shape) {
        return convertShape(characters, mat, shapeNum, shape.shapeRecords, shape.fillStyles, shape.lineStyles);
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, ShapeTag shape) {
        return convertShape(characters, mat, shape.getShapeNum(), shape.getShapes());
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords) {
        return convertShape(characters, mat, shapeNum, shapeRecords, null, null);
    }

    public static String convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles) {
        String ret = "";
        if (mat == null) {
            mat = new MATRIX();
        }
        List<SHAPERECORD> edges = new ArrayList<SHAPERECORD>();
        int fillStyleCount = 0;
        int lineStyleCount = 0;
        int lastFillStyleCount = 0;
        int lastLineStyleCount = 0;
        String edgeStyle = "";
        int fillStyle0 = -1;
        int fillStyle1 = -1;
        int strokeStyle = -1;
        String edgesStr = "";
        String fillsStr = "";
        String strokesStr = "";
        fillsStr += "<fills>";
        strokesStr += "<strokes>";
        edgesStr += "<edges>";

        if (fillStyles != null) {
            for (FILLSTYLE fs : fillStyles.fillStyles) {
                fillsStr += "<FillStyle index=\"" + (fillStyleCount + 1) + "\">";
                fillsStr += convertFillStyle(characters, fs, shapeNum);
                fillsStr += "</FillStyle>";
                fillStyleCount++;
            }
            lastFillStyleCount = fillStyleCount;
        }
        if (lineStyles != null) {
            if (shapeNum == 4) {
                for (int l = 0; l < lineStyles.lineStyles2.length; l++) {
                    strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                    strokesStr += convertLineStyle(characters, lineStyles.lineStyles2[l], shapeNum);
                    strokesStr += "</StrokeStyle>";
                    lineStyleCount++;
                }
            } else {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                    strokesStr += convertLineStyle(lineStyles.lineStyles[l], shapeNum);
                    strokesStr += "</StrokeStyle>";
                    lineStyleCount++;
                }
            }
            lastLineStyleCount = lineStyleCount;
        }
        for (SHAPERECORD edge : shapeRecords) {
            if (edge instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) edge;
                if (scr.stateNewStyles) {
                    for (int f = 0; f < scr.fillStyles.fillStyles.length; f++) {
                        fillsStr += "<FillStyle index=\"" + (fillStyleCount + 1) + "\">";
                        fillsStr += convertFillStyle(characters, scr.fillStyles.fillStyles[f], shapeNum);
                        fillsStr += "</FillStyle>";
                        fillStyleCount++;
                    }
                    if (shapeNum == 4) {
                        for (int l = 0; l < scr.lineStyles.lineStyles2.length; l++) {
                            strokesStr += "<StrokeStyle index=\"" + (lineStyleCount + 1) + "\">";
                            strokesStr += convertLineStyle(characters, scr.lineStyles.lineStyles2[l], shapeNum);
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
                    lastFillStyleCount = scr.fillStyles.fillStyles.length;
                    lastLineStyleCount = (shapeNum == 4) ? scr.lineStyles.lineStyles2.length : scr.lineStyles.lineStyles.length;
                }
                if (scr.stateFillStyle0) {
                    /*edgeStyle += " fillStyle0=\"";
                     edgeStyle += fillStyleCount - lastFillStyleCount + scr.fillStyle0;
                     edgeStyle += "\"";*/
                    fillStyle0 = fillStyleCount - lastFillStyleCount + scr.fillStyle0;
                }
                if (scr.stateFillStyle1) {
                    /*edgeStyle += " fillStyle1=\"";
                     edgeStyle += fillStyleCount - lastFillStyleCount + scr.fillStyle1;
                     edgeStyle += "\"";*/
                    fillStyle1 = fillStyleCount - lastFillStyleCount + scr.fillStyle1;
                }
                if (scr.stateLineStyle) {
                    /*edgeStyle += " strokeStyle=\"";
                     edgeStyle += lineStyleCount - lastLineStyleCount + scr.lineStyle;
                     edgeStyle += "\"";*/
                    strokeStyle = lineStyleCount - lastLineStyleCount + scr.lineStyle;
                }
                if (!edges.isEmpty()) {
                    edgesStr += "<Edge";
                    if (fillStyle0 > -1) {
                        edgesStr += " fillStyle0=\"" + fillStyle0 + "\"";
                    }
                    if (fillStyle1 > -1) {
                        edgesStr += " fillStyle1=\"" + fillStyle1 + "\"";
                    }
                    if (strokeStyle > -1) {
                        edgesStr += " strokeStyle=\"" + strokeStyle + "\"";
                    }
                    edgesStr += " edges=\"" + convertShapeEdges(mat, edges) + "\" />";
                    edgeStyle = "";
                    strokeStyle = -1;
                    fillStyle0 = -1;
                    fillStyle1 = -1;
                }
                edges.clear();
            }
            edges.add(edge);
        }
        if (!edges.isEmpty()) {
            edgesStr += "<Edge";
            if (fillStyle0 > -1) {
                edgesStr += " fillStyle0=\"" + fillStyle0 + "\"";
            }
            if (fillStyle1 > -1) {
                edgesStr += " fillStyle1=\"" + fillStyle1 + "\"";
            }
            if (strokeStyle > -1) {
                edgesStr += " strokeStyle=\"" + strokeStyle + "\"";
            }
            edgesStr += " edges=\"" + convertShapeEdges(mat, edges) + "\" />";
            edgeStyle = "";
        }
        edges.clear();
        fillsStr += "</fills>";
        strokesStr += "</strokes>";
        edgesStr += "</edges>";

        ret += "<DOMShape>";
        ret += fillsStr;
        ret += strokesStr;
        ret += edgesStr;
        ret += "</DOMShape>";
        return ret;
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

    private static List<Integer> getOneInstanceShapes(List<Tag> tags, HashMap<Integer, CharacterTag> characters) {
        HashMap<Integer, Integer> usages = new HashMap<Integer, Integer>();
        for (Tag t : tags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int ch = po.getCharacterId();
                if (ch > -1) {
                    if (!usages.containsKey(ch)) {
                        usages.put(ch, 0);
                    }
                    int usageCount = usages.get(ch);
                    if (po.getName() != null) {
                        usageCount++;
                    }
                    if (po.getColorTransform() != null) {
                        usageCount++;
                    }
                    if (po.getColorTransformWithAlpha() != null) {
                        usageCount++;
                    }
                    if (po.cacheAsBitmap()) {
                        usageCount++;
                    }
                    usages.put(ch, usageCount + 1);
                }
            }
        }
        List<Integer> ret = new ArrayList<Integer>();
        for (int ch : usages.keySet()) {
            if (usages.get(ch) < 2) {
                ret.add(ch);
            }
        }
        return ret;
    }

    private static HashMap<Integer, CharacterTag> getCharacters(List<Tag> tags) {
        HashMap<Integer, CharacterTag> ret = new HashMap<Integer, CharacterTag>();
        for (Tag t : tags) {
            if (t instanceof CharacterTag) {
                CharacterTag ct = (CharacterTag) t;
                ret.put(ct.getCharacterID(), ct);
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
            ret += " strength=\"" + doubleToString(dsf.strength) + "\"";
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
            ret += " strength=\"" + doubleToString(gf.strength) + "\"";
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
            ret += " strength=\"" + doubleToString(bf.strength) + "\"";
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
            ret += " strength=\"" + doubleToString(ggf.strength) + "\"";
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
            ret += " strength=\"" + doubleToString(gbf.strength) + "\"";
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

    public static String convertSymbolInstance(String name, MATRIX matrix, CXFORM colorTransform, CXFORMWITHALPHA colorTransformAlpha, boolean cacheAsBitmap, int blendMode, List<FILTER> filters, boolean isVisible, RGBA backgroundColor, CharacterTag tag, HashMap<Integer, CharacterTag> characters, List<Tag> tags) {
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

        ret += "<DOMSymbolInstance libraryItemName=\"" + "Symbol " + tag.getCharacterID() + "\"";
        if (name != null) {
            ret += " name=\"" + name + "\"";
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
            RECT spriteRect = sprite.getRect(characters);
            double centerPoint3DX = twipToPixel(matrix.translateX + spriteRect.getWidth() / 2);
            double centerPoint3DY = twipToPixel(matrix.translateY + spriteRect.getHeight() / 2);
            ret += " centerPoint3DX=\"" + centerPoint3DX + "\" centerPoint3DY=\"" + centerPoint3DY + "\"";
        } else if (tag instanceof ButtonTag) {
            ret += " symbolType=\"button\"";
        }
        if (cacheAsBitmap) {
            ret += " cacheAsBitmap=\"true\"";
        }
        if (!isVisible) {
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
            if (colorTransform.hasMultTerms) {
                ret += " redMultiplier=\"" + (((float) colorTransform.redMultTerm) / 256.0f) + "\"";
                ret += " greenMultiplier=\"" + (((float) colorTransform.greenMultTerm) / 256.0f) + "\"";
                ret += " blueMultiplier=\"" + (((float) colorTransform.blueMultTerm) / 256.0f) + "\"";
            }
            if (colorTransform.hasAddTerms) {
                ret += " redOffset=\"" + colorTransform.redAddTerm + "\"";
                ret += " greenOffset=\"" + colorTransform.greenAddTerm + "\"";
                ret += " blueOffset=\"" + colorTransform.blueAddTerm + "\"";
            }
            ret += "/></color>";
        } else if (colorTransformAlpha != null) {
            ret += "<color><Color";
            if (colorTransformAlpha.hasMultTerms) {
                ret += " alphaMultiplier=\"" + (((float) colorTransformAlpha.alphaMultTerm) / 256.0f) + "\"";
                ret += " redMultiplier=\"" + (((float) colorTransformAlpha.redMultTerm) / 256.0f) + "\"";
                ret += " greenMultiplier=\"" + (((float) colorTransformAlpha.greenMultTerm) / 256.0f) + "\"";
                ret += " blueMultiplier=\"" + (((float) colorTransformAlpha.blueMultTerm) / 256.0f) + "\"";
            }
            if (colorTransformAlpha.hasAddTerms) {
                ret += " alphaOffset=\"" + colorTransformAlpha.alphaAddTerm + "\"";
                ret += " redOffset=\"" + colorTransformAlpha.redAddTerm + "\"";
                ret += " greenOffset=\"" + colorTransformAlpha.greenAddTerm + "\"";
                ret += " blueOffset=\"" + colorTransformAlpha.blueAddTerm + "\"";
            }
            ret += "/></color>";
        }
        if (!filters.isEmpty()) {
            ret += "<filters>";
            for (FILTER f : filters) {
                ret += convertFilter(f);
            }
            ret += "</filters>";
        }
        if (tag instanceof DefineButtonTag) {
            ret += "<ActionScript><script><![CDATA[";
            ret += "on(press){\r\n";
            ret += convertActionScript(((DefineButtonTag) tag));
            ret += "}";
            ret += "]]></script></ActionScript>";
        }
        if (tag instanceof DefineButton2Tag) {
            DefineButton2Tag db2 = (DefineButton2Tag) tag;
            if (!db2.actions.isEmpty()) {
                ret += "<ActionScript><script><![CDATA[";
                for (BUTTONCONDACTION bca : db2.actions) {
                    List<String> events = new ArrayList<String>();
                    if (bca.condOverUpToOverDown) {
                        events.add("press");
                    }
                    if (bca.condOverDownToOverUp) {
                        events.add("release");
                    }
                    if (bca.condOutDownToIdle) {
                        events.add("releaseOutside");
                    }
                    if (bca.condIdleToOverUp) {
                        events.add("rollOver");
                    }
                    if (bca.condOverUpToIddle) {
                        events.add("rollOut");
                    }
                    if (bca.condOverDownToOutDown) {
                        events.add("dragOut");
                    }
                    if (bca.condOutDownToOverDown) {
                        events.add("dragOver");
                    }
                    if (bca.condKeyPress > 0) {
                        String keyNames[] = {
                            null,
                            "<Left>",
                            "<Right>",
                            "<Home>",
                            "<End>",
                            "<Insert>",
                            "<Delete>",
                            "<Backspace>",
                            "<Enter>",
                            "<Up>",
                            "<Down>",
                            "<PgUp>",
                            "<PgDn>",
                            "<Tab>",
                            "<Escape>",
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "<Space>"
                        };
                        if ((bca.condKeyPress < keyNames.length) && (bca.condKeyPress > 0) && (keyNames[bca.condKeyPress] != null)) {
                            events.add("keyPress \"" + keyNames[bca.condKeyPress] + "\"");
                        } else {
                            events.add("keyPress \"" + (char) bca.condKeyPress + "\"");
                        }
                    }
                    String onStr = "";
                    for (int i = 0; i < events.size(); i++) {
                        if (i > 0) {
                            onStr += ", ";
                        }
                        onStr += events.get(i);
                    }
                    ret += "on(" + onStr + "){\r\n";
                    ret += convertActionScript(bca);
                    ret += "}";
                }
                ret += "]]></script></ActionScript>";
            }
        }
        ret += "</DOMSymbolInstance>";
        return ret;
    }

    private static String convertActionScript(ASMSource as) {
        String decompiledASHilighted = com.jpexs.decompiler.flash.action.Action.actionsToSource(as.getActions(SWF.DEFAULT_VERSION), SWF.DEFAULT_VERSION);
        return Highlighting.stripHilights(decompiledASHilighted);
    }

    private static long getTimestamp() {
        return new Date().getTime() / 1000;
    }

    public static String convertLibrary(Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, List<Integer> oneInstanceShapes, String backgroundColor, List<Tag> tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files) {

        //TODO: Imported assets
        //linkageImportForRS="true" linkageIdentifier="xxx" linkageURL="yyy.swf"
        String ret = "";
        List<String> media = new ArrayList<String>();
        List<String> symbols = new ArrayList<String>();
        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if ((symbol instanceof ShapeTag) && oneInstanceShapes.contains(symbol.getCharacterID())) {
                continue; //shapes with 1 ocurrence are not added to library
            }
            if ((symbol instanceof ShapeTag) || (symbol instanceof DefineSpriteTag) || (symbol instanceof ButtonTag)) {
                String symbolStr = "";

                symbolStr += "<DOMSymbolItem xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" name=\"Symbol " + symbol.getCharacterID() + "\" lastModified=\"" + getTimestamp() + "\""; //TODO:itemID
                if (symbol instanceof ShapeTag) {
                    symbolStr += " symbolType=\"graphic\"";
                } else if (symbol instanceof ButtonTag) {
                    symbolStr += " symbolType=\"button\"";
                    if (((ButtonTag) symbol).trackAsMenu()) {
                        symbolStr += "  trackAsMenu=\"true\"";
                    }
                }
                if (characterClasses.containsKey(symbol.getCharacterID())) {
                    symbolStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterID()) + "\"";
                }
                if (characterVariables.containsKey(symbol.getCharacterID())) {
                    symbolStr += " linkageExportForAS=\"true\" linkageIdentifier=\"" + characterVariables.get(symbol.getCharacterID()) + "\"";
                }
                symbolStr += ">";
                symbolStr += "<timeline>";
                String itemIcon = null;
                if (symbol instanceof ButtonTag) {
                    itemIcon = "0";
                    symbolStr += "<DOMTimeline name=\"Symbol " + symbol.getCharacterID() + "\" currentFrame=\"0\">";
                    symbolStr += "<layers>";
                    symbolStr += "<DOMLayer name=\"Layer 1\" current=\"true\" isSelected=\"true\">"; //color=\"#4FFF4F\"
                    symbolStr += "<frames>";
                    ButtonTag button = (ButtonTag) symbol;
                    List<BUTTONRECORD> records = button.getRecords();
                    String frames[] = {"", "", "", ""};
                    int lastFrame = 0;
                    int frame = 0;
                    for (BUTTONRECORD rec : records) {
                        CXFORMWITHALPHA colorTransformAlpha = null;
                        int blendMode = 0;
                        List<FILTER> filters = new ArrayList<FILTER>();
                        if (button instanceof DefineButton2Tag) {
                            colorTransformAlpha = rec.colorTransform;
                            if (rec.buttonHasBlendMode) {
                                blendMode = rec.blendMode;
                            }
                            if (rec.buttonHasFilterList) {
                                filters = rec.filterList;
                            }
                        }
                        String recCharStr = convertSymbolInstance(null, null, null, colorTransformAlpha, false, blendMode, filters, true, null, characters.get(rec.characterId), characters, tags);
                        if (rec.buttonStateUp) {
                            frame = 1;
                        }
                        if (rec.buttonStateOver) {
                            frame = 2;
                        }
                        if (rec.buttonStateDown) {
                            frame = 3;
                        }
                        if (rec.buttonStateHitTest) {
                            frame = 4;
                        }
                        int duration = frame - lastFrame;
                        lastFrame = frame;
                        if (duration > 0) {
                            symbolStr += "<DOMFrame index=\"";
                            symbolStr += (frame - 1);
                            symbolStr += "\"";
                            if (duration > 1) {
                                symbolStr += " duration=\"" + duration + "\"";
                            }
                            symbolStr += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                            symbolStr += "<elements>";
                            symbolStr += recCharStr;
                            symbolStr += "</elements>";
                            symbolStr += "</DOMFrame>";
                        }
                    }

                    symbolStr += "</frames>";
                    symbolStr += "</DOMLayer>";
                    symbolStr += "</layers>";
                    symbolStr += "</DOMTimeline>";
                } else if (symbol instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) symbol;
                    String initActionScript = "";
                    for (Tag t : tags) {
                        if (t instanceof DoInitActionTag) {
                            DoInitActionTag dia = (DoInitActionTag) t;
                            if (dia.spriteId == sprite.spriteId) {
                                initActionScript += convertActionScript(dia);
                            }
                        }
                    }
                    if (!initActionScript.equals("")) {
                        initActionScript = "#initclip\r\n" + initActionScript + "#endinitclip";
                    }

                    symbolStr += convertTimeline(initActionScript, oneInstanceShapes, backgroundColor, sprite.getSubTags(), characters, "Symbol " + symbol.getCharacterID());
                } else if (symbol instanceof ShapeTag) {
                    itemIcon = "1";
                    ShapeTag shape = (ShapeTag) symbol;
                    symbolStr += "<DOMTimeline name=\"Symbol " + symbol.getCharacterID() + "\" currentFrame=\"0\">";
                    symbolStr += "<layers>";
                    symbolStr += "<DOMLayer name=\"Layer 1\" current=\"true\" isSelected=\"true\">"; //color=\"#4FFF4F\"
                    symbolStr += "<frames>";
                    symbolStr += "<DOMFrame index=\"0\" keyMode=\"" + KEY_MODE_NORMAL + "\">";
                    symbolStr += "<elements>";
                    symbolStr += convertShape(characters, null, shape);
                    symbolStr += "</elements>";
                    symbolStr += "</DOMFrame>";
                    symbolStr += "</frames>";
                    symbolStr += "</DOMLayer>";
                    symbolStr += "</layers>";
                    symbolStr += "</DOMTimeline>";
                }
                symbolStr += "</timeline>";
                symbolStr += "</DOMSymbolItem>";
                symbolStr = prettyFormatXML(symbolStr);
                String symbolFile = "Symbol " + symbol.getCharacterID() + ".xml";
                try {
                    files.put(symbolFile, symbolStr.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
                String symbLinkStr = "";
                symbLinkStr += "<Include href=\"" + symbolFile + "\"";
                if (itemIcon != null) {
                    symbLinkStr += " itemIcon=\"" + itemIcon + "\"";
                }
                symbLinkStr += " loadImmediate=\"false\" lastModified=\"" + getTimestamp() + "\"/>"; //TODO: itemID=\"518de416-00000341\"
                symbols.add(symbLinkStr);
            } else if (symbol instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) symbol;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedImage image = imageTag.getImage(tags);
                String format = imageTag.getImageFormat();
                try {
                    ImageIO.write(image, format.toUpperCase(), baos);
                } catch (IOException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
                }
                String symbolFile = "bitmap" + symbol.getCharacterID() + "." + imageTag.getImageFormat();
                files.put(symbolFile, baos.toByteArray());
                String mediaLinkStr = "<DOMBitmapItem name=\"" + symbolFile + "\" sourceLastImported=\"" + getTimestamp() + "\" externalFileSize=\"" + baos.toByteArray().length + "\"";
                if (format.equals("png") || format.equals("gif")) {
                    mediaLinkStr += " useImportedJPEGData=\"false\" compressionType=\"lossless\" originalCompressionType=\"lossless\"";
                } else if (format.equals("jpg")) {
                    mediaLinkStr += " isJPEG=\"true\"";
                }
                if (characterClasses.containsKey(symbol.getCharacterID())) {
                    mediaLinkStr += " linkageExportForAS=\"true\" linkageClassName=\"" + characterClasses.get(symbol.getCharacterID()) + "\"";
                }
                mediaLinkStr += " quality=\"50\" href=\"" + symbolFile + "\" bitmapDataHRef=\"M " + (media.size() + 1) + " " + getTimestamp() + ".dat\" frameRight=\"" + image.getWidth() + "\" frameBottom=\"" + image.getHeight() + "\"/>\n";
                media.add(mediaLinkStr);

            }
            //TODO: sound, video...
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
        } catch (Exception e) {
            Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Pretty print error", e);
            return input;
        }
    }

    private static String convertFrames(String initActionScript, String prevStr, String afterStr, List<Integer> oneInstanceShapes, List<Tag> tags, HashMap<Integer, CharacterTag> characters, int depth) {
        String ret = "";
        prevStr += "<frames>";
        int frame = -1;
        String elements = "";
        String lastElements = "";
        int characterId = -1;
        int duration = 1;
        String actionScript = "";
        String lastActionScript = "";
        String frameName = null;
        boolean isAnchor = false;
        for (Tag t : tags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                if (po.getDepth() == depth) {
                    int newCharId = po.getCharacterId();
                    if (newCharId == -1) {
                        newCharId = characterId;
                    }
                    characterId = newCharId;
                    if (characters.containsKey(characterId)) {
                        CharacterTag ch = characters.get(characterId);
                        if ((ch instanceof ShapeTag) && oneInstanceShapes.contains(characterId)) {
                            elements += convertShape(characters, po.getMatrix(), (ShapeTag) ch);
                        } else if (ch instanceof TextTag) {
                            elements += convertText(tags, (TextTag) ch, po.getMatrix(), po.getFilters());
                        } else {
                            elements += convertSymbolInstance(po.getName(), po.getMatrix(), po.getColorTransform(), po.getColorTransformWithAlpha(), po.cacheAsBitmap(), po.getBlendMode(), po.getFilters(), po.isVisible(), po.getBackgroundColor(), characters.get(characterId), characters, tags);
                        }
                    }
                }
            }
            if (t instanceof DoActionTag) {
                actionScript += convertActionScript((DoActionTag) t);
            }
            if (t instanceof FrameLabelTag) {
                FrameLabelTag flt = (FrameLabelTag) t;
                if (frameName != null) {
                    ret += "<DOMFrame index=\"" + (frame) + "\"";
                    if (duration > 1) {
                        ret += " duration=\"" + duration + "\"";
                    }
                    ret += " name=\"" + frameName + "\"";
                    if (isAnchor) {
                        ret += " labelType=\"anchor\" bookmark=\"true\"";
                    } else {
                        ret += " labelType=\"name\"";
                    }
                    ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                    lastActionScript = initActionScript + lastActionScript;
                    initActionScript = "";

                    if (!lastActionScript.equals("")) {
                        ret += "<ActionScript><script><![CDATA[";
                        ret += lastActionScript;
                        ret += "]]></script></ActionScript>";
                    }
                    ret += "<elements>";
                    ret += lastElements;
                    ret += "</elements>";
                    ret += "</DOMFrame>";
                    frame += duration;
                    duration = 1;
                    lastElements = elements;
                    lastActionScript = actionScript;
                    elements = "";
                    actionScript = "";
                }
                frameName = flt.getLabelName();
                isAnchor = flt.isNamedAnchor();
            }
            if (t instanceof ShowFrameTag) {
                if (!elements.equals("") || !actionScript.equals("")) {
                    if (!lastElements.equals("") || !lastActionScript.equals("")) {
                        ret += "<DOMFrame index=\"" + (frame) + "\"";
                        if (duration > 1) {
                            ret += " duration=\"" + duration + "\"";
                        }
                        if (frameName != null) {
                            ret += " name=\"" + frameName + "\"";
                            if (isAnchor) {
                                ret += " labelType=\"anchor\" bookmark=\"true\"";
                            } else {
                                ret += " labelType=\"name\"";
                            }
                            isAnchor = false;
                            frameName = null;
                        }
                        ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
                        lastActionScript = initActionScript + lastActionScript;
                        initActionScript = "";

                        if (!lastActionScript.equals("")) {
                            ret += "<ActionScript><script><![CDATA[";
                            ret += lastActionScript;
                            ret += "]]></script></ActionScript>";
                        }
                        ret += "<elements>";
                        ret += lastElements;
                        ret += "</elements>";
                        ret += "</DOMFrame>";
                    }
                    frame += duration;
                    duration = 1;
                    lastElements = elements;
                    lastActionScript = actionScript;
                    elements = "";
                    actionScript = "";
                } else {
                    duration++;
                }
            }
        }
        lastActionScript = initActionScript + lastActionScript;
        initActionScript = "";
        if (!lastElements.equals("") || !lastActionScript.equals("")) {
            if (frame < 0) {
                frame = 0;
                duration = 1;
            }
            ret += "<DOMFrame index=\"" + frame + "\"";
            if (frameName != null) {
                ret += " name=\"" + frameName + "\"";
                if (isAnchor) {
                    ret += " labelType=\"anchor\" bookmark=\"true\"";
                } else {
                    ret += " labelType=\"name\"";
                }
            }
            if (duration > 1) {
                ret += " duration=\"" + duration + "\"";
            }
            ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
            if (!lastActionScript.equals("")) {
                ret += "<ActionScript><script><![CDATA[";
                ret += lastActionScript;
                ret += "]]></script></ActionScript>";
            }
            ret += "<elements>";
            ret += lastElements;
            ret += "</elements>";
            ret += "</DOMFrame>";
            frame += duration;
            duration = 1;
            elements = "";
        }

        afterStr = "</frames>" + afterStr;
        if (!ret.equals("")) {
            ret = prevStr + ret + afterStr;
        }
        return ret;
    }

    public static String convertTimeline(String initActionScript, List<Integer> oneInstanceShapes, String backgroundColor, List<Tag> tags, HashMap<Integer, CharacterTag> characters, String name) {
        String ret = "";
        ret += "<DOMTimeline name=\"" + name + "\">";
        ret += "<layers>";
        int layerCount = getLayerCount(tags);
        Stack<Integer> parentLayers = new Stack<Integer>();
        int index = 0;
        if ((layerCount == 0) && (!initActionScript.equals(""))) {
            ret += "<DOMLayer name=\"Layer " + index + "\" color=\"" + backgroundColor + "\">";
            ret += "<frames>";
            ret += "<DOMFrame index=\"" + 0 + "\"";
            ret += " keyMode=\"" + KEY_MODE_NORMAL + "\">";
            ret += "<ActionScript><script><![CDATA[";
            ret += initActionScript;
            ret += "]]></script></ActionScript>";
            ret += "<elements>";
            ret += "</elements>";
            ret += "</DOMFrame>";
            ret += "</DOMLayer>";
        }
        for (int d = layerCount; d >= 1; d--, index++) {
            for (Tag t : tags) {
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                    if (po.getClipDepth() == d) {
                        for (int m = po.getDepth(); m < po.getClipDepth(); m++) {
                            parentLayers.push(index);
                        }

                        ret += "<DOMLayer name=\"Layer " + index + "\" color=\"" + backgroundColor + "\" ";
                        ret += " layerType=\"mask\" locked=\"true\"";
                        ret += ">";
                        ret += convertFrames(index == 0 ? initActionScript : "", "", "", oneInstanceShapes, tags, characters, po.getDepth());
                        ret += "</DOMLayer>";
                        index++;
                        break;
                    }
                }
            }


            boolean hasClipDepth = false;
            for (Tag t : tags) {
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
            layerPrev += "<DOMLayer name=\"Layer " + index + "\" color=\"" + backgroundColor + "\" ";
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
            String cf = convertFrames(index == 0 ? initActionScript : "", layerPrev, layerAfter, oneInstanceShapes, tags, characters, d);
            if (cf.equals("")) {
                index--;
            }
            ret += cf;



        }
        ret += "</layers>";
        ret += "</DOMTimeline>";
        return ret;
    }

    private static void writeFile(byte data[], String file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(data);
        } catch (IOException iex) {
            Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Error during file write", iex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
    }

    private static Map<Integer, String> getCharacterClasses(List<Tag> tags) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        for (Tag t : tags) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag sc = (SymbolClassTag) t;
                for (int i = 0; i < sc.tagIDs.length; i++) {
                    if (!ret.containsKey(sc.tagIDs[i]) && !ret.containsValue(sc.classNames[i])) {
                        ret.put(sc.tagIDs[i], sc.classNames[i]);
                    }
                }
            }
        }
        return ret;
    }

    private static Map<Integer, String> getCharacterVariables(List<Tag> tags) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
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

    public static String convertText(List<Tag> tags, TextTag tag, MATRIX matrix, List<FILTER> filters) {
        String ret = "";
        if (matrix == null) {
            matrix = new MATRIX();
        }
        CSMTextSettingsTag csmts = null;
        String filterStr = "";
        if (!filters.isEmpty()) {
            filterStr += "<filters>";
            for (FILTER f : filters) {
                filterStr += convertFilter(f);
            }
            filterStr += "</filters>";
        }

        for (Tag t : tags) {
            if (t instanceof CSMTextSettingsTag) {
                CSMTextSettingsTag c = (CSMTextSettingsTag) t;
                if (c.textID == tag.getCharacterID()) {
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
        matStr += convertMatrix(matrix);
        matStr += "</matrix>";
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            List<TEXTRECORD> textRecords = new ArrayList<TEXTRECORD>();
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
            ret += antiAlias;
            ret += " width=\"" + tag.getBounds().getWidth() / 2 + "\" height=\"" + tag.getBounds().getHeight() + "\" autoExpand=\"true\" isSelectable=\"false\">";
            ret += matStr;

            ret += "<textRuns>";
            int fontId = -1;
            FontTag font = null;
            String fontName = null;
            String psFontName = null;
            String availableFonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            int textHeight = -1;
            RGB textColor = null;
            RGBA textColorA = null;
            for (TEXTRECORD rec : textRecords) {
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
                        fontName = font.getFontName(tags);
                    }
                    psFontName = null;
                    if (fontName != null) {
                        for (String avFont : availableFonts) {
                            if (avFont.equals(fontName)) {
                                Font f = new Font(fontName, 0, 10);
                                psFontName = f.getPSName();
                            }
                        }
                    }
                }
                ret += "<DOMTextRun>";
                ret += "<characters>" + xmlString(rec.getText(tags, font)) + "</characters>";
                ret += "<textAttrs>";

                ret += "<DOMTextAttrs aliasText=\"false\" rotation=\"true\" indent=\"5\" leftMargin=\"2\" letterSpacing=\"1\" lineSpacing=\"6\" rightMargin=\"3\" size=\"" + twipToPixel(textHeight) + "\" bitmapSize=\"1040\"";
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
            ret += antiAlias;
            double width = twipToPixel(det.getBounds().getWidth());
            double height = twipToPixel(det.getBounds().getHeight());
            if (det.hasLayout) {
                width -= twipToPixel(det.rightMargin);
                width -= twipToPixel(det.leftMargin);
                //height-=det.
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
            if (!det.variableName.equals("")) {
                ret += " variableName=\"" + det.variableName + "\"";
            }
            ret += ">";
            ret += matStr;
            ret += "<textRuns>";
            if (det.hasText) {
                ret += convertHTMLText(tags, det, det.initialText);
            }
            ret += "</textRuns>";
            ret += filterStr;
            ret += "</" + tagName + ">";
        }
        return ret;
    }

    public static void convertSWF(SWF swf, String swfFileName, String outfile, boolean compressed) {
        String domDocument = "";
        String baseName = swfFileName;
        File f = new File(baseName);
        baseName = f.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf("."));
        }
        HashMap<String, byte[]> files = new HashMap<String, byte[]>();
        HashMap<Integer, CharacterTag> characters = getCharacters(swf.tags);
        List<Integer> oneInstaceShapes = getOneInstanceShapes(swf.tags, characters);
        Map<Integer, String> characterClasses = getCharacterClasses(swf.tags);
        Map<Integer, String> characterVariables = getCharacterVariables(swf.tags);

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
        String backgroundColor = "#ffffff";
        for (Tag t : swf.tags) {
            if (t instanceof SetBackgroundColorTag) {
                SetBackgroundColorTag sbc = (SetBackgroundColorTag) t;
                backgroundColor = sbc.backgroundColor.toHexRGB();
            }
        }
        domDocument += "<DOMDocument xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://ns.adobe.com/xfl/2008/\" currentTimeline=\"1\" xflVersion=\"2.2\" creatorInfo=\"" + Main.applicationName + "\" platform=\"Windows\" versionInfo=\"Saved by " + Main.applicationVerName + "\" majorVersion=\"" + Main.version + "\" buildNumber=\"\" nextSceneIdentifier=\"2\" playOptionsPlayLoop=\"false\" playOptionsPlayPages=\"false\" playOptionsPlayFrameActions=\"false\" autoSaveHasPrompted=\"true\"";
        domDocument += " backgroundColor=\"" + backgroundColor + "\"";
        domDocument += " frameRate=\"" + swf.frameRate + "\"";
        domDocument += ">";

        domDocument += convertLibrary(characterVariables, characterClasses, oneInstaceShapes, backgroundColor, swf.tags, characters, files);
        domDocument += "<timelines>";
        domDocument += convertTimeline("", oneInstaceShapes, backgroundColor, swf.tags, characters, "Scene 1");
        domDocument += "</timelines>";
        domDocument += "</DOMDocument>";
        domDocument = prettyFormatXML(domDocument);


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
                + "    <qt>0</qt>\n"
                + "    <rnwk>0</rnwk>\n"
                + "    <swc>0</swc>\n"
                + "    <flashDefaultName>1</flashDefaultName>\n"
                + "    <projectorWinDefaultName>1</projectorWinDefaultName>\n"
                + "    <projectorMacDefaultName>1</projectorMacDefaultName>\n"
                + "    <htmlDefaultName>1</htmlDefaultName>\n"
                + "    <gifDefaultName>1</gifDefaultName>\n"
                + "    <jpegDefaultName>1</jpegDefaultName>\n"
                + "    <pngDefaultName>1</pngDefaultName>\n"
                + "    <qtDefaultName>1</qtDefaultName>\n"
                + "    <rnwkDefaultName>1</rnwkDefaultName>\n"
                + "    <swcDefaultName>1</swcDefaultName>\n"
                + "    <flashFileName>" + baseName + ".swf</flashFileName>\n"
                + "    <projectorWinFileName>" + baseName + ".exe</projectorWinFileName>\n"
                + "    <projectorMacFileName>" + baseName + ".app</projectorMacFileName>\n"
                + "    <htmlFileName>" + baseName + ".html</htmlFileName>\n"
                + "    <gifFileName>" + baseName + ".gif</gifFileName>\n"
                + "    <jpegFileName>" + baseName + ".jpg</jpegFileName>\n"
                + "    <pngFileName>" + baseName + ".png</pngFileName>\n"
                + "    <qtFileName>" + baseName + ".mov</qtFileName>\n"
                + "    <rnwkFileName>" + baseName + ".smil</rnwkFileName>\n"
                + "    <swcFileName>Untitled-4.swc</swcFileName>\n"
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
                + "    <Width>" + twipToPixel(swf.displayRect.getWidth()) + "</Width>\n"
                + "    <Height>" + twipToPixel(swf.displayRect.getHeight()) + "</Height>\n"
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
                + "    <Version>15</Version>\n"
                + "    <ExternalPlayer>FlashPlayer11.2</ExternalPlayer>\n"
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
                + "    <DocumentClass>" + (characterClasses.containsKey(0) ? characterClasses.get(0) : "") + "</DocumentClass>\n"
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
                + "    <LibraryPath>\n"
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
                + "    </LibraryVersions>\n"
                + "  </PublishFlashProperties>\n"
                + "  <PublishJpegProperties enabled=\"true\">\n"
                + "    <Width>" + twipToPixel(swf.displayRect.getWidth()) + "</Width>\n"
                + "    <Height>" + twipToPixel(swf.displayRect.getHeight()) + "</Height>\n"
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
                + "    <Width>" + twipToPixel(swf.displayRect.getWidth()) + "</Width>\n"
                + "    <Height>" + twipToPixel(swf.displayRect.getHeight()) + "</Height>\n"
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
                + "    <Width>" + twipToPixel(swf.displayRect.getWidth()) + "</Width>\n"
                + "    <Height>" + twipToPixel(swf.displayRect.getHeight()) + "</Height>\n"
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
                + "  <PublishQTProperties enabled=\"true\">\n"
                + "    <Width>" + twipToPixel(swf.displayRect.getWidth()) + "</Width>\n"
                + "    <Height>" + twipToPixel(swf.displayRect.getHeight()) + "</Height>\n"
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
                + "  </PublishQTProperties>\n"
                + "</flash_profile>\n"
                + "</flash_profiles>";

        if (compressed) {
            ZipOutputStream out = null;
            try {
                out = new ZipOutputStream(new FileOutputStream(outfile));
                out.putNextEntry(new ZipEntry("DOMDocument.xml"));
                out.write(domDocument.getBytes("UTF-8"));
                out.putNextEntry(new ZipEntry("PublishSettings.xml"));
                out.write(publishSettings.getBytes("UTF-8"));
                for (String fileName : files.keySet()) {
                    out.putNextEntry(new ZipEntry("LIBRARY/" + fileName));
                    out.write(files.get(fileName));
                }
            } catch (IOException ex) {
                Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }

        } else {
            File xfl = new File(outfile);
            File outDir = xfl.getParentFile();
            outDir.mkdirs();
            try {
                writeFile(domDocument.getBytes("UTF-8"), outDir.getAbsolutePath() + File.separator + "DOMDocument.xml");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                writeFile(publishSettings.getBytes("UTF-8"), outDir.getAbsolutePath() + File.separator + "PublishSettings.xml");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
            File libraryDir = new File(outDir.getAbsolutePath() + File.separator + "LIBRARY");
            libraryDir.mkdir();
            for (String fileName : files.keySet()) {
                writeFile(files.get(fileName), libraryDir.getAbsolutePath() + File.separator + fileName);
            }

            writeFile("PROXY-CS5".getBytes(), outfile);
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
        double ctrMap[] = {
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
        float matrix[][] = new float[5][5];
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
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);

            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();


            reader.setContentHandler(tparser);
            reader.setErrorHandler(tparser);
            reader.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(html.getBytes("UTF-8")), "UTF-8")));
        } catch (Exception e) {
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
        private List<Tag> tags;
        private boolean bold = false;
        private boolean italic = false;
        private boolean underline = false;
        private boolean li = false;
        private String url = null;
        private String target = null;

        public HTMLTextParser(List<Tag> tags, DefineEditTextTag det) {
            if (det.hasLayout) {
                leftMargin = det.leftMargin;
                rightMargin = det.rightMargin;
                indent = det.indent;
                lineSpacing = det.leading;
                String alignNames[] = {"left", "right", "center", "justify"};
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
            if (qName.equals("a")) {
                String href = attributes.getValue("href");
                if (href != null) {
                    url = href;
                }
                String t = attributes.getValue("target");
                if (t != null) {
                    target = t;
                }
            } else if (qName.equals("b")) {
                bold = true;
            } else if (qName.equals("i")) {
                italic = true;
            } else if (qName.equals("u")) {
                underline = true;
            } else if (qName.equals("li")) {
                li = true;
            } else if (qName.equals("p")) {
                String a = attributes.getValue("align");
                if (a != null) {
                    alignment = a;
                }
                if (!result.equals("")) {
                    putText("\r\n");
                }
            } else if (qName.equals("font")) {
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
                    for (Tag t : tags) {
                        if (t instanceof FontTag) {
                            FontTag ft = (FontTag) t;
                            String fontName = "";
                            if (f.equals(ft.getFontName(tags))) {
                                for (Tag u : tags) {
                                    if (u instanceof DefineFontNameTag) {
                                        if (((DefineFontNameTag) u).fontId == ft.getFontId()) {
                                            fontName = ((DefineFontNameTag) u).fontName;
                                        }
                                    }
                                }
                                if (fontName == null) {
                                    fontName = ft.getFontName(tags);
                                }
                                fontFace = new Font(fontName, (italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0) | (!italic && !bold ? Font.PLAIN : 0), size < 0 ? 10 : size).getPSName();
                                break;
                            }
                        }
                    }
                }
            }
            //textformat,tab,br
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
        }
    }
}
