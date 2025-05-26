/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;

/**
 * @author JPEXS
 */
public class TimelinePanel extends JPanel {

    private TimelineBodyPanel timelineBodyPanel;

    private TimelineTimePanel timePanel;

    private TimelineDepthPanel depthPanel;

    private Timeline timeline;

    private Timelined timelined;

    public static final int FRAME_WIDTH = 8;

    public static final int FRAME_HEIGHT = 18;

    private final JScrollPane timelineBodyScrollPane;

    public TimelinePanel(EasySwfPanel swfPanel, UndoManager undoManager) {
        timelineBodyPanel = new TimelineBodyPanel(swfPanel, undoManager);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        timelineBodyScrollPane = new FasterScrollPane(timelineBodyPanel);

        depthPanel = new TimelineDepthPanel();

        timelineBodyPanel.addChangeListener(new Runnable() {
            @Override
            public void run() {
                timeline = timelined.getTimeline();
                depthPanel.setTimeline(timeline);
                timePanel.setTimeline(timeline);
            }
        });

        timePanel = new TimelineTimePanel(depthPanel);

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        add(timePanel, gbc);

        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0;
        gbc.weighty = 1;
        add(depthPanel, gbc);

        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(timelineBodyScrollPane, gbc);

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
            public void frameSelected(int frame, List<Integer> depths) {
                ftime.frameSelect(frame);
            }
        });
        final TimelineBodyPanel ftimeline = timelineBodyPanel;
        timePanel.addFrameSelectionListener(new FrameSelectionListener() {

            @Override
            public void frameSelected(int frame, List<Integer> depths) {
                ftimeline.frameSelect(frame, depths);
            }
        });
    }

    public static Color getBackgroundColor() {
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
        } else {
            return SystemColor.control;
        }
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

    public void setDepths(List<Integer> depths) {
        timelineBodyPanel.depthsSelect(depths);
    }

    public void setFrame(int frame, int depth) {
        timelineBodyPanel.frameSelect(frame, depth);
    }

    public void setFrame(int frame, List<Integer> depths) {
        timelineBodyPanel.frameSelect(frame, depths);
    }

    public void refresh() {
        timelineBodyPanel.refresh();
    }

    public void addChangeListener(Runnable l) {
        timelineBodyPanel.addChangeListener(l);
    }

    public void removeChangeListener(Runnable l) {
        timelineBodyPanel.removeChangeListener(l);
    }

    public void setTimelined(Timelined timelined) {
        this.timelined = timelined;
        if (timelined == null) {
            timelineBodyPanel.setTimeline(null);
            depthPanel.setTimeline(null);
            timePanel.setTimeline(null);
        } else {
            timelineBodyPanel.setTimeline(timelined.getTimeline());
            depthPanel.setTimeline(timelined.getTimeline());
            timePanel.setTimeline(timelined.getTimeline());
            timelineBodyPanel.frameSelect(0, 0);
        }
    }
}
