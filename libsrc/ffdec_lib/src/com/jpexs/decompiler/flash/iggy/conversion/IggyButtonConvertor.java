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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy.conversion;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Type-7 SimpleButton definitions, shared by the 32 legacy, 32 and 64 layouts.
 */
public final class IggyButtonConvertor {

    private static final int[] BLEND = {1, 1, 2, 3, 4, 5, 6, 9, 7, 8, 10, 11, 12, 13, 14};

    private IggyButtonConvertor() {
    }

    public static DefineButton2Tag toSwf(SWF swf, IggyTimeline source, long at, int id) throws IOException {
        boolean wide = source.is64();
        ByteBuffer header = source.bytes(at, wide ? 88 : 60);
        int count = header.getShort(wide ? 68 : 48);
        if (count < 0) {
            throw new IOException("Invalid Iggy button record count");
        }
        DefineButton2Tag button = new DefineButton2Tag(swf);
        button.buttonId = id;
        button.trackAsMenu = header.get(wide ? 70 : 50) != 0;
        CXFORMWITHALPHA global = header.get(wide ? 71 : 51) == 0 ? null
                : readColor(source, at + (wide ? 40 : 20));
        long records = source.reference(at + (wide ? 72 : 52));
        if (count != 0 && records == 0) {
            throw new IOException("Missing Iggy button records");
        }
        int stride = wide ? 72 : 64;
        if (count != 0) {
            source.bytes(records, count * stride);
        }
        for (int i = 0; i < count; i++) {
            long recordAt = records + (long) stride * i;
            ByteBuffer data = source.bytes(recordAt, stride);
            BUTTONRECORD record = new BUTTONRECORD(swf, button);
            int states = data.get(0) & 255;
            if ((states & ~15) != 0 || states == 0) {
                throw new IOException("Invalid Iggy button states");
            }
            record.buttonStateHitTest = (states & 1) != 0;
            record.buttonStateUp = (states & 2) != 0;
            record.buttonStateOver = (states & 4) != 0;
            record.buttonStateDown = (states & 8) != 0;
            record.characterId = data.getShort(4) & 65535;
            record.placeDepth = data.getShort(6) & 65535;
            if (record.characterId == 0) {
                throw new IOException("Invalid Iggy button character ID");
            }
            record.placeMatrix = IggyShapeStyles.matrix(source, recordAt + 36, 1);
            record.colorTransform = readColor(source, recordAt + 8);
            // DefineButtonCxform is folded into each record when normalizing to DefineButton2.
            if (global != null) {
                record.colorTransform = compose(global, record.colorTransform);
            }
            int blend = data.get(1) & 255;
            record.buttonHasBlendMode = blend != 15;
            if (record.buttonHasBlendMode) {
                record.blendMode = -1;
                for (int j = 1; j < BLEND.length; j++) {
                    if (BLEND[j] == blend) {
                        record.blendMode = j;
                        break;
                    }
                }
                if (record.blendMode < 0) {
                    throw new IOException("Invalid Iggy button blend mode " + blend);
                }
            }
            int filterCount = data.get(2) & 255;
            record.filterList = new ArrayList<>();
            record.buttonHasFilterList = filterCount != 0;
            if (filterCount != 0) {
                long filters = source.reference(recordAt + (wide ? 64 : 60));
                if (filters == 0) {
                    throw new IOException("Missing Iggy button filters");
                }
                for (int j = 0; j < filterCount; j++) {
                    record.filterList.add(IggyTimelineFilters.read(swf, source, filters + (wide ? 56L : 44L) * j));
                }
            }
            button.characters.add(record);
        }
        button.setModified(true);
        return button;
    }

