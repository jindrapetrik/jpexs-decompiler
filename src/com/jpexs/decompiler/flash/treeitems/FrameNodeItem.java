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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;

/**
 *
 * @author JPEXS
 */
public class FrameNodeItem implements TreeItem {

    private final SWF swf;
    private final int frame;
    private final Tag parent;
    private final ShowFrameTag showFrameTag;
    private final boolean display;

    public FrameNodeItem(SWF swf, int frame, Tag parent, ShowFrameTag showFrameTag, boolean display) {

        this.swf = swf;
        this.frame = frame;
        this.parent = parent;
        this.showFrameTag = showFrameTag;
        this.display = display;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    public boolean isDisplayed() {
        return display;
    }

    @Override
    public String toString() {
        return "frame " + frame;
    }

    public int getFrame() {
        return frame;
    }

    public Tag getParent() {
        return parent;
    }

    public ShowFrameTag getShowFrameTag() {
        return showFrameTag;
    }
}
