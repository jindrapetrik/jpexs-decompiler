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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.Exportable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class TagScript implements TreeItem, Exportable {

    private final SWF swf;

    private final Tag tag;

    private final List<TreeItem> frames;

    public TagScript(SWF swf, Tag tag, List<TreeItem> frames) {
        this.swf = swf;
        this.tag = tag;
        this.frames = frames;
    }

    public Tag getTag() {
        return tag;
    }

    public List<TreeItem> getFrames() {
        return frames;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public String toString() {
        return tag.toString();
    }

    @Override
    public String getExportFileName() {
        return tag.getExportFileName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagScript) {
            return Objects.equals(tag, ((TagScript) obj).tag);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean isModified() {
        for (TreeItem f : frames) {
            if (f.isModified()) {
                return true;
            }
        }
        return tag.isModified();
    }
}
