/*
 *  Copyright (C) 2010-2026 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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
    private final TimelineLayerDrag layerDrag;

    public TimelinePanel(EasySwfPanel swfPanel, UndoManager undoManager) {
        timelineBodyPanel = new TimelineBodyPanel(swfPanel, undoManager);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        timelineBodyScrollPane = new FasterScrollPane(timelineBodyPanel);

        depthPanel = new TimelineDepthPanel(swfPanel, false, timelineBodyPanel.getLayerSelection());
        layerDrag = new TimelineLayerDrag(depthPanel, timelineBodyScrollPane, 1,
                timelineBodyPanel::setDropRow, timelineBodyPanel::moveLayers);

        depthPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event) && !event.isPopupTrigger()
                        && !layerDrag.wasDragged() && !depthPanel.isOverControls(event)) {
                    int depth = depthPanel.getDepthAt(event);
                    if (depth > 0) {
                        timelineBodyPanel.selectLayerFrames(depth, event.isControlDown(), event.isShiftDown());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                showLayerMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                showLayerMenu(event);
            }

            private void showLayerMenu(MouseEvent event) {
                if (!event.isPopupTrigger() || timelined == null || depthPanel.isOverControls(event)) {
                    return;
                }
                int depth = depthPanel.getDepthAt(event);
                if (depth == 0 && (depthPanel.getDepthCount() != 0 || !depthPanel.isActionsRowAt(event))) {
                    return;
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem add = new JMenuItem(EasyStrings.translate("timeline.shape.addLayer"));
                add.setEnabled(timelineBodyPanel.canAddLayer());
                add.addActionListener(action -> timelineBodyPanel.addLayer(depth));
                menu.add(add);
                if (depth > 0) {
                    Set<Integer> depths = depthPanel.getLayerSelection().contextDepths(depth);
                    JMenuItem remove = new JMenuItem(EasyStrings.translate(depths.size() == 1 ? "timeline.shape.removeLayer" : "timeline.shape.removeLayers"));
                    remove.setEnabled(timelineBodyPanel.canRemoveLayers(depths));
                    remove.addActionListener(action -> timelineBodyPanel.removeLayers(depths));
                    menu.add(remove);
                }
                menu.show(depthPanel, event.getX(), event.getY());
            }
        });

        timelineBodyPanel.addChangeListener(this::refresh);

        timePanel = new TimelineTimePanel(depthPanel);

        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(depthPanel.getHeader(), gbc);

        gbc.gridx = 1;
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
                ftimeline.frameSelect(frame, depths, true);
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
        timePanel.frameSelect(frame);
        timelineBodyPanel.frameSelect(frame, depth, false);
    }

    public void setFrame(int frame, List<Integer> depths) {
        timePanel.frameSelect(frame);
        timelineBodyPanel.frameSelect(frame, depths, false);
    }

    void setStageSelection(int frame, List<Integer> depths) {
        setFrame(frame, depths);
        if (!depths.isEmpty()) {
            int depth = depths.get(depths.size() - 1);
            Rectangle row = timelineBodyPanel.getFrameBounds(frame, depth);
            row.x = timelineBodyScrollPane.getViewport().getViewPosition().x;
            row.width = 1;
            timelineBodyPanel.scrollRectToVisible(row);
        }
    }

    public void refresh() {
        layerDrag.cancel();
        timeline = timelined == null ? null : timelined.getTimeline();
        timelineBodyPanel.setTimeline(timeline);
        depthPanel.setTimeline(timeline);
        timePanel.setTimeline(timeline);
    }

    public void addChangeListener(Runnable l) {
        timelineBodyPanel.addChangeListener(l);
    }

    public void removeChangeListener(Runnable l) {
        timelineBodyPanel.removeChangeListener(l);
    }

    public void setTimelined(Timelined timelined) {
        this.timelined = timelined;
        refresh();
        if (timelined != null) {
            SWF swf = timelined.getSwf();
            SwfSpecificCustomConfiguration conf = swf == null ? null : Configuration.getSwfSpecificCustomConfiguration(swf);
            
            int frame = conf == null ? 0 : Integer.parseInt(conf.getCustomData(CustomConfigurationKeys.KEY_EASY_LAST_SELECTED_FRAME, "1")) - 1;
            
            timePanel.frameSelect(frame);
            timelineBodyPanel.frameSelect(frame, 0, false);
        }
    }

    public TimelineBodyPanel getTimelineBodyPanel() {
        return timelineBodyPanel;
    }
    
    
}
