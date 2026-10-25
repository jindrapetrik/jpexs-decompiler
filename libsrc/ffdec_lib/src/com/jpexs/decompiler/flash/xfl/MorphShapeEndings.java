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
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/** Recovers visible morph endpoints compiled into ordinary (possibly merged) shapes. */
final class MorphShapeEndings {
    private static final ColorTransform IDENTITY = new ColorTransform();

    private MorphShapeEndings() {
    }

    static final class EndPlacement extends PlaceObject2Tag {
        EndPlacement(State state) {
            super(state.character.getSwf());
            depth = state.depth;
            characterId = state.character.getCharacterId();
            placeFlagHasCharacter = true;
            placeFlagHasMatrix = true;
            matrix = state.matrix;
            placeFlagHasRatio = true;
            ratio = 65535;
        }
    }

    private static final class State {
        int depth;
        CharacterTag character;
        MATRIX matrix = new MATRIX();
        ColorTransform color;
        boolean neutral = true;
        PlaceObjectTypeTag placement;

        State copy() {
            State result = new State();
            result.depth = depth;
            result.character = character;
            result.matrix = matrix;
            result.color = color;
            result.neutral = neutral;
            result.placement = placement;
            return result;
        }
    }

    static ReadOnlyTagList restore(ReadOnlyTagList tags, MorphShapeSymbols symbols) {
        // Geometry depends on the character, not on its placement or frame.
        Map<ShapeTag, List<List<Paint>>> shapePaints = new IdentityHashMap<>();
        Map<MorphShapeTag, List<Paint>> morphPaints = new IdentityHashMap<>();
        Map<Integer, State> previous = new TreeMap<>();
        Map<Integer, State> current = new TreeMap<>();
        List<Tag> result = new ArrayList<>();
        List<Tag> frame = new ArrayList<>();
        List<Tag> restores = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                State state = current.get(place.getDepth());
                if (state == null || !place.flagMove()) {
                    state = new State();
                    state.depth = place.getDepth();
                    current.put(state.depth, state);
                }
                if (place.getCharacterId() >= 0) {
                    state.character = place.getSwf().getCharacter(place.getCharacterId());
                }
                if (place.getMatrix() != null) {
                    state.matrix = place.getMatrix();
                }
                if (place.getColorTransform() != null) {
                    state.color = place.getColorTransform();
                }
                state.neutral &= place.getInstanceName() == null && place.getClassName() == null
                        && place.getClipActions() == null && place.getClipDepth() <= 0
                        && !place.cacheAsBitmap() && place.getBlendMode() <= 1 && place.isVisible()
                        && place.getBackgroundColor() == null
                        && (place.getAmfData() == null || place.getAmfData().getValue() == null)
                        && (place.getFilters() == null || place.getFilters().isEmpty());
                state.placement = place;
            } else if (tag instanceof RemoveTag) {
                current.remove(((RemoveTag) tag).getDepth());
            }
            frame.add(tag);
            if (!(tag instanceof ShowFrameTag)) {
                continue;
            }
            result.addAll(restores);
            restores.clear();
            Set<Tag> replaced = new HashSet<>();
            Set<Integer> used = new HashSet<>();
            List<Tag> endings = new ArrayList<>();
            for (Tag change : frame) {
                if (!(change instanceof PlaceObjectTypeTag)) {
                    continue;
                }
                PlaceObjectTypeTag place = (PlaceObjectTypeTag) change;
                State end = current.get(place.getDepth());
                if (end == null || end.placement != place || !(end.character instanceof ShapeTag) || !neutral(end)) {
                    continue;
                }
                List<State> candidates = new ArrayList<>();
                for (State candidate : previous.values()) {
                    State now = current.get(candidate.depth);
                    if (candidate.character instanceof MorphShapeTag && !symbols.contains(candidate.character.getCharacterId())
                            && neutral(candidate) && Objects.equals(candidate.matrix, end.matrix)
                            && !used.contains(candidate.depth) && (now == null || now == end)) {
                        candidates.add(candidate);
                    }
                }
                if (candidates.isEmpty()) {
                    continue;
                }
                ShapeTag shape = (ShapeTag) end.character;
                List<List<Paint>> targets = shapePaints.computeIfAbsent(shape, key -> {
                    List<List<Paint>> paints = new ArrayList<>();
                    for (SHAPEWITHSTYLE group : groups(key.getShapes())) {
                        paints.add(paints(key, group, key.getShapeNum()));
                    }
                    return paints;
                });
                List<State> matched = new ArrayList<>();
                boolean complete = true;
                for (List<Paint> target : targets) {
                    if (target.isEmpty()) {
                        continue;
                    }
                    State match = null;
                    for (State candidate : candidates) {
                        if (matched.contains(candidate)) {
                            continue;
                        }
                        MorphShapeTag morph = (MorphShapeTag) candidate.character;
                        List<Paint> endpoint = morphPaints.computeIfAbsent(morph,
                                key -> paints(key, key.getShapeAtRatio(65535), key.getShapeNum() == 1 ? 3 : 4));
                        if (samePaints(target, endpoint)) {
                            if (match != null) {
                                match = null; // Ambiguous identical endpoints: retain the ordinary shape.
                                break;
                            }
                            match = candidate;
                        }
                    }
                    if (match == null) {
                        complete = false;
                        break;
                    }
                    matched.add(match);
                }
                if (!complete || matched.isEmpty()) {
                    continue;
                }
                replaced.add(place);
                for (State match : matched) {
                    endings.add(new EndPlacement(match));
                    used.add(match.depth);
                    RemoveObject2Tag remove = new RemoveObject2Tag(shape.getSwf());
                    remove.depth = match.depth;
                    restores.add(remove);
                }
                // On the following visible frame restore the source display list.
                // This also removes a split endpoint from a depth absent in the SWF.
                PlaceObject2Tag restore = new PlaceObject2Tag(shape.getSwf());
                restore.depth = end.depth;
                restore.characterId = shape.getCharacterId();
                restore.placeFlagHasCharacter = restore.placeFlagHasMatrix = true;
                restore.matrix = end.matrix;
                restores.add(restore);
            }
            for (Tag original : frame) {
                if (original instanceof ShowFrameTag) {
                    result.addAll(endings);
                }
                if (!replaced.contains(original)) {
                    result.add(original);
                }
            }
            previous.clear();
            for (State state : current.values()) {
                previous.put(state.depth, state.copy());
            }
            frame.clear();
        }
        if (frame.stream().anyMatch(tag -> tag instanceof PlaceObjectTypeTag)) {
            result.addAll(restores);
        }
        result.addAll(frame);
        return new ReadOnlyTagList(result);
    }

    private static boolean neutral(State state) {
        return state.neutral && (state.color == null || IDENTITY.equals(state.color));
    }

    private static List<SHAPEWITHSTYLE> groups(SHAPEWITHSTYLE shape) {
        List<SHAPEWITHSTYLE> result = new ArrayList<>();
        SHAPEWITHSTYLE group = new SHAPEWITHSTYLE();
        group.fillStyles = shape.fillStyles;
        group.lineStyles = shape.lineStyles;
        group.shapeRecords = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (SHAPERECORD record : shape.shapeRecords) {
            if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateNewStyles) {
                group.shapeRecords.add(new EndShapeRecord());
                result.add(group);
                StyleChangeRecord style = Helper.deepCopy((StyleChangeRecord) record);
                group = new SHAPEWITHSTYLE();
                group.fillStyles = style.fillStyles;
                group.lineStyles = style.lineStyles;
                group.shapeRecords = new ArrayList<>();
                style.stateNewStyles = false;
                style.moveDeltaX = record.changeX(x);
                style.moveDeltaY = record.changeY(y);
                style.stateMoveTo = true;
                group.shapeRecords.add(style);
            } else {
                group.shapeRecords.add(record);
            }
            x = record.changeX(x);
            y = record.changeY(y);
        }
        result.add(group);
        return result;
    }

    private static final class Paint {
        Object style;
        Area area;

        Paint(Object style, Area area) {
            this.style = style;
            this.area = area;
        }
    }

    private static List<Paint> paints(CharacterTag tag, SHAPEWITHSTYLE shape, int shapeNum) {
        List<Paint> result = new ArrayList<>();
        for (int i = 0; i < shape.fillStyles.fillStyles.length; i++) {
            Area area = new Area(PathExporter.exportFillStyle(ShapeTag.WIND_EVEN_ODD, shapeNum, tag.getSwf(), shape, i + 1));
            FILLSTYLE fill = normalizedFill(shape.fillStyles.fillStyles[i]);
            if (!area.isEmpty() && !(fill.fillStyleType == FILLSTYLE.SOLID && ((RGBA) fill.color).alpha == 0)) {
                result.add(new Paint(fill, area));
            }
        }
        int count = shapeNum >= 4 ? shape.lineStyles.lineStyles2.length : shape.lineStyles.lineStyles.length;
        for (int i = 0; i < count; i++) {
            LINESTYLE2 line = shapeNum >= 4 ? Helper.deepCopy(shape.lineStyles.lineStyles2[i]) : shape.lineStyles.lineStyles[i].toLineStyle2();
            if (!line.hasFillFlag) {
                line.color = new RGBA(line.color);
                if (((RGBA) line.color).alpha == 0) {
                    continue;
                }
            } else {
                line.fillType = normalizedFill(line.fillType);
            }
            Area area = new Area(new BasicStroke(1).createStrokedShape(PathExporter.exportLineStyle(
                    ShapeTag.WIND_EVEN_ODD, shapeNum, tag.getSwf(), shape, i + 1)));
            if (!area.isEmpty()) {
                result.add(new Paint(line, area));
            }
        }
        return result;
    }

    private static FILLSTYLE normalizedFill(FILLSTYLE source) {
        FILLSTYLE result = source.toShapeNum(4);
        result.inShape3 = true;
        if (result.gradient != null) {
            for (com.jpexs.decompiler.flash.types.GRADRECORD record : result.gradient.gradientRecords) {
                record.inShape3 = true;
            }
        }
        return result;
    }

    private static boolean samePaints(List<Paint> a, List<Paint> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i).style, b.get(i).style)) {
                return false;
            }
            Area difference = new Area(a.get(i).area);
            difference.exclusiveOr(b.get(i).area);
            // A compiler may split/rejoin a quadratic at a rounded twip.
            difference.subtract(new Area(new BasicStroke(2).createStrokedShape(a.get(i).area)));
            if (!difference.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
