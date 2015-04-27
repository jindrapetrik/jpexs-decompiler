/*
 *  Copyright (C) 2010-2015 JPEXS
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
import com.jpexs.decompiler.flash.gui.helpers.TableLayoutHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import layout.TableLayout;

/**
 *
 * @author JPEXS
 */
public class HeaderInfoPanel extends JPanel {

    private final JLabel signatureLabel = new JLabel();

    private final JLabel compressionLabel = new JLabel();

    private final JLabel gfxLabel = new JLabel();

    private final JLabel versionLabel = new JLabel();

    private final JLabel fileSizeLabel = new JLabel();

    private final JLabel frameRateLabel = new JLabel();

    private final JLabel frameCountLabel = new JLabel();

    private final JLabel displayRectTwipsLabel = new JLabel();

    private final JLabel displayRectPixelsLabel = new JLabel();
    
    private final JComboBox<String> versionComboBox = new JComboBox<>();
    
    private final JCheckBox gfxCheckBox = new JCheckBox();
    
    private final JSpinner frameRateEditor = new JSpinner();
    
    private final JPanel propertiesPanel = new JPanel();

    private final JPanel buttonsPanel = new JPanel();

    private final JButton editButton = new JButton(AppStrings.translate("button.edit"), View.getIcon("edit16"));

    private final JButton saveButton = new JButton(AppStrings.translate("button.save"), View.getIcon("save16"));

    private final JButton cancelButton = new JButton(AppStrings.translate("button.cancel"), View.getIcon("cancel16"));
    
    private final JPanel displayRectEditorPanel = new JPanel();
    
    private final JSpinner xMinEditor = new JSpinner();
    
    private final JSpinner xMaxEditor = new JSpinner();
    
    private final JSpinner yMinEditor = new JSpinner();
    
    private final JSpinner yMaxEditor = new JSpinner();

    private SWF swf;

