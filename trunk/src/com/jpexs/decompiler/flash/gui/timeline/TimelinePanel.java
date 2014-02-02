/*
 * Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.gui.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.timeline.Timeline;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class TimelinePanel extends JPanel {

    public TimelineBodyPanel timelineBodyPanel;
    public TimelineTimePanel timePanel;
    public TimelineDepthPanel depthPanel;
    public Timeline timeline;

    public static final int FRAME_WIDTH = 8;
    public static final int FRAME_HEIGHT = 18;

    public static Color selectedColor = new Color(0xff, 0x99, 0x99);
    public static Color selectedBorderColor = new Color(0xcc, 0, 0);
    public static Color backgroundColor = new Color(0xee, 0xee, 0xee);

    public TimelinePanel(SWF swf) {
        timeline = new Timeline(swf);
        timelineBodyPanel = new TimelineBodyPanel(timeline);
        setLayout(new BorderLayout());

        JScrollPane sp = new JScrollPane(timelineBodyPanel);

        depthPanel = new TimelineDepthPanel(timeline);

        timePanel = new TimelineTimePanel();

        JPanel row1Panel = new JPanel();
        row1Panel.setLayout(new BorderLayout());
        JPanel sepPanel = new JPanel();
        sepPanel.setBackground(TimelinePanel.backgroundColor);
        sepPanel.setPreferredSize(new Dimension(depthPanel.getWidth(), timePanel.getHeight()));
        row1Panel.add(sepPanel, BorderLayout.WEST);
        row1Panel.add(timePanel, BorderLayout.CENTER);

        JPanel row2Panel = new JPanel();
        row2Panel.setLayout(new BorderLayout());
        row2Panel.add(depthPanel, BorderLayout.WEST);
        row2Panel.add(sp, BorderLayout.CENTER);

        add(row1Panel, BorderLayout.NORTH);
        add(row2Panel, BorderLayout.CENTER);

        sp.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                timePanel.scroll(e.getValue());
            }
        });
        sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

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

    }
}
