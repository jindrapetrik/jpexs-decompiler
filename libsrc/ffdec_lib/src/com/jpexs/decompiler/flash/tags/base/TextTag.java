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
 * License along with this library. */
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.FontExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.shape.CanvasShapeExporter;
import com.jpexs.decompiler.flash.exporters.shape.SVGShapeExporter;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.importers.TextImportResizeTextBoundsMode;
import com.jpexs.decompiler.flash.tags.text.JustifyAlignGlyphEntry;
import com.jpexs.decompiler.flash.tags.text.TextAlign;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.DynamicTextGlyphEntry;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;

/**
 *
 * @author JPEXS
 */
public abstract class TextTag extends DrawableTag {

    public TextTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract MATRIX getTextMatrix();

    public abstract List<String> getTexts();

    public abstract List<Integer> getFontIds();

    public abstract HighlightedText getFormattedText(boolean ignoreLetterSpacing);

    // use the texts from the "texts" argument when it is not null
    public abstract boolean setFormattedText(MissingCharacterHandler missingCharHandler, String formattedText, String[] texts) throws TextParseException;

    public abstract void updateTextBounds();

    public abstract boolean alignText(TextAlign textAlign);

    public abstract boolean translateText(int diff);

    public abstract RECT getBounds();

    public abstract void setBounds(RECT r);

    public abstract ExportRectangle calculateTextBounds();

    @Override
    public RECT getRect() {
        return getRect(null); // parameter not used
    }

    private static void updateRect(RECT ret, int x, int y) {
        if (x < ret.Xmin) {
            ret.Xmin = x;
        }
        if (x > ret.Xmax) {
            ret.Xmax = x;
        }
        if (y < ret.Ymin) {
            ret.Ymin = y;
        }
        if (y > ret.Ymax) {
            ret.Ymax = y;
        }
    }

