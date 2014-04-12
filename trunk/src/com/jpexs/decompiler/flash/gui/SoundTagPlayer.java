/*
 * Copyright (C) 2014 JPEXS
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

import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.helpers.SoundPlayer;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author JPEXS
 */
public class SoundTagPlayer implements MediaDisplay {

    private final SoundPlayer player;

    private Thread thr;
    private int actualPos = 0;
    private final SoundTag tag;
    private final List<PlayerListener> listeners = new ArrayList<>();

    public void addListener(PlayerListener l) {
        listeners.add(l);
    }

    public void removeListener(PlayerListener l) {
        listeners.remove(l);
    }

    public void fireFinished() {
        for (PlayerListener l : listeners) {
            l.playingFinished();
        }
    }

    private static final int FRAME_DIVISOR = 8000;

    private int loops;
    private boolean paused = true;
    private final Object playLock = new Object();

    public SoundTagPlayer(SoundTag tag, int loops) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.tag = tag;
        this.loops = loops;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(tag.getRawSoundData());
        tag.getSoundFormat().createWav(bais, baos);
        player = new SoundPlayer(new ByteArrayInputStream(baos.toByteArray()));
    }

    @Override
    public synchronized int getCurrentFrame() {

        synchronized (playLock) {
            if (isPlaying()) {
                actualPos = (int) (player.getSamplePosition() / FRAME_DIVISOR);
            }
            return actualPos;
        }
    }

    @Override
    public synchronized int getTotalFrames() {

        int ret = (int) (player.samplesCount() / FRAME_DIVISOR);
        return ret;
    }

    @Override
    public synchronized void pause() {
        if (!isPlaying()) {
            paused = true;
            return;
        }

        synchronized (playLock) {
            actualPos = (int) (player.getSamplePosition() / FRAME_DIVISOR);
        }

        waitStop();
    }

    private void waitStop() {
        synchronized (playLock) {
            if (!paused) {
                paused = true;
                player.stop();
                try {
                    playLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void play(boolean async) {

        waitStop();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                boolean playAgain = true;
                while (playAgain) {
                    int startPos = 0;
                    synchronized (playLock) {
                        startPos = actualPos * FRAME_DIVISOR;
                    }
                    player.setPosition(startPos);
                    player.play();

                    synchronized (playLock) {
                        playAgain = !paused && loops > 0;
                        if (!paused) {
                            if (loops == 0) {
                                paused = true;
                            } else if (loops != Integer.MAX_VALUE) {
                                loops--;
                            }
                        }
                        if (playAgain) {
                            actualPos = 0;
                        }
                    }
                }

                fireFinished();
                synchronized (playLock) {
                    playLock.notifyAll();
                }
            }
        };
        synchronized (playLock) {
            paused = false;
        }
        if (async) {
            thr = new Thread(r);
            thr.start();
        } else {
            r.run();
        }

    }

    @Override
    public synchronized void play() {
        play(true);
    }

    @Override
    public void rewind() {
        gotoFrame(0);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public synchronized void gotoFrame(int frame) {
        pause();
        synchronized (playLock) {
            actualPos = frame;
        }
    }

    @Override
    public void setBackground(Color color) {

    }

    @Override
    public int getFrameRate() {
        if (player == null) {
            return 1;
        }
        return (int) (player.getFrameRate() / FRAME_DIVISOR);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

}
