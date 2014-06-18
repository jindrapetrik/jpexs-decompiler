/*
 *  Copyright (C) 2010-2014 JPEXS
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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class ButtonTag extends CharacterTag implements DrawableTag, Timelined {

    public static int FRAME_UP = 0;
    public static int FRAME_OVER = 1;
    public static int FRAME_DOWN = 2;
    public static int FRAME_HITTEST = 3;

    public ButtonTag(SWF swf, int id, String name, long pos, int length) {
        super(swf, id, name, pos, length);
    }

    public abstract List<BUTTONRECORD> getRecords();

    public abstract boolean trackAsMenu();

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        return getTimeline().getOutline(frame, time, ratio, stateUnderCursor, mouseButton, transformation);
    }

    @Override
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SWF.frameToImage(getTimeline(), frame, time, stateUnderCursor, mouseButton, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        SWF.frameToSvg(getTimeline(), 0, 0, null, 0, exporter, colorTransform, level + 1);
    }

    public DefineButtonSoundTag getSounds() {
        for (Tag t : swf.tags) {
            if (t instanceof DefineButtonSoundTag) {
                DefineButtonSoundTag st = (DefineButtonSoundTag) t;
                if (st.buttonId == getCharacterId()) {
                    return st;
                }
            }
        }
        return null;
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        return getTimeline().toHtmlCanvas(unitDivisor, Arrays.asList(0)); //TODO: handle states?
    }

}
