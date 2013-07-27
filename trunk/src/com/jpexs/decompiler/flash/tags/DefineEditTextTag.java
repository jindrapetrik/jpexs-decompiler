/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MissingCharacterHandler;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.text.ParseException;
import com.jpexs.decompiler.flash.tags.text.ParsedSymbol;
import com.jpexs.decompiler.flash.tags.text.TextLexer;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.geom.GeneralPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class DefineEditTextTag extends CharacterTag implements BoundedTag, TextTag {

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
    public int fontId;
    public String fontClass;
    public int fontHeight;
    public RGBA textColor;
    public int maxLength;
    public int align;
    public int leftMargin;
    public int rightMargin;
    public int indent;
    public int leading;
    public String variableName;
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
                outp = outp + inp.charAt(i);
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
    public String getText(List<Tag> tags) {
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
    public String getFormattedText(List<Tag> tags) {
        String ret = "";
        ret += "[";
        String[] alignValues = {"left", "right", "center", "justify"};
        ret += "\r\nxmin " + bounds.Xmin + "\r\nymin " + bounds.Ymin + "\r\nxmax " + bounds.Xmax + "\r\nymax " + bounds.Ymax + "\r\n";
        ret += (wordWrap ? "wordwrap 1\r\n" : "") + (multiline ? "multiline 1\r\n" : "")
                + (password ? "password 1\r\n" : "") + (readOnly ? "readonly 1\r\n" : "")
                + (autoSize ? "autosize 1\r\n" : "") + (noSelect ? "noselect 1\r\n" : "")
                + (border ? "border 1\r\n" : "") + (wasStatic ? "wasstatic 1\r\n" : "")
                + (html ? "html 1\r\n" : "") + (useOutlines ? "useoutlines 1\r\n" : "")
                + (hasFont ? "font " + fontId + "\r\n" : "") + (hasTextColor ? "color " + textColor.toHexARGB() + "\r\n" : "")
                + (hasFontClass ? "fontclass " + fontClass + "\r\n" : "") + (hasMaxLength ? "maxlength " + maxLength + "\r\n" : "")
                + "align " + alignValues[align] + "\r\n"
                + (hasLayout ? "leftmargin " + leftMargin + "\r\nrightmargin " + rightMargin + "\r\nindent " + indent + "\r\nleading " + leading + "\r\n" : "")
                + (!variableName.equals("") ? "variablename " + variableName + "\r\n" : "");
        ret = ret + "]";
        if (hasText) {
            ret += initialText.replace("\\", "\\\\").replace("[", "\\[").replace("]", "\\]");
        }
        return ret;
    }

    @Override
    public boolean setFormattedText(MissingCharacterHandler missingCharHandler, List<Tag> tags, String text, String fontName) throws ParseException {
        try {
            TextLexer lexer = new TextLexer(new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF-8")), "UTF-8"));
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
                        if (paramName.equals("xmin")) {
                            try {
                                bounds.Xmin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid xmin value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("ymin")) {
                            try {
                                bounds.Ymin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid ymin value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("xmax")) {
                            try {
                                bounds.Xmax = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid xmax value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("ymax")) {
                            try {
                                bounds.Ymax = Integer.parseInt(paramValue);
                            } catch (NumberFormatException nfe) {
                                throw new ParseException("Invalid ymax value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("wordwrap")) {
                            if (paramValue.equals("1")) {
                                wordWrap = true;
                            }
                        } else if (paramName.equals("multiline")) {
                            if (paramValue.equals("1")) {
                                multiline = true;
                            }
                        } else if (paramName.equals("password")) {
                            if (paramValue.equals("1")) {
                                password = true;
                            }
                        } else if (paramName.equals("readonly")) {
                            if (paramValue.equals("1")) {
                                readOnly = true;
                            }
                        } else if (paramName.equals("autosize")) {
                            if (paramValue.equals("1")) {
                                autoSize = true;
                            }
                        } else if (paramName.equals("noselect")) {
                            if (paramValue.equals("1")) {
                                noSelect = true;
                            }
                        } else if (paramName.equals("border")) {
                            if (paramValue.equals("1")) {
                                border = true;
                            }
                        } else if (paramName.equals("wasstatic")) {
                            if (paramValue.equals("1")) {
                                wasStatic = true;
                            }
                        } else if (paramName.equals("html")) {
                            if (paramValue.equals("1")) {
                                html = true;
                            }
                        } else if (paramName.equals("useoutlines")) {
                            if (paramValue.equals("1")) {
                                useOutlines = true;
                            }
                        } else if (paramName.equals("font")) {
                            try {
                                fontId = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid font value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("fontclass")) {
                            fontClass = paramValue;
                        } else if (paramName.equals("height")) {
                            try {
                                fontHeight = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid height value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("color")) {
                            Matcher m = Pattern.compile("#([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])").matcher(paramValue);
                            if (m.matches()) {
                                textColor = new RGBA(Integer.parseInt(m.group(2), 16), Integer.parseInt(m.group(3), 16), Integer.parseInt(m.group(4), 16), Integer.parseInt(m.group(1), 16));
                            } else {
                                throw new ParseException("Invalid color. Valid format is #aarrggbb.", lexer.yyline());
                            }
                        } else if (paramName.equals("maxlength")) {
                            try {
                                maxLength = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid maxLength value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("align")) {
                            if (paramValue.equals("left")) {
                                align = 0;
                            } else if (paramValue.equals("right")) {
                                align = 1;
                            } else if (paramValue.equals("center")) {
                                align = 2;
                            } else if (paramValue.equals("justify")) {
                                align = 3;
                            } else {
                                throw new ParseException("Invalid align value. Expected one of: left,right,center or justify.", lexer.yyline());
                            }

                        } else if (paramName.equals("leftmargin")) {
                            try {
                                leftMargin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid leftmargin value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("rightmargin")) {
                            try {
                                rightMargin = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid rightmargin value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("indent")) {
                            try {
                                indent = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid indent value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("leading")) {
                            try {
                                leading = Integer.parseInt(paramValue);
                            } catch (NumberFormatException ne) {
                                throw new ParseException("Invalid leading value. Number expected.", lexer.yyline());
                            }
                        } else if (paramName.equals("variablename")) {
                            variableName = paramValue;
                        } else {
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
    public RECT getRect(HashMap<Integer, CharacterTag> allCharacters, Stack<Integer> visited) {
        return bounds;
    }

    @Override
    public int getCharacterId() {
        return characterID;
    }

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        SWFOutputStream sos = new SWFOutputStream(os, version);
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
    public DefineEditTextTag(SWF swf, byte data[], int version, long pos) throws IOException {
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

    //@Override
    public List<GeneralPath> getPaths(List<Tag> tags) {
        return null; //FIXME
    }
}
