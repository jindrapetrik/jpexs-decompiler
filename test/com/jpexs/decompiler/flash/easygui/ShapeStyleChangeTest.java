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
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.ShapeLayers;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.annotations.Test;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.*;
import static org.testng.Assert.*;

public class ShapeStyleChangeTest {

    private static ShapeSelection.Selection at(ShapeSelection model, double x, double y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static void addClean(ShapeTag shape) {
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
        shape.getSwf().setHeaderModified(false);
    }

    private static ShapeTag roundTrip(ShapeTag shape) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shape.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return (ShapeTag) restored.getCharacter(shape.shapeId);
    }

    @Test
    public void shapeLayersSurviveSaveAndEditsAcrossTagVersions() throws Exception {
        SWF swf = new SWF();
        swf.version = 1;
        DefineShapeTag original = new DefineShapeTag(swf);
        original.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill(Color.red).toShapeNum(1)};
        original.shapes.shapeRecords.clear();
        rectangle(original, 400, 500, 2000, 2000, 1, 0);
        original.shapes.shapeRecords.add(new EndShapeRecord());
        original.updateBounds();
        original.createOriginalData();
        addClean(original);

        UndoManager history = new UndoManager();
        ShapeTag withLayers = ShapeStyleChangeOperation.createShape(original, Arrays.asList(original.shapes,
                SHAPEWITHSTYLE.createEmpty(1), SHAPEWITHSTYLE.createEmpty(1)));
        history.doOperation(new ShapeStyleChangeOperation(original, withLayers, "Add layer", null), swf);
        ShapeTag emptyLayers = roundTrip(current(original));
        assertEquals(emptyLayers.getShapeNum(), 2);
        assertEquals(emptyLayers.shapeBounds.getTopLeft(), original.shapeBounds.getTopLeft());
        assertEquals(emptyLayers.shapeBounds.getBottomRight(), original.shapeBounds.getBottomRight());
        List<SHAPEWITHSTYLE> layers = ShapeLayers.split(emptyLayers.getShapes(), emptyLayers.getShapeNum());
        assertEquals(layers.size(), 3);
        assertFalse(ShapeLayers.hasEdges(layers.get(1)));
        assertFalse(ShapeLayers.hasEdges(layers.get(2)));

        ShapeTag beforeDrawing = current(original);
        ShapeSelection active = new ShapeSelection(beforeDrawing, layers.get(2));
        ShapeSelection.BatchEdit drawing = active.drawShape(new Rectangle2D.Double(700, 800, 1000, 1000),
                fill(new Color(0, 0, 255, 128)), null);
        ShapeTag painted = ShapeStyleChangeOperation.createShape(beforeDrawing, layers, 2, drawing.model);
        history.doOperation(new ShapeStyleChangeOperation(beforeDrawing, painted, "Draw shape", null), swf);
        ShapeTag saved = roundTrip(current(original));
        assertEquals(saved.getShapeNum(), 3);
        List<SHAPEWITHSTYLE> savedLayers = ShapeLayers.split(saved.getShapes(), saved.getShapeNum());
        assertEquals(savedLayers.size(), 3);
        assertEquals(at(new ShapeSelection(saved, savedLayers.get(0)), 1000, 1000).fill.color.toColor(), Color.red);
        assertFalse(ShapeLayers.hasEdges(savedLayers.get(1)));
        assertEquals(at(new ShapeSelection(saved), 1000, 1000).fill.color.toColor(), new Color(0, 0, 255, 128));

