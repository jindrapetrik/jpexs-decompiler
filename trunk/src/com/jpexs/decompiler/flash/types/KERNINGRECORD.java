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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.awt.Font;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Represents 24-bit red, green, blue value
 *
 * @author JPEXS
 */
public class KERNINGRECORD {

    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode1;

    @SWFType(value = BasicType.UI8, alternateValue = BasicType.UI16, alternateCondition = "fontFlagsWideCodes")
    public int fontKerningCode2;

    @SWFType(BasicType.SI16)
    public int fontKerningAdjustment;

    @Override
    public String toString() {
        return "[KERNINGRECORD fontKerningCode1=" + fontKerningCode1 + ", fontKerningCode2=" + fontKerningCode2 + ", fontKerningAdjustment=" + fontKerningAdjustment + "]";
    }

    public KERNINGRECORD(int fontKerningCode1, int fontKerningCode2, int fontKerningAdjustment) {
        this.fontKerningCode1 = fontKerningCode1;
        this.fontKerningCode2 = fontKerningCode2;
        this.fontKerningAdjustment = fontKerningAdjustment;
    }

    public KERNINGRECORD() {
    }

    public KERNINGRECORD(Font font, char char1, char char2) {
        fontKerningCode1 = char1;
        fontKerningCode2 = char2;
        fontKerningAdjustment = KERNINGRECORD.getSystemFontKerning(font, char1, char2);
    }

    public KERNINGRECORD(String fontName, int fontStyle, int fontSize, char char1, char char2) {
        fontKerningCode1 = char1;
        fontKerningCode2 = char2;
        fontKerningAdjustment = KERNINGRECORD.getSystemFontKerning(fontName, fontSize, fontStyle, char1, char2);
    }

    public static int getSystemFontKerning(Font font, char char1, char char2) {
        return getSystemFontKerning(font.getFamily(), font.getSize(), font.getStyle(), char1, char2);
    }

    public static int getSystemFontKerning(String fontName, int fontSize, int fontStyle, char char1, char char2) {
        char[] chars = new char[]{char1, char2};
        Map<AttributedCharacterIterator.Attribute, Object> withKerningAttrs = new HashMap<>();

        withKerningAttrs.put(TextAttribute.FAMILY, fontName);
        if ((fontStyle & Font.BOLD) == Font.BOLD) {
            withKerningAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if ((fontStyle & Font.ITALIC) == Font.ITALIC) {
            withKerningAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        withKerningAttrs.put(TextAttribute.SIZE, (float) fontSize);
        withKerningAttrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        Font withKerningFont = Font.getFont(withKerningAttrs);
        GlyphVector withKerningVector = withKerningFont.layoutGlyphVector((new JPanel()).getFontMetrics(withKerningFont).getFontRenderContext(), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        int withKerningX = withKerningVector.getGlyphLogicalBounds(1).getBounds().x;

        Map<AttributedCharacterIterator.Attribute, Object> noKerningAttrs = new HashMap<>();
        noKerningAttrs.put(TextAttribute.FAMILY, fontName);
        if ((fontStyle & Font.BOLD) == Font.BOLD) {
            noKerningAttrs.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        }
        if ((fontStyle & Font.ITALIC) == Font.ITALIC) {
            noKerningAttrs.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        }
        noKerningAttrs.put(TextAttribute.SIZE, (float) fontSize);
        Font noKerningFont = Font.getFont(noKerningAttrs);
        GlyphVector noKerningVector = noKerningFont.layoutGlyphVector((new JPanel()).getFontMetrics(noKerningFont).getFontRenderContext(), chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
        int noKerningX = noKerningVector.getGlyphLogicalBounds(1).getBounds().x;
        return withKerningX - noKerningX;
    }
}
