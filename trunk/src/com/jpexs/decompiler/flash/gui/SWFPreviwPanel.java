/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.player.FlashDisplay;
import com.jpexs.decompiler.flash.gui.player.PlayerControls;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class SWFPreviwPanel extends JPanel implements FlashDisplay {

    ImagePanel pan;
    Timer timer;
    int frame = 1;
    List<BufferedImage> frameImages = new ArrayList<>();
    JLabel buffering = new JLabel(AppStrings.translate("work.buffering") + "...");

    public SWFPreviwPanel() {
        pan = new ImagePanel();
        pan.setBackground(View.swfBackgroundColor);
        setLayout(new BorderLayout());
        add(new JScrollPane(pan), BorderLayout.CENTER);
        buffering.setHorizontalAlignment(JLabel.CENTER);
        buffering.setVisible(false);
        JLabel prevLabel = new HeaderLabel(AppStrings.translate("swfpreview.internal"));
        prevLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //prevLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
        add(prevLabel, BorderLayout.NORTH);
        //add(buffering, BorderLayout.SOUTH);
        add(new PlayerControls(this), BorderLayout.SOUTH);
    }
    private SWF swf;

    public void load(final SWF swf) {
        this.swf = swf;
        new Thread() {
            @Override
            public void run() {
                buffering.setVisible(true);
                SWF.framesToImage(0, frameImages, 0, swf.frameCount - 1, swf.tags, swf.tags, swf.displayRect, swf.frameCount, new Stack<Integer>());
                buffering.setVisible(false);
            }
        }.start();


    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void play() {
        if (swf == null) {
            return;
        }
        if (swf.frameRate == 0) {
            return;
        }
        stop();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int newframe = (frame == swf.frameCount ? 1 : frame + 1);
                if (frameImages.size() >= newframe) {
                    //pan.setImage(frameImages.get(newframe - 1));
                    frame = newframe;
                    drawFrame();
                    pan.setBackground(View.swfBackgroundColor);
                }
            }
        }, 0, 1000 / swf.frameRate);
    }

    private void drawFrame() {
        pan.setImage(frameImages.get(frame - 1));
    }

    @Override
    public int getCurrentFrame() {
        return frame;
    }

    @Override
    public int getTotalFrames() {
        if (swf == null) {
            return 0;
        }
        return swf.frameCount;
    }

    @Override
    public void pause() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void rewind() {
        frame = 1;
        drawFrame();
    }

    @Override
    public boolean isPlaying() {
        return timer != null;
    }

    @Override
    public void gotoFrame(int frame) {
        this.frame = frame;
        drawFrame();
    }

    @Override
    public int getFrameRate() {
        if (swf == null) {
            return 1;
        }
        return swf.frameRate;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
