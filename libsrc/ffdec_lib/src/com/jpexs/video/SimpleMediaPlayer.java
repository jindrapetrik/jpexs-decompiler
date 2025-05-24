/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.video;

import com.jpexs.decompiler.flash.jna.platform.win32.Advapi32Util;
import com.jpexs.decompiler.flash.jna.platform.win32.WinReg;
import com.jpexs.helpers.Helper;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.medialist.MediaListRef;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter;
import uk.co.caprica.vlcj.player.list.PlaybackMode;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

/**
 * Simple media player that uses VLCJ library to play video files.
 */
public class SimpleMediaPlayer {

    private List<FrameListener> listeners = new ArrayList<>();
    private final EmbeddedMediaPlayer embeddedMediaPlayer;
    private final MediaListPlayer mediaListPlayer;
    private final MediaPlayerFactory mediaPlayerFactory;
    private boolean paused = false;

    private long time = 0L;

    private float position = 0f;

    private long length = 0L;

    private boolean positionSet = false;

    private boolean loaded = false;

    private boolean finished = false;

    private boolean singleFrame = false;

    private final Object displayLock = new Object();

    private final Object pauseLock = new Object();

    private String file;

    private MyRenderCallback callback;

    private static boolean available = true;

    static {
        if (Platform.isWindows()) {
            boolean needs64bit = Helper.is64BitJre();
            final String VLC_REGISTRY_KEY = "SOFTWARE\\VideoLAN\\VLC";
            if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE, VLC_REGISTRY_KEY, needs64bit)) {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, VLC_REGISTRY_KEY, "InstallDir", needs64bit)) {
                    String vlcInstallDir = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, VLC_REGISTRY_KEY, "InstallDir", needs64bit);
                    NativeLibrary.addSearchPath("libvlc", vlcInstallDir);
                } else {
                    available = false;
                }
            } else {
                available = false;
            }
        }

        if (available) {
            try {
                LibVlcVersion version = new LibVlcVersion();
                if (!version.isSupported()) {
                    available = false;
                }
            } catch (UnsatisfiedLinkError err) {
                available = false;
            }
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public long getLength() {
        return length;
    }

    public void addFrameListener(FrameListener listener) {
        listeners.add(listener);
    }

    public void removeFrameListener(FrameListener listener) {
        listeners.remove(listener);
    }

    public void play(String file) {
        loaded = false;
        this.file = file;
        //embeddedMediaPlayer.media().play(file); //.play(file);

        MediaList mediaList = mediaPlayerFactory.media().newMediaList();
        mediaList.media().add(file);

        MediaListRef mediaListRef = mediaList.newMediaListRef();
        try {
            mediaListPlayer.list().setMediaList(mediaListRef);
        } finally {
            mediaListRef.release();
        }
        mediaListPlayer.controls().play();
        embeddedMediaPlayer.controls().setPause(true);
    }

    public void stop() {
        embeddedMediaPlayer.controls().stop();
    }

    public float getPosition() {
        return embeddedMediaPlayer.status().position();
    }

    public void setPosition(float position) {

        //System.out.println("setting position: "+ position);
        if (!isPaused()) {
            synchronized (pauseLock) {
                embeddedMediaPlayer.controls().setPause(true);
                try {
                    pauseLock.wait();
                } catch (InterruptedException ex) {
                    //Logger.getLogger(SimpleMediaPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //embeddedMediaPlayer.controls().setPosition(0);
        embeddedMediaPlayer.controls().setPosition(position);
        embeddedMediaPlayer.controls().play();

        /*synchronized (pauseLock) {
            embeddedMediaPlayer.controls().setPause(true);
            try {
                pauseLock.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(SimpleMediaPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
        //setPaused(false);        
        /*synchronized (displayLock) {
            this.position = position;
            positionSet = true;
            singleFrame = true;
        }*/
        //embeddedMediaPlayer.controls().play();
        //embeddedMediaPlayer.controls().setPause(true);
        //embeddedMediaPlayer.controls().nextFrame();
        //embeddedMediaPlayer.controls().play();
        /*if (paused) {
                try {
                    displayLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimpleMediaPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
        //embeddedMediaPlayer.controls().play();
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void setPaused(boolean val) {
        this.paused = val;
    }

    public void pause() {
        embeddedMediaPlayer.controls().pause();
    }

    /*public void rewind() {
        System.out.println("rewinding");
        //embeddedMediaPlayer.controls().stop();
        //embeddedMediaPlayer.controls().start();        
        position = 0f;
        loaded = false;
        embeddedMediaPlayer.media().play(file);
        //embeddedMediaPlayer.controls().setPosition(0f);
        //embeddedMediaPlayer.controls().play();
        System.out.println("rewound");
    }*/
    public SimpleMediaPlayer() {

        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {

            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                //return new RV32BufferFormat(sourceWidth, sourceHeight);
                return new BufferFormat("BGRA", sourceWidth, sourceHeight, new int[]{sourceWidth * 4}, new int[]{sourceHeight});
            }

            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {
            }
        };

        callback = new MyRenderCallback(listeners);
        CallbackVideoSurface callbackVideoSurface = new CallbackVideoSurface(bufferFormatCallback, callback,
                false, VideoSurfaceAdapters.getVideoSurfaceAdapter());

        mediaPlayerFactory = new MediaPlayerFactory();

        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        callbackVideoSurface.attach(embeddedMediaPlayer);
        embeddedMediaPlayer.videoSurface().set(callbackVideoSurface);
        embeddedMediaPlayer.videoSurface().attachVideoSurface();

        MediaPlayerEventAdapter adapter = (new MediaPlayerEventAdapter() {

            @Override
            public void lengthChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newLength) {
                //System.out.println("lengthChanged = "+newLength);
                length = newLength;
            }

            @Override
            public void paused(MediaPlayer mediaPlayer) {
                setPaused(true);
                synchronized (pauseLock) {
                    pauseLock.notifyAll();
                }
                //System.out.println("paused");
            }

            @Override
            public void playing(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                /*if (!loaded) {
                    if (positionSet) {
                        embeddedMediaPlayer.controls().setPosition(position);
                    } else {
                        embeddedMediaPlayer.controls().setPosition(((float) time) / length);
                    }
                }*/
                finished = false;
                setPaused(false);
                //System.out.println("playing");
                //embeddedMediaPlayer.controls().setRepeat(true);
            }

        });

        //embeddedMediaPlayer.controls().setRepeat(true);
        mediaListPlayer = mediaPlayerFactory.mediaPlayers().newMediaListPlayer();

        mediaListPlayer.events().addMediaListPlayerEventListener(new MediaListPlayerEventAdapter() {
            @Override
            public void nextItem(MediaListPlayer mediaListPlayer, MediaRef item) {
                //System.out.println("nextItem()");
            }
        });

        mediaListPlayer.mediaPlayer().setMediaPlayer(embeddedMediaPlayer);

        mediaListPlayer.controls().setMode(PlaybackMode.LOOP);

        embeddedMediaPlayer.events().addMediaPlayerEventListener(adapter);

    }

    public boolean isFinished() {
        return finished;
    }

    private class MyRenderCallback implements RenderCallback {

        private List<FrameListener> videoSurfaces;
        private int width;
        private int height;
        private BufferedImage image;
        private int[] rgbBuffer;

        public BufferedImage getImage() {
            return image;
        }

        public void sendImage() {
            for (FrameListener fl : videoSurfaces) {
                fl.newFrameReceived(this.image);
            }
        }

        public MyRenderCallback(List<FrameListener> listeners) {
            this.videoSurfaces = listeners;
        }

        @Override
        public void display(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            if (!isPaused()) {
                //embeddedMediaPlayer.controls().setPause(true);
                /*mediaPlayer.submit(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.controls().setPause(true);
                    }
                    
                });*/
            }
            synchronized (displayLock) {
                if (true) { //singleFrame) {
                    singleFrame = false;
                    if (image == null) {
                        this.width = bufferFormat.getWidth();
                        this.height = bufferFormat.getHeight();
                        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
                        rgbBuffer = new int[image.getWidth() * image.getHeight()];
                    }

                    nativeBuffers[0].asIntBuffer().get(rgbBuffer, 0, bufferFormat.getHeight() * bufferFormat.getWidth());
                    image.setRGB(0, 0, image.getWidth(), image.getHeight(), rgbBuffer, 0, image.getWidth());
                    sendImage();
                } else {
                    System.out.println("skipped frame");
                }

                /*if (!loaded) {
                    loaded = true;
                    //System.out.println("just loaded");
                    if (positionSet) {
                        embeddedMediaPlayer.controls().setPosition(position);
                    } else {
                        embeddedMediaPlayer.controls().setPosition(((float) time) / length);
                    }
                }*/
            }

            //System.out.println("display return");
        }
    }
}
