/*
 *  Copyright (C) 2026 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.DefineBeforeUsageFixer;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Edits the display commands of individual layers while keeping frame-level tags in place.
 */
final class TimelineFrameEdits {

    private final Timeline timeline;
    private final Timelined timelined;
    private final int originalFrameCount;
    private final int originalTagCount;
    private final boolean implicitLastFrame;
    private int frameCount;
    private boolean shiftsCommandsEarlier;
    private final Map<Tag, Integer> indices = new IdentityHashMap<>();
    private final NavigableMap<Integer, List<Tag>> insertions = new TreeMap<>();
    private final NavigableSet<Integer> removals = new TreeSet<>();
    private final Map<Integer, List<Tag>> appendedFrames = new TreeMap<>();

    TimelineFrameEdits(Timeline timeline) {
        this.timeline = timeline;
        timelined = timeline.timelined;
        originalFrameCount = timeline.getFrameCount();
        implicitLastFrame = originalFrameCount > 0 && timeline.getFrame(originalFrameCount - 1).showFrameTag == null;
        frameCount = originalFrameCount;
        int index = 0;
        for (Tag tag : timelined.getTags()) {
            indices.put(tag, index++);
        }
        originalTagCount = index;
    }

    void insertFrames(Set<Point> cells) {
        Map<Integer, Integer> lastFrames = new TreeMap<>(timeline.getDepthMaxFrame());
        for (Map.Entry<Integer, NavigableSet<Integer>> entry : groupByDepth(cells).entrySet()) {
            int depth = entry.getKey();
            int last = lastFrames.getOrDefault(depth, -1);
            if (!isPlaced(timeline.getDepthState(last, depth))) {
                continue;
            }
            NavigableMap<Integer, List<Tag>> original = getCommands(depth);
            NavigableMap<Integer, List<Tag>> edited = new TreeMap<>(original);
            NavigableSet<Integer> selection = entry.getValue();
            for (int frame : selection.headSet(originalFrameCount, false).descendingSet()) {
                int start = Math.min(frame, last) + 1;
                int count = Math.max(1, frame - last);
                shiftCommandsRight(edited, start, count);
                last += count;
            }
            int requestedEnd = selection.last();
            if (requestedEnd >= originalFrameCount && requestedEnd > last) {
                // Move the commands after the last occupied frame so the existing placement holds.
                shiftCommandsRight(edited, last + 1, requestedEnd - last);
                last = requestedEnd;
            }
            lastFrames.put(depth, last);
            frameCount = Math.max(frameCount, last + 1);
            for (Map.Entry<Integer, List<Tag>> commands : edited.tailMap(frameCount, true).entrySet()) {
                if (commands.getValue().stream().anyMatch(tag -> tag instanceof PlaceObjectTypeTag)) {
                    frameCount = commands.getKey() + 1;
                }
            }
            recordChanges(original, edited);
        }
        if (frameCount > originalFrameCount) {
            for (Map.Entry<Integer, Integer> entry : lastFrames.entrySet()) {
                int depth = entry.getKey();
                int last = entry.getValue();
                if (last >= originalFrameCount - 1 && last + 1 < frameCount) {
                    // A layer only extends as far as its own edited content.
                    DepthState state = timeline.getDepthState(timeline.getDepthMaxFrame().get(depth), depth);
                    List<Tag> commands = appendedFrames.get(last + 1);
                    if (isPlaced(state) && (commands == null
                            || commands.stream().noneMatch(tag -> getCommandDepth(tag) == depth))) {
                        addToFrame(last + 1, makeRemove(timelined, state, depth));
                    }
                }
            }
        }
    }

    private static void shiftCommandsRight(NavigableMap<Integer, List<Tag>> commands, int start, int count) {
        for (int frame : new ArrayList<>(commands.tailMap(start, true).descendingKeySet())) {
            commands.put(frame + count, commands.remove(frame));
        }
    }

    void removeFrames(Set<Point> cells) {
        shiftsCommandsEarlier = true;
        for (Map.Entry<Integer, NavigableSet<Integer>> entry : groupByDepth(cells).entrySet()) {
            int depth = entry.getKey();
            NavigableMap<Integer, List<Tag>> original = getCommands(depth);
            if (original.isEmpty()) {
                continue;
            }
            NavigableMap<Integer, List<Tag>> edited = new TreeMap<>(original);
            DepthState last = timeline.getDepthState(originalFrameCount - 1, depth);
            if (isPlaced(last)) {
                edited.put(originalFrameCount, Collections.singletonList(makeRemove(timelined, last, depth)));
            }
            for (int frame : entry.getValue().headSet(originalFrameCount, false).descendingSet()) {
                List<Tag> removed = edited.remove(frame);
                for (int index : new ArrayList<>(edited.tailMap(frame, false).keySet())) {
                    edited.put(index - 1, edited.remove(index));
                }
                if (removed == null) {
                    continue;
                }
                List<Tag> next = edited.getOrDefault(frame, Collections.emptyList());
                if (!next.isEmpty() && next.get(0) instanceof RemoveTag) {
                    // The following frame resets this layer. Retarget its removal to the
                    // object before the deleted frame, which can be a different character.
                    List<Tag> commands = new ArrayList<>(next.subList(1, next.size()));
                    DepthState previous = timeline.getDepthState(frame - 1, depth);
                    if (isPlaced(previous)) {
                        commands.add(0, makeRemove(timelined, previous, depth));
                    }
                    edited.put(frame, commands);
                } else {
                    // A hold or a partial Move can depend on the deleted frame's commands.
                    List<Tag> commands = new ArrayList<>(removed);
                    commands.addAll(next);
                    edited.put(frame, commands);
                }
            }
            edited.tailMap(originalFrameCount, true).clear();
            recordChanges(original, edited);
        }
    }

