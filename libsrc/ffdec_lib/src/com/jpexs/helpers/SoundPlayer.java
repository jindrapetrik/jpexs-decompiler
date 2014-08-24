/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.helpers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class SoundPlayer {

    private final Clip clip;

    public SoundPlayer(InputStream is) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
        clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(is)));
    }

    public long samplesCount() {
        return clip.getMicrosecondLength();
    }

    public void play() {

        final SoundPlayer t = this;
        clip.addLineListener(new LineListener() {

            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    //clip.close();

                    synchronized (t) {
                        t.notifyAll();
                    }
                }
            }
        });
        clip.start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException ex) {
            //Ignore
        }
    }

    public long getSamplePosition() {
        return clip.getMicrosecondPosition();
    }

    public void setPosition(long frames) {
        clip.setMicrosecondPosition(frames);
    }

    public void stop() {
        clip.stop();
    }

    public boolean isPlaying() {
        return clip.isActive();
    }

    public long getFrameRate() {
        return 1000000L;
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

}
