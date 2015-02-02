/*
 *  Copyright (C) 2010-2015 JPEXS
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.plaf.TreeUI;
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
public class TagTree extends JTree {

    public TagTreeContextMenu contextPopupMenu;

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
        setRowHeight(16);
        setLargeModel(true);

        TreeUI treeUI = new BasicTreeUI() {
            {
                setHashColor(Color.gray);
            }
        };

        setUI(treeUI);
    }

    public void createContextMenu() {
        contextPopupMenu = new TagTreeContextMenu(this, mainPanel);
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
                if (gfx) {
                    ret = Arrays.asList(DefineFontTag.ID, DefineFont2Tag.ID, DefineFont3Tag.ID, DefineFont4Tag.ID, DefineCompactedFont.ID);
                } else {
                    ret = Arrays.asList(DefineFontTag.ID, DefineFont2Tag.ID, DefineFont3Tag.ID, DefineFont4Tag.ID);
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
            if (d instanceof SWFList) {
                continue;
            }
            if (d.getSwf() != swf) {
                continue;
            }

            if (d instanceof TagScript) {
                Tag tag = ((TagScript) d).getTag();
                if (tag instanceof DoActionTag || tag instanceof DoInitActionTag) {
                    d = tag;
                }
            }

            if (d instanceof Tag || d instanceof ASMSource) {
                TreeNodeType nodeType = TagTree.getTreeNodeType(d);
                if (nodeType == TreeNodeType.IMAGE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SHAPE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.MORPH_SHAPE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.AS) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.MOVIE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.SOUND) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.BINARY_DATA) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.TEXT) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.FONT) {
                    ret.add(d);
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

    @Override
    public void setModel(TreeModel tm) {
        super.setModel(tm);
        if (tm != null) {
            int rowCount = tm.getChildCount(tm.getRoot());
            for (int i = rowCount - 1; i >= 0; i--) {
                expandRow(i);
            }
        }
    }
}
