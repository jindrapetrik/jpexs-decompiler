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
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.CLIPACTIONS;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.SerializableImage;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Timeline {

    public List<Frame> frames = new ArrayList<>();
    public int id;
    public SWF swf;
    public RECT displayRect;


    public int getMaxDepth() {
        int max_depth = 0;
        for (Frame f : frames) {
            for (int depth : f.layers.keySet()) {
                if (depth > max_depth) {
                    max_depth = depth;
                }
            }
        }
        return max_depth;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public Timeline(SWF swf) {
        this(swf, swf.tags, 0, swf.displayRect);
    }

    public Timeline(SWF swf, List<Tag> tags, int id, RECT displayRect) {
        this.id = id;
        this.swf = swf;
        this.displayRect = displayRect;
        Frame frame = new Frame(this);        
        for (Tag t : tags) {
            if (t instanceof SetBackgroundColorTag) {
                frame.backgroundColor = ((SetBackgroundColorTag) t).backgroundColor;
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag po = (PlaceObjectTypeTag) t;
                int depth = po.getDepth();
                if (!frame.layers.containsKey(depth)) {
                    frame.layers.put(depth, new DepthState(swf,frame));
                }
                DepthState fl = frame.layers.get(depth);
                int characterId = po.getCharacterId();
                if (characterId != -1) {
                    fl.characterId = characterId;
                }
                if (po.flagMove()) {
                    MATRIX matrix2 = po.getMatrix();
                    if (matrix2 != null) {
                        fl.matrix = matrix2;
                    }
                    String instanceName2 = po.getInstanceName();
                    if (instanceName2 != null) {
                        fl.instanceName = instanceName2;
                    }
                    ColorTransform colorTransForm2 = po.getColorTransform();
                    if (colorTransForm2 != null) {
                        fl.colorTransForm = colorTransForm2;
                    }

                    CLIPACTIONS clipActions2 = po.getClipActions();
                    if (clipActions2 != null) {
                        fl.clipActions = clipActions2;
                    }
                    if (po.cacheAsBitmap()) {
                        fl.cacheAsBitmap = true;
                    }
                    int blendMode2 = po.getBlendMode();
                    if (blendMode2 > 0) {
                        fl.blendMode = blendMode2;
                    }
                    List<FILTER> filters2 = po.getFilters();
                    if (filters2 != null) {
                        fl.filters = filters2;
                    }
                    int ratio2 = po.getRatio();
                    if (ratio2 > -1) {
                        fl.ratio = ratio2;
                    }
                    int clipDepth2 = po.getClipDepth();
                    if (clipDepth2 > -1) {
                        fl.clipDepth = clipDepth2;
                    }
                } else {
                    fl.matrix = po.getMatrix();
                    fl.instanceName = po.getInstanceName();
                    fl.colorTransForm = po.getColorTransform();
                    fl.cacheAsBitmap = po.cacheAsBitmap();
                    fl.blendMode = po.getBlendMode();
                    fl.filters = po.getFilters();
                    fl.ratio = po.getRatio();
                    fl.clipActions = po.getClipActions();
                    fl.clipDepth = po.getClipDepth();
                }
                fl.key = true;
            }
            if (t instanceof RemoveTag) {
                RemoveTag r = (RemoveTag) t;
                int depth = r.getDepth();
                frame.layers.remove(depth);
            }
            if (t instanceof DoActionTag) {
                frame.action = (DoActionTag) t;
            }
            if (t instanceof ShowFrameTag) {
                frames.add(frame);
                frame = new Frame(frame);
            }
        }
    }

    public void toImage(int frame, int ratio, Point mousePos,int mouseButton,SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SWF.frameToImage(this, frame, mousePos,mouseButton,image, transformation, colorTransform);
    }
}
