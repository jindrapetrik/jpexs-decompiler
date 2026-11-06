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
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class TimelineFrameEditsTest {

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
