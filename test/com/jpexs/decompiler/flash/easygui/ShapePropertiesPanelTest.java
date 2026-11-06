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
import com.jpexs.decompiler.flash.easygui.properties.AbstractPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.FloatPropertyField;
import com.jpexs.decompiler.flash.easygui.properties.GradientEditor;
import com.jpexs.decompiler.flash.easygui.properties.TextPropertyField;
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Constructor;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ShapePropertiesPanelTest {

    private Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private void editColorField(AbstractPropertyField<?> field, String value) {
        JLabel label = (JLabel) field.getComponent(0);
        label.dispatchEvent(new MouseEvent(label, MouseEvent.MOUSE_CLICKED, 0, 0, 1, 1, 1, false, MouseEvent.BUTTON1));
        JTextField input = (JTextField) field.getComponent(1);
        assertTrue(input.isVisible());
        input.setText(value);
        KeyEvent enter = new KeyEvent(input, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ENTER, '\n');
        for (KeyListener listener : input.getKeyListeners()) {
            listener.keyPressed(enter);
        }
        assertTrue(label.isVisible());
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
    public void positionAndSizeFieldsEditTheWholeSelection() throws Exception {
        DefineShape4Tag shape = ShapeSelectionTest.shape();
        ShapeSelectionTest.rectangle(shape, 200, -400, 600, 1000, 1, 0);
        ShapeSelectionTest.rectangle(shape, 1200, 0, 400, 600, 2, 0);
        ShapeSelection model = ShapeSelectionTest.selection(shape);
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                FloatPropertyField x = (FloatPropertyField) field(panel, "xField");
                FloatPropertyField y = (FloatPropertyField) field(panel, "yField");
                FloatPropertyField w = (FloatPropertyField) field(panel, "wField");
                FloatPropertyField h = (FloatPropertyField) field(panel, "hField");
                assertFalse(x.isEnabled());
                assertNull(x.getValue());

                AtomicReference<List<ShapeSelection.Selection>> selected = new AtomicReference<>(model.selectAll());
                AtomicReference<ShapeSelection> current = new AtomicReference<>(model);
                panel.setSelections(selected.get());
                panel.setSelectionBoundsListener(requested -> {
                    ShapeSelection.BatchEdit edit = current.get().transformFloatingSelection(selected.get(),
                            ShapeSelection.transformSelectionBounds(selected.get(), requested).toTransform());
                    selected.set(edit.selected);
                    current.set(edit.model);
                    panel.setSelections(edit.selected);
                });
                assertEquals(ShapeSelection.selectionBounds(selected.get()), new Rectangle2D.Double(200, -400, 1400, 1000));
                assertEquals(x.getValue(), Float.valueOf(10));
                assertEquals(y.getValue(), Float.valueOf(-20));
                assertEquals(w.getValue(), Float.valueOf(70));
                assertEquals(h.getValue(), Float.valueOf(50));

                editColorField(x, "-12.5");
                editColorField(y, "7.25");
                editColorField(w, "140");
                editColorField(h, "25");
                editColorField(w, "0");
                assertEquals(ShapeSelection.selectionBounds(selected.get()), new Rectangle2D.Double(-250, 145, 2800, 500));
                assertEquals(w.getValue(), Float.valueOf(140));
                assertEquals(h.getValue(), Float.valueOf(25));
                panel.setShape(shape);
                assertFalse(x.isEnabled());
                assertNull(x.getValue());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
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
                    GradientEditor editor = (GradientEditor) field(fillEditor, "gradientEditor");
                    @SuppressWarnings("unchecked")
                    List<Color> colors = (List<Color>) field(editor, "colors");
                    colors.set(0, Color.green);
                    editor.save();
                }
                ((FloatPropertyField) field(panel, "widthEditor")).setValue(7f);
                ((FloatPropertyField) field(panel, "miterEditor")).setValue(8f);
                for (String name : new String[]{"scaleComboBox", "startCapComboBox", "endCapComboBox", "joinComboBox"}) {
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
                for (String name : new String[]{"scaleComboBox", "startCapComboBox", "endCapComboBox", "joinComboBox"}) {
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
                assertEquals(((JComboBox<?>) field(panel, "startCapComboBox")).getSelectedIndex(), LINESTYLE2.SQUARE_CAP);
                assertEquals(((JComboBox<?>) field(panel, "endCapComboBox")).getSelectedIndex(), LINESTYLE2.ROUND_CAP);
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
    public void loadingDoesNotEmitEditsAndWidthPreservesOtherStrokeProperties() throws Exception {
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

    @Test
    public void inlineGradientColorTargetsTheSelectedStopAcrossReloads() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = new ShapePropertiesPanel();
                FILLSTYLE fill = new FILLSTYLE();
                fill.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
                fill.gradient = gradient();
                GRADRECORD hardStop = new GRADRECORD();
                hardStop.ratio = 30;
                hardStop.color = new RGBA(255, 255, 0, 100);
                fill.gradient.gradientRecords = new GRADRECORD[]{
                    fill.gradient.gradientRecords[0], hardStop, fill.gradient.gradientRecords[1]
                };
                panel.setSelectedFill(fill);
                Object fillEditor = field(panel, "fillEditor");
                GradientEditor editor = (GradientEditor) field(fillEditor, "gradientEditor");
                JPanel strip = (JPanel) field(editor, "inlineStops");
                strip.setSize(210, 50);
                int x = Math.round(200 * 220f / 255) + 5;
                strip.dispatchEvent(new MouseEvent(strip, MouseEvent.MOUSE_PRESSED, 0, 0, x, 44, 1, false, MouseEvent.BUTTON1));
                strip.dispatchEvent(new MouseEvent(strip, MouseEvent.MOUSE_RELEASED, 0, 0, x, 44, 1, false, MouseEvent.BUTTON1));

                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertFalse(stroke);
                    assertEquals(property, "gradient");
                    FILLSTYLE changed = panel.getFillStyle();
                    panel.setSelectedFill(null);
                    panel.setSelectedFill(changed);
                    edits.incrementAndGet();
                });
                Object stopColorEditor = field(editor, "stopColorEditor");
                TextPropertyField hex = (TextPropertyField) field(stopColorEditor, "hexField");
                assertEquals(hex.getValue(), "0000FF");
                FloatPropertyField red = ((FloatPropertyField[]) field(stopColorEditor, "rgbFields"))[0];
                editColorField(red, "68");
                assertEquals(hex.getValue(), "4400FF");
                assertEquals(panel.getFillStyle().gradient.gradientRecords[2].color, new RGBA(68, 0, 255, 200));
                editColorField(hex, " #112233AA ");
                assertEquals(edits.get(), 2);
                GRADRECORD[] stops = panel.getFillStyle().gradient.gradientRecords;
                assertEquals(stops[0].ratio, 30);
                assertEquals(stops[0].color, new RGBA(255, 0, 0, 80));
                assertEquals(stops[1].ratio, 30);
                assertEquals(stops[1].color, hardStop.color);
                assertEquals(stops[2].ratio, 220);
                assertEquals(stops[2].color, new RGBA(17, 34, 51, 170));
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Test
    public void swatchAndChannelEditsUpdateStrokeColor() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                ShapePropertiesPanel panel = ShapePropertiesPanel.createDrawingStyles();
                AtomicInteger edits = new AtomicInteger();
                panel.setPropertyChangeListener((stroke, property) -> {
                    assertTrue(stroke);
                    assertEquals(property, "color");
                    edits.incrementAndGet();
                });
                JToggleButton strokeTab = (JToggleButton) field(panel, "strokeTab");
                strokeTab.doClick();
                assertTrue(((JPanel) field(panel, "strokePanel")).isVisible());
                assertFalse(((JPanel) field(panel, "fillPanel")).isVisible());
                Object colorEditor = field(field(panel, "strokeEditor"), "colorEditor");
                assertEquals(edits.get(), 0);
                TextPropertyField hex = (TextPropertyField) field(colorEditor, "hexField");
                editColorField(hex, "33669980");
                assertEquals(edits.get(), 1);
                assertEquals(panel.getDrawingStrokeStyle().color, new RGBA(51, 102, 153, 128));
                FloatPropertyField[] rgb = (FloatPropertyField[]) field(colorEditor, "rgbFields");
                assertEquals(rgb[0].getValue(), Float.valueOf(51));
                editColorField(rgb[0], "76");
                assertEquals(panel.getDrawingStrokeStyle().color, new RGBA(76, 102, 153, 128));
                JPopupMenu palette = (JPopupMenu) field(colorEditor, "swatchesPopup");
                JMenuItem red = (JMenuItem) palette.getComponent(6);
                red.doClick();
                assertEquals(hex.getValue(), "FF0000");
                assertEquals(rgb[0].getValue(), Float.valueOf(255));
                FloatPropertyField hue = ((FloatPropertyField[]) field(colorEditor, "channelFields"))[0];
                editColorField(hue, "120");
                assertEquals(rgb[0].getValue(), Float.valueOf(0));
                assertEquals(rgb[1].getValue(), Float.valueOf(255));
                assertEquals(panel.getDrawingStrokeStyle().color, new RGBA(0, 255, 0, 128));
                editColorField((FloatPropertyField) field(colorEditor, "alphaField"), "25,1");
                editColorField(hex, "invalid");
                editColorField(rgb[1], "256");
                assertEquals(panel.getDrawingStrokeStyle().color, new RGBA(0, 255, 0, 64));
                assertEquals(edits.get(), 5);
                assertEquals(panel.getFillStyle().color.toColor(), Color.white);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