    private static CXFORMWITHALPHA readColor(IggyTimeline source, long at) throws IOException {
        ByteBuffer c = source.bytes(at, 28);
        CXFORMWITHALPHA result = new CXFORMWITHALPHA();
        result.hasAddTerms = c.get(1) != 0;
        result.hasMultTerms = c.get(2) != 0;
        result.redMultTerm = Math.round(c.getFloat(4) * 256);
        result.greenMultTerm = Math.round(c.getFloat(8) * 256);
        result.blueMultTerm = Math.round(c.getFloat(12) * 256);
        result.alphaMultTerm = c.get(0) == 0 ? 256 : Math.round(c.getFloat(16) * 256);
        result.redAddTerm = c.getShort(20);
        result.greenAddTerm = c.getShort(22);
        result.blueAddTerm = c.getShort(24);
        result.alphaAddTerm = c.get(0) == 0 ? 0 : c.getShort(26);
        return result;
    }

    private static CXFORMWITHALPHA compose(ColorTransform outer, ColorTransform inner) {
        CXFORMWITHALPHA result = new CXFORMWITHALPHA();
        result.hasAddTerms = true;
        result.hasMultTerms = true;
        result.redMultTerm = Math.round(outer.getRedMulti() * inner.getRedMulti() / 256f);
        result.greenMultTerm = Math.round(outer.getGreenMulti() * inner.getGreenMulti() / 256f);
        result.blueMultTerm = Math.round(outer.getBlueMulti() * inner.getBlueMulti() / 256f);
        result.alphaMultTerm = Math.round(outer.getAlphaMulti() * inner.getAlphaMulti() / 256f);
        result.redAddTerm = Math.round(outer.getRedMulti() * inner.getRedAdd() / 256f) + outer.getRedAdd();
        result.greenAddTerm = Math.round(outer.getGreenMulti() * inner.getGreenAdd() / 256f) + outer.getGreenAdd();
        result.blueAddTerm = Math.round(outer.getBlueMulti() * inner.getBlueAdd() / 256f) + outer.getBlueAdd();
        result.alphaAddTerm = Math.round(outer.getAlphaMulti() * inner.getAlphaAdd() / 256f) + outer.getAlphaAdd();
        return result;
    }

    private static void writeColor(IggyTimelineBuffer b, long at, ColorTransform color, boolean alpha) throws IOException {
        b.u8(at, alpha ? 1 : 0);
        b.u8(at + 1, color != null ? 1 : 0);
        b.u8(at + 2, color != null ? 1 : 0);
        int[] mult = color == null ? new int[]{256, 256, 256, 256}
                : new int[]{color.getRedMulti(), color.getGreenMulti(), color.getBlueMulti(), color.getAlphaMulti()};
        int[] add = color == null ? new int[4]
                : new int[]{color.getRedAdd(), color.getGreenAdd(), color.getBlueAdd(), color.getAlphaAdd()};
        for (int i = 0; i < 4; i++) {
            b.f32(at + 4 + 4 * i, mult[i] / 256.0);
            b.u16(at + 20 + 2 * i, add[i]);
        }
    }

    private static boolean hasActions(byte[] bytes) {
        return bytes.length != 0 && (bytes.length != 1 || bytes[0] != 0);
    }

    static void validateReferences(SWF swf, ButtonTag button) throws IOException {
        Set<Integer> available = new HashSet<>();
        collectCharacters(swf.getTags(), available);
        for (BUTTONRECORD record : button.getRecords()) {
            if (!available.contains(record.characterId)) {
                throw new IOException("Missing Iggy button character " + record.characterId);
            }
        }
    }

    private static void collectCharacters(Iterable<Tag> tags, Set<Integer> result) {
        for (Tag tag : tags) {
            if (tag instanceof CharacterTag) {
                result.add(((CharacterTag) tag).getCharacterId());
            }
            if (tag instanceof ImportTag) {
                result.addAll(((ImportTag) tag).getAssets().keySet());
            }
            if (tag instanceof DefineSpriteTag) {
                collectCharacters(((DefineSpriteTag) tag).getTags(), result);
            }
        }
    }