    public HeaderInfoPanel() {
        setLayout(new BorderLayout());
        
        TableLayout tl;
        propertiesPanel.setLayout(tl = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, 
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED}
        }));

        displayRectEditorPanel.setLayout(new FlowLayout(SwingConstants.WEST));
        xMinEditor.setPreferredSize(new Dimension(80, xMinEditor.getPreferredSize().height));
        xMaxEditor.setPreferredSize(new Dimension(80, xMaxEditor.getPreferredSize().height));
        yMinEditor.setPreferredSize(new Dimension(80, yMinEditor.getPreferredSize().height));
        yMaxEditor.setPreferredSize(new Dimension(80, yMaxEditor.getPreferredSize().height));
        displayRectEditorPanel.add(xMinEditor);
        displayRectEditorPanel.add(xMaxEditor);
        displayRectEditorPanel.add(yMinEditor);
        displayRectEditorPanel.add(yMaxEditor);
        
        propertiesPanel.add(new JLabel(AppStrings.translate("header.signature")), "0,0");
        propertiesPanel.add(signatureLabel, "1,0");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.compression")), "0,1");
        propertiesPanel.add(compressionLabel, "1,1");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.version")), "0,2");
        propertiesPanel.add(versionLabel, "1,2");
        propertiesPanel.add(versionComboBox, "1,2");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.gfx")), "0,3");
        propertiesPanel.add(gfxLabel, "1,3");
        propertiesPanel.add(gfxCheckBox, "1,3");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.filesize")), "0,4");
        propertiesPanel.add(fileSizeLabel, "1,4");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framerate")), "0,5");
        propertiesPanel.add(frameRateLabel, "1,5");
        propertiesPanel.add(frameRateEditor, "1,5");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framecount")), "0,6");
        propertiesPanel.add(frameCountLabel, "1,6");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.displayrect")), "0,7");
        propertiesPanel.add(displayRectTwipsLabel, "1,7");
        propertiesPanel.add(displayRectEditorPanel, "1,7");
        propertiesPanel.add(new JLabel(""), "0,8");
        propertiesPanel.add(displayRectPixelsLabel, "1,8");

        for (int i = 1; i <= SWF.MAX_VERSION; i++) {
            versionComboBox.addItem(Integer.toString(i));
        }

        add(propertiesPanel, BorderLayout.CENTER);
        
        editButton.setVisible(false);
        editButton.addActionListener(this::editButtonActionPerformed);
        
        saveButton.setVisible(false);
        saveButton.addActionListener(this::saveButtonActionPerformed);

        cancelButton.setVisible(false);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        buttonsPanel.add(editButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        TableLayoutHelper.addTableSpaces(tl, 10);
        setEditMode(false);
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        setEditMode(true);
    }
    
    private void saveButtonActionPerformed(ActionEvent evt) {
        swf.version = Integer.parseInt((String) versionComboBox.getSelectedItem());
        swf.gfx = gfxCheckBox.isSelected();
        swf.frameRate = (int) frameRateEditor.getModel().getValue();
        swf.displayRect.Xmin = (int) xMinEditor.getModel().getValue();
        swf.displayRect.Xmax = (int) xMaxEditor.getModel().getValue();
        swf.displayRect.Ymin = (int) yMinEditor.getModel().getValue();
        swf.displayRect.Ymax = (int) yMaxEditor.getModel().getValue();
        
        load(swf);
        setEditMode(false);
    }
    
    private void cancelButtonActionPerformed(ActionEvent evt) {
        load(swf);
        setEditMode(false);
    }
    
    public void load(SWF swf) {
        this.swf = swf;
        signatureLabel.setText(swf.getHeaderBytes());
        switch (swf.compression) {
            case LZMA:
                compressionLabel.setText(AppStrings.translate("header.compression.lzma"));
                break;
            case ZLIB:
                compressionLabel.setText(AppStrings.translate("header.compression.zlib"));
                break;
            case NONE:
                compressionLabel.setText(AppStrings.translate("header.compression.none"));
                break;
        }

        versionLabel.setText(Integer.toString(swf.version));
        versionComboBox.setSelectedItem(Integer.toString(swf.version));

        gfxLabel.setText(swf.gfx ? AppStrings.translate("yes") : AppStrings.translate("no"));
        gfxCheckBox.setSelected(swf.gfx);
        
        fileSizeLabel.setText(Long.toString(swf.fileSize));
        
        frameRateLabel.setText(Integer.toString(swf.frameRate));
        frameRateEditor.setModel(new SpinnerNumberModel(swf.frameRate, -0x80000000, 0x7fffffff, 1));
        
        frameCountLabel.setText("" + swf.frameCount);
        displayRectTwipsLabel.setText(AppStrings.translate("header.displayrect.value.twips")
                .replace("%xmin%", Integer.toString(swf.displayRect.Xmin))
                .replace("%ymin%", Integer.toString(swf.displayRect.Ymin))
                .replace("%xmax%", Integer.toString(swf.displayRect.Xmax))
                .replace("%ymax%", Integer.toString(swf.displayRect.Ymax)));
        displayRectPixelsLabel.setText(AppStrings.translate("header.displayrect.value.pixels")
                .replace("%xmin%", fmtDouble(swf.displayRect.Xmin / SWF.unitDivisor))
                .replace("%ymin%", fmtDouble(swf.displayRect.Ymin / SWF.unitDivisor))
                .replace("%xmax%", fmtDouble(swf.displayRect.Xmax / SWF.unitDivisor))
                .replace("%ymax%", fmtDouble(swf.displayRect.Ymax / SWF.unitDivisor)));
        
        xMinEditor.setModel(new SpinnerNumberModel(swf.displayRect.Xmin, -0x80000000, 0x7fffffff, 1));
        xMaxEditor.setModel(new SpinnerNumberModel(swf.displayRect.Xmax, -0x80000000, 0x7fffffff, 1));
        yMinEditor.setModel(new SpinnerNumberModel(swf.displayRect.Ymin, -0x80000000, 0x7fffffff, 1));
        yMaxEditor.setModel(new SpinnerNumberModel(swf.displayRect.Ymax, -0x80000000, 0x7fffffff, 1));

        setEditMode(false);
    }

    public void clear() {
        swf = null;
    }

    private String fmtDouble(double d) {
        String r = "" + d;
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }
        return r;
    }
    
    private void setEditMode(boolean edit) {
        versionLabel.setVisible(!edit);
        versionComboBox.setVisible(edit);
        gfxLabel.setVisible(!edit);
        gfxCheckBox.setVisible(edit);
        frameRateLabel.setVisible(!edit);
        frameRateEditor.setVisible(edit);
        
        displayRectTwipsLabel.setVisible(!edit);
        displayRectPixelsLabel.setVisible(!edit);
        displayRectEditorPanel.setVisible(edit);
        
        editButton.setVisible(!edit);
        saveButton.setVisible(edit);
        cancelButton.setVisible(edit);
    }
}
