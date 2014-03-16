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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.BitmapExporter;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public abstract class TextTag extends CharacterTag implements BoundedTag, DrawableTag {

    public TextTag(SWF swf, int id, String name, byte[] data, long pos) {
        super(swf, id, name, data, pos);
    }

    public abstract MATRIX getTextMatrix();

    public abstract String getText();

    public abstract List<Integer> getFontIds();

    public abstract String getFormattedText();

    public abstract boolean setFormattedText(MissingCharacterHandler missingCharHandler, String text) throws ParseException;

    @Override
    public abstract int getCharacterId();

    public abstract RECT getBounds();

    public abstract void setBounds(RECT r);

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

    public static Map<String, Object> getTextRecordsAttributes(List<TEXTRECORD> list, List<Tag> tags) {
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

        List<SHAPE> glyphs = new ArrayList<>();
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
                for (Tag t : tags) {
                    if (t instanceof FontTag) {
                        FontTag ft = (FontTag) t;
                        if (ft.getFontId() == rec.fontId) {
                            font = ft;
                        }
                    }
                }
                textHeight = rec.textHeight;
                glyphs = font.getGlyphShapeTable();

                if (!font.hasLayout()) {
                    String fontName = FontTag.getFontNameWithFallback(font.getFontName());
                    aFont = new Font(fontName, font.getFontStyle(), (int) (textHeight / SWF.unitDivisor));
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
                    top += lineDistance;
                    int topint = (int) Math.round(SWF.unitDivisor * top);
                    lineSpacing = rec.yOffset - topint;
                    top += lineSpacing / SWF.unitDivisor;
                } else {
                    top = ascent - 2.0; //I don't know why, but there are always 2 pixels
                }
                y = rec.yOffset;
            }
            firstLine = false;
            allLeftMargins.add(currentLeftMargin);
            int letterSpacing = 0;
            for (int e = 0; e < rec.glyphEntries.length; e++) {
                GLYPHENTRY entry = rec.glyphEntries[e];
                GLYPHENTRY nextEntry = null;
                if (e < rec.glyphEntries.length - 1) {
                    nextEntry = rec.glyphEntries[e + 1];
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
                        kerningAdjustment = font.getGlyphKerningAdjustment(tags, entry.glyphIndex, nextEntry.glyphIndex);
                    }
                    defaultAdvance = (int) Math.round(SWF.unitDivisor * textHeight * (font.getGlyphAdvance(entry.glyphIndex) + kerningAdjustment) / (font.getDivider() * 1024.0));
                } else {
                    defaultAdvance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(aFont, font.glyphToChar(tags, entry.glyphIndex), nextEntry == null ? null : font.glyphToChar(tags, nextEntry.glyphIndex)));
                }
                letterSpacing = adv - defaultAdvance;
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

    public static void drawBorder(SWF swf, SerializableImage image, RGB borderColor, RGB fillColor, RECT rect, MATRIX textMatrix, Matrix transformation, ColorTransform colorTransform) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        Matrix mat = transformation.clone();
        mat = mat.concatenate(new Matrix(textMatrix));
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
        BitmapExporter.export(swf, shape, null, image, mat, colorTransform);
    }

    public static void staticTextToImage(SWF swf, List<TEXTRECORD> textRecords, int numText, SerializableImage image, MATRIX textMatrix, Matrix transformation, ColorTransform colorTransform) {
        Color textColor = new Color(0, 0, 0);
        FontTag font = null;
        int textHeight = 12;
        int x = 0;
        int y = 0;
        Graphics2D g = (Graphics2D) image.getGraphics();
        List<SHAPE> glyphs = new ArrayList<>();
        for (TEXTRECORD rec : textRecords) {
            if (rec.styleFlagsHasColor) {
                if (numText == 2) {
                    textColor = colorTransform.apply(rec.textColorA.toColor());
                } else {
                    textColor = colorTransform.apply(rec.textColor.toColor());
                }
            }
            if (rec.styleFlagsHasFont) {
                font = (FontTag) swf.characters.get(rec.fontId);
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

            for (GLYPHENTRY entry : rec.glyphEntries) {
                Matrix mat = transformation.clone();
                mat = mat.concatenate(new Matrix(textMatrix));
                Matrix matTr = Matrix.getTranslateInstance(x, y);
                mat = mat.concatenate(matTr);
                mat = mat.concatenate(Matrix.getScaleInstance(rat));
                if (entry.glyphIndex != -1) {
                    // shapeNum: 1
                    SHAPE shape = glyphs.get(entry.glyphIndex);
                    BitmapExporter.export(swf, shape, textColor, image, mat, colorTransform);
                    x += entry.glyphAdvance;
                }
            }
        }
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        RECT r = getBounds();
        return new Rectangle(r.Xmin, r.Ymin, r.getWidth(), r.getHeight()); //TODO: match character shapes
    }
}
