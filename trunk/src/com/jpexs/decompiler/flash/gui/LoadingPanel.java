/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Panel with loading animation
 *
 * @author JPEXS
 */
public class LoadingPanel extends JPanel {

   private int pos = 0;
   private Image animationImage;
   private int iconWidth;
   private int iconHeight;

   /**
    * Constructor
    *
    * @param iconWidth Width of displayed icon
    * @param iconHeight Height of displayed icon
    */
   public LoadingPanel(int iconWidth, int iconHeight) {
      this.iconWidth = iconWidth;
      this.iconHeight = iconHeight;
      ImageIcon icon = (new ImageIcon(this.getClass().getResource("/com/jpexs/decompiler/flash/gui/graphics/loading.png")));
      animationImage = icon.getImage();
      java.util.Timer timer = new java.util.Timer();
      timer.schedule(new java.util.TimerTask() {
         public void run() {
            pos = (pos + 1) % 12;
            repaint();
         }
      }, 100, 100);
   }

   /**
    * Paints component
    *
    * @param g Graphics to paint on
    */
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawImage(animationImage, getWidth() / 2 - iconWidth / 2, getHeight() / 2 - iconHeight / 2, getWidth() / 2 + iconWidth / 2, getHeight() / 2 + iconHeight / 2, pos * 100, 0, (pos + 1) * 100, 100, this);
   }
}
