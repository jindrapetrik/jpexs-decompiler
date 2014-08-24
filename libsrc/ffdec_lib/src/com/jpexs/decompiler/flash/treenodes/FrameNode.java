/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.treenodes;

import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.treeitems.FrameNodeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FrameNode extends TreeNode {

    public boolean scriptsNode = false;

    public FrameNode(FrameNodeItem item, List<Tag> innerTags, boolean scriptsNode) {
        super(item);
        this.scriptsNode = scriptsNode;
        if (innerTags != null) {
            for (Tag tag : innerTags) {
                subNodes.add(new TagNode(tag));
            }
        }
    }

    @Override
    public FrameNodeItem getItem() {
        return (FrameNodeItem) item;
    }

}
