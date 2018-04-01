/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.iggy.conversion.IggySwfBundle;
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
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
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
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SymbolClassTypeTag;
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
import java.util.Locale;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JPEXS
 */
public class TagTree extends JTree {

    public TagTreeContextMenu contextPopupMenu;

    private final MainPanel mainPanel;

    private static final Map<TreeNodeType, Icon> ICONS;

    static {
        ICONS = new HashMap<>();
        for (TreeNodeType treeNodeType : TreeNodeType.values()) {
            if (treeNodeType != TreeNodeType.UNKNOWN && treeNodeType != TreeNodeType.SHOW_FRAME) {
                String tagTypeStr = treeNodeType.toString().toLowerCase(Locale.ENGLISH).replace("_", "");
                ICONS.put(treeNodeType, View.getIcon(tagTypeStr + "16"));
            }
        }
    }

    public static Icon getIconForType(TreeNodeType t) {
        return ICONS.get(t);
    }

    public static class TagTreeCellRenderer extends DefaultTreeCellRenderer {

        private Font plainFont;

        private Font boldFont;

        public TagTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            //setBackground(Color.green);
            setBackgroundNonSelectionColor(Color.white);
            //setBackgroundSelectionColor(Color.ORANGE);

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

            TreeItem val = (TreeItem) value;
            if (val != null && !(val instanceof SWFList) && val.getSwf() == null) {
                // SWF was closed
                value = null;
            }

            super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus);

            if (val == null) {
                return this;
            }

            TreeNodeType type = getTreeNodeType(val);

            if (type == TreeNodeType.FOLDER && expanded) {
                type = TreeNodeType.FOLDER_OPEN;
            }

            if ((type == TreeNodeType.FOLDER || type == TreeNodeType.FOLDER_OPEN) && val instanceof FolderItem) {
                FolderItem si = (FolderItem) val;
                if (!TagTreeRoot.FOLDER_ROOT.equals(si.getName())) {
                    String itemName = "folder" + si.getName();
                    setIcon(View.getIcon(itemName.toLowerCase(Locale.ENGLISH) + "16"));
                }
            } else {
                setIcon(ICONS.get(type));
            }

            /* boolean isModified = val instanceof Tag && ((Tag) val).isModified();
             if(val instanceof ScriptPack){
             ScriptPack sp=(ScriptPack)val;
             if(sp.abc.script_info.get(sp.scriptIndex).isModified()){
             isModified = true;
             }
             }*/
            boolean isReadOnly = false;
            if (val instanceof Tag) {
                isReadOnly = ((Tag) val).isReadOnly();
            }

            boolean isModified = val.isModified();
            if (isModified) {
                if (boldFont == null) {
                    Font font = getFont();
                    boldFont = font.deriveFont(Font.BOLD);
                }
            } else if (plainFont == null) {
                Font font = getFont();
                plainFont = font.deriveFont(Font.PLAIN);
            }
            setFont(isModified ? boldFont : plainFont);
            if (isReadOnly) {
                setForeground(new Color(0xcc, 0xcc, 0xcc));
            } else {
                setForeground(Color.BLACK);
            }

