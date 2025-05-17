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

import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.timeline.Timeline;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * @author JPEXS
 */
public class TimelineTimePanel extends JPanel implements MouseListener {

    public static final Color borderColor = Color.black;

    public static final int lineLength = 3;

    public static final int lineTextSpace = 3;

    public static final Color fontColor = Color.black;

    public float fontSize = 10.0f;

    private int scrollOffset = 0;

    private int selectedFrame = -1;
    private int leftPos = 0;
    
    private Timeline timeline;

    private final List<FrameSelectionListener> listeners = new ArrayList<>();   

    public TimelineTimePanel() {
        Dimension dim = new Dimension(Integer.MAX_VALUE, TimelineBodyPanel.FRAME_HEIGHT);
        setSize(dim);
        setPreferredSize(dim);
        addMouseListener(this);
    }

    public void setTimeline(Timeline timeline) {
        int maxDepth = timeline == null ? 0 : timeline.getMaxDepth();
        String maxDepthStr = Integer.toString(maxDepth);
        setFont(getFont().deriveFont(TimelineDepthPanel.FONT_SIZE));
        int maxDepthW = getFontMetrics(getFont()).stringWidth(maxDepthStr);
        leftPos = maxDepthW + 2 * TimelineDepthPanel.PADDING;
        this.timeline = timeline;
    }
    
    
    public void addFrameSelectionListener(FrameSelectionListener l) {
        listeners.add(l);
    }

    public void removeFrameSelectionListener(FrameSelectionListener l) {
        listeners.remove(l);
    }

    public void frameSelect(int frame) {
        if (selectedFrame == frame) {
            return;
        }        
        selectedFrame = frame;
        repaint();
    }

    public void scroll(int offset) {
        this.scrollOffset = offset;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int frameWidth = TimelineBodyPanel.getFrameWidthForTimeline(timeline);
        int start_f = (scrollOffset + clip.x) / frameWidth;
        int end_f = (scrollOffset + clip.x + clip.width) / frameWidth;
        g.setColor(TimelineBodyPanel.getBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(borderColor);
        int xofs = -scrollOffset % frameWidth;
        for (int f = 0; f <= end_f; f++) {
            g.drawLine(xofs + f * frameWidth + 1, TimelineBodyPanel.FRAME_HEIGHT - 1, xofs + f * frameWidth + 1, TimelineBodyPanel.FRAME_HEIGHT - lineLength);
        }
        g.setFont(g.getFont().deriveFont(fontSize));
        for (int f = 0; f <= end_f; f++) {
            int cur_f = start_f + f;
            if (selectedFrame == cur_f) {
                g.setColor(TimelineBodyPanel.SELECTED_COLOR);
                g.fillRect(xofs + f * frameWidth + 1, 0, frameWidth, TimelineBodyPanel.FRAME_HEIGHT - 1);
                g.setColor(TimelineBodyPanel.SELECTED_BORDER_COLOR);
                g.drawRect(xofs + f * frameWidth + 1, 0, frameWidth, TimelineBodyPanel.FRAME_HEIGHT - 1);
            }
            g.setColor(fontColor);
            
            if (timeline != null && timeline.timelined instanceof ButtonTag) {
                if (f < 5) {
                    String timeStr = "";
                    switch (f) {
                        case ButtonTag.FRAME_UP:
                            timeStr = "Up";
                            break;
                        case ButtonTag.FRAME_OVER:
                            timeStr = "Over";
                            break;
                        case ButtonTag.FRAME_DOWN:
                            timeStr = "Down";
                            break;
                        case ButtonTag.FRAME_HITTEST:
                            timeStr = "Hit";
                            break;
                    }
                    int w = g.getFontMetrics().stringWidth(timeStr);
                    g.drawString(timeStr, xofs + f * frameWidth + frameWidth / 2 - w / 2 + 1, TimelineBodyPanel.FRAME_HEIGHT - lineLength - lineTextSpace);
                }
            } else {
                if ((cur_f + 1) % 5 == 0 || cur_f == 0) {
                    String timeStr = Integer.toString(cur_f + 1);
                    int w = g.getFontMetrics().stringWidth(timeStr);
                    g.drawString(timeStr, xofs + f * frameWidth + frameWidth / 2 - w / 2 + 1, TimelineBodyPanel.FRAME_HEIGHT - lineLength - lineTextSpace);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int frame = (scrollOffset + e.getX()) / TimelineBodyPanel.getFrameWidthForTimeline(timeline);
        frameSelect(frame);
        for (FrameSelectionListener l : listeners) {
            l.frameSelected(frame, new ArrayList<>());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
