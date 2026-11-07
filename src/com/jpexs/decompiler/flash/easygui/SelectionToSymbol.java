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
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/** Creates a symbol from the effective display states, including inherited properties. */
final class SelectionToSymbol {

    private SelectionToSymbol() {
    }

    static CharacterTag create(SWF swf, boolean button, Map<Integer, DepthState> selection) {
        CharacterTag symbol = button ? new DefineButton2Tag(swf) : new DefineSpriteTag(swf);
        Timelined target = (Timelined) symbol;
        NavigableMap<Integer, DepthState> ordered = new TreeMap<>(selection);
        int depth = 0;
        for (DepthState state : ordered.values()) {
            DepthState copy = new DepthState(state, state.frame, state.placeFrame, true);
            copy.hasImage = state.hasImage;
            if (copy.clipDepth >= 0) {
                int clipDepth = ordered.headMap(copy.clipDepth, true).size();
                copy.clipDepth = clipDepth > depth + 1 ? clipDepth : -1;
            }
            PlaceObjectTypeTag place = TimelineFrameEdits.makePlacement(target, copy, ++depth, false);
            if (button) {
                BUTTONRECORD record = new BUTTONRECORD(swf, (ButtonTag) symbol);
                record.fromPlaceObject(place);
                for (int frame = ButtonTag.FRAME_UP; frame <= ButtonTag.FRAME_HITTEST; frame++) {
                    record.setFrame(frame, true);
                }
                ((ButtonTag) symbol).getRecords().add(record);
            } else {
                target.addTag(place);
            }
        }
        if (!button) {
            ShowFrameTag showFrame = new ShowFrameTag(swf);
            showFrame.setTimelined(target);
            target.addTag(showFrame);
            target.setFrameCount(1);
            ((DefineSpriteTag) symbol).hasEndTag = true;
        }
        symbol.setModified(true);
        return symbol;
    }

    static TimelineBlankFrames replace(Timelined parent, CharacterTag symbol,
            Map<Integer, TimelineBodyPanel.FrameSpan> spans, TimelineBlankFrames blanks) {
        int topDepth = spans.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
        DepthState state = new DepthState(parent.getSwf(), null, null);
        state.characterId = symbol.getCharacterId();
        state.matrix = new MATRIX();
        state.minPlaceObjectNum = 2;
        if (parent instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) parent;
            TimelineButtonFrameEdits edits = new TimelineButtonFrameEdits(button, blanks);
            BUTTONRECORD record = new BUTTONRECORD(parent.getSwf(), button);
            record.fromPlaceObject(TimelineFrameEdits.makePlacement(parent, state, topDepth, false));
            spans.forEach((depth, span) -> edits.replaceSpan(depth, span.first, span.last, depth == topDepth ? record : null));
            button.getRecords().clear();
            button.getRecords().addAll(edits.getRecords());
            button.resetTimeline();
            return edits.getBlankFrames();
        }
        TimelineFrameEdits edits = new TimelineFrameEdits(parent.getTimeline(), blanks);
        spans.forEach((depth, span) -> edits.replaceSpan(depth, span.first, span.last, depth == topDepth ? state : null));
        edits.apply();
        return edits.getBlankFrames();
    }
}
