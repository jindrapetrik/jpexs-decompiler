/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class Frame {

    public TreeMap<Integer, DepthState> layers = new TreeMap<>();
    public DoActionTag action;
    public RGB backgroundColor = new RGBA(0, 0, 0, 0);
    public Timeline timeline;
    public List<Integer> sounds = new ArrayList<>();
    public List<String> soundClasses = new ArrayList<>();
    public List<Tag> innerTags = new ArrayList<>();
    public ShowFrameTag showFrameTag = null; // can be null for the last frame

    public Frame(Timeline timeline) {
        this.timeline = timeline;
    }

    public Frame(Frame obj) {
        layers = new TreeMap<>();
        backgroundColor = obj.backgroundColor;
        timeline = obj.timeline;
        for (int depth : obj.layers.keySet()) {
            layers.put(depth, new DepthState(obj.layers.get(depth), this, true));
        }
        //Do not copy sounds
    }
}
