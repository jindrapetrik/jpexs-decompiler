/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.easygui.ShapeTool;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandToggleButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ShapeToolsRibbonTest {

    @Test
    public void sharesToolSelectionAndDispatchesWholeShapeTransferActions() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("Ribbon frames require a graphics environment");
        }
        SwingUtilities.invokeAndWait(() -> {
            JRibbonFrame frame = new JRibbonFrame();
            try {
                JRibbon ribbon = frame.getRibbon();
                JRibbonBand fileBand = new JRibbonBand("File", null);
                fileBand.addCommandButton(new JCommandButton("Open"), RibbonElementPriority.TOP);
                fileBand.setResizePolicies(CoreRibbonResizePolicies.getCorePoliciesNone(fileBand));
                RibbonTask file = new RibbonTask("File", fileBand);
                ribbon.addTask(file);
                ResourceBundle translations = ResourceBundle.getBundle("com.jpexs.decompiler.flash.gui.locales.MainFrame", Locale.ENGLISH);
                AtomicReference<ShapeTool> selected = new AtomicReference<>(ShapeTool.SELECT);
                AtomicInteger replace = new AtomicInteger();
                AtomicInteger updateBounds = new AtomicInteger();
                AtomicInteger export = new AtomicInteger();
                ShapeToolsRibbon tools = new ShapeToolsRibbon(ribbon, translations::getString, selected::set,
                        replace::incrementAndGet, updateBounds::incrementAndGet, export::incrementAndGet);
                RibbonContextualTaskGroup context = ribbon.getContextualTaskGroup(0);
                RibbonTask task = context.getTask(0);
                Assert.assertFalse(ribbon.isVisible(context));
                Assert.assertEquals(task.getBandCount(), 4);
                List<JCommandToggleButton> buttons = new ArrayList<>();
                int[] counts = {5, 4, 4, 0};
                for (int i = 0; i < task.getBandCount(); i++) {
                    int before = buttons.size();
                    for (Component component : task.getBand(i).getControlPanel().getComponents()) {
                        if (component instanceof JCommandToggleButton) {
                            buttons.add((JCommandToggleButton) component);
                        }
                    }
                    Assert.assertEquals(buttons.size() - before, counts[i]);
                }
                List<JCommandButton> transfer = new ArrayList<>();
                for (Component component : task.getBand(3).getControlPanel().getComponents()) {
                    if (component instanceof JCommandButton) {
                        transfer.add((JCommandButton) component);
                    }
                }
                Assert.assertEquals(transfer.size(), 3);
                Assert.assertEquals(transfer.get(0).getText(), "Replace");
                Assert.assertEquals(transfer.get(1).getText(), "Replace & Update bounds");
                Assert.assertEquals(transfer.get(2).getText(), "Export");
                for (JCommandButton button : transfer) {
                    Assert.assertNotNull(button.getIcon());
                    button.doActionClick();
                }
                Assert.assertEquals(replace.get(), 1);
                Assert.assertEquals(updateBounds.get(), 1);
                Assert.assertEquals(export.get(), 1);
                Assert.assertSame(selected.get(), ShapeTool.SELECT);
                assertSelected(buttons, "Select");
                tools.update(true, selected.get());
                Assert.assertTrue(ribbon.isVisible(context));
                Assert.assertSame(ribbon.getSelectedTask(), task);
                for (ShapeTool tool : ShapeTool.values()) {
                    String label = translations.getString("menu.shapes.tool." + tool.getId());
                    JCommandToggleButton button = buttons.stream().filter(b -> label.equals(b.getText())).findFirst().get();
                    Assert.assertNotNull(button.getIcon());
                    button.doActionClick();
                    Assert.assertSame(selected.get(), tool);
                    assertSelected(buttons, label);
                    // Clicking the active tool must not leave the editor without a mode.
                    button.doActionClick();
                    assertSelected(buttons, label);
                }
                ribbon.setSelectedTask(file);
                tools.update(true, selected.get());
                Assert.assertSame(ribbon.getSelectedTask(), file, "Refreshes must not steal another ribbon tab");
                ribbon.setSelectedTask(task);
                tools.update(false, selected.get());
                Assert.assertFalse(ribbon.isVisible(context));
                Assert.assertSame(ribbon.getSelectedTask(), file);
                tools.update(true, ShapeTool.SELECT);
                Assert.assertSame(ribbon.getSelectedTask(), task);
                assertSelected(buttons, "Select");
            } finally {
                frame.dispose();
            }
        });
    }

    private static void assertSelected(List<JCommandToggleButton> buttons, String label) {
        Assert.assertEquals(buttons.stream().filter(b -> b.getActionModel().isSelected()).count(), 1L);
        Assert.assertEquals(buttons.stream().filter(b -> b.getActionModel().isSelected()).findFirst().get().getText(), label);
    }
}