    public static void alignText(SWF swf, List<TEXTRECORD> textRecords, TextAlign textAlign) {
        // Remove Justify align entries
        for (TEXTRECORD tr : textRecords) {
            for (int i = 0; i < tr.glyphEntries.size(); i++) {
                GLYPHENTRY ge = tr.glyphEntries.get(i);
                if (ge instanceof JustifyAlignGlyphEntry) {
                    JustifyAlignGlyphEntry jge = (JustifyAlignGlyphEntry) ge;
                    ge = new GLYPHENTRY();
                    ge.glyphAdvance = jge.originalAdvance;
                    ge.glyphIndex = jge.glyphIndex;
                    tr.glyphEntries.set(i, ge);
                }
            }
        }

        int xMin = Integer.MAX_VALUE;
        int maxWidth = 0;
        for (TEXTRECORD tr : textRecords) {
            int xOffset = tr.styleFlagsHasXOffset ? tr.xOffset : 0;
            if (xOffset < xMin) {
                xMin = xOffset;
            }

            int width = tr.getTotalAdvance();

            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        FontTag font = null;

        for (TEXTRECORD tr : textRecords) {
            if (tr.styleFlagsHasFont) {
                FontTag font2 = swf.getFont(tr.fontId);
                if (font2 != null) {
                    font = font2;
                }
            }

            int width = tr.getTotalAdvance();
            switch (textAlign) {
                case LEFT:
                    tr.xOffset = xMin;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case CENTER:
                    tr.xOffset = xMin + (maxWidth - width) / 2;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case RIGHT:
                    tr.xOffset = xMin + maxWidth - width;
                    tr.styleFlagsHasXOffset = true;
                    break;
                case JUSTIFY:
                    tr.xOffset = xMin;
                    tr.styleFlagsHasXOffset = true;

                    if (font != null) {
                        int diff = maxWidth - width;
                        if (diff > 0) {
                            int spaces = 0;
                            int spaces2 = 0;
                            int state = 0;
                            List<GLYPHENTRY> glyphEntries = new ArrayList<>();
                            List<GLYPHENTRY> glyphEntries2 = new ArrayList<>();
                            for (GLYPHENTRY ge : tr.glyphEntries) {
                                char ch = font.glyphToChar(ge.glyphIndex);
                                boolean whitespace = Character.isWhitespace(ch);
                                switch (state) {
                                    case 0:
                                        if (!whitespace) {
                                            state = 1;
                                        }
                                        break;
                                    case 1:
                                        if (whitespace) {
                                            spaces2++;
                                            glyphEntries2.add(ge);
                                        } else {
                                            spaces += spaces2;
                                            spaces2 = 0;
                                            glyphEntries.addAll(glyphEntries2);
                                            glyphEntries2.clear();
                                        }
                                        break;
                                }
                            }

                            if (spaces > 0 && glyphEntries.size() > 0) {
                                int fix = diff / spaces;
                                int remaining = diff - fix * spaces;
                                for (GLYPHENTRY ge : glyphEntries) {
                                    int diff2 = fix;
                                    if (remaining-- > 0) {
                                        diff2++;
                                    }

                                    JustifyAlignGlyphEntry jge = new JustifyAlignGlyphEntry();
                                    jge.originalAdvance = ge.glyphAdvance;
                                    jge.glyphAdvance = ge.glyphAdvance + diff2;
                                    jge.glyphIndex = ge.glyphIndex;
                                    int idx = tr.glyphEntries.indexOf(ge);
                                    tr.glyphEntries.set(idx, jge);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    public static Map<String, Object> getTextRecordsAttributes(List<TEXTRECORD> list, SWF swf) {
        Map<String, Object> att = new HashMap<>();
        RECT textBounds = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        FontTag font = null;
        int x = 0;
        int y = 0;
        int textHeight = 12;
        int lineSpacing = 0;
        double leading = 0;
        double ascent = 0;
        double descent = 0;
        double lineDistance = 0;

        List<SHAPE> glyphs = null;
        boolean firstLine = true;
        double top = 0;
        List<Integer> allLeftMargins = new ArrayList<>();
        List<Integer> allLetterSpacings = new ArrayList<>();
        FontMetrics fontMetrics;
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bi.getGraphics();
        Font aFont = null;
        int currentLeftMargin;
        for (int r = 0; r < list.size(); r++) {
            TEXTRECORD rec = list.get(r);
            if (rec.styleFlagsHasFont) {
                FontTag font2 = swf.getFont(rec.fontId);
                if (font2 != null) {
                    font = font2;
                }
                textHeight = rec.textHeight;
                if (font == null) {
                    Logger.getLogger(TextTag.class.getName()).log(Level.SEVERE, "Font with id=" + rec.fontId + " was not found.");
                    continue;
                }

                glyphs = font.getGlyphShapeTable();

                if (!font.hasLayout()) {
                    String fontName = FontTag.getFontNameWithFallback(font.getFontNameIntag());
                    aFont = new Font(fontName, font.getFontStyle(), (int) (textHeight / SWF.unitDivisor));

                    Map<TextAttribute, Integer> attr = new HashMap<>();

                    attr.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
                    attr.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
                    aFont = aFont.deriveFont(attr);

                    fontMetrics = graphics.getFontMetrics(aFont);
                    LineMetrics lm = fontMetrics.getLineMetrics("A", graphics);
                    ascent = lm.getAscent();
                    descent = lm.getDescent();
                    leading = lm.getLeading();
                    lineDistance = ascent + descent;
                } else {
                    leading = ((double) font.getLeading() * textHeight / 1024.0 / font.getDivider() / SWF.unitDivisor);
                    ascent = ((double) font.getAscent() * textHeight / 1024.0 / font.getDivider() / SWF.unitDivisor);
                    descent = ((double) font.getDescent() * textHeight / 1024.0 / font.getDivider() / SWF.unitDivisor);
                    lineDistance = ascent + descent;
                }
            }
            currentLeftMargin = 0;
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset;
                currentLeftMargin = x;
            }
            if (rec.styleFlagsHasYOffset) {
                if (!firstLine) {
                    top += ascent + descent;
                    int topint = (int) (Math.round(top) * SWF.unitDivisor);
                    lineSpacing = rec.yOffset - topint;
                    top += lineSpacing / SWF.unitDivisor;
                } else {
                    top = ascent;
                }
                y = rec.yOffset;
            }

            firstLine = false;
            allLeftMargins.add(currentLeftMargin);

            if (glyphs == null) {
                Logger.getLogger(TextTag.class.getName()).log(Level.SEVERE, "Glyphs not found.");
                continue;
            }

            int letterSpacing = Integer.MAX_VALUE;
            for (int e = 0; e < rec.glyphEntries.size(); e++) {
                GLYPHENTRY entry = rec.glyphEntries.get(e);
                GLYPHENTRY nextEntry = null;
                if (e < rec.glyphEntries.size() - 1) {
                    nextEntry = rec.glyphEntries.get(e + 1);
                }
                RECT rect = SHAPERECORD.getBounds(glyphs.get(entry.glyphIndex).shapeRecords);
                rect.Xmax = (int) Math.round(((double) rect.Xmax * textHeight) / (font.getDivider() * 1024));
                rect.Xmin = (int) Math.round(((double) rect.Xmin * textHeight) / (font.getDivider() * 1024));
                rect.Ymax = (int) Math.round(((double) rect.Ymax * textHeight) / (font.getDivider() * 1024));
                rect.Ymin = (int) Math.round(((double) rect.Ymin * textHeight) / (font.getDivider() * 1024));
                updateRect(textBounds, x + rect.Xmin, y + rect.Ymin);
                updateRect(textBounds, x + rect.Xmax, y + rect.Ymax);
                int adv = entry.glyphAdvance;

                int defaultAdvance;
                if (font.hasLayout()) {
                    int kerningAdjustment = 0;
                    if (nextEntry != null) {
                        kerningAdjustment = font.getGlyphKerningAdjustment(entry.glyphIndex, nextEntry.glyphIndex);
                    }
                    defaultAdvance = (int) (Math.round(textHeight * (font.getGlyphAdvance(entry.glyphIndex) + kerningAdjustment) / (1024.0 * font.getDivider())));
                } else {
                    defaultAdvance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(aFont, font.glyphToChar(entry.glyphIndex), nextEntry == null ? null : font.glyphToChar(nextEntry.glyphIndex)));
                }
                int newLetterSpacing = adv - defaultAdvance;
                if (e == 0 || e == rec.glyphEntries.size() - 1) {
                    if (rec.glyphEntries.size() == 1) {
                        letterSpacing = 0;
                    }
                } else if (newLetterSpacing < letterSpacing) {
                    letterSpacing = newLetterSpacing;
                }
                x += adv;
            }
            allLetterSpacings.add(letterSpacing);
        }
        att.put("indent", 0); //?
        att.put("rightMargin", 0); //?
        att.put("lineSpacing", lineSpacing);
        att.put("textBounds", textBounds);
        att.put("allLeftMargins", allLeftMargins);
        att.put("allLetterSpacings", allLetterSpacings);
        return att;
    }

    public static SHAPE getBorderShape(RGB borderColor, RGB fillColor, RECT rect) {
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        if (fillColor != null) {
            shape.fillStyles.fillStyles = new FILLSTYLE[1];
            FILLSTYLE fillStyle = new FILLSTYLE();
            fillStyle.fillStyleType = FILLSTYLE.SOLID;
            fillStyle.color = fillColor;
            shape.fillStyles.fillStyles[0] = fillStyle;
        } else {
            shape.fillStyles.fillStyles = new FILLSTYLE[0];
        }
        shape.lineStyles = new LINESTYLEARRAY();
        shape.lineStyles.lineStyles = new LINESTYLE[1];
        LINESTYLE lineStyle = new LINESTYLE();
        lineStyle.color = borderColor;
        lineStyle.width = 20;
        shape.lineStyles.lineStyles[0] = lineStyle;
        shape.shapeRecords = new ArrayList<>();
        StyleChangeRecord style = new StyleChangeRecord();
        style.lineStyle = 1;
        style.stateLineStyle = true;
        if (fillColor != null) {
            style.stateFillStyle0 = true;
            style.fillStyle0 = 1;
        }
        style.stateMoveTo = true;
        shape.shapeRecords.add(style);
        StraightEdgeRecord top = new StraightEdgeRecord();
        top.generalLineFlag = true;
        top.deltaX = rect.getWidth();
        StraightEdgeRecord right = new StraightEdgeRecord();
        right.generalLineFlag = true;
        right.deltaY = rect.getHeight();
        StraightEdgeRecord bottom = new StraightEdgeRecord();
        bottom.generalLineFlag = true;
        bottom.deltaX = -rect.getWidth();
        StraightEdgeRecord left = new StraightEdgeRecord();
        left.generalLineFlag = true;
        left.deltaY = -rect.getHeight();
        shape.shapeRecords.add(top);
        shape.shapeRecords.add(right);
        shape.shapeRecords.add(bottom);
        shape.shapeRecords.add(left);
        shape.shapeRecords.add(new EndShapeRecord());
        return shape;
    }

    public static void drawBorder(SWF swf, SerializableImage image, RGB borderColor, RGB fillColor, RECT rect, MATRIX textMatrix, Matrix transformation, ColorTransform colorTransform) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        Matrix mat = transformation.clone();
        mat = mat.concatenate(new Matrix(textMatrix));
        BitmapExporter.export(swf, getBorderShape(borderColor, fillColor, rect), null, image, mat, mat, colorTransform);
    }

    public static void drawBorderHtmlCanvas(SWF swf, StringBuilder result, RGB borderColor, RGB fillColor, RECT rect, MATRIX textMatrix, ColorTransform colorTransform, double unitDivisor) {
        Matrix mat = new Matrix(textMatrix);
        result.append("\tctx.save();\r\n");
        result.append("\tctx.transform(").append(mat.scaleX).append(",").append(mat.rotateSkew0).append(",").append(mat.rotateSkew1).append(",").append(mat.scaleY).append(",").append(mat.translateX).append(",").append(mat.translateY).append(");\r\n");
        SHAPE shape = getBorderShape(borderColor, fillColor, rect);
        CanvasShapeExporter cse = new CanvasShapeExporter(null, unitDivisor, swf, shape, colorTransform, 0, 0);
        cse.export();
        result.append(cse.getShapeData());
        result.append("\tctx.restore();\r\n");
    }

    public static void drawBorderSVG(SWF swf, SVGExporter exporter, RGB borderColor, RGB fillColor, RECT rect, MATRIX textMatrix, ColorTransform colorTransform, double zoom) {
        exporter.createSubGroup(new Matrix(textMatrix), null);
        SHAPE shape = getBorderShape(borderColor, fillColor, rect);
        SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, shape, 0, exporter, null, colorTransform, zoom);
        shapeExporter.export();
        exporter.endGroup();
    }

    public static void staticTextToImage(SWF swf, List<TEXTRECORD> textRecords, int numText, SerializableImage image, MATRIX textMatrix, Matrix transformation, ColorTransform colorTransform) {
        int textColor = 0;
        FontTag font = null;
        int textHeight = 12;
        int x = 0;
        int y = 0;
        List<SHAPE> glyphs = null;
        Matrix mat0 = transformation.clone();
        mat0 = mat0.concatenate(new Matrix(textMatrix));
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasColor) {
                if (numText == 2) {
                    textColor = rec.textColorA.toInt();
                } else {
                    textColor = rec.textColor.toInt();
                }

                if (colorTransform != null) {
                    textColor = colorTransform.apply(textColor);
                }
            }
            if (rec.styleFlagsHasFont) {
                FontTag font2 = swf.getFont(rec.fontId);
                if (font2 != null) {
                    font = font2;
                }
                glyphs = font == null ? null : font.getGlyphShapeTable();
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                y = rec.yOffset;
            }

            double divider = font == null ? 1 : font.getDivider();
            double rat = textHeight / 1024.0 / divider;

            Matrix matScale = Matrix.getScaleInstance(rat);
            Color textColor2 = new Color(textColor, true);
            for (GLYPHENTRY entry : rec.glyphEntries) {
                matScale.translateX = x;
                matScale.translateY = y;

                Matrix mat = mat0.concatenate(matScale);
                SHAPE shape = null;
                if (entry.glyphIndex != -1 && glyphs != null) {
                    // shapeNum: 1
                    shape = glyphs.get(entry.glyphIndex);
                } else if (entry instanceof DynamicTextGlyphEntry) {
                    DynamicTextGlyphEntry dynamicEntry = (DynamicTextGlyphEntry) entry;
                    if (dynamicEntry.fontFace != null) {
                        FontTag fnt = swf.getFontByName(dynamicEntry.fontFace);
                        if (fnt != null && entry.glyphIndex != -1) {
                            shape = fnt.getGlyphShapeTable().get(entry.glyphIndex);
                        } else {
                            shape = SHAPERECORD.fontCharacterToSHAPE(new Font(dynamicEntry.fontFace, dynamicEntry.fontStyle, 12), (int) Math.round(divider * 1024), dynamicEntry.character);
                        }
                    }
                }

                if (shape != null) {
                    BitmapExporter.export(swf, shape, textColor2, image, mat, mat, colorTransform);
                    if (SHAPERECORD.DRAW_BOUNDING_BOX) {
                        RGB borderColor = new RGBA(Color.black);
                        RGB fillColor = new RGBA(new Color(255, 255, 255, 0));
                        RECT bounds = shape.getBounds();
                        mat = Matrix.getTranslateInstance(bounds.Xmin, bounds.Ymin).preConcatenate(mat);
                        TextTag.drawBorder(swf, image, borderColor, fillColor, bounds, new MATRIX(), mat, colorTransform);
                    }
                }

                x += entry.glyphAdvance;
            }
        }
    }

    public static ExportRectangle calculateTextBounds(SWF swf, List<TEXTRECORD> textRecords, MATRIX textMatrix) {
        FontTag font = null;
        int textHeight = 12;
        int x = 0;
        int y = 0;
        List<SHAPE> glyphs = null;
        ExportRectangle result = null;
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasFont) {
                font = swf.getFont(rec.fontId);
                glyphs = font == null ? null : font.getGlyphShapeTable();
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                y = rec.yOffset;
            }

            double rat = textHeight / 1024.0 / (font == null ? 1 : font.getDivider());

            for (GLYPHENTRY entry : rec.glyphEntries) {
                Matrix mat = new Matrix();
                mat = mat.concatenate(new Matrix(textMatrix));
                Matrix matTr = Matrix.getTranslateInstance(x, y);
                mat = mat.concatenate(matTr);
                mat = mat.concatenate(Matrix.getScaleInstance(rat));
                if (entry.glyphIndex != -1 && glyphs != null) {
                    // shapeNum: 1
                    SHAPE shape = glyphs.get(entry.glyphIndex);
                    RECT glyphBounds = shape.getBounds();
                    int glyphWidth = glyphBounds.getWidth();
                    int glyphHeight = glyphBounds.getHeight();
                    glyphBounds.Xmin -= glyphWidth / 2;
                    glyphBounds.Ymin -= glyphHeight / 2;
                    glyphBounds.Xmax += glyphWidth;
                    glyphBounds.Ymax += glyphHeight;
                    if (glyphBounds.Xmax > glyphBounds.Xmin && glyphBounds.Ymax > glyphBounds.Ymin) {
                        ExportRectangle rect = mat.transform(new ExportRectangle(glyphBounds));
                        if (result == null) {
                            result = rect;
                        } else {
                            result.xMin = Math.min(result.xMin, rect.xMin);
                            result.yMin = Math.min(result.yMin, rect.yMin);
                            result.xMax = Math.max(result.xMax, rect.xMax);
                            result.yMax = Math.max(result.yMax, rect.yMax);
                        }
                    }
                    x += entry.glyphAdvance;
                }
            }
        }

        return result;
    }

