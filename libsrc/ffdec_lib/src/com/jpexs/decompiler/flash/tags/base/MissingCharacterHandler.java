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
package com.jpexs.decompiler.flash.tags.base;

import java.awt.Font;
import java.util.Map;

/**
 * Handler for missing characters in fonts.
 *
 * @author JPEXS
 */
public class MissingCharacterHandler {

    /**
     * Checks if missing characters should be ignored.
     * @return True if missing characters should be ignored, false otherwise.
     */
    public boolean getIgnoreMissingCharacters() {
        return false;
    }

    /**
     * Handles missing character.
     * @param textTag Text tag
     * @param font Font tag
     * @param character Missing character
     * @return True if character was handled, false otherwise.
     */
    public boolean handle(TextTag textTag, FontTag font, char character) {
        String fontName = font.getFontNameIntag();
        if (!FontTag.getInstalledFontsByFamily().containsKey(fontName)) {
            return false;
        }
        Map<String, Font> faces = FontTag.getInstalledFontsByFamily().get(fontName);

        Font f = null;
        for (String face : faces.keySet()) {
            Font ff = faces.get(face);
            if (ff.isBold() == font.isBold() && ff.isItalic() == font.isItalic()) {
                f = ff;
                break;
            }
        }
        if (f == null) {
            f = faces.get(faces.keySet().iterator().next());
        }
        if (!f.canDisplay(character)) {
            return false;
        }
        return font.addCharacter(character, f);
    }
}
