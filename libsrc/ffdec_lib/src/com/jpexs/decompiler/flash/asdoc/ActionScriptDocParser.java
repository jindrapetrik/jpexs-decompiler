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
package com.jpexs.decompiler.flash.asdoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionScriptDocParser {

    public List<AsDocComment> parse(String str) {
        List<AsDocComment> comments = new ArrayList<>();
        ActionScriptDocLexer lexer = new ActionScriptDocLexer(str);
        try {
            ParsedSymbol s = lexer.yylex();
            String startText = null;
            List<AsDocTag> tags = new ArrayList<>();
            while (s.type != SymbolType.EOF) {
                if (s.type == SymbolType.DOC_BEGIN) {
                    startText = s.text;
                }
                if (s.type == SymbolType.DOC_MIDDLE || (s.type == SymbolType.DOC_END && s.tag != null)) {
                    tags.add(new AsDocTag(s.tag, s.text));
                }
                if (s.type == SymbolType.DOC_END) {
                    comments.add(new AsDocComment(startText, tags));
                    startText = null;
                    tags = new ArrayList<>();
                }
                s = lexer.yylex();
            }
        } catch (IOException | AsDocParseException ex) {
            //ignored
        }
        return comments;
    }
}