    protected void updateTextBounds(RECT textBounds) {
        TextImportResizeTextBoundsMode resizeMode = Configuration.textImportResizeTextBoundsMode.get();
        if (resizeMode != null && (resizeMode.equals(TextImportResizeTextBoundsMode.GROW_ONLY) || resizeMode.equals(TextImportResizeTextBoundsMode.GROW_AND_SHRINK))) {
            ExportRectangle newBounds = calculateTextBounds();
            if (newBounds != null) {
                int xMin = (int) Math.floor(newBounds.xMin);
                int yMin = (int) Math.floor(newBounds.yMin);
                int xMax = (int) Math.ceil(newBounds.xMax);
                int yMax = (int) Math.ceil(newBounds.yMax);
                if (resizeMode.equals(TextImportResizeTextBoundsMode.GROW_ONLY)) {
                    textBounds.Xmin = Math.min(xMin, textBounds.Xmin);
                    textBounds.Ymin = Math.min(yMin, textBounds.Ymin);
                    textBounds.Xmax = Math.max(xMax, textBounds.Xmax);
                    textBounds.Ymax = Math.max(yMax, textBounds.Ymax);
                } else if (resizeMode.equals(TextImportResizeTextBoundsMode.GROW_AND_SHRINK)) {
                    textBounds.Xmin = xMin;
                    textBounds.Ymin = yMin;
                    textBounds.Xmax = xMax;
                    textBounds.Ymax = yMax;
                }
            }
        }
    }

