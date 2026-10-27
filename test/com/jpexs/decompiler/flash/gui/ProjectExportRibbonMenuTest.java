/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProjectExportRibbonMenuTest {

    private void add(MainFrameRibbonMenu menu, String path, boolean leaf, java.awt.event.ActionListener action) {
        String label = path.substring(path.lastIndexOf('/') + 1);
        menu.addMenuItem(path, label, null, action, MainFrameMenu.PRIORITY_MEDIUM, null, leaf, null, false);
    }

    @Test
    public void registersNestedProjectMenusAndPreservesChildActionsAndAvailability() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MainFrameRibbonMenu menu = new MainFrameRibbonMenu(null, null);
            try {
                menu.initMenu();
                add(menu, "/file", false, null);
                add(menu, "/file/export", false, null);
                String parent = "/file/export/exportMore";
                add(menu, parent, false, null);
                AtomicInteger as3 = new AtomicInteger();
                AtomicInteger mxml = new AtomicInteger();
                for (String ide : new String[]{"exportFlashDevelop", "exportIdea", "exportVsCode"}) {
                    String path = parent + "/" + ide;
                    add(menu, path, false, null);
                    add(menu, path + "/as3", true, event -> as3.incrementAndGet());
                    add(menu, path + "/mxml", true, event -> mxml.incrementAndGet());
                    menu.finishMenu(path);
                }
                menu.finishMenu(parent);
                Field field = MainFrameRibbonMenu.class.getDeclaredField("menuItems");
                field.setAccessible(true);
                Map<?, ?> items = (Map<?, ?>) field.get(menu);
                for (String ide : new String[]{"exportFlashDevelop", "exportIdea", "exportVsCode"}) {
                    String path = parent + "/" + ide;
                    Assert.assertTrue(items.get(path) instanceof JMenu, path);
                    JMenu group = (JMenu) items.get(path);
                    Assert.assertEquals(group.getItemCount(), 2);
                    Assert.assertEquals(group.getItem(0), items.get(path + "/as3"));
                    Assert.assertEquals(group.getItem(1), items.get(path + "/mxml"));
                    Assert.assertNotNull(group.getParent(), "The IDE submenu must be attached to the export popup");
                    menu.setMenuEnabled(path, true);
                    menu.setMenuEnabled(path + "/as3", true);
                    menu.setMenuEnabled(path + "/mxml", false);
                    Assert.assertTrue(group.isEnabled());
                    Assert.assertFalse(group.getItem(1).isEnabled());
                    group.getItem(0).doClick();
                    group.getItem(1).doClick();
                    menu.setMenuEnabled(path + "/mxml", true);
                    group.getItem(1).doClick();
                    menu.setMenuEnabled(path, false);
                    Assert.assertFalse(group.isEnabled());
                }
                Assert.assertEquals(as3.get(), 3);
                Assert.assertEquals(mxml.get(), 3);
                Assert.assertNotNull(items.get(parent));
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            } finally {
                // This test builds only the menu, without the application debugger.
                // Remove its key hook without invoking application-wide shutdown.
                try {
                    Field dispatcher = MainFrameMenu.class.getDeclaredField("keyEventDispatcher");
                    dispatcher.setAccessible(true);
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher((KeyEventDispatcher) dispatcher.get(menu));
                } catch (ReflectiveOperationException ex) {
                    throw new AssertionError(ex);
                }
            }
        });
    }
}
