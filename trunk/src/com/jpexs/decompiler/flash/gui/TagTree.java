/*
 *  Copyright (C) 2013 JPEXS
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

import com.jpexs.decompiler.flash.AS3PackageNodeItem;
import com.jpexs.decompiler.flash.FrameNodeItem;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.TreeElementItem;
import com.jpexs.decompiler.flash.TreeItem;
import com.jpexs.decompiler.flash.abc.ScriptPack;
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
import com.jpexs.decompiler.flash.tags.gfx.DefineCompactedFont;
import com.jpexs.decompiler.flash.treenodes.TreeNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author JPEXS
 */
public class TagTree extends JTree {

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
                String tagTypeStr = type.toString().toLowerCase().replace("_", "");
                setIcon(View.getIcon(tagTypeStr + "16"));
                //setToolTipText("This book is in the Tutorial series.");
            } else {
                //setToolTipText(null); //no tool tip
            }

            String tos = value.toString();
            int sw = getFontMetrics(getFont()).stringWidth(tos);
            setPreferredSize(new Dimension(18 + sw, getPreferredSize().height));
            setUI(new BasicLabelUI());
            setOpaque(false);
            //setBackground(Color.green);
            setBackgroundNonSelectionColor(Color.white);
            //setBackgroundSelectionColor(Color.ORANGE);
            
            if (treeNode instanceof TreeNode) {
                if (treeNode.getItem() instanceof Tag) {
                    Tag tag = (Tag) treeNode.getItem();
                    if (tag.isModified()) {
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                }
            }
            return this;
        }
    }

    TagTree(TagTreeModel treeModel) {
        super(treeModel);
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

        return TreeNodeType.FOLDER;
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
}
