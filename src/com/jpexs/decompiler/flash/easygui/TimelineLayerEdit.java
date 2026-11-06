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

import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import java.util.ArrayList;
import java.util.List;

/** Inserts or removes one display-list depth, keeping the commands of other layers in order. */
final class TimelineLayerEdit {

    static final int MAX_DEPTH = 0xffff;
    private final Timelined target;
    private final int frameCount;
    private ShowFrameTag preservedFrame;
    private final List<TagChange> changedTags = new ArrayList<>();
    private final List<Integer> removedIndices = new ArrayList<>();
    private final List<Tag> removedTags = new ArrayList<>();
    private final List<BUTTONRECORD> beforeRecords = new ArrayList<>();
    private final List<BUTTONRECORD> afterRecords = new ArrayList<>();
    private boolean changedRecords;

    TimelineLayerEdit(Timelined target, int depth, boolean inserting) {
        this.target = target;
        frameCount = target.getTimeline().getFrameCount();
        if (target instanceof ButtonTag) {
            for (BUTTONRECORD record : ((ButtonTag) target).getRecords()) {
                beforeRecords.add(new BUTTONRECORD(record));
                int mapped = mapDepth(record.placeDepth, depth, inserting);
                changedRecords |= mapped != record.placeDepth;
                if (inserting || record.placeDepth != depth) {
                    BUTTONRECORD copy = new BUTTONRECORD(record);
                    copy.placeDepth = mapped;
                    afterRecords.add(copy);
                }
            }
        } else {
            int index = 0;
            for (Tag tag : target.getTags()) {
                int oldDepth = TimelineDepthMove.depthOf(tag);
                if (!inserting && oldDepth == depth) {
                    removedIndices.add(index);
                    removedTags.add(tag);
                } else {
                    int oldClip = TimelineDepthMove.clipDepthOf(tag);
                    int newDepth = mapDepth(oldDepth, depth, inserting);
                    int newClip = oldClip < depth ? oldClip : oldClip + (inserting ? 1 : -1);
                    if (newDepth != oldDepth || newClip != oldClip) {
                        changedTags.add(new TagChange(tag, oldDepth, newDepth, oldClip, newClip));
                    }
                }
                index++;
            }
        }
    }

    static int mapDepth(int value, int depth, boolean inserting) {
        if (value < depth) {
            return value;
        }
        return inserting ? value + 1 : value == depth ? 0 : value - 1;
    }

    static boolean canInsert(Timelined target) {
        if (target instanceof ButtonTag) {
            return ((ButtonTag) target).getRecords().stream().noneMatch(record -> record.placeDepth >= MAX_DEPTH);
        }
        for (Tag tag : target.getTags()) {
            if (TimelineDepthMove.depthOf(tag) >= MAX_DEPTH || TimelineDepthMove.clipDepthOf(tag) >= MAX_DEPTH) {
                return false;
            }
        }
        return true;
    }

    boolean modifiesSwf() {
        return changedRecords || !changedTags.isEmpty() || !removedTags.isEmpty();
    }

    void apply(boolean forward) {
        if (!modifiesSwf()) {
            return;
        }
        if (target instanceof ButtonTag) {
            List<BUTTONRECORD> records = ((ButtonTag) target).getRecords();
            records.clear();
            for (BUTTONRECORD record : forward ? afterRecords : beforeRecords) {
                records.add(new BUTTONRECORD(record));
            }
        } else {
            if (forward) {
                for (int i = removedIndices.size() - 1; i >= 0; i--) {
                    target.removeTag(removedIndices.get(i));
                }
            } else {
                if (preservedFrame != null) {
                    target.removeTag(preservedFrame);
                }
                for (int i = 0; i < removedIndices.size(); i++) {
                    Tag tag = removedTags.get(i);
                    tag.setModified(true);
                    target.addTag(removedIndices.get(i), tag);
                }
            }
            for (TagChange change : changedTags) {
                change.apply(forward);
            }
        }
        target.setModified(true);
        target.resetTimeline();
        if (forward && !removedTags.isEmpty() && target.getTimeline().getFrameCount() < frameCount) {
            // Removing the last display command can erase an implicit last frame.
            if (preservedFrame == null) {
                preservedFrame = new ShowFrameTag(target.getSwf());
                preservedFrame.setTimelined(target);
            }
            target.addTag(preservedFrame);
            target.resetTimeline();
        }
    }

    private static final class TagChange {

        private final Tag tag;
        private final int oldDepth;
        private final int newDepth;
        private final int oldClip;
        private final int newClip;

        TagChange(Tag tag, int oldDepth, int newDepth, int oldClip, int newClip) {
            this.tag = tag;
            this.oldDepth = oldDepth;
            this.newDepth = newDepth;
            this.oldClip = oldClip;
            this.newClip = newClip;
        }

        void apply(boolean forward) {
            if (oldDepth != newDepth) {
                TimelineDepthMove.setDepth(tag, forward ? newDepth : oldDepth);
            }
            if (oldClip != newClip) {
                TimelineDepthMove.setClipDepth(tag, forward ? newClip : oldClip);
            }
            tag.setModified(true);
        }
    }
}
