/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.javactivex.ActiveXEventListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class FlashPlayerPanel extends Panel implements Closeable, MediaDisplay {

    private ShockwaveFlash flash;

    private boolean stopped = true;
    private int frameRate;

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
    public double getZoom() {
        return 0;
    }

    private double zoom = 1.0;

    @Override
    public synchronized void zoom(double zoom) {
        int zoomint = (int) Math.round(100 / (zoom / this.zoom));
        if (zoom == 1.0) {
            zoomint = 0;
        }
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
        } catch (ActiveXException ex) { //Can be "Data not available yet exception"
            return 0;
        }
    }

    @Override
    public synchronized int getTotalFrames() {
        if (flash == null) {
            return 0;
        }
        if (flash.getReadyState() == 4) {
            try {
                return flash.getTotalFrames();
            } catch (ActiveXException ex) { //Can be "Data not available yet exception"
                return 0;
            }
        }
        return 0;
    }

    @Override
    public synchronized void setBackground(Color color) {
        try {
            flash.setBackgroundColor((color.getRed() << 16) + (color.getGreen() << 8) + color.getBlue());
        } catch (ActiveXException ex) {
            //ignore
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
        flash.setAllowNetworking("all");
        flash.addFlashCallListener(new ActiveXEventListener() {

            @Override
            public void onEvent(ActiveXEvent axe) {
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
            }
        });
    }

    public synchronized void stopSWF() {
        displaySWF("-", null, 1);
        stopped = true;
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

    public synchronized void displaySWF(String flashName, Color bgColor, int frameRate) {

        zoom = 1.0;
        this.frameRate = frameRate;
        if (bgColor != null) {
            setBackground(bgColor);
        }
        flash.setMovie(flashName);
        //play
        stopped = false;

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void pause() {
        try {
            flash.Stop();
        } catch (ActiveXException ex) {
            //ignore
        }
    }

    @Override
    public void rewind() {
        try {
            flash.Rewind();
        } catch (ActiveXException ex) {
            //ignore
        }
    }

    @Override
    public void play() {
        try {
            flash.Play();
        } catch (ActiveXException ex) {
            //ignore
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return flash.IsPlaying();
        } catch (ActiveXException ex) {
            return false;
        }
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
        } catch (ActiveXException ex) {
            //ignore
        }
    }

    @Override
    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public boolean isLoaded() {
        return !isStopped();
    }

}