        ShapeTag beforeRemoval = current(original);
        savedLayers.remove(0);
        ShapeTag removed = ShapeStyleChangeOperation.createShape(beforeRemoval, savedLayers);
        history.doOperation(new ShapeStyleChangeOperation(beforeRemoval, removed, "Remove layer", null), swf);
        assertNull(at(new ShapeSelection(current(original)), 500, 600));
        history.undo(swf);
        assertSame(current(original), beforeRemoval);
        history.undo(swf);
        assertEquals(current(original).getShapeNum(), 2);
        history.undo(swf);
        assertSame(current(original), original);
        assertEquals(swf.version, 1);
        history.redo(swf);
        history.redo(swf);
        history.redo(swf);
        ShapeTag reopened = roundTrip(current(original));
        assertEquals(ShapeLayers.split(reopened.getShapes(), reopened.getShapeNum()).size(), 2);
        assertEquals(at(new ShapeSelection(reopened), 1000, 1000).fill.color.toColor(), new Color(0, 0, 255, 128));
    }

    @Test
    public void aBatchEditMovesSeveralLayersWithoutFlatteningThem() throws Exception {
        DefineShape4Tag bottom = shape();
        rectangle(bottom, 0, 0, 1000, 1000, 1, 0);
        selection(bottom);
        DefineShape4Tag middle = shape();
        rectangle(middle, 2000, 0, 1000, 1000, 2, 0);
        selection(middle);
        DefineShape4Tag top = shape();
        rectangle(top, 4000, 0, 1000, 1000, 1, 0);
        selection(top);
        ShapeTag original = ShapeStyleChangeOperation.createShape(bottom,
                Arrays.asList(bottom.getShapes(), middle.getShapes(), top.getShapes()));
        original.createOriginalData();
        addClean(original);

        List<SHAPEWITHSTYLE> layers = ShapeLayers.split(original.getShapes(), original.getShapeNum());
        Map<Integer, ShapeSelection> edits = new LinkedHashMap<>();
        for (int layer = 0; layer < 2; layer++) {
            ShapeSelection model = new ShapeSelection(original, layers.get(layer));
            ShapeSelection.RectangleSelection selected = model.selectRectangle(
                    new Rectangle2D.Double(0, 0, 3000, 1000), new ArrayList<>(), false);
            edits.put(layer, selected.model.moveFloatingSelection(selected.selected, 0, 1500).model);
        }
        ShapeTag moved = ShapeStyleChangeOperation.createShape(original, layers, edits);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(original, moved, "Move", null), original.getSwf());

        ShapeTag saved = roundTrip(current(original));
        List<SHAPEWITHSTYLE> savedLayers = ShapeLayers.split(saved.getShapes(), saved.getShapeNum());
        assertEquals(savedLayers.size(), 3);
        assertEquals(at(new ShapeSelection(saved, savedLayers.get(0)), 500, 2000).fill.color.toColor(), Color.red);
        assertEquals(at(new ShapeSelection(saved, savedLayers.get(1)), 2500, 2000).fill.color.toColor(), Color.blue);
        assertEquals(at(new ShapeSelection(saved, savedLayers.get(2)), 4500, 500).fill.color.toColor(), Color.red);
        assertNull(at(new ShapeSelection(saved), 500, 500));
        assertNull(at(new ShapeSelection(saved), 2500, 500));

        history.undo(original.getSwf());
        assertSame(current(original), original);
        assertFalse(history.canUndo(original.getSwf()));
        history.redo(original.getSwf());
        assertEquals(current(original).getData(), saved.getData());
    }

    @Test
    public void reorderingLayersChangesStackingAndPreservesTheirGeometryOnSave() throws Exception {
        DefineShape4Tag bottom = shape();
        rectangle(bottom, 0, 0, 2000, 2000, 1, 0);
        selection(bottom);
        DefineShape4Tag top = shape();
        rectangle(top, 1000, 500, 2000, 2000, 2, 0);
        selection(top);
        ShapeTag original = ShapeStyleChangeOperation.createShape(bottom,
                Arrays.asList(bottom.getShapes(), SHAPEWITHSTYLE.createEmpty(4), top.getShapes()));
        original.createOriginalData();
        addClean(original);
        assertEquals(at(new ShapeSelection(original), 1500, 1000).fill.color.toColor(), Color.blue);

        List<SHAPEWITHSTYLE> layers = ShapeLayers.split(original.getShapes(), original.getShapeNum());
        layers.add(layers.remove(0));
        ShapeTag reordered = ShapeStyleChangeOperation.createShape(original, layers);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(original, reordered, "Move layer", null), original.getSwf());

        ShapeTag saved = roundTrip(current(original));
        List<SHAPEWITHSTYLE> savedLayers = ShapeLayers.split(saved.getShapes(), saved.getShapeNum());
        assertEquals(savedLayers.size(), 3);
        assertFalse(ShapeLayers.hasEdges(savedLayers.get(0)));
        ShapeSelection result = new ShapeSelection(saved);
        assertEquals(at(result, 1500, 1000).fill.color.toColor(), Color.red);
        assertEquals(at(result, 500, 500).fill.color.toColor(), Color.red);
        assertEquals(at(result, 2500, 1000).fill.color.toColor(), Color.blue);
        history.undo(original.getSwf());
        assertSame(current(original), original);
        history.redo(original.getSwf());
        assertEquals(current(original).getData(), saved.getData());
    }

    @Test
    public void resourceUndoRestoresLoadedBytesAfterSeveralEdits() throws Exception {
        DefineShape4Tag fixture = shape();
        rectangle(fixture, 0, 0, 2000, 2000, 1, 0);
        selection(fixture);
        fixture.createOriginalData();
        addClean(fixture);
        ShapeTag shape = roundTrip(fixture);
        byte[] originalRange = shape.getOriginalRange().getRangeData();
        byte[] originalData = shape.getData();
        UndoManager history = new UndoManager();
        ShapeSelection model = new ShapeSelection(shape);
        ShapeSelection.Edit green = model.change(at(model, 500, 500), fill(Color.green), null);
        history.doOperation(new ShapeStyleChangeOperation(shape, green.model, "Green", null), shape.getSwf());
        ShapeSelection.Edit blue = green.model.change(green.selected, fill(Color.blue), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), blue.model, "Blue", null), shape.getSwf());
        ShapeTag edited = current(shape);
        assertTrue(edited.canUndo());
        assertEquals(edited.getOriginalRange().getRangeData(), originalRange);
        edited.undo();
        assertEquals(edited.getData(), originalData);
        assertEquals(edited.getOriginalRange().getRangeData(), originalRange);
        assertFalse(edited.isModified());
        assertFalse(edited.canUndo());
    }

    @Test
    public void resourceUndoUsesTheSavedBaselineAfterHistoryChanges() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        shape.createOriginalData();
        UndoManager history = new UndoManager();
        ShapeSelection.Edit green = model.change(at(model, 500, 500), fill(Color.green), null);
        history.doOperation(new ShapeStyleChangeOperation(shape, green.model, "Green", null), shape.getSwf());
        shape.getSwf().clearModified();
        byte[] savedData = current(shape).getData();
        byte[] savedRange = current(shape).getOriginalRange().getRangeData();
        ShapeSelection.Edit blue = green.model.change(green.selected, fill(Color.blue), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), blue.model, "Blue", null), shape.getSwf());
        history.undo(shape.getSwf());
        history.undo(shape.getSwf());
        assertTrue(current(shape).canUndo());
        assertEquals(current(shape).getOriginalRange().getRangeData(), savedRange);
        current(shape).undo();
        assertEquals(current(shape).getData(), savedData);
        assertFalse(current(shape).isModified());
    }

    @Test
    public void sharedFillIsIsolatedAndUndoRestoresOriginalBytesAndCleanFlag() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        rectangle(shape, 500, 500, 1000, 1000, 1, 0);
        rectangle(shape, 3000, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] original = shape.getData();
        ShapeSelection.Selection selected = at(model, 200, 200);
        ShapeSelection.Edit edit = model.change(selected, fill(Color.green), null);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Fill", null), shape.getSwf());
        ShapeTag changed = current(shape);
        assertTrue(changed.isModified());
        assertEquals(at(new ShapeSelection(changed), 200, 200).fill.color.toColor(), Color.green);
        assertEquals(at(new ShapeSelection(changed), 3500, 500).fill.color.toColor(), Color.red);
        assertNull(at(new ShapeSelection(changed), 1000, 1000));
        assertTrue(shape.getSwf().isModified());
        byte[] after = changed.getData();
        assertEquals(at(new ShapeSelection(roundTrip(changed)), 200, 200).fill.color.toColor(), Color.green);
        for (int i = 0; i < 3; i++) {
            history.undo(shape.getSwf());
            assertSame(current(shape), shape);
            assertEquals(current(shape).getData(), original);
            assertFalse(shape.getSwf().isModified());
            assertFalse(current(shape).isModified());
            history.redo(shape.getSwf());
            assertEquals(current(shape).getData(), after);
            assertTrue(current(shape).isModified());
        }
    }

    @Test
    public void editingFillOnOneSideOfAStrokeDoesNotLeaveAnOldColorRim() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        move(shape.shapes.shapeRecords, 1000, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 0, 2000);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.Edit fillEdit = model.change(at(model, 500, 1000), fill(Color.green), null);
        assertFalse(fillEdit.model.displaySelection(fillEdit.selected).area.contains(950, 1000));
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, fillEdit.model, "Fill", null), shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 500, 1000).fill.color.toColor(), Color.green);
        assertEquals(at(new ShapeSelection(current(shape)), 1500, 1000).fill.color.toColor(), Color.red);
        ShapeSelection.Selection stroke = at(fillEdit.model, 1000, 1000);
        ShapeSelection.Edit noStroke = fillEdit.model.change(stroke, null, null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), noStroke.model, "Stroke", null), shape.getSwf());
        ShapeSelection result = new ShapeSelection(roundTrip(current(shape)));
        assertEquals(at(result, 999, 1000).fill.color.toColor(), Color.green);
        assertEquals(at(result, 1001, 1000).fill.color.toColor(), Color.red);
        history.undo(shape.getSwf());
        assertNotNull(at(new ShapeSelection(current(shape)), 1000, 1000).line);
        history.undo(shape.getSwf());
        assertFalse(shape.getSwf().isModified());
    }

    @Test
    public void noneAndSubsequentEditsKeepTheirOwnSnapshotsAndDropRedoBranch() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.Selection selected = at(model, 500, 500);
        ShapeSelection.Edit none = model.change(selected, null, null);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, none.model, "None", null), shape.getSwf());
        assertNull(at(new ShapeSelection(current(shape)), 500, 500));
        ShapeSelection.Edit green = none.model.change(none.selected, fill(Color.green), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), green.model, "Green", null), shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 500, 500).fill.color.toColor(), Color.green);
        history.undo(shape.getSwf());
        history.undo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 500, 500).fill.color.toColor(), Color.red);
        ShapeSelection.Edit blue = model.change(selected, fill(Color.blue), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), blue.model, "Blue", null), shape.getSwf());
        assertFalse(history.canRedo(shape.getSwf()));
        assertEquals(at(new ShapeSelection(current(shape)), 500, 500).fill.color.toColor(), Color.blue);
    }

    @Test
    public void onlyTheSelectedEdgeChangesAndUndoRestoresTheWholeOriginalStroke() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] original = shape.getData();
        ShapeSelection.Selection selected = at(model, 1000, 0);
        LINESTYLE2 thicker = new LINESTYLE2();
        thicker.width = 400;
        thicker.color = new RGBA(Color.blue);
        ShapeSelection.Edit edit = model.change(selected, null, thicker);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Edge", null), shape.getSwf());
        ShapeSelection restored = new ShapeSelection(roundTrip(current(shape)));
        assertEquals(at(restored, 1000, 0).line.getWidth(), 400);
        assertEquals(at(restored, 1000, 0).line.getColor().toColor(), Color.blue);
        for (Point2D point : new Point2D[]{new Point2D.Double(0, 1000), new Point2D.Double(2000, 1000), new Point2D.Double(1000, 2000)}) {
            ShapeSelection.Selection untouched = restored.select(point, 0);
            assertEquals(untouched.line.getWidth(), 200);
            assertEquals(untouched.line.getColor().toColor(), Color.black);
        }
        assertEquals(at(restored, 1000, 1000).fill.color.toColor(), Color.red);
        history.undo(shape.getSwf());
        assertEquals(current(shape).getData(), original);
        assertFalse(shape.getSwf().isModified());
        history.redo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 1000, 0).line.getWidth(), 400);
        ShapeSelection.Edit deleted = model.change(selected, null, null);
        history.undo(shape.getSwf());
        history.doOperation(new ShapeStyleChangeOperation(current(shape), deleted.model, "Delete edge", null), shape.getSwf());
        ShapeSelection withoutTop = new ShapeSelection(roundTrip(current(shape)));
        assertNull(at(withoutTop, 1000, -50));
        assertNotNull(at(withoutTop, 0, 1000).line);
        assertNotNull(at(withoutTop, 2000, 1000).line);
        assertNotNull(at(withoutTop, 1000, 2000).line);
    }

    @Test
    public void advancedStrokeUpgradesLegacyTagAndVersionAndUndoRevertsBoth() throws Exception {
        DefineShape3Tag shape = new DefineShape3Tag(new SWF());
        shape.getSwf().version = 3;
        LINESTYLE legacyLine = new LINESTYLE();
        legacyLine.width = 100;
        legacyLine.color = new RGBA(Color.black);
        shape.shapes.lineStyles.lineStyles = new LINESTYLE[]{legacyLine};
        shape.shapes.shapeRecords = new ArrayList<>();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        move(shape.shapes.shapeRecords, 0, 1000, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        LINESTYLE2 changedLine = legacyLine.toLineStyle2();
        changedLine.width = 400;
        changedLine.startCapStyle = LINESTYLE2.SQUARE_CAP;
        changedLine.endCapStyle = LINESTYLE2.SQUARE_CAP;
        changedLine.joinStyle = LINESTYLE2.MITER_JOIN;
        changedLine.miterLimitFactor = 7;
        changedLine.noHScaleFlag = true;
        ShapeSelection.Edit edit = model.change(at(model, 1000, 0), null, changedLine);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Stroke", null), shape.getSwf());
        assertTrue(current(shape) instanceof DefineShape4Tag);
        assertEquals(shape.getSwf().version, 8);
        ShapeSelection restored = new ShapeSelection(roundTrip(current(shape)));
        LINESTYLE2 selected = (LINESTYLE2) at(restored, 1000, 0).line;
        assertEquals(selected.width, 400);
        assertEquals(selected.startCapStyle, LINESTYLE2.SQUARE_CAP);
        assertEquals(selected.joinStyle, LINESTYLE2.MITER_JOIN);
        assertEquals(selected.miterLimitFactor, 7f);
        assertTrue(selected.noHScaleFlag);
        assertEquals(at(restored, 1000, 1000).line.getWidth(), 100);
        history.undo(shape.getSwf());
        assertSame(current(shape), shape);
        assertEquals(shape.getSwf().version, 3);
        assertFalse(shape.getSwf().isModified());
        history.redo(shape.getSwf());
        assertEquals(current(shape).getShapeNum(), 4);
    }

    @Test
    public void resourceUndoRestoresTheOriginalTagTypeAfterAnUpgrade() throws Exception {
        DefineShape3Tag shape = new DefineShape3Tag(new SWF());
        LINESTYLE line = new LINESTYLE();
        line.width = 100;
        line.color = new RGBA(Color.black);
        shape.shapes.lineStyles.lineStyles = new LINESTYLE[]{line};
        shape.shapes.shapeRecords = new ArrayList<>();
        move(shape.shapes.shapeRecords, 0, 0, 0, 1);
        edge(shape.shapes.shapeRecords, 2000, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        shape.createOriginalData();
        byte[] originalData = shape.getData();
        byte[] originalRange = shape.getOriginalRange().getRangeData();
        LINESTYLE2 square = line.toLineStyle2();
        square.startCapStyle = LINESTYLE2.SQUARE_CAP;
        ShapeSelection.Edit changed = model.change(at(model, 1000, 0), null, square);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, changed.model, "Cap", null), shape.getSwf());
        ShapeTag upgraded = current(shape);
        assertEquals(upgraded.getShapeNum(), 4);
        assertEquals(((ShapeTag) upgraded.getOriginalTag()).getShapeNum(), 3);
        upgraded.undo();
        shape.getSwf().clearAllCache();
        ShapeTag restored = current(shape);
        assertEquals(restored.getShapeNum(), 3);
        assertEquals(restored.getData(), originalData);
        assertEquals(restored.getOriginalRange().getRangeData(), originalRange);
        assertFalse(restored.canUndo());
    }

    @Test
    public void undoAfterSavingIsDirtyAndRedoBackToTheSavedPositionIsClean() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        UndoManager history = new UndoManager();
        ShapeSelection.Edit green = model.change(at(model, 500, 500), fill(Color.green), null);
        history.doOperation(new ShapeStyleChangeOperation(shape, green.model, "Green", null), shape.getSwf());
        ShapeSelection.Edit blue = green.model.change(green.selected, fill(Color.blue), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), blue.model, "Blue", null), shape.getSwf());
        shape.getSwf().clearModified(); // The application marks the SWF clean after saving it.
        assertFalse(current(shape).isModified());
        history.undo(shape.getSwf());
        assertTrue(shape.getSwf().isModified());
        assertTrue(current(shape).isModified());
        history.undo(shape.getSwf());
        assertTrue(shape.getSwf().isModified());
        assertTrue(current(shape).isModified());
        history.redo(shape.getSwf());
        assertTrue(shape.getSwf().isModified());
        assertTrue(current(shape).isModified());
        history.redo(shape.getSwf());
        assertFalse(shape.getSwf().isModified());
        assertFalse(current(shape).isModified());
        assertEquals(at(new ShapeSelection(current(shape)), 500, 500).fill.color.toColor(), Color.blue);
        history.undo(shape.getSwf());
        ShapeSelection.Edit yellow = green.model.change(green.selected, fill(Color.yellow), null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), yellow.model, "Yellow", null), shape.getSwf());
        assertFalse(history.canRedo(shape.getSwf()));
        assertTrue(shape.getSwf().isModified());
        history.undo(shape.getSwf());
        assertTrue(shape.getSwf().isModified());
    }

    @Test
    public void addsAStrokeToAFillWithoutChangingOtherRegions() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        rectangle(shape, 3000, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        LINESTYLE2 outline = new LINESTYLE2();
        outline.width = 200;
        outline.color = new RGBA(Color.blue);
        ShapeSelection.Selection selected = at(model, 500, 500);
        ShapeSelection.Edit edit = model.change(selected, selected.fill, outline);
        assertTrue(edit.selected.stroke);
        assertNull(edit.selected.fill);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Outline", null), shape.getSwf());
        ShapeSelection restored = new ShapeSelection(roundTrip(current(shape)));
        assertEquals(at(restored, 0, 1000).line.getWidth(), 200);
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.red);
        assertNull(at(restored, 3001, 500).line);
        history.undo(shape.getSwf());
        assertNull(at(new ShapeSelection(current(shape)), 1, 1000).line);
        assertFalse(shape.getSwf().isModified());
    }

    @Test
    public void gradientAndBitmapStylesSurviveSerializationAndHistory() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        FILLSTYLE gradient = new FILLSTYLE();
        gradient.fillStyleType = FILLSTYLE.RADIAL_GRADIENT;
        gradient.gradientMatrix = new MATRIX();
        gradient.gradient = new GRADIENT();
        gradient.gradient.gradientRecords = new GRADRECORD[2];
        for (int i = 0; i < 2; i++) {
            GRADRECORD stop = new GRADRECORD();
            stop.ratio = i * 255;
            stop.color = new RGBA(i == 0 ? new Color(255, 0, 0, 80) : Color.blue);
            stop.inShape3 = true;
            gradient.gradient.gradientRecords[i] = stop;
        }
        ShapeSelection.Edit gradientEdit = model.change(at(model, 500, 500), gradient, null);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, gradientEdit.model, "Gradient", null), shape.getSwf());
        FILLSTYLE restored = at(new ShapeSelection(roundTrip(current(shape))), 500, 500).fill;
        assertEquals(restored.fillStyleType, FILLSTYLE.RADIAL_GRADIENT);
        assertEquals(((RGBA) restored.gradient.gradientRecords[0].color).alpha, 80);
        FILLSTYLE bitmap = new FILLSTYLE();
        bitmap.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
        bitmap.bitmapId = 123;
        bitmap.bitmapMatrix = new MATRIX();
        ShapeSelection.Edit bitmapEdit = gradientEdit.model.change(gradientEdit.selected, bitmap, null);
        history.doOperation(new ShapeStyleChangeOperation(current(shape), bitmapEdit.model, "Bitmap", null), shape.getSwf());
        assertEquals(at(new ShapeSelection(roundTrip(current(shape))), 500, 500).fill.bitmapId, 123);
        history.undo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 500, 500).fill.fillStyleType, FILLSTYLE.RADIAL_GRADIENT);
    }
}
