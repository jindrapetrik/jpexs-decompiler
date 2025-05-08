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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class SnappingDialog extends AppDialog {

    private final JCheckBox snapAlignCheckBox;

    private final JCheckBox snapToGridCheckBox;

    private final JCheckBox snapToGuidesCheckBox;

    private final JCheckBox snapToPixelsCheckBox;
    private final JCheckBox snapToObjectsCheckBox;

    private final JTextField stageBorderTextField;
    private final JTextField objectHorizontalSpacingTextField;
    private final JTextField objectVerticalSpacingTextField;

    private final JCheckBox centerAlignmentHorizontalCheckBox;
    private final JCheckBox centerAlignmentVerticalCheckBox;

    public SnappingDialog(Window owner) {
        super(owner);
        setSize(800, 600);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();

        cnt.setLayout(new BorderLayout());
        JPanel snapPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;

        snapAlignCheckBox = new JCheckBox(translate("snapAlign"));
        snapAlignCheckBox.setSelected(Configuration.snapAlign.get());
        snapPanel.add(snapAlignCheckBox, c);

        snapToGridCheckBox = new JCheckBox(translate("snapToGrid"));
        snapToGridCheckBox.setSelected(Configuration.snapToGrid.get());
        c.gridy++;
        snapPanel.add(snapToGridCheckBox, c);

        snapToGuidesCheckBox = new JCheckBox(translate("snapToGuides"));
        snapToGuidesCheckBox.setSelected(Configuration.snapToGuides.get());
        c.gridy++;
        snapPanel.add(snapToGuidesCheckBox, c);

        snapToPixelsCheckBox = new JCheckBox(translate("snapToPixels"));
        snapToPixelsCheckBox.setSelected(Configuration.snapToPixels.get());
        c.gridy++;
        snapPanel.add(snapToPixelsCheckBox, c);

        snapToObjectsCheckBox = new JCheckBox(translate("snapToObjects"));
        snapToObjectsCheckBox.setSelected(Configuration.snapToObjects.get());
        c.gridy++;
        snapPanel.add(snapToObjectsCheckBox, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        snapPanel.add(new JPanel(), c);

        JPanel centralPanel = new JPanel();
        centralPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
        centralPanel.add(snapPanel);
        centralPanel.add(Box.createVerticalStrut(10));

        JPanel snapAlignPanel = new JPanel(new GridBagLayout());
        snapAlignPanel.setBorder(BorderFactory.createTitledBorder(translate("snapAlign.settings")));
        c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        JLabel objectSpacingLabel = new JLabel(translate("snapAlign.objectSpacing"));
        JLabel objectHorizontalSpacingLabel = new JLabel(translate("snapAlign.objectSpacing.horizontal"));
        JLabel objectVerticalSpacingLabel = new JLabel(translate("snapAlign.objectSpacing.vertical"));
        JLabel centerAlignmentLabel = new JLabel(translate("snapAlign.centerAlignment"));
        
        JLabel stageBorderLabel = new JLabel(translate("snapAlign.stageBorder"));
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_END;
        snapAlignPanel.add(stageBorderLabel, c);

        stageBorderTextField = new JTextField(10);
        stageBorderLabel.setLabelFor(stageBorderTextField);
        
        stageBorderTextField.setText("" + Configuration.snapAlignStageBorder.get());
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;
        snapAlignPanel.add(stageBorderTextField, c);

        c.gridx = 0;
        c.gridy++;
        snapAlignPanel.add(objectSpacingLabel, c);

        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;
        snapAlignPanel.add(objectHorizontalSpacingLabel, c);

        objectHorizontalSpacingTextField = new JTextField(10);
        objectHorizontalSpacingLabel.setLabelFor(objectHorizontalSpacingTextField);
        objectHorizontalSpacingTextField.setText("" + Configuration.snapAlignObjectHorizontalSpace.get());        
        
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;
        snapAlignPanel.add(objectHorizontalSpacingTextField, c);

        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;
        snapAlignPanel.add(objectVerticalSpacingLabel, c);

        objectVerticalSpacingTextField = new JTextField(10);
        objectVerticalSpacingLabel.setLabelFor(objectVerticalSpacingTextField);
        objectVerticalSpacingTextField.setText("" + Configuration.snapAlignObjectVerticalSpace.get());
        
        c.gridx++;
        c.anchor = GridBagConstraints.LINE_START;
        snapAlignPanel.add(objectVerticalSpacingTextField, c);

        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        snapAlignPanel.add(centerAlignmentLabel, c);

        centerAlignmentHorizontalCheckBox = new JCheckBox(translate("snapAlign.centerAlignment.horizontal"));
        centerAlignmentHorizontalCheckBox.setSelected(Configuration.snapAlignCenterAlignmentHorizontal.get());
        c.gridy++;
        snapAlignPanel.add(centerAlignmentHorizontalCheckBox, c);
        
        centerAlignmentVerticalCheckBox = new JCheckBox(translate("snapAlign.centerAlignment.vertical"));
        centerAlignmentVerticalCheckBox.setSelected(Configuration.snapAlignCenterAlignmentVertical.get());
        c.gridy++;
        snapAlignPanel.add(centerAlignmentVerticalCheckBox, c);

        centralPanel.add(snapAlignPanel);

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

        cnt.add(centralPanel, BorderLayout.CENTER);
        cnt.add(buttonsPanel, BorderLayout.EAST);

        pack();
        View.setWindowIcon16(this, "snap");
        View.centerScreen(this);
        setModal(true);      
        setResizable(false);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        int spacingX;
        try {
            spacingX = Integer.parseInt(objectHorizontalSpacingTextField.getText());
        } catch (NumberFormatException nfe) {
            ViewMessages.showMessageDialog(this, translate("error.invalidSpacing"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            objectHorizontalSpacingTextField.requestFocus();
            return;
        }
        int spacingY;
        try {
            spacingY = Integer.parseInt(objectVerticalSpacingTextField.getText());
        } catch (NumberFormatException nfe) {
            ViewMessages.showMessageDialog(this, translate("error.invalidSpacing"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            objectVerticalSpacingTextField.requestFocus();
            return;
        }
        
        int stageBorder;
        try {
            stageBorder = Integer.parseInt(stageBorderTextField.getText());
        } catch (NumberFormatException nfe) {
            ViewMessages.showMessageDialog(this, translate("error.invalidBorder"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            stageBorderTextField.requestFocus();
            return;
        }
                
        Configuration.snapAlign.set(snapAlignCheckBox.isSelected());
        Configuration.snapToGrid.set(snapToGridCheckBox.isSelected());
        Configuration.snapToGuides.set(snapToGuidesCheckBox.isSelected());
        Configuration.snapToPixels.set(snapToPixelsCheckBox.isSelected());
        Configuration.snapToObjects.set(snapToObjectsCheckBox.isSelected());
        
        Configuration.snapAlignObjectHorizontalSpace.set(spacingX);
        Configuration.snapAlignObjectVerticalSpace.set(spacingY);
        Configuration.snapAlignStageBorder.set(stageBorder);
        
        Configuration.snapAlignCenterAlignmentHorizontal.set(centerAlignmentHorizontalCheckBox.isSelected());
        Configuration.snapAlignCenterAlignmentVertical.set(centerAlignmentVerticalCheckBox.isSelected());
        
        setVisible(false);
    }
    /*
    

    private final JTextField stageBorderTextField;
    private final JTextField objectHorizontalSpacingTextField;
    private final JTextField objectVerticalSpacingTextField;

    private final JCheckBox centerAlignmentHorizontalCheckBox;
    private final JCheckBox centerAlignmentVerticalCheckBox;
    */

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }
}
