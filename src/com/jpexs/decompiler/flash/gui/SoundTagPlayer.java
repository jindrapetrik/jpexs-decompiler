/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.decompiler.flash.types.SOUNDINFO;
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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author JPEXS
 */
public class SoundTagPlayer implements MediaDisplay {

    private int loopCount;

    private boolean paused = false;

    private boolean closed = false;

    private final Object playLock = new Object();

    private final SoundTag tag;

    private final Timer timer;

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private boolean rewindAfterStop = false;

    private AudioInputStream audioStream;

    private SourceDataLine sourceLine;

    private Thread thread;

    private long lengthInMicroSec = 0;

    private double microsecPerByte = 0;

    private double positionMicrosec = 0;

    private Long newPositionMicrosec = null;

    private byte[] wavData = null;

    private boolean active = false;

    private boolean getActiveFlag() {
        synchronized (playLock) {
            return active;
        }
    }

    private void setActiveFlag(boolean value) {
        synchronized (playLock) {
            active = value;
        }
    }

    private boolean getPausedFlag() {
        synchronized (playLock) {
            return paused;
        }
    }

    private void setPausedFlag(boolean value) {
        synchronized (playLock) {
            paused = value;
        }
    }

    private boolean getClosedFlag() {
        synchronized (playLock) {
            return closed;
        }
    }

    private void setClosedFlag(boolean value) {
        synchronized (playLock) {
            closed = value;
        }
    }

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

    public SoundTagPlayer(final SOUNDINFO soundInfo, final SoundTag tag, int loops, boolean async) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.tag = tag;
        this.loopCount = loops;

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
            setPausedFlag(true);
        }

        thread = new Thread("Sound tag player") {
            @Override
            public void run() {
                try {
                    openSound(soundInfo, tag);
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
                synchronized (playLock) {
                    if (!paused) {
                        play();
                    }
                }
                this.setPriority(MIN_PRIORITY);
                playLoop();
            }
        };
        thread.start();
    }

    private void openSound(SOUNDINFO soundInfo, SoundTag tag) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        SWF swf = ((Tag) tag).getSwf();
        wavData = swf.getFromCache(soundInfo, tag);
        if (wavData == null) {
            List<ByteArrayRange> soundData = tag.getRawSoundData();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            tag.getSoundFormat().createWav(soundInfo, soundData, baos);
            wavData = baos.toByteArray();
            swf.putToCache(soundInfo, tag, wavData);
        }

        long soundLength44 = 0;
        int sampleLen = 2 * (tag.getSoundFormat().stereo ? 2 : 1);
        switch (tag.getSoundRate()) {
            case 0: //5.5kHz
                soundLength44 = 8 * tag.getTotalSoundSampleCount();
                microsecPerByte = 1000000d / (5512.5d * sampleLen);
                break;
            case 1: //11kHz
                soundLength44 = 4 * tag.getTotalSoundSampleCount();
                microsecPerByte = 1000000d / (11025d * sampleLen);
                break;
            case 2: //22kHz
                soundLength44 = 2 * tag.getTotalSoundSampleCount();
                microsecPerByte = 1000000d / (22050d * sampleLen);
                break;
            case 3: //44kHz
                soundLength44 = tag.getTotalSoundSampleCount();
                microsecPerByte = 1000000d / (44100d * sampleLen);
                break;
        }
        lengthInMicroSec = soundLength44 * 1000000 / 44100;

        synchronized (playLock) {
            audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavData));
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioStream.getFormat());
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioStream.getFormat());
            sourceLine.start();
        }
    }

    @Override
    public int getCurrentFrame() {

        synchronized (playLock) {
            return (int) (positionMicrosec / FRAME_DIVISOR);
        }
    }

    @Override
    public int getTotalFrames() {
        synchronized (playLock) {
            return (int) (lengthInMicroSec / FRAME_DIVISOR);
        }
    }

    @Override
    public void pause() {
        setPausedFlag(true);
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
        synchronized (playLock) {
            closed = true;
        }
    }

    private void reloadAudioStream() throws IOException, UnsupportedAudioFileException {
        audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavData));
    }

    private void playLoop() {

        loop: while (true) {

            final byte[] data = new byte[16];

            setPausedFlag(false);
            setActiveFlag(true);
            long posBytes = 0;
            try {
                int numReadBytes = 0;
                while (numReadBytes != -1) {

                    if (getClosedFlag()) {
                        break;
                    }
                    if (!getPausedFlag()) {
                        if (newPositionMicrosec != null) {
                            long newPosBytes = (long) (newPositionMicrosec / microsecPerByte);
                            audioStream.close();
                            reloadAudioStream();
                            audioStream.skip(newPosBytes);
                            newPositionMicrosec = null;
                            posBytes = newPosBytes;
                        }

                        numReadBytes = audioStream.read(data, 0, data.length);
                        if (numReadBytes != -1) {
                            posBytes += numReadBytes;
                            if (sourceLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                                //((FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN)).setValue(volume_dB);
                            }
                            sourceLine.write(data, 0, numReadBytes);
                            synchronized (playLock) {
                                positionMicrosec = microsecPerByte * posBytes;
                            }
                        }
                    }

                    if (getPausedFlag()) {
                        synchronized (thread) {
                            try {
                                thread.wait(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }

                if (getClosedFlag()) {
                    sourceLine.drain();
                    sourceLine.stop();
                    sourceLine.close();
                }
                audioStream.close();
            } catch (IOException ex) {
                Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedAudioFileException ex) {
                Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!getClosedFlag()) {
                decreaseLoopCount();

                int currentLoopCount;
                synchronized (playLock) {
                    currentLoopCount = loopCount;
                }

                if (currentLoopCount > 0) {
                    try {
                        reloadAudioStream();
                        continue loop;
                    } catch (IOException ex) {
                        Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedAudioFileException ex) {
                        Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    setActiveFlag(false);
                    firePlayingFinished();

                    if (getClosedFlag()) {
                        return;
                    }
                    synchronized (thread) {
                        try {
                            thread.wait();
                        } catch (InterruptedException ex) {
                            return;
                        }
                    }

                    if (getClosedFlag()) {
                        return;
                    }
                    setActiveFlag(true);

                    try {
                        reloadAudioStream();
                        continue loop;
                    } catch (IOException ex) {
                        Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedAudioFileException ex) {
                        Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            break;
        }
    }

    @Override
    public void play() {
        setPausedFlag(false);
        synchronized (thread) {
            thread.notifyAll();
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
            return active && !paused && !closed;
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
        synchronized (playLock) {
            loopCount = loop ? Integer.MAX_VALUE : 1;
        }
    }

    @Override
    public void gotoFrame(int frame) {
        synchronized (playLock) {
            newPositionMicrosec = (long) (frame * FRAME_DIVISOR);
            positionMicrosec = newPositionMicrosec;
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

    /*
    Finalize is deprecated, let's see how this will work without it...
    
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
    }*/
    private void decreaseLoopCount() {
        synchronized (playLock) {
            if (loopCount > 0 && loopCount != Integer.MAX_VALUE) {
                loopCount--;
            }
        }
    }

    @Override
    public Color getBackgroundColor() {
        return Color.white;
    }
}
