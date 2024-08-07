/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
     * @return
     */
    public SWF getSwf();

    /**
     * Gets the timeline object.
     *
     * @return
     */
    public Timeline getTimeline();

    /**
     * Resets timeline so it must be recalculated freshly.
     */
    public void resetTimeline();

    /**
     * Sets modification flag.
     *
     * @param value
     */
    public void setModified(boolean value);

    /**
     * Gets tags.
     *
     * @return
     */
    public ReadOnlyTagList getTags();

    /**
     * Removes tag by index.
     *
     * @param index
     */
    public void removeTag(int index);

    /**
     * Removes tag.
     *
     * @param tag
     */
    public void removeTag(Tag tag);

    /**
     * Adds tag.
     *
     * @param tag
     */
    public void addTag(Tag tag);

    /**
     * Adds tag at the specified index.
     *
     * @param index
     * @param tag
     */
    public void addTag(int index, Tag tag);

    /**
     * Replaces tag at the specified index.
     *
     * @param index
     * @param newTag
     */
    public void replaceTag(int index, Tag newTag);

    /**
     * Replaces old tag with new tag.
     *
     * @param oldTag
     * @param newTag
     */
    public void replaceTag(Tag oldTag, Tag newTag);

    /**
     * Gets index of tag.
     *
     * @param tag
     * @return Index or -1 when not found
     */
    public int indexOfTag(Tag tag);

    /**
     * Sets frame count.
     *
     * @param frameCount
     */
    public void setFrameCount(int frameCount);

    /**
     * Gets frame count.
     *
     * @return
     */
    public int getFrameCount();
}
