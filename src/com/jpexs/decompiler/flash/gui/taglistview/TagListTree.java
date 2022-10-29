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
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class TagListTree extends JTree {

    private List<SWFList> swfs;
    
    private TagListTreeNode root;
    
    private boolean initialized = false;
    
    public TagListTree() {
        setCellRenderer(new TagListTreeCellRenderer());
        setRootVisible(false);
        setShowsRootHandles(true);
        setRowHeight(Math.max(getFont().getSize() + 5, 16));

        if (View.isOceanic()) {
            setBackground(Color.white);
            setUI(new BasicTreeUI() {
                {
                    setHashColor(Color.gray);
                }
            });
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
       
    public void setSwfs(List<SWFList> swfs) {                
        this.swfs = swfs;
        if (updateSwfs()) {
            initialized = true;
        }
    }
    
    public boolean updateSwfs() {
        if (swfs == null) {
            return false;
        }
        root = new TagListTreeNode();
        root.setData("root");
        for (SWFList swfList : swfs) {
            populateNodes(root, swfList);
        }

        List<List<String>> expandedNodes = View.getExpandedNodes(this);            
        setModel(new DefaultTreeModel(root));
        View.expandTreeNodes(this, expandedNodes);
        return true;
    }

    private void populateNodes(TagListTreeNode parent, Object obj) {
        if (obj instanceof SWFList) {
            SWFList list = (SWFList) obj;
            TagListTreeNode node = new TagListTreeNode();
            node.setData(list);
            node.setParent(parent);
            parent.addChild(node);

            if (!list.isBundle()) {
                node.setData(list.get(0));
                populateTimelineNodes(list.get(0), node, list.get(0));
            } else {
                for (SWF swf : list) {
                    TagListTreeNode subNode = new TagListTreeNode();
                    subNode.setData(swf);
                    subNode.setParent(node);
                    node.addChild(subNode);
                    parent.addChild(node);
                    populateTimelineNodes(swf, node, swf);
                }
            }
        }
    }

    private void populateTimelineNodes(SWF swf, TagListTreeNode root, Timelined timelined) {
        int f = 0;

        Timeline timeline = timelined.getTimeline();
        
        TagListTreeNode frameNode = new TagListTreeNode();
        frameNode.setData(timeline.getFrame(0));
        frameNode.setParent(root);
        root.addChild(frameNode);

        for (Tag t : timelined.getTags()) {
            TagListTreeNode node = new TagListTreeNode();
            node.setData(t);
            node.setParent(frameNode);
            frameNode.addChild(node);

            if (t instanceof DefineSpriteTag) {
                populateTimelineNodes(swf, node, (DefineSpriteTag) t);
            }            
            if (t instanceof ShowFrameTag) {
                f++;
                frameNode = new TagListTreeNode();
                frameNode.setData(timeline.getFrame(f));
                frameNode.setParent(root);
                root.addChild(frameNode);
            }
        }
        if (frameNode.isLeaf()) {
            root.removeChild(root.getChildCount() - 1);
        }
    }

    public void expandRoot() {
        expandPath(new TreePath(new Object[]{root}));
    }

    public void expandFirstLevelNodes() {
        int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            expandPath(new TreePath(new Object[]{root, root.getChildAt(i)}));
        }
    }
    
            
    public TreePath getPathForData(Object data) {
        TagListTreeNode node = getNodeForData(data);
        if (node == null) {
            return new TreePath(new Object[0]);
        }
        return getPathForNode(node);
    }
    
    public TreePath getPathForNode(TagListTreeNode node) {
        List<Object> path = new ArrayList<>();        
        while (node.getParent() != null) {
            path.add(0, node);
            node = (TagListTreeNode)node.getParent();
        }
        path.add(0, node);
        Object[] pathArr = path.toArray(new Object[path.size()]);
        return new TreePath(pathArr);
    }
    
    public TagListTreeNode getNodeForData(Object data) {
        return getNodeForData(root, data);
    }
    
    private TagListTreeNode getNodeForData(TagListTreeNode startNode, Object data) {
        if (startNode.getData() == data) {
            return startNode;
        }
        for (int i = 0; i < startNode.getChildCount(); i++) {            
            TagListTreeNode foundNode = getNodeForData((TagListTreeNode)startNode.getChildAt(i), data);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }
    
    public List<TreeItem> getSelected() {
        TreePath[] paths = getSelectionPaths();
        Set<TreeItem> selected = new LinkedHashSet<>();
        for (TreePath path : paths) {
            selected.add((TreeItem)((TagListTreeNode)path.getLastPathComponent()).getData());
        }
        List<TreeItem> ret = new ArrayList<>(selected);
        return ret;
    }
    
    public List<TreeItem> getAllSelected() {
        TreePath[] paths = getSelectionPaths();
        Set<TreeItem> selected = new LinkedHashSet<>();
        for (TreePath path : paths) {
            populateSelected((TagListTreeNode)path.getLastPathComponent(), selected);
        }
        List<TreeItem> ret = new ArrayList<>(selected);
        return ret;
    }
    
    public List<TreeItem> getSelection(SWF swf) {
        Set<TreeItem> selected = new HashSet<>();
        populateSelectedSwf(swf, root, selected);
        return new ArrayList<>(selected);
    }
    
    public void populateSelectedSwf(SWF swf, TagListTreeNode node, Set<TreeItem> selected){
        TreeItem item = (TreeItem) node.getData();
        if (item.getSwf() == swf) {
            selected.add(item);
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            populateSelected((TagListTreeNode)node.getChildAt(i), selected);
        }
    }
    
    public void populateSelected(TagListTreeNode node, Set<TreeItem> selected){
        selected.add((TreeItem)node.getData());
        for (int i = 0; i < node.getChildCount(); i++) {
            populateSelected((TagListTreeNode)node.getChildAt(i), selected);
        }
    }
}
