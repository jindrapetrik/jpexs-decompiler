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

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.internal.utils.SubstanceColorUtilities;

/**
 * @author JPEXS
 */
public class TimelineBodyPanel extends JPanel implements MouseListener, KeyListener {

    private Timeline timeline;

    private static final int FRAME_WIDTH = 8;

    private static final int BUTTON_FRAME_WIDTH = 40;

    private static final int MARKER_SIZE = 4;

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

    private final List<FrameSelectionListener> selectionListeners = new ArrayList<>();

    private final List<Runnable> changeListeners = new ArrayList<>();

    public Set<Point> cursor = new LinkedHashSet<>();
    private final EasySwfPanel swfPanel;

    /*private int frame = 0;

    private int endFrame = 0;

    private int depth = 0;

    private int endDepth = 0;*/
    private final UndoManager undoManager;

    private boolean ctrlDown = false;

    private boolean altDown = false;

    private boolean shiftDown = false;

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
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
        } else {
            return SystemColor.control;
        }
    }

    public static Color getSelectedColorText() {
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getForegroundColor();
        } else {
            return SystemColor.textHighlightText;
        }
    }

    public static Color getFrameColor() {
        return SubstanceColorUtilities.getDarkerColor(getControlColor(), 0.1);
    }

    public static Color getSelectedColor() {
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ROLLOVER_SELECTED).getBackgroundFillColor();
        } else {
            return SystemColor.textHighlight;
        }
    }

    private static Color getControlColor() {
        if (Configuration.useRibbonInterface.get()) {
            return SubstanceLookAndFeel.getCurrentSkin().getColorScheme(DecorationAreaType.GENERAL, ColorSchemeAssociationKind.FILL, ComponentState.ENABLED).getBackgroundFillColor();
        } else {
            return SystemColor.control;
        }
    }

    public void addFrameSelectionListener(FrameSelectionListener l) {
        selectionListeners.add(l);
    }

    public void removeFrameSelectionListener(FrameSelectionListener l) {
        selectionListeners.remove(l);
    }

    public void addChangeListener(Runnable l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(Runnable l) {
        changeListeners.remove(l);
    }

    private void fireChanged() {
        for (Runnable l : changeListeners) {
            l.run();
        }
    }

    public TimelineBodyPanel(EasySwfPanel swfPanel, UndoManager undoManager) {
        refresh();
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        this.swfPanel = swfPanel;
        this.undoManager = undoManager;

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if ((e.getID() == KeyEvent.KEY_PRESSED) || (e.getID() == KeyEvent.KEY_RELEASED)) {
                    ctrlDown = ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK);
                    altDown = ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK);
                    shiftDown = ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK);
                }
                return false;
            }
        });
    }

    public static int getFrameWidthForTimeline(Timeline timeline) {
        if (timeline == null) {
            return FRAME_WIDTH;
        }
        if (timeline.timelined instanceof ButtonTag) {
            return BUTTON_FRAME_WIDTH;
        }
        return FRAME_WIDTH;
    }

    public int getFrameWidth() {
        return getFrameWidthForTimeline(timeline);
    }

    @Override
    protected void paintComponent(Graphics g1) {

        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(getBackgroundColor());
        g.fillRect(0, 0, getWidth(), getHeight());

        if (timeline == null) {
            return;
        }

        int frameWidth = getFrameWidth();

        Set<Integer> emptyFrames = new LinkedHashSet<>();
        if (timeline.timelined instanceof ButtonTag) {
            emptyFrames = ((ButtonTag) timeline.timelined).getEmptyFrames();
            frameWidth = BUTTON_FRAME_WIDTH;
        }
        Rectangle clip = g.getClipBounds();
        int frameHeight = FRAME_HEIGHT;
        int start_f = clip.x / frameWidth;
        int start_d = clip.y / frameHeight;
        int end_f = (clip.x + clip.width) / frameWidth;
        int end_d = (clip.y + clip.height) / frameHeight;

        int max_d = timeline.getMaxDepth();
        if (max_d < end_d) {
            //end_d = max_d;
        }
        int max_f = timeline.getFrameCount() - 1;
        if (max_f < end_f) {
            //end_f = max_f;
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
        for (Point c : cursor) {
            g.setColor(getSelectedColor());
            g.fillRect(c.x * frameWidth + 1, c.y * frameHeight + 1, frameWidth - 1, frameHeight - 1);
        }

        int awidth = g.getFontMetrics().stringWidth("a");
        boolean firstAction = true;
        for (int f = start_f; f <= end_f || (firstAction && f <= max_f); f++) {
            Frame fr = timeline.getFrame(f);
            if (fr != null && !fr.actions.isEmpty()) {
                if (firstAction) {
                    drawBlock(g, getEmptyFrameColor(), 0, 0, f, BlockType.EMPTY, frameWidth);
                }

                int f2 = f + 1;
                while (f2 <= max_f && timeline.getFrame(f2) != null && timeline.getFrame(f2).actions.isEmpty()) {
                    f2++;
                }
                drawBlock(g, getEmptyFrameColor(), 0, f, f2 - f, BlockType.EMPTY, frameWidth);
                g.setColor(A_COLOR);
                g.setFont(getFont().deriveFont(FONT_SIZE));
                g.drawString("a", f * frameWidth + frameWidth / 2 - awidth / 2, frameHeight / 2);
                firstAction = false;
            }
        }

        Map<Integer, Integer> depthMaxFrames = timeline.getDepthMaxFrameButtons();
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

                if (emptyFrames.contains(f)) {
                    fl = null;
                }

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

                drawBlock(g, backColor, d, draw_f, num_frames, blockType, frameWidth);
            }
        }

        Point cursorFirst = cursor.isEmpty() ? null : cursor.iterator().next();
        if (cursorFirst != null && cursorFirst.x >= start_f && cursorFirst.x <= end_f) {
            g.setColor(SELECTED_BORDER_COLOR);
            g.drawLine(cursorFirst.x * frameWidth + frameWidth / 2, 0, cursorFirst.x * frameWidth + frameWidth / 2, getHeight());
        }
    }

    private void drawBlock(Graphics2D g, Color backColor, int depth, int frame, int num_frames, BlockType blockType, int frameWidth) {
        int frameHeight = FRAME_HEIGHT;

        for (int n = frame; n < frame + num_frames; n++) {
            if (cursor != null && cursor.contains(new Point(n, depth))) {
                g.setColor(getSelectedColor());
            } else {
                g.setColor(backColor);
            }
            g.fillRect(n * frameWidth, depth * frameHeight, frameWidth, frameHeight);
        }

        g.setColor(BORDER_COLOR);
        g.drawRect(frame * frameWidth, depth * frameHeight, num_frames * frameWidth, frameHeight);

        //boolean selected = cursor != null && cursor.contains(cursor.x, depth);
        /*if (cursor != null && cursor.contains(frame, depth) && !cursor.contains(frame + num_frames, depth)) {//frame <= cursor.x && (frame + num_frames) > cursor.x && depth == cursor.y) {
            selected = true;
        }*/
        for (int n = frame + 1; n < frame + num_frames; n++) {
            g.setColor(getSelectedColor());
            if (cursor.contains(new Point(n, depth))) {
                g.fillRect(n * frameWidth + 1, depth * frameHeight + 1, frameWidth, frameHeight);
            }
        }

        /*if (selected) {
            g.setColor(getSelectedColor());
            g.fillRect(cursor.x * frameWidth + 1, depth * frameHeight + 1, (cursor.width * frameWidth) - 1, frameHeight - 1);
        }*/
        boolean isTween = blockType == BlockType.MOTION_TWEEN || blockType == BlockType.SHAPE_TWEEN;

        g.setColor(KEY_COLOR);
        if (isTween) {
            g.drawLine(frame * frameWidth, depth * frameHeight + frameHeight * 3 / 4,
                    frame * frameWidth + num_frames * frameWidth - frameWidth / 2, depth * frameHeight + frameHeight * 3 / 4
            );
        }

        if (cursor != null && cursor.contains(new Point(frame, depth))) {
            g.setBackground(getSelectedColorText());
        } else {
            g.setColor(KEY_COLOR);
        }
        if (blockType == BlockType.EMPTY) {
            g.drawOval(frame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, depth * frameHeight + frameHeight * 3 / 4 - MARKER_SIZE / 2, MARKER_SIZE, MARKER_SIZE);
        } else {
            g.fillOval(frame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, depth * frameHeight + frameHeight * 3 / 4 - MARKER_SIZE / 2, MARKER_SIZE, MARKER_SIZE);
        }

        if (num_frames > 1) {
            int endFrame = frame + num_frames - 1;
            if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                g.setBackground(getSelectedColorText());
            } else {
                g.setColor(KEY_COLOR);
            }
            if (isTween) {
                g.fillOval(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, depth * frameHeight + frameHeight * 3 / 4 - MARKER_SIZE / 2, MARKER_SIZE, MARKER_SIZE);
            } else {

                if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(STOP_COLOR);
                }
                g.fillRect(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, depth * frameHeight + frameHeight / 2 - 2, MARKER_SIZE, frameHeight / 2);
                if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(STOP_BORDER_COLOR);
                }
                g.drawRect(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, depth * frameHeight + frameHeight / 2 - 2, MARKER_SIZE, frameHeight / 2);
            }

            for (int n = frame + 1; n < frame + num_frames; n++) {
                if (cursor != null && cursor.contains(new Point(n, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(BORDER_LINES_COLOR);
                }
                g.drawLine(n * frameWidth, depth * frameHeight + 1, n * frameWidth, depth * frameHeight + BORDER_LINES_LENGTH);
                g.drawLine(n * frameWidth, depth * frameHeight + frameHeight - 1, n * frameWidth, depth * frameHeight + frameHeight - BORDER_LINES_LENGTH);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    public void depthSelect(int depth) {
        frameSelect(getFirstFrame(), Arrays.asList(depth));
    }

    public void depthsSelect(List<Integer> depths) {
        frameSelect(getFirstFrame(), depths);
    }

    public int getFirstFrame() {
        if (cursor.isEmpty()) {
            return 0;
        }
        return cursor.iterator().next().x;
    }

    public int getFirstDepth() {
        if (cursor.isEmpty()) {
            return 0;
        }
        return cursor.iterator().next().y;
    }

    public void rectSelect(int frame, int depth, int endFrame, int endDepth) {
        int x1 = Math.min(frame, endFrame);
        int x2 = Math.max(frame, endFrame);
        int y1 = Math.min(depth, endDepth);
        int y2 = Math.max(depth, endDepth);
        Set<Point> newCursor = new LinkedHashSet<>();
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                newCursor.add(new Point(x, y));
            }
        }
        if (cursor.equals(newCursor)) {
            return;
        }
        cursor.clear();
        cursor.addAll(newCursor);

        /*cursor = new Rectangle(
                x1,
                y1,
                x2 - x1 + 1,
                y2 - y1 + 1);*/
        repaint();
        /*this.frame = frame;
        this.depth = depth;
        this.endFrame = endFrame;
        this.endDepth = endDepth;*/
        scrollRectToVisible(getFrameBounds(frame, depth));
        List<Integer> depths = new ArrayList<>();
        for (int d = depth; d <= endDepth; d++) {
            depths.add(d);
        }
        fireFrameSelected(frame, depths);
    }

    public void frameSelect(int frame, int depth) {
        frameSelect(frame, Arrays.asList(depth));
    }

    public void frameSelect(int frame, List<Integer> depths) {
        /*if (cursor != null && cursor.width == 1 && cursor.height == 1 && (cursor.contains(frame, depth) || (depth == -1 && cursor.contains(frame, cursor.y)))) {
            return;
        }
        if (depth == -1 && cursor != null) {
            depth = cursor.y;
        }*/
        //rectSelect(frame, depth, frame, depth);

        Set<Point> newCursor = new LinkedHashSet<>();

        for (int d : depths) {
            newCursor.add(new Point(frame, d));
        }
        if (depths.isEmpty()) {
            newCursor.add(new Point(frame, 0));
        }

        cursor.clear();
        cursor.addAll(newCursor);

        repaint();

        /*
        this.frame = frame;
        this.depth = depths.isEmpty() ? 0 : depths.get(0);
        this.endFrame = frame;
         */
        fireFrameSelected(frame, depths);
    }

    private void fireFrameSelected(int frame, List<Integer> depths) {
        for (FrameSelectionListener l : selectionListeners) {
            l.frameSelected(frame, depths);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (timeline == null) {
            return;
        }
        Point p = e.getPoint();
        p.x = p.x / getFrameWidth();
        p.y = p.y / FRAME_HEIGHT;
        /*if (p.x >= timeline.getFrameCount()) {
            p.x = timeline.getFrameCount() - 1;
        }*/
        int maxDepth = timeline.getMaxDepth();
        /*if (p.y > maxDepth) {
            p.y = maxDepth;
        }*/
        if (shiftDown) {
            rectSelect(getFirstFrame(), getFirstDepth(), p.x, p.y);
        } else if (ctrlDown) {
            int frame = getFirstFrame();
            if (cursor.contains(p)) {
                cursor.remove(p);
            } else {
                cursor.add(p);
            }
            List<Integer> newDepths = new ArrayList<>();
            for (Point t : cursor) {
                if (t.x == frame) {
                    newDepths.add(t.y);
                }
            }

            repaint();
            fireFrameSelected(frame, newDepths);
        } else {
            if (!(cursor != null && cursor.contains(p) && SwingUtilities.isRightMouseButton(e))) {
                frameSelect(p.x, Arrays.asList(p.y));
            }
        }
        requestFocusInWindow();
        if (SwingUtilities.isRightMouseButton(e)) {

            JPopupMenu popupMenu = new JPopupMenu();

            int frame = getFirstFrame();
            int depth = getFirstDepth();

            Frame fr = timeline.getFrame(frame);
            DepthState ds = fr == null ? null : fr.layers.get(depth);
            Set<Integer> emptyFrames = new LinkedHashSet<>();
            if (timeline.timelined instanceof ButtonTag) {
                emptyFrames = ((ButtonTag) timeline.timelined).getEmptyFrames();
            }
            boolean thisEmpty = emptyFrames.contains(frame) || ds == null || ds.getCharacter() == null;
            boolean previousEmpty = true;
            boolean emptyDepth = true;
            boolean somethingBefore = false;
            boolean somethingAfter = false;
            if (frame > 0) {
                fr = timeline.getFrame(frame - 1);
                ds = fr == null ? null : fr.layers.get(depth);
                previousEmpty = emptyFrames.contains(frame - 1) || ds == null || ds.getCharacter() == null;
            }
            for (int f = frame - 1; f >= 0; f--) {
                fr = timeline.getFrame(f);
                ds = fr == null ? null : fr.layers.get(depth);
                boolean empty = emptyFrames.contains(f) || ds == null || ds.getCharacter() == null;
                if (!empty) {
                    somethingBefore = true;
                    break;
                }
            }
            for (int f = frame + 1; f < timeline.getFrameCount(); f++) {
                fr = timeline.getFrame(f);
                ds = fr == null ? null : fr.layers.get(depth);
                boolean empty = emptyFrames.contains(f) || ds == null || ds.getCharacter() == null;
                if (!empty) {
                    somethingAfter = true;
                    break;
                }
            }
            emptyDepth = thisEmpty && !somethingBefore && !somethingAfter;

            JMenuItem addKeyFrameMenuItem = new JMenuItem(EasyStrings.translate("action.addKeyFrame"));
            addKeyFrameMenuItem.addActionListener(this::addKeyFrame);
            JMenuItem addKeyFrameEmptyBeforeMenuItem = new JMenuItem(EasyStrings.translate("action.addKeyFrameWithBlankFrameBefore"));
            addKeyFrameEmptyBeforeMenuItem.addActionListener(this::addKeyFrameEmptyBefore);
            JMenuItem addFrameMenuItem = new JMenuItem(EasyStrings.translate("action.addFrame"));
            addFrameMenuItem.addActionListener(this::addFrame);
            JMenuItem removeFrameMenuItem = new JMenuItem(EasyStrings.translate("action.removeFrame"));
            removeFrameMenuItem.addActionListener(this::removeFrame);

            boolean multiSelect = cursor != null && (cursor.size() > 1);

            if (!thisEmpty || multiSelect) {
                popupMenu.add(addKeyFrameMenuItem);
            }

            if (thisEmpty && previousEmpty && somethingBefore && !somethingAfter && !multiSelect) {
                popupMenu.add(addKeyFrameEmptyBeforeMenuItem);
            }
            if (!emptyDepth || multiSelect) {
                popupMenu.add(addFrameMenuItem);
            }

            if (!thisEmpty || somethingAfter || multiSelect) {
                popupMenu.add(removeFrameMenuItem);
            }

            if (popupMenu.getComponentCount() > 0) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    private Set<Point> getBackOrderedCursor() {
        Set<Point> orderedCursor = new TreeSet<>(new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int dx = o2.x - o1.x; //later frames first
                if (dx != 0) {
                    return dx;
                }
                return o1.y - o2.y;
            }
        });
        orderedCursor.addAll(cursor);
        return orderedCursor;
    }

    private void removeFrame(ActionEvent e) {

        Set<Point> orderedCursor = getBackOrderedCursor();
        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timeline.timelined) {

            @Override
            public void doOperation() {
                super.doOperation();
                Timelined timelined = timeline.timelined;
                ReadOnlyTagList tags = timelined.getTags();

                for (Point p : orderedCursor) {
                    int nf = p.x;
                    int nd = p.y;

                    if (timelined instanceof ButtonTag) {
                        ButtonTag button = (ButtonTag) timelined;
                        BUTTONRECORD rec = button.getButtonRecordAt(nf, nd, false);
                        if (rec != null) {
                            rec.setFrame(nf, false);
                        }
                        for (int i = nf + 1; i < button.getFrameCount(); i++) {
                            rec = button.getButtonRecordAt(i, nd, false);
                            if (rec != null) {
                                rec.setFrame(i - 1, true);
                                rec.setFrame(i, false);
                            }
                        }
                        button.packRecords();
                    } else {

                        int f = timelined.getFrameCount();
                        List<Tag> lastFrameDepthTags = new ArrayList<>();
                        DepthState ds = timeline.getFrame(nf).layers.get(nd);
                        if (ds != null && ds.key) {
                            PlaceObjectTypeTag po = ds.placeObjectTag;
                            ShowFrameTag sf = timeline.getFrame(nf).showFrameTag;
                            int pos = sf == null ? tags.size() : timelined.indexOfTag(sf);
                            for (int i = pos + 1; i < tags.size(); i++) {
                                Tag t = tags.get(i);
                                if (t instanceof RemoveObject2Tag) {
                                    RemoveObject2Tag rt = (RemoveObject2Tag) t;
                                    if (rt.depth == nd) {
                                        timelined.removeTag(po);
                                        timelined.removeTag(rt);
                                        i--;
                                        i--;
                                    }
                                }
                                if (t instanceof ShowFrameTag) {
                                    break;
                                }
                            }
                        }

                        boolean endsWithRemove = false;
                        for (int i = tags.size() - 1; i >= 0; i--) {
                            Tag t = tags.get(i);
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                                if (pt.getDepth() == nd) {
                                    break;
                                }
                            }
                            if (t instanceof RemoveTag) {
                                RemoveTag rt = (RemoveTag) t;
                                if (rt.getDepth() == nd) {
                                    endsWithRemove = true;
                                    break;
                                }
                            }
                        }

                        for (int i = tags.size() - 1; i >= 0; i--) {
                            Tag t = tags.get(i);
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag pt = (PlaceObjectTypeTag) t;
                                if (pt.getDepth() == nd) {
                                    lastFrameDepthTags.add(pt);
                                    timelined.removeTag(i);
                                }
                            }
                            if (t instanceof RemoveTag) {
                                RemoveTag rt = (RemoveTag) t;
                                if (rt.getDepth() == nd) {
                                    lastFrameDepthTags.add(rt);
                                    timelined.removeTag(i);
                                }
                            }
                            if (t instanceof ShowFrameTag) {
                                for (Tag lt : lastFrameDepthTags) {
                                    timelined.addTag(i, lt);
                                }
                                lastFrameDepthTags.clear();
                                f--;
                                if (f == nf) {
                                    break;
                                }
                            }
                        }

                        if (!endsWithRemove) {
                            RemoveTag rt = new RemoveObject2Tag(timelined.getSwf());
                            rt.setTimelined(timelined);
                            rt.setDepth(nd);
                            Tag lt = tags.get(tags.size() - 1);
                            if (lt instanceof ShowFrameTag) {
                                timelined.addTag(tags.size() - 1, rt);
                            } else {
                                timelined.addTag(lt);
                            }
                        }
                    }
                }

                timelined.resetTimeline();

                Point firstCursor = orderedCursor.iterator().next();
                frameSelect(firstCursor.x, firstCursor.y);
                timeline = timelined.getTimeline();

                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public void undoOperation() {
                super.undoOperation();
                timeline = timeline.timelined.getTimeline();

                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate("action.removeFrame");
            }
        }, timeline.timelined.getSwf());
    }

    private void addKeyFrame(ActionEvent e) {
        if (cursor.size() == 1) {
            Point p = cursor.iterator().next();
            DepthState ds = timeline.getDepthState(p.x, p.y);
            if (ds != null && ds.key) {
                return;
            }
        }

        Set<Point> orderedCursor = getBackOrderedCursor();

        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timeline.timelined) {

            @Override
            public void doOperation() {
                super.doOperation();
                Timelined timelined = timeline.timelined;

                //for (int nf = frame; nf <= fendFrame; nf++) {
                // for (int nd = fdepth; nd <= fendDepth; nd++) 
                for (Point p : orderedCursor) {
                    int nf = p.x;
                    int nd = p.y;
                    DepthState ds = timeline.getFrame(nf).layers.get(nd);
                    if (ds.key) {
                        continue;
                    }
                    ds.key = true;

                    if (timelined instanceof ButtonTag) {
                        ButtonTag button = (ButtonTag) timelined;
                        BUTTONRECORD rec = button.getButtonRecordAt(nf, nd, false);
                        if (rec != null) {
                            BUTTONRECORD rec2 = new BUTTONRECORD(rec.getSwf(), rec.getTag());
                            rec2.fromPlaceObject(rec.toPlaceObject());

                            for (int i = nf; i < button.getFrameCount(); i++) {
                                rec2.setFrame(i, rec.hasFrame(i));
                                rec.setFrame(i, false);
                            }
                            button.getRecords().add(rec2);
                        }
                        continue;
                    }
                    PlaceObjectTypeTag place;
                    try {
                        place = (PlaceObjectTypeTag) ds.placeObjectTag.cloneTag();
                    } catch (InterruptedException | IOException ex) {
                        //should not happen
                        return;
                    }
                    place.setTimelined(timelined);
                    place.setPlaceFlagMove(true);
                    ShowFrameTag sf = timeline.getFrame(nf).showFrameTag;
                    int pos;
                    if (sf != null) {
                        pos = timelined.indexOfTag(sf);
                    } else {
                        pos = timelined.getTags().size();
                    }
                    timelined.addTag(pos++, place);
                }

                timelined.resetTimeline();
                timeline = timelined.getTimeline();

                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public void undoOperation() {
                super.undoOperation();
                timeline = timeline.timelined.getTimeline();
                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate("action.addKeyFrame");
            }
        }, timeline.timelined.getSwf());
    }

    private void addKeyFrameEmptyBefore(ActionEvent e) {
        final int fdepth = getFirstDepth();
        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timeline.timelined) {

            @Override
            public void doOperation() {
                super.doOperation();
                Timelined timelined = timeline.timelined;

                DepthState ds = null;

                if (!(timelined instanceof ButtonTag) && fframe >= timelined.getFrameCount()) {
                    int lastFrame = timelined.getFrameCount() - 1;
                    for (int d = 1; d <= timeline.maxDepth; d++) {
                        ds = timeline.getDepthState(lastFrame, d);
                        if (ds != null && ds.getCharacter() != null) {
                            RemoveTag rt = new RemoveObject2Tag(timelined.getSwf());
                            rt.setTimelined(timelined);
                            rt.setDepth(d);
                            timelined.addTag(rt);
                        }
                    }

                    for (int f = timelined.getFrameCount(); f <= fframe; f++) {
                        ShowFrameTag sf = new ShowFrameTag(timelined.getSwf());
                        sf.setTimelined(timelined);
                        timelined.addTag(sf);
                        timelined.setFrameCount(timelined.getFrameCount() + 1);
                    }
                    timelined.resetTimeline();
                }

                int nonEmptyFrame = -1;
                for (int f = fframe - 1; f >= 0; f--) {
                    ds = timeline.getDepthState(f, fdepth);
                    if (ds != null && ds.getCharacter() != null) {
                        nonEmptyFrame = f;
                        break;
                    }
                }
                if (timelined instanceof ButtonTag) {
                    ButtonTag button = (ButtonTag) timelined;
                    BUTTONRECORD rec = button.getButtonRecordAt(nonEmptyFrame, fdepth, false);
                    BUTTONRECORD rec2 = new BUTTONRECORD(rec.getSwf(), rec.getTag());
                    rec2.fromPlaceObject(rec.toPlaceObject());
                    rec2.setFrame(fframe, true);
                    button.getRecords().add(rec2);

                } else {
                    PlaceObjectTypeTag place;
                    try {
                        place = (PlaceObjectTypeTag) ds.placeObjectTag.cloneTag();
                    } catch (InterruptedException | IOException ex) {
                        //should not happen
                        return;
                    }
                    place.setTimelined(timelined);
                    place.setPlaceFlagMove(false);
                    ShowFrameTag sf = timeline.getFrame(fframe).showFrameTag;
                    int pos;
                    if (sf != null) {
                        pos = timelined.indexOfTag(sf);
                    } else {
                        pos = timelined.getTags().size();
                    }
                    timelined.addTag(pos++, place);
                }

                timelined.resetTimeline();
                timeline = timelined.getTimeline();

                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public void undoOperation() {
                super.undoOperation();
                timeline = timeline.timelined.getTimeline();
                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate("action.addKeyFrame"); //Intentionally not "...with blank frames before"
            }
        }, timeline.timelined.getSwf());
    }

    private void addFrame(ActionEvent e) {
        Set<Point> orderedCursor = getBackOrderedCursor();

        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timeline.timelined) {
            @Override
            public void doOperation() {
                super.doOperation();
                DepthState ds;
                Timelined timelined = timeline.timelined;

                for (Point p : orderedCursor) {
                    int nf = p.x;
                    int nd = p.y;

                    if (nf >= timelined.getFrameCount()) {

                        if (timelined instanceof ButtonTag) {
                            continue;
                        }

                        ReadOnlyTagList tags = timelined.getTags();
                        for (int i = tags.size() - 1; i >= 0; i--) {
                            Tag t = tags.get(i);
                            if (t instanceof PlaceObjectTypeTag) {
                                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                                if (po.getDepth() == nd) {
                                    break;
                                }
                            }
                            if (t instanceof RemoveTag) {
                                RemoveTag rt = (RemoveTag) t;
                                if (rt.getDepth() == nd) {
                                    timelined.removeTag(rt);
                                    break;
                                }
                            }
                        }

                        int lastFrame = timelined.getFrameCount() - 1;
                        //Add removeTag to other layers
                        for (int d = 1; d <= timeline.maxDepth; d++) {
                            if (d == nd) {
                                continue;
                            }
                            ds = timeline.getDepthState(lastFrame, d);
                            if (ds != null && ds.getCharacter() != null) {
                                RemoveTag rt = new RemoveObject2Tag(timelined.getSwf());
                                rt.setTimelined(timelined);
                                rt.setDepth(d);
                                timelined.addTag(rt);
                            }
                        }

                        while (nf >= timelined.getFrameCount()) {
                            ShowFrameTag sf = new ShowFrameTag(timelined.getSwf());
                            sf.setTimelined(timelined);
                            timelined.addTag(sf);
                            timelined.setFrameCount(timelined.getFrameCount() + 1);
                        }
                        timelined.resetTimeline();
                        continue;
                    }

                    boolean somethingAfter = false;
                    Set<Integer> emptyFrames = new LinkedHashSet<>();
                    if (timelined instanceof ButtonTag) {
                        emptyFrames = ((ButtonTag) timelined).getEmptyFrames();
                    }
                    for (int f = nf + 1; f < timeline.getFrameCount(); f++) {
                        ds = timeline.getDepthState(f, nd);
                        boolean empty = emptyFrames.contains(f) || ds == null || ds.getCharacter() == null;
                        if (!empty) {
                            somethingAfter = true;
                            break;
                        }
                    }

                    for (int f = nf; f >= 0; f--) {
                        ds = timeline.getDepthState(f, nd);
                        boolean empty = emptyFrames.contains(f) || ds == null || ds.getCharacter() == null;
                        if (!empty || somethingAfter) {
                            int moveFrameCount = somethingAfter ? 1 : nf - f;
                            for (int mf = 0; mf < moveFrameCount; mf++) {

                                if (timelined instanceof ButtonTag) {
                                    ButtonTag button = (ButtonTag) timelined;
                                    if (!somethingAfter) {
                                        BUTTONRECORD rec = button.getButtonRecordAt(f, nd, true);
                                        rec.setFrame(nf - mf, true);
                                    } else {
                                        for (int fx = button.getFrameCount() - 1; fx >= nf; fx--) {
                                            BUTTONRECORD rec = button.getButtonRecordAt(fx, nd, false);
                                            if (rec != null) {
                                                rec.setFrame(fx, false);
                                                rec.setFrame(fx + 1, true);
                                            }
                                        }
                                    }
                                    button.packRecords();
                                    continue;
                                }

                                int pos = timelined.indexOfTag(timeline.getFrame(f).showFrameTag);
                                ReadOnlyTagList tags = timelined.getTags();
                                List<Tag> lastFrameDepthTags = new ArrayList<>();
                                int f2 = f + 1;
                                boolean endsWithRemove = false;
                                boolean removeOnly = true;
                                for (int i = pos + 1; i < tags.size(); i++) {
                                    Tag t = tags.get(i);
                                    if (t instanceof PlaceObjectTypeTag) {
                                        PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                                        if (po.getDepth() == nd) {
                                            lastFrameDepthTags.add(po);
                                            timelined.removeTag(po);
                                            endsWithRemove = false;
                                            i--;
                                        }
                                    }
                                    if (t instanceof RemoveTag) {
                                        RemoveTag ro = (RemoveTag) t;
                                        if (ro.getDepth() == nd) {
                                            lastFrameDepthTags.add(ro);
                                            timelined.removeTag(ro);
                                            endsWithRemove = true;
                                            i--;
                                        }
                                    }
                                    if ((t instanceof ShowFrameTag) || (i == tags.size() - 1)) {
                                        if (!(t instanceof ShowFrameTag) && !lastFrameDepthTags.isEmpty()) {
                                            ShowFrameTag sf = new ShowFrameTag(timelined.getSwf());
                                            sf.setTimelined(timelined);
                                            timelined.addTag(sf);
                                            i++;
                                        }

                                        removeOnly = true;
                                        for (Tag lt : lastFrameDepthTags) {
                                            if (!(lt instanceof RemoveTag)) {
                                                removeOnly = false;
                                                break;
                                            }
                                        }

                                        boolean onLastFrame = f2 == timelined.getFrameCount() - 1;

                                        if (!(removeOnly && onLastFrame)) {
                                            for (Tag lt : lastFrameDepthTags) {
                                                i++;
                                                timelined.addTag(i, lt);
                                            }
                                        }

                                        lastFrameDepthTags.clear();
                                        f2++;
                                    }
                                }

                                if ((!removeOnly && !lastFrameDepthTags.isEmpty()) || !endsWithRemove) {
                                    //Add removeTag to other layers
                                    for (int d = 1; d <= timeline.maxDepth; d++) {
                                        if (d == nd) {
                                            continue;
                                        }
                                        ds = timeline.getDepthState(f2 - 1, d);
                                        if (ds != null && ds.getCharacter() != null) {
                                            RemoveTag rt = new RemoveObject2Tag(timelined.getSwf());
                                            rt.setTimelined(timelined);
                                            rt.setDepth(d);
                                            timelined.addTag(rt);
                                        }
                                    }

                                    ShowFrameTag sf = new ShowFrameTag(timelined.getSwf());
                                    sf.setTimelined(timelined);
                                    timelined.addTag(sf);
                                    timelined.setFrameCount(timelined.getFrameCount() + 1);
                                }
                            }
                            break;
                        }
                    }
                }

                timelined.resetTimeline();
                timelined.setFrameCount(timelined.getTimeline().getFrameCount());
                timeline = timelined.getTimeline();
                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public void undoOperation() {
                super.undoOperation();
                timeline = timeline.timelined.getTimeline();
                refresh();
                fireChanged();
                repaint();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate("action.addFrame");
            }
        }, timeline.timelined.getSwf());
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
        Point firstCursorItem = cursor.isEmpty() ? new Point(1, 1) : cursor.iterator().next();
        switch (e.getKeyCode()) {
            case 37: //left
                if (firstCursorItem.x > 0) {
                    frameSelect(firstCursorItem.x - 1, firstCursorItem.y);
                }
                break;
            case 39: //right
                if (firstCursorItem.x < timeline.getFrameCount() - 1) {
                    frameSelect(firstCursorItem.x + 1, firstCursorItem.y);
                }
                break;
            case 38: //up
                if (firstCursorItem.y > 0) {
                    frameSelect(firstCursorItem.x, firstCursorItem.y - 1);
                }
                break;
            case 40: //down
                if (firstCursorItem.y < timeline.getMaxDepth()) {
                    frameSelect(firstCursorItem.x, firstCursorItem.y + 1);
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public Rectangle getDepthBounds(int depth) {
        return getFrameBounds(getFirstFrame(), depth);
    }

    public Rectangle getFrameBounds(int frame, int depth) {
        Rectangle rect = new Rectangle();
        rect.width = getFrameWidth();
        rect.height = FRAME_HEIGHT;
        rect.x = frame * getFrameWidth();
        rect.y = depth * FRAME_HEIGHT;
        return rect;
    }

    public void refresh() {
        int frameCount = timeline == null ? 0 : timeline.getFrameCount();
        int maxDepth = timeline == null ? 0 : timeline.getMaxDepth();
        Dimension dim = new Dimension(getFrameWidth() * frameCount + 1, FRAME_HEIGHT * (maxDepth + 1 /*actions*/ + 1 /*one additional*/));
        setSize(dim);
        setPreferredSize(dim);
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        refresh();
    }
}
