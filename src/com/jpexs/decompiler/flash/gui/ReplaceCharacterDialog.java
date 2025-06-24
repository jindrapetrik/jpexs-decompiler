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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.tagtree.AbstractTagTree;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author JPEXS
 */
public class ReplaceCharacterDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    //private final JComboBox<ComboBoxItem<CharacterTag>> charactersComboBox = new JComboBox<>();
    private final JList<ComboBoxItem<CharacterTag>> charactersListBox = new JList<>();

    private final JTextField searchTextField = new JTextField(10);

    private DefaultListModel<ComboBoxItem<CharacterTag>> fullModel = new DefaultListModel<>();

    private int result = ERROR_OPTION;

    public ReplaceCharacterDialog(Window owner) {
        super(owner);
        setSize(400, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel(translate("replace.with")), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout());
        JLabel iconSearchLabel = new JLabel(View.getIcon("search16"));
        searchPanel.add(iconSearchLabel);
        searchPanel.add(searchTextField);
        topPanel.add(searchPanel, BorderLayout.EAST);

        cnt.add(topPanel, BorderLayout.NORTH);

        charactersListBox.setModel(new DefaultListModel<>());
        if (View.isOceanic()) {
            charactersListBox.setBackground(Color.white);
        }
        charactersListBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        charactersListBox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                okButton.setEnabled(charactersListBox.getSelectedIndex() > -1);
                if (okButton.isEnabled()) {
                    getRootPane().setDefaultButton(okButton);
                } else {
                    getRootPane().setDefaultButton(null);
                }
            }
        });

        okButton.setEnabled(false);

        JScrollPane sc = new JScrollPane(charactersListBox);
        sc.setPreferredSize(new Dimension(400, 300));

        cnt.add(sc, BorderLayout.CENTER);

        charactersListBox.setCellRenderer(new CharacterTagListCellRenderer());

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        cnt.add(panButtons, BorderLayout.SOUTH);

        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                String searchedText = searchTextField.getText();
                ComboBoxItem<CharacterTag> selectedValue = charactersListBox.getSelectedValue();
                DefaultListModel<ComboBoxItem<CharacterTag>> filteredModel = new DefaultListModel<>();
                int newSelectedIndex = -1;
                boolean isEmpty = searchedText.trim().isEmpty();
                for (int i = 0; i < fullModel.getSize(); i++) {
                    ComboBoxItem<CharacterTag> element = fullModel.getElementAt(i);
                    if (isEmpty || element.toString().toLowerCase().contains(searchedText.toLowerCase())) {
                        filteredModel.addElement(element);
                        if (selectedValue == element) {
                            newSelectedIndex = filteredModel.size() - 1;
                        }
                    }
                }
                charactersListBox.setModel(filteredModel);
                if (newSelectedIndex != -1) {
                    charactersListBox.setSelectedIndex(newSelectedIndex);
                }
            }
        });

        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        setTitle(translate("dialog.title"));
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

        ComboBoxItem<CharacterTag> item = charactersListBox.getSelectedValue();
        if (item == null) {
            return null;
        }
        return item.getValue().getCharacterId();
    }

    public int showDialog(SWF swf, int selectedCharacterId) {
        Map<Integer, CharacterTag> characters = swf.getCharacters(false);
        fullModel = new DefaultListModel<>();
        fullModel.clear();
        for (Integer key : characters.keySet()) {
            CharacterTag character = characters.get(key);
            if (character.isImported()) {
                continue;
            }
            int characterId = character.getCharacterId();
            if (characterId != selectedCharacterId) {
                fullModel.addElement(new ComboBoxItem<>(character.getName(), character));
            }
        }
        charactersListBox.setModel(fullModel);

        setVisible(true);
        return result;
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
}
