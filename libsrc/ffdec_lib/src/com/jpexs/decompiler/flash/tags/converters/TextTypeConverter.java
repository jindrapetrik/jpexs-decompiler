package com.jpexs.decompiler.flash.tags.converters;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.xfl.XFLXmlWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;

/**
 * Converts between text types (DefineText, DefineText2, DefineEditText)
 *
 * @author JPEXS
 */
public class TextTypeConverter {

    public static int TEXT_TYPE_DEFINETEXT = 1;
    public static int TEXT_TYPE_DEFINETEXT2 = 2;
    public static int TEXT_TYPE_DEFINEEXITTEXT = 3;

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
        for (TEXTRECORD rec : records) {
            if (defineTextVersion == 1 && rec.textColorA != null) {
                rec.textColor = new RGB(rec.textColorA);
                rec.textColorA = null;
            }
            if (defineTextVersion == 2 && rec.textColor != null) {
                rec.textColorA = new RGBA(rec.textColor);
                rec.textColor = null;
            }
        }
        ret.textRecords = records;
        ret.textMatrix = new MATRIX();        
        ExportRectangle bounds = ret.calculateTextBounds();
        ret.textBounds = new RECT((int) Math.round(bounds.xMin), (int) Math.round(bounds.xMax), (int) Math.round(bounds.yMin), (int) Math.round(bounds.yMax));
        return ret;
    }

    /**
     * Convers static text (DefineText, DefineText2) to DefineEditText
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

        det.bounds = new RECT(tag.getBounds());
        det.wasStatic = true;
        det.noSelect = true;
        det.useOutlines = true;
        det.multiline = true;

        det.indent = (int) attrs.get("indent");
        det.leftMargin = leftMargins.isEmpty() ? 0 : leftMargins.get(0);
        det.leading = (int) attrs.get("lineSpacing");
        det.rightMargin = (int) attrs.get("rightMargin");

        XFLXmlWriter writer = new XFLXmlWriter();
        writer.setMakeNewLines(false);
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
                }
                newline = false;
                if (!firstRun && rec.styleFlagsHasYOffset) {
                    newline = true;
                }
                firstRun = false;
                if (font != null) {
                    writer.writeStartElement("p");
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
        
        ExportRectangle bounds = det.calculateTextBounds();
        det.bounds = new RECT((int) Math.round(bounds.xMin), (int) Math.round(bounds.xMax), (int) Math.round(bounds.yMin), (int) Math.round(bounds.yMax));

        return det;
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
     * @param targetTextNum 1 = DefineText, 2 = DefineText2, 3 = DefineEditText
     */
    public void convertCharacter(SWF swf, int characterId, int targetTextNum) {
        CharacterTag ct = swf.getCharacter(characterId);
        if (!(ct instanceof TextTag)) {
            throw new IllegalArgumentException("Character " + characterId + " is not a text");
        }
        TextTag t = (TextTag) ct;
        Timelined tim = t.getTimelined();
        TextTag converted = convertTagType(t, swf, targetTextNum);
        converted.setCharacterId(characterId);
        swf.replaceTag(ct, converted);
        converted.setTimelined(tim);
        swf.updateCharacters();
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
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINETEXT;
        } else if (sourceTextTag instanceof DefineText2Tag) {
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINETEXT2;
        } else if (sourceTextTag instanceof DefineEditTextTag) {
            currentTextNum = TextTypeConverter.TEXT_TYPE_DEFINEEXITTEXT;
        } else {
            throw new IllegalArgumentException("Invalid text");
        }

        if (currentTextNum < TEXT_TYPE_DEFINEEXITTEXT && targetTextNum < TEXT_TYPE_DEFINEEXITTEXT) {
            return convertStaticText((StaticTextTag) sourceTextTag, targetTextNum, targetSWF);
        }
        if (currentTextNum < TEXT_TYPE_DEFINEEXITTEXT && targetTextNum == TEXT_TYPE_DEFINEEXITTEXT) {
            return staticTextToEditText((StaticTextTag) sourceTextTag, targetSWF);
        }

        if (currentTextNum == TEXT_TYPE_DEFINEEXITTEXT && targetTextNum < TEXT_TYPE_DEFINEEXITTEXT) {
            return editTextToStaticText((DefineEditTextTag) sourceTextTag, targetTextNum, targetSWF);
        }

        try {
            //currentTextNum == TEXT_TYPE_DEFINEEXITTEXT && targetTextNum == TEXT_TYPE_DEFINEEXITTEXT
            TextTag ret = (TextTag) sourceTextTag.cloneTag();
            ret.setSwf(targetSWF);
            return ret;
        } catch (InterruptedException | IOException ex) {
            return null;
        }
    }
}
