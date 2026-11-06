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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

/** Reorders layers without changing the relative order of either group of layers. */
final class TimelineDepthMove {

    enum Result {
        UNCHANGED, VALID, MASK_CONFLICT
    }

    private static final int INVALID_CLIP_DEPTH = -2;
    private final int[] mappedDepths;
    private final BitSet moving;

    private TimelineDepthMove(int[] mappedDepths, BitSet moving) {
        this.mappedDepths = mappedDepths;
        this.moving = moving;
    }

    static TimelineDepthMove forDrop(int count, Set<Integer> depths, int insertionRow) {
        List<Integer> ordered = new ArrayList<>(count);
        List<Integer> dragged = new ArrayList<>(depths.size());
        BitSet moving = new BitSet(count + 1);
        int insertion = 0;
        for (int depth = count; depth > 0; depth--) {
            if (depths.contains(depth)) {
                dragged.add(depth);
                moving.set(depth);
            } else {
                ordered.add(depth);
                if (count - depth < insertionRow) {
                    insertion++;
                }
            }
        }
        ordered.addAll(insertion, dragged);
        int[] mapped = new int[count + 1];
        for (int row = 0; row < count; row++) {
            mapped[ordered.get(row)] = count - row;
        }
        return new TimelineDepthMove(mapped, moving);
    }

    TimelineDepthMove reversed() {
        int[] reversed = new int[mappedDepths.length];
        BitSet reversedMoving = new BitSet(mappedDepths.length);
        for (int depth = 1; depth < mappedDepths.length; depth++) {
            int mapped = mappedDepths[depth];
            reversed[mapped] = depth;
            if (moving.get(depth)) {
                reversedMoving.set(mapped);
            }
        }
        return new TimelineDepthMove(reversed, reversedMoving);
    }

    int mapDepth(int depth) {
        return depth > 0 && depth < mappedDepths.length ? mappedDepths[depth] : depth;
    }

    boolean isIdentity() {
        for (int depth = 1; depth < mappedDepths.length; depth++) {
            if (mappedDepths[depth] != depth) {
                return false;
            }
        }
        return true;
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

        // Both subsequences retain their order, so their endpoints bound the mapped interval.
        int first = maskDepth + 1;
        int mappedFirst = Integer.MAX_VALUE;
        int mappedLast = Integer.MIN_VALUE;
        int firstStationary = moving.nextClearBit(first);
        if (firstStationary <= clipDepth) {
            mappedFirst = mapDepth(firstStationary);
            mappedLast = mapDepth(moving.previousClearBit(clipDepth));
        }
        int firstMoving = moving.nextSetBit(first);
        if (firstMoving >= 0 && firstMoving <= clipDepth) {
            mappedFirst = Math.min(mappedFirst, mapDepth(firstMoving));
            mappedLast = Math.max(mappedLast, mapDepth(moving.previousSetBit(clipDepth)));
        }
        // SWF can only encode a single contiguous interval immediately above the mask.
        return mappedFirst == mappedMask + 1 && mappedLast - mappedFirst == clipDepth - first
                ? mappedLast : INVALID_CLIP_DEPTH;
    }

    static int depthOf(Tag tag) {
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

    static void setDepth(Tag tag, int depth) {
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

    static int clipDepthOf(Tag tag) {
        return tag instanceof PlaceObjectTypeTag ? ((PlaceObjectTypeTag) tag).getClipDepth() : -1;
    }

    static void setClipDepth(Tag tag, int depth) {
        if (tag instanceof PlaceObject2Tag) {
            ((PlaceObject2Tag) tag).clipDepth = depth;
        } else if (tag instanceof PlaceObject3Tag) {
            ((PlaceObject3Tag) tag).clipDepth = depth;
        } else if (tag instanceof PlaceObject4Tag) {
            ((PlaceObject4Tag) tag).clipDepth = depth;
        }
    }
}
