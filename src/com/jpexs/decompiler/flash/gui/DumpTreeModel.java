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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.dumpview.DumpInfo;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class DumpTreeModel implements TreeModel {

    private final DumpInfo root;
            
    public DumpTreeModel(List<SWFList> swfs) {
        DumpInfo root = new DumpInfo("root", "", null, 0, 0);
        for (SWFList swfList : swfs) {
            for (SWF swf : swfList) {
                swf.dumpInfo.name = swf.getFileTitle();
                root.childInfos.add(swf.dumpInfo);
            }
        }
        this.root = root;
    }
    
    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object o, int i) {
        return ((DumpInfo) o).childInfos.get(i);
    }

    @Override
    public int getChildCount(Object o) {
        return ((DumpInfo) o).childInfos.size();
    }

    @Override
    public boolean isLeaf(Object o) {
        return ((DumpInfo) o).childInfos.isEmpty();
    }

    @Override
    public void valueForPathChanged(TreePath tp, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIndexOfChild(Object o, Object o1) {
        return ((DumpInfo) o).childInfos.indexOf(o1);
    }

    @Override
    public void addTreeModelListener(TreeModelListener tl) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener tl) {
    }
    
}
