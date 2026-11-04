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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author JPEXS
 */
public class TimelineDepthPanel extends JPanel {

    private final Icon showIcon = View.getIcon("show16");
    private final Icon hideIcon = View.getIcon("hide16");
    private final Supplier<Set<Integer>> hiddenDepths;

    private int maxDepth;
    private int iconX;

    public static final int PADDING = 5;

    public static final float FONT_SIZE = 10.0f;

    private int scrollOffset = 0;

    public static final Color BORDER_COLOR = Color.lightGray;

    public static final Color FONT_COLOR = Color.black;

    public TimelineDepthPanel(Supplier<Set<Integer>> hiddenDepths, IntConsumer toggleDepth) {
        this.hiddenDepths = hiddenDepths;
        setToolTipText("");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int depth = getIconDepth(e);
                if (SwingUtilities.isLeftMouseButton(e) && depth > 0) {
                    toggleDepth.accept(depth);
                    repaint();
                }
            }
        });
        setTimeline(null);
    }
    
    public void setTimeline(Timeline timeline) {
        maxDepth = timeline == null ? 0 : timeline.getMaxDepth();
        String maxDepthStr = Integer.toString(maxDepth);
        setFont(getFont().deriveFont(FONT_SIZE));
        int height = (maxDepth + 1) * TimelineBodyPanel.FRAME_HEIGHT + 1;
        int maxDepthW = getFontMetrics(getFont()).stringWidth(maxDepthStr);
        iconX = maxDepthW + 2 * PADDING;
        int width = iconX + showIcon.getIconWidth() + PADDING;
        Dimension dim = new Dimension(width, height);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(new Dimension(width, 0));
        revalidate();
        repaint();
    }

    public void scroll(int offset) {
        this.scrollOffset = offset;
        repaint();
    }

    private int getIconDepth(MouseEvent e) {
        if (e.getX() < iconX || e.getX() >= iconX + showIcon.getIconWidth()) {
            return 0;
        }
        int rowHeight = TimelineBodyPanel.FRAME_HEIGHT;
        int depth = (scrollOffset + e.getY()) / rowHeight;
        return depth > 0 && depth <= maxDepth ? depth : 0;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int depth = getIconDepth(e);
        return depth == 0 ? null : EasyStrings.translate(hiddenDepths.get().contains(depth)
                ? "timeline.depth.show" : "timeline.depth.hide");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int rowHeight = TimelineBodyPanel.FRAME_HEIGHT;
        int firstDepth = (scrollOffset + clip.y) / rowHeight;
        int lastDepth = Math.min(maxDepth, (scrollOffset + clip.y + clip.height - 1) / rowHeight);
        Set<Integer> hidden = hiddenDepths.get();
        g.setColor(TimelineBodyPanel.getBackgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        for (int depth = firstDepth; depth <= lastDepth; depth++) {
            int y = depth * rowHeight - scrollOffset;
            g.setColor(BORDER_COLOR);
            g.drawLine(0, y + rowHeight, getWidth(), y + rowHeight);
            g.setColor(FONT_COLOR);
            g.drawString(depth == 0 ? "a" : Integer.toString(depth), PADDING, y + rowHeight - PADDING);
            if (depth > 0) {
                Icon icon = hidden.contains(depth) ? hideIcon : showIcon;
                icon.paintIcon(this, g, iconX, y + (rowHeight - icon.getIconHeight()) / 2);
            }
        }
    }
}
