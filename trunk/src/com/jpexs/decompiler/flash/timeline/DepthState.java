/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DepthState {

    public int characterId = -1;
    public MATRIX matrix = null;
    public String instanceName = null;
    public ColorTransform colorTransForm = null;
    public boolean cacheAsBitmap = false;
    public int blendMode = 0;
    public List<FILTER> filters = new ArrayList<>();
    public boolean isVisible = true;
    public RGBA backGroundColor = null;
    public CLIPACTIONS clipActions = null;
    public int ratio = -1;
    public boolean key = false;
    public int clipDepth = -1;
    public int time = 0;
    private final SWF swf;
    public Frame frame;

    public DepthState(SWF swf, Frame frame) {
        this.swf = swf;
        this.frame = frame;
    }

    public DepthState(DepthState obj, Frame frame, boolean addTime) {
        this.frame = frame;
        swf = obj.swf;
        frame = obj.frame;
        characterId = obj.characterId;
        matrix = obj.matrix;
        instanceName = obj.instanceName;
        colorTransForm = obj.colorTransForm;
        cacheAsBitmap = obj.cacheAsBitmap;
        blendMode = obj.blendMode;
        filters = obj.filters;
        isVisible = obj.isVisible;
        backGroundColor = obj.backGroundColor;
        clipActions = obj.clipActions;
        ratio = obj.ratio;
        clipDepth = obj.clipDepth;
        time = obj.time;
        if (addTime) {
            time++;
        }
    }

}
