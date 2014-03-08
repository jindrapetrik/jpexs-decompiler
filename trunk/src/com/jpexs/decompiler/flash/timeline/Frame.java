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

import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 * @author JPEXS
 */
public class Frame {

    public TreeMap<Integer, DepthState> layers = new TreeMap<>();
    public DoActionTag action;
    public RGB backgroundColor = new RGBA(0, 0, 0, 0);
    public Timeline timeline;

    public Frame(Timeline timeline) {
        this.timeline = timeline;
    }

    public Frame(Frame obj) {
        layers = new TreeMap<>();
        backgroundColor = obj.backgroundColor;
        timeline = obj.timeline;
        for (int depth : obj.layers.keySet()) {
            layers.put(depth, new DepthState(obj.layers.get(depth), this, true));
        }
    }

    private class Clip {

        public Shape shape;
        public int depth;

        public Clip(Shape shape, int depth) {
            this.shape = shape;
            this.depth = depth;
        }

    }

    public List<DepthState> getObjectsUnderCursor(Point mousePos, int mouseButton, Matrix transformation) {
        List<DepthState> ret = new ArrayList<>();
        List<Integer> keys = new ArrayList<>(layers.keySet());
        DepthState maxds = null;
        Stack<Clip> clips = new Stack<>();
        for (int d : keys) { //int i=keys.size()-1;i>=0;i--){
            //int d = keys.get(i);
            DepthState ds = layers.get(d);
            Clip currentClip = null;
            for (int j = clips.size() - 1; j >= 0; j--) {
                Clip cl = clips.get(j);
                if (cl.depth >= d) {
                    clips.remove(j);
                }
            }
            if (!clips.isEmpty()) {
                currentClip = clips.peek();
            }
            CharacterTag c = timeline.swf.characters.get(ds.characterId);
            if (c instanceof DrawableTag) {
                Matrix m = new Matrix(ds.matrix).concatenate(transformation);
                int frame = ds.time % ((DrawableTag) c).getNumFrames();
                if (c instanceof ButtonTag) {
                    frame = ButtonTag.FRAME_HITTEST;
                }
                Shape outline = SHAPERECORD.twipToPixelShape(((DrawableTag) c).getOutline(frame, ds.ratio, null, mouseButton, m));

                Area checkArea = new Area(outline);
                if (currentClip != null) {
                    Area a = new Area(checkArea.getBounds());
                    a.subtract(new Area(currentClip.shape));
                    checkArea.subtract(a);
                }
                if (ds.clipDepth > -1) {
                    Clip clip = new Clip(checkArea, ds.clipDepth);
                    clips.push(clip);
                } else if (checkArea.contains(mousePos)) {
                    maxds = ds;
                }
            }
        }
        if (maxds == null) {
            return ret;
        }
        ret.add(maxds);

        CharacterTag c = timeline.swf.characters.get(maxds.characterId);
        if (c instanceof Timelined) {
            Timelined tc = ((Timelined) c);

            int frame = maxds.time % ((DrawableTag) c).getNumFrames();;
            if (tc instanceof ButtonTag) {
                frame = ButtonTag.FRAME_HITTEST;
            }

            ret.addAll(tc.getTimeline().frames.get(frame).getObjectsUnderCursor(mousePos, mouseButton, new Matrix(maxds.matrix).preConcatenate(transformation)));
        }

        return ret;
    }

}