    public static void staticTextToHtmlCanvas(double unitDivisor, SWF swf, List<TEXTRECORD> textRecords, int numText, StringBuilder result, RECT bounds, MATRIX textMatrix, ColorTransform colorTransform) {
        int textColor = 0;
        FontTag font = null;
        int fontId = -1;
        int textHeight = 12;
        int x = 0;
        int y = 0;

        List<SHAPE> glyphs = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasColor) {
                if (numText == 2) {
                    textColor = rec.textColorA.toInt();
                } else {
                    textColor = rec.textColor.toInt();
                }

                if (colorTransform != null) {
                    textColor = colorTransform.apply(textColor);
                }
            }
            if (rec.styleFlagsHasFont) {
                font = swf.getFont(rec.fontId);
                fontId = rec.fontId;
                glyphs = font.getGlyphShapeTable();
                textHeight = rec.textHeight;
            }
            if (rec.styleFlagsHasXOffset) {
                x = rec.xOffset;
            }
            if (rec.styleFlagsHasYOffset) {
                y = rec.yOffset;
            }

            double rat = textHeight / 1024.0 / font.getDivider();

            result.append("\tvar textColor = ").append(CanvasShapeExporter.color(textColor)).append(";\r\n");
            for (GLYPHENTRY entry : rec.glyphEntries) {
                Matrix mat = (new Matrix(textMatrix).concatenate(Matrix.getTranslateInstance(x, y))).concatenate(Matrix.getScaleInstance(rat));
                if (entry.glyphIndex != -1) {
                    // shapeNum: 1
                    result.append("\tctx.save();\r\n");
                    result.append("\tctx.transform(").append(mat.scaleX).append(",").append(mat.rotateSkew0).append(",").append(mat.rotateSkew1).append(",").append(mat.scaleY).append(",").append(mat.translateX).append(",").append(mat.translateY).append(");\r\n");
                    result.append("\tfont").append(fontId).append("(ctx,\"").append(("" + font.glyphToChar(entry.glyphIndex)).replace("\\", "\\\\").replace("\"", "\\\"")).append("\",textColor);\r\n");
                    result.append("\tctx.restore();\r\n");
                    x += entry.glyphAdvance;
                }
            }
        }
    }

    public static void staticTextToSVG(SWF swf, List<TEXTRECORD> textRecords, int numText, SVGExporter exporter, RECT bounds, MATRIX textMatrix, ColorTransform colorTransform, double zoom) {
        int textColor = 0;
        FontTag font = null;
        int textHeight = 12;
        int x = 0;
        int y = 0;
        List<SHAPE> glyphs = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasColor) {
                if (numText == 2) {
                    textColor = rec.textColorA.toInt();
                } else {
                    textColor = rec.textColor.toInt();
                }

                if (colorTransform != null) {
                    textColor = colorTransform.apply(textColor);
                }
            }
            if (rec.styleFlagsHasFont) {
                font = swf.getFont(rec.fontId);
                glyphs = font.getGlyphShapeTable();
                textHeight = rec.textHeight;
            }
            int offsetX = 0;
            int offsetY = 0;
            if (rec.styleFlagsHasXOffset) {
                offsetX = rec.xOffset;
                x = offsetX;
            }
            if (rec.styleFlagsHasYOffset) {
                offsetY = rec.yOffset;
                y = offsetY;
            }

            double rat = textHeight / 1024.0 / font.getDivider();

            exporter.createSubGroup(new Matrix(textMatrix), null);
            if (exporter.useTextTag) {
                StringBuilder text = new StringBuilder();
                int totalAdvance = 0;
                for (GLYPHENTRY entry : rec.glyphEntries) {
                    if (entry.glyphIndex != -1) {
                        char ch = font.glyphToChar(entry.glyphIndex);
                        text.append(ch);
                        totalAdvance += entry.glyphAdvance;
                    }
                }

                boolean hasOffset = offsetX != 0 || offsetY != 0;
                if (hasOffset) {
                    exporter.createSubGroup(Matrix.getTranslateInstance(offsetX, offsetY), null);
                }

                Element textElement = exporter.createElement("text");
                textElement.setAttribute("font-size", Double.toString(rat * 1024));
                textElement.setAttribute("font-family", font.getFontNameIntag());
                textElement.setAttribute("textLength", Double.toString(totalAdvance / SWF.unitDivisor));
                textElement.setAttribute("lengthAdjust", "spacing");
                textElement.setTextContent(text.toString());

                RGBA colorA = new RGBA(textColor);
                textElement.setAttribute("fill", colorA.toHexRGB());
                if (colorA.alpha != 255) {
                    textElement.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
                }

                exporter.addToGroup(textElement);
                FontExportMode fontExportMode = FontExportMode.WOFF;
                exporter.addStyle(font.getFontNameIntag(), new FontExporter().exportFont(font, fontExportMode), fontExportMode);

                if (hasOffset) {
                    exporter.endGroup();
                }
            } else {
                for (GLYPHENTRY entry : rec.glyphEntries) {
                    Matrix mat = Matrix.getTranslateInstance(x, y).concatenate(Matrix.getScaleInstance(rat));
                    if (entry.glyphIndex != -1) {
                        // shapeNum: 1
                        SHAPE shape = glyphs.get(entry.glyphIndex);
                        char ch = font.glyphToChar(entry.glyphIndex);

                        String charId = null;
                        Map<Integer, String> chs;
                        if (exporter.exportedChars.containsKey(font)) {
                            chs = exporter.exportedChars.get(font);
                            if (chs.containsKey(entry.glyphIndex)) {
                                charId = chs.get(entry.glyphIndex);
                            }
                        } else {
                            chs = new HashMap<>();
                            exporter.exportedChars.put(font, chs);
                        }

                        if (charId == null) {
                            charId = exporter.getUniqueId(Helper.getValidHtmlId("font_" + font.getFontNameIntag() + "_" + ch));
                            exporter.createDefGroup(null, charId);
                            SVGShapeExporter shapeExporter = new SVGShapeExporter(swf, shape, 0, exporter, null, colorTransform, zoom);
                            shapeExporter.export();
                            if (!exporter.endGroup()) {
                                charId = "";
                            }

                            chs.put(entry.glyphIndex, charId);
                        }

                        if (!"".equals(charId)) {
                            Element charImage = exporter.addUse(mat, bounds, charId, null);
                            RGBA colorA = new RGBA(textColor);
                            charImage.setAttribute("fill", colorA.toHexRGB());
                            if (colorA.alpha != 255) {
                                charImage.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
                            }
                        }

                        x += entry.glyphAdvance;
                    }
                }
            }
            exporter.endGroup();
        }
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked) {
        RECT r = getBounds();
        Shape shp = new Rectangle.Double(r.Xmin, r.Ymin, r.getWidth(), r.getHeight());
        return transformation.toTransform().createTransformedShape(shp); //TODO: match character shapes (?)
    }
}
