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
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.helpers.sound.MP3Player;
import com.jpexs.helpers.sound.SoundPlayer;
import com.jpexs.helpers.sound.WavPlayer;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;

/**
 *
 * @author JPEXS
 */
public class SoundTagPlayer implements MediaDisplay {

        private SoundPlayer player;

        private Thread thr;
        private int actualPos = 0;
        private boolean playing = false;
        private SoundTag tag;
        private byte data[];
        private List<PlayerListener> listeners=new ArrayList<>();
        
        public void addListener(PlayerListener l){
            listeners.add(l);
        }
        
        public void removeListener(PlayerListener l){
            listeners.remove(l);
        }

        public void fireFinished(){
            for(PlayerListener l:listeners){
                l.playingFinished();
            }
        }
        
        private static final int FRAME_DIVISOR = 1024;
        
        private int loops;
        
        public static boolean tagSupported(SoundTag tag){
            if(!Arrays.asList("mp3","wav").contains(tag.getExportFormat())){
               return false;
            }
            return true;
        }

        public SoundTagPlayer(SoundTag tag, int loops) {
            if(!tagSupported(tag)){
                throw new IllegalArgumentException("Format not supported");
            }
            this.tag = tag;
            this.loops = loops;
            try {
                this.data = ((Tag)tag).getSwf().exportSound((Tag)tag);
            } catch (IOException ex) {
                Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public synchronized int getCurrentFrame() {
            if (player == null) {
                return 0;
            }

            if (!playing) {
                return actualPos;
            }

            actualPos = (int) (player.getSamplePosition() / FRAME_DIVISOR);
            return actualPos;
        }

        @Override
        public synchronized int getTotalFrames() {
            //System.out.println("getTotalFrames");
            if (player == null) {
                return 0;
            }
            int ret = (int) (player.samplesCount() / FRAME_DIVISOR);

            //System.out.println("/getTotalFrames");
            return ret;
        }

        @Override
        public synchronized void pause() {
            if (!playing) {
                return;
            }
            playing = false;
            actualPos = (int) (player.getSamplePosition() / FRAME_DIVISOR);
            player.stop();

        }

        public void play(boolean async){
            if (player != null) {
                player.stop();
            }

            if (tag.getExportFormat().equals("mp3")) {
                final int soundRates[] = new int[]{5512, 11025, 22050, 44100};                
                try {
                    player = new MP3Player(new ByteArrayInputStream(data), (int) (tag.getTotalSoundSampleCount() * (tag.getSoundType() ? 2 : 1)), soundRates[tag.getSoundRate()] * (tag.getSoundType() ? 2 : 1));
                } catch (JavaLayerException ex) {
                    Logger.getLogger(SoundTagPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if(tag.getExportFormat().equals("wav")){
                player = new WavPlayer(new ByteArrayInputStream(data));
            }
            final int startPos = actualPos * FRAME_DIVISOR;
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    player.skip(startPos);
                    player.play();
                    fireFinished();
                    if(loops>0){
                        loops--;
                    }
                    if (playing && loops>0) {
                        gotoFrame(0);
                        play();
                    }
                }
            };
            playing = true;
            if(async){
                thr = new Thread(r);
                thr.start();
            }else{
                r.run();
            }
            
        }
        
        @Override
        public synchronized void play() {
            play(true);
        }

        @Override
        public void rewind() {
            actualPos = 0;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public synchronized void gotoFrame(int frame) {
            if (playing) {
                playing = false;
                player.stop();
            }
            actualPos = frame;
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