    void addToFrame(int frame, Tag tag) {
        if (frame >= originalFrameCount) {
            appendedFrames.computeIfAbsent(frame, key -> new ArrayList<>()).add(tag);
        } else {
            ShowFrameTag showFrame = timeline.getFrame(frame).showFrameTag;
            int index = showFrame == null ? originalTagCount : indices.get(showFrame);
            insertions.computeIfAbsent(index, key -> new ArrayList<>()).add(tag);
        }
    }

    void extendTo(int frame) {
        if (frame < frameCount) {
            return;
        }
        Frame last = timeline.getFrame(frameCount - 1);
        if (last != null) {
            for (Map.Entry<Integer, DepthState> layer : last.layers.entrySet()) {
                if (isPlaced(layer.getValue())) {
                    addToFrame(frameCount, makeRemove(timelined, layer.getValue(), layer.getKey()));
                }
            }
        }
        frameCount = frame + 1;
    }

    boolean isEmpty() {
        return insertions.isEmpty() && removals.isEmpty()
                && appendedFrames.isEmpty() && frameCount == originalFrameCount;
    }

    void apply() {
        NavigableSet<Integer> positions = new TreeSet<>(removals);
        positions.addAll(insertions.keySet());
        for (int index : positions.descendingSet()) {
            if (removals.contains(index)) {
                timelined.removeTag(index);
            }
            int position = index;
            for (Tag tag : insertions.getOrDefault(index, Collections.emptyList())) {
                timelined.addTag(position++, tag);
            }
        }
        if (shiftsCommandsEarlier && timelined instanceof SWF) {
            new DefineBeforeUsageFixer().fixDefineBeforeUsage((SWF) timelined);
        }
        if (implicitLastFrame) {
            if (frameCount > originalFrameCount) {
                appendShowFrame();
            } else {
                timelined.resetTimeline();
                if (timelined.getTimeline().getFrameCount() < originalFrameCount) {
                    appendShowFrame();
                }
            }
        }
        for (int frame = originalFrameCount; frame < frameCount; frame++) {
            for (Tag tag : appendedFrames.getOrDefault(frame, Collections.emptyList())) {
                timelined.addTag(tag);
            }
            appendShowFrame();
        }
        timelined.resetTimeline();
        timelined.setFrameCount(timelined.getTimeline().getFrameCount());
    }

    private void appendShowFrame() {
        ShowFrameTag tag = new ShowFrameTag(timelined.getSwf());
        tag.setTimelined(timelined);
        timelined.addTag(tag);
    }

    private static Map<Integer, NavigableSet<Integer>> groupByDepth(Set<Point> cells) {
        Map<Integer, NavigableSet<Integer>> result = new TreeMap<>();
        for (Point cell : cells) {
            if (cell.x >= 0 && cell.y > 0) {
                result.computeIfAbsent(cell.y, key -> new TreeSet<>()).add(cell.x);
            }
        }
        return result;
    }

    private NavigableMap<Integer, List<Tag>> getCommands(int depth) {
        NavigableMap<Integer, List<Tag>> commands = new TreeMap<>();
        for (int i = 0; i < originalFrameCount; i++) {
            for (Tag tag : timeline.getFrame(i).allInnerTags) {
                if (getCommandDepth(tag) == depth) {
                    commands.computeIfAbsent(i, key -> new ArrayList<>()).add(tag);
                }
            }
        }
        return commands;
    }

    private void recordChanges(Map<Integer, List<Tag>> original, Map<Integer, List<Tag>> edited) {
        for (Map.Entry<Integer, List<Tag>> frame : original.entrySet()) {
            if (!frame.getValue().equals(edited.get(frame.getKey()))) {
                for (Tag tag : frame.getValue()) {
                    removals.add(indices.get(tag));
                }
            }
        }
        for (Map.Entry<Integer, List<Tag>> frame : edited.entrySet()) {
            if (!frame.getValue().equals(original.get(frame.getKey()))) {
                for (Tag tag : frame.getValue()) {
                    addToFrame(frame.getKey(), tag);
                }
            }
        }
    }

    private static int getCommandDepth(Tag tag) {
        if (tag instanceof PlaceObjectTypeTag) {
            return ((PlaceObjectTypeTag) tag).getDepth();
        }
        if (tag instanceof RemoveTag) {
            return ((RemoveTag) tag).getDepth();
        }
        return -1;
    }

    static boolean isPlaced(DepthState state) {
        return state != null && (state.characterId >= 0 || state.className != null);
    }

    static PlaceObjectTypeTag makePlacement(Timelined target, DepthState state, int depth, boolean move) {
        DepthState placement = new DepthState(state, state.frame, state.placeFrame, true);
        if (move) {
            placement.minPlaceObjectNum = Math.max(2, placement.minPlaceObjectNum);
        }
        PlaceObjectTypeTag tag = placement.toPlaceObjectTag(depth);
        if (tag instanceof PlaceObject4Tag && state.placeObjectTag != null) {
            ((PlaceObject4Tag) tag).amfData = state.placeObjectTag.getAmfData();
        }
        tag.setPlaceFlagMove(move);
        tag.setTimelined(target);
        return tag;
    }

    static RemoveTag makeRemove(Timelined target, DepthState state, int depth) {
        RemoveTag tag;
        if (target.getSwf().version < 3) {
            RemoveObjectTag legacy = new RemoveObjectTag(target.getSwf());
            legacy.setCharacterId(state.characterId);
            tag = legacy;
        } else {
            tag = new RemoveObject2Tag(target.getSwf());
        }
        tag.setDepth(depth);
        tag.setTimelined(target);
        return tag;
    }
}
