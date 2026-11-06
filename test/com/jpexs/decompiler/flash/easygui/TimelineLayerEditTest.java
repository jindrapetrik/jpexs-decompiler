/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.testng.annotations.Test;

public class TimelineLayerEditTest {

    @Test
    public void insertingInAMaskShiftsPlacementsAndTheClipLimit() {
        SWF swf = new SWF();
        int character = shape(swf);
        PlaceObject2Tag mask = place(swf, character, 1, 3);
        PlaceObject2Tag front = place(swf, character, 3, -1);
        SetTabIndexTag tab = new SetTabIndexTag(swf);
        tab.depth = 3;
        swf.addTag(tab);
        swf.addTag(new ShowFrameTag(swf));

        TimelineLayerEdit edit = new TimelineLayerEdit(swf, 3, true);
        edit.apply(true);
        assertEquals(mask.getClipDepth(), 4);
        assertEquals(front.getDepth(), 4);
        assertEquals(tab.depth, 4);
        assertNull(swf.getTimeline().getDepthState(0, 3));
        edit.apply(false);
        assertEquals(mask.getClipDepth(), 3);
        assertEquals(front.getDepth(), 3);
        assertEquals(tab.depth, 3);
    }

    @Test
    public void removingALayerKeepsFramesAndRestoresTheExactMaskEndOnUndo() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        PlaceObject2Tag mask = place(swf, character, 1, 2);
        PlaceObject2Tag removed = place(swf, character, 2, -1);
        PlaceObject2Tag front = place(swf, character, 3, -1);
        swf.addTag(new ShowFrameTag(swf));
        RemoveObject2Tag removal = new RemoveObject2Tag(swf);
        removal.setDepth(2);
        swf.addTag(removal);
        PlaceObject2Tag implicitPlacement = place(swf, character, 2, -1);
        assertEquals(swf.getTimeline().getFrameCount(), 2);
        int removalIndex = swf.indexOfTag(removal);
        int placementIndex = swf.indexOfTag(removed);

        TimelineLayerEdit edit = new TimelineLayerEdit(swf, 2, false);
        edit.apply(true);
        assertEquals(swf.indexOfTag(removed), -1);
        assertEquals(swf.indexOfTag(removal), -1);
        assertEquals(swf.indexOfTag(implicitPlacement), -1);
        assertEquals(mask.getClipDepth(), 1);
        assertEquals(front.getDepth(), 2);
        assertEquals(roundTrip(swf).getTimeline().getFrameCount(), 2);

        swf.clearModified();
        edit.apply(false);
        assertSame(swf.getTags().get(placementIndex), removed);
        assertSame(swf.getTags().get(removalIndex), removal);
        assertEquals(mask.getClipDepth(), 2);
        assertEquals(front.getDepth(), 3);
        SWF restored = roundTrip(swf);
        assertEquals(restored.getTimeline().getDepthState(0, 1).clipDepth, 2);
        assertEquals(restored.getTimeline().getFrameCount(), 2);
        assertNull(restored.getTimeline().getFrame(1).showFrameTag);
    }

    @Test
    public void buttonLayerRemovalUpdatesAllButtonStates() {
        SWF swf = new SWF();
        int character = shape(swf);
        ButtonTag button = new DefineButton2Tag(swf);
        button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true).characterId = character;
        button.getButtonRecordAt(ButtonTag.FRAME_OVER, 1, true).characterId = character;
        button.getButtonRecordAt(ButtonTag.FRAME_DOWN, 2, true).characterId = character;
        TimelineLayerEdit edit = new TimelineLayerEdit(button, 1, false);
        edit.apply(true);
        assertEquals(button.getRecords().size(), 1);
        assertEquals(button.getRecords().get(0).placeDepth, 1);
        assertTrue(button.getRecords().get(0).buttonStateDown);
        edit.apply(false);
        assertEquals(button.getRecords().size(), 3);
        assertEquals(button.getButtonRecordAt(ButtonTag.FRAME_DOWN, 2, false).characterId, character);
    }

    @Test
    public void emptyLayerOperationsAreUndoableWithoutDirtyingTheSwf() {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, character, 1, -1);
        swf.addTag(new ShowFrameTag(swf));
        swf.clearModified();
        UndoManager history = new UndoManager();
        history.doOperation(operation(new TimelineLayerEdit(swf, 2, true)), swf);
        assertTrue(history.canUndo(swf));
        assertFalse(swf.isModified());
        history.undo(swf);
        assertFalse(swf.isModified());
        history.redo(swf);
        assertFalse(swf.isModified());
        history.doOperation(operation(new TimelineLayerEdit(swf, 1, false)), swf);
        assertTrue(swf.isModified());
        history.undo(swf);
        assertFalse(swf.isModified());
    }

    private static DoableOperation operation(TimelineLayerEdit edit) {
        return new DoableOperation() {
            @Override
            public void doOperation() {
                edit.apply(true);
            }

            @Override
            public void undoOperation() {
                edit.apply(false);
            }

            @Override
            public String getDescription() {
                return "Edit layer";
            }

            @Override
            public boolean modifiesSwf() {
                return edit.modifiesSwf();
            }
        };
    }

    private static PlaceObject2Tag place(SWF swf, int character, int depth, int clip) {
        PlaceObject2Tag tag = new PlaceObject2Tag(swf, false, depth, character, new MATRIX(), null, -1, null, clip, null);
        swf.addTag(tag);
        return tag;
    }

    private static int shape(SWF swf) {
        DefineShapeTag shape = new DefineShapeTag(swf);
        swf.addTag(shape);
        return shape.getCharacterId();
    }

    private static SWF roundTrip(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
    }
}
