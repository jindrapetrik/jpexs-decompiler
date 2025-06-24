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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexical analyzer bufferer. It stores all parsed symbols and can push them
 * back.
 *
 * @author JPEXS
 */
public class LexBufferer implements LexListener {

    /**
     * List of stored symbols
     */
    private final List<ParsedSymbol> items = new ArrayList<>();

    @Override
    public void onLex(ParsedSymbol s) {
        items.add(s);
    }

    @Override
    public void onPushBack(ParsedSymbol s) {
        if (items.get(items.size() - 1) == s) {
            items.remove(items.size() - 1);
        }
    }

    /**
     * Pushes all stored symbols back to lexer
     * @param lexer Lexer
     */
    public void pushAllBack(ActionScriptLexer lexer) {
        for (int i = items.size() - 1; i >= 0; i--) {
            lexer.pushback(items.get(i));
        }
        items.clear();
    }
}
