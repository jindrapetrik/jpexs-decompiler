package com.jpexs.decompiler.flash.tags.converters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.shapes.ShapeTransformer;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.DynamicTextGlyphEntry;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.decompiler.flash.xfl.XFLXmlWriter;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

/**
 * Converts between text types (DefineText, DefineText2, DefineEditText) and to shapes
 *
 * @author JPEXS
 */
public class TextTypeConverter {

    public static int TEXT_TYPE_DEFINE_TEXT = 1;
    public static int TEXT_TYPE_DEFINE_TEXT2 = 2;
    public static int TEXT_TYPE_DEFINE_EDIT_TEXT = 3;
    public static int TEXT_TYPE_SHAPE = 4;

    /**
     * Converts static text version
     *
     * @param tag DefineText or DefineText2
     * @param defineTextVersion 1 for DefineText, 2 for DefineText2
     * @param targetSWF Target SWF
     * @return DefineText or DefineText2
     */
    private StaticTextTag convertStaticText(StaticTextTag tag, int defineTextVersion, SWF targetSWF) {
        StaticTextTag ret;
        switch (defineTextVersion) {
            case 1:
                ret = new DefineTextTag(targetSWF);
                break;
            case 2:
                ret = new DefineText2Tag(targetSWF);
                break;
            default:
                throw new IllegalArgumentException("defineTextVersion should be either 1 or 2");
        }
        StaticTextTag clonedTag;
        try {
            clonedTag = (StaticTextTag) tag.cloneTag();
        } catch (InterruptedException | IOException ex) {
            return null;
        }
        ret.textRecords = clonedTag.textRecords;
        ret.textBounds = clonedTag.textBounds;
        ret.textMatrix = clonedTag.textMatrix;
        for (TEXTRECORD rec : ret.textRecords) {
            if (defineTextVersion == 1 && rec.textColorA != null) {
                rec.textColor = new RGB(rec.textColorA);
                rec.textColorA = null;
            }
            if (defineTextVersion == 2 && rec.textColor != null) {
                rec.textColorA = new RGBA(rec.textColor);
                rec.textColor = null;
            }
        }
        return ret;
    }

    /**
     * Converts DefineEditTextTag to static text (DefineText or DefineText2)
     *
     * @param tag DefineEditTextTag
     * @param defineTextVersion 1 for DefineText, 2 for DefineText2
     * @param targetSWF Target SWF
     * @return DefineText or DefineText2
     */
    private StaticTextTag editTextToStaticText(DefineEditTextTag tag, int defineTextVersion, SWF targetSWF) {
        StaticTextTag ret = null;
        switch (defineTextVersion) {
            case 1:
                ret = new DefineTextTag(targetSWF);
                break;
            case 2:
                ret = new DefineText2Tag(targetSWF);
                break;
            default:
                throw new IllegalArgumentException("defineTextVersion should be either 1 or 2");
        }
        List<TEXTRECORD> records = tag.getTextRecords(tag.getSwf(), new HashMap<>());
        
        int borderPadding = 40;
        int moveX = tag.bounds.Xmin + borderPadding;
        int moveY = tag.bounds.Ymin + borderPadding;
        
        boolean first = true;
        for (TEXTRECORD rec : records) {
            if (defineTextVersion == 1 && rec.textColorA != null) {
                rec.textColor = new RGB(rec.textColorA);
                rec.textColorA = null;
            }
            if (defineTextVersion == 2 && rec.textColor != null) {
                rec.textColorA = new RGBA(rec.textColor);
                rec.textColor = null;
            }  
            
            if (first) {
                rec.styleFlagsHasXOffset = true;
                rec.xOffset += moveX;
                rec.styleFlagsHasYOffset = true;
                rec.yOffset += moveY;
                first = false;
            } else {                     
                if (rec.styleFlagsHasXOffset) {
                    rec.xOffset += moveX;
                }
                if (rec.styleFlagsHasYOffset) {
                    rec.yOffset += moveY;
                }
            }            
        }
                        
        ret.textRecords = records;
        ret.textMatrix = new MATRIX();        
        ExportRectangle bounds = ret.calculateTextBounds();
        ret.textBounds = new RECT((int) Math.round(bounds.xMin), (int) Math.round(bounds.xMax), (int) Math.round(bounds.yMin), (int) Math.round(bounds.yMax));
        return ret;
    }

