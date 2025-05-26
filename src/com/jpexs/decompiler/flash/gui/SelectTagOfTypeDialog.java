/*
 *  Copyright (C) 2022-2025 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * @author JPEXS
 */
public class SelectTagOfTypeDialog extends AppDialog {

    private JComboBox<ComboItem> tagComboBox;
    private Tag result = null;

    public SelectTagOfTypeDialog(Window window, SWF swf, Class<?> tagType, String tagTypeName, int minFrame) {
        super(window);
        setTitle(translate("dialog.title").replace("%type%", tagTypeName));
        Container cnt = getContentPane();

        tagComboBox = new JComboBox<>();
        int pos = 0;
        int frame = 1;
        for (Tag t : swf.getTags()) {
            if (t instanceof ShowFrameTag) {
                frame++;
            }

            if (frame >= minFrame) {
                if (tagType.isAssignableFrom(t.getClass())) {
                    tagComboBox.addItem(new ComboItem(pos, t, frame));
                    pos++;
                }
            }
        }

        tagComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ComboItem item = (ComboItem) value;
                label.setIcon(TagTree.getIconFor(item.tag));
                label.setText(value.toString());
                return label;
            }
        });

        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));
        cnt.add(tagComboBox);
        cnt.add(buttonsPanel);

        pack();
        setModal(true);
        setResizable(false);
        View.setWindowIcon(this);
        View.centerScreen(this);
    }

    public Tag getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    private void okButtonActionPerformed(ActionEvent evt) {
        result = ((ComboItem) tagComboBox.getSelectedItem()).tag;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = null;
        setVisible(false);
    }

    public Tag showDialog() {
        result = null;
        if (tagComboBox.getItemCount() == 0) {
            return null;
        }
        if (tagComboBox.getItemCount() == 1) {
            result = tagComboBox.getItemAt(0).tag;
            return result;
        }
        setVisible(true);
        return result;
    }

    class ComboItem {

        public int index;
        public Tag tag;
        public int frame;

        public ComboItem(int index, Tag tag, int frame) {
            this.index = index;
            this.tag = tag;
            this.frame = frame;
        }

        @Override
        public String toString() {
            return "" + (index + 1) + ". " + tag.toString() + " (frame " + frame + ")";
        }

    }
}
