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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;

/**
 *
 * @author JPEXS
 */
public class FrameScript implements TreeItem, Exportable {

    private final SWF swf;

    private final Frame frame;

    public FrameScript(SWF swf, Frame frame) {
        this.swf = swf;
        this.frame = frame;
    }

    public Frame getFrame() {
        return frame;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public String toString() {
        return frame.toString();
    }

    @Override
    public String getExportFileName() {
        return frame.getExportFileName();
    }

    @Override
    public boolean isModified() {
        return frame.isModified();
    }
}
