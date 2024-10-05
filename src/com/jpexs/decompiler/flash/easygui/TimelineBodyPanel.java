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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JPanel;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;

/**
 * @author JPEXS
 */
public class TimelineBodyPanel extends JPanel implements MouseListener, KeyListener {

    private final Timeline timeline;
    
    public static final int FRAME_WIDTH = 8;

    public static final int FRAME_HEIGHT = 18;

    public static final Color SHAPE_TWEEN_COLOR = new Color(0x59, 0xfe, 0x7c);

    public static final Color MOTION_TWEEN_COLOR = new Color(0xd1, 0xac, 0xf1);

    //public static final Color frameColor = new Color(0xbd, 0xd8, 0xfc);
    public static final Color BORDER_COLOR = Color.black;

    public static final Color EMPTY_BORDER_COLOR = new Color(0xbd, 0xd8, 0xfc);

    public static final Color KEY_COLOR = Color.black;

    public static final Color A_COLOR = Color.black;

    public static final Color STOP_COLOR = Color.white;

    public static final Color STOP_BORDER_COLOR = Color.black;

    public static final Color BORDER_LINES_COLOR = new Color(0xde, 0xde, 0xde);

    public static final Color SELECTED_COLOR = new Color(0xff, 0x99, 0x99);

    public static final Color SELECTED_BORDER_COLOR = new Color(0xcc, 0, 0);
    
    //public static final Color SELECTED_COLOR = new Color(113, 174, 235);
    public static final int BORDER_LINES_LENGTH = 2;

    public static final float FONT_SIZE = 10.0f;

    private final List<FrameSelectionListener> listeners = new ArrayList<>();

    public Point cursor = null;
    
    
    private int frame = 1;

    private enum BlockType {

        EMPTY, NORMAL, MOTION_TWEEN, SHAPE_TWEEN
    }

    public static Color getEmptyFrameColor() {
        return SubstanceColorUtilities.getLighterColor(getControlColor(), 0.7);
    }

    public static Color getEmptyFrameSecondColor() {
        return SubstanceColorUtilities.getLighterColor(getControlColor(), 0.9);
    }

    public static Color getBackgroundColor() {
        return SystemColor.control;
    }
    
    public static Color getSelectedColor() {
        return SystemColor.textHighlight;
    }

    private static Color getControlColor() {
        return SystemColor.control;
    }

    public static Color getFrameColor() {
        return SubstanceColorUtilities.getDarkerColor(getControlColor(), 0.1);
    }

    public void addFrameSelectionListener(FrameSelectionListener l) {
        listeners.add(l);
    }

    public void removeFrameSelectionListener(FrameSelectionListener l) {
        listeners.remove(l);
    }

