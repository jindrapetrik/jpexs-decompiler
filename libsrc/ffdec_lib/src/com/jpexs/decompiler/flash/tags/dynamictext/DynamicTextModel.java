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
package com.jpexs.decompiler.flash.tags.dynamictext;

import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DynamicTextModel {

    public List<Paragraph> paragraphs = new ArrayList<>();

    private Paragraph paragraph;

    public TextStyle style;

    public int width;

    public DynamicTextModel() {

    }

    public void addGlyph(char character, GLYPHENTRY glyphEntry) {

        if (paragraph == null) {
            paragraph = new Paragraph(this);
            paragraphs.add(paragraph);
        }
        paragraph.addGlyph(character, glyphEntry);
    }

    public void newParagraph() {

        if (paragraph == null) {
            // add empty paragraph
            paragraph = new Paragraph(this);
            paragraphs.add(paragraph);
        }
        paragraph = null;
    }

    public void newWord() {

        if (paragraph != null) {
            paragraph.newWord();
        }
    }

    public void newRecord() {

        if (paragraph != null) {
            paragraph.newRecord();
        }
    }

    public int calculateTextWidths() {

        int width = 0;
        for (Paragraph p : paragraphs) {
            width += p.calculateTextWidths();
        }
        this.width = width;
        return width;
    }
}
