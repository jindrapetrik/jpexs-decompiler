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
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author JPEXS
 */
public class ExportSubspriteAnimationDialog extends AppDialog {
    
    private int result = ERROR_OPTION;
    
    private JTextField lengthTextField = new JTextField("2", 3);
    private JComboBox<String> formatComboBox;
    private final MainPanel mainPanel;
    private JButton okButton;
    
    private JTextField zoomTextField = new JTextField(4);
    
    private JCheckBox transparentFrameBackgroundCheckBox;
    
    private List<FrameExportMode> modes = new ArrayList<>();
    
    public ExportSubspriteAnimationDialog(MainPanel mainPanel, Window owner) {
        super(owner);
                        
        setTitle(translate("dialog.title"));
        Container cnt = getContentPane();
        
        setSize(800, 600);
        
        cnt.setLayout(new BorderLayout());
        
        JPanel centralPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;                
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;                
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centralPanel.add(new JLabel(translate("format")), gbc);
        Vector<String> optionNames = new Vector<>();
        
        String exportFormatsStr = Configuration.lastSelectedExportFormats.get();
        if ("".equals(exportFormatsStr)) {
            exportFormatsStr = null;
        }

        String[] exportFormatsArr = new String[0];
        if (exportFormatsStr != null) {
            if (exportFormatsStr.contains(",")) {
                exportFormatsArr = exportFormatsStr.split(",");
            } else {
                exportFormatsArr = new String[]{exportFormatsStr};
            }

        }

        List<String> exportFormats = Arrays.asList(exportFormatsArr);
        
        int selectedFormat = -1;
        for (FrameExportMode mode : FrameExportMode.values()) {
            if (mode == FrameExportMode.SWF
                    || mode == FrameExportMode.CANVAS) {
                continue;
            }
            String key = "frames." + mode.toString().toLowerCase(Locale.ENGLISH);            
            optionNames.add(AppStrings.translate(ExportDialog.class, key));
            if (exportFormats.contains(key)) {
                selectedFormat = modes.size();
            }
            modes.add(mode);
        }
        
        gbc.gridx++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        formatComboBox = new JComboBox<>(optionNames);
        if (selectedFormat != -1) {
            formatComboBox.setSelectedIndex(selectedFormat);
        }
        centralPanel.add(formatComboBox, gbc);
        
               
        gbc.gridy++;
        gbc.gridwidth = 2;        
        transparentFrameBackgroundCheckBox = new JCheckBox(AppStrings.translate(ExportDialog.class, "transparentFrameBackground"));
        transparentFrameBackgroundCheckBox.setVisible(true);
        centralPanel.add(transparentFrameBackgroundCheckBox, gbc);
        if (Configuration.lastExportTransparentBackground.get()) {
            transparentFrameBackgroundCheckBox.setSelected(true);
        }

        JLabel zlab = new JLabel(translateTitle("zoom"));
        JLabel pctLabel = new JLabel(AppStrings.translate(ExportDialog.class, "zoom.percent"));   
        zlab.setLabelFor(zoomTextField);
        
        String pct = "" + Configuration.lastSelectedExportZoom.get() * 100;
        if (pct.endsWith(".0")) {
            pct = pct.substring(0, pct.length() - 2);
        }

        zoomTextField.setText(pct);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;            
        gbc.anchor = GridBagConstraints.LINE_END;
        centralPanel.add(zlab, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.LINE_START;
        centralPanel.add(zoomTextField, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.LINE_START;            
        centralPanel.add(pctLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;              
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centralPanel.add(new JLabel(translate("length")), gbc);                                
        
        gbc.gridx++;        
        gbc.fill = GridBagConstraints.NONE;
        lengthTextField.setMinimumSize(lengthTextField.getPreferredSize());
        lengthTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }
            
        });
        centralPanel.add(lengthTextField, gbc);
        
        gbc.gridx++;
        centralPanel.add(new JLabel(translate("frames")), gbc);                
        
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        
        cnt.add(centralPanel, BorderLayout.CENTER);
        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        
        cnt.setMinimumSize(cnt.getPreferredSize());
        setSize(350, 200);
        View.centerScreen(this);
        View.setWindowIcon(this, "export");
        getRootPane().setDefaultButton(okButton);
        setModal(true);
        this.mainPanel = mainPanel;
    }
    
    private void check() {
        try {
            int len = Integer.parseInt(lengthTextField.getText());
            okButton.setEnabled(len > 1);
        } catch (NumberFormatException nfe) {
            okButton.setEnabled(false);
        }
    }
    
    private void okButtonActionPerformed(ActionEvent evt) {                
        result = OK_OPTION;    
        try {
            saveConfig();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(ExportSubspriteAnimationDialog.this, AppStrings.translate(ExportDialog.class, "zoom.invalid"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            zoomTextField.requestFocusInWindow();
            return;
        }
        setVisible(false);
    }
    
    private void saveConfig() {
        String format = getFormat().toString().toLowerCase(Locale.ENGLISH);
        String formats = Configuration.lastSelectedExportFormats.get();
        String[] parts = formats.split(",", -1);
        List<String> newFormats = new ArrayList<>();
        for (String part : parts) {
            if (part.startsWith("frames.")) {
                newFormats.add("frames." + format);
            } else {
                newFormats.add(part);
            }
        }
        String cfg = String.join(",", newFormats);
        Configuration.lastSelectedExportZoom.set(Double.parseDouble(zoomTextField.getText()) / 100);
        Configuration.lastSelectedExportFormats.set(cfg);
        Configuration.lastExportTransparentBackground.set(transparentFrameBackgroundCheckBox.isSelected());
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
    
    public int getLength() {
        try {
            return Integer.parseInt(lengthTextField.getText());
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
    
    public FrameExportMode getFormat() {
        return modes.get(formatComboBox.getSelectedIndex());
    }
    
    private String translateTitle(String title) {
        return AppStrings.translate(ExportDialog.class, "titleFormat").replace("%title%", AppStrings.translate(ExportDialog.class, title));
    }
    
    public boolean isTransparentFrameBackgroundEnabled() {
        return transparentFrameBackgroundCheckBox.isSelected();
    }

    public double getZoom() {
        return Double.parseDouble(zoomTextField.getText()) / 100;
    }
}
