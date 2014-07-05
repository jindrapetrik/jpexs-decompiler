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
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.abc.treenodes.TreeElement;
import com.jpexs.decompiler.flash.gui.treenodes.SWFNode;
import com.jpexs.decompiler.flash.gui.treenodes.TagTreeRoot;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.treeitems.AS2PackageNodeItem;
import com.jpexs.decompiler.flash.treeitems.AS3PackageNodeItem;
import com.jpexs.decompiler.flash.treeitems.FrameNodeItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.StringItem;
import com.jpexs.decompiler.flash.treeitems.TreeElementItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.treenodes.ContainerNode;
import com.jpexs.decompiler.flash.treenodes.FrameNode;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JPEXS
 */
public class TagTree extends JTree implements ActionListener {

    private static final String ACTION_RAW_EDIT = "RAWEDIT";
    private static final String ACTION_JUMP_TO_CHARACTER = "JUMPTOCHARACTER";
    private static final String ACTION_REMOVE_ITEM = "REMOVEITEM";
    private static final String ACTION_REMOVE_ITEM_WITH_DEPENDENCIES = "REMOVEITEMWITHDEPENDENCIES";
    private static final String ACTION_CLOSE_SWF = "CLOSESWF";
    private static final String ACTION_EXPAND_RECURSIVE = "EXPANDRECURSIVE";

    private final MainPanel mainPanel;

