/*
 *  Copyright (C) 2010-2022 JPEXS
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
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class ReplaceCharacterDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JComboBox<ComboBoxItem<CharacterTag>> charactersComboBox = new JComboBox<>();

    private int result = ERROR_OPTION;

    public ReplaceCharacterDialog(Window owner) {
        super(owner);
        setSize(400, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new JLabel(translate("replace.with")), BorderLayout.NORTH);

        charactersComboBox.setPreferredSize(new Dimension(400, charactersComboBox.getPreferredSize().height));
        add(charactersComboBox, BorderLayout.CENTER);
        
        charactersComboBox.setRenderer(new CharacterTagListCellRenderer());
        

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        add(panButtons, BorderLayout.SOUTH);

        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        setTitle(translate("dialog.title"));
        getRootPane().setDefaultButton(okButton);
        pack();
        View.centerScreen(this);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public Integer getCharacterId() {
        if (result == ERROR_OPTION) {
            return null;
        }

        @SuppressWarnings("unchecked")
        ComboBoxItem<CharacterTag> item = (ComboBoxItem<CharacterTag>) charactersComboBox.getSelectedItem();
        return item.getValue().getCharacterId();
    }

    public int showDialog(SWF swf, int selectedCharacterId) {
        Map<Integer, CharacterTag> characters = swf.getCharacters();
        for (Integer key : characters.keySet()) {
            CharacterTag character = characters.get(key);
            int characterId = character.getCharacterId();
            if (characterId != selectedCharacterId) {
                charactersComboBox.addItem(new ComboBoxItem<>(character.getName(), character));
            }
        }

        setVisible(true);
        return result;
    }
}

class CharacterTagListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            @SuppressWarnings("unchecked")
            ComboBoxItem<CharacterTag> comboboxItem = (ComboBoxItem<CharacterTag>) value;
            label.setIcon(AbstractTagTree.getIconForType(AbstractTagTree.getTreeNodeType(comboboxItem.getValue())));
        }
        return component;
    }       
}