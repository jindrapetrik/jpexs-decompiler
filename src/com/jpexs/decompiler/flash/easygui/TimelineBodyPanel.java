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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private final Set<Point> cursor = new LinkedHashSet<>();
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

    static void drawKeyMarker(Graphics g, int centerX, int rowY, boolean empty) {
        int x = centerX - MARKER_SIZE / 2;
        int y = rowY + FRAME_HEIGHT * 3 / 4 - MARKER_SIZE / 2;
        if (empty) {
            g.drawOval(x, y, MARKER_SIZE, MARKER_SIZE);
        } else {
            g.fillOval(x, y, MARKER_SIZE, MARKER_SIZE);
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
        drawKeyMarker(g, frame * frameWidth + frameWidth / 2, depth * frameHeight, blockType == BlockType.EMPTY);

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
        frameSelect(getFirstFrame(), Arrays.asList(depth), true);
    }

    public void depthsSelect(List<Integer> depths) {
        frameSelect(getFirstFrame(), depths, true);
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

    private Set<Integer> getLockedDepths() {
        return swfPanel.getLockedDepths(timeline == null ? null : timeline.timelined);
    }

    void removeLockedSelection() {
        int frame = getFirstFrame();
        Set<Integer> locked = getLockedDepths();
        if (cursor.removeIf(cell -> locked.contains(cell.y))) {
            if (cursor.isEmpty()) {
                cursor.add(new Point(frame, 0));
            }
            repaint();
            fireFrameSelected(getFirstFrame(), getDepthsAtFirstFrame());
        }
    }

    private void rectSelect(int frame, int depth, int endFrame, int endDepth) {
        int x1 = Math.min(frame, endFrame);
        int x2 = Math.max(frame, endFrame);
        int y1 = Math.min(depth, endDepth);
        int y2 = Math.max(depth, endDepth);
        Set<Integer> locked = getLockedDepths();
        Set<Point> newCursor = new LinkedHashSet<>();
        for (int y = y1; y <= y2; y++) {
            if (locked.contains(y)) {
                continue;
            }
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
        for (int d = y1; d <= y2; d++) {
            if (!locked.contains(d)) {
                depths.add(d);
            }
        }
        fireFrameSelected(frame, depths);
    }

    public void frameSelect(int frame, int depth, boolean notify) {
        frameSelect(frame, Arrays.asList(depth), notify);
    }

    public void frameSelect(int frame, List<Integer> depths, boolean notify) {
        /*if (cursor != null && cursor.width == 1 && cursor.height == 1 && (cursor.contains(frame, depth) || (depth == -1 && cursor.contains(frame, cursor.y)))) {
            return;
        }
        if (depth == -1 && cursor != null) {
            depth = cursor.y;
        }*/
        //rectSelect(frame, depth, frame, depth);

        Set<Point> newCursor = new LinkedHashSet<>();
        Set<Integer> locked = getLockedDepths();
        List<Integer> selectableDepths = new ArrayList<>(depths);
        selectableDepths.removeAll(locked);

        for (int d : selectableDepths) {
            newCursor.add(new Point(frame, d));
        }
        if (selectableDepths.isEmpty()) {
            newCursor.add(new Point(frame, 0));
        }

        /*if (cursor.equals(newCursor)) {
            return;
        }*/
        
        cursor.clear();
        cursor.addAll(newCursor);

        repaint();

        /*
        this.frame = frame;
        this.depth = depths.isEmpty() ? 0 : depths.get(0);
        this.endFrame = frame;
         */
        if (notify) {
            fireFrameSelected(frame, selectableDepths);
        }
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
        if (getLockedDepths().contains(p.y) && (!shiftDown || SwingUtilities.isRightMouseButton(e))) {
            return;
        }
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
                frameSelect(p.x, Arrays.asList(p.y), true);
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

            if (thisEmpty && previousEmpty && somethingBefore && !somethingAfter && !multiSelect
                    && (!(timeline.timelined instanceof ButtonTag) || frame <= ButtonTag.FRAME_HITTEST)) {
                popupMenu.add(addKeyFrameEmptyBeforeMenuItem);
            }
            if (!emptyDepth || multiSelect) {
                popupMenu.add(addFrameMenuItem);
            }

            if (!thisEmpty || somethingAfter || multiSelect) {
                popupMenu.add(removeFrameMenuItem);
            }

            Set<Point> cells = new LinkedHashSet<>();
            if (!(timeline.timelined instanceof ButtonTag)) {
                for (Point cell : cursor) {
                    if (cell.y > 0 && timeline.getFrame(cell.x) != null) {
                        cells.add(cell);
                    }
                }
            }
            if (!cells.isEmpty()) {
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.addSeparator();
                }
                JMenuItem replaceContent = new JMenuItem(EasyStrings.translate("action.replaceSelectedContent") + "...");
                replaceContent.addActionListener(event -> replaceSelectedContent(cells));
                popupMenu.add(replaceContent);
            }

            if (popupMenu.getComponentCount() > 0) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    private void replaceSelectedContent(Set<Point> cells) {
        Set<Integer> locked = getLockedDepths();
        cells.removeIf(cell -> locked.contains(cell.y));
        if (cells.isEmpty()) {
            return;
        }
        Timelined timelined = timeline.timelined;
        Set<Integer> originalIds = new LinkedHashSet<>();
        for (Point cell : cells) {
            DepthState state = timeline.getDepthState(cell.x, cell.y);
            CharacterTag original = state == null ? null : state.getCharacter();
            originalIds.add(original == null ? -1 : original.getCharacterId());
        }
        CharacterTag replacement = ReplaceTimelineContentDialog.choose(this, timelined, cells,
                originalIds, swfPanel.getSelectedLibraryCharacter());
        if (replacement == null) {
            return;
        }

        Set<Point> selection = new LinkedHashSet<>(cursor);
        ReplaceTimelineContentOperation operation = new ReplaceTimelineContentOperation(swfPanel, timeline,
                cells, replacement, () -> {
                    timeline = timelined.getTimeline();
                    restoreSelection(selection);
                    refresh();
                    fireChanged();
                    fireFrameSelected(getFirstFrame(), getDepthsAtFirstFrame());
                    repaint();
                });
        undoManager.doOperation(operation, timelined.getSwf());
    }

    private List<Integer> getDepthsAtFirstFrame() {
        int frame = getFirstFrame();
        List<Integer> depths = new ArrayList<>();
        for (Point cell : cursor) {
            if (cell.x == frame && cell.y > 0) {
                depths.add(cell.y);
            }
        }
        return depths;
    }

    private void restoreSelection(Set<Point> selection) {
        Set<Integer> locked = getLockedDepths();
        cursor.clear();
        for (Point cell : selection) {
            if (!locked.contains(cell.y)) {
                cursor.add(cell);
            }
        }
        if (cursor.isEmpty() && !selection.isEmpty()) {
            cursor.add(new Point(selection.iterator().next().x, 0));
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
        Point firstCell = cursor.stream()
                .min(Comparator.comparingInt((Point cell) -> cell.x).thenComparingInt(cell -> cell.y))
                .orElse(null);
        if (firstCell == null) {
            return;
        }
        Set<Point> remainingSelection = Collections.singleton(firstCell);
        if (timeline.timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timeline.timelined;
            List<BUTTONRECORD> records = copyButtonRecords(button.getRecords());
            boolean changed = false;
            for (Point cell : getBackOrderedCursor()) {
                int frame = cell.x;
                int depth = cell.y;
                if (depth <= 0 || frame < ButtonTag.FRAME_UP || frame > ButtonTag.FRAME_HITTEST) {
                    continue;
                }
                BUTTONRECORD record = findButtonRecord(records, frame, depth);
                if (record != null) {
                    record.setFrame(frame, false);
                    changed = true;
                }
                for (int next = frame + 1; next <= ButtonTag.FRAME_HITTEST; next++) {
                    record = findButtonRecord(records, next, depth);
                    if (record != null) {
                        record.setFrame(next - 1, true);
                        record.setFrame(next, false);
                        changed = true;
                    }
                }
            }
            if (changed) {
                records.removeIf(BUTTONRECORD::isEmpty);
                applyButtonEdit(button, records, "action.removeFrame", remainingSelection);
            }
            return;
        }
        TimelineFrameEdits edits = new TimelineFrameEdits(timeline);
        edits.removeFrames(cursor);
        applyFrameEdits(edits, "action.removeFrame", remainingSelection);
    }

    private void addKeyFrame(ActionEvent e) {
        Timelined timelined = timeline.timelined;
        if (timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timelined;
            List<BUTTONRECORD> records = copyButtonRecords(button.getRecords());
            boolean changed = false;
            for (Point cell : getBackOrderedCursor()) {
                DepthState state = timeline.getDepthState(cell.x, cell.y);
                BUTTONRECORD record = findButtonRecord(records, cell.x, cell.y);
                if (state == null || state.key || record == null) {
                    continue;
                }
                BUTTONRECORD split = new BUTTONRECORD(record);
                for (int frame = ButtonTag.FRAME_UP; frame <= ButtonTag.FRAME_HITTEST; frame++) {
                    if (frame < cell.x) {
                        split.setFrame(frame, false);
                    } else {
                        record.setFrame(frame, false);
                    }
                }
                records.add(split);
                changed = true;
            }
            if (changed) {
                records.removeIf(BUTTONRECORD::isEmpty);
                applyButtonEdit(button, records, "action.addKeyFrame", cursor);
            }
            return;
        }
        TimelineFrameEdits edits = new TimelineFrameEdits(timeline);
        for (Point cell : cursor) {
            DepthState state = timeline.getDepthState(cell.x, cell.y);
            if (cell.y <= 0 || !TimelineFrameEdits.isPlaced(state) || state.key) {
                continue;
            }
            boolean move = timelined.getSwf().version >= 3;
            if (!move) {
                edits.addToFrame(cell.x, TimelineFrameEdits.makeRemove(timelined, state, cell.y));
            }
            edits.addToFrame(cell.x, TimelineFrameEdits.makePlacement(timelined, state, cell.y, move));
        }
        applyFrameEdits(edits, "action.addKeyFrame", cursor);
    }

    private void addKeyFrameEmptyBefore(ActionEvent e) {
        int frame = getFirstFrame();
        int depth = getFirstDepth();
        Timelined timelined = timeline.timelined;
        if (depth <= 0 || frame <= 0) {
            return;
        }
        if (timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timelined;
            if (frame > ButtonTag.FRAME_HITTEST || button.getButtonRecordAt(frame, depth, false) != null) {
                return;
            }
            List<BUTTONRECORD> records = copyButtonRecords(button.getRecords());
            BUTTONRECORD previous = findPreviousButtonRecord(records, frame, depth);
            if (previous != null) {
                BUTTONRECORD key = new BUTTONRECORD(previous);
                for (int i = ButtonTag.FRAME_UP; i <= ButtonTag.FRAME_HITTEST; i++) {
                    key.setFrame(i, i == frame);
                }
                records.add(key);
                applyButtonEdit(button, records, "action.addKeyFrame", cursor);
            }
            return;
        }
        if (TimelineFrameEdits.isPlaced(timeline.getDepthState(frame, depth))) {
            return;
        }
        int previous = Math.min(frame, timeline.getFrameCount()) - 1;
        while (previous >= 0) {
            DepthState state = timeline.getDepthState(previous, depth);
            if (TimelineFrameEdits.isPlaced(state)) {
                TimelineFrameEdits edits = new TimelineFrameEdits(timeline);
                edits.extendTo(frame);
                edits.addToFrame(frame, TimelineFrameEdits.makePlacement(timelined, state, depth, false));
                applyFrameEdits(edits, "action.addKeyFrame", cursor);
                return;
            }
            previous--;
        }
    }

    private void addFrame(ActionEvent e) {
        if (timeline.timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timeline.timelined;
            List<BUTTONRECORD> records = copyButtonRecords(button.getRecords());
            boolean changed = false;
            for (Point cell : getBackOrderedCursor()) {
                changed |= insertButtonFrame(records, cell.x, cell.y);
            }
            if (changed) {
                applyButtonEdit(button, records, "action.addFrame", cursor);
            }
            return;
        }
        TimelineFrameEdits edits = new TimelineFrameEdits(timeline);
        edits.insertFrames(cursor);
        applyFrameEdits(edits, "action.addFrame", cursor);
    }

    private static boolean insertButtonFrame(List<BUTTONRECORD> records, int frame, int depth) {
        if (depth <= 0 || frame < ButtonTag.FRAME_UP || frame > ButtonTag.FRAME_HITTEST) {
            return false;
        }
        // Shifting right requires a free last state so no record is discarded.
        if (findButtonRecord(records, ButtonTag.FRAME_HITTEST, depth) != null) {
            return false;
        }
        BUTTONRECORD current = findButtonRecord(records, frame, depth);
        int insertion = frame + 1;
        boolean shifted = false;
        for (int next = ButtonTag.FRAME_HITTEST - 1; next >= insertion; next--) {
            BUTTONRECORD record = findButtonRecord(records, next, depth);
            if (record != null) {
                record.setFrame(next + 1, true);
                record.setFrame(next, false);
                shifted = true;
            }
        }
        if (current != null) {
            current.setFrame(insertion, true);
            return true;
        }
        if (!shifted) {
            for (int previous = frame - 1; previous >= ButtonTag.FRAME_UP; previous--) {
                BUTTONRECORD record = findButtonRecord(records, previous, depth);
                if (record != null) {
                    for (int added = previous + 1; added <= frame; added++) {
                        record.setFrame(added, true);
                    }
                    return true;
                }
            }
        }
        return shifted;
    }

    private static List<BUTTONRECORD> copyButtonRecords(List<BUTTONRECORD> records) {
        List<BUTTONRECORD> copy = new ArrayList<>(records.size());
        for (BUTTONRECORD record : records) {
            copy.add(new BUTTONRECORD(record));
        }
        return copy;
    }

    private static BUTTONRECORD findButtonRecord(List<BUTTONRECORD> records, int frame, int depth) {
        for (BUTTONRECORD record : records) {
            if (record.placeDepth == depth && record.hasFrame(frame)) {
                return record;
            }
        }
        return null;
    }

    private static BUTTONRECORD findPreviousButtonRecord(List<BUTTONRECORD> records, int frame, int depth) {
        for (int previous = frame - 1; previous >= ButtonTag.FRAME_UP; previous--) {
            BUTTONRECORD record = findButtonRecord(records, previous, depth);
            if (record != null) {
                return record;
            }
        }
        return null;
    }

    private void applyFrameEdits(TimelineFrameEdits edits, String description, Set<Point> selectionAfterEdit) {
        if (!edits.isEmpty()) {
            applyTimelineEdit(edits::apply, description, selectionAfterEdit);
        }
    }

    private void applyButtonEdit(ButtonTag button, List<BUTTONRECORD> records, String description, Set<Point> selectionAfterEdit) {
        applyTimelineEdit(() -> {
            button.getRecords().clear();
            button.getRecords().addAll(copyButtonRecords(records));
            button.resetTimeline();
        }, description, selectionAfterEdit);
    }

    private void applyTimelineEdit(Runnable edit, String description, Set<Point> selectionAfterEdit) {
        Timelined timelined = timeline.timelined;
        Set<Point> originalSelection = new LinkedHashSet<>(cursor);
        Set<Point> editedSelection = new LinkedHashSet<>(selectionAfterEdit);
        int originalFrameCount = timelined.getFrameCount();
        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timelined) {
            @Override
            public void doOperation() {
                super.doOperation();
                edit.run();
                refreshAfterEdit(editedSelection);
            }

            @Override
            public void undoOperation() {
                super.undoOperation();
                timelined.setFrameCount(originalFrameCount);
                refreshAfterEdit(originalSelection);
            }

            private void refreshAfterEdit(Set<Point> selection) {
                timeline = timelined.getTimeline();
                timelined.getSwf().clearImageCache();
                restoreSelection(selection);
                refresh();
                fireChanged();
                fireFrameSelected(getFirstFrame(), getDepthsAtFirstFrame());
                repaint();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate(description);
            }
        }, timelined.getSwf());
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
        if (timeline == null) {
            return;
        }
        Point firstCursorItem = cursor.isEmpty() ? new Point(1, 1) : cursor.iterator().next();
        switch (e.getKeyCode()) {
            case 37: //left
                if (firstCursorItem.x > 0) {
                    frameSelect(firstCursorItem.x - 1, firstCursorItem.y, true);
                }
                break;
            case 39: //right
                if (firstCursorItem.x < timeline.getFrameCount() - 1) {
                    frameSelect(firstCursorItem.x + 1, firstCursorItem.y, true);
                }
                break;
            case 38: //up
                selectNextUnlockedDepth(firstCursorItem, -1);
                break;
            case 40: //down
                selectNextUnlockedDepth(firstCursorItem, 1);
                break;
        }
    }

    private void selectNextUnlockedDepth(Point cell, int step) {
        Set<Integer> locked = getLockedDepths();
        int depth = cell.y + step;
        while (locked.contains(depth)) {
            depth += step;
        }
        if (depth >= 0 && depth <= timeline.getMaxDepth()) {
            frameSelect(cell.x, depth, true);
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
