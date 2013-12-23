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

import com.jpexs.decompiler.flash.FrameNode;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.StringNode;
import com.jpexs.decompiler.flash.TagNode;
import com.jpexs.decompiler.flash.gui.abc.ABCPanel;
import com.jpexs.decompiler.flash.gui.abc.ClassesListTreeModel;
import com.jpexs.decompiler.flash.gui.abc.TreeElement;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.Container;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

    private TagTreeRoot root = new TagTreeRoot();
    private List<SWFRoot> swfs;
    private MainFrame mainFrame;

    public TagTreeModel(MainFrame mainFrame, List<SWF> swfs, ABCPanel abcPanel) {
        this.mainFrame = mainFrame;
        this.swfs = new ArrayList<>();
        for (SWF swf : swfs) {
            List<ContainerItem> objs = new ArrayList<>();
            objs.addAll(swf.tags);
            List<TagNode> list = createTagList(objs, null, swf);

            SWFRoot swfRoot = new SWFRoot(swf, new File(swf.getFileTitle()).getName(), list);
            this.swfs.add(swfRoot);
        }
    }

    private String translate(String key) {
        return mainFrame.translate(key);
    }

    public List<TagNode> getTagNodesWithType(List<? extends ContainerItem> list, TagType type, Object parent, boolean display) {
        List<TagNode> ret = new ArrayList<>();
        int frameCnt = 0;
        for (ContainerItem o : list) {
            TagType ttype = MainFrame.getTagType(o);
            if (ttype == TagType.SHOW_FRAME && type == TagType.FRAME) {
                frameCnt++;
                ret.add(new TagNode(new FrameNode(o.getSwf(), frameCnt, parent, display), o.getSwf()));
            } else if (type == ttype) {
                ret.add(new TagNode(o, o.getSwf()));
            }
        }
        return ret;
    }

    private List<TagNode> createTagList(List<ContainerItem> list, Object parent, SWF swf) {
        List<TagNode> ret = new ArrayList<>();
        List<TagNode> frames = getTagNodesWithType(list, TagType.FRAME, parent, true);
        List<TagNode> shapes = getTagNodesWithType(list, TagType.SHAPE, parent, true);
        List<TagNode> morphShapes = getTagNodesWithType(list, TagType.MORPH_SHAPE, parent, true);
        List<TagNode> sprites = getTagNodesWithType(list, TagType.SPRITE, parent, true);
        List<TagNode> buttons = getTagNodesWithType(list, TagType.BUTTON, parent, true);
        List<TagNode> images = getTagNodesWithType(list, TagType.IMAGE, parent, true);
        List<TagNode> fonts = getTagNodesWithType(list, TagType.FONT, parent, true);
        List<TagNode> texts = getTagNodesWithType(list, TagType.TEXT, parent, true);
        List<TagNode> movies = getTagNodesWithType(list, TagType.MOVIE, parent, true);
        List<TagNode> sounds = getTagNodesWithType(list, TagType.SOUND, parent, true);
        List<TagNode> binaryData = getTagNodesWithType(list, TagType.BINARY_DATA, parent, true);

        for (int i = 0; i < sounds.size(); i++) {
            if (sounds.get(i).tag instanceof SoundStreamHeadTypeTag) {
                List<SoundStreamBlockTag> blocks = new ArrayList<>();
                SWF.populateSoundStreamBlocks(list, (Tag) sounds.get(i).tag, blocks);
                if (blocks.isEmpty()) {
                    sounds.remove(i);
                    i--;
                }
            }
        }

        for (TagNode n : sprites) {
            n.subItems = getTagNodesWithType(((DefineSpriteTag) n.tag).subTags, TagType.FRAME, n.tag, true);
        }

        List<ExportAssetsTag> exportAssetsTags = new ArrayList<>();
        for (ContainerItem t : list) {
            if (t instanceof ExportAssetsTag) {
                exportAssetsTags.add((ExportAssetsTag) t);
            }
            /*if (t instanceof ASMSource) {
             TagNode tti = new TagNode(t);
             ret.add(tti);
             } else */
            if (t instanceof Container) {
                TagNode tti = new TagNode(t, t.getSwf());
                if (((Container) t).getItemCount() > 0) {
                    List<ContainerItem> subItems = ((Container) t).getSubItems();
                    tti.subItems = createTagList(subItems, t, swf);
                }
                //ret.add(tti);
            }
        }

        List<TagNode> actionScript = SWF.createASTagList(list, null);
        TagNode textsNode = new TagNode(new StringNode(translate("node.texts")), swf);
        textsNode.subItems.addAll(texts);

        TagNode imagesNode = new TagNode(new StringNode(translate("node.images")), swf);
        imagesNode.subItems.addAll(images);

        TagNode moviesNode = new TagNode(new StringNode(translate("node.movies")), swf);
        moviesNode.subItems.addAll(movies);

        TagNode soundsNode = new TagNode(new StringNode(translate("node.sounds")), swf);
        soundsNode.subItems.addAll(sounds);


        TagNode binaryDataNode = new TagNode(new StringNode(translate("node.binaryData")), swf);
        binaryDataNode.subItems.addAll(binaryData);

        TagNode fontsNode = new TagNode(new StringNode(translate("node.fonts")), swf);
        fontsNode.subItems.addAll(fonts);


        TagNode spritesNode = new TagNode(new StringNode(translate("node.sprites")), swf);
        spritesNode.subItems.addAll(sprites);

        TagNode shapesNode = new TagNode(new StringNode(translate("node.shapes")), swf);
        shapesNode.subItems.addAll(shapes);

        TagNode morphShapesNode = new TagNode(new StringNode(translate("node.morphshapes")), swf);
        morphShapesNode.subItems.addAll(morphShapes);

        TagNode buttonsNode = new TagNode(new StringNode(translate("node.buttons")), swf);
        buttonsNode.subItems.addAll(buttons);

        TagNode framesNode = new TagNode(new StringNode(translate("node.frames")), swf);
        framesNode.subItems.addAll(frames);

        TagNode actionScriptNode = new TagNode(new StringNode(translate("node.scripts")), swf);
        actionScriptNode.mark = "scripts";
        actionScriptNode.subItems.addAll(actionScript);

        if (!shapesNode.subItems.isEmpty()) {
            ret.add(shapesNode);
        }
        if (!morphShapesNode.subItems.isEmpty()) {
            ret.add(morphShapesNode);
        }
        if (!spritesNode.subItems.isEmpty()) {
            ret.add(spritesNode);
        }
        if (!textsNode.subItems.isEmpty()) {
            ret.add(textsNode);
        }
        if (!imagesNode.subItems.isEmpty()) {
            ret.add(imagesNode);
        }
        if (!moviesNode.subItems.isEmpty()) {
            ret.add(moviesNode);
        }
        if (!soundsNode.subItems.isEmpty()) {
            ret.add(soundsNode);
        }
        if (!buttonsNode.subItems.isEmpty()) {
            ret.add(buttonsNode);
        }
        if (!fontsNode.subItems.isEmpty()) {
            ret.add(fontsNode);
        }
        if (!binaryDataNode.subItems.isEmpty()) {
            ret.add(binaryDataNode);
        }
        if (!framesNode.subItems.isEmpty()) {
            ret.add(framesNode);
        }

        boolean hasAbc = swf.abcList != null && !swf.abcList.isEmpty();
        
        if (hasAbc) {
            actionScriptNode.subItems.clear();
            actionScriptNode.tag = swf.classTreeModel;
        }
        if ((!actionScriptNode.subItems.isEmpty()) || hasAbc) {
            ret.add(actionScriptNode);
        }

        return ret;
    }

    private List<Object> searchTag(Object obj, Object parent, List<Object> path) {
        List<Object> ret = null;
        int cnt = getChildCount(parent);
        for (int i = 0; i < cnt; i++) {
            Object n = getChild(parent, i);
            List<Object> newPath = new ArrayList<>();
            newPath.addAll(path);
            newPath.add(n);

            if (n instanceof TreeElement) {
                TreeElement te = (TreeElement) n;
                Object it = te.getItem();
                if (obj == it) {
                    return newPath;
                }
            }
            if (n instanceof TagNode) {
                TagNode nd = (TagNode) n;
                if (nd.tag == obj) {
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

    public TreePath getTagPath(Object obj) {
        List<Object> path = new ArrayList<>();
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
        if (parent instanceof TagNode) {
            if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                return clt.getChild(clt.getRoot(), index);
            }
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getChild(index);
        }
        if (parent == root) {
            return swfs.get(index);
        } else if (parent instanceof SWFRoot) {
            return ((SWFRoot) parent).list.get(index);
        }
        return ((TagNode) parent).subItems.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == root) {
            return swfs.size();
        } else if (parent instanceof TagNode) {
            if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                return clt.getChildCount(clt.getRoot());
            }
            return ((TagNode) parent).subItems.size();
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getChildCount();
        } else if (parent instanceof SWFRoot) {
            return ((SWFRoot) parent).list.size();
        }
        return 0;
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
        if (parent instanceof TagNode) {
            if (((TagNode) parent).tag instanceof ClassesListTreeModel) {
                ClassesListTreeModel clt = (ClassesListTreeModel) ((TagNode) parent).tag;
                return clt.getIndexOfChild(clt.getRoot(), child);
            }
        }
        if (parent == root) {
            return swfs.indexOf(child);
        } else if (parent instanceof TagNode) {
            List<TagNode> subTags = ((TagNode) parent).subItems;
            return subTags.indexOf(child);
        } else if (parent instanceof TreeElement) {
            return ((TreeElement) parent).getIndexOfChild((TreeElement) child);
        } else if (parent instanceof SWFRoot) {
            return ((SWFRoot) parent).list.indexOf(child);
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
