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
package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author JPEXS
 */
public class PlayerControls extends JPanel implements ActionListener {

    static final String ACTION_PAUSE = "PAUSE";
    static final String ACTION_STOP = "STOP";

    private final JButton pauseButton;
    private boolean paused = false;
    private final FlashDisplay display;
    private JProgressBar progress;
    private final Timer timer;
    private final JLabel timeLabel;
    private final JLabel totalTimeLabel;
    private static final Icon pauseIcon = View.getIcon("pause16");
    private static final Icon playIcon = View.getIcon("play16");

    public PlayerControls(final FlashDisplay display) {
        this.display = display;
        JPanel controlPanel = new JPanel(new BorderLayout());
        timeLabel = new JLabel("00:00.00");
        totalTimeLabel = new JLabel("00:00.00");
        controlPanel.add(timeLabel, BorderLayout.WEST);
        controlPanel.add(totalTimeLabel, BorderLayout.EAST);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        pauseButton = new JButton(AppStrings.translate("preview.pause"), pauseIcon);
        pauseButton.setMargin(new Insets(0, 0, 0, 0));
        pauseButton.setActionCommand(ACTION_PAUSE);
        pauseButton.addActionListener(this);
        JButton stopButton = new JButton(AppStrings.translate("preview.stop"), View.getIcon("stop16"));
        stopButton.setMargin(new Insets(0, 0, 0, 0));
        stopButton.setActionCommand(ACTION_STOP);
        stopButton.addActionListener(this);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);
        controlPanel.add(buttonsPanel, BorderLayout.CENTER);

        progress = new JProgressBar();
        Dimension pref = progress.getPreferredSize();
        pref.height = 20;
        progress.setPreferredSize(pref);
        progress.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int frame = (int) Math.floor(e.getX() * display.getTotalFrames() / (double) progress.getWidth());
                boolean p = paused;
                display.gotoFrame(frame);
                if (!p) {
                    display.play();
                }
            }
        });
        add(progress);
        add(controlPanel);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 100, 100);
    }

    private String formatMs(long ms) {
        long s = ms / 1000;
        ms %= 1000;
        long m = s / 60;
        s %= 60;
        long h = m / 60;
        m %= 60;
        return "" + (h > 0 ? h + ":" : "") + pad(m) + ":" + pad(s) + "." + pad(ms / 10);
    }

    private String pad(long t) {
        String ret = "" + t;
        while (ret.length() < 2) {
            ret = "0" + ret;
        }
        return ret;
    }

    private void update() {
        View.execInEventDispatch(new Runnable() {
            @Override
            public void run() {
                if (!display.isLoaded()) {
                    return;
                }
                int totalFrames = display.getTotalFrames();
                int currentFrame = display.getCurrentFrame();
                int frameRate = display.getFrameRate();
                if (totalFrames == 0) {
                    progress.setIndeterminate(true);
                } else {
                    progress.setMaximum(totalFrames - 1);
                    progress.setMinimum(0);
                    progress.setValue(currentFrame);
                    progress.setIndeterminate(false);
                }
                if (frameRate != 0) {
                    timeLabel.setText(formatMs((currentFrame * 1000) / frameRate));
                    totalTimeLabel.setText(formatMs(((totalFrames - 1) * 1000) / frameRate));
                }
                if (totalFrames <= 1 && isVisible()) {
                    setVisible(false);
                }
                if (totalFrames > 1 && !isVisible()) {
                    setVisible(true);
                }
                if (display.isPlaying() == paused) {
                    paused = !paused;

                    if (paused) {
                        pauseButton.setText(AppStrings.translate("preview.play"));
                        pauseButton.setIcon(playIcon);
                    } else {
                        pauseButton.setText(AppStrings.translate("preview.pause"));
                        pauseButton.setIcon(pauseIcon);
                    }
                }
            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_PAUSE:
                if (paused) {
                    display.play();
                } else {
                    display.pause();
                }
                break;
            case ACTION_STOP:
                display.pause();
                display.rewind();
                break;
        }
    }
}
