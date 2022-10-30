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
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedAction;
import com.jpexs.decompiler.flash.gui.helpers.CollectionChangedEvent;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTreeModel;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.base.ASMSourceContainer;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class TagListTreeModel extends AbstractTagTreeModel {

    private final TagListTreeRoot root = new TagListTreeRoot();

    private List<SWFList> swfs;
    
    private Map<SWF, HeaderItem> swfHeaders = new HashMap<>();         
    
    public TagListTreeModel(List<SWFList> swfs) {
        this.swfs = swfs;
    }           
    
    @Override
    public TreeItem getRoot() {
        return root;
    }
    
    private HeaderItem getSwfHeader(SWF swf) {
        if (swfHeaders.containsKey(swf)) {
            return swfHeaders.get(swf);
        }
        HeaderItem header = new HeaderItem(swf, translate("node.header"));
        swfHeaders.put(swf, header);
        return header;
    }
    
    private String translate(String key) {
        return AppStrings.translate(key);
    }

    @Override
    public TreeItem getChild(Object parent, int index) {
        if(getChildCount(parent) == 0) {
            return null;
        }
        TreeItem parentNode = (TreeItem) parent;       

        if (parentNode == root) {
            SWFList swfList = swfs.get(index);
            if (!swfList.isBundle()) {
                return swfList.get(0);
            }
            return swfList;
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs.get(index);
        } else if (parentNode instanceof SWF) {
            if (index == 0) {
                return getSwfHeader((SWF) parentNode);
            }
            return ((SWF) parentNode).getTimeline().getFrame(index - 1);
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((DefineSpriteTag) parentNode).getTimeline().getFrame(index);
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).allInnerTags.get(index);
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return ((DefineBinaryDataTag) parentNode).innerSwf;
        } else if (parentNode instanceof ASMSourceContainer) {
            return ((ASMSourceContainer)parentNode).getSubItems().get(index);
        }
        throw new Error("Unsupported parent type: " + parentNode.getClass().getName());
    }

    @Override
    public int getChildCount(Object parent) {
        TreeItem parentNode = (TreeItem) parent;
        if (parentNode == root) {
            return  swfs.size();
        } else if (parentNode instanceof SWFList) {
            return  ((SWFList) parentNode).swfs.size();
        } else if (parentNode instanceof SWF) {
            return ((SWF) parentNode).getTimeline().getFrameCount() + 1;
        } else if (parentNode instanceof HeaderItem) {
            return  0;
        } else if (parentNode instanceof Frame) {
            return  ((Frame) parentNode).allInnerTags.size();
        } else if (parentNode instanceof DefineSpriteTag) {
            return  ((DefineSpriteTag) parentNode).getTimeline().getFrameCount();
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return  (((DefineBinaryDataTag) parentNode).innerSwf == null ? 0 : 1);
        } else if (parentNode instanceof ASMSourceContainer) {
            return ((ASMSourceContainer)parentNode).getSubItems().size();
        }

        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if(getChildCount(parent) == 0) {
            return -1;
        }
        TreeItem parentNode = (TreeItem) parent;       

        if (parentNode == root) {
             SWFList swfList = child instanceof SWFList
                    ? (SWFList) child
                    : ((SWF) child).swfList;
            return swfs.indexOf(swfList);
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs.indexOf(child);
        } else if (parentNode instanceof SWF) {
            
            HeaderItem header = getSwfHeader((SWF) parentNode);
            if (header == child) {
                return 0;
            }
            return ((Frame)child).frame + 1;
        } else if (parentNode instanceof DefineSpriteTag) {
            if (((Frame)child).timeline != ((DefineSpriteTag)parentNode).getTimeline()) {
                return -1;
            }
            return ((Frame)child).frame;
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).allInnerTags.indexOf(child);
        } else if (parentNode instanceof DefineBinaryDataTag) {
            return ((DefineBinaryDataTag) parentNode).innerSwf == child ? 0 : -1;
        } else if (parentNode instanceof ASMSourceContainer) {
            return ((ASMSourceContainer)parentNode).getSubItems().indexOf(child);
        }
        return -1;
    }

        
    
    @Override
    public void updateSwfs(CollectionChangedEvent e) {
        if (e.getAction() != CollectionChangedAction.ADD) {
            List<SWF> toRemove = new ArrayList<>();
            for (SWF swf : swfHeaders.keySet()) {
                SWF swf2 = swf.getRootSwf();
                if (swf2 != null && !swfs.contains(swf2.swfList)) {
                    toRemove.add(swf);
                }
            }

            for (SWF swf : toRemove) {
                swfHeaders.remove(swf);
            }
        }

        switch (e.getAction()) {
            case ADD: {
                TreePath rootPath = new TreePath(new Object[]{root});
                fireTreeNodesInserted(new TreeModelEvent(this, rootPath, new int[]{e.getNewIndex()}, new Object[]{e.getNewItem()}));
                break;
            }
            case REMOVE: {
                TreePath rootPath = new TreePath(new Object[]{root});
                fireTreeNodesRemoved(new TreeModelEvent(this, rootPath, new int[]{e.getOldIndex()}, new Object[]{e.getOldItem()}));
                break;
            }
            default:
                fireTreeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
        }
    }
    
    private Frame searchForFrame(Object parent, SWF swf, Timelined t, int frame) {
        int childCount = getChildCount(parent);
        Frame lastVisibleFrame = null;
        for (int i = 0; i < childCount; i++) {
            TreeItem child = getChild(parent, i);
            if ((child instanceof DefineSpriteTag) && child == t) {
                Frame si = searchForFrame(child, swf, t, frame);
                if (si != null) {
                    return si;
                }
            }
            if (child instanceof Frame) {
                Frame f = (Frame) child;
                if (f.frame <= frame) {
                    lastVisibleFrame = f;
                }
            }            
        }
        return lastVisibleFrame;
    }

    @Override
    public Frame getFrame(SWF swf, Timelined t, int frame) {
        return searchForFrame(swf, swf, t, frame);
    }

    @Override
    public List<? extends TreeItem> getAllChildren(Object parent) {
        TreeItem parentNode = (TreeItem) parent;
        if (parentNode == root) {
            List<TreeItem> result = new ArrayList<>(swfs.size());
            for (SWFList swfList : swfs) {
                if (!swfList.isBundle()) {
                    result.add(swfList.get(0));
                }
                result.add(swfList);
            }
            return result;
        } else if (parentNode instanceof SWFList) {
            return ((SWFList) parentNode).swfs;
        } else if (parentNode instanceof SWF) {
            List<TreeItem> ret = new ArrayList<>();
            ret.add(getSwfHeader((SWF)parentNode));
            ret.addAll(((SWF)parentNode).getTimeline().getFrames());
            return ret;
        } else if (parentNode instanceof Frame) {
            return ((Frame) parentNode).allInnerTags;
        } else if (parentNode instanceof DefineSpriteTag) {
            return ((DefineSpriteTag) parentNode).getTimeline().getFrames();
        } else if (parentNode instanceof DefineBinaryDataTag) {
            DefineBinaryDataTag binaryDataTag = (DefineBinaryDataTag) parentNode;
            if (binaryDataTag.innerSwf != null) {
                List<SWF> result = new ArrayList<>(1);
                result.add(((DefineBinaryDataTag) parentNode).innerSwf);
                return result;
            } else {
                return new ArrayList<>(0);
            }
        } else if (parentNode instanceof ASMSourceContainer) {
            return ((ASMSourceContainer)parentNode).getSubItems();
        }

        return new ArrayList<>();
    }
    
    @Override
    protected List<TreeItem> searchTreeItem(TreeItem obj, TreeItem parent, List<TreeItem> path) {
        List<TreeItem> ret = null;
        for (TreeItem n : getAllChildren(parent)) {
            List<TreeItem> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            if (obj.equals(n)) {
                return newPath;
            }

            ret = searchTreeItem(obj, n, newPath);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    @Override
    public TreePath getTreePath(TreeItem obj) {
        List<TreeItem> path = new ArrayList<>();
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
    
    @Override
    public void updateSwf(SWF swf) {
        swfHeaders.clear();
        TreePath changedPath = getTreePath(swf == null ? root : swf);
        fireTreeStructureChanged(new TreeModelEvent(this, changedPath));
    }  
}
