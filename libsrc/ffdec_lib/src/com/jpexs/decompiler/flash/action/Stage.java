package com.jpexs.decompiler.flash.action;

/**
 *
 * @author JPEXS
 */
public class Stage extends DisplayObject {

    protected long startTime;

    public Stage() {
        startTime = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void stopSounds() {

    }

    public void toggleQuality() {

    }

    public void getURL(String url, String target) {

    }

    public void trace(Object... val) {

    }
}
