/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.DefineBeforeUsageFixer;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.filters.BLURFILTER;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class SelectionToSymbolTest {

    @DataProvider(name = "symbolTypes")
    public Object[][] symbolTypes() {
        return new Object[][]{{false}, {true}};
    }

    private static SWF scene() {
        SWF swf = new SWF();
        swf.version = 10;
        DefineShape4Tag shape = new DefineShape4Tag(swf);
        swf.addTag(shape);
        place(swf, 2, shape.shapeId, 100, false);
        place(swf, 5, shape.shapeId, 500, false);
        swf.addTag(new ShowFrameTag(swf));
        place(swf, 2, -1, 200, true);
        place(swf, 5, -1, 600, true);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new ShowFrameTag(swf));
        place(swf, 2, -1, 300, true);
        place(swf, 5, -1, 700, true);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 4;
        return swf;
    }

    private static void place(Timelined target, int depth, int character, int x, boolean move) {
        MATRIX matrix = new MATRIX();
        matrix.translateX = x;
        target.addTag(new PlaceObject2Tag(target.getSwf(), move, depth, character, matrix, null, -1, null, -1, null));
    }

    private static Map<Integer, DepthState> selection(Timelined target, int frame) {
        Map<Integer, DepthState> selected = new LinkedHashMap<>();
        // Deliberately select in reverse order; stacking must follow depths.
        selected.put(5, target.getTimeline().getDepthState(frame, 5));
        selected.put(2, target.getTimeline().getDepthState(frame, 2));
        return selected;
    }

    private static SWF reopen(SWF swf) throws Exception {
        new DefineBeforeUsageFixer().fixDefineBeforeUsage(swf);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
    }

    @Test(dataProvider = "symbolTypes")
    public void selectionKeepsStackingAndInheritedTransformsAfterSaving(boolean button) throws Exception {
        SWF swf = scene();
        CharacterTag symbol = SelectionToSymbol.create(swf, button, selection(swf, 2));
        swf.addTag(symbol);
        Timelined saved = (Timelined) reopen(swf).getCharacter(symbol.getCharacterId());
        assertEquals(saved.getFrameCount(), button ? 4 : 1);
        for (int frame = 0; frame < saved.getFrameCount(); frame++) {
            assertEquals(saved.getTimeline().getDepthState(frame, 1).matrix.translateX, 200);
            assertEquals(saved.getTimeline().getDepthState(frame, 2).matrix.translateX, 600);
            assertEquals(saved.getTimeline().getMaxDepth(), 2);
        }
    }

    @Test(dataProvider = "symbolTypes")
    public void replacingCurrentSpansPreservesEarlierAndFollowingKeys(boolean button) throws Exception {
        SWF swf = scene();
        CharacterTag symbol = SelectionToSymbol.create(swf, button, selection(swf, 2));
        Map<Integer, TimelineBodyPanel.FrameSpan> spans = new LinkedHashMap<>();
        spans.put(2, new TimelineBodyPanel.FrameSpan(1, 2));
        spans.put(5, new TimelineBodyPanel.FrameSpan(1, 2));
        TimelineBlankFrames blanks = SelectionToSymbol.replace(swf, symbol, spans, new TimelineBlankFrames());
        swf.addTag(symbol);
        SWF saved = reopen(swf);
        assertEquals(saved.getTimeline().getDepthState(0, 2).matrix.translateX, 100);
        assertEquals(saved.getTimeline().getDepthState(0, 5).matrix.translateX, 500);
        for (int frame = 1; frame <= 2; frame++) {
            assertNull(saved.getTimeline().getDepthState(frame, 2));
            DepthState state = saved.getTimeline().getDepthState(frame, 5);
            assertEquals(state.characterId, symbol.getCharacterId());
            assertEquals(state.matrix.translateX, 0);
        }
        assertEquals(saved.getTimeline().getDepthState(3, 2).matrix.translateX, 300);
        assertEquals(saved.getTimeline().getDepthState(3, 5).matrix.translateX, 700);
        assertTrue(blanks.hasKey(2, 1));
        assertEquals(blanks.end(2), 2);
    }

    @Test(dataProvider = "symbolTypes")
    public void inheritedFiltersBlendAndColorAreKept(boolean button) throws Exception {
        SWF swf = scene();
        PlaceObject3Tag effects = new PlaceObject3Tag(swf);
        effects.depth = 5;
        effects.placeFlagMove = true;
        effects.placeFlagHasColorTransform = true;
        effects.colorTransform = new CXFORMWITHALPHA();
        effects.colorTransform.hasAddTerms = true;
        effects.colorTransform.redAddTerm = 42;
        effects.placeFlagHasBlendMode = true;
        effects.blendMode = 3;
        effects.placeFlagHasFilterList = true;
        effects.surfaceFilterList = Collections.singletonList(new BLURFILTER());
        swf.addTag(3, effects);
        swf.resetTimeline();
        CharacterTag symbol = SelectionToSymbol.create(swf, button, selection(swf, 2));
        swf.addTag(symbol);
        Timelined saved = (Timelined) reopen(swf).getCharacter(symbol.getCharacterId());
        DepthState state = saved.getTimeline().getDepthState(0, 2);
        assertEquals(state.colorTransForm.getRedAdd(), 42);
        assertEquals(state.blendMode, 3);
        assertEquals(state.filters.size(), 1);
    }

    @Test
    public void emptySymbolsHaveUsableFrameCountsAndNoObjects() throws Exception {
        SWF swf = scene();
        for (boolean button : new boolean[]{false, true}) {
            CharacterTag symbol = SelectionToSymbol.create(swf, button, Collections.emptyMap());
            swf.addTag(symbol);
            Timelined saved = (Timelined) reopen(swf).getCharacter(symbol.getCharacterId());
            assertEquals(saved.getFrameCount(), button ? 4 : 1);
            assertEquals(saved.getTimeline().getMaxDepth(), 0);
        }
    }

    @Test
    public void masksUseTheCompactedDepthsInsideTheNewMovieClip() throws Exception {
        SWF swf = scene();
        Map<Integer, DepthState> selected = selection(swf, 0);
        selected.get(2).clipDepth = 5;
        CharacterTag symbol = SelectionToSymbol.create(swf, false, selected);
        swf.addTag(symbol);
        Timelined saved = (Timelined) reopen(swf).getCharacter(symbol.getCharacterId());
        assertEquals(saved.getTimeline().getDepthState(0, 1).clipDepth, 2);
        assertEquals(selected.get(2).clipDepth, 5, "Creating a symbol must not mutate the original state");
    }

    @Test
    public void eachSourceLayerKeepsItsOwnCurrentSpan() throws Exception {
        SWF swf = scene();
        int originalId = swf.getTimeline().getDepthState(0, 2).characterId;
        place(swf, 3, originalId, 900, false);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 5;
        swf.resetTimeline();
        CharacterTag symbol = SelectionToSymbol.create(swf, false, selection(swf, 2));
        Map<Integer, TimelineBodyPanel.FrameSpan> spans = new LinkedHashMap<>();
        spans.put(2, new TimelineBodyPanel.FrameSpan(0, 2));
        spans.put(5, new TimelineBodyPanel.FrameSpan(1, 3));
        SelectionToSymbol.replace(swf, symbol, spans, new TimelineBlankFrames());
        swf.addTag(symbol);
        SWF saved = reopen(swf);
        assertNull(saved.getTimeline().getDepthState(0, 2));
        assertEquals(saved.getTimeline().getDepthState(0, 5).characterId, originalId);
        assertEquals(saved.getTimeline().getDepthState(3, 2).matrix.translateX, 300);
        assertEquals(saved.getTimeline().getDepthState(3, 5).characterId, symbol.getCharacterId());
        assertEquals(saved.getTimeline().getDepthState(4, 5).matrix.translateX, 700);
        assertEquals(saved.getTimeline().getDepthState(4, 3).matrix.translateX, 900, "An unselected layer survives");
    }

    @Test
    public void convertingInsideButtonChangesOnlyTheCurrentStateSpan() throws Exception {
        SWF swf = scene();
        DefineButton2Tag parent = (DefineButton2Tag) SelectionToSymbol.create(swf, true, selection(swf, 0));
        swf.addTag(parent);
        Map<Integer, DepthState> selected = new LinkedHashMap<>();
        selected.put(1, parent.getTimeline().getDepthState(1, 1));
        selected.put(2, parent.getTimeline().getDepthState(1, 2));
        CharacterTag symbol = SelectionToSymbol.create(swf, false, selected);
        Map<Integer, TimelineBodyPanel.FrameSpan> spans = new LinkedHashMap<>();
        spans.put(1, new TimelineBodyPanel.FrameSpan(1, 2));
        spans.put(2, new TimelineBodyPanel.FrameSpan(1, 2));
        SelectionToSymbol.replace(parent, symbol, spans, new TimelineBlankFrames());
        swf.addTag(symbol);
        ButtonTag saved = (ButtonTag) reopen(swf).getCharacter(parent.buttonId);
        assertNull(saved.getButtonRecordAt(1, 1, false));
        assertNull(saved.getButtonRecordAt(2, 1, false));
        assertEquals(saved.getButtonRecordAt(1, 2, false).characterId, symbol.getCharacterId());
        assertEquals(saved.getButtonRecordAt(2, 2, false).characterId, symbol.getCharacterId());
        for (int frame : new int[]{0, 3}) {
            BUTTONRECORD record = saved.getButtonRecordAt(frame, 1, false);
            assertNotNull(record);
            assertEquals(record.placeMatrix.translateX, 100);
            assertEquals(saved.getButtonRecordAt(frame, 2, false).placeMatrix.translateX, 500);
        }
    }
}