    public class TagTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);
            TreeNode treeNode = (TreeNode) value;
            TreeItem val = treeNode.getItem();
            TreeNodeType type = getTreeNodeType(val);
            if (type != null) {
                if (type == TreeNodeType.FOLDER && expanded) {
                    type = TreeNodeType.FOLDER_OPEN;
                }
                String itemName = type.toString();
                if (type == TreeNodeType.FOLDER || type == TreeNodeType.FOLDER_OPEN) {
                    if (val instanceof StringItem) {
                        StringItem si = (StringItem) val;
                        if (!TagTreeRoot.FOLDER_ROOT.equals(si.getName())) {
                            itemName = "folder" + si.getName();
                        }
                    }
                }
                String tagTypeStr = itemName.toLowerCase().replace("_", "");
                setIcon(View.getIcon(tagTypeStr + "16"));
            }

            Font font = getFont();
            boolean isModified = false;
            if (treeNode instanceof TreeNode) {
                if (treeNode.getItem() instanceof Tag) {
                    Tag tag = (Tag) treeNode.getItem();
                    if (tag.isModified()) {
                        isModified = true;
                    }
                }
            }

            if (isModified) {
                font = font.deriveFont(Font.BOLD);
            } else {
                font = font.deriveFont(Font.PLAIN);
            }
            setFont(font);

            setUI(new BasicLabelUI());
            setOpaque(false);
            //setBackground(Color.green);
            setBackgroundNonSelectionColor(Color.white);
            //setBackgroundSelectionColor(Color.ORANGE);

            return this;
        }
    }

    TagTree(TagTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel);
        this.mainPanel = mainPanel;
        setCellRenderer(new TagTreeCellRenderer());
        setRootVisible(false);
        setBackground(Color.white);
        setUI(new BasicTreeUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                setHashColor(Color.gray);
                super.paint(g, c);
            }
        });
    }

    public static TreeNodeType getTreeNodeType(TreeItem t) {
        if ((t instanceof DefineFontTag)
                || (t instanceof DefineFont2Tag)
                || (t instanceof DefineFont3Tag)
                || (t instanceof DefineFont4Tag)
                || (t instanceof DefineCompactedFont)) {
            return TreeNodeType.FONT;
        }
        if ((t instanceof DefineTextTag)
                || (t instanceof DefineText2Tag)
                || (t instanceof DefineEditTextTag)) {
            return TreeNodeType.TEXT;
        }

        if ((t instanceof DefineBitsTag)
                || (t instanceof DefineBitsJPEG2Tag)
                || (t instanceof DefineBitsJPEG3Tag)
                || (t instanceof DefineBitsJPEG4Tag)
                || (t instanceof DefineBitsLosslessTag)
                || (t instanceof DefineBitsLossless2Tag)) {
            return TreeNodeType.IMAGE;
        }
        if ((t instanceof DefineShapeTag)
                || (t instanceof DefineShape2Tag)
                || (t instanceof DefineShape3Tag)
                || (t instanceof DefineShape4Tag)) {
            return TreeNodeType.SHAPE;
        }

        if ((t instanceof DefineMorphShapeTag) || (t instanceof DefineMorphShape2Tag)) {
            return TreeNodeType.MORPH_SHAPE;
        }

        if (t instanceof DefineSpriteTag) {
            return TreeNodeType.SPRITE;
        }
        if ((t instanceof DefineButtonTag) || (t instanceof DefineButton2Tag)) {
            return TreeNodeType.BUTTON;
        }
        if (t instanceof ASMSource) {
            return TreeNodeType.AS;
        }
        if (t instanceof ScriptPack) {
            return TreeNodeType.AS;
        }
        if (t instanceof AS2PackageNodeItem) {
            return TreeNodeType.PACKAGE;
        }
        if (t instanceof AS3PackageNodeItem) {
            return TreeNodeType.PACKAGE;
        }
        if (t instanceof FrameNodeItem) {
            return TreeNodeType.FRAME;
        }
        if (t instanceof ShowFrameTag) {
            return TreeNodeType.SHOW_FRAME;
        }

        if (t instanceof DefineVideoStreamTag) {
            return TreeNodeType.MOVIE;
        }

        if ((t instanceof DefineSoundTag) || (t instanceof SoundStreamHeadTag) || (t instanceof SoundStreamHead2Tag)) {
            return TreeNodeType.SOUND;
        }

        if (t instanceof DefineBinaryDataTag) {
            return TreeNodeType.BINARY_DATA;
        }

        if (t instanceof SWF) {
            return TreeNodeType.FLASH;
        }

        if (t instanceof SWFList) {
            SWFList slist = (SWFList) t;
            if (slist.name != null) {
                if (slist.name.toLowerCase().endsWith(".zip")) {
                    return TreeNodeType.BUNDLE_ZIP;
                }
                if (slist.name.toLowerCase().endsWith(".swc")) {
                    return TreeNodeType.BUNDLE_SWC;
                } else {
                    return TreeNodeType.BUNDLE_BINARY;
                }
            }
        }

        if (t instanceof Tag) {
            return TreeNodeType.OTHER_TAG;
        }

        return TreeNodeType.FOLDER;
    }

    public void createContextMenu(final List<SWFList> swfs) {
        final JPopupMenu contextPopupMenu = new JPopupMenu();

        final JMenuItem expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this);
        expandRecursiveMenuItem.setActionCommand(ACTION_EXPAND_RECURSIVE);
        contextPopupMenu.add(expandRecursiveMenuItem);

        final JMenuItem removeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.remove"));
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand(ACTION_REMOVE_ITEM);
        contextPopupMenu.add(removeMenuItem);

        final JMenuItem removeWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.removeWithDependencies"));
        removeWithDependenciesMenuItem.addActionListener(this);
        removeWithDependenciesMenuItem.setActionCommand(ACTION_REMOVE_ITEM_WITH_DEPENDENCIES);
        contextPopupMenu.add(removeWithDependenciesMenuItem);

        final JMenuItem exportSelectionMenuItem = new JMenuItem(mainPanel.translate("menu.file.export.selection"));
        exportSelectionMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_SEL);
        exportSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(exportSelectionMenuItem);

        final JMenuItem replaceSelectionMenuItem = new JMenuItem(mainPanel.translate("button.replace"));
        replaceSelectionMenuItem.setActionCommand(MainPanel.ACTION_REPLACE);
        replaceSelectionMenuItem.addActionListener(mainPanel);
        contextPopupMenu.add(replaceSelectionMenuItem);

        final JMenuItem rawEditMenuItem = new JMenuItem(mainPanel.translate("contextmenu.rawEdit"));
        rawEditMenuItem.setActionCommand(ACTION_RAW_EDIT);
        rawEditMenuItem.addActionListener(this);
        rawEditMenuItem.setVisible(false);
        contextPopupMenu.add(rawEditMenuItem);

        final JMenuItem jumpToCharacterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.jumpToCharacter"));
        jumpToCharacterMenuItem.setActionCommand(ACTION_JUMP_TO_CHARACTER);
        jumpToCharacterMenuItem.addActionListener(this);
        jumpToCharacterMenuItem.setVisible(false);
        contextPopupMenu.add(jumpToCharacterMenuItem);

        final JMenuItem closeSelectionMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeSelectionMenuItem.setActionCommand(ACTION_CLOSE_SWF);
        closeSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(closeSelectionMenuItem);

        final JMenu moveTagMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        contextPopupMenu.add(moveTagMenu);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {

                    int row = getClosestRowForLocation(e.getX(), e.getY());
                    int[] selectionRows = getSelectionRows();
                    if (!Helper.contains(selectionRows, row)) {
                        setSelectionRow(row);
                    }

                    TreePath[] paths = getSelectionPaths();
                    if (paths == null || paths.length == 0) {
                        return;
                    }
                    boolean allSelectedIsTagOrFrame = true;
                    for (TreePath treePath : paths) {
                        TreeNode treeNode = (TreeNode) treePath.getLastPathComponent();

                        TreeItem tag = treeNode.getItem();
                        if (!(tag instanceof Tag) && !(tag instanceof FrameNodeItem)) {
                            allSelectedIsTagOrFrame = false;
                            break;
                        }
                    }

                    replaceSelectionMenuItem.setVisible(false);
                    closeSelectionMenuItem.setVisible(false);
                    moveTagMenu.setVisible(false);
                    expandRecursiveMenuItem.setVisible(false);

                    if (paths.length == 1) {
                        TreeNode treeNode = (TreeNode) paths[0].getLastPathComponent();

                        TreeItem item = ((TreeNode) treeNode).getItem();

                        if (item instanceof ImageTag && ((ImageTag) item).importSupported()) {
                            replaceSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof DefineBinaryDataTag) {
                            replaceSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof DefineSoundTag) {
                            replaceSelectionMenuItem.setVisible(true);
                        }

                        if (treeNode instanceof SWFNode) {
                            closeSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof Tag && swfs.size() > 1) {
                            final Tag tag = (Tag) item;
                            moveTagMenu.removeAll();
                            for (SWFList targetSwfList : swfs) {
                                for (final SWF targetSwf : targetSwfList) {
                                    if (targetSwf != tag.getSwf()) {
                                        JMenuItem swfItem = new JMenuItem(targetSwf.getShortFileName());
                                        swfItem.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent ae) {
                                                tag.getSwf().tags.remove(tag);
                                                tag.setSwf(targetSwf);
                                                targetSwf.tags.add(tag);
                                                mainPanel.refreshTree();
                                            }
                                        });
                                        moveTagMenu.add(swfItem);
                                    }
                                }
                            }
                            moveTagMenu.setVisible(true);
                        }

                        TreeModel model = getModel();
                        expandRecursiveMenuItem.setVisible(model.getChildCount(treeNode) > 0);

                        jumpToCharacterMenuItem.setVisible(item instanceof CharacterIdTag && !(item instanceof CharacterTag));

                        rawEditMenuItem.setVisible(item instanceof Tag);
                    }

                    removeMenuItem.setVisible(allSelectedIsTagOrFrame);
                    exportSelectionMenuItem.setEnabled(hasExportableNodes());
                    contextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_RAW_EDIT: {
                TreeItem item = getCurrentTreeItem();
                if (item == null) {
                    return;
                }

                mainPanel.showGenericTag((Tag) item);
            }
            break;
            case ACTION_JUMP_TO_CHARACTER: {
                TreeItem item = getCurrentTreeItem();
                if (item == null || !(item instanceof CharacterIdTag)) {
                    return;
                }

                CharacterIdTag characterIdTag = (CharacterIdTag) item;
                mainPanel.setTreeItem(item.getSwf().characters.get(characterIdTag.getCharacterId()));
            }
            break;
            case ACTION_EXPAND_RECURSIVE: {
                TreePath path = getSelectionPath();
                if (path == null) {
                    return;
                }
                View.expandTreeNodesRecursive(this, path, true);
            }
            break;
            case ACTION_REMOVE_ITEM:
            case ACTION_REMOVE_ITEM_WITH_DEPENDENCIES:
                List<TreeNode> sel = getSelected(this);

                List<Tag> tagsToRemove = new ArrayList<>();
                for (TreeNode o : sel) {
                    TreeItem tag = o.getItem();
                    if (tag instanceof Tag) {
                        tagsToRemove.add((Tag) tag);
                    } else if (tag instanceof FrameNodeItem) {
                        FrameNodeItem frameNode = (FrameNodeItem) tag;
                        Frame frame = frameNode.getParent().getTimeline().frames.get(frameNode.getFrame() - 1);
                        if (frame.showFrameTag != null) {
                            tagsToRemove.add(frame.showFrameTag);
                        } else {
                            // this should be the last frame, so remove the inner tags
                            tagsToRemove.addAll(frame.innerTags);
                        }
                    }
                }

                boolean removeDependencies = e.getActionCommand().equals(ACTION_REMOVE_ITEM_WITH_DEPENDENCIES);
                if (tagsToRemove.size() == 1) {
                    Tag tag = tagsToRemove.get(0);
                    if (View.showConfirmDialog(this, mainPanel.translate("message.confirm.remove").replace("%item%", tag.toString()), mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        tag.getSwf().removeTag(tag, removeDependencies);
                        mainPanel.refreshTree();
                    }
                } else if (tagsToRemove.size() > 1) {
                    if (View.showConfirmDialog(this, mainPanel.translate("message.confirm.removemultiple").replace("%count%", Integer.toString(tagsToRemove.size())), mainPanel.translate("message.confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        for (Tag tag : tagsToRemove) {
                            tag.getSwf().removeTag(tag, removeDependencies);
                        }
                        mainPanel.refreshTree();
                    }
                }
                break;
            case ACTION_CLOSE_SWF: {
                Main.closeFile(mainPanel.getCurrentSwfList());
            }
        }
    }
    
    public boolean hasExportableNodes() {
        return !getSelection(mainPanel.getCurrentSwf()).isEmpty();
    }

    public List<TreeNode> getAllSubs(JTree tree, TreeNode o) {
        TagTreeModel tm = (TagTreeModel) tree.getModel();
        List<TreeNode> ret = new ArrayList<>();
        for (int i = 0; i < tm.getChildCount(o); i++) {
            TreeNode c = tm.getChild(o, i);
            ret.add(c);
            ret.addAll(getAllSubs(tree, c));
        }
        return ret;
    }

    public List<TreeNode> getAllSelected(TagTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<TreeNode> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeNode treeNode = (TreeNode) tp.getLastPathComponent();
            ret.add(treeNode);
            ret.addAll(getAllSubs(tree, treeNode));
        }
        return ret;
    }

    public List<TreeNode> getSelected(JTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<TreeNode> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeNode treeNode = (TreeNode) tp.getLastPathComponent();
            ret.add(treeNode);
        }
        return ret;
    }

    public List<Object> getSelection(SWF swf) {
        List<Object> ret = new ArrayList<>();
        List<TreeNode> sel = getAllSelected(this);
        for (TreeNode d : sel) {
            if (d.getItem().getSwf() != swf) {
                continue;
            }
            if (d instanceof ContainerNode) {
                ContainerNode n = (ContainerNode) d;
                TreeNodeType nodeType = TagTree.getTreeNodeType(n.getItem());
                if (nodeType == TreeNodeType.IMAGE) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.SHAPE) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.MORPH_SHAPE) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.AS) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.MOVIE) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.SOUND) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.BINARY_DATA) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.TEXT) {
                    ret.add((Tag) n.getItem());
                }
                if (nodeType == TreeNodeType.FONT) {
                    ret.add((Tag) n.getItem());
                }
            }
            if (d instanceof FrameNode) {
                FrameNode fn = (FrameNode) d;
                if (!fn.scriptsNode) {
                    ret.add(d.getItem());
                }
            }
            if (d instanceof TreeElement) {
                if (((TreeElement) d).isLeaf()) {
                    TreeElement treeElement = (TreeElement) d;
                    ret.add((ScriptPack) treeElement.getItem());
                }
            }
        }
        return ret;
    }

    public List<TreeElementItem> getTagsWithType(List<TreeElementItem> list, TreeNodeType type) {
        List<TreeElementItem> ret = new ArrayList<>();
        for (TreeElementItem item : list) {
            TreeNodeType ttype = getTreeNodeType(item);
            if (type == ttype) {
                ret.add(item);
            }
        }
        return ret;
    }

    public TreeItem getCurrentTreeItem() {
        TreeNode treeNode = (TreeNode) getLastSelectedPathComponent();
        if (treeNode == null) {
            return null;
        }
        return treeNode.getItem();
    }
}
