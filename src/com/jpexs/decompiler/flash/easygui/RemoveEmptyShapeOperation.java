/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Removes an empty definition and its uses without mutating the undo snapshots. */
final class RemoveEmptyShapeOperation implements DoableOperation {

    private final ShapeTag shape;
    private final List<TimelineEdit> edits = new ArrayList<>();
    private final Consumer<Boolean> refresh;

    RemoveEmptyShapeOperation(ShapeTag shape, Consumer<Boolean> refresh) throws IOException, InterruptedException {
        if (!isEmpty(shape)) {
            throw new IllegalArgumentException("The shape still has edges");
        }
        this.shape = shape;
        this.refresh = refresh;
        plan(shape.getSwf());
    }

    static boolean isEmpty(ShapeTag shape) {
        return shape.getShapes().shapeRecords.stream()
                .noneMatch(record -> record instanceof StraightEdgeRecord || record instanceof CurvedEdgeRecord);
    }

    List<Timelined> getTimelines() {
        return edits.stream().map(edit -> edit.target).collect(Collectors.toList());
    }

    private void plan(Timelined target) throws IOException, InterruptedException {
        TimelineEdit edit = new TimelineEdit(target);
        int characterId = shape.getCharacterId();
        if (target instanceof ButtonTag) {
            edit.afterRecords.removeIf(record -> record.characterId == characterId);
        } else {
            Map<Integer, Integer> placed = new HashMap<>();
            int frame = 0;
            for (Tag original : edit.beforeTags) {
                Tag tag = original;
                if (tag instanceof PlaceObjectTypeTag) {
                    PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
                    int previous = placed.getOrDefault(place.getDepth(), -1);
                    if (place.getCharacterId() >= 0) {
                        placed.put(place.getDepth(), place.getCharacterId());
                    }
                    if (placed.getOrDefault(place.getDepth(), -1) == characterId) {
                        continue;
                    }
                    if (previous == characterId) {
                        // A replacement Move can inherit properties from the instance being removed.
                        DepthState state = target.getTimeline().getDepthState(frame, place.getDepth());
                        if (TimelineFrameEdits.isPlaced(state)) {
                            tag = TimelineFrameEdits.makePlacement(target, state, place.getDepth(), false);
                        } else {
                            // Several display commands can replace and remove an object within one frame.
                            PlaceObjectTypeTag replacement = (PlaceObjectTypeTag) place.cloneTag();
                            replacement.setPlaceFlagMove(false);
                            replacement.setTimelined(target);
                            tag = replacement;
                        }
                    }
                } else if (tag instanceof RemoveTag) {
                    Integer removed = placed.remove(((RemoveTag) tag).getDepth());
                    if (removed != null && removed == characterId) {
                        continue;
                    }
                } else if (tag instanceof CharacterIdTag && ((CharacterIdTag) tag).getCharacterId() == characterId) {
                    continue;
                } else if (tag instanceof Timelined) {
                    plan((Timelined) tag);
                } else {
                    Set<Integer> needed = new HashSet<>();
                    tag.getNeededCharacters(needed, new HashSet<>(), shape.getSwf());
                    if (needed.contains(characterId)) {
                        Tag replacement = tag.cloneTag();
                        if (replacement.removeCharacter(characterId)) {
                            replacement.setTimelined(target);
                            tag = replacement;
                        }
                    }
                }
                edit.afterTags.add(tag);
                if (tag instanceof ShowFrameTag) {
                    frame++;
                }
            }
        }
        if (edit.changed()) {
            edits.add(edit);
        }
    }

    @Override
    public void doOperation() {
        apply(true);
    }

    @Override
    public void undoOperation() {
        apply(false);
    }

    private void apply(boolean forward) {
        for (TimelineEdit edit : edits) {
            edit.apply(forward);
        }
        SWF swf = shape.getSwf();
        swf.updateCharacters();
        swf.clearAllCache();
        if (refresh != null) {
            refresh.accept(forward);
        }
    }

    @Override
    public String getDescription() {
        return EasyStrings.translate("action.removeEmptyShape");
    }

    private static final class TimelineEdit {

        private final Timelined target;
        private final boolean modified;
        private final int frameCount;
        private final List<Tag> beforeTags;
        private final List<Tag> afterTags = new ArrayList<>();
        private final List<BUTTONRECORD> beforeRecords;
        private final List<BUTTONRECORD> afterRecords;

        TimelineEdit(Timelined target) {
            this.target = target;
            modified = target.isModified();
            frameCount = target.getFrameCount();
            beforeTags = target.getTags().toArrayList();
            beforeRecords = target instanceof ButtonTag ? new ArrayList<>(((ButtonTag) target).getRecords()) : new ArrayList<>();
            afterRecords = new ArrayList<>(beforeRecords);
        }

        boolean changed() {
            return target instanceof ButtonTag ? !beforeRecords.equals(afterRecords) : !beforeTags.equals(afterTags);
        }

        void apply(boolean forward) {
            if (target instanceof ButtonTag) {
                List<BUTTONRECORD> records = ((ButtonTag) target).getRecords();
                records.clear();
                records.addAll(forward ? afterRecords : beforeRecords);
            } else {
                while (!target.getTags().isEmpty()) {
                    target.removeTag(0);
                }
                for (Tag tag : forward ? afterTags : beforeTags) {
                    target.addTag(tag);
                }
            }
            target.resetTimeline();
            target.setFrameCount(frameCount);
            target.setModified(forward || modified);
        }
    }
}
