/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import static com.jpexs.decompiler.flash.easygui.ShapeEyedropperTest.gradient;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.rectangle;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.selection;
import static com.jpexs.decompiler.flash.easygui.ShapeSelectionTest.shape;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.easygui.properties.JTriStateCheckBox;
import com.jpexs.decompiler.flash.easygui.properties.panels.BrushPropertiesPanel;
import com.jpexs.decompiler.flash.easygui.properties.panels.ShapePropertiesPanel;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;

public class ShapeGradientSettingsTest {

    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static ShapeSelection.Selection at(ShapeSelection model, int x, int y) {
        return model.select(new Point2D.Double(x, y), 0);
    }

    @Test
    public void mixedRadialFillsToggleFocalTypeWithoutChangingOtherSettingsAndSupportUndo() throws Exception {
        DefineShape4Tag fixture = shape();
        FILLSTYLE radial = gradient(FILLSTYLE.RADIAL_GRADIENT);
        FILLSTYLE focal = gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
        fixture.shapes.fillStyles.fillStyles = new FILLSTYLE[]{radial, focal};
        rectangle(fixture, 0, 0, 1000, 1000, 1, 0);
        rectangle(fixture, 2000, 0, 1000, 1000, 2, 0);
        ShapeSelection model = selection(fixture);
        List<ShapeSelection.Selection> selected = Arrays.asList(at(model, 500, 500), at(model, 2500, 500));
        AtomicReference<ShapeSelection.BatchEdit> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(fixture);
                panel.setSelections(selected);
                Object editor = field(panel, "fillEditor");
                JTriStateCheckBox checkbox = (JTriStateCheckBox) field(editor, "focalPointCheckBox");
                assertTrue(checkbox.isVisible());
                assertEquals(checkbox.getSelectionState(), 1);
                checkbox.doClick();
                assertEquals(checkbox.getSelectionState(), 2);
                ShapeSelection.BatchEdit enabled = model.changeAll(selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "focalPoint"), null);
                assertTrue(enabled.changed);
                for (int x : new int[]{500, 2500}) {
                    FILLSTYLE original = x == 500 ? radial : focal;
                    FILLSTYLE changed = at(enabled.model, x, 500).fill;
                    assertEquals(changed.fillStyleType, FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                    assertEquals(changed.gradient.getClass(), FOCALGRADIENT.class);
                    assertEquals(((FOCALGRADIENT) changed.gradient).focalPoint,
                            x == 500 ? 0f : ((FOCALGRADIENT) focal.gradient).focalPoint);
                    assertEquals(changed.gradientMatrix, original.gradientMatrix);
                    assertEquals(changed.gradient.gradientRecords, original.gradient.gradientRecords);
                    assertEquals(changed.gradient.spreadMode, original.gradient.spreadMode);
                    assertEquals(changed.gradient.interpolationMode, original.gradient.interpolationMode);
                }
                panel.setSelections(enabled.selected);
                checkbox.doClick();
                ShapeSelection.BatchEdit disabled = enabled.model.changeAll(enabled.selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "focalPoint"), null);
                assertTrue(disabled.changed);
                for (int x : new int[]{500, 2500}) {
                    FILLSTYLE changed = at(disabled.model, x, 500).fill;
                    assertEquals(changed.fillStyleType, FILLSTYLE.RADIAL_GRADIENT);
                    assertEquals(changed.gradient.getClass(), GRADIENT.class);
                }
                assertEquals(radial.gradient.getClass(), GRADIENT.class);
                assertEquals(((FOCALGRADIENT) focal.gradient).focalPoint, 0.375f);
                result.set(enabled);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        fixture.getSwf().addTag(fixture);
        byte[] before = fixture.getData();
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(fixture, result.get().model, "Focal point", null), fixture.getSwf());
        byte[] after = ((ShapeTag) fixture.getSwf().getCharacter(fixture.shapeId)).getData();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        fixture.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeSelection saved = new ShapeSelection((ShapeTag) restored.getCharacter(fixture.shapeId));
        assertEquals(((FOCALGRADIENT) at(saved, 500, 500).fill.gradient).focalPoint, 0f);
        assertEquals(((FOCALGRADIENT) at(saved, 2500, 500).fill.gradient).focalPoint, 0.375f);
        undo.undo(fixture.getSwf());
        assertEquals(((ShapeTag) fixture.getSwf().getCharacter(fixture.shapeId)).getData(), before);
        undo.redo(fixture.getSwf());
        assertEquals(((ShapeTag) fixture.getSwf().getCharacter(fixture.shapeId)).getData(), after);
    }

    @Test
    public void radialStrokeFocalCheckboxChangesOnlyTheGradientType() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel(true);
                LINESTYLE2 line = new LINESTYLE2();
                line.width = 120;
                line.hasFillFlag = true;
                line.fillType = gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                line.noHScaleFlag = true;
                line.noClose = true;
                panel.setSelectedStroke(line);
                Object editor = field(panel, "strokeEditor");
                JTriStateCheckBox checkbox = (JTriStateCheckBox) field(editor, "focalPointCheckBox");
                assertTrue(checkbox.isVisible());
                assertEquals(checkbox.getSelectionState(), 2);
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertTrue(stroke);
                    assertEquals(property, "focalPoint");
                    edits.incrementAndGet();
                });
                checkbox.doClick();
                LINESTYLE2 changed = panel.applyStrokeChange(line, new Rectangle2D.Double(), "focalPoint");
                LINESTYLE2 expected = Helper.deepCopy(line);
                expected.fillType.fillStyleType = FILLSTYLE.RADIAL_GRADIENT;
                expected.fillType.fieldChanged(null);
                assertEquals(changed, expected);
                assertEquals(changed.fillType.gradient.getClass(), GRADIENT.class);
                panel.setSelectedStroke(changed);
                checkbox.doClick();
                LINESTYLE2 enabled = panel.applyStrokeChange(changed, new Rectangle2D.Double(), "focalPoint");
                assertEquals(enabled.fillType.fillStyleType, FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                assertEquals(((FOCALGRADIENT) enabled.fillType.gradient).focalPoint, 0f);
                assertEquals(edits.get(), 2);
                assertEquals(line.fillType.fillStyleType, FILLSTYLE.FOCAL_RADIAL_GRADIENT);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void mixedFillSettingsChangeIndependentlyAndRoundTripWithUndo() throws Exception {
        DefineShape4Tag fixture = shape();
        FILLSTYLE first = gradient(FILLSTYLE.LINEAR_GRADIENT);
        first.gradient.interpolationMode = GRADIENT.INTERPOLATION_RGB_MODE;
        first.gradient.spreadMode = GRADIENT.SPREAD_PAD_MODE;
        FILLSTYLE second = Helper.deepCopy(first);
        second.fillStyleType = FILLSTYLE.RADIAL_GRADIENT;
        second.gradient.spreadMode = GRADIENT.SPREAD_REFLECT_MODE;
        second.gradient.interpolationMode = GRADIENT.INTERPOLATION_LINEAR_RGB_MODE;
        second.gradientMatrix.translateX += 700;
        second.gradient.gradientRecords[0].color = new RGBA(10, 20, 30, 40);
        fixture.shapes.fillStyles.fillStyles = new FILLSTYLE[]{first, second};
        rectangle(fixture, 0, 0, 1000, 1000, 1, 0);
        rectangle(fixture, 2000, 0, 1000, 1000, 2, 0);
        ShapeSelection model = selection(fixture);
        List<ShapeSelection.Selection> selected = Arrays.asList(at(model, 500, 500), at(model, 2500, 500));
        AtomicReference<ShapeSelection.BatchEdit> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> edits.incrementAndGet());
                panel.setShape(fixture);
                panel.setSelections(selected);
                assertEquals(edits.get(), 0);
                Object editor = field(panel, "fillEditor");
                JComboBox<?> flow = (JComboBox<?>) field(editor, "flowComboBox");
                JTriStateCheckBox linear = (JTriStateCheckBox) field(editor, "linearRGBCheckBox");
                // Different gradient kinds still expose their shared controls.
                assertTrue(((JPanel) field(editor, "gradientPanel")).isVisible());
                assertEquals(flow.getSelectedIndex(), -1);
                assertEquals(linear.getSelectionState(), 1);
                flow.setSelectedIndex(GRADIENT.SPREAD_REPEAT_MODE);
                ShapeSelection.BatchEdit repeated = model.changeAll(selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "flow"), null);
                assertEquals(at(repeated.model, 500, 500).fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_RGB_MODE);
                assertEquals(at(repeated.model, 2500, 500).fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
                panel.setSelections(repeated.selected);
                assertEquals(linear.getSelectionState(), 1);
                linear.doClick();
                assertEquals(linear.getSelectionState(), 2);
                ShapeSelection.BatchEdit linearEdit = repeated.model.changeAll(repeated.selected,
                        item -> panel.applyFillChange(item.fill, item.geometry.getBounds2D(), "linearRGB"), null);
                for (int x : new int[]{500, 2500}) {
                    FILLSTYLE changed = at(linearEdit.model, x, 500).fill;
                    FILLSTYLE original = x == 500 ? first : second;
                    assertEquals(changed.gradient.spreadMode, GRADIENT.SPREAD_REPEAT_MODE);
                    assertEquals(changed.gradient.interpolationMode, GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
                    assertEquals(changed.gradientMatrix, original.gradientMatrix);
                    assertEquals(changed.gradient.gradientRecords, original.gradient.gradientRecords);
                    assertEquals(changed.fillStyleType, original.fillStyleType);
                }
                assertEquals(edits.get(), 2);
                result.set(linearEdit);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        // These options require DefineShape4 / SWF 8, including on an older shape.
        DefineShape3Tag original = new DefineShape3Tag(fixture.getSwf());
        original.shapeId = fixture.shapeId;
        original.shapes = fixture.shapes;
        original.getSwf().version = 7;
        original.getSwf().addTag(original);
        byte[] before = original.getData();
        UndoManager undo = new UndoManager();
        undo.doOperation(new ShapeStyleChangeOperation(original, result.get().model, "Gradient", null), original.getSwf());
        ShapeTag after = (ShapeTag) original.getSwf().getCharacter(original.shapeId);
        assertEquals(after.getShapeNum(), 4);
        assertEquals(original.getSwf().version, 8);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        original.getSwf().saveTo(bytes);
        SWF restored = new SWF(new ByteArrayInputStream(bytes.toByteArray()), false);
        ShapeSelection saved = new ShapeSelection((ShapeTag) restored.getCharacter(original.shapeId));
        for (int x : new int[]{500, 2500}) {
            assertEquals(at(saved, x, 500).fill.gradient.spreadMode, GRADIENT.SPREAD_REPEAT_MODE);
            assertEquals(at(saved, x, 500).fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
        }
        undo.undo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), before);
        assertEquals(original.getSwf().version, 7);
        undo.redo(original.getSwf());
        assertEquals(((ShapeTag) original.getSwf().getCharacter(original.shapeId)).getData(), after.getData());
    }

    @Test
    public void strokeSettingsPreserveTheMatrixStopsAndLineParameters() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel(true);
                LINESTYLE2 line = new LINESTYLE2();
                line.width = 120;
                line.hasFillFlag = true;
                line.fillType = gradient(FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                line.noHScaleFlag = true;
                line.noClose = true;
                panel.setSelectedStroke(line);
                Object editor = field(panel, "strokeEditor");
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertTrue(stroke);
                    edits.incrementAndGet();
                });
                ((JComboBox<?>) field(editor, "flowComboBox")).setSelectedIndex(GRADIENT.SPREAD_REFLECT_MODE);
                LINESTYLE2 reflected = panel.applyStrokeChange(line, new Rectangle2D.Double(), "flow");
                ((JTriStateCheckBox) field(editor, "linearRGBCheckBox")).setSelected(false);
                ((JTriStateCheckBox) field(editor, "linearRGBCheckBox")).doClick();
                LINESTYLE2 changed = panel.applyStrokeChange(reflected, new Rectangle2D.Double(), "linearRGB");
                LINESTYLE2 expected = Helper.deepCopy(line);
                expected.fillType.gradient.spreadMode = GRADIENT.SPREAD_REFLECT_MODE;
                expected.fillType.gradient.interpolationMode = GRADIENT.INTERPOLATION_LINEAR_RGB_MODE;
                assertEquals(changed, expected);
                assertEquals(edits.get(), 2);
                assertFalse(line.fillType.gradient.spreadMode == GRADIENT.SPREAD_REFLECT_MODE);
                panel.setSelectedStroke(changed);
                assertEquals(panel.getStrokeStyle(), changed);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void sharedDrawingAndBrushEditorsUseTheChosenSettingsForNewFills() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                for (boolean brush : new boolean[]{false, true}) {
                    ShapePropertiesPanel styles = ShapePropertiesPanel.createDrawingStyles();
                    BrushPropertiesPanel brushPanel = new BrushPropertiesPanel();
                    Object editor = field(brush ? brushPanel : styles, "fillEditor");
                    JComboBox<?> type = (JComboBox<?>) field(editor, "typeComboBox");
                    for (int gradientType : new int[]{2, 3}) {
                        type.setSelectedIndex(gradientType - (brush ? 1 : 0));
                        for (int flow : new int[]{GRADIENT.SPREAD_PAD_MODE, GRADIENT.SPREAD_REFLECT_MODE, GRADIENT.SPREAD_REPEAT_MODE}) {
                            ((JComboBox<?>) field(editor, "flowComboBox")).setSelectedIndex(flow);
                            JTriStateCheckBox linear = (JTriStateCheckBox) field(editor, "linearRGBCheckBox");
                            linear.setSelected(false);
                            linear.doClick();
                            FILLSTYLE fill = brush ? brushPanel.getFillStyle(new Rectangle2D.Double(0, 0, 1000, 1000)) : styles.getFillStyle();
                            assertEquals(fill.gradient.spreadMode, flow);
                            assertEquals(fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_LINEAR_RGB_MODE);
                            linear.doClick();
                            fill = brush ? brushPanel.getFillStyle(new Rectangle2D.Double(0, 0, 1000, 1000)) : styles.getFillStyle();
                            assertEquals(fill.gradient.interpolationMode, GRADIENT.INTERPOLATION_RGB_MODE);
                        }
                        JTriStateCheckBox focal = (JTriStateCheckBox) field(editor, "focalPointCheckBox");
                        assertEquals(focal.isVisible(), gradientType == 3);
                        if (gradientType == 3) {
                            focal.doClick();
                            FILLSTYLE fill = brush ? brushPanel.getFillStyle(null) : styles.getFillStyle();
                            assertEquals(fill.fillStyleType, FILLSTYLE.FOCAL_RADIAL_GRADIENT);
                            assertEquals(fill.gradient.getClass(), FOCALGRADIENT.class);
                            type.setSelectedIndex(2 - (brush ? 1 : 0));
                            assertFalse(focal.isVisible());
                            fill = brush ? brushPanel.getFillStyle(null) : styles.getFillStyle();
                            assertEquals(fill.fillStyleType, FILLSTYLE.LINEAR_GRADIENT);
                            assertEquals(fill.gradient.getClass(), GRADIENT.class);
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
