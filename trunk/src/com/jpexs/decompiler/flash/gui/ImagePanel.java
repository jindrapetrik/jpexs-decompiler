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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import java.awt.BorderLayout;
import java.awt.Image;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {

    public JLabel label = new JLabel();
    public DrawableTag drawable;
    private Timer timer;
    private int percent;
    private int frame;

    public ImagePanel() {
        super(new BorderLayout());
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
    }

    public void setImage(byte data[]) {
        if (timer != null) {
            timer.cancel();
        }
        ImageIcon icon = new ImageIcon(data);
        label.setIcon(icon);
    }

    public void setDrawable(final DrawableTag drawable, final SWF swf, final HashMap<Integer, CharacterTag> characters) {
        this.drawable = drawable;
        if (timer != null) {
            timer.cancel();
        }

        if (drawable.getNumFrames() == 0) {
            label.setIcon(null);
            return;
        }
        if (drawable.getNumFrames() == 1) {
            setImage(drawable.toImage(0, swf.tags, swf.displayRect, characters));
            return;
        }
        timer = new Timer();

        percent = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int nframe = percent * drawable.getNumFrames() / 100;
                if (nframe != frame) {
                    ImageIcon icon = new ImageIcon(drawable.toImage(nframe, swf.tags, swf.displayRect, characters));
                    label.setIcon(icon);
                }
                if (percent == 100) {
                    percent = 0;
                } else {
                    percent++;
                }

            }
        }, 0, 20);
    }

    public void setImage(Image image) {
        if (timer != null) {
            timer.cancel();
        }
        ImageIcon icon = new ImageIcon(image);
        label.setIcon(icon);
    }
}
