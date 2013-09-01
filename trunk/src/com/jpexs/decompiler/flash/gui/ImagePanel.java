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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImagePanel extends JPanel implements ActionListener {

    public JLabel label = new JLabel();
    public DrawableTag drawable;
    private Timer timer;
    private int percent;
    private int frame;

    @Override
    public void setBackground(Color bg) {
        if (label != null) {
            label.setBackground(bg);//bg);            
        }
        super.setBackground(bg);
    }

    public ImagePanel() {
        super(new BorderLayout());
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        setOpaque(true);
        setBackground(View.DEFAULT_BACKGROUND_COLOR);
        add(label, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton selectColorButton = new JButton(View.getIcon("color16"));
        selectColorButton.addActionListener(this);
        selectColorButton.setActionCommand("SELECTCOLOR");
        selectColorButton.setToolTipText(AppStrings.translate("button.selectcolor.hint"));
        buttonsPanel.add(selectColorButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("SELECTCOLOR".equals(e.getActionCommand())) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectcolor.title"), View.swfBackgroundColor);
                    if (newColor != null) {
                        View.swfBackgroundColor = newColor;
                        setBackground(newColor);
                        repaint();
                    }
                }
            });

        }
    }

    public void setImage(byte data[]) {
        setBackground(View.swfBackgroundColor);
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
            setImage(drawable.toImage(0, swf.tags, swf.displayRect, characters, new Stack<Integer>()));
            return;
        }
        timer = new Timer();

        percent = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int nframe = percent * drawable.getNumFrames() / 100;
                if (nframe != frame) {
                    ImageIcon icon = new ImageIcon(drawable.toImage(nframe, swf.tags, swf.displayRect, characters, new Stack<Integer>()));
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
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        ImageIcon icon = new ImageIcon(image);
        label.setIcon(icon);
    }
}
