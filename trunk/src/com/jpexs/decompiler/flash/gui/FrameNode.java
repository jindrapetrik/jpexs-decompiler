package com.jpexs.decompiler.flash.gui;

/**
 *
 * @author JPEXS
 */
public class FrameNode {

    private int frame;
    private Object parent;
    private boolean display;

    public FrameNode(int frame, Object parent, boolean display) {
        this.frame = frame;
        this.parent = parent;
        this.display = display;
    }

    public boolean isDisplayed() {
        return display;
    }

    @Override
    public String toString() {
        return "frame " + frame;
    }

    public int getFrame() {
        return frame;
    }

    public Object getParent() {
        return parent;
    }
}
