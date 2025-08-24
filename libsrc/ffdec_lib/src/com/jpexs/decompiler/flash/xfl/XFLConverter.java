/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.FlashPlayerVersion;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.model.CallPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.GeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.LeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.NeqAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.action.ActionTreeOperation;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
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
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.CSMSettingsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
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
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
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
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.tags.font.CharacterRanges;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
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
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.filters.BEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER;
import com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor;
import com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.decompiler.flash.types.filters.GLOWFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER;
import com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.flash.xfl.shapefixer.CurvedEdgeRecordAdvanced;
import com.jpexs.decompiler.flash.xfl.shapefixer.MorphShapeFixer;
import com.jpexs.decompiler.flash.xfl.shapefixer.ShapeFixer;
import com.jpexs.decompiler.flash.xfl.shapefixer.ShapeRecordAdvanced;
import com.jpexs.decompiler.flash.xfl.shapefixer.StraightEdgeRecordAdvanced;
import com.jpexs.decompiler.flash.xfl.shapefixer.StyleChangeRecordAdvanced;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.flash.fla.converter.FlaConverter;
import com.jpexs.flash.fla.converter.FlaFormatVersion;
import com.jpexs.flash.fla.converter.streams.CfbOutputStorage;
import com.jpexs.flash.fla.converter.streams.InputStorageInterface;
import com.jpexs.flash.fla.converter.streams.OutputStorageInterface;
import com.jpexs.flash.fla.converter.streams.ZippedInputStorage;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Path;
import com.jpexs.helpers.ProgressListener;
import com.jpexs.helpers.Reference;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.XmlPrettyFormat;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * FLA / XFL converter of SWF files.
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

    /**
     * Adds "(depth xxx)" to layer name
     */
    private final boolean DEBUG_EXPORT_LAYER_DEPTHS = false;

    private static final DecimalFormat EDGE_DECIMAL_FORMAT = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    static {
        EDGE_DECIMAL_FORMAT.setGroupingUsed(false);
    }

    private static class StatusStack {

        private final Stack<String> statuses = new Stack<>();
        private final ProgressListener progressListener;

        public StatusStack(ProgressListener progressListener) {
            this.progressListener = progressListener;
        }

        public String getStatusString() {
            return String.join(", ", statuses);
        }

        private void update() {
            if (progressListener == null) {
                return;
            }

            progressListener.status(getStatusString());
        }

        public void pushStatus(String status) {
            statuses.push(status);
            update();
        }

        public void popStatus() {
            statuses.pop();
            update();
        }
    }

    private static String formatEdgeDouble(double value) {
        if (value % 1 == 0) {
            return "" + (int) value;
        }
        value = Math.round(value * 2.0) / 2.0;
        return EDGE_DECIMAL_FORMAT.format(value);
    }

    private static String convertShapeEdge(MATRIX mat, ShapeRecordAdvanced record, double x, double y) {
        if (record instanceof StyleChangeRecordAdvanced) {
            StyleChangeRecordAdvanced scr = (StyleChangeRecordAdvanced) record;
            Point2D p = new Point2D.Double(scr.moveDeltaX, scr.moveDeltaY);
            p = new Matrix(mat).transform(p);
            if (scr.stateMoveTo) {
                return "! " + formatEdgeDouble(p.getX()) + " " + formatEdgeDouble(p.getY());
            }
        } else if (record instanceof StraightEdgeRecordAdvanced) {
            StraightEdgeRecordAdvanced ser = (StraightEdgeRecordAdvanced) record;
            Point2D p = new Point2D.Double(x + ser.deltaX, y + ser.deltaY);
            p = new Matrix(mat).transform(p);

            return "| " + formatEdgeDouble(p.getX()) + " " + formatEdgeDouble(p.getY());
        } else if (record instanceof CurvedEdgeRecordAdvanced) {
            CurvedEdgeRecordAdvanced cer = (CurvedEdgeRecordAdvanced) record;
            double controlX = cer.controlDeltaX + x;
            double controlY = cer.controlDeltaY + y;
            double anchorX = cer.anchorDeltaX + controlX;
            double anchorY = cer.anchorDeltaY + controlY;
            Point2D control = new Point2D.Double(controlX, controlY);
            Point2D anchor = new Point.Double(anchorX, anchorY);
            control = new Matrix(mat).transform(control);
            anchor = new Matrix(mat).transform(anchor);
            return "[ " + formatEdgeDouble(control.getX()) + " " + formatEdgeDouble(control.getY()) + " " + formatEdgeDouble(anchor.getX()) + " " + formatEdgeDouble(anchor.getY());
        }
        return "";
    }

    private static void convertShapeEdges(boolean close, double startX, double startY, MATRIX mat, List<ShapeRecordAdvanced> recordsAdvanced, StringBuilder ret) {

        double x = startX;
        double y = startY;
        boolean hasMove = false;
        if (!recordsAdvanced.isEmpty()) {
            if (recordsAdvanced.get(0) instanceof StyleChangeRecordAdvanced) {
                StyleChangeRecordAdvanced scr = (StyleChangeRecordAdvanced) recordsAdvanced.get(0);
                if (scr.stateMoveTo) {
                    hasMove = true;
                }
            }
        }
        if (!hasMove) {
            ret.append("! ").append(formatEdgeDouble(startX)).append(" ").append(formatEdgeDouble(startY));
        }
        double lastMoveToX = startX;
        double lastMoveToY = startY;

        int fillStyle0 = 0;
        int fillStyle1 = 0;
        int lineStyle = 0;
        List<String> edges = new ArrayList<>();
        for (ShapeRecordAdvanced rec : recordsAdvanced) {
            if (rec instanceof StyleChangeRecordAdvanced) {
                StyleChangeRecordAdvanced scr = (StyleChangeRecordAdvanced) rec;
                if (scr.stateMoveTo) {
                    lastMoveToX = scr.moveDeltaX;
                    lastMoveToY = scr.moveDeltaY;
                }
                if (scr.stateNewStyles) {
                    fillStyle0 = 0;
                    fillStyle1 = 0;
                    lineStyle = 0;
                }
                if (scr.stateLineStyle) {
                    lineStyle = scr.lineStyle;
                }
                if (scr.stateFillStyle0) {
                    fillStyle0 = scr.fillStyle0;
                }
                if (scr.stateFillStyle1) {
                    fillStyle1 = scr.fillStyle1;
                }
            }

            String edge = convertShapeEdge(mat, rec, x, y);

            String curPos = "! " + formatEdgeDouble(x) + " " + formatEdgeDouble(y);
            //ignore duplicated edges with only strokes #2031
            if (fillStyle0 == 0 && fillStyle1 == 0 && lineStyle != 0) {
                if (!edges.contains(curPos + edge)) {
                    edges.add(curPos + edge);
                    ret.append(edge);
                }
            } else {
                edges.add(curPos + edge);
                ret.append(edge);
            }

            x = rec.changeX(x);
            y = rec.changeY(y);
        }
        //hack for morphshapes. TODO: make this better
        if (close && (Double.compare(lastMoveToX, x) != 0 || Double.compare(lastMoveToY, y) != 0)) {
            StraightEdgeRecordAdvanced ser = new StraightEdgeRecordAdvanced(lastMoveToX - x, lastMoveToY - y);
            ret.append(convertShapeEdge(mat, ser, x, y));
        }
    }

    private static String getScaleMode(ILINESTYLE lineStyle) {
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

    private static void convertLineStyle1(LINESTYLE ls, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("SolidStroke", new String[]{
            "scaleMode", getScaleMode(ls),
            "weight", Double.toString(((float) ls.getWidth()) / SWF.unitDivisor)});

        writer.writeStartElement("fill");
        writer.writeStartElement("SolidColor", new String[]{"color", ls.getColor().toHexRGB()});
        if (shapeNum >= 3) {
            writer.writeAttribute("alpha", ((RGBA) ls.getColor()).getAlphaFloat());
        }

        writer.writeEndElement(); //SolidColor

        writer.writeEndElement(); //fill
        writer.writeEndElement(); //SolidStroke
    }

    private static void convertLineStyle2(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, MATRIX mat, LINESTYLE2 ls, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
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
            convertFillStyle(lastImportedId, characterNameMap, swf, mat, ls.fillType, shapeNum, writer);
        }

        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static void convertFillStyle(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, MATRIX mat, FILLSTYLE fs, int shapeNum, XFLXmlWriter writer) throws XMLStreamException {
        if (mat == null) {
            mat = new MATRIX();
        }
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
                CharacterTag bitmapCh = swf.getCharacter(fs.bitmapId);
                if (!(bitmapCh instanceof ImageTag)) {
                    if (bitmapCh != null) {
                        logger.log(Level.SEVERE, "Suspicious bitmapfill:{0}", bitmapCh.getClass().getSimpleName());
                    }
                    writer.writeEmptyElement("SolidColor", new String[]{"color", "#ffffff"});
                    return;
                }

                ImageTag it = (ImageTag) bitmapCh;
                writer.writeStartElement("BitmapFill");
                writer.writeAttribute("bitmapPath", getSymbolName(lastImportedId, characterNameMap, swf, bitmapCh, "Bitmap") + it.getImageFormat().getExtension());

                if ((fs.fillStyleType == FILLSTYLE.CLIPPED_BITMAP) || (fs.fillStyleType == FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP)) {
                    writer.writeAttribute("bitmapIsClipped", true);
                }

                writer.writeStartElement("matrix");
                MATRIX bitmapMatrix = fs.bitmapMatrix;
                bitmapMatrix = (new Matrix(mat)).concatenate(new Matrix(bitmapMatrix)).toMATRIX();
                convertMatrix(bitmapMatrix, writer);
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

                MATRIX gradientMatrix = fs.gradientMatrix;
                gradientMatrix = (new Matrix(mat)).concatenate(new Matrix(gradientMatrix)).toMATRIX();

                convertMatrix(gradientMatrix, writer);
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

    private static boolean shapeHasMultiLayers(List<SHAPERECORD> shapeRecords) throws XMLStreamException {
        for (SHAPERECORD rec : shapeRecords) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateNewStyles) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void convertShape(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape, boolean useLayers, XFLXmlWriter writer) throws XMLStreamException {
        List<String> layers = getShapeLayers(lastImportedId, characterNameMap, swf, mat, shapeNum, shapeRecords, fillStyles, lineStyles, morphshape);
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

    private static List<String> getShapeLayers(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, MATRIX mat, int shapeNum, List<SHAPERECORD> shapeRecords, FILLSTYLEARRAY fillStyles, LINESTYLEARRAY lineStyles, boolean morphshape) throws XMLStreamException {
        if (mat == null) {
            mat = new MATRIX();
        }

        List<ShapeRecordAdvanced> shapeRecordsAdvanced;

        ShapeFixer fixer = morphshape ? new MorphShapeFixer() : new ShapeFixer();
        shapeRecordsAdvanced = fixer.fix(shapeRecords, shapeNum, fillStyles, lineStyles);

        List<ShapeRecordAdvanced> edges = new ArrayList<>();
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
                convertFillStyle(lastImportedId, characterNameMap, swf, mat, fs, shapeNum, fillsStr);
                fillsStr.writeEndElement();
                fillStyleCount++;
            }
        }
        if (lineStyles != null) {
            if (shapeNum <= 3 && lineStyles.lineStyles != null) {
                for (int l = 0; l < lineStyles.lineStyles.length; l++) {
                    strokesStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                    convertLineStyle1(lineStyles.lineStyles[l], shapeNum, strokesStr);
                    strokesStr.writeEndElement();
                    lineStyleCount++;
                }
            } else if (lineStyles.lineStyles2 != null) {
                for (int l = 0; l < lineStyles.lineStyles2.length; l++) {
                    strokesStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                    convertLineStyle2(lastImportedId, characterNameMap, swf, mat, (LINESTYLE2) lineStyles.lineStyles2[l], shapeNum, strokesStr);
                    strokesStr.writeEndElement();
                    lineStyleCount++;
                }
            }
        }

        fillsStr.writeEndElement();
        strokesStr.writeEndElement();

        boolean hasEdge = false;
        XFLXmlWriter currentLayer = new XFLXmlWriter();
        if (fillStyleCount > 0 || lineStyleCount > 0) {
            currentLayer.writeStartElement("DOMShape", new String[]{"isFloating", "true"});
            currentLayer.writeCharactersRaw(fillsStr.toString());
            currentLayer.writeCharactersRaw(strokesStr.toString());
            currentLayer.writeStartElement("edges");
        }

        double x = 0;
        double y = 0;
        double startEdgeX = 0;
        double startEdgeY = 0;

        fillStyleCount = fillStyles == null ? 0 : fillStyles.fillStyles.length;
        for (ShapeRecordAdvanced edge : shapeRecordsAdvanced) {
            if (edge instanceof StyleChangeRecordAdvanced) {
                StyleChangeRecordAdvanced scr = (StyleChangeRecordAdvanced) edge;
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
                            convertShapeEdges(((fillStyle0 > 0 || fillStyle1 > 0) && morphshape), startEdgeX, startEdgeY, mat, edges, edgesSb);
                            currentLayer.writeAttribute("edges", edgesSb.toString());
                            currentLayer.writeEndElement();
                            hasEdge = true;
                            edges.clear();
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
                        convertFillStyle(lastImportedId, characterNameMap, swf, mat, scr.fillStyles.fillStyles[f], shapeNum, fillsNewStr);
                        fillsNewStr.writeEndElement();
                        fillStyleCount++;
                    }

                    lineStyleCount = 0;
                    if (shapeNum <= 3) {
                        for (int l = 0; l < scr.lineStyles.lineStyles.length; l++) {
                            strokesNewStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                            convertLineStyle1(scr.lineStyles.lineStyles[l], shapeNum, strokesNewStr);
                            strokesNewStr.writeEndElement();
                            lineStyleCount++;
                        }
                    } else {
                        for (int l = 0; l < scr.lineStyles.lineStyles2.length; l++) {
                            strokesNewStr.writeStartElement("StrokeStyle", new String[]{"index", Integer.toString(lineStyleCount + 1)});
                            convertLineStyle2(lastImportedId, characterNameMap, swf, mat, (LINESTYLE2) scr.lineStyles.lineStyles2[l], shapeNum, strokesNewStr);
                            strokesNewStr.writeEndElement();
                            lineStyleCount++;
                        }
                    }
                    fillsNewStr.writeEndElement(); // fills
                    strokesNewStr.writeEndElement(); // strokes
                    currentLayer.writeCharactersRaw(fillsNewStr.toString());
                    currentLayer.writeCharactersRaw(strokesNewStr.toString());
                    currentLayer.writeStartElement("edges");
                }
                if (scr.stateFillStyle0) {
                    int fillStyle0_new = scr.fillStyle0;
                    if (morphshape) { //???
                        fillStyle1 = fillStyle0_new;
                    } else {
                        fillStyle0 = fillStyle0_new;
                    }
                }
                if (scr.stateFillStyle1) {
                    int fillStyle1_new = scr.fillStyle1;
                    if (morphshape) {
                        fillStyle0 = fillStyle1_new;
                    } else {
                        fillStyle1 = fillStyle1_new;
                    }
                }
                if (scr.stateLineStyle) {
                    strokeStyle = scr.lineStyle;
                }
                if (!edges.isEmpty()) {
                    if ((fillStyle0 > 0) || (fillStyle1 > 0) || (strokeStyle > 0)) {
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
                        convertShapeEdges(((lastFillStyle0 > 0 || lastFillStyle1 > 0) && morphshape), startEdgeX, startEdgeY, mat, edges, edgesSb);
                        currentLayer.writeAttribute("edges", edgesSb.toString());
                        currentLayer.writeEndElement();
                        hasEdge = true;

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
                convertShapeEdges(((fillStyle0 > 0 || fillStyle1 > 0) && morphshape), startEdgeX, startEdgeY, mat, edges, edgesSb);
                currentLayer.writeAttribute("edges", edgesSb.toString());
                currentLayer.writeEndElement();
                hasEdge = true;
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

    private static int getMaxDepth(ReadOnlyTagList tags) {
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

    private static void walkShapeUsages(ReadOnlyTagList timeLineTags, Map<CharacterTag, Integer> usages) {
        Map<Integer, Integer> depthMap = new HashMap<>();
        Map<Integer, Boolean> depthHasInstanceName = new HashMap<>();
        Map<Integer, Boolean> depthHasColorTransform = new HashMap<>();
        Map<Integer, Boolean> depthCacheAsBitmap = new HashMap<>();
        Map<Integer, Boolean> depthHasNonemptyMatrix = new HashMap<>();
        for (Tag t : timeLineTags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                walkShapeUsages(sprite.getTags(), usages);
            }
            if (t instanceof RemoveTag) {
                int d = ((RemoveTag) t).getDepth();
                depthMap.remove(d);
                depthHasInstanceName.remove(d);
                depthHasColorTransform.remove(d);
                depthCacheAsBitmap.remove(d);
                depthHasNonemptyMatrix.remove(d);
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int d = po.getDepth();

                if (po.flagMove() == depthMap.containsKey(d)) {
                    if (!po.flagMove()) {
                        depthHasInstanceName.put(d, false);
                        depthHasColorTransform.put(d, false);
                        depthCacheAsBitmap.put(d, false);
                        depthHasNonemptyMatrix.put(d, false);
                    }

                    if (po.getInstanceName() != null) {
                        depthHasInstanceName.put(d, true);
                    } else if (po.getColorTransform() != null) {
                        depthHasColorTransform.put(d, true);
                    } else if (po.cacheAsBitmap()) {
                        depthCacheAsBitmap.put(d, true);
                    } else if (po.getMatrix() != null && !po.getMatrix().isEmpty()) {
                        depthHasNonemptyMatrix.put(d, true);
                    }

                    int ch = po.getCharacterId();
                    if (ch == -1) {
                        if (depthMap.containsKey(d)) {
                            ch = depthMap.get(d);
                        }
                    } else {
                        depthMap.put(d, ch);
                    }
                    if (ch == -1) {
                        continue;
                    }

                    CharacterTag chObj = po.getSwf().getCharacter(ch);
                    if (!usages.containsKey(chObj)) {
                        usages.put(chObj, 0);
                    }
                    int usageCount = usages.get(chObj);
                    usageCount++;
                    if (depthHasInstanceName.get(d)) {
                        usageCount++;
                    } else if (depthHasColorTransform.get(d)) {
                        usageCount++;
                    } else if (depthCacheAsBitmap.get(d)) {
                        usageCount++;
                    } else if (depthHasNonemptyMatrix.get(d)) {
                        usageCount++;
                    }
                    usages.put(chObj, usageCount);
                }
            }
        }
    }

    private static void walkMorphShapeUsages(ReadOnlyTagList timeLineTags, Map<Integer, Integer> usages) {
        Map<Integer, Integer> depthMap = new HashMap<>();
        for (Tag t : timeLineTags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                walkMorphShapeUsages(sprite.getTags(), usages);
            }
            if (t instanceof RemoveTag) {
                depthMap.remove(((RemoveTag) t).getDepth());
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int d = po.getDepth();
                if (po.flagMove() == depthMap.containsKey(d)) {
                    int ch = po.getCharacterId();
                    if (ch == -1) {
                        if (depthMap.containsKey(d)) {
                            ch = depthMap.get(d);
                        }
                    } else {
                        depthMap.put(d, ch);
                    }
                    if (ch == -1) {
                        continue;
                    }

                    CharacterTag ct = po.getSwf().getCharacter(ch);
                    if (ct instanceof MorphShapeTag) {
                        if (!usages.containsKey(ch)) {
                            usages.put(ch, 0);
                        }
                        int usageCount = usages.get(ch);
                        if (po.getRatio() <= 0) {
                            usageCount++;
                            usages.put(ch, usageCount);
                        }
                    }
                }
            }
        }
    }

    private static List<Integer> getMultiUsageMorphShapes(ReadOnlyTagList tags) {
        List<Integer> ret = new ArrayList<>();
        Map<Integer, Integer> usages = new TreeMap<>();
        walkMorphShapeUsages(tags, usages);
        for (int ch : usages.keySet()) {
            if (usages.get(ch) > 1) {
                ret.add(ch);
            }
        }
        return ret;
    }

    private static List<CharacterTag> getNonLibraryShapes(ReadOnlyTagList tags) {
        Map<CharacterTag, Integer> usages = new IdentityHashMap<>();
        walkShapeUsages(tags, usages);
        List<CharacterTag> ret = new ArrayList<>();
        try {
            for (CharacterTag ch : usages.keySet()) {
                if (usages.get(ch) < 2) {
                    if (ch instanceof ShapeTag) {
                        ShapeTag shp = (ShapeTag) ch;
                        if (!shapeHasMultiLayers(shp.getShapes().shapeRecords)) {
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

    private static Set<CharacterTag> getCharactersAndAllDependent(SWF swf) {
        Set<CharacterTag> ret = new LinkedIdentityHashSet<>();

        Set<CharacterTag> charsInThisSwf = new LinkedIdentityHashSet<>();

        charsInThisSwf.addAll(swf.getCharacters(true).values());

        ret.addAll(charsInThisSwf);

        for (CharacterTag ct : charsInThisSwf) {
            walkNeededCharacters(ret, ct);
        }

        walkNeededClasses(ret, swf.getTags());
        return ret;
    }

    private static void walkNeededClasses(Set<CharacterTag> ret, ReadOnlyTagList tags) {
        for (Tag t : tags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                walkNeededClasses(ret, sprite.getTags());
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                String cls = pt.getClassName();
                if (cls != null) {
                    CharacterTag ct = pt.getSwf().getCharacterByClass(cls);
                    if (ct != null) {
                        if (!ret.contains(ct)) {
                            ret.add(ct);
                            walkNeededCharacters(ret, ct);
                        }
                    }
                }
            }
        }
    }

    private static void walkNeededCharacters(Set<CharacterTag> result, CharacterTag ct) {
        Set<Integer> needed = new HashSet<>();
        ct.getNeededCharactersDeep(needed);
        for (int n : needed) {
            CharacterTag nc = ct.getSwf().getCharacter(n);
            if (result.contains(nc)) {
                continue;
            }
            result.add(nc);
            walkNeededCharacters(result, nc);
            if (nc instanceof DefineSpriteTag) {
                DefineSpriteTag sp = (DefineSpriteTag) nc;
                walkNeededClasses(result, sp.getTags());
            }
        }
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

    private static String getSymbolName(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, CharacterTag tag) {
        return getSymbolName(lastImportedId, characterNameMap, swf, tag, "Symbol");
    }

    private static String getSymbolName(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, CharacterTag tag, String kind) {
        if (tag == null) {
            return "null";
        }
        if (characterNameMap.containsKey(tag)) {
            return characterNameMap.get(tag);
        }
        int characterId = swf.getCharacterId(tag);
        if (characterId == -1) {
            lastImportedId.setVal(lastImportedId.getVal() + 1);
            characterNameMap.put(tag, "imported/" + kind + " " + lastImportedId.getVal());
        } else {
            characterNameMap.put(tag, kind + " " + characterId);
        }
        return characterNameMap.get(tag);
    }

    private String getMaskedSymbolName(int symbolId) { //FIXME: Does this work with importassets???
        return (DEBUG_EXPORT_LAYER_DEPTHS ? "MaskedSymbol " : "Symbol ") + symbolId;
    }

    private static void convertSymbolInstance(int frame, AccessibilityBag accessibility, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, String name, MATRIX matrix, ColorTransform colorTransform, boolean cacheAsBitmap, int blendMode, List<FILTER> filters, boolean isVisible, RGBA backgroundColor, CLIPACTIONS clipActions, Amf3Value metadata, CharacterTag tag, FLAVersion flaVersion, XFLXmlWriter writer) throws XMLStreamException {
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

        writer.writeStartElement("DOMSymbolInstance", new String[]{"libraryItemName", getSymbolName(lastImportedId, characterNameMap, swf, tag)});
        if (name != null) {
            writer.writeAttribute("name", name);
            Map<String, String> accessibilityMap = accessibility.getAttributes(name, frame + 1);
            if (!accessibilityMap.isEmpty()) {
                writer.writeAttribute("hasAccessibleData", "true");
                for (String acKey : accessibilityMap.keySet()) {
                    writer.writeAttribute(acKey, accessibilityMap.get(acKey));
                }
            }
        }
        String blendModeStr = null;
        if (blendMode < BLENDMODES.length) {
            blendModeStr = BLENDMODES[blendMode];
        }
        if (blendModeStr != null) {
            writer.writeAttribute("blendMode", blendModeStr);
        }
        if (tag instanceof MorphShapeTag) { //multiple usage instance
            writer.writeAttribute("symbolType", "graphic");
            writer.writeAttribute("loop", "loop");
        } else if (tag instanceof ShapeTag) {
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
        if (backgroundColor != null) {
            writer.writeAttribute("bits32", false);
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
                        //missing t attribute = string (maybe "s"?)
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

    private static String convertActionScript12(ASMSource as, List<ActionTreeOperation> treeOperations) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        try {
            as.getActionScriptSource(writer, null, treeOperations);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        writer.finishHilights();
        return writer.toString();
    }

    private static String convertActionScript12(ASMSource as) {
        return convertActionScript12(as, new ArrayList<>());
    }

    private static long getTimestamp(SWF swf) {
        Date date = swf.getFileModificationDate();
        return date.getTime() / 1000;
    }

    private void convertLibrary(Reference<Integer> lastItemIdNumber, Set<CharacterTag> charactersExportedInFirstFrame, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, Map<CharacterTag, String> characterVariables, Map<CharacterTag, String> characterClasses, Map<CharacterTag, ScriptPack> characterScriptPacks, List<CharacterTag> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, FLAVersion flaVersion, XFLXmlWriter writer, Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol, List<Integer> multiUsageMorphShapes, StatusStack statusStack) throws XMLStreamException {
        statusStack.pushStatus("media");
        convertMedia(lastItemIdNumber, charactersExportedInFirstFrame, lastImportedId, characterNameMap, characterImportLinkageURL, characters, swf, characterVariables, characterClasses, tags, files, datfiles, writer, statusStack);
        statusStack.popStatus();
        statusStack.pushStatus("symbols");
        convertSymbols(lastItemIdNumber, charactersExportedInFirstFrame, characterImportLinkageURL, characters, lastImportedId, characterNameMap, swf, characterVariables, characterClasses, characterScriptPacks, nonLibraryShapes, backgroundColor, tags, files, flaVersion, writer, placeToMaskedSymbol, multiUsageMorphShapes, statusStack);
        statusStack.popStatus();
    }

    private void convertSymbols(Reference<Integer> lastItemIdNumber, Set<CharacterTag> charactersExportedInFirstFrame, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, Map<CharacterTag, String> characterVariables, Map<CharacterTag, String> characterClasses, Map<CharacterTag, ScriptPack> characterScriptPacks, List<CharacterTag> nonLibraryShapes, String backgroundColor, ReadOnlyTagList tags, HashMap<String, byte[]> files, FLAVersion flaVersion, XFLXmlWriter writer, Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol, List<Integer> multiUsageMorphShapes, StatusStack statusStack) throws XMLStreamException {
        //boolean hasSymbol = false;
        Reference<Integer> nextClipId = new Reference<>(-1);
        writer.writeStartElement("symbols");

        for (CharacterTag symbol : characters) {
            if ((symbol instanceof ShapeTag) && nonLibraryShapes.contains(symbol)) {
                continue; //shapes with 1 occurrence and single layer are not added to library
            }

            if ((symbol instanceof ShapeTag) || (symbol instanceof DefineSpriteTag) || (symbol instanceof ButtonTag)) {
                statusStack.pushStatus(symbol.toString());
                XFLXmlWriter symbolStr = new XFLXmlWriter();

                String itemId = generateItemId(lastItemIdNumber);
                symbolStr.writeStartElement("DOMSymbolItem", new String[]{
                    "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
                    "xmlns", "http://ns.adobe.com/xfl/2008/",
                    "name", getSymbolName(lastImportedId, characterNameMap, swf, symbol),
                    "itemID", itemId,
                    "lastModified", Long.toString(getTimestamp(swf))}); //TODO:itemID
                writeLinkage(symbolStr, symbol, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

                if (symbol instanceof ShapeTag) {
                    symbolStr.writeAttribute("symbolType", "graphic");
                } else if (symbol instanceof ButtonTag) {
                    symbolStr.writeAttribute("symbolType", "button");
                    if (((ButtonTag) symbol).trackAsMenu()) {
                        symbolStr.writeAttribute("trackAsMenu", true);
                    }
                }

                DefineScalingGridTag scalingGrid = symbol.getScalingGridTag();
                if (scalingGrid != null) {
                    symbolStr.writeAttribute("scaleGridLeft", doubleToString(scalingGrid.splitter.Xmin / SWF.unitDivisor));
                    symbolStr.writeAttribute("scaleGridRight", doubleToString(scalingGrid.splitter.Xmax / SWF.unitDivisor));
                    symbolStr.writeAttribute("scaleGridTop", doubleToString(scalingGrid.splitter.Ymin / SWF.unitDivisor));
                    symbolStr.writeAttribute("scaleGridBottom", doubleToString(scalingGrid.splitter.Ymax / SWF.unitDivisor));
                }

                String itemIcon = null;
                if (symbol instanceof ButtonTag) {
                    symbolStr.writeStartElement("timeline");
                    itemIcon = "0";
                    symbolStr.writeStartElement("DOMTimeline", new String[]{"name", getSymbolName(lastImportedId, characterNameMap, swf, symbol), "currentFrame", "0"});
                    symbolStr.writeStartElement("layers");

                    ButtonTag button = (ButtonTag) symbol;
                    List<BUTTONRECORD> records = button.getRecords();

                    int maxDepth = 0;
                    for (BUTTONRECORD rec : records) {
                        if (rec.placeDepth > maxDepth) {
                            maxDepth = rec.placeDepth;
                        }
                    }

                    DefineButtonSoundTag defineButtonSound = button.getSounds();
                    int soundLayerOffset = 0;
                    if (defineButtonSound != null) {
                        soundLayerOffset = 1;
                        symbolStr.writeStartElement("DOMLayer", new String[]{"name", "Layer 1"});
                        symbolStr.writeStartElement("frames");
                        for (int frame = 1; frame <= 4; frame++) {

                            int soundChar = 0;
                            SOUNDINFO soundInfo = null;
                            switch (frame) {
                                case 1:
                                    soundChar = defineButtonSound.buttonSoundChar0;
                                    soundInfo = defineButtonSound.buttonSoundInfo0;
                                    break;
                                case 2:
                                    soundChar = defineButtonSound.buttonSoundChar1;
                                    soundInfo = defineButtonSound.buttonSoundInfo1;
                                    break;
                                case 3:
                                    soundChar = defineButtonSound.buttonSoundChar2;
                                    soundInfo = defineButtonSound.buttonSoundInfo2;
                                    break;
                                case 4:
                                    soundChar = defineButtonSound.buttonSoundChar3;
                                    soundInfo = defineButtonSound.buttonSoundInfo3;
                                    break;
                            }
                            symbolStr.writeStartElement("DOMFrame", new String[]{
                                "index", Integer.toString(frame - 1),
                                "keyMode", Integer.toString(KEY_MODE_NORMAL)});
                            if (soundChar > 0) {
                                CharacterTag soundCharTag = button.getSwf().getCharacter(soundChar);
                                if (soundCharTag == null) {
                                    logger.log(Level.WARNING, "Sound tag (ID={0}) was not found", soundChar);
                                } else if (soundCharTag instanceof DefineSoundTag) {
                                    DefineSoundTag sound = (DefineSoundTag) soundCharTag;
                                    convertSoundUsage(symbolStr, sound, soundInfo);
                                } else {
                                    logger.log(Level.WARNING, "Tag (ID={0}) expected to be DefineSound, {1} found. It is referenced from DefineButtonSound({2}).", new Object[]{soundChar, soundCharTag.getClass().getSimpleName(), defineButtonSound.buttonId});
                                }
                            }
                            symbolStr.writeStartElement("elements");
                            symbolStr.writeEndElement(); //elements
                            symbolStr.writeEndElement(); //DOMFrame
                        }
                        symbolStr.writeEndElement(); // frames
                        symbolStr.writeEndElement(); // DOMLayer
                    }

                    for (int i = maxDepth; i >= 1; i--) {
                        symbolStr.writeStartElement("DOMLayer", new String[]{"name", "Layer " + (maxDepth - i + 1 + soundLayerOffset)});
                        if (i == 1) {
                            symbolStr.writeAttribute("current", true);
                            symbolStr.writeAttribute("isSelected", true);
                        }
                        symbolStr.writeAttribute("color", randomOutlineColor());
                        symbolStr.writeStartElement("frames");
                        int lastFrame = 0;

                        loopframes:
                        for (int frame = 1; frame <= 4; frame++) {

                            for (BUTTONRECORD rec : records) {
                                if (rec.placeDepth == i) {
                                    int duration = 0;
                                    while (frame + duration <= 4) {
                                        boolean ok = false;
                                        switch (frame + duration) {
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
                                            break;
                                        }
                                        duration++;
                                    }
                                    if (duration == 0) {
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
                                    CharacterTag character = button.getSwf().getCharacter(rec.characterId);
                                    if (character != null) {
                                        MATRIX matrix = rec.placeMatrix;                                        
                                        XFLXmlWriter recCharWriter = new XFLXmlWriter();

                                        if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(character))) {
                                            ShapeTag shape = (ShapeTag) character;
                                            statusStack.pushStatus(character.toString());
                                            convertShape(lastImportedId, characterNameMap, character.getSwf(), matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false, recCharWriter);
                                            statusStack.popStatus();
                                        } else if (character instanceof MorphShapeTag) { //can happen for HIT_TEST frame
                                            ShapeTag shape = ((MorphShapeTag) character).getStartShapeTag();
                                            statusStack.pushStatus(character.toString());
                                            convertShape(lastImportedId, characterNameMap, character.getSwf(), matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, true, false, recCharWriter);
                                            statusStack.popStatus();
                                        } else if (character instanceof TextTag) {
                                            statusStack.pushStatus(character.toString());
                                            convertText(frame, new AccessibilityBag() /*???*/, null, (TextTag) character, matrix, filters, recCharWriter, characterImportLinkageURL, lastImportedId, characterNameMap, characters);
                                            statusStack.popStatus();
                                        } else if (character instanceof DefineVideoStreamTag) {
                                            statusStack.pushStatus(character.toString());
                                            convertVideoInstance(null, matrix, (DefineVideoStreamTag) character, recCharWriter);
                                            statusStack.popStatus();
                                        } else if (character instanceof ImageTag) {
                                            statusStack.pushStatus(character.toString());
                                            convertImageInstance(lastImportedId, characterNameMap, swf, null, matrix, (ImageTag) character, recCharWriter);
                                            statusStack.popStatus();
                                        } else {
                                            convertSymbolInstance(-1, new AccessibilityBag() /*???*/, lastImportedId, characterNameMap, swf, null, matrix, colorTransformAlpha, false, blendMode, filters, true, null, null, null, character.getSwf().getCharacter(rec.characterId), flaVersion, recCharWriter);
                                        }

                                        int emptyDuration = frame - lastFrame - 1;
                                        lastFrame = frame + duration - 1;
                                        
                                        
                                        if (emptyDuration > 0) {
                                            symbolStr.writeStartElement("DOMFrame", new String[]{
                                                "index", Integer.toString(frame - emptyDuration),
                                                "duration", Integer.toString(emptyDuration),
                                                "keyMode", Integer.toString(KEY_MODE_NORMAL)});
                                            symbolStr.writeElementValue("elements", "");
                                            symbolStr.writeEndElement();
                                        }
                                        
                                        if (duration > 1) {
                                            symbolStr.writeStartElement("DOMFrame", new String[]{
                                                "index", Integer.toString(frame),
                                                "duration", Integer.toString(duration),
                                                "keyMode", Integer.toString(KEY_MODE_NORMAL)});
                                        } else {
                                            symbolStr.writeStartElement("DOMFrame", new String[]{
                                                "index", Integer.toString(frame),
                                                "keyMode", Integer.toString(KEY_MODE_NORMAL)});                                            
                                        }
                                        symbolStr.writeStartElement("elements");
                                        symbolStr.writeCharactersRaw(recCharWriter.toString());
                                        symbolStr.writeEndElement();
                                        symbolStr.writeEndElement();   
                                        frame += duration - 1;
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
                    symbolStr.writeEndElement(); // timeline
                } else if (symbol instanceof DefineSpriteTag) {
                    DefineSpriteTag sprite = (DefineSpriteTag) symbol;
                    if (sprite.getTags().isEmpty()) { //probably AS2 class
                        statusStack.popStatus();
                        continue;
                    }
                    final ScriptPack spriteScriptPack = characterScriptPacks.containsKey(sprite) ? characterScriptPacks.get(sprite) : null;

                    extractMultilevelClips(characterScriptPacks, lastItemIdNumber, lastImportedId, characterNameMap, sprite.getTags(), swf.getCharacterId(sprite), writer, swf, nextClipId, nonLibraryShapes, backgroundColor, flaVersion, files, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);

                    convertTimelines(characterScriptPacks, lastImportedId, characterNameMap, swf, swf.getAbcIndex(), sprite, swf.getCharacterId(sprite), characterVariables.get(sprite), nonLibraryShapes, tags, sprite.getTags(), getSymbolName(lastImportedId, characterNameMap, swf, symbol), flaVersion, files, symbolStr, spriteScriptPack, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);

                } else if (symbol instanceof ShapeTag) {
                    symbolStr.writeStartElement("timeline");
                    itemIcon = "1";
                    ShapeTag shape = (ShapeTag) symbol;
                    symbolStr.writeStartElement("DOMTimeline", new String[]{"name", getSymbolName(lastImportedId, characterNameMap, swf, symbol), "currentFrame", "0"});
                    symbolStr.writeStartElement("layers");
                    SHAPEWITHSTYLE shapeWithStyle = shape.getShapes();
                    if (shapeWithStyle != null) {
                        convertShape(lastImportedId, characterNameMap, symbol.getSwf(), null, shape.getShapeNum(), shapeWithStyle.shapeRecords, shapeWithStyle.fillStyles, shapeWithStyle.lineStyles, false, true, symbolStr);
                    }

                    symbolStr.writeEndElement(); // layers
                    symbolStr.writeEndElement(); // DOMTimeline
                    symbolStr.writeEndElement(); // timeline
                }

                symbolStr.writeEndElement(); // DOMSymbolItem
                String symbolStr2 = prettyFormatXML(symbolStr.toString());
                String symbolFile = getSymbolName(lastImportedId, characterNameMap, swf, symbol) + ".xml";
                files.put(symbolFile, Utf8Helper.getBytes(symbolStr2));

                // write symbLink
                writer.writeStartElement("Include", new String[]{"href", symbolFile});
                writer.writeAttribute("itemID", itemId);
                if (itemIcon != null) {
                    writer.writeAttribute("itemIcon", itemIcon);
                }
                writer.writeAttribute("loadImmediate", false);
                if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
                    writer.writeAttribute("lastModified", getTimestamp(swf));
                    //TODO: itemID="518de416-00000341"
                }
                writer.writeEndElement();
                //hasSymbol = true;                
                statusStack.popStatus();
            }
        }

        statusStack.pushStatus("extracting multilevel clips");
        extractMultilevelClips(characterScriptPacks, lastItemIdNumber, lastImportedId, characterNameMap, swf.getTags(), -1, writer, swf, nextClipId, nonLibraryShapes, backgroundColor, flaVersion, files, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
        statusStack.popStatus();

        statusStack.pushStatus("converting multiusage morphshapes");
        extractMultiUsageMorphShapes(characterScriptPacks, lastItemIdNumber, lastImportedId, characterNameMap, writer, swf, nonLibraryShapes, flaVersion, files, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
        statusStack.popStatus();
        /*if (hasSymbol) {
            
        }*/
        writer.writeEndElement();
    }

    private void convertSoundMedia(Reference<Integer> lastItemIdNumber, Map<CharacterTag, String> characterImportLinkageURL, SWF swf, ReadOnlyTagList tags, SoundTag symbol, XFLXmlWriter writer, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles) throws XMLStreamException {
        int soundFormat = 0;
        int soundRate = 0;
        boolean soundType = false;
        boolean soundSize = false;
        long soundSampleCount = 0;
        byte[] soundData = SWFInputStream.BYTE_ARRAY_EMPTY;
        int[] rateMap = {5, 11, 22, 44};
        String exportFormat = "wav";
        if (symbol instanceof SoundStreamFrameRange) {
            SoundStreamHeadTypeTag head = ((SoundStreamFrameRange) symbol).getHead();
            soundFormat = head.getSoundFormatId();
            soundRate = head.getSoundRate();
            soundType = head.getSoundType();
            soundSize = head.getSoundSize();
            soundSampleCount = 0; //head.getSoundSampleCount();
            boolean found = false;
            for (Tag t : tags) {
                if (found && (t instanceof SoundStreamBlockTag)) {
                    SoundStreamBlockTag bl = (SoundStreamBlockTag) t;
                    soundData = bl.streamSoundData.getRangeData();
                    break;
                }
                if (t == head) {
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
        int seekSamples = 0;
        boolean convertMp3ToWav = false;
        if (soundFormat == SoundFormat.FORMAT_MP3) {
            exportFormat = "mp3";
            if (!soundType) { //mono
                format += 1;
            }
            format += 4; //quality best
            try {
                SWFInputStream sis = new SWFInputStream(swf, soundData);
                MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
                if (s.seekSamples > 0) {
                    seekSamples = s.seekSamples;
                    exportFormat = "wav";
                    convertMp3ToWav = true;
                }
                if (!s.frames.isEmpty()) {
                    MP3FRAME frame = s.frames.get(0);
                    int bitRate = frame.getBitRate() / 1000;

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
                        default:
                            bits = 17;
                            exportFormat = "wav";
                            convertMp3ToWav = true;
                            break;
                    }
                }
            } catch (IOException | IndexOutOfBoundsException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        SoundTag st = (SoundTag) symbol;
        byte[] data = SWFInputStream.BYTE_ARRAY_EMPTY;
        try {
            data = new SoundExporter().exportSound(st, convertMp3ToWav ? SoundExportMode.WAV : SoundExportMode.MP3_WAV, false);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        byte[] decodedData = null;
        try {
            decodedData = st.getSoundFormat().decode(null, st.getRawSoundData(), seekSamples);
            if (soundSampleCount == 0) {
                soundSampleCount = decodedData.length / (2 * (st.getSoundType() ? 2 : 1));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        String datFileName = null;
        if (decodedData != null) {
            long ts = getTimestamp(swf);
            datFileName = "M " + (datfiles.size() + 1) + " " + ts + ".dat";
            datfiles.put(datFileName, decodedData);
        }

        String symbolFile = symbol.getFlaExportName() + "." + exportFormat;
        files.put(symbolFile, data);
        writer.writeStartElement("DOMSoundItem", new String[]{
            "name", symbolFile,
            "itemID", generateItemId(lastItemIdNumber),
            "sourceLastImported", Long.toString(getTimestamp(swf)),
            "externalFileSize", Integer.toString(data.length)});
        writer.writeAttribute("href", symbolFile);
        if (datFileName != null) {
            writer.writeAttribute("soundDataHRef", datFileName);
        }
        writer.writeAttribute("format", rateMap[soundRate] + "kHz" + " " + (soundSize ? "16bit" : "8bit") + " " + (soundType ? "Stereo" : "Mono"));
        writer.writeAttribute("exportFormat", format);
        writer.writeAttribute("exportBits", bits);
        writer.writeAttribute("sampleCount", soundSampleCount);
    }

    private void convertMedia(Reference<Integer> lastItemIdNumber, Set<CharacterTag> charactersExportedInFirstFrame, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters, SWF swf, Map<CharacterTag, String> characterVariables, Map<CharacterTag, String> characterClasses, ReadOnlyTagList tags, HashMap<String, byte[]> files, HashMap<String, byte[]> datfiles, XFLXmlWriter writer, StatusStack statusStack) throws XMLStreamException {
        boolean hasMedia = false;
        for (CharacterTag symbol : characters) {
            if (symbol instanceof ImageTag
                    || symbol instanceof DefineSoundTag
                    || symbol instanceof DefineVideoStreamTag) {
                //symbol instanceof SoundStreamHeadTypeTag  FIXME
                hasMedia = true;
            }
        }

        if (!hasMedia) {
            return;
        }

        writer.writeStartElement("media");

        for (CharacterTag symbol : characters) {
            if (symbol instanceof ImageTag) {

                statusStack.pushStatus(symbol.toString());
                ImageTag imageTag = (ImageTag) symbol;
                boolean allowSmoothing = false;

                //find if smoothed - a bitmap is smoothed when there is a shape with fillstyle smoothed bitmap
                looptags:
                for (Tag tag : swf.getTags()) {
                    if (tag instanceof ShapeTag) {
                        Set<Integer> needed = new HashSet<>();
                        tag.getNeededCharacters(needed, swf);
                        ShapeTag sht = (ShapeTag) tag;
                        if (needed.contains(imageTag.getCharacterId())) {
                            List<FILLSTYLE> fs = new ArrayList<>();
                            SHAPEWITHSTYLE s = sht.getShapes();
                            fs.addAll(Arrays.asList(s.fillStyles.fillStyles));
                            for (SHAPERECORD r : s.shapeRecords) {
                                if (r instanceof StyleChangeRecord) {
                                    StyleChangeRecord scr = (StyleChangeRecord) r;
                                    if (scr.stateNewStyles) {
                                        fs.addAll(Arrays.asList(scr.fillStyles.fillStyles));
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

                byte[] imageBytes = Helper.readStream(imageTag.getConvertedImageData());
                SerializableImage image = imageTag.getImageCached();
                ImageFormat format = imageTag.getImageFormat();
                String symbolFile = getSymbolName(lastImportedId, characterNameMap, swf, symbol, "Bitmap") + imageTag.getImageFormat().getExtension();
                files.put(symbolFile, imageBytes);
                writer.writeStartElement("DOMBitmapItem", new String[]{
                    "name", symbolFile,
                    "itemID", generateItemId(lastItemIdNumber),
                    "sourceLastImported", Long.toString(getTimestamp(swf)),
                    "externalFileSize", Integer.toString(imageBytes.length)});

                if (characterImportLinkageURL.containsKey(symbol)) {
                    writer.writeAttribute("linkageImportForRS", "true");
                    writer.writeAttribute("linkageURL", characterImportLinkageURL.get(symbol));
                }

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
                boolean linkageExportForAS = false;
                if (characterClasses.containsKey(symbol)) {
                    String className = characterClasses.get(symbol);
                    boolean isBitmapData = false;
                    for (ABCContainerTag c : swf.getAbcList()) {
                        int classIndex = c.getABC().findClassByName(className);
                        if (classIndex != -1) {
                            if (swf.getAbcIndex().isInstanceOf(c.getABC(), classIndex, DottedChain.parseNoSuffix("flash.display.BitmapData"))) {
                                isBitmapData = true;
                            }
                            break;
                        }
                    }
                    if (isBitmapData) {
                        linkageExportForAS = true;
                        writer.writeAttribute("linkageClassName", characterClasses.get(symbol));
                    }
                    //if it's not BitmapData, then it should use Embed
                }

                if (characterVariables.containsKey(symbol)) {
                    linkageExportForAS = true;
                    writer.writeAttribute("linkageIdentifier", characterVariables.get(symbol));
                }
                if (characterImportLinkageURL.containsKey(symbol)) {
                    linkageExportForAS = false;
                }
                if (linkageExportForAS) {
                    if (!charactersExportedInFirstFrame.contains(symbol)) {
                        writer.writeAttribute("linkageExportInFirstFrame", "false");
                    }

                    writer.writeAttribute("linkageExportForAS", true);
                }

                writer.writeAttribute("quality", 50);
                writer.writeAttribute("href", symbolFile);
                String datFileName = "M " + (datfiles.size() + 1) + " " + getTimestamp(swf) + ".dat";
                writer.writeAttribute("bitmapDataHRef", datFileName);
                writer.writeAttribute("frameRight", (int) (image.getWidth() * SWF.unitDivisor));
                writer.writeAttribute("frameBottom", (int) (image.getHeight() * SWF.unitDivisor));
                writer.writeEndElement();

                ImageBinDataGenerator ibg = new ImageBinDataGenerator();
                ByteArrayOutputStream iba = new ByteArrayOutputStream();
                try {
                    ibg.generateBinData(new ByteArrayInputStream(imageBytes), iba, format);
                } catch (IOException ex) {
                    Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Error during bin/dat file generation for image", ex);
                }
                datfiles.put(datFileName, iba.toByteArray());
                statusStack.popStatus();
            } else if (symbol instanceof DefineSoundTag) {
                statusStack.pushStatus(symbol.toString());
                convertSoundMedia(lastItemIdNumber, characterImportLinkageURL, swf, tags, (DefineSoundTag) symbol, writer, files, datfiles);

                writeLinkage(writer, symbol, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

                writer.writeEndElement();
                statusStack.popStatus();
            } else if (symbol instanceof DefineVideoStreamTag) {
                statusStack.pushStatus(symbol.toString());
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
                    writer.writeStartElement("DOMVideoItem", new String[]{
                        "name", symbolFile,
                        "sourceExternalFilepath", "./LIBRARY/" + symbolFile,
                        "sourceLastImported", Long.toString(ts),
                        "videoDataHRef", datFileName,
                        "channels", "0",
                        "isSpecial", "true"});
                    writeLinkage(writer, symbol, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

                    MovieBinDataGenerator mbd = new MovieBinDataGenerator();
                    datfiles.put(datFileName, mbd.generateEmptyBinData());
                } else {
                    files.put(symbolFile, data);
                    writer.writeStartElement("DOMVideoItem", new String[]{
                        "name", symbolFile,
                        "itemID", generateItemId(lastItemIdNumber),
                        "sourceLastImported", Long.toString(getTimestamp(swf)),
                        "externalFileSize", Integer.toString(data.length)});
                    writer.writeAttribute("href", symbolFile);
                    writer.writeAttribute("videoType", videoType);
                    writer.writeAttribute("fps", (int) swf.frameRate); // todo: is the cast to int needed?
                    writer.writeAttribute("width", video.width);
                    writer.writeAttribute("height", video.height);
                    double len = (double) video.numFrames / swf.frameRate;
                    writer.writeAttribute("length", len);
                    writeLinkage(writer, symbol, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

                    long ts = getTimestamp(swf);
                    String datFileName = "M " + (datfiles.size() + 1) + " " + ts + ".dat";
                    writer.writeAttribute("videoDataHRef", datFileName);
                    MovieBinDataGenerator mbg = new MovieBinDataGenerator();
                    ByteArrayOutputStream bba = new ByteArrayOutputStream();
                    try {
                        mbg.generateBinData(new ByteArrayInputStream(data), bba, swf.frameRate);
                    } catch (IOException ex) {
                        Logger.getLogger(XFLConverter.class.getName()).log(Level.SEVERE, "Error during bin/dat file generation for movie", ex);
                    }
                    datfiles.put(datFileName, bba.toByteArray());

                    writer.writeEndElement();
                }
                statusStack.popStatus();
            }
        }

        for (Tag t : tags) {
            if (t instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) t;
                for (SoundStreamFrameRange range : head.getRanges()) {
                    statusStack.pushStatus(range.toString());
                    convertSoundMedia(lastItemIdNumber, characterImportLinkageURL, swf, tags, range, writer, files, datfiles);
                    writer.writeEndElement();
                    statusStack.popStatus();
                }
            }
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                for (Tag st : sprite.getTags()) {
                    if (st instanceof SoundStreamHeadTypeTag) {
                        SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) st;
                        for (SoundStreamFrameRange range : head.getRanges()) {
                            statusStack.pushStatus(range.toString());
                            convertSoundMedia(lastItemIdNumber, characterImportLinkageURL, swf, sprite.getTags(), range, writer, files, datfiles);
                            writer.writeEndElement();
                            statusStack.popStatus();
                        }
                        break;
                    }
                }
            }
        }

        writer.writeEndElement();
    }

    private static void writeLinkage(XFLXmlWriter writer, CharacterTag symbol, Map<CharacterTag, String> characterVariables, Map<CharacterTag, String> characterClasses, Set<CharacterTag> charactersExportedInFirstFrame, Map<CharacterTag, String> characterImportLinkageURL) throws XMLStreamException {
        boolean linkageExportForAS = false;
        if (characterClasses.containsKey(symbol)) {
            linkageExportForAS = true;
            writer.writeAttribute("linkageClassName", characterClasses.get(symbol));
        }
        if (characterVariables.containsKey(symbol)) {
            linkageExportForAS = true;
            writer.writeAttribute("linkageIdentifier", characterVariables.get(symbol));
        }
        if (characterImportLinkageURL.containsKey(symbol)) {
            writer.writeAttribute("linkageImportForRS", "true");
            writer.writeAttribute("linkageURL", characterImportLinkageURL.get(symbol));
            linkageExportForAS = false;
        }
        if (linkageExportForAS) {
            if (!charactersExportedInFirstFrame.contains(symbol)) {
                writer.writeAttribute("linkageExportInFirstFrame", "false");
            }

            writer.writeAttribute("linkageExportForAS", true);
        }
    }

    private String prettyFormatXML(String input) {
        return new XmlPrettyFormat().prettyFormat(input, 5, false);
    }

    private static void convertSoundUsage(XFLXmlWriter writer, DefineSoundTag sound, SOUNDINFO soundInfo) throws XMLStreamException {
        String soundName = "sound" + sound.soundId + "." + sound.getExportFormat().toString().toLowerCase();
        writer.writeAttribute("soundName", soundName);
        if (soundInfo.hasInPoint) {
            writer.writeAttribute("inPoint44", soundInfo.inPoint);
        }
        if (soundInfo.hasOutPoint) {
            writer.writeAttribute("outPoint44", soundInfo.outPoint);
        }
        if (soundInfo.hasLoops) {
            if (soundInfo.loopCount == 32767) {
                writer.writeAttribute("soundLoopMode", "loop");
            }
            writer.writeAttribute("soundLoop", soundInfo.loopCount);
        }

        if (soundInfo.syncStop) {
            writer.writeAttribute("soundSync", "stop");
        } else if (soundInfo.syncNoMultiple) {
            writer.writeAttribute("soundSync", "start");
        }

        if (soundInfo.hasEnvelope) {
            SOUNDENVELOPE[] envelopeRecords = soundInfo.envelopeRecords;

            long soundLength44 = 0;
            switch (sound.soundRate) {
                case 0: //5.5kHz
                    soundLength44 = 8 * sound.soundSampleCount;
                    break;
                case 1: //11kHz
                    soundLength44 = 4 * sound.soundSampleCount;
                    break;
                case 2: //22kHz
                    soundLength44 = 2 * sound.soundSampleCount;
                    break;
                case 3: //44kHz
                    soundLength44 = sound.soundSampleCount;
                    break;

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
                    && envelopeRecords[1].pos44 == soundLength44
                    && envelopeRecords[1].rightLevel == 32768) {
                writer.writeAttribute("soundEffect", "fade left to right");
            } else if (envelopeRecords.length == 2
                    && envelopeRecords[0].leftLevel == 0
                    && envelopeRecords[0].pos44 == 0
                    && envelopeRecords[0].rightLevel == 32768
                    && envelopeRecords[1].leftLevel == 32768
                    && envelopeRecords[1].pos44 == soundLength44
                    && envelopeRecords[1].rightLevel == 0) {
                writer.writeAttribute("soundEffect", "fade right to left");
            } else if (envelopeRecords.length == 2
                    && envelopeRecords[0].leftLevel == 0
                    && envelopeRecords[0].pos44 == 0
                    && envelopeRecords[0].rightLevel == 0
                    && envelopeRecords[1].leftLevel == 32768
                    && envelopeRecords[1].pos44 == soundLength44 / 4
                    && envelopeRecords[1].rightLevel == 0) {
                writer.writeAttribute("soundEffect", "fade in");
            } else if (envelopeRecords.length == 2
                    && envelopeRecords[0].leftLevel == 32768
                    && envelopeRecords[0].pos44 == soundLength44 * 3 / 4
                    && envelopeRecords[0].rightLevel == 32768
                    && envelopeRecords[1].leftLevel == 0
                    && envelopeRecords[1].pos44 == soundLength44
                    && envelopeRecords[1].rightLevel == 0) {
                writer.writeAttribute("soundEffect", "fade out");
            } else {
                writer.writeAttribute("soundEffect", "custom");
            }

            writer.writeStartElement("SoundEnvelope");
            for (SOUNDENVELOPE env : envelopeRecords) {
                writer.writeEmptyElement("SoundEnvelopePoint", new String[]{"mark44", Long.toString(env.pos44), "level0", Integer.toString(env.leftLevel), "level1", Integer.toString(env.rightLevel)});
            }
            writer.writeEndElement(); // SoundEnvelope
        } else {
            writer.writeStartElement("SoundEnvelope");
            writer.writeEmptyElement("SoundEnvelopePoint", new String[]{"level0", "32768", "level1", "32768"});
            writer.writeEndElement(); // SoundEnvelope
        }
    }

    private static void convertFrame(boolean shapeTween, SoundStreamFrameRange soundStreamRange, StartSoundTag startSound, int frame, int duration, String actionScript, String elements, XFLXmlWriter writer, Integer acceleration) throws XMLStreamException {
        DefineSoundTag sound = null;
        if (startSound != null) {
            SWF swf = startSound.getSwf();
            sound = swf.getSound(startSound.soundId);
        }
        //System.err.println("-- writing frame " + frame);
        writer.writeStartElement("DOMFrame");
        writer.writeAttribute("index", frame);
        if (duration > 1) {
            writer.writeAttribute("duration", duration);
        }
        if (shapeTween) {
            writer.writeAttribute("tweenType", "shape");
            writer.writeAttribute("keyMode", KEY_MODE_SHAPE_TWEEN);
            if (acceleration != null) {
                writer.writeAttribute("acceleration", acceleration);
            }
        } else {
            writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
        }
        if (soundStreamRange != null && startSound == null) {
            String soundName = soundStreamRange.getFlaExportName() + "." + soundStreamRange.getExportFormat().toString().toLowerCase();
            writer.writeAttribute("soundName", soundName);
            writer.writeAttribute("soundSync", "stream");
            writer.writeStartElement("SoundEnvelope");
            writer.writeEmptyElement("SoundEnvelopePoint", new String[]{"level0", "32768", "level1", "32768"});
            writer.writeEndElement();

        }
        if (startSound != null && sound != null) {
            convertSoundUsage(writer, sound, startSound.soundInfo);
        }

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

    private static void convertVideoInstance(String instanceName, MATRIX matrix, DefineVideoStreamTag video, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("DOMVideoInstance", new String[]{
            "libraryItemName", "movie" + video.characterID + ".flv",
            "frameRight", Integer.toString((int) (SWF.unitDivisor * video.width)),
            "frameBottom", Integer.toString((int) (SWF.unitDivisor * video.height))});
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

    private static void convertImageInstance(Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, String instanceName, MATRIX matrix, ImageTag bitmap, XFLXmlWriter writer) throws XMLStreamException {
        writer.writeStartElement("DOMBitmapInstance", new String[]{
            "libraryItemName", getSymbolName(lastImportedId, characterNameMap, swf, bitmap, "Bitmap") + bitmap.getImageFormat().getExtension()});
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

    private static void convertFrames(AccessibilityBag accessibility, String symbolName, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, List<Integer> onlyFrames, int startFrame, int endFrame, String prevStr, String afterStr, List<CharacterTag> nonLibraryShapes, ReadOnlyTagList timelineTags, int depth, FLAVersion flaVersion, XFLXmlWriter writer, List<Integer> multiUsageMorphShapes, StatusStack statusStack, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters) throws XMLStreamException {
        Logger.getLogger(XFLConverter.class.getName()).log(Level.FINE, "Converting frames of {0}", symbolName);
        boolean lastIn = false;
        XFLXmlWriter writer2 = new XFLXmlWriter();
        prevStr += "<frames>";
        int frame = -1;
        String lastElements = "";
        CharacterTag lastCharacter = null;
        MATRIX lastMatrix = null;

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
        int ratio = -1;
        boolean shapeTween = false;
        MorphShapeTag shapeTweener = null;
        List<Integer> morphShapeRatios = new ArrayList<>();
        MorphShapeTag standaloneShapeTweener = null;
        MATRIX standaloneShapeTweenerMatrix = null;

        //Add ShowFrameTag to the end when there is one last missing
        List<Tag> timTags = timelineTags.toArrayList();
        boolean needsFrameAdd = false;
        for (int i = timTags.size() - 1; i >= 0; i--) {
            if (timTags.get(i) instanceof ShowFrameTag) {
                break;
            }
            if (timTags.get(i) instanceof PlaceObjectTypeTag) {
                needsFrameAdd = true;
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
                    String newCharCls = po.getClassName();
                    CharacterTag newCharacter = null;
                    if (newCharId != -1) {
                        newCharacter = po.getSwf().getCharacter(newCharId);
                    } else if (newCharCls != null) {
                        newCharacter = po.getSwf().getCharacterByClass(newCharCls);
                    }

                    if (newCharacter != null && newCharacter != character) {
                        if (shapeTween && character != null) {
                            MorphShapeTag m = (MorphShapeTag) character;
                            shapeTweener = m;
                            shapeTween = false;
                        }
                    }                    
                    if (newCharId == -1 && newCharCls == null) {
                        newCharacter = character;
                    }
                    character = newCharacter;
                    if (character != null) {
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
                            if (po.getPlaceObjectNum() >= 3) {
                                if (po.getVisible() != null) {
                                    isVisible = po.isVisible();
                                }
                            } else {
                                isVisible = true;
                            }
                            if (po.getBackgroundColor() != null) {
                                backGroundColor = po.getBackgroundColor();
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
                            isVisible = po.isVisible();
                            backGroundColor = po.getBackgroundColor();
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
                    clipActions = null;
                }
            }

            if (t instanceof ShowFrameTag) {
                frame++;
                if (frame < startFrame || frame > endFrame || (onlyFrames != null && !onlyFrames.contains(frame))) {
                    if (lastIn) {
                        if (!lastElements.isEmpty()) {
                            convertFrame(false, null, null, frame - duration, duration, "", lastElements, writer2, null);
                            duration = 1;
                        } else {
                            duration++;
                        }
                        lastElements = "";
                        lastIn = false;
                        lastCharacter = null;
                        lastMatrix = null;
                    } else if (frame == 0) {
                        duration = 1;
                    } else {
                        duration++;
                    }
                    continue;
                }
                lastIn = true;

                XFLXmlWriter elementsWriter = new XFLXmlWriter();

                if (shapeTweener != null) {
                    MorphShapeTag m = shapeTweener;
                    XFLXmlWriter addLastWriter = new XFLXmlWriter();

                    if ((character instanceof MorphShapeTag) && (!multiUsageMorphShapes.contains(character.getCharacterId()))) {
                        MorphShapeTag m2 = (MorphShapeTag) character;
                        statusStack.pushStatus(m2.toString());
                        convertShape(lastImportedId, characterNameMap, swf, matrix, m2.getShapeNum() == 1 ? 3 : 4, m2.getStartEdges().shapeRecords, m2.getFillStyles().getStartFillStyles(), m2.getLineStyles().getStartLineStyles(m2.getShapeNum()), true, false, addLastWriter);
                        statusStack.popStatus();
                        shapeTween = true;
                    } else {
                        SHAPEWITHSTYLE endShape = m.getShapeAtRatio(65535); //lastTweenRatio);                    
                        convertShape(lastImportedId, characterNameMap, swf, matrix, m.getShapeNum() == 1 ? 3 : 4, endShape.shapeRecords, m.getFillStyles().getFillStylesAt(65535), m.getLineStyles().getLineStylesAt(m.getShapeNum(), 65535), true, false, addLastWriter);
                    }

                    Integer ease = EasingDetector.getEaseFromShapeRatios(morphShapeRatios);
                    Integer acceleration = null;
                    if (ease != null) {
                        acceleration = -ease;
                    }
                    convertFrame(true, null, null, frame - duration, duration, "", lastElements, writer2, acceleration);
                    duration = 1;
                    lastElements = addLastWriter.toString();
                    lastMatrix = matrix;
                    lastCharacter = character;
                    shapeTweener = null;
                    morphShapeRatios.clear();
                    continue;
                }

                if (character instanceof ShapeTag && standaloneShapeTweener != null) {
                    convertSymbolInstance(frame, accessibility, lastImportedId, characterNameMap, swf, instanceName, standaloneShapeTweenerMatrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, metadata, standaloneShapeTweener, flaVersion, elementsWriter);
                    standaloneShapeTweener = null;
                } else if ((character instanceof ShapeTag) && (nonLibraryShapes.contains(character))) {
                    if (lastCharacter == character && Objects.equals(matrix, lastMatrix)) {
                        elementsWriter.writeCharactersRaw(lastElements);
                    } else {
                        ShapeTag shape = (ShapeTag) character;
                        statusStack.pushStatus(character.toString());
                        convertShape(lastImportedId, characterNameMap, swf, matrix, shape.getShapeNum(), shape.getShapes().shapeRecords, shape.getShapes().fillStyles, shape.getShapes().lineStyles, false, false, elementsWriter);
                        statusStack.popStatus();
                    }
                    shapeTween = false;
                    shapeTweener = null;
                } else if (character instanceof MorphShapeTag) {
                    MorphShapeTag m = (MorphShapeTag) character;
                    if (multiUsageMorphShapes.contains(m.getCharacterId())) {
                        shapeTween = false;
                        shapeTweener = null;
                        standaloneShapeTweener = m;
                        standaloneShapeTweenerMatrix = matrix;
                        convertSymbolInstance(frame, accessibility, lastImportedId, characterNameMap, swf, instanceName, matrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, metadata, character, flaVersion, elementsWriter);
                    } else {
                        morphShapeRatios.add(ratio == -1 ? 0 : ratio);
                        if (lastCharacter == m && Objects.equals(matrix, lastMatrix)) {
                            elementsWriter.writeCharactersRaw(lastElements);
                        } else {
                            statusStack.pushStatus(m.toString());
                            convertShape(lastImportedId, characterNameMap, swf, matrix, m.getShapeNum() == 1 ? 3 : 4, m.getStartEdges().shapeRecords, m.getFillStyles().getStartFillStyles(), m.getLineStyles().getStartLineStyles(m.getShapeNum()), true, false, elementsWriter);
                            statusStack.popStatus();
                        }
                        shapeTween = true;
                    }
                } else {
                    shapeTween = false;
                    if (character instanceof TextTag) {
                        statusStack.pushStatus(character.toString());
                        convertText(frame, accessibility, instanceName, (TextTag) character, matrix, filters, elementsWriter, characterImportLinkageURL, lastImportedId, characterNameMap, characters);
                        statusStack.popStatus();
                    } else if (character instanceof DefineVideoStreamTag) {
                        convertVideoInstance(instanceName, matrix, (DefineVideoStreamTag) character, elementsWriter);
                    } else if (character instanceof ImageTag) {
                        convertImageInstance(lastImportedId, characterNameMap, swf, instanceName, matrix, (ImageTag) character, elementsWriter);
                    } else if (character != null) {
                        convertSymbolInstance(frame, accessibility, lastImportedId, characterNameMap, swf, instanceName, matrix, colorTransForm, cacheAsBitmap, blendMode, filters, isVisible, backGroundColor, clipActions, metadata, character, flaVersion, elementsWriter);
                    }
                }

                String elements = elementsWriter.toString();
                if (!elements.equals(lastElements) && frame > 0) {
                    convertFrame(false, null, null, frame - duration, duration, "", lastElements, writer2, null);
                    duration = 1;
                } else if (frame == 0) {
                    duration = 1;
                } else {
                    duration++;
                }
                lastElements = elements;
                lastCharacter = character;
                lastMatrix = matrix;
            }
        }

        if ((!lastElements.isEmpty() || writer2.length() > 0) && lastIn) {
            if (frame >= startFrame && frame <= endFrame && (onlyFrames == null || onlyFrames.contains(frame))) {
                frame++;
                convertFrame(false, null, null, frame - duration, duration, "", lastElements, writer2, null);
            }
        }
        afterStr = "</frames>" + afterStr;

        if (writer2.length() > 0) {
            writer.writeCharactersRaw(prevStr);
            writer.writeCharactersRaw(writer2.toString());
            writer.writeCharactersRaw(afterStr);
        }
    }

    private static void convertFonts(Reference<Integer> lastItemIdNumber, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, Set<CharacterTag> characters, XFLXmlWriter writer, StatusStack statusStack, Map<CharacterTag, String> characterVariables, Map<CharacterTag, String> characterClasses, Set<CharacterTag> charactersExportedInFirstFrame, Map<CharacterTag, String> characterImportLinkageURL) throws XMLStreamException {
        boolean hasFont = false;
        int fontCounter = 0;
        for (CharacterTag t : characters) {
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

        for (Tag ct : characters) {
            if (ct instanceof FontTag) {
                statusStack.pushStatus(ct.toString());
                FontTag font = (FontTag) ct;
                DefineFontNameTag fontNameTag = font.getFontNameTag();
                String fontName = fontNameTag == null ? null : fontNameTag.fontName;
                int fontStyle = font.getFontStyle();
                if (fontName == null) {
                    fontName = font.getFontNameIntag();
                }

                if (fontName == null) {
                    fontName = FontTag.getDefaultFontName();
                }

                String installedFont;
                if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                    fontName = new Font(installedFont, fontStyle, 10).getPSName();
                }

                String embedRanges = "";

                String fontChars = font.getCharacters();
                if ("".equals(fontChars)) {
                    statusStack.popStatus();
                    continue;
                }

                String embeddedCharacters = fontChars;

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

                embeddedCharacters = embeddedCharacters.replace("\u00A0", ""); // nonbreak space
                for (char i = 0; i < 32; i++) {
                    if (i == 9 || i == 10 || i == 13) {
                        continue;
                    }

                    embeddedCharacters = embeddedCharacters.replace("" + i, ""); // not supported in xml
                }

                fontCounter++;
                writer.writeStartElement("DOMFontItem", new String[]{
                    "name", getSymbolName(lastImportedId, characterNameMap, swf, font, "Font"),
                    "itemID", generateItemId(lastItemIdNumber),
                    "font", fontName,
                    "size", "0",
                    "id", Integer.toString(fontCounter),
                    "embedRanges", embedRanges});

                writeLinkage(writer, (FontTag) ct, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

                if (!"".equals(embeddedCharacters)) {
                    writer.writeAttribute("embeddedCharacters", embeddedCharacters);
                }

                writer.writeEndElement();
                statusStack.popStatus();
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

    private static Map<String, String> getRootAccessibilityFromPack(AbcIndexing abcIndex, ScriptPack pack) {
        int swfVersion = -1;
        if (pack.getOpenable() instanceof SWF) {
            swfVersion = ((SWF) pack.getOpenable()).version;
        }
        Map<String, String> ret = new HashMap<>();
        int classIndex = getPackMainClassId(pack);
        if (classIndex > -1) {
            ABC abc = pack.abc;
            InstanceInfo instanceInfo = abc.instance_info.get(classIndex);
            int constructorMethodIndex = instanceInfo.iinit_index;
            MethodBody constructorBody = abc.findBody(constructorMethodIndex);
            try {
                List<MethodBody> callStack = new ArrayList<>();
                callStack.add(constructorBody);
                constructorBody.convert(swfVersion, callStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                if (constructorBody.convertedItems != null) {
                    for (int j = 0; j < constructorBody.convertedItems.size(); j++) {
                        GraphTargetItem ti = constructorBody.convertedItems.get(j);
                        if (ti instanceof SetPropertyAVM2Item) {
                            if (ti.value instanceof ConstructPropAVM2Item) {
                                ConstructPropAVM2Item cons = (ConstructPropAVM2Item) ti.value;
                                if (cons.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) cons.propertyName;
                                    if ("AccessibilityProperties".equals(fm.resolvedMultinameName)) {

                                        continue;
                                    }
                                }
                            }
                            SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) ti;
                            if (setProp.object instanceof GetPropertyAVM2Item) {
                                GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                    FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                    if ("accessibilityProperties".equals(parentProp.resolvedMultinameName)) {
                                        if (parentGetProp.object instanceof GetPropertyAVM2Item) {
                                            GetPropertyAVM2Item parentParentGetProp = (GetPropertyAVM2Item) parentGetProp.object;
                                            if (parentParentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                FullMultinameAVM2Item parentParentProp = (FullMultinameAVM2Item) parentParentGetProp.propertyName;
                                                if ("root".equals(parentParentProp.resolvedMultinameName)) {
                                                    if (parentParentGetProp.object instanceof ThisAVM2Item) {
                                                        if (setProp.propertyName instanceof FullMultinameAVM2Item) {
                                                            FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                            String acProp = prop.resolvedMultinameName;
                                                            if (Arrays.asList("name", "description", "forceSimple", "noAutoLabeling", "shortcut").contains(acProp)) {
                                                                boolean invert = false;
                                                                if ("noAutoLabeling".equals(acProp)) {
                                                                    acProp = "autoLabeling";
                                                                    invert = true;
                                                                }
                                                                if ("name".equals(acProp)) {
                                                                    acProp = "accName";
                                                                }
                                                                String val = "";
                                                                if (setProp.value instanceof StringAVM2Item) {
                                                                    val = (String) ((StringAVM2Item) setProp.value).getResult();
                                                                }
                                                                if (setProp.value instanceof TrueItem) {
                                                                    val = invert ? "false" : "true";
                                                                }
                                                                if (setProp.value instanceof FalseItem) {
                                                                    val = invert ? "true" : "false";
                                                                }
                                                                ret.put(acProp, val);
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
                    }
                }
            } catch (InterruptedException ex) {
                //ignore
            }
        }
        return ret;
    }

    private static class AccessibilityBag {

        private List<AccessibilityItem> items = new ArrayList<>();

        public void add(AccessibilityItem item) {
            items.add(item);
        }

        public Map<String, String> getAttributes(String instanceName, int frame) {
            Map<String, String> ret = new LinkedHashMap<>();
            for (AccessibilityItem item : items) {
                if (item.contains(instanceName, frame)) {
                    ret.put(item.attributeKey, item.attributeValue);
                }
            }
            return ret;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (AccessibilityItem item : items) {
                if (!first) {
                    sb.append("\r\n");
                }
                first = false;
                sb.append(item.toString());
            }
            return sb.toString();
        }
    }

    private static class AccessibilityItem {

        int startFrame;
        int endFrame;
        String instanceName;
        String attributeKey;
        String attributeValue;

        public AccessibilityItem(String instanceName, String attributeKey, String attributeValue) {
            this(instanceName, attributeKey, attributeValue, 1, Integer.MAX_VALUE);
        }

        public AccessibilityItem(String instanceName, String attributeKey, String attributeValue, int frame) {
            this(instanceName, attributeKey, attributeValue, frame, frame);
        }

        public AccessibilityItem(String instanceName, String attributeKey, String attributeValue, int startFrame, int endFrame) {
            this.startFrame = startFrame;
            this.endFrame = endFrame;
            this.instanceName = instanceName;
            this.attributeKey = attributeKey;
            this.attributeValue = attributeValue;
        }

        public boolean contains(String instanceName, int frame) {
            if (!Objects.equals(instanceName, this.instanceName)) {
                return false;
            }
            return frame >= startFrame && frame <= endFrame;
        }

        @Override
        public String toString() {
            return "[instance: " + instanceName + " key: \"" + attributeKey + "\" value: \"" + attributeValue + "\" frames: " + startFrame + " to " + (endFrame == Integer.MAX_VALUE ? "end" : endFrame) + "]";
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 13 * hash + this.startFrame;
            hash = 13 * hash + this.endFrame;
            hash = 13 * hash + Objects.hashCode(this.instanceName);
            hash = 13 * hash + Objects.hashCode(this.attributeKey);
            hash = 13 * hash + Objects.hashCode(this.attributeValue);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AccessibilityItem other = (AccessibilityItem) obj;
            if (this.startFrame != other.startFrame) {
                return false;
            }
            if (this.endFrame != other.endFrame) {
                return false;
            }
            if (!Objects.equals(this.instanceName, other.instanceName)) {
                return false;
            }
            if (!Objects.equals(this.attributeKey, other.attributeKey)) {
                return false;
            }
            return Objects.equals(this.attributeValue, other.attributeValue);
        }

    }

    private static AccessibilityBag getAccessibilityFromPack(AbcIndexing abcIndex, ScriptPack pack) {
        int swfVersion = -1;
        if (pack.getOpenable() instanceof SWF) {
            swfVersion = ((SWF) pack.getOpenable()).version;
        }
        AccessibilityBag ret = new AccessibilityBag();
        int classIndex = getPackMainClassId(pack);
        if (classIndex > -1) {
            ABC abc = pack.abc;
            InstanceInfo instanceInfo = abc.instance_info.get(classIndex);
            int constructorMethodIndex = instanceInfo.iinit_index;
            MethodBody constructorBody = abc.findBody(constructorMethodIndex);
            try {
                List<MethodBody> callStack = new ArrayList<>();
                callStack.add(constructorBody);
                constructorBody.convert(swfVersion, callStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                List<String> allFramesAccessibilityTraitNames = new ArrayList<>();
                List<String> frameTraitNames = new ArrayList<>();
                if (constructorBody.convertedItems != null) {
                    for (int j = 0; j < constructorBody.convertedItems.size(); j++) {
                        GraphTargetItem ti = constructorBody.convertedItems.get(j);
                        if (ti instanceof CallPropertyAVM2Item) {
                            CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                            if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                if (propName.resolvedMultinameName != null
                                        && (propName.resolvedMultinameName.startsWith("__setAcc_")
                                        || propName.resolvedMultinameName.startsWith("__setTab_"))
                                        && callProp.arguments.isEmpty()) {
                                    allFramesAccessibilityTraitNames.add(propName.resolvedMultinameName);
                                }
                                if ("addFrameScript".equals(propName.resolvedMultinameName)) {
                                    for (int i = 0; i < callProp.arguments.size(); i += 2) {
                                        if (callProp.arguments.get(i) instanceof IntegerValueAVM2Item) {
                                            if (callProp.arguments.get(i + 1) instanceof GetLexAVM2Item) {
                                                GetLexAVM2Item lex = (GetLexAVM2Item) callProp.arguments.get(i + 1);
                                                frameTraitNames.add(lex.propertyName.getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, true));
                                            } else if (callProp.arguments.get(i + 1) instanceof GetPropertyAVM2Item) {
                                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) callProp.arguments.get(i + 1);
                                                if (getProp.object instanceof ThisAVM2Item) {
                                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                        FullMultinameAVM2Item framePropName = (FullMultinameAVM2Item) getProp.propertyName;
                                                        int multinameIndex = framePropName.multinameIndex;
                                                        frameTraitNames.add(abc.constants.getMultiname(multinameIndex).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), false, true));
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

                List<String> frameAccessibilityTraitNames = new ArrayList<>();
                List<String> frameRangeAccessibilityTraitNames = new ArrayList<>();
                for (Trait t : instanceInfo.instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        String traitName = t.getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false);
                        if ("__setTab_handler".equals(traitName)
                                || "__setAcc_handler".equals(traitName)) {
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                            if (abc.method_info.get(tm.method_info).param_types.length != 1) {
                                continue;
                            }
                            MethodBody traitBody = abc.findBody(tm.method_info);
                            List<MethodBody> traitCallStack = new ArrayList<>();
                            traitCallStack.add(traitBody);
                            traitBody.convert(swfVersion, traitCallStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                            if (traitBody.convertedItems == null) {
                                continue;
                            }
                            for (int j = 0; j < traitBody.convertedItems.size(); j++) {
                                GraphTargetItem ti = traitBody.convertedItems.get(j);
                                if (!(ti instanceof CallPropertyAVM2Item)) {
                                    continue;
                                }
                                CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                                if (!(callProp.propertyName instanceof FullMultinameAVM2Item)) {
                                    continue;
                                }
                                FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                if (propName.resolvedMultinameName != null
                                        && (propName.resolvedMultinameName.startsWith("__setAcc_")
                                        || propName.resolvedMultinameName.startsWith("__setTab_"))
                                        && callProp.arguments.size() == 1) {
                                    frameRangeAccessibilityTraitNames.add(propName.resolvedMultinameName);
                                }
                            }
                        }
                        if (frameTraitNames.contains(traitName)) {
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                            MethodBody traitBody = abc.findBody(tm.method_info);
                            List<MethodBody> traitCallStack = new ArrayList<>();
                            traitCallStack.add(traitBody);
                            traitBody.convert(swfVersion, traitCallStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                            if (traitBody.convertedItems != null) {
                                for (int j = 0; j < traitBody.convertedItems.size(); j++) {
                                    GraphTargetItem ti = traitBody.convertedItems.get(j);
                                    if (ti instanceof CallPropertyAVM2Item) {
                                        CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                                        if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                            FullMultinameAVM2Item propName = (FullMultinameAVM2Item) callProp.propertyName;
                                            if (propName.resolvedMultinameName != null
                                                    && (propName.resolvedMultinameName.startsWith("__setAcc_")
                                                    || propName.resolvedMultinameName.startsWith("__setTab_"))
                                                    && callProp.arguments.isEmpty()) {
                                                frameAccessibilityTraitNames.add(propName.resolvedMultinameName);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (Trait t : instanceInfo.instance_traits.traits) {
                    if (t instanceof TraitMethodGetterSetter) {
                        String traitName = t.getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false);
                        if (frameAccessibilityTraitNames.contains(traitName)) {
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                            MethodBody traitBody = abc.findBody(tm.method_info);
                            List<MethodBody> traitCallStack = new ArrayList<>();
                            traitCallStack.add(traitBody);
                            traitBody.convert(swfVersion, traitCallStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                            if (traitBody.convertedItems != null) {
                                if (!traitBody.convertedItems.isEmpty()) {
                                    if (traitBody.convertedItems.get(0) instanceof IfItem) {
                                        IfItem ifi = (IfItem) traitBody.convertedItems.get(0);
                                        if (ifi.expression instanceof OrItem) {
                                            OrItem orItem = (OrItem) ifi.expression;
                                            if (orItem.rightSide instanceof NeqAVM2Item) {
                                                NeqAVM2Item neq = (NeqAVM2Item) orItem.rightSide;
                                                if (neq.rightSide instanceof IntegerValueAVM2Item) {
                                                    IntegerValueAVM2Item iv = (IntegerValueAVM2Item) neq.rightSide;
                                                    int frame = (Integer) iv.getResult();
                                                    for (int j = 0; j < ifi.onTrue.size(); j++) {
                                                        GraphTargetItem ti = ifi.onTrue.get(j);
                                                        if (ti instanceof SetPropertyAVM2Item) {
                                                            if (ti.value instanceof ConstructPropAVM2Item) {
                                                                ConstructPropAVM2Item cons = (ConstructPropAVM2Item) ti.value;
                                                                if (cons.propertyName instanceof FullMultinameAVM2Item) {
                                                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) cons.propertyName;
                                                                    if ("AccessibilityProperties".equals(fm.resolvedMultinameName)) {
                                                                        continue;
                                                                    }
                                                                }
                                                            }
                                                            SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) ti;

                                                            if (setProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                                if ("tabIndex".equals(prop.resolvedMultinameName)) {
                                                                    GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                                                    if (parentGetProp.object instanceof ThisAVM2Item) {
                                                                        if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                            if (setProp.value instanceof IntegerValueAVM2Item) {
                                                                                FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                                                iv = (IntegerValueAVM2Item) setProp.value;
                                                                                ret.add(new AccessibilityItem(parentProp.resolvedMultinameName, "tabIndex", "" + iv.getResult(), frame));
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (setProp.object instanceof GetPropertyAVM2Item) {
                                                                GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                                                if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                    FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                                    if ("accessibilityProperties".equals(parentProp.resolvedMultinameName)) {
                                                                        if (parentGetProp.object instanceof GetPropertyAVM2Item) {
                                                                            GetPropertyAVM2Item parentParentGetProp = (GetPropertyAVM2Item) parentGetProp.object;
                                                                            if (parentParentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                                FullMultinameAVM2Item parentParentProp = (FullMultinameAVM2Item) parentParentGetProp.propertyName;
                                                                                if (parentParentProp.resolvedMultinameName != null) {
                                                                                    if (parentParentGetProp.object instanceof ThisAVM2Item) {
                                                                                        if (setProp.propertyName instanceof FullMultinameAVM2Item) {

                                                                                            FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                                                            String acProp = prop.resolvedMultinameName;

                                                                                            if (Arrays.asList("name", "description", "forceSimple", "noAutoLabeling", "shortcut").contains(acProp)) {
                                                                                                boolean invert = false;
                                                                                                if ("noAutoLabeling".equals(acProp)) {
                                                                                                    acProp = "autoLabeling";
                                                                                                    invert = true;
                                                                                                }
                                                                                                if ("name".equals(acProp)) {
                                                                                                    acProp = "accName";
                                                                                                }
                                                                                                String val = "";
                                                                                                if (setProp.value instanceof StringAVM2Item) {
                                                                                                    val = (String) ((StringAVM2Item) setProp.value).getResult();
                                                                                                }
                                                                                                if (setProp.value instanceof TrueItem) {
                                                                                                    val = invert ? "false" : "true";
                                                                                                }
                                                                                                if (setProp.value instanceof FalseItem) {
                                                                                                    val = invert ? "true" : "false";
                                                                                                }
                                                                                                ret.add(new AccessibilityItem(parentParentProp.resolvedMultinameName, acProp, val, frame));
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
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (frameRangeAccessibilityTraitNames.contains(traitName)) {
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                            MethodBody traitBody = abc.findBody(tm.method_info);
                            List<MethodBody> traitCallStack = new ArrayList<>();
                            traitCallStack.add(traitBody);
                            traitBody.convert(swfVersion, traitCallStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                            if (traitBody.convertedItems != null) {
                                if (!traitBody.convertedItems.isEmpty()) {
                                    if (traitBody.convertedItems.get(0) instanceof IfItem) {
                                        //if(this.MovieClip6 != null && param1 >= 1 && param1 <= 4 && (this.__setTabDict[this.MovieClip6] == undefined || !(int(this.__setTabDict[this.MovieClip6]) >= 1 && int(this.__setTabDict[this.MovieClip6]) <= 4)))
                                        IfItem ifi = (IfItem) traitBody.convertedItems.get(0);
                                        if (ifi.expression instanceof AndItem) {
                                            AndItem ai = (AndItem) ifi.expression;
                                            if (ai.leftSide instanceof AndItem) {
                                                AndItem ai2 = (AndItem) ai.leftSide;
                                                if (ai2.leftSide instanceof AndItem) {
                                                    AndItem ai3 = (AndItem) ai2.leftSide;
                                                    if (ai3.rightSide instanceof GeAVM2Item) {
                                                        GeAVM2Item ge = (GeAVM2Item) ai3.rightSide;
                                                        if (ge.rightSide instanceof IntegerValueAVM2Item) {
                                                            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) ge.rightSide;
                                                            int startFrame = (Integer) iv.getResult();
                                                            if (ai2.rightSide instanceof LeAVM2Item) {
                                                                LeAVM2Item le = (LeAVM2Item) ai2.rightSide;
                                                                if (le.rightSide instanceof IntegerValueAVM2Item) {
                                                                    iv = (IntegerValueAVM2Item) le.rightSide;
                                                                    int endFrame = (Integer) iv.getResult();
                                                                    for (int j = 0; j < ifi.onTrue.size(); j++) {
                                                                        GraphTargetItem ti = ifi.onTrue.get(j);
                                                                        if (ti instanceof SetPropertyAVM2Item) {
                                                                            if (ti.value instanceof ConstructPropAVM2Item) {
                                                                                ConstructPropAVM2Item cons = (ConstructPropAVM2Item) ti.value;
                                                                                if (cons.propertyName instanceof FullMultinameAVM2Item) {
                                                                                    FullMultinameAVM2Item fm = (FullMultinameAVM2Item) cons.propertyName;
                                                                                    if ("AccessibilityProperties".equals(fm.resolvedMultinameName)) {
                                                                                        continue;
                                                                                    }
                                                                                }
                                                                            }
                                                                            SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) ti;

                                                                            if (setProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                                FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                                                if ("tabIndex".equals(prop.resolvedMultinameName)) {
                                                                                    GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                                                                    if (parentGetProp.object instanceof ThisAVM2Item) {
                                                                                        if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                                            if (setProp.value instanceof IntegerValueAVM2Item) {
                                                                                                FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                                                                iv = (IntegerValueAVM2Item) setProp.value;
                                                                                                ret.add(new AccessibilityItem(parentProp.resolvedMultinameName, "tabIndex", "" + iv.getResult(), startFrame, endFrame));
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }

                                                                            if (setProp.object instanceof GetPropertyAVM2Item) {
                                                                                GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                                                                if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                                    FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                                                    if ("accessibilityProperties".equals(parentProp.resolvedMultinameName)) {
                                                                                        if (parentGetProp.object instanceof GetPropertyAVM2Item) {
                                                                                            GetPropertyAVM2Item parentParentGetProp = (GetPropertyAVM2Item) parentGetProp.object;
                                                                                            if (parentParentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                                                                FullMultinameAVM2Item parentParentProp = (FullMultinameAVM2Item) parentParentGetProp.propertyName;
                                                                                                if (parentParentProp.resolvedMultinameName != null) {
                                                                                                    if (parentParentGetProp.object instanceof ThisAVM2Item) {
                                                                                                        if (setProp.propertyName instanceof FullMultinameAVM2Item) {

                                                                                                            FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                                                                            String acProp = prop.resolvedMultinameName;

                                                                                                            if (Arrays.asList("name", "description", "forceSimple", "noAutoLabeling", "shortcut").contains(acProp)) {
                                                                                                                boolean invert = false;
                                                                                                                if ("noAutoLabeling".equals(acProp)) {
                                                                                                                    acProp = "autoLabeling";
                                                                                                                    invert = true;
                                                                                                                }
                                                                                                                if ("name".equals(acProp)) {
                                                                                                                    acProp = "accName";
                                                                                                                }
                                                                                                                String val = "";
                                                                                                                if (setProp.value instanceof StringAVM2Item) {
                                                                                                                    val = (String) ((StringAVM2Item) setProp.value).getResult();
                                                                                                                }
                                                                                                                if (setProp.value instanceof TrueItem) {
                                                                                                                    val = invert ? "false" : "true";
                                                                                                                }
                                                                                                                if (setProp.value instanceof FalseItem) {
                                                                                                                    val = invert ? "true" : "false";
                                                                                                                }
                                                                                                                ret.add(new AccessibilityItem(parentParentProp.resolvedMultinameName, acProp, val, startFrame, endFrame));
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
                            }
                        }
                    }
                }

                for (Trait t : instanceInfo.instance_traits.traits) {
                    if (allFramesAccessibilityTraitNames.contains(t.getName(abc).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false))) {
                        if (t instanceof TraitMethodGetterSetter) {
                            TraitMethodGetterSetter tm = (TraitMethodGetterSetter) t;
                            MethodBody traitBody = abc.findBody(tm.method_info);
                            List<MethodBody> traitCallStack = new ArrayList<>();
                            traitCallStack.add(traitBody);
                            traitBody.convert(swfVersion, traitCallStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());
                            if (traitBody.convertedItems != null) {
                                for (int j = 0; j < traitBody.convertedItems.size(); j++) {
                                    GraphTargetItem ti = traitBody.convertedItems.get(j);
                                    if (ti instanceof SetPropertyAVM2Item) {
                                        if (ti.value instanceof ConstructPropAVM2Item) {
                                            ConstructPropAVM2Item cons = (ConstructPropAVM2Item) ti.value;
                                            if (cons.propertyName instanceof FullMultinameAVM2Item) {
                                                FullMultinameAVM2Item fm = (FullMultinameAVM2Item) cons.propertyName;
                                                if ("AccessibilityProperties".equals(fm.resolvedMultinameName)) {
                                                    continue;
                                                }
                                            }
                                        }
                                        SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) ti;

                                        if (setProp.propertyName instanceof FullMultinameAVM2Item) {
                                            FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                            if ("tabIndex".equals(prop.resolvedMultinameName)) {
                                                GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                                if (parentGetProp.object instanceof ThisAVM2Item) {
                                                    if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                        if (setProp.value instanceof IntegerValueAVM2Item) {
                                                            FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) setProp.value;
                                                            ret.add(new AccessibilityItem(parentProp.resolvedMultinameName, "tabIndex", "" + iv.getResult()));
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (setProp.object instanceof GetPropertyAVM2Item) {
                                            GetPropertyAVM2Item parentGetProp = (GetPropertyAVM2Item) setProp.object;
                                            if (parentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                FullMultinameAVM2Item parentProp = (FullMultinameAVM2Item) parentGetProp.propertyName;
                                                if ("accessibilityProperties".equals(parentProp.resolvedMultinameName)) {
                                                    if (parentGetProp.object instanceof GetPropertyAVM2Item) {
                                                        GetPropertyAVM2Item parentParentGetProp = (GetPropertyAVM2Item) parentGetProp.object;
                                                        if (parentParentGetProp.propertyName instanceof FullMultinameAVM2Item) {
                                                            FullMultinameAVM2Item parentParentProp = (FullMultinameAVM2Item) parentParentGetProp.propertyName;
                                                            if (parentParentProp.resolvedMultinameName != null) {
                                                                if (parentParentGetProp.object instanceof ThisAVM2Item) {
                                                                    if (setProp.propertyName instanceof FullMultinameAVM2Item) {

                                                                        FullMultinameAVM2Item prop = (FullMultinameAVM2Item) setProp.propertyName;
                                                                        String acProp = prop.resolvedMultinameName;

                                                                        if (Arrays.asList("name", "description", "forceSimple", "noAutoLabeling", "shortcut").contains(acProp)) {
                                                                            boolean invert = false;
                                                                            if ("noAutoLabeling".equals(acProp)) {
                                                                                acProp = "autoLabeling";
                                                                                invert = true;
                                                                            }
                                                                            if ("name".equals(acProp)) {
                                                                                acProp = "accName";
                                                                            }
                                                                            String val = "";
                                                                            if (setProp.value instanceof StringAVM2Item) {
                                                                                val = (String) ((StringAVM2Item) setProp.value).getResult();
                                                                            }
                                                                            if (setProp.value instanceof TrueItem) {
                                                                                val = invert ? "false" : "true";
                                                                            }
                                                                            if (setProp.value instanceof FalseItem) {
                                                                                val = invert ? "true" : "false";
                                                                            }
                                                                            ret.add(new AccessibilityItem(parentParentProp.resolvedMultinameName, acProp, val));
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
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException ex) {
                //ignore
            }
        }
        return ret;
    }

    private static Map<Integer, String> getFrameScriptsFromPack(AbcIndexing abcIndex, ScriptPack pack) {

        int swfVersion = -1;
        if (pack.getOpenable() instanceof SWF) {
            swfVersion = ((SWF) pack.getOpenable()).version;
        }
        Map<Integer, String> ret = new HashMap<>();
        int classIndex = getPackMainClassId(pack);
        if (classIndex > -1) {
            ABC abc = pack.abc;
            InstanceInfo instanceInfo = abc.instance_info.get(classIndex);
            int constructorMethodIndex = instanceInfo.iinit_index;
            MethodBody constructorBody = abc.findBody(constructorMethodIndex);
            try {
                List<MethodBody> callStack = new ArrayList<>();
                callStack.add(constructorBody);
                constructorBody.convert(swfVersion, callStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, constructorMethodIndex, pack.scriptIndex, classIndex, abc, null, new ScopeStack(), GraphTextWriter.TRAIT_INSTANCE_INITIALIZER, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());

                Map<Integer, String> frameToTraitName = new HashMap<>();

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
                                    for (int i = 0; i < callProp.arguments.size(); i += 2) {
                                        if (callProp.arguments.get(i) instanceof IntegerValueAVM2Item) {
                                            IntegerValueAVM2Item frameItem = (IntegerValueAVM2Item) callProp.arguments.get(i);
                                            int frame = frameItem.intValue();
                                            if (callProp.arguments.get(i + 1) instanceof GetLexAVM2Item) {
                                                GetLexAVM2Item lex = (GetLexAVM2Item) callProp.arguments.get(i + 1);
                                                frameToTraitName.put(frame, lex.propertyName.getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false));
                                            } else if (callProp.arguments.get(i + 1) instanceof GetPropertyAVM2Item) {
                                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) callProp.arguments.get(i + 1);
                                                if (getProp.object instanceof ThisAVM2Item) {
                                                    if (getProp.propertyName instanceof FullMultinameAVM2Item) {
                                                        FullMultinameAVM2Item framePropName = (FullMultinameAVM2Item) getProp.propertyName;
                                                        int multinameIndex = framePropName.multinameIndex;
                                                        frameToTraitName.put(frame, abc.constants.getMultiname(multinameIndex).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false));
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
                Map<String, TraitMethodGetterSetter> multinameToMethodTrait = new HashMap<>();
                for (Trait trait : instanceInfo.instance_traits.traits) {
                    if (trait instanceof TraitMethodGetterSetter) {
                        multinameToMethodTrait.put(abc.constants.getMultiname(trait.name_index).getName(new LinkedHashSet<>(), abc, abc.constants, new ArrayList<>(), true, false), (TraitMethodGetterSetter) trait);
                    }
                }

                for (int frame : frameToTraitName.keySet()) {
                    String traitName = frameToTraitName.get(frame);
                    if (multinameToMethodTrait.containsKey(traitName)) {
                        TraitMethodGetterSetter methodTrait = multinameToMethodTrait.get(traitName);
                        int methodIndex = methodTrait.method_info;
                        MethodBody frameBody = abc.findBody(methodIndex);

                        StringBuilder scriptBuilder = new StringBuilder();
                        callStack = new ArrayList<>();
                        callStack.add(frameBody);

                        frameBody.convert(swfVersion, callStack, abcIndex, new ConvertData(), "??", ScriptExportMode.AS, false, methodIndex, pack.scriptIndex, classIndex, abc, methodTrait, new ScopeStack(), 0, new NulWriter(), new ArrayList<>(), new Traits(), true, new HashSet<>(), new ArrayList<>(), new LinkedHashSet<>());

                        if (frameBody.convertedItems != null) {
                            for (int i = 0; i < frameBody.convertedItems.size(); i++) {
                                GraphTargetItem ti = frameBody.convertedItems.get(i);
                                if (ti instanceof CallPropertyAVM2Item) {
                                    CallPropertyAVM2Item callProp = (CallPropertyAVM2Item) ti;
                                    if (callProp.propertyName instanceof FullMultinameAVM2Item) {
                                        FullMultinameAVM2Item fm = (FullMultinameAVM2Item) callProp.propertyName;
                                        if (fm.resolvedMultinameName != null) {
                                            if (fm.resolvedMultinameName.startsWith("__setTab_")
                                                    || fm.resolvedMultinameName.startsWith("__setAcc_")) {
                                                frameBody.convertedItems.remove(i);
                                                i--;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        StringBuilderTextWriter writer = new StringBuilderTextWriter(Configuration.getCodeFormatting(), scriptBuilder);
                        frameBody.toString(new LinkedHashSet<>(), swfVersion, callStack, abcIndex, "??", ScriptExportMode.AS, abc, methodTrait, writer, new ArrayList<>(), new HashSet<>());

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

    private boolean convertActionScriptLayer(Scene scene, String initClipScript, AbcIndexing abcIndex, ReadOnlyTagList timeLineTags, XFLXmlWriter writer, ScriptPack scriptPack) throws XMLStreamException {
        boolean hasScript = false;

        String script = initClipScript;
        int duration = 0;
        int frame = 0;
        if (!script.isEmpty()) {
            script = "#initclip\r\n" + script + "#endinitclip\r\n";
        }

        Map<Integer, String> frameToScriptMap = new HashMap<>();

        if (scriptPack != null) {
            frameToScriptMap = getFrameScriptsFromPack(abcIndex, scriptPack);
        }

        for (Tag t : timeLineTags) {
            if (t instanceof DoActionTag) {
                DoActionTag da = (DoActionTag) t;
                script += convertActionScript12(da);
            }
            if (frameToScriptMap.containsKey(scene.startFrame + frame)) {
                script += frameToScriptMap.get(scene.startFrame + frame);
                frameToScriptMap.remove(scene.startFrame + frame);
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

    private int convertLabelsLayers(ReadOnlyTagList timeLineTags, XFLXmlWriter writer) throws XMLStreamException {
        Map<Integer, List<FrameLabelTag>> frameToLabels = new HashMap<>();
        int frame = 0;
        int layerCount = 0;
        for (Tag t : timeLineTags) {
            if (t instanceof FrameLabelTag) {
                FrameLabelTag frameLabel = (FrameLabelTag) t;
                if (!frameToLabels.containsKey(frame)) {
                    frameToLabels.put(frame, new ArrayList<>());
                }
                frameToLabels.get(frame).add(frameLabel);
                if (frameToLabels.get(frame).size() > layerCount) {
                    layerCount = frameToLabels.get(frame).size();
                }
            } else if (t instanceof ShowFrameTag) {
                frame++;
            }
        }
        int frameCount = frame;

        for (int lay = 0; lay < layerCount; lay++) {
            writer.writeStartElement("DOMLayer", new String[]{"name", "Labels Layer" + (layerCount > 1 ? " " + (lay + 1) : ""), "color", randomOutlineColor()});
            writer.writeStartElement("frames");
            int duration = 0;
            for (int i = 0; i < frameCount; i++) {
                List<FrameLabelTag> frameLabels = frameToLabels.get(i);
                FrameLabelTag frameLabel = null;
                if (frameLabels != null) {
                    if (frameLabels.size() > lay) {
                        frameLabel = frameLabels.get(lay);
                    }
                }
                if (frameLabel == null) {
                    duration++;
                } else {
                    if (duration > 0) {
                        writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(i - duration)});
                        if (duration > 1) {
                            writer.writeAttribute("duration", duration);
                        }
                        writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                        writer.writeElementValue("elements", "");
                        writer.writeEndElement();
                    }
                    writer.writeStartElement("DOMFrame", new String[]{"index", Integer.toString(i)});
                    writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                    writer.writeAttribute("name", frameLabel.name);
                    if (frameLabel.namedAnchor) {
                        writer.writeAttribute("labelType", "anchor");
                        writer.writeAttribute("bookmark", true);
                    } else {
                        writer.writeAttribute("labelType", "name");
                    }
                    writer.writeElementValue("elements", "");
                    writer.writeEndElement();
                    duration = 0;
                }

            }
            writer.writeEndElement(); // frames
            writer.writeEndElement(); // DOMLayer
        }

        return layerCount;
    }

    private void convertSoundLayer(Scene scene, ReadOnlyTagList timeLineTags, HashMap<String, byte[]> files, XFLXmlWriter writer) throws XMLStreamException {
        int soundLayerIndex = 0;
        List<StartSoundTag> startSounds = new ArrayList<>();
        List<Integer> startSoundFrameNumbers = new ArrayList<>();
        List<SoundStreamFrameRange> soundStreamRanges = new ArrayList<>();
        int frame = 0;
        for (Tag t : timeLineTags) {
            if (t instanceof StartSoundTag) {
                StartSoundTag startSound = (StartSoundTag) t;
                SWF swf = startSound.getSwf();
                DefineSoundTag s = swf.getSound(startSound.soundId);
                if (s == null) {
                    logger.log(Level.WARNING, "Sound tag (ID={0}) was not found", startSound.soundId);
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

                for (SoundStreamFrameRange range : soundStreamHead.getRanges()) {
                    if (files.containsKey(range.getFlaExportName() + "." + soundStreamHead.getExportFormat().toString().toLowerCase())) { //Sound was really exported              
                        soundStreamRanges.add(range);
                    }
                }
            } else if (t instanceof ShowFrameTag) {
                frame++;
            }
        }

        for (int i = 0; i < soundStreamRanges.size(); i++) {
            if (soundStreamRanges.get(i).startFrame < scene.startFrame) {
                continue;
            }
            if (soundStreamRanges.get(i).startFrame >= scene.startFrame + frame) {
                continue;
            }

            writer.writeStartElement("DOMLayer", new String[]{"name", "Sound Layer " + (soundLayerIndex++), "color", randomOutlineColor()});
            writer.writeStartElement("frames");

            int startFrame = soundStreamRanges.get(i).startFrame - scene.startFrame;
            int duration = frame - startFrame;

            if (startFrame != 0) {
                // empty frames should be added
                convertFrame(false, null, null, 0, startFrame, "", "", writer, null);
            }

            convertFrame(false, soundStreamRanges.get(i), null, startFrame, duration, "", "", writer, null);

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
                convertFrame(false, null, null, 0, startFrame, "", "", writer, null);
            }

            convertFrame(false, null, startSounds.get(i), startFrame, duration, "", "", writer, null);

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

    private String getMembersToClassName(GraphTargetItem item) {
        List<String> ret = new ArrayList<>();
        while (item instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) item;
            if (!(mem.memberName instanceof DirectValueActionItem)) {
                return null;
            }
            DirectValueActionItem dv = ((DirectValueActionItem) mem.memberName);
            if (!dv.isString()) {
                return null;
            }
            ret.add(0, dv.getAsString());
            item = mem.object;
        }
        if (!(item instanceof GetVariableActionItem)) {
            return null;
        }
        GetVariableActionItem gv = (GetVariableActionItem) item;
        if (!(gv.name instanceof DirectValueActionItem)) {
            return null;
        }
        DirectValueActionItem dv = ((DirectValueActionItem) gv.name);
        if (!dv.isString()) {
            return null;
        }
        String varName = dv.getAsString();
        ret.add(0, varName);
        return String.join(".", ret);
    }

    private void addExtractedClip(
            Map<CharacterTag, ScriptPack> characterScriptPacks,
            Reference<Integer> lastItemIdNumber,
            Reference<Integer> lastImportedId,
            Map<CharacterTag, String> characterNameMap,
            ReadOnlyTagList timelineTags,
            int spriteId,
            XFLXmlWriter writer,
            SWF swf,
            Reference<Integer> nextClipId,
            List<CharacterTag> nonLibraryShapes,
            String backgroundColor,
            FLAVersion flaVersion,
            HashMap<String, byte[]> files,
            Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol,
            List<Integer> multiUsageMorphShapes,
            StatusStack statusStack,
            Map<CharacterTag, String> characterImportLinkageURL,
            Set<CharacterTag> characters
    ) throws XMLStreamException {
        XFLXmlWriter symbolStr = new XFLXmlWriter();

        extractMultilevelClips(characterScriptPacks, lastItemIdNumber, lastImportedId, characterNameMap, timelineTags, spriteId, writer, swf, nextClipId, nonLibraryShapes, backgroundColor, flaVersion, files, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);

        if (nextClipId.getVal() < 0) {
            nextClipId.setVal(swf.getNextCharacterId());
        } else {
            nextClipId.setVal(nextClipId.getVal() + 1);
        }

        int objectId = nextClipId.getVal();
        String itemId = generateItemId(lastItemIdNumber);
        symbolStr.writeStartElement("DOMSymbolItem", new String[]{
            "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
            "xmlns", "http://ns.adobe.com/xfl/2008/",
            "name", getMaskedSymbolName(objectId),
            "itemID", itemId,
            "lastModified", Long.toString(getTimestamp(swf))});
        symbolStr.writeAttribute("symbolType", "graphic");

        convertTimelines(characterScriptPacks, lastImportedId, characterNameMap, swf, swf.getAbcIndex(), null, objectId, "", nonLibraryShapes, timelineTags, timelineTags, getMaskedSymbolName(objectId), flaVersion, files, symbolStr, null, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);

        symbolStr.writeEndElement(); // DOMSymbolItem
        String symbolStr2 = prettyFormatXML(symbolStr.toString());
        String symbolFile = getMaskedSymbolName(objectId) + ".xml";
        files.put(symbolFile, Utf8Helper.getBytes(symbolStr2));

        writer.writeStartElement("Include", new String[]{"href", symbolFile});
        writer.writeAttribute("itemID", itemId);
        writer.writeAttribute("itemIcon", "1");
        writer.writeAttribute("loadImmediate", false);
        if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
            writer.writeAttribute("lastModified", getTimestamp(swf));
            //TODO: itemID="518de416-00000341"
        }
        writer.writeEndElement();

    }

    private boolean getMorphshapeTimeline(int morphShapeId, ReadOnlyTagList tags, List<Tag> outTimelineTags) {
        int morphDepth = -2;
        boolean onTrack = false;
        boolean wasOnTrack = false;
        int lastRatio = -1;
        for (Tag t : tags) {
            if (t instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) t;
                if (getMorphshapeTimeline(morphShapeId, sprite.getTags(), outTimelineTags)) {
                    return true;
                }
            }
        }
        for (Tag t : tags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                if (morphDepth == place.getDepth()
                        && place.getCharacterId() != -1
                        && place.getCharacterId() != morphShapeId) {
                    outTimelineTags.add(t);
                    onTrack = false;
                } else if ((morphDepth == -2 && place.getCharacterId() == morphShapeId) || morphDepth == place.getDepth()) {
                    morphDepth = place.getDepth();
                    if (onTrack && place.getRatio() < lastRatio) {
                        onTrack = false;
                    } else {
                        onTrack = true;
                        wasOnTrack = true;
                        outTimelineTags.add(t);
                        lastRatio = place.getRatio();
                    }
                }
            }
            if (t instanceof RemoveTag) {
                RemoveTag rem = (RemoveTag) t;
                if (rem.getDepth() == morphDepth) {
                    if (onTrack) {
                        outTimelineTags.add(t);
                    }
                    onTrack = false;
                }
            }
            if (t instanceof ShowFrameTag) {
                if (onTrack) {
                    outTimelineTags.add(t);
                }
                if (wasOnTrack && !onTrack) {
                    outTimelineTags.add(t);
                    break;
                }
            }
        }

        return !outTimelineTags.isEmpty();
    }

    private void extractMultiUsageMorphShapes(
            Map<CharacterTag, ScriptPack> characterScriptPacks,
            Reference<Integer> lastItemIdNumber,
            Reference<Integer> lastImportedId,
            Map<CharacterTag, String> characterNameMap,
            XFLXmlWriter writer,
            SWF swf,
            List<CharacterTag> nonLibraryShapes,
            FLAVersion flaVersion,
            HashMap<String, byte[]> files,
            List<Integer> multiUsageMorphShapes,
            StatusStack statusStack,
            Map<CharacterTag, String> characterImportLinkageURL,
            Set<CharacterTag> characters
    ) throws XMLStreamException {

        for (int objectId : multiUsageMorphShapes) {
            statusStack.pushStatus(swf.getCharacter(objectId).toString());

            String itemId = generateItemId(lastItemIdNumber);
            XFLXmlWriter symbolStr = new XFLXmlWriter();
            symbolStr.writeStartElement("DOMSymbolItem", new String[]{
                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
                "xmlns", "http://ns.adobe.com/xfl/2008/",
                "name", getSymbolName(lastImportedId, characterNameMap, swf, swf.getCharacter(objectId)),
                "itemID", itemId,
                "lastModified", Long.toString(getTimestamp(swf))});
            symbolStr.writeAttribute("symbolType", "graphic");

            List<Tag> timelineTags = new ArrayList<>();

            getMorphshapeTimeline(objectId, swf.getTags(), timelineTags);

            timelineTags = Helper.deepCopy(timelineTags);

            for (Tag t : timelineTags) {
                t.setSwf(swf);
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                    if (place.getMatrix() != null) {
                        place.setMatrix(new MATRIX());
                    }
                }
            }

            convertTimelines(characterScriptPacks, lastImportedId, characterNameMap, swf, swf.getAbcIndex(), null, objectId, "", nonLibraryShapes, swf.getTags(), new ReadOnlyTagList(timelineTags), getSymbolName(lastImportedId, characterNameMap, swf, swf.getCharacter(objectId)), flaVersion, files, symbolStr, null, new HashMap<>(), new ArrayList<>(), statusStack, characterImportLinkageURL, characters);

            symbolStr.writeEndElement(); // DOMSymbolItem
            String symbolStr2 = prettyFormatXML(symbolStr.toString());
            String symbolFile = getSymbolName(lastImportedId, characterNameMap, swf, swf.getCharacter(objectId)) + ".xml";
            files.put(symbolFile, Utf8Helper.getBytes(symbolStr2));

            writer.writeStartElement("Include", new String[]{"href", symbolFile});
            writer.writeAttribute("itemID", itemId);
            writer.writeAttribute("itemIcon", "1");
            writer.writeAttribute("loadImmediate", false);
            if (flaVersion.ordinal() >= FLAVersion.CS5_5.ordinal()) {
                writer.writeAttribute("lastModified", getTimestamp(swf));
                //TODO: itemID="518de416-00000341"
            }
            writer.writeEndElement();
            statusStack.popStatus();
        }
    }

    private void extractMultilevelClips(
            Map<CharacterTag, ScriptPack> characterScriptPacks,
            Reference<Integer> lastItemIdNumber,
            Reference<Integer> lastImportedId,
            Map<CharacterTag, String> characterNameMap,
            ReadOnlyTagList timelineTags,
            int spriteId,
            XFLXmlWriter writer,
            SWF swf,
            Reference<Integer> nextClipId,
            List<CharacterTag> nonLibraryShapes,
            String backgroundColor,
            FLAVersion flaVersion,
            HashMap<String, byte[]> files,
            Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol,
            List<Integer> multiUsageMorphShapes,
            StatusStack statusStack,
            Map<CharacterTag, String> characterImportLinkageURL,
            Set<CharacterTag> characters
    ) throws XMLStreamException {
        int f = 0;

        List<PlaceObjectTypeTag> clipPlaces = new ArrayList<>();
        Map<Integer, PlaceObjectTypeTag> depthToClipPlace = new HashMap<>();
        Map<PlaceObjectTypeTag, Integer> clipFinishFrames = new HashMap<>();
        Map<PlaceObjectTypeTag, Integer> clipStartFrames = new HashMap<>();

        Set<Integer> occupiedDepths = new HashSet<>();

        List<PlaceObjectTypeTag> clipPlacesInCurrentFrame = new ArrayList<>();
        Comparator<PlaceObjectTypeTag> placeComparator = new Comparator<PlaceObjectTypeTag>() {
            @Override
            public int compare(PlaceObjectTypeTag o1, PlaceObjectTypeTag o2) {
                int ret = o2.getClipDepth() - o1.getClipDepth();
                if (ret != 0) {
                    return ret;
                }
                return o1.getDepth() - o1.getDepth();
            }
        };

        int maxDepth = getMaxDepth(timelineTags);
        Tag lastTag = null;
        for (Tag t : timelineTags) {
            if (t instanceof ShowFrameTag) {
                clipPlacesInCurrentFrame.sort(placeComparator);
                clipPlaces.addAll(clipPlacesInCurrentFrame);
                clipPlacesInCurrentFrame.clear();
                f++;
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;

                if (po.flagMove() == occupiedDepths.contains(po.getDepth())) {

                    if (po.flagMove()) {
                        if (po.getCharacterId() != -1 || po.getClassName() != null) {
                            occupiedDepths.add(po.getDepth());
                        }
                    } else {
                        occupiedDepths.add(po.getDepth());
                    }

                    if (po.getClipDepth() > -1) {
                        clipStartFrames.put(po, f);
                        clipPlacesInCurrentFrame.add(po);
                        if (depthToClipPlace.containsKey(po.getDepth())) {
                            clipFinishFrames.put(depthToClipPlace.get(po.getDepth()), f - 1);
                        }
                        depthToClipPlace.put(po.getDepth(), po);
                    } else {
                        if (!po.flagMove() && depthToClipPlace.containsKey(po.getDepth())) {
                            clipFinishFrames.put(depthToClipPlace.get(po.getDepth()), f - 1);
                            depthToClipPlace.remove(po.getDepth());
                        }
                    }
                }
            }
            if (t instanceof RemoveTag) {
                RemoveTag re = (RemoveTag) t;
                occupiedDepths.remove(re.getDepth());

                if (depthToClipPlace.containsKey(re.getDepth())) {
                    clipFinishFrames.put(depthToClipPlace.get(re.getDepth()), f - 1);
                    depthToClipPlace.remove(re.getDepth());
                }
            }
            lastTag = t;
        }

        clipPlacesInCurrentFrame.sort(placeComparator);
        clipPlaces.addAll(clipPlacesInCurrentFrame);
        clipPlacesInCurrentFrame.clear();

        if (clipPlaces.isEmpty()) {
            return;
        }

        //Some sprites do not end with ShowFrame:
        if (lastTag != null && !(lastTag instanceof ShowFrameTag)) {
            f++;
        }

        int frameCount = f;

        if (!depthToClipPlace.isEmpty()) {
            for (PlaceObjectTypeTag po : depthToClipPlace.values()) {
                clipFinishFrames.put(po, frameCount - 1);
            }
        }

        Map<Integer, List<Integer>> depthToFramesList = new HashMap<>();
        for (int d = maxDepth; d >= 0; d--) {
            depthToFramesList.put(d, new ArrayList<>());
            for (int i = 0; i < frameCount; i++) {
                depthToFramesList.get(d).add(i);
            }
        }

        Map<Integer, Map<Integer, List<PlaceObjectTypeTag>>> frameToDepthToClips = new TreeMap<>();

        for (f = 0; f < frameCount; f++) {
            for (int d = 0; d < maxDepth; d++) {
                for (int p = 0; p < clipPlaces.size(); p++) {
                    PlaceObjectTypeTag po = clipPlaces.get(p);
                    int startFrame = clipStartFrames.get(po);
                    int finishFrame = clipFinishFrames.get(po);
                    if (f >= startFrame && f <= finishFrame) {
                        if (d >= po.getDepth() && d <= po.getClipDepth()) {
                            if (!frameToDepthToClips.containsKey(f)) {
                                frameToDepthToClips.put(f, new TreeMap<>());
                            }
                            if (!frameToDepthToClips.get(f).containsKey(d)) {
                                frameToDepthToClips.get(f).put(d, new ArrayList<>());
                            }
                            frameToDepthToClips.get(f).get(d).add(po);
                        }
                    }
                }
            }
        }

        Set<PlaceObjectTypeTag> delegatedPlaces = new HashSet<>();

        for (int fr : frameToDepthToClips.keySet()) {
            for (int d : frameToDepthToClips.get(fr).keySet()) {
                List<PlaceObjectTypeTag> places = frameToDepthToClips.get(fr).get(d);

                if (places.size() > 1) {
                    depthToFramesList.get(d).remove((Integer) fr);
                    PlaceObjectTypeTag firstPlace = places.get(0);
                    PlaceObjectTypeTag secondPlace = places.get(1);
                    if (delegatedPlaces.contains(secondPlace)) {
                        continue;
                    }
                    delegatedPlaces.add(secondPlace);

                    List<Tag> delegatedTimeline = new ArrayList<>();
                    f = 0;
                    boolean removed = false;
                    int numFrames = 0;
                    lastTag = null;
                    //Map<Integer, Integer> depthStates = new HashMap<>();
                    occupiedDepths.clear();

                    for (Tag t : timelineTags) {
                        if (f < fr) {
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                                if (place.flagMove() != occupiedDepths.contains(place.getDepth())) {
                                    continue;
                                }
                                if (place.getCharacterId() != -1 && place.getClassName() != null) {
                                    occupiedDepths.add(place.getDepth());
                                }
                            }
                            if (t instanceof RemoveTag) {
                                occupiedDepths.remove(((RemoveTag) t).getDepth());
                            }
                        }
                        if (f >= fr) {
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                                if (place.flagMove() == occupiedDepths.contains(place.getDepth())) {
                                    if (place.getCharacterId() != -1 && place.getClassName() != null) {
                                        occupiedDepths.add(place.getDepth());
                                    }
                                    if (place.getDepth() == secondPlace.getDepth()) {
                                        if (place.flagMove()) {
                                            removed = false;
                                        } else if (place.getClipDepth() == secondPlace.getClipDepth()) {
                                            removed = false;
                                            delegatedPlaces.add(place);
                                        } else {
                                            removed = true;
                                        }
                                    }
                                    if (!removed
                                            && place != firstPlace
                                            && place.getDepth() >= secondPlace.getDepth()
                                            && place.getDepth() <= secondPlace.getClipDepth()) {
                                        delegatedTimeline.add(place);
                                    }
                                }
                            }
                            if (t instanceof RemoveTag) {
                                RemoveTag rt = (RemoveTag) t;
                                if (rt.getDepth() == secondPlace.getDepth()) {
                                    removed = true;
                                }
                                occupiedDepths.remove(rt.getDepth());
                            }
                        }
                        lastTag = t;
                        if (t instanceof ShowFrameTag) {
                            if (f >= fr) {
                                delegatedTimeline.add(t);
                                numFrames++;
                            }
                            if (removed) {
                                break;
                            }
                            f++;
                        }
                    }
                    if (!(lastTag instanceof ShowFrameTag)) {
                        numFrames++;
                        ShowFrameTag showFrame = new ShowFrameTag(swf);
                        //set timelined?
                        delegatedTimeline.add(showFrame);
                    }
                    addExtractedClip(characterScriptPacks, lastItemIdNumber, lastImportedId, characterNameMap, new ReadOnlyTagList(delegatedTimeline), spriteId, writer, swf, nextClipId, nonLibraryShapes, backgroundColor, flaVersion, files, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
                    placeToMaskedSymbol.put(secondPlace, new MultiLevelClip(secondPlace, nextClipId.getVal(), numFrames));
                }
            }
        }
    }

    private class Scene {

        public int startFrame;
        public int endFrame;
        public String name;
        public ReadOnlyTagList timelineSubTags;

        public Scene(int startFrame, int endFrame, String name) {
            this.startFrame = startFrame;
            this.endFrame = endFrame;
            this.name = name;
        }
    }

    //Note: symbolId argument might be a virtual symbol like MaskedSymbol
    private void convertTimelines(Map<CharacterTag, ScriptPack> characterScriptPacks, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, AbcIndexing abcIndex, CharacterTag sprite, int symbolId, String linkageIdentifier, List<CharacterTag> nonLibraryShapes, ReadOnlyTagList tags, ReadOnlyTagList timelineTags, String spriteName, FLAVersion flaVersion, HashMap<String, byte[]> files, XFLXmlWriter writer, ScriptPack scriptPack, Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol, List<Integer> multiUsageMorphShapes, StatusStack statusStack, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters) throws XMLStreamException {
        ScriptPack characterScriptPack = sprite == null ? null : characterScriptPacks.containsKey(sprite) ? characterScriptPacks.get(sprite) : null;

        if (sprite == null && symbolId == -1) {
            String documentClass = swf.getDocumentClass();
            if (documentClass != null) {
                try {
                    List<ScriptPack> sps = swf.getScriptPacksByClassNames(Arrays.asList(documentClass));
                    if (sps != null && !sps.isEmpty()) {
                        characterScriptPack = sps.get(0);
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }

        AccessibilityBag accessibility = new AccessibilityBag();
        if (characterScriptPack != null) {
            accessibility = getAccessibilityFromPack(swf.getAbcIndex(), characterScriptPack);
        }

        String symbolName = getSymbolName(lastImportedId, characterNameMap, swf, sprite);
        List<String> classNames = new ArrayList<>();
        //Searches for Object.registerClass("linkageIdentifier",mypkg.MyClass);        
        ActionTreeOperation getRegisterClassOp = new ActionTreeOperation() {
            @Override
            public void run(List<GraphTargetItem> tree) {
                List<Integer> listToRemove = new ArrayList<>();
                List<String> newClassNames = new ArrayList<>();
                for (int i = 0; i < tree.size(); i++) {
                    GraphTargetItem item = tree.get(i);
                    if (!(item instanceof CallMethodActionItem)) {
                        continue;
                    }
                    CallMethodActionItem callMethod = (CallMethodActionItem) item;
                    if (!(callMethod.scriptObject instanceof GetVariableActionItem)) {
                        continue;
                    }
                    GetVariableActionItem methodObject = (GetVariableActionItem) callMethod.scriptObject;
                    if (!(methodObject.name instanceof DirectValueActionItem)) {
                        continue;
                    }
                    if (methodObject.name == null || !methodObject.name.toString().equals("Object")) {
                        continue;
                    }
                    if (!(callMethod.methodName instanceof DirectValueActionItem)) {
                        continue;
                    }
                    if (!callMethod.methodName.toString().equals("registerClass")) {
                        continue;
                    }
                    if (callMethod.arguments.size() != 2) {
                        continue;
                    }
                    if (!(callMethod.arguments.get(0) instanceof DirectValueActionItem)) {
                        continue;
                    }
                    if (linkageIdentifier != null && !linkageIdentifier.equals(callMethod.arguments.get(0).toString())) {
                        continue;
                    }
                    String className = getMembersToClassName(callMethod.arguments.get(1));
                    if (className == null) {
                        continue;
                    }
                    newClassNames.add(className);
                    listToRemove.add(i);
                }
                //There's only single one
                if (listToRemove.size() != 1) {
                    return;
                }
                classNames.add(newClassNames.get(0));
                tree.remove((int) listToRemove.get(0));
            }
        };
        List<ActionTreeOperation> treeOps = new ArrayList<>();
        treeOps.add(getRegisterClassOp);

        String initClipScript = "";
        if (sprite != null) {
            for (Tag t : tags) {
                if (t instanceof DoInitActionTag) {
                    DoInitActionTag dia = (DoInitActionTag) t;
                    if (dia.spriteId == sprite.getCharacterId()) {
                        initClipScript += convertActionScript12(dia, treeOps);
                    }
                }
            }
        }
        if (classNames.size() == 1) {
            writer.writeAttribute("linkageClassName", classNames.get(0));
        }
        if (symbolId == -1) {
            writer.writeStartElement("timelines");
        } else {
            writer.writeStartElement("timeline");
        }

        List<Scene> scenes = new ArrayList<>();

        DefineSceneAndFrameLabelDataTag sceneLabelTag = null;
        int fc = 0;
        boolean lastShowFrame = true;
        for (Tag t : timelineTags) {
            if (t instanceof DefineSceneAndFrameLabelDataTag) {
                sceneLabelTag = (DefineSceneAndFrameLabelDataTag) t;
            }

            if (t instanceof ShowFrameTag) {
                lastShowFrame = true;
                fc++;
            } else {
                lastShowFrame = false;
            }
        }
        if (!lastShowFrame) {
            fc++;
        }
        if (symbolId == -1) {
            if (sceneLabelTag != null) {
                for (int i = 0; i < sceneLabelTag.sceneOffsets.length; i++) {
                    scenes.add(new Scene(
                            (int) sceneLabelTag.sceneOffsets[i],
                            sceneLabelTag.sceneOffsets.length - 1 == i
                                    ? fc - 1 : (int) sceneLabelTag.sceneOffsets[i + 1] - 1,
                            sceneLabelTag.sceneNames[i]));
                }

                if (!scenes.isEmpty()) {
                    int sceneId = 0;
                    int f = 0;
                    List<Tag> sceneTags = new ArrayList<>();

                    //TODO: The sound stream heads needs better handling, like splitting them into two or something...
                    SoundStreamHeadTypeTag soundStreamHead = null;
                    SoundStreamHeadTypeTag soundStreamHeadThisScene = null;
                    for (Tag t : timelineTags) {
                        sceneTags.add(t);
                        if (t instanceof SoundStreamHeadTypeTag) {
                            soundStreamHeadThisScene = (SoundStreamHeadTypeTag) t;
                            soundStreamHead = soundStreamHeadThisScene;
                        }
                        if (t instanceof ShowFrameTag) {
                            f++;
                            if (f > scenes.get(sceneId).endFrame) {
                                if (soundStreamHead != null && soundStreamHeadThisScene == null) {
                                    sceneTags.add(0, soundStreamHead);
                                }
                                scenes.get(sceneId).timelineSubTags = new ReadOnlyTagList(sceneTags);
                                sceneTags = new ArrayList<>();
                                sceneId++;
                                soundStreamHeadThisScene = null;
                            }
                        }
                    }
                    if (!sceneTags.isEmpty()) {
                        if (soundStreamHead != null && soundStreamHeadThisScene == null) {
                            sceneTags.add(0, soundStreamHead);
                        }
                        scenes.get(sceneId).timelineSubTags = new ReadOnlyTagList(sceneTags);
                    }
                }

            }

            if (scenes.isEmpty()) {
                Scene scene = new Scene(0, fc - 1, "Scene 1");
                scene.timelineSubTags = timelineTags;
                scenes.add(scene);
            }
        } else {
            Scene scene = new Scene(0, fc - 1, spriteName);
            scene.timelineSubTags = timelineTags;
            scenes.add(scene);
        }

        for (Scene scene : scenes) {
            writer.writeStartElement("DOMTimeline", new String[]{"name", scene.name});
            writer.writeStartElement("layers");

            ReadOnlyTagList sceneTimelineTags = scene.timelineSubTags;
            int labelLayerCount = convertLabelsLayers(sceneTimelineTags, writer);
            boolean hasScript = convertActionScriptLayer(scene, initClipScript, abcIndex, sceneTimelineTags, writer, scriptPack);

            int index = 0;

            index += labelLayerCount;

            if (hasScript) {
                index++;
            }

            int maxDepth = getMaxDepth(sceneTimelineTags);

            List<Integer> clipFrameSplitters = new ArrayList<>();
            List<PlaceObjectTypeTag> clipPlaces = new ArrayList<>();
            int f = 0;

            Map<Integer, PlaceObjectTypeTag> depthToClipPlace = new HashMap<>();
            Map<PlaceObjectTypeTag, Integer> clipFinishFrames = new HashMap<>();
            Map<PlaceObjectTypeTag, Integer> clipStartFrames = new HashMap<>();
            Set<Integer> occupiedDepths = new HashSet<>();
            Tag lastTag = null;
            int tpos = 0;

            List<PlaceObjectTypeTag> clipPlacesInCurrentFrame = new ArrayList<>();
            Comparator<PlaceObjectTypeTag> placeComparator = new Comparator<PlaceObjectTypeTag>() {
                @Override
                public int compare(PlaceObjectTypeTag o1, PlaceObjectTypeTag o2) {
                    int ret = o2.getClipDepth() - o1.getClipDepth();
                    if (ret != 0) {
                        return ret;
                    }
                    return o1.getDepth() - o1.getDepth();
                }
            };

            for (Tag t : sceneTimelineTags) {
                if (t instanceof ShowFrameTag) {
                    clipPlacesInCurrentFrame.sort(placeComparator);
                    clipPlaces.addAll(clipPlacesInCurrentFrame);
                    clipPlacesInCurrentFrame.clear();
                    f++;
                }
                if (t instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                    if (po.flagMove() == occupiedDepths.contains(po.getDepth())) {
                        if (po.flagMove()) {
                            if (po.getCharacterId() != -1 || po.getClassName() != null) {
                                occupiedDepths.add(po.getDepth());
                            }
                        } else {
                            occupiedDepths.add(po.getDepth());
                        }

                        if (po.getClipDepth() > -1) {
                            clipFrameSplitters.add(f);
                            clipStartFrames.put(po, f);
                            clipPlacesInCurrentFrame.add(po);

                            if (depthToClipPlace.containsKey(po.getDepth())) {
                                clipFinishFrames.put(depthToClipPlace.get(po.getDepth()), f - 1);
                            }

                            depthToClipPlace.put(po.getDepth(), po);
                            for (int j = tpos + 1; j <= sceneTimelineTags.size(); j++) {
                                Tag t2 = sceneTimelineTags.get(j);
                                if (t2 instanceof PlaceObject2Tag) {
                                    PlaceObject2Tag pl = (PlaceObject2Tag) t2;
                                    int d = pl.getDepth();
                                    if (d >= po.getDepth() && d <= po.getClipDepth()) {
                                        //placeToFirstCharacterDepth.put(po, d);
                                    }
                                }
                                if (t2 instanceof ShowFrameTag) {
                                    break;
                                }

                            }
                        } else {
                            if (!po.flagMove() && depthToClipPlace.containsKey(po.getDepth())) {
                                clipFinishFrames.put(depthToClipPlace.get(po.getDepth()), f - 1);
                                depthToClipPlace.remove(po.getDepth());
                            }
                        }
                    }
                }
                if (t instanceof RemoveTag) {
                    RemoveTag re = (RemoveTag) t;
                    if (depthToClipPlace.containsKey(re.getDepth())) {
                        clipFinishFrames.put(depthToClipPlace.get(re.getDepth()), f - 1);
                        depthToClipPlace.remove(re.getDepth());
                    }
                    occupiedDepths.remove(re.getDepth());
                }
                lastTag = t;
                tpos++;
            }

            clipPlacesInCurrentFrame.sort(placeComparator);
            clipPlaces.addAll(clipPlacesInCurrentFrame);
            clipPlacesInCurrentFrame.clear();

            //Some sprites do not end with ShowFrame:
            if (lastTag != null && !(lastTag instanceof ShowFrameTag)) {
                f++;
            }

            int frameCount = f;

            if (!depthToClipPlace.isEmpty()) {
                for (PlaceObjectTypeTag po : depthToClipPlace.values()) {
                    clipFinishFrames.put(po, frameCount - 1);
                }
            }

            if (clipFrameSplitters.isEmpty() || clipFrameSplitters.get(0) != 0) {
                clipFrameSplitters.add(0, 0);
                clipPlaces.add(0, null);
            }

            clipFrameSplitters.add(frameCount);
            clipPlaces.add(null);

            Map<Integer, List<Integer>> depthToFramesList = new HashMap<>();
            for (int d = maxDepth; d >= 0; d--) {
                depthToFramesList.put(d, new ArrayList<>());
                for (int i = 0; i < frameCount; i++) {
                    depthToFramesList.get(d).add(i);
                }
            }

            Map<Integer, Map<Integer, List<PlaceObjectTypeTag>>> frameToDepthToClips = new TreeMap<>();

            for (f = 0; f < frameCount; f++) {
                for (int d = 0; d < maxDepth; d++) {
                    for (int p = 0; p < clipPlaces.size() - 1; p++) {
                        PlaceObjectTypeTag po = clipPlaces.get(p);
                        if (po == null) {
                            continue;
                        }
                        int startFrame = clipStartFrames.get(po);
                        int finishFrame = clipFinishFrames.get(po);
                        if (f >= startFrame && f <= finishFrame) {
                            if (d >= po.getDepth() && d <= po.getClipDepth()) {
                                if (!frameToDepthToClips.containsKey(f)) {
                                    frameToDepthToClips.put(f, new TreeMap<>());
                                }
                                if (!frameToDepthToClips.get(f).containsKey(d)) {
                                    frameToDepthToClips.get(f).put(d, new ArrayList<>());
                                }
                                frameToDepthToClips.get(f).get(d).add(po);
                            }
                        }
                    }
                }
            }

            Set<PlaceObjectTypeTag> multiLevelsPlaces = new HashSet<>();

            for (int fr : frameToDepthToClips.keySet()) {
                for (int d : frameToDepthToClips.get(fr).keySet()) {
                    List<PlaceObjectTypeTag> places = frameToDepthToClips.get(fr).get(d);
                    if (places.size() > 1) {
                        //depthToFramesList.get(d).remove((Integer) fr);
                        for (int i = 1; i < places.size(); i++) {
                            multiLevelsPlaces.add(places.get(i));
                        }
                    }
                }
            }

            Set<PlaceObjectTypeTag> handledClips = new HashSet<>();
            for (int d = maxDepth; d >= 0; d--) {
                loopp:
                for (int p = 0; p < clipPlaces.size() - 1; p++) {
                    PlaceObjectTypeTag po = clipPlaces.get(p);
                    /*if (po != null && multiLevelsPlaces.contains(po)) {
                        continue;
                    }*/
                    if (po != null && handledClips.contains(po)) {
                        continue;
                    }
                    if (po != null && po.getClipDepth() == d) {
                        int clipFrame = clipFrameSplitters.get(p);
                        int nextFrame = clipFinishFrames.get(po);
                        handledClips.add(po);

                        int lastFrame = nextFrame;
                        for (int p2 = 0; p2 < clipPlaces.size() - 1; p2++) {
                            PlaceObjectTypeTag po2 = clipPlaces.get(p2);
                            if (po2 == null) {
                                continue;
                            }
                            int clipFrame2 = clipFrameSplitters.get(p2);
                            int nextFrame2 = clipFinishFrames.get(po2);
                            if (lastFrame + 1 == clipFrame2
                                    && po.getDepth() == po2.getDepth()
                                    && po.getClipDepth() == po2.getClipDepth()
                                    && !multiLevelsPlaces.contains(po2)) {
                                lastFrame = nextFrame2;
                                handledClips.add(po2);
                            }
                        }

                        writer.writeStartElement("DOMLayer", new String[]{
                            "name", "Layer " + (index + 1) + (DEBUG_EXPORT_LAYER_DEPTHS ? " (depth " + po.getDepth() + " clipdepth:" + po.getClipDepth() + ")" : ""),
                            "color", randomOutlineColor(),
                            "layerType", "mask",
                            "locked", "true"});
                        convertFrames(accessibility, symbolName, lastImportedId, characterNameMap, swf, depthToFramesList.get(po.getDepth()), clipFrame, lastFrame, "", "", nonLibraryShapes, sceneTimelineTags, po.getDepth(), flaVersion, writer, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
                        writer.writeEndElement();

                        int parentIndex = index;
                        index++;

                        for (int fx = clipFrame; fx <= lastFrame; fx++) {
                            for (int nd = po.getClipDepth() - 1; nd > po.getDepth(); nd--) {
                                if (!depthToFramesList.containsKey(nd) || !depthToFramesList.get(nd).contains(fx)) {
                                    continue;
                                }
                                if (frameToDepthToClips.containsKey(fx)
                                        && frameToDepthToClips.get(fx).containsKey(nd)) {
                                    List<PlaceObjectTypeTag> clips = frameToDepthToClips.get(fx).get(nd);
                                    if (clips.size() > 1) {
                                        PlaceObjectTypeTag po2 = clips.get(1);
                                        if (handledClips.contains(po2)) {
                                            continue;
                                        }
                                        handledClips.add(po2);

                                        for (int ndx = po.getClipDepth() - 1; ndx > po2.getClipDepth(); ndx--) {
                                            boolean nonEmpty = writeLayer(accessibility, symbolName, lastImportedId, characterNameMap, swf, index, depthToFramesList.get(ndx), ndx, clipFrame, lastFrame, parentIndex, writer, nonLibraryShapes, sceneTimelineTags, flaVersion, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
                                            for (int i = clipFrame; i <= lastFrame; i++) {
                                                depthToFramesList.get(ndx).remove((Integer) i);
                                            }
                                            if (nonEmpty) {
                                                index++;
                                            }
                                        }

                                        MultiLevelClip mlc = placeToMaskedSymbol.get(po2);

                                        if (mlc != null) {
                                            writer.writeStartElement("DOMLayer", new String[]{
                                                "name", "Layer " + (index + 1) + (DEBUG_EXPORT_LAYER_DEPTHS ? " (depth " + po2.getDepth() + " clipdepth:" + po2.getClipDepth() + " maskedid:" + mlc.symbol + ")" : ""),
                                                "color", randomOutlineColor(),
                                                "parentLayerIndex", "" + parentIndex,
                                                "locked", "true"
                                            });
                                            writer.writeStartElement("frames");

                                            int clipFrame2 = 0;
                                            for (int p2 = 0; p2 < clipPlaces.size() - 1; p2++) {
                                                if (clipPlaces.get(p2) == po2) {
                                                    clipFrame2 = clipFrameSplitters.get(p2);
                                                }
                                            }
                                            //int nextFrame2 = clipFinishFrames.get(po2);

                                            if (clipFrame2 > 0) {
                                                writer.writeStartElement("DOMFrame", new String[]{
                                                    "index", "0",
                                                    "duration", "" + clipFrame2,
                                                    "keyMode", "" + KEY_MODE_NORMAL
                                                });
                                                writer.writeEmptyElement("elements");
                                                writer.writeEndElement();
                                            }

                                            writer.writeStartElement("DOMFrame", new String[]{
                                                "index", "" + clipFrame2,
                                                "duration", "" + mlc.numFrames,
                                                "keyMode", "" + KEY_MODE_NORMAL
                                            });
                                            writer.writeStartElement("elements");
                                            writer.writeStartElement("DOMSymbolInstance", new String[]{
                                                "libraryItemName", getMaskedSymbolName(mlc.symbol),
                                                "symbolType", "graphic",
                                                "loop", "loop"
                                            });

                                            writer.writeStartElement("matrix");
                                            convertMatrix(new MATRIX(), writer);
                                            writer.writeEndElement(); //matrix                                                                

                                            writer.writeStartElement("transformationPoint");
                                            writer.writeEmptyElement("Point");
                                            writer.writeEndElement(); //transformationPoint

                                            writer.writeEndElement(); //DOMSymbolInstance
                                            writer.writeEndElement(); //elements
                                            writer.writeEndElement(); //DOMFrame                                    

                                            writer.writeEndElement(); //frames

                                            writer.writeEndElement();
                                            index++;

                                            for (int nd2 = po2.getDepth(); nd2 <= po2.getClipDepth(); nd2++) {
                                                for (int i = clipFrame2; i < clipFrame2 + mlc.numFrames; i++) {
                                                    depthToFramesList.get(nd2).remove((Integer) i);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        for (int nd = po.getClipDepth() - 1; nd > po.getDepth(); nd--) {
                            boolean nonEmpty = writeLayer(accessibility, symbolName, lastImportedId, characterNameMap, swf, index, depthToFramesList.get(nd), nd, clipFrame, lastFrame, parentIndex, writer, nonLibraryShapes, sceneTimelineTags, flaVersion, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
                            for (int i = clipFrame; i <= lastFrame; i++) {
                                depthToFramesList.get(nd).remove((Integer) i);
                            }
                            if (nonEmpty) {
                                index++;
                            }
                        }

                        if (index == parentIndex + 1) {
                            //put at least one empty layer as masked, otherwise the mask layer will be visible
                            writer.writeStartElement("DOMLayer", new String[]{
                                "name", "Layer " + (index + 1),
                                "color", randomOutlineColor(),
                                "parentLayerIndex", "" + parentIndex,
                                "locked", "true"
                            });
                            writer.writeStartElement("frames");

                            writer.writeStartElement("DOMFrame");
                            writer.writeAttribute("index", 0);
                            writer.writeAttribute("duration", lastFrame + 1);
                            writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
                            writer.writeStartElement("elements");
                            writer.writeEndElement(); //elements
                            writer.writeEndElement(); //DOMFrame

                            writer.writeEndElement(); //frames
                            writer.writeEndElement(); //DOMLayer 
                            index++;
                        }
                        for (int i = clipFrame; i <= lastFrame; i++) {
                            depthToFramesList.get(po.getDepth()).remove((Integer) i);
                        }
                    }
                }

                boolean nonEmpty = writeLayer(accessibility, symbolName, lastImportedId, characterNameMap, swf, index, depthToFramesList.get(d), d, 0, Integer.MAX_VALUE, -1, writer, nonLibraryShapes, sceneTimelineTags, flaVersion, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
                if (nonEmpty) {
                    index++;
                }
            }

            if (index == 0) {
                writeEmptyLayer(writer, frameCount);
                index++;
            }

            convertSoundLayer(scene, sceneTimelineTags, files, writer);
            writer.writeEndElement(); //layers
            writer.writeEndElement(); //DOMTimeline
        }
        writer.writeEndElement(); //timeline/s        
    }

    private void writeEmptyLayer(XFLXmlWriter writer, int frameCount) throws XMLStreamException {
        writer.writeStartElement("DOMLayer", new String[]{
            "name", "Layer 1",
            "color", randomOutlineColor()
        });
        writer.writeAttribute("current", true);
        writer.writeAttribute("isSelected", true);
        writer.writeStartElement("frames");

        writer.writeStartElement("DOMFrame");
        writer.writeAttribute("index", 0);
        writer.writeAttribute("duration", frameCount);
        writer.writeAttribute("keyMode", KEY_MODE_NORMAL);
        writer.writeStartElement("elements");
        writer.writeEndElement(); //elements
        writer.writeEndElement(); //DOMFrame

        writer.writeEndElement(); //frames
        writer.writeEndElement(); //DOMLayer        
    }

    private boolean writeLayer(AccessibilityBag accessibility, String symbolName, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf, int index, List<Integer> onlyFrames, int d, int startFrame, int endFrame, int parentLayer, XFLXmlWriter writer, List<CharacterTag> nonLibraryShapes, ReadOnlyTagList timelineTags, FLAVersion flaVersion, List<Integer> multiUsageMorphShapes, StatusStack statusStack, Map<CharacterTag, String> characterImportLinkageURL, Set<CharacterTag> characters) throws XMLStreamException {
        XFLXmlWriter layerPrev = new XFLXmlWriter();
        statusStack.pushStatus("layer " + (index + 1));
        //System.err.println("- writing layer " + (index + 1) + (startFrame == 0 && endFrame == Integer.MAX_VALUE ? ", all frames":  ", frame " + startFrame + " to " + endFrame));
        layerPrev.writeStartElement("DOMLayer", new String[]{
            "name", "Layer " + (index + 1) + (DEBUG_EXPORT_LAYER_DEPTHS ? " (depth " + d + ")" : ""),
            "color", randomOutlineColor()
        });
        if (d == 1) {
            layerPrev.writeAttribute("current", true);
            layerPrev.writeAttribute("isSelected", true);
        }
        if (parentLayer != -1) {
            layerPrev.writeAttribute("parentLayerIndex", parentLayer);
            layerPrev.writeAttribute("locked", true);
        }
        layerPrev.writeCharacters(""); // todo honfika: hack to close start tag
        String layerAfter = "</DOMLayer>";
        int prevLength = writer.length();
        convertFrames(accessibility, symbolName, lastImportedId, characterNameMap, swf, onlyFrames, startFrame, endFrame, layerPrev.toString(), layerAfter, nonLibraryShapes, timelineTags, d, flaVersion, writer, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
        statusStack.popStatus();
        return writer.length() != prevLength;
    }

    private static void writeFile(AbortRetryIgnoreHandler handler, final byte[] data, final String file) throws IOException, InterruptedException {
        new RetryTask(() -> {
            File fileObj = new File(file);
            File dir = fileObj.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
        }, handler).run();
    }

    private static Map<CharacterTag, ScriptPack> getCharacterScriptPacks(SWF swf, Map<CharacterTag, String> characterClasses) {
        Map<CharacterTag, ScriptPack> ret = new HashMap<>();

        Map<String, CharacterTag> classToId = new HashMap<>();
        for (CharacterTag ct : characterClasses.keySet()) {
            classToId.put(characterClasses.get(ct), ct);
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

    private static Map<CharacterTag, String> getCharacterClasses(Set<CharacterTag> characters) {
        Map<CharacterTag, String> ret = new HashMap<>();
        for (CharacterTag ct : characters) {
            Set<String> classes = ct.getClassNames();
            if (classes.size() == 1) {
                ret.put(ct, classes.iterator().next());
            }
        }

        //TODO: handle multiple classes assigned to same character (Can happen when Embed tag used with identical file)
        return ret;
    }

    private static Map<CharacterTag, String> getCharacterVariables(ReadOnlyTagList tags) {
        Map<CharacterTag, String> ret = new IdentityHashMap<>();
        for (Tag t : tags) {
            if (t instanceof ExportAssetsTag) {
                ExportAssetsTag ea = (ExportAssetsTag) t;
                for (int i = 0; i < ea.tags.size(); i++) {
                    CharacterTag ct = ea.getSwf().getCharacter(ea.tags.get(i));
                    if (!ret.containsKey(ct)) {
                        ret.put(ct, ea.names.get(i));
                    }
                }
            }
            if (t instanceof ImportTag) {
                ImportTag it = (ImportTag) t;
                Map<Integer, String> assets = it.getAssets();
                for (int chid : assets.keySet()) {
                    String importName = assets.get(chid);
                    CharacterTag cht = t.getSwf().getCharacter(chid);
                    ret.put(cht, importName);
                }
            }
        }
        return ret;
    }

    private static void convertText(int frame, AccessibilityBag accessibility, String instanceName, TextTag tag, MATRIX m, List<FILTER> filters, XFLXmlWriter writer, Map<CharacterTag, String> characterImportLinkageURL, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, Set<CharacterTag> characters) throws XMLStreamException {
        MATRIX matrix = new MATRIX(m);
        CSMSettingsTag csmts = null;
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
            if (t instanceof CSMSettingsTag) {
                CSMSettingsTag c = (CSMSettingsTag) t;
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
                    FontTag ft = rec.getFont(swf);
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
                    font = ((Tag) tag).getSwf().getFont(fontId);

                    if (font != null) {
                        DefineFontNameTag dfn = (DefineFontNameTag) font.getSwf().getCharacterIdTag(font.getCharacterId(), DefineFontNameTag.ID);
                        if (dfn != null) {
                            fontName = dfn.fontName;
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

                    if (font != null && characterImportLinkageURL.containsKey(font)) {
                        psFontName = getSymbolName(lastImportedId, characterNameMap, swf, font, "Font") + "*";
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
            FontTag ft = det.getSwf().getFont(det.fontId);
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
                Map<String, String> accessibilityMap = accessibility.getAttributes(instanceName, frame + 1);
                if (!accessibilityMap.isEmpty()) {
                    writer.writeAttribute("hasAccessibleData", "true");
                    for (String acKey : accessibilityMap.keySet()) {
                        writer.writeAttribute(acKey, accessibilityMap.get(acKey));
                    }
                }
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
            Matrix matrix2 = new Matrix(matrix);
            matrix2 = matrix2.preConcatenate(Matrix.getTranslateInstance(det.bounds.Xmin + 40, det.bounds.Ymin + 40)); // 40 is magic value, I don't know why but it's there
            convertMatrix(matrix2.toMATRIX(), writer);
            writer.writeEndElement();
            writer.writeStartElement("textRuns");
            String txt = "";
            if (det.hasText) {
                txt = det.initialText;
            }

            if (det.html) {
                writer.writeCharactersRaw(convertHTMLText(characters, det, txt, characterImportLinkageURL, lastImportedId, characterNameMap, swf));
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
                    if (ft != null) {
                        DefineFontNameTag dfn = (DefineFontNameTag) ft.getSwf().getCharacterIdTag(ft.getCharacterId(), DefineFontNameTag.ID);
                        if (dfn != null) {
                            fontName = dfn.fontName;
                        }

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
                        if (characterImportLinkageURL.containsKey(ft)) {
                            fontFace = getSymbolName(lastImportedId, characterNameMap, swf, ft, "Font") + "*";
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

                writer.writeAttribute("autoKern", "false");
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

    private SoundStreamHeadTypeTag getFirstNonEmptySoundStreamHead(ReadOnlyTagList tags, Reference<ReadOnlyTagList> foundTags) {
        for (Tag t : tags) {
            if (t instanceof SoundStreamHeadTypeTag) {
                foundTags.setVal(tags);
                SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) t;
                if (!head.getRanges().isEmpty()) {
                    return head;
                }
            }
            if (t instanceof DefineSpriteTag) {
                SoundStreamHeadTypeTag st = getFirstNonEmptySoundStreamHead(((DefineSpriteTag) t).getTags(), foundTags);
                if (st != null) {
                    return st;
                }
            }
        }
        return null;
    }

    /**
     * Converts SWF to FLA/XFL
     *
     * @param handler AbortRetryIgnoreHandler
     * @param swf SWF to convert
     * @param swfFileName SWF file name
     * @param outfile Output file name
     * @param settings Export settings
     * @param generator Generator name
     * @param generatorVerName Generator version name
     * @param generatorVersion Generator version
     * @param parallel Parallel conversion
     * @param flaVersion FLA version
     * @param progressListener Progress listener
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public void convertSWF(AbortRetryIgnoreHandler handler, SWF swf, String swfFileName, String outfile, XFLExportSettings settings, String generator, String generatorVerName, String generatorVersion, boolean parallel, FLAVersion flaVersion, ProgressListener progressListener) throws IOException, InterruptedException {
        FlaFormatVersion cbfFlaVersion = null;

        String xflVersion = flaVersion.xflVersion();

        if (flaVersion.getCfbFlaVersion() != null) {
            cbfFlaVersion = flaVersion.getCfbFlaVersion();
            xflVersion = FLAVersion.CS5.xflVersion();
        }

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
        File flaFile = new File(outfile);  //c:/mydir/myfile.fla                      
        String baseName = flaFile.getName();
        if (baseName.contains(".")) {
            baseName = baseName.substring(0, baseName.lastIndexOf('.')); //myfile
        }

        File scriptsDir = flaFile.getParentFile(); //c:/mydir                

        Path.createDirectorySafe(scriptsDir);

        File xflDataDir = null;
        String xflFile = null;
        if (!settings.compressed && cbfFlaVersion == null) {
            xflDataDir = new File(Path.combine(flaFile.getParentFile().getAbsolutePath(), baseName)); //c:/mydir/myfile/
            xflFile = Path.combine(xflDataDir.getAbsolutePath(), baseName + ".xfl"); // c:/mydir/myfile.xfl
            Path.createDirectorySafe(xflDataDir);
        }

        final HashMap<String, byte[]> files = new HashMap<>();
        final HashMap<String, byte[]> datfiles = new HashMap<>();
        List<Integer> multiUsageMorphShapes = getMultiUsageMorphShapes(swf.getTags());
        List<CharacterTag> nonLibraryShapes = getNonLibraryShapes(swf.getTags());

        Set<CharacterTag> characters = getCharactersAndAllDependent(swf);
        Map<CharacterTag, String> characterClasses = getCharacterClasses(characters);
        String documentClass = swf.getDocumentClass();
        Map<CharacterTag, ScriptPack> characterScriptPacks = getCharacterScriptPacks(swf, characterClasses);
        Map<CharacterTag, String> characterVariables = getCharacterVariables(swf.getTags());
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

            ScriptPack documentScriptPack = null;
            if (documentClass != null) {
                List<ScriptPack> packs = swf.getScriptPacksByClassNames(Arrays.asList(documentClass));
                documentScriptPack = packs.isEmpty() ? null : packs.get(0);
            }

            domDocument.writeStartElement("DOMDocument", new String[]{
                "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
                "xmlns", "http://ns.adobe.com/xfl/2008/",
                "currentTimeline", "1",
                "xflVersion", xflVersion,
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
                "frameRate", doubleToString(swf.frameRate)
            });

            if (documentScriptPack != null) {
                Map<String, String> accessibility = getRootAccessibilityFromPack(swf.getAbcIndex(), documentScriptPack);
                if (!accessibility.isEmpty()) {
                    domDocument.writeAttribute("hasAccessibleData", "true");
                    for (String acKey : accessibility.keySet()) {
                        domDocument.writeAttribute(acKey, accessibility.get(acKey));
                    }
                }
            }

            if (Double.compare(width, 550) != 0) {
                domDocument.writeAttribute("width", doubleToString(width));

            }
            if (Double.compare(height, 400) != 0) {
                domDocument.writeAttribute("height", doubleToString(height));
            }

            Map<PlaceObjectTypeTag, MultiLevelClip> placeToMaskedSymbol = new HashMap<>();

            StatusStack statusStack = new StatusStack(progressListener);

            Reference<Integer> lastImportedId = new Reference<>(0);
            Map<CharacterTag, String> characterNameMap = new IdentityHashMap<>();

            Set<CharacterTag> charactersExportedInFirstFrame = new LinkedIdentityHashSet<>();
            Map<CharacterTag, String> characterImportLinkageURL = new IdentityHashMap<>();
            Reference<Integer> lastItemIdNumber = new Reference<>(0);
            int frame = 1;
            for (Tag tag : swf.getTags()) {
                if (tag instanceof ImportTag) {
                    ImportTag it = (ImportTag) tag;
                    Map<Integer, String> assets = it.getAssets();
                    for (int chid : assets.keySet()) {
                        CharacterTag cht = swf.getCharacter(chid);
                        characterImportLinkageURL.put(cht, it.getUrl());
                    }
                }
                if (frame == 1) {
                    if (tag instanceof ExportAssetsTag) {
                        ExportAssetsTag et = (ExportAssetsTag) tag;
                        for (int id : et.tags) {
                            CharacterTag ct = swf.getCharacter(id);
                            if (ct != null) {
                                charactersExportedInFirstFrame.add(ct);
                            }
                        }
                    }
                    if (tag instanceof SymbolClassTag) {
                        SymbolClassTag sc = (SymbolClassTag) tag;
                        for (int id : sc.tags) {
                            if (id == 0) { //document class
                                continue;
                            }
                            CharacterTag ct = swf.getCharacter(id);
                            if (ct != null) {
                                charactersExportedInFirstFrame.add(ct);
                            }
                        }
                    }
                }
                if (tag instanceof ShowFrameTag) {
                    frame++;
                }
            }

            for (CharacterTag ct : characters) {
                String cls = characterClasses.get(ct);
                if (cls != null) {
                    String sourceUrl = swf.getClassSourceUrl(cls);
                    if (sourceUrl != null) {
                        characterImportLinkageURL.put(ct, sourceUrl);
                    }
                }
            }
            convertFonts(lastItemIdNumber, lastImportedId, characterNameMap, swf, characters, domDocument, statusStack, characterVariables, characterClasses, charactersExportedInFirstFrame, characterImportLinkageURL);

            convertLibrary(lastItemIdNumber, charactersExportedInFirstFrame, characterImportLinkageURL, characters, lastImportedId, characterNameMap, swf, characterVariables, characterClasses, characterScriptPacks, nonLibraryShapes, backgroundColor, swf.getTags(), files, datfiles, flaVersion, domDocument, placeToMaskedSymbol, multiUsageMorphShapes, statusStack);

            //domDocument.writeStartElement("timelines");            
            statusStack.pushStatus("main timeline");
            convertTimelines(characterScriptPacks, lastImportedId, characterNameMap, swf, swf.getAbcIndex(), null, -1, null, nonLibraryShapes, swf.getTags(), swf.getTags(), null, flaVersion, files, domDocument, documentScriptPack, placeToMaskedSymbol, multiUsageMorphShapes, statusStack, characterImportLinkageURL, characters);
            statusStack.popStatus();
            //domDocument.writeEndElement();

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
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        String domDocumentStr = prettyFormatXML(domDocument.toString());

        if (settings.exportScript) {
            for (Tag t : swf.getTags()) {
                if (t instanceof DoInitActionTag) {
                    DoInitActionTag dia = (DoInitActionTag) t;
                    int chid = dia.getCharacterId();
                    CharacterTag character = swf.getCharacter(chid);
                    if (character instanceof DefineSpriteTag) {
                        DefineSpriteTag sprite = (DefineSpriteTag) character;
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
                            File cdir = new File(scriptsDir.getAbsolutePath() + File.separator + expDir);
                            Path.createDirectorySafe(cdir);
                            writeFile(handler, Utf8Helper.getBytes(data), scriptsDir.getAbsolutePath() + File.separator + expPath + ".as");
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
            Reference<ReadOnlyTagList> tagsRef = new Reference<>(null);
            SoundStreamHeadTypeTag shead = getFirstNonEmptySoundStreamHead(swf.getTags(), tagsRef);
            if (shead == null) {
                publishSettings.writeElementValue("StreamFormat", 0);
                publishSettings.writeElementValue("StreamCompress", 7);
            } else {
                byte[] soundData = SWFInputStream.BYTE_ARRAY_EMPTY;
                int soundFormat = shead.getSoundFormatId();
                int soundRate = shead.getSoundRate();
                boolean soundType = shead.getSoundType();
                boolean found = false;
                for (Tag t : tagsRef.getVal()) {
                    if (found && (t instanceof SoundStreamBlockTag)) {
                        SoundStreamBlockTag bl = (SoundStreamBlockTag) t;
                        soundData = bl.streamSoundData.getRangeData();
                        break;
                    }
                    if (t == shead) {
                        found = true;
                    }
                }

                int streamFormat = 0;
                int streamCompress = 0;
                if ((soundFormat == SoundFormat.FORMAT_ADPCM)
                        || (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN)
                        || (soundFormat == SoundFormat.FORMAT_UNCOMPRESSED_NATIVE_ENDIAN)) {
                    if (soundType) { //stereo
                        streamFormat += 1;
                    }
                    switch (soundRate) {
                        case 0:
                            streamFormat += 0;
                            break;
                        case 1:
                            streamFormat += 2;
                            break;
                        case 2:
                            streamFormat += 4;
                            break;
                        case 3:
                            streamFormat += 6;
                            break;
                    }
                }
                if (soundFormat == SoundFormat.FORMAT_SPEEX) {
                    streamCompress = 18;
                }
                if (soundFormat == SoundFormat.FORMAT_ADPCM) {
                    try {
                        SWFInputStream sis = new SWFInputStream(swf, soundData);
                        int adpcmCodeSize = (int) sis.readUB(2, "adpcmCodeSize");
                        streamCompress = 2 + adpcmCodeSize;
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                if (soundFormat == SoundFormat.FORMAT_MP3) {
                    streamFormat = -1;
                    if (!soundType) { //mono
                        streamFormat += 1;
                    }
                    streamFormat += 4; //quality best, medium = +2
                    if (soundData == null) {
                        streamCompress = 17;
                    } else {
                        try {
                            SWFInputStream sis = new SWFInputStream(swf, soundData);
                            MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
                            if (!s.frames.isEmpty()) {
                                MP3FRAME frame = s.frames.get(0);
                                int bitRate = frame.getBitRate() / 1000;

                                switch (bitRate) {
                                    case 8:
                                        streamCompress = 6;
                                        break;
                                    case 16:
                                        streamCompress = 7;
                                        break;
                                    case 20:
                                        streamCompress = 8;
                                        break;
                                    case 24:
                                        streamCompress = 9;
                                        break;
                                    case 32:
                                        streamCompress = 10;
                                        break;
                                    case 48:
                                        streamCompress = 11;
                                        break;
                                    case 56:
                                        streamCompress = 12;
                                        break;
                                    case 64:
                                        streamCompress = 13;
                                        break;
                                    case 80:
                                        streamCompress = 14;
                                        break;
                                    case 112:
                                        streamCompress = 15;
                                        break;
                                    case 128:
                                        streamCompress = 16;
                                        break;
                                    case 160:
                                        streamCompress = 17;
                                        break;
                                    default:
                                        streamCompress = 17;
                                        break;
                                }
                            }
                        } catch (IOException | IndexOutOfBoundsException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
                publishSettings.writeElementValue("StreamFormat", streamFormat);
                publishSettings.writeElementValue("StreamCompress", streamCompress);
            }
            publishSettings.writeElementValue("EventFormat", 0);
            publishSettings.writeElementValue("EventCompress", 7);
            publishSettings.writeElementValue("OverrideSounds", 0);
            publishSettings.writeElementValue("Version", flaSwfVersion);
            publishSettings.writeElementValue("ExternalPlayer", "FlashPlayer" + FlashPlayerVersion.getFlashPlayerBySwfVersion(flaSwfVersion));
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
            publishSettings.writeElementValue("DocumentClass", documentClass == null ? "" : documentClass);
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

        String zipfile = outfile;

        if (settings.compressed || cbfFlaVersion != null) {
            final String domDocumentF = domDocumentStr;
            final String publishSettingsF = publishSettingsStr;

            if (cbfFlaVersion != null) {
                zipfile = File.createTempFile("ffdec_fla_export", ".fla").getAbsolutePath();
            }

            final String outfileF = zipfile;
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
                        byte[] data = datfiles.get(fileName);
                        if (data.length == 0) {
                            continue;
                        }
                        out.putNextEntry(new ZipEntry("bin/" + fileName));
                        out.write(data);
                    }
                }
            }, handler).run();

        } else {
            Path.createDirectorySafe(xflDataDir);
            writeFile(handler, Utf8Helper.getBytes(domDocumentStr), xflDataDir.getAbsolutePath() + File.separator + "DOMDocument.xml");
            writeFile(handler, Utf8Helper.getBytes(publishSettingsStr), xflDataDir.getAbsolutePath() + File.separator + "PublishSettings.xml");
            File libraryDir = new File(xflDataDir.getAbsolutePath() + File.separator + "LIBRARY");
            libraryDir.mkdir();
            File binDir = new File(xflDataDir.getAbsolutePath() + File.separator + "bin");
            binDir.mkdir();
            for (String fileName : files.keySet()) {
                writeFile(handler, files.get(fileName), libraryDir.getAbsolutePath() + File.separator + fileName);
            }
            for (String fileName : datfiles.keySet()) {
                byte[] data = datfiles.get(fileName);
                if (data.length == 0) {
                    continue;
                }
                writeFile(handler, data, binDir.getAbsolutePath() + File.separator + fileName);
            }
            writeFile(handler, Utf8Helper.getBytes("PROXY-CS5"), xflFile);
        }
        if (useAS3 && settings.exportScript) {
            try {
                ScriptExportSettings scriptExportSettings = new ScriptExportSettings(ScriptExportMode.AS, false, true, false, true, true, "/_assets/", Configuration.linkAllClasses.get(), true);
                swf.exportActionScript(handler, scriptsDir.getAbsolutePath(), scriptExportSettings, parallel, null);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error during ActionScript3 export", ex);
            }
        }

        if (cbfFlaVersion != null) {
            if (progressListener != null) {
                progressListener.status("Converting to " + cbfFlaVersion + " ...");
            }
            try {
                InputStorageInterface inputStorage = new ZippedInputStorage(new File(zipfile));
                OutputStorageInterface outputStorage = new CfbOutputStorage(new File(outfile));

                FlaConverter contentsGenerator = new FlaConverter(cbfFlaVersion, swf.getCharset());
                contentsGenerator.convert(inputStorage, outputStorage);
                inputStorage.close();
                outputStorage.close();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error while converting to old format", ex);
            } finally {
                new File(zipfile).delete();
            }
        }

    }


    private static void convertAdjustColorFilter(COLORMATRIXFILTER filter, XFLXmlWriter writer) throws XMLStreamException {
        ColorMatrixConvertor colorMatrixConvertor = new ColorMatrixConvertor(filter.matrix);
        
        writer.writeEmptyElement("AdjustColorFilter", new String[]{
            "brightness", Integer.toString(colorMatrixConvertor.getBrightness()),
            "contrast", Integer.toString(colorMatrixConvertor.getContrast()),
            "saturation", Integer.toString(colorMatrixConvertor.getSaturation()),
            "hue", Integer.toString(colorMatrixConvertor.getHue())});
    }

    private static String convertHTMLText(Set<CharacterTag> characterTags, DefineEditTextTag det, String html, Map<CharacterTag, String> characterImportLinkageURL, Reference<Integer> lastImportedId, Map<CharacterTag, String> characterNameMap, SWF swf) {
        HTMLTextParser tparser = new HTMLTextParser(characterTags, det, characterImportLinkageURL, lastImportedId, characterNameMap, swf);
        XMLReader parser;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser sparser = factory.newSAXParser();
            parser = sparser.getXMLReader();
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
        } catch (SAXException | IOException | ParserConfigurationException e) {
            logger.log(Level.SEVERE, "Error while converting HTML", e);
        }
        return tparser.result.toString();
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static String generateItemId(Reference<Integer> lastItemIdNumber) {
        lastItemIdNumber.setVal(lastItemIdNumber.getVal() + 1);
        String epochHex = String.format("%1$08x", Math.round(System.currentTimeMillis() / 1000));
        String numberHex = String.format("%1$08x", lastItemIdNumber.getVal());
        return epochHex + "-" + numberHex;
    }

    private static class HTMLTextParser extends DefaultHandler {

        public XFLXmlWriter result = new XFLXmlWriter();

        private String fontFace = "";

        private String color = "";

        private int colorAlpha = 255;

        private boolean autoKern = false;

        private int size = -1;

        private int indent = -1;

        private int leftMargin = -1;

        private int rightMargin = -1;

        private int lineSpacing = -1;

        private double letterSpacing = 0;

        private String alignment = null;

        private final Set<CharacterTag> characterTags;

        private boolean bold = false;

        private boolean italic = false;

        private boolean underline = false;

        private boolean li = false;

        private String url = null;

        private String target = null;

        private Stack<Double> fontLetterSpacingStack = new Stack<>();
        private Stack<Integer> fontSizeStack = new Stack<>();
        private Stack<String> fontFaceStack = new Stack<>();
        private Stack<String> fontColorStack = new Stack<>();
        private Stack<Integer> fontColorAlphaStack = new Stack<>();
        private Stack<Boolean> fontKerningStack = new Stack<>();
        private final Map<CharacterTag, String> characterImportLinkageURL;
        private final Reference<Integer> lastImportedId;
        private final Map<CharacterTag, String> characterNameMap;
        private final SWF swf;

        @Override
        public void error(SAXParseException e) throws SAXException {
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
        }

        public HTMLTextParser(
                Set<CharacterTag> characterTags,
                DefineEditTextTag det,
                Map<CharacterTag, String> characterImportLinkageURL,
                Reference<Integer> lastImportedId,
                Map<CharacterTag, String> characterNameMap,
                SWF swf
        ) {
            if (det.hasFont) {
                String fontName = null;
                FontTag ft = (FontTag) det.getSwf().getCharacter(det.fontId);
                if (ft != null) {
                    DefineFontNameTag fnt = ft.getFontNameTag();
                    if (fnt != null) {
                        fontName = fnt.fontName;
                    }

                    if (fontName == null) {
                        fontName = ft.getFontNameIntag();
                    }
                    if (fontName == null) {
                        fontName = FontTag.getDefaultFontName();
                    }
                    italic = ft.isItalic();
                    bold = ft.isBold();
                    size = (int) (det.fontHeight / SWF.unitDivisor);

                    String installedFont;
                    if ((installedFont = FontTag.isFontFamilyInstalled(fontName)) != null) {
                        fontFace = new Font(installedFont, (italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0) | (!italic && !bold ? Font.PLAIN : 0), size < 0 ? 10 : size).getPSName();
                    } else {
                        fontFace = fontName;
                    }

                    if (characterImportLinkageURL.containsKey(ft)) {
                        fontFace = getSymbolName(lastImportedId, characterNameMap, swf, ft, "Font") + "*";
                    }
                    fontFaceStack.push(fontFace);
                    fontSizeStack.push(size);
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
            if (det.hasTextColor) {
                color = det.textColor.toHexRGB();
                colorAlpha = det.textColor.alpha;
            }

            this.characterTags = characterTags;
            this.characterImportLinkageURL = characterImportLinkageURL;
            this.lastImportedId = lastImportedId;
            this.characterNameMap = characterNameMap;
            this.swf = swf;
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
                    String k = attributes.getValue("kerning");
                    if (k != null) {
                        autoKern = k.equals("1");
                    }
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
                            if (s.startsWith("+")) {
                                size += Integer.parseInt(s.substring(1));
                            } else if (s.startsWith("-")) {
                                size -= Integer.parseInt(s.substring(1));
                            } else {
                                size = Integer.parseInt(s);
                            }
                        } catch (NumberFormatException ex) {
                            logger.log(Level.WARNING, "Invalid font size: {0}", s);
                        }
                    }
                    String c = attributes.getValue("color");
                    if (c != null) {
                        if (c.matches("^#[0-9a-fA-F]{8}$")) {
                            color = "#" + c.substring(3);
                        } else if (c.matches("^#[0-9a-fA-F]{6}$")) {
                            color = c;
                            colorAlpha = 255;
                        } else {
                            //wrong format, do not change color
                        }
                    }
                    String f = attributes.getValue("face");
                    if (f != null) {
                        for (Tag tag : characterTags) {
                            if (tag instanceof FontTag) {
                                FontTag ft = (FontTag) tag;
                                String fontName = null;
                                if (f.equals(ft.getFontNameIntag())) {
                                    DefineFontNameTag fnt = ft.getFontNameTag();
                                    if (fnt != null) {
                                        fontName = fnt.fontName;
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

                                    if (characterImportLinkageURL.containsKey(ft)) {
                                        fontFace = getSymbolName(lastImportedId, characterNameMap, swf, ft, "Font") + "*";
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    fontColorStack.push(color);
                    fontColorAlphaStack.push(colorAlpha);
                    fontFaceStack.push(fontFace);
                    fontSizeStack.push(size);
                    fontLetterSpacingStack.push(letterSpacing);
                    fontKerningStack.push(autoKern);
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
            if (qName.equals("font")) {
                fontColorStack.pop();
                fontColorAlphaStack.pop();
                fontFaceStack.pop();
                fontKerningStack.pop();
                fontLetterSpacingStack.pop();
                fontSizeStack.pop();
                color = null;
                colorAlpha = 255;
                if (!fontColorStack.isEmpty()) {
                    color = fontColorStack.peek();
                    colorAlpha = fontColorAlphaStack.peek();
                }
                fontFace = null;
                if (!fontFaceStack.isEmpty()) {
                    fontFace = fontFaceStack.peek();
                }
                autoKern = false;
                if (!fontKerningStack.isEmpty()) {
                    autoKern = fontKerningStack.peek();
                }
                letterSpacing = 0;
                if (!fontLetterSpacingStack.isEmpty()) {
                    letterSpacing = fontLetterSpacingStack.peek();
                }
                size = 10; //??
                if (!fontSizeStack.isEmpty()) {
                    size = fontSizeStack.peek();
                }
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
                result.writeAttribute("autoKern", autoKern ? "true" : "false");
                if (letterSpacing != 0) {
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
                if (color != null && !color.isEmpty()) {
                    result.writeAttribute("fillColor", color);
                    if (colorAlpha != 255) {
                        result.writeAttribute("alpha", colorAlpha / 255.0);
                    }
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

    private class MultiLevelClip {

        public PlaceObjectTypeTag startClipPlaceTag;
        public int symbol;
        public int numFrames;

        public MultiLevelClip(PlaceObjectTypeTag startClipPlaceTag, int symbol, int numFrames) {
            this.startClipPlaceTag = startClipPlaceTag;
            this.symbol = symbol;
            this.numFrames = numFrames;
        }
    }
}
