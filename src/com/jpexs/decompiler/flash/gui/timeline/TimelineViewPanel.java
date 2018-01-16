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
package com.jpexs.decompiler.flash.gui.timeline;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.controls.JPersistentSplitPane;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 *
 * @author JPEXS
 */
public class TimelineViewPanel extends JPanel {

    public TimelinePanel timeline;

    public ImagePanel previewPanel;

    public TimelineViewPanel() {

    }

    public void setTimelined(Timelined timelined) {
        removeAll();
        if (timelined == null) {
            revalidate();
            return;
        }
        setLayout(new BorderLayout());
        timeline = new TimelinePanel();
        timeline.setTimelined(timelined);
        add(new JPersistentSplitPane(JSplitPane.HORIZONTAL_SPLIT, timeline, previewPanel = new ImagePanel(), Configuration.guiTimeLineSplitPaneDividerLocationPercent));

        previewPanel.setTimelined(timelined, timelined.getTimeline().swf, 0);
        //previewPanel.setPreferredSize(new Dimension(400,300));
        previewPanel.pause();
        previewPanel.gotoFrame(0);
        timeline.addFrameSelectionListener(new FrameSelectionListener() {

            @Override
            public void frameSelected(int frame, int depth) {
                previewPanel.selectDepth(depth);
                previewPanel.pause();
                previewPanel.gotoFrame(frame);
            }
        });
        revalidate();
    }
}
