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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Library wrappers for reused morphs and morphs with instance color effects. */
final class MorphShapeSymbols implements Iterable<Integer> {
    private final Map<Integer, Profile> profiles = new LinkedHashMap<>();
    private static final ColorTransform IDENTITY_COLOR = new ColorTransform();

    private static final class Profile {
        final Map<Integer, Integer> frames = new LinkedHashMap<>();
        boolean wrapped;
    }

    private static final class State {
        int characterId = -1;
        int ratio;
        ColorTransform color;
    }

    static MorphShapeSymbols collect(ReadOnlyTagList tags, Collection<Integer> reused) {
        MorphShapeSymbols result = new MorphShapeSymbols();
        result.scan(tags);
        for (Map.Entry<Integer, Profile> entry : result.profiles.entrySet()) {
            entry.getValue().wrapped |= reused.contains(entry.getKey());
        }
        result.profiles.entrySet().removeIf(entry -> !entry.getValue().wrapped);
        return result;
    }

    private void scan(ReadOnlyTagList tags) {
        Map<Integer, State> depths = new TreeMap<>();
        boolean pendingPlacement = false;
        for (Tag tag : tags) {
            if (tag instanceof DefineSpriteTag) {
                scan(((DefineSpriteTag) tag).getTags());
            }
            if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                State state = depths.get(place.getDepth());
                if (!place.flagMove() || state == null) {
                    state = new State();
                    depths.put(place.getDepth(), state);
                }
                if (place.getCharacterId() >= 0) {
                    state.characterId = place.getCharacterId();
                } else if (place.getClassName() != null && place.getSwf().getCharacterByClass(place.getClassName()) != null) {
                    state.characterId = place.getSwf().getCharacterByClass(place.getClassName()).getCharacterId();
                }
                if (place.getRatio() >= 0) {
                    state.ratio = place.getRatio();
                }
                if (place.getColorTransform() != null) {
                    state.color = place.getColorTransform();
                }
                pendingPlacement = true;
            } else if (tag instanceof RemoveTag) {
                depths.remove(((RemoveTag) tag).getDepth());
            } else if (tag instanceof ShowFrameTag) {
                record(tag.getSwf(), depths);
                pendingPlacement = false;
            }
        }
        // The timeline converter also emits a frame for trailing placements.
        if (pendingPlacement && !tags.isEmpty()) {
            record(tags.get(0).getSwf(), depths);
        }
    }

    private void record(SWF swf, Map<Integer, State> depths) {
        for (State state : depths.values()) {
            if (!(swf.getCharacter(state.characterId) instanceof MorphShapeTag)) {
                continue;
            }
            Profile profile = profiles.computeIfAbsent(state.characterId, key -> new Profile());
            if (!profile.frames.containsKey(state.ratio)) {
                profile.frames.put(state.ratio, profile.frames.size());
            }
            profile.wrapped |= state.color != null && !IDENTITY_COLOR.equals(state.color);
        }
    }

    boolean contains(int characterId) {
        return profiles.containsKey(characterId);
    }

    int frame(int characterId, int ratio) {
        Integer frame = profiles.get(characterId).frames.get(ratio < 0 ? 0 : ratio);
        if (frame == null) {
            throw new IllegalStateException("Missing visible morph ratio " + ratio + " for character " + characterId);
        }
        return frame;
    }

    List<Tag> timeline(SWF swf, int characterId) {
        List<Tag> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : profiles.get(characterId).frames.entrySet()) {
            PlaceObject2Tag place = new PlaceObject2Tag(swf);
            place.depth = 1;
            place.placeFlagMove = entry.getValue() != 0;
            place.placeFlagHasCharacter = entry.getValue() == 0;
            place.characterId = characterId;
            place.placeFlagHasMatrix = entry.getValue() == 0;
            place.matrix = new MATRIX();
            place.placeFlagHasRatio = true;
            place.ratio = entry.getKey();
            result.add(place);
            result.add(new ShowFrameTag(swf));
        }
        return result;
    }

    @Override
    public Iterator<Integer> iterator() {
        return profiles.keySet().iterator();
    }
}
