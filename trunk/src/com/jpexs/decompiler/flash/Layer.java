/*
 *  Copyright (C) 2010-2014 PEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.types.CXFORM;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Layer {

    public MATRIX matrix;
    public String instanceName = null;
    public CXFORM colorTransForm = null;
    public CXFORMWITHALPHA colorTransFormAlpha = null;
    public boolean cacheAsBitmap = false;
    public int blendMode = 0;
    public List<FILTER> filters = null;
    public boolean isVisible = true;
    public RGBA backGroundColor = null;
    public int characterId = -1;
    public int ratio = -1;
    public int duration = 0;
    public boolean visible = true;
}
