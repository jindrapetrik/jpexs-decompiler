/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.tags.dynamictext;

import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author JPEXS
 */
public class Paragraph {
    
    public List<Word> words = new ArrayList<>();

    private Word word;
    private DynamicTextModel model;
    public int width;

    public Paragraph(DynamicTextModel model) {

        this.model = model;
    }

    public void addGlyph(char character, GLYPHENTRY glyphEntry) {
        
        if (word == null) {
            word = new Word(model);
            words.add(word);
        }
        word.addGlyph(character, glyphEntry);
    }

    public void newWord() {

        word = null;
    }

    public void newRecord() {
        
        if (word != null) {
            word.newRecord();
        }
    }

    public int calculateTexWidths() {
        
        int width = 0;
        for (Word w : words) {
            width += w.calculateTexWidths();
        }
        this.width = width;
        return width;
    }
}
