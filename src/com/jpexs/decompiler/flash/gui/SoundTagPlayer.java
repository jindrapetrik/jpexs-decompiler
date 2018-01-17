/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
import com.jpexs.decompiler.flash.gui.player.MediaDisplayListener;
import com.jpexs.decompiler.flash.gui.player.Zoom;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private boolean paused = false;

    private final Object playLock = new Object();

    private final SoundTag tag;

    private final Timer timer;

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private boolean rewindAfterStop = false;

    @Override
    public void addEventListener(MediaDisplayListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(MediaDisplayListener listener) {
        listeners.remove(listener);
    }

    public void fireMediaDisplayStateChanged() {
        for (MediaDisplayListener l : listeners) {
            l.mediaDisplayStateChanged(this);
        }
    }

    private void firePlayingFinished() {
        for (MediaDisplayListener l : listeners) {
            l.playingFinished(this);
        }
    }

    private static final int FRAME_DIVISOR = 8000;

    public SoundTagPlayer(final SoundTag tag, int loops, boolean async) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.tag = tag;
        this.loopCount = loops;
        clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    synchronized (playLock) {
                        if (!paused) {
                            decreaseLoopCount();

                            if (loopCount > 0) {
                                clip.setFramePosition(0);
                                clip.start();
                            } else {
                                firePlayingFinished();
                            }
                        }
                    }

                    if (rewindAfterStop) {
                        rewind();
                        rewindAfterStop = false;
                    }
                }

                fireMediaDisplayStateChanged();
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    fireMediaDisplayStateChanged();
                } catch (Exception ex) {
                    // ignore
                    cancel();
                }
            }
        }, 100, 100);

        if (!async) {
            paused = true;
            openSound(tag);
        } else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        openSound(tag);
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                        Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    synchronized (playLock) {
                        if (!paused) {
                            play();
                        }
                    }
                }
            }.start();
        }
    }

    private void openSound(SoundTag tag) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        SWF swf = ((Tag) tag).getSwf();
        byte[] wavData = swf.getFromCache(tag);
        if (wavData == null) {
            List<ByteArrayRange> soundData = tag.getRawSoundData();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tag.getSoundFormat().createWav(soundData, baos);
            wavData = baos.toByteArray();
            swf.putToCache(tag, wavData);
        }

        synchronized (playLock) {
            clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavData)));
        }
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
    public void stop() {
        rewindAfterStop = true;
        pause();
        rewind();
    }

    @Override
    public void close() {
        stop();
        timer.cancel();
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

        fireMediaDisplayStateChanged();
    }

    @Override
    public void rewind() {
        gotoFrame(0);
    }

    @Override
    public boolean isPlaying() {
        synchronized (playLock) {
            return clip.isActive();
        }
    }

    @Override
    public boolean loopAvailable() {
        return true;
    }

    @Override
    public boolean screenAvailable() {
        return false;
    }

    @Override
    public void zoom(Zoom zoom) {
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
    public Zoom getZoom() {
        return null;
    }

    @Override
    public void setLoop(boolean loop) {
        loopCount = loop ? Integer.MAX_VALUE : 1;
    }

    @Override
    public void gotoFrame(int frame) {
        synchronized (playLock) {
            clip.setMicrosecondPosition((long) frame * FRAME_DIVISOR);
        }

        fireMediaDisplayStateChanged();
    }

    @Override
    public void setBackground(Color color) {

    }

    @Override
    public float getFrameRate() {
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
            timer.cancel();

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
