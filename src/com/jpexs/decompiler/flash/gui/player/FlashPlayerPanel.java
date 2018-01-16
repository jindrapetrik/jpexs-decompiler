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
package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.FlashUnsupportedException;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.javactivex.ActiveX;
import com.jpexs.javactivex.ActiveXEvent;
import com.jpexs.javactivex.ActiveXException;
import com.jpexs.javactivex.example.controls.flash.ShockwaveFlash;
import com.sun.jna.Platform;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public final class FlashPlayerPanel extends Panel implements Closeable, MediaDisplay {

    private static final Logger logger = Logger.getLogger(FlashPlayerPanel.class.getName());

    private final int setMovieDelay = Configuration.setMovieDelay.get();

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private final ShockwaveFlash flash;

    private final Timer timer;

    private boolean stopped = true;

    private boolean closed = false;

    private float frameRate;

    @Override
    public boolean loopAvailable() {
        return false;
    }

    @Override
    public boolean screenAvailable() {
        return true;
    }

    @Override
    public boolean zoomAvailable() {
        return true;
    }

    @Override
    public double getZoomToFit() {
        //TODO
        return 1;
    }

    @Override
    public Zoom getZoom() {
        return null;
    }

    private double zoom = 1.0;

    @Override
    public synchronized void zoom(Zoom zoom) {
        double zoomDouble = zoom.fit ? getZoomToFit() : zoom.value;
        int zoomint = (int) Math.round(100 / (zoomDouble / this.zoom));
        if (zoomint == 0) {
            zoomint = 1;
        }
        if (zoomint > 32767) {
            zoomint = 32767;
        }
        if (zoomint == 100) {
            zoomint = 0;
        }

        flash.Zoom(0); // hack, but this call is needed otherwise unzoom will fail for larger zoom values
        flash.Zoom(zoomint);
    }

    public synchronized String getVariable(String name) throws IOException {
        return flash.GetVariable(name);
    }

    public synchronized void setVariable(String name, String value) throws IOException {
        flash.SetVariable(name, value);
    }

    public synchronized String call(String callString) throws IOException {

        return flash.CallFunction(callString);
    }

    @Override
    public synchronized int getCurrentFrame() {
        if (flash == null) {
            return 0;
        }
        try {
            return flash.getFrameNum();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
        return 0;
    }

    @Override
    public synchronized int getTotalFrames() {
        if (flash == null) {
            return 0;
        }
        try {
            if (flash.getReadyState() == 4) {
                return flash.getTotalFrames();
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
        return 0;
    }

    @Override
    public synchronized void setBackground(Color color) {
        try {
            flash.setBackgroundColor((color.getRed() << 16) + (color.getGreen() << 8) + color.getBlue());
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    public FlashPlayerPanel(Component frame) {
        if (!Platform.isWindows()) {
            throw new FlashUnsupportedException();
        }

        try {
            Callable<ShockwaveFlash> callable = new Callable<ShockwaveFlash>() {
                @Override
                public ShockwaveFlash call() throws InterruptedException {
                    return ActiveX.createObject(ShockwaveFlash.class, FlashPlayerPanel.this);
                }
            };

            // hack: Kernel32.INSTANCE.ConnectNamedPipe never completes in ActiveXControl static constructor
            flash = CancellableWorker.call(callable, 5, TimeUnit.SECONDS);
        } catch (ActiveXException | TimeoutException | InterruptedException | ExecutionException ex) {
            logger.log(Level.WARNING, "Cannot initialize flash panel", ex);
            throw new FlashUnsupportedException();
        }

        flash.setAllowScriptAccess("always");
        try {
            flash.setAllowNetworking("all");
        } catch (ActiveXException ex) {
            // ignore
        }

        flash.addOnReadyStateChangeListener((ActiveXEvent axe) -> {
            fireMediaDisplayStateChanged();
        });

        flash.addFlashCallListener((ActiveXEvent axe) -> {
            String req = (String) axe.args.get("request");
            Matcher m = Pattern.compile("<invoke name=\"([^\"]+)\" returntype=\"xml\"><arguments><string>(.*)</string></arguments></invoke>").matcher(req);
            if (m.matches()) {
                String funname = m.group(1);
                String msg = m.group(2);
                if (funname.equals("alert") || funname.equals("console.log")) {
                    if (Main.debugDialog != null) {
                        Main.debugDialog.log(funname + ":" + msg);
                    }
                }
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            private boolean isPlaying = false;

            private int currentFrame = 0;

            @Override
            public void run() {
                if (closed) {
                    return;
                }
                try {
                    ShockwaveFlash flash1 = flash;

                    boolean changed = false;

                    if (flash1.getReadyState() >= 3) {
                        boolean isPlaying = flash1.IsPlaying();
                        if (this.isPlaying != isPlaying) {
                            this.isPlaying = isPlaying;
                        }

                        int currentFrame = flash1.CurrentFrame();
                        if (this.currentFrame != currentFrame) {
                            this.currentFrame = currentFrame;
                            changed = true;
                        }
                    } else {
                        this.isPlaying = false;
                    }

                    if (changed) {
                        fireMediaDisplayStateChanged();
                    }
                } catch (Exception ex) {
                    // ignore
                    cancel();
                }
            }
        }, 100, 100);
    }

    public synchronized void stopSWF() {
        displaySWF("-", null, 1);
        stopped = true;
        fireMediaDisplayStateChanged();
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    @Override
    public BufferedImage printScreen() {
        Point screenloc = getLocationOnScreen();
        try {
            return new Robot().createScreenCapture(new Rectangle(screenloc.x, screenloc.y, getWidth(), getHeight()));
        } catch (AWTException ex) {
            return null;
        }
    }

    private String movieToPlay = null;

    private Thread playQueue;

    private final Object queueLock = new Object();

    public synchronized void displaySWF(final String flashName, final Color bgColor, final float frameRate) {

        // Minimum of 1000 ms (setMovieDelay) delay before calling flash.setMovie to avoid illegalAccess errors
        if (playQueue == null) {
            playQueue = new Thread() {
                long lastTime;

                @Override
                public void run() {
                    while (true) {
                        boolean empty;
                        synchronized (queueLock) {
                            empty = movieToPlay == null;
                            if (empty) {
                                try {
                                    queueLock.wait();
                                } catch (InterruptedException ex) {
                                    break;
                                }
                            }
                        }
                        if (!empty) {
                            flash.setMovie(movieToPlay);
                            synchronized (queueLock) {
                                movieToPlay = null;
                            }
                            try {
                                Thread.sleep(setMovieDelay);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        }
                    }
                }
            };
            playQueue.start();
        }
        zoom = 1.0;
        this.frameRate = frameRate;
        if (bgColor != null) {
            setBackground(bgColor);
        }
        synchronized (queueLock) {
            movieToPlay = flashName;
            queueLock.notify();
        }

        //play
        stopped = false;
        fireMediaDisplayStateChanged();
    }

    @Override
    public synchronized void close() throws IOException {
        timer.cancel();
        closed = true;
    }

    @Override
    public void pause() {
        try {
            if (flash.getReadyState() >= 3) {
                flash.Stop();
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void stop() {
        try {
            if (flash.getReadyState() >= 3) {
                flash.Stop();
                flash.Rewind();
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void rewind() {
        try {
            if (flash.getReadyState() >= 3) {
                flash.Rewind();
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void play() {
        try {
            if (flash.getReadyState() >= 3) {
                flash.Play();
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            if (flash.getReadyState() >= 3) {
                return flash.IsPlaying();
            } else {
                return false;
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
            return false;
        }
    }

    @Override
    public void setLoop(boolean loop
    ) {
    }

    @Override
    public void gotoFrame(int frame
    ) {
        if (frame < 0) {
            return;
        }
        if (frame >= getTotalFrames()) {
            return;
        }
        try {
            if (flash.getReadyState() >= 3) {
                flash.GotoFrame(frame);
            }
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public float getFrameRate() {
        return frameRate;
    }

    @Override
    public boolean isLoaded() {
        return !isStopped();
    }

    public void fireMediaDisplayStateChanged() {
        for (MediaDisplayListener l : listeners) {
            l.mediaDisplayStateChanged(this);
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
}
