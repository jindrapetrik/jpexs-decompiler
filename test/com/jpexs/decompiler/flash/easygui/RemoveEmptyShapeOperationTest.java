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
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class RemoveEmptyShapeOperationTest {

    @Test
    public void emptyMeansNoGeometryIncludingAllShapeLayers() {
        DefineShape4Tag shape = new DefineShape4Tag(new SWF());
        assertTrue(RemoveEmptyShapeOperation.isEmpty(shape));
        StyleChangeRecord layer = new StyleChangeRecord();
        layer.stateMoveTo = true;
        layer.moveDeltaX = 200;
        shape.shapes.shapeRecords.add(0, layer);
        assertTrue(RemoveEmptyShapeOperation.isEmpty(shape));
        StraightEdgeRecord line = new StraightEdgeRecord();
        line.deltaX = 200;
        shape.shapes.shapeRecords.add(1, line);
        assertFalse(RemoveEmptyShapeOperation.isEmpty(shape), "A horizontal line must survive");
        line.deltaX = 0;
        line.deltaY = 200;
        line.vertLineFlag = true;
        assertFalse(RemoveEmptyShapeOperation.isEmpty(shape), "A vertical line must survive");
        shape.shapes.shapeRecords.set(1, new CurvedEdgeRecord());
        assertFalse(RemoveEmptyShapeOperation.isEmpty(shape));
    }

    private static void place(Timelined target, int depth, int id, boolean move) {
        target.addTag(new PlaceObject2Tag(target.getSwf(), move, depth, id, new MATRIX(), null, -1, null, -1, null));
    }

    private static SWF reopen(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
    }

    @Test
    public void removesDefinitionSharedInstancesMovesAndMetadataAndCanUndo() throws Exception {
        SWF swf = new SWF();
        swf.version = 10;
        DefineShape4Tag empty = new DefineShape4Tag(swf);
        swf.addTag(empty);
        DefineShape4Tag kept = new DefineShape4Tag(swf);
        swf.addTag(kept);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        place(sprite, 3, empty.shapeId, false);
        place(sprite, 5, kept.shapeId, false);
        sprite.addTag(new ShowFrameTag(swf));
        place(sprite, 3, -1, true);
        sprite.addTag(new ShowFrameTag(swf));
        place(sprite, 3, kept.shapeId, true);
        sprite.addTag(new ShowFrameTag(swf));
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 5;
        sprite.addTag(remove);
        sprite.addTag(new ShowFrameTag(swf));
        sprite.frameCount = 4;
        swf.addTag(sprite);
        DefineButton2Tag button = new DefineButton2Tag(swf);
        for (int id : new int[]{empty.shapeId, kept.shapeId}) {
            BUTTONRECORD record = new BUTTONRECORD(swf, button);
            record.fromPlaceObject(new PlaceObject2Tag(swf, false, id, id, new MATRIX(), null, -1, null, -1, null));
            for (int state = 0; state < 4; state++) {
                record.setFrame(state, true);
            }
            button.characters.add(record);
        }
        swf.addTag(button);
        DefineScalingGridTag grid = new DefineScalingGridTag(swf);
        grid.characterId = empty.shapeId;
        swf.addTag(grid);
        ExportAssetsTag exports = new ExportAssetsTag(swf);
        exports.tags.addAll(Arrays.asList(empty.shapeId, kept.shapeId));
        exports.names.addAll(Arrays.asList("empty", "kept"));
        swf.addTag(exports);
        SymbolClassTag classes = new SymbolClassTag(swf);
        classes.tags.addAll(Arrays.asList(empty.shapeId, kept.shapeId));
        classes.names.addAll(Arrays.asList("Empty", "Kept"));
        swf.addTag(classes);
        place(swf, 1, empty.shapeId, false);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 1;
        UndoManager history = new UndoManager();
        history.doOperation(new RemoveEmptyShapeOperation(empty, null), swf);
        assertNull(swf.getCharacter(empty.shapeId));
        assertNull(swf.getTimeline().getDepthState(0, 1));
        assertNull(sprite.getTimeline().getDepthState(0, 3));
        assertNull(sprite.getTimeline().getDepthState(1, 3));
        assertEquals(sprite.getTimeline().getDepthState(2, 3).characterId, kept.shapeId);
        assertEquals(sprite.frameCount, 4);
        assertNull(sprite.getTimeline().getDepthState(3, 5));
        assertEquals(button.characters.size(), 1);
        assertEquals(button.characters.get(0).characterId, kept.shapeId);
        assertFalse(swf.getTags().toArrayList().contains(grid));
        assertEquals(exports.tags.size(), 2, "The original metadata is an immutable undo snapshot");
        SWF saved = reopen(swf);
        assertNull(saved.getCharacter(empty.shapeId));
        assertEquals(((DefineSpriteTag) saved.getCharacter(sprite.spriteId)).frameCount, 4);
        assertEquals(saved.getCharacter(kept.shapeId).getExportName(), "kept");
        history.undo(swf);
        assertSame(swf.getCharacter(empty.shapeId), empty);
        assertEquals(sprite.getTimeline().getDepthState(0, 3).characterId, empty.shapeId);
        assertEquals(sprite.getTimeline().getDepthState(1, 3).characterId, empty.shapeId);
        assertEquals(button.characters.size(), 2);
        assertTrue(swf.getTags().toArrayList().contains(grid));
        assertTrue(swf.getTags().toArrayList().contains(exports));
        assertTrue(swf.getTags().toArrayList().contains(classes));
        assertNotNull(reopen(swf).getCharacter(empty.shapeId));
        history.redo(swf);
        assertNull(reopen(swf).getCharacter(empty.shapeId));
    }

    @Test
    public void replacementRemovedWithinTheSameFrameDoesNotNeedAFinalDepthState() throws Exception {
        SWF swf = new SWF();
        DefineShape4Tag empty = new DefineShape4Tag(swf);
        swf.addTag(empty);
        DefineShape4Tag kept = new DefineShape4Tag(swf);
        swf.addTag(kept);
        place(swf, 1, empty.shapeId, false);
        swf.addTag(new ShowFrameTag(swf));
        place(swf, 1, kept.shapeId, true);
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        swf.addTag(remove);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 2;
        RemoveEmptyShapeOperation removal = new RemoveEmptyShapeOperation(empty, null);
        removal.doOperation();
        assertNull(swf.getTimeline().getDepthState(1, 1));
        removal.undoOperation();
        assertEquals(swf.getTimeline().getDepthState(0, 1).characterId, empty.shapeId);
    }

    @Test
    public void undoRemovalThenUndoDrawingRestoresTheOriginalDefinition() throws Exception {
        DefineShape4Tag original = ShapeSelectionTest.shape();
        ShapeSelectionTest.rectangle(original, 0, 0, 1000, 1000, 1, 1);
        original.updateBounds();
        SWF swf = original.getSwf();
        swf.addTag(original);
        place(swf, 1, original.shapeId, false);
        swf.addTag(new ShowFrameTag(swf));
        swf.frameCount = 1;
        ShapeSelection model = new ShapeSelection(original);
        ShapeSelection.BatchEdit cleared = model.deleteAll(model.selectAll());
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(original, cleared.model, "Delete drawing", null), swf);
        DefineShape4Tag empty = (DefineShape4Tag) swf.getCharacter(original.shapeId);
        assertTrue(RemoveEmptyShapeOperation.isEmpty(empty));
        history.doOperation(new RemoveEmptyShapeOperation(empty, null), swf);
        history.undo(swf);
        assertSame(swf.getCharacter(original.shapeId), empty);
        history.undo(swf);
        assertSame(swf.getCharacter(original.shapeId), original);
        assertFalse(RemoveEmptyShapeOperation.isEmpty(original));
        history.redo(swf);
        history.redo(swf);
        assertNull(swf.getCharacter(original.shapeId));
    }
}
