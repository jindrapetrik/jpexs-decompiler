/*
 * Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.helpers.sound;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class WavPlayer extends SoundPlayer {

    private Clip clip;
    private boolean complete = false;
    private long startPos = 0;

    public WavPlayer(InputStream is) {
        super(is);
        try {
            clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
        } catch (LineUnavailableException ex) {
            Logger.getLogger(WavPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (clip == null) {
            return;
        }
        try {
            clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(is)));
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            Logger.getLogger(WavPlayer.class.getName()).log(Level.SEVERE, "Error opening", ex);
            clip = null;
        }
    }

    @Override
    public long samplesCount() {
        return clip.getMicrosecondLength();
    }

    @Override
    public void play() {
        if (clip == null) {
            return;
        }
        clip.setMicrosecondPosition(startPos);//startPos);  
        final WavPlayer t = this;
        clip.addLineListener(new LineListener() {

            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    complete = true;
                    startPos = 0;
                    clip.close();
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

    @Override
    public long getSamplePosition() {
        return clip.getMicrosecondPosition();
    }

    @Override
    public void skip(long frames) {
        startPos = getSamplePosition() + frames;
    }

    @Override
    public void stop() {
        clip.stop();
    }

    @Override
    public boolean isPlaying() {
        return !complete;
    }

    @Override
    public long getFrameRate() {
        return 1000000L;
    }

}
