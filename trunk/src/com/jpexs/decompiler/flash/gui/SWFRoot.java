/*
 *  Copyright (C) 2013 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.TreeNode;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SWFRoot implements TreeNode {

    private SWF swf;
    private String name;
    public List<TagNode> list;
    public ClassesListTreeModel classTreeModel;

    public SWFRoot(SWF swf, String name, List<TagNode> list) {
        this.swf = swf;
        this.name = name;
        this.list = list;
    }

    @Override
    public SWF getSwf() {
        return swf;
    }

    @Override
    public String toString() {
        return name;
    }
}
