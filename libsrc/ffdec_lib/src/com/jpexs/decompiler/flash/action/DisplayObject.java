/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.ecma.Undefined;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display object class.
 *
 * @author JPEXS
 */
public class DisplayObject extends ActionScriptObject {

    /**
     * Display list.
     */
    protected Map<Integer, Object> displayList = new HashMap<>();

    /**
     * Total frames.
     */
    protected int totalFrames = 1;

    /**
     * Current frame.
     */
    protected int currentFrame = 1;

    /**
     * Paused.
     */
    protected boolean paused = false;

    /**
     * Dragging.
     */
    protected boolean dragging = false;

    /**
     * Starts drag.
     */
    public void startDrag() {
        dragging = true;
    }

    /**
     * Stops drag.
     */
    public void stopDrag() {
        dragging = false;
    }

    /**
     * Gets current frame.
     *
     * @return Current frame.
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Gets total frames.
     *
     * @return Total frames.
     */
    public int getTotalFrames() {
        return totalFrames;
    }

    /**
     * Goes to frame.
     *
     * @param label Label.
     */
    public void gotoLabel(String label) {
        //TODO
    }

    /**
     * Goes to frame.
     *
     * @param frame Frame.
     */
    public void gotoFrame(int frame) {
        if (frame < 1) {
            frame = 1;
        }
        if (frame > totalFrames) {
            frame = totalFrames;
        }
        this.currentFrame = frame;
    }

    /**
     * Pauses.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Plays.
     */
    public void play() {
        paused = false;
    }

    /**
     * Calls function.
     *
     * @param functionAddress Function address.
     * @param functionLength Function length.
     * @param args Arguments.
     * @param regNames Register names.
     * @param thisObj This object.
     * @return Result.
     */
    public Object callFunction(long functionAddress, long functionLength, List<Object> args, Map<Integer, String> regNames, Object thisObj) {
        //TODO
        return Undefined.INSTANCE;
    }

    /**
     * Calls frame.
     *
     * @param frame Frame.
     */
    public void callFrame(int frame) {
        //TODO
    }

    /**
     * Adds to display list.
     *
     * @param depth Depth.
     * @param obj Object.
     */
    public void addToDisplayList(int depth, Object obj) {
        displayList.put(depth, obj);
    }

    /**
     * Removes from display list.
     *
     * @param depth Depth.
     * @return Object
     */
    public Object removeFromDisplayList(int depth) {
        return displayList.remove(depth);
    }

    /**
     * Gets from display list.
     *
     * @param depth Depth.
     * @return Object
     */
    public Object getFromDisplayList(int depth) {
        return displayList.get(depth);
    }
}
