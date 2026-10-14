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
import com.jpexs.decompiler.flash.iggy.IggyFile;
import com.jpexs.decompiler.flash.iggy.IggyObjectData;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.StaticTextTag;
import com.jpexs.decompiler.flash.tags.profiles.iggy.IggyExternalImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Converts dictionary graphics, buttons and sound while retaining unchanged
 * binary graphs.
 */
public final class IggyAssetConvertor {

    private IggyAssetConvertor() {
    }

    static long header(IggyTimelineBuffer b, int id, int type, int size) throws IOException {
        if (id <= 0 || id > 65535) {
            throw new IOException("Invalid Iggy asset character ID " + id);
        }
        long at = b.allocate(size);
        b.u8(at, type);
        b.value(at + 1, 255, 1, 12);
        b.u16(at + 2, id);
        b.u32(at + (b.wide ? 8 : 4), 0);
        b.pointer(at + (b.wide ? 16 : 8), -1);
        return at;
    }

    private static boolean supported(int type) {
        return type == 1 || type == 2 || type == 4 || type == 5 || type == 7 || type == 20 || type == 21;
    }

    private static Tag toSwf(SWF swf, IggyFile file, IggySwf movie, IggyTimeline source, IggySwf.DictionaryEntry entry) throws IOException {
        switch (entry.getType()) {
            case 1:
            case 2:
                return IggyVectorConvertor.toSwf(swf, movie, source, entry.getAddress(), entry.getId(), entry.getType());
            case 4:
                return IggyBitmapConvertor.toSwf(swf, file, source, entry.getAddress(), entry.getId());
            case 5:
                return IggyStaticTextConvertor.toSwf(swf, movie, source, entry.getAddress(), entry.getId());
            case 20:
                return IggyBinaryDataConvertor.toSwf(swf, source, entry.getAddress(), entry.getId());
            case 7:
                return IggyButtonConvertor.toSwf(swf, source, entry.getAddress(), entry.getId());
            case 21:
                return IggySoundConvertor.toSwf(swf, source, entry.getAddress(), entry.getId());
            default:
                throw new IOException("Unsupported Iggy asset type");
        }
    }

    public static void exportAssets(SWF swf, IggyFile file, Set<Integer> exportedIds) throws IOException {
        IggySwf movie = file.getSwf();
        IggyTimeline source = movie.getTimeline();
        // Images precede shapes using them as fills; buttons follow their graphics.
        for (int pass = 0; pass < 3; pass++) {
            for (IggySwf.DictionaryEntry entry : movie.getDictionary()) {
                if (!supported(entry.getType()) || (entry.getType() <= 2 ? 1 : (entry.getType() == 7 || entry.getType() == 5) ? 2 : 0) != pass) {
                    continue;
                }
                swf.addTag(toSwf(swf, file, movie, source, entry));
                exportedIds.add(entry.getId());
            }
        }
    }

    private static IggyObjectData fromSwf(SWF swf, boolean wide, Tag tag) throws IOException {
        if (tag instanceof ShapeTag || tag instanceof MorphShapeTag) {
            return IggyVectorConvertor.fromSwf(swf, wide, tag);
        }
        if (tag instanceof ImageTag) {
            return IggyBitmapConvertor.fromSwf(swf, wide, (ImageTag) tag);
        }
        if (tag instanceof ButtonTag) {
            return IggyButtonConvertor.fromSwf(swf, wide, (ButtonTag) tag);
        }
        if (tag instanceof StaticTextTag) {
            return IggyStaticTextConvertor.fromSwf(wide, (StaticTextTag) tag);
        }
        if (tag instanceof DefineBinaryDataTag) {
            return IggyBinaryDataConvertor.fromSwf(wide, (DefineBinaryDataTag) tag);
        }
        return IggySoundConvertor.fromSwf(wide, (DefineSoundTag) tag);
    }

    public static void updateAssets(IggySwf movie, SWF swf) throws IOException {
        movie.clearAssetChanges();
        updateAssets(movie, swf, swf.getTags(), new HashSet<Integer>());
    }

    private static void updateAssets(IggySwf movie, SWF swf, Iterable<Tag> tags, Set<Integer> seen) throws IOException {
        for (Tag tag : tags) {
            if (tag instanceof DefineSpriteTag) {
                updateAssets(movie, swf, ((DefineSpriteTag) tag).getTags(), seen);
                continue;
            }
            if (tag instanceof DefineButtonSoundTag) {
                throw new IOException("Iggy does not store DefineButtonSound; use timeline or AS3 sounds");
            }
            int id;
            if (tag instanceof ImageTag) {
                id = ((ImageTag) tag).characterID;
            } else if (tag instanceof ShapeTag) {
                id = ((ShapeTag) tag).shapeId;
            } else if (tag instanceof MorphShapeTag) {
                id = ((MorphShapeTag) tag).characterId;
            } else if (tag instanceof StaticTextTag) {
                id = ((StaticTextTag) tag).characterID;
            } else if (tag instanceof DefineBinaryDataTag) {
                id = ((DefineBinaryDataTag) tag).tag;
            } else if (tag instanceof ButtonTag) {
                id = ((ButtonTag) tag).getCharacterId();
            } else if (tag instanceof DefineSoundTag) {
                id = ((DefineSoundTag) tag).soundId;
            } else {
                continue;
            }
            if (!seen.add(id)) {
                throw new IOException("Duplicate Iggy asset ID " + id);
            }
            if (tag instanceof ButtonTag) {
                IggyButtonConvertor.validateReferences(swf, (ButtonTag) tag);
            }
            if (tag instanceof StaticTextTag) {
                IggyStaticTextConvertor.validateReferences(swf, (StaticTextTag) tag);
            }
            IggySwf.DictionaryEntry entry = id > 0 && id < movie.getDictionary().size() ? movie.getDictionary().get(id) : null;
            if (entry != null && !entry.isNull() && !supported(entry.getType())) {
                throw new IOException("Iggy asset ID is occupied by another object type: " + id);
            }
            IggyObjectData replacement = fromSwf(swf, movie.is64(), tag);
            boolean changed = true;
            if (entry != null && supported(entry.getType())) {
                IggyObjectData baseline = fromSwf(swf, movie.is64(), toSwf(swf, null, movie, movie.getTimeline(), entry));
                changed = !Arrays.equals(replacement.data, baseline.data) || !replacement.characters.equals(baseline.characters);
            }
            if (changed) {
                movie.replaceObject(id, replacement);
            }
            if (changed && entry != null && entry.getType() == 4 && replacement.type != 4) {
                movie.replaceTextureRecord(id, null);
            }
            if (tag instanceof IggyExternalImage && ((IggyExternalImage) tag).replacementImage.getLength() == 0) {
                IggyExternalImage external = (IggyExternalImage) tag;
                if (!external.fileName.equals(movie.getTextureResourceName())) {
                    throw new IOException("Iggy stores external texture IDs, not filenames; the companion filename cannot be changed in the movie");
                }
                if (external.resourceType == 8 && external.resourceOffset >= 0) {
                    ByteBuffer record = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
                    record.putInt((int) external.resourceOffset).putShort((short) external.resourceFormat)
                            .putShort((short) external.resourceWidth).putShort((short) external.resourceHeight).putShort((short) id);
                    movie.replaceTextureRecord(id, record.array());
                }
            } else if (tag instanceof ImageTag && changed) {
                movie.replaceTextureRecord(id, null);
            }
        }
    }
}
