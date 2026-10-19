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
import com.jpexs.decompiler.flash.iggy.IggyNamedFrame;
import com.jpexs.decompiler.flash.iggy.IggySwf;
import com.jpexs.decompiler.flash.iggy.IggyTimeline;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.SOUNDENVELOPE;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Linked display lists used by all three Iggy layouts.
 */
public final class IggyTimelineConvertor {

    private static final int[] BLEND = {1, 1, 2, 3, 4, 5, 6, 9, 7, 8, 10, 11, 12, 13, 14};

    private IggyTimelineConvertor() {
    }

    private static int u16(ByteBuffer b, int at) {
        return b.getShort(at) & 65535;
    }

    private static int u8(ByteBuffer b, int at) {
        return b.get(at) & 255;
    }

    public static List<Tag> toSwf(SWF swf, IggySwf movie, IggyTimeline timeline, int frames) throws IOException {
        List<Tag> result = new ArrayList<>();
        boolean wide = timeline.is64();
        int p = wide ? 8 : 4;
        int actualFrames = 0;
        for (IggyTimeline.Command command : timeline.getCommands()) {
            long address = command.getAddress();
            ByteBuffer b = timeline.bytes(address, command.getData().length);
            Tag tag;
            switch (command.getType()) {
                case 1:
                    tag = new ShowFrameTag(swf);
                    actualFrames++;
                    break;
                case 2:
                    PlaceObject3Tag place = new PlaceObject3Tag(swf);
                    int flags = u8(b, p + 1);
                    int depth = wide ? 40 : 36;
                    place.depth = u16(b, depth);
                    place.placeFlagMove = (flags & 4) != 0;
                    place.placeFlagHasMatrix = (flags & 1) != 0;
                    if (place.placeFlagHasMatrix) {
                        int m = wide ? 16 : 12;
                        MATRIX matrix = new MATRIX();
                        matrix.hasScale = true;
                        matrix.hasRotate = true;
                        matrix.scaleX = b.getFloat(m);
                        matrix.rotateSkew1 = b.getFloat(m + 4);
                        matrix.rotateSkew0 = b.getFloat(m + 8);
                        matrix.scaleY = b.getFloat(m + 12);
                        matrix.translateX = (int) Math.round(b.getFloat(m + 16) * SWF.unitDivisor);
                        matrix.translateY = (int) Math.round(b.getFloat(m + 20) * SWF.unitDivisor);
                        place.matrix = matrix;
                    }
                    long characterField = address + (wide ? 48 : 40);
                    int id = movie.getDictionaryId(characterField, timeline.reference(characterField));
                    place.placeFlagHasCharacter = id >= 0;
                    place.characterId = id;
                    place.placeFlagHasClassName = (flags & 32) != 0;
                    place.placeFlagHasImage = (flags & 8) != 0;
                    if (place.placeFlagHasClassName || place.placeFlagHasImage && place.placeFlagHasCharacter) {
                        place.className = timeline.string(timeline.reference(address + (wide ? 80 : 56)));
                    }
                    place.placeFlagHasName = (flags & 64) != 0;
                    if (place.placeFlagHasName) {
                        place.name = timeline.string(timeline.reference(address + (wide ? 64 : 48)));
                    }
                    place.placeFlagHasColorTransform = (flags & 2) != 0;
                    if (place.placeFlagHasColorTransform) {
                        long color = timeline.reference(address + (wide ? 72 : 52));
                        ByteBuffer c = timeline.bytes(color, 28);
                        CXFORMWITHALPHA cx = new CXFORMWITHALPHA();
                        cx.hasAddTerms = c.get(1) != 0;
                        cx.hasMultTerms = c.get(2) != 0;
                        cx.redMultTerm = Math.round(c.getFloat(4) * 256);
                        cx.greenMultTerm = Math.round(c.getFloat(8) * 256);
                        cx.blueMultTerm = Math.round(c.getFloat(12) * 256);
                        cx.alphaMultTerm = Math.round(c.getFloat(16) * 256);
                        cx.redAddTerm = c.getShort(20);
                        cx.greenAddTerm = c.getShort(22);
                        cx.blueAddTerm = c.getShort(24);
                        cx.alphaAddTerm = c.getShort(26);
                        place.colorTransform = cx;
                    }
                    place.placeFlagHasRatio = true;
                    place.ratio = u16(b, wide ? 56 : 44);
                    place.placeFlagHasClipDepth = (flags & 128) != 0;
                    place.clipDepth = u16(b, wide ? 58 : 46);
                    int blend = u8(b, p + 3);
                    place.placeFlagHasBlendMode = blend != 15;
                    if (place.placeFlagHasBlendMode) {
                        int found = -1;
                        for (int i = 1; i < BLEND.length; i++) {
                            if (BLEND[i] == blend) {
                                found = i;
                                break;
                            }
                        }
                        if (found < 0) {
                            throw new IOException("Invalid Iggy blend mode: " + blend);
                        }
                        place.blendMode = found;
                    }
                    place.placeFlagHasCacheAsBitmap = (flags & 16) != 0;
                    place.bitmapCache = 1;
                    int filterCount = u8(b, p + 2);
                    place.placeFlagHasFilterList = filterCount != 127;
                    place.surfaceFilterList = new ArrayList<>();
                    if (filterCount != 127 && filterCount > 0) {
                        long filters = timeline.reference(address + (wide ? 88 : 60));
                        for (int i = 0; i < filterCount; i++) {
                            place.surfaceFilterList.add(IggyTimelineFilters.read(swf, timeline, filters + (wide ? 56L : 44L) * i));
                        }
                    }
                    tag = place;
                    break;
                case 3:
                    RemoveObject2Tag remove = new RemoveObject2Tag(swf);
                    remove.depth = u16(b, p + 2);
                    tag = remove;
                    break;
                case 4:
                    PlaceObject3Tag ratio = new PlaceObject3Tag(swf);
                    ratio.depth = u16(b, p + 4);
                    ratio.placeFlagMove = true;
                    ratio.placeFlagHasRatio = true;
                    ratio.ratio = u16(b, p + 6);
                    tag = ratio;
                    break;
                case 5:
                    FrameLabelTag label = new FrameLabelTag(swf);
                    label.name = timeline.string(timeline.reference(address + (wide ? 16 : 8)));
                    label.namedAnchor = b.get(p + 1) != 0;
                    tag = label;
                    break;
                case 6:
                    SOUNDINFO info = new SOUNDINFO();
                    int start = wide ? 12 : 8;
                    info.syncStop = b.get(start + 2) != 0;
                    info.syncNoMultiple = b.get(start + 3) != 0;
                    info.inPoint = b.getInt(wide ? 24 : 16) & 0xffffffffL;
                    info.outPoint = b.getInt(wide ? 28 : 20) & 0xffffffffL;
                    info.loopCount = b.getInt(wide ? 36 : 28);
                    info.hasInPoint = info.inPoint != 0;
                    info.hasOutPoint = info.outPoint != 0x7fffffff;
                    info.hasLoops = info.loopCount != 1;
                    int count = b.getInt(wide ? 40 : 32);
                    if (count < 0 || count > 255) {
                        throw new IOException("Invalid Iggy sound envelope count");
                    }
                    info.hasEnvelope = count != 0;
                    info.envelopeRecords = new SOUNDENVELOPE[count];
                    if (count > 0) {
                        long envelopes = timeline.reference(address + (wide ? 48 : 36));
                        ByteBuffer e = timeline.bytes(envelopes, count * 8);
                        for (int i = 0; i < count; i++) {
                            SOUNDENVELOPE envelope = new SOUNDENVELOPE();
                            envelope.pos44 = e.getInt() & 0xffffffffL;
                            envelope.leftLevel = e.getShort() & 65535;
                            envelope.rightLevel = e.getShort() & 65535;
                            info.envelopeRecords[i] = envelope;
                        }
                    }
                    long classAddress = timeline.reference(address + (wide ? 16 : 12));
                    if (classAddress != 0) {
                        StartSound2Tag sound = new StartSound2Tag(swf);
                        sound.soundClassName = timeline.string(classAddress);
                        sound.soundInfo = info;
                        tag = sound;
                    } else {
                        StartSoundTag sound = new StartSoundTag(swf);
                        sound.soundId = u16(b, start);
                        sound.soundInfo = info;
                        tag = sound;
                    }
                    break;
                default:
                    throw new IOException("Unsupported Iggy timeline command " + command.getType());
            }
            tag.setModified(true);
            result.add(tag);
        }
        while (actualFrames++ < frames) {
            result.add(new ShowFrameTag(swf));
        }
        return result;
    }

