/*
 *  Copyright (C) 2010-2026 JPEXS
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

import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.gui.tagtree.TagTree;
import com.jpexs.decompiler.flash.tags.Tag;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for selecting a tag type and the number of tags to add.
 *
 * @author JPEXS
 */
public class AddMultipleTagsDialog extends AppDialog {

    private final JMenu tagTypesMenu = new JMenu();

    private final PopupButton tagTypeButton;

    private final JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

    private final JButton okButton = new JButton(translate("button.ok"));

    private Class<? extends Tag> selectedTagClass;

    private TreeNodeType createNodeType;

    private int result = ERROR_OPTION;

    public AddMultipleTagsDialog(Window owner) {
        super(owner);
        setTitle(translate("dialog.title"));
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        tagTypeButton = new PopupButton(translate("tagType.select")) {
            @Override
            protected JPopupMenu getPopupMenu() {
                return tagTypesMenu.getPopupMenu();
            }
        };
        tagTypeButton.setHorizontalAlignment(PopupButton.LEFT);
        tagTypeButton.setPreferredSize(new Dimension(260, tagTypeButton.getPreferredSize().height));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.LINE_START;
        fieldsPanel.add(new JLabel(translate("tagType")), constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        fieldsPanel.add(tagTypeButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        fieldsPanel.add(new JLabel(translate("count")), constraints);

        constraints.gridx = 1;
        countSpinner.setPreferredSize(new Dimension(100, countSpinner.getPreferredSize().height));
        fieldsPanel.add(countSpinner, constraints);

        okButton.setEnabled(false);
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(fieldsPanel, BorderLayout.CENTER);
        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        setModal(true);
        setResizable(false);
        pack();
        View.setWindowIcon(this);
        View.centerScreen(this);
    }

    public JMenu getTagTypesMenu() {
        return tagTypesMenu;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedTagType(Class<?> tagClass, TreeNodeType createNodeType) {
        selectedTagClass = (Class<? extends Tag>) tagClass;
        this.createNodeType = createNodeType;

        String className = tagClass.getSimpleName();
        if (className.endsWith("Tag")) {
            className = className.substring(0, className.length() - 3);
        }
        tagTypeButton.setText(className);
        tagTypeButton.setIcon(TagTree.getIconForType(AbstractTagTree.getTagNodeTypeFromTagClass(selectedTagClass)));
        tagTypeButton.setSelected(false);
        okButton.setEnabled(true);
    }

    public Class<? extends Tag> getSelectedTagClass() {
        return selectedTagClass;
    }

    public TreeNodeType getCreateNodeType() {
        return createNodeType;
    }

    public int getTagCount() {
        return ((Number) countSpinner.getValue()).intValue();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        try {
            countSpinner.commitEdit();
        } catch (ParseException ex) {
            return;
        }
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        result = ERROR_OPTION;
        setVisible(true);
        return result;
    }
}
