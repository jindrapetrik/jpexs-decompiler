/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.fill;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.TextPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.easygui.properties.JTriStateCheckBox;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapeMultiSelectionTest {

    private static ShapeSelection.Selection at(ShapeSelection model, int x, int y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    private static Object field(Object value, String name) throws Exception {
        Field field = value.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(value);
    }

    private static ShapeTag current(ShapeTag original) {
        return (ShapeTag) original.getSwf().getCharacter(original.shapeId);
    }

    private static ShapeSelection roundTrip(ShapeTag shape) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        shape.getSwf().saveTo(bytes);
        com.jpexs.decompiler.flash.SWF restored = new com.jpexs.decompiler.flash.SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        return new ShapeSelection((ShapeTag) restored.getCharacter(shape.shapeId));
    }

    private static void addClean(ShapeTag shape) {
        shape.getSwf().addTag(shape);
        shape.setModified(false);
        shape.getSwf().setModified(false);
    }

    @Test
    public void ctrlClickTogglesIndividualRegionsAndEdgesAndKeepsSelectionOnEmptySpace() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        rectangle(shape, 3000, 0, 1000, 1000, 1, 1);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> selected = new ArrayList<>();
        selected = ShapeSelection.updateSelection(selected, at(model, 500, 500), false);
        selected = ShapeSelection.updateSelection(selected, at(model, 3500, 500), true);
        selected = ShapeSelection.updateSelection(selected, at(model, 1000, 0), true);
        selected = ShapeSelection.updateSelection(selected, at(model, 2000, 1000), true);
        assertEquals(selected.size(), 4);
        selected = ShapeSelection.updateSelection(selected, at(model, 1000, 0), true);
        assertEquals(selected.size(), 3);
        assertTrue(selected.get(2).stroke);
        selected = ShapeSelection.updateSelection(selected, at(model, 500, 500), true);
        assertEquals(selected.size(), 2);
        assertEquals(ShapeSelection.updateSelection(selected, null, true).size(), 2);
        assertTrue(ShapeSelection.updateSelection(selected, null, false).isEmpty());
    }

    @Test
    public void multipleRegionsSharingAStyleChangeInOneHistoryStep() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        rectangle(shape, 2000, 0, 1000, 1000, 1, 0);
        rectangle(shape, 4000, 0, 1000, 1000, 1, 0);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] before = shape.getData();
        List<ShapeSelection.Selection> selected = Arrays.asList(at(model, 500, 500), at(model, 2500, 500));
        ShapeSelection.BatchEdit edit = model.changeAll(selected, item -> fill(Color.green), null);
        assertTrue(edit.changed);
        assertEquals(edit.selected.size(), 2);
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Fill", null), shape.getSwf());
        ShapeSelection restored = roundTrip(current(shape));
        assertEquals(at(restored, 500, 500).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 2500, 500).fill.color.toColor(), Color.green);
        assertEquals(at(restored, 4500, 500).fill.color.toColor(), Color.red);
        history.undo(shape.getSwf());
        assertFalse(history.canUndo(shape.getSwf()));
        assertFalse(shape.getSwf().isModified());
        assertEquals(current(shape).getData(), before);
        history.redo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 2500, 500).fill.color.toColor(), Color.green);
        assertEquals(ShapeSelection.updateSelection(edit.selected, at(edit.model, 2500, 500), true).size(), 1);
    }

    @Test
    public void adjacentEdgesKeepTheirJoinAndRebasedSelectionWhenChangedTogether() throws Exception {
        DefineShape4Tag shape = shape();
        shape.shapes.lineStyles.lineStyles2[0].joinStyle = LINESTYLE2.MITER_JOIN;
        shape.shapes.lineStyles.lineStyles2[0].miterLimitFactor = 4;
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        List<ShapeSelection.Selection> selected = Arrays.asList(at(model, 1000, 0), at(model, 2000, 1000));
        ShapeSelection.BatchEdit edit = model.changeAll(selected, null, item -> {
            LINESTYLE2 line = Helper.deepCopy((LINESTYLE2) item.line);
            line.width = 400;
            return line;
        });
        assertEquals(edit.selected.size(), 2);
        assertTrue(edit.model.getParts().stream().anyMatch(part -> part.line != null && part.line.getWidth() == 400
                && part.area.contains(2150, -150)));
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, edit.model, "Width", null), shape.getSwf());
        ShapeSelection restored = roundTrip(current(shape));
        assertEquals(at(restored, 1000, 0).line.getWidth(), 400);
        assertEquals(at(restored, 2000, 1000).line.getWidth(), 400);
        assertEquals(at(restored, 0, 1000).line.getWidth(), 200);
        assertEquals(at(restored, 1000, 2000).line.getWidth(), 200);
        assertEquals(ShapeSelection.updateSelection(edit.selected, at(edit.model, 2000, 1000), true).size(), 1);
        history.undo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 1000, 0).line.getWidth(), 200);
        history.redo(shape.getSwf());
        assertEquals(at(new ShapeSelection(current(shape)), 2000, 1000).line.getWidth(), 400);
    }

    @Test
    public void mixedSelectionRebasesUntouchedItemsAndOnlyChangesTheRequestedKind() {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> selected = Arrays.asList(at(model, 500, 500), at(model, 1000, 0), at(model, 2000, 1000));
        ShapeSelection.BatchEdit fillEdit = model.changeAll(selected, item -> fill(Color.green), null);
        assertEquals(fillEdit.selected.size(), 3);
        assertEquals(fillEdit.selected.get(1).line.getWidth(), 200);
        assertTrue(ShapeSelection.sameSelection(fillEdit.selected.get(1), at(fillEdit.model, 1000, 0)));
        ShapeSelection.BatchEdit lineEdit = fillEdit.model.changeAll(fillEdit.selected, null, item -> {
            LINESTYLE2 line = Helper.deepCopy((LINESTYLE2) item.line);
            line.width = 300;
            return line;
        });
        assertEquals(lineEdit.selected.get(0).fill.color.toColor(), Color.green);
        assertEquals(lineEdit.selected.get(1).line.getWidth(), 300);
        assertEquals(lineEdit.selected.get(2).line.getWidth(), 300);
        ShapeSelection.BatchEdit noChange = lineEdit.model.changeAll(lineEdit.selected, item -> item.fill, item -> item.line);
        assertFalse(noChange.changed);
    }

    @Test
    public void mixedPropertiesAreIndeterminateAndAWidthEditPreservesAllOtherAttributes() throws Exception {
        DefineShape4Tag shape = shape();
        LINESTYLE2 first = shape.shapes.lineStyles.lineStyles2[0];
        first.width = 40;
        first.color = new RGBA(Color.red);
        first.joinStyle = LINESTYLE2.MITER_JOIN;
        first.miterLimitFactor = 3;
        LINESTYLE2 second = Helper.deepCopy(first);
        second.width = 100;
        second.color = new RGBA(Color.blue);
        second.noHScaleFlag = true;
        second.startCapStyle = LINESTYLE2.SQUARE_CAP;
        second.endCapStyle = LINESTYLE2.SQUARE_CAP;
        second.miterLimitFactor = 9;
        shape.shapes.lineStyles.lineStyles2 = new LINESTYLE2[]{first, second};
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        rectangle(shape, 3000, 0, 1000, 1000, 2, 2);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> lines = Arrays.asList(at(model, 1000, 0), at(model, 3500, 0));
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertTrue(stroke);
                    assertEquals(property, "width");
                    edits.incrementAndGet();
                });
                panel.setShape(shape);
                panel.setSelections(lines);
                assertEquals(edits.get(), 0);
                FloatPropertyField width = (FloatPropertyField) field(panel, "widthEditor");
                FloatPropertyField miter = (FloatPropertyField) field(panel, "miterEditor");
                assertNull(width.getValue());
                assertNull(miter.getValue());
                assertEquals(((JLabel) width.getComponent(0)).getText(), "-");
                assertEquals(((JComboBox<?>) field(panel, "capComboBox")).getSelectedIndex(), -1);
                assertEquals(((JComboBox<?>) field(panel, "scaleComboBox")).getSelectedIndex(), -1);
                Object colorEditor = field(field(panel, "strokeEditor"), "colorEditor");
                TextPropertyField hexField = (TextPropertyField) field(colorEditor, "hexField");
                assertNull(hexField.getValue());
                assertEquals(((JLabel) hexField.getComponent(0)).getText(), "-");
                width.setValue(7f);
                assertEquals(edits.get(), 1);
                LINESTYLE2 changedFirst = panel.applyStrokeChange(first, lines.get(0).area.getBounds2D(), "width");
                LINESTYLE2 changedSecond = panel.applyStrokeChange(second, lines.get(1).area.getBounds2D(), "width");
                assertEquals(changedFirst.width, 140);
                assertEquals(changedSecond.width, 140);
                assertEquals(changedFirst.color.toColor(), Color.red);
                assertEquals(changedSecond.color.toColor(), Color.blue);
                assertFalse(changedFirst.noHScaleFlag);
                assertTrue(changedSecond.noHScaleFlag);
                assertEquals(changedFirst.startCapStyle, LINESTYLE2.ROUND_CAP);
                assertEquals(changedSecond.startCapStyle, LINESTYLE2.SQUARE_CAP);
                assertEquals(changedSecond.miterLimitFactor, 9f);
                panel.setSelections(Collections.singletonList(lines.get(0)));
                assertEquals(width.getValue(), Float.valueOf(2));
                assertEquals(edits.get(), 1);
                panel.setSelections(Arrays.asList(at(model, 500, 500), lines.get(0), lines.get(1)));
                assertTrue(((JComboBox<?>) field(field(panel, "fillEditor"), "typeComboBox")).isEnabled());
                assertTrue(((JComboBox<?>) field(field(panel, "strokeEditor"), "typeComboBox")).isEnabled());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void gradientColorsCanBeUnifiedWithoutOverwritingIndividualMatrices() throws Exception {
        DefineShape4Tag shape = shape();
        FILLSTYLE first = fill(Color.red);
        first.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
        first.gradient = new GRADIENT();
        first.gradient.gradientRecords = new GRADRECORD[2];
        for (int i = 0; i < 2; i++) {
            GRADRECORD stop = new GRADRECORD();
            stop.ratio = i * 255;
            stop.color = new RGBA(i == 0 ? Color.red : Color.black);
            first.gradient.gradientRecords[i] = stop;
        }
        first.gradientMatrix = new MATRIX();
        first.gradientMatrix.translateX = 100;
        FILLSTYLE second = Helper.deepCopy(first);
        second.gradient.gradientRecords[0].color = new RGBA(Color.blue);
        second.gradientMatrix.translateX = 900;
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{first, second};
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        rectangle(shape, 2000, 0, 1000, 1000, 2, 0);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> items = Arrays.asList(at(model, 500, 500), at(model, 2500, 500));
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(shape);
                panel.setSelections(items);
                Object fillEditor = field(panel, "fillEditor");
                GradientEditor gradient = (GradientEditor) field(fillEditor, "gradientEditor");
                assertEquals(field(gradient, "indeterminate"), Boolean.TRUE);
                @SuppressWarnings("unchecked")
                List<Color> colors = (List<Color>) field(gradient, "colors");
                colors.set(0, Color.green);
                FILLSTYLE a = panel.applyFillChange(first, items.get(0).geometry.getBounds2D(), "gradient");
                FILLSTYLE b = panel.applyFillChange(second, items.get(1).geometry.getBounds2D(), "gradient");
                assertEquals(a.gradient.gradientRecords[0].color.toColor(), Color.green);
                assertEquals(b.gradient.gradientRecords[0].color.toColor(), Color.green);
                assertEquals(a.gradientMatrix.translateX, 100);
                assertEquals(b.gradientMatrix.translateX, 900);
                assertEquals(first.gradient.gradientRecords[0].color.toColor(), Color.red);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void mixedBitmapIdsAndTypesAreIndeterminateAndBitmapChangesPreserveMatrices() throws Exception {
        DefineShape4Tag shape = shape();
        DefineBitsLossless2Tag imageA = new DefineBitsLossless2Tag(shape.getSwf());
        shape.getSwf().addTag(imageA);
        DefineBitsLossless2Tag imageB = new DefineBitsLossless2Tag(shape.getSwf());
        shape.getSwf().addTag(imageB);
        FILLSTYLE first = fill(Color.red);
        first.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
        first.bitmapId = imageA.getCharacterId();
        first.bitmapMatrix = new MATRIX();
        first.bitmapMatrix.translateX = 100;
        FILLSTYLE second = Helper.deepCopy(first);
        second.bitmapId = imageB.getCharacterId();
        second.bitmapMatrix.translateX = 900;
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{first, second, fill(Color.blue)};
        rectangle(shape, 0, 0, 1000, 1000, 1, 0);
        rectangle(shape, 2000, 0, 1000, 1000, 2, 0);
        rectangle(shape, 4000, 0, 1000, 1000, 3, 0);
        ShapeSelection model = selection(shape);
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(shape);
                panel.setSelections(Arrays.asList(at(model, 500, 500), at(model, 2500, 500)));
                Object editor = field(panel, "fillEditor");
                JComboBox<?> bitmap = (JComboBox<?>) field(editor, "bitmapComboBox");
                assertEquals(bitmap.getSelectedIndex(), -1);
                bitmap.setSelectedIndex(0);
                FILLSTYLE a = panel.applyFillChange(first, at(model, 500, 500).geometry.getBounds2D(), "bitmap");
                FILLSTYLE b = panel.applyFillChange(second, at(model, 2500, 500).geometry.getBounds2D(), "bitmap");
                assertEquals(a.bitmapId, imageA.getCharacterId());
                assertEquals(b.bitmapId, imageA.getCharacterId());
                assertEquals(a.bitmapMatrix.translateX, 100);
                assertEquals(b.bitmapMatrix.translateX, 900);
                panel.setSelections(Arrays.asList(at(model, 500, 500), at(model, 4500, 500)));
                assertEquals(((JComboBox<?>) field(editor, "typeComboBox")).getSelectedIndex(), -1);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void bitmapSmoothingPreservesWrappingAndMatricesForMixedSelectionsWithUndo() throws Exception {
        DefineShape4Tag shape = shape();
        DefineBitsLossless2Tag image = new DefineBitsLossless2Tag(shape.getSwf());
        image.setCharacterId(shape.shapeId + 1);
        shape.getSwf().addTag(image);
        int[] types = {FILLSTYLE.REPEATING_BITMAP, FILLSTYLE.CLIPPED_BITMAP,
            FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP, FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP};
        FILLSTYLE[] fills = new FILLSTYLE[types.length];
        for (int i = 0; i < types.length; i++) {
            FILLSTYLE paint = new FILLSTYLE();
            paint.fillStyleType = types[i];
            paint.bitmapId = image.getCharacterId();
            paint.bitmapMatrix = new MATRIX();
            paint.bitmapMatrix.translateX = i * 100;
            fills[i] = paint;
            rectangle(shape, i * 2000, 0, 1000, 1000, i + 1, 0);
        }
        shape.shapes.fillStyles.fillStyles = fills;
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> selected = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            selected.add(at(model, i * 2000 + 500, 500));
        }
        addClean(shape);
        byte[] before = shape.getData();
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(shape);
                panel.setSelections(selected);
                JTriStateCheckBox smoothed = (JTriStateCheckBox) field(field(panel, "fillEditor"), "smoothedCheckBox");
                JTriStateCheckBox repeat = (JTriStateCheckBox) field(field(panel, "fillEditor"), "repeatCheckBox");
                assertEquals(smoothed.getSelectionState(), 1);
                assertEquals(repeat.getSelectionState(), 1);
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertFalse(stroke);
                    assertTrue(property.equals("smoothed") || property.equals("repeat"));
                    edits.incrementAndGet();
                });
                smoothed.doClick();
                ShapeSelection.BatchEdit enabled = model.changeAll(selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "smoothed"), null);
                assertTrue(enabled.changed);
                panel.setSelections(enabled.selected);
                assertEquals(smoothed.getSelectionState(), 2);
                smoothed.doClick();
                ShapeSelection.BatchEdit disabled = enabled.model.changeAll(enabled.selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "smoothed"), null);
                assertTrue(disabled.changed);
                UndoManager undo = new UndoManager();
                undo.doOperation(new ShapeStyleChangeOperation(shape, disabled.model, "Smoothed", null), shape.getSwf());
                ShapeSelection saved = roundTrip(current(shape));
                for (int i = 0; i < types.length; i++) {
                    FILLSTYLE smoothFill = at(enabled.model, i * 2000 + 500, 500).fill;
                    assertEquals(smoothFill.fillStyleType, i % 2 == 0 ? FILLSTYLE.REPEATING_BITMAP : FILLSTYLE.CLIPPED_BITMAP);
                    FILLSTYLE savedFill = at(saved, i * 2000 + 500, 500).fill;
                    assertEquals(savedFill.fillStyleType, i % 2 == 0
                            ? FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP : FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                    assertEquals(savedFill.bitmapId, fills[i].bitmapId);
                    assertEquals(savedFill.bitmapMatrix, fills[i].bitmapMatrix);
                    assertEquals(fills[i].fillStyleType, types[i]);
                }
                byte[] after = current(shape).getData();
                undo.undo(shape.getSwf());
                assertEquals(current(shape).getData(), before);
                undo.redo(shape.getSwf());
                assertEquals(current(shape).getData(), after);
                assertEquals(edits.get(), 2);
                panel.setSelections(selected);
                assertEquals(repeat.getSelectionState(), 1);
                repeat.doClick();
                ShapeSelection.BatchEdit repeated = model.changeAll(selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "repeat"), null);
                assertTrue(repeated.changed);
                panel.setSelections(repeated.selected);
                assertEquals(repeat.getSelectionState(), 2);
                assertEquals(smoothed.getSelectionState(), 1);
                repeat.doClick();
                ShapeSelection.BatchEdit clipped = repeated.model.changeAll(repeated.selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "repeat"), null);
                assertTrue(clipped.changed);
                undo.doOperation(new ShapeStyleChangeOperation(current(shape), clipped.model, "Repeat", null), shape.getSwf());
                saved = roundTrip(current(shape));
                for (int i = 0; i < types.length; i++) {
                    assertEquals(at(repeated.model, i * 2000 + 500, 500).fill.fillStyleType,
                            i < 2 ? FILLSTYLE.REPEATING_BITMAP : FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP);
                    FILLSTYLE savedFill = at(saved, i * 2000 + 500, 500).fill;
                    assertEquals(savedFill.fillStyleType, i < 2 ? FILLSTYLE.CLIPPED_BITMAP : FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP);
                    assertEquals(savedFill.bitmapId, fills[i].bitmapId);
                    assertEquals(savedFill.bitmapMatrix, fills[i].bitmapMatrix);
                }
                byte[] clippedBytes = current(shape).getData();
                undo.undo(shape.getSwf());
                assertEquals(current(shape).getData(), after);
                undo.redo(shape.getSwf());
                assertEquals(current(shape).getData(), clippedBytes);
                assertEquals(edits.get(), 4);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void selectedEdgesAcrossTheClosingVertexRemainJoinedAndCanBeDeletedAndRestored() {
        DefineShape4Tag shape = shape();
        shape.shapes.lineStyles.lineStyles2[0].joinStyle = LINESTYLE2.MITER_JOIN;
        shape.shapes.lineStyles.lineStyles2[0].miterLimitFactor = 4;
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        List<ShapeSelection.Selection> edges = Arrays.asList(at(model, 1000, 0), at(model, 0, 1000));
        ShapeSelection.BatchEdit edit = model.changeAll(edges, null, item -> {
            LINESTYLE2 line = Helper.deepCopy((LINESTYLE2) item.line);
            line.width = 400;
            return line;
        });
        assertTrue(edit.model.getParts().stream().anyMatch(part -> part.line != null && part.line.getWidth() == 400
                && part.area.contains(-150, -150)));
        ShapeSelection.BatchEdit removed = edit.model.changeAll(edit.selected, null, item -> null);
        assertEquals(removed.selected.size(), 2);
        assertNull(removed.selected.get(0).line);
        assertNull(removed.selected.get(1).line);
        ShapeSelection.BatchEdit restored = removed.model.changeAll(removed.selected, null, item -> shape.shapes.lineStyles.lineStyles2[0]);
        assertEquals(at(restored.model, 1000, 0).line.getWidth(), 200);
        assertEquals(at(restored.model, 0, 1000).line.getWidth(), 200);
        assertEquals(ShapeSelection.updateSelection(restored.selected, at(restored.model, 0, 1000), true).size(), 1);
    }

    @Test
    public void deletingARectangularSelectionClearsItAndUndoRestoresTheExactShape() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        rectangle(shape, 3000, 0, 1000, 1000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        byte[] before = shape.getData();
        ShapeSelection.RectangleSelection box = model.selectRectangle(new Rectangle2D.Double(500, -200, 1000, 1000),
                Collections.emptyList(), false);
        assertEquals(box.selected.size(), 2);
        ShapeSelection.BatchEdit deleted = box.model.deleteAll(box.selected);
        assertTrue(deleted.changed);
        assertTrue(deleted.selected.isEmpty());
        UndoManager history = new UndoManager();
        history.doOperation(new ShapeStyleChangeOperation(shape, deleted.model, "Delete", null), shape.getSwf());
        ShapeSelection restored = roundTrip(current(shape));
        assertNull(at(restored, 1000, 400));
        assertNull(at(restored, 1000, 0));
        assertEquals(at(restored, 250, 400).fill.color.toColor(), Color.red);
        assertEquals(at(restored, 250, 0).line.getWidth(), 200);
        assertEquals(at(restored, 3500, 500).fill.color.toColor(), Color.red);
        assertFalse(deleted.model.deleteAll(deleted.selected).changed);
        history.undo(shape.getSwf());
        assertEquals(current(shape).getData(), before);
        assertFalse(shape.getSwf().isModified());
        assertFalse(history.canUndo(shape.getSwf()));
        history.redo(shape.getSwf());
        assertNull(at(new ShapeSelection(current(shape)), 1000, 400));
    }

    @Test
    public void deletingAllContentsSavesAnEmptyShapeAndDeletingOneEdgePreservesItsFill() throws Exception {
        DefineShape4Tag shape = shape();
        rectangle(shape, 0, 0, 2000, 2000, 1, 1);
        ShapeSelection model = selection(shape);
        addClean(shape);
        ShapeSelection.BatchEdit edgeDeleted = model.deleteAll(Collections.singletonList(at(model, 1000, 0)));
        assertTrue(edgeDeleted.selected.isEmpty());
        assertFalse(at(edgeDeleted.model, 1000, 0).stroke);
        assertEquals(at(edgeDeleted.model, 1000, 500).fill.color.toColor(), Color.red);
        assertEquals(at(edgeDeleted.model, 0, 1000).line.getWidth(), 200);
        ShapeSelection.RectangleSelection all = model.selectRectangle(new Rectangle2D.Double(-500, -500, 3000, 3000),
                Collections.emptyList(), false);
        ShapeSelection.BatchEdit deleted = all.model.deleteAll(all.selected);
        assertTrue(deleted.selected.isEmpty());
        new ShapeStyleChangeOperation(shape, deleted.model, "Delete", null).doOperation();
        ShapeSelection restored = roundTrip(current(shape));
        assertNull(at(restored, 1000, 500));
        assertNull(at(restored, 1000, 0));
        assertTrue(restored.getParts().isEmpty());
    }
}
