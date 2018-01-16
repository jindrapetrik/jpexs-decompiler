/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class DisplayObject extends ActionScriptObject {

    protected Map<Integer, Object> displayList = new HashMap<>();

    protected int totalFrames = 1;

    protected int currentFrame = 1;

    protected boolean paused = false;

    protected boolean dragging = false;

    public void startDrag() {
        dragging = true;
    }

    public void stopDrag() {
        dragging = false;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void gotoLabel(String label) {
        //TODO
    }

    public void gotoFrame(int frame) {
        if (frame < 1) {
            frame = 1;
        }
        if (frame > totalFrames) {
            frame = totalFrames;
        }
        this.currentFrame = frame;
    }

    public void pause() {
        paused = true;
    }

    public void play() {
        paused = false;
    }

    public Object callFunction(long functionAddress, long functionLength, List<Object> args, Map<Integer, String> regNames, Object thisObj) {
        //TODO
        return Undefined.INSTANCE;
    }

    public void callFrame(int frame) {
        //TODO
    }

    public void addToDisplayList(int depth, Object obj) {
        displayList.put(depth, obj);
    }

    public Object removeFromDisplayList(int depth) {
        return displayList.remove(depth);
    }

    public Object getFromDisplayList(int depth) {
        return displayList.get(depth);
    }
}
