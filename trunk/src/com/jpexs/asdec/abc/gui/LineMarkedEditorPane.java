/*
 *  Copyright (C) 2011-2013 JPEXS
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
package com.jpexs.asdec.abc.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;

/**
 *
 * @author JPEXS
 */
public class LineMarkedEditorPane extends JEditorPane {

   int lastLine = -1;

   public LineMarkedEditorPane() {
      setOpaque(false);
      addCaretListener(new CaretListener() {
         public void caretUpdate(CaretEvent e) {
            int caretPosition = getCaretPosition();
            Element root = getDocument().getDefaultRootElement();
            int currentLine = root.getElementIndex(caretPosition);
            if (currentLine != lastLine) {
               lastLine = currentLine;
               repaint();
            }
         }
      });
   }

   @Override
   public void paint(Graphics g) {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      FontMetrics fontMetrics = g.getFontMetrics();
      int lh = fontMetrics.getHeight();
      int a = fontMetrics.getAscent();
      int d = fontMetrics.getDescent();
      int h = a + d;
      int rH = h;
      g.setColor(new Color(0xee, 0xee, 0xee));
      g.fillRect(0, d + lh * lastLine - 1, getWidth(), lh);
      super.paint(g);
   }
}
