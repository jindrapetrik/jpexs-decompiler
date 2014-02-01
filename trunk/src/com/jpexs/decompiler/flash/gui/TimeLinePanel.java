/*
 * Copyright (C) 2014 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.TimeLine;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class TimeLinePanel extends JPanel implements MouseListener {

    private SWF swf;
    private TimeLine timeLine;
    public static final int FRAME_WIDTH = 10;
    public static final int FRAME_HEIGHT = 20;
    public static final Color frameColor = Color.lightGray;
    public static final Color emptyFrameColor = Color.white;
    public static final Color borderColor = Color.black;
    public static final Color emptyBorderColor = Color.lightGray;
    public static final Color keyColor = Color.black;
    public static final Color selectedColor = new Color(113, 174, 235);
    public static final Color timeColor = Color.red;

    public Point cursor = null;

    public TimeLinePanel(SWF swf) {
        this.swf = swf;
        this.timeLine = new TimeLine(swf);
        Dimension dim = new Dimension(FRAME_WIDTH * timeLine.getFrameCount()+1, FRAME_HEIGHT * timeLine.getMaxDepth());
        setSize(dim);
        setPreferredSize(dim);
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
        Rectangle clip = g.getClipBounds();
        int start_f = clip.x / FRAME_WIDTH;
        int start_d = clip.y / FRAME_HEIGHT;
        int end_f = (clip.x + clip.width) / FRAME_WIDTH;
        int end_d = (clip.y + clip.height) / FRAME_HEIGHT;

        int max_d = timeLine.getMaxDepth();
        if (max_d < end_d) {
            end_d = max_d;
        }
        int max_f = timeLine.getFrameCount() - 1;
        if (max_f < end_f) {
            end_f = max_f;
        }

        boolean keyfound[] = new boolean[end_d - start_d + 1];
        
        for (int f = start_f; f <= end_f; f++) {
            for (int d = start_d; d <= end_d; d++) {
                DepthState fl = timeLine.frames.get(f).layers.get(d);
                if(fl == null){
                    g.setColor(emptyFrameColor);
                    g.fillRect(f * FRAME_WIDTH, d * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
                    g.setColor(emptyBorderColor);
                    g.drawRect(f * FRAME_WIDTH, d * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
                }
            }
        }
        for (int f = start_f; f <= end_f; f++) {
            for (int d = start_d; d <= end_d; d++) {
                DepthState fl = timeLine.frames.get(f).layers.get(d);
                boolean selected = false;
                if (cursor != null) {
                    if (f == cursor.x && d == cursor.y) {
                        selected = true;
                    }
                }
                if (selected) {
                    g.setColor(selectedColor);
                    g.fillRect(f * FRAME_WIDTH+1, d * FRAME_HEIGHT+1, FRAME_WIDTH-1, FRAME_HEIGHT-1);
                }

                if (fl == null) {
                    continue;
                } else {

                    int draw_f = 0;
                    if (fl.key) {
                        draw_f = f;
                        keyfound[d - start_d] = true;
                    } else if (!keyfound[d - start_d]) {
                        for (int k = f - 1; k >= 0; k--) {
                            fl = timeLine.frames.get(k).layers.get(d);
                            if (fl == null) {
                                break;
                            }
                            if (fl.key) {
                                keyfound[d - start_d] = true;
                                draw_f = k;
                                break;
                            }
                        }
                    } else {
                        continue;
                    }
                    int num_frames = 1;
                    for (int n = draw_f + 1; n < timeLine.getFrameCount(); n++) {
                        fl = timeLine.frames.get(n).layers.get(d);
                        if (fl == null) {
                            break;
                        }
                        if (fl.key) {
                            break;
                        }
                        num_frames++;
                    }
                    g.setColor(frameColor);
                    g.fillRect(draw_f * FRAME_WIDTH, d * FRAME_HEIGHT, num_frames * FRAME_WIDTH, FRAME_HEIGHT);
                    
                    if (selected) {
                        g.setColor(selectedColor);
                        g.fillRect(draw_f * FRAME_WIDTH, d * FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT);
                    }
                    
                    g.setColor(borderColor);
                    g.drawRect(draw_f * FRAME_WIDTH, d * FRAME_HEIGHT, num_frames * FRAME_WIDTH, FRAME_HEIGHT);
                    g.setColor(keyColor);
                    g.fillOval(draw_f * FRAME_WIDTH + FRAME_WIDTH / 4, d * FRAME_HEIGHT + FRAME_HEIGHT / 2 - FRAME_WIDTH / 2, FRAME_WIDTH / 2, FRAME_WIDTH / 2);

                }
            }
        }
        
        if(cursor!=null && cursor.x>=start_f && cursor.x<=end_f){
            g.setColor(timeColor);
            g.drawLine(cursor.x*FRAME_WIDTH + FRAME_WIDTH/2, 0, cursor.x*FRAME_WIDTH + FRAME_WIDTH/2, getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        p.x = p.x / FRAME_WIDTH;
        p.y = p.y / FRAME_HEIGHT;
        if (p.x >= timeLine.getFrameCount()) {
            p.x = timeLine.getFrameCount() - 1;
        }
        if (p.y > timeLine.getMaxDepth()) {
            p.y = timeLine.getMaxDepth();
        }
        cursor = p;
        repaint();
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
