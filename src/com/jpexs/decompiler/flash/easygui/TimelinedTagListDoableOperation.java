/*
 * Copyright (C) 2024 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class TimelinedTagListDoableOperation implements DoableOperation {

    private final Timelined timelined;
    protected List<Tag> tags;
    protected boolean wasModified = false;

    public TimelinedTagListDoableOperation(Timelined timelined) {
        this.timelined = timelined;
    }

    @Override
    public void doOperation() {
        saveTagList();
        wasModified = timelined.isModified();
        timelined.setModified(true);
    }

    protected void saveTagList() {
        tags = timelined.getTags().toArrayList();
    }

    protected void restoreTagList() {
        if (tags != null) {
            ReadOnlyTagList newTags = timelined.getTags();
            int size = newTags.size();
            for (int i = 0; i < size; i++) {
                timelined.removeTag(0);
            }
            for (int i = 0; i < tags.size(); i++) {
                timelined.addTag(tags.get(i));
            }
            timelined.resetTimeline();
            timelined.setFrameCount(timelined.getTimeline().getFrameCount());                            
        }
    }

    @Override
    public void undoOperation() {
        restoreTagList();
        if (!wasModified) {
            timelined.setModified(false);
        }
    }

    @Override
    public abstract String getDescription();

}
