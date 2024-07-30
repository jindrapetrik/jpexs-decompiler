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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.ExportRectangle;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.BUTTONRECORD;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class ButtonTag extends DrawableTag implements Timelined {

    public static int FRAME_UP = 0;

    public static int FRAME_OVER = 1;

    public static int FRAME_DOWN = 2;

    public static int FRAME_HITTEST = 3;

    private Timeline timeline;

    private boolean isSingleFrameInitialized;

    private boolean isSingleFrame;

    public ButtonTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    public abstract List<BUTTONRECORD> getRecords();

    public abstract boolean trackAsMenu();

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        for (BUTTONRECORD r : getRecords()) {
            needed.add(r.characterId);
        }
    }

    @Override
    public RECT getRect() {
        return getRect(new HashSet<>());
    }

    @Override
    public RECT getRectWithStrokes() {
        return getRect();
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
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        getTimeline().toImage(frame, time, renderContext, image, fullImage, isClip, transformation, strokeTransformation, absoluteTransformation, colorTransform, unzoom, sameImage, viewRect, fullTransformation, scaleStrokes, drawMode, blendMode, canUseSmoothing);
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        getTimeline().toSVG(0, 0, null, 0, exporter, colorTransform, level + 1);
    }

    public DefineButtonSoundTag getSounds() {
        if (swf == null) {
            return null;
        }
        return (DefineButtonSoundTag) swf.getCharacterIdTag(getCharacterId(), DefineButtonSoundTag.ID);
    }

    @Override
    public void toHtmlCanvas(StringBuilder result, double unitDivisor) {
        getTimeline().toHtmlCanvas(result, unitDivisor, Arrays.asList(0)); //TODO: handle states?
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
            isSingleFrame = getTimeline().isSingleFrame();
            isSingleFrameInitialized = true;
        }
    }

    @Override
    public Timeline getTimeline() {
        if (timeline != null) {
            return timeline;
        }

        timeline = new Timeline(swf, this, getCharacterId(), getRect());
        initTimeline(timeline);
        return timeline;
    }

    @Override
    public void resetTimeline() {
        if (timeline != null) {
            timeline.reset(swf, this, getCharacterId(), getRect());
            initTimeline(timeline);
        }
    }

    protected abstract void initTimeline(Timeline timeline);

    @Override
    public ReadOnlyTagList getTags() {
        return ReadOnlyTagList.EMPTY;
    }

    @Override
    public void removeTag(int index) {
    }

    @Override
    public void removeTag(Tag tag) {
    }

    @Override
    public void addTag(Tag tag) {
    }

    @Override
    public void addTag(int index, Tag tag) {
    }

    @Override
    public int indexOfTag(Tag tag) {
        return -1;
    }

    @Override
    public void replaceTag(int index, Tag newTag) {
        removeTag(index);
        addTag(index, newTag);
    }

    @Override
    public void replaceTag(Tag oldTag, Tag newTag) {
        setModified(true);
        int index = indexOfTag(oldTag);
        if (index != -1) {
            replaceTag(index, newTag);
        }
    }

    @Override
    public void setSwf(SWF swf, boolean deep) {
        this.swf = swf;
        for (BUTTONRECORD record : getRecords()) {
            record.setSourceTag(this);
        }
    }

    @Override
    public void setModified(boolean value) {
        super.setModified(value);
        for (BUTTONRECORD record : getRecords()) {
            record.setModified(value);
        }
    }
}
