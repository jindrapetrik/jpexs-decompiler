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
import com.jpexs.decompiler.flash.tags.CSMTextSettingsTag;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineButton2Tag;
import com.jpexs.decompiler.flash.tags.DefineButtonCxformTag;
import com.jpexs.decompiler.flash.tags.DefineButtonSoundTag;
import com.jpexs.decompiler.flash.tags.DefineButtonTag;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineFont2Tag;
import com.jpexs.decompiler.flash.tags.DefineFont3Tag;
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineFontAlignZonesTag;
import com.jpexs.decompiler.flash.tags.DefineFontInfo2Tag;
import com.jpexs.decompiler.flash.tags.DefineFontInfoTag;
import com.jpexs.decompiler.flash.tags.DefineFontNameTag;
import com.jpexs.decompiler.flash.tags.DefineFontTag;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.DefineScalingGridTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineText2Tag;
import com.jpexs.decompiler.flash.tags.DefineTextTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.DoABCDefineTag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.DoInitActionTag;
import com.jpexs.decompiler.flash.tags.EnableDebugger2Tag;
import com.jpexs.decompiler.flash.tags.EnableDebuggerTag;
import com.jpexs.decompiler.flash.tags.EnableTelemetryTag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.FrameLabelTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ImportAssetsTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.MetadataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject4Tag;
import com.jpexs.decompiler.flash.tags.PlaceObjectTag;
import com.jpexs.decompiler.flash.tags.ProductInfoTag;
import com.jpexs.decompiler.flash.tags.ProtectTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObjectTag;
import com.jpexs.decompiler.flash.tags.ScriptLimitsTag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.SoundStreamHead2Tag;
import com.jpexs.decompiler.flash.tags.SoundStreamHeadTag;
import com.jpexs.decompiler.flash.tags.StartSound2Tag;
import com.jpexs.decompiler.flash.tags.StartSoundTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.VideoFrameTag;
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
import com.jpexs.decompiler.flash.timeline.Timelined;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
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

        private Font plainFont;

        private Font boldFont;

        private final Map<TreeNodeType, Icon> icons;

        public TagTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            //setBackground(Color.green);
            setBackgroundNonSelectionColor(Color.white);
            //setBackgroundSelectionColor(Color.ORANGE);

            icons = new HashMap<>();
            for (TreeNodeType treeNodeType : TreeNodeType.values()) {
                if (treeNodeType != TreeNodeType.UNKNOWN && treeNodeType != TreeNodeType.SHOW_FRAME) {
                    String tagTypeStr = treeNodeType.toString().toLowerCase().replace("_", "");
                    icons.put(treeNodeType, View.getIcon(tagTypeStr + "16"));
                }
            }
        }

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

            if (type == TreeNodeType.FOLDER && expanded) {
                type = TreeNodeType.FOLDER_OPEN;
            }

            if ((type == TreeNodeType.FOLDER || type == TreeNodeType.FOLDER_OPEN) && val instanceof FolderItem) {
                FolderItem si = (FolderItem) val;
                if (!TagTreeRoot.FOLDER_ROOT.equals(si.getName())) {
                    String itemName = "folder" + si.getName();
                    setIcon(View.getIcon(itemName.toLowerCase() + "16"));
                }
            } else {
                setIcon(icons.get(type));
            }

            boolean isModified = val instanceof Tag && ((Tag) val).isModified();
            if (isModified) {
                if (boldFont == null) {
                    Font font = getFont();
                    boldFont = font.deriveFont(Font.BOLD);
                }
            } else {
                if (plainFont == null) {
                    Font font = getFont();
                    plainFont = font.deriveFont(Font.PLAIN);
                }
            }
            setFont(isModified ? boldFont : plainFont);

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

    public List<Integer> getSwfFolderItemNestedTagIds(String folderName, boolean gfx) {
        List<Integer> ret = null;
        switch (folderName) {
            case TagTreeModel.FOLDER_SHAPES:
                ret = Arrays.asList(DefineShapeTag.ID, DefineShape2Tag.ID, DefineShape3Tag.ID, DefineShape4Tag.ID);
                break;
            case TagTreeModel.FOLDER_MORPHSHAPES:
                ret = Arrays.asList(DefineMorphShapeTag.ID, DefineMorphShape2Tag.ID);
                break;
            case TagTreeModel.FOLDER_SPRITES:
                ret = Arrays.asList(DefineSpriteTag.ID);
                break;
            case TagTreeModel.FOLDER_TEXTS:
                ret = Arrays.asList(DefineTextTag.ID, DefineText2Tag.ID, DefineEditTextTag.ID);
                break;
            case TagTreeModel.FOLDER_IMAGES:
                ret = Arrays.asList(DefineBitsTag.ID, DefineBitsJPEG2Tag.ID, DefineBitsJPEG3Tag.ID, DefineBitsJPEG4Tag.ID, DefineBitsLosslessTag.ID, DefineBitsLossless2Tag.ID);
                break;
            case TagTreeModel.FOLDER_MOVIES:
                ret = Arrays.asList(DefineVideoStreamTag.ID);
                break;
            case TagTreeModel.FOLDER_SOUNDS:
                ret = Arrays.asList(DefineSoundTag.ID);
                break;
            case TagTreeModel.FOLDER_BUTTONS:
                ret = Arrays.asList(DefineButtonTag.ID, DefineButton2Tag.ID);
                break;
            case TagTreeModel.FOLDER_FONTS:
                ret = Arrays.asList(DefineFontTag.ID, DefineFont2Tag.ID, DefineFont3Tag.ID, DefineFont4Tag.ID);
                if (gfx) {
                    ret.add(DefineCompactedFont.ID);
                }
                break;
            case TagTreeModel.FOLDER_BINARY_DATA:
                ret = Arrays.asList(DefineBinaryDataTag.ID);
                break;
            case TagTreeModel.FOLDER_FRAMES:
                ret = new ArrayList<>();
                break;
            case TagTreeModel.FOLDER_OTHERS:
                ret = Arrays.asList(CSMTextSettingsTag.ID, DebugIDTag.ID, DefineButtonCxformTag.ID, DefineButtonSoundTag.ID,
                        DefineFontAlignZonesTag.ID, DefineFontInfoTag.ID, DefineFontInfo2Tag.ID, DefineFontNameTag.ID,
                        DefineScalingGridTag.ID, DefineSceneAndFrameLabelDataTag.ID,
                        DoABCDefineTag.ID, DoABCTag.ID, DoActionTag.ID, DoInitActionTag.ID,
                        EnableDebuggerTag.ID, EnableDebugger2Tag.ID, EnableTelemetryTag.ID,
                        ExportAssetsTag.ID, FileAttributesTag.ID, ImportAssetsTag.ID, ImportAssets2Tag.ID,
                        JPEGTablesTag.ID, MetadataTag.ID, ProductInfoTag.ID, ProtectTag.ID, ScriptLimitsTag.ID,
                        SetBackgroundColorTag.ID, SetTabIndexTag.ID, SymbolClassTag.ID);
                break;
        }

        return ret;
    }

    public List<Integer> getSpriteNestedTagIds() {
        return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID);
    }

    JMenuItem expandRecursiveMenuItem;
    JMenuItem removeMenuItem;
    JMenuItem removeWithDependenciesMenuItem;
    JMenuItem exportSelectionMenuItem;
    JMenuItem replaceSelectionMenuItem;
    JMenuItem rawEditMenuItem;
    JMenuItem jumpToCharacterMenuItem;
    JMenuItem closeSelectionMenuItem;
    JMenu addTagMenu;
    JMenu moveTagMenu;
    JMenuItem openSWFInsideTagMenuItem;
    public JPopupMenu contextPopupMenu;
    public List<SWFList> swfs;

    public void createContextMenu(final List<SWFList> swfs) {
        this.swfs = swfs;
        contextPopupMenu = new JPopupMenu();
        expandRecursiveMenuItem = new JMenuItem(mainPanel.translate("contextmenu.expandAll"));
        expandRecursiveMenuItem.addActionListener(this);
        expandRecursiveMenuItem.setActionCommand(ACTION_EXPAND_RECURSIVE);
        contextPopupMenu.add(expandRecursiveMenuItem);

        removeMenuItem = new JMenuItem(mainPanel.translate("contextmenu.remove"));
        removeMenuItem.addActionListener(this);
        removeMenuItem.setActionCommand(ACTION_REMOVE_ITEM);
        contextPopupMenu.add(removeMenuItem);

        removeWithDependenciesMenuItem = new JMenuItem(mainPanel.translate("contextmenu.removeWithDependencies"));
        removeWithDependenciesMenuItem.addActionListener(this);
        removeWithDependenciesMenuItem.setActionCommand(ACTION_REMOVE_ITEM_WITH_DEPENDENCIES);
        contextPopupMenu.add(removeWithDependenciesMenuItem);

        exportSelectionMenuItem = new JMenuItem(mainPanel.translate("menu.file.export.selection"));
        exportSelectionMenuItem.setActionCommand(MainFrameRibbonMenu.ACTION_EXPORT_SEL);
        exportSelectionMenuItem.addActionListener(mainPanel);
        contextPopupMenu.add(exportSelectionMenuItem);

        replaceSelectionMenuItem = new JMenuItem(mainPanel.translate("button.replace"));
        replaceSelectionMenuItem.setActionCommand(MainPanel.ACTION_REPLACE);
        replaceSelectionMenuItem.addActionListener(mainPanel);
        contextPopupMenu.add(replaceSelectionMenuItem);

        rawEditMenuItem = new JMenuItem(mainPanel.translate("contextmenu.rawEdit"));
        rawEditMenuItem.setActionCommand(ACTION_RAW_EDIT);
        rawEditMenuItem.addActionListener(this);
        rawEditMenuItem.setVisible(false);
        contextPopupMenu.add(rawEditMenuItem);

        jumpToCharacterMenuItem = new JMenuItem(mainPanel.translate("contextmenu.jumpToCharacter"));
        jumpToCharacterMenuItem.setActionCommand(ACTION_JUMP_TO_CHARACTER);
        jumpToCharacterMenuItem.addActionListener(this);
        jumpToCharacterMenuItem.setVisible(false);
        contextPopupMenu.add(jumpToCharacterMenuItem);

        closeSelectionMenuItem = new JMenuItem(mainPanel.translate("contextmenu.closeSwf"));
        closeSelectionMenuItem.setActionCommand(ACTION_CLOSE_SWF);
        closeSelectionMenuItem.addActionListener(this);
        contextPopupMenu.add(closeSelectionMenuItem);

        addTagMenu = new JMenu(mainPanel.translate("contextmenu.addTag"));
        contextPopupMenu.add(addTagMenu);

        moveTagMenu = new JMenu(mainPanel.translate("contextmenu.moveTag"));
        contextPopupMenu.add(moveTagMenu);

        openSWFInsideTagMenuItem = new JMenuItem(mainPanel.translate("contextmenu.openswfinside"));
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
                        List<TreeItem> li = new ArrayList<>();
                        li.add(item);
                        updateContextMenu(swfs, li);
                    }

                    removeMenuItem.setVisible(allSelectedIsTagOrFrame);
                    exportSelectionMenuItem.setEnabled(hasExportableNodes());
                    contextPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    public void updateContextMenu(final List<SWFList> swfs, List<TreeItem> items) {

        replaceSelectionMenuItem.setVisible(false);
        closeSelectionMenuItem.setVisible(false);
        moveTagMenu.setVisible(false);
        addTagMenu.setVisible(false);
        expandRecursiveMenuItem.setVisible(false);
        openSWFInsideTagMenuItem.setVisible(false);
        removeMenuItem.setVisible(true);
        for (TreeItem t : items) {
            if (!((t instanceof Tag) || (t instanceof Frame))) {
                removeMenuItem.setVisible(false);
            }
        }
        exportSelectionMenuItem.setEnabled(!items.isEmpty() && !getSelection(items.get(0).getSwf(), items).isEmpty());

        final TreeItem item = items.get(0);
        if (item instanceof ImageTag && ((ImageTag) item).importSupported()) {
            replaceSelectionMenuItem.setVisible(true);
        }

        if (item instanceof DefineBinaryDataTag) {
            replaceSelectionMenuItem.setVisible(true);
            DefineBinaryDataTag bin = (DefineBinaryDataTag) item;
            if (bin.isSwfData()) {
                openSWFInsideTagMenuItem.setVisible(true);
            }
        }

        if (item instanceof DefineSoundTag) {
            replaceSelectionMenuItem.setVisible(true);
        }

        if (item instanceof SWF) {
            closeSelectionMenuItem.setVisible(true);
        }

        List<Integer> allowedTagTypes = null;
        if (item instanceof FolderItem) {
            allowedTagTypes = getSwfFolderItemNestedTagIds(((FolderItem) item).getName(), item.getSwf().gfx);
        } else if (item instanceof DefineSpriteTag) {
            allowedTagTypes = getSpriteNestedTagIds();
        }

        addTagMenu.removeAll();
        if (allowedTagTypes != null) {
            for (Integer tagId : allowedTagTypes) {
                final Class cl = TagIdClassMap.getClassByTagId(tagId);
                JMenuItem tagItem = new JMenuItem(cl.getSimpleName());
                tagItem.addActionListener(new ActionListener() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            SWF swf = item.getSwf();
                            Tag t = (Tag) cl.getDeclaredConstructor(SWF.class).newInstance(new Object[]{swf});
                            boolean isDefineSprite = item instanceof DefineSpriteTag;
                            Timelined timelined = isDefineSprite ? (DefineSpriteTag) item : swf;
                            t.setTimelined(timelined);
                            if (isDefineSprite) {
                                ((DefineSpriteTag) item).subTags.add(t);
                            } else {
                                swf.tags.add(t);
                            }
                            timelined.getTimeline().reset();
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
        if (!mainPanel.folderPreviewPanel.selectedItems.isEmpty()) {
            return new ArrayList<>(mainPanel.folderPreviewPanel.selectedItems.values());
        }
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
        List<TreeItem> sel;
        if (mainPanel.folderPreviewPanel.selectedItems.isEmpty()) {
            sel = getAllSelected(this);
        } else {
            sel = new ArrayList<>(mainPanel.folderPreviewPanel.selectedItems.values());
        }
        return getSelection(swf, sel);
    }

    public List<TreeItem> getSelection(SWF swf, List<TreeItem> sel) {
        List<TreeItem> ret = new ArrayList<>();
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
        if (!mainPanel.folderPreviewPanel.selectedItems.isEmpty()) {
            return mainPanel.folderPreviewPanel.selectedItems.get(0);
        }
        TreeItem item = (TreeItem) getLastSelectedPathComponent();
        return item;
    }
}
