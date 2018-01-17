/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.BoundedTag;

/**
 *
 * @author JPEXS
 */
public interface Timelined extends BoundedTag {

    public Timeline getTimeline();

    public void resetTimeline();

    public void setModified(boolean value);

    public ReadOnlyTagList getTags();

    public void removeTag(int index);

    public void removeTag(Tag tag);

    public void addTag(Tag tag);

    public void addTag(int index, Tag tag);

    public void replaceTag(int index, Tag newTag);
}
