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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.RECT;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ScalingGridTest {

    @DataProvider(name = "symbols")
    public Object[][] symbols() {
        return new Object[][]{{0}, {1}, {2}};
    }

    private static CharacterTag symbol(int type) throws Exception {
        DefineShape4Tag shape = ShapeSelectionTest.shape();
        ShapeSelectionTest.rectangle(shape, -1200, -900, 6000, 3600, 1, 0);
        ShapeSelectionTest.selection(shape);
        shape.updateBounds();
        SWF swf = shape.getSwf();
        swf.setFileTitle("scaling-grid-" + UUID.randomUUID() + ".swf");
        swf.version = 7;
        swf.frameRate = 24;
        swf.displayRect = new RECT(0, 12000, 0, 8000);
        swf.addTag(shape);
        CharacterTag result = shape;
        if (type == 1) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.frameCount = 1;
            ShapeSceneEditTest.place(sprite, shape.shapeId, 1, new Matrix());
            sprite.addTag(new ShowFrameTag(swf));
            swf.addTag(sprite);
            result = sprite;
        } else if (type == 2) {
            DefineButton2Tag button = new DefineButton2Tag(swf);
            BUTTONRECORD record = new BUTTONRECORD(swf, button);
            record.buttonStateUp = true;
            record.placeDepth = 1;
            record.characterId = shape.shapeId;
            record.placeMatrix = new Matrix().toMATRIX();
            record.colorTransform = new CXFORMWITHALPHA();
            button.characters.add(record);
            swf.addTag(button);
            result = button;
        }
        swf.addTag(new ShowFrameTag(swf));
        CharacterTag restored = roundTrip(result);
        restored.getSwf().setFileTitle(swf.getFileTitle());
        return restored;
    }

    private static void assertRect(RECT actual, RECT expected) {
        assertEquals(actual.Xmin, expected.Xmin);
        assertEquals(actual.Xmax, expected.Xmax);
        assertEquals(actual.Ymin, expected.Ymin);
        assertEquals(actual.Ymax, expected.Ymax);
    }

    private static CharacterTag roundTrip(CharacterTag symbol) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        symbol.getSwf().saveTo(bytes);
        return new SWF(new ByteArrayInputStream(bytes.toByteArray()), false).getCharacter(symbol.getCharacterId());
    }

    @Test(dataProvider = "symbols")
    public void enableMoveDisableRoundTripAndUndo(int type) throws Exception {
        CharacterTag symbol = symbol(type);
        SWF swf = symbol.getSwf();
        UndoManager history = new UndoManager();
        RECT thirds = ScalingGridOperation.initialSplitter((BoundedTag) symbol);
        assertRect(thirds, new RECT(800, 2800, 300, 1500));
        history.doOperation(new ScalingGridOperation(symbol, thirds, null), swf);
        DefineScalingGridTag grid = symbol.getScalingGridTag();
        assertNotNull(grid);
        assertEquals(swf.getTags().indexOf(grid), swf.getTags().indexOf(symbol) + 1);
        assertEquals(swf.version, 8);
        assertTrue(swf.isModified());
        assertRect(roundTrip(symbol).getScalingGridTag().splitter, thirds);
        history.undo(swf);
        assertNull(symbol.getScalingGridTag());
        assertEquals(swf.version, 7);
        assertFalse(swf.isModified());
        history.redo(swf);
        RECT moved = new RECT(1000, 2700, 500, 1400);
        history.doOperation(new ScalingGridOperation(symbol, moved, null), swf);
        moved.Xmin = -9999; // The operation owns its snapshot.
        assertRect(roundTrip(symbol).getScalingGridTag().splitter, new RECT(1000, 2700, 500, 1400));
        history.undo(swf);
        assertRect(symbol.getScalingGridTag().splitter, thirds);
        history.redo(swf);
        history.doOperation(new ScalingGridOperation(symbol, null, null), swf);
        assertNull(symbol.getScalingGridTag());
        assertNull(roundTrip(symbol).getScalingGridTag());
        history.undo(swf);
        assertSame(symbol.getScalingGridTag(), grid);
        assertRect(grid.splitter, new RECT(1000, 2700, 500, 1400));
        history.redo(swf);
        assertNull(symbol.getScalingGridTag());
    }

    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layout((Container) child);
            }
        }
    }

    private static void mouse(Component canvas, int id, Point point) throws Exception {
        MouseEvent event = new MouseEvent(canvas, id, 0, 0, point.x, point.y, 1, false, MouseEvent.BUTTON1);
        if (id == MouseEvent.MOUSE_DRAGGED) {
            for (Object listener : (List<?>) field(canvas, "mouseMotionListeners")) {
                ((MouseMotionListener) listener).mouseDragged(event);
            }
        } else {
            for (Object listener : (List<?>) field(canvas, "mouseListeners")) {
                if (id == MouseEvent.MOUSE_PRESSED) {
                    ((MouseListener) listener).mousePressed(event);
                } else if (id == MouseEvent.MOUSE_RELEASED) {
                    ((MouseListener) listener).mouseReleased(event);
                } else {
                    ((MouseListener) listener).mouseClicked(event);
                }
            }
        }
    }

    private static Point screen(ImagePanel panel, double x, double y) {
        Point2D point = panel.getTimelinedToPanelTransform().transform(new Point2D.Double(x, y), null);
        return new Point((int) Math.round(point.getX()), (int) Math.round(point.getY()));
    }

    @Test(dataProvider = "symbols")
    public void dragAllFourLinesAtZoomWithoutSelectingContent(int type) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            boolean ribbon = Configuration.useRibbonInterface.get();
            Configuration.useRibbonInterface.set(false);
            ImagePanel panel = new ImagePanel();
            try {
                CharacterTag symbol = symbol(type);
                SWF swf = symbol.getSwf();
                new ScalingGridOperation(symbol, ScalingGridOperation.initialSplitter((BoundedTag) symbol), null).doOperation();
                UndoManager history = new UndoManager();
                AtomicInteger changes = new AtomicInteger();
                AtomicInteger selections = new AtomicInteger();
                panel.setScalingGridChangeListener((character, rect) -> {
                    assertSame(character, symbol);
                    changes.incrementAndGet();
                    history.doOperation(new ScalingGridOperation(character, rect, null), swf);
                });
                Timelined timeline = symbol instanceof Timelined ? (Timelined) symbol : TimelinedMaker.makeTimelined(symbol);
                panel.setSize(800, 600);
                panel.setTimelined(timeline, swf, 0, true, false, true, true, true, false, true, true, false);
                panel.setRectangleSelectionListener((area, add) -> selections.incrementAndGet());
                Zoom zoom = new Zoom();
                zoom.value = 2;
                panel.zoom(zoom);
                layout(panel);
                Component canvas = (Component) field(panel, "iconPanel");
                BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                Method paintGrid = ImagePanel.class.getDeclaredMethod("paintScalingGrid", Graphics2D.class);
                paintGrid.setAccessible(true);
                paintGrid.invoke(panel, graphics);
                graphics.dispose();
                int black = 0;
                int white = 0;
                Point firstLine = screen(panel, 800, 900);
                for (int y = 0; y < image.getHeight(); y++) {
                    int pixel = image.getRGB(firstLine.x, y);
                    black += pixel == 0xff000000 ? 1 : 0;
                    white += pixel == 0xffffffff ? 1 : 0;
                }
                assertTrue(black > 20 && white > 20, "Grid guides must alternate black and white at the splitter");
                for (int line = 0; line < 4; line++) {
                    RECT before = new RECT(symbol.getScalingGridTag().splitter);
                    Point start = screen(panel, line == 0 ? before.Xmin : line == 1 ? before.Xmax : 1800,
                            line == 2 ? before.Ymin : line == 3 ? before.Ymax : 900);
                    Point end = new Point(start.x + (line < 2 ? 10 : 0), start.y + (line < 2 ? 0 : 10));
                    mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                    assertSame(canvas.getCursor(), field(panel, line < 2 ? "guideXCursor" : "guideYCursor"));
                    mouse(canvas, MouseEvent.MOUSE_DRAGGED, end);
                    assertSame(canvas.getCursor(), field(panel, line < 2 ? "guideXCursor" : "guideYCursor"));
                    assertRect(symbol.getScalingGridTag().splitter, before);
                    mouse(canvas, MouseEvent.MOUSE_RELEASED, end);
                    assertEquals(changes.get(), line + 1);
                    RECT expected = new RECT(before);
                    if (line == 0) {
                        expected.Xmin += 100;
                    } else if (line == 1) {
                        expected.Xmax += 100;
                    } else if (line == 2) {
                        expected.Ymin += 100;
                    } else {
                        expected.Ymax += 100;
                    }
                    assertRect(symbol.getScalingGridTag().splitter, expected);
                    history.undo(swf);
                    assertRect(symbol.getScalingGridTag().splitter, before);
                    history.redo(swf);
                }
                assertEquals(selections.get(), 0);
                RECT before = new RECT(symbol.getScalingGridTag().splitter);
                Point start = screen(panel, before.Xmin, 900);
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_RELEASED, start);
                mouse(canvas, MouseEvent.MOUSE_CLICKED, start);
                assertEquals(changes.get(), 4, "Clicking a line must not create an undo step");
                mouse(canvas, MouseEvent.MOUSE_PRESSED, start);
                mouse(canvas, MouseEvent.MOUSE_DRAGGED, new Point(start.x + 1000, start.y));
                RECT preview = (RECT) field(panel, "scalingGridPreview");
                assertEquals(preview.Xmin, before.Xmax - 1, "Lines must not cross");
                KeyEvent escape = new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_ESCAPE, (char) 27);
                for (KeyListener listener : canvas.getKeyListeners()) {
                    listener.keyPressed(escape);
                }
                assertNull(field(panel, "scalingGridPreview"));
                assertRect(symbol.getScalingGridTag().splitter, before);
                assertEquals(changes.get(), 4);
                panel.setTimelined(swf, swf, 0, true, false, true, true, true, false, true, true, false);
                assertNull(field(panel, "scalingGridPreview"));
                Method lines = ImagePanel.class.getDeclaredMethod("getScalingGridLines");
                lines.setAccessible(true);
                assertEquals(((Object[]) lines.invoke(panel)).length, 0, "Main stage must not show a library symbol's grid");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
                Configuration.useRibbonInterface.set(ribbon);
            }
        });
    }
}
