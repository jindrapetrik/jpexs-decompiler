/*
 *  Copyright (C) 2010-2015 JPEXS
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

import com.jpexs.decompiler.flash.gui.FlashUnsupportedException;
import com.jpexs.decompiler.flash.gui.Main;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public final class FlashPlayerPanel extends Panel implements Closeable, MediaDisplay {

    private final List<MediaDisplayListener> listeners = new ArrayList<>();

    private final ShockwaveFlash flash;

    private final Timer timer;

    private boolean stopped = true;

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
            flash = ActiveX.createObject(ShockwaveFlash.class, this);
        } catch (ActiveXException ex) {
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

                try {
                    ShockwaveFlash flash1 = flash;

                    boolean changed = false;

                    boolean isPlaying = flash1.IsPlaying();
                    if (this.isPlaying != isPlaying) {
                        this.isPlaying = isPlaying;
                    }

                    int currentFrame = flash1.CurrentFrame();
                    if (this.currentFrame != currentFrame) {
                        this.currentFrame = currentFrame;
                        changed = true;
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

    public synchronized void displaySWF(String flashName, Color bgColor, float frameRate) {

        zoom = 1.0;
        this.frameRate = frameRate;
        if (bgColor != null) {
            setBackground(bgColor);
        }
        flash.setMovie(flashName);
        //play
        stopped = false;
        fireMediaDisplayStateChanged();
    }

    @Override
    public void close() throws IOException {
        timer.cancel();
    }

    @Override
    public void pause() {
        try {
            flash.Stop();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void stop() {
        try {
            flash.Stop();
            flash.Rewind();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void rewind() {
        try {
            flash.Rewind();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public void play() {
        try {
            flash.Play();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return flash.IsPlaying();
        } catch (ActiveXException | NullPointerException ex) { // Can be "Data not available yet exception"
            return false;
        }
    }

    @Override
    public void setLoop(boolean loop) {
    }

    @Override
    public void gotoFrame(int frame) {
        if (frame < 0) {
            return;
        }
        if (frame >= getTotalFrames()) {
            return;
        }
        try {
            flash.GotoFrame(frame);
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
