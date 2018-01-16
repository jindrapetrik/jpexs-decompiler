/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import org.pushingpixels.substance.internal.ui.SubstanceProgressBarUI;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;

/**
 *
 * @author JPEXS
 */
public class MyProgressBarUI extends SubstanceProgressBarUI {

    private final class MySubstanceChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            SubstanceCoreUtilities.testComponentStateChangeThreadingViolation(progressBar);

            //if (displayTimeline != null) { //Main Change - this should be first
            //    displayTimeline.abort();
            //}
            int currValue = progressBar.getValue();
            int span = progressBar.getMaximum() - progressBar.getMinimum();

            int barRectWidth = progressBar.getWidth() - 2 * margin;
            int barRectHeight = progressBar.getHeight() - 2 * margin;
            int totalPixels = (progressBar.getOrientation() == JProgressBar.HORIZONTAL) ? barRectWidth
                    : barRectHeight;

            int pixelDelta = (span <= 0) ? 0 : (currValue - displayedValue)
                    * totalPixels / span;


            /*displayTimeline = new Timeline(progressBar);
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
             displayTimeline);*/
            boolean isInCellRenderer = (SwingUtilities.getAncestorOfClass(
                    CellRendererPane.class, progressBar) != null);
            //if (false) {//currValue > 0 && !isInCellRenderer && Math.abs(pixelDelta) > 5) {
            //    displayTimeline.play();
            //} else {
            displayedValue = currValue;
            progressBar.repaint();
            //}
        }
    }

    public static ComponentUI createUI(JComponent comp) {
        SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
        return new MyProgressBarUI();
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        this.progressBar.removeChangeListener(substanceValueChangeListener);
        this.substanceValueChangeListener = new MySubstanceChangeListener();
        this.progressBar.addChangeListener(this.substanceValueChangeListener);
    }
}
