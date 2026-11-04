/*
 *  Copyright (C) 2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties.panels;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import javax.swing.Icon;

/**
 * Illustrations for stroke options, painted in the renderer's foreground color.
 */
final class StrokeOptionIcons {

    private StrokeOptionIcons() {
    }

    static Icon cap(int cap) {
        return icon(graphics -> {
            Color foreground = graphics.getColor();
            graphics.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 110));
            graphics.drawLine(8, 3, 8, 18);
            graphics.drawLine(22, 3, 22, 18);
            graphics.setColor(foreground);
            graphics.setStroke(new BasicStroke(8, cap, BasicStroke.JOIN_ROUND));
            graphics.drawLine(8, 11, 22, 11);
        });
    }

    static Icon join(int join) {
        return icon(graphics -> {
            Path2D elbow = new Path2D.Float();
            elbow.moveTo(24, 6);
            elbow.lineTo(9, 6);
            elbow.lineTo(9, 19);
            graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_BUTT, join));
            graphics.draw(elbow);
        });
    }

    static Icon scale(boolean horizontal, boolean vertical) {
        return icon(graphics -> {
            graphics.drawRect(12, 8, 6, 6);
            if (horizontal) {
                arrow(graphics, 10, 11, -1, 0, 8);
                arrow(graphics, 20, 11, 1, 0, 8);
            }
            if (vertical) {
                arrow(graphics, 15, 6, 0, -1, 5);
                arrow(graphics, 15, 16, 0, 1, 5);
            }
        });
    }

    private static void arrow(Graphics2D graphics, int x, int y, int dx, int dy, int length) {
        int tipX = x + length * dx;
        int tipY = y + length * dy;
        graphics.drawLine(x, y, tipX, tipY);
        graphics.drawLine(tipX, tipY, tipX - 3 * dx + 2 * dy, tipY - 3 * dy - 2 * dx);
        graphics.drawLine(tipX, tipY, tipX - 3 * dx - 2 * dy, tipY - 3 * dy + 2 * dx);
    }

    private static Icon icon(Consumer<Graphics2D> draw) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return 30;
            }

            @Override
            public int getIconHeight() {
                return 22;
            }

            @Override
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                Graphics2D copy = (Graphics2D) graphics.create(x, y, getIconWidth(), getIconHeight());
                try {
                    if (component != null) {
                        copy.setColor(component.getForeground());
                    }
                    copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    draw.accept(copy);
                } finally {
                    copy.dispose();
                }
            }
        };
    }
}
