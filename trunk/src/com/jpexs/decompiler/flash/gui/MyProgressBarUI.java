/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.lafwidget.animation.AnimationConfigurationManager;
import org.pushingpixels.substance.internal.ui.SubstanceProgressBarUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.trident.Timeline;
import org.pushingpixels.trident.TimelinePropertyBuilder;
import org.pushingpixels.trident.ease.Spline;

/**
 *
 * @author JPEXS
 */
public class MyProgressBarUI extends SubstanceProgressBarUI {

    private final class SubstanceChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            SubstanceCoreUtilities.testComponentStateChangeThreadingViolation(progressBar);

            if (displayTimeline != null) { //Main Change - this should be first                
                displayTimeline.abort();
            }
            int currValue = progressBar.getValue();
            int span = progressBar.getMaximum() - progressBar.getMinimum();

            int barRectWidth = progressBar.getWidth() - 2 * margin;
            int barRectHeight = progressBar.getHeight() - 2 * margin;
            int totalPixels = (progressBar.getOrientation() == JProgressBar.HORIZONTAL) ? barRectWidth
                    : barRectHeight;

            int pixelDelta = (span <= 0) ? 0 : (currValue - displayedValue)
                    * totalPixels / span;


            displayTimeline = new Timeline(progressBar);
            displayTimeline.addPropertyToInterpolate(Timeline
                    .<Integer>property("displayedValue").from(displayedValue)
                    .to(currValue).setWith(new TimelinePropertyBuilder.PropertySetter<Integer>() {
                @Override
                public void set(Object obj, String fieldName,
                        Integer value) {
                    displayedValue = value;
                    progressBar.repaint();
                }
            }));
            displayTimeline.setEase(new Spline(0.4f));
            AnimationConfigurationManager.getInstance().configureTimeline(
                    displayTimeline);

            boolean isInCellRenderer = (SwingUtilities.getAncestorOfClass(
                    CellRendererPane.class, progressBar) != null);
            if (currValue > 0 && !isInCellRenderer && Math.abs(pixelDelta) > 5) {
                displayTimeline.play();
            } else {
                displayedValue = currValue;
                progressBar.repaint();
            }
        }
    }

    public static ComponentUI createUI(JComponent comp) {
        SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new MyProgressBarUI();
    }

    @Override
    protected void installListeners() {
        substanceValueChangeListener = new MyProgressBarUI.SubstanceChangeListener();
        this.progressBar.addChangeListener(this.substanceValueChangeListener);

        this.substancePropertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("font".equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar != null) {
                                progressBar.updateUI();
                            }
                        }
                    });
                }
            }
        };
        this.progressBar.addPropertyChangeListener(this.substancePropertyChangeListener);
    }
}
