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
import com.jpexs.decompiler.flash.configuration.enums.GuidesSnapAccuracy;
import com.jpexs.decompiler.flash.gui.player.MediaDisplay;
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
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class GuidesDialog extends AppDialog {
    
    private final ColorSelectionButton colorSelection;
    private final JCheckBox showGuidesCheckBox;
    private final JCheckBox snapToGuidesCheckBox;
    private final JCheckBox lockGuidesCheckBox;
    private final JComboBox<AccuracyItem> snapAccuracyComboBox;
    private final MediaDisplay mediaDisplay;
    
    public GuidesDialog(Window owner, MediaDisplay mediaDisplay) {
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
        
        colorSelection = new ColorSelectionButton(Configuration.guidesColor.get(), null);
        colorLabel.setLabelFor(colorSelection);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;        
        centralPanel.add(colorSelection, c);

        showGuidesCheckBox = new JCheckBox(translate("show"));
        showGuidesCheckBox.setSelected(Configuration.showGuides.get());
        c.gridy++;
        centralPanel.add(showGuidesCheckBox, c);
                
        snapToGuidesCheckBox = new JCheckBox(translate("snapTo"));
        snapToGuidesCheckBox.setSelected(Configuration.snapToGuides.get());
        c.gridy++;
        centralPanel.add(snapToGuidesCheckBox, c);
        
        lockGuidesCheckBox = new JCheckBox(translate("lock"));
        lockGuidesCheckBox.setSelected(Configuration.lockGuides.get());
        c.gridy++;
        centralPanel.add(lockGuidesCheckBox, c);
        
        JLabel snapAccuracyLabel = new JLabel(translate("snapAccuracy"));
        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.LINE_END;        
        centralPanel.add(snapAccuracyLabel, c);
        
        snapAccuracyComboBox = new JComboBox<>(
                new AccuracyItem[] {
                    new AccuracyItem(GuidesSnapAccuracy.MUST_BE_CLOSE),
                    new AccuracyItem(GuidesSnapAccuracy.NORMAL),
                    new AccuracyItem(GuidesSnapAccuracy.CAN_BE_DISTANT)
                }
        );
        snapAccuracyLabel.setLabelFor(snapAccuracyComboBox);
        
        snapAccuracyComboBox.setSelectedItem(new AccuracyItem(Configuration.guidesSnapAccuracy.get()));
        
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
                
        JButton clearGuidesButton = new JButton(translate("clear"));
        clearGuidesButton.addActionListener(this::clearButtonActionPerformed);
        
        buttonsPanel.add(okButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createVerticalStrut(5));
        buttonsPanel.add(clearGuidesButton);
        
        cnt.add(buttonsPanel, BorderLayout.EAST);
                        
        pack();
        View.setWindowIcon16(this, "guides");
        View.centerScreen(this);
        setModal(true);            
        setResizable(false);
        this.mediaDisplay = mediaDisplay;
    }
    
    private void clearButtonActionPerformed(ActionEvent evt) {
        mediaDisplay.clearGuides();
    }
    
    private void okButtonActionPerformed(ActionEvent evt) {
        Configuration.guidesColor.set(colorSelection.getValue());
        Configuration.showGuides.set(showGuidesCheckBox.isSelected());
        Configuration.snapToGuides.set(snapToGuidesCheckBox.isSelected());
        Configuration.lockGuides.set(lockGuidesCheckBox.isSelected());
        Configuration.guidesSnapAccuracy.set(((AccuracyItem) snapAccuracyComboBox.getSelectedItem()).acurracy);
        setVisible(false);
    }
    
    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }
    
    private class AccuracyItem {
        private GuidesSnapAccuracy acurracy;

        public AccuracyItem(GuidesSnapAccuracy acurracy) {
            this.acurracy = acurracy;
        }

        public GuidesSnapAccuracy getAcurracy() {
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
            }
            return "unknown";
        }                
    }
    
}
