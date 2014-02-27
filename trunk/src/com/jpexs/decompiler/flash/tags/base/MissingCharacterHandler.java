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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.Font;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MissingCharacterHandler {

    public boolean handle(FontTag font, List<Tag> tags, char character) {
        String fontName = font.getFontName();
        if (!FontTag.fontNames.contains(fontName)) {
            return false;
        }
        Font f = new Font(fontName, font.getFontStyle(), 18);
        if (!f.canDisplay(character)) {
            return false;
        }
        font.addCharacter(tags, character, fontName);
        return true;
    }
}
