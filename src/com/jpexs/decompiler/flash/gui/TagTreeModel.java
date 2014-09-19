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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.treenodes.ClassesListNode;
import com.jpexs.decompiler.flash.gui.abc.treenodes.TreeElement;
import com.jpexs.decompiler.flash.gui.treenodes.SWFBundleNode;
import com.jpexs.decompiler.flash.gui.treenodes.SWFContainerNode;
import com.jpexs.decompiler.flash.gui.treenodes.SWFNode;
import com.jpexs.decompiler.flash.gui.treenodes.StringNode;
import com.jpexs.decompiler.flash.gui.treenodes.TagTreeRoot;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.timeline.Timeline;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.FrameNodeItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.StringItem;
import com.jpexs.decompiler.flash.treeitems.TreeElementItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.treenodes.FrameNode;
import com.jpexs.decompiler.flash.treenodes.HeaderNode;
import com.jpexs.decompiler.flash.treenodes.TagNode;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

    public static final String FOLDER_TEXTS = "texts";
    public static final String FOLDER_IMAGES = "images";
    public static final String FOLDER_MOVIES = "movies";
    public static final String FOLDER_SOUNDS = "sounds";
    public static final String FOLDER_BINARY_DATA = "binaryData";
    public static final String FOLDER_FONTS = "fonts";
    public static final String FOLDER_SPRITES = "sprites";
    public static final String FOLDER_SHAPES = "shapes";
    public static final String FOLDER_MORPHSHAPES = "morphshapes";
    public static final String FOLDER_BUTTONS = "buttons";
    public static final String FOLDER_FRAMES = "frames";
    public static final String FOLDER_OTHERS = "others";
    public static final String FOLDER_SCRIPTS = "scripts";
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

    public SWFNode createSwfNode(SWF swf) {
        ClassesListTreeModel classTreeModel = new ClassesListTreeModel(swf);
        SWFNode swfNode = new SWFNode(swf, swf.getShortFileName());
        swfNode.list = createTagList(swf.tags, swf, swfNode, classTreeModel);
        swfToSwfNode.put(swf, swfNode);
        return swfNode;
    }

    private String translate(String key) {
        return mainFrame.translate(key);
    }

    
    private List<TreeNode> getSoundStreams(DefineSpriteTag sprite){
        List<TreeNode> ret=new ArrayList<>();
        for(Tag t:sprite.subTags){
            if(t instanceof SoundStreamHeadTypeTag){
                ret.add(new TagNode(t));
            }
        }
        return ret;
    }
    
    private List<TreeNode> createTagList(List<Tag> list, SWF swf, SWFNode swfNode, ClassesListTreeModel classTreeModel) {
        boolean hasAbc = swf.abcList != null && !swf.abcList.isEmpty();

        List<TreeNode> ret = new ArrayList<>();
        List<TreeNode> frames = new ArrayList<>();
        List<TreeNode> shapes = new ArrayList<>();
        List<TreeNode> morphShapes = new ArrayList<>();
        List<TreeNode> sprites = new ArrayList<>();
        List<TreeNode> buttons = new ArrayList<>();
        List<TreeNode> images = new ArrayList<>();
        List<TreeNode> fonts = new ArrayList<>();
        List<TreeNode> texts = new ArrayList<>();
        List<TreeNode> movies = new ArrayList<>();
        List<TreeNode> sounds = new ArrayList<>();
        List<TreeNode> binaryData = new ArrayList<>();
        List<TreeNode> others = new ArrayList<>();

        List<TreeNode> actionScript = SWF.createASTagList(list, swf);
        List<Tag> actionScriptTags = new ArrayList<>();
        SWF.getTagsFromTreeNodes(actionScript, actionScriptTags);

        for (Tag t : list) {
            TreeNodeType ttype = TagTree.getTreeNodeType(t);
            switch (ttype) {
                case SHAPE:
                    shapes.add(new TagNode(t));
                    break;
                case MORPH_SHAPE:
                    morphShapes.add(new TagNode(t));
                    break;
                case SPRITE:
                    sprites.add(new TagNode(t));
                    sounds.addAll(getSoundStreams((DefineSpriteTag)t));
                    break;
                case BUTTON:
                    buttons.add(new TagNode(t));
                    break;
                case IMAGE:
                    images.add(new TagNode(t));
                    break;
                case FONT:
                    fonts.add(new TagNode(t));
                    break;
                case TEXT:
                    texts.add(new TagNode(t));
                    break;
                case MOVIE:
                    movies.add(new TagNode(t));
                    break;
                case SOUND:
                    sounds.add(new TagNode(t));
                    break;
                case BINARY_DATA:
                    TagNode bt;
                    binaryData.add(bt = new TagNode(t));                                                            
                    break;
                default:
                    if (!actionScriptTags.contains(t) && t.getId() != ShowFrameTag.ID && !ShowFrameTag.isNestedTagType(t.getId())) {
                        others.add(new TagNode(t));
                    }
                    break;
            }
        }

        Timeline timeline = swf.getTimeline();
        for (int i = 0; i < timeline.getFrameCount(); i++) {
            frames.add(new FrameNode(new FrameNodeItem(swf, i + 1, swf, true), timeline.frames.get(i).innerTags, false));
        }

        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i).getItem() instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = ((SoundStreamHeadTypeTag) sounds.get(i).getItem()).getBlocks();
                if (blocks.isEmpty()) {
                    sounds.remove(i);
                    i--;
                }
            }
        }

        for (TreeNode n : sprites) {
            Timelined timelined = n.getItem() instanceof Timelined ? (Timelined) n.getItem() : null;
            n.subNodes = createSubTagList(((DefineSpriteTag) n.getItem()).subTags, timelined, swf, actionScriptTags);
        }

        StringNode textsNode = new StringNode(new StringItem(translate("node.texts"), FOLDER_TEXTS, swf));
        textsNode.subNodes.addAll(texts);

        StringNode imagesNode = new StringNode(new StringItem(translate("node.images"), FOLDER_IMAGES, swf));
        imagesNode.subNodes.addAll(images);

        StringNode moviesNode = new StringNode(new StringItem(translate("node.movies"), FOLDER_MOVIES, swf));
        moviesNode.subNodes.addAll(movies);

        StringNode soundsNode = new StringNode(new StringItem(translate("node.sounds"), FOLDER_SOUNDS, swf));
        soundsNode.subNodes.addAll(sounds);

        StringNode binaryDataNode = new StringNode(new StringItem(translate("node.binaryData"), FOLDER_BINARY_DATA, swf));
        binaryDataNode.subNodes.addAll(binaryData);

        StringNode fontsNode = new StringNode(new StringItem(translate("node.fonts"), FOLDER_FONTS, swf));
        fontsNode.subNodes.addAll(fonts);

        StringNode spritesNode = new StringNode(new StringItem(translate("node.sprites"), FOLDER_SPRITES, swf));
        spritesNode.subNodes.addAll(sprites);

        StringNode shapesNode = new StringNode(new StringItem(translate("node.shapes"), FOLDER_SHAPES, swf));
        shapesNode.subNodes.addAll(shapes);

        StringNode morphShapesNode = new StringNode(new StringItem(translate("node.morphshapes"), FOLDER_MORPHSHAPES, swf));
        morphShapesNode.subNodes.addAll(morphShapes);

        StringNode buttonsNode = new StringNode(new StringItem(translate("node.buttons"), FOLDER_BUTTONS, swf));
        buttonsNode.subNodes.addAll(buttons);

        StringNode framesNode = new StringNode(new StringItem(translate("node.frames"), FOLDER_FRAMES, swf));
        framesNode.subNodes.addAll(frames);

        StringNode otherNode = new StringNode(new StringItem(translate("node.others"), FOLDER_OTHERS, swf));
        otherNode.subNodes.addAll(others);

        TreeNode actionScriptNode;
        if (hasAbc) {
            actionScriptNode = new ClassesListNode(classTreeModel);
        } else {
            actionScriptNode = new StringNode(new StringItem(translate("node.scripts"), FOLDER_SCRIPTS, swf));
            actionScriptNode.subNodes.addAll(actionScript);
        }
        swfNode.scriptsNode = actionScriptNode;

        ret.add(new HeaderNode(new HeaderItem(swf, AppStrings.translate("node.header"))));

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
        if (!otherNode.subNodes.isEmpty()) {
            ret.add(otherNode);
        }

        if ((!actionScriptNode.subNodes.isEmpty()) || hasAbc) {
            ret.add(actionScriptNode);
        }

        return ret;
    }

    private List<TreeNode> createSubTagList(List<Tag> list, Timelined parent, SWF swf, List<Tag> actionScriptTags) {
        List<TreeNode> ret = new ArrayList<>();
        List<TreeNode> frames = new ArrayList<>();
        List<TreeNode> others = new ArrayList<>();

        for (Tag t : list) {
            TreeNodeType ttype = TagTree.getTreeNodeType(t);
            switch (ttype) {
                default:
                    if (!actionScriptTags.contains(t) && t.getId() != ShowFrameTag.ID && !ShowFrameTag.isNestedTagType(t.getId())) {
                        if (!(t instanceof SoundStreamHeadTypeTag)) {
                            others.add(new TagNode(t));
                        }
                    }
                    break;
            }
        }

        Timeline timeline = ((Timelined) parent).getTimeline();
        for (int i = 0; i < timeline.getFrameCount(); i++) {
            frames.add(new FrameNode(new FrameNodeItem(swf, i + 1, parent, true), timeline.frames.get(i).innerTags, false));
        }

        ret.addAll(frames);

        if (!others.isEmpty()) {
            StringNode otherNode = new StringNode(new StringItem(translate("node.others"), FOLDER_OTHERS, swf));
            otherNode.subNodes.addAll(others);
            ret.add(otherNode);
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

            if (obj instanceof StringItem && n.getItem() instanceof StringItem) {
                // StringItems are always recreated, so compare them by name
                StringItem nds = (StringItem) n.getItem();
                StringItem objs = (StringItem) obj;
                if (objs.getName().equals(nds.getName())) {
                    return newPath;
                }
            } else {
                if (n.getItem() == obj) {
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
        if (path == null) {
            return null;
        }
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
