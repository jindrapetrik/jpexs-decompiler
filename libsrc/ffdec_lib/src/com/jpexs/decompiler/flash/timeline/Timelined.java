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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;

/**
 * Represents an object which can be created timeline from. It has list of tags
 * which can be listed and/or modified.
 *
 * @author JPEXS
 */
public interface Timelined extends BoundedTag {

    /**
     * Gets SWF which this timelined is part of.
     *
     * @return SWF
     */
    public SWF getSwf();

    /**
     * Gets the timeline object.
     *
     * @return Timeline
     */
    public Timeline getTimeline();

    /**
     * Resets timeline so it must be recalculated freshly.
     */
    public void resetTimeline();

    /**
     * Sets modification flag.
     *
     * @param value True if modified
     */
    public void setModified(boolean value);
    
    /**
     * Gets modification flag.
     * @return  True of modified
     */
    public boolean isModified();

    /**
     * Gets tags.
     *
     * @return Tags
     */
    public ReadOnlyTagList getTags();

    /**
     * Removes tag by index.
     *
     * @param index Index
     */
    public void removeTag(int index);

    /**
     * Removes tag.
     *
     * @param tag Tag
     */
    public void removeTag(Tag tag);

    /**
     * Adds tag.
     *
     * @param tag Tag
     */
    public void addTag(Tag tag);

    /**
     * Adds tag at the specified index.
     *
     * @param index Index
     * @param tag Tag
     */
    public void addTag(int index, Tag tag);

    /**
     * Replaces tag at the specified index.
     *
     * @param index Index
     * @param newTag New tag
     */
    public void replaceTag(int index, Tag newTag);

    /**
     * Replaces old tag with new tag.
     *
     * @param oldTag Old tag
     * @param newTag New tag
     */
    public void replaceTag(Tag oldTag, Tag newTag);

    /**
     * Gets index of tag.
     *
     * @param tag Tag
     * @return Index or -1 when not found
     */
    public int indexOfTag(Tag tag);

    /**
     * Sets frame count.
     *
     * @param frameCount Frame count
     */
    public void setFrameCount(int frameCount);

    /**
     * Gets frame count.
     *
     * @return Frame count
     */
    public int getFrameCount();
}
