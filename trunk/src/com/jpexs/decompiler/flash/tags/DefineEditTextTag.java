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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.exporters.Point;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author JPEXS
 */
public class DefineEditTextTag extends TextTag implements DrawableTag {

    @SWFType(BasicType.UI16)
    public int characterID;

    public RECT bounds;
    public boolean hasText;
    public boolean wordWrap;
    public boolean multiline;
    public boolean password;
    public boolean readOnly;
    public boolean hasTextColor;
    public boolean hasMaxLength;
    public boolean hasFont;
    public boolean hasFontClass;
    public boolean autoSize;
    public boolean hasLayout;
    public boolean noSelect;
    public boolean border;
    public boolean wasStatic;
    public boolean html;
    public boolean useOutlines;

    @SWFType(BasicType.UI16)
    @Conditional("hasFont")
    public int fontId;

    @Conditional("hasFontClass")
    public String fontClass;

    @SWFType(BasicType.UI16)
    @Conditional("hasFont")
    public int fontHeight;

    @Conditional("hasTextColor")
    public RGBA textColor;

    @SWFType(BasicType.UI16)
    @Conditional("hasMaxLength")
    public int maxLength;

    @SWFType(BasicType.UI8)
    @Conditional("hasLayout")
    public int align;

    @SWFType(BasicType.UI16)
    @Conditional("hasLayout")
    public int leftMargin;

    @SWFType(BasicType.UI16)
    @Conditional("hasLayout")
    public int rightMargin;

    @SWFType(BasicType.UI16)
    @Conditional("hasLayout")
    public int indent;

    @SWFType(BasicType.SI16)
    @Conditional("hasLayout")
    public int leading;

    public String variableName;

    @Conditional("hasText")
    public String initialText;

    public static final int ID = 37;

    @Override
    public RECT getBounds() {
        return bounds;
    }

    @Override
    public MATRIX getTextMatrix() {
        return new MATRIX();
    }

    @Override
    public void setBounds(RECT r) {
        bounds = r;
    }

    private String stripTags(String inp) {
        boolean intag = false;
        String outp = "";
        inp = inp.replaceAll("<br ?/?>", "\r\n");
        for (int i = 0; i < inp.length(); ++i) {
            if (!intag && inp.charAt(i) == '<') {
                intag = true;
                continue;
            }
            if (intag && inp.charAt(i) == '>') {
                intag = false;
                continue;
            }
            if (!intag) {
                outp += inp.charAt(i);
            }
        }
        return outp;
    }

    private String entitiesReplace(String s) {
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        s = s.replace("&quot;", "\"");
        return s;
    }

    @Override
    public String getText() {
        String ret = "";
        if (hasText) {
            ret = initialText;
        }
        if (html) {
            ret = stripTags(ret);
            ret = entitiesReplace(ret);
        }
        return ret;
    }

    @Override
    public List<Integer> getFontIds() {
        List<Integer> ret = new ArrayList<>();
        ret.add(fontId);
        return ret;
    }

