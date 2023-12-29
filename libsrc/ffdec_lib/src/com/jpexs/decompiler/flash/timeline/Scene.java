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
public class Scene implements TreeItem {
    private SWF swf;
    public int startFrame;
    public int endFrame;
    public String name;        

    public Scene(SWF swf, int startFrame, int endFrame, String name) {
        this.swf = swf;
        this.startFrame = startFrame;
        this.endFrame = endFrame;
        this.name = name;
    }
    
    public int getSceneFrameCount() {
        return endFrame  - startFrame + 1;
    }
    
    public SceneFrame getSceneFrame(int sceneFrameIndex) {
        if (sceneFrameIndex >= getSceneFrameCount()) {
            throw new IndexOutOfBoundsException("Invalid sceneframe index");
        }
        return new SceneFrame(swf, this, startFrame + sceneFrameIndex);
    }

    @Override
    public Openable getOpenable() {
        return swf;
    }

    @Override
    public boolean isModified() {
        return false; //??
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.swf);
        hash = 97 * hash + this.startFrame;
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
        final Scene other = (Scene) obj;
        if (this.startFrame != other.startFrame) {
            return false;
        }
        return Objects.equals(this.swf, other.swf);
    }

    @Override
    public String toString() {
        return name;
    }        
}