            return this;
        }
    }

    public TagTree(TagTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel);
        this.mainPanel = mainPanel;
        setCellRenderer(new TagTreeCellRenderer());
        setRootVisible(false);
        setBackground(Color.white);
        setRowHeight(Math.max(getFont().getSize() + 5, 16));
        setLargeModel(true);
        setUI(new BasicTreeUI() {
            {
                setHashColor(Color.gray);
            }
        });
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
            if (slist.isBundle()) {
                if (slist.bundle.getClass() == ZippedSWFBundle.class) {
                    return TreeNodeType.BUNDLE_ZIP;
                } else if (slist.bundle.getClass() == SWC.class) {
                    return TreeNodeType.BUNDLE_SWC;
                } else if (slist.bundle.getClass() == IggySwfBundle.class) {
                    return TreeNodeType.BUNDLE_IGGY;
                } else {
                    return TreeNodeType.BUNDLE_BINARY;
                }
            }
        }

        if (t instanceof SetBackgroundColorTag) {
            return TreeNodeType.SET_BACKGROUNDCOLOR;
        }
        if (t instanceof FileAttributesTag) {
            return TreeNodeType.FILE_ATTRIBUTES;
        }
        if (t instanceof MetadataTag) {
            return TreeNodeType.METADATA;
        }
        if (t instanceof PlaceObjectTypeTag) {
            return TreeNodeType.PLACE_OBJECT;
        }
        if (t instanceof RemoveTag) {
            return TreeNodeType.REMOVE_OBJECT;
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
                // same as nested tags of DefineSpriteTag?
                ret = Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                        RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                        StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                        SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID,
                        DefineScalingGridTag.ID);
                break;
            case TagTreeModel.FOLDER_OTHERS:
                ret = Arrays.asList(
                        //CSMTextSettingsTag.ID,
                        DebugIDTag.ID,
                        //DefineButtonCxformTag.ID, DefineButtonSoundTag.ID,
                        //DefineFontAlignZonesTag.ID, DefineFontInfoTag.ID, DefineFontInfo2Tag.ID, DefineFontNameTag.ID,
                        /*DefineScalingGridTag.ID,*/ DefineSceneAndFrameLabelDataTag.ID,
                        DoABC2Tag.ID, DoABCTag.ID, DoActionTag.ID, DoInitActionTag.ID,
                        EnableDebuggerTag.ID, EnableDebugger2Tag.ID, EnableTelemetryTag.ID,
                        ExportAssetsTag.ID, FileAttributesTag.ID, ImportAssetsTag.ID, ImportAssets2Tag.ID,
                        JPEGTablesTag.ID, MetadataTag.ID, ProductInfoTag.ID, ProtectTag.ID, ScriptLimitsTag.ID,
                        SetBackgroundColorTag.ID, SetTabIndexTag.ID, SymbolClassTag.ID);
                break;
        }

        return ret;
    }

    public List<Integer> getFrameNestedTagIds() {
        return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                RemoveObjectTag.ID, RemoveObject2Tag.ID, FrameLabelTag.ID,
                StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID);
    }

    public List<Integer> getNestedTagIds(Tag obj) {
        if (obj instanceof DefineSpriteTag) {
            return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                    RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                    StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                    SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID,
                    DefineScalingGridTag.ID);
        }
        if (obj instanceof FontTag) {
            return Arrays.asList(DefineFontNameTag.ID, DefineFontAlignZonesTag.ID, DefineFontInfoTag.ID, DefineFontInfo2Tag.ID);
        }
        if (obj instanceof TextTag) {
            return Arrays.asList(CSMTextSettingsTag.ID);
        }
        if (obj instanceof DefineButtonTag) {
            return Arrays.asList(DefineButtonCxformTag.ID, DefineButtonSoundTag.ID, DefineScalingGridTag.ID);
        }
        if (obj instanceof DefineButton2Tag) {
            return Arrays.asList(DefineButtonSoundTag.ID, DefineScalingGridTag.ID);
        }
        return null;
    }

    public boolean hasExportableNodes() {
        return !getSelection(mainPanel.getCurrentSwf()).isEmpty();
    }

    public void getAllSubs(TreeItem o, List<TreeItem> ret) {
        TagTreeModel tm = getModel();
        for (TreeItem c : tm.getAllChildren(o)) {
            ret.add(c);
            getAllSubs(c, ret);
        }
    }

    public List<TreeItem> getAllSelected() {
        TreeSelectionModel tsm = getSelectionModel();
        TreePath[] tps = tsm.getSelectionPaths();
        List<TreeItem> ret = new ArrayList<>();
        if (tps == null) {
            return ret;
        }

        for (TreePath tp : tps) {
            TreeItem treeNode = (TreeItem) tp.getLastPathComponent();
            ret.add(treeNode);
            getAllSubs(treeNode, ret);
        }
        return ret;
    }

    public List<TreeItem> getSelected() {
        if (!mainPanel.folderPreviewPanel.selectedItems.isEmpty()) {
            return new ArrayList<>(mainPanel.folderPreviewPanel.selectedItems.values());
        }
        TreeSelectionModel tsm = getSelectionModel();
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
            sel = getAllSelected();
        } else {
            sel = new ArrayList<>();

            for (TreeItem treeItem : mainPanel.folderPreviewPanel.selectedItems.values()) {
                sel.add(treeItem);
                getAllSubs(treeItem, sel);
            }
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
                if (nodeType == TreeNodeType.SPRITE) {
                    ret.add(d);
                }
                if (nodeType == TreeNodeType.BUTTON) {
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
                if (nodeType == TreeNodeType.OTHER_TAG) {
                    if (d instanceof SymbolClassTypeTag) {
                        ret.add(d);
                    }
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
            return mainPanel.folderPreviewPanel.selectedItems.entrySet().iterator().next().getValue();
        }

        TreeItem item = (TreeItem) getLastSelectedPathComponent();
        return item;
    }

    public void updateSwfs(SWF[] swfs) {
        TagTreeModel ttm = getModel();
        if (ttm != null) {
            List<List<String>> expandedNodes = View.getExpandedNodes(this);
            ttm.updateSwf(null); // todo: honfika: update only the changed swfs, but there was an exception when i tried it
            View.expandTreeNodes(this, expandedNodes);
        }
    }

    @Override
    public TagTreeModel getModel() {
        return (TagTreeModel) super.getModel();
    }

    public void expandRoot() {
        TagTreeModel ttm = getModel();
        TreeItem root = ttm.getRoot();
        expandPath(new TreePath(new Object[]{root}));
    }

    public void expandFirstLevelNodes() {
        TagTreeModel ttm = getModel();
        TreeItem root = ttm.getRoot();
        int childCount = ttm.getChildCount(root);
        for (int i = 0; i < childCount; i++) {
            expandPath(new TreePath(new Object[]{root, ttm.getChild(root, i)}));
        }
    }

    public String getSelectionPathString() {
        StringBuilder sb = new StringBuilder();
        TreePath path = getSelectionPath();
        if (path != null) {
            boolean first = true;
            for (Object p : path.getPath()) {
                if (!first) {
                    sb.append("|");
                }

                first = false;
                sb.append(p.toString());
            }
        }

        return sb.toString();
    }

    public void setSelectionPathString(String pathStr) {
        if (pathStr != null && pathStr.length() > 0) {
            String[] path = pathStr.split("\\|");

            TreePath tp = View.getTreePathByPathStrings(this, Arrays.asList(path));
            if (tp != null) {
                // the current view is the Resources view, otherwise tp is null
                mainPanel.setTagTreeSelectedNode((TreeItem) tp.getLastPathComponent());
            }
        }
    }
}
