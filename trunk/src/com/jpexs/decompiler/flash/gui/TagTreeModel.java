/*
 *  Copyright (C) 2010-2013 JPEXS
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
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.treenodes.ClassesListNode;
import com.jpexs.decompiler.flash.gui.abc.treenodes.TreeElement;
import com.jpexs.decompiler.flash.gui.treenodes.SWFBundleNode;
import com.jpexs.decompiler.flash.gui.treenodes.SWFContainerNode;
import com.jpexs.decompiler.flash.gui.treenodes.SWFNode;
import com.jpexs.decompiler.flash.gui.treenodes.StringNode;
import com.jpexs.decompiler.flash.gui.treenodes.TagTreeRoot;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.treeitems.FrameNodeItem;
import com.jpexs.decompiler.flash.treeitems.StringItem;
import com.jpexs.decompiler.flash.treeitems.TreeElementItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.treenodes.ContainerNode;
import com.jpexs.decompiler.flash.treenodes.FrameNode;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

    private final TagTreeRoot root = new TagTreeRoot();
    private final List<SWFContainerNode> swfs;
    private final Map<SWF, SWFNode> swfToSwfNode;
    private final MainFrame mainFrame;

    public TagTreeModel(MainFrame mainFrame, List<SWFList> swfs) {
        this.mainFrame = mainFrame;
        this.swfs = new ArrayList<>();
        swfToSwfNode = new HashMap<>();
        for (SWFList swfList : swfs) {
            if (swfList.isBundle) {
                SWFBundleNode bundleNode = new SWFBundleNode(swfList, swfList.name);
                for (SWF swf : swfList) {
                    bundleNode.swfs.add(createSwfNode(swf));
                }
                this.swfs.add(bundleNode);
            } else {
                SWF swf = swfList.get(0);
                this.swfs.add(createSwfNode(swf));
            }
        }
    }

    private SWFNode createSwfNode(SWF swf) {
        List<ContainerItem> objs = new ArrayList<>();
        objs.addAll(swf.tags);
        ClassesListTreeModel classTreeModel = new ClassesListTreeModel(swf);
        SWFNode swfNode = new SWFNode(swf, new File(swf.getFileTitle()).getName());
        swfNode.list = createTagList(objs, null, swf, swfNode, classTreeModel);
        swfToSwfNode.put(swf, swfNode);
        return swfNode;
    }
    
    private String translate(String key) {
        return mainFrame.translate(key);
    }

    public List<TreeNode> getTagNodesWithType(List<? extends ContainerItem> list, TreeNodeType type, Tag parent, boolean display) {
        List<TreeNode> ret = new ArrayList<>();
        int frameCnt = 0;
        for (ContainerItem o : list) {
            TreeNodeType ttype = TagTree.getTreeNodeType(o);
            if (ttype == TreeNodeType.SHOW_FRAME && type == TreeNodeType.FRAME) {
                frameCnt++;
                ret.add(new FrameNode(new FrameNodeItem(o.getSwf(), frameCnt, parent, display)));
            } else if (type == ttype) {
                ret.add(new ContainerNode(o));
            }
        }
        return ret;
    }

    private List<TreeNode> createTagList(List<ContainerItem> list, Tag parent, SWF swf, SWFNode swfNode, ClassesListTreeModel classTreeModel) {
        boolean hasAbc = swf.abcList != null && !swf.abcList.isEmpty();

        List<TreeNode> ret = new ArrayList<>();
        List<TreeNode> frames = getTagNodesWithType(list, TreeNodeType.FRAME, parent, true);
        List<TreeNode> shapes = getTagNodesWithType(list, TreeNodeType.SHAPE, parent, true);
        List<TreeNode> morphShapes = getTagNodesWithType(list, TreeNodeType.MORPH_SHAPE, parent, true);
        List<TreeNode> sprites = getTagNodesWithType(list, TreeNodeType.SPRITE, parent, true);
        List<TreeNode> buttons = getTagNodesWithType(list, TreeNodeType.BUTTON, parent, true);
        List<TreeNode> images = getTagNodesWithType(list, TreeNodeType.IMAGE, parent, true);
        List<TreeNode> fonts = getTagNodesWithType(list, TreeNodeType.FONT, parent, true);
        List<TreeNode> texts = getTagNodesWithType(list, TreeNodeType.TEXT, parent, true);
        List<TreeNode> movies = getTagNodesWithType(list, TreeNodeType.MOVIE, parent, true);
        List<TreeNode> sounds = getTagNodesWithType(list, TreeNodeType.SOUND, parent, true);
        List<TreeNode> binaryData = getTagNodesWithType(list, TreeNodeType.BINARY_DATA, parent, true);

        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i).getItem() instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = new ArrayList<>();
                SWF.populateSoundStreamBlocks(list, (Tag) sounds.get(i).getItem(), blocks);
                if (blocks.isEmpty()) {
                    sounds.remove(i);
                    i--;
                }
            }
        }

        for (TreeNode n : sprites) {
            Tag tag = n.getItem() instanceof Tag ? (Tag) n.getItem() : null;
            n.subNodes = getTagNodesWithType(((DefineSpriteTag) n.getItem()).subTags, TreeNodeType.FRAME, tag, true);
        }

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
        for (ContainerItem t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
        }

        List<TreeNode> actionScript = SWF.createASTagList(list, null);
        StringNode textsNode = new StringNode(new StringItem(translate("node.texts"), swf));
        textsNode.subNodes.addAll(texts);

        StringNode imagesNode = new StringNode(new StringItem(translate("node.images"), swf));
        imagesNode.subNodes.addAll(images);

        StringNode moviesNode = new StringNode(new StringItem(translate("node.movies"), swf));
        moviesNode.subNodes.addAll(movies);

        StringNode soundsNode = new StringNode(new StringItem(translate("node.sounds"), swf));
        soundsNode.subNodes.addAll(sounds);


        StringNode binaryDataNode = new StringNode(new StringItem(translate("node.binaryData"), swf));
        binaryDataNode.subNodes.addAll(binaryData);

        StringNode fontsNode = new StringNode(new StringItem(translate("node.fonts"), swf));
        fontsNode.subNodes.addAll(fonts);


        StringNode spritesNode = new StringNode(new StringItem(translate("node.sprites"), swf));
        spritesNode.subNodes.addAll(sprites);

        StringNode shapesNode = new StringNode(new StringItem(translate("node.shapes"), swf));
        shapesNode.subNodes.addAll(shapes);

        StringNode morphShapesNode = new StringNode(new StringItem(translate("node.morphshapes"), swf));
        morphShapesNode.subNodes.addAll(morphShapes);

        StringNode buttonsNode = new StringNode(new StringItem(translate("node.buttons"), swf));
        buttonsNode.subNodes.addAll(buttons);

        StringNode framesNode = new StringNode(new StringItem(translate("node.frames"), swf));
        framesNode.subNodes.addAll(frames);

        TreeNode actionScriptNode;
        if (hasAbc) {
            actionScriptNode = new ClassesListNode(classTreeModel);
        } else {
            actionScriptNode = new StringNode(new StringItem(translate("node.scripts"), swf));
            actionScriptNode.subNodes.addAll(actionScript);
        }
        swfNode.scriptsNode = actionScriptNode;

        if (!shapesNode.subNodes.isEmpty()) {
            ret.add(shapesNode);
        }
        if (!morphShapesNode.subNodes.isEmpty()) {
            ret.add(morphShapesNode);
        }
        if (!spritesNode.subNodes.isEmpty()) {
            ret.add(spritesNode);
        }
        if (!textsNode.subNodes.isEmpty()) {
            ret.add(textsNode);
        }
        if (!imagesNode.subNodes.isEmpty()) {
            ret.add(imagesNode);
        }
        if (!moviesNode.subNodes.isEmpty()) {
            ret.add(moviesNode);
        }
        if (!soundsNode.subNodes.isEmpty()) {
            ret.add(soundsNode);
        }
        if (!buttonsNode.subNodes.isEmpty()) {
            ret.add(buttonsNode);
        }
        if (!fontsNode.subNodes.isEmpty()) {
            ret.add(fontsNode);
        }
        if (!binaryDataNode.subNodes.isEmpty()) {
            ret.add(binaryDataNode);
        }
        if (!framesNode.subNodes.isEmpty()) {
            ret.add(framesNode);
        }

        if ((!actionScriptNode.subNodes.isEmpty()) || hasAbc) {
            ret.add(actionScriptNode);
        }

        return ret;
    }

    private List<TreeNode> searchTag(TreeItem obj, TreeNode parent, List<TreeNode> path) {
        List<TreeNode> ret = null;
        int cnt = getChildCount(parent);
        for (int i = 0; i < cnt; i++) {
            TreeNode n = getChild(parent, i);
            List<TreeNode> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            if (n instanceof TreeElement) {
                TreeElement te = (TreeElement) n;
                TreeElementItem it = te.getItem();
                if (obj == it) {
                    return newPath;
                }
            }   
            if (n instanceof TreeNode) {
                TreeNode nd = (TreeNode) n;
                if (nd.getItem() == obj) {
                    return newPath;
                }
            }
            ret = searchTag(obj, n, newPath);
            if (ret != null) {
                return ret;
            }
        }
        return ret;
    }

    public SWFNode getSwfNode(SWF swf) {
        return swfToSwfNode.get(swf);
    }

    public TreePath getTagPath(TreeItem obj) {
        List<TreeNode> path = new ArrayList<>();
        path.add(getRoot());
        path = searchTag(obj, getRoot(), path);
        TreePath tp = new TreePath(path.toArray(new Object[path.size()]));
        return tp;
    }

    @Override
    public TreeNode getRoot() {
        return root;
    }

    @Override
    public TreeNode getChild(Object parent, int index) {
        TreeNode parentNode = (TreeNode) parent;
        if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getChild(index);
        } else {
            if (parentNode.getItem() instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) parentNode.getItem();
                return clt.getChild(clt.getRoot(), index);
            }
        }
        if (parent == root) {
            return swfs.get(index);
        } else if (parent instanceof SWFBundleNode) {
            return ((SWFBundleNode) parent).swfs.get(index);
        } else if (parent instanceof SWFNode) {
            return ((SWFNode) parent).list.get(index);
        }
        return parentNode.subNodes.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TreeNode parentNode = (TreeNode) parent;
        if (parent == root) {
            return swfs.size();
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getChildCount();
        } else if (parent instanceof SWFBundleNode) {
            return ((SWFBundleNode) parent).swfs.size();
        } else if (parent instanceof SWFNode) {
            return ((SWFNode) parent).list.size();
        } else {
            if (parentNode.getItem() instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) parentNode.getItem();
                return clt.getChildCount(clt.getRoot());
            }
            return parentNode.subNodes.size();
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeNode parentNode = (TreeNode) parent;
        if (parent == root) {
            return swfs.indexOf(child);
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getIndexOfChild((TreeElement) child);
        } else if (parent instanceof SWFBundleNode) {
            return ((SWFBundleNode) parent).swfs.indexOf(child);
        } else if (parent instanceof SWFNode) {
            return ((SWFNode) parent).list.indexOf(child);
        } else {
            if (parentNode.getItem() instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) parentNode.getItem();
                return clt.getIndexOfChild(clt.getRoot(), child);
            }
            return parentNode.subNodes.indexOf(child);
        } 
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
