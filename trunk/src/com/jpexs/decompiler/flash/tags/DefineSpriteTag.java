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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.exporters.Point;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines a sprite character
 */
public class DefineSpriteTag extends CharacterTag implements Container, DrawableTag, Timelined {

    /**
     * Character ID of sprite
     */
    @SWFType(BasicType.UI16)
    public int spriteId;
    /**
     * Number of frames in sprite
     */
    @SWFType(BasicType.UI16)
    public int frameCount;
    /**
     * A series of tags
     */
    public List<Tag> subTags;
    public boolean hasEndTag;

    public static final int ID = 39;

    private Timeline timeline;

    private boolean isSingleFrameInitialized;
    private boolean isSingleFrame;

    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(swf, subTags, spriteId, getRect());
        }
        return timeline;
    }

    @Override
    public void resetTimeline() {
        timeline = null;
    }

    @Override
    public int getCharacterId() {
        return spriteId;
    }

    private RECT getCharacterBounds(Set<Integer> characters) {
        RECT ret = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        boolean foundSomething = false;
        for (int c : characters) {
            Tag t = swf.characters.get(c);
            RECT r = null;
            if (t instanceof BoundedTag) {
                r = ((BoundedTag) t).getRect();
            }
            if (r != null) {
                foundSomething = true;
                ret.Xmin = Math.min(r.Xmin, ret.Xmin);
                ret.Ymin = Math.min(r.Ymin, ret.Ymin);
                ret.Xmax = Math.max(r.Xmax, ret.Xmax);
                ret.Ymax = Math.max(r.Ymax, ret.Ymax);
            }
        }
        if (!foundSomething) {
            return new RECT();
        }
        return ret;
    }
    private static final Cache<RECT> rectCache = Cache.getInstance(true);

    @Override
    public RECT getRect() {
        if (rectCache.contains(this)) {
            return (RECT) rectCache.get(this);
        }
        RECT emptyRet = new RECT();
        RECT ret = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        HashMap<Integer, Integer> depthMap = new HashMap<>();
        boolean foundSomething = false;
        int pos = 0;
        for (Tag t : subTags) {
            pos++;
            MATRIX m = null;
            int characterId = -1;
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pot = (PlaceObjectTypeTag) t;
                m = pot.getMatrix();
                int charId = pot.getCharacterId();
                if (charId > -1) {
                    depthMap.put(pot.getDepth(), charId);
                    characterId = (charId);
                } else {
                    Integer chi = (depthMap.get(pot.getDepth()));
                    if (chi != null) {
                        characterId = chi;
                    }
                }
            }
            if (characterId == -1) {
                continue;
            }
            Set<Integer> need = new HashSet<>();
            need.add(characterId);
            RECT r = getCharacterBounds(need);

            if (m != null) {
                AffineTransform trans = SWF.matrixToTransform(m);

                java.awt.Point topleft = new java.awt.Point();
                trans.transform(new java.awt.Point(r.Xmin, r.Ymin), topleft);
                java.awt.Point topright = new java.awt.Point();
                trans.transform(new java.awt.Point(r.Xmax, r.Ymin), topright);
                java.awt.Point bottomright = new java.awt.Point();
                trans.transform(new java.awt.Point(r.Xmax, r.Ymax), bottomright);
                java.awt.Point bottomleft = new java.awt.Point();
                trans.transform(new java.awt.Point(r.Xmin, r.Ymax), bottomleft);

                r.Xmin = (int) Math.min(Math.min(Math.min(topleft.x, topright.x), bottomleft.x), bottomright.x);
                r.Ymin = (int) Math.min(Math.min(Math.min(topleft.y, topright.y), bottomleft.y), bottomright.y);
                r.Xmax = (int) Math.max(Math.max(Math.max(topleft.x, topright.x), bottomleft.x), bottomright.x);
                r.Ymax = (int) Math.max(Math.max(Math.max(topleft.y, topright.y), bottomleft.y), bottomright.y);

            }
            ret.Xmin = Math.min(r.Xmin, ret.Xmin);
            ret.Ymin = Math.min(r.Ymin, ret.Ymin);
            ret.Xmax = Math.max(r.Xmax, ret.Xmax);
            ret.Ymax = Math.max(r.Ymax, ret.Ymax);
            foundSomething = true;
        }
        if (!foundSomething) {
            ret = new RECT();
        }
        rectCache.put(this, ret);
        return ret;
    }

    /**
     * Constructor
     *
     * @param swf
     * @param headerData
     * @param data Data bytes
     * @param level
     * @param pos
     * @param parallel
     * @param skipUnusualTags
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public DefineSpriteTag(SWF swf, byte[] headerData, byte[] data, int level, long pos, boolean parallel, boolean skipUnusualTags) throws IOException, InterruptedException {
        super(swf, ID, "DefineSprite", headerData, data, pos);
        SWFInputStream sis = new SWFInputStream(new ByteArrayInputStream(data), swf.version, pos);
        spriteId = sis.readUI16();
        frameCount = sis.readUI16();
        List<Tag> subTags = sis.readTagList(swf, this, level + 1, parallel, skipUnusualTags, true, swf.gfx);
        if (subTags.get(subTags.size() - 1).getId() == EndTag.ID) {
            hasEndTag = true;
            subTags.remove(subTags.size() - 1);
        }
        this.subTags = subTags;
    }
    static int c = 0;

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        if (Configuration.disableDangerous.get()) {
            return super.getData();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.debugCopy.get()) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(super.data));
        }
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(spriteId);
            sos.writeUI16(frameCount);
            if (hasEndTag) {
                sos.writeTags(subTags);
            }
            sos.writeUI16(0);
            sos.close();
        } catch (IOException e) {
        }
        return baos.toByteArray();
    }

    /**
     * Returns all sub-items
     *
     * @return List of sub-items
     */
    @Override
    public List<ContainerItem> getSubItems() {
        List<ContainerItem> ret = new ArrayList<>();
        ret.addAll(subTags);
        return ret;
    }

    /**
     * Returns number of sub-items
     *
     * @return Number of sub-items
     */
    @Override
    public int getItemCount() {
        return subTags.size();
    }

    @Override
    public boolean hasSubTags() {
        return true;
    }

    @Override
    public List<Tag> getSubTags() {
        return subTags;
    }

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = new HashSet<>();
        for (Tag t : subTags) {
            ret.addAll(t.getNeededCharacters());
        }
        return ret;
    }

    @Override
    public void toImage(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SWF.frameToImage(getTimeline(), frame, time, stateUnderCursor, mouseButton, image, transformation, colorTransform);
    }

    @Override
    public Point getImagePos(int frame) {
        return new Point(0, 0);
    }

    @Override
    public int getNumFrames() {
        return frameCount;
    }

    @Override
    public boolean isSingleFrame() {
        if (!isSingleFrameInitialized) {
            initialiteIsSingleFrame();
        }
        return isSingleFrame;
    }

    private synchronized void initialiteIsSingleFrame() {
        if (!isSingleFrameInitialized) {
            if (frameCount > 1) {
                isSingleFrameInitialized = true;
                return;
            }

            isSingleFrame = getTimeline().isSingleFrame();
            isSingleFrameInitialized = true;
        }
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, DepthState stateUnderCursor, int mouseButton, Matrix transformation) {
        return getTimeline().getOutline(frame, time, ratio, stateUnderCursor, mouseButton, transformation);
    }

    @Override
    public boolean isModified() {
        if(super.isModified()){
            return true;
        }
        for(Tag t:subTags){
            if(t.isModified()){
                return true;
            }
        }
        return false;
    }        
}
