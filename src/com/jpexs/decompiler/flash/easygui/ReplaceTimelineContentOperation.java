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
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class ReplaceTimelineContentOperation extends TimelinedTagListDoableOperation {

    private final Timelined timelined;
    private final Runnable refresh;
    private final MATRIX replacementMatrix;
    private final NavigableMap<Integer, TagEdit> edits = new TreeMap<>();
    private List<Tag> originalSwfTags;
    private int originalFrameCount;

    ReplaceTimelineContentOperation(EasySwfPanel swfPanel, Timeline timeline, Set<Point> selection,
            CharacterTag replacement, Runnable refresh) {
        super(swfPanel, timeline.timelined);
        this.timelined = timeline.timelined;
        this.refresh = refresh;
        RECT scene = timelined.getRect();
        RECT bounds = ((BoundedTag) replacement).getRect();
        replacementMatrix = new MATRIX();
        replacementMatrix.translateX = (int) Math.round(((double) scene.Xmin + scene.Xmax - bounds.Xmin - bounds.Xmax) / 2);
        replacementMatrix.translateY = (int) Math.round(((double) scene.Ymin + scene.Ymax - bounds.Ymin - bounds.Ymax) / 2);
        planEdits(timeline, selection, replacement);
    }

    private void planEdits(Timeline timeline, Set<Point> selection, CharacterTag replacement) {
        Map<Integer, Set<Integer>> affectedFrames = new TreeMap<>();
        for (Point cell : selection) {
            affectedFrames.computeIfAbsent(cell.x, key -> new TreeSet<>()).add(cell.y);
            if (cell.x + 1 < timeline.getFrameCount()) {
                affectedFrames.computeIfAbsent(cell.x + 1, key -> new TreeSet<>()).add(cell.y);
            }
        }

        Map<Tag, Integer> tagIndices = new IdentityHashMap<>();
        int tagCount = timelined.getTags().size();
        for (int i = 0; i < tagCount; i++) {
            tagIndices.put(timelined.getTags().get(i), i);
        }

        for (Map.Entry<Integer, Set<Integer>> entry : affectedFrames.entrySet()) {
            int frameIndex = entry.getKey();
            Frame frame = timeline.getFrame(frameIndex);
            Map<Integer, List<Tag>> commands = new TreeMap<>();
            for (Tag tag : frame.allInnerTags) {
                int depth = getCommandDepth(tag);
                if (entry.getValue().contains(depth)) {
                    commands.computeIfAbsent(depth, key -> new ArrayList<>()).add(tag);
                }
            }

            for (int depth : entry.getValue()) {
                boolean replacing = selection.contains(new Point(frameIndex, depth));
                boolean wasReplacing = selection.contains(new Point(frameIndex - 1, depth));
                List<Tag> depthCommands = commands.getOrDefault(depth, Collections.emptyList());

                if (!replacing && !depthCommands.isEmpty() && depthCommands.get(0) instanceof RemoveTag) {
                    // This frame already starts a fresh state at this depth.
                    retargetRemove(depthCommands.get(0), tagIndices, replacement);
                    continue;
                }

                for (Tag tag : depthCommands) {
                    edits.computeIfAbsent(tagIndices.get(tag), key -> new TagEdit()).removeOriginal = true;
                }
                if (replacing && wasReplacing) {
                    // With no commands at this depth, the replacement continues from the previous frame.
                    continue;
                }

                // At the start or end of a selected span, rebuild the depth from the desired state.
                int index = frame.showFrameTag == null ? tagCount : tagIndices.get(frame.showFrameTag);
                if (!depthCommands.isEmpty()) {
                    index = tagIndices.get(depthCommands.get(0));
                }
                TagEdit edit = edits.computeIfAbsent(index, key -> new TagEdit());
                DepthState previous = getPlacedState(timeline, frameIndex - 1, depth);
                if (wasReplacing || previous != null) {
                    int removedId = wasReplacing ? replacement.getCharacterId() : previous.characterId;
                    edit.tags.add(makeRemove(depth, removedId));
                }
                DepthState state = getPlacedState(timeline, frameIndex, depth);
                if (replacing || state != null) {
                    edit.tags.add(makePlacement(depth, state, replacing ? replacement : null));
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

    private static DepthState getPlacedState(Timeline timeline, int frame, int depth) {
        DepthState state = timeline.getDepthState(frame, depth);
        return state != null && (state.characterId >= 0 || state.className != null) ? state : null;
    }

    private Tag makeRemove(int depth, int characterId) {
        SWF swf = timelined.getSwf();
        RemoveTag remove;
        if (swf.version < 3) {
            RemoveObjectTag legacyRemove = new RemoveObjectTag(swf);
            legacyRemove.setCharacterId(characterId);
            remove = legacyRemove;
        } else {
            remove = new RemoveObject2Tag(swf);
        }
        remove.setDepth(depth);
        remove.setTimelined(timelined);
        return remove;
    }

    private Tag makePlacement(int depth, DepthState original, CharacterTag replacement) {
        DepthState state = original == null ? new DepthState(timelined.getSwf(), null, null)
                : new DepthState(original, original.frame, original.placeFrame, true);
        if (replacement != null) {
            state.characterId = replacement.getCharacterId();
            state.className = null;
            state.hasImage = false;
            state.matrix = new MATRIX(replacementMatrix);
            if (!(replacement instanceof MorphShapeTag)) {
                state.ratio = -1;
            }
        }
        PlaceObjectTypeTag place = state.toPlaceObjectTag(depth);
        if (place instanceof PlaceObject4Tag && original != null && original.placeObjectTag != null) {
            ((PlaceObject4Tag) place).amfData = original.placeObjectTag.getAmfData();
        }
        place.setTimelined(timelined);
        return place;
    }

    private void retargetRemove(Tag original, Map<Tag, Integer> tagIndices, CharacterTag replacement) {
        if (original instanceof RemoveObjectTag) {
            RemoveObjectTag remove = new RemoveObjectTag(timelined.getSwf());
            remove.setCharacterId(replacement.getCharacterId());
            remove.setDepth(((RemoveObjectTag) original).getDepth());
            remove.setTimelined(timelined);
            TagEdit edit = edits.computeIfAbsent(tagIndices.get(original), key -> new TagEdit());
            edit.removeOriginal = true;
            edit.tags.add(remove);
        }
    }

    @Override
    public void doOperation() {
        super.doOperation();
        SWF swf = timelined.getSwf();
        originalFrameCount = timelined.getFrameCount();
        int timelineFrameCount = timelined.getTimeline().getFrameCount();
        int rootFrameCount = timelined == swf ? timelineFrameCount : swf.getTimeline().getFrameCount();
        originalSwfTags = timelined == swf ? null : swf.getTags().toArrayList();
        for (Map.Entry<Integer, TagEdit> entry : edits.descendingMap().entrySet()) {
            int index = entry.getKey();
            TagEdit edit = entry.getValue();
            if (edit.removeOriginal) {
                timelined.removeTag(index);
            }
            for (Tag tag : edit.tags) {
                timelined.addTag(index++, tag);
            }
        }

        if (new DefineBeforeUsageFixer().fixDefineBeforeUsage(swf) && timelined != swf) {
            preserveFrameCount(swf, rootFrameCount);
        } else {
            originalSwfTags = null;
        }
        preserveFrameCount(timelined, timelineFrameCount);
        refreshTimeline();
    }

    private static void preserveFrameCount(Timelined target, int frameCount) {
        target.resetTimeline();
        if (target.getTimeline().getFrameCount() < frameCount) {
            // Removing the last placement or moving a definition can empty an implicit final frame.
            ShowFrameTag showFrame = new ShowFrameTag(target.getSwf());
            showFrame.setTimelined(target);
            target.addTag(showFrame);
            target.resetTimeline();
        }
    }

    @Override
    public void undoOperation() {
        super.undoOperation();
        timelined.setFrameCount(originalFrameCount);
        if (originalSwfTags != null) {
            SWF swf = timelined.getSwf();
            for (int i = swf.getTags().size() - 1; i >= 0; i--) {
                swf.removeTag(i);
            }
            for (Tag tag : originalSwfTags) {
                swf.addTag(tag);
            }
            swf.resetTimeline();
        }
        refreshTimeline();
    }

    private void refreshTimeline() {
        timelined.getSwf().clearImageCache();
        refresh.run();
    }

    @Override
    public String getDescription() {
        return EasyStrings.translate("action.replaceSelectedContent");
    }

    private static final class TagEdit {

        private boolean removeOriginal;
        private final List<Tag> tags = new ArrayList<>();
    }
}
