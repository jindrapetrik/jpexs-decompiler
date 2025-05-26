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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.Objects;

/**
 * A frame in a Scene object.
 *
 * @author JPEXS
 */
public class SceneFrame implements TreeItem {

    /**
     * SWF.
     */
    private final SWF swf;

    /**
     * Scene.
     */
    private final Scene scene;

    /**
     * Real frame index - from main SWF time line.
     */
    private final int realFrameIndex;

    /**
     * Constructs SceneFrame.
     *
     * @param swf SWF
     * @param scene Scene
     * @param realFrameIndex Real frame index - from main SWF time line
     */
    public SceneFrame(SWF swf, Scene scene, int realFrameIndex) {
        this.swf = swf;
        this.scene = scene;
        this.realFrameIndex = realFrameIndex;
    }

    /**
     * Gets scene frame index.
     *
     * @return Frame index
     */
    public int getSceneFrameIndex() {
        return realFrameIndex - scene.startFrame;
    }

    /**
     * Gets frame.
     *
     * @return Frame
     */
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
