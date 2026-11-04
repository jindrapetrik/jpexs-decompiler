/*
 *  Copyright (C) 2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.ImagePanel;
import com.jpexs.decompiler.flash.gui.TimelinedMaker;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

final class ReplaceTimelineContentDialog {

    private ReplaceTimelineContentDialog() {
    }

    static CharacterTag choose(Component parent, Timelined timelined, Set<Point> cells,
            Set<Integer> originalIds, CharacterTag preselected) {
        SWF swf = timelined.getSwf();
        List<CharacterTag> choices = new ArrayList<>();
        for (CharacterTag character : swf.getCharacters(false).values()) {
            if (canPlace(character, timelined)
                    && !(originalIds.size() == 1 && originalIds.contains(character.getCharacterId()))) {
                choices.add(character);
            }
        }

        String title = EasyStrings.translate("action.replaceSelectedContent");
        if (choices.isEmpty()) {
            JOptionPane.showMessageDialog(parent, EasyStrings.translate("replaceContent.noChoices"),
                    title, JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        choices.sort(Comparator.comparingInt(CharacterTag::getCharacterId));
        DefaultMutableTreeNode root = LibraryTreeTable.createLibraryRoot(choices);
        JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(18);
        tree.setUI(new BasicTreeUI());
        tree.setCellRenderer(new LibraryTreeCellRenderer() );
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        if (View.isOceanic()) {
            tree.setBackground(Color.WHITE);
        }

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        View.installEscapeCloseOperation(dialog);

        JLabel summary = new JLabel(describeCells(cells));
        JLabel prompt = new JLabel(EasyStrings.translate("replaceContent.choose"));
        prompt.setLabelFor(tree);
        JPanel heading = new JPanel(new BorderLayout(0, 6));
        heading.add(summary, BorderLayout.NORTH);
        heading.add(prompt, BorderLayout.SOUTH);

        JScrollPane treePane = new JScrollPane(tree);
        treePane.setPreferredSize(new Dimension(250, 230));
        ImagePanel preview = new ImagePanel();
        preview.setTopPanelVisible(false);
        preview.setPreferredSize(new Dimension(240, 230));

        JButton replace = new JButton(EasyStrings.translate("replaceContent.confirm"));
        replace.setEnabled(false);
        replace.addActionListener(event -> {
            dialog.getRootPane().putClientProperty("replacement", getSelectedCharacter(tree));
            dialog.dispose();
        });
        JButton cancel = new JButton(AppStrings.translate("button.cancel"));
        cancel.addActionListener(event -> dialog.dispose());

        tree.addTreeSelectionListener(event -> {
            CharacterTag character = getSelectedCharacter(tree);
            replace.setEnabled(character != null);
            if (character == null) {
                preview.clearAll();
            } else {
                preview.setTimelined(TimelinedMaker.makeTimelined(character), swf,
                        -1, false, true, true, true, true, false, true, true, true);
                preview.zoomFit();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        buttons.add(replace);
        buttons.add(cancel);

        JPanel content = new JPanel(new BorderLayout(12, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(heading, BorderLayout.NORTH);
        content.add(treePane, BorderLayout.WEST);
        content.add(preview, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(replace);
        dialog.pack();
        View.centerScreen(dialog);

        if (choices.contains(preselected)) {
            Enumeration<?> nodes = root.breadthFirstEnumeration();
            while (nodes.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
                if (node.getUserObject() == preselected) {
                    TreePath path = new TreePath(node.getPath());
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                    break;
                }
            }
        }
        tree.requestFocusInWindow();
        try {
            dialog.setVisible(true);
            return (CharacterTag) dialog.getRootPane().getClientProperty("replacement");
        } finally {
            preview.clearAll();
            dialog.dispose();
        }
    }

    private static class LibraryTreeCellRenderer extends DefaultTreeCellRenderer {   

        public LibraryTreeCellRenderer() {
            setOpaque(false);
            if (View.isOceanic()) {
                setBackgroundNonSelectionColor(Color.white);
            }
        }
        
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value,
                    selected, expanded, leaf, row, hasFocus);
            LibraryTreeTable.styleLibraryNode(label, value);

            if (View.isOceanic()) {
                /*if (selected) {
                    label.setBackground(getBackgroundSelectionColor());
                } else {
                    label.setBackground(Color.white);
                }
                label.setOpaque(true);*/
                /*if (selected) {
                    label.setBackground(super.getBackgroundSelectionColor());
                } else {
                    label.setBackground(Color.white);
                }*/
                //label.setOpaque(true);
            }
            return label;
        }        
    }
    
    private static CharacterTag getSelectedCharacter(JTree tree) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        Object item = node == null ? null : node.getUserObject();
        return item instanceof CharacterTag ? (CharacterTag) item : null;
    }

    private static boolean canPlace(CharacterTag character, Timelined timelined) {
        if (!(character instanceof DefineSpriteTag || character instanceof ShapeTag
                || character instanceof MorphShapeTag || character instanceof TextTag || character instanceof ButtonTag)
                || character == timelined) {
            return false;
        }
        if (character.getSwf().getCyclicCharacters().contains(character.getCharacterId())) {
            return false;
        }
        if (timelined instanceof CharacterTag) {
            Set<Integer> dependencies = new HashSet<>();
            character.getNeededCharactersDeep(dependencies, new HashSet<>());
            return !dependencies.contains(((CharacterTag) timelined).getCharacterId());
        }
        return true;
    }

    private static String describeCells(Set<Point> cells) {
        Set<Integer> layers = new HashSet<>();
        int firstFrame = Integer.MAX_VALUE;
        int lastFrame = 0;
        for (Point cell : cells) {
            layers.add(cell.y);
            firstFrame = Math.min(firstFrame, cell.x + 1);
            lastFrame = Math.max(lastFrame, cell.x + 1);
        }
        if (layers.size() == 1 && cells.size() == lastFrame - firstFrame + 1) {
            String frames = firstFrame == lastFrame ? Integer.toString(firstFrame) : firstFrame + "\u2013" + lastFrame;
            return EasyStrings.translate("replaceContent.range")
                    .replace("%layer%", layers.iterator().next().toString())
                    .replace("%frames%", frames);
        }
        return EasyStrings.translate("replaceContent.selection")
                .replace("%cells%", Integer.toString(cells.size()))
                .replace("%layers%", Integer.toString(layers.size()));
    }
}
