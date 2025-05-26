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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for button tags.
 *
 * @author JPEXS
 */
public abstract class ButtonTag extends DrawableTag implements Timelined {

    /**
     * Frame up
     */
    public static final int FRAME_UP = 0;

    /**
     * Frame over
     */
    public static final int FRAME_OVER = 1;

    /**
     * Frame down
     */
    public static final int FRAME_DOWN = 2;

    /**
     * Frame hit test
     */
    public static final int FRAME_HITTEST = 3;

    private transient Timeline timeline;

    private boolean isSingleFrameInitialized;

    private boolean isSingleFrame;

    /**
     * Constructor.
     *
     * @param swf SWF
     * @param id Tag ID
     * @param name Tag name
     * @param data Tag data
     */
    public ButtonTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    /**
     * Gets button records.
     *
     * @return Button records
     */
    public abstract List<BUTTONRECORD> getRecords();

    /**
     * Checks if the button is tracked as a menu.
     *
     * @return True if the button is tracked as a menu, otherwise false
     */
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
    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, SerializableImage fullImage, boolean isClip, Matrix transformation, Matrix strokeTransformation, Matrix absoluteTransformation, Matrix fullTransformation, ColorTransform colorTransform, double unzoom, boolean sameImage, ExportRectangle viewRect, ExportRectangle viewRectRaw, boolean scaleStrokes, int drawMode, int blendMode, boolean canUseSmoothing) {
        getTimeline().toImage(frame, time, renderContext, image, fullImage, isClip, transformation, strokeTransformation, absoluteTransformation, colorTransform, unzoom, sameImage, viewRect, viewRectRaw, fullTransformation, scaleStrokes, drawMode, blendMode, canUseSmoothing, new ArrayList<>());
    }

    @Override
    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level) throws IOException {
        getTimeline().toSVG(0, 0, null, 0, exporter, colorTransform, level + 1);
    }

    /**
     * Gets the sounds.
     *
     * @return Sounds
     */
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
            initializeIsSingleFrame();
        }
        return isSingleFrame;
    }

    private synchronized void initializeIsSingleFrame() {
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

    /**
     * Initializes the timeline.
     *
     * @param timeline Timeline
     */
    protected abstract void initTimeline(Timeline timeline);

    @Override
    public synchronized ReadOnlyTagList getTags() {
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

    public BUTTONRECORD getButtonRecordAt(int frame, int depth, boolean addIfNotExists) {
        for (BUTTONRECORD rec : getRecords()) {
            if (rec.placeDepth != depth) {
                continue;
            }

            switch (frame) {
                case FRAME_UP:
                    if (rec.buttonStateUp) {
                        return rec;
                    }
                    break;
                case FRAME_OVER:
                    if (rec.buttonStateOver) {
                        return rec;
                    }
                    break;
                case FRAME_DOWN:
                    if (rec.buttonStateDown) {
                        return rec;
                    }
                    break;
                case FRAME_HITTEST:
                    if (rec.buttonStateHitTest) {
                        return rec;
                    }
                    break;
            }
        }

        if (addIfNotExists) {
            BUTTONRECORD newRecord = new BUTTONRECORD(swf, this);
            switch (frame) {
                case FRAME_UP:
                    newRecord.buttonStateUp = true;
                    break;
                case FRAME_OVER:
                    newRecord.buttonStateOver = true;
                    break;
                case FRAME_DOWN:
                    newRecord.buttonStateDown = true;
                    break;
                case FRAME_HITTEST:
                    newRecord.buttonStateHitTest = true;
                    break;
            }
            newRecord.placeDepth = depth;
            getRecords().add(newRecord);
            return newRecord;
        }

        return null;
    }
    
    public void packRecords() {
        List<BUTTONRECORD> records = new ArrayList<>();
        for (int i = records.size() - 1; i >= 0; i--) {
            BUTTONRECORD rec = records.get(i);
            if (rec.isEmpty()) {
                records.remove(i);
            }
        }
    }
    
    public Set<Integer> getEmptyFrames() {        
        Set<Integer> ret = new LinkedHashSet<>();
        ret.add(FRAME_UP);
        ret.add(FRAME_OVER);
        ret.add(FRAME_DOWN);
        ret.add(FRAME_HITTEST);
        for (BUTTONRECORD rec : getRecords()) {
            if (rec.buttonStateUp) {
                ret.remove(FRAME_UP);
            }
            if (rec.buttonStateOver) {
                ret.remove(FRAME_OVER);
            }
            if (rec.buttonStateDown) {
                ret.remove(FRAME_DOWN);
            }
            if (rec.buttonStateHitTest) {
                ret.remove(FRAME_HITTEST);
            }
        }
        return ret;
    }
    
    
    public boolean isFrameEmpty(int frame) {
        
        return true;
    }

    public void setRecordFromPlaceObject(int frame, PlaceObjectTypeTag placeTag) {
        BUTTONRECORD selectedRecord = null;
        List<BUTTONRECORD> records = getRecords();
        loopRecords:
        for (BUTTONRECORD rec : records) {
            if (rec.placeDepth != placeTag.getDepth()) {
                continue;
            }

            switch (frame) {
                case FRAME_UP:
                    if (rec.buttonStateUp) {
                        selectedRecord = rec;
                        break loopRecords;
                    }
                    break;
                case FRAME_OVER:
                    if (rec.buttonStateOver) {
                        selectedRecord = rec;
                        break loopRecords;
                    }
                    break;
                case FRAME_DOWN:
                    if (rec.buttonStateDown) {
                        selectedRecord = rec;
                        break loopRecords;
                    }
                    break;
                case FRAME_HITTEST:
                    if (rec.buttonStateHitTest) {
                        selectedRecord = rec;
                        break loopRecords;
                    }
                    break;
            }
        }

        if (selectedRecord == null) {
            selectedRecord = new BUTTONRECORD(swf, this);
            switch (frame) {
                case FRAME_UP:
                    selectedRecord.buttonStateUp = true;
                    break;
                case FRAME_OVER:
                    selectedRecord.buttonStateOver = true;
                    break;
                case FRAME_DOWN:
                    selectedRecord.buttonStateDown = true;
                    break;
                case FRAME_HITTEST:
                    selectedRecord.buttonStateHitTest = true;
                    break;
            }
            records.add(selectedRecord);
        }
        selectedRecord.fromPlaceObject(placeTag);
    }
}
