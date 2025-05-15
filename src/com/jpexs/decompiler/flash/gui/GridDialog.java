/*
 * Copyright (C) 2025 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.enums.GridSnapAccuracy;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class GridDialog extends AppDialog {
    private final ColorSelectionButton colorSelection;
    private final JCheckBox showGridCheckBox;
    private final JCheckBox snapToGridCheckBox;
    private final JCheckBox showOverObjectsCheckBox;
    private final JComboBox<AccuracyItem> snapAccuracyComboBox;
    private final JTextField spacingHorizontalTextField;
    private final JTextField spacingVerticalTextField;
    
    public GridDialog(Window owner) {
        super(owner);
        setSize(800, 600);
        setTitle(translate("dialog.title"));
        Container cnt = getContentPane();
        
        JPanel centralPanel = new JPanel(new GridBagLayout());
        centralPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        
        JLabel colorLabel = new JLabel(translate("color"));
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_END;
        
        centralPanel.add(colorLabel, c);
        
        colorSelection = new ColorSelectionButton(Configuration.gridColor.get(), null);
        colorLabel.setLabelFor(colorSelection);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;        
        centralPanel.add(colorSelection, c);

        showGridCheckBox = new JCheckBox(translate("show"));
        showGridCheckBox.setSelected(Configuration.showGrid.get());
        c.gridy++;
        centralPanel.add(showGridCheckBox, c);
                
        snapToGridCheckBox = new JCheckBox(translate("snapTo"));
        snapToGridCheckBox.setSelected(Configuration.snapToGrid.get());
        c.gridy++;
        centralPanel.add(snapToGridCheckBox, c);
        
        showOverObjectsCheckBox = new JCheckBox(translate("showOverObjects"));
        showOverObjectsCheckBox.setSelected(Configuration.gridOverObjects.get());
        c.gridy++;
        centralPanel.add(showOverObjectsCheckBox, c);
                
        JLabel spacingHorizontalLabel = new JLabel(translate("spacing.x"));
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;    
        centralPanel.add(spacingHorizontalLabel, c);
        
        spacingHorizontalTextField = new JTextField(10);
        spacingHorizontalLabel.setLabelFor(spacingHorizontalTextField);
        spacingHorizontalTextField.setText("" + Configuration.gridHorizontalSpace.get());        
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;
        centralPanel.add(spacingHorizontalTextField, c);
                        
        JLabel spacingVerticalLabel = new JLabel(translate("spacing.y"));
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;    
        centralPanel.add(spacingVerticalLabel, c);
        
        spacingVerticalTextField = new JTextField(10);
        spacingVerticalLabel.setLabelFor(spacingVerticalTextField);
        spacingVerticalTextField.setText("" + Configuration.gridVerticalSpace.get());
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;
        centralPanel.add(spacingVerticalTextField, c);        
        
        JLabel snapAccuracyLabel = new JLabel(translate("snapAccuracy"));
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;        
        centralPanel.add(snapAccuracyLabel, c);
        
        snapAccuracyComboBox = new JComboBox<>(
                new AccuracyItem[] {
                    new AccuracyItem(GridSnapAccuracy.MUST_BE_CLOSE),
                    new AccuracyItem(GridSnapAccuracy.NORMAL),
                    new AccuracyItem(GridSnapAccuracy.CAN_BE_DISTANT),
                    new AccuracyItem(GridSnapAccuracy.ALWAYS_SNAP)
                }
        );
        snapAccuracyLabel.setLabelFor(snapAccuracyComboBox);
        
        snapAccuracyComboBox.setSelectedItem(new AccuracyItem(Configuration.gridSnapAccuracy.get()));
        
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;                
        centralPanel.add(snapAccuracyComboBox, c);                
        
        cnt.setLayout(new BorderLayout());
        cnt.add(centralPanel, BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);        
        
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);               
        
        buttonsPanel.add(okButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(cancelButton);
        
        cnt.add(buttonsPanel, BorderLayout.EAST);
                        
        pack();
        View.setWindowIcon16(this, "grid");
        View.centerScreen(this);
        setModal(true);            
        setResizable(false);
    }
    
    private void okButtonActionPerformed(ActionEvent evt) {
        int horizontalSpacing;
        try {
            horizontalSpacing = Integer.parseInt(spacingHorizontalTextField.getText());
        } catch (NumberFormatException nfe) {
            ViewMessages.showMessageDialog(this, translate("error.invalidSpacing"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            spacingHorizontalTextField.requestFocus();
            return;
        }
        int verticalSpacing;
        try {
            verticalSpacing = Integer.parseInt(spacingVerticalTextField.getText());
        } catch (NumberFormatException nfe) {
            ViewMessages.showMessageDialog(this, translate("error.invalidSpacing"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            spacingVerticalTextField.requestFocus();
            return;
        }
        
        Configuration.gridHorizontalSpace.set(horizontalSpacing);
        Configuration.gridVerticalSpace.set(verticalSpacing);
        Configuration.gridColor.set(colorSelection.getValue());
        Configuration.showGrid.set(showGridCheckBox.isSelected());
        Configuration.snapToGrid.set(snapToGridCheckBox.isSelected());
        Configuration.gridOverObjects.set(showOverObjectsCheckBox.isSelected());
        Configuration.gridSnapAccuracy.set(((AccuracyItem) snapAccuracyComboBox.getSelectedItem()).acurracy);
        setVisible(false);
    }
    
    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }
    
    private class AccuracyItem {
        private GridSnapAccuracy acurracy;

        public AccuracyItem(GridSnapAccuracy acurracy) {
            this.acurracy = acurracy;
        }

        public GridSnapAccuracy getAcurracy() {
            return acurracy;
        }                

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(this.acurracy);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AccuracyItem other = (AccuracyItem) obj;
            return this.acurracy == other.acurracy;
        }

        
        
        @Override
        public String toString() {
            switch (acurracy) {
                case MUST_BE_CLOSE:
                    return translate("snapAccuracy.mustBeClose");
                case NORMAL:
                    return translate("snapAccuracy.normal");  
                case CAN_BE_DISTANT:
                    return translate("snapAccuracy.canBeDistant");  
                case ALWAYS_SNAP:
                    return translate("snapAccuracy.alwaysSnap");
            }
            return "unknown";
        }                
    }
    
}
