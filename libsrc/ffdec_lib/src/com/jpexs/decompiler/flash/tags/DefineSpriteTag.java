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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.DrawableTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.RenderContext;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFField;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Cache;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * DefineSprite tag - Defines a sprite character.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class DefineSpriteTag extends DrawableTag implements Timelined {

    public static final int ID = 39;

    public static final String NAME = "DefineSprite";

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
    @SWFField
    private List<Tag> subTags;

    @Internal
    public transient ReadOnlyTagList readOnlyTags;

    public boolean hasEndTag;

    private transient Timeline timeline;

    private boolean isSingleFrameInitialized;

    private boolean isSingleFrame;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineSpriteTag(SWF swf) {
        super(swf, ID, NAME, null);
        spriteId = swf.getNextCharacterId();
        subTags = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @param level Level
     * @param parallel Parallel
     * @param skipUnusualTags Skip unusual tags
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public DefineSpriteTag(SWFInputStream sis, int level, ByteArrayRange data, boolean parallel, boolean skipUnusualTags) throws IOException, InterruptedException {
        super(sis.getSwf(), ID, NAME, data);
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
        readOnlyTags = null;
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(spriteId);
        sos.writeUI16(frameCount);
        sos.writeTags(getTags());
        if (hasEndTag) {
            sos.writeUI16(0);
        }
    }
    
    @Override
    public void getDataNoScript(SWFOutputStream sos) throws IOException {
        sos.writeUI16(spriteId);
        sos.writeUI16(frameCount);
        ReadOnlyTagList tags = getTags();
        for (Tag t : tags) {
            if (t instanceof DoActionTag
                    || t instanceof DoInitActionTag
                    || t instanceof ABCContainerTag
                    || t instanceof SymbolClassTag
                    || t instanceof ExportAssetsTag) {
                continue;
            }
            t.writeTagNoScripts(sos);
        }
        if (hasEndTag) {
            sos.writeUI16(0);
        }
    }

    @Override
    public Timeline getTimeline() {
        if (timeline == null) {
            timeline = new Timeline(swf, this, spriteId, getRect());
        }
        return timeline;
    }

    @Override
    public void resetTimeline() {
        Cache<CharacterTag, RECT> cache = swf == null ? null : swf.getRectCache();
        if (cache != null) {
            cache.remove(this);
        }
        if (timeline != null) {
            timeline.reset(swf, this, spriteId, getRect());
        }
    }

    @Override
    public int getCharacterId() {
        return spriteId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.spriteId = characterId;
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
                if (r.Xmin < r.Xmax && r.Ymin < r.Ymax) {
                    foundSomething = true;
                    ret.Xmin = Math.min(r.Xmin, ret.Xmin);
                    ret.Ymin = Math.min(r.Ymin, ret.Ymin);
                    ret.Xmax = Math.max(r.Xmax, ret.Xmax);
                    ret.Ymax = Math.max(r.Ymax, ret.Ymax);
                }
            }
        }
        if (!foundSomething) {
            return new RECT();
        }
        return ret;
    }

    @Override
    public RECT getRect() {
        return getRect(new HashSet<>());
    }

    @Override
    public RECT getRect(Set<BoundedTag> added) {
        Cache<CharacterTag, RECT> cache = swf == null ? null : swf.getRectCache();
        RECT ret = cache == null ? null : cache.get(this);
        if (ret != null) {
            return ret;
        }

        ret = new RECT(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE);
        HashMap<Integer, Integer> depthMap = new HashMap<>();
        HashMap<Integer, MATRIX> depthMatrixMap = new HashMap<>();
        boolean foundSomething = false;
        for (Tag t : getTags()) {
            MATRIX m = null;
            int characterId = -1;
            if (t instanceof RemoveTag) {
                RemoveTag rt = (RemoveTag) t;
                depthMap.remove(rt.getDepth());
                depthMatrixMap.remove(rt.getDepth());
            }
            if (t instanceof PlaceObjectTypeTag) {
                PlaceObjectTypeTag pot = (PlaceObjectTypeTag) t;
                m = pot.getMatrix();

                if (m == null) {
                    if (pot.flagMove()) {
                        if (!depthMatrixMap.containsKey(pot.getDepth())) {
                            m = null; //??
                        } else {
                            m = depthMatrixMap.get(pot.getDepth());
                        }
                    }
                }

                if (pot.flagMove() != depthMap.containsKey(pot.getDepth())) {
                    continue;
                }

                depthMatrixMap.put(pot.getDepth(), m);

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
            if (characterId != -1 && swf != null) {
                //Do not handle Fonts as characters. TODO: make this better
                if (swf.getCharacter(characterId) instanceof FontTag) {
                    characterId = -1;
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

        if (cache != null) {
            cache.put(this, ret);
        }

        return ret;
    }

    @Override
    public void setModified(boolean value) {
        if (!value) {
            for (Tag subTag : getTags()) {
                subTag.setModified(false);
            }
        }

        super.setModified(value);
    }

    @Override
    public synchronized ReadOnlyTagList getTags() {
        if (readOnlyTags == null) {
            readOnlyTags = new ReadOnlyTagList(subTags);
        }

        return readOnlyTags;
    }

    @Override
    public synchronized void removeTag(int index) {
        setModified(true);
        subTags.remove(index);
    }

    @Override
    public synchronized void removeTag(Tag tag) {
        setModified(true);
        subTags.remove(tag);
    }

    @Override
    public synchronized void addTag(Tag tag) {
        setModified(true);
        subTags.add(tag);
    }

    @Override
    public synchronized void addTag(int index, Tag tag) {
        setModified(true);
        subTags.add(index, tag);
    }

    @Override
    public synchronized int indexOfTag(Tag tag) {
        return subTags.indexOf(tag);
    }

    @Override
    public void createOriginalData() {
        super.createOriginalData();
        for (Tag subTag : getTags()) {
            subTag.createOriginalData();
        }
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        for (Tag t : getTags()) {           
            if (
                    (t instanceof PlaceObjectTypeTag)
                    || (t instanceof StartSoundTag)
                    || (t instanceof StartSound2Tag)
                    || (t instanceof VideoFrameTag)
                ) {
                t.getNeededCharacters(needed, swf);
            }            
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = getTimeline().replaceCharacter(oldCharacterId, newCharacterId);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = getTimeline().removeCharacter(characterId, null);
        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public int getUsedParameters() {
        return PARAMETER_FRAME | PARAMETER_TIME | PARAMETER_RATIO; // inner tags can contain morphshapes, too
    }

    @Override
    public Shape getOutline(boolean fast, int frame, int time, int ratio, RenderContext renderContext, Matrix transformation, boolean stroked, ExportRectangle viewRect, double unzoom) {
        return getTimeline().getOutline(fast, frame, time, renderContext, transformation, stroked, viewRect, unzoom);
    }

    @Override
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        getTimeline().toImage(frame, time, renderContext, image, fullImage, isClip, transformation, strokeTransformation, absoluteTransformation, colorTransform, unzoom, sameImage, viewRect, viewRectRaw, fullTransformation, scaleStrokes, drawMode, blendMode, canUseSmoothing, new ArrayList<>());
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, Matrix transformation, Matrix strokeTransformation) throws IOException {
        getTimeline().toSVG(0, 0, null, 0, exporter, colorTransform, level + 1, transformation, strokeTransformation);
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        getTimeline().toHtmlCanvas(result, unitDivisor, null);
    }

    @Override
    public int getNumFrames() {
        // flashplayer ignores the count stored in frameCount
        return getTimeline().getFrameCount(); // frameCount
    }

    @Override
    public boolean isSingleFrame() {
        if (!isSingleFrameInitialized) {
            initializeIsSingleFrame();
        }
        return isSingleFrame;
    }

    private synchronized void initializeIsSingleFrame() {
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
    public boolean isModified() {
        if (super.isModified()) {
            return true;
        }
        for (Tag t : getTags()) {
            if (t.isModified()) {
                return true;
            }
        }
        return false;
    }

    public void clearReadOnlyListCache() {
        readOnlyTags = null;
    }

    @Override
    public void replaceTag(int index, Tag newTag) {
        removeTag(index);
        addTag(index, newTag);
    }

    @Override
    public void replaceTag(Tag oldTag, Tag newTag) {
        int index = indexOfTag(oldTag);
        if (index != -1) {
            replaceTag(index, newTag);
        }
    }

    @Override
    public RECT getRectWithStrokes() {
        return getRect(); //?
    }

    @Override
    public Set<Integer> getMissingNeededCharacters(Set<Integer> needed) {
        Set<Integer> ret = new LinkedHashSet<>();
        for (Tag tag : getTags()) {
            Set<Integer> subNeeded = new HashSet<>();
            tag.getNeededCharactersDeep(subNeeded);
            Set<Integer> sub = tag.getMissingNeededCharacters(subNeeded);
            ret.addAll(sub);
        }
        return ret;
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }        
}