    static boolean command(Tag tag) {
        return tag instanceof PlaceObjectTypeTag || tag instanceof RemoveTag || tag instanceof ShowFrameTag
                || tag instanceof FrameLabelTag || tag instanceof StartSoundTag || tag instanceof StartSound2Tag;
    }

    public static void update(SWF swf, IggySwf movie, int characterId, Iterable<Tag> tags, int frames) throws IOException {
        IggyTimeline original = characterId == 0 ? movie.getTimeline() : movie.getSpriteTimelines().get(characterId);
        if (original == null) {
            IggyTimeline.Replacement replacement = fromSwf(swf, movie.is64(), tags, frames);
            movie.replaceTimeline(characterId, restoreClassReferences(movie,
                    new IggyTimeline(new byte[0], 0, movie.is64()), replacement));
            return;
        }
        IggyTimeline.Replacement replacement = fromSwf(swf, original.is64(), tags, frames);
        int oldFrames = movie.getTimelineFrameCount(characterId);
        IggyTimeline.Replacement baseline = fromSwf(swf, original.is64(), toSwf(swf, movie, original, oldFrames), oldFrames);
        // Comparison in the SWF-representable domain preserves original compact records,
        // opaque data and discarded presence bits on an unchanged round trip.
        if (replacement.frameCount != baseline.frameCount || !Arrays.equals(replacement.data, baseline.data)
                || !replacement.characters.equals(baseline.characters)) {
            movie.replaceTimeline(characterId, restoreClassReferences(movie, original, replacement));
        } else {
            movie.replaceTimeline(characterId, null);
        }
    }

