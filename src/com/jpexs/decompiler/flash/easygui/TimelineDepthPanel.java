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

import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author JPEXS
 */
public class TimelineDepthPanel extends JPanel {

    private final Icon showIcon = View.getIcon("show16");
    private final Icon crossIcon = View.getIcon("cross16");
    private final Icon lockIcon = View.getIcon("lock16");
    private final Icon bulletIcon = View.getIcon("bullet16");
    private final EasySwfPanel swfPanel;
    private final boolean shapeTimeline;
    private final TimelineLayerSelection layerSelection;
    private final JPanel header = new JPanel() {
        @Override
        public String getToolTipText(MouseEvent e) {
            if (maxDepth > 0) {
                if (isOverIcon(e, visibilityX, showIcon)) {
                    return EasyStrings.translate("timeline.depth.toggleAllVisibility");
                }
                if (isOverIcon(e, lockX, lockIcon)) {
                    return EasyStrings.translate("timeline.depth.toggleAllLocks");
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(TimelineBodyPanel.getBackgroundColor());
            g.fillRect(0, 0, getWidth(), getHeight());
            paintColumnIcon(g, showIcon, visibilityX, showIcon.getIconWidth(), 0, getHeight());
            paintColumnIcon(g, lockIcon, lockX, lockIcon.getIconWidth(), 0, getHeight());
        }
    };

    private Timelined timelined;
    private int maxDepth;
    private int dropRow = -1;
    private int visibilityX;
    private int lockX;

    public static final int PADDING = 5;

    public static final float FONT_SIZE = 10.0f;

    private int scrollOffset = 0;

    public static final Color BORDER_COLOR = Color.lightGray;

    public static final Color FONT_COLOR = Color.black;

    public TimelineDepthPanel(EasySwfPanel swfPanel) {
        this(swfPanel, false, new TimelineLayerSelection());
    }

    TimelineDepthPanel(EasySwfPanel swfPanel, boolean shapeTimeline, TimelineLayerSelection layerSelection) {
        this.swfPanel = swfPanel;
        this.shapeTimeline = shapeTimeline;
        this.layerSelection = layerSelection;
        layerSelection.addListener(this::repaint);
        Dimension headerSize = new Dimension(0, TimelineBodyPanel.FRAME_HEIGHT);
        header.setPreferredSize(headerSize);
        header.setMinimumSize(headerSize);
        header.setToolTipText("");
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e) || maxDepth == 0) {
                    return;
                }
                if (isOverIcon(e, visibilityX, showIcon)) {
                    if (shapeTimeline) {
                        swfPanel.toggleShapeLayerVisibility(-1);
                    } else {
                        swfPanel.toggleAllDepthVisibility();
                    }
                } else if (isOverIcon(e, lockX, lockIcon)) {
                    if (shapeTimeline) {
                        swfPanel.toggleShapeLayerLock(-1);
                    } else {
                        swfPanel.toggleAllDepthLocks();
                    }
                }
                repaint();
            }
        });
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int depth = getDepthAt(e);
                if (!SwingUtilities.isLeftMouseButton(e) || depth == 0) {
                    return;
                }
                if (isOverIcon(e, visibilityX, showIcon)) {
                    if (shapeTimeline) {
                        swfPanel.toggleShapeLayerVisibility(depth - 1);
                    } else {
                        swfPanel.toggleDepthVisibility(depth);
                    }
                } else if (isOverIcon(e, lockX, lockIcon)) {
                    if (shapeTimeline) {
                        swfPanel.toggleShapeLayerLock(depth - 1);
                    } else {
                        swfPanel.toggleDepthLock(depth);
                    }
                }
                repaint();
            }
        });
        setTimeline(null);
    }

    JPanel getHeader() {
        return header;
    }

    public void setTimeline(Timeline timeline) {
        timelined = timeline == null ? null : timeline.timelined;
        if (timeline == null) {
            setDepthCount(0);
        } else {
            setDepthCount(swfPanel == null ? timeline.getMaxDepth() : swfPanel.getTimelineDepthCount(timelined));
        }
    }

    void setDepthCount(int count) {
        maxDepth = count;
        setFont(getFont().deriveFont(FONT_SIZE));
        int height = (maxDepth + (shapeTimeline ? 0 : 1)) * TimelineBodyPanel.FRAME_HEIGHT + 1;
        int maxDepthW = getFontMetrics(getFont()).stringWidth(depthLabel(Math.max(1, maxDepth)));
        visibilityX = maxDepthW + 2 * PADDING;
        lockX = visibilityX + showIcon.getIconWidth() + PADDING;
        int width = lockX + lockIcon.getIconWidth() + PADDING;
        Dimension dim = new Dimension(width, height);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(new Dimension(width, 0));
        revalidate();
        repaint();
        header.repaint();
    }

    public void scroll(int offset) {
        this.scrollOffset = offset;
        repaint();
    }

    int getDepthCount() {
        return maxDepth;
    }

    TimelineLayerSelection getLayerSelection() {
        return layerSelection;
    }

    void setDropRow(int row) {
        if (dropRow != row) {
            dropRow = row;
            repaint();
        }
    }

    int getDepthAt(MouseEvent e) {
        int row = (scrollOffset + e.getY()) / TimelineBodyPanel.FRAME_HEIGHT;
        int depth = shapeTimeline ? maxDepth - row : TimelineBodyPanel.flipLayerOrder(row, maxDepth);
        return depth > 0 && depth <= maxDepth ? depth : 0;
    }

    boolean isActionsRowAt(MouseEvent event) {
        int y = scrollOffset + event.getY();
        return y >= 0 && y < TimelineBodyPanel.FRAME_HEIGHT;
    }

    boolean isOverControls(MouseEvent e) {
        return isOverIcon(e, visibilityX, showIcon) || isOverIcon(e, lockX, lockIcon);
    }

    private String depthLabel(int depth) {
        return depth == 0 ? "a"
                : EasyStrings.translate("timeline.shape.layerNumber").replace("%number%", Integer.toString(depth));
    }

    private boolean isHidden(int depth) {
        return shapeTimeline ? swfPanel.isShapeLayerHidden(depth - 1) : swfPanel.getHiddenDepths(timelined).contains(depth);
    }

    private boolean isLocked(int depth) {
        return shapeTimeline ? swfPanel.isShapeLayerLocked(depth - 1) : swfPanel.getLockedDepths(timelined).contains(depth);
    }

    private boolean isOverIcon(MouseEvent e, int x, Icon icon) {
        return e.getX() >= x && e.getX() < x + icon.getIconWidth();
    }

    private void paintColumnIcon(Graphics g, Icon icon, int x, int width, int y, int height) {
        icon.paintIcon(this, g, x + (width - icon.getIconWidth()) / 2, y + (height - icon.getIconHeight()) / 2);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int depth = getDepthAt(e);
        if (depth > 0) {
            if (isOverIcon(e, visibilityX, showIcon)) {
                return EasyStrings.translate(isHidden(depth)
                        ? "timeline.depth.show" : "timeline.depth.hide");
            }
            if (isOverIcon(e, lockX, lockIcon)) {
                return EasyStrings.translate(isLocked(depth)
                        ? "timeline.depth.unlock" : "timeline.depth.lock");
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int rowHeight = TimelineBodyPanel.FRAME_HEIGHT;
        int firstRow = (scrollOffset + clip.y) / rowHeight;
        int lastRow = Math.min(shapeTimeline ? maxDepth - 1 : maxDepth, (scrollOffset + clip.y + clip.height - 1) / rowHeight);
        g.setColor(TimelineBodyPanel.getBackgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        for (int row = firstRow; row <= lastRow; row++) {
            int depth = shapeTimeline ? maxDepth - row : TimelineBodyPanel.flipLayerOrder(row, maxDepth);
            int y = row * rowHeight - scrollOffset;
            boolean selected = layerSelection.get().depths.contains(depth);
            if (selected) {
                g.setColor(TimelineBodyPanel.getSelectedColor());
                g.fillRect(0, y, getWidth(), rowHeight);
            }
            g.setColor(BORDER_COLOR);
            g.drawLine(0, y + rowHeight, getWidth(), y + rowHeight);
            g.setColor(selected ? TimelineBodyPanel.getSelectedColorText() : FONT_COLOR);
            g.drawString(depthLabel(depth), PADDING, y + rowHeight - PADDING);
            if (depth > 0) {
                Icon visibility = isHidden(depth) ? crossIcon : bulletIcon;
                Icon lock = isLocked(depth) ? lockIcon : bulletIcon;
                paintColumnIcon(g, visibility, visibilityX, showIcon.getIconWidth(), y, rowHeight);
                paintColumnIcon(g, lock, lockX, lockIcon.getIconWidth(), y, rowHeight);
            }
        }
        if (dropRow >= 0) {
            TimelineLayerDrag.paintDropLine(g, getWidth(), dropRow * rowHeight - scrollOffset);
        }
    }
}
