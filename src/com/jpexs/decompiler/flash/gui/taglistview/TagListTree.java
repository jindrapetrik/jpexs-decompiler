/*
 * Copyright (C) 2022 JPEXS
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
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import static com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree.getSelection;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TagListTree extends AbstractTagTree {
    
    public TagListTree(TagListTreeModel model, MainPanel mainPanel) {
        super(model, mainPanel);        
        setCellRenderer(new TagListTreeCellRenderer());        
    }               
    
    @Override
    public List<TreeItem> getSelection(SWF swf) {
        return getSelection(swf, getAllSelected());
    }
    
    @Override
    public TagListTreeModel getModel() {
        return (TagListTreeModel) super.getModel();
    }

    @Override
    public List<Integer> getNestedTagIds(Tag obj) {
        if (obj instanceof DefineSpriteTag) {
            return getSpriteNestedTagIds();
        }
        return new ArrayList<>();
    }

    private List<Integer> getSpriteNestedTagIds() {
        return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                    RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                    StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                    SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID,
                    DefineScalingGridTag.ID); //scaling grid? FIXME?
    }
    
    @Override
    public List<Integer> getFrameNestedTagIds(boolean inSprite) {        
        if (inSprite) {
            return getSpriteNestedTagIds();
        }
        return null; //null = all possible tags
    }
}