    /**
     * Class-name placements follow FlashPlayer's SWF encoding. Preserve the
     * additional Iggy binding when an unchanged class still identifies it
     * uniquely.
     */
    private static IggyTimeline.Replacement restoreClassReferences(IggySwf movie, IggyTimeline original,
            IggyTimeline.Replacement replacement) throws IOException {
        Map<String, Integer> classes = new HashMap<>();
        Map<Integer, String> images = new HashMap<>();
        boolean wide = original.is64();
        int p = wide ? 8 : 4;
        for (IggyTimeline.Command command : original.getCommands()) {
            if (command.getType() != 2) {
                continue;
            }
            long at = command.getAddress();
            int flags = original.bytes(at + p + 1, 1).get() & 255;
            long field = at + (wide ? 48 : 40);
            int id = movie.getDictionaryId(field, original.reference(field));
            if ((flags & 32) != 0 && id >= 0 && !movie.isDefinitionRemoved(id)) {
                String name = original.string(original.reference(at + (wide ? 80 : 56)));
                if (classes.containsKey(name) && classes.get(name) != id) {
                    classes.put(name, -1);
                } else {
                    classes.put(name, id);
                }
            } else if ((flags & 8) != 0 && id >= 0) {
                String name = original.string(original.reference(at + (wide ? 80 : 56)));
                if (images.containsKey(id) && !java.util.Objects.equals(images.get(id), name)) {
                    images.put(id, null);
                } else {
                    images.put(id, name);
                }
            }
        }
        for (com.jpexs.decompiler.flash.iggy.IggyImport library : movie.getImports()) {
            for (com.jpexs.decompiler.flash.iggy.IggySymbolClass binding : library.getSymbols()) {
                classes.put(binding.getName(), (int) binding.getCharacterId());
            }
        }
        for (com.jpexs.decompiler.flash.iggy.IggySymbolClass binding : movie.getSymbolClasses()) {
            if (binding.getCharacterId() != 0) {
                classes.put(binding.getName(), (int) binding.getCharacterId());
            }
        }
        Map<Long, Integer> characters = new LinkedHashMap<>(replacement.characters);
        List<IggyFixup.Field> fields = new ArrayList<>(replacement.fields);
        MemoryDataStream data = new MemoryDataStream();
        data.writeBytes(replacement.data);
        IggyTimeline rebuilt = new IggyTimeline(replacement.data, replacement.first < 0 ? 0 : replacement.first, wide);
        for (IggyTimeline.Command command : rebuilt.getCommands()) {
            if (command.getType() != 2) {
                continue;
            }
            long at = command.getAddress();
            int flags = rebuilt.bytes(at + p + 1, 1).get() & 255;
            long field = at + (wide ? 48 : 40);
            long classField = at + (wide ? 80 : 56);
            if ((flags & 32) != 0) {
                String name = rebuilt.string(rebuilt.reference(classField));
                Integer id = classes.get(name);
                if (!characters.containsKey(field) && id != null && id >= 0) {
                    characters.put(field, id);
                }
            } else if ((flags & 8) != 0) {
                String name = images.get(characters.get(field));
                if (name != null) {
                    data.pad8bytes();
                    long target = data.position();
                    data.writeWChar(name);
                    for (int i = 0; i <= name.length(); i++) {
                        fields.add(new IggyFixup.Field(target + 2L * i, 4));
                    }
                    long end = data.position();
                    data.seek(classField, SeekMode.SET);
                    if (wide) {
                        data.writeUI64(target - classField);
                    } else {
                        data.writeUI32(target - classField);
                    }
                    data.seek(end, SeekMode.SET);
                }
            }
        }
        return new IggyTimeline.Replacement(data.getAllBytes(), fields, characters, replacement.first,
                replacement.last, replacement.maxDepth, replacement.frameCount, replacement.labels);
    }

