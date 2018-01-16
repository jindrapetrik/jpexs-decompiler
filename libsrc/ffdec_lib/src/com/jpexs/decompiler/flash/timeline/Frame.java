/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class Frame implements TreeItem, Exportable {

    public final int frame;

    public TreeMap<Integer, DepthState> layers = new TreeMap<>();

    public RGB backgroundColor = new RGBA(0, 0, 0, 0);

    public final Timeline timeline;

    public List<Integer> sounds = new ArrayList<>();

    public List<String> soundClasses = new ArrayList<>();

    public List<DoActionTag> actions = new ArrayList<>();

    public List<ASMSourceContainer> actionContainers = new ArrayList<>();

    public List<Tag> innerTags = new ArrayList<>();

    public ShowFrameTag showFrameTag = null; // can be null for the last frame

    public boolean layersChanged;

    public String label = null;

    public boolean namedAnchor = false;

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
            layers.put(depth, new DepthState(obj.layers.get(depth), this, true));
        }
        //Do not copy sounds
    }

    @Override
    public SWF getSwf() {
        return timeline.swf;
    }

    @Override
    public String toString() {
        return "frame " + (frame + 1);
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

    @Override
    public boolean isModified() {
        for (Tag t : innerTags) {
            if (t.isModified()) {
                return true;
            }
        }
        for (Tag t : actions) {
            if (t.isModified()) {
                return true;
            }
        }
        for (ASMSourceContainer t : actionContainers) {
            if (t.isModified()) {
                return true;
            }
        }
        if (showFrameTag != null && showFrameTag.isModified()) {
            return true;
        }
        return false;
    }
}
