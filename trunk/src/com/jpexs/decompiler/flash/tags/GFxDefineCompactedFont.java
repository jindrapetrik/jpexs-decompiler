/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.gfx.FontType;
import com.jpexs.decompiler.flash.types.gfx.GFxInputStream;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 *
 * @author JPEXS
 */
public class GFxDefineCompactedFont extends CharacterTag implements DrawableTag {

    public static final int ID = 1005;
    public int fontId;
    public List<FontType> fonts;

    /**
     * Gets data bytes
     *
     * @param version SWF version
     * @return Bytes of data
     */
    @Override
    public byte[] getData(int version) {
        return super.getData(version);
        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
         OutputStream os = baos;
         SWFOutputStream sos = new SWFOutputStream(os, version);        
         try {
         //sos.write
         } catch (IOException e) {
         }
         return baos.toByteArray();*/
    }

    /**
     * Constructor
     *
     * @param swf
     * @param data Data bytes
     * @param version SWF version
     * @param pos
     * @throws IOException
     */
    public GFxDefineCompactedFont(SWF swf, byte[] data, int version, long pos) throws IOException {
        super(swf, ID, "DefineCompactedFont", data, pos);

        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), version);
        fontId = sis.readUI16();
        fonts = new ArrayList<>();

        while (sis.available() > 0) {
            fonts.add(new FontType(new GFxInputStream(sis)));
        }
    }

    @Override
    public BufferedImage toImage(int frame, List<Tag> tags, RECT displayRect, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        List<SHAPE> shapes = new ArrayList<>();
        for (FontType f : fonts) {
            shapes.addAll(f.getGlyphShapes());
        }
        return SHAPERECORD.shapeListToImage(shapes, 500, 500, Color.black);
    }

    @Override
    public Point getImagePos(int frame, HashMap<Integer, CharacterTag> characters, Stack<Integer> visited) {
        return new Point(0, 0);
    }

    @Override
    public int getNumFrames() {
        return 1;
    }

    public String getFontName(List<Tag> tags) {
        String ret = "";
        for (int i = 0; i < fonts.size(); i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += fonts.get(i).fontName;
        }
        return ret;
    }

    @Override
    public String getName(List<Tag> tags) {
        String nameAppend = "";
        String fontName = getFontName(tags);
        if (fontName != null) {
            nameAppend = ": " + fontName;
        }
        return name + " (" + getCharacterId() + nameAppend + ")";
    }

    @Override
    public int getCharacterId() {
        return fontId;
    }
}
