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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc;

import com.jpexs.decompiler.flash.helpers.HighlightedText;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CachedDecompilation implements Serializable {

    public String text;

    public List<Highlighting> traitHilights;

    public List<Highlighting> classHilights;

    public List<Highlighting> methodHilights;

    public List<Highlighting> instructionHilights;

    public List<Highlighting> specialHilights;

    public List<Highlighting> getInstructionHighlights() {
        return instructionHilights;
    }

    public List<Highlighting> getTraitHighlights() {
        return traitHilights;
    }

    public List<Highlighting> getMethodHighlights() {
        return methodHilights;
    }

    public List<Highlighting> getClassHighlights() {
        return classHilights;
    }

    public List<Highlighting> getSpecialHighligths() {
        return specialHilights;
    }

    public CachedDecompilation(HighlightedText HighlightedText) {
        this.text = HighlightedText.text;
        this.traitHilights = HighlightedText.traitHilights;
        this.classHilights = HighlightedText.classHilights;
        this.methodHilights = HighlightedText.methodHilights;
        this.instructionHilights = HighlightedText.instructionHilights;
        this.specialHilights = HighlightedText.specialHilights;
    }
}
