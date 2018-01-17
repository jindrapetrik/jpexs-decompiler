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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author JPEXS
 */
public class NewTraitDialog extends AppDialog {

    private static final int[] modifiers = new int[]{
        Namespace.KIND_PACKAGE,
        Namespace.KIND_PRIVATE,
        Namespace.KIND_PROTECTED,
        Namespace.KIND_NAMESPACE,
        Namespace.KIND_PACKAGE_INTERNAL,
        Namespace.KIND_EXPLICIT,
        Namespace.KIND_STATIC_PROTECTED
    };

    private static final int[] types = new int[]{
        Trait.TRAIT_METHOD,
        Trait.TRAIT_GETTER,
        Trait.TRAIT_SETTER,
        Trait.TRAIT_CONST,
        Trait.TRAIT_SLOT
    };

    private final JComboBox<String> accessComboBox;

    private final JComboBox<String> typeComboBox;

    private final JCheckBox staticCheckbox;

    private final JTextField nameField;

    private int result = ERROR_OPTION;

    public boolean getStatic() {
        return staticCheckbox.isSelected();
    }

    public int getNamespaceKind() {
        return modifiers[accessComboBox.getSelectedIndex()];
    }

    public int getTraitType() {
        return types[typeComboBox.getSelectedIndex()];
    }

    public String getTraitName() {
        return nameField.getText();
    }

    public NewTraitDialog() {
        setSize(500, 300);
        setTitle(translate("dialog.title"));
        View.centerScreen(this);
        View.setWindowIcon(this);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JPanel optionsPanel = new JPanel(new FlowLayout());
        //optionsPanel.add(new JLabel(translate("label.type")));
        typeComboBox = new JComboBox<>(new String[]{
            translate("type.method"),
            translate("type.getter"),
            translate("type.setter"),
            translate("type.const"),
            translate("type.slot"),});
        staticCheckbox = new JCheckBox(translate("checkbox.static"));
        optionsPanel.add(staticCheckbox);
        String[] accessStrings = new String[modifiers.length];
        for (int i = 0; i < accessStrings.length; i++) {
            String pref = Namespace.kindToPrefix(modifiers[i]);
            String name = Namespace.kindToStr(modifiers[i]);
            accessStrings[i] = (pref.isEmpty() ? "" : pref + " ") + "(" + name + ")";
        }

        //optionsPanel.add(new JLabel(translate("label.access")));
        accessComboBox = new JComboBox<>(accessStrings);
        optionsPanel.add(accessComboBox);

        optionsPanel.add(typeComboBox);

        //optionsPanel.add(new JLabel(translate("label.name")));
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(300, nameField.getPreferredSize().height));
        optionsPanel.add(nameField);

        cnt.add(optionsPanel, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton buttonOk = new JButton(AppStrings.translate("button.ok"));
        buttonOk.addActionListener(this::okButtonActionPerformed);
        JButton buttonCancel = new JButton(AppStrings.translate("button.cancel"));
        buttonCancel.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(buttonOk);
        buttonsPanel.add(buttonCancel);
        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        pack();
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);

        nameField.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                JComponent component = event.getComponent();
                component.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        getRootPane().setDefaultButton(buttonOk);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
            nameField.setText("");
        }

        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        if (nameField.getText().trim().isEmpty()) {
            View.showMessageDialog(null, translate("error.name"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