    public TimelineBodyPanel(Timeline timeline) {

        this.timeline = timeline;
        Dimension dim = new Dimension(FRAME_WIDTH * timeline.getFrameCount() + 1, FRAME_HEIGHT * (timeline.getMaxDepth() + 1));
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

        g.setColor(getBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());
        Rectangle clip = g.getClipBounds();
        int frameWidth = FRAME_WIDTH;
        int frameHeight = FRAME_HEIGHT;
        int start_f = clip.x / frameWidth;
        int start_d = clip.y / frameHeight;
        int end_f = (clip.x + clip.width) / frameWidth;
        int end_d = (clip.y + clip.height) / frameHeight;

        int max_d = timeline.getMaxDepth();
        if (max_d < end_d) {
            end_d = max_d;
        }
        int max_f = timeline.getFrameCount() - 1;
        if (max_f < end_f) {
            end_f = max_f;
        }

        if (end_d - start_d + 1 < 0) {
            return;
        }

        // draw background
        for (int f = start_f; f <= end_f; f++) {
            g.setColor((f + 1) % 5 == 0 ? getEmptyFrameSecondColor() : getEmptyFrameColor());
            g.fillRect(f * frameWidth, start_d * frameHeight, frameWidth, (end_d - start_d + 1) * frameHeight);
            g.setColor(EMPTY_BORDER_COLOR);
            for (int d = start_d; d <= end_d; d++) {
                g.drawRect(f * frameWidth, d * frameHeight, frameWidth, frameHeight);
            }
        }

        // draw selected cell
        if (cursor != null) {
            g.setColor(getSelectedColor());
            g.fillRect(cursor.x * frameWidth + 1, cursor.y * frameHeight + 1, frameWidth - 1, frameHeight - 1);
        }

        g.setColor(A_COLOR);
        g.setFont(getFont().deriveFont(FONT_SIZE));
        int awidth = g.getFontMetrics().stringWidth("a");
        for (int f = start_f; f <= end_f; f++) {
            if (!timeline.getFrame(f).actions.isEmpty()) {
                g.drawString("a", f * frameWidth + frameWidth / 2 - awidth / 2, frameHeight / 2 + FONT_SIZE / 2);
            }
        }

        Map<Integer, Integer> depthMaxFrames = timeline.getDepthMaxFrame();
        for (int d = start_d; d <= end_d; d++) {
            int maxFrame = depthMaxFrames.containsKey(d) ? depthMaxFrames.get(d) : -1;
            if (maxFrame < 0) {
                continue;
            }

            int end_f2 = Math.min(end_f, maxFrame);
            int start_f2 = Math.min(start_f, end_f2);

            // find the start frame number of the current block
            DepthState dsStart = timeline.getFrame(start_f2).layers.get(d);
            for (; start_f2 >= 1; start_f2--) {
                DepthState ds = timeline.getFrame(start_f2 - 1).layers.get(d);
                if (((dsStart == null) != (ds == null))
                        || (ds != null && (dsStart.characterId != ds.characterId || !Objects.equals(dsStart.className, ds.className)))) {
                    break;
                }
            }

            for (int f = start_f2; f <= end_f2; f++) {
                DepthState fl = timeline.getFrame(f).layers.get(d);
                boolean motionTween = fl == null ? false : fl.motionTween;

                DepthState flNext = f < max_f ? timeline.getFrame(f + 1).layers.get(d) : null;
                DepthState flPrev = f > 0 ? timeline.getFrame(f - 1).layers.get(d) : null;

                CharacterTag cht = fl == null ? null : fl.getCharacter();
                boolean shapeTween = cht != null && (cht instanceof MorphShapeTag);
                boolean motionTweenStart = !motionTween && (flNext != null && flNext.motionTween);
                boolean motionTweenEnd = !motionTween && (flPrev != null && flPrev.motionTween);
                //boolean shapeTweenStart = shapeTween && (flPrev == null || flPrev.characterId != fl.characterId);
                //boolean shapeTweenEnd = shapeTween && (flNext == null || flNext.characterId != fl.characterId);

                /*if (motionTweenStart || motionTweenEnd) {
                 motionTween = true;
                 }*/
                int draw_f = f;
                int num_frames = 1;
                Color backColor;
                BlockType blockType;
                if (fl == null) {
                    for (; f + 1 < timeline.getFrameCount(); f++) {
                        fl = timeline.getFrame(f + 1).layers.get(d);
                        if (fl != null && fl.getCharacter() != null) {
                            break;
                        }

                        num_frames++;
                    }

                    backColor = getEmptyFrameColor();
                    blockType = BlockType.EMPTY;
                } else {
                    for (; f + 1 < timeline.getFrameCount(); f++) {
                        fl = timeline.getFrame(f + 1).layers.get(d);
                        if (fl == null || fl.key) {
                            break;
                        }

                        num_frames++;
                    }

                    backColor = shapeTween ? SHAPE_TWEEN_COLOR : motionTween ? MOTION_TWEEN_COLOR : getFrameColor();
                    blockType = shapeTween ? BlockType.SHAPE_TWEEN : motionTween ? BlockType.MOTION_TWEEN : BlockType.NORMAL;
                }

                drawBlock(g, backColor, d, draw_f, num_frames, blockType);
            }
        }

        if (cursor != null && cursor.x >= start_f && cursor.x <= end_f) {
            g.setColor(SELECTED_BORDER_COLOR);
            g.drawLine(cursor.x * frameWidth + frameWidth / 2, 0, cursor.x * frameWidth + frameWidth / 2, getHeight());
        }
    }

