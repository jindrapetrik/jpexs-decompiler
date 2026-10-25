/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FlaExportDialogTest {

    private void assertStoredQuality(int expected) {
        FlaExportDialog dialog = new FlaExportDialog(null);
        try {
            Assert.assertEquals(dialog.getTweenDetectionQuality(), expected);
        } finally {
            dialog.dispose();
        }
    }

    private JSlider slider(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JSlider) {
                return (JSlider) component;
            }
            if (component instanceof Container) {
                JSlider result = slider((Container) component);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private JButton cancel(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && ((JButton) component).getText().equals(text)) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton result = cancel((Container) component, text);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @DataProvider(name = "values")
    public Object[][] values() {
        return new Object[][]{{0}, {37}, {100}};
    }

    @Test(dataProvider = "values")
    public void restoresConfirmedValueAndDoesNotRememberCancelledChanges(int value) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new SkipException("Requires a graphical environment to construct a Swing dialog");
        }
        int previous = Configuration.lastFlaExportTweenDetectionQuality.get();
        boolean ribbon = Configuration.useRibbonInterface.get();
        try {
            Configuration.useRibbonInterface.set(false);
            SwingUtilities.invokeAndWait(() -> {
                Configuration.lastFlaExportTweenDetectionQuality.set(value);
                FlaExportDialog dialog = new FlaExportDialog(null);
                try {
                    Assert.assertEquals(dialog.getTweenDetectionQuality(), value);
                    JSlider slider = slider(dialog.getContentPane());
                    Assert.assertNotNull(slider);
                    Assert.assertEquals(slider.getMinimum(), 0);
                    Assert.assertEquals(slider.getMaximum(), 100);
                    int changed = (value + 13) % 101;
                    slider.setValue(changed);
                    cancel(dialog.getContentPane(), dialog.translate("button.cancel")).doClick();
                    Assert.assertEquals(Configuration.lastFlaExportTweenDetectionQuality.get().intValue(), value);
                    assertStoredQuality(value);
                    slider.setValue(changed);
                    dialog.getRootPane().getDefaultButton().doClick();
                    Assert.assertEquals(Configuration.lastFlaExportTweenDetectionQuality.get().intValue(), changed);
                    assertStoredQuality(changed);
                } finally {
                    dialog.dispose();
                }
            });
        } finally {
            Configuration.lastFlaExportTweenDetectionQuality.set(previous);
            Configuration.useRibbonInterface.set(ribbon);
        }
    }
}
