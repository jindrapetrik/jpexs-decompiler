/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.types.GLYPHENTRY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.TEXTRECORD;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Type-5 static text: linked, expanded text records with glyph/advance pairs.
 */
public final class IggyStaticTextConvertor {

    private IggyStaticTextConvertor() {
    }

    private static void fonts(Iterable<Tag> tags, Set<Integer> result) {
        for (Tag tag : tags) {
            if (tag instanceof FontTag) {
                result.add(((FontTag) tag).getCharacterId());
            }
            if (tag instanceof ImportTag) {
                result.addAll(((ImportTag) tag).getAssets().keySet());
            }
            if (tag instanceof DefineSpriteTag) {
                fonts(((DefineSpriteTag) tag).getTags(), result);
            }
        }
    }

    static void validateReferences(SWF swf, StaticTextTag text) throws IOException {
        Set<Integer> fonts = new HashSet<>();
        fonts(swf.getTags(), fonts);
        int id = -1;
        for (TEXTRECORD r : text.textRecords) {
            if (r.styleFlagsHasFont) {
                id = r.fontId;
            }
            if ((r.styleFlagsHasFont || !r.glyphEntries.isEmpty()) && !fonts.contains(id)) {
                throw new IOException("Missing Iggy static text font " + id);
            }
        }
    }

    public static DefineText2Tag toSwf(SWF swf, IggySwf movie, IggyTimeline source, long at, int id) throws IOException {
        boolean wide = source.is64();
        source.bytes(at, wide ? 80 : 60);
        DefineText2Tag text = new DefineText2Tag(swf);
        text.characterID = id;
        int rect = wide ? 32 : 16;
        text.textBounds = new RECT(Math.round(IggyShapeStyles.f32(source, at + rect) * 20),
                Math.round(IggyShapeStyles.f32(source, at + rect + 8) * 20),
                Math.round(IggyShapeStyles.f32(source, at + rect + 4) * 20),
                Math.round(IggyShapeStyles.f32(source, at + rect + 12) * 20));
        text.textMatrix = IggyShapeStyles.matrix(source, at + (wide ? 48 : 32), 1);
        text.textRecords = new ArrayList<>();
        long record = source.reference(at + (wide ? 72 : 56));
        Set<Long> visited = new HashSet<>();
        while (record != 0) {
            if (!visited.add(record)) {
                throw new IOException("Cycle in Iggy static text records");
            }
            source.bytes(record, wide ? 32 : 20);
            TEXTRECORD r = new TEXTRECORD();
            r.styleFlagsHasFont = true;
            r.styleFlagsHasColor = true;
            r.styleFlagsHasXOffset = true;
            r.styleFlagsHasYOffset = true;
            long font = source.reference(record + 8);
            r.fontId = movie.getDictionaryId(record + 8, font);
            if (r.fontId < 0) {
                r.styleFlagsHasFont = false;
            }
            r.textHeight = IggyShapeStyles.u16(source, record);
            r.xOffset = source.bytes(record + 2, 2).getShort();
            r.yOffset = source.bytes(record + 4, 2).getShort();
            r.textColorA = IggyShapeStyles.color(source, record + (wide ? 16 : 12));
            int count = IggyShapeStyles.u8(source, record + 6);
            r.glyphEntries = new ArrayList<>();
            long glyphs = record + (wide ? 32 : 20);
            source.bytes(glyphs, 4 * count);
            for (int i = 0; i < count; i++) {
                GLYPHENTRY glyph = new GLYPHENTRY();
                glyph.glyphIndex = IggyShapeStyles.u16(source, glyphs + 4L * i);
                glyph.glyphAdvance = source.bytes(glyphs + 4L * i + 2, 2).getShort();
                r.glyphEntries.add(glyph);
            }
            if (!r.styleFlagsHasFont && count > 0) {
                throw new IOException("Missing Iggy static text font");
            }
            text.textRecords.add(r);
            record = source.reference(record + (wide ? 24 : 16));
        }
        text.setModified(true);
        return text;
    }

    public static IggyObjectData fromSwf(boolean wide, StaticTextTag text) throws IOException {
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int size = wide ? 80 : 60;
        long at = IggyAssetConvertor.header(b, text.characterID, 5, size);
        RECT rect = text.textBounds;
        int off = wide ? 32 : 16;
        b.f32(at + off, rect.Xmin / 20.0);
        b.f32(at + off + 4, rect.Ymin / 20.0);
        b.f32(at + off + 8, rect.Xmax / 20.0);
        b.f32(at + off + 12, rect.Ymax / 20.0);
        IggyShapeStyles.matrix(b, at + (wide ? 48 : 32), text.textMatrix == null ? new MATRIX() : text.textMatrix, 1);
        long link = at + (wide ? 72 : 56);
        int font = -1;
        int height = 0;
        int x = 0;
        int y = 0;
        RGBA color = new RGBA(255, 255, 255, 255);
        for (TEXTRECORD r : text.textRecords) {
            if (r.styleFlagsHasFont) {
                font = r.fontId;
                height = r.textHeight;
            }
            if (r.styleFlagsHasXOffset) {
                x = r.xOffset;
            }
            if (r.styleFlagsHasYOffset) {
                y = r.yOffset;
            }
            if (r.styleFlagsHasColor) {
                RGB c = text.getTextNum() == 1 ? r.textColor : r.textColorA;
                color = new RGBA(c.red, c.green, c.blue, c instanceof RGBA ? ((RGBA) c).alpha : 255);
            }
            int count = r.glyphEntries.size();
            if (count > 255 || (font != -1 && (font <= 0 || font > 65535)) || (count != 0 && font == -1) || height < 0 || height > 65535
                    || x < Short.MIN_VALUE || x > Short.MAX_VALUE || y < Short.MIN_VALUE || y > Short.MAX_VALUE) {
                throw new IOException("Invalid Iggy static text record");
            }
            long record = b.allocate((wide ? 32 : 20) + 4 * count);
            b.pointer(link, record);
            link = record + (wide ? 24 : 16);
            b.u16(record, height);
            b.u16(record + 2, x);
            b.u16(record + 4, y);
            b.u8(record + 6, count);
            b.pointer(record + 8, -1);
            if (font >= 0) {
                b.characters.put(record + 8, font);
            }
            IggyShapeStyles.color(b, record + (wide ? 16 : 12), color);
            long glyphs = record + (wide ? 32 : 20);
            for (int i = 0; i < count; i++) {
                GLYPHENTRY glyph = r.glyphEntries.get(i);
                if (glyph.glyphIndex < 0 || glyph.glyphIndex > 65535 || glyph.glyphAdvance < Short.MIN_VALUE || glyph.glyphAdvance > Short.MAX_VALUE) {
                    throw new IOException("Iggy static text glyph exceeds 16-bit range");
                }
                b.u16(glyphs + 4L * i, glyph.glyphIndex);
                b.u16(glyphs + 4L * i + 2, glyph.glyphAdvance);
                x = (short) (x + glyph.glyphAdvance);
            }
        }
        b.pointer(link, -1);
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, 5);
    }
}
