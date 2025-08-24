/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.MainPanel;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DebugIDTag;
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
import com.jpexs.decompiler.flash.tags.PlaceImagePrivateTag;
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
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalImage2;
import com.jpexs.decompiler.flash.tags.gfx.DefineSubImage;
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.OpenableList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.helpers.Helper;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author JPEXS
 */
public class TagTree extends AbstractTagTree {

    public static class TagTreeCellRenderer extends DefaultTreeCellRenderer {

        private Font plainFont;

        private Font boldFont;

        private boolean semiTransparent = false;

        public TagTreeCellRenderer() {
            setUI(new BasicLabelUI());
            setOpaque(false);
            if (View.isOceanic()) {
                setBackgroundNonSelectionColor(Color.white);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (semiTransparent) {
                if (getIcon() != null) {
                    Color color = getBackground();
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 2));
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
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

            TreeItem val = (TreeItem) value;
            if (val != null && !(val instanceof OpenableList) && val.getOpenable() == null) {
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

            setIcon(getIconFor(val, expanded));

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

            if (isReadOnly) {
                isModified = false;
            }

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
            setToolTipText(null);

            AbstractTagTree aTree = (AbstractTagTree) tree;
            Map<TreeItem, Set<Integer>> allMissingNeededCharacters = aTree.getMissingNeededCharacters();
            if (allMissingNeededCharacters.containsKey((TreeItem) value)) {
                Set<Integer> missingNeededCharacters = allMissingNeededCharacters.get(value);
                if (!missingNeededCharacters.isEmpty()) {
                    List<String> missingAsStr = new ArrayList<>();
                    for (int v : missingNeededCharacters) {
                        missingAsStr.add("" + v);
                    }
                    if (missingAsStr.size() == 1) {
                        setToolTipText(AppStrings.translate("error.missing.characterTag.single").replace("%tag%", missingAsStr.get(0)));
                    } else {
                        setToolTipText(AppStrings.translate("error.missing.characterTag.multi").replace("%tags%", String.join(", ", missingAsStr)));
                    }
                    setForeground(Color.red);
                }
            }

            semiTransparent = false;
            if (aTree.getMainPanel().isClipboardCut() && aTree.getMainPanel().clipboardContains(val)) {
                semiTransparent = true;
            }
            int itemIndex = aTree.getFullModel().getItemIndex(val);
            if (itemIndex > 1) {
                setText(val.toString() + " [" + itemIndex + "]");
            }

            return this;
        }
    }

    public TagTree(TagTreeModel treeModel, MainPanel mainPanel) {
        super(treeModel, mainPanel);
        setCellRenderer(new TagTreeCellRenderer());
    }

    public static List<Integer> getSwfFolderItemNestedTagIds(String folderName, boolean gfx) {
        List<Integer> ret = new ArrayList<>();
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
                if (gfx) {
                    ret = Arrays.asList(DefineBitsTag.ID, DefineBitsJPEG2Tag.ID, DefineBitsJPEG3Tag.ID, DefineBitsJPEG4Tag.ID, DefineBitsLosslessTag.ID, DefineBitsLossless2Tag.ID,
                            DefineExternalImage.ID, DefineExternalImage2.ID, DefineSubImage.ID
                    );
                } else {
                    ret = Arrays.asList(DefineBitsTag.ID, DefineBitsJPEG2Tag.ID, DefineBitsJPEG3Tag.ID, DefineBitsJPEG4Tag.ID, DefineBitsLosslessTag.ID, DefineBitsLossless2Tag.ID);
                }
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
                        SoundStreamBlockTag.ID, SoundStreamHeadTag.ID, SoundStreamHead2Tag.ID,
                        SetTabIndexTag.ID, PlaceImagePrivateTag.ID);
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
                        SetBackgroundColorTag.ID,
                        //SetTabIndexTag.ID, 
                        SymbolClassTag.ID);
                break;
        }

        return ret;
    }

    @Override
    public List<TreeItem> getSelection(Openable openable) {
        List<TreeItem> sel;
        if (!(mainPanel.folderPreviewCard.isVisible() && mainPanel.folderPreviewPanel.isSomethingSelected())) {
            sel = getAllSelected();
        } else {
            sel = new ArrayList<>();

            List<TreeItem> siSorted = mainPanel.folderPreviewPanel.getSelectedItemsSorted();

            for (TreeItem treeItem : siSorted) {
                sel.add(treeItem);
                getAllSubs(treeItem, sel);
            }
        }
        return getSelection(openable, sel);
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
    public TagTreeModel getFullModel() {
        return (TagTreeModel) super.getFullModel();
    }

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DoInitActionTag) {
            DoInitActionTag tag = (DoInitActionTag) value;
            String expName = tag.getSwf().getExportName(tag.getCharacterId());
            if (expName != null && !expName.isEmpty()) {
                String[] pathParts = expName.contains(".") ? expName.split("\\.") : new String[]{expName};
                if (expName.startsWith("__Packages.")) {
                    return IdentifiersDeobfuscation.printIdentifier(tag.getSwf(), new LinkedHashSet<>(), false, pathParts[pathParts.length - 1]);
                } else {
                    return Helper.escapeExportname(tag.getSwf(), expName, false);
                }                
            }
        }
        if (value != null) {
            String sValue = value.toString();
            if (sValue != null) {
                return sValue;
            }
        }
        return "";
    }
}
