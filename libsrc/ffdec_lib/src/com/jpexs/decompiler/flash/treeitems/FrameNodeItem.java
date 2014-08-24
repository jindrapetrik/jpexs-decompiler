/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.timeline.Timelined;

/**
 *
 * @author JPEXS
 */
public class FrameNodeItem implements TreeItem {

    private final SWF swf;
    private final int frame;
    private final Timelined parent;
    private final boolean display;

    public FrameNodeItem(SWF swf, int frame, Timelined parent, boolean display) {

        this.swf = swf;
        this.frame = frame;
        this.parent = parent;
        this.display = display;
    }

    @Override
    public SWF getSwf() {
        return swf;
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

    public Timelined getParent() {
        return parent;
    }
}
