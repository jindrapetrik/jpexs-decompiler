package com.jpexs.decompiler.flash.action;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class DisplayObject extends ActionScriptObject {

    protected Map<Integer, Object> displayList = new HashMap<>();

    protected int totalFrames;
    protected int currentFrame;
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
