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
import static org.testng.Assert.assertNull;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.GenCommandTag;
import com.jpexs.decompiler.flash.tags.PlaceImagePrivateTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.testng.annotations.Test;

public class TimelineDepthMoveTest {

    @Test
    public void movingALayerRewritesItsWholeDisplayListAndDepthReferences() throws Exception {
        SWF swf = new SWF();
        int first = shape(swf);
        int second = shape(swf);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        PlaceObjectTag nested = new PlaceObjectTag(swf, first, 2, new MATRIX(), null);
        sprite.addTag(nested);
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);

        PlaceObjectTag placed = new PlaceObjectTag(swf, first, 2, new MATRIX(), null);
        swf.addTag(placed);
        PlaceObject3Tag other = new PlaceObject3Tag(swf);
        other.depth = 4;
        other.characterId = second;
        other.placeFlagHasCharacter = true;
        swf.addTag(other);
        SetTabIndexTag tab = new SetTabIndexTag(swf);
        tab.depth = 2;
        tab.tabIndex = 7;
        swf.addTag(tab);
        PlaceImagePrivateTag image = new PlaceImagePrivateTag(swf);
        image.depth = 3;
        swf.addTag(image);
        GenCommandTag generator = new GenCommandTag(swf);
        generator.depth = 4;
        generator.typeMovie = true;
        swf.addTag(generator);
        swf.addTag(new ShowFrameTag(swf));

        MATRIX matrix = new MATRIX();
        matrix.translateX = 123;
        PlaceObject2Tag change = new PlaceObject2Tag(swf, true, 2, -1, matrix, null, -1, null, -1, null);
        swf.addTag(change);
        RemoveObject2Tag removed = new RemoveObject2Tag(swf);
        removed.setDepth(4);
        swf.addTag(removed);
        swf.addTag(new ShowFrameTag(swf));
        RemoveObjectTag oldRemoval = new RemoveObjectTag(swf);
        oldRemoval.setCharacterId(first);
        oldRemoval.setDepth(2);
        swf.addTag(oldRemoval);
        swf.addTag(new ShowFrameTag(swf));

        TimelineDepthMove move = new TimelineDepthMove(2, 4);
        assertEquals(move.check(swf), TimelineDepthMove.Result.VALID);
        move.apply(swf);
        assertEquals(placed.getDepth(), 4);
        assertEquals(other.getDepth(), 3);
        assertEquals(change.getDepth(), 4);
        assertEquals(oldRemoval.getDepth(), 4);
        assertEquals(removed.getDepth(), 3);
        assertEquals(tab.depth, 4);
        assertEquals(tab.tabIndex, 7);
        assertEquals(image.depth, 2);
        assertEquals(generator.depth, 3);
        assertEquals(nested.getDepth(), 2);

        SWF saved = roundTrip(swf);
        assertEquals(saved.getTimeline().getDepthState(0, 4).characterId, first);
        assertEquals(saved.getTimeline().getDepthState(0, 3).characterId, second);
        assertEquals(saved.getTimeline().getDepthState(1, 4).matrix.translateX, 123);
        assertNull(saved.getTimeline().getDepthState(1, 3));
        assertNull(saved.getTimeline().getDepthState(2, 4));

        swf.clearModified();
        move.reversed().apply(swf);
        SWF undone = roundTrip(swf);
        assertEquals(undone.getTimeline().getDepthState(1, 2).matrix.translateX, 123);
        assertEquals(undone.getTimeline().getDepthState(0, 4).characterId, second);
        assertNull(undone.getTimeline().getDepthState(2, 2));
    }

    @Test
    public void maskEndpointsMoveWhenTheirRangesRemainRepresentable() {
        SWF swf = new SWF();
        int character = shape(swf);
        PlaceObject2Tag mask = new PlaceObject2Tag(swf, false, 2, character, new MATRIX(), null, -1, null, 5, null);
        swf.addTag(mask);
        PlaceObject4Tag maskUpdate = new PlaceObject4Tag(swf);
        maskUpdate.depth = 2;
        maskUpdate.placeFlagMove = true;
        maskUpdate.placeFlagHasClipDepth = true;
        maskUpdate.clipDepth = 4;
        swf.addTag(maskUpdate);
        PlaceObjectTag covered = new PlaceObjectTag(swf, character, 4, new MATRIX(), null);
        swf.addTag(covered);
        PlaceObjectTag outside = new PlaceObjectTag(swf, character, 6, new MATRIX(), null);
        swf.addTag(outside);

        TimelineDepthMove conflict = new TimelineDepthMove(4, 1);
        assertEquals(conflict.check(swf), TimelineDepthMove.Result.MASK_CONFLICT);
        conflict.apply(swf);
        assertEquals(covered.getDepth(), 4);
        assertEquals(mask.getClipDepth(), 5);
        assertEquals(new TimelineDepthMove(4, 3).check(swf), TimelineDepthMove.Result.VALID);
        assertEquals(new TimelineDepthMove(5, 3).check(swf), TimelineDepthMove.Result.MASK_CONFLICT,
                "A later update can have a shorter masking range");

        TimelineDepthMove valid = new TimelineDepthMove(6, 1);
        valid.apply(swf);
        assertEquals(mask.getDepth(), 3);
        assertEquals(mask.getClipDepth(), 6);
        assertEquals(maskUpdate.getDepth(), 3);
        assertEquals(maskUpdate.getClipDepth(), 5);
        assertEquals(outside.getDepth(), 1);
        valid.reversed().apply(swf);
        assertEquals(mask.getClipDepth(), 5);
        assertEquals(maskUpdate.getClipDepth(), 4);
    }

    @Test
    public void buttonLayersMoveInEveryState() {
        SWF swf = new SWF();
        for (ButtonTag button : new ButtonTag[]{new DefineButtonTag(swf), new DefineButton2Tag(swf)}) {
            BUTTONRECORD shared = button.getButtonRecordAt(ButtonTag.FRAME_UP, 1, true);
            shared.buttonStateDown = true;
            BUTTONRECORD over = button.getButtonRecordAt(ButtonTag.FRAME_OVER, 1, true);
            BUTTONRECORD hit = button.getButtonRecordAt(ButtonTag.FRAME_HITTEST, 3, true);
            TimelineDepthMove move = new TimelineDepthMove(1, 3);
            move.apply(button);
            assertEquals(shared.placeDepth, 3);
            assertEquals(over.placeDepth, 3);
            assertEquals(hit.placeDepth, 2);
            move.reversed().apply(button);
            assertEquals(shared.placeDepth, 1);
            assertEquals(over.placeDepth, 1);
            assertEquals(hit.placeDepth, 3);
        }
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