    public static IggyObjectData fromSwf(SWF swf, boolean wide, ButtonTag button) throws IOException {
        boolean legacy = button instanceof DefineButtonTag;
        if (legacy) {
            if (hasActions(((DefineButtonTag) button).getActionBytes().getRangeData())) {
                throw new IggyUnsupportedTagException(button, "Iggy does not store AVM1 button actions");
            }
        } else if (button instanceof DefineButton2Tag) {
            for (BUTTONCONDACTION action : ((DefineButton2Tag) button).actions) {
                if (hasActions(action.actionBytes.getRangeData())) {
                    throw new IggyUnsupportedTagException(button, "Iggy does not store AVM1 button actions");
                }
            }
        } else {
            throw new IggyUnsupportedTagException(button, "Unsupported SWF button tag");
        }
        List<BUTTONRECORD> records = new ArrayList<>(button.getRecords());
        if (records.size() > 32767) {
            throw new IOException("Too many Iggy button records");
        }
        // The DLL sorts by depth. Use a stable sort for records sharing a depth.
        records.sort(Comparator.comparingInt(record -> record.placeDepth));
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int size = wide ? 88 : 60;
        long at = IggyAssetConvertor.header(b, button.getCharacterId(), 7, size);
        ColorTransform global = null;
        if (legacy) {
            for (Tag tag : swf.getTags()) {
                if (tag instanceof DefineButtonCxformTag && ((DefineButtonCxformTag) tag).buttonId == button.getCharacterId()) {
                    global = ((DefineButtonCxformTag) tag).buttonColorTransform;
                }
            }
        }
        writeColor(b, at + (wide ? 40 : 20), global, false);
        b.u16(at + (wide ? 68 : 48), records.size());
        b.u8(at + (wide ? 70 : 50), !legacy && ((DefineButton2Tag) button).trackAsMenu ? 1 : 0);
        b.u8(at + (wide ? 71 : 51), global == null ? 0 : 1);
        int stride = wide ? 72 : 64;
        long array = records.isEmpty() ? -1 : b.allocate(stride * records.size());
        b.pointer(at + (wide ? 72 : 52), array);
        int maxDepth = 0;
        for (int i = 0; i < records.size(); i++) {
            BUTTONRECORD record = records.get(i);
            if (record.characterId <= 0 || record.characterId > 65535 || record.placeDepth < 0 || record.placeDepth > 65535) {
                throw new IOException("Invalid Iggy button character ID or depth");
            }
            int states = (record.buttonStateHitTest ? 1 : 0) | (record.buttonStateUp ? 2 : 0)
                    | (record.buttonStateOver ? 4 : 0) | (record.buttonStateDown ? 8 : 0);
            if (states == 0) {
                throw new IOException("Iggy button record has no states");
            }
            long r = array + (long) stride * i;
            b.value(r, states, 1, 3);
            int blend = !legacy && record.buttonHasBlendMode ? record.blendMode : -1;
            if (blend < -1 || blend >= BLEND.length) {
                throw new IOException("Invalid SWF button blend mode");
            }
            b.u8(r + 1, blend < 0 ? 15 : BLEND[blend]);
            List<FILTER> filters = !legacy && record.buttonHasFilterList ? record.filterList : null;
            if ((!legacy && record.buttonHasFilterList && filters == null) || (filters != null && filters.size() > 255)) {
                throw new IOException("Invalid Iggy button filter list");
            }
            int count = filters == null ? 0 : filters.size();
            b.u8(r + 2, count);
            b.u16(r + 4, record.characterId);
            b.u16(r + 6, record.placeDepth);
            writeColor(b, r + 8, legacy ? null : record.colorTransform, true);
            IggyShapeStyles.matrix(b, r + 36, record.placeMatrix == null ? new MATRIX() : record.placeMatrix, 1);
            long filterArray = count == 0 ? -1 : b.allocate(count * (wide ? 56 : 44));
            b.pointer(r + (wide ? 64 : 60), filterArray);
            for (int j = 0; j < count; j++) {
                IggyTimelineFilters.write(swf, b, filterArray + (wide ? 56L : 44L) * j, filters.get(j));
            }
            if ((states & 14) != 0) {
                maxDepth = Math.max(maxDepth, record.placeDepth);
            }
        }
        b.u32(at + (wide ? 80 : 56), maxDepth + 1L);
        return new IggyObjectData(b.data.getAllBytes(), new ArrayList<>(b.fields.values()), b.characters, at, size, 7);
    }
}
