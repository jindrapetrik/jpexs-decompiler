/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.abc.CopyOutputStream;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ByteArrayRange;
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
public class DefineSpriteTag extends CharacterTag implements DrawableTag, Timelined {

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

    private static final Cache<DefineSpriteTag, RECT> rectCache = Cache.getInstance(true, true, "rect_sprite");

    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(swf, this, subTags, spriteId, getRect());
        }
        return timeline;
    }

    @Override
    public int getCharacterId() {
        return spriteId;
    }

    private RECT getCharacterBounds(Set<Integer> characters, Set<BoundedTag> added) {
        RECT ret = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        boolean foundSomething = false;
        for (int c : characters) {
            Tag t = swf.getCharacter(c);
            RECT r = null;
            if (t instanceof BoundedTag) {
                BoundedTag bt = (BoundedTag) t;
                if (!added.contains(bt)) {
                    added.add(bt);
                    r = bt.getRect(added);
                    added.remove(bt);
                }
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

    @Override
    public RECT getRect() {
        return getRect(new HashSet<BoundedTag>());
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        if (rectCache.contains(this)) {
            return rectCache.get(this);
        }
        RECT ret = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        HashMap<Integer, Integer> depthMap = new HashMap<>();
        boolean foundSomething = false;
        for (Tag t : subTags) {
            MATRIX m = null;
            int characterId = -1;
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pot = (PlaceObjectTypeTag) t;
                m = pot.getMatrix();
                int charId = pot.getCharacterId();
                if (charId > -1) {
                    depthMap.put(pot.getDepth(), charId);
                    characterId = charId;
                } else {
                    Integer chi = depthMap.get(pot.getDepth());
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
            RECT r = getCharacterBounds(need, added);

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
     */
    public DefineSpriteTag(SWF swf) {
        super(swf, ID, "DefineSprite", null);
        spriteId = swf.getNextCharacterId();
        subTags = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     * @param level
     * @param parallel
     * @param skipUnusualTags
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public DefineSpriteTag(SWFInputStream sis, int level, ByteArrayRange data, boolean parallel, boolean skipUnusualTags) throws IOException, InterruptedException {
        super(sis.getSwf(), ID, "DefineSprite", data);
        readData(sis, data, level, parallel, skipUnusualTags, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException, InterruptedException {
        spriteId = sis.readUI16("spriteId");
        frameCount = sis.readUI16("frameCount");
        List<Tag> subTags = sis.readTagList(this, level + 1, parallel, skipUnusualTags, true, lazy);
        if (subTags.size() > 0 && subTags.get(subTags.size() - 1).getId() == EndTag.ID) {
            hasEndTag = true;
            subTags.remove(subTags.size() - 1);
        }
        this.subTags = subTags;
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = baos;
        if (Configuration.debugCopy.get()) {
            os = new CopyOutputStream(os, new ByteArrayInputStream(getOriginalData()));
        }
        SWFOutputStream sos = new SWFOutputStream(os, getVersion());
        try {
            sos.writeUI16(spriteId);
            sos.writeUI16(frameCount);
            sos.writeTags(subTags);
            if (hasEndTag) {
                sos.writeUI16(0);
            }
            sos.close();
        } catch (IOException e) {
            throw new Error("This should never happen.", e);
        }
        return baos.toByteArray();
    }

    public List<Tag> getSubTags() {
        return subTags;
    }

    @Override
    public void setModified(boolean value) {
        if (!value) {
            for (Tag subTag : subTags) {
                subTag.setModified(false);
            }
        }

        super.setModified(value);
    }

    public static void clearCache() {
        rectCache.clear();
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
        for (Tag t : subTags) {
            if (t instanceof CharacterIdTag) {
                needed.add(((CharacterIdTag) t).getCharacterId());
            }
        }
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = getTimeline().removeCharacter(characterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform) {
        SWF.frameToImage(getTimeline(), frame, time, renderContext, image, transformation, colorTransform);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) throws IOException {
        SWF.frameToSvg(getTimeline(), 0, 0, null, 0, exporter, colorTransform, level + 1, zoom);
    }

    @Override
    public int getNumFrames() {
        // flashplayer ignores the count stored in frameCount
        return getTimeline().getFrameCount(); // frameCount
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
            if (getTimeline().getRealFrameCount() > 1) {
                isSingleFrameInitialized = true;
                return;
            }

            isSingleFrame = getTimeline().isSingleFrame();
            isSingleFrameInitialized = true;
        }
    }

    @Override
    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation) {
        return getTimeline().getOutline(frame, time, ratio, renderContext, transformation);
    }

    @Override
    public boolean isModified() {
        if (super.isModified()) {
            return true;
        }
        for (Tag t : subTags) {
            if (t.isModified()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toHtmlCanvas(double unitDivisor) {
        return getTimeline().toHtmlCanvas(unitDivisor, null);
    }

    @Override
    public void setCharacterId(int characterId) {
        this.spriteId = characterId;
    }
}
