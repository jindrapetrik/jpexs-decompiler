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
package com.jpexs.decompiler.flash.gui.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import jsyntaxpane.components.Markers;

/**
 *
 * @author JPEXS
 */
public class WavyUnderLinePainter extends Markers.SimpleMarker {

    public WavyUnderLinePainter(Color color) {
        super(color);
    }

    @Override
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        try {
            // --- determine locations ---
            TextUI mapper = c.getUI();

            Color col = getColor();
            if (col == null) {
                col = Color.black;
            }
            g.setColor(col);

            int waveHeight = 2;

            for (int i = offs0; i < offs1; i++) {

                Rectangle2D r = com.jpexs.decompiler.flash.gui.View.textUIModelToView(mapper, c, i, Position.Bias.Forward);
                Rectangle2D r1 = com.jpexs.decompiler.flash.gui.View.textUIModelToView(mapper, c, i + 1, Position.Bias.Forward);
                if (r1.getY() == r.getY()) {
                    int x = (int) r.getX();
                    int y = (int) (r.getY() + r1.getHeight());
                    int maxX = (int) r1.getX();
                    boolean up = true;
                    for (; x < maxX; x += 2) {
                        if (up) {
                            g.drawLine(x, y, x + 2, y - waveHeight);
                        } else {
                            g.drawLine(x, y - waveHeight, x + 2, y);
                        }
                        up = !up;
                    }
                }
            }

        } catch (BadLocationException e) {
            // can't render
        }
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1,
            Shape bounds, JTextComponent c, View view) {

        g.setColor(c.getSelectionColor());

        Rectangle r;

        if (offs0 == view.getStartOffset()
                && offs1 == view.getEndOffset()) {
            // Contained in view, can just use bounds.
            if (bounds instanceof Rectangle) {
                r = (Rectangle) bounds;
            } else {
                r = bounds.getBounds();
            }
        } else {
            // Should only render part of View.
            try {
                // --- determine locations ---
                Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                        offs1, Position.Bias.Backward,
                        bounds);
                r = (shape instanceof Rectangle)
                        ? (Rectangle) shape : shape.getBounds();
            } catch (BadLocationException e) {
                // can't render
                r = null;
            }
        }

        if (r != null) {
            r.width = Math.max(r.width, 1);

            paint(g, offs0, offs1, r, c);

        }

        return r;
    }
}