    /**
     * Converts static text (DefineText, DefineText2) to DefineEditText
     *
     * @param tag DefineText or DefineText2
     * @param targetSWF Target SWF
     * @return New DefineEditText
     */
    private DefineEditTextTag staticTextToEditText(StaticTextTag tag, SWF targetSWF) {
        List<TEXTRECORD> textRecords = tag.textRecords;

        DefineEditTextTag det = new DefineEditTextTag(targetSWF);
        Map<String, Object> attrs = TextTag.getTextRecordsAttributes(textRecords, tag.getSwf(), new HashMap<>());
        @SuppressWarnings("unchecked")
        List<Integer> leftMargins = (List<Integer>) attrs.get("allLeftMargins");
        @SuppressWarnings("unchecked")
        List<Integer> letterSpacings = (List<Integer>) attrs.get("allLetterSpacings");
       
        int leftMargin = leftMargins.isEmpty() ? 0 : leftMargins.get(0);
        
        det.bounds = new RECT(tag.getBounds());
        det.wasStatic = true;
        det.noSelect = true;
        det.useOutlines = true;
        det.multiline = true;

        det.hasLayout = true;
        det.align = DefineEditTextTag.ALIGN_LEFT;
        det.indent = (int) attrs.get("indent");
        det.leftMargin = 0;
        det.leading = (int) attrs.get("lineSpacing");
        det.rightMargin = (int) attrs.get("rightMargin");

        XFLXmlWriter writer = new XFLXmlWriter();
        writer.setMakeNewLines(false);
        RGBA firstTextColor = new RGBA(Color.BLACK);   
        int firstFontId = -1;
        int firstTextHeight = -1;
        try {
            int fontId;
            FontTag font = null;
            String fontName = null;
            int textHeight = -1;
            RGB textColor = null;
            RGBA textColorA = null;
            boolean newline;
            boolean firstRun = true;
            boolean isBold = false;
            boolean isItalic = false;
            for (int r = 0; r < textRecords.size(); r++) {
                TEXTRECORD rec = textRecords.get(r);
                if (rec.styleFlagsHasColor) {
                    RGBA newTextColor;
                    if (tag instanceof DefineTextTag) {
                        textColor = rec.textColor;
                        newTextColor = new RGBA(textColor);
                    } else {
                        textColorA = rec.textColorA;
                        newTextColor = rec.textColorA;
                    }
                    if (r == 0) {
                        firstTextColor = newTextColor;
                    }
                }
                if (rec.styleFlagsHasFont) {
                    fontId = rec.fontId;
                    fontName = null;
                    textHeight = rec.textHeight;
                    font = ((Tag) tag).getSwf().getFont(fontId);

                    isBold = false;
                    isItalic = false;
                    if (font != null) {
                        fontName = font.getFontNameIntag();
                        isBold = font.isBold();
                        isItalic = font.isItalic();
                    }
                    if (fontName == null) {
                        fontName = FontTag.getDefaultFontName();
                    }
                    if (r == 0) {
                        firstFontId = fontId;
                        firstTextHeight = textHeight;
                    }
                }
                newline = false;
                if (!firstRun && rec.styleFlagsHasYOffset) {
                    newline = true;
                }
                firstRun = false;
                if (font != null) {
                    writer.writeStartElement("p");
                    writer.writeAttribute("align", "left");
                    writer.writeStartElement("font");
                    writer.writeAttribute("face", fontName);
                    writer.writeAttribute("size", doubleToString(twipToPixel(textHeight)));
                    if (textColor != null) {
                        writer.writeAttribute("color", textColor.toHexRGB());
                    } else if (textColorA != null) {
                        writer.writeAttribute("color", textColorA.toHexARGB());
                    } else {
                        writer.writeAttribute("color", "#000000");
                    }
                    writer.writeAttribute("letterSpacing", doubleToString(twipToPixel(letterSpacings.get(r))));

                    if (isBold) {
                        writer.writeStartElement("b");
                    }
                    if (isItalic) {
                        writer.writeStartElement("i");
                    }
                    writer.writeCharacters(rec.getText(font));
                    if (isItalic) {
                        writer.writeEndElement();
                    }
                    if (isBold) {
                        writer.writeEndElement(); //b
                    }
                    writer.writeEndElement(); //font
                    writer.writeEndElement(); //p                                      
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(TextTypeConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        det.html = true;
        det.hasText = true;
        det.initialText = writer.toString();
        det.hasTextColor = true;
        det.textColor = firstTextColor;      
        if (firstFontId > -1) {
            det.hasFont = true;
            det.fontId = firstFontId;
            det.fontHeight = firstTextHeight;
        }
        
        ExportRectangle bounds = det.calculateTextBounds();
        det.bounds = new RECT((int) Math.round(bounds.xMin + leftMargin), (int) Math.round(bounds.xMax + leftMargin), (int) Math.round(bounds.yMin), (int) Math.round(bounds.yMax));

        return det;
    }

    /**
     * Converts the current text content to glyph outlines.
     *
     * @param tag Source text
     * @param targetSWF Target SWF
     * @return DefineShape, or DefineShape3 when a fill has transparency
     */
    public ShapeTag convertToShape(TextTag tag, SWF targetSWF) {
        List<TEXTRECORD> records;
        Matrix textMatrix;
        if (tag instanceof StaticTextTag) {
            StaticTextTag text = (StaticTextTag) tag;
            records = text.textRecords;
            textMatrix = new Matrix(text.textMatrix);
        } else {
            DefineEditTextTag text = (DefineEditTextTag) tag;
            records = text.getTextRecords(tag.getSwf(), new HashMap<>());
            textMatrix = Matrix.getTranslateInstance(text.bounds.Xmin + 40, text.bounds.Ymin + 40);
        }
        ShapeTag result = new DefineShape3Tag(targetSWF);
        result.shapes.shapeRecords = new ArrayList<>();
        List<FILLSTYLE> fills = new ArrayList<>();
        Map<Integer, Integer> colorStyles = new HashMap<>();
        boolean transparent = false;
        if (tag instanceof DefineEditTextTag && ((DefineEditTextTag) tag).border) {
            DefineEditTextTag editText = (DefineEditTextTag) tag;
            SHAPEWITHSTYLE border = (SHAPEWITHSTYLE) TextTag.getBorderShape(new RGBA(Color.BLACK), new RGBA(Color.WHITE), editText.bounds);
            result.shapes.lineStyles = border.lineStyles;
            FILLSTYLE fill = new FILLSTYLE();
            fill.fillStyleType = FILLSTYLE.SOLID;
            fill.color = new RGBA(Color.WHITE);
            fills.add(fill);
            colorStyles.put(fill.color.toInt(), 1);
            appendShape(result, border, Matrix.getTranslateInstance(editText.bounds.Xmin, editText.bounds.Ymin), 1, 1);
        }
        FontTag font = null;
        int textHeight = 0;
        int x = 0;
        int y = 0;
        RGBA color = new RGBA(Color.BLACK);
        for (TEXTRECORD rec : records) {
            if (rec.styleFlagsHasFont) {
                font = rec.getFont(tag.getSwf());
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasColor) {
                color = rec.textColorA != null ? rec.textColorA : new RGBA(rec.textColor);
            }
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                y = rec.yOffset;
            }
            double divider = font == null ? 1 : font.getDivider();
            for (GLYPHENTRY entry : rec.glyphEntries) {
                SHAPE glyph = null;
                if (entry.glyphIndex >= 0 && font != null && entry.glyphIndex < font.getGlyphShapeTable().size()) {
                    glyph = font.getGlyphShapeTable().get(entry.glyphIndex);
                } else if (entry instanceof DynamicTextGlyphEntry) {
                    DynamicTextGlyphEntry dynamic = (DynamicTextGlyphEntry) entry;
                    String face = dynamic.fontFace == null ? FontTag.getDefaultFontName() : dynamic.fontFace;
                    glyph = SHAPERECORD.fontCharacterToSHAPE(new Font(face, dynamic.fontStyle, 12),
                            (float) (1024 * divider), dynamic.character);
                }
                if (glyph != null) {
                    Integer fillIndex = colorStyles.get(color.toInt());
                    if (fillIndex == null) {
                        FILLSTYLE fill = new FILLSTYLE();
                        fill.fillStyleType = FILLSTYLE.SOLID;
                        fill.color = new RGBA(color);
                        fills.add(fill);
                        fillIndex = fills.size();
                        colorStyles.put(color.toInt(), fillIndex);
                    }
                    transparent |= color.alpha != 255;
                    Matrix matrix = textMatrix
                            .concatenate(Matrix.getTranslateInstance(x, y))
                            .concatenate(Matrix.getScaleInstance(textHeight / 1024.0 / divider));
                    appendShape(result, glyph, matrix, fillIndex, 0);
                }
                x += entry.glyphAdvance;
            }
        }
        result.shapes.shapeRecords.add(new EndShapeRecord());
        result.shapes.fillStyles.fillStyles = fills.toArray(new FILLSTYLE[0]);
        result.shapeBounds = result.shapes.getBounds(3);
        if (!transparent) {
            ShapeTag opaque = new DefineShapeTag(targetSWF);
            opaque.shapes = result.shapes;
            opaque.shapes.fillStyles = opaque.shapes.fillStyles.toShapeNum(1);
            opaque.shapes.lineStyles = opaque.shapes.lineStyles.toShapeNum(3, 1);
            opaque.shapeBounds = result.shapeBounds;
            result = opaque;
        }
        return result;
    }

    private void appendShape(ShapeTag result, SHAPE source, Matrix matrix, int fillIndex, int lineIndex) {
        SHAPE shape = Helper.deepCopy(source);
        // Every glyph starts at its own origin and resets styles inherited from the preceding glyph.
        StyleChangeRecord start = new StyleChangeRecord();
        start.stateMoveTo = true;
        start.stateFillStyle0 = true;
        start.stateFillStyle1 = true;
        start.stateLineStyle = true;
        shape.shapeRecords.add(0, start);
        new ShapeTransformer().transformSHAPE(matrix, shape, 1);
        for (SHAPERECORD record : shape.shapeRecords) {
            if (record instanceof EndShapeRecord) {
                continue;
            }
            if (record instanceof StyleChangeRecord) {
                StyleChangeRecord style = (StyleChangeRecord) record;
                if (style.stateFillStyle0 && style.fillStyle0 != 0) {
                    style.fillStyle0 = fillIndex;
                }
                if (style.stateFillStyle1 && style.fillStyle1 != 0) {
                    style.fillStyle1 = fillIndex;
                }
                if (style.stateLineStyle && style.lineStyle != 0) {
                    style.lineStyle = lineIndex;
                }
            }
            record.calculateBits();
            result.shapes.shapeRecords.add(record);
        }
    }

    private static double twipToPixel(double tw) {
        return tw / SWF.unitDivisor;
    }

    private static String doubleToString(double d) {
        String ds = "" + d;
        if (ds.endsWith(".0")) {
            ds = ds.substring(0, ds.length() - 2);
        }
        return ds;
    }

    /**
     * Converts text tag referenced by character id in selected SWF file.
     *
     * @param swf SWF
     * @param characterId Character id
     * @param targetTextNum 1 = DefineText, 2 = DefineText2, 3 = DefineEditText, 4 = shape
     */
    public void convertCharacter(SWF swf, int characterId, int targetTextNum) {
        CharacterTag ct = swf.getCharacter(characterId);
        if (!(ct instanceof TextTag)) {
            throw new IllegalArgumentException("Character " + characterId + " is not a text");
        }
        TextTag t = (TextTag) ct;
        Timelined tim = t.getTimelined();
        CharacterTag converted = targetTextNum == TEXT_TYPE_SHAPE
                ? convertToShape(t, swf) : convertTagType(t, swf, targetTextNum);
        converted.setCharacterId(characterId);
        swf.replaceTag(ct, converted);
        converted.setTimelined(tim);
        swf.updateCharacters();
        swf.clearShapeCache();
        swf.clearImageCache();
        swf.assignClassesToSymbols();
        swf.assignExportNamesToSymbols();
        tim.resetTimeline();
    }

    /**
     * Converts text tag types
     *
     * @param sourceTextTag Source tag
     * @param targetSWF Target swf
     * @param targetTextNum 1 = DefineText, 2 = DefineText2, 3 = DefineEditText
     * @return Converted DefineShapeX tag
     * @throws IllegalArgumentException When conversion is not possible - see
     * getForcedMinShapeNum
     */
    public TextTag convertTagType(TextTag sourceTextTag, SWF targetSWF, int targetTextNum) {
        int currentTextNum;
        if (sourceTextTag instanceof DefineTextTag) {
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINE_TEXT;
        } else if (sourceTextTag instanceof DefineText2Tag) {
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINE_TEXT2;
        } else if (sourceTextTag instanceof DefineEditTextTag) {
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINE_EDIT_TEXT;
        } else {
            throw new IllegalArgumentException("Invalid text");
        }

        if (currentTextNum < TEXT_TYPE_DEFINE_EDIT_TEXT && targetTextNum < TEXT_TYPE_DEFINE_EDIT_TEXT) {
            return convertStaticText((StaticTextTag) sourceTextTag, targetTextNum, targetSWF);
        }
        if (currentTextNum < TEXT_TYPE_DEFINE_EDIT_TEXT && targetTextNum == TEXT_TYPE_DEFINE_EDIT_TEXT) {
            return staticTextToEditText((StaticTextTag) sourceTextTag, targetSWF);
        }

        if (currentTextNum == TEXT_TYPE_DEFINE_EDIT_TEXT && targetTextNum < TEXT_TYPE_DEFINE_EDIT_TEXT) {
            return editTextToStaticText((DefineEditTextTag) sourceTextTag, targetTextNum, targetSWF);
        }

        try {
            //currentTextNum == TEXT_TYPE_DEFINE_EDIT_TEXT && targetTextNum == TEXT_TYPE_DEFINE_EDIT_TEXT
            TextTag ret = (TextTag) sourceTextTag.cloneTag();
            ret.setSwf(targetSWF);
            return ret;
        } catch (InterruptedException | IOException ex) {
            return null;
        }
    }
}