    @Override
    public String getFormattedText() {
        String ret = "";
        ret += "[";
        String[] alignValues = {"left", "right", "center", "justify"};
        ret += "\r\nxmin " + bounds.Xmin + "\r\nymin " + bounds.Ymin + "\r\nxmax " + bounds.Xmax + "\r\nymax " + bounds.Ymax + "\r\n";
        ret += (wordWrap ? "wordwrap 1\r\n" : "") + (multiline ? "multiline 1\r\n" : "")
                + (password ? "password 1\r\n" : "") + (readOnly ? "readonly 1\r\n" : "")
                + (autoSize ? "autosize 1\r\n" : "") + (noSelect ? "noselect 1\r\n" : "")
                + (border ? "border 1\r\n" : "") + (wasStatic ? "wasstatic 1\r\n" : "")
                + (html ? "html 1\r\n" : "") + (useOutlines ? "useoutlines 1\r\n" : "")
                + (hasFont ? "font " + fontId + "\r\n" + "height " + fontHeight + "\r\n" : "") + (hasTextColor ? "color " + textColor.toHexARGB() + "\r\n" : "")
                + (hasFontClass ? "fontclass " + fontClass + "\r\n" : "") + (hasMaxLength ? "maxlength " + maxLength + "\r\n" : "")
                + "align " + alignValues[align] + "\r\n"
                + (hasLayout ? "leftmargin " + leftMargin + "\r\nrightmargin " + rightMargin + "\r\nindent " + indent + "\r\nleading " + leading + "\r\n" : "")
                + (!variableName.isEmpty() ? "variablename " + variableName + "\r\n" : "");
        ret += "]";
        if (hasText) {
            ret += initialText.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
        }
        return ret;
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, String text) throws ParseException {
        try {
            TextLexer lexer = new TextLexer(new StringReader(text));
            ParsedSymbol s = null;
            text = "";
            RECT bounds = new RECT(this.bounds);
            boolean wordWrap = false;
            boolean multiline = false;
            boolean password = false;
            boolean readOnly = false;
            boolean autoSize = false;
            boolean noSelect = false;
            boolean border = false;
            boolean wasStatic = false;
            boolean html = false;
            boolean useOutlines = false;
            int fontId = -1;
            int fontHeight = -1;
            String fontClass = null;
            RGBA textColor = null;
            int maxLength = -1;
            int align = -1;
            int leftMargin = -1;
            int rightMargin = -1;
            int indent = -1;
            int leading = -1;
            String variableName = null;

            while ((s = lexer.yylex()) != null) {
                switch (s.type) {
                    case PARAMETER:
                        String paramName = (String) s.values[0];
                        String paramValue = (String) s.values[1];
                        switch (paramName) {
                            case "xmin":
                                try {
                                    bounds.Xmin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new ParseException("Invalid xmin value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "ymin":
                                try {
                                    bounds.Ymin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new ParseException("Invalid ymin value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "xmax":
                                try {
                                    bounds.Xmax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new ParseException("Invalid xmax value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "ymax":
                                try {
                                    bounds.Ymax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new ParseException("Invalid ymax value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "wordwrap":
                                if (paramValue.equals("1")) {
                                    wordWrap = true;
                                }
                                break;
                            case "multiline":
                                if (paramValue.equals("1")) {
                                    multiline = true;
                                }
                                break;
                            case "password":
                                if (paramValue.equals("1")) {
                                    password = true;
                                }
                                break;
                            case "readonly":
                                if (paramValue.equals("1")) {
                                    readOnly = true;
                                }
                                break;
                            case "autosize":
                                if (paramValue.equals("1")) {
                                    autoSize = true;
                                }
                                break;
                            case "noselect":
                                if (paramValue.equals("1")) {
                                    noSelect = true;
                                }
                                break;
                            case "border":
                                if (paramValue.equals("1")) {
                                    border = true;
                                }
                                break;
                            case "wasstatic":
                                if (paramValue.equals("1")) {
                                    wasStatic = true;
                                }
                                break;
                            case "html":
                                if (paramValue.equals("1")) {
                                    html = true;
                                }
                                break;
                            case "useoutlines":
                                if (paramValue.equals("1")) {
                                    useOutlines = true;
                                }
                                break;
                            case "font":
                                try {
                                    fontId = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid font value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "fontclass":
                                fontClass = paramValue;
                                break;
                            case "height":
                                try {
                                    fontHeight = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid height value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "color":
                                Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                if (m.matches()) {
                                    textColor = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                                } else {
                                    throw new ParseException("Invalid color. Valid format is #aarrggbb.", lexer.yyline());
                                }
                                break;
                            case "maxlength":
                                try {
                                    maxLength = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid maxLength value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "align":
                                switch (paramValue) {
                                    case "left":
                                        align = 0;
                                        break;
                                    case "right":
                                        align = 1;
                                        break;
                                    case "center":
                                        align = 2;
                                        break;
                                    case "justify":
                                        align = 3;
                                        break;
                                    default:
                                        throw new ParseException("Invalid align value. Expected one of: left,right,center or justify.", lexer.yyline());
                                }
                                break;
                            case "leftmargin":
                                try {
                                    leftMargin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid leftmargin value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "rightmargin":
                                try {
                                    rightMargin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid rightmargin value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "indent":
                                try {
                                    indent = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid indent value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "leading":
                                try {
                                    leading = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new ParseException("Invalid leading value. Number expected.", lexer.yyline());
                                }
                                break;
                            case "variablename":
                                variableName = paramValue;
                                break;
                            default:
                                throw new ParseException("Unrecognized parameter name", lexer.yyline());
                        }
                        break;
                    case TEXT:
                        text += (String) s.values[0];
                        break;
                }
            }

            this.bounds = bounds;
            if (text.length() > 0) {
                initialText = text;
                this.hasText = true;
            } else {
                this.hasText = false;
            }
            this.wordWrap = wordWrap;
            this.multiline = multiline;
            this.password = password;
            this.readOnly = readOnly;
            this.noSelect = noSelect;
            this.border = border;
            this.wasStatic = wasStatic;
            this.html = html;
            this.useOutlines = useOutlines;
            if (textColor != null) {
                hasTextColor = true;
                this.textColor = textColor;
            }
            if (maxLength > -1) {
                this.maxLength = maxLength;
                hasMaxLength = true;
            }
            if (fontId > -1) {
                this.fontId = fontId;
            }
            if (fontHeight > -1) {
                this.fontHeight = fontHeight;
            }
            if (fontClass != null) {
                this.fontClass = fontClass;
                hasFontClass = true;
            }
            this.autoSize = autoSize;
            if ((leftMargin > -1)
                    || (rightMargin > -1)
                    || (indent > -1)
                    || (leading > -1)) {
                this.leftMargin = leftMargin;
                this.rightMargin = rightMargin;
                this.indent = indent;
                this.leading = leading;
                hasLayout = true;
            }
            if (variableName == null) {
                variableName = "";
            }
            this.variableName = variableName;

        } catch (IOException ex) {
            Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;

    }

    @Override
    public RECT getRect(Map<Integer, CharacterTag> allCharacters, Stack<Integer> visited) {
        return bounds;
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(characterID);
            sos.writeRECT(bounds);
            sos.writeUB(1, hasText ? 1 : 0);
            sos.writeUB(1, wordWrap ? 1 : 0);
            sos.writeUB(1, multiline ? 1 : 0);
            sos.writeUB(1, password ? 1 : 0);
            sos.writeUB(1, readOnly ? 1 : 0);
            sos.writeUB(1, hasTextColor ? 1 : 0);
            sos.writeUB(1, hasMaxLength ? 1 : 0);
            sos.writeUB(1, hasFont ? 1 : 0);
            sos.writeUB(1, hasFontClass ? 1 : 0);
            sos.writeUB(1, autoSize ? 1 : 0);
            sos.writeUB(1, hasLayout ? 1 : 0);
            sos.writeUB(1, noSelect ? 1 : 0);
            sos.writeUB(1, border ? 1 : 0);
            sos.writeUB(1, wasStatic ? 1 : 0);
            sos.writeUB(1, html ? 1 : 0);
            sos.writeUB(1, useOutlines ? 1 : 0);
            if (hasFont) {
                sos.writeUI16(fontId);
            }
            if (hasFontClass) {
                sos.writeString(fontClass);
            }
            if (hasFont) {
                sos.writeUI16(fontHeight);
            }
            if (hasTextColor) {
                sos.writeRGBA(textColor);
            }
            if (hasMaxLength) {
                sos.writeUI16(maxLength);
            }
            if (hasLayout) {
                sos.writeUI8(align);
                sos.writeUI16(leftMargin);
                sos.writeUI16(rightMargin);
                sos.writeUI16(indent);
                sos.writeSI16(leading);
            }
            sos.writeString(variableName);
            if (hasText) {
                sos.writeString(initialText);
            }

        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public DefineEditTextTag(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineEditText", data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        characterID = sis.readUI16();
        bounds = sis.readRECT();
        hasText = sis.readUB(1) == 1;
        wordWrap = sis.readUB(1) == 1;
        multiline = sis.readUB(1) == 1;
        password = sis.readUB(1) == 1;
        readOnly = sis.readUB(1) == 1;
        hasTextColor = sis.readUB(1) == 1;
        hasMaxLength = sis.readUB(1) == 1;
        hasFont = sis.readUB(1) == 1;
        hasFontClass = sis.readUB(1) == 1;
        autoSize = sis.readUB(1) == 1;
        hasLayout = sis.readUB(1) == 1;
        noSelect = sis.readUB(1) == 1;
        border = sis.readUB(1) == 1;
        wasStatic = sis.readUB(1) == 1;
        html = sis.readUB(1) == 1;
        useOutlines = sis.readUB(1) == 1;
        if (hasFont) {
            fontId = sis.readUI16();
        }
        if (hasFontClass) {
            fontClass = sis.readString();
        }
        if (hasFont) {
            fontHeight = sis.readUI16();
        }
        if (hasTextColor) {
            textColor = sis.readRGBA();
        }
        if (hasMaxLength) {
            maxLength = sis.readUI16();
        }
        if (hasLayout) {
            align = sis.readUI8(); //0 left,1 right, 2 center, 3 justify
            leftMargin = sis.readUI16();
            rightMargin = sis.readUI16();
            indent = sis.readUI16();
            leading = sis.readSI16();
        }
        variableName = sis.readString();
        if (hasText) {
            initialText = sis.readString();
        }

    }

    @Override
    public Set<Integer> getNeededCharacters() {
        HashSet<Integer> needed = new HashSet<>();
        if (hasFont) {
            needed.add(fontId);
        }
        return needed;
    }

    @Override
    public SerializableImage toImage(int frame, List<Tag> tags, Map<Integer, CharacterTag> characters, Stack<Integer> visited, Matrix transformation) {
        throw new Error("this overload of toImage call is not supported on BoundedTag");
    }

    @Override
    public void toImage(int frame, List<Tag> tags, Map<Integer, CharacterTag> characters, Stack<Integer> visited, SerializableImage image, Matrix transformation) {
        FontTag font = null;
        for (Tag tag : tags) {
            if (tag instanceof FontTag) {
                if (((FontTag) tag).getFontId() == fontId) {
                    font = (FontTag) tag;
                }
            }
        }
        if (hasText) {
            List<TEXTRECORD> textRecords = new ArrayList<>();
            TEXTRECORD tr = new TEXTRECORD();
            tr.styleFlagsHasFont = true;
            tr.fontId = fontId;
            tr.textHeight = fontHeight;
            tr.styleFlagsHasYOffset = true;
            tr.yOffset = fontHeight;
            String txt = getText();
            tr.glyphEntries = new GLYPHENTRY[txt.length()];
            int width = 0;
            for (int i = 0; i < txt.length(); i++) {
                char c = txt.charAt(i);
                Character nextChar = null;
                if (i + 1 < txt.length()) {
                    nextChar = txt.charAt(i + 1);
                }
                int advance;
                tr.glyphEntries[i] = new GLYPHENTRY();
                tr.glyphEntries[i].glyphIndex = font.charToGlyph(tags, c);
                if (font.hasLayout()) {
                    int kerningAdjustment = 0;
                    if (nextChar != null) {
                        kerningAdjustment = font.getGlyphKerningAdjustment(tags, tr.glyphEntries[i].glyphIndex, font.charToGlyph(tags, nextChar));
                        kerningAdjustment /= font.getDivider();
                    }
                    advance = (int) Math.round(font.getDivider() * Math.round((double) fontHeight * (font.getGlyphAdvance(tr.glyphEntries[i].glyphIndex) + kerningAdjustment) / (font.getDivider() * 1024.0)));
                } else {
                    String fontName = FontTag.defaultFontName;
                    advance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, font.getFontStyle(), (int) (fontHeight / SWF.unitDivisor), c, nextChar));
                }
                tr.glyphEntries[i].glyphAdvance = advance;
                width += advance;
            }
            switch (align) {
                case 1: // right
                    tr.styleFlagsHasXOffset = true;
                    tr.xOffset = bounds.getWidth() - width;
                    break;
                case 2: // center
                    tr.styleFlagsHasXOffset = true;
                    tr.xOffset = (int) ((bounds.getWidth() - width) / 2);
                    break;
                case 3: // justify
                    // todo;
                    break;
            }
            if (hasTextColor) {
                tr.styleFlagsHasColor = true;
                tr.textColorA = textColor;
            }
            textRecords.add(tr);
            staticTextToImage(swf, characters, textRecords, 2, image, getTextMatrix(), transformation);
        }
    }

    @Override
    public Point getImagePos(int frame, Map<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return new Point(bounds.Xmin / SWF.unitDivisor, bounds.Ymin / SWF.unitDivisor);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }
}
