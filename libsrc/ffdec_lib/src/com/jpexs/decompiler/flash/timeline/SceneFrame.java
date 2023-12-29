/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class SceneFrame implements TreeItem {
    private final SWF swf;
    private final Scene scene;
    private final int realFrameIndex;

    public SceneFrame(SWF swf, Scene scene, int realFrameIndex) {
        this.swf = swf;
        this.scene = scene;
        this.realFrameIndex = realFrameIndex;
    }            
    
    public int getSceneFrameIndex() {
        return realFrameIndex - scene.startFrame;
    }
    
    public Frame getFrame() {
        return swf.getTimeline().getFrame(realFrameIndex);
    }

    @Override
    public String toString() {
        return "scene frame " + (getSceneFrameIndex() + 1);
    }

    @Override
    public Openable getOpenable() {
        return swf;
    }

    @Override
    public boolean isModified() {
        return getFrame().isModified();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.scene);
        hash = 47 * hash + this.realFrameIndex;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SceneFrame other = (SceneFrame) obj;
        if (this.realFrameIndex != other.realFrameIndex) {
            return false;
        }
        return Objects.equals(this.scene, other.scene);
    }    
}
