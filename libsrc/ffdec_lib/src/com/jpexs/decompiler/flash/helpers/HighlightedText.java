/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightingList;
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

    private final HighlightingList traitHighlights;

    private final HighlightingList classHighlights;

    private final HighlightingList methodHighlights;

    private final HighlightingList instructionHighlights;

    private final HighlightingList specialHighlights;

    public HighlightingList getTraitHighlights() {
        return traitHighlights;
    }

    public HighlightingList getMethodHighlights() {
        return methodHighlights;
    }

    public HighlightingList getClassHighlights() {
        return classHighlights;
    }

    public HighlightingList getInstructionHighlights() {
        return instructionHighlights;
    }

    public HighlightingList getSpecialHighlights() {
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
        this.traitHighlights = new HighlightingList();
        this.classHighlights = new HighlightingList();
        this.methodHighlights = new HighlightingList();
        this.instructionHighlights = new HighlightingList();
        this.specialHighlights = new HighlightingList();
    }
}
