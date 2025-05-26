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
package com.jpexs.decompiler.flash.helpers;

import com.jpexs.decompiler.flash.helpers.hilight.HighlightingList;
import java.io.Serializable;

/**
 * Provides methods for highlighting positions of instructions in the text.
 *
 * @author JPEXS
 */
public class HighlightedText implements Serializable {

    /**
     * Empty highlighted text
     */
    public static HighlightedText EMPTY = new HighlightedText();

    /**
     * Text
     */
    public String text;

    private final HighlightingList traitHighlights;

    private final HighlightingList classHighlights;

    private final HighlightingList methodHighlights;

    private final HighlightingList instructionHighlights;

    private final HighlightingList specialHighlights;

    /**
     * Gets trait highlights
     * @return Trait highlights
     */
    public HighlightingList getTraitHighlights() {
        return traitHighlights;
    }

    /**
     * Gets method highlights
     * @return Method highlights
     */
    public HighlightingList getMethodHighlights() {
        return methodHighlights;
    }

    /**
     * Gets class highlights
     * @return Class highlights
     */
    public HighlightingList getClassHighlights() {
        return classHighlights;
    }

    /**
     * Gets instruction highlights
     * @return Instruction highlights
     */
    public HighlightingList getInstructionHighlights() {
        return instructionHighlights;
    }

    /**
     * Gets special highlights
     * @return Special highlights
     */
    public HighlightingList getSpecialHighlights() {
        return specialHighlights;
    }

    /**
     * Constructor.
     * @param writer Writer
     */
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

    /**
     * Constructor.
     * @param text Text
     */
    public HighlightedText(String text) {
        this.text = text;
        this.traitHighlights = new HighlightingList();
        this.classHighlights = new HighlightingList();
        this.methodHighlights = new HighlightingList();
        this.instructionHighlights = new HighlightingList();
        this.specialHighlights = new HighlightingList();
    }
}
