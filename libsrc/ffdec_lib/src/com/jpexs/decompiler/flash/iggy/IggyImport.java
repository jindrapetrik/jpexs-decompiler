/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Import from an already declared Iggy library.
 */
public final class IggyImport {

    private final String library;
    private final List<IggySymbolClass> symbols;

    public IggyImport(String library, List<IggySymbolClass> symbols) {
        this.library = library;
        this.symbols = Collections.unmodifiableList(new ArrayList<>(symbols));
    }

    public String getLibrary() {
        return library;
    }

    public List<IggySymbolClass> getSymbols() {
        return symbols;
    }
}
