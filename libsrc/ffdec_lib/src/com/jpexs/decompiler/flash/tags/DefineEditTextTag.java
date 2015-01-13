/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.dynamictext.CharacterWithStyle;
import com.jpexs.decompiler.flash.tags.dynamictext.DynamicTextModel;
import com.jpexs.decompiler.flash.tags.dynamictext.Paragraph;
import com.jpexs.decompiler.flash.tags.dynamictext.SameStyleTextRecord;
import com.jpexs.decompiler.flash.tags.dynamictext.TextStyle;
import com.jpexs.decompiler.flash.tags.dynamictext.Word;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 *
 * @author JPEXS
 */
public class DefineEditTextTag extends TextTag {

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
        MATRIX matrix = new MATRIX();
        matrix.translateX = bounds.Xmin;
        matrix.translateY = bounds.Ymin;
        return matrix;
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
    public List<String> getTexts() {
        String ret = "";
        if (hasText) {
            ret = initialText;
        }
        if (html) {
            ret = stripTags(ret);
            ret = entitiesReplace(ret);
        }
        return Arrays.asList(ret);
    }

    private List<CharacterWithStyle> getTextWithStyle() {
        String str = "";
        TextStyle style = new TextStyle();
        style.font = getFontTag();
        style.fontHeight = fontHeight;
        style.fontLeading = leading;
        if (hasTextColor) {
            style.textColor = textColor;
        }
        if (hasText) {
            str = initialText;
        }
        final List<CharacterWithStyle> ret = new ArrayList<>();
        if (html) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser;
            final Stack<TextStyle> styles = new Stack<>();
            styles.add(style);
            try {
                saxParser = factory.newSAXParser();
                DefaultHandler handler = new DefaultHandler() {

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                        TextStyle style = styles.peek();
                        switch (qName) {
                            case "p":
                                // todo: parse the following attribute:
                                // align
                                break;
                            case "b":
                                style = style.clone();
                                style.bold = true;
                                styles.add(style);
                                break;
                            case "i":
                                style = style.clone();
                                style.italic = true;
                                styles.add(style);
                                break;
                            case "u":
                                style = style.clone();
                                style.underlined = true;
                                styles.add(style);
                                break;
                            case "font":
                                style = style.clone();
                                String color = attributes.getValue("color");
                                if (color != null) {
                                    if (color.startsWith("#")) {
                                        style.textColor = new RGBA(Color.decode(color));
                                    }
                                }
                                String size = attributes.getValue("size");
                                if (size != null && size.length() > 0) {
                                    char firstChar = size.charAt(0);
                                    if (firstChar != '+' && firstChar != '-') {
                                        int fontSize = Integer.parseInt(size);
                                        style.fontHeight = (int) Math.round(fontSize * (style.font == null ? 1 : style.font.getDivider()));
                                        style.fontLeading = leading;
                                    } else {
                                        // todo: parse relative sizes
                                    }
                                }
                                // todo: parse the following attributes:
                                // face, letterSpacing, kerning
                                styles.add(style);
                                break;
                            case "br":
                            case "sbr": // what's this?
                                CharacterWithStyle cs = new CharacterWithStyle();
                                cs.character = '\n';
                                cs.style = style;
                                ret.add(cs);
                                break;
                        }
                        //ret = entitiesReplace(ret);
                    }

                    @Override
                    public void endElement(String uri, String localName, String qName) throws SAXException {
                        switch (qName) {
                            case "b":
                            case "i":
                            case "u":
                            case "font":
                                styles.pop();
                                break;
                            case "p":
                                TextStyle style = styles.peek();
                                CharacterWithStyle cs = new CharacterWithStyle();
                                cs.character = '\n';
                                cs.style = style;
                                ret.add(cs);
                                break;
                        }
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        String txt = new String(ch, start, length);
                        TextStyle style = styles.peek();
                        addCharacters(ret, txt, style);
                    }
                };
                str = "<!DOCTYPE html [\n"
                        + "    <!ENTITY nbsp \"&#160;\"> \n"
                        + "]><root>" + str + "</root>";
                saxParser.parse(new ByteArrayInputStream(str.getBytes(Utf8Helper.charset)), handler);
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            addCharacters(ret, str, style);
        }
        return ret;
    }

    private void addCharacters(List<CharacterWithStyle> list, String str, TextStyle style) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            CharacterWithStyle cs = new CharacterWithStyle();
            cs.character = ch;
            cs.style = style;
            list.add(cs);
        }
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
        String[] alignNames = {"left", "right", "center", "justify"};
        String alignment;
        if (align < alignNames.length) {
            alignment = alignNames[align];
        } else {
            alignment = "unknown";
        }
        ret += "\r\nxmin " + bounds.Xmin + "\r\nymin " + bounds.Ymin + "\r\nxmax " + bounds.Xmax + "\r\nymax " + bounds.Ymax + "\r\n";
        ret += (wordWrap ? "wordwrap 1\r\n" : "") + (multiline ? "multiline 1\r\n" : "")
                + (password ? "password 1\r\n" : "") + (readOnly ? "readonly 1\r\n" : "")
                + (autoSize ? "autosize 1\r\n" : "") + (noSelect ? "noselect 1\r\n" : "")
                + (border ? "border 1\r\n" : "") + (wasStatic ? "wasstatic 1\r\n" : "")
                + (html ? "html 1\r\n" : "") + (useOutlines ? "useoutlines 1\r\n" : "")
                + (hasFont ? "font " + fontId + "\r\n" + "height " + fontHeight + "\r\n" : "") + (hasTextColor ? "color " + textColor.toHexARGB() + "\r\n" : "")
                + (hasFontClass ? "fontclass " + fontClass + "\r\n" : "") + (hasMaxLength ? "maxlength " + maxLength + "\r\n" : "")
                + "align " + alignment + "\r\n"
                + (hasLayout ? "leftmargin " + leftMargin + "\r\nrightmargin " + rightMargin + "\r\nindent " + indent + "\r\nleading " + leading + "\r\n" : "")
                + (!variableName.isEmpty() ? "variablename " + variableName + "\r\n" : "");
        ret += "]";
        if (hasText) {
            ret += initialText.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
        }
        return ret;
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, String formattedText, String[] texts) throws TextParseException {
        try {
            TextLexer lexer = new TextLexer(new StringReader(formattedText));
            ParsedSymbol s = null;
            formattedText = "";
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

            int textIdx = 0;
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
                                    throw new TextParseException("Invalid xmin value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymin":
                                try {
                                    bounds.Ymin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymin value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "xmax":
                                try {
                                    bounds.Xmax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid xmax value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "ymax":
                                try {
                                    bounds.Ymax = Integer.parseInt(paramValue);
                                } catch (NumberFormatException nfe) {
                                    throw new TextParseException("Invalid ymax value. Number expected. Found: " + paramValue, lexer.yyline());
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
                                    throw new TextParseException("Invalid font value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "fontclass":
                                fontClass = paramValue;
                                break;
                            case "height":
                                try {
                                    fontHeight = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid height value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "color":
                                Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                                if (m.matches()) {
                                    textColor = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                                } else {
                                    throw new TextParseException("Invalid color. Valid format is #aarrggbb. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "maxlength":
                                try {
                                    maxLength = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid maxLength value. Number expected. Found: " + paramValue, lexer.yyline());
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
                                        throw new TextParseException("Invalid align value. Expected one of: left,right,center or justify. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "leftmargin":
                                try {
                                    leftMargin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid leftmargin value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "rightmargin":
                                try {
                                    rightMargin = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid rightmargin value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "indent":
                                try {
                                    indent = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid indent value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "leading":
                                try {
                                    leading = Integer.parseInt(paramValue);
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid leading value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "variablename":
                                variableName = paramValue;
                                break;
                            default:
                                throw new TextParseException("Unrecognized parameter name: " + paramName, lexer.yyline());
                        }
                        break;
                    case TEXT:
                        String s2 = (String) s.values[0];
                        if (s2 == null) {
                            s2 = "";
                        }
                        formattedText += (texts == null || textIdx >= texts.length) ? s2 : texts[textIdx++];
                        break;
                }
            }

            setModified(true);
            this.bounds = bounds;
            if (formattedText.length() > 0) {
                initialText = formattedText;
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
    public RECT getRect(Set<BoundedTag> added) {
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
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    /**
     * Constructor
     *
     * @param swf
     */
    public DefineEditTextTag(SWF swf) {
        super(swf, ID, "DefineEditText", null);
        characterID = swf.getNextCharacterId();
        bounds = new RECT();
        variableName = "";
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @throws IOException
     */
    public DefineEditTextTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, "DefineEditText", data);
        characterID = sis.readUI16("characterID");
        bounds = sis.readRECT("bounds");
        hasText = sis.readUB(1, "hasText") == 1;
        wordWrap = sis.readUB(1, "wordWrap") == 1;
        multiline = sis.readUB(1, "multiline") == 1;
        password = sis.readUB(1, "password") == 1;
        readOnly = sis.readUB(1, "readOnly") == 1;
        hasTextColor = sis.readUB(1, "hasTextColor") == 1;
        hasMaxLength = sis.readUB(1, "hasMaxLength") == 1;
        hasFont = sis.readUB(1, "hasFont") == 1;
        hasFontClass = sis.readUB(1, "hasFontClass") == 1;
        autoSize = sis.readUB(1, "autoSize") == 1;
        hasLayout = sis.readUB(1, "hasLayout") == 1;
        noSelect = sis.readUB(1, "noSelect") == 1;
        border = sis.readUB(1, "border") == 1;
        wasStatic = sis.readUB(1, "wasStatic") == 1;
        html = sis.readUB(1, "html") == 1;
        useOutlines = sis.readUB(1, "useOutlines") == 1;
        if (hasFont) {
            fontId = sis.readUI16("fontId");
        }
        if (hasFontClass) {
            fontClass = sis.readString("fontClass");
        }
        if (hasFont) {
            fontHeight = sis.readUI16("fontHeight");
        }
        if (hasTextColor) {
            textColor = sis.readRGBA("textColor");
        }
        if (hasMaxLength) {
            maxLength = sis.readUI16("maxLength");
        }
        if (hasLayout) {
            align = sis.readUI8("align"); //0 left, 1 right, 2 center, 3 justify
            leftMargin = sis.readUI16("leftMargin");
            rightMargin = sis.readUI16("rightMargin");
            indent = sis.readUI16("indent");
            leading = sis.readSI16("leading");
        }
        variableName = sis.readString("variableName");
        if (hasText) {
            initialText = sis.readString("initialText");
        }

    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        if (hasFont) {
            needed.add(fontId);
        }
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (fontId == characterId) {
            hasFont = false;
            fontId = 0;
            setModified(true);
            return true;
        }
        return false;
    }

    @Override
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        render(false, image, transformation, colorTransform);
    }

    private String render(boolean canvas, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        if (border) {
            // border is always black, fill color is always white?
            RGB borderColor = new RGBA(Color.black);
            RGB fillColor = new RGBA(Color.white);
            if (!canvas) {
                drawBorder(swf, image, borderColor, fillColor, getRect(new HashSet<BoundedTag>()), getTextMatrix(), transformation, colorTransform);
            } else {
                // TODO: draw border
            }
        }
        if (hasText) {
            DynamicTextModel textModel = new DynamicTextModel();
            List<CharacterWithStyle> txt = getTextWithStyle();
            TextStyle lastStyle = null;
            char prevChar = 0;
            boolean lastWasWhiteSpace = false;
            for (int i = 0; i < txt.size(); i++) {
                CharacterWithStyle cs = txt.get(i);
                char c = cs.character;
                if (c != '\r' && c != '\n') {
                    // create new SameStyleTextRecord for all words and all diffrent style text parts
                    if (lastWasWhiteSpace && !Character.isWhitespace(c)) {
                        textModel.newWord();
                        lastWasWhiteSpace = false;
                    }
                    if (cs.style != lastStyle) {
                        lastStyle = cs.style;
                        textModel.style = lastStyle;
                        textModel.newRecord();
                    }
                    Character nextChar = null;
                    if (i + 1 < txt.size()) {
                        nextChar = txt.get(i + 1).character;
                    }
                    int advance;
                    FontTag font = lastStyle.font;
                    GLYPHENTRY ge = new GLYPHENTRY();
                    ge.glyphIndex = font == null ? -1 : font.charToGlyph(c);
                    if (font != null && font.hasLayout()) {
                        int kerningAdjustment = 0;
                        if (nextChar != null) {
                            kerningAdjustment = font.getCharKerningAdjustment(c, nextChar);
                            kerningAdjustment /= font.getDivider();
                        }
                        advance = (int) Math.round(Math.round((double) lastStyle.fontHeight * (font.getGlyphAdvance(ge.glyphIndex) + kerningAdjustment) / (font.getDivider() * 1024.0)));
                    } else {
                        String fontName = FontTag.defaultFontName;
                        int fontStyle = font == null ? 0 : font.getFontStyle();
                        advance = (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, fontStyle, (int) (lastStyle.fontHeight / SWF.unitDivisor), c, nextChar));
                    }
                    ge.glyphAdvance = advance;
                    textModel.addGlyph(c, ge);
                    if (Character.isWhitespace(c)) {
                        lastWasWhiteSpace = true;
                    }
                } else {
                    if (c == '\r' || prevChar != '\r') {
                        if (multiline) {
                            textModel.newParagraph();
                        }
                    }
                }
                prevChar = c;
            }

            textModel.calculateTextWidths();
            List<List<SameStyleTextRecord>> lines;
            if (multiline && wordWrap) {
                lines = new ArrayList<>();
                int lineLength = 0;
                for (Paragraph paragraph : textModel.paragraphs) {
                    List<SameStyleTextRecord> line = new ArrayList<>();
                    for (Word word : paragraph.words) {
                        if (lineLength + word.width <= bounds.getWidth()) {
                            line.addAll(word.records);
                            lineLength += word.width;
                        } else {
                            lines.add(line);
                            line = new ArrayList<>();
                            line.addAll(word.records);
                            lineLength = 0;
                        }
                    }
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
            } else {
                lines = new ArrayList<>();
                for (Paragraph paragraph : textModel.paragraphs) {
                    List<SameStyleTextRecord> line = new ArrayList<>();
                    for (Word word : paragraph.words) {
                        for (SameStyleTextRecord tr : word.records) {
                            line.add(tr);
                        }
                    }
                    lines.add(line);
                }
            }

            // remove spaces after last word
            for (List<SameStyleTextRecord> line : lines) {
                boolean removed = true;
                while (removed) {
                    removed = false;
                    while (line.size() > 0 && line.get(line.size() - 1).glyphEntries.isEmpty()) {
                        line.remove(line.size() - 1);
                        removed = true;
                    }
                    if (line.size() > 0) {
                        SameStyleTextRecord lastRecord = line.get(line.size() - 1);
                        while (lastRecord.glyphEntries.size() > 0
                                && Character.isWhitespace(lastRecord.glyphEntries.get(lastRecord.glyphEntries.size() - 1).character)) {
                            lastRecord.glyphEntries.remove(lastRecord.glyphEntries.size() - 1);
                            removed = true;
                        }
                    }
                }
            }

            textModel.calculateTextWidths();

            List<TEXTRECORD> allTextRecords = new ArrayList<>();
            int yOffset = 0;
            for (List<SameStyleTextRecord> line : lines) {
                int width = 0;
                int currentOffset = 0;
                for (SameStyleTextRecord tr : line) {
                    width += tr.width;
                    if (tr.style.fontHeight + tr.style.fontLeading > currentOffset) {
                        currentOffset = tr.style.fontHeight + tr.style.fontLeading;
                    }
                }
                yOffset += currentOffset;
                int alignOffset = 0;
                switch (align) {
                    case 0: // left
                        alignOffset = 0;
                        break;
                    case 1: // right
                        alignOffset = bounds.getWidth() - width;
                        break;
                    case 2: // center
                        alignOffset = (bounds.getWidth() - width) / 2;
                        break;
                    case 3: // justify
                        // todo;
                        break;
                }
                for (SameStyleTextRecord tr : line) {
                    tr.xOffset = alignOffset;
                    alignOffset += tr.width;
                }
                for (SameStyleTextRecord tr : line) {
                    TEXTRECORD tr2 = new TEXTRECORD();
                    tr2.styleFlagsHasFont = fontId != 0;
                    tr2.fontId = fontId;
                    tr2.textHeight = tr.style.fontHeight;
                    if (tr.style.textColor != null) {
                        tr2.styleFlagsHasColor = true;
                        tr2.textColorA = tr.style.textColor;
                    }
                    // always add xOffset, because no xOffset and 0 xOffset is diffrent in text rendering
                    tr2.styleFlagsHasXOffset = true;
                    tr2.xOffset = tr.xOffset;
                    if (yOffset != 0) {
                        tr2.styleFlagsHasYOffset = true;
                        tr2.yOffset = yOffset;
                    }
                    tr2.glyphEntries = new GLYPHENTRY[tr.glyphEntries.size()];
                    for (int i = 0; i < tr2.glyphEntries.length; i++) {
                        tr2.glyphEntries[i] = tr.glyphEntries.get(i).glyphEntry;
                    }
                    allTextRecords.add(tr2);
                }
            }

            if (canvas) {
                return staticTextToHtmlCanvas(1, swf, allTextRecords, 2, getBounds(), getTextMatrix(), colorTransform);
            } else {
                staticTextToImage(swf, allTextRecords, 2, image, getTextMatrix(), transformation, colorTransform);
            }
        }
        
        return "";
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ExportRectangle calculateTextBounds() {
        return null;
    }

    private FontTag getFontTag() {
        FontTag font = null;
        for (Tag tag : swf.tags) {
            if (tag instanceof FontTag) {
                if (((FontTag) tag).getFontId() == fontId) {
                    font = (FontTag) tag;
                }
            }
        }
        return font;
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        return render(true, null, new Matrix(), new ColorTransform());
    }
}
