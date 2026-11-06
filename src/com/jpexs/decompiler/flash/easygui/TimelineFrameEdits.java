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
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.converters.PlaceObjectTypeConverter;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    static final int MAX_FRAME = 0xfffe;

    enum KeyEdit {
        INSERT, INSERT_BLANK, CLEAR, CONVERT, CONVERT_BLANK, CLEAR_FRAMES
    }

    private final Timeline timeline;
    private final Timelined timelined;
    private final TimelineBlankFrames blankFrames;
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
        this(timeline, new TimelineBlankFrames());
    }

    TimelineFrameEdits(Timeline timeline, TimelineBlankFrames blankFrames) {
        this.timeline = timeline;
        this.blankFrames = blankFrames.copy();
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

    void editMask(int depth, int first, int last, int clipDepth) {
        NavigableMap<Integer, List<Tag>> original = getCommands(depth);
        NavigableMap<Integer, List<Tag>> edited = new TreeMap<>(original);
        DepthState previous = placedAt(first - 1, depth);
        int previousClip = previous == null ? -1 : previous.clipDepth;
        for (int frame = first; frame <= last + 1 && frame < originalFrameCount; frame++) {
            DepthState state = placedAt(frame, depth);
            int desired = state == null ? -1 : frame <= last ? clipDepth : state.clipDepth;
            if (state != null) {
                List<Tag> commands = original.getOrDefault(frame, Collections.emptyList());
                boolean sameInstance = previous != null && previous.instanceId == state.instanceId;
                boolean inheritsClip = sameInstance
                        && commands.stream().noneMatch(tag -> TimelineDepthMove.clipDepthOf(tag) >= 0);
                int effective = inheritsClip ? previousClip : state.clipDepth;
                if (desired != effective) {
                    if (desired >= 0) {
                        setMaskAtFrame(frame, depth, desired, state, commands);
                    } else if (!sameInstance || previousClip < 0) {
                        for (Tag command : commands) {
                            if (TimelineDepthMove.clipDepthOf(command) >= 0) {
                                replaceClipDepth((PlaceObjectTypeTag) command, -1);
                            }
                        }
                    } else {
                        // A Move with no clip depth inherits the mask. Re-place the current state to clear it.
                        DepthState unmasked = new DepthState(state, state.frame, state.placeFrame, true);
                        unmasked.clipDepth = -1;
                        unmasked.hasImage = state.hasImage;
                        writeState(edited, depth, frame, previous, unmasked);
                    }
                }
            }
            previous = state;
            previousClip = desired;
        }
        recordChanges(original, edited);
    }

    private void setMaskAtFrame(int frame, int depth, int clipDepth, DepthState state, List<Tag> commands) {
        PlaceObjectTypeTag place = state.placeObjectTag;
        if (place == null || !commands.contains(place)) {
            PlaceObjectTypeTag move;
            if (state.isVisible) {
                move = new PlaceObject2Tag(timelined.getSwf(), true, depth, -1, null, null, -1, null, clipDepth, null);
            } else {
                PlaceObject3Tag invisible = new PlaceObject3Tag(timelined.getSwf());
                invisible.placeFlagMove = true;
                invisible.depth = depth;
                invisible.placeFlagHasClipDepth = true;
                invisible.clipDepth = clipDepth;
                invisible.placeFlagHasVisible = true;
                invisible.visible = 0;
                move = invisible;
            }
            move.setTimelined(timelined);
            addToFrame(frame, move);
            return;
        }
        replaceClipDepth(place, clipDepth);
    }

    private void replaceClipDepth(PlaceObjectTypeTag place, int clipDepth) {
        PlaceObjectTypeTag replacement = new PlaceObjectTypeConverter().convertTagType(place, timelined.getSwf(), Math.max(2, place.getPlaceObjectNum()), false);
        replacement.setTimelined(timelined);
        TimelineDepthMove.setClipDepth(replacement, clipDepth);
        boolean hasClipDepth = clipDepth >= 0;
        if (replacement instanceof PlaceObject2Tag) {
            ((PlaceObject2Tag) replacement).placeFlagHasClipDepth = hasClipDepth;
        } else if (replacement instanceof PlaceObject3Tag) {
            ((PlaceObject3Tag) replacement).placeFlagHasClipDepth = hasClipDepth;
        } else {
            ((PlaceObject4Tag) replacement).placeFlagHasClipDepth = hasClipDepth;
        }
        int index = indices.get(place);
        removals.add(index);
        insertions.computeIfAbsent(index, key -> new ArrayList<>()).add(replacement);
    }

    void insertFrames(Set<Point> cells) {
        insertFrames(cells, blankFrames, Collections.emptySet());
    }

    private void insertFrames(Set<Point> cells, TimelineBlankFrames blanks, Set<Integer> endedDepths) {
        Map<Integer, Integer> lastFrames = new TreeMap<>(timeline.getDepthMaxFrame());
        for (Map.Entry<Integer, NavigableSet<Integer>> entry : groupByDepth(cells).entrySet()) {
            int depth = entry.getKey();
            int last = lastFrames.getOrDefault(depth, -1);
            NavigableSet<Integer> selection = new TreeSet<>(entry.getValue());
            if (blanks.end(depth) > last || last < 0) {
                for (int frame : new ArrayList<>(selection.tailSet(last, false).descendingSet())) {
                    if (frame >= blanks.end(depth)) {
                        blanks.extend(depth, Math.max(frame, blanks.end(depth) + 1));
                    } else {
                        blanks.insert(depth, frame + 1, 1);
                    }
                    selection.remove(frame);
                }
            }
            if (selection.isEmpty() || !isPlaced(timeline.getDepthState(last, depth))) {
                continue;
            }
            NavigableMap<Integer, List<Tag>> original = getCommands(depth);
            NavigableMap<Integer, List<Tag>> edited = new TreeMap<>(original);
            for (int frame : selection.headSet(originalFrameCount, false).descendingSet()) {
                int start = Math.min(frame, last) + 1;
                int count = Math.max(1, frame - last);
                shiftCommandsRight(edited, start, count);
                blanks.insert(depth, start, count);
                last += count;
            }
            int requestedEnd = selection.last();
            if (requestedEnd >= originalFrameCount && requestedEnd > last) {
                // Move the commands after the last occupied frame so the existing placement holds.
                shiftCommandsRight(edited, last + 1, requestedEnd - last);
                blanks.insert(depth, last + 1, requestedEnd - last);
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
                if (last >= originalFrameCount - 1 && last + 1 < frameCount && !endedDepths.contains(depth)) {
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
        while (frameCount > 1 && canTrimLastFrame(frameCount - 1, cells)) {
            frameCount--;
            ShowFrameTag showFrame = timeline.getFrame(frameCount).showFrameTag;
            if (showFrame != null) {
                removals.add(indices.get(showFrame));
            }
        }
        for (Map.Entry<Integer, NavigableSet<Integer>> entry : groupByDepth(cells).entrySet()) {
            int depth = entry.getKey();
            for (int frame : entry.getValue().descendingSet()) {
                blankFrames.remove(depth, frame);
            }
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
            edited.tailMap(frameCount, true).clear();
            recordChanges(original, edited);
        }
    }

    private boolean canTrimLastFrame(int frame, Set<Point> cells) {
        if (blankFrames.lastFrame() >= frame) {
            return false;
        }
        Frame last = timeline.getFrame(frame);
        boolean hasPlacement = false;
        for (Map.Entry<Integer, DepthState> layer : last.layers.entrySet()) {
            if (isPlaced(layer.getValue())) {
                if (!cells.contains(new Point(frame, layer.getKey()))) {
                    return false;
                }
                hasPlacement = true;
            }
        }
        return hasPlacement && last.allInnerTags.stream().allMatch(tag ->
                tag == last.showFrameTag || cells.contains(new Point(frame, getCommandDepth(tag))));
    }

    TimelineBlankFrames getBlankFrames() {
        return blankFrames;
    }

    boolean fitsSwf() {
        return frameCount <= MAX_FRAME + 1 && blankFrames.lastFrame() <= MAX_FRAME;
    }

    void editKeyframes(Set<Point> cells, KeyEdit action) {
        Set<Point> occupiedExtensions = new HashSet<>();
        Set<Integer> endedDepths = new HashSet<>();
        for (Map.Entry<Integer, NavigableSet<Integer>> layer : groupByDepth(cells).entrySet()) {
            int depth = layer.getKey();
            NavigableSet<Integer> selected = layer.getValue();
            int occupiedEnd = timeline.getDepthMaxFrame().getOrDefault(depth, -1);
            int blankEnd = blankFrames.end(depth);
            NavigableSet<Integer> targets = keyTargets(selected, action);
            if (action == KeyEdit.CLEAR_FRAMES) {
                clearFrames(depth, selected, occupiedEnd);
            } else if (action == KeyEdit.INSERT || action == KeyEdit.CONVERT) {
                DepthState tail = action == KeyEdit.INSERT && blankEnd <= occupiedEnd && targets.last() > occupiedEnd
                        ? placedAt(occupiedEnd, depth) : null;
                for (int frame : targets) {
                    addKey(depth, frame, frame > occupiedEnd ? tail : null);
                }
                if (tail != null) {
                    occupiedExtensions.add(new Point(targets.last(), depth));
                }
            } else {
                int end = Math.max(Math.max(occupiedEnd, blankEnd), selected.last());
                boolean lastPlacementReplaced = editKeySpans(depth, targets, end, action, occupiedEnd);
                if (action == KeyEdit.INSERT_BLANK && occupiedEnd >= 0) {
                    if (lastPlacementReplaced) {
                        endedDepths.add(depth);
                    } else if (blankEnd <= occupiedEnd) {
                        Integer blankKey = targets.higher(occupiedEnd);
                        if (blankKey != null && blankKey > occupiedEnd + 1) {
                            occupiedExtensions.add(new Point(blankKey - 1, depth));
                        }
                    }
                }
            }
        }
        if (!occupiedExtensions.isEmpty()) {
            // Extend the old placement while keeping the new keys at their requested frames.
            insertFrames(occupiedExtensions, new TimelineBlankFrames(), endedDepths);
        }
    }

    private void clearFrames(int depth, NavigableSet<Integer> selected, int occupiedEnd) {
        NavigableMap<Integer, DepthState> replacement = new TreeMap<>();
        for (int frame : selected) {
            if (placedAt(frame, depth) != null) {
                if (replacement.containsKey(frame - 1)) {
                    blankFrames.clearKey(depth, frame);
                    blankFrames.extend(depth, frame);
                } else {
                    blankFrames.addKey(depth, frame);
                }
                replacement.put(frame, null);
            }
        }
        rewriteSpans(depth, replacement, Math.max(occupiedEnd, blankFrames.end(depth)));
    }

    static NavigableSet<Integer> keyTargets(NavigableSet<Integer> selected, KeyEdit action) {
        if (action != KeyEdit.INSERT && action != KeyEdit.INSERT_BLANK) {
            return selected;
        }
        NavigableSet<Integer> starts = new TreeSet<>();
        for (int frame : selected) {
            if (!selected.contains(frame - 1)) {
                starts.add(frame);
            }
        }
        return starts;
    }

    private void addKey(int depth, int frame, DepthState tail) {
        DepthState state = placedAt(frame, depth);
        if (state != null && state.key) {
            return;
        }
        if (state == null) {
            state = tail;
        }
        if (state == null) {
            blankFrames.addKey(depth, frame);
            return;
        }
        boolean move = timelined.getSwf().version >= 3;
        if (!move) {
            addToFrame(frame, makeRemove(timelined, state, depth));
        }
        addToFrame(frame, makePlacement(timelined, state, depth, move));
    }

    private boolean editKeySpans(int depth, NavigableSet<Integer> selected, int end, KeyEdit action, int occupiedEnd) {
        NavigableSet<Integer> keys = new TreeSet<>(blankFrames.keys(depth));
        for (int frame = 0; frame < originalFrameCount && frame <= end + 1; frame++) {
            DepthState state = placedAt(frame, depth);
            if (state != null ? state.key : isRemovalFrame(frame, depth)) {
                keys.add(frame);
            } else if (state != null) {
                keys.remove(frame);
            }
        }
        NavigableMap<Integer, DepthState> replacement = new TreeMap<>();
        for (int frame : selected) {
            if (action == KeyEdit.CLEAR && !keys.contains(frame)) {
                continue;
            }
            Integer nextKey = keys.higher(frame);
            int until = nextKey == null ? end + 1 : nextKey;
            if (action == KeyEdit.CLEAR && nextKey == null) {
                until = Math.max(until, originalFrameCount);
            }
            DepthState state = null;
            if (action == KeyEdit.CLEAR) {
                state = replacement.containsKey(frame - 1) ? replacement.get(frame - 1) : placedAt(frame - 1, depth);
                blankFrames.clearKey(depth, frame);
            } else {
                blankFrames.addKey(depth, frame);
                blankFrames.extend(depth, until - 1);
            }
            for (int index = frame; index < until && index < originalFrameCount; index++) {
                replacement.put(index, state);
            }
        }
        rewriteSpans(depth, replacement, Math.max(occupiedEnd, blankFrames.end(depth)));
        return replacement.containsKey(occupiedEnd);
    }

    private void rewriteSpans(int depth, NavigableMap<Integer, DepthState> replacement, int layerEnd) {
        if (replacement.isEmpty()) {
            return;
        }
        NavigableMap<Integer, List<Tag>> original = getCommands(depth);
        NavigableMap<Integer, List<Tag>> edited = new TreeMap<>(original);
        for (Map.Entry<Integer, DepthState> entry : replacement.entrySet()) {
            int frame = entry.getKey();
            DepthState originalPrevious = placedAt(frame - 1, depth);
            DepthState previous = replacement.containsKey(frame - 1) ? replacement.get(frame - 1) : originalPrevious;
            if (previous != originalPrevious || entry.getValue() != null || placedAt(frame, depth) != null) {
                writeState(edited, depth, frame, previous, entry.getValue());
            }
            int next = frame + 1;
            if (next < originalFrameCount && !replacement.containsKey(next) && entry.getValue() != placedAt(frame, depth)) {
                // A following Move or hold can depend on the placement that the edit just removed.
                DepthState nextState = placedAt(next, depth);
                if (next <= layerEnd && nextState == null && entry.getValue() == null && isRemovalFrame(next, depth)) {
                    blankFrames.addKey(depth, next);
                }
                writeState(edited, depth, next, entry.getValue(), nextState);
            }
        }
        recordChanges(original, edited);
    }

    private void writeState(NavigableMap<Integer, List<Tag>> commands, int depth, int frame,
            DepthState previous, DepthState state) {
        commands.remove(frame);
        if (previous == state) {
            return;
        }
        List<Tag> tags = new ArrayList<>();
        if (previous != null) {
            tags.add(makeRemove(timelined, previous, depth));
        }
        if (state != null) {
            tags.add(makePlacement(timelined, state, depth, false));
        }
        commands.put(frame, tags);
    }

    private DepthState placedAt(int frame, int depth) {
        DepthState state = timeline.getDepthState(frame, depth);
        return isPlaced(state) ? state : null;
    }

    private boolean isRemovalFrame(int frame, int depth) {
        return frame < originalFrameCount && placedAt(frame - 1, depth) != null;
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

    void endPlacementBeforeFrame(int frame, RemoveTag removal) {
        for (Tag tag : timeline.getFrame(frame).allInnerTags) {
            if (getCommandDepth(tag) == removal.getDepth()) {
                if (!(tag instanceof RemoveTag)) {
                    insertions.computeIfAbsent(indices.get(tag), key -> new ArrayList<>()).add(removal);
                }
                return;
            }
        }
        addToFrame(frame, removal);
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
                if (timelined.getTimeline().getFrameCount() < frameCount) {
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

    static Map<Integer, NavigableSet<Integer>> groupByDepth(Set<Point> cells) {
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
        placement.hasImage = state.hasImage;
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
