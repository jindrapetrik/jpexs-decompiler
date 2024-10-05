/*
 *  Copyright (C) 2010-2024 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author JPEXS
 */
public class TimelinePanel extends JPanel {

    private TimelineBodyPanel timelineBodyPanel;

    private TimelineTimePanel timePanel;

    private TimelineDepthPanel depthPanel;

    private Timeline timeline;

    public static final int FRAME_WIDTH = 8;

    public static final int FRAME_HEIGHT = 18;

    
    private JScrollPane timelineBodyScrollPane;
    
    //public static final Color backgroundColor = new Color(0xd9, 0xe7, 0xfa);
    public static Color getBackgroundColor() {
        return SystemColor.control;        
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void addFrameSelectionListener(FrameSelectionListener l) {
        timelineBodyPanel.addFrameSelectionListener(l);
    }

    public void removeFrameSelectionListener(FrameSelectionListener l) {
        timelineBodyPanel.removeFrameSelectionListener(l);
    }

    public void setDepth(int depth) {
        timelineBodyPanel.depthSelect(depth);
    }
    
    public void setTimelined(Timelined timelined) {
        this.removeAll();
        if (timelined == null) {
            this.revalidate();
            return;
        }
        timeline = timelined.getTimeline();
        timelineBodyPanel = new TimelineBodyPanel(timeline);
        setLayout(new BorderLayout());

        timelineBodyScrollPane = new FasterScrollPane(timelineBodyPanel);

        depthPanel = new TimelineDepthPanel(timeline);

        timePanel = new TimelineTimePanel();

        JPanel row1Panel = new JPanel();
        row1Panel.setLayout(new BorderLayout());
        JPanel sepPanel = new JPanel();
        sepPanel.setBackground(getBackgroundColor());
        sepPanel.setPreferredSize(new Dimension(depthPanel.getWidth(), timePanel.getHeight()));
        row1Panel.add(sepPanel, BorderLayout.WEST);
        row1Panel.add(timePanel, BorderLayout.CENTER);

        JPanel row2Panel = new JPanel();
        row2Panel.setLayout(new BorderLayout());
        row2Panel.add(depthPanel, BorderLayout.WEST);
        row2Panel.add(timelineBodyScrollPane, BorderLayout.CENTER);

        add(row1Panel, BorderLayout.NORTH);
        add(row2Panel, BorderLayout.CENTER);

        timelineBodyScrollPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                timePanel.scroll(e.getValue());
            }
        });
        timelineBodyScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                depthPanel.scroll(e.getValue());
            }
        });

        final TimelineTimePanel ftime = timePanel;
        timelineBodyPanel.addFrameSelectionListener(new FrameSelectionListener() {

            @Override
            public void frameSelected(int frame, int depth) {
                ftime.frameSelect(frame);
            }
        });
        final TimelineBodyPanel ftimeline = timelineBodyPanel;
        timePanel.addFrameSelectionListener(new FrameSelectionListener() {

            @Override
            public void frameSelected(int frame, int depth) {
                ftimeline.frameSelect(frame, depth);
            }
        });
        this.revalidate();
    }
}
