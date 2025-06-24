/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.docs;

import com.jpexs.helpers.Cache;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class for documentation.
 */
public class AbstractDocs {

    /**
     * Constructs new AbstractDocs
     */
    public AbstractDocs() {
    }

    /**
     * Cache for documentation.
     */
    protected static Cache<String, String> docsCache = Cache.getInstance(false, true, "abstractDocsCache", false);

    /**
     * Gets HTML header.
     * @return HTML header
     */
    protected static String htmlFooter() {
        StringBuilder sb = new StringBuilder();

        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Gets style.
     * @return Style
     */
    public static String getStyle() {
        String cached = docsCache.get("__style");
        if (cached != null) {
            return cached;
        }
        String style = "";
        InputStream is = As3PCodeDocs.class.getResourceAsStream("/com/jpexs/decompiler/flash/docs/docs.css");
        if (is == null) {
            Logger.getLogger(As3PCodeDocs.class.getName()).log(Level.SEVERE, "docs.css needed for documentation not found");
        } else {
            style = new String(Helper.readStream(is), Utf8Helper.charset);
        }

        docsCache.put("__style", style);
        return style;
    }

    /**
     * Gets meta property.
     * @param name Name
     * @param content Content
     * @return Meta property
     */
    protected static String metaProp(String name, String content) {
        return "\t\t<meta property=\"" + name + "\" content=\"" + content + "\">" + As3PCodeOtherDocs.NEWLINE;
    }

    /**
     * Gets meta property.
     * @param name Name
     * @param content Content
     * @return Meta property
     */
    protected static String meta(String name, String content) {
        return "\t\t<meta name=\"" + name + "\" content=\"" + content + "\">" + As3PCodeOtherDocs.NEWLINE;
    }

    /**
     * Gets meta property.
     * @param name Name
     * @param content Content
     * @return Meta property
     */
    protected static String meta(String name, Date content) {
        return "\t\t<meta name=\"" + name + "\" content=\"" + getISO8601StringForDate(content) + "\">" + As3PCodeOtherDocs.NEWLINE;
    }

    /**
     * Gets ISO8601 string for date.
     * @param date Date
     * @return ISO8601 string for date
     */
    protected static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    /**
     * Hilights argument.
     * @param docs Docs
     * @param argumentIndex Argument index
     * @return Hilighted argument
     */
    protected static String hilightArgument(String docs, int argumentIndex) {
        if (argumentIndex < 0) {
            return docs;
        }
        String opHeader = "<span class=\"instruction-operands\">";
        int opIndex = docs.indexOf(opHeader) + opHeader.length();
        int opEndIndex = docs.indexOf("</span>", opIndex);
        String operandDocs = docs.substring(opIndex, opEndIndex).trim();
        if (operandDocs.isEmpty()) {
            return docs;
        }

        DocsOperandLexer lexer = new DocsOperandLexer(new StringReader(operandDocs));
        try {
            ParsedSymbol symb;

            int pos = 0;
            int endPos = 0;
            int startPos = 0;
            while (true) {
                startPos = lexer.yychar();
                symb = lexer.lex();
                if (symb.type == ParsedSymbol.TYPE_DOTS) {
                    endPos = lexer.yychar() + 3;
                    break;
                }
                if (symb.type == ParsedSymbol.TYPE_EOF) {
                    endPos = lexer.yychar();
                    break;
                }
                if (symb.type == ParsedSymbol.TYPE_BRACKET_OPEN) {
                    while (symb.type != ParsedSymbol.TYPE_BRACKET_CLOSE && symb.type != ParsedSymbol.TYPE_EOF) {
                        symb = lexer.lex();
                    }
                    endPos = lexer.yychar() + 1;
                    break;
                }
                if (symb.type == ParsedSymbol.TYPE_IDENTIFIER) {
                    symb = lexer.lex();
                    endPos = lexer.yychar();
                    if (symb.type == ParsedSymbol.TYPE_COLON) {
                        do {
                            symb = lexer.lex();
                            if (symb.type != ParsedSymbol.TYPE_IDENTIFIER && symb.type != ParsedSymbol.TYPE_STAR) {
                                throw new IOException("type identifier expected");
                            }
                            symb = lexer.lex();
                            endPos = lexer.yychar();
                        } while (symb.type == ParsedSymbol.TYPE_PIPE);
                    }

                    if (pos == argumentIndex) {
                        break;
                    }

                    if (symb.type == ParsedSymbol.TYPE_COMMA) {
                        pos++;
                    } else {
                        break;
                    }
                }
            }
            String hilightedOperandDocs = operandDocs.substring(0, startPos)
                    + "<strong class=\"selected-operand\">" + operandDocs.substring(startPos, endPos) + "</strong>"
                    + operandDocs.substring(endPos);
            docs = docs.substring(0, opIndex) + hilightedOperandDocs + docs.substring(opEndIndex);
        } catch (IOException ex) {
            //ignore
        }
        return docs;
    }

}
