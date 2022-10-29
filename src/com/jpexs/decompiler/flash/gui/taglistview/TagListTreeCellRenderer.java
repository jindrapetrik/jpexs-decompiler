/*
 * Copyright (C) 2022 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author JPEXS
 */
public class TagListTreeCellRenderer extends DefaultTreeCellRenderer {

    private Font plainFont;

    private Font boldFont;

    public TagListTreeCellRenderer() {
        if (View.isOceanic()) {
            setUI(new BasicLabelUI());
            setOpaque(false);
            setBackgroundNonSelectionColor(Color.white);
        }
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Object subValue = value;
        if (value instanceof TagListTreeNode) {
            subValue = ((TagListTreeNode) value).getData();
        }
        TreeItem val = null;
        if (subValue instanceof TreeItem) {
            val = (TreeItem) subValue;
        }

        if (val != null && !(val instanceof SWFList) && val.getSwf() == null) {
            // SWF was closed
            value = null;
        }
        Component renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        this.selected = sel;
        if (renderer instanceof JLabel) {
            JLabel lab = (JLabel) renderer;

            if (subValue instanceof TreeItem) {
                lab.setIcon(TagTree.getIconForType(TagTree.getTreeNodeType((TreeItem) subValue)));

                boolean isReadOnly = false;
                if (val instanceof Tag) {
                    isReadOnly = ((Tag) val).isReadOnly();
                }

                boolean isModified;
                if (val instanceof Frame) {
                    isModified = ((Frame)val).isAllInnerTagsModified();
                } else {
                    isModified = val.isModified();
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
            }
        }
        return renderer;
    }
}
