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
import com.jpexs.decompiler.flash.iggy.IggySymbolClass;
import com.jpexs.decompiler.flash.iggy.IggyText;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImportTag;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Supplementary movie tables and sprite scaling grids.
 */
public final class IggyMovieTagConvertor {

    private IggyMovieTagConvertor() {
    }

    public static void exportScalingGrids(SWF swf, IggySwf movie) throws IOException {
        for (int id : movie.getSpriteTimelines().keySet()) {
            float[] rect = movie.getScalingGrid(id);
            if (rect == null) {
                continue;
            }
            DefineScalingGridTag grid = new DefineScalingGridTag(swf);
            grid.characterId = id;
            grid.splitter = new RECT(Math.round(rect[0] * 20), Math.round(rect[2] * 20),
                    Math.round(rect[1] * 20), Math.round(rect[3] * 20));
            grid.setModified(true);
            swf.addTag(grid);
        }
    }

    private static void collect(Iterable<Tag> tags, Map<Integer, float[]> grids, Set<Integer> available) throws IOException {
        for (Tag tag : tags) {
            if (tag instanceof CharacterTag) {
                available.add(((CharacterTag) tag).getCharacterId());
            }
            if (tag instanceof ImportTag) {
                available.addAll(((ImportTag) tag).getAssets().keySet());
            }
            if (tag instanceof DefineScalingGridTag) {
                DefineScalingGridTag source = (DefineScalingGridTag) tag;
                if (grids.containsKey(source.characterId)) {
                    throw new IOException("Duplicate Iggy scaling grid");
                }
                RECT r = source.splitter;
                if (r == null) {
                    throw new IOException("Missing Iggy scaling grid rectangle");
                }
                grids.put(source.characterId, new float[]{r.Xmin / 20f, r.Ymin / 20f, r.Xmax / 20f, r.Ymax / 20f});
            }
            if (tag instanceof DefineSpriteTag) {
                collect(((DefineSpriteTag) tag).getTags(), grids, available);
            }
        }
    }

    public static void update(IggySwf movie, SWF swf) throws IOException {
        Map<Integer, float[]> grids = new LinkedHashMap<>();
        Set<Integer> available = new HashSet<>();
        collect(swf.getTags(), grids, available);
        movie.setScalingGrids(grids);
        DefineSceneAndFrameLabelDataTag sceneTag = null;
        List<IggySymbolClass> symbols = new ArrayList<>();
        Map<Integer, String> symbolNames = new LinkedHashMap<>();
        boolean as3 = false;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof ABCContainerTag || (tag instanceof FileAttributesTag && ((FileAttributesTag) tag).actionScript3)) {
                as3 = true;
            }
            if (tag instanceof DefineSceneAndFrameLabelDataTag) {
                if (sceneTag != null) {
                    throw new IOException("Duplicate Iggy scene metadata tag");
                }
                sceneTag = (DefineSceneAndFrameLabelDataTag) tag;
            }
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag source = (SymbolClassTag) tag;
                if (source.tags.size() != source.names.size()) {
                    throw new IOException("Invalid Iggy SymbolClass table");
                }
                for (int i = 0; i < source.tags.size(); i++) {
                    int id = source.tags.get(i);
                    if ((id != 0 && !available.contains(id)) || symbolNames.containsKey(id)) {
                        throw new IOException("Invalid or duplicate Iggy SymbolClass character ID " + id);
                    }
                    symbolNames.put(id, source.names.get(i));
                }
            }
        }
        movie.setAS3(as3);
        Set<Long> originalSymbolIds = new HashSet<>();
        for (IggySymbolClass s : movie.getOriginalSymbolClasses()) {
            originalSymbolIds.add(s.getCharacterId());
        }
        Set<Integer> intrinsic = new HashSet<>();
        for (IggyText text : movie.getTexts()) {
            if (movie.hasOriginalTextSymbolClass(text.getTextIndex())) {
                if (!originalSymbolIds.contains((long) text.getTextIndex())) {
                    intrinsic.add(text.getTextIndex());
                }
                text.setSymbolClassName(symbolNames.get(text.getTextIndex()));
            }
        }
        for (Map.Entry<Integer, String> s : symbolNames.entrySet()) {
            if (!intrinsic.contains(s.getKey())) {
                symbols.add(new IggySymbolClass(0, s.getValue(), s.getKey(), 0));
            }
        }
        movie.setSymbolClasses(symbols);
        List<IggyNamedFrame> scenes = new ArrayList<>();
        List<IggyNamedFrame> labels = new ArrayList<>();
        if (sceneTag != null) {
            if (sceneTag.sceneOffsets.length != sceneTag.sceneNames.length || sceneTag.frameNums.length != sceneTag.frameNames.length) {
                throw new IOException("Invalid Iggy scene metadata arrays");
            }
            for (int i = 0; i < sceneTag.sceneNames.length; i++) {
                scenes.add(new IggyNamedFrame(sceneTag.sceneNames[i], sceneTag.sceneOffsets[i], 0));
            }
            for (int i = 0; i < sceneTag.frameNames.length; i++) {
                labels.add(new IggyNamedFrame(sceneTag.frameNames[i], sceneTag.frameNums[i], 0));
            }
        }
        movie.setNamedFrames(scenes, labels);
    }
}
