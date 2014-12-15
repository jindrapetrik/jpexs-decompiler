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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author JPEXS
 */
public class SoundTagPlayer implements MediaDisplay {

    private final Clip clip;

    private int loopCount;
    private boolean paused = true;
    private final Object playLock = new Object();
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

    public SoundTagPlayer(SoundTag tag, int loops) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.tag = tag;
        this.loopCount = loops;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<byte[]> soundData = tag.getRawSoundData();
        SWF swf = ((Tag) tag).getSwf();
        List<SWFInputStream> siss = new ArrayList<>();
        for (byte[] data : soundData) {
            siss.add(new SWFInputStream(swf, data));
        }
        tag.getSoundFormat().createWav(siss, baos);
        clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
        clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(baos.toByteArray())));

        clip.addLineListener(new LineListener() {

            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    //clip.close();
                    synchronized (playLock) {
                        if (!paused) {
                            decreaseLoopCount();
                
                            if (loopCount > 0) {
                                clip.setFramePosition(0);
                                clip.start();
                            } else {
                                fireFinished();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getCurrentFrame() {

        synchronized (playLock) {
            return (int) (clip.getMicrosecondPosition() / FRAME_DIVISOR);
        }
    }

    @Override
    public int getTotalFrames() {

        synchronized (playLock) {
            return (int) (clip.getMicrosecondLength() / FRAME_DIVISOR);
        }
    }

    @Override
    public void pause() {

        synchronized (playLock) {
            paused = true;
            clip.stop();
        }
    }

    @Override
    public void play() {
        synchronized (playLock) {
            paused = false;
            if (!clip.isActive()) {
                if (clip.getMicrosecondLength() == clip.getMicrosecondPosition()) {
                    decreaseLoopCount();
                    clip.setFramePosition(0);
                }
                
                clip.start();
            }
        }
    }

    @Override
    public void rewind() {
        gotoFrame(0);
    }

    @Override
    public boolean isPlaying() {
        return clip.isActive();
    }

    @Override
    public boolean screenAvailable() {
        return false;
    }

    @Override
    public void zoom(double zoom) {

    }

    @Override
    public boolean zoomAvailable() {
        return false;
    }

    @Override
    public double getZoomToFit() {
        return 1;
    }

    @Override
    public double getZoom() {
        return 0;
    }

    @Override
    public void gotoFrame(int frame) {
        synchronized (playLock) {
            boolean active = clip.isActive();
            if (active) {
                clip.stop();
            }
            clip.setMicrosecondPosition(frame * FRAME_DIVISOR);
            
            if (active) {
                clip.start();
            }
        }
    }

    @Override
    public void setBackground(Color color) {

    }

    @Override
    public int getFrameRate() {
        return (int) (1000000L / FRAME_DIVISOR);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public BufferedImage printScreen() {
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (clip != null) {
                clip.close();
            }
        } finally {
            super.finalize();
        }
    }
    
    private void decreaseLoopCount() {
        // this method should be called from synchronized (playLock) block
        if (loopCount > 0 && loopCount != Integer.MAX_VALUE) {
            loopCount--;
        }
    }
}
