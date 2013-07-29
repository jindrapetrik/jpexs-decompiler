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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public abstract class TextTag extends CharacterTag implements BoundedTag {

    public TextTag(SWF swf, int id, String name, byte[] data, long pos) {
        super(swf, id, name, data, pos);
    }

    public abstract MATRIX getTextMatrix();

    public abstract String getText(List<Tag> tags);

    public abstract String getFormattedText(List<Tag> tags);

    public abstract boolean setFormattedText(MissingCharacterHandler missingCharHandler, List<Tag> tags, String text, String fontName) throws ParseException;

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

        List<String> availableFonts = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

        List<SHAPE> glyphs = new ArrayList<>();
        boolean firstLine = true;
        double top = 0;
        List<Integer> allLeftMargins = new ArrayList<>();
        List<Integer> allLetterSpacings = new ArrayList<>();
        FontMetrics fontMetrics = null;
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Font aFont = null;
        int currentLeftMargin = 0;
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

                if (font.getLeading() == -1) {
                    String fontName = font.getFontName(tags);
                    if (!availableFonts.contains(fontName)) {
                        fontName = "Times New Roman";
                    }
                    if (!availableFonts.contains(fontName)) {
                        fontName = "Arial";
                    }
                    aFont = new Font(fontName, font.getFontStyle(), textHeight / 20);
                    fontMetrics = bi.getGraphics().getFontMetrics(aFont);
                    LineMetrics lm = fontMetrics.getLineMetrics("A", bi.getGraphics());
                    ascent = lm.getAscent();
                    descent = lm.getDescent();
                    leading = lm.getLeading();
                    lineDistance = ascent + descent;
                } else {
                    leading = ((double) font.getLeading() * textHeight / 1024.0 / font.getDivider() / 20.0);
                    ascent = ((double) font.getAscent() * textHeight / 1024.0 / font.getDivider() / 20.0);
                    descent = ((double) font.getDescent() * textHeight / 1024.0 / font.getDivider() / 20.0);
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
                    int topint = 20 * (int) Math.round(top);
                    lineSpacing = rec.yOffset - topint;
                    top += ((double) lineSpacing) / 20;
                } else {
                    top = ascent - 2.0; //I don't know why, but there are always 2 pixels
                }
                y = rec.yOffset;
            }
            firstLine = false;
            String lineStr = "";
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
                int defaultAdvance = 20 * FontTag.getSystemFontAdvance(aFont, font.glyphToChar(tags, entry.glyphIndex));
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
}
