/*
 *  Copyright (C) 2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import javax.swing.JLabel;

/**
 *
 * @author JPEXS
 */
class DottedUnderlineLabel extends JLabel {

    public DottedUnderlineLabel(String text) {
        super(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Font font = getFont();
        FontMetrics metrics = getFontMetrics(font);
        String text = getText();

        int x = 0;
        int y = metrics.getAscent();

        g2d.setPaint(getForeground());

        int textWidth = metrics.stringWidth(text);
        int underlineY = y + 6;

        float[] dash = {1f, 1f};
        Stroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, dash, 0f);
        g2d.setStroke(dotted);

        g2d.draw(new Line2D.Float(x, underlineY, x + textWidth, underlineY));
    }
}
