/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.gui.player.FlashDisplay;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.helpers.SerializableImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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

public final class ImagePanel extends JPanel implements ActionListener, FlashDisplay {

    static final String ACTION_SELECT_BKCOLOR = "SELECTCOLOR";

    public JLabel label = new JLabel();
    public DrawableTag drawable;
    private Timer timer;
    private int percent;
    private int frame = -1;
    private SWF swf;
    private HashMap<Integer, CharacterTag> characters;
    private int frameRate;
    private boolean loaded;

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
        selectColorButton.setActionCommand(ACTION_SELECT_BKCOLOR);
        selectColorButton.setToolTipText(AppStrings.translate("button.selectbkcolor.hint"));
        buttonsPanel.add(selectColorButton);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == ACTION_SELECT_BKCOLOR) {
            View.execInEventDispatch(new Runnable() {
                @Override
                public void run() {
                    Color newColor = JColorChooser.showDialog(null, AppStrings.translate("dialog.selectbkcolor.title"), View.swfBackgroundColor);
                    if (newColor != null) {
                        View.swfBackgroundColor = newColor;
                        setBackground(newColor);
                        repaint();
                    }
                }
            });

        }
    }

    public void setImage(byte[] data) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        drawable = null;
        loaded = true;
        ImageIcon icon = new ImageIcon(data);
        label.setIcon(icon);
    }

    public void setDrawable(final DrawableTag drawable, final SWF swf, final HashMap<Integer, CharacterTag> characters, int frameRate) {
        pause();
        this.drawable = drawable;
        this.swf = swf;
        this.characters = characters;
        loaded = true;

        if (drawable.getNumFrames() == 0) {
            label.setIcon(null);
            return;
        }
        percent = 0;
        if (drawable.getNumFrames() == 1) {
            Matrix mat = new Matrix();
            mat.translateX = swf.displayRect.Xmin;
            mat.translateY = swf.displayRect.Ymin;
            String key = "drawable_0_" + drawable.hashCode();
            SerializableImage img = SWF.getFromCache(key);
            if (img == null) {
                img = drawable.toImage(0, swf.tags, characters, new Stack<Integer>(), Matrix.getScaleInstance(1 / SWF.unitDivisor));
                SWF.putToCache(key, img);
            }
            if (img != null && img.bounds != null && mat != null) {
                mat.translate(img.bounds.getMinX(), img.bounds.getMinY());
            }
            if (img != null) {
                setImage(img);
            }
            return;
        }
        play();
    }

    public void setImage(SerializableImage image) {
        setBackground(View.swfBackgroundColor);
        if (timer != null) {
            timer.cancel();
        }
        drawable = null;
        loaded = true;
        ImageIcon icon = new ImageIcon(image.getBufferedImage());
        label.setIcon(icon);
    }

    @Override
    public int getCurrentFrame() {
        if (drawable == null) {
            return 0;
        }
        int ret = percent * (drawable.getNumFrames() - 1) / 100;
        return ret;
    }

    @Override
    public int getTotalFrames() {
        if (drawable == null) {
            return 0;
        }
        return drawable.getNumFrames();
    }

    @Override
    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void drawFrame() {
        if (drawable == null) {
            return;
        }
        int nframe = percent * (drawable.getNumFrames() - 1) / 100;
        if (nframe != frame) {
            Matrix mat = new Matrix();
            mat.translateX = swf.displayRect.Xmin;
            mat.translateY = swf.displayRect.Ymin;
            String key = "drawable_" + nframe + "_" + drawable.hashCode();
            SerializableImage img = SWF.getFromCache(key);
            if (img == null) {
                img = drawable.toImage(nframe, swf.tags, characters, new Stack<Integer>(), Matrix.getScaleInstance(1 / SWF.unitDivisor));
                SWF.putToCache(key, img);
            }
            mat.translate(img.bounds.getMinX(), img.bounds.getMinY());
            ImageIcon icon = new ImageIcon(img.getBufferedImage());
            label.setIcon(icon);
            frame = nframe;
        }
    }

    @Override
    public void play() {
        pause();
        if (drawable.getNumFrames() > 1) {

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    drawFrame();
                    if (percent == 100) {
                        percent = 0;
                    } else {
                        percent++;
                    }

                }
            }, 0, 20);
        }
    }

    @Override
    public void rewind() {
        percent = 0;
        drawFrame();
    }

    @Override
    public boolean isPlaying() {
        if (drawable == null) {
            return false;
        }
        return (drawable.getNumFrames() <= 1) || (timer != null);
    }

    @Override
    public void gotoFrame(int frame) {
        if (drawable == null) {
            percent = 0;
        } else {
            percent = frame * 100 / drawable.getNumFrames();
        }
        drawFrame();
    }

    @Override
    public int getFrameRate() {
        if (drawable == null) {
            return 1;
        }
        return drawable.getNumFrames() / 2;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }
}
