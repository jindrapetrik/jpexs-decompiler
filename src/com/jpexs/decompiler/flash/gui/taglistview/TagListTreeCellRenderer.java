/*
 *  Copyright (C) 2022 JPEXS
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
package com.jpexs.decompiler.flash.gui.taglistview;

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.treeitems.SWFList;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        TreeItem val = null;
        if (value instanceof TreeItem) {
            val = (TreeItem) value;
        }

        if (val != null && !(val instanceof SWFList) && val.getSwf() == null) {
            // SWF was closed
            value = null;
        }
        Component renderer = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        this.selected = sel;
        if (renderer instanceof JLabel) {
            JLabel lab = (JLabel) renderer;

            if (value instanceof TreeItem) {
                lab.setIcon(TagTree.getIconForType(TagTree.getTreeNodeType((TreeItem) value)));

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
                setToolTipText(null);
                AbstractTagTree aTree = (AbstractTagTree) tree;
                Map<TreeItem, Set<Integer>> allMissingNeededCharacters = aTree.getMissingNeededCharacters();
                if (allMissingNeededCharacters.containsKey((TreeItem)value)) {
                    Set<Integer> missingNeededCharacters = allMissingNeededCharacters.get(value);
                    if (!missingNeededCharacters.isEmpty()) {
                        List<String> missingAsStr = new ArrayList<>();
                        for (int v:missingNeededCharacters) {
                            missingAsStr.add("" + v);
                        }
                        if (missingAsStr.size() == 1) {
                            lab.setToolTipText(AppStrings.translate("error.missing.characterTag.single").replace("%tag%", missingAsStr.get(0)));
                        } else {
                            lab.setToolTipText(AppStrings.translate("error.missing.characterTag.multi").replace("%tags%",String.join(", ", missingAsStr)));
                        }
                        lab.setForeground(Color.red);                       
                    }
                }
            }
        }
        return renderer;
    }
}
