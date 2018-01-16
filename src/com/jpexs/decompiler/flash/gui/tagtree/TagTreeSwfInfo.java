/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class TagTreeSwfInfo {

    public List<TreeItem> folders;

    public List<FolderItem> emptyFolders;

    public Map<Integer, List<TreeItem>> mappedTags;

    public Map<Tag, TagScript> tagScriptCache;
}
