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

    public static final Color BORDER_COLOR = Color.black;

    public static final int LINE_LENGTH = 3;

    public static final int LINE_TEXT_SPACE = 3;

    public static final Color FONT_COLOR = Color.black;

    public float fontSize = 10.0f;

    private int scrollOffset = 0;

    private int selectedFrame = -1;
    
    private Timeline timeline;

    private final List<FrameSelectionListener> listeners = new ArrayList<>();   
    
    private final TimelineDepthPanel depthPanel;

    public TimelineTimePanel(TimelineDepthPanel depthPanel) {
        Dimension dim = new Dimension(Integer.MAX_VALUE, TimelineBodyPanel.FRAME_HEIGHT);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(new Dimension(0, TimelineBodyPanel.FRAME_HEIGHT));
        addMouseListener(this);
        this.depthPanel = depthPanel;
    }

    public void setTimeline(Timeline timeline) {
        setFont(getFont().deriveFont(TimelineDepthPanel.FONT_SIZE));
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
        g.setColor(BORDER_COLOR);
        int xofs = -scrollOffset % frameWidth;
        for (int f = 0; f <= end_f; f++) {
            g.drawLine(xofs + f * frameWidth + 1, TimelineBodyPanel.FRAME_HEIGHT - 1, xofs + f * frameWidth + 1, TimelineBodyPanel.FRAME_HEIGHT - LINE_LENGTH);
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
            g.setColor(FONT_COLOR);
            
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
                    g.drawString(timeStr, xofs + f * frameWidth + frameWidth / 2 - w / 2 + 1, TimelineBodyPanel.FRAME_HEIGHT - LINE_LENGTH - LINE_TEXT_SPACE);
                }
            } else {
                if ((cur_f + 1) % 5 == 0 || cur_f == 0) {
                    String timeStr = Integer.toString(cur_f + 1);
                    int w = g.getFontMetrics().stringWidth(timeStr);
                    g.drawString(timeStr, xofs + f * frameWidth + frameWidth / 2 - w / 2 + 1, TimelineBodyPanel.FRAME_HEIGHT - LINE_LENGTH - LINE_TEXT_SPACE);
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
