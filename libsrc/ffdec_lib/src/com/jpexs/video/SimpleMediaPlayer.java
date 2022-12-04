package com.jpexs.video;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.image.BufferedImage;
import java.lang.annotation.Native;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.sun.jna.NativeLibrary;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.provider.DirectoryProviderDiscoveryStrategy;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
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
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter;
import uk.co.caprica.vlcj.player.list.PlaybackMode;

public class SimpleMediaPlayer {

    private List<FrameListener> listeners = new ArrayList<>();
    private final EmbeddedMediaPlayer embeddedMediaPlayer;
    private final MediaListPlayer mediaListPlayer;
    private final MediaPlayerFactory mediaPlayerFactory;
    private boolean paused = true;

    private long time = 0L;

    private float position = 0f;

    private long length = 0L;

    private boolean positionSet = false;

    private boolean loaded = false;
    
    private boolean finished = false;

    private boolean singleFrame = false;

    private final Object displayLock = new Object();
    
    private String file;
    
    private MyRenderCallback callback;   
    
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
    }

    public void stop() {
        embeddedMediaPlayer.controls().stop();
    }

    public void pause() {
        if (paused) {
            paused = false;
            if (length != 0) {
                embeddedMediaPlayer.controls().setPosition(((float) time) / length);
            }
            embeddedMediaPlayer.controls().play();
            return;
        }
        embeddedMediaPlayer.controls().pause();
        paused = true;
    }

    public void setTime(long time) {
        synchronized (displayLock) {
            this.time = time;
            positionSet = false;
            if (paused) {
                singleFrame = true;
            }
            embeddedMediaPlayer.controls().setTime(time);
            embeddedMediaPlayer.controls().play();
            
            if (paused) {
                try {
                    displayLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimpleMediaPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //embeddedMediaPlayer.controls().play();
    }

    public void setPosition(float position) {
        synchronized (displayLock) {
            this.position = position;
            positionSet = true;
            singleFrame = true;
        }
            //System.out.println("setting position: "+ position);
            embeddedMediaPlayer.controls().pause();
            embeddedMediaPlayer.controls().setPosition(position);
            embeddedMediaPlayer.controls().play();
            /*if (paused) {
                try {
                    displayLock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SimpleMediaPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
        

        //embeddedMediaPlayer.controls().play();
    }

    public boolean isPaused() {
        return paused;
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
                return new RV32BufferFormat(sourceWidth, sourceHeight);
            }

            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {
            }
        };

        callback = new MyRenderCallback(listeners);
        CallbackVideoSurface callbackVideoSurface = new CallbackVideoSurface(bufferFormatCallback, callback,
                false, VideoSurfaceAdapters.getVideoSurfaceAdapter());
        
        NativeLibrary.addSearchPath("libvlc", Configuration.vlcPlayerLocation.get());
        mediaPlayerFactory = new MediaPlayerFactory();

        embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        callbackVideoSurface.attach(embeddedMediaPlayer);
        embeddedMediaPlayer.videoSurface().set(callbackVideoSurface);
        embeddedMediaPlayer.videoSurface().attachVideoSurface();                
                
        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void lengthChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newLength) {
                length = newLength;
            }

            @Override
            public void timeChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, long newTime) {
                SimpleMediaPlayer.this.time = newTime;
            }

            @Override
            public void positionChanged(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, float newPosition) {
                SimpleMediaPlayer.this.position = newPosition;
                //System.out.println("position changed to "+newPosition);
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {                                              
                /*System.out.println("finished");
                finished = true;
                callback.sendImage();
                new Thread() {
                    @Override
                    public void run() {
                        System.out.println("finished settime 0");
                        mediaPlayer.controls().setPosition(0f);
                        System.out.println("finished play");
                        mediaPlayer.controls().play();
                        System.out.println("/finished");
                    }                    
                }.start();*/
            }        
                        

            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                System.out.println("stopped");
            }
            
            
            

            @Override
            public void playing(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer) {
                if (!loaded) {
                    if (positionSet) {
                        embeddedMediaPlayer.controls().setPosition(position);
                    } else {
                        embeddedMediaPlayer.controls().setPosition(((float) time) / length);
                    }                    
                }
                //System.out.println("playing");
                finished = false;
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
                fl.newFrameRecieved(this.image);
            }
        }

        
        
        public MyRenderCallback(List<FrameListener> listeners) {
            this.videoSurfaces = listeners;
        }

        @Override
        public void display(uk.co.caprica.vlcj.player.base.MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {            
            synchronized (displayLock) {                
                if (singleFrame) {                    
                    singleFrame = false;
                    if (image == null) {
                        this.width = bufferFormat.getWidth();
                        this.height = bufferFormat.getHeight();
                        this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
                        rgbBuffer = new int[image.getWidth() * image.getHeight()];
                    }

                    nativeBuffers[0].asIntBuffer().get(rgbBuffer, 0, bufferFormat.getHeight() * bufferFormat.getWidth());
                    image.setRGB(0, 0, image.getWidth(), image.getHeight(), rgbBuffer, 0, image.getWidth());
                    
                                       
                }           
                
                if (!loaded) {
                    loaded = true;
                    //System.out.println("just loaded");
                    if (positionSet) {
                        embeddedMediaPlayer.controls().setPosition(position);
                    } else {
                        embeddedMediaPlayer.controls().setPosition(((float) time) / length);
                    }
                }                                
            }
            embeddedMediaPlayer.controls().pause();
            sendImage();
            //System.out.println("display return");
        }
    }
}
