/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class HighlightedText implements Serializable {

    public static HighlightedText EMPTY = new HighlightedText();

    public String text;

    private final List<Highlighting> traitHighlights;

    private final List<Highlighting> classHighlights;

    private final List<Highlighting> methodHighlights;

    private final List<Highlighting> instructionHighlights;

    private final List<Highlighting> specialHighlights;

    public List<Highlighting> getTraitHighlights() {
        return traitHighlights;
    }

    public List<Highlighting> getMethodHighlights() {
        return methodHighlights;
    }

    public List<Highlighting> getClassHighlights() {
        return classHighlights;
    }

    public List<Highlighting> getInstructionHighlights() {
        return instructionHighlights;
    }

    public List<Highlighting> getSpecialHighlights() {
        return specialHighlights;
    }

    public HighlightedText(HighlightedTextWriter writer) {
        this.text = writer.toString();
        this.traitHighlights = writer.traitHilights;
        this.classHighlights = writer.classHilights;
        this.methodHighlights = writer.methodHilights;
        this.instructionHighlights = writer.instructionHilights;
        this.specialHighlights = writer.specialHilights;
    }

    private HighlightedText() {
        this("");
    }

    public HighlightedText(String text) {
        this.text = text;
        this.traitHighlights = new ArrayList<>();
        this.classHighlights = new ArrayList<>();
        this.methodHighlights = new ArrayList<>();
        this.instructionHighlights = new ArrayList<>();
        this.specialHighlights = new ArrayList<>();
    }
}
