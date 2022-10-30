/*
 *  Copyright (C) 2010-2021 JPEXS
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
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.configuration.Configuration;
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
import com.jpexs.decompiler.flash.tags.EndTag;
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
import com.jpexs.decompiler.flash.types.BUTTONCONDACTION;
import com.jpexs.decompiler.flash.types.CLIPACTIONRECORD;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 *
 * @author JPEXS
 */
public class TagTree extends AbstractTagTree {
    public static class TagTreeCellRenderer extends DefaultTreeCellRenderer {

        private Font plainFont;

        private Font boldFont;

        public TagTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            if (View.isOceanic()) {
                setBackgroundNonSelectionColor(Color.white);
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
                setIcon(getIconForType(type));
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
            if (View.isOceanic()) {
                if (isReadOnly) {
                    setForeground(new Color(0xcc, 0xcc, 0xcc));
                } else {
                    setForeground(Color.BLACK);
                }
            }

            return this;
        }
    }

    public TagTree(TagTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel, mainPanel);                        
        setCellRenderer(new TagTreeCellRenderer());
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

        if (Configuration.useAsTypeIcons.get()) {
            if (t instanceof DoInitActionTag) {
                DoInitActionTag doInit = (DoInitActionTag) t;
                if (doInit.getSwf().getExportName(doInit.spriteId) != null) {
                    return TreeNodeType.AS_CLASS;
                }
                return TreeNodeType.AS_INIT;
            }

            if (t instanceof CLIPACTIONRECORD) {
                return TreeNodeType.AS_CLIP;
            }

            if (t instanceof BUTTONCONDACTION) {
                return TreeNodeType.AS_BUTTON;
            }

            if (t instanceof DoActionTag) {
                return TreeNodeType.AS_FRAME;
            }
        }

        if (t instanceof ASMSource) {
            return TreeNodeType.AS;
        }

        if (t instanceof ScriptPack) {
            if (Configuration.useAsTypeIcons.get()) {
                ScriptPack pack = (ScriptPack) t;
                Trait trait = pack.getPublicTrait();
                if (trait == null) {
                    return TreeNodeType.AS;
                }
                if (trait instanceof TraitFunction) {
                    return TreeNodeType.AS_FUNCTION;
                }
                if (trait instanceof TraitMethodGetterSetter) {
                    return TreeNodeType.AS_FUNCTION;
                }
                if (trait instanceof TraitSlotConst) {
                    TraitSlotConst traitSlotConst = (TraitSlotConst) trait;
                    if (traitSlotConst.isConst()) {
                        return TreeNodeType.AS_CONST;
                    } else {
                        return TreeNodeType.AS_VAR;
                    }
                }
                if (trait instanceof TraitClass) {
                    TraitClass traitClass = (TraitClass) trait;
                    if (pack.abc.instance_info.get(traitClass.class_info).isInterface()) {
                        return TreeNodeType.AS_INTERFACE;
                    }
                    return TreeNodeType.AS_CLASS;
                }
            }
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
        
        if (t instanceof EndTag) {
            return TreeNodeType.END;
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
                // same as nested tags of DefineSpriteTag but without DefineScalingGrid
                ret = Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                        RemoveObjectTag.ID, RemoveObject2Tag.ID, ShowFrameTag.ID, FrameLabelTag.ID,
                        StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                        SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID);
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

    @Override
    public List<Integer> getFrameNestedTagIds() {
        return Arrays.asList(PlaceObjectTag.ID, PlaceObject2Tag.ID, PlaceObject3Tag.ID, PlaceObject4Tag.ID,
                RemoveObjectTag.ID, RemoveObject2Tag.ID, FrameLabelTag.ID,
                StartSoundTag.ID, StartSound2Tag.ID, VideoFrameTag.ID,
                SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID);
    }

    @Override
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

    

    @Override
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

    @Override
    public TreeItem getCurrentTreeItem() {
        if (!mainPanel.folderPreviewPanel.selectedItems.isEmpty()) {
            return mainPanel.folderPreviewPanel.selectedItems.entrySet().iterator().next().getValue();
        }

        return super.getCurrentTreeItem();
    }

    @Override
    public TagTreeModel getModel() {
        return (TagTreeModel) super.getModel();
    }
    
    

    
}
