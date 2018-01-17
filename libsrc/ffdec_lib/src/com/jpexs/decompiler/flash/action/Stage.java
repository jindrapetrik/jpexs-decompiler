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

import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Stage extends DisplayObject {

    protected long startTime;

    protected Timelined timelined;

    protected Timeline timeline;

    protected Frame frame;

    public Stage(Timelined timelined) {
        startTime = System.currentTimeMillis();
        this.timelined = timelined;
        this.timeline = timelined != null ? timelined.getTimeline() : null;
        this.frame = timelined != null && this.timeline.getFrameCount() > 0 ? this.timeline.getFrame(0) : null;
    }

    @Override
    public List<String> enumerate() {
        List<String> ret = new ArrayList<>();
        if (frame != null) {
            for (DepthState ds : frame.layers.values()) {
                if (ds.instanceName != null) {
                    ret.add(ds.instanceName);
                }
            }
        }
        return ret;
    }

    @Override
    protected Object getThisMember(String name) {
        if (frame != null) {
            for (DepthState ds : frame.layers.values()) {
                if (name.equals(ds.instanceName)) {
                    return new DepthStateObjectAdapter(ds);
                }
            }
        }
        return null;
    }

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public int getTotalFrames() {
        if (timeline == null) {
            return 1;
        }
        return timeline.getFrameCount();
    }

    @Override
    public void gotoFrame(int frameNum) {
        super.gotoFrame(frameNum);
        if (timeline != null) {
            this.frame = timeline.getFrame(getCurrentFrame() - 1);
        }
    }

    @Override
    public void gotoLabel(String label) {
        if (timeline == null) {
            return;
        }
        int f = timeline.getFrameWithLabel(label);
        if (f != -1) {
            gotoFrame(f + 1);
        }
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
