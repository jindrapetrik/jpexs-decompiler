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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.xfl.FLAVersion;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class FlaExportDialog extends AppDialog {
    
    private int result = ERROR_OPTION;
    
    private JComboBox<FlaVersionItem> combo;
    
    private JCheckBox compressedCheckBox;

    private JSlider tweenQualitySlider;
    
    public FlaExportDialog(Window owner) {
        super(owner);
        setTitle(translate("dialog.title"));
        
        Container cnt = getContentPane();
        
        
        JPanel comboPanel = new JPanel(new FlowLayout());
        combo = new JComboBox<>();
        
        combo.addActionListener(this::comboActionPerformed);       
        
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lab = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof FlaVersionItem) {
                    FLAVersion ver = ((FlaVersionItem) value).flaVersion;
                    lab.setIcon(View.getIcon("flash/" + ver.shortName().toLowerCase()));
                }                
                return lab;
            }            
        });
        
        JLabel comboLabel = new JLabel(translate("version"));
        comboPanel.add(comboLabel);
        comboLabel.setLabelFor(combo);
        comboPanel.add(combo);
        comboPanel.setAlignmentX(Component.CENTER_ALIGNMENT);        
        
        compressedCheckBox = new JCheckBox(translate("compressed"));
        compressedCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(comboPanel);
        centerPanel.add(compressedCheckBox);

        JPanel qualityPanel = new JPanel(new BorderLayout(8, 4));
        qualityPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        JPanel qualityLabelPanel = new JPanel(new FlowLayout());
        JLabel qualityLabel = new JLabel(translate("tweenDetectionQuality"));
        JLabel qualityValue = new JLabel();
        qualityLabelPanel.add(qualityLabel);
        qualityLabelPanel.add(qualityValue);
        tweenQualitySlider = new JSlider(0, 100, Math.max(0, Math.min(100, Configuration.lastFlaExportTweenDetectionQuality.get())));
        tweenQualitySlider.setMajorTickSpacing(25);
        tweenQualitySlider.setMinorTickSpacing(5);
        tweenQualitySlider.setPaintTicks(true);
        tweenQualitySlider.setPreferredSize(new Dimension(360, tweenQualitySlider.getPreferredSize().height));
        // Standalone labels avoid HiDPI clipping in the slider's label renderer.
        JPanel qualityEndpoints = new JPanel(new GridLayout(1, 2));
        qualityEndpoints.add(new JLabel(translate("tweenDetectionQuality.off"), SwingConstants.LEADING));
        qualityEndpoints.add(new JLabel(translate("tweenDetectionQuality.high"), SwingConstants.TRAILING));
        tweenQualitySlider.addChangeListener(e -> qualityValue.setText(Integer.toString(tweenQualitySlider.getValue())));
        qualityValue.setText(Integer.toString(tweenQualitySlider.getValue()));
        qualityLabel.setLabelFor(tweenQualitySlider);
        qualityLabel.setToolTipText(translate("tweenDetectionQuality.tooltip"));
        tweenQualitySlider.setToolTipText(translate("tweenDetectionQuality.tooltip"));
        qualityPanel.add(qualityLabelPanel, BorderLayout.NORTH);
        qualityPanel.add(tweenQualitySlider, BorderLayout.CENTER);
        qualityPanel.add(qualityEndpoints, BorderLayout.SOUTH);
        qualityPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(qualityPanel);
        
        cnt.add(centerPanel, BorderLayout.CENTER);
        
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        pack();
        View.centerScreen(this);
        View.setWindowIcon(this, "flash");
        getRootPane().setDefaultButton(okButton);
        setModal(true);
    }
    
    private void comboActionPerformed(ActionEvent e) {
        int selectedIndex = combo.getSelectedIndex();
        if (selectedIndex > -1) {

            FlaVersionItem item = (FlaVersionItem) combo.getItemAt(selectedIndex);
            if (item != null) {
                if (item.flaVersion.xflVersion() == null) {
                    compressedCheckBox.setSelected(true);
                }
                compressedCheckBox.setEnabled(item.flaVersion.xflVersion() != null);
            }
        }
    }
    
    
    private class FlaVersionItem {
        private final FLAVersion flaVersion;
        private final boolean recommended;

        public FlaVersionItem(FLAVersion flaVersion, boolean recommended) {
            this.flaVersion = flaVersion;
            this.recommended = recommended;
        }                

        @Override
        public String toString() {
            return flaVersion.applicationName() + " " + (recommended ? translate("recommended") : "");
        }                
    }
    
    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
        }
        super.setVisible(b);
    }
    
    public boolean isCompressed() {
        return compressedCheckBox.isSelected();
    }
    
    public int getTweenDetectionQuality() {
        return tweenQualitySlider.getValue();
    }

    public FLAVersion getFlaVersion() {
        if (combo.getSelectedItem() == null) {
            return null;
        }
        return ((FlaVersionItem) combo.getSelectedItem()).flaVersion;
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        Configuration.lastFlaExportTweenDetectionQuality.set(getTweenDetectionQuality());
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }
    
    public int showExportDialog(SWF swf) {
        DefaultComboBoxModel<FlaVersionItem> model = new DefaultComboBoxModel<>();
        boolean recommended = true;
        boolean isAs3 = swf.isAS3();
        int recommendedIndex = -1;
        for (FLAVersion ver : FLAVersion.values()) {
            if (ver.minASVersion() == 3 && !isAs3) {
                continue;
            }
            if (ver.maxASVersion() < 3 && isAs3) {
                continue;
            }
            if (swf.version <= ver.maxSwfVersion()) {
                model.addElement(new FlaVersionItem(ver, recommended));
                if (recommended) {
                    recommendedIndex = model.getSize() - 1;
                }
                recommended = false;                
            } else {
                model.addElement(new FlaVersionItem(ver, false));
            }
        }
        combo.setModel(model);
        if (recommendedIndex > -1) {
            combo.setSelectedIndex(recommendedIndex);
            compressedCheckBox.setSelected(Configuration.lastFlaExportCompressed.get());
            comboActionPerformed(null);
        }
        tweenQualitySlider.setValue(Math.max(0, Math.min(100, Configuration.lastFlaExportTweenDetectionQuality.get())));
        pack();
        setVisible(true);        
        return result;
    }
}
