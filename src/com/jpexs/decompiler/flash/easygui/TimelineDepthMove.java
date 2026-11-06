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

import com.jpexs.decompiler.flash.tags.GenCommandTag;
import com.jpexs.decompiler.flash.tags.PlaceImagePrivateTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.DepthTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;

/** Moves a layer and shifts the depths of the layers between its old and new positions. */
final class TimelineDepthMove {

    enum Result {
        UNCHANGED, VALID, MASK_CONFLICT
    }

    private static final int INVALID_CLIP_DEPTH = -2;
    private final int source;
    private final int destination;

    TimelineDepthMove(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    TimelineDepthMove reversed() {
        return new TimelineDepthMove(destination, source);
    }

    int mapDepth(int depth) {
        if (depth == source) {
            return destination;
        }
        if (source < depth && depth <= destination) {
            return depth - 1;
        }
        if (destination <= depth && depth < source) {
            return depth + 1;
        }
        return depth;
    }

    Result check(Timelined target) {
        if (target instanceof ButtonTag) {
            return ((ButtonTag) target).getRecords().stream().anyMatch(record -> mapDepth(record.placeDepth) != record.placeDepth)
                    ? Result.VALID : Result.UNCHANGED;
        }
        Result result = Result.UNCHANGED;
        for (Tag tag : target.getTags()) {
            int depth = depthOf(tag);
            int clipDepth = clipDepthOf(tag);
            int mappedClipDepth = mapClipDepth(depth, clipDepth);
            if (mappedClipDepth == INVALID_CLIP_DEPTH) {
                return Result.MASK_CONFLICT;
            }
            if (mapDepth(depth) != depth || mappedClipDepth != clipDepth) {
                result = Result.VALID;
            }
        }
        return result;
    }

    void apply(Timelined target) {
        if (check(target) != Result.VALID) {
            return;
        }
        if (target instanceof ButtonTag) {
            for (BUTTONRECORD record : ((ButtonTag) target).getRecords()) {
                record.placeDepth = mapDepth(record.placeDepth);
            }
        } else {
            for (Tag tag : target.getTags()) {
                int depth = depthOf(tag);
                int clipDepth = clipDepthOf(tag);
                int mappedDepth = mapDepth(depth);
                int mappedClipDepth = mapClipDepth(depth, clipDepth);
                if (mappedDepth != depth) {
                    setDepth(tag, mappedDepth);
                }
                if (mappedClipDepth != clipDepth) {
                    setClipDepth(tag, mappedClipDepth);
                }
                if (mappedDepth != depth || mappedClipDepth != clipDepth) {
                    // The baseline bytes can belong to either side of this edit after saving.
                    tag.setModified(true);
                }
            }
        }
        target.setModified(true);
        target.resetTimeline();
    }

    private int mapClipDepth(int maskDepth, int clipDepth) {
        if (clipDepth < 0) {
            return clipDepth;
        }
        int mappedMask = mapDepth(maskDepth);
        if (clipDepth <= maskDepth) {
            int mappedClip = mapDepth(clipDepth);
            return mappedClip <= mappedMask ? mappedClip : INVALID_CLIP_DEPTH;
        }

        // All depths except the one being moved retain their relative order.
        int first = maskDepth + 1;
        int mappedFirst = mapDepth(first == source && first < clipDepth ? first + 1 : first);
        int mappedLast = mapDepth(clipDepth == source && first < clipDepth ? clipDepth - 1 : clipDepth);
        if (source >= first && source <= clipDepth) {
            mappedFirst = Math.min(mappedFirst, destination);
            mappedLast = Math.max(mappedLast, destination);
        }
        // SWF can only encode a single contiguous interval immediately above the mask.
        return mappedFirst == mappedMask + 1 && mappedLast - mappedFirst == clipDepth - first
                ? mappedLast : INVALID_CLIP_DEPTH;
    }

    private static int depthOf(Tag tag) {
        if (tag instanceof DepthTag) {
            return ((DepthTag) tag).getDepth();
        }
        if (tag instanceof SetTabIndexTag) {
            return ((SetTabIndexTag) tag).depth;
        }
        if (tag instanceof PlaceImagePrivateTag) {
            return ((PlaceImagePrivateTag) tag).depth;
        }
        if (tag instanceof GenCommandTag && !((GenCommandTag) tag).typeGlobal) {
            return ((GenCommandTag) tag).depth;
        }
        return 0;
    }

    private static void setDepth(Tag tag, int depth) {
        if (tag instanceof DepthTag) {
            ((DepthTag) tag).setDepth(depth);
        } else if (tag instanceof SetTabIndexTag) {
            ((SetTabIndexTag) tag).depth = depth;
        } else if (tag instanceof PlaceImagePrivateTag) {
            ((PlaceImagePrivateTag) tag).depth = depth;
        } else if (tag instanceof GenCommandTag) {
            ((GenCommandTag) tag).depth = depth;
        }
    }

    private static int clipDepthOf(Tag tag) {
        return tag instanceof PlaceObjectTypeTag ? ((PlaceObjectTypeTag) tag).getClipDepth() : -1;
    }

    private static void setClipDepth(Tag tag, int depth) {
        if (tag instanceof PlaceObject2Tag) {
            ((PlaceObject2Tag) tag).clipDepth = depth;
        } else if (tag instanceof PlaceObject3Tag) {
            ((PlaceObject3Tag) tag).clipDepth = depth;
        } else if (tag instanceof PlaceObject4Tag) {
            ((PlaceObject4Tag) tag).clipDepth = depth;
        }
    }
}
