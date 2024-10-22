/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.dynamictext.AdvancedTextRecord;
import com.jpexs.decompiler.flash.tags.dynamictext.CharacterWithStyle;
import com.jpexs.decompiler.flash.tags.dynamictext.DynamicTextModel;
import com.jpexs.decompiler.flash.tags.dynamictext.GlyphCharacter;
import com.jpexs.decompiler.flash.tags.dynamictext.Paragraph;
import com.jpexs.decompiler.flash.tags.dynamictext.SameStyleTextRecord;
import com.jpexs.decompiler.flash.tags.dynamictext.TextStyle;
import com.jpexs.decompiler.flash.tags.dynamictext.Word;
import com.jpexs.decompiler.flash.tags.enums.TextRenderMode;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.SymbolType;
import com.jpexs.decompiler.flash.tags.text.TextAlign;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.tags.text.TextParseException;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.DynamicTextGlyphEntry;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.Multiline;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
 * DefineEditText tag - defines an editable text field.
 *
 * @author JPEXS
 */
@SWFVersion(from = 4)
public class DefineEditTextTag extends TextTag {

    public static final int ID = 37;

    public static final String NAME = "DefineEditText";

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
    @Conditional("hasFont|hasFontClass")
    public int fontHeight;

    @Conditional("hasTextColor")
    public RGBA textColor;

    @SWFType(BasicType.UI16)
    @Conditional("hasMaxLength")
    public int maxLength;

    @SWFType(BasicType.UI8)
    @Conditional("hasLayout")
    @EnumValue(value = ALIGN_LEFT, text = "Left")
    @EnumValue(value = ALIGN_RIGHT, text = "Right")
    @EnumValue(value = ALIGN_CENTER, text = "Center")
    @EnumValue(value = ALIGN_JUSTIFY, text = "Justify")
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
    @Multiline
    public String initialText;

    public static final int ALIGN_LEFT = 0;

    public static final int ALIGN_RIGHT = 1;

    public static final int ALIGN_CENTER = 2;

