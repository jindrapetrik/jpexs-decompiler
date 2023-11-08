/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class Frame implements TreeItem, Exportable {

    /**
     * Zero based frame index
     */
    public final int frame;

    public TreeMap<Integer, DepthState> layers = new TreeMap<>();

    public RGB backgroundColor = new RGBA(0, 0, 0, 0);

    public final Timeline timeline;

    public List<Integer> sounds = new ArrayList<>();

    public List<String> soundClasses = new ArrayList<>();

    public List<SOUNDINFO> soundInfos = new ArrayList<>();

    public List<DoActionTag> actions = new ArrayList<>();

    public List<ASMSourceContainer> actionContainers = new ArrayList<>();

    public List<Tag> innerTags = new ArrayList<>();

    public List<Tag> allInnerTags = new ArrayList<>();

    public ShowFrameTag showFrameTag = null; // can be null for the last frame

    public boolean layersChanged;

    public List<String> labels = new ArrayList<>();

    public List<Boolean> namedAnchors = new ArrayList<>();

    public Frame(Timeline timeline, int frame) {
        this.timeline = timeline;
        this.frame = frame;
    }

    public Frame(Frame obj, int frame) {
        this.frame = frame;
        layers = new TreeMap<>();
        backgroundColor = obj.backgroundColor;
        timeline = obj.timeline;
        for (int depth : obj.layers.keySet()) {
            layers.put(depth, new DepthState(obj.layers.get(depth), this, obj.layers.get(depth).placeFrame, true));
        }
        //Do not copy sounds
    }

    @Override
    public Openable getOpenable() {
        return timeline.swf;
    }

    @Override
    public String toString() {
        String name = "frame " + (frame + 1);
        List<String> labels = new ArrayList<>();
        for (Tag t : innerTags) {
            if (t instanceof FrameLabelTag) {
                labels.add(((FrameLabelTag) t).name);
            }
        }
        if (!labels.isEmpty()) {
            name += " (" + String.join(", ", labels) + ")";
        }
        return name;
    }

    @Override
    public String getExportFileName() {
        return "frame_" + (frame + 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Frame) {
            Frame frameObj = (Frame) obj;
            return Objects.equals(timeline, frameObj.timeline) && frame == frameObj.frame;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return timeline.hashCode() ^ Integer.hashCode(frame);
    }

    public boolean isAllInnerTagsModified() {
        for (Tag t : allInnerTags) {
            if (t.isModified() && !t.isReadOnly()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isModified() {
        for (Tag t : innerTags) {
            if (t.isModified() && !t.isReadOnly()) {
                return true;
            }
        }
        for (Tag t : actions) {
            if (t.isModified() && !t.isReadOnly()) {
                return true;
            }
        }
        for (ASMSourceContainer t : actionContainers) {
            if (t.isModified()) {
                return true;
            }
        }
        if (showFrameTag != null && showFrameTag.isModified() && !showFrameTag.isReadOnly()) {
            return true;
        }
        return false;
    }

    public void getNeededCharacters(Set<Integer> needed) {
        for (Tag t : innerTags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                int characterId = place.getCharacterId();
                if (characterId != -1) {
                    needed.add(characterId);
                }
            }
        }
    }

    public void getNeededCharactersDeep(Set<Integer> needed) {
        for (Tag t : innerTags) {
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) t;
                int characterId = ((PlaceObjectTypeTag) t).getCharacterId();
                if (characterId != -1) {
                    CharacterTag character = place.getSwf().getCharacter(characterId);
                    if (character != null) {
                        character.getNeededCharactersDeep(needed);
                    }
                    needed.add(characterId);
                }
            }
        }
    }
}
