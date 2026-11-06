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
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.TimelineFrameEdits.KeyEdit;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class TimelineFrameEditsTest {

    @Test
    public void removingAnAppendedKeyShortensTheTimeline() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        endFrame(swf);

        TimelineFrameEdits append = new TimelineFrameEdits(swf.getTimeline());
        append.editKeyframes(cells(1), KeyEdit.INSERT);
        append.apply();
        assertTrue(swf.getTimeline().getDepthState(1, 1).key);

        TimelineFrameEdits remove = new TimelineFrameEdits(swf.getTimeline());
        remove.removeFrames(cells(1));
        remove.apply();
        assertEquals(swf.getTimeline().getFrameCount(), 1);
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, character);
        assertEquals(reopen(swf).getTimeline().getFrameCount(), 1);
    }

    @Test
    public void removingALastLayerKeyKeepsTheSharedFrameWithoutAnExtraLayerKey() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        place(swf, 2, character, 0, false);
        endFrame(swf);
        place(swf, 1, character, 100, true);
        endFrame(swf);

        TimelineFrameEdits remove = new TimelineFrameEdits(swf.getTimeline());
        remove.removeFrames(cells(1));
        remove.apply();
        assertEquals(swf.getTimeline().getFrameCount(), 2);
        assertEquals(swf.getTimeline().getDepthState(1, 2).characterId, character);
        assertNull(swf.getTimeline().getDepthState(1, 1));

        SwingUtilities.invokeAndWait(() -> {
            TimelineBodyPanel body = new TimelineBodyPanel(null, new UndoManager());
            body.setTimeline(swf.getTimeline());
            body.frameSelect(1, 1, false);
            JPopupMenu menu = body.createFrameMenu();
            assertFalse(menu.getComponent(1).isEnabled(), "No layer frame remains to remove");
            assertFalse(menu.getComponent(5).isEnabled(), "The layer termination is not an editable key");
        });
    }

    @Test
    public void removingTheLastKeyPreservesATrailingFrameLabel() {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        endFrame(swf);
        FrameLabelTag label = new FrameLabelTag(swf);
        swf.addTag(label);
        place(swf, 1, character, 100, true);
        endFrame(swf);

        TimelineFrameEdits remove = new TimelineFrameEdits(swf.getTimeline());
        remove.removeFrames(cells(1));
        remove.apply();
        assertEquals(swf.getTimeline().getFrameCount(), 2);
        assertTrue(swf.getTimeline().getFrame(1).labels.contains(label.name));
        assertNull(swf.getTimeline().getDepthState(1, 1));
    }

    @Test
    public void removingReplacementKeyPreservesBlankFramesAndImplicitFrameLabel() {
        SWF swf = new SWF();
        int first = shape(swf);
        int replacement = shape(swf);
        place(swf, 1, first, 0, false);
        endFrame(swf);
        place(swf, 1, replacement, 100, true);
        endFrame(swf);
        remove(swf, 1);
        endFrame(swf);
        swf.addTag(new FrameLabelTag(swf));

        TimelineFrameEdits edits = new TimelineFrameEdits(swf.getTimeline());
        edits.removeFrames(new HashSet<>(Arrays.asList(new Point(1, 1), new Point(4, 1))));
        edits.apply();

        assertEquals(swf.getFrameCount(), 4);
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, first);
        for (int frame = 1; frame < 4; frame++) {
            assertNull(swf.getTimeline().getDepthState(frame, 1));
        }
        assertEquals(swf.getTimeline().getFrame(3).labels.size(), 1);
    }

    @Test
    public void extendingPastTimelineHoldsEachLayerUntilItsSelectedEnd() {
        SWF swf = new SWF();
        int first = shape(swf);
        int second = shape(swf);
        for (int frame = 1; frame < 66; frame++) {
            endFrame(swf);
        }
        place(swf, 1, second, 0, false);
        place(swf, 2, second, 0, false);
        place(swf, 3, first, 50, false);
        endFrame(swf);
        remove(swf, 3);
        endFrame(swf);

        TimelineFrameEdits edits = new TimelineFrameEdits(swf.getTimeline());
        edits.insertFrames(new HashSet<>(Arrays.asList(new Point(79, 3), new Point(74, 2))));
        edits.apply();

        assertEquals(swf.getFrameCount(), 80);
        assertTrue(swf.getTimeline().getDepthState(65, 3).key);
        for (int frame = 66; frame < 80; frame++) {
            DepthState held = swf.getTimeline().getDepthState(frame, 3);
            assertEquals(held.characterId, first);
            assertEquals(held.matrix.translateX, 50);
            assertFalse(held.key);
        }
        assertEquals(swf.getTimeline().getDepthState(66, 1).characterId, second);
        assertNull(swf.getTimeline().getDepthState(67, 1));
        assertEquals(swf.getTimeline().getDepthState(74, 2).characterId, second);
        assertNull(swf.getTimeline().getDepthState(75, 2));
    }

    @Test
    public void keyPlacementUsesTheCompleteStateAfterAMove() {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        endFrame(swf);
        place(swf, 1, -1, 100, true);
        endFrame(swf);
        endFrame(swf);
        remove(swf, 1);
        endFrame(swf);
        endFrame(swf);

        DepthState moved = swf.getTimeline().getDepthState(2, 1);
        TimelineFrameEdits edits = new TimelineFrameEdits(swf.getTimeline());
        edits.addToFrame(2, TimelineFrameEdits.makePlacement(swf, moved, 1, true));
        edits.addToFrame(4, TimelineFrameEdits.makePlacement(swf, moved, 1, false));
        edits.apply();

        for (int frame : new int[]{2, 4}) {
            DepthState key = swf.getTimeline().getDepthState(frame, 1);
            assertTrue(key.key);
            assertEquals(key.characterId, character);
            assertEquals(key.matrix.translateX, 100);
        }
        assertNull(swf.getTimeline().getDepthState(3, 1));
    }

    @Test
    public void blankKeyClearsItsSpanAndClearKeyRestoresThePreviousContent() throws Exception {
        SWF swf = new SWF();
        int first = shape(swf);
        int next = shape(swf);
        place(swf, 1, first, 0, false);
        place(swf, 2, first, 0, false);
        endFrame(swf);
        place(swf, 1, -1, 100, true);
        endFrame(swf);
        endFrame(swf);
        place(swf, 1, next, 200, true);
        endFrame(swf);

        TimelineFrameEdits blank = new TimelineFrameEdits(swf.getTimeline());
        blank.editKeyframes(cells(1), KeyEdit.INSERT_BLANK);
        blank.apply();
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, first);
        assertNull(swf.getTimeline().getDepthState(1, 1));
        assertNull(swf.getTimeline().getDepthState(2, 1));
        assertEquals(swf.getTimeline().getDepthState(3, 1).characterId, next);
        assertEquals(swf.getTimeline().getDepthState(2, 2).characterId, first);
        SWF reopened = reopen(swf);
        assertNull(reopened.getTimeline().getDepthState(1, 1));
        assertEquals(reopened.getTimeline().getDepthState(3, 1).characterId, next);

        TimelineFrameEdits clear = new TimelineFrameEdits(swf.getTimeline(), blank.getBlankFrames());
        clear.editKeyframes(cells(1), KeyEdit.CLEAR);
        clear.apply();
        assertEquals(swf.getTimeline().getDepthState(2, 1).characterId, first);
        assertEquals(swf.getTimeline().getDepthState(2, 1).matrix.translateX, 0);
        assertFalse(swf.getTimeline().getDepthState(1, 1).key);
        assertEquals(swf.getTimeline().getDepthState(3, 1).characterId, next);
        assertFalse(clear.getBlankFrames().hasKey(1, 1));
    }

    @Test
    public void clearingFramesPreservesTheUnselectedFramesAndFrameCount() throws Exception {
        SWF swf = new SWF();
        int original = shape(swf);
        int next = shape(swf);
        place(swf, 1, original, 0, false);
        place(swf, 2, original, 0, false);
        endFrame(swf);
        endFrame(swf);
        place(swf, 1, -1, 75, true);
        FrameLabelTag label = new FrameLabelTag(swf);
        label.name = "kept";
        swf.addTag(label);
        endFrame(swf);
        endFrame(swf);
        place(swf, 1, next, 120, true);
        endFrame(swf);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(cells(1, 2, 4), KeyEdit.CLEAR_FRAMES);
        edit.apply();

        SWF saved = reopen(swf);
        assertEquals(saved.getTimeline().getFrameCount(), 6);
        assertEquals(saved.getTimeline().getDepthState(0, 1).characterId, original);
        assertNull(saved.getTimeline().getDepthState(1, 1));
        assertNull(saved.getTimeline().getDepthState(2, 1));
        assertEquals(saved.getTimeline().getDepthState(3, 1).characterId, original);
        assertEquals(saved.getTimeline().getDepthState(3, 1).matrix.translateX, 75);
        assertNull(saved.getTimeline().getDepthState(4, 1));
        assertEquals(saved.getTimeline().getDepthState(5, 1).characterId, next);
        assertEquals(saved.getTimeline().getDepthState(5, 1).matrix.translateX, 120);
        assertEquals(saved.getTimeline().getDepthState(2, 2).characterId, original);
        assertTrue(saved.getTimeline().getFrame(2).labels.contains("kept"));
        assertTrue(edit.getBlankFrames().hasKey(1, 1));
        assertFalse(edit.getBlankFrames().hasKey(1, 2));
        assertTrue(edit.getBlankFrames().hasKey(1, 4));
    }

    @Test
    public void clearingButtonFramesPreservesTheUnselectedStates() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();
        record.buttonStateOver = true;
        record.buttonStateDown = true;
        record.buttonStateHitTest = true;

        TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        edit.editKeyframes(cells(ButtonTag.FRAME_OVER, ButtonTag.FRAME_DOWN), KeyEdit.CLEAR_FRAMES);
        assertEquals(edit.getRecords().size(), 1);
        BUTTONRECORD remaining = edit.getRecords().get(0);
        assertTrue(remaining.buttonStateUp);
        assertFalse(remaining.buttonStateOver);
        assertFalse(remaining.buttonStateDown);
        assertTrue(remaining.buttonStateHitTest);
        assertEquals(edit.getBlankFrames().end(1), ButtonTag.FRAME_DOWN);
        assertTrue(edit.getBlankFrames().hasKey(1, ButtonTag.FRAME_OVER));
        assertFalse(edit.getBlankFrames().hasKey(1, ButtonTag.FRAME_DOWN));
    }

    @Test
    public void convertingABlankKeyStopsAtTheNextOriginalKey() {
        SWF swf = new SWF();
        int character = shape(swf);
        int next = shape(swf);
        place(swf, 1, character, 0, false);
        endFrame(swf);
        place(swf, 1, -1, 75, true);
        endFrame(swf);
        endFrame(swf);
        endFrame(swf);
        place(swf, 1, next, 120, true);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(cells(1), KeyEdit.CONVERT_BLANK);
        edit.apply();
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, character);
        assertNull(swf.getTimeline().getDepthState(1, 1));
        assertNull(swf.getTimeline().getDepthState(2, 1));
        assertNull(swf.getTimeline().getDepthState(3, 1));
        DepthState nextKey = swf.getTimeline().getDepthState(4, 1);
        assertEquals(nextKey.characterId, next);
        assertEquals(nextKey.matrix.translateX, 120);
        assertTrue(nextKey.key);
        assertTrue(edit.getBlankFrames().hasKey(1, 1));
        assertFalse(edit.getBlankFrames().hasKey(1, 2));
        assertFalse(edit.getBlankFrames().hasKey(1, 3));
    }

    @Test
    public void convertingTheLastSpanDoesNotAddAKeyAtTheLayerTermination() {
        SWF swf = new SWF();
        place(swf, 1, shape(swf), 0, false);
        endFrame(swf);
        endFrame(swf);
        endFrame(swf);
        remove(swf, 1);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(cells(1), KeyEdit.CONVERT_BLANK);
        edit.apply();
        assertNull(swf.getTimeline().getDepthState(2, 1));
        assertEquals(edit.getBlankFrames().end(1), 2);
        assertTrue(edit.getBlankFrames().hasKey(1, 1));
        assertFalse(edit.getBlankFrames().hasKey(1, 3));
    }

    @Test
    public void insertionStartsAKeySpanAndConversionCreatesAKeyOnEachSelectedFrame() {
        for (KeyEdit action : Arrays.asList(KeyEdit.INSERT, KeyEdit.CONVERT)) {
            SWF swf = new SWF();
            place(swf, 1, shape(swf), 0, false);
            endFrame(swf);
            endFrame(swf);
            endFrame(swf);
            endFrame(swf);

            TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
            edit.editKeyframes(cells(1, 2), action);
            edit.apply();
            assertTrue(swf.getTimeline().getDepthState(1, 1).key);
            assertEquals(swf.getTimeline().getDepthState(2, 1).key, action == KeyEdit.CONVERT);
            assertFalse(swf.getTimeline().getDepthState(3, 1).key);
        }
    }

    @Test
    public void fillingABlankFrameKeepsTheFollowingPlacement() {
        SWF swf = new SWF();
        int newCharacter = shape(swf);
        int existing = shape(swf);
        endFrame(swf);
        place(swf, 1, existing, 120, false);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.addToFrame(0, new PlaceObject2Tag(swf, false, 1, newCharacter, new MATRIX(), null, -1, null, -1, null));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        edit.endPlacementBeforeFrame(1, remove);
        edit.apply();
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, newCharacter);
        assertEquals(swf.getTimeline().getDepthState(1, 1).characterId, existing);
        assertEquals(swf.getTimeline().getDepthState(1, 1).matrix.translateX, 120);
    }

    @Test
    public void stagePlacementFollowsTheSelectedBlockOrFrameRange() throws Exception {
        SWF swf = new SWF();
        int first = shape(swf);
        int next = shape(swf);
        place(swf, 1, first, 0, false);
        endFrame(swf);
        endFrame(swf);
        remove(swf, 1);
        endFrame(swf);
        endFrame(swf);
        endFrame(swf);
        place(swf, 1, next, 0, false);
        endFrame(swf);
        endFrame(swf);

        SwingUtilities.invokeAndWait(() -> {
            TimelineBodyPanel body = new TimelineBodyPanel(null, new UndoManager());
            body.setTimeline(swf.getTimeline());
            body.frameSelect(1, 1, false);
            TimelineBodyPanel.FrameSpan occupied = body.getStagePlacementSpan(1, 1);
            assertEquals(occupied.first, 0);
            assertEquals(occupied.last, 1);

            body.frameSelect(3, 1, false);
            TimelineBodyPanel.FrameSpan blank = body.getStagePlacementSpan(3, 1);
            assertEquals(blank.first, 2);
            assertEquals(blank.last, 4);

            TimelineBodyPanel.FrameSpan beyondLayer = body.getStagePlacementSpan(8, 1);
            assertEquals(beyondLayer.first, 8);
            assertEquals(beyondLayer.last, 8);

            body.frameSelect(1, 1, false);
            body.selectLayerFrames(1);
            TimelineBodyPanel.FrameSpan selection = body.getStagePlacementSpan(1, 1);
            assertEquals(selection.first, 0);
            assertEquals(selection.last, 6);
        });
    }

    @Test
    public void trailingBlankKeyAndFramesCanBeEditedWithoutAddingSwfFrames() throws Exception {
        SWF swf = new SWF();
        place(swf, 1, shape(swf), 0, false);
        endFrame(swf);
        TimelineFrameEdits firstBlank = new TimelineFrameEdits(swf.getTimeline());
        firstBlank.editKeyframes(cells(1), KeyEdit.INSERT_BLANK);
        TimelineFrameEdits key = new TimelineFrameEdits(swf.getTimeline(), firstBlank.getBlankFrames());
        key.editKeyframes(cells(2), KeyEdit.INSERT_BLANK);
        assertTrue(key.isEmpty());
        assertTrue(key.getBlankFrames().hasKey(1, 2));

        TimelineFrameEdits hold = new TimelineFrameEdits(swf.getTimeline(), key.getBlankFrames());
        hold.insertFrames(cells(2));
        assertTrue(hold.isEmpty());
        assertEquals(hold.getBlankFrames().end(1), 3);

        TimelineFrameEdits extend = new TimelineFrameEdits(swf.getTimeline(), hold.getBlankFrames());
        extend.insertFrames(cells(5));
        assertTrue(extend.isEmpty());
        assertEquals(extend.getBlankFrames().end(1), 5);
        assertFalse(extend.getBlankFrames().hasKey(1, 5));

        TimelineFrameEdits insertKey = new TimelineFrameEdits(swf.getTimeline(), extend.getBlankFrames());
        insertKey.editKeyframes(cells(7), KeyEdit.INSERT);
        assertTrue(insertKey.isEmpty());
        assertEquals(insertKey.getBlankFrames().end(1), 7);
        assertTrue(insertKey.getBlankFrames().hasKey(1, 2));
        assertTrue(insertKey.getBlankFrames().hasKey(1, 7));

        TimelineFrameEdits remove = new TimelineFrameEdits(swf.getTimeline(), hold.getBlankFrames());
        remove.removeFrames(cells(1));
        assertTrue(remove.isEmpty());
        assertEquals(remove.getBlankFrames().end(1), 2);
        assertTrue(remove.getBlankFrames().hasKey(1, 1));
        assertEquals(swf.getTimeline().getFrameCount(), 1);
        assertEquals(reopen(swf).getTimeline().getFrameCount(), 1);
    }

    @Test
    public void insertingKeysExtendsOccupiedTailsThroughTheSelectedFrames() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 75, false);
        place(swf, 2, character, 0, false);
        place(swf, 3, character, 0, false);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(new HashSet<>(Arrays.asList(new Point(4, 1), new Point(9, 1), new Point(12, 2))), KeyEdit.INSERT);
        edit.apply();

        SWF saved = reopen(swf);
        assertEquals(saved.getTimeline().getFrameCount(), 13);
        for (int frame = 0; frame <= 9; frame++) {
            DepthState state = saved.getTimeline().getDepthState(frame, 1);
            assertEquals(state.characterId, character);
            assertEquals(state.matrix.translateX, 75);
            assertEquals(state.key, frame == 0 || frame == 4 || frame == 9);
        }
        assertNull(saved.getTimeline().getDepthState(10, 1));
        assertEquals(saved.getTimeline().getDepthState(11, 2).characterId, character);
        assertTrue(saved.getTimeline().getDepthState(12, 2).key);
        assertNull(saved.getTimeline().getDepthState(1, 3));
    }

    @Test
    public void insertingAKeyAfterALayerEndMovesTheRemovalPastTheNewKey() {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        endFrame(swf);
        remove(swf, 1);
        for (int frame = 1; frame <= 10; frame++) {
            endFrame(swf);
        }

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(cells(9), KeyEdit.INSERT);
        edit.apply();
        assertEquals(swf.getTimeline().getFrameCount(), 11);
        assertEquals(swf.getTimeline().getDepthState(8, 1).characterId, character);
        assertTrue(swf.getTimeline().getDepthState(9, 1).key);
        assertNull(swf.getTimeline().getDepthState(10, 1));
    }

    @Test
    public void insertingBlankKeysExtendsOnlyTheSelectedOccupiedTails() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 75, false);
        place(swf, 2, character, 0, false);
        place(swf, 3, character, 0, false);
        endFrame(swf);

        TimelineFrameEdits edit = new TimelineFrameEdits(swf.getTimeline());
        edit.editKeyframes(new HashSet<>(Arrays.asList(new Point(9, 1), new Point(12, 2))), KeyEdit.INSERT_BLANK);
        edit.apply();

        SWF saved = reopen(swf);
        assertEquals(saved.getTimeline().getFrameCount(), 12);
        for (int frame = 0; frame < 9; frame++) {
            DepthState state = saved.getTimeline().getDepthState(frame, 1);
            assertEquals(state.characterId, character);
            assertEquals(state.matrix.translateX, 75);
            assertEquals(state.key, frame == 0);
        }
        assertNull(saved.getTimeline().getDepthState(9, 1));
        assertEquals(saved.getTimeline().getDepthState(11, 2).characterId, character);
        assertNull(saved.getTimeline().getDepthState(1, 3));
        assertTrue(edit.getBlankFrames().hasKey(1, 9));
        assertTrue(edit.getBlankFrames().hasKey(2, 12));
    }

    @Test
    public void insertingAButtonKeyExtendsThePreviousRecordBeforeCreatingTheNewKey() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();

        TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        edit.editKeyframes(cells(ButtonTag.FRAME_HITTEST), KeyEdit.INSERT);
        assertEquals(edit.getRecords().size(), 2);
        BUTTONRECORD extended = edit.getRecords().get(0);
        assertTrue(extended.buttonStateUp);
        assertTrue(extended.buttonStateOver);
        assertTrue(extended.buttonStateDown);
        assertFalse(extended.buttonStateHitTest);
        assertTrue(edit.getRecords().get(1).buttonStateHitTest);
        assertEquals(edit.getRecords().get(1).characterId, record.characterId);
    }

    @Test
    public void insertingABlankButtonKeyExtendsThePreviousRecord() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();

        TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        edit.editKeyframes(cells(ButtonTag.FRAME_HITTEST), KeyEdit.INSERT_BLANK);
        assertEquals(edit.getRecords().size(), 1);
        BUTTONRECORD extended = edit.getRecords().get(0);
        assertTrue(extended.buttonStateUp);
        assertTrue(extended.buttonStateOver);
        assertTrue(extended.buttonStateDown);
        assertFalse(extended.buttonStateHitTest);
        assertTrue(edit.getBlankFrames().hasKey(1, ButtonTag.FRAME_HITTEST));
    }

    @Test
    public void buttonConversionAndClearUseTheFourFixedStates() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();
        record.buttonStateOver = true;
        record.buttonStateDown = true;
        TimelineButtonFrameEdits split = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        split.editKeyframes(cells(ButtonTag.FRAME_OVER, ButtonTag.FRAME_DOWN), KeyEdit.CONVERT);
        assertEquals(split.getRecords().size(), 3);
        button.getRecords().clear();
        button.getRecords().addAll(split.getRecords());

        TimelineButtonFrameEdits clear = new TimelineButtonFrameEdits(button, split.getBlankFrames());
        clear.editKeyframes(cells(ButtonTag.FRAME_OVER), KeyEdit.CLEAR);
        assertEquals(clear.getRecords().size(), 2);
        assertTrue(clear.getRecords().get(0).buttonStateOver);
        assertFalse(clear.getRecords().get(0).buttonStateDown);

        TimelineButtonFrameEdits blank = new TimelineButtonFrameEdits(button, split.getBlankFrames());
        blank.editKeyframes(cells(ButtonTag.FRAME_HITTEST), KeyEdit.INSERT_BLANK);
        assertFalse(blank.changesRecords());
        assertTrue(blank.getBlankFrames().hasKey(1, ButtonTag.FRAME_HITTEST));
    }

    @Test
    public void convertingABlankButtonKeyClearsTheRestOfItsStateSpan() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();
        record.buttonStateOver = true;
        record.buttonStateDown = true;
        BUTTONRECORD next = button.getButtonRecordAt(ButtonTag.FRAME_HITTEST, 1, true);
        next.characterId = shape(swf);
        next.placeMatrix = new MATRIX();

        TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        edit.editKeyframes(cells(ButtonTag.FRAME_OVER), KeyEdit.CONVERT_BLANK);
        assertEquals(edit.getRecords().size(), 2);
        assertTrue(edit.getRecords().get(0).buttonStateUp);
        assertFalse(edit.getRecords().get(0).buttonStateOver);
        assertFalse(edit.getRecords().get(0).buttonStateDown);
        assertTrue(edit.getRecords().get(1).buttonStateHitTest);
        assertEquals(edit.getRecords().get(1).characterId, next.characterId);
        assertTrue(edit.getBlankFrames().hasKey(1, ButtonTag.FRAME_OVER));
        assertFalse(edit.getBlankFrames().hasKey(1, ButtonTag.FRAME_DOWN));
    }

    @Test
    public void insertingButtonStatesAfterAVirtualBlankKeepsTheLayerBlank() {
        SWF swf = new SWF();
        ButtonTag button = new DefineButton2Tag(swf);
        BUTTONRECORD record = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
        record.characterId = shape(swf);
        record.placeMatrix = new MATRIX();

        TimelineButtonFrameEdits blank = new TimelineButtonFrameEdits(button, new TimelineBlankFrames());
        blank.editKeyframes(cells(ButtonTag.FRAME_OVER), KeyEdit.INSERT_BLANK);
        TimelineButtonFrameEdits hold = new TimelineButtonFrameEdits(button, blank.getBlankFrames());
        hold.editFrames(cells(ButtonTag.FRAME_DOWN), true);
        assertFalse(hold.changesRecords());
        assertEquals(hold.getBlankFrames().end(1), ButtonTag.FRAME_DOWN);
        assertFalse(hold.getBlankFrames().hasKey(1, ButtonTag.FRAME_DOWN));

        TimelineButtonFrameEdits key = new TimelineButtonFrameEdits(button, hold.getBlankFrames());
        key.editKeyframes(cells(ButtonTag.FRAME_HITTEST), KeyEdit.INSERT);
        assertFalse(key.changesRecords());
        assertTrue(key.getBlankFrames().hasKey(1, ButtonTag.FRAME_OVER));
        assertTrue(key.getBlankFrames().hasKey(1, ButtonTag.FRAME_HITTEST));
    }

    @Test
    public void menuContainsAllFrameCommandsAndEnablesClearOnAKey() throws Exception {
        SWF swf = new SWF();
        place(swf, 1, shape(swf), 0, false);
        endFrame(swf);
        SwingUtilities.invokeAndWait(() -> {
            TimelineBodyPanel body = new TimelineBodyPanel(null, new UndoManager());
            body.setTimeline(swf.getTimeline());
            body.frameSelect(0, 1, false);
            JPopupMenu menu = body.createFrameMenu();
            List<String> keys = Arrays.asList("insertFrame", "removeFrames", "insertKeyframe", "insertBlankKeyframe",
                    "clearKeyframe", "convertToKeyframes", "convertToBlankKeyframes", "clearFrames");
            for (int i = 0; i < keys.size(); i++) {
                JMenuItem item = (JMenuItem) menu.getComponent(i < 2 ? i : i + 1);
                assertEquals(item.getText(), EasyStrings.translate("action." + keys.get(i)));
                assertTrue(item.isEnabled());
            }
            body.frameSelect(1, 1, false);
            assertFalse(body.createFrameMenu().getComponent(8).isEnabled());
        });
    }

    private static Set<Point> cells(int... frames) {
        Set<Point> cells = new HashSet<>();
        for (int frame : frames) {
            cells.add(new Point(frame, 1));
        }
        return cells;
    }

    private static SWF reopen(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
    }

    @Test
    public void layerLabelsAndFrameBoundsUseTheSameReversedDepthOrder() throws Exception {
        SWF swf = new SWF();
        int character = shape(swf);
        place(swf, 1, character, 0, false);
        place(swf, 5, character, 0, false);
        endFrame(swf);

        SwingUtilities.invokeAndWait(() -> {
            TimelineBodyPanel frames = new TimelineBodyPanel(null, new UndoManager());
            TimelineDepthPanel labels = new TimelineDepthPanel(null);
            frames.setTimeline(swf.getTimeline());
            labels.setTimeline(swf.getTimeline());

            int height = TimelineBodyPanel.FRAME_HEIGHT;
            assertEquals(frames.getDepthBounds(0).y, 0, "Actions stay above the layers");
            assertEquals(frames.getDepthBounds(5).y, height, "The highest depth is the first layer");
            assertEquals(frames.getDepthBounds(1).y, 5 * height, "Depth 1 is the last layer");
            assertEquals(frames.getDepthBounds(6).y, 6 * height, "Spare rows keep their existing depth indexes");

            labels.scroll(height);
            MouseEvent click = new MouseEvent(labels, MouseEvent.MOUSE_PRESSED, 0, 0, 0, height + 1, 1, false);
            assertEquals(labels.getDepthAt(click), 4, "A scrolled layer control resolves to the same depth as its frame");
            assertEquals(frames.getDepthBounds(4).y, 2 * height);
        });
    }

    private static int shape(SWF swf) {
        DefineShapeTag shape = new DefineShapeTag(swf);
        swf.addTag(shape);
        return shape.getCharacterId();
    }

    private static void place(SWF swf, int depth, int character, int x, boolean move) {
        MATRIX matrix = new MATRIX();
        matrix.translateX = x;
        swf.addTag(new PlaceObject2Tag(swf, move, depth, character, matrix, null, -1, null, -1, null));
    }

    private static void remove(SWF swf, int depth) {
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.setDepth(depth);
        swf.addTag(remove);
    }

    private static void endFrame(SWF swf) {
        swf.addTag(new ShowFrameTag(swf));
    }
}
