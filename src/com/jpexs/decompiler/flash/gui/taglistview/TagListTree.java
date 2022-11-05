/*
 *  Copyright (C) 2022 JPEXS
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
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import static com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree.getSelection;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
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
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DoInitActionTag) {
            DoInitActionTag tag = (DoInitActionTag) value;
            return DoInitActionTag.NAME + " (" + tag.spriteId + ")";
        }
        if(value != null) {
            String sValue = value.toString();
            if (sValue != null) {
                return sValue;
            }
        }
        return "";
    }

    
}
