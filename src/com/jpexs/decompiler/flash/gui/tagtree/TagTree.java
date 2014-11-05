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
package com.jpexs.decompiler.flash.gui.tagtree;

import com.jpexs.decompiler.flash.SWC;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.ZippedSWFBundle;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.MainFrameRibbonMenu;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
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
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ContainerItem;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.timeline.AS2Package;
import com.jpexs.decompiler.flash.timeline.AS3Package;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.FrameScript;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.FolderItem;
import com.jpexs.decompiler.flash.treeitems.HeaderItem;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.utf8.Utf8Helper;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final String ACTION_OPEN_SWFINSIDE = "OPENSWFINSIDE";

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
            TreeItem val = (TreeItem) value;
            TreeNodeType type = getTreeNodeType(val);
            if (type != null) {
                if (type == TreeNodeType.FOLDER && expanded) {
                    type = TreeNodeType.FOLDER_OPEN;
                }
                String itemName = type.toString();
                if (type == TreeNodeType.FOLDER || type == TreeNodeType.FOLDER_OPEN) {
                    if (val instanceof FolderItem) {
                        FolderItem si = (FolderItem) val;
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
            if (val instanceof Tag) {
                Tag tag = (Tag) val;
                if (tag.isModified()) {
                    isModified = true;
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

    public TagTree(TagTreeModel treeModel, MainPanel mainPanel) {
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

        if (t instanceof TagScript) {
            t = ((TagScript) t).getTag();
        }

        if (t instanceof HeaderItem) {
            return TreeNodeType.HEADER;
        }

        if ((t instanceof DefineFontTag)
                || (t instanceof DefineFont2Tag)
                || (t instanceof DefineFont3Tag)
                || (t instanceof DefineFont4Tag)
                || (t instanceof DefineCompactedFont)) {
            return TreeNodeType.FONT;
        }

        // DefineText, DefineText2, DefineEditTextTag
        if (t instanceof TextTag) {
            return TreeNodeType.TEXT;
        }

        // DefineBits, DefineBitsJPEG2, DefineBitsJPEG3, DefineBitsJPEG4, DefineBitsLossless, DefineBitsLossless2
        if (t instanceof ImageTag) {
            return TreeNodeType.IMAGE;
        }

        // DefineShape, DefineShape2, DefineShape3, DefineShape4
        if (t instanceof ShapeTag) {
            return TreeNodeType.SHAPE;
        }

        // DefineMorphShape, DefineMorphShape2
        if (t instanceof MorphShapeTag) {
            return TreeNodeType.MORPH_SHAPE;
        }

        if (t instanceof DefineSpriteTag) {
            return TreeNodeType.SPRITE;
        }

        // DefineButton, DefineButton2
        if (t instanceof ButtonTag) {
            return TreeNodeType.BUTTON;
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

        if (t instanceof ASMSource) {
            return TreeNodeType.AS;
        }

        if (t instanceof ScriptPack) {
            return TreeNodeType.AS;
        }

        if (t instanceof AS2Package) {
            return TreeNodeType.PACKAGE;
        }

        if (t instanceof AS3Package) {
            return TreeNodeType.PACKAGE;
        }

        if ((t instanceof Frame)
                || (t instanceof FrameScript)) {
            return TreeNodeType.FRAME;
        }

        if (t instanceof ShowFrameTag) {
            return TreeNodeType.SHOW_FRAME;
        }

        if (t instanceof SWF) {
            return TreeNodeType.FLASH;
        }

        if (t instanceof SWFList) {
            SWFList slist = (SWFList) t;
            if (slist.bundleClass != null) {
                if (slist.bundleClass == ZippedSWFBundle.class) {
                    return TreeNodeType.BUNDLE_ZIP;
                } else if (slist.bundleClass == SWC.class) {
                    return TreeNodeType.BUNDLE_SWC;
                } else {
                    return TreeNodeType.BUNDLE_BINARY;
                }
            }
        }

        if (t instanceof Tag) {
            return TreeNodeType.OTHER_TAG;
        }

        if (t instanceof FolderItem) {
            return TreeNodeType.FOLDER;
        }

        return TreeNodeType.FOLDER;
    }

    public List<Class> getTreeItemClasses(String folderName, boolean gfx) {
        List<Class> ret = null;
        switch (folderName) {
            case TagTreeModel.FOLDER_SHAPES:
                ret = Arrays.asList((Class) DefineShapeTag.class, DefineShape2Tag.class, DefineShape3Tag.class, DefineShape4Tag.class);
                break;
            case TagTreeModel.FOLDER_MORPHSHAPES:
                ret = Arrays.asList((Class) DefineMorphShapeTag.class, DefineMorphShape2Tag.class);
                break;
            case TagTreeModel.FOLDER_SPRITES:
                ret = Arrays.asList((Class) DefineSpriteTag.class);
                break;
            case TagTreeModel.FOLDER_TEXTS:
                ret = Arrays.asList((Class) DefineTextTag.class, DefineText2Tag.class, DefineEditTextTag.class);
                break;
            case TagTreeModel.FOLDER_IMAGES:
                ret = Arrays.asList((Class) DefineBitsTag.class, DefineBitsJPEG2Tag.class, DefineBitsJPEG3Tag.class, DefineBitsJPEG4Tag.class, DefineBitsLosslessTag.class, DefineBitsLossless2Tag.class);
                break;
            case TagTreeModel.FOLDER_MOVIES:
                ret = Arrays.asList((Class) DefineVideoStreamTag.class);
                break;
            case TagTreeModel.FOLDER_SOUNDS:
                ret = Arrays.asList((Class) DefineSoundTag.class, SoundStreamHeadTag.class, SoundStreamHead2Tag.class);
                break;
            case TagTreeModel.FOLDER_BUTTONS:
                ret = Arrays.asList((Class) DefineButtonTag.class, DefineButton2Tag.class);
                break;
            case TagTreeModel.FOLDER_FONTS:
                ret = Arrays.asList((Class) DefineFontTag.class, DefineFont2Tag.class, DefineFont3Tag.class, DefineFont4Tag.class);
                if (gfx) {
                    ret.add(DefineCompactedFont.class);
                }
                break;
            case TagTreeModel.FOLDER_BINARY_DATA:
                ret = Arrays.asList((Class) DefineBinaryDataTag.class);
                break;
            case TagTreeModel.FOLDER_FRAMES:
                ret = new ArrayList<>();
                break;
            case TagTreeModel.FOLDER_OTHERS:
                ret = new ArrayList<>();
                break;
        }

        return ret;
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
        exportSelectionMenuItem.addActionListener(mainPanel);
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

        final JMenu addTagMenu = new JMenu(mainPanel.translate("contextmenu.addTag"));
        contextPopupMenu.add(addTagMenu);

        final JMenu moveTagMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        contextPopupMenu.add(moveTagMenu);

        final JMenuItem openSWFInsideTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.openswfinside"));
        contextPopupMenu.add(openSWFInsideTagMenuItem);
        openSWFInsideTagMenuItem.setActionCommand(ACTION_OPEN_SWFINSIDE);
        openSWFInsideTagMenuItem.addActionListener(this);

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
                        TreeItem tag = (TreeItem) treePath.getLastPathComponent();
                        if (!(tag instanceof Tag) && !(tag instanceof Frame)) {
                            allSelectedIsTagOrFrame = false;
                            break;
                        }
                    }

                    replaceSelectionMenuItem.setVisible(false);
                    closeSelectionMenuItem.setVisible(false);
                    moveTagMenu.setVisible(false);
                    addTagMenu.setVisible(false);
                    expandRecursiveMenuItem.setVisible(false);
                    openSWFInsideTagMenuItem.setVisible(false);

                    if (paths.length == 1) {
                        TreeItem item = (TreeItem) paths[0].getLastPathComponent();

                        if (item instanceof ImageTag && ((ImageTag) item).importSupported()) {
                            replaceSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof DefineBinaryDataTag) {
                            replaceSelectionMenuItem.setVisible(true);
                            DefineBinaryDataTag bin = (DefineBinaryDataTag) item;
                            if (bin.binaryData.length > 8) {
                                String signature = new String(bin.binaryData, 0, 3, Utf8Helper.charset);
                                if (Arrays.asList(
                                        "FWS", //Uncompressed Flash
                                        "CWS", //ZLib compressed Flash
                                        "ZWS", //LZMA compressed Flash
                                        "GFX", //Uncompressed ScaleForm GFx
                                        "CFX" //Compressed ScaleForm GFx
                                ).contains(signature)) {
                                    openSWFInsideTagMenuItem.setVisible(true);
                                }
                            }
                        }

                        if (item instanceof DefineSoundTag) {
                            replaceSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof SWF) {
                            closeSelectionMenuItem.setVisible(true);
                        }

                        if (item instanceof FolderItem) {
                            final FolderItem folderItem = (FolderItem) item;
                            List<Class> allowedTagTypes = getTreeItemClasses(folderItem.getName(), item.getSwf().gfx);
                            addTagMenu.removeAll();
                            if (allowedTagTypes != null) {
                                for (final Class cl : allowedTagTypes) {
                                    JMenuItem tagItem = new JMenuItem(cl.getSimpleName());
                                    tagItem.addActionListener(new ActionListener() {

                                        @Override
                                        @SuppressWarnings("unchecked")
                                        public void actionPerformed(ActionEvent ae) {
                                            try {
                                                SWF swf = folderItem.getSwf();
                                                Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});
                                                t.setTimelined(swf);
                                                swf.tags.add(t);
                                                swf.updateCharacters();
                                                mainPanel.refreshTree();
                                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                                                Logger.getLogger(TagTree.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    });
                                    addTagMenu.add(tagItem);
                                }
                                addTagMenu.setVisible(true);
                            }
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
                        expandRecursiveMenuItem.setVisible(model.getChildCount(item) > 0);

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
            case ACTION_OPEN_SWFINSIDE:
                Object itemo = getLastSelectedPathComponent();
                if (itemo == null) {
                    return;
                }
                if (itemo instanceof DefineBinaryDataTag) {
                    mainPanel.loadFromBinaryTag((DefineBinaryDataTag) itemo);
                }
                break;
            case ACTION_RAW_EDIT: {
                TreeItem itemr = getCurrentTreeItem();
                if (itemr == null) {
                    return;
                }

                mainPanel.showGenericTag((Tag) itemr);
            }
            break;
            case ACTION_JUMP_TO_CHARACTER: {
                TreeItem itemj = getCurrentTreeItem();
                if (itemj == null || !(itemj instanceof CharacterIdTag)) {
                    return;
                }

                CharacterIdTag characterIdTag = (CharacterIdTag) itemj;
                mainPanel.setTagTreeSelectedNode(itemj.getSwf().characters.get(characterIdTag.getCharacterId()));
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
                List<TreeItem> sel = getSelected(this);

                List<Tag> tagsToRemove = new ArrayList<>();
                for (TreeItem tag : sel) {
                    if (tag instanceof Tag) {
                        tagsToRemove.add((Tag) tag);
                    } else if (tag instanceof Frame) {
                        Frame frameNode = (Frame) tag;
                        Frame frame = frameNode.timeline.getFrames().get(frameNode.frame);
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

    public List<TreeItem> getAllSubs(JTree tree, TreeItem o) {
        TagTreeModel tm = (TagTreeModel) tree.getModel();
        List<TreeItem> ret = new ArrayList<>();
        for (int i = 0; i < tm.getChildCount(o); i++) {
            TreeItem c = tm.getChild(o, i);
            ret.add(c);
            ret.addAll(getAllSubs(tree, c));
        }
        return ret;
    }

    public List<TreeItem> getAllSelected(TagTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<TreeItem> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeItem treeNode = (TreeItem) tp.getLastPathComponent();
            ret.add(treeNode);
            ret.addAll(getAllSubs(tree, treeNode));
        }
        return ret;
    }

    public List<TreeItem> getSelected(JTree tree) {
        TreeSelectionModel tsm = tree.getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<TreeItem> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeItem treeNode = (TreeItem) tp.getLastPathComponent();
            ret.add(treeNode);
        }
        return ret;
    }

    public List<TreeItem> getSelection(SWF swf) {
        List<TreeItem> ret = new ArrayList<>();
        List<TreeItem> sel = getAllSelected(this);
        for (TreeItem d : sel) {
            if (d.getSwf() != swf) {
                continue;
            }
            if (d instanceof ContainerItem) {
                ContainerItem n = (ContainerItem) d;
                TreeNodeType nodeType = TagTree.getTreeNodeType(n);
                if (nodeType == TreeNodeType.IMAGE) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.SHAPE) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.MORPH_SHAPE) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.AS) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.MOVIE) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.SOUND) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.BINARY_DATA) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.TEXT) {
                    ret.add(n);
                }
                if (nodeType == TreeNodeType.FONT) {
                    ret.add(n);
                }
            }
            if (d instanceof Frame) {
                ret.add(d);
            }
            if (d instanceof ScriptPack) {
                ret.add(d);
            }
        }
        return ret;
    }

    public List<AS3ClassTreeItem> getTagsWithType(List<AS3ClassTreeItem> list, TreeNodeType type) {
        List<AS3ClassTreeItem> ret = new ArrayList<>();
        for (AS3ClassTreeItem item : list) {
            TreeNodeType ttype = getTreeNodeType(item);
            if (type == ttype) {
                ret.add(item);
            }
        }
        return ret;
    }

    public TreeItem getCurrentTreeItem() {
        TreeItem item = (TreeItem) getLastSelectedPathComponent();
        return item;
    }
}
