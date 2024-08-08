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
package com.jpexs.decompiler.flash.tags.dynamictext;

import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import java.util.ArrayList;
import java.util.List;

/**
 * Word.
 *
 * @author JPEXS
 */
public class Word {

    /**
     * Records
     */
    public List<SameStyleTextRecord> records = new ArrayList<>();

    /**
     * Record
     */
    private SameStyleTextRecord record;

    /**
     * Model
     */
    private final DynamicTextModel model;

    /**
     * Width
     */
    public int width;

    /**
     * Constructor.
     *
     * @param model Model
     */
    public Word(DynamicTextModel model) {
        this.model = model;
    }

    /**
     * Adds glyph.
     * @param character Character
     * @param glyphEntry Glyph entry
     */
    public void addGlyph(char character, GLYPHENTRY glyphEntry) {

        if (record == null) {
            record = new SameStyleTextRecord();
            record.style = model.style;
            records.add(record);
        }
        record.glyphEntries.add(new GlyphCharacter(character, glyphEntry));
    }

    /**
     * New record.
     */
    public void newRecord() {

        record = null;
    }

    /**
     * Calculates text widths.
     * @return Width
     */
    public int calculateTextWidths() {

        int width = 0;
        for (SameStyleTextRecord r : records) {
            width += r.calculateTextWidths();
        }
        this.width = width;
        return width;
    }
}
