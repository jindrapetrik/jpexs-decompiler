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
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedEvent;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public abstract class AbstractTagTreeModel implements TreeModel {
    
    protected final List<TreeModelListener> listeners = new ArrayList<>();   
    
    public abstract void updateSwfs(CollectionChangedEvent e);
    
    protected void fireTreeNodesRemoved(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(e);
        }
    }

    protected void fireTreeNodesInserted(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(e);
        }
    }

    protected void fireTreeStructureChanged(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(e);
        }
    }
    
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }
    
    public boolean treePathExists(TreePath treePath) {
        TreeItem current = null;
        for (Object o : treePath.getPath()) {
            TreeItem item = (TreeItem) o;
            if (current == null) {
                if (item != getRoot()) {
                    return false;
                }

                current = item;
            } else {
                int idx = getIndexOfChild(current, item);
                if (idx == -1) {
                    return false;
                }

                current = item;
            }
        }

        return true;
    }   
    
    public abstract Frame getFrame(SWF swf, Timelined t, int frame);
    
    @Override
    public abstract TreeItem getChild(Object parent, int index);
    
    public abstract List<? extends TreeItem> getAllChildren(Object parent);
    
    @Override
    public abstract TreeItem getRoot();
       
    protected abstract List<TreeItem> searchTreeItem(TreeItem obj, TreeItem parent, List<TreeItem> path);
    
    public TreePath getTreePath(TreeItem obj) {
        List<TreeItem> path = new ArrayList<>();
        TreeItem root = getRoot();
        path.add(root);
        if (obj != root) {
            path = searchTreeItem(obj, root, path);
        }
        if (path == null) {
            return null;
        }

        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        return tp;
    }
    
    public void updateNode(TreeItem treeItem) {
        TreePath changedPath = getTreePath(treeItem);
        fireTreeStructureChanged(new TreeModelEvent(this, changedPath));
    }

    public void updateNode(TreePath changedPath) {
        fireTreeStructureChanged(new TreeModelEvent(this, changedPath.getParentPath()));
    }
    
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }
    
    public abstract void updateSwf(SWF swf);
    
    public TreeItem getParent(TreeItem obj) {
        return (TreeItem)getTreePath(obj).getParentPath().getLastPathComponent();
    }
}