    public static final int ALIGN_JUSTIFY = 3;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineEditTextTag(SWF swf) {
        super(swf, ID, NAME, null);
        characterID = swf.getNextCharacterId();
        bounds = new RECT();
        variableName = "";
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineEditTextTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
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
        // condition is wrong in the documentation
        if (hasFont || hasFontClass) {
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

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
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
        if (hasFont || hasFontClass) {
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
    }

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
        if (swf == null) {
            return new ArrayList<>();
        }
        String str = "";
        TextStyle style = new TextStyle();
        if (fontClass != null) {
            style.font = swf.getFontByClass(fontClass);
        } else {
            style.font = swf.getFont(fontId);
        }
        style.fontHeight = fontHeight;
        style.fontLeading = leading;
        if (hasTextColor) {
            style.textColor = textColor;
        }
        if (hasText) {
            str = initialText;
        }
        style.leftMargin = leftMargin;
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
                            case "a":
                                // todo: handle link - href, target attributes
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
                                String color = unescape(attributes.getValue("color"));
                                if (color != null) {
                                    if (color.startsWith("#")) {
                                        style.textColor = new RGBA(Color.decode(color));
                                    }
                                }
                                String size = unescape(attributes.getValue("size"));
                                if (size != null && size.length() > 0) {
                                    char firstChar = size.charAt(0);
                                    if (firstChar != '+' && firstChar != '-') {
                                        int fontSize = Integer.parseInt(size);
                                        style.fontHeight = (int) Math.round(fontSize * SWF.unitDivisor);
                                    } else {
                                        int fontSizeDelta = (int) Math.round(Integer.parseInt(size.substring(1)) * SWF.unitDivisor);
                                        if (firstChar == '+') {
                                            style.fontHeight = style.fontHeight + fontSizeDelta;
                                        } else {
                                            style.fontHeight = style.fontHeight - fontSizeDelta;
                                        }
                                    }
                                    style.fontLeading = leading;
                                }
                                String face = unescape(attributes.getValue("face"));

                                if (face != null && face.length() > 0) {
                                    style.fontFace = face;
                                }

                                String letterspacing = unescape(attributes.getValue("letterSpacing"));
                                if (letterspacing != null && letterspacing.length() > 0) {
                                    style.letterSpacing = Double.parseDouble(letterspacing);
                                }

                                String kerning = unescape(attributes.getValue("kerning"));
                                if (kerning != null && kerning.length() > 0) {
                                    style.kerning = kerning.equals("1");
                                }

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

                    private String unescape(String txt) {
                        if (txt == null) {
                            return null;
                        }
                        txt = txt.replace("/{entity-nbsp}", "\u00A0");
                        txt = txt.replace("/{entity-lt}", "<");
                        txt = txt.replace("/{entity-gt}", ">");
                        txt = txt.replace("/{entity-quot}", "\"");
                        txt = txt.replace("/{entity-amp}", "&");
                        txt = txt.replace("/{entity-apos}", "'");
                        return txt;
                    }

                    @Override
                    public void characters(char[] ch, int start, int length) throws SAXException {
                        String txt = unescape(new String(ch, start, length));
                        TextStyle style = styles.peek();
                        if (style.fontFace != null && useOutlines) {
                            CharacterTag ct = swf.getCharacterByExportName(style.fontFace);
                            if (ct != null && (ct instanceof FontTag)) {
                                style.font = (FontTag) ct;
                            } else {
                                style.font = swf.getFontByNameInTag(style.fontFace, style.bold, style.italic);
                            }
                            if (style.font == null) {
                                style.fontFace = null;
                            }
                        }
                        addCharacters(ret, txt, style);
                    }
                };

                str = str.replace("&nbsp;", "/{entity-nbsp}");
                str = str.replace("&lt;", "/{entity-lt}");
                str = str.replace("&gt;", "/{entity-gt}");
                str = str.replace("&quot;", "/{entity-quot}");
                str = str.replace("&amp;", "/{entity-amp}");
                str = str.replace("&apos;", "/{entity-apos}");
                str = str.replace("&", "&amp;");

                str = "<!DOCTYPE html [\n"
                        + "]><root>" + str + "</root>";
                saxParser.parse(new ByteArrayInputStream(str.getBytes(Utf8Helper.charset)), handler);
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(DefineEditTextTag.class.getName()).log(Level.SEVERE, "Error parsing text " + getCharacterId(), ex);
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
    public HighlightedText getFormattedText(boolean ignoreLetterSpacing) {
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), true);
        writer.append("[");
        String[] alignNames = {"left", "right", "center", "justify"};
        String alignment;
        if (align < alignNames.length) {
            alignment = alignNames[align];
        } else {
            alignment = "unknown";
        }
        writer.newLine();
        writer.append("xmin ").append(bounds.Xmin).newLine();
        writer.append("ymin ").append(bounds.Ymin).newLine();
        writer.append("xmax ").append(bounds.Xmax).newLine();
        writer.append("ymax ").append(bounds.Ymax).newLine();
        if (wordWrap) {
            writer.append("wordwrap 1").newLine();
        }
        if (multiline) {
            writer.append("multiline 1").newLine();
        }
        if (password) {
            writer.append("password 1").newLine();
        }
        if (readOnly) {
            writer.append("readonly 1").newLine();
        }
        if (autoSize) {
            writer.append("autosize 1").newLine();
        }
        if (noSelect) {
            writer.append("noselect 1").newLine();
        }
        if (border) {
            writer.append("border 1").newLine();
        }
        if (wasStatic) {
            writer.append("wasstatic 1").newLine();
        }
        if (html) {
            writer.append("html 1").newLine();
        }
        if (useOutlines) {
            writer.append("useoutlines 1").newLine();
        }
        if (hasFont) {
            writer.append("font ").append(fontId).newLine();
            writer.append("height ").append(fontHeight).newLine();
        }
        if (hasTextColor) {
            writer.append("color ").append(textColor.toHexARGB()).newLine();
        }
        if (hasFontClass) {
            writer.append("fontclass ").append(fontClass).newLine();
        }
        if (hasMaxLength) {
            writer.append("maxlength ").append(maxLength).newLine();
        }
        writer.append("align ").append(alignment).newLine();
        if (hasLayout) {
            writer.append("leftmargin ").append(leftMargin).newLine();
            writer.append("rightmargin ").append(rightMargin).newLine();
            writer.append("indent ").append(indent).newLine();
            writer.append("leading ").append(leading).newLine();
        }
        if (!variableName.isEmpty()) {
            writer.append("variablename ").append(variableName).newLine();
        }
        writer.append("]");
        if (hasText) {
            String text = initialText.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
            writer.hilightSpecial(text, HighlightSpecialType.TEXT);
        }
        writer.finishHilights();
        return new HighlightedText(writer);
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, String formattedText, String[] texts) throws TextParseException {
        try {
            TextLexer lexer = new TextLexer(new StringReader(formattedText));
            ParsedSymbol s;
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
                    case PARAMETER_IDENTIFIER:
                        String paramName = (String) s.value;
                        s = lexer.yylex();                        
                        String paramValue = (String) s.value;
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
                            case "font"://note: height parameter must be also present
                                try {
                                    fontId = Integer.parseInt(paramValue);

                                    FontTag ft = swf.getFont(fontId);
                                    if (ft == null) {
                                        throw new TextParseException("Font not found.", lexer.yyline());
                                    }
                                    hasFont = true;
                                } catch (NumberFormatException ne) {
                                    throw new TextParseException("Invalid font value. Number expected. Found: " + paramValue, lexer.yyline());
                                }
                                break;
                            case "fontclass":
                                fontClass = paramValue;
                                break;
                            case "height":
                                try { //TODO: font parameter must be also present
                                    fontHeight = Integer.parseInt(paramValue);
                                    hasFont = true;
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
                        String s2 = (String) s.value;
                        if (s2 == null) {
                            s2 = "";
                        }

                        formattedText += (texts == null || textIdx >= texts.length) ? s2 : texts[textIdx++];
                        formattedText = formattedText.replace("\r\n", "\r");
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
            } else {
                hasTextColor = false;
            }

            if (maxLength > -1) {
                this.maxLength = maxLength;
                hasMaxLength = true;
            } else {
                hasMaxLength = false;
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
            } else {
                hasFontClass = false;
            }

            this.autoSize = autoSize;
            this.align = align;
            if (leftMargin > -1 || rightMargin > -1 || indent > -1 || leading > -1) {
                this.leftMargin = leftMargin;
                this.rightMargin = rightMargin;
                this.indent = indent;
                this.leading = leading;
                hasLayout = true;
            } else {
                hasLayout = false;
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
    public void updateTextBounds() {
    }

    @Override
    public boolean alignText(TextAlign textAlign) {
        return true;
    }

    @Override
    public boolean translateText(int diff) {
        return true;
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        return bounds;
    }

    @Override
    public RECT getRectWithStrokes() {
        return getRect();
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterID = characterId;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if (hasFont) {
            needed.add(fontId);
        }
        if (html && hasText) {
            List<CharacterWithStyle> chs = getTextWithStyle();
            for (CharacterWithStyle ch : chs) {
                if (ch.style.font != null) {
                    needed.add(swf.getCharacterId(ch.style.font));
                }
            }
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (fontId == oldCharacterId) {
            fontId = newCharacterId;
            setModified(true);
            return true;
        }
        return false;
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
    public int getUsedParameters() {
        return 0;
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        render(TextRenderMode.BITMAP, image, null, null, transformation, colorTransform, 1);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) {
        render(TextRenderMode.SVG, null, exporter, null, new Matrix(), colorTransform, 1);
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        render(TextRenderMode.HTML5_CANVAS, null, null, result, new Matrix(), null, unitDivisor);
    }

    private void render(TextRenderMode renderMode, SerializableImage image, SVGExporter svgExporter, StringBuilder htmlCanvasBuilder, Matrix transformation, ColorTransform colorTransform, double zoom) {
        if (border) {
            // border is always black, fill color is always white?
            RGB borderColor = new RGBA(Color.black);
            RGB fillColor = new RGBA(Color.white);
            switch (renderMode) {
                case BITMAP:
                    drawBorder(swf, image, borderColor, fillColor, getRect(), getTextMatrix(), transformation, colorTransform);
                    break;
                case HTML5_CANVAS:
                    drawBorderHtmlCanvas(swf, htmlCanvasBuilder, borderColor, fillColor, getRect(), getTextMatrix(), colorTransform, zoom);
                    break;
                case SVG:
                    drawBorderSVG(swf, svgExporter, borderColor, fillColor, getRect(), getTextMatrix(), colorTransform, zoom);
                    break;
            }
        }
        if (hasText) {
            List<TEXTRECORD> allTextRecords = getTextRecords(swf);
            switch (renderMode) {
                case BITMAP:
                    staticTextToImage(swf, allTextRecords, 2, image, getTextMatrix(), transformation, colorTransform);
                    break;
                case HTML5_CANVAS:
                    staticTextToHtmlCanvas(zoom, swf, allTextRecords, 2, htmlCanvasBuilder, getBounds(), getTextMatrix(), colorTransform);
                    break;
                case SVG:
                    staticTextToSVG(swf, allTextRecords, 2, svgExporter, getBounds(), getTextMatrix(), colorTransform, zoom);
                    break;
            }
        }
    }

    public List<TEXTRECORD> getTextRecords(SWF swf) {
        DynamicTextModel textModel = new DynamicTextModel();
        List<CharacterWithStyle> txt = getTextWithStyle();
        TextStyle lastStyle = null;
        char prevChar = 0;
        boolean lastWasWhiteSpace = false;
        for (int i = 0; i < txt.size(); i++) {
            CharacterWithStyle cs = txt.get(i);
            char c = cs.character;
            if (c != '\r' && c != '\n') {
                // create new SameStyleTextRecord for all words and all different style text parts
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

                FontTag font = lastStyle.font;
                DynamicTextGlyphEntry ge = new DynamicTextGlyphEntry();
                ge.fontFace = lastStyle.fontFace;
                if (ge.fontFace == null && font != null) {
                    ge.fontFace = font.getFontNameIntag();
                }

                ge.fontStyle = (lastStyle.bold ? Font.BOLD : 0) | (lastStyle.italic ? Font.ITALIC : 0);
                ge.character = c;

                if (useOutlines && font != null) {
                    ge.glyphIndex = font.charToGlyph(c);
                } else {
                    ge.glyphIndex = -1;
                }

                String fontName = ge.fontFace != null ? ge.fontFace : FontTag.getDefaultFontName();
                int fontStyle = font == null ? ge.fontStyle : font.getFontStyle();
                ge.glyphAdvance = ge.glyphIndex == -1 ? (int) Math.round(SWF.unitDivisor * FontTag.getSystemFontAdvance(fontName, fontStyle, (int) (lastStyle.fontHeight / SWF.unitDivisor), c, lastStyle.kerning ? nextChar : null))
                        : (int) Math.round(font.getGlyphAdvance(ge.glyphIndex) / font.getDivider() * lastStyle.fontHeight / 1024);
                ge.glyphAdvance += lastStyle.letterSpacing * SWF.unitDivisor;
                if (useOutlines && lastStyle.kerning && font != null && font.hasLayout()) {
                    if (nextChar != null) {
                        ge.glyphAdvance += font.getCharKerningAdjustment(c, nextChar) / font.getDivider();
                    }
                }
                textModel.addGlyph(c, ge);
                if (Character.isWhitespace(c)) {
                    lastWasWhiteSpace = true;
                }
            } else if (multiline) {
                textModel.newParagraph();
            }
            prevChar = c;
        }

        textModel.calculateTextWidths();

        Set<Integer> noIndentLineIndices = new HashSet<>();

        List<List<SameStyleTextRecord>> lines;
        if (multiline && wordWrap) {
            lines = new ArrayList<>();
            for (Paragraph paragraph : textModel.paragraphs) {
                List<SameStyleTextRecord> line = new ArrayList<>();
                int lineLength = 0;
                for (Word word : paragraph.words) {
                    int indentVal = noIndentLineIndices.contains(lines.size()) ? 0 : indent;
                    int maxLineWidth = bounds.getWidth() - leftMargin - indentVal;
                    if (word.width > maxLineWidth) {
                        List<SameStyleTextRecord> recs = new ArrayList<>();
                        for (int i = 0; i < word.records.size(); i++) {
                            SameStyleTextRecord rec = word.records.get(i);
                            for (int g = 0; g < rec.glyphEntries.size(); g++) {
                                GlyphCharacter gc = rec.glyphEntries.get(g);
                                int ga = gc.glyphEntry.glyphAdvance;
                                indentVal = noIndentLineIndices.contains(lines.size()) ? 0 : indent;
                                maxLineWidth = bounds.getWidth() - leftMargin - indentVal;
                                if (lineLength + ga > maxLineWidth) {
                                    recs.add(rec);
                                    line.addAll(recs);
                                    lines.add(line);

                                    recs = new ArrayList<>();
                                    SameStyleTextRecord rec2 = new SameStyleTextRecord();
                                    rec2.style = rec.style.clone();
                                    rec2.glyphEntries = new ArrayList<>();
                                    int glen = rec.glyphEntries.size();
                                    for (int g2 = g; g2 < glen; g2++) {
                                        rec2.glyphEntries.add(rec.glyphEntries.remove(g));
                                    }
                                    rec2.calculateTextWidths();
                                    rec.calculateTextWidths();

                                    rec = rec2;
                                    g = 0;

                                    noIndentLineIndices.add(lines.size());
                                    line = new ArrayList<>();
                                    lineLength = 0;
                                }
                                lineLength += ga;
                            }
                            recs.add(rec);
                        }
                        if (!recs.isEmpty()) {
                            line.addAll(recs);
                        }
                    } else if (lineLength + word.width <= maxLineWidth) {
                        line.addAll(word.records);
                        lineLength += word.width;
                    } else {
                        lines.add(line);
                        noIndentLineIndices.add(lines.size());
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
        int lastHeight = 0;
        int yOffset = 0;
        boolean firstLine = true;
        for (int k = 0; k < lines.size(); k++) {
            List<SameStyleTextRecord> line = lines.get(k);
            int width = 0;
            int currentOffset = 0;
            if (line.isEmpty()) {
                currentOffset = lastHeight;
            } else {
                for (SameStyleTextRecord tr : line) {
                    width += tr.width;
                    int lineHeight = (useOutlines && tr.style.font != null /*Font missing*/) && tr.style.font.hasLayout() ? (int) Math.round(tr.style.fontHeight * tr.style.font.getAscent() / tr.style.font.getDivider() / 1024.0) + tr.style.fontLeading
                            : tr.style.fontHeight + tr.style.fontLeading;
                    if (useOutlines && tr.style.font != null && !firstLine && tr.style.font.hasLayout()) {
                        lineHeight += (int) Math.round(tr.style.fontHeight * tr.style.font.getDescent() / tr.style.font.getDivider() / 1024.0);
                    }
                    //TODO: maybe get ascent/descent from system font when not haslayout
                    lastHeight = lineHeight;
                    if (lineHeight > currentOffset) {
                        currentOffset = lineHeight;
                    }
                }
            }
            firstLine = false;
            yOffset += currentOffset;
            int alignOffset = 0;

            int currentIndent = 0;
            if (!noIndentLineIndices.contains(k)) {
                currentIndent = indent;
            }

            switch (align) {
                case ALIGN_LEFT:
                    alignOffset = 0;
                    break;
                case ALIGN_RIGHT:
                    alignOffset = bounds.getWidth() - width - leftMargin - currentIndent;
                    break;
                case ALIGN_CENTER:
                    alignOffset = (bounds.getWidth() - width - leftMargin - currentIndent) / 2;
                    break;
                case ALIGN_JUSTIFY:
                    // todo;
                    break;
            }
            for (SameStyleTextRecord tr : line) {
                tr.xOffset = alignOffset;
                alignOffset += tr.width;
            }
            for (SameStyleTextRecord tr : line) {
                tr.xOffset += tr.style.leftMargin;
                tr.xOffset += currentIndent;
            }
            for (SameStyleTextRecord tr : line) {
                AdvancedTextRecord tr2 = new AdvancedTextRecord();
                int fid = fontId;
                if (fontClass != null) {
                    tr2.fontClass = fontClass;
                }
                if (tr.style.font != null) {
                    fid = swf.getCharacterId(tr.style.font);
                }

                tr2.styleFlagsHasFont = fid != 0;
                tr2.fontId = fid;
                tr2.textHeight = tr.style.fontHeight;
                if (tr.style.textColor != null) {
                    tr2.styleFlagsHasColor = true;
                    tr2.textColorA = tr.style.textColor;
                }
                // always add xOffset, because no xOffset and 0 xOffset is different in text rendering
                tr2.styleFlagsHasXOffset = true;
                tr2.xOffset = tr.xOffset;
                if (yOffset != 0) {
                    tr2.styleFlagsHasYOffset = true;
                    tr2.yOffset = yOffset;
                }
                tr2.glyphEntries = new ArrayList<>(tr.glyphEntries.size());
                for (GlyphCharacter ge : tr.glyphEntries) {
                    tr2.glyphEntries.add(ge.glyphEntry);
                }
                allTextRecords.add(tr2);
            }
        }
        return allTextRecords;
    }

    @Override
    public ExportRectangle calculateTextBounds() {
        return null;
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    @Override
    public boolean isSingleFrame() {
        return true;
    }
}
