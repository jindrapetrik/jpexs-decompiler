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

import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
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

    public int characterId;
    public MATRIX matrix = null;
    public String instanceName = null;
    public CXFORM colorTransForm = null;
    public CXFORMWITHALPHA colorTransFormAlpha = null;
    public boolean cacheAsBitmap = false;
    public int blendMode = 0;
    public List<FILTER> filters = new ArrayList<>();
    public boolean isVisible = true;
    public RGBA backGroundColor = null;
    public CLIPACTIONS clipActions = null;
    public int ratio;
    public boolean key = false;

    public DepthState() {

    }

    public DepthState(DepthState obj) {
        characterId = obj.characterId;
        matrix = obj.matrix;
        instanceName = obj.instanceName;
        colorTransForm = obj.colorTransForm;
        colorTransFormAlpha = obj.colorTransFormAlpha;
        cacheAsBitmap = obj.cacheAsBitmap;
        blendMode = obj.blendMode;
        filters = obj.filters;
        isVisible = obj.isVisible;
        backGroundColor = obj.backGroundColor;
        clipActions = obj.clipActions;
        ratio = obj.ratio;
    }
}
