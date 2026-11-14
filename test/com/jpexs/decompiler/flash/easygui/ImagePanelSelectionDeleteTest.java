/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.MainFrame;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.testng.SkipException;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ImagePanelSelectionDeleteTest {

    private static KeyEvent delete(Component canvas, int modifiers) {
        KeyEvent event = new KeyEvent(canvas, KeyEvent.KEY_PRESSED, 0, modifiers, KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        for (KeyListener listener : canvas.getKeyListeners()) {
            listener.keyPressed(event);
        }
        return event;
    }

    @Test
    public void stageDeleteUsesItsCallbackWithoutInterceptingOtherEditingModes() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("ImagePanel requires a desktop graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            AppStrings.setResourceClass(MainFrame.class);
            ImagePanel panel = new ImagePanel();
            try {
                Field field = ImagePanel.class.getDeclaredField("iconPanel");
                field.setAccessible(true);
                Component canvas = (Component) field.get(panel);
                AtomicInteger deleted = new AtomicInteger();
                panel.setSelectionDeleteListener(deleted::incrementAndGet);
                assertFalse(delete(canvas, 0).isConsumed(), "A preview does not delete instances");
                panel.setSelectionMode(true);
                for (int modifiers : new int[]{InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK, InputEvent.ALT_DOWN_MASK}) {
                    assertFalse(delete(canvas, modifiers).isConsumed(), "Modified Delete follows the timeline behavior");
                }
                assertEquals(deleted.get(), 0);
                assertTrue(delete(canvas, 0).isConsumed());
                assertEquals(deleted.get(), 1);
                panel.setSelectionMode(false);
                assertFalse(delete(canvas, 0).isConsumed(), "Point and drawing tools retain their own Delete handling");
                assertEquals(deleted.get(), 1);
                panel.setAreaSelectionListener((area, additive) -> { }, false);
                assertTrue(delete(canvas, 0).isConsumed(), "Shape selection still uses its deletion callback");
                assertEquals(deleted.get(), 2);
                panel.setSelectionDeleteListener(null);
                assertFalse(delete(canvas, 0).isConsumed());
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            } finally {
                panel.clearAll();
            }
        });
    }
}
