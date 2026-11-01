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
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
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
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ShapePropertiesPanelTest {

    private Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private byte[] data(ShapeTag shape) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (SWFOutputStream output = new SWFOutputStream(bytes, shape.getSwf().version, shape.getSwf().getCharset())) {
            shape.getData(output);
        }
        return bytes.toByteArray();
    }

    private GRADIENT gradient() {
        GRADIENT gradient = new GRADIENT();
        GRADRECORD start = new GRADRECORD();
        start.inShape3 = true;
        start.ratio = 30;
        start.color = new RGBA(255, 0, 0, 80);
        GRADRECORD end = new GRADRECORD();
        end.inShape3 = true;
        end.ratio = 220;
        end.color = new RGBA(0, 0, 255, 200);
        gradient.gradientRecords = new GRADRECORD[]{start, end};
        return gradient;
    }

    @Test
    public void styleControlsDoNotChangeShapeData() throws Exception {
        SWF swf = new SWF();
        DefineShape4Tag shape = new DefineShape4Tag(swf);
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
        fill.gradientMatrix = new MATRIX();
        fill.gradient = gradient();
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill};
        LINESTYLE2 line = new LINESTYLE2();
        line.width = 60;
        line.hasFillFlag = true;
        line.fillType = fill;
        line.noVScaleFlag = true;
        line.joinStyle = LINESTYLE2.MITER_JOIN;
        line.miterLimitFactor = 4;
        shape.shapes.lineStyles.lineStyles2 = new LINESTYLE2[]{line};
        shape.setModified(false);
        byte[] before = data(shape);
        boolean modified = shape.isModified();

        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(shape);
                panel.setSelectedStroke(line);
                assertEquals(((FloatPropertyField) field(panel, "widthEditor")).getValue(), Float.valueOf(3f));
                assertEquals(((JComboBox<?>) field(panel, "scaleComboBox")).getSelectedIndex(), 1);
                for (String name : new String[]{"fillEditor", "strokeEditor"}) {
                    if (name.equals("fillEditor")) {
                        panel.setSelectedFill(fill);
                    } else {
                        panel.setSelectedStroke(line);
                    }
                    Object fillEditor = field(panel, name);
                    JComboBox<?> type = (JComboBox<?>) field(fillEditor, "typeComboBox");
                    for (int i = 0; i < type.getItemCount(); i++) {
                        type.setSelectedIndex(i);
                    }
                    JPanel gradientPanel = (JPanel) field(fillEditor, "gradientPanel");
                    GradientEditor editor = (GradientEditor) gradientPanel.getComponent(0);
                    @SuppressWarnings("unchecked")
                    List<Color> colors = (List<Color>) field(editor, "colors");
                    colors.set(0, Color.green);
                    editor.save();
                }
                ((FloatPropertyField) field(panel, "widthEditor")).setValue(7f);
                ((FloatPropertyField) field(panel, "miterEditor")).setValue(8f);
                for (String name : new String[]{"scaleComboBox", "capComboBox", "joinComboBox"}) {
                    ((JComboBox<?>) field(panel, name)).setSelectedIndex(2);
                }
                assertEquals(data(shape), before);
                assertEquals(shape.isModified(), modified);
                assertEquals(fill.gradient.gradientRecords[0].color, new RGBA(255, 0, 0, 80));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void selectedFillAndStrokeAreMutuallyExclusive() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(new DefineShape4Tag(new SWF()));
                JComboBox<?> fillType = (JComboBox<?>) field(field(panel, "fillEditor"), "typeComboBox");
                JComboBox<?> strokeType = (JComboBox<?>) field(field(panel, "strokeEditor"), "typeComboBox");
                assertEquals(fillType.getSelectedIndex(), 0);
                assertEquals(strokeType.getSelectedIndex(), 0);
                FILLSTYLE fill = new FILLSTYLE();
                fill.fillStyleType = FILLSTYLE.RADIAL_GRADIENT;
                fill.gradient = gradient();
                panel.setSelectedFill(fill);
                assertEquals(fillType.getSelectedIndex(), 3);
                assertEquals(strokeType.getSelectedIndex(), 0);
                assertTrue(fillType.isEnabled());
                assertFalse(strokeType.isEnabled());
                assertFalse(((FloatPropertyField) field(panel, "widthEditor")).isEnabled());
                assertFalse(((FloatPropertyField) field(panel, "miterEditor")).isEnabled());
                for (String name : new String[]{"scaleComboBox", "capComboBox", "joinComboBox"}) {
                    assertFalse(((JComboBox<?>) field(panel, name)).isEnabled());
                }
                LINESTYLE2 line = new LINESTYLE2();
                line.color = new RGBA(Color.black);
                line.width = 100;
                line.noHScaleFlag = true;
                line.startCapStyle = LINESTYLE2.SQUARE_CAP;
                line.joinStyle = LINESTYLE2.MITER_JOIN;
                line.miterLimitFactor = 6;
                panel.setSelectedStroke(line);
                assertEquals(fillType.getSelectedIndex(), 0);
                assertEquals(strokeType.getSelectedIndex(), 1);
                assertFalse(fillType.isEnabled());
                assertTrue(strokeType.isEnabled());
                assertTrue(((FloatPropertyField) field(panel, "widthEditor")).isEnabled());
                assertTrue(((FloatPropertyField) field(panel, "miterEditor")).isEnabled());
                assertEquals(((FloatPropertyField) field(panel, "widthEditor")).getValue(), Float.valueOf(5));
                assertEquals(((FloatPropertyField) field(panel, "miterEditor")).getValue(), Float.valueOf(6));
                assertEquals(((JComboBox<?>) field(panel, "scaleComboBox")).getSelectedIndex(), 2);
                assertEquals(((JComboBox<?>) field(panel, "capComboBox")).getSelectedIndex(), 2);
                assertEquals(((JComboBox<?>) field(panel, "joinComboBox")).getSelectedIndex(), 2);
                // A selected region remains the same kind when its style is None.
                panel.setSelectedStyles(null, null, null, true);
                assertFalse(fillType.isEnabled());
                assertEquals(fillType.getSelectedIndex(), 0);
                assertTrue(strokeType.isEnabled());
                panel.setSelectedStyles(null, null, null, false);
                assertTrue(fillType.isEnabled());
                assertFalse(strokeType.isEnabled());
                assertEquals(strokeType.getSelectedIndex(), 0);
                panel.setSelectedFill(null);
                assertEquals(fillType.getSelectedIndex(), 0);
                assertEquals(strokeType.getSelectedIndex(), 0);
                assertTrue(fillType.isEnabled());
                assertTrue(strokeType.isEnabled());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void loadingDoesNotEmitEditsAndUnexposedStrokeFlagsArePreserved() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                AtomicInteger edits = new AtomicInteger();
                panel.setChangeListener(stroke -> edits.incrementAndGet());
                panel.setShape(new DefineShape4Tag(new SWF()));
                LINESTYLE2 line = new LINESTYLE2();
                line.color = new RGBA(Color.black);
                line.width = 120;
                line.pixelHintingFlag = true;
                line.noClose = true;
                line.endCapStyle = LINESTYLE2.SQUARE_CAP;
                line.noHScaleFlag = true;
                line.joinStyle = LINESTYLE2.MITER_JOIN;
                line.miterLimitFactor = 7;
                panel.setSelectedStroke(line);
                assertEquals(edits.get(), 0);
                assertEquals(panel.getStrokeStyle(), line);
                ((FloatPropertyField) field(panel, "widthEditor")).setValue(8f);
                assertEquals(edits.get(), 1);
                LINESTYLE2 changed = panel.getStrokeStyle();
                assertEquals(changed.width, 160);
                assertTrue(changed.pixelHintingFlag);
                assertTrue(changed.noClose);
                assertEquals(changed.endCapStyle, LINESTYLE2.SQUARE_CAP);
                assertEquals(line.width, 120);
                panel.setSelectedFill(null);
                assertEquals(edits.get(), 1);
                JComboBox<?> type = (JComboBox<?>) field(field(panel, "fillEditor"), "typeComboBox");
                type.setSelectedIndex(2);
                assertEquals(edits.get(), 2);
                FILLSTYLE fill = panel.getFillStyle();
                assertEquals(fill.fillStyleType, FILLSTYLE.LINEAR_GRADIENT);
                assertNotNull(fill.gradientMatrix);
                assertTrue(fill.gradientMatrix.scaleX > 0);
                assertEquals(fill.gradient.gradientRecords.length, 2);
                panel.setSelectedFill(fill);
                ((FloatPropertyField) field(panel, "widthEditor")).setValue(9f);
                assertEquals(edits.get(), 2);
                assertNull(panel.getStrokeStyle());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void bitmapChoicesFollowTheCurrentSwf() throws Exception {
        SWF withImages = new SWF();
        withImages.addTag(new DefineBitsLossless2Tag(withImages));
        withImages.addTag(new DefineBitsLossless2Tag(withImages));
        SWF empty = new SWF();
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                panel.setShape(new DefineShape4Tag(withImages));
                for (String name : new String[]{"fillEditor", "strokeEditor"}) {
                    JComboBox<?> bitmaps = (JComboBox<?>) field(field(panel, name), "bitmapComboBox");
                    assertEquals(bitmaps.getItemCount(), 2);
                    assertTrue(bitmaps.isEnabled());
                }
                panel.setShape(new DefineShape4Tag(empty));
                for (String name : new String[]{"fillEditor", "strokeEditor"}) {
                    JComboBox<?> bitmaps = (JComboBox<?>) field(field(panel, name), "bitmapComboBox");
                    assertEquals(bitmaps.getItemCount(), 0);
                    assertFalse(bitmaps.isEnabled());
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void shapeGradientEditorCannotExceedTheSwfStopLimit() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                GRADIENT gradient = gradient();
                GRADRECORD[] stops = new GRADRECORD[15];
                for (int i = 0; i < stops.length; i++) {
                    GRADRECORD stop = new GRADRECORD();
                    stop.color = new RGBA(Color.red);
                    stop.ratio = Math.round(i * 255f / 14);
                    stops[i] = stop;
                }
                gradient.gradientRecords = stops;
                GradientEditor editor = new GradientEditor(gradient);
                Class<?> panelClass = Class.forName(GradientEditor.class.getName() + "$GradientEditorPanel");
                Constructor<?> constructor = panelClass.getDeclaredConstructor(GradientEditor.class);
                constructor.setAccessible(true);
                JPanel controls = (JPanel) constructor.newInstance(editor);
                controls.setSize(300, 50);
                controls.dispatchEvent(new MouseEvent(controls, MouseEvent.MOUSE_PRESSED, 0, 0, 150, 10, 1, false, MouseEvent.BUTTON1));
                editor.save();
                assertEquals(gradient.gradientRecords.length, 15);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void shapeGradientsKeepAlphaAndProvideAnEditableDefault() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GRADIENT gradient = gradient();
            GradientEditor editor = new GradientEditor(gradient);
            editor.save();
            assertEquals(gradient.gradientRecords[0].ratio, 30);
            assertEquals(gradient.gradientRecords[0].color, new RGBA(255, 0, 0, 80));
            assertEquals(gradient.gradientRecords[1].ratio, 220);
            assertEquals(gradient.gradientRecords[1].color, new RGBA(0, 0, 255, 200));
            GRADIENT empty = new GRADIENT();
            new GradientEditor(empty).save();
            assertEquals(empty.gradientRecords.length, 2);
            assertEquals(empty.gradientRecords[0].ratio, 0);
            assertEquals(empty.gradientRecords[1].ratio, 255);
        });
    }
}
