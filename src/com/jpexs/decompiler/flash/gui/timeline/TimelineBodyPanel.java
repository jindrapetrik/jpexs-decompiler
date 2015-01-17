/*
 *  Copyright (C) 2010-2015 JPEXS
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

import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timeline;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class TimelineBodyPanel extends JPanel implements MouseListener, KeyListener {

    private final Timeline timeLine;

    public static Color motionTweenColor = new Color(0x59, 0xfe, 0x7c);
    public static Color shapeTweenColor = new Color(0xd1, 0xac, 0xf1);
    public static Color frameColor = new Color(0xbd, 0xd8, 0xfc);
    public static Color emptyFrameColor = Color.white;
    public static Color emptyFrameSecondColor = new Color(0xea, 0xf2, 0xfc);
    public static Color borderColor = Color.black;
    public static Color emptyBorderColor = new Color(0xbd, 0xd8, 0xfc);
    public static Color keyColor = Color.black;
    public static Color aColor = Color.black;
    public static Color stopColor = Color.white;
    public static Color stopBorderColor = Color.black;
    public static Color borderLinesColor = new Color(0xde, 0xde, 0xde);

    public static Color selectedColor = new Color(113, 174, 235);
    public static final int borderLinesLength = 2;
    public static final float fontSize = 10.0f;

    private final List<FrameSelectionListener> listeners = new ArrayList<>();

    public Point cursor = null;

    public void addFrameSelectionListener(FrameSelectionListener l) {
        listeners.add(l);
    }

    public void removeFrameSelectionListener(FrameSelectionListener l) {
        listeners.remove(l);
    }

    public TimelineBodyPanel(Timeline timeLine) {

        this.timeLine = timeLine;
        Dimension dim = new Dimension(TimelinePanel.FRAME_WIDTH * timeLine.getFrameCount() + 1, TimelinePanel.FRAME_HEIGHT * timeLine.getMaxDepth());
        setSize(dim);
        setPreferredSize(dim);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(TimelinePanel.backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        Rectangle clip = g.getClipBounds();
        int start_f = clip.x / TimelinePanel.FRAME_WIDTH;
        int start_d = clip.y / TimelinePanel.FRAME_HEIGHT;
        int end_f = (clip.x + clip.width) / TimelinePanel.FRAME_WIDTH;
        int end_d = (clip.y + clip.height) / TimelinePanel.FRAME_HEIGHT;

        int max_d = timeLine.getMaxDepth();
        if (max_d < end_d) {
            end_d = max_d;
        }
        int max_f = timeLine.getFrameCount() - 1;
        if (max_f < end_f) {
            end_f = max_f;
        }

        if (end_d - start_d + 1 < 0) {
            return;
        }

        boolean keyfound[] = new boolean[end_d - start_d + 1];

        for (int f = start_f; f <= end_f; f++) {
            for (int d = start_d; d <= end_d; d++) {
                DepthState fl = timeLine.getFrames().get(f).layers.get(d);
                if (fl == null) {
                    if ((f + 1) % 5 == 0) {
                        g.setColor(emptyFrameSecondColor);
                    } else {
                        g.setColor(emptyFrameColor);
                    }
                    g.fillRect(f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                    g.setColor(emptyBorderColor);
                    g.drawRect(f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                }
            }
        }
        for (int f = start_f; f <= end_f; f++) {
            for (int d = start_d; d <= end_d; d++) {
                DepthState fl = timeLine.getFrames().get(f).layers.get(d);
                boolean motionTween = fl == null ? false : fl.motionTween;

                DepthState flNext = null;
                if (f < max_f) {
                    flNext = timeLine.getFrames().get(f + 1).layers.get(d);
                }
                DepthState flPrev = null;
                if (f > 0) {
                    flPrev = timeLine.getFrames().get(f - 1).layers.get(d);
                }

                CharacterTag cht = fl == null ? null : timeLine.swf.getCharacter(fl.characterId);
                boolean shapeTween = cht != null && (cht instanceof MorphShapeTag);
                boolean motionTweenStart = (!motionTween) && (flNext != null && flNext.motionTween);
                boolean motionTweenEnd = (!motionTween) && (flPrev != null && flPrev.motionTween);
                //boolean shapeTweenStart = shapeTween && (flPrev == null || flPrev.characterId != fl.characterId);
                //boolean shapeTweenEnd = shapeTween && (flNext == null || flNext.characterId != fl.characterId);

                if (motionTweenStart || motionTweenEnd) {
                    motionTween = true;
                }
                boolean selected = false;
                if (cursor != null) {
                    if (f == cursor.x && d == cursor.y) {
                        selected = true;
                    }
                }
                if (selected) {
                    //if (!(fl != null && (flNext == null || flNext.key))) {
                    g.setColor(selectedColor);
                    g.fillRect(f * TimelinePanel.FRAME_WIDTH + 1, d * TimelinePanel.FRAME_HEIGHT + 1, TimelinePanel.FRAME_WIDTH - 1, TimelinePanel.FRAME_HEIGHT - 1);
                    //}
                }

                if (fl == null) {

                    if (timeLine.getDepthMaxFrame().containsKey(d) && f < timeLine.getDepthMaxFrame().get(d)) {
                        int draw_f = f;

                        DepthState prev_ds = f < 1 ? null : timeLine.getFrames().get(f - 1).layers.get(d);

                        if (f == 0 || prev_ds != null) {
                            draw_f = f;
                            keyfound[d - start_d] = true;
                        } else if (!keyfound[d - start_d]) {
                            for (; draw_f >= 0; draw_f--) {
                                if (timeLine.getFrames().get(draw_f).layers.get(d) != null) {
                                    if (timeLine.getFrames().get(draw_f).layers.get(d).characterId != -1) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            continue;
                        }
                        int num_frames = 1;
                        for (int i = draw_f + 1; i < timeLine.getFrames().size(); i++) {
                            if (timeLine.getFrames().get(i).layers.get(d) != null) {
                                if (timeLine.getFrames().get(i).layers.get(d).characterId != -1) {
                                    break;
                                }
                            }
                            num_frames++;
                        }
                        g.setColor(emptyFrameColor);
                        g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                        g.setColor(borderColor);
                        g.drawRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);

                        if (selected) {
                            g.setColor(selectedColor);
                            g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                        }

                        g.setColor(keyColor);
                        g.drawOval(draw_f * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT * 3 / 4 - TimelinePanel.FRAME_WIDTH / 2 / 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_WIDTH / 2);

                        if (num_frames > 1) {
                            if (cursor != null && cursor.y == d && cursor.x == f + num_frames - 1) {
                                g.setColor(selectedColor);
                                g.fillRect((f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + 1, d * TimelinePanel.FRAME_HEIGHT + 1, TimelinePanel.FRAME_WIDTH - 1, TimelinePanel.FRAME_HEIGHT - 1);
                            }

                            g.setColor(stopColor);
                            g.fillRect((draw_f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT / 2 - 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_HEIGHT / 2);
                            g.setColor(stopBorderColor);
                            g.drawRect((draw_f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT / 2 - 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_HEIGHT / 2);

                            g.setColor(borderLinesColor);
                            for (int n = draw_f + 1; n < draw_f + num_frames; n++) {
                                g.drawLine(n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + 1, n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + borderLinesLength);
                                g.drawLine(n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT - 1, n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT - borderLinesLength);
                            }
                        }
                    }

                    if (d == 0) {
                        if (!timeLine.getFrames().get(f).actions.isEmpty()) {
                            g.setColor(aColor);
                            g.setFont(getFont().deriveFont(fontSize));
                            int awidth = g.getFontMetrics().stringWidth("a");
                            g.drawString("a", f * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 2 - awidth / 2, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT / 2 + fontSize / 2);
                        }
                    }
                    continue;
                } else {

                    int draw_f = 0;
                    if (fl.key) {
                        draw_f = f;
                        keyfound[d - start_d] = true;
                    } else if (!keyfound[d - start_d]) {
                        for (int k = f - 1; k >= 0; k--) {
                            fl = timeLine.getFrames().get(k).layers.get(d);
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
                        fl = timeLine.getFrames().get(n).layers.get(d);
                        if (fl == null) {
                            break;
                        }
                        if (fl.key) {
                            break;
                        }
                        num_frames++;
                    }
                    g.setColor(frameColor);
                    g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);

                    if (motionTween) {
                        g.setColor(motionTweenColor);
                        g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                    }
                    if (shapeTween) {
                        g.setColor(shapeTweenColor);
                        g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                    }

                    if (selected) {
                        g.setColor(selectedColor);
                        g.fillRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                    }

                    g.setColor(borderColor);
                    g.drawRect(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT, num_frames * TimelinePanel.FRAME_WIDTH, TimelinePanel.FRAME_HEIGHT);
                    //}
                    if ((motionTween || shapeTween)) {
                        g.drawLine(draw_f * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT * 3 / 4,
                                draw_f * TimelinePanel.FRAME_WIDTH + num_frames * TimelinePanel.FRAME_WIDTH - TimelinePanel.FRAME_WIDTH / 2, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT * 3 / 4
                        );
                    }
                    g.setColor(keyColor);
                    g.fillOval(draw_f * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT * 3 / 4 - TimelinePanel.FRAME_WIDTH / 2 / 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_WIDTH / 2);
                    if ((motionTween || shapeTween)) {
                        g.fillOval((draw_f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT * 3 / 4 - TimelinePanel.FRAME_WIDTH / 2 / 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_WIDTH / 2);
                    }

                    if (num_frames > 1) {
                        if (cursor != null && cursor.y == d && cursor.x == f + num_frames - 1) {
                            g.setColor(selectedColor);
                            g.fillRect((f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + 1, d * TimelinePanel.FRAME_HEIGHT + 1, TimelinePanel.FRAME_WIDTH - 1, TimelinePanel.FRAME_HEIGHT - 1);
                        }

                        if (!(motionTween || shapeTween)) {
                            g.setColor(stopColor);
                            g.fillRect((draw_f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT / 2 - 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_HEIGHT / 2);
                            g.setColor(stopBorderColor);
                            g.drawRect((draw_f + num_frames - 1) * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 4, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT / 2 - 2, TimelinePanel.FRAME_WIDTH / 2, TimelinePanel.FRAME_HEIGHT / 2);
                        }
                        g.setColor(borderLinesColor);
                        for (int n = draw_f + 1; n < draw_f + num_frames; n++) {
                            g.drawLine(n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + 1, n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + borderLinesLength);
                            g.drawLine(n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT - 1, n * TimelinePanel.FRAME_WIDTH, d * TimelinePanel.FRAME_HEIGHT + TimelinePanel.FRAME_HEIGHT - borderLinesLength);
                        }
                    }
                }
            }
        }

        if (cursor != null && cursor.x >= start_f && cursor.x <= end_f) {
            g.setColor(TimelinePanel.selectedBorderColor);
            g.drawLine(cursor.x * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 2, 0, cursor.x * TimelinePanel.FRAME_WIDTH + TimelinePanel.FRAME_WIDTH / 2, getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void frameSelect(int frame, int depth) {
        if (cursor != null && cursor.x == frame && (cursor.y == depth || depth == -1)) {
            return;
        }
        if (depth == -1 && cursor != null) {
            depth = cursor.y;
        }
        cursor = new Point(frame, depth);
        for (FrameSelectionListener l : listeners) {
            l.frameSelected(frame, depth);
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        p.x = p.x / TimelinePanel.FRAME_WIDTH;
        p.y = p.y / TimelinePanel.FRAME_HEIGHT;
        if (p.x >= timeLine.getFrameCount()) {
            p.x = timeLine.getFrameCount() - 1;
        }
        int maxDepth = timeLine.getMaxDepth();
        if (p.y > maxDepth) {
            p.y = maxDepth;
        }
        frameSelect(p.x, p.y);
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

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 37: //left
                if (cursor.x > 0) {
                    frameSelect(cursor.x - 1, cursor.y);
                }
                break;
            case 39: //right
                if (cursor.x < timeLine.getFrames().size() - 1) {
                    frameSelect(cursor.x + 1, cursor.y);
                }
                break;
            case 38: //up
                if (cursor.y > 0) {
                    frameSelect(cursor.x, cursor.y - 1);
                }
                break;
            case 40: //down
                if (cursor.y < timeLine.getMaxDepth()) {
                    frameSelect(cursor.x, cursor.y + 1);
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

}
