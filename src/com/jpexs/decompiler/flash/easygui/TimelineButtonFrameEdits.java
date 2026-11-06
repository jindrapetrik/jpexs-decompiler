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

import com.jpexs.decompiler.flash.easygui.TimelineFrameEdits.KeyEdit;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/** Edits a button layer within the four states defined by SWF. */
final class TimelineButtonFrameEdits {

    private static final int LAST = ButtonTag.FRAME_HITTEST;
    private final List<BUTTONRECORD> records;
    private final TimelineBlankFrames blankFrames;
    private boolean recordsChanged;

    TimelineButtonFrameEdits(ButtonTag button, TimelineBlankFrames blankFrames) {
        records = new ArrayList<>(button.getRecords());
        this.blankFrames = blankFrames.copy();
    }

    List<BUTTONRECORD> getRecords() {
        return records;
    }

    TimelineBlankFrames getBlankFrames() {
        return blankFrames;
    }

    boolean changesRecords() {
        return recordsChanged;
    }

    void editFrames(Set<Point> cells, boolean insert) {
        for (Map.Entry<Integer, NavigableSet<Integer>> layer : TimelineFrameEdits.groupByDepth(cells).entrySet()) {
            int depth = layer.getKey();
            BUTTONRECORD[] original = getStates(depth);
            BUTTONRECORD[] states = original.clone();
            for (int frame : layer.getValue().headSet(LAST, true).descendingSet()) {
                if (insert) {
                    insertFrame(depth, frame, states);
                } else {
                    for (int next = frame; next < LAST; next++) {
                        states[next] = states[next + 1];
                    }
                    states[LAST] = null;
                    blankFrames.remove(depth, frame);
                }
            }
            updateRecords(depth, original, states);
        }
    }

    private void insertFrame(int depth, int frame, BUTTONRECORD[] states) {
        if (states[LAST] != null) {
            return;
        }
        int end = LAST;
        while (end >= 0 && states[end] == null) {
            end--;
        }
        if (frame > end && blankFrames.end(depth) <= end && end >= 0) {
            Arrays.fill(states, end + 1, frame + 1, states[end]);
        } else if (frame < LAST) {
            System.arraycopy(states, frame + 1, states, frame + 2, LAST - frame - 1);
            states[frame + 1] = states[frame];
            blankFrames.insert(depth, frame + 1, 1);
        }
        if (states[frame] == null && frame >= end) {
            blankFrames.extend(depth, frame);
        }
        blankFrames.limit(depth, LAST);
    }

    void editKeyframes(Set<Point> cells, KeyEdit action) {
        for (Map.Entry<Integer, NavigableSet<Integer>> layer : TimelineFrameEdits.groupByDepth(cells).entrySet()) {
            int depth = layer.getKey();
            BUTTONRECORD[] original = getStates(depth);
            BUTTONRECORD[] states = original.clone();
            NavigableSet<Integer> keys = new TreeSet<>();
            for (int frame = 0; frame <= LAST; frame++) {
                if (states[frame] != (frame == 0 ? null : states[frame - 1]) || blankFrames.hasKey(depth, frame)) {
                    keys.add(frame);
                }
            }
            NavigableSet<Integer> selected = layer.getValue().headSet(LAST, true);
            for (int frame : TimelineFrameEdits.keyTargets(selected, action)) {
                Integer next = keys.higher(frame);
                int until = next == null ? LAST + 1 : next;
                BUTTONRECORD previous = frame == 0 ? null : states[frame - 1];
                switch (action) {
                    case INSERT:
                    case CONVERT:
                        if (states[frame] == null) {
                            int last = LAST;
                            while (last >= 0 && states[last] == null) {
                                last--;
                            }
                            if (action == KeyEdit.INSERT && last >= 0 && frame > last && blankFrames.end(depth) <= last) {
                                Arrays.fill(states, last + 1, frame, states[last]);
                                states[frame] = new BUTTONRECORD(states[last]);
                            } else {
                                blankFrames.addKey(depth, frame);
                            }
                        } else if (states[frame] == previous) {
                            Arrays.fill(states, frame, until, new BUTTONRECORD(states[frame]));
                        }
                        break;
                    case CLEAR:
                        if (keys.contains(frame)) {
                            Arrays.fill(states, frame, until, previous);
                            blankFrames.clearKey(depth, frame);
                        }
                        break;
                    case CLEAR_FRAMES:
                        if (states[frame] != null) {
                            states[frame] = null;
                            if (frame > 0 && selected.contains(frame - 1) && original[frame - 1] != null) {
                                blankFrames.clearKey(depth, frame);
                                blankFrames.extend(depth, frame);
                            } else {
                                blankFrames.addKey(depth, frame);
                            }
                        }
                        break;
                    case INSERT_BLANK:
                    case CONVERT_BLANK:
                        if (action == KeyEdit.INSERT_BLANK) {
                            int last = LAST;
                            while (last >= 0 && states[last] == null) {
                                last--;
                            }
                            if (last >= 0 && frame > last + 1 && blankFrames.end(depth) <= last) {
                                Arrays.fill(states, last + 1, frame, states[last]);
                            }
                        }
                        blankFrames.addKey(depth, frame);
                        Arrays.fill(states, frame, until, null);
                        int last = LAST;
                        while (last > frame && original[last] == null) {
                            last--;
                        }
                        blankFrames.extend(depth, Math.min(until - 1, last));
                        break;
                }
            }
            updateRecords(depth, original, states);
        }
    }

    private BUTTONRECORD[] getStates(int depth) {
        BUTTONRECORD[] states = new BUTTONRECORD[LAST + 1];
        for (BUTTONRECORD record : records) {
            if (record.placeDepth == depth) {
                for (int frame = 0; frame <= LAST; frame++) {
                    if (states[frame] == null && record.hasFrame(frame)) {
                        states[frame] = record;
                    }
                }
            }
        }
        return states;
    }

    private void updateRecords(int depth, BUTTONRECORD[] original, BUTTONRECORD[] states) {
        if (Arrays.equals(original, states)) {
            return;
        }
        recordsChanged = true;
        records.removeIf(record -> record.placeDepth == depth);
        Map<BUTTONRECORD, BUTTONRECORD> copies = new IdentityHashMap<>();
        for (int frame = 0; frame <= LAST; frame++) {
            if (states[frame] != null) {
                BUTTONRECORD copy = copies.computeIfAbsent(states[frame], state -> {
                    BUTTONRECORD record = new BUTTONRECORD(state);
                    for (int i = 0; i <= LAST; i++) {
                        record.setFrame(i, false);
                    }
                    records.add(record);
                    return record;
                });
                copy.setFrame(frame, true);
                blankFrames.clearKey(depth, frame);
            }
        }
    }
}