    private void drawBlock(Graphics2D g, Color backColor, int depth, int frame, int num_frames, BlockType blockType) {
        int frameWidth = FRAME_WIDTH;
        int frameHeight = FRAME_HEIGHT;

        g.setColor(backColor);
        g.fillRect(frame * frameWidth, depth * frameHeight, num_frames * frameWidth, frameHeight);
        g.setColor(BORDER_COLOR);
        g.drawRect(frame * frameWidth, depth * frameHeight, num_frames * frameWidth, frameHeight);

        boolean selected = false;
        if (cursor != null && frame <= cursor.x && (frame + num_frames) > cursor.x && depth == cursor.y) {
            selected = true;
        }

        if (selected) {
            g.setColor(getSelectedColor());
            g.fillRect(cursor.x * frameWidth + 1, depth * frameHeight + 1, frameWidth - 1, frameHeight - 1);
        }

        boolean isTween = blockType == BlockType.MOTION_TWEEN || blockType == BlockType.SHAPE_TWEEN;

        g.setColor(KEY_COLOR);
        if (isTween) {
            g.drawLine(frame * frameWidth, depth * frameHeight + frameHeight * 3 / 4,
                    frame * frameWidth + num_frames * frameWidth - frameWidth / 2, depth * frameHeight + frameHeight * 3 / 4
            );
        }

        if (blockType == BlockType.EMPTY) {
            g.drawOval(frame * frameWidth + frameWidth / 4, depth * frameHeight + frameHeight * 3 / 4 - frameWidth / 2 / 2, frameWidth / 2, frameWidth / 2);
        } else {
            g.fillOval(frame * frameWidth + frameWidth / 4, depth * frameHeight + frameHeight * 3 / 4 - frameWidth / 2 / 2, frameWidth / 2, frameWidth / 2);
        }

        if (num_frames > 1) {
            int endFrame = frame + num_frames - 1;
            if (isTween) {
                g.fillOval(endFrame * frameWidth + frameWidth / 4, depth * frameHeight + frameHeight * 3 / 4 - frameWidth / 2 / 2, frameWidth / 2, frameWidth / 2);
            } else {
                g.setColor(STOP_COLOR);
                g.fillRect(endFrame * frameWidth + frameWidth / 4, depth * frameHeight + frameHeight / 2 - 2, frameWidth / 2, frameHeight / 2);
                g.setColor(STOP_BORDER_COLOR);
                g.drawRect(endFrame * frameWidth + frameWidth / 4, depth * frameHeight + frameHeight / 2 - 2, frameWidth / 2, frameHeight / 2);
            }

            g.setColor(BORDER_LINES_COLOR);
            for (int n = frame + 1; n < frame + num_frames; n++) {
                g.drawLine(n * frameWidth, depth * frameHeight + 1, n * frameWidth, depth * frameHeight + BORDER_LINES_LENGTH);
                g.drawLine(n * frameWidth, depth * frameHeight + frameHeight - 1, n * frameWidth, depth * frameHeight + frameHeight - BORDER_LINES_LENGTH);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void depthSelect(int depth) {
        frameSelect(frame, depth);
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
        this.frame = frame;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        p.x = p.x / FRAME_WIDTH;
        p.y = p.y / FRAME_HEIGHT;
        if (p.x >= timeline.getFrameCount()) {
            p.x = timeline.getFrameCount() - 1;
        }
        int maxDepth = timeline.getMaxDepth();
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
                if (cursor.x < timeline.getFrameCount() - 1) {
                    frameSelect(cursor.x + 1, cursor.y);
                }
                break;
            case 38: //up
                if (cursor.y > 0) {
                    frameSelect(cursor.x, cursor.y - 1);
                }
                break;
            case 40: //down
                if (cursor.y < timeline.getMaxDepth()) {
                    frameSelect(cursor.x, cursor.y + 1);
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    public Rectangle getDepthBounds(int depth) {
        return getFrameBounds(frame, depth);
    }
    public Rectangle getFrameBounds(int frame, int depth) {
        Rectangle rect = new Rectangle();
        rect.width = FRAME_WIDTH;
        rect.height = FRAME_HEIGHT;
        rect.x = frame * FRAME_WIDTH;
        rect.y = depth * FRAME_HEIGHT;
        return rect;
    }
}

