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
import com.jpexs.decompiler.flash.easygui.TimelineFrameEdits.KeyEdit;
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
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    private int dropRow = -1;

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

        Rectangle clip = g.getClipBounds();
        int frameHeight = FRAME_HEIGHT;
        int start_f = clip.x / frameWidth;
        int firstRow = clip.y / frameHeight;
        int end_f = (clip.x + clip.width) / frameWidth;
        int lastRow = (clip.y + clip.height) / frameHeight;

        int maxDepth = getDepthCount();
        int max_f = timeline.getFrameCount() - 1;

        // draw background
        for (int f = start_f; f <= end_f; f++) {
            g.setColor((f + 1) % 5 == 0 ? getEmptyFrameSecondColor() : getEmptyFrameColor());
            g.fillRect(f * frameWidth, firstRow * frameHeight, frameWidth, (lastRow - firstRow + 1) * frameHeight);
            g.setColor(EMPTY_BORDER_COLOR);
            for (int row = firstRow; row <= lastRow; row++) {
                g.drawRect(f * frameWidth, row * frameHeight, frameWidth, frameHeight);
            }
        }

        // draw selected cell
        for (Point c : cursor) {
            g.setColor(getSelectedColor());
            g.fillRect(c.x * frameWidth + 1, flipLayerOrder(c.y, maxDepth) * frameHeight + 1, frameWidth - 1, frameHeight - 1);
        }

        int awidth = g.getFontMetrics().stringWidth("a");
        boolean firstAction = true;
        for (int f = start_f; f <= end_f || (firstAction && f <= max_f); f++) {
            Frame fr = timeline.getFrame(f);
            if (fr != null && !fr.actions.isEmpty()) {
                if (firstAction) {
                    drawBlock(g, getEmptyFrameColor(), 0, 0, f, BlockType.EMPTY, frameWidth, true);
                }

                int f2 = f + 1;
                while (f2 <= max_f && timeline.getFrame(f2) != null && timeline.getFrame(f2).actions.isEmpty()) {
                    f2++;
                }
                drawBlock(g, getEmptyFrameColor(), 0, f, f2 - f, BlockType.EMPTY, frameWidth, true);
                g.setColor(A_COLOR);
                g.setFont(getFont().deriveFont(FONT_SIZE));
                g.drawString("a", f * frameWidth + frameWidth / 2 - awidth / 2, frameHeight / 2);
                firstAction = false;
            }
        }

        TimelineBlankFrames blanks = getBlankFrames();
        for (int row = Math.max(1, firstRow); row <= Math.min(lastRow, maxDepth); row++) {
            int depth = flipLayerOrder(row, maxDepth);
            int end = getLayerEnd(depth, blanks);
            if (start_f > end) {
                continue;
            }

            int frame = start_f;
            while (frame > 0 && !startsFrameBlock(frame, depth, blanks)) {
                frame--;
            }
            while (frame <= end && frame <= end_f) {
                DepthState state = getPlacedState(frame, depth);
                int next = frame + 1;
                while (next <= end && !startsFrameBlock(next, depth, blanks)) {
                    next++;
                }
                boolean shapeTween = state != null && state.getCharacter() instanceof MorphShapeTag;
                boolean motionTween = state != null && state.motionTween;
                Color color = state == null ? getEmptyFrameColor() : shapeTween ? SHAPE_TWEEN_COLOR : getFrameColor();
                BlockType block = state == null ? BlockType.EMPTY
                        : motionTween ? BlockType.MOTION_TWEEN : shapeTween ? BlockType.SHAPE_TWEEN : BlockType.NORMAL;
                drawBlock(g, color, depth, frame, next - frame, block, frameWidth, isKey(frame, depth, blanks));
                frame = next;
            }
        }

        Point cursorFirst = cursor.isEmpty() ? null : cursor.iterator().next();
        if (cursorFirst != null && cursorFirst.x >= start_f && cursorFirst.x <= end_f) {
            g.setColor(SELECTED_BORDER_COLOR);
            g.drawLine(cursorFirst.x * frameWidth + frameWidth / 2, 0, cursorFirst.x * frameWidth + frameWidth / 2, getHeight());
        }
        if (dropRow >= 0) {
            TimelineLayerDrag.paintDropLine(g, getWidth(), dropRow * frameHeight);
        }
    }

    void setDropRow(int row) {
        if (dropRow != row) {
            dropRow = row;
            repaint();
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

    private void drawBlock(Graphics2D g, Color backColor, int depth, int frame, int num_frames, BlockType blockType, int frameWidth, boolean key) {
        int frameHeight = FRAME_HEIGHT;
        int rowY = flipLayerOrder(depth, getDepthCount()) * frameHeight;

        for (int n = frame; n < frame + num_frames; n++) {
            if (cursor != null && cursor.contains(new Point(n, depth))) {
                g.setColor(getSelectedColor());
            } else {
                g.setColor(backColor);
            }
            g.fillRect(n * frameWidth, rowY, frameWidth, frameHeight);
        }

        g.setColor(BORDER_COLOR);
        g.drawRect(frame * frameWidth, rowY, num_frames * frameWidth, frameHeight);

        //boolean selected = cursor != null && cursor.contains(cursor.x, depth);
        /*if (cursor != null && cursor.contains(frame, depth) && !cursor.contains(frame + num_frames, depth)) {//frame <= cursor.x && (frame + num_frames) > cursor.x && depth == cursor.y) {
            selected = true;
        }*/
        for (int n = frame + 1; n < frame + num_frames; n++) {
            g.setColor(getSelectedColor());
            if (cursor.contains(new Point(n, depth))) {
                g.fillRect(n * frameWidth + 1, rowY + 1, frameWidth, frameHeight);
            }
        }

        /*if (selected) {
            g.setColor(getSelectedColor());
            g.fillRect(cursor.x * frameWidth + 1, rowY + 1, (cursor.width * frameWidth) - 1, frameHeight - 1);
        }*/
        boolean isTween = blockType == BlockType.MOTION_TWEEN || blockType == BlockType.SHAPE_TWEEN;

        g.setColor(KEY_COLOR);
        if (isTween) {
            g.drawLine(frame * frameWidth, rowY + frameHeight * 3 / 4,
                    frame * frameWidth + num_frames * frameWidth - frameWidth / 2, rowY + frameHeight * 3 / 4
            );
        }

        if (cursor != null && cursor.contains(new Point(frame, depth))) {
            g.setBackground(getSelectedColorText());
        } else {
            g.setColor(KEY_COLOR);
        }
        if (key) {
            drawKeyMarker(g, frame * frameWidth + frameWidth / 2, rowY, blockType == BlockType.EMPTY);
        }

        if (num_frames > 1) {
            int endFrame = frame + num_frames - 1;
            if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                g.setBackground(getSelectedColorText());
            } else {
                g.setColor(KEY_COLOR);
            }
            if (isTween) {
                g.fillOval(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, rowY + frameHeight * 3 / 4 - MARKER_SIZE / 2, MARKER_SIZE, MARKER_SIZE);
            } else {

                if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(STOP_COLOR);
                }
                g.fillRect(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, rowY + frameHeight / 2 - 2, MARKER_SIZE, frameHeight / 2);
                if (cursor != null && cursor.contains(new Point(endFrame, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(STOP_BORDER_COLOR);
                }
                g.drawRect(endFrame * frameWidth + frameWidth / 2 - MARKER_SIZE / 2, rowY + frameHeight / 2 - 2, MARKER_SIZE, frameHeight / 2);
            }

            for (int n = frame + 1; n < frame + num_frames; n++) {
                if (cursor != null && cursor.contains(new Point(n, depth))) {
                    g.setBackground(getSelectedColorText());
                } else {
                    g.setColor(BORDER_LINES_COLOR);
                }
                g.drawLine(n * frameWidth, rowY + 1, n * frameWidth, rowY + BORDER_LINES_LENGTH);
                g.drawLine(n * frameWidth, rowY + frameHeight - 1, n * frameWidth, rowY + frameHeight - BORDER_LINES_LENGTH);
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

    void selectLayerFrames(int depth) {
        if (timeline == null || getLockedDepths().contains(depth)) {
            return;
        }
        int frameCount = getDisplayedFrameCount();
        int activeFrame = Math.max(0, Math.min(getFirstFrame(), frameCount - 1));
        cursor.clear();
        cursor.add(new Point(activeFrame, depth));
        for (int frame = 0; frame < frameCount; frame++) {
            if (frame != activeFrame) {
                cursor.add(new Point(frame, depth));
            }
        }
        repaint();
        fireFrameSelected(activeFrame, Collections.singletonList(depth));
        requestFocusInWindow();
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

    Set<Point> copySelection() {
        return new LinkedHashSet<>(cursor);
    }

    FrameSpan getSelectedFrameSpan(int frame, int depth) {
        if (depth <= 0 || depth > getDepthCount()) {
            return new FrameSpan(frame, frame);
        }
        boolean occupied = isFrameOccupied(frame, depth);
        if (occupied) {
            int first = frame;
            int lastSelected = frame;
            while (first > 0 && cursor.contains(new Point(first - 1, depth))) {
                first--;
            }
            int frameLimit = getFrameLimit();
            while (lastSelected < frameLimit && cursor.contains(new Point(lastSelected + 1, depth))) {
                lastSelected++;
            }
            if (first != lastSelected) {
                return new FrameSpan(first, lastSelected);
            }
        }
        return getFrameBlock(frame, depth, occupied);
    }

    private FrameSpan getFrameBlock(int frame, int depth, boolean occupied) {
        TimelineBlankFrames blanks = getBlankFrames();
        int first = frame;
        if (occupied || frame <= getLayerEnd(depth, blanks)) {
            while (first > 0 && !startsFrameBlock(first, depth, blanks)) {
                first--;
            }
        }

        int realEnd = timeline.getFrameCount() - 1;
        int blankEnd = blanks.end(depth);
        int frameLimit = getFrameLimit();
        int end = Math.min(frameLimit, Math.max(realEnd, blankEnd));
        for (int next = frame + 1; next <= end; next++) {
            if (startsFrameBlock(next, depth, blanks)) {
                return new FrameSpan(first, next - 1);
            }
        }
        int last = !occupied && blankEnd >= frame ? blankEnd : realEnd;
        return new FrameSpan(first, Math.max(frame, Math.min(frameLimit, last)));
    }

    private boolean isFrameOccupied(int frame, int depth) {
        return timeline.timelined instanceof ButtonTag ? ((ButtonTag) timeline.timelined).getButtonRecordAt(frame, depth, false) != null
                : getPlacedState(frame, depth) != null;
    }

    private int getFrameLimit() {
        return timeline.timelined instanceof ButtonTag ? ButtonTag.FRAME_HITTEST : TimelineFrameEdits.MAX_FRAME;
    }

    static final class FrameSpan {

        final int first;
        final int last;

        FrameSpan(int first, int last) {
            this.first = first;
            this.last = last;
        }
    }

    private Set<Integer> getLockedDepths() {
        return swfPanel == null ? Collections.emptySet() : swfPanel.getLockedDepths(timeline == null ? null : timeline.timelined);
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
        int maxDepth = getDepthCount();
        int row = flipLayerOrder(depth, maxDepth);
        int endRow = flipLayerOrder(endDepth, maxDepth);
        Set<Integer> locked = getLockedDepths();
        Set<Point> newCursor = new LinkedHashSet<>();
        List<Integer> depths = new ArrayList<>();
        for (int y = Math.min(row, endRow); y <= Math.max(row, endRow); y++) {
            int selectedDepth = flipLayerOrder(y, maxDepth);
            if (locked.contains(selectedDepth)) {
                continue;
            }
            depths.add(selectedDepth);
            for (int x = x1; x <= x2; x++) {
                newCursor.add(new Point(x, selectedDepth));
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
        p.y = flipLayerOrder(p.y / FRAME_HEIGHT, getDepthCount());
        if (getLockedDepths().contains(p.y) && (!shiftDown || SwingUtilities.isRightMouseButton(e))) {
            return;
        }
        /*if (p.x >= timeline.getFrameCount()) {
            p.x = timeline.getFrameCount() - 1;
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

            JPopupMenu popupMenu = createFrameMenu();
            if (popupMenu.getComponentCount() > 0) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    JPopupMenu createFrameMenu() {
        JPopupMenu menu = new JPopupMenu();
        Set<Point> cells = getEditableCells();
        if (cells.isEmpty()) {
            return menu;
        }
        TimelineBlankFrames blanks = getBlankFrames();
        boolean canRemove = cells.stream().anyMatch(cell -> cell.x <= getLayerEnd(cell.y, blanks));
        Set<Point> keys = new LinkedHashSet<>(cells);
        keys.removeIf(cell -> cell.x > getLayerEnd(cell.y, blanks) || !isKey(cell.x, cell.y, blanks));
        boolean canInsert = !(timeline.timelined instanceof ButtonTag)
                || cells.stream().anyMatch(cell -> ((ButtonTag) timeline.timelined).getButtonRecordAt(ButtonTag.FRAME_HITTEST, cell.y, false) == null);
        addFrameMenuItem(menu, "action.insertFrame", canInsert, () -> editFrames(cells, true));
        addFrameMenuItem(menu, "action.removeFrames", canRemove, () -> editFrames(cells, false));
        menu.addSeparator();
        addFrameMenuItem(menu, "action.insertKeyframe", true, () -> editKeyframes(cells, KeyEdit.INSERT, "action.insertKeyframe"));
        addFrameMenuItem(menu, "action.insertBlankKeyframe", true, () -> editKeyframes(cells, KeyEdit.INSERT_BLANK, "action.insertBlankKeyframe"));
        addFrameMenuItem(menu, "action.clearKeyframe", !keys.isEmpty(), () -> editKeyframes(keys, KeyEdit.CLEAR, "action.clearKeyframe"));
        addFrameMenuItem(menu, "action.convertToKeyframes", true, () -> editKeyframes(cells, KeyEdit.CONVERT, "action.convertToKeyframes"));
        addFrameMenuItem(menu, "action.convertToBlankKeyframes", true, () -> editKeyframes(cells, KeyEdit.CONVERT_BLANK, "action.convertToBlankKeyframes"));
        addFrameMenuItem(menu, "action.clearFrames", canClearFrames(cells), () -> clearFrames(cells));

        if (!(timeline.timelined instanceof ButtonTag)) {
            Set<Point> realCells = new LinkedHashSet<>(cells);
            realCells.removeIf(cell -> timeline.getFrame(cell.x) == null);
            if (!realCells.isEmpty()) {
                menu.addSeparator();
                JMenuItem replace = new JMenuItem(EasyStrings.translate("action.replaceSelectedContent") + "...");
                replace.addActionListener(event -> replaceSelectedContent(realCells));
                menu.add(replace);
            }
        }
        return menu;
    }

    private boolean canClearFrames(Set<Point> cells) {
        if (!(timeline.timelined instanceof ButtonTag)) {
            return cells.stream().anyMatch(cell -> getPlacedState(cell.x, cell.y) != null);
        }
        boolean selected = false;
        boolean remaining = false;
        for (BUTTONRECORD record : ((ButtonTag) timeline.timelined).getRecords()) {
            for (int frame = ButtonTag.FRAME_UP; frame <= ButtonTag.FRAME_HITTEST; frame++) {
                if (record.hasFrame(frame)) {
                    if (cells.contains(new Point(frame, record.placeDepth))) {
                        selected = true;
                    } else {
                        remaining = true;
                    }
                }
            }
        }
        return selected && remaining;
    }

    private void clearFrames(Set<Point> cells) {
        if (canClearFrames(cells)) {
            editKeyframes(cells, KeyEdit.CLEAR_FRAMES, "action.clearFrames");
        }
    }

    Set<Point> getSelectedBlockCells() {
        Set<Point> cells = new LinkedHashSet<>();
        for (Point cell : getEditableCells()) {
            if (cells.contains(cell) || !isFrameOccupied(cell.x, cell.y)) {
                continue;
            }
            FrameSpan span = getFrameBlock(cell.x, cell.y, true);
            for (int frame = span.first; frame <= span.last; frame++) {
                cells.add(new Point(frame, cell.y));
            }
        }
        return cells;
    }

    private Set<Point> getEditableCells() {
        Set<Point> cells = new LinkedHashSet<>();
        Set<Integer> locked = getLockedDepths();
        int last = getFrameLimit();
        int depthCount = getDepthCount();
        for (Point cell : cursor) {
            if (cell.x >= 0 && cell.x <= last && cell.y > 0 && cell.y <= depthCount && !locked.contains(cell.y)) {
                cells.add(cell);
            }
        }
        return cells;
    }

    private static void addFrameMenuItem(JPopupMenu menu, String key, boolean enabled, Runnable action) {
        JMenuItem item = new JMenuItem(EasyStrings.translate(key));
        item.setEnabled(enabled);
        item.addActionListener(event -> action.run());
        menu.add(item);
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

    private void editFrames(Set<Point> cells, boolean insert) {
        String description = insert ? "action.insertFrame" : "action.removeFrames";
        Set<Point> after = insert ? cursor : selectionAfterRemovingFrames(cells);
        if (timeline.timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timeline.timelined;
            TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, getBlankFrames());
            edit.editFrames(cells, insert);
            applyButtonEdit(button, edit, description, after);
        } else {
            TimelineFrameEdits edit = new TimelineFrameEdits(timeline, getBlankFrames());
            if (insert) {
                edit.insertFrames(cells);
            } else {
                edit.removeFrames(cells);
            }
            applyFrameEdit(edit, description, after);
        }
    }

    private Set<Point> selectionAfterRemovingFrames(Set<Point> cells) {
        Point first = Collections.min(cells, Comparator.comparingInt((Point cell) -> cell.x).thenComparingInt(cell -> cell.y));
        int end = getLayerEnd(first.y, getBlankFrames());
        int removed = (int) cells.stream().filter(cell -> cell.y == first.y && cell.x <= end).count();
        return Collections.singleton(new Point(Math.max(0, Math.min(first.x, end - removed)), first.y));
    }

    private void editKeyframes(Set<Point> cells, KeyEdit action, String description) {
        if (timeline.timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timeline.timelined;
            TimelineButtonFrameEdits edit = new TimelineButtonFrameEdits(button, getBlankFrames());
            edit.editKeyframes(cells, action);
            applyButtonEdit(button, edit, description, cursor);
        } else {
            TimelineFrameEdits edit = new TimelineFrameEdits(timeline, getBlankFrames());
            edit.editKeyframes(cells, action);
            applyFrameEdit(edit, description, cursor);
        }
    }

    private void applyFrameEdit(TimelineFrameEdits edit, String description, Set<Point> after) {
        if (edit.fitsSwf()) {
            applyTimelineEdit(edit::apply, !edit.isEmpty(), edit.getBlankFrames(), description, after);
        }
    }

    private void applyButtonEdit(ButtonTag button, TimelineButtonFrameEdits edit, String description, Set<Point> after) {
        if (edit.changesRecords() && edit.getRecords().isEmpty()) {
            return;
        }
        applyTimelineEdit(() -> {
            button.getRecords().clear();
            for (BUTTONRECORD record : edit.getRecords()) {
                button.getRecords().add(new BUTTONRECORD(record));
            }
            button.resetTimeline();
        }, edit.changesRecords(), edit.getBlankFrames(), description, after);
    }

    boolean canAddLayer() {
        return timeline != null && getDepthCount() < TimelineLayerEdit.MAX_DEPTH
                && TimelineLayerEdit.canInsert(timeline.timelined);
    }

    void addLayer(int aboveDepth) {
        if (canAddLayer()) {
            editLayer(aboveDepth + 1, true);
        }
    }

    boolean canRemoveLayer(int depth) {
        if (depth <= 0 || depth > getDepthCount() || getLockedDepths().contains(depth)) {
            return false;
        }
        if (timeline.timelined instanceof ButtonTag) {
            List<BUTTONRECORD> records = ((ButtonTag) timeline.timelined).getRecords();
            return records.isEmpty() || records.stream().anyMatch(record -> record.placeDepth != depth);
        }
        return true;
    }

    void removeLayer(int depth) {
        if (canRemoveLayer(depth)) {
            editLayer(depth, false);
        }
    }

    private void editLayer(int depth, boolean inserting) {
        Timelined target = timeline.timelined;
        TimelineLayerEdit edit = new TimelineLayerEdit(target, depth, inserting);
        TimelineBlankFrames originalBlanks = getBlankFrames();
        TimelineBlankFrames movedBlanks = originalBlanks.remap(value -> TimelineLayerEdit.mapDepth(value, depth, inserting));
        int beforeCount = getDepthCount();
        int afterCount = beforeCount + (inserting ? 1 : -1);
        Set<Point> before = new LinkedHashSet<>(cursor);
        Set<Point> after = new LinkedHashSet<>();
        int frame = getFirstFrame();
        if (inserting) {
            after.add(new Point(frame, depth));
        } else {
            for (Point cell : before) {
                if (cell.y != depth) {
                    after.add(new Point(cell.x, TimelineLayerEdit.mapDepth(cell.y, depth, false)));
                }
            }
            if (after.isEmpty()) {
                after.add(new Point(frame, Math.min(afterCount, Math.max(1, depth - 1))));
            }
        }
        undoManager.doOperation(new DoableOperation() {
            private boolean hidden;
            private boolean locked;

            @Override
            public void doOperation() {
                apply(true);
            }

            @Override
            public void undoOperation() {
                apply(false);
            }

            private void apply(boolean forward) {
                swfPanel.setTimelined(target);
                boolean grows = forward == inserting;
                if (!grows) {
                    hidden = swfPanel.getHiddenDepths(target).contains(depth);
                    locked = swfPanel.getLockedDepths(target).contains(depth);
                }
                edit.apply(forward);
                swfPanel.setTimelineDepthCount(target, forward ? afterCount : beforeCount);
                swfPanel.setTimelineBlankFrames(target, forward ? movedBlanks : originalBlanks);
                swfPanel.remapDepthInteraction(value -> TimelineLayerEdit.mapDepth(value, depth, grows));
                if (grows) {
                    swfPanel.restoreLayerFlags(depth, hidden, locked);
                }
                refreshAfterEdit(target, forward ? after : before);
                swfPanel.refreshDepthInteraction();
                scrollRectToVisible(getFrameBounds(getFirstFrame(), getFirstDepth()));
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate(inserting ? "timeline.shape.addLayer" : "timeline.shape.removeLayer");
            }

            @Override
            public boolean modifiesSwf() {
                return edit.modifiesSwf();
            }
        }, target.getSwf());
    }

    void moveLayer(int source, int destination) {
        if (timeline == null || source == destination) {
            return;
        }
        Timelined target = timeline.timelined;
        TimelineDepthMove move = new TimelineDepthMove(source, destination);
        TimelineDepthMove.Result result = move.check(target);
        if (result == TimelineDepthMove.Result.MASK_CONFLICT) {
            JOptionPane.showMessageDialog(this, EasyStrings.translate("timeline.depth.moveMaskConflict"),
                    EasyStrings.translate("timeline.shape.moveLayer"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        TimelineBlankFrames originalBlanks = getBlankFrames();
        TimelineBlankFrames movedBlanks = originalBlanks.remap(move::mapDepth);
        if (result == TimelineDepthMove.Result.UNCHANGED && originalBlanks.equals(movedBlanks)) {
            return;
        }
        int depthCount = getDepthCount();
        Set<Point> before = new LinkedHashSet<>(cursor);
        Set<Point> after = new LinkedHashSet<>();
        for (Point cell : before) {
            after.add(new Point(cell.x, move.mapDepth(cell.y)));
        }
        undoManager.doOperation(new DoableOperation() {
            @Override
            public void doOperation() {
                apply(move, movedBlanks, after);
            }

            @Override
            public void undoOperation() {
                apply(move.reversed(), originalBlanks, before);
            }

            private void apply(TimelineDepthMove step, TimelineBlankFrames frames, Set<Point> selection) {
                swfPanel.setTimelined(target);
                step.apply(target);
                swfPanel.setTimelineDepthCount(target, depthCount);
                swfPanel.setTimelineBlankFrames(target, frames);
                swfPanel.remapDepthInteraction(step::mapDepth);
                refreshAfterEdit(target, selection);
                swfPanel.refreshDepthInteraction();
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate("timeline.shape.moveLayer");
            }

            @Override
            public boolean modifiesSwf() {
                return result == TimelineDepthMove.Result.VALID;
            }
        }, target.getSwf());
    }

    private void applyTimelineEdit(Runnable edit, boolean changesTags, TimelineBlankFrames blankFrames,
            String description, Set<Point> selectionAfterEdit) {
        TimelineBlankFrames originalBlanks = getBlankFrames();
        if (!changesTags && originalBlanks.equals(blankFrames)) {
            return;
        }
        Timelined timelined = timeline.timelined;
        Set<Point> originalSelection = new LinkedHashSet<>(cursor);
        Set<Point> editedSelection = new LinkedHashSet<>(selectionAfterEdit);
        int originalFrameCount = timelined.getFrameCount();
        int depthCount = getDepthCount();
        undoManager.doOperation(new TimelinedTagListDoableOperation(swfPanel, timelined) {
            @Override
            public void doOperation() {
                if (changesTags) {
                    super.doOperation();
                    edit.run();
                } else {
                    swfPanel.setTimelined(timelined);
                }
                restore(blankFrames, editedSelection);
            }

            @Override
            public void undoOperation() {
                if (changesTags) {
                    super.undoOperation();
                    timelined.setFrameCount(originalFrameCount);
                } else {
                    swfPanel.setTimelined(timelined);
                }
                restore(originalBlanks, originalSelection);
            }

            private void restore(TimelineBlankFrames frames, Set<Point> selection) {
                swfPanel.setTimelineDepthCount(timelined, depthCount);
                swfPanel.setTimelineBlankFrames(timelined, frames);
                refreshAfterEdit(timelined, selection);
            }

            @Override
            public String getDescription() {
                return EasyStrings.translate(description);
            }

            @Override
            public boolean modifiesSwf() {
                return changesTags;
            }
        }, timelined.getSwf());
    }

    void refreshAfterEdit(Timelined target, Set<Point> selection) {
        timeline = target.getTimeline();
        target.getSwf().clearImageCache();
        restoreSelection(selection);
        refresh();
        fireChanged();
        fireFrameSelected(getFirstFrame(), getDepthsAtFirstFrame());
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
            case KeyEvent.VK_DELETE:
                if (e.getModifiersEx() == 0) {
                    clearFrames(getSelectedBlockCells());
                    requestFocusInWindow();
                    e.consume();
                }
                break;
            case 37: //left
                if (firstCursorItem.x > 0) {
                    frameSelect(firstCursorItem.x - 1, firstCursorItem.y, true);
                }
                break;
            case 39: //right
                if (firstCursorItem.x < getDisplayedFrameCount() - 1) {
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
        int maxDepth = getDepthCount();
        for (int row = flipLayerOrder(cell.y, maxDepth) + step; row >= 0 && row <= maxDepth; row += step) {
            int depth = flipLayerOrder(row, maxDepth);
            if (!locked.contains(depth)) {
                frameSelect(cell.x, depth, true);
                return;
            }
        }
    }

    // Converts in both directions. The actions row and spare rows stay in place.
    static int flipLayerOrder(int index, int maxDepth) {
        return index > 0 && index <= maxDepth ? maxDepth + 1 - index : index;
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
        rect.y = flipLayerOrder(depth, getDepthCount()) * FRAME_HEIGHT;
        return rect;
    }

    private int getDepthCount() {
        if (timeline == null) {
            return 0;
        }
        return swfPanel == null ? timeline.getMaxDepth() : swfPanel.getTimelineDepthCount(timeline.timelined);
    }

    private TimelineBlankFrames getBlankFrames() {
        return timeline == null || swfPanel == null ? new TimelineBlankFrames() : swfPanel.getTimelineBlankFrames(timeline.timelined);
    }

    private int getDisplayedFrameCount() {
        return timeline == null ? 0 : Math.max(timeline.getFrameCount(), getBlankFrames().lastFrame() + 1);
    }

    private int getLayerEnd(int depth, TimelineBlankFrames blanks) {
        int lastOccupied = timeline.getDepthMaxFrameButtons().getOrDefault(depth, -1);
        return Math.max(lastOccupied, blanks.end(depth));
    }

    private DepthState getPlacedState(int frame, int depth) {
        if (timeline.timelined instanceof ButtonTag && ((ButtonTag) timeline.timelined).getButtonRecordAt(frame, depth, false) == null) {
            return null;
        }
        DepthState state = timeline.getDepthState(frame, depth);
        return TimelineFrameEdits.isPlaced(state) ? state : null;
    }

    private boolean isKey(int frame, int depth, TimelineBlankFrames blanks) {
        if (timeline.timelined instanceof ButtonTag) {
            ButtonTag button = (ButtonTag) timeline.timelined;
            BUTTONRECORD record = button.getButtonRecordAt(frame, depth, false);
            return record != button.getButtonRecordAt(frame - 1, depth, false)
                    || (record == null && blanks.hasKey(depth, frame));
        }
        DepthState state = getPlacedState(frame, depth);
        if (state != null) {
            return state.key;
        }
        return blanks.hasKey(depth, frame) || (frame < timeline.getFrameCount() && getPlacedState(frame - 1, depth) != null);
    }

    private boolean startsFrameBlock(int frame, int depth, TimelineBlankFrames blanks) {
        return isKey(frame, depth, blanks)
                || (frame == timeline.getFrameCount() && getPlacedState(frame - 1, depth) != null);
    }

    public void refresh() {
        int frameCount = getDisplayedFrameCount();
        int maxDepth = getDepthCount();
        Dimension dim = new Dimension(getFrameWidth() * frameCount + 1, FRAME_HEIGHT * (maxDepth + 1 /*actions*/ + 1 /*one additional*/));
        setSize(dim);
        setPreferredSize(dim);
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
        refresh();
    }
}