    public static IggyTimeline.Replacement fromSwf(SWF swf, boolean wide, Iterable<Tag> tags, int frames) throws IOException {
        IggyTimelineBuffer b = new IggyTimelineBuffer(wide);
        int p = wide ? 8 : 4;
        int actualFrames = 0;
        int maxDepth = 0;
        long first = -1;
        long previous = -1;
        List<Long> placements = new ArrayList<>();
        List<Long> placementOwners = new ArrayList<>();
        Map<Integer, Long> lifetimes = new HashMap<>();
        Map<Integer, MATRIX> matrices = new HashMap<>();
        List<IggyNamedFrame> labels = new ArrayList<>();
        for (Tag tag : tags) {
            if (!command(tag)) {
                continue;
            }
            int type = tag instanceof ShowFrameTag ? 1 : tag instanceof PlaceObjectTypeTag ? 2
                    : tag instanceof RemoveTag ? 3 : tag instanceof FrameLabelTag ? 5 : 6;
            int size = type == 2 ? (wide ? 96 : 64) : type == 6 ? (wide ? 56 : 40)
                    : type == 3 || type == 5 ? (wide ? 24 : 12) : (wide ? 16 : 8);
            long at = b.allocate(size);
            if (first < 0) {
                first = at;
            }
            if (previous >= 0) {
                b.pointer(previous, at);
            }
            previous = at;
            b.pointer(at, -1);
            b.u8(at + p, type);
            if (type == 1) {
                actualFrames++;
                continue;
            }
            if (type == 2) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                if (place.getClipActions() != null) {
                    throw new IggyUnsupportedTagException(tag, "Iggy does not store SWF clip actions");
                }
                if (place.getVisible() != null && !place.isVisible() || place.getBackgroundColor() != null) {
                    throw new IggyUnsupportedTagException(tag, "Iggy does not store timeline visibility or opaque background");
                }
                int depth = place.getDepth();
                if (depth < 0 || depth > 65535) {
                    throw new IOException("Invalid Iggy placement depth");
                }
                maxDepth = Math.max(maxDepth, depth);
                MATRIX matrix = place.getMatrix();
                ColorTransform color = place.getColorTransform();
                String name = place.getInstanceName();
                String className = place.getClassName();
                boolean hasCharacter = place.getCharacterId() >= 0;
                boolean explicitClass = place instanceof PlaceObject3Tag && ((PlaceObject3Tag) place).placeFlagHasClassName;
                // Match the native SWF loader's matrix preprocessing. Seeking
                // skips expired placements, so a replacement must carry the
                // matrix it inherits from earlier commands at the same depth.
                if (matrix != null) {
                    matrices.put(depth, matrix);
                } else if (hasCharacter || explicitClass) {
                    matrix = matrices.get(depth);
                }
                int flags = (matrix != null ? 1 : 0) | (color != null ? 2 : 0) | (place.flagMove() ? 4 : 0)
                        | (place.hasImage() ? 8 : 0) | (place.cacheAsBitmap() ? 16 : 0)
                        | (explicitClass ? 32 : 0) | (name != null ? 64 : 0) | (place.getClipDepth() >= 0 ? 128 : 0);
                b.value(at + p + 1, flags, 1, 3);
                b.u16(at + (wide ? 40 : 36), depth);
                b.u32(at + p + 4, 65536);
                long characterField = at + (wide ? 48 : 40);
                b.pointer(characterField, -1);
                if (hasCharacter) {
                    b.characters.put(characterField, place.getCharacterId());
                }
                if (hasCharacter || className != null) {
                    Long old = lifetimes.put(depth, at);
                    if (old != null) {
                        b.u32(old + p + 4, actualFrames);
                    }
                }
                placements.add(at);
                placementOwners.add(lifetimes.get(depth));
                if (matrix != null) {
                    int m = wide ? 16 : 12;
                    b.f32(at + m, matrix.hasScale ? matrix.scaleX : 1);
                    b.f32(at + m + 4, matrix.hasRotate ? matrix.rotateSkew1 : 0);
                    b.f32(at + m + 8, matrix.hasRotate ? matrix.rotateSkew0 : 0);
                    b.f32(at + m + 12, matrix.hasScale ? matrix.scaleY : 1);
                    b.f32(at + m + 16, matrix.translateX / SWF.unitDivisor);
                    b.f32(at + m + 20, matrix.translateY / SWF.unitDivisor);
                }
                b.string(at + (wide ? 64 : 48), name);
                b.string(at + (wide ? 80 : 56), className);
                b.pointer(at + (wide ? 72 : 52), -1);
                if (color != null) {
                    long c = b.allocate(28);
                    b.pointer(at + (wide ? 72 : 52), c);
                    b.u8(c, 1);
                    b.u8(c + 1, 1);
                    b.u8(c + 2, 1);
                    int[] multipliers = {color.getRedMulti(), color.getGreenMulti(), color.getBlueMulti(), color.getAlphaMulti()};
                    int[] additions = {color.getRedAdd(), color.getGreenAdd(), color.getBlueAdd(), color.getAlphaAdd()};
                    for (int i = 0; i < 4; i++) {
                        b.f32(c + 4 + 4 * i, multipliers[i] / 256.0);
                        b.u16(c + 20 + 2 * i, additions[i]);
                    }
                }
                b.u16(at + (wide ? 56 : 44), Math.max(0, place.getRatio()));
                b.u16(at + (wide ? 58 : 46), Math.max(0, place.getClipDepth()));
                if (place.getClipDepth() >= 0) {
                    maxDepth = Math.max(maxDepth, place.getClipDepth());
                }
                int blend = place.getBlendMode();
                if (!(place instanceof PlaceObject3Tag) || !((PlaceObject3Tag) place).placeFlagHasBlendMode) {
                    blend = -1;
                }
                if (blend >= BLEND.length) {
                    throw new IOException("Invalid SWF blend mode");
                }
                b.u8(at + p + 3, blend < 0 ? 15 : BLEND[blend]);
                List<FILTER> filters = place.getFilters();
                int count = filters == null ? 127 : filters.size();
                if (count > 126 && filters != null) {
                    throw new IOException("Too many Iggy filters");
                }
                b.u8(at + p + 2, count);
                b.pointer(at + (wide ? 88 : 60), -1);
                if (filters != null && !filters.isEmpty()) {
                    long data = b.allocate(filters.size() * (wide ? 56 : 44));
                    b.pointer(at + (wide ? 88 : 60), data);
                    for (int i = 0; i < filters.size(); i++) {
                        IggyTimelineFilters.write(swf, b, data + (wide ? 56L : 44L) * i, filters.get(i));
                    }
                }
            } else if (type == 3) {
                int depth = ((RemoveTag) tag).getDepth();
                matrices.remove(depth);
                maxDepth = Math.max(maxDepth, depth);
                b.u16(at + p + 2, depth);
                b.pointer(at + (wide ? 16 : 8), -1);
                Long old = lifetimes.remove(depth);
                if (old != null) {
                    b.u32(old + p + 4, actualFrames);
                }
            } else if (type == 5) {
                FrameLabelTag label = (FrameLabelTag) tag;
                labels.add(new IggyNamedFrame(label.name, actualFrames, 0));
                b.u8(at + p + 1, label.namedAnchor ? 1 : 0);
                b.string(at + (wide ? 16 : 8), label.name);
            } else {
                boolean byClass = tag instanceof StartSound2Tag;
                SOUNDINFO info = byClass ? ((StartSound2Tag) tag).soundInfo : ((StartSoundTag) tag).soundInfo;
                int start = wide ? 12 : 8;
                b.u16(at + start, byClass ? 0 : ((StartSoundTag) tag).soundId);
                b.u8(at + start + 2, info.syncStop ? 1 : 0);
                b.u8(at + start + 3, info.syncNoMultiple ? 1 : 0);
                b.string(at + (wide ? 16 : 12), byClass ? ((StartSound2Tag) tag).soundClassName : null);
                b.u32(at + (wide ? 24 : 16), info.hasInPoint ? info.inPoint : 0);
                b.u32(at + (wide ? 28 : 20), info.hasOutPoint ? info.outPoint : 0x7fffffff);
                b.u32(at + (wide ? 36 : 28), info.hasLoops ? info.loopCount : 1);
                int count = info.hasEnvelope ? info.envelopeRecords.length : 0;
                b.u32(at + (wide ? 40 : 32), count);
                b.pointer(at + (wide ? 48 : 36), -1);
                if (count > 0) {
                    long data = b.allocate(count * 8);
                    b.pointer(at + (wide ? 48 : 36), data);
                    for (int i = 0; i < count; i++) {
                        SOUNDENVELOPE e = info.envelopeRecords[i];
                        b.u32(data + 8L * i, e.pos44);
                        b.u16(data + 8L * i + 4, e.leftLevel);
                        b.u16(data + 8L * i + 6, e.rightLevel);
                    }
                }
            }
        }
        while (actualFrames < frames) {
            long at = b.allocate(wide ? 16 : 8);
            if (first < 0) {
                first = at;
            }
            if (previous >= 0) {
                b.pointer(previous, at);
            }
            previous = at;
            b.pointer(at, -1);
            b.u8(at + p, 1);
            actualFrames++;
        }
        // Moves inherit the lifespan of the placement active at their frame.
        ByteBuffer body = ByteBuffer.wrap(b.data.getAllBytes()).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < placements.size(); i++) {
            long at = placements.get(i);
            Long owner = placementOwners.get(i);
            if (owner != null && owner != at) {
                b.u32(at + p + 4, body.getInt(owner.intValue() + p + 4));
            }
        }
        return new IggyTimeline.Replacement(b.data.getAllBytes(), new ArrayList<>(b.fields.values()),
                b.characters, first, previous, maxDepth + 1, frames, labels);
    }
}
