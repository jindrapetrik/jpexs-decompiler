/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.amf.amf3.Amf3Value;
import com.jpexs.decompiler.flash.amf.amf3.types.ObjectType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.MovieExporter;
import com.jpexs.decompiler.flash.exporters.SoundExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ScriptExportSettings;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
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
import com.jpexs.decompiler.flash.tags.base.ButtonAction;
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
import com.jpexs.decompiler.flash.timeline.Timelined;
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
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
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
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
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

    public static final String PUBLISH_DATA_PREFIX = "PUB_PRST_DATA";

    public static final String PUBLISH_DATA_FORMAT = "_EMBED_SWF_";

    private final Random random = new Random(123); // predictable random

    private static void convertShapeEdge(MATRIX mat, SHAPERECORD record, int x, int y, StringBuilder ret) {
        if (record instanceof StyleChangeRecord) {
            StyleChangeRecord scr = (StyleChangeRecord) record;
            Point p = new Point(scr.moveDeltaX, scr.moveDeltaY);
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
            ret.append("| ").append(p.x).append(" ").append(p.y);
        } else if (record instanceof CurvedEdgeRecord) {
            CurvedEdgeRecord cer = (CurvedEdgeRecord) record;
            int controlX = cer.controlDeltaX + x;
            int controlY = cer.controlDeltaY + y;
            int anchorX = cer.anchorDeltaX + controlX;
            int anchorY = cer.anchorDeltaY + controlY;
            Point control = new Point(controlX, controlY);
            Point anchor = new Point(anchorX, anchorY);
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

    private static String getScaleMode(LINESTYLE lineStyle) {
        if (lineStyle instanceof LINESTYLE2) {
            LINESTYLE2 ls2 = (LINESTYLE2) lineStyle;
            if (ls2.noHScaleFlag && ls2.noVScaleFlag) {
                return "none";
            } else if (ls2.noHScaleFlag) {
                return "vertical";
            } else if (ls2.noVScaleFlag) {
                return "horizontal";
            } else {
                return "normal";
            }
        }

        return "normal";
    }

    private static void convertLineStyle(LINESTYLE ls, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("SolidStroke", new String[]{
            "scaleMode", getScaleMode(ls),
            "weight", Double.toString(((float) ls.width) / SWF.unitDivisor),});

        writer.writeStartElement("fill");
        if (!(ls instanceof LINESTYLE2) || !((LINESTYLE2) ls).hasFillFlag) {
            writer.writeStartElement("SolidColor", new String[]{"color", ls.color.toHexRGB()});
            if (shapeNum >= 3) {
                writer.writeAttribute("alpha", ((RGBA) ls.color).getAlphaFloat());
            }

            writer.writeEndElement();
        } else {
            // todo: line fill
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void convertLineStyle(HashMap<Integer, CharacterTag> characters, LINESTYLE2 ls, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("SolidStroke", new String[]{"weight", Double.toString(((float) ls.width) / SWF.unitDivisor)});
        if (ls.pixelHintingFlag) {
            writer.writeAttribute("pixelHinting", true);
        }
        if (ls.width == 1) {
            writer.writeAttribute("solidStyle", "hairline");
        }
        writer.writeAttribute("scaleMode", getScaleMode(ls));

        switch (ls.endCapStyle) {  //What about endCapStyle?
            case LINESTYLE2.NO_CAP:
                writer.writeAttribute("caps", "none");
                break;
            case LINESTYLE2.SQUARE_CAP:
                writer.writeAttribute("caps", "square");
                break;
        }
        switch (ls.joinStyle) {
            case LINESTYLE2.BEVEL_JOIN:
                writer.writeAttribute("joints", "bevel");
                break;
            case LINESTYLE2.MITER_JOIN:
                writer.writeAttribute("joints", "miter");
                float miterLimitFactor = ls.miterLimitFactor;
                if (miterLimitFactor != 3.0f) {
                    writer.writeAttribute("miterLimit", miterLimitFactor);
                }
                break;
        }

        writer.writeStartElement("fill");

        if (!ls.hasFillFlag) {
            RGBA color = (RGBA) ls.color;
            writer.writeStartElement("SolidColor", new String[]{"color", color.toHexRGB()});
            if (color.getAlphaFloat() != 1) {
                writer.writeAttribute("alpha", Float.toString(color.getAlphaFloat()));
            }

            writer.writeEndElement();
        } else {
            convertFillStyle(null/* FIXME */, characters, ls.fillType, shapeNum, writer);
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void convertFillStyle(MATRIX mat, HashMap<Integer, CharacterTag> characters, FILLSTYLE fs, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
        /* todo: use matrix
         if (mat == null) {
         mat = new MATRIX();
         }*/
        //ret.append("<FillStyle index=\"").append(index).append("\">");
        switch (fs.fillStyleType) {
            case FILLSTYLE.SOLID:
                writer.writeStartElement("SolidColor", new String[]{"color", fs.color.toHexRGB()});
                if (shapeNum >= 3) {
                    writer.writeAttribute("alpha", ((RGBA) fs.color).getAlphaFloat());
                }

                writer.writeEndElement();
                break;
            case FILLSTYLE.REPEATING_BITMAP:
            case FILLSTYLE.CLIPPED_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP:
            case FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP:
                CharacterTag bitmapCh = characters.get(fs.bitmapId);
                if (!(bitmapCh instanceof ImageTag)) {
                    if (bitmapCh != null) {
                        logger.log(Level.SEVERE, "Suspicious bitmapfill:{0}", bitmapCh.getClass().getSimpleName());
                    }
                    writer.writeEmptyElement("SolidColor", new String[]{"color", "#ffffff"});
                    return;
                }

                ImageTag it = (ImageTag) bitmapCh;
                writer.writeStartElement("BitmapFill");
                writer.writeAttribute("bitmapPath", "bitmap" + bitmapCh.getCharacterId() + it.getImageFormat().getExtension());

                if ((fs.fillStyleType == FILLSTYLE.CLIPPED_BITMAP) || (fs.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
                    writer.writeAttribute("bitmapIsClipped", true);
                }

                writer.writeStartElement("matrix");
                convertMatrix(fs.bitmapMatrix, writer);
                writer.writeEndElement();
                writer.writeEndElement();
                break;
            case FILLSTYLE.LINEAR_GRADIENT:
            case FILLSTYLE.RADIAL_GRADIENT:
            case FILLSTYLE.FOCAL_RADIAL_GRADIENT:

                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    writer.writeStartElement("LinearGradient");
                } else {
                    writer.writeStartElement("RadialGradient");
                    String focalPointRatioStr;
                    if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                        focalPointRatioStr = Float.toString(((FOCALGRADIENT) fs.gradient).focalPoint);
                    } else {
                        focalPointRatioStr = "0";
                    }

                    writer.writeAttribute("focalPointRatio", focalPointRatioStr);
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
                    writer.writeAttribute("interpolationMethod", "linearRGB");
                }
                switch (spreadMode) {
                    case GRADIENT.SPREAD_PAD_MODE:

                        break;
                    case GRADIENT.SPREAD_REFLECT_MODE:
                        writer.writeAttribute("spreadMethod", "reflect");
                        break;
                    case GRADIENT.SPREAD_REPEAT_MODE:
                        writer.writeAttribute("spreadMethod", "repeat");
                        break;
                }

                writer.writeStartElement("matrix");
                convertMatrix(fs.gradientMatrix, writer);
                writer.writeEndElement();
                GRADRECORD[] records;
                if (fs.fillStyleType == FILLSTYLE.FOCAL_RADIAL_GRADIENT) {
                    records = fs.gradient.gradientRecords;
                } else {
                    records = fs.gradient.gradientRecords;
                }
                for (GRADRECORD rec : records) {
                    writer.writeStartElement("GradientEntry");
                    writer.writeAttribute("color", rec.color.toHexRGB());
                    if (shapeNum >= 3) {
                        writer.writeAttribute("alpha", ((RGBA) rec.color).getAlphaFloat());
                    }
                    writer.writeAttribute("ratio", rec.getRatioFloat());
                    writer.writeEndElement();
                }
                if (fs.fillStyleType == FILLSTYLE.LINEAR_GRADIENT) {
                    writer.writeEndElement(); // LinearGradient
                } else {
                    writer.writeEndElement(); //RadialGradient
                }
                break;
        }
        //ret.append("</FillStyle>");
    }

    private static void convertMatrix(MATRIX matrix, XFLXmlWriter writer) throws XMLStreamException {
        Matrix m = new Matrix(matrix);
        writer.writeStartElement("Matrix");
        writer.writeAttribute("tx", ((float) m.translateX) / SWF.unitDivisor);
        writer.writeAttribute("ty", ((float) m.translateY) / SWF.unitDivisor);
        if (m.scaleX != 1.0 || m.scaleY != 1.0) {
            writer.writeAttribute("a", m.scaleX);
            writer.writeAttribute("d", m.scaleY);
        }
        if (m.rotateSkew0 != 0.0 || m.rotateSkew1 != 0.0) {
            writer.writeAttribute("b", m.rotateSkew0);
            writer.writeAttribute("c", m.rotateSkew1);
        }
        writer.writeEndElement();
    }

    private static boolean shapeHasMultiLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles) throws XMLStreamException {
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, false);
        return layers.size() > 1;
    }

    private static void convertShape(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape, boolean useLayers, XFLXmlWriter writer) throws XMLStreamException {
        List<String> layers = getShapeLayers(characters, mat, shapeNum, shapeRecords, fillStyles, lineStyles, morphshape);
        if (!useLayers) {
            for (int l = layers.size() - 1; l >= 0; l--) {
                writer.writeCharactersRaw(layers.get(l));
            }
        } else {
            int layer = 1;
            for (int l = layers.size() - 1; l >= 0; l--) {
                writer.writeStartElement("DOMLayer", new String[]{"name", "Layer " + layer++}); //color="#4FFF4F"
                writer.writeStartElement("frames");
                writer.writeStartElement("DOMFrame", new String[]{"index", "0", "motionTweenScale", "false", "keyMode", Integer.toString(KEY_MODE_SHAPE_LAYERS)});
                writer.writeStartElement("elements");
                writer.writeCharactersRaw(layers.get(l));
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
    }

    private static int snapToGrid(int v, int gridSize) {
        double divisor = (double) gridSize;
        int ret = (int) (Math.round(v / divisor) * divisor);
        return ret;
    }

    //just some testing methods to smooth shapes more, but without success (issue #1257)
    private static List<SHAPERECORD> snapShapeToGrid(List<SHAPERECORD> shapeRecords, int gridSize) {
        List<SHAPERECORD> ret = new ArrayList<>(shapeRecords.size());
        int hintedX = 0;
        int hintedY = 0;
        int correctX = 0;
        int correctY = 0;
        int lastCorrectX;
        int lastCorrectY;
        for (SHAPERECORD rec : shapeRecords) {
            SHAPERECORD ch = rec.clone();
            lastCorrectX = correctX;
            lastCorrectY = correctY;
            correctX = ch.changeX(correctX);
            correctY = ch.changeY(correctY);

            if (ch instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) ch;
                if (scr.stateMoveTo) {
                    int shouldBeX = snapToGrid(correctX, gridSize);
                    int shouldBeY = snapToGrid(correctY, gridSize);

                    scr.moveDeltaX = shouldBeX;
                    scr.moveDeltaY = shouldBeY;

                    hintedX = shouldBeX;
                    hintedY = shouldBeY;
                }
            } else if (ch instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) ch;
                if (ser.generalLineFlag || !ser.vertLineFlag) { //has x
                    int shouldBeX = snapToGrid(correctX, gridSize);
                    ser.deltaX = shouldBeX - hintedX;
                    hintedX = shouldBeX;
                }
                if (ser.generalLineFlag || ser.vertLineFlag) { //has y
                    int shouldBeY = snapToGrid(correctY, gridSize);
                    ser.deltaY = shouldBeY - hintedY;
                    hintedY = shouldBeY;
                }
            } else if (ch instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) ch;
                int controlShouldBeX = snapToGrid(lastCorrectX + cer.controlDeltaX, gridSize);
                int controlShouldBeY = snapToGrid(lastCorrectY + cer.controlDeltaY, gridSize);

                cer.controlDeltaX = controlShouldBeX - hintedX;
                cer.controlDeltaY = controlShouldBeY - hintedY;

                int anchorShouldBeX = snapToGrid(correctX, gridSize);
                int anchorShouldBeY = snapToGrid(correctY, gridSize);

                cer.anchorDeltaX = anchorShouldBeX - (hintedX + cer.controlDeltaX);
                cer.anchorDeltaY = anchorShouldBeY - (hintedY + cer.controlDeltaY);
                hintedX = anchorShouldBeX;
                hintedY = anchorShouldBeY;
            }
            ret.add(ch);
        }
        return ret;
    }

    private static double distance(Point p1, Point p2) {
        double dx = (p1.x - p2.x);
        double dy = (p1.y - p2.y);
        return Math.sqrt(dx * dx + dy * dy);
    }

    //just some testing methods to smooth shapes more, but without success (issue #1257)
    private static List<SHAPERECORD> snapCloseTogether(List<SHAPERECORD> shapeRecords, double maxDistance) {
        List<Point> points = new ArrayList<>();

        int x = 0;
        int y = 0;
        Point prevPoint = null;
        Point startPoint = null;
        for (SHAPERECORD rec : shapeRecords) {
            x = rec.changeX(x);
            y = rec.changeY(y);

            Point currentPoint = new Point(x, y);

            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo) {
                    if (prevPoint != null && startPoint != null) {
                        if (distance(prevPoint, startPoint) <= maxDistance) { //start and end of the path near => close
                            prevPoint.x = startPoint.x;
                            prevPoint.y = startPoint.y;
                            System.err.println("CLOSED");
                        }
                    }
                    startPoint = currentPoint;
                }
            }

            /*for (Point p : points) {
                if (distance(p, currentPoint) <= maxDistance) {
                    currentPoint = (Point) p.clone();
                    break;
                }
            }*/
            points.add(currentPoint);
            prevPoint = currentPoint;
        }

        List<SHAPERECORD> ret = new ArrayList<>(shapeRecords.size());
        int hintedX = 0;
        int hintedY = 0;
        int correctX = 0;
        int correctY = 0;
        int lastCorrectX = 0;
        int lastCorrectY = 0;
        int index = 0;
        for (SHAPERECORD rec : shapeRecords) {
            SHAPERECORD ch = rec.clone();
            lastCorrectX = correctX;
            lastCorrectY = correctY;
            correctX = ch.changeX(correctX);
            correctY = ch.changeY(correctY);

            if (ch instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) ch;
                if (scr.stateMoveTo) {
                    int shouldBeX = points.get(index).x;
                    int shouldBeY = points.get(index).y;

                    scr.moveDeltaX = shouldBeX;
                    scr.moveDeltaY = shouldBeY;

                    hintedX = shouldBeX;
                    hintedY = shouldBeY;
                }
            } else if (ch instanceof StraightEdgeRecord) {
                StraightEdgeRecord ser = (StraightEdgeRecord) ch;
                if (ser.generalLineFlag || !ser.vertLineFlag) { //has x
                    int shouldBeX = points.get(index).x;
                    ser.deltaX = shouldBeX - hintedX;
                    hintedX = shouldBeX;
                }
                if (ser.generalLineFlag || ser.vertLineFlag) { //has y
                    int shouldBeY = points.get(index).y;
                    ser.deltaY = shouldBeY - hintedY;
                    hintedY = shouldBeY;
                }
            } else if (ch instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) ch;
                //int controlShouldBeX = snapToGrid(lastCorrectX + cer.controlDeltaX, gridSize);
                //int controlShouldBeY = snapToGrid(lastCorrectY + cer.controlDeltaY, gridSize);

                //cer.controlDeltaX = controlShouldBeX - hintedX;
                //cer.controlDeltaY = controlShouldBeY - hintedY;
                int anchorShouldBeX = points.get(index).x;
                int anchorShouldBeY = points.get(index).y;

                cer.anchorDeltaX = anchorShouldBeX - (hintedX + cer.controlDeltaX);
                cer.anchorDeltaY = anchorShouldBeY - (hintedY + cer.controlDeltaY);
                hintedX = anchorShouldBeX;
                hintedY = anchorShouldBeY;
            }
            ret.add(ch);
            index++;
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

    private static List<String> getShapeLayers(HashMap<Integer, CharacterTag> characters, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape) throws XMLStreamException {
        if (mat == null) {
            mat = new MATRIX();
        }
        //TODO: insert some magic methods here to fix issue #1257
        shapeRecords = smoothShape(shapeRecords);
        List<SHAPERECORD> edges = new ArrayList<>();
        int lineStyleCount = 0;
        int fillStyle0 = -1;
        int fillStyle1 = -1;
        int strokeStyle = -1;
        XFLXmlWriter fillsStr = new XFLXmlWriter();
        XFLXmlWriter strokesStr = new XFLXmlWriter();
        fillsStr.writeStartElement("fills");
        strokesStr.writeStartElement("strokes");
        List<String> layers = new ArrayList<>();

        int fillStyleCount = 0;
        if (fillStyles != null) {
            for (FILLSTYLE fs : fillStyles.fillStyles) {
                fillsStr.writeStartElement("FillStyle", new String[]{"index", Integer.toString(fillStyleCount + 1)});
                convertFillStyle(mat, characters, fs, shapeNum, fillsStr);
                fillsStr.writeEndElement();
                fillStyleCount++;
            }
        }
        if (lineStyles != null) {
            if (shapeNum <= 3 && lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                    convertLineStyle(lineStyles.lineStyles[l], shapeNum, strokesStr);
                    strokesStr.writeEndElement();
                    lineStyleCount++;
                }
            } else if (lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                    convertLineStyle(characters, (LINESTYLE2) lineStyles.lineStyles[l], shapeNum, strokesStr);
                    strokesStr.writeEndElement();
                    lineStyleCount++;
                }
            }
        }

        fillsStr.writeEndElement();
        strokesStr.writeEndElement();

        int layer = 1;

        boolean hasEdge = false;
        XFLXmlWriter currentLayer = new XFLXmlWriter();
        if (fillStyleCount > 0 || lineStyleCount > 0) {
            currentLayer.writeStartElement("DOMShape", new String[]{"isFloating", "true"});
            currentLayer.writeCharactersRaw(fillsStr.toString());
            currentLayer.writeCharactersRaw(strokesStr.toString());
            currentLayer.writeStartElement("edges");
        }

        int x = 0;
        int y = 0;
        int startEdgeX = 0;
        int startEdgeY = 0;

        LINESTYLEARRAY actualLinestyles = lineStyles;
        int strokeStyleOrig = 0;
        fillStyleCount = fillStyles == null ? 0 : fillStyles.fillStyles.length;
        for (SHAPERECORD edge : shapeRecords) {
            if (edge instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) edge;
                boolean styleChange = false;
                int lastFillStyle1 = fillStyle1;
                int lastFillStyle0 = fillStyle0;
                int lastStrokeStyle = strokeStyle;
                if (scr.stateNewStyles) {
                    XFLXmlWriter fillsNewStr = new XFLXmlWriter();
                    XFLXmlWriter strokesNewStr = new XFLXmlWriter();
                    fillsNewStr.writeStartElement("fills");
                    strokesNewStr.writeStartElement("strokes");
                    if (fillStyleCount > 0 || lineStyleCount > 0) {

                        if ((fillStyle0 > 0) || (fillStyle1 > 0) || (strokeStyle > 0)) {

                            boolean empty = false;
                            if ((fillStyle0 <= 0) && (fillStyle1 <= 0) && (strokeStyle > 0) && morphshape) {
                                if (shapeNum == 4) {
                                    if (strokeStyleOrig > 0) {
                                        if (actualLinestyles != null && !((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                            RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                            if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                                empty = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!empty) {
                                currentLayer.writeStartElement("Edge");
                                if (fillStyle0 > -1) {
                                    currentLayer.writeAttribute("fillStyle0", fillStyle0);
                                }
                                if (fillStyle1 > -1) {
                                    currentLayer.writeAttribute("fillStyle1", fillStyle1);
                                }
                                if (strokeStyle > -1) {
                                    currentLayer.writeAttribute("strokeStyle", strokeStyle);
                                }
                                StringBuilder edgesSb = new StringBuilder();
                                convertShapeEdges(startEdgeX, startEdgeY, mat, edges, edgesSb);
                                currentLayer.writeAttribute("edges", edgesSb.toString());
                                currentLayer.writeEndElement();
                                hasEdge = true;
                            }
                        }

                    }
                    if (currentLayer.length() > 0) {
                        currentLayer.writeEndElement(); // edges
                        currentLayer.writeEndElement(); // DOMShape
                    }
                    if (currentLayer.length() > 0 && hasEdge) { //no empty layers
                        layers.add(currentLayer.toString());
                    }
                    currentLayer.setLength(0);
                    hasEdge = false;
                    currentLayer.writeStartElement("DOMShape", new String[]{"isFloating", "true"});
                    //ret += convertShape(characters, null, shape);
                    for (int f = 0; f < scr.fillStyles.fillStyles.length; f++) {
                        fillsNewStr.writeStartElement("FillStyle", new String[]{"index", Integer.toString(f + 1)});
                        convertFillStyle(mat, characters, scr.fillStyles.fillStyles[f], shapeNum, fillsNewStr);
                        fillsNewStr.writeEndElement();
                        fillStyleCount++;
                    }

                    lineStyleCount = 0;
                    if (shapeNum <= 3) {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesNewStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                            convertLineStyle(scr.lineStyles.lineStyles[l], shapeNum, strokesNewStr);
                            strokesNewStr.writeEndElement();
                            lineStyleCount++;
                        }
                    } else {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesNewStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                            convertLineStyle(characters, (LINESTYLE2) scr.lineStyles.lineStyles[l], shapeNum, strokesNewStr);
                            strokesNewStr.writeEndElement();
                            lineStyleCount++;
                        }
                    }
                    fillsNewStr.writeEndElement(); // fills
                    strokesNewStr.writeEndElement(); // strokes
                    currentLayer.writeCharactersRaw(fillsNewStr.toString());
                    currentLayer.writeCharactersRaw(strokesNewStr.toString());
                    currentLayer.writeStartElement("edges");
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
                                    if (actualLinestyles != null && !((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                        RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                        if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                            empty = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (!empty) {
                            currentLayer.writeStartElement("Edge");
                            if (lastFillStyle0 > -1) {
                                currentLayer.writeAttribute("fillStyle0", lastFillStyle0);
                            }
                            if (lastFillStyle1 > -1) {
                                currentLayer.writeAttribute("fillStyle1", lastFillStyle1);
                            }
                            if (lastStrokeStyle > -1) {
                                currentLayer.writeAttribute("strokeStyle", lastStrokeStyle);
                            }
                            StringBuilder edgesSb = new StringBuilder();
                            convertShapeEdges(startEdgeX, startEdgeY, mat, edges, edgesSb);
                            currentLayer.writeAttribute("edges", edgesSb.toString());
                            currentLayer.writeEndElement();
                            hasEdge = true;
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
                            if (actualLinestyles != null && !((LINESTYLE2) actualLinestyles.lineStyles[strokeStyleOrig]).hasFillFlag) {
                                RGBA color = (RGBA) actualLinestyles.lineStyles[strokeStyleOrig].color;
                                if (color.alpha == 0 && color.red == 0 && color.green == 0 && color.blue == 0) {
                                    empty = true;
                                }
                            }
                        }
                    }
                }
                if (!empty) {
                    currentLayer.writeStartElement("Edge");
                    if (fillStyle0 > -1) {
                        currentLayer.writeAttribute("fillStyle0", fillStyle0);
                    }
                    if (fillStyle1 > -1) {
                        currentLayer.writeAttribute("fillStyle1", fillStyle1);
                    }
                    if (strokeStyle > -1) {
                        currentLayer.writeAttribute("strokeStyle", strokeStyle);
                    }
                    StringBuilder edgesSb = new StringBuilder();
                    convertShapeEdges(startEdgeX, startEdgeY, mat, edges, edgesSb);
                    currentLayer.writeAttribute("edges", edgesSb.toString());
                    currentLayer.writeEndElement();
                    hasEdge = true;
                }
            }
        }
        edges.clear();
        if (currentLayer.length() > 0) {
            currentLayer.writeEndElement(); // edges
            currentLayer.writeEndElement(); // DOMShape

            if (currentLayer.length() > 0 && hasEdge) { //no empty layers
                layers.add(currentLayer.toString());
            }
        }
        return layers;
    }

    private static int getLayerCount(ReadOnlyTagList tags) {
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

    private static void walkShapeUsages(ReadOnlyTagList timeLineTags, HashMap<Integer, CharacterTag> characters, HashMap<Integer, Integer> usages) {
        for (Tag t : timeLineTags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                walkShapeUsages(sprite.getTags(), characters, usages);
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

    private static List<Integer> getNonLibraryShapes(ReadOnlyTagList tags, HashMap<Integer, CharacterTag> characters) {
        HashMap<Integer, Integer> usages = new HashMap<>();
        walkShapeUsages(tags, characters, usages);
        List<Integer> ret = new ArrayList<>();
        try {
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
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return ret;
    }

    private static HashMap<Integer, CharacterTag> getCharacters(ReadOnlyTagList tags) {
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

    private static void convertFilter(FILTER filter, XFLXmlWriter writer) throws XMLStreamException {
        if (filter instanceof DROPSHADOWFILTER) {
            DROPSHADOWFILTER dsf = (DROPSHADOWFILTER) filter;
            writer.writeStartElement("DropShadowFilter");
            if (dsf.dropShadowColor.alpha != 255) {
                writer.writeAttribute("alpha", doubleToString(dsf.dropShadowColor.getAlphaFloat()));
            }
            writer.writeAttribute("angle", doubleToString(radToDeg(dsf.angle)));
            writer.writeAttribute("blurX", doubleToString(dsf.blurX));
            writer.writeAttribute("blurY", doubleToString(dsf.blurY));
            writer.writeAttribute("color", dsf.dropShadowColor.toHexRGB());
            writer.writeAttribute("distance", doubleToString(dsf.distance));
            if (!dsf.compositeSource) {
                writer.writeAttribute("hideObject", true);
            }
            if (dsf.innerShadow) {
                writer.writeAttribute("inner", true);
            }
            if (dsf.knockout) {
                writer.writeAttribute("knockout", true);
            }
            writer.writeAttribute("quality", dsf.passes);
            writer.writeAttribute("strength", doubleToString(dsf.strength, 2));
            writer.writeEndElement();
        } else if (filter instanceof BLURFILTER) {
            BLURFILTER bf = (BLURFILTER) filter;
            writer.writeStartElement("BlurFilter");
            writer.writeAttribute("blurX", doubleToString(bf.blurX));
            writer.writeAttribute("blurY", doubleToString(bf.blurY));
            writer.writeAttribute("quality", bf.passes);
            writer.writeEndElement();
        } else if (filter instanceof GLOWFILTER) {
            GLOWFILTER gf = (GLOWFILTER) filter;
            writer.writeStartElement("GlowFilter");
            if (gf.glowColor.alpha != 255) {
                writer.writeAttribute("alpha", gf.glowColor.getAlphaFloat());
            }
            writer.writeAttribute("blurX", doubleToString(gf.blurX));
            writer.writeAttribute("blurY", doubleToString(gf.blurY));
            writer.writeAttribute("color", gf.glowColor.toHexRGB());

            if (gf.innerGlow) {
                writer.writeAttribute("inner", true);
            }
            if (gf.knockout) {
                writer.writeAttribute("knockout", true);
            }
            writer.writeAttribute("quality", gf.passes);
            writer.writeAttribute("strength", doubleToString(gf.strength, 2));
            writer.writeEndElement();
        } else if (filter instanceof BEVELFILTER) {
            BEVELFILTER bf = (BEVELFILTER) filter;
            writer.writeStartElement("BevelFilter");
            writer.writeAttribute("blurX", doubleToString(bf.blurX));
            writer.writeAttribute("blurY", doubleToString(bf.blurY));
            writer.writeAttribute("quality", bf.passes);
            writer.writeAttribute("angle", doubleToString(radToDeg(bf.angle)));
            writer.writeAttribute("distance", bf.distance);
            if (bf.highlightColor.alpha != 255) {
                writer.writeAttribute("highlightAlpha", bf.highlightColor.getAlphaFloat());
            }
            writer.writeAttribute("highlightColor", bf.highlightColor.toHexRGB());
            if (bf.knockout) {
                writer.writeAttribute("knockout", true);
            }
            if (bf.shadowColor.alpha != 255) {
                writer.writeAttribute("shadowAlpha", bf.shadowColor.getAlphaFloat());
            }
            writer.writeAttribute("shadowColor", bf.shadowColor.toHexRGB());
            writer.writeAttribute("strength", doubleToString(bf.strength, 2));
            if (bf.onTop && !bf.innerShadow) {
                writer.writeAttribute("type", "full");
            } else if (!bf.innerShadow) {
                writer.writeAttribute("type", "outer");
            }
            writer.writeEndElement();
        } else if (filter instanceof GRADIENTGLOWFILTER) {
            GRADIENTGLOWFILTER ggf = (GRADIENTGLOWFILTER) filter;
            writer.writeStartElement("GradientGlowFilter");
            writer.writeAttribute("angle", doubleToString(radToDeg(ggf.angle)));

            writer.writeAttribute("blurX", doubleToString(ggf.blurX));
            writer.writeAttribute("blurY", doubleToString(ggf.blurY));
            writer.writeAttribute("quality", ggf.passes);
            writer.writeAttribute("distance", doubleToString(ggf.distance));
            if (ggf.knockout) {
                writer.writeAttribute("knockout", true);
            }
            writer.writeAttribute("strength", doubleToString(ggf.strength, 2));
            if (ggf.onTop && !ggf.innerShadow) {
                writer.writeAttribute("type", "full");
            } else if (!ggf.innerShadow) {
                writer.writeAttribute("type", "outer");
            }
            for (int g = 0; g < ggf.gradientColors.length; g++) {
                RGBA gc = ggf.gradientColors[g];
                writer.writeStartElement("GradientEntry", new String[]{"color", gc.toHexRGB()});
                if (gc.alpha != 255) {
                    writer.writeAttribute("alpha", gc.getAlphaFloat());
                }
                writer.writeAttribute("ratio", doubleToString(((float) ggf.gradientRatio[g]) / 255.0));
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else if (filter instanceof GRADIENTBEVELFILTER) {
            GRADIENTBEVELFILTER gbf = (GRADIENTBEVELFILTER) filter;
            writer.writeStartElement("GradientBevelFilter");
            writer.writeAttribute("angle", doubleToString(radToDeg(gbf.angle)));

            writer.writeAttribute("blurX", doubleToString(gbf.blurX));
            writer.writeAttribute("blurY", doubleToString(gbf.blurY));
            writer.writeAttribute("quality", gbf.passes);
            writer.writeAttribute("distance", doubleToString(gbf.distance));
            if (gbf.knockout) {
                writer.writeAttribute("knockout", true);
            }
            writer.writeAttribute("strength", doubleToString(gbf.strength, 2));
            if (gbf.onTop && !gbf.innerShadow) {
                writer.writeAttribute("type", "full");
            } else if (!gbf.innerShadow) {
                writer.writeAttribute("type", "outer");
            }
            for (int g = 0; g < gbf.gradientColors.length; g++) {
                RGBA gc = gbf.gradientColors[g];
                writer.writeStartElement("GradientEntry", new String[]{"color", gc.toHexRGB()});
                if (gc.alpha != 255) {
                    writer.writeAttribute("alpha", gc.getAlphaFloat());
                }
                writer.writeAttribute("ratio", doubleToString(((float) gbf.gradientRatio[g]) / 255.0));
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else if (filter instanceof COLORMATRIXFILTER) {
            COLORMATRIXFILTER cmf = (COLORMATRIXFILTER) filter;
            convertAdjustColorFilter(cmf, writer);
        }
    }

    private static void convertSymbolInstance(String name, MATRIX matrix, ColorTransform colorTransform, boolean cacheAsBitmap, int blendMode, List<FILTER> filters, boolean isVisible, RGBA backgroundColor, CLIPACTIONS clipActions, Amf3Value metadata, CharacterTag tag, HashMap<Integer, CharacterTag> characters, ReadOnlyTagList tags, FLAVersion flaVersion, XFLXmlWriter writer) throws XMLStreamException {
        if (matrix == null) {
            matrix = new MATRIX();
        }
        if (tag instanceof DefineButtonTag) {
            DefineButtonTag bt = (DefineButtonTag) tag;
            DefineButtonCxformTag bcx = (DefineButtonCxformTag) bt.getSwf().getCharacterIdTag(bt.buttonId, DefineButtonCxformTag.ID);
            if (bcx != null) {
                colorTransform = bcx.buttonColorTransform;
            }
        }

        writer.writeStartElement("DOMSymbolInstance", new String[]{"libraryItemName", "Symbol " + tag.getCharacterId()});
        if (name != null) {
            writer.writeAttribute("name", name);
        }
        String blendModeStr = null;
        if (blendMode < BLENDMODES.length) {
            blendModeStr = BLENDMODES[blendMode];
        }
        if (blendModeStr != null) {
            writer.writeAttribute("blendMode", blendModeStr);
        }
        if (tag instanceof ShapeTag) {
            writer.writeAttribute("symbolType", "graphic");
            writer.writeAttribute("loop", "loop");
        } else if (tag instanceof DefineSpriteTag) {
            DefineSpriteTag sprite = (DefineSpriteTag) tag;
            RECT spriteRect = sprite.getRect();
            double centerPoint3DX = twipToPixel(matrix.translateX + spriteRect.getWidth() / 2);
            double centerPoint3DY = twipToPixel(matrix.translateY + spriteRect.getHeight() / 2);
            writer.writeAttribute("centerPoint3DX", centerPoint3DX);
            writer.writeAttribute("centerPoint3DY", centerPoint3DY);
        } else if (tag instanceof ButtonTag) {
            writer.writeAttribute("symbolType", "button");
        }
        if (cacheAsBitmap) {
            writer.writeAttribute("cacheAsBitmap", true);
        }
        if (!isVisible && flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
            writer.writeAttribute("isVisible", false);
        }
        writer.writeStartElement("matrix");
        convertMatrix(matrix, writer);
        writer.writeEndElement();
        writer.writeStartElement("transformationPoint");
        writer.writeEmptyElement("Point");
        writer.writeEndElement();

        if (backgroundColor != null) {
            writer.writeStartElement("MatteColor", new String[]{"color", backgroundColor.toHexRGB()});
            if (backgroundColor.alpha != 255) {
                writer.writeAttribute("alpha", doubleToString(backgroundColor.getAlphaFloat()));
            }
            writer.writeEndElement();
        }
        if (colorTransform != null) {
            writer.writeStartElement("color");
            writer.writeStartElement("Color");
            if (colorTransform.getRedMulti() != 256) {
                writer.writeAttribute("redMultiplier", ((float) colorTransform.getRedMulti()) / 256.0f);
            }
            if (colorTransform.getGreenMulti() != 256) {
                writer.writeAttribute("greenMultiplier", ((float) colorTransform.getGreenMulti()) / 256.0f);
            }
            if (colorTransform.getBlueMulti() != 256) {
                writer.writeAttribute("blueMultiplier", ((float) colorTransform.getBlueMulti()) / 256.0f);
            }
            if (colorTransform.getAlphaMulti() != 256) {
                writer.writeAttribute("alphaMultiplier", ((float) colorTransform.getAlphaMulti()) / 256.0f);
            }

            if (colorTransform.getRedAdd() != 0) {
                writer.writeAttribute("redOffset", colorTransform.getRedAdd());
            }
            if (colorTransform.getGreenAdd() != 0) {
                writer.writeAttribute("greenOffset", colorTransform.getGreenAdd());
            }
            if (colorTransform.getBlueAdd() != 0) {
                writer.writeAttribute("blueOffset", colorTransform.getBlueAdd());
            }
            if (colorTransform.getAlphaAdd() != 0) {
                writer.writeAttribute("alphaOffset", colorTransform.getAlphaAdd());
            }

            writer.writeEndElement();
            writer.writeEndElement(); // color
        }
        if (filters != null) {
            writer.writeStartElement("filters");
            for (FILTER f : filters) {
                convertFilter(f, writer);
            }
            writer.writeEndElement();
        }
        if (tag instanceof DefineButtonTag) {
            writer.writeStartElement("Actionscript");
            writer.writeStartElement("script");
            writer.writeCData("on(press){\r\n" + convertActionScript12(new ButtonAction((DefineButtonTag) tag)) + "}");
            writer.writeEndElement();
            writer.writeEndElement();
        }
        if (tag instanceof DefineButton2Tag) {
            DefineButton2Tag db2 = (DefineButton2Tag) tag;
            if (!db2.actions.isEmpty()) {
                writer.writeStartElement("Actionscript");
                writer.writeStartElement("script");
                StringBuilder sbActions = new StringBuilder();
                for (BUTTONCONDACTION bca : db2.actions) {
                    sbActions.append(convertActionScript12(bca));
                }
                writer.writeCData(sbActions.toString());
                writer.writeEndElement();
                writer.writeEndElement();
            }
        }
        if (clipActions != null) {
            writer.writeStartElement("Actionscript");
            writer.writeStartElement("script");
            StringBuilder sbActions = new StringBuilder();
            for (CLIPACTIONRECORD rec : clipActions.clipActionRecords) {
                sbActions.append(convertActionScript12(rec));
            }
            writer.writeCData(sbActions.toString());
            writer.writeEndElement();
            writer.writeEndElement();
        }
        if (metadata != null && (metadata.getValue() instanceof ObjectType)) {
            ObjectType metadataObject = (ObjectType) metadata.getValue();

            if (metadataObject.isDynamic()) {
                writer.writeStartElement("persistentData");
                List<String> exportedNames = new ArrayList<>();
                for (String n : metadataObject.dynamicMembersKeySet()) {
                    Object v = metadataObject.getDynamicMember(n);
                    if (v instanceof Long) {
                        exportedNames.add(n);
                        writer.writeStartElement("PD");
                        writer.writeAttribute("n", n);
                        writer.writeAttribute("t", "i");
                        writer.writeAttribute("v", (Long) v);
                        writer.writeEndElement();
                        exportedNames.add(n);
                    } else if (v instanceof Double) {
                        writer.writeStartElement("PD");
                        writer.writeAttribute("n", n);
                        writer.writeAttribute("t", "d");
                        writer.writeAttribute("v", (Double) v);
                        writer.writeEndElement();
                        exportedNames.add(n);
                    } else if (v instanceof String) {
                        writer.writeStartElement("PD");
                        writer.writeAttribute("n", n);
                        //missing t attrinute = string (maybe "s"?)
                        writer.writeAttribute("v", (String) v);
                        writer.writeEndElement();
                        exportedNames.add(n);
                    }
                    /*
                    From JSFL, also data types integerArray ("I"), doubleArray("D") and byteArray("B") can be set.
                    These datatypes can be in the FLA file but are not exported to SWF with _EMBED_SWF_ publish format.
                     */
                }

                //Mark these names for publishing in embedded swf format:  (setPublishPersistentData function in JSFL)
                for (String n : exportedNames) {
                    writer.writeStartElement("PD");
                    writer.writeAttribute("n", PUBLISH_DATA_PREFIX + PUBLISH_DATA_FORMAT + n);
                    writer.writeAttribute("t", "i");
                    writer.writeAttribute("v", 1);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    private static String convertActionScript12(ASMSource as) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        try {
            as.getActionScriptSource(writer, null);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    private static long getTimestamp(SWF swf) {
        Date date = swf.getFileModificationDate();
        return date.getTime() / 1000;
    }

    private void convertLibrary(SWF swf, Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, Map<Integer, ScriptPack> characterScriptPacks, List<Integer> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion, XFLXmlWriter writer) throws XMLStreamException {

        //TODO: Imported assets
        //linkageImportForRS="true" linkageIdentifier="xxx" linkageURL="yyy.swf"
        convertMedia(swf, characterVariables, characterClasses, nonLibraryShapes, backgroundColor, tags, characters, files, datfiles, flaVersion, writer);
        convertSymbols(swf, characterVariables, characterClasses, characterScriptPacks, nonLibraryShapes, backgroundColor, tags, characters, files, datfiles, flaVersion, writer);
    }

    private void convertSymbols(SWF swf, Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, Map<Integer, ScriptPack> characterScriptPacks, List<Integer> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion, XFLXmlWriter writer) throws XMLStreamException {
        boolean hasSymbol = false;
        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if ((symbol instanceof ShapeTag) && nonLibraryShapes.contains(symbol.getCharacterId())) {
                continue; //shapes with 1 ocurrence and single layer are not added to library
            }

            if ((symbol instanceof ShapeTag) || (symbol instanceof DefineSpriteTag) || (symbol instanceof ButtonTag)) {
                XFLXmlWriter symbolStr = new XFLXmlWriter();

                symbolStr.writeStartElement("DOMSymbolItem", new String[]{
                    "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
                    "xmlns", "http://ns.adobe.com/xfl/2008/",
                    "name", "Symbol " + symbol.getCharacterId(),
                    "lastModified", Long.toString(getTimestamp(swf))}); //TODO:itemID
                if (symbol instanceof ShapeTag) {
                    symbolStr.writeAttribute("symbolType", "graphic");
                } else if (symbol instanceof ButtonTag) {
                    symbolStr.writeAttribute("symbolType", "button");
                    if (((ButtonTag) symbol).trackAsMenu()) {
                        symbolStr.writeAttribute("trackAsMenu", true);
                    }
                }
                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr.writeAttribute("linkageClassName", characterClasses.get(symbol.getCharacterId()));
                }
                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    symbolStr.writeAttribute("linkageIdentifier", characterVariables.get(symbol.getCharacterId()));
                }
                if (linkageExportForAS) {
                    symbolStr.writeAttribute("linkageExportForAS", true);
                }
                symbolStr.writeStartElement("timeline");
                String itemIcon = null;
                if (symbol instanceof ButtonTag) {
                    itemIcon = "0";
                    symbolStr.writeStartElement("DOMTimeline", new String[]{"name", "Symbol " + symbol.getCharacterId(), "currentFrame", "0"});
                    symbolStr.writeStartElement("layers");

                    ButtonTag button = (ButtonTag) symbol;
                    List<BUTTONRECORD> records = button.getRecords();

                    int maxDepth = 0;
                    for (BUTTONRECORD rec : records) {
                        if (rec.placeDepth > maxDepth) {
                            maxDepth = rec.placeDepth;
                        }
                    }
                    for (int i = maxDepth; i >= 1; i--) {
                        symbolStr.writeStartElement("DOMLayer", new String[]{"name", "Layer " + (maxDepth - i + 1)});
                        if (i == 1) {
                            symbolStr.writeAttribute("current", true);
                            symbolStr.writeAttribute("isSelected", true);
                        }
                        symbolStr.writeAttribute("color", randomOutlineColor());
                        symbolStr.writeStartElement("frames");
                        int lastFrame = 0;
                        DefineButtonSoundTag sound = button.getSounds();
                        loopframes:
                        for (int frame = 1; frame <= 4; frame++) {
                            if (sound != null) {
                                switch (frame) {
                                    case 1:
                                        break;
                                    case 2:
                                        break;
                                    case 3:
                                        break;
                                    case 4:
                                        break;
                                }
                            }
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
                                    if (character != null) {
                                        MATRIX matrix = rec.placeMatrix;
                                        XFLXmlWriter recCharWriter = new XFLXmlWriter();

                                        int characterId = character.getCharacterId();
                                        if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(characterId))) {
                                            ShapeTag shape = (ShapeTag) character;
                                            convertShape(characters, matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false, recCharWriter);
                                        } else if (character instanceof TextTag) {
                                            convertText(null, (TextTag) character, matrix, filters, null, recCharWriter);
                                        } else if (character instanceof DefineVideoStreamTag) {
                                            convertVideoInstance(null, matrix, (DefineVideoStreamTag) character, null, recCharWriter);
                                        } else {
                                            convertSymbolInstance(null, matrix, colorTransformAlpha, false, blendMode, filters, true, null, null, null, characters.get(rec.characterId), characters, tags, flaVersion, recCharWriter);
                                        }

                                        int duration = frame - lastFrame;
                                        lastFrame = frame;
                                        if (duration > 0) {
                                            if (duration > 1) {
                                                symbolStr.writeStartElement("DOMFrame", new String[]{
                                                    "index", Integer.toString(frame - duration),
                                                    "duration", Integer.toString(duration - 1),
                                                    "keyMode", Integer.toString(KEY_MODE_NORMAL),});
                                                symbolStr.writeElementValue("elements", "");
                                                symbolStr.writeEndElement();
                                            }
                                            symbolStr.writeStartElement("DOMFrame", new String[]{
                                                "index", Integer.toString(frame - 1),
                                                "keyMode", Integer.toString(KEY_MODE_NORMAL),});
                                            symbolStr.writeStartElement("elements");
                                            symbolStr.writeCharactersRaw(recCharWriter.toString());
                                            symbolStr.writeEndElement();
                                            symbolStr.writeEndElement();
                                        }
                                    } else {
                                        logger.log(Level.WARNING, "Character with id={0} was not found.", rec.characterId);
                                    }
                                }
                            }
                        }
                        symbolStr.writeEndElement(); // frames
                        symbolStr.writeEndElement(); // DOMLayer
                    }
                    symbolStr.writeEndElement(); // layers
                    symbolStr.writeEndElement(); // DOMTimeline
                } else if (symbol instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) symbol;
                    if (sprite.getTags().isEmpty()) { //probably AS2 class
                        continue;
                    }
                    final ScriptPack spriteScriptPack = characterScriptPacks.containsKey(sprite.spriteId) ? characterScriptPacks.get(sprite.spriteId) : null;
                    convertTimeline(sprite.spriteId, nonLibraryShapes, backgroundColor, tags, sprite.getTags(), characters, "Symbol " + symbol.getCharacterId(), flaVersion, files, symbolStr, spriteScriptPack);

                } else if (symbol instanceof ShapeTag) {
                    itemIcon = "1";
                    ShapeTag shape = (ShapeTag) symbol;
                    symbolStr.writeStartElement("DOMTimeline", new String[]{"name", "Symbol " + symbol.getCharacterId(), "currentFrame", "0"});
                    symbolStr.writeStartElement("layers");
                    SHAPEWITHSTYLE shapeWithStyle = shape.getShapes();
                    if (shapeWithStyle != null) {
                        convertShape(characters, null, shape.getShapeNum(), shapeWithStyle.shapeRecords, shapeWithStyle.fillStyles, shapeWithStyle.lineStyles, false, true, symbolStr);
                    }

                    symbolStr.writeEndElement(); // layers
                    symbolStr.writeEndElement(); // DOMTimeline
                }
                symbolStr.writeEndElement(); // timeline
                symbolStr.writeEndElement(); // DOMSymbolItem
                String symbolStr2 = prettyFormatXML(symbolStr.toString());
                String symbolFile = "Symbol " + symbol.getCharacterId() + ".xml";
                files.put(symbolFile, Utf8Helper.getBytes(symbolStr2));

                if (!hasSymbol) {
                    writer.writeStartElement("symbols");
                }

                // write symbLink
                writer.writeStartElement("Include", new String[]{"href", symbolFile});
                if (itemIcon != null) {
                    writer.writeAttribute("itemIcon", itemIcon);
                }
                writer.writeAttribute("loadImmediate", false);
                if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
                    writer.writeAttribute("lastModified", getTimestamp(swf));
                    //TODO: itemID="518de416-00000341"
                }
                writer.writeEndElement();
                hasSymbol = true;
            }
        }

        if (hasSymbol) {
            writer.writeEndElement();
        }
    }

    private void convertMedia(SWF swf, Map<Integer, String> characterVariables, Map<Integer, String> characterClasses, List<Integer> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, HashMap<Integer, CharacterTag> characters, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion, XFLXmlWriter writer) throws XMLStreamException {
        boolean hasMedia = false;
        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if (symbol instanceof ImageTag
                    || symbol instanceof SoundStreamHeadTypeTag || symbol instanceof DefineSoundTag
                    || symbol instanceof DefineVideoStreamTag) {
                hasMedia = true;
            }
        }

        if (!hasMedia) {
            return;
        }

        int mediaCount = 0;
        writer.writeStartElement("media");

        for (int ch : characters.keySet()) {
            CharacterTag symbol = characters.get(ch);
            if (symbol instanceof ImageTag) {
                ImageTag imageTag = (ImageTag) symbol;
                boolean allowSmoothing = false;

                //find if smoothed - a bitmap is smoothed when there is a shape with fillstyle smoothed bitmap
                looptags:
                for (Tag tag : swf.getTags()) {
                    if (tag instanceof ShapeTag) {
                        Set<Integer> needed = new HashSet<>();
                        tag.getNeededCharacters(needed);
                        ShapeTag sht = (ShapeTag) tag;
                        if (needed.contains(imageTag.getCharacterId())) {
                            List<FILLSTYLE> fs = new ArrayList<>();
                            SHAPEWITHSTYLE s = sht.getShapes();
                            for (FILLSTYLE f : s.fillStyles.fillStyles) {
                                fs.add(f);
                            }
                            for (SHAPERECORD r : s.shapeRecords) {
                                if (r instanceof StyleChangeRecord) {
                                    StyleChangeRecord scr = (StyleChangeRecord) r;
                                    if (scr.stateNewStyles) {
                                        for (FILLSTYLE f : scr.fillStyles.fillStyles) {
                                            fs.add(f);
                                        }
                                    }
                                }
                            }
                            for (FILLSTYLE f : fs) {
                                if (Arrays.asList(FILLSTYLE.REPEATING_BITMAP, FILLSTYLE.CLIPPED_BITMAP, FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP).contains(f.fillStyleType) && f.bitmapId == imageTag.getCharacterId()) {
                                    allowSmoothing = f.fillStyleType == FILLSTYLE.CLIPPED_BITMAP || f.fillStyleType == FILLSTYLE.REPEATING_BITMAP;
                                    break looptags;
                                }
                            }
                        }
                    }
                }

                byte[] imageBytes = Helper.readStream(imageTag.getImageData());
                SerializableImage image = imageTag.getImageCached();
                ImageFormat format = imageTag.getImageFormat();
                String symbolFile = "bitmap" + symbol.getCharacterId() + imageTag.getImageFormat().getExtension();
                files.put(symbolFile, imageBytes);
                writer.writeStartElement("DOMBitmapItem", new String[]{
                    "name", symbolFile,
                    "sourceLastImported", Long.toString(getTimestamp(swf)),
                    "externalFileSize", Integer.toString(imageBytes.length),});
                if (allowSmoothing) {
                    writer.writeAttribute("allowSmoothing", true);
                }
                switch (format) {
                    case PNG:
                    case GIF:
                        if (imageTag.getOriginalImageFormat() != ImageFormat.JPEG) {
                            writer.writeAttribute("useImportedJPEGData", false);
                            writer.writeAttribute("compressionType", "lossless");
                        }
                        writer.writeAttribute("originalCompressionType", "lossless");
                        break;
                    case JPEG:
                        writer.writeAttribute("isJPEG", true);
                        break;
                }
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    writer.writeAttribute("linkageExportForAS", true);
                    writer.writeAttribute("linkageClassName", characterClasses.get(symbol.getCharacterId()));
                }
                writer.writeAttribute("quality", 50);
                writer.writeAttribute("href", symbolFile);
                writer.writeAttribute("bitmapDataHRef", "M " + (mediaCount + 1) + " " + getTimestamp(swf) + ".dat");
                writer.writeAttribute("frameRight", image.getWidth());
                writer.writeAttribute("frameBottom", image.getHeight());
                writer.writeEndElement();
                mediaCount++;
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
                        SWFInputStream sis = new SWFInputStream(swf, soundData);
                        MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
                        if (!s.frames.isEmpty()) {
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
                        }
                    } catch (IOException | IndexOutOfBoundsException ex) {
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
                writer.writeStartElement("DOMSoundItem", new String[]{
                    "name", symbolFile,
                    "sourceLastImported", Long.toString(getTimestamp(swf)),
                    "externalFileSize", Integer.toString(data.length)});
                writer.writeAttribute("href", symbolFile);
                writer.writeAttribute("format", rateMap[soundRate] + "kHz" + " " + (soundSize ? "16bit" : "8bit") + " " + (soundType ? "Stereo" : "Mono"));
                writer.writeAttribute("exportFormat", format);
                writer.writeAttribute("exportBits", bits);
                writer.writeAttribute("sampleCount", soundSampleCount);

                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    writer.writeAttribute("linkageClassName", characterClasses.get(symbol.getCharacterId()));
                }

                if (characterVariables.containsKey(symbol.getCharacterId())) {
                    linkageExportForAS = true;
                    writer.writeAttribute("linkageIdentifier", characterVariables.get(symbol.getCharacterId()));
                }
                if (linkageExportForAS) {
                    writer.writeAttribute("linkageExportForAS", true);
                }

                writer.writeEndElement();
                mediaCount++;
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
                if (data.length == 0) { //Video has zero length, this probably means it is "Video - Actionscript-controlled"
                    long ts = getTimestamp(swf);
                    String datFileName = "M " + (datfiles.size() + 1) + " " + ts + ".dat";
                    writer.writeEmptyElement("DOMVideoItem", new String[]{
                        "name", symbolFile,
                        "sourceExternalFilepath", "./LIBRARY/" + symbolFile,
                        "sourceLastImported", Long.toString(ts),
                        "videoDataHRef", datFileName,
                        "channels", "0",
                        "isSpecial", "true"});
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
                    writer.writeStartElement("DOMVideoItem", new String[]{
                        "name", symbolFile,
                        "sourceLastImported", Long.toString(getTimestamp(swf)),
                        "externalFileSize", Integer.toString(data.length)});
                    writer.writeAttribute("href", symbolFile);
                    writer.writeAttribute("videoType", videoType);
                    writer.writeAttribute("fps", (int) swf.frameRate); // todo: is the cast to int needed?
                    writer.writeAttribute("width", video.width);
                    writer.writeAttribute("height", video.height);
                    double len = (double) video.numFrames / swf.frameRate;
                    writer.writeAttribute("length", len);
                    boolean linkageExportForAS = false;
                    if (characterClasses.containsKey(symbol.getCharacterId())) {
                        linkageExportForAS = true;
                        writer.writeAttribute("linkageClassName", characterClasses.get(symbol.getCharacterId()));
                    }
                    if (characterVariables.containsKey(symbol.getCharacterId())) {
                        linkageExportForAS = true;
                        writer.writeAttribute("linkageIdentifier", characterVariables.get(symbol.getCharacterId()));
                    }
                    if (linkageExportForAS) {
                        writer.writeAttribute("linkageExportForAS", true);
                    }
                    writer.writeEndElement();
                }

                mediaCount++;
            }
        }

        writer.writeEndElement();
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

    private static void convertFrame(boolean shapeTween, SoundStreamHeadTypeTag soundStreamHead, StartSoundTag startSound, int frame, int duration, String actionScript, String elements, HashMap<String, byte[]> files, XFLXmlWriter writer) throws XMLStreamException {
        DefineSoundTag sound = null;
        if (startSound != null) {
            SWF swf = startSound.getSwf();
            sound = swf.getSound(startSound.soundId);
        }

        writer.writeStartElement("DOMFrame");
        writer.writeAttribute("index", frame);
        if (duration > 1) {
            writer.writeAttribute("duration", duration);
        }
        if (shapeTween) {
            writer.writeAttribute("tweenType", "shape");
            writer.writeAttribute("keyMode", KEY_MODE_SHAPE_TWEEN);
        } else {
            writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
        }
        XFLXmlWriter soundEnvelopeStr = new XFLXmlWriter();
        if (soundStreamHead != null && startSound == null) {
            String soundName = "sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat().toString().toLowerCase();
            writer.writeAttribute("soundName", soundName);
            writer.writeAttribute("soundSync", "stream");
            soundEnvelopeStr.writeStartElement("SoundEnvelope");
            soundEnvelopeStr.writeEmptyElement("SoundEnvelopePoint", new String[]{"level0", "32768", "level1", "32768"});
            soundEnvelopeStr.writeEndElement();
        }
        if (startSound != null && sound != null) {
            String soundName = "sound" + sound.soundId + "." + sound.getExportFormat().toString().toLowerCase();
            writer.writeAttribute("soundName", soundName);
            if (startSound.soundInfo.hasInPoint) {
                writer.writeAttribute("inPoint44", startSound.soundInfo.inPoint);
            }
            if (startSound.soundInfo.hasOutPoint) {
                writer.writeAttribute("outPoint44", startSound.soundInfo.outPoint);
            }
            if (startSound.soundInfo.hasLoops) {
                if (startSound.soundInfo.loopCount == 32767) {
                    writer.writeAttribute("soundLoopMode", "loop");
                }
                writer.writeAttribute("soundLoop", startSound.soundInfo.loopCount);
            }

            if (startSound.soundInfo.syncStop) {
                writer.writeAttribute("soundSync", "stop");
            } else if (startSound.soundInfo.syncNoMultiple) {
                writer.writeAttribute("soundSync", "start");
            }
            soundEnvelopeStr.writeStartElement("SoundEnvelope");
            if (startSound.soundInfo.hasEnvelope) {
                SOUNDENVELOPE[] envelopeRecords = startSound.soundInfo.envelopeRecords;
                for (SOUNDENVELOPE env : envelopeRecords) {
                    soundEnvelopeStr.writeEmptyElement("SoundEnvelopePoint", new String[]{"mark44", Long.toString(env.pos44), "level0", Integer.toString(env.leftLevel), "level1", Integer.toString(env.rightLevel)});
                }

                if (envelopeRecords.length == 1
                        && envelopeRecords[0].leftLevel == 32768
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 0) {
                    writer.writeAttribute("soundEffect", "left channel");
                } else if (envelopeRecords.length == 1
                        && envelopeRecords[0].leftLevel == 0
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 32768) {
                    writer.writeAttribute("soundEffect", "right channel");
                } else if (envelopeRecords.length == 2
                        && envelopeRecords[0].leftLevel == 32768
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 0
                        && envelopeRecords[1].leftLevel == 0
                        && envelopeRecords[1].pos44 == sound.soundSampleCount
                        && envelopeRecords[1].rightLevel == 32768) {
                    writer.writeAttribute("soundEffect", "fade left to right");
                } else if (envelopeRecords.length == 2
                        && envelopeRecords[0].leftLevel == 0
                        && envelopeRecords[0].pos44 == 0
                        && envelopeRecords[0].rightLevel == 32768
                        && envelopeRecords[1].leftLevel == 32768
                        && envelopeRecords[1].pos44 == sound.soundSampleCount
                        && envelopeRecords[1].rightLevel == 0) {
                    writer.writeAttribute("soundEffect", "fade right to left");
                } else {
                    writer.writeAttribute("soundEffect", "custom");
                }
                //TODO: fade in, fade out

            } else {
                soundEnvelopeStr.writeEmptyElement("SoundEnvelopePoint", new String[]{"level0", "32768", "level1", "32768"});
            }
            soundEnvelopeStr.writeEndElement(); // SoundEnvelope
        }

        writer.writeCharactersRaw(soundEnvelopeStr.toString());
        if (!actionScript.isEmpty()) {
            writer.writeStartElement("Actionscript");
            writer.writeStartElement("script");
            writer.writeCData(actionScript);
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeStartElement("elements");
        writer.writeCharactersRaw(elements);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void convertVideoInstance(String instanceName, MATRIX matrix, DefineVideoStreamTag video, CLIPACTIONS clipActions, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("DOMVideoInstance", new String[]{
            "libraryItemName", "movie" + video.characterID + ".flv",
            "frameRight", Integer.toString((int) (SWF.unitDivisor * video.width)),
            "frameBottom", Integer.toString((int) (SWF.unitDivisor * video.height)),});
        if (instanceName != null) {
            writer.writeAttribute("name", instanceName);
        }

        writer.writeStartElement("matrix");
        convertMatrix(matrix, writer);
        writer.writeEndElement();
        writer.writeStartElement("transformationPoint");
        writer.writeEmptyElement("Point");
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void convertFrames(String prevStr, String afterStr, List<Integer> nonLibraryShapes, ReadOnlyTagList tags, ReadOnlyTagList timelineTags, HashMap<Integer, CharacterTag> characters, int depth, FLAVersion flaVersion, HashMap<String, byte[]> files, XFLXmlWriter writer) throws XMLStreamException {
        XFLXmlWriter writer2 = new XFLXmlWriter();
        prevStr += "<frames>";
        int frame = -1;
        String lastElements = "";

        int duration = 1;

        CharacterTag character = null;
        MATRIX matrix = null;
        Amf3Value metadata = null;
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
        List<Tag> timTags = timelineTags.toArrayList();
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
                            Amf3Value metadata2 = po.getAmfData();
                            if (metadata2 != null && metadata2.getValue() != null) {
                                metadata = metadata2;
                            }
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
                            metadata = po.getAmfData();
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
                    metadata = null;
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
                XFLXmlWriter elementsWriter = new XFLXmlWriter();
                if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(characterId) || shapeTweener != null)) {
                    ShapeTag shape = (ShapeTag) character;
                    convertShape(characters, matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false, elementsWriter);
                    shapeTween = false;
                    shapeTweener = null;
                } else if (character != null) {
                    if (character instanceof MorphShapeTag) {
                        MorphShapeTag m = (MorphShapeTag) character;
                        convertShape(characters, matrix, 3, m.getStartEdges().shapeRecords, m.getFillStyles().getStartFillStyles(), m.getLineStyles().getStartLineStyles(m.getShapeNum()), true, false, elementsWriter);
                        shapeTween = true;
                    } else {
                        shapeTween = false;
                        if (character instanceof TextTag) {
                            convertText(instanceName, (TextTag) character, matrix, filters, clipActions, elementsWriter);
                        } else if (character instanceof DefineVideoStreamTag) {
                            convertVideoInstance(instanceName, matrix, (DefineVideoStreamTag) character, clipActions, elementsWriter);
                        } else {
                            convertSymbolInstance(instanceName, matrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, metadata, character, characters, tags, flaVersion, elementsWriter);
                        }
                    }
                }

                frame++;
                String elements = elementsWriter.toString();
                if (!elements.equals(lastElements) && frame > 0) {
                    convertFrame(lastShapeTween, null, null, frame - duration, duration, "", lastElements, files, writer2);
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
            convertFrame(lastShapeTween, null, null, (frame - duration < 0 ? 0 : frame - duration), duration, "", lastElements, files, writer2);
        }
        afterStr = "</frames>" + afterStr;

        if (writer2.length() > 0) {
            writer.writeCharactersRaw(prevStr);
            writer.writeCharactersRaw(writer2.toString());
            writer.writeCharactersRaw(afterStr);
        }
    }

    private static void convertFonts(ReadOnlyTagList tags, XFLXmlWriter writer) throws XMLStreamException {
        boolean hasFont = false;
        for (Tag t : tags) {
            if (t instanceof FontTag) {
                FontTag font = (FontTag) t;
                if (font.getCharacterCount() > 0) {
                    hasFont = true;
                    break;
                }
            }
        }

        if (!hasFont) {
            return;
        }

        writer.writeStartElement("fonts");

        for (Tag t : tags) {
            if (t instanceof FontTag) {
                SWF swf = t.getSwf();
                FontTag font = (FontTag) t;
                int fontId = font.getFontId();
                DefineFontNameTag fontNameTag = (DefineFontNameTag) swf.getCharacterIdTag(fontId, DefineFontNameTag.ID);
                String fontName = fontNameTag == null ? null : fontNameTag.fontName;
                if (fontName == null) {
                    fontName = font.getFontNameIntag();
                }
                if (fontName == null) {
                    fontName = FontTag.getDefaultFontName();
                }
                int fontStyle = font.getFontStyle();
                String installedFont;
                if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                    fontName = new Font(installedFont, fontStyle, 10).getPSName();
                }
                String embedRanges = "";

                String fontChars = font.getCharacters();
                if ("".equals(fontChars)) {
                    continue;
                }
                String embeddedCharacters = fontChars;
                embeddedCharacters = embeddedCharacters.replace("\u00A0", ""); // nonbreak space
                embeddedCharacters = embeddedCharacters.replace(".", ""); // todo: honfika: why?
                for (char i = 0; i < 32; i++) {
                    if (i == 9 || i == 10 || i == 13) {
                        continue;
                    }

                    embeddedCharacters = embeddedCharacters.replace("" + i, ""); // not supported in xml
                }

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

                writer.writeStartElement("DOMFontItem", new String[]{
                    "name", "Font " + fontId,
                    "font", fontName,
                    "size", "0",
                    "id", Integer.toString(fontId),
                    "embedRanges", embedRanges});
                if (!"".equals(embeddedCharacters)) {
                    writer.writeAttribute("embeddedCharacters", embeddedCharacters);
                }

                writer.writeEndElement();
            }
        }

        writer.writeEndElement();
    }

    private static int getPackMainClassId(ScriptPack pack) {
        ABC abc = pack.abc;
        ScriptInfo script = abc.script_info.get(pack.scriptIndex);
        for (int traitIndex : pack.traitIndices) {
            Trait trait = script.traits.traits.get(traitIndex);
            if (trait instanceof TraitClass) {
                TraitClass tc = (TraitClass) trait;
                Namespace traitNameNamespace = abc.constants.getNamespace(trait.getName(abc).namespace_index);
                if (traitNameNamespace.kind == Namespace.KIND_PACKAGE) { //its public class
                    //assuming the one public class in the pack is the class we are looking for
                    return tc.class_info;
                }
            }
        }
        return -1;
    }

    private static Map<Integer, String> getFrameScriptsFromPack(ScriptPack pack) {
        Map<Integer, String> ret = new HashMap<>();
        int classIndex = getPackMainClassId(pack);
        if (classIndex > -1) {
            ABC abc = pack.abc;
            InstanceInfo instanceInfo = abc.instance_info.get(classIndex);
            int constructorMethodIndex = instanceInfo.iinit_index;
            MethodBody constructorBody = abc.findBody(constructorMethodIndex);
            try {
                if (constructorBody.convertedItems == null) {
                    constructorBody.convert(new ConvertData(), "??", ScriptExportMode.AS, true, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true);
                }

                Map<Integer, Integer> frameToTraitMultiname = new HashMap<>();

                //find all addFrameScript(xx,this.method) in constructor
                /*
                It looks like this:
                CallPropertyAVM2Item
                ->propertyName == FullMultinameAVM2Item
                        -> resolvedMultinameName (String) "addFrameScript"
                ->arguments
                        ->0 IntegerValueAVM2Item
                                ->value (Long) 0    - zero based
                        ->1 GetPropertyAVM2Item
                                ->object (ThisAVM2Item)
                                ->propertyName (FullMultinameAvm2Item)
                                        ->multinameIndex
                                        ->resolvedMultinameName (String) "frame1"
                 */
                if (constructorBody.convertedItems != null) {
                    for (GraphTargetItem ti : constructorBody.convertedItems) {
                        if (ti instanceof CallPropertyAVM2Item) {
                            CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                            if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                if ("addFrameScript".equals(propName.resolvedMultinameName)) {
                                    if (callProp.arguments.size() == 2) {
                                        if (callProp.arguments.get(0) instanceof IntegerValueAVM2Item) {
                                            IntegerValueAVM2Item frameItem = (IntegerValueAVM2Item) callProp.arguments.get(0);
                                            int frame = frameItem.intValue();
                                            if (callProp.arguments.get(1) instanceof GetPropertyAVM2Item) {
                                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) callProp.arguments.get(1);
                                                if (getProp.object instanceof ThisAVM2Item) {
                                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                        FullMultinameAVM2Item framePropName = (FullMultinameAVM2Item) getProp.propertyName;
                                                        int multinameIndex = framePropName.multinameIndex;
                                                        frameToTraitMultiname.put(frame, multinameIndex);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Map<Integer, TraitMethodGetterSetter> multinameToMethodTrait = new HashMap<>();
                for (Trait trait : instanceInfo.instance_traits.traits) {
                    if (trait instanceof TraitMethodGetterSetter) {
                        multinameToMethodTrait.put(trait.name_index, (TraitMethodGetterSetter) trait);
                    }
                }

                for (int frame : frameToTraitMultiname.keySet()) {
                    int multiName = frameToTraitMultiname.get(frame);
                    if (multinameToMethodTrait.containsKey(multiName)) {
                        TraitMethodGetterSetter methodTrait = multinameToMethodTrait.get(multiName);
                        int methodIndex = methodTrait.method_info;
                        MethodBody frameBody = abc.findBody(methodIndex);

                        StringBuilder scriptBuilder = new StringBuilder();
                        frameBody.convert(new ConvertData(), "??", ScriptExportMode.AS, false, methodIndex, pack.scriptIndex, classIndex, abc, methodTrait, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new ArrayList<>(), true);
                        StringBuilderTextWriter writer = new StringBuilderTextWriter(Configuration.getCodeFormatting(), scriptBuilder);
                        frameBody.toString("??", ScriptExportMode.AS, abc, methodTrait, writer, new ArrayList<>());

                        String script = scriptBuilder.toString();
                        ret.put(frame, script);
                    }
                }

            } catch (InterruptedException ex) {
                //ignore
            }
        }
        return ret;
    }

    private boolean convertActionScriptLayer(int spriteId, ReadOnlyTagList tags, ReadOnlyTagList timeLineTags, String backgroundColor, XFLXmlWriter writer, ScriptPack scriptPack) throws XMLStreamException {
        boolean hasScript = false;

        String script = "";
        int duration = 0;
        int frame = 0;
        for (Tag t : tags) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag dia = (DoInitActionTag) t;
                if (dia.spriteId == spriteId) {
                    script += convertActionScript12(dia);
                }
            }
        }
        if (!script.isEmpty()) {
            script = "#initclip\r\n" + script + "#endinitclip\r\n";
        }

        Map<Integer, String> frameToScriptMap = new HashMap<>();

        if (scriptPack != null) {
            frameToScriptMap = getFrameScriptsFromPack(scriptPack);
        }

        for (Tag t : timeLineTags) {
            if (t instanceof DoActionTag) {
                DoActionTag da = (DoActionTag) t;
                script += convertActionScript12(da);
            }
            if (frameToScriptMap.containsKey(frame)) {
                script += frameToScriptMap.get(frame);
                frameToScriptMap.remove(frame);
            }
            if (t instanceof ShowFrameTag) {

                if (script.isEmpty()) {
                    duration++;
                } else {
                    if (!hasScript) {
                        writer.writeStartElement("DOMLayer", new String[]{"name", "Script Layer", "color", randomOutlineColor()});
                        writer.writeStartElement("frames");
                        hasScript = true;
                    }

                    if (duration > 0) {
                        writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(frame - duration)});
                        if (duration > 1) {
                            writer.writeAttribute("duration", duration);
                        }
                        writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                        writer.writeElementValue("elements", "");
                        writer.writeEndElement();
                    }

                    writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(frame)});
                    writer.writeAttribute("keyMode", KEY_MODE_NORMAL);

                    writer.writeStartElement("Actionscript");
                    writer.writeStartElement("script");
                    writer.writeCData(script);
                    writer.writeEndElement();
                    writer.writeEndElement();

                    writer.writeElementValue("elements", "");
                    writer.writeEndElement();
                    script = "";
                    duration = 0;
                }
                frame++;
            }
        }

        if (hasScript) {
            writer.writeEndElement(); // frames
            writer.writeEndElement(); // DOMLayer
        }

        return hasScript;
    }

    private boolean convertLabelsLayer(int spriteId, ReadOnlyTagList tags, ReadOnlyTagList timeLineTags, String backgroundColor, XFLXmlWriter writer) throws XMLStreamException {
        boolean hasLabel = false;

        int duration = 0;
        int frame = 0;
        String frameLabel = "";
        boolean isAnchor = false;
        for (Tag t : timeLineTags) {
            if (t instanceof FrameLabelTag) {
                FrameLabelTag fl = (FrameLabelTag) t;
                frameLabel = fl.getLabelName();
                isAnchor = fl.isNamedAnchor();
            } else if (t instanceof ShowFrameTag) {
                if (frameLabel.isEmpty()) {
                    duration++;
                } else {
                    if (!hasLabel) {
                        writer.writeStartElement("DOMLayer", new String[]{"name", "Labels Layer", "color", randomOutlineColor()});
                        writer.writeStartElement("frames");
                        hasLabel = true;
                    }

                    if (duration > 0) {
                        writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(frame - duration)});
                        if (duration > 1) {
                            writer.writeAttribute("duration", duration);
                        }
                        writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                        writer.writeElementValue("elements", "");
                        writer.writeEndElement();
                    }

                    writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(frame)});
                    writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                    writer.writeAttribute("name", frameLabel);
                    if (isAnchor) {
                        writer.writeAttribute("labelType", "anchor");
                        writer.writeAttribute("bookmark", true);
                    } else {
                        writer.writeAttribute("labelType", "name");
                    }
                    writer.writeElementValue("elements", "");
                    writer.writeEndElement();
                    frameLabel = "";
                    duration = 0;
                }
                frame++;
            }
        }

        if (hasLabel) {
            writer.writeEndElement(); // frames
            writer.writeEndElement(); // DOMLayer
        }

        return hasLabel;
    }

    private void convertSoundLayer(int layerIndex, ReadOnlyTagList timeLineTags, HashMap<String, byte[]> files, XFLXmlWriter writer) throws XMLStreamException {
        int soundLayerIndex = 0;
        XFLXmlWriter writer2 = new XFLXmlWriter();
        List<StartSoundTag> startSounds = new ArrayList<>();
        List<Integer> startSoundFrameNumbers = new ArrayList<>();
        List<SoundStreamHeadTypeTag> soundStreamHeads = new ArrayList<>();
        List<Integer> soundStreamHeadFrameNumbers = new ArrayList<>();
        int frame = 0;
        for (Tag t : timeLineTags) {
            if (t instanceof StartSoundTag) {
                StartSoundTag startSound = (StartSoundTag) t;
                SWF swf = startSound.getSwf();
                DefineSoundTag s = swf.getSound(startSound.soundId);
                if (s == null) {
                    logger.log(Level.WARNING, "Sount tag (ID={0}) was not found", startSound.soundId);
                    continue;
                }

                if (!files.containsKey("sound" + s.soundId + "." + s.getExportFormat().toString().toLowerCase())) { //Sound was not exported
                    startSound = null; // ignore
                }

                if (startSound != null) {
                    startSounds.add(startSound);
                    startSoundFrameNumbers.add(frame);
                }
            } else if (t instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag soundStreamHead = (SoundStreamHeadTypeTag) t;
                if (!files.containsKey("sound" + soundStreamHead.getCharacterId() + "." + soundStreamHead.getExportFormat().toString().toLowerCase())) { //Sound was not exported
                    soundStreamHead = null; // ignore
                }

                if (soundStreamHead != null) {
                    soundStreamHeads.add(soundStreamHead);
                    soundStreamHeadFrameNumbers.add(frame);
                }
            } else if (t instanceof ShowFrameTag) {
                frame++;
            }
        }

        for (int i = 0; i < soundStreamHeads.size(); i++) {
            writer.writeStartElement("DOMLayer", new String[]{"name", "Sound Layer " + (soundLayerIndex++), "color", randomOutlineColor()});
            writer.writeStartElement("frames");

            int startFrame = soundStreamHeadFrameNumbers.get(i);
            int duration = frame - startFrame;

            if (startFrame != 0) {
                // empty frames should be added
                convertFrame(false, null, null, 0, startFrame, "", "", files, writer);
            }

            convertFrame(false, soundStreamHeads.get(i), null, startFrame, duration, "", "", files, writer);

            writer.writeEndElement();
            writer.writeEndElement();
        }

        for (int i = 0; i < startSounds.size(); i++) {
            writer.writeStartElement("DOMLayer", new String[]{"name", "Sound Layer " + (soundLayerIndex++), "color", randomOutlineColor()});
            writer.writeStartElement("frames");

            int startFrame = startSoundFrameNumbers.get(i);
            int duration = frame - startFrame;

            if (startFrame != 0) {
                // empty frames should be added
                convertFrame(false, null, null, 0, startFrame, "", "", files, writer);
            }

            convertFrame(false, null, startSounds.get(i), startFrame, duration, "", "", files, writer);

            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private String randomOutlineColor() {
        RGB outlineColor = new RGB();
        do {
            outlineColor.red = random.nextInt(256);
            outlineColor.green = random.nextInt(256);
            outlineColor.blue = random.nextInt(256);
        } while ((outlineColor.red + outlineColor.green + outlineColor.blue) / 3 < 128);
        return outlineColor.toHexRGB();
    }

    private void convertTimeline(int spriteId, List<Integer> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, ReadOnlyTagList timelineTags, HashMap<Integer, CharacterTag> characters, String name, FLAVersion flaVersion, HashMap<String, byte[]> files, XFLXmlWriter writer, ScriptPack scriptPack) throws XMLStreamException {
        writer.writeStartElement("DOMTimeline", new String[]{"name", name});
        writer.writeStartElement("layers");

        boolean hasLabel = convertLabelsLayer(spriteId, tags, timelineTags, backgroundColor, writer);
        boolean hasScript = convertActionScriptLayer(spriteId, tags, timelineTags, backgroundColor, writer, scriptPack);

        int index = 0;

        if (hasLabel) {
            index++;
        }

        if (hasScript) {
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

                        writer.writeStartElement("DOMLayer", new String[]{
                            "name", "Layer " + (index + 1),
                            "color", randomOutlineColor(),
                            "layerType", "mask",
                            "locked", "true"});
                        convertFrames("", "", nonLibraryShapes, tags, timelineTags, characters, po.getDepth(), flaVersion, files, writer);
                        writer.writeEndElement();
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

            XFLXmlWriter layerPrev = new XFLXmlWriter();
            layerPrev.writeStartElement("DOMLayer", new String[]{
                "name", "Layer " + (index + 1),
                "color", randomOutlineColor()
            });
            if (d == 1) {
                layerPrev.writeAttribute("current", true);
                layerPrev.writeAttribute("isSelected", true);
            }
            if (parentLayer != -1) {
                if (parentLayer != d) {
                    layerPrev.writeAttribute("parentLayerIndex", parentLayer);
                    layerPrev.writeAttribute("locked", true);
                }
            }
            layerPrev.writeCharacters(""); // todo honfika: hack to close start tag
            String layerAfter = "</DOMLayer>";
            int prevLength = writer.length();
            convertFrames(layerPrev.toString(), layerAfter, nonLibraryShapes, tags, timelineTags, characters, d, flaVersion, files, writer);
            if (writer.length() == prevLength) {
                index--;
            }
        }

        int soundLayerIndex = layerCount;
        layerCount++;
        convertSoundLayer(soundLayerIndex, timelineTags, files, writer);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void writeFile(AbortRetryIgnoreHandler handler, final byte[] data, final String file) throws IOException, InterruptedException {
        new RetryTask(() -> {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
        }, handler).run();
    }

    private static Map<Integer, ScriptPack> getCharacterScriptPacks(SWF swf, Map<Integer, String> characterClasses) {
        Map<Integer, ScriptPack> ret = new HashMap<>();

        Map<String, Integer> classToId = new HashMap<>();
        for (int id : characterClasses.keySet()) {
            classToId.put(characterClasses.get(id), id);
        }

        List<String> allClasses = new ArrayList<>(characterClasses.values());
        List<ScriptPack> packs = new ArrayList<>();
        try {
            packs = swf.getScriptPacksByClassNames(allClasses);
        } catch (Exception ex) {
            //ignore
        }
        for (ScriptPack pack : packs) {
            String packClass = pack.getClassPath().toRawString();
            if (classToId.containsKey(packClass)) {
                ret.put(classToId.get(packClass), pack);
            }
        }
        return ret;
    }

    private static Map<Integer, String> getCharacterClasses(ReadOnlyTagList tags) {
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

    private static Map<Integer, String> getCharacterVariables(ReadOnlyTagList tags) {
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

    private static void convertText(String instanceName, TextTag tag, MATRIX m, List<FILTER> filters, CLIPACTIONS clipActions, XFLXmlWriter writer) throws XMLStreamException {
        MATRIX matrix = new MATRIX(m);
        CSMTextSettingsTag csmts = null;
        XFLXmlWriter filterStr = new XFLXmlWriter();
        if (filters != null) {
            filterStr.writeStartElement("filters");
            for (FILTER f : filters) {
                convertFilter(f, filterStr);
            }
            filterStr.writeEndElement();
        }

        SWF swf = tag.getSwf();
        for (Tag t : swf.getTags()) {
            if (t instanceof CSMTextSettingsTag) {
                CSMTextSettingsTag c = (CSMTextSettingsTag) t;
                if (c.textID == tag.getCharacterId()) {
                    csmts = c;
                    break;
                }
            }
        }

        String fontRenderingMode = "standard";
        String antiAliasSharpness = null;
        String antiAliasThickness = null;
        if (csmts != null) {
            if (csmts.thickness == 0 & csmts.sharpness == 0) {
                fontRenderingMode = null;
            } else {
                fontRenderingMode = "customThicknessSharpness";
            }
            antiAliasSharpness = doubleToString(csmts.sharpness);
            antiAliasThickness = doubleToString(csmts.thickness);
        }
        String left = null;
        RECT bounds = tag.getBounds();
        if ((tag instanceof DefineTextTag) || (tag instanceof DefineText2Tag)) {
            MATRIX textMatrix = tag.getTextMatrix();
            left = doubleToString((textMatrix.translateX) / SWF.unitDivisor);
        }
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

            writer.writeStartElement("DOMStaticText");
            if (left != null) {
                writer.writeAttribute("left", left);
            }
            if (fontRenderingMode != null) {
                writer.writeAttribute("fontRenderingMode", fontRenderingMode);
            }
            if (instanceName != null) {
                writer.writeAttribute("instanceName", instanceName);
            }
            if (antiAliasSharpness != null) {
                writer.writeAttribute("antiAliasSharpness", antiAliasSharpness);
                writer.writeAttribute("antiAliasThickness", antiAliasThickness);
            }
            Map<String, Object> attrs = TextTag.getTextRecordsAttributes(textRecords, swf);
            writer.writeAttribute("width", tag.getBounds().getWidth() / 2);
            writer.writeAttribute("height", tag.getBounds().getHeight());
            writer.writeAttribute("autoExpand", true);
            writer.writeAttribute("isSelectable", false);
            writer.writeStartElement("matrix");
            convertMatrix(matrix, writer);
            writer.writeEndElement();

            writer.writeStartElement("textRuns");
            int fontId;
            FontTag font = null;
            String fontName;
            String psFontName = null;
            int textHeight = -1;
            RGB textColor = null;
            RGBA textColorA = null;
            boolean newline;
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
                    for (Tag t : swf.getTags()) {
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
                        fontName = FontTag.getDefaultFontName();
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
                    writer.writeStartElement("DOMTextRun");
                    writer.writeStartElement("characters");
                    writer.writeCharacters((newline ? "\r" : "") + rec.getText(font));
                    writer.writeEndElement();
                    writer.writeStartElement("textAttrs");

                    writer.writeStartElement("DOMTextAttrs", new String[]{
                        "aliasText", "false",
                        "rotation", "true",
                        "size", Double.toString(twipToPixel(textHeight)),
                        "bitmapSize", Integer.toString(textHeight),
                        "letterSpacing", doubleToString(twipToPixel(letterSpacings.get(r))),
                        "indent", doubleToString(twipToPixel((int) attrs.get("indent"))),
                        "leftMargin", doubleToString(twipToPixel(leftMargins.get(r))),
                        "lineSpacing", doubleToString(twipToPixel((int) attrs.get("lineSpacing"))),
                        "rightMargin", doubleToString(twipToPixel((int) attrs.get("rightMargin")))
                    });

                    if (textColor != null) {
                        writer.writeAttribute("fillColor", textColor.toHexRGB());
                    } else if (textColorA != null) {
                        writer.writeAttribute("fillColor", textColorA.toHexRGB());
                        writer.writeAttribute("alpha", textColorA.getAlphaFloat());
                    }
                    writer.writeAttribute("face", psFontName);
                    writer.writeEndElement();

                    writer.writeEndElement(); // textAttrs
                    writer.writeEndElement(); // DOMTextRun
                }
            }
            writer.writeEndElement(); // textRuns
            writer.writeCharactersRaw(filterStr.toString());
            writer.writeEndElement(); // DOMStaticText
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
            writer.writeStartElement(tagName);
            if (fontRenderingMode != null) {
                writer.writeAttribute("fontRenderingMode", fontRenderingMode);
            }
            if (instanceName != null) {
                writer.writeAttribute("name", instanceName);
            }
            if (antiAliasSharpness != null) {
                writer.writeAttribute("antiAliasSharpness", antiAliasSharpness);
                writer.writeAttribute("antiAliasThickness", antiAliasThickness);
            }
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
            writer.writeAttribute("width", width);
            writer.writeAttribute("height", height);
            if (det.border) {
                writer.writeAttribute("border", true);
            }
            if (det.html) {
                writer.writeAttribute("renderAsHTML", true);
            }
            if (det.noSelect) {
                writer.writeAttribute("isSelectable", false);
            }
            if (det.multiline && det.wordWrap) {
                writer.writeAttribute("lineType", "multiline");
            } else if (det.multiline && (!det.wordWrap)) {
                writer.writeAttribute("lineType", "multiline no wrap");
            } else if (det.password) {
                writer.writeAttribute("lineType", "password");
            }
            if (det.hasMaxLength) {
                writer.writeAttribute("maxCharacters", det.maxLength);
            }
            if (!det.variableName.isEmpty()) {
                writer.writeAttribute("variableName", det.variableName);
            }
            writer.writeStartElement("matrix");
            convertMatrix(matrix, writer);
            writer.writeEndElement();
            writer.writeStartElement("textRuns");
            String txt = "";
            if (det.hasText) {
                txt = det.initialText;
            }

            if (det.html) {
                writer.writeCharactersRaw(convertHTMLText(swf.getTags(), det, txt));
            } else {
                writer.writeStartElement("DOMTextRun");
                writer.writeStartElement("characters");
                writer.writeCharacters(txt);
                writer.writeEndElement();
                int leftMargin = -1;
                int rightMargin = -1;
                int indent = -1;
                int lineSpacing = -1;
                String alignment = null;
                boolean italic;
                boolean bold;
                String fontFace = null;
                int size = -1;
                RGBA textColor = null;
                if (det.hasTextColor) {
                    textColor = det.textColor;
                }
                if (det.hasFont) {
                    String fontName = null;
                    for (Tag u : swf.getTags()) {
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
                            fontName = FontTag.getDefaultFontName();
                        }
                        italic = ft.isItalic();
                        bold = ft.isBold();
                        size = det.fontHeight;
                        fontFace = fontName;
                        String installedFont;
                        if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                            //fontName = installedFont;
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
                writer.writeStartElement("textAttrs");
                writer.writeStartElement("DOMTextAttrs");
                if (alignment != null) {
                    writer.writeAttribute("alignment", alignment);
                }
                writer.writeAttribute("rotation", true); //?
                if (indent > -1) {
                    writer.writeAttribute("indent", twipToPixel(indent));
                }
                if (leftMargin > -1) {
                    writer.writeAttribute("leftMargin", twipToPixel(leftMargin));
                }
                if (lineSpacing > -1) {
                    writer.writeAttribute("lineSpacing", twipToPixel(lineSpacing));
                }
                if (rightMargin > -1) {
                    writer.writeAttribute("rightMargin", twipToPixel(rightMargin));
                }
                if (size > -1) {
                    writer.writeAttribute("size", twipToPixel(size));
                    writer.writeAttribute("bitmapSize", size);
                }
                if (fontFace != null) {
                    writer.writeAttribute("face", fontFace);
                }
                if (textColor != null) {
                    writer.writeAttribute("fillColor", textColor.toHexRGB());
                    writer.writeAttribute("alpha", textColor.getAlphaFloat());
                }
                writer.writeEndElement();
                writer.writeEndElement(); // textAttrs
                writer.writeEndElement(); // DOMTextRun
            }
            writer.writeEndElement(); // textRuns
            writer.writeCharactersRaw(filterStr.toString());
            writer.writeEndElement(); // tagName
        }
    }

    private boolean hasAmfMetadata(Tag tag) {
        if (tag instanceof PlaceObjectTypeTag) {
            PlaceObjectTypeTag po = (PlaceObjectTypeTag) tag;
            if (po.getAmfData() != null && po.getAmfData().getValue() != null) {
                return true;
            }
        }
        if (tag instanceof Timelined) {
            Timelined tl = (Timelined) tag;
            for (Tag t : tl.getTags()) {
                if (hasAmfMetadata(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAmfMetadata(SWF swf) {
        for (Tag t : swf.getTags()) {
            if (hasAmfMetadata(t)) {
                return true;
            }
        }
        return false;
    }

    public void convertSWF(AbortRetryIgnoreHandler handler, SWF swf, String swfFileName, String outfile, XFLExportSettings settings, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion flaVersion) throws IOException, InterruptedException {

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
        String baseName = swfFileName;
        File f = new File(baseName);
        baseName = f.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.'));
        }
        final HashMap<String, byte[]> files = new HashMap<>();
        final HashMap<String, byte[]> datfiles = new HashMap<>();
        HashMap<Integer, CharacterTag> characters = getCharacters(swf.getTags());
        List<Integer> nonLibraryShapes = getNonLibraryShapes(swf.getTags(), characters);
        Map<Integer, String> characterClasses = getCharacterClasses(swf.getTags());
        Map<Integer, ScriptPack> characterScriptPacks = getCharacterScriptPacks(swf, characterClasses);
        Map<Integer, String> characterVariables = getCharacterVariables(swf.getTags());
        boolean hasAmfMetadata = hasAmfMetadata(swf);

        String backgroundColor = "#ffffff";
        SetBackgroundColorTag setBgColorTag = swf.getBackgroundColor();
        if (setBgColorTag != null) {
            backgroundColor = setBgColorTag.backgroundColor.toHexRGB();
        }

        double width = twipToPixel(swf.displayRect.getWidth());
        double height = twipToPixel(swf.displayRect.getHeight());

        XFLXmlWriter domDocument = new XFLXmlWriter();
        try {
            domDocument.writeStartElement("DOMDocument", new String[]{
                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
                "xmlns", "http://ns.adobe.com/xfl/2008/",
                "currentTimeline", "1",
                "xflVersion", flaVersion.xflVersion(),
                "creatorInfo", generator,
                "platform", "Windows",
                "versionInfo", "Saved by " + generatorVerName,
                "majorVersion", generatorVersion,
                "buildNumber", "",
                "nextSceneIdentifier", "2",
                "playOptionsPlayLoop", "false",
                "playOptionsPlayPages", "false",
                "playOptionsPlayFrameActions", "false",
                "autoSaveHasPrompted", "true",
                "backgroundColor", backgroundColor,
                "frameRate", Integer.toString((int) swf.frameRate) // todo: is the cast to int needed?
            });

            if (Double.compare(width, 550) != 0) {
                domDocument.writeAttribute("width", doubleToString(width));

            }
            if (Double.compare(height, 400) != 0) {
                domDocument.writeAttribute("height", doubleToString(height));
            }

            convertFonts(swf.getTags(), domDocument);
            convertLibrary(swf, characterVariables, characterClasses, characterScriptPacks, nonLibraryShapes, backgroundColor, swf.getTags(), characters, files, datfiles, flaVersion, domDocument);

            domDocument.writeStartElement("timelines");
            ScriptPack documentScriptPack = characterScriptPacks.containsKey(0) ? characterScriptPacks.get(0) : null;
            convertTimeline(0, nonLibraryShapes, backgroundColor, swf.getTags(), swf.getTags(), characters, "Scene 1", flaVersion, files, domDocument, documentScriptPack);
            domDocument.writeEndElement();

            if (hasAmfMetadata) {
                domDocument.writeStartElement("persistentData");

                domDocument.writeStartElement("PD");
                domDocument.writeAttribute("n", PUBLISH_DATA_PREFIX + PUBLISH_DATA_FORMAT);
                domDocument.writeAttribute("t", "i");
                domDocument.writeAttribute("v", 1);
                domDocument.writeEndElement();

                domDocument.writeEndElement();
            }

            domDocument.writeEndElement();
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        String domDocumentStr = prettyFormatXML(domDocument.toString());

        if (settings.exportScript) {
            for (Tag t : swf.getTags()) {
                if (t instanceof DoInitActionTag) {
                    DoInitActionTag dia = (DoInitActionTag) t;
                    int chid = dia.getCharacterId();
                    if (characters.containsKey(chid)) {
                        if (characters.get(chid) instanceof DefineSpriteTag) {
                            DefineSpriteTag sprite = (DefineSpriteTag) characters.get(chid);
                            if (sprite.getTags().isEmpty()) {
                                String data = convertActionScript12(dia);
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
        }

        int flaSwfVersion = swf.version > flaVersion.maxSwfVersion() ? flaVersion.maxSwfVersion() : swf.version;
        boolean greaterThanCC = flaVersion.ordinal() >= FLAVersion.CC.ordinal();
        XFLXmlWriter publishSettings = new XFLXmlWriter();
        try {
            publishSettings.writeStartElement("flash_profiles");
            publishSettings.writeStartElement("flash_profile", new String[]{"version", "1.0", "name", "Default", "current", "true"});

            publishSettings.writeStartElement("PublishFormatProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("defaultNames", 1);
            publishSettings.writeElementValue("flash", 1);
            publishSettings.writeElementValue("projectorWin", 0);
            publishSettings.writeElementValue("projectorMac", 0);
            publishSettings.writeElementValue("html", 1);
            publishSettings.writeElementValue("gif", 0);
            publishSettings.writeElementValue("jpeg", 0);
            publishSettings.writeElementValue("png", 0);
            publishSettings.writeElementValue(greaterThanCC ? "svg" : "qt", 0);
            publishSettings.writeElementValue("rnwk", 0);
            publishSettings.writeElementValue("swc", 0);
            publishSettings.writeElementValue("flashDefaultName", 1);
            publishSettings.writeElementValue("projectorWinDefaultName", 1);
            publishSettings.writeElementValue("projectorMacDefaultName", 1);
            publishSettings.writeElementValue("htmlDefaultName", 1);
            publishSettings.writeElementValue("gifDefaultName", 1);
            publishSettings.writeElementValue("jpegDefaultName", 1);
            publishSettings.writeElementValue("pngDefaultName", 1);
            publishSettings.writeElementValue(greaterThanCC ? "svgDefaultName" : "qtDefaultName", 1);
            publishSettings.writeElementValue("rnwkDefaultName", 1);
            publishSettings.writeElementValue("swcDefaultName", 1);
            publishSettings.writeElementValue("flashFileName", baseName + ".swf");
            publishSettings.writeElementValue("projectorWinFileName", baseName + ".exe");
            publishSettings.writeElementValue("projectorMacFileName", baseName + ".app");
            publishSettings.writeElementValue("htmlFileName", baseName + ".html");
            publishSettings.writeElementValue("gifFileName", baseName + ".gif");
            publishSettings.writeElementValue("jpegFileName", baseName + ".jpg");
            publishSettings.writeElementValue("pngFileName", baseName + ".png");
            publishSettings.writeElementValue(greaterThanCC ? "svgFileName" : "qtFileName", 1);
            publishSettings.writeElementValue("rnwkFileName", baseName + ".smil");
            publishSettings.writeElementValue("swcFileName", baseName + ".swc");
            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishHtmlProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("VersionDetectionIfAvailable", 0);
            publishSettings.writeElementValue("VersionInfo", "12,0,0,0;11,2,0,0;11,1,0,0;10,3,0,0;10,2,153,0;10,1,52,0;9,0,124,0;8,0,24,0;7,0,14,0;6,0,79,0;5,0,58,0;4,0,32,0;3,0,8,0;2,0,1,12;1,0,0,1;");
            publishSettings.writeElementValue("UsingDefaultContentFilename", 1);
            publishSettings.writeElementValue("UsingDefaultAlternateFilename", 1);
            publishSettings.writeElementValue("ContentFilename", baseName + "_content.html");
            publishSettings.writeElementValue("AlternateFilename", baseName + "_alternate.html");
            publishSettings.writeElementValue("UsingOwnAlternateFile", 0);
            publishSettings.writeElementValue("OwnAlternateFilename", "");
            publishSettings.writeElementValue("Width", width);
            publishSettings.writeElementValue("Height", height);
            publishSettings.writeElementValue("Align", 0);
            publishSettings.writeElementValue("Units", 0);
            publishSettings.writeElementValue("Loop", 1);
            publishSettings.writeElementValue("StartPaused", 0);
            publishSettings.writeElementValue("Scale", 0);
            publishSettings.writeElementValue("HorizontalAlignment", 1);
            publishSettings.writeElementValue("VerticalAlignment", 1);
            publishSettings.writeElementValue("Quality", 4);
            publishSettings.writeElementValue("DeblockingFilter", 0);
            publishSettings.writeElementValue("WindowMode", 0);
            publishSettings.writeElementValue("DisplayMenu", 1);
            publishSettings.writeElementValue("DeviceFont", 0);
            publishSettings.writeElementValue("TemplateFileName", "");
            publishSettings.writeElementValue("showTagWarnMsg", 1);
            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishFlashProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("TopDown", "");
            publishSettings.writeElementValue("FireFox", "");
            publishSettings.writeElementValue("Report", 0);
            publishSettings.writeElementValue("Protect", 0);
            publishSettings.writeElementValue("OmitTraceActions", 0);
            publishSettings.writeElementValue("Quality", "80");
            publishSettings.writeElementValue("DeblockingFilter", 0);
            publishSettings.writeElementValue("StreamFormat", 0);
            publishSettings.writeElementValue("StreamCompress", 7);
            publishSettings.writeElementValue("EventFormat", 0);
            publishSettings.writeElementValue("EventCompress", 7);
            publishSettings.writeElementValue("OverrideSounds", 0);
            publishSettings.writeElementValue("Version", flaSwfVersion);
            publishSettings.writeElementValue("ExternalPlayer", FLAVersion.swfVersionToPlayer(flaSwfVersion));
            publishSettings.writeElementValue("ActionScriptVersion", useAS3 ? 3 : 2);
            publishSettings.writeElementValue("PackageExportFrame", 1);
            publishSettings.writeElementValue("PackagePaths", "");
            publishSettings.writeElementValue("AS3PackagePaths", ".");
            publishSettings.writeElementValue("AS3ConfigConst", "CONFIG::FLASH_AUTHORING=\"true\";");
            publishSettings.writeElementValue("DebuggingPermitted", 0);
            publishSettings.writeElementValue("DebuggingPassword", "");
            publishSettings.writeElementValue("CompressMovie", swf.compression == SWFCompression.NONE ? 0 : 1);
            publishSettings.writeElementValue("CompressionType", swf.compression == SWFCompression.LZMA ? 1 : 0);
            publishSettings.writeElementValue("InvisibleLayer", 1);
            publishSettings.writeElementValue("DeviceSound", 0);
            publishSettings.writeElementValue("StreamUse8kSampleRate", 0);
            publishSettings.writeElementValue("EventUse8kSampleRate", 0);
            publishSettings.writeElementValue("UseNetwork", useNetwork ? 1 : 0);
            publishSettings.writeElementValue("DocumentClass", characterClasses.containsKey(0) ? characterClasses.get(0) : "");
            publishSettings.writeElementValue("AS3Strict", 2);
            publishSettings.writeElementValue("AS3Coach", 4);
            publishSettings.writeElementValue("AS3AutoDeclare", 4096);
            publishSettings.writeElementValue("AS3Dialect", "AS3");
            publishSettings.writeElementValue("AS3ExportFrame", 1);
            publishSettings.writeElementValue("AS3Optimize", 1);
            publishSettings.writeElementValue("ExportSwc", 0);
            publishSettings.writeElementValue("ScriptStuckDelay", 15);
            publishSettings.writeElementValue("IncludeXMP", 1);
            publishSettings.writeElementValue("HardwareAcceleration", 0);
            publishSettings.writeElementValue("AS3Flags", 4102);
            publishSettings.writeElementValue("DefaultLibraryLinkage", "rsl");
            publishSettings.writeElementValue("RSLPreloaderMethod", "wrap");
            publishSettings.writeElementValue("RSLPreloaderSWF", "$(AppConfig)/ActionScript 3.0/rsls/loader_animation.swf");
            if (greaterThanCC) {
                publishSettings.writeStartElement("LibraryPath");
                publishSettings.writeStartElement("library-path-entry");
                publishSettings.writeElementValue("swc-path", "$(AppConfig)/ActionScript 3.0/libs");
                publishSettings.writeElementValue("linkage", "merge");
                publishSettings.writeEndElement();
                publishSettings.writeStartElement("library-path-entry");
                publishSettings.writeElementValue("swc-path", "$(FlexSDK)/frameworks/libs/flex.swc");
                publishSettings.writeElementValue("linkage", "merge");
                publishSettings.writeElementValue("rsl-url", "textLayout_2.0.0.232.swz");
                publishSettings.writeEndElement();
                publishSettings.writeStartElement("library-path-entry");
                publishSettings.writeElementValue("swc-path", "$(FlexSDK)/frameworks/libs/core.swc");
                publishSettings.writeElementValue("linkage", "merge");
                publishSettings.writeElementValue("rsl-url", "textLayout_2.0.0.232.swz");
                publishSettings.writeEndElement();
                publishSettings.writeEndElement();
                publishSettings.writeElementValueRaw("LibraryVersions", Helper.newLine + "      "); // todo: is this really needed or an empty tag is ok?
            } else {
                publishSettings.writeStartElement("LibraryPath");
                publishSettings.writeStartElement("library-path-entry");
                publishSettings.writeElementValue("swc-path", "$(AppConfig)/ActionScript 3.0/libs");
                publishSettings.writeElementValue("linkage", "merge");
                publishSettings.writeEndElement();
                publishSettings.writeStartElement("library-path-entry");
                publishSettings.writeElementValue("swc-path", "$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc");
                publishSettings.writeElementValue("linkage", "rsl", new String[]{"usesDefault", "true"});
                publishSettings.writeElementValue("rsl-url", "http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz");
                publishSettings.writeElementValue("policy-file-url", "http://fpdownload.adobe.com/pub/swz/crossdomain.xml");
                publishSettings.writeElementValue("rsl-url", "textLayout_2.0.0.232.swz");
                publishSettings.writeEndElement();
                publishSettings.writeEndElement();

                publishSettings.writeStartElement("LibraryVersions");
                publishSettings.writeStartElement("library-version");
                publishSettings.writeElementValue("swc-path", "$(AppConfig)/ActionScript 3.0/libs/11.0/textLayout.swc");
                publishSettings.writeEmptyElement("feature", new String[]{"name", "tlfText", "majorVersion", "2", "minorVersion", "0", "build", "232"});
                publishSettings.writeElementValue("rsl-url", "http://fpdownload.adobe.com/pub/swz/tlf/2.0.0.232/textLayout_2.0.0.232.swz");
                publishSettings.writeElementValue("policy-file-url", "http://fpdownload.adobe.com/pub/swz/crossdomain.xml");
                publishSettings.writeElementValue("rsl-url", "textLayout_2.0.0.232.swz");
                publishSettings.writeEndElement();
                publishSettings.writeEndElement();
            }

            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishJpegProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("Width", width);
            publishSettings.writeElementValue("Height", height);
            publishSettings.writeElementValue("Progressive", 0);
            publishSettings.writeElementValue("DPI", 4718592);
            publishSettings.writeElementValue("Size", 0);
            publishSettings.writeElementValue("Quality", 80);
            publishSettings.writeElementValue("MatchMovieDim", 1);
            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishRNWKProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("exportFlash", 1);
            publishSettings.writeElementValue("flashBitRate", 0);
            publishSettings.writeElementValue("exportAudio", 1);
            publishSettings.writeElementValue("audioFormat", 0);
            publishSettings.writeElementValue("singleRateAudio", 0);
            publishSettings.writeElementValue("realVideoRate", 100000);
            publishSettings.writeElementValue("speed28K", 1);
            publishSettings.writeElementValue("speed56K", 1);
            publishSettings.writeElementValue("speedSingleISDN", 0);
            publishSettings.writeElementValue("speedDualISDN", 0);
            publishSettings.writeElementValue("speedCorporateLAN", 0);
            publishSettings.writeElementValue("speed256K", 0);
            publishSettings.writeElementValue("speed384K", 0);
            publishSettings.writeElementValue("speed512K", 0);
            publishSettings.writeElementValue("exportSMIL", 1);
            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishGifProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("Width", width);
            publishSettings.writeElementValue("Height", height);
            publishSettings.writeElementValue("Animated", 0);
            publishSettings.writeElementValue("MatchMovieDim", 1);
            publishSettings.writeElementValue("Loop", 1);
            publishSettings.writeElementValue("LoopCount", "");
            publishSettings.writeElementValue("OptimizeColors", 1);
            publishSettings.writeElementValue("Interlace", 0);
            publishSettings.writeElementValue("Smooth", 1);
            publishSettings.writeElementValue("DitherSolids", 0);
            publishSettings.writeElementValue("RemoveGradients", 0);
            publishSettings.writeElementValue("TransparentOption", "");
            publishSettings.writeElementValue("TransparentAlpha", 128);
            publishSettings.writeElementValue("DitherOption", "");
            publishSettings.writeElementValue("PaletteOption", "");
            publishSettings.writeElementValue("MaxColors", 255);
            publishSettings.writeElementValue("PaletteName", "");
            publishSettings.writeEndElement();

            publishSettings.writeStartElement("PublishPNGProperties", new String[]{"enabled", "true"});
            publishSettings.writeElementValue("Width", width);
            publishSettings.writeElementValue("Height", height);
            publishSettings.writeElementValue("OptimizeColors", 1);
            publishSettings.writeElementValue("Interlace", 0);
            publishSettings.writeElementValue("Transparent", 0);
            publishSettings.writeElementValue("Smooth", 1);
            publishSettings.writeElementValue("DitherSolids", 0);
            publishSettings.writeElementValue("RemoveGradients", 0);
            publishSettings.writeElementValue("MatchMovieDim", 1);
            publishSettings.writeElementValue("DitherOption", "");
            publishSettings.writeElementValue("FilterOption", "");
            publishSettings.writeElementValue("PaletteOption", "");
            publishSettings.writeElementValue("BitDepth", "24-bit with Alpha");
            publishSettings.writeElementValue("MaxColors", 255);
            publishSettings.writeElementValue("PaletteName", "");
            publishSettings.writeEndElement();

            if (!greaterThanCC) {
                publishSettings.writeStartElement("PublishQTProperties", new String[]{"enabled", "true"});
                publishSettings.writeElementValue("Width", width);
                publishSettings.writeElementValue("Height", height);
                publishSettings.writeElementValue("MatchMovieDim", 1);
                publishSettings.writeElementValue("UseQTSoundCompression", 0);
                publishSettings.writeElementValue("AlphaOption", "");
                publishSettings.writeElementValue("LayerOption", "");
                publishSettings.writeElementValue("QTSndSettings", "00000000");
                publishSettings.writeElementValue("ControllerOption", 0);
                publishSettings.writeElementValue("Looping", 0);
                publishSettings.writeElementValue("PausedAtStart", 0);
                publishSettings.writeElementValue("PlayEveryFrame", 0);
                publishSettings.writeElementValue("Flatten", 1);
                publishSettings.writeEndElement();
            }

            publishSettings.writeEndElement();
            publishSettings.writeEndElement();
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        String publishSettingsStr = publishSettings.toString();

        if (settings.compressed) {
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
        if (useAS3 && settings.exportScript) {
            try {
                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false);
                swf.exportActionScript(handler, outDir.getAbsolutePath(), scriptExportSettings, parallel, null);
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

    private static void convertAdjustColorFilter(COLORMATRIXFILTER filter, XFLXmlWriter writer) throws XMLStreamException {
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

        writer.writeEmptyElement("AdjustColorFilter", new String[]{
            "brightness", Integer.toString(normBrightness(b)),
            "contrast", Integer.toString(normContrast(c)),
            "saturation", Integer.toString(normSaturation(s)),
            "hue", Integer.toString(normHue(h)),});
    }

    private static String convertHTMLText(ReadOnlyTagList tags, DefineEditTextTag det, String html) {
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
        return tparser.result.toString();
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static class HTMLTextParser extends DefaultHandler {

        public XFLXmlWriter result = new XFLXmlWriter();

        private String fontFace = "";

        private String color = "";

        private int size = -1;

        private int indent = -1;

        private int leftMargin = -1;

        private int rightMargin = -1;

        private int lineSpacing = -1;

        private double letterSpacing = -1;

        private String alignment = null;

        private final ReadOnlyTagList tags;

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

        public HTMLTextParser(ReadOnlyTagList tags, DefineEditTextTag det) {
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
                        fontName = FontTag.getDefaultFontName();
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
                        try {
                            letterSpacing = Double.parseDouble(ls);
                        } catch (NumberFormatException ex) {
                            logger.log(Level.WARNING, "Invalid letter spacing value: {0}", ls);
                        }
                    }
                    String s = attributes.getValue("size");
                    if (s != null) {
                        try {
                            size = Integer.parseInt(s);
                        } catch (NumberFormatException ex) {
                            logger.log(Level.WARNING, "Invalid font size: {0}", s);
                        }
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
            try {
                result.writeStartElement("DOMTextRun");
                result.writeElementValue("characters", txt);
                result.writeStartElement("textAttrs");
                result.writeStartElement("DOMTextAttrs");
                if (alignment != null) {
                    result.writeAttribute("alignment", alignment);
                }
                result.writeAttribute("rotation", true);
                if (indent > -1) {
                    result.writeAttribute("indent", twipToPixel(indent));
                }
                if (leftMargin > -1) {
                    result.writeAttribute("leftMargin", twipToPixel(leftMargin));
                }
                if (letterSpacing > -1) {
                    result.writeAttribute("letterSpacing", letterSpacing);
                }
                if (lineSpacing > -1) {
                    result.writeAttribute("lineSpacing", twipToPixel(lineSpacing));
                }
                if (rightMargin > -1) {
                    result.writeAttribute("rightMargin", twipToPixel(rightMargin));
                }
                if (size > -1) {
                    result.writeAttribute("size", size);
                    result.writeAttribute("bitmapSize", (int) (size * SWF.unitDivisor));
                }
                if (fontFace != null) {
                    result.writeAttribute("face", fontFace);
                }
                if (color != null) {
                    result.writeAttribute("fillColor", color);
                }
                if (url != null) {
                    result.writeAttribute("url", url);
                }
                if (target != null) {
                    result.writeAttribute("target", target);
                }
                result.writeEndElement();
                result.writeEndElement();
                result.writeEndElement();
            } catch (XMLStreamException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
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
