/*
 *  Copyright (C) 2022-2023 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.TreeNodeType;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
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
 *
 * @author JPEXS
 */
public class SelectDoABCDialog extends AppDialog {

    private JComboBox<ComboItem> abcComboBox;
    private ABCContainerTag result = null;

    public SelectDoABCDialog(Window window, SWF swf) {
        super(window);
        setTitle(translate("dialog.title"));
        Container cnt = getContentPane();

        abcComboBox = new JComboBox<>();
        int pos = 0;
        for (Tag t : swf.getTags()) {
            if (t instanceof ABCContainerTag) {
                abcComboBox.addItem(new ComboItem(pos, (ABCContainerTag) t));
                pos++;
            }
        }

        abcComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setIcon(TagTree.getIconForType(TreeNodeType.AS));
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
        cnt.add(abcComboBox);
        cnt.add(buttonsPanel);

        pack();
        setModal(true);
        setResizable(false);
        View.setWindowIcon(this);
        View.centerScreen(this);
    }

    public ABCContainerTag getResult() {
        return result;
    }

    @SuppressWarnings("unchecked")
    private void okButtonActionPerformed(ActionEvent evt) {
        result = ((ComboItem) abcComboBox.getSelectedItem()).abc;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = null;
        setVisible(false);
    }

    public ABCContainerTag showDialog() {
        result = null;
        if (abcComboBox.getItemCount() == 0) {
            return null;
        }
        if (abcComboBox.getItemCount() == 1) {
            result = abcComboBox.getItemAt(0).abc;
            return result;
        }
        setVisible(true);
        return result;
    }

    class ComboItem {

        public int index;
        public ABCContainerTag abc;

        public ComboItem(int index, ABCContainerTag abc) {
            this.index = index;
            this.abc = abc;
        }

        @Override
        public String toString() {
            return "" + (index + 1) + ". " + abc.toString();
        }

    }
}
